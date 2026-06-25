package com.uniinformation.utils;

import java.io.*;
import java.util.*;
import java.text.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kyoko.common.TextUtil;

public class UniLog {
   static boolean fSaveInString = false;
   static boolean useStdErr = false;
   static StringBuffer lastLog = null;
   static String encoding = null;
   static Runtime rt = Runtime.getRuntime();

   /*
    * SLF4J version of the old UniLog.
    *
    * Important compatibility note:
    * The old class configured Log4j 1.x by itself when log4j.configuration
    * was not supplied. SLF4J is only a facade, so it should not configure the
    * backend here. Make sure the application provides an SLF4J provider, for
    * example logback-classic, slf4j-simple, or another SLF4J 2.x binding.
    */
   static {
      if (useStdErr) {
         System.err.println("UniLog.java: Use System.err for log output");
      }
   };

   static public boolean setFSaveInString(boolean p_flag) {
      boolean oflag = fSaveInString;
      fSaveInString = p_flag;
      return(oflag);
   }

   static public String getLastLog() {
      if (lastLog == null)
         return(null);
      String str = lastLog.toString();
      lastLog = new StringBuffer();
      return(str);
   }

   static public String baseClassName(Object p_obj) {
      if (p_obj == null)
         return("");
      String token = "";
      for (StringTokenizer st = new StringTokenizer(p_obj.getClass().getName(), ".");
           st.hasMoreTokens();
           ) {
         token = st.nextToken();
      }
      return(token);
   }

   static public void logm(int p_level, Object p_object, String p_format, Object... p_args) {
      logm(p_level, 3, p_object, p_format, p_args);
   }

   static public void logm(Object p_object, String p_format, Object... p_args) {
      logm(1, 3, p_object, p_format, p_args);
   }

   static public void logm(int p_level, int p_stackLevel, Object p_object, String p_format, Object... p_args) {
      StringBuffer content = new StringBuffer();
      try{
         if (p_object != null){
            content.append(p_object.getClass().toString().substring(p_object.getClass().toString().lastIndexOf(".") + 1));
            content.append(".");
         }
         content.append(Thread.currentThread().getStackTrace()[p_stackLevel].getMethodName());
      }
      catch(Exception ex){
         ex.printStackTrace();
      }
      content.append("() ");

      if (p_args == null || p_args.length == 0){
         content.append(p_format);
      }
      else{
         content.append(String.format(p_format, p_args));
      }
      log(p_level, content.toString());
   }

   /***
    * Can obtain class name and method name of static/object method
    * Default log level 1
    * Better version of logm, not required to pass object
    *
    * REMARK:
    * Please copy this method to scorpion_jdbc/altsrc to manually
    * @param stackOffset - 0 caller method, 1 caller caller method,...
    * @param p_format
    * @param p_args
    */
   static public void log1(int p_stackOffset, String p_format, Object... p_args) {
      StringBuffer content = new StringBuffer();
      try{
         StackTraceElement ste = Thread.currentThread().getStackTrace()[ 2 + p_stackOffset];
         content.append(ste.getClassName().substring(ste.getClassName().lastIndexOf(".") + 1));
         content.append(".");
         content.append(ste.getMethodName());
         content.append("() ");
         if (p_args == null || p_args.length == 0){
            content.append(p_format);
         }
         else{
            content.append(String.format(p_format, p_args));
         }
         log(1, content.toString());
      }
      catch(Exception ex){
         content.setLength(0);
         content.append("log error:" + ex.getMessage() + " [");
         content.append(p_format);
         if (p_args != null){
            for (int i=0; i<p_args.length; i++){
               content.append("," + p_args[i]);
            }
         }
         content.append("]");
         log(1, content.toString());
      }
   }

   static public void log1(String p_format, Object... p_args) {
      log1(1, p_format, p_args);
   }

   static public void log(String p_string) {
      log(1, p_string);
   }

   static public void log(int p_level, String p_string) {
      /*
      log_with_dateformatter(p_level, p_string, findformatter());
      System.err.flush();
      */
      if(useStdErr)
         System.err.println(
              "com.uniinformation.utils.UniLog:"+p_string              );
      else
         LoggerFactory.getLogger("com.uniinformation.utils.UniLog").info(p_string);
   }

   static public void logClass(int p_level, Object p_obj, String p_string) {
      try{
         if(!useStdErr)
            LoggerFactory.getLogger(p_obj.getClass()).info(
               new StringBuffer()
                    .append(baseClassName(p_obj))
                    .append("@")
                    .append(TextUtil.intToHex(p_obj.hashCode()))
                    .append(":")
                    .append(p_string)
                    .toString()
            );
         else
            log(p_level,
               new StringBuffer()
                    .append(baseClassName(p_obj))
                    .append("@")
                    .append(TextUtil.intToHex(p_obj.hashCode()))
                    .append(":")
                    .append(p_string)
                    .toString()
            );
      }
      catch (NoClassDefFoundError ex){
         System.err.println(String.format("logClass fail(%s):%s", ex.getMessage(), p_string));
      }
   }

   static public void logCatClass(String p_category, Object p_obj, String p_string) {
      logCatClass(1, p_category, p_obj, p_string);
   }

   static public void logCatClass(int p_level, String p_category, Object p_obj, String p_string) {
      LoggerFactory.getLogger(p_category).info(
         new StringBuffer()
              .append(baseClassName(p_obj))
              .append("@")
              .append(TextUtil.intToHex(p_obj.hashCode()))
              .append(":")
              .append(p_string)
              .toString()
      );
   }

   static public void logCat(String p_category, String p_string) {
      logCat(1, p_category, p_string);
   }

   static public void logCat(int p_level, String p_category, String p_string) {
      LoggerFactory.getLogger(p_category).info(p_string);
   }

   static public void logClass(Object p_obj, String p_string) {
      logClass(5, p_obj, p_string);
   }

   static public void logClass(Object p_obj, Throwable p_ex) {
      log(3, p_obj, p_ex);
   }

   static public void log(Exception p_ex) {
      log(3, (Throwable) p_ex);
   }

   static public void log(Throwable p_ex) {
      log(3, p_ex);
   }

   static public void log(int p_level, Exception p_ex) {
      log(p_level, (Throwable) p_ex);
   }

   static public void log(int p_level, Throwable p_ex) {
      log(p_level, null, p_ex);
   }

   static public void log(int p_level, Object p_obj, Throwable p_ex) {
      try {
         SimpleDateFormat formatter = findformatter();
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         LoggerFactory.getLogger(p_obj == null ? "Exception" : p_obj.getClass().getName()).info(
            "Exception caught : "+p_ex.toString()
         );
         // log_with_dateformatter(p_level, "Exception caught : "+p_ex.toString(), formatter);
         p_ex.printStackTrace(pw);
         String s = sw.toString();
         int lasti = -1;
         for (;;) {
            int i = s.indexOf('\n', lasti+1);
            if (i < 0) {
               LoggerFactory.getLogger(p_obj == null ? "Exception" : p_obj.getClass().getName()).info(
                  s.substring(lasti+1, s.length())
               );
               // log_with_dateformatter(p_level, s.substring(lasti+1, s.length()), formatter);
               break;
            }
            LoggerFactory.getLogger(p_obj == null ? "Exception" : p_obj.getClass().getName()).info(
               s.substring(lasti+1, i)
            );
            // log_with_dateformatter(p_level, s.substring(lasti+1, i), formatter);
            lasti = i;
         }
         sw.close();
         pw.close();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      System.err.flush();
   }

   static private SimpleDateFormat findformatter() {
      return(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"));
   }

   synchronized static public void log_with_dateformatter(int p_level, String p_string, SimpleDateFormat p_formatter) {
      Date now = new Date();
      System.err.print(p_level);
      System.err.print(":");
      System.err.print(p_formatter.format(now));
      System.err.print(":");
      Thread th = Thread.currentThread();
      System.err.print(th.getName());
      System.err.print(":");
      if (encoding == null)
         System.err.println(p_string+"[mem:"+rt.freeMemory()+"/"+rt.totalMemory()+"]");
      else {
         try {
            System.err.println(new String(p_string.getBytes(encoding), "ISO8859_1")+"[mem:"+rt.freeMemory()+"/"+rt.totalMemory()+"]");
         } catch (Exception ex) {
            System.err.println(p_string);
         }
      }

      if (!fSaveInString) return;

      if (lastLog == null)
         lastLog = new StringBuffer();
      lastLog.append(p_level)
             .append(":")
             .append(p_formatter.format(now))
             .append(":")
             .append(th.getName())
             .append(":")
             .append(p_string)
             .append("\n");
   }

   static public String printStackTraceToString(Exception p_ex) {
      try {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         pw.println("Exception caught : "+p_ex.toString());
         p_ex.printStackTrace(pw);
         return(sw.toString());
      } catch (Exception ex) {
         ex.printStackTrace();
         return(p_ex.toString());
      }
   }

   static public String printStackTraceToString(Throwable p_ex) {
      try {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         pw.println("Exception caught : "+p_ex.toString());
         p_ex.printStackTrace(pw);
         return(sw.toString());
      } catch (Exception ex) {
         ex.printStackTrace();
         return(p_ex.toString());
      }
   }

   static public void setEncoding(String p_encoding) {
      encoding = p_encoding;
   }

   static public void main(String[] args) {
      log(Integer.parseInt(args[0]), args[1]);
      log(1, new Exception("this is an exception"));
   }
}
