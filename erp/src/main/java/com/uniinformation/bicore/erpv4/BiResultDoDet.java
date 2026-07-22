package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultDoDet extends BiResultStmovd {

	public BiResultDoDet(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
//	public CellValueAction allocateChange = new CellValueAction() {
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			if(isActionEnabled() ) {
//			BiResultDoG2 parentBr = (BiResultDoG2) getParent();
//			parentBr.updateConsumedQtyToHash();
//			}
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//		}
//	};
	
//	public CellValueAction syncStmdQuoToDo = new CellValueAction() {
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			BiCellCollection bc = ((ColumnCell) p_value).getCollection();
//			{
//				BiResultDoG2 parentBr = (BiResultDoG2) getParent();
//				int stirg = bc.getCellInt("stmd_irg");
//				if(stirg <= 0) return;
//				int invrg = bc.getCellInt("ind_rg");
//				if(invrg > 0) {
//						Pair<Integer,Integer> qlc = parentBr.getNewQorgOrgFromQuotation(invrg,stirg);
//						if(qlc != null) {
//							bc.getCell("stmd_qorg").set(qlc.getLeft());
//							bc.getCell("stmd_org").set(qlc.getRight());
//						}
//				}
//			}
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//		}
//		
//	};
	
	@Override
	protected void createColumnCells(final BiCellCollection col)
	{
		super.createColumnCells(col);	
//		col.getCell("ind_rg").addAction(syncStmdQuoToDo);
//		col.getCell("stmd_irg").addAction(syncStmdQuoToDo);
//		col.getCell("stmd_org").addAction(allocateChange);
//		col.getCell("stmd_qorg").addAction(allocateChange);
//		col.getCell("stmd_qty").addAction(allocateChange);
//		col.getCell("ind_rg").addAction(allocateChange);
	}
}
