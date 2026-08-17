package com.uniinformation.erpv4.gbp;


import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncGbpRpcServlet implements RpcServlet{
	private static final ConcurrentHashMap<String, Long> dupCheckHM = new ConcurrentHashMap<String, Long>(); //scannerid, ts
	public static float dupCheckDur = 1000; //unit ms
	static {
		String newDupCheckDurStr = System.getProperty("RecSyncGbpRpcServlet.dupCheckDur");
		if (StringUtils.isNotBlank(newDupCheckDurStr)) {
			try {
				float newDupCheckDur = Float.parseFloat(newDupCheckDurStr);
				if (newDupCheckDur != dupCheckDur) {
					UniLog.log1("dupCheckDur=%.1f newDupCheckDur:%.1f", dupCheckDur, newDupCheckDur);
					dupCheckDur = newDupCheckDur;
				}
			}
			catch(Exception ex) {
				UniLog.log1("unable to update dupCheckDur. %s", ex.getMessage());
			}
		}
	}

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


	public String scanOneBox(String p_agent,String p_scannerId,String p_barcode)
	{
		SessionHelper sh = RecSync.getSessionHelperByAgent(p_agent);
		if (StringUtils.isBlank(p_scannerId)) {
			return("FAILInvalid scanner id");
		}
		if(sh == null) {
			return("FAILInvalid agent");
		}
		try {
			Long lastTS = dupCheckHM.getOrDefault(p_scannerId, 0L);
			Long newTS = System.currentTimeMillis();
			UniLog.log1("id:%s lastTS:%d newTS:%d dupCheckDur:%.1f", p_scannerId, lastTS, newTS, dupCheckDur);
			dupCheckHM.put(p_scannerId, newTS);
			if ((newTS - lastTS) < dupCheckDur) {
				return(String.format("FAILScan too fast(<%.1fs)\n\u6383\u63CF\u901F\u5EA6\u592A\u5FEB\n",(dupCheckDur/1000)));
			}
			
			BiView v = sh.getBiSchema().getViewByName("gbp.FgScan");
			BiResult br = v.newBiResult(sh.getLoginId(), null, null, sh);
			br.clearCurrentRec();
			br.getCell("fgm_barcode").sync(p_barcode);
			if(br.getCellInt("fgs_mrg") <= 0) return("FAILRecord not found\n\u627E\u4E0D\u5230\u8A18\u9304");
			br.getCell("fgs_scannerid").set(p_scannerId);
			br.getCell("fgs_login").set(sh.getLoginId());
			ReturnMsg rtn = br.addCurrent();
			if(rtn != null && !rtn.getStatus()) return("FAIL"+rtn.getMsg());
			return("OK  Record added\n\u6210\u529F\u6DFB\u52A0\u8A18\u9304");
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAILException Catched");
		}
	}
}
