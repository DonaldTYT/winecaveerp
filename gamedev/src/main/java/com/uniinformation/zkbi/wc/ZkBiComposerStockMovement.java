package com.uniinformation.zkbi.wc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.WordPressHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.erpv4.LedgerReportBiActionHandler;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerStockMovement extends com.uniinformation.zkbi.ZkBiComposerAnalysisReport{
	@Override

	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		aggregateOffset = 0;
		zkfName = "zkf/winecave/StockMovement.zul";
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}


	@Override
	protected void createZkfCollection(final BiResult p_result) {
	    if(rptCol == null) {
	    	rptCol = p_result.getCurrentCollection();
	    	if(rptCol.testCell("toDate") != null) {
	    		try {
	    			rptCol.getCell("toDate").set(DateUtil.today());
	    		} catch (CellException cex) {
	    			UniLog.log(cex);
	    		}
	    	}
	    	
	    	if(rptCol.testCell("fromDate") != null) {
	    		rptCol.getCell("fromDate").addAction(
	    				new CellValueAction() {

							@Override
							public void cellAction_onchange(Cell p_value) throws CellException {
								// TODO Auto-generated method stub
								refresh(p_result,masterWin,null,false);
								try {
									regenAggregateAndPivot(p_result);
								} catch (Exception ex) {
									UniLog.log(ex);
									throw new CellException(ex.toString());
								}
							}

							@Override
							public void cellAction_onfree() throws CellException {
								// TODO Auto-generated method stub
								
							}
	    					
	    				}
	    				);
	    	}
		/*
	    	if(rptCol.testCell("fromDate") == null) {
	    		Cell c = rptCol.addCell("pivotCcy", new ColumnCell(DateUtil.today(),Cell.VMODE_NORMAL));
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
	    */
	    }
	}
	@Override 
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
		/*
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
		*/
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"pds_begbal");
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"stmd_inqty");
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"stmd_outqty");
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"pds_endbal");
	} 
	@Override 
	protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
		/*
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
		*/
	}
	@Override
	protected void setupExportButton(final BiResult result) {
		super.setupExportButton(result);
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbPrintLedger","Print Ledger","fa-user",
				new StockMovementLedgerBiActionHandler(this,result, masterWin,
						new StockMovementLedgerBiActionHandler.OnReportInit() {
							@Override
							public void onInit(StockMovementLedgerBiActionHandler p_hdr) {
								HashSet<String> groupColumns = new HashSet<String>();
								Vector<BiColumn> xv = result.getListColumns();
						    	Listhead lh = listbox.getListhead();
						    	for(int i=0;i<xv.size();i++) {
						    		Listheader lhdr = (Listheader) lh.getFellow("browser_listheader_"+(i+1));
						    		if(lhdr.isVisible()) {
						    			groupColumns.add(xv.get(i).getLabel());
						    		}
						    	}
				    			p_hdr.setGroupColumns(groupColumns);
								p_hdr.setFromDate(rptCol.getDate("fromDate"));
								p_hdr.setToDate(DateUtil.today());
								p_hdr.setSubTitle("Stock Ledger Report");
								p_hdr.setOutputFileName("stockledger_"+DateUtil.toDateString(rptCol.getDate("fromDate"),"yyyymmdd")+"-"+DateUtil.toDateString(DateUtil.today(),"yyyymmdd"));
								p_hdr.setShowBreakDown(true);
								p_hdr.setOutputType(LedgerReportBiActionHandler.OUTPUT_Excel);
							}
						}
				)
			);	
		
	}
}
