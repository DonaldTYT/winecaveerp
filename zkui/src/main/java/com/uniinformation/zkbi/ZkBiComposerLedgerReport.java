package com.uniinformation.zkbi;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.birt.ScriptedDataSetEventHander;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.BIRTUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.zkf.ZkForm;


public class ZkBiComposerLedgerReport extends ZkBiComposerReport {
	// Ledger Report Should have a comparable class as the key of each transaction , typically the effective date of the transaction. or sometimes use a chronological ordered number such as the serial_id 
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	BiColumn cumulatorComparable;
	CellCollection rptCol;
	@Override
   	public BiResult initZkBiWindows()
   	{
		BiResult br = super.initZkBiWindows();
		cumulatorComparable = ((BiReportInterface) br).getCumulatorComparable();
		return(br);
   	}
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
    	
	    final ZkForm zkf1;
	    String ss = BiConfig.getString(getSessionHelper(), "StockLedgerZul");
	    if(ss != null) {
	    	zkf1 = new ZkForm(rpth,ss);
	    } else {
	    	zkf1 = new ZkForm(rpth,"zkf/erpv4/ledgerReport.zul");
	    }
	    rptCol = new CellCollection();
    	rptCol.addCell("rptCondition", new Cell(""));
    	Cell c = rptCol.addCell("cbIncludeNoActivity", new Cell(false));
    	c.addAction(
    				new CellValueAction() {

						@Override
						public void cellAction_onchange(Cell p_value) throws CellException {
							// TODO Auto-generated method stub
								onSelectionChanged(result,null);
						}

						@Override
						public void cellAction_onfree() throws CellException {
							// TODO Auto-generated method stub
							
						}
    					
    				}
    			);
//   	Condition cond = result.getCustomCondition();
	    try {
	    	onSelectionChanged(result,null);
	    	zkf1.mapCellCollection(rptCol,new EventListener() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						if(arg0.getTarget().getId().equals("btGenerate")) {
							if(false) {
								Messagebox.show("Condition Incomplete");
							} else {
								result.clearOrderBy();
								result.setQueryIncludeNoDetail(rptCol.getCell("cbIncludeNoActivity").getBoolean());
//        			        	refresh(result,masterWin,-1,true,true); 

        			        	refresh(result, masterWin, (MultiSortMap) null, false);
        			        	zkbiListTop.setVisible(true);
							}
						
						}
						if(arg0.getTarget().getId().equals("btPrint")) {
							if(rptCol.getCell("cbPrintDetail").getBoolean()) {
								initBirtDoc(result, true, "/com/uniinformation/birt/StockLedgerBreakdown3.rptdesign", "StockLedgerBreakdownReport");
							} else {
								initBirtDoc(result, false, "/com/uniinformation/birt/StockLedger3.rptdesign", "StockLedgerReport");
							}
						}
	    			}
	    		}
	    	);
	    } catch (CellException cex ) {
	    	UniLog.log(cex);
	    }
	}
	@Override
	protected void setupExportButton(final BiResult result) {
		if(getSessionHelper().isAdminUser()) {
			super.setupExportButton(result);
		}
	}
	@Override
    protected void setupGeneralReportButton(final BiResult result) {
    	
    }
    protected void onSelectionChanged(BiResult p_result,MultiSortMap sortMap) throws CellException {
    	String conditions = inputFieldsList.getCustomCondition();
    	Cell cdc = rptCol.getCell("rptCondition");
    	if(conditions != null && !conditions.equals(""))  {
    		cdc.set(BiCellCollection.translateCond(p_result.getView(),conditions,p_result));
		} else {
    		cdc.set("All");
		}
       	zkbiListTop.setVisible(false);
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
	protected void createBirtDoc(String p_subTitle,Date startDate, Date endDate, List<Map<String, Object>> dsList, String designRes, String outputFileName) throws Exception {
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

			if(rptCol.getCell("cbOutputExcel").getBoolean()) {
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
	
	private void initBirtDoc(final BiResult result, final boolean isBreakdownReport, final String designRes, final String outputFileName) {
		/*
		final String[] dsFieldList = new String[] {
			"st_irg", "stmd_mrg", "stmd_tdtype", 
			"st_icode", "st_iname", "stmd_date", "stm_ref1", "stm_ref2", 
			"stmd_openbal", "stmd_inqty", "stmd_outqty", "stmd_closebal",
			"stmd_openamt", "stmd_inamount", "stmd_outamount", "stmd_closeamt", 
			"stmd_exprice1", "stmd_avcost"
		};
		*/
		BiReportInterface lpi = (BiReportInterface) result;
//		lpi.setGroupBy(rptCol.getCellInt("rgGroupBy"));
		Date minDate = null, maxDate = null;
		final List<Map<String, Object>> dsList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < result.getRowCount(); i++) {
			result.loadOneRecV(i);
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			for (BiReportInterface.ledgerColumns en : BiReportInterface.ledgerColumns.values()) { 
				ColumnCell cc = lpi.getValue(en);
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
					if(ss.isEmpty() && en == BiReportInterface.ledgerColumns.st_icode) {
						ss = "Nil";
					}
					m.put(en.toString(), ss);
					break;
				}
				}
				if(en == BiReportInterface.ledgerColumns.lg_date) {
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
		/*
		final String[] dsFieldList = new String[] {
			"st_icode", "st_iname", "stmd_date", "stm_ref1", "stm_ref2", 
			"stmd_tdtype", 
			"stmd_openbal", "stmd_inqty", "stmd_outqty", "stmd_closebal",
			"stmd_openamt", "stmd_inamount", "stmd_outamount", "stmd_closeamt", 
			"stmd_exprice1", "stmd_avcost"
		};

		for (int i = 0; i < result.getRowCount(); i++) {
			result.loadOneRecV(i);
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			for (String label : dsFieldList) {
				String rptLabel = label;
				if(label.equals("stmd_date")) rptLabel = "lg_date";
				ColumnCell cc = result.getCell(label);
				switch (cc.getType()) {
				case Cell.VTYPE_DOUBLE:
					m.put(rptLabel, cc.getDouble());
					break;
				case Cell.VTYPE_INT:
					m.put(rptLabel, cc.getInt());
					break;
				case Cell.VTYPE_DATE:
					m.put(rptLabel, dateFormat.format(cc.getDate()));
					break;
				default:
					m.put(rptLabel, cc.getString());
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
		*/
		try {
			createBirtDoc(((BiReportInterface) result).getFilterStr(), minDate, maxDate, dsList, designRes, outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
			ZkUtil.showErrMsg("Create Birt Doc error: %s", StringEscapeUtils.escapeJavaScript(e.getMessage()));
		}
	}
	HashSet<String> setLedgerLocationByMode(int mode,SelectUtil su) {
		HashSet<String> hs = null;
		switch(mode) {
		case 1: 
				hs = new HashSet<String>();
				hs.add("CTR02");
				break;
		case 2: 
				hs = new HashSet<String>();
				try {
					TableRec tr = su.getQueryResult("select * from locationcode where loc_code <> 'CTR02'");
					for(int i = 0;i<tr.getRecordCount();i++) {
//						hs.add("TST01");
						tr.setRecPointer(i);
						hs.add(tr.getFieldString("loc_code"));
					}
				} catch (Exception p_ex) {
					UniLog.log(p_ex);
				}
				{
					int cc;
					cc = 0;
				}
				break;
		default: return(null);
		}
		return(hs);
	}
	
	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		Date d0 = rptCol.getDate("sdate");
		Date d1 = rptCol.getDate("edate");
		if(d0 == null) d0 = DateUtil.zeroDate;
		if(d1 == null) d1 = DateUtil.zeroDate;
		if(!d0.after(DateUtil.minDate) && !d1.after(DateUtil.minDate)) {
			return(new ReturnMsg(false,"Please Select Date Range"));
		}
		if(!d0.after(DateUtil.minDate)) {
			result.addCustomCondition(cumulatorComparable.getLabel() + " <= '" + DateUtil.toDateString(d1, "yyyy/mm/dd")+"'");
		} else {
			if(!d1.after(DateUtil.minDate)) {
				result.addCustomCondition(cumulatorComparable.getLabel() + " >= '" + DateUtil.toDateString(d0, "yyyy/mm/dd") + "'");
			} else {
				result.addCustomCondition(cumulatorComparable.getLabel() + " between '" + DateUtil.toDateString(d0, "yyyy/mm/dd") + "' and '" + DateUtil.toDateString(d1, "yyyy/mm/dd") + "'");
			}
		}
		/*
		 Will be added back using dynamic class  260309 DT
		if(result instanceof BiResultStockLedger){
			Cell loc = rptCol.testCell("location");
			if(loc != null) {
				((BiResultStockLedger) result).setLocation(setLedgerLocationByMode(loc.getInt(),result.getSelectUtil()));
			}
		}
		 */
		return(ReturnMsg.defaultOk);
	}
}
