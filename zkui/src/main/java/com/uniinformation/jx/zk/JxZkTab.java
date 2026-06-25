package com.uniinformation.jx.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Toolbarbutton;

import com.uniinformation.jx.JxActionListener;
import com.uniinformation.utils.UniLog;

public class JxZkTab extends JxZkElement {
	public JxZkTab(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
	}
	
	public void addActionListener(JxActionListener p_listener)
	{
		//UniLog.log("addActionListener on Unknown Element Type field " + getName());
		UniLog.log("JxCbuilderTab.addActionListener " + getName());
		if(p_listener != null) {
			if(actionListener == null) {
				comp.addEventListener("onSelect", zkEventListener);
			}
		} else {
			if(actionListener != null) {
				comp.removeEventListener("onSelect", zkEventListener);
			}
		}
		actionListener = p_listener;
	}
	protected String processAction(Event ev) {
			switch(JxZkGadgetProvider.getEventID(ev.getName())) {
				case JxZkGadgetProvider.EV_ONSELECT:
					if(actionListener != null) actionListener.actionPerformed(getJxField());
					return(null);
				default:	
					return(super.processAction(ev));
			}
	}
	public String getText() {
		if(comp instanceof Tab) {
			if(((Tab) comp).isSelected()) {
				return("Y");
			} else {
				return("N");
			}
		} else return(null);
	}
	public void setText(String p_text) {
		if(comp instanceof Tab) {
			if(p_text != null && p_text.equals("Y")) {
				((Tab) comp).setSelected(true);
			} else {
				((Tab) comp).setSelected(false);
			}
		}
	}
	
	@Override
	public Object getValue() {
		return(null);
	}
}
