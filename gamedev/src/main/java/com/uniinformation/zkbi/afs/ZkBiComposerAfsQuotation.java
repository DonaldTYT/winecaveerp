package com.uniinformation.zkbi.afs;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.afs.BiResultAfsQuotation;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerAfsQuotation extends ZkBiComposerBase {
//	@Override
//	   protected void setupAddButton(final BiResult result)
//	    {
//	       	final Button btnAdd = new ZkBiButton();
//	        btnAdd.setLabel("Scan");
//	        btnAdd.setId("btScan");
//	        btnAdd.addEventListener("onClick",
//	        	new EventListener() {
//	        		public void onEvent(Event event) throws Exception {
//	        			clearSearch(result);
//  	   		       	hideListPanel();
//	        			UniLog.log("HAHA Scan Barcode Started");
//	    				UniLog.log("HAHA activate scan barcode");
////	    			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
//	    			   	Clients.evalJavaScript("launchScanner()");
//	        		}
//	        	}
//	        );
//	        actionBar.appendChild(btnAdd); 
////	        c.appendChild(btnAdd); 
//	    }
//	@Override
//    protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc)
//    {
//		BiResult br = super.getQueryResult(sessionHelper,p_viewid, p_sortIdx, p_sortDesc);
//		if(br != null) {
//			String orderType = Executions.getCurrent().getParameter("orderType");
//			if(orderType != null && orderType.equals("MC")) {
//				((BiResultAfsQuotation) br).setQuotationMode(BiResultAfsQuotation.AfsQuotationType_MC);
//			}
//			
//		}
//		return(br);
//    }
}
