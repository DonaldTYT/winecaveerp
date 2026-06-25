package com.uniinformation.jxapp.hw;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.hw.BiResultHwQuotation;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.bicore.hw.BiResultHwInvoice;
import com.uniinformation.bicore.hw.BiResultHwQuoDet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.hw.HwOrderBase.quoDetGetItemProperty;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkf.ZkForm;

public class HwQuotation extends HwOrderBase {
	String invoiceUrl = "zkbiloader.html?action=browse&viewid=hw.HwInvoice&page_id=HwInvoice_01&zul=zkbiloader.zul&composer=hw.ZkBiComposerHwInvoice";
	@Override
	void setupActionButton(boolean is_dirty)
	{
		super.setupActionButton(is_dirty);
		jxAdd("btConfirmOdr").setEnable(false);
		jxAdd("btUnConfirmOdr").setEnable(false);
		jxAdd("btCopyOdr").setEnable(false);
		jxAdd("btSavePrint").setEnable(false);
		jxAdd("btVoidOdr").setEnable(false);
		jxAdd("btPrintWO").setEnable(false);
		jxSetEnable("btPrintQuo",false);
		jxSetEnable("btCopyWo",false);
		jxSetEnable("btPrintDN",false);
		jxSetEnable("btGenInvoice",false);
		jxSetEnable("btSetAssignTo",false);
		if (getBr().allowUpdate()) {
			if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
				jxAdd("btUnConfirmOdr").setEnable(true);
			} else {
				jxAdd("btConfirmOdr").setEnable(true);
			}
			if(curMode == JxZkBiBase.MODE_ADD) {
				jxAdd("btSavePrint").setEnable(true);
				jxAdd("btConfirmOdr").setEnable(true);
			}
			if(is_dirty) {
				jxAdd("btSavePrint").setEnable(true);
			}
		}
		if(curMode == JxZkBiBase.MODE_UPDATE) { 
			if(!is_dirty) {
				if(sessionHelper.getAccessRights().contains("userg1")) {
					jxAdd("btPrintWO").setEnable(true);
					jxSetEnable("btPrintQuo",true);
					jxSetEnable("btCopyWo",true);
					jxAdd("btVoidOdr").setEnable(true);
					jxAdd("btCopyOdr").setEnable(true);
					if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
						jxSetEnable("btGenInvoice",true);
					}
				}
				if(sessionHelper.getAccessRights().contains("prtwo")) {
					jxSetEnable("btPrintWO",true);
				}
				if(sessionHelper.getAccessRights().contains("setassignt")) {
					jxSetEnable("btSetAssignTo",true);
				}
				
				jxSetEnable("btPrintDN",true);
			}
		}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldChange("inv_roundmode") {
			public boolean valueChanged(JxField fd, String p_value) {
				if (!checkBr()) return true;
				try {
					BiResult sr = getBr().getSubLink(detViewName);
					for(int i=0;i<sr.getRowCount();i++) {
						CellCollection sc = sr.getRowCollectionV(i);
						sc.getCell("ind_roundmode").set(getBr().getCellInt("inv_roundmode"));
					}
				} catch(CellException cex) {
					UniLog.log(cex);
				}
				return(true);
			}
		};	
		new JxFieldAction("btPrintDN") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			UniLog.log("print D/N Pressed");
			RpcClient rpc = getRpcClient();
			ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
			rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
//			rpcservlet.setConnection(rpc.getConnection());
			Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
					);
			//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
			val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			

			val = rpc.callSegment("erpv4_print_dn",
						new VectorUtil()
						.addElement(getBr().getCell("inv_rg").getInt())
						.addElement("USEFILEING")
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
					ChnftrParser ps = new ChnftrParser(is,"'");
					ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
						@Override
						public byte[] getImage(String p_key) {
							//TODO obtain image file from filing
							if (!StringUtils.startsWith(p_key, "jxHwQuoDetFiling_")){
								UniLog.logm(this, "invalid getImage key %s", p_key);
								return(null);
							}
							try{
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								FilingUtil.getFile(sessionHelper.getAgent(), null, p_key, bos);
								byte[] bytes = bos.toByteArray();
								bos.close();
								return(bytes);
							}
							catch(Exception ex){
								ex.printStackTrace();
								return(null);
							}
						}});
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ps.print(bos);
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
					ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
					
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
	};	
	new JxFieldAction("btPrintWO") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			UniLog.log("print quo Pressed");
			RpcClient rpc = getRpcClient();
			ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
			rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
//			rpcservlet.setConnection(rpc.getConnection());
			Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
					);
			//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
			val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			
			val = rpc.callSegment("erpv4_print_wo",
						new VectorUtil()
						.addElement(getBr().getCell("inv_rg").getInt())
						.addElement("USEFILEING")
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
					ChnftrParser ps = new ChnftrParser(is,"'");
					ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
						@Override
						public byte[] getImage(String p_key) {
							//TODO obtain image file from filing
							if (!StringUtils.startsWith(p_key, "jxHwQuoDetFiling_")){
								UniLog.logm(this, "invalid getImage key %s", p_key);
								return(null);
							}
							try{
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								FilingUtil.getFile(sessionHelper.getAgent(), null, p_key, bos);
								byte[] bytes = bos.toByteArray();
								bos.close();
								return(bytes);
							}
							catch(Exception ex){
								ex.printStackTrace();
								return(null);
							}
						}});
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ps.print(bos);
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
//					ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
					ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Project-"+getBr().getCell("inv_invno").getString());
					
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
	};	
	new JxFieldAction("btSavePrint") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			ReturnMsg rtn = null;
			if(curMode == JxZkBiBase.MODE_UPDATE) { 
				rtn = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
			} else if(curMode == JxZkBiBase.MODE_ADD) { 
				rtn = processAdd(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
			} else return;
			Button btn = (Button) jxAdd("btPrintWO").getNativeObject();
			Events.echoEvent(Events.ON_CLICK, btn, null);
		}
	};
	new JxFieldAction("btPrintQuo") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ReturnMsg rtn = ((BiResultHwQuotation) getBr()).printQuotation(bos);
            if(rtn.getStatus()) {
				ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Quotation-"+getBr().getCell("inv_invno").getString());
            } else {
				Messagebox.show(rtn.getMsg());
            }
		}
	};
	new JxFieldAction("btGenInvoiceYY") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			confirm("Do you want to create invoice for this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
						}
					}
				);	
			
				
		}
	};
	new JxFieldAction("btGenInvoice") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			BiResult sr = getBr().getSubLink( detViewName );
			boolean hasUnInvoicedItem = false;
			Vector<BiCellCollection> v = sr.getRowCollectionList();
			for(BiCellCollection col : v) {
				if(col.getCell("ind_linked").getInt() <= 0) {
					hasUnInvoicedItem = true;
					break;
				}
			}
			if(!hasUnInvoicedItem) {
				messageBox("This Project's has all been invoiced, no need to generate");
				return;
			}
			final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/geninvoice.zul"); // ? should this be put in global so taht it only be intantiated once ?
			final CellCollection col = new CellCollection();
				final Listbox lb =  (Listbox) zkf1.getComponent("invoiceList");
				TableRec tr = null;
				if(lb != null) {
					for(int i=lb.getItemCount()-1;i>=0;i--) {
						lb.removeItemAt(i);
					}
					try {
						tr = getBr().getSelectUtil().getQueryResult("select * from invoice where inv_quostatus not in('Confirmed') and inv_vcode = '" + getBr().getCellString("inv_vcode") + "' order by inv_rg");
						for(int i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							Listitem li = new Listitem();
							li.appendChild(new Listcell(tr.getFieldString("inv_date")));
							li.appendChild(new Listcell(tr.getFieldString("inv_invno")));
							li.appendChild(new Listcell(tr.getFieldString("inv_cuser")));
							li.appendChild(new Listcell(tr.getFieldString("inv_quostatus")));
							li.appendChild(new Listcell(tr.getFieldString("inv_quonum")));
							lb.appendChild(li);
						}
					} catch (Exception ex) {
						UniLog.log(ex);
						ZkUtil.errMsg("Unknown Error 13111");
					}
					
				} 
				final TableRec ftr = tr;
				try {
				zkf1.doModal(col,new ZkBiEventListener(2000) {
					@Override
					public void onZkBiEvent(Event arg0) throws Exception {
					// TODO Auto-generated method stub
						if(arg0.getTarget().getId().equals("btSelect")) {
							int cc = lb.getSelectedIndex();
							if( cc >= 0) {
							getBr().beginWork();
							ReturnMsg rtn = getBr().lockRecordForUpdate();
							if(rtn != null && rtn.getStatus() != true) {
								getBr().rollbackWork();
								messageBox("Record is being modified by other user, cannot generate invoice");
								return;
							}
							RpcClient rpc = getBr().getSelectUtil().getRpcClient();
							ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
							rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);

//							RpcClient rpc = getRpcClient();
							
							rpc.callSegment("setCocodeBaseccy",
									new VectorUtil()
//									.addElement(Erpv4Config.getCoCode(getBr().getSessionHelper()))
//									.addElement(Erpv4Config.getBaseCcy(getBr().getSessionHelper()))
									.addElement(getBr().getCellString("inv_cocode"))
									.addElement(Erpv4Config.getBaseCcy(getBr().getSessionHelper(),getBr().getCellString("inv_cocode")))
									.toVector()
									);
							
							
//							java.util.Date d = DateUtil.today();
							java.util.Date d = getBr().getCell("inv_delidate").getDate();
							if(!d.after(DateUtil.zeroDate)) d = DateUtil.today();
							ftr.setRecPointer(cc);
//							String s = ((BiResultHwQuotation) br).getNewOrderNumber(d);
							String is = ftr.getFieldString("inv_invno");
							Value val = rpc.callSegment("HWcreateInvoiceFromQuo",
										new VectorUtil()
										.addElement(is)
										.addElement(d)
										.addElement(getBr().getCell("inv_rg").getInt())
										.toVector()
									);
							if(val != null && val.toString().startsWith("OK")) {
								getBr().commitWork();
								final int new_invrg = Integer.parseInt(val.toString().substring(4, 14).trim());
								UniLog.log("Invoice merged:" + val.toString().substring(14));
								confirm("Invoice merged to:" + val.toString().substring(14) + " goto Invoice ? "
										, 
										new MessageBoxActionInterface() {
											public void onButtonClicked( Object p_obj) {
												int rtn = ((Integer) p_obj);
												if(rtn == 1) {
													
								try {					
        	    					Executions.getCurrent().sendRedirect(
        	    							invoiceUrl+"&overrideaction=update&querycondition="
        	    									+getBr().getSessionHelper().putOneTimeData(
        	    												new JSONObject().put("customCondition","inv_rg="+new_invrg)
        	    											)
        	    							);
        	    					
								} catch(JSONException jex)	 {
									UniLog.log(jex);
								}
													
												}
											}
										}
									);	
									
								
							} else {
								messageBox("Generate Invoice failed " + val == null ? "Reason Unknown" : val.toString());
							}
							
								
								zkf1.exitModal();
							} else {
								ZkUtil.errMsg("Please Select Invoice/Quotation");
							}
						}
						if(arg0.getTarget().getId().equals("btOK")) {
							getBr().beginWork();
							ReturnMsg rtn = getBr().lockRecordForUpdate();
							if(rtn != null && rtn.getStatus() != true) {
								getBr().rollbackWork();
								messageBox("Record is being modified by other user, cannot generate invoice");
								return;
							}
							RpcClient rpc = getBr().getSelectUtil().getRpcClient();
							rpc.callSegment("setCocodeBaseccy",
									new VectorUtil()
//									.addElement(Erpv4Config.getCoCode(getBr().getSessionHelper()))
//									.addElement(Erpv4Config.getBaseCcy(getBr().getSessionHelper()))
									.addElement(getBr().getCellString("inv_cocode"))
									.addElement(Erpv4Config.getBaseCcy(getBr().getSessionHelper(),getBr().getCellString("inv_cocode")))
									.toVector()
									);
//							java.util.Date d = DateUtil.today();
							java.util.Date d = getBr().getCell("inv_delidate").getDate();
							if(!d.after(DateUtil.zeroDate)) d = DateUtil.today();
							String is = "";
							Value val = rpc.callSegment("HWcreateInvoiceFromQuo",
										new VectorUtil()
										.addElement(is)
										.addElement(d)
										.addElement(getBr().getCell("inv_rg").getInt())
										.toVector()
									);
							if(val != null && val.toString().startsWith("OK")) {
								getBr().commitWork();
								final int new_invrg = Integer.parseInt(val.toString().substring(4, 14).trim());
								UniLog.log("Invoice created :" + val.toString().substring(14));
								
								confirm("Invoice genearated:" + val.toString().substring(14) + " goto Invoice ? "
										, 
										new MessageBoxActionInterface() {
											public void onButtonClicked( Object p_obj) {
												int rtn = ((Integer) p_obj);
												if(rtn == 1) {
													
								try {					
        	    					Executions.getCurrent().sendRedirect(
        	    							invoiceUrl+"&overrideaction=update&querycondition="
        	    									+getBr().getSessionHelper().putOneTimeData(
        	    												new JSONObject().put("customCondition","inv_rg="+new_invrg)
        	    											)
        	    							);
        	    					
								} catch(JSONException jex)	 {
									UniLog.log(jex);
								}
												}
											}
										}
									);	
								
								
								getBr().refetchCurrent();
								bindCellCollection(getBr(),curMode);
							} else {
								getBr().rollbackWork();
								messageBox("Create Invoice failed " + val == null ? "Reason Unknown" : val.toString());
							}
							
							zkf1.exitModal();
						}
						if(arg0.getTarget().getId().equals("btCancel")) {
							zkf1.exitModal();
						}
					}
				}
	        );
				} catch (CellException cex ){
					UniLog.log(cex);
				}
				
		}
	};
	new JxFieldAction("btCopyWo") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			confirm("Do you want to copy project detail to quotation (Current Quo Detail will be deleted) ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								getBr().beginWork();
								ReturnMsg rtn = getBr().lockRecordForUpdate();
								if(rtn != null && rtn.getStatus() != true) {
									getBr().rollbackWork();
									messageBox("Record is being modified by other user, cannot copy");
									return;
								}
								RpcClient rpc = getBr().getSelectUtil().getRpcClient();
								rpc.callSegment("setCocodeBaseccy",
									new VectorUtil()
//									.addElement(Erpv4Config.getCoCode(getBr().getSessionHelper()))
//									.addElement(Erpv4Config.getBaseCcy(getBr().getSessionHelper()))
									.addElement(getBr().getCellString("inv_cocode"))
									.addElement(Erpv4Config.getBaseCcy(getBr().getSessionHelper(),getBr().getCellString("inv_cocode")))
									.toVector()
									);
								Value val = rpc.callSegment("HWcreateQuodetFromWo",
										new VectorUtil()
										.addElement(getBr().getCellInt("inv_rg"))
										.toVector()
									);
								if(val != null && val.toString().startsWith("OK")) {
									getBr().commitWork();
									getBr().refetchCurrent();
									bindCellCollection(getBr(),curMode);
								} else {
									getBr().rollbackWork();
								}
								
							} catch (Exception ex) {
								getBr().rollbackWork();
								UniLog.log(ex);
							}

//							RpcClient rpc = getRpcClient();
							
						}
					}
				);	
			
				
		}
	};
	new JxFieldAction("btSetAssignTo") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			try {
	        			UniLog.log("HAHA btSetAssignTo");
	        			final ZkForm zkf1 = new ZkForm(null,"zkf/kanghong/set_assign_to.zul");
	        			final CellCollection col = new CellCollection();
	        			col.addCell("userid", new Cell("donald"));
	        			zkf1.doModal(col,new ZkBiEventListener() {
								@Override
								public void onZkBiEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
									if(arg0.getTarget().getId().equals("btOK")) {
										zkf1.exitModal();
										getBr().getCell("inv_assignto").set(col.getCell("tbsetassignto").getString());
										ReturnMsg rtn = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
										if(rtn != null && rtn.getStatus() != true) {
											messageBox("Record is being modified by other user, cannot update assign to");
										}
										/*
										getBr().beginWork();
										ReturnMsg rtn = getBr().lockRecordForUpdate();
										if(rtn != null && rtn.getStatus() != true) {
											getBr().rollbackWork();
											messageBox("Record is being modified by other user, cannot update assign to");
											return;
										}
										*/
									}
									if(arg0.getTarget().getId().equals("btCancel")) {
										zkf1.exitModal();
									}
								}
	        				}
	        			);
			
			} catch (CellException  cex ) {
				UniLog.log(cex);
			}
		}
	};
		setUpdateAndClose(JxZkBiBase.CloseAction.Reload);
		setAddAndClose(JxZkBiBase.CloseAction.Reload);
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		if(quoDetailAddListener==null) {
			BiResult sr = br.getSubLink(detViewName);
			quoDetailAddListener = genListboxAddActionListener(this, sr , jxAdd( "list_"+JxZkBiBase.replaceViewName(detViewName)),INS_IDX_ACTIONIDX);
//			jxAdd("btAddQuoDet_"+BiResultHwQuoDet.DELTALTYPE_STOCK_ITEM ).addActionListener(quoDetailAddListener);
			jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM) ).addActionListener(quoDetailAddListener);
			jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) ).addActionListener(quoDetailAddListener);
			jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION)).addActionListener(quoDetailAddListener);
			jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM)).addActionListener(quoDetailAddListener);
			jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK)).addActionListener(quoDetailAddListener);
		}
		jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM)).setVisible(true);
		jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM)).setVisible(true);
		
//		jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM) ).setVisible(true);
		
		jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION)).setVisible(true);
		jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM)).setVisible(true);
		jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK)).setVisible(true);
		super.bindCellCollection(br, mode);
		
		jxAdd("list_"+JxZkBiBase.replaceViewName(detViewName)
				).setAttribute("mode","canInsert");
		if(BiSchema.hasAccessRight(getSessionHelper(), "quocreate")) {
			jxSetVisible("panelQuotation",true);
			jxSetVisible("tabQuotation",true);
		} else {
			jxSetVisible("panelQuotation",false);
			jxSetVisible("tabQuotation",false);
		}
	}
	
	@Override
   	public void afterDisplayAction(final Component p_comp, int p_mode){
		//generate default detail rows for add new record
		if (p_mode == JxZkBiBase.MODE_ADD){
			final Button btAddJob = (Button) p_comp.getFellowIfAny("btAddQuoDet_" + BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM));
			final Button btAddPrintItem = (Button) p_comp.getFellowIfAny("btAddQuoDet_" + BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM));
			
			//simulate click event set actionIdx to button itself (default is popup comp), so need to clear after use
			btAddJob.setAttribute("actionIdx", -1);  //set actionIdx to button itself
			Events.echoEvent("onClick",btAddJob, null);
			btAddPrintItem.setAttribute("actionIdx", 0);
			Events.echoEvent("onClick",btAddPrintItem, null);
			
			//clear actionIdx to avoid messed up insert logic
			new ZkBiAbstractLongOp(p_comp){
				public ReturnMsg longOp() {
					btAddJob.removeAttribute("actionIdx");
					btAddPrintItem.removeAttribute("actionIdx");
					return null;
				}
			};
			
			//change the initial focus
			new ZkBiAbstractLongOp(p_comp, null, 500){
				public ReturnMsg longOp() {
					Component comp = (Component) jxAdd("inv_sellto").getNativeObject();
					((HtmlBasedComponent) comp).focus();
					return null;
				}
			};
			
			addInitFlag = true;
		}
		
   	}
	protected void afterUnDeleteLink(BiResult sr,int idx)
	{
		super.afterUnDeleteLink(sr,idx);
		if(sr.getView().getName().equals("hw.HwQuoExtra")) {
			((BiResultHwQuotation) getBr()).realCalQuoTotal();
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		super.afterDeleteLink(sr,idx);
		if(sr.getView().getName().equals("hw.HwQuoExtra")) {
			((BiResultHwQuotation) getBr()).realCalQuoTotal();
		}
	}	
}
