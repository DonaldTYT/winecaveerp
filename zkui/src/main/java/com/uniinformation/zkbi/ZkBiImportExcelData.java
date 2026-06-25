package com.uniinformation.zkbi;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.MessageboxDlg;

import com.google.api.client.util.Lists;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class ZkBiImportExcelData {
   	private final static int MAX_IMPORT_COLUMNS = 256;
   	private final static String ADD_STATE_COLOR = "LightGreen";
   	private final static String UPDATE_STATE_COLOR = "LightSkyBlue";
   	private final static String FAIL_STATE_COLOR = "LightPink";
   	private final static String SKIP_STATE_COLOR = "Gold";
   	private final static String READ_EXCEL_ROWS_EVENT = "onReadExcelRows";
   	private final static String READ_EXCEL_DETAIL_ROWS_EVENT = "onReadExcelDetailRows";
   	private final static String UPDATE_RECORD_EVENT = "onUpdateRecord";
   	private final static String READ_EXCEL_FINISH_EVENT = "onReadExcelFinish";
   	private final static int AUTO_PROCEED_MSEC = 60000;

   	private MessageboxDlg pvDialog;
 	private GridHelper pvGrid;
    private Textbox pvSummaryText;
    private Row pvProgressBarRow;
    private Progressmeter pvProgressBar;
    private Button pvAbortButton;
    private Hlayout pvColorDesc;
    private Vlayout vl;
    private Button proceedButton, closeButton;
    private Checkbox cbAutoProceed;
    private Timer autoProceedTimer;

    private SessionHelper sessionHelper;
    private BiResult result;
    private Component targetComp;
    private Callback callback;

	private ExcelPoi exlpoi;
	//private boolean proceedMode; //update2023/05/29: Change the 2 phase import to 1 phase import.
	private boolean aborted;
	private Integer pkCol; //primary key column
	private String pkFieldName = "";
	private BiTable masterTable;
	private HashMap<Integer,BiColumn> impColHash = new HashMap <Integer,BiColumn> (); //key:excel column
	private HashMap<BiView,ImpDetailColumnRec> impDetailHash = new HashMap<BiView,ImpDetailColumnRec>();
	private int lastColumnNum;

    private int currRow = -1;
    private boolean alreadyBeginWork;
    private ReturnMsg rtnMsg;
	private int nAdded = 0;
	private int nUpdated = 0;
	private int nSkipped = 0;
//	private boolean translateToBig5 = true;
	
	private long startRunTime, totalEventTime;
    
    interface Callback {
    	void afterCloseDialog();
    }
    
    public ZkBiImportExcelData(SessionHelper sessionHelper, final BiResult result, final Component targetComp, final Callback callback) {
    	this.sessionHelper = sessionHelper;
    	this.result = result;
    	this.targetComp = targetComp;
    	this.callback = callback;
//    	if(result.getView().getSchema().autoTranslate) {
//    		translateToBig5 = false;
//    	}
	    Fileupload.get(new HashMap<String, Object>(), null, null, ".xls|.xlsx", 1, -1, false, new ZkBiEventListener <UploadEvent>(){
			@Override
			public void onZkBiEvent(UploadEvent event) throws Exception {
		        UniLog.log1("upload event catched");
   		        final org.zkoss.util.media.Media media = event.getMedia();
   		        if (media != null) {
   		        	try {
   		        		importData(media);
   		        	}
   		        	catch (Exception ex) {
                		UniLog.log(ex);
   						if (currRow >= 1)
   							setPvGridRowBkColor(currRow - 1, 1, "Fail");
                		if (pvDialog != null) {
                			rtnMsg = new ReturnMsg(ex);
                			echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
                		}
                		else
    						Messagebox.show("Failed to load at row " + currRow + " : " + ex.toString());
   		        	}
   		        }
   		        else
   		        	Messagebox.show("File Not Selected");
			}
	    });
    }
    
    private void importData(org.zkoss.util.media.Media media) throws Exception {
    	//load excel file
    	InputStream is = media.getStreamData();
   		try {
   			exlpoi = ExcelPoi.newExcelPoi(is,false);
      	} 
	    catch (Exception ex) {
	    	is.close();
			is = media.getStreamData();
			exlpoi = ExcelPoi.newExcelPoi(is,true);
		}
		exlpoi.excel_translate_Chinese(0);
		is.close();
		UniLog.log("Excel row count = " + exlpoi.getRowCount());

		//get excel header
		List <String> impColList = new ArrayList<String>();
		lastColumnNum = -1;
		int maxColumn = exlpoi.excel_getColumnCount(0);
		if (maxColumn > MAX_IMPORT_COLUMNS) maxColumn = MAX_IMPORT_COLUMNS;
		for(int i = 0;i < maxColumn;i++) {
			String colhdr = exlpoi.getStringValue(0, i);
			if(colhdr != null && !colhdr.trim().equals("")) {
				impColList.add(colhdr);
				lastColumnNum = i;
			} else impColList.add(null);
		}
		
		Vector<BiColumn> xv = result.getListColumns();

		//check primary key, get biColumn by excel header
		masterTable = result.getView().getTable();
		for(int j=0;j<impColList.size();j++) {
			BiColumn bl = null;
			String colHdr = impColList.get(j);
			if(colHdr != null) {
				bl = findBiColumnByHeader(result,colHdr);
			} else {
				//revision 5535: column with blank header will map according to the previouse column with non-blank header by column offset
				if(j > 0) {
					for(int k = j-1;k>=0;k--) {
						String ctrlCol = impColList.get(k);
						if(ctrlCol != null) {
							for(int l=0;l<xv.size();l++) {
								BiColumn ctrlbl = (BiColumn) xv.get(l);
								if(ctrlbl.getEngName().equals(ctrlCol) ||
										ctrlCol.equals( ZkBiTranslateHelper.getText(sessionHelper, ctrlbl.getCellFullName(), "LABEL", sessionHelper.getLabel(ctrlbl)))
										) {
									for(int m=k,n=l+1;m<j;m++,n++) {
										String ss = ((BiColumn) xv.get(n)).getEngName();
										if(ss != null && !ss.isEmpty()) {
											bl = null;
											break;
										}
										bl = (BiColumn) xv.get(n);
									}
									if(bl != null) break;
								}
							}
							break;
						}
					}
				}
			}
			if(bl != null && bl.isSkipImport()) bl=null;
			if(bl == null) {
				Vector<BiResult> srList = result.getSubLinks();
				if(srList != null) {
				for(BiResult sr : srList) {
					bl = findExportColumnByHeader(sr,colHdr);
					if(bl != null && !bl.isSkipImport()) {
						impColHash.put(new Integer(j),bl);
						if(impDetailHash.get(sr.getView()) == null) 
							impDetailHash.put(sr.getView(), null);
						break;
					} else {
						UniLog.log("skip unknown column " + j);
					}
				}
				}
			} else {
				impColHash.put(new Integer(j),bl);
				if(masterTable.getPrimaryKey() == null ||
						masterTable.getPrimaryKey().trim().equals("") ) {
					if(bl.getLabel().equals("serial_id")) {
						pkCol = new Integer(j);
						pkFieldName = masterTable.getSerialId() + " = ";
					}
				} else {
					if(bl.getField() != null && bl.getField().getName().equals(masterTable.getPrimaryKey())) {
						pkCol = new Integer(j);
						pkFieldName = bl.getLabel() + " = ";
					}
				}
			}
		}
		if (pkCol == null) {
			Messagebox.show("Primary Key Not In Import File, Cannot Import");
			return;
		}
		boolean b = false;
		for (BiColumn bc : impColHash.values()) {
			if (!bc.isNoEntry(sessionHelper) || !bc.isNoUpdate(sessionHelper)) {
				b = true;
				break;
			}
		}
		if (!b) {
			Messagebox.show("No Updatable Fields In import File, Cannot Import");
			return;
		}
		UniLog.log1("pkCol:%d, pkFieldName:%s, impColHash size:%d, impDetailHash size:%d", pkCol, pkFieldName, impColHash.size(), impDetailHash.size());

      	//build preview dialog
		pvSummaryText = new Textbox() {{
			setReadonly(true);
		}};
		pvProgressBar = new Progressmeter();
		pvAbortButton = new Button("Abort");
		pvAbortButton.setSclass("zkbi-deletebutton");
		pvAbortButton.setTooltiptext("Abort current action");
	    pvGrid = new GridHelper(lastColumnNum + 2) {{
	    	setVflex("1");
			setSclass("zkbi-da");
		}};
		pvColorDesc = new Hlayout() {{
			appendChild(new Div() {{
				appendChild(new Label("Import Data") {{
					setStyle("font-size:18px !important");
				}});
				setHflex("1");
			}});
			final String[] ss = new String[] {"Add", "Update", "Skip", "Fail"};
			final String[] cs = new String[] {ADD_STATE_COLOR, UPDATE_STATE_COLOR, SKIP_STATE_COLOR, FAIL_STATE_COLOR};
			for (int i = 0; i < ss.length; i++) {
				final int j = i;
				appendChild(new Hbox() {{
					appendChild(new Label(ss[j]));
					setAlign("center");
					setPack("center");
					setStyle("border:1px solid #87a8ba;width:40px;height:30px;background-color:" + cs[j]);
				}});
			}
			setHflex("1");
		}};
		pvGrid.getColumn(0).setLabel("#");
		pvGrid.getColumn(0).setHflex("min");
		pvGrid.getColumn(0).setAlign("right");
		for(int i = 0;i <= lastColumnNum;i++) {
			String colhdr = impColList.get(i);
			int gridCol = i + 1;
			pvGrid.getColumn(gridCol).setLabel(StringUtils.defaultString(colhdr));
			pvGrid.getColumn(gridCol).setHflex("min");
			BiColumn bc = impColHash.get(i);
			if (bc != null && (bc.getColumnType().equals("serial") 
					|| bc.getColumnType().equals("integer")
					|| bc.getColumnType().equals("float")
					|| bc.getColumnType().equals("money"))) {
				pvGrid.getColumn(gridCol).setAlign("right");
			}
		}
		vl = new Vlayout() {{
			appendChild(pvColorDesc);
			appendChild(pvGrid);
			appendChild(new GridHelper(2) {{
				getColumn(0).setHflex("min");
				getColumn(1).setHflex("1");
				pvSummaryText.setHflex("1");
				addRow(new Label("Status"), pvSummaryText);
				addRow(new Label(), new Hlayout() {{
					pvProgressBar.setHflex("1");
					pvProgressBar.setStyle("margin-top:5px");
					appendChild(pvProgressBar);
					appendChild(pvAbortButton);
				}});
				pvProgressBarRow = getLastRow();
			}});
		}};
		
		pvDialog = buildMessageboxDlg("Data Import Window", vl, 
			new Messagebox.Button[] {Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
			new String[] {"Proceed", "Close"},
			targetComp.getRoot(), new ZkBiEventListener<Messagebox.ClickEvent>() {
				@Override
				public void onZkBiEvent(ClickEvent event) throws Exception {
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
					case OK:
						cancelAutoProceedTimer();
						cbAutoProceed.setDisabled(true);
						closeButton.setDisabled(true);
						proceedButton.setLabel("Proceed");
						proceedButton.setTooltiptext("Confirm data import. You can enable Auto Procced by toggle on Auto Proceed button");

						/*for (Component c : pvGrid.getRows().getChildren().toArray(new Component[0]))
							pvGrid.getRows().removeChild(c);
						proceedMode = true;*/
						proceedButton.setDisabled(true);
						/*pvSummaryText.setValue("");
						pvProgressBar.setValue(0);
						pvAbortButton.setDisabled(false);
						pvProgressBarRow.setVisible(true);
						Clients.resize(pvDialog);

						currRow = -1;
						rtnMsg = null;
						nAdded = 0;
						nUpdated = 0;
						nSkipped = 0;

						result.beginWork();
						alreadyBeginWork = true;
						startRunTime = System.currentTimeMillis();
						totalEventTime = 0;
						echoEvent(READ_EXCEL_ROWS_EVENT, pvDialog, new org.zkoss.json.JSONObject() {{
							put("excelRow", 1);
						}});*/
						if (alreadyBeginWork) {
							try {
								result.commitWork();
								pvSummaryText.setValue(String.format("Run status: Committed     Add:%d  Updated:%d  Skip:%d", nAdded, nUpdated, nSkipped));
								ZkUtil.msg("Data imported successfully");
							}
							catch (Exception ex) {
								pvSummaryText.setValue(String.format("Run status: Commit Fail     Add:%d  Update:%d  Skip:%d %s", nAdded, nUpdated, nSkipped, ex.getMessage()));
								ZkUtil.msg("Unable to import data. " + ex.getMessage());
							}
						}
						closeButton.setDisabled(false);
						event.stopPropagation();
						break;
					default:
						doClickCloseButton(event);
						break;
					}
				}
		});
		pvDialog.addEventListener(Events.ON_CLOSE, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				if (closeButton == null || closeButton.isDisabled())
					event.stopPropagation();
				else
					doClickCloseButton(event);
			}
		});
		pvDialog.setWidth("80%");
		pvDialog.setHeight("80%");
		pvDialog.doHighlighted();


		UniLog.log1("exlpoi count:%d", exlpoi.getRowCount());
		//setup event
		pvDialog.addEventListener(READ_EXCEL_ROWS_EVENT, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
				final int i = (Integer)jobj.get("excelRow");
				final long startTime = System.currentTimeMillis();
				UniLog.log1("READ_EXCEL_ROWS_EVENT excelRow i:%d", i);
				if (i >= exlpoi.getRowCount() || aborted) {
					echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
					logt("READ_EXCEL_ROWS_EVENT 0", startTime);
					return;
				}

				try {
					currRow = i;
					boolean isUpdate = false;
					for (BiView bv : impDetailHash.keySet())
						impDetailHash.put(bv, null);
	
					//add row to grid
					String errMsg = addPvGridRow(i);
					if (errMsg != null)
						throw new Exception(errMsg);
	
					//try query record by primary key
					Object ko = getPoiObjectByBiCol(impColHash.get(pkCol),i,pkCol, impColHash.get(pkCol).isAutoTranslate());
					if(ko != null && (!(ko instanceof String) || !((String) ko).equals(""))) {
						result.clearCondition();
						String s = pkFieldName;
						if(ko instanceof String) {
//							s += "'" + ko.toString() + "'";
							s += "'" + ((String) ko).replace("'", "\\'") + "'";
						} else if( ko instanceof java.util.Date) {
							s += "'" + DateUtil.toDateString((java.util.Date) ko,"yyyy/mm/dd") + "'";
						} else s += ko.toString();
						result.addCondition(new VectorUtil().addElement(masterTable).toVector(), s);
						ReturnMsg rtn = result.query(false);
						if(rtn != null && !rtn.getStatus()) {
							rtnMsg = new ReturnMsg(false,"Error : row " + i+ "  load record error " + (rtn == null ? "": rtn.getMsg()));
							echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
							logt("READ_EXCEL_ROWS_EVENT 1 ", startTime);
							return;
						}
						if(result.getRowCount() == 1) {
							result.loadOneRecV(0);
							result.fetchOneRecV(0);
							isUpdate = true;
						} else if( result.getRowCount() > 0){
							rtnMsg = new ReturnMsg(false,"Error : row " + i+ "  primary key not unique");
							echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
							logt("READ_EXCEL_ROWS_EVENT 1", startTime);
							return;
						} else {
							if(pkFieldName.startsWith("serial_id")) {
								if(ko.toString().trim() == "" ||
									ko.toString().trim() == "0" 
									) {
									UniLog.log("Fetch import record by serial_id failed, do add record");
								} else {
									UniLog.log("Fetch import record failed, skipped");
									setPvGridRowBkColor(i - 1, 1, "Skip");
									nSkipped++;
									echoEvent(READ_EXCEL_ROWS_EVENT, pvDialog, new org.zkoss.json.JSONObject() {{
										put("excelRow", i + 1);
									}});
									logt("READ_EXCEL_ROWS_EVENT 2", startTime);
									return;
								}
							} else {
								UniLog.log("Fetch import record by key failed, do add record");
							}
						}
					}
	
					if (isUpdate == false)
						result.clearCurrentRec();
	
					//try set field 
					boolean hasValue = false;
					boolean hasDetail = false;
					for(Integer xc : impColHash.keySet()) {
						BiColumn bc = impColHash.get(xc);
						
						if( (isUpdate && !bc.isNoUpdate(sessionHelper)) ||
							(!isUpdate && !bc.isNoEntry(sessionHelper)) ) {
							Object xo = getPoiObjectByBiCol(bc,i,xc.intValue(),bc.isAutoTranslate());
							if(bc.getView() == result.getView()) {
								Cell ce = result.getCell(bc.getLabel());
			    				if( isUpdate ||
			    					(xo != null && (!(xo instanceof String) || !((String) xo).equals("")))
			    					) {
								if(ce.getItemPropertyInterface() != null) {
									String ss = xo.toString();
									AbstractGetItemProperty gipi = ce.getItemPropertyInterface();
									boolean matched = false;
									for(int k=0;k<gipi.getRowCount();k++) {
										Object o = gipi.getRow(k);
										if(gipi.getString(o).equals(ss)) {
											ce.set(o);
											matched = true;
											break;
										}
									}
									if(!matched) {
										rtnMsg = new ReturnMsg(false,"Import Failed on row " + i + " column " + ce.getCellLabel() + " invalid ");
										echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
										logt("READ_EXCEL_ROWS_EVENT 3", startTime);
										return;
									}
								} else if(xo instanceof String && ce.getItemList() != null && ce.getType() == Cell.VTYPE_INT ) {
									int idx = (ce.getItemList()).indexOf(xo);
									ce.set(idx);
								} else  {
									//revision 6397: fix excel import that cell with no content is now updated to the record. 
									if(xo == null) {
										switch(ce.getType()) {
										case Cell.VTYPE_DATE : ce.set(DateUtil.zeroDate); break;
										case Cell.VTYPE_DOUBLE : ce.set(0.0); break;
										case Cell.VTYPE_INT: ce.set(0); break;
										case Cell.VTYPE_BOOLEAN: ce.set(false); break;
										default : ce.set(""); break;
										}
									} else
										ce.set(xo);
								}
			    				} else {
			    					UniLog.log("Excel Cell is null or blank in add mode, skipped");
			    				}
			    				//revision 6385: fix excel import bug that record with all blank values will not be imported and terminate the import process.
								if(isUpdate || (xo != null && (!(xo instanceof String) || !((String) xo).equals("")))) {
		    						hasValue = true;
			    				}
							} else {
			
								if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
									pvProgressBar.setValue(i * 100 / (exlpoi.getRowCount() - 1));
									//pvSummaryText.setValue(String.format("%s run status: Processing     %d/%d", proceedMode ? "Real" : "Test", i, exlpoi.getRowCount() - 1));
									pvSummaryText.setValue(String.format("Run status: Processing     %d/%d", i, exlpoi.getRowCount() - 1));
									hasDetail = true;
								}
			
							}
						}				    						
						
					}
	
					if (hasValue) {
						if (hasDetail) {
							org.zkoss.json.JSONObject jo = new org.zkoss.json.JSONObject();
							jo.put("masterExcelRow", i);
							jo.put("detailExcelRow", i + 1);
							//jo.put("updateRows", 0);
							jo.put("updateRows", 1);
							jo.put("isUpdate", isUpdate);
							echoEvent(READ_EXCEL_DETAIL_ROWS_EVENT, pvDialog, jo);
							logt("READ_EXCEL_ROWS_EVENT 4", startTime);
							return;
						}
	
						org.zkoss.json.JSONObject jo = new org.zkoss.json.JSONObject();
						jo.put("excelRow", i);
						jo.put("updateRows", 1);
						jo.put("isUpdate", isUpdate);
						jo.put("nextExcelRow", i + 1);
						echoEvent(UPDATE_RECORD_EVENT, pvDialog, jo);
					}
					else {
						echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
					}
				}
				catch (Exception ex) {
					setPvGridRowBkColor(i - 1, 1, "Fail");
					rtnMsg = new ReturnMsg(ex);
					echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
				}
				logt("READ_EXCEL_ROWS_EVENT 5", startTime);
			}
		});
		pvDialog.addEventListener(READ_EXCEL_DETAIL_ROWS_EVENT, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
				final int i = (Integer)jobj.get("masterExcelRow");
				final int k = (Integer)jobj.get("detailExcelRow");
				final int n = (Integer)jobj.get("updateRows");
				final boolean isUpdate = (Boolean)jobj.get("isUpdate");
				UniLog.log1("READ_EXCEL_DETAIL_ROWS_EVENT excelRow i:%d, k:%d, n:%d, isUpdate:%b", i, k, n, isUpdate);
				final long startTime = System.currentTimeMillis();
				if (aborted) {
					echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
					logt("READ_EXCEL_DETAIL_ROWS_EVENT 0", startTime);
					return;
				}
				if (k >= exlpoi.getRowCount()) {
					org.zkoss.json.JSONObject jo = new org.zkoss.json.JSONObject();
					jo.put("excelRow", i);
					jo.put("updateRows", n);
					jo.put("isUpdate", isUpdate);
					jo.put("nextExcelRow", k);
					echoEvent(UPDATE_RECORD_EVENT, pvDialog, jo);
					logt("READ_EXCEL_DETAIL_ROWS_EVENT 1", startTime);
					return;
				}

				try {
					boolean hasMaster = false;
					for(Integer xc : impColHash.keySet()) {
						BiColumn bc = impColHash.get(xc);
						Object xo = getPoiObjectByBiCol(bc,k,xc.intValue(),bc.isAutoTranslate());
						if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
							if((isUpdate && !bc.isNoUpdate(sessionHelper)) ||
								(!isUpdate && !bc.isNoEntry(sessionHelper)) ) {
								if(bc.getView() == result.getView()) {
									hasMaster = true;
									break;
								} 
							}
						}
					}
					if (hasMaster) {
						org.zkoss.json.JSONObject jo = new org.zkoss.json.JSONObject();
						jo.put("excelRow", i);
						jo.put("updateRows", n);
						jo.put("isUpdate", isUpdate);
						jo.put("nextExcelRow", k);
						echoEvent(UPDATE_RECORD_EVENT, pvDialog, jo);
						logt("READ_EXCEL_DETAIL_ROWS_EVENT 2", startTime);
						return;
					}
	
					//add row to grid
					String errMsg = addPvGridRow(k);
					if (errMsg != null)
						throw new Exception(errMsg);
	
					for(Integer xc : impColHash.keySet()) {
						BiColumn bc = impColHash.get(xc);
						Object xo = getPoiObjectByBiCol(bc,k,xc.intValue(),bc.isAutoTranslate());
						if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
							pvProgressBar.setValue(k * 100 / (exlpoi.getRowCount() - 1));
							//pvSummaryText.setValue(String.format("%s run status: Processing     %d/%d", proceedMode ? "Real" : "Test", k, exlpoi.getRowCount() - 1));
							pvSummaryText.setValue(String.format("Run status: Processing     %d/%d", k, exlpoi.getRowCount() - 1));
							//hasDetail = true;
						}
						
					}
	
					org.zkoss.json.JSONObject jo = new org.zkoss.json.JSONObject();
					jo.put("masterExcelRow", i);
					jo.put("detailExcelRow", k + 1);
					jo.put("updateRows", n + 1);
					jo.put("isUpdate", isUpdate);
					echoEvent(READ_EXCEL_DETAIL_ROWS_EVENT, pvDialog, jo);
				}
				catch (Exception ex) {
					setPvGridRowBkColor(k - 1, 1, "Fail");
					rtnMsg = new ReturnMsg(ex);
					echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
				}
				logt("READ_EXCEL_DETAIL_ROWS_EVENT 3", startTime);
			}
		});
		pvDialog.addEventListener(UPDATE_RECORD_EVENT, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
				final int i = (Integer)jobj.get("excelRow");
				final int n = (Integer)jobj.get("updateRows");
				final boolean isUpdate = (Boolean)jobj.get("isUpdate");
				final int nextI = (Integer)jobj.get("nextExcelRow");
				UniLog.log1("UPDATE_RECORD_EVENT excelRow i:%d, n:%d, isUpdate:%b", i, n, isUpdate);
				final long startTime = System.currentTimeMillis();
				if (aborted) {
					echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
					logt("UPDATE_RECORD_EVENT 0", startTime);
					return;
				}

				if(isUpdate) {
					//update record
					UniLog.log("Excel Import Update Record " + i);
					rtnMsg = result.updateCurrent();
					if(rtnMsg != null && !rtnMsg.getStatus()) {
						setPvGridRowBkColor(i - 1, n, "Fail");
						echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
						logt("UPDATE_RECORD_EVENT 1", startTime);
						return;
					}
					setPvGridRowBkColor(i - 1, n, "Update");
					nUpdated++;
				} else {
					//insert record
					UniLog.log("Excel Import Insert Record " + i);
					rtnMsg = result.addCurrent();
					if(rtnMsg != null && !rtnMsg.getStatus()) {
						setPvGridRowBkColor(i - 1, n, "Fail");
						echoEvent(READ_EXCEL_FINISH_EVENT, pvDialog, null);
						logt("UPDATE_RECORD_EVENT 2", startTime);
						return;
					}
					setPvGridRowBkColor(i - 1, n, "Add");
					nAdded++;
				}

				echoEvent(READ_EXCEL_ROWS_EVENT, pvDialog, new org.zkoss.json.JSONObject() {{
					//put("excelRow", i + 1);
					put("excelRow", nextI);
				}});
				logt("UPDATE_RECORD_EVENT 3", startTime);
			}
		});
		pvDialog.addEventListener(READ_EXCEL_FINISH_EVENT, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				//UniLog.log1("alreadyBeginWork:%b, aborted:%b, proceedMode:%b", alreadyBeginWork, aborted, proceedMode);
				UniLog.log1("alreadyBeginWork:%b, aborted:%b", alreadyBeginWork, aborted);
				if (alreadyBeginWork) {
					/*if (aborted || !proceedMode)
						result.rollbackWork();
					else
						result.commitWork();*/
					if (aborted)
						result.rollbackWork();
					UniLog.log1("total run time:%d, total event time:%d", System.currentTimeMillis() - startRunTime, totalEventTime);
					if (rtnMsg != null && !rtnMsg.getStatus()) {
						//pvSummaryText.setValue(String.format("%s run status: Fail     Add:%d  Update:%d  Skip:%d %s", proceedMode ? "Real" : "Test", nAdded, nUpdated, nSkipped, rtnMsg.getMsg()));
						pvSummaryText.setValue(String.format("Run status: Fail     Add:%d  Update:%d  Skip:%d %s", nAdded, nUpdated, nSkipped, rtnMsg.getMsg()));
						ZkUtil.msg("Unable to import data. " + rtnMsg.getMsg());
						proceedButton.setDisabled(true);
						cbAutoProceed.setDisabled(true);
						closeButton.setDisabled(false);
					}
					else if (!aborted) {
						//result.getStatusJson(true);  //for debug
						//pvSummaryText.setValue(String.format("%s run status: OK     Add:%d  Updated:%d  Skip:%d", proceedMode ? "Real" : "Test", nAdded, nUpdated, nSkipped));
						pvSummaryText.setValue(String.format("Run status: OK Waiting for confirmation     Add:%d  Updated:%d  Skip:%d", nAdded, nUpdated, nSkipped));
						//proceedButton.setDisabled(proceedMode);
						proceedButton.setDisabled(false);
						
						//When import preview complete and Auto Proceed button is ON, it will trigger a 60second countdown.
						//if (!proceedMode) {
							closeButton.setDisabled(cbAutoProceed.isChecked());
				    		cancelAutoProceedTimer();
							autoProceedTimer = new Timer();
							autoProceedTimer.setPage(pvDialog.getPage());
							autoProceedTimer.setDelay(1000);
							autoProceedTimer.setRepeats(true);
							autoProceedTimer.addEventListener(Events.ON_TIMER, new EventListener<Event>(){
								long startTime;
								boolean closedCountDown = !cbAutoProceed.isChecked();
								@Override
								public void onEvent(Event event) throws Exception {
									if (cbAutoProceed.isChecked()) {
										if (closedCountDown) {
											closedCountDown = false;
											startTime = 0;
											closeButton.setDisabled(true);
										}
										if (startTime == 0) {
											startTime = System.currentTimeMillis();
											proceedButton.setLabel(String.format("Proceed (%ds)", (AUTO_PROCEED_MSEC / 1000)));
											proceedButton.setTooltiptext("You can abort Auto Proceed by toggle off Auto Proceed or Click Proceed to commit immediately");
										}
										else {
											long countdownTime = AUTO_PROCEED_MSEC - (System.currentTimeMillis() - startTime);
											if (countdownTime <= 0) {
												cancelAutoProceedTimer();
												Events.echoEvent(Events.ON_CLICK, proceedButton, null);
											}
											else {
												proceedButton.setLabel(String.format("Proceed (%ds)", countdownTime / 1000));
												proceedButton.setTooltiptext("You can abort Auto Proceed by toggle off Auto Proceed or click Proceed to commit immediately");
											}
										}
									}
									else {
										if (!closedCountDown) {
											closedCountDown = true;
											proceedButton.setLabel("Proceed");
											proceedButton.setTooltiptext("Confirm data import. You can enable Auto Procced by toggle on Auto Proceed button");
											closeButton.setDisabled(false);
										}
									}
								}
							});
							autoProceedTimer.setRunning(true);
						/*}
						else
							closeButton.setDisabled(false);*/
					}
					else {
						//pvSummaryText.setValue(String.format("%s run status: Aborted     Add:%d  Updated:%d  Skip:%d", proceedMode ? "Real" : "Test", nAdded, nUpdated, nSkipped));
						pvSummaryText.setValue(String.format("Run status: Aborted     Add:%d  Updated:%d  Skip:%d", nAdded, nUpdated, nSkipped));
						proceedButton.setDisabled(true);
						cbAutoProceed.setDisabled(true);
						closeButton.setDisabled(false);
					}
				}
				else if (rtnMsg != null && !rtnMsg.getStatus()) {
					//pvSummaryText.setValue(String.format("%s run status: Fail     Add:%d  Update:%d  Skip:%d %s", proceedMode ? "Real" : "Test", nAdded, nUpdated, nSkipped, rtnMsg.getMsg()));
					pvSummaryText.setValue(String.format("Run status: Fail     Add:%d  Update:%d  Skip:%d %s", nAdded, nUpdated, nSkipped, rtnMsg.getMsg()));
					proceedButton.setDisabled(true);
					cbAutoProceed.setDisabled(true);
					closeButton.setDisabled(false);
				}
				pvAbortButton.setDisabled(true);
				//pvAbortButton.setVisible(false);
				//pvProgressBar.setVisible(false);
				pvProgressBarRow.setVisible(false);
				Clients.resize(pvDialog);
			}
		});

		//find buttons
		for (Component cbtn : pvDialog.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), "Proceed")) {
				proceedButton = btn;
				proceedButton.setTooltiptext("Confirm data import. You can enable Auto Procced by toggle on Auto Proceed button");
			}
			else if (StringUtils.equals(btn.getLabel(), "Close"))
				closeButton = btn;
		}
		proceedButton.setDisabled(true);
		proceedButton.setIconSclass("z-icon-check");
		closeButton.setDisabled(true);
		pvAbortButton.setIconSclass("z-icon-times");
		pvAbortButton.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				aborted = true;
				pvAbortButton.setDisabled(true);
			}
		});
		cbAutoProceed.setDisabled(false);
		
		//start read excel data
		result.beginWork();
		alreadyBeginWork = true;
		startRunTime = System.currentTimeMillis();
		totalEventTime = 0;
		echoEvent(READ_EXCEL_ROWS_EVENT, pvDialog, new org.zkoss.json.JSONObject() {{
			put("excelRow", 1);
		}});
		
		
		/*
		for (int i=1;i<exlpoi.getRowCount();) {
			int n = 1;
			currRow = i;
			boolean isUpdate = false;
			for(BiView bv : impDetailHash.keySet()) {
				impDetailHash.put(bv, null);
			}

			//add row to grid
			addPvGridRow(i);

			//try query record by primary key
			Object ko = getPoiObjectByBiCol(impColHash.get(pkCol),i,pkCol);
			if(ko != null && (!(ko instanceof String) || !((String) ko).equals(""))) {
				result.clearCondition();
				String s = pkFieldName;
				if(ko instanceof String) {
					s += "'" + ko.toString() + "'";
				} else if( ko instanceof java.util.Date) {
					s += "'" + DateUtil.toDateString((java.util.Date) ko,"yyyy/mm/dd") + "'";
				} else s += ko.toString();
				result.addCondition(new VectorUtil().addElement(masterTable).toVector(), s);
				result.query(false);
				if(result.getRowCount() == 1) {
					result.loadOneRecV(0);
					result.fetchOneRecV(0);
					isUpdate = true;
				} else if( result.getRowCount() > 0){
					rtnMsg = new ReturnMsg(false,"Error : row " + i+ "  primary key not unique");
					break;
				} else {
					if(pkFieldName.startsWith("serial_id")) {
						if(ko.toString().trim() == "" ||
							ko.toString().trim() == "0" 
								) {
							UniLog.log("Fetch import record by serial_id failed, do add record");
						} else {
							UniLog.log("Fetch import record failed, skipped");
							setPvGridRowBkColor(i - 1, n, "Skip");
							nSkipped++;
							i += n;
							continue;
						}
					} else {
						UniLog.log("Fetch import record by key failed, do add record");
					}
				}
			}

			if(isUpdate == false) {
				result.clearCurrentRec();
			}

			//try set field 
			boolean hasValue = false;
			boolean hasDetail = false;
			for(Integer xc : impColHash.keySet()) {
				BiColumn bc = impColHash.get(xc);
				
				if( (isUpdate && !bc.isNoUpdate()) ||
					(!isUpdate && !bc.isNoEntry()) ) {
					Object xo = getPoiObjectByBiCol(bc,i,xc.intValue());
					if(bc.getView() == result.getView()) {
						Cell ce = result.getCell(bc.getLabel());
						if(ce.getItemPropertyInterface() != null) {
							String ss = xo.toString();
							AbstractGetItemProperty gipi = ce.getItemPropertyInterface();
							boolean matched = false;
							for(int k=0;k<gipi.getRowCount();k++) {
								Object o = gipi.getRow(k);
								if(gipi.getString(o).equals(ss)) {
									ce.set(o);
									matched = true;
									break;
								}
							}
							if(!matched) {
								rtnMsg = new ReturnMsg(false,"Import Failed on row " + i + " column " + ce.getCellLabel() + " invalid ");
								break;
							}
						} else if(xo instanceof String && ce.getItemList() != null && ce.getType() == Cell.VTYPE_INT ) {
							int idx = (ce.getItemList()).indexOf(xo);
							ce.set(idx);
						} else  {
							ce.set(xo);
						}
						if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
							hasValue = true;
						}
					} else {

						if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
							hasDetail = true;
						}

					}
				}				    						
				
			}
			
			if(hasDetail) {
				for (int k=i+1;k<exlpoi.getRowCount();k++) {
					boolean hasMaster = false;
					for(Integer xc : impColHash.keySet()) {
						BiColumn bc = impColHash.get(xc);
						Object xo = getPoiObjectByBiCol(bc,k,xc.intValue());
						if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
							if(
									(isUpdate && !bc.isNoUpdate()) ||
									(!isUpdate && !bc.isNoEntry()) ) {
								if(bc.getView() == result.getView()) {
									hasMaster = true;
									break;
								} 

							}
						}
					}
					if(hasMaster) break;

					//add row to grid
					addPvGridRow(k);

					for(Integer xc : impColHash.keySet()) {
						BiColumn bc = impColHash.get(xc);
						Object xo = getPoiObjectByBiCol(bc,k,xc.intValue());
						if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
							//hasDetail = true;
						}
						
					}
					n++;
				}
			}

			if(hasValue) {
				if(isUpdate) {
					//update record
					UniLog.log("Excel Import Update Record " + i);
					rtnMsg = result.updateCurrent();
					if(rtnMsg != null && !rtnMsg.getStatus()) {
						setPvGridRowBkColor(i - 1, n, "Fail");
						break;
					}
					setPvGridRowBkColor(i - 1, n, "Update");
					nUpdated++;
				} else {
					//insert record
					UniLog.log("Excel Import Insert Record " + i);
					rtnMsg = result.addCurrent();
					if(rtnMsg != null && !rtnMsg.getStatus()) {
						setPvGridRowBkColor(i - 1, n, "Fail");
						break;
					}
					setPvGridRowBkColor(i - 1, n, "Add");
					nAdded++;
				}
			} 
			else {
				//end read excel
				break;
			}
			
			i+=n;
		}

		if(rtnMsg != null && !rtnMsg.getStatus()) {
			String strMsg = String.format("Test run status: Fail     Add:%d  Update:%d  Skip:%d\n%s", nAdded, nUpdated, nSkipped, rtnMsg.getMsg());
			pvSummaryLabel.setValue(strMsg);
			class CancelEventListener<T extends Event> extends ZkBiEventListener<T> {
				@Override
				public void onZkBiEvent(T event) throws Exception {
					UniLog.log("rollback work due to import error");
					if (event instanceof Messagebox.ClickEvent) {
						if (((Messagebox.ClickEvent)event).getButton() == null)
							return;
					}
					if(importAsSingle) result.rollbackWork();
					//refresh(result,rootComp,(MultiSortMap)mMultiSortMap.clone(),false);
					callback.afterCloseDialog();
				}
			}
			//pvDialog = ZkUtil.buildSimpleMessageboxDlg("Import Error", vl, 
			//	new Messagebox.Button[] {Messagebox.Button.CANCEL}, 
			//	new String[] {"Close"},
			//	rootComp, new CancelEventListener<Messagebox.ClickEvent>());
			//pvDialog.addEventListener(Events.ON_CLOSE, new CancelEventListener<Event>());
		}
		else {
			String strMsg = String.format("Test run status: OK     Add:%d  Updated:%d  Skip:%d", nAdded, nUpdated, nSkipped);
			pvSummaryLabel.setValue(strMsg);
			class CompleteEventListener<T extends Event> extends ZkBiEventListener<T> {
				@Override
				public void onZkBiEvent(T event) throws Exception {
					if (event instanceof Messagebox.ClickEvent) {
						Messagebox.Button btn = ((Messagebox.ClickEvent)event).getButton();
						if (btn == null)
							return;
						if (btn == Messagebox.Button.OK) {
							UniLog.log("Confirm OK commit work");
							try {
								result.commitWork();
							} catch (Exception ex) {
								UniLog.log(ex);
								Messagebox.show(ex.toString());
							}
							//refresh(result,rootComp,(MultiSortMap)mMultiSortMap.clone(),false);
							callback.afterCloseDialog();
							return;
						}
					}
					UniLog.log("Confirm Canceled rollback work");
					result.rollbackWork();
					//refresh(result,rootComp,(MultiSortMap)mMultiSortMap.clone(),false);
 					callback.afterCloseDialog();
				}
			}
			//pvDialog = ZkUtil.buildSimpleMessageboxDlg("Confirm Save Changes ?", vl, 
			//	new Messagebox.Button[] {Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
			//	new String[] {"Proceed", "Cancel"},
			//	rootComp, new CompleteEventListener<Messagebox.ClickEvent>());
			//pvDialog.addEventListener(Events.ON_CLOSE, new CompleteEventListener<Event>());
		}

		//pvDialog.setWidth("80%");
		//pvDialog.setHeight("80%");
		//pvDialog.doHighlighted();*/
    }

    private boolean processImportDetailColumn(HashMap<BiView,ImpDetailColumnRec> impDetailHash,BiColumn bc,BiResult result,Object xo) throws CellException {
    	boolean hasDetail = false;
		if(impDetailHash.containsKey(bc.getView())) {
			BiResult sr = result.getSubLink(bc.getView().getName());
			ImpDetailColumnRec idcr = impDetailHash.get(bc.getView());
			BiCellCollection col = null;
			if(idcr != null) col = impDetailHash.get(bc.getView()).col;
		    if(idcr == null) {
		    	idcr = new ImpDetailColumnRec();
		    	impDetailHash.put(sr.getView(), idcr);
		    	int nn = sr.getRowCount();
		    	for(int j =0;j<nn;j++) {
		    		Object o = sr.getTrStatObj(j);
		    		if(o != null) sr.markDelete(o, true);
		    	}
		    }

			if(col == null) {
				if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
			    	col = sr.newRowCollection();
			    	ReturnMsg rtn = sr.addSubRecord(col, -1,"");
			    	idcr.setCollection(col);
			    	hasDetail = true;

					idcr.setUpdCols(bc.getLabel());
			    	updateImpostcell(col.getCell(bc.getLabel()),xo);
				}
			} else {
				if(idcr.contains(bc.getLabel())) {
			    	idcr.clear();
			    	col = null;
				}
				if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {	
					if(col == null) {
						col = sr.newRowCollection();
						ReturnMsg rtn = sr.addSubRecord(col, -1,"");
						idcr.setCollection(col);
					}
				}
				if(col != null) {
					idcr.setUpdCols(bc.getLabel());
			    	updateImpostcell(col.getCell(bc.getLabel()),xo);
				}
			}
		}
		return(hasDetail);
    }

    private void updateImpostcell(Cell p_cell,Object p_object) throws CellException {
    	if(p_object == null) {
    		switch(p_cell.getType()) {
    		case Cell.VTYPE_BOOLEAN : p_cell.set(false); break;
    		case Cell.VTYPE_DATE : 
    		case Cell.VTYPE_DATETIME : 
    						p_cell.set(DateUtil.zeroDate); break;
    		case Cell.VTYPE_DOUBLE :
    						p_cell.set(0.0); break;
    		case Cell.VTYPE_INT:
    						p_cell.set(0); break;
    		case Cell.VTYPE_STRING :
    						p_cell.set(""); break;
    		default : throw new CellException("column " + p_cell.getCellLabel() + " invalid column type " + p_cell.getType());
    		}
    		return;
    	}
		if(p_cell.getItemPropertyInterface() != null) {
			  String ss = p_object.toString();
			  AbstractGetItemProperty gipi = p_cell.getItemPropertyInterface();
			  boolean matched = false;
			  for(int k=0;k<gipi.getRowCount();k++) {
			    	Object o = gipi.getRow(k);
			    	if(gipi.getString(o).equals(ss)) {
			    		p_cell.set(o);
			    		matched = true;
			    		break;
			    	}
			  }
			  if(!matched) {
			    	throw new CellException("column " + p_cell.getCellLabel() + " invalid ");
			   }
		} else {
			if(p_cell.getType() == Cell.VTYPE_INT) {
				if(p_cell.getItemList() != null) {
					int idx = p_cell.getItemList().indexOf(p_object);
					if(idx >= 0) {
						p_cell.set(idx);
					} else p_cell.set(0);
					return;
				} 
			}
			p_cell.set(p_object);
		}
    }

    private BiColumn findBiColumnByHeader(BiResult result,String colHdr) {
//		Vector xv = result.getListColumns();	
		List<BiColumn> xv = result.getExportColumns();		
		for(int i=0;i<xv.size();i++) {
			BiColumn bl = (BiColumn) xv.get(i);
			if(bl.getEngName().equals(colHdr) ||
					colHdr.equals( ZkBiTranslateHelper.getText(sessionHelper, bl.getCellFullName(), "LABEL", sessionHelper.getLabel(bl)))
					) {
				return(bl);
			}
		}
		return(null);
    }

    private BiColumn findExportColumnByHeader(BiResult result,String colHdr) {
    	if(colHdr == null) return(null);
		List xv = result.getExportColumns();	
		for(int i=0;i<xv.size();i++) {
			BiColumn bl = (BiColumn) xv.get(i);
			if(bl.getEngName().equals(colHdr) ||
					colHdr.equals( ZkBiTranslateHelper.getText(sessionHelper, bl.getCellFullName(), "LABEL", sessionHelper.getLabel(bl)))
					) {
				return(bl);
			}
		}
		return(null);
    }

    private Object getPoiObjectByBiCol(BiColumn bc,int row,int col,boolean autoTranslate) throws Exception {
    	Object xo = null;
		if(bc != null && bc.getColumnType().equals("date")) {
			xo = exlpoi.getDateValue(row, col);
			if(xo == null && exlpoi.getStringValue(row,col) != null) {
				throw new Exception ("Input Error, Date Column "+ bc.getLabel() + " contains non-date value");
			}
		} 
		else if(bc != null && bc.getColumnType().equals("serial")) {
			xo = exlpoi.getIntegerValue(row, col);
		} 
		else if(bc != null && bc.getColumnType().equals("integer")) {
			xo = exlpoi.getIntegerValue(row, col);
		} 
		else if(bc != null && bc.getColumnType().equals("float")) {
			xo = exlpoi.getDoubleValue(row, col);
		} 
		else if(bc != null && bc.getColumnType().equals("money")) {
			xo = exlpoi.getDoubleValue(row, col);
		} 
		else {
    		if(autoTranslate)  {
				if( sessionHelper.getLHLang().equals("SCHN")) {
					xo = ChineseConvert.convertAuto2Gnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
				} else {
					xo = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
				}
    		}  else
    			xo = stripNonPrintable(exlpoi.getStringValue(row, col));
			
//    		if(translateToBig5)
//    			xo = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
//    		else
//    			xo = stripNonPrintable(exlpoi.getStringValue(row, col));
//			xo = exlpoi.getStringValue(row, col);
		}
		return(xo);
    }

    private String getPoiStringByBiCol(BiColumn bc,int row,int col,boolean translateToBig5) throws Exception {
		if(bc != null && bc.getColumnType().equals("datetime")) {
			Date xo = exlpoi.getDateValue(row, col);
			if(xo == null && exlpoi.getStringValue(row,col) != null) {
				throw new Exception ("Input Error, Date Column "+ bc.getLabel() + " contains non-date value");
			}
			return DateUtil.dateToDateTimeStr(xo,"yyyy/MM/dd HH:mm:ss");
		} 
		else if(bc != null && bc.getColumnType().equals("date")) {
			Date xo = exlpoi.getDateValue(row, col);
			if(xo == null && exlpoi.getStringValue(row,col) != null) {
				throw new Exception ("Input Error, Date Column "+ bc.getLabel() + " contains non-date value");
			}
			return DateUtil.dateToDateTimeStr(xo,"yyyy/MM/dd");
		} 
		else if(bc != null && bc.getColumnType().equals("time")) {
			Date xo = exlpoi.getDateValue(row, col);
			if(xo == null && exlpoi.getStringValue(row,col) != null) {
				throw new Exception ("Input Error, Date Column "+ bc.getLabel() + " contains non-date value");
			}
			return DateUtil.dateToDateTimeStr(xo,"HH:mm:ss");
		} 
		else if(bc != null && bc.getColumnType().equals("serial")) {
			Integer xo = exlpoi.getIntegerValue(row, col);
			//return String.valueOf(xo);
			return xo != null ? String.valueOf(xo) : "";
		} 
		else if(bc != null && bc.getColumnType().equals("integer")) {
			Integer xo = exlpoi.getIntegerValue(row, col);
			//return String.valueOf(xo);
			return xo != null ? String.valueOf(xo) : "";
		} 
		else if(bc != null && bc.getColumnType().equals("float")) {
			Double xo = exlpoi.getDoubleValue(row, col);
			if (xo == null)
				return "";
			if (StringUtils.isNotBlank(bc.getFormat()))
				return new DecimalFormat(bc.getFormat()).format(xo);
			return String.format("%.2f", xo);
		} 
		else if(bc != null && bc.getColumnType().equals("money")) {
			Double xo = exlpoi.getDoubleValue(row, col);
			if (xo == null)
				return "";
			if (StringUtils.isNotBlank(bc.getFormat()))
				return new DecimalFormat(bc.getFormat()).format(xo);
			return String.format("%.2f", xo);
		} 
		else 
    		if(translateToBig5)
    			return ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
    		else
    			return stripNonPrintable(exlpoi.getStringValue(row, col));
    }

	private String stripNonPrintable(String p_s)
	{
		StringBuffer sb = new StringBuffer();
		if(p_s == null) return(null);
		char[] ca = p_s.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if(ca[i] < 32) continue;
			if(Character.isWhitespace(ca[i])) sb.append(' '); else sb.append(ca[i]);
		}
		return(StringUtils.stripEnd(sb.toString()," "));
	}

 	private void setPvGridRowBkColor(int startRow, int rowCount, String state) {
 		String color = null;
        if (state.equals("Add"))
        	color = ADD_STATE_COLOR;
        else if (state.equals("Update"))
        	color = UPDATE_STATE_COLOR;
        else if (state.equals("Fail"))
        	color = FAIL_STATE_COLOR;
        else if (state.equals("Skip"))
        	color = SKIP_STATE_COLOR;
        if (color != null) {
        	for (int l = startRow; l < startRow + rowCount; l++) {
        		if (l < pvGrid.getRows().getChildren().size())
        			pvGrid.getRow(l).setStyle("background-color:" + color +" !important");
        	}
        	pvProgressBar.setValue((startRow + rowCount) * 100 / (exlpoi.getRowCount() - 1));
			//pvSummaryText.setValue(String.format("%s run status: Processing     %d/%d", proceedMode ? "Real" : "Test", startRow + rowCount, exlpoi.getRowCount() - 1));
			pvSummaryText.setValue(String.format("Run status: Processing     %d/%d", startRow + rowCount, exlpoi.getRowCount() - 1));
        }
 	}
 	
 	private String addPvGridRow(int row) throws Exception {
 		String errMsg = null;
		Label[] cs = new Label[lastColumnNum + 2];
		cs[0] = new Label(String.valueOf(row));
		for (int i = 0; i <= lastColumnNum; i++) {
			Object o = null;
			try {
				o = getPoiStringByBiCol(impColHash.get(i),row,i,false);
			}
			catch (Exception e) {
				if (errMsg == null)
					errMsg = e.getMessage();
			}
			if(o != null) cs[i + 1] = new Label(o.toString()); else cs[i+1] = new Label("");
			cs[i + 1].setPre(true);
		}
		pvGrid.addRow(cs);
		if (pvGrid.getRows().getChildren().size() % 10 == 0) //resize function too slow
			Clients.resize(pvGrid);
		Clients.scrollIntoView(pvGrid.getLastRow());
		return errMsg;
 	}
 	
 	private void doClickCloseButton(Event event) {
 		if (!proceedButton.isDisabled()) {
			ZkBiMsgbox.show(ZkBiMsgbox.Type.question, "Are you sure leave? You have unsaved changes", new String[]{"Yes","No"},new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					if (StringUtils.equals(btn.getName(), "Yes")) {
						cancelAutoProceedTimer();
						pvDialog.detach();
						callback.afterCloseDialog();
					}
				}
			});				
 			event.stopPropagation();
 		}
 		else
 			callback.afterCloseDialog();
 	}
 	
 	private void logt(String msg, long startTime, String...ps) {
 		long eventTime = System.currentTimeMillis() - startTime;
 		UniLog.log1(msg + " event use time:" + eventTime, ps);
 		totalEventTime += eventTime;
 	}
 	
 	private void echoEvent(String name, Component target, Object data) {
 		Events.echoEvent(name, target, data);
 	}

    private MessageboxDlg buildMessageboxDlg(String title, final HtmlBasedComponent child, Messagebox.Button[] buttons, String[] buttonsLabel, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
		MessageboxDlg dlg = new MessageboxDlg();
		dlg.setTitle(title);
		dlg.setBorder("normal");
		dlg.setClosable(true);
    	dlg.setParent(parent);
    	child.setVflex("1");
    	child.setHflex("1");
    	final Div div = new Div();
    	div.setId("buttons");
    	div.setStyle("display:flex;display:-webkit-flex;"
    			+ "flex-wrap:wrap;-webkit-wrap:wrap;"
    			+ "justify-content:center;-webkit-justify-content:center;"
    			+ "padding:0 5px 10px 0;");
    	div.setAttribute("button.sclass", "zkbi-messagebox-button");
    	div.setHflex("1");
    	cbAutoProceed = new Checkbox("Auto Proceed");
    	cbAutoProceed.setTooltiptext(String.format("After preview, it proceed automatically in %d seconds", (AUTO_PROCEED_MSEC / 1000)));
    	cbAutoProceed.setMold("switch");
    	cbAutoProceed.setStyle("margin-top:10px");
    	cbAutoProceed.setChecked(true);
    	cbAutoProceed.setDisabled(true);
    	final Hlayout hl = new Hlayout() {{
    		appendChild(div);
    		appendChild(cbAutoProceed);
    	}};
    	dlg.appendChild(new Vbox() {{
    		appendChild(child);
    		appendChild(hl);
    		setHflex("1");
    		setVflex("1");
    	}});
    	dlg.setButtons(buttons, buttonsLabel);
    	dlg.setEventListener(eventListener);
    	return dlg;
    }
    
    private void cancelAutoProceedTimer() {
		if (autoProceedTimer != null) {
			autoProceedTimer.setRunning(false);
			autoProceedTimer.detach();
			autoProceedTimer = null;
		}
    }

    private class ImpDetailColumnRec {
    	BiCellCollection col;
    	HashSet<String> updCols;
    	void clear() {
    		col = null;
    		updCols = null;
    	}
    	void setCollection(BiCellCollection p_col) {
    		col = p_col;
    		updCols = new HashSet<String>();
    	}
    	void setUpdCols(String p_updCols) {
    		updCols.add(p_updCols);
    	}
    	boolean contains(String p_updCols) {
    		return(updCols == null ? false : updCols.contains(p_updCols));
    	}
    }
}
