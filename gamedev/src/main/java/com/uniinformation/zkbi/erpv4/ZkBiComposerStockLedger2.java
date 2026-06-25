package com.uniinformation.zkbi.erpv4;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.MasterPageHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Filedownload;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultStockLedger;
import com.uniinformation.birt.ScriptedDataSetEventHander;
import com.uniinformation.cell.Cell;
import com.uniinformation.utils.BIRTUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerStockLedger2 extends ZkBiComposerReport {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	Checkbox cbOutputToExcel;

	@Override
	protected void setupExportButton(final BiResult result) {
		super.setupExportButton(result);
		final Checkbox cbIncludeNoDetail = new Checkbox();
		cbIncludeNoDetail.setLabel("Include No Activty Record");
		actionBar.appendChild(cbIncludeNoDetail);
		cbOutputToExcel = new Checkbox();
		cbOutputToExcel.setLabel("Output To Excel");
		actionBar.appendChild(cbOutputToExcel);
		cbIncludeNoDetail.addEventListener(Events.ON_CLICK, 
				new EventListener() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						// TODO Auto-generated method stub
						result.setQueryIncludeNoDetail(cbIncludeNoDetail.isChecked());
						refresh(result,null);
					}
			
				}
			);

		final Button btnStockLedgerReport = new ZkBiButton();
		btnStockLedgerReport.setLabel("Stock Ledger Report");
		btnStockLedgerReport.setId("btStockLedgerReport");
		btnStockLedgerReport.addEventListener("onClick", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("button clicked");
				createBirtDoc(result, false, "/com/uniinformation/birt/StockLedger3.rptdesign", "StockLedgerReport");
			}
		});
		abHelper.addButton(btnStockLedgerReport);

		final Button btnStockLedgerBreakdownReport = new ZkBiButton();
		btnStockLedgerBreakdownReport.setLabel("Stock Ledger Report (Break down)");
		btnStockLedgerBreakdownReport.setId("btStockLedgerBreakdownReport");
		btnStockLedgerBreakdownReport.addEventListener("onClick", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("button clicked");
				createBirtDoc(result, true, "/com/uniinformation/birt/StockLedgerBreakdown3.rptdesign", "StockLedgerBreakdownReport");
			}
		});
		abHelper.addButton(btnStockLedgerBreakdownReport);
	}

	private void createBirtDoc(final BiResult result, final boolean isBreakdownReport, final String designRes, final String outputFileName) {
		final String[] dsFieldList = new String[] {
			"st_irg", "stmd_mrg", "stmd_tdtype", 
			"st_icode", "st_iname", "stmd_date", "stm_ref1", "stm_ref2", 
			"stmd_openbal", "stmd_inqty", "stmd_outqty", "stmd_closebal",
			"stmd_openamt", "stmd_inamount", "stmd_outamount", "stmd_closeamt", 
			"stmd_exprice1", "stmd_avcost"
		};

		final List<Map<String, Object>> dsList = new ArrayList<Map<String, Object>>();
		Date minDate = null, maxDate = null;
		for (int i = 0; i < result.getRowCount(); i++) {
			result.loadOneRecV(i);
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			for (String label : dsFieldList) {
				ColumnCell cc = result.getCell(label);
				switch (cc.getType()) {
				case Cell.VTYPE_DOUBLE:
					m.put(label, cc.getDouble());
					break;
				case Cell.VTYPE_INT:
					m.put(label, cc.getInt());
					break;
				case Cell.VTYPE_DATE:
					m.put(label, dateFormat.format(cc.getDate()));
					break;
				default:
					m.put(label, cc.getString());
					break;
				}
				if (StringUtils.equals(label, "stmd_date")) {
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
			createBirtDoc(((BiResultStockLedger) result).getFilterStr(), minDate, maxDate, dsList, designRes, outputFileName);
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
	private void createBirtDoc(String p_subTitle,Date startDate, Date endDate, List<Map<String, Object>> dsList, String designRes, String outputFileName) throws Exception {
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

			if(cbOutputToExcel.isChecked()) {
////				File f = File.createTempFile("stkrpt", ".xls");
//				File fd = new File("/tmp");
//				File f = File.createTempFile("stkrpt", ".xlsx",fd);
//				String outputXlsFilePath = f.getName();
//				BIRTUtil.createRunAndRenderTask(design, outputXlsFilePath);
////    			Filedownload.save(bos.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outFileName);
//				FileInputStream fis = new FileInputStream(outputXlsFilePath);
//    			Filedownload.save(fis, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "stkrpt.xlsx");
////    			fis.close();
////    			f.delete();
    			
    			
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
