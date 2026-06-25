package com.uniinformation.cron;

import com.uniinformation.webcore.SessionHelper;

public abstract class CronJob {
	public abstract int runOnce() throws Exception;
	public abstract void setSessionHelper(SessionHelper p_sh) throws Exception;
	
	/***
	 * call when thread start
	 */
	public void start() {
	}
	/***
	 * call when thread end
	 */
	public void stop() {
	}
	
	public int getPollTime() {
		return 20000;
	}
}
