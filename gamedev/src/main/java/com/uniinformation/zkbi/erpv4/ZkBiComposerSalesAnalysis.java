package com.uniinformation.zkbi.erpv4;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Vbox;

import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.CjsUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkReportHelper;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerAnalysisPivotTableJs;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerSalesAnalysis extends ZkBiComposerAnalysisPivotTableJs {
	protected boolean sumTotal = true;
	protected Grid dataGrid = null;
	protected Hlayout chtDiv = null;
	protected Div barChartDiv = null;
    protected Vbox optBox = null;
    protected Vbox chartOptBox = null;
    protected Radiogroup rgChartType= null;
    protected Combobox cbChartAggregate = null; // only apply if both row and col > 1 , in that case only single aggregate is allow, due to limitation of 2D grath
	protected LinkedHashMap<String,AnalysisReport> rptList=null;
	protected JSONObject cjsData = null;
	Div seperator1 = null;
	public void addReportButton(final AnalysisReport rpt,final BiResult p_result) {
			if(rptList == null) {
				rptList = new LinkedHashMap<String,AnalysisReport>();
			}
        	Button b1 = null;
        	b1 = new ZkBiButton(rpt.getRptName());
        	buttonPanel.insertBefore(b1,seperator1);
        	
//        	b1.setParent(buttonPanel);
        	b1.addEventListener("onClick",
       			new EventListener<Event>() {
					@Override
					public void onEvent(Event arg0)
					throws Exception {
						currentRpt = rpt;
						setupOutput(p_result);
						drawZkBiOutput(p_result);
					}
        		}
        	);
        	rptList.put(rpt.getRptName(),rpt);
	}
	@Override
    protected void setupDataAnalysisButton(final BiResult result) {
		super.setupDataAnalysisButton(result);
		chtDiv = new Hlayout();
		chtDiv.setStyle("overflow:auto");
		chtDiv.setParent(displayDiv);

//		barChartDiv = new Div();
//		barChartDiv.setWidth("800px");
//		barChartDiv.setHeight("400px");
//		barChartDiv.setParent(chtDiv);
		
		chtDiv.setVisible(false);
		dataGrid = new Grid();
//		dataGrid.setId("AfsSalesAnalysis_dataGrid");
		dataGrid.setId("Analysis_dataGrid");
		dataGrid.setSclass("zkbi-da");
		//dataGrid.setHflex("1");
		dataGrid.setVflex("1");
		displayDiv.appendChild(dataGrid);
		{
			seperator1 = new Div();
			seperator1.setParent(buttonPanel);
        	optBox = new Vbox();
        	optBox.setWidth("160px");
        	optBox.setParent(buttonPanel);
        	chartOptBox = new Vbox();
        	chartOptBox.setWidth("200");
        	chartOptBox.setParent(buttonPanel);
        	Button b1 = null;
        	b1 = new ZkBiButton("Print Chart");
        	b1.setParent(buttonPanel);
        	b1.addEventListener("onClick",
       			new EventListener() {
					@Override
					public void onEvent(Event arg0)
					throws Exception {
						// TODO Auto-generated method stub
//						ZkUtil.print(chtDiv);
						if(chtDiv.isVisible() && cjsData != null) {
							CjsUtil.createChartImg(
									barChartDiv,
//									CjsUtil.getDemoData2(), 
									cjsData,
									new CjsUtil.CjsCallback() {
										@Override
										public void createChartImgFinish(String p_data) {
											UniLog.log1("interface got data:" + p_data);
											byte[] result = DatatypeConverter.parseBase64Binary(p_data.substring(22));
											ByteArrayInputStream bis = new ByteArrayInputStream(result);
											ZkUtil.printFromStream(bis, "image/png", sessionHelper);
											
									}}); 
						}
					}
        		}
        	);
        	b1 = new ZkBiButton("Export Chart");
        	b1.setParent(buttonPanel);
        	b1.addEventListener("onClick",
       			new EventListener() {
					@Override
					public void onEvent(Event arg0)
					throws Exception {
						// TODO Auto-generated method stub
						if(chtDiv.isVisible() && cjsData != null) {
							CjsUtil.createChartImg(
									barChartDiv,
//									CjsUtil.getDemoData2(), 
									cjsData,
									new CjsUtil.CjsCallback() {
										@Override
										public void createChartImgFinish(String p_data) {
											UniLog.log1("interface got data:" + p_data);
											byte[] result = DatatypeConverter.parseBase64Binary(p_data.substring(22));
											Filedownload.save(result, "image/png", "saveimg"+ ".png");
//											ImageIO.getImageReaders(null);
									}}); 
						}
					}
        		}
        	);
           	b1 = new ZkBiButton("Export to Excel");
        	b1.setParent(buttonPanel);
        	b1.addEventListener("onClick",
       			new EventListener() {
					@Override
					public void onEvent(Event arg0)
					throws Exception {
						InputStream is = sessionHelper.openResourceAsStream("/template/export_template.xlsx");
						ByteArrayOutputStream os = ZkReportHelper.exportGridToExcel(dataGrid, currentRpt.getRowsArr().size(), null,true);
						is.close();
						// TODO Auto-generated method stub
    			    	Filedownload.save(os.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "report.xlsx");
					}
        		}
        	);
		}
	}
	
	@Override
	protected void processAnalysizedData(JSONObject p_data,BiResult p_result) throws JSONException{
		ZkReportHelper rptutil = new ZkReportHelper(p_data);
		rptutil.analysedDataToGrid(dataGrid, sumTotal,"Report Title");
		setupOptionList(p_result,p_data);
	}
	void setupOptionList(final BiResult p_result,final JSONObject p_data) throws JSONException {
		Components.removeAllChildren(optBox);
		Components.removeAllChildren(chtDiv);
		if(currentRpt == null) return;
		currentRpt.getRowsArr();
		optBox.appendChild(new Label("Rows"));
		for(AnalysisReport.RowRec rec : currentRpt.getRowsArr()) {
			Checkbox cb = new Checkbox(rec.getName(p_result));
			cb.setChecked(!rec.isHided());
			cb.setParent(optBox);
			final String key = rec.getKey();
			cb.addEventListener(Events.ON_CHECK,
						new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								// TODO Auto-generated method stub
								Checkbox thisCb = (Checkbox )arg0.getTarget();
								if(currentRpt != null) {
									currentRpt.hideRow(key,!thisCb.isChecked());
									setupOutput(p_result);
									drawZkBiOutput(p_result);
								}
							}
				
						}
					);
		}
		optBox.appendChild(new Label("Columns"));
		for(AnalysisReport.ColRec rec : currentRpt.getColsArr()) {
			Checkbox cb = new Checkbox(rec.getName(p_result));
			cb.setChecked(!rec.isHided());
			cb.setParent(optBox);
			final String key = rec.getKey();
			cb.addEventListener(Events.ON_CHECK,
						new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								// TODO Auto-generated method stub
								Checkbox thisCb = (Checkbox )arg0.getTarget();
								if(currentRpt != null) {
									currentRpt.hideCol(key,!thisCb.isChecked());
									setupOutput(p_result);
									drawZkBiOutput(p_result);
								}
							}
				
						}
					);
		}
		optBox.appendChild(new Label("Values"));
		for(AnalysisReport.AggregateRec rec : currentRpt.getAggsArr()) {
			Checkbox cb = new Checkbox(rec.getName(p_result));
			cb.setChecked(!rec.isHided());
			cb.setParent(optBox);
			final String key = rec.getKey();
			cb.addEventListener(Events.ON_CHECK,
						new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								// TODO Auto-generated method stub
								Checkbox thisCb = (Checkbox )arg0.getTarget();
								if(currentRpt != null) {
									currentRpt.hideAggregate(key,!thisCb.isChecked());
									setupOutput(p_result);
									drawZkBiOutput(p_result);
								}
							}
				
						}
					);
		}
		Components.removeAllChildren(chartOptBox);
		cbChartAggregate = null;
		rgChartType = null;
		
		rgChartType = new Radiogroup();
		rgChartType.appendItem("None", null);
		Radio rb;
		rb = new Radio("Bar"); rb.setValue(CjsUtil.TYPE_BARCHART); rgChartType.appendChild(rb);
		rb = new Radio("Line"); rb.setValue(CjsUtil.TYPE_LINECHART); rgChartType.appendChild(rb);
		rb = new Radio("Pie"); rb.setValue(CjsUtil.TYPE_PIECHART); rgChartType.appendChild(rb);
		rgChartType.setSelectedIndex(0);
		rgChartType.setParent(chartOptBox);
		rgChartType.addEventListener(Events.ON_CHECK,
						new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								drawOneChart(p_data);
//								if(rgChartType.getSelectedIndex() <= 0) {
//									UniLog.log("Chart Option Changed");
//									chtDiv.setVisible(false);
//								} else {
//									chtDiv.setVisible(true);
////									CjsUtil.createChart(barChartDiv,CjsUtil.getDemoData1("bar"));
//									drawOneChart(p_data);
//								}
							}
						}
					);

		if(p_data.getJSONArray("Rows").length() > 1
		    &&  p_data.getJSONArray("Columns").length() > 1) {
			
		cbChartAggregate = new Combobox();
		for(int i=0;i<p_data.getJSONArray("Aggregates").length();i++) {
			Comboitem ci = new Comboitem();
			ci.setLabel(p_data.getJSONArray("Aggregates").getJSONObject(i).getString("name"));
			ci.setValue(p_data.getJSONArray("Aggregates").getJSONObject(i).getString("key"));
			cbChartAggregate.appendChild(ci);
		}
		cbChartAggregate.setSelectedIndex(0);
		cbChartAggregate.addEventListener(Events.ON_CHANGE,
						new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								drawOneChart(p_data);
//								if(cbChartAggregate.getSelectedIndex() <= 0) {
//									chtDiv.setVisible(false);
//								} else {
//									chtDiv.setVisible(true);
//									drawOneChart(p_data);
//								}
							}
						}
					);
		cbChartAggregate.setParent(chartOptBox);
		}

	}
	
//								if(cbChartAggregate.getSelectedIndex() <= 0) {
//									chtDiv.setVisible(false);
//								} else {
//									chtDiv.setVisible(true);
//									int aggOrder[] = new int[1];
//									aggOrder[0] = cbChartAggregate.getSelectedIndex();
//									drawOneChart(p_data,(Integer) rgChartType.getSelectedItem().getValue(),null,null,aggOrder) ;
//								}
//	void drawOneChart(JSONObject p_data,int p_chartType,int p_rowOrder[],int p_colOrder[],int p_aggOrder[]) throws JSONException {
//		CjsUtil cjs = new CjsUtil(p_data);
//		JSONObject cjsData = 
//				cjs.analysedDataToCjsData(p_chartType,p_rowOrder,p_colOrder,p_aggOrder);
//		Components.removeAllChildren(chtDiv);
//		barChartDiv = new Div();
//		barChartDiv.setWidth("800px");
//		barChartDiv.setHeight("400px");
//		barChartDiv.setParent(chtDiv);
//		CjsUtil.createChart(barChartDiv,cjsData);
//	}
	
	protected void drawOneChart(JSONObject p_data) throws JSONException {
		int aggOrder[];
		
		JSONArray jsonAggregatesArr = p_data.getJSONArray("Aggregates");
		JSONArray jsonRowsArr = p_data.getJSONArray("RowHeaders");
		JSONArray jsonColsArr = p_data.getJSONArray("ColHeaders");
		
		if(jsonAggregatesArr.length() <= 0) {
			rgChartType.setSelectedIndex(0);
		}
		if(jsonRowsArr.length() <= 0 && jsonColsArr.length() <=0 ) {
			rgChartType.setSelectedIndex(0);
		}
		
		if(rgChartType.getSelectedIndex() <= 0) {
			UniLog.log("Chart Option Changed");
			chtDiv.setVisible(false);
			return;
		} else {
			chtDiv.setVisible(true);
//									CjsUtil.createChart(barChartDiv,CjsUtil.getDemoData1("bar"));
		}
		if(cbChartAggregate == null) {
			aggOrder = new int[p_data.getJSONArray("Aggregates").length()];
			for(int i=0;i<aggOrder.length;i++) aggOrder[i] = i;
		} else{
			aggOrder = new int[1];
			aggOrder[0] = cbChartAggregate.getSelectedIndex();
		}
		CjsUtil cjs = new CjsUtil(p_data);
		cjsData = 
				cjs.analysedDataToCjsData((Integer) rgChartType.getSelectedItem().getValue(),getReportTitle(),null,null,aggOrder);
		Components.removeAllChildren(chtDiv);
		barChartDiv = new Div();
		int nCols = cjsData.getJSONObject("data").getJSONArray("labels").length();
		if(isMobile()) {
			//barChartDiv.setWidth("100%");
			barChartDiv.setWidth(sessionHelper.getScreenWidth() - 30 + "px");  //andrew190918: dirty way to fix chart width
//			barChartDiv.setWidth("1800px");
			barChartDiv.setHeight("400px");
		} else {
//			barChartDiv.setWidth("1800px");
			int px = nCols * 32;
			if( px < 800) px = 800;
			if( px > 4096) px = 4096;
			barChartDiv.setWidth(""+px+"px");
			barChartDiv.setHeight("400px");
		}
		/*
		Div sv = new Div();
		sv.setWidth("2000px");
		sv.setHeight("100px");
		sv.setParent(chtDiv);
		*/
		/*
		Div sv = new Div();
		sv.setWidth("2000px");
		sv.setStyle("overflow:auto");
		sv.setParent(chtDiv);
		barChartDiv.setParent(sv);
		*/
		barChartDiv.setParent(chtDiv);
		chtDiv.invalidate();
		CjsUtil.createChart(barChartDiv,cjsData);
	}
	
	String getReportTitle() throws JSONException{
		String s = getCurrentPresetName();
		if(s.startsWith("Public")) {
			s = StringUtil.strpart(s, 6, -1);
		}
		if(s.startsWith("Custom")) {
			s = StringUtil.strpart(s, 6, -1);
		}
		return(s);
	}
}
