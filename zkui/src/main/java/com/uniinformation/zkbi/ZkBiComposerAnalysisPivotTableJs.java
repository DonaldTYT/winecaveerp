package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Vbox;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerAnalysis.AnalysisReport.AggregateRec;
import com.uniinformation.zkbi.ZkBiComposerAnalysis.AnalysisReport.ColRec;
import com.uniinformation.zkbi.ZkBiComposerAnalysis.AnalysisReport.RowRec;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerAnalysisPivotTableJs extends ZkBiComposerAnalysis {
	class PivotTableJsAggregateRec {
		String pivotJsName;
		Double multiplier;
		PivotTableJsAggregateRec(String p_pivotJsName, Double p_multiplier) {
			pivotJsName = p_pivotJsName;
			multiplier = p_multiplier;
		}
	}
	
    JSONArray jsonColsArr = null;
    JSONArray jsonRowsArr = null;
    JSONArray jsonAggsArr = null;
	
    Div pvDiv=null;
    int drawAggIdx=0;
    HashMap<String,PivotTableJsAggregateRec> aggregateMap = null;
	PivotTableJsAggregateRec currentAgRec = null;
	@Override
    protected void setupDataAnalysisButton(final BiResult result) {
		super.setupDataAnalysisButton(result);
		aggregateMap = new HashMap();
		aggregateMap.put(AGGREGATES.COUNT.name(), new PivotTableJsAggregateRec("Count",null));
		aggregateMap.put(AGGREGATES.COUNT_UNIQUE.name(), new PivotTableJsAggregateRec("Count Unique Values",null));
		aggregateMap.put(AGGREGATES.SUM.name(), new PivotTableJsAggregateRec("Sum",null));
		aggregateMap.put(AGGREGATES.AVERAGE.name(), new PivotTableJsAggregateRec("Average",null));
		aggregateMap.put(AGGREGATES.MEDIAN.name(), new PivotTableJsAggregateRec("Median",null));
		aggregateMap.put(AGGREGATES.STANDARD_DEVIATION.name(), new PivotTableJsAggregateRec("Sample Standard Deviation",null));
		aggregateMap.put(AGGREGATES.MAX.name(), new PivotTableJsAggregateRec("Max",null));
		aggregateMap.put(AGGREGATES.MIN.name(), new PivotTableJsAggregateRec("Min",null));
		aggregateMap.put(AGGREGATES.PERCENT_TOTAL.name(), new PivotTableJsAggregateRec("Sum as Fraction of Total",new Double(100.0)));
		aggregateMap.put(AGGREGATES.PERCENT_ROW.name(), new PivotTableJsAggregateRec("Sum as Fraction of Rows",new Double(100.0)));
		aggregateMap.put(AGGREGATES.PERCENT_COL.name(), new PivotTableJsAggregateRec("Sum as Fraction of Columns",new Double(100.0)));
        			pvDiv = new Div();
        			pvDiv.setId("data-analysis-div");
        			pvDiv.setParent(daDiv);
        			
					pvDiv.addEventListener("onPivotCallback", new EventListener(){ 
						public void onEvent(Event event) throws Exception {
							analysedDataToJson((String) event.getData(),jsonAnalysedData) ;
							drawAggIdx++;
							if(drawAggIdx < jsonAggsArr.length()) {
								drawZkBiOutput_One(result);
							} else {
								processAnalysizedData(jsonAnalysedData,result);
							}
						}
						
					});
					pvDiv.addEventListener("onDataAnalysis", new EventListener(){ 
						public void onEvent(Event event) throws Exception {
							UniLog.log("onDataAnalysis Called ["+event.getData()+"]");
							if(event.getData() != null) {
								JSONObject jo = new JSONObject(event.getData().toString());
								JSONObject jf = jo.getJSONObject("config");
								JSONArray jr = jf.getJSONArray("rows");
								JSONArray jc = jf.getJSONArray("cols");
								String agName = jf.getString("aggregatorName");
								if(jo.getString("cmd").equals("close")) {
									UniLog.log("command close");
								}
								if(jo.getString("cmd").equals("save")) {
									UniLog.log("command save");
								}
								if(jo.getString("cmd").equals("append")) {
									UniLog.log("command append");
								}
								setupOutput(result);
								drawZkBiOutput(result);
							}
						}
						
					});
        if(result.getSessionHelper().isAdminUser()){
        	Button b0 = new ZkBiButton("Pivot Table Edit");
        	b0.setParent(buttonPanel);
        	b0.addEventListener("onClick",
       			new EventListener() {
					@Override
					public void onEvent(Event arg0)
						throws Exception {
						// TODO Auto-generated method stub
						setupOutput_pv(result);
						drawPivotOutput(result);
					}
        		}
        	);
        }
	}
	void drawPivotOutput(BiResult p_result) {
		daArea.setVisible(false);
		String renderer = "Table";
        			Clients.evalJavaScript(String.format("startDataAnalysisUI('$data-analysis-div', '%s', '%s', %s, %s, %s);", 
        					renderer,"",jsonRawDataArr.toString(), jsonColsArr.toString(), jsonRowsArr.toString()
        					));
			
	}	
	void drawZkBiOutput_One(BiResult p_result) {
		daArea.setVisible(true);
		JSONObject jo = jsonAggsArr.optJSONObject(drawAggIdx);
		currentAgRec = aggregateMap.get(jo.optString("aggregate"));
		JSONArray ja = jo.optJSONArray("vals");
		JSONArray jas = new JSONArray();
		for(int i = 0;i<ja.length();i++) {
			jas.put(p_result.getColumnByLabel(ja.optString(i)).getEngName());
		}
		String renderer = "ZKBI";
        Clients.evalJavaScript(String.format("startDataAnalysis('$data-analysis-div', '%s', '%s', %s, %s, %s, %s);", 
        		renderer,currentAgRec.pivotJsName,jas,jsonRawDataArr.toString(), jsonColsArr.toString(), jsonRowsArr.toString()
        ));
	}	

	@Override
	protected void drawZkBiOutput(BiResult p_result) throws JSONException{
		drawAggIdx = 0;
		jsonAnalysedData = new JSONObject();
		jsonAnalysedData.put("Aggregates", jsonAggsArr);
		jsonAnalysedData.put("RowHeaders", jsonRowsArr);
		jsonAnalysedData.put("ColHeaders", jsonColsArr);
		drawZkBiOutput_One(p_result);
	}	
	void analysedDataToJson(String p_data,JSONObject p_jsondata) throws JSONException{
		JSONArray jsonColValsArr = p_jsondata.optJSONArray("Columns");
		JSONArray jsonRowValsArr = p_jsondata.optJSONArray("Rows");
		if(jsonColValsArr == null) {jsonColValsArr = new JSONArray(); p_jsondata.put("Columns", jsonColValsArr);};
		if(jsonRowValsArr == null) {jsonRowValsArr = new JSONArray(); p_jsondata.put("Rows", jsonRowValsArr);};
		boolean isHeader = true;
		
		int nr = jsonRowsArr.length();
		String lines[] = ((String) p_data).split("\n");
		int lineN = 0;
//		double multipler = jsonAggsArr.getJSONObject(drawAggIdx).optDouble("multiplier");
		for(String str : lines) {
			String cells[] = str.split("\t",-1);
			if(isHeader) {
				for(int i=nr;i<cells.length;i++) {
					JSONObject jo = new JSONObject();
					String subCols[] = cells[i].split(",",-1);
					for(int j = 0; j < jsonColsArr.length();j++) {
						jo.put(jsonColsArr.getString(j), subCols[j]);
					}
					jsonColValsArr.put(i-nr,jo);
				}
				isHeader = false;
			} else {
				JSONObject jo = jsonRowValsArr.optJSONObject(lineN);
				if(jo == null) jo = new JSONObject();
				for(int i=0;i<nr;i++) {
					jo.put(jsonRowsArr.getString(i), cells[i]);
				}
				JSONArray ja = jo.optJSONArray("datas");
				if(ja == null) ja = new JSONArray();
				for(int i=nr;i<cells.length;i++) {
//					UniLog.log("Cell:"+cell);
					JSONArray cellArr = ja.optJSONArray(i-nr);
					if(cellArr == null) cellArr = new JSONArray();
					double value = 0;
					try {
						if(!cells[i].trim().equals("")) {
							value = Double.parseDouble(cells[i].trim());
//							if(!Double.isNaN(multipler)) value *= multipler;
							if(currentAgRec.multiplier != null) value *= currentAgRec.multiplier;
						}
					} catch (NumberFormatException nex) {
						UniLog.log(nex);
					}
//					ja.put(cells[i].trim());
					cellArr.put(value);
					ja.put(i-nr,cellArr);
				}
				jo.put("datas", ja);
				jsonRowValsArr.put(lineN,jo);
				lineN++;
			}
		}
	//	JSONObject jo = new JSONObject();
//		if(jsonColValsArr != null) {
//			jo.put("Columns", jsonColValsArr);
//		}
//		if(jsonRowValsArr != null) {
//			jo.put("Rows", jsonRowValsArr);
//		}
	}
	@Override
	protected void  setupOutput(BiResult result) throws JSONException {
//       				renderer = "ZKBI";
//       	sumTotal = true;
//       	
//       	
    	jsonColsArr = new JSONArray();
    	jsonRowsArr = new JSONArray();
    	jsonAggsArr = new JSONArray();
    	
		if(currentRpt == null) return;
		ArrayList<RowRec> rowsRec = currentRpt.getRowsArr();
		for(RowRec rec:rowsRec) {
			if(!rec.hided) jsonRowsArr.put(result.getCell(rec.rowName).getBiColumn().getEngName());
		}
		ArrayList<ColRec> colsRec = currentRpt.getColsArr();
		for(ColRec rec:colsRec) {
			if(!rec.hided) jsonColsArr.put(result.getCell(rec.colName).getBiColumn().getEngName());
		}
		ArrayList<AggregateRec> aggsRec = currentRpt.getAggsArr();
		for(AggregateRec rec:aggsRec) {
			if(rec.hided) continue;
			JSONObject jo;
			JSONArray ja;
			jo = new JSONObject();
			jo.put("aggregate", rec.aggregate.name());// remember : use .name() instead of using the emum value for easier debug
			jo.put("name", rec.getName(result));
			jo.put("key", rec.getKey());
			if(rec.getFormat(result) != null) jo.put("format", rec.getFormat(result));
			if(rec.getWidth(result) != null) jo.put("width", rec.getWidth(result));
			ja = new JSONArray();
			for(String val : rec.varName) {
				ja.put(val);
			}
			jo.put("vals", ja);
			jsonAggsArr.put(jo);
		}
		if(jsonAggsArr.length() <= 0) {
			JSONObject jo;
			JSONArray ja;
			jo = new JSONObject();
			ja = new JSONArray();
			jo.put("aggregate", AGGREGATES.COUNT.name());
			jo.put("name", "Count");
			jo.put("key", "COUNT(*)");
			jo.put("format", "###,###,##0");
			jo.put("width", "100px");
			jo.put("vals", ja);
			jsonAggsArr.put(jo);
			
		}
		/*
		aggregates = "{"
				+ "\"Number of Records\":function() { return pivottpl.count()() }"	
				+ ",\"Total Qty\": function() { return pivottpl.sum()([\"Sales Qty\"])}	"
				+ "}";
				*/
	}
}
