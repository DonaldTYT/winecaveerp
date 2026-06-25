package com.uniinformation.zkbi.vincero;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.vincero.BiResultCompoundResult;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerAnalysisReport;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;


public class ZkBiComposerCompoundResult extends ZkBiComposerAnalysisReport{
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
		zkfName = "zkf/vincero/CompoundResult.zul";	
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	
	@Override
	protected void processOptionEvent(BiResult result, ZkForm zkf1,Event arg0) throws Exception{ 
		if(arg0.getTarget().getId().equals("btRecalWorkSheet")) {
	        result.recal();
        	refreshListItems(null);
        	ZkUtil.msg("Recal Completed");
        	((BiResultCompoundResult) result).reLoadDataToZkForm(zkf1);
			return;
		}
		super.processOptionEvent(result, zkf1,arg0);
	}
	
	
//	@Override
//	protected void setupExtraButton(final BiResult result) {
//		super.setupExtraButton(result);
//		Button btSimulate;
//	    if(masterWin.hasFellow("btSimulate")) {
//	    	btSimulate = (Button) masterWin.getFellow("btSimulate");
//	    } 
//	   else {	
//		        btSimulate = new ZkBiButton();
//		        btSimulate .setLabel("Simulate");
//		        btSimulate .setId("btSimulate");
//	    } 
//		abHelper.addButton(btSimulate , "fa-user");
//	    btSimulate.addEventListener("onClick",
//	        new ZkBiEventListener () {
//				@Override
//				public void onZkBiEvent(Event event) throws Exception {
//						((BiResultCompoundResult) result).runSimulator();
//        			    refreshListItems(null);
//					}
//	        	}
//	        );	
//	    }
	
	
	
}
