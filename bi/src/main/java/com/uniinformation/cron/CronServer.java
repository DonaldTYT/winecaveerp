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
		
		//request to stop all thread
		if(agentHash == null) {
			return;
		}
		synchronized(agentHash) {
			for (String key : agentHash.keySet()) {
				try {
					UniLog.log1("stop %s",key);
					agentHash.get(key).fStop.set(true);
					agentHash.get(key).cronJob.stop();
					agentHash.get(key).servthread.interrupt();
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//wait for all thread end. 
		long expireTime = new Date().getTime() + timeout;
		synchronized(agentHash) {
			for (;;) {
				int aliveCnt = 0;
				long curTime = new Date().getTime();
				int timeRemain = (int) (expireTime - curTime) /1000;
				for (String key : agentHash.keySet()) {
					try {
						Thread thread = agentHash.get(key).servthread;
						if (thread.isAlive()) {
							UniLog.log1("cron:%s still alive:%s state:%s timeremain:%d",key, thread.isAlive(), thread.getState(), timeRemain);
							aliveCnt++;
						}
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
				}
				if (aliveCnt == 0) {
					UniLog.log1("no more thread alive.");
					break;
				}
				if (curTime > expireTime) {
					UniLog.log1("stop timeout");
					break;
				}
				
				//sleep awhile
				try {
					Thread.sleep(5000);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		
	}
}
