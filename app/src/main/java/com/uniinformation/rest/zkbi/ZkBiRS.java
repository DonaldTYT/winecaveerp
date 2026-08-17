package com.uniinformation.rest.zkbi;

import java.util.Date;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.rest.RSBase;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

@Path("/zkbi")
public class ZkBiRS extends RSBase{

	@Override
	public String getVersion() {
		return "1.00";
	}
	/***
	 * generate temp login token via ws
	 * @param p_loginId
	 * @param p_password
	 * @return
	 */
	@POST
	@Path("/gentlt")
	@Produces(MediaType.APPLICATION_JSON)
	public Response genTLT(@HeaderParam("loginId") String p_loginId, @HeaderParam("password") String p_password){
		JsonObject json = new JsonObject();
		json.addProperty("status", true);
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
		json.addProperty("version", StringUtils.isBlank(getVersion()) ? "N/A" : getVersion());
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null, false);
		ReturnMsg rtn = sh.genTLT(p_loginId, p_password);
		if (rtn.isBad()) {
			return Response.status(Status.BAD_REQUEST).entity("Error - " + rtn.getMsg()).type(MediaType.TEXT_PLAIN).build();
		}
		String key = (String) rtn.getData();
		if (StringUtils.isBlank(key)) {
			return Response.status(Status.BAD_REQUEST).entity("Error - key is blank").type(MediaType.TEXT_PLAIN).build();
		}
		
		json.addProperty("tltKey", key);
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
}
