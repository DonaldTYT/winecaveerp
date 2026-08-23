package com.uniinformation.jxapp;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiActionListener;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.utils.PopupWindowAction;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkComposerBase;

public class JxSelOpt extends JxForm {
	Object userdata = null;
	JxField jxBtSelect = null;
	JxField jxPickListBox = null;
//	JxField jxFormArea = null;
	XulElement filterComp;
	JxActionListener al;
	
	public void afterBind() {
		jxBtSelect = jxAdd("btSelect");
		jxPickListBox = jxAdd("pickListBox");
		jxPickListBox.setAttribute("useLazyLoad", "true");
//		jxFormArea = jxAdd("formArea");
		
		new JxFieldAction("btClear") {
			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				setSelectedRow(false,-1);
				triggerOnSelect();
			}
		};
		jxPickListBox.setAttribute("mode", "singleClickAction");
		new JxFieldChange("selOptFilter") {
			public boolean valueChanged(JxField fd, String orgValue)  {
				jxPickListBox.setAttribute("filter", fd.getText());
				setSelectedRow( false, 0);
				return(true);
			}
		};
		filterComp = (XulElement) jxAdd("selOptFilter").getNativeObject();
		
		filterComp.setCtrlKeys("#up#down");
		filterComp.addEventListener(Events.ON_OK, new EventListener(){
			@Override
			public void onEvent(Event p_event) throws Exception {
				if(al != null) al.actionPerformed(jxPickListBox);
				
				closeForm();
		}});
		filterComp.addEventListener(Events.ON_CTRL_KEY, new EventListener(){
			@Override
			public void onEvent(Event p_event) throws Exception {
				if (p_event instanceof KeyEvent){
					int keyCode = ((KeyEvent) p_event).getKeyCode();
					switch(keyCode){
						case 40: 
							UniLog.logm(this,"down arrow click");
							setSelectedRow( true, 1);
						break;
						case 38: 
							UniLog.logm(this,"up arrow click");
							setSelectedRow( true, -1);
						break;
					}
				}
			}});	
	}
	
	public static JxSelOpt createJxSelOpt(JxZkGadgetProvider pvdr) {
		Idspace ids = new Idspace();
		JxSelOpt js = (JxSelOpt) JxZkBiBase.getOrCreateJxZkForm(ids,pvdr ,"JxSelOpt");	
		return(js);
	}
	public static JxSelOpt createPopupJxSelOpt(SessionHelper sessionHelper) {
		return createPopupJxSelOpt(sessionHelper, false);
	}
	public static JxSelOpt createPopupJxSelOpt(SessionHelper sessionHelper, final boolean p_freeOnClose) {
		return createPopupJxSelOpt(sessionHelper, p_freeOnClose, ZkUtil.getMainComp());
	}
	public static JxSelOpt createPopupJxSelOpt(SessionHelper sessionHelper, final boolean p_freeOnClose,
			Component p_parent) {
		final JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
		if(pvdr == null) return(null);
		final Window[] popupHolder = new Window[1];
		Window thisPopup = ZkUtil.newPopupWindow(sessionHelper.isMobile(),sessionHelper.getLabel("Pick By Select"),
				p_parent == null ? ZkUtil.getMainComp() : p_parent, p_freeOnClose, p_freeOnClose ? new PopupWindowAction() {
					@Override
					public void onClose() {
						Window popup = popupHolder[0];
						if (popup != null) pvdr.jxUnRegisterForm(popup.getId());
					}
				} : null
//				Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot()	
				);
		popupHolder[0] = thisPopup;
		
		thisPopup.setSclass("jxselopt-popup");
		thisPopup.setStyle("background:#305496; opacity:1;");
		
//		thisPopup.setWidth("");
//		thisPopup.setHflex("min");
		thisPopup.setSizable(true);
		thisPopup.setHeight("325px");
		JxSelOpt selopt = (JxSelOpt) JxZkBiBase.getOrCreateJxZkForm(thisPopup,pvdr ,"JxSelOpt");	
		selopt.addFormCloseListener(
				new JxFormCloseListener( ) {
					public int formClose(JxForm jxf) {
						return(p_freeOnClose ? JxFormCloseListener.caFree : JxFormCloseListener.caHide);
					}
				}	
			);
		Component parent = (Component) selopt.getNativeComponent();
		if(parent != null && parent instanceof HtmlBasedComponent) {
//			((HtmlBasedComponent) parent).setWidth("min");
//			((HtmlBasedComponent) parent).setHflex("0");
		}
		return(selopt);
	}
//	public void setWidth(String p_width) {
//		UniLog.logm(this,"width = %s", p_width);
//		((Div) jxFormArea.getNativeObject()).setWidth(p_width);
//	}
//	
	public void setOnSelectAction(JxActionListener p_listener) {
		al = p_listener;
		jxPickListBox.addActionListener(p_listener);
		if (jxBtSelect != null){
			jxBtSelect.addActionListener(p_listener);
		}
	}
	public <T> void addCustomButton(String p_butName, final BiActionListener<? super T> p_action, final T p_value) {
		final Toolbarbutton customButton = new Toolbarbutton(p_butName);
		customButton.setId(p_butName);
		customButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event p_event) throws Exception {
				if (p_action != null) p_action.actionPerformed(p_value);
			}
		});

		Component clearButton = (Component) jxAdd("btClear").getNativeObject();
		clearButton.getParent().insertBefore(customButton, clearButton);
	}
	public void setUserData(Object p_userdata) {
		userdata = p_userdata;
	}
	public Object getUserData() {
		return(userdata);
	}
	public Object getPickListBoxValue() {
		if (jxPickListBox != null){
			return(jxPickListBox.getValue());
		}
		return(null);
	}
	void setSelectedRow( boolean p_isrelative, int p_idx) {
		int curidx;
		if(p_isrelative) {
			curidx = jxPickListBox.getCurrentRow();
			if(curidx < 0) curidx = 0;
		}  else curidx = 0;
		curidx += p_idx;
		jxPickListBox.gridSetCurrentRow(curidx);
	}
	public void beginPick()  {
		beginPick(true);
	}
	
	public void beginPick(boolean p_focusFilter) {
		jxAdd("selOptFilter").setText("");
		setSelectedRow( false, 0);
		if(p_focusFilter) filterComp.focus();
		/*
		Listbox lb = (Listbox) jxAdd("pickListBox").getNativeObject();
		lb.invalidate();
		*/
		HtmlBasedComponent hb = (HtmlBasedComponent) getNativeComponent();
		hb.invalidate();
	}
	
	public void triggerOnSelect() {
		if(al != null) al.actionPerformed(jxPickListBox);
	}
	
	public void setPickListBoxIdx(int p_idx) {
		jxPickListBox.gridSetCurrentRow(p_idx);
	}
	
	public void setPopupWidth(String width) {
		Component parent = (Component) getNativeComponent();
		if(parent != null && parent instanceof HtmlBasedComponent) {
			((HtmlBasedComponent) parent).setWidth(width);
		}
	}
	
}
