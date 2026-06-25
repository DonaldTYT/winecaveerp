package com.uniinformation.zkbi.erpv4;

import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerAnalysisReport;

public class ZkBiComposerLocationMovement extends ZkBiComposerAnalysisReport {
	@Override
	protected void createZkfCollection(BiResult p_result) {
	    if(rptCol == null) {
	    	rptCol = p_result.getCurrentCollection();
	    }
	}
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		aggregateOffset = 2;
		zkfName = "zkf/erpv4/LocationMovementReport.zul";
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
		CellValueAction pivotChange = new CellValueAction() {
			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
				boolean byDate = result.getCell("pivotDate").getBoolean();
				boolean byType = result.getCell("pivotType").getBoolean();
				boolean byLoc = result.getCell("pivotLoc").getBoolean();
				UniLog.log("pivot change " + byDate +  " " + byType + " "+ byLoc);
				try {
					if(byDate || byType || byLoc) {
						result.getCell("pivotColumn").set("pds_pivot");
					} else {
						result.getCell("pivotColumn").set("");
					}
					refresh(result,masterWin,-1,true);
					regenAggregateAndPivot(result);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		};
		result.getCell("pivotDate").addAction(pivotChange);
		result.getCell("pivotType").addAction(pivotChange);
		result.getCell("pivotLoc").addAction(pivotChange);
	}
	
	@Override
	public void setupExportButton(final BiResult p_result) {
		
	}
			
}
