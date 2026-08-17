package com.uniinformation.cron;

import java.util.concurrent.atomic.AtomicBoolean;

import com.uniinformation.webcore.SessionHelper;

public abstract class CronJob {
	private volatile AtomicBoolean cronServerStopFlag;

	final void setCronServerStopFlag(AtomicBoolean p_stopFlag) {
		cronServerStopFlag = p_stopFlag;
	}

	final void notifyCronServerStopRequested() {
		onCronServerStopRequested();
	}

	/**
	 * Cooperative shutdown hook for jobs whose runOnce method contains its own
	 * long-running loop.
	 */
	protected final boolean isCronServerStopRequested() {
		AtomicBoolean stopFlag = cronServerStopFlag;
		return stopFlag != null && stopFlag.get();
	}

	/**
	 * Called after the CronServer stop flag is set. Long-running jobs can use
	 * this callback to wake a timed wait; resource cleanup remains in stop().
	 */
	protected void onCronServerStopRequested() {
	}

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
