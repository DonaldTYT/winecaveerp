package com.uniinformation.bicore.erpv4ext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4ext.BiResultLeaveApplication.LeaveCal;
import com.uniinformation.bicore.erpv4ext.BiResultLeaveApplication.RlvItem;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLeaveSummaryRpt extends BiResultLeaveApplication {
	class LeaveCalExt {
		int aldays;
		int aladj;
	}
	private Map<String, LeaveCalExt> leaveCalExtCacheMap = new HashMap<String, LeaveCalExt>(); //key: emid
	private boolean afterLoadSerialMapFlag = false;
	private int queryYear = DateUtil.getYear(DateUtil.today());
	
	public BiResultLeaveSummaryRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
		try {
			Vector<CellCollection> vc = su.getQueryResultToCellVector("select lvrs_name from leavereason where lvrs_name <> 'AL' order by lvrs_name", null);
			for (CellCollection cc : vc) {
				String name = cc.getString("lvrs_name");
				addTempColumn("emx_days_" + name, name, "", "0.0", "float", null,0);
			}
		} catch (Exception e) {
			UniLog.log(e);
		}
	}
	
	@Override 
	protected void loadLeaveCal() throws Exception {
		super.loadLeaveCal();
		String eid = getCellString("em_eid");
		Date queryStDate = DateUtil.dateTimeStrToDate(queryYear + "/01/01");
		Date queryEndDate = DateUtil.yearEnd(queryStDate);
//		Date queryAsAtDate = DateUtil.today();
		Vector<BiCellCollection> recs = getSubLinkResult("erpv4ext.LeaveAdminDet");
		LeaveCalExt lvExt = new LeaveCalExt();
		for (BiCellCollection cc : recs) {
			Date lvDate = cc.getDate("lv_sdate");
			if(lvDate.before(queryStDate)) continue;
			if(lvDate.after(queryEndDate)) continue;
			if (StringUtils.equals(cc.getString("lv_reason"), "AL")) {
				Date sdate = cc.getCellDate("lv_sdate");
				Date edate = cc.getCellDate("lv_edate");
				Date stime = cc.getCellDate("lv_sttime");
				Date etime = cc.getCellDate("lv_endtime");
				int leaveunit = cc.getCellInt("lv_leaveunit");
				if (stime.compareTo(BiResultLeaveApplication.START_TIME_IN_DAY) == 0 && etime.compareTo(BiResultLeaveApplication.START_TIME_IN_DAY) == 0) //if sttime&&endtime==00:00, can be manual input leave days
									lvExt.aladj += leaveunit;
								else
									lvExt.aldays += leaveunit;
			}
		}
		if(getCell("emx_aldays") != null) getCell("emx_aldays").set(lvExt.aldays/LEAVEUNIT_PER_DAY);
		if(getCell("emx_aladj") != null) getCell("emx_aladj").set(lvExt.aladj/LEAVEUNIT_PER_DAY);
		if(getCell("emx_alexp") != null) {
			List<RlvItem> rlvList = new ArrayList<RlvItem>();
			LeaveCal lv = 	getLeaveCal(eid);
			int cc = 0;
			Date asAtDate;
			asAtDate =  queryEndDate;
			if(asAtDate.after(DateUtil.today())) {
				asAtDate = DateUtil.today();
			}
			lv.genGetLeaveExpired("AL", asAtDate, rlvList);
			for (RlvItem item : rlvList) { 
				cc += item.unit;
			}
			rlvList = new ArrayList<RlvItem>();
			asAtDate =  DateUtil.prevday(queryStDate);
			if(asAtDate.after(DateUtil.today())) {
				asAtDate = DateUtil.today();
			}
			lv.genGetLeaveExpired("AL", asAtDate, rlvList);
			for (RlvItem item : rlvList) { 
				cc -= item.unit;
			}
			getCell("emx_alexp").set(cc/LEAVEUNIT_PER_DAY);
		}
		if(getCell("emx_albal") != null) {
			List<RlvItem> rlvList = new ArrayList<RlvItem>();
			LeaveCal lv = 	getLeaveCal(eid);
			Date asAtDate;
			asAtDate =  queryEndDate;
			if(asAtDate.after(DateUtil.today())) {
				asAtDate = DateUtil.today();
			}
			lv.genGetLeaveRemain("AL", asAtDate, queryEndDate, rlvList);
			int cc = 0;
			for (RlvItem item : rlvList) { 
				cc += item.unit;
			}
			getCell("emx_albal").set(cc/LEAVEUNIT_PER_DAY);
		}
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		UniLog.log("createColumnCollection");
		return new Erpv4LeaveSummaryRptCellColletion(p_parent, this);
	}

	public Map<String, LeaveCalExt> getLeaveCalExtCacheMap() {
		return afterLoadSerialMapFlag ? leaveCalExtCacheMap : null;
	}
	
	public void setQueryYear(int year) {
		queryYear = year;
	}

	public int getQueryYear() {
		return queryYear;
	}
	
	private void clearCacheMap() {
		UniLog.log("clearCacheMap");
		leaveCalExtCacheMap.clear();
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		//clear cache map when perform query/refresh
		ReturnMsg rtn = super.afterLoadSerialMap();
		if (!rtn.getStatus()) return rtn;
		
		clearCacheMap();
		afterLoadSerialMapFlag = true;
		return(ReturnMsg.defaultOk);
	}
}
