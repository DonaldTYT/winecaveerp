package com.uniinformation.dynamic.aw;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ReviseQuotation  extends BiActionHandler implements JxActionListener {

	public ReviseQuotation() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public ReviseQuotation(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResultQuotation br = (BiResultQuotation) jxf.getBr();
		int invrg = br.getCellInt("inv_rg");
		if(invrg < 300000) {
			field.getJxForm().messageBox("Cannont revise old quotation");
			return;
		}	
		
		Messagebox.show("Revise Quotation ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
				new EventListener() {
				   public void onEvent(Event evt) throws Exception {
				    	if (((Integer)evt.getData()) == Messagebox.YES){
				    		ReturnMsg rtn = br.lockRecordForUpdate();
				    		if(rtn != null && !rtn.getStatus()) {
				    			jxf.messageBox(rtn.getMsg());
				    			return;
				    		}
				    		RpcClient rpc = br.getSelectUtil().getRpcClient();
							rpc.callSegment("setCocodeBaseccy",
									new VectorUtil()
									.addElement( br.getCellString("inv_cocode"))
									.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),br.getCellString("inv_cocode")))
									.toVector()
									);
							Value val = rpc.callSegment("real_revise_quotationG2",
									new VectorUtil()
									.addElement(br.getCell("inv_invrg").getInt())
									.toVector()
									);
							if(val == null || val.toString().startsWith("OK")) {
				    			jxf.messageBox("Revise Quo Failed : " + val == null ? "" : val.toString());
				    			br.rollbackWork();
				    			return;
							}
							int rev = br.getCellInt("inv_revisonno");
							rev++;
							br.getCell("inv_revisonno").set(rev);
							
				    	} else{
				    		return;
				    	}
				   }
				}
			);
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
	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(true);
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			if(!p_br.getSessionHelper().hasAccessRight("#revquo")) {
				return(true);
			}
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
	
	public ReturnMsg isRunnable(BiResult br,boolean isBatch) {
		return(ReturnMsg.defaultOk);
	}
}
