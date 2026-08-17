package com.uniinformation.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
//import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;

//import sun.misc.BASE64Decoder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.uniinformation.rest.TestRS.TestJson;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.SessionHelper.ActiveUserInfo;
import com.uniinformation.webcore.ZkSessionHelper;

@Path("/auth")
public class AuthRS
{
	@Context private HttpServletRequest request;
	static Gson gson = new GsonBuilder().setPrettyPrinting().create(); //gson should be thread safe

	@GET  //get for testing only, change to post later
	@DenyAll
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		JsonObject json = new JsonObject();
		json.addProperty("status", false);
		try{
			sh.logout();
			request.getSession().invalidate();
			json.addProperty("status", true);
		}
		catch(Exception ex){
			ex.printStackTrace();
			json.addProperty("errMsg", ex.getMessage());
		}
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}

	@GET
	@DenyAll
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response status(){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		ActiveUserInfo aui = sh.getActiveUserInfo();
		JsonObject json = new JsonObject();
		//request.getSession().setMaxInactiveInterval(5); //short internal for dev
		
		json.addProperty("status", sh.isLogin());
		json.addProperty("loginId", sh.getLoginId());
		if (aui == null){
			UniLog.log1("sth wrong, aui is null");
		}
		else{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			json.addProperty("ip", aui.ip);
			json.addProperty("firstAccessTime", sdf.format(sh.getActiveUserInfo().firstAccessTime));
			json.addProperty("lastAccessTime", sdf.format(sh.getActiveUserInfo().lastAccessTime));
			json.addProperty("agent", aui.agent);
			json.addProperty("sessionHelper", sh.toString());
			json.addProperty("maxInactiveInternal", request.getSession().getMaxInactiveInterval());
		}
		//return Response.status(Status.OK).entity(gson.toJson(json)).build();
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
	
	/***
	 * it do not generate new session
	 * it do not require login
	 * @return
	 */
	@GET
	@DenyAll
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping(){
		JsonObject json = new JsonObject();
		json.addProperty("status", true);
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
}