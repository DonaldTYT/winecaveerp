package com.uniinformation.bicore.wc;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockIn extends BiResultStmov {

	public BiResultStockIn(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
//	@Override
//	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
//		ReturnMsg rtnMsg = null;
//		RpcClient rpc = getSelectUtil().getRpcClient();
//		
//		Vector<BiCellCollection> stmdcols = getSubLink("wc.StmdMi").getRowCollectionList();
//   		Vector args ;
//   		try {
//		for(BiCellCollection col : stmdcols) {
//			args = new Vector();
//			args.add(col.getCellString("stmd_ocode"));
//			args.add(col.getCellString("stmd_cocode"));
//			args.add(col.getCellInt("stmd_mrg"));
//			args.add(col.getCellString("stmd_vcode"));
//			args.add(col.getCell("stmd_date").getDate());
//			if(col.getCellInt("stmd_org") == 0) {
//				Value v = rpc.callSegment( "new_check_or_create_porecord_bypocode", args);
//				col.getCell("stmd_org").set(v.toInt());
//			}
//		}
//   		} catch (Exception ex) {
//   			UniLog.log(ex);
//   			return(new ReturnMsg(false,"Fail to get org",true));
//   		}
//		
//   		args = new Vector();
//		args.add("smimvh");
//		args.add(pcol.getCell("stm_date").getDate());
//		Value v = rpc.callSegment( "erpv3GetrgByControl", args);
//		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
//		try {
//			pcol.getCell("stm_ref1").set(v.toString().substring(4).trim());
//		} catch(CellException ex) {
//			UniLog.log(ex);
//			return(new ReturnMsg(false,"Unknown Error",true));
//		}
//		rtnMsg = super.biBeforeAddCurrent(pcol);
//		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
//		return(new ReturnMsg(true));
//	}
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection pcol,boolean isUpdate) {
		ReturnMsg rtnMsg = null;
		RpcClient rpc = getSelectUtil().getRpcClient();
		
		Vector<BiCellCollection> stmdcols = getSubLink("wc.StmdMi").getRowCollectionList();
   		Vector args ;
   		try {
		for(BiCellCollection col : stmdcols) {
			args = new Vector();
			args.add(col.getCellString("stmd_ocode"));
			args.add(col.getCellString("stmd_cocode"));
			args.add(col.getCellInt("stmd_mrg"));
			args.add(col.getCellString("stmd_vcode"));
			args.add(col.getCell("stmd_date").getDate());
			if(col.getCellInt("stmd_org") == 0) {
				Value v = rpc.callSegment( "erpv3_check_or_create_porecord_bypocode", args);
				int cc = v.toInt();
				if(cc <= 0) {
					return(new ReturnMsg(false,"Fail to get org",true));
				}
				col.getCell("stmd_org").set(cc);
			}
		}
   		} catch (Exception ex) {
   			UniLog.log(ex);
   			return(new ReturnMsg(false,"Fail to get org",true));
   		}
		
   		args = new Vector();
		args.add("smimvh");
		args.add(pcol.getCell("stm_date").getDate());
		Value v = rpc.callSegment( "erpv3GetrgByControl", args);
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
		try {
			pcol.getCell("stm_ref1").set(v.toString().substring(4).trim());
		} catch(CellException ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Unknown Error",true));
		}
		rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
}
