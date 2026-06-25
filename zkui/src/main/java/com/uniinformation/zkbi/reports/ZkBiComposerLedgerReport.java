package com.uniinformation.zkbi.reports;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.birt.ScriptedDataSetEventHander;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.BIRTUtil;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;


public class ZkBiComposerLedgerReport extends ZkBiComposerAggregateReport {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		if(zkfName == null) zkfName = "zkf/reports/LedgerReport.zul";	
		listboxHeightAdjust=60;
		super.doAfterCompose(comp);
	}
	
	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
    	Cell cdc = rptCol.testCell("rptCondition");
    	if(cdc != null) {
   		try {
    	String conditions = inputFieldsList.getCustomCondition();
    	if(conditions != null && !conditions.equals(""))  {
    		cdc.set(BiCellCollection.translateCond(result.getView(),conditions,result));
		} else {
    		cdc.set("Anything");
		}	
   		} catch (CellException cex) {
   			UniLog.log(cex);
   		}
    	}
		Date d0 = rptCol.getDate("sdate");
		Date d1 = rptCol.getDate("edate");
		if(d0 == null) d0 = DateUtil.zeroDate;
		if(d1 == null) d1 = DateUtil.zeroDate;
		if(!d0.after(DateUtil.minDate) || !d1.after(DateUtil.minDate)) {
			return(new ReturnMsg(false,"Please Select Date Range"));
		}
		if(result instanceof BiLedgerReportInterface) {
			((BiLedgerReportInterface) result).setLedgerDate(d0,d1);
			/*
			result.addCustomCondition(
					((BiLedgerReportInterface) result).getCumulatorColumn() + " <= '" + DateUtil.toDateString(d1, "yyyy/mm/dd") + "'");
					*/
		} else {
			result.addCustomCondition(
					((BiLedgerReportInterface) result).getCumulatorColumn() + " between '" + DateUtil.toDateString(d0, "yyyy/mm/dd") + "' and '" + DateUtil.toDateString(d1, "yyyy/mm/dd") + "'");
		}
		return(ReturnMsg.defaultOk);
	}

	@Override
	protected void onSetupParameterChange(BiResult result,String p_id) {
		UniLog.log("Setup Change "+p_id);
		
		if(p_id.equals("showNoDetail")) {
			result.setQueryIncludeNoDetail(rptCol.getCell("showNoDetail").getBoolean());
		}
		if(p_id.equals("rgSummaryOrDetail")) {
			resetListHeader(result);
			String presetKey = conditionPresetListbox.getSelectedItem().getValue();
			visibleCols(presetKey, listbox,result);
		}
	}
	
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	try {
    	((BiLedgerReportInterface) result).setRptCol(rptCol);
    	Cell tc = rptCol.getCell("reportTitle"); 
    	if(tc != null) {
    		if(tc.getString().equals(""))
    			tc.set(((BiLedgerReportInterface) result).getReportTitle());
    	}
    	} catch (CellException cex) {
    		UniLog.log(cex);
    	}
		result.setQueryIncludeNoDetail(rptCol.getCell("showNoDetail").getBoolean());
		resetListHeader(result);
	}
	
//	@Override
//    protected void setupExtraButton(final BiResult result) {
//		
//	}
	
	
//	@Override
    protected void setupGeneralReportButtonXX(final BiResult result) {
		Button btnExport,btnPrint;
		if(!result.allowUpdate()) return;
    	if(masterWin.hasFellow("btExport")) {
    		btnExport = (Button) masterWin.getFellow("btExport");
    	} 
    	else {	
	        btnExport = new ZkBiButton();
	        btnExport.setLabel("Export to Excel");
	        btnExport.setId("btExport");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnExport, "fa-user");
    	} 
    	if(masterWin.hasFellow("btPrint")) {
    		btnPrint = (Button) masterWin.getFellow("btPrint");
    	} 
    	else {	
	        btnPrint = new ZkBiButton();
	        btnPrint.setLabel("Print to Pdf");
	        btnPrint.setId("btPrint");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnPrint, "fa-user");
    	} 
        btnExport.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {

            	}
        	}
        );	
        btnPrint.addEventListener("onClick",
                new EventListener() {
                	public void onEvent(Event event) throws Exception {
                		Cell c = rptCol.getCell("rgSummaryOrDetail");
                		if(c.getInt() == 0) {
							initBirtDoc(result, true, "/com/uniinformation/birt/StockLedgerBreakdown3.rptdesign", "StockLedgerBreakdownReport",false);
                		} else {
							initBirtDoc(result, false, "/com/uniinformation/birt/StockLedger3.rptdesign", "StockLedgerReport",false);
                		}
                	}
            	}
            );	
        
    }
	private void initBirtDoc(final BiResult result, final boolean isBreakdownReport, final String designRes, final String outputFileName,boolean p_toExcel) {
		BiLedgerReportInterface lpi = (BiLedgerReportInterface) result;
		Date minDate = null, maxDate = null;
		final List<Map<String, Object>> dsList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < result.getRowCount(); i++) {
			result.loadOneRecV(i);
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			for (BiLedgerReportInterface.ledgerColumns en : BiLedgerReportInterface.ledgerColumns.values()) { 
				Cell cc = lpi.getValue(en);
				if(cc != null) {
				switch (cc.getType()) {
				case Cell.VTYPE_DOUBLE:
					m.put(en.toString(), cc.getDouble());
					break;
				case Cell.VTYPE_INT:
					m.put(en.toString(), cc.getInt());
					break;
				case Cell.VTYPE_DATE:
					m.put(en.toString(), dateFormat.format(cc.getDate()));
					break;
				default:
					String ss =cc.getString();
					if(ss.isEmpty() && en == BiLedgerReportInterface.ledgerColumns.st_icode) {
						ss = "Nil";
					}
					m.put(en.toString(), ss);
					break;
				}
				}
				if(en == BiLedgerReportInterface.ledgerColumns.lg_date) {
					Date d = cc.getDate();
					if (minDate == null)
						minDate = maxDate = d;
					else {
						if (d.compareTo(minDate) < 0)
							minDate = d;
						if (d.compareTo(maxDate) > 0)
							maxDate = d;
					}
				}
			}
			dsList.add(m);
		}
		try {
			createBirtDoc(((BiLedgerReportInterface) result).getFilterStr(), minDate, maxDate, dsList, designRes, outputFileName,p_toExcel);
		} catch (Exception e) {
			e.printStackTrace();
			ZkUtil.showErrMsg("Create Birt Doc error: %s", StringEscapeUtils.escapeJavaScript(e.getMessage()));
		}
	}
	/**
	 * Setup stockledger.rpgdesign: 
	 *    (1) Add User Property "Start Date" and "End date"
	 *    (2) Set "Data Set 1" event handler class to "com.uniinformation.birt.ScriptedDataSetEventHander"
	 *    (3) Add global function script at report initialize
	 *    (4) Add table element, define table columns
	 *    (5) Setup table filters
	 *    (6) Setup table sorting
	 *    (7) Setup table groups
	 *    (8) Add Aggregation items to table
	 * **/
	protected void createBirtDoc(String p_subTitle,Date startDate, Date endDate, List<Map<String, Object>> dsList, String designRes, String outputFileName,boolean p_toExcel) throws Exception {
		synchronized(BIRTUtil.getObjLock()){
			final InputStream rptDesign = getClass().getResourceAsStream(designRes);

			UniLog.log1("start");
			BIRTUtil.initEngine();
			final IReportEngine engine = BIRTUtil.getReportEngine();

			//1. load design
			IReportRunnable design = engine.openReportDesign(rptDesign);
			rptDesign.close();

			//2. init rptdesign and json
			ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();

			//3. First, add User Property in stockledger.rptdesign, and then set its value
			designHandle.setProperty("Start date", startDate);
			designHandle.setProperty("End date", endDate);
			{
			if (startDate.getTime() < DateUtil.minDate.getTime())
				designHandle.setProperty("SubTitle", String.format("Date upto ", dateFormat.format(startDate)));
			else if (endDate.getTime() >= DateUtil.maxDate.getTime())
				designHandle.setProperty("SubTitle", String.format("Date from %s", dateFormat.format(endDate)));
			else
				designHandle.setProperty("SubTitle", String.format("Date Between %s and %s", dateFormat.format(startDate), dateFormat.format(endDate)));
			}
			designHandle.setProperty("SubTitle", p_subTitle);

			//setup subtitle1
			/*designHandle.setProperty("SubTitle1", "condition");
			MasterPageHandle materPageHandle = designHandle.findMasterPage("Simple MasterPage");
			materPageHandle.setProperty("headerHeight", "0.88in");*/

			//4. add record list to data set
			final String dataSetName = "Data Set 1";
			ScriptedDataSetEventHander.clearRecordList();
			ScriptedDataSetEventHander.addRecordList(dataSetName, dsList);

			if(p_toExcel) {
				File fd = new File("/tmp");
				File f = File.createTempFile("stkrpt", ".xlsx",fd);
				String outputXlsFilePath = f.getPath();
				BIRTUtil.createRunAndRenderTask(design, outputXlsFilePath);
				FileInputStream fis = new FileInputStream(outputXlsFilePath);
    			Filedownload.save(fis, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "stkrpt.xlsx");
    			f.delete();
			} else {
			//5. create and run task
			String outputFilePath = "/tmp/birt/tmppdf" + Thread.currentThread().getId() + "_" + System.currentTimeMillis();
			UniLog.log1("outputFilePath:%s, dsList size:%d", outputFilePath, dsList.size());
			BIRTUtil.createRunAndRenderTask(design, outputFilePath);
			//6. show pdf
			File file = new File(outputFilePath);
			ZkUtil.showPdfDialog(masterWin, sessionHelper, FileUtils.readFileToByteArray(file), outputFileName);
			file.delete();
			}
			UniLog.log1("done");
		}
	}
}
