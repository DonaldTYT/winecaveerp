package com.uniinformation.jxapp.clinic;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.erpv4.MoPos;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiMsgbox;

public class MoTransferClinic extends MoPos {
	@Override
	public void afterBind() {
		super.afterBind();
		detViewId = "erpv4.MoDetPosTfr";
//		toLoc = "LNTST";
		new JxFieldAction("btReceiveCtr") {
			public void actionPerformed(JxField fd){
				copyFromRemoteHost(getBr());
			}
		};
		
	}

	void copyFromRemoteHost(BiResult br) {
	String rHost = Erpv4Config.getString(getSessionHelper(), "ClerpHosts");
	try {
	RpcClient rpc = RecSync.openRpc(getSessionHelper().getAgent(), rHost, 5000);
	if(rpc != null)	{
		br.getCell("stm_toloc").set("CTR02");
		Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.ping");
//		v = rpc.callSegment("com.uniinformation.erpv4.clinic.RecSyncClerpRpcServlet.ping");
		v = rpc.callSegment("com.uniinformation.erpv4.clinic.RecSyncClerpRpcServlet.getOutstandingBalanceByLocation",
				new VectorUtil()
				.addElement(rHost)
				.addElement("TST01")
				.toVector()
					);
		if(v.toString().startsWith("OK")) {
			BiResult sr = br.getSubLink("erpv4.MoDetPosTfr");
			JSONObject jo = new JSONObject(v.toString().substring(4));
			JSONArray ja = jo.getJSONArray("recordList");
			JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
			for(int i=0;i<ja.length();i++) {
				JSONObject jr = ja.getJSONObject(i);
				double qty = jr.getDouble("stsn_nqty");
				if(qty > 0) {
					BiCellCollection col = sr.newRowCollection();

					col.getCell("stmd_tdtype").set("KO");
					col.getCell("stmd_irg").set(jr.getInt("stsn_irg"));
					col.getCell("stmd_nref4").set(1);
//					col.getCell("stmd_org").set(jr.getInt("stsn_org"));
					col.getCell("stmd_ref4").set(jr.getString("stsn_ref4"));
					col.getCell("stmd_entryqty").set(qty);
					ReturnMsg rtn = sr.addSubRecord(col, -1,"");
					Object tr = rtn.getData();
					int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
					sv.addItemToList(tr, rowIdx);
				}
			}
		}
		rpc.close();
	}
	} catch (Exception ex) {
		UniLog.log(ex);
		ZkBiMsgbox.show("Cannot Connect to Remote Site, please check netowrk");
	}
	}
	
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		String MoSync = Erpv4Config.getString(getSessionHelper(), "MoSync");
		if(MoSync != null && MoSync.equals("clerpmaster")) {
			jxSetVisible("btReceiveCtr",true);
		} else {
			jxSetVisible("btReceiveCtr",false);
		}
	}
}
