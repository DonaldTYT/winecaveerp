package com.uniinformation.utils;

import java.util.*;

import com.kyoko.common.Sprintf;

public class ThreadPool implements IdleRunnable {
	//static ThreadPool defaultThreadPool = new ThreadPool("default");  //andrew210315 should not instantiate complex object in static block
	static ThreadPool defaultThreadPool = null;  //andrew210315 set defaultThreadPool in getDefaultThreadPool()
	LinkedList idlePool = new LinkedList();
	LinkedList runningPool = new LinkedList();
	String threadPoolName = null;
	int threadCounter = 0;
	int initPoolSize = 10;
	boolean isIdleRunStarted = false;
	public ThreadPool(String p_threadPoolName) {
	   UniLog.log1("construct threadpool:%s", p_threadPoolName);
	   threadPoolName = p_threadPoolName;
	   checkPoolSize(initPoolSize);
	}
	public void idleRun() {
		//UniLog.log("ThreadPool: idleRun() ...");
	   checkPoolSize(initPoolSize);
	}
	private void checkPoolSize(int p_minFreeThread) {
		synchronized(idlePool) {
	      if (idlePool.size() < p_minFreeThread) {
				int cnt = p_minFreeThread - idlePool.size();
				for (int i=0; i<cnt; i++) {
			      String threadName;
			      synchronized (this) {
				      threadName = threadPoolName+"_"+threadCounter;
			         threadCounter++;
			      }
		         idlePool.addLast(new ThreadPoolThread(this, threadName));
				}
		   }
		}
	}
	private ThreadPoolThread getThreadFromPool() {
	   synchronized (this) {
	      if (!isIdleRunStarted) {
				isIdleRunStarted = true;
		      IdleRun.getDefaultIdleRun().addRunClient(2000, this);
		   }
		}
	   synchronized (idlePool) {
	      checkPoolSize(1);
		   if (idlePool.size() > 0) {
			   ThreadPoolThread myThread = (ThreadPoolThread) idlePool.getFirst();
			   idlePool.removeFirst();
				return(myThread);
		   }
		}
		UniLog.log(new Exception("getThreadFromPool() failed"));
		return(null);
	}
	public ThreadPoolThread getThread(Runnable p_runnable) {
	   return(getThread(p_runnable, new Sprintf("%s@%x").add(p_runnable.getClass().getName()).add(p_runnable.hashCode()).toString()));
	}
	public ThreadPoolThread getThread(Runnable p_runnable, String p_name) {
		ThreadPoolThread myThread = getThreadFromPool();
	   myThread.setRunnable(p_runnable);
		synchronized (runningPool) {
	      runningPool.addFirst(myThread);
		}
		myThread.setUserName(p_name);
	   return(myThread);
	}
	public void returnThread(ThreadPoolThread myThread) {
	   if (myThread == null) {
			UniLog.log(3, "ThreadPool:returnThread(): myThread is null");
		   return;
		}
	   synchronized (runningPool) {
		   runningPool.remove(myThread);
		}
      synchronized (idlePool) {
		   idlePool.addLast(myThread);
		}
	}
	public void removeThread(ThreadPoolThread myThread) {
	   if (myThread == null) {
		   UniLog.log(3, "ThreadPool:removeThread(): myThread is null");
		   return;
	   }
		UniLog.log("ThreadPool:removeThread("+myThread.getThreadName()+")");
	   synchronized (runningPool) {
		   runningPool.remove(myThread);
		}
      synchronized (idlePool) {
		   idlePool.remove(myThread);
		}
	}
	synchronized public void shutdown() {
		synchronized (runningPool) {
		   int cnt = runningPool.size();
			for (int i=0; i<cnt; i++) {
			   ThreadPoolThread myThread = (ThreadPoolThread) runningPool.get(i);
			   myThread.pleaseDie();
			}
		}
		synchronized (idlePool) {
		   int cnt = idlePool.size();
			for (int i=0; i<cnt; i++) {
			   ThreadPoolThread myThread = (ThreadPoolThread) idlePool.get(i);
			   myThread.pleaseDie();
			}
		}
	   for (;;) {
		   if (runningPool.size() == 0 && idlePool.size() == 0)
			   return;
		   try { 
		      //UniLog.log("ThreadPool: "+threadPoolName+" waiting all threads to die");
		      UniLog.log1("ThreadPool: %s waiting all threads to die. running:%d idel:%d", threadPoolName, runningPool.size(), idlePool.size());
			   wait(1000);
		   } catch (InterruptedException ex) {
			   UniLog.log(ex);
			}
		}
	}
	public String toString() {
	   return(
		   new StringBuffer()
				 .append("<ThreadPool>")
				 .append("<threadPoolName>").append(threadPoolName).append("</threadPoolName>")
				 .append("<threadCounter>").append(threadCounter).append("</threadCounter>")
				 .append("<idlePool>").append(idlePool).append("</idlePool>")
				 .append("<runningPool>").append(runningPool).append("</runningPool>")
				 .append("</ThreadPool>")
			    .toString()
		);
	}
	private static class TestRunnable implements Runnable {
	   public void run() {
		   UniLog.log("TestRunnable:running");
			/*
		   for (int i=0; i<1000000; i++)
			   new java.util.Date();
			*/
		   try { 
		      Thread.currentThread().sleep(1000); 
		   } catch (Exception ex) { 
		      UniLog.log(ex); 
		   }
		   UniLog.log("TestRunnable:finish running");
		}
	}
	/*
	public static ThreadPool getDefaultThreadPool() {
	   return(defaultThreadPool);
	}
	*/
	public static synchronized ThreadPool getDefaultThreadPool() {
		if (defaultThreadPool == null) {
			defaultThreadPool = new ThreadPool("default");
		}
	   return(defaultThreadPool);
	}
	public static void shutdownDefaultThreadPool() {
		if (defaultThreadPool == null) {
			UniLog.log1("no default thread pool");
			return;
		}
		UniLog.log1("shutdown default thread pool");
		defaultThreadPool.shutdown();
	}
	public static void main(String[] args) {
		ThreadPool threadPool = ThreadPool.getDefaultThreadPool();
	   UniLog.log(threadPool.toString());
		try { 
		   Thread.currentThread().sleep(1000); 
		} catch (Exception ex) { 
		   UniLog.log(ex); 
		}
		for (int i=0; i<10; i++) {
	      Thread t1 = threadPool.getThread(new TestRunnable(), "i="+i);
		   t1.start();
		}
	   UniLog.log(threadPool.toString());
	   threadPool.shutdown();
	}
}
