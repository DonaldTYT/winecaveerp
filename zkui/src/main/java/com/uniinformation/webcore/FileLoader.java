package com.uniinformation.webcore;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.UniLog;

/**
 * Servlet implementation class FileLoader
 * For download file from Filing
 * 
  web.xml
  <servlet>
    <description></description>
    <display-name>FileLoader</display-name>
    <servlet-name>FileLoader</servlet-name>
    <servlet-class>com.uniinformation.webcore.FileLoader</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>FileLoader</servlet-name>
    <url-pattern>/fileloader/*</url-pattern>
  </servlet-mapping>
  
  url http://localhost:8080/pmsdemo/fileloader/<filingKey>
  e.g. url http://localhost:8080/pmsdemo/fileloader/logo.jpg
  e.g. url http://localhost:8080/pmsdemo/fileloader/jxStockImageFiling_0000000187_0000000037
 *
 */
public class FileLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//final static long responseExpires = 0L;  //no cache
	//final static long responseExpires = 86400000L; //expire: one day
	final static long responseExpires =  604800000L; //expire: one week 
    public FileLoader() {
        super();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UniLog.log1("called %s", request.getPathInfo());
		if (request.getPathInfo() == null || request.getPathInfo().trim().length() <= 6){ //format /<KEY>
			UniLog.log("key is empty or too short, ignore");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		String filingKey = request.getPathInfo().substring(1);  //remove initial slash 
		UniLog.log1("key=%s", filingKey);
		loadFile(request, response, filingKey);
	}
    private boolean isAllowed(SessionHelper p_sh, String p_filingKey){
    	/*
    	//need to login
    	if (!p_sh.isLogin()){
    		UniLog.log1("no login");
			return false;
    	}
    	*/
    	
    	//check filing key in whitelist
    	if (!p_sh.isFilingKeyValid(p_filingKey)){
    		UniLog.log1("invalid filing key, not in whitelist");
    		return false;
    	}
    	
    	return true;
    }
	private void loadFile(HttpServletRequest request, HttpServletResponse response, String p_filingKey) throws ServletException, IOException {
		try{
			SessionHelper sh = ZkSessionHelper.getSessionHelper(request , response);
			UniLog.log1("key:%s sh:%s", p_filingKey, sh.toString());
			
			//validation
			if (!isAllowed(sh,p_filingKey)){
				UniLog.log1("access denied");
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FilingUtilObject fuo = FilingUtil.getFile(sh.getAgent(), null, p_filingKey, baos);
			baos.close();
			byte[] ba = baos.toByteArray();
			if (fuo == null || fuo.size == 0L){
				UniLog.log1("file not found");
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			
			//guess file type
			Map<String,String> fileTypeMap = FilingUtil.guessFileType(ba);
			//append ext if required
			String fileName = p_filingKey;
			if (!fileName.matches("^.*[.][a-zA-Z]{3,6}$")){
				fileName = fileName + fileTypeMap.get("ext");
			}
			
			//set response header
			response.setHeader("Content-Type", fileTypeMap.get("mimeType"));
			//response.setHeader("Content-Length", "" + fuo.size); //andrew190920: content-length is unknown, unless pre-fetch the record 
			response.setHeader("Content-Length", "" + fuo.size);  //andrew191004: load the file to bytes first. Content-Length is optional
			response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
			
			if (responseExpires <= 0L){
				response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
				response.setHeader("Pragma", "no-cache"); // HTTP 1.0
				response.setDateHeader("Expires", 0); 	// Proxies.
			}
			else{
				response.setDateHeader("Expires", System.currentTimeMillis() + responseExpires);
			}
			response.getOutputStream().write(ba);
			response.getOutputStream().flush();
		}
		catch(Exception ex){
			//display a dummy image here
			UniLog.log(ex);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
