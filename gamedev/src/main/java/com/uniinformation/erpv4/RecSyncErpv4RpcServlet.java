package com.uniinformation.erpv4;

import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncErpv4RpcServlet implements RpcServlet {

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
		return null;
	}
	public String getBiCollection(String p_agent,String p_view,String p_condition) {
		RecSync recsync = RecSync.getRecSync(p_agent);
		if(recsync == null) return("FAILNo Such Agent");
		synchronized(recsync) {
			SessionHelper sh = recsync.sessionHelper;
			BiView v = sh.getBiSchema().getViewByName(p_view);
			BiResult br = v.newBiResult(sh.getLoginId(), null, null, sh);
			br.addCustomCondition(p_condition);
			br.query();
			if(br.getRowCount() >= 1) {
				try {
					br.fetchOneRecV(0);
					JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection());
					return("OK  "+jo.toString());
				} catch (Exception ex) {
					UniLog.log(ex);
					return("FAILException Catched");
				}
			} else {
				return("FAILRecord Not Found");
			}
		}
	}
	
	public String resyncStmov(String p_agent,int p_mrg)
	{
		SessionHelper sh = RecSync.getSessionHelperByAgent(p_agent);
		if(sh == null) return("FAIL001");
		BiView v = sh.getBiSchema().getViewByName("erpv4.MoGeneric");
		BiResult br = v.newBiResult(sh.getLoginId(), null, null, sh);
		br.addCustomCondition("stm_mrg = " + p_mrg);
		br.query();
		if(br.getRowCount() != 1) return("FAIL");
		boolean ok = br.fetchOneRecV(0);
		if(!ok) return("FAIL");
		ReturnMsg rtn = br.updateCurrent();
		if(rtn != null && !rtn.getStatus()) return("FAIL"+rtn.getMsg());
		return("OK  ");
	}
}
