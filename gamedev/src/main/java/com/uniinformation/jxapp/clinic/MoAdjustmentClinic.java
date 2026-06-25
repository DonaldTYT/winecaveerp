package com.uniinformation.jxapp.clinic;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBase;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4.MoPos;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiMsgbox;

public class MoAdjustmentClinic extends MoPos {
	
	void copyFromRemoteHost(BiResult br) {
	String rHost = Erpv4Config.getString(getSessionHelper(), "ClerpHosts");
	try {
	RpcClient rpc = RecSync.openRpc(getSessionHelper().getAgent(), rHost, 5000);
	if(rpc != null)	{
		Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.ping");
//		v = rpc.callSegment("com.uniinformation.erpv4.clinic.RecSyncClerpRpcServlet.ping");
		v = rpc.callSegment("com.uniinformation.erpv4.clinic.RecSyncClerpRpcServlet.getOutstandingBalanceByLocation",
				new VectorUtil()
				.addElement(rHost)
				.addElement(br.getCellString("stm_toloc"))
				.toVector()
					);
		if(v.toString().startsWith("OK")) {
			BiResult sr = br.getSubLink("clinic.DrugDetail");
			JSONObject jo = new JSONObject(v.toString().substring(4));
			JSONArray ja = jo.getJSONArray("recordList");
			JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
			for(int i=0;i<ja.length();i++) {
				JSONObject jr = ja.getJSONObject(i);
				double qty = jr.getDouble("stsn_nqty");
				if(qty > 0) {
					BiCellCollection col = sr.newRowCollection();

					col.getCell("stmd_tdtype").set("JI");
					col.getCell("stmd_irg").set(jr.getInt("stsn_irg"));
					col.getCell("stmd_org").set(jr.getInt("stsn_org"));
					col.getCell("stmd_ref4").set(jr.getString("stsn_ref4"));
					col.getCell("stmd_entryqty").set(qty);
					if(jr.has("st_avcost")) {
						col.getCell("stmd_uprice").set(jr.getDouble("st_avcost"));
					}
					ReturnMsg rtn = sr.addSubRecord(col, -1,"");
					Object tr = rtn.getData();
					int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
					sv.addItemToList(tr, rowIdx);
				}
			}
			((BiResultStmov) br).calAmount();
		}
		rpc.close();
	}
	} catch (Exception ex) {
		UniLog.log(ex);
		ZkBiMsgbox.show("Cannot Connect to Remote Site, please check netowrk");
	}
	}
	
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		if(mode == JxZkBiBase.MODE_ADD) {
			if(zkcb.getExtraInfo() == null) copyFromRemoteHost(br);
		}
	}
	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		try {
			br.beginWork();
		} catch(Exception ex) {
			UniLog.log(ex);
		}
		return(super.beforeAdd(br));
	}
	
	ReturnMsg syncAddToRemote(BiResult br) {
		String rHost = Erpv4Config.getString(getSessionHelper(), "ClerpHosts");
		String rLoc  = Erpv4Config.getString(getSessionHelper(), "ClerpLocs");
//		if(br.getCellString("stm_fromloc").equals(rLoc)) {

		UniLog.log("MoAdjustment AfterAdd 1["+br.getCellString("stm_ref3")+"]["+br.getCellString("stm_fromloc")+"]["+rLoc+"]");
		if(br.getCellString("stm_ref3").isEmpty() && br.getCellString("stm_fromloc").equals(rLoc)) {
			UniLog.log("MoAdjustment Calling Remote");
			try {
			RpcClient rpc = RecSync.openRpc(getSessionHelper().getAgent(), rHost, 5000);
			if(rpc != null)	{
				Value v;
				JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection());
				v = rpc.callSegment("com.uniinformation.erpv4.clinic.RecSyncClerpRpcServlet.createCounterAdjustment",
						new VectorUtil()
						.addElement(rHost)
						.addElement(jo.toString())
						.toVector()
							);
				if(v == null || !v.toString().startsWith("OK")) {
					ReturnMsg msg = new ReturnMsg(false,"Error : failed to sync to remote host");
					msg.setFatal(true);
					return(msg);
				}
				rpc.close();
			}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"Cannot Connect to Remote Site, please check netowrk"));
			}
		}	
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	protected ReturnMsg afterAdd(BiResult br)
	{
//		if(zkcb.getExtraInfo() == null) return(syncAddToRemote(br));
		return(ReturnMsg.defaultOk);
	}
}
