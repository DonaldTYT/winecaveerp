package com.uniinformation.webcore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Box;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Span;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Vbox;

import com.uniinformation.utils.MapUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkcomp.ZkBiButton;

public class ActionButtonHelper {
	LinkedHashMap<Button,Map<Object,Object>> buttonHM = new LinkedHashMap<Button,Map<Object,Object>>();
	SessionHelper sh = null;
	String abmUUID = null;
	Component containerParent = null;
	private final static int DEF_SEQ = 1000;
	//AtomicInteger curSeq = new AtomicInteger(100);
	public static boolean fDebug = false;
	
	/***
	 * constructor
	 * @param p_sh
	 */
	public ActionButtonHelper(SessionHelper p_sh){
		UniLog.log1("constructor called");
		synchronized(buttonHM){
			buttonHM.clear();
		}
		sh = p_sh;
	}
	
	public ReturnMsg addButton(Button p_button){
		return(addButton(p_button, false, true, null,DEF_SEQ));
	}
	public ReturnMsg addButton(Button p_button, int p_seq){
		return(addButton(p_button, false, true, null, p_seq));
	}
	public ReturnMsg addButton(Button p_button, String p_iconName){
		return(addButton(p_button, false, true, p_iconName,DEF_SEQ));
	}
	public ReturnMsg addButton(Button p_button, String p_iconName, int p_seq){
		return(addButton(p_button, false, true, p_iconName, p_seq));
	}
	/***
	 * construct action button data structure
	 * button need onClick event
	 * @param p_button - button component
	 * @param p_delayClickEventFlag - true: delay click event avoid missing instant value
	 * @param p_iconName
	 * @param p_seq default seq is 1000, <1000 will place before default button
	 * @return
	 */
	public ReturnMsg addButton(Button p_button, boolean p_delayClickEventFlag, boolean p_attachFlag, String p_iconName, int p_seq){
		if (p_button == null) return ReturnMsg.defaultFail;
		synchronized(buttonHM){
			p_button.setAttribute("abHelperButtonSeq", p_seq);  //for display order
			p_button.setAttribute("abHelperButtonUUID", genButtonUUID());
			buttonHM.put(p_button, MapUtil.of("delayClickEventFlag",p_delayClickEventFlag,"attachFlag", p_attachFlag, "iconName", p_iconName));
		}
		return ReturnMsg.defaultOk;
	}
	public boolean checkButtonExist(Object p_btnObj) {
		if (!(p_btnObj instanceof Button)) return false;
		return buttonHM.get(p_btnObj) != null ? true : false;
	}
	
	public static int getOnClickEventCnt(Button p_button){
		Iterable<EventListener<? extends Event>> iter = p_button.getEventListeners("onClick");
		int eventCnt = 0;
		for (EventListener el : iter){
			eventCnt++;
		}
		return(eventCnt);
	}
	
	/***
	 * modify button onClick event to onDelayClickEvent
	 * allow call ActionHelper.setDelayClickEvent() or external
	 * @param p_button
	 * @param p_sh
	 * @return
	 */
	public static boolean setDelayClickEventOne(Button p_button, SessionHelper p_sh){
		if (p_sh == null || !p_sh.getAllowDelayClick()){
			UniLog.log1("sh is null or feature disabled");
			return false;
		}
		return(setDelayClickEventOne(p_button, p_sh.getDelayClickMS(), p_sh));
	}
	
	/***
	 * modify button onClick event to onDelayClickEvent
	 * @param p_button
	 * @return
	 */
	private static boolean setDelayClickEventOne(final Button p_button, final int p_delayClickMS, final SessionHelper sh){
		if (p_button == null){
			UniLog.log1("button is null");
			return false;
		}
		//status check
		if (StringUtils.equals((String)p_button.getAttribute("delayClickEventFlag"), "Y")){
			UniLog.log1("button cannot apply delayclickevent twice, ignore");
			return false;
		}
		p_button.setAttribute("delayClickEventFlag", "Y");
		
		//collect and remove original onClick event
		Iterable<EventListener<? extends Event>> iter = p_button.getEventListeners("onClick");
		ArrayList<EventListener> events = new ArrayList<EventListener>();
		for (EventListener el : iter){
			events.add(el);
			p_button.removeEventListener("onClick", el);
		}
		if (events.size() == 0){
			UniLog.log1("button without onClick event, ignore");
			return false;
		}

		//add onDelayClick event
		for (EventListener el : events){
			//change onClick event to onDelayClick event
			p_button.addEventListener("onDelayClick", el);

			//create new onClick event will delay feature
			p_button.addEventListener("onClick", new EventListener(){
				@Override
				public void onEvent(final Event event) throws Exception {
					//UniLog.log1("before sleep");
					new ZkBiAbstractLongOp(p_button,sh.getLabel("Processing. Please wait..."),p_delayClickMS){
						@Override
						public ReturnMsg longOp() {
							//UniLog.log1("after sleep");
							//Events.echoEvent("onDelayClick", event.getTarget(), event.getData());
							Events.sendEvent("onDelayClick", event.getTarget(), event.getData()); //andrew211216 wait for result
							return ReturnMsg.defaultOk;
						}

					};

				}});
		}
		
		return(true);
	}
	
	/***
	 * set delay click event for all button
	 */
	public void setDelayClickEvent(){
		List<Button> buttons = new ArrayList<Button>(buttonHM.keySet());
		for (final Button button : buttons){
			Map optionHM = buttonHM.get(button);
			boolean delayClickEventFlag = MapUtil.getBoolean(optionHM, "delayClickEventFlag",false);
			if (fDebug) UniLog.log1("DEBUG:id:%s:delayClickEventFlag:%s",button.getId(),delayClickEventFlag);
			
			//modify onClick event if needed
			if (delayClickEventFlag){
				setDelayClickEventOne(button, sh);
			}
		}
	}
	
	private List<Button> getSortedButtonList() {
		List<Button> buttons = new ArrayList<Button>(buttonHM.keySet());
		Collections.sort(buttons, new Comparator<Button>(){

			@Override
			public int compare(Button p_button0, Button p_button1) {
				int seq0 = DEF_SEQ;
				int seq1 = DEF_SEQ;
				if (p_button0.getAttribute("abHelperButtonSeq") instanceof Integer){
					seq0 = (Integer) p_button0.getAttribute("abHelperButtonSeq");
				}
				if (p_button1.getAttribute("abHelperButtonSeq") instanceof Integer){
					seq1 = (Integer) p_button1.getAttribute("abHelperButtonSeq");
				}
				return (seq0 - seq1);
			}
		});
		return buttons;
	}
	
	/***
	 * attach buttons to target container 
	 * @param p_parentComp
	 */
	private void attachButtons(Component p_parentComp){
		List<Button> buttons = getSortedButtonList();
		
		for (final Button button : buttons){
			Map optionHM = buttonHM.get(button);
			boolean attachFlag = MapUtil.getBoolean(optionHM, "attachFlag",true);
			//attach button to new parent
			if (attachFlag){
				/*
				for (Component child : p_parentComp.getChildren()){
					if (child.equals(button)){
						UniLog.log1("button %s attached already, skip", button.getId());
						continue;
					}
				}
				*/
				button.detach();
				if (fDebug) UniLog.log1("attach %s", button.getId());
				p_parentComp.appendChild(button);
			}
		}
	}
	
	/***
	 * set containerParent of buttons
	 * @param p_containerParent
	 */
	public void setContainerParent(final Component p_containerParent){
		if (p_containerParent == null) {
			return;
		}
		containerParent = p_containerParent;
		
		//add event listener to handle Action Button Click
		boolean hasEL = false;
		Iterable<EventListener<? extends Event>> iter = p_containerParent.getEventListeners("onABMClick");
		for (EventListener el : iter){
			hasEL = true;
			break;
		}
		
		if (!hasEL){
			String uuid = genContainerUUID();
			p_containerParent.setId(uuid);

			p_containerParent.addEventListener("onABMClick", new EventListener(){
				@Override
				public void onEvent(Event event) throws Exception {
					Component targetComp = p_containerParent.getFellowIfAny(event.getData() + "");
					if (ZkBiAbstractLongOp.isBusy(targetComp)){
						ZkUtil.showWarnMsg("Function disabled.");
						return;
					}
					if (targetComp == null){
						ZkUtil.showWarnMsg("Function disabled.");
						UniLog.log1("targetComp not found, skip event." + event.getData());
						return;
					}
					if (!targetComp.isVisible()) {
						ZkUtil.showWarnMsg("Function disabled.");
						UniLog.log1("targetComp not visible, skip event. " + event.getData());
						return;
					}
					UniLog.log1("send onClick event to %s", targetComp.getId());
					Events.echoEvent("onClick", targetComp, null);
				}
			});
		}
		
	}
	
	/***
	 * attach buttons to container. allow multiple run
	 */
	public void attachButtonsToContainer(){
		if (containerParent == null){
			UniLog.log1("containerParent is null. action ignore");
			return;
		}
		//generateActionButtonMenu();
		
		/*
		//special handle for toolbar layout. seems cannot put a box under toolbar, wrong alignment
		if (containerParent instanceof Toolbar){
			attachButtons(containerParent);
			return containerParent;
		}
		Box box = null;
		if (sh == null || !sh.isMobileDevice()){
			box = new Hbox();
		}
		else{
			//box = new Vbox();
			box = new Hbox();
		}
		//ZkUtil.appendStyle(box,"background:#FFF;border:1px solid #FFF;"); //white background for debug purpose
		attachButtons(box);
		box.setParent(containerParent);
		return(box);
		*/
		
		
		attachButtons(containerParent);
	}
	
	
	/*
	@Override
	protected void finalize() throws Throwable {
		try{
			UniLog.log1("clean up");
		}
		catch(Throwable t){
			throw t;
		}
		finally{
			super.finalize();
		}
	}
	*/
	
	public static void main(String args[]){
		Div div = new Div();
		Button btn = new ZkBiButton();
		btn.setId("xxx");
	    btn.addEventListener("onClick", new EventListener(){
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}});
		ActionButtonHelper abh = new ActionButtonHelper(null);
		abh.addButton(btn);
		abh = null;
		System.gc();
		UniLog.log1("called");
		
	}
	/*
	public void setVisible(boolean p_flag) {
		if (StringUtils.isBlank(abmUUID)) {
			return;
		}
		if (p_flag) {
			ZkUtil.js("$('#%s').show();", abmUUID);
		}
		else {
			ZkUtil.js("$('#%s').hide();", abmUUID);
		}
	}
	*/
	public void removeActionButtonMenu() {
		if (abmUUID == null){
			if (fDebug) UniLog.log1("menu not exsit");
			return;
		}
		ZkUtil.js("$('#%s').remove();", abmUUID);
		abmUUID = null;
	}
	
	public void showActionButtonMenu(){
		if (!sh.getAllowActionButtonMenu()) return;
		if (containerParent == null) {
			UniLog.log("containerParent is null");
			return;
		}
		
		//remove old menu if any
		removeActionButtonMenu();
		
		//generate a new menu id
		abmUUID = genMenuUUID();
		
		//create menu
		StringBuilder sb = new StringBuilder();
		sb.append("<html xmlns='native'>");
		sb.append("<span id=\""+abmUUID+"\" title=\"Draggable Action Panel\">");
		
		
		//andrew190802: add text-align:left fix icon alignment problem
		if (sh != null && sh.isMobileDevice()){
			sb.append("<ul class='mfb-component--br mfb-zoomin' data-mfb-toggle='click' data-mfb-state='closed' style='opacity:0.95; user-select:none; text-align:left;'>");
		}
		else{
			sb.append("<ul class='mfb-component--br mfb-zoomin' data-mfb-toggle='hover' data-mfb-state='closed' style='opacity:0.95; user-select:none; text-align:left;'>");
		}
		sb.append("<li class='mfb-component__wrap'>");
		sb.append("  <a href='javascript:void(0);' class='mfb-component__button--main'>");
		sb.append("    <i class='mfb-component__main-icon--resting fa fa-bars'></i>");
		sb.append("    <i class='mfb-component__main-icon--active fa fa-minus'></i>");
		sb.append("  </a>");
		
		
		sb.append("  <ul class='mfb-component__list'>");
		List<Button> buttons = getSortedButtonList();
		sb.append("    <li class='abm_nodrag'>");
		sb.append(String.format("      <a href='javascript:abmHideToggle()' data-mfb-label='%s' class='mfb-component__button--child'>", sh.getLabel("Hide this menu")));
		sb.append("        <i class='mfb-component__child-icon fa fa-eye-slash'></i>");
		sb.append("      </a>");
		sb.append("    </li>");
		
		for (Button button : buttons){
			//if (!button.isVisible() || button.isDisabled()) continue;
			if (!button.isVisible()) continue;
			String buttonUUID = (String) button.getAttribute("abHelperButtonUUID");
			if (buttonUUID == null) continue;
			String listItemId = "li-" + buttonUUID;
			Map optionHM = buttonHM.get(button);
			String iconName = MapUtil.getString(optionHM, "iconName","fa-circle");
			boolean attachFlag = MapUtil.getBoolean(optionHM, "attachFlag",true);
			
			//String label = button.getLabel();
			String label = StringUtil.convertWebString(button.getLabel()); //andrew230223: fix label contain special char (e.g. &)
			
			if (button.isDisabled()){
				sb.append("    <li class='abm_item_disable abm_nodrag' id='"+listItemId+"'>");
			}
			else{
				sb.append("    <li class='abm_nodrag' id='"+listItemId+"'>");
			}
			//sb.append("      <a href='javascript:abmClick(\""+containerParent.getId()+"\", \""+button.getId()+"\")' data-mfb-label='"+label+"' class='mfb-component__button--child'>");
			sb.append(String.format("      <a href='javascript:abmClick(\"%s\",\"%s\",\"%s\")' data-mfb-label='%s' class='mfb-component__button--child'>", containerParent.getId(),button.getId(),listItemId,label));
			sb.append("        <i class='mfb-component__child-icon fa "+iconName+"'></i>");
			sb.append("      </a>");
			sb.append("    </li>");
			
			if (sh.getHideOrgActionButton() && attachFlag){
				ZkUtil.appendStyle(button, "display:none"); //hide org button if button displayed in menu
			}
		}
		
		sb.append("  </ul>");
		sb.append("</li>");
		sb.append("</ul>");
		sb.append("<script type='text/javascript' src='js/mfb-0.12.js'></script>");		
		if (sh.getAllowTouchDragFloatingActionButton())
			sb.append("<script type='text/javascript' src='js/jquery.ui.touch-punch.js'></script>");		
		
		//make the action panel draggable. remark: drag first li element. required to set right,bottom=auto as jquery draggable only control top,left property.
		sb.append("<script type='text/javascript'>");
		sb.append("$('#"+abmUUID+">ul').draggable({'cancel':'.abm_nodrag', 'containment':'window', 'start':function(event,ui){$(this).css({'right':'auto','bottom':'auto'});}});");
		sb.append("</script>");
		
		sb.append("</span>");
		sb.append("</html>");
		//Executions.getCurrent().createComponentsDirectly(sb.toString(), null, containerParent, null);
		Executions.getCurrent().createComponentsDirectly(sb.toString(), null, ZkUtil.getRootComponent(containerParent), null);  //andrew200417: fix button blocked by borderlayout
		
		
		//handle button disable / enable
		//event issued from aspectj ActionButtonAJ
		ZkUtil.receiveDesktopEvent(new EventListener(){
			@Override
			public void onEvent(Event event) throws Exception {
				if (event.getData() instanceof Map){
					String action = MapUtil.getString(event.getData(), "action");
					if (!StringUtils.equalsAny(action,"BUTTON_DISABLE","BUTTON_ENABLE")) {
						UniLog.log1("ignore action %s", action);
						return;
					}
					Component comp = (Component) MapUtil.getObject(event.getData(), "comp");
					
					//validation
					if (comp == null){
						//UniLog.log1("comp is null");
						return;
					}
					String buttonUUID = (String) comp.getAttribute("abHelperButtonUUID");
					if (buttonUUID == null || buttonHM.get(comp) == null) {
						//UniLog.log1("ignore non action button:" + comp +":"+comp.getId());
						return;
					}
					String listItemId = "li-" + buttonUUID;
					
					//rocess action
					if (StringUtils.equals(action,"BUTTON_DISABLE")){ 
						ZkUtil.js("$('#%s').addClass('abm_item_disable');", listItemId);
					}
					else if (StringUtils.equals(action,"BUTTON_ENABLE")){
						ZkUtil.js("$('#%s').removeClass('abm_item_disable');", listItemId);
					}
				}
				
		}});
	}
	
	private String genContainerUUID(){
		return("abc-" + UUID.randomUUID().toString());
	}
	private String genMenuUUID(){
		return("abm-" + UUID.randomUUID().toString());
	}
	private String genButtonUUID(){
		return("abb-" + UUID.randomUUID().toString());
	}
}
