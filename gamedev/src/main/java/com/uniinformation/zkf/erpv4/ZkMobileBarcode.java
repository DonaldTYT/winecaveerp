package com.uniinformation.zkf.erpv4;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;

import org.eclipse.birt.report.model.api.util.StringUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;

import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValidation;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.winecave.Winelist;
import com.uniinformation.zkf.ZkCellActionForm;
import com.uniinformation.zkf.ZkForm;

public class ZkMobileBarcode extends ZkCellActionForm{
	static public HashSet<String> mobileScannerList = new HashSet();
	static String allocateMbsId() {
		synchronized(mobileScannerList) {
			for(int i=0;i<1000;i++) {
				String ss = DeviceControl.MOBILE_SCANNER_PREFIX+i;
				if(mobileScannerList.contains(ss)) continue;
				mobileScannerList.add(ss);
				return(ss);
			}
			return(null);
		}
	}
	static void releaseMbsId(String p_id) {
		
		synchronized(mobileScannerList) {
			mobileScannerList.remove(p_id);
		}
	}
	Cell barcodeText;
	Cell readBarcode;
	Cell scannerId;
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		/*
	onClickListener = new EventListener(){
		@Override
		public void onEvent(Event arg0) throws Exception {
			if(arg0.getTarget().getId().equals("bt_scan")) {
				barcodeText.set("");
				UniLog.log("bt_scan Clicked Barcode Opened");
				UniLog.log("activate scan barcode");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
			   	Clients.evalJavaScript("launchScanner()");
			}
		
			if(arg0.getTarget().getId().equals("btScanE")) {
				UniLog.log("btScanE Clicked barcode Closed");
				UniLog.log("Last Barcode = ["+formCollection.getCellString("txNewBarcode")+"]");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
			   	Clients.evalJavaScript("closeScanner()");
				
			}	
		}
	};
		*/
		super.doAfterCompose(arg0);
		barcodeText = formCollection.testCell("txNewBarcode");
		readBarcode = formCollection.testCell("readBarcode");
		scannerId = formCollection.testCell("scannerId");
		if(scannerId == null) {
			scannerId = new Cell("");
		}
		
		if(barcodeText != null) {
			barcodeText.setValidation(
					new CellValidation() {

						@Override
						public boolean validate(Cell p_cell, Object p_value) {
							// TODO Auto-generated method stub
							String barcode = p_value.toString();
							UniLog.log("got barcode " + barcode);
							try {
							if(readBarcode != null) {
								readBarcode.set(barcode);
							}
							if(!barcode.equals("")) {
								if(StringUtil.isBlank(scannerId.getString())) {
									String ss = allocateMbsId();
									if(ss != null) scannerId.set(ss);
								}
//								String scannerId = "MB01";
								if(!scannerId.isBlank()) {
									DeviceControl.postBarcode(sessionHelper,scannerId.getString(),barcode); 
								}
								Clients.evalJavaScript("beep(1)");
							};	
							} catch(Exception ex) {
								UniLog.log(ex);
							}
							return false;
						}

						@Override
						public String getErrMsg() {
							// TODO Auto-generated method stub
							return null;
						}
						
					}
			);
			/*
			barcodeText.addAction(
					new CellValueAction() {

						@Override
						public void cellAction_onchange(Cell p_value) throws CellException {
							// TODO Auto-generated method stub
							UniLog.log("barcode scanned ["+p_value.getString()+"]"); 
							if(!p_value.getString().equals("")) {
//								UniLog.log("validate barcode, close scanner");
								DeviceControl.postBarcode("BC01",p_value.getString()); 
								Clients.evalJavaScript("beep(1)");

								barcodeText.set("");
//								Clients.evalJavaScript("closeScanner()");
							};	
						}

						@Override
						public void cellAction_onfree() throws CellException {
							// TODO Auto-generated method stub
							
						}
						
					}
					);
				*/
				
		}
		barcodeText.set("");
		Clients.evalJavaScript("launchScanner()");
	}
	
	@Override
	protected void processActionByComposer(String p_eventName,Component p_target,boolean p_needResponse,InputStream p_upload) throws Exception {
		if(p_target.getId().equals("btTest")) {
			UniLog.log("HAHA test clicked");
//	        final ZkForm zkf1 = new ZkForm(null,"/zkf/erpv4/StockTakeClearZero.zul");
//	        CellCollection col = new CellCollection();
//	        			zkf1.doModal(col,new EventListener() {
//								@Override
//								public void onEvent(Event arg0) throws Exception {
//								}
//	        				}
//	        			);
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		if(scannerId != null && !scannerId.isBlank()) {
			releaseMbsId(scannerId.toString());
			scannerId = null;
		}
	}
}
