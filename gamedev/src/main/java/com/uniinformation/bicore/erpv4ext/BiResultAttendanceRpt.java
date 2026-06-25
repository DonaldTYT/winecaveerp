package com.uniinformation.bicore.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord.Calot;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord.CalotAttendDetItem;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiTranslateHelper;

public class BiResultAttendanceRpt extends BiResult {
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	public static final int DEFAULT_ATTENDDET_INOUT_COLUMN_COUNT = 3;
	public static final int MAX_ATTENDDET_INOUT_COLUMN_COUNT = 9;
	private Map<String, List<CalotAttendDetItem>> attendDetCacheMap = new HashMap<String, List<CalotAttendDetItem>>(); //key: eid-date
	private int showAttendDetInOutCount = DEFAULT_ATTENDDET_INOUT_COLUMN_COUNT;
	private boolean afterLoadSerialMapFlag = false;
	
	private BiResult brAttendDet;

	public BiResultAttendanceRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
		try {
			for (int i = 3; i < MAX_ATTENDDET_INOUT_COLUMN_COUNT; i++) {
				addTempColumn("at_xattintype" + i, "", "", "", "char",null, 1, "at_xsumot");
				addTempColumn("at_xattin" + i, ZkBiTranslateHelper.getText(sh, "ERPV4EXT.ATTENDANCERPT.AT_XATTIN0", "LABEL", "In"), "", "", "char", null,6, "at_xsumot");
				addTempColumn("at_xattouttype" + i, "", "", "", "char", null,1, "at_xsumot");
				addTempColumn("at_xattout" + i, ZkBiTranslateHelper.getText(sh, "ERPV4EXT.ATTENDANCERPT.AT_XATTOUT0", "LABEL", "Out"), "", "", "char", null,6, "at_xsumot");
			}
		} catch (Exception e) {
			UniLog.log(e);
		}
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		UniLog.log("createColumnCollection");
		return(new Erpv4AttendanceRptCellColletion(p_parent, this));
	}

	@Override
	public String getColumnDisplayString(ColumnCell p_cell) {
		if (StringUtils.equalsAny(p_cell.getCellLabel(), "at_xsumot", "at_xdreallate", "at_xleaveearly", "at_xdnowork", "at_xdwktime")) {
			int i = p_cell.getInt();
			return i != 0 ? String.format("%02d:%02d", i / 60, i % 60) : "";
		}
		return super.getColumnDisplayString(p_cell);
	}

	public Map<String, List<CalotAttendDetItem>> getAttendDetCacheMap() {
		return afterLoadSerialMapFlag ? attendDetCacheMap : null;
	}

	public int getShowAttendDetInOutCount() {
		return showAttendDetInOutCount;
	}
	
	private void clearCacheMap() {
		UniLog.log("clearCacheMap");
		attendDetCacheMap.clear();
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		//clear cache map when perform query/refresh
		ReturnMsg rtn = super.afterLoadSerialMap();
		if (!rtn.getStatus()) return rtn;
		
		clearCacheMap();
		afterLoadSerialMapFlag = true;

		try {
			TableRec tr = resultTr;
			UniLog.log1("getRowCount:%d", tr.getRecordCount());
			
			//set date range
			Map<String, RangeSet<Date>> m = new HashMap<String, RangeSet<Date>>();
			for (int i = 0; i < tr.getRecordCount(); i++) {
				String eid = tr.getFieldString("at_eid", i);
				Date date = tr.getFieldDate("at_date", i);
				RangeSet<Date> rs = m.get(eid);
				if (rs == null) {
					rs = TreeRangeSet.create();
					m.put(eid, rs);
				}
				rs.add(Range.openClosed(date, DateUtil.nextday(date)));
			}

			//fill all attenddet data to attendDetCacheMap
			for (Map.Entry<String, RangeSet<Date>> entry : m.entrySet()) {
				String eid = entry.getKey();
				RangeSet<Date> rs = entry.getValue();
				Set<Range<Date>> s = rs.asRanges();
				for (Range<Date> r : s) {
					Date startDate = r.lowerEndpoint();
					Date endDate = DateUtil.prevday(r.upperEndpoint());
					String str = startDate.compareTo(endDate) == 0 
							? String.format("atd_date = '%s'", ddf.format(startDate)) 
							: String.format("atd_date between '%s' and '%s'", ddf.format(startDate), ddf.format(endDate));
					brAttendDet = BiResultHelper.create(sh, "erpv4ext.AttendDet2", brAttendDet, String.format("atd_eid = '%s' and %s", eid, str), null, -1, 
							new ArrayList(Arrays.asList(Pair.of("atd_eid", false), Pair.of("atd_date", false), Pair.of("atd_time", false))), false);
					while (brAttendDet != null && brAttendDet.next()) {
						Date date = brAttendDet.getCellDate("atd_date");
						Date time = brAttendDet.getCellDate("atd_time");
						String flag = brAttendDet.getCellString("atd_flag");
						String atype = brAttendDet.getCellString("atd_atype");
						String key = eid + "-" + ddf.format(date);
						List<CalotAttendDetItem> attDetList = attendDetCacheMap.get(key);
						if (attDetList == null) {
							attDetList = new ArrayList<CalotAttendDetItem>();
							attendDetCacheMap.put(key, attDetList);
							UniLog.log1("key:%s", key);
						}
						Calot.addAttendDetWithFlagIntoList(AttendanceRecord.dateToMinute(time), flag, atype, attDetList);
					}
				}
				for (Range<Date> r : s) {
					for (Date date = r.lowerEndpoint(); date.compareTo(r.upperEndpoint()) < 0; date = DateUtil.nextday(date)) {
						String key = eid + "-" + ddf.format(date);
						if (!attendDetCacheMap.containsKey(key))
							attendDetCacheMap.put(key, new ArrayList<CalotAttendDetItem>());
					}
				}
			}

		}
		catch (Exception e) {
			UniLog.log(e);
		}
		
		return(ReturnMsg.defaultOk);
	}

	@Override
	protected ReturnMsg afterLoadSerialMap2() {
		ReturnMsg rtn = super.afterLoadSerialMap2();
		if (!rtn.getStatus()) return rtn;

		//set showAttendDetInOutCount
		showAttendDetInOutCount = DEFAULT_ATTENDDET_INOUT_COLUMN_COUNT;
		try {
			TableRec tr = resultTr;
			UniLog.log1("getRowCount:%d", tr.getRecordCount());
			
			for (int i = 0; i < tr.getRecordCount(); i++) {
				String eid = tr.getFieldString("at_eid", i);
				Date date = tr.getFieldDate("at_date", i);
				List<CalotAttendDetItem> list = attendDetCacheMap.get(eid + "-" + ddf.format(date));
				if (list != null)
					showAttendDetInOutCount = Math.max(list.size(), showAttendDetInOutCount);
			}
		}
		catch (Exception e) {
			UniLog.log(e);
		}

		return(ReturnMsg.defaultOk);
	}
}
