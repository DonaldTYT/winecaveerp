package com.kikyosoft.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.uniinformation.cell.Cell;

//import com.kikyosoft.cell.Cell;

public class JsonUtil{
	JSONObject jo = null;
	JSONArray ja = null;
	
	Object cellToJsonObject(Cell cc) throws Exception{
		switch(cc.getType()) {
		case Cell.VTYPE_BOOLEAN : 
		case Cell.VTYPE_STRING: 
		case Cell.VTYPE_DOUBLE: 
		case Cell.VTYPE_INT: 
				return(cc.getObject());
		case Cell.VTYPE_DATE :
				{
				String s = cc.getDate().toInstant()
						.atZone(java.time.ZoneId.of("Asia/Hong_Kong"))
						.toLocalDate()
						.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
				return(s);
				}
		case Cell.VTYPE_DATETIME :
				{
				String s = cc.getDate().toInstant()
						.atZone(java.time.ZoneId.of("Asia/Hong_Kong"))
						.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);	
				return(s);
				}
		default:
			throw new Exception("Not Yet Implemented");
		}
		
	}
	public JsonUtil(JSONObject p_jo) {
		jo = (p_jo == null ? new JSONObject() : p_jo);
	}
	public JsonUtil(JSONArray p_ja) {
		ja = (p_ja == null ? new JSONArray() : p_ja);
	}
	public JsonUtil add(Object cc) throws Exception {
		if(ja == null) {
			throw new Exception("JsonUtil Not Initialized for Json Array");
		}
		if(cc instanceof Cell) {
			addCell((Cell) cc);
		} else {
			ja.put(cc);
		}
		return(this);
	}
	public JsonUtil add(String key,Object cc) throws Exception {
		if(jo == null) {
			throw new Exception("JsonUtil Not Initialized for Json Object");
		}
		if(cc instanceof Cell) {
			addCell(key,(Cell) cc);
		} else {
			jo.put(key,cc);
		}
		return(this);
	}
	public JsonUtil addCell(Cell cc) throws Exception {
		if(ja == null) {
			throw new Exception("JsonUtil Not Initialized for Json Array");
		}
		ja.put(cellToJsonObject(cc));
		return(this);
	}
	public JsonUtil addCell(String key,Cell cc) throws Exception {
		if(jo == null) {
			throw new Exception("JsonUtil Not Initialized for Json Object");
		}
		jo.put(key, cellToJsonObject(cc));
		/*
		switch(cc.getType()) {
		case Cell.VTYPE_BOOLEAN : 
		case Cell.VTYPE_STRING: 
		case Cell.VTYPE_DOUBLE: 
		case Cell.VTYPE_INT: 
				jo.put(key, cc.getObject());
				break;
		case Cell.VTYPE_DATE :
				{
				String s = cc.getDate().toInstant()
						.atZone(java.time.ZoneId.of("Asia/Hong_Kong"))
						.toLocalDate()
						.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
				jo.put(key, s);
				}
				break;
		case Cell.VTYPE_DATETIME :
				{
				String s = cc.getDate().toInstant()
						.atZone(java.time.ZoneId.of("Asia/Hong_Kong"))
						.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);	
						jo.put(key, s);
				jo.put(key, s);
				}
				break;
		default:
			throw new Exception("Not Yet Implemented");
		}
		*/
		return(this);
	}
	public JSONObject toJSONObject() throws Exception {
		if(jo == null) {
			throw new Exception("JsonUtil Not Initialized for Json Object");
		}
		return(jo);
	}
	
	public JsonUtil addI18NObject(String p_locale,Object p_label) throws Exception {
		if(jo == null) {
			throw new Exception("JsonUtil Not Initialized for Json Object");
		}
		if(p_label instanceof String) {
			if(StringUtils.isBlank(((String) p_label))) {
				return(this);
			}
		}
		if(p_label instanceof JSONObject ) {
			if(((JSONObject) p_label).isEmpty()) {
				return(this);
			}
		}
		JSONObject jt = jo.optJSONObject("translation");
		if(jt == null) {
			jt = new JSONObject();
			jo.put("translation", jt);
		}
		jt.put(p_locale, p_label);
		return(this);
	}
	
	public Object toI18NObject() throws Exception {
		if(jo == null) {
			throw new Exception("JsonUtil Not Initialized for Json Object");
		}
		Object jn = jo.opt("name");
		if(jn == null) {
			throw new Exception("Primary Object not set");
		}
		JSONObject jt = jo.optJSONObject("translation");
		if(jt == null || jt.length() <= 0) return(jn); else return(jo);
	}
	
	public static String i18nGetKey(Object p_object) {
		if(p_object instanceof JSONObject) {
			Object o = ((JSONObject) p_object).opt("name");
			if(o == null) return(null);
			return(i18nGetKey(o));
		} else return(p_object.toString());
	}
}
