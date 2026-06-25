package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPostDate extends BiResultErpv4 {

	public BiResultPostDate(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	/* use this for post process validataion after add/update to database */
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		getSessionHelper().removeSessionData("STMMINDATE");
		return(super.biAfterAddUpdateCurrent(col, isUpdate));
	}
	
}
