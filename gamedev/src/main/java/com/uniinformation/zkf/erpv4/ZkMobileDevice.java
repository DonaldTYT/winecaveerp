package com.uniinformation.zkf.erpv4;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;

import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValidation;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.winecave.Winelist;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkMobileDevice extends ZkCellActionForm{
	final int DEV_BARCODESCANNER=0;
	final int DEV_CAMERA=1;
	final int DEV_RECORDER=2;
	final int DEV_GPS=3;
	String devId;
	String bindedSession;
	int currentDevice = 0;
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				Component c = (Component)arg0.getTarget();
				if(c.getId().equals("btBarcode")) {
//					Clients.evalJavaScript("zkDevice.launchScanner()");
					switch(currentDevice) {
					case DEV_BARCODESCANNER: return;
					case DEV_CAMERA: 
							ZkUtil.js("zkDevice.closeCamera()"); 
							break;
					}
					currentDevice = DEV_BARCODESCANNER;
   		 			ZkUtil.js("zkDevice.launchScanner()"); 
				}
				if(c.getId().equals("btCamera")) {
//					Clients.evalJavaScript("zkDevice.launchScanner()");
					ZkUtil.js("zkDevice.closeScanner()"); 
   		 			ZkUtil.js("zkDevice.launchPhotoCapture()"); 
				}
			}
		};
		
		super.doAfterCompose(arg0);
		devId = "Mobile"+getSessionHelper().hashCode();
		Cell cc = formCollection.testCell("lbSessionId");
		cc.set("ID:"+getSessionHelper().hashCode());
		bindedSession = DeviceControl.getSessionIdByDevice(devId);
		if(bindedSession == null) {
			cc = formCollection.testCell("lbMessage");
			cc.set("Click the barcode Icon on the top left corner of the Applicatoin Page, when the binding QR code is shown, scan the QR Code");
		} else {
			((Button) zkf.getComponent("btCamera")).setDisabled(false);
		}
		this.addEventListener("onBarcodeNotify", 
   		 	new ZkBiEventListener() {
   		 		public void onZkBiEvent(Event event) throws Exception {
   		 			if (event.getName() != null && event.getName().equals("onBarcodeNotify")){
   		 				String barcode = (String) event.getData();
   		 				UniLog.log("got scanned barcode ["+barcode+ "]");
						DeviceControl.postBarcode(sessionHelper,devId,barcode); 
						//		Clients.evalJavaScript("beep(1)");
						ZkUtil.js("zkDevice.beep(1)");
   		 			}
   		 		}
   		 	}
		);
		this.addEventListener("onAddPhoto", 
   		 	new ZkBiEventListener() {
   		 		public void onZkBiEvent(Event event) throws Exception {
   		 			if (event.getName() != null && event.getName().equals("onAddPhoto")){
   		 				UniLog.log("onAddPhoto: " + event.getName() + " data.length():" + event.getData().toString().length());
   		 				DeviceControl.postPhoto(getSessionHelper(), devId, event.getData());
   		 			}
   		 		}
   		 	}
		);
   		ZkUtil.js("zkDevice.launchScanner()"); 
	}
}
