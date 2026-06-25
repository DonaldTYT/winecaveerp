package com.uniinformation.zkbi;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerExtended extends ZkBiComposerBase{
//	protected int listboxHeightAdjust = 70;

	HashSet<String>pivotColumns;
	protected Boolean headerAggregateFirst = null;
	
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
		List<BiColumn> cls = p_result.getColumns();
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
		cls = p_result.getTempColumnList();
		if(cls != null) {
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
		
		p_aop.addAggregate(AggregateOrPivot.AGGREGATES.valueOf("COUNT"));
	} 
	protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
		Vector<BiColumn> cls = p_result.getListColumns();
    	Listhead lh = listbox.getListhead();
    	p_aop.setPivotSubtotal(true);
    	for(int i=0;i<cls.size();i++) {
    		BiColumn cl = cls.get(i);
    		Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(i+1));
			if(pivotColumns.contains(cl.getLabel())) {
				p_aop.addCol(cl.getLabel());
				if (lhdr != null) lhdr.setVisible(false);
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
		if(headerAggregateFirst != null) aop.setHeaderAggregateFirst(headerAggregateFirst);
		setAggregates(p_result,aop);
//		if(aop.getAggsArr().size() <= 0) {
//			computeAggregateAndPivot(p_result,null);
//			return;
//		}
		setPivots(p_result,aop);
		Vector<BiColumn> xv = p_result.getListColumns();
    	Listhead lh = listbox.getListhead();
    	for(int i=0;i<xv.size();i++) {
    		if (!sessionHelper.isMobile()) {
    			Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(i+1));
    			if(lhdr != null && lhdr.isVisible()) {
    				if(!xv.get(i).isAggregate()) aop.addRow(xv.get(i).getLabel());
    			}
    		}
    		else {
  				if(!xv.get(i).isAggregate()) aop.addRow(xv.get(i).getLabel());
    		}
    	}
    	computeAggregateAndPivot(p_result,aop);
	}
	
	
	void updateSelectionString (BiResult p_result) throws CellException {
    	String conditions = inputFieldsList.getCustomCondition();
    	Cell cdc = p_result.getCurrentCollection().testCell("rptCondition");
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
    	} else {
    		super.headerDragged(p_result, from, to);
    	}
    }

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
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		String colLabel = aggRec.getKey();
		if(isAggregateHidden(colLabel)) return(false);
		BiColumn bc = p_result.getColumnByLabel(colLabel);
		if(bc == null) return(true);
		if(p_aop.isSubTotalColumn(p_idx)) {
			return(bc.isAggregateFlagOn());
		} else {
			return(bc.isPivotFlagOn());
		}
    }	
	
	
	@Override
	protected List<BiColumn> allowSelectAggregateList(BiResult result) {
		return(result.getAggregateColumnList());
	}
}
