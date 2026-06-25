package com.uniinformation.utils;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
/***
 * Stopwatch utility for monitor slow task.
 * @author andrew
 *
 */
public class StopWatchHelper {
	final StopWatch stopWatch;
	final Boolean withLog;
	long alertThreshold = -1;
	final String label;
	/***
	 * with log
	 */
	public StopWatchHelper() {
		this(true,-1,null);
	}
	/***
	 * with alert message for debug slow operation
	 * without log
	 * @param p_alertThreshold
	 */
	public StopWatchHelper(long p_alertThreshold) {
		this(false,p_alertThreshold,null);
	}
	/***
	 * with alert message for debug slow operation
	 * without log
	 * @param p_alertThreshold
	 * @param p_label
	 */
	public StopWatchHelper(long p_alertThreshold, String p_label) {
		this(false,p_alertThreshold,p_label);
	}
	
	/***
	 * with log 
	 * @param p_label
	 */
	public StopWatchHelper(String p_label) {
		this(true,-1,p_label);
	}
	
	/***
	 * 
	 * @param p_withLog mark time in log
	 * @param p_alertThreshold unit in ms
	 */
	public StopWatchHelper(boolean p_withLog,long p_alertThreshold,String p_label) {
		stopWatch = new StopWatch();
		withLog = p_withLog;
		label = p_label == null ? "" : "[" + p_label + "]";
		alertThreshold = p_alertThreshold;
		stopWatch.start();
		if (withLog) UniLog.log1("stopwatch%s start", label);
	}
	public StopWatchHelper pause() {
		stopWatch.suspend();
		if (withLog) UniLog.log1("stopwatch%s pause. duration:%s", label, getDurationString());
		return this;
	}
	public StopWatchHelper resume() {
		stopWatch.resume();
		if (withLog) UniLog.log1("stopwatch%s resume. duration:%s", label, getDurationString());
		return this;
	}
	public void stop() {
		stopWatch.stop();
		if (withLog) UniLog.log1("stopwatch%s stop. duration:%s", label, getDurationString());
		if (alertThreshold > 0 && stopWatch.getTime() > alertThreshold) {
			UniLog.log1("stopwatch%s alert. duration:%s", label, getDurationString());
		}
	}
	public long getDuration() {
		return stopWatch.getTime();
	}
	public String getDurationString() {
		return DurationFormatUtils.formatDurationHMS(stopWatch.getTime());
	}
}