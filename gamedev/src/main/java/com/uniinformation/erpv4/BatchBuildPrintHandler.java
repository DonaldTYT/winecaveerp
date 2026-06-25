package com.uniinformation.erpv4;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ChnftrBuilder;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiUiExecutor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;

public abstract class BatchBuildPrintHandler extends BiActionHandler implements JxActionListener {
	protected Rectangle pageSize = PageSize.A4;
	protected float docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(20);
	protected float docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(20);
	protected int docWidth = ChnftrParser.pxToDpi100(docWidthPx);
	protected int docHeight = ChnftrParser.pxToDpi100(docHeightPx);
	protected int fontPt = 12;
	protected int rowHeight = 40;
	protected int lineHeight = 20;
	protected String engFont = "helv_nr";
	protected String chnFont = "msheis";

	protected ChnftrBuilder builder;
	protected int recordCount;

	protected int maxDocCount = 10000;
	protected SessionHelper sh;
	protected BiResult br;
    protected PRINT_MODE printMode = PRINT_MODE.SHOW_DIALOG;
    
    public static enum PRINT_MODE { SHOW_DIALOG, SHOW_PREVIEW, DOWNLOAD };

	public BatchBuildPrintHandler(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		useAsync = p_bibase != null ? p_bibase.getSessionHelper().getAllowBatchPrtdocAsync() : false;
	}

	public BatchBuildPrintHandler() {
		this(null);
	}

	protected ReturnMsg initBuilder() {
		builder = new ChnftrBuilder();
		recordCount = 0;
		return ReturnMsg.defaultOk;
	}

	protected abstract void print() throws Exception;

	protected ChnftrParser createChnftrParser(String buildStr) throws Exception {
		ChnftrParser p = new ChnftrParser(buildStr, StandardCharsets.UTF_8.name(), pageSize, docWidthPx, docHeightPx, ChnftrParser.CHNFTR_DPI, 11, ChnftrParser.CHNFTR_DPI / lineHeight);
		p.setUseAscender(false);
		return p;
	}

	protected byte[] getPrintData() throws Exception {
		return createChnftrParser(builder.toString()).printToData();
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		sh = p_result.getSessionHelper();
		if (cnt > maxDocCount)
			return new ReturnMsg(false,sh.getLabel("Cannot Print more than 10000 documents"));
		ReturnMsg rtn = initBuilder();
		if (rtn != null && !rtn.getStatus()) return(rtn);
		return ReturnMsg.defaultOk;		
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			if (!p_result.fetchOneRecV(p_recIdx)) 
				return new ReturnMsg(false,sh.getLabel("Fetch Record failed"));
			br = p_result;
			print();
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,String.format(sh.getLabel("Print Document %d Failed"), p_recIdx)));
		}
	}

	@Override
	public ReturnMsg afterAction(BiResult br) {
		try {
			printData(getPrintData());
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,sh.getLabel("Print failed")));
		}
	}

	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		CompletableFuture.supplyAsync(() -> {
			try {
				return getPrintData();
			} catch (Exception e) {
				UniLog.log(e);
				return null;
			}
		}).thenAcceptAsync(data -> {
			biBase.hideProgressPanel();
			if (data != null) {
				printData(data);
				cb.callback(ReturnMsg.defaultOk);
			} else
				cb.callback(new ReturnMsg(false,sh.getLabel("Print failed")));
		}, new ZkBiUiExecutor(biBase.getRootComponent(), () -> {
			biBase.setProgressPanelProgress("Write document", 0);
		}));
	}

	@Override
	public void actionPerformed(JxField field) {
		try {
			JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
			br = jxf.getBr();
			sh = br.getSessionHelper();
			initBuilder();
			print();
			ZkUtil.showPdfDialog((Component) field.getJxForm().getNativeComponent(), sh, getPrintData(), getDocumentName(br));
		} catch (Exception ex) {
			UniLog.log(ex); 
			Messagebox.show(ex.toString());
		}
	}
	
	private void printData(byte[] data) {
		switch (printMode) {
		case SHOW_DIALOG:
			ZkUtil.showPdfDialog((Component) biBase.getRootComponent(), sh, data, getDocumentName(br));
			break;
		case SHOW_PREVIEW:
			ZkUtil.printFromStream(new ByteArrayInputStream(data), "application/pdf", sh);
			break;
		case DOWNLOAD:
   		    Filedownload.save(data, "application/pdf", getDocumentName(br) + ".pdf");
			break;
		}
	}

	protected abstract String getDocumentName(BiResult p_br);
}
