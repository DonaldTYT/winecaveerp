package com.kyoko.common;

import java.io.*;
import java.util.*;
import java.text.*;


public class CoreLog {
	static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	static void log(int p_level, String p_string) {
		System.err.println( "kyoko.common.CoreLog(2):"+dateTimeFormat.format(new Date())+":"+p_string);
	}

	static public void log1(String p_format, Object... p_args) {
		log(p_format, p_args);
	}
	static public void log(String p_format, Object... p_args) {
		String ss = String.format(p_format, p_args);
		log(1, ss);
	}
	static public void log(Exception p_ex) {
	   StringWriter writer = new StringWriter();
	   PrintWriter printWriter = new PrintWriter( writer );
	   p_ex.printStackTrace( printWriter );
	   printWriter.flush();
	   String stackTrace = writer.toString();
       log(1, stackTrace);
	}
	static public void log(String p_string) {
		log(1, p_string);
	}
	static public void logClass(Object p_class,String p_string) {
		log(1, p_string);
	}
}
