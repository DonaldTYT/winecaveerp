package com.uniinformation.zkbi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkforge.ckez.CKeditor;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Space;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.MessageboxDlg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.uniinformation.bicore.BiView;
import com.uniinformation.utils.FilingUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiHelpDialog {
	private final String keyPrefix = "zkbi_help_";

	private final SessionHelper sessionHelper;
	private final String specViewId, specViewName;
	private final String title;

  	private GridHelper gridAllSec;
	private final Html hmView;
	private final Html hmGeneral;
	
	private MessageboxDlg mainDialog;

	private final CKeditor ckView;
	private final CKeditor ckGeneral;

	private final String viewKey;
	private final String generalKey;
		
	private final Component parentComp;
	private Button btnHelp;

 	private List<Map<String, String>> allHelpList;

	public ZkBiHelpDialog(SessionHelper sessionHelper, Button btnHelp, Component parentComp, String title, String specViewId, String specViewName) {
		this.sessionHelper = sessionHelper;
		this.btnHelp = btnHelp;
		this.parentComp = parentComp;
		this.title = title;
		this.specViewId = specViewId;
		this.specViewName = specViewName;

		viewKey = keyPrefix + specViewId;
		generalKey = keyPrefix + "general";

		hmView = new Html();
		hmGeneral = new Html();
		ckView = buildCKeditor("help_view_ckeditor");
		ckGeneral = buildCKeditor("help_general_ckeditor");
		
		for (EventListener<? extends Event> el : btnHelp.getEventListeners(Events.ON_CLICK))
			btnHelp.removeEventListener(Events.ON_CLICK, el);
		btnHelp.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				show();
			}
		});

		String content = null;
		try {
			String key = keyPrefix + specViewId;
			JSONObject json = FilingUtil.getJson(sessionHelper.getAgent(), null, key);
			if (json != null) {
				content = json.optString("content");
				if (StringUtils.isBlank(specViewName) && sessionHelper.getBiSchema().getViewByName(specViewId) == null)
					specViewName = json.optString("viewName");
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		if (StringUtils.isBlank(specViewName) && sessionHelper.getBiSchema().getViewByName(specViewId) == null)
			specViewName = specViewId;
		UniLog.log1("specViewId:%s, specViewName:%s", specViewId, specViewName);
		updateHelpBtnStyle(sessionHelper, content, specViewId, btnHelp);
	}
	private String getValue(String key) throws Exception {
		JSONObject json = FilingUtil.getJson(sessionHelper.getAgent(), null, key);
		String value = "";
		if (json != null) {
			value = json.optString("content");
			if (!json.optString("type").equals("ckeditor"))
				value = value.replaceAll("\\r\\n|\\r|\\n", "<br>");
		}
		UniLog.log1("help key:%s, value:%s", key, value);
		return value;
	}
	private void saveValue(String key, String viewName, CKeditor ck, boolean updateHelpBtnFlag) throws Exception {
		String oldValue = getValue(key);
		String newValue = ck.getValue();
		if (!StringUtils.equals(oldValue, newValue)) {
			UniLog.log1("save key:%s", key);
			JSONObject json = new JSONObject();
			json.put("type", "ckeditor");
			json.put("content", ck.getValue());
			if (StringUtils.isNotBlank(viewName))
				json.put("viewName", viewName);
			FilingUtil.storeJson(sessionHelper.getAgent(), null, key, key, key, json);
			if (updateHelpBtnFlag) {
				updateHelpBtnStyle(sessionHelper, ck.getValue(), specViewId, btnHelp);
			}
			sessionHelper.putSessionData("zkbi_help_list", null);
			allHelpList = null;
		}
	}
	private CKeditor buildCKeditor(String id) {
		final CKeditor ck = new CKeditor();
		ck.setCustomConfigurationsPath("/js/zkckeditor_config.js");
		//ck.setToolbar("Simple");
		ck.setToolbar("HelpCk");
		final Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put("readOnly", !sessionHelper.isAdminUser());
		configMap.put("removePlugins", "elementspath");
		configMap.put("resize_enabled", false);
		ck.setConfig(configMap);
		ck.setId(id);
		ck.setVflex("1");
		ck.setHflex("1");
		return ck;
	}
	private Div buildHtmlDiv(final Html hm) {
		return new Div() {{
			appendChild(hm);
			setVflex("1");
			setHflex("1");
			setStyle("overflow:auto; padding:10px; border:1px solid #d9d9d9;");
		}};
	}
	private GridHelper buildAllSecGrid() {
     	GridHelper gh = new GridHelper(2);
		gh.setSclass("zkbi-da");
	    gh.getColumn(0).setHflex("min");
	    gh.getColumn(1).setHflex("1");
	    gh.appendChild(new Auxhead() {{
  			appendChild(new Auxheader() {{ appendChild(new Label(sessionHelper.getLabel("View Id"))); }});
  			appendChild(new Auxheader() {{ appendChild(new Label(sessionHelper.getLabel("View Name"))); }});
	    }});
     	return gh;
	}

	private void buildImportDialog() {
	    Fileupload.get(new ZkBiEventListener <UploadEvent>(){
	    	public void onZkBiEvent(UploadEvent event) {
	       		UniLog.log("upload event catched");
	       		org.zkoss.util.media.Media media = event.getMedia();
	       		if (media != null && media instanceof org.zkoss.util.media.AMedia) {
	       			org.zkoss.util.media.AMedia amedia = (org.zkoss.util.media.AMedia) media;

       				final List<Map<String, Object>> helpList = new ArrayList<Map<String, Object>>();
	       			try {
	       				String s = new String(amedia.getByteData(), "UTF-8");
	       				JSONArray jsonArr = new JSONArray(s);
	       				for (int i = 0; i < jsonArr.length(); i++) {
	       					JSONObject jsonObj = jsonArr.getJSONObject(i);
	       					final String viewId = jsonObj.optString("viewId");
	       					final String content = jsonObj.optString("content");
	       					String viewName = jsonObj.optString("viewName");
	       					final boolean needStoreViewName = StringUtils.isNotBlank(viewName);
       						if (StringUtils.isNotBlank(viewId)) {
	       						if (!viewId.equals("general")) {
	       							BiView biView = sessionHelper.getBiSchema().getViewByName(viewId);
	       							if (biView != null)
	       								viewName = biView.getHeader();
	       						}
	       						else
	       							viewName = "General";
       						}
	       					final String viewName1 = viewName;
	       					if (StringUtils.isNotBlank(viewName1)) {
   								helpList.add(new HashMap<String, Object>(){{
       								put("viewId", viewId);
       								put("viewName", viewName1);
       								put("content", content);
       								put("enabled", true);
       								put("needStoreViewName", needStoreViewName);
       							}});
	       					}	
	       				}
	       				//sort by viewName
	       				helpList.sort(new Comparator<Map<String, Object>>() {
	       					@Override
	       					public int compare(Map<String, Object> o1, Map<String, Object> o2) {
	       						return StringUtils.compare((String)o1.get("viewName"), (String)o2.get("viewName"));
	       					}
	       				});
	       			}
	       			catch (Exception e) {
	       				e.printStackTrace();
	       				ZkUtil.errMsg(sessionHelper.getLabel("Invalid import file") + ".\n" + e.getMessage());
	       				return;
	       			}
	       			
	       			if (helpList.isEmpty()) {
	       				ZkUtil.errMsg(sessionHelper.getLabel("Help data not found"));
	       				return;
	       			}

	       			final GridHelper gh = new GridHelper(3);
	       			final Checkbox cbAll = new Checkbox();
	       			gh.setSclass("zkbi-da");
	       			gh.getColumn(0).setHflex("min");
	       			gh.getColumn(1).setHflex("1");
	       			gh.getColumn(2).setHflex("min");
	       			gh.appendChild(new Auxhead() {{
  		       			appendChild(new Auxheader() {{ appendChild(cbAll); }});
  		       			appendChild(new Auxheader() {{ appendChild(new Label(sessionHelper.getLabel("View Name"))); }});
  		       			appendChild(new Auxheader() {{ appendChild(new Label(sessionHelper.getLabel("View Id"))); }});
	       			}});
	       			for (final Map<String, Object> map : helpList) {
	       				Checkbox cbEnable = new Checkbox();
	       				cbEnable.setChecked((Boolean)map.get("enabled"));
	       				gh.addRow(cbEnable, new Label((String)map.get("viewName")), new Label((String)map.get("viewId")));
	       				map.put("enableComp", cbEnable);
	       				cbEnable.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
							@Override
							public void onZkBiEvent(CheckEvent event) throws Exception {
								map.put("enabled", event.isChecked());
								int checkedCount = 0;
								for (Map<String, Object> m : helpList) {
									if ((Boolean)m.get("enabled"))
										checkedCount++;
								}
								if (checkedCount == 0)
									cbAll.setChecked(false);
								else if (checkedCount == helpList.size())
									cbAll.setChecked(true);
							}
	       				});
	       			}
	       			cbAll.setChecked(true);
	       			cbAll.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
						@Override
						public void onZkBiEvent(CheckEvent event) throws Exception {
							for (Map<String, Object> m : helpList) {
								m.put("enabled", event.isChecked());
								((Checkbox)m.get("enableComp")).setChecked(event.isChecked());
							}
						}
	       			});

	       			final MessageboxDlg dlg = ZkUtil.buildSimpleMessageboxDlg(sessionHelper.getLabel("The following view will be updated"), gh, 
       					new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
       					new String[]{sessionHelper.getBtLabel("Confirm"), sessionHelper.getBtLabel("Cancel")},
       					parentComp, new ZkBiEventListener<Messagebox.ClickEvent>(){
	       				@Override
	       				public void onZkBiEvent(ClickEvent event) throws Exception {
	       					UniLog.log1("event:%s", event);
	       					if (event.getButton() == null)
	       						return;
	       					switch (event.getButton()) {
	       					case OK: //store json data
	       						boolean checkedItem = false;
	       						for (Map<String, Object> map : helpList) {
	       							if (!(Boolean)map.get("enabled"))
	       								continue;
	       							checkedItem = true;
	       						}
	       						if (!checkedItem) {
	       							Clients.evalJavaScript("$('#"+gh.getUuid()+"').notify(\"Please check item\", { className: \"error\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
	       							event.stopPropagation();
	       							break;
	       						}
								int okCount = 0;
								int failCount = 0;
	       						for (Map<String, Object> map : helpList) {
	       							if (!(Boolean)map.get("enabled"))
	       								continue;
	       							String vid = (String)map.get("viewId");
	       							String vname = (String)map.get("viewName");
	       							String content = (String)map.get("content");
	       							boolean needStoreViewName = (Boolean)map.get("needStoreViewName");
	       							String key = keyPrefix + vid;
	       							JSONObject json = new JSONObject();
	       							json.put("type", "ckeditor");
	       							json.put("content", content);
	       							if (needStoreViewName)
	       								json.put("viewName", vname);
	       							ReturnMsg rtn;
	       							try {
	       								rtn = FilingUtil.storeJson(sessionHelper.getAgent(), null, key, key, key, json);
	       								if (StringUtils.equals(vid, specViewId)) {
	       									hmView.setContent(content);
	       									ckView.setValue(content);
	       									updateHelpBtnStyle(sessionHelper, content, specViewId, btnHelp);
	       								}
	       								else if (StringUtils.equals(vid, "general")) {
	       									hmGeneral.setContent(content);
	       									ckGeneral.setValue(content);
	       								}
	       							}
	       							catch (Exception e) {
	       								rtn = new ReturnMsg(e);
	       							}
	       							if (rtn.getStatus())
	       								okCount++;
	       							else
	       								failCount++;
	       						}
	       						StringBuilder sb = new StringBuilder();
	       						if (okCount > 0)
	       							sb.append(", Ok: " + okCount);
	       						if (failCount > 0)
	       							sb.append(", Fail: " + failCount);
	       						if (sb.length() > 0)
	       							sb.delete(0, 2);
    							buildAllSectionTabpanel(true);
	       						ZkUtil.msg(sessionHelper.getLabel("Import from file finished") + ". " + sb.toString());
	       						break;
	       					}
	       				}
	       			});
	       			gh.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<Event>() {
	  		       		@Override
	  		       		public void onZkBiEvent(Event event) throws Exception {
	  		       			Integer oldGridHeight = (Integer) gh.getAttribute("gridHeight");
	  		       			int newGridHeight = (event instanceof AfterSizeEvent) ? ((AfterSizeEvent)event).getHeight() : (Integer)gh.getAttribute("gridHeight");
	  		       			int gridHeaderHeight = 34;
	  		       			if (oldGridHeight == null || newGridHeight != oldGridHeight) {
		  		       			int newDlgHeight = (gridHeaderHeight + newGridHeight + 2 + 32 + 48 + 16); //2: grid border, 32: titlebar, 48: bottombar, 16: other padding/border...
		  		       			UniLog.log1("newGridHeight:%d, oldGridHeight:%d", newGridHeight, oldGridHeight);
		  		       			dlg.setHeight(newDlgHeight + "px");
		  		       			dlg.setAttribute("dlgHeight", newDlgHeight);
		  		       			gh.setAttribute("gridHeight", newGridHeight);
	  		       			}
  		       			}
	       			});
	       			dlg.setWidth("500px");
	       			dlg.setStyle("max-width:100%;max-height:" + (sessionHelper.isMobile() ? "100" : "95") + "%");
	       			dlg.doHighlighted();
	       		}
	    	}
	    });
	}
	private void buildExportDialog() {
		final Radiogroup radiogroup = new Radiogroup() {{
			appendChild(new Radio(sessionHelper.getLabel("Current View")));
			appendChild(new Radio(sessionHelper.getLabel("All View")));
			setSelectedIndex(1);
		}};
		final Checkbox cbIncludeGeneralSection = new Checkbox(sessionHelper.getLabel("Include General Section")) {{
			setChecked(true);
		}};
		final Checkbox cbIncludeBlankContentView = new Checkbox(sessionHelper.getLabel("Include blank content view")) {{
			setChecked(true);
		}};
		final Vlayout vl = new Vlayout() {{
			appendChild(radiogroup);
			appendChild(cbIncludeGeneralSection);
			appendChild(cbIncludeBlankContentView);
			setStyle("padding:10px 10px 0 10px");
		}};
		MessageboxDlg dlg = ZkUtil.buildSimpleMessageboxDlg(sessionHelper.getLabel("Confirm export to file?"), vl, 
				new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
				parentComp, new ZkBiEventListener<Messagebox.ClickEvent>(){
					@Override
					public void onZkBiEvent(ClickEvent event) throws Exception {
						UniLog.log1("event:%s", event);
						if (event.getButton() == null)
							return;
						switch (event.getButton()) {
						case OK:
							List<String> keyList = new ArrayList<String>();
							if (radiogroup.getSelectedIndex() == 1) //all views
								keyList.addAll(FilingUtil.getKeys(sessionHelper.getAgent(), keyPrefix + "%"));
							else {
								keyList.add(keyPrefix + "general");
								keyList.add(keyPrefix + specViewId);
							}
							if (!cbIncludeGeneralSection.isChecked())
								keyList.remove(keyPrefix + "general");

							List<Map<String, String>> joList = new ArrayList<Map<String, String>>();
							for (String key : keyList) {
								final JSONObject jsonSrc = FilingUtil.getJson(sessionHelper.getAgent(), null, key);
								if (jsonSrc != null && jsonSrc.optString("type").equals("ckeditor")) {
									final String viewId = key.substring(keyPrefix.length());
									final String content = jsonSrc.optString("content").replaceAll("[\\n\\r]", "");
									final String viewName = jsonSrc.optString("viewName");
									if (StringUtils.isNotBlank(content) || cbIncludeBlankContentView.isChecked()) {
										joList.add(new HashMap<String, String>() {{
											put("viewId", viewId);
											put("content", content);
											if (StringUtils.isNotBlank(viewName))
												put("viewName", viewName);
										}});
									}
								}
							}
							Gson gson = new GsonBuilder()
								.setPrettyPrinting()
								.disableHtmlEscaping()
								.create();

							String json = gson.toJson(joList, new TypeToken<List<Map<String, String>>>() {}.getType());
							Filedownload.save(json, "application/json", "help_" + sessionHelper.getLoginId() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".json");
							break;
						}
					}
		});
		dlg.setWidth("250px");
		dlg.doHighlighted();
	}
	
	private void buildAllSectionTabpanel(boolean reset) throws Exception {
		if (reset) {
			sessionHelper.putSessionData("zkbi_help_list", null);
			allHelpList = null;
			Rows rows = gridAllSec.getRows();
			while (!rows.getChildren().isEmpty())
				rows.removeChild(gridAllSec.getRow(0));
		}
		if (allHelpList == null)
			allHelpList = (List<Map<String, String>>)sessionHelper.getSessionData("zkbi_help_list");
		if (allHelpList == null) {
			new ZkBiAbstractLongOp(mainDialog, "Loading...", 50) {
				@Override
				public ReturnMsg longOp() {
					try {
						allHelpList = new ArrayList<Map<String, String>>();
						List<String> keyList = FilingUtil.getKeys(sessionHelper.getAgent(), keyPrefix + "%");
						for (String key : keyList) {
							final JSONObject jsonSrc = FilingUtil.getJson(sessionHelper.getAgent(), null, key);
							if (jsonSrc != null && jsonSrc.optString("type").equals("ckeditor")) {
								final String viewId = key.substring(keyPrefix.length());
								final String content = jsonSrc.optString("content").replaceAll("[\\n\\r]", "");
								final String viewName = jsonSrc.optString("viewName");
								UniLog.log1("viewId:%s, isNoneBlank:%b, checkSideMenuHelpExist:%b", viewId, StringUtils.isNotBlank(content), sessionHelper.checkSideMenuHelpExist(viewId));
								if (StringUtils.isNoneBlank(content) && sessionHelper.checkSideMenuHelpExist(viewId)) {
									allHelpList.add(new HashMap<String, String>() {{
										put("viewId", viewId);
										//put("content", content);
										if (StringUtils.isNotBlank(viewName))
											put("viewName", viewName);
										if (!viewId.equals("general")) {
											BiView biView = sessionHelper.getBiSchema().getViewByName(viewId);
											if (biView != null)
												put("viewName", biView.getHeader());
										}
										else
											put("viewName", "General");
									}});
								}
							}
						}
						//sort by viewName
						allHelpList.sort(new Comparator<Map<String, String>>() {
							@Override
							public int compare(Map<String, String> o1, Map<String, String> o2) {
								return StringUtils.compare(o1.get("viewName"), o2.get("viewName"));
							}
						});
						sessionHelper.putSessionData("zkbi_help_list", allHelpList);
						buildAllSectionTabpanel1();
					}
					catch (Exception e) {
						e.printStackTrace();
						ZkUtil.errMsg(sessionHelper.getLabel("Load all help data fail") + ".\n%s", e.getMessage());
					}
					return null;
				}
			};
		}
		else
			buildAllSectionTabpanel1();
	}

	private void buildAllSectionTabpanel1() {
		if (gridAllSec.getRows().getChildren().isEmpty() && !allHelpList.isEmpty()) {
			int rowNum = 0;
			for (final Map<String, String> map : allHelpList) {
				gridAllSec.addRow(new Label(map.get("viewId")), new Label(map.get("viewName")));
				//UniLog.log1("check %s help:%s view:%s", map.get("viewId"), sessionHelper.checkSideMenuHelpExist(map.get("viewId")), sessionHelper.checkSideMenuViewExist(map.get("viewId")));
				final Row row = gridAllSec.getRow(rowNum);
				row.setStyle("cursor:pointer");
				row.setTooltiptext(sessionHelper.getLabel("Click to show") + " " + map.get("viewName"));
				row.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						UniLog.log1("clicked event:%s", event);
						buildAllSecViewDialog(map);
					}
				});
				rowNum++;
			}
		}
		gridAllSec.invalidate();
	}
	
	private void buildAllSecViewDialog(Map<String, String> viewMap) throws Exception {
		final String viewId = viewMap.get("viewId");
		final String viewName = viewMap.get("viewName");
		final Html hmView = new Html(getValue(keyPrefix + viewId));
		final Div divHmView = buildHtmlDiv(hmView);
		MessageboxDlg dlg = ZkUtil.buildSimpleMessageboxDlg(viewName + " " + sessionHelper.getLabel("Section"), divHmView, 
						new Messagebox.Button[]{Messagebox.Button.CANCEL},
						new String[]{sessionHelper.getBtLabel("CLOSE")}, 
				parentComp, new ZkBiEventListener<Messagebox.ClickEvent>(){
					@Override
					public void onZkBiEvent(ClickEvent event) throws Exception {
						UniLog.log("helperButton target:" + event.getTarget() + ",button:" + event.getButton());
						if (event.getButton() == null)
							return;
					}
				});
		dlg.setWidth("90%");
		dlg.setHeight("90%");
		dlg.doHighlighted();
	}
	
	public void show() throws Exception {
		final Div divHmView = buildHtmlDiv(hmView);
		final Div divHmGeneral = buildHtmlDiv(hmGeneral);
		gridAllSec = buildAllSecGrid();

		final Tabpanel tabpanelView = new Tabpanel() {{
			appendChild(divHmView);
			appendChild(ckView);
		}};
		final Tabpanel tabpanelGeneral = new Tabpanel() {{
			appendChild(divHmGeneral);
			appendChild(ckGeneral);
		}};
		final Tabpanel tabpanelAllSec = new Tabpanel() {{
			appendChild(gridAllSec);
		}};
		final Tabbox tabbox = new Tabbox() {{
			appendChild(new Tabs() {{
				appendChild(new Tab(StringUtils.isBlank(title) ? sessionHelper.getLabel("Local Section") : title + " " + sessionHelper.getLabel("Section")));
				appendChild(new Tab(sessionHelper.getLabel("General Section")));
				appendChild(new Tab(sessionHelper.getLabel("All Section")));
			}});
			appendChild(new Tabpanels() {{
				appendChild(tabpanelView);
				appendChild(tabpanelGeneral);
				appendChild(tabpanelAllSec);
			}});
		}};

		String viewValue = getValue(viewKey);
		String generalValue = getValue(generalKey);
		hmView.setContent(viewValue);
		divHmView.setVisible(true);
		hmGeneral.setContent(generalValue);
		divHmGeneral.setVisible(true);
		ckView.setValue(viewValue);
		ckView.setVisible(false);
		ckGeneral.setValue(generalValue);
		ckGeneral.setVisible(false);
		
		//If the view specific section with content, set view specific tab active
		tabbox.setSelectedPanel(StringUtils.isNotBlank(viewValue) ? tabpanelView : tabpanelGeneral);
		
		tabbox.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<?, ?>>() {
			@Override
			public void onZkBiEvent(SelectEvent<?, ?> event) throws Exception {
				UniLog.log1("event:%s, ref:%s", event, event.getReference());
				Tab tab = (Tab)event.getReference();
				if (StringUtils.equals(tab.getLabel(), "All Section"))
					buildAllSectionTabpanel(false);
			}
		});

		Clients.evalJavaScript("setTimeout(function(){"
				+ "jq('$help_view_ckeditor').addClass('zkbi-ckeditor');"
				+ "jq('$help_general_ckeditor').addClass('zkbi-ckeditor');"
				+ "},150);");
		
		final Map<String, Button> btnMap = new HashMap<String, Button>();
		mainDialog = ZkUtil.buildSimpleMessageboxDlg(sessionHelper.getLabel("Help"), tabbox, 
				sessionHelper.isAdminUser() ?
						new Messagebox.Button[]{Messagebox.Button.ABORT, Messagebox.Button.OK, Messagebox.Button.IGNORE, Messagebox.Button.YES, Messagebox.Button.NO, Messagebox.Button.CANCEL}
						: new Messagebox.Button[]{Messagebox.Button.CANCEL},
				sessionHelper.isAdminUser() ? 
						new String[]{sessionHelper.getBtLabel("EDIT"), sessionHelper.getBtLabel("OK"), sessionHelper.getBtLabel("CLEAR"), sessionHelper.getBtLabel("IMPORT"), sessionHelper.getBtLabel("EXPORT"), sessionHelper.getBtLabel("CLOSE")}  //andrew220103 rename CANCEL to CLOSE, remove PREVIEW
						: new String[]{sessionHelper.getBtLabel("CLOSE")}, 
				parentComp, new ZkBiEventListener<Messagebox.ClickEvent>(){
					@Override
					public void onZkBiEvent(ClickEvent event) throws Exception {
						UniLog.log("helperButton target:" + event.getTarget() + ",button:" + event.getButton());
						if (event.getButton() == null)
							return;
						switch (event.getButton()) {
						case OK:
							saveValue(viewKey, specViewName, ckView, true);
							saveValue(generalKey, null, ckGeneral, false);
							break;
						case ABORT: //EDIT
							divHmView.setVisible(false);
							divHmGeneral.setVisible(false);
							ckView.setVisible(true);
							ckGeneral.setVisible(true);
							btnMap.get(sessionHelper.getBtLabel("EDIT")).setVisible(false);
							btnMap.get(sessionHelper.getBtLabel("OK")).setVisible(true);
							btnMap.get(sessionHelper.getBtLabel("CLEAR")).setVisible(true);
							event.stopPropagation();
							break;
						case IGNORE: //CLEAR
							if (tabbox.getSelectedIndex() == 1)
								ckGeneral.setValue("");
							else
								ckView.setValue("");
							event.stopPropagation();
							break;
						case YES: //IMPORT
							buildImportDialog();
							event.stopPropagation();
							break;
						case NO: //EXPORT
							buildExportDialog();
							event.stopPropagation();
							break;
						default:
							break;
						}
					}
				});
		mainDialog.setWidth("90%");
		mainDialog.setHeight("90%");
		mainDialog.doHighlighted();
		//find buttons
		for (Component cbtn : mainDialog.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.isNotBlank(btn.getLabel()))
				btnMap.put(btn.getLabel(), btn);
		}
		if (sessionHelper.isAdminUser()) {
			btnMap.get(sessionHelper.getBtLabel("OK")).setVisible(false);
			btnMap.get(sessionHelper.getBtLabel("CLEAR")).setVisible(false);
		}
	}
	
	private static void updateHelpBtnStyle(SessionHelper p_sh, String p_helpContent, String p_viewId, HtmlBasedComponent p_comp){
		if (StringUtils.isNotBlank(p_helpContent)) {
			p_comp.setSclass("zkbi-header-help narrowtoolbarbutton zkbi-tbb-active");
		}
		else{
			p_comp.setSclass("zkbi-header-help narrowtoolbarbutton zkbi-tbb-inactive");
		}
	}
}
