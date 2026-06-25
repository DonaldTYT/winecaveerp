package com.uniinformation.zkf.winecave;

import java.io.ByteArrayOutputStream;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;

import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkf.ZkCellActionForm;


public class GoogleMerchantCentre extends ZkCellActionForm   {
	@Wire
	private Button btDownload;
	
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		super.doAfterCompose(arg0);
		if(btDownload != null) {
		   	btDownload.setVisible(true);
		   		btDownload.addEventListener("onClick",
   		        new ZkBiEventListener() {
   		     			public void onZkBiEvent(Event event) throws Exception {
   		     				UniLog.log("HAHA");
   		     			}
   		     	} 
   	       	);
   		}
		
	}
}
