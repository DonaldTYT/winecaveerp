package com.uniinformation.rest.tools;

import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.kyoko.common.DateUtil;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

@Path("/jvmstatus")
public class JVMStatusRS extends RSBase {

	@Override
	public String getVersion() {
		return "1.00";
	}
	
	@GET
	@Path("/report")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response report(){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null, false);
		
		JsonObject json = new JsonObject();
		
		json.addProperty("status", true);
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
		json.addProperty("version", StringUtils.isBlank(getVersion()) ? "N/A" : getVersion());
		
		//only allow localhost request
		if (!sh.isLocalhostRequest()) {
			json.addProperty("status", false);
			json.addProperty("message", "unauthorized");
			return Response.status(Status.UNAUTHORIZED).entity(gson.toJson(json)).build();
		}
		
		
		//collect memory info
		long usedMem = 0;
		Runtime runtime = Runtime.getRuntime();
		try {
			usedMem = (long) (runtime.totalMemory()-runtime.freeMemory())/1048576;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		json.addProperty("mem_max", runtime.maxMemory()/1048576);
		json.addProperty("mem_total", runtime.totalMemory()/1048576);
		json.addProperty("mem_used", usedMem);
		json.addProperty("mem_free", runtime.freeMemory()/1048576);
		json.addProperty("user_count_active", SessionHelper.getActiveUserListCount());
		json.addProperty("user_count_all", SessionHelper.getUserListCount());
		
		
		
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}

}