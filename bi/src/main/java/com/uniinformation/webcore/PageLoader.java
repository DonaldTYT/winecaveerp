package com.uniinformation.webcore;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uniinformation.utils.UniLog;


public class PageLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static long responseExpires =  604800000L; //default export : one week 
	
	static class PageDescriber {
		String agent;
		Date timeToExpire;
		String accessKey;
		String content;
	}
	
	static Hashtable<String , Hashtable<String,PageDescriber>> pageHash = new Hashtable<String , Hashtable<String,PageDescriber>> ();
	
	static public void addPageX(String p_sessionKey,String p_agent,Date p_expire,String p_accessKey,String pageName,String p_content) throws Exception {
		synchronized(pageHash) {
			Hashtable<String,PageDescriber> pdh = pageHash.get(p_sessionKey);
			if(pdh == null) {
				pdh = new Hashtable<String,PageDescriber>();
				pageHash.put(p_sessionKey, pdh);
			}
			PageDescriber pdr = new PageDescriber();
			pdr.agent = p_agent;
			pdr.timeToExpire = p_expire;
			pdr.accessKey = p_accessKey;
			pdr.content = p_content;
			pdh.put(pageName, pdr);
		}
	}
	static public void delPageX(String p_sessionKey,String pageName) throws Exception {
		synchronized(pageHash) {
			Hashtable<String,PageDescriber> pdh = pageHash.get(p_sessionKey);
			if(pdh == null) return;
			pdh.remove(pageName);
		}
	}
	static public void purgeByAgentX(String p_agent) throws Exception {
		synchronized(pageHash) {
			for(Hashtable<String,PageDescriber> pdh : pageHash.values()) {
				for(String key : pdh.keySet()) {
					PageDescriber pdr = pdh.get(key);
					if(p_agent.equals(pdr.agent)) {
						pdh.remove(key);
					}
				}
			}
		}
    }
	static public void purgeBySession(String p_sessionKey) throws Exception {
		synchronized(pageHash) {
			pageHash.remove(p_sessionKey);
		}
    }
	static public void purgeByTime(Date p_time) throws Exception {
		synchronized(pageHash) {
			for(Hashtable<String,PageDescriber> pdh : pageHash.values()) {
				for(String key : pdh.keySet()) {
					PageDescriber pdr = pdh.get(key);
					if(pdr.timeToExpire != null && p_time.before(pdr.timeToExpire)) {
						pdh.remove(key);
					}
				}
			}
		}
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UniLog.log1("called %s", request.getPathInfo());
		SessionHelper sh = ZkSessionHelper.getSessionHelper(request , response);
		if(sh == null) {
			response.setContentType("text/html");
	    	PrintWriter out = response.getWriter();      
	    	out.println("<h1>" + "Please Login"+ "</h1>");      
	    	out.println("<p>" + "Hello Friends!" + "</p>"); 
	    	return;
		}
		Hashtable<String,PageDescriber> pdh = pageHash.get(sh.getSessionKey());
		if(pdh == null) {
			response.setContentType("text/html");
	    	PrintWriter out = response.getWriter();      
	    	out.println("<h1>" + "Session Expired"+ "</h1>");      
	    	out.println("<p>" + "Hello Friends!" + "</p>"); 
	    	return;
		}
		String pageName = request.getParameter("pagename");
		PageDescriber pdr = pdh.get(pageName);
		if(pdr == null) {
			response.setContentType("text/html");
	    	PrintWriter out = response.getWriter();      
	    	out.println("<h1>" + "Page Not Found"+ "</h1>");      
	    	out.println("<p>" + "Hello Friends!" + "</p>"); 
	    	return;
		}
		if(pdr.agent != null && !pdr.agent.equals(sh.getAgent())) {
			response.setContentType("text/html");
	    	PrintWriter out = response.getWriter();      
	    	out.println("<h1>" + "Invalid Session"+ "</h1>");      
	    	out.println("<p>" + "Hello Friends!" + "</p>"); 
	    	return;
		}
		response.setContentType("text/html");
	    PrintWriter out = response.getWriter();      
	    out.print(pdr.content);
	    return;
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
