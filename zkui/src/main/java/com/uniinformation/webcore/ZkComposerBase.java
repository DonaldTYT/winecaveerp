package com.uniinformation.webcore;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
//import com.uniinformation.zkbi.ZkBiAuService;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
//import com.uniinformation.zkbi.ZkBiAuService.ZkBiAuEvent;
 
public class ZkComposerBase implements Composer<Component> {
  	protected ZkSessionHelper sessionHelper;
  	protected Component rootComp;
  	protected boolean autoWire = true;
  	protected boolean accessOkFlag = false; //for abort the doAfterCompose execution.
  	protected String desktopId = "";
  	protected String customLoginUrl = null;
  	//private Map<String,Object> urlParams = new HashMap<String, Object>(); //andrew210507 logic moved to sessionHelper
  	private Map<String,Object> urlParams = new HashMap<String, Object>();
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		rootComp = p_comp;
		
		//UniLog.log1("Desktop:" + Executions.getCurrent().getDesktop() );
		if (autoWire) {
			Selectors.wireComponents(p_comp, this, false);  //important for wire variable
		}
		

		if (StringUtils.isBlank(p_comp.getId())){
			p_comp.setId(UUID.randomUUID().toString());
		}

		//obtain session helper
   		sessionHelper = ZkSessionHelper.getSessionHelper();		
   		
		//common validation logic
   		if(sessionHelper == null) {
   			UniLog.log("sessionHelper is null");
   			redirectToLogin();
   			return;
   		}
   		desktopId = p_comp.getDesktop().getId();
		sessionHelper.addActiveDesktop(p_comp.getDesktop().getId());
		
		//for monitor desktop cleanup only
		p_comp.getDesktop().addListener(new DesktopCleanup() {
			@Override
			public void cleanup(Desktop p_desktop) throws Exception {
				//UniLog.log1("cleanup desktop:%s", p_desktop.getId());
				sessionHelper.deleteActiveDesktop(p_desktop.getId());
			}            
			
		});
		
		if(!sessionHelper.isLogin()) {
   			UniLog.log("sessionHelper not logged in");
   			if(customLoginUrl != null && StringUtils.isBlank(customLoginUrl)) {
   				
   			} else {
   				redirectToLogin();
   				return;
   			}
   		}
		
		
		//validate URL
		/*
		String requestURL = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getServletPath().substring(1) + "?" +
						  URLDecoder.decode(((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getQueryString(),"ISO8859-1");
		*/
		String requestURL = ZkUtil.getURL();
		accessOkFlag = validateURL(requestURL);
		UniLog.log1("loginId:"+sessionHelper.getLoginId() + " url:" + requestURL + " access:" + accessOkFlag);
		
		Map<?, ?> argMap = Executions.getCurrent().getArg();
		boolean isWidget = (argMap != null && StringUtils.equals((String)argMap.get("widget"), "Y"));
		
		if(requestURL.startsWith("custom_menu.html") || isWidget) {
			accessOkFlag = true;
		}

		//set pageid
		ZkUtil.setPageId(p_comp);
		
		//log access for allow and deny
		ZkBiLogHelper.logAccess(sessionHelper, rootComp, accessOkFlag ? ETYPE.ACCESS_PAGE : ETYPE.ACCESS_DENIED, String.format("url:%s", requestURL));
		if(!accessOkFlag) {
			UniLog.log1("URL access denied");
			Messagebox.show(
					sessionHelper.getLabel("Access Denied"),
					sessionHelper.getLabel("Warning"), 
					Messagebox.OK , 
					Messagebox.EXCLAMATION, 
					new org.zkoss.zk.ui.event.EventListener() {
						public void onEvent(Event evt) throws InterruptedException {
							Executions.getCurrent().sendRedirect(sessionHelper.getLandingPage());	
						}
					});	
			return;	
		} 				
		
		
		//receive broadcast message
		ZkUtil.receiveBroadcast(sessionHelper);
		
		//register client Info Event
		ZkUtil.registerClientInfoEvent((HtmlBasedComponent)rootComp,sessionHelper,adjustRootCompWidth(),adjustRootCompWidthOffset());
		
		if (sessionHelper.getAllowSyslog()){
			Clients.evalJavaScript("Logger.show();");
		}
		//handle translate click event
		ZkBiTranslateHelper.addOnUpdateTranslateEventListener(p_comp, sessionHelper);
		
		//test receive desktop event
		/*
		//andrew200616 obsoleted. receive desktop level client side event, replacted by root component event
		if (sessionHelper.getAllowZkBiAu()) {
			ZkBiAuService.build(p_comp).addEventListener("onStartup", new ZkBiAuEvent() {
				@Override
				public void onEvent(String p_evName, Map<String, Object> p_data) {
					UniLog.log1("haha called: name:%s data:%s", p_evName, p_data);
				}
			});		
		}
		*/
		
		
		
		//andrew200720: trigger a gc for fix artway oom issue (Experimental. It may has side effect)
		//System.gc(); //andrew200806: it's useless, artway oom problem due to large biresult and too many desktop per session
		
		//store the parameter map
		urlParams.putAll(Executions.getCurrent().getParameterMap());
		
		//store one more copy of parameter map to comp
		UniLog.log1("isWidget:%b, p_comp:%s, getArg:%s, getParameterMap:%s, getURL:%s", isWidget, p_comp, Executions.getCurrent().getArg(), Executions.getCurrent().getParameterMap(), ZkUtil.getURL());
		if (isWidget)
			ZkUtil.setArgsToComp(p_comp);
		else
			ZkUtil.setURLParamsToComp(p_comp);
		
	}
	
	/***
	 * add event listener to root component
	 * @param evtnm
	 * @param listener
	 */
	public void addEventListener(String evtnm, EventListener<? extends Event> listener) {
		rootComp.addEventListener(evtnm, listener);
	}
	protected boolean adjustRootCompWidth() {
		return true;
	}
	protected int adjustRootCompWidthOffset() {
		return 0;
	}
	
	/***
	 *  basic url validation
	 *  allow override
	 */
	protected boolean validateURL(String p_requestURL) {
		UniLog.log1("called");
		try {
			return sessionHelper.checkWebMenuAccess(p_requestURL);
		} 
		catch (Exception ex){
			UniLog.log(ex);
		}
		return false;
	}
	
	
	public SessionHelper getSessionHelper() {
		return(sessionHelper);
	}
	public Component getRootComponent() {
		return(rootComp);
	}

    public void redirectToLogin() {
    	if(customLoginUrl != null) {
    		if(!StringUtils.isBlank(customLoginUrl)) {
    			Executions.getCurrent().sendRedirect(customLoginUrl);
    		}
    	} else {
    		Executions.getCurrent().sendRedirect("/login.html");
    	}
    }
    public Map<String,Object> getURLParams(){
    	return urlParams;
//    	return sessionHelper.getURLParams();
    }
    public String getURLParam(String p_key) {
	  	Object obj = urlParams.get(p_key);
	  	if (obj == null) {
	  		return null;
	  	}
	  	else if (obj instanceof String) {
	  	   return (String) obj;
	  	}
	  	else if (obj instanceof String[] && ((String []) obj).length > 0) {
	  		return ((String []) obj)[((String []) obj).length-1];
	  	}
	  	return null;
 //   	return sessionHelper.getURLParam(p_key);
    }
    public boolean isWidget() {
    	//UniLog.log1("HAHA999 session:%s url:%s", getURLParam("widget"), ZkUtil.getURLParamFromComp(rootComp, "widget"));
    	
    	//obtain urlparam from comp. don't obtain from session to handle multiple tab
    	String widgetParam = ZkUtil.getURLParamFromComp(rootComp, "widget");
    	return StringUtils.equalsAnyIgnoreCase(widgetParam, "Y","TRUE");
    	
    }
    public String getDesktopId() {
    	return desktopId;
    }
}
