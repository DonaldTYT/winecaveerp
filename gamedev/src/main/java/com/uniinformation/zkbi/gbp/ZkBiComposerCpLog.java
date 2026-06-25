package com.uniinformation.zkbi.gbp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.impl.MessageboxDlg;

import com.drew.tools.FileUtil;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerCpLog extends ZkBiComposerBase  {
	//Window trWin = null;
	protected void setupAddButton(final BiResult p_result)
	{
		super.setupAddButton(p_result);
		final Button btnAdd = new ZkBiButton();
		btnAdd.setLabel("Tracking Report");
		btnAdd.setId("btTrackingReport");
		btnAdd.addEventListener("onClick", new EventListener() {
			public void onEvent(Event event) throws Exception {
				cpLogDialog(p_result);
			}
		});
		actionBar.appendChild(btnAdd); 
	}
	/*
	public void buildBrowserWindow(final BiResult p_result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		super.buildBrowserWindow(p_result, comp, p_sortIdx, p_sortDesc);
	}
	*/
	/**
	 * 
	 */
	private void cpLogDialog(final BiResult p_result) {
		final List<Pair<String, Integer>> valList = new ArrayList<Pair<String, Integer>>();
		Grid grid = new Grid();
		grid.setWidth("100%");
		grid.appendChild(new Columns(){{
			appendChild(new Column(){{setHflex("1");}});
			appendChild(new Column(){{setHflex("2");}});
		}});
		Rows rows = new Rows();
		rows.setParent(grid);
		final Datebox startDate = new Datebox();
		startDate.setFormat("yyyy/MM/dd");
		final Datebox endDate = new Datebox();
		endDate.setFormat("yyyy/MM/dd");
		final Textbox inFolders = new Textbox();
		inFolders.setPlaceholder("DraftSet_379_130_jt7gbpf3\nDraftSet_379_9_jt7g8q8l\n...");
		inFolders.setMultiline(true); 
		inFolders.setRows(20); 
		inFolders.setWidth("100%");
		final Textbox jobName = new Textbox();
		jobName.setPlaceholder("Please enter job name");
		
		rows.appendChild(new Row(){{ 
			appendChild(new Label("Start Date:")); 
			appendChild(startDate);
		}});
		rows.appendChild(new Row(){{ 
			appendChild(new Label("End Date:")); 
			appendChild(endDate);
		}});
		rows.appendChild(new Row(){{ 
			appendChild(new Label("In Folder List:"){{ this.setPre(true);}});
			appendChild(inFolders);
		}});
		rows.appendChild(new Row(){{ 
			appendChild(new Label("Job Name:"));
			appendChild(jobName);
		}});

		MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Checkpoint Tracking Report", 
			grid, new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, masterWin,
			new EventListener<Messagebox.ClickEvent>(){
				@Override
				public void onEvent(ClickEvent event) throws Exception {
					//prepare data for construct wherecl
					long startUnixtime = -1;
					if (startDate.getValue() != null){
						startUnixtime = DateUtil.dateStart(startDate.getValue().getTime()) / 1000;
					}
					long endUnixtime = -1;
					if (endDate.getValue() != null){
						endUnixtime = DateUtil.dateEnd(endDate.getValue().getTime()) / 1000;
					}
					ArrayList<String >inFolderList = new ArrayList<String>();
					if (StringUtils.isNotBlank(inFolders.getValue())){
						String sa[] = StringUtils.split(inFolders.getValue(), "\n,; ");
						if (sa != null && sa.length > 0){
							inFolderList.addAll(Arrays.asList(sa));
						}
					}
					
					UniLog.log1("start unixtime: " + startUnixtime);
					UniLog.log1("end unixtime: " + endUnixtime);
					UniLog.log1("inFolderList: " + inFolderList.toString());
					UniLog.log1("jobName: " + jobName.getValue().trim());

					
					if (event.getButton() == null){
						return;
					}
					switch (event.getButton()) {
					case OK:
						Wherecl wherecl = buildWhereCl(startUnixtime, endUnixtime, inFolderList, jobName.getValue());
						//dummy call to test the wherecl
						TableRec tr = p_result.getSelectUtil().getQueryResult("select count(*) from cplog", wherecl);
						UniLog.log1("count(*) = %d", tr.getField(0));
						//TODO: call cheng code to generate a excel report
						//cpTrackingReport("123456", p_result, wherecl);
						cpTrackingReport(p_result, wherecl);
						break;
					case CANCEL:
						UniLog.log1("got cancel");
						break;
					default:
						break;
					}
				}
			}
		);
		dlg.doHighlighted();
		dlg.setWidth("50%");
	}
	private void cpTrackingReport(BiResult br, Wherecl p_wherecl) {
       	synchronized(exportTimerEvent) {
       		try {
       			exportTimerEvent.zkBiTimerEventInterface = new ExportToExcel("tracking_report", br, p_wherecl);
       		} 
       		catch (Exception ex ) {
       			UniLog.log(ex);
       		}
       	}
	}
	private Wherecl buildWhereCl(long startTime, long endTime, ArrayList<String> inFolderList, String jobName){
		Wherecl wherecl = new Wherecl();
		if (startTime > 0){
			wherecl.andUniop("time", ">=", startTime);
		}
		if (endTime > 0){
			wherecl.andUniop("time", "<=", endTime);
		}
		if (StringUtils.isNotBlank(jobName)) {
			wherecl.andUniop("jobname", "=", jobName);
		}
		Wherecl inFolderWherecl = new Wherecl();
		for (String inFolder : inFolderList){
			if (StringUtils.isNotEmpty(inFolder)){
				inFolderWherecl.orUniop("infoldername", "=", inFolder);
			}
		}
		wherecl.andWherecl(inFolderWherecl);
		//wherecl = wherecl.stripAnd();
		UniLog.log("wherecl: " + wherecl.toString());
		return wherecl;
	}
	class ExportToExcel implements ZkBiTimerEventInterface {
    	ExcelPoi jxf;
    	//String jobId;
    	BiResult biResult;
    	Wherecl wherecl;
   		String outFileName;
   		Integer[] colStyles = new Integer[16];
   		Integer[] colModeStyles = new Integer[3];
   		Map<Object, DeckRow> deckRowMap = new HashMap<Object, DeckRow>();
   		List<DeckRow> deckRowList = new ArrayList<DeckRow>();
		int row;
   		class DeckRow {
   			int row;
   			int lastestTime;
   			String jobName;
   			Map<Integer, String> cpModeMap = new LinkedHashMap<Integer, String>(){{
   				put(0, null);
   				put(1, null);
   				put(3, null);
   				put(4, null);
   				put(5, null);
   			}};
   		}
    	ExportToExcel(String p_outFileName, BiResult p_result, Wherecl p_wherecl) throws IOException {
    		outFileName = p_outFileName;
    		//jobId = p_jobId;
    		biResult = p_result;
    		wherecl = p_wherecl;
   			InputStream is = sessionHelper.openResourceAsStream("/template/tracking_report_template.xlsx");
			jxf = ExcelPoi.newExcelPoi(is,true); 
   			is.close();
   			for (int i = 0; i < colStyles.length; i++) {
				try {
					jxf.excel_setStringValue(1, i, "");
					colStyles[i] = jxf.excel_getCellStyleIdx(1, i);
				} catch (Exception e) {
					UniLog.log("getCellStyle " + i + "," + e.toString());
					e.printStackTrace();
				}
   			}
   			for (int i = 0; i < colModeStyles.length; i++) {
				try {
					jxf.excel_setStringValue(2 + i, 10, "");
					colModeStyles[i] = jxf.excel_getCellStyleIdx(2 + i, 10);
					jxf.excel_setCellStyle(2 + i, 10, colStyles[10]);
				} catch (Exception e) {
					UniLog.log("getCellStyle " + i + "," + e.toString());
					e.printStackTrace();
				}
   			}
    		exportTimer.setDelay(200);
    		exportTimer.setRepeats(true);
    		exportTimer.setRunning(true);
    		progressName.setValue("Processing ...");
    		progressMeter.setValue(0);
    		progressMeter.invalidate();
    		progressPanel.doModal();
    	}
    	private void setCellStyle(int row, int col, Integer modeStyleIndex) {
    		if (colStyles.length > col && colStyles[col] != null) {
				try {
					jxf.excel_setCellStyle(row, col, modeStyleIndex != null ? colModeStyles[modeStyleIndex] : colStyles[col]);
				} catch (Exception e) {
					UniLog.log("setCellStyle " + col + "," + e.toString());
					e.printStackTrace();
				}
    		}
    	}
    	private void setCellValue(int row, int col, String value) throws Exception {
    		setCellValue(row, col, value, null);
    	}
    	private void setCellValue(int row, int col, String value, Integer modeStyleIndex) throws Exception {
			jxf.excel_setStringValue(row, col, value);
			setCellStyle(row, col, modeStyleIndex);
    	}
    	private void setCellValue(int row, int col, int value) throws Exception {
    		setCellValue(row, col, value, null);
    	}
    	private void setCellValue(int row, int col, int value, Integer modeStyleIndex) throws Exception {
			jxf.excel_setNumericValue(row, col, value);
			setCellStyle(row, col, modeStyleIndex);
    	}
    	
    	//andrew230111 handle long for gbp checkpoint report
    	private void setCellValue(int row, int col, long value) throws Exception {
    		setCellValue(row, col, value, null);
    	}
    	private void setCellValue(int row, int col, long value, Integer modeStyleIndex) throws Exception {
			jxf.excel_setNumericValue(row, col, value);
			setCellStyle(row, col, modeStyleIndex);
    	}
    	
    	private void setCellValue(int row, int col, Date value) throws Exception {
    		setCellValue(row, col, value, null);
    	}
    	private void setCellValue(int row, int col, Date value, Integer modeStyleIndex) throws Exception {
			jxf.excel_setDateValue(row, col, value);
			setCellStyle(row, col, modeStyleIndex);
    	}
    	private void setModeCellValue(int row, int col, String value) throws Exception {
    		if (value.equals("CHECK"))
    			setCellValue(row, col, "OK", 0);
    		else if (value.equals("VOID"))
    			setCellValue(row, col, "\u5831\u5ee2", 1);
    		else
    			setCellValue(row, col, "X", 2);
    	}
		@Override
		public void onTimerFired() {
			try {
				UniLog.log1("heapSize:HAHA1:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
				TableRec tr = biResult.getSelectUtil().getQueryResult("select * from cplog", 
						new Wherecl().appendString(" cpid = 0 and result not like 'FAIL%' ")
									.appendString(wherecl.toWhereclString())
									.setOrderby("time,utime"));
				UniLog.log("cpid 0 count:" + tr.getRecordCount());
				UniLog.log1("heapSize:HAHA2:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
				
				row = 1;
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					appendRow(tr);
				}
				UniLog.log1("heapSize:HAHA3:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
				UniLog.log("deckRowList count:" + deckRowList.size());
				tr = biResult.getSelectUtil().getQueryResult("select * from cplog", 
					new Wherecl().appendString(" cpid in (1,3,4) and finstatus <> 'IN_PROGRESS' and result not like 'FAIL%' ")
								.appendString(wherecl.toWhereclString())
								.setOrderby("cpid,time,utime"));
				UniLog.log("cpid 1 - 4 count:" + tr.getRecordCount());
				UniLog.log1("heapSize:HAHA4:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					appendRow(tr);
				}
				UniLog.log("deckRowList count:" + deckRowList.size() + ",free memory:" + Runtime.getRuntime().freeMemory());
				UniLog.log1("heapSize:HAHA5:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
				tr = biResult.getSelectUtil().getQueryResult("select * from cplog", 
					new Wherecl().appendString(" cpid = 5 and result not like 'FAIL%' ")
								.appendString(wherecl.toWhereclString())
								.setOrderby("time,utime"));
				UniLog.log("cpid 5 count:" + tr.getRecordCount());
				UniLog.log1("heapSize:HAHA6:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					appendRow(tr);
				}
				UniLog.log("deckRowList count:" + deckRowList.size());
				UniLog.log1("heapSize:HAHA7:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
				for (DeckRow deckRow : deckRowList) {
					Date date = new Date(deckRow.lastestTime * 1000L);
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					setCellValue(deckRow.row, 2, date);
					setCellValue(deckRow.row, 3, cal.getTime());
					setCellValue(deckRow.row, 4, deckRow.jobName);
					String modeResult = "--";
					int i = 0;
					for (Map.Entry<Integer, String> entry : deckRow.cpModeMap.entrySet()) {
						int cpid = entry.getKey();
						String cpMode = entry.getValue();
						if (StringUtils.isNotBlank(cpMode)) {
							setModeCellValue(deckRow.row, 10 + i, cpMode);
							if (cpid == 5 && cpMode.equals("CHECK"))
								modeResult = "CHECK";
							else if (cpMode.equals("VOID"))
								modeResult = "VOID";
						} else
							setModeCellValue(deckRow.row, 10 + i, "--");
						i++;
					}
					setModeCellValue(deckRow.row, 15, modeResult);
				}
				UniLog.log1("heapSize:HAHA8:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
			} catch (Exception e) {
				e.printStackTrace();
			}
    		try {
    			ByteArrayOutputStream bos = new ByteArrayOutputStream();
				UniLog.log1("heapSize:HAHA9:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
    		    jxf.writeWorkBook(bos);
				UniLog.log1("heapSize:HAHA10:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
    		   	Filedownload.save(bos.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outFileName);
				UniLog.log1("heapSize:HAHA11:%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()));
    		} catch (Exception ex) {
    			UniLog.log(ex);
    		}
    		synchronized(exportTimerEvent) {
    			exportTimerEvent.zkBiTimerEventInterface = null;
    		}
    		exportTimer.setRunning(false);
    		progressPanel.setVisible(false);
		}
		private void appendRow(TableRec tr) throws Exception {
			int cpid = tr.getFieldInt("cpid");
			int time = tr.getFieldInt("time");
			String mode = tr.getFieldString("mode");
			//int deckid = tr.getFieldInt("deckid");
			long deckid = tr.getFieldLong("deckid");  //andrew230111 handle long deckid
			String deckcode = tr.getFieldString("deckcode");
			String infilename = tr.getFieldString("infilename");
			String infoldername = tr.getFieldString("infoldername");
			String groupname = tr.getFieldString("groupname");
			String jobName = tr.getFieldString("jobname");
			DeckRow deckRow = null;
			if (deckid > 0 && deckRowMap.containsKey(deckid))
				deckRow = deckRowMap.get(deckid);
			else if (StringUtils.isNotBlank(deckcode) && deckRowMap.containsKey(deckcode))
				deckRow = deckRowMap.get(deckcode);
			if (deckRow == null) {
				Date date = new Date(time * 1000L);
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				setCellValue(row, 0, date);
				setCellValue(row, 1, cal.getTime());
				//setCellValue(row, 4, jobName);
				setCellValue(row, 5, infoldername);
				setCellValue(row, 6, infilename);
				setCellValue(row, 7, deckcode);
				setCellValue(row, 8, "" + deckid);  //andrew230111 excel convert long deckid to string. excel has rounding issue for long number
				setCellValue(row, 9, groupname);
				deckRow = new DeckRow();
				deckRow.row = row++;
				deckRowList.add(deckRow);
				if (deckid > 0)
					deckRowMap.put(deckid, deckRow);
				if (StringUtils.isNotBlank(deckcode))
					deckRowMap.put(deckcode, deckRow);
			}
			if (StringUtils.isNotBlank(jobName))
				deckRow.jobName = jobName;
			deckRow.lastestTime = Math.max(time, deckRow.lastestTime);
			deckRow.cpModeMap.put(cpid, cpid == 0 ? "CHECK" : mode);
		}
		@Override
		public void onCancelClicked() {
    		UniLog.log("Export to Excel Cancelled");
    		synchronized(exportTimerEvent) {
    			exportTimerEvent.zkBiTimerEventInterface = null;
    		}
    		exportTimer.setRunning(false);
    		progressPanel.setVisible(false);
		}
    }
}
