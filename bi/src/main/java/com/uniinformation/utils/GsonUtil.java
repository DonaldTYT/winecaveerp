package com.uniinformation.utils;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class GsonUtil{
	public final static Gson gson = getGsonBuilder().create();
	
	/***
	 * create a default gson builder
	 * @return
	 */
	public static GsonBuilder getGsonBuilder() {
		return new GsonBuilder()
			.setPrettyPrinting() .setDateFormat("yyyy/MM/dd HH:mm:ss")
			.excludeFieldsWithModifiers(Modifier.FINAL,Modifier.TRANSIENT,Modifier.STATIC); //common exclude list
	}
	/***
	 * create JsonObject from string
	 * @param p_jsonString
	 * @return if error, return null
	 */
	public static JsonObject createJsonObject(String p_jsonString) {
		if (StringUtils.isBlank(p_jsonString)) {
			return null;
		}
		try {
			return new JsonParser().parse(p_jsonString).getAsJsonObject();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static JsonObject convertToJsonObject(JSONObject p_jsonObject) throws Exception {
		if (p_jsonObject == null) {
			return null;
		}
		String jsonStr = p_jsonObject.toString();
		return createJsonObject(jsonStr);
	}
	
	/*
	//obsoleted, replaced by objToStr
	public static String jsonToStr(JsonObject p_jsonObject) throws Exception {
		 return gson.toJson(p_jsonObject); 
	}
	*/
	
	/***
	 * serialize obj to Json String
	 * @param p_jsonObject
	 * @return
	 * @throws Exception
	 */
	public static String objToStr(Object p_object, Class<?> p_class) {
		 return gson.toJson(p_object, p_class);
	}
	/***
	 * serialize obj to Json String
	 * @param p_object
	 * @return
	 */
	public static String objToStr(Object p_object) {
		 return gson.toJson(p_object);
	}
	
	public static JsonObject objToJson(Object p_obj) {
		if (p_obj == null) {
			return null;
		}
		return (JsonObject) gson.toJsonTree(p_obj);
		
	}
	
	public static JSONObject convertToJSONObject(JsonObject p_jsonObject) throws Exception {
		if (p_jsonObject == null) {
			return null;
		}
		return new JSONObject(p_jsonObject.toString());	
	}
	
	/***
	 * deserialize jsonObject to specific class
	 * map jsonObject to object
	 * @param p_jsonObject
	 * @param p_class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertToObject(JsonObject p_jsonObject, Class<T> p_class) {
		return (T) gson.fromJson(p_jsonObject, p_class);
	}
	
	public static <T> T convertToObject(JsonObject p_jsonObject, Class<T> p_class, 	final Object p_existingObj) {
		InstanceCreator<T> creator = new InstanceCreator<T>() {
			@Override
			public T createInstance(Type arg0) {
				return (T) p_existingObj;
			}
		};

		Gson gson2 = getGsonBuilder().registerTypeAdapter(p_class, creator).create();
		return (T) gson2.fromJson(p_jsonObject, p_class);
	}

	public static <T> T convertToObject(Gson g, String p_json, Class<T> p_class) {
		return (T) g.fromJson(p_json, p_class);
	}

	public static <T> T convertToObject(String p_json, Class<T> p_class) {
		return convertToObject(gson, p_json, p_class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T convertToObject(Gson g, String p_json, Type type) {
		return (T) g.fromJson(p_json, type);
	}

	public static <T> T convertToObject(String p_json, Type type) {
		return convertToObject(gson, p_json, type);
	}

	public static <T extends Collection<?>> T convertToCollection(Gson g, String p_json) {
		return g.fromJson(p_json, new TypeToken<T>(){}.getType());
	}

	public static <T extends Collection<?>> T convertToCollection(String p_json) {
		return convertToCollection(gson, p_json);
	}
	
	public static String getString(JsonObject p_json,String p_key) {
		return getString(p_json,p_key, null);
	}
	public static String getString(JsonObject p_json,String p_key, String p_defVal) {
		if (!(p_json instanceof JsonObject)){
			return(p_defVal);
		}
		JsonElement je = p_json.get(p_key);
		return (je == null ? p_defVal : je.getAsString());
	}
	
	class DummyTestGson {
		public String name = "";
		private int age = 0;
		//private boolean is_true = false;
	}
	public static void main(String args[]) throws Exception{
		//create JsonObject
		JsonObject json = GsonUtil.createJsonObject("{ 'name': 'myname', 'age': 123, 'is_true': true }");
		
		//convert JsonObject to Json String
		//UniLog.log1("jsonToStr(json): %s", GsonUtil.jsonToStr(json));
		UniLog.log1("objToStr(json): %s", GsonUtil.objToStr(json));
		UniLog.log1("objToStr(json,JsonObject.class): %s", GsonUtil.objToStr(json, JsonObject.class));
		
		//convert JsonObject to JSONObject
		JSONObject jSON = GsonUtil.convertToJSONObject(json);
		UniLog.log1("%s vs %s",json.get("name").getAsString(),jSON.getString("name"));
		UniLog.log1("%s vs %s",json.get("is_true").getAsBoolean(),jSON.getBoolean("is_true"));
		UniLog.log1("%d vs %d",json.get("age").getAsInt(),jSON.getInt("age"));
		
		//map JsonObject to specific class
		DummyTestGson testGson = GsonUtil.convertToObject(json, DummyTestGson.class);
		UniLog.log1("toString(testGson, DummyTestGson.class): %s", GsonUtil.objToStr(testGson, DummyTestGson.class));
		UniLog.log1("toXML:\n" + XStreamUtil.objToXMLString(testGson, null));
		
	}
}