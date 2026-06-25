package com.uniinformation.utils;

import java.io.*;
import java.util.*;
import java.lang.*;

public class SiteConfig {
	static SiteConfig siteConfig = new SiteConfig();
	public static SiteConfig getSiteConfig() {
	   return(siteConfig);
	}
   public String getParameter(String p_name) {
	   if (p_name.equals("XslInputSampleDir")) {
         Map env = System.getenv();
         if (env.get("APP_BASEDIR") != null && !env.get("APP_BASEDIR").toString().trim().equals("")) {
		   	return(env.get("APP_BASEDIR").toString().trim()+File.separator+"xslSample");
         }
			else if (getParameter("ServerType").equals("Linux")) {
		   	return("/yic/v/unidev/spool/xslSample");
			}
			else
		   	return("C:\\opt\\unidev\\spool\\xslSample");
		}
		if (p_name.equals("ServerType")) {
			if (File.separatorChar == '/')
				return("Linux");
			else 
				return("Windows");
		}
		return(null);
	}
	public static void main(String args[]) {
		SiteConfig  sc = getSiteConfig();
		UniLog.log("trace: system="+sc.getParameter("ServerType"));
	} 
}
