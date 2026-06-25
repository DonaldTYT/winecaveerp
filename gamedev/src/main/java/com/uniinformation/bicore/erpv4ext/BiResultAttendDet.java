package com.uniinformation.bicore.erpv4ext;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAttendDet extends BiResult {

	public BiResultAttendDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		if (getParent() != null && StringUtils.equals(getParent().getView().getName(), "erpv4ext.InoutRecord")) {
			BiResultInoutRecord parentBr = (BiResultInoutRecord)getParent();
			Date periodStartDate = parentBr.getQueryPeriodStartDate();
			Date periodEndDate = parentBr.getQueryPeriodEndDate();
			String eid = parentBr.getCellString("em_eid");
			if (!DateUtil.isValid(periodStartDate))
				periodStartDate = DateUtil.monthStart(DateUtil.today());
			if (!DateUtil.isValid(periodEndDate))
				periodEndDate = DateUtil.monthEnd(DateUtil.today());
			parentBr.setQueryPeriod(periodStartDate, periodEndDate);
			int sttime = (int)(periodStartDate.getTime() / 1000);
			int endtime = (int)(DateUtil.nextday(periodEndDate).getTime() / 1000 - 1);
			UniLog.log1("periodStartDate:%s, periodEndDate:%s, sttime:%d, endtime:%d, eid:%s", periodStartDate, periodEndDate, sttime, endtime, eid);
			p_where.andRange("atd_atime", sttime, endtime);
		}
		return super.addExtraWhereStr(p_where, p_hash);
	}
}
