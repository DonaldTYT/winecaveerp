package com.uniinformation.rest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
//import org.glassfish.jersey.internal.util.Base64;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.BanIpHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

@Provider
public class AuthFilter implements javax.ws.rs.container.ContainerRequestFilter{

	@Context
	private ResourceInfo resourceInfo;

	@Context
	private HttpServletRequest request;
	
	public final static int maxInactiveInterval = 300;  //0: expire immediately. tomcat default 1800s. suggest 300s for ws.

	@Override
	public void filter(ContainerRequestContext requestContext){
		UniLog.log1("called [%s%s] [%s]", request.getRequestURL(), (request.getQueryString() == null ? "" : "?" + request.getQueryString()), request.getRemoteAddr());
		Method method = resourceInfo.getResourceMethod();
		
		//set session timeout
		if (request.getSession().getMaxInactiveInterval() != maxInactiveInterval) {
			UniLog.log1("change session timeout from %d to %d", request.getSession().getMaxInactiveInterval(), maxInactiveInterval);
			request.getSession().setMaxInactiveInterval(maxInactiveInterval);
		}

		//global validation
		if(method.isAnnotationPresent(DenyAll.class)){
			UniLog.log1("deny all");
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Access Denied - Deny for all").type(MediaType.TEXT_PLAIN).build());
			return;
		}
		if(method.isAnnotationPresent(PermitAll.class)){
			UniLog.log1("permit all");
			return;
		}
		
		
		/*
		//role based validation  e.g. add @RolesAllowed({"user","admin"})
		//andrew190916:check login status is good enough, so comment of this block of code
		if(!method.isAnnotationPresent(RolesAllowed.class)){
			UniLog.log1("no role allowed, bypass validation");
			return true;
		}
		RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
		Set<String> requiredRoles = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));
		*/

		//get credential
		Map credentialMap = getCredentialFromHeader(requestContext);
		if (!MapUtil.getBoolean(credentialMap,"status",false)){
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(credentialMap.get("errMsg")).type(MediaType.TEXT_PLAIN).build());
			return;
		}

		SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null, false);
		ReturnMsg rtnMsg = isUserAllowed(sh, MapUtil.getString(credentialMap,"loginId"), MapUtil.getString(credentialMap,"password"), request.getRemoteAddr());
		if(!rtnMsg.getStatus()){
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(rtnMsg.getMsg()).type(MediaType.TEXT_PLAIN).build());
			return;
		}
	}
	/***
	 * obtain loginId/password from header (basic auth)
	 * @param requestContext
	 * @return
	 */
	private static Map<String,Object> getCredentialFromHeader(ContainerRequestContext requestContext){
		try{
			//check authorization
			final MultivaluedMap<String, String> headers = requestContext.getHeaders();
			final List<String> authHeaderList = headers.get("Authorization");
			if(authHeaderList == null || authHeaderList.isEmpty()){
				UniLog.log1("header without authorization");
				return MapUtil.of("status", false, "errMsg", "Access Denied - No credentials provided");
			}

			//check basic auth
			String[] authHeaderParts = authHeaderList.get(0).split("\\s+");
			if (authHeaderParts.length <= 1 || !StringUtils.equals(authHeaderParts[0],"Basic")){
				UniLog.log1("non basic auth");
				return MapUtil.of("status", false, "errMsg", "Access Denied - Invalid Auth Type");
			}

			//extract loginId and password
			//String authString = new String(Base64.decode(authHeaderParts[1].getBytes()));  //jersey 2.25.1
            String authString = new String(java.util.Base64.getDecoder().decode(authHeaderParts[1].getBytes()));  //jersey 2.30.1
			String[] authStringParts = authString.split(":",2);  //password may contain colon
			String loginId = authStringParts[0];
			String password = authStringParts[1];

			if (StringUtils.isBlank(loginId) || StringUtils.isBlank(password)){
				UniLog.log1("loginId or password is blank");
				return MapUtil.of("status", false, "errMsg", "Access Denied - Invalid login id or password");
			}
			//UniLog.log1("authString:%s loginId:%s password:%s", authString, loginId, password);
			UniLog.log1("loginId:%s", loginId);
			return MapUtil.of("status",true,"loginId",loginId,"password",password);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return MapUtil.of("status",false,"errMsg","Internal Error");
		}
	}
	private static ReturnMsg isUserAllowed(SessionHelper sh, String loginId, String password, String ip){
		try{
			//check login/password against session
			if (sh.isLogin()) {
				if (!StringUtils.equals(loginId, sh.getLoginId()) || !StringUtils.equals(SessionHelper.calHash(password), sh.getPasswordHash())){
					UniLog.log1("credential changed. old[%s] new[%s]. trigger logout action", sh.getLoginId(), loginId);
					sh.logout();
				}
			}
			if (sh.isLogin()) {
				UniLog.log1("same credential");
				return ReturnMsg.defaultOk;
			}
			
			if (sh.getBadIpMaxFailCnt() > 0 && BanIpHelper.addIp(ip,0) >= sh.getBadIpMaxFailCnt()){
				return new ReturnMsg(false,"Access Denied - Too many login attempts.");
			}
			boolean loginStatus = sh.login(loginId, password).getStatus();
			UniLog.log1("login status:" + loginStatus);
			if (loginStatus){
				BanIpHelper.clearIp(ip);
				return(ReturnMsg.defaultOk);
			}
			else{
				BanIpHelper.addIp(ip,sh.getBanIpDur());
				return new ReturnMsg(false,"Access Denied - You cannot access this resource");
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			return new ReturnMsg(false,"Access Denied - Internal Error");
		}
	}
}