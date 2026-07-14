package com.uniinformation.erpv4;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ChnftrBuilder;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.TextSpliter;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.BiUtil.CheckedConsumer2;
import com.uniinformation.utils.BiUtil.CheckedConsumer3;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiUiExecutor;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import static com.uniinformation.utils.ZkUtil.throwIntFunction;

public abstract class BatchBuildPrintHandler extends BiActionHandler implements JxActionListener {
	protected Rectangle pageSize = PageSize.A4;
	protected float docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(20);
	protected float docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(20);
	protected int docWidth = ChnftrParser.pxToDpi100(docWidthPx);
	protected int docHeight = ChnftrParser.pxToDpi100(docHeightPx);
	protected int fontPt = 12;
	protected int rowHeight = 40;
	protected int lineHeight = 20;
	protected int maxBottom = 0;
	protected String engFont = "helv_nr";
	protected String chnFont = "msheis";

	protected ChnftrBuilder builder;
	protected int fetchIndex, recordCount, recordCount2;

	protected int maxDocCount = 10000;
	protected JxZkBiBase jxf;
	protected SessionHelper sh;
	protected BiResult br;
    protected PRINT_MODE printMode = PRINT_MODE.SHOW_DIALOG;
    protected boolean needDownloadRename;
    
    public static enum PRINT_MODE { SHOW_DIALOG, SHOW_PREVIEW, DOWNLOAD };
    public static Rectangle A5L = fixPageSize(PageSize.A5.rotate());
    
    public static Rectangle fixPageSize(Rectangle ps) {
		return new Rectangle(0, 0, ps.getWidth(), ps.getHeight());
    }
    
	public BatchBuildPrintHandler(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		useAsync = p_bibase != null ? p_bibase.getSessionHelper().getAllowBatchPrtdocAsync() : false;
	}

	public BatchBuildPrintHandler() {
		this(null);
	}

	protected ReturnMsg initBuilder() {
		builder = new ChnftrBuilder();
		fetchIndex = 0;
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
	
	public byte[] getBuilderData() throws UnsupportedEncodingException {
		return builder.toBytes(StandardCharsets.UTF_8.name());
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		sh = p_result.getSessionHelper();
		if (cnt > maxDocCount)
			return new ReturnMsg(false,sh.getLabel("Cannot Print more than 10000 documents"));
		recordCount2 = cnt;
		ReturnMsg rtn = initBuilder();
		if (rtn != null && !rtn.getStatus()) return(rtn);
		return ReturnMsg.defaultOk;		
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			if (!p_result.fetchOneRecV(p_recIdx)) 
				return new ReturnMsg(false,sh.getLabel("Fetch Record failed"));
			fetchIndex = p_recIdx;
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
			jxf = (JxZkBiBase) field.getJxForm();
			br = jxf.getBr();
			sh = br.getSessionHelper();
			recordCount2 = 1;
			initBuilder();
			print();
			ZkUtil.showPdfDialog((Component) field.getJxForm().getNativeComponent(), sh, getPrintData(), getDocumentName(br), needDownloadRename);
		} catch (Exception ex) {
			UniLog.log(ex); 
			Messagebox.show(ex.toString());
		}
	}

	public void print(BiResult p_br) throws Exception {
		br = p_br;
		sh = br.getSessionHelper();
		recordCount2 = 1;
		initBuilder();
		print();
	}
	
	private void printData(byte[] data) {
		switch (printMode) {
		case SHOW_DIALOG:
			ZkUtil.showPdfDialog((Component) biBase.getRootComponent(), sh, data, getDocumentName(br), needDownloadRename);
			break;
		case SHOW_PREVIEW:
			ZkUtil.printFromStream(new ByteArrayInputStream(data), "application/pdf", sh);
			break;
		case DOWNLOAD:
			if (needDownloadRename) {
				try {
					ZkUtil.downloadFileByRenameDlg(sh, getDocumentName(br), "application/pdf", data, null);
				} catch (Exception e) {
					UniLog.log(e);
				}
			} else
				Filedownload.save(data, "application/pdf", getDocumentName(br) + ".pdf");
			break;
		}
	}

	protected String getDocumentName(BiResult p_br) {
		return br.getView().getHeader();
	}
	
	public static void changeDocLineHeight(Cell cell, int lineHeight) {
		cell.addAnyItem(new ChangeLpiItem()).setLineHeight(lineHeight);
	}

	protected abstract class PrintTemplate1 {
		protected LinkedList<Cell> cellList = new LinkedList<>();
		
		public void startPrint() {
			cellList.clear();
		}

		public void endPrint(CheckedConsumer3<Cell, Integer, Integer> cb) throws Exception {
			if (recordCount > 0)
				builder.P();
			int pageNum = 0;
			for (Cell cell : cellList) {
				if (pageNum > 0)
					builder.P();
				if (cb != null)
					cb.accept(cell, pageNum, cellList.size());
				cell.build();
				pageNum++;
			}
			recordCount++;
		}

		public Phrase[] buildAdjustPhrase(int type, String... texts) throws Exception {
			return buildAdjustPhrase(type, rowHeight, lineHeight, texts);
		}

		public Phrase[] buildAdjustPhrase(int type, int rowHeight, int lineHeight, String... texts) throws Exception {
			return buildAdjustPhrase(type, rowHeight, lineHeight, true, null, texts);
		}

		public Phrase[] buildAdjustPhrase(int type, int rowHeight, int lineHeight, boolean canNextPage, CheckedConsumer2<Integer, Phrase> cbRemark, String... texts) throws Exception {
			Phrase ph = buildPhrase(type, rowHeight, texts);
			AtomicInteger maxHeight = new AtomicInteger();
			ph.getGroupList().stream().filter(x -> x instanceof TextCell).map(x -> (TextCell)x).forEach(tc -> {
				TextItem ti = tc.getTextItem();
				TextSpliter ts = new TextSpliter(ti.getText(), ti.getEngFontFace(), ti.getChnFontFace(), ti.getFontSize(), tc.getWidth() - ti.getX() * 2);
				ti.setText(String.join("\n", ts.getResultList()));
				maxHeight.set(Math.max(maxHeight.get(), ts.getResultCount() - 1));
			});
			ph.addHeight(lineHeight * maxHeight.get());
			if (!canNextPage)
				return new Phrase[] { ph };
			Cell cell = nextCellOrNot(0);
			if (cell == null) {
				cellList.getLast().removeItem(ph);
				cell = newCell();
				Phrase lastPhrase = cell.getAnyItem("lastPhrase");
				ph.setY(lastPhrase.getY() + lastPhrase.getHeight());
				cell.addItem("lastPhrase", ph);
				if (nextCellOrNot(0) == null) {
					cell.removeItem(ph);
					cell.addItemToMap("lastPhrase", lastPhrase);
					return buildRemarkPhrase(type, lineHeight, cbRemark, texts);
				}
			}
			return new Phrase[] { ph };
		}

		public Phrase[] buildRemarkPhrase(int type, String... texts) throws Exception {
			return buildRemarkPhrase(type, lineHeight, texts);
		}

		public Phrase[] buildRemarkPhrase(int type, int lineHeight, String... texts) throws Exception {
			return buildRemarkPhrase(type, lineHeight, null, texts);
		}

		public Phrase[] buildRemarkPhrase(int type, int lineHeight, CheckedConsumer2<Integer, Phrase> cb, String... texts) throws Exception {
			Cell cell = nextCell(lineHeight);
			Phrase lastPhrase = cell.getAnyItem("lastPhrase");
			Phrase ph = buildPhrase(type, lineHeight, texts);
			List<String[]> list = ph.getGroupList().stream().map(x -> {
				if (x instanceof TextCell) {
					TextItem ti = ((TextCell)x).getTextItem();
					TextSpliter ts = new TextSpliter(ti.getText(), ti.getEngFontFace(), ti.getChnFontFace(), ti.getFontSize(), ((TextCell)x).getWidth() - ti.getX() * 2);
					return ts.getResultList().stream().toArray(String[]::new);
				} else
					return new String[0];
			}).collect(Collectors.toList());
			cell.removeItem(ph);
			cell.addItemToMap("lastPhrase", lastPhrase);

			int rowCount = list.stream().mapToInt(ss -> ss.length).max().orElse(0);
			int colCount = list.size();
			return IntStream.range(0, rowCount).mapToObj(throwIntFunction(row -> {
				Phrase ph1 = buildPhrase(type, lineHeight, IntStream.range(0, colCount).mapToObj(col -> {
					String[] ss = list.get(col);
					return row < ss.length ? ss[row] : "";
				}).toArray(String[]::new));
				if (cb != null)
					cb.accept(row, ph1);
				return ph1;
			})).toArray(Phrase[]::new);
		}

		public void buildHeightPhrase(int height, boolean needNextPageAdd) throws Exception {
			Cell lastCell = null;
			if (!cellList.isEmpty())
				lastCell = cellList.getLast();
			Cell cell = nextCell(height);
			Phrase lastPhrase = cell.getAnyItem("lastPhrase");
			if (needNextPageAdd || lastCell == null || cell == lastCell)
				cell.addPhrase("lastPhrase", 0, lastPhrase.getY() + lastPhrase.getHeight()).setHeight(height);
		}
		
		public void changeDocLineHeight(int lineHeight) throws Exception {
			BatchBuildPrintHandler.changeDocLineHeight(cellList.getLast(), lineHeight);
		}

		public void restoreDocLineHeight() throws Exception {
			BatchBuildPrintHandler.changeDocLineHeight(cellList.getLast(), lineHeight);
		}

		protected Cell nextCellOrNot(int rowHeight) {
			Cell cell = null;
			if (!cellList.isEmpty()) {
				cell = cellList.getLast();
				Phrase lastPhrase = cell.getAnyItem("lastPhrase");
				//UniLog.log1("nextCellOrNot:%d, %d", lastPhrase.getY() + lastPhrase.getHeight() + rowHeight, docHeight - maxBottom);
				if (lastPhrase.getY() + lastPhrase.getHeight() + rowHeight > docHeight - maxBottom)
					cell = null;
			}
			return cell;
		}

		protected Cell nextCell(int rowHeight) throws Exception {
			Cell cell = nextCellOrNot(rowHeight);
			if (cell == null)
				cell = newCell();
			return cell;
		}

		protected abstract Cell newCell() throws Exception;

		public Phrase buildPhrase(int type, int rowHeight) throws Exception {
			Cell cell = nextCell(rowHeight);
			Phrase lastPhrase = cell.getAnyItem("lastPhrase");
			return buildPhrase(cell, lastPhrase.getY() + lastPhrase.getHeight(), type);
		}

		public Phrase buildPhrase(int type, String... texts) throws Exception {
			return buildPhrase(type, rowHeight, texts);
		}

		public Phrase buildPhrase(int type, int rowHeight, String... texts) throws Exception {
			return buildPhrase(type, rowHeight).setGroupsTexts(null, texts);
		}

		public Phrase buildPhrase(Cell cell, int y, int type, String... texts) {
			return buildPhrase(cell, y, type).setGroupsTexts(null, texts);
		}

		protected abstract Phrase buildPhrase(Cell cell, int y, int type);
		
		public <T> Stream<T> getItemStream(String name, Class<T> cls) {
			return cellList.stream().map(cell -> (T)cell.getAnyItem(name)).filter(item -> item != null);
		}
	}
}
