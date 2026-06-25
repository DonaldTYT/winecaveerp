package com.uniinformation.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPoolThread extends Thread {
	String threadName = null;
   ThreadPool threadPool = null;
	Runnable runnable = null;
	private boolean isStarted = false;
	private boolean isRunning = false;
	private boolean isPleaseDie = false;
	private String userName = null;
	private int version = 0;
	AtomicBoolean fDebug = new AtomicBoolean(false);
	public ThreadPoolThread(ThreadPool p_threadPool, String p_threadName) {
		super(p_threadName);
		if (fDebug.get()) UniLog.log("ThreadPoolThread("+p_threadName+"): created");
		threadPool = p_threadPool;
		threadName = p_threadName;
		super.start();
	}
	public String getThreadName() {
		return(threadName);
	}
	public void setUserName(String p_name) {
		userName = p_name;
	}
	public String getUserName() {
		return(userName);
	}
	synchronized public void pleaseDie() {
		isPleaseDie = true;
		if (runnable != null) {
		   if (runnable instanceof ThreadPoolRunnable) {
			   UniLog.log1("try to shutdown [%s] [%s]",threadName, runnable.toString());
			   ((ThreadPoolRunnable) runnable).shutdown();
		   }
		   else {
			   UniLog.log1("skip shutdown %s",runnable.toString());
		   }
		}
		notify();
	}
	synchronized public void setRunnable(Runnable p_runnable) {
		runnable = p_runnable;
	}
	synchronized public void start() {
		if (fDebug.get()) UniLog.log("ThreadPoolThread:"+threadName+"["+getUserName()+"] starting");
		setName(threadName+":"+getUserName());
		isStarted = true;
		notify();
	}
	synchronized public int getVersion() {
	   return(version);
	}
	synchronized public void joinByVersion(int p_version) {
	   UniLog.log("joinByVersion("+p_version+")");
	   for (;;) {
			if (runnable == null)
			   break;
			if (p_version != version)
		      break;
			try { 
				wait(5000);
			} catch (InterruptedException ex) {
			   UniLog.log(ex);
			}
		}
	}
	public void run() {
		for (;;) {
			synchronized (this) {
			   try { 
					if (!isStarted)
				      wait(10000);
			   } catch (InterruptedException ex) {
			      UniLog.log(ex);
			   }
		   }
		   if (isStarted) {
			   UniLog.log("ThreadPoolThread:"+threadName+"["+getUserName()+"] running");
			   synchronized (this) {
		         isRunning = true;
			   }
			   runnable.run();
			   UniLog.log("ThreadPoolThread:"+threadName+"["+getUserName()+"] finish running");
				if (runnable instanceof ThreadPoolEventHandler) {
			      UniLog.log("ThreadPoolThread:"+threadName+"["+getUserName()+"] finish running event triggered");
					((ThreadPoolEventHandler) runnable).finishRunning();
				}
			   synchronized (this) {
				   runnable = null;
				   isStarted = false;
		         isRunning = false;
				   setUserName(null);
		         setName(threadName);
					version++;
			   }
			   threadPool.returnThread(this);
			   synchronized (this) {
				   notify();
				}
		   }
		   if (isPleaseDie) {
			   UniLog.log("ThreadPoolThread:"+threadName+" dying");
			   threadPool.removeThread(this);
			   synchronized (threadPool) {
				   threadPool.notify();
			   }
			   return;
		   }
	   }
	}
}
