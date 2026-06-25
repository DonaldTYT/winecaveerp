package com.uniinformation.zkbi.hw;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellFormula;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerHwInvoice extends ZkBiComposerBase {
	@Override
    protected void setupExportButton(final BiResult result)
	{
		Button btnPrintLabel;
		Button btnUnPost;
		super.setupExportButton(result);
		if(!result.allowUpdate()) return;
    	if(masterWin.hasFellow("btBatchPost")) {
    		btnPrintLabel = (Button) masterWin.getFellow("btBatchPost");
    	} 
    	else {	
	        btnPrintLabel = new ZkBiButton();
	        btnPrintLabel.setLabel("Post");
	        btnPrintLabel.setId("btBatchPost");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnPrintLabel, "fa-print");
    	} 
    	
        btnPrintLabel.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
             		final java.util.Set selection = listModelList.getSelection();
            		RpcClient rpc = sessionHelper.getRpcClient();
        			Vector args = new Vector();
//        			args.add(DateUtil.now());
        			String custCode = null;
        			String ordType= null;
        			String doViewName = null;
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Invoice To Post",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
        			
               		for(Iterator it=selection.iterator();it.hasNext();) {
            				Object o = it.next();
            				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
            				result.loadOneRecV(idx);
            				result.fetchOneRecV(idx);
            				UniLog.log("HAHA do batch post " + result.getCell("inv_rg").getInt());
            				String status = result.getCellString("inv_quostatus");
            				if(status.equals("New") && !result.getCellString("inv_invno").equals("")) {
            					result.getCell("inv_quostatus").set("Confirmed");
            					ReturnMsg rtn = result.updateCurrent();
            					if(rtn != null && !rtn.getStatus()) {
            						Messagebox.show(
            	   							"Post Error " + rtn.getMsg(),
            	   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            	       					return;	
            					}
            				}
               		}	
					refresh(result,null);
            	}
        	}
        );
        
    	if(masterWin.hasFellow("btBatchUnPost")) {
    		btnUnPost = (Button) masterWin.getFellow("btBatchUnPost");
    	} 
    	else {	
	        btnUnPost= new ZkBiButton();
	        btnUnPost.setLabel("Unpost");
	        btnUnPost.setId("btBatchUnPost");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnUnPost, "fa-print");
    	} 
    	
        btnUnPost.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
             		final java.util.Set selection = listModelList.getSelection();
            		RpcClient rpc = sessionHelper.getRpcClient();
        			Vector args = new Vector();
//        			args.add(DateUtil.now());
        			String custCode = null;
        			String ordType= null;
        			String doViewName = null;
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Invoice To UnPost",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
        			
               		for(Iterator it=selection.iterator();it.hasNext();) {
            				Object o = it.next();
            				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
            				result.loadOneRecV(idx);
            				result.fetchOneRecV(idx);
            				UniLog.log("HAHA do batch unpost " + result.getCell("inv_rg").getInt());
            				String status = result.getCellString("inv_quostatus");
            				if(status.equals("Confirmed")) {
            					result.getCell("inv_quostatus").set("New");
            					ReturnMsg rtn = result.updateCurrent();
            					if(rtn != null && !rtn.getStatus()) {
            						Messagebox.show(
            	   							"Post Error " + rtn.getMsg(),
            	   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            	       					return;	
            					}
            				}
               		}	
					refresh(result,null);
               		
            	}
        	}
        );
        setupBatchModeButton(btnPrintLabel);
        setupBatchModeButton(btnUnPost);
	}
}
