package com.uniinformation.zkbi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Space;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
/***
 * for construct message dialog 
 * - it's enhanced version of ZkUtil.buildMessageboxDlg for replace zk Messagebox.show()
 * - easy to construct (just msg content and list of button name)
 * - customizable (e.g. modify button style)
 * - translate msg content (regular expression translation)
 * - translate button label
 * 
 * identify clicked button 
 * - ZkBiMsgbox.getIdx()
 * - ZkBiMsgbox.getName()
 * 
 *
 */
public class ZkBiMsgbox {
	Window msgboxWin = null;
	Vbox msgboxVbox = null;
	Component contentComp = null;
	ZkBiMsgboxButton[] btns = null;
	ZkBiEventListener<Event> el = null;
	public enum Type { info, question, warning, error };
	Type type = Type.info;
	boolean buildFlag = false;
	
	public static class ZkBiMsgboxButton extends Button{
		int idx = -1;
		ZkBiMsgbox msgbox = null;
		String name = null;
		boolean isDefault = false;
		
		/****
		 * parent constructor
		 * @param arg0
		 */
		public ZkBiMsgboxButton(String p_label) {
			super(p_label);
			name = p_label; //set default name as label
		}
		public ZkBiMsgboxButton(String p_label, String p_image) {
			super(p_label, p_image);
			name = p_label; //set default name as label
		}
		
		
		
		public ZkBiMsgboxButton setIdx(int p_idx) {
			idx = p_idx;
			return this;
		}
		public int getIdx() {
			return idx;
		}
		
		/***
		 * set default focus
		 * @return
		 */
		public ZkBiMsgboxButton setDefault() {
			isDefault = true;
			return this;
		}
		
		public ZkBiMsgboxButton appendStyle(String p_style) {
			ZkUtil.appendStyle(this, p_style);
			return this;
		}
		
		/***
		 * set button name
		 * it's not same as label, as label is support multi langage
		 * @param p_name
		 * @return
		 */
		public ZkBiMsgboxButton setName(String p_name) {
			name = p_name;
			return this;
		}
		public String getName() {
			if (name != null) {
				return name;
			}
			if (StringUtils.isNotBlank(this.getLabel())){
				return this.getLabel();
			}
			return "";
		}
		
		/*
		 * internal reference to msgbox
		 */
		public void setMsgbox(ZkBiMsgbox p_msgbox) {
			msgbox = p_msgbox;
		}
		
		/***
		 * close msgbox
		 */
		public void closeMsgbox() {
			if (msgbox != null) {
				msgbox.close();
			}
		}
	}
	SessionHelper sh = null;
	public ZkBiMsgbox(SessionHelper p_sh) {
		sh = p_sh;
	}
	public ZkBiMsgbox() {
		sh = ZkSessionHelper.getSessionHelper();
	}
	
	/***
	 * build dialog
	 * 
	 */
	public ZkBiMsgbox build() throws Exception {
		if (buildFlag) {
			UniLog.log1("already built");
			return this;
		}
		if (btns == null || btns.length == 0) {
			throw new Exception("no button defined");
		}
		if (contentComp == null) {
			throw new Exception("no content defined");
		}
		
		for (int i=0; i<btns.length; i++) {
			ZkBiMsgboxButton btn = btns[i];
			if (btn.getIdx() < 0) {
				btn.setIdx(i);
			}
			btn.setMsgbox(this);
			if (StringUtils.isBlank(btn.getId())){
				btn.setId("msgboxbtn_idx"+i+"_"+RandomStringUtils.randomAlphabetic(4));  //just random 4 char string to avoid duplicate id
			}
				
				
			if (el != null) {
				btn.addEventListener(Events.ON_CLICK, el);
			}
			else {
				//create a empty event listener for handle close dialog
				btn.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						//UniLog.log1("got event:"+event.getName());
					}});
			}
		}
		
		msgboxWin = new Window();
		msgboxWin.setParent(ZkUtil.getMainComp());
		msgboxWin.setClosable(true);
		msgboxWin.setStyle("max-width:90%");  //andrew231107 fix msgbox too wide
		msgboxWin.addEventListener(Events.ON_CLOSE, new ZkBiEventListener<Event>(){
			public void onZkBiEvent(Event event) throws Exception {
				event.getTarget().setVisible(false);
				event.stopPropagation();
				msgboxWin.detach();
		}});
		
		
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    String titleLb = sh == null ? "System Message" : sh.getLabel("System Message");
    	msgboxWin.setTitle(titleLb + " " + sdf.format(new Date()));
		msgboxVbox = new Vbox();
		msgboxVbox.setHflex("1");
		msgboxVbox.setStyle("min-width:350px; margin:10px;");
		msgboxWin.appendChild(msgboxVbox);
		
		msgboxVbox.appendChild(contentComp);
		msgboxVbox.appendChild(new Separator() {{ setHeight("10px"); }});
		/*
		msgboxVbox.appendChild(new Hbox() {{
			this.setHflex("1");
			this.setPack("center");
			for (ZkBiMsgboxButton btn : btns) {
				this.appendChild(btn);
			}
		}});
		*/
		
		//andrew220607 use div to allow buttons wrap
		msgboxVbox.appendChild(new Div() {{
			this.setHflex("1");
			this.setStyle("text-align:center;");
			for (ZkBiMsgboxButton btn : btns) {
				this.appendChild(btn);
				this.appendChild(new Space() {{  this.setWidth("5px"); }});
			}
		}});
		
		//set button focus and translation
		int setDefaultCnt = 0;
		for (ZkBiMsgboxButton btn : btns) {
			if (btn.isDefault) {
				btn.setFocus(true);
				setDefaultCnt++;
			}
		}
		//if no default, set first button as default
		if (setDefaultCnt == 0) {
			btns[0].setFocus(true);
		}
		
		//translate button label
		if (sh != null) {
			for (ZkBiMsgboxButton btn : btns) {
				if (StringUtils.isNotBlank(btn.getLabel())) {
					btn.setLabel(sh.getLabel(btn.getLabel()));
				}
			}
		}
		buildFlag = true;
		return this;
	}
	
	public ZkBiMsgbox appendStyle(String style) {
		ZkUtil.appendStyle(msgboxWin, style);
		return this;
	}

	public ZkBiMsgbox setStyle(String style) {
		msgboxWin.setStyle(style);
		return this;
	}

	public ZkBiMsgbox appendVboxStyle(String style) {
		ZkUtil.appendStyle(msgboxVbox, style);
		return this;
	}

	public ZkBiMsgbox setVboxStyle(String style) {
		msgboxVbox.setStyle(style);
		return this;
	}
	
	/***
	 * for close dlg win
	 */
	public void close() {
		if (msgboxWin != null) {
			Events.echoEvent(Events.ON_CLOSE, msgboxWin, null);
		}
	}
	
	
	public ZkBiMsgbox doModal() {
		if (msgboxWin != null) {
			msgboxWin.doModal();
		}
		return this;
	}
	
	public ZkBiMsgbox setTitle(String p_title) {
		msgboxWin.setTitle(p_title);
		return this;
	}
	
	public ZkBiMsgbox setCloseEventListener(EventListener<Event> listener) {
		ZkUtil.setEventListener(msgboxWin, Events.ON_CLOSE, listener);
		return this;
	}
	
	public ZkBiMsgbox setPosition(String pos) {
		msgboxWin.setPosition(pos);
		return this;
	}

	public ZkBiMsgbox setWinEventListener(String name, EventListener<?> listener) {
		ZkUtil.setEventListener(msgboxWin, name, listener);
		return this;
	}
	
	/***
	 * set content 
	 * TODO: support translation
	 * @param p_content
	 * @return
	 */
	public ZkBiMsgbox setContent(String p_content) {
		//z-messagebox-icon z-messagebox-exclamation z-div	
		Hbox contentHbox = new Hbox();
		contentHbox.setAlign("center");
		Div iconDiv = new Div();
		switch(type) {
			case info: iconDiv.setSclass("z-messagebox-icon z-messagebox-information"); break;
			case question: iconDiv.setSclass("z-messagebox-icon z-messagebox-question"); break;
			case warning: iconDiv.setSclass("z-messagebox-icon z-messagebox-exclamation"); break;
			case error: iconDiv.setSclass("z-messagebox-icon z-messagebox-error"); break;
			default: iconDiv.setSclass("z-messagebox-icon z-messagebox-information"); break;
		}
		
		contentHbox.appendChild(iconDiv);
		contentHbox.appendChild(new Separator() {{ this.setWidth("10px");}});
		Vbox contentTextVbox = new Vbox();
		contentHbox.appendChild(contentTextVbox);
		
		
		if (StringUtils.isBlank(p_content)) {
			UniLog.log1("content is blank");
			return this;
		}
		//String content = p_content;
		
		//translate the content
		String content = ZkBiTranslateHelper.getText(sh, p_content, "PATTERN", p_content);
		//UniLog.log1("translate %s -> %s", p_content, content);
		
		//handle newline
		for (String s : content.split("[\\r\\n]+")) {
			contentTextVbox.appendChild(new Label(s));
		}
		contentComp = contentHbox;
		return this;
	}
	
	/***
	 * set content
	 * @param p_contentComp
	 * @return
	 */
	public ZkBiMsgbox setContent(Component p_contentComp) {
		contentComp = p_contentComp;
		return this;
	}
	
	/***
	 * set dialog buttons
	 * @param p_btnNames
	 * @return
	 */
	public ZkBiMsgbox setButtons(String[] p_btnNames) {
		ArrayList<ZkBiMsgboxButton> btnList = new ArrayList<ZkBiMsgboxButton>();
		for (int i=0; i<p_btnNames.length; i++) {
			btnList.add(new ZkBiMsgboxButton(p_btnNames[i]));
		}
		btns = btnList.toArray(new ZkBiMsgboxButton[btnList.size()]);
		return this;
	}
	
	/***
	 * set dialog buttons
	 * @param p_btns
	 * @return
	 */
	public ZkBiMsgbox setButtons(ZkBiMsgboxButton[] p_btns) {
		btns = p_btns;
		return this;
	}
	
	/***
	 * set event listener
	 * @param p_el
	 * @return
	 */
	public ZkBiMsgbox setEventListener(ZkBiEventListener<Event> p_el) {
		el = p_el;
		return this;
	}
	
	public ZkBiMsgbox setType(Type p_type) {
		type = p_type;
		return this;
	}
	
	/***
	 * handy static method for display msgbox
	 * @param p_content
	 * @param p_btns
	 * @param p_el
	 */
	public static void show(String p_content, String[] p_btnNames, ZkBiEventListener p_el) {
		try {
			new ZkBiMsgbox().setContent(p_content).setButtons(p_btnNames).setEventListener(p_el).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void show(String p_content, ZkBiMsgboxButton[] p_btn, ZkBiEventListener p_el) {
		try {
			new ZkBiMsgbox().setContent(p_content).setButtons(p_btn).setEventListener(p_el).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void show(Component p_content, ZkBiMsgboxButton[] p_btn, ZkBiEventListener p_el) {
		try {
			new ZkBiMsgbox().setContent(p_content).setButtons(p_btn).setEventListener(p_el).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void show(Component p_content, String[] p_btnNames, ZkBiEventListener p_el) {
		try {
			new ZkBiMsgbox().setContent(p_content).setButtons(p_btnNames).setEventListener(p_el).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void show(Type p_type, String p_content, String[] p_btnNames, ZkBiEventListener p_el) {
		try {
			new ZkBiMsgbox().setType(p_type).setContent(p_content).setButtons(p_btnNames).setEventListener(p_el).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void show(String p_content, String[] p_btnNames) {
		try {
			new ZkBiMsgbox().setContent(p_content).setButtons(p_btnNames).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static void show(Type p_type, String p_content) {
		try {
			new ZkBiMsgbox().setType(p_type).setContent(p_content).setButtons(new String[] {"Close"}).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void show(String p_content) {
		try {
			new ZkBiMsgbox().setContent(p_content).setButtons(new String[] {"Close"}).build().doModal();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Component buildMsgboxContentComp(String message) {
		return new Div() {{
			setStyle("max-height:calc(100vh - 130px);overflow-y:auto");
			appendChild(new Vbox() {{
				for (String s : message.split("[\\r\\n]+")) {
					appendChild(new Label(s));
				}
			}});
		}};
	}
	
	public static void main(String args[]) throws Exception{
		
		//sample 1 - single button, no interaction
		ZkBiMsgbox.show("hello\nhow are you?");
		ZkBiMsgbox.show(ZkBiMsgbox.Type.error,"hello\nhow are you?");
		
		//sample 2 - with interaction
		ZkBiMsgbox.show("hello\nhow are you?",new String[]{"111","222"},new ZkBiEventListener<Event>(){
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
				UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
			}
		});
		
		//sample 3 - customize button color and position
		ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton("111"),new ZkBiMsgboxButton("222"), new ZkBiMsgboxButton("333").setDefault()};
		btns[2].setSclass("zkbi-deletebutton");
		btns[2].setStyle("margin-left:50px");
		new ZkBiMsgbox().setContent("hello?").setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
				UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
			}
		}).build().doModal();
		
	}
}
