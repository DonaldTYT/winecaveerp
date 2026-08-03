package com.uniinformation.dynamic.winecave;

import java.util.HashMap;
import java.util.HashSet;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.wc.BiResultStockOutChg;
import com.uniinformation.bicore.wc.BiResultStorageChg;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class GenerateStockOutDet extends BiActionHandler implements JxActionListener {
	public GenerateStockOutDet() {
		super(null);
	}
	public GenerateStockOutDet(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	@Override
	public void actionPerformed(JxField field) {
		
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResultStockOutChg br = (BiResultStockOutChg) jxf.getBr();
		Messagebox.show("Confirm Generate Storate Detail ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			new EventListener() {
			   public void onEvent(Event evt) throws Exception {
			    	if (((Integer)evt.getData()) == Messagebox.YES){
			    		br.regen_stockoutdet();
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
		return(p_br.getSessionHelper().hasAccessRight("#storage"));
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(true);
		} else {
			return(false);
		}
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		// TODO Auto-generated method stub
		return null;
	}
}
