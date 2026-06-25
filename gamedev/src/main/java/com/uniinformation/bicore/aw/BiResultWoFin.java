package com.uniinformation.bicore.aw;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultWoFin extends BiResult {
	
	PrintOwCallback prtWoCallback;

	public BiResultWoFin(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		final ColumnCell cc = (ColumnCell) col.getCell("wf_prtow");
		cc.addAction(
					new CellValueAction() {

						@Override
						public void cellAction_onchange(Cell p_value) throws CellException {
							// TODO Auto-generated method stub
							UniLog.log("wf_prtow pressed");
							BiResult wo = getParent();
							if(prtWoCallback != null) {
								prtWoCallback.invoke(cc);
							}
						}

						@Override
						public void cellAction_onfree() throws CellException {
							// TODO Auto-generated method stub
							
						}
						
					}
				);
	}

	public void setPrtWoCallback (PrintOwCallback p_cb) {
		prtWoCallback = p_cb;
	}
}
