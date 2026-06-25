package com.uniinformation.jx.zk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;

import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ZkComposerBase;

public class JxZkWorksheet extends ZkComposerBase {
	private static final JSONArray luckySheetData = new JSONArray() {{
		try {
			put(new JSONObject("{ name: \"Sheet1\", index: \"Sheet_001\" }"));
		} catch(Exception e) {
			UniLog.log(e);
		}
	}};
	private static final Map<Object, JSONObject> removedLuckySheetData = new HashMap<Object, JSONObject>();
	
	//@Wire
	//private Button btSetConfig, btSetRangeMerge, btSetRangeValue, btSetRangeFormat;
	@Wire
	private Div divLuckysheet;
	
	@Override
	protected boolean validateURL(String p_url) {
		return true;  //disable vailidation during dev
	}

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("desktop id:%s", getDesktopId());
		ZkUtil.delayJs(p_comp, null, 500, "createSampleSheet('%s', %s)", divLuckysheet.getUuid(), luckySheetData.toString());

		divLuckysheet.addEventListener("onSendMessage", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				String msg = (String)event.getData();
				UniLog.log1("onSendMessage event:%s", event);
				if (StringUtils.isBlank(msg) || StringUtils.equals(msg, "rub"))
					return;
				try {
		            String s = URLDecoder.decode(unCompressMessage(msg), "UTF-8");
		            JSONObject jo = new JSONObject(s);
		            JSONObject jo1 = new JSONObject();
		            jo1.put("data", jo);
		            jo1.put("type", (StringUtils.equals(jo.optString("t"), "mv")) ? 3 : 2);
		            jo1.put("id", getDesktopId());
		            jo1.put("username", sessionHelper.getLoginId());
		            //UniLog.log1("jo1:%s", jo1.toString(3));
					EventQueue<Event> que = EventQueues.lookup("LuckysheetNotify", EventQueues.APPLICATION, true);
					que.publish(new Event("onLuckysheetNotify", null, jo1));
					addLuckySheetData(jo);
		        } catch (Exception e) {
		        	UniLog.log(e);
		        }
			}
		});
		EventQueue<Event> que = EventQueues.lookup("LuckysheetNotify", EventQueues.APPLICATION, true);
		que.subscribe(new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (StringUtils.equals(event.getName(), "onLuckysheetNotify")) {
		            JSONObject jo = (JSONObject)event.getData();
		            if (jo != null && !StringUtils.equals(jo.optString("id"), getDesktopId())) {
		            	UniLog.log1("onLuckysheetNotify jo:%s, id:%s", jo.toString(3), jo.optString("id"));
		            	ZkUtil.js("luckysheet.server.websocket.onmessage(%s);", jo.toString());
		            }
				}
			}
		});

		/*if (btSetConfig != null){
			btSetConfig.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					UniLog.log1("got event");
					JSONObject json = new JSONObject("{" + 
							"merge: {" + 
							"	2_3: {" + 
							"		r: 2," + 
							"		c: 3," + 
							"		rs: 3," + 
							"		cs: 5" + 
							"	}," + 
							"}," + 
							"borderInfo: [" + 
							"	{" + 
							"		rangeType: \"cell\"," + 
							"		value: {" + 
							"			row_index: 10," + 
							"			col_index: 10," + 
							"			l: {" + 
							"				style: 1," + 
							"				color: \"#ff0000\"" + 
							"			}," + 
							"			r: {" + 
							"				style: 1," + 
							"				color: \"#ff0000\"" + 
							"			}," + 
							"			t: {" + 
							"				style: 1," + 
							"				color: \"#ff0000\"" + 
							"			}," + 
							"			b: {" + 
							"				style: 1," + 
							"				color: \"#ff0000\"" + 
							"			}" + 
							"		}" + 
							"	}, {" + 
							"		rangeType: \"range\"," + 
							"		borderType: \"border-outside\"," + 
							"		style: 1," + 
							"		color: \"#ff00ff\"," + 
							"		range: [{" + 
							"			row: [7, 9]," + 
							"			column: [2, 4]" + 
							"		}]" + 
							"	}" + 
							"]," + 
							"}");
					ZkUtil.js("luckysheet.setConfig(%s, {order: 0});", json.toString());
				}
			});
		}
		if (btSetRangeMerge != null) {
			btSetRangeMerge.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					UniLog.log1("got event");
					ZkUtil.js("luckysheet.setRangeMerge(\"all\", {range:[{row:[2,4],column:[3,7]}, {row:[8,9],column:[9,10]}], order:0});");
				}
			});
		}
		if (btSetRangeValue != null) {
			btSetRangeValue.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					UniLog.log1("got event");
					JSONArray json = new JSONArray("[" + 
							"		[" + 
							"			{" + 
							"				ct: { fa: \"General\", t: \"g\" }," + 
							"				v: \"value1\"" + 
							"			}," + 
							"			{" + 
							"				ct: { fa: \"@\", t: \"s\" }," + 
							"				bg: \"#ff00ff\"," + 
							"				v: \"value2\"" + 
							"			}," + 
							"			{" + 
							"				ct: { fa: \"@\", t: \"s\" }," + 
							"				bg: \"#ffff00\"," + 
							"				ht: 0," + 
							"				v: \"value3\"" + 
							"			}," + 
							"		],[" + 
							"			{" + 
							"				ct: { fa: \"0.00\", t: \"n\" }," + 
							"				bl: 1," + 
							"				v: \"123.456\"" + 
							"			}," + 
							"			{" + 
							"				ct: { fa: \"0.000\", t: \"n\" }," + 
							"				fs: 20," + 
							"				v: \"123.456\"" + 
							"			}," + 
							"			{" + 
							"				ct: { fa: \"0.000\", t: \"n\" }," + 
							"				fc: \"#ff0000\"," + 
							"				v: \"123.456\"" + 
							"			}," + 
							"		]"
							+ "]");
					ZkUtil.js("luckysheet.setRangeValue(%s, {range:\"A1:C2\"});", json.toString());
				}
			});
		}
		if (btSetRangeFormat != null) {
			btSetRangeFormat.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					UniLog.log1("got event");
					ZkUtil.js("luckysheet.setRangeFormat(\"bg\", \"#ffff00\", {range:\"A25:B27\"});");
					ZkUtil.js("luckysheet.setRangeFormat(\"ht\", 2, {range:\"A25:B27\"});");
					ZkUtil.js("luckysheet.setRangeFormat(\"v\", \"abc\", {range:\"A25:B27\"});");
				}
			});
		}*/
	}
	
    public static String unCompressMessage(String str) throws Exception {
        if (StringUtils.isBlank(str))
            return str;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer)) >= 0)
            out.write(buffer, 0, n);
        return out.toString();
    }
    
    private static void addLuckySheetData(JSONObject jo) throws Exception {
    	synchronized(luckySheetData) {
    		final String t = jo.getString("t");
    		if (StringUtils.equals(t, "v")) {
    			final Object sheetIndex = jo.get("i");
    			final Object v = jo.get("v");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			final int r = jo.getInt("r");
    			final int c = jo.getInt("c");
    			JSONArray cellData = getLuckySheetCellData(sheet);
    			JSONObject cell = findLuckySheetCell(cellData, r, c);
    			if (cell == null) {
    				cell = new JSONObject() {{
    					put("r", r);
    					put("c", c);
    					put("v", v);
    				}};
    				cellData.put(cell);
    			} else
    				cell.put("v", v);
    		} else if (StringUtils.equals(t, "cg")) {
    			final Object sheetIndex = jo.get("i");
    			final Object v = jo.get("v");
    			final String k = jo.getString("k");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			JSONObject config = getLuckySheetConfig(sheet);
    			config.put(k, v);
    		} else if (StringUtils.equals(t, "all")) {
    			final Object sheetIndex = jo.get("i");
    			final Object v = jo.get("v");
    			final String k = jo.getString("k");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			sheet.put(k, v);
    		} else if (StringUtils.equals(t, "fc")) { //calc chain
    			final Object sheetIndex = jo.get("i");
    			final Object v = jo.get("v");
    			final String op = jo.getString("op");
    			final int pos = jo.getInt("pos");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			JSONArray calcChain = getLuckySheetCalcChain(sheet);
    			if (StringUtils.equals(op, "add"))
    				calcChain.put(v);
    			else if (StringUtils.equals(op, "update"))
    				calcChain.put(pos, v);
    			else if (StringUtils.equals(op, "del"))
    				calcChain.remove(pos);
    		} else if (StringUtils.equals(t, "drc")) { //delete row/col
    			final Object sheetIndex = jo.get("i");
    			final String rc = jo.getString("rc");
    			final JSONObject v = jo.getJSONObject("v");
    			final int vindex = v.getInt("index");
    			final int vlen = v.getInt("len");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			JSONArray cellData = getLuckySheetCellData(sheet);
    			for (int i = 0; i < cellData.length(); i++) {
    				JSONObject cell = cellData.getJSONObject(i);
    				int r = cell.getInt("r");
    				int c = cell.getInt("c");
    				if (StringUtils.equals(rc, "r")) {
    					if (r >= vindex + vlen) {
    						r -= vlen;
    						cell.put("r", r);
    					} else if (r >= vindex && r < vindex + vlen)
    						cellData.remove(i);
    				} else if (StringUtils.equals(rc, "c")) {
    					if (c >= vindex + vlen) {
    						c -= vlen;
    						cell.put("c", c);
    					} else if (c >= vindex && c < vindex + vlen)
    						cellData.remove(i);
    				}
    			}
    		} else if (StringUtils.equals(t, "arc")) { //add row/col
    			final Object sheetIndex = jo.get("i");
    			final String rc = jo.getString("rc");
    			final JSONObject v = jo.getJSONObject("v");
    			final int vindex = v.getInt("index");
    			final int vlen = v.getInt("len");
    			final String vdirection = v.getString("direction");
    			final JSONArray vdata = v.getJSONArray("data");
 				int vi = vindex + (StringUtils.equals(vdirection, "rightbottom") ? 1 : 0);
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			JSONArray cellData = getLuckySheetCellData(sheet);
    			for (int i = 0; i < cellData.length(); i++) {
    				JSONObject cell = cellData.getJSONObject(i);
    				int r = cell.getInt("r");
    				int c = cell.getInt("c");
    				if (StringUtils.equals(rc, "r")) {
    					if (r >= vi) {
    						r += vlen;
    						cell.put("r", r);
    					}
    				} else if (StringUtils.equals(rc, "c")) {
    					if (c >= vi) {
    						c += vlen;
    						cell.put("c", c);
    					}
    				}
    			}
    			for (int i = 0; i < vdata.length(); i++) {
    				JSONArray rd = vdata.getJSONArray(i);
    				for (int j = 0; j < rd.length(); j++) {
    					Object o = rd.get(j);
    					if (o == null)
    						continue;
    					JSONObject d = new JSONObject();
    					if (StringUtils.equals(rc, "r")) {
    						d.put("r", vi + i);
    						d.put("c", j);
    					} else if (StringUtils.equals(rc, "c")) {
    						d.put("r", i);
    						d.put("c", vi + j);
    					}
    					d.put("v", o);
    				}
    			}
    		} else if (StringUtils.equals(t, "fsc")) { //clear filter 
    			final Object sheetIndex = jo.get("i");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			sheet.remove("filter");
    			sheet.remove("filter_select");
    		} else if (StringUtils.equals(t, "fsr")) { //restore filter 
    			final Object sheetIndex = jo.get("i");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
    			final JSONObject v = jo.getJSONObject("v");
    			final JSONArray vfilter = jo.getJSONArray("filter");
    			final JSONObject vfilterselect = jo.getJSONObject("filter_select");
    			sheet.put("filter", vfilter);
    			sheet.put("filter_select", vfilterselect);
    		} else if (StringUtils.equals(t, "sha")) { //create sheet
    			final JSONObject v = jo.getJSONObject("v");
   				luckySheetData.put(v);
    		} else if (StringUtils.equals(t, "shc")) { //copy sheet
    			final Object sheetIndex = jo.get("i");
    			final JSONObject v = jo.getJSONObject("v");
    			final Object vcopyindex = v.get("copyindex");
    			final String vname = v.getString("name");
    			JSONObject sheet = findLuckySheetSheet(vcopyindex);
    			JSONObject newSheet = new JSONObject(sheet.toString());
    			newSheet.put("index", sheetIndex);
    			newSheet.put("name", vname);
    			luckySheetData.put(newSheet);
    		} else if (StringUtils.equals(t, "shd")) { //remove sheet
    			final JSONObject v = jo.getJSONObject("v");
    			final Object deleIndex = v.get("deleIndex");
    			for (int i = 0; i < luckySheetData.length(); i++) {
    				JSONObject sheet = luckySheetData.getJSONObject(i);
    				if (ObjectUtils.equals(sheet.get("index"), deleIndex)) {
    					removedLuckySheetData.put(deleIndex, sheet);
    					luckySheetData.remove(i);
    					break;
    				}
    			}
    		} else if (StringUtils.equals(t, "shre")) { //restore after remove sheet
    			final JSONObject v = jo.getJSONObject("v");
    			final Object reIndex = v.get("reIndex");
    			JSONObject reo = removedLuckySheetData.remove(reIndex);
    			if (reo != null)
    				luckySheetData.put(reo);
    			else
    				throw new Exception(String.format("shre fail, Sheet index '%s' not found", reIndex));
    		} else if (StringUtils.equals(t, "shr")) { //set sheet order
    			final JSONObject v = jo.getJSONObject("v");
    			Iterator<String> it = v.keys();
    			while (it.hasNext()) {
    				String sheetIndex = it.next();
    				JSONObject sheet = findLuckySheetSheet(sheetIndex);
    				if (sheet != null)
   						sheet.put("order", v.getInt(sheetIndex));
    			}
    		} else if (StringUtils.equals(t, "shs")) { //set sheet status
    			final Object v = jo.get("v");
   				for (int i = 0; i < luckySheetData.length(); i++) {
   					JSONObject sheet = luckySheetData.getJSONObject(i);
  					sheet.put("status", ObjectUtils.equals(sheet.get("index"), v) ? 1 : 0);
   				}
    		} else if (StringUtils.equals(t, "sh")) { //sheet show/hide
    			final Object sheetIndex = jo.get("i");
    			final int v = jo.getInt("v");
    			final String op = jo.getString("op");
    			final Object cur = jo.get("cur");
   				for (int i = 0; i < luckySheetData.length(); i++) {
  					JSONObject sheet = luckySheetData.getJSONObject(i);
 					sheet.put("status", ObjectUtils.equals(sheet.get("index"), cur) ? 1 : 0);
   					if (ObjectUtils.equals(sheet.get("index"), sheetIndex)) {
   						if (v == 1) {
   							sheet.put("hide", 1);
   							sheet.put("status", 0);
   						} else
   							sheet.put("hide", 0);
   					}
   				}
    		} else if (StringUtils.equals(t, "na")) { //workbook title
    		} else if (StringUtils.equals(t, "c")) { //chart
    			final Object sheetIndex = jo.get("i");
    			final String op = jo.getString("op");
    			final JSONObject v = jo.getJSONObject("v");
    			JSONObject sheet = findLuckySheetSheet(sheetIndex);
   				JSONArray charts = getLuckySheetChart(sheet);
    			if (StringUtils.equals(op, "add"))
    				charts.put(v);
    			else if (StringUtils.equals(op, "xy")) {
    				final String vchart_id = v.getString("chart_id");
    				final int vleft = v.getInt("left");
    				final int vtop = v.getInt("top");
    				Pair<JSONObject, Integer> p = findLuckySheetChart(charts, vchart_id);
    				JSONObject chart = p.getLeft();
    				chart.put("left", vleft);
    				chart.put("top", vtop);
    			} else if (StringUtils.equals(op, "wh")) {
    				final String vchart_id = v.getString("chart_id");
    				final int vleft = v.getInt("left");
    				final int vtop = v.getInt("top");
    				final int vwidth = v.getInt("width");
    				final int vheight = v.getInt("height");
    				Pair<JSONObject, Integer> p = findLuckySheetChart(charts, vchart_id);
    				JSONObject chart = p.getLeft();
    				chart.put("left", vleft);
    				chart.put("top", vtop);
    				chart.put("width", vwidth);
    				chart.put("height", vheight);
    			} else if (StringUtils.equals(op, "update")) {
    				final String vchart_id = v.getString("chart_id");
    				Pair<JSONObject, Integer> p = findLuckySheetChart(charts, vchart_id);
    				charts.put(p.getRight(), v);
    			}
    		}
    	}
    }
    
    private static JSONArray getLuckySheetCellData(JSONObject sheet) throws Exception {
    	synchronized(luckySheetData) {
    		JSONArray cellData = sheet.optJSONArray("celldata");
    		if (cellData == null) {
    			cellData = new JSONArray();
    			sheet.put("celldata", cellData);
    		}
    		return cellData;
    	}
    }

    private static JSONObject getLuckySheetConfig(JSONObject sheet) throws Exception {
    	synchronized(luckySheetData) {
    		JSONObject config = sheet.optJSONObject("config");
    		if (config == null) {
    			config = new JSONObject();
    			sheet.put("config", config);
    		}
    		return config;
    	}
    }

    private static JSONArray getLuckySheetCalcChain(JSONObject sheet) throws Exception {
    	synchronized(luckySheetData) {
    		JSONArray calcChain = sheet.optJSONArray("calcChain");
    		if (calcChain == null) {
    			calcChain = new JSONArray();
    			sheet.put("calcChain", calcChain);
    		}
    		return calcChain;
    	}
    }

    private static JSONArray getLuckySheetChart(JSONObject sheet) throws Exception {
    	synchronized(luckySheetData) {
    		JSONArray chart = sheet.optJSONArray("chart");
    		if (chart == null) {
    			chart = new JSONArray();
    			sheet.put("chart", chart);
    		}
    		return chart;
    	}
    }

    private static JSONObject findLuckySheetSheet(Object sheetIndex) throws Exception {
    	synchronized(luckySheetData) {
    		for (int i = 0; i < luckySheetData.length(); i++) {
    			JSONObject jo = luckySheetData.getJSONObject(i);
    			if (ObjectUtils.equals(jo.get("index"), sheetIndex))
    				return jo;
    		}
    		throw new Exception(String.format("Sheet index '%s' not found", sheetIndex));
    	}
    }
    
    private static JSONObject findLuckySheetCell(JSONArray cellData, int row, int col) throws Exception {
    	synchronized(luckySheetData) {
    		for (int i = 0; i < cellData.length(); i++) {
    			JSONObject cell = cellData.getJSONObject(i);
    			int r = cell.getInt("r");
    			int c = cell.getInt("c");
    			if (r == row && c == col)
    				return cell;
    		}
    		return null;
    	}
    }

    private static Pair<JSONObject, Integer> findLuckySheetChart(JSONArray charts, String chartId) throws Exception {
    	synchronized(luckySheetData) {
    		for (int i = 0; i < charts.length(); i++) {
    			JSONObject jo = charts.getJSONObject(i);
    			if (ObjectUtils.equals("chart_id", chartId))
    				return Pair.of(jo, i);
    		}
    		throw new Exception(String.format("Chart Id '%s' not found", chartId));
    	}
    }
}
