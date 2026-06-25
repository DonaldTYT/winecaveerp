package com.uniinformation.dynamic.aw;

import java.util.HashMap;
import java.util.HashSet;

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

public class ConfirmOrder extends BiActionHandler implements JxActionListener {
	HashSet<Integer> cfmSet;
	public ConfirmOrder() {
		super(null);
	}
	public ConfirmOrder(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		cfmSet = new HashSet<Integer>();
		return (ReturnMsg.defaultOk);
	}
	
	ReturnMsg confirmOneQuotation(BiResult p_result, BiResult quoBr,int invrg) {
		quoBr.addCustomCondition("inv_rg = "+invrg);
		quoBr.query();
		if(quoBr.getRecordCount() == 1) {
			try {
				quoBr.fetchOneRecV(0);
				String ss = quoBr.getCellString("inv_quostatus");
				if(ss.equals("Confirmed") || ss.equals("Void")) {
					return(ReturnMsg.defaultOk);
				}
				quoBr.getCell("inv_quostatus").set("Confirmed");
				ReturnMsg rtn = quoBr.updateCurrent();
				return(rtn);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
		} else {
			return(new ReturnMsg(false,"Quotation Not Found"));
		}
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		BiResult quoBr;
		int invrg = p_result.getCellInt("inv_rg");
		if(invrg < 300000) {
			return(new ReturnMsg(false,"Cannont confirmed old quotation"));
		}
		cfmSet.add(invrg);
		return (ReturnMsg.defaultOk);
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		if(!cfmSet.isEmpty()) {
			BiResult quoBr = p_br.getSessionHelper().getBiSchema().getViewByName("erpv4.QuotationG2").newBiResult(p_br.getSessionHelper().getLoginId(), null, null, p_br.getSessionHelper());
			for(int invrg : cfmSet) {
				ReturnMsg rtn = confirmOneQuotation(p_br, quoBr,invrg);
				if(rtn != null && !rtn.getStatus()) {
					return(rtn);
				}
			}
			biBase.biBaseRefresh(p_br);
		}
		return (ReturnMsg.defaultOk);
	}
	@Override
	public void actionPerformed(JxField field) {
		
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult br = jxf.getBr();
		int invrg = br.getCellInt("inv_rg");
		if(invrg < 300000) {
			field.getJxForm().messageBox("Cannont confirmed old quotation");
			return;
		}
		Messagebox.show("Confirm Order ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			new EventListener() {
			   public void onEvent(Event evt) throws Exception {
			    	if (((Integer)evt.getData()) == Messagebox.YES){
			    		BiResult quoBr;
			    		quoBr = br.getSessionHelper().getBiSchema().getViewByName("erpv4.QuotationG2").newBiResult(br.getSessionHelper().getLoginId(), null, null, br.getSessionHelper());
			    		ReturnMsg rtn = confirmOneQuotation(br, quoBr,invrg);
			    		if(rtn != null && !rtn.getStatus()) {
			    			field.getJxForm().messageBox("Error while confirm quotation " + rtn.getMsg());
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
		return(p_br.getSessionHelper().hasAccessRight("#cfmwo"));
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			String qs = p_br.getCellString("inv_quostatus");
			if(qs.equals("Confirmed") || qs.equals("Void")) return(true);
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
}
