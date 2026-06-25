package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsDoDet extends BiResult {
	/*
		sync packing dimension  from goods received record to order detail record
	 */
	CellValueAction getPackingDimension = null;
	public BiResultAfsDoDet(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("BiResultAfsDoDet");
		getPackingDimension = new CellValueAction() {
			public void cellAction_onchange(Cell p_cell) throws CellException {
				if(!isActionEnabled()) return;
				ColumnCell c = (ColumnCell) p_cell;
				CellCollection col = c.getCollection();
				int irg = col.getCell("stmd_irg").getInt();
				int org = col.getCell("stmd_org").getInt();
				if(irg <= 0 || org <= 0) return;
				try {
					TableRec tr = su.getQueryResult("select serial_id,stmd_nref1 , stmd_nref2 , stmd_nref3 , stmd_fref1 from stmovd where "
							+ " stmd_tdtype in (" + Erpv4Config.STOCKIN_TDtypes+") "
							+ " and stmd_org = " + org
							+ " and stmd_irg = " + irg
							+ " and stmd_fref1 > 0"
							+ " order by serial_id desc"
						, null);
					if(tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
						col.getCell("stmd_nref1").set(tr.getField("stmd_nref1"));
						col.getCell("stmd_nref2").set(tr.getField("stmd_nref2"));
						col.getCell("stmd_nref3").set(tr.getField("stmd_nref3"));
						col.getCell("stmd_fref1").set(tr.getField("stmd_fref1"));
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					throw new CellException(ex.toString());
				}
			}
			public void cellAction_onfree() {
				
			}
		};
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		col.getCell("stmd_org").addAction(getPackingDimension);
		col.getCell("stmd_irg").addAction(getPackingDimension);
		if(((BiResultStmov) getParent()).detAmtCell != null) {
			col.getCell(
					((BiResultStmov) getParent()).detAmtCell
					).addAction(((BiResultStmov) getParent()).stmCalAmount);
		}
	}
}
