package com.uniinformation.erpv4;

import java.io.ByteArrayOutputStream;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncRpcServlet implements RpcServlet {
	RpcServerConnection rpcConn;
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
		rpcConn = conn;
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
		UniLog.log("RecSync Rpccall Ping");
		return ("OK");
	}
	
	public String syncOneRecord(String p_agent,String p_view,String p_key,String p_jsonDetail) {
		RecSync recsync = RecSync.getRecSync(p_agent);
		if(recsync == null) return("FAIL");
		synchronized(recsync) {
			ReturnMsg rtn = recsync.syncOneRecord(p_view,p_key,p_jsonDetail);
			if(rtn != null && rtn.getStatus()) return("OK");
			return("FAIL"+ rtn == null ? "" : rtn.getMsg());
		}
	}
	public String syncOneRecordByFile(String p_agent,String p_view,String p_key,String p_jsonFile) {
		String jsonStr = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			rpcConn.getFile(out, -1, p_jsonFile, 0, 0);
			out.close();
			jsonStr = out.toString();
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAILFile Read Error");
		};
		RecSync recsync = RecSync.getRecSync(p_agent);
		if(recsync == null) return("FAIL");
		synchronized(recsync) {
			ReturnMsg rtn = recsync.syncOneRecord(p_view,p_key,jsonStr);
			if(rtn != null && rtn.getStatus()) return("OK");
			return("FAIL"+ rtn == null ? "" : rtn.getMsg());
		}
	}

	public String erpv4GetStockAverageCost(String p_agent,String p_irg,String p_org) {
		int irg = Integer.parseInt(p_irg);
		int org = Integer.parseInt(p_org);
		return(erpv4GetStockAverageCost(p_agent,irg,org));
	}
	public String erpv4GetStockAverageCost(String p_agent,int p_irg,int p_org) {
		RecSync recsync = RecSync.getRecSync(p_agent);
		if(recsync == null) return("FAIL");
		synchronized(recsync) {
			try {
				return("OK  "+ getErpCostCache(recsync.getSessionHelper(),p_irg, p_org));
			} catch (Exception ex) {
				return("FAIL"+ex.toString());
			}
		}
	}
	public String erpv4GetStockStatus(String p_agent,int p_irg,int p_org) {
		RecSync recsync = RecSync.getRecSync(p_agent);
		if(recsync == null) return("FAIL");
		synchronized(recsync) {
			try {
				return("OK  "+ getErpStockStatus(recsync.getSessionHelper(),p_irg,p_org));
			} catch (Exception ex) {
				return("FAIL"+ex.toString());
			}
		}
	}
	
	public String getErpCostCache(SessionHelper p_sh,int p_irg,int p_org) throws Exception {
		return (CostCalculation.getWaCostCache(p_sh, p_irg, p_org));
	}
	public String getErpStockStatus(SessionHelper p_sh,int p_irg,int p_org) throws Exception {
		return (CostCalculation.getErpStockStatus(p_sh, p_irg, p_org));
	}
}
