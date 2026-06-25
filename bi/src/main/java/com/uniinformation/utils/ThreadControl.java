package com.uniinformation.utils;

import java.util.*;

public class ThreadControl {
	int standbyThreadCount = 1;
	int maxThreadCount = 5;
	int threadId = 0;
	String name;
	LinkedList standbyThreads = new LinkedList();
	HashSet runningThreads = new HashSet();
	HashSet finishingThreads = new HashSet();
	//boolean fTimebomb = ((new java.util.Date()).getTime() > DateUtil.nextday(DateUtil.getDate("2012/06/01", "yyyy/mm/dd"), 122).getTime());
	boolean fTimebomb = false;
	class ThreadControlRunnable extends Thread {
	   Runnable runnable = null;
		boolean fToFinish = false;
	   ThreadControlRunnable(String p_threadId) {
		   super(p_threadId);
		   start();
		}
		synchronized void start(Runnable p_runnable) {
		   runnable = p_runnable;
			notify();
		}
		synchronized void requestFinish() {
		   fToFinish = true;
			notify();
		}
		void resetRunnable() {
		   runnable = null;
		}
	   public void run() {
		   for (;;) {
			   try {
				   synchronized(this) {
				      if (fToFinish)
				         break;
				      if (runnable == null) {
					      wait();
				      }
					}
				} catch (Exception ex) {
				   UniLog.logClass(this, ex);
				}
				if (fToFinish)
				   break;
				if (runnable == null) {
					continue;
				}
	         onRunnableStarting(this);
				runnable.run();
	         UniLog.logClass(runnable, "calling onRunnableEnded("+getName()+") ...");
	         if (!fTimebomb)
				   runnable = null;  
	         onRunnableEnded(this);
				// Mon Jun 11 21:05:02 HKG 2012 Lai
				// reset runnable should be before putting the thread resources back to er
				// runnable = null;  
	         if (fTimebomb)
				   runnable = null;  
			}
	      onRunnableDestroyed(this);
		}
	}
   public ThreadControl(String p_name) {
	   name = p_name;
	   checkStandbyThread();
	}
	public void setStandbyThreadCount(int p_standbyThreadCount) {
	   standbyThreadCount = p_standbyThreadCount;
	   checkStandbyThread();
	}
	public void setMaxThreadCount(int p_maxThreadCount) {
	   maxThreadCount = p_maxThreadCount;
	   checkStandbyThread();
	}
	synchronized public void checkStandbyThread() {
	   int cnt = standbyThreadCount;
		if (cnt > (maxThreadCount - runningThreads.size()))
		   cnt = maxThreadCount - runningThreads.size();
		if (cnt <= 0) {
         UniLog.logClass(this, "concurrent thead exceeded "+runningThreads.size()+" >= "+maxThreadCount);
		   return;
		}
		while (cnt < standbyThreads.size()) {
	      ThreadControlRunnable tcr = (ThreadControlRunnable) standbyThreads.removeFirst();
			finishingThreads.add(tcr);
		   tcr.requestFinish();
		}
		while (cnt > standbyThreads.size()) {
	      threadId++;
	      ThreadControlRunnable tcr = new ThreadControlRunnable(name+"-"+threadId);
	      standbyThreads.addLast(tcr);
		}
		notify();
	}
	synchronized public void onRunnableStarting(ThreadControlRunnable p_tcr) {
	   UniLog.logClass(p_tcr.runnable, "onRunnableStarting("+p_tcr.getName()+") ...");
	}
	synchronized public void onRunnableEnded(ThreadControlRunnable p_tcr) {
	   runningThreads.remove(p_tcr);
		standbyThreads.addLast(p_tcr);
		notify();
	   checkStandbyThread();
	}
	synchronized public void onRunnableDestroyed(ThreadControlRunnable p_tcr) {
	   UniLog.logClass(p_tcr, "onRunnableDestroyed("+p_tcr.getName()+") ...");
		finishingThreads.remove(p_tcr);
	}
	synchronized public boolean waitThread(long p_waitms) {
UniLog.logClass(this, "waitThread():0.1:runningThreads.size()="+runningThreads.size());
UniLog.logClass(this, "waitThread():0.2:standbyThreads.size()="+standbyThreads.size());
	   if (standbyThreads.size() > 0) {
UniLog.logClass(this, "waitThread():1");
		   return(true);
		}
	   try {
UniLog.logClass(this, "waitThread():2:runningThreads.size()="+runningThreads.size());
	      checkStandbyThread();
UniLog.logClass(this, "waitThread():3");
	      if (standbyThreads.size() > 0)
		      return(true);
UniLog.logClass(this, "waitThread():4");
		   wait(p_waitms);
UniLog.logClass(this, "waitThread():5");
		} catch (InterruptedException ex) {
		   UniLog.logClass(this, ex);
		}
	   return(standbyThreads.size() > 0);
	}
	synchronized public void startThread(Runnable p_runnable) throws ThreadControlNoThreadAvailableException {
	   if (runningThreads.size() >= maxThreadCount)
		   throw(new ThreadControlNoThreadAvailableException());
	   checkStandbyThread();
	   ThreadControlRunnable tcr = (ThreadControlRunnable) standbyThreads.removeFirst();
	   runningThreads.add(tcr);
		tcr.start(p_runnable);
	   checkStandbyThread();
	}
	public static void main(String[] args) throws Exception {
      ThreadControl tc = new ThreadControl("test");
		for (int i=0; i<100; i++) {
		   tc.waitThread(60000);
		   tc.startThread(new Runnable() {
			   public void run() {
				   UniLog.logClass(this, "running");
					try {
					   Thread.sleep(1000);
					} catch (Exception ex) {
					   UniLog.logClass(this, ex);
					}
				}
			});
		}
	   for (;;) {
		   Thread.sleep(5000);
	      UniLog.logClass(tc, "standbyThreads="+tc.standbyThreads);
         UniLog.logClass(tc, "runningThreads="+tc.runningThreads);
         UniLog.logClass(tc, "finishingThreads="+tc.finishingThreads);
		}
	}
}
