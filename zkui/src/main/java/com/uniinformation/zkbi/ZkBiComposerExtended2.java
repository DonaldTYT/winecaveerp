package com.uniinformation.zkbi;

import org.eclipse.birt.report.model.api.util.StringUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerExtended2 extends ZkBiComposerExtended {
	
	protected void onZkFEvent(Event ev) {
		
	}
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		super.buildBrowserWindow(result, comp, p_sortIdx, p_sortDesc);
		String zkfName = Executions.getCurrent().getParameter("zkfName");
		if(!StringUtil.isBlank(zkfName)) {
			Div rpth = new Div();
			zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
			ZkForm zkf1 = new ZkForm(rpth,zkfName.replaceAll("\\.", "/")+".zul");
	    	try {
	    		zkf1.mapCellCollection(result.getCurrentCollection(),new EventListener() {
			    	@Override
			    	public void onEvent(Event arg0) throws Exception {
			    		onZkFEvent(arg0);
//			    		biBaseRefresh(result);
			    	}
		    	}
	    		);
	    	} catch(Exception ex) {
	    		UniLog.log(ex);
	    	}
		}
	}

}
