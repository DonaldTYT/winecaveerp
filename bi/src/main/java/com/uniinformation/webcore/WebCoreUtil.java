package com.uniinformation.webcore;
import java.io.*;
import java.util.*;
import java.text.*;

import javax.mail.*; 
import javax.mail.internet.*;
import javax.servlet.http.*;

import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;
import com.kyoko.common.*;

public class WebCoreUtil {
   public static InternetAddress getInternetAddress(String p_address) throws Exception {
      if (p_address.indexOf('<') >= 0) {
         String namePart = StringUtil.strpart(p_address, 0, p_address.indexOf('<')).trim();
         String addressPart = StringUtil.strpart(
                              p_address, 
                              p_address.indexOf('<')+1, 
                              p_address.lastIndexOf('>')-p_address.indexOf('<')-1
                           ).trim();
         return(new InternetAddress(addressPart, namePart, "utf-8"));
      }
      else
         return(new InternetAddress(p_address, p_address, "utf-8"));
   }
	static boolean jdbcLoaded = false;
	private static Hashtable jdbcPools = new Hashtable();
	public static void closeCachedJdbcPool(String p_dbLabel) {
		synchronized (jdbcPools) {
			JdbcPool pool = (JdbcPool) jdbcPools.get(p_dbLabel);
			if (pool != null) {
				UniLog.log1("closeCachedJdbcPool %s", p_dbLabel);
				try {
					pool.setExiting();
					pool.pauseAllConnections();
				} catch (Exception e) {
					e.printStackTrace();
				}
				jdbcPools.remove(p_dbLabel);
			}
		}
	}
	public static JdbcPool getJdbcPoolByConnectionString(String p_dbLabel,int p_poolcnt,String p_jdbcString,String p_login,String p_password) {
		return getJdbcPoolByConnectionString(p_dbLabel,p_poolcnt,JdbcPool.DEFAULT_MAX_CONNECTION_COUNT,p_jdbcString,p_login,p_password);
	}
	public static JdbcPool getJdbcPoolByConnectionString(String p_dbLabel,int p_poolcnt,int p_maxpoolcnt,String p_jdbcString,String p_login,String p_password) {
		if(!jdbcLoaded ) {
			SelectUtil.loadJdbcDrivers();
			jdbcLoaded = true;
		}
	   JdbcPool pool = (JdbcPool) jdbcPools.get(p_dbLabel);
	   if (pool == null) {
	      synchronized (jdbcPools) {
	         pool = (JdbcPool) jdbcPools.get(p_dbLabel);
	         if (pool == null) {
	        	 try {
	        		 int maxPoolCnt = p_maxpoolcnt;
	        		 if (maxPoolCnt < p_poolcnt) {
	        			 maxPoolCnt = p_poolcnt;
	        		 }
	        		 //UniLog.log("WebCoreUtil.getJdbcPoolByConnectionString(): connect database "+p_dbLabel+" poolcnt " + p_poolcnt + " using "+p_jdbcString+" ...");
	        		 UniLog.log1("db:%s poolcnt:%d maxpoolcnt:%d jdbc:%s",p_dbLabel, p_poolcnt, maxPoolCnt, p_jdbcString);
	        		 pool = new JdbcPool(p_dbLabel);
	        		 //UniLog.log("After new JdbcPool");
	        		 pool.setConnectionCount(p_poolcnt);
	        		 //UniLog.log("After setConnectionCount");
	        		 pool.setMaxConnectionCount(maxPoolCnt);
	        		 pool.setConnectionString(p_jdbcString,p_login,p_password);
	        		 //UniLog.log1("jdbc xml:%s", pool.getRunningStatisticsXML());
	        		 //UniLog.log("After setConnectionString");
	        		 jdbcPools.put(p_dbLabel, pool);
	        	 } 
	        	 catch (Exception ex) {
	        		 UniLog.log(ex);
	        		 return(null);
	        	 }
	         }
		   }
		}
		return(pool);
	}

	/*
	public static DoorUtilParameterConverterInterface getDoorConverter() {
	   UniLog.log("DoorUtilParameterConverterInterface force return null");
	   return(null);
	}
	*/
	
	public static String uncompressUrl(String p_url) {
        try {
           if (p_url.indexOf('?') < 0)
              return(p_url);
           String prefix = StringUtil.strpart(p_url, 0, p_url.indexOf('?'));
           //LruContainer lruCont = UniDataContext.getContext().getLruContainer();
           Hashtable args = new Hashtable();
           StringBuffer sb = new StringBuffer();
           StringUtil.parseCommand(args, p_url);
           for (Enumeration en=args.keys(); en.hasMoreElements(); ) {
              String fdName = (String) en.nextElement();
              String value = (String) args.get(fdName);
              if (sb.length() == 0)
                 sb.append("?");
              else
                 sb.append("&");
              sb.append(fdName).append("=").append(StringUtil.urlencode(value));
           }
           return(prefix+sb.toString());
        } 
        catch (Exception ex) {
           UniLog.log(ex);
           return(p_url);
        }
	}
}
