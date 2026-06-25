package com.uniinformation.jx.zk;

import java.awt.Event;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;

import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class ZkJxPickOpt extends Popup{
	Listbox lb;
	EventListener e;
	public ZkJxPickOpt(){
		super();
		lb = new Listbox();
		lb.setWidth("100px");
		lb.setHeight("100px");
		appendChild(lb);
	}
	public void pick(List <Listitem> l,Component c,EventListener  p_e) {
		if(e != null) {
			lb.removeEventListener(Events.ON_SELECT,e);
		}
		lb.getItems().clear();
		for(int i=0;i<l.size();i++) {
			lb.getItems().add(l.get(i));
		}
		e = p_e;
		lb.addEventListener(Events.ON_SELECT,e);
		open(c);
	}
	public Listbox getListbox()
	{
		return(lb);
	}
	/*
	static public int pick(List<Object> l,Component p_ref) {
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));
		if(sessionHelper == null) return(-1);
		ZkJxPickOpt zkp = (ZkJxPickOpt) sessionHelper.getSessionData("ZkJxPickOpt");
		if(zkp == null) {
			zkp = new ZkJxPickOpt();
			sessionHelper.putSessionData("ZkJxPickOpt",zkp);
			UniLog.log("Create Popup Component");
		}
		zkp.p.appendChild(p_ref);
		zkp.p.open(p_ref);
		return(-1);
	}
	*/
}
