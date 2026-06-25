package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsStmd_not_used extends BiResult {
	String stmdLinkName;
	public BiResultAfsStmd_not_used (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultAfsStmd Used");
	}
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
//   		RpcClient rpc = getView().getSchema().getRpcClient();
		UniLog.logm(this, "biBeforeDeleteCurrent haha0");
		RpcClient rpc = getSelectUtil().getRpcClient();
   		Vector args = new Vector();
		args.add(col.getCell("stm_mrg").getInt());
		args.add(col.getCell("stm_status").getString());
  		Value v = rpc.callSegment(
					"erpv4UpdateStmdGenBucket",
					args
				);
//    		rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				
				v = rpc.callSegment(
						"erpv4GenbucketCommit", new Vector()
					);
		  		if(v != null && v.toString().startsWith("OK")) {
		  			ReturnMsg rtnMsg = null;
		  			String s = StringUtil.strpart(v.toString(), 4 , -1);
		  			if(!s.trim().equals("")) {
		  				rtnMsg = GenbucketUtil.qoGenBucketCheckResult(s,sh);
		  			} 
					if(rtnMsg == null || rtnMsg.getStatus()) {
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
		  		} 
				// redir to P.O. Update Page
			} 
			if(v == null) {
				return(new ReturnMsg(false, "Confirm Failed : Unknown Reason"));
			} else {
				return(new ReturnMsg(false, "Confirm Failed : " + v.toString().substring(4)));
			}
	}
	ReturnMsg updateBucket(CellCollection pcol) {
//  		RpcClient rpc = getView().getSchema().getRpcClient();
		UniLog.logm(this, "updateBucket haha0");
		RpcClient rpc = getSelectUtil().getRpcClient();
   		Vector args = new Vector();
		args.add(pcol.getCell("stm_mrg").getInt());
		args.add(pcol.getCell("stm_status").getString());
//		Vector <CellCollection> recs = getSubLink("AfsPoDet").getRecs();
		Vector <BiCellCollection> recs = getSubLinkResult(stmdLinkName);
		double d = 0;
		int n=0;
		try {
		for(CellCollection col:recs) {
			col.getCell("stmd_tdindex").set(n);
			int org = col.getCell("stmd_org").getInt();
			/*
			if(org <= 0) {
				org = getView().getSchema().getRg("", 1006);
				col.getCell("stmd_org").set(org);
			}
			*/
			args.add(col.getCell("stmd_tdtype").getString());
			args.add(col.getCell("stmd_qorg").getInt());
			args.add(col.getCell("stmd_org").getInt());
			args.add(col.getCell("stmd_irg").getInt());
			args.add(col.getCell("stmd_qirg").getInt());
			Cell c0 = col.testCell("stmd_ref4");
			if(c0 != null) args.add(c0.getString()); else args.add("");
			args.add(col.getCell("stmd_qty").getDouble());
			n++;
		}
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,"Update Failed"));
		}
  		Value val = rpc.callSegment(
				"erpv4UpdateStmdGenBucket",
				args
			);
	
  		if(val != null && val.toString().startsWith("OK")) {
  			val = rpc.callSegment(
				"erpv4GenbucketCommit", new Vector()
			);
  			if(val != null && val.toString().startsWith("OK")) {
  				String s = StringUtil.strpart(val.toString(), 4 , -1);
  				if(!s.trim().equals("")) {
  					return(GenbucketUtil.qoGenBucketCheckResult(s,sh));
  				} else return(null);
  			} 
  		}
  		if(val == null) {
  			return(new ReturnMsg(false, "Confirm Failed : Unknown Reason"));
  		} else {
  			return(new ReturnMsg(false, "Confirm Failed : " + val.toString().substring(4)));
  		}
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			pcol.getCell("stm_cuser").set(su.getLoginId());
			pcol.getCell("stm_cdate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		return(updateBucket(pcol));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(updateBucket(pcol));
	}
}
