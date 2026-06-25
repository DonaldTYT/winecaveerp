package com.uniinformation.zkf;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.ZkUtil;

public class ZkCellActionWorkSheet extends ZkCellActionForm {
	protected String baseBrName = null;
	protected BiResult baseBr;
	
	
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		super.doAfterCompose(arg0);
	}
	@Override 
	protected void beforeMapCollection() {
		super.beforeMapCollection();
		if(baseBrName != null) {
			baseBr = sessionHelper.newBiResult(baseBrName);
			baseBr.query();
			formCollection = baseBr.getCurrentCollection();
		}
	}
}
