package com.uniinformation.rest.clerpmulti;

import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.erpv4.BiResultLocationAsAt;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

@Path("/clerpmulti")
public class ClerpMultiRS extends RSBase {


	@Override
	public String getVersion() {
		return "1.00";
	}
	
	
	
	/***
	 * trigger stock refill from external (curl localhost)
	 * @param limit
	 * @param showAllStock
	 * @param utimeAfter
	 * @return
	 */
	@GET
	@Path("/triggerstockrefill")
	//@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response triggerStockRefill(
			@QueryParam("cocode") String cocode,  
			@QueryParam("lcrg") Integer lcrg,  
			@QueryParam("asatdate") String asAtDate,  
			@QueryParam("fromloc") String fromLoc,  
			@QueryParam("toloc") String toLoc
			) {
		if (!StringUtils.startsWithAny(request.getRemoteAddr(),"127.","0:0:0:0:0:0:0:1")) {
			UniLog.log("access restricted remoteAddr:" +request.getRemoteAddr());
			return Response.status(Status.BAD_REQUEST).entity("access restricted - invalid ip").type(MediaType.TEXT_PLAIN).build();
		}
		if (cocode == null || lcrg == null || asAtDate == null || fromLoc == null || toLoc == null) {
			return Response.status(Status.BAD_REQUEST).entity("invalid param - param missing").type(MediaType.TEXT_PLAIN).build();
		}
		
		Date asAtDateObj = DateUtil.getDateY4MD(asAtDate);
		if (!DateUtil.isValid(asAtDateObj)) {
			return Response.status(Status.BAD_REQUEST).entity("invalid param - invalid date").type(MediaType.TEXT_PLAIN).build();
		}
		
		
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
		if (!StringUtils.equalsAny(sh.getAgent(),"clerpmulti", "clerpmultitest", "clerpmultidemo")){
			return Response.status(Status.BAD_REQUEST).entity("access restricted - invalid agent").type(MediaType.TEXT_PLAIN).build();
		}
		
		JsonObject json = new JsonObject();
		json.addProperty("timestamp", DateUtil.dateToDateTimeStr(new Date()));
		json.addProperty("version", StringUtils.isBlank(getVersion()) ? "N/A" : getVersion());
		//ReturnMsg rtn = stockRefill(sh, "001", 64, DateUtil.prevday(new Date()), "HQ01", "HZ01");
		ReturnMsg rtn = stockRefill(sh, cocode, lcrg, asAtDateObj, fromLoc, toLoc);
		UniLog.log1("stockRefill return: " + rtn);
		
		//HAHA add code here, better synchronize the method for handle concurrent access
		if (rtn.getStatus()) {
			json.addProperty("result", "OK");
			json.addProperty("message", rtn.getMsg());
			return Response.status(Status.OK).entity(gson.toJson(json)).build();	
		}
		else {
			return Response.status(Status.BAD_REQUEST).entity("error - " + rtn.getMsg()).type(MediaType.TEXT_PLAIN).build();
		}
	}
	/***
	 * 
	 * @param p_sh 
	 * @param p_cocode  - for change co
	 * @param p_lcrg    - for change co
	 * @param p_asAtDate  - refill as at date
	 * @param p_locationCode - refill location
	 * @return
	 */
	public static ReturnMsg stockRefill(SessionHelper p_sh, String p_cocode, int p_lcrg, Date p_asAtDate, String p_fromLoc, String p_toLoc) {
		try {
			UniLog.log1("called cocode:%s lcrg:%d asAt:%s from:%s to:%s", p_cocode, p_lcrg, p_asAtDate, p_fromLoc, p_toLoc);
			
			//validation
			if (p_sh == null) {
				return new ReturnMsg(false, "invalid sh");
			}
			
			if (StringUtils.isBlank(p_cocode) || p_lcrg <= 0) {
				return new ReturnMsg(false, "invalid cocode or lcrg");
			}
			if (!DateUtil.isValid(p_asAtDate)) {
				return new ReturnMsg(false, "invalid asatdate");
			}
			if (!StringUtils.equalsAny(p_fromLoc, "HQ01")) {
				return new ReturnMsg(false, "invalid from loc");
			}
			if (!StringUtils.equalsAny(p_toLoc, "HZ01")) {
				return new ReturnMsg(false, "invalid to loc");
			}
			
			//switch company
			try {
				Erpv4Config.setDefaultCocode(p_sh, p_cocode);
				if(!Erpv4Config.isMultiStockLoc(p_sh)) {
					return new ReturnMsg(false, "ignore for non multi stock loc");
				}
				Erpv4Config.setDefaultLcrg(p_sh,p_lcrg);
			}
			catch(Exception ex) {
				throw new Exception("switch company fail - " + ex.getMessage()) ;
			}
			
			//if (true) return new ReturnMsg(true, "dev in progress");
					
			
			//obtain record list
			//p_sh.addAccessRight("#allloc");  //BiResultLocationAsAt require allloc, should obtain from login 
			BiResultLocationAsAt locAsAtBr = (BiResultLocationAsAt) BiResultHelper.create(p_sh, "erpv4.LocationAsAt", null, String.format("stmd_date <= '%s' and stmd_loc = '%s' and stmd_sumqty < 0.00", DateUtil.toDateStringY4MD(p_asAtDate), p_toLoc), null, -1, null, false);
			if (locAsAtBr == null) {
				return new ReturnMsg(false, "query locationAsAt fail");
			}
			
			
			UniLog.log1("locAsAt rowCnt:%d", locAsAtBr.getRowCount());
			if (locAsAtBr.getRowCount() <= 0) {
				return new ReturnMsg(true, "no record available");
			}
			
			BiResult stmovBr = p_sh.getBiSchema().getViewByName("erpv4.MoCompanyTfr").newBiResult(p_sh.getLoginId(), null, null, p_sh);
			stmovBr.clearCurrentRec();
			int recCnt = 0;
			
			while (locAsAtBr.next()) {
				recCnt++;
				UniLog.log1("rec: date:%s asatdate:%s loc:%s irg:%d sumqty:%f org:%d ref4:%s",
					locAsAtBr.getCellDate("atd_date"),
					locAsAtBr.getAsAtDate(),
					locAsAtBr.getCellString("stmd_loc"),
					locAsAtBr.getCellInt("stmd_irg"),
					locAsAtBr.getCellDouble("stmd_sumqty"),
					locAsAtBr.getCellInt("stmd_org"),
					locAsAtBr.getCellString("stmd_ref4"));
				
				
				//insert master
				if (recCnt == 1) {
					stmovBr.getCell("stm_date").set(locAsAtBr.getAsAtDate());
					stmovBr.getCell("stm_fromloc").set(p_fromLoc);
					stmovBr.getCell("stm_toloc").set(locAsAtBr.getCellString("stmd_loc"));
					stmovBr.getCell("stm_status").set("Confirmed");
					stmovBr.getCell("stm_ctrspec").set("Refill");
				}
				
				
				//insert detail
				BiResult sr = stmovBr.getSubLink("erpv4.MoCompanyTfrDet");
				BiCellCollection col = sr.newRowCollection();								
				sr.addSubRecord(col, -1 ,"");
				col.getCell("stmd_irg").set(locAsAtBr.getCellInt("stmd_irg"));
				col.getCell("stmd_entryqty").set(-locAsAtBr.getCellDouble("stmd_sumqty"));
				col.getCell("stmd_eratio").set(1.0);
				col.getCell("stmd_org").set(locAsAtBr.getCellInt("stmd_org"));
				col.getCell("stmd_ref4").set(locAsAtBr.getCellString("stmd_ref4"));
				if(col.getCellString("stmd_ref4").equals("")) {
					col.getCell("stmd_lotno").sync("");
					col.getCell("stmd_exprdate").sync(DateUtil.zeroDate);
				}
			}
			
			//insert record
			UniLog.log1("reccnt:%d",recCnt);
			if (recCnt > 0) {
				stmovBr.addCurrent();
				return new ReturnMsg(true,"reccnt:" + recCnt);
			}
			
			
			
		}
		catch(Exception ex) {
			//ex.printStackTrace();
			UniLog.log1("error:"+ ex.getMessage());
			return new ReturnMsg(ex);
		}
		return ReturnMsg.defaultOk;
		
	}
	public static void main(String args[]) {
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("clerpmulti","hlv",null); 
		UniLog.log1("result:" + new ClerpMultiRS().stockRefill(sh, "001", 64, DateUtil.prevday(new Date()), "HQ01", "HZ01"));
		System.exit(0);
	}
	
}
