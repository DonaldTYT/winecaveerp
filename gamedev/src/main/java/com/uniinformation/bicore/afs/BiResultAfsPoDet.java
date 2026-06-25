package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.bicore.erpv4.BiResultStmovd;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsPoDet extends BiResultStmovd {

	public BiResultAfsPoDet(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		try {
			p_cc.getCell("stmd_ref2").setMode(Cell.VMODE_NORMAL);
			p_cc.getCell("stmd_ref3").setMode(Cell.VMODE_NORMAL);
			p_cc.getCell("stmd_date").setMode(Cell.VMODE_NORMAL);
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}	

	@Override
	protected void createColumnCells(final BiCellCollection col)
	{

		super.createColumnCells(col);	
		col.getCell("stmd_exprice").addAction(((BiResultStmov) getParent()).stmCalAmount);
	}	
}
