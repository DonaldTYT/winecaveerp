package com.uniinformation.rest.wc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
//import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;
//import org.zkoss.zsoup.helper.StringUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.SessionHelper.ActiveUserInfo;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.winecave.WineCaveApiUtil;
/***
 * <pre>
 *  Rest WebService for Winecave internal (Zkbi Web ERP/ Web Eshop)
 *  For external integration (Major), please use WineCaveRS instead.
 *  </pre>
 * @author andrew 2019
 *
 */
@Path("/wcp")
public class WineCavePrivateRS extends RSBase {
	/***
	 * Get stock item extra photos
	 * @param rg record ref id
	 * @return
	 */
	@GET
	@PermitAll
	@Path("/stocks/{rg}/extra_photos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stockExtraPhotos(@PathParam("rg") int rg){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		BiResult biResult = BiResultHelper.create(sh, "wc.StockImgs", String.format("mdoc_mrg = %d",rg), -1, null);  //TODO: can cache the biresult
		UniLog.log1("row count:" + biResult.getRowCount());
		
		JsonArray jImgs = new JsonArray();
		while (biResult.next()){  //only return the first record
			UniLog.log1("filekey:%s", biResult.getCell("mdoc_filekey").getString());
			JsonObject jImg = new JsonObject();
			//jImg.addProperty("filekey", biResult.getCell("mdoc_filekey").getString());
			//jImg.addProperty("sfilekey", biResult.getCell("mdoc_sfilekey").getString());
			jImg.addProperty("photosize", biResult.getCell("mdoc_photosize").getString());
			jImg.addProperty("thumbsize", biResult.getCell("mdoc_photosize").getString());
			jImgs.add(jImg);
		}
		if (jImgs.size() > 0)
			return Response.status(Status.OK).entity(gson.toJson(jImgs)).build();
		UniLog.log1("record not found");
		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
	}
	/***
	 * Get stock item extra photo
	 * @param rg record ref id
	 * @param idx: photo index
	 * @param isThumb: is thumbnail
	 * @return
	 */
	@GET
	@PermitAll
	@Path("/stocks/{rg}/extra_photos/{idx}/{is_thumb}")
	@Produces("image/*")
	public Response stockExtraPhoto(@PathParam("rg") int rg, @PathParam("idx") int idx, @PathParam("is_thumb") boolean is_thumb){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		BiResult biResult = BiResultHelper.create(sh, "wc.StockImgs", String.format("mdoc_mrg = %d",rg), -1, null);  //TODO: can cache the biresult
		UniLog.log1("row count:" + biResult.getRowCount() + ",rg:" + rg + ",idx:" + idx + ",is_thumb:" + is_thumb);
		if (idx < biResult.getRowCount() && biResult.loadOneRecV(idx)) {
			String filekey = biResult.getCell(is_thumb ? "mdoc_sfilekey" : "mdoc_filekey").getString();
			try{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (FilingUtil.getFile(sh.getAgent(), null, filekey, baos) != null){
					String mimeType = FilingUtil.guessFileType(baos.toByteArray()).get("mimeType");
					CacheControl cacheControl = new CacheControl();
					cacheControl.setMaxAge(86400); //one day
					UniLog.log1("File:%s", filekey);
					return Response.ok(baos.toByteArray(), mimeType).cacheControl(cacheControl).build();
				} else {
					UniLog.log1("filing record not found");
					return Response.status(Status.BAD_REQUEST).entity("filing record not found").type(MediaType.TEXT_PLAIN).build();
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		UniLog.log1("record not found");
		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
	}
	
	@Override
	public String getVersion() {
		return "1.00";
	}
	
}
