package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMStock extends BiResult {

	public BiResultMStock(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
//	//andrew200108: take photo/upload does not trigger biAfterAddUpdateCurrent
//	protected ReturnMsg biAfterAddUpdateCurrent(CellCollection col,boolean isUpdate) {
//		ReturnMsg rtnMsg = super.biAfterAddUpdateCurrent(col, isUpdate);
//		if(!rtnMsg.getStatus()) return(rtnMsg);
//		RpcClient rpc = getSelectUtil().getRpcClient();
//		rpc.callSegment("updateStExtraImg", 
//					new VectorUtil()
//						.addElement(col.getInt("st_irg"))
//						.toVector()
//				);
//		return(ReturnMsg.defaultOk);
//	}
//	//andrew200108: take photo/upload does not trigger biAfterAddUpdateCurrent
//	protected ReturnMsg biBeforeAddUpdateCurrent(CellCollection col,boolean isUpdate) {
//		ReturnMsg rtnMsg = super.biBeforeAddUpdateCurrent(col, isUpdate);
//		if(!rtnMsg.getStatus()) return(rtnMsg);
//		String wc = Erpv4Config.getString(getSessionHelper(), "WINEAC");
//		if(wc != null && wc.equals("Y")) {
//			String ss = getCellString("st_mbrand");
//			if(ss.trim().equals("")) {
//				try {
//				BiResult sr = getView().getSchema().getViewByName("wc.StBrand").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
//				sr.clearCurrentRec();
//				sr.getCell("stbd_type").set("W");
//				sr.getCell("stbd_name").set(getCellString("stbd_name"));
//				sr.getCell("stbd_origin").set("UN");
//				sr.addCurrent();
//				getCell("st_mbrand").set(sr.getCellString("stbd_code"));
//				} catch (Exception ex) {
//					UniLog.log(ex);
//					return(ReturnMsg.defaultFail);
//				}
//			}
//		}
//		return(ReturnMsg.defaultOk);
//	}
}
