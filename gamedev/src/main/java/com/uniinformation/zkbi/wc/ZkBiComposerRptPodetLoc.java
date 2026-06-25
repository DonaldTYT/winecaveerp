package com.uniinformation.zkbi.wc;

import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;

public class ZkBiComposerRptPodetLoc extends ZkBiComposerAggregateReport{
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	result.setQueryIncludeNoDetail(true);
	}
}
