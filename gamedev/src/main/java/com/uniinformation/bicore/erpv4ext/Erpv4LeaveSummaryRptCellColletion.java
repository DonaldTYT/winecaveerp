package com.uniinformation.bicore.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication.LeaveCal;
import com.uniinformation.utils.UniLog;

public class Erpv4LeaveSummaryRptCellColletion extends BiCellCollection {
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private enum FuncName { FUNC_getNDays, NOT_DEFINED }

	public Erpv4LeaveSummaryRptCellColletion(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
		UniLog.log1("called");
	}

	private Map<String, LeaveCal> getLeaveCalCacheMap() {
		if (getBr() instanceof BiResultLeaveSummaryRpt)
			return ((BiResultLeaveSummaryRpt)getBr()).getLeaveCalCacheMap();
		return null;
	}

	private BiResult brLeave = null;
	
	private void setCellLvValue(String targetKey, String refKey, int d, double[] refValues) throws CellException {
		double dd = NumberUtils.toDouble(LeaveApplication.getLeaveUnit2LvStr(d));
		if (StringUtils.equals(targetKey, refKey))
			refValues[0] = dd;
		else
			getCell(targetKey).set(dd);
	}

	@Override
	public Object evalFunction(String p_fname, Vector p_args) throws Exception {
		//UniLog.log1("p_fname:%s", p_fname);
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}

		switch (funcName){
			case FUNC_getNDays:
				String refKey = (String)p_args.get(0);
				String eid = (String)p_args.get(1);
				Date emstdate = (Date)p_args.get(2);
				Date emenddate = (Date)p_args.get(3);
				int stalcnt = ((Double)p_args.get(4)).intValue();
				int maxalcnt = ((Double)p_args.get(5)).intValue();
				int ofsalcnt = ((Double)p_args.get(6)).intValue();
				String alstday = (String)p_args.get(7);
				Map<String, LeaveCal> cacheMap = getLeaveCalCacheMap();
				UniLog.log1("refKey:%s, sid:%d, eid:%s, cacheMap:%b, emstdate:%s, emenddate:%s, stalcnt:%d, maxalcnt:%d, ofsalcnt:%d, alstday:%s", refKey, getSid(), eid, cacheMap != null, emstdate, emenddate, stalcnt, maxalcnt, ofsalcnt, alstday);
				if (getSid() == 0 || StringUtils.isBlank(eid) || cacheMap == null)
					return 0.0;
				double[] resultDays = new double[] {0.0};
				LeaveCal leaveCal = cacheMap.get(eid);
				if (leaveCal == null) {
					leaveCal = new LeaveCal(br, emstdate, emenddate, stalcnt, maxalcnt, ofsalcnt, alstday);
					cacheMap.put(eid, leaveCal);
					leaveCal.clearLvUtilList("");

					//query 'AL (Adj/Bal)': date <= 'year/12/31', query 'BL/CL/JL/MA...': date between 'year/01/01' and 'year/12/31'
					int queryYear = ((BiResultLeaveSummaryRpt)br).getQueryYear();
					Date queryStDate = DateUtil.dateTimeStrToDate(queryYear + "/01/01");
					Date queryEndDate = DateUtil.yearEnd(queryStDate);
					UniLog.log1("queryYear:%d, queryStDate:%s, queryEndDate:%s", queryYear, queryStDate, queryEndDate);
					leaveCal.genCalLeave("AL", eid, queryEndDate);

					StringBuilder sbQuery = new StringBuilder(String.format("lv_eid = '%s'", eid));
					sbQuery.append(String.format(" and lv_sdate <= '%s'", ddf.format(queryEndDate)));
					sbQuery.append(String.format(" and lv_sdate >= '%s'", ddf.format(emstdate)));
					sbQuery.append(String.format(" and lv_edate >= '%s'", ddf.format(emstdate)));
					if (DateUtil.isValid(emenddate)) {
						sbQuery.append(String.format(" and lv_sdate <= '%s'", ddf.format(emenddate)));
						sbQuery.append(String.format(" and lv_edate <= '%s'", ddf.format(emenddate)));
					}
					brLeave = BiResultHelper.create(br.getSessionHelper(), "erpv4ext.LeaveApplicationDet", brLeave, sbQuery.toString(), null, -1, null, false);

					int aldays = 0;
					int aladj = 0;
					int albal = 0;
					Map<String, Object> userMap = leaveCal.getUserMap();
					Map<String, Integer> daysMap = new HashMap<String, Integer>();
					while (brLeave != null && brLeave.next()) {
						String reason = brLeave.getCellString("lv_reason");
						Date sdate = brLeave.getCellDate("lv_sdate");
						Date stime = brLeave.getCellDate("lv_sttime");
						Date etime = brLeave.getCellDate("lv_endtime");
						int leaveunit = brLeave.getCellInt("lv_leaveunit");
						UniLog.log1("reason:%s, sdate:%s, stime:%s, etime:%s, leaveunit:%d", reason, sdate, stime, etime, leaveunit);
						if (StringUtils.equals(reason, "AL")) {
							albal = leaveCal.genCalLeaveRemained(reason, sdate, leaveunit);
							if (stime.compareTo(LeaveApplication.START_TIME_IN_DAY) == 0 && etime.compareTo(LeaveApplication.START_TIME_IN_DAY) == 0) //if sttime&&endtime==00:00, can be manual input leave days
								aladj += leaveunit;
							else
								aldays += leaveunit;
						}
						else {
							if (sdate.compareTo(queryStDate) < 0) //query 'BL/CL/JL/MA...': date between 'year/01/01' and 'year/12/31'
								continue;
							int d = (Integer)ObjectUtils.defaultIfNull(daysMap.get("emx_days_" + reason), 0);
							daysMap.put("emx_days_" + reason, d + leaveunit);
						}
					}
					albal = leaveCal.genCalLeaveRemained("AL", queryEndDate, 0);

					userMap.put("emx_aldays", aldays);
					userMap.put("emx_aladj", aladj);
					userMap.put("emx_albal", albal);
					setCellLvValue("emx_aldays", refKey, aldays, resultDays);
					setCellLvValue("emx_aladj", refKey, aladj, resultDays);
					setCellLvValue("emx_albal", refKey, albal, resultDays);
					for (Enumeration<String> en = getCellTable().keys(); en.hasMoreElements(); ) {
						String key = en.nextElement();
						if (StringUtils.startsWith(key, "emx_days_")) {
							if (daysMap.containsKey(key)) {
								int value = daysMap.get(key);
								userMap.put(key, value);
								setCellLvValue(key, refKey, value, resultDays);
							} else
								setCellLvValue(key, refKey, 0, resultDays);
						}
					}
				} else {
					Map<String, Object> m = leaveCal.getUserMap();
					for (Enumeration<String> en = getCellTable().keys(); en.hasMoreElements(); ) {
						String key = en.nextElement();
						if (StringUtils.startsWith(key, "emx_"))
							setCellLvValue(key, refKey, m.containsKey(key) ? (Integer)m.get(key) : 0, resultDays);
					}
				}
				return resultDays[0];
		}
		return super.evalFunction(p_fname, p_args);
	}
}
