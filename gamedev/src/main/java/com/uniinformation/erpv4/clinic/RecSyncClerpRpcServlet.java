package com.uniinformation.erpv4.clinic;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncClerpRpcServlet implements RpcServlet{

	@Override
	public void init_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConnection(RpcServerConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
		return ("OK");
	}

//	public String resyncStmov(String p_agent,int p_mrg)
//	{
//		SessionHelper sh = RecSync.getSessionHelperByAgent(p_agent);
//		if(sh == null) return("FAIL001");
//		BiView v = sh.getBiSchema().getViewByName("erpv4.MoGeneric");
//		BiResult br = v.newBiResult(sh.getLoginId(), null, null, sh);
//		br.addCustomCondition("stm_mrg = " + p_mrg);
//		br.query();
//		if(br.getRowCount() != 1) return("FAIL");
//		boolean ok = br.fetchOneRecV(0);
//		if(!ok) return("FAIL");
//		ReturnMsg rtn = br.updateCurrent();
//		if(rtn != null && !rtn.getStatus()) return("FAIL"+rtn.getMsg());
//		return("OK  ");
//	}
	
	public String getOutstandingBalanceByLocation(String p_agent,String p_loc)
	{
		SessionHelper sh = RecSync.getSessionHelperByAgent(p_agent);
		if(sh == null) return("FAIL001");
		BiView v = sh.getBiSchema().getViewByName("erpv4.StockList");
		BiResult br = v.newBiResult(sh.getLoginId(), null, null, sh);
		br.addCustomCondition("stsn_loc = '"+p_loc+"' and stsn_nqty <> 0");
		br.query();
		JSONObject jo;
		try {
			jo = BiResult.resultToJson(br);
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAIL");
		}
		return("OK  "+jo.toString());
	}
	public String createCounterAdjustment(String p_agent,String p_json)
	{
		SessionHelper sh = RecSync.getSessionHelperByAgent(p_agent);
		if(sh == null) return("FAIL001");
		BiView v = sh.getBiSchema().getViewByName("clinic.MoAdjustmentClinic");
		BiResult br = v.newBiResult(sh.getLoginId(), null, null, sh);
		JSONObject jo;
		try {
			jo = new JSONObject(p_json);
			br.clearCurrentRec();
			java.util.Date d = DateUtil.dateTimeStrToDate(jo.getString("stm_date"));
			br.getCell("stm_date").set(d);
			br.getCell("stm_ref3").set(jo.getString("stm_ref1"));
			br.getCell("stm_nref1").set(DateUtil.dateToUnixtime(new java.util.Date()));
			br.getCell("stm_status").set(jo.getString("stm_status"));
			br.getCell("stm_cuser").set("hlv");
			br.getCell("stm_cdate").set(DateUtil.today());
			br.getCell("stm_fromloc").set(jo.getString("stm_fromloc"));
			br.getCell("stm_toloc").set(jo.getString("stm_toloc"));
			BiResult sr = br.getSubLink("clinic.DrugDetail");
			JSONArray ja = jo.getJSONArray(sr.getView().getName());
			for(int i=0;i<ja.length();i++) {
				JSONObject jd = ja.getJSONObject(i);
				BiCellCollection bl = sr.newRowCollection();
				/*
				if(jd.getString("stmd_tdtype").equals("JI")) bl.getCell("stmd_tdtype").set("JO");
				if(jd.getString("stmd_tdtype").equals("JO")) bl.getCell("stmd_tdtype").set("JI");
				*/
				bl.getCell("stmd_tdtype").set( jd.getString("stmd_tdtype"));
				bl.getCell("stmd_irg").set(jd.getInt("stmd_irg"));
				bl.getCell("stmd_org").set(jd.getInt("stmd_org"));
				bl.getCell("stmd_entryqty").set(-jd.getDouble("stmd_entryqty"));
				bl.getCell("stmd_qty").set(-jd.getDouble("stmd_qty"));
				bl.getCell("stmd_ref4").set(jd.getString("stmd_ref4"));
				bl.getCell("stmd_uprice").set(jd.getDouble("stmd_uprice"));
				sr.addSubRecord(bl, -1,"");
			}
			ReturnMsg rtn = br.addCurrent();
			if(rtn == null || rtn.getStatus())  {
				return("OK  "+br.getCellString("stm_ref1"));
			} else {
				return("FAIL"+rtn.getMsg());
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAIL");
		}
	}
	
	public String verifyStmov() {
		return("OK  ");
	}
}
