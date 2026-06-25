package com.uniinformation.jxapp;

import org.zkoss.zk.ui.Executions;

import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class JxZkBase extends JxForm {
	protected SessionHelper sessionHelper = null;
	JxSelOpt popupSelopt = null;
	JxSelOpt pulldownSelOpt = null;
	public JxZkBase(){
		sessionHelper = ZkSessionHelper.getSessionHelper();
		UniLog.log1("new JxZkBase: " + Executions.getCurrent().getDesktop());
	}
	/***
	 * get session helper from cache
	 * for static code, please use SessionHelper.getSessionHelper() instead
	 * @return
	 */
	public SessionHelper getSessionHelper(){
		return sessionHelper;
		//return((SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath())));	
	}

	public JxSelOpt getPopupSelOpt() {
		if(popupSelopt == null) {
			popupSelopt = JxSelOpt.createPopupJxSelOpt(getSessionHelper());
		}
		return(popupSelopt);
	}
	protected JxSelOpt getPulldownSelOpt() {
		if(pulldownSelOpt == null) {
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) getSessionHelper().getSessionData("jxzkgadgetprovider");
			pulldownSelOpt = JxSelOpt.createJxSelOpt(pvdr);
//			pulldownSelOpt.hideForm();
		}
		return(pulldownSelOpt);
	}
}
