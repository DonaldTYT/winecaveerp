package com.uniinformation.zkbi.wc;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.birt.ScriptedDataSetEventHander;
import com.uniinformation.cell.Cell;
import com.uniinformation.utils.BIRTUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Filedownload;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiField;
import com.uniinformation.bicore.BiReportInterface;

public class StockMovementLedgerBiActionHandler extends BiActionHandler {
	
	public interface OnReportInit {
		void onInit(StockMovementLedgerBiActionHandler p_hdr);
	}
	final static public int OUTPUT_PDF = 0;
	final static public int OUTPUT_Excel  = 1;
	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public boolean isShowBreakDown() {
		return showBreakDown;
	}

	public void setShowBreakDown(boolean showBreakDown) {
		this.showBreakDown = showBreakDown;
	}

	public int getOutputType() {
		return outputType;
	}

	public void setOutputType(int outputType) {
		this.outputType = outputType;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	
	public void setGroupColumns(HashSet<String> p_groupColumns) {
		groupColumns = p_groupColumns;
	}
	Date fromDate;
	Date toDate;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	String rptDesignBreakdown = "/com/uniinformation/birt/StockLedgerBreakdown3.rptdesign";
	String rptDesignSummary = "/com/uniinformation/birt/StockLedger3.rptdesign";
	List<Map<String, Object>> dsList = null;
	boolean showBreakDown = false;
	int outputType = OUTPUT_PDF;
	String subTitle;
	Component rootWin;
	String outputFileName;
	OnReportInit onReportInit;
	BiResult br;
	String icodeCol = null;
	Wherecl selectWhere = null;
	int recLimit = 10000;
	HashSet<String> groupColumns = null;
	public StockMovementLedgerBiActionHandler(ZkBiComposerBase p_bibase,BiResult p_br,Component p_root,OnReportInit p_init) {
		super(p_bibase);
		br = p_br;
		rootWin = p_root;
		onReportInit = p_init;
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		dsList = new ArrayList<Map<String, Object>>();
		try {
			selectWhere = br.conditionToWhereCl();
			if(selectWhere == null) selectWhere = new Wherecl();
			selectWhere.andUniop("pds_rcvqty", ">", 0);
			selectWhere.appendString(" and stmd_tdtype not in ('KI','KO') ");
			selectWhere.appendString(" and pds_irg = st_irg and or_org = pds_org and stmd_irg = pds_irg and stmd_org = pds_org and stm_mrg = stmd_mrg");
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		onReportInit.onInit(this);
		return (ReturnMsg.defaultOk);
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			Wherecl wherecl = new Wherecl().andWherecl(selectWhere);
			for(String colLabel : groupColumns) {
				BiField bf = br.getView().getColumnByLabel(colLabel).getField();
				if(bf != null) wherecl.andUniop(bf.getName(),"=",br.getCell(colLabel).getObject());
			}
			wherecl.appendString(" order by stmd_date,stmd_tdtype limit " + recLimit);
			TableRec tr = p_result.getSelectUtil().getQueryResult("select * from stock,podetstatus,orders,stmovd,stmov ",
						wherecl
				);
			if( tr.getRecordCount() >= recLimit){
				return(new ReturnMsg(false,"Error Detail Record exists Limit"));
			}
			if( tr.getRecordCount() > 0){
    			Object[] vals = p_result.getAggregateValues(p_recIdx);
				double begBal = (Double) vals[0];
				double closeBal = 0;
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					Map<String, Object> m = new LinkedHashMap<String, Object>();
					m.put(BiReportInterface.ledgerColumns.st_icode.toString(), br.getCellString("st_icode").replace("/", "_"));
					m.put(BiReportInterface.ledgerColumns.st_iname.toString(), br.getCellString("st_iname"));
					m.put(BiReportInterface.ledgerColumns.stm_ref1.toString(), tr.getFieldString("stm_ref1"));
					m.put(BiReportInterface.ledgerColumns.stm_ref2.toString(), tr.getFieldString("stmd_tdtype"));
					m.put(BiReportInterface.ledgerColumns.lg_date.toString(), tr.getFieldDate("stmd_date"));
					m.put(BiReportInterface.ledgerColumns.stmd_openbal.toString(), begBal);
					double qty = tr.getFieldDouble("stmd_qty");
					if(qty > 0)
						m.put(BiReportInterface.ledgerColumns.stmd_inqty.toString(), qty);
					else
						m.put(BiReportInterface.ledgerColumns.stmd_outqty.toString(), qty);
					closeBal = begBal + qty;
					m.put(BiReportInterface.ledgerColumns.stmd_closebal.toString(), closeBal);
					begBal = closeBal;
					dsList.add(m);
				}
			} else {
				Map<String, Object> m = new LinkedHashMap<String, Object>();
				m.put(BiReportInterface.ledgerColumns.st_icode.toString(), br.getCellString("st_icode").replace("/", "_"));
				m.put(BiReportInterface.ledgerColumns.st_iname.toString(), br.getCellString("st_iname"));
    			Double[] vals = (Double []) p_result.getAggregateValues(p_recIdx);
				m.put(BiReportInterface.ledgerColumns.stmd_openbal.toString(), vals[0]);
				m.put(BiReportInterface.ledgerColumns.stmd_inqty.toString(), vals[1]);
				m.put(BiReportInterface.ledgerColumns.stmd_outqty.toString(), vals[2]);
				m.put(BiReportInterface.ledgerColumns.stmd_closebal.toString(), vals[3]);
				dsList.add(m);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return (ReturnMsg.defaultOk);
	}
	public void setICodeColumn(String p_icodeCol) {
		icodeCol = p_icodeCol;
		
	}
	public void setSelectWherecl(Wherecl p_where) {
		selectWhere = p_where;
	}
	public void setRecordLimit(int p_limit) {
		recLimit = p_limit;
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		// TODO Auto-generated method stub
		synchronized(BIRTUtil.getObjLock()){
		try {
			InputStream rptDesign;

			BIRTUtil.initEngine();
			IReportEngine engine = BIRTUtil.getReportEngine();

			//1. load design
			if(showBreakDown) {
				rptDesign = getClass().getResourceAsStream(rptDesignBreakdown);
			} else {
				rptDesign = getClass().getResourceAsStream(rptDesignSummary);
			}
			IReportRunnable design = engine.openReportDesign(rptDesign);
			rptDesign.close();

			//2. init rptdesign and json
			ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();

			//3. First, add User Property in stockledger.rptdesign, and then set its value
			designHandle.setProperty("Start date", fromDate);
			designHandle.setProperty("End date", toDate);
			{
				if (fromDate.getTime() < DateUtil.minDate.getTime())
					designHandle.setProperty("SubTitle", String.format("Date upto ", dateFormat.format(fromDate)));
				else if (toDate.getTime() >= DateUtil.maxDate.getTime())
					designHandle.setProperty("SubTitle", String.format("Date from %s", dateFormat.format(toDate)));
				else
					designHandle.setProperty("SubTitle", String.format("Date Between %s and %s", dateFormat.format(fromDate), dateFormat.format(toDate)));
			}
			designHandle.setProperty("SubTitle", subTitle);
//			if(br != null) {
//				BiResult sr = br.getSubLink(detailView);
//				for(BiReportInterface.ledgerColumns k : fdHash.keySet()) {
//					String lb = fdHash.get(k);
//					BiColumn bc = br.getColumnByLabel(lb);
//					if(bc == null) bc = sr.getColumnByLabel(lb);
//					if(bc != null) {
//						designHandle.setProperty("hdr_"+k.toString(),bc.getEngName());
//					}
//				}
//			}

			
			//setup subtitle1
			/*designHandle.setProperty("SubTitle1", "condition");
			MasterPageHandle materPageHandle = designHandle.findMasterPage("Simple MasterPage");
			materPageHandle.setProperty("headerHeight", "0.88in");*/

			//4. add record list to data set
			final String dataSetName = "Data Set 1";
			ScriptedDataSetEventHander.clearRecordList();
			ScriptedDataSetEventHander.addRecordList(dataSetName, dsList);

			if(outputType == OUTPUT_Excel) {
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
				File f = File.createTempFile("ledgerrpt", ".xlsx",fd);
				String outputXlsFilePath = f.getPath();
				BIRTUtil.createRunAndRenderTask(design, outputXlsFilePath);
				FileInputStream fis = new FileInputStream(outputXlsFilePath);
    			Filedownload.save(fis, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outputFileName+".xlsx");
    			f.delete();
			} else {
				//5. create and run task
				String outputFilePath = "/tmp/birt/tmppdf" + Thread.currentThread().getId() + "_" + System.currentTimeMillis();
				UniLog.log1("outputFilePath:%s, dsList size:%d", outputFilePath, dsList.size());
				BIRTUtil.createRunAndRenderTask(design, outputFilePath);
				//6. show pdf
				File file = new File(outputFilePath);
				ZkUtil.showPdfDialog(rootWin, br.getSessionHelper(), FileUtils.readFileToByteArray(file), outputFileName+".pdf");
				file.delete();
			}
			
			UniLog.log1("done");
		} catch (Exception ex) {
			UniLog.log(ex);
		} finally {
			dsList = null;
		}
		}
		return null;
	}
}
