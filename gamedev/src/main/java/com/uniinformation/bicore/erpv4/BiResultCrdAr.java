package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCrdAr extends BiResultErpv4 {

	public BiResultCrdAr(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		BiResultCrhAr parentBr = (BiResultCrhAr) getParent();
		parentBr.setCellActionCal_Amount(p_col.getCell("crd_amount"));
		parentBr.setCellActionCal_Amount(p_col.getCell("crd_lamount"));
		parentBr.setCellActionCal_Amount(p_col.getCell("crd_cid"));
//		parentBr.setCellActionCal_lAmount(p_col.getCell("crd_lamount"));
	}
	@Override
	public String getPickColumnCondition(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("sih_sno")) {
			return("sih_vcode = '"+p_cc.getCollection().getCellString("crh_vcode")+"'");
		}
		return(null);
	}
}
