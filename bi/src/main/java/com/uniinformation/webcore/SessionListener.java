package com.uniinformation.webcore;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.UniLog;

public class SessionListener implements HttpSessionListener {

  private static HashMap<String, String> activeSessionHM = new HashMap<String,String>();
  private final static boolean debugFlag = false;

  public static int getTotalActiveSession(){
	  synchronized(activeSessionHM){
		  int size = activeSessionHM.size();
		  return(size);
	  }
  }
  
  @Override
  public void sessionCreated(HttpSessionEvent p_event) {
        synchronized (activeSessionHM){
        	try{
        		//UniLog.log1("sessionCreated - add one session into counter:" + p_event.getSession());
        		activeSessionHM.put(p_event.getSession().getId(), p_event.getSession().getId());
        		UniLog.log1("current active session count: %d", activeSessionHM.size());
        		if (debugFlag) {
        			for (String as : activeSessionHM.keySet()){
    	 				UniLog.log1("active session: %s", as);
    	 			}
        		}
        	}
        	catch(Exception ex){
        		ex.printStackTrace();
        	}
        }
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent p_event) {
   		
        HttpSession session = p_event.getSession();
        try{
        	SessionHelper.deleteActiveUser(session.getId());
        	SessionHelper sh = (SessionHelper) session.getAttribute(SessionHelper.getNameByContextPath(null));
        	//UniLog.log1("sessionDestroyed - deduct one session from counter: " + session.toString());
        	if(sh != null) {
        		sh.cleanSessionData(); //may trigger exception 
        	}
        }
        catch(Exception ex){
        	ex.printStackTrace();
        }
        synchronized (activeSessionHM){
        	try{
		    	activeSessionHM.remove(p_event.getSession().getId());
        		UniLog.log1("current active session count: %d", activeSessionHM.size());
        		if (debugFlag) {
        			for (String as : activeSessionHM.keySet()){
    	 				UniLog.log1("active session: %s", as);
    	 			}
        		}
        	}
        	catch(Exception ex){
        		ex.printStackTrace();
        	}
        }
  }
}