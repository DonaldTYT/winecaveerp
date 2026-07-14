package com.uniinformation.zkbi.propertymgmt;

import java.util.stream.Stream;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zk.ui.select.Selectors;

import com.google.common.collect.Sets;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerParkingTmpLocPayment extends ZkBiComposerBase {

	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
   		super.doAfterCompose(comp);
		Selectors.find("[id^='btExtraBatchAction_']").stream().map(x -> (Button)x).forEach(bt -> {
			bt.addSclass("orange1");
		});
   	}

	@Override
   	public BiResult initZkBiWindows() {
   		masterWin.setAttribute("advSearchDatePlusDayEndTimeList", Sets.newHashSet("col_a"));
		return super.initZkBiWindows();
	}

	@Override
    protected JxZkBiBase buildDetailWindow(final BiResult result) {
		try {
			return super.buildDetailWindow(result);
		} finally {
			Selectors.find("[id^='btExtraJxFormAction_']").stream().map(x -> (Button)x).forEach(bt -> {
				bt.addSclass("orange1");
			});
		}
    }

	@Override
	public void hotkeyEvent(int p_modifierKey, char p_dataKey) {
		if (Stream.of("zkbi-messagebox-window", "zkbi-messageboxdlg", "z-messagebox-window", "z-window-modal").allMatch(s -> Selectors.find(".z-window." + s).isEmpty()))
			super.hotkeyEvent(p_modifierKey, p_dataKey);
	}
}
