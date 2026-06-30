package com.uniinformation.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Timer;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.utils.PrtdocJsonChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public abstract class BatchPrtdocHandler extends BiActionHandler implements PrtdocInterface,JxActionListener {
	public BatchPrtdocHandler(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		if(p_bibase != null) useAsync = p_bibase.getSessionHelper().getAllowBatchPrtdocAsync(); else useAsync = false;
	}

	protected int maxDocCount = 10000;
	protected SessionHelper sh;
	protected BiResult br;
	protected boolean skipFetch = false;
    protected PrtdocJson ppj;
    protected int docCnt;
    protected boolean batchDownloadReport = false;
    
	abstract protected ReturnMsg initPrtdoc();
	abstract protected String getDocumentName(BiResult p_br);
	
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		// TODO Auto-generated method stub
		sh = p_result.getSessionHelper();
		if(cnt > maxDocCount) {
			return(new ReturnMsg(false,sh.getLabel("Cannot Print more than 10000 documents")));
		}		
		ReturnMsg rtn = initPrtdoc();
		if(rtn != null && !rtn.getStatus()) return(rtn);
		return(ReturnMsg.defaultOk);		
	}

	protected boolean skipPrint() {
		return(false);
	}
	
	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			if(!skipFetch) {
				boolean ok = p_result.fetchOneRecV(p_recIdx);
				if(!ok) return(new ReturnMsg(false,sh.getLabel("Fetch Record failed")));
			}
			br = p_result;
			if(!skipPrint()) print();
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			//return(new ReturnMsg(false,"Print Invoice" + result.getCellString("inv_invno") + " Failed " ));
			return(new ReturnMsg(false,String.format(sh.getLabel("Print Document %d Failed"), p_recIdx)));
		}
	}

	@Override
	public ReturnMsg afterAction(BiResult br) {
//		ByteArrayInputStream bis = null;
//		ZkUtil.printFromStream(bis, "application/pdf", getSessionHelper());
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ReturnMsg rtn = getPrintDocJson().toPdfStream(os, sh);	
			//biBase.showProgressPanel(false, true, "", 0);
			if(rtn != null && !rtn.getStatus()) {
				Messagebox.show(rtn.getMsg());
			} else {
				ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
				if(batchDownloadReport) {
    			    Filedownload.save(is, "application/pdf", "PaymentNotice"+ ".pdf");
				} else {
					ZkUtil.printFromStream(is, "application/pdf", sh);
				}
			}
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,sh.getLabel("End Print Invoice Failed")));
		}
	}

	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		UniLog.log1("afterActionAsync start");
		biBase.showProgressPanel(false, null);
		biBase.setProgressPanelProgress(String.format("Write document page: %d/%d", 0, docCnt), 0);
        Map<String, Object> m = new HashMap<String, Object>();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

        Timer timer = new Timer();
   		timer.setPage(ZkUtil.getMainCompPage());
   		timer.setDelay(1000);
   		timer.setRepeats(true);
   		timer.setRunning(true);
   		timer.addEventListener(Events.ON_TIMER, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("timer:%s", event);
				if (m.containsKey("isCompleted")) {
					UniLog.log1("afterActionAsync isCompleted");
					timer.stop();
		    		timer.detach();
					ReturnMsg rtn = (ReturnMsg)m.get("resultMsg");
					biBase.hideProgressPanel();
		    		if(rtn == null || rtn.getStatus()) {
				   		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
				   		if(batchDownloadReport) {
    			       		Filedownload.save(is, "application/pdf", "PaymentNotice"+ ".pdf");
				   		} else {
					   		ZkUtil.printFromStream(is, "application/pdf", sh);
				   		}
			   		}
		    		cb.callback(rtn);
				} else if (m.containsKey("progressValue")) {
					String progressLabel = (String)m.get("progressLabel");
					int progressValue = (Integer)m.get("progressValue");
					biBase.setProgressPanelProgress(progressLabel, progressValue);
				}
			}
   		});

		PrtdocJson ppj = getPrintDocJson();
		if (ppj instanceof PrtdocPerfJson) {
			((PrtdocPerfJson)ppj).setNotifyCallback(new PrtdocJsonChnftrRpcServlet.Callback() {
				@Override
				public void notify(int docNum) {
					UniLog.log1("setNotifyCallback notify:%d", docNum);
					int progress = docCnt > 0 ? (docNum + 1) * 100 / docCnt : 0;
					m.put("progressLabel", String.format("Write document page: %d/%d", docNum + 1, docCnt));
					m.put("progressValue", progress);
				}
			});
		}

		CompletableFuture.runAsync(() -> {
			ReturnMsg rtn = null;
			try {
				rtn = ppj.toPdfStream(os, sh);
			} catch (Exception ex) {
				UniLog.log(ex);
				rtn = new ReturnMsg(false, sh.getLabel("End Print Invoice Failed"));
			}
			m.put("isCompleted", true);
			m.put("resultMsg", rtn);
		});
	}

	@Override
	public void actionPerformed(JxField field) {
		try {
			JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
			br = jxf.getBr();
			sh = br.getSessionHelper();
			ReturnMsg rtn = initPrtdoc();
			if(rtn != null && !rtn.getStatus()) {
				field.getJxForm().messageBox(rtn.getMsg());
				return;
			}
			print();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			rtn = getPrintDocJson().toPdfStream(os, sh);	
			if(rtn != null && !rtn.getStatus()) {
				field.getJxForm().messageBox(rtn.getMsg());
			} else {
				String ss = getDocumentName(br);
				ZkUtil.showPdfDialog((Component) field.getJxForm().getNativeComponent(), sh, os.toByteArray(), ss);
			}
		} catch (Exception ex) {
			UniLog.log(ex); 
			Messagebox.show(ex.toString());
		}	
	}

	@Override
	public PrtdocJson getPrintDocJson() {
		return(ppj);
	}

}
