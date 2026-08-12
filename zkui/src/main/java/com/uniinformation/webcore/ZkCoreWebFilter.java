package com.uniinformation.webcore;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uniinformation.utils.UniLog;

/***
 * 
 * experimental only !!!
 *
 */
public class ZkCoreWebFilter implements Filter {
	private FilterConfig config;
	private static String commonPrefix = "/common";
	private static String localPrefix = "/local";
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
    	UniLog.log("doFilter called : request:"+request.getRequestURL());
    	
    	//skip process zk internal file
    	if (request.getRequestURI().startsWith(request.getContextPath() + "/zkau")){
        	chain.doFilter(request, response);
        	return;
    	}
    	//if url contain common, remove it
    	if (request.getRequestURI().startsWith(request.getContextPath() + commonPrefix)){
    		StringBuffer urlSb = new StringBuffer();
    		urlSb.append(request.getContextPath());
    		urlSb.append(request.getRequestURI().substring(request.getContextPath().length() + commonPrefix.length()));
    		if (request.getQueryString() != null){
    			urlSb.append("?");
    			urlSb.append(request.getQueryString());
    		}
    		response.sendRedirect(urlSb.toString()); //becareful dead loop
        	//chain.doFilter(request, response); // bad, common appear in url
        	return;
    	}
    	if (request.getRequestURI().startsWith(request.getContextPath() + localPrefix)){
    		StringBuffer urlSb = new StringBuffer();
    		urlSb.append(request.getContextPath());
    		urlSb.append(request.getRequestURI().substring(request.getContextPath().length() + localPrefix.length()));
    		if (request.getQueryString() != null){
    			urlSb.append("?");
    			urlSb.append(request.getQueryString());
    		}
    		response.sendRedirect(urlSb.toString()); //becareful dead loop
        	//chain.doFilter(request, response); // bad, common appear in url
        	return;
    	}
    	StringBuffer urlSb = new StringBuffer();
    	urlSb.append(commonPrefix);
    	urlSb.append(request.getRequestURI().substring(request.getContextPath().length()));
    	if (request.getQueryString() != null){
    		urlSb.append("?");
    		urlSb.append(request.getQueryString());
    	}
    	String commonUrl = buildUrl(request, commonPrefix, true);
    	String localUrl = buildUrl(request, localPrefix, true);
    	String localUrlFile = config.getServletContext().getRealPath(buildUrl(request, localPrefix, false));
    	boolean localUrlFileExist = (new File(localUrlFile)).isFile();
    	UniLog.log("commonUrl:" + commonUrl);
    	UniLog.log("localUrl:" + localUrl);
    	UniLog.log("localUrlFile:" + localUrlFile + " exist:" + localUrlFileExist);
    	if (localUrlFileExist){
    		request.getRequestDispatcher(localUrl).forward(request, response);
    	}
    	else{
    		request.getRequestDispatcher(commonUrl).forward(request, response);
    	}
    }
    public static String buildUrl(HttpServletRequest request, String p_prefix, boolean p_withQueryString){
    	StringBuffer urlSb = new StringBuffer();
    	urlSb.append(p_prefix);
    	urlSb.append(request.getRequestURI().substring(request.getContextPath().length()));
    	if (p_withQueryString && request.getQueryString() != null){
    		urlSb.append("?");
    		urlSb.append(request.getQueryString());
    	}
    	return(urlSb.toString());
    }
    
	@Override
	public void destroy() {
		UniLog.log("webfilter destroy");
	}

	@Override
	public void init(FilterConfig p_config) throws ServletException {
		config = p_config;
		UniLog.log("webfilter init");
	}
	
	public String getAbsPath(String p_path){
		return("HAHA" + p_path);
	}
}