package com.uniinformation.winecave;
import java.io.*;
import java.util.*;
import com.uniinformation.utils.*;
import com.kyoko.common.CoreLog;
import com.uniinformation.rpccall.*;

public class WineCaveConfig
{
	public final static String CONFIGFILE = "WineCaveConfig.properties";
	static Properties config = readConfig();

//	public static Properties readConfig(String configFile) {
//	   Properties prop = new Properties();
//	   File file = new File(configFile);
//	   return(prop);
//	}
   public static String getProperty(Properties config, String p_propname) {
		String value = null;
		value = config.getProperty(p_propname);
		if (value == null) 
			return("");
		else
			return(value);
   }
   private static Properties readConfig() {
	   try {
		  return(IniHelper.loadProperty(null, CONFIGFILE));
	   } catch (Exception ex) {
		   CoreLog.log(ex);
		   return(null);
	   }
   }
   public static String getProperty(String p_propname) {
	   return getProperty(config, p_propname);
	}
}

