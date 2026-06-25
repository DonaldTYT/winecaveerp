package com.uniinformation.zkbi.erpv4;

import java.util.HashSet;
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
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerOsPoDet extends ZkBiComposerBase {
	Window popupPoScr = null;
    BiResult popupPoBr = null;
	JxZkBiBase popupJx = null;
	@Override
	protected void setupAddButton(final BiResult result)
	{
	}
	@Override
    protected void setupDeleteButton(final BiResult result)
	{
		Button btnPrintLabel;
    	UniLog.log("ZkBiComposerShipmark setupDeleteButton");
		if(!result.allowUpdate()) return;
    	if(masterWin.hasFellow("btDelete")) {
    		btnPrintLabel = (Button) masterWin.getFellow("btDelete");
    	} 
    	else {	
	        btnPrintLabel = new ZkBiButton();
	        btnPrintLabel.setLabel(sessionHelper.getBtLabel("Goods Receive"));
	        btnPrintLabel.setId("btCreateGRN");
//	        batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnPrintLabel, "fa-print");
    	} 
    	

        btnPrintLabel.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		java.util.Set selection = listModelList.getSelection();
            		RpcClient rpc = sessionHelper.getRpcClient();
        			Vector args = new Vector();
        			args.add(DateUtil.now());
          			if(selection.size() <= 0) {
      					Messagebox.show(
   							sessionHelper.getLabel("Please Select Items To Receive"),
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
          			String fromLoc = null;
          			String vdCode = null;
          			boolean requireLoc = Erpv4Config.requiredLoc(getSessionHelper());
            		for(Iterator it=selection.iterator();it.hasNext();) {
        				Object o = it.next();
        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
        				UniLog.log("Create GRN for " + idx);
        				result.loadOneRecV(idx);
        				double qty = result.getCell("stmd_qty").getDouble() - result.getCell("pds_rcvqty").getDouble() ;
        				if(qty > 0.0)  {
//        					String s = result.getCell("st_mbrand").getString();
        					String s = result.getCell("stm_ref2").getString();
        					if(vdCode == null) {
        						vdCode = s;
        						args.add(vdCode);
        					} else {
        						if(!vdCode.equals(s)) {
        							Messagebox.show(
        									sessionHelper.getLabel("Create GR Failed : Cannot create single GRN for multiple supplier"),
        									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        							return;
        						}
        					}	
      						if(fromLoc == null) {
      							if(requireLoc) {
       								fromLoc = result.getCellString("stm_fromloc");
        						} else {
        							fromLoc = "";
        						}
        						args.add(fromLoc);
        					} else {
      							if(requireLoc) {
      								if(!fromLoc.equals(result.getCellString("stm_fromloc"))) {
        								Messagebox.show(
        									sessionHelper.getLabel("Create GR Failed : Cannot create single GRN for multiple location"),
        									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        								return;
      								}
      							}
        					}
        					args.add(result.getCell("stmd_org").getInt());
        					args.add(result.getCell("stmd_irg").getInt());
        					args.add(result.getCell("stmd_qorg").getInt());
        					args.add(result.getCell("stmd_qirg").getInt());
        					args.add(result.getCell("stmd_ref4").getString());
        					args.add(qty);
        				}
            		}
            		if(vdCode == null) {
        				Messagebox.show(
        					sessionHelper.getLabel("Create GRN Failed : No Outstanding Item in selected list"),
        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        				return;
            		}
            		String cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
            		rpc.callSegment("setCocodeBaseccy",
            				new VectorUtil()
            				.addElement(cocode)
            				.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),cocode))
            				.toVector()
            				);
            		
            		Value v = rpc.callSegment(
							"erpv4CreateGRN",
							args
						);
            		rpc.close();
    				if(v != null && v.toString().startsWith("OK")) {
    					
       					// redir to P.O. Update Page
//    					JSONObject jo = new JSONObject();
//    					JSONArray ja = new JSONArray();
//    					BiView pov = result.getView().getSchema().getViewByName("AfsGR");
//    					ja.put(pov.getTable().getName());
//    					jo.put("tablist", ja);
//    					jo.put("wherestr", pov.getColumnByLabel("stm_mrg").getSelectName() + " = " + mrg);
//    					String key = sessionHelper.putOneTimeData( jo);
//    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=update&viewid=AfsGR&page_id=AfsGR_01&zul=zkbiloader.zul&querycondition="+key);
                		if(popupPoScr == null) {
                			popupPoScr = ZkUtil.newPopupWindow(sessionHelper.getBtLabel("Goods Receive"),masterWin);
                			popupPoScr.setClosable(false);
//                			popupPoScr.setWidth("1920px");
//               			popupPoScr.setHeight("1000px");
                			popupPoScr.setWidth("95%");
                			popupPoScr.setHeight("95%");
                			popupPoScr.setContentStyle("overflow:auto;");
                			
                			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
                			if(schema == null) schema = BiSchema.loadSchema(sessionHelper);
                			BiView view = schema.getViewByName(getGrView());
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
            						if(p_br.getLastUpdate() != null) return;
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
    					int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
                		popupPoBr.clearCondition();
                		popupPoBr.addCustomCondition("stm_mrg = " + mrg);
                		popupPoBr.query(true);
                		if(popupPoBr.getRowCount() > 0 ) {
                			popupPoBr.loadOneRecV(0);
                			popupPoBr.fetchOneRecV(0);
                			popupPoBr.clearLastUpdate();
                			popupJx.setIsMobile(false);
                			popupJx.bindCellCollection(popupPoBr,JxZkBiBase.MODE_UPDATE);
                			popupJx.translateAllComp(popupPoBr);  //andrew231205 fix radio button not translate bug
                			popupJx.jxSetVisible("btUpdate",false);
    					    popupJx.jxSetVisible("btAdd",true);
    					    popupJx.showForm();	
    					    popupJx.doModalUpdate();   					
                		} else {
                			Messagebox.show(
            					"Fatal System Error : Reason Unknown. Code 3102",
            					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            			
                		}
    					
    					
    					// redir to P.O. Update Page
    				} else {
    					if(v == null) {
    						Messagebox.show(
    						"Confirm Failed : Unknown Reason",
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
    						
    					} else {
    						Messagebox.show(
    								"Confirm Failed : " + v.toString(),
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
    					}
    				}
            	}
        	}
        );
        setupBatchModeButton(btnPrintLabel);
	}
	
	protected String getGrView() {
		return("erpv4.GR");
	}
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		if(!result.allowUpdate()) hasAUDColumn=false;
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
}
