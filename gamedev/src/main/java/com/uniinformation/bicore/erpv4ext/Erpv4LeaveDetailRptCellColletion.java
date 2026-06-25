package com.uniinformation.bicore.erpv4ext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;

public class Erpv4LeaveDetailRptCellColletion extends BiCellCollection {
	private enum FuncName { FUNC_getStartTime, FUNC_getEndTime, FUNC_getNDay, FUNC_getSumDetNDay, NOT_DEFINED }

	public Erpv4LeaveDetailRptCellColletion(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
		UniLog.log1("called");
	}

	private Map<String, Map<Date, Double>> getLeaveSumDetNDayCacheMap() {
		if (getBr() instanceof BiResultLeaveDetailRpt)
			return ((BiResultLeaveDetailRpt)getBr()).getLeaveSumDetNDayCacheMap();
		return null;
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
			case FUNC_getStartTime:
				if (sid == 0)
					return "";
				Date sttime = (Date)p_args.get(0);
				String stfd = (String)p_args.get(1);
				UniLog.log1("sttime:%s, stfd:%s", sttime, stfd);
				return StringUtils.equalsAny(stfd, "F", "H") ? "" : DateUtil.dateDigtalToTimeStr(sttime, false);
			case FUNC_getEndTime:
				if (sid == 0)
					return "";
				Date endtime = (Date)p_args.get(0);
				stfd = (String)p_args.get(1);
				UniLog.log1("endtime:%s, stfd:%s", endtime, stfd);
				return StringUtils.equalsAny(stfd, "F", "H") ? "" : DateUtil.dateDigtalToTimeStr(endtime, false);
			case FUNC_getNDay:
				if (sid == 0)
					return 0.0;
				int leaveunit = ((Double)p_args.get(0)).intValue();
				UniLog.log1("leaveunit:%d", leaveunit);
				return NumberUtils.toDouble(LeaveApplication.getLeaveUnit2LvStr(leaveunit));
			case FUNC_getSumDetNDay:
				String eid = (String)p_args.get(0);
				Date sdate = (Date)p_args.get(1);
				Map<String, Map<Date, Double>> cacheMap = getLeaveSumDetNDayCacheMap();
				UniLog.log1("eid:%s, sdate:%s, cacheMap:%b", eid, sdate, cacheMap != null);
				if (sid == 0 || StringUtils.isBlank(eid) || !DateUtil.isValid(sdate) || cacheMap == null)
					return 0.0;
				Double nday = null;
				Map<Date, Double> cacheDateMap = cacheMap.get(eid);
				if (cacheDateMap != null)
					nday = cacheDateMap.get(sdate);
				if (nday == null) {
					TableRec tr = getBr().getSelectUtil().getQueryResult("select sum(lvd_nday) from leavedet where lvd_eid = ? and lvd_sdate = ?", new Wherecl().appendArgument(eid).appendArgument(sdate));
					if (tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
						nday = (Double)tr.getField(0);
						UniLog.log1("nday:%f", nday);
						if (cacheDateMap == null)
							cacheDateMap = new HashMap<Date, Double>();
						cacheDateMap.put(sdate, nday);
						cacheMap.put(eid, cacheDateMap);
					}
				}
				return nday != null ? nday : 0.0;
		}
		return super.evalFunction(p_fname, p_args);
	}
}
