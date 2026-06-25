package com.uniinformation.zkf;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.impl.MessageboxDlg;

import com.kikyosoft.utils.LogUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.webcore.vincero.VinceroSessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;

public class ZkCellActionMobileCheck extends SelectorComposer<Component> {
	@Wire
	private Div mainMenu;
	@Wire
	private Div disclaimer;
	@Wire
	private Div loginPanel;
	@Wire
	private Button btLogin;
	@Wire
	private Button btLogout;
	@Wire
	private Button btReset;
	@Wire
	private Textbox loginId;
	@Wire
	private Textbox loginPwd;
	@Wire
	private A a0001;
	@Wire
	private A a0002;
	@Wire
	private A a0003;
	@Wire
	private A a0004;
	@Wire
	private A a0005;
	@Wire
	private Button btScan;
	

	SessionHelper sessionHelper = null;	
	static final String admLoginId = "hlv";
	
	String generateRandomPassword() {
		double d = Math.random() * 999999.0;
		String ss = String.format("%06d", (int) d);
		return(ss);
	}
	
 	ReturnMsg sendResetPasswordEmail(SessionHelper sessionHelper,String sendFrom, String sendTo ,String passwd) {
 		return(
		BiUtil.sendEmail(
				Pair.of(sendFrom, (String) null),
				new VectorUtil()
				.addElement(Pair.of(sendTo,(String) null))
				.toVector(),
				null, 
				null, 
				"Message from Vincero On " + new java.util.Date().toString(),
				null, 
				new StringUtil()
					.cat("Dear Member,","\n")
					.addline()
					.cat("Welcome to Vincero Investment Academy!","\n")
					.addline()
					.cat("Your account for the VIA Student Portal has been successfully created.","\n")
					.addline()
					.cat("Here are your login details:","\n")
					.cat(String.format("Password: %s",passwd),"\n")
					.addline()
					.cat("You can access the Student Portal here: http://www.erpv4.com/vincero_app/vincero_login.jsp","\n")
					.addline()
					.cat("If you require any assistance, please do not reply to this email, as it is system-generated. Instead, contact our support team directly at: support@vinceroia.com","\n")
					.addline()
					.cat("We look forward to supporting your journey!","\n")
					.addline()
					.cat("Warm regards,","\n")
					.cat("Team Vincero","\n")	
					
					
				.toString(),
				null, sessionHelper)
		);
 	}
	
	void setMenuOne(A anc) {
		if(anc == null) return;
		String ref = anc.getHref();
		if(ref.startsWith("/")) {
			ref = ref.substring(1);
		}
		if(sessionHelper.checkWebMenuAccess(ref)) {
			anc.setVisible(true);
		} else {
			anc.setVisible(false);
		}
	}
//	private Button enterBtn;
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		super.doAfterCompose(arg0);
		LogUtil.log("In ZkCellActionCalMenu");	
		//Map<String,String[]> newUrlParams = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getParameterMap();
		//obtain session helper
	 	sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());		
	   	if(!sessionHelper.isValidAgent()) {
    		throw new Exception("Invalid agent");
    	}	
   		
   		if(sessionHelper.isLogin()) {
   			if(mainMenu != null) {
		 		mainMenu.setVisible(true);
   			}
   			if(disclaimer != null) {
		 		disclaimer.setVisible(true);
   			}
   			if(loginPanel != null) {
   				loginPanel.setVisible(false);
   				setMenuOne(a0001);
   				setMenuOne(a0002);
   				setMenuOne(a0003);
   				setMenuOne(a0004);
   				setMenuOne(a0005);
   			}
   			if(btLogin != null) {
   				btLogin.setVisible(false);
   			}
   			if(btLogout != null) {
   				btLogout.setVisible(true);
   		       	btLogout.addEventListener("onClick",
   		           new ZkBiEventListener() {
   		     			public void onZkBiEvent(Event event) throws Exception {
   		     				sessionHelper.logout();
   		     				Executions.getCurrent().sendRedirect("/vincero_login.jsp");
   		     			}
   		     	   } 
   		       	);
   			}
   			if(btReset != null) {
   				btReset.setVisible(false);
   			}
   		} else {
   			if(StringUtils.isBlank(sessionHelper.getAgent())) {
				sessionHelper.resetIniAgent((HttpServletRequest) Executions.getCurrent().getNativeRequest());
   			}
   			if(mainMenu != null) {
		 		mainMenu.setVisible(false);
   			}
   			if(disclaimer != null) {
		 		disclaimer.setVisible(false);
   			}
   			if(loginPanel != null) {
   				loginPanel.setVisible(true);
   			}
   			if(btLogin != null) {
   				btLogin.setVisible(true);
   		       	btLogin.addEventListener("onClick",
   		           new ZkBiEventListener() {
   		     			public void onZkBiEvent(Event event) throws Exception {
   		     				if(StringUtils.isBlank(loginId.getText())) {
   		     					ZkUtil.showErrMsg("Please Enter Email Address");
   		     					return;
   		     				}
   		     				if(StringUtils.isBlank(loginPwd.getText())) {
   		     					ZkUtil.showErrMsg("Please Enter Password");
   		     					return;
   		     				}
   		     				/*
   		     				BiResult br = sessionHelper.getBiSchema().getViewByName("vincero.subslogin").newBiResult(admLoginId, null, null, sessionHelper);
   		     				br.clear();
   		     				br.clearCondition();
   		     				br.addCustomCondition("sblogin_loginid = '"+loginId.getText()+"'");
   		     				br.query();
   		     				if(br.getRecordCount() != 1) {
   		     					if(br.getRecordCount() == 0) {
   		     						br.clearCurrentRec();
   		     						br.getCell("col_c").set(loginId.getText());
   		     						br.getCell("sblogin_password").set("345678");
   		     						br.getCell("sblogin_enabled").set(true);
   		     						ReturnMsg rtn = br.addCurrent();
   		     						if(rtn != null && !rtn.getStatus()) {
   		     							ZkUtil.showErrMsg(rtn.getMsg());
   		     							return;
   		     						} else {
   		     							Executions.getCurrent().sendRedirect("/vincero_login.jsp");
   		     						}
   		     					}
   		     					ZkUtil.showErrMsg("Invalid Login");
   		     					return;
   		     				} 
   		     				*/
   		     				boolean loginOk = ((VinceroSessionHelper) sessionHelper).loginProceed_vincero(loginId.getText(), loginPwd.getText());
   		     				if(!loginOk) {
   		     					ZkUtil.showErrMsg("Invalid Login");
   		     					return;
   		     				} else {
   		     					sessionHelper.setHomePage("/vincero_login.jsp");
   		     					sessionHelper.setAllowUserProfile(false);
   		     					sessionHelper.setLogoutURL("./vincero_login.jsp");
   		     					Executions.getCurrent().sendRedirect("/vincero_login.jsp");
   		     				}
   		     				/*
   		     				br.loadOneRecV(0);
   		     				if(loginPwd.getText().equals("302426") ||
   		     						loginPwd.getText().equals(br.getCellString("sblogin_password"))
   		     						) {
   		     					
   		     				} else {
   		     					ZkUtil.showErrMsg("Invalid Login");
   		     					return;
   		     				}
   		     				*/
   		     			}
   		     	   } 
   	       	);
   			}
   			if(btLogout != null) {
   				btLogout.setVisible(false);
   			}
   			if(btReset != null) {
   				btReset.setVisible(true);
 		       	btReset.addEventListener("onClick",
 	   		           new ZkBiEventListener() {
 	   		     			public void onZkBiEvent(Event event) throws Exception {
 	   		     				if(StringUtils.isBlank(loginId.getText())) {
 	   		     					ZkUtil.showErrMsg("Please Enter Email Address");
 	   		     					return;
 	   		     				}
 	   		     				BiResult br = sessionHelper.getBiSchema().getViewByName("vincero.subslogin").newBiResult(admLoginId, null, null, sessionHelper);
 	   		     				br.clear();
 	   		     				br.clearCondition();
 	   		     				br.addCustomCondition("sblogin_loginid = '"+loginId.getText()+"'");
 	   		     				br.query();
 	   		     				final boolean isCreated;
 	   		     				boolean isRegistered = false;
 	   		     				if(br.getRecordCount() == 1) {
 	   		     					isCreated = true;
 	   		     					br.loadOneRecV(0);
 	   		     					if(!br.getCellBoolean("sblogin_enabled")) {
 	   		     						ZkUtil.showErrMsg("Email Address Invalid");
 	   		     						return;
 	   		     					}
 	   		     					if(!StringUtils.isBlank(br.getCellString("sblogin_password"))) {
 	   		     						isRegistered = true;
 	   		     					}
 	   		     				} else {
 	   		     					isCreated = false;
 	   		     				}
 	   		     				if(!isRegistered) {
 	   		     					String passwd = generateRandomPassword();
 	   		     					if(isCreated) {
 	   		     					try {
 	   		     						br.fetchOneRecV(0);
 	   		     						br.getCell("sblogin_password").set(passwd);
 	   		     					} catch (CellException cex) {
 	   		     						ZkUtil.showErrMsg("Failed To Register");
 	   		     						return;
 	   		     					}
 	   		     					} else {
 	   		     					try {
 	   		     						br.clearCurrentRec();
 	   		     						br.getCell("col_c").set(loginId.getText());
 	   		     						br.getCell("sblogin_password").set(passwd);
 	   		     						br.getCell("sblogin_enabled").set(true);
 	   		     					} catch (CellException cex) {
 	   		     						ZkUtil.showErrMsg("Email Address Invalid");
 	   		     						return;
 	   		     					}
 	   		     					}
 	   		     					Messagebox.show(
 			        					"Confirm Register Account ?",
 			        					"Register", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
 			        					public void onEvent(Event evt) throws Exception {
 			        						if (evt.getName().equals("onOK")) {
 			        							ReturnMsg rtn;
 			        							if(isCreated) rtn = br.updateCurrent(); else rtn = br.addCurrent();
 			        							if(rtn != null && !rtn.getStatus()) {
 			        								ZkUtil.showErrMsg(rtn.getMsg());
 			        								return;
 			        							} else {
 			        								String email = br.getCellString("sblogin_loginid");
 			        								String passwd = br.getCellString("sblogin_password");
 			        								rtn = sendResetPasswordEmail(sessionHelper,"postmaster@erpv4.com",email,passwd);
// 			        								rtn = sendResetPasswordEmail(sessionHelper,"postmaster@erpv4.com","support@vinceroia.com",passwd);
// 			        								rtn = sendResetPasswordEmail(sessionHelper,"postmaster@erpv4.com","tyt223@gmail.com",passwd);
 			        								if(rtn != null && rtn.getStatus()) {
 			        									ZkUtil.showMsg("Registration successful. Your password has been sent to your email (please also check spam mail folder if you don't receive the email).");
 			        								} else {
 			        									ZkUtil.showMsg("There is something wrong,please contact our support :  " + (rtn == null ? "null" : rtn.getStatus()));
 			        								}
 			        							}
 			        						}
 			        					};
 			        				});
 	   		     					/*
 	   		     					if(br.getRecordCount() == 0) {
 	   		     						br.clearCurrentRec();
 	   		     						br.getCell("col_c").set(loginId.getText());
 	   		     						br.getCell("sblogin_password").set("345678");
 	   		     						br.getCell("sblogin_enabled").set(true);
 	   		     						ReturnMsg rtn = br.addCurrent();
 	   		     						if(rtn != null && !rtn.getStatus()) {
 	   		     							ZkUtil.showErrMsg(rtn.getMsg());
 	   		     							return;
 	   		     						} else {
 	   		     							Executions.getCurrent().sendRedirect("/vincero_login.jsp");
 	   		     						}
 	   		     					}
 	   		     					ZkUtil.showErrMsg("Invalid Login");
 	   		     					return;
 	   		     					*/
 	   		     				} else {
 	   		     					br.loadOneRecV(0);
 			        				String email = br.getCellString("sblogin_loginid");
 			        				String passwd = br.getCellString("sblogin_password");
 			        				ReturnMsg rtn = sendResetPasswordEmail(sessionHelper,"postmaster@erpv4.com",email,passwd);
// 			        				ReturnMsg rtn = sendResetPasswordEmail(sessionHelper,"postmaster@erpv4.com","support@vinceroia.com",passwd);
// 			        				ReturnMsg rtn = sendResetPasswordEmail(sessionHelper,"postmaster@erpv4.com","tyt223@gmail.com",passwd);
 			        				if(rtn != null && rtn.getStatus()) {
// 	   		     						ZkUtil.showMsg("Your have already registered. The password has been sent to your email.");
 	   		     						ZkUtil.msg("Your have already registered. The password has been sent to your email (please also check spam mail folder if you don't receive the email) .");
 			        				} else {
 			        					ZkUtil.showMsg("There is something wrong,please contact our support :  " + (rtn == null ? "null" : rtn.getStatus()));
 			        				}
 	   		     				}
 	   		     			}
 	   		     	   } 
 	   		       	);
   			}
   			if(btScan != null) {
   				btScan.setVisible(true);
 		       	btScan.addEventListener("onClick",
 	   		           new ZkBiEventListener() {
 	   		     			public void onZkBiEvent(Event event) throws Exception {
 	   		     				UniLog.log("Scan Button Pressed");
 	   		     				Clients.evalJavaScript(
 	   		     					"if (window.ZkBiCamera) {" +
 	   		     					"  ZkBiCamera.open({" +
 	   		     					"    mode: 'scanner'," +
 	   		     					"    autoStopAfterScan: true," +
 	   		     					"    onScan: function(text) {" +
 	   		     					"      var loginId = zk.Widget.$('$loginId');" +
 	   		     					"      if (loginId) {" +
 	   		     					"        loginId.$n().value = text;" +
 	   		     					"        loginId.updateChange_();" +
 	   		     					"      }" +
 	   		     					"    }" +
 	   		     					"  });" +
 	   		     					"} else {" +
 	   		     					"  alert('Camera scanner script is not loaded.');" +
 	   		     					"}"
 	   		     				);
 	   		     			}
 	   		     	   } 
 	   		       	);
   			}
   		}
	}
}
