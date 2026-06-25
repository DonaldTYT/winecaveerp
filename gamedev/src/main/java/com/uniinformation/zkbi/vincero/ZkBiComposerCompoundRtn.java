package com.uniinformation.zkbi.vincero;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.vincero.BiResultCompoundResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.zkbi.ZkBiComposerAnalysisReport;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;


public class ZkBiComposerCompoundRtn extends ZkBiComposerAnalysisReport{
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
		zkfName = "zkf/vincero/CompoundReturn.zul";	
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	
}
