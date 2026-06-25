package com.uniinformation.jx.zk;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Radio;

import com.uniinformation.jx.JxField;
import com.uniinformation.utils.UniLog;

public class JxZkBandBox extends JxZkInputElement {
	
	public JxZkBandBox(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		if(c instanceof ZkJxQueryInput) {
			UniLog.log("Handing ZkJxQueryInput skinElement");
    		((ZkJxQueryInput)c).setEventListenerCallback(new ZkJxQueryInput.EventListenerCallback() {
				@Override
				public void callback(Event event) throws Exception {
//					zkEventListener.onEvent(event);
					Event ev = new Event(Events.ON_CHANGE);
					Events.echoEvent("onLongOp", c, ev);
				}
    		});
			
			
		} else {
			comp.addEventListener("onOpen", zkEventListener);	
		}
//		if(c instanceof ZkJxPickInput) {
//			((ZkJxPickInput) c) .setOnClickListener(
//				new EventListener() {
//					public void onEvent(Event event) throws Exception {
//						ZkJxPickInput ji = (ZkJxPickInput) comp;
//						Object o = ji.getSelectedItem();
//						if(o != null) {
//							if(o instanceof String) {
//								String s = orgValue;
//								orgValue = textValue;
//								textValue = o.toString();
//							}
//						}
//						if(actionListener != null) {
//							lastActionType = getJxField().ACTIONTYPE_SELECT;
//								actionListener.actionPerformed(getJxField());
//								lastActionType = 0;
//						}
//					}		
//				}
//			);
//			
//		}
		
	}
	
	@Override
	protected String processAction(Event ev) {
		JxField fd;
		switch(JxZkGadgetProvider.getEventID(ev.getName())) {
		case JxZkGadgetProvider.EV_ONOPEN:
			if(actionListener != null) {
				if(((Bandbox) comp).isOpen()) {
					lastActionType = JxField.ACTIONTYPE_PICKINPUTOPENED;
				} else {
					lastActionType = JxField.ACTIONTYPE_PICKINPUTCLOSED;
				}
				actionListener.actionPerformed(getJxField());
				lastActionType = 0;
				getSkin().setDirtyFlag(true);
			}
			return(null);
		case JxZkGadgetProvider.EV_ONCLICK:
			if(((Bandbox) comp).isOpen()) return(null);
		default : return(super.processAction(ev));	
		}
	}	
	
//	public int grid_getcurrentrow()
//	{
//		if(comp instanceof ZkJxPickInput) {
//			return(((ZkJxPickInput) comp).getSelectedIndex());
//		} else return(-1);
//	}
//	
//	@Override
//	public void setItemList(Vector itemlist)
//	{
//		if(comp instanceof ZkJxPickInput) {	
//			((ZkJxPickInput) comp).setListModel(itemlist);
//		}
//	}

}
