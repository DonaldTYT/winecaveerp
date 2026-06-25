package com.uniinformation.zkbi.erpv4;


import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.zkoss.zk.ui.Component;

//import com.ibm.icu.text.SimpleDateFormat;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.birt.ScriptedDataSetEventHander;
import com.uniinformation.cell.Cell;
import com.uniinformation.utils.BIRTUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.utils.whereclpar.Parser;
import com.uniinformation.zkbi.ZkBiFormComposer;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;


public class ZkBiStockLedgerReport extends ZkBiFormComposer {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		onClickListener = new EventListener(){
			@Override
			public void onEvent(final Event p_event) throws Exception {
				if(p_event.getTarget().getId().equals("btPrint")) {
					Cell condIcode = formCollection.testCell("cond_icode");
					Cell condDate  = formCollection.testCell("cond_date");
					Cell showDetail = formCollection.testCell("rpt_showdetail");
//					Date d0 = DateUtil.zeroDate;
//					Date d1 = DateUtil.maxDate;
					Date d0 = DateUtil.getDate("2018/01/01");
					Date d1 = DateUtil.getDate("2020/04/01");
					result.clear();
					result.clearCondition();
					if(!condIcode.getString().trim().equals("")) {
						result.addCustomCondition(condIcode.getString());
					}
					if(!condDate.getString().trim().equals("")) {
						Condition cond = (Condition) new Parser(condDate.getString(),null,null).parse();
						// assumount the return cond is predicate
						switch(cond.get_operator()) {
						case Condition.COMPARE_OP_EQ:
								d0 = DateUtil.dateTimeStrToDate(cond.get_rightExpression().eval(null).toString());
								d1 = DateUtil.dateTimeStrToDate(cond.get_rightExpression().eval(null).toString());
								break;
						case Condition.COMPARE_OP_LE:
								d1 = DateUtil.dateTimeStrToDate(cond.get_rightExpression().eval(null).toString());
								break;
						case Condition.COMPARE_OP_GE:
								d0 = DateUtil.dateTimeStrToDate(cond.get_rightExpression().eval(null).toString());
								break;
						case Condition.COMPARE_OP_BETWEEN:
								d0 = DateUtil.dateTimeStrToDate(cond.get_rightExpression1().eval(null).toString());
								d1 = DateUtil.dateTimeStrToDate(cond.get_rightExpression2().eval(null).toString());
								break;
						}
					}
					result.clearOrderBy();
					result.query();
					final String[] dsFieldList = new String[] {
							"st_icode", "stm_date", "stm_ref1", "stmd_tdtype", "stmd_trandesc", "orders_pocode", "stmd_cur", 
							"stmd_uprice", "inv_invno", "inv_cid", "stmd_sprice", "stmd_pdsost", "stmd_onhandqty", "stmd_ref4"
						};
					final List<Map<String, Object>> dsList = new ArrayList<Map<String, Object>>();
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
						}
						dsList.add(m);
					}
					String designRes = "/com/uniinformation/birt/StockLedger.rptdesign";
					if(showDetail != null && showDetail.getBoolean()) {
						designRes = "/com/uniinformation/birt/StockLedgerBreakdown.rptdesign";
					} 
					String outputFileName = "StockLedgerReport";
					createBreakdownBirtDoc(d0, d1, dsList, designRes, outputFileName);
				}
				
			}
		};	
		super.doAfterCompose(arg0);
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
			
			//6. show pdf
			File file = new File(outputFilePath);
			ZkUtil.showPdfDialog(rootComp, sessionHelper, FileUtils.readFileToByteArray(file), outputFileName);
			file.delete();
			
			UniLog.log1("done");
		}
	}

}
