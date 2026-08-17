package com.uniinformation.rest;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.uniinformation.utils.UniLog;

@Path("/test")
public class TestRS
{
	public class TestJson{
		@Expose
		public int id;
		@Expose
		public String name;
		@Expose
		public Date date1 = new Date();
		public Date date2 = new Date();
		
		public TestJson(int id, String name){
			this.id = id;
			this.name = name;
		}
	}
	@GET
	public String testing(){
		JsonObject testJson = new JsonParser().parse("{\"a\": \"A\"}").getAsJsonObject();
		UniLog.log1(""+testJson.get("a").getAsString());
		
		Gson gson = new GsonBuilder()
					.setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
					.excludeFieldsWithoutExposeAnnotation()
					.create();
		return gson.toJson(new TestJson(1,"test1"));
	}
}