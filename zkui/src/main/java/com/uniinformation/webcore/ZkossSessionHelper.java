package com.uniinformation.webcore;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.erpv4.NotifyMsgObj;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.DownloadByteArrayOutputStream;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper.EVENT_TYPE;


public class ZkossSessionHelper extends SessionHelper {
    private String notifyMsgMethodFullName = "";
    private Method notifyMsgMethod = null;
	private Hashtable<String,QueueRec> deviceQueHash;

	public static ZkSessionHelper getSessionHelperDummy(String p_iniAgent,String p_loginid,ServletContext p_svc) {
		return getSessionHelperDummy(p_iniAgent, p_loginid, p_svc, () -> new ZkSessionHelper());
	}
	/***
	 * object sessionHelper from session
	 * @return
	 */
	public static SessionHelper getSessionHelper() {
    	return (SessionHelper) getCurrentHttpSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
	}
	/***
	 * the entry point of obtain a sessionhelper
	 * @param p_request
	 * @param p_response
	 * @param p_requireNew
	 * @return
	 */
	synchronized public static SessionHelper getSessionHelper(HttpServletRequest p_request, HttpServletResponse p_response, boolean p_requireNew) {
		return getSessionHelper(p_request, p_response, p_requireNew, () -> new ZkSessionHelper());
	}
	/***
	 * get / create sessionHelper
	 * @param p_request
	 * @param p_response
	 * @return
	 */
	public static SessionHelper getSessionHelper(HttpServletRequest p_request, HttpServletResponse p_response) {
		return(getSessionHelper(p_request, p_response, false));
	}
	/***
	 * for dev or unit test only
	 * @param p_iniAgent
	 * @return
	 */
//	public static ZkSessionHelper getSessionHelperDummy(String p_iniAgent) {
//		return(getSessionHelperDummy(p_iniAgent,"dummy"));
//	}
//	public static ZkSessionHelper getSessionHelperDummy() {
//		return(getSessionHelperDummy(null,"dummy"));
//	}
//	public static ZkSessionHelper getSessionHelperDummy(String p_iniAgent,String p_loginid) {
//		return(getSessionHelperDummy(p_iniAgent,p_loginid,null));
//	}
//	public static ZkSessionHelper getSessionHelperDummy(String p_iniAgent,String p_loginid,ServletContext p_svc) {
//		return getSessionHelperDummy(p_iniAgent, p_loginid, p_svc, () -> new ZkSessionHelper());
//	}

	@Override
	protected void readSessionProperties(IniHelper ini) {
		super.readSessionProperties(ini);
    	notifyMsgMethodFullName = ini.getString("notifyMsgMethodFullName","");
    	try {
   			UniLog.log1("load notifymsg method:%s", notifyMsgMethodFullName);
    		notifyMsgMethod = null;
    		if (StringUtils.isNotBlank(notifyMsgMethodFullName)) {
    			String className = notifyMsgMethodFullName.substring(0,notifyMsgMethodFullName.lastIndexOf("."));
    			String methodName = notifyMsgMethodFullName.substring(notifyMsgMethodFullName.lastIndexOf(".")+1);
   				Class clazz = Class.forName(className);
				notifyMsgMethod = clazz.getDeclaredMethod(methodName, SessionHelper.class);
    		}
    	}
    	catch(Exception ex) {
    		UniLog.log1("ignore invalid notifymsg method:%s:%s", notifyMsgMethodFullName, ex.getMessage());
    	}
	}

    public boolean getAllowNotifyMsg() {
    	return(notifyMsgMethod != null);
    }

	public String setDeviceEventQueueListener(String p_device,EventListener p_listener,boolean p_force) {
		/*
		if(eventQueHash == null) {
			eventQueHash = new Hashtable<String,QueueRec>();
		}
		*/
		QueueRec qrec=null;
		synchronized(deviceQueHash) {
			qrec = deviceQueHash.get(p_device);
			if(qrec == null) {
				String queueId = DeviceControl.getUniqueEventQueid();
				qrec = new QueueRec(p_device,queueId);
				deviceQueHash.put(p_device, qrec);
			}
		}
		qrec.setListner(p_listener,p_force);
		return(qrec.queueId);
	}

	private void cleanDeviceQue() {
		if(deviceQueHash == null) return;
		synchronized(deviceQueHash) {
			Iterator<Map.Entry<String , QueueRec>> it = deviceQueHash.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, QueueRec> entry = it.next();
				QueueRec data = entry.getValue();
				DeviceControl.detachListiner(entry.getKey(), data.queueId);
			}
		}
	}

	class QueueRec {
		String devId;
		String queueId;
		EventListener listener;
		public QueueRec(String p_devid,String p_queueid ) {
			devId = p_devid;
			queueId = p_queueid;
		}
		public void setListner(EventListener p_listener,boolean p_force) {
			EventQueue que = EventQueues.lookup(queueId, EventQueues.APPLICATION, true);
			if(listener != null) {
				if(listener != p_listener) {
					try {
//						listener.onEvent( new Event("onBarcodeDetached"));
						que.publish(new Event("onListenerDetached", null,listener));
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					listener = p_listener;
					if(listener != null) {
						que.subscribe(listener);
					} else {
						DeviceControl.detachListiner(devId, queueId);
						return;
					}
				}
			} else {
				listener = p_listener;
				if(listener != null) {
					que.subscribe(listener);
				}
			}
			DeviceControl.attachListiner(devId, queueId, p_force);
		}
	}


    public void showNotifyMsg() {
    	if (notifyMsgMethod	== null) {
    		return;
    	}
    	try {
    		List<NotifyMsgObj> msgs = (List<NotifyMsgObj>) notifyMsgMethod.invoke(null,this);
    		for (NotifyMsgObj msg : msgs) {
    			if (msg.level == NotifyMsgObj.Level.norm) {
    				ZkUtil.showMsg(msg.toString());
    			}
    			else if (msg.level == NotifyMsgObj.Level.warn) {
    				ZkUtil.showWarnMsg(msg.toString());
    			}
    			else {
    				ZkUtil.showErrMsg(msg.toString());
    			}
    		}
    	}
    	catch(Exception ex) {
    		UniLog.log1("error:%s", ex.getMessage());
    	}
    }

    @Override
	protected boolean setLogin(boolean p_fLogin) throws Exception {
    	if (super.setLogin(p_fLogin)) {
    		deviceQueHash = new Hashtable<String,QueueRec>();
    		return true;
    	}
    	return false;
	}

    @Override
	public void cleanSessionData() {
    	super.cleanSessionData();
		cleanDeviceQue();
	}

	public static void updateActiveUser(SessionHelper p_sh, String p_url) {
		updateActiveUser(p_sh, getCurrentHttpSession(), p_url);
	}

	@Override
	synchronized public OutputStream newErpFileOutputStream(String p_filename) throws Exception {
		if(p_filename.startsWith(URLHEADER_DOWNLOAD)) {
			return(new DownloadByteArrayOutputStream(
						this,
						null,
						p_filename.substring(URLHEADER_FILING.length()),
						p_filename.substring(URLHEADER_FILING.length()),
						""
					));
		}
		return super.newErpFileOutputStream(p_filename);
	}
	
    public static HttpSession getCurrentHttpSession() {
    	Session session = Sessions.getCurrent();
    	return session != null ? (HttpSession) session.getNativeSession() : null;
    }

    /*
    public static HttpServletRequest getCurrentHttpServletRequest() {
    	Execution execution = Executions.getCurrent();
    	return execution != null ? (HttpServletRequest) execution.getNativeRequest() : null;
    }
    */

	public String getWebContentRealPath(String p_path, boolean p_withSeparator){
		if(Executions.getCurrent() != null) {
  		    return getWebContentRealPath((HttpServletRequest)Executions.getCurrent().getNativeRequest(), p_path, p_withSeparator);
		} else {
			return svc.getRealPath(p_path) + (p_withSeparator ? File.separator :"");
		}
	}

	static String getWebContentRealPath(HttpServletRequest request, String p_path, boolean p_withSeparator){
		return getWebContentRealPath(request.getSession(), p_path, p_withSeparator);
	}
	static String getWebContentRealPath(HttpSession session, String p_path, boolean p_withSeparator){
		return getWebContentRealPath(session.getServletContext(), p_path, p_withSeparator);
	}
	static String getWebContentRealPath(ServletContext svc, String p_path, boolean p_withSeparator){
		return svc.getRealPath(p_path) + (p_withSeparator ? File.separator :"");
	}
	
    @Override
    public String getURLParam(String p_key) {
			return(Executions.getCurrent().getParameter(p_key));
    }
    
    
    @Override
    public String getClassRootPath() {
		String rootPath = ZkUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		rootPath = StringUtils.removeEnd(rootPath, StringUtils.replaceChars(ZkUtil.class.getName(),'.','/') +".class");
		
		if (!StringUtils.endsWith(rootPath, "/")) {
			rootPath = rootPath +"/";
		}
		return rootPath;
    }
    
    
    @Override
    protected void cleanSessionObject(Object o) {
		if(o instanceof JxZkGadgetProvider) {
			UniLog.log("Calling JxZkGadgetProvider Cleanup");
			((JxZkGadgetProvider) o).providerCleanUp();
		}
    }
    
	public static void showMsg(String p_format, Object...p_args){
		ZkUtil.showMsg(p_format, p_args);
	}
	public static void showWarnMsg(String p_format, Object...p_args){
		ZkUtil.showWarnMsg(p_format, p_args);
	}
	public static void showErrMsg(String p_format, Object...p_args){
		ZkUtil.showErrMsg(p_format, p_args);
	}
	
	@Override
	public Object lookupEventQueue(String p_name,EVENT_TYPE p_type,boolean p_autoCreate) {
		String scope = null;
		switch(p_type) {
		case APPLICATION : scope = EventQueues.APPLICATION;
		}
		EventQueue que = EventQueues.lookup(p_name, scope, p_autoCreate);
		return(que);
	}
	
	@Override
	public void publishEventQueue(Object p_que,String p_eventStr,Object p_data) {
		if(p_que instanceof EventQueue) {
			((EventQueue) p_que).publish(new Event(p_eventStr, null,p_data));
		}
	}
	
	public String getViewExtraBatchAction(String p_viewid,int idx) {
		return(BiConfig.getString(this, JxZkBiBase.replaceViewName(p_viewid)+"_ViewExtraBatchAction_"+idx));
	}
	public String getViewExtraJxFormAction(String p_viewid,int idx) {
		return(BiConfig.getString(this, JxZkBiBase.replaceViewName(p_viewid)+"_ViewExtraJxFormAction_"+idx));
	}
}
