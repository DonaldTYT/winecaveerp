package com.uniinformation.bicore.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.erpv4ext.BiResultEmColligateRpt.SumAttendItem;
import com.uniinformation.utils.UniLog;

public class Erpv4EmColligateRptCellColletion extends BiCellCollection {
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private static enum FuncName { FUNC_getSumTime, FUNC_getRemark, NOT_DEFINED }

	private BiResult brAttendance = null;
	
	public Erpv4EmColligateRptCellColletion(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
		UniLog.log1("called");
	}

	private Map<String, SumAttendItem> getSumAttendItemCacheMap() {
		if (getBr() instanceof BiResultEmColligateRpt)
			return ((BiResultEmColligateRpt)getBr()).getSumAttendCacheMap();
		return null;
	}

	private SumAttendItem getSumAttendItem(String eid, Date emStDate, Date emEndDate) throws Exception {
		Map<String, SumAttendItem> cacheMap = getSumAttendItemCacheMap();
		if (cacheMap == null)
			return null;
		SumAttendItem item = cacheMap.get(eid);
		if (item == null) {
			item = new SumAttendItem();
			cacheMap.put(eid, item);

			BiResultEmColligateRpt br = (BiResultEmColligateRpt)getBr();
			boolean remarkFlag = false;
			brAttendance = BiResultHelper.create(br.getSessionHelper(), "erpv4ext.ScheduleRptAtt", brAttendance, 
					String.format("at_eid = '%s' and at_date between '%s' and '%s'", eid, ddf.format(br.getPeriodStartDate()), ddf.format(br.getPeriodEndDate())), null, -1, null, false);
			while (brAttendance != null && brAttendance.next()) {
				Date date = brAttendance.getCellDate("at_date");
				if (date.compareTo(emStDate) >= 0 && (DateUtil.isDateNull(emEndDate) || date.compareTo(emEndDate) <= 0)) {
					String shiftCode = brAttendance.getCellString("at_shiftcode");
					int wktime = brAttendance.getCellInt("at_wktime");
					int sot = brAttendance.getCellInt("at_sot");
					int late = brAttendance.getCellInt("at_reallate");
					int lvearly = brAttendance.getCellInt("at_xleaveearly");
					int ot = brAttendance.getCellInt("at_ot");
					int nowork = brAttendance.getCellInt("at_nowork");
					boolean manualot = brAttendance.getCell("at_manualot").getBoolean();
					int othr = brAttendance.getCellInt("at_othr");
					String cddesc = brAttendance.getCellString("cd_desc");
					String remark = brAttendance.getCellString("at_evectionreason");
					//UniLog.log1("date:%s, wktime:%d, sot:%d, ot:%d, late:%d, lvearly:%d, nowork:%d", date, wktime, sot, ot, late, lvearly, nowork);
					if (manualot) {
						if (StringUtils.equals(shiftCode, "-") || StringUtils.isNoneBlank(cddesc)) {
							sot = 0;
							ot = othr;
						} else {
							sot = othr;
							ot = 0;
						}
					}
					if (wktime > 0) item.workDay++;
					item.workMin += wktime;
					if (sot > 0) item.sotDay++;
					item.sotMin += sot;
					if (late > 0) item.lateDay++;
					item.lateMin += late;
					if (lvearly > 0) item.lvearlyDay++;
					item.lvearlyMin += lvearly;
					if (ot > 0) item.otDay++;
					item.otMin += ot;
					if (nowork > 0) item.noworkDay++;
					item.noworkMin += nowork;
					if (!remarkFlag) {
						item.remark = remark;
						remarkFlag = true;
					}
				}
			}
		}
		return item;
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
			case FUNC_getSumTime:
				String refKey = (String)p_args.get(0);
				String eid = (String)p_args.get(1);
				Date emStDate = (Date)p_args.get(2);
				Date emEndDate = (Date)p_args.get(3);
				SumAttendItem item;
				if (sid == 0 || StringUtils.isBlank(eid) || !DateUtil.isValid(emStDate) || (item = getSumAttendItem(eid, emStDate, emEndDate)) == null)
					return 0;
				UniLog.log1("refKey:%s, eid:%s, emStDate:%s, emEndDate:%s", refKey, eid, emStDate, emEndDate);
				if (StringUtils.equals(refKey, "em_xworkdays"))
					return item.workDay;
				if (StringUtils.equalsAny(refKey, "em_xworkmins", "em_xqworkmins"))
					return item.workMin;
				if (StringUtils.equals(refKey, "em_xsotdays"))
					return item.sotDay;
				if (StringUtils.equalsAny(refKey, "em_xsotmins", "em_xqsotmins"))
					return item.sotMin;
				if (StringUtils.equals(refKey, "em_xlatedays"))
					return item.lateDay;
				if (StringUtils.equalsAny(refKey, "em_xlatemins", "em_xqlatemins"))
					return item.lateMin;
				if (StringUtils.equals(refKey, "em_xlvearlydays"))
					return item.lvearlyDay;
				if (StringUtils.equalsAny(refKey, "em_xlvearlymins", "em_xqlvearlymins"))
					return item.lvearlyMin;
				if (StringUtils.equals(refKey, "em_xotdays"))
					return item.otDay;
				if (StringUtils.equalsAny(refKey, "em_xotmins", "em_xqotmins"))
					return item.otMin;
				if (StringUtils.equals(refKey, "em_xnoworkdays"))
					return item.noworkDay;
				if (StringUtils.equalsAny(refKey, "em_xnoworkmins", "em_xqnoworkmins"))
					return item.noworkMin;
				return 0;
			case FUNC_getRemark:
				eid = (String)p_args.get(0);
				emStDate = (Date)p_args.get(1);
				emEndDate = (Date)p_args.get(2);
				if (sid == 0 || StringUtils.isBlank(eid) || !DateUtil.isValid(emStDate) || (item = getSumAttendItem(eid, emStDate, emEndDate)) == null)
					return "";
				UniLog.log1("FUNC_getRemark eid:%s, emStDate:%s, emEndDate:%s", eid, emStDate, emEndDate);
				return item.remark;
		}
		return super.evalFunction(p_fname, p_args);
	}
}
