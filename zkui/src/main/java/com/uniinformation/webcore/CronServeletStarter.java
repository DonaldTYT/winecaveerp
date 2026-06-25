package com.uniinformation.webcore;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.cron.CronServer;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;

public class CronServeletStarter extends HttpServlet  {
	
	private void startCronClass(IniHelper ini,String cronLoginId,String iniAgent) {
			String[] cronLoginIdArr = StringUtils.split(cronLoginId, ",;:");
			String cronClass = ini.getString("cronClass");  //support multiple cron class
			if (StringUtils.isBlank(cronClass)) {
				UniLog.log1("cronClass is blank. do not start cron server");
				return;
			}
			UniLog.log1("cronClass=%s", cronClass);
			String[] cronClassArr = StringUtils.split(cronClass, ",;:");
			for (int i=0; i<cronClassArr.length; i++) {
				try {
					String curClassName = cronClassArr[i].trim();
					String curLoginId = i < cronLoginIdArr.length ? cronLoginIdArr[i] : cronLoginIdArr[0];
					UniLog.log1("start cron server. agent:%s class:%s login:%s", iniAgent, curClassName, curLoginId);
					if (!StringUtils.isBlank(cronClassArr[i])) {
						CronServer.startCronServer(iniAgent,curClassName,cronLoginId,getServletContext());
					}
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
	}
	private void startCronRpcServer(IniHelper ini,String cronLoginId,String iniAgent) {
			String rpcPortStr = ini.getString("cronRpcPort"); 
			if(rpcPortStr == null || rpcPortStr.trim().equals("")) return;
			int rpcPort = Integer.parseInt(rpcPortStr.trim());
			if(rpcPort <= 0 || rpcPort > 65535) {
				UniLog.log1("port out of range. do not start webrpc");
				return;
			}
			InputStream is = null;
			String classListName = ini.getString("rpcClassList", "webrpcserver.classlist");
			/*
			Thread.currentThread().getContextClassLoader().getResourceAsStream(classListName);
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("webrpcserver.classlist"+"."+iniAgent);
			if(is == null) {
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream("webrpcserver.classlist");
			}
			*/
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classListName);
			if (is == null) {
				UniLog.log1("no webrpcserver.classlist");
				return;
			}
			SessionHelper sessionHelper = ZkSessionHelper.getSessionHelperDummy(iniAgent,cronLoginId,null);
			if(sessionHelper != null) {
				WebRpcServer.startWebRpcServer(rpcPort, is,sessionHelper);
			}
		
	}
	
	@Override
	public void init() throws ServletException {
		try {
			IniHelper ini = SessionHelper.getIniHelper();
			if (ini == null) {
				UniLog.log1("ini is null");
				return;
			}
			String iniAgent = ini.getAgent();
			if (StringUtils.isBlank(iniAgent)) {
				UniLog.log1("iniAgent is blank");
				return;
			}
			
			String cronLoginId = ini.getString("cronLoginId"); //support multiple cron loginId
			if (StringUtils.isBlank(cronLoginId)) {
				UniLog.log1("cronLoginId is blank. do not start cron server");
				return;
			}
			startCronClass(ini,cronLoginId,iniAgent);
//			String[] cronLoginIdArr = StringUtils.split(cronLoginId, ",;:");
//			
//			String cronClass = ini.getString("cronClass");  //support multiple cron class
//			if (StringUtils.isBlank(cronClass)) {
//				UniLog.log1("cronClass is blank. do not start cron server");
//				return;
//			}
//			UniLog.log1("cronClass=%s", cronClass);
//			String[] cronClassArr = StringUtils.split(cronClass, ",;:");
//			for (int i=0; i<cronClassArr.length; i++) {
//				try {
//					String curClassName = cronClassArr[i].trim();
//					String curLoginId = i < cronLoginIdArr.length ? cronLoginIdArr[i] : cronLoginIdArr[0];
//					UniLog.log1("start cron server. agent:%s class:%s login:%s", iniAgent, curClassName, curLoginId);
//					if (!StringUtils.isBlank(cronClassArr[i])) {
//						CronServer.startCronServer(iniAgent,curClassName,cronLoginId);
//					}
//				}
//				catch(Exception ex) {
//					ex.printStackTrace();
//				}
//			}
			
			startCronRpcServer(ini,cronLoginId,iniAgent);

//			String rpcPortStr = ini.getString("cronRpcPort"); 
//			if(rpcPortStr == null || rpcPortStr.trim().equals("")) return;
//			int rpcPort = Integer.parseInt(rpcPortStr.trim());
//			if(rpcPort <= 0 || rpcPort > 65535) {
//				UniLog.log1("port out of range. do not start webrpc");
//				return;
//			}
//			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("webrpcserver.classlist");
//			if (is == null) {
//				UniLog.log1("no webrpcserver.classlist");
//				return;
//			}
//			SessionHelper sessionHelper = SessionHelper.getSessionHelperDummy(iniAgent,cronLoginId);
//			if(sessionHelper != null) {
//				WebRpcServer.startWebRpcServer(rpcPort, is,sessionHelper);
//			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		UniLog.log1("called");
		CronServer.stop(60000);
		super.destroy();
	}
}
