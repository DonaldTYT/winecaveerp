package com.uniinformation.zkbi.vincero;

import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.ZkBiComposerAnalysisReport;


public class ZkBiComposerForexTrade extends ZkBiComposerAnalysisReport{
	/*
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		zkfName = "zkf/vincero/CompoundReturn.zul";	
		super.doAfterCompose(comp);
	}
	*/
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		aggregateOffset = 0;
		rptCol = result.getCurrentCollection();
		zkfName = "zkf/vincero/ForexTrade.zul";	
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
}
