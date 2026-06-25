package com.uniinformation.zkbi.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.zkbi.reports.ZkBiComposerLedgerReport;

public class ZkBiComposerProfitLedger extends ZkBiComposerLedgerReport {
	@Override
	protected void resetListHeader(BiResult result) {
			Cell c = rptCol.getCell("rgSummaryOrDetail");
			if(c.getInt() == 0) {
				result.resetViewList();
			} else {
				Vector<BiColumn> vl = (Vector <BiColumn>) result.getListColumns().clone();
				for(BiColumn bc:vl) {
					if(bc.getLabel().startsWith("stm")) {
						if(!bc.getLabel().startsWith("stmd_loc")) {
							result.hideViewColumn(bc);
						}
					}
				}
			}
			super.resetListHeader(result);
	}
}
