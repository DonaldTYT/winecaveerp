package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsJobEngineer extends BiResult{
	CellValueAction calTotalWorkTime = null;
	public BiResultAfsJobEngineer (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList,p_whereStr,p_sh);
		UniLog.log("BiResultJobEngineer Used");
		calTotalWorkTime = new CellValueAction() {
			public void cellAction_onchange(Cell p_cell) throws CellException {
				if(!isActionEnabled()) return;
				ColumnCell cl = (ColumnCell) p_cell;
				BiResultAfsServiceJob br = (BiResultAfsServiceJob) getParent();
				br.updateWorkTime();
			}
			public void cellAction_onfree() {
				
			}
		};
		BiResultAfsServiceJob br = (BiResultAfsServiceJob) getParent();
		br.addSublinkAction(this.getView().getName(),calTotalWorkTime);
		
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		col.getCell("svjobegr_worktime").addAction(calTotalWorkTime);
	}
}
