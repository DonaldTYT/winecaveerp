package com.uniinformation.zkbi.erpv4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultCrhAr;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiPopupBase;
import com.uniinformation.zkbi.erpv4.ZkBiComposerSih.PopupCrh;
import com.uniinformation.zkbi.erpv4.ZkBiComposerSih.PopupCrh.CrdDetail;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerSihReport extends com.uniinformation.zkbi.ZkBiComposerAnalysisReport {
	
@Override
public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){

	aggregateOffset = 0;
	if(zkfName == null) zkfName = "zkf/erpv4/ArApInvoice.zul";
	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
}

@Override
protected void createZkfCollection(BiResult p_result) {
    if(rptCol == null) {
    	rptCol = p_result.getCurrentCollection();
    	if(rptCol.testCell("pivotCcy") == null) {
    		Cell c = rptCol.addCell("pivotCcy", new ColumnCell(false,Cell.VMODE_NORMAL));
    		c.addAction(rptColChanged);
    		
    	}
    	if(rptCol.testCell("pivotDuedate") == null) {
    		Cell c = rptCol.addCell("pivotDuedate", new ColumnCell(false,Cell.VMODE_NORMAL));
    		c.addAction(rptColChanged);
    	}
    	if(rptCol.testCell("showaggs") == null) {
    		Cell c = rptCol.addCell("showaggs", new Cell(0,Cell.VMODE_NORMAL));
    		c.addAction(rptColChanged);
    	}
    	/*
    	if(p_result.getView().getTable().getName().equals("sih_ar")) {
    		rptCol.addCell("rpttitle",new Cell( "Receivable Invoices",Cell.VMODE_NORMAL));
    	}
    	if(p_result.getView().getTable().getName().equals("sih_ap")) {
    		rptCol.addCell("rpttitle",new Cell( "Payable Invoices",Cell.VMODE_NORMAL));
    	}
    	*/
    }
}
@Override 
protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
	int showAggs = 0;
	if(p_result.getNativeCell("showaggs") != null) {
		showAggs = p_result.getCellInt("showaggs");
	}
	if(p_result.getNativeCell("pivotCcy") != null &&
		p_result.getNativeCell("pivotCcy").getBoolean()) {
		if(showAggs != 2) p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"sih_total");
		if(showAggs != 1) p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"sih_osbal");
	} else {
		if(p_result.getNativeCell("pivotDuedate") != null &&
				p_result.getNativeCell("pivotDuedate").getBoolean()) {
		} else {
			if(showAggs != 2) p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"sih_total");
			if(showAggs != 1) p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"sih_osbal");
		}
		if(showAggs != 2) p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"sih_ltotal");
		if(showAggs != 1) p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"sih_losbal");
	}
} 
@Override 
protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
	if(p_result.getNativeCell("pivotCcy") != null) {
		if(p_result.getNativeCell("pivotCcy").getBoolean()) {
			p_aop.addCol("sih_cid");
		}
	}
	if(p_result.getNativeCell("pivotDuedate") != null) {
		if(p_result.getNativeCell("pivotDuedate").getBoolean()) {
			p_aop.addCol("sih_dueperiod");
		}
	}
}
/*
protected void processOptionEvent(BiResult result, ZkForm zkf1,Event arg0) throws Exception{
}
*/
}
