package com.uniinformation.bicore.erpv4ext;

import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultEmgProvidentFund extends BiResult {

	public BiResultEmgProvidentFund(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		if (getParent() != null && StringUtils.equals(getParent().getView().getName(), "erpv4ext.PromotionTransHdr"))
			p_where.appendString(" and empe_date <= today and empe_enddate >= today ");
		return super.addExtraWhereStr(p_where, p_hash);
	}
}
