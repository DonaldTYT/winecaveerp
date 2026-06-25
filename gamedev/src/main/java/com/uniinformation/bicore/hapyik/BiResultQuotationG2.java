package com.uniinformation.bicore.hapyik;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.webcore.SessionHelper;

public class BiResultQuotationG2 extends com.uniinformation.bicore.erpv4.BiResultQuotationG2 {


	public BiResultQuotationG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getNewOrderNumber(java.util.Date p_date) throws Exception {
		java.util.Date d = p_date;
		String s = "";
		String ds = DateUtil.toDateString(d, "yyyymmdd");
		int nextidx = 1;
		TableRec tr = su.getQueryResult("select inv_invno from quotation where inv_invno matches '" + ds + "*' order by inv_invno desc",null);
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			s = tr.getField("inv_invno").toString();
			String ss = StringUtil.strpart(s, 8, -1);
			nextidx = Integer.parseInt(ss) + 1;
		}
		s = String.format("%s%03d",ds, nextidx);
		return(s);
	}
}
