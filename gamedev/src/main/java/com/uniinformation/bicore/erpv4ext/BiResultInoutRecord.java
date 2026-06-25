package com.uniinformation.bicore.erpv4ext;

import java.util.Date;
import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultInoutRecord extends BiResult {
	private Date periodStartDate, periodEndDate;

	public BiResultInoutRecord(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
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
}
