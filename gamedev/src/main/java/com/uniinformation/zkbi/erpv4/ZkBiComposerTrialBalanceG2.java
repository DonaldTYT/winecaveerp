package com.uniinformation.zkbi.erpv4;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultBalanceSheet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.GlBalanceCalculation;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;
public class ZkBiComposerTrialBalanceG2 extends ZkBiComposerAggregateReport {
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
	    final ZkForm zkf1 = new ZkForm(rpth,"zkf/erpv4/erpv4TrialBalance.zul");
		Date td = DateUtil.today();
		Date md = Erpv4Config.getMaxPcStart(result.getSelectUtil(), Erpv4Config.getDefaultCoCode(getSessionHelper()));
		if(td.before(md)) md = DateUtil.monthStart(td);
    	try {
    		zkf1.mapCellCollection(result.getCurrentCollection(),new EventListener() {
		    	@Override
		    	public void onEvent(Event arg0) throws Exception {
		    		// TODO Auto-generated method stub
		    		UniLog.log("Trialbalance onEvent " + arg0.getName());
//		    		refresh(result,masterWin,-1,true,true); 
		    		biBaseRefresh(result);
		    		
		    	}
	    	
	    	}
    		);
    	} catch(Exception ex) {
    		UniLog.log(ex);
    	}
	}
	@Override
	protected void setupExportButton(final BiResult result) {
		super.setupExportButton(result);
		final Button btnClearCache = new ZkBiButton();
		btnClearCache.setLabel("Clear Cache");
		abHelper.addButton(btnClearCache);
		btnClearCache.addEventListener("onClick", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("button clicked");
				GlBalanceCalculation.clearAcu(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()), null);
			}
		});
	}
	
}
