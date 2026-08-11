package com.uniinformation.webcore;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import com.uniinformation.rpccall.RpcServer;
import com.uniinformation.utils.UniLog;

public class WebRpcServer extends RpcServer {
	public static volatile WebRpcServer currentWebRpcServer = null;
	static volatile SessionHelper sh;
	private final Thread serverThread;

	public WebRpcServer(int p_port,InputStream p_is) {
		super(p_port);
		Thread newServerThread = null;
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
			// Bind before publishing/starting the server so a port conflict fails
			// synchronously and does not leave a retry thread behind.
			init();
			newServerThread = new Thread(this, "WebRpcServer-" + p_port);
			newServerThread.setDaemon(true);
			newServerThread.start();
		} catch (Exception ex) {
			UniLog.log(ex);
			// If binding succeeded but a later startup step failed, make sure the
			// listener is not left behind.
			stop();
		} finally {
			if (lnr != null) {
				try { lnr.close(); } catch (Exception ex1) {};
			}
		}
		serverThread = newServerThread;
	}

	public static void stoptWebRpcServer() {
		stopWebRpcServer(10000L);
	}

	/**
	 * Stop the listener, wait for its thread to terminate, and release all
	 * static references owned by this web application.
	 */
	public static boolean stopWebRpcServer(long p_timeoutMs) {
		WebRpcServer server;
		synchronized (WebRpcServer.class) {
			server = currentWebRpcServer;
		}
		if (server == null) {
			UniLog.log1("no webrpcserver");
			return true;
		}

		UniLog.log1("stop webrpcserver");
		server.stop();
		server.interruptServerThread();
		boolean stopped = server.awaitTermination(p_timeoutMs);
		if (!stopped) {
			UniLog.log1("webrpcserver stop timeout. thread:%s state:%s",
				server.getServerThreadName(), server.getServerThreadState());
			return false;
		}

		SessionHelper sessionHelperToClean = null;
		synchronized (WebRpcServer.class) {
			if (currentWebRpcServer == server) {
				currentWebRpcServer = null;
				sessionHelperToClean = sh;
				sh = null;
			}
		}
		if (sessionHelperToClean != null) {
			try {
				sessionHelperToClean.cleanSessionData();
			}
			catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		UniLog.log1("webrpcserver stopped");
		return true;
	}

	public static boolean checkWebRpcServer(){
		WebRpcServer server = currentWebRpcServer;
		return server != null && server.isServerThreadAlive();
	}
	public static void startWebRpcServer(int p_port,InputStream p_classlist) {
		startWebRpcServer(p_port,p_classlist,null);
	}
	public static synchronized void startWebRpcServer(int p_port,InputStream p_classlist,SessionHelper p_sh)
	{
		if(currentWebRpcServer != null) {
			if (currentWebRpcServer.isServerThreadAlive()) {
				UniLog.log("WebRpcServer service already started, ignored");
				return;
			}
			currentWebRpcServer = null;
			sh = null;
		}
		sh = p_sh;
		UniLog.log("WebRpcServer service startup " + p_port);
		WebRpcServer server = new WebRpcServer(p_port,p_classlist);
		if (server.serverThread != null) {
			currentWebRpcServer = server;
		} else {
			sh = null;
			UniLog.log1("WebRpcServer service failed to start");
		}
	}

	private void interruptServerThread() {
		if (serverThread != null) {
			serverThread.interrupt();
		}
	}

	private boolean awaitTermination(long p_timeoutMs) {
		if (serverThread == null) {
			return true;
		}
		long timeoutMs = Math.max(0L, p_timeoutMs);
		try {
			if (timeoutMs > 0L) {
				serverThread.join(timeoutMs);
			}
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			UniLog.log1("interrupted while waiting for webrpcserver shutdown");
		}
		return !serverThread.isAlive();
	}

	private boolean isServerThreadAlive() {
		return serverThread != null && serverThread.isAlive();
	}

	private String getServerThreadName() {
		return serverThread == null ? "null" : serverThread.getName();
	}

	private Thread.State getServerThreadState() {
		return serverThread == null ? Thread.State.TERMINATED : serverThread.getState();
	}
	public static SessionHelper getSessionHelper() {
		return(sh);
	}
}
