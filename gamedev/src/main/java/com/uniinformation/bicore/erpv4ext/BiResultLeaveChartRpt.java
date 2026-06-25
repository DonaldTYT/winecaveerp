package com.uniinformation.bicore.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLeaveChartRpt extends BiResult {
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat ddf1 = new SimpleDateFormat("yyyyMMdd");
	private Map<String, Map<Date, Pair<String, Double>>> cacheMap = new HashMap<String, Map<Date, Pair<String, Double>>>(); //key: eid, pair: (leavecode, leaveday)

	private boolean afterLoadSerialMapFlag = false;
	private Date periodStartDate, periodEndDate;

	public BiResultLeaveChartRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}
	
	@Override
	protected void afterLoadCollection(boolean p_isFetch, BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		try {
			String eid = p_cc.getString("em_eid");
			Map<Date, Pair<String, Double>> m = getLeaveMap(eid);
			double total = 0.0;
			for (Enumeration<String> en = p_cc.getCellTable().keys(); en.hasMoreElements(); ) {
				String key = en.nextElement();
				if (StringUtils.startsWith(key, "em_xdate")) {
					Date date = ddf1.parse(key.substring(8));
					Pair<String, Double> p = m.get(date);
					if (p != null) {
						total += p.getRight();
						p_cc.getCell(key).set(p.getLeft() + (p.getRight() == 0.5 ? "(H)" : ""));
					} else
						p_cc.getCell(key).set("");
				}
			}
			p_cc.getCell("em_xtotal").set(total);
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
		cacheMap.clear();
	}

	private Map<Date, Pair<String, Double>> getLeaveMap(String eid) throws Exception {
		if (!afterLoadSerialMapFlag)
			return new HashMap<Date, Pair<String, Double>>();
		Map<Date, Pair<String, Double>> m = cacheMap.get(eid);
		if (m == null) {
			m = new HashMap<Date, Pair<String, Double>>();
			cacheMap.put(eid, m);

			TableRec tr = getSelectUtil().getQueryResult("select * from leave where lv_eid = ? and lv_sdate <= ? and lv_edate >= ?", 
					new Wherecl().appendArgument(eid).appendArgument(ddf.format(periodEndDate)).appendArgument(ddf.format(periodStartDate)));
			if (tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				Date sdate = tr.getFieldDate("lv_sdate");
				Date edate = tr.getFieldDate("lv_edate");
				String stfd = tr.getFieldString("lv_stfd");
				String enfd = tr.getFieldString("lv_enfd");
				String reason = tr.getFieldString("lv_reason");
				if (StringUtils.equals(stfd, "F"))
					m.put(sdate, Pair.of(reason, 1.0));
				else if (StringUtils.equals(stfd, "H"))
					m.put(sdate, Pair.of(reason, 0.5));
				for (Date date = DateUtil.nextday(sdate); date.compareTo(edate) < 0; date = DateUtil.nextday(date))
					m.put(date, Pair.of(reason, 1.0));
				if (StringUtils.equals(enfd, "F"))
					m.put(edate, Pair.of(reason, 1.0));
				else if (StringUtils.equals(enfd, "H"))
					m.put(edate, Pair.of(reason, 0.5));
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
