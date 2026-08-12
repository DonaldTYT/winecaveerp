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


public class PageLoader2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final static long responseExpires =  604800000L; //default export : one week 
	
	static class PageDescriber {
		String sessionKey;
		Date timeToExpire;
		String accessKey;
		String content;
	}
	
	static Hashtable<String , Hashtable<String,PageDescriber>> pageHash = new Hashtable<String , Hashtable<String,PageDescriber>> ();
	
	static public void addPage(String p_agent,String p_sessionKey,Date p_expire,String p_accessKey,String pageName,String p_content) throws Exception {
		synchronized(pageHash) {
			Hashtable<String,PageDescriber> pdh = pageHash.get(p_agent);
			if(pdh == null) {
				pdh = new Hashtable<String,PageDescriber>();
				pageHash.put(p_agent, pdh);
			}
			PageDescriber pdr = new PageDescriber();
			pdr.sessionKey = p_sessionKey;
			pdr.timeToExpire = p_expire;
			pdr.accessKey = p_accessKey;
			pdr.content = p_content;
			pdh.put(pageName, pdr);
		}
	}
	static public void delPage(String p_agent,String pageName) throws Exception {
		synchronized(pageHash) {
			Hashtable<String,PageDescriber> pdh = pageHash.get(p_agent);
			if(pdh == null) return;
			pdh.remove(pageName);
		}
	}
	static public void purgeBySession(String p_sessionKey) throws Exception {
		synchronized(pageHash) {
			for(Hashtable<String,PageDescriber> pdh : pageHash.values()) {
				for(String key : pdh.keySet()) {
					PageDescriber pdr = pdh.get(key);
					if(p_sessionKey.equals(pdr.sessionKey)) {
						pdh.remove(key);
					}
				}
			}
		}
    }
	static public void purgeByAgent(String p_agent) throws Exception {
		synchronized(pageHash) {
			pageHash.remove(p_agent);
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
		Hashtable<String,PageDescriber> pdh = pageHash.get(sh.getAgent());
		if(pdh == null) {
			response.setContentType("text/html");
	    	PrintWriter out = response.getWriter();      
	    	out.println("<h1>" + "Agent Not Found"+ "</h1>");      
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
		if(pdr.sessionKey != null && !pdr.sessionKey.equals(sh.getSessionKey())) {
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
