package com.uniinformation.jxapp.aw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.chungkee.BiResultQuotation;
import com.uniinformation.bicore.erpv4.BiResultDO;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class QuotationG2 extends com.uniinformation.jxapp.erpv4.QuotationG2 {
	
	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		//handle Download button
		return ListUtil.of(
			new ZkBiGetItemProperty(p_br.getSubLink("erpv4.QuoInvoiceG2"),this) {
				@Override
				public void onValueChanged(Object p_value,int p_ctype) {
					ColumnCell bcc = (ColumnCell) p_value;
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("invh_voidinv")){
						UniLog.log1("%s clicked", bcc.getCellLabel());
						Messagebox.show("Delete Invoice ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
								new EventListener() {
								   public void onEvent(Event evt) throws Exception {
								    	if (((Integer)evt.getData()) == Messagebox.YES){
								    		
						try {
							CellCollection col = bcc.getCollection();
							String invno = col.getCellString("invh_invno");
							BiResult sobr = p_br.getView().getSchema().getViewByName("erpv4.InvoiceG2").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
							sobr.addCustomCondition("invh_invno = '"+invno+"'");
							sobr.query();
							if(sobr.getRowCount() == 1) {
								sobr.fetchOneRecV(0);
								ReturnMsg rtn = sobr.deleteCurrent();
								if(rtn != null && !rtn.getStatus()) {
									messageBox(rtn.getMsg());
								} else {
					    			BiResult quoInvoiceSr = p_br.getSubLink(((BiResultQuotationG2) p_br).get_invoiceLinkId());
					    			p_br.fetchOneSubLink(p_br.getCurrentCollection(),quoInvoiceSr,null) ;
					    			JxField sv = jxAdd("list_"+replaceViewName(((BiResultQuotationG2) p_br).get_invoiceLinkId()));
					    			bindSublinkList(sv, quoInvoiceSr);
					    			refreshAllListitem();
								}
							}
						} catch (Exception ex) {
							UniLog.log(ex);
							messageBox(ex.toString());
						}
								    		
								    	} else{
								    		return;
								    	}
								   }
								}
							);						
						
//						messageBox("Void invoice Not Implemented");
						
						
						
					}
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("invh_print")){
						UniLog.log1("%s clicked", bcc.getCellLabel());
						try {
							CellCollection col = bcc.getCollection();
							String invno = col.getCellString("invh_invno");
							BiResult sobr = p_br.getView().getSchema().getViewByName("erpv4.InvoiceG2").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
							sobr.addCustomCondition("invh_invno = '"+invno+"'");
							sobr.query();
							if(sobr.getRowCount() == 1) {
								sobr.fetchOneRecV(0);
								
								RpcClient rpc = getRpcClient();
								ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
								rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
								Value val = rpc.callSegment("printer_autoselect",
											new VectorUtil()
											.addElement(1)
											.toVector()
										);
								//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
								rpc.callSegment("setCocodeBaseccy",
										new VectorUtil()
										.addElement( sobr.getCellString("invh_cocode"))
										.addElement( Erpv4Config.getBaseCcy(getSessionHelper(),sobr.getCellString("invh_cocode")))
										.toVector()
										);
								val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getBr().getSessionHelper().getWebContentRealPath("images", true)) .toVector());
								val = rpc.callSegment("artway_print_invoice",
											new VectorUtil()
											.addElement(sobr.getCellInt("invh_rg"))
											.addElement("CHNPRINT")
											.addElement("VARIABLE")
											.addElement("A4P")
											.addElement("NORMAL")
											.addElement("LPTRAW")
											.toVector()
										);
								rpc.close();
								if(val != null && val.toString().startsWith("OK")) {
									String fname = val.toString().substring(4);
									UniLog.log("Print wo got " + fname);
									try {
										InputStream is = erpFileInputStream(fname);
										ChnftrParser ps = new ChnftrParser(is,""); // print as A3 , always two pages
//										ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
										ByteArrayOutputStream bos = new ByteArrayOutputStream();
										ps.print(bos);
										ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
										SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
										ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
									} catch (Exception ex) {
										UniLog.log(ex);
									}
								}	
								
								/*
								String prtdocClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClassG2");
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								PrtdocClass jpi = null;
								Class[]	paramTypes = new Class[]{BiResultQuotation.class};
								jpi = (PrtdocClass) DynamicClassLoader.newInstance(prtdocClass, paramTypes,sobr);
								jpi.print();
								ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
								if(rtn != null && !rtn.getStatus()) {
									Messagebox.show(rtn.getMsg());
								} else {
									String ss = getBr().getCellString("invh_invno");
									ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
									ZkUtil.printFromStream(is, "application/pdf", getBr().getSessionHelper());
								}
								*/
							}
						} catch (Exception ex) {
							UniLog.log(ex);
							messageBox(ex.toString());
						}
					}
				}
			}
		);	
		
	}
	
}
