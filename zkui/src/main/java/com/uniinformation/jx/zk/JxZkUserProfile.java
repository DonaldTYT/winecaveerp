package com.uniinformation.jx.zk;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.MessageboxDlg;

import com.uniinformation.utils.MapUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.TOTPUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.BanIpHelper;
import com.uniinformation.webcore.LabelHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.zk.*;

public class JxZkUserProfile extends SelectorComposer<Window> {
	public static final String filingKeyFormat = "zkbi_userprofile_%s";
	@Wire
	private Window winUserProfile;
	@Wire 
	private Label lbHeader;
	@Wire 
	private Textbox txLoginId;
	@Wire 
	private Textbox txOldPassword;
	@Wire 
	private Textbox txNewPassword;
	@Wire 
	private Textbox txNewPasswordConfirm;
	@Wire
	Label lbLoginId;
	@Wire
	Label lbOldPassword;
	@Wire
	Label lbNewPassword;
	@Wire
	Label lbNewPasswordConfirm;
	@Wire
	Button btUpdate;
	@Wire
	Button btClear;
	@Wire
	Combobox comboLang;
	@Wire
	Label lbLang;
	@Wire
	Checkbox cbGA;
	@Wire
	Image imgGA;
	@Wire
	Button btGASubmit;
	@Wire
	Box confirmBox;
	@Wire
	Intbox txGAConfirmCode;
	@Wire
	Textbox txGASecret;
	@Wire
	Label lbGA;
	@Wire
	Label straightPassword;
	@Wire
	Row rowAccessKey;
	@Wire
	Textbox txAccessKey;
	@Wire
	Div divTwoFactorAuth;
	
   	SessionHelper sh = null;
	
	public static HashMap<String, Integer> langIdxHM = new HashMap<String, Integer>(){{
		this.put("ENG", 0);
		this.put("TCHN", 1);
		this.put("SCHN", 2);
		this.put("JAP", 3);
		this.put("KOR", 4);
	}};
	
	public static ReturnMsg buildFilingKey(String p_loginId){
		if (p_loginId == null || p_loginId.trim().equals("")){
			return(new ReturnMsg(false,"Invalid login id"));
		}
		return(new ReturnMsg(true, String.format(filingKeyFormat, p_loginId)));
	}
	
	@Override
	public void doAfterCompose(final Window comp) throws Exception {
		super.doAfterCompose(comp);
		UniLog.log("In SelectorComposer window id: "+comp.getId());
		
    	sh= ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
	    if(sh == null || !sh.isLogin()) {
	    	UniLog.log("Not yet login, redirect to login.html");
	    	Executions.getCurrent().sendRedirect("login.html");
	    	return;
	    }
	    lbHeader.setValue(sh.getLabel("User Profile"));
	    
	    lbLoginId.setValue(sh.getLabel("Login Id"));
	    lbOldPassword.setValue(sh.getLabel("Old Password"));
	    lbNewPassword.setValue(sh.getLabel("New Password"));
	    lbNewPasswordConfirm.setValue(sh.getLabel("New Password Confirm"));
	    
	    btUpdate.setLabel(sh.getLabel("Update"));
	    btClear.setLabel(sh.getLabel("Clear"));
	    if (!sh.getAllowChangePassword()){
	    	btUpdate.setDisabled(true);
	    	btUpdate.setTooltiptext(sh.getLabel("Update password function disabled"));
	    }
    	txLoginId.setValue(sh.getLoginId());
    	txLoginId.setReadonly(true);
    	lbLang.setValue(sh.getLabel("Preferred Language"));
		//comboLang.setValue(LabelHelper.getText("Language", LabelHelper.TYPE_LB, sh.getLHLang()));
		comboLang.appendChild(new Comboitem("English"){{this.setValue("ENG");}});
		comboLang.appendChild(new Comboitem("\u7E41\u9AD4\u4E2D\u6587"){{this.setValue("TCHN");}});
		comboLang.appendChild(new Comboitem("\u7C21\u9AD4\u4E2D\u6587"){{this.setValue("SCHN");}});
		comboLang.appendChild(new Comboitem("\u65E5\u672C\u8A9E"){{this.setValue("JAP");}});
		comboLang.appendChild(new Comboitem("\uD55C\uAD6D\uC5B4"){{this.setValue("KOR");}});
		comboLang.setSelectedIndex(0);
		String langString = sh.getLHLang();
		comboLang.setSelectedIndex(langIdxHM.get(langString) == null ? 0 : langIdxHM.get(langString));
		comboLang.addEventListener(Events.ON_SELECT, new ZkBiEventListener() {
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.logm(null,"comoLang %s %s", comboLang.getSelectedItem().getLabel(), comboLang.getSelectedItem().getValue());
				if (comboLang.getSelectedItem().getValue() != null){
					sh.setLHLang(comboLang.getSelectedItem().getValue().toString(),1,true);
				}
				Executions.getCurrent().sendRedirect("");  //refresh here can preserve notification msg
				
			}
		});
    	
    	if (sh.isMobileDevice()){
    		comp.setWidth("");
    	}
    	else{
    		comp.setWidth("90%");
    	}
    	
   		divTwoFactorAuth.setVisible(sh.getAllowTwoFactorAuth());
    	if (sh.getAllowTwoFactorAuth()) {
    		lbGA.setValue("Google Authenticator");
    	}
    	else {
    		lbGA.setValue("Google Authenticator (Feature Not Available)");
    		cbGA.setTooltiptext("Feature Not Available");
    		btGASubmit.setDisabled(true);
    		btGASubmit.setTooltiptext("Feature Not Available");
    		
    	}
    	if (sh.twoFactorIsEnable()) {
			cbGA.setChecked(true);
    	}
    	
    	if(sh.isStraightPassword()) {
    		straightPassword.setValue(sh.getLabel("Must be mixture of both uppercase and lowercase letters and include at least one special character, e.g., ! @ # ?")
    				);
    	}
    	
   		//cbGA.setDisabled(!sh.getAllowTwoFactorAuth());
		
       	btUpdate.addEventListener("onClick",
           	new ZkBiEventListener() {
   				public void onZkBiEvent(Event event) throws Exception {
   					UniLog.log("update button Pressed");
   					if (txOldPassword.getText().trim().length() > 0 && txNewPassword.getText().trim().length() > 0){
	   					if (txNewPassword.getText().trim().length() < 4){
	   						showWarnMsg(sh.getLabel("New password too short"));
	   						return;
	   					}
	   					if (!txNewPassword.getText().equals(txNewPasswordConfirm.getText())){
	   						showWarnMsg(sh.getLabel("New password mismatch"));
	   						return;
	   					}
	   					if (!StringUtils.isAlphanumeric(txNewPassword.getText())){
	   						showWarnMsg(sh.getLabel("New password cannot contain special character"));
	   						return;
	   					}
	   					ReturnMsg rtnMsg = sh.changePassword(sh.getLoginId(), txOldPassword.getText(), txNewPassword.getText());
	   					if (!rtnMsg.getStatus()){
	   						showWarnMsg(rtnMsg.getMsg());
	   						return;
	   					}
   					}
   					if (comboLang.getSelectedItem().getValue() != null){
   						UniLog.log("save language");
   						//JSONObject json = new JSONObject(){{ this.put("lang", comboLang.getSelectedItem().getValue().toString());}};
						//ReturnMsg returnMsg = sh.storeUserProfileJson(json);
						ReturnMsg returnMsg = sh.saveUserProfile();
						if (!returnMsg.getStatus()){
	   						showWarnMsg(sh.getLabel("Fail to update prefered language"));
	   						return;
						}
   					}
   					txOldPassword.setText("");
   					txNewPassword.setText("");
   					txNewPasswordConfirm.setText("");
					showMsg(sh.getLabel("Record Updated"));
   				}
   			});
       	btClear.addEventListener("onClick",
           	new ZkBiEventListener() {
   				public void onZkBiEvent(Event event) throws Exception {
   					UniLog.log("update clear Pressed");
   					txOldPassword.setText("");
   					txNewPassword.setText("");
   					txNewPasswordConfirm.setText("");
   				}
   			});
       	
       	cbGA.addEventListener("onCheck", new ZkBiEventListener() {
       		@Override
       		public void onZkBiEvent(Event event) throws Exception {
       			UniLog.log1("got event");
       			if (sh.twoFactorIsEnable() && !cbGA.isChecked()){
       				MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(sh.getLabel("Warning"), 
       						new Vbox() {{ appendChild(new Label("Are you sure to cancel Google Authenticator?"));}}, 
       						new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
       						winUserProfile, 
       						new ZkBiEventListener<Messagebox.ClickEvent>() {
       							@Override
       							public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
       								if (event.getButton() == Messagebox.Button.OK) {
       									sh.setTwoFactorSecret(null);
       									ReturnMsg returnMsg = sh.saveUserProfile();
       									if (returnMsg.getStatus()) {
       										ZkUtil.showMsg(sh.getLabel("Record Updated"));
       									}
       									else {
       										ZkUtil.errMsg(sh.getLabel("Unable to update user profile. Please try again later"));
       									}
       								}
       								else if (event.getButton() == Messagebox.Button.CANCEL) {
       									cbGA.setChecked(true);
       									UniLog.log1("cancel clicked");
       									return;
       								}
       							}
       						}
       						);
       				dlg.doModal();
       				return;
       			}

       			if (cbGA.isChecked()) {
       				Map totp = TOTPUtil.createTOTP("ERP "+sh.getAgent(), sh.getLoginId());
       				imgGA.setSrc(MapUtil.getString(totp,"otpAuthImg"));
       				txGASecret.setText(MapUtil.getString(totp, "secret"));
       				confirmBox.setVisible(true);
       			}
       			else {
       				imgGA.setSrc(null);
       				confirmBox.setVisible(false);
       				txGAConfirmCode.setValue(null);
       				txGASecret.setText("");
       			}

       		}
       	});
       	btGASubmit.addEventListener("onClick", new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("got event");
				String secret = txGASecret.getValue().trim();
				int confirmCode = -1;
				try {
					confirmCode = txGAConfirmCode.intValue();
				}
				catch(Exception ex) { }
				if (StringUtils.isBlank(secret)){
					UniLog.log1("invalid secret");
					ZkUtil.errMsg(sh.getLabel("Invalid secret key. Please try again later"));
					return;
				}
				if (TOTPUtil.validatePassword(secret,confirmCode)) {
					sh.setTwoFactorSecret(secret);
					ReturnMsg returnMsg = sh.saveUserProfile();
					if (returnMsg.getStatus()) {
						confirmBox.setVisible(false);
						ZkUtil.showMsg(sh.getLabel("Record Updated"));
					}
					else {
						ZkUtil.errMsg(sh.getLabel("Unable to update user profile. Please try again later"));
					}
				}
				else {
					ZkUtil.errMsg(sh.getLabel("Invalid confirm code. Please try again"));
				}
				
			}
       	});
       	
       	
       	if (sh.getAllowShowDebug()) {
       		rowAccessKey.setVisible(true);
       		//txAccessKey.setValue(sh.getAccessRights().toString());
       		String[] accessRightArr = sh.getAccessRights().toArray(new String[0]);
       		Arrays.sort(accessRightArr);
       		txAccessKey.setValue(Arrays.toString(accessRightArr));
       	}
       	
       	
   		ZkUtil.receiveBroadcast(sh);
   		ZkUtil.registerClientInfoEvent(comp, sh, true, -100);
		ZkBiTranslateHelper.addOnUpdateTranslateEventListener(comp, sh);
	}
    void showMsg(String p_format, Object...p_args){
       	Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: 6000 })");
    }
    void showWarnMsg(String p_format, Object...p_args){
       	Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"warn\", globalPosition:\"bottom right\", autoHideDelay: 6000 })");
    }
}
