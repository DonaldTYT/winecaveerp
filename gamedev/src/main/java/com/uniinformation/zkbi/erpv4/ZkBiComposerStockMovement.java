package com.uniinformation.zkbi.erpv4;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerAnalysis;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerStockMovement extends ZkBiComposerReport {

	@Override
    protected void setupExportButton(final BiResult result)
	{
		super.setupExportButton(result);
		Button btnGlPost,btnGlUnPost;
		if(!result.allowUpdate()) return;
		if(!getSessionHelper().getLoginId().equals("hlv")) return;
    	if(masterWin.hasFellow("btSum")) {
    		btnGlPost = (Button) masterWin.getFellow("btSum");
    	} 
    	else {	
	        btnGlPost = new ZkBiButton();
	        btnGlPost.setLabel("Sum");
	        btnGlPost.setId("btSum");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnGlPost, "fa-user");
    	} 
    	
//			Obselated    	
//        btnGlPost.addEventListener("onClick",
//                new EventListener() {
//                	public void onEvent(Event event) throws Exception {
//                			computeAggregateAndPivot(result,
//                					new VectorUtil().addElement("st_iname")
//                						.toVector()
//                						, AggregateOrPivot.AGGREGATES.SUM
//                						,"stmd_qty"
//                						,"stmd_loc"
//                						);
//                	}
//            	}
//            );	
    	
	}
}
