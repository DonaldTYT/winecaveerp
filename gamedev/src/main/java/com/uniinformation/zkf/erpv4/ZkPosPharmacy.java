package com.uniinformation.zkf.erpv4;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.erpv4.wip.WfmEventData;
import com.uniinformation.erpv4.wip.WfmStep;
import com.uniinformation.erpv4.wip.WfmTaskBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkPosPharmacy extends ZkCellActionForm {
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		EventQueue que = EventQueues.lookup("BarcodeNotify", EventQueues.APPLICATION, true);
		que.subscribe(new EventListener() {
            public void onEvent(Event event) throws Exception {
            	if (event.getName() != null && event.getName().equals("onBarcodeNotify")){
            		String barcode = (String) event.getData();
            		UniLog.log("got scanned barcode ["+barcode+ "]");
            	}
            }
        });
		DeviceControl.attachListiner("BC01","BarcodeNotify",false);
		
//		que.publish(new Event("onWipNotify", null,wed));
	}
}
