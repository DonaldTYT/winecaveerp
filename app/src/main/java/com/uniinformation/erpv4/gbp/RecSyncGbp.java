package com.uniinformation.erpv4.gbp;

import java.util.ArrayList;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.erpv4.RecSyncErpv4;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class RecSyncGbp extends RecSyncErpv4 {
	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		super.setSessionHelper(p_sh);
		ArrayList<String> syncList = new ArrayList<String>();
		ArrayList<String> nullList = new ArrayList<String>();

		syncList.add("fgm_rg");
		syncList.add("fgm_wocode");
		syncList.add("fgm_edition");
		syncList.add("fgm_prdqty");
		syncList.add("fgm_innerbox");
		syncList.add("fgm_expfintime");
		syncList.add("fgm_actfintime");
		syncList.add("fgm_barcode");
		syncList.add("fgm_finqty");
		syncList.add("fgm_remark");
		syncList.add("fgm_qtyperbox");
		syncList.add("fgm_finbox");
		RecSync.addRpcClass(p_sh.getAgent(),"com.uniinformation.erpv4.gbp.RecSyncGbpRpcServlet");
		if(p_sh.getAgent().equals("erpv4gbp")) {
			addOneView("gbp.FgMaster",new Erpv4SyncHandler("gbp.FgMaster",syncList,nullList,false));
		}  else {
			addOneView("gbp.FgMaster",new Erpv4SyncHandler("gbp.FgMaster",nullList,syncList,true));
		}
	}

}
