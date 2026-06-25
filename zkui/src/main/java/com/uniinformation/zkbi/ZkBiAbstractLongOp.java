package com.uniinformation.zkbi;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
public abstract class ZkBiAbstractLongOp {
	Timer timer = new Timer();
	Component comp;
	Component busyComp; //comp for show busy message
	String busyMsg;
	/***
	 * @param p_comp - zk component for attach timer.
	 *                 display busy message (if any).
	 * @param p_busyMsg - show busy message, can be null.
	 *                    null will not display busy message.
	 */
	
	public ZkBiAbstractLongOp(Component p_comp){
		this(p_comp, null);
	}
	public ZkBiAbstractLongOp(Component p_comp, String p_busyMsg){
		this(p_comp, p_busyMsg, 0);
	}
	public ZkBiAbstractLongOp(Component p_comp, String p_busyMsg, int p_delay){
		comp = p_comp;
		busyMsg = p_busyMsg;
		if (comp == null){
			UniLog.log1("WARNING:component is null, ignore");
			return;
		}
		Page page = null;
		page = comp.getPage();
		if (page == null) {
			UniLog.log1("component page is null, try to obtain from main component");
			page = ZkUtil.getMainCompPage();
		}
		if (page == null) {
			UniLog.log1("WARNING:page is null, ignore");
			return;
		}
		busyComp = getBusyComp(p_comp,5);
		
		//step 1. display busy msg
		beforeLongOp();
		
		
		//step 2. process long operation
		timer.setDelay(p_delay > 0 ? p_delay : 0);
		timer.setPage(page);
		timer.setRepeats(false);
		timer.addEventListener(Events.ON_TIMER, new EventListener<Event>(){
			@Override
			public void onEvent(Event p_event) throws Exception {
				if (timer == null)
					return;
				ReturnMsg rtnMsg = longOp();
				
				//step 3. finish
				afterLongOp(rtnMsg);
				timer.setRunning(false);
				timer.detach(); //avoid memory leak
				timer = null;
			}
		});
		timer.setRunning(true);
	}
	
	/***
	 * pre action
	 * display a busy message
	 */
	public void beforeLongOp(){
		markBusy();
		if (!StringUtils.isBlank(busyMsg)){
			//Clients.showBusy(comp,busyMsg);
			Clients.showBusy(busyComp,busyMsg);
		}
	}
	
	/***
	 * post action, can obtain the result here
	 * @param p_rtnMsg - result of LongOp()
	 */
	public void afterLongOp(ReturnMsg p_rtnMsg){
		if (!StringUtils.isBlank(busyMsg)){
			//Clients.clearBusy(comp);
			Clients.clearBusy(busyComp);
		}
		clearBusy();
	}
	
	/***
	 * method implement by user
	 * @return
	 */
	public abstract ReturnMsg longOp();
	
	
    /***
     * mark comp busy 
     * REMARK: may raise multithread issue, need to investigate
     */
    private void markBusy(){
    	if (comp != null){
    		comp.setAttribute("ZkBiAbstractLongOp.busyFlag", "Y");
    	}
    }
    
    /***
     * clear comp busy flag
     * @param p_comp
     */
    public void clearBusy(){
    	if (comp != null){
    		comp.removeAttribute("ZkBiAbstractLongOp.busyFlag");
    	}
    }
    
    public void cancel() {
    	if (timer == null)
    		return;
    	UniLog.log("longOp cancelled");
		afterLongOp(null);
    	timer.setRunning(false);
		timer.detach(); //avoid memory leak
		timer = null;
    }
    
    /***
     * check comp is busy
     * @param p_comp
     * @return
     */
    public static boolean isBusy(Component p_comp){
    	if (p_comp == null) {
    		return false;
    	}
    	return StringUtils.equals((String)p_comp.getAttribute("ZkBiAbstractLongOp.busyFlag"), "Y");
    }
    
    public static Component getBusyComp(Component p_comp) {
    	Component resComp = getBusyComp(p_comp, 5);
    	return resComp != null ? resComp : p_comp;
    }
    private static Component getBusyComp(Component p_comp, int p_maxLevel) {
    	if (p_maxLevel <= 0 || p_comp == null) {
    		return null;
    	}
    	if (p_comp instanceof Window || p_comp instanceof Div) {
    		return p_comp;
    	}
    	return getBusyComp(p_comp.getParent(), p_maxLevel-1);
    }
    
    public static interface Callback {
    	ReturnMsg action();
    }
    public static ZkBiAbstractLongOp newInstance(Component p_comp, String p_busyMsg, int p_delay, Callback callback) {
    	return newInstance(null, p_comp, p_busyMsg, p_delay, callback);
    }
    public static ZkBiAbstractLongOp newInstance(ZkBiAbstractLongOp oldLongOp, Component p_comp, String p_busyMsg, int p_delay, Callback callback) {
    	if (oldLongOp != null)
    		oldLongOp.cancel();
    	return new ZkBiAbstractLongOp(p_comp, p_busyMsg, p_delay) {
			@Override
			public ReturnMsg longOp() {
				return callback.action();
			}
    	};
    }
}
