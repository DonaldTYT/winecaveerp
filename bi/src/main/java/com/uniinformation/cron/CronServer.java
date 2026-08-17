package com.uniinformation.cron;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class CronServer implements Runnable {
	static Hashtable<String,CronServer> agentHash;
	static int startupDelayTime = 20000;
	static volatile boolean enabled = true;
	static AtomicInteger threadId = new AtomicInteger(0);

	final String agent;
	final String cronClassName;
	final String cronLoginId;
	final ServletContext svc;
	final AtomicBoolean fStop = new AtomicBoolean(false);
	final AtomicBoolean cronJobStopCalled = new AtomicBoolean(false);
	final Object waitLock = new Object();

	volatile SessionHelper sessionHelper;
	volatile CronJob cronJob;
	volatile Thread servthread;

	public static synchronized void startCronServer(String p_agent,String p_class,String p_cronLoginId,ServletContext p_svc) throws Exception {
		if(StringUtils.isAnyBlank(p_agent,p_class,p_cronLoginId)) {
			UniLog.log1("missing madatory param (%s,%s,%s). action abort",p_agent,p_class,p_cronLoginId);
			return;
		}
		if(agentHash == null) {
			UniLog.log("CronServer Server Init");
			enabled = true;
			agentHash = new Hashtable<String,CronServer>();
		}
		addAgentServer(p_agent,p_class,p_cronLoginId,p_svc);
	}

	static public void addAgentServer(String p_agent,String p_class,String p_cronLoginId,ServletContext p_svc) throws Exception {
		CronServer server;
		synchronized(CronServer.class) {
			if(agentHash == null) {
				UniLog.log("CronServer add agent failed : CronServer not Initialized");
				return;
			}
			String key = p_agent+":"+p_class;
			if(agentHash.get(key) != null) return;
			UniLog.log("CronServer add agent " + key);
			server = new CronServer(p_agent,p_class,p_cronLoginId,p_svc);
			agentHash.put(key,server);
		}
		server.startWorkerIfEnabled();
	}

	public CronServer(String p_agent,String p_class,String p_cronLoginId,ServletContext p_svc) {
		agent = p_agent;
		cronClassName = p_class;
		cronLoginId = p_cronLoginId;
		svc = p_svc;
	}

	private synchronized void startWorkerIfEnabled() {
		if(!enabled || (servthread != null && servthread.isAlive())) return;
		fStop.set(false);
		cronJobStopCalled.set(false);
		servthread = new Thread(this);
		servthread.setDaemon(true);
		servthread.setName(cronClassName.replaceAll("^.*\\.","") +"-"+ threadId.addAndGet(1));
		servthread.start();
	}

	private boolean waitForNextRun(long p_waitTime) throws InterruptedException {
		long expireTime = System.currentTimeMillis() + Math.max(0L,p_waitTime);
		synchronized(waitLock) {
			while(!fStop.get()) {
				long waitTime = expireTime-System.currentTimeMillis();
				if(waitTime <= 0L) return true;
				waitLock.wait(waitTime);
			}
		}
		return false;
	}

	private void requestStop() {
		fStop.set(true);
		CronJob job = cronJob;
		if(job != null) {
			try {
				job.notifyCronServerStopRequested();
			}
			catch(Exception ex) {
				UniLog.log(ex);
			}
		}
		synchronized(waitLock) {
			waitLock.notifyAll();
		}
	}

	private void stopJobNow() {
		CronJob job = cronJob;
		if(job != null && cronJobStopCalled.compareAndSet(false,true)) {
			try {
				job.stop();
			}
			catch(Exception ex) {
				UniLog.log(ex);
			}
		}
	}

	private void interruptWorker() {
		Thread thread = servthread;
		if(thread != null) thread.interrupt();
	}

	private boolean awaitStopped(long p_timeout) {
		Thread thread = servthread;
		if(thread == null || thread == Thread.currentThread()) return thread == null;
		if(p_timeout <= 0L) return !thread.isAlive();
		try {
			thread.join(p_timeout);
		}
		catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
			return false;
		}
		return !thread.isAlive();
	}

	private void releaseResources(CronJob p_cronJob,SessionHelper p_sessionHelper) {
		if(p_cronJob != null && cronJobStopCalled.compareAndSet(false,true)) {
			try {
				p_cronJob.stop();
			}
			catch(Exception ex) {
				UniLog.log(ex);
			}
		}
		if(p_sessionHelper != null) {
			try {
				p_sessionHelper.cleanSessionData();
			}
			catch(Exception ex) {
				UniLog.log(ex);
			}
		}
	}

	@Override
	public void run() {
		CronJob currentCronJob = null;
		SessionHelper currentSessionHelper = null;
		try {
			Class<?> cronJobClass = Class.forName(cronClassName);
			currentCronJob = (CronJob) cronJobClass.newInstance();
			currentCronJob.setCronServerStopFlag(fStop);
			cronJob = currentCronJob;
			int pollTime = currentCronJob.getPollTime();
			UniLog.log1("cronserver (agent:%s) (startupdelay:%d) (polltime:%d)",agent,startupDelayTime,pollTime);
			if(!waitForNextRun(startupDelayTime)) return;
			if(fStop.get()) return;

			currentSessionHelper = ZkSessionHelper.getSessionHelperDummy(agent,cronLoginId,svc);
			sessionHelper = currentSessionHelper;
			currentCronJob.setSessionHelper(currentSessionHelper);
			currentCronJob.start();

			while(!fStop.get()) {
				try {
					UniLog.log1("wakeup agent:%s",agent);
					currentCronJob.runOnce();
				}
				catch(InterruptedException ex) {
					// CronServer does not interrupt runOnce during a toggle. Preserve an
					// interrupt raised by the job or container and leave when stopping.
					if(fStop.get()) break;
					Thread.currentThread().interrupt();
					throw ex;
				}
				catch(Exception ex) {
					UniLog.log1("ERROR:"+ex.getMessage());
				}
				if(fStop.get() || !waitForNextRun(pollTime)) break;
			}
		}
		catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
			UniLog.log1("cronserver interrupted. agent:%s",agent);
		}
		catch(Exception ex) {
			UniLog.log("cronserver cronjob start failed. agent:"+agent+" class:"+cronClassName+" ex:"+ex.getMessage());
		}
		finally {
			// This is reached only after runOnce has returned (or thrown), so job
			// resources are never closed underneath an active runOnce invocation.
			releaseResources(currentCronJob,currentSessionHelper);
			boolean restart;
			synchronized(this) {
				cronJob = null;
				sessionHelper = null;
				servthread = null;
				// Restart here only when an enable raced with a graceful stop. A
				// startup/runtime failure must retain the old behavior and stay down.
				restart = fStop.get() && enabled && isRegistered(this);
			}
			UniLog.log1("cronserver worker stopped. agent:%s class:%s",agent,cronClassName);
			if(restart) startWorkerIfEnabled();
		}
	}

	private static synchronized boolean isRegistered(CronServer p_server) {
		return agentHash != null && agentHash.containsValue(p_server);
	}

	private static synchronized List<CronServer> getServers() {
		return agentHash == null
				? new ArrayList<CronServer>()
				: new ArrayList<CronServer>(agentHash.values());
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static int getConfiguredServerCount() {
		return getServers().size();
	}

	public static int getRunningServerCount() {
		int count = 0;
		for(CronServer server : getServers()) {
			Thread thread = server.servthread;
			if(thread != null && thread.isAlive()) count++;
		}
		return count;
	}

	/**
	 * Enables or gracefully disables every configured CronServer worker.
	 * Disabling waits for active runOnce calls to return before resources are
	 * released. A false return means one or more jobs are still stopping after
	 * the requested timeout; they will clean up when runOnce eventually returns.
	 */
	public static boolean setEnabled(boolean p_enabled,long p_timeout) {
		List<CronServer> servers;
		synchronized(CronServer.class) {
			enabled = p_enabled;
			servers = getServers();
		}
		UniLog.log1("CronServer enabled:%s configured:%d",p_enabled,servers.size());
		if(p_enabled) {
			for(CronServer server : servers) server.startWorkerIfEnabled();
			return true;
		}

		for(CronServer server : servers) server.requestStop();
		long timeout = Math.max(0L,p_timeout);
		long expireTime = System.currentTimeMillis()+timeout;
		boolean stopped = true;
		for(CronServer server : servers) {
			long waitTime = Math.max(0L,expireTime-System.currentTimeMillis());
			if(!server.awaitStopped(waitTime)) {
				stopped = false;
				Thread thread = server.servthread;
				UniLog.log1("cron:%s still stopping state:%s",server.agent,thread == null ? "stopped" : thread.getState());
			}
		}
		return stopped;
	}

	/**
	 * Final shutdown called from CronServeletStarter.destroy(). Unlike the UI
	 * toggle, this also removes all retained job definitions.
	 */
	public static void stop(long p_timeout) {
		long timeout = p_timeout < 60000L ? 60000L : p_timeout;
		UniLog.log1("called: timeout:%d",timeout);
		List<CronServer> servers;
		synchronized(CronServer.class) {
			enabled = false;
			servers = getServers();
		}
		// Final servlet destruction retains the original immediate-stop behavior
		// so a CronJob can unblock its own runOnce. The UI toggle does not use it.
		for(CronServer server : servers) server.requestStop();
		for(CronServer server : servers) {
			server.stopJobNow();
			server.interruptWorker();
		}
		long expireTime = System.currentTimeMillis()+timeout;
		for(CronServer server : servers) {
			server.awaitStopped(Math.max(0L,expireTime-System.currentTimeMillis()));
		}
		synchronized(CronServer.class) {
			if(agentHash != null) agentHash.clear();
			agentHash = null;
		}
		UniLog.log1("cron shutdown complete");
	}
}
