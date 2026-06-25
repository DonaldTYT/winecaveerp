package com.uniinformation.zkbi.erpv4;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.TextItemHandle;
import org.zkoss.zhtml.Filedownload;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.impl.MessageboxDlg;

//import com.ibm.icu.text.SimpleDateFormat;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.birt.ScriptedDataSetEventHander;
import com.uniinformation.cell.Cell;
import com.uniinformation.utils.BIRTUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerStockLedger1 extends ZkBiComposerBase{
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	protected void setupAddButton(final BiResult result) {
		super.setupAddButton(result);
		final Button btnStockLedgerReport = new ZkBiButton();
		btnStockLedgerReport.setLabel("Stock Ledger Report");
		btnStockLedgerReport.setId("btStockLedgerReport");
		btnStockLedgerReport.addEventListener("onClick", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("button clicked");
				createBirtDoc(result, false, "/com/uniinformation/birt/StockLedger.rptdesign", "StockLedgerReport");
			}
		});
		abHelper.addButton(btnStockLedgerReport);

		final Button btnStockLedgerBreakdownReport = new ZkBiButton();
		btnStockLedgerBreakdownReport.setLabel("Stock Ledger Report (Break down)");
		btnStockLedgerBreakdownReport.setId("btStockLedgerBreakdownReport");
		btnStockLedgerBreakdownReport.addEventListener("onClick", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("button clicked");
				createBirtDoc(result, true, "/com/uniinformation/birt/StockLedgerBreakdown.rptdesign", "StockLedgerBreakdownReport");
			}
		});
		abHelper.addButton(btnStockLedgerBreakdownReport);
	}
	private void createBirtDoc(final BiResult result, final boolean isBreakdownReport, final String designRes, final String outputFileName) {
		final String[] dsFieldList = new String[] {
			"st_icode", "stm_date", "stm_ref1", "stmd_tdtype", "stmd_trandesc", "orders_pocode", "stmd_cur", 
			"stmd_uprice", "inv_invno", "inv_cid", "stmd_sprice", "stmd_pdsost", "stmd_onhandqty", "stmd_ref4"
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
				if (StringUtils.equals(label, "stm_date")) {
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
		final Datebox datebox0 = new Datebox(minDate);
		final Datebox datebox1 = new Datebox(maxDate);
		datebox0.setConstraint(String.format("no empty"));
		datebox1.setConstraint(datebox0.getConstraint());
		datebox0.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("datebox0 event:" + event + ",value:" + datebox0.getValue());
				Date d0 = datebox0.getValue();
				Date d1 = datebox1.getValue();
				if (d0 != null && (d1 == null || d1.compareTo(d0) < 0))
					datebox1.setValue(d0);
			}
		});
		final Hbox hbox = new Hbox(){{
			appendChild(datebox0);
			appendChild(new Label("to"));
			appendChild(datebox1);
		}};
		MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Please select date range", 
			hbox, 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
			masterWin, 
			new EventListener<Messagebox.ClickEvent>(){
				@Override
				public void onEvent(ClickEvent event) throws Exception {
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
					case OK:
						/*//remove records exclude stm_date range
						Iterator<Map<String, Object>> it = dsList.iterator();
						while (it.hasNext()) {
							Map<String, Object> map = it.next();
							Date date = dateFormat.parse((String)map.get("stm_date"));
							if (isBreakdownReport) {
								if (date.compareTo(datebox0.getValue()) < 0 || date.compareTo(datebox1.getValue()) > 0)
									it.remove();
							} else {
								if (date.compareTo(datebox1.getValue()) > 0)
									it.remove();
							}
						}*/
						createBreakdownBirtDoc(datebox0.getValue(), datebox1.getValue(), dsList, designRes, outputFileName);
						break;
					default:
						break;
					}
				}
			}
		);
		dlg.doHighlighted();
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
	private void createBreakdownBirtDoc(Date startDate, Date endDate, List<Map<String, Object>> dsList, String designRes, String outputFileName) throws Exception {
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

			//4. add record list to data set
			final String dataSetName = "Data Set 1";
			ScriptedDataSetEventHander.clearRecordList();
			ScriptedDataSetEventHander.addRecordList(dataSetName, dsList);

			//5. create and run task
			String outputFilePath = "/tmp/birt/tmppdf" + Thread.currentThread().getId() + "_" + System.currentTimeMillis();
			UniLog.log1("outputFilePath:%s", outputFilePath);
			BIRTUtil.createRunAndRenderTask(design, outputFilePath);
//			String outputXlsFilePath = "/tmp/birt/tmppdf" + Thread.currentThread().getId() + "_" + System.currentTimeMillis() + ".xls";
			String outputXlsFilePath = "c:/tmp/birt/tmppdf" + Thread.currentThread().getId() + "_" + System.currentTimeMillis() + ".xls";
			BIRTUtil.createRunAndRenderTask(design, outputXlsFilePath);
			
			//6. show pdf
			File file = new File(outputFilePath);
			ZkUtil.showPdfDialog(masterWin, sessionHelper, FileUtils.readFileToByteArray(file), outputFileName);
			file.delete();
			
			UniLog.log1("done");
		}
	}
}
