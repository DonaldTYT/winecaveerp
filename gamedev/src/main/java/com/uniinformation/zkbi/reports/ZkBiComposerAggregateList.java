package com.uniinformation.zkbi.reports;

import java.util.Vector;

import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;

public class ZkBiComposerAggregateList extends ZkBiComposerAggregate {
	@Override
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
		super.setAggregates(p_result,p_aop);
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.valueOf("COUNT"));
	} 
}
