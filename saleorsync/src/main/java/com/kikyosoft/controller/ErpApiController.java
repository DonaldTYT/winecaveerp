package com.kikyosoft.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kikyosoft.utils.LogUtil;
import com.kikyosoft.utils.VectorUtil;
import com.kyoko.common.CoreLog;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.winecave.WineCaveConfig;
import com.uniinformation.winecave.WineCaveOrderPayPayDollar;
import com.uniinformation.winecave.WineCaveUtil;
import com.uniinformation.winecave.webcore.WinecaveSessionHelper;


//add this annotation:
@CrossOrigin(
origins = {
 "http://192.168.19.212:3000", // your Next dev server
 "http://192.168.19.212:3001", // your Next dev server
 "http://192.168.19.212:3002", // your Next dev server
 "http://192.168.19.212:9000"  // if you also call from this
},
allowCredentials = "true", // set to "false" if you don't need cookies/JSESSIONID
maxAge = 3600
)


@RestController
@RequestMapping("/erp")
public class ErpApiController {

  private final ObjectMapper om = new ObjectMapper();

  @PostMapping(value = "/login",
		  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		  produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ErpLoginResponse> loginPostQuery(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpSession session
  ) {
    String loginid = request.getParameter("loginid");
    String password = request.getParameter("password");
    return handleLogin(loginid, password, request, response);
  }
  
  // ---------- POST: JSON ----------
  @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ErpLoginResponse> loginJson(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpSession session
  ) throws IOException {

    request.setCharacterEncoding("UTF-8");
    String body = readBody(request);
    JsonNode root = om.readTree(body);

    String loginid = asText(root, "loginid");
    String password = asText(root, "password");

    // (optional) you can set response headers or cookies here if you want
    // response.setHeader("X-ERP-Version", "1.0");
    // response.addCookie(new javax.servlet.http.Cookie("example", "value"));

    return handleLogin(loginid, password, request, response);
  }

  // ---------- GET: query params ----------
  @GetMapping(value = "/login" , produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ErpLoginResponse> loginQuery(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpSession session
  ) {
    String loginid = request.getParameter("loginid");
    String password = request.getParameter("password");

    // (optional) same idea—headers/cookies are available
    // response.setHeader("X-ERP-Query", "1");

    return handleLogin(loginid, password, request , response);
  }

  // ---------- Shared logic ----------
  private ResponseEntity<ErpLoginResponse> handleLogin(
      String loginid,
      String password,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
	  WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response,true);
    if (!erpLogin(sp,loginid, password,request,response)) {
      // You can also tweak the raw response here if needed
      // response.setHeader("X-Auth-Reason", "Bad credentials");
      return ResponseEntity.status(401).body(ErpLoginResponse.error("Invalid loginid or password"));
    }

    // Ensure servlet session exists & attach attributes for later ERP APIs
//    session.setAttribute("erp_user_loginid", loginid);
//    session.setAttribute("erp_user_roles", "USER");

    // Example: set a cookie or header tied to this session (optional)
    // javax.servlet.http.Cookie mark = new javax.servlet.http.Cookie("erp_auth", "1");
    // mark.setPath("/");
    // mark.setHttpOnly(true);
    // response.addCookie(mark);

    // Stub mappings (replace with your real lookups)
    String saleorEmail = mapToSaleorEmail(loginid);
    String saleorPassword = mapToSaleorPassword(loginid);
    String shortName = sp.getWebLoginId();

    return ResponseEntity.ok(ErpLoginResponse.ok(saleorEmail, saleorPassword, shortName));
  }

  // ---------- Helpers ----------
  private static String readBody(HttpServletRequest req) throws IOException {
    try (BufferedReader r = req.getReader()) {
      return r.lines().collect(Collectors.joining("\n"));
    }
  }
  private static String asText(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return v != null && !v.isNull() ? v.asText() : null;
  }

  private boolean erpLogin (SessionHelper sp,String loginid, String password,HttpServletRequest request, HttpServletResponse response) {
	  try {
		  synchronized(sp) {
			  ReturnMsg rtn =sp.login("weborder:"+loginid, password) ;
			  return(rtn != null && rtn.getStatus());
		  }
	  } catch (Exception ex) {
		  LogUtil.log(ex);
		  return(false);
	  }
  }
  // ---------- Stubs to replace ----------
  private boolean pseudoValidate(String loginid, String password,HttpServletRequest request, HttpServletResponse repsonse) {
    return loginid != null && !loginid.isBlank() && password != null && password.equals(loginid);
  }
//  private String mapToSaleorEmail(String loginid) { return loginid.toLowerCase() + "@winecave.com"; }
  private String mapToSaleorEmail(String loginid) { return "weborder@winecavehk.com"; }
//  private String mapToSaleorPassword(String loginid) { return "TempP@ss-" + Math.abs(loginid.hashCode()); }
  private String mapToSaleorPassword(String loginid) { return "Qwe123456" ;};
  private String mapToShortName(String loginid) { return Character.toUpperCase(loginid.charAt(0)) + loginid.substring(1); }

  // ---------- Inline response DTO ----------
  public static class ErpLoginResponse {
    private boolean ok;
    private String saleorEmail;
    private String saleorPassword;
    private String shortName;
    private String error;

    public static ErpLoginResponse ok(String email, String password, String shortName) {
      ErpLoginResponse r = new ErpLoginResponse();
      r.ok = true; r.saleorEmail = email; r.saleorPassword = password; r.shortName = shortName; return r;
    }
    public static ErpLoginResponse error(String msg) {
      ErpLoginResponse r = new ErpLoginResponse();
      r.ok = false; r.error = msg; return r;
    }

    public boolean isOk() { return ok; }
    public String getSaleorEmail() { return saleorEmail; }
    public String getSaleorPassword() { return saleorPassword; }
    public String getShortName() { return shortName; }
    public String getError() { return error; }
    public void setOk(boolean ok) { this.ok = ok; }
    public void setSaleorEmail(String saleorEmail) { this.saleorEmail = saleorEmail; }
    public void setSaleorPassword(String saleorPassword) { this.saleorPassword = saleorPassword; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public void setError(String error) { this.error = error; }
  }
  
  @GetMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<java.util.Map<String, Object>> erpLogout(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
	 SessionHelper sp = WinecaveSessionHelper.getSessionHelper(request, response,false);
	 if(sp != null) {
		 synchronized(sp) {
			 sp.logout();
		 }
	 }
    HttpSession s = request.getSession(false);
    if (s != null) {
      s.invalidate();
    }
    // Return {"ok": true}
    return ResponseEntity.ok(java.util.Map.of("ok", true));
  }
  
  @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<java.util.Map<String, Object>> erpLogoutPost(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
	 SessionHelper sp = WinecaveSessionHelper.getSessionHelper(request, response,false);
	 if(sp != null) {
		 synchronized(sp) {
			 sp.logout();
		 }
	 }
    HttpSession s = request.getSession(false);
    if (s != null) {
      s.invalidate();
    }
    return ResponseEntity.ok(java.util.Map.of("ok", true));
  }

  static final long TIME_TO_REFRESH = 10000;
  @GetMapping(value = "/getloginid", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<java.util.Map<String, Object>> erpGetLoginId(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
	WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
	if(!sp.isLogin()) {
		/*
		  return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(Map.of(
	                        "error", "NO_LOGIN",
	                        "message", "User is not logged in.",
	                        "status", 401
	                ));
		*/
		return ResponseEntity.ok(java.util.Map.of("ok", false, "loginid", "Not Logged In"));
//		return ResponseEntity.ok(java.util.Map.of("ok", true , "loginid", "Not Logged In"));
	} else {
		synchronized(sp) {
		Date dNow = new Date();
		Date dLast= sp.getLastAccess();
		if(dNow.getTime() - dLast.getTime() > TIME_TO_REFRESH) {
			sp.setLastAccess(dNow);
			return ResponseEntity.ok(java.util.Map.of(
						"ok", true
						,"loginid", sp.getWebLoginId()
						,"refresh", true
						,"saleorEmail" , mapToSaleorEmail("abc")
						,"saleorPassword",  mapToSaleorPassword("abc")
						));
		} else {
			return ResponseEntity.ok(java.util.Map.of("ok", true , "loginid", sp.getWebLoginId()));
		}
		}
	}
  }
  @GetMapping(value = "/getNetPrice", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> erpGetNetPrice(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam Map<String, String> allParams
  ) {
		WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
		if(!sp.isLogin()) {
			return ResponseEntity.ok(java.util.Map.of("ok", true , "loginid", "Not Logged In"));
		} else {
			JSONObject jo = null;
			synchronized(sp) {
				JSONObject args = new JSONObject(allParams); 
				double price = args.getDouble("price");
				jo = sp.getNetPrice(null,price);
			}
	        return ResponseEntity
	                .ok()
	                .contentType(MediaType.APPLICATION_JSON)
	                .body(jo.put("ok", true).toString()); 
	        
		}
  }
  ;
  @GetMapping(value = "/getprofile", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> erpGetProfile(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam Map<String, String> allParams
  ) {
	WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
	if(!sp.isLogin()) {
		return ResponseEntity.ok(java.util.Map.of("ok", true , "loginid", "Not Logged In"));
	} else {
		JSONObject jo = null;
		synchronized(sp) {
			JSONObject args = new JSONObject(allParams); 
			jo = sp.getCustomerProfile(args);
		}
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jo.put("ok", true).toString()); 
        
	}
  }
  @PostMapping(
	      value = "/createOrder",
	      consumes = MediaType.APPLICATION_JSON_VALUE,
	      produces = MediaType.APPLICATION_JSON_VALUE
	  )
	  public ResponseEntity<String> erpCreateOrder(
	      HttpServletRequest request,
	      HttpServletResponse response
	  ) {
	    // 1) Verify ERP session is logged in
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

	    if (sp == null || !sp.isLogin()) {
	      String noLoginJson = "{\"ok\":false,\"Reason\":\"NOT_LOGGED_IN\"}";
	      return ResponseEntity
	          .status(HttpStatus.UNAUTHORIZED)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(noLoginJson);
	    }

	    try {
	      request.setCharacterEncoding("UTF-8");
	    } catch (Exception ignore) {
	      // ignore
	    }

	    // 2) Read JSON body from request
	    final String body;
	    try {
	      body = readBody(request);  // reuse your existing helper
	    } catch (IOException e) {
	      LogUtil.log(e);
	      String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
	      return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }

	    try {
	      // 3) Call ERP: sp.createOrder(jsonString) – it returns JSON string (success / fail)
	      String resultJson = sp.createOrder(sp.getVcode(),body);

	      if (resultJson == null || resultJson.isEmpty()) {
	        resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
	      }

	      // 4) Forward JSON directly to caller
	      return ResponseEntity
	          .ok()
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(resultJson);

	    } catch (Exception ex) {
	      LogUtil.log(ex);
	      String errJson = "{\"ok\":false,\"Reason\":\"CREATE_ORDER_EXCEPTION\"}";
	      return ResponseEntity
	          .status(HttpStatus.INTERNAL_SERVER_ERROR)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }
	  }

  @PostMapping(
	      value = "/commitOrder",
	      consumes = MediaType.APPLICATION_JSON_VALUE,
	      produces = MediaType.APPLICATION_JSON_VALUE
	  )
	  public ResponseEntity<String> erpCommitOrder(
	      HttpServletRequest request,
	      HttpServletResponse response
	  ) {
	    // 1) Verify ERP session is logged in
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

	    if (sp == null || !sp.isLogin()) {
	      String noLoginJson = "{\"ok\":false,\"Reason\":\"NOT_LOGGED_IN\"}";
	      return ResponseEntity
	          .status(HttpStatus.UNAUTHORIZED)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(noLoginJson);
	    }

	    try {
	      request.setCharacterEncoding("UTF-8");
	    } catch (Exception ignore) {
	      // ignore
	    }

	    // 2) Read JSON body from request
	    final String body;
	    try {
	      body = readBody(request);  // reuse your existing helper
	    } catch (IOException e) {
	      LogUtil.log(e);
	      String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
	      return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }

	    try {
	      // 3) Call ERP: sp.createOrder(jsonString) – it returns JSON string (success / fail)
	      String resultJson = sp.commitOrder(body);

	      if (resultJson == null || resultJson.isEmpty()) {
	        resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
	      }

	      // 4) Forward JSON directly to caller
	      return ResponseEntity
	          .ok()
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(resultJson);

	    } catch (Exception ex) {
	      LogUtil.log(ex);
	      String errJson = "{\"ok\":false,\"Reason\":\"CREATE_ORDER_EXCEPTION\"}";
	      return ResponseEntity
	          .status(HttpStatus.INTERNAL_SERVER_ERROR)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }
	  }
  
  @PostMapping(
		    value = "/payAndCommitOrder",
		    consumes = MediaType.APPLICATION_JSON_VALUE,
		    produces = MediaType.APPLICATION_JSON_VALUE
		)
		public ResponseEntity<String> erpPayAndCommitOrder(
		    HttpServletRequest request,
		    HttpServletResponse response
		) {
		    // 1) Verify ERP session
		    WinecaveSessionHelper sp =
		        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

		    if (sp == null || !sp.isLogin()) {
		        String noLoginJson = "{\"ok\":false,\"Reason\":\"NOT_LOGGED_IN\"}";
		        return ResponseEntity
		            .status(HttpStatus.UNAUTHORIZED)
		            .contentType(MediaType.APPLICATION_JSON)
		            .body(noLoginJson);
		    }

		    try {
		        request.setCharacterEncoding("UTF-8");
		    } catch (Exception ignore) {
		    }

		    final String body;
		    try {
		        body = readBody(request);
		    } catch (IOException e) {
		        LogUtil.log(e);
		        String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
		        return ResponseEntity
		            .status(HttpStatus.BAD_REQUEST)
		            .contentType(MediaType.APPLICATION_JSON)
		            .body(errJson);
		    }

		    try {
		        // 2) Commit order in ERP
		        String resultJson = sp.commitOrder(body);

		        if (resultJson == null || resultJson.isEmpty()) {
		            resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
		        }

		        org.json.JSONObject erp = new org.json.JSONObject(resultJson);
		        if (!erp.optBoolean("ok", false)) {
		            // ERP failed -> just forward the error
		            return ResponseEntity
		                .ok()
		                .contentType(MediaType.APPLICATION_JSON)
		                .body(erp.toString());
		        }

		        // Assume ERP returns orderNo + amount (adjust to your real fields)
		        String orderNo = erp.optString("orderNo", null);
		        String amountStr = erp.optString("grandTotal", "0");
		        java.math.BigDecimal amount = new java.math.BigDecimal(amountStr);

		        // 3) Build returnUrl & notifyUrl for payment gateway
		        String baseUrl = getBaseUrl(request); // helper below
		        String returnUrl = baseUrl + "/api/erpapi/erp/paymentReturn?orderNo=" + orderNo;
		        String notifyUrl = baseUrl + "/api/erpapi/erp/paymentNotify";

		        // 4) Call your payment gateway init (PayDollar, etc.)
//		        PaymentInitResult pay = paymentGatewayService.initiatePayment(
//		            orderNo,
//		            amount,
//		            returnUrl,
//		            notifyUrl
//		        );

		        // 5) Build response for frontend
		        org.json.JSONObject resp = new org.json.JSONObject();
		        resp.put("ok", true);
		        resp.put("orderNo", orderNo);
//		        resp.put("paymentGatewayUrl", pay.getRedirectUrl());
		        // If your gateway needs POST with HTML form, you can also return:
		        // resp.put("paymentFormHtml", pay.getHtmlForm());

		        return ResponseEntity
		            .ok()
		            .contentType(MediaType.APPLICATION_JSON)
		            .body(resp.toString());

		    } catch (Exception ex) {
		        LogUtil.log(ex);
		        String errJson = "{\"ok\":false,\"Reason\":\"COMMIT_ORDER_EXCEPTION\"}";
		        return ResponseEntity
		            .status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .contentType(MediaType.APPLICATION_JSON)
		            .body(errJson);
		    }
		}

		// Helper: get http(s)://host:port
		private String getBaseUrl(HttpServletRequest request) {
		    StringBuffer url = request.getRequestURL();
		    String uri = request.getRequestURI();
		    return url.substring(0, url.length() - uri.length());
		}


     @GetMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<java.util.Map<String, Object>> erpRegister(
      HttpServletRequest request,
      HttpServletResponse response
    	) {
    	SessionHelper sp = WinecaveSessionHelper.getSessionHelper(request, response,false);
    	
    	Properties winecaveConfig;
    	try {
    		/*
    		IniHelper winecaveConfig = null;
    		winecaveConfig = new IniHelper(null, "winecaveconfig.properties", null);
    		if(winecaveConfig != null) {
    			String ss = winecaveConfig.getString("rpcServerHost");
    			CoreLog.log(ss);
    		}
    		*/
    		/*
    		winecaveConfig = IniHelper.loadProperty(null, "WineCaveConfig.properties");
    		if(winecaveConfig != null) {
    			String ss = winecaveConfig.getProperty("rpcServerHost");
    			CoreLog.log(ss);
    		}
    		*/
    		//String ss = WineCaveConfig.getProperty("rpcServerHost");
    		//String ss = WineCaveUtil.getOrderStatusByCode("SO25-0827");
    		//CoreLog.log(ss);
    		HttpSession s = request.getSession(false);
    		return ResponseEntity.ok(java.util.Map.of("ok", true));
    	} catch (Exception ex) {
    		CoreLog.log(ex);
    		HttpSession s = request.getSession(false);
    		return ResponseEntity.ok(java.util.Map.of("ok", false, "message", ex.toString()));
    	}
     }

//  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<java.util.Map<String, Object>> erpRegisterPost(
//      HttpServletRequest request,
//      HttpServletResponse response
//  ) {
//    	SessionHelper sp = WinecaveSessionHelper.getSessionHelper(request, response,false);
//    	
//    	Properties winecaveConfig;
//    	try {
//    		HttpSession s = request.getSession(false);
//    		return ResponseEntity.ok(java.util.Map.of("ok", true));
//    	} catch (Exception ex) {
//    		CoreLog.log(ex);
//    		HttpSession s = request.getSession(false);
//    		return ResponseEntity.ok(java.util.Map.of("ok", false, "message", ex.toString()));
//    	}
//  }
     @PostMapping(
    		    value = "/register",
    		    consumes = MediaType.APPLICATION_JSON_VALUE,
    		    produces = MediaType.APPLICATION_JSON_VALUE
    		)
    		public ResponseEntity<Map<String, Object>> erpRegisterPost(
    		    @RequestBody Map<String, Object> body,
    		    HttpServletRequest request,
    		    HttpServletResponse response
    		) {
    		    SessionHelper sp = WinecaveSessionHelper.getSessionHelper(request, response, false);

    		    try {
    		        // Read values that our Next.js page is sending:
    		        String loginId     = (String) body.get("loginId");
    		        String password    = (String) body.get("password");
    		        String englishName = (String) body.get("englishName");
    		        String chineseName = (String) body.get("chineseName");
    		        String address     = (String) body.get("address");
    		        String contact     = (String) body.get("contact");
    		        String telephone   = (String) body.get("telephone");
    		        String fax         = (String) body.get("fax");
    		        String email       = (String) body.get("email");
    		        String channel     = (String) body.get("channel");
    		        String locale      = (String) body.get("locale");

    		    	String result = WineCaveUtil.registorNewCustomer(0,loginId, password, englishName, chineseName, address,contact, telephone, fax, email);
    		        HttpSession s = request.getSession(false);

    		        // TODO: do your registration logic here, e.g. create ERP customer
    		    	if(result != null && result.startsWith("OK  ")) {
    		    		
		BiUtil.sendEmail(
				Pair.of("storage@winecavehk.com", (String) null),
				new VectorUtil()
				.addElement(Pair.of(email,(String) null))
				.toVector(),
				null, 
				null, 
				"Wine Cave System Registration On " + new java.util.Date().toString(),
				null, 
				new StringUtil()
					.cat("Dear Customer","\n")
					.addline()
					.cat("Your Wine Cave Account Has Been Create As Follow :-","\n")
					.addline()
					.cat("LoginId : " +  loginId, "\n")
//					.cat("Password: " +  password, "\n")
				.toString(),
				null, sp);
    		    		
    		    		
    		    		return ResponseEntity.ok(Map.of("ok", true));
    		    	} else {
    		    		return ResponseEntity.ok(Map.of("ok", false, "message", result.substring(4)));
    		    	}

    		        // or on success:
    		        // return ResponseEntity.ok(Map.of("ok", true));
    		    } catch (Exception ex) {
    		        CoreLog.log(ex);
    		        HttpSession s = request.getSession(false);
    		        return ResponseEntity.ok(Map.of("ok", false, "message", ex.toString()));
    		    }
    		}

  @PostMapping(
	      value = "/updateConsignment",
	      consumes = MediaType.APPLICATION_JSON_VALUE,
	      produces = MediaType.APPLICATION_JSON_VALUE
	  )
	  public ResponseEntity<String> erpUpdateConsignment(
	      HttpServletRequest request,
	      HttpServletResponse response
	  ) {
	    // 1) Verify ERP session is logged in
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

	    if (sp == null || !sp.isLogin()) {
	      String noLoginJson = "{\"ok\":false,\"Reason\":\"NOT_LOGGED_IN\"}";
	      return ResponseEntity
	          .status(HttpStatus.UNAUTHORIZED)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(noLoginJson);
	    }

	    try {
	      request.setCharacterEncoding("UTF-8");
	    } catch (Exception ignore) {
	      // ignore
	    }

	    // 2) Read JSON body from request
	    final String body;
	    try {
	      body = readBody(request);  // reuse your existing helper
	    } catch (IOException e) {
	      LogUtil.log(e);
	      String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
	      return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }

	    try {
	      // 3) Call ERP: sp.createOrder(jsonString) – it returns JSON string (success / fail)
	      String resultJson = sp.updateConsignment(body);

	      if (resultJson == null || resultJson.isEmpty()) {
	        resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
	      }

	      // 4) Forward JSON directly to caller
	      return ResponseEntity
	          .ok()
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(resultJson);

	    } catch (Exception ex) {
	      LogUtil.log(ex);
	      String errJson = "{\"ok\":false,\"Reason\":\"UPDATE_CONSIGNMENT_EXCEPTION\"}";
	      return ResponseEntity
	          .status(HttpStatus.INTERNAL_SERVER_ERROR)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }
	  }

  @PostMapping(
		  
	      value = "/clientTransfer",
	      consumes = MediaType.APPLICATION_JSON_VALUE,
	      produces = MediaType.APPLICATION_JSON_VALUE
	  )
	  public ResponseEntity<String> erpClientTransfer(
	      HttpServletRequest request,
	      HttpServletResponse response
	  ) {
	    // 1) Verify ERP session is logged in
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

	    if (sp == null || !sp.isLogin()) {
	      String noLoginJson = "{\"ok\":false,\"Reason\":\"NOT_LOGGED_IN\"}";
	      return ResponseEntity
	          .status(HttpStatus.UNAUTHORIZED)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(noLoginJson);
	    }

	    try {
	      request.setCharacterEncoding("UTF-8");
	    } catch (Exception ignore) {
	      // ignore
	    }

	    // 2) Read JSON body from request
	    final String body;
	    try {
	      body = readBody(request);  // reuse your existing helper
	    } catch (IOException e) {
	      LogUtil.log(e);
	      String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
	      return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }

	    try {
	      // 3) Call ERP: sp.createOrder(jsonString) – it returns JSON string (success / fail)
	      String resultJson = sp.clientTransfer(body);

	      if (resultJson == null || resultJson.isEmpty()) {
	        resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
	      }

	      // 4) Forward JSON directly to caller
	      return ResponseEntity
	          .ok()
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(resultJson);

	    } catch (Exception ex) {
	      LogUtil.log(ex);
	      String errJson = "{\"ok\":false,\"Reason\":\"UPDATE_CONSIGNMENT_EXCEPTION\"}";
	      return ResponseEntity
	          .status(HttpStatus.INTERNAL_SERVER_ERROR)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }
	  }

  @PostMapping(
		  
	      value = "/sendMessage",
	      consumes = MediaType.APPLICATION_JSON_VALUE,
	      produces = MediaType.APPLICATION_JSON_VALUE
	  )
	  public ResponseEntity<String> erpSendMessage(
	      HttpServletRequest request,
	      HttpServletResponse response
	  ) {
	    // 1) Verify ERP session is logged in
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

	    try {
	      request.setCharacterEncoding("UTF-8");
	    } catch (Exception ignore) {
	      // ignore
	    }

	    // 2) Read JSON body from request
	    final String body;
	    try {
	      body = readBody(request);  // reuse your existing helper
	    } catch (IOException e) {
	      LogUtil.log(e);
	      String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
	      return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }

	    try {
	      // 3) Call ERP: sp.createOrder(jsonString) – it returns JSON string (success / fail)
	      String resultJson = sp.sendMessage(body);

	      if (resultJson == null || resultJson.isEmpty()) {
	        resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
	      }

	      // 4) Forward JSON directly to caller
	      return ResponseEntity
	          .ok()
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(resultJson);

	    } catch (Exception ex) {
	      LogUtil.log(ex);
	      String errJson = "{\"ok\":false,\"Reason\":\"UPDATE_CONSIGNMENT_EXCEPTION\"}";
	      return ResponseEntity
	          .status(HttpStatus.INTERNAL_SERVER_ERROR)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }
	  }

  @GetMapping(value = "/orderpay", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<java.util.Map<String, Object>> erpOrderPay(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
	WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
	if(!sp.isLogin()) {
		return ResponseEntity.ok(java.util.Map.of("ok", false, "message", "Not Logged In"));
	} else {
		synchronized(sp) {
			WineCaveOrderPayPayDollar orderpay = new WineCaveOrderPayPayDollar();
			String url = orderpay.getPayUrl();
			String param = orderpay.getRequestParams(213711, 0, sp);
			JSONObject jo = new JSONObject(param);
			return ResponseEntity.ok(java.util.Map.of("ok", true,"url" , url ));
		}
	}
  }

  @GetMapping(value = "/getstocklist", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> erpStockList(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam Map<String, String> allParams
  ) {
	WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
	if(!sp.isLogin()) {
		return ResponseEntity.ok(java.util.Map.of("ok", true , "loginid", "Not Logged In"));
	} else {
		JSONObject jo = null;
		synchronized(sp) {
			JSONObject args = new JSONObject(allParams); 
			jo = sp.getCustomerStockList(args);
		}
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jo.put("ok", true).toString()); 
        
	}
  }

//  @GetMapping(
//		  
//	      value = "/resetpasswd",
//	      consumes = MediaType.APPLICATION_JSON_VALUE,
//	      produces = MediaType.APPLICATION_JSON_VALUE
//	  )
//	  public ResponseEntity<String> erpResetPasswd(
//	      HttpServletRequest request,
//	      HttpServletResponse response
//	  ) {
//	    // 1) Verify ERP session is logged in
//	    WinecaveSessionHelper sp =
//	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
//
//	    try {
//	      request.setCharacterEncoding("UTF-8");
//	    } catch (Exception ignore) {
//	      // ignore
//	    }
//
//	    // 2) Read JSON body from request
//	    final String body;
//	    try {
//	      body = readBody(request);  // reuse your existing helper
//	    } catch (IOException e) {
//	      LogUtil.log(e);
//	      String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
//	      return ResponseEntity
//	          .status(HttpStatus.BAD_REQUEST)
//	          .contentType(MediaType.APPLICATION_JSON)
//	          .body(errJson);
//	    }
//
//	    try {
//	      // 3) Call ERP: sp.createOrder(jsonString) – it returns JSON string (success / fail)
//	      String resultJson = sp.resetPasswd(body);
//
//	      if (resultJson == null || resultJson.isEmpty()) {
//	        resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
//	      }
//
//	      // 4) Forward JSON directly to caller
//	      return ResponseEntity
//	          .ok()
//	          .contentType(MediaType.APPLICATION_JSON)
//	          .body(resultJson);
//
//	    } catch (Exception ex) {
//	      LogUtil.log(ex);
//	      String errJson = "{\"ok\":false,\"Reason\":\"UPDATE_CONSIGNMENT_EXCEPTION\"}";
//	      return ResponseEntity
//	          .status(HttpStatus.INTERNAL_SERVER_ERROR)
//	          .contentType(MediaType.APPLICATION_JSON)
//	          .body(errJson);
//	    }
//	  }

  // ---------- GET: query params ----------
  @GetMapping(value = "/resetpasswd" , produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> resetPasswd(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpSession session
  ) {
    String loginid = request.getParameter("loginid");
    String email = request.getParameter("email");
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

	    try {
	      String resultJson = sp.resetPasswd(loginid,email);
	      if (resultJson == null || resultJson.isEmpty()) {
	        resultJson = "{\"ok\":false,\"Reason\":\"EMPTY_RESPONSE_FROM_ERP\"}";
	      }

	      // 4) Forward JSON directly to caller
	      return ResponseEntity
	          .ok()
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(resultJson);
	    } catch (Exception ignore) {
	      // ignore
	    }
	    String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
	    return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
  }

  // ---------- GET: query params ----------
  @GetMapping(value = "/syncProduct" , produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> syncProduct(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpSession session
  ) {
    String key = request.getParameter("key");
    String irg = request.getParameter("irg");

    // (optional) same idea—headers/cookies are available
    // response.setHeader("X-ERP-Query", "1");
    JSONObject jo = new JSONObject();
    if(key.equals("dumbcow")) {
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
    	int i = Integer.parseInt(irg.trim());
    	JSONArray ja = new JSONArray();
    	ja.put(i);
    	jo.put("irgList", ja);
    	sp.syncProductToSaleor(jo);
    }
    jo = new JSONObject();
    jo.put("ok", false);
	    return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(jo.toString());

  }
  
  // ---------- GET: return document PDF ----------
  // Example:
  //   GET /erp/getDocument?docType=SO&docCode=SO25-0827
  //   GET /erp/getDocument?docType=INVOICE&docCode=INV123&lang=zh-Hant&copy=1
  @GetMapping(value = "/getDocument", produces = MediaType.APPLICATION_PDF_VALUE)
  public void getDocument(
      @RequestParam("docType") String docType,
      @RequestParam("docCode") String docCode,
      @RequestParam Map<String, String> allParams,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {

    // Make sure user has an ERP session (same pattern as other endpoints)
	WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response, false);
    if (sp == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"ok\":false,\"Reason\":\"NO_ERP_SESSION\"}");
      return;
    }

    // Remove required params from the pass-through param map
    java.util.Map<String, String> params = new java.util.HashMap<>(allParams);
    params.remove("docType");
    params.remove("docCode");

    try {
      
      byte[] pdf = sp.getDocument(docType, docCode);

      if (pdf == null || pdf.length == 0) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"ok\":false,\"Reason\":\"DOCUMENT_NOT_FOUND\"}");
        return;
      }

      // Stream the PDF
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType(MediaType.APPLICATION_PDF_VALUE);
      response.setHeader("Content-Disposition",
          "inline; filename=\"" + safePdfFilename(docType, docCode) + "\"");
      response.setContentLength(pdf.length);

      try (java.io.OutputStream os = response.getOutputStream()) {
        os.write(pdf);
        os.flush();
      }
    } catch (Exception ex) {
      LogUtil.log(ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"ok\":false,\"Reason\":\"GET_DOCUMENT_EXCEPTION\"}");
    }
  }

  @PostMapping(
	      value = "/saveprofile",
	      consumes = MediaType.APPLICATION_JSON_VALUE,
	      produces = MediaType.APPLICATION_JSON_VALUE
	  )
	  public ResponseEntity<String> saveProfile(
	      HttpServletRequest request,
	      HttpServletResponse response
	  ) {
	    // 1) Verify ERP session is logged in
	    WinecaveSessionHelper sp =
	        (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);

	    if (sp == null || !sp.isLogin()) {
	      String noLoginJson = "{\"ok\":false,\"Reason\":\"NOT_LOGGED_IN\"}";
	      return ResponseEntity
	          .status(HttpStatus.UNAUTHORIZED)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(noLoginJson);
	    }

	    try {
	      request.setCharacterEncoding("UTF-8");
	    } catch (Exception ignore) {
	      // ignore
	    }

	    // 2) Read JSON body from request
	    final String body;
	    try {
	      body = readBody(request);  // reuse your existing helper
	    } catch (IOException e) {
	      LogUtil.log(e);
	      String errJson = "{\"ok\":false,\"Reason\":\"BAD_REQUEST_BODY\"}";
	      return ResponseEntity
	          .status(HttpStatus.BAD_REQUEST)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }

	    try {
	      // 3) Call ERP: sp.createOrder(jsonString) – it returns JSON string (success / fail)
	      JSONObject result = sp.saveCustomerProfile(body);
	      // 4) Forward JSON directly to caller
	      return ResponseEntity
	          .ok()
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(result.toString());

	    } catch (Exception ex) {
	      LogUtil.log(ex);
	      String errJson = "{\"ok\":false,\"Reason\":\"CREATE_ORDER_EXCEPTION\"}";
	      return ResponseEntity
	          .status(HttpStatus.INTERNAL_SERVER_ERROR)
	          .contentType(MediaType.APPLICATION_JSON)
	          .body(errJson);
	    }
	  }
  private static String safePdfFilename(String docType, String docCode) {
    String base = (String.valueOf(docType) + "-" + String.valueOf(docCode))
        .replaceAll("[^a-zA-Z0-9._-]+", "_");
    if (base.length() > 120) base = base.substring(0, 120);
    return base + ".pdf";
  }

  @GetMapping(value = "/getshipinfo", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> erpGetShipInfo(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam Map<String, String> allParams
  ) {
	WinecaveSessionHelper sp = (WinecaveSessionHelper) WinecaveSessionHelper.getSessionHelper(request, response);
	if(!sp.isLogin()) {
		return ResponseEntity.ok(java.util.Map.of("ok", true , "loginid", "Not Logged In"));
	} else {
		JSONObject jo = null;
		synchronized(sp) {
			JSONObject args = new JSONObject(allParams); 
			jo = sp.getShipInfo(args);
		}
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jo.put("ok", true).toString()); 
        
	}
  }
}
