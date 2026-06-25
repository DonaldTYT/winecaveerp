package com.uniinformation.zkbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Space;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.MessageboxDlg;
import org.zkoss.zul.impl.MessageboxDlg.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiUpdateBiView {
	private static final int LONGOP_DEFAULT_DELAY = 50;

	private BiResult result;
	private Component parentComp;
	private SessionHelper sessionHelper;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private Map<String, BiResult> biResultMap = new LinkedHashMap<String, BiResult>(); //br.getView().getName(), br
	private Map<String, BiColumn> biColumnMap = new LinkedHashMap<String, BiColumn>(); //bc.getLabel(), bc
	private Map<String, String> biColumnResultMap = new HashMap<String, String>(); //bc.getLabel(), br.getView().getName()

	private Map<String, Vlayout> biResultViewMap = new LinkedHashMap<String, Vlayout>(); //br.getView().getName(), Vlayout
	private Map<String, Window> biColumnBlockMap = new LinkedHashMap<String, Window>(); //bc.getLabel(), Window
	
	private Map<String, Object> bcFieldMap = new HashMap<String, Object>();
	private Map<String, Boolean> bcFieldUpdatedMap = new HashMap<String, Boolean>();
	private Map<String, Boolean> bcFieldInJsonMap = new HashMap<String, Boolean>(); //bc.getLabel() - classField, removeInJsonFlag
	
	private MessageboxDlg dialog;
	private Window window;
	private Tabbox tbFieldList;
	private Groupbox groupConList;
	private Button btnLoad, btnSave, btnImport, btnExport, btnClose;
	
	private String selectedBrName;

	public ZkBiUpdateBiView(BiResult result, Component parentComp, SessionHelper sessionHelper) {
		this.result = result;
		this.parentComp = parentComp;
		this.sessionHelper = sessionHelper;
		UniLog.log1("ZkBiUpdateBiView getCustomBiViewBase:%s, agent:%s", sessionHelper.getCustomBiViewBase(), sessionHelper.getAgent());
	}
	
	private Window createWindow() {
		tbFieldList = new Tabbox() {{
			if (sessionHelper.isMobile()) {
				setHflex("1");
				setVflex("2");
			} else {
				setWidth("170px");
				setVflex("1");
			}
		}};

		groupConList = new Groupbox() {{
			setMold("3d");
			setClosable(false);
			setTitle("BiColumn List");
			setDroppable("true");
			setHflex("1");
			setVflex(sessionHelper.isMobile() ? "3" : "1");
		}};
		
		Component layout;
		if (sessionHelper.isMobile()) {
			layout = new Vlayout() {{
				setSclass("zkbi-advs-condition");
				appendChild(tbFieldList);
				appendChild(groupConList);
				setHflex("1");
				setVflex("1");
			}};
		} else {
			layout = new Hlayout() {{
				setSclass("zkbi-advs-condition");
				appendChild(tbFieldList);
				appendChild(groupConList);
				setHflex("1");
				setVflex("1");
			}};
		}
		final Component layout1 = layout;

		window = new Window() {{
			setStyle("padding:0px !important; user-select: none;");
			setClass("zkbi-advsearch-g2");
			appendChild(layout1);
		}};

		groupConList.addEventListener(Events.ON_DROP, new EventListener<DropEvent>() {
			@Override
			public void onEvent(DropEvent event) throws Exception {
				UniLog.log("onDrop " + event 
						+ ",area:" + event.getArea() + ",keys:" + event.getKeys() + ",data:" + event.getData() 
						+ ",areacomponent:" + event.getAreaComponent() + ",dragged:" + event.getDragged());
				Component dragComp = event.getDragged();
				if (dragComp != null) {
					BiColumn bc = (BiColumn)dragComp.getAttribute("biColumn");
					if (bc != null) {
						if (biColumnBlockMap.containsKey(bc.getLabel()))
							showErrMsg("Block '%s' already exists", bc.getLabel());
						else {
							createBiColumnBlock(biColumnResultMap.get(bc.getLabel()), bc.getLabel(), null, false);
							checkBtnSaveDisabled();
						}
					}
				}
			}
		});
		return window;
	}
	
	private void createBiResultView() {
		final Tabs ts = new Tabs();
		final Tabpanels tps = new Tabpanels();
		ts.setParent(tbFieldList);
		tps.setParent(tbFieldList);

		Vector<BiResult> resultList = new Vector<BiResult>();
		resultList.add(result);
		if(result.getSubLinks()!=null) resultList.addAll(result.getSubLinks());
		int i = 0;
		for (final BiResult br : resultList) {
			final String brName = br.getView().getName();
			UniLog.log1("biView name:%s", brName);
			Tab t = new Tab(brName);
			t.setParent(ts);
			Tabpanel tp = new Tabpanel();
			tp.setParent(tps);
			Grid grid = new Grid();
			Rows rows = new Rows();
			grid.setParent(tp);
			grid.setVflex("1");
			grid.appendChild(rows);
			biResultMap.put(brName, br);
			for (final BiColumn bc : br.getColumns()) {
				//if(br != result && (bc.getField() == null )) continue;
				Row row = new Row();
				row.setParent(rows);
				row.setAttribute("biResult", br);
				row.setAttribute("biColumn", bc);
				row.setDraggable("true");
				final Label lblName = new Label(bc.getLabel());
				final Toolbarbutton btnAdd = new Toolbarbutton();
				lblName.setHflex("1");
				btnAdd.setIconSclass("z-icon-plus-circle");
				btnAdd.setStyle("color:#779CB1!important");
				btnAdd.setSclass("narrowtoolbarbutton");
				btnAdd.setTooltiptext("Add data field to condition block");
				btnAdd.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (biColumnBlockMap.containsKey(bc.getLabel()))
							showErrMsg("Block '%s' already exists", bc.getLabel());
						else {
							createBiColumnBlock(brName, bc.getLabel(), null, false);
							checkBtnSaveDisabled();
						}
					}
				});
				row.appendChild(new Hlayout() {{
					appendChild(lblName);
					appendChild(btnAdd);
				}});
				biColumnMap.put(bc.getLabel(), bc);
				biColumnResultMap.put(bc.getLabel(), brName);
			}

			final Vlayout vlCon = new Vlayout();
			vlCon.setVflex("1");
			vlCon.setHflex("1");
			vlCon.setStyle("overflow:auto;");
			groupConList.appendChild(vlCon);
			if (i == 0) {
				vlCon.setVisible(true);
				groupConList.setTitle(br.getView().getName());
				selectedBrName = brName;
			} else
				vlCon.setVisible(false);
			biResultViewMap.put(br.getView().getName(), vlCon);

			t.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log("tab select:" + event);
					new ZkBiAbstractLongOp(dialog, "Loading...", LONGOP_DEFAULT_DELAY){
						@Override
						public ReturnMsg longOp() {
							for (Vlayout layout : biResultViewMap.values())
								layout.setVisible(layout == vlCon);
							groupConList.setTitle(br.getView().getName());
							selectedBrName = brName;
							return null;
						}
					};
				}
			});
			i++;
		}
	}

	private File getJsonFile(String brName) {
		String baseName = sessionHelper.getCustomBiViewBase();
		if (baseName.contains(","))
			baseName = baseName.split(",")[0];
		return new File((baseName + "/" + brName).replaceAll("\\s+", "").replaceAll("\\.", "/") +".json");
	}

	private void loadBcField(String brName, Reader reader, boolean isFromImportJsonFile) throws Exception {
		JsonParser parser = new JsonParser();
		JsonArray ja = parser.parse(reader).getAsJsonArray();
		for (JsonElement je : ja) {
			if (!je.isJsonObject()) continue;
			JsonObject jsonObj = je.getAsJsonObject();
			if (jsonObj.get("label") == null) {
				UniLog.log1("ignore. label is null");
				continue;
			}
			String label = jsonObj.get("label").getAsString();
			if (StringUtils.isBlank(label)) {
				UniLog.log1("ignore. label is blank");
				continue;
			}
			createBiColumnBlock(brName, label, jsonObj, isFromImportJsonFile);
		}
	}
	
	private void loadBcField() {
		clearBcFieldMap();
		clearBcFieldInJsonMap();
		clearBcFieldUpdatedMap();
		clearBiColumnBlock();
		for (Map.Entry<String, BiResult> entry : biResultMap.entrySet()) {
			BiResult br = entry.getValue();
			String brName = br.getView().getName();
			try {
				File file = getJsonFile(brName);
				if (file.exists()) {
					UniLog.log1("json file %s exists", file.getPath());
					BufferedReader reader = new BufferedReader(new FileReader(file));
					loadBcField(brName, reader, false);
					reader.close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		btnSave.setDisabled(true);
	}

	private int saveBcField() {
		try {
			boolean updated1 = false;
			for (Map.Entry<String, Vlayout> entry : biResultViewMap.entrySet()) {
				String brName = entry.getKey();
				Vlayout panelView = entry.getValue();
				boolean updated = false;
				JsonArray jsonArr = new JsonArray();
				for (Component win : panelView.getChildren()) {
					JsonObject jsonObj = new JsonObject();
					String bcLabel = (String) win.getAttribute("bcLabel");
					jsonObj.addProperty("label", bcLabel);
					List<Component> compList = new ArrayList<Component>();
					getAllInputComponent(win, compList);
					for (Component comp : compList) {
						String classField = (String) comp.getAttribute("classField");
						String fieldType = (String) comp.getAttribute("fieldType");
						Object oldValue = getBcField(bcLabel, classField);
						if (classField != null) {
							if (comp instanceof Combobox) {
								String cbStr = ((Combobox)comp).getValue();
								if (oldValue == null)
									oldValue = "";
								if (ObjectUtils.notEqual(cbStr, oldValue)) {
									jsonObj.addProperty(classField, cbStr);
									putBcField(bcLabel, classField, cbStr);
									putBcFieldInJson(bcLabel, classField);
									updated = true;
								} else if (isBcFieldInJson(bcLabel, classField))
									jsonObj.addProperty(classField, cbStr);
							}
							else if (comp instanceof Intbox) {
								Integer ibInt = ((Intbox)comp).getValue();
								if (ibInt == null)
									ibInt = 0;
								if (ObjectUtils.notEqual(ibInt, oldValue)) {
									jsonObj.addProperty(classField, ibInt);
									putBcField(bcLabel, classField, ibInt);
									putBcFieldInJson(bcLabel, classField);
									updated = true;
								} else if (isBcFieldInJson(bcLabel, classField))
									jsonObj.addProperty(classField, ibInt);
							}
							else if (comp instanceof Textbox) {
								String tbStr = ((Textbox)comp).getValue();
								if (fieldType.equals("[Ljava.lang.String;")) {
									JsonArray arr = null;
									String[] ss = StringUtils.isNotBlank(tbStr) ? tbStr.split(",") : (oldValue != null ? new String[0] : null);
									String[] oss = (String[])oldValue;
									if (ss != null) {
										arr = new JsonArray();
										for (String s : ss)
											arr.add(s);
									}
									boolean flag = false;
									if (ss != null) {
										if (oss != null) {
											if (!Arrays.equals(ss, oss))
												flag = true;
										} else
											flag = true;
									} else if (oss != null)
										flag = true;
									if (flag) {
										jsonObj.add(classField, arr);
										putBcField(bcLabel, classField, arr);
										putBcFieldInJson(bcLabel, classField);
										updated = true;
									} else if (isBcFieldInJson(bcLabel, classField))
										jsonObj.add(classField, arr);
								}
								else {
									if (oldValue == null)
										oldValue = "";
									if (ObjectUtils.notEqual(tbStr, oldValue)) {
										jsonObj.addProperty(classField, tbStr);
										putBcField(bcLabel, classField, tbStr);
										putBcFieldInJson(bcLabel, classField);
										updated = true;
									} else if (isBcFieldInJson(bcLabel, classField))
										jsonObj.addProperty(classField, tbStr);
								}
							}
							else if (comp instanceof Checkbox) {
								boolean cbBool = ((Checkbox)comp).isChecked();
								if (ObjectUtils.notEqual(cbBool, oldValue)) {
									jsonObj.addProperty(classField, cbBool);
									putBcField(bcLabel, classField, cbBool);
									putBcFieldInJson(bcLabel, classField);
									updated = true;
								} else if (isBcFieldInJson(bcLabel, classField))
									jsonObj.addProperty(classField, cbBool);
							}
						}
					}
					jsonArr.add(jsonObj);
				}
				if (!updated) {
					for (Map.Entry<String, Boolean> entry1 : bcFieldInJsonMap.entrySet()) {
						String l = getBiColumnLabelByBcFieldName(entry1.getKey());
						if (biColumnResultMap.get(l).equals(brName) && entry1.getValue()) {
							updated = true;
							break;
						}
					}
				}
				if (updated) {
					File file = getJsonFile(brName);
					if (file.exists()) {
						SimpleDateFormat f = new SimpleDateFormat("yyyyMMddhhmmssSSS");
						file.renameTo(new File(file.getPath() + "." + f.format(new Date())));
					} else
						file.getParentFile().mkdirs();
					if (jsonArr.size() > 0) {
						FileWriter fw = new FileWriter(file);
						gson.toJson(jsonArr, fw);
						fw.close();
					}
					updated1 = true;
				}
				UniLog.log1("saveBiColumn:%s,%b", brName, updated);
				//UniLog.log1("saveBiColumn:%s,%b,%s", brName, updated, gson.toJson(jsonArr));
			}
			if (updated1) {
				clearBcFieldUpdatedMap();
				resetBcFieldInJson();
				btnSave.setDisabled(true);
				BiSchema.clearSchemaCache();
				showMsg("Saved successfully");
				return 0;
			}
			else {
				showErrMsg("No change, no need to write to file");
				return -2;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			showErrMsg("Save fail: " + e.toString());
			return -1;
		}
	}
	
	private void getAllInputComponent(Component parentComp, List<Component> compList) {
		for (Component child : parentComp.getChildren()) {
			String classField = (String) child.getAttribute("classField");
			if (classField != null)
				compList.add(child);
			else
				getAllInputComponent(child, compList);
		}
	}
	
	private void putBcField(String bcLabel, String classField, Object value) {
		bcFieldMap.put(bcLabel + " - " + classField, value);
	}

	private void putBcFieldInJson(String bcLabel, String classField) {
		putBcFieldInJson(bcLabel, classField, null);
	}

	private void putBcFieldInJson(String bcLabel, String classField, Boolean removeInJsonFlag) {
		String key = bcLabel + " - " + classField;
		if (removeInJsonFlag != null) {
			if (bcFieldInJsonMap.containsKey(key))
				bcFieldInJsonMap.put(key, removeInJsonFlag);
		}
		else
			bcFieldInJsonMap.put(key, false);
	}

	private void putBcFieldInJson(String bcLabel, boolean removeInJsonFlag) {
		for (String key : bcFieldInJsonMap.keySet()) {
			if (key.startsWith(bcLabel + " - "))
				bcFieldInJsonMap.put(key, removeInJsonFlag);
		}
	}

	private void putBcFieldInJsonByBrName(String brName, boolean removeInJsonFlag) {
		for (String key : bcFieldInJsonMap.keySet()) {
			String bcLabel = getBiColumnLabelByBcFieldName(key);
			if (biColumnResultMap.get(bcLabel).equals(brName))
				bcFieldInJsonMap.put(key, removeInJsonFlag);
		}
	}

	private void resetBcFieldInJson() {
		Iterator<Map.Entry<String, Boolean>> it = bcFieldInJsonMap.entrySet().iterator();
		while (it.hasNext()) {
			if (it.next().getValue())
				it.remove();
		}
	}

	private void putBcFieldUpdated(String bcLabel, String classField, boolean value) {
		bcFieldUpdatedMap.put(bcLabel + " - " + classField, value);
	}

	private void removeBcFieldUpdated(String bcLabel) {
		Iterator<Map.Entry<String, Boolean>> it = bcFieldUpdatedMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Boolean> entry = it.next();
			if (entry.getKey().startsWith(bcLabel + " - "))
				it.remove();
		}
	}

	private void removeBcFieldUpdatedByBrName(String brName) {
		Iterator<Map.Entry<String, Boolean>> it = bcFieldUpdatedMap.entrySet().iterator();
		while (it.hasNext()) {
			String l = getBiColumnLabelByBcFieldName(it.next().getKey());
			if (biColumnResultMap.get(l).equals(brName))
				it.remove();
		}
	}

	private Object getBcField(String bcLabel, String classField) {
		return bcFieldMap.get(bcLabel + " - " + classField);
	}

	private boolean isBcFieldInJson(String bcLabel, String classField) {
		return BooleanUtils.isFalse(bcFieldInJsonMap.get(bcLabel + " - " + classField));
	}

	private boolean isBcFieldUpdated() {
		if (bcFieldUpdatedMap.containsValue(true))
			return true;
		if (bcFieldInJsonMap.containsValue(true))
			return true;
		return false;
	}

	private boolean isBcFieldUpdated(String bcLabel) {
		for (Map.Entry<String, Boolean> entry : bcFieldUpdatedMap.entrySet()) {
			if (entry.getKey().startsWith(bcLabel + " - ") && entry.getValue())
				return true;
		}
		return false;
	}

	private void checkBtnSaveDisabled() {
		btnSave.setDisabled(!isBcFieldUpdated());
	}

	private void removeBiColumnBlockByBrName(String p_brName) {
		Iterator<Map.Entry<String, Window>> it = biColumnBlockMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Window> entry = it.next();
			String label = entry.getKey();
			Window block = entry.getValue();
			String brName = biColumnResultMap.get(label);
			if (p_brName == null || brName.equals(p_brName)) {
				biResultViewMap.get(brName).removeChild(block);
				it.remove();
			}
		}
	}

	private void clearBiColumnBlock() {
		removeBiColumnBlockByBrName(null);
	}
	
	private void clearBcFieldMap() {
		bcFieldMap.clear();
	}

	private void clearBcFieldInJsonMap() {
		bcFieldInJsonMap.clear();
	}

	private void clearBcFieldUpdatedMap() {
		bcFieldUpdatedMap.clear();
	}
	
	private String getBiColumnLabelByBcFieldName(String name) {
		return name.substring(0, name.indexOf(" - "));
	}

	private void createBiColumnBlock(String brName, final String bcLabel, JsonObject jsonObj, boolean isFromImportJsonFile) {
		if (biColumnBlockMap.containsKey(bcLabel)) {
			UniLog.log1("BiColumn Block '%s' already exists", bcLabel);
			return;
		}
		final BiColumn bc = biColumnMap.get(bcLabel);
		if (bc == null) {
			UniLog.log1("biColumn %s not found", bcLabel);
			return;
		}
		final Window win = new Window();
    	final Div div = new Div();
    	div.setStyle("display:flex;display:-webkit-flex;"
    			+ "flex-wrap:wrap;-webkit-wrap:wrap;"
    			+ "align-items:center;-webkit-align-items:center;");
		win.setParent(biResultViewMap.get(brName));
		div.setParent(win);
		win.setAttribute("vlDiv", div);
		win.setAttribute("bcLabel", bcLabel);
		win.setClosable(true);
		win.setTitle(bc.getLabel());
		
		Field[] fieldArray = bc.getClass().getDeclaredFields();		
		List<Component> compList = new ArrayList<Component>();
		//sortTypeNumA: 0: Boolean, 1: Select, 1: Integer, 2: String, 3: String[]
		//sortTypeNumB: 0: Boolean, 1: Select, 2: Integer, 3: String, 4: String[]
		for (final Field f : fieldArray) {
			try {
				f.setAccessible(true);
				Annotation[] annotations = f.getDeclaredAnnotations();
				boolean isExpose = false;
				boolean isExclude = false;
				for (Annotation a : annotations) {
					if (a.annotationType().getName().equals("com.google.gson.annotations.Expose")) {
						isExpose = true;
						break;
					}
				}
				final String fieldName = f.getName();
				final String fieldType = f.getType().getName();
				if (fieldName.equals("label"))
					isExclude = true;
				if (isExpose && !isExclude) {
					UniLog.log1("field name:%s, type:%s, value:%s", fieldName, fieldType, f.get(bc));
					final JsonElement jsonEle = (jsonObj != null) ? jsonObj.get(fieldName) : null;
					if (fieldType.equals("java.lang.String")) {
						if (fieldName.equals("fdtype")) {
							final Combobox cb = new Combobox();
							final String fieldValue = (String)f.get(bc);
							final String oldValue = (String)getBcField(bcLabel, fieldName);
							final String oldValue1 = oldValue != null ? oldValue : fieldValue;
							boolean foundJsonItem = false;
							cb.appendItem("float");
							cb.appendItem("integer");
							cb.appendItem("char");
							cb.appendItem("date");
							cb.appendItem("list");
							cb.appendItem("checkbox");
							cb.appendItem("button");
							cb.appendItem("pickinput");
							cb.appendItem("datetime");
							cb.appendItem("time");
							cb.appendItem("label");
							cb.appendItem("radio");
							cb.appendItem("colorbox");
							cb.appendItem("memo");
							cb.appendItem("div");
							cb.appendItem("combobox");
							cb.setReadonly(true);
							cb.setWidth("100px");
							if (jsonEle != null) {
								try {
									String s = jsonEle.getAsString();
									cb.setValue(s);
									putBcField(bcLabel, fieldName, isFromImportJsonFile ? oldValue1 : s);
									putBcFieldInJson(bcLabel, fieldName, isFromImportJsonFile ? false : null);
									foundJsonItem = true;
								} catch (Exception e) {
									e.printStackTrace();
									cb.setValue(isFromImportJsonFile ? fieldValue : oldValue1);
									putBcField(bcLabel, fieldName, oldValue1);
									if (!isFromImportJsonFile)
										putBcFieldInJson(bcLabel, fieldName, false);
								}
							}
							else {
								cb.setValue(isFromImportJsonFile ? fieldValue : oldValue1);
								putBcField(bcLabel, fieldName, oldValue1);
								if (!isFromImportJsonFile)
									putBcFieldInJson(bcLabel, fieldName, false);
							}
							cb.setAttribute("classField", fieldName);
							cb.setAttribute("fieldType", fieldType);
							compList.add(new Hlayout() {{
								setAttribute("pclassField", fieldName);
								setAttribute("sortTypeNumA", 1);
								setAttribute("sortTypeNumB", 1);
								appendChild(new Label(fieldName) {{
									setStyle("display:inline-block;min-width:90px");
								}});
								appendChild(cb);
								ZkUtil.appendStyle(this, "margin:5px 0");
							}});
							EventListener<Event> el = new EventListener<Event>() {
								@Override
								public void onEvent(Event event) throws Exception {
									Object ov = event != null ? getBcField(bcLabel, fieldName) : oldValue1;
									String cv = cb.getValue();
									if (ov == null)
										ov = "";
									UniLog.log("select event:" + event + ", ov:" + ov + ", cv:" + cv);
									putBcFieldUpdated(bcLabel, fieldName, ObjectUtils.notEqual(cv, ov));
									if (event != null)
										checkBtnSaveDisabled();
								}
							};
							cb.addEventListener(Events.ON_SELECT, el);
							if (foundJsonItem && isFromImportJsonFile)
								el.onEvent(null);
						}
						else {
							final Textbox tb = new Textbox();
							final String fieldValue = (String)f.get(bc);
							final String oldValue = (String)getBcField(bcLabel, fieldName);
							final String oldValue1 = oldValue != null ? oldValue : fieldValue;
							boolean foundJsonItem = false;
							tb.setHflex("1");
							if (jsonEle != null) {
								try {
									String s = jsonEle.getAsString();
									tb.setValue(s);
									putBcField(bcLabel, fieldName, isFromImportJsonFile ? oldValue1 : s);
									putBcFieldInJson(bcLabel, fieldName, isFromImportJsonFile ? false : null);
									foundJsonItem = true;
								} catch (Exception e) {
									e.printStackTrace();
									tb.setValue(isFromImportJsonFile ? fieldValue : oldValue1);
									putBcField(bcLabel, fieldName, oldValue1);
									if (!isFromImportJsonFile)
										putBcFieldInJson(bcLabel, fieldName, false);
								}
							}
							else {
								tb.setValue(isFromImportJsonFile ? fieldValue : oldValue1);
								putBcField(bcLabel, fieldName, oldValue1);
								if (!isFromImportJsonFile)
									putBcFieldInJson(bcLabel, fieldName, false);
							}
							tb.setAttribute("classField", fieldName);
							tb.setAttribute("fieldType", fieldType);
							compList.add(new Hlayout() {{
								setAttribute("pclassField", fieldName);
								setAttribute("sortTypeNumA", 2);
								setAttribute("sortTypeNumB", 3);
								appendChild(new Label(fieldName) {{
									setStyle("display:inline-block;min-width:90px");
								}});
								appendChild(tb);
								setWidth(sessionHelper.isMobile() ? "calc(100% - 10px)" : "calc(50% - 10px)");
								ZkUtil.appendStyle(this, "margin:5px 0");
							}});
							EventListener<InputEvent> el = new EventListener<InputEvent>() {
								@Override
								public void onEvent(InputEvent event) throws Exception {
									Object ov = event != null ? getBcField(bcLabel, fieldName) : oldValue1;
									String cv = event != null ? event.getValue() : tb.getValue();
									if (ov == null)
										ov = "";
									UniLog.log("change event:" + event + ", ov:" + ov + ", cv:" + cv);
									putBcFieldUpdated(bcLabel, fieldName, ObjectUtils.notEqual(cv, ov));
									if (event != null)
										checkBtnSaveDisabled();
								}
							};
							tb.addEventListener(Events.ON_CHANGING, el);
							if (foundJsonItem && isFromImportJsonFile)
								el.onEvent(null);
						}
					} else if (fieldType.equals("int")) {
						final Intbox ib = new Intbox();
						final int fieldValue = f.getInt(bc);
						final Integer oldValue = (Integer)getBcField(bcLabel, fieldName);
						final int oldValue1 = oldValue != null ? oldValue : fieldValue;
						boolean foundJsonItem = false;
						ib.setWidth("100px");
						if (jsonEle != null) {
							try {
								int i = jsonEle.getAsInt();
								ib.setValue(i);
								putBcField(bcLabel, fieldName, isFromImportJsonFile ? oldValue1 : i);
								putBcFieldInJson(bcLabel, fieldName, isFromImportJsonFile ? false : null);
								foundJsonItem = true;
							} catch (Exception e) {
								e.printStackTrace();
								ib.setValue(isFromImportJsonFile ? fieldValue : oldValue1);
								putBcField(bcLabel, fieldName, oldValue1);
								if (!isFromImportJsonFile)
									putBcFieldInJson(bcLabel, fieldName, false);
							}
						}
						else {
							ib.setValue(isFromImportJsonFile ? fieldValue : oldValue1);
							putBcField(bcLabel, fieldName, oldValue1);
							if (!isFromImportJsonFile)
								putBcFieldInJson(bcLabel, fieldName, false);
						}
						ib.setAttribute("classField", fieldName);
						ib.setAttribute("fieldType", fieldType);
						compList.add(new Hlayout() {{
							setAttribute("pclassField", fieldName);
							setAttribute("sortTypeNumA", 1);
							setAttribute("sortTypeNumB", 2);
							appendChild(new Label(fieldName) {{
								setStyle("display:inline-block;min-width:90px");
							}});
							appendChild(ib);
							ZkUtil.appendStyle(this, "margin:5px 0");
						}});
						EventListener<InputEvent> el = new EventListener<InputEvent>() {
							@Override
							public void onEvent(InputEvent event) throws Exception {
								Object ov = event != null ? getBcField(bcLabel, fieldName) : oldValue1;
								int cv = 0;
								try {
									if (event != null)
										cv = Integer.parseInt(event.getValue());
									else
										cv = ib.getValue();
								}
								catch (Exception e) {
								}
								UniLog.log("change event:" + event + ", ov:" + ov + ", cv:" + cv);
								putBcFieldUpdated(bcLabel, fieldName, ObjectUtils.notEqual(cv, ov));
								if (event != null)
									checkBtnSaveDisabled();
							}
						};
						ib.addEventListener(Events.ON_CHANGING, el);
						if (foundJsonItem && isFromImportJsonFile)
							el.onEvent(null);
					} else if (fieldType.equals("boolean")) {
						final Checkbox cb = new Checkbox(fieldName);
						final boolean fieldValue = f.getBoolean(bc);
						final Boolean oldValue = (Boolean)getBcField(bcLabel, fieldName);
						final Boolean oldValue1 = (oldValue != null && isFromImportJsonFile) ? oldValue : fieldValue;
						boolean foundJsonItem = false;
						if (jsonEle != null) {
							try {
								boolean b = jsonEle.getAsBoolean();
								cb.setChecked(b);
								putBcField(bcLabel, fieldName, isFromImportJsonFile ? oldValue1 : b);
								putBcFieldInJson(bcLabel, fieldName, isFromImportJsonFile ? false : null);
								foundJsonItem = true;
							} catch (Exception e) {
								e.printStackTrace();
								cb.setChecked(isFromImportJsonFile ? fieldValue : oldValue1);
								putBcField(bcLabel, fieldName, oldValue1);
								if (!isFromImportJsonFile)
									putBcFieldInJson(bcLabel, fieldName, false);
							}
						}
						else {
							cb.setChecked(isFromImportJsonFile ? fieldValue : oldValue1);
							putBcField(bcLabel, fieldName, oldValue1);
							if (!isFromImportJsonFile)
								putBcFieldInJson(bcLabel, fieldName, false);
						}
						cb.setAttribute("classField", fieldName);
						cb.setAttribute("fieldType", fieldType);
						ZkUtil.appendStyle(cb, "margin-top:8px");
						cb.setAttribute("pclassField", fieldName);
						cb.setAttribute("sortTypeNumA", 0);
						cb.setAttribute("sortTypeNumB", 0);
						compList.add(cb);
						EventListener<CheckEvent> el = new EventListener<CheckEvent>() {
							@Override
							public void onEvent(CheckEvent event) throws Exception {
								Object ov = event != null ? getBcField(bcLabel, fieldName) : oldValue1;
								boolean cv = event != null ? event.isChecked() : cb.isChecked();
								UniLog.log("check event:" + event + ", ov:" + ov + ", cv:" + cv);
								putBcFieldUpdated(bcLabel, fieldName, ObjectUtils.notEqual(cv, ov));
								if (event != null)
									checkBtnSaveDisabled();
							}
						};
						cb.addEventListener(Events.ON_CHECK, el);
						if (foundJsonItem && isFromImportJsonFile)
							el.onEvent(null);
					} else if (fieldType.equals("[Ljava.lang.String;")) {
						final Textbox tb = new Textbox();
						final String[] fieldValue = (String[])f.get(bc);
						final String[] oldValue = (String[])getBcField(bcLabel, fieldName);
						final String[] oldValue1 = (oldValue != null && !isFromImportJsonFile) ? oldValue : fieldValue;
						boolean foundJsonItem = false;
						tb.setHflex("1");
						String[] vsv;
						if (jsonEle != null) {
							try {
								JsonArray arr = jsonEle.getAsJsonArray();
								vsv = gson.fromJson(arr, String[].class);
								putBcField(bcLabel, fieldName, isFromImportJsonFile ? oldValue1 : vsv);
								putBcFieldInJson(bcLabel, fieldName, isFromImportJsonFile ? false : null);
								foundJsonItem = true;
							} catch (Exception e) {
								e.printStackTrace();
								vsv = isFromImportJsonFile ? fieldValue : oldValue1;
								putBcField(bcLabel, fieldName, oldValue1);
								if (!isFromImportJsonFile)
									putBcFieldInJson(bcLabel, fieldName, false);
							}
						}
						else {
							vsv = isFromImportJsonFile ? fieldValue : oldValue1;
							putBcField(bcLabel, fieldName, oldValue1);
							if (!isFromImportJsonFile)
								putBcFieldInJson(bcLabel, fieldName, false);
						}
						StringBuilder sb = new StringBuilder();
						if (vsv != null) {
							for (String v : vsv)
								sb.append("," + v);
							sb.deleteCharAt(0);
						}
						tb.setValue(sb.toString());
						tb.setAttribute("classField", fieldName);
						tb.setAttribute("fieldType", fieldType);
						compList.add(new Hlayout() {{
							setAttribute("pclassField", fieldName);
							setAttribute("sortTypeNumA", 3);
							setAttribute("sortTypeNumB", 4);
							appendChild(new Label(fieldName) {{
								setStyle("display:inline-block;min-width:90px");
							}});
							appendChild(tb);
							setWidth("calc(100% - 10px)");
							ZkUtil.appendStyle(this, "margin:5px 0");
						}});
						EventListener<InputEvent> el = new EventListener<InputEvent>() {
							@Override
							public void onEvent(InputEvent event) throws Exception {
								String tbStr = event != null ? event.getValue() : tb.getValue();
								Object ov = event != null ? getBcField(bcLabel, fieldName) : oldValue1;
								String[] ss = StringUtils.isNotBlank(tbStr) ? tbStr.split(",") : (ov != null ? new String[0] : null);
								String[] oss = (String[])ov;
								boolean flag = false;
								if (ss != null) {
									if (oss != null) {
										if (!Arrays.equals(ss, oss))
											flag = true;
									} else
										flag = true;
								} else if (oss != null)
									flag = true;
								UniLog.log("change event:" + event + ", ov:" + ov + ", tbStr:" + tbStr + ", flag:" + flag);
								putBcFieldUpdated(bcLabel, fieldName, flag);
								if (event != null)
									checkBtnSaveDisabled();
							}
						};
						tb.addEventListener(Events.ON_CHANGING, el);
						if (foundJsonItem && isFromImportJsonFile)
							el.onEvent(null);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Collections.sort(compList, new Comparator<Component>() {
			@Override
			public int compare(Component c1, Component c2) {
				int sortTypeNumA1 = (Integer) c1.getAttribute("sortTypeNumA");
				int sortTypeNumA2 = (Integer) c2.getAttribute("sortTypeNumA");
				int sortTypeNumB1 = (Integer) c1.getAttribute("sortTypeNumB");
				int sortTypeNumB2 = (Integer) c2.getAttribute("sortTypeNumB");
				String classField1 = (String) c1.getAttribute("pclassField");
				String classField2 = (String) c2.getAttribute("pclassField");
				if (sortTypeNumA1 != sortTypeNumA2)
					return sortTypeNumA1 - sortTypeNumA2;
				else if (sortTypeNumB1 != sortTypeNumB2)
					return sortTypeNumB1 - sortTypeNumB2;
				else
					return classField1.compareTo(classField2);
			}
		});
		for (int i = 0; i < compList.size(); i++) {
			Component comp = compList.get(i);
			final int sortTypeNumA = (Integer) comp.getAttribute("sortTypeNumA");
			if (i > 0) {
				final int lastSortTypeNumA = (Integer) compList.get(i - 1).getAttribute("sortTypeNumA");
				div.appendChild(new Space() {{
					if (sortTypeNumA != lastSortTypeNumA) {
						setWidth("100%");
						setHeight("0px");
						setOrient("horizontal");
					}
				}});
			}
			div.appendChild(comp);
		}
		
		win.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
			private void removeSelfBlock() {
				biColumnBlockMap.remove(bcLabel);
				removeBcFieldUpdated(bcLabel);
				putBcFieldInJson(bcLabel, true);
				checkBtnSaveDisabled();
			}
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("block close " + event);
				if (isBcFieldUpdated(bcLabel)) {
					event.stopPropagation();
					Messagebox.show("change detected, still close window?", "Remove block", 
						new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
						Messagebox.QUESTION, new ZkBiEventListener<Messagebox.ClickEvent>() {
						@Override
						public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
							switch (event.getButton()) {
							case OK:
								removeSelfBlock();
								win.onClose();
								break;
							default:
								break;
							}
						}
					});
				}
				else
					removeSelfBlock();
			}
		});
		
		biColumnBlockMap.put(bc.getLabel(), win);
	}

	public MessageboxDlg showDialog() {
		dialog = buildMessageboxDlg("Update Custom BiView", createWindow(),
			new Messagebox.Button[]{Messagebox.Button.RETRY, Messagebox.Button.YES, Messagebox.Button.OK, Messagebox.Button.NO, Messagebox.Button.CANCEL},
			new String[]{"Load/Reload", "Import", "Save", "Export", "Close"},
			parentComp, 
			new ZkBiEventListener<Messagebox.ClickEvent>() {
				@Override
				public void onZkBiEvent(ClickEvent event) throws Exception {
					UniLog.log("ZkBiUpdateBiView onZkBiEvent target:" + event.getTarget() + ",button:" + event.getButton());
					if (event.getButton() == null)
						return;
					event.stopPropagation();
					switch (event.getButton()) {
					case RETRY: //Load
						new ZkBiAbstractLongOp(event.getTarget(), "Loading...", LONGOP_DEFAULT_DELAY){
							@Override
							public ReturnMsg longOp() {
								loadBcField();
								return null;
							}
						};
						break;
					case OK: //Save
						new ZkBiAbstractLongOp(event.getTarget(), "Saving...", LONGOP_DEFAULT_DELAY){
							@Override
							public ReturnMsg longOp() {
								if (saveBcField() == -1)
									dialog.onClose();
								return null;
							}
						};
						break;
					case YES: //Import
						break;
					case NO: //Export
						try {
							File file = getJsonFile(selectedBrName);
							if (file.exists())
								Filedownload.save(file, "application/json");
							else
								showErrMsg("File not found");
						}
						catch (Exception e) {
							showErrMsg("Export error: %s", e.toString());
							e.printStackTrace();
						}
						break;
					case CANCEL: //Close
						dialog.onClose();
						break;
					default:
						break;
					}
				}
			}
		);
		dialog.setMaximizable(true);
		dialog.setSizable(true);
		if (sessionHelper.isMobile()) {
			dialog.setWidth("100%");
			dialog.setHeight("100%");
		} else {
			dialog.setWidth("50%");
			dialog.setHeight("50%");
			ZkUtil.appendStyle(dialog, "min-width:1280px;min-height:720px;");
		}
		dialog.doHighlighted();
		
		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (isBcFieldUpdated()) {
					event.stopPropagation();
					Messagebox.show("change detected, still close window?", "Close window", 
						new Messagebox.Button[]{Messagebox.Button.YES, Messagebox.Button.NO, Messagebox.Button.CANCEL}, 
						Messagebox.QUESTION, new ZkBiEventListener<Messagebox.ClickEvent>() {
						@Override
						public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
							switch (event.getButton()) {
							case YES:
								new ZkBiAbstractLongOp(event.getTarget(), "Saving...", LONGOP_DEFAULT_DELAY){
									@Override
									public ReturnMsg longOp() {
										if (saveBcField() == 0)
											dialog.onClose();
										return null;
									}
								};
								break;
							case NO:
								dialog.onClose();
								break;
							default:
								break;
							}
						}
					});
				}
			}
		});

		//find buttons
		for (Component cbtn : dialog.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), "Load/Reload"))
				btnLoad = btn;
			else if (StringUtils.equals(btn.getLabel(), "Save"))
				btnSave = btn;
			else if (StringUtils.equals(btn.getLabel(), "Import"))
				btnImport = btn;
			else if (StringUtils.equals(btn.getLabel(), "Export"))
				btnExport = btn;
			else if (StringUtils.equals(btn.getLabel(), "Close"))
				btnClose = btn;
		}

		//setup import button
		btnImport.setUpload("true,maxsize=4194304,accept=.json");
		btnImport.addEventListener(Events.ON_UPLOAD, new EventListener<UploadEvent>() {
			@Override
			public void onEvent(UploadEvent event) throws Exception {
				UniLog.log("upload event:" + event.getMedia().getClass());
				org.zkoss.util.media.Media media = event.getMedia();
				if (media instanceof org.zkoss.util.media.AMedia) {
					final org.zkoss.util.media.AMedia amedia = (org.zkoss.util.media.AMedia) media;
					UniLog.log("amedia:" + amedia.getContentType() + "," + amedia.getFormat() + "," + amedia.getName());
					if (selectedBrName == null)
						return;
					new ZkBiAbstractLongOp(dialog, "Loading...", LONGOP_DEFAULT_DELAY){
						@Override
						public ReturnMsg longOp() {
							removeBiColumnBlockByBrName(selectedBrName);
							removeBcFieldUpdatedByBrName(selectedBrName);
							putBcFieldInJsonByBrName(selectedBrName, true);
							try {
								InputStreamReader reader = new InputStreamReader(amedia.getStreamData());
								loadBcField(selectedBrName, reader, true);
								reader.close();
							}
							catch (Exception e) {
								showErrMsg("Import error: %s", e.toString());
								e.printStackTrace();
							}
							checkBtnSaveDisabled();
							if (!btnSave.isDisabled()) {
								Messagebox.show("Save import data?", "Message", 
									new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
									Messagebox.QUESTION, new ZkBiEventListener<Messagebox.ClickEvent>() {
									@Override
									public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
										switch (event.getButton()) {
										case OK:
											new ZkBiAbstractLongOp(event.getTarget(), "Saving...", LONGOP_DEFAULT_DELAY){
												@Override
												public ReturnMsg longOp() {
													saveBcField();
													return null;
												}
											};
											break;
										default:
											break;
										}
									}
								});
							}
							return null;
						}
					};
				}
			}
		});

		//create BiResult View, load BcField
		new ZkBiAbstractLongOp(dialog, "Loading...", LONGOP_DEFAULT_DELAY){
			@Override
			public ReturnMsg longOp() {
				createBiResultView();
				loadBcField();
				return null;
			}
		};
		return dialog;
	}

    public static MessageboxDlg buildMessageboxDlg(String title, final HtmlBasedComponent child, Messagebox.Button[] buttons, String[] buttonsLabel, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
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
    	dlg.appendChild(new Vbox() {{
    		appendChild(child);
    		appendChild(div);
    		setVflex("1");
    		setHflex("1");
    	}});

    	dlg.setButtons(buttons, buttonsLabel);
    	dlg.setEventListener(eventListener);
    	return dlg;
    }

    private void showMsg(String p_format, Object...p_args){
       	Clients.evalJavaScript("$('#"+window.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
    }

    private void showErrMsg(String p_format, Object...p_args){
       	Clients.evalJavaScript("$('#"+window.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
    }
}
