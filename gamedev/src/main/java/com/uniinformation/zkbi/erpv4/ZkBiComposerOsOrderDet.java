package com.uniinformation.zkbi.erpv4;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZkBiComposerOsOrderDet extends ZkBiComposerBase {
	class PopupControl  {
		Window popupPoScr = null;
		BiResult popupPoBr = null;
		JxZkBiBase popupJx = null;
		PopupControl (String p_viewName) throws Exception {
            			popupPoScr = ZkUtil.newPopupWindow(sessionHelper.getLabel("Purchase Order Popup"),masterWin);
            			popupPoScr.setClosable(false);
//            			popupPoScr.setWidth("1920px");
//           			popupPoScr.setHeight("1000px");
                		popupPoScr.setWidth("95%");
                		popupPoScr.setHeight("95%");
            			popupPoScr.setContentStyle("overflow:auto;");
            			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
            			if(schema == null) schema = BiSchema.loadSchema(sessionHelper);
            			BiView view = schema.getViewByName(p_viewName);
            			UniLog.log("queryResult view:"+view);
//            			popupPoBr = view.newBiResult(sessionHelper.getVcode(),null,"com.uniinformation.bicore.afs.BiResultAfsPO",sessionHelper);
            			popupPoBr = view.newBiResult(sessionHelper.getLoginId(),null,null,sessionHelper);
//            			popupJx = JxZkBiBase.buildDetailWindow(popupPoBr, popupPoScr, false, true, (JxZkBiBaseCallback) null);
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
	}
	
	Hashtable <String,PopupControl> popupViewHash = new Hashtable();
//	Window popupPoScr = null;
//  BiResult popupPoBr = null;
//	JxZkBiBase popupJx = null;
	class StmdRec {
		public int sid;
		public String type;
		public int org;
		public int irg;
		public double qty;
		StmdRec(int p_sid,String p_type,int p_irg,int p_org,double p_qty) {
			sid = p_sid;
			type = p_type;
			org = p_org;
			irg = p_irg;
			qty = p_qty;
		}
	}
	void doPopupStmd(String p_viewName ,int p_mrg) throws Exception
	{
		PopupControl ppc = popupViewHash.get(p_viewName);
            		if(ppc == null) {
            			ppc = new PopupControl(p_viewName);
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
						ppc.popupJx.translateAllComp(ppc.popupPoBr);  //andrew231205 fix radio button not translate bug
						ppc.popupJx.jxSetVisible("btUpdate",false);
						ppc.popupJx.jxSetVisible("btAdd",true);
						ppc.popupJx.showForm();	
						ppc.popupJx.doModalUpdate();
            		} else {
            			Messagebox.show(
            					"Fatal System Error : Reason Unknown. Code 3101",
            					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            			
            		}
	}
	@Override
	protected void setupAddButton(final BiResult result)
	{
	}
	@Override
    protected void setupDeleteButton(final BiResult result)
	{
		Button btnCreatePO;
		Button btnAllocate;
		if(!result.allowUpdate()) return;
    	UniLog.log("ZkBiComposerShipmark setupDeleteButton");
	        btnCreatePO = new ZkBiButton();
	        btnCreatePO.setLabel(sessionHelper.getBtLabel("Issue Purchase Order"));
	        btnCreatePO.setId("btCreatePo");
//          batchActionBar.appendChild(btnCreatePO);
	        abHelper.addButton(btnCreatePO, "fa-print"); 
	        btnAllocate = new ZkBiButton();
	        btnAllocate.setLabel(sessionHelper.getBtLabel("Allocate From Stock"));
	        btnAllocate.setId("btAllocate");
//	        batchActionBar.appendChild(btnAllocate);
	        abHelper.addButton(btnAllocate, "fa-print"); 
	 
     btnCreatePO.addEventListener("onClick",
     new EventListener() {
     	public void onEvent(Event event) throws Exception {
     		java.util.Set selection = listModelList.getSelection();
     		RpcClient rpc = sessionHelper.getRpcClient();
 			Vector args = new Vector();
 			java.util.Date td = DateUtil.now();
 			args.add(td);
   			if(selection.size() <= 0) {
					Messagebox.show(
						sessionHelper.getLabel("Please Select Items To Purchase"),
						 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
					return;
 			}
 			String vdCode = null;
 			String ordType= null;
 			String poViewName = null;
 			boolean requireApprove = "Y".equals(Erpv4Config.getString(getSessionHelper(),"QUORequireApproval"));
 			
     		for(Iterator it=selection.iterator();it.hasNext();) {
 				Object o = it.next();
 				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
 				UniLog.log("Create P.O. for " + idx);
 				result.loadOneRecV(idx);
 				double qty = result.getCell("qdst_ostqty").getDouble();
 				if(qty > 0.0)  {
 					String s = result.getCell("st_mbrand").getString();
 					
 					if(vdCode == null) {
 						vdCode = s;
// 						String ss = result.getCell("inv_invno").getString();
 						ordType = getOrdType(result);
 						SelectUtil su = result.getSelectUtil();
 						TableRec tr = su.getQueryResult("select * from st_brand where stbd_code = '"+vdCode+"'");
 						if(tr.getRecordCount() <= 0) {
 							Messagebox.show(
 									sessionHelper.getLabel("Create PO Failed : brand not exist"),
 									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
 							return;
 						}
 						tr.setRecPointer(0);
 						args.add(tr.getFieldString("stbd_supplier"));
						args.add(ordType);
 //						String ss = result.getCell("inv_invno").getString();
 						poViewName = getPoView(result,ordType);
 					} else {
 						if(!vdCode.equals(s)) {
 							Messagebox.show(
 									sessionHelper.getLabel("Create PO Failed : Cannot create single PO for multiple brand"),
 									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
 							return;
 						}
 						if( !getOrdType(result).equals(ordType)) {
 							Messagebox.show(
 									sessionHelper.getLabel("Create PO Failed : Cannot create single PO for both parts and machine"),
 									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
 							return;
 						}
 					}
 					args.add(result.getCell("qdst_qorg").getInt());
 					args.add(result.getCell("qdst_qirg").getInt());
 					args.add(qty);
 					args.add(result.getCell("ind_ref1").getString());
 					args.add(result.getCell("ind_ref2").getString());
 				}
     		}
     		if(vdCode == null) {
 				Messagebox.show(
 					sessionHelper.getLabel("Create PO Failed : No Outstanding Item in selected list"),
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
						"erpv4CreatePO",
						args
					);
     		rpc.close();
				if(v != null && v.toString().startsWith("OK")) {
    				int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
					doPopupStmd(poViewName,mrg);
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
       
        btnAllocate.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		java.util.Set selection = listModelList.getSelection();
            		if(selection.size() <= 0) {
            			Messagebox.show(
            					sessionHelper.getLabel("Please Select Items To Allocate"),
            					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            			result.rollbackWork();
            			return;
            		}
            		result.beginWork();
            		RpcClient rpc = result.getSelectUtil().getRpcClient();
            		Value val = rpc.callSegment(
            				"erpv4GenbucketBegin", new Vector()
            				);	
            		if(val == null || !val.toString().startsWith("OK")) {
            			Messagebox.show(
            					sessionHelper.getLabel("Allocate Failed") +  ": genbucket begin failed",
            					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            			result.rollbackWork();
            			return;
            		}
            		Vector args = new Vector();
            		Hashtable <Integer,LinkedHashMap <Integer,Double>> irgHash = new Hashtable <Integer,LinkedHashMap<Integer,Double>> ();
            		try {
            			for(Iterator it=selection.iterator();it.hasNext();) {
            				TableRec tr;
            				Object o = it.next();
            				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
            				UniLog.log("Create P.O. for " + idx);
            				result.loadOneRecV(idx);
            				double ostQty = result.getCell("qdst_ostqty").getDouble();
            				if(ostQty <= 0) continue;
            				Hashtable <String,Hashtable<Integer,Double>> stmdHash = new Hashtable<String,Hashtable<Integer,Double>> ();
            				Vector <StmdRec> stmdRecs = new Vector<StmdRec>();
            				boolean needUpdate = false;
            				tr = result.getSelectUtil().getQueryResult(
            						"select serial_id,stmd_tdtype,stmd_irg,stmd_org,stmd_qty from stmovd where stmd_mrg = " + result.getCell("ind_rg").getInt() 
            						+ " and stmd_qorg = " + result.getCell("ind_odrg").getInt()
            						+ " and stmd_qirg = " + result.getCell("ind_irg").getInt()
            						, null);
            				for(int i=0;i<tr.getRecordCount();i++) {
            					tr.setRecPointer(i);
            					stmdRecs.add(new StmdRec(
            							(Integer) tr.getField("serial_id"),
            							(String) tr.getField("stmd_tdtype"),
            							(Integer) tr.getField("stmd_irg"),
            							(Integer) tr.getField("stmd_org"),
            							(Double) tr.getField("stmd_qty")
            							));
            				}

            				LinkedHashMap<Integer,Double > orgHash = irgHash.get(result.getCell("qdst_qirg").getInt());
            				if(orgHash == null) {
            					orgHash = new LinkedHashMap <Integer,Double>();
            					tr = result.getSelectUtil().getQueryResult(
            							"select pds_org,pds_stockqty from podetstatus where pds_irg = " + result.getCell("qdst_qirg").getInt() + " and pds_stockqty > 0 order by pds_org", null);
            					for(int i=0;i<tr.getRecordCount();i++) {
            						orgHash.put((Integer) tr.getField("pds_org"),(Double) tr.getField("pds_stockqty"));
            					}
            					irgHash.put(result.getCell("qdst_qirg").getInt(), orgHash);
            				}
            				for (int org : orgHash.keySet()) {
            					double remain = orgHash.get(org);
            					double consumed=0;
            					if(remain > ostQty) {
            						consumed = ostQty;
            						remain -= ostQty;
            						ostQty = 0;
            					} else {
            						consumed = remain;
            						ostQty -= remain;
            						remain = 0;
            					}
            					orgHash.put(org,remain);
            					// issue reserve deduct here;
            					if(consumed > 0) {
            						StmdRec stmdr=null;
            						for(int i=0;i<stmdRecs.size();i++) {
            							if(stmdRecs.get(i).type.equals("SI") &&
            									stmdRecs.get(i).irg == result.getCell("qdst_qirg").getInt() &&
            									stmdRecs.get(i).org == org
            									) {
            								stmdr = stmdRecs.get(i);
            								break;
            							}
            						}
            						if(stmdr == null) {
            							stmdr = new StmdRec(
            									0,
            									"SI",
            									result.getCell("qdst_qirg").getInt(),
            									org,
            									0
            									);
            							stmdRecs.add(stmdr);
            						}
            						needUpdate = true;
            						stmdr.qty += consumed;
            					}
            					if(ostQty <= 0) break;
            				}
            				if(needUpdate) {
            					args = new Vector();
            					args.add(result.getCell("ind_rg").getInt());
            					args.add(result.getCell("ind_odrg").getInt());
            					args.add(result.getCell("ind_irg").getInt());	
            					for(StmdRec stmdr:stmdRecs) {
            						args.add(stmdr.type);
            						args.add(stmdr.org);
            						args.add(stmdr.irg);
            						args.add("");   /* Only support single blank location code at this moment 2021/04/08 */
            						args.add(stmdr.qty);
            					}
            					val = rpc.callSegment(
            							"erpv4UpdateQuodetStmdGenBucket",
            							args
            							);
            					if(val == null || !val.toString().startsWith("OK")) {
            						result.rollbackWork();
            						Messagebox.show(
            								sessionHelper.getLabel("Allocation Failed"),
            								sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            					} else {
            						BiTable bt = result.getView().getSchema().getTable("stmovd_any");
            						result.getSelectUtil().executeUpdate("delete from " + bt.getDbtName(),
            								new Wherecl()
            						.andUniop("stmd_mrg", "=", result.getCell("ind_rg").getInt())
            						.andUniop("stmd_qorg", "=", result.getCell("ind_odrg").getInt())
            						.andUniop("stmd_qirg", "=", result.getCell("ind_irg").getInt())
            								);
            						tr = bt.newTableRec();
            						for(StmdRec stmdr:stmdRecs) {
            							tr.addRecord();
            							tr.setField("stmd_mrg",result.getCell("ind_rg").getInt());
            							tr.setField("stmd_qorg",result.getCell("ind_odrg").getInt());
            							tr.setField("stmd_qirg",result.getCell("ind_irg").getInt());
            							tr.setField("stmd_ref2",result.getCell("ind_ref1").getString());
            							tr.setField("stmd_ref3",result.getCell("ind_ref2").getString());
            							tr.setField("stmd_tdtype",stmdr.type);
            							tr.setField("stmd_irg",stmdr.irg);
            							tr.setField("stmd_org",stmdr.org);
            							tr.setField("stmd_qty",stmdr.qty);
            						}
            						result.getSelectUtil().insertByTableRec(bt.getDbtName(), tr,true,bt.getSerialId());
            					}
            				}
            			}	
            			val = rpc.callSegment(
            					"erpv4GenbucketCommit", new Vector()
            					);
            			ReturnMsg rtnMsg = null;
            			if(val != null && val.toString().startsWith("OK")) {
            				String s = StringUtil.strpart(val.toString(), 4 , -1);
            				if(!s.trim().equals("")) {
            					rtnMsg = (GenbucketUtil.qoGenBucketCheckResult(s,getSessionHelper()));
            				} 
            			} else {
            				if(val != null) 
            					rtnMsg = new ReturnMsg(false,val.toString());
            				else
            					rtnMsg = new ReturnMsg(false,"Reason Unknown");
            			}
            			if(rtnMsg != null && !rtnMsg.getStatus()) {
            				result.rollbackWork();
            				Messagebox.show(
            						sessionHelper.getLabel("Allocation Failed") + ": " + rtnMsg.getMsg(),
            						sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            			} else {
            				result.commitWork();
            				Messagebox.show(
            						sessionHelper.getLabel("Allocation Completed"),
            						sessionHelper.getLabel("Success"), 
            						Messagebox.OK , 
            						Messagebox.INFORMATION, 
            						new org.zkoss.zk.ui.event.EventListener() {
            							public void onEvent(Event evt) throws InterruptedException {
            								refresh(result, null,-1,true);
            							}
            						}
            						);	
            			}

            		} catch (Exception ex){
            			result.rollbackWork();
            			UniLog.log(ex);
            		}

            	}
        	}
        );
       	setupBatchModeButton(btnCreatePO);
       	setupBatchModeButton(btnAllocate);
	}
	 protected String getOrdType(BiResult p_result) {
 		return("general");
	 }
	 protected String getPoView(BiResult p_result,String p_ordType) {
//		return("erpv4.PO");
		return("erpv4.PoMulti");
	 }
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		if(!result.allowUpdate()) hasAUDColumn=false;
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
}
