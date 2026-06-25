package com.uniinformation.zkbi.preview;

import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.zkbi.reports.ZkBiComposerAggregateReport;
import com.uniinformation.zkbi.reports.ZkBiComposerAnalysisReport;

public class ZkBiComposerPreviewSalesAnalysis extends ZkBiComposerAggregateReport {
	
	/* Override this method if sum pivoted aggregate is meanlingless and should be hide away */
	@Override
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		if(!p_aop.isSubTotalColumn(p_idx)) {
			if(
			"inv_grossprofit".equals(aggRec.getKey()) 
//			|| "stmd_outqty".equals(aggRec.getKey()) 
					) {
				return(false);
			} else {
				return(true);
			}
		}
    	return(true);
    }	

}
