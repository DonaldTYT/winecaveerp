package com.uniinformation.zkbi.clinic;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerAdjustment extends ZkBiComposerBase {


	@Override
    protected void setupAddButton(final BiResult result)
    {
	      if(!result.allowAdd()) return;
	      String MoSync = Erpv4Config.getString(getSessionHelper(), "MoSync");
	      if(MoSync != null && MoSync.equals("clerpslave")) {
	        final Button btnAdd;
	    	if(masterWin.hasFellow("btAdd")) {
	    		btnAdd = (Button) masterWin.getFellow("btAdd");
	    	} else {
	        	btnAdd = new ZkBiButton();
		        btnAdd.setLabel(sessionHelper.getBtLabel("Add Drug Receve"));
		        btnAdd.setId("btAdd");
		        btnAdd.setImage("images/icons/zkweb/038-file-4-25x25.png");
		        btnAdd.setAttribute("tlkey", "bt_master_add");
		        abHelper.addButton(btnAdd,"fa-plus");
	    	}
	    	btnAdd.setTooltiptext(sessionHelper.getLabel("Add New Drug Receive"));
	    	addHotkey('A', btnAdd);
	        btnAdd.addEventListener("onClick",
		        	new ZkBiEventListener() {
		        		public void onZkBiEvent(Event event) throws Exception {
		        			setExtraInfo(null);
		        			result.clearCurrentRec();
		        			doAddOneRow(masterWin,result);
		        		}
		        	}
		        );
	      }
	      {
	        final Button btnAddAdj;
	    	if(masterWin.hasFellow("btAddAdj")) {
	    		btnAddAdj = (Button) masterWin.getFellow("btAddAdj");
	    	} else {
	        	btnAddAdj = new ZkBiButton();
		        btnAddAdj.setLabel(sessionHelper.getBtLabel("Add Drug Adjustment"));
		        btnAddAdj.setId("btAddAdj");
		        btnAddAdj.setImage("images/icons/zkweb/038-file-4-25x25.png");
		        btnAddAdj.setAttribute("tlkey", "bt_master_add");
		        abHelper.addButton(btnAddAdj,"fa-plus");
	    	}
	    	btnAddAdj.setTooltiptext(sessionHelper.getLabel("Add Drug Adjustment"));
	        btnAddAdj.addEventListener("onClick",
		        	new ZkBiEventListener() {
		        		public void onZkBiEvent(Event event) throws Exception {
		        			setExtraInfo("No RemoteHost");
		        			result.clearCurrentRec();
		        			doAddOneRow(masterWin,result);
		        		}
		        	}
		        );
	        if(!result.allowAdd()) btnAddAdj.setVisible(false);	
	      }
    }
}
