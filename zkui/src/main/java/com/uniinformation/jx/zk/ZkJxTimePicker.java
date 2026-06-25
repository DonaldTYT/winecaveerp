package com.uniinformation.jx.zk;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModel;

import com.kyoko.common.DateUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;

public class ZkJxTimePicker extends Combobox implements AfterCompose{
	private static final int minInDay = 1440;
	int startInMin = 0;
	int endInMin = minInDay;
	int stepMin = 15;
	int dayCnt = 1;
	boolean isShortFormat = false;
	boolean fInit = false;
	
	/***
	 * called by zul
	 * @param p_stepMin
	 */
	public void setStepMin(int p_stepMin) {
		if (p_stepMin > 0) {
			stepMin = p_stepMin;
		}
		UniLog.log1("stepMin change to %d", stepMin);
	}
	
	/***
	 * called by zul
	 * @param p_timeStr
	 */
	public void setStartTime(String p_timeStr) {
		startInMin = DateUtil.timeToMin(p_timeStr);
		if (startInMin < 0) {
			startInMin = 0;
		}
	}
	
	/***
	 * called by zul
	 * @param p_timeStr
	 */
	public void setEndTime(String p_timeStr) {
		endInMin = DateUtil.timeToMin(p_timeStr);
		dayCnt = (int)Math.ceil((double)endInMin / minInDay); 
		if (endInMin > minInDay * dayCnt) {
			endInMin = minInDay * dayCnt;
		}
	}

	/***
	 * called by zul
	 * @param p_shortfmt
	 */
	public void setIsShortFormat(boolean p_shortfmt) {
		isShortFormat = p_shortfmt;
	}
	
	/***
	 * convert hh:mm to min
	 * @param p_timeStr
	 * @return
	 */
	public static void main(String args[]) {
		ZkJxTimePicker tp = new ZkJxTimePicker();
		//tp.setStepMin(15);
		//tp.setStartTime("9:00");
		//tp.setEndTime("18:00");
		tp.init();
		for (Comboitem ci : tp.getItems()) {
			UniLog.log1("" + ci.getLabel());
			
		}
		
	}
	public ZkJxTimePicker() {
		super();
		/*
		//andrew210803 this block of code moved to afterCompose
		for(int i=0;i<96;i++) {
			int h = i / 4;
			int m = i % 4;
			appendItem(String.format("%02d:%02d:00", h,m * 15));
		}
		*/
		/*
		//andrew210803 this validation block of code seems useless
		EventListener zkEventListener = new EventListener() {
			public void onEvent(Event event) throws Exception {
					UniLog.log("Time Picker Opened");
					if(getSelectedIndex() < 0) {
						Calendar cal=Calendar.getInstance();
						cal.setTime(new Date());
						int h = cal.get(Calendar.HOUR);
						int m = cal.get(Calendar.MINUTE);
//						setSelectedIndex(h * 4 + (m/15));
					}
			}	
		};
		addEventListener(Events.ON_OPEN, zkEventListener);
		*/
	}
	/***
	 * construct the item list. need to run after setXXX method.
	 */
	public ZkJxTimePicker init() {
		if (fInit) {
			UniLog.log1("init already");
			return this;
		}
		fInit = true;
		for (int curMin=0; curMin< minInDay * dayCnt; curMin+=stepMin) {
			if (curMin >= startInMin && curMin <= endInMin) {
				//UniLog.log1(String.format("%02d:%02d:00", curMin/60, curMin%60));
				appendItem(String.format("%02d:%02d" + (isShortFormat ? "" : ":00"), curMin/60, curMin%60));
			}
		}
		return this;
	}
	@Override
	public void afterCompose() {
		init();
	}
}
