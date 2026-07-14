package com.uniinformation.zkbi.propertymgmt;

import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerProjectFee extends ZkBiComposerBase {

	@Override
    protected JxZkBiBase buildDetailWindow(final BiResult result) {
		try {
			return super.buildDetailWindow(result);
		} finally {
			Selectors.find("[id^='btExtraJxFormAction_']").stream().map(x -> (Button)x).forEach(bt -> {
				bt.setVisible(sessionHelper.hasAccessRight("#pmgtadm1"));
			});
		}
    }
}
