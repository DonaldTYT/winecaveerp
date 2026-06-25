package com.uniinformation.jx.zk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.google.gson.reflect.TypeToken;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.utils.ConditionPresets;
import com.uniinformation.utils.ConditionPresets.ConditionField;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.LabelHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkbi.ZkBiTranslateHelper;

public class JxZkCustomMenu extends ZkComposerBase {
	private static final long serialVersionUID = 7473634379060944327L;
//	public static final String FILING_STORE_KEY = "ZkCustomMenu_custom_%s";
	static boolean notifyDisabled = false;
	private static class EmbdedComponentInfo {
		JSONObject json;
		Div comp;
	}
	/*
	private int notifyCountRefreshInitDelay = 1;
	private int notifyCountRefreshDelay = 60000;
	private int notifyCountRoundMaxItems = 5;
	private int notifyCountRoundCurrentItem = 0;
	private int notifyCountRoundDelay = 1000;
	*/
	private static int notifyCountRefreshInitDelay = 1;
	//private static int notifyCountRefreshDelay = 600000;  //longer delay 10min
	private static int notifyCountRefreshDelay = 1800000;  //longer delay 30min
	private static int notifyCountRoundMaxItems = 5;
	private int notifyCountRoundCurrentItem = 0;
	private static int notifyCountRoundDelay = 1000;
	
	@Wire
	A anEdit;
	@Wire
	A anSortByName;
	@Wire
	A anSortByCategory;
	@Wire
	A anAddAll;
	@Wire
	A anRemoveAll;
	@Wire
	A anSave;
	@Wire
	A anCancel;
	@Wire
	Div divTemp;
	@Wire
	Label lbDashboard;
	
	@Override
	protected boolean validateURL(String p_url) {
		return true;
	}
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		if (!accessOkFlag) {
			return;
		}
    	
		/*
    	anEdit.setLabel(sessionHelper.getLabel("Edit"));
    	*/
    	anEdit.setTooltiptext(sessionHelper.getTtLabel("Edit menu item"));
    	anSortByName.setLabel(sessionHelper.getLabel("Sort by Name"));
    	anSortByName.setTooltiptext(sessionHelper.getTtLabel("Sort by name"));
    	//anSortByCategory.setLabel(sessionHelper.getLabel("Sort by Category"));
    	//anSortByCategory.setTooltiptext(sessionHelper.getTtLabel("Sort by category"));
    	anSortByCategory.setLabel(sessionHelper.getLabel("Sort by Menu Order"));
    	anSortByCategory.setTooltiptext(sessionHelper.getTtLabel("Sort by Menu Order"));
    	anAddAll.setLabel(sessionHelper.getLabel("Add All"));
    	anAddAll.setTooltiptext(sessionHelper.getTtLabel("Add all menu item"));
    	anRemoveAll.setLabel(sessionHelper.getLabel("Remove All"));
    	anRemoveAll.setTooltiptext(sessionHelper.getTtLabel("Remove all menu item"));
    	//anSave.setLabel(sessionHelper.getLabel("Save"));
    	//anSave.setTooltiptext(sessionHelper.getTtLabel("Save menu item"));
    	//anCancel.setLabel(sessionHelper.getLabel("Cancel"));
    	//anCancel.setTooltiptext(sessionHelper.getTtLabel("Cancel changes"));
    	anSave.setLabel(sessionHelper.getLabel("Close"));
    	anSave.setTooltiptext(sessionHelper.getTtLabel("Close"));
    	
    	if (lbDashboard != null){
    		String v = sessionHelper.getLabel("Home");
    		lbDashboard.setValue(v);
    		Clients.evalJavaScript(String.format("document.title='%s'", StringEscapeUtils.escapeJavaScript(v)));
    	}
    	
    	Map<String, Object> map = sessionHelper.generateCustomMenuList(ZkUtil.getServletRequest());
   		JSONArray jsonArray = new JSONArray((String)map.get("items"));
   		ArrayList<EmbdedComponentInfo> embdedComponentInfos = new ArrayList<EmbdedComponentInfo>();
   		int embdedNum = 0;
   		for (int i = 0; i < jsonArray.length(); i++) {
   			JSONObject json = jsonArray.getJSONObject(i);
   			String zul = json.optString("zul", "");
   			boolean removed = json.optBoolean("removed");
   			JSONObject zulParams = json.optJSONObject("zulParams");
   			if (!zul.isEmpty() && !removed) {
   				try {
	  				Map<String, Object> zulParamMap = null;
	   				if (zulParams != null) {
	   					zulParamMap = GsonUtil.gson.fromJson(zulParams.toString(), new TypeToken<Map<String, Object>>(){}.getType());
	   					zulParamMap.put("removedFromMenu", removed ? "Y" : "N");
	   				}
	   				UniLog.log1("zulParamMap:%s", zulParamMap);
		   			json.put("embdedNum", String.valueOf(embdedNum));
		   			Div div = new Div();
		   			div.setClientAttribute("embdedNum", String.valueOf(embdedNum));
		   			div.setParent(divTemp);
		   			div.setStyle("display:inline-block");
		   			Executions.createComponents(zul, div, zulParamMap);
		   			EmbdedComponentInfo info = new EmbdedComponentInfo();
		   			info.json = json;
		   			info.comp = div;
		   			embdedComponentInfos.add(info);
		   			embdedNum++;
	   			}
	   			catch(Exception ex) {
	   				UniLog.log1("error:" + ex.getMessage());
	   				//ex.printStackTrace();
	   			}
   			}
   		}
   		for (EmbdedComponentInfo info : embdedComponentInfos) {
   			for (Component c : info.comp.getChildren()) {
   				if (c instanceof Window) {
   					Window win = (Window) c;
   					win.setAttribute("GridItemRg", info.json.getInt("rg"));
   					break;
   				}
   			}
   		}
   		Clients.evalJavaScript("onCreate(" + jsonArray.toString(4) + ", " + map.get("sortMode") + ")");
   		displayNotifyCount(comp, sessionHelper, jsonArray);
   		
    	comp.addEventListener("onSave", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("onSave data: %s", event.getData());
				InputStream inStream = null;
				try {
					String key = String.format(SessionHelper.CUSTOMMENU_FILING_STORE_KEY, sessionHelper.getLoginId());
					inStream = new ByteArrayInputStream(event.getData().toString().getBytes());
					FilingUtil.storeFile(sessionHelper.getAgent(), null, key, key, key, inStream);
					//Messagebox.show("Save successfully");
				} catch (Exception ex) {
					UniLog.log("save fail " + ex.toString());
					Messagebox.show("Save fail " + ex.toString());
				} finally {
					if (inStream != null)
						inStream.close();
				}
				Executions.getCurrent().sendRedirect(""); //dirty way to refresh notify count
			}
    	});
    	comp.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>(){
			@Override
			public void onEvent(AfterSizeEvent event) throws Exception {
				UniLog.log("onAfterSize data: " + event.getName() + " " + event.getData() + " width:" + event.getWidth() + ",height:" + event.getHeight());
			}
		});
    	comp.addEventListener("onClientSize", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("onClientSize data: " + event.getName() + " " + event.getData());
				Clients.resize(comp);
			}
		});
    	
   		//ZkUtil.notifySidrStatus(comp, sessionHelper);  //andrew181211: seems same function as autoAdjustWinWidth
	}
	
	private void displayNotifyCount(Component comp, final SessionHelper p_sessionHelper, final JSONArray jsonArray){
		final List<JSONObject> jsonList = new ArrayList<JSONObject>();
		final Pattern pattern = Pattern.compile("viewid=([\\w.]+)($|&)"); //andrew 181003: fix viewid with dot. e.g. afs.QuotationMC
		final Pattern patternForPreset = Pattern.compile("presetid=([\\w.]+)($|&)");
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				
				//validation
				int rg = json.getInt("rg");
				if (rg <= 0){
					UniLog.logm(this, "invalid rg:%d", rg);
					continue;
				}
				
				if (json.getBoolean("removed")) { //do not display notify count of removed item
					continue;
				}
				String url = json.getString("link");
				if (StringUtils.isBlank(url)){
					continue;
				}
						
				//obtain notify count by viewid and render it
				Matcher matcher = pattern.matcher(url);
				if (matcher.find() && matcher.groupCount() >= 1)
					jsonList.add(json);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
   		final Timer notifyCountTimer = new Timer();
   		notifyCountTimer.setPage(comp.getPage());
   		
   		//1st round, set a short timer and non repeatable
   		notifyCountTimer.setRepeats(false); //avoid fire twice short timer
   		notifyCountTimer.setRunning(true);
   		notifyCountTimer.setDelay(notifyCountRefreshInitDelay);
   		notifyCountRoundCurrentItem = 0;
   		notifyCountTimer.addEventListener("onTimer", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				//TODO: 
				//after change the getNotifyCount() using preset logic, this block of code will become very slow during first round initialization 
				//i.e. if has 100 view, will need to construct 100 ConditionPresets during init.
				//so need to improve the update logic to avoid the busy spinner display too long
				//e.g. each round only update limited number of items (e.g. 5 item each round)
				if(notifyDisabled) return;
				int i = notifyCountRoundCurrentItem * notifyCountRoundMaxItems;
				if (i >= jsonList.size()) {
					notifyCountRoundCurrentItem = 0;
					i = 0;
				}
				int startItem = i;
				UniLog.log1("event start: " + i + ",jsonArray:" + jsonArray.length() + ",jsonList:" + jsonList.size());
				for (; i < Math.min(jsonList.size(), startItem + notifyCountRoundMaxItems); i++) {
					JSONObject json = jsonList.get(i);
					int rg = json.getInt("rg");
					String url = json.getString("link");

					//obtain notify count by viewid and render it
					Matcher matcher = pattern.matcher(url);
					matcher.find();
					String viewId = matcher.group(1);
					String presetId = null;
					matcher = patternForPreset.matcher(url);
					if (matcher.find() && matcher.groupCount() >= 1)
						presetId = matcher.group(1);
					if (StringUtils.isBlank(presetId) || !presetId.matches("[a-zA-Z0-9-.]+"))
						presetId = viewId;

					int nCnt = getNotifyCount(p_sessionHelper, presetId); 
					//UniLog.log1("viewId:%s, presetId:%s, rg:%d, count:%d", viewId, presetId, rg, nCnt);
					if (nCnt >= 0){
						UniLog.logm(this, "sendNotificationCount(%d,%d) viewid:%s",rg, nCnt, viewId);
						Clients.evalJavaScript(String.format("sendNotificationCount(%d,%d)",rg, nCnt));
					}
				}
				notifyCountRoundCurrentItem++;
				
				//next round, send a longer timer and repeatable
				if (!notifyCountTimer.isRepeats()){
					//notifyCountTimer.setDelay(notifyCountRoundCurrentItem * notifyCountRoundMaxItems >= jsonList.size() ? notifyCountRefreshDelay : notifyCountRoundDelay);
					int delay = notifyCountRoundCurrentItem * notifyCountRoundMaxItems >= jsonList.size() ? notifyCountRefreshDelay : notifyCountRoundDelay;
					//UniLog.log1("delay:%d", delay);
					notifyCountTimer.setDelay(delay);
					//notifyCountTimer.setRepeats(true);
					notifyCountTimer.setRunning(true);
					
				}
			}
   		});
	}
	private int getNotifyCount(SessionHelper p_sessionHelper, String p_view){
		try{
			if (StringUtils.isBlank(p_view)) {
				return -1;
			}
			
			ConditionPresets cp = p_sessionHelper.getConditionPresets(p_view);
			String preset = cp.getDisplayRecCntPreset();
			if (preset != null) {
				UniLog.log("found display record count preset: " + p_view + "," + preset);
				ConditionFieldMap cfm = cp.getFieldMap(preset);
				if (cfm != null) {
					long startTime = System.currentTimeMillis();
					BiSchema schema = (BiSchema) p_sessionHelper.getSessionData("biSchema");
					if(schema == null) {
						schema = BiSchema.loadSchema(p_sessionHelper);
					}
					BiView view = schema.getViewByName(p_view);
					if (view == null){
						UniLog.log("invalid view: " + p_view);
						return -1;
					}
					//return(view.getNotifyCount(p_sessionHelper.getLoginId()));
					BiResult biResult = view.newBiResult(p_sessionHelper.getLoginId(), null,null,p_sessionHelper);
					Vector<BiColumn> bcs = view.getColumns();
					List<ConditionField> cfs = new ArrayList<ConditionField>();
					for (BiColumn bc : bcs) {
						ConditionField cf1 = cfm.get(bc.getEngName(), bc.getField(), bc.getLabel());
						if (cf1 != null) {
							ConditionField cf = cf1.clone();
							cf.setBc(bc);
							cf.makeConditionString();
							cfs.add(cf);
						}
					}
					boolean flag = false;
					for (ConditionField cf : cfs) {
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
								biResult.addCondition(new VectorUtil().addElement(cf.bc.getField().getTable()).toVector(), str);
							else
								biResult.addCondition(new Vector<Object>(), str);
							flag = true;
						}
					}
					if (StringUtils.isNotBlank(cfm.getCustomCondition())) {
						biResult.addCustomCondition(cfm.getCustomCondition());
						flag = true;
					}
					if (flag) {
						int count = biResult.getQueryRecCount();
						UniLog.log("display record count use time: " + (System.currentTimeMillis() - startTime));
						return count;
					}
				}
			}
			return -1;
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
   		return(0);
	}
}
