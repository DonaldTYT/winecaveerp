package com.uniinformation.utils;

import java.util.*;

public class IdleRun implements Runnable, ThreadPoolRunnable {
	static IdleRun defaultIdleRun = new IdleRun();
	static Object dummy = new Object();
   Hashtable clients = new Hashtable();
	boolean inNewThread = false;
	Thread thread = null;
   boolean isShutdown = false;
	boolean isKicked = false;
	public static IdleRun getDefaultIdleRun() {
	   return(defaultIdleRun);
	}
	public void removeRunClient(Object p_irc) {
		if (p_irc == null) {
		   UniLog.log(new Exception("removeRunClient(): object is null"));
			return;
		}
	   if (p_irc instanceof IdleRunClient) {
		   synchronized (clients) {
		      clients.remove(p_irc);
		   }
		   return;
		}
		else {
		   UniLog.log(new Exception("removeRunClient(): object("+p_irc.getClass().getName()+") is not instanceof IdleRunClient"));
			return;
		}
	}
	public Object addRunClient(long p_runInterval, IdleRunnable p_idleRunnable) {
		synchronized (this) {
			if (!inNewThread) {
			   inNewThread = true;
			   if (thread == null) {
			      thread = ThreadPool.getDefaultThreadPool().getThread(this, "IdleRun");
				   thread.start();
			   }
			   inNewThread = false;
			}
		}
	   IdleRunClient irc = new IdleRunClient(p_runInterval, p_idleRunnable);
		synchronized (clients) {
		   clients.put(irc, dummy);
		}
		synchronized (this) {
		   this.notify();
		}
		return(irc);
	}
	public void kickRunClient(Object p_irc) {
	   if (!(p_irc instanceof IdleRunClient)) {
		   UniLog.log(new Exception("kickRunClient(): object("+p_irc.getClass().getName()+") is not instanceof IdleRunClient"));
			return;
		}
		((IdleRunClient) p_irc).nextRunTime = System.currentTimeMillis();
		isKicked = true;
		synchronized (this) {
		   this.notify();
		}
	}
	/* for Runnable interface */
	public void run() {
	   for (;;) {
		   if (isShutdown) {
		      UniLog.log("IdleRun: shutting down ...");
		      break;
			}
			try {
			   long sleepTime = 5000;  // max sleep time
				isKicked = false;
			   for (Iterator iterator = clients.keySet().iterator();
				     iterator.hasNext(); ) {
	            IdleRunClient irc = (IdleRunClient) iterator.next();
				   long now = System.currentTimeMillis();
					long t = irc.nextRunTime - now;
					if (t <= 0) {
					   irc.idleRunnable.idleRun();
					   irc.nextRunTime = System.currentTimeMillis() + irc.runInterval;
					   t = irc.runInterval;
					}
					if (t > 0 && t < sleepTime)
						sleepTime = t;
				}
				if ((!isKicked) && sleepTime > 0) {
					try {
				      synchronized (this) {
						   wait(sleepTime);
				      }
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			} catch (ConcurrentModificationException ex) {
				UniLog.log("job entries modified while looping ...");
			}
		}
	}
	/* for ThreadPoolRunnable interface */
	public void shutdown() {
	   isShutdown = true;
		UniLog.log("IdleRun: shutdown activated");
		synchronized (this) {
		   this.notify();
		}
	}
   private class IdleRunClient {
	   long runInterval; /* in terms of milliseconds */
		IdleRunnable idleRunnable;
		long nextRunTime;
	   public IdleRunClient(long p_runInterval, IdleRunnable p_idleRunnable) {
		   runInterval = p_runInterval;
		   idleRunnable = p_idleRunnable;
		   nextRunTime = System.currentTimeMillis();
		}
	}
	private static class myRunnable implements IdleRunnable {
	   String message;
		Object obj;
		public myRunnable(String p_message, Object p_obj) {
		   message = p_message;
			obj = p_obj;
		}
	   public void idleRun() {
		   UniLog.log(message);
			if (obj != null) {
			   UniLog.log("kicking ...");
	         IdleRun.getDefaultIdleRun().kickRunClient(obj);
		   }
		}
	}
   public static void main(String[] args) throws Exception {
	   IdleRun idl = IdleRun.getDefaultIdleRun();
	   Object handle1 = idl.addRunClient(2000, new myRunnable("Hello World!: 1", null));
		Thread.currentThread().sleep(2000);
	   Object handle2 = idl.addRunClient(2000, new myRunnable("Hello World!: 2", null));
		Thread.currentThread().sleep(2000);
	   Object handle3 = idl.addRunClient(5000, new myRunnable("Hello World!: 3", handle2));
		Thread.currentThread().sleep(2000);
		idl.removeRunClient(handle1);
		Thread.currentThread().sleep(50000);
		ThreadPool.getDefaultThreadPool().shutdown();
	}
}
