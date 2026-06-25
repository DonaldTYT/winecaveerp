package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsCustomer extends com.uniinformation.bicore.erpv4.BiResultCustomer{
	public BiResultAfsCustomer(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("BiResultAfsCustomerUsed");
	}
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		if(stockViewId == null) stockViewId = "AfsStock";
		super.createColumnCells(p_col);
	}
}
