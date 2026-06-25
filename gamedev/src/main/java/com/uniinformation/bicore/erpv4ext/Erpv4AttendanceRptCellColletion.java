package com.uniinformation.bicore.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord.Calot;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord.CalotAttendDetItem;
import com.uniinformation.utils.UniLog;

public class Erpv4AttendanceRptCellColletion extends BiCellCollection {
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private static enum FuncName { FUNC_getDayOfWeek, /*FUNC_getHHmmTime, */FUNC_getWorkTime, FUNC_getAttDetTime, FUNC_getAttDetType, NOT_DEFINED }

	private BiResult brAttendDet = null;
	
	public Erpv4AttendanceRptCellColletion(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
		UniLog.log1("called");
	}

	private Map<String, List<CalotAttendDetItem>> getAttendDetCacheMap() {
		if (getBr() instanceof BiResultAttendanceRpt)
			return ((BiResultAttendanceRpt)getBr()).getAttendDetCacheMap();
		return null;
	}

	/*private Map<String, Integer> getWorkTimeCacheMap() {
		if (getBr() instanceof BiResultAttendanceRpt)
			return ((BiResultAttendanceRpt)getBr()).getWorkTimeCacheMap();
		return null;
	}*/
	
	private String loadAttendDet(String eid, Date date, String stfCode) throws Exception {
		Map<String, List<CalotAttendDetItem>> attDetCacheMap = getAttendDetCacheMap();
		//Map<String, Integer> workTimeCacheMap = getWorkTimeCacheMap();
		if (attDetCacheMap == null)
			return null;
		String key = eid + "-" + ddf.format(date);
		List<CalotAttendDetItem> attDetList = attDetCacheMap.get(key);
		if (attDetList == null) {
			attDetList = new ArrayList<CalotAttendDetItem>();
			attDetCacheMap.put(key, attDetList);

			brAttendDet = BiResultHelper.create(br.getSessionHelper(), "erpv4ext.AttendDet", brAttendDet, String.format("atd_eid = '%s' and atd_date = '%s'", eid, ddf.format(date)), null, -1, null, false);
			while (brAttendDet != null && brAttendDet.next()) {
				Date time = brAttendDet.getCellDate("atd_time");
				String flag = brAttendDet.getCellString("atd_flag");
				String atype = brAttendDet.getCellString("atd_atype");
				Calot.addAttendDetWithFlagIntoList(AttendanceRecord.dateToMinute(time), flag, atype, attDetList);
			}
			
			/*Calot calot = new Calot(getBr().getSelectUtil());
			calot.clearAttendDet();
			for (CalotAttendDetItem item : attDetList)
				calot.addAttendDet(item.cada_intime, item.cada_outtime);
			calot.init(eid, date, stfCode);
			calot.readStatus2();
			workTimeCacheMap.put(key, calot.getWorkTime());*/
		}
		return key;
	}
	
	private static String getIndicationByAType(String aType) {
		if (StringUtils.equals(aType, "00"))
			return "M";
		if (StringUtils.equals(aType, "99"))
			return "A";
		if (StringUtils.equalsAny(aType, "12", "22", "32", "42"))
			return "*";
		return "";
	}

	@Override
	public Object evalFunction(String p_fname, Vector p_args) throws Exception {
		UniLog.log1("p_fname:%s", p_fname);
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		int sid = getSid();

		switch (funcName){
			case FUNC_getDayOfWeek:
				Date date = (Date)p_args.get(0);
				if (sid == 0 || !DateUtil.isValid(date))
					return "";
				UniLog.log1("date:%s", date);
				return AttendanceRecord.getShortDayOfWeek(this.getBr().getSessionHelper(), date);
			/*case FUNC_getHHmmTime:
				String refKey = (String)p_args.get(0);
				date = (Date)p_args.get(1);
				int min1 = ((Double)p_args.get(2)).intValue();
				int min2 = ((Double)p_args.get(3)).intValue();
				if (sid == 0 || !DateUtil.isValid(date))
					return LeaveApplication.START_TIME_IN_DAY;
				UniLog.log1("refKey:%s, date:%s, min1:%d, min2:%d", refKey, date, min1, min2);
				return AttendanceRecord.minuteToDate(min1 + min2);*/
			/*case FUNC_getWorkTime:
				String refKey = (String)p_args.get(0);
				String eid = (String)p_args.get(1);
				date = (Date)p_args.get(2);
				String sftCode = (String)p_args.get(3);
				String key = null;
				if (sid == 0 || StringUtils.isBlank(eid) || !DateUtil.isValid(date) || (key = loadAttendDet(eid, date, sftCode)) == null)
					return 0;
				UniLog.log1("refKey:%s, eid:%s, date:%s", refKey, eid, date, sftCode);
				int workTime = getWorkTimeCacheMap().get(key);
				return workTime;*/
			case FUNC_getAttDetTime:
				String refKey = (String)p_args.get(0);
				String eid = (String)p_args.get(1);
				date = (Date)p_args.get(2);
				String sftCode = (String)p_args.get(3);
				String key;
				if (sid == 0 || StringUtils.isBlank(eid) || !DateUtil.isValid(date) || (key = loadAttendDet(eid, date, sftCode)) == null)
					return "";
				List<CalotAttendDetItem> list = getAttendDetCacheMap().get(key);
				String str = "";
				int i;
				for (i = 0; i < list.size(); i++) {
					CalotAttendDetItem item = list.get(i);
					String inTimeStr = String.format("%02d:%02d", item.cada_intime / 60, item.cada_intime % 60);
					String outTimeStr = String.format("%02d:%02d", item.cada_outtime / 60, item.cada_outtime % 60);
					if (i < BiResultAttendanceRpt.DEFAULT_ATTENDDET_INOUT_COLUMN_COUNT) {
						if (StringUtils.equals(refKey, "at_xattin" + i))
							str = inTimeStr;
						else if (StringUtils.equals(refKey, "at_xattout" + i))
							str = outTimeStr;
					} else if (testCell("at_xattin" + i) != null) {
						getCell("at_xattin" + i).set(inTimeStr);
						getCell("at_xattout" + i).set(outTimeStr);
					}
				}
				for (; testCell("at_xattin" + i) != null; i++) {
					getCell("at_xattin" + i).set("");
					getCell("at_xattout" + i).set("");
				}
				return str;
			case FUNC_getAttDetType:
				refKey = (String)p_args.get(0);
				eid = (String)p_args.get(1);
				date = (Date)p_args.get(2);
				sftCode = (String)p_args.get(3);
				if (sid == 0 || StringUtils.isBlank(eid) || !DateUtil.isValid(date) || (key = loadAttendDet(eid, date, sftCode)) == null)
					return "";
				list = getAttendDetCacheMap().get(key);
				str = "";
				for (i = 0; i < list.size(); i++) {
					CalotAttendDetItem item = list.get(i);
					String inType = getIndicationByAType(item.cada_inatype);
					String outType = getIndicationByAType(item.cada_outatype);
					if (i < BiResultAttendanceRpt.DEFAULT_ATTENDDET_INOUT_COLUMN_COUNT) {
						if (StringUtils.equals(refKey, "at_xattintype" + i))
							str = inType;
						else if (StringUtils.equals(refKey, "at_xattouttype" + i))
							str = outType;
					} else if (testCell("at_xattintype" + i) != null) {
						getCell("at_xattintype" + i).set(inType);
						getCell("at_xattouttype" + i).set(outType);
					}
				}
				for (; testCell("at_xattintype" + i) != null; i++) {
					getCell("at_xattintype" + i).set("");
					getCell("at_xattouttype" + i).set("");
				}
				return str;
		}
		return super.evalFunction(p_fname, p_args);
	}
}
