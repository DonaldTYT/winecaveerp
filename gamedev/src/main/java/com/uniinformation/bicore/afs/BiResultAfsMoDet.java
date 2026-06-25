package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultMoDet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsMoDet extends BiResultMoDet {
	public BiResultAfsMoDet (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("BiResultAfsMoDet Used");
	}
	
//	@Override
//	protected void createColumnCells(final BiCellCollection col)
//	{
//		super.createColumnCells(col);
//		col.getCell("stmd_tdtype").addAction(
//				new CellValueAction() {
//
//					@Override
//					public void cellAction_onchange(Cell p_value)
//							throws CellException {
//						if(col.getCell("stmd_tdtype").getString().equals("MO")) {
//							col.getCell("or_ocode").setMode(Cell.VMODE_NORMAL);
//							col.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
//						} else if(col.getCell("stmd_tdtype").getString().equals("MI")) {
//							col.getCell("or_ocode").setMode(Cell.VMODE_DISPONLY);
//							col.getCell("inv_invno").setMode(Cell.VMODE_NORMAL);
////							col.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
//						} else {
//							col.getCell("or_ocode").setMode(Cell.VMODE_DISPONLY);
//							col.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
//						}
//						// TODO Auto-generated method stub
//						
//					}
//
//					@Override
//					public void cellAction_onfree() throws CellException {
//						// TODO Auto-generated method stub
//						
//					}
//					
//				}
//				);
//	}
}
