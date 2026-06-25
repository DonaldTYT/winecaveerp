package com.uniinformation.zkbi;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.InputElement;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerExtendedReport extends ZkBiComposerExtended {
	
	protected String zkfName;
	protected ZkForm zkf1;
	protected void onZkFEvent(BiResult result,Event event) throws Exception {
	}
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
    	allowDragDropHeader = true;
		super.doAfterCompose(comp);
	}
	
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		super.buildBrowserWindow(result, comp, p_sortIdx, p_sortDesc);
		String ss = Executions.getCurrent().getParameter("zkfName");
		if(!StringUtils.isBlank(ss)) zkfName = ss;
		if(!StringUtil.isBlank(zkfName)) {
			Div rpth = new Div();
			zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
			zkf1 = new ZkForm(rpth,zkfName.replaceAll("\\.", "/")+".zul");
	    	try {
	    		zkf1.mapCellCollection(result.getCurrentCollection(),new EventListener() {
			    	@Override
			    	public void onEvent(Event arg0) throws Exception {
			    		onZkFEvent(result,arg0);
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
