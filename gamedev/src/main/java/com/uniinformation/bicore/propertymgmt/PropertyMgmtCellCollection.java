package com.uniinformation.bicore.propertymgmt;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.internal.StringUtil;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.MonthUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;


public class PropertyMgmtCellCollection extends ExcelCellCollection {
    private static final DateTimeFormatter YMFMT = DateTimeFormatter.ofPattern("yyyy-MM");
	private enum FuncName { FUNC_calPayUnitMgtfee,FUNC_calPayUnitResfee, FUNC_calPayUnitDetail,FUNC_calPayUnitMgtStart, FUNC_calPayUnitResStart,  
		FUNC_calPayUnitMgtfeePerMon,
		FUNC_calPayUnitResfeePerMon,
		FUNC_calPrepaidAndDisPercent,
		FUNC_calMonthCount,
		NOT_DEFINED }

	public PropertyMgmtCellCollection(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
		// TODO Auto-generated constructor stub
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
		int sid = getSid();
		switch (funcName){
		case FUNC_calPayUnitDetail: {
			String voucher = (String) p_args.get(0);
			String unit = (String) p_args.get(1);
			if(StringUtils.isBlank(unit)) return("");
			String type = (String) p_args.get(2);
			if(StringUtils.isBlank(type)) return("");
			String month = (String) p_args.get(3);
//			if(StringUtils.isBlank(month)) return(0.0);
			boolean mgtfee = (Boolean) p_args.get(4);
			boolean resfee = (Boolean) p_args.get(5);
			if(!mgtfee && !resfee)  return("");
			SelectUtil su = br.getSelectUtil();
			JSONArray ja = new JSONArray();
			int nMonth = MonthUtil.getMonth(month);
			if(nMonth < 0) {
				throw new CellException("Invalid Month",CellException.CELLEXCEPTION_EVAL_ERROR);
			}
			TableRec tr;
			if(StringUtils.isBlank(month)) {
					tr = su.getQueryResult(
							"select contractfee.col_c unit, contractfee.col_b contractdate, contractfee.col_f noofmonth, contractfee.col_g enabled, contractfee.col_d mgtfeepermon, contractfee.col_e resfeepermon, contractfee.col_h constart,max(payitem.col_d) lastpaidmon, sum(payitem.col_e) mgtfeercvd, sum(payitem.col_f) resfeercvd from contractfee left join (unitmonth join payitem on pmu_propertyunit = payitem.col_c and pmu_month = payitem.col_d and payitem.col_d <= ? and payitem.col_a <> ?) on unitmonth.pmu_propertyunit = contractfee.col_c and unitmonth.pmu_contractdate = contractfee.col_b where contractfee.col_c = ?  group by 1,2,3,4,5,6,7 order by 1,2",
					new Wherecl()
						.appendArgument("9999-99")
						.appendArgument(voucher)
						.appendArgument(unit)
					);
			} else {
					tr = su.getQueryResult(
							"select contractfee.col_c unit, contractfee.col_b contractdate, contractfee.col_f noofmonth, contractfee.col_g enabled, contractfee.col_d mgtfeepermon, contractfee.col_e resfeepermon, contractfee.col_h constart,max(payitem.col_d) lastpaidmon, sum(payitem.col_e) mgtfeercvd, sum(payitem.col_f) resfeercvd from contractfee left join (unitmonth join payitem on pmu_propertyunit = payitem.col_c and pmu_month = payitem.col_d and payitem.col_d <= ? and payitem.col_a <> ?) on unitmonth.pmu_propertyunit = contractfee.col_c and unitmonth.pmu_contractdate = contractfee.col_b where contractfee.col_c = ?  group by 1,2,3,4,5,6,7 order by 1,2",
					new Wherecl()
						.appendArgument(month)
						.appendArgument(voucher)
						.appendArgument(unit)
					);
				/*
					tr = su.getQueryResult(
					"select mpy_propertyunit,mpy_month,mpy_mgtfee,mpy_resfee,sum(col_e) mgtpaid,sum(col_f) respaid from monthpayment "
					+ " left outer join	payitem "
					+ "on payitem.col_c = mpy_propertyunit and payitem.col_d = mpy_month and payitem.col_a <> ? "
					+ "where mpy_propertyunit = ? and mpy_month <= ? "
					+ " group by 1,2,3,4 "
					+ " order by 1,2",
					new Wherecl()
						.appendArgument(voucher)
						.appendArgument(unit)
						.appendArgument(month)
					);
					*/
			} 
					String mgtLastMonth = "";
					String resLastMonth = "";
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				int noofmonth = tr.getFieldInt("noofmonth");
				boolean enabled = StringUtils.equals(tr.getFieldString("enabled"), "Y");
				String constart = tr.getFieldString("constart");
				double mgtfeepermonth = tr.getFieldDouble("mgtfeepermon");
				double resfeepermonth = tr.getFieldDouble("resfeepermon");

				double mgtFee = mgtfeepermonth * noofmonth;
				double dd = tr.getFieldDouble("mgtfeercvd");
				if(!Double.isNaN(dd)) mgtFee -= dd;
				
				double resFee = resfeepermonth * noofmonth;
				dd = tr.getFieldDouble("resfeercvd");
				if(!Double.isNaN(dd)) resFee -= dd;
				
					if(mgtfee && mgtfeepermonth > 0) {
						nMonth = (int) Math.floor(mgtFee/mgtfeepermonth);
						if(nMonth < noofmonth) {
							String ss = MonthUtil.nextNmonth(constart,noofmonth-nMonth-1);
							if(ss != null && ss.compareTo(mgtLastMonth) > 0) {
								mgtLastMonth = ss;
							}
						} else if(i > 0) {
//							mgtLastMonth = MonthUtil.nextNmonth(constart,-1);
						}
					}
					if(resfee && resfeepermonth > 0) {
						nMonth = (int) Math.floor(resFee/resfeepermonth);
						if(nMonth < noofmonth) {
							String ss = MonthUtil.nextNmonth(constart,noofmonth-nMonth-1);
							if(ss != null && ss.compareTo(resLastMonth) > 0) {
								resLastMonth = ss;
							}
						} 
					}
				if((mgtfee && mgtFee > 0.0) || (resfee && resFee > 0)) {
					JSONObject jo = new JSONObject();
					jo.put("mgtlastmonth", mgtLastMonth);
					jo.put("reslastmonth", resLastMonth);
					jo.put("mgtfeepermon", mgtfeepermonth);
					jo.put("resfeepermon", resfeepermonth);
					jo.put("constart", constart);
					jo.put("mgtfee", mgtFee);
					jo.put("resfee", resFee);
					jo.put("noofmonth", noofmonth);
					jo.put("enabled", enabled);
					ja.put(jo);
				}
//				UniLog.log("Record " + tr.getFieldString("mpy_month") + " " + tr.getFieldDouble("mpy_mgtfee") + " " + tr.getFieldDouble("mgtpaid")
			}
			return(ja.toString());
			}	
		
		
		case FUNC_calPayUnitMgtfee: {
			String ss = (String) p_args.get(0);
			if(StringUtils.isBlank(ss)) return(0.0);
			String mm = (String) p_args.get(1);
			if(StringUtils.isBlank(mm)) return(0.0);
			Boolean bb = (Boolean) p_args.get(2);
			if(bb==null || !bb) return(0.0);
			JSONArray ja = new JSONArray(ss);
			double fee = 0;
			for(int i=0;i<ja.length();i++) {
				JSONObject jo = ja.getJSONObject(i);
				double feepermonth = jo.getDouble("mgtfeepermon");
				double thisfee = jo.getDouble("mgtfee");
				int noofmonth = jo.getInt("noofmonth");
				String constart = jo.getString("constart");
				int nMon0 = MonthUtil.getMonth(constart);
				int nMon1 = MonthUtil.getMonth(mm);
				int nM;
				if(nMon1-nMon0+1 >= noofmonth) {
					nM = 0;
				} else {
					nM = noofmonth - (nMon1-nMon0+1);
				}
				if(thisfee > feepermonth * nM) fee += thisfee - feepermonth * nM;
			}
			return(fee);
			}
		case FUNC_calPayUnitResfee: {
			String ss = (String) p_args.get(0);
			if(StringUtils.isBlank(ss)) return(0.0);
			String mm = (String) p_args.get(1);
			if(StringUtils.isBlank(mm)) return(0.0);
			Boolean bb = (Boolean) p_args.get(2);
			if(bb==null || !bb) return(0.0);
			JSONArray ja = new JSONArray(ss);
			double fee = 0;
			for(int i=0;i<ja.length();i++) {
				JSONObject jo = ja.getJSONObject(i);
				double feepermonth = jo.getDouble("resfeepermon");
				double thisfee = jo.getDouble("resfee");
				int noofmonth = jo.getInt("noofmonth");
				String constart = jo.getString("constart");
				int nMon0 = MonthUtil.getMonth(constart);
				int nMon1 = MonthUtil.getMonth(mm);
				int nM;
				if(nMon1<nMon0) {
					continue;
				} else if(nMon1-nMon0+1 >= noofmonth) {
					nM = 0;
				} else {
					nM = noofmonth - (nMon1-nMon0+1);
				}
				if(thisfee > feepermonth * nM) fee += thisfee - feepermonth * nM;
			}
			return(fee);
			}
		case FUNC_calPayUnitMgtStart: {
			String ss = (String) p_args.get(0);
			if(StringUtils.isBlank(ss)) return("");
			JSONArray ja = new JSONArray(ss);
			String ms = null;
			for(int i=0;i<ja.length();i++) {
				JSONObject jo = ja.getJSONObject(i);
				String s2 = jo.getString("mgtlastmonth");
				if(!StringUtils.isBlank(s2 )) {
				if(ms == null) ms = s2; else {
					if(ms.compareTo(s2) > 0) {
						ms = s2;
					}
				}
				}
			}
			return(ms == null ? "" : ms);
			}
		case FUNC_calPayUnitResStart: {
			String ss = (String) p_args.get(0);
			if(StringUtils.isBlank(ss)) return("");
			JSONArray ja = new JSONArray(ss);
			String ms = null;
			for(int i=0;i<ja.length();i++) {
				JSONObject jo = ja.getJSONObject(i);
				String s2 = jo.getString("reslastmonth");
				if(!StringUtils.isBlank(s2 )) {
				if(ms == null) ms = s2; else {
					if(ms.compareTo(s2) > 0) {
						ms = s2;
					}
				}
				}
			}
			return(ms == null ? "" : ms);
			}
		case FUNC_calPayUnitMgtfeePerMon: {
			String ss = (String) p_args.get(0);
			if(StringUtils.isBlank(ss)) return("");
			JSONArray ja = new JSONArray(ss);
			String ms = null;
			JSONObject jo;
			LinkedHashSet<String>mfees = new LinkedHashSet<String>();
			for(int i=0;i<ja.length();i++) {
				jo = ja.getJSONObject(i);
				double fee = jo.getDouble("mgtfeepermon");
				if(fee != 0.0) {
					mfees.add(String.format("%.2f", fee));
				}
//				if(ms != null) ms += ",";
//				ms +=String.format("%.2f", jo.getDouble("mgtfeepermon"));
			}
			switch(mfees.size()) {
			case 0 : break;
			case 1 : ms = mfees.toArray()[0].toString(); break;
			default : ms = mfees.toString(); break;
			}
//			if(ja.length() == 0 ) {
//				ms = "";
//			} else if(ja.length() == 1 ) {
//				jo = ja.getJSONObject(0);
//				ms = String.format("%.2f", jo.getDouble("mgtfeepermon"));
//			} else if(ja.length() == 2){
//				jo = ja.getJSONObject(1);
//				ms = "<"+jo.getString("constart")+":";
//				jo = ja.getJSONObject(0);
//				ms +=String.format("%.2f", jo.getDouble("mgtfeepermon"));
//				ms += ",";
//				jo = ja.getJSONObject(1);
//				ms += ">="+jo.getString("constart")+":";
//				ms +=String.format("%.2f", jo.getDouble("mgtfeepermon"));
//			} else {
//				ms = "Not implemented 13491";
//				/*
//				for(int i=0;i<ja.length();i++) {
//					JSONObject jo = ja.getJSONObject(i);
//					String s2 = jo.getString("lastmonth");
//					if(ms == null) ms = s2; else {
//						if(ms.compareTo(s2) > 0) {
//							ms = s2;
//						}
//					}
//				}
//				*/
//			}
			return(ms == null ? "" : ms);
			}
		case FUNC_calPayUnitResfeePerMon: {
			String ss = (String) p_args.get(0);
			if(StringUtils.isBlank(ss)) return("");
			JSONArray ja = new JSONArray(ss);
			String ms = null;
			JSONObject jo;
			
			LinkedHashSet<String>mfees = new LinkedHashSet<String>();
			for(int i=0;i<ja.length();i++) {
				jo = ja.getJSONObject(i);
				double fee = jo.getDouble("resfeepermon");
				if(fee != 0.0) {
					mfees.add(String.format("%.2f", fee));
				}
//				if(ms != null) ms += ",";
//				ms +=String.format("%.2f", jo.getDouble("mgtfeepermon"));
			}
			switch(mfees.size()) {
			case 0 : break;
			case 1 : ms = mfees.toArray()[0].toString(); break;
			default : ms = mfees.toString(); break;
			}
			
//			if(ja.length() == 0 ) {
//				ms = "";
//			} else if(ja.length() == 1 ) {
//				jo = ja.getJSONObject(0);
//				ms = String.format("%.2f", jo.getDouble("resfeepermon"));
//			} else if(ja.length() == 2){
//				jo = ja.getJSONObject(1);
//				ms = "<"+jo.getString("constart")+":";
//				jo = ja.getJSONObject(0);
//				ms +=String.format("%.2f", jo.getDouble("resfeepermon"));
//				ms += ",";
//				jo = ja.getJSONObject(1);
//				ms = ">="+jo.getString("constart")+":";
//				ms +=String.format("%.2f", jo.getDouble("resfeepermon"));
//			} else {
//				ms = "Not implemented 13492";
//				/*
//				for(int i=0;i<ja.length();i++) {
//					JSONObject jo = ja.getJSONObject(i);
//					String s2 = jo.getString("lastmonth");
//					if(ms == null) ms = s2; else {
//						if(ms.compareTo(s2) > 0) {
//							ms = s2;
//						}
//					}
//				}
//				*/
//			}
			return(ms == null ? "" : ms);
			}
		case FUNC_calPrepaidAndDisPercent: {
				Object o1 = p_args.get(1);
				double fee = (double)p_args.get(0);
				int prepaid = o1 instanceof Integer ? (int)o1 : (int)(double)o1;
				double discount = (double)p_args.get(2);
				return String.format("%d | %d%%", prepaid, fee != 0 ? (int)Math.round(discount / fee * 100) : 0);
			}
		case FUNC_calMonthCount: {
				return (int)getMonthCount((String)p_args.get(0), (String)p_args.get(1));
			}
		}
		return super.evalFunction(p_fname, p_args);
	}
	
	public static boolean isValidYearMonth(String input) {
        try {
            YearMonth.parse(input, YMFMT);
            return true;
        } catch (Exception e) {
            return false;
        }
	}
	
	public static long getMonthCount(String start, String end) {
		if (StringUtils.isBlank(start) || StringUtil.isBlank(end))
			return 0;
		try {
			YearMonth startMonth = YearMonth.parse(start, YMFMT);
			YearMonth endMonth = YearMonth.parse(end, YMFMT);
			return ChronoUnit.MONTHS.between(startMonth, endMonth) + 1;
		} catch (Exception e) {
			UniLog.log(e);
			return 0;
		}
    }

	public static String nextMonth(String start, int cnt, String defaultMonth) throws Exception {
		if (StringUtils.isBlank(start))
			return defaultMonth;
        return YearMonth.parse(start, YMFMT).plusMonths(cnt).format(YMFMT);
    }

	public static String nextMonth(String start, int cnt) throws Exception {
		return nextMonth(start, cnt, "");
	}

	public static Set<String> getMonthRange(String start, String end) throws Exception {
        YearMonth startMonth = YearMonth.parse(start, YMFMT);
        YearMonth endMonth = YearMonth.parse(end, YMFMT);
        long monthsBetween = ChronoUnit.MONTHS.between(startMonth, endMonth);
        return LongStream.rangeClosed(0, monthsBetween).mapToObj(i -> startMonth.plusMonths(i).format(YMFMT)).collect(Collectors.toCollection(TreeSet::new));
    }

	public static Set<String> getMonthRange(String start, int plusMonthCount) throws Exception {
		return getMonthRange(start, nextMonth(start, plusMonthCount));
	}
}
