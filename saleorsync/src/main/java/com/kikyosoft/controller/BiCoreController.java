package com.kikyosoft.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kikyosoft.utils.LogUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiCoreRpcApi;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.winecave.webcore.WinecaveSessionHelper;

//add this annotation:
@CrossOrigin(
origins = {
},
allowCredentials = "false", // set to "false" if you don't need cookies/JSESSIONID
maxAge = 3600
)


@RestController
@RequestMapping("/bicore")
public class BiCoreController {

  private final ObjectMapper om = new ObjectMapper();

  @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<java.util.Map<String, Object>> bicorePing(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    return ResponseEntity.ok(java.util.Map.of("ok", true));
  }

  @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<java.util.Map<String, Object>> bicoreLogin(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String loginid = request.getParameter("loginid");
    String password = request.getParameter("password");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,true);
		  ReturnMsg rtn =sp.login(loginid, password) ;
		  if(rtn != null && rtn.getStatus()) {
			  return ResponseEntity.ok(java.util.Map.of("ok", true));
		  } else {
			  return ResponseEntity.ok(java.util.Map.of("ok", false));
		  }
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }

  @GetMapping(value = "/getViewListColumns", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> bicoreGetViewListColumns(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
      String viewName = request.getParameter("viewName");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,false);
		  if(!sp.isLogin()) return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
		  JSONObject jo = new BiCoreRpcApi().getViewListColumns(sp, viewName);
		  if(jo != null)  {
			  jo.put("ok", true);
	          return ResponseEntity
                  .ok()
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(jo.toString()); // <-- send JSON text
			  
		  } else return ResponseEntity.ok(java.util.Map.of("ok", false));
		  
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }

  @GetMapping(value = "/openView", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> bicoreOpenView (
      HttpServletRequest request,
      HttpServletResponse response
  ) {
      String viewName = request.getParameter("viewName");
      String resultName= request.getParameter("resultName");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,false);
		  if(!sp.isLogin()) return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
		  BiResult br = (BiResult) sp.getSessionData("biresult."+resultName);
		  if(br == null) {
			  br = sp.newBiResult(viewName);
			  sp.putSessionData("biresult."+resultName, br);
		  }
		  return ResponseEntity.ok(java.util.Map.of("ok", true));
		  
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }

  @GetMapping(value = "/queryView", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> bicoreQueryView (
      HttpServletRequest request,
      HttpServletResponse response
  ) {
      String resultName= request.getParameter("resultName");
//      String startRecord = request.getParameter("start");
//      String maxRecord = request.getParameter("count");
      String condition = request.getParameter("condition");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,false);
		  if(!sp.isLogin()) return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
		  BiResult br = (BiResult) sp.getSessionData("biresult."+resultName);
		  if(br == null) return ResponseEntity.ok(java.util.Map.of("ok", false));
//		  int start = startRecord == null ? 0 : Integer.parseInt(startRecord);
//		  int max = maxRecord == null ? 1 : Integer.parseInt(maxRecord);
		  br.clear();
		  br.clearCondition();
		  br.query();
		  return ResponseEntity.ok(java.util.Map.of("ok", true, "count", br.getRecordCount()));
		  
		  /*
		  JSONArray ja = new JSONArray();
		  if(br.getRecordCount() < start) {
			  for(int i = start;i< br.getRecordCount();i++) {
				  br.loadOneRecV(i);
				  
			  }
		  }
		  return ResponseEntity.ok(java.util.Map.of("ok", true));
		  */
		  
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }

  @GetMapping(value = "/loadView", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> bicoreLoadView (
      HttpServletRequest request,
      HttpServletResponse response
  ) {
      String resultName= request.getParameter("resultName");
      String startRecord = request.getParameter("start");
      String maxRecord = request.getParameter("count");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,false);
		  if(!sp.isLogin()) return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
		  BiResult br = (BiResult) sp.getSessionData("biresult."+resultName);
		  if(br == null) return ResponseEntity.ok(java.util.Map.of("ok", false));
		  int start = startRecord == null ? 0 : Integer.parseInt(startRecord);
		  int max = maxRecord == null ? 1 : Integer.parseInt(maxRecord);
		  
		  JSONArray ja = new JSONArray();
		  for(int i = start;i< br.getRecordCount();i++) {
				  br.loadOneRecV(i);
				  ja.put( BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection()));
				  if(ja.length() >= max) break;
		  }
		  JSONObject jo = new JSONObject();
		  jo.put("ok", true);
		  jo.put("records", ja);
	      return ResponseEntity
                  .ok()
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(jo.toString()); // <-- send JSON text
		  
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }

  @GetMapping(value = "/fetchView", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> bicoreFetchView (
      HttpServletRequest request,
      HttpServletResponse response
  ) {
      String resultName= request.getParameter("resultName");
      String startRecord = request.getParameter("start");
      String maxRecord = request.getParameter("count");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,false);
		  if(!sp.isLogin()) return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
		  BiResult br = (BiResult) sp.getSessionData("biresult."+resultName);
		  if(br == null) return ResponseEntity.ok(java.util.Map.of("ok", false));
		  int start = startRecord == null ? 0 : Integer.parseInt(startRecord);
		  int max = maxRecord == null ? 1 : Integer.parseInt(maxRecord);
		  
		  JSONArray ja = new JSONArray();
		  for(int i = start;i< br.getRecordCount();i++) {
				  br.fetchOneRecV(i);
				  ja.put( BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection()));
				  if(ja.length() >= max) break;
		  }
		  JSONObject jo = new JSONObject();
		  jo.put("ok", true);
		  jo.put("records", ja);
	      return ResponseEntity
                  .ok()
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(jo.toString()); // <-- send JSON text
		  
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }

  @GetMapping(value = "/loadRecord", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> bicoreLoadRecord(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
      String resultName= request.getParameter("resultName");
      String recordNumber = request.getParameter("recordNumber");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,false);
		  if(!sp.isLogin()) return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
		  BiResult br = (BiResult) sp.getSessionData("biresult."+resultName);
		  if(br == null) return ResponseEntity.ok(java.util.Map.of("ok", false));
		  int rec = recordNumber == null ? 0 : Integer.parseInt(recordNumber);
		  
		  br.loadOneRecV(rec);
		  JSONObject jo = new JSONObject();
		  jo.put("ok", true);
		  jo.put("record", BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection()));
	      return ResponseEntity
                  .ok()
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(jo.toString()); // <-- send JSON text
		  
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }
  @GetMapping(value = "/fetchRecord", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> bicoreFetchRecord(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
      String resultName= request.getParameter("resultName");
      String recordNumber = request.getParameter("recordNumber");
	  try {
		  SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response,false);
		  if(!sp.isLogin()) return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
		  BiResult br = (BiResult) sp.getSessionData("biresult."+resultName);
		  if(br == null) return ResponseEntity.ok(java.util.Map.of("ok", false));
		  int rec = recordNumber == null ? 0 : Integer.parseInt(recordNumber);
		  
		  br.fetchOneRecV(rec);
		  JSONObject jo = new JSONObject();
		  jo.put("ok", true);
		  jo.put("record", BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection()));
	      return ResponseEntity
                  .ok()
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(jo.toString()); // <-- send JSON text
		  
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return ResponseEntity.ok(java.util.Map.of("ok", false));
	  }
  }

  
}
