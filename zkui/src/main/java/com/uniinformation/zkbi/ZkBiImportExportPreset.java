package com.uniinformation.zkbi;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zul.Filedownload;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Space;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.MessageboxDlg;

import com.uniinformation.utils.ConditionPresets;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiImportExportPreset {
	private Component parentComp;
	private SessionHelper sessionHelper;
	private ConditionPresets conditionPresets;
	private String viewId;
	
	private MessageboxDlg dialog;
	private Window window;
	private Checkbox cbPublicPreset;
	private Checkbox cbCustomPreset;
	private Checkbox cbRemoveExistingPreset;
	private Button btnImport, btnExport;
	
	private ImportPresetCallback importPresetCallback;
	
	interface ImportPresetCallback {
		void callback();
	}

	public ZkBiImportExportPreset(ConditionPresets conditionPresets, String viewId, Component parentComp, SessionHelper sessionHelper) {
		this.parentComp = parentComp;
		this.sessionHelper = sessionHelper;
		this.conditionPresets = conditionPresets;
		this.viewId = viewId;
		UniLog.log1("ZkBiImportExportPreset:%s, agent:%s", sessionHelper.getCustomBiViewBase(), sessionHelper.getAgent());
	}

	private Window createImportWindow() {
		cbPublicPreset = new Checkbox("Public Preset") {{
			setChecked(true);
			setStyle("margin-left: 10px");
		}};
		cbCustomPreset = new Checkbox("Custom Preset") {{
			setChecked(true);
			setStyle("margin-left: 10px");
		}};
		cbRemoveExistingPreset = new Checkbox("Remove existing Preset") {{
			setChecked(false);
			setStyle("margin-left: 10px");
		}};
		final Vbox vbox = new Vbox() {{
			setWidth("200px");
			appendChild(new Space(){{ this.setHeight("10px");}});
			appendChild(cbPublicPreset);
			appendChild(new Space(){{ this.setHeight("5px");}});
			appendChild(cbCustomPreset);
			appendChild(new Space(){{ this.setHeight("5px");}});
			appendChild(cbRemoveExistingPreset);
		}};
		window = new Window() {{
			setStyle("padding:0px !important; user-select: none;");
			appendChild(vbox);
		}};
		return window;
	}

	private Window createExportWindow() {
		cbPublicPreset = new Checkbox("Public Preset") {{
			setChecked(true);
			setStyle("margin-left: 10px");
		}};
		cbCustomPreset = new Checkbox("Custom Preset") {{
			setChecked(true);
			setStyle("margin-left: 10px");
		}};
		final Vbox vbox = new Vbox() {{
			setWidth("200px");
			appendChild(new Space(){{ this.setHeight("10px");}});
			appendChild(cbPublicPreset);
			appendChild(new Space(){{ this.setHeight("5px");}});
			appendChild(cbCustomPreset);
		}};
		window = new Window() {{
			setStyle("padding:0px !important; user-select: none;");
			appendChild(vbox);
		}};
		return window;
	}

	public void showImportDialog(ImportPresetCallback callback) {
		importPresetCallback = callback;
		dialog = ZkUtil.buildMessageboxDlg("Import Preset", createImportWindow(), 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
			new String[]{"Import", null}, 
			parentComp, 
			new ZkBiEventListener<Messagebox.ClickEvent>(){
				@Override
				public void onZkBiEvent(ClickEvent event) throws Exception {
					UniLog.log("import dialog click:" + event.getTarget() + ",button:" + event.getButton());
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
						case OK:
							event.stopPropagation();
							break;
						default:
							break;
					}
				}
			});
		dialog.doHighlighted();

		//find buttons
		for (Component cbtn : dialog.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), "Import"))
				btnImport = btn;
		}
		
		for (Checkbox cb : new Checkbox[] {cbPublicPreset, cbCustomPreset}) {
			cb.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					btnImport.setDisabled(!cbPublicPreset.isChecked() && !cbCustomPreset.isChecked());
				}
			});
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
					importPreset(new String(amedia.getByteData()));
				}
			}
		});
	}

	public void showExportDialog() {
		dialog = ZkUtil.buildMessageboxDlg("Export Preset", createExportWindow(), 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
			new String[]{"Export", null}, 
			parentComp, 
			new ZkBiEventListener<Messagebox.ClickEvent>(){
				@Override
				public void onZkBiEvent(ClickEvent event) throws Exception {
					UniLog.log("export dialog click:" + event.getTarget() + ",button:" + event.getButton());
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
						case OK:
							exportPreset();
							break;
						default:
							break;
					}
				}
			});
		dialog.doHighlighted();

		//find buttons
		for (Component cbtn : dialog.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), "Export"))
				btnExport = btn;
		}

		for (Checkbox cb : new Checkbox[] {cbPublicPreset, cbCustomPreset}) {
			cb.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					btnExport.setDisabled(!cbPublicPreset.isChecked() && !cbCustomPreset.isChecked());
				}
			});
		}
	}

	private void importPreset(String jsonString) {
		try {
			JSONObject json = new JSONObject(jsonString);
			String vId = json.getString("viewId");
			boolean errorFound = false;
			if (!vId.equals(viewId)) {
				ZkUtil.showErrMsg("Incorrect view id: %s", vId);
				errorFound = true;
			}

			//srcMap: from json
			final Map<String, ConditionPresets> srcMap = new HashMap<String, ConditionPresets>();
			//targetMap: from filing, current ConditionPresets
			final Map<String, ConditionPresets> targetMap = getCurrentConditionPresets(viewId);

			//current login user has public preset
			final ConditionPresets currUserConditionPresets = new ConditionPresets();
			srcMap.put(sessionHelper.getLoginId(), currUserConditionPresets);

			if (!errorFound) {
				JSONObject presetsObj = json.getJSONObject("presets");
				JSONArray publicPresetArr = presetsObj.optJSONArray("public");
				JSONObject customPresetObj = presetsObj.optJSONObject("custom");
				if (cbPublicPreset.isChecked() && publicPresetArr != null)
					currUserConditionPresets.parseConditionPreset(publicPresetArr, false, sessionHelper);
				if (cbCustomPreset.isChecked() && customPresetObj != null) {
					JSONArray names = customPresetObj.names();
					for (int i = 0; names != null && i < names.length(); i++) {
						String userId = names.getString(i);
						JSONArray contents = customPresetObj.optJSONArray(userId);
						ConditionPresets cp;
						if (contents != null) {
							if (userId.equals(sessionHelper.getLoginId()))
								cp = currUserConditionPresets;
							else {
								cp = new ConditionPresets();
								srcMap.put(userId, cp);
							}
							cp.parseConditionPreset(contents, true, sessionHelper);
						}
					}
				}
			}
			if (!errorFound) {
				if (!cbRemoveExistingPreset.isChecked()) {
					final Map<ConditionPresets, List<String>> needRemoveMap = new HashMap<ConditionPresets, List<String>>();
					for (Map.Entry<String, ConditionPresets> entry : srcMap.entrySet()) {
						String userId = entry.getKey();
						ConditionPresets srcCp = entry.getValue();
						ConditionPresets targetCp = targetMap.get(userId);
						if (targetCp != null) {
							List<String> list = new ArrayList<String>();
							for (String presetName : srcCp.getPresets()) {
								if (targetCp.getPresets().contains(presetName)) {
									list.add(presetName);
									UniLog.log1("existing preset:%s", presetName);
								}
							}
							if (!list.isEmpty())
								needRemoveMap.put(srcCp, list);
						}
					}
					
					if (!needRemoveMap.isEmpty()) {
						Messagebox.show("Preset already exist. Are you sure to overwrite it?", "Overwrite", 
								new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
								Messagebox.QUESTION, new ZkBiEventListener<Messagebox.ClickEvent>() {
								@Override
								public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
									switch (event.getButton()) {
									case OK:
										break;
									default:
										for (Map.Entry<ConditionPresets, List<String>> entry : needRemoveMap.entrySet()) {
											ConditionPresets cp = entry.getKey();
											List<String> presetNameList = entry.getValue();
											for (String presetName : presetNameList)
												cp.removeFieldMap(presetName);
										}
										break;
									}
									importPreset(srcMap, targetMap);
								}
						});
					}
					else
						importPreset(srcMap, targetMap);
				}
			}
		}
		catch (Exception e) {
			ZkUtil.showErrMsg("Import error: %s", e.toString());
			e.printStackTrace();
		}
	}
	
	private void importPreset(Map<String, ConditionPresets> srcMap, Map<String, ConditionPresets> targetMap) {
		int importCount = 0;
		String errMsg = null;
		for (Map.Entry<String, ConditionPresets> entry : srcMap.entrySet()) {
			String userId = entry.getKey();
			ConditionPresets srcCp = entry.getValue();
			ConditionPresets targetCp = targetMap.get(userId);
			if (!srcCp.hasPresets())
				continue;
			Pair<Integer, String> p = importPreset(userId, srcCp, targetCp);
			int count = p.getKey();
			errMsg = p.getValue();
			if (errMsg == null)
				importCount += count;
			else
				break;
		}
		if (errMsg == null)
			ZkUtil.showMsg("Import preset count: " + importCount);
		else
			ZkUtil.showErrMsg(errMsg);
		dialog.onClose();
		if (importPresetCallback != null)
			importPresetCallback.callback();
	}

	private Pair<Integer, String> importPreset(String userId, ConditionPresets srcCp, ConditionPresets targetCp) {
		String errMsg;
		if (targetCp != null) {
			//save for target ConditionPresets
			UniLog.log1("import targetCp userId:%s, preset size:%d, src preset size:%d", userId, targetCp.getPresets().size(), srcCp.getPresets().size());
			String publicDefaultPresetName = null;
			String customDefaultPresetName = null;
			for (String presetName : targetCp.getPresets()) {
				ConditionFieldMap cfm = targetCp.getFieldMap(presetName);
				if (cfm.isDefault()) {
					if (cfm.isCustom())
						customDefaultPresetName = presetName;
					else
						publicDefaultPresetName = presetName;
				}
			}
			for (String presetName : srcCp.getPresets()) {
				ConditionFieldMap cfm = srcCp.getFieldMap(presetName);
				if (cfm.isDefault()) {
					if ((cfm.isCustom() && customDefaultPresetName != null && !presetName.equals(customDefaultPresetName)) ||
						(!cfm.isCustom() && publicDefaultPresetName != null && !presetName.equals(publicDefaultPresetName)))
						cfm.setDefault(false);
				}
				targetCp.putFieldMap(presetName, cfm);
			}
			errMsg = targetCp.saveConditionPresets(viewId, userId, userId.equals(sessionHelper.getLoginId()), true, sessionHelper);
		}
		else {
			//save for src ConditionPresets
			UniLog.log1("import srcCp userId:%s, preset size:%d", userId, srcCp.getPresets().size());
			errMsg = srcCp.saveConditionPresets(viewId, userId, false, true, sessionHelper);
		}
		if (errMsg == null)
			return Pair.of(srcCp.getPresets().size(), null);
		else
			return Pair.of(0, errMsg);
	}
	
	private Map<String, ConditionPresets> getCurrentConditionPresets(String viewId) {
		Map<String, ConditionPresets> map = new HashMap<String, ConditionPresets>();
		map.put(sessionHelper.getLoginId(), conditionPresets);
		try {
			String prefix = String.format("zkbi_querypreset_%s_custom_", viewId);
			List<String> presetKeys = FilingUtil.getKeys(sessionHelper.getAgent(), prefix + "%");
			for (String key : presetKeys) {
				String userId = key.substring(prefix.length());
				if (userId.equals(sessionHelper.getLoginId()))
					continue;
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				FilingUtilObject fuo = FilingUtil.getFile(sessionHelper.getAgent(), null, key, outStream);
				if (fuo != null) {
					String conditionPreset = outStream.toString("UTF-8");
	  				if (conditionPreset != null) {
	  					ConditionPresets cp = new ConditionPresets();
	  					cp.setCustomStoreKey(fuo.key);
	  					cp.setCustomStoreName(fuo.name);
	  					cp.setCustomStoreDesc(fuo.desc);
	  					cp.parseConditionPreset(conditionPreset, true, sessionHelper);
	  					map.put(userId, cp);
	  				}
					UniLog.log1("key:%s, conditionPreset:%s", key, conditionPreset);
				}
				outStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	private void exportPreset() {
		try {
			Map<String, ConditionPresets> map = getCurrentConditionPresets(viewId);

			JSONObject rootObj = new JSONObject();
			JSONObject presetsObj = new JSONObject();
			JSONArray publicPresetArr = new JSONArray();
			JSONObject customPresetObj = new JSONObject();
			rootObj.put("viewId", viewId);
			rootObj.put("userId", sessionHelper.getLoginId());
			rootObj.put("timestamp", System.currentTimeMillis() / 1000);
			for (Map.Entry<String, ConditionPresets> entry : map.entrySet()) {
				String userId = entry.getKey();
				ConditionPresets cp = entry.getValue();
				for (String presetName : cp.getPresets()) {
					ConditionFieldMap cfm = cp.getFieldMap(presetName);
					if (cbPublicPreset.isChecked() && !cfm.isCustom())
						publicPresetArr.put(cfm.toJson(presetName));
					else if (cbCustomPreset.isChecked() && cfm.isCustom()) {
						JSONArray arr;
						if (!customPresetObj.has(userId))
							customPresetObj.put(userId, arr = new JSONArray());
						else
							arr = customPresetObj.getJSONArray(userId);
						arr.put(cfm.toJson(presetName));
					}
				}
			}
			presetsObj.put("public", publicPresetArr);
			presetsObj.put("custom", customPresetObj);
			rootObj.put("presets", presetsObj);
			Filedownload.save(rootObj.toString(4), "application/json", viewId + "_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".json");
		}
		catch (Exception e) {
			ZkUtil.showErrMsg("Export error: %s", e.toString());
			e.printStackTrace();
		}
	}
}
