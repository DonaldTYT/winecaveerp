package com.uniinformation.jx.zk;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import com.uniinformation.utils.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.jx.*;

import java.util.*;
/***
 * This simple Composer mainly used for testing.
 * e.g. e.g. TestBarcodeScanner,JxZkTestCjs,...
 *
 */
public class ZkJxComposer extends SelectorComposer<Window> {
	Label  browserWindowId;
	@Wire
	Textbox tbMobilePrint;
	@Wire
	Button btMobilePrint;
	public void doAfterCompose(final Window comp) throws Exception {
		super.doAfterCompose(comp);
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		if(sessionHelper == null || !sessionHelper.isLogin()) {
			UniLog.log1("Not yet login, redirect to login.html");
			Executions.getCurrent().sendRedirect("login.html");
		} else {
			ZkUtil.registerClientInfoEvent(comp, sessionHelper, true, 0);
			if (Executions.getCurrent().getParameter("title") != null){
				comp.setTitle(Executions.getCurrent().getParameter("title").trim());
			}
			final JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
			if(pvdr == null) {
				UniLog.log1("Not JxZk Session , redirect to login.html");
				Executions.getCurrent().sendRedirect("login.html");
			} 
			else {
				browserWindowId = new Label();
				browserWindowId.setValue(UUID.randomUUID()+"");
				String formInstanceName = comp.getId()+"."+browserWindowId.getValue();
				UniLog.log1("Create New JxForm "+formInstanceName);
				JxForm jxf = pvdr.jxGetForm(formInstanceName);
				if(jxf != null) {
					UniLog.log1("Calling ZkJxGadgetProvider:jxUnregisterForm " );
					pvdr.jxUnRegisterForm(formInstanceName);
				}
				pvdr.jxzk_forminit(comp,Executions.getCurrent().getParameterMap(),comp.getId()+"."+browserWindowId.getValue());
				
			}
		}
		btMobilePrint.addEventListener(Events.ON_CLICK, event -> {
			UniLog.log1("event:%s, text:%s", event, tbMobilePrint.getText());
			ZkUtil.sendToMobileUsbPrinter(tbMobilePrint.getText());
		});
	}
}