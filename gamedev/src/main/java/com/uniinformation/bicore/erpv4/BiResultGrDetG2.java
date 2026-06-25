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

public class BiResultGrDetG2 extends BiResultStmovd {

	public BiResultGrDetG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	public CellValueAction pdsChange = new CellValueAction() {
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			if(isActionEnabled() ) {
			BiResultGrG2 parentBr = (BiResultGrG2) getParent();
			BiCellCollection bc = ((ColumnCell) p_value).getCollection();
			if(p_value.getCellLabel().equals("orddet_mrg")) {
//				bc.getCell("stmd_irg").set(0);
				bc.getCell("st_icode").set("");
				bc.getCell("stmd_org").set(0);
			}
			
			parentBr.updateConsumedQtyToHash();
			}
		}

		@Override
		public void cellAction_onfree() throws CellException {
		}
	};
	
	public CellValueAction syncStmdPdBi = new CellValueAction() {
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			BiCellCollection bc = ((ColumnCell) p_value).getCollection();
			if(p_value.getCellLabel().equals("stmd_irg")) {
				BiResultGrG2 parentBr = (BiResultGrG2) getParent();
				int stirg = p_value.getInt();
				if(stirg <= 0) return;
				int odrmrg = bc.getCellInt("orddet_mrg");
				if(odrmrg > 0) {
						Integer org = parentBr.getNewOrgFromPO(odrmrg,stirg);
						if(org != null) {
							bc.getCell("stmd_org").set(org);
						} else {
							throw new CellException ("Item Not Found");
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

		col.getCell("orddet_mrg").addAction(syncStmdPdBi);
		col.getCell("stmd_irg").addAction(syncStmdPdBi);
		col.getCell("stmd_org").addAction(pdsChange);
		col.getCell("stmd_qty").addAction(pdsChange);
		col.getCell("orddet_mrg").addAction(pdsChange);
	}
}
