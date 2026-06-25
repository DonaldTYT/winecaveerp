package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.afs.BiResultAfsStockSet.BiCellAction_mt_tpname;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsServiceUnit extends BiResult {
	public BiResultAfsServiceUnit(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList,p_whereStr,p_sh);
		UniLog.log("BiResultAfsStockSet Used");
	}
	ReturnMsg genbucketBegin() {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment( "erpv4GenbucketBegin", new Vector());
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
		return(new ReturnMsg(true));
	}
	ReturnMsg genbucketCommit() {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment( "erpv4GenbucketCommit", new Vector());
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
		Value v = rpc.callSegment( "erpv4AddSvmcGenBucket", args);
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
		return(new ReturnMsg(true));
	}	
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketBegin();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketAdd(pcol.getCell("svmc_rg").getInt(),-1.0);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketCommit();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			su.executeUpdate("delete from sv_machine",
					new Wherecl().andUniop("svmc_mrg", "=",pcol.getCell("svmc_mrg").getObject())
						.appendString(" and svmc_mrg <> svmc_rg ")
						.stripAnd()
			);
			return(null);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false, ex.toString()));
		}	
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketBegin();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketBegin();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketAdd(pcol.getCell("svmc_rg").getInt(),-1.0);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean p_isUpdate) {
		ReturnMsg rtnMsg = super.biAfterAddUpdateCurrent(col,p_isUpdate);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketAdd(col.getCell("svmc_rg").getInt(),1.0);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = genbucketCommit();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(p_lookupTable.getName().equals("stock")) {
		if(wcl == null ) wcl = new Wherecl();
		wcl.appendString(" and st_mtype in('M','O') ").stripAnd();
		}
//		if(p_lookupTable.getName().equals("stmcmodel")) {
//		if(wcl == null ) wcl = new Wherecl();
//		wcl.appendString(" and stmcm_rg in(select mcfm_modelrg from mcfitmodel where mcfm_mrg = "+ getCell("svmc_irg").getInt() + ") ").stripAnd();
//		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	
}
