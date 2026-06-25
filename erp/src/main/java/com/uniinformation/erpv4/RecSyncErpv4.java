package com.uniinformation.erpv4;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.rpccall.Value;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncErpv4 extends RecSyncBi {
	static class StockStatusRec {
		int irg;
		int org;
		double freestock;
		double onorder;
	};
	
//	@Override
//	public void clearRemoteHash() {
//		remoteRpcHash.clear();
//	}
	
	public class Erpv4SyncHandler extends BiSyncHandler 
	{
		List<String> updList;
		List<String> syncList;
		String destViewId;
		public Erpv4SyncHandler(String p_viewId,List<String>p_updateList,List<String> p_syncList,boolean p_allowAdd) {
//			destView = sessionHelper.getBiSchema().getViewByName(p_viewId);
//			destView = sessionHelper.getBiSchema().getViewByName(p_viewId);
			destViewId = p_viewId;
			updList = p_updateList;
			syncList = p_syncList;
			allowAdd = p_allowAdd;
		}
		
		@Override
		protected String getDestViewId() {
			// TODO Auto-generated method stub
//			return (destView.getName());
			return(destViewId);
		}
		@Override
		protected String getKey(CellCollection p_bicol) {
			// TODO Auto-generated method stub
//			BiTable mastertab = destView.getTable();
			BiView destView = br.getView();
			BiTable mastertab = br.getView().getTable();
			
			String pkeys[] = mastertab.getPrimaryKeys();
			if(pkeys == null || pkeys.length <= 0) return(null);
			String getKeyStr = "";
			for(int n=0;n<pkeys.length;n++) {
				String pkey = pkeys[n];
				getKeyStr += pkey + " = ";
				ColumnCell cc = (ColumnCell) p_bicol.getCell(pkey);
				switch(cc.getType()) {
				case Cell.VTYPE_INT:
					getKeyStr +=  cc.getInt();
					break;
				case Cell.VTYPE_DOUBLE:
					getKeyStr +=  cc.getDouble();
					break;
				case Cell.VTYPE_STRING:
					getKeyStr += "'";
					getKeyStr +=  cc.getString();
					getKeyStr += "'";
					break;
				case Cell.VTYPE_DATE:
					getKeyStr += "'";
					getKeyStr +=  DateUtil.toDateString(cc.getDate(), "yyyy/mm/dd");
					getKeyStr += "'";
					break;
				case Cell.VTYPE_BOOLEAN:
					getKeyStr += "'";
					getKeyStr +=  cc.getString();
					getKeyStr += "'";
					break;
//			case Cell.VTYPE_SET:
//			case Cell.VTYPE_DATETIME:
//			case Cell.VTYPE_NULL:
				default:
					return(null);
				}
			}
			return(getKeyStr);

//			String pkey = mastertab.getPrimaryKey();
//			if(pkey == null || pkey.equals("")) pkey = mastertab.getSerialId();
//			if(destView.getColumnByLabel(pkey) == null) {
//				return(null);
//			}
//			ColumnCell cc = (ColumnCell) p_bicol.getCell(pkey);
//			if(cc == null) return(null);
//			String getKeyStr = pkey + " = ";
//			switch(cc.getType()) {
//			case Cell.VTYPE_INT:
//					getKeyStr +=  cc.getInt();
//					break;
//			case Cell.VTYPE_DOUBLE:
//					getKeyStr +=  cc.getDouble();
//					break;
//			case Cell.VTYPE_STRING:
//					getKeyStr += "'";
//					getKeyStr +=  cc.getString();
//					getKeyStr += "'";
//					break;
//			case Cell.VTYPE_DATE:
//					getKeyStr += "'";
//					getKeyStr +=  DateUtil.toDateString(cc.getDate(), "yyyy/mm/dd");
//					getKeyStr += "'";
//					break;
//			case Cell.VTYPE_BOOLEAN:
//					getKeyStr += "'";
//					getKeyStr +=  cc.getString();
//					getKeyStr += "'";
//					break;
//			//		case Cell.VTYPE_SET:
//			//		case Cell.VTYPE_DATETIME:
//			//		case Cell.VTYPE_NULL:
//			default:
//					return(null);
//			}
//			return(getKeyStr);
		}

		@Override
		protected ReturnMsg syncRec(CellCollection p_bicol, JSONObject p_jo) {
			
			// TODO Auto-generated method stub
			try {
				Vector<BiColumn> cl = getBr().getColumns();
				for(BiColumn bc : cl) {
					if(syncList.indexOf(bc.getLabel()) < 0) continue;
					Cell cc = p_bicol.getCell(bc.getLabel());
					if(cc != null) {
						joToCell(p_jo,p_bicol,bc.getLabel());
					}
				}
				return(ReturnMsg.defaultOk);
			} catch (Exception ex) {
				//UniLog.log(ex); //andrew210119: too many exception log in clerp
				UniLog.log1("Exception:" + ex.getMessage());
				return(ReturnMsg.defaultFail);
			}	
		}


		@Override
		protected ReturnMsg updateRec(CellCollection p_bicol) throws Exception {
			JSONObject jo = new JSONObject();
			Vector<BiColumn> cl = br.getView().getColumns();
			for(BiColumn bc : cl) {
				if(updList.indexOf(bc.getLabel()) < 0) continue;
				Cell cc = p_bicol.getCell(bc.getLabel());
				if(cc != null) {
			switch(cc.getType()) {
			case Cell.VTYPE_INT:
					jo.put(srcToDestColumn(bc.getLabel()), cc.getInt());
					break;
			case Cell.VTYPE_DOUBLE:
					jo.put(srcToDestColumn(bc.getLabel()), cc.getDouble());
					break;
			case Cell.VTYPE_STRING:
					jo.put(srcToDestColumn(bc.getLabel()), cc.getString());
					break;
			case Cell.VTYPE_DATE:
					jo.put(srcToDestColumn(bc.getLabel()), DateUtil.toDateString(cc.getDate(), "yyyy/mm/dd"));
					break;
			case Cell.VTYPE_BOOLEAN:
					jo.put(srcToDestColumn(bc.getLabel()), cc.getBoolean());
					break;
			case Cell.VTYPE_DATETIME:
					jo.put(srcToDestColumn(bc.getLabel()), cc.getInt());
					break;
//			case Cell.VTYPE_SET:
//			case Cell.VTYPE_NULL:
			default:
			}
					
				}
			}
			ReturnMsg rtn = new ReturnMsg(true);
			rtn.setData(jo.toString());
			return (rtn);
		}

		@Override
		protected String getRemoteHosts(CellCollection p_bicol) {
			// TODO Auto-generated method stub
			String RecSyncHost = BiConfig.getString(sessionHelper,"RecSyncHost" );
			JSONArray ja = new JSONArray();
			ja.put(RecSyncHost);
			return(ja.toString());
		}

		@Override
		protected String srcToDestColumn(String p_srcCol) {
			// TODO Auto-generated method stub
			return p_srcCol;
		}
		
	}
	
	static public enum STOCKSTATUS {FREESTOCK,ONORDER};
	static public void erpv4ClearRemoteFreeStock(String p_localAgent,String p_remoteAgent,String p_costkey) throws Exception {
		RecSyncErpv4 rs = (RecSyncErpv4) agentHash.get(p_localAgent);
		if(rs != null) {
			RecSyncHost rh = rs.serverHash.get(p_remoteAgent);
			if(rh != null) {
				Hashtable<String,StockStatusRec> ht = (Hashtable<String,StockStatusRec>)rh.cacheHash.get("stockStatus");
				if(ht != null) {
					ht.remove(p_costkey);
					ht.remove(CostCalculation.getCostKeyIrgOnly(p_costkey));
				}
			}
		}
	}
	static public double erpv4GetRemoteFreeStock(String p_localAgent,String p_remoteAgent,int p_irg,int p_org,STOCKSTATUS p_type) throws Exception{
		RecSyncErpv4 rs = (RecSyncErpv4) agentHash.get(p_localAgent);
		if(rs == null) return(Double.NaN);
		RecSyncHost rh = rs.serverHash.get(p_remoteAgent);
		synchronized(rh) {
			Hashtable<String,StockStatusRec> ht = (Hashtable<String,StockStatusRec>)rh.cacheHash.get("stockStatus");
			if(ht == null) {
				ht = new Hashtable<String,StockStatusRec>();
				rh.cacheHash.put("stockStatus", ht);
			}
			StockStatusRec ssr = ht.get(CostCalculation.getCostKey(p_irg, p_org));
			if(ssr == null) {
				ssr = new StockStatusRec();
				ssr.irg = p_irg;
				ssr.org = p_org;
				Value v = rh.trpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.erpv4GetStockStatus",
						new VectorUtil()
						.addElement(p_remoteAgent)
						.addElement(p_irg)
						.addElement(p_org)
						.toVector());
				if(v == null || !v.toString().startsWith("OK")) return(Double.NaN);
				JSONObject jo = new JSONObject(v.toString().substring(4));
				ssr.freestock = jo.getDouble("freestock");
				ssr.onorder = jo.getDouble("onorder");
				ht.put(CostCalculation.getCostKey(p_irg, p_org),ssr);
			}
			switch(p_type) {
			case FREESTOCK : return(ssr.freestock);
			case ONORDER : return(ssr.onorder);
			default : return(0.0);
			}
		}
		
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		super.setSessionHelper(p_sh);
		RecSync.addRpcClass(p_sh.getAgent(),"com.uniinformation.erpv4.RecSyncErpv4RpcServlet");
	}
	public String getErpCostCache(int p_irg,int p_org) throws Exception {
		return (CostCalculation.getWaCostCache(sessionHelper, p_irg, p_org));
	}
	public String getErpStockStatus(int p_irg,int p_org) throws Exception {
		return (CostCalculation.getErpStockStatus(sessionHelper, p_irg, p_org));
	}

}
