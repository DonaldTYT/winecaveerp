package com.uniinformation.bicore.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultScheduleRpt extends BiResult {
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat ddf1 = new SimpleDateFormat("yyyyMMdd");
	private Map<String, Map<Date, String>> shiftCodeCacheMap = new HashMap<String, Map<Date, String>>(); //key: eid

	private boolean afterLoadSerialMapFlag = false;
	private Date periodStartDate, periodEndDate;
	private BiResult brAttendance;

	public BiResultScheduleRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
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
	protected void afterLoadCollection(boolean p_isFetch, BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		try {
			String eid = p_cc.getString("em_eid");
			Date emStDate = p_cc.getDate("em_stdate");
			Date emEndDate = p_cc.getDate("em_enddate");
			Map<Date, String> m = getShiftCodeMap(eid, emStDate, emEndDate);
			for (Enumeration<String> en = p_cc.getCellTable().keys(); en.hasMoreElements(); ) {
				String key = en.nextElement();
				if (StringUtils.startsWith(key, "em_xschedate")) {
					Date date = ddf1.parse(key.substring(12));
					p_cc.getCell(key).set(StringUtils.defaultString(m.get(date)));
				}
			}
		}
		catch (Exception e) {
			UniLog.log(e);
		}
	}	

	public void setQueryPeriod(Date startDate, Date endDate) {
		periodStartDate = startDate;
		periodEndDate = endDate;
		UniLog.log1("periodStartDate:%s, periodEndDate:%s", periodStartDate, periodEndDate);
	}

	private void clearCacheMap() {
		UniLog.log("clearCacheMap");
		shiftCodeCacheMap.clear();
	}

	private Map<Date, String> getShiftCodeMap(String eid, Date emStDate, Date emEndDate) throws Exception {
		if (!afterLoadSerialMapFlag)
			return new HashMap<Date, String>();
		Map<Date, String> m = shiftCodeCacheMap.get(eid);
		if (m == null) {
			m = new HashMap<Date, String>();
			shiftCodeCacheMap.put(eid, m);

			brAttendance = BiResultHelper.create(sh, "erpv4ext.ScheduleRptAtt", brAttendance, String.format("at_eid = '%s' and at_date between '%s' and '%s'", eid, ddf.format(periodStartDate), ddf.format(periodEndDate)), null, -1, null, false);
			while (brAttendance != null && brAttendance.next()) {
				Date date = brAttendance.getCellDate("at_date");
				String shiftCode = brAttendance.getCellString("at_shiftcode");
				if (date.compareTo(emStDate) >= 0 && (DateUtil.isDateNull(emEndDate) || date.compareTo(emEndDate) <= 0))
					m.put(date, shiftCode);
			}
		}
		return m;
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
