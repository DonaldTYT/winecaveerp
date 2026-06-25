package com.uniinformation.zkbi.erpv4;

import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.erpv4.ZkBiComposerSih.PopupCrh;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerGlTran extends ZkBiComposerBase {
	@Override
    protected void setupExtraButton(final BiResult result)
	{
		Button btnGlPost,btnGlUnPost;
		super.setupExtraButton(result);
		if(!result.allowUpdate()) return;
    	if(masterWin.hasFellow("btGlPost")) {
    		btnGlPost = (Button) masterWin.getFellow("btGlPost");
    	} 
    	else {	
	        btnGlPost = new ZkBiButton();
	        btnGlPost.setLabel("Post");
	        btnGlPost.setId("btGlPost");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnGlPost, "fa-user");
    	} 
    	if(masterWin.hasFellow("btGlUnPost")) {
    		btnGlUnPost = (Button) masterWin.getFellow("btGlUnPost");
    	} 
    	else {	
	        btnGlUnPost = new ZkBiButton();
	        btnGlUnPost.setLabel("UnPost");
	        btnGlUnPost.setId("btGlUnPost");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnGlUnPost, "fa-user");
    	} 
        btnGlPost.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		java.util.Set selection = listModelList.getSelection();
        			Vector args = new Vector();
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Transaction to Post",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
//        			final CellCollectionArPayment col = new CellCollectionArPayment();
        			result.beginWork();
        			try {
        			RpcClient rpc = result.getSelectUtil().getRpcClient();
					Value v;
            		for(Iterator it=selection.iterator();it.hasNext();) {
        				Object o = it.next();
        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
        				result.loadOneRecV(idx);
						v = rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
//										Erpv4Config.getCoCode(getSessionHelper()) 
										result.getCellString("tr_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),result.getCellString("tr_cocode"))
									)
							.toVector()
							);
        				v = rpc.callSegment("onlinepost_silent",
        						new VectorUtil()
        						.addElement(result.getCellInt("tr_xno"))
        						.toVector()
        						);
        				if(v == null || !v.toString().startsWith("OK")) {
        					Messagebox.show( "Post Error "+(v == null ? "null" : v.toString()), "Error", Messagebox.OK, Messagebox.ERROR);
        					result.rollbackWork();
        					return;
        				}
        							
            		}
            		result.commitWork();
					refresh(result,null);
        			} catch (Exception ex) {
        				UniLog.log(ex);
        				result.rollbackWork();
        				throw(ex);
        			}
            	}
        	}
        );
        btnGlUnPost.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		java.util.Set selection = listModelList.getSelection();
        			Vector args = new Vector();
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Transaction to UnPost",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
//        			final CellCollectionArPayment col = new CellCollectionArPayment();
        			result.beginWork();
        			try {
        			RpcClient rpc = result.getSelectUtil().getRpcClient();
					Value v;
            		for(Iterator it=selection.iterator();it.hasNext();) {
        				Object o = it.next();
        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
        				result.loadOneRecV(idx);
						v = rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
//										Erpv4Config.getCoCode(getSessionHelper()) 
										result.getCellString("tr_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),result.getCellString("tr_cocode"))
									)
							.toVector()
							);
        				v = rpc.callSegment("reversepost_silent",
        						new VectorUtil()
        						.addElement(result.getCellInt("tr_xno"))
        						.toVector()
        						);
        				if(v == null || !v.toString().startsWith("OK")) {
        					Messagebox.show( "UnPost Error "+(v == null ? "null" : v.toString()), "Error", Messagebox.OK, Messagebox.ERROR);
        					result.rollbackWork();
        					return;
        				}
        							
            		}
            		result.commitWork();
					refresh(result,null);
        			} catch (Exception ex) {
        				UniLog.log(ex);
        				result.rollbackWork();
        				throw(ex);
        			}
            	}
        	}
        );
    	setupBatchModeButton(btnGlPost);
    	setupBatchModeButton(btnGlUnPost);
	}
}
