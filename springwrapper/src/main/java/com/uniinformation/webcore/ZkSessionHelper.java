package com.uniinformation.webcore;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.UniLog;

public class ZkSessionHelper extends SessionHelper {
	/***
	 * for dev or unit test only
	 * @param p_iniAgent
	 * @return
	 */
//	public static ZkSessionHelper getSessionHelperDummy(String p_iniAgent) {
//		return(getSessionHelperDummy(p_iniAgent,"dummy"));
//	}
//	public static ZkSessionHelper getSessionHelperDummy() {
//		return(getSessionHelperDummy(null,"dummy"));
//	}
//	public static ZkSessionHelper getSessionHelperDummy(String p_iniAgent,String p_loginid) {
//		return(getSessionHelperDummy(p_iniAgent,p_loginid,null));
//	}
	public static ZkSessionHelper getSessionHelperDummy(String p_iniAgent,String p_loginid,ServletContext p_svc) {
		return com.uniinformation.webcore.SessionHelper.getSessionHelperDummy(p_iniAgent, p_loginid, p_svc, () -> new ZkSessionHelper());
	}
	/***
	 * object sessionHelper from session
	 * @return
	 */
	public static ZkSessionHelper getSessionHelper() {
		/* should add code for Spring */
    	return (null);
	}

	synchronized public static SessionHelper getSessionHelper(HttpServletRequest p_request, HttpServletResponse p_response, boolean p_requireNew) {
		return getSessionHelper(p_request, p_response, p_requireNew, () -> new ZkSessionHelper());
	}
	public static SessionHelper getSessionHelper(HttpServletRequest p_request, HttpServletResponse p_response) {
		return(getSessionHelper(p_request, p_response, false));
	}
	@Override
	public String getURLParam(String p_key) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public InputStream openResourceAsStream(String p_path) {
		try {
			return(IniHelper.getResourceAsStream(getSvc(),p_path));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}
	@Override
	public String getWebContentRealPath(String p_path, boolean p_withSeparator) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void showMsg(String p_format, Object...p_args){
	}
	public static void showWarnMsg(String p_format, Object...p_args){
	}
	public static void showErrMsg(String p_format, Object...p_args){
	}
}
