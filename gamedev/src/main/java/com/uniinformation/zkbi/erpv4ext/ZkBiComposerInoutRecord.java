package com.uniinformation.zkbi.erpv4ext;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultInoutRecord;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerInoutRecord extends ZkBiComposerBase {

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("doAfterCompose called");
		masterWin.addEventListener("onHideButtonAttRec", new EventListener<Event>() {
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
		Events.echoEvent("onHideButtonAttRec", masterWin, null);
	}

	@Override
	public void biBaseClose(BiResult p_br) {
		super.biBaseClose(p_br);
		((BiResultInoutRecord)p_br).setQueryPeriod(null, null);
	}
}
