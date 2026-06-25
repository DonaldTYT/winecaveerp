package com.uniinformation.bicore.erpv4ext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellVector;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.jxapp.erpv4ext.AttendanceRecord.AttendanceRecalc;

public class BiResultAttendanceRecord extends BiResult {
	private Date periodStartDate, periodEndDate;
	private Map<Date, List<Map<String, Date>>> manualAttDetMap = new HashMap<Date, List<Map<String, Date>>>();

	public BiResultAttendanceRecord(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			String eid = getCellString("em_eid");
			String attmode = getCellString("em_yflag");
			Vector<BiCellCollection> recs = getSubLinkResult("erpv4ext.Attendance");
			for (BiCellCollection cc : recs) {
				Date date = cc.getDate("at_date");
				boolean xflag3 = cc.getBoolean("at_xflag3");
				boolean flag4 = cc.getBoolean("at_flag4");
				List<Map<String, Date>> list = manualAttDetMap.get(date);
				if (xflag3 && list != null) {
					Wherecl wherecl = new Wherecl().appendArgument(eid).appendArgument(date);
					su.executeUpdate("delete from attenddet where atd_eid = ? and atd_adate = ? and atd_atype = '00'", wherecl);
					if (!list.isEmpty()) {
						for (Map<String, Date> m : list) {
							Date inTime = m.get("IN");
							Date outTime = m.get("OU");
							Date inDateTime = AttendanceRecord.unionDateTime(date, inTime);
							Date outDateTime = AttendanceRecord.unionDateTime(date, outTime);
							su.executeUpdate("insert into attenddet (atd_eid, atd_date, atd_time, atd_flag, atd_atype, atd_atime, atd_adate, atd_oflag) "
									+ "values(?, ?, ?, 'IN', '00', ?, ?, '')", 
									new Wherecl().appendArgument(eid).appendArgument(date).appendArgument(inTime.getTime() / 1000).appendArgument(inDateTime.getTime() / 1000).appendArgument(date));
							su.executeUpdate("insert into attenddet (atd_eid, atd_date, atd_time, atd_flag, atd_atype, atd_atime, atd_adate, atd_oflag) "
									+ "values(?, ?, ?, 'OU', '00', ?, ?, '')", 
									new Wherecl().appendArgument(eid).appendArgument(date).appendArgument(outTime.getTime() / 1000).appendArgument(outDateTime.getTime() / 1000).appendArgument(date));
						}
					}
					else {
						su.executeUpdate("update attenddet set atd_flag = atd_oflag where atd_eid = ? and atd_date = ? and atd_atype <> '00'", wherecl);
						su.executeUpdate("update attenddet set atd_oflag = '' where atd_eid = ? and atd_date = ? and atd_atype <> '00'", wherecl);
					}
				}
				cc.getCell("at_xflag3").set(false);
				AttendanceRecord.setEmlvrCompensation(su, eid, date, flag4);
			}
			AttendanceRecalc attl = new AttendanceRecalc(su, eid, attmode, periodStartDate, periodEndDate);
			if (attl.start())
				attl.finish();
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}

		return rtn;
	}

	public void setQueryPeriod(Date startDate, Date endDate) {
		periodStartDate = startDate;
		periodEndDate = endDate;
	}

	public Date getQueryPeriodStartDate() {
		return periodStartDate;
	}

	public Date getQueryPeriodEndDate() {
		return periodEndDate;
	}
	
	public void clearManualAttDetMap() {
		manualAttDetMap.clear();
	}

	public List<Map<String, Date>> getManualAttDet(Date date) {
		return manualAttDetMap.get(date);
	}

	public void putManualAttDet(Date date, List<Map<String, Date>> m) {
		manualAttDetMap.put(date, m);
	}

}
