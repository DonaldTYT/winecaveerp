package com.uniinformation.jxapp;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Idspace;

import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class AfsStockSetLink extends JxZkBiBase {
	@Override
	public void afterBind() {
		super.afterBind();
		
		new JxFieldAction("st_icode") {
			public void actionPerformed(JxField fd){
					UniLog.log("Bandbox Clicked actiontype = " + fd.getActionType());
					try {
						ZkJxPickInput zjpi = (ZkJxPickInput) fd.getNativeObject();
						if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
							AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
							if(jxf == null) {
								zjpi.setPopupWidth("320px");
								SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
								JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
								jxf = (AfsIcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"AfsIcodePicker");
								zjpi.setJxZkForm(jxf);
							}
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
			}
		};

	}

}
