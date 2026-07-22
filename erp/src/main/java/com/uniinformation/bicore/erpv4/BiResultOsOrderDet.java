package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;


public class BiResultOsOrderDet extends BiResult {
	protected String subLinkId=null;
	HashSet<String> affectedIrg = null;
	boolean updateCostTable = true;
	public BiResultOsOrderDet (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultAfsOsOrderDet Used");
		subLinkId = "erpv4.OsDet";
	}

	ReturnMsg updateBucket(CellCollection pcol) {
		//		RpcClient rpc = getView().getSchema().getRpcClient();
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value val = rpc.callSegment(
				"erpv4GenbucketBegin", new Vector()
				);	
		if(val == null || !val.toString().startsWith("OK")) {
			return(new ReturnMsg(false,"genbucket begin failed"));
		}

		Vector args = new Vector();
		args.add(pcol.getCell("ind_rg").getInt());
		args.add(pcol.getCell("ind_odrg").getInt());
		args.add(pcol.getCell("ind_irg").getInt());
		Vector <BiCellCollection> recs = getSubLinkResult(subLinkId);
		double d = 0;
		int n=0;
		try {
			for(CellCollection col:recs) {
				col.getCell("stmd_tdindex").set(n);
				args.add("SI");
				args.add(col.getCell("stmd_org").getInt());
				args.add(col.getCell("stmd_irg").getInt());
				if(col.testCell("stmd_loc") != null) {
					args.add(col.getCell("stmd_loc").getString());
				} else {
					args.add("");
				}
				args.add(col.getCell("stmd_qty").getDouble());
				n++;
			}
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,"Update Failed"));
		}
		val = rpc.callSegment(
				"erpv4UpdateQuodetStmdGenBucket",
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
			return(new ReturnMsg(false, "Confirm Failed : " + val.toString()));
		}
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(updateCostTable)  {
			try {
				affectedIrg = new HashSet<String>(); 
				setAffectedIrgFromDatabase(pcol);
			} catch (Exception ex) {
				UniLog.log(ex);
				rtnMsg = new ReturnMsg(false,"select quodet error");
				rtnMsg.setFatal(true);
				return(rtnMsg);
			}
		}
		
		return(ReturnMsg.defaultOk);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(updateCostTable) {
			affectedIrg = new HashSet<String>(); 
		}
		return(updateBucket(pcol));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(updateCostTable)  {
		try {
			affectedIrg = new HashSet<String>(); 
			setAffectedIrgFromDatabase(pcol);
		} catch (Exception ex) {
			UniLog.log(ex);
			rtnMsg = new ReturnMsg(false,"select stmovd error");
			rtnMsg.setFatal(true);
			return(rtnMsg);
		}
		}
		return(updateBucket(pcol));
	}
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtnMsg = super.biAfterAddUpdateCurrent(col,isUpdate);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(updateCostTable) {
			setAffectedIrgFromCurrentRec(col);
			clearAffectedCostTable();
		}
		return(ReturnMsg.defaultOk);
	}
	void setAffectedIrgFromDatabase(CellCollection pcol) throws Exception {
			TableRec tr = getSelectUtil().getQueryResult("select stmd_irg,stmd_org from stmovd where stmd_mrg = ? and stmd_qorg = ? and stmd_qirg = ? " 
						, new Wherecl()
							.appendArgument(pcol.getCellInt("ind_rg"))
							.appendArgument(pcol.getCellInt("ind_odrg"))
							.appendArgument(pcol.getCellInt("ind_irg"))
					);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				affectedIrg.add(CostCalculation.getCostKey(tr.getFieldInt("stmd_irg"),tr.getFieldInt("stmd_org")));
			}
	}
	void setAffectedIrgFromCurrentRec(BiCellCollection pcol) {
   		BiResult sr = getSubLink(subLinkId);
   		if(sr == null) return;
    	Vector<BiCellCollection> v = sr.getRowCollectionList();
    	for(BiCellCollection c : v) {
			affectedIrg.add(CostCalculation.getCostKey(c.getCellInt("stmd_irg"),c.getCellInt("stmd_org")));
    	}
	}
	void clearAffectedCostTable() {

    	try {
    	for(String s : affectedIrg) {
    		CostCalculation.clearCostTable(sh,s);
    	}
    	} catch (Exception ex) {
    		UniLog.log(ex);
    	}
	}
	
}
