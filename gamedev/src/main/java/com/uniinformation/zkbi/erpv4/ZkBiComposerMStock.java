package com.uniinformation.zkbi.erpv4;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.util.Clients;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;

public class ZkBiComposerMStock extends ZkBiComposerBase {
	String barcodeDevId = "DEV_02";	
	@Override
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
			EventQueue que = EventQueues.lookup("BarcodeNotify", EventQueues.APPLICATION, true);
			que.subscribe(new EventListener() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					// TODO Auto-generated method stub
				  	if (arg0.getName() != null && arg0.getName().equals("onBarcodeNotify")){
	            		String barcode = (String) arg0.getData();
	            		if(barcode.trim().equals("")) return;
	            		UniLog.log("Mstock BarcodeNotify catched ["+barcode+"]");
            			int currentFetchedIdx = result.getCurrentRecIdx();
      					if(currentFetchedIdx >= 0 && result.inBeginWork()) {
      						if(result.getCellString("st_barcode").equals("")) {
      							result.getCell("st_barcode").update(barcode);
      						} else {
      							ZkBiMsgbox.show(ZkBiMsgbox.Type.error,"Please clear previouse barcode Firest");
      						}
	      					return;
      					} else {
      						int matchedIdx = -1;
      						for(int i=0;i<result.getRowCount();i++) {
    							result.loadOneRecV(i);
    							if(
    								result.getCellString("st_icode").equals(barcode)
    								|| result.getCellString("st_barcode").equals(barcode)
    								) {
    								matchedIdx = i;
    								break;
    							}
      						}
      						if(currentFetchedIdx >= 0) {
      							if(matchedIdx >= 0 && matchedIdx != currentFetchedIdx) { 
      								
      							} else {
      								result.loadOneRecV(currentFetchedIdx);
      								result.fetchOneRecV(currentFetchedIdx);
      								if(result.getCellString("st_barcode").equals("")) {
      									result.getCell("st_barcode").update(barcode);
      									detailForm.setDirtyFlag(true);
      									return;
      								}  else {
      									ZkBiMsgbox.show(ZkBiMsgbox.Type.error,"Please clear previouse barcode Firest");
      									return;
      								}
      							}
      						} else {
      							if(matchedIdx < 0) {
      								ZkBiMsgbox.show(ZkBiMsgbox.Type.error,"Scanned Barcode not found");
      								return;
      							}
      						}
      						if(matchedIdx >= 0 && matchedIdx != currentFetchedIdx ) {
   						      	result.fetchOneRecV(matchedIdx);
   						      	doUpdateOneRow(masterWin,result);
      						}
      					}
      					/*
	            		if(currentFetchedIdx >= 0) {
	            			boolean found = false;
	      					for(int i=0;i<result.getRowCount();i++) {
	    						result.loadOneRecV(i);
	    						if(
	    								result.getCellString("st_icode").equals(barcode)
	    								|| result.getCellString("st_barcode").equals(barcode)
	    								) {
	    							result.fetchOneRecV(i);
	    							doUpdateOneRow(masterWin,result);
	    							found = true;
	    							break;
	    						}
	    					}
	      					if(!found) {
	      						if(currentFetchedIdx >= 0) {
	    							result.fetchOneRecV(currentFetchedIdx);
	    							doUpdateOneRow(masterWin,result);
	      						}
	      						ZkBiMsgbox.show(ZkBiMsgbox.Type.error,"Scanned Barcode Not Exist");
	      					}
	            		} else {
      						ZkBiMsgbox.show(ZkBiMsgbox.Type.error,"");
	            		}
	            		*/
				  	}
				}
            }
			);
			
			/* obsoleted By DT 2020/11/18 */

//			DeviceControl.attachListiner(barcodeDevId,"BarcodeNotify");	
	}
	
	
	@Override
	public void biBaseClose(BiResult p_br) {
		super.biBaseClose(p_br);
		p_br.clearCurrentRec();
	}
}
