package com.uniinformation.zkbi;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Space;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.MessageboxDlg;
import org.zkoss.zul.impl.XulElement;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiField;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.jx.zk.ZkJxQueryInput.EventListenerCallback;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.ConditionPresets;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.utils.whereclpar.Expression;
import com.uniinformation.utils.whereclpar.Parser;
import com.uniinformation.utils.whereclpar.Variable;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiAdvSearch {
	private static final boolean showFieldConnector = false;
	private static final boolean showClearConBlock = false; //do not display clear button by default
	private static final boolean showDataFieldHeader = false;
	private static final int ALL_FIELD_MODE = 0;
	private static final int ANY_FIELD_MODE = 1;
	private static final String TIMEBOX_DATE = "1970/01/01";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:dd");
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:dd");

	protected BiResult result;
	private SessionHelper sessionHelper;
	protected Component parentComp;
	private boolean showPresetComp;
	private String refCustomParam;
	private ConditionPresets refConditionPresets;
	private String refSelectedPreset;
	private String viewId;
	
	private boolean isEmbedMode, isFromEmbedPage;

	private Callback callback;
	//private Map<String, ConditionFieldMapWrapper> conditionPresetMap = new LinkedHashMap<String, ConditionFieldMapWrapper>();
	private Map<String, ConditionFieldMapWrapper> conditionPresetMap = new TreeMap<String, ConditionFieldMapWrapper>();
	private Vlayout vlCon = new Vlayout();
	//private Vlayout vlCon1 = new Vlayout();
	private Component vlCon1 = new Vlayout();
	private Radiogroup rgConMode = new Radiogroup();
	private Button btnAddConBlock = new ZkBiButton();
	private Button btnClearConBlock = new ZkBiButton();
	private Spinner ibLimit = new Spinner();
	private String customParamValue = "";
	private Checkbox conditionPresetIsDefault = new Checkbox();
	private Listbox conditionPresetListbox = new Listbox();
	private Toolbarbutton btnCustomParam = new Toolbarbutton();
	private Toolbarbutton btnAddPreset = new Toolbarbutton();
	private Toolbarbutton btnDeletePreset = new Toolbarbutton();
	//private Map<Window, Set<Hlayout>> blockWinMap = new LinkedHashMap<Window, Set<Hlayout>>(); //key:condition block window, value:block Row Comps
	private Map<Component, List<Hlayout>> blockWinMap = new LinkedHashMap<Component, List<Hlayout>>(); //key:condition block window, value:block Row Comps
	private Window activeConditionBlockWin = null;

	private Map<String, BiResult> biResultMap = new HashMap<String, BiResult>();
	private Map<String, BiColumn> biColumnMap = new HashMap<String, BiColumn>();
	private Map<String, String> biColumnResultMap = new HashMap<String, String>();

	public static interface Callback {
		EventListener<Messagebox.ClickEvent> getClickDialogButtonEvent(ZkBiAdvSearch advSearch);
		//Preset functions
		void setPresetMapOrderbysAndHideCols(ConditionFieldMap cfm);
		void refreshUIForSavePreset(String errMsg, ZkBiAdvSearch advSearch,BiResult result);
		void customConditionChanged(String newCondition);
		void customConditionReset(String newCondition);
	}
	private static enum ConditionOp { 
		EQ(Condition.COMPARE_OP_EQ), NE(Condition.COMPARE_OP_NE), GT(Condition.COMPARE_OP_GT), LT(Condition.COMPARE_OP_LT), 
		GE(Condition.COMPARE_OP_GE), LE(Condition.COMPARE_OP_LE), IN_ITEMLIST(Condition.COMPARE_OP_IN_ITEMLIST), 
		NOTIN_ITEMLIST(Condition.COMPARE_OP_NOTIN_ITEMLIST), BETWEEN(Condition.COMPARE_OP_BETWEEN), NOT_BETWEEN(Condition.COMPARE_OP_NOT_BETWEEN),
		LK(Condition.COMPARE_OP_LK), NLK(Condition.COMPARE_OP_NLK), MA(Condition.COMPARE_OP_MA), NM(Condition.COMPARE_OP_NM),
		IS_NULL(Condition.COMPARE_OP_IS_NULL), IS_NOT_NULL(Condition.COMPARE_OP_IS_NOT_NULL),
		IS_BLANK(0), IS_NOT_BLANK(0), REGEXP(Condition.COMPARE_OP_REGEXP), NOT_REGEXP(Condition.COMPARE_OP_NOT_REGEXP);
		int id;
		ConditionOp(int id) {
			this.id = id;
		}
		static ConditionOp findConditionOp(int id) {
			for (ConditionOp op : ConditionOp.values()) {
				if (op.id == id)
					return op;
			}
			return null;
		}
	}
	private static class ConditionWrapper {
		Condition cond;
		List<ConditionWrapper> childs = new ArrayList<ConditionWrapper>();
		ConditionWrapper(Condition cond) {
			this.cond = cond;
		}
	}
	private static class ConditionFieldMapWrapper {
		//ConditionFieldMap map;
		String customCondition;
		boolean isCustom, isDefault, isDisplayRecCnt;
		String accessKeyForPublic;
		int recordLimit;
		boolean saved;
	}
	private Map<ConditionOp, Pair<String, String>> operatorMap;

	public ZkBiAdvSearch(BiResult result, Component parentComp, SessionHelper sh, String customParam, 
			ConditionPresets conditionPresets, String selectedPreset, String viewId,
			Callback callback) {
		this(result, parentComp, sh, customParam, conditionPresets, selectedPreset, viewId, false, callback);
	}
	/***
	 * @param result - BiResult
	 * @param parentComp - parent component
	 * @param sessionHelper
	 * @param customParam
	 * @param conditionPresets - optional. if conditionPresets is not null, showPresetComp is true
	 * @param selectedPreset - optional. required conditionPresets is not null
	 * @param viewid - optional. required conditionPresets is not null
	 * @param isFromEmbedPage
	 * @param callback - callback interface
	 */
	public ZkBiAdvSearch(BiResult result, Component parentComp, SessionHelper sh, String customParam, 
			ConditionPresets conditionPresets, String selectedPreset, String viewId, boolean isFromEmbedPage,
			Callback callback) {
		this.result = result;
		this.callback = callback;
		this.sessionHelper = sh;
		this.parentComp = parentComp;
		this.refCustomParam = customParam;
		this.refConditionPresets = conditionPresets;
		this.refSelectedPreset = selectedPreset;
		this.viewId = viewId;
		this.isFromEmbedPage = isFromEmbedPage;

		operatorMap = new LinkedHashMap<ConditionOp, Pair<String, String>>(){{
			put(ConditionOp.EQ, Pair.of(sh.getLabel("equal"), sh.getLabel("field value = keyword")));
			put(ConditionOp.NE, Pair.of(sh.getLabel("not equal"), sh.getLabel("field value <> keyword")));
			put(ConditionOp.GT, Pair.of(sh.getLabel("greater"), sh.getLabel("field value > keyword")));
			put(ConditionOp.LT, Pair.of(sh.getLabel("less"), sh.getLabel("field value < keyword")));
			put(ConditionOp.GE, Pair.of(sh.getLabel("greater or equal"), sh.getLabel("field value >= keyword")));
			put(ConditionOp.LE, Pair.of(sh.getLabel("less or equal"), sh.getLabel("field value <= keyword")));
			put(ConditionOp.IN_ITEMLIST, Pair.of(sh.getLabel("in"), sh.getLabel("field value in keyword1,keyword2,...")));
			put(ConditionOp.NOTIN_ITEMLIST, Pair.of(sh.getLabel("not in"), sh.getLabel("field value not in keyword1,keyword2,...")));
			put(ConditionOp.BETWEEN, Pair.of(sh.getLabel("between"), sh.getLabel("field value in between keyword1 and keyword2")));
			put(ConditionOp.NOT_BETWEEN, Pair.of(sh.getLabel("not between"), sh.getLabel("field value not between keyword1 and keyword2")));
			//put(Condition.COMPARE_OP_MA, Pair.of("matches", "field value matches keyword"));
			//put(Condition.COMPARE_OP_NM, Pair.of("not matches", "field value not matches keyword"));
			put(ConditionOp.LK, Pair.of(sh.getLabel("like"), sh.getLabel("field value like keyword")));
			put(ConditionOp.NLK, Pair.of(sh.getLabel("not like"), sh.getLabel("field value not like keyword")));
			//put(Condition.COMPARE_OP_IS_NULL, Pair.of("is null", "field value is null"));
			//put(Condition.COMPARE_OP_IS_NOT_NULL, Pair.of("is not null", "field value is not null"));
			put(ConditionOp.IS_BLANK, Pair.of(sh.getLabel("is blank"), sh.getLabel("field value is blank")));
			put(ConditionOp.IS_NOT_BLANK, Pair.of(sh.getLabel("is not blank"), sh.getLabel("field value is not blank")));
			put(ConditionOp.REGEXP, Pair.of(sh.getLabel("contains"), sh.getLabel("field value contains keyword")));
			put(ConditionOp.NOT_REGEXP, Pair.of(sh.getLabel("not contains"), sh.getLabel("field value not contains keyword")));
		}};

		if (refConditionPresets != null)
			showPresetComp = true;
		if (showPresetComp) {
			for (String preset : refConditionPresets.getPresets()) {
				ConditionFieldMap cfm = refConditionPresets.getFieldMap(preset);
				ConditionFieldMapWrapper cfmw = new ConditionFieldMapWrapper();
				//cfmw.map = cfm;
				cfmw.customCondition = cfm.getCustomCondition();
				cfmw.isCustom = cfm.isCustom();
				cfmw.accessKeyForPublic = cfm.getAccessKeyForPublic();
				cfmw.isDefault = cfm.isDefault();
				cfmw.isDisplayRecCnt = cfm.isDisplayRecCnt();
				cfmw.recordLimit = cfm.getRecordLimit();
				conditionPresetMap.put(preset, cfmw);
			}
		}
	}
	/***
	 * @param result - BiResult
	 * @param parentComp - parent component
	 * @param sessionHelper
	 * @param customParam
	 * @param callback - callback interface
	 */
	public ZkBiAdvSearch(BiResult result, Component parentComp, SessionHelper sessionHelper, String customParam, Callback callback) {
		this(result, parentComp, sessionHelper, customParam, null, null, null, callback);
	}
	public Window create() {
		final Window win = new Window();
		final Vlayout vl = new Vlayout();
		final Hlayout hlFC = new Hlayout();
		hlFC.setSclass("zkbi-advs-condition");
		final Tabbox tbFieldList = new Tabbox();
		final Groupbox groupConList = new Groupbox();
		groupConList.setMold("3d");
		groupConList.setClosable(false);
		final Vlayout vlCustParam = new Vlayout();
		win.appendChild(vl);
		vl.appendChild(vlCustParam);
		vl.appendChild(hlFC);
		hlFC.appendChild(tbFieldList);
		if(sessionHelper.isMobileDevice()) {
			tbFieldList.setVisible(false);
		}
		hlFC.appendChild(groupConList);
		win.setWidth("100%");
		vl.setHflex("1");
		hlFC.setHflex("1");
		tbFieldList.setHflex("1");
		groupConList.setHflex("3");
		vlCustParam.setHflex("1");
		
		//win.setHeight("calc(100% - 50px)");
		win.setHeight("calc(100% - 60px)");
		vl.setVflex("1");
		hlFC.setVflex("1");
		tbFieldList.setVflex("1");
		groupConList.setVflex("1");
			
		win.setStyle("padding:0px !important; user-select: none;");
		win.setClass("zkbi-advsearch-g2");
		if(sessionHelper.isMobileDevice()) {
		} else {
			groupConList.setTitle(sessionHelper.getLabel("Condition List"));
		}
		
		//field list
		Tabs ts = new Tabs();
		Tabpanels tps = new Tabpanels();
		ts.setParent(tbFieldList);
		tps.setParent(tbFieldList);
		Vector<BiResult> resultList = new Vector<BiResult>();
		resultList.add(result);
		if(result.getSubLinks()!=null) resultList.addAll(result.getSubLinks());
		for (final BiResult br : resultList) {
			new Tab(br.getView().getHeader()).setParent(ts);
			Tabpanel tp = new Tabpanel();
			tp.setParent(tps);
			Grid grid = new Grid();
			Rows rows = new Rows();
			grid.setParent(tp);
			grid.setVflex("1");
			if (showDataFieldHeader) {
				grid.appendChild(new Columns(){{ appendChild(new Column("Data Field")); }});
			}
			grid.appendChild(rows);
			biResultMap.put(br.getView().getName(), br);
			for (final BiColumn bc : br.getColumns()) {
				if(!bc.isNoQuery() && (!bc.isInvisible(br.getSessionHelper()) || bc.isInList(br.getSessionHelper()))) {
					if(br != result && (bc.getField() == null )) continue;
					if (biColumnMap.containsKey(bc.getLabel())) continue;
					Row row = new Row();
					row.setParent(rows);
					row.setAttribute("biResult", br);
					row.setAttribute("biColumn", bc);
					row.setDraggable("true");
					//final Label lblName = new Label(bc.getEngName());
					final Label lblName = new Label(getBiColumnTranslateName(bc));
					final Toolbarbutton btnAdd = new Toolbarbutton();
					lblName.setHflex("1");
					btnAdd.setIconSclass("z-icon-plus-circle");
					btnAdd.setStyle("color:#779CB1!important");
					btnAdd.setSclass("narrowtoolbarbutton");
					btnAdd.setTooltiptext(sessionHelper.getLabel("Add data field to condition block"));
					btnAdd.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							if (!blockWinMap.isEmpty()) {
								//add data field to active condition block
								if (activeConditionBlockWin != null) {
									createConditionBlockRow(activeConditionBlockWin, br, bc, null);
									resizeConditionBlock();
									makeCustomCondition();
								}
								else {
									UniLog.log1("no active block window");
								}
							}
						}
					});
					row.appendChild(new Hlayout() {{
						appendChild(lblName);
						appendChild(btnAdd);
					}});
					biColumnMap.put(bc.getLabel(), bc);
					biColumnResultMap.put(bc.getLabel(), br.getView().getName());
					/*String fieldType = null;
					if (bc.getField() != null)
						fieldType = bc.getField().getFieldType();
					UniLog.log1("viewname:%s, engname:%s, label:%s, columntype:%s, fieldtype:%s", br.getView().getName(), bc.getEngName(), bc.getLabel(), bc.getColumnType(), fieldType);*/
				}
			}
			//custom predicate
			Row row = new Row();
			row.setParent(rows);
			row.setAttribute("biResult", br);
			row.setAttribute("biColumn", null);
			row.setDraggable("true");
			final Label lblName = new Label("Predicate " + new String(Character.toChars(0x1F4DD)));  //emoji char
			final Toolbarbutton btnAdd = new Toolbarbutton();
			lblName.setHflex("1");
			btnAdd.setIconSclass("z-icon-plus-circle");
			btnAdd.setStyle("color:#779CB1!important");
			btnAdd.setSclass("narrowtoolbarbutton");
			btnAdd.setTooltiptext(sessionHelper.getLabel("Add data field to condition block"));
			btnAdd.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (!blockWinMap.isEmpty()) {
						//add data field to active condition block
						if (activeConditionBlockWin != null) {
							createCustomPredicateConditionBlockRow(activeConditionBlockWin, null);
							resizeConditionBlock();
							makeCustomCondition();
						}
						else {
							UniLog.log1("no active block window");
						}
					}
				}
			});
			row.appendChild(new Hlayout() {{
				appendChild(lblName);
				appendChild(btnAdd);
			}});
		}

		//condition list
		rgConMode.appendChild(new Radio(sessionHelper.getLabel("All Field Mode")){{
			setTooltiptext(
				sessionHelper.getLabel("Match all fields within block and match any blocks") + "\n" +
				sessionHelper.getLabel("- Join Fields with AND connector.") + "\n" +
				sessionHelper.getLabel("- Join Condition Blocks with OR connector.")
			);
		}});
		rgConMode.appendChild(new Radio(sessionHelper.getLabel("Any Field Mode")){{
			setTooltiptext(
				sessionHelper.getLabel("Match any fields within block and match all blocks") + "\n" +
				sessionHelper.getLabel("- Join Fields with OR connector.") + "\n" +
				sessionHelper.getLabel("- Join Condition Blocks with AND connector.")
			);
		}});
		vlCon.setVflex("1");
		vlCon.setHflex("1");
		vlCon.setId("searchConditionBlockLayout");
		groupConList.appendChild(new Caption(){{ 
			appendChild(new Space()); 
			appendChild(new Space()); 
			appendChild(rgConMode); 
			appendChild(btnAddConBlock); 
			appendChild(btnClearConBlock);
		}});
		groupConList.appendChild(vlCon);
		if(sessionHelper.isMobileDevice()) {
			rgConMode.setVisible(false);
			btnAddConBlock.setVisible(false);
			btnClearConBlock.setVisible(false);
		}
		rgConMode.addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>(){
			@Override
			public void onEvent(CheckEvent event) throws Exception {
				UniLog.log("rgConMode " + event);
				for (Map.Entry<Component, List<Hlayout>> entry : blockWinMap.entrySet()) {
					updateConnectorLabel((Div) entry.getKey().getAttribute("connectorComp"));
					for (Hlayout hl : entry.getValue())
						updateConnectorLabel((Div) hl.getAttribute("connectorComp"));
				}
				makeCustomCondition();
			}
		});
		
		Vlayout tmpVlCon1 = (Vlayout)vlCon1;
		tmpVlCon1.setVflex("1");
		tmpVlCon1.setHflex("1");
		tmpVlCon1.setStyle("overflow:auto;");
		btnAddConBlock.setLabel(sessionHelper.getBtLabel("Add Block"));
		btnAddConBlock.setSclass("zkbi-small-button");
		ZkUtil.appendStyle(btnAddConBlock,"float:right");
		btnClearConBlock.setLabel("Clear");
		btnClearConBlock.setSclass("zkbi-small-button");
		ZkUtil.appendStyle(btnClearConBlock,"float:right");
		if (!showClearConBlock) {
			ZkUtil.appendStyle(btnClearConBlock,"display:none");
		}
		vlCon.appendChild(vlCon1);
		btnAddConBlock.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				createConditionBlock(true);
			}
		});
		btnClearConBlock.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				clearConditionBlock();
				customParamValue = "";
				createConditionBlock(false);
			}
		});
		if (!sessionHelper.isAdminUser()) {
			btnAddConBlock.setDisabled(true);
		}

		//custom parameter
		Hlayout hlLimit = new Hlayout();
		hlLimit.setParent(vlCustParam);
		Label lbLimit = new Label(sessionHelper.getLabel("Record Limit") + ":");
		lbLimit.setParent(hlLimit);
		ibLimit.setParent(hlLimit);
		ibLimit.setConstraint("no negative,no zero");
		ibLimit.setStep(1000);
		if(sessionHelper.isMobileDevice()) {
			lbLimit.setVisible(false);
			ibLimit.setVisible(false);
		}
		Hbox presetBox = new Hbox();
		presetBox.setParent(hlLimit);
		presetBox.setAlign("center");
		presetBox.setPack("end");
		presetBox.setHflex("1");
		btnCustomParam.setParent(presetBox);
		btnCustomParam.setIconSclass("z-icon-code");
		btnCustomParam.setTooltiptext(sessionHelper.getBtLabel("View custom query string"));
		btnCustomParam.setSclass("narrowtoolbarbutton");
		new Space(){{setWidth("5px");}}.setParent(presetBox);
		if (showPresetComp) {
			Div pickDiv = new Div();
			pickDiv.setParent(presetBox);
			pickDiv.setHeight("30px");
			pickDiv.setSclass("input-group");
			if (sessionHelper.getAllowS2Listbox())
				pickDiv.addSclass("zkbi-presetbox-select2");
			conditionPresetIsDefault.setTooltiptext(
				sessionHelper.getLabel("Set current preset as default.") + "\n" + sessionHelper.getLabel("Not applicable to public preset.")
			);
			conditionPresetIsDefault.setSclass("input-group-addon");
			conditionPresetIsDefault.setLabel("");
			conditionPresetIsDefault.setHeight("28px");
			conditionPresetIsDefault.setWidth("40px");
			conditionPresetIsDefault.setStyle("padding-top:3px; padding-bottom:3px; padding-left:3px; padding-right:3px;");
			conditionPresetIsDefault.setParent(pickDiv);
			conditionPresetIsDefault.setDisabled(true);
			conditionPresetIsDefault.setWidth("50px");
			conditionPresetListbox.setRows(1);
			conditionPresetListbox.setWidth("220px");
			//conditionPresetListbox.setHeight("28px");
			conditionPresetListbox.setMold("select");
			conditionPresetListbox.setTooltiptext(
				sessionHelper.getLabel("Choose preset search condition.") + "\n" +
				sessionHelper.getLabel("You can define your own search condition by using advanced search.")
			);
			conditionPresetListbox.setSclass("form-control");
			conditionPresetListbox.setStyle("padding-top:4px; padding-bottom:4px; padding-left:5px; padding-right:5px;");
			conditionPresetListbox.setParent(pickDiv);
			Hbox presetBtnBox = new Hbox();
			presetBtnBox.setParent(presetBox);
			presetBtnBox.setAlign("end");
			btnAddPreset.setParent(presetBtnBox);
			btnAddPreset.setIconSclass("z-icon-plus");
			btnAddPreset.setTooltiptext(sessionHelper.getLabel("Save Preset"));
			btnAddPreset.setSclass("narrowtoolbarbutton");
			btnDeletePreset.setParent(presetBtnBox);
			btnDeletePreset.setIconSclass("z-icon-minus");
			btnDeletePreset.setTooltiptext(sessionHelper.getLabel("Delete Preset"));
			btnDeletePreset.setSclass("narrowtoolbarbutton");

			conditionPresetIsDefault.addEventListener(Events.ON_CHECK, conditionPresetIsDefaultEventListener);
			conditionPresetListbox.addEventListener(Events.ON_SELECT, conditionPresetListboxEventListener);
			conditionPresetListbox.addEventListener("onReset", conditionPresetListboxEventListener);
			conditionPresetListbox.addEventListener("onSave", conditionPresetListboxEventListener);
			btnAddPreset.addEventListener(Events.ON_CLICK, addBtnPresetEventListener);
			btnDeletePreset.addEventListener(Events.ON_CLICK, delBtnPresetEventListener);
		}
		btnCustomParam.addEventListener(Events.ON_CLICK, customParamBtnEventListener);
		if(sessionHelper.isMobileDevice()) {
			btnCustomParam.setVisible(false);
		}
		resetValues();
		return win;
	}
	private void checkComponentState() {
		final String presetKey = conditionPresetListbox.getSelectedItem().getValue();
   		conditionPresetIsDefault.setDisabled(true);
		conditionPresetIsDefault.setChecked(false);
		btnDeletePreset.setDisabled(true);
		if (presetKey != null) {
			ConditionFieldMapWrapper fieldMap = conditionPresetMap.get(presetKey);
		if (fieldMap != null) {
			if (fieldMap.isCustom) {
				btnDeletePreset.setDisabled(false);
			} else {
				if (sessionHelper.isAdminUser())
					btnDeletePreset.setDisabled(false);
				}
				if (fieldMap.isDefault)
					conditionPresetIsDefault.setChecked(true);
			}
		}
	}
	private EventListener<CheckEvent> conditionPresetIsDefaultEventListener = new EventListener<CheckEvent>() {
		@Override
		public void onEvent(CheckEvent event) throws Exception {
			String preset = conditionPresetListbox.getSelectedItem().getValue();
			if (preset != null) {
				if (event.isChecked()) {
					boolean isCustom = conditionPresetMap.get(preset).isCustom;
					for (Map.Entry<String, ConditionFieldMapWrapper> entry : conditionPresetMap.entrySet()) {
						String p = entry.getKey();
						ConditionFieldMapWrapper m = entry.getValue();
						if (m.isCustom == isCustom)
							m.isDefault = p.equals(preset);
					}
				} else
					conditionPresetMap.get(preset).isDefault = false;
			}
		}
	};
	private EventListener<Event> conditionPresetListboxEventListener = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			Listbox conditionPresetListbox = (Listbox) event.getTarget();
			if (event.getName().equals(Events.ON_SELECT)) {
				checkComponentState();
				SelectEvent<Listitem, String> selEvt = (SelectEvent<Listitem, String>) event;
				String preset = selEvt.getReference().getValue() != null ? selEvt.getReference().getValue().toString() : null;
				ConditionFieldMapWrapper conditionFieldMap = preset != null ? conditionPresetMap.get(preset) : null;
				clearConditionBlock();
				customParamValue = conditionFieldMap != null ? conditionFieldMap.customCondition : "";
				ibLimit.setValue((conditionFieldMap != null && conditionFieldMap.recordLimit > 0) ? conditionFieldMap.recordLimit : sessionHelper.getDefaultRecordLimit());
				if (preset == null)
					rgConMode.setSelectedIndex(ALL_FIELD_MODE);
				restoreConditionBlocks();
			}
			if (event.getName().equals("onReset")) {
				String selectedKey = (String) event.getData();
				while (conditionPresetListbox.getItemCount() > 0)
					conditionPresetListbox.removeItemAt(0);
				conditionPresetListbox.appendItem(" - " + sessionHelper.getLabel("Choose preset search") + " - ", null);
				conditionPresetListbox.setSelectedIndex(0);
				for (Map.Entry<String, ConditionFieldMapWrapper> entry : conditionPresetMap.entrySet()) {
					String s = entry.getKey();
					Listitem li = conditionPresetListbox.appendItem(ConditionPresets.buildPresetLabelByKey(s, entry.getValue().accessKeyForPublic),s);
					if (conditionPresetMap.get(s).isDefault)
						li.setStyle("font-weight: bold;");
					if (selectedKey != null && s.equals(selectedKey))
						conditionPresetListbox.selectItem(li);
				}
				checkComponentState();
				ConditionFieldMapWrapper conditionFieldMap = selectedKey != null ? conditionPresetMap.get(selectedKey) : null;
				clearConditionBlock();
				customParamValue = conditionFieldMap != null ? conditionFieldMap.customCondition : "";
				ibLimit.setValue((conditionFieldMap != null && conditionFieldMap.recordLimit > 0) ? conditionFieldMap.recordLimit : sessionHelper.getDefaultRecordLimit());
				//reset s2listbox
				if (sessionHelper.getAllowS2Listbox())
					resetupS2Comp(conditionPresetListbox);
				restoreConditionBlocks();
			}
			if (event.getName().equals("onSave")) {
				String defaultPublicPreset = null, defaultCustomPreset = null;
				String displayRecCntPreset = null;
				Map<String, ConditionFieldMapWrapper> newConditionPresetMap = conditionPresetMap;
				for (Map.Entry<String, ConditionFieldMapWrapper> entry : newConditionPresetMap.entrySet()) {
					String newPreset = entry.getKey();
					ConditionFieldMapWrapper newConMap = entry.getValue();
					ConditionFieldMap cfm = refConditionPresets.getFieldMap(newPreset);
					if (cfm == null) {
						cfm = new ConditionFieldMap(newConMap.isCustom, sessionHelper);
						refConditionPresets.putFieldMap(newPreset, cfm);
					}
					cfm.setAccessKeyForPublic(newConMap.accessKeyForPublic);
					cfm.setCustomCondition(newConMap.customCondition);
					cfm.setRecordLimit(newConMap.recordLimit);
					if (newConMap.saved) {
						UniLog.log("G2 setPresetMapOrderbysAndHideCols " + newPreset);
						callback.setPresetMapOrderbysAndHideCols(cfm);
					}
					if (newConMap.isDefault) {
						if (newConMap.isCustom)
							defaultCustomPreset = newPreset;
						else
							defaultPublicPreset = newPreset;
					}
					if (newConMap.isDisplayRecCnt)
						displayRecCntPreset = newPreset;
				}
				List<String> presetList = new ArrayList<String>(refConditionPresets.getPresets());
				for (String preset : presetList) {
					if (!newConditionPresetMap.containsKey(preset))
						refConditionPresets.removeFieldMap(preset);
				}

				if (defaultCustomPreset != null)
					refConditionPresets.setDefaultPreset(defaultCustomPreset, true);
				else {
					defaultCustomPreset = refConditionPresets.getAllDefaultPreset().get("custom");
					if (defaultCustomPreset != null)
						refConditionPresets.setDefaultPreset(defaultCustomPreset, false);
				}
				if (defaultPublicPreset != null)
					refConditionPresets.setDefaultPreset(defaultPublicPreset, true);
				else {
					defaultPublicPreset = refConditionPresets.getAllDefaultPreset().get("public");
					if (defaultPublicPreset != null)
						refConditionPresets.setDefaultPreset(defaultPublicPreset, false);
				}

				if (displayRecCntPreset != null)
					refConditionPresets.setDisplayRecCntPreset(displayRecCntPreset, true);
				else {
					displayRecCntPreset = refConditionPresets.getDisplayRecCntPreset();
					if (displayRecCntPreset != null)
						refConditionPresets.setDisplayRecCntPreset(displayRecCntPreset, false);
				}
				String errMsg = refConditionPresets.saveConditionPresets(viewId, sessionHelper);
				callback.refreshUIForSavePreset(errMsg, ZkBiAdvSearch.this,result);
			}
		}
	};
	private EventListener<Event> addBtnPresetEventListener = new EventListener<Event>() {
		private String realPresetKey, lastRealPresetKey;
		private Checkbox cbPublic;
		private void setRealPresetKey(String presetKey, boolean isPublic) {
			realPresetKey = (isPublic ? "public_" : "custom_") + presetKey;
		}
		private String getPresetKey(String realPresetKey) {
			return realPresetKey.substring(realPresetKey.indexOf('_') + 1);
		}
		@Override
		public void onEvent(Event event) throws Exception {
			final Textbox tb = new Textbox();
			tb.setPlaceholder(sessionHelper.getLabel("Preset name"));
			final Checkbox cbDefault = new Checkbox(sessionHelper.getLabel("Set as default"));
			cbPublic = new Checkbox(sessionHelper.getLabel("Public mode"));
			cbPublic.setChecked(false);
			final Textbox tbAccessKeyForPublic = new Textbox();
			tbAccessKeyForPublic.setWidth("100px");
			tbAccessKeyForPublic.setPlaceholder(sessionHelper.getLabel("Accesskey"));
			tbAccessKeyForPublic.setTooltiptext(sessionHelper.getLabel("Accesskey for public mode"));
			tbAccessKeyForPublic.setDisabled(true);
			final Checkbox cbDisplayRecCnt = new Checkbox(sessionHelper.getLabel("Display record count in dashboard"));
			final Vbox vbox = new Vbox(){{
				appendChild(tb);
				appendChild(cbDefault);
				if (sessionHelper.isAdminUser()) {
					if (sessionHelper.getAllowPresetAccessKey()) {
						appendChild(new Hlayout() {{
							appendChild(cbPublic);
							appendChild(new Space());
							appendChild(tbAccessKeyForPublic);
							setValign("middle");
						}});
					}
					else
						appendChild(cbPublic);
					appendChild(cbDisplayRecCnt);
				}
			}};
			cbPublic.addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>(){
				@Override
				public void onEvent(CheckEvent event) throws Exception {
					if (sessionHelper.getAllowPresetAccessKey()) {
						tbAccessKeyForPublic.setDisabled(!event.isChecked());
						if (!event.isChecked())
							tbAccessKeyForPublic.setText("");
					}
					setRealPresetKey(tb.getText(), event.isChecked());
				}
			});
			MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(sessionHelper.getLabel("Save Preset"), vbox, 
					new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
					parentComp, new EventListener<Messagebox.ClickEvent>() {
				@Override
				public void onEvent(Messagebox.ClickEvent event) throws Exception {
					if (event.getButton() == Messagebox.Button.OK) {
						if (tb.isValid()) {
							boolean isUpdate = false;
							boolean isPublic;
							String accessKeyForPublic = null;
							ConditionFieldMapWrapper fieldMapWraper = conditionPresetMap.get(realPresetKey);
							if (fieldMapWraper != null) {
								isUpdate = true;
								isPublic = cbPublic.isChecked();
								accessKeyForPublic = sessionHelper.getAllowPresetAccessKey() ? tbAccessKeyForPublic.getText() : null;
								if (isPublic && !sessionHelper.isAdminUser()) {
									Messagebox.show("The current user can't update record in public mode");
									event.stopPropagation();
									return;
								}
							} else { 
								fieldMapWraper = new ConditionFieldMapWrapper();
								if (sessionHelper.isAdminUser()) {
									isPublic = cbPublic.isChecked();
									accessKeyForPublic = sessionHelper.getAllowPresetAccessKey() ? tbAccessKeyForPublic.getText() : null;
								}
								else {
									isPublic = false;
									accessKeyForPublic = null;
									setRealPresetKey(tb.getText(), isPublic);
								}
								//fieldMapWraper.map = callback.makeConditionFieldMap(!cbPublic.isChecked());
								conditionPresetMap.put(realPresetKey, fieldMapWraper);
								if (lastRealPresetKey != null) {
									if (getPresetKey(realPresetKey).equals(getPresetKey(lastRealPresetKey)))
										conditionPresetMap.remove(lastRealPresetKey);
								}
							}
							fieldMapWraper.customCondition = customParamValue;
							fieldMapWraper.isCustom = !isPublic;
							if (sessionHelper.getAllowPresetAccessKey())
								fieldMapWraper.accessKeyForPublic = accessKeyForPublic;

							if (cbDefault.isChecked()) {
								for (Map.Entry<String, ConditionFieldMapWrapper> entry : conditionPresetMap.entrySet()) {
									String p = entry.getKey();
									ConditionFieldMapWrapper m = entry.getValue();
									if (m.isCustom == fieldMapWraper.isCustom)
										m.isDefault = p.equals(realPresetKey);
								}
							} else
								fieldMapWraper.isDefault = false;

							if (cbDisplayRecCnt.isChecked()) {
								for (Map.Entry<String, ConditionFieldMapWrapper> entry : conditionPresetMap.entrySet()) {
									String p = entry.getKey();
									ConditionFieldMapWrapper m = entry.getValue();
									m.isDisplayRecCnt = p.equals(realPresetKey);
								}
							} else
								fieldMapWraper.isDisplayRecCnt = false;

							fieldMapWraper.recordLimit = ibLimit.getValue();
							fieldMapWraper.saved = true;
							if (!isUpdate)
								Events.postEvent("onReset", conditionPresetListbox, realPresetKey);
							else {
								String key = conditionPresetListbox.getSelectedItem().getValue();
								if (StringUtils.equals(key, realPresetKey))
									checkComponentState();
								else {
									for (Listitem li : conditionPresetListbox.getItems()) {
										if (StringUtils.equals((String)li.getValue(), realPresetKey)) {
											li.setSelected(true);
											checkComponentState();
											break;
										}
									}
								}
							}
							Events.postEvent("onSave", conditionPresetListbox, null);
						} else
							event.stopPropagation();
					}
				}
   			});
			String presetKey = "";
			lastRealPresetKey = realPresetKey = conditionPresetListbox.getSelectedItem().getValue();
			ConditionFieldMapWrapper fieldMap = null;
			if (realPresetKey != null)
				fieldMap = conditionPresetMap.get(realPresetKey);
			if (fieldMap == null) {
				for (int i = 1;; i++) {
					presetKey = "preset" + i;
					setRealPresetKey(presetKey, false);
					if (!conditionPresetMap.containsKey(realPresetKey))
						break;
				}
			} else
				presetKey = getPresetKey(realPresetKey);
   			tb.setSelectedText(0, presetKey.length(), presetKey, true);
   			tb.setConstraint(constraint);
   			if (fieldMap != null) {
   				cbDefault.setChecked(fieldMap.isDefault);
   				cbPublic.setChecked(!fieldMap.isCustom);
				if (sessionHelper.getAllowPresetAccessKey()) {
					tbAccessKeyForPublic.setText(cbPublic.isChecked() ? fieldMap.accessKeyForPublic : "");
					tbAccessKeyForPublic.setDisabled(!cbPublic.isChecked());
				}
   				cbDisplayRecCnt.setChecked(fieldMap.isDisplayRecCnt);
   			}
			dlg.doHighlighted();
		}
		private final Constraint constraint = new Constraint() {
			@Override
			public void validate(Component comp, Object value)
					throws WrongValueException {
				String s = value != null ? (String) value : "";
				if (s.length() < 1)
					throw new WrongValueException(comp, sessionHelper.getLabel("Name too short"));
				setRealPresetKey(s, cbPublic.isChecked());
			}
		};
	};
	private EventListener<Event> delBtnPresetEventListener = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			final String presetKey = conditionPresetListbox.getSelectedItem().getValue();
			if (presetKey == null)
				Messagebox.show("Please choose preset item");
			else if (!sessionHelper.isAdminUser() && !conditionPresetMap.get(presetKey).isCustom)
				Messagebox.show("This preset can't be updated!");
			else {
				Messagebox.show(String.format("'%s'?", presetKey), sessionHelper.getLabel("Delete Preset"), 
					new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
					Messagebox.QUESTION, new EventListener<Messagebox.ClickEvent>() {
					@Override
					public void onEvent(Messagebox.ClickEvent event) throws Exception {
						if (event.getButton() == Messagebox.Button.OK) {
							conditionPresetMap.remove(presetKey);
							Events.postEvent("onReset", conditionPresetListbox, null);
							Events.postEvent("onSave", conditionPresetListbox, null);
						}
					}
				});
			}
		}
	};
	private EventListener<Event> customParamBtnEventListener = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			Textbox tbCustomParam = new Textbox();
			tbCustomParam.setRows(5);
			tbCustomParam.setMultiline(true);
			tbCustomParam.setDisabled(true);
			tbCustomParam.setWidth("100%");
			tbCustomParam.setText(customParamValue);
			MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(sessionHelper.getLabel("Custom Query String"), tbCustomParam,
				new Messagebox.Button[]{Messagebox.Button.OK },
				parentComp, null
			);
			dlg.setWidth("500px");
			dlg.doHighlighted();
		}
	};
	void resetValues() {
		if (showPresetComp) {
			//conditionPresetIsDefault.setChecked(callback.getConditionPresetIsDefaultValue());
    		while (conditionPresetListbox.getItemCount() > 0)
    			conditionPresetListbox.removeItemAt(0);
    		/*String defaultPreset = refConditionPresets.getDefaultPreset();
			for (Listitem li : callback.getConditionPresetListbox().getItems()) {
				String preset = (String) li.getValue();
				Listitem newLi = conditionPresetListbox.appendItem(li.getLabel(), preset);
   				if (preset != null && StringUtils.equals(preset, defaultPreset))
   					newLi.setStyle("font-weight: bold;");
			}
			if (conditionPresetListbox.getItemCount() > 0) {
				int selIdx = callback.getConditionPresetListbox().getSelectedIndex();
				conditionPresetListbox.setSelectedIndex(selIdx >= 0 ? selIdx : 0);
			}*/
			conditionPresetListbox.appendItem(" - " + sessionHelper.getLabel("Choose preset search") + " - ", null);
 			conditionPresetListbox.setSelectedIndex(0);
    		for (String s : refConditionPresets.getPresets()) {
				Listitem li = conditionPresetListbox.appendItem(ConditionPresets.buildPresetLabelByKey(s, conditionPresetMap.get(s).accessKeyForPublic),s);
				if (conditionPresetMap.get(s).isDefault)
					li.setStyle("font-weight: bold;");
    			if (StringUtils.equals(refSelectedPreset, s))
    				conditionPresetListbox.selectItem(li);
    		}
			//btnDeletePreset.setDisabled(callback.isDeletePresetButtonDisabled());
			checkComponentState();
		}

		clearConditionBlock();
		rgConMode.setSelectedIndex(ALL_FIELD_MODE);
		ibLimit.setValue(result.getRecLimit());
		customParamValue = refCustomParam;
		restoreConditionBlocks();
	}
	private boolean isMayFieldName(String src) {
		src = StringUtils.defaultString(src).trim();
		if (StringUtils.startsWithAny(src, new String[]{"\"", "'"}) && StringUtils.endsWithAny(src, new String[]{"\"", "'"}))
			return false;
		if (NumberUtils.isCreatable(src))
			return false;
		return true;
	}
	private boolean isBlankFieldName(String src) {
		src = StringUtils.defaultString(src).trim();
		if (isFromEmbedPage || isEmbedMode) {
			if (src.equals("blank()"))
				return true;
		}
		return false;
	}
	private String expression2CompValue(String src, BiColumn bc) {
		src = StringUtils.defaultString(src).trim();
		UniLog.log1("src:%s,columntype:%s", src, bc.getColumnType(true));
		if (StringUtils.startsWithAny(src, new String[]{"\"", "'"}))
			src = src.substring(1);
		if (StringUtils.endsWithAny(src, new String[]{"\"", "'"}))
			src = src.substring(0, src.length() - 1);
		src = src.replace("\\'", "'");
		return src;
	}
	private String expression2CompValue(List<Object> list, BiColumn bc) {
		return ZkJxQueryInput.escapeStringList(expression2CompList(list, bc));
	}
	private List<String> expression2CompList(List<Object> list, BiColumn bc) {
		List<String> strList = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++)
			strList.add(expression2CompValue(list.get(i).toString(), bc));
		return strList;
	}
	private String makeCustomPredicateFieldHtml(String name, String cellfullname) {
   		return "<span contenteditable='false' class='field' draggable='true' style='color:#0088b0; text-decoration: underline; margin:1px;inline-block;' data-cellfullname='"+cellfullname+"' title='"+cellfullname+"'>"+name+"</span>&nbsp;";
	}
	private String makeCustomPredicateHtml(String text) {
		text = text.replaceAll("\\s+", " ");
		Map<String, BiColumn> map = new HashMap<String, BiColumn>();
		List<BiColumn> list = new ArrayList<BiColumn>(biColumnMap.values());
		Collections.sort(list, new Comparator<BiColumn>() {
			@Override
			public int compare(BiColumn o1, BiColumn o2) {
				return o2.getLabel().compareTo(o1.getLabel());
			}
		});
		int i = 0;
		for (BiColumn bc : list) {
			if (text.indexOf(bc.getLabel()) > -1) {
				String key = "\t" + i++;
				text = text.replace(bc.getLabel(), key);
				map.put(key, bc);
			}
		}
		for (Map.Entry<String, BiColumn> entry : map.entrySet()) {
			BiColumn bc = entry.getValue();
			text = text.replace(entry.getKey(), makeCustomPredicateFieldHtml(getBiColumnTranslateName(bc)/*bc.getEngName()*/, bc.getLabel()));
		}
		return text;
	}
	private void bindCustomPredicateElement(Hlayout hlRow) {
		ZkUtil.js("setTimeout(function() { zkAdvSearchBindPredicateElement('%s','%s') }, 100)", ((Div)hlRow.getAttribute("valueComp0")).getId(), sessionHelper.getLabel("Please drag data field here to build predicate"));
	}
	private void setCustomPredicateText(Hlayout hlRow, String text) {
		ZkUtil.js("zkAdvSearchSetPredicateHtml('%s', '%s')", ((Div)hlRow.getAttribute("valueComp0")).getId(), StringEscapeUtils.escapeJavaScript(makeCustomPredicateHtml(text)));
		hlRow.setAttribute("customValue", text);
	}
	private void restoreConditionBlocks1(/*Window win*/Component win, Condition predicateCond) {
		if (!predicateCond.get_isPredicate())
			return;
		Expression leftExp = predicateCond.get_leftExpression();
		int operator = predicateCond.get_operator();
		BiColumn biColumn = biColumnMap.get(StringUtils.defaultString(leftExp.toString()).trim());
		boolean isBlankRightExp = false;
		if (biColumn != null) {
			//try create custom predicate condition block row
			switch (operator) {
				case Condition.COMPARE_OP_GT:
				case Condition.COMPARE_OP_GE:
				case Condition.COMPARE_OP_EQ:
				case Condition.COMPARE_OP_NE:
				case Condition.COMPARE_OP_LT:
				case Condition.COMPARE_OP_LE:
					String rightExp = StringUtils.defaultString(predicateCond.get_rightExpression().toString()).trim();
					isBlankRightExp = isBlankFieldName(rightExp) && (operator == Condition.COMPARE_OP_EQ || operator == Condition.COMPARE_OP_NE);
					if (!isBlankRightExp && isMayFieldName(rightExp)) {
						Hlayout hlRow = createCustomPredicateConditionBlockRow(win, predicateCond);
						setCustomPredicateText(hlRow, predicateCond.toString());
						return;
					}
					break;
				case Condition.COMPARE_OP_BETWEEN:
				case Condition.COMPARE_OP_NOT_BETWEEN:
				case Condition.COMPARE_OP_IN_ITEMLIST:
				case Condition.COMPARE_OP_NOTIN_ITEMLIST:
				case Condition.COMPARE_OP_MA:
				case Condition.COMPARE_OP_NM:
				case Condition.COMPARE_OP_LK:
				case Condition.COMPARE_OP_NLK:
				case Condition.COMPARE_OP_REGEXP:
				case Condition.COMPARE_OP_NOT_REGEXP:
				case Condition.COMPARE_OP_IS_NOT_NULL:
				case Condition.COMPARE_OP_IS_NULL:
					break;
				default:
					{
						//create custom predicate condition block row
						Hlayout hlRow = createCustomPredicateConditionBlockRow(win, predicateCond);
						setCustomPredicateText(hlRow, predicateCond.toString());
						return;
					}
			}
			
			//create condition block row
			BiResult biResult = biResultMap.get(biColumnResultMap.get(biColumn.getLabel()));
			Hlayout hlRow = createConditionBlockRow(win, biResult, biColumn, predicateCond);
			Combobox cbOperator = (Combobox) hlRow.getAttribute("operatorComp");
			Textbox labelComp = (Textbox) hlRow.getAttribute("labelComp");
			XulElement valComp0 = (XulElement) hlRow.getAttribute("valueComp0");
			XulElement valComp1 = (XulElement) hlRow.getAttribute("valueComp1");
			XulElement valComp2 = (XulElement) hlRow.getAttribute("valueComp2");
			XulElement valComp3 = (XulElement) hlRow.getAttribute("valueComp3");
			Label valAnd = (Label) hlRow.getAttribute("valueAnd");
			List<Comboitem> cbOperatorItemList = cbOperator.getItems();
			boolean flag = false;
			for (Comboitem item : cbOperatorItemList) {
				if ((ConditionOp)item.getValue() == ConditionOp.findConditionOp(operator)) {
					cbOperator.setSelectedItem(item);
					flag = true;
					break;
				}
			}
			if (!flag)
				cbOperator.setSelectedIndex(-1);
			String[] optionList = getTypeIntOptionList(biColumn);
			String comp0Value = "", comp1Value = "", comp2Value = "", comp3Value = "";
			List<String> comp3List = new ArrayList<String>();
			//if (operatorMap.containsKey(operator)) {
			if (operator > 0 && ConditionOp.findConditionOp(operator) != null) {
				switch (operator) {
				case Condition.COMPARE_OP_BETWEEN:
				case Condition.COMPARE_OP_NOT_BETWEEN:
					comp0Value = expression2CompValue(predicateCond.get_rightExpression1().toString(), biColumn);
					comp1Value = expression2CompValue(predicateCond.get_rightExpression2().toString(), biColumn);
					comp0Value = getOptionLabel(optionList, comp0Value);
					comp1Value = getOptionLabel(optionList, comp1Value);
					break;
				case Condition.COMPARE_OP_IN_ITEMLIST:
				case Condition.COMPARE_OP_NOTIN_ITEMLIST:
					if (valComp3 instanceof Listbox)
						comp3List = expression2CompList(predicateCond.get_rightExpressionList(), biColumn);
					else {
						comp3Value = expression2CompValue(predicateCond.get_rightExpressionList(), biColumn);
						comp3Value = getOptionLabelForComma(optionList, comp3Value);
					}
					break;
				case Condition.COMPARE_OP_MA:
				case Condition.COMPARE_OP_NM:
				case Condition.COMPARE_OP_LK:
				case Condition.COMPARE_OP_NLK:
				case Condition.COMPARE_OP_REGEXP:
				case Condition.COMPARE_OP_NOT_REGEXP:
					comp2Value = expression2CompValue(predicateCond.get_rightExpression().toString(), biColumn);
					break;
				case Condition.COMPARE_OP_IS_NOT_NULL:
				case Condition.COMPARE_OP_IS_NULL:
					break;
				default:
					if (isBlankRightExp) {
						for (Comboitem item : cbOperatorItemList) {
							if ((operator == Condition.COMPARE_OP_EQ && (ConditionOp)item.getValue() == ConditionOp.IS_BLANK)
									|| (operator == Condition.COMPARE_OP_NE && (ConditionOp)item.getValue() == ConditionOp.IS_NOT_BLANK)) {
								cbOperator.setSelectedItem(item);
								break;
							}
						}
					} else {
						comp0Value = expression2CompValue(predicateCond.get_rightExpression().toString(), biColumn);
						if (comp0Value.isEmpty()) {
							for (Comboitem item : cbOperatorItemList) {
								if (isEmbedMode || isFromEmbedPage) {
									if ((operator == Condition.COMPARE_OP_EQ && (ConditionOp)item.getValue() == ConditionOp.EQ)
											|| (operator == Condition.COMPARE_OP_NE && (ConditionOp)item.getValue() == ConditionOp.NE)) {
										cbOperator.setSelectedItem(item);
										break;
									}
								} else {
									if ((operator == Condition.COMPARE_OP_EQ && (ConditionOp)item.getValue() == ConditionOp.IS_BLANK)
											|| (operator == Condition.COMPARE_OP_NE && (ConditionOp)item.getValue() == ConditionOp.IS_NOT_BLANK)) {
										cbOperator.setSelectedItem(item);
										break;
									}
								}
							}
						}
						else
							comp0Value = getOptionLabel(optionList, comp0Value);
					}
				}
			}
			else {
				labelComp.setValue(sessionHelper.getLabel("Custom SQL"));
				comp2Value = predicateCond.toString();
			}
			UniLog.log1("leftExt:" + leftExp + ",operator:" + operator + ",c0v:" + comp0Value + ",c1v:" + comp1Value + ",c2v:" + comp2Value + ",c3v:" + StringUtils.join(comp3List, ","));
			setInputValue(valComp0, comp0Value);
			setInputValue(valComp1, comp1Value);
			setInputValue(valComp2, comp2Value);
			if (valComp3 instanceof Listbox)
				setInputValue(valComp3, comp3List);
			else
				setInputValue(valComp3, comp3Value);
			visibleInputComps(cbOperator, valComp0, valComp1, valAnd, valComp2, valComp3);
		}
		else {
			//create custom predicate condition block row
			Hlayout hlRow = createCustomPredicateConditionBlockRow(win, predicateCond);
			setCustomPredicateText(hlRow, predicateCond.toString());
		}
	}
	private List<ConditionWrapper> parseCondition(String str, Boolean[] byAnds) throws Exception {
		List<ConditionWrapper> list = new ArrayList<ConditionWrapper>();
		Parser yyparser = new Parser(null, null, null);
		UniLog.log1("str:" + str);
		Condition result = (Condition) yyparser.parse(str);
		UniLog.log1(result.toString());
		if (result.get_isPredicate())
			list.add(new ConditionWrapper(result));
		else {
			boolean byAnd = result.get_operator() == Condition.LOGIC_OP_OR;
			byAnds[0] = byAnd;
			List<Condition> l1 = Condition.serializeCondition(byAnd, result);
			l1 = Condition.optimizeConditionList(l1, yyparser, byAnd);
			for (Condition cond : l1) {
				UniLog.log1("cond:" + cond + "," + cond.get_isPredicate());
				ConditionWrapper condw = new ConditionWrapper(cond);
				list.add(condw);
				if (!cond.get_isPredicate())
					condw.childs.addAll(parseCondition(cond.toString(), new Boolean[1]));
			}
		}
		return list;
	}
	private Condition parseCondition(String str) throws Exception {
		if (StringUtils.isBlank(str))
			return null;
		Condition result = null;
		try {
			Parser yyparser = new Parser(null, null, null);
			UniLog.log1("parseCondition str:" + str);
			result = (Condition) yyparser.parse(str);
			UniLog.log1("result:%s,get_isPredicate:%b,get_leftExpression:%s,get_rightExpression:%s", result.toString(), result.get_isPredicate(), result.get_leftExpression(), result.get_rightExpression());
		}
		catch (Exception e) {
		}
		catch (Error e) {
		}
		return result;
	}
	private void fillBiResultMap() {
		if (biResultMap.isEmpty()) {
			biResultMap.clear();
			biColumnMap.clear();
			biColumnResultMap.clear();
			Vector<BiResult> resultList = new Vector<BiResult>();
			resultList.add(result);
			if(result.getSubLinks()!=null) resultList.addAll(result.getSubLinks());
			for (final BiResult br : resultList) {
				biResultMap.put(br.getView().getName(), br);
				for (final BiColumn bc : br.getColumns()) {
					if (!bc.isNoQuery() && (!bc.isInvisible(br.getSessionHelper()) || bc.isInList(br.getSessionHelper()))) {
						if(br != result && (bc.getField() == null )) continue;
						if (biColumnMap.containsKey(bc.getLabel())) continue;
						biColumnMap.put(bc.getLabel(), bc);
						biColumnResultMap.put(bc.getLabel(), br.getView().getName());
					}
				}
			}
		}
	}
	/*
	 * Embed advanced search to main list 
	 * */
	public boolean restoreEmbedConditionBlocks(Groupbox gbEmbed, Div divParent, String customParam) {
		try {
			UniLog.log1("customParam:%s", customParam);
			isEmbedMode = true;
			customParamValue = customParam;
			gbEmbed.setTitle(String.format(sessionHelper.getLabel("Advanced Search") + " (%s)", sessionHelper.getLabel("All Field Mode")));
			while (divParent.getChildren().size() > 0)
				divParent.getChildren().remove(0);
			blockWinMap.clear();
			if (callback != null)
				callback.customConditionReset(customParam);
			if (StringUtils.isBlank(customParamValue))
				return false;

			vlCon1 = divParent;
			while (rgConMode.getItemCount() > 0)
				rgConMode.removeItemAt(0);
			rgConMode.appendChild(new Radio(sessionHelper.getLabel("All Field Mode")));
			rgConMode.appendChild(new Radio(sessionHelper.getLabel("Any Field Mode")));
			rgConMode.setSelectedIndex(ALL_FIELD_MODE);

			fillBiResultMap();
			
			Boolean[] byAnds = new Boolean[1];
			List<ConditionWrapper> list = parseCondition(customParamValue, byAnds);
			boolean onlyOneBlock = true;
			for (ConditionWrapper condw : list) {
				if (!condw.cond.get_isPredicate() || !condw.childs.isEmpty()) {
					onlyOneBlock = false;
					break;
				}
			}
			if (onlyOneBlock) {
				if (byAnds[0] != null) {
					rgConMode.setSelectedIndex(byAnds[0] ? ANY_FIELD_MODE : ALL_FIELD_MODE);
					gbEmbed.setTitle(String.format(sessionHelper.getLabel("Advanced Search") + " (%s)", sessionHelper.getLabel(byAnds[0] ? "Any Field Mode" : "All Field Mode")));
				}
				Div win = createEmbedConditionBlockNoTitle(divParent);
				divParent.appendChild(win);
				for (ConditionWrapper condw : list)
					restoreConditionBlocks1(win, condw.cond);
			} else {
				rgConMode.setSelectedIndex(byAnds[0] ? ALL_FIELD_MODE : ANY_FIELD_MODE);
				gbEmbed.setTitle(String.format(sessionHelper.getLabel("Advanced Search") + " (%s)", sessionHelper.getLabel(byAnds[0] ? "All Field Mode" : "Any Field Mode")));
				for (ConditionWrapper condw : list) {
					Groupbox win = createEmbedConditionBlockHasTitle(divParent);
					if (condw.cond.get_isPredicate())
						restoreConditionBlocks1(win, condw.cond);
					else {
						for (ConditionWrapper condwc : condw.childs)
							restoreConditionBlocks1(win, condwc.cond);
					}
				}
			}
			return !blockWinMap.isEmpty();
		} catch (Exception e) {
			UniLog.log(e);
			return false;
		}
	}
	private void restoreConditionBlocks() {
		try {
			if (StringUtils.isBlank(customParamValue)) {
				clearConditionBlock();
				createConditionBlock(false);
				return;
			}
			Boolean[] byAnds = new Boolean[1];
			List<ConditionWrapper> list = parseCondition(customParamValue, byAnds);
			boolean onlyOneBlock = true;
			for (ConditionWrapper condw : list) {
				if (!condw.cond.get_isPredicate() || !condw.childs.isEmpty()) {
					onlyOneBlock = false;
					break;
				}
			}
			if (onlyOneBlock) {
				if (byAnds[0] != null)
					rgConMode.setSelectedIndex(byAnds[0] ? ANY_FIELD_MODE : ALL_FIELD_MODE);
				Window win = createConditionBlock(false);
				for (ConditionWrapper condw : list)
					restoreConditionBlocks1(win, condw.cond);
			} else {
				rgConMode.setSelectedIndex(byAnds[0] ? ALL_FIELD_MODE : ANY_FIELD_MODE);
				for (ConditionWrapper condw : list) {
					Window win = createConditionBlock(false);
					if (condw.cond.get_isPredicate())
						restoreConditionBlocks1(win, condw.cond);
					else {
						for (ConditionWrapper condwc : condw.childs)
							restoreConditionBlocks1(win, condwc.cond);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			clearConditionBlock();
			createConditionBlock(false);
		}
	}
	private Div createConnector(Component parent) {
		Div div = new Div();
		div.setParent(parent);
		div.setClass(parent == vlCon1 ? "zkbi-block-connector" : "zkbi-field-connector");
		div.setAlign("center");
		if (parent == vlCon1 || showFieldConnector) {
			Label label = new Label();
			label.setTooltiptext(sessionHelper.getLabel(parent == vlCon1 ? "Blocks Connector" : "Fields Connector"));
			label.setWidth("50px");
			label.setStyle("display:inline-block;");
			label.setParent(div);
			div.setAttribute("label", label);
		}
		updateConnectorLabel(div);
		return div;
	}
	private void updateConnectorLabel(Div div) {
		if (div == null)
			return;
		Component parent = div.getParent();
		//Label label = (Label) div.query("Label");
		Label label = (Label) div.getAttribute("label");
		if (parent == vlCon1) {
			if (label != null)
				label.setValue(rgConMode.getSelectedIndex() == ANY_FIELD_MODE ? "AND" : "OR");
			div.setAttribute("connector", rgConMode.getSelectedIndex() == ANY_FIELD_MODE ? Condition.LOGIC_OP_AND : Condition.LOGIC_OP_OR);
		} else {
			if (label != null)
				label.setValue(rgConMode.getSelectedIndex() == ANY_FIELD_MODE ? "OR" : "AND");
			div.setAttribute("connector", rgConMode.getSelectedIndex() == ANY_FIELD_MODE ? Condition.LOGIC_OP_OR : Condition.LOGIC_OP_AND);
		}
	}
	private List<Expression> getInputExpression(XulElement valueComp, XulElement valueComp0, BiColumn bc) {
		List<String> objList = new ArrayList<String>();
		if (valueComp instanceof ZkJxQueryInput) {
			Object o = ((ZkJxQueryInput)valueComp).getQueryObject();
			if (o instanceof String)
				objList.add((String)o);
		}
		else if (valueComp instanceof InputElement) {
			objList.add(((InputElement)valueComp).getText());
		}
		else if (valueComp instanceof Listbox) {
			for (Listitem li : ((Listbox)valueComp).getSelectedItems())
				objList.add((String)li.getValue());
			if (objList.isEmpty())
				objList.add("");
		}
		UniLog.log("getInputExpression obj:" + StringUtils.join(objList, ","));
		String[] optionList = getTypeIntOptionList(bc);
		List<Expression> list = new ArrayList<Expression>();
		for (String obj : objList) {
			for (String s : ZkJxQueryInput.unescapeString(obj)) {
				String s1 = s.replace("\\", "\\\\");
				UniLog.log1("getInputExpression s:%s, s1:%s", s, s1);
				Object o = null;
				if (valueComp0 instanceof ZkJxQueryInput)
					o = ((ZkJxQueryInput)valueComp0).getQueryObjectFromText(s1);
				else
					o = s1;
				if (o != null) {
					if (o instanceof Integer)
						list.add(new Expression((Integer)o));
					else if (o instanceof Date)
						list.add(new Expression((Date)o));
					else if (o instanceof Float)
						list.add(new Expression((Float)o));
					else if (o instanceof Double)
						list.add(new Expression((Double)o));
					else if (o instanceof String)
						list.add(new Expression(indexOfOptionList(optionList, o).toString()));
					else 
						list.add(new Expression(o.toString()));
				}
			}
		}
		return list;
	}
	private Expression getInputExpression(XulElement valueComp, BiColumn bc) throws Exception {
		Object obj;
		if (valueComp instanceof ZkJxQueryInput) {
			obj = ((ZkJxQueryInput)valueComp).getQueryObject();
			obj = indexOfOptionList(getTypeIntOptionList(bc), obj);
		} else if (valueComp instanceof Timebox)
			obj = StringUtils.isNotBlank(((Timebox)valueComp).getText()) ? TIMEBOX_DATE + " " + ((Timebox)valueComp).getText() : "";
		else if (valueComp instanceof InputElement)
			obj = ((InputElement)valueComp).getText();
		else if (valueComp instanceof Listbox) {
			Listitem li = ((Listbox)valueComp).getSelectedItem();
			obj = li != null ? li.getValue() : "";
		} else
			obj = "";
		if (obj instanceof Date && StringUtils.equals(bc.getColumnType(true), "datetime"))
			obj = DateUtil.dateToDateTimeStr((Date)obj);
		UniLog.log("getInputExpression obj:" + obj);
		if (obj != null) {
			if (obj instanceof Integer)
				return new Expression((Integer)obj);
			else if (obj instanceof Date)
				return new Expression((Date)obj);
			else if (obj instanceof Float)
				return new Expression((Float)obj);
			else if (obj instanceof Double)
				return new Expression((Double)obj);
			else 
				return new Expression(obj.toString().replace("\\", "\\\\"));
		} else
			return new Expression("");
	}
	private String getInputValue(XulElement valueComp) {
		if (valueComp instanceof ZkJxQueryInput)
			return ((ZkJxQueryInput)valueComp).getQueryString();
		else if (valueComp instanceof Timebox)
			return StringUtils.isNotBlank(((Timebox)valueComp).getText()) ? TIMEBOX_DATE + " " + ((Timebox)valueComp).getText() : "";
		else if (valueComp instanceof InputElement)
			return ((InputElement)valueComp).getText();
		else if (valueComp instanceof Listbox) {
			Listitem li = ((Listbox)valueComp).getSelectedItem();
			return li != null ? li.getValue().toString() : "";
		}
		else
			return "";
	}
	private void setInputValue(XulElement valueComp, String text) {
		if (valueComp instanceof ZkJxQueryInput)
			((ZkJxQueryInput)valueComp).setQueryString(text);
		else if (valueComp instanceof Timebox) {
			Date d = DateUtil.dateTimeStrToDate(text);
			((Timebox) valueComp).setValue(d);
		}
		else if (valueComp instanceof InputElement)
			((InputElement)valueComp).setText(text);
		else if (valueComp instanceof Listbox) {
			Listbox lb = (Listbox)valueComp;
			boolean flag = false;
			for (Listitem li : lb.getItems()) {
				if (StringUtils.equals(li.getValue().toString(), text)) {
					li.setSelected(true);
					flag = true;
					break;
				}
			}
			if (!flag && StringUtils.isNotEmpty(text))
				lb.appendItem(text, text).setSelected(true);
			resetupS2Comp((Listbox)valueComp);
		}
	}
	private void setInputValue(XulElement valueComp, List<String> textList) {
		if (valueComp instanceof Listbox) {
			Listbox lb = (Listbox)valueComp;
			boolean flag = false;
			for (Listitem li : lb.getItems()) {
				if (textList.contains(li.getValue().toString())) {
					li.setSelected(true);
					flag = true;
				}
			}
			if (!flag) {
				if (textList.size() > 1) {
					for (String s : textList)
						lb.appendItem(s, s).setSelected(true);
				} else if (textList.size() == 1) {
					String s = textList.get(0);
					if (StringUtils.isNotEmpty(s))
						lb.appendItem(s, s).setSelected(true);
				}
			}
			resetupS2Comp((Listbox)valueComp);
		}
	}
	private void resetupS2Comp(Listbox lb) {
		ZkUtil.delayJs(lb,null,50,"zkbis2.setup('%s',%b,%b,'%s',%b,%b);$('#%s').focus()",lb.getUuid(), lb.isMultiple(), false, StringUtils.defaultString((String)lb.getAttribute("placeholder")), false, false, lb.getUuid());
	}
	private ConditionOp getConditionOp(Combobox cbOperator) {
		return cbOperator.getSelectedIndex() >= 0 ? (ConditionOp)cbOperator.getSelectedItem().getValue() : null;
	}
	private Condition getInputCondition(BiColumn bc, XulElement valueComp0, Combobox cbOperator, XulElement valueComp1, XulElement valueComp2, XulElement valueComp3, Condition oldCondition) throws Exception {
		return getInputCondition(bc.getLabel(), bc, valueComp0, cbOperator, valueComp1, valueComp2, valueComp3, oldCondition);
	}
	private Condition getInputCondition(String expLeftStr, BiColumn bc, XulElement valueComp0, Combobox cbOperator, XulElement valueComp1, XulElement valueComp2, XulElement valueComp3, Condition oldCondition) throws Exception {
		Expression fExp = new Expression(new Variable(expLeftStr,null,null));
		ConditionOp operator = getConditionOp(cbOperator);
		if (operator != null) {
			switch (operator) {
			case BETWEEN:
			case NOT_BETWEEN:
				Expression vExp0 = getInputExpression(valueComp0, bc);
				Expression vExp1 = getInputExpression(valueComp1, bc);
				return new Condition(fExp, operator.id, vExp0, vExp1);
			case IN_ITEMLIST:
			case NOTIN_ITEMLIST:
				//return new Condition(fExp, operator.id, getInputExpression(valueComp2, valueComp0));
				return new Condition(fExp, operator.id, getInputExpression(valueComp3, valueComp0, bc));
			case MA:
			case NM:
			case LK:
			case NLK:
			case REGEXP:
			case NOT_REGEXP:
				return new Condition(fExp, operator.id, getInputExpression(valueComp2, bc));
			case IS_NOT_NULL:
			case IS_NULL:
				return new Condition(fExp, operator.id);
			case IS_BLANK:
				if (isFromEmbedPage)
					return parseCondition(expLeftStr + " = blank()");
				else
					return new Condition(fExp, Condition.COMPARE_OP_EQ, new Expression(""));
			case IS_NOT_BLANK:
				if (isFromEmbedPage)
					return parseCondition(expLeftStr + " <> blank()");
				else
					return new Condition(fExp, Condition.COMPARE_OP_NE, new Expression(""));
			default:
				return new Condition(fExp, operator.id, getInputExpression(valueComp0, bc));
			}
		} else 
			return oldCondition;
	}
	public static class QueryGetItemProperty extends AbstractGetItemProperty
	{
		BiColumn bicol;
		BiField bifd;
		SelectUtil su;
		Wherecl appendWhere;
		List<Object> qList = null;
		public QueryGetItemProperty(BiColumn p_bc,SelectUtil p_su,Wherecl p_where) {
			bicol = p_bc;
			bifd = p_bc.getField();
			su = p_su;
			appendWhere = p_where;
		}

		@Override
		public int getColumnCount(Object item) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public String getString(Object item) {
			// TODO Auto-generated method stub
			if (bicol.getColumnType(true).trim().equals("date") && item instanceof Date)
				return DATE_FORMAT.format(item);
			else if (bicol.getColumnType(true).trim().equals("datetime") && item instanceof Integer)
				return DATETIME_FORMAT.format(new Date((Integer)item * 1000));
			else if (bicol.getColumnType(true).trim().equals("time") && item instanceof Integer)
				return TIME_FORMAT.format(new Date((Integer)item * 1000));
			return(item.toString());
		}

		@Override
		public Object getRow(int p_row) {
			// TODO Auto-generated method stub
			return(qList.get(p_row));
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			if(qList == null) {
				qList = bifd.getUniqueList(su,appendWhere);
			}
			return (qList == null ? 0:qList.size());
		}

		@Override
		public int getIndexOf(Object item) {
			// TODO Auto-generated method stub
			return(qList.indexOf(item));
		}

		@Override
		public int getRowWidth() {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	protected XulElement buildInputComp(BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		return buildInputComp(sessionHelper, parentComp, result, bc, textFlag, multiPickSelectFlag);
	}
	public static XulElement buildInputComp(SessionHelper sessionHelper, Component parentComp, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		XulElement ie;
		if (!textFlag) {
			if (bc.getColumnType(true).trim().equals("date")){
				ie = new ZkJxQueryInput();
				((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_DATE, sessionHelper);
			} else if (bc.getColumnType(true).trim().equals("datetime")){
				ie = new ZkJxQueryInput();
				((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_DATETIME, sessionHelper);
			} else if (bc.getColumnType(true).trim().equals("time")){
				ie = new Timebox();
			} else if (bc.getColumnType(true).trim().matches("integer|serial")){
				ie = new ZkJxQueryInput();
				((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_INTEGER, sessionHelper);
			} else if (bc.getColumnType(true).trim().matches("float|double|money")){
				ie = new ZkJxQueryInput();
				((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_FLOAT, sessionHelper);
			} else {
				ie = buildQueryInput(sessionHelper, parentComp, textFlag, multiPickSelectFlag, true, result, bc);
			}
		} else {
			ie = buildQueryInput(sessionHelper, parentComp, textFlag, multiPickSelectFlag, true, result, bc);
		}
		return ie;
	}
	public static XulElement buildQueryInput(SessionHelper sessionHelper, Component parentComp, boolean textFlag, boolean multiPickSelectFlag, boolean allowS2, BiResult result, BiColumn bc) {
		XulElement ie;
		if (textFlag) {
			if (sessionHelper.getAllowAdvSearchUseS2() && allowS2) {
				if (multiPickSelectFlag) {
					if (bc.getField() != null)
						ie = buildS2ListboxComp(sessionHelper, parentComp, true, 
								new QueryGetItemProperty(bc, result.getSelectUtil(),result.getFieldUniqueListAppendWhere(bc, null)), 
								bc.getOptionList(sessionHelper), true, true);
					else if (bc.getOptionList(sessionHelper) != null)
						ie = buildS2ListboxComp(sessionHelper, parentComp, true, null, bc.getOptionList(sessionHelper), false, true);
					else {
						ie = new ZkJxQueryInput();
						((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
					}
				} else {
					ie = new ZkJxQueryInput();
					((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				}
			} else {
				ie = new ZkJxQueryInput();
				((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				if (multiPickSelectFlag) {
					if (bc.getField() != null)
						((ZkJxQueryInput)ie).setGiPi(
							new QueryGetItemProperty(bc, result.getSelectUtil(),result.getFieldUniqueListAppendWhere(bc, null)), 
							bc.getOptionList(sessionHelper), true, StringUtils.equals(bc.getField().getFieldType(), "integer"), true);
					else
						((ZkJxQueryInput)ie).setGiPi(null, bc.getOptionList(sessionHelper), true, false, true);
				}
			}
		} else {
			if (sessionHelper.getAllowAdvSearchUseS2() && allowS2) {
				if (bc.getField() != null)
					ie = buildS2ListboxComp(sessionHelper, parentComp, false, 
							new QueryGetItemProperty(bc, result.getSelectUtil(),result.getFieldUniqueListAppendWhere(bc, null)), 
							bc.getOptionList(sessionHelper), true, true);
				else if (bc.getOptionList(sessionHelper) != null)
					ie = buildS2ListboxComp(sessionHelper, parentComp, false, null, bc.getOptionList(sessionHelper), false, true);
				else {
					ie = new ZkJxQueryInput();
					((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
					((InputElement)ie).setMaxlength(bc.getColumnLength());
				}
			} else {
				ie = new ZkJxQueryInput();
				((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				if (bc.getField() != null)
					((ZkJxQueryInput)ie).setGiPi(new QueryGetItemProperty(bc, result.getSelectUtil(),result.getFieldUniqueListAppendWhere(bc, null)), 
						bc.getOptionList(sessionHelper), false, StringUtils.equals(bc.getField().getFieldType(), "integer"), true);
				else
					((ZkJxQueryInput)ie).setGiPi(null, bc.getOptionList(sessionHelper), false, false, true);
				((InputElement)ie).setMaxlength(bc.getColumnLength());
			}
		}
		return ie;
	}
	public static String[] getTypeIntOptionList(BiColumn bc, SessionHelper sessionHelper) {
		return bc.getField() != null && StringUtils.equals(bc.getField().getFieldType(), "integer") ? bc.getOptionList(sessionHelper) : null;
	}
	public static Listbox buildS2ListboxComp(SessionHelper sessionHelper, Component parentComp, boolean multiPickSelectFlag, AbstractGetItemProperty gipi, String[] colOptionList, boolean needCheckOptionListIndex, boolean needUnionOptionList) {
		final Listbox lb = new Listbox();
		lb.setMold("select");
		lb.setMultiple(multiPickSelectFlag);
		lb.setAttribute("select2-enable", "Y");
		lb.setAttribute("select2-multiple", multiPickSelectFlag ? "Y" : "N");
		lb.setAttribute("placeholder", sessionHelper.getLabel("Please choose an option"));
		
		Set<Integer> optFlagList = new HashSet<Integer>();
		int n = gipi.getRowCount();
		for(int i=0;i<n;i++) {
			Object o = gipi.getRow(i);
			String v = gipi.getString(o);
			String s;
			if (needCheckOptionListIndex && colOptionList != null && NumberUtils.isDigits(v)) {
				int vi = NumberUtils.toInt(v);
				if (vi >= 0 && vi < colOptionList.length) {
					s = colOptionList[vi];
					optFlagList.add(vi);
				} else
					s = v;
			}
			else
				s = v;
			lb.appendItem(s, s);
		}
		if (colOptionList != null && needUnionOptionList) {
			for (String s : colOptionList) {
				if (optFlagList.contains(s))
					continue;
				lb.appendItem(s, s);
			}
		}

		lb.addEventListener("onSetupSelect2", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				ZkUtil.setupSelect2(lb, true, !lb.isMultiple());
			}
		});
		ZkUtil.delayPostEvent("onSetupSelect2", parentComp, lb, null, 50);

		return lb;
	}
	private void setValueCompVisible(XulElement comp, boolean b) {
		XulElement ele = getValueCompPackDivOrDefault(comp);
		ele.setVisible(b);
		if (b && comp instanceof Listbox)
			resetupS2Comp((Listbox)comp);
	}
	private XulElement getValueCompPackDivOrDefault(XulElement comp) {
		if (comp.hasAttribute("packDiv"))
			return (XulElement)comp.getAttribute("packDiv");
		return comp;
	}
	private void visibleInputComps(Combobox cbOperator, XulElement valComp0, XulElement valComp1, Label valAnd, XulElement valComp2, XulElement valComp3) {
		if (cbOperator.getSelectedIndex() >= 0) {
			cbOperator.setVisible(true);
			switch ((ConditionOp)cbOperator.getSelectedItem().getValue()) {
			case BETWEEN:
			case NOT_BETWEEN:
				setValueCompVisible(valComp0, true);
				setValueCompVisible(valComp1, true);
				setValueCompVisible(valComp2, false);
				setValueCompVisible(valComp3, false);
				valAnd.setVisible(true);
				break;
			case IS_NULL:
			case IS_NOT_NULL:
			case IS_BLANK:
			case IS_NOT_BLANK:
				setValueCompVisible(valComp0, false);
				setValueCompVisible(valComp1, false);
				setValueCompVisible(valComp2, false);
				setValueCompVisible(valComp3, false);
				valAnd.setVisible(false);
				break;
			case IN_ITEMLIST:
			case NOTIN_ITEMLIST:
				setValueCompVisible(valComp0, false);
				setValueCompVisible(valComp1, false);
				setValueCompVisible(valComp2, false);
				setValueCompVisible(valComp3, true);
				valAnd.setVisible(false);
				break;
			case MA:
			case NM:
			case LK:
			case NLK:
			case REGEXP:
			case NOT_REGEXP:
				setValueCompVisible(valComp0, false);
				setValueCompVisible(valComp1, false);
				setValueCompVisible(valComp2, true);
				setValueCompVisible(valComp3, false);
				valAnd.setVisible(false);
				break;
			default:
				setValueCompVisible(valComp0, true);
				setValueCompVisible(valComp1, false);
				setValueCompVisible(valComp2, false);
				setValueCompVisible(valComp3, false);
				valAnd.setVisible(false);
				break;
			}
		} else {
			cbOperator.setVisible(false);
			setValueCompVisible(valComp0, false);
			setValueCompVisible(valComp1, false);
			setValueCompVisible(valComp2, true);
			setValueCompVisible(valComp3, false);
			valAnd.setVisible(false);
			if (valComp2 instanceof InputElement)
				((InputElement)valComp2).setDisabled(true);
			else if (valComp2 instanceof Listbox)
				((Listbox)valComp2).setDisabled(true);
			((Hlayout)getValueCompPackDivOrDefault(valComp2).getParent()).setHflex("3");
		}
		delayResizeConditionBlock(50);
	}
	private boolean isCharColumnType(BiColumn bc) {
		if (StringUtils.equalsAny(bc.getColumnType(true).trim(), "char", "pickinput", "list", "combobox", "checkbox", "radio", "memo")) {
			if (bc.getField() != null && StringUtils.equals(bc.getField().getFieldType(), "integer"))
				return false;
			return true;
		}
		if (StringUtils.equals(bc.getColumnType().trim(), "label")) {
			if (bc.getField() != null && StringUtils.equals(bc.getField().getFieldType(), "char"))
				return true;
		}
		return false;
	}
	private String[] getTypeIntOptionList(BiColumn bc) {
		return getTypeIntOptionList(bc, sessionHelper);
	}
	public static Object indexOfOptionList(String[] list, Object o) {
		int pos = ArrayUtils.indexOf(list, o);
		if (pos >= 0)
			return pos;
		return o;
	}
	public static String getOptionLabel(String[] list, String s) {
		if (list != null && NumberUtils.isDigits(s)) {
			int vi = NumberUtils.toInt(s);
			if (vi >= 0 && vi < list.length)
				return list[vi];
		}
		return s;
	}
	public static String getOptionLabelForComma(String[] list, String str) {
		String[] ss = str.split(",", -1);
		StringBuilder sb = new StringBuilder();
		if (list != null) {
			for (String s : ss) {
				if (NumberUtils.isDigits(s)) {
					int vi = NumberUtils.toInt(s);
					if (vi >= 0 && vi < list.length) {
						if (sb.length() > 0)
							sb.append(",");
						sb.append(list[vi]);
					}
					else
						return str;
				}
				else
					return str;
			}
		}
		return StringUtils.defaultIfBlank(sb.toString(), str);
	}
	private long customPredicateDivIdNum;
	Hlayout createCustomPredicateConditionBlockRow(/*final Window win*/final Component win, final Condition condition) {
		((Label)win.getAttribute("hintLabel")).setVisible(false);
		final Vlayout parent = (Vlayout) win.getAttribute("vlRows");
		final List<Hlayout> blockRowCompList = blockWinMap.get(win);
		Div divConnector = null;
		if (!blockRowCompList.isEmpty())
			divConnector = createConnector(parent);
		final Hlayout hlRow = new Hlayout();
		final Textbox tbLabel = new Textbox("Predicate " + new String(Character.toChars(0x1F4DD)));  //emoji char
		final Hlayout hlValue = new Hlayout();
		final Div valCustom = new Div() {{
			setId("customPredicateDiv" + customPredicateDivIdNum++);
			setSclass("z-textbox");
			setStyle("white-space:nowrap;overflow:hidden;margin-right:5px");
			setDroppable("true");
		}};
		final Toolbarbutton btnUp = new Toolbarbutton() {{
			setIconSclass("z-icon-arrow-circle-o-up");
			setSclass("narrowtoolbarbutton");
			setStyle("color:#779CB1!important");
			setTooltiptext(sessionHelper.getLabel("Up"));
		}};
		final Toolbarbutton btnDown = new Toolbarbutton() {{
			setIconSclass("z-icon-arrow-circle-o-down");
			setSclass("narrowtoolbarbutton");
			setStyle("color:#779CB1!important");
			setTooltiptext(sessionHelper.getLabel("Down"));
		}};
		final Toolbarbutton btnDelete = new Toolbarbutton() {{
			setIconSclass("z-icon-trash");
			setSclass("narrowtoolbarbutton");
			setStyle("color:#779CB1!important");
			setTooltiptext(sessionHelper.getLabel("Delete"));
		}};
		hlRow.setParent(parent);
		tbLabel.setParent(hlRow);
		hlValue.setParent(hlRow);
		btnUp.setParent(hlRow);
		btnDown.setParent(hlRow);
		btnDelete.setParent(hlRow);
		valCustom.setParent(hlValue);
		hlRow.setHflex("1");
		tbLabel.setHflex("1");
		hlValue.setHflex("3");
		tbLabel.setReadonly(true);
		valCustom.setHflex("1");
		hlRow.setAttribute("oldCondition", condition);
		hlRow.setAttribute("connectorComp", divConnector);
		hlRow.setAttribute("operatorComp", null);
		hlRow.setAttribute("labelComp", tbLabel);
		hlRow.setAttribute("valueComp0", valCustom);
		hlRow.setAttribute("valueComp1", null);
		hlRow.setAttribute("valueComp2", null);
		hlRow.setAttribute("valueComp3", null);
		hlRow.setAttribute("valueAnd", null);
		hlRow.setAttribute("biResult", null);
		hlRow.setAttribute("biColumn", null);
		blockRowCompList.add(hlRow);
		btnUp.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("clicked up:%s", event);
				swapBlockRow(blockRowCompList, hlRow, parent, false);
			}
		});
		btnDown.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("clicked down:%s", event);
				swapBlockRow(blockRowCompList, hlRow, parent, true);
			}
		});
		btnDelete.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				Component connectorComp = (Component) hlRow.getAttribute("connectorComp");
				if (connectorComp != null)
					parent.removeChild(connectorComp);
				parent.removeChild(hlRow);
				blockRowCompList.remove(hlRow);
				if (!blockRowCompList.isEmpty()) {
					connectorComp = (Component) blockRowCompList.iterator().next().getAttribute("connectorComp");
					if (connectorComp != null)
						parent.removeChild(connectorComp);
				} else {
					((Label)win.getAttribute("hintLabel")).setVisible(true);
				}
				makeCustomCondition();
			}
		});
		if(sessionHelper.isMobileDevice() || isEmbedMode) {
			btnUp.setVisible(false);
			btnDown.setVisible(false);
			btnDelete.setVisible(false);
		}
		valCustom.addEventListener("onValueChange", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event %s, data:%s", event, event.getData());
				JSONObject json = (JSONObject)event.getData();
				String d = URLDecoder.decode((String)json.get("value"), "UTF-8");
				UniLog.log1("d:%s", d);
				hlRow.setAttribute("customValue", d);
				makeCustomCondition();
			}
		});
		valCustom.addEventListener(Events.ON_DROP, new EventListener<DropEvent>(){
			@Override
			public void onEvent(DropEvent event) throws Exception {
				UniLog.log("valCustom onDrop " + event 
						+ ",area:" + event.getArea() + ",keys:" + event.getKeys() + ",data:" + event.getData() 
						+ ",areacomponent:" + event.getAreaComponent() + ",dragged:" + event.getDragged() 
						+ ",X:" + event.getX() + ",Y:" + event.getY() + ",pageX:" + event.getPageX() + ",pageY:" + event.getPageY() + ",keys:" + event.getKeys());
				Component dragComp = event.getDragged();
				if (dragComp != null) {
					BiColumn bc = (BiColumn)dragComp.getAttribute("biColumn");
					if (bc != null) {
						ZkUtil.js("zkAdvSearchAppendPredicateHtml('%s', '%s', %d, %d)", 
								valCustom.getId(), StringEscapeUtils.escapeJavaScript(makeCustomPredicateFieldHtml(getBiColumnTranslateName(bc)/*bc.getEngName()*/, bc.getLabel())), event.getPageX(), event.getPageY());
					}
					//resizeConditionBlock();
					//makeCustomCondition();
				}
			}
		});
		bindCustomPredicateElement(hlRow);
		return hlRow;
	}
	Hlayout createConditionBlockRow(/*final Window win*/final Component win, final BiResult br, final BiColumn bc, final Condition condition) {
		((Label)win.getAttribute("hintLabel")).setVisible(false);
		final Vlayout parent = (Vlayout) win.getAttribute("vlRows");
		final List<Hlayout> blockRowCompList = blockWinMap.get(win);
		Div divConnector = null;
		if (!blockRowCompList.isEmpty())
			divConnector = createConnector(parent);
		final Hlayout hlRow = new Hlayout();
		final Textbox tbLabel = new Textbox();
		final Combobox cbOperator = new Combobox();
		//final Hlayout hlValue = new Hlayout();
		final Div hlValue = new Div() {{
			setStyle("display:flex");
		}};
		final XulElement valComp0 = buildInputComp(bc, false, false);
		final XulElement valComp1 = buildInputComp(bc, false, false);
		final XulElement valComp2 = buildInputComp(bc, true, false);
		final XulElement valComp3 = buildInputComp(bc, true, true);
		final Label valAnd = new Label(sessionHelper.getLabel("and"));
		final Toolbarbutton btnUp = new Toolbarbutton() {{
			setIconSclass("z-icon-arrow-circle-o-up");
			setSclass("narrowtoolbarbutton");
			setStyle("color:#779CB1!important");
			setTooltiptext(sessionHelper.getLabel("Up"));
		}};
		final Toolbarbutton btnDown = new Toolbarbutton() {{
			setIconSclass("z-icon-arrow-circle-o-down");
			setSclass("narrowtoolbarbutton");
			setStyle("color:#779CB1!important");
			setTooltiptext(sessionHelper.getLabel("Down"));
		}};
		final Toolbarbutton btnDelete = new Toolbarbutton() {{
			setIconSclass("z-icon-trash");
			setSclass("narrowtoolbarbutton");
			setStyle("color:#779CB1!important");
			setTooltiptext(sessionHelper.getLabel("Delete"));
		}};
		hlRow.setParent(parent);
		if(sessionHelper.isMobileDevice()) {
			Vlayout vl = new Vlayout();
			vl.setHflex("1");
			vl.setParent(hlRow);
			tbLabel.setParent(vl);
			cbOperator.setParent(vl);
			hlValue.setParent(vl);
		} else {
			tbLabel.setParent(hlRow);
			cbOperator.setParent(hlRow);
			hlValue.setParent(hlRow);
		}
		for (XulElement comp : new XulElement[] {valComp0, valAnd, valComp1, valComp2, valComp3}) {
			if (comp == valAnd) {
				comp.setParent(hlValue);
				comp.setStyle("font-size:8pt;margin-right:5px");
			} else if (comp instanceof Listbox) {
				Div div = new Div();
				//div.setHflex("1");
				div.setStyle("flex:auto;margin-right:5px");
				div.setParent(hlValue);
				div.setSclass("zkbi-advsearch-select2");
				comp.setParent(div);
				comp.setAttribute("packDiv", div);
			} else {
				comp.setParent(hlValue);
				//comp.setHflex("1");
				comp.setStyle("flex:auto;margin-right:5px");
			}
		}
		btnUp.setParent(hlRow);
		btnDown.setParent(hlRow);
		btnDelete.setParent(hlRow);
		hlRow.setHflex("1");
		if(sessionHelper.isMobileDevice()) {
			tbLabel.setWidth("100%");
			cbOperator.setWidth("100%");
		} else {
			tbLabel.setHflex("1");
			cbOperator.setHflex("1");
		}
		hlValue.setHflex("2");
		tbLabel.setReadonly(true);
		for (final Map.Entry<ConditionOp, Pair<String, String>> entry : operatorMap.entrySet()) {
			boolean flag = false;
			switch (entry.getKey()) {
			case LK:
			case NLK:
			case IS_BLANK:
			case IS_NOT_BLANK:
			case REGEXP:
			case NOT_REGEXP:
				if (isCharColumnType(bc))
					flag = true;
				break;
			case GT:
			case LT:
			case GE:
			case LE:
			case BETWEEN:
			case NOT_BETWEEN:
				//if (!isCharColumnType(bc)) //update at 2024-07-15, char field can use
					flag = true;
				break;
			default:
				flag = true;
				break;
			}
			if (flag) {
				cbOperator.appendChild(new Comboitem(){{
					setValue(entry.getKey());
					setLabel(entry.getValue().getLeft());
					setDescription(entry.getValue().getRight());
				}});
			}
		}
		cbOperator.setSelectedIndex(0);
		cbOperator.setReadonly(true);
		//if (isEmbedMode) //update 230427: Allow user to change embed search operator. It make the embed query screen more flexible.
		//	cbOperator.setDisabled(true);
		visibleInputComps(cbOperator, valComp0, valComp1, valAnd, valComp2, valComp3);
		//tbLabel.setValue(bc.getEngName());
		tbLabel.setValue(getBiColumnTranslateName(bc));
		hlRow.setAttribute("oldCondition", condition);
		hlRow.setAttribute("connectorComp", divConnector);
		hlRow.setAttribute("operatorComp", cbOperator);
		hlRow.setAttribute("labelComp", tbLabel);
		hlRow.setAttribute("valueComp0", valComp0);
		hlRow.setAttribute("valueComp1", valComp1);
		hlRow.setAttribute("valueComp2", valComp2);
		hlRow.setAttribute("valueComp3", valComp3);
		hlRow.setAttribute("valueAnd", valAnd);
		hlRow.setAttribute("biResult", br);
		hlRow.setAttribute("biColumn", bc);
		blockRowCompList.add(hlRow);
		btnUp.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("clicked up:%s", event);
				swapBlockRow(blockRowCompList, hlRow, parent, false);
			}
		});
		btnDown.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("clicked down:%s", event);
				swapBlockRow(blockRowCompList, hlRow, parent, true);
			}
		});
		btnDelete.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				Component connectorComp = (Component) hlRow.getAttribute("connectorComp");
				if (connectorComp != null)
					parent.removeChild(connectorComp);
				parent.removeChild(hlRow);
				blockRowCompList.remove(hlRow);
				if (!blockRowCompList.isEmpty()) {
					connectorComp = (Component) blockRowCompList.iterator().next().getAttribute("connectorComp");
					if (connectorComp != null)
						parent.removeChild(connectorComp);
				} else {
					((Label)win.getAttribute("hintLabel")).setVisible(true);
				}
				makeCustomCondition();
			}
		});
		if(sessionHelper.isMobileDevice() || isEmbedMode) {
			btnUp.setVisible(false);
			btnDown.setVisible(false);
			btnDelete.setVisible(false);
		}
		cbOperator.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("cbOperator " + event);
				visibleInputComps(cbOperator, valComp0, valComp1, valAnd, valComp2, valComp3);
				makeCustomCondition();
			}
		});
		XulElement[] vcs = new XulElement[]{valComp0, valComp1, valComp2, valComp3};
		for (final XulElement vc : vcs) {
			if (vc instanceof ZkJxQueryInput) {
				final ZkJxQueryInput vcz = (ZkJxQueryInput)vc;
				vcz.setEventListenerCallback(new EventListenerCallback(){
					@Override
					public void callback(Event event) {
						UniLog.log("vc ZkJxQueryInput " + event);
						makeCustomCondition();
					}
				});
				vcz.addEventListener(Events.ON_OPEN, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log("vc ZkJxQueryInput open " + event);
						if (vcz.isOpen()) {
							switch (vcz.getQueryInputType()) {
							case ZkJxQueryInput.TYPE_DATE:
							case ZkJxQueryInput.TYPE_DATETIME:
								makeCustomCondition();
								break;
							}
						}
					}
				});
			} else if (vc instanceof Listbox) {
				vc.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Listitem, String>>(){
					@Override
					public void onEvent(SelectEvent<Listitem, String> event) throws Exception {
						UniLog.log("vc select " + event);
						delayResizeConditionBlock(50);
						makeCustomCondition();
					}
				});
			} else {
				vc.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log("vc " + event);
						makeCustomCondition();
					}
				});
			}
			//Ignore the enter event
			vc.addEventListener(Events.ON_OK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log("ok event for " + event.getName());
				}
			});
		}
		if (!sessionHelper.isMobileDevice()) {
			win.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
				@Override
				public void onEvent(AfterSizeEvent event) throws Exception {
					int width = (event.getWidth() - 10 - 19) / 4 * 2 - 5;
					UniLog.log1("event:%s, winwidth:%d, width:%d, hlValue:%s", event, event.getWidth(), width, hlValue.getUuid());
					if (width > 0)
						ZkUtil.delayJs(parentComp, null, 50, "$('#%s').css('max-width', '%dpx')", hlValue.getUuid(), width);
					else
						ZkUtil.delayJs(parentComp, null, 50, "$('#%s').css('max-width', 'initial')", hlValue.getUuid());
				}
			});
		}
		return hlRow;
	}
	private void swapBlockRow(List<Hlayout> blockRowCompList, Hlayout hlRow, Vlayout parent, boolean isDown) {
		int i = blockRowCompList.indexOf(hlRow);
		if ((isDown && i < blockRowCompList.size() - 1) || (!isDown && i > 0)) {
			int i0 = i + (isDown ? 1 : -1);
			Hlayout hlRow0 = blockRowCompList.get(i0);
			Component connectorComp = (Component) hlRow.getAttribute("connectorComp");
			Component connectorComp0 = (Component) hlRow0.getAttribute("connectorComp");
			Div div0 = new Div();
			parent.insertBefore(div0, hlRow);
			parent.removeChild(hlRow);
			parent.insertBefore(hlRow, hlRow0);
			parent.removeChild(hlRow0);
			parent.insertBefore(hlRow0, div0);
			parent.removeChild(div0);
			hlRow.setAttribute("connectorComp", connectorComp0);
			hlRow0.setAttribute("connectorComp", connectorComp);
			for (Hlayout hl : new Hlayout[] {hlRow, hlRow0}) {
				if (hl.getAttribute("biColumn") != null) {
					for (int j = 0; j <= 3; j++) {
						Component valueComp = (Component) hl.getAttribute("valueComp" + j);
						if (valueComp != null && valueComp.isVisible() && valueComp instanceof Listbox)
							resetupS2Comp((Listbox)valueComp);
					}
				} else {
					bindCustomPredicateElement(hl);
					setCustomPredicateText(hl, StringUtils.defaultString((String)hl.getAttribute("customValue")));
				}
			}
			Collections.swap(blockRowCompList, i, i0);
			makeCustomCondition();
		}
	}
	private void resizeConditionBlock() {
		for (Component win : blockWinMap.keySet())
			Clients.resize(win);
	}
	private void delayResizeConditionBlock(final int delay) {
		final Timer timer = new Timer();
		timer.setPage(parentComp.getPage());
		timer.setDelay(delay);
		timer.setRepeats(false);
		timer.addEventListener(Events.ON_TIMER, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("delayResizeConditionBlock %d event:%s", delay, event);
				resizeConditionBlock();
				timer.setRunning(false);
				timer.detach();
			}
		});
		timer.setRunning(true);
	}
	private void clearConditionBlock() {
		while (blockWinMap.size() > 0) {
			Component comp = blockWinMap.keySet().iterator().next();
			if (comp instanceof Window) {
				Window win = (Window)comp;
				removeConditionBlock(win);
				vlCon1.removeChild(win);
			}
		}
	}
	/***
	 * @param p_win null set first block as active
	 */
	private void setActiveConditionBlockWin(Window p_win) {
		if (activeConditionBlockWin == p_win) {
			UniLog.log1("same win, ignore update");
			return;
		}
		
		if (activeConditionBlockWin != null) {
			activeConditionBlockWin.setSclass("");
		}
				
		if (p_win != null) {
			activeConditionBlockWin = p_win;
		}
		else {
			//if win is null, set first window as active
			Window nextCandidateWin = null;
			for (Component tmpWin : blockWinMap.keySet()) {
				if (tmpWin instanceof Window && tmpWin != activeConditionBlockWin) {
					nextCandidateWin = (Window)tmpWin;
					break;
				}
			}
			activeConditionBlockWin = nextCandidateWin;
		}
		if (activeConditionBlockWin != null) {
			activeConditionBlockWin.setSclass("zkbi-active-window");
		}
	}
	private void removeConditionBlock(Window winBlock) {
				
		if (winBlock == activeConditionBlockWin) {
			setActiveConditionBlockWin(null);
		}
		Component comp = (Component) winBlock.getAttribute("connectorComp");
		if (comp != null)
			vlCon1.removeChild(comp);
		blockWinMap.remove(winBlock);
		if (!blockWinMap.isEmpty()) {
			comp = (Component) blockWinMap.keySet().iterator().next().getAttribute("connectorComp");
			if (comp != null)
				vlCon1.removeChild(comp);
		}
	}
	private Div createEmbedConditionBlockNoTitle(Div divParent) {
		Div divConnector = null;
		if (!blockWinMap.isEmpty())
			divConnector = createConnector(divParent);
		final Div win = new Div();
		final Label hintLabel = new Label();
		hintLabel.setSclass("zkbi-placeholder-dark");
		final Vlayout vlRows = new Vlayout();
		win.setParent(divParent);
		hintLabel.setParent(win);
		vlRows.setParent(win);
		vlRows.setHflex("1");
		win.setAttribute("hintLabel", hintLabel);
		win.setAttribute("vlRows", vlRows);
		blockWinMap.put(win, new ArrayList<Hlayout>());
		win.setAttribute("connectorComp", divConnector);
		return win;
	}
	private Groupbox createEmbedConditionBlockHasTitle(Div divParent) {
		Div divConnector = null;
		if (!blockWinMap.isEmpty())
			divConnector = createConnector(divParent);
		final Groupbox win = new Groupbox();
		final Label hintLabel = new Label();
		hintLabel.setSclass("zkbi-placeholder-dark");
		final Vlayout vlRows = new Vlayout();
		win.setParent(divParent);
		hintLabel.setParent(win);
		vlRows.setParent(win);
		vlRows.setHflex("1");
		win.setAttribute("hintLabel", hintLabel);
		win.setAttribute("vlRows", vlRows);
		win.setTitle(sessionHelper.getLabel("Condition Block") + " " + (blockWinMap.size() + 1));
		blockWinMap.put(win, new ArrayList<Hlayout>());
		win.setAttribute("connectorComp", divConnector);
		return win;
	}
	private Window createConditionBlock(boolean isMakeCustomCondition) {
		Div divConnector = null;
		if (!blockWinMap.isEmpty())
			divConnector = createConnector(vlCon1);
		final Window win = new Window();
		if (activeConditionBlockWin == null) {
			setActiveConditionBlockWin(win);
		}
		final Label hintLabel = new Label(sessionHelper.getLabel("Please drag data field here to build condition block"));
		hintLabel.setSclass("zkbi-placeholder-dark");
		final Vlayout vlRows = new Vlayout();
		win.setParent(vlCon1);
		hintLabel.setParent(win);
		vlRows.setParent(win);
		vlRows.setHflex("1");
		win.setAttribute("hintLabel", hintLabel);
		win.setAttribute("vlRows", vlRows);
		win.setClosable(true);
		win.setTitle(sessionHelper.getLabel("Condition Block") + " " + (blockWinMap.size() + 1));
		win.setDroppable("true");
		win.setTooltiptext(sessionHelper.getLabel("Please drag field here"));
		win.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				//UniLog.log1("onclick activeConditionBlockWin:%s win:%s", activeConditionBlockWin, win);
				setActiveConditionBlockWin(win);
			}
		});
		win.addEventListener(Events.ON_DROP, new EventListener<DropEvent>(){
			@Override
			public void onEvent(DropEvent event) throws Exception {
				UniLog.log("createConditionBlock onDrop " + event 
						+ ",area:" + event.getArea() + ",keys:" + event.getKeys() + ",data:" + event.getData() 
						+ ",areacomponent:" + event.getAreaComponent() + ",dragged:" + event.getDragged());
				Component dragComp = event.getDragged();
				if (dragComp != null) {
					BiColumn bc = (BiColumn)dragComp.getAttribute("biColumn");
					if (bc != null)
						createConditionBlockRow(win, (BiResult)dragComp.getAttribute("biResult"), bc, null);
					else
						createCustomPredicateConditionBlockRow(win, null);
					resizeConditionBlock();
					makeCustomCondition();
				}
			}
		});
		win.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("conditionblock close " + event);
				removeConditionBlock(win);
				if (blockWinMap.isEmpty())
					createConditionBlock(true);
				else {
					resizeConditionBlock();
					makeCustomCondition();
				}
			}
		});
		blockWinMap.put(win, new ArrayList<Hlayout>());
		win.setAttribute("connectorComp", divConnector);
		resizeConditionBlock();
		if (isMakeCustomCondition)
			makeCustomCondition();
		return win;
	}
	private String getBiColumnTranslateName(BiColumn bc) {
		return ZkBiTranslateHelper.getText(sessionHelper, bc.getCellFullName(), "LABEL", sessionHelper.getLabel(bc));
	}
	private void makeCustomCondition() {
		customParamValue = makeCustomCondition(false, false);
		callback.customConditionChanged(customParamValue);
	}
	public String makeCustomCondition(boolean useBcEnglishName, boolean ignoreBlankValue) {
		try {
			UniLog.log1("blockWinMap size:%d, ignoreBlankValue:%b, useBcEnglishName:%b", blockWinMap.size(), ignoreBlankValue, useBcEnglishName);
			Condition winCondition = null;
			for (Map.Entry<Component, List<Hlayout>> entry : blockWinMap.entrySet()) {
				Div divConnector = (Div) entry.getKey().getAttribute("connectorComp");
				Integer winConnector = divConnector != null ? (Integer) divConnector.getAttribute("connector") : null;
				UniLog.log1("win connector: " + winConnector + ",size:" + entry.getValue().size());
				Condition rowCondition = null;
				for (Hlayout hlRow : entry.getValue()) {
					//BiResult br = (BiResult) hlRow.getAttribute("biResult");
					BiColumn bc = (BiColumn) hlRow.getAttribute("biColumn");
					Condition condition = null;
					if (bc != null) {
						Combobox opComp = (Combobox) hlRow.getAttribute("operatorComp");
						XulElement valueComp0 = (XulElement) hlRow.getAttribute("valueComp0");
						XulElement valueComp1 = (XulElement) hlRow.getAttribute("valueComp1");
						XulElement valueComp2 = (XulElement) hlRow.getAttribute("valueComp2");
						XulElement valueComp3 = (XulElement) hlRow.getAttribute("valueComp3");
						Condition oldCondition = (Condition) hlRow.getAttribute("oldCondition");
						//Expression fExp = new Expression(new Variable(bc.getLabel(),null,null));
						if (useBcEnglishName)
							condition = getInputCondition(getBiColumnTranslateName(bc)/*bc.getEngName()*/, bc, valueComp0, opComp, valueComp1, valueComp2, valueComp3, oldCondition);
						else {
							condition = getInputCondition(bc, valueComp0, opComp, valueComp1, valueComp2, valueComp3, oldCondition);
							ConditionOp operator = getConditionOp(opComp);
							UniLog.log1("condition:%s, operator:%s", condition, operator);
							if (ignoreBlankValue && operator != ConditionOp.IS_BLANK && operator != ConditionOp.IS_NOT_BLANK) {
								try {
									switch (condition.get_operator()) {
										case Condition.COMPARE_OP_EQ:
											String rExp = expression2CompValue(condition.get_rightExpression().toString(), bc);
											UniLog.log1("condition:%s,get_isPredicate:%b,get_leftExpression:%s,get_rightExpression:%s", condition.toString(), condition.get_isPredicate(), condition.get_leftExpression(), rExp);
											if (StringUtils.isBlank(rExp))
												condition = null;
											break;
										case Condition.COMPARE_OP_IN_ITEMLIST:
										case Condition.COMPARE_OP_REGEXP:
											if (condition.get_rightExpressionList().isEmpty()) {
												condition = null;
											} else if (condition.get_rightExpressionList().size() == 1) {
												rExp = expression2CompValue(condition.get_rightExpressionList().get(0).toString(), bc);
												UniLog.log1("condition:%s,get_isPredicate:%b,get_leftExpression:%s,get_rightExpressionList:%s", condition.toString(), condition.get_isPredicate(), condition.get_leftExpression(), rExp);
												if (StringUtils.isBlank(rExp))
													condition = null;
											}
											break;
									}
								}
								catch (Exception e) {
									UniLog.log(e);
								}
							}
						}
					}
					else {
						condition = parseCondition((String)hlRow.getAttribute("customValue"));
					}
					divConnector = (Div) hlRow.getAttribute("connectorComp");
					Integer connector = divConnector != null ? (Integer) divConnector.getAttribute("connector") : null;
					UniLog.log1("hlrow condition: " + condition + ",connector:" + connector);
					if (rowCondition != null) {
						if (connector > 0 && condition != null)
							rowCondition = new Condition(rowCondition, connector, condition);
					} else
						rowCondition = condition;
				}
				UniLog.log1("hlrows condition: " + rowCondition);
				if (rowCondition != null) {
					if (winCondition != null) {
						if (winConnector > 0)
							winCondition = new Condition(winCondition, winConnector, rowCondition);
					} else
						winCondition = rowCondition;
				}
			}
			UniLog.log1("win condition: " + winCondition);
			return winCondition != null ? winCondition.toString() : ""; 
		} catch (Exception e) {
			UniLog.log(e);
			return "";
		}
	}

	public String getCustomParam() {
		return customParamValue;
	}

	public String getSelectedPreset() {
		return conditionPresetListbox.getSelectedItem().getValue();
	}

	public int getLimit() {
		return ibLimit.getValue();
	}
	
	public MessageboxDlg showDialog() {
		MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(sessionHelper.getLabel("Advanced Search"), create(),
				new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
				new String[]{sessionHelper.getBtLabel("Search"), sessionHelper.getBtLabel("Close")},
				parentComp, callback.getClickDialogButtonEvent(this));
		if(sessionHelper.isMobileDevice()) {
		//	ZkUtil.appendStyle(dlg, "min-width:800px;min-height:400px;");
			ZkUtil.appendStyle(dlg, "width:100%;height:100%");
		} else {
			dlg.setMaximizable(true);
			dlg.setSizable(true);
			dlg.setWidth("55%");
			dlg.setHeight("50%");
			ZkUtil.appendStyle(dlg, "min-width:800px;min-height:400px;");
		}
		dlg.doHighlighted();
		if (showPresetComp && sessionHelper.getAllowS2Listbox()) {
			conditionPresetListbox.setAttribute("select2-enable", "Y");
			//ZkUtil.setupSelect2(conditionPresetListbox, true);
			ZkUtil.setupSelect2(conditionPresetListbox, true, false);
		}
		return dlg;
	}
}
