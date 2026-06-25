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
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.wc.BiResultApiStockInfo;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.SessionHelper.ActiveUserInfo;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.winecave.WineCaveApiUtil;
/***
 * <pre>
 *  Rest WebService for Winecave external integration. 
 *  Version 1.00 2019/09/27
 *  - Basic Auth required (beside the ping ws)
 *  - https required
 *  Version 1.10
 *  - Add create order, get orders, get order
 *  Version 1.11
 *  - update create order / get orders / get order
 *  - ws stocks add showAllStock param
 *  Version 1.12
 *  - stocks select condition updated
 *  Version 1.13
 *  - stocks select condition updated
 *  Version 1.14
 *  - create order add customOrderNo
 *  </pre>
 * @author andrew 2019
 *
 */
@Path("/wc")
public class WineCaveRS extends RSBase{
	
	/***
	 * 
	 * Get all stock items for sale
	 * @param limit limit the number of record. 0:no limit.
	 * @param showAllStock Y:show all stocks, include unavailable stock. N:show available stocks only. (Default N)
	 * @return json data
	 */
	@GET
	@Path("/stocks")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stocks(@DefaultValue("0") @QueryParam("limit") int limit, @DefaultValue("N") @QueryParam("showAllStock") String showAllStock){
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ArrayList<Pair<String,Boolean>> orderBy = new ArrayList<Pair<String,Boolean>>();
		orderBy.add(Pair.of("st_irg", false));
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		Wherecl wherecl = null;
		if (!StringUtils.equalsAnyIgnoreCase(showAllStock, "Y", "YES", "TRUE")){
			//filter unavailable stock.  Remark: should generate the wherecl based on loginId
//			wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders where or_org = pdls_org and pdls_loc = 'WH01' and pdls_stockqty > 0 and or_cocode in('WINECAVE','MAJOR1')) ");
			
//  do not show WINECAVE stock			
//			wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders,stock st2 where or_org = pdls_org and st2.st_irg = pdls_irg and (or_cocode = 'MAJOR1' or (or_cocode = 'WINECAVE' and pdls_loc = 'WH01' and st2.st_retailprice > 0)) and pdls_stockqty > 0 ) ");
//			wherecl = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders,stock st2 where or_org = pdls_org and st2.st_irg = pdls_irg and (or_cocode not in ('MAJOR1') and pdls_loc = 'WH01') and pdls_stockqty > 0 ) ");
		}
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockInfo", null, "st_oicode <> ''", wherecl, limit, orderBy);  //TODO: can cache the biresult
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockInfo", null, null,wherecl, limit, orderBy);  //TODO: can cache the biresult
		
		JsonObject json = new JsonObject();
		JsonArray jStocks = new JsonArray();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(biResult.getLastQuery()));
		json.addProperty("view", "wc.ApiStockInfo");
		json.addProperty("recordCount", biResult.getRowCount());
		JsonObject jHeader = new JsonObject();
		for (BiColumn biColumn : biResult.getListColumns()) {
			jHeader.addProperty(biColumn.getLabel(), biColumn.getEngName());
		}
		json.add("header", jHeader);
		json.add("itemList", jStocks);
		
		
		//BiResult imgSR = biResult.getSubLink("wc.StockImgs");
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
				String url = uri.getBaseUri().toString() + "wc/stocks/" + + biResult.getCellInt("st_irg") + "/photo/0"; //idx=0 master photo
				photoURLs.add(url);
			}
			/*
			//add extra photo StockImgs (obtain from sublink, much slower)
			for (CellCollection imgCC : imgSR.getRowCollectionList()){
				UniLog.log1("filekey:%s mrg:%d drg:%d", imgCC.getString("mdoc_filekey"), imgCC.getInt("mdoc_mrg"), imgCC.getInt("mdoc_drg"));
				if (imgCC.getInt("mdoc_drg") > 0){
					String url = uri.getBaseUri().toString() + "wc/stocks/" + + biResult.getCellInt("st_irg") + "/photo/" + imgCC.getInt("mdoc_drg");
					photoURLs.add(url);
				}
				else{
					UniLog.log1("skip invalid mdoc_drg %d", imgCC.getInt("mdoc_drg"));
				}
			}
			*/
			//add extra photo StockImgs (obtain from cache field, much faster)
			String extraImgStr = biResult.getCellString("st_extraimg");
			if (StringUtils.isNotBlank(extraImgStr)){
				String[] imgIds = extraImgStr.split("[ ]+");
				for (String imgId : imgIds){
					if (StringUtils.isNotBlank(imgId)){
						String url = uri.getBaseUri().toString() + "wc/stocks/" + + biResult.getCellInt("st_irg") + "/photo/" + imgId.trim();
						photoURLs.add(url);
					}
					else{
						UniLog.log1("imgStr contain empty id [%s]", extraImgStr);
					}
				}
			}
			
			jStock.add("photoURLs", photoURLs);
			
			jStocks.add(jStock);
			//UniLog.log1(""+jStock.toString()); //for debug
		}
		stopWatch.stop();
		UniLog.log1("stopwatch duration %s", DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		return Response.status(Status.OK).entity(gson.toJson(json)).build();
	}
	/***
	 * 
	 * Get stock item detail. 
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
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", null, String.format("pdls_irg = %d and st_oicode <> '' and pdls_loc = 'WH01' and pdls_stockqty > 0 and or_cocode in('WINECAVE','MAJOR1')", rg), null, -1, orderBy);  //TODO: can cache the biresult
		
		
//	    do not show WINDAVE stock
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", null, String.format("pdls_irg = %d and st_oicode <> '' and (or_cocode = 'MAJOR1' or (or_cocode = 'WINECAVE' and pdls_loc = 'WH01' and st_retailprice > 0)) and pdls_stockqty > 0 ", rg), null, -1, orderBy);  //TODO: can cache the biresult
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", null, , null, -1, orderBy);  //TODO: can cache the biresult
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", null, String.format("pdls_irg = %d and pdls_sellingprice > 0", rg) , null, -1, orderBy);  //TODO: can cache the biresult
		
		
		
		//BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockList", String.format("pdls_irg = %d",rg), -1, orderBy);  //TODO: can cache the biresult
		JsonObject json = new JsonObject();
		JsonArray jStocks = new JsonArray();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(biResult.getLastQuery()));
		json.addProperty("view", "wc.ApiStockList");
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
	 * Get stock item photo
	 * The URL can obtain from /stocks/itemList/photoURLs
	 * @param rg record ref id
	 * @param idx 0:master photo &gt;=1:appendix photo
	 * @return
	 */
	@GET
	@Path("/stocks/{rg}/photo/{idx}")
	@Produces("image/*")
	public Response stockPhoto(@PathParam("rg") int rg, @PathParam("idx") int idx){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
//		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockInfo", String.format("st_irg = %d and st_oicode <> ''",rg), -1, null);  //TODO: can cache the biresult
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockInfo", String.format("st_irg = %d ",rg), -1, null);  //TODO: can cache the biresult
		UniLog.log1("row count:" + biResult.getRowCount());
		
		BiResult imgSR = biResult.getSubLink("wc.StockImgs");
		if (biResult.next(false)){  //only return the first record
			if (idx == 0){ //master photo
				/*
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
				*/
				byte mainPhotoBytes[] = getMainPhotoFromRemote(sh,biResult.getCell("st_photoid").getInt(), biResult.getCell("st_photofmt").getString());
				String mineType = FilingUtil.guessFileType(mainPhotoBytes).get("mimeType");
				CacheControl cacheControl = new CacheControl();
				cacheControl.setMaxAge(86400); //one day
//				UniLog.log1("File:%s mimeType:%s", mainPhotoFile.getAbsolutePath(), mimeType);
//				return Response.ok(mainPhotoFile, mimeType).cacheControl(cacheControl).build();
				return Response.ok(mainPhotoBytes,mineType).cacheControl(cacheControl).build();
//								return Response.ok(baos.toByteArray(), mimeType).cacheControl(cacheControl).build();
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
	
	private static byte[] getMainPhotoFromRemote(SessionHelper p_sh,int p_photoId, String p_ext) {
		if (p_photoId <= 0 || StringUtils.isBlank(p_ext)){
			UniLog.log1("ignore invalid photo %d %s", p_photoId, p_ext);
			return null;
		}
		NumberFormat objNumberFormat = new DecimalFormat("0000000000");
		String strStock = objNumberFormat.format(p_photoId); 
		/*
		String fileName = "/yic/v/erp_v3/message/STOCK_IMAGE"
				+File.separator+strStock.substring(0, 2)
				+File.separator+strStock.substring(2, 4)
				+File.separator+strStock.substring(4, 6)
				+File.separator+strStock.substring(6, 8)
				+File.separator+strStock
				+(StringUtil.isBlank(p_ext) ? ".jpg" : "." + p_ext);
				*/
		String fileName = "/yic/v/erp_v3/message/STOCK_IMAGE"
				+"/"+strStock.substring(0, 2)
				+"/"+strStock.substring(2, 4)
				+"/"+strStock.substring(4, 6)
				+"/"+strStock.substring(6, 8)
				+"/"+strStock
				+(StringUtils.isBlank(p_ext) ? ".jpg" : "." + p_ext);
		try {
			return(p_sh.newErpFileToByteArray(fileName));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
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
	
	private JsonObject checkJsonObject(JsonObject p_json, String p_key){
		return (p_json);
		
	}
	
	/***
	 * Create a order using json data object. It support multiple items.
	 * Remark: form-method: POST
	 * @param p_inJsonString json string with itemLis
	 * <pre>
	 * sample json
	 * {
     *  "itemList": [ 
     *     { "pdls_irg": 218, "pdls_org": 45, "qty": 1}
     *   ] 
     * }
     * Remark:
     *    pdls_irg: Stock Ref No (It can obtain from ws:/stocks/{rg})
     *    pdls_org: Lot No       (It can obtain from ws:/stocks/{rg})
     *    qty: Quantity 
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
		
		String customOrderNo = getStringFromJson(inJson, "customOrderNo", "");
		String action = getStringFromJson(inJson, "action", "");
		//construct order detail vector for api
		if(action == null || !action.equals("create")) {
			return Response.status(Status.BAD_REQUEST).entity("input json error - " + "action incorrect").type(MediaType.TEXT_PLAIN).build();
		}
		Vector orderDetails = new Vector();
		int itemCnt = 0;
		try{
			JsonArray itemListJsonArr = inJson.get("itemList").getAsJsonArray();
			UniLog.log1("itemList: %d %s", itemListJsonArr.size(), itemListJsonArr);
			itemCnt = itemListJsonArr.size();
			if (itemListJsonArr.size() <= 0){
				throw new Exception("itemList is empty");
			}
			for (JsonElement je :itemListJsonArr){
				
				JsonObject item = je.getAsJsonObject();
				orderDetails.add("");  //ignore st_oicode
				orderDetails.add(item.get("pdls_irg").getAsInt()); //stock rg
				orderDetails.add(item.get("pdls_org").getAsInt()); //lot rg
				orderDetails.add(item.get("qty").getAsInt());
				/*
				orderDetails.add(item.get("sold_by_bottle").getAsBoolean() ? 1 : 0);
				if(item.has("selling_price")) {
					orderDetails.add(item.get("selling_price").getAsDouble());
				} else {
					orderDetails.add(0.0);
				}
				*/
				orderDetails.add(1);  //always sold by bottle
				orderDetails.add(0.0);  //always no selling price
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("input json error - " + ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		
		try{
			SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
			
			int rg = WineCaveApiUtil.apiPlaceWebOrder(sh, itemCnt, orderDetails, MapUtil.of("customOrderNo",customOrderNo));
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
	
	/***
	 * Get a list of order by date range
	 * @param limit limit the number of record. 0:no limit.
	 * @param startDate start date (YYYY/MM/DD)
	 * @param endDate end date (YYYY/MM/DD)
	 * @return json data
	 */
	@GET
	@Path("/orders")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrders(@QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate, @DefaultValue("0") @QueryParam("limit") int limit){
		Date sDate = DateUtil.dateTimeStrToDate(startDate,false);
		Date eDate = DateUtil.dateTimeStrToDate(endDate,false);
		UniLog.log1("startDate:%s(%s) endDate:%s(%s) limit:%d",startDate, sDate, endDate, eDate, limit);
		if (sDate == null){
			return Response.status(Status.BAD_REQUEST).entity("Error - invalid param: startDate(YYYY/MM/DD)").type(MediaType.TEXT_PLAIN).build();
		}
		if (eDate == null){
			return Response.status(Status.BAD_REQUEST).entity("Error - invalid param: endDate(YYYY/MM/DD)").type(MediaType.TEXT_PLAIN).build();
		}
		
		ArrayList<Pair<String,Boolean>> orderBy = new ArrayList<Pair<String,Boolean>>();
		orderBy.add(Pair.of("stm_mrg", false));
		
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiSalesOrder", String.format("stm_date between '%s' and '%s' and stm_ref2='MAJORWEB'", startDate, endDate), limit, orderBy);  //TODO: can cache the biresult
		UniLog.log1("row count:" + biResult.getRowCount());
		
		JsonObject outJson = new JsonObject();
		JsonArray jOrders = new JsonArray();
		outJson.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date())); //change to query timestamp
		outJson.addProperty("view", biResult.getView().getName());
		outJson.addProperty("recordCount", biResult.getRowCount());
		
		//build header
		JsonObject jHeader = new JsonObject();
		for (BiColumn biColumn : biResult.getListColumns()) {
			jHeader.addProperty(biColumn.getLabel(), biColumn.getEngName());
		}
		outJson.add("header", jHeader);
		
		//build order List
		while (biResult.next()){
			JsonObject jOrder = new JsonObject();
			for (BiColumn biColumn : biResult.getListColumns()) {
				if (biColumn.isNumber()){
					jOrder.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getNumber());
				}
				else{
					jOrder.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getString());
				}
			}
			jOrders.add(jOrder);
		}
		outJson.add("orderList", jOrders);
		
		
		return Response.status(Status.OK).entity(gson.toJson(outJson)).build();
	}
	
	/***
	 * Get a single order by ref no (stm_mrg)
	 * Remark: form-method: GET
	 * @param rg record ref id
	 * @return
	 */
	@GET
	@Path("/orders/{rg}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrder(@PathParam("rg") int rg){
		UniLog.log1("rg=%d",rg);
		if (rg <= 0){
			return Response.status(Status.BAD_REQUEST).entity("Invalid rg.").build();
		}
		ArrayList<Pair<String,Boolean>> orderBy = new ArrayList<Pair<String,Boolean>>();
		orderBy.add(Pair.of("stm_mrg", false));
		
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiSalesOrder", String.format("stm_mrg = %d and stm_ref2='MAJORWEB'", rg), -1, orderBy);  //TODO: can cache the biresult
		UniLog.log1("row count:" + biResult.getRowCount());
		
		JsonObject outJson = new JsonObject();
		JsonArray jOrders = new JsonArray();
		outJson.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date())); //change to query timestamp
		outJson.addProperty("view", biResult.getView().getName());
		outJson.addProperty("recordCount", biResult.getRowCount());
		
		//build header
		JsonObject jHeader = new JsonObject();
		for (BiColumn biColumn : biResult.getListColumns()) {
			jHeader.addProperty(biColumn.getLabel(), biColumn.getEngName());
		}
		outJson.add("header", jHeader);
		
		//build order List
		while (biResult.next()){
			JsonObject jOrder = new JsonObject();
			for (BiColumn biColumn : biResult.getListColumns()) {
				if (biColumn.isNumber()){
					jOrder.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getNumber());
				}
				else{
					jOrder.addProperty(biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getString());
				}
			}
			jOrders.add(jOrder);
		}
		outJson.add("orderList", jOrders);
		
		return Response.status(Status.OK).entity(gson.toJson(outJson)).build();
	}
	private String getStringFromJson(JsonObject p_json, String p_key, String p_defaultValue){
		try{
			return (p_json.get(p_key).getAsString());
		}
		catch(Exception ex){
			UniLog.log1("json item %s not available", p_key);
			return p_defaultValue;
		}
	}
	
	@Override
	public String getVersion() {
		return "1.14";
	}

	@GET
	@Path("/stockbycode/{code}/photo/{idx}")
	@Produces("image/*")
	public Response stockByCodePhoto(@PathParam("code") String code, @PathParam("idx") int idx){
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		/*
		BiResult biResult = BiResultHelper.create(sh, "wc.ApiStockInfo", String.format("st_oicode = '%s'",code), -1, null);  //TODO: can cache the biresult
		UniLog.log1("row count:" + biResult.getRowCount());
		if(biResult.getRowCount() <= 0) {
			biResult = BiResultHelper.create(sh, "wc.ApiStockInfo", String.format("st_origin = '%s'",code), -1, 
					new VectorUtil().addElement( Pair.of("st_msize1", true)).toVector()
					);  //TODO: can cache the biresult
		}
		*/
		BiView bv = sh.getBiSchema().getViewByName("wc.ApiStockInfo");
		BiResult biResult = bv.newBiResult(sh.getLoginId(), null, null, sh);
		((BiResultApiStockInfo) biResult).setUseFilter(false);
		biResult.addCustomCondition( String.format("st_oicode = '%s'",code));
		biResult.addOrderByColumnList("st_msize1", true);
		biResult.query();
		UniLog.log1("row count 1:" + biResult.getRowCount());
		if(biResult.getRowCount() <= 0) {
			biResult.clearCondition();
			biResult.addCustomCondition( String.format("st_origin = '%s'",code));
			biResult.query();
		}
		UniLog.log1("row count 2:" + biResult.getRowCount());
		
		BiResult imgSR = biResult.getSubLink("wc.StockImgs");
		if (biResult.next(false)){  //only return the first record
			if (idx == 0){ //master photo
				/*
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
				*/

				byte mainPhotoBytes[] = getMainPhotoFromRemote(sh,biResult.getCell("st_photoid").getInt(), biResult.getCell("st_photofmt").getString());
				String mineType = FilingUtil.guessFileType(mainPhotoBytes).get("mimeType");
				CacheControl cacheControl = new CacheControl();
				cacheControl.setMaxAge(86400); //one day
//				UniLog.log1("File:%s mimeType:%s", mainPhotoFile.getAbsolutePath(), mimeType);
//				return Response.ok(mainPhotoFile, mimeType).cacheControl(cacheControl).build();
				return Response.ok(mainPhotoBytes,mineType).cacheControl(cacheControl).build();
//								return Response.ok(baos.toByteArray(), mimeType).cacheControl(cacheControl).build();
			}
			else {  //extra photo StockImgs 
				Vector imgList = imgSR.getRowCollectionList();
				if(imgList.size() >= idx) {
					CellCollection imgCC = (CellCollection) imgList.get(idx-1);
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
				/*
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
				*/
				UniLog.log1("invalid idx");
				return Response.status(Status.BAD_REQUEST).entity("invalid idx").type(MediaType.TEXT_PLAIN).build();
			}
		}
		UniLog.log1("record not found");
		return Response.status(Status.BAD_REQUEST).entity("Record not found").type(MediaType.TEXT_PLAIN).build();
	}
	
}