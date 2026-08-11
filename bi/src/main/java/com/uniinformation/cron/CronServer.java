package com.uniinformation.cron;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.StopWatchHelper;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class CronServer implements Runnable{
	static Hashtable<String,CronServer>agentHash;
	//static int pollTime = 20000;
	static int startupDelayTime = 20000;
	SessionHelper sessionHelper;
	String agent;
	CronJob cronJob;
	String cronLoginId = null;
	AtomicBoolean fStop = new AtomicBoolean(false);
    Thread servthread = null;
    static AtomicInteger threadId = new AtomicInteger(0);
    ServletContext svc;
	public static synchronized void startCronServer(String p_agent,String p_class,String p_cronLoginId,ServletContext p_svc) throws Exception
	{
		if (StringUtils.isAnyBlank(p_agent,p_class,p_cronLoginId)){
			UniLog.log1("missing madatory param (%s,%s,%s). action abort",p_agent,p_class,p_cronLoginId);
			return;
		}
		if(agentHash == null) {
			UniLog.log("CronServer Server Init");
			agentHash = new Hashtable();
		}
		addAgentServer(p_agent,p_class,p_cronLoginId,p_svc);
	}
	
	static public void addAgentServer(String p_agent,String p_class,String p_cronLoginId,ServletContext p_svc) throws Exception {
		if(agentHash == null) {
			UniLog.log("CronServer add agent failed : CronServer not Initialized");
			return;
		}
		synchronized(agentHash) {
			if(agentHash.get(p_agent+":"+p_class) != null) return;
			UniLog.log("CronServer add agent " + p_agent+":"+p_class);
			agentHash.put(p_agent+":"+p_class, new CronServer(p_agent,p_class,p_cronLoginId,p_svc));
		}
	}
	
	public CronServer(String p_agent,String p_class,String p_cronLoginId,ServletContext p_svc) throws Exception {
		   agent = p_agent;
		   svc = p_svc;
		   cronLoginId = p_cronLoginId;
		   Class cjc = Class.forName(p_class);
		   Object cj = cjc.newInstance();
		   cronJob = (CronJob) cj;
		   servthread = new Thread(this);
		   servthread.setDaemon(true);
		   servthread.setName(p_class.replaceAll("^.*\\.","") +"-"+ threadId.addAndGet(1));
		   servthread.start();
	}

	@Override
	public void run() {
		try {
			int pt = cronJob.getPollTime();  //240215 obtain polltime from cronjob
			UniLog.log1("cronserver (startupdelay:%d) (polltime:%d)", startupDelayTime, pt);
			Thread.sleep(startupDelayTime);
			UniLog.log1("start");
			sessionHelper = ZkSessionHelper.getSessionHelperDummy(agent,cronLoginId,svc);
			cronJob.setSessionHelper(sessionHelper);
			cronJob.start();
			for(;;) {
				try {
					UniLog.log1("wakeup agent:%s", agent);
					cronJob.runOnce();
					//UniLog.log1("complete agent:%s", agent);
				} 
				catch (InterruptedException exi) {
					UniLog.log1("got interrupt");
					if (fStop.get()) {
						UniLog.log1("end");
						break;
					}
				}
				catch (Exception ex) {
					UniLog.log1("ERROR:"+ ex.getMessage());
				}
				Thread.sleep(pt);
			}
		} 
		catch (Exception ex) {
			UniLog.log("cronserver cronjob start failed. ex:" + ex.getMessage());
		}
	}
	/***
	 * stop all thread
	 * call from servlet CronServletStarter.destory()
	 */
	public static void stop(long p_timeout) {
		long timeout = p_timeout < 60000 ? 60000 : p_timeout;
		UniLog.log1("called: timeout:%d", timeout);

		List<CronServer> servers;
		Hashtable<String,CronServer> currentAgentHash;
		synchronized(CronServer.class) {
			currentAgentHash = agentHash;
			if(currentAgentHash == null) return;
			synchronized(currentAgentHash) {
				servers = new ArrayList<CronServer>(currentAgentHash.values());
			}
		}

		// Do not hold the registry lock while calling application stop methods.
		// A slow or synchronized CronJob.stop() must not prevent the other jobs
		// from receiving their shutdown signal.
		for(CronServer server : servers) {
			server.fStop.set(true);
			try {
				UniLog.log1("stop %s:%s", server.agent,
					server.cronJob == null ? "null" : server.cronJob.getClass().getName());
				if(server.cronJob != null) server.cronJob.stop();
			}
			catch(Exception ex) {
				UniLog.log(ex);
			}
			finally {
				// Always interrupt the outer poll/sleep thread, even when stop() fails.
				if(server.servthread != null) server.servthread.interrupt();
			}
		}

		long expireTime = System.currentTimeMillis() + timeout;
		for(CronServer server : servers) {
			Thread thread = server.servthread;
			if(thread == null || thread == Thread.currentThread()) continue;
			long waitMs = Math.max(0L, expireTime - System.currentTimeMillis());
			if(waitMs > 0L) {
				try {
					thread.join(waitMs);
				}
				catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			if(thread.isAlive()) {
				UniLog.log1("cron:%s still alive state:%s", server.agent, thread.getState());
			}
		}

		for(CronServer server : servers) {
			Thread thread = server.servthread;
			if(thread != null && thread.isAlive()) continue;
			if(server.sessionHelper != null) {
				try {
					server.sessionHelper.cleanSessionData();
				}
				catch(Exception ex) {
					UniLog.log(ex);
				}
			}
			server.sessionHelper = null;
			server.cronJob = null;
			server.svc = null;
		}

		synchronized(CronServer.class) {
			if(agentHash == currentAgentHash) {
				synchronized(currentAgentHash) {
					currentAgentHash.clear();
				}
				agentHash = null;
			}
		}
		UniLog.log1("cron shutdown complete");
	}
}
