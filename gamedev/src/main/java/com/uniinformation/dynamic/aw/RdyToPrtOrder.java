package com.uniinformation.dynamic.aw;

import java.util.HashMap;
import java.util.HashSet;

import org.zkoss.zhtml.Br;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class RdyToPrtOrder extends BiActionHandler implements JxActionListener {
	String actionName = "Mark Ready To Print";
	public RdyToPrtOrder() {
		super(null);
	}
	public RdyToPrtOrder(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		if(cnt > 100) {
			return(new ReturnMsg(false,"Cannot update more than 100 job at the same time"));
		}
		try {
			p_result.beginWork();
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return (ReturnMsg.defaultOk);
	}
	
	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			p_result.loadOneRecV(p_recIdx);
			p_result.fetchOneRecV(p_recIdx);
			p_result.getCell("jm_rdytoprt").set(true);
			return(p_result.updateCurrent());
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		try {
			p_br.commitWork();
			biBase.biBaseRefresh(p_br);
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return (ReturnMsg.defaultOk);
	}
	@Override
	public void actionPerformed(JxField field) {
		
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult br = jxf.getBr();
		Messagebox.show("Confirm " + actionName + " ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			new EventListener() {
			   public void onEvent(Event evt) throws Exception {
			    	if (((Integer)evt.getData()) == Messagebox.YES){
			    		br.getCell("jm_rdytoprt").set(true);
			    		ReturnMsg rtn = br.updateCurrent();
			    		if(rtn != null && !rtn.getStatus()) {
			    			field.getJxForm().messageBox("Error while " + actionName + " : " + rtn.getMsg());
			    		} else {
			    			((JxZkBiBase) jxf).refreshAllListitem();
			    		}
			    	} else{
			    		return;
			    	}
			   }
			}
		);
	}

	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(p_br.getSessionHelper().hasAccessRight("#wosetrdyprt"));
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			String qs = p_br.getCellString("inv_quostatus");
			if(!qs.equals("Confirmed")) return(true);
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
}
