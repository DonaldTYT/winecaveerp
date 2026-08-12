package com.uniinformation.zkf;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;

import com.uniinformation.utils.UniLog;

public class ZkCellActionWebglBarcode extends ZkCellActionForm {
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				Component c = (Component)arg0.getTarget();
				if(c.getId().equals("btStart")) {
					UniLog.log("Toogle Barcode Scanner");
					Clients.evalJavaScript("toggle_webgl();");;
				}
				if(c.getId().equals("btStop")) {
					UniLog.log("Start Barcode Scanner");
					Clients.evalJavaScript("start_webgl();");;
				}
			}
		};
		super.doAfterCompose(arg0);
	}

}
