package com.uniinformation.zkbi.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.afs.BiResultAfsQuotationG2;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerQuotationG2 extends ZkBiComposerBase {
	@Override
    protected void setupExtraButton(final BiResult result) {
    	super.setupExtraButton(result);
		Button batchprtinv = addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbBatchPrintInvoice",sessionHelper.getBtLabel("Print Invoice"),"fa-print",
			new BiActionHandler(this) {
				PrtdocClass jpi = null;
				@Override
				public ReturnMsg beforeAction(BiResult p_result,int cnt) { 
					if(cnt > 50) {
						return(new ReturnMsg(false,sessionHelper.getLabel("Cannot Print more than 50 orders")));
					}
					try {
						String prtdocClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClassG2");
						if(prtdocClass == null) {
							prtdocClass = "com.uniinformation.dynamic.chungkee.ChungkeePrintInvoice";
						}
						Class[]	paramTypes = new Class[]{BiResultQuotation.class};
						jpi = (PrtdocClass) DynamicClassLoader.newInstance(prtdocClass, paramTypes,result);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,sessionHelper.getLabel("Initialized Print Job Failed")));
					}
					return(ReturnMsg.defaultOk);
				}

				@Override
				public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							boolean ok = result.fetchOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,sessionHelper.getLabel("Fetch Record failed")));
							jpi.print();
							return(ReturnMsg.defaultOk);
						} catch (Exception ex) {
							UniLog.log(ex);
							//return(new ReturnMsg(false,"Print Invoice" + result.getCellString("inv_invno") + " Failed " ));
							return(new ReturnMsg(false,String.format(sessionHelper.getLabel("Print Invoice %s Failed"), result.getCellString("inv_invno"))));
						}
				}

				@Override
				public ReturnMsg afterAction(BiResult p_br) {
//					ByteArrayInputStream bis = null;
//					ZkUtil.printFromStream(bis, "application/pdf", getSessionHelper());
					try {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
						if(rtn != null && !rtn.getStatus()) {
							Messagebox.show(rtn.getMsg());
						} else {
							String ss = result.getCellString("inv_invno");
							ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
							ZkUtil.printFromStream(is, "application/pdf", result.getSessionHelper());
						}
						return(ReturnMsg.defaultOk);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,sessionHelper.getLabel("End Print Invoice Failed")));
					}
				}
			}
		);
//		batchprtinv.setDisabled(true);
		if(((BiResultQuotationG2) result).getQuomode() == BiResultQuotation.QUOMODE.QUOTATION) {
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbBatchCreateOrder",sessionHelper.getBtLabel("Create Order"),"fa-user",
			new BiActionHandler(this) {
				@Override
				public ReturnMsg beforeAction(BiResult p_result,int cnt) { 
					return(ReturnMsg.defaultOk);
				}

				@Override
				public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							UniLog.log1("Create Order %d", p_recIdx);
							boolean ok = result.fetchOneRecV(p_recIdx);
							return(ReturnMsg.defaultOk);
						} catch (Exception ex) {
							UniLog.log(ex);
							//return(new ReturnMsg(false,"Create Order " + result.getCellString("inv_quonum") + " Failed " ));
							return(new ReturnMsg(false,String.format(sessionHelper.getLabel("Create Order %s Failed"), result.getCellString("inv_quonum"))));
						}
				}

				@Override
				public ReturnMsg afterAction(BiResult p_br) {
					try {
						return(ReturnMsg.defaultOk);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,sessionHelper.getLabel("Create Order Failed")));
					}
				}
			}
		);
			
		}
	}
   	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
   		super.doAfterCompose(comp);
   		String QuoMode = getURLParam("QuoMode");
   		if("Quotation".equals(QuoMode)) {
   			
   		}
   		
   	}
   	
	@Override
	protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc) {
		BiResult result = super.getQueryResult(sessionHelper, p_viewid, p_sortIdx, p_sortDesc);
   		String QuoMode = getURLParam("QuoMode");
   		if("Quotation".equals(QuoMode)) {
   			((BiResultQuotationG2) result).setQuomode(BiResultQuotationG2.QUOMODE.QUOTATION);
   		}		
   		if("Order".equals(QuoMode)) {
   			((BiResultQuotationG2) result).setQuomode(BiResultQuotationG2.QUOMODE.ORDER);
   		}		
		return(result);
	}	
   	
}
