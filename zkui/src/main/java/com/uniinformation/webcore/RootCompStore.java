package com.uniinformation.webcore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

import com.uniinformation.utils.UniLog;
public class RootCompStore {
	
	//this Hashmap clean up by no reference to key object / timeout
	private static WeakHashMap<Component,Date> rootCompsWHM = new WeakHashMap<Component,Date>();  //key:root component value:last active timestamp
	private static int TIMEOUT = 900000; //15min
	private static boolean fEnable = false;  //experimental. don't enable it
	
	/***
	 * experimental only, not ready for production yet
	 * call it regularly to avoid expire
	 * workflow: js send event to server regularly. it may affect browser background tab, mobile phone browser
	 * 
	 * @param p_comp
	 */
	public static void updateRootComp(Component p_comp) {
		if (!fEnable) return;
		if (p_comp == null) return;
		cleanExpiredComp();
		synchronized(rootCompsWHM) 
		{
			rootCompsWHM.put(p_comp, new Date());
		}
	}
	
	/***
	 * remove comp from map
	 * @param p_comp
	 */
	private static void removeRootComp(Component p_comp) {
		if (!fEnable) return;
		if (p_comp == null) return;
		synchronized(rootCompsWHM) {
			rootCompsWHM.remove(p_comp);
		}
	}
	
	/***
	 * loop for clean up expired comp
	 * 
	 */
	private static void cleanExpiredComp() {
		if (!fEnable) return;
		synchronized(rootCompsWHM) {
			ArrayList<Component> removeList = new ArrayList<Component>();
			
			//scan the list, find out expired root component
			for (Map.Entry<Component,Date> entry : rootCompsWHM.entrySet()) {
				Component comp = entry.getKey();
				Date ts = entry.getValue();
				if (ts.getTime() + TIMEOUT < new Date().getTime()) {
					removeList.add(comp);
				}
			}
			
			//remove expired root component
			for (Component comp : removeList) {
				removeRootComp(comp);
				Events.sendEvent("onCleanExpiredComp", comp, null);
			}
		}
		
	}
	private static void debug() {
		if (!fEnable) return;
		synchronized(rootCompsWHM) 
		{
			System.gc();
			int idx = 0;
			int size = rootCompsWHM.size();
			for (Map.Entry<Component,Date> entry : rootCompsWHM.entrySet()) {
				Component comp = entry.getKey();
				Date ts = entry.getValue();
				UniLog.log1("idx:%d/%d comp:%s id:%s ts:%s", ++idx, size, comp, comp.getId(), ts);
			}
		}
		
	}

}
