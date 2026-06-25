package com.uniinformation.zkbi.erpv4;

import java.util.Date;
import java.util.Vector;

import org.zkoss.zul.Listhead;

import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultLedgerG2;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.reports.ZkBiComposerLedgerReport;

public class ZkBiComposerStockLedgerG2 extends ZkBiComposerLedgerReport {
	@Override
	protected void resetListHeader(BiResult result) {
			Cell c = rptCol.getCell("rgSummaryOrDetail");
			if(c.getInt() == 0) {
				result.resetViewList();
			} else {
				Vector<BiColumn> vl = (Vector <BiColumn>) result.getListColumns().clone();
				for(BiColumn bc:vl) {
					if(bc.getLabel().startsWith("stmdo_")) {
						continue;
					}
					if(bc.getLabel().startsWith("stm")) {
						if(!bc.getLabel().startsWith("stmd_loc")) {
							result.hideViewColumn(bc);
						}
					}
					if(bc.getLabel().startsWith("cldoc")) {
						result.hideViewColumn(bc);
					}
				}
			}
			super.resetListHeader(result);
	}
	/* Override this method if sum pivoted aggregate is meanlingless and should be hide away */
	@Override
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		if(!p_aop.isSubTotalColumn(p_idx)) {

			if(
			"stmd_inqty".equals(aggRec.getKey()) 
			|| "stmd_outqty".equals(aggRec.getKey()) 
			|| "stmd_runqty".equals(aggRec.getKey()) 
			|| "stmd_inamount".equals(aggRec.getKey()) 
			|| "stmd_outamount".equals(aggRec.getKey()) 
					) {
				return(true);
			} else {
				return(false);
			}
		}
		Cell c = rptCol.getCell("rgSummaryOrDetail");
		if(c.getInt() != 0) {
			if(
			"stmd_balance".equals(aggRec.getKey()) 
			|| "stmd_runamount".equals(aggRec.getKey()) 
					) {
				return(false);
			}	
		}
    	return(true);
    }	
	
	
	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		ReturnMsg rtn = super.setAdditionalQueryCondition(result);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if( ! (result instanceof BiResultLedgerG2) ) {
		if(!getSessionHelper().hasAccessRight("#multicomp")) {
			result.addCustomCondition("loc_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper()) + "'");
		}
		}
		return(ReturnMsg.defaultOk);
	}
	
}
