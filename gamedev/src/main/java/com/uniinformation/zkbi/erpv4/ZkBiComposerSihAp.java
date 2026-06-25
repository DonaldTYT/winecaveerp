package com.uniinformation.zkbi.erpv4;

import org.zkoss.zk.ui.Component;

public class ZkBiComposerSihAp extends ZkBiComposerSih {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		super.doAfterCompose(comp);
		module = "AP";
		paymentViewId = "erpv4.CrhAp";
		adjListboxHeight(65);
	}
}
