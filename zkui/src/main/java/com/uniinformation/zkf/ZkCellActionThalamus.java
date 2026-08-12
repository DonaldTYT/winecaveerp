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
import com.uniinformation.zkbi.ZkBiEventListener;

public class ZkCellActionThalamus extends SelectorComposer<Component> {
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
