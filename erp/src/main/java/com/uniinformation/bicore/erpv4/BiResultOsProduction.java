package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultOsProduction extends BiResult {

	public BiResultOsProduction(BiResult p_parent, BiView p_view,
			SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value val = rpc.callSegment(
				"erpv4GenbucketBegin", new Vector()
				);	
		if(val == null || !val.toString().startsWith("OK")) {
			return(new ReturnMsg(false,"genbucket begin failed"));
		}
		return(null);
	}
	@Override 
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean p_isUpdate) {
		ReturnMsg rtnMsg = super.biAfterAddUpdateCurrent(col,p_isUpdate);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketAdd(col,1.0);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
			RpcClient rpc = getSelectUtil().getRpcClient();
	  		Value val = 
				rpc.callSegment(
						"erpv4GenbucketCommit", new Vector()
					);
				if(val != null && val.toString().startsWith("OK")) {
					String s = StringUtil.strpart(val.toString(), 4 , -1);
					if(!s.trim().equals("")) {
						return(GenbucketUtil.qoGenBucketCheckResult(s,sh));
					} else return(null);
				} 
			if(val == null) {
				return(new ReturnMsg(false, "Update Failed : Unknown Reason",true));
			} else {
				return(new ReturnMsg(false, "Update Failed : " + val.toString().substring(4),true));
			}
	}
	ReturnMsg genbucketAdd(CellCollection pcol,double p_factor) {
		RpcClient rpc = getSelectUtil().getRpcClient();
   		Vector args = new Vector();
		args.add(pcol.getCell("wipd_qorg").getInt());
		args.add(pcol.getCell("wipd_qirg").getInt());
		args.add(pcol.getCell("wipd_irg").getInt());
		args.add(pcol.getCell("wipd_org").getInt());
		args.add(p_factor);
		Value v = rpc.callSegment( "erpv4WipDetailAdd", args);
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
		return(new ReturnMsg(true));
	}
	
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(genbucketAdd(pcol,-1));
	}
}
