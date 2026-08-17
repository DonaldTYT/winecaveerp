package com.uniinformation.rest.vincero;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.Vector;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.bicore.bischema.ExcelWorkSheetCache;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

@Path("/virs")
public class VinceroRS extends RSBase {
	@GET
	@Path("/view")
	@Produces(MediaType.APPLICATION_JSON)
	/***
	 * display the view raw data in json format
	 * @param viewid
	 * @param limit
	 * @return
	 */
	public Response getView( @DefaultValue("") @QueryParam("viewid") String viewid, @DefaultValue("0") @QueryParam("limit") int limit){
		try {
			SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null, false);
			viewid = StringUtils.startsWith(viewid,"vincero.") ? viewid : "vincero." + viewid;
			UniLog.log1("called viewid:%s limit:%d loginId:%s isLogin:%s", viewid, limit, sh.getLoginId(), sh.isLogin());

			//TODO check permission.
			//check user can access the target view. i.e. webmenu visibile 
			sh.generateSideMenu(request, null, "sidr");
			if (!sh.checkSideMenuViewExist(viewid)) {
				UniLog.log1("checkSideMenuViewExist %s fail", viewid);
				return Response.status(Status.BAD_REQUEST).entity("Error - no permission").type(MediaType.TEXT_PLAIN).build();
			}

			JsonObject rtnJson = new JsonObject();
			rtnJson.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
			rtnJson.addProperty("version", StringUtils.isBlank(getVersion()) ? "N/A" : getVersion());
			rtnJson.addProperty("status", true);
			rtnJson.addProperty("viewid", viewid);



			//TODO collect the view data. It will be used as execl web data source
			/*rtnJson.addProperty("recordCount", 12345);
			JsonArray ja = new JsonArray();
			//gen some dummy data
			for (int i=0; i<limit; i++) {
				JsonObject jo = new JsonObject();
				ja.add(jo);
				jo.addProperty("col_a","a"+i);
				jo.addProperty("col_b","b"+i);
				jo.addProperty("col_c",DateUtil.dateToDateTimeStr(new Date()));
				jo.addProperty("col_d",i);
				jo.addProperty("col_e",new Double(i));
			}
			rtnJson.add("recordList", ja);*/
			
			//BiResult br = BiResultHelper.create(sh, viewid, null, limit, null);
			BiResult br = ExcelWorkSheetCache.getBrFromCache(sh,viewid); //andrew240222 can consider to obtain biresult from cache, but need to reset the recidx??
			if (br == null)
				throw new Exception("Query record fail");

			rtnJson.addProperty("recordCount", br.getRowCount());
			JsonArray ja = new JsonArray();
			//while (br.next()) { }  //next does not work in cached br, need to change to loadOneRecV
			for(int i=0;i<br.getRowCount();i++) {
				br.loadOneRecV(i);
				JsonObject jo = new JsonObject();
				for (BiColumn bc : br.getColumns()) {
					if (bc.isInvisible(sh))
						continue;
					//jo.addProperty(bc.getLabel(), br.getCellString(bc.getLabel()));
					//jo.addProperty(buildExcelHeader(bc), br.getCellString(bc.getLabel()));  //andrew240222 use eng header
					if (bc.isNumber()) {
						jo.addProperty(buildExcelHeader(bc), br.getCellDouble(bc.getLabel()));
					}
					else {
						jo.addProperty(buildExcelHeader(bc), br.getCellString(bc.getLabel()));
					}
					
				}
				ja.add(jo);
			}
			rtnJson.add("recordList", ja);
			
			//UniLog.log1("json:%s", gson.toJson(rtnJson));

			//return Response.status(Status.OK).entity(gson.toJson(rtnJson)).build();
			return Response.status(Status.OK).entity(gson.toJson(ja)).build();
		}
		catch(Exception ex) {
			return Response.status(Status.BAD_REQUEST).entity("Error - " + ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
	}
	private static String buildExcelHeader(BiColumn bc) {
		return bc.getEngName() + " " +bc.getLabel();
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "1.00";
	}

}