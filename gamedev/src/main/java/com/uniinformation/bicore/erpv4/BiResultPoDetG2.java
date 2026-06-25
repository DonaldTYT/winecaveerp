package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPoDetG2 extends BiResultStmovd {

	public BiResultPoDetG2(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	
	public CellValueAction qoQuantityChange = new CellValueAction() {
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			if(isActionEnabled() ) {
			BiCellCollection bc = ((ColumnCell) p_value).getCollection();
			if(bc.getCell("stmd_qorg").getInt() > 0) {
				BiResultPO parentBr = (BiResultPO) getParent();
				parentBr.updateConsumedQtyToHash();
			}
			}
		}

		@Override
		public void cellAction_onfree() throws CellException {
		}
	};
	public CellValueAction qoDetailChange = new CellValueAction() {
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			if(isActionEnabled() ) {
			BiResultPO parentBr = (BiResultPO) getParent();
			parentBr.updateConsumedQtyToHash();
			}
		}

		@Override
		public void cellAction_onfree() throws CellException {
		}
	};
	public CellValueAction syncQuoDetWithStmd = new CellValueAction() {
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			BiCellCollection bc = ((ColumnCell) p_value).getCollection();
			if(p_value.getCellLabel().equals("stmd_irg")) {
				BiResultPO parentBr = (BiResultPO) getParent();
				int stirg = p_value.getInt();
				if(stirg <= 0) return;
				int invrg = bc.getCellInt("ind_rg");
				if(invrg > 0) {
					int qorg = bc.getCellInt("stmd_qorg");
					if(qorg > 0) {
						int indirg = bc.getCellInt("ind_irg");
						if(indirg != stirg ) {
							throw new CellException("Purchase Item Code Not Equals Sales Order Item Code");
						}
					} else {
						double qty = bc.getCellDouble("stmd_qty");
						Pair<Integer,Comparable> quoItem = parentBr.getNewOdrgFromQuotation(invrg,stirg,qty);
						if(quoItem != null) {
							qorg = quoItem.getLeft();
							int itemno = (Integer) quoItem.getRight();
							bc.getCell("stmd_qorg").set(qorg);
//							bc.getCell("ind_itemno").set(itemno);
						}
					}
				}
			}
		}

		@Override
		public void cellAction_onfree() throws CellException {
		}
		
	};
	
	
	@Override
	protected void createColumnCells(final BiCellCollection col)
	{
		super.createColumnCells(col);	
		col.getCell("stmd_exprice").addAction(((BiResultStmov) getParent()).stmCalAmount);
		col.getCell("ind_rg").addAction(syncQuoDetWithStmd);
		col.getCell("stmd_irg").addAction(syncQuoDetWithStmd);
		col.getCell("stmd_qorg").addAction(qoDetailChange);
		col.getCell("stmd_qty").addAction(qoQuantityChange);
		col.getCell("ind_rg").addAction(qoDetailChange);
	}
}
