package com.uniinformation.jxapp.clinic;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;

public class JxZkAutoFiling extends ZkComposerBase{
	@Wire
	Window winMain;
	@Wire
	Div zkDropzone;

	//SessionHelper sessionHelper;  //move to parent

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		UniLog.log("doAfterCompose autofiling");
   		/*
		Selectors.wireComponents(p_comp, this, false);  //important for wire variable
   		sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
   		if (!sessionHelper.isLogin()){
   			Messagebox.show("Permission denied");
   			winMain.setVisible(false);
   			return;
   		}
		ZkBiLogHelper.logAccess(sessionHelper, p_comp, ETYPE.ACCESS_PAGE, this.getClass().toString());
   		*/
   		
   		zkDropzone.addEventListener("onDropzoneAdd", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
			}
   		});
		Clients.evalJavaScript("addDropzone('div#jsDropzone',false,true,'application/pdf',false);");
	    //ZkUtil.registerClientInfoEvent(winMain, sessionHelper, true, -100);
	}
}
