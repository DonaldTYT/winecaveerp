package com.uniinformation.zkbi.erpv4;

import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultAccountBalance;
import com.uniinformation.bicore.erpv4.BiResultArAp;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;

public class ZkBiComposerArAsAt extends ZkBiComposerSihReport {
	@Override
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
			zkfName = "zkf/erpv4/ArApAsAt.zul";
			super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
			rptCol.getCell("asatdate").addAction(new CellValueAction() {
				@Override
				public void cellAction_onchange(Cell p_value) throws CellException {
					// TODO Auto-generated method stub
						((BiResultArAp) result).setAsAtDate(p_value.getDate());
			    		refresh(result,masterWin,-1,true,true); 
				}
				@Override
				public void cellAction_onfree() throws CellException {
					// TODO Auto-generated method stub
				}
			}
		);
	}
	@Override
	protected void createZkfCollection(BiResult p_result) {
		super.createZkfCollection(p_result);
	   	rptCol.addCell("rpttitle",new Cell( "Receivable As At",Cell.VMODE_NORMAL));
	}	
}
