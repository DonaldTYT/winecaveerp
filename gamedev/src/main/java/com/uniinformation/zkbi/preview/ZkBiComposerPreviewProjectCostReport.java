package com.uniinformation.zkbi.preview;

import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.reports.ZkBiComposerAggregateReport;

public class ZkBiComposerPreviewProjectCostReport extends ZkBiComposerAggregateReport {


	/* Override this method if sum pivoted aggregate is meanlingless and should be hide away */
	@Override
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		if(
			"sih_losbal".equals(aggRec.getKey()) 
			|| "sih_ltotal".equals(aggRec.getKey()) 
			) {
			if(!p_aop.isSubTotalColumn(p_idx)) return(false);
		}
    	return(true);
    }
    
}
