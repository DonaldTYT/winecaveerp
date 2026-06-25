package com.uniinformation.bicore.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultEmColligateRpt extends BiResult {
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");

	private Map<String, SumAttendItem> sumAttendCacheMap = new HashMap<String, SumAttendItem>(); //key: eid
	private Date periodStartDate, periodEndDate;
	private boolean afterLoadSerialMapFlag = false;
	
	public static class SumAttendItem {
		public int workDay;
		public int workMin;
		public int sotDay;
		public int sotMin;
		public int lateDay;
		public int lateMin;
		public int lvearlyDay;
		public int lvearlyMin;
		public int otDay;
		public int otMin;
		public int noworkDay;
		public int noworkMin;
		public String remark = "";
	}

	public BiResultEmColligateRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		UniLog.log1("addExtraWhereStr %s,%s", periodStartDate, periodEndDate);
		p_where.appendString(String.format(" and exists(select serial_id from attendance where at_eid = em_eid and at_date between '%s' and '%s' and at_date >= em_stdate and (em_enddate = '' or at_date <= em_enddate)) ", 
				ddf.format(periodStartDate), ddf.format(periodEndDate)));
		return super.addExtraWhereStr(p_where, p_hash);
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		UniLog.log("createColumnCollection");
		return(new Erpv4EmColligateRptCellColletion(p_parent, this));
	}

	@Override
	public String getColumnDisplayString(ColumnCell p_cell) {
		if (StringUtils.equalsAny(p_cell.getCellLabel(), "em_xworkmins", "em_xsotmins", "em_xlatemins", "em_xlvearlymins", "em_xotmins", "em_xnoworkmins")) {
			int i = p_cell.getInt();
			return String.format("%02d:%02d", i / 60, i % 60);
		}
		return super.getColumnDisplayString(p_cell);
	}

	public Map<String, SumAttendItem> getSumAttendCacheMap() {
		return afterLoadSerialMapFlag ? sumAttendCacheMap : null;
	}

	private void clearCacheMap() {
		UniLog.log("clearCacheMap");
		sumAttendCacheMap.clear();
	}

	public void setQueryPeriod(Date startDate, Date endDate) {
		periodStartDate = startDate;
		periodEndDate = endDate;
		UniLog.log1("periodStartDate:%s, periodEndDate:%s", periodStartDate, periodEndDate);
	}
	
	public Date getPeriodStartDate() {
		return periodStartDate;
	}

	public Date getPeriodEndDate() {
		return periodEndDate;
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
