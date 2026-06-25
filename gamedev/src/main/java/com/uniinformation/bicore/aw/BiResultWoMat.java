package com.uniinformation.bicore.aw;

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

public class BiResultWoMat extends BiResult {
	public BiResultWoMat(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultWorkOrder");
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		BiResultWorkOrder awWorkOrder = (BiResultWorkOrder) getParent();
//		//biStmovOm.setCellActionCalTotalAmount( (ColumnCell) col.getCell("stmd_exprice") );
		awWorkOrder.setSyncMatType((ColumnCell) (col.getCell("wm_matname")));
		awWorkOrder.setSyncMatSize((ColumnCell) (col.getCell("wm_matsize")));
	}
}
