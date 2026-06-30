package com.uniinformation.zkbi;


// InputField actually is QueryField, InputFieldList is QueryFieldList, should change to proper naming later

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.text.View;

import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.collections.BidiMap;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.util.CellReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.zkforge.ckez.CKeditor;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zhtml.I;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.ext.ScopeListener;
import org.zkoss.zk.ui.metainfo.ComponentDefinition;
import org.zkoss.zul.*;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Window.Mode;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.MessageboxDlg;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.utils.*;
import com.uniinformation.utils.ConditionPresets.ConditionField;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.utils.whereclpar.Expression;
import com.uniinformation.utils.whereclpar.Parser;
import com.uniinformation.utils.whereclpar.Variable;
import com.uniinformation.webcore.*;
import com.uniinformation.zkbi.ZkBiHotkeyHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkbi.reports.ZkBiComposerAggregateReport;
import com.uniinformation.zkcomp.S2Listbox;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkcomp.ZkBiButtonGroup;
import com.uniinformation.zkf.ZkForm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.kyoko.common.NumberUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.kyoko.parser.excelformula.CellPositionInterface;
import com.kyoko.parser.excelformula.ColumnTranslateInterface;
import com.kyoko.parser.excelformula.ExcelCellRef;
import com.uniinformation.bicore.*;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.bicore.AggregateOrPivotHeader.APLabel;
import com.uniinformation.bicore.bischema.BiResultExcelSheet;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.bicore.bischema.ExcelColToBiColumn;
import com.uniinformation.bicore.bischema.ExcelWorkSheetCache;
import com.uniinformation.birt.ReportGenerate;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.IgnoreValue;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.jx.*;
import com.uniinformation.jx.zk.*;
import com.uniinformation.jx.zk.ZkJxQueryInput.EventListenerCallback;
import com.uniinformation.jxapp.JxZkBiBase;
 
public class ZkBiComposerBase extends ZkBiComposerView implements Composer<Component>, ZkBiSearchInterface, ZkBiHotkeyInterface, JxZkBiBaseCallback {
	final static long maxClickSelectGap = 50;
   	protected final static int defaultSortIdx = 0;
   	protected final static boolean defaultSortDesc = true;
   	final static String QOPER_PREFIX = "qcb0";
   	final static String QINP0_PREFIX = "qinp0";
   	final static String QINP1_PREFIX = "qinp1";
   	final static String QINP2_PREFIX = "qinp2";
   	public final static int MAX_IMPORT_COLUMNS=256;
   	
   	public final static String IMG_DELETE = "images/DeleteRed24.png";
   	public final static String IMG_UPDATE = "images/UpdateGreen24.png";
   	public final static String IMG_SINGLESELECT = "images/single24.png";
   	public final static String IMG_MULTISELECT = "images/multiple24.png";
   	public final static String IMG_PULLDOWN= "images/pulldown50real.png";
   	public final static String IMG_RELOAD= "images/reload50real.png";
   	
   	String zkbiExtraInfo = null;
   	
    int baseHeaderCount = 0;
    int auxHeaderCount = 0;
	protected InputFieldsList inputFieldsList = new InputFieldsList();
	protected XulElement masterWin;
   	Spinner ibLimit;
    //Hbox actionConfirmBar;
    Div actionConfirmBar;
    Hbox batchActionConfirmBar;
    //protected Hbox actionBar;
    protected Hlayout actionBar;
    Hbox confirmBar;
    protected Button btnDelete;
	protected JxZkBiBase detailForm;
	String viewid;
	String helpid;
	String presetid;
	private String pageid;
	String action;
	String queryCondition;
	protected String title;
    protected ConditionPresets mConditionPresets;
    int lastSelectIdx = -1;
    int currSelectIdxXX = -1;
   	protected Listbox listbox;
	String listboxHeightSclass = ""; //current listbox height sclass 
	int listboxPct;
   	
   	protected Component zkbiListTop = null;
   	protected HtmlBasedComponent zkbiListPanel = null;
   	protected Div zkbiEmbedSearchDiv;
    protected EventListener itemClickListener = null;
    
    Toolbarbutton btnCustCondition;
   	protected Toolbarbutton btnHelp = null;
   	protected Vbox bottomPanelVbox = null;
  	Hbox shortcutBar= null;
  	protected Hbox queryBar= null;
   	protected Box headerBox;
	Div condDiv = null;
   	protected ListModelList listModelList;
   	protected Listbox conditionPresetListbox;
   	Checkbox conditionPresetIsDefault;
    Label browserWindowId;
    
    protected Boolean hasAUDColumn = null;
    protected boolean hasDetailButton = true;
   	boolean hideDeleted = false;
   	boolean showModified= false;
   	protected boolean multiSelect = false;
   	
   	protected Hbox batchActionBar = null;
   	Listbox batchActionFieldList;
   	ListModelList batchActionFieldListModel;
   	Textbox batchActionFieldTextbox = null;
   	Datebox batchActionFieldDatebox = null;
   	Div batchActionFieldDiv = null;
   	Button btnBatchActionSet = null;
	protected Toolbarbutton batchModeToggleButton;
    
	String biResultClass = null;

	protected Window progressPanel;
	protected Label progressName;
	protected Progressmeter progressMeter ;
	Button progressCancel;
	
    protected Timer exportTimer = new Timer();
    protected ZkBiTimerEvent exportTimerEvent;
    
    final Bandbox tbSearchBox = new Bandbox();
    final Checkbox cbSearchBox = new Checkbox();
    
    protected HtmlBasedComponent searchDiv;
    final Div searchTagDiv = new Div();
    
   	ZkBiSearchHelper zkBiSearch = null;
   	ZkBiHotkeyHelper zkBiHotkey = null;
   	
   	Window expWin = new Window();
   	Textbox expWinFileName = new Textbox();
   	Textbox expWinPassword = new Textbox();
    Checkbox expWinPasswordCb = new Checkbox();
    Checkbox expWinMerged = new Checkbox();
    Checkbox expWinTemplate = new Checkbox();
    Checkbox expWinDetail = new Checkbox();
    
   	Div headerLabelDiv = new Div();
   	Toolbarbutton headerLabel;
	ArrayList<Pair<Character,Component>> hotkeyList = new ArrayList<Pair<Character,Component>>();
   	Listfoot listfoot = null;
   	
	Timer autoRefreshTimer = new Timer();
   	public static boolean buildDetailFormDuringInitFlag = false; //build detail form during init, seems no side effect
	
	boolean adjustWidthFlag = false;
	Toolbarbutton btAdjustWidth;
	
	boolean advSearchFlag = false;
	protected Div divAdvSearchG2, divAdvSearchG2Indicator;
	Toolbarbutton btAdvSearch, btAdvSearchG2;
	Toolbarbutton btTour;
	
	protected Toolbarbutton btReload = new Toolbarbutton();
	boolean allowCopyFieldToClipboard = true;
	boolean allowCopyAllFieldToClipboard = false; //disable it temporary, it's a bit annoying when open detail page
	protected ActionButtonHelper abHelper = null;
	boolean customLayout = false;
	boolean allowAdvSearchG1Preset = false;

	String barcodeScanner = null;
	String barcodeDevId = null;
   	Image barcodeScannerImg = null;
	String printerDevice = null;
	String printerDevId = null;
   	Image printerImg = null;
	String cameraDevice = null;
	String cameraDevId = null;
   	Image cameraImg = null;
	String recorderDevice = null;
	String recorderDevId = null;
   	Image recorderImg = null;
	String gpsDevice = null;
	String gpsDevId = null;
   	Image gpsImg = null;

	String gpsScanner = null;
	String gpsreDevId = null;
   	Image gpsrImg = null;
	String eventQueId = null;
	protected boolean inDetailForm=false;
	Set needRefreshSet=null;

	boolean importAsSingle = true;
	boolean useColumnOrderArray = true;
//	protected ArrayList<Integer> columnOrderArray;
	protected int aggregateOffset = 0; /* offset relative to last list column : 0 = next to last column, 1 = last but one column */
	
//	protected boolean screenLayoutG3 = true;
	protected boolean allowDragDropHeader = false;
//	protected boolean renderHiddenColumns = true;
	private int frozenCount = 0;
	
    Button btnBiView = null;
    
    Button btImportPreset = null;
    Button btExportPreset = null;

//	ArrayList<Pair<String,String>> ColumnHeaderTranslate = new ArrayList<Pair<String,String>>();
	ArrayList<Pair<String,String>> importExportMap = null;
    Button btImportTranslate = null;
    Button btExportTranslate = null;
    
   	Button btnImport = null;
   	Button btnImportG2 = null;
   	Button btnExport = null;
   	
   	protected Paging listPaging;
	
   	protected String generateReportDesignRes = "ReportGenerate.rptdesign";
   	protected Map<String, String> generateReportItemScriptMap;
   	protected Map<String, Object> generateReportUserPropMap;
   	protected Map<String, Object> generateReportSettingMap;
   	protected ReportGenerate.Callback generateReportCallback;
   	
	boolean ignoreInvalidDate = true;
	//boolean importAlwaysAdd = false;
	
	Button btRecalUpd = null;
	
	List<String> hideAggregates;
//    Menupopup menupopup;
	protected List<String> defaultColumnOrders;
	protected Hashtable<String,BiActionHandler> bahHash;
	protected String detailIcon = "images/icons/zkweb/039-file-3-20x20.png" ; 
    protected interface ZkBiTimerEventInterface   //TODO: need to simplify and generalized export dialog
    {
    	public void onTimerFired();
    	public void onCancelClicked();
    }
    
    protected class ZkBiTimerEvent
    {
    	public ZkBiTimerEventInterface zkBiTimerEventInterface;
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
    
	
    private void setPresetMapOrderbysAndHideCols(ConditionFieldMap presetMap,BiResult p_result) {
		if(listbox != null) {
//			List <Integer>  hideCols = new ArrayList <Integer>();
			//List <Integer>  orderbys = new ArrayList <Integer>();
			Map<Integer, String> orderbys = new LinkedHashMap<Integer, String>(); //<(+-)colid, bcLabel>
			List <String>  hideColLabels = new ArrayList <String>();
			List <Pair<String, Boolean>>  orderbyLabels = new ArrayList <Pair<String, Boolean>>();
			presetMap.setOrderbyLabel(null);
			presetMap.setHideColLabels(null);
			presetMap.setOrderbyLabels(null);
			if (!mMultiSortMap.isEmpty()) {
				//presetMap.setOrderby(0);
				for (Map.Entry<Integer, MultiSortInfo> entry : mMultiSortMap.entrySet()) {
					//orderbys.add(entry.getValue().sortDesc ? -entry.getKey() : entry.getKey());
					orderbys.put(entry.getValue().sortDesc ? -entry.getKey() : entry.getKey(), null);
				}
				//presetMap.setOrderbys(orderbys);
			}
			for (Component tmpComp : listbox.queryAll("Listheader")){
				Listheader tmpListheader = (Listheader) tmpComp;
				BiColumn biColumn = (BiColumn) tmpListheader.getAttribute("ma_bicolumn");
				String aop = (String)tmpListheader.getAttribute("aggregate_or_pivot");
				String bcLabel = biColumn != null ? biColumn.getLabel() : aop;
				if (tmpListheader.getId().startsWith("browser_listheader_") && bcLabel != null){
					int colid = Integer.parseInt( tmpListheader.getId().substring(19));
					String s = tmpListheader.getSortDirection();
					if(biColumn != null) {
					if(!tmpListheader.isVisible() && (Boolean)tmpListheader.getAttribute("isHideForTempBiColumn") == null) {
//						hideCols.add(new Integer(colid));
						hideColLabels.add(bcLabel);
					}
					}
					/*if (orderbys.contains(-colid))
						orderbyLabels.add(Pair.of(bcLabel, true));
					else if (orderbys.contains(colid))
						orderbyLabels.add(Pair.of(bcLabel, false));*/
					if (orderbys.containsKey(-colid))
						orderbys.put(-colid, bcLabel);
					else if (orderbys.containsKey(colid))
						orderbys.put(colid, bcLabel);
					if (mMultiSortMap.isEmpty()) {
						if(s.equals("ascending")) {
							presetMap.setOrderbyLabel(Pair.of(bcLabel, false));
						}
						if(s.equals("descending")) {
							presetMap.setOrderbyLabel(Pair.of(bcLabel, true));
						}
					}
				}
			}
			for (Map.Entry<Integer, String> entry : orderbys.entrySet()) {
				int colid = entry.getKey();
				String bcLabel = entry.getValue();
				if (bcLabel != null)
					orderbyLabels.add(Pair.of(bcLabel, colid < 0));
			}
			if (!hideColLabels.isEmpty())
				presetMap.setHideColLabels(hideColLabels);
			if (getPivotColumns() != null)
				presetMap.setPivotColLabels(new ArrayList<String>(getPivotColumns()));
			if (!orderbyLabels.isEmpty())
				presetMap.setOrderbyLabels(orderbyLabels);
			if (hideAggregates != null && !hideAggregates.isEmpty()) {
				presetMap.setHideAggregates(hideAggregates);
			}
			{
				List<String> adhocColList = p_result.getAdhocColumnList();
				if(adhocColList != null && adhocColList.size() > 0) {
					try {
					ArrayList<String> jAdhocCols  = new ArrayList<String>();
					for(String tmpCol : adhocColList) {
						BiColumn bc = p_result.getTempColumnByLabel(tmpCol);
						JSONObject jAdhocCol = new JSONObject();
						jAdhocCol.put("adhocColLabel", bc.getLabel());
						jAdhocCol.put("adhocColHdr", bc.getEngName());
						jAdhocCol.put("adhocColType", bc.getColumnType());
						jAdhocCol.put("adhocColAgg", bc.getAggregate() == null ? "" : bc.getAggregate());
						jAdhocCol.put("adhocColFormula", bc.getFormula(false) == null ? "" : bc.getFormula(false));
						jAdhocCol.put("adhocColFormat", bc.getFormat() == null ? "" : bc.getFormat());
						jAdhocCols.add(jAdhocCol.toString());
					}
					presetMap.setAdhocColumns(jAdhocCols);
					} catch (JSONException jex) {
						UniLog.log(jex);
					}
				}
			}
			presetMap.setColumnOrders(p_result.getModifiedViewList());
			presetMap.setFrozenCount(frozenCount);
		}
    }
   	public class InputFieldsList extends ArrayList<InputFields> {
   		private String customCondition;
   		public void setCustomCondition(String customCondition) {
   			this.customCondition = customCondition;
   			if (btnCustCondition != null)
   				ZkUtil.delayPostEvent("onStateChange", btnCustCondition, null, 10);
   		}
   		public String getCustomCondition() {
   			return customCondition;
   		}
   		public int validateCustomCondition(BiResult result) {
   			if (StringUtils.isNotBlank(customCondition))
   				return result.addCustomCondition(customCondition, true).booleanValue() ? 1 : 2;
   			else
   				return 0;
   		}
   		@Override
   		public void clear() {
   			super.clear();
   			setCustomCondition("");
   		}
	    public ConditionFieldMap makeConditionFieldMap(boolean isCustom,BiResult p_result) {
	    	ConditionFieldMap presetMap = new ConditionFieldMap(isCustom, sessionHelper);
			for (InputFields inputFields : this) {
				for (InputField inputField : inputFields) {
					ConditionField cf = inputField.makeConditionField();
					//UniLog.logm("DEBUG:inputField:%s,%s,%s", cf.label, cf.fdName, cf.fdLabel);
					if (cf != null) {
						if (!presetMap.containsKey(cf.label, cf.fdName, cf.fdLabel)) 
							presetMap.put(cf.label, cf.fdName, cf.fdLabel, cf);
						else
							presetMap.get(cf.label, cf.fdName, cf.fdLabel).orConditionFields.add(cf);
					}
				}
			}
	
			setPresetMapOrderbysAndHideCols(presetMap,p_result);
			presetMap.setCustomCondition(customCondition);
			return presetMap;
	    }
	    public void reset(String preset) {
	    	ConditionFieldMap conditionFieldMap = mConditionPresets.getFieldMap(preset);
	    	reset(conditionFieldMap);
	    }
	    private void reset(ConditionFieldMap conditionFieldMap) {
	    	for (InputFields inputFields : this) {
	    		inputFields.clear();
	    		InputField inputField = inputFields.getFirst();
	   			ConditionField cf = conditionFieldMap != null ? conditionFieldMap.get(inputField.parent.bc.getEngName(), inputField.parent.bc.getField(), inputField.parent.bc.getLabel()) : null;
	   			//ConditionField cf = conditionFieldMap != null ? conditionFieldMap.get(inputField.lb.getValue()) : null;
	    		if (cf != null) {
	    			if (sessionHelper.getAllowNewAdvSearch()) {
	    				inputField.clear();
    					InputField inputField1 = inputFields.add();
    					inputField1.reset(cf);
    					Vbox vbox = null;
		    			XulElement hbox = (XulElement) inputField.parentComp;
		    			if (hbox != null)
		    				vbox = (Vbox) hbox.getParent();
		    			if (vbox != null)
		    				buildAdvSearchCond(vbox, inputField1);
	    				for (ConditionField cf1 : cf.orConditionFields) {
			    			inputField1 = inputFields.add();
			    			inputField1.reset(cf1);
			    			if (vbox != null)
			    				buildAdvSearchCond(vbox, inputField1);
			    		}
	    			} else {
	    				inputField.reset(cf);
	    				for (ConditionField cf1 : cf.orConditionFields) {
	    					InputField inputField1 = inputFields.add();
	    					inputField1.reset(cf1);
	    				}
	    			}
	    		}
	    	}
	    	setCustomCondition(conditionFieldMap != null ? conditionFieldMap.getCustomCondition() : "");
	    	if (sessionHelper.getAllowNewAdvSearchG2())
	    		ibLimit.setValue((conditionFieldMap != null && conditionFieldMap.getRecordLimit() > 0) ? conditionFieldMap.getRecordLimit() : sessionHelper.getDefaultRecordLimit());
	    	if(conditionFieldMap != null) frozenCount = conditionFieldMap.getFrozenCount(); else frozenCount = 0;
	    }
//	    public void reset(Vector<BiCondition> conditionList) {
//    		for (BiCondition condition : conditionList) {
//    			UniLog.log("conditionList " + condition.getWherecl());
//    			ConditionField cf = ConditionField.parseConditionString(condition.getWherecl().trim());
//    			if (cf != null) {
//    				for (InputFields inputFields : this) {
//    					//if (inputFields.bc.getField().getFullName().equals(cf.label)) {
//    					if (inputFields.bc.getLabel().equals(cf.label)) {
//    						inputFields.clear();
//    						inputFields.getFirst().reset(cf);
//    						for (ConditionField cf1 : cf.orConditionFields)
//		    					inputFields.add().reset(cf1);
//    						break;
//    					}
//    				}
//    			}
//    		}
//    		customCondition = "";
//	    }
	    public void clearAllInputFields() {
	    	for (InputFields inputFields : this)
	    		inputFields.clear();
	    	setCustomCondition("");
	    }
	    public ReturnMsg toConditions(BiResult result) {
	    	result.clearCondition();
	    	ConditionFieldMap conditionFieldMap = makeConditionFieldMap(true,result);
	    	for (ConditionField cf : conditionFieldMap.values()) {
	    		boolean foundOr = false;
	    		StringBuilder sb = new StringBuilder(StringUtils.defaultString(cf.conditionString));
	    		for (ConditionField cfOr : cf.orConditionFields) {
	    			if (StringUtils.isNotBlank(cfOr.conditionString)) {
	    				sb.append(" or " + cfOr.conditionString);
	    				foundOr = true;
	    			}
	    		}
	    		String str = sb.toString();
	    		if (foundOr)
	    			str = String.format("(%s)", str);
	    		if (StringUtils.isNotBlank(str)) {
	    			if (cf.bc.getField() != null)
	    				result.addCondition(new VectorUtil().addElement(cf.bc.getField().getTable()).toVector(), str);
	    			else
	    				result.addCondition(new Vector<Object>(), str);
	    		}
	    	}
    		ReturnMsg rtn = new ReturnMsg(true);
	    	if (StringUtils.isNotBlank(customCondition)) {
	    		String cond;
	    		if (isIncludeEmbedSearch()) {
        			String preset = (String)conditionPresetListbox.getSelectedItem().getValue();
	    			ZkBiAdvSearch advSearch = new ZkBiAdvSearch(result, masterWin, sessionHelper, null, mConditionPresets, preset, presetid, null);
	    			advSearch.restoreEmbedConditionBlocks(new Groupbox(), new Div(), inputFieldsList.customCondition);
	    			cond = advSearch.makeCustomCondition(false, true);
	    			UniLog.log1("addCustomCondition:%s", cond);
	    		} else
	    			cond = customCondition;
	    		rtn = result.addCustomCondition(cond);
	    	}
	    	/*
			for (Object tmpCondition :  result.getCondition()){
				UniLog.log("DEBUG condition:"+ ((BiCondition) tmpCondition).getWherecl());
			}
			*/
			String presetKey = conditionPresetListbox.getSelectedItem().getValue();
			result.putUserData("presetKey",presetKey);
			result.putUserData("conditionFieldMap",conditionFieldMap);
			
			return rtn;
	    }
   	}
   	public class InputFields extends ArrayList<InputField> {
		BiColumn bc;
   		public InputFields(BiColumn bc, Component parentComp) {
   			this.bc = bc;
   			this.add(parentComp);
   		}
   		public InputField add() {
   			return add((Component)null);
   		}
   		public InputField add(Component parentComp) {
   			InputField inputField; 
   			super.add(inputField = new InputField(this, parentComp));
   			return inputField;
   		}
   		public InputField insertAfter(InputField bfInputField) {
   			return insertAfter(bfInputField, null);
   		}
   		public InputField insertAfter(InputField bfInputField, Component parentComp) {
   			InputField inputField = new InputField(this, parentComp);
   			if (bfInputField != null)
   				add(indexOf(bfInputField) + 1, inputField);
   			else
   				add(inputField);
   			return inputField;
   		}
   		public InputField insertBefore(int index) {
   			return insertBefore(index, null);
   		}
   		public InputField insertBefore(int index, Component parentComp) {
   			InputField inputField = new InputField(this, parentComp);
   			add(index, inputField);
   			return inputField;
   		}
   		public InputField next(InputField bfInputField) {
   			if (bfInputField != null) {
   				int index = indexOf(bfInputField);
   				if (index >= 0) {
   					if (++index < size())
   						return get(index);
   				}
   			}
   			return null;
   		}
   		public boolean remove(InputField inputField) {
   			if (inputField.parentComp != null)
   				inputField.parentComp.detach();
   			return super.remove(inputField);
   		}
   		@Override
   		public void clear() {
    		for (int i = size() - 1; i >= 0; i--) {
   	    		InputField inputField = get(i);
   	    		if (i > 0) {
   	    			if (inputField.parentComp != null)
   	    				inputField.parentComp.detach();
   	    			remove(i);
   	    		} else {
   	    			inputField.clear();
   	    		}
   	    	}
   		}
   		public InputField getFirst() {
   			return get(0);
   		}
   	}
   	public class InputField{
   		private static final String TIMEBOX_DATE = "1970/01/01";
   		InputFields parent;
   		Vlayout vl;
   		Hlayout hl;
   		Label   lb;
   		Listbox cb;
   		Textbox tb;
   		InputElement if0;
   		Label   lba;
   		InputElement if1;
   		Toolbarbutton tbbClose;
   		Component parentComp;
   		private boolean readonly;
   		public void clear() {
   			clearValues();
   			reFormat();
   		}
   		public void clearValues() {
   			//cb.setValue("=");
   			setOperator("=");
  			if0.setText("");
  			if1.setText("");
  			tb.setText("");
   		}
   		public void reset(ConditionField cf) {
			setOperator(cf.operator);
		    reFormat();
	    	setValue("");
	    	setValue1("");
		    if (cf.value1 != null)
		    	setValue(cf.value1.toString());
		    if (cf.value2 != null)
		    	setValue1(cf.value2.toString());
   		}
   		public void setReadonly(boolean readonly) {
   			cb.setDisabled(readonly);
   			tb.setReadonly(readonly);
   			if0.setReadonly(readonly);
   			if1.setReadonly(readonly);
   			this.readonly = readonly;
   		}
   		public boolean isReadonly() {
   			return readonly;
   		}
   		public void copy(InputField src) {
   			ConditionField cf = src.makeConditionField();
   			if (cf != null)
   				reset(cf);
   		}
   		private void setValue(InputElement inputElement, String value) {
   			if (inputElement instanceof ZkJxQueryInput)
   				((ZkJxQueryInput) inputElement).setQueryString(value);
   			else if (inputElement instanceof Timebox) {
   				Timebox tb = (Timebox)inputElement;
   				try {
   					DateFormat df = new SimpleDateFormat();
					tb.setValue(df.parse(value));
				} catch (Exception e) {
					UniLog.log("setValue error: " + e.toString());
					e.printStackTrace();
				}
   			} else
   				inputElement.setText(value);
   		}
   		private String getValue(InputElement inputElement, boolean isSqlValue) {
   			String value;
   			if (inputElement instanceof ZkJxQueryInput) {
   				value = isSqlValue ? ((ZkJxQueryInput) inputElement).getText() 
   						: ((ZkJxQueryInput) inputElement).getQueryString();
   			} else if (inputElement instanceof Timebox) {
   				value = StringUtils.isNotBlank(inputElement.getText()) ? TIMEBOX_DATE + " " + inputElement.getText() : "";
   			} else
   				value = inputElement.getText();
   			if (isSqlValue) {
   				BiColumn bc = parent.bc;
   				String type = bc.getField() != null ? bc.getField().getFieldType() : bc.getColumnType();
   				if (StringUtils.equals(type, "integer")) {
   					String[] ol = bc.getOptionList(getSessionHelper());
   					if (ol != null) {
   						for (int i = 0; i < ol.length; i++) {
   							if (StringUtils.equalsIgnoreCase(ol[i], value)) {
   								UniLog.log("InputField getRadioValue index:" + i + ",value:" + value);
   								return String.valueOf(i);
   							}
   						}
   						return String.valueOf(-1);
   					}
   				}
   			}
   			return value;
   		}
   		public void setValue(String p_val){
   			if (tb.isVisible())
   				setValue(tb, p_val);
   			else
  				setValue(if0, p_val);
   		}
   		public void setValue1(String p_val){
 			setValue(if1, p_val);
   		}
   		public String getValue(){
   			return getValue(false);
   		}
   		public String getValue(boolean isSqlValue){
   			if (tb.isVisible())
   				return getValue(tb, isSqlValue);
   			else
  				return getValue(if0, isSqlValue);
   		}
   		public String getValue1(boolean isSqlValue){
 			return getValue(if1, isSqlValue);
   		}
   		public String getValue1(){
   			return getValue1(false);
   		}
   		public void setOperator(String operator) {
   			//cb.setValue(operator);
   			for (Listitem li : cb.getItems()) {
   				if (li.getValue().equals(operator)) {
   					cb.selectItem(li);
   					return;
   				}
   			}
   			UniLog.log("setOperator invalie value " + operator);
   		}
   		private void appendOperator(String operator) {
   			cb.appendItem(operator, operator);
   		}
   		private String getOperator() {
   			Listitem li = cb.getSelectedItem();
   			if (li != null && li.getValue() instanceof String)
   				return (String) li.getValue();
   			UniLog.log("getOperator empty value");
   			return "";
   		}
   		public void reFormat()
   		{
			if(getOperator().equals("in") || getOperator().equals("not in") || getOperator().equals("matches")
					) {
				tb.setVisible(true);
				hl.setVisible(false);
				if0.setVisible(false);
				lba.setVisible(false);
			} else {
				tb.setVisible(false);
				if(getOperator().equals("is blank")) {
					if0.setVisible(false);
				} else {
					if0.setVisible(true);
				}
				if(getOperator().equals("between")) {
					lba.setVisible(true);
					if1.setVisible(true);
				} else {
					lba.setVisible(false);
					if1.setVisible(false);
				}
				hl.setVisible(true);
			}
			if (parentComp != null){
				ZkUtil.delayPostEvent("onCustomResize", parentComp, null, 100);
			}
   		}
   		private InputField(InputFields parent, Component parentComp){
   			this.parent = parent;
   			this.parentComp = parentComp;
   			lb = new Label();
   			lb.setValue(sessionHelper.getLabel(parent.bc.getEngName()));
    		lb.setWidth("95%");
   			cb = new Listbox();
   			cb.setMold("select");
   			cb.setStyle("height:24px;width:70px;border-radius: 4px; font-size:8pt");
   			if (isMobile()){
   				cb.setWidth("95%");
   			}
    		appendOperator("=");
    		appendOperator("<>");
    		appendOperator(">");
    		appendOperator("<");
    		appendOperator(">=");
    		appendOperator("<=");
    		appendOperator("is blank");
    		appendOperator("matches");
    		appendOperator("between");
    		appendOperator("in");
    		appendOperator("not in");
    		setOperator("=");
    		
    		cb.addEventListener(Events.ON_SELECT,
    		    	new ZkBiEventListener<Event>() {
    		       		public void onZkBiEvent(Event event) throws Exception {
    		        			UniLog.log("Query operator chaneg");
    		        			reFormat();
    		        			Clients.resize(listbox);
    		            	}	
    		       	});		
   			
   			vl = new Vlayout();
   			vl.setWidth("100%");
   			tb = new Textbox();
   			tb.setWidth("100%");
   			tb.setVisible(false);
   			hl = new Hlayout();
   			if (parentComp != null) {
   				parentComp.appendChild(lb);
   				parentComp.appendChild(cb);
   				parentComp.appendChild(vl);
   			}
   			vl.appendChild(tb);
   			vl.appendChild(hl);
   			UniLog.log("InputField getColumnType " + parent.bc.getEngName() + "," + parent.bc.getColumnType());
   			if (parent.bc.getColumnType().trim().equals("date")){
   				if0 = new ZkJxQueryInput();
   				((ZkJxQueryInput)if0).setType(ZkJxQueryInput.TYPE_DATE, sessionHelper);
   				if1 = new ZkJxQueryInput();
   				((ZkJxQueryInput)if1).setType(ZkJxQueryInput.TYPE_DATE, sessionHelper); 
   			} else if (parent.bc.getColumnType().trim().equals("datetime")){
   				if0 = new ZkJxQueryInput();
   				((ZkJxQueryInput)if0).setType(ZkJxQueryInput.TYPE_DATETIME, sessionHelper);
   				if1 = new ZkJxQueryInput();
   				((ZkJxQueryInput)if1).setType(ZkJxQueryInput.TYPE_DATETIME, sessionHelper); 
   			} else if (parent.bc.getColumnType().trim().equals("time")){
   				if0 = new Timebox();
   				if1 = new Timebox();
    		} else {
    			if0 = new ZkJxQueryInput();
    			if0.setMaxlength(parent.bc.getColumnLength());
    			if0.setText("");
    			if1 = new ZkJxQueryInput();
    			if1.setMaxlength(parent.bc.getColumnLength());
    			if1.setText("");
   			}
   			
   			if1.setVisible(false);
   			lba = new Label();
   			lba.setValue("and");
   			lba.setStyle("font-size:8pt");
   			lba.setVisible(false);
   			hl.appendChild(if0);
   			hl.appendChild(lba);
   			hl.appendChild(if1);
   		}
   		private ConditionField createConditionField() {
   			ConditionField cf = new ConditionField();
			cf.label = parent.bc.getEngName();
			cf.fdName = parent.bc.getField() != null ? parent.bc.getField().getFullName() : "";
			cf.fdLabel = parent.bc.getLabel();
			cf.operator = getOperator();
			return cf;
   		}
   		private String realFieldName() {
//   			return parent.bc.getField() != null ? parent.bc.getField().getFullName() : parent.bc.getLabel();
   			return (parent.bc.getLabel());
   		}
   		public ConditionField makeConditionField() {
			ConditionField cf = null;
			if(
				getOperator().equals("=") ||
				getOperator().equals("<>") ||
				getOperator().equals(">=") ||
				getOperator().equals("<=") ||
				getOperator().equals(">") ||
				getOperator().equals("<")
				) {
				if (StringUtils.isBlank(getValue())) return null;
				cf = createConditionField();
				cf.value1 = getValue();
				cf.conditionString = String.format("%s %s '%s'", realFieldName(), getOperator(), Expression.escapeStr(getValue(true)));
			}
			if( getOperator().equals("in") ||
				getOperator().equals("not in")) {
				if (StringUtils.isBlank(getValue())) return null;
				cf = createConditionField();
				cf.value1 = getValue();
				cf.conditionString = String.format("%s %s (%s)", realFieldName(), getOperator(), getValue(true));
			}
			if( getOperator().equals("matches")) {
				if (StringUtils.isBlank(getValue())) return null;
				cf = createConditionField();
				cf.value1 = getValue();
				cf.conditionString = String.format("%s %s '%s'", realFieldName(), getOperator(), Expression.escapeStr(getValue(true)));
			}
			
			if( getOperator().equals("between")) {
				if (StringUtils.isBlank(getValue())) return null;
				if (StringUtils.isBlank(getValue1())) return null;
				cf = createConditionField();
				cf.value1 = getValue();
				cf.value2 = getValue1();
				cf.conditionString = String.format("%s %s '%s' and '%s'", realFieldName(), getOperator(), 
						Expression.escapeStr(getValue(true)), Expression.escapeStr(getValue1(true)));
			}
			if (getOperator().equals("is blank")) {
				cf = createConditionField();
				cf.conditionString = String.format("%s =''", realFieldName());
			}
			
			if (cf != null)
				cf.bc = parent.bc;
			return cf;
	    }
   	}
   	
   	public BiResult initZkBiWindows()
   	{
		mConditionPresets = sessionHelper.getConditionPresets(presetid);
 		BiResult result = do_bi_browse(sessionHelper,viewid,pageid+":"+browserWindowId.getValue(),masterWin);
  		sessionHelper.logJVMStat();
  		return(result);
   	}
   	
   	public void doAfterCompose(final Component comp) throws Exception { 
   		super.doAfterCompose(comp);
   		if(hasAUDColumn == null) hasAUDColumn = true;
   		if (!accessOkFlag) {
   			return;
   		}
   		masterWin = (XulElement) comp;

   		browserWindowId = new Label(); 
   		browserWindowId.setValue(UUID.randomUUID()+"");

   		autoRefreshTimer.setPage(masterWin.getPage());
   		autoRefreshTimer.setRepeats(true);
   		autoRefreshTimer.setRunning(false);

   		autoRefreshTimer.addEventListener("onTimer", new ZkBiEventListener() {
   			public void onZkBiEvent(Event event) throws Exception {
   				Toolbarbutton btReload = (Toolbarbutton) masterWin.query("#btReload");

   				//TODO: reset timer if detect user movement
   				if (ZkBiHotkeyHelper.checkAcceptHotkey(btReload)){
   					UniLog.log("autoRefreshTimer call btReload");
   					Events.postEvent(Events.ON_CLICK, masterWin.getFellow("btReload"), null);
   				}
   				else{
   					UniLog.log("autoRefreshTimer skip");
   				}
   			}
   		});

   		exportTimerEvent = new ZkBiTimerEvent();
   		
   		exportTimer.setPage(comp.getPage());
   		exportTimer.setDelay(300);
   		exportTimer.setRepeats(false);
   		exportTimer.addEventListener("onTimer", new ZkBiEventListener() {
   			@Override
   			public void onZkBiEvent(Event event) throws Exception {
   				UniLog.logm(this,"exportTimer running...");
   				synchronized(exportTimerEvent) {
   					synchronized (exportTimerEvent) {
   						if (exportTimerEvent.zkBiTimerEventInterface != null){
   							exportTimerEvent.zkBiTimerEventInterface.onTimerFired();
   						}
   					}
   				}
   			}
   		});
   		exportTimer.setRunning(false);

   		UniLog.log(String.format("ZkBiComposer:doAfterCompose(): %s comp.getId()=%s comp.getUuid()=%s httpSession.getId()=%s page=%s", 
   				""+this,
   				comp.getId(),
   				comp.getUuid(),
   				((HttpSession) (Executions.getCurrent()).getDesktop().getSession().getNativeSession()).getId(),
   				comp.getPage()
   				));

   		inputFieldsList.clear();
   		action = getExecutionURLParam(comp, "action");
   		queryCondition = getExecutionURLParam(comp, "querycondition");
   		if(action == null) {
   			UniLog.log("Action is null");
   			redirectToLogin();
   			return;
   		}
		pageid = getExecutionURLParam(comp, "page_id");
		if(pageid == null || pageid.trim().equals("")) {
			UniLog.log("pageid is null");
			redirectToLogin();
			return;
		}
		biResultClass = getExecutionURLParam(comp, "biResult");
		UniLog.log("ZkComposerBase biResultClass = "  + biResultClass);
		if(queryCondition != null && sessionHelper.hasOneTimeData(queryCondition)) {
			String overrideAction = getExecutionURLParam(comp, "overrideaction");
			if(overrideAction != null) action = overrideAction;
		}
		barcodeScanner = getExecutionURLParam(comp, "BarcodeScanner");
		if(!"ONDEMAND".equals(barcodeScanner)) {
			barcodeDevId = barcodeScanner;
		}
		cameraDevice = getExecutionURLParam(comp, "Camera");
		if(!"ONDEMAND".equals(cameraDevice)) {
//			cameraDevId = cameraDevice;
		}
		recorderDevice = getExecutionURLParam(comp, "Recorder");
		if(!"ONDEMAND".equals(recorderDevice)) {
//			recorderDevId = recorderDevice;
		}
		gpsDevice = getExecutionURLParam(comp, "Gps");
		if(!"ONDEMAND".equals(gpsDevice)) {
//			gpsDevId = gpsDevice;
		}
		printerDevice = getExecutionURLParam(comp, "Printer");
		if(!"ONDEMAND".equals(printerDevice)) {
//			printerDevId = printerDevice;
		}
		if(action.equals("browse") || action.equals("add") || action.equals("update")) {
			viewid = getExecutionURLParam(comp, "viewid");
			helpid = StringUtils.defaultIfBlank(getExecutionURLParam(comp, "helpid"), viewid);
			presetid = getExecutionURLParam(comp, "presetid");
			if (StringUtils.isBlank(presetid) || !presetid.matches("[a-zA-Z0-9-.]+"))
				presetid = viewid;
			UniLog.log1("viewid:%s, presetid:%s", viewid, presetid);
			BiResult result = initZkBiWindows();
			if (sessionHelper.getAutoRefresh() > 0){
				autoRefreshTimer.setDelay(sessionHelper.getAutoRefresh());
				autoRefreshTimer.setRunning(true);
			}
			else{
				if (autoRefreshTimer.isRunning()){
					autoRefreshTimer.setRunning(false);
				}
			}
			
			if (isWidget()) {
				queryBar.setVisible(false);
				bottomPanelVbox.setVisible(false);
				btnHelp.setVisible(false);
				final HtmlBasedComponent hcomp = (HtmlBasedComponent)comp;
				final int width = NumberUtils.toInt(getExecutionURLParam(comp, "width"));
				final int height = NumberUtils.toInt(getExecutionURLParam(comp, "height"));
				final int mwidth = NumberUtils.toInt(getExecutionURLParam(comp, "mwidth"));
				final int mheight = NumberUtils.toInt(getExecutionURLParam(comp, "mheight"));
				if (isMobile()) {
					if (mheight > 0) {
						hcomp.setHeight(mheight + "px");
						listbox.setHeight((mheight - 33) + "px");
					}
					hcomp.setWidth("100%");
					if (mwidth > 0) {
						hcomp.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>(){
							@Override
							public void onZkBiEvent(AfterSizeEvent event) throws Exception {
								UniLog.log1("hcomp AfterSizeEvent width:%d, height:%d", event.getWidth(), event.getHeight());
								if (event.getWidth() - 20 > mwidth)
									hcomp.setWidth(mwidth + "px");
							}
						});
					}
					ZkUtil.appendStyle(hcomp, "text-align:left");
				}
				else {
					if (width > 0)
						hcomp.setWidth(width + "px");
					if (height > 0) {
						hcomp.setHeight(height + "px");
						listbox.setHeight((height - 40) + "px");
						adjListboxHeight(0);
					}
				}
			}
			ZkUtil.translateAllComp(result,masterWin);
			if(needDoQuery()) {
//				Events.echoEvent(Events.ON_CLICK, btReload, null);
						/* if exception dump at this line, probably  */
      		    		String preset = conditionPresetListbox.getSelectedItem().getValue();
           		    	inputFieldsList.reset(preset);

      		    		MultiSortMap sortMap = (MultiSortMap) mMultiSortMap.clone();
       		    		onSelectionChanged(result,sortMap,comp);
           		    	
       		    		divAdvSearchG2Indicator.setVisible(false);	
			}
			return;
		}
		UniLog.log("Unknow Action :"+action);
		Messagebox.show(sessionHelper.getLabel("Unknow Action") + ": "+action);

   	}
    
	private String getExecutionURLParam(Component p_comp, String p_key) {
		if (isWidget())
			return ZkUtil.getURLParamFromComp(p_comp, p_key);
		else
			return Executions.getCurrent().getParameter(p_key);
	}

    
    protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc)
    {
    	try {
    		BiSchema schema = BiSchema.loadSchema(sessionHelper);
    		UniLog.logm(this, "queryResult schema:%s agent:%s",schema,schema.getAgent());
    		if(schema == null) return(null); 
    		BiView view = schema.getViewByName(p_viewid);
    		UniLog.log("queryResult view:"+view);
    		if(view == null) 
    			return(null);     		

			BiResult result = view.newBiResult(sessionHelper.getLoginId(),null,biResultClass,sessionHelper);
			UniLog.log("call view.newBiResult() return:" + result);
    		if(result == null) {
    			Messagebox.show("Error new BiResult for view " + view.getName() + " got null");
    			return(null); 
    		}
			if(p_sortIdx > 0) {
				result.addOrderByViewList(p_sortIdx, p_sortDesc); 
			}
    		return(result);
    	} 
    	catch (Exception ex) {
    		UniLog.log(ex);
			Messagebox.show(ex.toString());
    		return(null);
    	}
    }
    
    ReturnMsg setQueryCondition(BiResult result) {
  			try {
  				if(queryCondition == null) return(new ReturnMsg(false,"Condition Key is Null"));
  					JSONObject jo = (JSONObject) sessionHelper.getOneTimeData(queryCondition);
	  				if(jo == null) return(new ReturnMsg(false,"Condition Content is Null"));
	  				result.clearCondition();
	  				if(jo.optString("customCondition") != null) {
//	  					result.addCustomCondition(queryCondition);
	  					result.addCustomCondition( jo.optString("customCondition"));
	  				}
	  				if(jo.optJSONArray("tablist") != null) {
	  					JSONArray ja = jo.getJSONArray("tablist");
	  					Vector tabList = new Vector();
	  					for(int i=0;i<ja.length();i++) {
	  						BiTable bt = result.getView().getSchema().getTable(ja.getString(i));
	  						if(bt == null) return(new ReturnMsg(false,"Table " + ja.getString(i) + " Not Found "));
	  						tabList.add(bt);
	  					}
	  					result.addCondition(tabList, jo.getString("wherestr"));
	  				}
  				return(new ReturnMsg(true));
  			} catch (JSONException jex) {
  				UniLog.log(jex);
  				return(new ReturnMsg(false,jex.toString()));
  			}
    }

    boolean needDoQuery() {
    	boolean doQuery = true;
  		if(/* action.equals("update") && */ queryCondition != null) {
    		doQuery = false;
  		}
    	if (this instanceof ZkBiComposerLedgerReport) {
    		doQuery = false;
    	}
    	if (this instanceof ZkBiComposerLedgerReportMulti) {
    		doQuery = false;
    	}
    	/*
    	if (this instanceof com.uniinformation.zkbi.reports.ZkBiComposerLedgerReport) {
    		doQuery = false;
    	}
    	*/
    	if (StringUtils.equalsAnyIgnoreCase(getURLParam("doquery"),"N","NO","FALSE")) {
    		doQuery = false;
    	}
    	if (StringUtils.equalsAnyIgnoreCase(getURLParam("doquery"),"Y","YES","true")) {
    		doQuery = true;
    	}
    	return(doQuery);
    }
    private BiResult do_bi_browse(SessionHelper p_sessionHelper,String p_viewid,String p_pageid,Component comp)
    {
//    	boolean doQuery = true;
    	boolean doQuery = false;
    	if (this instanceof ZkBiComposerLedgerReport) {
    		doQuery = false;
    	}
    	if (this instanceof ZkBiComposerLedgerReportMulti) {
    		doQuery = false;
    	}
    	/*
    	if (this instanceof com.uniinformation.zkbi.reports.ZkBiComposerLedgerReport) {
    		doQuery = false;
    	}
    	*/
    	if (StringUtils.equalsAnyIgnoreCase(getURLParam("doquery"),"N","NO","FALSE")) {
    		doQuery = false;
    	}
    	if (StringUtils.equalsAnyIgnoreCase(getURLParam("doquery"),"Y","YES","true")) {
    		doQuery = true;
    	}
    	return(this.do_bi_browse(p_sessionHelper,p_viewid,p_pageid,comp,doQuery));
    	//return(do_bi_browse(p_sessionHelper,p_viewid,p_pageid,comp, !(this instanceof ZkBiComposerLedgerReport) ));
//    	return(do_bi_browse(p_sessionHelper,p_viewid,p_pageid,comp, true));
    }
    private BiResult do_bi_browse(SessionHelper p_sessionHelper,String p_viewid,String p_pageid,Component comp,boolean p_doQuery)
    {
   		abHelper = new ActionButtonHelper(sessionHelper);
    	setupHotkey();
    	UniLog.log("do_bi_browse entered viewid:"+p_viewid + " pageid:"+ p_pageid);
    	if(p_viewid == null || p_viewid.trim().equals("")) {
    			UniLog.log("invalid viewid");
    			return null;
    	}
    	BiView view;
    	BiResult result;
    	
    	//andrew200720: this block of code should be unreachable due to random pageid
    	if((result = (BiResult) sessionHelper.getSessionData("biresult."+p_pageid.trim())) != null) {
    		result.unmapColumns();
    		view = result.getView();
    		if(!view.getName().equals(p_viewid)) {
    			result.close();
    			sessionHelper.removeSessionData("biresult."+p_pageid.trim());
    			view = null;
    			result = null;
    			UniLog.log("do_bi_browse previous result set invalid , cleared");
    		} 
    		else {
    			UniLog.log("do_bi_browse use previous result set");
    		}
    	}
    	boolean needQuery = false;
    	if(result == null) {
    		result = getQueryResult(sessionHelper,p_viewid, defaultSortIdx, defaultSortDesc);
    		if(result != null) {
    			//andrew200720: comment out putSessionData(biresult), it may cause memory leak
    			//It should be no longer work due to random pageid. It may affect CUEST_detail, WOEST_detail
    			//sessionHelper.putSessionData("biresult."+p_pageid.trim(),result);  
    		
    			
    			needQuery = p_doQuery;
    		}
    	} 
    	else {
    		if(result.getLastQuery() == null || (result.getLastUpdate() != null && result.getLastUpdate().after(result.getLastQuery()))) {
    			UniLog.log("ResultSet Changed, redo query");
    			//result.query();
    			needQuery = p_doQuery;
    		}
    	}
    	needQuery = p_doQuery;//new request, remove the cache history, like initial visit.
    	if(result == null) {
    		UniLog.log("do_bi_browse error : fail to query or get previouse result");
    		return null;
    	}
		if(title == null) {
 			title = StringUtils.defaultIfBlank(sessionHelper.getSideMenuDesc(pageid), result.getView().getHeader());
			if (StringUtils.isNotBlank(pageid))
				title = ZkBiTranslateHelper.getText(sessionHelper, pageid.toUpperCase(), "MENU", title);
 			//Clients.evalJavaScript(String.format("document.title='%s'", StringEscapeUtils.escapeJavaScript(sessionHelper.getLabel(title))));
 			Clients.evalJavaScript(String.format(
				"if (!$(document).data('titleSetted')){"
				+ "document.title='%s';"
				+ "}", StringEscapeUtils.escapeJavaScript(sessionHelper.getLabel(title))));
 		}
    	UniLog.log("do_bi_browse result:"+result);
    	buildQueryWindow(result,comp);
    	
    	if (buildDetailFormDuringInitFlag && detailForm == null){
    		detailForm = buildDetailWindow(result);
    		detailForm.closeForm();
    	}
    	
    	String curPreset = null;
  		if(action.equals("add")) {
  			result.clear();
  			needQuery = false;
  		}
		curPreset = mConditionPresets.getDefaultPreset();
  		inputFieldsList.reset(curPreset);
  		Events.sendEvent("onSelect1", conditionPresetListbox, curPreset);
		ReturnMsg rtnQueryMsg = null;

    	UniLog.log("after buildQueryWindow");
    	buildBrowserWindow(result, comp, defaultSortIdx, defaultSortDesc); //TODO: pass current sort index to result??
		
//  		if(action.equals("update") && queryCondition != null) needQuery = true;
  		if(queryCondition != null) needQuery = true;
  		if (needQuery) {
  			if(action.equals("update")) {
  				ReturnMsg rtnMsg = setQueryCondition(result);
  				if(!rtnMsg.getStatus()) {
  					Messagebox.show(
  							rtnMsg.getMsg(),
  							rtnMsg.getMsg(),
  							Messagebox.OK,
  							rtnMsg.getMsg(),
  							new ZkBiEventListener() {
  								public void onZkBiEvent(Event evt) throws InterruptedException {
//  									Executions.getCurrent().sendRedirect("menu.html?menuid=menu_main.html");
  									Executions.getCurrent().sendRedirect("menu.html?menuid="+p_sessionHelper.getRootMenu());
  								}
  							});	
  					return result;	
  				}
  			} 
  			else {
  				if(queryCondition != null) {
  					rtnQueryMsg = setQueryCondition(result);
  				}
  				if(rtnQueryMsg == null || !rtnQueryMsg.getStatus()) 
  					rtnQueryMsg = inputFieldsList.toConditions(result);
  			}

  			result.setRecLimit(ibLimit.getValue());
			if(rtnQueryMsg == null || rtnQueryMsg.getStatus()) 
				rtnQueryMsg = setAdditionalQueryCondition(result);
  			boolean queryStatus = false;
  			if(rtnQueryMsg.getStatus()) {
  				queryStatus = result.query(true).getStatus();
  				UniLog.log("queryResult queryStatus:"+queryStatus);
  				if (!queryStatus){
					Messagebox.show(result.getLastErrorMessage());
  					return result;
  				}
  			} else {
  				result.clear();
  			}
  			if(result.getRowCount() >= result.getRecLimit()) {
				showWarnMsg(sessionHelper.getLabel("Load record - Query result has more than the limit of %d Records. Please refine search condition or increase the limit"), result.getRecLimit());
  			} 
  			else {
				showMsg(sessionHelper.getLabel("Load record - %d records found"), result.getRowCount());
  			}
			headerLabel.setTooltiptext(
					String.format(sessionHelper.getTtLabel("%d Records Loaded at %s") , result.getRowCount(),new SimpleDateFormat("HH:mm:ss").format(new Date()))
					);
			
			sortSingle(result, comp, 0, false);
  		}

//    	if (curPreset != null) {
   			boolean needResetHeader = false;
   			ConditionFieldMap fieldMap = mConditionPresets.getFieldMap(curPreset);
   			if(result != null && fieldMap != null) {
   				List<String> adhocColList = fieldMap.getAdhocColumns();
   				if(adhocColList != null && adhocColList.size() > 0) {
   					try {
   						for(String ss : adhocColList) {
   							JSONObject jo = new JSONObject(ss);
							result.addAdhocColumn(
								jo.optString("adhocColLabel"),
								jo.optString("adhocColHdr"),
								jo.optString("adhocColFormula"),
								jo.optString("adhocColFormat"),
								jo.optString("adhocColType"),
								jo.optString("adhocColAgg")
							);
   							
   						}
   					} catch (Exception ex) {
   						UniLog.log(ex);
   					}
   					needResetHeader = true;
   				}
   			}
    		visibleCols(curPreset, comp,result);
    		mMultiSortMap.reset(curPreset,result);
   			if(result != null) {
   				List<String> list = fieldMap == null ? null : fieldMap.getColumnOrders();
   				if (CollectionUtils.isEmpty(list) && CollectionUtils.isNotEmpty(defaultColumnOrders))
   					list = defaultColumnOrders;
   				if(result.setModifiedViewList(list)) {
   					needResetHeader = true;
   				}
   			}
   			if(needResetHeader) resetListHeader(result);

    		
//    		sortMulti(result, comp);
//    	}
		resizeListbox();
		lastSelectIdx = -1;
		setSelectIdx(-1,null);
  		if(action.equals("add")) {
	        result.clearCurrentRec();
//	        result.doBeforeAdd();
	        doAddOneRow(masterWin,result);
  			return result;
  		}
  		else if(action.equals("update")) {
  			listbox.setSelectedIndex(0);
    		result.loadOneRecV(0);
    		result.fetchOneRecV(0);
    		doUpdateOneRow(masterWin,result);
  			return result;
  		}
  		else{
  			UniLog.log1("unknown action %s", action);
  			return result;
  		}
    }
    
    public int getTrIdxByObj(ListModelList p_lml, Object p_obj){
    	return(getTrIdxByObj(p_lml, p_obj, -1));
    }
    
    //remark: for search, trIdx != listIdx; 
    /**
     * obtain record index by trStat/trStatFilter object
     * remark: for query result, trIdx == listIdx, for search result, trIdx != listIdx
     * @param p_lml - listModelList
     * @param p_obj - TrIdx or TrStatFilter
     * @param p_defaultIdx - default index
     * @return TrIdx
     */
    public int getTrIdxByObj(ListModelList p_lml, Object p_obj, int p_defaultIdx){
    	int idx = p_defaultIdx;
    	int listIdx = getListIdxByObj(p_lml, p_obj);
    	if (p_obj instanceof TrStatFilter){ 
    		idx = ((TrStatFilter) p_obj).getTrStatIdx();
    	}
    	else if (listIdx >= 0){
    		idx = listIdx;
    	}
    	return(idx);
    }
    
    /**
     * obtain listbox index by trStat/trStatFilter Object
     * @param p_lml
     * @param p_obj
     * @return
     */
    public int getListIdxByObj(ListModelList p_lml, Object p_obj){
    	return(listModelList.indexOf(p_obj));
    }
    
    protected void renderOneRecord_real(Listitem item, Object trStat, Vector listColumns,final BiResult result,int idx,Object ts) throws Exception {
    		//Vlayout vl=null;
    		Div divTb = null;
    		int nColumns = 2;
    		int curColumns = 0;
    		Listcell lc;
    		//UniLog.log("render record " + idx);
    		if(useMobileList(isMobile(),result)) {
    			divTb = new Div();
    			divTb.setId("mobile_divtb" + idx);
    			divTb.setStyle("display:table;");
    		}
    		else{
//    			if(allowDragDropHeader) item.setDroppable("true");
    			if(hasAUDColumn){
    				lc = new Listcell("");
   					lc.setStyle("padding-left:15px;text-align:left;");
    				if(result.isMarkedDelete(ts)) {
    					lc.setImage(IMG_DELETE);
    				} else if(result.isMarkedUpdate(ts)) {
    					lc.setImage(IMG_UPDATE);
    				} else {
    					if(!multiSelect && hasDetailButton) {
   							Toolbarbutton tbb = new Toolbarbutton();
   							tbb.setSclass("narrowtoolbarbutton");
//							tbb.setImage("images/icons/zkweb/039-file-3-20x20.png");
							tbb.setImage(detailIcon);   							
   							tbb.setTooltiptext(sessionHelper.getTtLabel("Record Detail"));
    						tbb.addEventListener(Events.ON_CLICK, itemClickListener);
    						lc.appendChild(tbb);
							tbb.setAttribute("trStat", trStat);
							
							//copy all field to clipboard 
							addLcPopup(result, listColumns, -1, lc);
    					}
    				}
    				lc.setParent(item);
    			}
    		}
    		int colDisplayedCnt = 0;
    		boolean hasHideMobileExclude = false;
    		boolean wspreFlag = StringUtils.equalsAnyIgnoreCase(getURLParam("wspre"),"Y","YES","TRUE");

			Listhead listhead = listbox.getListhead();
    		
    		for(int ii = 0;ii<listColumns.size();ii++) {
    			int i;
//    			if(columnOrderArray != null) i = columnOrderArray.get(ii) ; else i = ii;
    			i = ii;
//    			if(!renderHiddenColumns) {
//				Listheader listheader = (Listheader) listhead.query("#browser_listheader_" + (i+(hasAUDColumn ? 1 : 0)));
//				if(listheader != null && !listheader.isVisible()) {
//					continue;
//				}
//    			}
    			if(!useMobileList(isMobile(),result)) {
    				BiColumn biColumn = (BiColumn) listColumns.get(i);
    				String str = result.getCell(biColumn.getLabel()).getColumnDisplayString();
    				String sclass = result.getCell(biColumn.getLabel()).getColumnDisplayClass();
    				int align = result.getCell(biColumn.getLabel()).getAlignment();
    				
    				lc = new Listcell();
//    				Label lb = new Label(str);
//    				if(sclass != null) lb.setSclass(sclass);
//    				lb.setParent(lc);
//    				lc.setParent(item);
//    				lc.setAttribute("bclabel", biColumn.getLabel());
       				Label lb = null;
    				if(biColumn.getColumnType().equals("html")) {
    					Html h = new Html();
    					h.setContent(str);
    					h.setParent(lc);
    					lc.setParent(item);
    					lc.setAttribute("bclabel", biColumn.getLabel());
    				} else {
    					lb = new Label(str);
    					if(sclass != null) lb.setSclass(sclass);
    					lb.setParent(lc);
    					lc.setParent(item);
    					lc.setAttribute("bclabel", biColumn.getLabel());
    				}
    				
    				//andrew 240223 add style for formula cell
    				if (StringUtils.isNotBlank(biColumn.getFormula(false))){
    					ZkUtil.appendSclass(lc, "zkbi-formula");
    				}
    				
    				if(align != 0) {
    					if(align > 0) {
    						ZkUtil.appendStyle(lc, "text-align:" + ((align & 4) != 0 ? "center" : "left") + ";");
    					} else {
    						ZkUtil.appendStyle(lc, "text-align:right;");
    					}
    					if(((align > 0 ? align : -align) & 2) != 0) {
    						ZkUtil.appendStyle(lc, "word-wrap:word-break");
    					}
    				}
					String paddingStr = BiUtil.extractColDecorationValue(biColumn.getDecoration(), "padding");
					if (StringUtils.isNotBlank(paddingStr))
						ZkUtil.appendStyle(lc, "padding:" + paddingStr);
    				/*
    				if (biColumn.getColumnType().trim().matches("float|money|serial|integer|decimal")){
    					ZkUtil.appendStyle(lc, "text-align:right;");
    				}
    				else{
    					ZkUtil.appendStyle(lc, "text-align:left;");
    				}
    				*/
    				
    				/*
    				if (doSearchSingle(lc.getLabel(),i)){
    					ZkUtil.appendStyle(lc, "background-color:rgba(255,255,0,0.3);");
    				}
    				*/
    				//andrew220928 fix quick search highlight matched value
    				if (doSearchSingle(lb.getValue(),i)){
    					ZkUtil.appendStyle(lc, "background-color:rgba(255,255,0,0.3);");
    				}
    				
   					//ZkUtil.appendStyle(lc,"white-space:nowrap;overflow:hidden;text-overflow:ellipsis;");
    				/*
   					ZkUtil.appendStyle(lc,"white-space:nowrap;overflow:hidden;");
   					if (!biColumn.getColumnType().trim().equals("date")){ //andrew210618: fix date field always show '...'
   						ZkUtil.appendStyle(lc,"text-overflow:ellipsis;");
   					}
   					*/
   					//ZkUtil.appendStyle(lc,"white-space:nowrap;overflow:hidden;text-overflow:'';"); //andrew210618 fix any field show '...'
    				if (wspreFlag) {
    					ZkUtil.appendStyle(lc,"white-space:pre;");  //andrew220323 for display preformatted text.
    				}
    				else {
    					//default is nowrap
    					ZkUtil.appendStyle(lc,"white-space:nowrap;"); 
    				}
   					//ZkUtil.appendStyle(lc,"overflow:hidden;text-overflow:'';"); //andrew210618 fix any field show '...'
   					ZkUtil.appendStyle(lc,"overflow:hidden;text-overflow:initial;"); //andrew210618 fix any field show '...'  //andrew220323 fix wrong text-overflow
    				
    				//display popup content. may has performance issue
   					if(!sessionHelper.disableLcPopup()) {
    				if(addLcPopup(result, listColumns, i, lc)) {
//    					ZkUtil.appendStyle(lc, "color:rgb(26,13,171);font-style: italic;");
    					ZkUtil.appendStyle(lc, "color:rgb(26,13,171);");
    				}
   					}
    				colDisplayedCnt++;
    			} 
    			else { //for mobile
    				BiColumn biColumn = (BiColumn) listColumns.get(i);
    				Label lb0 = new Label(sessionHelper.getLabel(biColumn.getEngName()));
					lb0.setValue(ZkBiTranslateHelper.getText(sessionHelper, biColumn.getCellFullName(), "LABEL", lb0.getValue()));
    				String str = result.getCell(biColumn.getLabel()).getColumnDisplayString();
    				Label lb = new Label(str);
    				String sclass = result.getCell(biColumn.getLabel()).getColumnDisplayClass();
    				if(sclass != null) {
    					lb.setSclass(sclass);
    				}
    				boolean matchedFlag = doSearchSingle(lb.getValue(),i);
    				
					Div divRow = new Div();
    				divRow.setStyle("display:table-row;");
    				Div divCell0 = new Div();
    				divCell0.setStyle("display:table-cell;padding-right:10px;padding-bottom:0px;white-space:nowrap;color:#888;line-height:initial;");
    				Div divCell = new Div();
    				divCell.setStyle("display:table-cell;line-height:initial;");
    				if (matchedFlag){
    					lb0.setStyle("background-color:yellow;");
    					lb.setStyle("background-color:yellow;");
    				}
    				divTb.appendChild(divRow);
    				divRow.appendChild(divCell0);
    				divRow.appendChild(divCell);
    				divCell0.appendChild(lb0);
    				divCell.appendChild(lb);
    				divCell.setAttribute("bclabel", biColumn.getLabel());

    				if (biColumn.isExcludeForMobile() || 
    						(sessionHelper.getMobileMaxCol() > 0 && colDisplayedCnt>=sessionHelper.getMobileMaxCol() && !matchedFlag)){
    					divRow.setSclass("zkbi-hide-mobile-exclude");
    					hasHideMobileExclude = true;
    				}
    				else{
    					colDisplayedCnt++;
    				}
   					//colDisplayedCnt++;
    			}
    		}

//    		if(result.getAggregateOrPivotList() != null) {
    		if(result.aggregateOrPivotSize() > 0) {
    			List <Component> lhdrs = item.getChildren();
    			Object[] vals = result.getAggregateValues(idx);
    			int n = result.aggregateOrPivotSize();
    			int ncols = listColumns.size();
    			for(int i=0;i<n;i++) {
    				if(i < vals.length && vals[i] != null) {
    					if(vals[i] instanceof Double) {
    						DecimalFormat df = new DecimalFormat("##,###,###,##0.00");
    						if (result.getAggregateOrPivotHeader() != null) {
    							AggregateRec aggRec = result.getAggregateOrPivotHeader().getAggregate(i);
    							String fmt = aggRec.getFormat(result);
    							//UniLog.log1("aggRec key:%s, name:%s, format:%s", aggRec.getKey(), aggRec.getName(result), fmt);
    							if (fmt != null)
    								df = new DecimalFormat(fmt);
    						}
    						double d;
  							d = (Double) vals[i];
//    						if(Double.isInfinite((Double) vals[i])) lc = new Listcell("");
//   						else if(Double.isNaN((Double) vals[i])) lc = new Listcell("");
    						if(Double.isInfinite(d)) lc = new Listcell("");
    						else if(Double.isNaN(d)) lc = new Listcell("");
    							else {
    								lc = new Listcell(df.format(d));
    								ZkUtil.appendStyle(lc, "text-align:right;");
    						}
//    						lc = new Listcell(df.format(vals[i]));
    					} else {
    						lc = new Listcell(""+vals[i]);
    						ZkUtil.appendStyle(lc, "text-align:left;");
    					}
    				} else{
    					lc = new Listcell("");
    				}
    				if (!useMobileList(sessionHelper.isMobile(),result)) {
    					if(aggregateOffset <= 0) item.appendChild(lc); else item.insertBefore(lc, lhdrs.get(ncols+i-aggregateOffset));
    					//lc.setParent(item);
    					colDisplayedCnt++;
    				}
    				else {
    					if (result.getAggregateOrPivotHeader() != null && !isAggregateVisible(result, result.getAggregateOrPivotHeader(), i))
    						continue;
    					Label lb0 = new Label();
    					Label lb = new Label(lc.getLabel());
    					List<String> apList = result.getAggregateOrPivotList();
    					if (apList != null && i < apList.size())
    						lb0.setValue(APLabel.of(apList.get(i)).toString());

    					Div divRow = new Div();
    					divRow.setStyle("display:table-row;");
    					Div divCell0 = new Div();
    					divCell0.setStyle("display:table-cell;padding-right:10px;padding-bottom:0px;white-space:nowrap;color:#888;line-height:initial;");
    					Div divCell = new Div();
    					divCell.setStyle("display:table-cell;line-height:initial;");
    					divTb.appendChild(divRow);
    					divRow.appendChild(divCell0);
    					divRow.appendChild(divCell);
    					divCell0.appendChild(lb0);
    					divCell.appendChild(lb);
    					if ((sessionHelper.getMobileMaxCol() > 0 && colDisplayedCnt >= sessionHelper.getMobileMaxCol())){
    						divRow.setSclass("zkbi-hide-mobile-exclude");
    						hasHideMobileExclude = true;
    					} else 
    						colDisplayedCnt++;
    				}
    			}
    		}

    		if(useMobileList(isMobile(),result)) {
    			final Div divTb1 = divTb;
    			if (hasHideMobileExclude) {
    				divTb1.appendChild(new A(){{
   						setHref("javascript:;");
    					setStyle("display:inline-block;position:absolute;opacity:.7;right:0px;bottom:0px;border:1px solid #a7a5a6;background:#f0f0f0;color:#231f20;");
    					appendChild(new I(){{
    						setSclass("fa fa-ellipsis-h");
    						setStyle("padding-left:3px;padding-right:3px;padding-top:2px;padding-bottom:2px;");
   						}});
   						addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								UniLog.log("showHideFieldDivCell event:" + event);
								Clients.evalJavaScript(
									String.format("var divTb = $('#%s');"
											+ "var he = divTb.find('.zkbi-hide-mobile-exclude');"
											+ "if (he.hasClass('zkbi-hide-mobile-exclude-cancel')){"
											+ "he.removeClass('zkbi-hide-mobile-exclude-cancel');"
											+ "}else{"
											+ "he.addClass('zkbi-hide-mobile-exclude-cancel');"
											+ "}", divTb1.getUuid()));
							}
   						});
    				}});
    			}
    			lc =  new Listcell();
    			divTb.setParent(lc);
    			lc.setParent(item);
    		}

    		if(!useMobileList(isMobile(),result)) {
    			lc = new Listcell();
    			/*
    			Div dv = new Div();
    			Label lb = new Label("HAHA");
    			lb.setParent(dv);
    			dv.setHflex("1");
    			dv.setParent(lc);
    			*/
    			lc.setParent(item);
    		}
    		item.setAttribute("renderidx", idx);

    }
    public void renderOneRecord(Listitem item, Object trStat, int p_idx,Vector listColumns,final BiResult result) throws Exception {
    	//UniLog.log("renderOneRecord " + p_idx);
    	int idx = getTrIdxByObj(listModelList, trStat, p_idx);
    	Object ts = trStat;
    	if (ts instanceof TrStatFilter){
    		ts = ((TrStatFilter)ts).trStatIdx;
    	}
    	//ZkUtil.addDragAndDrop(item); //for test drag and drop only, remove it later
  		if(action.equals("add") || action.equals("update")) return; // temporary solution for acction = add, other the biResult CellCollectio value is wrong because of loadOneRecV
    	if(result.loadOneRecV(idx)) {
    		renderOneRecord_real(item, trStat,listColumns,result,idx,ts);
    		item.setAttribute("renderidx", idx);

    		boolean visibleFlag = true;
    		if(showModified) {
    			if( (!result.isMarkedDelete(ts)) && (!result.isMarkedUpdate(ts))) {
    				visibleFlag = false;
    			}
    		}
    		if(hideDeleted && result.isMarkedDelete(ts)) visibleFlag = false;
    		if(visibleFlag) {
    			if (useMobileList(isMobile(),result)){
    				if (!isWidget())
    					item.addEventListener(Events.ON_CLICK, itemClickListener);
    			}
    			/*
    			//andrew200708 not require to listen double click event
    			else{
    				item.addEventListener(Events.ON_DOUBLE_CLICK, itemClickListener);
    			}
    			*/
    		}
    		item.setVisible(visibleFlag);
    	} 
    	else {
    		item.setLabel("Record Not Found");
    	}
    }
    
    void setBatchModeToogleButton(boolean p_sw,BiResult result) {
		if(p_sw){
			if (listbox.getListhead().getChildren().size() >= 1){
				Clients.showNotification(sessionHelper.getLabel("First, select record(s) for maintain"), "info", listbox.getListhead().getChildren().get(0),"end_center", 5000, true); 
			}
			
   			//batchModeToggleButton.setIconSclass("z-icon-edit z-icon-2x");
   			batchModeToggleButton.setImage("images/icons/zkweb/091-switch-1-25x25.png");
   			batchModeToggleButton.setSclass("narrowtoolbarbutton zkbi-active-toolbarbutton");
   			batchModeToggleButton.setTooltiptext(sessionHelper.getTtLabel("Exit Maintenance Mode"));
   			batchModeToggleButton.setAttribute("BATCH_MODE_STATUS", "Y");
   			if(!getSessionHelper().useJxFormG2()) {
   				batchActionConfirmBar.setVisible(true);
   			}
//			tbSearchBox.setVisible(true);
//			cbSearchBox.setVisible(true);
//			searchDiv.setVisible(true);
		} else {
   			//batchModeToggleButton.setIconSclass("z-icon-external-link z-icon-2x");
   			batchModeToggleButton.setImage("images/icons/zkweb/092-switch-25x25.png");
   			batchModeToggleButton.setSclass("narrowtoolbarbutton");
   			batchModeToggleButton.setTooltiptext(sessionHelper.getTtLabel("Enter Maintenance Mode"));
   			batchModeToggleButton.setAttribute("BATCH_MODE_STATUS", "N");
   			if(!getSessionHelper().useJxFormG2()) {
   				batchActionConfirmBar.setVisible(false);
   			}
//			tbSearchBox.setVisible(true);
//			cbSearchBox.setVisible(true);
//			searchDiv.setVisible(true);
		}
    }
    void setBatchActionConfirmBar(boolean p_sw,BiResult result) {
    		if(getSessionHelper().useJxFormG2()) {
    			batchActionBar.setVisible(p_sw);
    		} else {
    			batchActionConfirmBar.setVisible(p_sw);
    		}
    }
    void setActionConfirmBar(boolean p_sw,BiResult result) {
   			actionBar.setVisible(p_sw);
    }
    
//    public class MyModel extends ListModelList {
//        public MyModel(List list) {
//            super(list);
//        }
//        public void refreshList(int p_from,int p_to) {
//            fireEvent(org.zkoss.zul.event.ListDataEvent.CONTENTS_CHANGED, p_from, p_to);
//        }
//    }   
    
    void setMultiSelectMode(boolean p_sw,BiResult result)
    {
    	if(p_sw == multiSelect) return;
    	multiSelect = p_sw;
		listModelList.setMultiple(multiSelect);
		listbox.setMultiple(multiSelect);
		listbox.setCheckmark(multiSelect); //HAHA this line has sth wrong
		setSelectIdx(-1,null);
		if(!multiSelect) {
			lastSelectIdx = -1;
		} else {
			if(listbox.getSelectedCount() > 1) {
				listbox.clearSelection();
				lastSelectIdx = -1;
			} else if(listbox.getSelectedCount() <= 0) {
				lastSelectIdx = -1;
			} else {
				lastSelectIdx = listbox.getSelectedIndex();
			}
		}
		resizeListbox();
		listModelList.clear();
		listModelList.addAll(result.getResultStat());
//    	listbox.setModel(listModelList);
//    	((MyModel) listModelList).refreshList(-1,100);

		doSearch(true, false);
    }
    
    /* This method should obsolete and should not be used, only remain to make compatible with JxWipTimeline , which is also a obsoleted class, no time to verify */
    public void setMultiSelect(boolean p_sw,BiResult result)
    {
    	if(p_sw == multiSelect) return;
    	multiSelect = p_sw;
		listModelList.setMultiple(multiSelect);
		listbox.setMultiple(multiSelect);
		listbox.setCheckmark(multiSelect); //HAHA this line has sth wrong
		if(multiSelect){
			if (listbox.getListhead().getChildren().size() >= 1){
				Clients.showNotification(sessionHelper.getLabel("First, select record(s) for maintain"), "info", listbox.getListhead().getChildren().get(0),"end_center", 5000, true); 
			}
			
   			//batchModeToggleButton.setIconSclass("z-icon-edit z-icon-2x");
   			batchModeToggleButton.setImage("images/icons/zkweb/091-switch-1-25x25.png");
   			batchModeToggleButton.setSclass("narrowtoolbarbutton zkbi-active-toolbarbutton");
   			batchModeToggleButton.setTooltiptext(sessionHelper.getLabel("Exit Maintenance Mode"));
   			batchModeToggleButton.setAttribute("BATCH_MODE_STATUS", "Y");
	        batchActionConfirmBar.setVisible(true);
			tbSearchBox.setVisible(true);
			cbSearchBox.setVisible(true);
			searchDiv.setVisible(true);
		} else {
   			//batchModeToggleButton.setIconSclass("z-icon-external-link z-icon-2x");
   			batchModeToggleButton.setImage("images/icons/zkweb/092-switch-25x25.png");
   			batchModeToggleButton.setSclass("narrowtoolbarbutton");
   			batchModeToggleButton.setTooltiptext(sessionHelper.getTtLabel("Enter Maintenance Mode"));
   			batchModeToggleButton.setAttribute("BATCH_MODE_STATUS", "N");
	        batchActionConfirmBar.setVisible(false);
			tbSearchBox.setVisible(true);
			cbSearchBox.setVisible(true);
			searchDiv.setVisible(true);
		}
		resizeListbox();
    }

    

    protected void setupListHeader(final BiResult result,final Component comp,int p_sortIdx,boolean p_sortDesc,Listhead listhead) {
    	{
    		//andrew210304: this remove header code seems buggy. probably fail on mobile mode
	   		List <Component> lhdrs = listhead.getChildren();
	   		int n = lhdrs.size();
	   		for(int i = n-1;i>=(hasAUDColumn ? 1 : 0);i--) {
	   			Listheader lh = (Listheader) lhdrs.get(i);
	   			listhead.removeChild(lh);
	   		}
   			Listfoot lf = listbox.getListfoot();
   			if(lf != null) {
   				listbox.removeChild(lf);
   			}
    	}	
    	
    	Vector<BiColumn> listColumns = result.getListColumns();
    		int cw[];
    		int m=100;
    		int tw=100;
    		int hw=100;
    		if( !useMobileList(isMobile(),result)) {
    			cw = new int[listColumns.size()];
    			tw = 0;
    			for(int i=0;i<listColumns.size();i++) {
    				cw[i] = ((BiColumn) listColumns.get(i)).getColumnLength();
    				if(cw[i] > 60) cw[i] = 60; // maximum field length limit to 60 chars
    				tw += cw[i];
    				m = 1000;
    			}
    		} 
    		else {
    			cw = new int[1];
    			cw[0] = 100;
    			tw = 100;
    			m = 100;
    		}
//    		if(allowDragDropHeader) listhead.setDroppable("true");
    		Map tttMap = null; //a map contain tooltiptext, it's optional. it can be null
    		tttMap = (Map) listbox.getAttribute("tttMap");
    		
    		
    		for(int ii = 0;ii<listColumns.size();ii++) {
    			int i;
//    			if(columnOrderArray != null) i = columnOrderArray.get(ii) ; else i = ii;
    			i = ii;
    			if(useMobileList(isMobile(),result) && i > 0) break;
    			
    			final Listheader browser_listheader = new Listheader();
    			browser_listheader.setAlign("center");
   				BiColumn col = (BiColumn)listColumns.get(i);
    			//browser_listheader.setLabel(sessionHelper.getLabel(col));
    			browser_listheader.setLabel(ZkBiTranslateHelper.getText(sessionHelper, col.getCellFullName(), "LABEL", sessionHelper.getLabel(col)));
    			hw = m * cw[i]/tw;
    			browser_listheader.setVisible(true);
    			browser_listheader.setParent(listhead);
    			browser_listheader.setId("browser_listheader_"+(i+1));
    			if(
    					(sessionHelper.getAllowUpdatePivot() && allowDragDropHeader)
    					|| result.getView().allowMoveAndFreezeColumn()
    					) browser_listheader.setDraggable("viewColumn");
    			if(result.getView().allowMoveAndFreezeColumn()) {
    					browser_listheader.setDroppable("viewColumn");
    					browser_listheader.addEventListener(Events.ON_DROP, 
    							new HeaderDropListener(result)
    					);
    			}
//    			if(allowDragDropHeader) browser_listheader.setDroppable("pivotColumn");
    			
    			//add tooltiptext if any
    			if (tttMap != null) {
    				String tttValue = (String) tttMap.get(col.getCellLabel());
    				if (StringUtils.isNotBlank(tttValue)) {
    					browser_listheader.setTooltiptext(sessionHelper == null ? tttValue : sessionHelper.getLabel(tttValue));
    				}
    			}
    			
    			if(col.getFgColor() > 0) {
    				browser_listheader.setStyle(String.format("color:#%x;",col.getFgColor()));
    			}

    			if(col.getBgColor() > 0) {
    				browser_listheader.setStyle(String.format("background-color:#%06x;",col.getBgColor()));
    			}
   				//JxZkBiBase.addContextMenu(sessionHelper, browser_listheader,MapUtil.of("changeLabel", col));
    			if (!isWidget())
    				JxZkBiBase.addContextMenu(sessionHelper, browser_listheader,MapUtil.of("changeLabel", MapUtil.of("key",col.getCellFullName(),"defaultValue",col.getEngName())));

    			//browser_listheader.setHflex("min");
    			/*
    			int minWidth = JxZkBiBase.calPxByString(browser_listheader.getLabel()) + 50; //extra length for header button
    			JxZkBiBase.setComponentFormat(browser_listheader, listColumns.get(i), minWidth, 0, sessionHelper.getWidthByContent());
    			*/
    			
    			if ((i + 1) == p_sortIdx){ //sortIdx starting from 1
    				if (p_sortDesc)
    					setListHeaderSortDirection(browser_listheader, "descending");
    				else
    					setListHeaderSortDirection(browser_listheader, "ascending");
    			}
    			else{
   					setListHeaderSortDirection(browser_listheader, "natural");
    			}

    			browser_listheader.setSort("auto");
    			browser_listheader.setSortAscending(new ListitemComparator());
    			browser_listheader.addEventListener("onSort", 
    					new ZkBiEventListener(0){ //andrew200709: hotfix skip duplicate event check to avoid ClassCastException
    				public void onZkBiEvent(Event arg0) throws Exception {
    					UniLog.log("onSort event:"  + arg0.getName() +"," + arg0.getTarget().getId());
    					try{
    						if (arg0.getTarget().getId().startsWith("browser_listheader_") && arg0 instanceof SortEvent){
    							int colIdx = Integer.parseInt(arg0.getTarget().getId().substring(19));
    							sortSingle(result, comp, colIdx, !((SortEvent)arg0).isAscending());
    						}
    					}
    					catch(Exception ex){
    						UniLog.log(ex);
    					}
    					arg0.stopPropagation();
    				}
    			});
    			browser_listheader.addEventListener("onMultiSort", 
    					new ZkBiEventListener(0){ //andrew200709: hotfix skip duplicate event check to avoid ClassCastException
    				public void onZkBiEvent(Event arg0) throws Exception {
    					UniLog.log("onMultiSort event:"  + arg0.getName() +"," + arg0.getTarget().getId());
    					try{
    						if (arg0.getTarget().getId().startsWith("browser_listheader_") && arg0 instanceof SortEvent){
    							int colIdx = Integer.parseInt(arg0.getTarget().getId().substring(19));
    							sortMulti(result, comp, colIdx, !((SortEvent)arg0).isAscending());
    						}
    					}
    					catch(Exception ex){
    						UniLog.log(ex);
    					}
    					arg0.stopPropagation();
    				}
    			});
    			browser_listheader.setAttribute("ma_bicolumn", (BiColumn) listColumns.get(i));
    			/*final Menuitem menuitem = new Menuitem();
    			menuitem.setParent(menupopup);
    			menuitem.setLabel(browser_listheader.getLabel());
    			menuitem.setCheckmark(true);
    			menuitem.setAttribute("ma_type", "show_column");
    			menuitem.setAttribute("ma_listheader", browser_listheader);
    			menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						browser_listheader.setVisible(!menuitem.isChecked());
						refreshSortIcon();
					}
    			});*/
    		}
//    	if(!isMobile()) {
//    			Listheader lastHeader = new Listheader();
//    			lastHeader.setId("__lastHeader");
//    			lastHeader.setHflex("1");
//    			lastHeader.setParent(listhead);
//    	}
	
    	{

//    		Listhead listhead = (Listhead) listbox.query("Listhead");
    		
    		List <Component> lhdrs = listhead.getChildren();
    		baseHeaderCount = lhdrs.size();
    		
//    		Auxhead ah = new Auxhead();
//    		ah.setZclass("z-listbox-header");
//    		Auxheader ahdr = new Auxheader();
//    		ahdr.setZclass("z-listbox-header");
//    		ahdr.setColspan(baseHeaderCount);
//    		ahdr.appendChild(new Label("HAHA"));
//    		ah.appendChild(ahdr);
//    		listbox.insertBefore(ah,listhead);

    		/*
    		for(int i=0;i<5;i++) {
    			Listheader browser_listheader = new Listheader();
    			browser_listheader.setAlign("center");
    			browser_listheader.setLabel("Subheader " + i);
    			browser_listheader.setParent(listhead);
    			browser_listheader.setId("subhead_"+i);
    			
    			browser_listheader.setHflex("min");
    		}
    		*/
    		setAggregateAndPivotHeaders(result,null,null);
    	}
    	visibleCols( conditionPresetListbox.getSelectedItem().getValue() , comp,result);
	
} 
    
    protected void createColumnOrderArray() {
    	
    }
    public void buildBrowserWindow(final BiResult result,final Component p_listRoot, int p_sortIdx, boolean p_sortDesc){
    	if(StringUtils.equalsIgnoreCase(this.getURLParam("mode"),"list")) {
    		UniLog.log("List Mode");
    		masterWin.setHeight(null);
    		masterWin.setWidth(null);
    		masterWin.setVflex("1");
    		masterWin.setHflex("1");
    		masterWin.setZclass("abc");
    	}
    	if (!isMobile()){
    		if(!StringUtils.equalsIgnoreCase(this.getURLParam("mode"),"list")) {
    		masterWin.setHeight("650px");
    		}
    		if(useColumnOrderArray) {
    			createColumnOrderArray();
    		}
    	}
    	if( masterWin instanceof Window) {
    		
    	}
    	if (masterWin instanceof Window && ((Window) masterWin).getMinheight() <= 0){
    		if (!isMobile()){
    			((Window) masterWin).setMinheight(400);
    		}
    	}
    	masterWin.setSclass("zkbi-main-window");
    	if (isWidget()) {
    		ZkUtil.appendSclass(masterWin, "zkbi-widget-window");
    	}
    	
   		if (masterWin.getWidth() == null || masterWin.getWidth().trim().equals("")){
    		if(!StringUtils.equalsIgnoreCase(this.getURLParam("mode"),"list")) {
   			if (isMobile()){
    			masterWin.setWidth("100%");
    		}
    		else{
    			masterWin.setWidth("7680px"); //1920 x 4 
    		}
    		}
   		}
   		

    	if(masterWin.hasFellow("zkbiListPanel")) {
    		zkbiListPanel = (HtmlBasedComponent) masterWin.getFellow("zkbiListPanel");
    		customLayout = true;
    	} else {
    		zkbiListPanel = new Div();
    		masterWin.appendChild(zkbiListPanel);
    	}
    	if(masterWin.hasFellow("zkbiListTop")) {
    		zkbiListTop = masterWin.getFellow("zkbiListTop");
    	} else  {
    		zkbiListTop = zkbiListPanel;
    	}
    	/*	
   		if(useCustPaginal) {
   			searchDiv = new Hlayout();
   			((Hlayout) searchDiv).setValign("middle");
   		} else {
   			searchDiv = new Div();
   		}
   		*/
		searchDiv = new Div();
		if(masterWin.hasFellow("browser_listbox")) {
			listbox = (Listbox) masterWin.getFellow("browser_listbox");
		} 

		else {
			listbox = new Listbox();
			//listbox.setRenderdefer(0);  //test render refer
			listbox.setId("browser_listbox");
		}
		listbox.setEmptyMessage(sessionHelper.getLabel("No Record"));
		searchDiv.setHflex("1");

		if(useMobileList(isMobile(),result)) {
			Box footerDiv = new Vbox();
			footerDiv.setAlign("center");
			footerDiv.setWidth("100%");
			footerDiv.appendChild(searchDiv);
			if (isWidget())
				footerDiv.setVisible(false);
			zkbiListPanel.appendChild(footerDiv);
			zkbiListPanel.appendChild(listbox);
		} else {
			Box footerDiv = new Hbox();
			footerDiv.setAlign("center");
			footerDiv.setWidth("100%");
			footerDiv.setSclass("z-paging");	
			footerDiv.appendChild(searchDiv);
			if (isWidget())
				footerDiv.setVisible(false);
			zkbiListPanel.appendChild(listbox);
			if(sessionHelper.getNewButtonPanelLayout()){
				zkbiListPanel.insertBefore(footerDiv,listbox);
			} else {
				zkbiListPanel.appendChild(footerDiv);
			}
		}

    		
		ZkUtil.addSclass(listbox, "zkbi-main-listbox");
		if (isMobile()) {
			ZkUtil.addSclass(listbox, "zkbi-main-listbox-mobile");
		}
		ZkUtil.addSclass(listbox, "basictour_s1");
    	
    	listbox.setWidth("100%");
    	/*
    	if ("css".equals(masterWin.getAttribute("listboxHeight"))){
  			UniLog.log("do not set listbox height");
    	}
    	else{
    		listbox.setHeight("400px"); //listbox height controlled by css
    	}
    	*/
    	/*
    	if (isMobile()){
//    		listbox.setMold("paging");
//    		masterWin.setSizable(false);
//    		listbox.setPageSize(20);
//    		listbox.setAutopaging(false);
//    		listbox.setRows(20);
    		
    		int lh =sessionHelper.getScreenHeight()-240;
    		if(lh < 300) lh = 300;
    		listbox.setHeight(""+lh+"px");
    		
    	}
    	else{
    		if (sessionHelper.isPagingMode()){
    			listbox.setMold("paging");
    			listbox.setAutopaging(true);
//    			//andrew210511 this will break the css code, zkbi-main-listbox only apply to listbox
//    			if(screenLayoutG3) {
//    				ZkUtil.removeSclass(listbox, "zkbi-main-listbox");
//    				ZkUtil.addSclass(zkbiListPanel, "zkbi-main-listbox");
//    			}
    		}
    		else{
    			//listbox.setAttribute("org.zkoss.zul.listbox.rod", "true"); //rod need EE license
    			//listbox.setAttribute("scope","page");  //rod need EE license
    			ZkUtil.removeSclass(listbox, "zkbi-main-listbox");
//    			if(screenLayoutG3) {
//    				int lh =sessionHelper.getScreenHeight()-240;
//    				if(lh < 300) lh = 300;
//    				listbox.setHeight(""+lh+"px");
//    			}
    		}
    		//masterWin.setSizable(true);  //andrew200723: avoid strange effect when dragging action button
    		if (listbox.getPagingChild() != null){
    			listbox.getPagingChild().setAutohide(false); //avoid paging bug
    		}
    	}
    	*/
    	
    	if (!useMobileList(isMobile(),result) && !isWidget()) {
    		listbox.setMold("paging");
	    	listbox.setAutopaging(true);
	    		
	   		//use custom paginal
	  		listPaging = new Paging();
	  		listPaging.setSclass("listpaging");
	   		listPaging.setHflex("min");
	   		listPaging.setStyle("float:right");
	   		listPaging.setParent(searchDiv);
	   		listPaging.setDetailed(true);
	   		if (sessionHelper.getPageSize() > 0) {
	   			listbox.setAutopaging(false);
	   			listPaging.setPageSize(sessionHelper.getPageSize());
	   		}
	   		listbox.setPaginal(listPaging);
	    	if (listbox.getPagingChild() != null){
	    		listbox.getPagingChild().setAutohide(false); //avoid paging bug
	    	}
    	}
    	
    	
    	masterWin.addEventListener("onSize", new ZkBiEventListener() {
    		public void onZkBiEvent(Event event) throws Exception {
    			resizeListbox();
    	    }
    	});
    	
    	//for test send large data from client side. It affected by server.xml maxPostSize (default 2097152 2M)
    	//step to test: run testLargeData() from console.
    	masterWin.addEventListener("onTestLargeData", new ZkBiEventListener() {
    		public void onZkBiEvent(Event event) throws Exception {
    			UniLog.log1("called: leng:%s", NumberFormat.getIntegerInstance().format(event.getData().toString().length()));
    	    }
    	});
    	
    	//for obtain browserWinId (experimental)
       	masterWin.addEventListener("onSetBrowserWinId", new ZkBiEventListener() {
    		public void onZkBiEvent(Event event) throws Exception {
    			UniLog.log1("called: event:%s data:%s", event.toString(), event.getData());
    	    }
    	});

    	//for connect Js Client
       	masterWin.addEventListener("onConnectJsClient", new ZkBiEventListener() {
    		public void onZkBiEvent(Event event) throws Exception {
    			UniLog.log1("called: event:%s data:%s", event.toString(), event.getData());
	 			Clients.evalJavaScript("connectJsClient()");
	     	}
    	});

    	//for close iframe parent window
       	masterWin.addEventListener("onCloseParentWindow", new ZkBiEventListener() {
    		public void onZkBiEvent(Event event) throws Exception {
    			UniLog.log1("called: event:%s data:%s", event.toString(), event.getData());
    			if (expWin.isVisible()){
		 			Events.postEvent("onClose", expWin, null);
		 			return;
	 			}
	 			if (detailForm != null && detailForm.isFormVisible()){
		 			detailForm.doClose(true, true);
		 			return;
	 			}
	 			Clients.evalJavaScript("closeParentWindow()");
	     	}
    	});
    	
    	listbox.addEventListener(Events.ON_OK,
    		new ZkBiEventListener() {
    		    public void onZkBiEvent(Event event) throws Exception {
    		    	if(multiSelect) {
    		    		UniLog.log("170921 listbox ok , multiselect on selected item " + listbox.getSelectedIndex());
    		    	} else {
    		    		UniLog.log("170921 listbox ok , multiselect off selected item " + listbox.getSelectedIndex());
        				java.util.Set selection = listModelList.getSelection();
	        			if(selection.size() == 1) {
	        				Object o = selection.iterator().next();
	        				int idx = getTrIdxByObj(listModelList, o, -1);
    						result.fetchOneRecV(idx);
    						doBrowseItemSelected(masterWin, result);
	        			}
    		    	}
    		    };
    		}
    	);
    	itemClickListener = new ZkBiEventListener() {
    		    public void onZkBiEvent(Event event) throws Exception {
    		    	if(multiSelect) {
    		    		UniLog.log("multiselect mode, ignore click action");
    		    		showWarnMsg(sessionHelper.getLabel("Batch mode not allowed to select record"));
    		    		return;
    		    	} 
    		    	//round 1: obtain trStat from attribute (handle open toolbar button)
					Object trStat = (Object) event.getTarget().getAttribute("trStat");
					
					//round 2: obtain trStat from selected index
					if (trStat == null){
    		    		int selectedIdx = listbox.getSelectedIndex();
    		    		if(selectedIdx >= 0 && selectedIdx == getSelectIdx()) {
    		    			java.util.Set selection = listModelList.getSelection();
    					 	trStat = selection.iterator().next();
    		    		}
    				}
					if (trStat == null){
    		    		showErrMsg(sessionHelper.getLabel("Unable to obtain record index"));
    		    		UniLog.log("trStat is null");
						return;
					}
					int trIdx = getTrIdxByObj(listModelList, trStat, -1);
					int listIdx = getListIdxByObj(listModelList, trStat); //remark: for search, trIdx != listIdx; 
					if (trIdx < 0 || listIdx < 0){
    		    		showErrMsg(sessionHelper.getLabel("Unable to obtain record index"));
    		    		UniLog.log("invalid record index");
						return;
					}
					listbox.setSelectedIndex(listIdx); //highlight currect record
					
					//load record
   					if(!result.fetchOneRecV(trIdx)) {  
    		    		showErrMsg(sessionHelper.getLabel("Load record fail"));
   						Messagebox.show(sessionHelper.getLabel("Fetch Record Error") +  ": " + result.getLastErrorMessage());
   						return;
   					}
   					doBrowseItemSelected(masterWin, result);
						
    				if (isMobile()){ //dirty trick: clear selected row to avoid double highlight
//    					listbox.setSelectedIndex(-1);
    				}
    		    }
    	};
    	listbox.addEventListener(Events.ON_SELECT,
    		new ZkBiEventListener() {
    		    public void onZkBiEvent(Event event) throws Exception {
    		    	if(multiSelect) {
    		    		UniLog.log("170921 listbox selected, multiselect on selected item " + listbox.getSelectedIndex());
    		    	} else {
    		    		UniLog.log("170921 listbox selected, multiselect off selected item " + listbox.getSelectedIndex());
    		    		lastSelectIdx = getSelectIdx();
						setSelectIdx(listbox.getSelectedIndex(),result);
 						if(getSelectIdx() != lastSelectIdx) {
 							if(getSelectIdx() >= 0) {
 								Listitem li = listbox.getItemAtIndex(getSelectIdx());
 								List<Component> lcs = li.getChildren();
 								if(lcs.size() > 0) {
 									Listcell lc = (Listcell) lcs.get(0);
 									List<Component> ccs = lc.getChildren();
 									if(ccs.size() > 0 && ccs.get(0) instanceof Toolbarbutton){
 										ccs.get(0).setVisible(true);
 									}
 								}
 							}
 							if(lastSelectIdx >= 0) {
 								Listitem li = listbox.getItemAtIndex(lastSelectIdx);
 								List<Component> lcs = li.getChildren();
 								if(lcs.size() > 0) {
 									Listcell lc = (Listcell) lcs.get(0);
 									List<Component> ccs = lc.getChildren();
 									/*
 									if(ccs.size() > 0 && ccs.get(0) instanceof Toolbarbutton){
 										ccs.get(0).setVisible(false);
 									}
 									*/
 								}
 							}
 						}
    		    	}
    		    };
    		}
    	);
    	
    	buildAdvSearchFooter(result);
    	final Listhead listhead = new Listhead();
    	listhead.setId("browser_listhead");
    	listhead.setSizable(true);
    	listhead.setParent(listbox);
    	listhead.setMenupopup("menua");
    	listhead.addEventListener("onClearSort", new ZkBiEventListener<Event>(){
			@Override
			public void onZkBiEvent(Event event) throws Exception {
 				try{
   					sortSingle(result, p_listRoot, 0, false);
    			}
    			catch(Exception ex){
    				UniLog.log(ex);
    			}
    			event.stopPropagation();
			}
		});
    	listhead.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>(){
			@Override
			public void onZkBiEvent(AfterSizeEvent event) throws Exception {
				UniLog.log("listhead onAfterSize " + event.getTarget() + ",width:" + event.getWidth() + ",height:" + event.getHeight() + ",data:" + event.getData());

				if(listhead.hasFellow("__lastHeader",true)) {
					Component c = listhead.getFellow("__lastHeader");
					c.invalidate();
				}
				refreshSortIcon();
			}
    	});
    	
    	final Menupopup menupopup = new Menupopup();
    	menupopup.setParent(masterWin);
    	menupopup.setId("menua");
    	menupopup.addEventListener("onOpen", new ZkBiEventListener<OpenEvent>(){
			@Override
			public void onZkBiEvent(OpenEvent event) throws Exception {
				if (!event.isOpen())
					return;
				Listheader refListheader = (Listheader) event.getReference();
				BiColumn biColumn = (BiColumn) refListheader.getAttribute("ma_bicolumn");
				String pivotHeader = (String) refListheader.getAttribute("pivot_header");
				menupopup.setAttribute("ma_ref_listheader", refListheader);
				for (Component comp : menupopup.queryAll("Menuitem")) {
					Menuitem menuitem = (Menuitem) comp; 
					String type = (String) menuitem.getAttribute("ma_type");
					if (!type.equals("show_columns")) {
						/*if (biColumn != null) {
							if (type.equals("summary")) {
								String colType = biColumn.getColumnType();
								menuitem.setDisabled(!colType.equals("serial")
									&& !colType.equals("integer")
									&& !colType.equals("money")
									&& !colType.equals("float")
									&& !colType.equals("double"));
							} else {
								menuitem.setDisabled(false);
							}
						} else if (pivotHeader == null) {
							menuitem.setDisabled(true);
						}*/
						if(!type.equals("batchupdate")) {
							menuitem.setDisabled(biColumn == null && pivotHeader == null);
						} else {
							menuitem.setDisabled(biColumn == null || !biColumn.allowBatchUpdate());
						}
					} 
				}
			}
    	});
    	{
	    	Menuitem menuitem = new Menuitem();
	    	menuitem.setParent(menupopup);
	    	menuitem.setLabel(sessionHelper.getLabel("Sort Ascending"));
	    	menuitem.setImage("~./zul/img/grid/menu-arrowup.png");
	    	//menuitem.setIconSclass("z-icon-sort-alpha-asc");
	    	menuitem.setAttribute("ma_type", "sort");
	    	menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					Listheader refListheader = (Listheader) menupopup.getAttribute("ma_ref_listheader");
					Events.postEvent(new ColumnSortEvent("onSort", refListheader, true));
				}
	    	});
	    	menuitem = new Menuitem();
	    	menuitem.setParent(menupopup);
	    	menuitem.setLabel(sessionHelper.getLabel("Sort Descending"));
	    	//menuitem.setIconSclass("z-icon-sort-alpha-desc");
	    	menuitem.setImage("~./zul/img/grid/menu-arrowdown.png");
	    	menuitem.setAttribute("ma_type", "sort");
	    	menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					Listheader refListheader = (Listheader) menupopup.getAttribute("ma_ref_listheader");
					Events.postEvent(new ColumnSortEvent("onSort", refListheader, false));
				}
	    	});
	    	Menu menu = new Menu();
	    	menu.setParent(menupopup);
	    	menu.setLabel(sessionHelper.getLabel("Sort Multicolumn"));
	    	menu.setIconSclass("z-icon-sort");
	    	menu.setAttribute("ma_type", "sort");
	    	{
	    		Menupopup submpopup = new Menupopup();
	    		submpopup.setParent(menu);
	    		Menuitem submitem = new Menuitem();
	    		submitem.setParent(submpopup);
		    	submitem.setLabel(sessionHelper.getLabel("Ascending"));
		    	submitem.setImage("~./zul/img/grid/menu-arrowup.png");
		    	submitem.setAttribute("ma_type", "sort");
		    	submitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Listheader refListheader = (Listheader) menupopup.getAttribute("ma_ref_listheader");
						Events.postEvent(new SortEvent("onMultiSort", refListheader, true));
					}
		    	});
	    		submitem = new Menuitem();
	    		submitem.setParent(submpopup);
	    		submitem.setLabel(sessionHelper.getLabel("Descending"));
		    	submitem.setImage("~./zul/img/grid/menu-arrowdown.png");
		    	submitem.setAttribute("ma_type", "sort");
		    	submitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Listheader refListheader = (Listheader) menupopup.getAttribute("ma_ref_listheader");
						Events.postEvent(new SortEvent("onMultiSort", refListheader, false));
					}
		    	});
	    		submitem = new Menuitem();
	    		submitem.setParent(submpopup);
	    		submitem.setLabel(sessionHelper.getLabel("Clear"));
	    		submitem.setIconSclass("z-icon-eraser");
		    	submitem.setAttribute("ma_type", "sort");
		    	submitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Events.postEvent(new Event("onClearSort", listhead, null));
					}
		    	});
	    	}
    		menuitem = new Menuitem();
	    	menuitem.setParent(menupopup);
	    	menuitem.setLabel(sessionHelper.getLabel("Summary"));
	    	menuitem.setIconSclass("z-icon-info");
	    	menuitem.setAttribute("ma_type", "summary");
	    	menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					/*int resultSize = result.getResultStat().size();
					if (resultSize == 0) {
						Messagebox.show("record not found");
						return;
					}*/
					Listheader refListheader = (Listheader) menupopup.getAttribute("ma_ref_listheader");
					BiColumn biColumn = (BiColumn) refListheader.getAttribute("ma_bicolumn");

					int pivotIdx = -1;
					String pivotHeader = (String) refListheader.getAttribute("pivot_header");
					String aop = (String) refListheader.getAttribute("aggregate_or_pivot");
					if (aop != null)
						pivotHeader = aop;
					List<String> aggregateOrPivotList;
					if(result.getAggregateOrPivotHeader() != null) {
						aggregateOrPivotList = result.getAggregateOrPivotHeader().getAggregateOrPivotList();
					} else {
						aggregateOrPivotList = result.getAggregateOrPivotList();
					}
					if (aggregateOrPivotList != null && pivotHeader != null) {
						pivotIdx = aggregateOrPivotList.indexOf(pivotHeader);
						/*
						for(int i=0;i<aggregateOrPivotList.size();i++) {
							if(aggregateOrPivotList.get(i).toString().equals(pivotHeader)) {
								pivotIdx = i;
								break;
							}
						}
						*/
					}
						
					double total = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, avg = 0;
					/*for (int i = 0; i < resultSize; i++) {
						result.loadOneRecV(i);
						double qty = result.getCell(biColumn.getLabel()).getDouble();
						total += qty;
						min = Math.min(qty, min);
						max = Math.max(qty, max);
					}*/

					String colType = biColumn != null ? biColumn.getColumnType() : "double";
					/*
					boolean isNumericColType = colType.equals("serial") || colType.equals("integer") || colType.equals("money") 
												&& colType.equals("float") || colType.equals("double");
					*/
					boolean isNumericColType = StringUtils.equalsAny(colType, "serial", "integer", "money", "float", "double");  //andrew220720 fix float typo

					boolean isFloatColType = colType.equals("money") || colType.equals("float") || colType.equals("double");

					DecimalFormat dfDefault = new DecimalFormat("#0.00");

					int resultSize = 0;
					DecimalFormat df = null;
					Set<Object> uniqueList = new HashSet<Object>();
       				for (int i = 0; i < listModelList.size(); i++) {
       					int idx = getTrIdxByObj(listModelList, listModelList.get(i), -1);
       					if (idx >= 0) {
       						boolean hasRecord = false;
      						Double qty = null;
       						if (biColumn != null) {
       							result.loadOneRecV(idx);
      							ColumnCell cc = result.getCell(biColumn.getLabel());
       							if (isNumericColType) {
       								df = cc.getDecFormat();
       								qty = cc.getDouble();
       								uniqueList.add((isFloatColType && df != null) ? df.format(qty) : qty);
								}
       							else
       								uniqueList.add(cc.getString());
       							hasRecord = true;
       						}
       						else if (pivotIdx >= 0) {
       							Object[] avs = result.getAggregateValues(idx);
       							if (avs != null && pivotIdx < avs.length) {
       								if(avs[pivotIdx] instanceof Double) {
       									qty = (Double) avs[pivotIdx];
       									if (!Double.isNaN(qty)) {
       										uniqueList.add(dfDefault.format(qty));
       										hasRecord = true;
       									}
       								}
       							}
       						}
       						if (hasRecord) {
       							if (qty != null) {
       								total += qty;
       								min = Math.min(qty, min);
       								max = Math.max(qty, max);
       							}
       							resultSize++;
       						}
       					}
       				}
					if (resultSize == 0) {
						Messagebox.show(sessionHelper.getLabel("Record not found"));
						return;
					}
       				if (df == null)
       					df = dfDefault;
					avg = total / resultSize;
					if (isNumericColType) {
						if (Double.isInfinite(min))
							min = 0.0;
						if (Double.isInfinite(max))
							max = 0.0;
						Messagebox.show(
							isFloatColType ?
							//String.format("Avg: %s\nMin: %s\nMax: %s\nSum: %s\nCount: %d\nUnique Count: %d", df.format(avg), df.format(min), df.format(max), df.format(total), resultSize, uniqueList.size()) :
							ZkUtil.joinStringLabel(sessionHelper, "\n", "Avg: %s", df.format(avg), "Min: %s", df.format(min), "Max: %s", df.format(max), "Sum: %s", df.format(total), "Count: %d", resultSize, "Unique Count: %d", uniqueList.size()) :
							//String.format("Avg: %f\nMin: %d\nMax: %d\nSum: %d\nCount: %d\nUnique Count: %d", avg, (int) min, (int) max, (int) total, resultSize, uniqueList.size()),
							ZkUtil.joinStringLabel(sessionHelper, "\n", "Avg: %f", avg, "Min: %d", (int)min, "Max: %d", (int)max, "Sum: %d", (int)total, "Count: %d", resultSize, "Unique Count: %d", uniqueList.size()),
							sessionHelper.getLabel("Summary"),
							Messagebox.OK,
							Messagebox.INFORMATION
						);
					}
					else {
						Messagebox.show(
							//String.format("Count: %d\nUnique Count: %d", resultSize, uniqueList.size()),
							ZkUtil.joinStringLabel(sessionHelper, "\n", "Count: %d", resultSize, "Unique Count: %d", uniqueList.size()),
							sessionHelper.getLabel("Summary"),
							Messagebox.OK,
							Messagebox.INFORMATION
						);
					}
				}
	    	});
	    	if(getSessionHelper().useJxFormG2() && result.getView().allowBatchUpdate(getSessionHelper())) {
	    		menuitem = new Menuitem();
	    		menuitem.setParent(menupopup);
	    		menuitem.setLabel(sessionHelper.getLabel("Batch Update"));
	    		menuitem.setIconSclass("z-icon-pencil");
	    		menuitem.setAttribute("ma_type", "batchupdate");
	    		menuitem.setDisabled(true);
	    		menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
//   	ListModelList batchActionFieldListModel;
						Listheader refListheader = (Listheader) menupopup.getAttribute("ma_ref_listheader");
						BiColumn biColumn = (BiColumn) refListheader.getAttribute("ma_bicolumn");
						if(getSessionHelper().useJxFormG2()) {
							result.clearCurrentRec();
							result.invalidateLoadCache();
						}
						for(int i=0;i<batchActionFieldListModel.size();i++) {
							if(!(batchActionFieldListModel.get(i) instanceof BiColumn)) continue;
							BiColumn bc = (BiColumn) batchActionFieldListModel.get(i);
							if(bc == biColumn) {
								HashSet hs = new HashSet();
								hs.add(bc);
								batchActionFieldListModel.setSelection(hs);
//								batchActionFieldList.setSelectedIndex(i);
								Events.sendEvent(batchActionFieldList, new Event(Events.ON_SELECT, batchActionFieldList));
								break;
							}
						}
						setMultiSelectMode(true,result);
						setBatchActionConfirmBar(true,result);
						setActionConfirmBar(false,result);
//						Events.echoEvent("onClick", batchModeToggleButton, null);
					}
	    		});
	    	}
	    	
	    	new Menuseparator().setParent(menupopup);
    	}
 
    	Vector<BiColumn> listColumns0 = result.getListColumns();
    	
    	if(!useMobileList(isMobile(),result)) {
    		if(hasAUDColumn) {
    			Listheader browser_listheader = new Listheader();
    			browser_listheader.setId("browser_listheader_0");
    			browser_listheader.setVisible(true);
  				//browser_listheader.setWidth("150px");
  				//browser_listheader.setWidth("100px"); //moved to setListheaderWidth
  				browser_listheader.setStyle("padding-left:16px;text-align:left;");
    			browser_listheader.setParent(listhead);
    		}
    		if(/* hasAUDColumn*/ true) {

    			batchModeToggleButton = new Toolbarbutton();
    			//batchModeToggleButton.setImage(IMG_MULTISELECT);
    			//batchModeToggleButton.setIconSclass("z-icon-external-link z-icon-2x");
    			batchModeToggleButton.setImage("images/icons/zkweb/092-switch-25x25.png");
    			batchModeToggleButton.setId("tbMultiSelect");
    			batchModeToggleButton.setTooltiptext(sessionHelper.getTtLabel("Enter Maintenance Mode"));
    			//batchModeToggleButton.setStyle("narrowtoolbarbutton");
    			batchModeToggleButton.setSclass("narrowtoolbarbutton");
   				//batchModeToggleButton.setParent(browser_listheader); //move to query bar
    			if(result.getSessionHelper().useJxFormG2()) {
    				batchModeToggleButton.setVisible(false);
    			}
    			if (condDiv != null){
    				queryBar.insertBefore(batchModeToggleButton,condDiv);
    			}
    			else{
    				queryBar.appendChild(batchModeToggleButton);
    			}

    			batchModeToggleButton.addEventListener("onClick", new ZkBiEventListener(){
    				public void onZkBiEvent(Event arg0) throws Exception {
    					UniLog.log("Toogle multiple select change to " + !multiSelect);
    					setMultiSelectMode(!multiSelect,result);
    					setBatchModeToogleButton(multiSelect,result);
//    					setMultiSelect(!multiSelect,result);
//   						setSelectIdx(-1,null);
//    					if(!multiSelect) {
//    						lastSelectIdx = -1;
//    					} 
//    					else {
//    						if(listbox.getSelectedCount() > 1) {
//    							listbox.clearSelection();
//    							lastSelectIdx = -1;
//    						} 
//    						else if(listbox.getSelectedCount() <= 0) {
//    							lastSelectIdx = -1;
//    						} 
//    						else {
//    							lastSelectIdx = listbox.getSelectedIndex();
//    						}
//    						
//    					}
//    					resizeListbox();
//        			    listModelList.clear();
//        			    listModelList.addAll(result.getResultStat());
//        			    doSearch(true, false);
    				}
    			});
    		}
    		
//    		final int frozenCount = JxZkBiBase.getFrozenCount(hasAUDColumn, sessionHelper);
    		//set frozen column
   			setFrozenColumn(frozenCount);
   			
   			if(!isMobile()) {
   				final Menuitem menuitem = new Menuitem();
   				menuitem.setParent(menupopup);
   				menuitem.setLabel(sessionHelper.getLabel("Display Column"));
   				menuitem.setIconSclass("z-icon-columns");
	   			menuitem.setAttribute("ma_type", "show_columns");
	   			menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						final Vbox vbox = new Vbox(){{
							appendChild(new Label(sessionHelper.getLabel("Please choose columns to display.")));
							final Checkbox cbAll = new Checkbox(sessionHelper.getLabel("Select All / Select None"));
							appendChild(cbAll);
							Listhead listhead = (Listhead) p_listRoot.query("#browser_listhead");
							Vector<BiColumn> listColumns1 = result.getListColumns();
							boolean isVisabledAll = true;
							for(int i=0;i<listColumns1.size();i++){
								BiColumn biColumn = listColumns1.get(i);
								Listheader listheader = (Listheader) listhead.query("#browser_listheader_" + (i+1));
								if (listheader != null && (Boolean)listheader.getAttribute("isHideForTempBiColumn") == null) {
									Checkbox cb = new Checkbox(listheader.getLabel());
									cb.setChecked((getPivotColumns() != null && getPivotColumns().contains(biColumn.getLabel())) ? true : listheader.hasAttribute("customVisible") ? (Boolean)listheader.getAttribute("customVisible") : listheader.isVisible());
									cb.setAttribute("listheader", listheader);
									appendChild(cb);
									if (!listheader.isVisible())
										isVisabledAll = false;
									cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>(){
										@Override
										public void onZkBiEvent(CheckEvent event) throws Exception {
											boolean isCheckAll = true;
											for (Component comp : queryAll("Checkbox")) {
												Checkbox cb = (Checkbox) comp;
												Listheader lh = (Listheader) cb.getAttribute("listheader");
												if (lh != null && !cb.isChecked()) {
													isCheckAll = false;
													break;
												}
											}
											cbAll.setChecked(isCheckAll);
										}
									});
								}
							}
							List<BiColumn>aggList =  allowSelectAggregateList(result);
							if(aggList != null) {
								for(BiColumn aggCol  : aggList) {
									String aggHdr = aggCol.getEngName();
									Checkbox cb = new Checkbox(aggHdr);
									cb.setAttribute("aggcolumn", aggCol);
									appendChild(cb);
									if(hideAggregates != null && hideAggregates.indexOf(aggCol.getLabel()) >= 0) {
										cb.setChecked(false);
										isVisabledAll = false;
									} else {
										cb.setChecked(true);
									}
									cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>(){
										@Override
										public void onZkBiEvent(CheckEvent event) throws Exception {
											boolean isCheckAll = true;
											for (Component comp : queryAll("aggcolumn")) {
												Checkbox cb = (Checkbox) comp;
												BiColumn bc = (BiColumn) cb.getAttribute("aggcolumn");
												if (bc != null && !cb.isChecked()) {
													isCheckAll = false;
													break;
												}
											}
											cbAll.setChecked(isCheckAll);
										}
									});
								}
							}
							cbAll.setChecked(isVisabledAll);
							cbAll.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>(){
								@Override
								public void onZkBiEvent(CheckEvent event) throws Exception {
									UniLog.log("display column all:" + event.getTarget() + ",checked:" + event.isChecked());
									for (Component comp : queryAll("Checkbox")) {
										Checkbox cb = (Checkbox) comp;
										Listheader lh = (Listheader) cb.getAttribute("listheader");
										if (lh != null)
											cb.setChecked(event.isChecked());
										BiColumn bc = (BiColumn) cb.getAttribute("aggcolumn");
										if(bc != null) {
											cb.setChecked(event.isChecked());
										}
									}
								}
							});
						}};
						final Div div = new Div();
						div.setVflex("1");
						div.setStyle("overflow-y:auto");
						div.appendChild(vbox);
						if( result.getView().allowMoveAndFreezeColumn()) {
							Hlayout hlsp = new Hlayout();
							Label spLabel = new Label("Freeze Column At:");
							hlsp.appendChild(spLabel);
							Spinner spsp = new Spinner();
							spsp.setId("frozenCount");
							spsp.setValue(frozenCount);
							hlsp.appendChild(spsp);
							div.appendChild(hlsp);
						}
						/*
						{
							Div tSpacing = new Div();
							tSpacing.setHeight("100px");
							div.appendChild(tSpacing);
						}
						*/
						final MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(sessionHelper.getLabel("Display Column"), 
								div,
								new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
								masterWin, 
								new ZkBiEventListener<Messagebox.ClickEvent>(){
									@Override
									public void onZkBiEvent(ClickEvent event) throws Exception {
										UniLog.log("display column click:" + event.getTarget() + ",button:" + event.getButton());
										if (event.getButton() == null)
											return;
										switch (event.getButton()) {
										case OK:
											Map<String, Boolean> updatePivotMap = new HashMap<String, Boolean>();
											List<String> newAggHideColumn = null;
											for (Component comp : vbox.queryAll("Checkbox")) {
												Checkbox cb = (Checkbox) comp;
												Listheader lh = (Listheader) cb.getAttribute("listheader");
												if (lh != null) {
													BiColumn biColumn = (BiColumn) lh.getAttribute("ma_bicolumn");
													Set<String> pivotColumns = getPivotColumns();
													if (biColumn != null && pivotColumns != null) {
														String label = biColumn.getLabel();
														if (!cb.isChecked()) {
															lh.setVisible(false);
															if (pivotColumns.contains(label))
																updatePivotMap.put(label, false);
														}
														else {
															if (!lh.isVisible() && !pivotColumns.contains(label)) {
																if (biColumn.isPivot())
																	updatePivotMap.put(label, true);
																else
																	lh.setVisible(true);
															}
														}
													}
													else
														lh.setVisible(cb.isChecked());
												}
												BiColumn bc = (BiColumn) cb.getAttribute("aggcolumn");
												if(bc != null) {
													if(!cb.isChecked()) {
														if(newAggHideColumn == null) {
															newAggHideColumn = new ArrayList<String>();
														}
														newAggHideColumn.add(bc.getLabel());
													}
												}
											}
											hideAggregates = newAggHideColumn;
											//onListColumnVisibleChanged(result);
											UniLog.log1("updatePivotMap:%s", updatePivotMap);
											updatePivotColumns(result, updatePivotMap);
											if(vbox.hasFellow("frozenCount", true)) {
												Spinner spsp = (Spinner) vbox.getFellow("frozenCount", true) ;
												frozenCount = spsp.getValue();
												setFrozenColumn(frozenCount);
											}
//											if(renderHiddenColumns) {
//												Clients.resize(listbox);
//											} else {
//												listbox.setModel(listModelList);
//											}
											listbox.setModel(listModelList);
											changedDisplayListColumn(result);
											break;
										default:
											break;
										}
									}
								}
						);
						dlg.setStyle("max-height:100%");
						dlg.doHighlighted();

						vbox.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>() {
							@Override
							public void onZkBiEvent(AfterSizeEvent event) throws Exception {
								UniLog.log1("vbox event:%s width:%d, height:%d", event, event.getWidth(), event.getHeight());
								if (sessionHelper.getLargeFlag())
									dlg.setHeight((event.getHeight() + 40 + 60 + 10 + 6) + "px"); //vbox + header + bottombar + padding
								else
									dlg.setHeight((event.getHeight() + 32 + 43 + 10 + 6) + "px");
							}
						});
					}
	   			});

	   			/*menuitem.setLabel("Select All / Select None");
	   			menuitem.setCheckmark(true);
	   			menuitem.setAttribute("ma_type", "show_column_all");
	   			menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Listhead listhead = (Listhead) comp.query("#browser_listhead");
						for(int i=0;i<listColumns.size();i++){
							Listheader listheader = (Listheader) listhead.query("#browser_listheader_" + (i+1));
							if (listheader != null)
								listheader.setVisible(!menuitem.isChecked());
						}
						refreshSortIcon();
						event.stopPropagation();
					}
	   			});*/
	   			
	   			if( result.getView().allowAdhocColumn()) {
				final Menuitem adCol = new Menuitem();
				adCol.setParent(menupopup);
				adCol.setLabel(sessionHelper.getLabel("Ad Hoc Column"));
				adCol.setIconSclass("z-icon-columns");
				adCol.setAttribute("ma_type", "adhoc_columns");
				adCol.addEventListener("onClick", new ZkBiEventListener<Event>(){
	
					@Override
					public void onZkBiEvent(Event event) throws Exception {
		        			try {
		        				final ZkForm zkf1 = new ZkForm(null,"zkf/AdhocColumn.zul");
		        				final CellCollection col = new CellCollection();
		        				Cell adhocColName = col.addCell("adhocColName",new Cell(""));
		        				adhocColName.setItemList( result.getAdhocColumnList());
		        				zkf1.doModal(col,new EventListener() {
									@Override
									public void onEvent(Event arg0) throws Exception {
										if(arg0.getTarget().getId().equals("btCancel")) {
											zkf1.exitModal();
										} else if(arg0.getTarget().getId().equals("btAdd")) {
											if(col.getCellString("adhocColHdr").isEmpty()) {
												Messagebox.show(sessionHelper.getLabel("Please Enter Column Header"));
												return;
											}
											if(col.getCellString("adhocColType").isEmpty()) {
												Messagebox.show(sessionHelper.getLabel("Please Select Column Type"));
												return;
											}
											result.addAdhocColumn(
														null,
														col.getCellString("adhocColHdr"),
														col.getCellString("adhocColFormula"),
														col.getCellString("adhocColFormat"),
														col.getCellString("adhocColType"),
														col.getCellString("adhocColAgg")
														);
											resetListHeader(result);
											//refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
											biBaseRefresh(result);
											zkf1.exitModal();
										} else if(arg0.getTarget().getId().equals("btUpd")) {
										} else if(arg0.getTarget().getId().equals("btDel")) {
											if(StringUtils.isBlank(adhocColName.getString())) {
												Messagebox.show(sessionHelper.getLabel("Please Select Column Name"));
												return;
											}
										} else if(arg0.getTarget().getId().equals("btColName")) {
										} else if(arg0.getTarget().getId().equals("xxx")) {
										}
									}
		        				}
		        				);
		        			} catch (Exception ex) {
		        				UniLog.log(ex);
		        			}
						
					}
					
				});
				
			}	   			
	   			{
	   				final Menuitem resetView = new Menuitem();
					resetView.setParent(menupopup);
					resetView.setLabel(sessionHelper.getLabel("Reset View"));
					resetView.setIconSclass("z-icon-columns");
					resetView.setAttribute("ma_type", "adhoc_columns");
					resetView.addEventListener("onClick", new ZkBiEventListener<Event>(){

						@Override
						public void onZkBiEvent(Event event) throws Exception {
							// TODO Auto-generated method stub
							UniLog.log("Reset View");
							
			       			Messagebox.show(
		        					"Confirm Reset View to Default ?",
		        					"Reset View", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
		        			    public void onEvent(Event evt) throws Exception {
		        			    	if (evt.getName().equals("onOK")) {
										visibleCols(null, masterWin, result);
										resetListHeader(result);
										updatePivotColumns(result, new HashMap<String, Boolean>());
										listbox.setModel(listModelList);
		        			    	}
		        			    };
		        			});
							
							

//							MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Save Preset", masterWin, 
//									new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
//									masterWin, new ZkBiEventListener<Messagebox.ClickEvent>() {
//								@Override
//								public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
//									if (event.getButton() == Messagebox.Button.OK) {
//										visibleCols(null, masterWin, result);
//										resetListHeader(result);
//										updatePivotColumns(result, new HashMap<String, Boolean>());
//										listbox.setModel(listModelList);
//									}
//								}
//			    			});
						}
						
					});
	   			}
	   			
	   			
   			}

//			{
//				Listfoot lf = new Listfoot();
//				listbox.appendChild(lf);
//				Listfooter lfdr = new Listfooter();
//				lf.appendChild(lfdr);
//				lfdr.appendChild(new Label("HAHA"));
//			}
   			setupListHeader(result,p_listRoot,p_sortIdx,p_sortDesc,listhead);
   			
//   		{
//    		int cw[];
//    		int m=100;
//    		int tw=100;
//    		int hw=100;
//    		if(!isMobile()) {
//    			cw = new int[listColumns.size()];
//    			tw = 0;
//    			for(int i=0;i<listColumns.size();i++) {
//    				cw[i] = ((BiColumn) listColumns.get(i)).getColumnLength();
//    				if(cw[i] > 60) cw[i] = 60; // maximum field length limit to 60 chars
//    				tw += cw[i];
//    				m = 1000;
//    			}
//    		} 
//    		else {
//    			cw = new int[1];
//    			cw[0] = 100;
//    			tw = 100;
//    			m = 100;
//    		}
//    		for(int i=0;i<listColumns.size();i++){
//    			if(isMobile() && i > 0) break;
//    			
//    			final Listheader browser_listheader = new Listheader();
//    			browser_listheader.setAlign("center");
//   				BiColumn col = (BiColumn)listColumns.get(i);
//    			//browser_listheader.setLabel(sessionHelper.getLabel(col));
//    			browser_listheader.setLabel(ZkBiTranslateHelper.getText(sessionHelper, col.getCellFullName(), "LABEL", sessionHelper.getLabel(col)));
//    			hw = m * cw[i]/tw;
//    			browser_listheader.setVisible(true);
//    			browser_listheader.setParent(listhead);
//    			browser_listheader.setId("browser_listheader_"+(i+1));
//    			if(col.getFgColor() > 0) {
//    				browser_listheader.setStyle(String.format("color:#%x;",col.getFgColor()));
//    			}
//
//    			if(col.getBgColor() > 0) {
//    				browser_listheader.setStyle(String.format("background-color:#%06x;",col.getBgColor()));
//    			}
//   				//JxZkBiBase.addContextMenu(sessionHelper, browser_listheader,MapUtil.of("changeLabel", col));
//   				JxZkBiBase.addContextMenu(sessionHelper, browser_listheader,MapUtil.of("changeLabel", MapUtil.of("key",col.getCellFullName(),"defaultValue",col.getEngName())));
//
//    			//browser_listheader.setHflex("min");
//    			/*
//    			int minWidth = JxZkBiBase.calPxByString(browser_listheader.getLabel()) + 50; //extra length for header button
//    			JxZkBiBase.setComponentFormat(browser_listheader, listColumns.get(i), minWidth, 0, sessionHelper.getWidthByContent());
//    			*/
//    			
//    			if ((i + 1) == p_sortIdx){ //sortIdx starting from 1
//    				if (p_sortDesc)
//    					browser_listheader.setSortDirection("descending");
//    				else
//    					browser_listheader.setSortDirection("ascending");
//    			}
//    			else{
//    				browser_listheader.setSortDirection("natural");
//    			}
//
//    			browser_listheader.setSort("auto");
//    			browser_listheader.setSortAscending(new ListitemComparator());
//    			browser_listheader.addEventListener("onSort", 
//    					new ZkBiEventListener(0){ //andrew200709: hotfix skip duplicate event check to avoid ClassCastException
//    				public void onZkBiEvent(Event arg0) throws Exception {
//    					UniLog.log("onSort event:"  + arg0.getName() +"," + arg0.getTarget().getId());
//    					try{
//    						if (arg0.getTarget().getId().startsWith("browser_listheader_") && arg0 instanceof SortEvent){
//    							int colIdx = Integer.parseInt(arg0.getTarget().getId().substring(19));
//    							sortSingle(result, comp, colIdx, !((SortEvent)arg0).isAscending());
//    						}
//    					}
//    					catch(Exception ex){
//    						UniLog.log(ex);
//    					}
//    					arg0.stopPropagation();
//    				}
//    			});
//    			browser_listheader.addEventListener("onMultiSort", 
//    					new ZkBiEventListener(0){ //andrew200709: hotfix skip duplicate event check to avoid ClassCastException
//    				public void onZkBiEvent(Event arg0) throws Exception {
//    					UniLog.log("onMultiSort event:"  + arg0.getName() +"," + arg0.getTarget().getId());
//    					try{
//    						if (arg0.getTarget().getId().startsWith("browser_listheader_") && arg0 instanceof SortEvent){
//    							int colIdx = Integer.parseInt(arg0.getTarget().getId().substring(19));
//    							sortMulti(result, comp, colIdx, !((SortEvent)arg0).isAscending());
//    						}
//    					}
//    					catch(Exception ex){
//    						UniLog.log(ex);
//    					}
//    					arg0.stopPropagation();
//    				}
//    			});
//    			browser_listheader.setAttribute("ma_bicolumn", (BiColumn) listColumns.get(i));
//    			/*final Menuitem menuitem = new Menuitem();
//    			menuitem.setParent(menupopup);
//    			menuitem.setLabel(browser_listheader.getLabel());
//    			menuitem.setCheckmark(true);
//    			menuitem.setAttribute("ma_type", "show_column");
//    			menuitem.setAttribute("ma_listheader", browser_listheader);
//    			menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
//					@Override
//					public void onZkBiEvent(Event event) throws Exception {
//						browser_listheader.setVisible(!menuitem.isChecked());
//						refreshSortIcon();
//					}
//    			});*/
//    		}
//   		}	
    		//adjust default col width
    		adjustWidthFlag = sessionHelper.getDefWidthByContent();
	    	setListheaderWidth(result, adjustWidthFlag);
    	} 
    	else {  //for mobile
    		Listheader browser_listheader = new Listheader();
    		browser_listheader.setParent(listhead);
    	}
    	//load the record here
    	ListitemRenderer listitemRenderer = 
    			new ListitemRenderer <Object> () {
    				   public void render(Listitem item, Object trStat, int p_idx) throws Exception {
    					   renderOneRecord(item,trStat,p_idx,result.getListColumns(),result);
    				   }	
    			};
  		listbox.setItemRenderer(listitemRenderer);	
    	
  		listModelList = new ListModelList(result.getResultStat());
//  		listModelList = new MyModel(result.getResultStat());
    	listbox.setModel(listModelList);
    	if(masterWin.hasFellow("zkbiBottomPanel")) {
    		bottomPanelVbox = (Vbox) masterWin.getFellow("zkbiBottomPanel");
    	} 
    	else {
    		masterWin.appendChild(new Separator());
    		bottomPanelVbox = new Vbox();
    		bottomPanelVbox.setWidth("100%");
    		if(customLayout) {
    		if(/*!isMobile() &&*/ sessionHelper.getNewButtonPanelLayout()){
    			zkbiListPanel.getParent().insertBefore(bottomPanelVbox, zkbiListPanel);
    		} else {
    			zkbiListPanel.getParent().appendChild(bottomPanelVbox);
    		}
    		} else {
    		if(/*!isMobile() &&*/ sessionHelper.getNewButtonPanelLayout()){
    			masterWin.insertBefore(bottomPanelVbox, zkbiListPanel);
    		} else {
    			masterWin.appendChild(bottomPanelVbox);
    		}
    		}
    	}
    	//bottomPanelVbox.setWidth("800px"); //TODO: use hflex and master width define the width
    	//bottomPanelVbox.setWidth("100%"); 
    	
    	if(masterWin.hasFellow("zkbiBatchActionConfirmBar")) {
    		batchActionConfirmBar = (Hbox) masterWin.getFellow("zkbiBatchActionConfirmBar");
    	} else {
    		batchActionConfirmBar = new Hbox();
    		batchActionConfirmBar.setWidth("100%");
    		batchActionConfirmBar.setVisible(false);
    		batchActionConfirmBar.setWidths("70%,30%"); 
    		bottomPanelVbox.appendChild(batchActionConfirmBar);
    	}
    	
    	if(masterWin.hasFellow("zkbiBatchActionBar")) {
    		batchActionBar = (Hbox) masterWin.getFellow("zkbiBatchActionBar");
    	} 
    	else {
    		batchActionBar = new Hbox();
    		batchActionBar.setWidth("100%");
    		if(getSessionHelper().useJxFormG2()) {
//    			actionConfirmBar.appendChild(batchActionBar);
    			batchActionBar.setVisible(false);
    		} else {
    			batchActionConfirmBar.appendChild(batchActionBar);
    		}
    		
    	}
    	batchActionBar.setAlign("center");
    	setupBatchActionBar(listColumns0, result);
    	
    	if(masterWin.hasFellow("zkbiConfirmBar")) {
    		confirmBar = (Hbox) masterWin.getFellow("zkbiConfirmBar");
    	} else {
    		confirmBar = new Hbox();
    		confirmBar.setWidth("100%");
    		confirmBar.setPack("end");
    		confirmBar.setAlign("end");
    		batchActionConfirmBar.appendChild(confirmBar);
    	}
       	setupSaveChangeButton(result);
    	
    	if(masterWin.hasFellow("zkbiActionConfirmBar")) {
    		actionConfirmBar = (Div) masterWin.getFellow("zkbiActionConfirmBar");
    	} else {
    		actionConfirmBar = new Div();
    		actionConfirmBar.setStyle("display:flex;display:-webkit-flex;align-items:center;-webkit-align-items:center;");
    		actionConfirmBar.setHflex("1");
    		bottomPanelVbox.appendChild(actionConfirmBar);
    	}
    	if(masterWin.hasFellow("zkbkActionBar")) {
    		actionBar = (Hlayout) masterWin.getFellow("zkbkActionBar");
    	} else {	
    		actionBar = new Hlayout();
    		actionBar.setWidth("0");
    		actionBar.setStyle("overflow-x:auto;flex:auto;-webkit-flex:auto;");
    		actionConfirmBar.appendChild(actionBar);
    	}
    		if(batchActionBar != null && getSessionHelper().useJxFormG2()) {
    			actionConfirmBar.appendChild(batchActionBar);
    		} else {
//    			batchActionConfirmBar.appendChild(batchActionBar);
    		}
    	
   		if(!isMobile()) actionConfirmBar.appendChild(queryBar);
    	actionBar.setSclass("basictour_s3 zkbi-actionbar");
    	
        if(!result.allowDetail()) hasDetailButton = false;
	    setupDetailButton(result);
	    if(hasAUDColumn) {
	    	setupAddButton(result);
	    }
        if (hasAUDColumn){
        	setupDeleteButton(result);
        }
	    if (sessionHelper.getAllowGeneralReport()) {
    		setupGeneralReportButton(result);
	    }
	    if (sessionHelper.getAllowPrintButton())
	    	setupPrintButton(result);
	    if (sessionHelper.getAllowDataAnalysis())
	    	setupDataAnalysisButton(result);
	    
	    
	    //setup biview, import/export preset
	    if (sessionHelper.getAllowUpdateCustomBiView())
	    	setupUpdateBiViewButton(result);
	    if (sessionHelper.getAllowImportPreset())
	    	setupImportPresetButton(result, p_listRoot);
	    if (sessionHelper.getAllowExportPreset())
	    	setupExportPresetButton(result);
	    if (btnBiView != null || btImportPreset != null || btExportPreset != null) {
	    	Button bgMaint = new ZkBiButtonGroup(sessionHelper).addButton(btnBiView).addSeparator().addButton(btImportPreset).addButton(btExportPreset).setId("bgPreset").setLabel(sessionHelper.getLabel("Maintenance")).build();
	        bgMaint.setImage("images/icons/zkweb/047-gears-25x25.png");
	        abHelper.addButton(bgMaint, "fa-folder-open-o");
	    }
	    
	    //setup import/export button
   		//setupImportButton(result);
	    if (sessionHelper.getAllowImportG1()) {
	    	setupImportButton(result);
	    }
	    if (sessionHelper.getAllowImportG2())
	    	setupImportG2Button(result);
   		setupExportButton(result);
   		setupImpExpTranlateButton(result);
   		if (!isMobile() && (btnImport != null || btnImportG2 != null || btnExport != null)) {
	    	ZkBiButtonGroup bg = new ZkBiButtonGroup(sessionHelper).addButton(btnImport).addButton(btnImportG2).addButton(btnExport).setId("bgImportExport").setLabel(sessionHelper.getLabel("Import/Export"));
	    	if(btImportTranslate != null) {
	    		bg.addSeparator();
	    		bg.addButton(btImportTranslate);
	    		if(btExportTranslate != null) {
	    			bg.addButton(btExportTranslate);
	    		}
	    	}
	    	Button bgImportExport = bg.build();
//	    	Button bgImportExport = new ZkBiButtonGroup(sessionHelper).addButton(btnImport).addButton(btnImportG2).addButton(btnExport).setId("bgImportExport").setLabel(sessionHelper.getLabel("Import/Export")).build();
	        abHelper.addButton(bgImportExport, "fa-folder-open-o");
   		}
   		
   		setupCleanupButton(result);
   		
   		setupExtraButton(result);
	    setupSearch(result);
	    setupExportWindow(result,null);
	    //setupClientInfoEvent();
	    //ZkUtil.autoAdjustWinWidth(masterWin, 0, isMobile());
	    //ZkUtil.registerClientInfoEvent(masterWin, sessionHelper, true, 0); //andrew200610: move to parent
	    abHelper.setContainerParent(actionBar);
	    abHelper.attachButtonsToContainer();
		abHelper.showActionButtonMenu();
	    abHelper.setDelayClickEvent();
	    
	    setupProgessPanel();
	    refreshButtonStatus(result);
    }
    
    private void setupProgessPanel() {
    	progressPanel = new Window();
    	progressPanel.setId("progPanel");
    	progressName = new Label();
    	progressName.setId("progName");
    	progressName.setValue("Progress:");
    	progressMeter = new Progressmeter();
    	progressMeter.setId("progMeter");
    	progressMeter.setWidth("200px");
    	progressMeter.setValue(50);
    	progressCancel = new ZkBiButton();
    	progressCancel.addEventListener(Events.ON_CLICK, 
			new ZkBiEventListener(){
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log("Progress Panel Closed");
					synchronized (exportTimerEvent) {
						if (exportTimerEvent.zkBiTimerEventInterface != null){
							exportTimerEvent.zkBiTimerEventInterface.onCancelClicked();
						}
					}
					progressPanel.setVisible(false);
				}
			}
		);
    	progressCancel.setId("progCancel");
    	progressCancel.setLabel("Cancel");
    	progressPanel.appendChild(progressName);
    	progressPanel.appendChild(progressMeter);
    	progressPanel.appendChild(progressCancel);
    	progressPanel.setVisible(false);
    	masterWin.appendChild(progressPanel);
    }
    
    private BiColumn getBatchActionBiColumn(){
		java.util.Set selection = batchActionFieldListModel.getSelection();
		if(selection.size() == 1) {
			Object selectionObj = selection.iterator().next();
			if (selectionObj instanceof BiColumn){
				return((BiColumn) selectionObj);
			}
		}
		return(null);
    }
    
    void setUpdObject(BiResult p_result,BiColumn bic) throws Exception {
        					ColumnCell cc = p_result.getCell(bic.getLabel());
        				if(getSessionHelper().useJxFormG2()) {
        					Component comp = batchActionFieldDiv.getFirstChild();
        					if (StringUtils.equals((String)comp.getAttribute("isQueryInputComp"), "Y")) {
       							p_result.getCell(bic.getLabel()).set(((ZkJxQueryInput)comp).getQueryString());
        					} else {
        						UniLog.log1("gettype:%s, comp:%s", cc.getType(), comp);
	        					if (cc.getType() == Cell.VTYPE_DATE || cc.getType() == Cell.VTYPE_DATETIME) {
	        						p_result.getCell(bic.getLabel()).set(((Datebox) comp).getValue());
	        					} else if (cc.getType() == Cell.VTYPE_INT) {
	        						if(comp instanceof Combobox) {
	        							Combobox cb = (Combobox) comp;
	//        							ColumnCell cc = p_result.getCell(bic.getLabel());
	//       							if(cc.getItemList() != null) {
	        								if(cb.getSelectedIndex() >= 0) {
	        									p_result.getCell(bic.getLabel()).set(cb.getSelectedIndex());
	        								} else throw new Exception("Input Value Invalid");
	//       							}
	        						} else if(comp instanceof S2Listbox) {
	        							S2Listbox s2 = (S2Listbox) comp;
	        							Listbox lb = (Listbox) s2.getComp();
	       								if(lb.getSelectedIndex() >= 0) {
	       									p_result.getCell(bic.getLabel()).set(lb.getSelectedIndex());
	       								} else throw new Exception("Input Value Invalid");
	        						} else if(comp instanceof Radiogroup ) {
	        							Radiogroup cb = (Radiogroup) comp;
	       								if(cb.getSelectedIndex() >= 0) {
	       									p_result.getCell(bic.getLabel()).set(cb.getSelectedIndex());
	       								} else throw new Exception("Input Value Invalid");
	        						} else {
	        							String ss = ((InputElement) comp).getText();
	        							int nn = Integer.parseInt(ss);
	        							p_result.getCell(bic.getLabel()).set(nn);
	        						}
	        					} else if (cc.getType() == Cell.VTYPE_DOUBLE) {
	        						String ss = ((InputElement) comp).getText();
	        						double dd = Double.parseDouble(ss);
	        						p_result.getCell(bic.getLabel()).set(dd);
	        					} else if (cc.getType() == Cell.VTYPE_BOOLEAN) {
	        						if(comp instanceof Checkbox) {
	        							cc.set(((Checkbox) comp).isChecked());
	        						} else {
	       								String ss = ((InputElement) comp).getText();
	       								if("Y".equals(ss)) {
	       									cc.set(true);
	       								} else {
	       									cc.set(false);
	       								}
	       							}
	        					} else {
	        						/* asumed Cell.VTYPE_STRING */
	        						if(comp instanceof Radiogroup) {
	        							Radiogroup cb = (Radiogroup) comp;
	       								Radio ci = cb.getSelectedItem();
	       								if(ci != null) {
	       									if(ci.getValue() != null) {
	       										if(ci.getValue() instanceof Pair) {
	       											p_result.getCell(bic.getLabel()).set(((Pair) ci.getValue()).getLeft());
	       										} else {
	       											p_result.getCell(bic.getLabel()).set(ci.getValue().toString());
	       										}
	       									} else {
	       										p_result.getCell(bic.getLabel()).set(ci.getLabel());
	       									}
	       								} else throw new Exception("Input Value Invalid");
	        						} else if(comp instanceof S2Listbox) {
	        							S2Listbox s2 = (S2Listbox) comp;
	        							Listbox lb = (Listbox) s2.getComp();
	       								Listitem ci = lb.getSelectedItem();
	       								if(ci != null) {
	       									if(ci.getValue() != null) {
	       										if(ci.getValue() instanceof Pair) {
	       											p_result.getCell(bic.getLabel()).set(((Pair) ci.getValue()).getLeft());
	       										} else {
	       											p_result.getCell(bic.getLabel()).set(ci.getValue().toString());
	       										}
	       									} else {
	       										p_result.getCell(bic.getLabel()).set(ci.getLabel());
	       									}
	       								} else throw new Exception("Input Value Invalid");
	        						} else {
	        							p_result.getCell(bic.getLabel()).set(((InputElement) comp).getText());
	        						}
	        					}
        					}
        				}
	}
    Button btSaveChange=null;
    protected void setupBatchSetGet(Vector <BiColumn> p_listColumns, final BiResult p_result){
//	    if (!sessionHelper.isAdminUser()) return;
    	if(! p_result.getView().allowBatchUpdate(sessionHelper)) return;
    	if(getSessionHelper().useJxFormG2()) batchActionBar.appendChild(new Label(sessionHelper.getLabel("Batch Update Set") + " "));
    	batchActionFieldList = new Listbox();
    	batchActionFieldList.setRows(1);
    	//batchActionFieldList.setVflex(true);
    	batchActionFieldList.setWidth("200px");
    	batchActionFieldList.setMold("select");
    	batchActionFieldListModel = new ListModelList();
    	if(!getSessionHelper().useJxFormG2()) {
    		batchActionFieldListModel.add(" - Batch update column - ");
    	}
    	for (BiColumn bic : p_listColumns){
    		if (!bic.isNoUpdate(getSessionHelper()) && bic.allowBatchUpdate() ){
    			batchActionFieldListModel.add(bic); //TODO check if column is editable
    		}
    	}
    	batchActionFieldList.setModel(batchActionFieldListModel);
    	if(!getSessionHelper().useJxFormG2()) {
    		batchActionFieldList.setSelectedIndex(0);
    	}
    	batchActionFieldList.setItemRenderer(
    			new ListitemRenderer <Object> () {
    				   public void render(Listitem item, Object p_obj, int p_idx) throws Exception {
    					   if (p_obj instanceof BiColumn){
    						   BiColumn bic = (BiColumn)p_obj;
    						   item.appendChild((new Listcell(bic.getEngName())));
    					   }
    					   else{
    						   item.appendChild((new Listcell(p_obj + "")));
    					   }
    				   }
    			});
    	batchActionBar.appendChild(batchActionFieldList);
    	if(getSessionHelper().useJxFormG2()) batchActionBar.appendChild(new Label(" " + sessionHelper.getLabel("to") + " "));
    	
    	if(getSessionHelper().useJxFormG2()) {
    		batchActionFieldDiv = new Div();
    		batchActionBar.appendChild(batchActionFieldDiv);
    	} else {
    	batchActionFieldTextbox = new Textbox();
    	batchActionFieldTextbox.setWidth("130px");
    	batchActionFieldTextbox.setVisible(false);
    	batchActionBar.appendChild(batchActionFieldTextbox);
    	
    	batchActionFieldDatebox = new Datebox();
    	batchActionFieldDatebox.setWidth("100px");
    	batchActionFieldDatebox.setVisible(false);
    	batchActionBar.appendChild(batchActionFieldDatebox);
    	}
    	
    	batchActionFieldList.addEventListener(Events.ON_SELECT, 
    		new ZkBiEventListener() {
    		    public void onZkBiEvent(Event event) throws Exception {
    		    	if(getSessionHelper().useJxFormG2()) {
    		    	} else {
					batchActionFieldTextbox.setVisible(false);
					batchActionFieldDatebox.setVisible(false);
					batchActionFieldTextbox.setValue(null);
					batchActionFieldDatebox.setValue(null);
    		    	}
    		    	BiColumn bic = getBatchActionBiColumn();
    		    	if (bic == null){
        				UniLog.log("ignore non bicolumn");
        				return;
    		    	}
    		    	if (bic.isNoUpdate(getSessionHelper())){
        				UniLog.log("ignore noupdate column");
    		    	}
    		    	if(getSessionHelper().useJxFormG2()) {
    		    		Components.removeAllChildren( batchActionFieldDiv);
    		    		Component comp;
    		    		if (bic.getColumnType().trim().equals("char")) {
    		    			comp = ZkBiAdvSearch.buildQueryInput(sessionHelper, masterWin, false, false, false, p_result, bic);
    		    			comp.setAttribute("isQueryInputComp", "Y");
    		    		} else {
	    		    		comp = JxZkBiBase.createComponentByBiColumn(bic) ;
	    		    		JxZkBiBase.setComponentFormat(comp,bic,0,0,300,false,getSessionHelper(),null,false);
	    		    		ColumnCell cc = p_result.getCell(bic.getLabel());
	    		    		if(comp instanceof Radiogroup) {
	    		    			if(cc.getItemList() != null) {
	    		    				for(Object o : cc.getItemList()) {
	    		    					if(o instanceof String) {
	   		    							((Radiogroup) comp).appendItem((String) o, null);
	    		    					}
	    		    					if(o instanceof Pair) {
	    		    						Radio rd = ((Radiogroup) comp).appendItem((String) ((Pair) o).getRight(),null);
	    		    						rd.setValue(((Pair) o).getLeft());
	    		    					}
	    		    				}
	    		    			}
	    		    		}
	    		    		if(comp instanceof Combobox) {
	    		    			if(cc.getItemList() != null) {
	    		    				for(Object o : cc.getItemList()) {
	    		    					if(o instanceof String) {
	   		    							((Combobox) comp).appendItem((String) o);
	    		    					}
	    		    					if(o instanceof Pair) {
	    		    						Comboitem rd = ((Combobox) comp).appendItem((String) ((Pair) o).getRight());
	    		    						rd.setValue(((Pair) o).getLeft());
	    		    					}
	    		    				}
	    		    			} else if(cc.getItemPropertyInterface() != null){
	    		    				
	    		    			} else if(bic.getSelPickList(p_result) != null) {
	    		    				for(Object o : bic.getSelPickList(p_result)) {
	   		    						((Combobox) comp).appendItem(o.toString());
	    		    				}
	    		    			}
	    		    				
	    		    		}
	    		    		if(comp instanceof S2Listbox ) {
	        					Listbox lb = (Listbox) ((S2Listbox) comp).getComp();
	    		    			if(cc.getItemList() != null) {
	    		    				for(Object o : cc.getItemList()) {
	    		    					if(o instanceof String) {
	   		    							lb.appendItem((String) o,null);
	    		    					}
	    		    					if(o instanceof Pair) {
	    		    						Listitem rd = lb.appendItem((String) ((Pair) o).getRight(),null);
	    		    						rd.setValue(((Pair) o).getLeft());
	    		    					}
	    		    				}
	    		    			} else if(cc.getItemPropertyInterface() != null){
	    		    				AbstractGetItemProperty gipi =  cc.getItemPropertyInterface();
	    		    				for(int i=0;i<cc.getItemPropertyInterface().getRowCount();i++) {
	    		    					if (gipi instanceof GipiNamedItemList) {
	    		    						lb.appendItem(((GipiNamedItemList)gipi).getName(i),gipi.getRow(i).toString());
	    		    					}
	    		    					else {
	    		    						lb.appendItem( gipi.getRow(i).toString(),null);
	    		    					}
	    		    				}
	    		    			}
	    		    		}
    		    		}
    		    		batchActionFieldDiv.appendChild(comp);
    		    	} else {
   					if (bic.getColumnType().trim().equals("date")){
   						//show calender
   						UniLog.log("show calender");
   						batchActionFieldDatebox.setVisible(true);
   					}
   					else{
  						//show textbox
   						UniLog.log("show textbox");
   						batchActionFieldTextbox.setVisible(true);
   					}
    		    	}
    		    };
    		}
    	);
    	
    	if(getSessionHelper().useJxFormG2()) {
//    		batchActionFieldList.setRe
    	}
    	
    	btnBatchActionSet = new ZkBiButton();
    	if(getSessionHelper().useJxFormG2()) {
    		btnBatchActionSet.setLabel(sessionHelper.getBtLabel("Proceed"));
    	} else {
    		btnBatchActionSet.setLabel(sessionHelper.getBtLabel("Batch Update"));
    	}
    	btnBatchActionSet.setId("btBatchActionSet");
    	batchActionBar.appendChild(btnBatchActionSet);
    	
    	if(getSessionHelper().useJxFormG2()) 
        btnBatchActionSet.addEventListener("onClick",
        	new ZkBiEventListener() {
        		public void onZkBiEvent(Event event) throws Exception {
        			java.util.Set selection = listModelList.getSelection();
        			if (selection.size() == 0){
        				ZkUtil.msg("No rows selected");
        				return;
        			}
       				BiColumn bic = getBatchActionBiColumn();
       				if (bic == null){
       					ZkUtil.msg(sessionHelper.getLabel("Please choose batch update column"));
       					return;
       				}
       				
       				//add max update control
       				int maxUpd = 100; //set a reasonable default for normal user
       				String maxUpdKey = sessionHelper.getAccessRightKeyByPrefix("#maxupd");
       				if (StringUtils.isNotBlank(maxUpdKey)) {
       					maxUpd = NumberUtil.atoi(maxUpdKey);
       					UniLog.log1("maxUdp change to %d", maxUpd);
       				}
       				if (!sessionHelper.isAdminUser() && selection.size() > maxUpd) {
       					ZkUtil.warnMsg("Too many rows selected. (max %d)", maxUpd);
       					return;
       				}
       				
       				
      				ColumnCell cc = p_result.getCell(bic.getLabel());
      				int uptCnt = 0;
				    if(importAsSingle) p_result.beginWork();
        			for(Object o : selection) {
				    	uptCnt++;
        				int listIdx = getListIdxByObj(listModelList, o);
       					int idx = getTrIdxByObj(listModelList, o, -1);
        				UniLog.log("Update Row (ABC) " + idx +  " : " + o);
        				Object ts = o;
        				if (ts instanceof TrStatFilter){
        					ts = ((TrStatFilter)ts).trStatIdx;
        				}
        				p_result.loadOneRecV(idx);
				    	p_result.fetchOneRecV(idx);
				    	setUpdObject(p_result,bic);
				    	ReturnMsg rtnMsg = p_result.updateCurrent();
				    	if(rtnMsg != null && !rtnMsg.getStatus()) {
				    		p_result.rollbackWork();
				    		Messagebox.show(sessionHelper.getLabel("Update Failed") + ": " + rtnMsg == null ? "" : rtnMsg.getMsg());
				    		return;
				    	}
				    	
        			}
        			listModelList.clearSelection();
        			//String strMsg = sessionHelper.getLabel("Records Updated");
        			String strMsg = sessionHelper.getLabel(String.format("%d Records Updated", uptCnt));
        			Messagebox.show(
        					strMsg,
        					sessionHelper.getLabel("Confirm Save Changes?"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {
        			    public void onZkBiEvent(Event evt) throws InterruptedException {
        			    	if(importAsSingle) {
        			        if (evt.getName().equals("onOK")) {
        			        	UniLog.log("Confirm OK commit work");
        			        	try {
        			        		if(!p_result.commitWork()) {
        			        			Messagebox.show(sessionHelper.getLabel("Commit Work Failed, please upload again"));
        			        		}
        			        	} catch (Exception ex) {
        			        		UniLog.log(ex);
        			        		Messagebox.show(ex.toString());
        			        	}
       			        		refresh(p_result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
        			        } else {
        			        	UniLog.log("Confirm Canceled rollback work");
        			        	p_result.rollbackWork();
        			    		refresh(p_result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
        			        }
        			    	}
        			    }
        			});	
        			
        		}
        	}
        );
    	else
        btnBatchActionSet.addEventListener("onClick",
        	new ZkBiEventListener() {
        		public void onZkBiEvent(Event event) throws Exception {
        			java.util.Set selection = listModelList.getSelection();
        			if (selection.size() == 0){
       					Messagebox.show(sessionHelper.getLabel("No rows selected"));
        				return;
        			}
       				BiColumn bic = getBatchActionBiColumn();
       				if (bic == null){
       					Messagebox.show(sessionHelper.getLabel("Please choose batch update column"));
       					return;
       				}
        			for(Object o : selection) {
        				int listIdx = getListIdxByObj(listModelList, o);
       					int idx = getTrIdxByObj(listModelList, o, -1);
        				UniLog.log("Update Row (ABC) " + idx +  " : " + o);
        				Object ts = o;
        				if (ts instanceof TrStatFilter){
        					ts = ((TrStatFilter)ts).trStatIdx;
        				}
       					p_result.markUpdate(ts, true);
        				p_result.loadOneRecV(idx);
        				if(getSessionHelper().useJxFormG2()) {
        					setUpdObject(p_result,bic);
        				} else {
        				if (bic.getColumnType().trim().equals("date")){
        					p_result.getCell(bic.getLabel()).set(batchActionFieldDatebox.getValue());
        				} if (bic.getColumnType().trim().equals("float")){
        					String ss = batchActionFieldTextbox.getValue();
        					try {
        						double dd = Double.parseDouble(ss);
        						p_result.getCell(bic.getLabel()).set(dd);
        						
        					} catch (Exception ex) {
        						UniLog.log(ex);
        					}
        				} if (bic.getColumnType().trim().equals("integer")){
        					String ss = batchActionFieldTextbox.getValue();
        					try {
        						int nn = Integer.parseInt(ss);
        						p_result.getCell(bic.getLabel()).set(nn);
        						
        					} catch (Exception ex) {
        						UniLog.log(ex);
        					}
        				}
        				else{
        					p_result.getCell(bic.getLabel()).set(batchActionFieldTextbox.getValue());
        				}
        				}
        				p_result.saveOneRecV(idx);
        				//listModelList.set(idx, o);
        				listModelList.set(listIdx, o); //use list index
        				UniLog.log("Update Row DEF");
        			}
        			listModelList.clearSelection();
        			Events.echoEvent(Events.ON_CLICK, btSaveChange, null);
        		}
        	}
        );
    	if(getSessionHelper().useJxFormG2()) {
    		Button btnBatchActionBack = new ZkBiButton();
    		btnBatchActionBack.setLabel(sessionHelper.getBtLabel("Back"));
    		btnBatchActionBack.setId("btBatchActionBack");
    		btnBatchActionBack.addEventListener("onClick",
    			new ZkBiEventListener() {
        			public void onZkBiEvent(Event event) throws Exception {
						setMultiSelectMode(false,p_result);
						setBatchActionConfirmBar(false,p_result);
						setActionConfirmBar(true,p_result);
        			}
        		}
    		);
    		batchActionBar.appendChild(btnBatchActionBack);
    	}
    	
    }
    void setupBatchActionBar(Vector <BiColumn> p_listColumns, final BiResult p_result){
        if (hasAUDColumn){
        	setupBatchSetGet(p_listColumns, p_result);
        }
    }
    
    public void markUpdateOneRow(Object o,BiResult p_result)
    {
       int idx = getTrIdxByObj(listModelList, o, -1);
       int listIdx = getListIdxByObj(listModelList, o);
       Object ts = o;
       if (ts instanceof TrStatFilter){
    	   ts = ((TrStatFilter)ts).trStatIdx;
       }
       p_result.markUpdate(ts, true);
       p_result.saveOneRecV(idx);
       //listModelList.set(idx, o);
       listModelList.set(listIdx, o);
	}
    
    protected void refreshListItems(Object o) {
    	if (o == null){
    		//andrew: temporary dirty fix. if fail to obtain the index, call doSearch to refresh the list
    		doSearch(false, false); 
    		return;
        }
        int listIdx = getListIdxByObj(listModelList, o);
        if(listModelList.size() <= 0) return;
        //listModelList.set(idx, o);
        if (listIdx >= 0){ //avoid generate error
        	listModelList.set(listIdx, o);
        } 
        //second round, handle search result
        if (zkBiSearch != null && BiResult.isTrStat(o) && listModelList.get(0) instanceof TrStatFilter){
        	TrStatFilter trf = zkBiSearch.getTrStatFilterByTrStat(o);
        	if (trf != null){
        		listIdx = getListIdxByObj(listModelList, trf);
        		if (listIdx < 0){
        			UniLog.log1("fail to obtain listIdx:" + o);
        			return;
        		}
				listModelList.set(listIdx, trf);
        	}
        }
     }
     
    //fix refresh by TrStat object  (andrew: alternative fix, for replace original refreshListItems)
    //need to test!!!
    public void refreshListItems(Object o, int p_trStatIdx) {
    	//first round: default search
    	int listIdx = getListIdxByObj(listModelList, o); 
    	if (listIdx >= 0){
    		listModelList.set(listIdx, o);
    		return;
    	}

    	//second round: search trStatIdx from TrStatFilterList
    	if (listIdx < 0 && p_trStatIdx >= 0 && zkBiSearch != null){
    		for (int i=0; i<listModelList.size(); i++){
    			Object listModelObj = listModelList.get(i);
    			if (listModelObj instanceof TrStatFilter){
    				if (((TrStatFilter)listModelObj).trStatIdx == p_trStatIdx){
    					listModelList.set(i, zkBiSearch.new TrStatFilter(p_trStatIdx));
    					return;
    				}
    			}
    		}
    	}
    }
    
    protected boolean saveChangeCheck(BiResult result) {
					if ((result.getDeleteCount()+result.getUpdateCount()+result.getInsertCount()) <= 0){
						UniLog.log("no changes, ignore");
						ZkUtil.normMsg(sessionHelper.getLabel("No changes detected. Action ignore."));
   						return false;
   					}
					int maxDel = 1;  //default for normal user
       				String maxDelKey = sessionHelper.getAccessRightKeyByPrefix("#maxdel");
       				if (StringUtils.isNotBlank(maxDelKey)) {
       					maxDel = NumberUtil.atoi(maxDelKey);
       					UniLog.log1("maxDel change to %d", maxDel);
       				}
					
					if (!sessionHelper.isAdminUser() && result.getDeleteCount() > maxDel) {
						refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
						UniLog.log1("You cannot delete multiple records at once");
						ZkUtil.warnMsg(sessionHelper.getLabel(String.format("You cannot delete multiple records at once. (max %d)", maxDel)));
						return false;
					}
					return true;
    }
    
    protected boolean saveChangeConfirm(BiResult result) {
        			        	boolean needRefresh = false;
        			        	if(result.getDeleteCount() > 0 || result.getInsertCount() > 0  ) needRefresh = true;
        			        	UniLog.log("Confirm OK");
        			        	ReturnMsg rtnMsg = result.batchAddUpdateDelete();
        			        	result.unMarkAll();
        			        	result.clearCurrentRec();
        			        	result.invalidateLoadCache();
        			        	if(needRefresh) {
        			        		refresh(result,masterWin,-1,true,true); 
        			        		//should preserve search
        			        	} else {
        			        		listModelList.clear();
        			        		listModelList.addAll(result.getResultStat());
        			        		doSearch(true, false);
        			        	}
        			        	if(rtnMsg != null && !rtnMsg.getStatus()) {
        			        		Messagebox.show(rtnMsg.getMsg());
        			        		return false;
        			        	}
        			        	return true;
    }
    
	protected void setupSaveChangeButton(final BiResult result){
    	final Checkbox cbShowModified;
    	if(masterWin.hasFellow("cbShowModified")) {
    		cbShowModified= (Checkbox) masterWin.getFellow("cbShowModified");
    	} 
    	else {
	        cbShowModified= new Checkbox();
	        cbShowModified.setLabel("Show Modified");
	        cbShowModified.setId("cbShowModified");
	        confirmBar.appendChild(cbShowModified);
	        cbShowModified.setVisible(false); //disable temporary to avoid conflict with search result
    		
    	}
		cbShowModified.addEventListener("onClick",
        	new ZkBiEventListener() {
        		public void onZkBiEvent(Event event) throws Exception {
    				showModified = cbShowModified.isChecked();
    				//clearSearch();
    				if(showModified) {
    					int n = result.getDeleteCount()+result.getUpdateCount()+result.getInsertCount();
    					if (n <= 0){
    						Messagebox.show(sessionHelper.getLabel("No changes made"));
    						cbShowModified.setChecked(false);
    						showModified = false;
    						return;
    					}
    					//if(n > 20) n = 20;
    					//listbox.setPageSize(n);
    				} else {
    					//listbox.setPageSize(20);
    				}
    				clearSearch(result);
        		}
        	}
		);
		
    	if(masterWin.hasFellow("btSaveChange")) {
    		btSaveChange = (Button) masterWin.getFellow("btSaveChange");
    	} 
    	else {
    		btSaveChange = new ZkBiButton();
	        btSaveChange.setLabel("Apply");
	        //btSaveChange.setVisible(false);
	        if (!sessionHelper.isAdminUser()){
	        	btSaveChange.setDisabled(true);
	        	btSaveChange.setTooltiptext(sessionHelper.getTtLabel("No permission"));
	        }
	        btSaveChange.setVisible(false);
	        cbShowModified.setId("btSaveChange");
	        confirmBar.appendChild(btSaveChange);
    		
    	}
		btSaveChange.addEventListener("onClick",
        	new ZkBiEventListener() {
        		public void onZkBiEvent(Event event) throws Exception {
        			saveChangeCheck(result);
        			Messagebox.show(
        					//""+result.getInsertCount()+" Added " + result.getUpdateCount() + " Updated " + result.getDeleteCount() + " Deleted",
        					String.format(sessionHelper.getLabel("%d Added, %d Updated, %d Deleted"), result.getInsertCount(), result.getUpdateCount(), result.getDeleteCount()),
        					sessionHelper.getLabel("Confirm Save Changes?"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {
        			    public void onZkBiEvent(Event evt) throws InterruptedException {
        			        if (evt.getName().equals("onOK")) {
        			        	saveChangeConfirm(result);
        			        } else {
        			        	UniLog.log("Confirm Canceled");
        			        	refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
        			        }
        			    }
        			});	
        		}
        	}
		);
	}
	protected void setupDeleteButton(final BiResult result) {
		setupDeleteButton(result, true);
	}
	protected void setupDeleteButton(final BiResult result, boolean needSetupBatch)
	{
        if(!result.allowDelete()) return;
        if(isMobile()) return;
		boolean btnDeletePreDefined = false;
    	if(masterWin.hasFellow("btDelete")) {
    		btnDelete  = (Button) masterWin.getFellow("btDelete");
    		btnDeletePreDefined = true;
    	} 
    	else {	
	        btnDelete = new ZkBiButton();
	        //btnDelete.setLabel("Delete");
	        btnDelete.setLabel(sessionHelper.getBtLabel("Delete"));
	        btnDelete.setId("btDelete");
	        btnDelete.setAttribute("tlkey", "bt_master_delete");
	        btnDelete.setImage("images/icons/zkweb/028-file-5-20x20.png");
	        abHelper.addButton(btnDelete, "fa-times");
    	} 
    	btnDelete.setTooltiptext(sessionHelper.getTtLabel("Delete record"));
        btnDelete.addEventListener("onClick",
            	new ZkBiEventListener() {
            		public void onZkBiEvent(Event event) throws Exception {
            			java.util.Set selection = listModelList.getSelection();
            			for(Iterator it=selection.iterator();it.hasNext();) {
            				Object o = it.next();
            				int listIdx = getListIdxByObj(listModelList,o);
            				int idx = getTrIdxByObj(listModelList, o, -1);
            				Object ts = o;
            				if (ts instanceof TrStatFilter){
            					ts = ((TrStatFilter)ts).trStatIdx;
            				}
            				UniLog.log("Delete Row " + idx);
            				result.markDelete(ts, !result.isMarkedDelete(ts));
            				//Listitem li = listbox.getItemAtIndex(idx);
            				Listitem li = listbox.getItemAtIndex(listIdx);
            				Listcell lc = (Listcell) li.getChildren().get(0);
            				//listModelList.set(idx, o);
            				listModelList.set(listIdx, o); //use list index
            			}
            			if(multiSelect) listModelList.clearSelection();
            			Events.echoEvent(Events.ON_CLICK, btSaveChange, null);
            		}
            	}	
        );
        if(!btnDeletePreDefined && needSetupBatch) {
        	setupBatchModeButton(btnDelete);
        }
	}
    
    protected void setupAddButton(final BiResult result)
    {
        if(!result.allowAdd()) return;
        final Button btnAdd;
    	if(masterWin.hasFellow("btAdd")) {
    		btnAdd = (Button) masterWin.getFellow("btAdd");
    	} else {
        	btnAdd = new ZkBiButton();
	        btnAdd.setLabel(sessionHelper.getBtLabel("Add"));
	        btnAdd.setId("btAdd");
	        btnAdd.setImage("images/icons/zkweb/038-file-4-25x25.png");
	        btnAdd.setAttribute("tlkey", "bt_master_add");
	        abHelper.addButton(btnAdd,"fa-plus");
    	}
    	btnAdd.setTooltiptext(sessionHelper.getTtLabel("Add new record"));
    	addHotkey('A', btnAdd);
        btnAdd.addEventListener("onClick",
	        	new ZkBiEventListener() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			result.clearCurrentRec();
	        			doAddOneRow(masterWin,result);
	        		}
	        	}
	        );
        if(!result.allowAdd()) btnAdd.setVisible(false);
    }
    protected void setupUpdateBiViewButton(final BiResult result) {
    	if (!sessionHelper.isAdminUser()) return;
    	if(masterWin.hasFellow("btUpdateBiView")) {
    		btnBiView = (Button) masterWin.getFellow("btUpdateBiView");
    	} else {
        	btnBiView = new ZkBiButton();
	        btnBiView.setLabel(sessionHelper.getBtLabel("BiView"));
	        btnBiView.setId("btUpdateBiView");
	        btnBiView.setImage("images/icons/zkweb/072-slider-tool-25x25.png");
	        //abHelper.addButton(btnBiView, "fa-print");
    	}
    	btnBiView.setTooltiptext(sessionHelper.getTtLabel("Update Custom BiView"));
        btnBiView.addEventListener(Events.ON_CLICK,
       		new ZkBiEventListener<Event>() {
        		public void onZkBiEvent(Event event) throws Exception {
        			UniLog.log("click:" + event);
        			new ZkBiUpdateBiView(result, masterWin, sessionHelper)
						.showDialog();
        		}
        	}
	    );
    }
    protected void setupImportPresetButton(final BiResult result, final Component comp) {
    	if (!sessionHelper.isAdminUser()) return;
    	if(masterWin.hasFellow("btImportPreset")) {
    		btImportPreset = (Button) masterWin.getFellow("btImportPreset");
    	} else {
        	btImportPreset = new ZkBiButton();
	        btImportPreset.setLabel(sessionHelper.getBtLabel("Import Preset"));
	        btImportPreset.setId("btImportPreset");
	        btImportPreset.setImage("images/icons/zkweb/020-import-25x25.png");
	        //abHelper.addButton(btImportPreset, "fa-print");
    	}
    	btImportPreset.setTooltiptext(sessionHelper.getTtLabel("Import Preset"));
        btImportPreset.addEventListener(Events.ON_CLICK,
       		new ZkBiEventListener<Event>() {
        		public void onZkBiEvent(Event event) throws Exception {
        			UniLog.log("click:" + event);
        			new ZkBiImportExportPreset(mConditionPresets, presetid, masterWin, sessionHelper)
						.showImportDialog(new ZkBiImportExportPreset.ImportPresetCallback() {
							@Override
							public void callback() {
								String curPreset = conditionPresetListbox.getSelectedItem().getValue();
								if (curPreset != null) {
									ConditionFieldMap cfm = mConditionPresets.getFieldMap(curPreset);
									inputFieldsList.reset(curPreset);
									inputFieldsList.customCondition = cfm.getCustomCondition();
									ibLimit.setValue(cfm.getRecordLimit());
									frozenCount = cfm.getFrozenCount();
									setFrozenColumn(frozenCount);
									visibleCols(curPreset, comp, result);
									MultiSortMap sortMap = curPreset != null ? new MultiSortMap(curPreset,result) : null;
									refresh(result,masterWin,sortMap,false);
								}
								Events.sendEvent("onReset", conditionPresetListbox, curPreset);
							}
						});
        		}
        	}
	    );
    }
    protected void setupExportPresetButton(final BiResult result) {
    	if (!sessionHelper.isAdminUser()) return;
    	if(masterWin.hasFellow("btExportPreset")) {
    		btExportPreset = (Button) masterWin.getFellow("btExportPreset");
    	} else {
        	btExportPreset = new ZkBiButton();
	        btExportPreset.setLabel(sessionHelper.getBtLabel("Export Preset"));
	        btExportPreset.setId("btExportPreset");
	        btExportPreset.setImage("images/icons/zkweb/019-export-25x25.png");
	        //abHelper.addButton(btExportPreset, "fa-print");
    	}
    	btExportPreset.setTooltiptext(sessionHelper.getTtLabel("Export Preset"));
        btExportPreset.addEventListener(Events.ON_CLICK,
       		new ZkBiEventListener<Event>() {
        		public void onZkBiEvent(Event event) throws Exception {
        			UniLog.log("click:" + event);
        			new ZkBiImportExportPreset(mConditionPresets, presetid, masterWin, sessionHelper)
						.showExportDialog();
        		}
        	}
	    );
    }
    protected void setupGeneralReportButton(final BiResult result) {
	    if(!result.getView().allowReport(sessionHelper)) return;
        final Button btn;
    	if(masterWin.hasFellow("btGeneralReport")) {
    		btn = (Button) masterWin.getFellow("btGeneralReport");
    	} else {
        	btn = new ZkBiButton();
	        btn.setLabel(sessionHelper.getBtLabel("Report"));
	        btn.setId("btGeneralReport");
	        btn.setImage("images/icons/zkweb/053-printer-25x25.png");
	        abHelper.addButton(btn, "fa-print");
    	}
    	btn.setTooltiptext(sessionHelper.getTtLabel("General Report"));
    	//btn.setVisible(!isMobile()); //disable report for mobile
    	
    	addHotkey('O', btn);
        btn.addEventListener(Events.ON_CLICK,
	        	new ZkBiEventListener<Event>() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			UniLog.log("click:" + event);
	        			printGenerateReport(result);
	        		}
	        	}
	        );
    }
    protected void printGenerateReport(BiResult result) {
    	String viewHeader = StringUtils.defaultIfBlank(sessionHelper.getSideMenuDesc(pageid), result.getView().getHeader());
		if (StringUtils.isNotBlank(pageid))
			viewHeader = ZkBiTranslateHelper.getText(sessionHelper, pageid.toUpperCase(), "MENU", viewHeader);
		viewHeader = sessionHelper.getLabel(viewHeader);
		String title = viewHeader;
		String subTitle = null;
		String preset = (String)conditionPresetListbox.getSelectedItem().getValue();
		//if (preset != null)
		//	subTitle = (String)conditionPresetListbox.getSelectedItem().getLabel();

		List<String> dsFieldList = new ArrayList<String>();
		List<Integer> aopIdxList = new ArrayList<Integer>();
		//Map<String, MultiSortInfo> dsFieldSortMap = new HashMap<String, MultiSortInfo>();
		Map<Integer, MultiSortInfo> dsFieldSortMap = new HashMap<Integer, MultiSortInfo>();
		Map<String, String[]> aopHeaderMap = new HashMap<String, String[]>();
		Vector<BiColumn> listColumns = result.getListColumns();
		if (sessionHelper.isMobile()) {
			for (BiColumn biColumn : listColumns) {
				dsFieldList.add(biColumn.getLabel());
				aopIdxList.add(-1);
			}
		} else {
			int ncols = listColumns.size();
			for (Component tmpComp : listbox.queryAll("Listheader")){
				Listheader tmpListheader = (Listheader) tmpComp;
				BiColumn biColumn = (BiColumn) tmpListheader.getAttribute("ma_bicolumn");
				String pivotHeader = (String) tmpListheader.getAttribute("pivot_header");
				String aop = (String) tmpListheader.getAttribute("aggregate_or_pivot");
				String[] pivotAuxheaderList = (String[]) tmpListheader.getAttribute("pivot_auxheader_list");
				UniLog.log1("biColumn:%s, pivotHeader:%s, aop:%s, pivotAuxheaderList:%s", biColumn, pivotHeader, aop, Arrays.toString(pivotAuxheaderList));
				if (aop != null) {
					aopHeaderMap.put(aop, ArrayUtils.add(pivotAuxheaderList, 0, pivotHeader));
					pivotHeader = aop;
					UniLog.log1("aop:%s, aopHeader:%s", aop, Arrays.toString(aopHeaderMap.get(aop)));
				}
				if (tmpListheader.isVisible() && tmpListheader.getId().startsWith("browser_listheader_") && (biColumn != null || pivotHeader != null)) {
					dsFieldList.add(biColumn != null ? biColumn.getLabel() : pivotHeader);
					int colIdx = Integer.parseInt(tmpListheader.getId().substring(19));
					int aopIdx = -1;
					if (aop != null)
						aopIdx = colIdx - ncols - 1;
					aopIdxList.add(aopIdx);
					MultiSortInfo msi = mMultiSortMap.get(colIdx);
					if (msi != null) {
						//dsFieldSortMap.put(biColumn != null ? biColumn.getLabel() : pivotHeader, msi);
						dsFieldSortMap.put(dsFieldList.size() - 1, msi);
					}
				}
			}
		}
		ZkBiAdvSearch advSearch = new ZkBiAdvSearch(result, masterWin, sessionHelper, null, mConditionPresets, preset, presetid, null);
		advSearch.restoreEmbedConditionBlocks(new Groupbox(), new Div(), inputFieldsList.customCondition);
		ReportGenerate.showDialog(masterWin, sessionHelper, presetid, result, viewHeader, title, advSearch.makeCustomCondition(true, false), preset, mConditionPresets, generateReportDesignRes, dsFieldList, dsFieldSortMap, aopHeaderMap, generateReportItemScriptMap, generateReportUserPropMap, generateReportSettingMap, listModelList, aopIdxList, generateReportCallback);
		//ReportGenerate.showDialog(masterWin, sessionHelper, viewid, result, viewHeader, title, null, preset, mConditionPresets, "ReportGenerate.rptdesign", dsFieldList, dsFieldSortMap, aopHeaderMap);
    }
    protected void setupPrintButton(final BiResult result) {
        final Button btn;
    	if(masterWin.hasFellow("btPrint")) {
    		btn = (Button) masterWin.getFellow("btPrint");
    	} else {
        	btn = new ZkBiButton();
	        btn.setLabel(sessionHelper.getBtLabel("Print"));
	        btn.setId("btPrint");
	        abHelper.addButton(btn, "fa-print");
    	}
    	btn.setTooltiptext(sessionHelper.getTtLabel("Print records"));
    	addHotkey('P', btn);
        btn.addEventListener(Events.ON_CLICK,
	        	new ZkBiEventListener<Event>() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			ZkUtil.print(listbox);
	        		}
	        	}
	        );
    }
    protected void setupDetailButton(final BiResult result)
    {
        if(!result.allowDetail()) return;
    	if(isMobile()) return;
        final Button btnDetail;
    	if(masterWin.hasFellow("btDetail")) {
    		btnDetail = (Button) masterWin.getFellow("btDetail");
    	} else {
        	btnDetail = new ZkBiButton();
	        btnDetail.setLabel(sessionHelper.getBtLabel("Detail"));
	        btnDetail.setImage("images/icons/zkweb/039-file-3-25x25.png");
	        btnDetail.setId("btDetail");
	        btnDetail.setVisible(false); //not required to display detail button by default
        	//actionBar.appendChild(btnDetail);
        	abHelper.addButton(btnDetail, "fa-file-o");
    	}
    	//addHotkey('D', btnDetail); //remark: alt+d for chrome default action
    	btnDetail.setTooltiptext(sessionHelper.getTtLabel("View/Edit record"));
        btnDetail.addEventListener("onClick",
	        	new ZkBiEventListener() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			UniLog.log("Detail Button Clicked");
	        			if(listbox.getSelectedCount() == 1) {
	        				java.util.Set selection = listModelList.getSelection();
	        				if(selection.size() == 1) {
	        					Object o = selection.iterator().next();
	        					int idx = getTrIdxByObj(listModelList, o, -1);
    							result.fetchOneRecV(idx);
    							doUpdateOneRow(masterWin,result);
	        				}
	        			}
	        			else{
        					showMsg(sessionHelper.getLabel("Please select a record first"));
	        			}
	        		}
	        	}
	        );
    }
    protected void setupDataAnalysisButton(final BiResult result) {
    	
    }
    protected void setupDataAnalysisButtonGeneral(final BiResult result) {
        final Button btn;
    	if(masterWin.hasFellow("btDataAnalysis")) {
    		btn = (Button) masterWin.getFellow("btDataAnalysis");
    	} else {
        	btn = new ZkBiButton();
	        btn.setLabel(sessionHelper.getBtLabel("Data Analysis"));
	        btn.setAttribute("tlkey", "bt_master_dataanalysis");
	        btn.setId("btDataAnalysis");
	        //actionBar.appendChild(btn);
	        abHelper.addButton(btn, "fa-bar-chart");
    	}
    	btn.setTooltiptext(sessionHelper.getTtLabel("Data Analysis"));
    	addHotkey('N', btn);
        btn.addEventListener(Events.ON_CLICK,
        	new ZkBiEventListener<Event>() {
        		public void onZkBiEvent(Event event) throws Exception {
        			if (listbox == null)
        				return;
        			final Vector<BiColumn> listColumns = result.getListColumns();
    				JSONArray jsonColsArr = new JSONArray();
    				JSONArray jsonRowsArr = new JSONArray();
    				JSONArray jsonDataArr = new JSONArray();
    				List<Integer> cols = new ArrayList<Integer>();
        			for (Component tmpComp : listbox.queryAll("Listheader")){
        				Listheader tmpListheader = (Listheader) tmpComp;
        				if (tmpListheader.isVisible() && tmpListheader.getId().startsWith("browser_listheader_")) {
        					int col = Integer.parseInt(tmpListheader.getId().substring(19)) - 1;
        					if (col >= 0) {
        						cols.add(col);
        					}
        				}
        			}
       				for (int col : cols) {
       					BiColumn biColumn = listColumns.get(col);
       					jsonRowsArr.put(biColumn.getEngName());
       				}
       				for (int i = 0; i < listModelList.size(); i++) {
       					int idx = getTrIdxByObj(listModelList, listModelList.get(i), -1);
       					if (idx >= 0) {
       						result.loadOneRecV(idx);
	        				JSONObject jsonObj = new JSONObject();
	        				for (int col : cols) {
	        					BiColumn biColumn = listColumns.get(col);
	        					if (biColumn.getColumnType().trim().matches("float|money"))
	        						jsonObj.put(biColumn.getEngName(), result.getCell(biColumn.getLabel()).getDouble());
	        					else if (biColumn.getColumnType().trim().matches("integer"))
	        						jsonObj.put(biColumn.getEngName(), result.getCell(biColumn.getLabel()).getInt());
	        					else	
	        						jsonObj.put(biColumn.getEngName(), result.getCell(biColumn.getLabel()).getString());
	        				}
	        				jsonDataArr.put(jsonObj);
       					}
       				}

        			Div div = new Div();
        			div.setId("data-analysis-div");
        			div.setWidth("100%");
        			div.setHeight("calc(100% - 45px)");
        			div.setStyle("overflow:auto;");
					MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(sessionHelper.getLabel("Data Analysis"), 
						div, 
						new Messagebox.Button[]{Messagebox.Button.OK}, 
						masterWin, 
						null
					);
					dlg.setId("data-analysis-dlg");
					dlg.setWidth("100%");
					dlg.setHeight("100%");
					div.addEventListener("onPivotCallback", new ZkBiEventListener(){ 
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("got data:"+ event.getData());
						}
					});
        			Clients.evalJavaScript(String.format("startDataAnalysis('$data-analysis-div', %s, %s, %s, %s);", 
        					jsonDataArr.toString(), jsonColsArr.toString(), jsonRowsArr.toString()
        						,"{\"Number of Records\":function() { return pivottpl.count()() }}"	
        					));
					dlg.doHighlighted();
        		}
        	}
        );
    }
    
	String stripNonPrintable(String p_s)
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
    Object getPoiObjectByBiCol(ExcelPoi exlpoi,BiColumn bc,int row,int col,boolean autoTranslate) throws Exception {
    	Object xo = null;
		//if(bc != null && bc.getColumnType().equals("date")) { ... }
		if(StringUtils.equalsAny(bc.getColumnType(),"date","datetime")) {  //andrew240201 allow datetime type
			xo = exlpoi.getDateValue(row, col);
			if(xo == null && (exlpoi.getStringValue(row,col) != null  && !exlpoi.getStringValue(row, col).isEmpty()) ) {
				if(!ignoreInvalidDate) {
					throw new Exception ("Input Error, Date Column "+ bc.getLabel() + " contains non-date value");
				} else {
					xo = DateUtil.zeroDate;
				}
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
				if( getSessionHelper().getLHLang().equals("SCHN")) {
					xo = ChineseConvert.convertAuto2Gnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
				} else {
					xo = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
				}
    		}  else
    			xo = stripNonPrintable(exlpoi.getStringValue(row, col));
//			xo = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
//			xo = exlpoi.getStringValue(row, col);
		}
		return(xo);
    }
    String getPoiStringByBiCol(ExcelPoi exlpoi,BiColumn bc,int row,int col) throws Exception {
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
			return String.valueOf(xo);
		} 
		else if(bc != null && bc.getColumnType().equals("integer")) {
			Integer xo = exlpoi.getIntegerValue(row, col);
			return String.valueOf(xo);
		} 
		else if(bc != null && bc.getColumnType().equals("float")) {
			Double xo = exlpoi.getDoubleValue(row, col);
			if (StringUtils.isNotBlank(bc.getFormat()))
				return new DecimalFormat(bc.getFormat()).format(xo);
			return String.format("%.2f", xo);
		} 
		else if(bc != null && bc.getColumnType().equals("money")) {
			Double xo = exlpoi.getDoubleValue(row, col);
			if (StringUtils.isNotBlank(bc.getFormat()))
				return new DecimalFormat(bc.getFormat()).format(xo);
			return String.format("%.2f", xo);
		} 
		else 
			return ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
    }
    public BiColumn findBiColumnByHeader(BiResult result,String colHdr) {
    	return findBiColumnByHeader(result,colHdr,-1);
    }
    public BiColumn findBiColumnByHeader(BiResult result,String colHdr,int p_idx) {
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
    
//    boolean processImportDetailColumn(HashMap<BiView,ImpDetailColumnRec> impDetailHash,BiColumn bc,BiResult result,Object xo) throws CellException
//    {
//    	boolean hasDetail = false;
//		if(impDetailHash.containsKey(bc.getView())) {
//			BiResult sr = result.getSubLink(bc.getView().getName());
//			ImpDetailColumnRec idcr = impDetailHash.get(bc.getView());
//			BiCellCollection col = null;
//			if(idcr != null) col = impDetailHash.get(bc.getView()).col;
//			if(col == null) {
//			    if(idcr == null) {
//			    	idcr = new ImpDetailColumnRec();
//			    	impDetailHash.put(sr.getView(), idcr);
//			    	int nn = sr.getRowCount();
//			    	for(int j =0;j<nn;j++) {
//			    		Object o = sr.getTrStatObj(j);
//			    		if(o != null) sr.markDelete(o, true);
//			    	}
//			    }
//			    if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
//			    	col = sr.newRowCollection();
//			    	ReturnMsg rtn = sr.addSubRecord(col, -1,"");
//			    	idcr.setCollection(col);
//			    	hasDetail = true;
//			    }
//			}
//			if(col != null) {
//				 if(idcr.contains(bc.getLabel())) {
//			    	col = sr.newRowCollection();
//			    	ReturnMsg rtn = sr.addSubRecord(col, -1,"");
//			    	idcr.setCollection(col);
//			    	if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
//			    		col.getCell(bc.getLabel()).set(xo);
//			    	}
//				 } else col.getCell(bc.getLabel()).set(xo);
//			}
//		}
//		return(hasDetail);
//    }
    
    void updateImportcell(Cell p_cell,Object p_object) throws CellException {
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
    boolean compareImportcell(Cell p_cell,Object p_object) throws CellException {
    	if(p_object == null) return(true);
    	if(p_object instanceof String) {
    		if(StringUtils.isBlank((String) p_object)) return(true);
    	}
    	return(p_cell.equals(p_object));
    }
    boolean processImportDetailColumn(HashMap<BiView,ImpDetailColumnRec> impDetailHash,BiColumn bc,BiResult result,Object xo) throws CellException
    {
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
			    	updateImportcell(col.getCell(bc.getLabel()),xo);
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
			    	updateImportcell(col.getCell(bc.getLabel()),xo);
				}
				/*
				if(xo != null 
						 && 
						 (!(xo instanceof String) || !(((String) xo).equals("")) || (col != null &&   !col.getString(bc.getLabel()).equals(xo)) )  ) {
					if(col == null) {
						col = sr.newRowCollection();
						ReturnMsg rtn = sr.addSubRecord(col, -1,"");
						idcr.setCollection(col);
					}
					idcr.setUpdCols(bc.getLabel());
//			    	col.getCell(bc.getLabel()).set(xo);
			    	updateImportcell(col.getCell(bc.getLabel()),xo);
				}	
				*/
			}
		    

//			if(col == null) {
//				if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
//			    	col = sr.newRowCollection();
//			    	ReturnMsg rtn = sr.addSubRecord(col, -1,"");
//			    	idcr.setCollection(col);
//			    	hasDetail = true;
//
//					idcr.setUpdCols(bc.getLabel());
//			    	col.getCell(bc.getLabel()).set(xo);
//				}
//			} else {
//				if(idcr.contains(bc.getLabel())) {
//			    	col = sr.newRowCollection();
//			    	ReturnMsg rtn = sr.addSubRecord(col, -1,"");
//			    	idcr.setCollection(col);
//				} else {
//					idcr.setUpdCols(bc.getLabel());
//			    	col.getCell(bc.getLabel()).set(xo);
//				}
//			}
			
			
		}
		return(hasDetail);
    }
    protected void setupExtraButton(final BiResult result)
    {
    	if(getSessionHelper().hasAccessRight("updFormula")) {
	       	final Button btn = new ZkBiButton();
	        btn.setLabel("Update Formula");
	        btn.setId("btUpdFormula");
	        btn.addEventListener("onClick",
	        	new EventListener() {
	        		public void onEvent(Event event) throws Exception {
	        			UniLog.log("HAHA update formula");
	        			try {
//	        				final ZkForm zkf1 = new ZkForm(null,"zkf/webmenu001.zul");
	        				final ZkForm zkf1 = new ZkForm(null,"zkf/UpdFormula.zul");
	        				final CellCollection col = new CellCollection();
	        				/*
	        				Cell colList = new Cell(-1);
	        				Vector vv = new Vector();
	        				
	        				for (BiColumn bc : result.getView().getColumns()){
	        					vv.add(BiPair.of(bc.getLabel(), bc.getEngName()));
	        				}
	        				colList.setItemPropertyInterface(new PairedItemList(vv));
	        				*/
	        				Cell colList = new Cell("");
	        				Vector vv = new Vector();
	        				for (BiColumn bc : result.getView().getColumns()){
	        					vv.add(bc.getLabel());
	        				}
	        				colList.setItemList(vv);
	        				col.addCell("updFormulaCol", colList);
	        				col.addCell("firstPosition", new Cell("A2"));
	        				zkf1.doModal(col,new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
									if(arg0.getTarget().getId().equals("updFormulaCol" )) {
//										BiColumn bc = result.getColumnByLabel(col.getCellString("updFormulaCol"));
										ColumnCell cc = result.getCell(col.getCellString("updFormulaCol"));
										if(cc != null && cc.getFormula() != null) {
											col.getCell("updFormulaStr").set(cc.getFormula().toString());
										} else {
											col.getCell("updFormulaStr").set("");
										}
									} else if(arg0.getTarget().getId().equals("updFormulaXls")) {
										UniLog.log("Excel formula changed");
										String xlsf = col.getCellString("updFormulaXls");
										if(!StringUtils.isBlank(xlsf)) {
											try {
//												{
//													Object parseResult;
//													com.kyoko.parser.excelformula.Parser yyparser = new com.kyoko.parser.excelformula.Parser(
//														new CellPositionInterface () {
//
//															@Override
//															public int getColIdx() {
//																// TODO Auto-generated method stub
//																return 0;
//															}
//
//															@Override
//															public int getRowIdx() {
//																// TODO Auto-generated method stub
//																return 0;
//															}
//															
//														}
//															);
//													parseResult = yyparser.parse("B1C1 + B2C2");	
//													if(!(parseResult instanceof com.kyoko.parser.Expression)) {
//														// error
//													} else {
//														com.kyoko.parser.Expression translatedExpression = 
//															com.kyoko.parser.ExcelTranslate.translateFromXlsToBi((com.kyoko.parser.Expression) parseResult,null,null,
//																	new ColumnTranslateInterface() {
//
//																		@Override
//																		public String cellColumnToBiColumn(
//																				String p_workSheet, int col)
//																				throws CellException {
//																			// TODO Auto-generated method stub
//																			return null;
//																		}
//
//																		@Override
//																		public int biColumnToCellColumn(
//																				String p_workSheet, String p_label)
//																				throws CellException {
//																			// TODO Auto-generated method stub
//																			return 0;
//																		}
//
//																		@Override
//																		public String getWorkSheetFirstValuePosition(
//																				String p_worksheet) {
//																			// TODO Auto-generated method stub
//																			return null;
//																		}
//																
//															}
//															);	
//														String translatedFormula = translatedExpression.toString();
//													}
//												}
												Pair<Boolean,Integer>[] zeropos = ExcelCellRef.decodeExcelRC(
														col.getCellString("firstPosition")
															) ;
												CellPositionInterface gpf = new CellPositionInterface() {

													@Override
													public int getColIdx() {
														// TODO Auto-generated method stub
														return(zeropos[0].getRight() + vv.indexOf(col.getCellString("updFormulaCol")));
													}

													@Override
													public int getRowIdx() {
														// TODO Auto-generated method stub
														return(zeropos[1].getRight());
													}
													
												};
												com.kyoko.parser.excelformula.Parser yyparser = new com.kyoko.parser.excelformula.Parser(gpf);
												Object parseResult = yyparser.parse(xlsf);
												if(parseResult == null || ! (parseResult instanceof com.kyoko.parser.Expression)) {
													ZkUtil.showMsg("Formula Syntex Invalid");
													return;
												}
												HashMap<String,String[]> xlsToBiMap = new HashMap<String,String[]>();

												/* use either depends on whether the workshet refer to the current instance of BiResult or to the BiView() */
//												Vector<BiColumn>cl = result.getView().getColumns();
												Vector<BiColumn>cl = result.getListColumns();
												String[]  colStr = new String[cl.size()];
												for(int i=0;i<colStr.length;i++) {
													colStr[i] = cl.get(i).getLabel();
												}
												xlsToBiMap.put(null,colStr);
												
												HashMap<String,String> firstPositionMap = new HashMap<String,String>();
												firstPositionMap.put(null, col.getCellString("firstPosition"));
												
												com.kyoko.parser.Expression translatedExpression = 
															com.kyoko.parser.ExcelTranslate.translateFromXlsToBi((com.kyoko.parser.Expression) parseResult,null,null,
																	new ExcelColToBiColumn(getSessionHelper(),xlsToBiMap,firstPositionMap)
																		);
												
												col.getCell("updFormulaStr").set(translatedExpression.toString());
											} catch (Exception ex){ 
												ZkUtil.showMsg("Formula Syntex Invalid");
												UniLog.log(ex);
											}
										}
									} if(arg0.getTarget().getId().equals("btOK")) {
										/*
										BiColumn bc = result.getColumnByLabel(col.getCellString("updFormulaCol"));
										bc.setFormula(col.getCellString("updFormulaStr"));
										*/
										result.updateFormula(col.getCellString("updFormulaCol"), col.getCellString("updFormulaStr"));
										zkf1.exitModal();
									}
									if(arg0.getTarget().getId().equals("btCancel")) {
										zkf1.exitModal();
									}
								}
	        				}
	        				);
	        			} catch (Exception ex) {
	        				UniLog.log(ex);
	        			}
	        		}
	        	}
	        );	
	        abHelper.addButton(btn);
    	}
    	if(getSessionHelper().hasAccessRight("allowRecal")) {
	       	final Button btn = new ZkBiButton();
	        btn.setLabel("Recal");
	        btn.setId("btRecal");
	        btn.setTooltiptext("Recalculate Formula. (no db update)");
	        btn.addEventListener("onClick",
	        	new ZkBiEventListener() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			UniLog.log("HAHA recal");
	        			result.recal();
        			    refreshListItems(null);
        			    ZkUtil.msg("Recal Completed");
	        		}
	        	}
	        );	
	        abHelper.addButton(btn);

	       	final Button btBatchAUD = new ZkBiButton();
	        btBatchAUD.setLabel("Batch A/U/D");
	        btBatchAUD.setId("btBatchAUD");
	        btBatchAUD.setTooltiptext("Save AUD to db");
	        btBatchAUD.addEventListener("onClick",
	        	new ZkBiEventListener() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			result.batchAddUpdateDelete();
        			    result.unMarkAll();
        			    result.clearCurrentRec();
        			    result.invalidateLoadCache();
        			    listModelList.clear();
        			    listModelList.addAll(result.getResultStat());
        			    doSearch(true, false);
        			    ZkUtil.msg("Batch AUD Completed");
	        		}
	        	}
	        );	
	        abHelper.addButton(btBatchAUD);
    	}
       	if(getSessionHelper().hasAccessRight("allowRecal")) {
	       	final Button btn = new ZkBiButton();
	        btn.setLabel("Recal&UpdateCacheAll");
	        btn.setTooltiptext("Recalculate All Worksheet and update to db");
	        btn.setId("btRecalUpdCacheAll");
	        btn.addEventListener("onClick",
	        	new ZkBiEventListener() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			ExcelWorkSheetCache.cacheAllWorkSheet(getSessionHelper(),true);  //much faster
	        			//ExcelCellCollection.cacheAllWorkSheet(getSessionHelper(),false);  //much slower
	        			ExcelWorkSheetCache.recalAndUpdateAllCache(result.getView().getSchema(),false);
        			    result.unMarkAll();
        			    result.clearCurrentRec();
        			    result.invalidateLoadCache();
        			    refresh(result,masterWin,-1,true,true); 
        			    ZkUtil.msg("Recal and Update Completed. view:" + result.getView().getName());
	        		}
	        	}
	        );	
	        abHelper.addButton(btn);
    	}
    	if(getSessionHelper().hasAccessRight("#recalUpd")) {
	       	//final Button btn = new ZkBiButton();
	       	btRecalUpd = new ZkBiButton();
	        btRecalUpd.setLabel(sessionHelper.getBtLabel("Recal&Update"));
	        btRecalUpd.setTooltiptext("Recalculate Formula and update to db");
	        btRecalUpd.setId("btRecalUpd");
	        btRecalUpd.addEventListener("onClick",
	        	new ZkBiEventListener() {
	        		public void onZkBiEvent(Event event) throws Exception {
	        			BiResult rBr = result.getView().newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
	        			rBr.query();
	        			rBr.recal();
	        			rBr.batchAddUpdateDelete();
	        			ExcelWorkSheetCache.setDirty(rBr.getView().getName(),false);
	        			ExcelWorkSheetCache.clearBrCache(rBr.getView().getSchema(),rBr.getView().getName());
	        			{
	    HashSet<String> aliasSet = result.getView().getSchema().getAliasSet(result.getView().getName());
	    if(aliasSet != null) {
	    	for(String vN : aliasSet) {
	    		ExcelWorkSheetCache.clearBrCache(result.getView().getSchema(),vN);
	    	}
	    }
		Vector v = result.getSubLinks();
		if(v != null) {
			for(int i = 0;i < v.size();i++) {
				BiResult sr = (BiResult) v.get(i);
	    		ExcelWorkSheetCache.clearBrCache(result.getView().getSchema(),sr.getView().getName());
			}
		}
	        			}
	        			
	        			ExcelWorkSheetCache.cacheAndRecalBr(getSessionHelper(),rBr.getView().getName());
        			    result.unMarkAll();
        			    result.clearCurrentRec();
        			    result.invalidateLoadCache();
        			    refresh(result,masterWin,-1,true,true); 
        			    ZkUtil.msg("%s (Recal and Update Completed)", sessionHelper.getLabel("Data updated"));
	        		}
	        	}
	        );	
	        abHelper.addButton(btRecalUpd);
    	}
    	
    	String ggg = BiConfig.getString(getSessionHelper(),"InvPaperType");
    	for(int i=0;i<10;i++) {
    		String ss = ((ZkSessionHelper) getSessionHelper()).getViewExtraBatchAction(result.getView().getName(),i);
    		if(ss == null) break;
    		String ss2[] = ss.split(","); // 0 : buttonLabel, 1 : buttonIcon, 2 : accessKey, 3 : classPath
			try {
				BiActionHandler bah = null;
				Class[]	paramTypes = new Class[]{ZkBiComposerBase.class};
				bah = (BiActionHandler) DynamicClassLoader.newInstance(ss2[3],paramTypes,this);
				Button batchActionButton = addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, ss2[2],"btExtraBatchAction_"+i,sessionHelper.getBtLabel(ss2[0]),ss2[1],bah);
				if(!bah.isVisible(result, true)) batchActionButton.setVisible(false);
				if(bah.isDisabled(result, true)) batchActionButton.setDisabled(true);
				if(bahHash == null) {
					bahHash = new Hashtable<String,BiActionHandler>();
				}
				bahHash.put("btExtraBatchAction_"+i, bah);
			} catch (Exception ex){
				UniLog.log(ex);
			}
    	}
    }
    
    protected void setupImpExpTranlateButton(final BiResult result)
    {
    	if(!getSessionHelper().hasAccessRight("#impexptl")) return;
    	btImportTranslate = new ZkBiButton();
    	btImportTranslate.setLabel(sessionHelper.getBtLabel("ImportTranslate"));
		btImportTranslate.setImage("images/icons/zkweb/020-import-25x25.png");
    	btImportTranslate.setId("btImportTranslate");
    	btImportTranslate.setTooltiptext(sessionHelper.getTtLabel("Import Translate"));
    	btImportTranslate.addEventListener("onClick", new ZkBiEventListener <Event> () {
    		public void onZkBiEvent(Event event) throws Exception {
//    		-0	mConditionPresets.
    			Fileupload.get(new ZkBiEventListener <UploadEvent>(){
		    		public void onZkBiEvent(UploadEvent event) {
		        		UniLog.log("translate template upload event catched");
		        		org.zkoss.util.media.Media media = event.getMedia();
		        		if (media instanceof org.zkoss.util.media.AMedia) {
		        			final org.zkoss.util.media.AMedia amedia = (org.zkoss.util.media.AMedia) media;
		        			UniLog.log("amedia:" + amedia.getContentType() + "," + amedia.getFormat() + "," + amedia.getName());
		        			String jstr = new String(amedia.getByteData());
	                        try {
	                        	JSONObject jo = new JSONObject(jstr);
	                        	Iterator<String> keys = jo.keys();
	                        	importExportMap = new ArrayList<Pair<String,String>>();
	                        	while (keys.hasNext()) {
	                        	    String key = keys.next();
	                        	    String value = jo.getString(key);
	                        	    System.out.println(key + ": " + value);
	                        	    importExportMap.add(Pair.of(key, value));
	                        	}
	                        } catch (Exception ex) {
	                        	UniLog.log(ex);
	                        	Messagebox.show("Error Importing Translate Table");
	                        }
		        		}
//		        		Media media = event.getMedia();
//		        		if(media != null) {
//	                		String jstr = media.getStringData();
//	                        try {
//	                        	JSONObject jo = new JSONObject(jstr);
//	                        	Iterator<String> keys = jo.keys();
//	                        	importExportMap = new ArrayList<Pair<String,String>>();
//	                        	while (keys.hasNext()) {
//	                        	    String key = keys.next();
//	                        	    String value = jo.getString(key);
//	                        	    System.out.println(key + ": " + value);
//	                        	    importExportMap.add(Pair.of(key, value));
//	                        	}
//	                        } catch (Exception ex) {
//	                        	UniLog.log(ex);
//	                        	Messagebox.show("Error Importing Translate Table");
//	                        }
//		        		}
		    		}
		    	});
    		}
		});
    	btExportTranslate = new ZkBiButton();
    	btExportTranslate.setLabel(sessionHelper.getBtLabel("ExportTranslate"));
		btExportTranslate.setImage("images/icons/zkweb/019-export-25x25.png");
    	btExportTranslate.setId("btExportTranslate");
    	btExportTranslate.setTooltiptext(sessionHelper.getTtLabel("Export Translate"));
    	btExportTranslate.addEventListener("onClick", new ZkBiEventListener <Event> () {
    		public void onZkBiEvent(Event event) throws Exception {
    			Fileupload.get(new ZkBiEventListener <UploadEvent>(){
		    		public void onZkBiEvent(UploadEvent event) {
		        		UniLog.log("translate template upload event catched");
		    		}
		    	});
    		}
		});
    }
    protected EventListener<Event> getImportButtonEventListener(BiResult result) {
    	return new ImportButtonEventListener(result);
    }
    protected class ImportButtonEventListener extends ZkBiEventListener<Event> {
    		protected BiResult result;
    		public ImportButtonEventListener(BiResult result) {
    			this.result = result;
    		}
    		public class UploadEventListener extends ZkBiEventListener<UploadEvent> {
               		ExcelPoi exlpoi = null;
    				int currRow = -1;
    				Integer pkCol = null;
    				String pkColName = null;
    				String pkFieldName="";
    				
    				private Component buildConfirmImportColumnsComponent(List<Map<String, Object>> list) {
    					return new GridHelper(5) {{
    						setSclass("confirmImportColumns");
		  					setStyle("max-height:calc(100vh - 130px);");
		  					setLabels("Excel Column", "Excel Column Header", "Result", "ERP Column Id", "ERP Column Header");
		  					int j = 0;
		  					for (Map<String, Object> m : list) {
		  						String result = (String)m.get("result");
		  						BiColumn bc = (BiColumn)m.get("biColumn");
		  						Label resultLabel = new Label();
		  						switch (result) {
		  						case "Match":
		  						case "Not Match":
		  							resultLabel.setValue(result);
		  							break;
		  						case "Skip":
		  							resultLabel.setValue("Match");
		  							resultLabel.setStyle("color:blue");
		  							break;
		  						}
		  						addRow(new Label(CellReference.convertNumToColString(j++)),
		  								new Label((String)m.get("excelColumnHeader")),
		  								resultLabel,
		  								new Label(bc != null ? bc.getLabel() : ""),
		  								new Label(bc != null ? bc.getEngName() : ""));
		  					}
		  					ZkUtil.js("$('div.z-grid.confirmImportColumns .z-grid-body').css('max-height', 'calc(100vh - 130px - 31px)')");
		  				}};
    				}

		    		public void onZkBiEvent(UploadEvent event) {
		        		UniLog.log("upload event catched");
		                org.zkoss.util.media.Media media = event.getMedia();
		                if(media != null) {
		                	if(sessionHelper.useNewImport()) {
		                	if(result.getPrimaryColumns() == null) {
		                		Messagebox.show(sessionHelper.getLabel("Primary Columns not defined or include in this page, Cannot Import"));	
		                		return;
		                	}
		                	long startRunTime = System.currentTimeMillis();
		                	try  {
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
		                		List <String> impColList = new ArrayList<String>();
		                		int maxColumn = exlpoi.excel_getColumnCount(0);
		                		if(maxColumn > MAX_IMPORT_COLUMNS) maxColumn = MAX_IMPORT_COLUMNS;
		                		for(int i = 0;i < maxColumn;i++) {
		                			String colhdr = exlpoi.getStringValue(0, i);
		                			if(colhdr != null && !colhdr.trim().equals("")) {
		                				colhdr = translateFromExcelHeader(colhdr);
		                				impColList.add(colhdr);
		                			} else impColList.add(null);
		                		}
			    				Vector v = new Vector();
			    				Listhead lh = listbox.getListhead();
			    				List <Component> lhdrs = lh.getChildren();
			    				HashMap <Integer,BiColumn> impColHash = new HashMap <Integer,BiColumn> ();
			    				HashMap <Integer,BiColumn> skipColHash = new HashMap <Integer,BiColumn> ();
			    				Vector xv = result.getListColumns();
			    				BiTable masterTable = result.getView().getTable();
			    				HashMap<BiView,ImpDetailColumnRec> impDetailHash = new HashMap<BiView,ImpDetailColumnRec>();
			    				for(int j=0;j<impColList.size();j++) {
			    					BiColumn bl = null;
			    					String colHdr = impColList.get(j);
			    					if(colHdr != null) {
			    						bl = findBiColumnByHeader(result,colHdr,0);
			    					} else {
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
			    												if(n >= xv.size()) break;
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
			    					if(bl != null && bl.isSkipImport()) {
			    						skipColHash.put(j, bl);
			    						bl=null;
			    					}
			    					if(bl == null) {
		    							Vector<BiResult> srList = result.getSubLinks();
		    							if(srList != null) {
		    							for(BiResult sr : srList) {
		    								if(!result.getView().inExportList(sr.getView())) continue;
		    								bl = findExportColumnByHeader(sr,colHdr);
		    								if(bl != null && !bl.isSkipImport()) {
		    									impColHash.put(new Integer(j),bl);
		    									if(impDetailHash.get(sr.getView()) == null) 
		    										impDetailHash.put(sr.getView(), null);
		    									break;
		    								} else {
		    									UniLog.log("skip unknown column " + j);
		    									if (bl != null && bl.isSkipImport())
		    										skipColHash.put(j, bl);
		    								}
		    							}
		    							}
			    					} else {
			    						impColHash.put(new Integer(j),bl);
//			    						String pkeys[] = masterTable.getPrimaryKeys();
//			    						String pk = null;
//			    						if(pkeys != null && pkeys.length > 0)  pk = pkeys[0];
//			    						if(pk==null) {
//			    							if(bl.getLabel().equals("serial_id")) {
//			    								pkCol = new Integer(j);
//			    								pkFieldName = masterTable.getSerialId() + " = ";
//			    							}
//			    						} else {
//			    							if(bl.getField() != null && bl.getField().getName().equals(pk)) {
//			    								pkCol = new Integer(j);
//			    								pkFieldName = bl.getLabel() + " = ";
//			    							}
//			    						}
			    					}
			    				}
		    					{
		    						List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		    						for(int j=0;j<impColList.size();j++) {
		    							list.add(MapUtil.of("excelColumnHeader", impColList.get(j),
		    												"result", impColHash.containsKey(j) ? "Match" : skipColHash.containsKey(j) ? "Skip" : "Not Match",
		    												"biColumn", impColHash.containsKey(j) ? impColHash.get(j) : skipColHash.containsKey(j) ? skipColHash.get(j) : null
		    											));
		    						}
		    						final ZkBiMsgboxButton btnContinue = new ZkBiMsgboxButton(sessionHelper.getBtLabel("Continue"));
		    						final Checkbox cbAutoConfirm = new Checkbox(sessionHelper.getLabel("Auto confirm after import"));
									new ZkBiMsgbox(sessionHelper)
										.setContent(buildConfirmImportColumnsComponent(list))
										.setButtons(new ZkBiMsgboxButton[] {btnContinue,new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))})
										.setEventListener(new ZkBiEventListener<Event>() {
											@Override
											public void onZkBiEvent(Event event) throws Exception {
												ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
												if (btn.getIdx() == 0) {
													try {
									    				ReturnMsg rtnMsg = null;
									    					
									    				int nAdded = 0;
									    				int nUpdated = 0;
									    				int nSkipped = 0;
									    				if(importAsSingle) result.beginWork();
									    				int n;
									    				Object ko = null;
					//				    				boolean translateToBig5 = !result.getView().getSchema().autoTranslate;
									    				for (int i=1;i<exlpoi.getRowCount();) {
									    					n = 1;
									    					currRow = i;
									    					boolean isUpdate = false;
									    					for(BiView bv : impDetailHash.keySet()) {
									    						impDetailHash.put(bv, null);
									    					}
									    					result.clearCurrentRec();
									    					boolean hasValue = false;
									    					boolean hasDetail = false;
									    					for(;;) {
									    						hasValue = false;
									    						hasDetail = false;
//										    					for(Integer xc : impColHash.keySet()) {
										    					for(int xc = 0;xc<impColList.size();xc++) {
//										    						BiColumn bc = impColHash.get(xc);
										    						BiColumn bc;
										    						if((bc = impColHash.get(xc)) == null) continue;
										    						/* 2025-01-10 , by dt, this is pretty tricky */
										    						if( !bc.isNoUpdate(getSessionHelper()) ||
										    							(!isUpdate  && !bc.isNoEntry(getSessionHelper())) ) {
										    							Object xo = getPoiObjectByBiCol(exlpoi,bc,i,xc,bc.isAutoTranslate());
									    								if(bc.getView() == result.getView()) {
									    									/* HAHAXXX */
									    									if( (xo != null && (!(xo instanceof String) || !((String) xo).equals("")))) {
										    									Cell ce = result.getCell(bc.getLabel());
										    									if(ce.getItemPropertyInterface() != null) {
									    											String ss = xo.toString();
									    											AbstractGetItemProperty gipi = ce.getItemPropertyInterface();
									    											boolean matched = false;
									    											for(int k=0;k<gipi.getRowCount();k++) {
									    												Object o = gipi.getRow(k);
									    												if(gipi.getString(o).equals(ss)) {
									    													ce.sync(o);
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
									    											ce.sync(idx);
										    									} else  {
									    											if(xo == null) {
									    												switch(ce.getType()) {
									    												case Cell.VTYPE_DATE : ce.sync(DateUtil.zeroDate); break;
									    												case Cell.VTYPE_DOUBLE : ce.sync(0.0); break;
									    												case Cell.VTYPE_INT: ce.sync(0); break;
									    												case Cell.VTYPE_BOOLEAN: ce.sync(false); break;
									    												default : ce.sync(""); break;
									    												}
									    											} else
									    												ce.sync(xo);
										    									}
									    									} else {
									    										UniLog.log("Excel Cell is null or blank in add mode, skipped");
									    									}
						//			    									result.getCell(bc.getLabel()).set(xo);
									    									if( (xo != null && (!(xo instanceof String) || !((String) xo).equals("")))) {
									    										hasValue = true;
									    									}
									    								} else {
									    									if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
									    										hasDetail = true;
									    									}
									    								}
										    						}				    						
										    						
										    					}
									    						if( !isUpdate && result.getPrimaryColumns() != null) {
									    							
									    							Wherecl wcl = new Wherecl();
									    							for(int m=0;m<result.getPrimaryColumns().length;m++) {
									    								BiColumn kbc = result.getPrimaryColumns()[m];
									    								if(kbc.isformulaOnSave()) {
									    									Cell c = result.getCell(kbc.getLabel());
									    									int ignoreCase = result.parserIgnoreCase();
									    									com.uniinformation.utils.exprpar.Parser parser 
									    										= new com.uniinformation.utils.exprpar.Parser(ignoreCase,kbc.getFormula(result.getParent() == null),result.getCurrentCollection(),result.getCurrentCollection());
									    									ko = parser.evaluate();
									    									if(ko instanceof IgnoreValue) {
									    										ko = result.getCell(kbc.getLabel()).getObject();
									    									}
									    								} else {
									    									ko = result.getCell(kbc.getLabel()).getObject();
									    								}
									    								wcl.andUniop(kbc.getField().getFullName(), "=", ko);
									    							}
									    							TableRec tr = result.getSelectUtil().getQueryResult("select serial_id from "+masterTable.getSelectFromName(), wcl);
									    							if(tr.getRecordCount() > 0) {
									    								tr.setRecPointer(0);
									    								int serial_id = tr.getFieldInt("serial_id");
									    								result.clearCondition();
									    								wcl = new Wherecl();
									    								wcl.andUniop(masterTable.getSidField(),"=",serial_id);
									    								result.appendWherecl(wcl);
							    										ReturnMsg rtn = result.query(false);
							    										if(rtn == null || rtn.getStatus()) {
							    											UniLog.log("record fetched = "+ result.getRowCount());
							    											result.loadOneRecV(0);
							    											result.fetchOneRecV(0);
							    											isUpdate = true;
							    											for(BiView bv : impDetailHash.keySet()) {
							    												impDetailHash.put(bv, null);
							    											}
							    											continue;
							    										}
									    							}
									    						}
									    						break;
									    					}
									    					if(hasDetail) {
									    						boolean cm = result.getView().compareMasterWhenImport();
									    						for (int k=i+1;k<exlpoi.getRowCount();k++) {
									    							boolean hasMaster = false;
									    							for(Integer xc : impColHash.keySet()) {
									    								BiColumn bc = impColHash.get(xc);
									    								Object xo = getPoiObjectByBiCol(exlpoi,bc,k,xc.intValue(),bc.isAutoTranslate());
									    								if(cm) {
										    								if(bc.getView() == result.getView()) {
										    									if(
									    											(isUpdate && !bc.isNoUpdate(getSessionHelper())) ||
									    											(!isUpdate && !bc.isNoEntry(getSessionHelper())) ) {
										    										if(!compareImportcell(result.getCell(bc.getLabel()),xo)) {
										    											hasMaster = true;
										    											break;
										    										}
										    									}
										    								}
									    								} else {
										    								if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
										    									if(
										    											(isUpdate && !bc.isNoUpdate(getSessionHelper())) ||
										    											(!isUpdate && !bc.isNoEntry(getSessionHelper())) ) {
										    										if(bc.getView() == result.getView()) {
										    											hasMaster = true;
										    											break;
										    										} 
					
										    									}
										    								}
									    								}
									    							}
									    							if(hasMaster) break;
									    							for(Integer xc : impColHash.keySet()) {
									    								BiColumn bc = impColHash.get(xc);
									    								Object xo = getPoiObjectByBiCol(exlpoi,bc,k,xc.intValue(),bc.isAutoTranslate());
								    									if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
					//			    										hasDetail = true;
								    									}
									    								
									    							}
					//				    							for(BiView bv : impDetailHash.keySet()) {
					//				    								impDetailHash.put(bv, null);
					//				    							}
					//				    							if(hasMaster) break; else n++;
									    							n++;
									    						}
									    					}
									    					if(hasValue) {
									    						if(isUpdate) {
									    							UniLog.log("Excel Import Update Record " + i);
									    							rtnMsg = result.updateCurrent();
									    							if(rtnMsg != null && !rtnMsg.getStatus()) {
									    								break;
									    							}
									    							nUpdated++;
									    						} else {
									    							UniLog.log("Excel Import Insert Record " + i);
					//				    							result.doBeforeAdd();
									    							rtnMsg = result.addCurrent();
									    							if(rtnMsg != null && !rtnMsg.getStatus()) {
									    								break;
									    							}
									    							nAdded++;
									    						}
									    					} else break;
									    					
									    					i+=n;
									    				}

								    
									    				UniLog.log1("total run time:%d", System.currentTimeMillis() - startRunTime);
										    			String strMsg;
										    			if(rtnMsg != null && !rtnMsg.getStatus()) {
										    				String rowStr;
										    				if(rtnMsg.getData() != null && rtnMsg.getData() instanceof Integer) {
										    					int detIdx = (Integer) rtnMsg.getData();
										    					rowStr = " row " + (currRow+detIdx);
										    				} else {
										    					rowStr = " row " + currRow;
										    				}
										    				rowStr +=  " key = ("+(ko == null ? null : ko.toString())+")";
										    				strMsg = "Update Incomplete : at " + rowStr + " " + rtnMsg.getMsg() + ", " + nAdded + " Added " + nUpdated + " Updated " + nSkipped + " Skipped" ;
										    				Messagebox.show(
									    						strMsg,
									    						sessionHelper.getLabel("Import Error"), 
									    						Messagebox.OK , 
									    						Messagebox.ERROR, 
									    						new ZkBiEventListener() {
									    							public void onZkBiEvent(Event evt) throws InterruptedException {
									    								UniLog.log("rollback work due to import error");
									    								if(importAsSingle) result.rollbackWork();
									    								refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
									    							}
									    						});	
										    			} else {
										    				//strMsg = "Update Completed : " + nAdded + " Added " + nUpdated + " Updated " + nSkipped + " Skipped" ;
										    				strMsg = String.format(sessionHelper.getLabel("Update Completed: %d Added, %d Updated, %d Skipped"), nAdded, nUpdated, nSkipped);
										    				if(rtnMsg != null) strMsg += rtnMsg.getMsg();
										    				if (cbAutoConfirm.isChecked()) {
					    										UniLog.log("Confirm OK commit work");
										    					try {
										    						if(!result.commitWork()) {
										    							Messagebox.show(sessionHelper.getLabel("Commit Work Failed, please upload again"));
										    						}
									    						} catch (Exception ex) {
										    						UniLog.log(ex);
										    						Messagebox.show(ex.toString());
										    					}
										    					refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
										    					Messagebox.show(strMsg, sessionHelper.getLabel("Information"), Messagebox.OK, Messagebox.INFORMATION);
										    				} else {
											    				Messagebox.show(
										    						strMsg,
										    						sessionHelper.getLabel("Confirm Save Changes?"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {
										    							Timer klTimer = null;
										    							{
										    								if (importAsSingle) {
										    									klTimer = new Timer();
										    									klTimer.setPage(masterWin.getPage());
										    									klTimer.setDelay(30000);
										    									klTimer.setRepeats(true);
										    									klTimer.addEventListener(Events.ON_TIMER, (Event) -> {
										    										UniLog.log1("doKeepAlive fbegin %b", result.inBeginWork());
										    										result.getSelectUtil().setAliveUntil(30000);
										    									});
										    									klTimer.setRunning(true);
										    								}
										    							}
										    							public void onZkBiEvent(Event evt) throws InterruptedException {
								    										if (klTimer != null) {
								    											klTimer.stop();
								    											klTimer.detach();
								    										}
										    								if(importAsSingle) {
										    									if (evt.getName().equals("onOK")) {
										    										UniLog.log("Confirm OK commit work");
										    										try {
										    											if(!result.commitWork()) {
										    												Messagebox.show(sessionHelper.getLabel("Commit Work Failed, please upload again"));
										    											}
										    										} catch (Exception ex) {
										    											UniLog.log(ex);
										    											Messagebox.show(ex.toString());
										    										}
										    										refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
										    									} else {
										    										UniLog.log("Confirm Canceled rollback work");
										    										result.rollbackWork();
										    										refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
										    									}
										    								}
										    							}
										    						});	
										    				}
										    			}
													} catch (Exception ex) {
														UniLog.log(ex);
														//Messagebox.show("Failed to load at row " + currRow + " : " + ex.toString());
														Messagebox.show(String.format(sessionHelper.getLabel("Failed to load at row %d: %s"), currRow, ex.toString()));
													}
													
												}
											}
										})
										.build().doModal();
										Component pc = btnContinue.getParent();
										pc.insertBefore(cbAutoConfirm, btnContinue);
										pc.insertBefore(new Space() {{ setWidth("10px"); }}, btnContinue);
		    						

    			
    			
    			
    			
    			
			    				}
		                	} 
		                	catch (Exception ex) {
		                		UniLog.log(ex);
		                		//Messagebox.show("Failed to load at row " + currRow + " : " + ex.toString());
		                		Messagebox.show(String.format(sessionHelper.getLabel("Failed to load at row %d: %s"), currRow, ex.toString()));
		                	}
		                		
		                	} else {
		                	if(result.getPrimaryColumns() != null && result.getPrimaryColumns().length > 1) {
		                		Messagebox.show(sessionHelper.getLabel("Primary Key with Multiple Columns not supported, Cannot Import"));	
		                		return;
		                	}
		                	long startRunTime = System.currentTimeMillis();
		                	try  {
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
		                		List <String> impColList = new ArrayList<String>();
		                		int maxColumn = exlpoi.excel_getColumnCount(0);
		                		if(maxColumn > MAX_IMPORT_COLUMNS) maxColumn = MAX_IMPORT_COLUMNS;
		                		for(int i = 0;i < maxColumn;i++) {
		                			String colhdr = exlpoi.getStringValue(0, i);
		                			if(colhdr != null && !colhdr.trim().equals("")) {
		                				colhdr = translateFromExcelHeader(colhdr);
		                				impColList.add(colhdr);
		                			} else impColList.add(null);
		                		}
			    				Vector v = new Vector();
			    				Listhead lh = listbox.getListhead();
			    				List <Component> lhdrs = lh.getChildren();
			    				HashMap <Integer,BiColumn> impColHash = new HashMap <Integer,BiColumn> ();
			    				HashMap <Integer,BiColumn> skipColHash = new HashMap <Integer,BiColumn> ();
			    				Vector xv = result.getListColumns();
			    				/*Integer pkCol = null;
			    				String pkColName = null;
			    				String pkFieldName="";*/
			    				BiTable masterTable = result.getView().getTable();
			    				HashMap<BiView,ImpDetailColumnRec> impDetailHash = new HashMap<BiView,ImpDetailColumnRec>();
			    				for(int j=0;j<impColList.size();j++) {
			    					BiColumn bl = null;
			    					String colHdr = impColList.get(j);
			    					if(colHdr != null) {
			    						bl = findBiColumnByHeader(result,colHdr,0);
			    					} else {
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
			    												if(n >= xv.size()) break;
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
			    					if(bl != null && bl.isSkipImport()) {
			    						skipColHash.put(j, bl);
			    						bl=null;
			    					}
			    					if(bl == null) {
		    							Vector<BiResult> srList = result.getSubLinks();
		    							if(srList != null) {
		    							for(BiResult sr : srList) {
		    								if(!result.getView().inExportList(sr.getView())) continue;
		    								bl = findExportColumnByHeader(sr,colHdr);
		    								if(bl != null && !bl.isSkipImport()) {
		    									impColHash.put(new Integer(j),bl);
		    									if(impDetailHash.get(sr.getView()) == null) 
		    										impDetailHash.put(sr.getView(), null);
		    									break;
		    								} else {
		    									UniLog.log("skip unknown column " + j);
		    									if (bl != null && bl.isSkipImport())
		    										skipColHash.put(j, bl);
		    								}
		    							}
		    							}
			    					} else {
			    						impColHash.put(new Integer(j),bl);
			    						String pkeys[] = masterTable.getPrimaryKeys();
			    						String pk = null;
			    						if(pkeys != null && pkeys.length > 0)  pk = pkeys[0];
			    						if(pk==null) {
			    							if(bl.getLabel().equals("serial_id")) {
			    								pkCol = new Integer(j);
			    								pkFieldName = masterTable.getSerialId() + " = ";
			    							}
			    						} else {
			    							if(bl.getField() != null && bl.getField().getName().equals(pk)) {
			    								pkCol = new Integer(j);
			    								pkFieldName = bl.getLabel() + " = ";
			    							}
			    						}
			    					}
			    					
//				    					for(int i=0;i<xv.size();i++) {
//				    					BiColumn bl = (BiColumn) xv.get(i);
//				    					if(bl.getEngName().equals(colHdr) ||
//				    							colHdr.equals( ZkBiTranslateHelper.getText(sessionHelper, bl.getCellFullName(), "LABEL", sessionHelper.getLabel(bl)))
//				    							) {
//				    						impColHash.put(new Integer(j),bl);
//				    						if(masterTable.getPrimaryKey() == null ||
//				    								masterTable.getPrimaryKey().trim().equals("") ) {
//				    							if(bl.getLabel().equals("serial_id")) {
//				    								pkCol = new Integer(j);
//				    								pkFieldName = masterTable.getSerialId() + " = ";
//				    							}
//				    						} else {
//				    							if(bl.getField() != null && bl.getField().getName().equals(masterTable.getPrimaryKey())) {
//				    								pkCol = new Integer(j);
//				    								pkFieldName = bl.getLabel() + " = ";
//				    							}
//				    						}
//				    					}
//    		    						}
			    					
			    					
			    				}
		    					if(!sessionHelper.getDataImportAlwaysAdd() && pkCol == null) {
			    					Messagebox.show(sessionHelper.getLabel("Primary Key Not In Import File, Cannot Import"));
		    					} else {
		    						List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		    						for(int j=0;j<impColList.size();j++) {
		    							list.add(MapUtil.of("excelColumnHeader", impColList.get(j),
		    												"result", impColHash.containsKey(j) ? "Match" : skipColHash.containsKey(j) ? "Skip" : "Not Match",
		    												"biColumn", impColHash.containsKey(j) ? impColHash.get(j) : skipColHash.containsKey(j) ? skipColHash.get(j) : null
		    											));
		    						}
		    						final ZkBiMsgboxButton btnContinue = new ZkBiMsgboxButton(sessionHelper.getBtLabel("Continue"));
		    						final Checkbox cbAutoConfirm = new Checkbox(sessionHelper.getLabel("Auto confirm after import"));
		    						cbAutoConfirm.setChecked(StringUtils.equals(BiConfig.getString(sessionHelper, "autoConfirmAfterImport"), "Y"));
									new ZkBiMsgbox(sessionHelper)
										.setContent(buildConfirmImportColumnsComponent(list))
										.setButtons(new ZkBiMsgboxButton[] {btnContinue,new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))})
										.setEventListener(new ZkBiEventListener<Event>() {
											@Override
											public void onZkBiEvent(Event event) throws Exception {
												ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
												if (btn.getIdx() == 0) {
													try {
														String pkeys[] = masterTable.getPrimaryKeys();
														String pk = null;
														if(pkeys != null && pkeys.length > 0)  pk = pkeys[0];
														if(pkCol == null && pk != null) {
							    							for(BiColumn bc : result.getView().getColumns()) {
							    								if(bc.getField() != null && bc.getField().getName().equals(pk)) {
							    									if(!StringUtils.isBlank(bc.getFormula(result.getParent() == null))) {
							    										pkColName = bc.getLabel();
							    										break;
							    									}
							    								}
							    							}
							    						}
							    						
									    				ReturnMsg rtnMsg = null;
									    					
									    				int nAdded = 0;
									    				int nUpdated = 0;
									    				int nSkipped = 0;
									    				if(importAsSingle) result.beginWork();
									    				int n;
									    				Object ko = null;
					//				    				boolean translateToBig5 = !result.getView().getSchema().autoTranslate;
									    				for (int i=1;i<exlpoi.getRowCount();) {
									    					n = 1;
									    					currRow = i;
									    					boolean isUpdate = false;
									    					for(BiView bv : impDetailHash.keySet()) {
									    						impDetailHash.put(bv, null);
									    					}
									    					//if(!sessionHelper.getDataImportAlwaysAdd()) { ... }
									    					if(pkCol != null ) {  //andrew240220 when pkcol defined, should ignore alwaysadd flag
										    					ko = getPoiObjectByBiCol(exlpoi,impColHash.get(pkCol),i, pkCol, impColHash.get(pkCol).isAutoTranslate());
										    					if(ko != null && (!(ko instanceof String) || !((String) ko).equals(""))) {
										    						result.clearCondition();
										    						String s = pkFieldName;
										    						if(ko instanceof String) {
						//				    							s += "'" + ko.toString() + "'";
										    							s += "'" + ((String) ko).replace("'", "\\'") + "'";
										    						} else if( ko instanceof java.util.Date) {
										    							s += "'" + DateUtil.toDateString((java.util.Date) ko,"yyyy/mm/dd") + "'";
										    						} else s += ko.toString();
										    						result.addCondition(new VectorUtil().addElement(masterTable).toVector(), s);
										    						ReturnMsg rtn = result.query(false);
										    						if(rtn != null && !rtn.getStatus()) {
										    							rtnMsg = new ReturnMsg(false,"Error : row " + i+ "  load record error " + (rtn == null ? "": rtn.getMsg()));
										    							break;
										    						}
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
										    									nSkipped++;
										    									i += n;
										    									continue;
										    								}
										    							} else {
										    								UniLog.log("Fetch import record by key failed, do add record");
										    							}
										    						}
										    					}
									    					} else {
									    						if(pkColName != null) {
									    						}
									    					}
									    					if(isUpdate == false) {
									    						result.clearCurrentRec();
									    					}
									    					boolean hasValue = false;
									    					boolean hasDetail = false;
									    					for(;;) {
										    					for(Integer xc : impColHash.keySet()) {
										    						BiColumn bc = impColHash.get(xc);
									    							//UniLog.log1("bc:%s, isUpdate:%b, isNoEntry:%b, isNoUpdate:%b", bc.getLabel(), isUpdate, bc.isNoEntry(getSessionHelper()), bc.isNoUpdate(getSessionHelper()));
										    						if( (isUpdate && !bc.isNoUpdate(getSessionHelper())) ||
										    							(!isUpdate && !bc.isNoEntry(getSessionHelper())) ) {
										    							Object xo = getPoiObjectByBiCol(exlpoi,bc,i,xc.intValue(),bc.isAutoTranslate());
										    							//UniLog.log1("i:%d, xc:%d, bc:%s, xo:%s", i, xc, bc.getLabel(), xo);
									    								if(bc.getView() == result.getView()) {
									    									/* HAHAXXX */
									    									if( isUpdate ||
									    											(xo != null && (!(xo instanceof String) || !((String) xo).equals("")))
									    											) {
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
						//			    									result.getCell(bc.getLabel()).set(xo);
									    									if(isUpdate ||
									    											(xo != null && (!(xo instanceof String) || !((String) xo).equals("")))
									    											) {
									    										hasValue = true;
									    									}
									    								} else {
						
									    									if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
									    										hasDetail = true;
									    									}
									    								}
										    						}				    						
										    						
										    					}
									    						if(!isUpdate && pkColName != null) {
									    							BiColumn kbc = result.getColumnByLabel(pkColName);
									    							if(kbc.isformulaOnSave()) {
									    								Cell c = result.getCell(pkColName);
									    								int ignoreCase = result.parserIgnoreCase();
									    								com.uniinformation.utils.exprpar.Parser parser 
									    									= new com.uniinformation.utils.exprpar.Parser(ignoreCase,kbc.getFormula(result.getParent() == null),result.getCurrentCollection(),result.getCurrentCollection());
									    								ko = parser.evaluate();
									    							} else {
									    								ko = result.getCell(pkColName).getObject();
									    							}
									    							if(!(ko instanceof IgnoreValue)) {
									    								if(ko != null) {
									    									TableRec tr = 
									    											result.getSelectUtil().getQueryResult("select serial_id from "+masterTable.getDbtName(), 
									    													new Wherecl().andUniop(kbc.getField().getName(), "=", ko)	);
									    									if(tr.getRecordCount() > 0) {
									    										tr.setRecPointer(0);
									    										String s = kbc.getField().getName() + " = ";
									    										result.clearCondition();
									    										if(ko instanceof String) {
									    											s += "'" + ((String) ko).replace("'", "\\'") + "'";
									    										} else if( ko instanceof java.util.Date) {
									    											s += "'" + DateUtil.toDateString((java.util.Date) ko,"yyyy/mm/dd") + "'";
									    										} else s += ko.toString();
									    										result.addCondition(new VectorUtil().addElement(masterTable).toVector(), s);
									    										ReturnMsg rtn = result.query(false);
									    										if(rtn == null || rtn.getStatus()) {
									    											result.loadOneRecV(0);
									    											result.fetchOneRecV(0);
									    											isUpdate = true;
									    											for(BiView bv : impDetailHash.keySet()) {
									    												impDetailHash.put(bv, null);
									    											}
									    											continue;
									    										}
									    									}
									    								}
									    							}
									    						}
									    						break;
									    					}
									    					if(hasDetail) {
									    						boolean cm = result.getView().compareMasterWhenImport();
									    						for (int k=i+1;k<exlpoi.getRowCount();k++) {
									    							boolean hasMaster = false;
									    							for(Integer xc : impColHash.keySet()) {
									    								BiColumn bc = impColHash.get(xc);
									    								Object xo = getPoiObjectByBiCol(exlpoi,bc,k,xc.intValue(),bc.isAutoTranslate());
									    								if(cm) {
										    								if(bc.getView() == result.getView()) {
										    									if(
									    											(isUpdate && !bc.isNoUpdate(getSessionHelper())) ||
									    											(!isUpdate && !bc.isNoEntry(getSessionHelper())) ) {
										    										if(!compareImportcell(result.getCell(bc.getLabel()),xo)) {
										    											hasMaster = true;
										    											break;
										    										}
										    									}
										    								}
									    								} else {
										    								if(xo != null && (!(xo instanceof String) || !((String) xo).equals(""))) {
										    									if(
										    											(isUpdate && !bc.isNoUpdate(getSessionHelper())) ||
										    											(!isUpdate && !bc.isNoEntry(getSessionHelper())) ) {
										    										if(bc.getView() == result.getView()) {
										    											hasMaster = true;
										    											break;
										    										} 
					
										    									}
										    								}
									    								}
									    							}
									    							if(hasMaster) break;
									    							for(Integer xc : impColHash.keySet()) {
									    								BiColumn bc = impColHash.get(xc);
									    								Object xo = getPoiObjectByBiCol(exlpoi,bc,k,xc.intValue(),bc.isAutoTranslate());
								    									if(processImportDetailColumn(impDetailHash,bc,result,xo)) {
					//			    										hasDetail = true;
								    									}
									    								
									    							}
					//				    							for(BiView bv : impDetailHash.keySet()) {
					//				    								impDetailHash.put(bv, null);
					//				    							}
					//				    							if(hasMaster) break; else n++;
									    							n++;
									    						}
									    					}
									    					if(hasValue) {
									    						if(isUpdate) {
									    							UniLog.log("Excel Import Update Record " + i);
									    							rtnMsg = result.updateCurrent();
									    							if(rtnMsg != null && !rtnMsg.getStatus()) {
									    								break;
									    							}
									    							nUpdated++;
									    						} else {
									    							UniLog.log("Excel Import Insert Record " + i);
					//				    							result.doBeforeAdd();
									    							rtnMsg = result.addCurrent();
									    							if(rtnMsg != null && !rtnMsg.getStatus()) {
									    								break;
									    							}
									    							nAdded++;
									    						}
									    					} else break;
									    					
									    					i+=n;
									    				}

								    
									    				UniLog.log1("total run time:%d", System.currentTimeMillis() - startRunTime);
										    			String strMsg;
										    			if(rtnMsg != null && !rtnMsg.getStatus()) {
										    				String rowStr;
										    				if(rtnMsg.getData() != null && rtnMsg.getData() instanceof Integer) {
										    					int detIdx = (Integer) rtnMsg.getData();
										    					rowStr = " row " + (currRow+detIdx);
										    				} else {
										    					rowStr = " row " + currRow;
										    				}
										    				rowStr +=  " key = ("+(ko == null ? null : ko.toString())+")";
										    				strMsg = "Update Incomplete : at " + rowStr + " " + rtnMsg.getMsg() + ", " + nAdded + " Added " + nUpdated + " Updated " + nSkipped + " Skipped" ;
										    				Messagebox.show(
									    						strMsg,
									    						sessionHelper.getLabel("Import Error"), 
									    						Messagebox.OK , 
									    						Messagebox.ERROR, 
									    						new ZkBiEventListener() {
									    							public void onZkBiEvent(Event evt) throws InterruptedException {
									    								UniLog.log("rollback work due to import error");
									    								if(importAsSingle) result.rollbackWork();
									    								refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
									    							}
									    						});	
										    			} else {
										    				//strMsg = "Update Completed : " + nAdded + " Added " + nUpdated + " Updated " + nSkipped + " Skipped" ;
										    				strMsg = String.format(sessionHelper.getLabel("Update Completed: %d Added, %d Updated, %d Skipped"), nAdded, nUpdated, nSkipped);
										    				if(rtnMsg != null) strMsg += rtnMsg.getMsg();
										    				if (cbAutoConfirm.isChecked()) {
					    										UniLog.log("Confirm OK commit work");
										    					try {
										    						if(!result.commitWork()) {
										    							Messagebox.show(sessionHelper.getLabel("Commit Work Failed, please upload again"));
										    						}
									    						} catch (Exception ex) {
										    						UniLog.log(ex);
										    						Messagebox.show(ex.toString());
										    					}
										    					refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
										    					Messagebox.show(strMsg, sessionHelper.getLabel("Information"), Messagebox.OK, Messagebox.INFORMATION);
										    				} else {
											    				Messagebox.show(
										    						strMsg,
										    						sessionHelper.getLabel("Confirm Save Changes?"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {
										    							Timer klTimer = null;
										    							{
										    								if (importAsSingle) {
										    									klTimer = new Timer();
										    									klTimer.setPage(masterWin.getPage());
										    									klTimer.setDelay(30000);
										    									klTimer.setRepeats(true);
										    									klTimer.addEventListener(Events.ON_TIMER, (Event) -> {
										    										UniLog.log1("doKeepAlive fbegin %b", result.inBeginWork());
										    										result.getSelectUtil().setAliveUntil(30000);
										    									});
										    									klTimer.setRunning(true);
										    								}
										    							}
										    							public void onZkBiEvent(Event evt) throws InterruptedException {
								    										if (klTimer != null) {
								    											klTimer.stop();
								    											klTimer.detach();
								    										}
										    								if(importAsSingle) {
										    									if (evt.getName().equals("onOK")) {
										    										UniLog.log("Confirm OK commit work");
										    										try {
										    											if(!result.commitWork()) {
										    												Messagebox.show(sessionHelper.getLabel("Commit Work Failed, please upload again"));
										    											}
										    										} catch (Exception ex) {
										    											UniLog.log(ex);
										    											Messagebox.show(ex.toString());
										    										}
										    										refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
										    									} else {
										    										UniLog.log("Confirm Canceled rollback work");
										    										result.rollbackWork();
										    										refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
										    									}
										    								}
										    							}
										    						});	
										    				}
										    			}
													} catch (Exception ex) {
														UniLog.log(ex);
														//Messagebox.show("Failed to load at row " + currRow + " : " + ex.toString());
														Messagebox.show(String.format(sessionHelper.getLabel("Failed to load at row %d: %s"), currRow, ex.toString()));
													}
													
												}
											}
										})
										.build().doModal();
										Component pc = btnContinue.getParent();
										pc.insertBefore(cbAutoConfirm, btnContinue);
										pc.insertBefore(new Space() {{ setWidth("10px"); }}, btnContinue);
		    						

    			
    			
    			
    			
    			
			    				}
		                	} 
		                	catch (Exception ex) {
		                		UniLog.log(ex);
		                		//Messagebox.show("Failed to load at row " + currRow + " : " + ex.toString());
		                		Messagebox.show(String.format(sessionHelper.getLabel("Failed to load at row %d: %s"), currRow, ex.toString()));
		                	}
		                		
		                	}
		                } 
		                else {
		                	Messagebox.show(sessionHelper.getLabel("File Not Selected"));
		                }
		                
		        	}
    		}
    		public void onZkBiEvent(Event event) throws Exception {
    			UniLog.log("Import button pressed");
    			Fileupload.get(new UploadEventListener());
    		}
    }
    protected void setupImportButton(final BiResult result)
    {
    	if(result.getView().isNoImport(getSessionHelper()) || !result.allowUpdate()) return;
    	btnImport = new ZkBiButton();
    	btnImport.setLabel(sessionHelper.getBtLabel("Import"));
		btnImport.setImage("images/icons/zkweb/020-import-25x25.png");
    	btnImport.setId("btImport");
    	btnImport.setTooltiptext(sessionHelper.getTtLabel("Import data (Gen 1)"));
    	addHotkey('I', btnImport);
    	//abHelper.addButton(btnImport, "fa-cloud-upload");
    	btnImport.addEventListener("onClick", getImportButtonEventListener(result));
    	btnImport.setVisible(!isMobile()); //disable export for mobile
    }
    protected void setupImportG2Button(final BiResult result) {
    	if(result.getView().isNoImport(getSessionHelper()) || !result.allowUpdate()) return;
    	btnImportG2 = new ZkBiButton();
    	btnImportG2.setLabel(sessionHelper.getBtLabel("Import G2"));
		btnImportG2.setImage("images/icons/zkweb/020-import-25x25.png");
    	btnImportG2.setId("btImportG2");
    	btnImportG2.setTooltiptext(sessionHelper.getTtLabel("Import data (Gen 2)"));
    	//addHotkey('I', btnImportG2);
    	//abHelper.addButton(btnImportG2, "fa-cloud-upload");
    	btnImportG2.addEventListener("onClick", new ZkBiEventListener <Event> () {
    		public void onZkBiEvent(Event event) throws Exception {
    			UniLog.log("Import button pressed");
    			new ZkBiImportExcelData(sessionHelper, result, btnImportG2, new ZkBiImportExcelData.Callback() {
					@Override
					public void afterCloseDialog() {
						refresh(result, masterWin, (MultiSortMap)mMultiSortMap.clone(),false);
					}
				});
    		}
    	});
    	btnImportG2.setVisible(!isMobile()); //disable export for mobile
    }
    protected void setupExportButton(final BiResult result) {
    	if(result.getView().isNoExport(getSessionHelper())) return;
    	btnExport = new ZkBiButton();
    	btnExport.setLabel(sessionHelper.getBtLabel("Export"));
    	btnExport.setAttribute("tlkey", "bt_master_export");
    	btnExport.setImage("images/icons/zkweb/019-export-25x25.png");
    	btnExport.setId("btExport");
    	btnExport.setTooltiptext(sessionHelper.getTtLabel("Export data"));
    	addHotkey('E', btnExport);
    	btnExport.addEventListener("onClick", new ZkBiEventListener() {
    		public void onZkBiEvent(Event event) throws Exception {
    			UniLog.log("Export button pressed");
    			showExportWindow();
    		}	
    	});
    	//actionBar.appendChild(btnExport);
    	//abHelper.addButton(btnExport, "fa-cloud-download");
    	btnExport.setVisible(!isMobile()); //disable export for mobile
    	//ZkUtil.setupBatchModeButton(btnExport, batchModeToggleButton);
    }
    protected JxZkBiBase buildDetailWindow(final BiResult result){
    	try{
			final Idspace dWin;
			dWin = new Idspace();
			
			UniLog.logm(this,"set dWin width as %s", masterWin.getWidth());
			if (isMobile()){
				dWin.setWidth("100%");
			}
			else{
				dWin.setWidth("100%");
				//dWin.setWidth("7680px"); //1920 x 4 
			}
			dWin.setParent(masterWin);
			return(JxZkBiBase.buildDetailWindow(result,dWin,isMobile(),hasAUDColumn,this,this.getURLParams()));
    	}
    	catch(Exception ex){
    		UniLog.log(ex);
    		return(null);
    	}
    }
    
    EventListener barcodeEventListener=null;
    @SuppressWarnings("deprecation")
	public void buildQueryWindow(final BiResult result,final Component comp){
    	XulElement win = (XulElement) comp;
    	UniLog.log("buildQueryWindow");
    	//header
    	
    	if (isMobile()){
    		headerBox = new Hbox();
    	}
    	else{
    		headerBox = new Hbox();
   			headerBox.setWidths("50%,50%");
    	}
    	headerBox.setAlign("left");
   		headerBox.setWidth("100%");
    	headerLabel = new Toolbarbutton();
    	headerLabel.setClass("zkbi-header-label");
    	headerLabel.setDisabled(StringUtils.equalsIgnoreCase(this.getURLParam("closetab"),"Y"));
    	headerLabel.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event arg0) throws Exception {
				Events.sendEvent("onCustomEsc", masterWin, null);
			}});
 		headerLabel.setLabel("  " + sessionHelper.getLabel(title));
 		String sideMenuImg = sessionHelper.getSideMenuImg(getURLParam("page_id"));
 		if (StringUtils.startsWithAny(sideMenuImg, "fa-")) {
 			headerLabelDiv.appendChild(new Html(String.format("<i class='fa %s fa1 zkbi-header-icon'></i>",sideMenuImg)));
 		}
 		else if (StringUtils.startsWithAny(sideMenuImg, "flaticon-")) {
 			headerLabelDiv.appendChild(new Html(String.format("<i class='%s zkbi-header-icon'></i>",sideMenuImg)));
 		}
 		else if (StringUtils.startsWithAny(sideMenuImg, "images/")) {
 			headerLabelDiv.appendChild(new Html(String.format("<img src='%s' style='width:auto; height:20px;'/>",sideMenuImg)));
 		}
    	
 		if (!isMobile()) {
 			headerLabelDiv.setWidth("100%");
 		}
    	headerLabelDiv.appendChild(headerLabel);
    	headerLabelDiv.setStyle("white-space: nowrap;");
   		headerBox.appendChild(headerLabelDiv);
   		
    	btnHelp = new Toolbarbutton();
		//btnHelp.setImage("images/icons/zkweb/058-idea-25x25.png");
		//btnHelp.setImage("images/icons/zkweb/089-support-25x25.png");
		btnHelp.setImage("images/icons/zkweb/033-doubt-25x25.png");
		btnHelp.setTooltiptext(sessionHelper.getTtLabel("Help"));
		//btnHelp.setStyle("font-size: 25px; opacity:0.8;");
		btnHelp.setStyle("font-size: 25px;");
   		addHotkey('H',btnHelp);
		
   		headerLabelDiv.appendChild(btnHelp);
		headerLabelDiv.setSclass("zkbi-form-header zkbi-main-headerlabeldiv");
   		
   		queryBar= new Hbox();
 		if (isMobile()) {
 			queryBar.setHflex("1");
 			queryBar.setAlign("center");
 			queryBar.setPack("end");
 		}
 		/*else
 			queryBar.setWidth("100%");*/
		ZkUtil.appendSclass(queryBar, "zkbi-main-querybar");

 		if (barcodeScanner != null) {
 			if(shortcutBar == null) {
 				shortcutBar= new Hbox();
 			/*
 			shortcutBar.setWidth("100%");
 			*/
 			//shortcutBar.setHflex("1");
 			shortcutBar.setHflex("min");
 			shortcutBar.setSclass("zkbi-main-shortcutbar");
   		 	shortcutBar.setAlign("center");
   		 	shortcutBar.setPack("end");
   		 	/*
   		 	Toolbarbutton b1 = new Toolbarbutton("HOME");
   		 	b1.setTabindex(-1);
   		 	shortcutBar.appendChild(b1);
   		 	*/
 			}

   		 	{
   		 		barcodeScannerImg = new Image();
   		 		barcodeScannerImg.setSrc("/images/001-barcode.png");
   		 		barcodeScannerImg.setHeight("30px");
   		 		shortcutBar.appendChild(barcodeScannerImg);
   		 		barcodeEventListener = 
   		 			new ZkBiEventListener() {
   		 				public void onZkBiEvent(Event event) throws Exception {
   		 					if (event.getName() != null && event.getName().equals("onBarcodeNotify")){
   		 						String barcode = (String) event.getData();
   		 						UniLog.log("got scanned barcode ["+barcode+ "]");
   		 						if(inDetailForm) {
   		 							if(detailForm != null) {
   		 								detailForm.onBarcode(barcode);
   		 							}
   		 						} else {
   		 							if(!barcode.equals("")) {
   		 								onBarcode(result,barcode);
   		 								/*
   		 								tbSearchBox.setText(barcode);
   		 								zkBiSearch.doSearch(false, false);
   		 								*/
   		 							}
   		 						}
   		 					}
   		 					if (event.getName() != null && event.getName().equals("onBarcodeAttached")){
   		 						String devid = (String) event.getData();
   		 						String iconFile = null;
   		 						if(devid.startsWith(DeviceControl.MOBILE_SCANNER_PREFIX)) {
   		 							iconFile = "/images/002-barcode-"+"MB01"+".png";
   		 						} else {
   		 							iconFile = "/images/002-barcode-"+devid+".png";
   		 						}
   		 						barcodeScannerImg.setSrc(iconFile);
//   		 						img.setSrc("/images/002-barcode-1.png");
   		 						barcodeScannerImg.invalidate();
            		
   		 					}
   		 					if (event.getName() != null && event.getName().equals("onBarcodeDetached")){
   		 						barcodeScannerImg.setSrc("/images/001-barcode.png");
   		 						barcodeScannerImg.invalidate();
   		 					}
   		 					if (event.getName() != null && event.getName().equals("onListenerDetached")){
   		 						EventListener evd =(EventListener) event.getData();
   		 						if(barcodeEventListener != evd) return;
   		 						EventQueue que = EventQueues.lookup(eventQueId, EventQueues.APPLICATION, false);
   		 						if(que != null) {
   		 							que.unsubscribe(barcodeEventListener);
   		 						}
   		 						barcodeScannerImg.setSrc("/images/001-barcode.png");
   		 						barcodeScannerImg.invalidate();
   		 					}

   		 					if (event.getName() != null && event.getName().equals("onAddPhoto")){
   		 						UniLog.log("onAddPhoto: " + event.getName() + " data.length():" + event.getData().toString().length());
   		 						Object obj = new JSONTokener((String)event.getData()).nextValue();
   		 						if (obj != null && obj instanceof JSONObject) {
   		 							JSONObject jsonObj = (JSONObject) obj;
					 				String action = (String) jsonObj.get("action");
					 				String value = (String) jsonObj.get("value");
					 				String version = (String) jsonObj.get("version");
					 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
					 				Base64Util.convertStringToOutputStream(value, bos);
					 				byte[] photobytes = bos.toByteArray();
					 				bos.close();
					 				ByteArrayInputStream bis = new ByteArrayInputStream(photobytes);
					 				AImage img = new AImage("captured",photobytes);
					 				UniLog.log("addPhoto image decoded " + img.toString());
					 				onAddPhoto(result,img);
					 				/*
					 				File tmpFile = File.createTempFile("MStock", ".jpg", new File("/tmp")); //copy img to tmpfile for dev
					 				Base64Util.convertStringToOutputStream(value, new FileOutputStream(tmpFile));
					 				saveImageFile(new AImage(tmpFile));
					 				UniLog.log1("log to file %s", tmpFile.getAbsolutePath());
					 				*/
   		 						}
   		 					}
   		 				}
   		 			};
   		 		
   		 			
   		 		//andrew201124 main component register onBarcodeNotify too. called by android/ios device zkdevice e.g. zkDevice.processUrlEncodedJson
   		 		this.addEventListener("onBarcodeNotify", barcodeEventListener);
   		 		
   		 		
   		 		if("ONDEMAND".equals(barcodeScanner)) {
   		 			barcodeDevId = DeviceControl.getDevIdBySession(""+sessionHelper.hashCode());
   		 		}
   		 		if(barcodeDevId != null) {
   		 			eventQueId = sessionHelper.setDeviceEventQueueListener(barcodeDevId,barcodeEventListener,false); 
   		 		}
//   		 		DeviceControl.attachListiner(barcodeScannerId,eventQueId,false);
   		 		barcodeScannerImg.addEventListener("onClick",
   		 			new ZkBiEventListener() {
   		 				public void onZkBiEvent(Event event) throws Exception {
   		 					if("ONDEMAND".equals(barcodeScanner)) {
   		 						/*
   		 						if(
   		 								sessionHelper.isMobile() &&
   		 								sessionHelper.checkDeviceFeature(SessionHelper.DEVICE_FEATURE.SCANNER)
   		 								) {
   		 							
   		 						}
   		 						 */
   		 						if( sessionHelper.isMobile())  {
   		 							
   		 							SessionHelper.DEVICE_FEATURE_STATE ds = sessionHelper.checkDeviceFeature(SessionHelper.DEVICE_FEATURE.SCANNER);
   		 							if(ds.equals(SessionHelper.DEVICE_FEATURE_STATE.TRUE)) {
   		 								ZkUtil.js("zkDevice.launchScanner()"); 
   		 								return;
   		 							}
   		 							
   		 						}
   		 						
   		 						UniLog.log1("scanner:%s",sessionHelper.checkDeviceFeature(SessionHelper.DEVICE_FEATURE.SCANNER));
   		 						if(barcodeDevId != null) {
   		 							sessionHelper.setDeviceEventQueueListener(barcodeDevId,null,true); 
   		 						}
   		 						final Timer timer = new Timer();
   		 						final ZkForm zkf1 = new ZkForm(null,"zkf/showBarcode.zul");
   		 						Image bImg = (Image) zkf1.getComponent("barcodeImg");
   		 						if(bImg != null) {
   		 							byte[] imgBytes = QRCodeUtil.createQRCode(
   		 									"BindBcnToSession:"+sessionHelper.hashCode()
   		 									,200,200,"PNG");
   		 							bImg.setSrc(Base64Util.convertToImgString(imgBytes, "PNG"));
   		 						}
   		 						zkf1.doModal(null,
   		 							new ZkBiEventListener() {
   		    		 					public void onZkBiEvent(Event arg0) throws Exception {
   		    		 						if(arg0.getTarget().getId().equals("btCancel")) {
   		    		 							zkf1.exitModal();
   		    		 							timer.stop();
   		    		 							timer.detach();
   		    		 						}
   		    		 					}	
   		    		 				}	
   		 								
   		 								);
   		 						timer.setPage(masterWin.getPage());
   		 						timer.setDelay(1000);
   		 						timer.setRepeats(true);
   		 						timer.addEventListener(Events.ON_TIMER, new ZkBiEventListener<Event>(){
   		 							@Override
   		 							public void onZkBiEvent(Event event) throws Exception {
   		 								String devId = DeviceControl.getDevIdBySession(""+sessionHelper.hashCode());
   		 								if(devId != null) {
   		    		 							zkf1.exitModal();
   		    		 							timer.stop();
   		    		 							timer.detach();
   		    		 							barcodeDevId = devId;
   		    		 							eventQueId = sessionHelper.setDeviceEventQueueListener(barcodeDevId,barcodeEventListener,true); 
   		 								}
   		 							}
   		 						});
   		 					} else {
   		 					UniLog.log("activate barcode scanner");
   		 					if(barcodeDevId != null) {
   		 						eventQueId = sessionHelper.setDeviceEventQueueListener(barcodeDevId,barcodeEventListener,true); 
   		 					//ZkUtil.js("zkDevice.launchScanner()"); //andrew201124 for test android/ios
//   		 					DeviceControl.attachListiner(barcodeScannerId,eventQueId,true);
   		 					}
   		 					}
   		 				}	
   		 			}
   		 		);
   		 		/*
   		 		eventQueId = DeviceControl.getUniqueEventQueid();
   		 		EventQueue que = EventQueues.lookup(eventQueId, EventQueues.APPLICATION, true);
   		 		que.subscribe(new ZkBiEventListener() {
   		 			public void onZkBiEvent(Event event) throws Exception {
   		 				if (event.getName() != null && event.getName().equals("onBarcodeNotify")){
   		 					String barcode = (String) event.getData();
   		 					UniLog.log("got scanned barcode ["+barcode+ "]");
   		 				}
   		 				if (event.getName() != null && event.getName().equals("onBarcodeAttached")){
   		 					img.setSrc("/images/002-barcode-1.png");
   		 					img.invalidate();
            		
   		 				}
   		 				if (event.getName() != null && event.getName().equals("onBarcodeDetached")){
            				img.setSrc("/images/001-barcode.png");
            				img.invalidate();
   		 				}
   		 			}
   		 		});
   		 		*/
   		 	}
 		}
 		if (cameraDevice != null) {
 			if(shortcutBar == null) {
 				shortcutBar= new Hbox();
 			/*
 			shortcutBar.setWidth("100%");
 			*/
 			//shortcutBar.setHflex("1");
 			shortcutBar.setHflex("min");
 			shortcutBar.setSclass("zkbi-main-shortcutbar");
   		 	shortcutBar.setAlign("center");
   		 	shortcutBar.setPack("end");
   		 	/*
   		 	Toolbarbutton b1 = new Toolbarbutton("HOME");
   		 	b1.setTabindex(-1);
   		 	shortcutBar.appendChild(b1);
   		 	*/
   		 	}
   		 		if(barcodeEventListener != null) this.addEventListener("onAddPhoto", barcodeEventListener);
   		 		cameraImg = new Image();
   		 		cameraImg.setSrc("/images/camera.png");
   		 		cameraImg.setHeight("30px");
   		 		shortcutBar.appendChild(cameraImg);
   		 		cameraImg.addEventListener("onClick",
   		 			new ZkBiEventListener() {
   		 				public void onZkBiEvent(Event event) throws Exception {
   		 					if("ONDEMAND".equals(barcodeScanner)) {
   		 						if( sessionHelper.isMobile())  {
   		 							
   		 							SessionHelper.DEVICE_FEATURE_STATE ds = sessionHelper.checkDeviceFeature(SessionHelper.DEVICE_FEATURE.TAKE_PHOTO);
   		 							if(ds.equals(SessionHelper.DEVICE_FEATURE_STATE.TRUE)) {
   		 								ZkUtil.js("zkDevice.launchPhotoCapture()"); 
   		 								return;
   		 							}
   		 							
   		 						}
   		 					}
   		 				}	
   		 			}
   		 		);
 		}
 		if (recorderDevice != null) {
 			if(shortcutBar == null) {
 				shortcutBar= new Hbox();
 			/*
 			shortcutBar.setWidth("100%");
 			*/
 			//shortcutBar.setHflex("1");
 			shortcutBar.setHflex("min");
 			shortcutBar.setSclass("zkbi-main-shortcutbar");
   		 	shortcutBar.setAlign("center");
   		 	shortcutBar.setPack("end");
   		 	/*
   		 	Toolbarbutton b1 = new Toolbarbutton("HOME");
   		 	b1.setTabindex(-1);
   		 	shortcutBar.appendChild(b1);
   		 	*/
   		 	}
   		 		recorderImg = new Image();
   		 		recorderImg.setSrc("/images/recorder.png");
   		 		recorderImg.setHeight("30px");
   		 		shortcutBar.appendChild(recorderImg);
 		}
 		if (gpsDevice != null) {
 			if(shortcutBar == null) {
 				shortcutBar= new Hbox();
 			/*
 			shortcutBar.setWidth("100%");
 			*/
 			//shortcutBar.setHflex("1");
 			shortcutBar.setHflex("min");
 			shortcutBar.setSclass("zkbi-main-shortcutbar");
   		 	shortcutBar.setAlign("center");
   		 	shortcutBar.setPack("end");
   		 	/*
   		 	Toolbarbutton b1 = new Toolbarbutton("HOME");
   		 	b1.setTabindex(-1);
   		 	shortcutBar.appendChild(b1);
   		 	*/
   		 	}
   		 		gpsImg = new Image();
   		 		gpsImg.setSrc("/images/gps.png");
   		 		gpsImg.setHeight("30px");
   		 		shortcutBar.appendChild(gpsImg);
 		}
 		if (printerDevice != null) {
 			if(shortcutBar == null) {
 				shortcutBar= new Hbox();
 			/*
 			shortcutBar.setWidth("100%");
 			*/
 			//shortcutBar.setHflex("1");
 			shortcutBar.setHflex("min");
 			shortcutBar.setSclass("zkbi-main-shortcutbar");
   		 	shortcutBar.setAlign("center");
   		 	shortcutBar.setPack("end");
   		 	/*
   		 	Toolbarbutton b1 = new Toolbarbutton("HOME");
   		 	b1.setTabindex(-1);
   		 	shortcutBar.appendChild(b1);
   		 	*/
   		 	}
   		 		printerImg = new Image();
   		 		printerImg.setSrc("/images/printer.png");
   		 		printerImg.setHeight("30px");
   		 		shortcutBar.appendChild(printerImg);
   		 		printerImg.addEventListener("onClick",
   		 			new ZkBiEventListener() {
   		 				public void onZkBiEvent(Event event) throws Exception {
   		 					if("ONDEMAND".equals(barcodeScanner)) {
   		 						if( sessionHelper.isMobile())  {
   		 							
   		 						}
   		 					}
   		 				}	
   		 			}
   		 		);
 		}
   		
   		new ZkBiHelpDialog(sessionHelper, btnHelp, masterWin, title, helpid, null);
   		
   		if (sessionHelper.getAllowTour() && !isMobile()){
	   		btTour = new Toolbarbutton();
	   		btTour.setId("btTour");
			btTour.setImage("images/icons/zkweb/007-paper-plane-25x25.png");
			btTour.setStyle("font-size: 25px; opacity:1;");
	   		btTour.setTooltiptext(sessionHelper.getTtLabel("Tour"));
	   		btTour.setSclass("narrowtoolbarbutton");
	   		addHotkey('T',btTour);
	   		btTour.addEventListener("onClick", new ZkBiEventListener() {
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					UniLog.logm(this,"tour clicked");
					Clients.evalJavaScript(String.format("startBasicTour();"));
				}
	   		});
   		}
   		
    		
   		btAdvSearch = new Toolbarbutton();
   		btAdvSearch.setId("btAdvancedSearch");
		btAdvSearch.setImage("images/icons/zkweb/062-search-25x25.png");
		btAdvSearch.setStyle("font-size: 25px; opacity:0.8;");
   		btAdvSearch.setTooltiptext(sessionHelper.getTtLabel("Advanced Search"));
   		btAdvSearch.setSclass("narrowtoolbarbutton");
   		addHotkey('S',btAdvSearch);
   		
   		btAdvSearch.addEventListener("onClick", new ZkBiEventListener() {
    			public void onZkBiEvent(Event event) throws Exception {
    				Grid queryGrid = (Grid) comp.query("#query_grid");
    				//Listbox listbox = (Listbox) comp.query("#browser_listbox");
    				
    				if (sessionHelper.getAllowNewAdvSearch()){
    					Listheader listheader = (Listheader)listbox.query("#browser_listheader_0");
    					if (advSearchFlag) {
    						listfoot.setVisible(false);
    						setListheaderWidth(result, false);
    						btAdvSearch.setSclass("narrowtoolbarbutton");
    						advSearchFlag = false;
    					} else {
    						listfoot.setVisible(true);
    						setListheaderWidth(result, true);
    						btAdvSearch.setSclass("narrowtoolbarbutton zkbi-active-toolbarbutton");
    						advSearchFlag = true;
    					}
    					listbox.invalidate();
    				}
    				else{
    					queryGrid.setVisible(!queryGrid.isVisible());
    				}
    			}
   		});
   		//btAdvSearch.setVisible(sessionHelper.isAdminUser() && !isMobile()); //disable advanced search for mobile temporary 
   		btAdvSearch.setVisible(sessionHelper.getAllowNewAdvSearch() && !isMobile()); //disable advanced search for mobile temporary 

   		btAdvSearchG2 = new Toolbarbutton();
   		btAdvSearchG2.setId("btAdvancedSearchG2");
		btAdvSearchG2.setImage("images/icons/zkweb/062-search-25x25.png");
		btAdvSearchG2.setStyle("font-size: 25px; opacity:0.8;");
   		btAdvSearchG2.setTooltiptext(sessionHelper.getTtLabel("Advanced Search"));
   		btAdvSearchG2.setSclass("narrowtoolbarbutton");
   		addHotkey('G',btAdvSearchG2);

   		divAdvSearchG2 = new Div();
   		divAdvSearchG2.setStyle("position:relative;");
   		divAdvSearchG2Indicator = new Div();
   		divAdvSearchG2Indicator.setStyle("position:absolute;right:2px;top:2px;width:10px;height:10px;background-color:red;border-radius:50%;");
   		divAdvSearchG2Indicator.setVisible(false);
   		divAdvSearchG2.appendChild(btAdvSearchG2);
   		divAdvSearchG2.appendChild(divAdvSearchG2Indicator);
   		
   		btAdvSearchG2.addEventListener("onClick", new ZkBiEventListener() {
   			public void onZkBiEvent(Event event) throws Exception {
				btAdvSearchG2.setSclass("narrowtoolbarbutton zkbi-active-toolbarbutton");
				getAdvSearchForG2(result, comp).showDialog();
   			}
   		});
   		divAdvSearchG2.setVisible(
   				sessionHelper.getAllowNewAdvSearchG2() && 
   				 (!isMobile()  || (sessionHelper != null && sessionHelper.getAllowMobileAdvanceSearch()))/* && sessionHelper.isAdminUser() */ ); //disable advanced search for mobile temporary 

   		btReload.setId("btReload");
		//btReload.setImage("images/icons/zkweb/018-loop-25x25.png");
		btReload.setImage("images/icons/zkweb/018-loop-blue-grey-25x25.png");
		btReload.setStyle("font-size: 25px");
   		btReload.setTooltiptext(sessionHelper.getTtLabel("Reload Records From Database"));
   		btReload.setSclass("narrowtoolbarbutton");
   		addHotkey('R', btReload);
   		btReload.addEventListener("onClick",
            	new ZkBiEventListener() {
        		    public void onZkBiEvent(Event event) throws Exception {
        		    	
// original code before 210525,         		    	
//        		    	UniLog.log("Refresh Pressed");
//   	        			//sort index reset to default
//      	     			//refresh(result, comp, defaultSortIdx, defaultSortDesc, false);  
//       		    		//refresh(result,comp,(MultiSortMap)mMultiSortMap.clone(),false);
//       		    		String preset = conditionPresetListbox.getSelectedItem().getValue();
//       		    		inputFieldsList.reset(preset);
//       		    		visibleCols(preset, comp);
//       		    		MultiSortMap sortMap = preset != null ? new MultiSortMap(preset) : null;
//       		    		if((ZkBiComposerBase.this instanceof ZkBiComposerLedgerReport)) {
//       		    			((ZkBiComposerLedgerReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
//       		    		} else if((ZkBiComposerBase.this instanceof ZkBiComposerAnalysisReport)) {
//       		    			((ZkBiComposerAnalysisReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
//       		    		} else {
//       		    			refresh(result,comp,sortMap,false);
//       		    		}
//       		    		divAdvSearchG2Indicator.setVisible(false);
       		    		
       		    		
       		    		
           		    	UniLog.log("Refresh Pressed");
           		    	/*
   	        			//sort index reset to default
      	     			//refresh(result, comp, defaultSortIdx, defaultSortDesc, false);  
       		    		//refresh(result,comp,(MultiSortMap)mMultiSortMap.clone(),false);
//      		    		String preset = conditionPresetListbox.getSelectedItem().getValue();
//       		    		inputFieldsList.reset(preset);
      		    		MultiSortMap sortMap = (MultiSortMap) mMultiSortMap.clone();
       		    		if((ZkBiComposerBase.this instanceof ZkBiComposerLedgerReport)) {
//       		    			visibleCols(preset, comp);
//       		    			MultiSortMap sortMap = preset != null ? new MultiSortMap(preset) : null;
       		    			((ZkBiComposerLedgerReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
       		    		} else if((ZkBiComposerBase.this instanceof ZkBiComposerAnalysisReport)) {
//      		    			visibleCols(preset, comp);
//      		    			MultiSortMap sortMap = preset != null ? new MultiSortMap(preset) : null;
       		    			((ZkBiComposerAnalysisReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
       		    		} else {
//       		    			refresh(result,comp,(MultiSortMap)mMultiSortMap.clone(),false);
       		    			refresh(result,comp,sortMap,false);
       		    		}
       		    		*/
      		    		String preset = conditionPresetListbox.getSelectedItem().getValue();
           		    	inputFieldsList.reset(preset);

      		    		MultiSortMap sortMap = (MultiSortMap) mMultiSortMap.clone();
       		    		onSelectionChanged(result,sortMap,comp);
           		    	
       		    		divAdvSearchG2Indicator.setVisible(false);	
        		    }	
   		});
   		
   		
   		btAdjustWidth = new Toolbarbutton();
   		btAdjustWidth.setId("btAdjustWidth");
		//btAdjustWidth.setImage("images/icons/zkweb/093-text-width-25x25.png");
		btAdjustWidth.setImage("images/icons/zkweb/093-text-width-blue-grey-25x25.png");
		btAdjustWidth.setStyle("font-size: 25px;");
   		btAdjustWidth.setTooltiptext(sessionHelper.getTtLabel("Width by data"));
   		btAdjustWidth.setSclass("narrowtoolbarbutton");
   		//btAdjustWidth.setVisible(!isMobile());
   		btAdjustWidth.setVisible(false);
   		addHotkey('W', btAdjustWidth);
   		btAdjustWidth.addEventListener("onClick",
           	new ZkBiEventListener() {
       		    public void onZkBiEvent(Event event) throws Exception {
       		    	UniLog.log("btAdjustWidth called");
       		    	adjustWidthFlag = !adjustWidthFlag;
       		    	if (adjustWidthFlag){
       		    		setListheaderWidth(result, true);
       		    	}
       		    	else{
       		    		setListheaderWidth(result, false);
       		    	}
       		    	listbox.invalidate();
       		    }	
   		});
   		
   		
   		if(isMobile()) {
   			headerBox.appendChild(queryBar); 
   		}
 		if (barcodeScanner != null) {
 			headerBox.appendChild(shortcutBar);
 		}
    	//query form
    	Grid grid = new Grid();
    	grid.setId("query_grid");
    	grid.setWidth("100%");
    	grid.setVisible(false);
    	if(win.hasFellow("zkbiTabbox")) {
    		Component cc = win.getFellow("zkbiTabbox");
    		win.insertBefore(headerBox,cc);
    		win.insertBefore(grid,cc);
    	} 
    	else if (win.hasFellow("zkbiListTop")){  //temporary fix: headerbox placed under listbox
    		Component cc = win.getFellow("zkbiListTop");
    		win.insertBefore(headerBox,cc);
    		win.insertBefore(grid,cc);
    	}
    	else{
    		win.appendChild(headerBox);
    		win.appendChild(grid);
    	}
    	Columns columns = new Columns();

    	Column labelColumn = new Column();
    	labelColumn.setWidth("25%");
    	columns.appendChild(labelColumn);
    	
    	Column cb0Column = new Column();
    	cb0Column.setWidth("25%");
    	columns.appendChild(cb0Column);

    	Column textColumn = new Column();
    	textColumn.setWidth("50%");
    	columns.appendChild(textColumn);
    	
    	
    	grid.appendChild(columns);    	
    	Rows rows = new Rows();
    	grid.appendChild(rows);
    	
    	//build search header
    	Auxhead searchHead0 = new Auxhead();
    	searchHead0.setParent(grid);
    	Auxheader searchHeader00 = new Auxheader();
    	searchHeader00.setLabel(sessionHelper.getLabel("Advanced Search"));
    	
    	searchHeader00.setColspan(3);
    	searchHeader00.setParent(searchHead0);
    	
    	Auxhead searchHead1 = new Auxhead();
    	searchHead1.setParent(grid);
    	Auxheader searchHeader10 = new Auxheader();
    	searchHeader10.setLabel(sessionHelper.getLabel("Data Field"));
    	searchHeader10.setColspan(1);
    	searchHeader10.setParent(searchHead1);
    	Auxheader searchHeader11 = new Auxheader();
    	searchHeader11.setLabel(sessionHelper.getLabel("Operator"));
    	searchHeader11.setColspan(1);
    	searchHeader11.setParent(searchHead1);
    	Auxheader searchHeader12 = new Auxheader();
    	searchHeader12.setLabel(sessionHelper.getLabel("Value"));
    	searchHeader12.setColspan(1);
    	searchHeader12.setParent(searchHead1);
    	
    	final Vector<BiColumn> listColumns = result.getListColumns();
    	for (int i=0; i<listColumns.size(); i++){
    		BiColumn biColumn = (BiColumn) listColumns.get(i);
    		//if(biColumn.isNoQuery()) continue;
    		//UniLog.log("biColumn" + i +":" + biColumn.getEngName() + "," + biColumn.getChnName() + "," + biColumn.getLabel() + "," + biColumn.getColumnType() + "," + biColumn.getColumnLength() + "," + biColumn.getField() + "," + (biColumn.getField() != null ? biColumn.getField().getFullName() : ""));
    		final Row row = new Row();
    		rows.appendChild(row);
    		
    		inputFieldsList.add(new InputFields(biColumn, sessionHelper.getAllowNewAdvSearch() ? null : row));
    	}
        
        final Button btnClearForm = new ZkBiButton();
        btnClearForm.setLabel("Clear Conditions");
        btnClearForm.setId("clearform_button");
        btnClearForm.addEventListener("onClick",
        	new ZkBiEventListener() {
        		public void onZkBiEvent(Event event) throws Exception {
        			UniLog.log("Clear Form Button Pressed");
        			inputFieldsList.clearAllInputFields();
        			Events.postEvent(Events.ON_CLICK, masterWin.getFellow("btReload"), null);
            	}	
        		
        	}
        );
    	
    	
    	Row buttonRow = new Row();
    	rows.appendChild(buttonRow);
    	
    	org.zkoss.zul.Cell ce = new org.zkoss.zul.Cell();
    	ce.setColspan(3);;
    	buttonRow.appendChild(ce);
    	Hbox hbox = new Hbox();
    	hbox.setAlign("end");
    	ce.appendChild(hbox);
    	hbox.appendChild(btnClearForm);
    	
    	
    	conditionPresetListbox = new Listbox();
    	conditionPresetListbox.setRows(1);
   		conditionPresetListbox.setWidth("220px");
    	//conditionPresetListbox.setHeight("25px");
    	conditionPresetListbox.setMold("select");
    	//conditionPresetListbox.setTooltiptext("Choose preset search condition. You can define your own search condition by using advanced search.");
    	conditionPresetListbox.setTooltiptext(
    		sessionHelper.getLabel("Choose preset search condition.") + "\n" +
    		sessionHelper.getLabel("You can define your own search condition by using advanced search.")
 		);

    	conditionPresetIsDefault = new Checkbox();
    	conditionPresetIsDefault.setLabel("Default");
    	conditionPresetIsDefault.setWidth("50px");
   		conditionPresetIsDefault.setTooltiptext(
 			sessionHelper.getLabel("Set current preset as default.") + "\n" +
 			sessionHelper.getLabel("Not applicable to public preset.")
 		);
    	conditionPresetIsDefault.addEventListener(Events.ON_CHECK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				String preset = conditionPresetListbox.getSelectedItem().getValue();
				if (preset != null) {
					mConditionPresets.setDefaultPreset(preset, conditionPresetIsDefault.isChecked());
					mConditionPresets.saveConditionPresets(presetid, sessionHelper);
				}
			}
    	});
    	{
    	}

    	//build preset block
		condDiv = new Div();
		condDiv.setHeight("30px");
		condDiv.setSclass("input-group");
		if (sessionHelper.getAllowS2Listbox())
			condDiv.addSclass("zkbi-presetbox-select2");
		conditionPresetIsDefault.setSclass("input-group-addon");
		conditionPresetIsDefault.setLabel("");
		conditionPresetIsDefault.setHeight("28px");
		conditionPresetIsDefault.setWidth(isMobile() ? "30px" : "40px");
		conditionPresetIsDefault.setStyle("padding-top:3px; padding-bottom:3px; padding-left:3px; padding-right:3px;");
		conditionPresetListbox.setSclass("form-control");
		//conditionPresetListbox.setHeight("28px");
		if (isMobile()) {
			conditionPresetListbox.setStyle("padding-top:4px; padding-bottom:4px; padding-left:0px; padding-right:0px;");
			condDiv.setHflex("1");
			conditionPresetListbox.setWidth("");
			ZkUtil.appendStyle(conditionPresetListbox, "font-size:12px !important;");
			if(sessionHelper != null && sessionHelper.getAllowMobileAdvanceSearch()) {
				conditionPresetIsDefault.setVisible(false);
				conditionPresetListbox.setVisible(false);
			}
		} else 
			conditionPresetListbox.setStyle("padding-top:4px; padding-bottom:4px; padding-left:5px; padding-right:5px;");
		condDiv.appendChild(conditionPresetIsDefault);
		condDiv.appendChild(conditionPresetListbox);
		
		//build query bar
		if (sessionHelper.getAllowTour() && btTour != null){
//			queryBar.appendChild(btTour);
			headerLabelDiv.appendChild(btTour);
		}
   		queryBar.appendChild(btAdvSearch);
   		queryBar.appendChild(divAdvSearchG2);
   		queryBar.appendChild(btAdjustWidth);
		queryBar.appendChild(condDiv);
   		queryBar.appendChild(btReload);
   		ZkUtil.appendSclass(queryBar,"basictour_s2");
    		
    	Button btnAddPreset = new ZkBiButton();
        btnAddPreset.setLabel("Save Preset");
        btnAddPreset.setId("savepreset_button");
        btnAddPreset.setVisible(!mConditionPresets.isFromUrl());
        btnAddPreset.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
			private String realPresetKey, lastRealPresetKey;
			private Checkbox cbPublic;
			private void setRealPresetKey(String presetKey, boolean isPublic) {
				realPresetKey = (isPublic ? "public_" : "custom_") + presetKey;
			}
			private String getPresetKey(String realPresetKey) {
				return realPresetKey.substring(realPresetKey.indexOf('_') + 1);
			}
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				final Textbox tb = new Textbox();
				tb.setPlaceholder("Preset name");
				final Checkbox cbDefault = new Checkbox("Set as default");
				cbPublic = new Checkbox("Public mode");
				cbPublic.setChecked(false);
				final Checkbox cbDisplayRecCnt = new Checkbox("Display record count in dashboard");
				final Vbox vbox = new Vbox(){{
					appendChild(tb);
					appendChild(cbDefault);
					if (sessionHelper.isAdminUser()) {
						appendChild(cbPublic);
						appendChild(cbDisplayRecCnt);
					}
				}};
				cbPublic.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>(){
					@Override
					public void onZkBiEvent(CheckEvent event) throws Exception {
						setRealPresetKey(tb.getText(), event.isChecked());
					}
				});
				MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Save Preset", vbox, 
						new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
						masterWin, new ZkBiEventListener<Messagebox.ClickEvent>() {
					@Override
					public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
						if (event.getButton() == Messagebox.Button.OK) {
							if (tb.isValid()) {
								boolean isUpdate = false;
								if (mConditionPresets.containsPreset(realPresetKey))
									isUpdate = true;
								else if (lastRealPresetKey != null) {
									if (getPresetKey(realPresetKey).equals(getPresetKey(lastRealPresetKey)))
										mConditionPresets.removeFieldMap(lastRealPresetKey);
								}
								UniLog.log("save to " + realPresetKey);
								ConditionFieldMap fieldMap = inputFieldsList.makeConditionFieldMap(!cbPublic.isChecked(),result);
								mConditionPresets.putFieldMap(realPresetKey, fieldMap);
								mConditionPresets.setDefaultPreset(realPresetKey, cbDefault.isChecked());
								mConditionPresets.setDisplayRecCntPreset(realPresetKey, cbDisplayRecCnt.isChecked());
								String errMsg = mConditionPresets.saveConditionPresets(presetid, sessionHelper);
								//Messagebox.show(StringUtils.defaultString(errMsg, "Preset saved"));
								showMsg(sessionHelper.getLabel("Preset saved"));
								if (!isUpdate) {
									Events.postEvent("onReset", conditionPresetListbox, realPresetKey);
									result.putUserData("presetKey", realPresetKey);
								}
							} else
								event.stopPropagation();
						}
					}
    			});
				String presetKey = "";
				lastRealPresetKey = realPresetKey = conditionPresetListbox.getSelectedItem().getValue();
				ConditionFieldMap fieldMap = null;
				if (realPresetKey != null)
					fieldMap = mConditionPresets.getFieldMap(realPresetKey);
				if (fieldMap == null /*|| !fieldMap.isCustom()*/) {
					for (int i = 1;; i++) {
						presetKey = "preset" + i;
						setRealPresetKey(presetKey, false);
						if (!mConditionPresets.containsPreset(realPresetKey))
							break;
					}
				} else
					presetKey = getPresetKey(realPresetKey);
    			tb.setSelectedText(0, presetKey.length(), presetKey, true);
    			tb.setConstraint(constraint);
    			if (fieldMap != null) {
    				cbDefault.setChecked(fieldMap.isDefault());
    				cbPublic.setChecked(!fieldMap.isCustom());
    				cbDisplayRecCnt.setChecked(fieldMap.isDisplayRecCnt());
    			}
				dlg.doHighlighted();
			}
			private final Constraint constraint = new Constraint() {
				final Pattern reg = Pattern.compile("[\\w \\.]+");
				@Override
				public void validate(Component comp, Object value)
						throws WrongValueException {
					String s = value != null ? (String) value : "";
					/*
					if (!reg.matcher(s).matches())
						throw new WrongValueException(comp, "Please input english characters");
					*/
					if (s.length() < 1){
						throw new WrongValueException(comp, "Name too short");
					}
					setRealPresetKey(s, cbPublic.isChecked());
					//if (mConditionPresets.containsPreset(realPresetKey))
					//	throw new WrongValueException(comp, "Duplicate preset name");
				}
			};
        });
    	hbox.appendChild(btnAddPreset);

    	Button btnDeletePreset = new ZkBiButton();
        btnDeletePreset.setLabel("Delete Preset");
        btnDeletePreset.setId("deletepreset_button");
        btnDeletePreset.setVisible(!mConditionPresets.isFromUrl());
        btnDeletePreset.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				final String presetKey = conditionPresetListbox.getSelectedItem().getValue();
				if (presetKey == null)
     				Messagebox.show("Please choose preset item");
				else if (!sessionHelper.isAdminUser() && !mConditionPresets.getFieldMap(presetKey).isCustom())
     				Messagebox.show("This preset can't be updated!");
				else {
					Messagebox.show(String.format("'%s'?", presetKey), "Delete preset", 
						new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
						Messagebox.QUESTION, new ZkBiEventListener<Messagebox.ClickEvent>() {
						@Override
						public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
							if (event.getButton() == Messagebox.Button.OK) {
								mConditionPresets.removeFieldMap(presetKey);
								String errMsg = mConditionPresets.saveConditionPresets(presetid, sessionHelper);
								//Messagebox.show(errMsg == null ? "Preset deleted" : errMsg);
								showMsg(errMsg == null ? sessionHelper.getLabel("Preset deleted") : errMsg);
								Events.postEvent("onReset", conditionPresetListbox, null);
								inputFieldsList.clearAllInputFields();
								Events.postEvent(Events.ON_CLICK, masterWin.getFellow("btReload"), null);
							}
						}
					});
				}
			}
        });
    	hbox.appendChild(btnDeletePreset);
    	hbox.appendChild(new Label("Limit:"));
    	ibLimit = new Spinner();
    	ibLimit.setConstraint("no negative,no zero");
    	ibLimit.setStep(1000);
    	ibLimit.setValue(result.getRecLimit());
    	if (!sessionHelper.getAllowNewAdvSearch())
    		hbox.appendChild(ibLimit);
    	else
    		ibLimit.setWidth("95px");
    	conditionPresetListbox.setAttribute("biResult", result);
    	conditionPresetListbox.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
			@Override
			public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
				checkComponentState();
				String preset = event.getReference().getValue() != null ? event.getReference().getValue().toString() : null;
   		   		inputFieldsList.reset(preset);
   		   		setFrozenColumn(frozenCount);
   		   		visibleCols(preset, comp,result);
   		   		//refresh(result,masterWin,-1,true,false);
   		   		/*
   		   		if(result != null) {
   		   			if(result.setModifiedViewList(fieldMap == null ? null : fieldMap.getColumnOrders())) {
   		   				resetListHeader(result);
   		   			}
   		   		}
   		   		*/
//   		   		MultiSortMap sortMap = preset != null ? new MultiSortMap(preset,result) : null;
   		   		MultiSortMap sortMap = null;
   		   		boolean needResetHeader = false;
   		   		if(preset != null) {
   		   			sortMap = new MultiSortMap(preset,result);
   		   			if(result != null) {
//   				if(result.setModifiedViewList(fieldMap == null ? null : fieldMap.getColumnOrders())) {
   		   				ConditionFieldMap fieldMap = mConditionPresets.getFieldMap(preset);
   		   				if(result.setModifiedViewList(fieldMap.getColumnOrders())) {
   		   					resetListHeader(result);
   		   				}
   		   				List<String> adhocColList = fieldMap.getAdhocColumns();
   		   				if(adhocColList != null && adhocColList.size() > 0) {
   		   					result.clearAdhocolumns();
   		   					try {
   								for(String ss : adhocColList) {
   									JSONObject jo = new JSONObject(ss);
   									result.addAdhocColumn(
   										jo.optString("adhocColLabel"),
   										jo.optString("adhocColHdr"),
   										jo.optString("adhocColFormula"),
   										jo.optString("adhocColFormat"),
   										jo.optString("adhocColType"),
   										jo.optString("adhocColAgg")
   									);
   								}
   		   					} catch (Exception ex) {
   		   						UniLog.log(ex);
   		   					}
 		   					needResetHeader = true;
   		   				} else {
   		   					if(result.getAdhocColumnList() != null) {
   		   						result.clearAdhocolumns();
   		   						needResetHeader = true;
   		   					}
   		   				}
   		   			}
   		   		} else {
   		   			if(result != null) {
   		   				if(result.setModifiedViewList(null)) {
   		   					needResetHeader = true;
   		   				}
   		   				if(result.getAdhocColumnList() != null) {
   		   					result.clearAdhocolumns();
   		   					needResetHeader = true;
   		   				}
   		   			}
   		   		}
   		   		if(needResetHeader) resetListHeader(result);
   		   		/*
				if((ZkBiComposerBase.this instanceof ZkBiComposerLedgerReport)) {
					((ZkBiComposerLedgerReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
				} else if((ZkBiComposerBase.this instanceof ZkBiComposerAnalysisReport)) {
					((ZkBiComposerAnalysisReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
				} else {
					refresh(result,masterWin,sortMap,false);
				}
				*/
				onSelectionChanged(result,sortMap,masterWin);
				divAdvSearchG2Indicator.setVisible(false);
				Events.echoEvent("onSetupEmbedSearch", event.getTarget(), preset);
				if (isIncludeEmbedSearch() && !sessionHelper.isMobile() && zkbiEmbedSearchDiv != null && zkbiEmbedSearchDiv.getAttribute("oldHeight") != null)
					Events.echoEvent("onCustomAfterSize", zkbiEmbedSearchDiv, new AfterSizeEvent(Events.ON_AFTER_SIZE, zkbiEmbedSearchDiv, 0, (Integer)zkbiEmbedSearchDiv.getAttribute("oldHeight")));
			}
    	});
    	conditionPresetListbox.addEventListener("onReset", mConditionPresetListboxEventListener);
    	conditionPresetListbox.addEventListener("onSelect1", mConditionPresetListboxEventListener);
    	conditionPresetListbox.addEventListener("onSetupSelect2", mConditionPresetListboxEventListener);
    	conditionPresetListbox.addEventListener("onSetupEmbedSearch", mConditionPresetListboxEventListener);
    	Events.sendEvent("onReset", conditionPresetListbox, null);
    	Events.echoEvent("onSetupSelect2", conditionPresetListbox, null);
    }
    private EventListener<Event> mConditionPresetListboxEventListener = new ZkBiEventListener<Event>() {
		@Override
		public void onZkBiEvent(Event event) throws Exception {
			UniLog.log1("mConditionPresetListbox event:%s", event);
			Listbox conditionPresetListbox = (Listbox) event.getTarget();
			if (event.getName().equals("onReset")) {
				String selectedKey = (String) event.getData();
		    	while (conditionPresetListbox.getItemCount() > 0)
		    		conditionPresetListbox.removeItemAt(0);
		    	//conditionPresetListbox.appendItem(" -- " + getLabel("Preset Search") + " -- ", null);
		    	conditionPresetListbox.appendItem(" - " + sessionHelper.getLabel("Choose preset search") + " - ", null);
		    	conditionPresetListbox.setSelectedIndex(0);
		    	if (mConditionPresets.hasPresets()) {
		    		for (String s : mConditionPresets.getPresets()) {
		    			ConditionFieldMap cfm = mConditionPresets.getFieldMap(s);
		    			Listitem li = conditionPresetListbox.appendItem(ConditionPresets.buildPresetLabelByKey(s, cfm.getAccessKeyForPublic()),s);
		    			if (cfm.isDefault()){
		    				li.setStyle("font-weight: bold;");
		    				//li.setStyle("font-style: italic; font-weight: bold;");
		    			}
		    			if (selectedKey != null && s.equals(selectedKey)) {
		    				conditionPresetListbox.selectItem(li);
		    			}
		    		}
		    	}
				checkComponentState();
				divAdvSearchG2Indicator.setVisible(false);
				if (sessionHelper.getAllowS2Listbox() && StringUtils.equals((String)conditionPresetListbox.getAttribute("setupSelect2.status"),"Y"))
					ZkUtil.delayJs(conditionPresetListbox,null,50,"zkbis2.setup('%s',%s,%s,'%s',%b,%b);$('#%s').focus()",conditionPresetListbox.getUuid(), false, false, "", false, false, conditionPresetListbox.getUuid());
				Events.echoEvent("onSetupEmbedSearch", event.getTarget(), selectedKey);
			} else if (event.getName().equals("onSelect1")) {
				String presetKey = (String) event.getData();
		    	UniLog.log("selectConditionPresetListboxItem " + presetKey);
		  		conditionPresetListbox.setSelectedIndex(0);
				for(int i=0;presetKey!=null&&i<conditionPresetListbox.getItemCount();i++) {
					Listitem li = conditionPresetListbox.getItemAtIndex(i);
					if(li.getValue() != null && li.getValue().equals(presetKey)) {
			    		conditionPresetListbox.selectItem(li);
			    		break;
			    	}
			    }
				checkComponentState();
				Events.echoEvent("onSetupEmbedSearch", event.getTarget(), presetKey);
			} else if (event.getName().equals("onSetupSelect2")) {
//				if (sessionHelper.getAllowS2Listbox() && conditionPresetListbox.isVisible()) {
//					conditionPresetListbox.setAttribute("select2-enable", "Y");
//					//ZkUtil.setupSelect2(conditionPresetListbox, true);
//					ZkUtil.setupSelect2(conditionPresetListbox, true, false);
//				}
			} else if (event.getName().equals("onSetupEmbedSearch")) {
				setupEmbedSearch((String)event.getData(), (BiResult)event.getTarget().getAttribute("biResult"));
			}
		}
    };
    private void clearSortDirection(Component comp) {
		for (Component tmpComp : comp.queryAll("Listheader")){
			Listheader tmpListheader = (Listheader) tmpComp;
			if (tmpListheader.getId().startsWith("browser_listheader_")){
				ZkUtil.js("var $sorticon = jq('#%s').find('.z-listheader-sorticon');"
						+ "if ($sorticon.hasClass('listheader-sorticon')) {"
						+ "		$sorticon.find('.listheader-sortnum').remove();"
						+ "		$sorticon.removeClass('listheader-sorticon');"
						+ "}", tmpListheader.getUuid());
				setListHeaderSortDirection(tmpListheader, "natural");
			}
		}
    }
    public void sortSingle(BiResult result, Component comp, int p_sortIdx, boolean p_sortDesc) 
    {
		mMultiSortMap.clear();
		if(p_sortIdx > 0)
			sortMulti(result, comp, p_sortIdx, p_sortDesc);
		else {
			clearSortDirection(comp);
			if (p_sortIdx == 0) {
				result.clearOrderBy();
				result.sort();
				listModelList.clear();
				listModelList.addAll(result.getResultStat());
	       		doSearch(true, false);
	  			lastSelectIdx = -1;
	  			setSelectIdx(-1,null);
			}
		}
    }
    public static class ColumnSortEvent extends SortEvent {
		public ColumnSortEvent(String name, Component target, boolean ascending) {
			super(name, target, ascending);
		}
    }
    public static class MultiSortInfo {
    	public boolean sortDesc;
    	public int num;
    	public MultiSortInfo(boolean sortDesc) {
    		this.sortDesc = sortDesc;
    	}
    	public MultiSortInfo(boolean sortDesc, int num) {
    		this(sortDesc);
    		this.num = num;
    	}
    	@Override
    	public Object clone() {
    		return new MultiSortInfo(sortDesc, num);
    	}
    }
    public class MultiSortMap extends LinkedHashMap<Integer, MultiSortInfo> {
		public MultiSortMap() {
		}
		public MultiSortMap(String preset,BiResult result) {
			reset(preset,result);
		}
		public void reset(String preset,BiResult result) {
			ConditionFieldMap fieldMap = mConditionPresets.getFieldMap(preset);
			List<Integer> orderbys = null;
			int orderby = 0;
//			if(result != null) {
//				if(result.setModifiedViewList(fieldMap == null ? null : fieldMap.getColumnOrders())) {
//					resetListHeader(result);
//				}
//			}
			if(fieldMap != null) {
			List<Pair<String, Boolean>> orderbyLabels = fieldMap.getOrderbyLabels();
			Pair<String, Boolean> orderbyLabel = fieldMap.getOrderbyLabel();
			//orderbyLabels
			if (orderbyLabels != null) {
				orderbys = new ArrayList<Integer>();
				for (Pair<String, Boolean> obl : orderbyLabels) {
					int xcolid = result.listColumnPosition(obl.getLeft());
					if (xcolid >= 0) {
						xcolid++;
						if(obl.getRight()) xcolid = - xcolid;
						orderbys.add(xcolid);
					}
					/*
					Integer colid = getListBoxColumnId(obl.getLeft());
					if (colid != null) 
						orderbys.add(obl.getRight() ? -colid : colid);
							*/
				}
				UniLog.log("orderbyLabels to orderbys:" + orderbys);
			} else {
			//orderbys
				orderbys = (List<Integer>) ObjectUtils.defaultIfNull(fieldMap.getOrderbys(), new ArrayList<Integer>());
			}
			//orderbyLabel
			if (orderbyLabel != null) {
				Integer colid = getListBoxColumnId(orderbyLabel.getLeft());
				if (colid != null)
					orderby = orderbyLabel.getRight() ? -colid : colid;
				UniLog.log("orderbyLabel to orderby:" + orderby);
			} else {
			//orderby
				orderby = fieldMap.getOrderby();
			}
			if (orderbys.isEmpty() && orderby != 0)
				orderbys.add(orderby);
			}
			clear();
			if(orderbys != null) {
			for (int i = 0; i < orderbys.size(); i++)
				put(Math.abs(orderbys.get(i)), new MultiSortInfo(orderbys.get(i) < 0, i + 1));
			}
		}
    }
   	protected MultiSortMap mMultiSortMap = new MultiSortMap();
    public void sortMulti(BiResult result, Component comp) {
		clearSortDirection(comp);
   		result.clearOrderBy();
		for (Map.Entry<Integer, MultiSortInfo> entry : mMultiSortMap.entrySet()) {
    		UniLog.log("sort multi " + entry.getKey() + " " + entry.getValue());
    		result.addOrderByViewList(entry.getKey(), entry.getValue().sortDesc);
		}
		result.sort();
       	listModelList.clear();
       	listModelList.addAll(result.getResultStat());
       	int i = 0;
    	for (Map.Entry<Integer, MultiSortInfo> entry : mMultiSortMap.entrySet()) {
    		entry.getValue().num = ++i;
    		String id = "#browser_listheader_" + entry.getKey();
    		Listheader listheader = (Listheader) comp.query(id);
    		if (listheader != null) {
    			if (entry.getValue().sortDesc)
    				setListHeaderSortDirection(listheader, "descending");
    			else
    				setListHeaderSortDirection(listheader, "ascending");
    		}
    	}
    	refreshSortIcon();
       	doSearch(true, false);
  		lastSelectIdx = -1;
  		setSelectIdx(-1,null);
    }
    public void sortMulti(BiResult result, Component comp, int p_sortIdx, boolean p_sortDesc) {
   		if (p_sortIdx <= 0)
   			return;
   		if (mMultiSortMap.containsKey(p_sortIdx))
    		mMultiSortMap.remove(p_sortIdx);
    	mMultiSortMap.put(p_sortIdx, new MultiSortInfo(p_sortDesc));
    	sortMulti(result, comp);
    }
    private void refreshSortIcon() {
		UniLog.log("refreshSortIcon");
    	Listhead listhead = (Listhead) listbox.query("Listhead");
		for (Component c : listhead.queryAll("Listheader")) {
			Listheader lh = (Listheader) c;
			String id = lh.getId();
			String prefix = "browser_listheader_";
			if (id.startsWith(prefix)) {
				int sortIdx = Integer.parseInt(id.substring(prefix.length()));
				if (mMultiSortMap.containsKey(sortIdx)) {
					Clients.evalJavaScript(String.format("setTimeout(function(){"
							+ "var $sorticon = jq('#%s').find('.z-listheader-sorticon');"
							+ "if (!$sorticon.hasClass('listheader-sorticon')) {"
							+ "		$sorticon.append('<span class=\\'listheader-sortnum\\'>%d</span>').addClass('listheader-sorticon');"
							+ "}}, 10)", lh.getUuid(), mMultiSortMap.get(sortIdx).num));
				}
			}
		}
    }
    public void refresh(final BiResult result,final Component comp){
   		refresh(result,comp, 1, false, false);
    }
    
    public void refresh(final BiResult result, final Component p_comp, int p_sortIdx, boolean p_sortDesc, boolean p_doSearch){
    	MultiSortMap sortMap = null;
    	if (p_sortIdx >= 0)
    		sortMap = new MultiSortMap();
    	if (p_sortIdx > 0)
    		sortMap.put(p_sortIdx, new MultiSortInfo(p_sortDesc, 1));
    	refresh(result, p_comp, sortMap, p_doSearch);
    }
    public void refresh(final BiResult result, final Component p_comp, MultiSortMap sortMap){
    	refresh(result, p_comp, sortMap, false);
    }
    protected void after_refresh(BiResult result) {
    }
    public void refresh(final BiResult result, final Component p_comp, MultiSortMap sortMap, boolean p_doSearch){
    	refresh_real(result, p_comp, sortMap, p_doSearch);
    	after_refresh(result);
    }
    private void refresh_real(final BiResult result, final Component p_comp, MultiSortMap sortMap, boolean p_doSearch){
    	Component comp;
    	if(p_comp == null) comp = masterWin; else comp=p_comp;
    	UniLog.log("refresh: sortMap: "+sortMap);

		//init query condition
		result.clearCondition();
		if (!p_doSearch){
			if (zkBiSearch != null){
				zkBiSearch.clearSearch();
			}
		}

		clearSortDirection(comp);
   		mMultiSortMap.clear();

    	//add condition		
		ReturnMsg rtn = inputFieldsList.toConditions(result);
		listModelList.clear();
		if (rtn.getStatus())
			rtn = setAdditionalQueryCondition(result);
  		ReturnMsg queryStatus;
  		if(rtn.getStatus()) {
  			result.setRecLimit(ibLimit.getValue());
  			queryStatus = result.query(true);
  		} else {
  			result.clear();
    		//Messagebox.show(rtn.getMsg());
  			ZkUtil.showErrMsg(rtn.getMsg());
    		return;
  		}
		setupListboxSort(result, comp, sortMap, false);
    	setAggregateAndPivotHeaders(result,null,null);
        listModelList.addAll(result.getResultStat());
  		lastSelectIdx = -1;
  		setSelectIdx(-1,null);
        if (p_doSearch){
        	doSearch(true, false);
        }
		if (queryStatus.getStatus()){ 
			if(result.getRowCount() >= result.getRecLimit()) {
				showWarnMsg(sessionHelper.getLabel("Reload record - Query result has more than the limit of %d Records. Please refine search condition or increase the limit"), result.getRecLimit());
			} 
			else {
				showMsg(sessionHelper.getLabel("Reload record - %d records found"), result.getRowCount());
			}
			headerLabel.setTooltiptext(
					String.format(sessionHelper.getTtLabel("%d Records Loaded at %s") , result.getRowCount(),new SimpleDateFormat("HH:mm:ss").format(new Date()))
					);
		}
		else{
			showErrMsg(String.format(sessionHelper.getLabel("Reloads record - Error:%s"),queryStatus.getMsg()));
			headerLabel.setTooltiptext("");
		}
		refreshButtonStatus(result);
		Clients.resize(listbox);
		//use js to fix column not align bug
		Clients.evalJavaScript(String.format(
				"var $lbp = jq('#%s').parent();"
				+ "if (typeof($lbp.attr('saved_listbox_hscroll_left')) !== 'undefined'){"
				+ "	var $lbbody = $lbp.find('.z-listbox-body');"
				+ "	var sl = parseInt($lbp.attr('saved_listbox_hscroll_left'));"
				+ "	$lbbody.scrollLeft(sl);"
				+ "	$lbp.removeAttr('saved_listbox_hscroll_left');"
				+ "}", listbox.getUuid()));
    }
    private void setupListboxSort(BiResult result, Component p_comp, MultiSortMap sortMap, boolean needClearSortDirection) {
    	Component comp;
    	if(p_comp == null) comp = masterWin; else comp=p_comp;
    	if (needClearSortDirection) {
    		clearSortDirection(comp);
    		mMultiSortMap.clear();
    	}
    	if (sortMap != null) {
			result.clearOrderBy();
		  	//mMultiSortMap.putAll(sortMap);
		   	int listColumnSize = result.getListColumns().size() + (result.getAggregateOrPivotList() != null ? result.getAggregateOrPivotList().size() : 0);
		   	List<Integer> removedNums = new ArrayList<Integer>();
			for (Map.Entry<Integer, MultiSortInfo> entry : sortMap.entrySet()) {
				UniLog.log1("sortMap key:%d, num:%d, sortDesc:%b, listColumnSize:%d", entry.getKey(), entry.getValue().num, entry.getValue().sortDesc, listColumnSize);
				if (entry.getKey() <= listColumnSize)
					mMultiSortMap.put(entry.getKey(), entry.getValue());
				else if (entry.getValue().num > 0)
					removedNums.add(entry.getValue().num);
			}
			if (!removedNums.isEmpty()) {
				for (Map.Entry<Integer, MultiSortInfo> entry : sortMap.entrySet()) {
					MultiSortInfo v = entry.getValue();
					int tnum = v.num;
					for (int num : removedNums) {
						if (tnum > num)
							v.num--;
					}
				}
			}
			for (Map.Entry<Integer, MultiSortInfo> entry : mMultiSortMap.entrySet()) {
		    	UniLog.log("sort multi " + entry.getKey() + " " + entry.getValue().sortDesc);
		    	result.addOrderByViewList(entry.getKey(), entry.getValue().sortDesc);
		    	Listheader listheader = (Listheader) comp.query("#browser_listheader_"+entry.getKey());
		    	if (listheader != null) {
		    		if (entry.getValue().sortDesc)
    					setListHeaderSortDirection(listheader, "descending");
			   		else
    					setListHeaderSortDirection(listheader, "ascending");
		    	}
			}
			result.sort();
    	}
    }
    protected void visibleCols(String preset, Component comp,BiResult result) {
 		List<Integer> defaultHideCols = null;
   		if (preset != null) {
   			UniLog.log("visibleCols key:" + preset);
   			ConditionFieldMap fieldMap = mConditionPresets.getFieldMap(preset);
   			//hideColLabels
   			List<String> hideColLabels = fieldMap.getHideColLabels();
   			if (hideColLabels != null) {
   				defaultHideCols = new ArrayList<Integer>();
   				for (String hcl : hideColLabels) {
   					Integer colid = getListBoxColumnId(hcl);
   					if (colid != null)
   						defaultHideCols.add(colid);
   				}
				UniLog.log("hideColLabels to hideCols:" + defaultHideCols);
   			} else {
   				defaultHideCols = fieldMap.getHideCols();
   			}
   			hideAggregates = fieldMap.getHideAggregates();
//			if(result != null) result.setModifiedViewList(fieldMap.getColumnOrders());
   		} else {
//   			List<ConditionPresets.ConditionFieldMap.AggregateProperty> aggProperties;
   			hideAggregates = null;
   			
   		}
   		Listhead listhead = (Listhead) comp.query("#browser_listhead");
   		if (listhead != null) {
   			for (int i = 1; i <= listhead.getChildren().size(); i++) {
   				Listheader listheader = (Listheader) listhead.query("#browser_listheader_" + i);
   				if (listheader != null) {
   					Boolean b = (Boolean)listheader.getAttribute("isHideForTempBiColumn");
   					if (b != null)
						listheader.setVisible(!b);
   					else
   						listheader.setVisible(defaultHideCols == null || !defaultHideCols.contains(i));
   				}
   			}
   			if (isWidget()) {
   				Listheader listheader = (Listheader) listhead.query("#browser_listheader_0");
   				if (listheader != null)
   					listheader.setVisible(false);
   			}
   		}
    }
    public void refresh(final BiResult result, final Component p_comp, int p_sortIdx, boolean p_sortDesc){
    	refresh(result, p_comp, p_sortIdx, p_sortDesc, false);
    }
    
    public boolean doBrowseItemSelected(XulElement p_win, BiResult p_result)
    {
    	if(!p_result.allowDetail()) return(true);
   		return(doUpdateOneRow(p_win,p_result)) ;
    }
    void changeDetailSubHeader(BiResult p_result) {
    	//remark: if need to show navigationbar properly, need to define view.primarykey
    	if(p_result.getPrimaryColumns() != null) {
    		addSubHeader(p_result.getCellString(p_result.getPrimaryColumns().toString()));
    	} else {
    		//addSubHeader(""+p_result.getCurrentCollection().getSid());  //andrew220825: it's a bit misleading to show serial_id in navigationbar
    		addSubHeader(sessionHelper.getLabel("Record Details"), sessionHelper.isAdminUser() ? String.format(sessionHelper.getLabel("Record Sid:%d"), p_result.getCurrentCollection().getSid()) : "");
    	}
    }
    public boolean doUpdateOneRow(XulElement p_win,BiResult p_result) {
    	changeDetailSubHeader(p_result);
//    	if(p_result.getPrimaryKey() != null) {
//    		addSubHeader(p_result.getCellString(p_result.getPrimaryKey()));
//    	} else {
//    		addSubHeader("Record Details");
//    	}
		if(detailForm == null) {
			JxZkBiBase dw = buildDetailWindow(p_result);
			if(dw != null) {
					detailForm = dw;
					dw.setIsMobile(isMobile());
					dw.bindCellCollection(p_result,dw.displayOnlyWhenUpdate ? JxZkBiBase.MODE_DISPLAY : JxZkBiBase.MODE_UPDATE);
					dw.translateAllComp(p_result);
//					dw.doModalUpdate(this);
					if(dw.displayOnlyWhenUpdate) dw.doDisplay(); else dw.doUpdate();
			}
		}
		else {
			detailForm.bindCellCollection(p_result,detailForm.displayOnlyWhenUpdate ? JxZkBiBase.MODE_DISPLAY : JxZkBiBase.MODE_UPDATE);
			detailForm.translateAllComp(p_result);
//			detailForm.doModalUpdate(this);
//			detailForm.doUpdate();
			if(detailForm.displayOnlyWhenUpdate) detailForm.doDisplay(); else detailForm.doUpdate();
		}
		//detailForm.changeMode(p_result, JxZkBiBase.MODE_UPDATE, masterWin.getFellowIfAny("JxZkBiBase")); //cannot hardcode object id
		//detailForm.changeMode(p_result, JxZkBiBase.MODE_UPDATE, JxZkBiBase.getDetailWindow(masterWin, p_result)); //moved to bindCellCollection
		return(true);
    }
    public boolean doAddOneRow(XulElement p_win,BiResult p_result) {
    	addSubHeader("Add Record");
		if(detailForm == null) {
				JxZkBiBase dw = buildDetailWindow(p_result);
				if(dw != null) {
					detailForm = dw;
					dw.setIsMobile(isMobile());
					dw.bindCellCollection(p_result,JxZkBiBase.MODE_ADD);
					dw.translateAllComp(p_result);
					//dw.doModalAdd(this);
					dw.doAdd();
				}
		} 
		else {
			detailForm.bindCellCollection(p_result,JxZkBiBase.MODE_ADD);
			detailForm.translateAllComp(p_result);
			//detailForm.doModalAdd(this);
			detailForm.doAdd();
		}
		//detailForm.changeMode(p_result, JxZkBiBase.MODE_ADD, JxZkBiBase.getDetailWindow(masterWin, p_result)); //moved to bindCellCollection
		return(true);
    } 
    protected boolean isMobile() {
    	if (sessionHelper == null) {
    		return false;
    	}
    	return(sessionHelper.isMobileDevice());
    }
    private String estListboxHeight(String p_windowHeight){
    	int h = 0;
   		if(p_windowHeight != null) {
    	try{
    		h = Integer.parseInt(p_windowHeight.replaceAll("\\D+",""));
    		h -= 100;
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    	}
   		}
    	if (h <= 100)
    		h  = 400;
    	return(h + "px");
    }
    private String estListboxWidth(String p_windowWidth){
    	int w = 0;
    	try{
    		w = Integer.parseInt(p_windowWidth.replaceAll("\\D+",""));
    		w -= 20;
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    	}
    	if (w <= 100)
    		w = 900;
    	return(w + "px");
    }
    private void resizeListbox(){
    	if (isMobile()){
    		return;
    	}
   		if(!StringUtils.equalsIgnoreCase(this.getURLParam("mode"),"list")) {
		masterWin.setHeight(""); //dirty way to allow masterWin expand
   		}
    }
    class ExportToExcel implements ZkBiTimerEventInterface {
    	ExcelPoi jxf = null;
    	int xr = 0;
    	int ridx = 0;
    	BiResult result;
   		Vector<Integer> linksStIdx = null;
    	Vector links = null;
    	Vector<BiColumn> vvv;
    	Hashtable<BiColumn,String>formulaHash = new Hashtable<BiColumn,String>();
   		int colt[];
   		String outFileName = null;
   		List<Integer> aggCols = null;
    	ExportToExcel(BiResult p_result, String p_outFileName) throws IOException {
    		outFileName = p_outFileName;
    		result = p_result;
  			vvv = new Vector<BiColumn>();
			if(expWinDetail.isChecked()) {
					links = result.getExportLinks();
					linksStIdx = new Vector<Integer>();
					
					
//					Vector xv = result.getColumns();
//					for(int i = 0;i<xv.size();i++) {
//						BiColumn cl = (BiColumn) xv.get(i);
//						if(cl.isInvisible(result.getSessionHelper())) continue;
//    					vvv.add(cl);
//					}

					Vector<BiColumn> xv = result.getListColumns();
    				Listhead lh = listbox.getListhead();
    				List <Component> lhdrs = lh.getChildren();
   					if(expWinTemplate.isChecked()) {
   						List<BiColumn> lc = result.getImportColumns();
   						for(BiColumn bc : lc) {
   							vvv.add(bc);
   						}
   					} else {
    				for(int i= (hasAUDColumn ? 1:0);i<baseHeaderCount;i++) {
    					Listheader lhdr = (Listheader) lhdrs.get(i);
    					if(lhdr.isVisible()) vvv.add(xv.get(i - (hasAUDColumn ? 1 : 0) ));
    				}		
   					}
					
			} else {
   					if(expWinTemplate.isChecked()) {
   						List<BiColumn> lc = result.getImportColumns();
   						for(BiColumn bc : lc) {
   							vvv.add(bc);
   						}
   					} else {
					Vector<BiColumn> xv = result.getListColumns();
    				Listhead lh = listbox.getListhead();
    				List <Component> lhdrs = lh.getChildren();
    				for(int i= (hasAUDColumn ? 1:0);i<baseHeaderCount;i++) {
    					Listheader lhdr = (Listheader) lhdrs.get(i);
    					if(lhdr.isVisible()) vvv.add(xv.get(i - (hasAUDColumn ? 1 : 0) ));
    				}
   					}
   			}
   			InputStream is = sessionHelper.openResourceAsStream("/template/export_template.xlsx");
			jxf = ExcelPoi.newExcelPoi(is,true); 
   			is.close();
   			
   			if(result.aggregateOrPivotSize() > 0) {
   				int n = result.aggregateOrPivotSize();
   				aggCols = new ArrayList<Integer>();
   				for(int i=0;i<n;i++) {
   					if(isAggregateVisible(result, result.getAggregateOrPivotHeader(), i)) {
   						aggCols.add(i);
   					}
   				}
   			}
   			
   			colt = result.formatExportExcel (
   					jxf,
   					links
   					, linksStIdx
   					, expWinTemplate.isChecked()
   					, vvv
   					, aggCols
			);
   			
    		exportTimer.setDelay(200);
    		exportTimer.setRepeats(true);
    		exportTimer.setRunning(true);
    		progressName.setValue(sessionHelper.getLabel("Processing..."));
    		progressMeter.setValue(0);
    		progressMeter.invalidate();
    		progressPanel.doModal();
    	}
    	public void onTimerFired() {
    		int i = ridx;
    		int n = ridx + 50;
    		if(n > listModelList.getSize()) n = listModelList.getSize();
    		UniLog.log("Export to Excel Timer fired processing rows");
			for(;i<n;i++) {
    			int realIdx = getTrIdxByObj(listModelList, listModelList.get(i));
    			xr = result.putOneRowToExceli(
    					realIdx,
    					linksStIdx,
    					links,
    					vvv,
    					colt,
    					jxf,
    					xr,
    					expWinMerged.isChecked(),
    					expWinTemplate.isChecked(),
    					aggCols
				);
				if((i % 100) == 0) {
					if(progressMeter != null) {
						int ps = ((i * 100)/ result.getRowCount());
						UniLog.log1("excel export row:%d progress:%d",i,ps);
					}
				}
				if((i % 500) == 0) {
					System.gc();
				}
			}
			ridx = i;
    		if(ridx >= listModelList.getSize()) {
    			//result.postProcessExportExcel(jxf,expWinTemplate.isChecked(),vvv);
    			result.postProcessExportExcel(jxf,expWinTemplate.isChecked(),vvv,expWinMerged.isChecked()); //andrew220609 ignore autoresize for mergeRows. It avoid slow autoResizeColumn bugs
    			try {
    				ByteArrayOutputStream bos = new ByteArrayOutputStream();
    			    jxf.writeWorkBook(bos);
    			    if (!expWinPassword.isDisabled() && expWinPassword.getText().length() > 0){
    			    	ByteArrayOutputStream passwordProtectedOS = new ByteArrayOutputStream();
    			       	//ZipUtil.createZip(expWinPassword.getText(), true, passwordProtectedOS, new ByteArrayInputStream(bos.toByteArray()), outFileName + ".xls");
    			       	ZipUtil.createZip(expWinPassword.getText(), true, passwordProtectedOS, new ByteArrayInputStream(bos.toByteArray()), outFileName + ".xlsx");
    			    	Filedownload.save(passwordProtectedOS.toByteArray(), "application/zip", outFileName + ".zip");
    			    	passwordProtectedOS.close();
    			    }
    			    else{
    			    	//Filedownload.save(bos.toByteArray(), "application/vnd.ms-excel", outFileName);
    			    	Filedownload.save(bos.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outFileName);
    			    } 
    			} catch (Exception ex) {
    				UniLog.log(ex);
    			}
    			synchronized(exportTimerEvent) {
    				exportTimerEvent.zkBiTimerEventInterface = null;
    			}
    			exportTimer.setRunning(false);
    			progressPanel.setVisible(false);
    		} else {
    			//UniLog.logm(this, "debug: %d", ((int) ((ridx * 100) / result.getRowCount())));
    			progressMeter.setValue((ridx * 100) / result.getRowCount());
    		}
			//System.gc();  //andrew231103 gc too often, it make the cpu busy
    	}
    	public void onCancelClicked() {
    		UniLog.log("Export to Excel Cancelled");
    		synchronized(exportTimerEvent) {
    			exportTimerEvent.zkBiTimerEventInterface = null;
    		}
    		exportTimer.setRunning(false);
    		progressPanel.setVisible(false);
    	}
    }   
    void setupSearch(final BiResult result)
    {
    	if (sessionHelper.getAllowSearch() && !result.getView().isNoSearch()) {
    		zkBiSearch = new ZkBiSearchHelper();
    		
    		tbSearchBox.setPlaceholder(sessionHelper.getLabel("Quick Filter"));
    		tbSearchBox.setSclass("zkbi-im-search");
    		tbSearchBox.setTooltiptext(sessionHelper.getTtLabel("To filter type and hit enter"));
    		tbSearchBox.setMaxlength(sessionHelper.getQuickFilterMaxChar());
   			tbSearchBox.setIconSclass("z-icon-filter");
    		zkBiSearch.setTbSearchBox(tbSearchBox);
    		//cbSearchBox.setLabel(sessionHelper.getLabel("Match All"));
    		cbSearchBox.setLabel(sessionHelper.getBtLabel("Match All"));
    		zkBiSearch.setCbSearchBox(cbSearchBox);
    		if(isMobile()) {
    			tbSearchBox.setWidth("100%");
    		} else {
    			tbSearchBox.setWidth("270px");
    		}
    		zkBiSearch.setSearchDiv(searchDiv);
    		/*
    		if (isMobile()){
    			searchDiv.setSclass("zkbi-nscrollout-mark70");
    		}
    		*/
    		zkBiSearch.setSearchTagDiv(searchTagDiv);
    		//zkBiSearch.setListModelList(listModelList);
    		zkBiSearch.setupSearch(result, this);
    		
    		/*searchTagDiv.setStyle("display:inline-block;vertical-align:bottom");*/
    		searchTagDiv.setStyle("display:inline-block;");
    		searchDiv.appendChild(tbSearchBox);
    		searchDiv.appendChild(new Div(){{
    			this.setStyle("display:inline-block;padding-top:5px;"); //dirty way to make it look like align center
    			this.appendChild(cbSearchBox);
    		}});
    		cbSearchBox.setVisible(!isMobile()); //mobile not allow user to change search mode
    		//Space space = new Space();
    		if (isMobile()) {
    			//space.setHeight("10px");
    			//space.setStyle("display:block;");
    			searchDiv.appendChild(searchTagDiv);
    			//searchDiv.appendChild(space);
    			/*
    			if (sessionHelper.isPagingMode()){
    				Paging pg = new Paging();
    				pg.setHflex("min");
    				pg.setParent(searchDiv.getParent());
    				pg.setDetailed(true);
    				listbox.setPaginal(pg);
    			}
    			*/
    		} else {
    			//searchDiv.appendChild(space);
    			searchDiv.appendChild(searchTagDiv);
    		}
    		//actionBar.appendChild(searchDiv);
    		if (sessionHelper.getAllowQuickFilterURLParam()) {
    			searchDiv.addEventListener("onSetupFromUrl", new EventListener<Event>() {
    				@Override
					public void onEvent(Event event) throws Exception {
    					String qfm = sessionHelper.getURLParam("qfm");
    					String[] qfs = (String[])getURLParams().get("qf");
    					UniLog.log1("event:%s, qf:%s, qfm:%s", event, qfs, qfm);
   						cbSearchBox.setChecked(!StringUtils.equalsIgnoreCase(qfm, "or"));
   						zkBiSearch.setDefaultSearcAndMode(cbSearchBox.isChecked());
    					if (qfs != null) {
    						for (String s : qfs) {
    							tbSearchBox.setText(URLDecoder.decode(s, "UTF-8"));
    							Events.sendEvent(Events.ON_OK, tbSearchBox, null);
    						}
    					}
    				}
    			});
    			Events.echoEvent("onSetupFromUrl", searchDiv, null);
    		}
    		
    	}
    } 
    protected boolean doSearchSingle(String p_inText, int p_colIdx){
    	if (zkBiSearch != null){
    		return(zkBiSearch.doSearchSingle(p_inText, p_colIdx));
    	}
    	return(false);
    }
    void doSearch(boolean p_ignoreBlankFlag, boolean p_addSearchTag){
    	if (zkBiSearch != null){
    		zkBiSearch.doSearch(p_ignoreBlankFlag, p_addSearchTag);
    		return;
    	}
    }
    protected void clearSearch(BiResult result){
    	if (zkBiSearch != null){
    		zkBiSearch.clearSearch();
    	}
    	if(listModelList != null) {
    		listModelList.clear();
  		 	listModelList.addAll(result.getResultStat());
    	}
    }
    
    void showMsg(String p_format, Object...p_args){
       	//Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: 5000 })");
    	ZkUtil.showMsg(p_format, p_args);
       	//addSyslog(false, p_format, p_args);
    }
    void showWarnMsg(String p_format, Object...p_args){
       	//Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"warn\", globalPosition:\"bottom right\", autoHideDelay: 5000 })");
    	ZkUtil.showWarnMsg(p_format, p_args);
       	addSyslog(false, p_format, p_args);
    }
    void showErrMsg(String p_format, Object...p_args){
       	//Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", globalPosition:\"bottom right\", autoHideDelay: 5000 })");
    	ZkUtil.showErrMsg(p_format, p_args);
       	addSyslog(false, p_format, p_args);
    }
    protected void addSyslog(boolean p_autoOpen, String p_format, Object...p_args){
       	if (sessionHelper.getAllowSyslog()){
       		if (p_autoOpen){
       			Clients.evalJavaScript("Logger.open(); log(\""+String.format(p_format, p_args)+"\")");
       		}
       		else{
       			Clients.evalJavaScript("log(\""+String.format(p_format, p_args)+"\")");
       		}
       	}
    }
    void setupExportWindow(final BiResult p_result, String p_id){
    	expWin.setBorder("normal");
    	expWinPassword.setType("password");
    	expWin.setClosable(true);
        expWin.addEventListener(Events.ON_CLOSE, new ZkBiEventListener<Event>(){
			public void onZkBiEvent(Event event) throws Exception {
				event.getTarget().setVisible(false);
				event.stopPropagation();
			}
        });
        Vbox vbox = new Vbox();
        
        
        expWin.setHeight("800px");
        expWin.setWidth("600px");
        if (p_id != null){
        	expWin.setId(p_id);
        }
        expWin.setTitle(sessionHelper.getLabel("Confirm export to excel?"));
        expWin.setVisible(false);
        
        Separator sep = new Separator("vertical");
        sep.setSpacing("10px");
        expWin.appendChild(vbox);
        Space space = new Space();
        space.setHeight("10px");
        vbox.setWidth("100%");
        vbox.setHeight("100%");
        Vbox msgDiv = new Vbox();
        msgDiv.setHflex("1");
        msgDiv.setVflex("5");
        Hbox btnDiv = new Hbox();
        btnDiv.setHflex("1");
        btnDiv.setVflex("1");
        btnDiv.setPack("center");
        vbox.appendChild(msgDiv);
        //msgDiv.setPack("center");
        msgDiv.setAlign("start");

        
        msgDiv.appendChild(new Space(){{ this.setHeight("15px");}});
        msgDiv.appendChild(new Label(sessionHelper.getLabel("Export file name:")));
        msgDiv.appendChild(expWinFileName);
        msgDiv.appendChild(new Space(){{ this.setHeight("15px");}});
        
        expWinFileName.setConstraint("/[a-zA-Z0-9\\-_]+/: Please enter a valid file name");
        expWinFileName.setInstant(true);
        
		expWinPassword.setDisabled(true);
		
		expWinPasswordCb.setLabel(sessionHelper.getLabel("Password protect"));
        expWinPasswordCb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>(){
			public void onZkBiEvent(Event event) throws Exception {
					expWinPassword.setDisabled(!((Checkbox)event.getTarget()).isChecked());
				}
        });

		expWinMerged.setLabel(sessionHelper.getLabel("Merge common field"));
		expWinMerged.setTooltiptext(sessionHelper.getTtLabel("Cell merge common master record field"));
		expWinMerged.setStyle("padding-left:0px");

		expWinTemplate.setLabel(sessionHelper.getLabel("As Import Template"));
		expWinTemplate.setTooltiptext(sessionHelper.getTtLabel("Used as input template. e.g. dropdown list for option field, Greyed out for readonly column,.."));
		expWinTemplate.setStyle("padding-left:0px");

		expWinDetail.setLabel(sessionHelper.getLabel("Export All Detail"));
		expWinDetail.setTooltiptext(sessionHelper.getTtLabel("Include detail record data fields"));
		expWinDetail.setStyle("padding-left:0px");
        
        msgDiv.appendChild(expWinPasswordCb);
        msgDiv.appendChild(new Label(sessionHelper.getLabel("Password:")));
        msgDiv.appendChild(expWinPassword);
        msgDiv.appendChild(new Space(){{ this.setHeight("15px");}});
        msgDiv.appendChild(expWinMerged);
        msgDiv.appendChild(expWinTemplate);
        msgDiv.appendChild(expWinDetail);
        
        
        vbox.appendChild(btnDiv);
        Button btnExportOk = new ZkBiButton();
        btnExportOk.setLabel(sessionHelper.getBtLabel("Ok"));
        
        btnExportOk.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
			public void onZkBiEvent(Event event) throws Exception {
					if (!expWinFileName.isValid()){
						return;
					}
		        	UniLog.logm(this, "start export");
					expWin.setVisible(false);
		        	synchronized(exportTimerEvent) {
		        			exportTimerEvent.zkBiTimerEventInterface = new ExportToExcel(p_result, expWinFileName.getText());
//		        		try {
//		        			exportTimerEvent.zkBiTimerEventInterface = new ExportToExcel(p_result, expWinFileName.getText());
//		        		} 
//		        		catch (Exception ex ) {
//		        			UniLog.log(ex);
//		        		}
		        	}
				}
        });
        
        Button btnCancel = new ZkBiButton();
        btnCancel.setLabel(sessionHelper.getBtLabel("Cancel"));
        btnCancel.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
			public void onZkBiEvent(Event event) throws Exception {
				expWin.setVisible(false);
			}
        });
        
        btnDiv.setStyle("text-align:center;");
        btnDiv.appendChild(btnExportOk);
        btnDiv.appendChild(sep);
        btnDiv.appendChild(btnCancel);
        expWin.setParent(masterWin);
    }
    void showExportWindow(){
    	expWinPassword.setText("");
    	expWinPasswordCb.setChecked(false);
    	expWinPassword.setDisabled(true);
   	    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    	expWinFileName.setText("export_" + format.format(new Date()));
    	
        expWin.doModal();
    }    
    
    public void hideListPanel()
    {
    	zkbiListTop.setVisible(false);
    	bottomPanelVbox.setVisible(false);
    	queryBar.setVisible(false);
    	if (shortcutBar != null) {
    		shortcutBar.setHflex("1");  //andrew201223 after hide queryBar, need to expand shortcutBar to maintain icon alignment
    	}
    	abHelper.removeActionButtonMenu();
    	//Clients.confirmClose("Are you sure to leave?");
    	if (zkbiEmbedSearchDiv != null)
    		zkbiEmbedSearchDiv.setVisible(false);
    }
    public void showListPanel()
    {
    	zkbiListTop.setVisible(true);
    	queryBar.setVisible(true);
    	bottomPanelVbox.setVisible(true);
    	if (shortcutBar != null) {
    		shortcutBar.setHflex("min"); //andrew201223 when queryBar appear, need to collapse shortcutBar
    		shortcutBar.invalidate();    //force redraw
    	}
    	
    	removeSubHeader();
		listbox.setSelectedIndex(-1);
		abHelper.showActionButtonMenu(); //andrew190822: regenerate button menu to fix menu not responding problem
		
		if (isWidget()) {
			queryBar.setVisible(false);
			bottomPanelVbox.setVisible(false);
			btnHelp.setVisible(false);
		}
    	if (zkbiEmbedSearchDiv != null)
    		zkbiEmbedSearchDiv.setVisible(zkbiEmbedSearchDiv.hasAttribute("isVisibleInBrowseMode"));
    	if (isMobile()) {
    		//xjcheng230613: scanner icon disappear after click close button problem
    		headerBox.addEventListener("onResizeForhideListPanel", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("onResizeForhideListPanel event:%s", event);
					Clients.resize(headerBox);
					headerBox.removeEventListener("onResizeForhideListPanel", this);
				}
    		});
    		ZkUtil.delayPostEvent("onResizeForhideListPanel", headerBox, null, 10);
    	}
    }

	@Override
	public void searchResult(ArrayList<TrStatFilter> searchResultStatList,HashSet<TrStatFilter> il) {
		listModelList.clear();
        listModelList.addAll(searchResultStatList);
        if(il != null) listModelList.setSelection(il);
  		lastSelectIdx = -1;
  		setSelectIdx(-1,null);
	}
	private void setupHotkey(){
		hotkeyList.clear();
    	zkBiHotkey = new ZkBiHotkeyHelper(masterWin, this, true, true);
	}
	
	//assume alt+<key>
	//TODO: support detail form
	public void addHotkey(char p_key, Component p_comp){
		hotkeyList.add(Pair.of(p_key, p_comp));
		if (p_comp instanceof Button){
			((Button)p_comp).setTooltiptext(String.format("%s (Alt+%s)", ((Button) p_comp).getTooltiptext() == null ? "" : ((Button) p_comp).getTooltiptext(), p_key));
		}
	}
	
	@Override
	public void hotkeyEvent(int p_modifierKey, char p_dataKey) {
		switch (p_modifierKey){
		case ZkBiHotkeyHelper.ALT_KEY:
			for (int i=0; i<hotkeyList.size(); i++){
				char key = hotkeyList.get(i).getLeft();
				Component comp = hotkeyList.get(i).getRight();
				if (key == p_dataKey){
					if (ZkBiHotkeyHelper.checkAcceptHotkey(comp)){ //fire first validate candidate
						Events.postEvent("onClick", comp, null);
						return;
					}
				}
			}
			ZkUtil.showWarnMsg(sessionHelper.getLabel("Hotkey(Alt+%c) not supported"), p_dataKey);
			break;
		case ZkBiHotkeyHelper.ESC_KEY:
			if (expWin.isVisible()){
				Events.postEvent("onClose", expWin, null);
				return;
			}
			if (detailForm != null && detailForm.isFormVisible()){
				detailForm.doClose(true);
				return;
			}
			break;
		}
	}
	public void addSubHeader(String p_subHeader) {
		addSubHeader(p_subHeader, null);
	}
	public void addSubHeader(String p_subHeader, String p_tooltiptext){ //TODO multiple level
		removeSubHeader();
		//andrew 181228: fix html generate 2 more unwanted right arrow by adding </i>
		headerLabelDiv.insertBefore(new Html("&nbsp;<i class=\"fa fa-caret-right\"></i>&nbsp;"){{
			this.setId("subheadersep");
			this.setStyle("font-size:8pt; user-select: none;");
		}}, btnHelp);
		
		Toolbarbutton btSubHeader =  new Toolbarbutton(sessionHelper.getLabel(p_subHeader));
		if (StringUtils.isNotBlank(p_tooltiptext)) {
			btSubHeader.setTooltiptext(p_tooltiptext);
		}
		btSubHeader.setId("subheader");
		btSubHeader.setSclass("zkbi-header-label");
		btSubHeader.setHref("#");
   		btSubHeader.setDisabled(StringUtils.equalsIgnoreCase(this.getURLParam("closetab"),"Y"));
		headerLabelDiv.insertBefore(btSubHeader, btnHelp);
	}
	public void removeSubHeader(){
		if (headerLabelDiv.hasFellow("subheader")){
			headerLabelDiv.getFellow("subheader").detach();
		}
		if (headerLabelDiv.hasFellow("subheadersep")){
			headerLabelDiv.getFellow("subheadersep").detach();
		}
	}
	
	private void buildAdvSearchFooter(final BiResult result){
    	if (sessionHelper.getAllowNewAdvSearch()){
    		final Toolbarbutton btnClearForm = new Toolbarbutton(){{
    			setId("clearform_button1");
    			//setIconSclass("z-icon-eraser");
				//setImage("images/icons/zkweb/051-file-1-25x25.png");
    			setIconSclass("z-icon-times");
    			setTooltiptext(sessionHelper.getTtLabel("Clear Conditions"));
				setSclass("narrowtoolbarbutton");
    			addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Events.sendEvent(Events.ON_CLICK, masterWin.getFellow("clearform_button"), null);
						Clients.resize(listbox);
					}
				});
    		}};
    		final Toolbarbutton btnSavePreset = new Toolbarbutton(){{
    			setId("savepreset_button1");
    			setIconSclass("z-icon-plus");
    			
    			setTooltiptext(sessionHelper.getTtLabel("Save Preset"));
				setSclass("narrowtoolbarbutton");
    			addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Events.sendEvent(Events.ON_CLICK, masterWin.getFellow("savepreset_button"), null);
						Clients.resize(listbox);
					}
				});
    		}};
    		final Toolbarbutton btnDeletePreset = new Toolbarbutton(){{
    			setId("deletepreset_button1");
    			setIconSclass("z-icon-minus");
    			setTooltiptext(sessionHelper.getTtLabel("Delete Preset"));
				setSclass("narrowtoolbarbutton");
    			addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Events.sendEvent(Events.ON_CLICK, masterWin.getFellow("deletepreset_button"), null);
						Clients.resize(listbox);
					}
				});
    		}};
    		btnCustCondition = new Toolbarbutton(){{
    			setId("custcondition_button");
    			setIconSclass("z-icon-file-code-o");
    			setTooltiptext(sessionHelper.getTtLabel("Custom Query Condition"));
				setSclass("narrowtoolbarbutton");
    			addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						UniLog.log("custcondition_button " + event.getName());
						final Textbox tb = new Textbox();
						tb.setMultiline(true);
						tb.setWidth("100%");
						tb.setHeight("calc(100% - 45px)");
						tb.setValue(inputFieldsList.getCustomCondition());
						MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Custom condition", tb, 
								new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.IGNORE, Messagebox.Button.CANCEL}, 
								new String[]{null, "CLEAR", null}, 
								masterWin, new ZkBiEventListener<Messagebox.ClickEvent>(){
									@Override
									public void onZkBiEvent(ClickEvent event) throws Exception {
										UniLog.log("Custom condition target:" + event.getTarget() + ",button:" + event.getButton() + ",value:" + tb.getValue());
										if (event.getButton() == null)
											return;
										switch (event.getButton()) {
										case OK:
											inputFieldsList.setCustomCondition(tb.getValue());
											break;
										case IGNORE:
											tb.setValue("");
											event.stopPropagation();
											break;
										default:
											break;
										}
									}
								}
						);
						dlg.setWidth("90%");
						dlg.setHeight("90%");
						dlg.doHighlighted();
					}
				});
    			addEventListener("onStateChange", new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Toolbarbutton btn = (Toolbarbutton) event.getTarget();
						switch (inputFieldsList.validateCustomCondition(result)) {
						case 1:
		    				btn.setStyle("color:#00ff00;");
							break;
						case 2:
		    				btn.setStyle("color:#ff0000;");
							break;
						default:
		    				btn.setStyle("color:rgba(0,0,0,0.57);");
							break;
						}
					}
    			});
    		}};
    		listfoot = new Listfoot();
	    	listfoot.setVisible(false);
	    	listfoot.setId("listtood");
	    	listfoot.setParent(listbox);
	    	if (!isMobile()) {
		    	listfoot.appendChild(new Listfooter(){{
	   				this.setStyle("vertical-align: top;");
		    		this.appendChild(
		    		    new Hbox(){{
			    			this.setAlign("center");
			    			this.appendChild(btnSavePreset);
			    			this.appendChild(btnDeletePreset);
			    			this.appendChild(btnClearForm);
			    			this.appendChild(btnCustCondition);
			    			this.appendChild(new Label("Limit:") {{
			    				ZkUtil.appendStyle(this, "font-size:10pt;");
			    			}});
		    				ZkUtil.appendStyle(ibLimit, "font-size:10pt;");
			    			this.appendChild(ibLimit);
    						this.setVisible(allowAdvSearchG1Preset);
		    			}});
		    	}});
		    	
		    	for (final InputFields inputFields : inputFieldsList) {
		    		final Vbox vbox = new Vbox();
		    		vbox.setSpacing("1px");
		    		listfoot.appendChild(new Listfooter(){{
	    				this.setStyle("vertical-align: top;");
	    				this.appendChild(vbox);
		    		}});
		    		if (!inputFields.isEmpty()) {
		    			if (inputFields.get(0).makeConditionField() != null)
		    				inputFields.insertBefore(0);
		    			for (InputField inputField : inputFields)
		    				buildAdvSearchCond(vbox, inputField);
		    		}
		    	}
	    	} else {
	    		final Vbox box = new Vbox();
	    		final Listfooter listfooter;
		    	listfoot.appendChild(listfooter = new Listfooter(){{
		    		appendChild(box);
		    	}});
	    		box.appendChild(new Div(){{
	    			appendChild(btnSavePreset);
	    			appendChild(btnDeletePreset);
	    			appendChild(btnClearForm);
    				appendChild(new Label("Limit:"){{
    					setStyle("vertical-align:bottom");
    				}});
    				appendChild(ibLimit);
    				setVisible(allowAdvSearchG1Preset);
	    		}});
		    	for (final InputFields inputFields : inputFieldsList) {
		    		final Vbox vbox = new Vbox();
		    		box.appendChild(vbox);
		    		if (!inputFields.isEmpty()) {
		    			if (inputFields.get(0).makeConditionField() != null)
		    				inputFields.insertBefore(0);
		    			for (InputField inputField : inputFields)
		    				buildAdvSearchCond(vbox, inputField);
		    		}
		    	}
		   		listfooter.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>(){
					@Override
					public void onZkBiEvent(AfterSizeEvent event) throws Exception {
						UniLog.log("listfooter aftersize: " + event.getWidth() + " " + event.getHeight());
						listbox.setAttribute("first_listfooter_width", event.getWidth());
						listbox.setAttribute("first_listfooter_height", event.getHeight());
						for (InputFields inputFields : inputFieldsList) {
							for (InputField inputField : inputFields) {
								if (inputField.parentComp != null){
									ZkUtil.delayPostEvent("onCustomResize", inputField.parentComp, null, 100);
								}
							}
						}
					}
		   		});
	    	}
    		checkComponentState();
    	}
	}
	private Textbox getAdvSearchTextbox(Component p_advSearchCond){
		if (!(p_advSearchCond instanceof Hbox)){
			return(null);
		}
		
		//look for first textbox exclude combobox
		for (Component comp : p_advSearchCond.getChildren()){
			if (comp instanceof Combobox){
				continue;
			}
			if (comp instanceof Textbox){ 
				return((Textbox)comp);
			}
		}
		return(null);
	}
	private void buildAdvSearchCond(final Vbox parentComp, final InputField inputField){
		final XulElement mbox = !isMobile() ? new Hbox() : new Div();
		final Label lb = new Label();
		final Listbox cb = inputField.cb;
   		final Vlayout vl = inputField.vl;
		final Textbox tb = inputField.tb;
   		final Hlayout hl = inputField.hl;
		final InputElement if0 = inputField.if0, if1 = inputField.if1;
		final Label lba = inputField.lba;
		final InputFields inputFields = inputField.parent;
		inputField.parentComp = mbox;
		
		if (mbox instanceof Box){
			((Box)mbox).setSpacing("0px");
		}
		
		lb.setStyle("display:block;");
		hl.setValign("middle");
  		vl.setWidth((String) ObjectUtils.defaultIfNull(listbox.getAttribute("last_vl_width"), ""));
		cb.setWidth("120px");
		if (isMobile()) {
			cb.setStyle("float:left;");
			vl.setStyle("float:left;");
			tb.setWidth("100%");
			if0.setHflex("1");
			if1.setHflex("1");
		} else {
			cb.setWidth("60px");
			tb.setWidth("140px");
			if (inputFields.bc.getColumnType().trim().equals("datetime")) {
				if0.setWidth("168px");
				if1.setWidth("168px");
			} else if (inputFields.bc.getColumnType().trim().equals("time")) {
				if0.setWidth("110px");
				if1.setWidth("110px");
			} else {
				if0.setWidth("140px");
				if1.setWidth("140px");
			}
			ZkUtil.appendStyle(cb, "font-size:8pt;");
			ZkUtil.appendStyle(tb, "font-size:8pt;");
			ZkUtil.appendStyle(if0, "font-size:8pt;");
			ZkUtil.appendStyle(if1, "font-size:8pt;");
		}
	   	lb.setValue(inputField.parent.bc.getEngName() + ":");
	   	tb.setPlaceholder(inputField.parent.bc.getEngName());
	   	if0.setPlaceholder(inputField.parent.bc.getEngName());
	   	if0.setTooltiptext(sessionHelper.getTtLabel("Please enter search condition and hit enter"));
	   	if1.setPlaceholder(inputField.parent.bc.getEngName());
	   	if1.setTooltiptext(sessionHelper.getTtLabel("Please enter search condition and hit enter"));
	   	
   		hl.appendChild(if0);
   		hl.appendChild(lba);
   		hl.appendChild(if1);
   		
   		vl.appendChild(tb);
   		vl.appendChild(hl);
	   	
   		if (isMobile() && inputField.parent.size() == 1)
   			mbox.appendChild(lb);
   		mbox.appendChild(cb);
   		mbox.appendChild(vl);
		final int index = inputFields.indexOf(inputField);
	   	
   		cb.addEventListener(Events.ON_SELECT,
   		    new ZkBiEventListener() {
   		    	public void onZkBiEvent(Event event) throws Exception {
   		       		UniLog.log("Query operator chaneg");
   		       		inputField.reFormat();
   		       		Clients.resize(listbox);
   		        }	
	    });		
   		mbox.addEventListener(Events.ON_OK, new ZkBiEventListener<KeyEvent>(){
			@Override
			public void onZkBiEvent(KeyEvent event) throws Exception {
				if (index == 0) {
					for (InputField inputField1 : inputFields) {
						if (inputField1.makeConditionField() == null) {
							showErrMsg(sessionHelper.getLabel("Please input condition!"));
							return;
						}
					}
					InputField newInputField = inputFields.add();
					newInputField.copy(inputField);
					buildAdvSearchCond(parentComp, newInputField);
					inputField.clear();
					XulElement focusComp;
					if (inputField.if0.isVisible())
						focusComp = inputField.if0;
					else if (inputField.tb.isVisible())
						focusComp = inputField.tb;
					else
						focusComp = (XulElement) inputField.parentComp;
					focusComp.setFocus(true);
					Clients.resize(listbox);
					//use js to fix column not align bug
					Clients.evalJavaScript(String.format(
							"var $lb = jq('#%s');"
							+ "var $lbbody = $lb.find('.z-listbox-body');"
							+ "$lb.parent().attr('saved_listbox_hscroll_left', '' + $lbbody.scrollLeft());", 
							listbox.getUuid()));
					ZkUtil.delayPostEvent(Events.ON_CLICK, masterWin.getFellow("btReload"), null, 100);
				}
			}
		});
   		mbox.addEventListener("onCustomResize", new ZkBiEventListener<Event>(){
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				if (!isMobile())
					return;
				final int lfWidth = (Integer) ObjectUtils.defaultIfNull(listbox.getAttribute("first_listfooter_width"), 300);
				final int padding = 32;
				final int cbWidth = 120;
				final int closeBtnWidth = (Integer) ObjectUtils.defaultIfNull(mbox.getAttribute("remove_button_width"), 52);
				int width = Math.max(lfWidth - padding - closeBtnWidth - cbWidth - 1, lba.isVisible() ? 120 : 50);
				vl.setWidth(width + "px");
				vl.invalidate();
				listbox.setAttribute("last_vl_width", width + "px");
				UniLog.log("mbox onCustomResize: tb_vis=" + tb.isVisible() + " hl_vis=" + hl.isVisible() + " if0_vis=" + if0.isVisible() + " lba_vis=" + lba.isVisible() + " width=" + width);
			}
   		});
		if (index > 0) {
			inputField.tbbClose = new Toolbarbutton(){{
				setIconSclass("z-icon-remove");
				setTooltiptext(sessionHelper.getTtLabel("Delete"));
				setSclass("narrowtoolbarbutton smalltoolbarbutton");
				setStyle("margin-top:5px;");
				if (isMobile())
					setStyle(getStyle() + "float:left;");
				addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
					public void onZkBiEvent(Event event) throws Exception {
						if (inputField.makeConditionField() != null)
							Events.postEvent(Events.ON_CLICK, masterWin.getFellow("btReload"), null);
						if (inputFields.size() > 1){
							inputFields.remove(inputField);
							showMsg(sessionHelper.getMsgLabel("Input field removed"));
						}
						else {
							inputField.clear();
							showMsg(sessionHelper.getMsgLabel("Input field cleared"));
						}
						Clients.resize(listbox);
					}
				});
				addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>(){
					@Override
					public void onZkBiEvent(AfterSizeEvent event) throws Exception {
						mbox.setAttribute("remove_button_width", event.getWidth());
						mbox.setAttribute("remove_button_height", event.getHeight());
						UniLog.log("remove_button size: " + event.getWidth() + " " + event.getHeight());
					}
				});
			}};
			mbox.appendChild(inputField.tbbClose);
		} else {
			mbox.appendChild(new Space(){{
				setWidth("26px");
			}});
		}
		inputField.setReadonly(index > 0);
		if (index + 1 < inputFields.size())
			parentComp.insertBefore(mbox, inputFields.get(index + 1).parentComp);
		else
			parentComp.appendChild(mbox);
	}
	private void checkComponentState() {
		final String presetKey = conditionPresetListbox.getSelectedItem().getValue();
   		conditionPresetIsDefault.setDisabled(true);
   		conditionPresetIsDefault.setChecked(false);
   		Button deletePresetBtn = (Button) masterWin.getFellow("deletepreset_button");
   		Button deletePresetBtn1 = (Button) masterWin.query("#deletepreset_button1");
   		deletePresetBtn.setDisabled(true);
   		if (deletePresetBtn1 != null)
   			deletePresetBtn1.setDisabled(true);
		if (presetKey != null) {
			ConditionFieldMap fieldMap = mConditionPresets.getFieldMap(presetKey);
			if (fieldMap != null) {
				if (fieldMap.isCustom()) {
					conditionPresetIsDefault.setDisabled(false);
					deletePresetBtn.setDisabled(false);
					if (deletePresetBtn1 != null)
						deletePresetBtn1.setDisabled(false);
				} else {
					if (sessionHelper.isAdminUser()) {
						deletePresetBtn.setDisabled(false);
						if (deletePresetBtn1 != null)
							deletePresetBtn1.setDisabled(false);
					}
				}
				if (fieldMap.isDefault())
					conditionPresetIsDefault.setChecked(true);
			}
		}
		if (btnCustCondition != null)
			ZkUtil.delayPostEvent("onStateChange", btnCustCondition, null, 10);
	}
	
	private String getComposerAction()
	{
		return(action);
	}
	
	private void nextComposerAction() {
		action = "browse";
	}

	protected boolean addLcPopup(BiResult p_result, Vector<BiColumn> p_listColumns, int p_colIdx, Listcell p_lc) {
		if (isMobile()){
			return(false);
		}
		Popup popup = new Popup();
    	String contentStr = null;
    	boolean hasLink = false;

    	//prepare content str of AUD column
    	if (p_colIdx < 0 && allowCopyAllFieldToClipboard){
			StringBuilder sb = new StringBuilder();
			for(int i = 0;i<p_listColumns.size();i++) {
				BiColumn biColumn = (BiColumn) p_listColumns.get(i);
				if (i > 0)
					sb.append("\r\n");
				String fieldName = sessionHelper.getLabel(biColumn.getEngName());
				sb.append(fieldName + ": " + p_result.getCell(biColumn.getLabel()).getString());
			}
			contentStr = sb.toString().trim();
    	}
    	//prepare content str of data column
    	if (p_colIdx >= 0 && allowCopyFieldToClipboard){
			contentStr = p_result.getCell(p_listColumns.get(p_colIdx).getLabel()).getString();
    	}
    	if (StringUtils.isNotBlank(contentStr)){
			//create visit view button
   			BiColumn biColumn = p_listColumns.get(p_colIdx);
//   			String visitUrl = null;
//   			if(p_result.getLinkedView(biColumn.getLabel()) != null) visitUrl = SessionHelper.getUrlByViewid(sessionHelper, p_result.getLinkedView(biColumn.getLabel()));
   			String visitUrl = p_result.getLinkedUrl(biColumn.getLabel(),p_result.getCurrentCollection());
    		if (p_colIdx >= 0 && sessionHelper.getAllowVisitView() && visitUrl != null /* && biColumn.isLookup() */){
	   			try {
	   				Toolbarbutton visitViewBt = new Toolbarbutton();  
	    			visitViewBt.setTooltiptext(sessionHelper.getTtLabel("Visit view"));
	    			visitViewBt.setIconSclass("z-icon-link");
					visitViewBt.setSclass("narrowtoolbarbutton");
	   				visitViewBt.setStyle("vertical-align:top;");
	   				/*
	   				JSONObject jo = new JSONObject();
	   				String cond = p_result.getLinkedColumn(biColumn.getLabel()) + " = '" + p_result.getCellString(biColumn.getLabel()) + "'";
	   				jo.put("customCondition", cond);
	   				String key = sessionHelper.putOneTimeData( jo);
	   				visitUrl += "&querycondition="+key;
	   				*/
	   				final JSONObject jo = p_result.getLinkedCondition(biColumn.getLabel(),p_result.getCell(biColumn.getLabel()));
	   				final String linkurl = visitUrl;
	   				hasLink = true;
	   				visitViewBt.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
						@Override
						public void onZkBiEvent(Event p_event) throws Exception {
							UniLog.logm(this, "clicked");
							String key = sessionHelper.putOneTimeData( jo);
							String url = linkurl + "&querycondition="+key;
							url += "&overrideaction=update";
							if(p_event instanceof MouseEvent) {
								MouseEvent me = (MouseEvent) p_event;
								if((me.getKeys() & me.CTRL_KEY) != 0) {
									ZkUtil.js("openNewTab('"+url+"')");
								} else {
									Executions.sendRedirect(url);
								}
							} else {
								Executions.sendRedirect(url);
							}	
							//TODO implement visit view feature
						}}
	   				);
	    			popup.appendChild(visitViewBt);
	   			} catch (Exception ex) {
	   				UniLog.log(ex);
	   			}
    		}
    	
    		//create copy to clipboard button
	    	Toolbarbutton copyBt = new Toolbarbutton();  
	    	copyBt.setTooltiptext(sessionHelper.getTtLabel("Copy to clipboard"));
	    	copyBt.setIconSclass("z-icon-copy");
			copyBt.setSclass("narrowtoolbarbutton");
			//copyBt.setClientAttribute("onClick", String.format("copyToClipboard('%s');",StringUtil.jsStringTrim(contentStr))); 
			copyBt.setClientAttribute("onClick", String.format("copyToClipboard('%s'); $.notify(\"%s\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: 5000 });", StringUtil.jsStringTrim(contentStr), sessionHelper.getLabel("Data Copied"))); 
			
			
	   		copyBt.setStyle("vertical-align:top;");
	    	popup.appendChild(copyBt);
    	
	    	//display content
	    	Vlayout vLayout = new Vlayout();
	    	vLayout.setStyle("display:inline-block;vertical-align:bottom;");
	    	for (String s : contentStr.split("\\r\\n|\\r|\\n")){
	    		vLayout.appendChild(new Label(s));
	    	}
	    	popup.appendChild(new Space());
	    	popup.appendChild(vLayout);
	    	
	    	popup.setParent(masterWin);
			p_lc.setTooltip(popup);
    	}
    	return(hasLink);
	}

    public void modelWindow(Window p_win) {
    	p_win.doModal();
    }
    public void refreshNeedSet() {
		for(Object o : needRefreshSet) {
			refreshListItems(o);
		}
		needRefreshSet = null;
    }
	@Override
	public void biBaseClose(BiResult p_br) {
		refreshNeedSet();
		if(getComposerAction().equals("add") || getComposerAction().equals("update")) {
			Clients.evalJavaScript("goBack()");  //andrew 190215: goback is not a good idea
	        //currently hardcoded to the main menu url, should find someway to generalize this
			//Executions.getCurrent().sendRedirect("menu.html?menuid=menu_main.html&menuitem=SITEMAP");
			//Clients.evalJavaScript("history.back(-1)"); 
		} else {
		    nextComposerAction();
		    showListPanel();
		}
		inDetailForm = false;
	}
	@Override
	public void biBaseOpen() {
		hideListPanel();
		inDetailForm = true;
		needRefreshSet = new HashSet();
	}
	@Override
	public void biBaseRefresh(BiResult p_result){
   		refresh(p_result,null,(MultiSortMap)mMultiSortMap.clone(),true);
	}
    public void biBaseRefresh(BiResult p_result, MultiSortMap p_sortMap, boolean p_doSearch){
		refresh(p_result,null,p_sortMap,p_doSearch);
    }
	@Override
	public void biBaseRefreshListitems(Object p_dataObj) {
		if(needRefreshSet == null) needRefreshSet = new HashSet();
		needRefreshSet.add(p_dataObj);
	}
	
	protected void setListheaderWidth(BiResult p_result, boolean p_widthByContent){
		if (p_widthByContent){
			btAdjustWidth.setSclass("narrowtoolbarbutton zkbi-active-toolbarbutton");
			//ZkUtil.removeSclass(listbox, "listbox-nwidthbycontent");
			//ZkUtil.addSclass(listbox, "listbox-widthbycontent");
		}
		else{
			btAdjustWidth.setSclass("narrowtoolbarbutton");
			//ZkUtil.removeSclass(listbox, "listbox-widthbycontent");
			//ZkUtil.addSclass(listbox, "listbox-nwidthbycontent");
		}
		
		Listheader lhr0 = (Listheader) listbox.getFellowIfAny("browser_listheader_0");
		if (lhr0 != null){
			lhr0.setWidth("70px");
			lhr0.setHflex(null);
		}
		
    	Vector<BiColumn> listColumns = p_result.getListColumns();
   		for(int i=0;i<listColumns.size();i++){
   			String compId = "browser_listheader_"+(i+1);
			Listheader lhr = (Listheader) listbox.getFellowIfAny(compId);
			if (lhr0 != null){
				lhr0.setWidth("70px");
			}
   			if (lhr != null){
   				//int minWidth = JxZkBiBase.calPxByString(lhr.getLabel()) + 50; //extra length for header button
   				lhr.setWidth(null); //required to clear the original value first
   				lhr.setHflex(null); //required to clear the original value first
    			JxZkBiBase.setComponentFormat(lhr, listColumns.get(i), 15, 0, 0, p_widthByContent,sessionHelper, "1", true);
   			}
   			else{
   				UniLog.logm(this, "%s not found", compId);
   			}
   		}
	}
	private static Button buildTestEditor(final Component p_parentComp){
   		Button btTestEditor = new ZkBiButton("Editor");
		btTestEditor.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event arg0) throws Exception {
				UniLog.logm(this,"click");
				final Window win = new Window();
				win.setId("winChild");
				win.setParent(p_parentComp);
				win.setWidth("800px");
				win.setHeight("800px");
				final CKeditor editor = new CKeditor();
				editor.setHeight("600px");
				editor.setCustomConfigurationsPath("/tmp/ckeditor_config.js");
				//editor.setToolbar("toolbar_Simple");
				editor.setToolbar("Simple");
				win.appendChild(editor);
				
				//save
				Button btSave = new ZkBiButton("Save"){{
					this.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
						@Override
						public void onZkBiEvent(Event arg0) throws Exception {
							UniLog.logm(this,"value:" + editor.getValue());
							Messagebox.show("" + editor.getValue());
						}
					});
					this.setParent(win);
				}};
				new Space(){{ setParent(win); }};
				
				//close
				Button btClose = new ZkBiButton("Close"){{
					this.setParent(win);
					this.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
						@Override
						public void onZkBiEvent(Event arg0) throws Exception {
							win.detach();
						}
					});
				}};
				win.doModal();
			}
		});
		return(btTestEditor);
	}

	
	public String getCurrentPresetName() {
		if(
			conditionPresetListbox != null &&
			conditionPresetListbox.getSelectedItem() != null &&
			conditionPresetListbox.getSelectedItem().getLabel() != null
			)
		return(conditionPresetListbox.getSelectedItem().getLabel().toString());
		else
		return("No Title");
	}
	public void setupBatchModeButton(Button p_button) {
    	//ZkUtil.setupBatchModeButton(p_button, batchModeToggleButton);
    	ZkUtil.setupBatchModeButton(p_button, batchModeToggleButton, getSessionHelper());
	}
	
	protected void doZkbiItemSelected(int p_idx,BiResult p_br) {
		
	}
	int getSelectIdx() {
		return(currSelectIdxXX);
	}
	void setSelectIdx(int p_idx,BiResult p_br) {
		currSelectIdxXX = p_idx;
 		int recIdx = -1;
 		if(p_br != null && currSelectIdxXX >= 0) {
 			java.util.Set selection = listModelList.getSelection();
 			Object selectedObject = selection.toArray()[0];
 			recIdx = getTrIdxByObj(listModelList, selectedObject);
 			p_br.loadOneRecV(recIdx);
 		}
 		UniLog.log("List Record " + recIdx + " Selected");
 		doZkbiItemSelected(recIdx,p_br);
	}
	
	public void clearSelection() {
    	listbox.clearSelection();
    	lastSelectIdx = -1;
	}
	
	private Integer getListBoxColumnId(String bcLabel) {
		for (Component tmpComp : listbox.queryAll("Listheader")){
			Listheader tmpListheader = (Listheader) tmpComp;
			BiColumn biColumn = (BiColumn) tmpListheader.getAttribute("ma_bicolumn");
			String aop = (String) tmpListheader.getAttribute("aggregate_or_pivot");
			if (tmpListheader.getId().startsWith("browser_listheader_") && (biColumn != null || aop != null)){
				int colid = Integer.parseInt( tmpListheader.getId().substring(19));
				if (StringUtils.equals(bcLabel, biColumn != null ? biColumn.getLabel() : aop))
					return colid;
			}
		}
		return null;
	}
	@Override
	public HashSet<Integer> getInludeList() {
		if(multiSelect) {
			java.util.Set selection = listModelList.getSelection();
			HashSet<Integer> il = new HashSet<Integer>();
			if(selection.size() > 0) {
          		for(Iterator it=selection.iterator();it.hasNext();) {
        				Object o = it.next();
        				int idx = getTrIdxByObj(listModelList, o);
        				il.add(idx);
          		}
          		return(il);
			}
		}
		return null;
	}
	
	private class AdvSearchCallback implements ZkBiAdvSearch.Callback {
		private BiResult result;
		private Component comp;
		public AdvSearchCallback(BiResult result, Component comp) {
			this.result = result;
			this.comp = comp;
		}
		
		@Override
		public void setPresetMapOrderbysAndHideCols(ConditionFieldMap cfm) {
			ZkBiComposerBase.this.setPresetMapOrderbysAndHideCols(cfm,result);
		}
		
		@Override
		public void refreshUIForSavePreset(String errMsg, ZkBiAdvSearch advSearch,BiResult result) {
			showMsg(errMsg == null ? sessionHelper.getLabel("Preset saved") : errMsg);
			String curPreset = advSearch.getSelectedPreset();
			inputFieldsList.reset(curPreset);
			inputFieldsList.customCondition = advSearch.getCustomParam();
			ibLimit.setValue(advSearch.getLimit());
			visibleCols(curPreset, comp,result);
			MultiSortMap sortMap = curPreset != null ? new MultiSortMap(curPreset,result) : null;
			//refresh(result,masterWin,sortMap,false);
			biBaseRefresh(result, sortMap, false);
			Events.sendEvent("onReset", conditionPresetListbox, curPreset);
		}

		@Override
		public void customConditionChanged(String newCondition) {
		}

		@Override
		public void customConditionReset(String newCondition) {
		}
		
		/*
		public List<ConditionPresets.ConditionFieldMap.AggregateProperty> getAggProperty() {
			String curPreset = advSearch.getSelectedPreset();
			if(curPreset == null)
		}
		*/
		
		@Override
		public EventListener<ClickEvent> getClickDialogButtonEvent(final ZkBiAdvSearch advSearch) {
			return new ZkBiEventListener<Messagebox.ClickEvent>(){
				@Override
				public void onZkBiEvent(ClickEvent event) throws Exception {
					UniLog.log("btAdvSearchG2 target:" + event.getTarget() + ",button:" + event.getButton());
					btAdvSearchG2.setSclass("narrowtoolbarbutton");
					if (event.getButton() == null) {
						return;
					}
					switch (event.getButton()) {
					case OK:
						String curPreset = advSearch.getSelectedPreset();
						
						String oldCondition = "";
						if (curPreset != null) {
							ConditionFieldMap cfm = mConditionPresets.getFieldMap(curPreset);
							oldCondition = cfm.getCustomCondition();
						}
						boolean updateFlag = !StringUtils.equals(oldCondition, advSearch.getCustomParam());
						UniLog.log1("oldcustomCondition:%s", oldCondition);
						UniLog.log1("getCustomParam:%s", advSearch.getCustomParam());
						UniLog.log1("customConUpdated:%b", updateFlag);

						inputFieldsList.customCondition = advSearch.getCustomParam();
						ibLimit.setValue(advSearch.getLimit());
						MultiSortMap sortMap = curPreset != null ? new MultiSortMap(curPreset,result) : null;
						/*
						if((ZkBiComposerBase.this instanceof ZkBiComposerLedgerReport)) {
							((ZkBiComposerLedgerReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
						} else if((ZkBiComposerBase.this instanceof ZkBiComposerAnalysisReport)) {
							((ZkBiComposerAnalysisReport) ZkBiComposerBase.this).onSelectionChanged(result,sortMap);
						} else {
							refresh(result,masterWin,sortMap,false);
						}
						*/
						onSelectionChanged(result,sortMap,masterWin);
						Events.sendEvent("onReset", conditionPresetListbox, curPreset);
						if (updateFlag)
							divAdvSearchG2Indicator.setVisible(true);
						break;
					default:
						break;
					}
				}
			};
		}
	};
	
	private void generateReport() {
	}
	
	ReturnMsg fetchRecordByListIdx(BiResult p_br,int p_idx) {
		Object trStatOrFilterTrStat = listModelList.get(p_idx);
		int trIdx = getTrIdxByObj(listModelList, trStatOrFilterTrStat, -1);
		if(trIdx >= 0) {
   			if(!p_br.fetchOneRecV(trIdx)) {  
    		   showErrMsg(sessionHelper.getLabel("Load record fail"));
   				Messagebox.show(sessionHelper.getLabel("Fetch Record Error") + ": " + p_br.getLastErrorMessage());
    		   return(ReturnMsg.defaultFail);
   			}
			listbox.setSelectedIndex(p_idx);
			return(ReturnMsg.defaultOk);
		}
		return(ReturnMsg.defaultFail);
	}
	@Override
	public ReturnMsg fetchNext(BiResult p_br) {
    	int idx = listbox.getSelectedIndex();
    	if(idx >= 0) {
    		if(listModelList.size() > idx+1) {
    			idx++;
//    			return(fetchRecordByListIdx(p_br,idx)) ;
    			ReturnMsg rtn = fetchRecordByListIdx(p_br,idx) ;
    			if(rtn == null || rtn.getStatus()) changeDetailSubHeader(p_br);
    			return(rtn);
    		}
    	}
		return ReturnMsg.defaultFail;
	}
	@Override
	public ReturnMsg fetchPrevious(BiResult p_br) {
    	int idx = listbox.getSelectedIndex();
    	if(idx > 0) {
    		idx--;
//    		return(fetchRecordByListIdx(p_br,idx)) ;
    		ReturnMsg rtn = fetchRecordByListIdx(p_br,idx) ;
    		if(rtn == null || rtn.getStatus()) changeDetailSubHeader(p_br);
    		return(rtn);
    	}
		return ReturnMsg.defaultFail;
	}
    
	@Override
	public Boolean hasNextRec() {
    	return ((listbox.getSelectedIndex()+1) < listModelList.size());
	}
	@Override
	public Boolean hasPrevRec() {
    	return listbox.getSelectedIndex() > 0;
	}
	
	protected void onBarcode(BiResult p_result,String p_barcode) {
   		 tbSearchBox.setText(p_barcode);
//   		 zkBiSearch.doSearch(false, false);
   		 zkBiSearch.doSearch(false, !zkBiSearch.getDefaultSearcAndMode());
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
    
    /* Override this method if sum pivoted aggregate is meanlingless and should be hide away */
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int idx) {
    	return(true);
    }
    protected void headerDragged(BiResult result,Component from,Component to) throws Exception {
    	BiColumn toCol = null;
    	BiColumn fromCol = null;
    	if(from.getAttribute("ma_bicolumn") != null) {
			fromCol = (BiColumn) from.getAttribute("ma_bicolumn");
    	}
    	if(to.getAttribute("ma_bicolumn") != null) {
			toCol = (BiColumn) to.getAttribute("ma_bicolumn");
    	}
    	if(fromCol != null && toCol != null) {
    		result.moveViewColumn(fromCol,toCol);
			resetListHeader(result);
//			refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
			biBaseRefresh(result);
    	}
    }

	public HashSet<String> getPivotColumns() {
		return null;
	}

    class HeaderDropListener extends ZkBiEventListener {
    	BiResult result;
    	public HeaderDropListener(BiResult p_result) {
    		result = p_result;
    	}
    	public void onZkBiEvent(Event arg0) throws Exception {
    		UniLog.log("onDrop event:"  + arg0.getName() +"," + arg0.getTarget().getId() + " dragged " + ((DropEvent) arg0).getDragged().getId());
    		headerDragged(result, ((DropEvent) arg0).getDragged(), arg0.getTarget());
    		arg0.stopPropagation();
    	}
    }

    class HeaderRemoveListener extends ZkBiEventListener {
    	BiResult result;
    	public HeaderRemoveListener(BiResult p_result) {
    		result = p_result;
    	}
    	public void onZkBiEvent(Event event) throws Exception {
    		final String ah = (String) event.getTarget().getAttribute("pivot_header");
    		final String id = (String) event.getTarget().getAttribute("pivot_id");
    		UniLog.log1("event:%s, ah:%s, id:%s", event, ah, id);
    		if (id != null) {
    			BiColumn bc = result.getColumnByLabel(id);
    			if (bc != null) {
    				ZkBiMsgbox.show(String.format(sessionHelper.getLabel("Remove pivot column '%s'?"), bc.getEngName()), 
    						new String[] {sessionHelper.getBtLabel("OK"), sessionHelper.getBtLabel("Cancel")}, new ZkBiEventListener() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
							if (btn.getName().equals(sessionHelper.getBtLabel("OK"))) {
								Vector<BiColumn> xv = result.getListColumns();
								Listhead lh = listbox.getListhead();
								for(int i=0;i<xv.size();i++) {
									BiColumn bc = xv.get(i);
									Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(i+1));
									if(lhdr != null && bc.getLabel().equals(id)) {
										lhdr.setVisible(true);
										//onListColumnVisibleChanged(result);
										updatePivotColumns(result, MapUtil.ofPairs(Pair.of(id, false)));
										break;
									}
								}
							}
						}
    				});
    			}
    		}
    	}
    }

    void setAggregateAndPivotHeaders(final BiResult result,List<String> p_headers,AggregateOrPivotHeader p_aopHeader) {
    	if (useMobileList(sessionHelper.isMobile(),result))
    		return;
    	ArrayList<APLabel> pivotHeaders = new ArrayList<APLabel>();
    	Map<String, List<String>> aopAuxHeaderMap = new HashMap<String, List<String>>();
  		Listhead listhead = (Listhead) listbox.query("Listhead");
  		Auxhead auxhead = null;
  		for(int i = 0;i<auxHeaderCount;i++) {
  			auxhead = (Auxhead) listbox.getFellowIfAny("PivotAuxHead_"+i, true);
  			auxhead.setVisible(false);
  		}
    	if(p_headers != null) {
    		if(p_aopHeader != null && p_aopHeader.getAuxHeaderCount() > 0) {
    			for(int i=0;i<p_aopHeader.getAuxHeaderCount();i++) {
    				if(i >= auxHeaderCount) {
    					auxhead = new Auxhead();
		    			auxhead.setId("PivotAuxHead_"+i);
    					//auxhead.setZclass("z-listbox-header"); 
    					auxhead.setZclass("z-listheader");
    					/*
    					Auxheader ahdr = new Auxheader();
    					ahdr.setZclass("z-listbox-header");
    					ahdr.setColspan(baseHeaderCount-aggregateOffset);
    					ahdr.appendChild(new Label(""));
		    			auxhead.appendChild(ahdr);
		    			*/
    					listbox.insertBefore(auxhead,listhead);
    					auxHeaderCount++;
    				} else {
    					auxhead = (Auxhead) listbox.getFellowIfAny("PivotAuxHead_"+i, true);
    					auxhead.setVisible(true);
    					List <Component> ahdrs = auxhead.getChildren();
    					int n = ahdrs.size();
//    					if(n > 1) {
//    						for(int j = n-1;j>0;j--) {
    						for(int j = n-1;j>=0;j--) {
    							Auxheader lh = (Auxheader) ahdrs.get(j);
    							auxhead.removeChild(lh);
    						}
//    					}
    				}
    			}
    			/*
    		if(auxhead  == null) {
    			auxhead = new Auxhead();
    			auxhead.setId("PivotAuxHead");
    			auxhead.setZclass("z-listbox-header");
    			Auxheader ahdr = new Auxheader();
    			ahdr.setZclass("z-listbox-header");
    			ahdr.setColspan(baseHeaderCount-aggregateOffset);
    			ahdr.appendChild(new Label("HAHA"));
    			auxhead.appendChild(ahdr);
    			listbox.insertBefore(auxhead,listhead);
    		} else {
    			auxhead.setVisible(true);
    			List <Component> ahdrs = auxhead.getChildren();
    			int n = ahdrs.size();
    			if(n > 1) {
    				for(int i = n-1;i>0;i--) {
    					Auxheader lh = (Auxheader) ahdrs.get(i);
    					auxhead.removeChild(lh);
    				}
    			}
    		}
    			*/
    		}
    		
    		if(p_aopHeader != null) {
    			int cnt = p_aopHeader.getAggregateOrPivotList().size();
    			for(int i=0;i<cnt;i++) {
   			    	pivotHeaders.add(p_aopHeader.getHeader(i));
   			    	aopAuxHeaderMap.put(p_aopHeader.getAggregateOrPivotList().get(i), new ArrayList<String>());
    			}
    		} else {
    			for(String hs : p_headers) {
   			    	pivotHeaders.add(APLabel.of(hs.toString()));
    			/*
    			if(p_aopHeader != null && p_aopHeader.getAuxHeaderCount() > 0) {
    			Auxheader ahdr = new Auxheader();
    			ahdr.setZclass("z-listbox-header");
    			ahdr.setStyle("background-color:red;");
    			ahdr.setColspan(1);
    			ahdr.appendChild(new Label("Col "));
    			auxhead.appendChild(ahdr);
    			}
    			*/
    			}
    		}
    		if(p_aopHeader != null ) {
    			int auxCnt = p_aopHeader.getAuxHeaderCount();
    			int hdrCnt = p_aopHeader.getAggregateOrPivotList().size();
    			for(int i=0;i<auxCnt;i++) {
    				auxhead = (Auxhead) listbox.getFellow("PivotAuxHead_"+i, true);
//    				if(allowDragDropHeader) auxhead.setDraggable("pivotColumn");
//    				auxhead.setDroppable("true");
    					Auxheader ahdr = new Auxheader();
    					//ahdr.setZclass("z-listbox-header");
    					ahdr.setZclass("z-listheader");
    					ahdr.setColspan(baseHeaderCount-aggregateOffset);
    					ahdr.appendChild(new Label(""));
    					if(sessionHelper.getAllowUpdatePivot() && allowDragDropHeader) ahdr.setDraggable("false");
    					ZkUtil.appendStyle(ahdr, "text-align:right;");
		    			auxhead.appendChild(ahdr);
		    			
    				for(int j=0;j<hdrCnt;j++) {
    					APLabel ahLabel = p_aopHeader.getAuxHeader(i, j);
    					String ah = ahLabel.toString();
    					String pivotName = ahLabel.pivotName(result);
    					String aop = p_aopHeader.getAggregateOrPivotList().get(j);
    					String aggName = p_aopHeader.getAggregate(j).getName(result);
    					aopAuxHeaderMap.get(aop).add(ah);
    					int span = 0;
    					for(int k=j+1;k<hdrCnt;k++) {
    						if(ah.equals(p_aopHeader.getAuxHeader(i, k).toString())) {
    							span++;
    							aop = p_aopHeader.getAggregateOrPivotList().get(k);
    							aopAuxHeaderMap.get(aop).add(ah);
    						} 
    						else 
    							break;
    					}
//    					Auxheader ahdr = new Auxheader();
    					ahdr = new Auxheader();
    					//ahdr.setZclass("z-listbox-header");
    					ahdr.setZclass("z-listheader");
    					ahdr.setAlign("center");
    					//ahdr.setStyle("background-color:red;");  //andrew220615 remove pivot header color
    					//ahdr.setSclass("zkbi-pivot-header");
    					ahdr.setSclass(ahLabel.isPivot() ? "zkbi-pivot-header" : "zkbi-aggregate-header");
    					if (ahLabel.isPivot() && !sessionHelper.getAllowUpdatePivot())
   							ZkUtil.appendSclass(ahdr, "zkbi-no-delete");
    					ahdr.setColspan(span+1);
    					//ahdr.appendChild(new Label(ah));
    					//ahdr.appendChild(StringUtils.isNotBlank(ah) ? new Label(ah) : new Label(" "));  //andrew220615 fix blank pivot header
    					if (StringUtils.isBlank(ah)) {
   							ZkUtil.appendSclass(ahdr, "zkbi-blank");
    						ahdr.appendChild(new Label(" "));
    					}
    					else {
    						ahdr.setTooltiptext(ahLabel.isPivot() ? pivotName + " (" + sessionHelper.getLabel("Pivot Column") + ")" : aggName + " (" + sessionHelper.getLabel("Aggregate Column") + ")");
    						ahdr.appendChild(new Label(ah));
    						if (sessionHelper.getAllowUpdatePivot() && ahLabel.isPivot())
    							ahdr.addEventListener(Events.ON_CLICK, new HeaderRemoveListener(result));
    					}
    					ahdr.setAttribute("pivot_header", ah);
    					ahdr.setAttribute("pivot_id", ahLabel.pivotId());
//    					ahdr.setDraggable("pivotColumn");
    					if(sessionHelper.getAllowUpdatePivot() && allowDragDropHeader) {
    						ahdr.setDroppable("viewColumn");
    						ahdr.addEventListener(Events.ON_DROP, 
    								new HeaderDropListener(result)
//    							new ZkBiEventListener(0){ 
//    								public void onZkBiEvent(Event arg0) throws Exception {
//    									UniLog.log("onDrop event:"  + arg0.getName() +"," + arg0.getTarget().getId() + " dragged " +
//    										((DropEvent) arg0).getDragged().getId()
//    									);
//    									headerDragged(result, ((DropEvent) arg0).getDragged(), arg0.getTarget());
//    									arg0.stopPropagation();
//    								}
//    							}
    						);
    					}
    					auxhead.appendChild(ahdr);
    					j += span;
    				}
    			}
    		}
    		if(p_aopHeader != null && p_aopHeader.getAuxHeaderCount() > 0) {
    			/*
    		if(aggregateOffset > 0) {
    			Auxheader ahdr = new Auxheader();
    			ahdr.setZclass("z-listbox-header");
    			ahdr.setColspan(aggregateOffset);
    			auxhead.appendChild(ahdr);
    		}
    			*/
    		}
    	}
   		List <Component> lhdrs = listhead.getChildren();
   		int n = lhdrs.size();
   		for(int i = n-1;i>=baseHeaderCount;i--) {
   			Listheader lh = (Listheader) lhdrs.get(i-aggregateOffset);
   			listhead.removeChild(lh);
   		}
   		/*
   		for(int i = n-1;i>=baseHeaderCount;i--) {
   			Listheader lh = (Listheader) lhdrs.get(i);
   			String lhid = lh.getId();
   			String ph = (String) lh.getAttribute("pivot_header");
   			if (ph != null && pivotHeaders.contains(ph))
   				pivotHeaders.remove(ph);
   			else
   				listhead.removeChild(lh);
   		}
   		*/

    	Vector<BiColumn> listColumns = result.getListColumns();
    	UniLog.log1("listColumns size:%d, pivotHeaders size:%d", listColumns.size(), pivotHeaders.size());
    	int i = listColumns.size();
    	int ncols = i;
   		for (int j = 0; j < pivotHeaders.size(); j++) {
   			final APLabel phLabel = pivotHeaders.get(j);
   			String ph = phLabel.toString();
   			String pivotName = phLabel.pivotName(result);
   			String eggName = "";
   			Listheader browser_listheader = new Listheader();
    		if(p_aopHeader != null) {
    			eggName = p_aopHeader.getAggregate(j).getName(result);
    			
    			//andrew221201 translate aggregate header (experimental)
    			if (AggregateOrPivot.enableTranslateAggHeader) {
    				BiColumn aggCol = p_aopHeader.getAggregate(j).getBiColumn(result);
    				if (aggCol != null) {
    					JxZkBiBase.addContextMenu(sessionHelper,browser_listheader,MapUtil.of("changeLabel", MapUtil.of("key",aggCol.getCellFullName(),"defaultValue",aggCol.getEngName())));
    				}
    			}
    		}
   			//browser_listheader.setStyle("background-color:red;");  //andrew220615 remove pivot header color
   			//browser_listheader.setSclass("zkbi-pivot-header");
   			browser_listheader.setSclass(phLabel.isPivot() ? "zkbi-pivot-header" : "zkbi-aggregate-header");
			if (phLabel.isPivot() && !sessionHelper.getAllowUpdatePivot())
   				ZkUtil.appendSclass(browser_listheader, "zkbi-no-delete");
   			browser_listheader.setTooltiptext(phLabel.isPivot() ? pivotName + " (" + sessionHelper.getLabel("Pivot Column") + ")" : eggName + " (" + sessionHelper.getLabel("Aggregate Column") + ")");
   			browser_listheader.setAlign("center");
   			//browser_listheader.setLabel(ph);
   			browser_listheader.setLabel(StringUtils.isBlank(ph) ? " " : ph); //230310 fix blank label not wrong spacing
   			//browser_listheader.setParent(listhead);
   			if(aggregateOffset <= 0) listhead.appendChild(browser_listheader); else listhead.insertBefore(browser_listheader, lhdrs.get(i-aggregateOffset));
   			//browser_listheader.setId(ph);
   			final int colIdx = ++i;
   			browser_listheader.setId("browser_listheader_" + colIdx);
   			browser_listheader.setAttribute("pivot_header", ph);
   			if (p_aopHeader != null) {
				String aop = p_aopHeader.getAggregateOrPivotList().get(j);
				browser_listheader.setAttribute("aggregate_or_pivot", aop);
				browser_listheader.setAttribute("pivot_id", phLabel.pivotId());
				browser_listheader.setAttribute("pivot_auxheader_list", aopAuxHeaderMap.get(aop).toArray(new String[0]));
   			}
   			browser_listheader.setHflex("min");

			setListHeaderSortDirection(browser_listheader, "natural");
   			browser_listheader.setSort("auto");
   			browser_listheader.setSortAscending(new ListitemComparator());
   			browser_listheader.addEventListener("onSort", new ZkBiEventListener(0) {
   				public void onZkBiEvent(Event arg0) throws Exception {
   					UniLog.log("onSort event:"  + arg0.getName() +"," + arg0.getTarget().getId());
   					try {
   						if (sessionHelper.getAllowUpdatePivot() && phLabel.isPivot()) {
   							if (arg0 instanceof ColumnSortEvent)
   								sortSingle(result, masterWin, colIdx, !((SortEvent)arg0).isAscending());
   							else
   								Events.echoEvent("onRemoveHeader", arg0.getTarget(), null);
   						}
   						else
 							sortSingle(result, masterWin, colIdx, !((SortEvent)arg0).isAscending());
   					}
   					catch(Exception ex){
   						UniLog.log(ex);
   					}
   					arg0.stopPropagation();
   				}
   			});
   			browser_listheader.addEventListener("onMultiSort", new ZkBiEventListener(0) {
   				public void onZkBiEvent(Event arg0) throws Exception {
   					UniLog.log("onMultiSort event:"  + arg0.getName() +"," + arg0.getTarget().getId());
   					try{
   						if (arg0 instanceof SortEvent)
   							sortMulti(result, masterWin, colIdx, !((SortEvent)arg0).isAscending());
   					}
   					catch(Exception ex){
   						UniLog.log(ex);
   					}
   					arg0.stopPropagation();
   				}
   			});
    		if(sessionHelper.getAllowUpdatePivot() && allowDragDropHeader) {
    				browser_listheader.setDroppable("viewColumn");
    				browser_listheader.addEventListener(Events.ON_DROP, 
    						new HeaderDropListener(result)
    				);
    		}
    		if (sessionHelper.getAllowUpdatePivot() && phLabel.isPivot())
				browser_listheader.addEventListener("onRemoveHeader", new HeaderRemoveListener(result));
   			
   			/* this don't work , don't know why */
   			if(!isAggregateVisible(result,p_aopHeader,j)) {
   				browser_listheader.setVisible(false);
   			}
   		}
   		if(p_aopHeader != null ) {
   			Listfoot lf = listbox.getListfoot();
	    	if(lf == null) {
	    		lf = new Listfoot();
	    		for(int j=0;j<baseHeaderCount-aggregateOffset;j++) {
	    			Listfooter lftr = new Listfooter();
//	    			lftr.setZclass("z-listheader");
//	    			lftr.appendChild(new Label("Subtotal:"));
	    			lf.appendChild(lftr);
	    		}
	    	} 
	    	{
    			List <Component> fdrs = lf.getChildren();
    			int nn = fdrs.size();
    			for(int j = nn-1;j>=baseHeaderCount-aggregateOffset;j--) {
    				lf.removeChild(fdrs.get(j));
    			}
	   			Object[] vals = result.getAggregateSubtotal();
	   			if(vals != null) {
    			for(int j=0;j<vals.length;j++) {
	    			Listfooter lftr = new Listfooter();
	    			lftr.setZclass("z-listheader");
	    			lftr.setSclass("zkbi-aggregate-footer");
	    			try {
	    				//lftr.setTooltiptext("Sum of " + pivotHeaders.get(j) + (p_aopHeader.isSubTotalColumn(j) ? " Subtotal" : ""));
    					lftr.setTooltiptext(String.format(sessionHelper.getTtLabel("Sum of %s" + (p_aopHeader.isSubTotalColumn(j) ? " Subtotal" : "")), pivotHeaders.get(j)));
	    			}
	    			catch(Exception ex) { }
//	    			lftr.appendChild(new Label("Subtotal:"));
	    			lf.appendChild(lftr);
	    			{
	    				if(vals[j] != null) {
	    					if(vals[j] instanceof Double) {
	    						DecimalFormat df = new DecimalFormat("##,###,###,##0.00");
								if (result.getAggregateOrPivotHeader() != null) {
    				 				AggregateRec aggRec = result.getAggregateOrPivotHeader().getAggregate(j);
 						    		String fmt = aggRec.getFormat(result);
 						    		if (fmt != null)
 						    			df = new DecimalFormat(fmt);
 						    	}
	    						double d;
	  							d = (Double) vals[j];
	    						if(Double.isInfinite(d)) ;
	    						else if(Double.isNaN(d)) ;
	    							else {
	    								Div dv = new Div();
	    								ZkUtil.appendStyle(dv, "text-align:right;");
	    								Label lb = new Label(df.format(d));
	    								dv.appendChild(lb);
	    								lftr.appendChild(dv);
	    						}
//	    						lc = new Listcell(df.format(vals[i]));
	    					} else {
	    					}
	    				} else{
	    				}
	    			}
	   			} 
    			}
	    		listbox.appendChild(lf);
	    	}
	    	
   		}
//    	if(!isMobile()) {
//    			Listheader lastHeader = new Listheader();
//    			lastHeader.setId("__lastHeader");
//    			lastHeader.setHflex("1");
//    			lastHeader.setParent(listhead);
//    	}
    }
    
//    protected void computeAggregateAndPivot(BiResult p_result,List<String> groupColumns, AggregateOrPivot.AGGREGATES p_agg,String agField,String pivotColumn) throws Exception {
//
//        clearSearch(p_result);
//		listModelList.clear();
//        p_result.computeAggregateDataSet(groupColumns, p_agg,agField,pivotColumn);
//        p_result.sort();
//        setAggregateAndPivotHeaders(p_result,p_result.getAggregateOrPivotList());
//        listModelList.addAll(p_result.getResultStat());
//  		lastSelectIdx = -1;
//  		setSelectIdx(-1,null);
//    }
    protected void computeAggregateAndPivot(BiResult p_result,AggregateOrPivot p_aop) throws Exception {

        clearSearch(p_result);
		if(listModelList != null) listModelList.clear(); 
		try {
			p_result.computeAggregateDataSet(p_aop);
		} catch (Exception ex) {
    		Messagebox.show(sessionHelper.getLabel("Regen Aggregate Error"));
			UniLog.log(ex);
			return;
		}
        //p_result.sort();

        setAggregateAndPivotHeaders(p_result,p_result.getAggregateOrPivotList(),p_result.getAggregateOrPivotHeader());

		String curPreset = (String)conditionPresetListbox.getSelectedItem().getValue();
		MultiSortMap sortMap = curPreset != null ? new MultiSortMap(curPreset,p_result) : null;
		setupListboxSort(p_result, null, sortMap, true);

        if(listModelList != null) listModelList.addAll(p_result.getResultStat());
  		lastSelectIdx = -1;
  		setSelectIdx(-1,null);
    }

	protected void afterActionCallback(BiResult p_result, ReturnMsg rtn1) {
		
	}
    
    public Button addBatchBiActionHandler(final BiResult p_result,boolean p_BatchMode ,int p_AccessControl, String p_AccesKey,String p_id,String p_label,String p_icon,final BiActionHandler p_handler) {
		Button btn;
    	if(masterWin.hasFellow(p_id)) {
    		btn = (Button) masterWin.getFellow(p_id);
    	} 
    	else {	
	        btn = new ZkBiButton();
	        btn.setLabel(p_label);
	        btn.setId(p_id);
	        abHelper.addButton(btn, p_icon);
    	} 
        btn.addEventListener("onClick",
        new ZkBiEventListener() {
           	public void onZkBiEvent(Event event) throws Exception {	
           			if(p_handler == null) return;
             		final java.util.Set selection = listModelList.getSelection();
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							sessionHelper.getLabel("Please Select Items To Proceed"),
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
        			ReturnMsg rtn = p_handler.beforeAction(p_result,selection.size());
        			if(rtn != null && !rtn.getStatus()) {
      					Messagebox.show(
   							rtn.getMsg(),
   							sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
               		if (p_handler.isUseAsync()) {
						Map<String, Object> m = new HashMap<String, Object>();
               			showProgressPanel(true, (ev) -> {
               				m.put("requestStop", true);
               			});
               			setProgressPanelProgress(String.format("Load Record: %d/%d", 0, selection.size()), 0);
               			Iterator<?> it = btn.getEventListeners("onBiAction").iterator();
               			while (it.hasNext())
               				it.remove();
               			Iterator<?> it1 = selection.iterator();
               			m.put("selectionIdx", -1);
						m.put("startTime", System.currentTimeMillis());
						btn.addEventListener("onBiAction", (ev) -> {
							UniLog.log1("event:%s, data:%s", ev, ev.getData());
							while (it1.hasNext()) {
								if (m.containsKey("requestStop"))
									return;
								Object o = it1.next();
								m.put("selectionIdx", (int)m.get("selectionIdx") + 1);
								int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
								p_result.loadOneRecV(idx);
								ReturnMsg rtn1 = p_handler.processAction(p_result,idx);
								UniLog.log1("Load Record:%d,%d", m.get("selectionIdx"), selection.size());
								setProgressPanelProgress(String.format("Load Record: %d/%d", (int)m.get("selectionIdx") + 1, selection.size()), ((int)m.get("selectionIdx") + 1) * 100 / selection.size());
								if(rtn1 != null && !rtn1.getStatus()) {
									hideProgressPanel();
									Messagebox.show(rtn1.getMsg(), sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
									return;
								}
								long currentTime = System.currentTimeMillis();
								if (currentTime - (long)m.get("startTime") > 5000) {
									Events.echoEvent("onBiAction", btn, null);
									m.put("startTime", currentTime);
									return;
								}
							}
							p_handler.afterActionAsync((rtn1) -> {
								if (rtn1 != null && !rtn1.getStatus())
			          				Messagebox.show(rtn1.getMsg(), sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
								p_handler.afterActionCallback(p_result,rtn1);
							});
						});
						Events.sendEvent("onBiAction", btn, null);
               		} else {
         				int itemCnt=0;
               			for(Iterator it=selection.iterator();it.hasNext();) {
	            				Object o = it.next();
	            				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
	            				p_result.loadOneRecV(idx);
	            				rtn = p_handler.processAction(p_result,idx);
	            				if(rtn != null && !rtn.getStatus()) {
	            					Messagebox.show(
	            							rtn.getMsg(),
	            					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	            					return;
	            				}
	            				itemCnt++;
	               		}	
               			rtn = p_handler.afterAction(p_result);
               			if(rtn != null && !rtn.getStatus()) {
      						Messagebox.show(rtn.getMsg(), sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
   					    	return;
  			    		}
               		}
           		};
           	}
        );
        if(p_BatchMode) setupBatchModeButton(btn);
        return btn;
    }
	@Override
	public String getExtraInfo() {
		// TODO Auto-generated method stub
		return zkbiExtraInfo;
	}
	public void setExtraInfo(String p_info) {
		// TODO Auto-generated method stub
		zkbiExtraInfo = p_info;
	}
	
	/*protected void onListColumnVisibleChanged(BiResult p_result) throws Exception {
		
	}*/
	protected void updatePivotColumns(BiResult result, Map<String, Boolean> updateList) throws Exception {
	}
    protected void onSelectionChanged(BiResult p_result,MultiSortMap sortMap,Component comp) throws Exception {
       		refresh(p_result,comp,sortMap,false);
    } 
	
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		return(ReturnMsg.defaultOk);
	}
	
	
	/***
	 * 
	 * @param listboxHeight unit in percentage. range 50-100
	 */
	public final void adjListboxHeight(int p_listboxHeight) {
		if (listbox == null) {
			UniLog.log1("listbox is null");
			return;
		}
		if (isWidget()) {
			if (StringUtils.isNotBlank(listboxHeightSclass))
				ZkUtil.removeSclass(listbox, listboxHeightSclass);
			listboxHeightSclass = "zkbi-widget-height";
			ZkUtil.addSclass(listbox, listboxHeightSclass);
			return;
		}

		listboxPct = p_listboxHeight;
		listboxPct = ((int)(listboxPct/5))*5; //round to 5
		
		if (listboxPct < 40 || listboxPct > 100) {
			UniLog.log1("invalid listboxHeight %d %d", p_listboxHeight, listboxPct);
		}
		if (listboxPct < 40) {
			listboxPct = 40;
		}
		if (listboxPct > 100) {
			listboxPct = 100;
		}
		
		
		//remove old height sclass if any
		if (StringUtils.isNotBlank(listboxHeightSclass)) {
			ZkUtil.removeSclass(listbox, listboxHeightSclass);
		}
		
		//new new height sclass
		listboxHeightSclass = String.format("zkbi-%dpct-height",listboxPct);
		ZkUtil.addSclass(listbox, listboxHeightSclass);
	}

    private void setListHeaderSortDirection(final Listheader listheader, final String direction) {
    	if (isWidget()) {
			//xjcheng220812: fix sort direction icon bug in widget status
    		if (!listheader.getEventListeners("onSetSortDirection").iterator().hasNext()) {
    			listheader.addEventListener("onSetSortDirection", new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log1("setSortDirection %s, %s", listheader, event.getData());
						listheader.setSortDirection((String)event.getData());
					}
    			});
    		}
    		Events.echoEvent("onSetSortDirection", listheader, direction);
    	}
    	else
			listheader.setSortDirection(direction);
		/*if (isWidget()) {
			//xjcheng220808: fix sort direction icon bug in widget status
			StringBuilder sb = new StringBuilder(String.format("var $sorticon = jq('#%s').find('.z-listheader-sorticon');", listheader.getUuid()));
			if (StringUtils.equals(direction, "natural"))
				sb.append("$sorticon.find(\"i[id$='-sort-icon']\").removeClass('z-icon-caret-down z-icon-caret-up');");
			else if (StringUtils.equals(direction, "ascending"))
				sb.append("$sorticon.find(\"i[id$='-sort-icon']\").removeClass('z-icon-caret-down').addClass('z-icon-caret-up');");
			else if (StringUtils.equals(direction, "descending"))
				sb.append("$sorticon.find(\"i[id$='-sort-icon']\").removeClass('z-icon-caret-up').addClass('z-icon-caret-down');");
			ZkUtil.js(sb.toString());
		}*/
    }
    
    /***
     * experimental to cleanup a page
     */
    private void cleanup(BiResult br) {
    	try {
    		UniLog.log1("called");
    		br.unmapColumns();
    		br.clear();
    		br.clearCondition();
    		br.close();
    		listModelList.clear();
    		masterWin.detach();
    		System.gc();
    	}
    	catch(Exception ex) {
    		ex.printStackTrace();
    	}
    }
    /***
     * experimental to cleanup current page
     * @param result
     */
    private void setupCleanupButton(final BiResult result) {
    	if (!sessionHelper.isAdminUser()) return;
    	if (!sessionHelper.getAllowDesktopCleanup()) return;
    	UniLog.log1("called");
    	final Button btCleanup = new ZkBiButton();
    	btCleanup.setLabel(sessionHelper.getBtLabel("Page Cleanup(" + getDesktopId() +")"));
    	btCleanup.setAttribute("tlkey", "bt_master_cleanup");
    	btCleanup.setId("btCleanup");
    	btCleanup.setIconSclass("z-icon-trash");
    	btCleanup.setTooltiptext(sessionHelper.getTtLabel("Cleanup current page"));
    	btCleanup.addEventListener("onClick", new ZkBiEventListener() {
    		public void onZkBiEvent(Event event) throws Exception {
    			cleanup(result);
    		}	
    	});
    	abHelper.addButton(btCleanup, "fa-trash");
    }
    
    private boolean isIncludeEmbedSearch() {
    	return sessionHelper.getAllowEmbedSearch() && StringUtils.equalsAnyIgnoreCase(ZkUtil.getURLParamFromComp(rootComp, "embedsearch"), "Y", "TRUE", "AUTO");
    }
    /*
     * Embed advanced search to main list
     * */
    private void setupEmbedSearch(String preset, final BiResult result) {
    	UniLog.log1("setupEmbedSearch preset:%s,result:%s", preset, result);
    	if (zkbiListTop == null || result == null || !sessionHelper.getAllowEmbedSearch() || isWidget()) return;
    	final String embedSearchParam = ZkUtil.getURLParamFromComp(rootComp, "embedsearch");
    	if (!StringUtils.equalsAnyIgnoreCase(embedSearchParam, "Y","TRUE","AUTO")) return;
    	if (!ZkUtil.isRealVisible(divAdvSearchG2)) return;

    	final int defaultListboxPct = 75;
   		Groupbox gbEmbed;
   		Div divCondition;
   		Button btSearch, btReset;
    	if (!masterWin.hasFellow("divEmbedSearch")) {
    		zkbiEmbedSearchDiv = new Div();
    		gbEmbed = new Groupbox();
    		zkbiEmbedSearchDiv.setId("divEmbedSearch");
    		zkbiListTop.getParent().insertBefore(zkbiEmbedSearchDiv, zkbiListTop);
    		zkbiEmbedSearchDiv.appendChild(gbEmbed);
    		zkbiEmbedSearchDiv.setStyle("background:white;padding:5px");
    		gbEmbed.setTitle("Advanced Search");
    		gbEmbed.setStyle("max-width:800px;padding-top:5px");
    		divCondition = new Div();
    		divCondition.setId("divEmbedSearchCondition");
    		divCondition.setStyle("margin-bottom:10px");
    		gbEmbed.appendChild(divCondition);
    		Hlayout hlButtons = new Hlayout();
    		gbEmbed.appendChild(hlButtons);
    		btSearch = new Button(sessionHelper.getBtLabel("Search"));
    		btSearch.setIconSclass("z-icon-search");
    		btSearch.setTooltiptext(
    			sessionHelper.getLabel("Search by condition") + "\n" +
    			sessionHelper.getLabel("Refresh search results based on query conditions")
 			);
    		btSearch.setVisible(!StringUtils.equalsIgnoreCase(embedSearchParam, "AUTO"));
    		zkbiEmbedSearchDiv.setAttribute("btSearch", btSearch);
    		btReset = new Button(sessionHelper.getBtLabel("Reset"));
    		//btReset.setIconSclass("z-icon-eraser");
    		btReset.setIconSclass("z-icon-undo");
    		//btReset.setTooltiptext("Reset all condition");
    		btReset.setTooltiptext(sessionHelper.getLabel("Reset to default"));
    		hlButtons.appendChild(btSearch);
    		hlButtons.appendChild(btReset);
    		final Groupbox gbEmbed1 = gbEmbed;
    		final Div divCondition1 = divCondition;
    		btSearch.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiAdvSearch advSearch = (ZkBiAdvSearch)zkbiEmbedSearchDiv.getAttribute("advSearchObject");
					String preset = (String)zkbiEmbedSearchDiv.getAttribute("preset");
					ConditionFieldMap cfm = mConditionPresets.getFieldMap(preset);
					String oldCondition = cfm.getCustomCondition();
					String newCondition = advSearch.getCustomParam();
					UniLog.log1("event:%s, newCondition:%s, oldCondition:%s", event, newCondition, oldCondition);

					inputFieldsList.customCondition = newCondition;
					biBaseRefresh(result);

					divAdvSearchG2Indicator.setVisible(!StringUtils.equals(oldCondition, newCondition));
				}
    		});
    		btReset.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					String preset = (String)zkbiEmbedSearchDiv.getAttribute("preset");
					ConditionFieldMap cfm = mConditionPresets.getFieldMap(preset);
					String oldCondition = cfm.getCustomCondition();

					ZkBiAdvSearch advSearch = (ZkBiAdvSearch)zkbiEmbedSearchDiv.getAttribute("advSearchObject");
					advSearch.restoreEmbedConditionBlocks(gbEmbed1, divCondition1, oldCondition);

					inputFieldsList.customCondition = oldCondition;
					biBaseRefresh(result);

					divAdvSearchG2Indicator.setVisible(false);
				}
    		});
    		if (!sessionHelper.isMobile()) {
    			//The main list height need to adjust according to the embed form.
    			zkbiEmbedSearchDiv.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>() {
					@Override
					public void onZkBiEvent(AfterSizeEvent event) throws Exception {
						UniLog.log1("event:%s, height:%d, desktopheight:%d", event, event.getHeight(), sessionHelper.getDesktopHeight());
						event.getTarget().setAttribute("oldHeight", event.getHeight());
						if (event.getHeight() > 0 && sessionHelper.getDesktopHeight() > 0) {
							int lbpct = defaultListboxPct;
							Integer oldListboxPct = (Integer)event.getTarget().getAttribute("oldListboxPct");
							if (oldListboxPct != null && oldListboxPct > 0)
								lbpct = oldListboxPct;
							int p = (int)(((double)event.getHeight() / sessionHelper.getDesktopHeight()) * 100);
							UniLog.log1("p:%d", p);
							adjListboxHeight(lbpct - p);
						}
					}
    			});
    			zkbiEmbedSearchDiv.addEventListener("onCustomAfterSize", new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						UniLog.log1("onCustomAfterSize event:%s", event);
						if (ZkUtil.isRealVisible(event.getTarget()) && event.getData() instanceof AfterSizeEvent)
							Events.sendEvent(event.getTarget(), (Event)event.getData());
					}
    			});
    			zkbiEmbedSearchDiv.setAttribute("oldListboxPct", listboxPct);
    		}
    	}
    	else {
    		zkbiEmbedSearchDiv = (Div)masterWin.getFellow("divEmbedSearch");
    		gbEmbed = (Groupbox)zkbiEmbedSearchDiv.getFirstChild();
    		divCondition = (Div)gbEmbed.getFellow("divEmbedSearchCondition");
    	}
    	boolean b;
    	if (preset != null) {
    		ZkBiAdvSearch advSearch = getAdvSearchForSetupEmbedSearch(preset, result, embedSearchParam);
    		zkbiEmbedSearchDiv.setAttribute("advSearchObject", advSearch);
    		zkbiEmbedSearchDiv.setAttribute("preset", preset);
    		b = advSearch.restoreEmbedConditionBlocks(gbEmbed, divCondition, inputFieldsList.customCondition);
    	}
    	else
    		b = false;
   		zkbiEmbedSearchDiv.setVisible(b);
   		if (b)
   			zkbiEmbedSearchDiv.setAttribute("isVisibleInBrowseMode", b);
   		else
   			zkbiEmbedSearchDiv.removeAttribute("isVisibleInBrowseMode");
    	if (!sessionHelper.isMobile()) {
    		if (!b) {
    			zkbiEmbedSearchDiv.setAttribute("lastListboxPct", listboxPct);
    			Integer oldListboxPct = (Integer)zkbiEmbedSearchDiv.getAttribute("oldListboxPct");
    			adjListboxHeight((oldListboxPct != null && oldListboxPct > 0) ? oldListboxPct : defaultListboxPct);
    		}
    		else {
    			Integer lastListboxPct = (Integer)zkbiEmbedSearchDiv.getAttribute("lastListboxPct");
    			if (lastListboxPct != null && lastListboxPct > 0)
    				adjListboxHeight(lastListboxPct);
    		}
			if (isIncludeEmbedSearch() && zkbiEmbedSearchDiv.getAttribute("oldHeight") != null)
				Events.echoEvent("onCustomAfterSize", zkbiEmbedSearchDiv, new AfterSizeEvent(Events.ON_AFTER_SIZE, zkbiEmbedSearchDiv, 0, (Integer)zkbiEmbedSearchDiv.getAttribute("oldHeight")));
    	}
    }

    protected class ZkBiAdvSearchForG2 extends ZkBiAdvSearch {
    	public ZkBiAdvSearchForG2(BiResult result, Component comp) {
			super(result, comp, sessionHelper, inputFieldsList.customCondition, 
					mConditionPresets, (String)conditionPresetListbox.getSelectedItem().getValue(), presetid, isIncludeEmbedSearch(),
					new AdvSearchCallback(result, comp));
    	}
		@Override
		protected XulElement buildInputComp(BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
			return buildAdvSearchInputComp(this, result, bc, textFlag, multiPickSelectFlag);
		}
    }

    protected class ZkBiAdvSearchForSetupEmbedSearch extends ZkBiAdvSearch {
    	public ZkBiAdvSearchForSetupEmbedSearch(String preset, BiResult result, String embedSearchParam) {
	   		super(result, masterWin, sessionHelper, null, mConditionPresets, preset, presetid, true, new ZkBiAdvSearch.Callback() {
	   			private String lastCondition;
				@Override
				public void setPresetMapOrderbysAndHideCols(ConditionFieldMap cfm) {
				}
				@Override
				public void refreshUIForSavePreset(String errMsg, ZkBiAdvSearch advSearch, BiResult result) {
				}
				@Override
				public EventListener<ClickEvent> getClickDialogButtonEvent(ZkBiAdvSearch advSearch) {
					return null;
				}
				@Override
				public void customConditionChanged(String newCondition) {
					UniLog.log1("customConditionChanged newCondition:%s, lastCondition:%s, embedSearchParam:%s", newCondition, lastCondition, embedSearchParam);
					Button btSearch = (Button)zkbiEmbedSearchDiv.getAttribute("btSearch");
					if (StringUtils.equalsIgnoreCase(embedSearchParam, "AUTO") && !StringUtils.equals(newCondition, lastCondition)) {
						Events.echoEvent(Events.ON_CLICK, btSearch, null);
						lastCondition = newCondition;
					}
				}
				@Override
				public void customConditionReset(String newCondition) {
					lastCondition = newCondition;
				}
			});
    	}
		@Override
		protected XulElement buildInputComp(BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
			return buildAdvSearchInputComp(this, result, bc, textFlag, multiPickSelectFlag);
		}
    }

	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		return advSearch.buildInputComp(sessionHelper, advSearch.parentComp, result, bc, textFlag, multiPickSelectFlag);
	}

    protected ZkBiAdvSearch getAdvSearchForG2(BiResult result, Component comp) {
   		return new ZkBiAdvSearchForG2(result, comp);
    }

    protected ZkBiAdvSearch getAdvSearchForSetupEmbedSearch(String preset, BiResult result, String embedSearchParam) {
   		return new ZkBiAdvSearchForSetupEmbedSearch(preset, result, embedSearchParam);
    }

	protected void onAddPhoto(BiResult p_result,org.zkoss.image.Image p_image) {
	}
	
	protected void refreshButtonStatus(BiResult biresult) {
		if (btRecalUpd != null) {
			if (ExcelWorkSheetCache.cacheRequireUpdate(biresult.getView().getName())) {
				ZkUtil.addSclass(btRecalUpd, "zkbi-deletebutton");
				btRecalUpd.setTooltiptext("Formula column is out of date. Please recal it.");
			}
			else {
    			ZkUtil.removeSclass(btRecalUpd, "zkbi-deletebutton");
			}
		}
		/*
		//testing code for update button status
    	if(btRecalUpd != null) {
    		if (Math.random() < 0.5) {
    			ZkUtil.addSclass(btRecalUpd, "zkbi-deletebutton");
    		}
    		else {
    			ZkUtil.removeSclass(btRecalUpd, "zkbi-deletebutton");
    		}
    	}
    	*/
	}
	
	protected String getPageid() {
		return(pageid);
	}
	
	protected List<BiColumn> allowSelectAggregateList(BiResult result) {
		return(null);
	}
	
	public boolean isAggregateHidden(String p_aggName) {
		if(hideAggregates == null) return(false);
		return(hideAggregates.indexOf(p_aggName) >= 0);
	}

	protected void resetListHeader(BiResult result) {
		if (sessionHelper.isMobile())
			return;
		Listhead listhead = (Listhead) listbox.query("Listhead");
		setupListHeader(result,masterWin,defaultSortIdx, defaultSortDesc,listhead);
		setListheaderWidth(result, true);   //andrew20620 try to fix wrong column alignment
		listbox.invalidate();
	}
	
	protected void changedDisplayListColumn(BiResult result) {
	}
	
	private void setFrozenColumn(int p_col) {
		int fc;
   		if (p_col > 0) {
   			if(hasAUDColumn != null && hasAUDColumn) {
   				fc = p_col+1;
   			} else {
   				fc = p_col;
   			}
   			Frozen fz = listbox.getFrozen();
   			if(fz == null) {
   				listbox.appendChild(new Frozen(){{
  					this.setColumns(fc);
  					this.setStart(0);
   				}});
   				listbox.invalidate();
   			} else {
   				if(fz.getColumns() != fc) {
   					fz.setColumns(fc);
   					listbox.invalidate();
   				}
   			}
   		} else {
   			Frozen fz = listbox.getFrozen();
   			if(fz != null) {
   				listbox.removeChild(fz);
   				listbox.invalidate();
   			}
   		}	
	}
	public HashSet<BiColumn> getVisibleColumns(BiResult p_br) {
		return(null);
	}
	
	public boolean useMobileList(boolean p_isMobile,BiResult p_result) {
		return(p_isMobile && !p_result.getView().alwaysInListView());
	}

	public void showProgressPanel(boolean isShowCancelButton, EventListener<Event> cancelButtonListener) {
		if (!progressPanel.isVisible()) {
			progressMeter.setValue(0);
			progressMeter.invalidate();
			progressPanel.doModal();
		}
		progressCancel.setVisible(isShowCancelButton);
		if (cancelButtonListener != null) {
			EventListener<Event> listener = (EventListener<Event>)progressCancel.getAttribute("clickListener");
			if (listener != null)
				progressCancel.removeEventListener(Events.ON_CLICK, listener);
			progressCancel.addEventListener(Events.ON_CLICK, cancelButtonListener);
			progressCancel.setAttribute("clickListener", cancelButtonListener);
		}
	}

	public void hideProgressPanel() {
    	progressPanel.setVisible(false);
    	progressCancel.setVisible(true);
		EventListener<Event> listener = (EventListener<Event>)progressCancel.getAttribute("clickListener");
		if (listener != null)
			progressCancel.removeEventListener(Events.ON_CLICK, listener);
	}

	public void setProgressPanelProgress(String progressLabel, int progressValue) {
		if (progressPanel.isVisible()) {
			progressName.setValue(progressLabel);
			progressMeter.setValue(progressValue);
		}
	}
	
	String translateFromExcelHeader(String p_colhdr) {
		String colhdr;
		if(sessionHelper.getLHLang().equals("TCHN")) {
			colhdr = ChineseConvert.convertAuto2Bnew(p_colhdr);
			if(importExportMap != null) {
				for(Pair<String,String> p : importExportMap) {
					String th = ChineseConvert.convertAuto2Bnew(p.getRight());
					if(colhdr.equals(th)) {
						colhdr = ChineseConvert.convertAuto2Bnew(p.getLeft());
						break;
					}
				}
			}
		} else if(sessionHelper.getLHLang().equals("SCHN")) {
			colhdr = ChineseConvert.convertAuto2Gnew(p_colhdr);
			if(importExportMap != null) {
				for(Pair<String,String> p : importExportMap) {
					String th = ChineseConvert.convertAuto2Gnew(p.getRight());
					if(colhdr.equals(th)) {
						colhdr = ChineseConvert.convertAuto2Gnew(p.getLeft());
						break;
					}
				}
			}
		} else {
			colhdr=p_colhdr;
			if(importExportMap != null) {
				for(Pair<String,String> p : importExportMap) {
					String th = p.getRight();
					if(colhdr.equals(th)) {
						colhdr = p.getLeft();
						break;
					}
				}
			}
		}
		return(colhdr);
	}
	
	public void reload(BiResult p_result) {
		String preset = conditionPresetListbox.getSelectedItem().getValue();
		inputFieldsList.reset(preset);
		MultiSortMap sortMap = (MultiSortMap) mMultiSortMap.clone();
		try {
			onSelectionChanged(p_result,sortMap,masterWin);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}	
	
	public void resetBatchActionHandler(BiResult p_result) {
		if(bahHash != null) {
			for(String batchButtonId : bahHash.keySet()) {
				BiActionHandler bah = bahHash.get(batchButtonId);
				Button btn = (Button) queryBar.getFellowIfAny(batchButtonId, true);
				btn.setDisabled(bah.isDisabled(p_result, true));
			}
		}
	}

    protected class ImportWithReloadButtonEventListener extends ZkBiComposerBase.ImportButtonEventListener {
    	private String template, reloadDataButtonName;
		public ImportWithReloadButtonEventListener(BiResult result, String template, String reloadDataButtonName) {
			super(result);
			this.template = template;
			this.reloadDataButtonName = reloadDataButtonName;
		}
   		public void onZkBiEvent(Event event) throws Exception {
   			UniLog.log("Import button pressed");
   			ZkBiFileuploadDlg.get(template, new UploadEventListener());
   		}
		public class UploadEventListener extends ZkBiComposerBase.ImportButtonEventListener.UploadEventListener {
			private void markDeleteAllItems(boolean b) {
				for (Object o : listModelList){
					int listIdx = getListIdxByObj(listModelList,o);
					Object ts = o;
					if (ts instanceof TrStatFilter)
						ts = ((TrStatFilter)ts).getTrStatIdx();
					result.markDelete(ts, b);
					listModelList.set(listIdx, o);
				}
			}
			@Override
    		public void onZkBiEvent(final UploadEvent event) {
				UniLog.log1("event:%s, targetid:%s", event, event.getTarget().getId());
                org.zkoss.util.media.Media media = event.getMedia();
                if(media != null) {
                	if (StringUtils.equals(event.getTarget().getId(), reloadDataButtonName) && !listModelList.isEmpty()) {
                		ZkBiMsgbox.show(ZkBiMsgbox.Type.question, "確定要刪除所有記錄？", new String[] {sessionHelper.getBtLabel("Ok"), sessionHelper.getBtLabel("Cancel") }, new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event1) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event1.getTarget();
								if (btn.getIdx() == 0) {
									markDeleteAllItems(true);
									if (!saveChangeCheck(result)) {
										markDeleteAllItems(false);
										return;
									}
									if (saveChangeConfirm(result))
										UploadEventListener.super.onZkBiEvent(event);
								}
							}
                		});
                	} else
                		super.onZkBiEvent(event);
                } else
                	Messagebox.show(sessionHelper.getLabel("File Not Selected"));
  			}
		}
    }
    
    public Object getStateValue(String key) {
    	return(null);
    }
}