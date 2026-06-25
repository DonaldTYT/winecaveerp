package com.uniinformation.webcore;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uniinformation.utils.UniLog;

public class WebRpcStarter extends HttpServlet {
	@Override
	public void init() throws ServletException {
		UniLog.log1("called");
		try {
			InputStream is;
			Properties prop = new Properties();
			UniLog.log("WebRpcStarter Loading properties file from contextpath");
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("webrpcserver.properties");
			if(is == null) {
				UniLog.log1("no webrpcserver.properties");
				return;
			}
			prop.load(is);
			//prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("webrpcserver.properties"));
			UniLog.log("WebRpcStarter load from contextpath OK " + prop);
			String s = prop.getProperty("WebRpcServerPort");
			UniLog.log("WebRpcStarter port = " + s);
			if(s == null || s.equals("")) {
				UniLog.log1("port is blank. do not start webrpc");
				return;
			}
			int port = Integer.parseInt(s);
			if(port <= 0 || port > 65535) {
				UniLog.log1("port out of range. do not start webrpc");
				return;
			}
			String ss = prop.getProperty("NewRpcServer");
			if(ss.equals("Y")) {
				is.close();
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream("webrpcserver.classlist");
			} else {
				is.close();
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream("webrpcserver.classlist");
			}
			if (is == null) {
				UniLog.log1("no webrpcserver.classlist");
				return;
			}
			if(ss.equals("Y")) {
				WebRpcServer.startWebRpcServer(port, is);
			} else {
				WebRpcServer.startWebRpcServer(port, is);
			}
		} 
		catch (Exception  ex) {
			UniLog.log("WebRpcStarter load from contextpath failed");
		}
	}
	@Override
	public void destroy() {
		UniLog.log1("called");
		
		int timeRemain = 10000;
		WebRpcServer.stoptWebRpcServer();
		try {
			while(timeRemain > 0 && WebRpcServer.checkWebRpcServer()) {
				UniLog.log1("waiting for WebRpcServer end. timeRemain:%d", timeRemain);
				Thread.sleep(1000);
				timeRemain-=1000;
			}
		}
		catch(InterruptedException ex) {
			ex.printStackTrace();
			UniLog.log1("got interrupt");
		}
	}
}
