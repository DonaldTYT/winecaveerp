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
			IniHelper serverIni = SessionHelper.getIniHelper();
			if (serverIni == null) {
				UniLog.log1("ini is null");
				return;
			}
			String serverAgent = serverIni.getAgent();
			if (StringUtils.isBlank(serverAgent)) {
				UniLog.log1("iniAgent is blank");
				return;
			}

			/*
			 * Cron work may belong to a shell/orchestrator agent while this webapp's
			 * default agent is an ERP data agent.  RPC startup remains bound to the
			 * default agent; cronAgent changes only the CronServer session.
			 */
			String cronAgent = StringUtils.defaultIfBlank(serverIni.getString("cronAgent"), serverAgent).trim();
			IniHelper cronIni = StringUtils.equals(cronAgent, serverAgent)
					? serverIni : SessionHelper.getIniHelper(cronAgent);
			if(cronIni == null) {
				UniLog.log1("cannot load cron ini for agent:%s", cronAgent);
				return;
			}

			String cronLoginId = cronIni.getString("cronLoginId"); //support multiple cron loginId
			if (StringUtils.isBlank(cronLoginId)) {
				UniLog.log1("cronLoginId is blank. do not start cron server");
				return;
			}
			startCronClass(cronIni,cronLoginId,cronAgent);
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
			
			startCronRpcServer(serverIni,cronLoginId,serverAgent);

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
		try {
			if (!WebRpcServer.stopWebRpcServer(15000L)) {
				UniLog.log1("cron RpcServer did not stop within timeout");
			}
		}
		catch (Exception ex) {
			UniLog.log(ex);
		}
		try {
			CronServer.stop(60000);
		}
		catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			super.destroy();
		}
	}
}
