package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMoCompanyTfr extends BiResultMO {


	public BiResultMoCompanyTfr(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
		extraStmds.add("stmdji");
	}
	@Override
	protected boolean allowCrossLocation() {
		return(false);
	}
	@Override
	protected boolean allowCrossCompanyLoc() {
		return(true);
	}
}
