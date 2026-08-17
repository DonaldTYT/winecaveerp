package com.uniinformation.webcore.vincero;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.erpv4.Erpv4SessionHelper;

public class VinceroSessionHelper extends Erpv4SessionHelper {
	/*
	String fullName = null;
	int mrg;
	*/
	boolean isVinceroLogin = false;
	public boolean loginProceed_vincero(String p_loginid, String p_password) throws Exception{
			try {
				SelectUtil su = new SelectUtil(); 
				su.init(getJdbcPool());
				TableRec tr = su.getQueryResult("select * from subslogin where sblogin_loginid = ? and sblogin_enabled='Y'",new Wherecl().appendArgument(p_loginid));
				su.close();
				if(tr.getRecordCount() <= 0) return(false);
				tr.setRecPointer(0);
				if(tr.getFieldString("sblogin_password").equals(p_password)) {
					boolean ok = super.loginProceed(tr.getFieldString("sblogin_access"), "udx40192w");
					if(ok) {
						setVcode(p_loginid);
						/*
						fullName = tr.getFieldString("otlm_name");
						mrg = tr.getFieldIndex("otlm_rg");
						*/
						isVinceroLogin = true;
						return(ok);
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			return(false);
	}
	
	@Override 
	protected boolean isLoginDisabled(String p_loginid) {
		return(false);
	}
	
	@Override
	protected String getLoginIdByVcode(String p_vcode) {
			try {
				SelectUtil su = new SelectUtil(); 
				su.init(getJdbcPool());
				TableRec tr = su.getQueryResult("select * from subslogin where sblogin_loginid = ? and sblogin_enabled = 'Y'",new Wherecl().appendArgument(p_vcode));
				su.close();
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					/*
					fullName = tr.getFieldString("otlm_name");
					mrg = tr.getFieldIndex("otlm_rg");
					*/
					return(tr.getFieldString("sblogin_loginid"));
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		
		return(p_vcode);
	}
	
	/*
	@Override
	public Object getLoginProperty(PROPNAME p_propname) throws Exception {
		switch (p_propname) {
			case FULLNAME: if(fullName != null) return(fullName);
			case URG: return(mrg);
		}
		return(null);
	}
	*/
	
	@Override
	public String loginGetId(String p_loginStr) {
		return(p_loginStr);
	}
	@Override
	public String loginGetAgent(String p_loginStr) {
		return("");
	}
	@Override
	public void validateLogin(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception{
		if (!isLogin()) {
			String servletPath = buildServletPath(p_request, true);
			if (servletPath.length() > 0){
				if(servletPath.startsWith("/zkbiloader.html?zul=zkf/vincero/")) {
					p_response.sendRedirect("vincero_login.jsp");
					return;
				}
				String targetURLBase64 = (new org.apache.commons.codec.binary.Base64(true)).encodeBase64URLSafeString(servletPath.getBytes("UTF-8"));
				String agentTag = "";
				if (p_request.getParameter("agent") != null && !p_request.getParameter("agent").trim().equals("")){
					agentTag = "&agent=" + p_request.getParameter("agent").trim();
				}
				p_response.sendRedirect("login.html?targetURL=" + targetURLBase64 + agentTag);
			}
			else{
				p_response.sendRedirect("login.html");
			}	
		}
	}
	
	@Override
	public String getLandingPage(){
		if(isVinceroLogin) {
			return "./vincero_login.jsp";
		} else {
			return super.getLandingPage();
		}
	}	
	@Override
	protected String getSiteMapUrl(String rootMenu) {
		if(isVinceroLogin) {
			return "./vincero_login.jsp";
		} else {
			return(super.getSiteMapUrl(rootMenu));
		}
	}	
//	@Override
//	public final ReturnMsg login(HttpServletRequest p_request, HttpServletResponse p_response, String p_loginStr,String p_password) throws Exception {
//		String servletPath = buildServletPath(p_request, true);
//		return(super.login(p_request, p_response, p_loginStr,p_password));
//	}
}
