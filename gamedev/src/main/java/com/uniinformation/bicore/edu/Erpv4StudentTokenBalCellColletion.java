package com.uniinformation.bicore.edu;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;

public class Erpv4StudentTokenBalCellColletion extends BiCellCollection {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private DecimalFormat feeDecFmt = new DecimalFormat(",###.##");
	private enum FuncName { FUNC_getSessionRemain,FUNC_getSessionOverdue,FUNC_getSessionRemainBySessNo,NOT_DEFINED }

	public Erpv4StudentTokenBalCellColletion(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
		UniLog.log1("called");
	}
	private Map<Integer, Map<Integer, Map<String, Object>>> getCacheMap() {
		if (getBr() instanceof BiResultStudentTokenBal)
			return ((BiResultStudentTokenBal)getBr()).getSessionBalanceCacheMap();
		return null;
	}
	private Map<String, Object> calcSessionBalance(Vector p_args) throws Exception {
		int sid = getSid();
		int studentRg = ((Double)p_args.get(0)).intValue();
		int courseRg = ((Double)p_args.get(1)).intValue();
		Map<Integer, Map<Integer, Map<String, Object>>> cacheMap = getCacheMap();
		if (sid == 0 || studentRg == 0 || courseRg == 0 || cacheMap == null)
			return null;
		Map<Integer, Map<String, Object>> cacheCourseMap = cacheMap.get(studentRg);
		if (cacheCourseMap == null) {
			//UniLog.log1("studentRg:%d, courseRg:%d to cache map", studentRg, courseRg);
			cacheCourseMap = new HashMap<Integer, Map<String, Object>>();
			cacheMap.put(studentRg, cacheCourseMap);

			//query course
			TableRec tr = getBr().getSelectUtil().getQueryResult("select essb_avrg, essb_startdate, essb_enddate, essb_sessoffset, esav_fee, esav_tokenccy, esav_numsession, tkbal_ostqty "
																+ "from essubscribe, esactivity, outer(tokenbal) "
																+ "where essb_type = 0 and essb_sdrg = ? and essb_status <> 'Cancelled' "
																+ "and esav_rg = essb_avrg and tkbal_org = essb_sdrg and tkbal_ccy = esav_tokenccy" , 
						new Wherecl().appendArgument(studentRg));
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				int avrg = tr.getFieldInt("essb_avrg");
				Date subStartDate = tr.getFieldDate("essb_startdate");
				Date subEndDate = tr.getFieldDate("essb_enddate");
				int sessionOffset = tr.getFieldInt("essb_sessoffset");
				double sessionFee = tr.getFieldDouble("esav_fee");
				int numSession = tr.getFieldInt("esav_numsession");
				String tokenCcy = tr.getFieldString("esav_tokenccy");
				double tokenBalance = tr.getFieldDouble("tkbal_ostqty");
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("startDate", subStartDate);
				m.put("endDate", subEndDate);
				m.put("sessionOffset", sessionOffset);
				m.put("sessionFee", sessionFee);
				m.put("numSession", numSession);
				m.put("tokenCcy", tokenCcy);
				m.put("tokenBalance", tokenBalance);
				cacheCourseMap.put(avrg, m);
			}
			Map<String, Double> tMap = new HashMap<String, Double>(); //sum(Courses tentative balance) group by tokenCcy
			for (Map.Entry<Integer, Map<String, Object>> entry : cacheCourseMap.entrySet()) {
				int avrg = entry.getKey();
				Map<String, Object> m = entry.getValue();
				Date startDate = (Date)m.get("startDate");
				Date endDate = (Date)m.get("endDate");
				double sessionFee = (Double)m.get("sessionFee");
				int sessionOffset = (Integer)m.get("sessionOffset");
				int numSession = (Integer)m.get("numSession");
				String tokenCcy = (String)m.get("tokenCcy");
				Map<Integer, Boolean> sMap = new HashMap<Integer, Boolean>(); //key: sessionRg
				if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && endDate.compareTo(DateUtil.today()) >= 0) {
					sMap = new HashMap<Integer, Boolean>();
					Date startDate1 = (startDate.compareTo(DateUtil.today()) > 0) ? startDate : DateUtil.today();
					//query session
					tr = getBr().getSelectUtil().getQueryResult("select essn_rg from essession where essn_avrg = ? and essn_date between ? and ?" , 
										new Wherecl().appendArgument(avrg)
													.appendArgument(sdf.format(startDate1))
													.appendArgument(sdf.format(endDate)));
					for (int i = 0; i < tr.getRecordCount(); i++) {
						tr.setRecPointer(i);
						int sessionRg = tr.getFieldInt("essn_rg");
						sMap.put(sessionRg, false);
					}
				}
				//calc session completed count(Present Count+Absent Count)
				Set<Integer> aSet = new HashSet<Integer>();
				int completedCount = 0;
				int sessionOverdue = 0;
				Date lastAttDate = null;
				int n = sessionOffset;
				if (n < 0)
					n = 0;
				//query attendance
				tr = getBr().getSelectUtil().getQueryResult("select esat_snrg, esat_status, essn_date from esattendance, essession "
						+ "where esat_attype = 'SD' and esat_atrg = ? and essn_avrg = ? and essn_rg = esat_snrg" , 
						new Wherecl().appendArgument(studentRg)
								.appendArgument(avrg));
				for (int i = 0; i < tr.getRecordCount(); i++) {
					tr.setRecPointer(i);
					int sessionRg1 = tr.getFieldInt("esat_snrg");
					String attStatus = tr.getFieldString("esat_status");
					Date attDate = tr.getFieldDate("essn_date");
					if (lastAttDate == null || lastAttDate.compareTo(attDate) < 0)
						lastAttDate = attDate;
					if (StringUtils.equalsAny(attStatus, "Present", "Absent")) {
						completedCount++;
						if (!DateUtil.isDateNull(endDate) && !DateUtil.isDateNull(attDate) && attDate.compareTo(endDate) > 0)
							sessionOverdue++;
						if (numSession > 0) {
							if (++n > numSession)
								n = 1;
						}
					}
					aSet.add(sessionRg1);
				}
				m.put("essbsd_sesscomp", completedCount);
				m.put("essbsd_sessoverdue", sessionOverdue);

				//calc session remain (By Session No) 
				int sessionRemainBySessNo = 0;
				if (numSession > 0 && n < numSession && lastAttDate != null) {
					/*Date date = lastAttDate;
					//query session
					tr = getBr().getSelectUtil().getQueryResult("select count(*) from essession where essn_avrg = ? and essn_date > ?" , 
										new Wherecl().appendArgument(avrg)
													.appendArgument(sdf.format(date)));
					if (tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
						sessionRemainBySessNo = Math.min((Integer)tr.getField(0), numSession - n);
					}*/
					sessionRemainBySessNo = numSession - n;
				}
				m.put("essbsd_sessrebysn", sessionRemainBySessNo);
				
				//calc session remain (By Subscription End) 
				for (int rg : aSet) {
					if (sMap.containsKey(rg))
						sMap.put(rg, true);
				}
				int sessionRemain = 0;
				for (Map.Entry<Integer, Boolean> et : sMap.entrySet()) {
					if (!et.getValue())
						sessionRemain++;
				}
				m.put("essbsd_sessremain", sessionRemain);
				
				//calc tentative balance
				double tentativeBalance = sessionRemain * sessionFee;
				m.put("essbsd_tentatibal", tentativeBalance);

				//sum tentative balance
				Double ttb = tMap.get(tokenCcy);
				if (ttb == null)
					tMap.put(tokenCcy, tentativeBalance);
				else
					tMap.put(tokenCcy, ttb + tentativeBalance);
			}
			for (Map.Entry<Integer, Map<String, Object>> entry : cacheCourseMap.entrySet()) {
				Map<String, Object> m = entry.getValue();
				String tokenCcy = (String)m.get("tokenCcy");
				int numSession = (Integer)m.get("numSession");
				double sessionFee = (Double)m.get("sessionFee");
				double tokenBalance = (Double)m.get("tokenBalance");
				Double courseTentativeBalance = tMap.get(tokenCcy);
				if (courseTentativeBalance == null)
					courseTentativeBalance = 0.0;
				m.put("essbsd_tenremainba", tokenBalance - courseTentativeBalance);
				m.put("essbsd_subsfee", String.format("$%s for %d Session", feeDecFmt.format(sessionFee * numSession), numSession));
			}
		}
		Map<String, Object> map = cacheCourseMap.get(courseRg);
		//UniLog.log1("studentRg:%d, courseRg:%d, map:%s", studentRg, courseRg, map);
		return map;
	}
	/*private Map<Integer, List<Map<String, Object>>> getCacheMap() {
		if (getBr() instanceof BiResultStudentTokenBal)
			return ((BiResultStudentTokenBal)getBr()).getSessionBalanceCacheMap();
		return null;
	}
	private Map<String, Object> calcSessionBalance(Vector p_args) throws Exception {
		int sid = getSid();
		int studentRg = ((Double)p_args.get(0)).intValue();
		int courseRg = ((Double)p_args.get(1)).intValue();
		Date startDate = (Date)p_args.get(2);
		Date endDate = (Date)p_args.get(3);
		double sessionFee = (Double)p_args.get(4);
		Map<Integer, List<Map<String, Object>>> cacheMap = getCacheMap();
		if (sid == 0 || studentRg == 0 || courseRg == 0 || cacheMap == null) {
			//UniLog.log("skip not ready");
			return null;
		}
		//UniLog.log1("sid:%d, studentRg:%d, courseRg:%d, startDate:%s, endDate:%s, sessionFee:%f, cacheMap:%b", sid, studentRg, courseRg, startDate, endDate, sessionFee, cacheMap != null);
		List<Map<String, Object>> mapList = cacheMap.get(sid);
		Map<String, Object> map = null;
		if (mapList != null) {
			for (Map<String, Object> m : mapList) {
				if ((Integer)m.get("studentRg") == studentRg && (Integer)m.get("courseRg") == courseRg 
						&& ((Date)m.get("startDate")).compareTo(startDate) == 0 && ((Date)m.get("endDate")).compareTo(endDate) == 0 
						&& (Double)m.get("sessionFee") == sessionFee) {
					//UniLog.log("skip return cache");
					return m;
				}
			}
			map = new HashMap<String, Object>();
			mapList.add(map);
		}
		else {
			mapList = new ArrayList<Map<String, Object>>();
			map = new HashMap<String, Object>();
			cacheMap.put(sid, mapList);
			mapList.add(map);
		}
		//UniLog.log1("process sid:%d, studentRg:%d, courseRg:%d, startDate:%s, endDate:%s, sessionFee:%f", sid, studentRg, courseRg, startDate, endDate, sessionFee);
		
		Map<Integer, Boolean> sMap = (Map<Integer, Boolean>)map.get("sMap");
		if (sMap == null || ((Date)map.get("startDate")).compareTo(startDate) != 0 || ((Date)map.get("endDate")).compareTo(endDate) != 0) {
			sMap = null;
			//calc session remain
			if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && endDate.compareTo(DateUtil.today()) >= 0) {
				sMap = new HashMap<Integer, Boolean>();
				Date startDate1 = (startDate.compareTo(DateUtil.today()) > 0) ? startDate : DateUtil.today();
				TableRec tr = getBr().getSelectUtil().getQueryResult("select essn_rg from essession where essn_avrg = ? and essn_date between ? and ?" , 
						new Wherecl().appendArgument(courseRg)
						.appendArgument(sdf.format(startDate1))
						.appendArgument(sdf.format(endDate)));
				for (int i = 0; i < tr.getRecordCount(); i++) {
					tr.setRecPointer(i);
					int sessionRg = tr.getFieldInt("essn_rg");
					sMap.put(sessionRg, false);
				}
			}
			map.put("sMap", sMap);
		}

		//calc session completed count(Present Count+Absent Count)
		Set<Integer> tSet = (Set<Integer>)map.get("tSet");
		if (tSet == null || (Integer)map.get("studentRg") != studentRg || (Integer)map.get("courseRg") != courseRg) {
			tSet = new HashSet<Integer>();
			int completedCount = 0;
			int sessionOverdue = 0;
			TableRec tr = getBr().getSelectUtil().getQueryResult("select esat_snrg, esat_status, essn_date from esattendance, essession "
					+ "where esat_attype = 'SD' and esat_atrg = ? and essn_avrg = ? and essn_rg = esat_snrg" , 
					new Wherecl().appendArgument(studentRg)
							.appendArgument(courseRg));
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				int sessionRg1 = tr.getFieldInt("esat_snrg");
				String attStatus = tr.getFieldString("esat_status");
				Date attDate = tr.getFieldDate("essn_date");
				if (StringUtils.equalsAny(attStatus, "Present", "Absent")) {
					completedCount++;
					if (!DateUtil.isDateNull(endDate) && !DateUtil.isDateNull(attDate) && attDate.compareTo(endDate) > 0)
						sessionOverdue++;
				}
				tSet.add(sessionRg1);
			}
			map.put("essbsd_sesscomp", completedCount);
			map.put("essbsd_sessoverdue", sessionOverdue);
			map.put("tSet", tSet);
		}
		
		if (sMap != null) {
			//UniLog.log1("studentRg:%d, courseRg:%d sMap != null", studentRg, courseRg);
			for (int rg : tSet) {
				if (sMap.containsKey(rg))
					sMap.put(rg, true);
			}
			//calc session remain 
			int sessionRemain = 0;
			for (Map.Entry<Integer, Boolean> entry : sMap.entrySet()) {
				if (!entry.getValue())
					sessionRemain++;
			}
			map.put("essbsd_sessremain", sessionRemain);

			//calc tentative balance
			double tentativeBalance = sessionRemain * sessionFee;
			map.put("essbsd_tentatibal", tentativeBalance);
		}
		else {
			map.put("essbsd_sessremain", 0);
			map.put("essbsd_tentatibal", 0.0);
		}

		map.put("studentRg", studentRg);
		map.put("courseRg", courseRg);
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("sessionFee", sessionFee);

		return map;
	}*/
	@Override
	public Object evalFunction(String p_fname,Vector p_args) throws Exception {
		//UniLog.log1("p_fname:%s", p_fname);
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		switch (funcName){
			case FUNC_getSessionRemain: {
				Map<String, Object> map = calcSessionBalance(p_args);
				if (map != null) {
					getCell("essbsd_tentatibal").set(map.get("essbsd_tentatibal"));;
					getCell("essbsd_sesscomp").set(map.get("essbsd_sesscomp"));;
					getCell("essbsd_tenremainba").set(map.get("essbsd_tenremainba"));
					getCell("essbsd_subsfee").set(map.get("essbsd_subsfee"));
					getCell("essbsd_sessrebysn").set(map.get("essbsd_sessrebysn"));
					return map.get("essbsd_sessremain");
				}
				else {
					getCell("essbsd_tentatibal").set(0.0);
					getCell("essbsd_sesscomp").set(0);
					getCell("essbsd_tenremainba").set(0);
					getCell("essbsd_subsfee").set("");
					getCell("essbsd_sessrebysn").set(0);
				}
				return 0;
			}
			case FUNC_getSessionOverdue: {
				Map<String, Object> map = calcSessionBalance(p_args);
				return map != null ? (Integer)map.get("essbsd_sessoverdue") : 0;
			}
			case FUNC_getSessionRemainBySessNo: {
				Map<String, Object> map = calcSessionBalance(p_args);
				return map != null ? (Integer)map.get("essbsd_sessrebysn") : 0;
			}
		}
		return(super.evalFunction(p_fname,p_args) );
	}
}
