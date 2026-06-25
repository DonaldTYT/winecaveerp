package com.uniinformation.zkbi.edu;

import org.zkoss.zk.ui.Component;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerAssessment extends ZkBiComposerBase {
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin")) {
			queryBar.setVisible(false);
			bottomPanelVbox.setVisible(false);
		}
	}

	@Override
    public void showListPanel() {
		super.showListPanel();
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin")) {
			queryBar.setVisible(false);
			bottomPanelVbox.setVisible(false);
		}
    }
}
