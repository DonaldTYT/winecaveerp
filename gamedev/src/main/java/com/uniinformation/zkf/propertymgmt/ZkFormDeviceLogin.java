package com.uniinformation.zkf.propertymgmt;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkFormDeviceLogin extends ZkCellActionForm {
	String targetURL;
	String password;
	String deviceId;
	String keybuf;
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		customLoginUrl = "";
		keybuf = "";
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				UniLog.log("Button " + arg0.getTarget().getId());
				String butId = arg0.getTarget().getId();
				if(butId.equals("btaEnter")) {
					if(password.equals(keybuf)) {
						UniLog.log("Login OK");
						ReturnMsg rtn = sessionHelper.login("dev#0000", "irns481");
						if(rtn.getStatus()) {
							sessionHelper.setVcode(deviceId);
						}
						Executions.sendRedirect(targetURL);
						return;
					}
					keybuf = "";
				} else if(butId.equals("btaBack")) {
					if(keybuf.length() > 0) {
						keybuf = keybuf.substring(0,keybuf.length()-1);
					}
				} else {
					if(butId.startsWith("btN")) {
						String digit = butId.substring(3,4);
						if(keybuf.length() < 8) {
							keybuf += digit;
						}
					}
				}
				formCollection.getCell("ldv_keybuf").set("**********".substring(0, keybuf.length()));
				// TODO Auto-generated method stub
				
			}
		};
		super.doAfterCompose(p_comp);
		targetURL = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getRequestURL().toString();
		String  queryString = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getQueryString();
      	deviceId = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getParameter("deviceid");
		if(queryString != null) targetURL += "?" + queryString;
		if(sessionHelper.isLogin()) {
			Executions.sendRedirect(targetURL);
		} else {
			sessionHelper.setLogoutURL(targetURL);
			SelectUtil su = sessionHelper.getBiSchema().getSelectUtil();
			TableRec tr = su.getQueryResult("select * from devicelogin,location where ldv_login = ? and lc_rg = ldv_lcrg" , 
					new Wherecl().appendArgument(deviceId)
					);
			tr.setRecPointer(0);
			password = tr.getFieldString("ldv_password");
			Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sessionHelper, Erpv4Config.getDefaultCoCode(sessionHelper));
			//formCollection.getCell("ldv_cmpname").set(Erpv4Config.getCoName(sessionHelper, Erpv4Config.getDefaultCoCode(sessionHelper)));
			formCollection.getCell("ldv_cmpname").set(coMap.get("co_coname"));
			formCollection.getCell("ldv_cmpname1").set(coMap.get("co_chnname"));
			formCollection.getCell("ldv_locname").set(tr.getFieldString("lc_desc"));
			formCollection.getCell("ldv_title").set("自助繳費機");
		}
	}

	@Override
	protected boolean validateURL(String p_requestURL) {
		return true;
	}
}
