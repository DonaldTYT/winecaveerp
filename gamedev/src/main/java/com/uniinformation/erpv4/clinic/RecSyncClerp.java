package com.uniinformation.erpv4.clinic;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.erpv4.RecSyncErpv4;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncClerp extends RecSyncErpv4 {
	public class clerpStockMovementHandler extends BiSyncHandler 
	{
		String destView;
		public clerpStockMovementHandler(String p_destView) {
			destView = p_destView;
			allowAdd = true;
		}
		@Override
		protected String getDestViewId() {
			return destView;
		}

		@Override
		protected String getKey(CellCollection p_bicol) {
			return("stm_stmgrg ="+p_bicol.getCellInt("stm_mrg")+" and stm_syncagent = '" + getBr().getSessionHelper().getAgent() + "'");
		}

		@Override
		protected ReturnMsg syncRec(CellCollection p_bicol, JSONObject p_jo) {
			// TODO Auto-generated method stub
			String MoSync = Erpv4Config.getString(getBr().getSessionHelper(),"MoSync");
			if(MoSync == null || MoSync.equals("")) return(null);
			try {
				int sid = ((BiCellCollection) p_bicol).getSid();
				if(sid > 0) {
					// version of remote transaction later then current sync , ignored
					int synccnt = p_bicol.getCellInt("stm_synccnt");
					if( synccnt >= p_jo.getInt("stm_updcnt") ) {
						return(null);
					}
				}
				p_bicol.getCell("stm_stmgrg").set(p_jo.getInt("stm_mrg"));
				p_bicol.getCell("stm_synccnt").set(p_jo.getInt("stm_updcnt"));
				p_bicol.getCell("stm_date").set(DateUtil.dateTimeStrToDate(p_jo.getString("stm_date")));
				p_bicol.getCell("stm_syncagent").set(p_jo.getString("stm_syncagent"));
				p_bicol.getCell("stm_status").set(p_jo.getString("stm_status"));
				p_bicol.getCell("stm_module").set("genmo");
				p_bicol.getCell("stm_cur").set("HKD");
				p_bicol.getCell("stm_ref2").set("S999");
				JSONArray stmdList = p_jo.getJSONArray(p_jo.getString("stmdLink"));
				BiResult sr = getBr().getSubLink("erpv4.MoGenericDet");
				CellCollection col;
				int ridx = 0;
				int tdindex = 0;
				for(int i=0;i<stmdList.length();i++) {
					JSONObject jo2 = stmdList.getJSONObject(i);

					if(MoSync.equals("clerpmaster")) {
						if(jo2.getString("stmd_tdtype").equals("JI")
						   && p_jo.getString("stm_fromloc").equals("TST01")) {
							if(ridx >= sr.getRowCount()) {
								col = sr.newRowCollection();								
								ReturnMsg rtn = sr.addSubRecord(col, ridx,"");
							} else {
								col = sr.getRowCollectionV(ridx);
							}
							col.getCell("stmd_tdtype").set("KO");
							col.getCell("stmd_irg").set(jo2.getInt("stmd_irg"));
							col.getCell("stmd_qty").set(jo2.getDouble("stmd_qty"));
							col.getCell("stmd_loc").set("CTR01");
							col.getCell("stmd_ref4").set("");
							col.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(getBr().getSessionHelper(), p_bicol.getCellString("stm_cocode")));
							ridx++;
							if(ridx >= sr.getRowCount()) {
								col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, ridx,"");
							} else {
								col = sr.getRowCollectionV(ridx);
							}
							col.getCell("stmd_tdtype").set("KI");
							col.getCell("stmd_irg").set(jo2.getInt("stmd_irg"));
							col.getCell("stmd_qty").set(jo2.getDouble("stmd_qty"));
							col.getCell("stmd_loc").set("CTR02");
							col.getCell("stmd_ref4").set(jo2.getString("stmd_ref4"));
							col.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(getBr().getSessionHelper(), p_bicol.getCellString("stm_cocode")));
							ridx++;
							col.getCell("stmd_tdindex").set(tdindex);
							tdindex++;
						}
						String stmdtype = jo2.getString("stmd_tdtype");
						if( 
								stmdtype.equals("MO")
								|| stmdtype.equals("RI")
								|| stmdtype.equals("MI")
								|| stmdtype.equals("RO")
								|| stmdtype.equals("JO")
								) {
							if(ridx >= sr.getRowCount()) {
								col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, ridx,"");
							} else {
								col = sr.getRowCollectionV(ridx);
							}
							col.getCell("stmd_tdtype").set(jo2.getString("stmd_tdtype"));
							col.getCell("stmd_irg").set(jo2.getInt("stmd_irg"));
							col.getCell("stmd_qty").set(jo2.getDouble("stmd_qty"));
							col.getCell("stmd_ref4").set(jo2.getString("stmd_ref4"));
							col.getCell("stmd_entryqty").set(jo2.getDouble("stmd_entryqty"));
							col.getCell("stmd_entryunit").set(jo2.getString("stmd_entryunit"));
							col.getCell("stmd_loc").set("CTR02");
							col.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(getBr().getSessionHelper(), Erpv4Config.getDefaultCoCode(getBr().getSessionHelper())));
							col.getCell("stmd_uprice").set(jo2.getDouble("stmd_uprice"));
							col.getCell("stmd_exprice").set(jo2.getDouble("stmd_exprice"));
							col.getCell("stmd_exprice1").set(jo2.getDouble("stmd_exprice1"));
							ridx++;
							col.getCell("stmd_tdindex").set(tdindex);
							tdindex++;
						}
						if(jo2.getString("stmd_tdtype").equals("KO")) {
							String kiRef4 = null;
							double kiQty = 0.0;
							if(jo2.has("stmdki_loc")) {
								kiRef4 = jo2.getString("stmdki_ref4");
								kiQty = jo2.getDouble("stmdki_qty");
							} else {
								if(i+1 >= stmdList.length()) return(new ReturnMsg(false,"Stmovd Inconsistant"));
								JSONObject jo3 = stmdList.getJSONObject(i+1);
//								if(i <= 0)  continue;
//								JSONObject jo3 = stmdList.getJSONObject(i-1);
								if(!jo3.getString("stmd_tdtype").equals("KI")) continue;
								kiRef4 = jo3.getString("stmd_ref4");
								kiQty = jo3.getDouble("stmd_qty");
							}
							if(
									kiRef4.equals(jo2.getString("stmd_ref4")) &&
									kiQty == jo2.getDouble("stmd_qty")
									) {
								continue;
							}
							if(ridx >= sr.getRowCount()) {
								col = sr.newRowCollection();								
								ReturnMsg rtn = sr.addSubRecord(col, ridx,"");
							} else {
								col = sr.getRowCollectionV(ridx);
							}
							col.getCell("stmd_tdtype").set("KO");
							col.getCell("stmd_irg").set(jo2.getInt("stmd_irg"));
							col.getCell("stmd_qty").set(jo2.getDouble("stmd_qty"));
							col.getCell("stmd_loc").set("CTR02");
							col.getCell("stmd_ref4").set(jo2.getString("stmd_ref4"));
							col.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(getBr().getSessionHelper(), p_bicol.getCellString("stm_cocode")));
							col.getCell("stmd_tdindex").set(tdindex);
							ridx++;
							if(ridx >= sr.getRowCount()) {
								col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, ridx,"");
							} else {
								col = sr.getRowCollectionV(ridx);
							}
							col.getCell("stmd_tdtype").set("KI");
							col.getCell("stmd_irg").set(jo2.getInt("stmd_irg"));
							col.getCell("stmd_qty").set(kiQty);
							col.getCell("stmd_loc").set("CTR02");
							col.getCell("stmd_ref4").set(kiRef4);
							col.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(getBr().getSessionHelper(), p_bicol.getCellString("stm_cocode")));
							col.getCell("stmd_tdindex").set(tdindex);
							ridx++;
							tdindex++;
						}
					}
					
					if(MoSync.equals("clerpslave")) {
						if(jo2.getString("stmd_tdtype").equals("KO")) {
							if(jo2.has("stmdki_loc")) {
								if(!jo2.getString("stmdki_loc").equals("CTR02")) continue;
							} else {
								if(i+1 >= stmdList.length()) return(new ReturnMsg(false,"Stmovd Inconsistant"));
								JSONObject jo3 = stmdList.getJSONObject(i+1);
//								if(i <= 0)  continue;
//								JSONObject jo3 = stmdList.getJSONObject(i-1);
								if(!jo3.getString("stmd_tdtype").equals("KI")) continue;
								if(!jo3.getString("stmd_loc").equals("CTR02")) continue;
							}
							if(ridx >= sr.getRowCount()) {
								col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, ridx,"");
							} else {
								col = sr.getRowCollectionV(ridx);
							}
							col.getCell("stmd_tdtype").set("JI");
							col.getCell("stmd_irg").set(jo2.getInt("stmd_irg"));
							col.getCell("stmd_qty").set(jo2.getDouble("stmd_qty"));
							col.getCell("stmd_loc").set("TST01");
							col.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(getBr().getSessionHelper(), p_bicol.getCellString("stm_cocode")));
							double avCost = CostCalculation.getWaCost(getBr().getSessionHelper(), col.getInt("stmd_irg"), col.getInt("stmd_org"), col.getCell("stmd_date").getDate());
							if(!Double.isNaN(avCost)) {
								col.getCell("stmd_uprice").set(avCost);
								col.getCell("stmd_exprice").set(avCost * col.getDouble("stmd_qty"));
								col.getCell("stmd_exprice1").set(avCost * col.getDouble("stmd_qty"));
							}
							ridx++;
							col.getCell("stmd_tdindex").set(tdindex);
							tdindex++;
						}
					}
				}
				if(sid <= 0) {
					if(ridx <= 0) return(null); // nothing to sync , skip add empty stmov trans;
				} else {
					for(int i=ridx;i<sr.getRowCount();i++) {
						Object o = sr.getTrStatObj(new Integer(i));
						sr.markDelete( o, true);
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
			return (ReturnMsg.defaultOk);
		}

		@Override
		protected ReturnMsg updateRec(CellCollection p_bicol) throws Exception {
			String MoSync = Erpv4Config.getString(getBr().getSessionHelper(),"MoSync");
			boolean needSync = false;
			if(MoSync == null || MoSync.equals("")) return(null);
			if(p_bicol.testCell("stm_syncagent") != null) {
				if(!p_bicol.getString("stm_syncagent").trim().equals("")) return(null);
			}
			if(MoSync.equals("clerpmaster")) {
				java.util.Date d = p_bicol.getCell("stm_date").getDate();
				if(!d.after(DateUtil.dateTimeStrToDate("2021/02/28"))) return(null);
				if(p_bicol.getString("stm_module").equals("sttfr") 
						&& p_bicol.getString("stm_toloc").equals("CTR02") ) {
					needSync = true;
				}
			}
			if(MoSync.equals("clerpslave")) {
				java.util.Date d = p_bicol.getCell("stm_date").getDate();
				if(!d.after(DateUtil.dateTimeStrToDate("2021/02/28"))) return(null);
				needSync = true;
			}
			if(needSync) {
				JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON((BiCellCollection) p_bicol);
				jo.put("stm_syncagent", getBr().getSessionHelper().getAgent());
				jo.put("stmdLink", ((BiResultStmov) getBr()).getStmdLinkName());
				ReturnMsg rtn = new ReturnMsg(true);
				rtn.setData(jo.toString());
				return (rtn);
			}
			return(ReturnMsg.defaultFail);
		}

		@Override
		protected String getRemoteHosts(CellCollection p_bicol) {
			String ClerpSyncHosts = Erpv4Config.getString(sessionHelper,"ClerpHosts" );
			JSONArray ja = new JSONArray();
			ja.put(ClerpSyncHosts);
			return(ja.toString());
		}
		@Override
		protected ReturnMsg afterSync(String p_jsonStr) {
			try {
				JSONObject jo = new JSONObject(p_jsonStr);
				int mrg = jo.getInt("stm_mrg");
				int synccnt = jo.getInt("stm_updcnt");
				SelectUtil su = getBr().getSelectUtil();
				su.executeUpdate("update stmov set stm_synccnt = "+synccnt+" where stm_mrg = " + mrg + " and stm_synccnt < " + synccnt,null);
				/*
				getBr().clearCondition();
				getBr().addCustomCondition("stm_mrg = " + mrg);
				getBr().query();
				ReturnMsg rtn = null;
				if(getBr().getRowCount() == 1) {
					getBr().loadOneRecV(0);
					getBr().fetchOneRecV(0);
					getBr().beginWork();
					rtn = getBr().lockRecordForUpdate();
					if(rtn != null && !rtn.getStatus()) {
						getBr().rollbackWork();
						return(new ReturnMsg(false,"Record In Use"));
					}
					int lastSync = getBr().getCellInt("stm_synccnt");
					if(lastSync < synccnt) {
						getBr().getCell("stm_synccnt").set(synccnt);
						rtn = getBr().updateCurrent();
						if(rtn == null || rtn.getStatus()) {
							getBr().commitWork();
						} else {
							getBr().rollbackWork();
							return(rtn);
							
						}
					}
				}
				*/
				return(ReturnMsg.defaultOk);
			} catch (Exception ex) {
				UniLog.log(ex);
//				getBr().rollbackWork();
				return(new ReturnMsg(false,ex.toString()));
			}
		}
		@Override
		protected String srcToDestColumn(String p_srcCol) {
			// TODO Auto-generated method stub
			return p_srcCol;
		}
	}	

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		super.setSessionHelper(p_sh);
		ArrayList<String> syncList = new ArrayList<String>();
		syncList.add("st_irg");
		syncList.add("st_icode");
		syncList.add("st_iname");
		syncList.add("st_einame");
		syncList.add("st_barcode");
		syncList.add("mt_tpname");
		syncList.add("stu_desc");
		syncList.add("st_remark");	
		addOneView(Erpv4Config.getStockViewId(p_sh),new Erpv4SyncHandler(Erpv4Config.getStockViewId(p_sh),syncList,syncList,true));
		/*
		clerpStockMovementHandler hdr = new clerpStockMovementHandler("erpv4.MoGeneric") ;
		addOneView("clinic.MoAdjustmentClinic",hdr);
		addOneView("clinic.MoTransferClinic",hdr);
		addOneView("erpv4.MoGeneric",hdr);
		*/
		addOneView("clinic.MoAdjustmentClinic",new clerpStockMovementHandler("erpv4.MoGeneric")) ;
		addOneView("clinic.MoTransferClinic",new clerpStockMovementHandler("erpv4.MoGeneric")) ;
		addOneView("clinic.MoPosClinic",new clerpStockMovementHandler("erpv4.MoGeneric")) ;
		addOneView("clinic.MoPurchaseClinic",new clerpStockMovementHandler("erpv4.MoGeneric")) ;
		addOneView("erpv4.MoGeneric",new clerpStockMovementHandler("erpv4.MoGeneric")) ;
		String MoSync = Erpv4Config.getString(p_sh,"MoSync");
		if(MoSync != null && MoSync.equals("clerpslave")) {
			addOneView("erpv4.StockTake",new clerpStockMovementHandler("erpv4.MoGeneric")) ;
		}
		RecSync.addRpcClass(p_sh.getAgent(),"com.uniinformation.erpv4.clinic.RecSyncClerpRpcServlet");
	}

}
