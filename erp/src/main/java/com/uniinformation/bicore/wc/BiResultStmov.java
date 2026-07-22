package com.uniinformation.bicore.wc;

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
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStmov extends BiResult {

	public BiResultStmov(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(getCellString("stm_void").equals("Y")) return(rtnMsg);
		rtnMsg = genbucketBegin();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketAdd(col.getCell("stm_mrg").getInt(),-1.0);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketCommit();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			su.executeUpdate("delete from stmovd",
					new Wherecl().andUniop("stmd_mrg", "=",col.getCell("stm_mrg").getObject()).stripAnd()
			);
			return(null);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false, ex.toString()));
		}
	}
	
	ReturnMsg genbucketBegin() {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment( "erpv3GenbucketBegin", new Vector());
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
		return(new ReturnMsg(true));
	}
	ReturnMsg genbucketCommit() {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment( "erpv3GenbucketCommit", new Vector());
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
  		String s = StringUtil.strpart(v.toString(), 4 , -1);
 		if(!s.trim().equals("")) {
  			return(GenbucketUtil.qoGenBucketCheckResult(s,sh));
  		} else return(new ReturnMsg(true));
	}
	ReturnMsg genbucketAdd(int p_mrg,double p_factor) {
		RpcClient rpc = getSelectUtil().getRpcClient();
   		Vector args = new Vector();
		args.add(p_mrg);
		args.add(p_factor);
		Value v = rpc.callSegment( "erpv3StmovAdd", args);
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(getCellString("stm_void").equals("Y")) return(rtnMsg);
		rtnMsg = genbucketBegin();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(getCellString("stm_void").equals("Y")) return(rtnMsg);
		rtnMsg = genbucketBegin();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketAdd(pcol.getCell("stm_mrg").getInt(),-1.0);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean p_isUpdate) {
		ReturnMsg rtnMsg = super.biAfterAddUpdateCurrent(col,p_isUpdate);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(getCellString("stm_void").equals("Y")) return(rtnMsg);
		rtnMsg = genbucketAdd(col.getCell("stm_mrg").getInt(),1.0);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketCommit();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
}
