package com.uniinformation.zkbi.propertymgmt;

import java.util.stream.Stream;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zk.ui.select.Selectors;

import com.google.common.collect.Sets;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerProjectPayment extends ZkBiComposerBase {

	@Override
   	public BiResult initZkBiWindows() {
   		masterWin.setAttribute("advSearchDatePlusDayEndTimeList", Sets.newHashSet("col_a"));
		return super.initZkBiWindows();
	}

	@Override
    public void buildBrowserWindow(final BiResult result, final Component p_listRoot, int p_sortIdx, boolean p_sortDesc) {
   		super.buildBrowserWindow(result, p_listRoot, p_sortIdx, p_sortDesc);
		Selectors.find(p_listRoot, "[id^='btExtraBatchAction_']").stream().map(x -> (Button)x).forEach(bt -> {
			bt.addSclass("orange1");
		});
		Selectors.find(p_listRoot, "zkbibutton#btImport").forEach(bt -> {
			try {
				ClassLoader classLoader = DynamicClassLoader.class.getClassLoader();
				DynamicClassLoader dl = new DynamicClassLoader(classLoader);
				Class<?> clazz = dl.loadClass("com.uniinformation.dynamic.propertymgmt.ImportProjectPayment");
				EventListener<Event> el = (EventListener<Event>)ZkUtil.createConstructorHandle(clazz).invoke();
				Runnable refreshAction = () -> refresh(result, masterWin, (MultiSortMap)mMultiSortMap.clone(), false);
				bt.setAttribute("refreshAction", refreshAction);
				ZkUtil.setZkBiEventListener(bt, Events.ON_CLICK, el);
			} catch (Throwable e) {
				UniLog.log(e);
			}
		});
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
