package com.uniinformation.rest.wac;

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
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.SessionHelper.ActiveUserInfo;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.winecave.WineCaveApiUtil;
/***
 * <pre>
 *  Rest WebService for WineAC external integration. 
 *  Version 1.00 2019/11/01
 *  - Initial version
 *  Version 1.1
 *  - stocks add utimeAfter param
 *  </pre>
 * @author andrew 2019
 *
 */
@Path("/wineac")
public class WineACRS extends RSBase{
	
	/***
	 * <pre>
	 * Get all stock items for sale (obsolated old version 210217)
	 * No Auth required
	 * </pre>
	 * @param limit limit the number of record. 0:no limit.
	 * @param showAllStock Y:show all stocks, include unavailable stock. N:show available stocks only. (Default N)
	 * @param utimeAfter val >=0: Only show record after [utimeAfter]. Unixtime format (Default -1)
	 * @return json data
	 */
	@GET
	@Path("/stocks_210217")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response stocks_210217(@DefaultValue("0") @QueryParam("limit") int limit, @DefaultValue("N") @QueryParam("showAllStock") String showAllStock, @DefaultValue("-1") @QueryParam("utimeAfter") int utimeAfter){
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ArrayList<Pair<String,Boolean>> orderBy = new ArrayList<Pair<String,Boolean>>();
		orderBy.add(Pair.of("st_irg", false));
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		Wherecl wherecl = null;
		if (!StringUtils.equalsAnyIgnoreCase(showAllStock, "Y", "YES", "TRUE")){
			//filter unavailable stock.  Remark: should generate the wherecl based on loginId
//			 wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders where or_org = pdls_org and pdls_loc = 'WH01' and pdls_stockqty > 0 and or_cocode in('WINECAVE','WCHUNG')) ");
			 wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders where or_org = pdls_org and pdls_loc = 'WH01' and pdls_stockqty > 0 ) ");
			//wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders where or_org = pdls_org and (1 = 1 or pdls_loc = 'WH01') and pdls_stockqty > 0 and or_cocode in('WINECAVE','WCHUNG')) ");
		}
		else{
//			wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders where or_org = pdls_org and pdls_loc = 'WH01' and or_cocode in('WINECAVE','WCHUNG')) ");
			wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders where or_org = pdls_org and pdls_loc = 'WH01' ) ");
			//wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders where or_org = pdls_org and (1 = 1 or pdls_loc = 'WH01') and or_cocode in('WINECAVE','WCHUNG')) ");
		}
		String customCondStr = null;
		if (utimeAfter >= 0) {
			customCondStr = String.format("utime > %d",utimeAfter);
		}
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiWaStockInfo", null, customCondStr, wherecl, limit, orderBy);  //TODO: can cache the biresult
		
		JsonObject json = new JsonObject();
		JsonArray jStocks = new JsonArray();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(biResult.getLastQuery()));
		json.addProperty("view", "wc.ApiWaStockInfo");
		json.addProperty("recordCount", biResult.getRowCount());
		JsonObject jHeader = new JsonObject();
		for (BiColumn biColumn : biResult.getListColumns()) {
			jHeader.addProperty(biColumn.getLabel(), biColumn.getEngName());
		}
		json.add("header", jHeader);
		json.add("itemList", jStocks);
		
		
		while (biResult.next()){
			JsonObject jStock = new JsonObject();
			for (BiColumn biColumn : biResult.getListColumns()) {
				if (biColumn.isNumber()){
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getNumber());
				}
				else{
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getString());
				}
			}
			//build photo url list
			//add master photo
			JsonArray photoURLs = new JsonArray();
			if (biResult.getCellInt("st_photoid" )> 0 && StringUtils.isNotBlank(biResult.getCellString("st_photofmt"))){
				String url = uri.getBaseUri().toString() + "wineac/stocks/" + + biResult.getCellInt("st_irg") + "/photo/0"; //idx=0 master photo
				photoURLs.add(url);
			}

				
			//add extra photo StockImgs (obtain from cache field, much faster)
			String extraImgStr = biResult.getCellString("st_extraimg");
			if (StringUtils.isNotBlank(extraImgStr)){
				String[] imgIds = extraImgStr.split("[ ]+");
				for (String imgId : imgIds){
					if (StringUtils.isNotBlank(imgId)){
						String url = uri.getBaseUri().toString() + "wineac/stocks/" + + biResult.getCellInt("st_irg") + "/photo/" + imgId.trim();
						photoURLs.add(url);
					}
					else{
						UniLog.log1("imgStr contain empty id [%s]", extraImgStr);
					}
				}
			}
			
			jStock.add("photoURLs", photoURLs);

			if(!biResult.getCellString("stbd_photoid").equals("")) {
				String brandPhotoURL = uri.getBaseUri().toString() + "wineac/brandphoto/" + biResult.getCellString("stbd_photoid");
				jStock.addProperty("brandPhotoURL", brandPhotoURL);
			}		
			/*
			if(biResult.getCellInt("stbd_photorg") > 0) {
				String brandPhotoURL = uri.getBaseUri().toString() + "wineac/brandphoto/" + biResult.getCellInt("stbd_photorg");
				jStock.addProperty("brandPhotoURL", brandPhotoURL);
			}		
			*/
				
			jStocks.add(jStock);
			//UniLog.log1(""+jStock.toString()); //for debug
		}
		stopWatch.stop();
		UniLog.log1("stopwatch duration %s", DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
	

	/***
	 * <pre>
	 * Get all stock items for sale
	 * No Auth required
	 * </pre>
	 * @param limit limit the number of record. 0:no limit.
	 * @param showAllStock Y:show all stocks, include unavailable stock. N:show available stocks only. (Default N)
	 * @param utimeAfter val >=0: Only show record after [utimeAfter]. Unixtime format (Default -1)
	 * @return json data
	 */
	@GET
	@Path("/stocks")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response stocks(@DefaultValue("0") @QueryParam("limit") int limit, @DefaultValue("N") @QueryParam("showAllStock") String showAllStock, @DefaultValue("-1") @QueryParam("utimeAfter") int utimeAfter){
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ArrayList<Pair<String,Boolean>> orderBy = new ArrayList<Pair<String,Boolean>>();
		orderBy.add(Pair.of("st_irg", false));
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		Wherecl wherecl = null;
		String customCondStr = null;
		if (utimeAfter >= 0) {
			customCondStr = String.format("utime > %d",utimeAfter);
		}
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiWaStockInfo2", null, customCondStr, wherecl, limit, orderBy);  //TODO: can cache the biresult
		
		JsonObject json = new JsonObject();
		JsonArray jStocks = new JsonArray();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(biResult.getLastQuery()));
		json.addProperty("view", "wc.ApiWaStockInfo2");
		json.addProperty("recordCount", biResult.getRowCount());
		JsonObject jHeader = new JsonObject();
		for (BiColumn biColumn : biResult.getListColumns()) {
			jHeader.addProperty(biColumn.getLabel(), biColumn.getEngName());
		}
		json.add("header", jHeader);
		json.add("itemList", jStocks);
		
		
		while (biResult.next()){
			JsonObject jStock = new JsonObject();
			for (BiColumn biColumn : biResult.getListColumns()) {
				if (biColumn.isNumber()){
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getNumber());
				}
				else{
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getString());
				}
			}
			//build photo url list
			//add master photo
			JsonArray photoURLs = new JsonArray();
			/*
			if (biResult.getCellInt("st_photoid" )> 0 && StringUtils.isNotBlank(biResult.getCellString("st_photofmt"))){
				String url = uri.getBaseUri().toString() + "wineac/stocks/" + + biResult.getCellInt("st_irg") + "/photo/0"; //idx=0 master photo
				photoURLs.add(url);
			}
			*/

				
			//add extra photo StockImgs (obtain from cache field, much faster)
			String extraImgStr = biResult.getCellString("st_extraimg");
			if (StringUtils.isNotBlank(extraImgStr)){
				String[] imgIds = extraImgStr.split("[ ]+");
				for (String imgId : imgIds){
					if (StringUtils.isNotBlank(imgId)){
						String url = uri.getBaseUri().toString() + "wineac/stocks/" + + biResult.getCellInt("st_irg") + "/photo/" + imgId.trim();
						photoURLs.add(url);
					}
					else{
						UniLog.log1("imgStr contain empty id [%s]", extraImgStr);
					}
				}
			}
			
			jStock.add("photoURLs", photoURLs);

			if(!biResult.getCellString("stbd_photoid").equals("")) {
				String brandPhotoURL = uri.getBaseUri().toString() + "wineac/brandphoto/" + biResult.getCellString("stbd_photoid");
				jStock.addProperty("brandPhotoURL", brandPhotoURL);
			}		
			/*
			if(biResult.getCellInt("stbd_photorg") > 0) {
				String brandPhotoURL = uri.getBaseUri().toString() + "wineac/brandphoto/" + biResult.getCellInt("stbd_photorg");
				jStock.addProperty("brandPhotoURL", brandPhotoURL);
			}		
			*/
				
			jStocks.add(jStock);
			//UniLog.log1(""+jStock.toString()); //for debug
		}
		stopWatch.stop();
		UniLog.log1("stopwatch duration %s", DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
	
	/***
	 * <pre>
	 * Get stock item detail, include lot information. 
	 * Basic Auth required
	 * </pre>
	 * @param rg record ref id. it can obtain from /stocks/itemList/st_irg
	 * @return json data
	 */
	@GET
	@Path("/stocks/{rg}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stock(@PathParam("rg") int rg){
		if (rg <= 0){
			return Response.status(Status.BAD_REQUEST).entity("Invalid rg").build();
		}
		ArrayList<Pair<String,Boolean>> orderBy = new ArrayList<Pair<String,Boolean>>();
		orderBy.add(Pair.of("pdls_irg", false));
		orderBy.add(Pair.of("pdls_org", false));
		
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", null, String.format("pdls_irg = %d and pdls_loc = 'WH01' and pdls_stockqty > 0 and or_cocode in('WINECAVE','WCHUNG')", rg), null, -1, orderBy);  //TODO: can cache the biresult
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiWaStockList", null, String.format("pdls_irg = %d and pdls_loc = 'WH01' and pdls_stockqty > 0 ", rg), null, -1, orderBy);  //TODO: can cache the biresult
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", null, String.format("pdls_irg = %d and (1 = 1 or pdls_loc = 'WH01') and pdls_stockqty > 0 and or_cocode in('WINECAVE','WCHUNG')", rg), null, -1, orderBy);  //TODO: can cache the biresult
		
		
		
		//BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", String.format("pdls_irg = %d",rg), -1, orderBy);  //TODO: can cache the biresult
		JsonObject json = new JsonObject();
		JsonArray jStocks = new JsonArray();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(biResult.getLastQuery()));
		json.addProperty("view", "wc.ApiWaStockList");
		json.addProperty("recordCount", biResult.getRowCount());
		JsonObject jHeader = new JsonObject();
		for (BiColumn biColumn : biResult.getListColumns()) {
			jHeader.addProperty(biColumn.getLabel(), biColumn.getEngName());
		}
		json.add("header", jHeader);
		json.add("lotList", jStocks);
		while (biResult.next(true)){
			JsonObject jStock = new JsonObject();
			
			//build data record 
			for (BiColumn biColumn : biResult.getListColumns()) {
				if (biColumn.isNumber()){
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getNumber());
				}
				else{
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getString());
				}
			}
			jStocks.add(jStock);
			//UniLog.log1(""+jStock.toString()); //for debug
		}
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
	
	/***
	 * <pre>
	 * Get stock item photo
	 * The URL can obtain from /stocks/itemList/photoURLs
	 * No Auth required
	 * </pre>
	 * @param rg record ref id
	 * @param idx 0:master photo &gt;=1:appendix photo (if any)
	 * @return
	 */
	@GET
	@Path("/stocks/{rg}/photo/{idx}")
	@PermitAll
	@Produces("image/*")
	public Response stockPhoto(@PathParam("rg") int rg, @PathParam("idx") int idx){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockInfo", String.format("st_irg = %d",rg), -1, null);  //TODO: can cache the biresult
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiWaStockInfo1", String.format("st_irg = %d",rg), -1, null);  //TODO: can cache the biresult
		UniLog.log1("row count:" + biResult.getRowCount());
		
		BiResult imgSR = biResult.getSubLink("wc.StockImgs");
		if (biResult.next(false)){  //only return the first record
			if (idx == 0){ //master photo
				File mainPhotoFile = getMainPhoto(biResult.getCell("st_photoid").getInt(), biResult.getCell("st_photofmt").getString());
				if (mainPhotoFile == null){
					UniLog.log1("photo file not found");
					return Response.status(Status.BAD_REQUEST).entity("Photo file not found").type(MediaType.TEXT_PLAIN).build();
				}
				String mimeType = FilingUtil.guessFileType(mainPhotoFile).get("mimeType");
				CacheControl cacheControl = new CacheControl();
				cacheControl.setMaxAge(86400); //one day
				UniLog.log1("File:%s mimeType:%s", mainPhotoFile.getAbsolutePath(), mimeType);
				return Response.ok(mainPhotoFile, mimeType).cacheControl(cacheControl).build();
			}
			else {  //extra photo StockImgs 
				for (CellCollection imgCC : imgSR.getRowCollectionList()){
					UniLog.log1("filekey:%s mrg:%d drg:%d", imgCC.getString("mdoc_filekey"), imgCC.getInt("mdoc_mrg"), imgCC.getInt("mdoc_drg"));
					if (imgCC.getInt("mdoc_drg") == idx){
						try{
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							if (FilingUtil.getFile(sh.getAgent(), null, imgCC.getString("mdoc_filekey"), baos) != null){
								String mimeType = FilingUtil.guessFileType(baos.toByteArray()).get("mimeType");
								CacheControl cacheControl = new CacheControl();
								cacheControl.setMaxAge(86400); //one day
								UniLog.log1("File:%s mimeType:%s", imgCC.getString("mdoc_filekey"), mimeType);
								return Response.ok(baos.toByteArray(), mimeType).cacheControl(cacheControl).build();
							}
							else {
								UniLog.log1("filing record not found");
								return Response.status(Status.BAD_REQUEST).entity("filing record not found").type(MediaType.TEXT_PLAIN).build();
							}
						}
						catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}
				UniLog.log1("invalid idx");
				return Response.status(Status.BAD_REQUEST).entity("invalid idx").type(MediaType.TEXT_PLAIN).build();
			}
		}
		UniLog.log1("record not found");
		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
	}
	
//	@GET
//	@Path("/brandphoto/{rg}")
//	@PermitAll
//	@Produces("image/*")
//	public Response stockBrandPhoto(@PathParam("rg") int rg){
//		if (rg > 0) {
//			//TODO obtain the real photo
//			File brandPhotoFile = new File( rg == 1 ? "/tmp/dummy1.jpg" : "/tmp/dummy2.jpg");
//			if (!brandPhotoFile.isFile()) {
//				UniLog.log1("file not found");
//				return Response.status(Status.BAD_REQUEST).entity("File not found").type(MediaType.TEXT_PLAIN).build();
//			}
//			String mimeType = FilingUtil.guessFileType(brandPhotoFile).get("mimeType");
//			CacheControl cacheControl = new CacheControl();
//			cacheControl.setMaxAge(86400); //one day
//			UniLog.log1("File:%s mimeType:%s", brandPhotoFile.getAbsolutePath(), mimeType);
//			return Response.ok(brandPhotoFile, mimeType).cacheControl(cacheControl).build();
//		}
//		UniLog.log1("record not found");
//		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
//	}
	
	@GET
	@Path("/brandphoto/{id}")
	@PermitAll
	@Produces("image/*")
	public Response stockBrandPhoto(@PathParam("id") String id){
		if (id != null && !id.equals("")) {
			try{
				SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (FilingUtil.getFile(sh.getAgent(), null, id, baos) != null){
					String mimeType = FilingUtil.guessFileType(baos.toByteArray()).get("mimeType");
					CacheControl cacheControl = new CacheControl();
					cacheControl.setMaxAge(86400); //one day
					UniLog.log1("File:%s mimeType:%s", id, mimeType);
					return Response.ok(baos.toByteArray(), mimeType).cacheControl(cacheControl).build();
				}  else {
					UniLog.log1("filing record not found");
					return Response.status(Status.BAD_REQUEST).entity("filing record not found").type(MediaType.TEXT_PLAIN).build();
				}
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
		UniLog.log1("record not found");
		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
	}
	private static File getMainPhoto(int p_photoId, String p_ext){
		if (p_photoId <= 0 || StringUtils.isBlank(p_ext)){
			UniLog.log1("ignore invalid photo %d %s", p_photoId, p_ext);
			return null;
		}
		NumberFormat objNumberFormat = new DecimalFormat("0000000000");
		String strStock = objNumberFormat.format(p_photoId); 
		String fileName = "/yic/v/erp_v3/message/STOCK_IMAGE"
				+File.separator+strStock.substring(0, 2)
				+File.separator+strStock.substring(2, 4)
				+File.separator+strStock.substring(4, 6)
				+File.separator+strStock.substring(6, 8)
				+File.separator+strStock
				+(StringUtils.isBlank(p_ext) ? ".jpg" : "." + p_ext);
		File file = new File(fileName);
		if (!file.exists()){
			UniLog.log1("file not found %s", fileName);
			return(null);
		}
		return(file);
	}
	
	/***
	 * <pre>
	 * Get storage items of particular user
	 * Basic Auth required
	 * </pre>
	 * @param limit limit the number of record. 0:no limit.
	 * @return json data
	 */
	@GET
	@Path("/storages")
	@Produces(MediaType.APPLICATION_JSON)
	public Response storages(@DefaultValue("0") @QueryParam("limit") int limit){
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		Wherecl wherecl = null;
		BiResult biResult = BiResultHelper.create(sh, "wc.stocklist", null, String.format("or_cocode = '%s'", sh.getLoginId().toUpperCase()) , wherecl, limit, null);  //TODO: can cache the biresult
		
		JsonObject json = new JsonObject();
		JsonArray jStocks = new JsonArray();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(biResult.getLastQuery()));
		json.addProperty("view", biResult.getView().getName());
		json.addProperty("recordCount", biResult.getRowCount());
		JsonObject jHeader = new JsonObject();
		for (BiColumn biColumn : biResult.getListColumns()) {
			jHeader.addProperty(biColumn.getLabel(), biColumn.getEngName());
		}
		json.add("header", jHeader);
		json.add("itemList", jStocks);
		
		
		while (biResult.next()){
			JsonObject jStock = new JsonObject();
			for (BiColumn biColumn : biResult.getListColumns()) {
				if (biColumn.isNumber()){
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getNumber());
				}
				else{
					jStock.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getString());
				}
			}
			
			jStocks.add(jStock);
			//UniLog.log1(""+jStock.toString()); //for debug
		}
		stopWatch.stop();
		UniLog.log1("stopwatch duration %s", DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
	
	
	@Override
	public String getVersion() {
		return "1.1";
	}
	
	
	
	/***
	 * Create a order using json data object. It support multiple items.
	 * Remark: form-method: POST
	 * @param p_inJsonString json string with itemList
	 * <pre>
	 * sample json
	 * {
	 * "timestamp":"2020/11/02 18:09:28",
	 * "payment_method":"cod",
   	 * "customer_id":0,    //optional. available if user logon
   	 * "customer_login":"xxxid",  //optional. available if user logon
   	 * "customer_note":"my order note",
   	 * "customer_ip_address":"::1",
   	 * "customer_user_agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0",
   	 * "billing_email":"xx123x@skdjaslkjdalksjd.com",
   	 * "billing_phone":"123",
   	 * "billing_first_name":"firstname",
   	 * "billing_last_name":"lastname",
   	 * "billing_company":"",
   	 * "billing_address_1":"street1",
   	 * "billing_address_2":"stree2",
   	 * "billing_city":"dist",
   	 * "billing_state":"KOWLOON",
   	 * "billing_postcode":"ZIP",
   	 * "billing_country":"HK",
   	 * "shipping_method":"TBC",
   	 * "shipping_first_name":"firstname",
   	 * "shipping_last_name":"lastname",
   	 * "shipping_company":"",
   	 * "shipping_address_1":"street1",
   	 * "shipping_address_2":"stree2",
   	 * "shipping_city":"dist",
   	 * "shipping_state":"KOWLOON",
   	 * "shipping_postcode":"ZIP",
   	 * "shipping_country":"HK",
   	 * "itemList":[
   	 *     {
   	 *        "pdls_irg":"1347",
   	 *        "pdls_org":0,
   	 *        "qty":3,
   	 *        "price":"1500",
   	 *        "subtotal":"4500"
   	 *     }
   	 *  ]
   	 * }
     * 
     * 
     * 
     * 
     * 
     * Remark:
     *    pdls_irg: Stock Ref No (It can obtain from ws:/stocks/{rg})
     *    pdls_org: Lot No       (It can obtain from ws:/stocks/{rg}. 0 - auto lot assignment)
     *    qty: Quantity 
     *    price: price           (It can obtain from ws:/stocks)
     * </pre>
	 * 
	 * @return json with ref id
	 */
	@POST
	@Path("/orders")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createOrder(String p_inJsonString){
		UniLog.log1("jsonStr:" + p_inJsonString);
	
		//parse json string
		JsonObject inJson = null;
		try{
			inJson = new JsonParser().parse(p_inJsonString).getAsJsonObject();
			UniLog.log1("json:" + inJson);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Invalid Json - " + ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		try {
			//TODO place order call for wineac
			SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
			int rg = WineCaveApiUtil.apiPlaceWebOrderByJson(sh, p_inJsonString);
			//int rg = 123;
			JsonObject outJson = new JsonObject();
			outJson.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
			outJson.addProperty("view", "create order");
			outJson.addProperty("stm_mrg", rg);
			return Response.status(Status.OK).entity(gson.toJson(outJson)).build();
		}
		catch(Exception ex){
			ex.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Error - " + ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
	}
}