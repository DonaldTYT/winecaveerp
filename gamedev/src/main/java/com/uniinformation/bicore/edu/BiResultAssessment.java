package com.uniinformation.bicore.edu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAssessment extends BiResult{
	public BiResultAssessment(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		UniLog.log1("called");
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		rtn = doAddUpdateDeleteToken(false);
		return(rtn);
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col) {
		UniLog.log1("called");
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		rtn = doAddUpdateDeleteToken(false);
		return(rtn);
	}

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		UniLog.log1("called");
		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		rtn = doAddUpdateDeleteToken(true);
		return(rtn);
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		UniLog.log1("called");
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			double fee = col.getCellDouble("essnas_fee");
			String ccy = col.getCellString("essnas_tokenccy");
			if (fee > 0) {
				if (StringUtils.isBlank(ccy))
					return new ReturnMsg(false, "Token name cannot be empty");
			}
			else if (fee < 0)
				return new ReturnMsg(false, "Fee/Session cannot be less than 0");
		}
		catch (Exception e) {
			return new ReturnMsg(e);
		}

		return(rtn);
	}

	@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		SessionHelper sh = getSessionHelper();
		if (!sh.isAdminUser() && !sh.hasAccessRight("#edu") && !sh.hasAccessRight("#eduadmin")) {
			if (sh.hasAccessRight("#tutor")) {
				p_where.andUniop("estt_ttno", "=" , sh.getLoginId().toUpperCase());
				return Sets.newHashSet(getColumnByLabel("estt_ttno").getTable());
			}
		}
		return super.addExtraWhereStr(p_where, p_hash);
	}

	ReturnMsg doAddUpdateDeleteToken(boolean deleteMode) {
		List<String> cardNoList = new ArrayList<String>();
		try {
			if (!StringUtils.equals(getCellString("essnas_status"), "Normal"))
				return ReturnMsg.defaultOk;
			RpcClient rpc = getSelectUtil().getRpcClient();
			Vector args = new Vector();
			args.add(0); //course rg
			args.add(getCellInt("essnas_rg")); //session rg
			if (!deleteMode) {
				for (BiCellCollection bc : (Vector<BiCellCollection>) getSubLinkResult("edu.Attendance")) {
					args.add(bc.getCellInt("esatsd_atrg")); //student rg
					args.add(bc.getCellString("essnas_tokenccy")); //ccy
					args.add(StringUtils.equalsAny(bc.getCellString("esatsd_status"), "Present", "Absent") ? bc.getCellDouble("essnas_fee") : 0.0); //sesionfee
					int count = args.size();
					UniLog.log1("rpccall courseRg:%d, sessionRg:%d, studentRg:%d, ccy:%s, fee:%f", args.get(0), args.get(1), args.get(count - 3), args.get(count - 2), args.get(count - 1));
					cardNoList.add(bc.getCellString("essd_cardno"));
				}
			}
			else {
				UniLog.log1("rpccall courseRg:%d, sessionRg:%d");
				for (BiCellCollection bc : (Vector<BiCellCollection>) getSubLinkResult("edu.Attendance")) {
					cardNoList.add(bc.getCellString("essd_cardno"));
				}
			}
			Value v = rpc.callSegment("token_addUpdateCourseAttendMulti", args);
			if(v == null && !v.toString().startsWith("OK")) {
				return(
						new ReturnMsg(false,v == null ? "null" : v.toString())
						);
			}
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		//need to call the ProcessScanLog.setCSMapDirty() when attendance updated
		for (String cardNo : cardNoList)
			ProcessScanLog.setCSMapDirty(cardNo);
		return(null);
	}
}
