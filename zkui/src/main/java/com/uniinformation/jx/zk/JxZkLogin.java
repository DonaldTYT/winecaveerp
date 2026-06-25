package com.uniinformation.jx.zk;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.MessageboxDlg;

import com.kyoko.common.*;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.FilingUtil.FilingUtilPropObj;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.webcore.BanIpHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkcomp.S2Listbox;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.zk.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
public class JxZkLogin extends SelectorComposer<Window> {
	@Wire 
	private Textbox txPassword;
	@Wire 
	private Textbox txLoginId;
	@Wire
	Button btReset;
	@Wire
	Button btSubmit;
	@Wire
	Checkbox cbKeepMeSignedIn;
	//@Wire
	//Label lbLoginId;
	//@Wire
	//Label lbPassword;
	@Wire
	Auxheader ahSystemLogin;
	@Wire
	Label lbSystemLogin;
	@Wire 
	Window winMain;
	@Wire
	Auxhead auxhead;
	@Wire
	Listbox s2Agents;
	@Wire
	Image co_logo;
	@Wire
	Html htmlSysEnv;
	
	
	String targetURL = "";
	SessionHelper sessionHelper = null;
	String remoteAddr = "";
	protected boolean isDeviceLogin = false;
	
	
	@Override
	public void doAfterCompose(final Window comp) throws Exception {
		super.doAfterCompose(comp);
		if(isDeviceLogin) {
			targetURL = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getRequestURL().toString();
			String  queryString = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getQueryString();
			if(queryString != null) targetURL += "?" + queryString;
		} else {
		targetURL = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getParameter("targetURL");
		if (targetURL != null){
			//targetURL = URLDecoder.decode(targetURL,"UTF-8");  //cannot handle chinese title
			targetURL = new String((new org.apache.commons.codec.binary.Base64(true)).decodeBase64(targetURL.getBytes("UTF-8")));	 //fix chinese title problem, encoding doesn't matter as its base64
		}
		}
		
    	sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
    	if(!sessionHelper.isValidAgent()) {
    		throw new Exception("Invalid agent");
    	}
    	if(isDeviceLogin) {
    		sessionHelper.setLogoutURL(targetURL);
    	}
   		remoteAddr = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getRemoteAddr();
    	
		
		//disable all button until onTimer event
		btReset.setDisabled(true);
		btReset.setLabel(sessionHelper.getLabel("Clear"));
		//btReset.setStyle("display:none");
		setLoginTokenInput(false);
		btSubmit.setDisabled(true);
		btSubmit.setLabel(sessionHelper.getLabel("Login"));
		cbKeepMeSignedIn.setLabel(sessionHelper.getLabel("Keep me signed in"));
		if (!sessionHelper.getAllowLoginToken()){
			cbKeepMeSignedIn.setChecked(false);
			cbKeepMeSignedIn.setDisabled(true);
		}
		if (lbSystemLogin != null){
			lbSystemLogin.setValue(sessionHelper.getLabel("System Login"));
		}
		else{
			ahSystemLogin.setLabel(sessionHelper.getLabel("System Login"));
		}
		
		if(co_logo != null) {
			String defLogoFilename = BiConfig.getDefaultLogo(sessionHelper);
			if(StringUtils.isNotBlank(defLogoFilename)) {
				co_logo.setSrc("images/" + defLogoFilename);
				co_logo.setVisible(true);
				
				if(BiConfig.getString(sessionHelper, "LogoWidth") != null) {
					co_logo.setWidth(BiConfig.getString(sessionHelper, "LogoWidth"));
				}
			}
			else {
				auxhead.setVisible(true);
			}
		}
		
		
		//lbLoginId.setValue(sessionHelper.getLabel("Login Id"));
		txLoginId.setPlaceholder(sessionHelper.getLabel("Login Id"));
		//lbPassword.setValue(sessionHelper.getLabel("Password"));
		txPassword.setPlaceholder(sessionHelper.getLabel("Password"));
		if (comp.hasFellow("comboLang")){
			final Combobox comboLang = (Combobox)comp.getFellow("comboLang");
			comboLang.setValue(sessionHelper.getLabel("Language"));
			
			comboLang.appendChild(new Comboitem("English"){{this.setValue("eng");}});
			comboLang.appendChild(new Comboitem("\u7E41\u9AD4\u4E2D\u6587"){{this.setValue("tchn");}});
			comboLang.appendChild(new Comboitem("\u7C21\u9AD4\u4E2D\u6587"){{this.setValue("schn");}});
			comboLang.appendChild(new Comboitem("\u65E5\u672C\u8A9E"){{this.setValue("jap");}});
			comboLang.appendChild(new Comboitem("\uD55C\uAD6D\uC5B4"){{this.setValue("kor");}});
			comboLang.addEventListener(Events.ON_SELECT, new ZkBiEventListener() {
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.logm(null,"comoLang %s %s", comboLang.getSelectedItem().getLabel(), comboLang.getSelectedItem().getValue());
					Executions.getCurrent().sendRedirect("login.html?lhlang="+comboLang.getSelectedItem().getValue());
				}
			});
			
		}
		
		//perform auto login
		new ZkBiAbstractLongOp(winMain, null){
			@Override
			public ReturnMsg longOp() {
				btReset.setDisabled(false);
				txLoginId.setFocus(true);
				txLoginId.setInstant(true);
				login(sessionHelper, true);   //login() perform within event listener to fix setCookie problem.
				return null;
			}
		};
		
       	txLoginId.addEventListener("onChange",
	             	new ZkBiEventListener() {
	     				public void onZkBiEvent(Event event) throws Exception {
	     					txLoginId.setValue(txLoginId.getValue().toLowerCase());
	     					UniLog.log("Login ID Changed ["+txLoginId.getValue()+"]");
	     					btSubmitRefresh();
	     				}
	     			} 
       	);
       	txLoginId.addEventListener("onOK",
	             	new ZkBiEventListener() {
	     				public void onZkBiEvent(Event event) throws Exception {
	     					//UniLog.log("enter pressed 1");
	     					if (!txLoginId.getText().isEmpty()){
	     						//txPassword.setConstraint("");
	     						txPassword.setFocus(true);
	     						//txPassword.setConstraint("no empty");
	     					}
	     				}
	     			} 
       	);
       	txPassword.addEventListener("onOK",
	             	new ZkBiEventListener() {
	     				public void onZkBiEvent(Event event) throws Exception {
	     					//UniLog.log("enter pressed");
	     					if (!btSubmit.isDisabled())
	     						Events.postEvent(Events.ON_CLICK, btSubmit, null);
	     				}
	     			} 
       	);
       	btReset.addEventListener("onClick",
	             	new ZkBiEventListener() {
	     				public void onZkBiEvent(Event event) throws Exception {
	     					//UniLog.log("Reset Pressed");
	     					//txLoginId.setConstraint("");
	     					txLoginId.setValue("");
	     					//txLoginId.setConstraint("no empty");
	     					//txPassword.setConstraint("");
	     					txPassword.setValue("");
	     					//txPassword.setConstraint("no empty");
	     					btSubmitRefresh();
	     					
	     					
	     					UniLog.log1("clear login token");
	     					sessionHelper.clearLoginToken((HttpServletRequest) Executions.getCurrent().getNativeRequest(), (HttpServletResponse) Executions.getCurrent().getNativeResponse());
	     					
	     					setLoginTokenInput(false);
	     					if (!txLoginId.isReadonly()) {
	     						txLoginId.setFocus(true);
	     					}
	     					
	     				}
	     			} 		 
	        	);
       	
      	btSubmit.addEventListener("onClick",
             	new ZkBiEventListener() {
     				public void onZkBiEvent(Event event) throws Exception {
     					//UniLog.log("Submit Pressed");
     					login(sessionHelper,false);
     				}
     			} 		 
        	);
      	
      	if (s2Agents != null) {
      		s2Agents.setAttribute("placeholder", "Agent");

      		//set selected index
      		String paramAgent = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getParameter("agent");
      		if (sessionHelper.getAllowShowAgents()) {

      			int selectedIdx = -1;
      			//construct the listbox
      			int idx = -1;
      			for (final String tmpAgent : sessionHelper.getAgents()) {
      				idx++;
      				if (StringUtils.isBlank(tmpAgent)) {
      					continue;
      				}
      				if (StringUtils.equalsIgnoreCase(paramAgent, tmpAgent)) {
      					selectedIdx = idx;
      				}
      				s2Agents.appendChild(new Listitem(tmpAgent){{this.setValue(tmpAgent);}});
      			}
      			s2Agents.setSelectedIndex(selectedIdx);
      			ZkUtil.setupSelect2(s2Agents, true);
      		}
      		else {
      			//s2Agents.appendChild(new Listitem(sessionHelper.getAgent()));
      			//s2Agents.setSelectedIndex(0);
      			s2Agents.setVisible(false);
      		}
      		
      		
      		s2Agents.addEventListener(Events.ON_SELECT, new ZkBiEventListener() {
      			public void onZkBiEvent(Event event) throws Exception {
      				UniLog.log1("s2Agents %s %s", s2Agents.getSelectedItem().getLabel(), s2Agents.getSelectedItem().getValue());
      				Executions.getCurrent().sendRedirect("login.html?agent="+s2Agents.getSelectedItem().getValue());
      			}
      		});
      		
      		try {
      			String coName = sessionHelper.getDbLocation();
      			if(sessionHelper.getWebPageCoName() != null && !sessionHelper.getWebPageCoName().isEmpty()) {
      				if(!coName.isEmpty()) coName += ":";
      				coName += sessionHelper.getWebPageCoName();
      			}
      			ZkUtil.js("$('#spanWebPageCoName').text(\"%s\")",StringUtil.jsString(coName));
      		}
      		catch(Exception ex) {
      			ZkUtil.showErrMsg("Cannot load webpage coname");
      			ex.printStackTrace();
      		}
      		try {
   				ZkUtil.js("$('#spanWebPageName').text(\"%s\")",StringUtil.jsString(sessionHelper.getWebPageName()));
      		}
      		catch(Exception ex) {
      			ZkUtil.showErrMsg("Cannot load webpage name");
      			ex.printStackTrace();
      		}
      	}
      	
      	if (htmlSysEnv != null && sessionHelper.getAllowShowSysEnv()) {
      		try {
      			StringBuilder sb = new StringBuilder();
      			sb.append(String.format("<span>Version: %s</span><br>", sessionHelper.getVersionId()));
   				sb.append(String.format("<span>Agent: %s</span><br>", sessionHelper.getAgent()));
   				sb.append(String.format("<span>Lang: %s</span><br>", sessionHelper.getLHLang()));
   				
   				try {
   					//default assume from db
					boolean sameDay = true;
					String schemaFile = "latest";
					String schemaTS = "latest";
					
					//check if has xml
   					if (StringUtils.isNotBlank(sessionHelper.getSchemaXML())) {
   						File xmlFile = new File(sessionHelper.getSchemaXML());
   						if (xmlFile.exists()) {
   							Date xmlDate = new Date(xmlFile.lastModified());
   							sameDay = DateUtils.isSameDay(new Date(), xmlDate);
   							schemaFile = sessionHelper.getSchemaXML();
   							schemaTS = DateUtil.dateToDateTimeStr(new Date(xmlFile.lastModified()));
   						}
   					}
					sb.append(String.format("<span>Schema File: <span style='%s'>%s</span></span><br>", sameDay ? "color:#54bc53" : "color:#dbbb5b",schemaFile));
   					sb.append(String.format("<span>Schema Timestamp: <span style='%s'>%s</span></span><br>", sameDay ? "color:#54bc53" : "color:#dbbb5b", schemaTS));
   				}
   				catch(Exception ex) {
   					UniLog.log1("error:" + ex.getMessage());
   				}
      			htmlSysEnv.setContent(sb.toString());
      		}
      		catch(Exception ex) {
      			UniLog.log1("error:" + ex.getMessage());
      		}
      	}
      	
   		ZkUtil.registerClientInfoEvent(comp, sessionHelper, false, 0, false);   //andrew230609 to minimize the side effect, do not enable jsIdleCtrl
   					
      
		ZkBiLogHelper.logAccess(sessionHelper, comp, ETYPE.ACCESS_PAGE, this.getClass().toString());
	}
	private void btSubmitRefresh(){
		if(txLoginId == null || btSubmit == null)
			return;
     	//txLoginId.setConstraint("");
		if (txLoginId.getText().trim().isEmpty()) 
				btSubmit.setDisabled(true); 
			else 
				btSubmit.setDisabled(false);
     	//txLoginId.setConstraint("no empty");
	}
	public void login(SessionHelper p_sh, final boolean p_loginByTokenFlag){
		/*
		//old long operation 
		Clients.showBusy(winMain, p_sh.getLabel("Login in progress..."));
		Events.echoEvent("onLogin", winMain, p_loginByTokenFlag);
		*/
		new ZkBiAbstractLongOp(winMain, p_sh.getLabel("Login in progress...")){
			@Override
			public ReturnMsg longOp() {
				try{
					login(p_loginByTokenFlag);
					return new ReturnMsg(true);
				}
				catch(Exception ex){
					ex.printStackTrace();
					return new ReturnMsg(false,ex);
				}
			}
		};
	}
	/*
	//old long operation
	@Listen("onLogin=#winMain")
	public void login(Event p_event) throws Exception {
		boolean p_loginByTokenFlag = (Boolean) p_event.getData();
	*/
	private void setLoginTokenInput(boolean p_flag) {
		txLoginId.setReadonly(p_flag);
		txPassword.setReadonly(p_flag);
		if (p_flag) {
			txLoginId.setTooltiptext(sessionHelper.getTtLabel("Readonly. You can click Clear Button to remove it"));
			txPassword.setTooltiptext(sessionHelper.getTtLabel("Readonly. You can click Clear Button to remove it"));
			btReset.setTooltiptext(sessionHelper.getTtLabel("Clear Input and Remeber Me"));
			btReset.setStyle("");
			btReset.setSclass("zkbi-deletebutton");
		}
		else {
			txLoginId.setTooltiptext("");
			txPassword.setTooltiptext("");
			btReset.setStyle("display:none");
		}
	}
	
	//this block of code is slow
	public void login(boolean p_loginByTokenFlag) throws Exception {
		final ReturnMsg loginResult;
		final HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		final HttpServletResponse response = (HttpServletResponse) Executions.getCurrent().getNativeResponse();
		final String loginId = txLoginId.getText().trim();
		String password = txPassword.getText().trim();
		final boolean keepMeSignedIn = (cbKeepMeSignedIn != null && cbKeepMeSignedIn.isChecked());
    	if (p_loginByTokenFlag){
    		if (sessionHelper.getAllowLoginTokenConfirm()) { 
    			
    			//ltConfirm step 1/2 
    			ReturnMsg checkLoginToken = sessionHelper.loginByToken(request, response, true);
    			if (checkLoginToken.getStatus()) {
    				txLoginId.setValue((String)checkLoginToken.getData());
    				txPassword.setValue("LOGIN_BY_TOKEN");
    				setLoginTokenInput(true);
    				
    				btSubmit.setFocus(true);
    				btSubmitRefresh();
    			}
    			return;
    		}
    		else {
    			
    			//classic auto login
    			loginResult = sessionHelper.loginByToken(request, response);
    			if (!loginResult.getStatus()){
    				return;
    			}
    		}
    	}
    	else if (txPassword.isReadonly() && StringUtils.equals(txPassword.getValue(),"LOGIN_BY_TOKEN")){
    		//ltConfirm step 2/2
   			loginResult = sessionHelper.loginByToken(request, response);
   			if (!loginResult.getStatus()){
   				return;
   			}
   		}
    	else{
    		if (!checkTooManyAttempt()) return;
    		loginResult = sessionHelper.login(request, response, loginId, password);
    		UniLog.log1("login:%s addr:%s result:%s",txLoginId.getText(), remoteAddr, loginResult);
    		if (loginResult.getStatus() && !sessionHelper.twoFactorIsPass()) {
    			Vbox div = new Vbox();
    			//div.setWidth("100%");
    			//div.setHeight("calc(100% - 45px)");
    			//div.setStyle("overflow:auto;");
    			div.setAlign("center");
    			div.appendChild(new Label("You can get a verification code from Google Authenticator App"));
    			final Textbox txCode = new Textbox();
    			txCode.setPlaceholder("Enter 6 digit Code");
    			div.appendChild(new Space());
    			txCode.setWidth("150px");
    			div.appendChild(txCode);
    			div.appendChild(new Space());
				MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("2-Step Verification", 
					div, 
					new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
					winMain, 
					new ZkBiEventListener<Messagebox.ClickEvent>() {
						@Override
						public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
							if (!checkTooManyAttempt()) return;
							if (event.getButton() == Messagebox.Button.OK) {
								UniLog.log1("ok clicked");
								if (sessionHelper.twoFactorValidate(txCode.getValue())){
									postLogin(request,response,loginId, loginResult, keepMeSignedIn);
									return;
								}
								else {
									ZkUtil.errMsg("Invalid code");
									BanIpHelper.addIp(remoteAddr,sessionHelper.getBanIpDur());
									event.stopPropagation();
								}
							}
							else if (event.getButton() == Messagebox.Button.CANCEL) {
								UniLog.log1("cancel clicked");
							}
						}
					}
				);
				dlg.doModal();
				return;
    		}
    	}
    	
    	postLogin(request,response,loginId, loginResult, keepMeSignedIn);
    	
	}
	private void postLogin(HttpServletRequest request, HttpServletResponse response, String p_loginId, ReturnMsg loginResult, boolean keepMeSignedIn) throws Exception{
		String loginId = p_loginId;
		
		//if no LoginId, obtain it from loginResult
		if (StringUtils.isBlank(loginId) && loginResult.getStatus() && StringUtils.isNotBlank((String)loginResult.getData())) {
			loginId = (String)loginResult.getData();
		}
		
    	if(loginResult.getStatus()) {
    		BanIpHelper.clearIp(remoteAddr);
    		if (keepMeSignedIn) {
    			sessionHelper.genLoginToken(request, response, loginId);
    		}
    		//Clients.showBusy("Loading....");
			JxZkGadgetProvider pvdr = new JxZkGadgetProvider(sessionHelper);
			sessionHelper.putSessionData("jxzkgadgetprovider", pvdr);
    		
    		//load schema after login
    		if (sessionHelper.getSessionData("biSchema") == null){
    			BiSchema biSchema = BiSchema.loadSchema(sessionHelper);
    			if (biSchema == null) {
    				UniLog.log1("unable to load schema, force abort");
    				ZkUtil.errMsg("Unable to load schema");
    				return;
    			}
    			
    		}
    		ZkUtil.js("zkbiBc.send({action:'reloadCurrent'},false);"); //trigger non-active browser window auto login
    		
    		if (targetURL == null){
    			Executions.getCurrent().sendRedirect(sessionHelper.getHomePage());
    		}
    		else
    			Executions.getCurrent().sendRedirect(sessionHelper.getLandingPage(targetURL));
    		return;
    		
    	} 
    	else if (StringUtils.containsIgnoreCase(loginResult.getMsg(), "maintenance")) {
    		//do not count login fail
    		ZkUtil.warnMsg(loginResult.getMsg());
    		txPassword.setValue("");
    		return;
    	}
    	else {
    		BanIpHelper.addIp(remoteAddr,sessionHelper.getBanIpDur());
       		//Messagebox.show("Login Fail", "System Message", Messagebox.OK, Messagebox.INFORMATION);	
    		ZkUtil.errMsg(StringUtils.isNotBlank(loginResult.getMsg()) ? loginResult.getMsg() : "Login Fail");
    		txPassword.setValue("");
    		//Clients.clearBusy(winMain);
    		return;
    	}
		
	}
	private boolean checkTooManyAttempt() {
		if (sessionHelper.getBadIpMaxFailCnt() > 0 && BanIpHelper.addIp(remoteAddr,0) >= sessionHelper.getBadIpMaxFailCnt()){
   			UniLog.log1("too many login attempts");
   			Messagebox.show("Too many login attempts.\nPlease try again later.", "System Message", Messagebox.OK, Messagebox.INFORMATION);	
   			return false;
		}
		else {
			return true;
		}
	}
}
