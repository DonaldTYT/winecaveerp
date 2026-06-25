package com.uniinformation.zkbi.reports;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;

public class ZkBiComposerAggregateReport extends ZkBiComposerAggregate {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
    	if(hasAUDColumn == null) hasAUDColumn=false;
		super.doAfterCompose(comp);
   	}
    @Override
    public boolean doBrowseItemSelected(XulElement p_win, BiResult p_result)
    {
    	UniLog.log("doBrowseItemSelected do nothing in zkreport");
    	return(true);
    }
}
