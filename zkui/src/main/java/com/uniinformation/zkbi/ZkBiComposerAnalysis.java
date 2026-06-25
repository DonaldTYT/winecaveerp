package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.MessageboxDlg;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerAnalysis extends ZkBiComposerReport {
	static public enum AGGREGATES {
		COUNT,
		COUNT_UNIQUE,
		SUM,
		AVERAGE,
		MEDIAN,
		STANDARD_DEVIATION,
		MAX,
		MIN,
		PERCENT_TOTAL,
		PERCENT_ROW,
		PERCENT_COL,
	};
	
	static HashMap<String,Enum> NAME_TO_ENUM_HASH = null;
	static Enum getAggregateEnum(String p_name) {
		if(NAME_TO_ENUM_HASH == null) {
			NAME_TO_ENUM_HASH = new HashMap<String,Enum> ();
			for(Enum ee : AGGREGATES.values() ) {
				NAME_TO_ENUM_HASH.put(ee.name(), ee);
			}
		}
		return(NAME_TO_ENUM_HASH.get(p_name));
	}
    protected Div displayDiv=null;
    protected Div controlPanel = null;
    protected Vbox buttonPanel = null;
    protected JSONObject jsonAnalysedData = null;
    protected AnalysisReport currentRpt = null;
    
    Div daDiv=null;
    Hlayout daArea = null;
    JSONArray jsonRawDataArr = null;


    protected class AnalysisReport {
    	public ArrayList<ColRec> getColsArr() {
			return colsArr;
		}

		public ArrayList<RowRec> getRowsArr() {
			return rowsArr;
		}

		public ArrayList<AggregateRec> getAggsArr() {
			return aggsArr;
		}

		public String getRptName() {
			return rptName;
		}

		public class RowRec {
    		public boolean isHided() {
				return hided;
			}
			public String getName(BiResult p_result) {
    			return(p_result.getCell(rowName).getBiColumn().getEngName());
			}
			boolean hided = false;
    		String rowName;
    		String style;
    		public RowRec(String p_rowName,String p_style) {
    			rowName = p_rowName;
    			style = p_style;
    		}
    		public String getKey() {
				return rowName;
    		}
    	}
    	public class ColRec {
    		public boolean isHided() {
				return hided;
			}
			public String getName(BiResult p_result) {
    			return(p_result.getCell(colName).getBiColumn().getEngName());
			}
			boolean hided = false;
    		String colName;
    		String style;
    		public ColRec(String p_rowName,String p_style) {
    			colName = p_rowName;
    			style = p_style;
    		}
    		public String getKey() {
				return colName;
    		}
    	}
    	public class AggregateRec {
    		private String name;
    		AGGREGATES aggregate;
    		private String format;
    		private String width;
    		private String style;
    		String[] varName;
    		boolean hided;
    		public boolean isHided() {
				return hided;
			}
    		public AggregateRec(String p_name,AGGREGATES p_aggregate,String p_format,String p_width,String p_style,String... args) {
    			name = p_name;
    			aggregate = p_aggregate;
    			format = p_format;
    			width = p_width;
    			style = p_style;
    			varName = args;
    		}
    		public String getKey() {
    			String s = null ;
    			for(String ss : varName) {
    				if(s == null) s = ss ; else s += "," + ss;
    			}
				return (aggregate.name()+"("+(s==null ? "": s)+ ")");
    		}
    		public String getName(BiResult p_result) {
    			if(name == null) {
    				switch (aggregate) {
    				case COUNT:
    				case COUNT_UNIQUE:
    						return("Count");
    				case SUM:
    						return(p_result.getCell(varName[0]).getBiColumn().getEngName());
    				case AVERAGE:
    				case MEDIAN:
    				case STANDARD_DEVIATION:
    				case MAX:
    				case MIN:
    						return(aggregate.name());
    				case PERCENT_TOTAL:
    				case PERCENT_ROW:
    				case PERCENT_COL:
    						return("%");
    				}
    					
    			} 
    			return(name);
    		}
    		public String getFormat(BiResult p_result) {
    			if(format == null) {
    				switch (aggregate) {
    				case COUNT:
    				case COUNT_UNIQUE:
    						return("##,##0");
    				case SUM: {
    						String fmt = p_result.getCell(varName[0]).getBiColumn().getFormat();
    						if(fmt != null && !fmt.trim().equals("")) return(fmt); else return("###,###,##0");
    					}
    				case AVERAGE:
    				case MEDIAN:
    				case STANDARD_DEVIATION:
    				case MAX:
    				case MIN: {
    						String fmt = p_result.getCell(varName[0]).getBiColumn().getFormat();
    						if(fmt != null && !fmt.trim().equals("")) return(fmt); else return("###,###,##0");
    					}
    				case PERCENT_TOTAL:
    				case PERCENT_ROW:
    				case PERCENT_COL:
    						return("##0.0");
    				}
    					
    			} 
    			return(format);
    		}
    		public String getWidth(BiResult p_result) {
    			if(width == null) {
    				switch (aggregate) {
    				case COUNT:
    				case COUNT_UNIQUE:
    						return("70px");
    				case SUM: {
    						return("100px");
    					}
    				case AVERAGE:
    				case MEDIAN:
    				case STANDARD_DEVIATION:
    				case MAX:
    				case MIN: {
    						return("100px");
    					}
    				case PERCENT_TOTAL:
    				case PERCENT_ROW:
    				case PERCENT_COL:
    						return("50px");
    				}
    					
    			} 
    			return(width);
    		}
    	}

    	protected ArrayList<ColRec> colsArr = null;
        protected ArrayList<RowRec> rowsArr = null;
        protected ArrayList<AggregateRec> aggsArr = null;
        protected String rptName = null;
        
    	public AnalysisReport(String p_rptName) {
    		rptName = p_rptName;
    		colsArr = new ArrayList<ColRec>();
    		rowsArr = new ArrayList<RowRec>();
    		aggsArr = new ArrayList<AggregateRec>();
    	}
    	
    	public AnalysisReport addRow(String p_rowName) {
    		return(addRow(p_rowName,null,-1));
    	}
    	public AnalysisReport addRow(String p_rowName,String p_style) {
    		return(addRow(p_rowName,p_style,-1));
    	}
    	public AnalysisReport addRow(String p_rowName,String p_style,int p_idx) {
    		if(p_idx < 0) rowsArr.add(new RowRec(p_rowName,p_style)); else  rowsArr.add(p_idx, new RowRec(p_rowName,p_style));
    		return(this);
    	}
    	public AnalysisReport addCol(String p_colName) {
    		return(addCol(p_colName,null,-1));
    	}
    	public AnalysisReport addCol(String p_colName,String p_style) {
    		return(addCol(p_colName,p_style,-1));
    	}
    	public AnalysisReport addCol(String p_colName,String p_style,int p_idx) {
    		if(p_idx < 0) colsArr.add(new ColRec(p_colName,p_style)); else  colsArr.add(p_idx, new ColRec(p_colName,p_style));
    		return(this);
    	}
    	public AnalysisReport addAggregate(AGGREGATES p_aggregate,String... p_args) {
    		return addAggregate(null,p_aggregate,null,null,null,-1,p_args);
    	}
    	public AnalysisReport addAggregate(String p_name,AGGREGATES p_aggregate,String p_format,String p_width,String p_style,int p_idx,String... p_args) {
    		if(p_idx < 0) 
    			aggsArr.add(new AggregateRec(p_name,p_aggregate,p_format,p_width,p_style,p_args)); 
    		else  
    			aggsArr.add(p_idx, new AggregateRec(p_name,p_aggregate,p_format,p_width,p_style,p_args));
    		
    		return(this);
    	}
    	
    	public AnalysisReport hideRow(String p_rowName,boolean p_sw) {
    		for(RowRec rec : rowsArr) {
    			if(rec.getKey().equals(p_rowName)) {
    				rec.hided = p_sw;
    			}
    		}
    		return(this);
    	}
    	public AnalysisReport hideCol(String p_colName,boolean p_sw) {
    		for(ColRec rec : colsArr) {
    			if(rec.getKey().equals(p_colName)) {
    				rec.hided = p_sw;
    			}
    		}
    		return(this);
    	}
    	public AnalysisReport hideAggregate(String p_aggName,boolean p_sw) {
    		for(AggregateRec rec : aggsArr) {
    			if(rec.getKey().equals(p_aggName)) {
    				rec.hided = p_sw;
    			}
    		}
    		return(this);
    	}
    }
    
    Window daWindow=null;
	@Override
    protected void setupDataAnalysisButton(final BiResult result) {
        final Button btn;
        
    	if(masterWin.hasFellow("btDataAnalysis")) {
    		btn = (Button) masterWin.getFellow("btDataAnalysis");
    	} else {
        	btn = new ZkBiButton();
	        btn.setLabel(sessionHelper.getBtLabel("Analysis"));
	        btn.setAttribute("tlkey", "bt_master_dataanalysis");
	        btn.setId("btDataAnalysis");
	        //actionBar.appendChild(btn);
    	}
        			daDiv = new Div();
        			daDiv.setWidth("100%");
        			daDiv.setSclass("zkbi-95pct-height");
        			//daDiv.setHeight("calc(100% - 45px)");
        			daDiv.setStyle("overflow:auto;");

        			daArea = new Hlayout();
        			daArea.setParent(daDiv);
        			Vlayout vl = new Vlayout();
        			vl.setHflex("1");
        			vl.setParent(daArea);
        			controlPanel = new Div();
        			controlPanel.setWidth("250px");
        			if(isMobile()) {
//        				daArea.setWidth("1000px");
        				daArea.setWidth(""+(sessionHelper.getScreenWidth()-30)+"px");
        				final Popup pp = new Popup();
        				pp.setParent(vl);
        				controlPanel.setStyle("overflow:auto;");
        				controlPanel.setHeight("500px");
        				controlPanel.setParent(pp);
        				Button bt = new ZkBiButton("Setting");
        				bt.setParent(vl);
        				bt.addEventListener(Events.ON_CLICK,
        					new EventListener<Event>() {
        						public void onEvent(Event event) throws Exception {
//        							pp.open(0, 0);
        							pp.open(daArea);
        						}
        					}
        				);
        			} else {
        				controlPanel.setParent(daArea);
        			}
        			buttonPanel = new Vbox();
        			buttonPanel.setHflex("1");
        			buttonPanel.setAlign("center");
       				buttonPanel.setParent(controlPanel);
        			
        			
        			displayDiv = new Div();
        			displayDiv.setParent(vl);
        			
        			daArea.setVisible(false);
	    daWindow = new Window();
	    daWindow.setVisible(false);
	    daWindow.setParent(masterWin);
        			
    	btn.setTooltiptext(sessionHelper.getLabel("Run Data Analysis Report"));
    	addHotkey('N', btn);
        btn.addEventListener(Events.ON_CLICK,
        	new EventListener<Event>() {
        		public void onEvent(Event event) throws Exception {
//        			if (listbox == null)
//        				return;
//        			prepareData(result);
//        			setupOutput(result);
//        			drawZkBiOutput(result);
//
//					MessageboxDlg daDlg = ZkUtil.buildMessageboxDlg("Data Analysis", 
//						daDiv, 
//						new Messagebox.Button[]{Messagebox.Button.OK}, 
//						masterWin, 
//						null
//					);
//					daDlg.setId("data-analysis-dlg");
//					daDlg.setWidth("100%");
//					daDlg.setHeight("100%");
//					daDlg.setVisible(false);
//					daDlg.doModal();
        			
        			
        			if (listbox == null) return;

        			prepareData(result);
        			setupOutput(result);
        			drawZkBiOutput(result);
        			
        			hideListPanel();
        			daWindow.setWidth("100%");
        			//daWindow.setHeight("600px");
        			//daWindow.setSclass("zkbi-95pct-height");
        			//daDiv.setSclass("zkbi-95pct-height");
        			daWindow.setVisible(true);
					daDiv.setParent(daWindow);        			
        			
        			
        		}
        	}
        );
        abHelper.addButton(btn,"flaticon-bes-graph");
//        final Button btn2 = new ZkBiButton("Run Report");
//    	btn.setTooltiptext(sessionHelper.getLabel("Data Analysis"));
//    	addHotkey('N', btn);
//	    actionBar.appendChild(btn2);
//	    daWindow = new Window();
//	    daWindow.setVisible(false);
//	    daWindow.setParent(masterWin);
//        btn2.addEventListener(Events.ON_CLICK,
//        	new EventListener<Event>() {
//        		public void onEvent(Event event) throws Exception {
//        			if (listbox == null) return;
//
//        			prepareData(result);
//        			setupOutput(result);
//        			drawZkBiOutput(result);
//        			
//        			hideListPanel();
//        			daWindow.setWidth("100%");
//        			//daWindow.setHeight("600px");
//        			//daWindow.setSclass("zkbi-95pct-height");
//        			//daDiv.setSclass("zkbi-95pct-height");
//        			daWindow.setVisible(true);
//					daDiv.setParent(daWindow);
//        			
//        		}
//        	}
//        );
//        abHelper.addButton(btn2);
    }
	
	protected void  setupOutput(BiResult result) throws JSONException {
//       				renderer = "ZKBI";
	}
	void  prepareData(BiResult result) throws JSONException {
        			Vector<BiColumn> listColumns = result.getListColumns();
        			jsonAnalysedData = new JSONObject();
    				jsonRawDataArr = new JSONArray();
    				/*
    				List<Integer> cols = new ArrayList<Integer>();
        			for (Component tmpComp : listbox.queryAll("Listheader")){
        				Listheader tmpListheader = (Listheader) tmpComp;
        				if (tmpListheader.isVisible() && tmpListheader.getId().startsWith("browser_listheader_")) {
        					int col = Integer.parseInt(tmpListheader.getId().substring(19)) - 1;
        					if (col >= 0) {
        						cols.add(col);
        					}
        				}
        			}
        			*/
//       				for (int col : cols) {
//       					BiColumn biColumn = listColumns.get(col);
//       					jsonRowsArr.put(biColumn.getEngName());
//       				}
       				for (int i = 0; i < listModelList.size(); i++) {
       					int idx = getTrIdxByObj(listModelList, listModelList.get(i), -1);
       					if (idx >= 0) {
       						result.loadOneRecV(idx);
	        				JSONObject jsonObj = new JSONObject();
	        				
        					for (int col=0; col<result.getListColumns().size(); col++){
	        					BiColumn biColumn = listColumns.get(col);
	        					if (biColumn.getColumnType().trim().matches("float|money"))
	        						jsonObj.put(biColumn.getEngName(), result.getCell(biColumn.getLabel()).getDouble());
	        					else if (biColumn.getColumnType().trim().matches("integer")) {
	        						jsonObj.put(biColumn.getEngName(), result.getCell(biColumn.getLabel()).getInt());
	        					} else	 {
//	        						jsonObj.put(biColumn.getEngName(), result.getCell(biColumn.getLabel()).getString());
	        							jsonObj.put(biColumn.getEngName(), 
	        									result.getCell(biColumn.getLabel()).getColumnDisplayString()
	        									);
	        					}
	        				}
	        				jsonRawDataArr.put(jsonObj);
       					}
       				}
	}

	protected void drawZkBiOutput(BiResult p_result) throws JSONException {
	}	
	protected void processAnalysizedData(JSONObject p_data,BiResult p_result) throws JSONException {
		UniLog.log("get Analysised Data: " + p_data);
//		StringTokenizer tok = new StringTokenizer((String) p_data,"\n",true);
//		while(tok.hasMoreElements()) {
//			String ss = tok.nextToken();
//			StringTokenizer tok2 = new StringTokenizer(ss,"\t",true);
//			while(tok2.hasMoreElements()) {
		
//				String ss2 = tok2.nextToken();
//				UniLog.log("Cell : "+ss2);
//			}
//		}
	}
	void setupOutput_pv(BiResult p_result) throws JSONException {
		
//		jsonRowsArr.put(p_result.getCell("mt_tpname").getBiColumn().getEngName());
//		jsonRowsArr.put(p_result.getCell("vd_vname").getBiColumn().getEngName());
//		jsonColsArr.put(p_result.getCell("ind_period").getBiColumn().getEngName());
//		jsonColsArr.put(p_result.getCell("inv_date").getBiColumn().getEngName());
//		aggregates = "{\"Number of Records\":function() { return pivottpl.count()() }}"	;
//		aggregates = "{\"Total Qty\": function() { return pivottpl.sum()([\"Sales Qty\"])}	}";
//		aggregates = "{\"Number of Records\":function() { return pivottpl.count()() }}"	;
       	
		/*
		aggregates = "{"
				+ "\"Number of Records\":function() { return pivottpl.count()() }"	
				+ ",\"Total Qty\": function() { return pivottpl.sum()([\"Sales Qty\"])}	"
				+ "}";
				*/
//       	renderer = "ZKBI";
	}
}