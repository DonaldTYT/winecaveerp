package com.uniinformation.webcore.servlet;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.lang3.StringUtils;
//import org.zkoss.zk.ui.Executions;

import com.uniinformation.utils.*;


public class Redirect extends HttpServlet implements SingleThreadModel
{
	HttpServletRequest request;
	HttpServletResponse response;

	protected void doPost(HttpServletRequest req,HttpServletResponse res)
		throws ServletException, IOException
    {
		doIt(req,res);
    }
	protected void doGet(HttpServletRequest req,HttpServletResponse res)
		throws ServletException, IOException
    {
		doIt(req,res);
    }
	protected void doIt(HttpServletRequest p_request,HttpServletResponse p_response)
		throws ServletException, IOException
    {
		request = p_request;
		response = p_response;
		UniLog.logClass(this,"requestURI="+request.getRequestURI());
		processRequest();
	}
	
	String getTrimmedQueryString() {
			try {
				String queryStr = request.getQueryString();
				queryStr = trimUrlRoot(queryStr);
				if(StringUtils.isNoneBlank(queryStr)) {
					queryStr = "?"+queryStr;
				}
				return(queryStr);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				return(null);
			}
	}
	private String trimUrlRoot(String p_params){
		String url;
		if(p_params.startsWith("urlroot=")) {
			url = p_params.replaceAll("urlroot=[a-zA-Z0-9-_.%+]*", "");
			if(!StringUtils.isBlank(url)) {
				if(url.startsWith("&")) {
					url = url.substring(1);
				}
			}
		} else {
			url = p_params.replaceAll("&urlroot=[a-zA-Z0-9-_.%+]*", "");
		}
		return(url);
	}
	void processRequest()
	{
		String urlRoot = request.getParameter("urlroot");
		String redirUrl;
		if(urlRoot != null) {
			String queryStr = getTrimmedQueryString();
			String url = request.getPathInfo();
			
			
			
			/*
//			String url = request.getRequestURI();
			String url = request.getPathInfo();
//			String queryStr = request.getQueryString();
//			String urlStr = URLEncoder.encode("urlroot="+urlRoot);
			String queryStr = URLDecoder.decode(request.getQueryString());
			String urlStr = "urlroot="+urlRoot;
			int idx = queryStr.indexOf("?"+urlStr);
			if(idx < 0) {
				idx = queryStr.indexOf("&"+urlStr);
			}
			String s0="";
			if(idx > 0) {
				s0 = queryStr.substring(0,idx);
			}
			if(queryStr.length() > idx+queryStr.length()+1) {
				s0 += queryStr.substring(idx+queryStr.length()+1);
			}
			redirUrl = urlRoot+"/"+request.getRequestURI()+s0;
			*/
			redirUrl = urlRoot;
			if(!redirUrl.endsWith("/")) redirUrl += "/";
			redirUrl += url;
			redirUrl += queryStr;
//			redirUrl = "http://www.hellovoice.com";
		} else redirUrl = "http://www.hellovoice.com";
		response.setContentType("text/html");
		PrintWriter os = null;
		try {
			os = response.getWriter();
			os.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			os.println("<!DOCTYPE html>");
			os.println("<html>");
			os.println("<body>");
			   os.println("<p id = \"result\"></p>");
			   os.println("<script>");
			      os.println("function redirect () {");
			         os.println("setTimeout(myURL, 100);");
			         os.println("var result = document.getElementById(\"result\");");
//			         os.println("result.innerHTML = \"<b> The page will redirect after delay of 0.5 seconds\";");
			         os.println("result.innerHTML = \"<b>\";");
			      os.println("}");

			      os.println("function myURL() {");
			         os.println("document.location.href = '"+redirUrl+"';");
//			         os.println("document.location.href = '"+"http://www.hellovoice.com"+"';");
			      os.println("}");
			      os.println("redirect ();");
			   os.println("</script>");
			os.println("</body>");
			os.println("</html>");
			os.flush();
		} catch (Exception ex) {
			UniLog.log(ex);
			return;
		} finally {
			if(os != null) {
				/*
				try {
					os.close();
				} catch(Exception ignore) {
				}
				*/
			}

		}
	}
}
