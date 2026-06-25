package com.uniinformation.zkbi.afs;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;

import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.AggregateOrPivot.AGGREGATES;

public class ZkBiComposerOrderItemStatus extends com.uniinformation.zkbi.ZkBiComposerAnalysisReport{
	@Override
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		aggregateOffset = 0;
		zkfName = "zkf/afs/OrderItemStatus.zul";
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	
	@Override 
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
//		p_aop.addAggregate("D/N",AggregateOrPivot.AGGREGATES.UNIQUECAT,null,null,null,-1,"stmso_ref1");
//		p_aop.addAggregate("Serial No",AggregateOrPivot.AGGREGATES.STRCAT,null,null,null,-1,"stmdso_ref4");
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.UNIQUECAT,"stmso_ref1");
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.STRCAT,"stmdso_ref4");
	} 

	@Override 
	protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
		p_aop.addCol("st_icode");
	}
}
