package com.uniinformation.zkbi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.MapUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkcomp.S2Listbox;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkcomp.ZkBiButtonGroup;

public class ZkBiRecordCopy {
	private static final int MAX_RECURSIVE_LEVEL = 20;
	private static final int LONGOP_DEFAULT_DELAY = 50;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static final SimpleDateFormat fsdf = new SimpleDateFormat("yyyyMMddHHmmss");

	private SessionHelper sessionHelper;
	private JxZkBiBase bibase;
	private Component curComponent;
  	private Component parentComponent;
	private String curViewName;

   	private Button btnPaste = new ZkBiButton();
	
	public void setBiBase(JxZkBiBase bibase) {
		UniLog.log1("setBiBase:%s", bibase);
		this.bibase = bibase;
	}

	public Button buildButtonGroup(final SessionHelper sessionHelper, Component component, String viewName, String buttonGroupId) {
		if (sessionHelper == null){
			UniLog.log1("sessionHelper is null");
			return null;
		}
		if (!sessionHelper.getAllowRecordCopy()){
			UniLog.log1("skip build button group");
			return null;
		}
		this.sessionHelper = sessionHelper;
		curComponent = component;
		parentComponent = component.getParent();
		if(parentComponent == null) return(null);
		curViewName = viewName;

    	final Button btnCopy = new ZkBiButton();
    	btnCopy.setLabel(sessionHelper.getBtLabel("Copy"));
		//btnCopy.setImage("images/icons/zkweb/020-import-25x25.png");
    	btnCopy.setIconSclass("z-icon-copy");
    	btnCopy.setId("btRecordCopyWithCopy");
    	btnCopy.setTooltiptext(sessionHelper.getLabel("Copy data to clipboard"));

    	final Button btnExportToFile = new ZkBiButton();
    	btnExportToFile.setLabel(sessionHelper.getBtLabel("Export To File"));
    	btnExportToFile.setIconSclass("z-icon-save");
    	btnExportToFile.setId("btRecordCopyWithExportToFile");
    	btnExportToFile.setTooltiptext(sessionHelper.getLabel("Export data to file(json)"));
    	btnExportToFile.setVisible(sessionHelper.isAdminUser());

    	btnPaste.setLabel(sessionHelper.getBtLabel("Paste"));
    	btnPaste.setIconSclass("z-icon-paste");
    	btnPaste.setId("btRecordCopyWithPaste");
    	btnPaste.setTooltiptext(ZkUtil.joinStringLabel(sessionHelper, "\n", "Paste data from clipboard.", null, "Remark: It exclude unsupported items.", null));
    	btnPaste.setDisabled(!isValidImportJson(getCopiedJson()));
    	
    	final Button btnImportFromFile = new ZkBiButton();
    	btnImportFromFile.setLabel(sessionHelper.getBtLabel("Import From File"));
    	btnImportFromFile.setIconSclass("z-icon-file");
    	btnImportFromFile.setId("btRecordCopyWithImportFromFile");
    	btnImportFromFile.setTooltiptext(ZkUtil.joinStringLabel(sessionHelper, "\n", "Import data from file(json).", null, "Remark: It exclude unsupported items.", null));
    	btnImportFromFile.setVisible(sessionHelper.isAdminUser());
    	
    	final Button btnCopyAndAdd = new ZkBiButton();
    	btnCopyAndAdd.setLabel(sessionHelper.getBtLabel("Copy & Add"));
    	btnCopyAndAdd.setIconSclass("z-icon-copy");
    	btnCopyAndAdd.setId("btRecordCopyWithCopyAndAdd");
    	btnCopyAndAdd.setTooltiptext(sessionHelper.getLabel("Copy & Add"));

    	ZkBiButtonGroup g = new ZkBiButtonGroup(sessionHelper)
    			.addButton(btnCopy)
    			.addButton(btnPaste);
   		g.addSeparator();
   		g.addButton(btnCopyAndAdd);
    	if (sessionHelper.isAdminUser())
    		g.addSeparator();
    	final Button bgRecordCopy = g
    			.addButton(btnExportToFile)
    			.addButton(btnImportFromFile)
    			.setId(buttonGroupId)
    			.setLabel(sessionHelper.getBtLabel("Copy & Paste"))
    			.setTooltiptext(ZkUtil.joinStringLabel(sessionHelper, "\n", "Copy current record data to/from clipboard.", "", "Remark: It exclude unsupported items.", ""))
    			.setIconSclass("z-icon-copy")
    			.build();
        //bgMaint.setImage("images/icons/zkweb/047-gears-25x25.png");

		Button btAdd = (Button) parentComponent.getFellowIfAny("btAdd", false);
    	bgRecordCopy.setVisible(btAdd != null && ZkUtil.isRealVisible(btAdd) && !btAdd.isDisabled());
    	
    	btnCopy.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				new ZkBiAbstractLongOp(event.getTarget(), sessionHelper.getLabel("Copying..."), LONGOP_DEFAULT_DELAY){
					@Override
					public ReturnMsg longOp() {
						copy();
						return null;
					}
				};
			}
    	});

    	btnExportToFile.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				new ZkBiAbstractLongOp(event.getTarget(), sessionHelper.getLabel("Exporting..."), LONGOP_DEFAULT_DELAY){
					@Override
					public ReturnMsg longOp() {
						exportToFile();
						return null;
					}
				};
			}
    	});

    	btnPaste.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				if (bibase != null) {
					if (bibase.isDirty()) {
						ZkBiMsgbox.show(ZkBiMsgbox.Type.question, "Are you sure to update the content?", new String[]{"Yes","No"},new ZkBiEventListener<Event>(){
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								if (StringUtils.equals(btn.getName(), "Yes"))
									paste();
							}
						});				
					}
					else
						paste();
				}
				else
					ZkUtil.showErrMsg("Bibase not found");
			}
    	});

    	handleCopyAndAddButtonEvent(btnCopyAndAdd);
    	handleImportFromFileButtonEvent(btnImportFromFile);
    	
        return bgRecordCopy;
	}
	
	private boolean copy() {
		try {
			String json = saveCurrentComponentDatasToJson();
			UniLog.log1("json:%s", json);
			sessionHelper.putSessionData("recordcopy.copiedjson", json);
			ZkUtil.showMsg(sessionHelper.getLabel("Current record copied"));
		} catch (Exception e) {
			UniLog.log1("Error:%s", e.getMessage());
			ZkUtil.showErrMsg(sessionHelper.getLabel("Copy Error") + ": %s", e.getMessage());
		}
		boolean r = isValidImportJson(getCopiedJson());
    	btnPaste.setDisabled(!r);
    	return r;
	}
	
	private void exportToFile() {
		try {
			String json = saveCurrentComponentDatasToJson();
			UniLog.log1("json:%s", json);
			Filedownload.save(json, "application/json", String.format("recordcopy_%s_%s.json", curViewName, fsdf.format(DateUtil.now())));
			ZkUtil.showMsg(sessionHelper.getLabel("Current record exported to file"));
		} catch (Exception e) {
			UniLog.log1("Error:%s", e.getMessage());
			ZkUtil.showErrMsg(sessionHelper.getLabel("Export Error") + ": %s", e.getMessage());
		}
	}
	
	private String getCopiedJson() {
		return (String)sessionHelper.getSessionData("recordcopy.copiedjson");
	}
	
	private void paste() {
		try {
			String json = getCopiedJson();
			fillCurrentComponentDatas(json);
			if(bibase != null) bibase.afterPaste();
		} catch (Exception e) {
			UniLog.log1("Error:%s", e.getMessage());
			ZkUtil.showErrMsg(sessionHelper.getLabel("Error") + ": %s", e.getMessage());
		}
	}
	
	private void showBusy(Component busyComp, String msg) {
		Clients.showBusy(busyComp, msg);
		busyComp.setAttribute("ZkBiAbstractLongOp.busyFlag", "Y");
	}

	private void clearBusy(Component busyComp) {
		Clients.clearBusy(busyComp);
		busyComp.removeAttribute("ZkBiAbstractLongOp.busyFlag");
	}

	private void handleCopyAndAddButtonEvent(final Button btn) {
    	UniLog.log1("handleCopyAndAddButtonEvent");
		final Component busyComp1 = ZkBiAbstractLongOp.getBusyComp(parentComponent);
    	btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				if (bibase.isDirty()) {
					ZkUtil.showErrMsg(sessionHelper.getLabel("Editing in progress. Copy action abort"));
					return;
				}
				new ZkBiAbstractLongOp(event.getTarget(), sessionHelper.getLabel("Copying..."), LONGOP_DEFAULT_DELAY){
					@Override
					public ReturnMsg longOp() {
						Button btClose = (Button) curComponent.getFellowIfAny("btClose", false);
						if (btClose == null) {
							ZkUtil.showErrMsg(sessionHelper.getLabel("Close button not found"));
							return null;
						}
						Button btAdd = (Button) parentComponent.getFellowIfAny("btAdd", false);
						if (btAdd == null) {
							ZkUtil.showErrMsg(sessionHelper.getLabel("Add button not found"));
							return null;
						}
						if (!copy()) {
							ZkUtil.showErrMsg(sessionHelper.getLabel("Invalid copied data"));
							return null;
						}
						showBusy(busyComp1, sessionHelper.getLabel("Processing..."));
						UniLog.log1("click btClose");
						Events.sendEvent(Events.ON_CLICK, btClose, null);
						ZkUtil.delayPostEvent("onRCAfterClickCloseButton", parentComponent, 200, 50);
						return null;
					}
				};
			}
    	});
    	Iterator<EventListener<?>> it = parentComponent.getEventListeners("onRCAfterClickCloseButton").iterator();
    	while (it.hasNext())
    		parentComponent.removeEventListener("onRCAfterClickCloseButton", it.next());
	    parentComponent.addEventListener("onRCAfterClickCloseButton", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s, data:%s", event, event.getData());
				if (bibase.optBr() != null) {
					int tryCount = (Integer)event.getData();
					if (tryCount == 0) {
						ZkUtil.showErrMsg(sessionHelper.getLabel("Paste fail"));
						clearBusy(busyComp1);
						return;
					}
					ZkUtil.delayPostEvent("onRCAfterClickCloseButton", parentComponent, tryCount - 1, 50);
					return;
				}

				Button btAdd = (Button) parentComponent.getFellowIfAny("btAdd", false);
				if (btAdd == null) {
					ZkUtil.showErrMsg(sessionHelper.getLabel("Add button not found"));
					clearBusy(busyComp1);
					return;
				}
				if (!ZkUtil.isRealVisible(btAdd) || btAdd.isDisabled()) {
					ZkUtil.showErrMsg(sessionHelper.getLabel("Add button is invisibled or disabled"));
					return;
				}
				UniLog.log1("click btAdd");
				Events.sendEvent(Events.ON_CLICK, btAdd, null);
				ZkUtil.delayPostEvent("onRCAfterClickAddButton", parentComponent, 200, 50);
			}
	    });
    	it = parentComponent.getEventListeners("onRCAfterClickAddButton").iterator();
    	while (it.hasNext())
    		parentComponent.removeEventListener("onRCAfterClickAddButton", it.next());
	   	parentComponent.addEventListener("onRCAfterClickAddButton", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s, data:%s", event, event.getData());
				if (bibase.optBr() == null) {
					int tryCount = (Integer)event.getData();
					if (tryCount == 0) {
						ZkUtil.showErrMsg(sessionHelper.getLabel("Paste fail"));
						clearBusy(busyComp1);
						return;
					}
					ZkUtil.delayPostEvent("onRCAfterClickAddButton", parentComponent, tryCount - 1, 50);
					return;
				}
				clearBusy(busyComp1);
				paste();
			}
	   	});
	}
	
	private void handleImportFromFileButtonEvent(final Button btn) {
		btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			private void upload() {
				Fileupload.get(new HashMap<String, Object>(), null, null, ".json", 1, -1, false, new EventListener<UploadEvent>(){
					@Override
					public void onEvent(final UploadEvent event) throws Exception {
						UniLog.log1("upload event class:%s, target:%s", event.getMedia().getClass(), event.getTarget());
						try {
							org.zkoss.util.media.Media media = event.getMedia();
							if (media instanceof org.zkoss.util.media.AMedia) {
								final org.zkoss.util.media.AMedia amedia = (org.zkoss.util.media.AMedia) media;
								UniLog.log("amedia:" + amedia.getContentType() + "," + amedia.getFormat() + "," + amedia.getName());
								String json = new String(amedia.getByteData());
								fillCurrentComponentDatas(json);
							}
						}
						catch (Exception e) {
							UniLog.log1("Error:%s", e.getMessage());
							ZkUtil.showErrMsg(sessionHelper.getLabel("Error") + ": %s", e.getMessage());
						}
					}
				});
			}
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("click event:%s", event);
				if (bibase != null) {
					if (bibase.isDirty()) {
						ZkBiMsgbox.show(ZkBiMsgbox.Type.question, "Are you sure to update the content?", new String[]{"Yes","No"},new ZkBiEventListener<Event>(){
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								if (StringUtils.equals(btn.getName(), "Yes"))
									upload();
							}
						});				
					}
					else
						upload();
				}
				else
					ZkUtil.showErrMsg("Bibase not found");
			}
		});
	}
	
	private String saveCurrentComponentDatasToJson() throws Exception {
		String json = String.format("{"
				+ "	\"viewlist\" : [{"
				+ "		\"viewid\" : \"%s\", "
				+ "		\"data\" : [{}]"
				+ "	}]"
				+ "}", curViewName);
		JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
		JsonObject jo1 = jo.getAsJsonArray("viewlist").get(0).getAsJsonObject().getAsJsonArray("data").get(0).getAsJsonObject();
		componentDatasToMap(curComponent, jo1, MAX_RECURSIVE_LEVEL);
		return GsonUtil.objToStr(jo);
	}
	
	private Map<String, Object> getCurrentViewJsonData(String json) {
		try {
			if (StringUtils.isBlank(json))
				return MapUtil.of("errMsg", "No Json data found");
			JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
			JsonObject jo1 = jo.getAsJsonArray("viewlist").get(0).getAsJsonObject();
			String viewId = jo1.get("viewid").getAsString();
			if (!StringUtils.equals(curViewName, viewId))
				return MapUtil.of("The json is not same as current view");
			JsonObject jo2 = jo1.getAsJsonArray("data").get(0).getAsJsonObject();
			return MapUtil.of("data", jo2);
		}
		catch (Exception ex) {
			return MapUtil.of("errMsg", "Error: " + ex.getMessage());
		}
	}

	private boolean isValidImportJson(String json) {
		return getCurrentViewJsonData(json).get("errMsg") == null;
	}

	private void fillCurrentComponentDatas(String json) throws Exception {
		Map<String, Object> m = getCurrentViewJsonData(json);
		String errMsg = (String)m.get("errMsg");
		if (errMsg != null)
			throw new Exception(errMsg);
		JsonObject jo2 = (JsonObject)m.get("data");

		final Component busyComp = ZkBiAbstractLongOp.getBusyComp(curComponent);
		Clients.showBusy(busyComp, sessionHelper.getLabel("Pasting..."));
		busyComp.setAttribute("ZkBiAbstractLongOp.busyFlag", "Y");
		mapToComponentDatas(jo2, curComponent, MAX_RECURSIVE_LEVEL, new MapToComponentDatasCallback() {
			@Override
			public void completed(String errMsg) {
				if (errMsg != null)
					ZkUtil.showErrMsg(errMsg);
				else
					ZkUtil.showMsg(sessionHelper.getLabel("Paste completed"));
				Clients.clearBusy(busyComp);
				busyComp.removeAttribute("ZkBiAbstractLongOp.busyFlag");
			}
		});
	}

	
	private static void componentDatasToMap(Component p_comp, JsonObject map, int maxRecursiveLevel) throws Exception {
		if (p_comp == null || maxRecursiveLevel == 0)
			return;
		List<Component> comps = p_comp.getChildren();
		if (comps == null || comps.isEmpty())
			return;
		for (Component comp : comps) {
			String id = comp.getId();
			if (StringUtils.isNotBlank(id)) {
				Object value = null;
				if (!(comp instanceof Label))
					value = getComponentValue(comp);
				//UniLog.log1("componentDatasToMap id:%s, class:%s, value:%s", id, comp.getClass(), value);
				if (value != null)
					addJsonProperty(map, id, value);
				else if (comp instanceof Listbox && id.startsWith("list_")) {
					final String subViewName = JxZkBiBase.replaceViewName(id.substring(5));
					final List<String> biColumnLabelList = new ArrayList<String>();

					JsonArray jaViewlist = map.getAsJsonArray("viewlist");
					if (jaViewlist == null) {
						jaViewlist = new JsonArray();
						map.add("viewlist", jaViewlist);
					}

					String json = String.format("{"
							+ "	\"viewid\" : \"%s\", "
							+ "	\"data\" : []"
							+ "}", subViewName);
					JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
					jaViewlist.add(jo);
					JsonArray jaData = jo.getAsJsonArray("data");
				
					Listbox lb = (Listbox)comp;
					lb.renderAll();
					if (lb.getListhead() != null) {
						for (Component hc : lb.getListhead().getChildren()) {
							if (hc instanceof Listheader)
								biColumnLabelList.add((String)hc.getAttribute("biColumnLabel"));
							else
								biColumnLabelList.add(null);
						}
					}
					for (Listitem li : lb.getItems()) {
						JsonObject m = new JsonObject();
						jaData.add(m);
						for (int i = 0; i < li.getChildren().size() && i < biColumnLabelList.size(); i++) {
							Component lic = li.getChildren().get(i);
							String bcLabel = biColumnLabelList.get(i);
							if (lic instanceof Listcell) {
								if (lic.getChildren() != null && !lic.getChildren().isEmpty()) {
									Component childComp = lic.getFirstChild();
									Object v = getComponentValue(childComp);
									if (v != null) {
										if (StringUtils.isNotBlank(bcLabel))
											addJsonProperty(m, bcLabel, v);
										else {
											String cellLabel = getComponentCellLabel(childComp);
											if (cellLabel != null)
												addJsonProperty(m, cellLabel, v);
										}
									}
									else {
										List<Component> cc = new ArrayList<Component>();
										findAllInputChildComponents(childComp, cc, MAX_RECURSIVE_LEVEL);
										for (Component c : cc) {
											String cellLabel = getComponentCellLabel(c);
											v = getComponentValue(c);
											if (cellLabel != null && v != null)
												addJsonProperty(m, cellLabel, v);
										}
									}
								}
								else if (StringUtils.isNotBlank(bcLabel))
									addJsonProperty(m, bcLabel, ((Listcell)lic).getLabel());
							}
						}
					}
				}
				else
					componentDatasToMap(comp, map, maxRecursiveLevel - 1);
			}
			else
				componentDatasToMap(comp, map, maxRecursiveLevel - 1);
		}
	}
	
	private static void mapToListboxItemDatas(final Listbox lb, final JsonArray ja, final MapToComponentDatasCallback callback) {
		try {
			if (ja == null || ja.size() == 0 || lb.getListhead() == null) {
				callback.completed(null);
				return;
			}
			final String subViewName = JxZkBiBase.replaceViewName(lb.getId().substring(5));
			final List<String> biColumnLabelList = new ArrayList<String>();
			Button btnAdd = null;
			//find add item button
			for (Component hc : lb.getListhead().getChildren()) {
				if (hc instanceof Listheader) {
					String biColumnLabel = (String)hc.getAttribute("biColumnLabel");
					biColumnLabelList.add(biColumnLabel);
					if (biColumnLabel == null && hc.getChildren() != null && !hc.getChildren().isEmpty() 
							&& hc.getFirstChild() instanceof Button && StringUtils.equals(hc.getFirstChild().getId(), "btadd_list_" + subViewName)) {
						btnAdd = (Button)hc.getFirstChild();
						if (btnAdd.isDisabled())
							btnAdd = null;
					}
				}
				else
					biColumnLabelList.add(null);
			}
			if (btnAdd != null) {
				Iterator<EventListener<?>> it = btnAdd.getEventListeners("onRCAddItem").iterator();
				while (it.hasNext()) {
					it.next();
					it.remove();
				}
				it = btnAdd.getEventListeners("onRCFillItem").iterator();
				while (it.hasNext()) {
					it.next();
					it.remove();
				}
				//handle add item
				btnAdd.addEventListener("onRCAddItem", new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log1("onRCAddItem:%s", event);
						try {
							Map<String, Object> params = (Map<String, Object>)event.getData();
							final JsonArray ja = (JsonArray)params.get("ja");
							final int jaIndex = (Integer)params.get("jaIndex");
							if (jaIndex >= ja.size()) {
								callback.completed(null);
								return;
							}
							JsonObject jo = ja.get(jaIndex).getAsJsonObject();
							if (jo.size() > 0) {
								final List<Listitem> lis = lb.getItems();
								final int osize = lis.size();
								//send add item event
								if (!sendAddListItemEvent(lb, event.getTarget())) {
									lb.renderAll();
									sendAddListItemEvent(lb, event.getTarget());
								}
								UniLog.log1("osize:%d, nsize:%d", osize, lis.size());

								if (lis.size() > osize) {
									//get last item
									final Listitem li = lis.get(osize);
									Events.echoEvent("onRCFillItem", event.getTarget(), MapUtil.of("listitem", li, "ja", ja, "jaIndex", jaIndex));
								}
								else {
									callback.completed("Error: add listbox item fail");
									return;
								}
							}
							else {
								//send add next item event
								Events.echoEvent("onRCAddItem", event.getTarget(), MapUtil.of("ja", ja, "jaIndex", jaIndex + 1));
							}
	
						}
						catch (Exception ex) {
							UniLog.log1("Error: %s", ex.getMessage());
							callback.completed("Error: " + ex.getMessage());
						}
					}
				});
				//handle fill item
				btnAdd.addEventListener("onRCFillItem", new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log1("onRCFillItem:%s", event);
						try {
							Map<String, Object> params = (Map<String, Object>)event.getData();
							Listitem li = (Listitem)params.get("listitem");
							final JsonArray ja = (JsonArray)params.get("ja");
							final int jaIndex = (Integer)params.get("jaIndex");
							if (jaIndex >= ja.size()) {
								callback.completed(null);
								return;
							}
							lb.renderAll();
							JsonObject jo = ja.get(jaIndex).getAsJsonObject();
							List<Component> compList = li.getChildren();
							final List<Map<String, Object>> setComponentValueList = new ArrayList<Map<String, Object>>();
							for (int j = 0; j < compList.size() && j < biColumnLabelList.size(); j++) {
								Component lic = li.getChildren().get(j);
								String bcLabel = biColumnLabelList.get(j);
								if (lic instanceof Listcell && lic.getChildren() != null && !lic.getChildren().isEmpty()) {
									Component childComp = lic.getFirstChild();
									if (isInputComponent(childComp)) {
										if (StringUtils.isNotBlank(bcLabel))
											setComponentValueList.add((Map)MapUtil.of("comp", childComp, "jo", jo, "joKey", bcLabel));
											//setComponentValue(childComp, jo, bcLabel);
										else {
											String cellLabel = getComponentCellLabel(childComp);
											if (cellLabel != null)
												setComponentValueList.add((Map)MapUtil.of("comp", childComp, "jo", jo, "joKey", cellLabel));
												//setComponentValue(childComp, jo, cellLabel);
										}
									}
									else {
										List<Component> cc = new ArrayList<Component>();
										findAllInputChildComponents(childComp, cc, MAX_RECURSIVE_LEVEL);
										for (Component c : cc) {
											String cellLabel = getComponentCellLabel(c);
											if (cellLabel != null)
												setComponentValueList.add((Map)MapUtil.of("comp", c, "jo", jo, "joKey", cellLabel));
												//setComponentValue(c, jo, cellLabel);
										}
									}
								}
							}

							//Round 1, fill non-formula fields
							for (Map<String, Object> sm : setComponentValueList) {
								Component compp = (Component)sm.get("comp");
								JsonObject joo = (JsonObject)sm.get("jo");
								String joKeyy = (String)sm.get("joKey");
								if (!ZkUtil.isFormula(compp) && !ZkUtil.isNoPaste(compp)) {
									UniLog.log1("round1 comp:%s, joKey:%s, isFormula:%b", compp, joKeyy, ZkUtil.isFormula(compp));
									setComponentValue(compp, joo, joKeyy);
								}
							}
							final String[] evList = new String[] {"onRCFillItemRound2", "onRCFillItemRound3", "onRCFillItemRound4"};
							for (int i = 0; i < evList.length; i++) {
								final String evKey = evList[i];
								final int evi = i;
								Iterator<EventListener<?>> it = event.getTarget().getEventListeners(evKey).iterator();
								while (it.hasNext())
									event.getTarget().removeEventListener(evKey, it.next());
								event.getTarget().addEventListener(evKey, new EventListener<Event>() {
									@Override
									public void onEvent(Event event) throws Exception {
										UniLog.log1("%s:%s", evKey, event);
										for (Map<String, Object> sm : setComponentValueList) {
											Component compp = (Component)sm.get("comp");
											JsonObject joo = (JsonObject)sm.get("jo");
											String joKeyy = (String)sm.get("joKey");
											if (evKey.equals("onRCFillItemRound2")) {
												//Round 2, fill non-formula fields(2nd stage)
												if (!ZkUtil.isFormula(compp) && !ZkUtil.isNoPaste(compp)) {
													String joValue = getJsonValue(compp, joo, joKeyy);
													Object compValue1 = getComponentValue(compp);
													String compValue = compValue1 != null ? compValue1.toString() : null;
													UniLog.log1("round2 comp:%s, joKey:%s, isFormula:%b, joValue:%s, compValue:%s, same:%b", compp, joKeyy, ZkUtil.isFormula(compp), joValue, compValue, StringUtils.equals(joValue, compValue));
													if (joValue != null && compValue != null && !StringUtils.equals(joValue, compValue))
														setComponentValue(compp, joo, joKeyy);
												}
											}
											else if (evKey.equals("onRCFillItemRound3")) {
												//Round 3, fill formula fields
												if (ZkUtil.isFormula(compp) && !ZkUtil.isNoPaste(compp)) {
													UniLog.log1("round3 comp:%s, joKey:%s, isFormula:%b", compp, joKeyy, ZkUtil.isFormula(compp));
													setComponentValue(compp, joo, joKeyy);
												}
											}
											else if (evKey.equals("onRCFillItemRound4")) {
												//Round 4, fill formula fields(2nd stage)
												if (ZkUtil.isFormula(compp) && !ZkUtil.isNoPaste(compp)) {
													setComponentValue(compp, joo, joKeyy);
													String joValue = getJsonValue(compp, joo, joKeyy);
													Object compValue1 = getComponentValue(compp);
													String compValue = compValue1 != null ? compValue1.toString() : null;
													UniLog.log1("round4 comp:%s, joKey:%s, isFormula:%b, joValue:%s, compValue:%s, same:%b", compp, joKeyy, ZkUtil.isFormula(compp), joValue, compValue, StringUtils.equals(joValue, compValue));
													if (joValue != null && compValue != null && !StringUtils.equals(joValue, compValue))
														setComponentValue(compp, joo, joKeyy);
												}
											}
										}
										if (evKey.equals("onRCFillItemRound4")) {
											//send add next item event
											Events.echoEvent("onRCAddItem", event.getTarget(), MapUtil.of("ja", ja, "jaIndex", jaIndex + 1));
										} else
											Events.echoEvent(evList[evi + 1], event.getTarget(), null);
									}
								});
							}
							Events.echoEvent("onRCFillItemRound2", event.getTarget(), null);
						}
						catch (Exception ex) {
							UniLog.log1("Error: %s", ex.getMessage());
							callback.completed("Error: " + ex.getMessage());
						}
					}
				});
				//send add item event
				Events.sendEvent("onRCAddItem", btnAdd, MapUtil.of("ja", ja, "jaIndex", 0));
			}
			else
				callback.completed(null);
		}
		catch (Exception ex) {
			UniLog.log1("Error: %s", ex.getMessage());
			callback.completed("Error: " + ex.getMessage());
		}
	}
	
	private static void mapToComponentDatas(JsonObject map, Component p_comp, final int maxRecursiveLevel, final MapToComponentDatasCallback callback) throws Exception {
		final Map<Listbox, JsonArray> lbMap = new LinkedHashMap<Listbox, JsonArray>();
		final List<Map<String, Object>> setComponentValueList = new ArrayList<Map<String, Object>>();
		mapToComponentDatas(map, p_comp, maxRecursiveLevel, lbMap, setComponentValueList);
		
		//Round 1, fill non-formula fields
		for (Map<String, Object> sm : setComponentValueList) {
			Component comp = (Component)sm.get("comp");
			JsonObject jo = (JsonObject)sm.get("jo");
			String joKey = (String)sm.get("joKey");
			if (!ZkUtil.isFormula(comp) && !ZkUtil.isNoPaste(comp)) {
				UniLog.log1("comp:%s, joKey:%s, isFormula:%b", comp, joKey, ZkUtil.isFormula(comp));
				setComponentValue(comp, jo, joKey);
			}
		}
		
		Iterator<EventListener<?>> it1 = p_comp.getEventListeners("onHandleNext").iterator();
		while (it1.hasNext()) {
			it1.next();
			it1.remove();
		}
		p_comp.addEventListener("onHandleNext", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("onHandleNext:%s", event);
				//Round 2, fill formula fields
				for (Map<String, Object> sm : setComponentValueList) {
					Component comp = (Component)sm.get("comp");
					JsonObject jo = (JsonObject)sm.get("jo");
					String joKey = (String)sm.get("joKey");
					if (ZkUtil.isFormula(comp) && !ZkUtil.isNoPaste(comp)) {
						UniLog.log1("comp:%s, joKey:%s, isFormula:%b", comp, joKey, ZkUtil.isFormula(comp));
						setComponentValue(comp, jo, joKey);
					}
				}

				//map to listbox item datas
				final Iterator<Map.Entry<Listbox, JsonArray>> it = lbMap.entrySet().iterator();
				if (it.hasNext()) {
					MapToComponentDatasCallback callback1 = new MapToComponentDatasCallback() {
						@Override
						public void completed(String errMsg) {
							if (errMsg == null) {
								if (it.hasNext()) {
									Map.Entry<Listbox, JsonArray> entry = it.next();
									mapToListboxItemDatas(entry.getKey(), entry.getValue(), this);
								}
								else
									callback.completed(null);
							}
							else
								callback.completed(errMsg);
						}
						
					};
					Map.Entry<Listbox, JsonArray> entry = it.next();
					mapToListboxItemDatas(entry.getKey(), entry.getValue(), callback1);
				}
				else
					callback.completed(null);
			}
		});
		ZkUtil.delayPostEvent("onHandleNext", p_comp, null, 1000);
	}

	private static void mapToComponentDatas(final JsonObject map, Component p_comp, final int maxRecursiveLevel, Map<Listbox, JsonArray> lbMap, List<Map<String, Object>> setComponentValueList) throws Exception {
		if (p_comp == null || maxRecursiveLevel == 0)
			return;
		List<Component> comps = p_comp.getChildren();
		if (comps == null || comps.isEmpty())
			return;
		Map<String, JsonArray> subViewMap = new HashMap<String, JsonArray>();
		JsonElement je = map.get("viewlist");
		if (je != null) {
			JsonArray ja = je.getAsJsonArray();
			for (int i = 0; i < ja.size(); i++) {
				JsonObject jo = ja.get(i).getAsJsonObject();
				JsonArray ja1 = jo.get("data").getAsJsonArray();
				if (ja1.size() > 0)
					subViewMap.put(jo.get("viewid").getAsString(), ja1);
			}
		}

		for (final Component comp : comps) {
			final String id = comp.getId();
			if (StringUtils.isNotBlank(id)) {
				if (comp instanceof Listbox && id.startsWith("list_")) {
					String subViewName = JxZkBiBase.replaceViewName(id.substring(5));
					final JsonArray ja = subViewMap.get(subViewName);
					final Listbox lb = (Listbox)comp;
					lbMap.put(lb, ja);
				}
				//else if (!setComponentValue(comp, map, id))
				//	mapToComponentDatas(map, comp, maxRecursiveLevel - 1, lbMap);
				else if (isValidJsonKey(map, id))
					setComponentValueList.add((Map)MapUtil.of("comp", comp, "jo", map, "joKey", id));
				else
					mapToComponentDatas(map, comp, maxRecursiveLevel - 1, lbMap, setComponentValueList);
			}
			else
				mapToComponentDatas(map, comp, maxRecursiveLevel - 1, lbMap, setComponentValueList);
		}
	}
	
	private static Object getComponentValue(Component comp) {
		if (comp == null)
			return null;
		if (comp instanceof Label)
			return ((Label)comp).getValue();
		else if (comp instanceof Intbox)
			return ((Intbox)comp).getValue();
		else if (comp instanceof Doublebox)
			return ((Doublebox)comp).getValue();
		else if (comp instanceof Datebox)
			return ((Datebox)comp).getValue();
		else if (comp instanceof Spinner)
			return ((Spinner)comp).getValue();
		else if (comp instanceof Checkbox)
			return ((Checkbox)comp).isChecked();
		else if (comp instanceof Radiogroup) {
			Radiogroup rg = (Radiogroup)comp;
			Radio rd = rg.getSelectedItem();
			return (rd != null && rd.getValue() instanceof String) ? rd.getValue() : null;
		}
		else if (comp instanceof Listbox) {
			Listbox lb = (Listbox)comp;
			if (StringUtils.equals(lb.getMold(), "select")) {
				if (!lb.isMultiple()) {
					Listitem li = lb.getSelectedItem();
					if (li != null) {
						if (li.getValue() != null && li.getValue() instanceof String)
							return li.getValue();
						else
							return li.getLabel();
					}
				}
			}
		}
		else if (comp instanceof S2Listbox) {
			S2Listbox s2 = (S2Listbox) comp;
			if (s2.getChildren() != null && !s2.getChildren().isEmpty() && s2.getFirstChild() instanceof Listbox)
				return getComponentValue(s2.getFirstChild());
		}
		else if (comp instanceof Textbox) {
			Textbox tb = (Textbox)comp;
			if (!StringUtils.equals(tb.getType(), "password"))
				return tb.getText();
		}
		return null;
	}
	
	private static boolean isValidJsonKey(JsonObject jo, String joKey) {
		if (StringUtils.isBlank(joKey))
			return false;
		JsonElement je = jo.get(joKey);
		if (je == null)
			return false;
		return true;
	}
	
	private static String getJsonValue(Component comp, JsonObject jo, String joKey) {
		if (!isValidJsonKey(jo, joKey))
			return null;
		if (!comp.isVisible())
			return null;
		if (ZkUtil.isNoPaste(comp)) {
			return null;
		}
		JsonElement je = jo.get(joKey);
		return je.getAsString();
	}

	private static void setComponentValue(Component comp, JsonObject jo, String joKey) throws Exception {
		//UniLog.log1("joKey:%s, comp:%s isformula:%s", joKey, comp, ZkUtil.isFormula(comp));
		String v = getJsonValue(comp, jo, joKey);
		//UniLog.log1("v:%s", v);
		if (comp instanceof Intbox) {
			Intbox ib = (Intbox)comp;
			if (ib.isDisabled() || ib.isReadonly())
				return;
			ib.setValue(toInt(v));
			Events.postEvent(Events.ON_CHANGE, ib, null);
		}
		else if (comp instanceof Doublebox) {
			Doublebox db = (Doublebox)comp;
			//UniLog.log1("toDouble:%f, isDisabled:%b, isReadonly:%b", toDouble(v), db.isDisabled(), db.isReadonly());
			if (db.isDisabled() || db.isReadonly())
				return;
			db.setValue(toDouble(v));
			Events.postEvent(Events.ON_CHANGE, db, null);
		}
		else if (comp instanceof Datebox) {
			Datebox db = (Datebox)comp;
			if (db.isDisabled() || db.isReadonly())
				return;
			db.setValue(DateUtil.dateTimeStrToDate(v));
			Events.postEvent(Events.ON_CHANGE, db, null);
		}
		else if (comp instanceof Spinner) {
			Spinner sp = (Spinner)comp;
			if (sp.isDisabled() || sp.isReadonly())
				return;
			sp.setValue(toInt(v));
			Events.postEvent(Events.ON_CHANGE, sp, null);
		}
		else if (comp instanceof Checkbox) {
			Checkbox cb = (Checkbox)comp;
			if (cb.isDisabled())
				return;
			boolean b = BooleanUtils.toBoolean(v);
			cb.setChecked(b);
			Events.postEvent(new CheckEvent(Events.ON_CHECK, cb, b));
		}
		else if (comp instanceof Radiogroup) {
			Radiogroup rg = (Radiogroup)comp;
			for (int i = 0; i < rg.getItemCount(); i++) {
				Radio rd = rg.getItemAtIndex(i);
				if (rd.isDisabled())
					return;
			}
			for (int i = 0; i < rg.getItemCount(); i++) {
				Radio rd = rg.getItemAtIndex(i);
				if (Objects.equals(rd.getValue(), v)) {
					rg.setSelectedIndex(i);
					Events.postEvent(new CheckEvent(Events.ON_CHECK, rd, true));
					break;
				}
			}
		}
		else if (comp instanceof Combobox) {
			Combobox cb = (Combobox)comp;
			if (cb.isDisabled())
				return;
			int i = 0;
			for (; i < cb.getItemCount(); i++) {
				if (Objects.equals(cb.getItemAtIndex(i).getValue(), v)) {
					cb.setSelectedIndex(i);
					Events.postEvent(Events.ON_CHANGE, cb, null);
					break;
				}
			}
			if (i == cb.getItemCount()) {
				cb.setSelectedIndex(-1);
				cb.setText(v);
				Events.postEvent(Events.ON_CHANGE, cb, null);
			}
		}
		else if (comp instanceof Listbox) {
			Listbox lb = (Listbox)comp;
			if (lb.isDisabled())
				return;
			if (StringUtils.equals(lb.getMold(), "select")) {
				if (!lb.isMultiple()) {
					int i = 0;
					for (; i < lb.getItemCount(); i++) {
						Listitem li = lb.getItemAtIndex(i);
						if (Objects.equals(li.getValue(), v) || StringUtils.equals(li.getLabel(), v)) {
							lb.setSelectedIndex(i);
							Events.postEvent(new SelectEvent<Listitem, Object>(Events.ON_SELECT, lb, Sets.newHashSet(lb.getItemAtIndex(i)), lb.getItemAtIndex(i)));
							break;
						}
					}
					if (i == lb.getItemCount()) {
						lb.setSelectedIndex(-1);
						Events.postEvent(new SelectEvent<Listitem, Object>(Events.ON_SELECT, lb, null, null));
					}
				}
			}
		}
		else if (comp instanceof S2Listbox) {
			S2Listbox s2 = (S2Listbox) comp;
			if (s2.getChildren() != null && !s2.getChildren().isEmpty() && s2.getFirstChild() instanceof Listbox) {
				Listbox lb = (Listbox)s2.getFirstChild();
				setComponentValue(lb, jo, joKey);
				ZkUtil.delayJs(lb,null,50,"zkbis2.setup('%s',%s,%s,'%s',%b,%b);$('#%s').focus()",lb.getUuid(), false, false, "", false, false, lb.getUuid());
			}
		}
		else if (comp instanceof Textbox) {
			Textbox tb = (Textbox)comp;
			if (tb.isDisabled() || tb.isReadonly() || StringUtils.equals(tb.getType(), "password"))
				return;
			tb.setText(v);
			Events.postEvent(Events.ON_CHANGE, tb, null);
		}
	}

	private static boolean sendAddListItemEvent(Listbox lb, Component headAddButton) {
		List<Listitem> lis = lb.getItems();
		int osize = lis.size();
		if (osize > 0) {
			List<Component> lis1 = lis.get(osize - 1).getChildren();
			if (lis1 != null && !lis1.isEmpty() && lis1.get(0) instanceof Listcell) {
				for (Component c : lis1.get(0).getChildren()) {
					if (c instanceof Button && c.hasAttribute("isAddButton")) {
						Events.sendEvent(Events.ON_CLICK, c, null);
						return true;
					}
				}
			}
		}
		else {
			Events.sendEvent(Events.ON_CLICK, headAddButton, null);
			return true;
		}
		return false;
	}
	
	private static boolean isInputComponent(Component comp) {
		return comp instanceof Intbox
				|| comp instanceof Doublebox
				|| comp instanceof Datebox
				|| comp instanceof Spinner
				|| comp instanceof Checkbox
				|| comp instanceof Radiogroup
				|| comp instanceof Listbox
				|| comp instanceof S2Listbox
				|| comp instanceof Textbox;
	}

	private static void findAllInputChildComponents(Component p_comp, List<Component> childCompList, int maxRecursiveLevel) {
		if (p_comp == null || maxRecursiveLevel == 0)
			return;
		List<Component> comps = p_comp.getChildren();
		if (comps == null || comps.isEmpty())
			return;
		for (Component comp : comps) {
			if (isInputComponent(comp))
				childCompList.add(comp);
			else
				findAllInputChildComponents(comp, childCompList, maxRecursiveLevel - 1);
		}
	}
	
	private static String getComponentCellLabel(Component c) {
		if (c instanceof S2Listbox) {
			S2Listbox s2 = (S2Listbox) c;
			if (s2.getChildren() != null && !s2.getChildren().isEmpty() && s2.getFirstChild() instanceof Listbox)
				c = s2.getFirstChild();
		}
		Object o = c.getAttribute("CellValueMapper");
		if (o != null && o instanceof ZkBiCellValueMapper) {
			ZkBiCellValueMapper mapper = (ZkBiCellValueMapper)o;
			if (mapper.getBindedCell() != null && StringUtils.isNotBlank(mapper.getBindedCell().getCellLabel()))
				return mapper.getBindedCell().getCellLabel();
		}
		return null;
	}
	
	private static void addJsonProperty(JsonObject jo, String property, Object obj) {
		if (obj instanceof String)
			jo.addProperty(property, (String)obj);
		else if (obj instanceof Number)
			jo.addProperty(property, (Number)obj);
		else if (obj instanceof Character)
			jo.addProperty(property, (Character)obj);
		else if (obj instanceof Boolean)
			jo.addProperty(property, (Boolean)obj);
		else if (obj instanceof Date)
			jo.addProperty(property, sdf.format((Date)obj));
	}

	private static int toInt(String s) {
		return NumberUtils.toInt(StringUtils.replace(s, ",", ""));
	}

	private static double toDouble(String s) {
		return NumberUtils.toDouble(StringUtils.replace(s, ",", ""));
	}

	private static interface MapToComponentDatasCallback {
		void completed(String errMsg);
	}
}
