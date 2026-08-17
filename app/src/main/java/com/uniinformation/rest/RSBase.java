package com.uniinformation.rest;

import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kyoko.common.DateUtil;
//import com.uniinformation.rest.wc.WineCaveRS;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public abstract class RSBase {
	@Context protected HttpServletRequest request;
	@Context protected UriInfo uri;
	private final static boolean fDebug = true;
	protected static Gson gson = new GsonBuilder()
						.setPrettyPrinting()
						.setDateFormat("yyyy/MM/dd HH:mm:ss")
						.create();
	
	/***
	 * <pre>
	 * Connectivity test. 
	 * No Auth required.
	 * </pre>
	 * @return
	 */
	@GET
	@PermitAll
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping(){
		JsonObject json = new JsonObject();
		json.addProperty("status", true);
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
		json.addProperty("version", StringUtils.isBlank(getVersion()) ? "N/A" : getVersion());
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null, false);
		if (fDebug){
			json.addProperty("isLogin", sh.isLogin());
			json.addProperty("loginId", sh.getLoginId());
			json.addProperty("agent", sh.getAgent());
			json.addProperty("class", this.getClass().getSimpleName());
			json.addProperty("uri.getBaseUri()", uri.getBaseUri().toString());
			json.addProperty("uri.getRequestUri()", uri.getRequestUri().toString());
			json.addProperty("sessionKey", sh.getSessionKey());
		}
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
	
	/***
	 * return version string
	 * @return
	 */
	public abstract String getVersion();
}
