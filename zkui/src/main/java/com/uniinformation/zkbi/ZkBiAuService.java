package com.uniinformation.zkbi;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;

import com.uniinformation.utils.UniLog;
/***
 * for register desktop level event
 * 
 * js client sample:
 * zkbiSend("onSomeEvent", {somekey: 'somevalue'});
 *
 */
public class ZkBiAuService implements AuService {
	HashMap<String,ZkBiAuEvent> listenerHM = new HashMap<String,ZkBiAuEvent>();

	/***
	 * create ZkBiAuService and add to desktop
	 * @param p_comp
	 * @return
	 */
	public static ZkBiAuService build(Desktop p_desktop) {
		if (p_desktop == null) {
			UniLog.log1("comp is null, abort");
			return null;
		}
		
		//return cache
		if (p_desktop.getAttribute("zkbiau") != null) {
			return (ZkBiAuService)p_desktop.getAttribute("zkbiau");
		}
		
		//create new instance and cache
		ZkBiAuService zkAu = new ZkBiAuService();
		p_desktop.addListener(zkAu);
		p_desktop.setAttribute("zkbiau", zkAu);
		
		/*
		zkAu.addEventListener("onStartup", new ZkBiAuEvent() {
			@Override
			public void onEvent(String p_evName, Map<String, Object> p_data) {
				UniLog.log1("called: name:%s data:%s", p_evName, p_data);
			}});
		*/
		return zkAu;
	}

	public void addEventListener(String p_evName, ZkBiAuEvent p_ev) {
		listenerHM.put(p_evName, p_ev);
	}
	public boolean service(AuRequest request, boolean everError) {
		final String cmd = request.getCommand();
		//UniLog.log1("called:%s",cmd);
		ZkBiAuEvent event = listenerHM.get(cmd);
		if (event != null) {
			event.onEvent(cmd, request.getData());
			return true;
		}
		return false;
	}

	public interface ZkBiAuEvent {
		public void onEvent(String p_evName, Map<String,Object> p_data);

	}
}