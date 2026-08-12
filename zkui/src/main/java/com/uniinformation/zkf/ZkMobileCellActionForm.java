package com.uniinformation.zkf;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

import com.uniinformation.utils.UniLog;

public class ZkMobileCellActionForm extends ZkCellActionForm {
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		super.doAfterCompose(arg0);
		UniLog.log("ZkMobileCellAction mobile size " + sessionHelper.getScreenWidth() + " x " + sessionHelper.getScreenHeight());
		if(arg0 instanceof Window) {
				((Window) arg0).setHeight("" + (sessionHelper.getScreenHeight()-100) + "px");
			
		}
	}
}
