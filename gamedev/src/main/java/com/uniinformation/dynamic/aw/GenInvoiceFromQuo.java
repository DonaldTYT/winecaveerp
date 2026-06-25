package com.uniinformation.dynamic.aw;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class GenInvoiceFromQuo  extends BiActionHandler implements JxActionListener {

	public GenInvoiceFromQuo() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public GenInvoiceFromQuo(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	
	ReturnMsg generateOneInvoice(BiResult quoBr,BiResult invBr) {
		try {
			ReturnMsg rtn = null;
			invBr.clearCurrentRec();
			invBr.getCell("invh_cocode").set(quoBr.getCell("inv_cocode").getString());
			invBr.getCell("invh_quonum").set(quoBr.getCell("inv_invno").getString());
			invBr.getCell("invh_vcode").set(quoBr.getCell("inv_vcode").getString());
			invBr.getCell("invh_revisonno").set(quoBr.getCell("inv_revisonno").getInt());
//			invBr.getCell("invh_jobno").set(quoBr.getCell("inv_jobno").getString());
			invBr.getCell("invh_projecttitle").set(quoBr.getCell("inv_projecttitle").getString());
			invBr.getCell("invh_email").set(quoBr.getCell("inv_email").getString());
			invBr.getCell("invh_quodeli").set(quoBr.getCell("inv_quodeli").getString());
			invBr.getCell("invh_discount").set(quoBr.getCell("inv_discount").getDouble());
			invBr.getCell("invh_tax").set(quoBr.getCell("inv_tax").getDouble());
			invBr.getCell("invh_quorg").set(quoBr.getCell("inv_rg").getInt());
			invBr.getCell("invh_smcode").set(quoBr.getCell("inv_smcode").getString());
			invBr.getCell("invh_remark").set(quoBr.getCell("inv_remark").getString());
			invBr.getCell("invh_contact").set(quoBr.getCell("inv_contact").getString());
			invBr.getCell("invh_addr0").set(quoBr.getCell("inv_addr0").getString());
			invBr.getCell("invh_addr1").set(quoBr.getCell("inv_addr1").getString());
			invBr.getCell("invh_addr2").set(quoBr.getCell("inv_addr2").getString());
			invBr.getCell("invh_addr3").set(quoBr.getCell("inv_addr3").getString());
			invBr.getCell("invh_tel").set(quoBr.getCell("inv_tel").getString());
			invBr.getCell("invh_fax").set(quoBr.getCell("inv_fax").getString());
			invBr.getCell("invh_pocode").set(quoBr.getCell("inv_pocode").getString());
			invBr.getCell("invh_cid").set(quoBr.getCell("inv_cid").getString());
			invBr.getCell("invh_xrate").set(quoBr.getCell("inv_xrate").getDouble());
//			invBr.getCell("invh_wcocode").set(quoBr.getCell("inv_wcocode").getString());
			
			BiResult indBr = invBr.getSubLink("erpv4.InvDetG2");
			for(BiCellCollection bc : quoBr.getSubLink(((BiResultQuotation) quoBr).get_subLinkId()).getRowCollectionList()) {
				BiCellCollection invBc = indBr.newRowCollection();
				rtn = indBr.addSubRecord(invBc, -1,"");
				invBc.getCell("invd_cocode").set(bc.getCell("ind_cocode").getString());
				// invBc.getCell("invd_rg").set(bc.getCell("ind_rg").getInt());
				invBc.getCell("invd_seq").set(bc.getCell("ind_seq").getInt());
				invBc.getCell("invd_desc").set(bc.getCell("ind_desc").getString());
				invBc.getCell("invd_qty").set(bc.getCell("ind_qty").getString());
				invBc.getCell("invd_unit").set(bc.getCell("ind_unit").getString());
				invBc.getCell("invd_uprice").set(bc.getCell("ind_uprice").getDouble());
				invBc.getCell("invd_cid").set(bc.getCell("ind_cid").getString());
				invBc.getCell("invd_xrate").set(bc.getCell("ind_xrate").getDouble());
				invBc.getCell("invd_amount").set(bc.getCell("ind_amount").getDouble());
				invBc.getCell("invd_lamount").set(bc.getCell("ind_lamount").getDouble());
				invBc.getCell("invd_irg").set(bc.getCell("ind_irg").getInt());
				invBc.getCell("invd_pdsrg").set(bc.getCell("ind_pdsrg").getInt());
				invBc.getCell("invd_postano").set(bc.getCell("ind_postano").getString());
//				invBc.getCell("invd_jnsubtype").set(bc.getCell("ind_jnsubtype").getString());
//				invBc.getCell("invd_jnsubcode").set(bc.getCell("ind_jnsubcode").getString());
				invBc.getCell("invd_show").set(bc.getCell("ind_show").getString());
				invBc.getCell("invd_subitem").set(bc.getCell("ind_subitem").getString());
			}
			rtn = invBr.addCurrent();
			return(rtn);
			//for(BiCellCollection bc : quoBr.getSubLink( ((BiResultQuotation) quoBr)).
				
			//}
			
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}

	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResultQuotation br = (BiResultQuotation) jxf.getBr();
		int invrg = br.getCellInt("inv_rg");
		if(invrg < 300000) {
			field.getJxForm().messageBox("Cannont generate invoice for old quotation");
			return;
		}	
		
		Messagebox.show("Generate Invoice ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
				new EventListener() {
				   public void onEvent(Event evt) throws Exception {
				    	if (((Integer)evt.getData()) == Messagebox.YES){
				    		BiResult invG2Br;
				    		invG2Br = br.getSessionHelper().getBiSchema().getViewByName("erpv4.InvoiceG2").newBiResult(br.getSessionHelper().getLoginId(), null, null, br.getSessionHelper());
				    		ReturnMsg rtn = generateOneInvoice(br, invG2Br);
				    		
				    		if(rtn != null && !rtn.getStatus()) {
				    			field.getJxForm().messageBox("Error while generate invoice " + rtn.getMsg());
				    		} else {
				    			BiResult quoInvoiceSr = br.getSubLink(br.get_invoiceLinkId());
				    			br.fetchOneSubLink(br.getCurrentCollection(),quoInvoiceSr,null) ;
				    			JxField sv = jxf.jxAdd("list_"+jxf.replaceViewName(br.get_invoiceLinkId()));
				    			((JxZkBiBase) jxf).bindSublinkList(sv, quoInvoiceSr);
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
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
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
			if(!p_br.getSessionHelper().hasAccessRight("#addinv")) {
				return(true);
			}
			String qs = p_br.getCellString("inv_quostatus");
			if(!qs.equals("Confirmed") ) return(true);
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
	
	public ReturnMsg isRunnable(BiResult br,boolean isBatch) {
		return(ReturnMsg.defaultOk);
	}
}
