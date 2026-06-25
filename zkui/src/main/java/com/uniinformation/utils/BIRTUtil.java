package com.uniinformation.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.DocxRenderOption;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IExcelRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.ColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.Expression;
import org.eclipse.birt.report.model.api.GridHandle;
import org.eclipse.birt.report.model.api.ImageHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.ScriptDataSetHandle;
import org.eclipse.birt.report.model.api.ScriptDataSourceHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.TextItemHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.elements.structures.ResultSetColumn;
import org.eclipse.birt.report.model.elements.interfaces.IStyleModel;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.birt.ScriptedDataSetEventHander;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BIRTUtil {
	private static IReportEngine engine = null;
	private final static String logDir = "/tmp";
	private final static Level logLevel = Level.OFF;
	private final static Object objLock = new Object();
	//private final static Level logLevel = Level.FINEST;

	public static void main(String[] args) throws Exception{
		//sample test.json content { "bindings": [ { "var1":"1", "var2":"2", }, { "var1":"3", "var2":"4", }, { "var1":"5", "var2":"6", } ] }
		//createDoc(BIRTUtil.class.getClass().getResourceAsStream("/com/uniinformation/birt/test.rptdesign"),"/tmp/birt/test1.xls", "/tmp/birt/test.json");
		//createDoc(BIRTUtil.class.getClass().getResourceAsStream("/com/uniinformation/birt/test.rptdesign"),"/tmp/birt/test1.xlsx", "/tmp/birt/test.json");
		//createDoc(BIRTUtil.class.getClass().getResourceAsStream("/com/uniinformation/birt/test.rptdesign"),"/tmp/birt/test1.pdf", "/tmp/birt/test.json");
		//createDoc(BIRTUtil.class.getClass().getResourceAsStream("/com/uniinformation/birt/test.rptdesign"),"/tmp/birt/test1.html", "/tmp/birt/test.json");
		createDoc1(BIRTUtil.class.getClass().getResourceAsStream("/com/uniinformation/birt/template1.rptdesign"),"/tmp/birt/test1.pdf");
	}
	
	/***
	 * for destory engine
	 * not required to call
	 */
	public static void destory(){
		UniLog.log1("called");
		synchronized(objLock){
			if(engine != null) {
			engine.destroy();
			engine = null;
			}
			Platform.shutdown();
		}
	}
	public static Object getObjLock() {
		return objLock;
	}
	public static IReportEngine getReportEngine() {
		return engine;
	}
	
	public static void initEngine(SessionHelper sh) throws Exception{
		synchronized(objLock){
			if (engine != null) {
				return;
			}
			UniLog.log1("create engine");
			EngineConfig config = new EngineConfig();
			//config.setBIRTHome("/Users/andre/Downloads/birt-runtime-4_2_2/ReportEngine");    //seems not required to set BIRTHome
			config.setLogConfig(logDir, logLevel);
			String cfgFile = "fontsConfig_pdf.xml";
			if (sh != null)
				cfgFile = BiConfig.getString(sh, "birt_fontconfig_file", cfgFile);
			config.setFontConfig(com.uniinformation.birt.ReportGenerate.class.getResource(cfgFile).toURI().toURL());
			Platform.startup(config);
			IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			engine = factory.createReportEngine(config);      
		}
	}
	public static void initEngine() throws Exception{
		initEngine(null);
	}

	/***
	 * create pdf/xls/html file using birt. 
	 * All report data is obtain from json file.
	 * TODO (2): design a general json file data structure for passing data to birt.
	 * @param p_rptdesign birt rptdesign file
	 * @param p_outFileName switch doc type based on extension
	 * @return ok/fail ReturnMsg
	 * @throws EngineException
	 */
	public static ReturnMsg createDoc(InputStream p_rptdesign, String p_outFileName, String p_jsonFileName) throws EngineException {
		synchronized(objLock){
			try {
				UniLog.log1("start");
				initEngine();
				if (p_rptdesign == null){
					UniLog.log1("rptdesign not found");
					return ReturnMsg.defaultFail;
				}
				if (StringUtils.isBlank(p_outFileName)){
					UniLog.log1("outFile is blank");
					return ReturnMsg.defaultFail;
				}

				//1. load design
				IReportRunnable design = engine.openReportDesign(p_rptdesign);
				p_rptdesign.close();
				
				//2. pass json data to rptdesign property
				ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();
				String jsonData = FileUtils.readFileToString(new File(p_jsonFileName), "UTF-8");
				//JSONObject json = new JSONObject(jsonData);
				Gson gson = new GsonBuilder()
						.setPrettyPrinting()
						.setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
						.create();
				Map<String, Object> jsonMap = gson.fromJson(jsonData, new TypeToken<Map<String, Object>>(){}.getType());
				
				
				//test create label 
				ElementFactory ef = design.getDesignHandle().getElementFactory();
				designHandle.getBody().add(newLabel(ef,"label#1"));
				designHandle.getBody().add(newLabel(ef,"label#2"),0);
				
				//test create grid
				GridHandle grid = ef.newGridItem(null, 3, 3);
				grid.setProperty(StyleHandle.BACKGROUND_COLOR_PROP, "Red");
				/*
				grid.setProperty(StyleHandle.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				*/
				for (int i=0; i<3; i++){
					for (int j=0;j<3; j++){
						((CellHandle)((RowHandle) grid.getRows().get(i)).getCells().get(j)).getContent().add(newLabel(ef,"GRID:"+i+j));
					}
				}
				
				grid.setWidth("100%");
				designHandle.getBody().add(grid,0);
				
				//fill global item
				Map<String, String> globalJsonMap = (Map<String, String>) jsonMap.get("global");
				for (Map.Entry<String, String> entry : globalJsonMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					DesignElementHandle element = designHandle.findElement(key);
					if (element != null) {
						if (element instanceof ImageHandle)
							((ImageHandle) element).setURL("\"" + value + "\"");
						else if (element instanceof TextItemHandle)
							((TextItemHandle) element).setContent(value);
						else if (element instanceof LabelHandle)
							((LabelHandle) element).setText(value);
					}
				}
				//3. pass json dataset to rptdesign dataset
				final String dataSetName = "Data Set 1";
				List<Map<String, Object>> dataSetRecordList = (List<Map<String, Object>>) jsonMap.get(dataSetName);
				ScriptedDataSetEventHander.clearRecordList();
				ScriptedDataSetEventHander.addRecordList(dataSetName, dataSetRecordList);
				
				
				
				/*JSONObject globalJson = json.optJSONObject("global");
				if (globalJson != null) {
					Iterator<String> it = globalJson.keys();
					while (it.hasNext()) {
						String key = it.next();
						String value = globalJson.getString(key);
						DesignElementHandle element = designHandle.findElement(key);
						if (element != null) {
							if (element instanceof ImageHandle)
								((ImageHandle) element).setURL("\"" + value + "\"");
							else if (element instanceof TextItemHandle)
								((TextItemHandle) element).setContent(value);
							else if (element instanceof LabelHandle)
								((LabelHandle) element).setText(value);
						}
					}
				}*/
				//3. pass json dataset to rptdesign dataset
				/*StringBuilder sb = new StringBuilder();
				sb.setLength(0);
				jss(sb,"importPackage(Packages.java.io);");
				jss(sb,"importPackage(Packages.org.apache.commons.io);");
				jss(sb,"log('prepare called');");
				jss(sb,"jsonData = FileUtils.readFileToString(new File('%s'),\"UTF-8\");", p_jsonFileName);
				jss(sb,"jsonObj = eval('('+ jsonData +')');");
				//UniLog.log1("prepare:\n" +sb.toString());
				designHandle.setOnPrepare(sb.toString());
				
				UniLog.log1("dataset count=%d",designHandle.getDataSets().getCount());
				for (int i = 0; i < designHandle.getDataSets().getCount(); i++) {
					ScriptDataSetHandle dataSet = (ScriptDataSetHandle)designHandle.getDataSets().get(i);
					UniLog.log1("dataSet.getName():" + dataSet.getName());
					
					sb.setLength(0);
					jss(sb,"log('(%s) open called');",dataSet.getName());
					jss(sb,"len = jsonObj['"+dataSet.getName()+"'].length;");
					jss(sb,"count = 0;");
					jss(sb,"log('json len='+len);");
					//UniLog.log1("open:\n" +sb.toString());
					dataSet.setOpen(sb.toString());
					
					sb.setLength(0);
					jss(sb,"log('(%s) fetch called');",dataSet.getName());
					jss(sb,"if (count < len){");
					jss(sb,"   binding = jsonObj['"+dataSet.getName()+"'][count];");
					jss(sb,"   for (k in binding){");
					jss(sb,"	      row[k] = binding[k];");
					jss(sb,"       log('count='+count+' row['+k+']='+row[k])");
					jss(sb,"   }");
					jss(sb,"   count++;");
					jss(sb,"   return true;");
					jss(sb,"}");
					jss(sb,"else{");
					jss(sb,"   return false;");
					jss(sb,"}");
					//UniLog.log1("fetch:\n" +sb.toString());
					dataSet.setFetch(sb.toString());
					
					sb.setLength(0);
					jss(sb,"log('(%s) close called');",dataSet.getName());
					//UniLog.log1("close:\n" +sb.toString());
					dataSet.setClose(sb.toString());
				}*/

				//4. create and run task
				createRunAndRenderTask(design, p_outFileName);
				UniLog.log1("done");
				return ReturnMsg.defaultOk;
			} 
			catch(Exception ex) {
				ex.printStackTrace();
				return new ReturnMsg(ex);
			} 
		}
	}
	public static ReturnMsg createDoc1(InputStream p_rptdesign, String p_outFileName) throws EngineException {
		synchronized(objLock){
			try {
				UniLog.log1("start");
				initEngine();
				if (p_rptdesign == null){
					UniLog.log1("rptdesign not found");
					return ReturnMsg.defaultFail;
				}
				if (StringUtils.isBlank(p_outFileName)){
					UniLog.log1("outFile is blank");
					return ReturnMsg.defaultFail;
				}

				//1. load design
				IReportRunnable design = engine.openReportDesign(p_rptdesign);
				p_rptdesign.close();
				
				ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();
				ElementFactory ef = design.getDesignHandle().getElementFactory();

				final int columnCount = 5;
				final String dataSetName = "Data Set 1";
				final String[] dsFieldList = new String[] {
					"field1", "field2", "field3", "field4", "field5"
				};
				final String[] dsFieldNameList = new String[] {
					"Field1", "Field2", "Field3", "Field4", "Field5"
				};
				final String[] dsFieldTypeList = new String[] {
					"string", "float", "string", "integer", "date"
				};
				final SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
				final List<Map<String, Object>> dsList = new ArrayList<Map<String, Object>>();
				dsList.add(new HashMap<String, Object>(){{
					put("field1", "A1");
					put("field2", 2);
					put("field3", "A3");
					put("field4", 4);
					put("field5", sdf.parse("2020/03/20"));
				}});
				dsList.add(new HashMap<String, Object>(){{
					put("field1", "B1");
					put("field2", 2000.16);
					put("field3", "B3");
					put("field4", 4000);
					put("field5", sdf.parse("2019/12/13"));
				}});
				dsList.add(new HashMap<String, Object>(){{
					put("field1", "C1");
					put("field2", 210000.78);
					put("field3", "C3");
					put("field4", 410000000);
					put("field5", sdf.parse("2008/08/08"));
				}});

				designHandle.setProperty("Title", "Test Report Title");
				designHandle.setProperty("SubTitle", "Test Report SubTitle");
				designHandle.setProperty("SubTitle1", "Date: 2019/12/13 - 2020/03/03");

				//setup dataset
				DataSetHandle dataSet = (ScriptDataSetHandle) designHandle.findDataSet(dataSetName);
				PropertyHandle resultSet = dataSet.getPropertyHandle(DataSetHandle.RESULT_SET_PROP);
				for (int i = 0; i < columnCount; i++) {
					ResultSetColumn column = StructureFactory.createResultSetColumn();
					column.setColumnName(dsFieldList[i]);
					column.setDataType(dsFieldTypeList[i]); 
					resultSet.addItem(column);
				}

				//new table
				TableHandle table = ef.newTableItem("Table 1", columnCount, 1, 1, 0);
				designHandle.getBody().add(table);
				table.setWidth("100%");
				table.setDataSet(dataSet);

				RowHandle headerRow = (RowHandle)table.getHeader().getContents().get(0);
				RowHandle detailRow = (RowHandle)table.getDetail().getContents().get(0);
				SlotHandle headerCells = headerRow.getCells();
				SlotHandle detailCells = detailRow.getCells();
				for (int i = 0; i < columnCount; i++) {
					//add column binding
					ResultSetColumn resetSetColumn = (ResultSetColumn) resultSet.getItems().get(i);
					String dataType = resetSetColumn.getDataType();
					ComputedColumn computedColumn = StructureFactory.createComputedColumn( );
					computedColumn.setName(dsFieldList[i]);
					computedColumn.setDataType(dataType);
					computedColumn.setExpression("dataSetRow[\""+resetSetColumn.getColumnName()+"\"]");
					table.addColumnBinding(computedColumn, false);

					//setup column style
					UniLog.log1("columnname:%s,datatype:%s", resetSetColumn.getColumnName(), resetSetColumn.getDataType());
					ColumnHandle column = (ColumnHandle) table.getColumns().get(i);
					if (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT))
						column.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_RIGHT);
					else
						column.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_LEFT);

					//setup header
					CellHandle cell = (CellHandle) headerCells.get(i);
					if (i > 0) {
						cell.setProperty(IStyleModel.BORDER_LEFT_WIDTH_PROP, "1px");
						cell.setProperty(IStyleModel.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					}
					cell.setProperty(IStyleModel.BORDER_TOP_WIDTH_PROP, "1px");
					cell.setProperty(IStyleModel.BORDER_TOP_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					cell.setProperty(IStyleModel.BORDER_BOTTOM_WIDTH_PROP, "1px");
					cell.setProperty(IStyleModel.BORDER_BOTTOM_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					
					LabelHandle label = ef.newLabel(null);
					label.setText(dsFieldNameList[i]);
					cell.getContent().add(label);
					
					//setup detail
					cell = (CellHandle) detailCells.get(i);
					DataItemHandle dataItem = ef.newDataItem(dsFieldList[i] + "_" + i);
					dataItem.setResultSetColumn(dsFieldList[i]);
					if (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER))
						dataItem.setOnCreate("setDefaultIntegerDisplayValue(this);");
					else if (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT))
						dataItem.setOnCreate("setDefaultFloatDisplayValue(this);");
					else if (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DATE))
						dataItem.setOnCreate("setDefaultDateDisplayValue(this);");
					cell.getContent().add(dataItem);
				}

				//3. pass dataset to rptdesign dataset
				ScriptedDataSetEventHander.clearRecordList();
				ScriptedDataSetEventHander.addRecordList(dataSetName, dsList);
				
				//4. create and run task
				createRunAndRenderTask(design, p_outFileName);
				UniLog.log1("done");
				return ReturnMsg.defaultOk;
			} 
			catch(Exception ex) {
				ex.printStackTrace();
				return new ReturnMsg(ex);
			} 
		}
	}

	public static void createRunAndRenderTask(IReportRunnable design, RenderOption renderOption) throws EngineException {
		createRunAndRenderTask(design, renderOption, Locale.TRADITIONAL_CHINESE);
	}
	public static void createRunAndRenderTask(IReportRunnable design, RenderOption renderOption, Locale locale) throws EngineException {
		IRunAndRenderTask task = engine.createRunAndRenderTask(design);      
		task.setLocale(locale);
		task.setRenderOption(renderOption);
		task.run();
		task.close();
	}
	public static void createRunAndRenderTask(IReportRunnable design, String p_outFileName) throws EngineException {
		createRunAndRenderTask(design, p_outFileName, Locale.TRADITIONAL_CHINESE);
	}
	public static void createRunAndRenderTask(IReportRunnable design, String p_outFileName, Locale locale) throws EngineException {
		RenderOption renderOption;
		if (StringUtils.endsWithAny(p_outFileName, "xls")){
			renderOption = new EXCELRenderOption();
			renderOption.setOutputFormat("xls");
		}
		else if (StringUtils.endsWithAny(p_outFileName, "xlsx")){ //REMARK: does not work, probably due to lib version??
			renderOption = new EXCELRenderOption();
			renderOption.setOutputFormat("xlsx");
			//renderOption.setOption( IExcelRenderOption.OFFICE_VERSION, "office2007");
		}
		else if (StringUtils.endsWithAny(p_outFileName, "docx")){ 
			renderOption = new DocxRenderOption();
			renderOption.setOutputFormat("docx");
		}
		else if (StringUtils.endsWithAny(p_outFileName, "html")){ //REMARK:
			renderOption = new HTMLRenderOption();
			renderOption.setOutputFormat("html");
		}
		else{
			renderOption = new PDFRenderOption();
			renderOption.setOutputFormat("pdf");
			renderOption.setOption(PDFRenderOption.PDF_HYPHENATION, true);
		}
		renderOption.setOutputFileName(p_outFileName);
		renderOption.setOption(IPDFRenderOption.LOCALE, locale);
		createRunAndRenderTask(design, renderOption, locale);
	}

	public static StringBuilder jss(StringBuilder p_sb,String p_format, Object... p_args){
		String str = p_format;
		if (p_args == null || p_args.length == 0){
		   str = p_format +"\n";
		}
		else{
		   str = String.format(p_format, p_args) +"\n";
		}
		str = str.replaceAll("^log\\(", "java.lang.System.out.println(");
		str = str.replaceAll(" log\\(", " java.lang.System.out.println(");
		//UniLog.log1("in:%s out:%s", p_format, str);
		p_sb.append(str);
		return(p_sb);
		
	}
	private static LabelHandle newLabel(ElementFactory ef, String text) throws Exception{
		LabelHandle label = ef.newLabel(null);
		label.setText(text);
		return label;
	}

}