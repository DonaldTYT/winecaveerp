package com.uniinformation.zkbi.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerOsDeli extends ZkBiComposerReport {
	class PopupControl  {
		Window popupPoScr = null;
		BiResult popupPoBr = null;
		JxZkBiBase popupJx = null;	
		BiResult pbr;
		PopupControl (String p_viewName,BiResult p_pbr) throws Exception {
			pbr = p_pbr;
   			popupPoScr = ZkUtil.newPopupWindow(sessionHelper.getLabel("Delivery Order Popup"),masterWin);
   			popupPoScr.setClosable(false);
//			popupPoScr.setWidth("1920px");
//			popupPoScr.setHeight("1000px");
            popupPoScr.setWidth("95%");
            popupPoScr.setHeight("95%");
			popupPoScr.setContentStyle("overflow:auto;");
			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
			if(schema == null) schema = BiSchema.loadSchema(sessionHelper);
			BiView view = schema.getViewByName(p_viewName);
			UniLog.log("queryResult view:"+view);
			popupPoBr = view.newBiResult(sessionHelper.getLoginId(),null,null,sessionHelper);
			popupJx = JxZkBiBase.buildDetailWindow(popupPoBr, popupPoScr, false, true, 
			new JxZkBiBaseCallback()  {
				public void biBaseRefresh(BiResult p_br) {
				}
				public void biBaseOpen() {
				}
				public void biBaseRefreshItem(Object p_obj) {
				}
				public void biBaseRefreshListitems(Object p_obj) {
				}
				public void biBaseClose(BiResult p_br) {
					if(p_br.getLastUpdate() != null) {
						refresh(pbr,null);
						return;
					}
					try {
						p_br.getSelectUtil().executeUpdate("delete from stmovd",
							new Wherecl()
							.andUniop("stmd_mrg", "=", p_br.getCell("stm_mrg").getInt())
							);
						p_br.getSelectUtil().executeUpdate("delete from stmov",
							new Wherecl()
							.andUniop("stm_mrg", "=", p_br.getCell("stm_mrg").getInt())
							);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				@Override
				public ReturnMsg fetchNext(BiResult p_br) {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public ReturnMsg fetchPrevious(BiResult p_br) {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public String getExtraInfo() {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public Boolean hasNextRec() {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public Boolean hasPrevRec() {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public HashSet<BiColumn> getVisibleColumns(BiResult p_br) {
					// TODO Auto-generated method stub
					return null;
				}
			}
					
			);
		}
	}
	Hashtable <String,PopupControl> popupViewHash = new Hashtable();
	
	void doPopupStmd(String p_viewName ,int p_mrg,BiResult p_pbr) throws Exception
	{
		PopupControl ppc = popupViewHash.get(p_viewName);
            		if(ppc == null) {
            			ppc = new PopupControl(p_viewName,p_pbr);
            			popupViewHash.put(p_viewName, ppc);
            		}
    				int mrg = p_mrg;
               		ppc.popupPoBr.clearCondition();
            		ppc.popupPoBr.addCustomCondition("stm_mrg = " + mrg);
            		ppc.popupPoBr.query(true);
            		if(ppc.popupPoBr.getRowCount() > 0 ) {
            			ppc.popupPoBr.loadOneRecV(0);
            			ppc.popupPoBr.fetchOneRecV(0);
            			ppc.popupPoBr.clearLastUpdate();
            			ppc.popupJx.setIsMobile(false);
					    ppc.popupJx.bindCellCollection(ppc.popupPoBr,JxZkBiBase.MODE_UPDATE);
						ppc.popupJx.translateAllComp(ppc.popupPoBr);
					    ppc.popupJx.jxSetVisible("btUpdate",false);
					    ppc.popupJx.jxSetVisible("btAdd",true);
					    ppc.popupJx.showForm();	
					    ppc.popupJx.doModalUpdate();
            		} else {
            			Messagebox.show(
        					"Fatal System Error : Reason Unknown. Code 3102",
        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        			
            		}
            					
    				
	}	
	
	
//	Window popupPoScr = null;
//    BiResult popupPoBr = null;
//	JxZkBiBase popupJx = null;
	
	static public void autoGenerateDn(SessionHelper sessionHelper,Date p_from ,Date p_to) throws Exception {
        String doViewName = "erpv4.DoMulti";
		BiResult result = sessionHelper.getBiSchema().getViewByName("erpv4.OsDeli").newBiResult(sessionHelper.getLoginId(), null,null, sessionHelper);
		BiResult doresult = sessionHelper.getBiSchema().getViewByName(doViewName).newBiResult(sessionHelper.getLoginId(), null,null, sessionHelper);
		for(Date d = p_from;!d.after(p_to);d = DateUtil.nextday(d)) {
			result.clearCondition();
			result.addCustomCondition("palc_delqty > 0");
			result.addCustomCondition("inv_date = '" + DateUtil.toDateString(d, "yyyy/mm/dd") + "'");
			result.query();
			if(result.getRowCount() > 0) {
					RpcClient rpc = sessionHelper.getRpcClient();
				UniLog.log("Generate D/N for " + DateUtil.toDateString(d, "yyyy/mm/dd") + " " + result.getRowCount() + " invoices ");
        			Vector args = new Vector();
//        			args.add(DateUtil.now());
        			String custCode = null;
          			String fromLoc = "";
        			String ordType= null;

            		for(int i=0;i<result.getRowCount();i++) {
        				result.loadOneRecV(i);
        				String s = result.getCell("inv_vcode").getString();

        				if(custCode == null) {
        						custCode = s;
        						ordType = "general";
        				} else {
        					if(!custCode.equals(s)) {
        						if(Erpv4Config.allowMultipleCustomerDN(sessionHelper,result.getSelectUtil())) {
        							custCode = "";
        						} else {
        						Messagebox.show(
    								sessionHelper.getLabel("Create D/N Failed : Cannot create single D/N for multiple customer"),
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        						return;
        						}
        					}
        				}
        				
        				double qty = result.getCell("palc_delqty").getDouble();
        				if(qty > 0.0)  {
        					args.add(result.getCell("palc_qorg").getInt());
        					args.add(result.getCell("palc_org").getInt());
        					args.add(result.getCell("palc_irg").getInt());
        					args.add(result.getCell("palc_qirg").getInt());
        					if(qty == 1.0) {
        						SelectUtil su = result.getSelectUtil();
        						TableRec tr = su.getQueryResult("select * from stockserial where stsn_org = ? and stsn_irg = ? and stsn_nqty > 0",
        								new Wherecl()
        									.appendArgument(result.getCell("palc_org").getInt())
        									.appendArgument(result.getCell("palc_irg").getInt())
        								);
        						if(tr.getRecordCount() == 1 ) {
        							tr.setRecPointer(0);
        							args.add(tr.getFieldString("stsn_ref4"));
        						} else args.add("");
        					} else args.add("");
        					args.add(qty);
        					if(result.getCell("ind_eratio") != null) {
        						args.add(qty/result.getCell("ind_eratio").getDouble());
        						args.add(result.getCell("ind_unit").getString());
        						args.add(result.getCell("ind_eratio").getDouble());
        					} else {
        						args.add(qty);
        						args.add(result.getCell("st_unit").getString());
        						args.add(1.0);
        					}
        				}
            		}
            		if(custCode == null) {
        				Messagebox.show(
        					sessionHelper.getLabel("Create D/N Failed : No Outstanding Item in selected list"),
        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        				return;
            		}
       				args.add(0,fromLoc);
    				args.add(0,ordType);
       				args.add(0,custCode);
        			args.add(0,d);
            		String cocode = Erpv4Config.getDefaultCoCode(sessionHelper);
            		rpc.callSegment("setCocodeBaseccy",
            				new VectorUtil()
            				.addElement(cocode)
            				.addElement(Erpv4Config.getBaseCcy(sessionHelper,cocode))
            				.toVector()
            				);
				
            		Value v = rpc.callSegment(
							"erpv4CreateDN",
							args
						);
            		rpc.close();
    				if(v != null && v.toString().startsWith("OK")) {
       					// redir to DO Update Page
    					int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
    					doresult.clearCondition();
    					doresult.addCustomCondition("stm_mrg = " + mrg );
    					doresult.query();
    					if(doresult.getRowCount() == 1 ) {
    						doresult.loadOneRecV(0);
    						doresult.fetchOneRecV(0);
    						doresult.getCell("stm_status").set("Confirmed");
    						doresult.updateCurrent();
    					}
//    					doViewName,mrg
    				}
			}
		}
	}
	
	@Override
    protected void setupExportButton(final BiResult result)
	{
		super.setupExportButton(result);
		Button btnPrintLabel;
    	UniLog.log("ZkBiComposerOsDeli setupDeleteButton");
		if(!result.allowUpdate()) return;
    	if(masterWin.hasFellow("btDelete")) {
    		btnPrintLabel = (Button) masterWin.getFellow("btDelete");
    	} 
    	else {	
	        btnPrintLabel = new ZkBiButton();
	        //btnPrintLabel.setLabel("Delivery Note");
	        btnPrintLabel.setLabel(sessionHelper.getBtLabel("Delivery Note"));
	        btnPrintLabel.setId("btCreateDN");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnPrintLabel, "fa-print");
    	} 
    	
        btnPrintLabel.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		java.util.Set selection = listModelList.getSelection();
            		RpcClient rpc = sessionHelper.getRpcClient();
        			Vector args = new Vector();
//        			args.add(DateUtil.now());
        			String custCode = null;
          			String fromLoc = null;
        			String ordType= null;
        			String doViewName = null;
          			boolean requireLoc = Erpv4Config.requiredLoc(getSessionHelper());
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							sessionHelper.getLabel("Please Select Items To Deliver"),
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
        			
        			boolean requireApprove = "Y".equals(Erpv4Config.getString(getSessionHelper(),"QUORequireApproval"));
        			
            		for(Iterator it=selection.iterator();it.hasNext();) {
        				Object o = it.next();
        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
        				UniLog.log("Create D/N for " + idx);
        				result.loadOneRecV(idx);
        				String s = result.getCell("inv_vcode").getString();

        				/*
        				if(requireApprove) {
        					if(!result.getCell("inv_allowdn").getBoolean()) {
        						Messagebox.show(
 									"Create DN Failed : Sales Order not yet approved for delivery",
 									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        						return;
        					}
        				}
        				*/
        				if(custCode == null) {
        						custCode = s;
 //       						args.add(custCode);
        						ordType = getOrdType(result);
        						doViewName = getDoView(result,ordType);
        				} else {
        					if(!custCode.equals(s)) {
        						if(Erpv4Config.allowMultipleCustomerDN(sessionHelper,result.getSelectUtil())) {
        							custCode = "";
        						} else {
        						Messagebox.show(
    								sessionHelper.getLabel("Create D/N Failed : Cannot create single D/N for multiple customer"),
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        						return;
        						}
        					}
//        					if(!result.getCell("inv_invno").getString().startsWith(ordType)) {
        					if(!getOrdType(result).equals(ordType)) {
        						ordType = getOrdType(null);
        						doViewName = getDoView(result,ordType);
        						/*
        						Messagebox.show(
 									"Create PO Failed : Cannot create single DN for both parts and machine",
 									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        						return;
        						*/
        					}
        				}
        				
						if(fromLoc == null) {
  							if(requireLoc) {
   								fromLoc = result.getCellString("inv_loc");
    						} else {
    							fromLoc = "";
    						}
    					} else {
  							if(requireLoc) {
  								if(!fromLoc.equals(result.getCellString("inv_loc"))) {
    								Messagebox.show(
    									sessionHelper.getLabel("Create D/N Failed : Cannot create single D/N for multiple location"),
    									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
    								return;
  								}
  							}
    					}       				
        				
        				double qty = result.getCell("palc_delqty").getDouble();
        				if(qty > 0.0)  {
        					args.add(result.getCell("palc_qorg").getInt());
        					args.add(result.getCell("palc_org").getInt());
        					args.add(result.getCell("palc_irg").getInt());
        					args.add(result.getCell("palc_qirg").getInt());
        					if(qty == 1.0) {
        						SelectUtil su = result.getSelectUtil();
        						TableRec tr = su.getQueryResult("select * from stockserial where stsn_org = ? and stsn_irg = ? and stsn_nqty > 0",
        								new Wherecl()
        									.appendArgument(result.getCell("palc_org").getInt())
        									.appendArgument(result.getCell("palc_irg").getInt())
        								);
        						if(tr.getRecordCount() == 1 ) {
        							tr.setRecPointer(0);
        							args.add(tr.getFieldString("stsn_ref4"));
        						} else args.add("");
        					} else args.add("");
        					args.add(qty);
        					if(result.getCell("ind_eratio") != null) {
        						args.add(qty/result.getCell("ind_eratio").getDouble());
        						args.add(result.getCell("ind_unit").getString());
        						args.add(result.getCell("ind_eratio").getDouble());
        					} else {
        						args.add(qty);
        						args.add(result.getCell("st_unit").getString());
        						args.add(1.0);
        					}
        				}
            		}
            		if(custCode == null) {
        				Messagebox.show(
        					sessionHelper.getLabel("Create D/N Failed : No Outstanding Item in selected list"),
        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        				return;
            		}
       				args.add(0,fromLoc);
    				args.add(0,ordType);
       				args.add(0,custCode);
        			args.add(0,DateUtil.now());
            		String cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
            		rpc.callSegment("setCocodeBaseccy",
            				new VectorUtil()
            				.addElement(cocode)
            				.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),cocode))
            				.toVector()
            				);
            		Value v = rpc.callSegment(
							"erpv4CreateDN",
							args
						);
            		rpc.close();
    				if(v != null && v.toString().startsWith("OK")) {
       					// redir to DO Update Page
    					int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
    					doPopupStmd(doViewName,mrg,result);
    					
//    					JSONObject jo = new JSONObject();
//    					JSONArray ja = new JSONArray();
//    					BiView pov = result.getView().getSchema().getViewByName("AfsDO");
//    					ja.put(pov.getTable().getName());
//    					jo.put("tablist", ja);
//    					jo.put("wherestr", pov.getColumnByLabel("stm_mrg").getSelectName() + " = " + mrg);
//    					String key = sessionHelper.putOneTimeData( jo);
//    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=update&viewid=AfsDO&page_id=AfsDO_01&zul=zkbiloader.zul&querycondition="+key);
    					
    					
//    	           		if(popupPoScr == null) {
//                			popupPoScr = ZkUtil.newPopupWindow("Test Popup",masterWin);
//                			popupPoScr.setWidth("1920px");
//                			popupPoScr.setHeight("1000px");
//                			popupPoScr.setContentStyle("overflow:auto;");
//                			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
//                			if(schema == null) schema = BiSchema.loadSchema(sessionHelper);
//                			BiView view = schema.getViewByName("AfsDO");
//                			UniLog.log("queryResult view:"+view);
//                			popupPoBr = view.newBiResult(sessionHelper.getVcode(),null,"com.uniinformation.bicore.afs.BiResultAfsDO");
//                			popupJx = JxZkBiBase.buildDetailWindow(popupPoBr, popupPoScr, false, true, 
//            				new JxZkBiBaseCallback()  {
//            					public void biBaseRefresh(BiResult p_br) {
//            					}
//            					public void biBaseOpen() {
//            					}
//            					public void biBaseRefreshItem(Object p_obj) {
//            					}
//            					public void biBaseRefreshListitems(Object p_obj) {
//            					}
//            					public void biBaseClose(BiResult p_br) {
//            						if(p_br.getLastUpdate() != null) return;
//            						try {
//            							p_br.getSelectUtil().executeUpdate("delete from stmovd",
//            								new Wherecl()
//            								.andUniop("stmd_mrg", "=", p_br.getCell("stm_mrg").getInt())
//            								);
//            							p_br.getSelectUtil().executeUpdate("delete from stmov",
//            								new Wherecl()
//            								.andUniop("stm_mrg", "=", p_br.getCell("stm_mrg").getInt())
//            								);
//            						} catch (Exception ex) {
//            							UniLog.log(ex);
//            						}
//            					}
//            				}
//                					
//                			);
//                		}
//                		popupPoBr.clearCondition();
//                		popupPoBr.addCustomCondition("stm_mrg = " + mrg);
//                		popupPoBr.query(true);
//                		if(popupPoBr.getRowCount() > 0 ) {
//                			popupPoBr.loadOneRecV(0);
//                			popupPoBr.fetchOneRecV(0);
//                			popupPoBr.clearLastUpdate();
//                			popupJx.setIsMobile(false);
//    					    popupJx.bindCellCollection(popupPoBr,JxZkBiBase.MODE_UPDATE);
//    					    popupJx.jxSetVisible("btUpdate",false);
//    					    popupJx.jxSetVisible("btAdd",true);
//    					    popupJx.showForm();	
//    					    popupJx.doModalUpdate();
//                		} else {
//                			Messagebox.show(
//            					"Fatal System Error : Reason Unknown. Code 3102",
//            					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//            			
//                		}
//                		
    					
    				} else {
    					if(v == null) {
    						Messagebox.show(
    						"Confirm Failed : Unknown Reason",
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
    						
    					} else {
    						Messagebox.show(
    								"Confirm Failed : " + v.toString().substring(4),
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
    					}
    				}
            	}
        	}
        );
        
        setupBatchModeButton(btnPrintLabel);
	}
	 protected String getOrdType(BiResult p_result) {
 		return("general");
	 }
	 protected String getDoView(BiResult p_result,String p_ordType) {
		if(Erpv4Config.allowMultipleCustomerDN(sessionHelper, p_result.getSelectUtil())) {
			return("erpv4.DoMulti");
		} else {
			return("erpv4.DO");
		}
	 }
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		if(!result.allowUpdate()) hasAUDColumn=false;
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
}
