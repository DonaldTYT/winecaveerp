package com.uniinformation.rest.axa;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.axa.AxaUtil;
import com.uniinformation.axa.AxaUtilEx;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.winecave.WineCaveApiUtil;

@Path("/axa")
public class AxaRS extends RSBase {


	@Override
	public String getVersion() {
		return "1.00";
	}
	
	/***
	 * 
	 * @param p_inJsonString
	 * @return
	 */
	@POST
	@PermitAll
	@Path("/sendemail")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendEmail(String p_inJsonString){
		UniLog.log1("sendEmail(Ex) called remoteip:%s", request.getRemoteAddr());
		//IP validation
		if (!StringUtils.startsWithAny(request.getRemoteAddr(),"127.","0:0:0:0:0:0:0:1","192.168","172.")) {
			UniLog.log("access restricted remoteAddr:" +request.getRemoteAddr());
			return Response.status(Status.BAD_REQUEST).entity("access restricted").type(MediaType.TEXT_PLAIN).build();
		}
		try{
			//UniLog.log1("json:" + inJson);  //json is large, don't display it.
			JsonObject inJson = null;
			inJson = new JsonParser().parse(p_inJsonString).getAsJsonObject();
			String datFileName = inJson.get("datFileName").getAsString();
			String subject = inJson.get("subject").getAsString();
			String emailAddrs = null;
			if(inJson.get("emailAddrs") != null) {
				emailAddrs = inJson.get("emailAddrs").getAsString();
			}
			UniLog.log1("In AxaRS emailAddrs : [%s]",emailAddrs);
			byte[] datBytes = Base64Util.decodeAsBytes(inJson.get("dat").getAsString());
			UniLog.log1("datFileName:%s subject:%s size:%d", datFileName, subject, datBytes.length);
			ReturnMsg rtn = AxaUtilEx.sendEmailEx(subject, datFileName, datBytes,emailAddrs);
			
			JsonObject outJson = new JsonObject();
			outJson.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
			outJson.addProperty("result", rtn.getStatus() ? "OK" : "FAIL");
			outJson.addProperty("message", rtn.getMsg());
			UniLog.log1("sendEmail rtn:%s", rtn);
			return Response.status(Status.OK).entity(gson.toJson(outJson)).build();
		}
		catch(JsonParseException ex) {
			ex.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Invalid Json - " + ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		catch(Exception ex){
			ex.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Error - " + ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
	}
	
	@POST
	@PermitAll
	@Path("/getemailatts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEmailAtts(String p_inJsonString){
		UniLog.log1("called");
		if (!StringUtils.startsWithAny(request.getRemoteAddr(),"127.","0:0:0:0:0:0:0:1","192.168","172.")) {
			UniLog.log("access restricted remoteAddr:" +request.getRemoteAddr());
			return Response.status(Status.BAD_REQUEST).entity("access restricted").type(MediaType.TEXT_PLAIN).build();
		}
		try {
			JsonObject inJson = null;
			inJson = new JsonParser().parse(p_inJsonString).getAsJsonObject();
			String host = inJson.get("host").getAsString();
			String login = inJson.get("login").getAsString();
			String passwd = inJson.get("passwd").getAsString();
			boolean delEmailFlag = inJson.get("delEmailFlag").getAsBoolean();
			int maxEmailCnt = inJson.get("maxEmailCnt").getAsInt();
			
			JsonArray atts = new JsonArray();
			List<Map<String,Object>> attMapList = AxaUtil.getEmailAtts(host,login,passwd,delEmailFlag,maxEmailCnt);
			
			for (Map<String,Object> attMap: attMapList) {
				UniLog.log1("process sub:%s date:%s file:%s name:%s ", attMap.get("msgSub"), attMap.get("msgRecvDate"), attMap.get("attFile"), attMap.get("attName"));
				
				//ignore non pgp file
				if (!StringUtils.endsWith((String) attMap.get("attName"), ".pgp")){
					UniLog.log1("ignore non pgp file");
					continue;
				}
				//convert attMap to json
				atts.add(AxaUtil.buildAttJson(attMap));
				
				//remove attFile
				File attFile = (File) attMap.get("attFile");
				UniLog.log1("remove attFile %s", attFile.getAbsolutePath());
				FileUtils.deleteQuietly(attFile);
			}
			
			
			JsonObject outJson = new JsonObject();
			outJson.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
			outJson.addProperty("result", "OK");
			outJson.add("atts", atts);
			outJson.addProperty("message", "");
			return Response.status(Status.OK).entity(gson.toJson(outJson)).build();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Error - " + ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		
	}
	
	/***
	 * trigger poll email from axaweb server (curl localhost)
	 * @param limit
	 * @param showAllStock
	 * @param utimeAfter
	 * @return
	 */
	@GET
	@Path("/triggerpoll")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response triggerpoll() {
		if (!StringUtils.startsWithAny(request.getRemoteAddr(),"127.","0:0:0:0:0:0:0:1")) {
			UniLog.log("access restricted remoteAddr:" +request.getRemoteAddr());
			return Response.status(Status.BAD_REQUEST).entity("access restricted - invalid ip").type(MediaType.TEXT_PLAIN).build();
		}
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		if (!StringUtils.equalsAny(sh.getAgent(),"axaweb", "axa")){
			return Response.status(Status.BAD_REQUEST).entity("access restricted - invalid agent").type(MediaType.TEXT_PLAIN).build();
		}
		
		JsonObject json = new JsonObject();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
		json.addProperty("version", StringUtils.isBlank(getVersion()) ? "N/A" : getVersion());
		ReturnMsg rtn = AxaUtil.pollAndProcessEmail(true,1,sh);
		if (rtn.getStatus()) {
			json.addProperty("result", "OK");
			json.addProperty("message", rtn.getMsg());
			return Response.status(Status.OK).entity(gson.toJson(json)).build();	
		}
		else {
			return Response.status(Status.BAD_REQUEST).entity("Error - " + rtn.getMsg()).type(MediaType.TEXT_PLAIN).build();
		}
	}
	
}
