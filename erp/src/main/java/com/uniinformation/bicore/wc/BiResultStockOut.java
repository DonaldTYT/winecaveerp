package com.uniinformation.bicore.wc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import org.apache.commons.lang3.tuple.Pair;

public class BiResultStockOut extends BiResultStmov {

	public BiResultStockOut(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection pcol,boolean isUpdate) {
		ReturnMsg rtnMsg = null;
		rtnMsg = super.biBeforeAddUpdateCurrent(pcol,isUpdate);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(pcol.getCellDate("stm_cfmdate") != null &&
				pcol.getCellDate("stm_cfmdate").after(DateUtil.minDate)) {
			return(new ReturnMsg(false,"Order Posted to Accounting, Cannout Update"));
		}
		RpcClient rpc = getSelectUtil().getRpcClient();
		Vector<BiCellCollection> stmdcols = getSubLink("wc.StmdMoSi").getRowCollectionList();
   		Vector args ;
   		try {
		for(BiCellCollection col : stmdcols) {
			int siorg = col.getCellInt("stmdsi_org");
			if(siorg <= 0) {
			args = new Vector();
			args.add(col.getCellString("or_ocode"));
			args.add(col.getCellString("stm_ref2"));
			args.add(col.getCellInt("stmd_mrg"));
			args.add(col.getCellString("or_vcode"));
			args.add(col.getCell("stmd_date").getDate());
			Value v = rpc.callSegment( "erpv3_check_or_create_porecord_bypocode", args);
			int cc = v.toInt();
			if(cc <= 0) {
				return(new ReturnMsg(false,"Fail to get siorg",true));
			}
			col.getCell("stmdsi_org").set(cc);
			}
		}
   		} catch (Exception ex) {
   			UniLog.log(ex);
   			return(new ReturnMsg(false,"Fail to get org",true));
   		}
		
   		if(!isUpdate) {
   			args = new Vector();
			args.add("smomvh");
			args.add(pcol.getCell("stm_date").getDate());
			Value v = rpc.callSegment( "erpv3GetrgByControl", args);
			if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,"Unknown Error",true));
			try {
				pcol.getCell("stm_cuser").set(su.getLoginId());
				pcol.getCell("stm_cdate").set(new java.util.Date());
				pcol.getCell("stm_uuser").set(su.getLoginId());
				pcol.getCell("stm_udate").set(new java.util.Date());
				pcol.getCell("stm_ref1").set(v.toString().substring(4).trim());
			} catch(CellException ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"Unknown Error",true));
			}
   		} else {
			try {
				pcol.getCell("stm_uuser").set(su.getLoginId());
				pcol.getCell("stm_udate").set(new java.util.Date());
			} catch(CellException ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"Unknown Error",true));
			}
   		}
		return(new ReturnMsg(true));
	}
	ReturnMsg doAddUpdateStmpostExt() {
		String nonConsignmentCoCode = Erpv4Config.getString(sh, "NonConsigmentCocode");
		if(!StringUtils.isBlank(nonConsignmentCoCode)) {
			Hashtable<String,Pair<Double,Double>> consgHash = new Hashtable<String,Pair<Double,Double>> ();
//			for(BiCellCollection bi : getSubLink("wc.StmpostExt").getRowCollectionList()) {
			for(BiCellCollection bi : getSubLink("wc.StmdMoSi").getRowCollectionList()) {
				String cocode = bi.getCellString("or_cocode");
				if(!nonConsignmentCoCode.equals(cocode)) {
					double cbtl = -bi.getCellDouble("stmd_qty");
					double camt = bi.getCellDouble("stmd_fref1") * cbtl;
					Pair<Double,Double> consgPair = consgHash.get(cocode);
					if(consgPair != null) {
						cbtl += consgPair.getLeft();
						camt += consgPair.getRight();
					}
					consgHash.put(cocode, Pair.of(cbtl, camt));
				}
			}
			BiResult sr = getSubLink("wc.StmpostExt");
			int n = sr.getRowCount();
			try {
			for(int i=0;i<n;i++) {
				BiCellCollection bc = sr.getRowCollectionV(i);
				String cocode = bc.getCellString("stmp_cocode");
				Pair<Double,Double> consgPair = consgHash.get(cocode);
				if(consgPair != null) {
					Object o = sr.getTrStatObj(i);
					sr.markDelete( o, false);
					bc.getCell("stmp_cbtl").set(consgPair.getLeft());
					bc.getCell("stmp_amount").set(consgPair.getRight());
					bc.getCell("stmp_net").set(consgPair.getRight());
					consgHash.remove(cocode);
				} else {
					Object o = sr.getTrStatObj(i);
					sr.markDelete( o, true);
				}
			}
			for(String cocode : consgHash.keySet()) {
				Pair<Double,Double> consgPair = consgHash.get(cocode);
				BiCellCollection scol = sr.newRowCollection();
				scol.getCell("stmp_cocode").set(cocode);
				scol.getCell("stmp_ptype").set("CP");
				scol.getCell("stmp_cbtl").set(consgPair.getLeft());
				scol.getCell("stmp_amount").set(consgPair.getRight());
				scol.getCell("stmp_net").set(consgPair.getRight());
				ReturnMsg rtn = sr.addSubRecord(scol, -1,"");
				if(rtn != null && !rtn.getStatus()) {
					return(rtn);
				}
			}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
		}
		return(ReturnMsg.defaultOk);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = doAddUpdateStmpostExt();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = doAddUpdateStmpostExt();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht  = super.addExtraWhereStr(p_where, p_hash);
		String uid = getSelectUtil().getLoginId();
		UniLog.log("user = " + getSelectUtil().getLoginId());
		if(!BiSchema.hasAccessRight(sh, "#allso")) {
			Wherecl wcl1 = new Wherecl();
			wcl1.andUniop("stm_cuser", "=", sh.getVcode());
			if(BiSchema.hasAccessRight(sh, "!!sales")) {
				try {
				SelectUtil su = getSelectUtil();
				TableRec tr = su.getQueryResult("select * from salesman where sm_logname = ? ",
						new Wherecl().appendArgument(sh.getVcode())
						);
				if(tr.getRecordCount() == 1) {
					tr.setRecPointer(0);
					Wherecl wcl2 = new Wherecl();
					wcl2.andUniop("stm_ref3", "=", tr.getFieldString("sm_code"));
					wcl1.orWherecl(wcl2);
				}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			p_where.andWherecl(wcl1);
		} 
		return(ht);
	}
}
