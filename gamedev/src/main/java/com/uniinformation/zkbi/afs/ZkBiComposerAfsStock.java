package com.uniinformation.zkbi.afs;

import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.erpv4.ZkBiComposerStock;

public class ZkBiComposerAfsStock extends ZkBiComposerStock{
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		if(!result.allowUpdate()) hasAUDColumn=false;
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	/*
	@Override
    protected void setupDeleteButton(final BiResult result)
	{
    	UniLog.log("ZkBiComposerOsInstall setupDeleteButton");
		if(!result.allowUpdate()) return;
   	}
   	*/
}
