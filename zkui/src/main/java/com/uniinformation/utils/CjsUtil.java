package com.uniinformation.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.HtmlNativeComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;

public class CjsUtil {
	static final public int TYPE_BARCHART = 1;
	static final public int TYPE_LINECHART = 2;
	static final public int TYPE_PIECHART = 3;
	static String defaultBackgroundColor[] = {
		"rgba(255, 99, 132, 0.5)",
//		"rgba(54, 162, 235, 0.5)",
		"rgba(173, 255, 47, 0.5)",
		"rgba(255, 206, 86, 0.5)",
		"rgba(75, 192, 192, 0.5)",
		"rgba(153, 102, 255, 0.5)",
//		"rgba(255, 159, 64, 0.5)",
	};
	static String defaultBorderColor[] = {
		"rgba(255,99,132,0.7)",
//		"rgba(54, 162, 235, 0.7)",
		"rgba(173, 255, 47, 0.5)",
		"rgba(255, 206, 86, 0.7)",
		"rgba(75, 192, 192, 0.7)",
		"rgba(153, 102, 255, 0.7)",
//		"rgba(255, 159, 64, 0.7)",
	};
	static final String defaultOption = "{ responsive: true, maintainAspectRatio: false, animation: false }";
	public static HtmlNativeComponent createChart(Div p_div, JSONObject p_data){
		return(createChart(p_div, p_data, false));
	}
	/***
	 * create a chartjs and place it inside a div
	 * @param p_div - chart container
	 * @param p_data - json chart data
	 * @param p_toServerFlag - send result event back to server
	 * @return
	 */
	public static HtmlNativeComponent createChart(Div p_div, JSONObject p_data, boolean p_toServerFlag){
		if (p_div == null){
			UniLog.log1("comp is null");
			return null;
		}
		HtmlNativeComponent canvas = new HtmlNativeComponent("canvas");
//		String canvasId = UUID.randomUUID().toString();
//		canvas.setId(canvasId);
    	canvas.setParent(p_div);
		try{
		ZkUtil.js("cjsCreateChart('%s',%s,%s)",/* canvasId */ canvas.getUuid(), p_data, p_toServerFlag);
//			UniLog.log("canvas uuid = " + canvas.getUuid());
//			ZkUtil.js("cjsCreateChart('%s',%s,%s)",canvasId, p_data, p_toServerFlag);
			return canvas;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
		
	}
	/***
	 * CjsUtil Callback interface
	 */
	public interface CjsCallback{
		public void createChartImgFinish(String p_data);
	}
	/***
	 * create base64 png chart image
	 * @param p_div - chart container
	 * @param p_data - chartjs json data
	 * @param p_cb - callback for return image
	 * @return
	 */
	public static HtmlNativeComponent createChartImg(Div p_div, JSONObject p_data, final CjsCallback p_cb){
		HtmlNativeComponent canvas = createChart(p_div, p_data, true);
		canvas.addEventListener("onCreateChartImgFinish", new EventListener(){
			public void onEvent(Event p_event) throws Exception {
				UniLog.log1("got event:" + p_event.getName() + ":" + p_event.getData());
				p_cb.createChartImgFinish((String)p_event.getData());
			}});
		return(canvas);
	}
	
	/***
	 * demo data. with 1 set of data.
	 * @param p_type
	 * @return
	 */
	public static JSONObject getDemoData1(String p_type){
		try{
			JSONObject jo = new JSONObject("{ type: 'bar', data: { labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'], datasets: [{ label: '# of Votes', data: [12, 19, 3, 5, 2, 3], backgroundColor: [ 'rgba(255, 99, 132, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(255, 206, 86, 0.2)', 'rgba(75, 192, 192, 0.2)', 'rgba(153, 102, 255, 0.2)', 'rgba(255, 159, 64, 0.2)' ], borderColor: [ 'rgba(255,99,132,1)', 'rgba(54, 162, 235, 1)', 'rgba(255, 206, 86, 1)', 'rgba(75, 192, 192, 1)', 'rgba(153, 102, 255, 1)', 'rgba(255, 159, 64, 1)' ], borderWidth: 1 }] }, options: { responsive: true, maintainAspectRatio: false, animation: false, scales: { yAxes: [{ ticks: { beginAtZero:true } }] } } }");
			jo.put("type", p_type);
			return(jo);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return(null);
	}
	
	/***
	 * demo data. with 2 set of data.
	 * @return
	 */
	public static JSONObject getDemoData2(){
		try{
			JSONObject jo = new JSONObject ("{ type: 'bar', data: { labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'], datasets: [ { label: '# of Votes', type: 'bar', data: [12, 19, 3, 5, 2, 3], backgroundColor: [ 'rgba(255, 99, 132, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(255, 206, 86, 0.2)', 'rgba(75, 192, 192, 0.2)', 'rgba(153, 102, 255, 0.2)', 'rgba(255, 159, 64, 0.2)' ], borderColor: [ 'rgba(255,99,132,1)', 'rgba(54, 162, 235, 1)', 'rgba(255, 206, 86, 1)', 'rgba(75, 192, 192, 1)', 'rgba(153, 102, 255, 1)', 'rgba(255, 159, 64, 1)' ], borderWidth: 1 }, { label: '# of Votes', type: 'line', data: [2, 3, 4, 5, 2, 1] }, ] }, options: { responsive: true, maintainAspectRatio: false, animation: false, scales: { yAxes: [{ ticks: { beginAtZero:true } }] } }, }");
			return(jo);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return(null);
	}
	
	JSONObject data;
	
	public CjsUtil(JSONObject p_reportData) {
		data = p_reportData;
	}
	public String getBackgroundColor(int p_idx) {
		return(defaultBackgroundColor[ p_idx % (defaultBackgroundColor.length)]);
	}
	public String getBorderColor(int p_idx) {
		return(defaultBorderColor[ (p_idx + p_idx/defaultBackgroundColor.length)% (defaultBorderColor.length)]);
	}
	public int getBorderWidth(int p_idx) {
		if(p_idx < defaultBackgroundColor.length) return(0); else return(2);
	}
	public JSONObject analysedDataToCjsData(int p_chartType,String p_title,int p_rowOrder[],int p_colOrder[],int p_aggOrder[]) throws JSONException{
		JSONArray jsonAggregatesArr = data.getJSONArray("Aggregates");
		JSONArray jsonRowsArr = data.getJSONArray("RowHeaders");
		JSONArray jsonColsArr = data.getJSONArray("ColHeaders");
		JSONArray jrows = data.getJSONArray("Rows");
		JSONArray jcols = data.getJSONArray("Columns");
		String title = p_title;
		boolean aggNameInTitle = false;
		int rowOrder[] = p_rowOrder;
		if(rowOrder == null) {
			rowOrder = new int[jrows.length()];
			for(int i=0;i<rowOrder.length;i++) rowOrder[i] = i;
		}
		int colOrder[] = p_rowOrder;
		if(colOrder == null) {
			colOrder = new int[jcols.length()];
			for(int i=0;i<colOrder.length;i++) colOrder[i] = i;
		}

		int aggOrder[] = p_aggOrder;
		if(aggOrder == null) {
			aggOrder = new int[jsonAggregatesArr.length()];
			for(int i=0;i<aggOrder.length;i++) aggOrder[i] = i;
		}
		
//		int aggIdx=-1;
//		for(int i=0;i<jsonAggregatesArr.length();i++) {
//			if(jsonAggregatesArr.getJSONObject(i).getString("key").equals(p_aggregate)) {
//				aggIdx = i;
//				break;
//			}
//		}
//		if(aggIdx < 0) return(null);
		if(colOrder.length < 1 || rowOrder.length < 1 || aggOrder.length < 1) return(null);

		JSONObject cjs = new JSONObject();
		switch(p_chartType) {
		case TYPE_BARCHART:
				cjs.put("type", "bar");
				break;
		case TYPE_LINECHART:
				cjs.put("type", "line");
				break;
		case TYPE_PIECHART:
				cjs.put("type", "pie");
//				cjs.put("type", "doughnut");
				break;
		}
		JSONObject cjs_data =  new JSONObject();
		JSONArray cjs_data_labels = new JSONArray();  //Label Array
		JSONArray cjs_data_datasets = new JSONArray();
		if(rowOrder.length <= 1 /* or preferred column as label */) {
			if(colOrder.length > 0) {
				//	Row Size <=1 and Column Size > 1  , use Columns as label			
				for(int i=0;i<colOrder.length;i++) {
					JSONObject jc = jcols.getJSONObject(colOrder[i]);
					String lb = "";
					for(int k=0;k<jsonColsArr.length();k++) {
						if(lb.length() != 0) lb+=",";
						lb += jc.get(jsonColsArr.getString(k));
					}
					cjs_data_labels.put(lb);
				}
				cjs_data.put("labels", cjs_data_labels);
				for(int i=0;i<rowOrder.length;i++) {
					JSONObject jr = jrows.getJSONObject(rowOrder[i]);
					for(int p=0;p < aggOrder.length;p++) {
						JSONObject cjs_data_datasets_object = new JSONObject();
						if(jsonRowsArr.length() > 0) {
							String lb = "";
							for(int k=0;k<jsonRowsArr.length();k++) {
								if(lb.length() != 0) lb+=",";
								lb += jr.get(jsonRowsArr.getString(k));
							}
							if(aggOrder.length > 1) {
								lb += "-" + jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name");
							} else {
								if(!aggNameInTitle) {
									title += "-" + jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name");
									aggNameInTitle = true;
								}
							}
							cjs_data_datasets_object.put("label", lb);
						} else {
							cjs_data_datasets_object.put("label", jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name"));
						}
						if (p_chartType	 == TYPE_LINECHART){
							cjs_data_datasets_object.put("lineTension", 0);
							cjs_data_datasets_object.put("fill", false);
						}
//						if (p_chartType	 == TYPE_BARCHART){
//							cjs_data_datasets_object.put("borderWidth", 1);
//						}
						if(/* rowOrder.length * aggOrder.length > 1 && */ p_chartType != TYPE_PIECHART) {
							cjs_data_datasets_object.put("backgroundColor", getBackgroundColor(i * aggOrder.length + p) );
							cjs_data_datasets_object.put("borderColor", getBorderColor(i * aggOrder.length + p) );
							cjs_data_datasets_object.put("borderWidth", getBorderWidth(i * aggOrder.length + p) );
						} else {
							JSONArray bgcArr = new JSONArray();
							JSONArray boArr = new JSONArray();
							JSONArray bwArr = new JSONArray();
							for(int j=0;j<colOrder.length;j++) {
								bgcArr.put(j, getBackgroundColor(j) );
								boArr.put(j, getBorderColor(j) );
								bwArr.put(j, getBorderWidth(j) );
							}
							cjs_data_datasets_object.put("backgroundColor", bgcArr);
							cjs_data_datasets_object.put("borderColor", boArr);
							cjs_data_datasets_object.put("borderWidth", boArr);
						}
						JSONArray cjs_data_datasets_object_data = new JSONArray();
						for(int j=0;j<colOrder.length;j++) {
							JSONArray da = jr.getJSONArray("datas");
							JSONArray cellArr = da.getJSONArray(colOrder[j]);
							double d = cellArr.getDouble(aggOrder[p]);
							cjs_data_datasets_object_data.put(d);
						}
						cjs_data_datasets_object.put("data", cjs_data_datasets_object_data);
						cjs_data_datasets.put(cjs_data_datasets_object);
					}
				}
			} else {
				//	Row Size <= 1 and Column Size <= 1  , use Aggregates as label			
			}
		} else {
		  if(false /* colOrder.length > rowOrder.length */) {
			for(int i=0;i<colOrder.length;i++) {
				JSONObject jc = jcols.getJSONObject(colOrder[i]);
				String lb = "";
				for(int k=0;k<jsonColsArr.length();k++) {
					if(lb.length() != 0) lb+=",";
					lb += jc.get(jsonColsArr.getString(k));
				}
				cjs_data_labels.put(lb);
			}
			cjs_data.put("labels", cjs_data_labels);
			for(int i=0;i<rowOrder.length;i++) {
				JSONObject jr = jrows.getJSONObject(rowOrder[i]);
				for(int p=0;p < aggOrder.length;p++) {

				JSONObject cjs_data_datasets_object = new JSONObject();
				if(jsonRowsArr.length() > 0) {
					String lb = "";
					for(int k=0;k<jsonRowsArr.length();k++) {
						if(lb.length() != 0) lb+=",";
						lb += jr.get(jsonRowsArr.getString(k));
					}
					if(aggOrder.length > 1) {
						lb += "-" + jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name");
					} else {
								if(!aggNameInTitle) {
									title += "-" + jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name");
									aggNameInTitle = true;
								}
					}
					cjs_data_datasets_object.put("label", lb);
				} else {
					cjs_data_datasets_object.put("label", jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name"));
				}
				if (p_chartType	 == TYPE_LINECHART){
					cjs_data_datasets_object.put("lineTension", 0);
					cjs_data_datasets_object.put("fill", false);
				}
//				if (p_chartType	 == TYPE_BARCHART){
//					cjs_data_datasets_object.put("borderWidth", 1);
//				}

				if(p_chartType != TYPE_PIECHART) {
					cjs_data_datasets_object.put("backgroundColor", getBackgroundColor(i * aggOrder.length + p) );
					cjs_data_datasets_object.put("borderColor", getBorderColor(i * aggOrder.length + p) );
					cjs_data_datasets_object.put("borderWidth", getBorderWidth(i * aggOrder.length + p) );
				} else {
					JSONArray bgcArr = new JSONArray();
					JSONArray boArr = new JSONArray();
					JSONArray bwArr = new JSONArray();
					for(int j=0;j<colOrder.length;j++) {
						bgcArr.put(j, getBackgroundColor(j) );
						boArr.put(j, getBorderColor(j) );
						bwArr.put(j, getBorderWidth(j) );
					}
					cjs_data_datasets_object.put("backgroundColor", bgcArr);
					cjs_data_datasets_object.put("borderColor", boArr);
					cjs_data_datasets_object.put("borderWidth", boArr);
				}							
				
				JSONArray cjs_data_datasets_object_data = new JSONArray();

				for(int j=0;j<colOrder.length;j++) {
					JSONObject cr = jrows.getJSONObject(rowOrder[i]);
					JSONArray da = cr.getJSONArray("datas");
					JSONArray cellArr = da.getJSONArray(colOrder[j]);
					double d = cellArr.getDouble(aggOrder[p]);
					cjs_data_datasets_object_data.put(d);
				}
				cjs_data_datasets_object.put("data", cjs_data_datasets_object_data);
				cjs_data_datasets.put(cjs_data_datasets_object);
				
				}
			}
		  } else {
			  
			
			//	Row Size > 1 , use Row as Label
			for(int i=0;i<rowOrder.length;i++) {
				JSONObject jr = jrows.getJSONObject(rowOrder[i]);
				String lb = "";
				for(int k=0;k<jsonRowsArr.length();k++) {
					if(lb.length() != 0) lb+=",";
					lb += jr.getString(jsonRowsArr.getString(k));
				}
				cjs_data_labels.put(lb);
			}
			cjs_data.put("labels", cjs_data_labels);
			for(int i=0;i<colOrder.length;i++) {
				JSONObject jc = jcols.getJSONObject(colOrder[i]);
				for(int p=0;p < aggOrder.length;p++) {
				JSONObject cjs_data_datasets_object = new JSONObject();
				if(jsonColsArr.length() > 0) {
					String lb = "";
					for(int k=0;k<jsonColsArr.length();k++) {
						if(lb.length() != 0) lb+=",";
						lb += jc.get(jsonColsArr.getString(k));
					}
					if(aggOrder.length > 1) {
//						lb += "-" + jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name");
					} else {
								if(!aggNameInTitle) {
									title += "-" + jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name");
									aggNameInTitle = true;
								}
					}
					if(p == 0) {
						cjs_data_datasets_object.put("label", lb);
					} else {
						cjs_data_datasets_object.put("label", "");
					}
				} else {
					cjs_data_datasets_object.put("label", jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name"));
				}
				if (p_chartType	 == TYPE_LINECHART){
					cjs_data_datasets_object.put("lineTension", 0);
					cjs_data_datasets_object.put("fill", false);
				}
//				if (p_chartType	 == TYPE_BARCHART){
//					cjs_data_datasets_object.put("borderWidth", 1);
//				}
//				if(colOrder.length * aggOrder.length > 1 && p_chartType != TYPE_PIECHART) {
//					cjs_data_datasets_object.put("backgroundColor", getBackgroundColor(i * aggOrder.length + p) );
//					cjs_data_datasets_object.put("borderColor", getBorderColor(i * aggOrder.length + p) );
//				} else {
//					JSONArray bgcArr = new JSONArray();
//					JSONArray boArr = new JSONArray();
//					for(int j=0;j<rowOrder.length;j++) {
//						bgcArr.put(j, getBackgroundColor(j) );
//						boArr.put(j, getBorderColor(j) );
//					}
//					cjs_data_datasets_object.put("backgroundColor", bgcArr);
//					cjs_data_datasets_object.put("borderColor", boArr);
//				}
				if(p_chartType != TYPE_PIECHART) {
					/*
					cjs_data_datasets_object.put("backgroundColor", getBackgroundColor(i * aggOrder.length + p) );
					cjs_data_datasets_object.put("borderColor", getBorderColor(i * aggOrder.length + p) );
					cjs_data_datasets_object.put("borderWidth", getBorderWidth(i * aggOrder.length + p) );
					*/
					if(colOrder.length <= 1) {
						cjs_data_datasets_object.put("backgroundColor", getBackgroundColor(p) );
						cjs_data_datasets_object.put("borderColor", getBorderColor(p) );
						cjs_data_datasets_object.put("borderWidth", getBorderWidth(p) );
					} else {
						cjs_data_datasets_object.put("backgroundColor", getBackgroundColor(i) );
						cjs_data_datasets_object.put("borderColor", getBorderColor(i) );
						cjs_data_datasets_object.put("borderWidth", getBorderWidth(i) );

						if(p > 0) {
							cjs_data_datasets_object.put("type", "line");
							cjs_data_datasets_object.put("lineTension", 0);
							cjs_data_datasets_object.put("fill", false);
						}
					}
				} else {
					JSONArray bgcArr = new JSONArray();
					JSONArray boArr = new JSONArray();
					JSONArray bwArr = new JSONArray();
					for(int j=0;j<rowOrder.length;j++) {
						bgcArr.put(j, getBackgroundColor(j) );
						boArr.put(j, getBorderColor(j) );
						bwArr.put(j, getBorderWidth(j) );
					}
					cjs_data_datasets_object.put("backgroundColor", bgcArr);
					cjs_data_datasets_object.put("borderColor", boArr);
					cjs_data_datasets_object.put("borderWidth", boArr);
				}
				
				
				JSONArray cjs_data_datasets_object_data = new JSONArray();
				
				for(int j=0;j<rowOrder.length;j++) {
					JSONObject jr = jrows.getJSONObject(rowOrder[j]);
					JSONArray da = jr.getJSONArray("datas");
					JSONArray cellArr = da.getJSONArray(colOrder[i]);
					double d = cellArr.getDouble(aggOrder[p]);
					cjs_data_datasets_object_data.put(d);
				}
				cjs_data_datasets_object.put("data", cjs_data_datasets_object_data);
				cjs_data_datasets.put(cjs_data_datasets_object);
				}
			}
			
		  }
			
			
//			//	Row Size > 1 , use Row as Label
//			for(int i=0;i<rowOrder.length;i++) {
//				JSONObject jr = jrows.getJSONObject(rowOrder[i]);
//				String lb = "";
//				for(int k=0;k<jsonRowsArr.length();k++) {
//					lb += jr.getString(jsonRowsArr.getString(k));
//				}
//				cjs_data_labels.put(lb);
//			}
//			cjs_data.put("labels", cjs_data_labels);
//			for(int i=0;i<colOrder.length;i++) {
//				JSONObject jc = jcols.getJSONObject(colOrder[i]);
//				for(int p=0;p < aggOrder.length;p++) {
//				JSONObject cjs_data_datasets_object = new JSONObject();
//				if(jsonColsArr.length() > 0) {
//					String cl = "";
//					for(int k=0;k<jsonColsArr.length();k++) {
//						cl += jc.get(jsonColsArr.getString(k));
//					}
//					if(aggOrder.length > 1) {
//						cl += "-" + jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name");
//					}
//					cjs_data_datasets_object.put("label", cl);
//				} else {
//					cjs_data_datasets_object.put("label", jsonAggregatesArr.getJSONObject(aggOrder[p]).getString("name"));
//				}
//				if (p_chartType	 == TYPE_LINECHART){
//					cjs_data_datasets_object.put("lineTension", 0);
//					cjs_data_datasets_object.put("fill", false);
//				}
//				if(colOrder.length * aggOrder.length > 1 && p_chartType != TYPE_PIECHART) {
//					cjs_data_datasets_object.put("backgroundColor", getBackgroundColor(i * aggOrder.length + p) );
//					cjs_data_datasets_object.put("borderColor", getBorderColor(i * aggOrder.length + p) );
//				} else {
//					JSONArray bgcArr = new JSONArray();
//					JSONArray boArr = new JSONArray();
//					for(int j=0;j<rowOrder.length;j++) {
//						bgcArr.put(j, getBackgroundColor(j) );
//						boArr.put(j, getBorderColor(j) );
//					}
//					cjs_data_datasets_object.put("backgroundColor", bgcArr);
//					cjs_data_datasets_object.put("borderColor", boArr);
//				}
//				
//				JSONArray cjs_data_datasets_object_data = new JSONArray();
//				
//				for(int j=0;j<rowOrder.length;j++) {
//					JSONObject jr = jrows.getJSONObject(rowOrder[j]);
//					JSONArray da = jr.getJSONArray("datas");
//					JSONArray cellArr = da.getJSONArray(colOrder[i]);
//					double d = cellArr.getDouble(aggOrder[p]);
//					cjs_data_datasets_object_data.put(d);
//				}
//				cjs_data_datasets_object.put("data", cjs_data_datasets_object_data);
//				cjs_data_datasets.put(cjs_data_datasets_object);
//				}
//			}
		}
		
		cjs_data.put("datasets",cjs_data_datasets);
		cjs.put("data", cjs_data);
		JSONObject cjs_option = new JSONObject(defaultOption);
//		JSONObject cjs_option = new JSONObject();
		if(title != null) {
			JSONObject cjs_option_title = new JSONObject();
			cjs_option_title.put("text",title);
			cjs_option_title.put("display",true);
			cjs_option.put("title", cjs_option_title);
		}


		if(cjs_data_datasets.length() > 0) {
			JSONArray cjs_axislist = new JSONArray();
			int nYAxis = jsonAggregatesArr.length();
			for(int i=0;i<cjs_data_datasets.length();i++) {
				JSONObject jo = cjs_data_datasets.getJSONObject(i);
				jo.put("yAxisID", "y-axis-"+(i % nYAxis));
			}
			for(int i=0;i<nYAxis;i++) {
				JSONObject cjs_axis = new JSONObject();
				cjs_axis.put("type", "linear");
				cjs_axis.put("display", true);
				cjs_axis.put("position", (i % 2) == 0 ? "left" : "right");
				cjs_axis.put("id","y-axis-"+i);
				/*
				jo = new JSONObject();
				jo.put("beginAtZero",true);
				cjs_axis.put("ticks", jo);
				*/
				JSONObject jo = new JSONObject();
				jo.put("labelString",jsonAggregatesArr.getJSONObject(i).getString("name"));
				if(nYAxis > 1) {
					jo.put("display", true);
				}
				cjs_axis.put("scaleLabel", jo);
				cjs_axislist.put(cjs_axis);
			}
			JSONObject jo = new JSONObject();
			jo.put("yAxes", cjs_axislist);

			cjs_option.put("scales", jo);
		}
		
		cjs.put("options", cjs_option);
		return(cjs);
	}
	
}
