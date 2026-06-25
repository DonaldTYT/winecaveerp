package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMoStockTakeG2 extends BiResultMoStockTake {
//	String stockTakeLoc = null;
	public BiResultMoStockTakeG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
		
		for(BiResult sr : getSubLinks()) {
			if(sr.getView().getTable().getName().equals("stmovd_ko")) {
				stmdLinkName = sr.getView().getName();
				break;
			}
		}		
		extraStmds.add("stmdki");
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		BiCellCollection col = super.createColumnCollection(p_parent);
		String stockTakeLoc = Erpv4Config.getStockTakeLoc(getSessionHelper(),Erpv4Config.getDefaultCoCode(getSessionHelper()));
		col.addCell("stockTakeLoc", new Cell(stockTakeLoc));
		return(col);
	}
}
