package com.uniinformation.zkbi.reports;

import java.util.List;

import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.zkbi.reports.ZkBiComposerAnalysisReport;

public class ZkBiComposerSummaryStatReport extends ZkBiComposerAnalysisReport {
	@Override
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		String colLabel = aggRec.getKey();
		if(isAggregateHidden(colLabel)) return(false);
		BiColumn bc = p_result.getColumnByLabel(colLabel);
		if(p_aop.isSubTotalColumn(p_idx)) {
			return(bc.isAggregateFlagOn());
		} else {
			return(bc.isPivotFlagOn());
		}
    }	
	
	@Override
	protected List<BiColumn> allowSelectAggregateList(BiResult result) {
		return(result.getAggregateColumnList());
	}
}
