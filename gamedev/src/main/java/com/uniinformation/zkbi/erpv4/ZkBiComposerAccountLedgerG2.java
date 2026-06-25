package com.uniinformation.zkbi.erpv4;

import java.util.Date;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listhead;

import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.reports.ZkBiComposerLedgerReport;

public class ZkBiComposerAccountLedgerG2 extends ZkBiComposerLedgerReport {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		if(zkfName == null) zkfName = "zkf/reports/AccountLedgerReport.zul";	
		super.doAfterCompose(comp);
	}
	@Override
	protected void resetListHeader(BiResult result) {
			Cell c = rptCol.getCell("rgSummaryOrDetail");
			if(c.getInt() == 0) {
				result.resetViewList();
			} else {
				Vector<BiColumn> vl = (Vector <BiColumn>) result.getListColumns().clone();
				for(BiColumn bc:vl) {
					if(bc.getLabel().equals("jn_xdate")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_xno")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_seq")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_srcno")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_post")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_flag")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_jcode")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_desc0")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_gldpcode")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("gldp_gldpcode")) result.hideViewColumn(bc);
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
			"jn_pamount".equals(aggRec.getKey()) 
			|| "jn_namount".equals(aggRec.getKey()) 
					) {
				return(true);
			} else {
				return(false);
			}
		}
		Cell c = rptCol.getCell("rgSummaryOrDetail");
		if(c.getInt() != 0) {
			if(
			"jn_balance".equals(aggRec.getKey()) 
			|| "jn_lbalance".equals(aggRec.getKey()) 
					) {
				return(false);
			}	
		}
    	return(true);
    }	
	
	
	/*
	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		ReturnMsg rtn = super.setAdditionalQueryCondition(result);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(!getSessionHelper().hasAccessRight("#multicomp")) {
			result.addCustomCondition("loc_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper()) + "'");
			
		}
		return(ReturnMsg.defaultOk);
	}
	*/
}
