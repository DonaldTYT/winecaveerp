package com.uniinformation.webcore;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import com.uniinformation.rpccall.RpcServer;
import com.uniinformation.utils.UniLog;

public class WebRpcServer extends RpcServer {
	public static WebRpcServer currentWebRpcServer = null;
	static SessionHelper sh;
	public WebRpcServer(int p_port,InputStream p_is) {
		super(p_port);
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(p_is));
		try {
			for (;;) {
				String line = lnr.readLine();
				if (line == null)
					break;
				if (line.startsWith("#"))
					UniLog.logClass(this, "Skipping "+line+" ...");
				else {
					UniLog.logClass(this, "Loading "+line+" ...");
					StringTokenizer strtoken = new StringTokenizer(line);
					if(strtoken.countTokens() == 1) {
						try {
							Class.forName(line);
							registerService(line, false, true);
						} catch (Exception ex) {
							UniLog.log(ex);
						}
					} else {
						if(strtoken.countTokens() == 2) {
							String s;
							int maxstore;
							s = strtoken.nextToken();
							maxstore = Integer.parseInt(strtoken.nextToken());
							try {
								Class.forName(s);
								registerService(s, false, true, maxstore);
							} catch (Exception ex) {
								UniLog.log(ex);
							}
						} else {
							UniLog.logClass(this, "RpcSeriver Syntax Error " + line);
						}
					}
				}
			}
			Thread servthread = new Thread(this);
			servthread.setDaemon(true);
			servthread.start();
		} catch (Exception ex) {
			UniLog.log(ex);
		} finally {
			if (lnr != null) {
				try { lnr.close(); } catch (Exception ex1) {};
			}
		}
	}
	public static void stoptWebRpcServer() {
		if (currentWebRpcServer != null) {
			UniLog.log1("stop webrpcserver");
			currentWebRpcServer.stop();
		}
		else {
			UniLog.log1("no webrpcserver");
		}
	}
	public static boolean checkWebRpcServer(){
		if (currentWebRpcServer == null) {
			return false;
		}
		return currentWebRpcServer.isRunning();
	}
	public static void startWebRpcServer(int p_port,InputStream p_classlist) {
		startWebRpcServer(p_port,p_classlist,null);
	}
	public static void startWebRpcServer(int p_port,InputStream p_classlist,SessionHelper p_sh)
	{
		if(currentWebRpcServer != null) {
			UniLog.log("WebRpcServer service already started, ignored");
		} else {
			sh = p_sh;
			UniLog.log("WebRpcServer service startup " + p_port);
			currentWebRpcServer = new WebRpcServer(p_port,p_classlist);
		}
	}
	public static SessionHelper getSessionHelper() {
		return(sh);
	}
}
