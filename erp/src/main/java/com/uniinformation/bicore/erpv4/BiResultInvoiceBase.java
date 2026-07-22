package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultInvoiceBase extends BiResultErpv4 {

	String indLinkName=null;
	public BiResultInvoiceBase(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
		if(getSubLinks() != null) {
		for(BiResult sr : getSubLinks()) {
			if(sr.getView().getTable().getName().equals("invdet")) {
				indLinkName = sr.getView().getName();
				break;
			}
		}
		}
	}
	
	public String getIndLinkName() {
		return indLinkName;
	}
	
}
