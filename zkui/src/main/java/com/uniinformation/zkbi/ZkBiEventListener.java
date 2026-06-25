package com.uniinformation.zkbi;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.impl.MessageboxDlg;

import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
/***
 * for replace zk EventListener
 * @author andrew
 *
 */
public abstract class ZkBiEventListener<T extends Event> implements EventListener<T>{
	final int dupThreshold;
	public ZkBiEventListener() {
		//dupThreshold = 500;
		dupThreshold = 200;
	}
	public ZkBiEventListener(int p_dupThreshold) {
		dupThreshold = p_dupThreshold;
	}
	
	/**
	 * preprocess zk event
	 * 
	 */
	final public void onEvent(T event) throws Exception {
		UniLog.log1("got event: %s",event.getName());
		
		//validation/pre/post only apply for interaction event
		if (!checkEventSupport(event)) {
			onZkBiEvent(event);
			return;
		}
		
		//validation, ignore duplicate event
		if (dupThreshold > 0 && !checkDupEvent(event,dupThreshold)) {
			return;
		}
		
		//pre processing
		preProcessing(event);
		
		//process event
		onZkBiEvent(event);
		
		//update timestamp again after process event
		if (dupThreshold > 0) {
			checkDupEvent(event,0);
		}
		
		//post processing
		postProcessing(event);
	}
	private static boolean checkEventSupport(Event event) {
		return StringUtils.equalsAny(event.getName(), "onClick", "onDelayClick", "onDoubleClick", "onOK", "onCancel");
	}
	
	private void preProcessing(T event) {
		//update last acces timestamp
		//SessionHelper.updateActiveUserStatic(null);
		ZkSessionHelper.updateActiveUser(null,null);
		
		//log event
		if (SessionHelper.logButtonFlag.get() && event != null && event.getTarget() != null){

			if (event.getTarget() instanceof Button && StringUtils.equalsAny(event.getName(),"onClick","onDelayClick")){
				ZkBiLogHelper.logEvent(null, event, ETYPE.LOG_BUTTON);
			}
			else if (event.getTarget() instanceof MessageboxDlg){
				ZkBiLogHelper.logEvent(null, event, ETYPE.LOG_BUTTON);
			}
		}
	}
	private void postProcessing(T event) {
		//auto close dlg window
		if (event.getTarget() instanceof ZkBiMsgboxButton && !event.getTarget().hasAttribute("forbitClose")) {
			((ZkBiMsgboxButton)event.getTarget()).closeMsgbox();
		}
	}
	
	/***
	 *
	 * Check event is duplicate within threshould
	 * Call this method before handle the event
	 * For slow operation (longer than threshold) should call one more time after handle the event
	 * 
	 * @param p_ev
	 * @param p_dupThreshold duplcate threshold in ms. value <= 0 disable duplcate check
	 * @return true: no duplicate event, false is duplicate and should be ignored
	 */
	private static boolean checkDupEvent(Event p_ev, int p_dupThreshold) { 
		if (p_ev == null || p_ev.getTarget() == null) {
			UniLog.log1("invalid event or target. skip check");
			return true;
		}
		
		//only handle interaction event
		if (!checkEventSupport(p_ev)) {
			return true;
		}
		
		//setup hashmap to keep timestamp
		ConcurrentHashMap<String,Long> dupEventHM = null;
		synchronized(p_ev.getTarget()) {
			dupEventHM = (ConcurrentHashMap) p_ev.getTarget().getAttribute("dupEventHM");
			if (dupEventHM == null) {
				//UniLog.log1("create new hashmap");
				dupEventHM = new ConcurrentHashMap<String,Long>();
				p_ev.getTarget().setAttribute("dupEventHM", dupEventHM);
			}
		}
		String evKey = p_ev.getTarget().hashCode() + "_"+ p_ev.getTarget().getId() +"_"+p_ev.getName();
		Long lastTS = dupEventHM.get(evKey);
		lastTS = lastTS == null ? 0 : lastTS;
		Long curTS = new Date().getTime();
		dupEventHM.put(evKey, curTS);
		/*
		UniLog.log1("update ts. key:%s lastTS:%d curTS:%d diff:%d", evKey, lastTS, curTS, curTS-lastTS);  
		try {
			throw new Exception("debug");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		*/

		if (p_dupThreshold <= 0) {
			return true;
		}

		//validatation
		if ((curTS - lastTS) < p_dupThreshold) {
			UniLog.log1("ignore duplicate event:%s", evKey);
			ZkUtil.showWarnMsg("Ignore duplicate event" + ": " + evKey);
			return false;
		}
		else {
			//UniLog.log1("valid event:%s", evKey);
			return true;
		}
	}
	private static boolean checkDupEvent(Event p_ev) { 
		return checkDupEvent(p_ev, 500);
	}
	
	
	/**
	 * mandatory implement by subclass
	 * same signature as EventListener.onEvent()
	 * 
	 * 
	 * @param event
	 * @throws Exception
	 */
	public abstract void onZkBiEvent(T event) throws Exception ;
	
	
	
}