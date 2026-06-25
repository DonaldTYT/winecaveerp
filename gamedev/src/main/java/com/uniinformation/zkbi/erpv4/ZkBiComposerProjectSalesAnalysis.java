package com.uniinformation.zkbi.erpv4;

import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.reports.ZkBiComposerAggregateReport;
import com.uniinformation.zkbi.reports.ZkBiComposerAnalysisReport;

public class ZkBiComposerProjectSalesAnalysis extends  ZkBiComposerAggregateReport {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		headerAggregateFirst = true;
		super.doAfterCompose(comp);
   	}
	
	@Override
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		UniLog.log("Check IsAggregateVisible column " + p_idx + " " + aggRec.getKey() + " is subTotal " + p_aop.isSubTotalColumn(p_idx));
		if(!p_aop.isSubTotalColumn(p_idx)) {
			if(
			"ind_amount".equals(aggRec.getKey()) 
					) {
				return(false);
			} else {
				return(true);
			}
		}
    	return(true);
    }	
}
