package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.CjsUtil;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerAnalysisReport extends ZkBiComposerReport {
    protected boolean useNewPivotHeader = false;
	protected CellCollection rptCol;
	protected CellValueAction rptColChanged = null;
	protected String zkfName = "zkf/erpv4/analysisReport.zul";
	
	protected void createZkfCollection(BiResult p_result) {
	    if(rptCol == null) {
	    	rptCol = new CellCollection();
	    	rptCol.addCell("rptCondition", new Cell(""));
	    	rptCol.addCell("aggregateFunction", new Cell(""));
	    	rptCol.addCell("aggregateExpression", new Cell(""));
	    	rptCol.addCell("pivotColumn", new Cell(""));
	    	rptCol.addCell("showCount", new Cell(false));
	    }
	}
	
	protected void processOptionEvent(BiResult result, ZkForm zkf1,Event arg0) throws Exception{
		if(arg0.getTarget().getId().equals("btGenerate")) {
			if(false) {
				Messagebox.show("Condition Incomplete");
			} else {
	        	refresh(result,masterWin,-1,true,true); 
//	        	zkbiListTop.setVisible(true);
			}
		}
		if(arg0.getTarget().getId().equals("btCreateCol")) {
			Listhead listhead = (Listhead) listbox.query("Listhead");
			result.addTempColumn(rptCol.getCellString("vclabel"), rptCol.getCellString("vclabel"), rptCol.getCellString("vcformula"), "", rptCol.getCellString("vctype"), null,0);
			setupListHeader(result,masterWin,defaultSortIdx,defaultSortDesc,listhead);
		}
		if(arg0.getTarget().getId().equals("btPlot")) {
//			JSONObject cjsData = CjsUtil.getDemoData2();
			List<Integer> filteredTrList = null;
			if(listModelList.size() > 0) {
				Object o = listModelList.get(0);
				if(o instanceof TrStatFilter) {
					filteredTrList = new ArrayList<Integer>();
					for(Object oo : listModelList) {
						int realIdx = getTrIdxByObj(listModelList, oo);
						filteredTrList.add(realIdx);
					}
				}
			}
			JSONObject ja = result.getAnalysedData(filteredTrList);
			CjsUtil cjs = new CjsUtil(ja);
			JSONObject cjsData = cjs.analysedDataToCjsData(CjsUtil.TYPE_BARCHART, "Chart Header", null, null, null);
			Div chartDiv = (Div) zkf1.getComponent("cjsdiv");
//			CjsUtil.createChart(chartDiv,cjsData);
			Components.removeAllChildren(chartDiv);
			Div cjsDiv = new Div();
			if(isMobile()) {
				//barChartDiv.setWidth("100%");
				cjsDiv.setWidth(sessionHelper.getScreenWidth() - 30 + "px");  //andrew190918: dirty way to fix chart width
//				cjsDiv.setWidth("1800px");
				cjsDiv.setHeight("400px");
			} else {
//				cjsDiv.setWidth("1800px");
//				int px = result.getAggregateOrPivotList().size() * 32;
				int px = result.aggregateOrPivotSize() * 32;
				if( px < 800) px = 800;
				if( px > 4096) px = 4096;
				cjsDiv.setWidth(""+px+"px");
				cjsDiv.setHeight("400px");
			}
			cjsDiv.setParent(chartDiv);
			chartDiv.invalidate();
			CjsUtil.createChart(cjsDiv,cjsData);
		}	
	}
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	{
    		  String s = BiConfig.getString(result.getSessionHelper(), "UseNewPivotHeader");
    		  if("Y".equals(s)) {
    			  useNewPivotHeader = true;
    		  }
    	}
    	Div rpth = new Div();
    	if(/* screenLayoutG3 */ true) {
    		List<Component> cl = zkbiListTop.getChildren();
    		zkbiListTop.insertBefore(rpth,cl.get(0));
    	} else {
    		zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
    	}
    	
	    final ZkForm zkf1 = new ZkForm(rpth,zkfName);
//    	BiReportInterface lrf = (BiReportInterface) result;
    	if(rptColChanged == null) {
    		rptColChanged = new CellValueAction() {
    			@Override
		  		public void cellAction_onchange(Cell p_value) throws CellException {
    				try {
    					regenAggregateAndPivot(result);
    				} catch (Exception ex) {
    					UniLog.log(ex);
    					throw new CellException(ex.toString());
    				}
    			}
    			@Override
    			public void cellAction_onfree() throws CellException {
    			}
    		};
		
    	};
//	    if(rptCol == null) rptCol = new CellCollection();
//	    Cell c = null;
//	    if(rptCol.testCell("rptCondition") == null) {
//	    	rptCol.addCell("rptCondition", new Cell(""));
//	    }
//	    if(rptCol.testCell("aggregateFunction") == null) {
//	    	c = rptCol.addCell("aggregateFunction", new Cell(""));
//	    	c.setItemList(
//    			new VectorUtil()
//    			.addElement("")
//    			.addElement(AggregateOrPivot.AGGREGATES.SUM.toString())
//    			.addElement(AggregateOrPivot.AGGREGATES.AVERAGE.toString())
//    			.addElement(AggregateOrPivot.AGGREGATES.COUNT.toString())
//    			.toVector()
//    			);
//	    }
//    	c.addAction( rptColChanged );
//	    if(rptCol.testCell("aggregateExpression") == null) {
//	    	c = rptCol.addCell("aggregateExpression", new Cell(""));
//	    	List<String> l= result.getAggregateColumnList();
//	    	GipiNamedItemList gipi = new GipiNamedItemList();
//	    	for(String fn : l) {
//	    		gipi.appendItem(fn, result.getColumnByLabel(fn).getEngName());
//	    	}
//	    	c.setItemPropertyInterface(gipi);
//	    	//c.setItemList( l);
//	    }
//    	c.addAction( rptColChanged );
//	    if(rptCol.testCell("pivotColumn") == null) {
//	    	c = rptCol.addCell("pivotColumn", new Cell(""));
//	    	List<String> l = result.getPivotableColumnList();
//	    	GipiNamedItemList gipi = new GipiNamedItemList();
//	    	for(String fn : l) {
//	    		gipi.appendItem(fn, result.getColumnByLabel(fn).getEngName());
//	    	}
//	    	c.setItemPropertyInterface(gipi);
//	    	//c.setItemList( l );
//	    }
//    	c.addAction( rptColChanged );
//    	
//    	c = rptCol.addCell("showCount", new Cell(false));
//    	c.addAction( rptColChanged );

    	createZkfCollection(result);
	    Cell c = null;
	    if((c = rptCol.testCell("aggregateFunction")) != null) {
	    	c.setItemList(
    			new VectorUtil()
    			.addElement("")
    			.addElement(AggregateOrPivot.AGGREGATES.SUM.toString())
    			.addElement(AggregateOrPivot.AGGREGATES.COUNT.toString())
    			.addElement(AggregateOrPivot.AGGREGATES.STRCAT.toString())
    			.toVector()
    			);
	    	c.addAction( rptColChanged );
	    }
	    if((c = rptCol.testCell("aggregateExpression")) != null) {
	    	List<BiColumn> l= result.getAggregateColumnList();
	    	GipiNamedItemList gipi = new GipiNamedItemList();
	    	for(BiColumn fn : l) {
	    		gipi.appendItem(fn, fn.getEngName());
	    	}
	    	c.setItemPropertyInterface(gipi);
	    	if(l.size() > 0) {
	    		try {
	    			c.set(l.get(0));
	    		} catch (CellException cex ) {
	    			UniLog.log(cex);
	    		}
	    	}
	    	//c.setItemList( l);
	    	c.addAction( rptColChanged );
	    }
	    if((c = rptCol.testCell("pivotColumn")) != null) {
	    	List<String> l = result.getPivotableColumnList();
	    	GipiNamedItemList gipi = new GipiNamedItemList();
	    	for(String fn : l) {
	    		gipi.appendItem(fn, result.getColumnByLabel(fn).getEngName());
	    	}
	    	c.setItemPropertyInterface(gipi);
	    	//c.setItemList( l );
	    	c.addAction( rptColChanged );
	    }
	    if((c = rptCol.testCell("showCount")) != null) {
	    	c.addAction( rptColChanged );	
	    }
	    
	    try {
	    	updateSelectionString (result);
	    	zkf1.mapCellCollection(rptCol,new EventListener() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						processOptionEvent(result, zkf1,arg0);
//						if(arg0.getTarget().getId().equals("btGenerate")) {
//							if(false) {
//								Messagebox.show("Condition Incomplete");
//							} else {
//        			        	refresh(result,masterWin,-1,true,true); 
//        			        	//zkbiListTop.setVisible(true);
//							}
//						
//						}
//						if(arg0.getTarget().getId().equals("btCreateCol")) {
//							Listhead listhead = (Listhead) listbox.query("Listhead");
//							result.addTempColumn(rptCol.getCellString("vclabel"), rptCol.getCellString("vclabel"), rptCol.getCellString("vcformula"), "", rptCol.getCellString("vctype"), 0);
//							setupListHeader(result,masterWin,defaultSortIdx,defaultSortDesc,listhead);
//						}
//						if(arg0.getTarget().getId().equals("btPlot")) {
//							//JSONObject cjsData = CjsUtil.getDemoData2();
//							List<Integer> filteredTrList = null;
//							if(listModelList.size() > 0) {
//								Object o = listModelList.get(0);
//								if(o instanceof TrStatFilter) {
//									filteredTrList = new ArrayList<Integer>();
//									for(Object oo : listModelList) {
//										int realIdx = getTrIdxByObj(listModelList, oo);
//										filteredTrList.add(realIdx);
//									}
//								}
//							}
//							JSONObject ja = result.getAnalysedData(filteredTrList);
//							CjsUtil cjs = new CjsUtil(ja);
//							JSONObject cjsData = cjs.analysedDataToCjsData(CjsUtil.TYPE_BARCHART, "Chart Header", null, null, null);
//							Div chartDiv = (Div) zkf1.getComponent("cjsdiv");
//							//CjsUtil.createChart(chartDiv,cjsData);
//							Components.removeAllChildren(chartDiv);
//							Div cjsDiv = new Div();
//							if(isMobile()) {
//								//barChartDiv.setWidth("100%");
//								cjsDiv.setWidth(sessionHelper.getScreenWidth() - 30 + "px");  //andrew190918: dirty way to fix chart width
//								//cjsDiv.setWidth("1800px");
//								cjsDiv.setHeight("400px");
//							} else {
//								//cjsDiv.setWidth("1800px");
//								int px = result.getAggregateOrPivotList().size() * 32;
//								if( px < 800) px = 800;
//								if( px > 4096) px = 4096;
//								cjsDiv.setWidth(""+px+"px");
//								cjsDiv.setHeight("400px");
//							}
//							cjsDiv.setParent(chartDiv);
//							chartDiv.invalidate();
//							CjsUtil.createChart(cjsDiv,cjsData);
//						}
	    			}
	    		}
	    	);
	    	regenAggregateAndPivot(result);
	    } catch (Exception ex ) {
	    	UniLog.log(ex);
	    }
	}
	
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
		String aggregate = rptCol.getCellString("aggregateFunction");
		if(!aggregate.equals("")) {
			p_aop.addAggregate(AggregateOrPivot.AGGREGATES.valueOf(aggregate), rptCol.getCellString("aggregateExpression"));
		}
		if(rptCol.testCell("showCount") != null) {
			if(rptCol.getBoolean("showCount")) p_aop.addAggregate(AggregateOrPivot.AGGREGATES.valueOf("COUNT"));
		}
	} 
	
	protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
		String pivot = rptCol.getCellString("pivotColumn");
		if(!pivot.equals("")) {
			p_aop.addCol(pivot);
		}
	}
			
	
	protected void regenAggregateAndPivot(BiResult p_result) throws Exception {
		AggregateOrPivot aop = new AggregateOrPivot(p_result.getView().getHeader(),useNewPivotHeader);
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
    		if (sessionHelper.isMobile() || (lhdr != null && lhdr.isVisible())) {
    			if(!xv.get(i).isAggregate()) aop.addRow(xv.get(i).getLabel());
    		}
    	}
    	computeAggregateAndPivot(p_result,aop);
	}

	void updateSelectionString (BiResult p_result) throws CellException {
    	String conditions = inputFieldsList.getCustomCondition();
    	Cell cdc = rptCol.testCell("rptCondition");
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
    
//    @Override
//	protected void onListColumnVisibleChanged(BiResult p_result) throws Exception {
//    	regenAggregateAndPivot(p_result);
//	}

	@Override
	public void biBaseRefresh(BiResult p_result){
		super.biBaseRefresh(p_result);
		try {
			regenAggregateAndPivot(p_result);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
}
