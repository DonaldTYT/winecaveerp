package com.uniinformation.bicore;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.CoreLog;
import com.uniinformation.webcore.SessionHelper;

public class BiCoreRpcApi {
	public JSONArray getViewList( SessionHelper sh) {
		try {
			List<BiView> vList = sh.getBiSchema().getAllView();
			JSONArray ja = new JSONArray();
			for(BiView bv : vList) {
				ja.put(bv.getName());
			}
			return(ja);
		} catch (Exception ex) {
			CoreLog.log("FAIL"+ex.toString());
			return(null);
		}
	}
	public JSONObject getViewListColumns( SessionHelper sh, String p_viewName) {
		BiView bv = sh.getBiSchema().getViewByName(p_viewName);
		BiResult br = bv.newBiResult(sh.getLoginId(), null, null, sh);
		/*
		JSONArray ja = new JSONArray();
		for(BiColumn bc : br.getListColumns()) {
			JSONObject jo = new JSONObject();
			jo.put("id", bc.getLabel());
			jo.put("header", bc.getEngName());
			jo.put("type", bc.getColumnType());
			jo.put("format", bc.getFormat());
			ja.put(jo);
		}
		*/
		JSONArray ja = br.getListColumnsAsJson();
		
		JSONObject jo = new JSONObject();
		jo.put("name",p_viewName);
		jo.put("fields",ja);
		return(jo);
	}
}
