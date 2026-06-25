package com.uniinformation.zkbi.erpv4;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;

import com.google.common.collect.Sets;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultArAp;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerAnalysisReport;

public class ZkBiComposerLocationAsAtMulti extends ZkBiComposerAnalysisReport{
	BiColumn currentPivot = null;
	@Override
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
			zkfName = "zkf/erpv4/AnalysisReportNew.zul";
			super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
			useNewPivotHeader = true;
			rptCol.getCell("cbPivot").addAction(new CellValueAction() {
				@Override
				public void cellAction_onchange(Cell p_value) throws CellException {
					UniLog.log("cbPivot clicked");
//						((BiResultArAp) result).setAsAtDate(p_value.getDate());
						Vector<BiColumn> xv = result.getListColumns();
						if(rptCol.getCell("cbPivot").getBoolean()) {
							for(int i=0;i<xv.size();i++) {
								if(xv.get(i).isPivot()) {
									currentPivot = xv.get(i);
									Listhead lh = listbox.getListhead();
									Listheader lhdr = (Listheader) lh.getFellow("browser_listheader_"+(i+1));
									if(lhdr != null) {
										lhdr.setVisible(false);
									}
									try {
										regenAggregateAndPivot(result);
									} catch (Exception ex) {
										UniLog.log(ex);
									}
								}
							}		
						} else {
							if(currentPivot != null) {
								Listhead lh = listbox.getListhead();
								int idx = xv.indexOf(currentPivot);
								if(idx >= 0) {
									Listheader lhdr = (Listheader) lh.getFellow("browser_listheader_"+(idx+1));
									if(lhdr != null) {
										lhdr.setVisible(true);
									}
								}
								currentPivot = null;
								try {
									regenAggregateAndPivot(result);
								} catch (Exception ex) {
									UniLog.log(ex);
								}
							}
						}
//			    		refresh(result,masterWin,-1,true,true); 
				}
				@Override
				public void cellAction_onfree() throws CellException {
				}
			}
			);
	}

//    @Override
//	protected void onListColumnVisibleChanged(BiResult p_result) throws Exception {
//    	if(currentPivot != null) {
//			Vector<BiColumn> xv = p_result.getListColumns();
//			int idx = xv.indexOf(currentPivot);
//			if(idx >= 0) {
//				Listhead lh = listbox.getListhead();
//				Listheader lhdr = (Listheader) lh.getFellow("browser_listheader_"+(idx+1));
//				if(lhdr.isVisible()) {
//					currentPivot = null;
//					rptCol.getCell("cbPivot").set(false);
//				}
//			}
//    	}
//    	regenAggregateAndPivot(p_result);
//	}
//	
//	@Override
//	protected void createZkfCollection(BiResult p_result) {
//	    if(rptCol == null) {
//	    	rptCol = new CellCollection();
//	    	rptCol.addCell("cbPivot", new Cell(false));
//	    	
//	    }
//	}	
    @Override
	protected void updatePivotColumns(BiResult result, Map<String, Boolean> updateMap) throws Exception {
    	if(currentPivot != null) {
    		Vector<BiColumn> xv = result.getListColumns();
			int idx = xv.indexOf(currentPivot);
			if(idx >= 0) {
				Listhead lh = listbox.getListhead();
				Listheader lhdr = (Listheader) lh.getFellow("browser_listheader_"+(idx+1));
				if(lhdr.isVisible() || (updateMap.get(currentPivot.getLabel()) != null && !updateMap.get(currentPivot.getLabel()))) {
					currentPivot = null;
					rptCol.getCell("cbPivot").set(false);
				}
			}
    	}
    	else {
    		for (BiColumn biColumn : result.getListColumns()) {
    			if (biColumn.isPivot() && updateMap.get(biColumn.getLabel()) != null && updateMap.get(biColumn.getLabel())) {
    				currentPivot = biColumn;
					rptCol.getCell("cbPivot").set(true);
    				break;
    			}
    		}
    	}
    	regenAggregateAndPivot(result);
	}

	@Override
	public HashSet<String> getPivotColumns() {
		return currentPivot != null ? Sets.newHashSet(currentPivot.getLabel()) : new HashSet<String>();
	}

	@Override 
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
		Vector<BiColumn> cls = p_result.getColumns();
		for(BiColumn cl : cls) {
			if(cl.isAggregate()) {
				p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,cl.getLabel());
			}
		}
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
	} 
	@Override 
	protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
		if(currentPivot != null) {
			p_aop.addCol(currentPivot.getLabel());
		}
		/*
		Vector<BiColumn> cls = p_result.getColumns();
		for(BiColumn cl : cls) {
			if(cl.isPrvot()) {
				p_aop.addCol(cl.getLabel());
			}
		}
		*/
	}
}
