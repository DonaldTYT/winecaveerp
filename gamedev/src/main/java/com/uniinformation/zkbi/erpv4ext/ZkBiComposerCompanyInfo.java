package com.uniinformation.zkbi.erpv4ext;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiComposerCompany;

public class ZkBiComposerCompanyInfo extends ZkBiComposerCompany {

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("doAfterCompose called");
		masterWin.addEventListener("onHideButtonCompInfo", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				Button btn = (Button)masterWin.getFellowIfAny("btAdd");
				if (btn != null)
					btn.setVisible(false);
				btn = (Button)masterWin.getFellowIfAny("btDelete");
				if (btn != null)
					btn.setVisible(false);
			}
		});
		Events.echoEvent("onHideButtonCompInfo", masterWin, null);
	}
}
