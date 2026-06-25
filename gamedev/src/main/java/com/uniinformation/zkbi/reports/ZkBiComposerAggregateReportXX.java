package com.uniinformation.zkbi.reports;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listfoot;
import org.zkoss.zul.Listfooter;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkbi.ZkBiComposerBase.MultiSortMap;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerAggregateReportXX extends ZkBiComposerReport {
	protected String zkfName;
	protected int listboxHeightAdjust = 70;
	HashSet<String>pivotColumns;
	CellCollection rptCol;
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		if(zkfName == null) zkfName = "zkf/reports/AggregateReport.zul";	
    	allowDragDropHeader = true;
		super.doAfterCompose(comp);
//		if(listboxHeightAdjust != 0) 
		adjListboxHeight(listboxHeightAdjust);
	}
	
	protected void onSetupParameterChange(BiResult p_result,String p_id) {
		
	}
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	if(/* screenLayoutG3 */ true) {
    		List<Component> cl = zkbiListTop.getChildren();
    		zkbiListTop.insertBefore(rpth,cl.get(0));
    	} else {
    		zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
    	}
	    final ZkForm zkf1 = new ZkForm(rpth,zkfName);
		try {
			rptCol = new CellCollection();
			final String key = "AGGREPORT_"+result.getView()+"_"+getSessionHelper().getVcode();
			JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null , key);
			if(jo != null) CellCollectionToJsonInterface.JSONObjectToCellCollection(rptCol, jo);
			zkf1.mapCellCollection(rptCol, new EventListener() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("ZkBiComposerAggregateReport event class:%s, name:%s, target:%s", event.getClass(), event.getName(), event.getTarget());
					if(event.getName().equals(Events.ON_CHANGE)) {
						if(event.getTarget() instanceof InputElement) {
							JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(rptCol);
							FilingUtil.storeJson(getSessionHelper().getAgent(), null , "AGGREPORT_"+result.getView()+"_"+getSessionHelper().getVcode(), null, null,jo);
							onSetupParameterChange(result,event.getTarget().getId());
							refresh(result,masterWin,-1,true,true); 
							regenAggregateAndPivot(result);
						}
					}
					if(event.getName().equals(Events.ON_CLICK)) {
						if(!(event.getTarget() instanceof InputElement)) {
							JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(rptCol);
							FilingUtil.storeJson(getSessionHelper().getAgent(), null , "AGGREPORT_"+result.getView()+"_"+getSessionHelper().getVcode(), null, null,jo);
							onSetupParameterChange(result,event.getTarget().getId());
							refresh(result,masterWin,-1,true,true); 
							regenAggregateAndPivot(result);
						}
					}
					
					
					/*
					if (!(event.getTarget() instanceof Textbox || event.getTarget() instanceof Radiogroup || event.getTarget() instanceof Radio || event.getTarget() instanceof Checkbox
							|| event.getTarget() instanceof Datebox))
							*/
					return;
				}
				
			});
		} catch (Exception cex) {
			UniLog.log(cex);
		}
	    
	    /*
	    Listfoot lf = listbox.getListfoot();
	    if(lf == null) {
	    	lf = new Listfoot();
	    	listbox.appendChild(lf);
	    	Listfooter lftr = new Listfooter();
	    	lftr.appendChild(new Label("Subtotal:"));
	    	lf.appendChild(lftr);
	    }
	    */
	}
	
	
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
		Vector<BiColumn> cls = p_result.getColumns();
		for(BiColumn cl : cls) {
			if(cl.getAggregate() != null) {
				if(!cl.isInvisible(p_result.getSessionHelper())) {
				AggregateOrPivot.AGGREGATES aggregate = AggregateOrPivot.AGGREGATES.valueOf(cl.getAggregate());
				if(aggregate == AggregateOrPivot.AGGREGATES.EXPRESSION
					|| aggregate == AggregateOrPivot.AGGREGATES.EXPRESSION2) {
					p_aop.addAggregate(AggregateOrPivot.AGGREGATES.valueOf(cl.getAggregate()),cl.getLabel(),cl.getAggregateExpression());
				} else {
					p_aop.addAggregate(AggregateOrPivot.AGGREGATES.valueOf(cl.getAggregate()),cl.getLabel());
				}
				}
			}
		}
	} 
	protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
		Vector<BiColumn> cls = p_result.getListColumns();
    	Listhead lh = listbox.getListhead();
    	p_aop.setPivotSubtotal(true);
    	for(int i=0;i<cls.size();i++) {
    		BiColumn cl = cls.get(i);
    		Listheader lhdr = (Listheader) lh.getFellow("browser_listheader_"+(i+1));
			if(pivotColumns.contains(cl.getLabel())) {
				p_aop.addCol(cl.getLabel());
				lhdr.setVisible(false);
			}
    	}
	}

    @Override
    public void visibleCols(String preset, Component comp,BiResult result) {
    	super.visibleCols(preset, comp,result);
		pivotColumns=new HashSet<String>();
 		List<String> presetPivotColLabels = null;
   		if (preset != null) {
   			ConditionFieldMap fieldMap = mConditionPresets.getFieldMap(preset);
   			presetPivotColLabels = fieldMap.getPivotColLabels();
   			if (presetPivotColLabels != null)
   				pivotColumns.addAll(presetPivotColLabels);
   		}
   		UniLog.log1("presetPivotColLabels:%s", presetPivotColLabels);
		Vector<BiColumn> cls = result.getListColumns();
   		Listhead lh = (Listhead) comp.query("#browser_listhead");
    	for(int i=0;i<cls.size();i++) {
    		BiColumn cl = cls.get(i);
    		Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(i+1));
    		if (presetPivotColLabels != null) {
    			if (pivotColumns.contains(cl.getLabel()))
    				if(lhdr != null) lhdr.setVisible(false);
    		}
    		else {
    			if (cl.isPivot()) {
    				pivotColumns.add(cl.getLabel());
    				if(lhdr != null) lhdr.setVisible(false);
    			}
    		}
    	}
   		UniLog.log1("pivotColumns:%s", pivotColumns);
    	try {
	    	regenAggregateAndPivot(result);
	    } catch (Exception ex ) {
	    	UniLog.log(ex);
	    }
    }
    
	@Override
	public HashSet<String> getPivotColumns() {
		return pivotColumns;
	}
    
	protected void regenAggregateAndPivot(BiResult p_result) throws Exception {
		AggregateOrPivot aop = new AggregateOrPivot(p_result.getView().getHeader(),true);
		setAggregates(p_result,aop);
		if(aop.getAggsArr().size() <= 0) {
			computeAggregateAndPivot(p_result,null);
			return;
		}
		setPivots(p_result,aop);
		Vector<BiColumn> xv = p_result.getListColumns();
    	Listhead lh = listbox.getListhead();
    	for(int i=0;i<xv.size();i++) {
    		Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(i+1));
    		if(lhdr != null && lhdr.isVisible()) {
    			if(!xv.get(i).isAggregate()) aop.addRow(xv.get(i).getLabel());
    		}
    	}
    	computeAggregateAndPivot(p_result,aop);
	}
	
	
	void updateSelectionString (BiResult p_result) throws CellException {
    	String conditions = inputFieldsList.getCustomCondition();
    	Cell cdc = p_result.getCell("rptCondition");
    	if(cdc != null) {
    	if(conditions != null && !conditions.equals(""))  {
    		cdc.set(BiCellCollection.translateCond(p_result.getView(),conditions,p_result));
		} else {
    		cdc.set("All");
		}
    	}
		
	}

    @Override
    protected void onSelectionChanged(BiResult p_result,MultiSortMap sortMap,Component comp) throws Exception {
//       	zkbiListTop.setVisible(false);
    	updateSelectionString (p_result);
		refresh(p_result,comp,sortMap,false);
    	regenAggregateAndPivot(p_result);
    } 
    

    @Override
    protected void headerDragged(BiResult p_result,Component from,Component to) throws Exception {
    	if(to.getAttribute("pivot_header") != null) {
    		String id = from.getId();
    		Vector<BiColumn> xv = p_result.getListColumns();
    		Listhead lh = listbox.getListhead();
    		for(int i=0;i<xv.size();i++) {
    			String hdrid = "browser_listheader_"+(i+1);
    			if(hdrid.equals(id)) {
    				Listheader lhdr = (Listheader) lh.getFellowIfAny(id);
    				if(lhdr != null && lhdr.isVisible()) {
    					pivotColumns.add(xv.get(i).getLabel());
    					regenAggregateAndPivot(p_result);
    					
    				}
    			}
    		}
    	}
    }

    /***
     *   when column is visible, remove it from pivot column
     *   TODO: it will be obsoleted and replaced by delete pivot button
     *   
     */
    /*@Override
	protected void onListColumnVisibleChanged(BiResult p_result) throws Exception {
    	if(pivotColumns != null) {
    		Vector<BiColumn> xv = p_result.getListColumns();
    		Listhead lh = listbox.getListhead();
    		for(int i=0;i<xv.size();i++) {
    			Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(i+1));
    			if(lhdr != null && lhdr.isVisible()) {
    				pivotColumns.remove(xv.get(i).getLabel());
    			}
    		}
    	}
    	regenAggregateAndPivot(p_result);
	}*/

    @Override
	protected void updatePivotColumns(BiResult result, Map<String, Boolean> updateMap) throws Exception {
    	if(pivotColumns != null) {
    		Vector<BiColumn> xv = result.getListColumns();
    		Listhead lh = listbox.getListhead();
    		for(int i=0;i<xv.size();i++) {
    			Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(i+1));
    			String label = xv.get(i).getLabel();
    			if(lhdr != null) {
    				if (lhdr.isVisible() || (updateMap.get(label) != null && !updateMap.get(label)))
    					pivotColumns.remove(label);
    				else if (!lhdr.isVisible() && (updateMap.get(label) != null && updateMap.get(label)))
    					pivotColumns.add(label);
    			}
    		}
    	}
    	regenAggregateAndPivot(result);
	}

	@Override
	public void biBaseRefresh(BiResult p_result){
		super.biBaseRefresh(p_result);
		try {
			regenAggregateAndPivot(p_result);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
    public void biBaseRefresh(BiResult p_result, MultiSortMap p_sortMap, boolean p_doSearch){
		super.biBaseRefresh(p_result, p_sortMap, p_doSearch);
		try {
			regenAggregateAndPivot(p_result);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	
	@Override
    protected void setupExportButton(final BiResult result) {
    	
    }
	
}
