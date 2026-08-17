package com.uniinformation.rest.atq;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.UniLog;

@Path("/atqp")
public class AntiquePrivateRS extends RSBase {

	/***
	 * Get antique all items
	 * @return
	 */
	@GET
	@PermitAll
	@Path("/antiques/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response antiqueAllItems() {
		if (!StringUtils.startsWithAny(request.getRemoteAddr(),"127.","0:0:0:0:0:0:0:1")) {
			UniLog.log("access restricted remoteAddr:" +request.getRemoteAddr());
			return Response.status(Status.BAD_REQUEST).entity("access restricted").type(MediaType.TEXT_PLAIN).build();
		}
		Reader reader = null;
		try {
			File file = new File("/yic/v/antique/antique.json");
			if (file.exists()) {
				//reader = new FileReader(file);
				reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
				JsonParser parser = new JsonParser();
				JsonArray ja = parser.parse(reader).getAsJsonArray();
				if (ja.size() > 0)
					return Response.status(Status.OK).entity(gson.toJson(ja)).build();
			}
			else {
				UniLog.log1("filing record not found");
				return Response.status(Status.BAD_REQUEST).entity("file not found").type(MediaType.TEXT_PLAIN).build();
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		UniLog.log1("record not found");
		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
	}

	/***
	 * Get antique item photo
	 * @param rg record ref id
	 * @param isThumb: is thumbnail
	 * @return
	 */
	@GET
	@PermitAll
	@Path("/antiques/{rg}/photos/{is_thumb}")
	@Produces("image/*")
	public Response antiquePhoto(@PathParam("rg") int rg, @PathParam("idx") int idx, @PathParam("is_thumb") boolean is_thumb){
		try {
			File file = new File(String.format("/yic/v/antique/img%s/%03d.jpg", is_thumb? "/thumb" : "", rg));
			if (file.exists()) {
				CacheControl cacheControl = new CacheControl();
				cacheControl.setMaxAge(86400); //one day
				return Response.ok(FileUtils.readFileToByteArray(file), "image/jpeg").cacheControl(cacheControl).build();
			}
			else {
				UniLog.log1("filing record not found");
				return Response.status(Status.BAD_REQUEST).entity("file not found").type(MediaType.TEXT_PLAIN).build();
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		UniLog.log1("record not found");
		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
	}

	@Override
	public String getVersion() {
		return "1.00";
	}
}
