package com.uniinformation.erpv4.afs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.RecSyncErpv4;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncAfs extends RecSyncErpv4 {
	class AfsSyncHandler extends Erpv4SyncHandler {

		public AfsSyncHandler(String p_viewId, List<String> p_updateList, List<String> p_syncList, boolean p_allowAdd) {
			super(p_viewId, p_updateList, p_syncList, p_allowAdd);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected String srcToDestColumn(String p_srcCol) {
			if(p_srcCol.equals("stbd_name")) return("stbd_cname");
			if(p_srcCol.equals("stbd_cname")) return("stbd_name");
			if(p_srcCol.equals("mt_tpname")) return("mt_tpcname");
			if(p_srcCol.equals("mt_tpcname")) return("mt_tpname");
			if(p_srcCol.equals("st_iname")) return("st_einame");
			if(p_srcCol.equals("st_einame")) return("st_iname");
			if(p_srcCol.equals("st_standardcur")) return("st_standardcostcur");
			if(p_srcCol.equals("st_price1")) return("st_standardcost");
			return(p_srcCol);
		}
		
	}
	class BalancePushHandler extends SyncHandler {
		
		boolean isMaster;
		public BalancePushHandler(boolean p_isMaster) {
			isMaster = p_isMaster;
		}

		@Override
		protected String getDestViewId() {
			// TODO Auto-generated method stub
			return("StockBalancePush");
		}

		@Override
		protected String getKey(CellCollection p_bicol) {
			// TODO Auto-generated method stub
			return(p_bicol.getString("costkey"));
		}

		@Override
		protected ReturnMsg syncRec(CellCollection p_bicol, JSONObject p_jo) {
			// TODO Auto-generated method stub
			try {
				RecSyncErpv4.erpv4ClearRemoteFreeStock(sessionHelper.getAgent(),p_jo.getString("agent"),p_jo.getString("costkey"));
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			return(ReturnMsg.defaultOk);
		}

		@Override
		protected ReturnMsg updateRec(CellCollection p_bicol) throws Exception {
			// TODO Auto-generated method stub
			if(!isMaster) return null;
			JSONObject jo = new JSONObject();
			jo.put("costkey", p_bicol.getString("costkey"));
			jo.put("agent", sessionHelper.getAgent());
			ReturnMsg rtn = new ReturnMsg(true);
			rtn.setData(jo.toString());
			return (rtn);
		}

		@Override
		protected String getRemoteHosts(CellCollection p_bicol) {
			// TODO Auto-generated method stub
			String RecSyncHost = Erpv4Config.getString(sessionHelper,"RecSyncHost" );
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
	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		super.setSessionHelper(p_sh);

//		ArrayList<String> hkList = new ArrayList<String>();
//		ArrayList<String> cnList = new ArrayList<String>();
//		hkList.add("st_irg");
//		hkList.add("st_icode");
//		hkList.add("st_mtype");
//		hkList.add("st_msubtype");
//		hkList.add("st_icode");
//		hkList.add("st_iname");
//		hkList.add("st_mbrand");
//		hkList.add("st_modelno");
//		hkList.add("st_oicode");
//		hkList.add("st_unit");
//		hkList.add("st_szrange");
//		hkList.add("st_dimensionl");
//		hkList.add("st_dimensionw");
//		hkList.add("st_dimensionh");
//		hkList.add("st_netweight");
//		hkList.add("st_grossweight");
//		hkList.add("st_packd");
//		hkList.add("st_packw");
//		hkList.add("st_packh");
//		hkList.add("st_remark");
//		
//		cnList.add("st_einame");
//		
//		if(p_sh.getAgent().equals("afs") || p_sh.getAgent().equals("afsdev") || p_sh.getAgent().equals("afshk") ) {
//			addOneView("AfsStock",new Erpv4SyncHandler("AfsStock",hkList,cnList,false));
//		} else {
//			addOneView("AfsStock",new Erpv4SyncHandler("AfsStock",cnList,hkList,true));
//		}
		
		if(
				p_sh.getAgent().equals("afshk") ||
				p_sh.getAgent().equals("afstbdev")
				) {
			UniLog.log("Setup Afs Master RecSync for Agent " + p_sh.getAgent());
			addOneView("AfsStockBrand",new AfsSyncHandler("erpv4.StockBrand",
						new VectorUtil()
						.addElement("stbd_type")
						.addElement("stbd_code")
						.addElement("stbd_name")
						.addElement("stbd_cname")
						.addElement("stbd_origin")
						.toVector()
						,
						new VectorUtil()
						.addElement("stbd_type")
						.addElement("stbd_code")
						.addElement("stbd_name")
						.addElement("stbd_cname")
						.addElement("stbd_origin")
						.toVector()
						,true));
			addOneView("AfsStockCat",new AfsSyncHandler("erpv4.McType",
						new VectorUtil()
						.addElement("mt_tpcode")
						.addElement("mt_tptype")
						.addElement("mt_tpscode")
						.addElement("mt_tpname")
						.addElement("mt_tpcname")
						.toVector()
						,
						new VectorUtil()
						.addElement("mt_tpcode")
						.addElement("mt_tptype")
						.addElement("mt_tpscode")
						.addElement("mt_tpname")
						.addElement("mt_tpcname")
						.toVector()
						,true));

			addOneView("AfsStock",new AfsSyncHandler("erpv4.Stock",
						new VectorUtil()
						.addElement("st_irg")
						.addElement("st_icode")
						.addElement("st_iname")
						.addElement("st_einame")
						.addElement("st_mtype")
						.addElement("st_mbrand")
						.addElement("st_oicode")
						.addElement("st_modelno")
						.addElement("st_unit")
						.addElement("st_msubtype")
						.addElement("st_price1")
						.addElement("st_standardcur")
						.toVector()
						,
						new VectorUtil()
						.addElement("st_irg")
						.addElement("st_icode")
						.addElement("st_iname")
						.addElement("st_einame")
						.addElement("st_mtype")
						.addElement("st_mbrand")
						.addElement("st_oicode")
						.addElement("st_modelno")
						.addElement("st_unit")
						.addElement("st_msubtype")
						.toVector()
						,true));
//			addOneView("AfsStock",new Erpv4SyncHandler("AfsStock",null,null,true));
		}
		if(
				p_sh.getAgent().equals("erpv4afscn")
				) {
			UniLog.log("Setup Afs Slave RecSync for Agent " + p_sh.getAgent());
			addOneView("erpv4.StockBrand",new AfsSyncHandler("AfsStockBrand",
						new VectorUtil()
						.addElement("stbd_type")
						.addElement("stbd_code")
						.addElement("stbd_name")
						.addElement("stbd_cname")
						.addElement("stbd_origin")
						.toVector()
						,
						new VectorUtil()
						.addElement("stbd_type")
						.addElement("stbd_code")
						.addElement("stbd_name")
						.addElement("stbd_cname")
						.addElement("stbd_origin")
						.toVector()
						,true));
			addOneView("erpv4.McType",new AfsSyncHandler("AfsStockCat",
						new VectorUtil()
						.addElement("mt_tpcode")
						.addElement("mt_tptype")
						.addElement("mt_tpscode")
						.addElement("mt_tpname")
						.addElement("mt_tpcname")
						.toVector()
						,
						new VectorUtil()
						.addElement("mt_tpcode")
						.addElement("mt_tptype")
						.addElement("mt_tpscode")
						.addElement("mt_tpname")
						.addElement("mt_tpcname")
						.toVector()
						,true));
			addOneView("erpv4.Stock",new AfsSyncHandler("AfsStock",
						new VectorUtil()
						.addElement("st_irg")
						.addElement("st_icode")
						.addElement("st_iname")
						.addElement("st_einame")
						.addElement("st_mtype")
						.addElement("st_mbrand")
						.addElement("st_oicode")
						.addElement("st_modelno")
						.addElement("st_unit")
						.addElement("st_msubtype")
						.toVector()
						,
						new VectorUtil()
						.addElement("st_irg")
						.addElement("st_icode")
						.addElement("st_iname")
						.addElement("st_einame")
						.addElement("st_mtype")
						.addElement("st_mbrand")
						.addElement("st_oicode")
						.addElement("st_modelno")
						.addElement("st_unit")
						.addElement("st_msubtype")
						.addElement("st_standardcost")
						.addElement("st_standardcostcur")
						.toVector()
						,true));
//			addOneView("AfsStock",new Erpv4SyncHandler("AfsStock",null,null,true));
		}
		
		
//		hkList = new ArrayList<String>();
//		cnList = new ArrayList<String>();
//		hkList.add("stbd_type");
//		hkList.add("stbd_code");
//		hkList.add("stbd_name");
//		hkList.add("stbd_origin");
//		cnList.add("stbd_cname");
//		if(p_sh.getAgent().equals("afs") || p_sh.getAgent().equals("afsdev") ) {
//			addOneView("AfsStockBrand",new Erpv4SyncHandler("AfsStockBrand",hkList,cnList,false));
//		} else {
//			addOneView("AfsStockBrand",new Erpv4SyncHandler("AfsStockBrand",cnList,hkList,true));
//		}
//		if(p_sh.getAgent().equals("afs") || p_sh.getAgent().equals("afsdev") || p_sh.getAgent().equals("afshk") ) {
//			addOneView("StockBalancePush",new BalancePushHandler(true));
//		} else {
//			addOneView("StockBalancePush",new BalancePushHandler(false));
//		}
	}
}
