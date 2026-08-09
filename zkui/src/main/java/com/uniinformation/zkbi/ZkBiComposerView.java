package com.uniinformation.zkbi;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.event.Event;

import com.codahale.metrics.Timer;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.ZkComposerBase;

public class ZkBiComposerView extends ZkComposerBase {
	Timer deskTopAliveTimer;
   	public void doAfterCompose(final Component comp) throws Exception { 
   		super.doAfterCompose(comp);
   		comp.addEventListener("onTabId", (Event e) -> {
   			String tabId = (String) e.getData();
   			// store it in Desktop or Session
   			// p_comp.setAttribute("tabid", tabId);
   			UniLog.log("tablId for " + desktopId + " = " + tabId);
   	    });

   	    // Ask client to send it back
   	    Clients.evalJavaScript(
   	      "zAu.send(new zk.Event(zk.Widget.$('$" + comp.getUuid() + "'), 'onTabId', getOrCreateTabId()));"
   	    );	
   	}
}
