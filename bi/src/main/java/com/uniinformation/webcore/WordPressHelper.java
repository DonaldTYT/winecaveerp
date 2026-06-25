package com.uniinformation.webcore;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
//import org.eclipse.birt.report.model.api.util.StringUtil;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
//import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.logging.LoggingFeature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
/***
 * For Wordpress REST API integration (client side)
 * - Not thread safe
 * @author Andrew
 *
 */
public class WordPressHelper {
	private boolean fDebug = false;
	//private boolean fDebug = true;
	public static final int DEFAULT_CONNECT_TIMEOUT = 5000;  //5s
	public static final int DEFAULT_READ_TIMEOUT = 60000;  //60s
	public static final int DEFAULT_LONG_CONNECT_TIMEOUT = 60000;  //60s
	public static final int DEFAULT_LONG_READ_TIMEOUT = 3600000;  //1h
	
	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private int readTimeout = DEFAULT_READ_TIMEOUT;
	
	private final String basicAuthLoginId;
	private final String basicAuthPassword;
	private final String serverURL;
	
	private BidiMap userMap = null;
	
	private static Lock triggerSyncLock = new ReentrantLock();
	
	protected static Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.setDateFormat("yyyy/MM/dd HH:mm:ss")
			.create();
			
			
	public WordPressHelper(String p_url, String p_login, String p_password) {
		serverURL = p_url;
		basicAuthLoginId = p_login;
		basicAuthPassword = p_password;
	}
	public WordPressHelper(SessionHelper p_sh) {
		this(p_sh.getWPBaseURL(),p_sh.getWPLogin(),p_sh.getWPPassword());
	}
	
	/***
	 * list wordpress user
	 * @return json array 
	 */
	public JsonArray getUserJA() {
		try {
			//Map resMap = call("/wp-json/wp/v2/users","GET",Arrays.asList(Pair.of("orderby","id"), Pair.of("context","edit"))); //andrew210218: default can list 10 user only
			Map resMap = call("/wp-json/wp/v2/users","GET",Arrays.asList(Pair.of("orderby","id"), Pair.of("context","edit"), Pair.of("per_page", "100"), Pair.of("page", "1"))); //TODO show more than 100 user. probably need to join pages result or patch wordpress code
			String entity = MapUtil.getString(resMap, "entity");
			JsonArray resultJA = new JsonParser().parse(entity).getAsJsonArray();
			return resultJA;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/***
	 * create user, called by update user
	 * @param p_login
	 * @param p_email
	 * @param p_password
	 * @return
	 */
	private ReturnMsg createUser(String p_login, String p_email, String p_password) {
		try {
			UniLog.log1("login:%s", p_login);
			if (StringUtils.isBlank(p_login)) {
				return new ReturnMsg(false,"login is blank");
			}
			if (StringUtils.isBlank(p_email)) {
				return new ReturnMsg(false,"email is blank");
			}
			if (StringUtils.isBlank(p_password)) {
				return new ReturnMsg(false,"password is blank");
			}
			Map resMap = call("/wp-json/wp/v2/users","POST",Arrays.asList(Pair.of("username",p_login),Pair.of("email",p_email), Pair.of("password", p_password)));
			String entity = MapUtil.getString(resMap, "entity");
			UniLog.log1("result:" + entity);
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			if (fDebug) {
				ex.printStackTrace();
			}
			return new ReturnMsg(ex);
		}
	}
	public boolean isSpecialAccount(String p_loginid) {
		return StringUtils.equalsAnyIgnoreCase(p_loginid, "client", "webdev", "wchung");
		
	}
	public ReturnMsg deleteUser(String p_login) {
		try {
			UniLog.log1("login:%s", p_login);
			if (isSpecialAccount(p_login)) {
				return new ReturnMsg(false,"Cannot delete special account");
			}
			int userId = getUserId(p_login);
			
			//if user not exist, try to create user
			if (userId <= 0) {
				//return new ReturnMsg(false,"User does not exist");
				return new ReturnMsg(true,"User does not exist"); 
			}
			ArrayList<Pair<String,String>> params = new ArrayList<Pair<String,String>>();
			params.add(Pair.of("force","true"));
			params.add(Pair.of("reassign","-1"));
			Map resMap = call("/wp-json/wp/v2/users/" + userId, "DELETE", params);
			String entity = MapUtil.getString(resMap, "entity");
			UniLog.log1("result:" + entity);
			
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			if (fDebug) {
				ex.printStackTrace();
			}
			/*
			if (ex.getMessage() != null && ex.getMessage().matches("res"))
			if (ex.getMessage().)
			*/
			return ReturnMsg.defaultFail;
		}

	}
	
	
	/***
	 * build usermap for userId/login
	 * @throws Exception
	 */
	private void buildUserMap(boolean p_refresh) throws Exception{
		if (p_refresh) {
			userMap = null;
		}
		if (userMap != null) {
			return;
		}
		JsonArray userJA = getUserJA();
		if (userJA == null) {
			throw new Exception("unable to obtain user list");
		}
		userMap = new TreeBidiMap();
		for (JsonObject jo : (List<JsonObject>)IteratorUtils.toList(userJA.iterator())){
			userMap.put(jo.get("id").getAsInt(), jo.get("username").getAsString());
			UniLog.log1("buildmap %d:%s", jo.get("id").getAsInt(), jo.get("username").getAsString());
		}
	}
	private void buildUserMap() throws Exception{
		buildUserMap(false);
	}
	
	/***
	 * get userId by login
	 * @param p_login
	 * @return -1 if not exist
	 * @throws Exception
	 */
	private int getUserId(String p_login) throws Exception{
		if (StringUtils.isBlank(p_login)) {
			throw new Exception("LoginId is blank");
		}
		buildUserMap();
		Integer userId = (Integer) userMap.getKey(p_login);
		if (userId == null) {
			return -1;
		}
		return userId;
	}
	/***
	 * get login by userId
	 * @param p_userId
	 * @return
	 * @throws Exception
	 */
	private String getLoginId(int p_userId) throws Exception{
		if (p_userId <= 0) {
			throw new Exception("invalid user id");
		}
		buildUserMap();
		String login = (String) userMap.get(p_userId);
		if (login == null) {
			throw new Exception(String.format("userId %d not found", p_userId));
		}
		return login;
	}
	
	/***
	 * 
	 * @param p_login - target login id
	 * @param p_email - new email, ignore if blank
	 * @param p_password - new password, ignore if blank
	 * @param p_allowCreate - create if not exist
	 * @return ok/fail
	 */
	public ReturnMsg updateUser(String p_login, String p_email, String p_password, boolean p_allowCreate) {
		try {
			UniLog.log1("login:%s", p_login);
			int userId = getUserId(p_login);
			
			//if user not exist, try to create user
			if (userId <= 0 && p_allowCreate) {
				return createUser(p_login, p_email, p_password);
			}
			
			//update user
			ArrayList<Pair<String,String>> params = new ArrayList<Pair<String,String>>();
			if (StringUtils.isNotBlank(p_email)) {
				params.add(Pair.of("email",p_email.trim()));
			}
			if (StringUtils.isNotBlank(p_password)) {
				params.add(Pair.of("password",p_password.trim()));
			}
			if (params.size() <= 0) {
				UniLog.log1("no changes");
				return ReturnMsg.defaultOk;
			}
			
			Map resMap = call("/wp-json/wp/v2/users/" + userId, "POST", params);
			String entity = MapUtil.getString(resMap, "entity");
			UniLog.log1("result:" + entity);
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return ReturnMsg.defaultFail;
		}

	}
	public ReturnMsg updateUser(String p_login, String p_email, String p_password) {
		return updateUser(p_login, p_email, p_password, true);
	}
	
	/***
	 * user jersey to call wordpress server
	 * @param p_resource
	 * @param p_action
	 * @param p_params 
	 * @return
	 * @throws Exception
	 */
	private Map call(String p_resource, String p_action, List<Pair<String,String>> p_params) throws Exception{
		UniLog.log1("res:%s action:%s", p_resource, p_action);
		if (StringUtils.isBlank(serverURL) || StringUtils.isBlank(basicAuthLoginId) || StringUtils.isBlank(basicAuthPassword)) {
			throw new Exception("server url or credential is blank");
		}
		
		Client client = ClientBuilder.newClient();
		
		//set timeout
		if (connectTimeout > 0) {
			client.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout);
		}
		if (readTimeout > 0) {
			client.property(ClientProperties.READ_TIMEOUT, readTimeout);
		}
		
		//set basic auth
		client.register(HttpAuthenticationFeature.basic(basicAuthLoginId, basicAuthPassword));
		
		//set debug
		if (fDebug) {
			client.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, LoggingFeature.Verbosity.PAYLOAD_ANY);
			client.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, "INFO");
		}
		
		WebTarget webTarget = client.target(serverURL).path(p_resource);
		
		//GET command
		Response response = null;
		if ("GET".equalsIgnoreCase(p_action)) {
			if (p_params != null) { //this block of code not yet test
				for (int i=0; i<p_params.size(); i++) {
					//UniLog.log1("add param: %s=%s", p_params.get(i), p_params.get(i+1));
					webTarget = webTarget.queryParam(p_params.get(i).getLeft(), p_params.get(i).getRight());
				}
			}
			response = webTarget.request().get();

		}
		//POST command
		else if ("POST".equalsIgnoreCase(p_action)) {
			if (p_params != null) {
				MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
				for (int i=0; i<p_params.size(); i++) {
					//UniLog.log1("add param: %s=%s", p_params.get(i), p_params.get(i+1));
					formData.add(p_params.get(i).getLeft(), p_params.get(i).getRight());
				}
				response = webTarget.request().post(Entity.form(formData));
			}
			else {
				response = webTarget.request().post(null);
			}
		}
		else if ("DELETE".equalsIgnoreCase(p_action)) {
			if (p_params != null) {
				for (int i=0; i<p_params.size(); i++) {
					//UniLog.log1("add param: %s=%s", p_params.get(i), p_params.get(i+1));
					webTarget = webTarget.queryParam(p_params.get(i).getLeft(), p_params.get(i).getRight());
				}
			}
			response = webTarget.request().delete();
		}
		
		else {
			throw new Exception("action not supported");
		}
		
		//check response status
		if (response.getStatus() != 200 && response.getStatus() != 201) {
			
			throw new Exception("call failure. status:" + response.getStatus() + " entity:" +response.readEntity(String.class));
		}
		//UniLog.log1("call ok. status:" + response.getStatus());

		return(MapUtil.of(
				"response", response, 
				"status", response.getStatus(),
				"entity", response.readEntity(String.class)
				));
	}
	public void setDebug(boolean p_fDebug) {
		fDebug = p_fDebug;
	}
	public int setConnectTimeout(int p_connectTimeout) {
		int oldValue = connectTimeout;
		connectTimeout = p_connectTimeout;
		return oldValue;
	}
	public int setReadTimeout(int p_readTimeout) {
		int oldValue = readTimeout;
		readTimeout = p_readTimeout;
		return oldValue;
	}
	/***
	 * get order list (woocommerce)
	 * @return
	 */
	public JsonArray listOrders() {
		//TODO add query condition
		try {
			Map resMap = call("/wp-json/wc/v3/orders","GET", Arrays.asList(Pair.of("orderby","id"), Pair.of("order","asc")));
			String entity = MapUtil.getString(resMap, "entity");
			JsonArray ja = new JsonParser().parse(entity).getAsJsonArray();
			//UniLog.log1("json:" + gson.toJson(ja));
			
			for (JsonObject jo : (List<JsonObject>)IteratorUtils.toList(ja.iterator())){
				UniLog.log1("output: %d,%d,%s,%s,%s", 
						jo.get("id").getAsInt(), 
						jo.get("customer_id").getAsInt(), 
						jo.get("customer_id").getAsInt() == 0 ? "guest" : getLoginId(jo.get("customer_id").getAsInt()), 
						jo.get("total").getAsString(), 
						jo.get("date_created_gmt").getAsString());
			}
			return ja;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	public ReturnMsg triggerSync() {
		//url sample: http://localhost:8000/wineac/wp-json/wineac/v1/trigger_sync
		if (triggerSyncLock.tryLock()) {
			try {
				UniLog.log1("called");
				setConnectTimeout(DEFAULT_LONG_CONNECT_TIMEOUT);
				setReadTimeout(DEFAULT_LONG_READ_TIMEOUT);
				Map resMap = call("/wp-json/wineac/v1/trigger_sync","GET", null);
				setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
				setReadTimeout(DEFAULT_READ_TIMEOUT);

				String entity = MapUtil.getString(resMap, "entity");
				JsonObject json = new JsonParser().parse(entity).getAsJsonObject();
				UniLog.log1("json:" + json);
				if (json.get("status").getAsBoolean()) {
					return ReturnMsg.defaultOk;
				}
				else {
					return ReturnMsg.defaultFail;
				}
			}
			catch(Exception ex) {
				if (fDebug) {
					ex.printStackTrace();
				}
				return new ReturnMsg(ex);
			}
			finally{
				triggerSyncLock.unlock();
			}
		}
		else {
			return new ReturnMsg(false,"already running. action abort");
		}
	}
		
	public static void main(String args[]) throws Exception{
		ReturnMsg rtnMsg;
		JsonArray userJA;
		WordPressHelper wpHelper = new WordPressHelper("http://localhost:8000/wineac" ,"client","client2020##");
		
		//create or update user
		//rtnMsg = wpHelper.updateUser("test3", "test3.4@hellovoice.com", "xxx");
		//UniLog.log1("create user return %s", rtnMsg);
		
		//delete user
		//rtnMsg = wpHelper.deleteUser("test4");
		//UniLog.log1("delete user return %s", rtnMsg);
		
		//list user
		//userJA = wpHelper.getUserJA();
		//if (userJA != null) {
		//	for (JsonObject jo : (List<JsonObject>)IteratorUtils.toList(userJA.iterator())){
		//		UniLog.log1("output: %d,%s,%s", jo.get("id").getAsInt(), jo.get("username").getAsString(), jo.get("email").getAsString());
		//	}
		//}
		
		//build user map
		wpHelper.buildUserMap();
		
		//trigger sync
		//UniLog.log1("triggerSync:%s", wpHelper.triggerSync());
		
		
		//wpHelper.listOrders();
	}
}