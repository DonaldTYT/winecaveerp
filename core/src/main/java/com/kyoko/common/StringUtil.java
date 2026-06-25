package com.kyoko.common;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.uniinformation.cell.*;
import com.uniinformation.utils.UniLog;

public class StringUtil
{
	
	StringBuffer sb;
	public StringUtil addline() {
		sb.append("\n");
		return(this);
	}
	public StringUtil cat(String str,String seperator) {
		if(sb == null) {
			if(!StringUtils.isBlank(str)) sb = new StringBuffer(str); 
		} else {
			if(!StringUtils.isBlank(str)) {
				sb.append(seperator);
				sb.append(str);
			}
		}
		return(this);
	}
	public String toString() {
		if(sb == null) return("");
		return(sb.toString());
	}
	
	
	final public static char LF = 10;
	final public static char CR = 13;
	final public static char BS = 32;
   public static String u_to_8859(String p_string) {
		try {
         return(new String(p_string.getBytes("GB2312"), "ISO8859_1"));
		} catch (Exception ex) {
		   UniLog.log(ex);
			return(p_string);
		}
	}
   public static String jsStringTrim(String p_string) {
	   if (p_string == null) return("");
      return(jsString(p_string.trim()));
	}
   public static String jsString(String p_string) {
	   if (p_string == null) return("");
		Sprintf sp = new Sprintf("%04x");
	   char carr[];
		StringBuffer sb = new StringBuffer();
		carr = p_string.toCharArray();
	   for (int i=0; i<carr.length; i++) {
		   int cc = (int)carr[i];
			if ((cc >= 'a' && cc <= 'z') ||
			    (cc >= 'A' && cc <= 'Z') ||
			    (cc >= '0' && cc <= '9'))
		   	sb.append(carr[i]);
			else if (cc == 0x09 || cc == 0x0a || cc == 0x0d  ||
					 (cc >= 0x20 && cc <=0x7e) ||
					 (cc >= 0x80 && cc <=0xd7ff) ||
					 (cc >= 0xe000 && cc <=0xf8ff) ||
					 (cc >= 0xf900 && cc <=0xfffd) ||
					 (cc >= 0x10000 && cc <=0x10ffff)) {
		      sp.clear();
		   	sb.append("\\u")
				  .append(sp.add(cc).toString());
			} else {
		   	sb.append("\\u")
				  .append(sp.add(63).toString());
			}
		}
		return(sb.toString());
	}
   public static String cwst(String p_string) {
      return(convertWebStringTrim(p_string));
	}
   public static String cws(String p_string) {
	   return(convertWebString(p_string));
	}
   public static String convertWebStringTrim(String p_string) {
	   if (p_string == null) return("");
		return(convertWebString(new String(p_string).trim()));
	}
   public static String convertWebString(String p_string) {
	   if (p_string == null) return("");
	   char carr[];
		StringBuffer sb = new StringBuffer();
		//DecimalFormat form = new DecimalFormat("00000");
		//FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD);

		carr = p_string.toCharArray();
	   for (int i=0; i<carr.length; i++) {
		   int cc = (int)carr[i];
			if ((cc >= 'a' && cc <= 'z') ||
			    (cc >= 'A' && cc <= 'Z') ||
			    (cc >= '0' && cc <= '9'))
		   	sb.append(carr[i]);
			else if (cc == 0x09 || cc == 0x0a || cc == 0x0d  ||
					 (cc >= 0x20 && cc <=0x7e) ||
					 (cc >= 0x80 && cc <=0xd7ff) ||
					 (cc >= 0xe000 && cc <=0xf8ff) ||
					 (cc >= 0xf900 && cc <=0xfffd) ||
					 (cc >= 0x10000 && cc <=0x10ffff)) {
		   	sb.append("&#").append((int) carr[i]).append(";");
				//form.format((long) carr[i], sb, fp);
			} else {
				sb.append("&#63;");
			}
		}
		return(sb.toString());
	}
   public static String cws2(String p_string) {
	   if (p_string == null) return("");
	   char carr[];
		StringBuffer sb = new StringBuffer();
		carr = p_string.toCharArray();
	   for (int i=0; i<carr.length; i++) {
		   int cc = (int)carr[i];
			if ((cc >= 'a' && cc <= 'z') ||
			    (cc >= 'A' && cc <= 'Z') ||
			    (cc >= '0' && cc <= '9') ||
			    (cc == 0x09) || 
				 (cc == 0x0a) || 
				 (cc == 0x0d) || 
				 (cc >= 0x20 && cc <=0x7e))
		   	sb.append(carr[i]);
			else if ((cc >= 0x80 && cc <=0xd7ff) ||
					 (cc >= 0xe000 && cc <=0xf8ff) ||
					 (cc >= 0xf900 && cc <=0xfffd) ||
					 (cc >= 0x10000 && cc <=0x10ffff)) {
		   	sb.append("&#").append((int) carr[i]).append(";");
			} else {
				sb.append("&#63;");
			}
		}
		return(sb.toString());
	}
	public static String getUrlFromHash(Hashtable p_hash, String p_delim) {
	   return(getUrlFromHash(p_hash, p_delim, true));
	}
	public static String getUrlFromHash(Hashtable p_hash, String p_delim, boolean p_fIncludeEmpty) {
	   StringBuffer sb = new StringBuffer();
		Vector v = new Vector();
	   for (Enumeration en=p_hash.keys(); en.hasMoreElements(); ) {
		   Object key = en.nextElement();
		   v.addElement(key);
		}
		Collections.sort(v);
	   for (int j=0; j<v.size(); j++) {
		   Object key = v.elementAt(j);
			Object obj = p_hash.get(key);
			if (obj instanceof String) {
				if (p_fIncludeEmpty || !obj.toString().trim().equals("")) {
		         sb.append(p_delim);
			      sb.append(key.toString());
			      sb.append("=");
			      sb.append(StringUtil.urlencode((String) obj));
				}
			}
			else if (obj instanceof String[]) {
			   String[] arr = (String[]) obj;
			   for (int i=0; i<arr.length; i++) {
				   if (p_fIncludeEmpty || !arr[i].toString().trim().equals("")) {
		            sb.append(p_delim);
			         sb.append(key.toString());
			         sb.append("=");
			         sb.append(StringUtil.urlencode(arr[i]));
					}
				}
			}
		}
		return(sb.toString());
	}
	public static CellCollection readUrlIntoCellCollection(String p_url) {
	   CellCollection cc = new CellCollection();
		StringBuffer decodedUrlSb = new StringBuffer();
		cc.putValue("url", p_url);
		cc.putValue("encodedurl", urlencode(p_url));
		if (p_url.indexOf('?') >= 0) {
		   cc.putValue("path", StringUtil.strpart(p_url, 0, p_url.indexOf('?')));
		   decodedUrlSb.append(StringUtil.strpart(p_url, 0, p_url.indexOf('?')));
		}
	   CellCollection cc2 = new CellCollection();
	   CellCollection cc3 = new CellCollection();
		Hashtable args = new Hashtable();
	   parseCommand(args, p_url);
		int cnt=0;
	   for (Enumeration en=args.keys(); en.hasMoreElements(); ) {
		   Object key = en.nextElement();
			cc2.putValue(key.toString(), args.get(key).toString());
			cc3.putValue(key.toString(), urlencode(args.get(key).toString()));
			if (cnt == 0)
		      decodedUrlSb.append("?");
			else
		      decodedUrlSb.append("&");
		   decodedUrlSb.append(key.toString())
			            .append("=")
			            .append(args.get(key).toString());
		   cnt++;
		}
		cc.addCollection("args", cc2);
		cc.addCollection("argsUrlEncoded", cc3);
		cc.putValue("decodedUrl", decodedUrlSb.toString());
	   return(cc);
	}
	public static Vector getElementsByDelimiter(String p_str, String p_delim) {
		Vector v = new Vector();
     	StringTokenizer tk = new StringTokenizer(p_str, p_delim);
      for (int i=0; tk.hasMoreTokens(); ) {
         String arg = tk.nextToken().trim();
         if (!arg.trim().equals(""))
            v.addElement(arg.trim());
         i++;
      }
		return(v);
	}
	/*
	public static int parseCommand(Hashtable p_hash, String p_url) {
		String url = p_url;
		if (url.indexOf('?') >= 0)
		   url = StringUtil.strpart(url, url.indexOf('?')+1, -1);
	   int idx0, idx1, cnt;
		cnt = 0;
		idx0 = 0;
	   for (;;) {
			if (idx0 >= url.length())
			   break;
		   idx1 = url.indexOf('/', idx0);
			if (idx1 < 0)
		      idx1 = url.indexOf('&', idx0);
		   if (idx1 < 0) {
	         if (parseOption(p_hash, url.substring(idx0)) == 0) {
				   cnt++;
				}
				break;
			}
			if (idx1 > idx0) {
	         if (parseOption(p_hash, url.substring(idx0, idx1)) == 0) {
				   cnt++;
			   }
			}
		   idx0 = idx1 + 1;
		}
		return(cnt);
	}
	*/
	public static int parseCommand(Hashtable p_hash, String p_url) {
		String url = p_url;
		if (url.indexOf('?') >= 0)
		   url = StringUtil.strpart(url, url.indexOf('?')+1, -1);
	   int cnt;
		cnt = 0;
		int state = 0;
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<url.length(); i++) {
			switch (url.charAt(i)) {
			   case '&':
			   case '/':
               if (parseOption(p_hash, sb.toString()) == 0)
                  cnt++;
			      sb = new StringBuffer();
			      break;
			   default:
		         sb.append(url.charAt(i));
					if (i == (url.length()-1)) {
                  if (parseOption(p_hash, sb.toString()) == 0)
                     cnt++;
			         sb = new StringBuffer();
					}
					break;
			}
		}
		return(cnt);
	}
	public static int parseOption(Hashtable p_hash, String p_option) {
	   int equalAt;
		int skipcnt = 0;
		if (p_option == null) 
		   return(-1);
		equalAt = p_option.indexOf('=');
		if (equalAt > 0) 
		   skipcnt = 1;
		else {
		   equalAt = p_option.indexOf("%3d");
		   if (equalAt > 0) 
		      skipcnt = 3;
			else {
		      equalAt = p_option.indexOf("%3D");
		      if (equalAt > 0) 
		         skipcnt = 3;
	         else
				   return(-1);
			}
		}
		if (p_hash != null) {
			if (equalAt == p_option.length()-1) {
		      p_hash.put(p_option.substring(0, equalAt), "");
			}
		   else {
				/*
		      p_hash.put(p_option.substring(0, equalAt), 
		                 new URLDecoder().decode(p_option.substring(equalAt+skipcnt)));
			   */
		      p_hash.put(p_option.substring(0, equalAt), 
		                 urldecode(p_option.substring(equalAt+skipcnt)));
		   }
		}
	   return(0);
	}
	public static String basename(String p_filename) {
		int cc;
	   if (p_filename == null)
		   return(null);
	   cc = p_filename.lastIndexOf('\\');
		if (cc >= 0) {
		   return(p_filename.substring(cc+1));
		}
	   cc = p_filename.lastIndexOf('/');
		if (cc < 0)
		   return(p_filename);
		return(p_filename.substring(cc+1));
	}
	public static String fileExtension(String p_filename) {
		int cc;
	   if (p_filename == null)
		   return(null);
	   cc = p_filename.lastIndexOf('.');
		if (cc >= 0) {
		   return(p_filename.substring(cc+1));
		}
		return(null);
	}
	public static String dirname(String p_filename) {
	   return(dirname(p_filename, '/'));
	}
	public static String dirname(String p_filename, char p_sep) {
		int cc;
	   if (p_filename == null)
		   return(null);
	   cc = p_filename.lastIndexOf(p_sep);
		if (cc < 0)
		   return(".");
		return(p_filename.substring(0, cc));
	}
	public static char getLanguageFromURL(String p_url) {
		if (p_url == null) return('C');  // default is Chinese
	   if (p_url.indexOf("Chtml") >= 0) return('C');
		return('E');
	}
	public static String getTodayString() {
		return(new java.sql.Date(new java.util.Date().getTime()).toString());
	}
	private static int hextoint(byte p_c) {
	   if (p_c >= '0' && p_c <= '9')
			return(p_c-'0');
	   else if (p_c >= 'A' && p_c <= 'F')
			return(p_c-'A'+10);
	   else if (p_c >= 'a' && p_c <= 'f')
			return(p_c-'a'+10);
	   return(0);
	}
	public static String urldecode(String p_string) {
		StringBuffer sb = new StringBuffer();
		byte[] buffer;
		int b;
		int i;
		try {
		   buffer = p_string.getBytes("ISO8859_1");
		} catch (Exception ex) {
		   ex.printStackTrace();
			return("");
		}
		i = 0;
		for (;;) {
			if (i >= buffer.length)
			   break;
			if (buffer[i] == '|') {
			   i++;
				if (buffer[i] == '|') {
				   i++;
					sb.append("|");
					continue;
				}
				else if (buffer[i] == 'u') {
				   i++;
	            b = hextoint(buffer[i+2]);
					b <<= 4;
	            b += hextoint(buffer[i+3]);
					b <<= 4;
	            b += hextoint(buffer[i+0]);
					b <<= 4;
	            b += hextoint(buffer[i+1]);
					i += 4;
		         sb.append((char) b);
					continue;
				}
				else
				   continue;
			}
			else if (buffer[i] == '%') {
			   i++;
				if (buffer[i] >= '0' && buffer[i] <= '9' )
				   b = buffer[i]-'0';
				else if (buffer[i] >= 'A' && buffer[i] <= 'F')
				   b = buffer[i]-'A' + 10;
				else if (buffer[i] >= 'a' && buffer[i] <= 'f')
				   b = buffer[i]-'a' + 10;
				else
					continue;
				i++;
				b <<= 4;
				if (buffer[i] >= '0' && buffer[i] <= '9') 
				   b += buffer[i]-'0';
				else if (buffer[i] >= 'A' && buffer[i] <= 'F')
				   b += buffer[i]-'A' + 10;
				else if (buffer[i] >= 'a' && buffer[i] <= 'f')
				   b += buffer[i]-'a' + 10;
				else 
					continue;
		      sb.append((char) b);
			   i++;
			}
			else {
		      sb.append((char) buffer[i]);
			   i++;
		   }
		}
		return(sb.toString());
	}
	public static String urlencode(String p_string) {
		if (p_string == null) 
		   return("");
	   int i;
		StringBuffer sb = new StringBuffer();
		/*
		byte[] buffer;
		try {
		    buffer = p_string.getBytes("ISO8859_1");
		} catch (Exception ex) {
		   ex.printStackTrace();
			return("");
		}
		*/
		char[] buffer = p_string.toCharArray();
		for (i=0; i<buffer.length; i++) {
			/*
		   if (buffer[i] == '|') {
			   sb.append("||");
			}
			else */ if ((buffer[i] >= 'a' && buffer[i] <= 'z') ||
			    (buffer[i] >= 'A' && buffer[i] <= 'Z') ||
				 (buffer[i] >= '0' && buffer[i] <= '9')) {
			   sb.append((char) buffer[i]);
		   } else if (buffer[i] < 256) {
				sb.append("%");
			   switch ((buffer[i] >> 4) & 0xf) {
				   case 0: sb.append("0"); break;
				   case 1: sb.append("1"); break;
				   case 2: sb.append("2"); break;
				   case 3: sb.append("3"); break;
				   case 4: sb.append("4"); break;
				   case 5: sb.append("5"); break;
				   case 6: sb.append("6"); break;
				   case 7: sb.append("7"); break;
				   case 8: sb.append("8"); break;
				   case 9: sb.append("9"); break;
				   case 10: sb.append("A"); break;
				   case 11: sb.append("B"); break;
				   case 12: sb.append("C"); break;
				   case 13: sb.append("D"); break;
				   case 14: sb.append("E"); break;
				   case 15: sb.append("F"); break;
				}
			   switch (buffer[i] & 0xf) {
				   case 0: sb.append("0"); break;
				   case 1: sb.append("1"); break;
				   case 2: sb.append("2"); break;
				   case 3: sb.append("3"); break;
				   case 4: sb.append("4"); break;
				   case 5: sb.append("5"); break;
				   case 6: sb.append("6"); break;
				   case 7: sb.append("7"); break;
				   case 8: sb.append("8"); break;
				   case 9: sb.append("9"); break;
				   case 10: sb.append("A"); break;
				   case 11: sb.append("B"); break;
				   case 12: sb.append("C"); break;
				   case 13: sb.append("D"); break;
				   case 14: sb.append("E"); break;
				   case 15: sb.append("F"); break;
				}
		   } else {
		      Sprintf sp = new Sprintf("%02x");
				int ci = buffer[i];
		   	sb.append("|u");
				sb.append(sp.add(ci & 0xff).toString());
				sp.clear();
				sb.append(sp.add((ci>>8) & 0xff).toString());
			}
		}
		return(sb.toString());
	}
   private static int Url_processParameterAction(char p_command, 
	                 String p_paramName, String p_newValue, 
						  StringBuffer sb, StringBuffer name, StringBuffer value) {
		if (name == null) return(0);
		if (!name.toString().equals(p_paramName)) {
			if (sb.charAt(sb.length()-1) != '?')
				sb.append("&");
			sb.append(name.toString());
			sb.append("=");
			if (value != null) {
				sb.append(value.toString());
			}
			return(0);
		}
		switch (p_command) {
		   case 'R': 
			   if (sb.charAt(sb.length()-1) != '?')
				   sb.append("&");
			   sb.append(name.toString());
				sb.append("=");
			   if (p_newValue != null) {
				   sb.append(p_newValue);
			   }
			   break;
		   case 'D':
			   break;
		}
		return(1);
	}
	// p_command = 'R' replace
	// p_command = 'D' delete
	public static String Url_processParameter(char p_command, String p_Url, String p_paramName, String p_newValue) {
		int state = 0;
		int idx = 0;
		int actioncnt = 0;
		StringBuffer sb = new StringBuffer();
		StringBuffer name = null;
		StringBuffer value = null;
	   for (;;) {
		   char c;
			if (idx >= p_Url.length()) {
		      switch (state) {
			      case 0: 
					   if (p_command == 'R')
						   sb.append("?");
					   break;
			      case 1: 
					   actioncnt += Url_processParameterAction(p_command, p_paramName, p_newValue, sb, name, value);
					   break;
			      case 2: 
					   actioncnt += Url_processParameterAction(p_command, p_paramName, p_newValue, sb, name, value);
					   break;
				}
				break;
			}
			c = p_Url.charAt(idx++);
		   switch (state) {
			   case 0: // before ?
				   if (c == '?') {
					   sb.append(c);
						state = 1;
			      }
					else {
					   sb.append(c);
					}
					break;
			   case 1: // within parameter before =
				   if (c == '&') {
					   // process the tag
	               actioncnt += Url_processParameterAction(p_command, p_paramName, p_newValue, sb, name, value);
						name = null;
						value = null;
						state = 1;
					}
				   else if (c == '=') {
					   state = 2;
					}
					else {
						if (name == null)
						   name = new StringBuffer();
					   name.append(c);
					}
					break;
			   case 2: // within parameter after =
				   if (c == '&') {
					   // process the tag
	               actioncnt += Url_processParameterAction(p_command, p_paramName, p_newValue, sb, name, value);
						name = null;
						value = null;
						state = 1;
					}
					else {
						if (value == null)
						   value = new StringBuffer();
					   value.append(c);
					}
				   break;
			}
		}
	   if (p_command == 'R' && actioncnt <= 0) {
			if (sb.charAt(sb.length()-1) != '?')
				sb.append("&");
			sb.append(p_paramName);
			sb.append("=");
			if (p_newValue != null) {
				sb.append(p_newValue);
			}
		}
		return(sb.toString());
	}
	public static String urlStringInXsl(String p_urlstring) {
		if (p_urlstring == null) 
		   return(null);
		StringBuffer sb = new StringBuffer();
	   for (int i=0; i<p_urlstring.length(); i++) {
			switch (p_urlstring.charAt(i)) {
			   case '&': 
				   sb.append("&amp;");
				   break;
			   default:  
				   sb.append(p_urlstring.charAt(i));
				   break;
			}
	   }
		return(sb.toString());
	}
	public static String toString(Object p_object) {
		return(p_object == null ? "" : p_object.toString());
	}
	public static String encodeInQuote(String p_string) {
		if (p_string == null)
		   return("");
	   if (p_string.indexOf('"') >= 0) 
		   return("'"+p_string+"'");
		return("\""+p_string+"\"");
	}
	public static String strpart(String p_string, int p_offset, int p_length) {
	   if (p_string == null) 
		   return("");
		if (p_offset < 0)
		   return("");
		int length;
		if (p_length < 0) 
		   length = p_string.length() - p_offset;
		else if (p_offset+p_length > p_string.length())
		   length = p_string.length() - p_offset;
	   else
		   length = p_length;
	   if (length <= 0) 
		   return("");
	   return(p_string.substring(p_offset, p_offset+length));
	}
	public static String sr(String p_string) {
		if (p_string == null)
		   return(null);
	   String str1 = p_string.trim();
		if (str1.equals(""))
		   return("");
		if (str1.length() == p_string.length())
		   return(p_string);
	   int idx = p_string.indexOf(str1);
		return(p_string.substring(0, idx+str1.length()));
	}
	public static boolean allDigit(String s)
	{
		char [] b = s.toCharArray();
		for(int i=0;i<b.length;i++) {
			if(b[i] < '0' || b[i] > '9')
				return(false);
		}
		return(true);
	}
	public static String stripNumber(String s)
	{
		char [] b = s.toCharArray();
		char [] a = new char[b.length];
		int j = 0;
		for(int i=0;i<b.length;i++) {
			if(b[i] >= '0' && b[i] <= '9') {
				a[j] = b[i];
				j++;
			}
			if(b[i] == '.') break;
		}
		return(new String(a,0,j));
	}

	public static String stripDecimal(String s) {
		char [] b = s.toCharArray();
		char [] a = new char[b.length];
		int j = 0;
		for(int i=0;i<b.length;i++) {
			if((b[i] >= '0' && b[i] <= '9') || b[i] == '.' || b[i] == '-') {
				a[j] = b[i];
				j++;
			}
		}
		return(new String(a,0,j));
	}
	public static double atof(String s)
	{
		char [] b = s.toCharArray();
		char [] a = new char[b.length];
		int j = 0;
		for(int i=0;i<b.length;i++) {
			if((b[i] >= '0' && b[i] <= '9') || b[i] == '.') {
				a[j] = b[i];
				j++;
			} else break;
		}
		try {
			return(Double.parseDouble(new String(a,0,j)));
		} catch (Exception ex) {
			UniLog.log("atof " + s + "," + new String(a,0,j));
			UniLog.log(ex);
			return(0.0);
		}
	}
	public static String exceptionToDisplay(Exception p_ex) {
		if (p_ex == null)
		   return("");
	   String str = p_ex.toString();
	   int cc = str.toLowerCase().indexOf("exception:");
		if (cc < 0)
		   return(str);
	   return(str.substring(cc+10).trim());
	}
	public static String modifyDosCompatibleFilename(String p_origFilename) {
		StringBuffer sb = new StringBuffer();
	   int cnt = p_origFilename.length();
	   for (int i=0; i<cnt; i++) {
		   switch (p_origFilename.charAt(i)) {
			   case '"': 
				   sb.append('\'');
				   break;
			   case '>': 
			   case '<': 
			   case '|': 
			   case '\\': 
			   case '/': 
			   case ':': 
			   case '?': 
			   case '*': 
				   sb.append('_');
					break;
			   default:  
				   sb.append(p_origFilename.charAt(i));
			      break;
			}
		}
		return(sb.toString());
	}
	public static String modifyUrlDelimiter(String p_url, char p_from, char p_to) {
	   StringBuffer sb = new StringBuffer();
		int i = 0;
		if (p_url.indexOf('?') > 0) {
		   sb.append(StringUtil.strpart(p_url, 0, p_url.indexOf('?')))
			  .append('?');
		   i = p_url.indexOf('?')+1;
		}
	   for (; i<p_url.length(); i++) {
		   if (p_url.charAt(i) == p_from)
			   sb.append(p_to);
		   else
			   sb.append(p_url.charAt(i));
		}
		return(sb.toString());
	}
	public static double stringSimilarity(String p_str0, String p_str1) {
		if (p_str0.length() < p_str1.length())
	      return(stringSimilarity(p_str0.toCharArray(), p_str1.toCharArray()));
	   else
	      return(stringSimilarity(p_str1.toCharArray(), p_str0.toCharArray()));
	}
	private static double stringSimilarity(char[] p_ca0, char[] p_ca1) {
	   double retval = 0;
	   for (int j=0; j<p_ca1.length; j++) {
		   int k=j;
			double thisval = 0;
			int lasti = -1;
		   for (int i=0; i<p_ca0.length; i++) {
			   if (k >= p_ca1.length)
				   k = 0;
			   if (p_ca1[k] == p_ca0[i]) {
				   if (lasti == i-1) {
				      retval -= thisval;
					   thisval += thisval;
					}
					else {
					   thisval = 0;
					}
					thisval++;
					lasti = i;
					retval += thisval;
			   }
				k++;
			}
		}
		return(retval);
	}

	public static String ftostr(double p_dbval,String p_format)
	{
		DecimalFormat fmt;
		FieldPosition pos=new FieldPosition(NumberFormat.INTEGER_FIELD);
		StringBuffer sb = new StringBuffer();
		fmt = new DecimalFormat(p_format);
		sb = fmt.format(p_dbval,sb,pos);
		return(sb.toString());
	}
	public static String preformatText(final String p_string, final int p_width) {
		return(
	   new Object() {
	      StringBuffer sb = new StringBuffer();
	      char[] carr = p_string.toCharArray();
		   int width = 0;
		   int linestart = 0;
		   int linelen = 0;
			int lastspace = -1;
	      StringBuffer tsb = new StringBuffer();
			void flush(int p_len, boolean p_skiplf) {
				if (p_len > 0)
				   sb.append(carr, linestart, p_len);
				if (!p_skiplf) {
				   sb.append((char) CR);
				   sb.append((char) LF);
				}
				linelen -= p_len > 0 ? p_len : 0;
				width = 0;
				if (linelen == 0)
				   lastspace = -1;
			}
			void flush(int p_len) {
			   flush(p_len, false);
			}
			String format() {
		      for (int i=0; i<carr.length; i++) {
		         char c = carr[i];
			      if (c == LF) {
		            flush(linelen);
						linestart = i+1;
			      }
			      else if (c == CR) {
		            flush(linelen);
				      if (i+1 < carr.length && carr[i+1] == LF)
					      i++;
						linestart = i+1;
			      }
			      else if (c == ' ') { 
					   if (width >= p_width) {
		               flush(linelen);
							linestart = i;
						}
			         lastspace = i;
						width++;
						linelen++;
			      }
					else {
					   if (width >= p_width) {
						   if (lastspace >= 0) {
								int nextlinestart = lastspace+1;
							   flush(lastspace-linestart+1, c == LF || c == CR);
		                  linestart = nextlinestart;
				            lastspace = -1;
								width = 0;
								for (int j=linestart; j<i; j++) {
								   width++;
		                     if (carr[j] >= 256)
								      width++;
								}
							}
							else {
							   flush(linelen, c == LF || c == CR);
		                  linestart = i;
						   }
							i--;  // start over again
						}
						else {
						   if (c == ' ' || c > 256)
							   lastspace = i;
						   linelen++;
							width++;
		               if (c >= 256)
								width++;
						}
					}
		      }
				if (linelen > 0) 
				   flush(linelen);
			   return(sb.toString());
			}
		}.format());
	}
	public static String preformatText_old(String p_string, int p_width) {
	   StringBuffer sb = new StringBuffer();
	   char[] carr = p_string.toCharArray();
		int state = 0;
		int width = 0;
		for (int i=0; i<carr.length; i++) {
		   char c = carr[i];
		   switch (state) {
			   case 0: 
				   if (c == 13) {
					   sb.append((char) 13);
					   sb.append((char) 10);
						width = 0;
						state = 1;
					} 
					else if (c == 10) {
					   sb.append((char) 13);
					   sb.append((char) 10);
						width = 0;
					   state = 0;
					}
					else {
					   sb.append(c);
						width++;
						if (c >= 256)
						   width++;
						if (width >= p_width) {
					      sb.append((char) 13);
					      sb.append((char) 10);
							width = 0;
					   }
					   state = 0;
					}
				   break;
			   case 1:
				   if (c == 13) {
					   sb.append((char) 13);
					   sb.append((char) 10);
						width = 0;
						state = 1;
					} 
					else if (c == 10) {
					   state = 0;
					}
					else {
					   sb.append(c);
						width++;
						if (c >= 256)
						   width++;
						if (width >= p_width) {
					      sb.append((char) 13);
					      sb.append((char) 10);
							width = 0;
					   }
					   state = 0;
					}
				   break;
			}
		}
		return(sb.toString());
	}
	static public String toFirstLetterUpperCase(String p_str) {
		char[] carr = p_str.toCharArray();
		int state = 0;
		StringBuffer sb = new StringBuffer();
	   for (int i=0; i<carr.length; i++) {
		   char c = carr[i];
		   switch (state) {
			   case 0: 
			      if (c >= 'A' && c <= 'Z') {
					   sb.append(c);
					   state = 1;
					}
			      else if (c >= '0' && c <= '9') {
					   sb.append(c);
					   state = 1;
					}
			      else if (c >= 'a' && c <= 'z') {
					   sb.append(Character.toUpperCase(c));
					   state = 1;
					}
					else
					   sb.append(c);
					break;
			   case 1:
				   if (c >= '0' && c <= '9') {
					   sb.append(c);
					}
				   else if (c >= 'a' && c <= 'z') {
					   sb.append(c);
					}
				   else if (c >= 'A' && c <= 'Z') {
					   sb.append(Character.toLowerCase(c));
					}
					else {
					   sb.append(c);
						state = 0;
					}
				   break;
			}
		}
		return(sb.toString());
	}
	public static String byteArrayToString(byte[] p_bytes) {
	   StringBuffer sb = new StringBuffer();
		Sprintf sf = new Sprintf("%02x");
		for (int i=0; i<p_bytes.length; i++) {
		   int cc = p_bytes[i];
			if (cc < 0)
			   cc = cc + 256;
		   sf.clear();
		   sb.append(sf.add(cc).toString());
		}
		return(sb.toString());
	}
	public static byte[] stringToByteArray(String p_message) {
	   int len = p_message.length();
		byte[] bytes = new byte[len/2];
	   for (int i=0, j=0; i<len; i += 2, j++) {
			int cc = ((hextoint((byte) p_message.charAt(i)) << 4)
			       + hextoint((byte) p_message.charAt(i+1)));
			if (cc >= 128)
			  cc -= 256;
			bytes[j] = (byte) cc;
		}
		return(bytes);
	}
	public static String trimAll(String p_str) {
	   StringBuffer sb = new StringBuffer();
		char[] carr = p_str.toCharArray();
		for (int i=0; i<carr.length; i++) {
		   if (carr[i] == ' ' 
			    || carr[i] == '\t' 
			    || carr[i] == '\n')
		      continue;
		   sb.append(carr[i]);
		}
		return(sb.toString());
	}
	public static Vector breakOptions(String p_str) {
      char[] ca = p_str.toCharArray();
		int state = 0;
		StringBuffer sb = null;
		Vector v = new Vector();
		for (int i=0; i<ca.length; i++) {
		   char c = ca[i];
		   switch (state) {
			   case 0:
				   if (c == '|') {
					   state = 1;
						sb = new StringBuffer();
					}
					else
					   state = -1;
				   break;
			   case 1:
				   if (c == '|') {
					   state = 0;
						v.addElement(sb.toString());
					}
					else
					   sb.append(c);
				   break;
			}
		}
		return(v);
	}
	public static String toNumberOnly(String s) {
	   StringBuffer sb = new StringBuffer();
		int fSign= 0;
		char [] b = s.toCharArray();
		for (int i=0; i<b.length; i++) {
			if (b[i] == '-' && fSign == 0) {
			   sb.append(b[i]);
			   fSign = -1;
			}
			else if ((b[i] >= '0' && b[i] <= '9') || b[i] == '.') {
			   sb.append(b[i]);
				if (fSign == 0)
				   fSign = 1;
			}
		}

		return(sb.length() <= 0 ? "0" : sb.toString());
	}
	public static String filterBaseFileName(String s) {
	   StringBuffer sb = new StringBuffer();
		char [] b = s.toCharArray();
		for (int i=0; i<b.length; i++) {
			switch (b[i]) {
			   case '?':
			   case '/':
			   case '\\':
			   case ':':
					sb.append("_");
				   break;
			   default:
					sb.append(b[i]);
				   break;
			}
		}
		return(sb.toString());
	}
	public static String urlRemoveParameter(String p_url, String p_parameter) {
	   int pos = p_url.indexOf('?');
	   Hashtable args = new Hashtable();
	   StringUtil.parseCommand(args, p_url);
	   args.remove(p_parameter);
		return(p_url.substring(0, pos)+"?"+StringUtil.getUrlFromHash(args, "&").substring(1));
	}
	
	public static String limitOccurrence(String p_inStr, String p_regex, int p_maxCount){
		if (p_inStr == null || p_inStr.length() == 0){
			return(p_inStr);
		}
		StringBuffer resultSb = new StringBuffer();
		try{
			String inStrSplit[] = p_inStr.split("\n", p_maxCount+1);
			for (int i=0;i<p_maxCount && i<inStrSplit.length; i++){
				if (resultSb.length() != 0){
					resultSb.append("\n");
				}
				resultSb.append(inStrSplit[i]);
			}
		}
		catch(Exception ex){ 
			ex.printStackTrace();
		}
		return(resultSb.toString());
	}
	/*
	public static void abc(float p_f) {
	   UniLog.log("abc:haha:0");
	}
	public static void abc(double p_d) {
	   UniLog.log("abc:haha:1");
	}
	public static void main_xxx(String args[]) throws Exception {
      //System.out.println(convert(args[0]));
      //System.out.println("basename="+basename(args[0]));
      //System.out.println("dirname="+dirname(args[0]));
	   //System.out.println("["+args[0]+"]");
	   //System.out.println("["+urlencode(args[0])+"]");
	   //System.out.println("["+urldecode(urlencode(args[0]))+"]");
	   //System.out.println("processParameter() return "+Url_processParameter(args[0].charAt(0), args[1], args[2], args[3]));
	   //System.out.println("urlStringInXsl() return "+urlStringInXsl(args[0]));
		//System.out.println("["+strpart(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]))+"]");
		//System.out.println("sr["+args[0]+"] = ["+sr(args[0])+"]");
	   //StringTokenizer st = new StringTokenizer("asdfa,asdfa,dfdies", ",");
		//while (st.hasMoreTokens()) {
		   //UniLog.log(st.nextToken());
		//}
	   //UniLog.log("Double.MIN_VALUE="+Double.MIN_VALUE);
	   //UniLog.log("Double.MAX_VALUE="+Double.MAX_VALUE);
	   //UniLog.log("Float.MIN_VALUE="+Float.MIN_VALUE);
	   //UniLog.log("Float.MAX_VALUE="+Float.MAX_VALUE);
		//abc(1.1);
	   //UniLog.log("URLDecoder.decode()=["+new URLDecoder().decode(args[0])+"]");
	   //UniLog.log("JsString()="+jsString(args[0]));
		//String[] abc = new String[2];
		//UniLog.log("abc.getClass().getName()="+abc.getClass().getName());
		//UniLog.log("abc.getClass().isArray()="+abc.getClass().isArray());
		//UniLog.log("abc.getClass().getComponentType()="+abc.getClass().getComponentType().getName());
		//UniLog.log("abc instanceof String[] ="+(abc instanceof String[]));
      //long longbits = Double.doubleToLongBits(Double.parseDouble(args[0]));
      //for (int i=0;i<8;i++) {
			//UniLog.log(new Sprintf("%d").add(longbits & 0xff).toString());
	      //longbits >>=8;
      //}
		String path = args[0];
		String sep = ""+path.charAt(0);
		if (!sep.equals("/") && !sep.equals("\\"))
		   throw(new Exception("Not absolute path"));
		path = StringUtil.strpart(path, 1, -1);
	   for (;;) {
			int cc = path.indexOf(sep);
			String node = null;
			if (cc < 0)
			   node = path;
			else
			   node = StringUtil.strpart(path, 0, cc);
			path = StringUtil.strpart(path, node.length()+1, -1);
			UniLog.log("Search for ["+node+"]");
			if (path.length() == 0) {
				UniLog.log("path found");
			   break;
		   }
		}
	}
	public static void main_031223(String args[]) throws Exception {
	   UniLog.log("modifyDosCompatibleFilename()="+modifyDosCompatibleFilename(args[0]));
	}
	public static void main_031224(String args[]) throws Exception {
		UniLog.log("haha:0");
	   UniLog.log("stringSimilarity("+args[0]+","+args[1]+")"+stringSimilarity(args[0], args[1]));
		UniLog.log("haha:1");
	}
	public static void main_040406(String args[]) throws Exception {
		UniLog.log(""+readUrlIntoCellCollection(args[0]).toXMLRecursive(new StringBuffer()).toString());
	}
	public static void main(String args[]) throws Exception {
		UniLog.log(""+preformatText(args[1], Integer.parseInt(args[0])));
	}
	public static void main(String args[]) throws Exception {
		UniLog.log(""+toFirstLetterUpperCase(args[0]));
	}
	public static void main(String args[]) throws Exception {
	   byte[] bytes = stringToByteArray(args[0]);
		for (int i=0; i<bytes.length; i++) {
		  UniLog.log("["+i+"]="+bytes[i]);
		}
	}
	public static void main(String args[]) throws Exception {
      for (StringTokenizer stk = new StringTokenizer(args[0], "/"); 
           stk.hasMoreTokens(); ) {
         String part = stk.nextToken();
		   UniLog.log(part);
      }
	}
	public static void main(String args[]) throws Exception {
	   CellCollection cc = readUrlIntoCellCollection(args[0]);
		UniLog.log("cc="+cc.toXMLRecursive(new StringBuffer()).toString());
	}
	*/
	public static void main(String args[]) throws Exception {
	   //UniLog.log(toNumberOnly(args[0]));
	   //UniLog.log("xxx=["+ftostr(Double.parseDouble(args[0]), args[1])+"]");
	   UniLog.log(""+trimSqlSafe("abc"));
	   UniLog.log(""+trimSqlSafe("abc.\"d\\ef"));
	   UniLog.log(""+trimSqlSafe("abc.def#"));
	   UniLog.log(""+trimSqlSafe("abc'def"));
	   
	}
	public static String stripNonPrintable(String p_s)
	{
		StringBuffer sb = new StringBuffer();
		char[] ca = p_s.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if(ca[i] < 32) continue;
			if(Character.isWhitespace(ca[i])) sb.append(' '); else sb.append(ca[i]);
		}
		return(sb.toString());
	}
	
	public static String concatString(String separator,List v)
	{
		if(v==null) return(null);
		String s="";
		for(int i = 0;i<v.size();i++) {
			if(i > 0) s += separator;
			s += v.get(i).toString();
		}
		return(s);
	}
	public static String removeLastChar(String p_str){
		if (p_str == null) return (null);
		if (p_str.length() == 0) return ("");
		return(p_str.substring(0, p_str.length() - 1));
	}
	
	/***
	 * translate simplified regular expression(only support *) to regular expression
	 * @param p_inString
	 * @return regular expression
	 */
    public static String matchStringTranslateRE(String p_inString){
   		//simplified regular expression * - wildcard
   		StringBuffer sbTag = new StringBuffer(p_inString.replace("*", ".*"));
   		sbTag.insert(0, "^.*");
   		sbTag.append(".*$");
   		//UniLog.log(String.format("field:%s sTag:%s result:%s", field, sbTag, field.matches(sbTag.toString()))); 
   		return(sbTag.toString());
    }
    /***
     * mainly for zkbi quick search
     * @param p_field
     * @param p_searchTag
     * @param p_trimAndIgnoreCase
     * @return
     */
    public static boolean matchString(String p_field, String p_searchTag, boolean p_trimAndIgnoreCase){
    	try{
	    	String field = p_trimAndIgnoreCase ? p_field.trim().toLowerCase() : p_field;
	    	String searchTag = p_trimAndIgnoreCase ? p_searchTag.trim().toLowerCase() : field;
	    	if (p_searchTag.length() >= 2 && p_searchTag.startsWith("=")){ 
	    		//normal search
	    		return(field.equals((searchTag.substring(1))));
	    	}
	    	else if (p_searchTag.contains("*")){
	    		return(field.matches(matchStringTranslateRE(searchTag)));
	    	}
	    	else {
	    		return(field.contains(searchTag));
	    	}
    	}
    	catch(Exception ex){
    		UniLog.log("match error: " + ex.getMessage());
    		return(false);
    	}
    }
    /***
     * build and update attrbiute string
     * e.g. NNNNN -> YNNNN
     * @param p_idx
     * @param p_flag
     * @param p_attr
     * @param p_length
     * @return
     */
	public static String buildCharAttr(int p_idx, char p_flag, String p_attr, int p_length){
		if (p_idx < 0 || p_idx > p_length){
			UniLog.log("invalid idx, ignore update. idx:"+p_idx);
			return p_attr;
		}
		
		char[] attrChars = new char[p_length + 1];
		for (int i=0; i<attrChars.length; i++){
			attrChars[i] = ' ';
		}
		if (p_attr != null){
			for (int i=0; i<p_attr.length() && i<attrChars.length;i++){
				attrChars[i] = p_attr.charAt(i);
			}
		}
		attrChars[p_idx] = p_flag;
		String newAttr = StringUtil.sr(String.valueOf(attrChars));
		//UniLog.logm(null,"debug:%d %s %s -> [%s]", p_idx, p_flag, p_attr, newAttr);
		return(newAttr);
	}
	/**
	 * get attribute char from attribute string
	 * @param p_idx
	 * @param p_attr
	 * @return return ' ' if attr not exist
	 */
	public static char getCharAttrOne(int p_idx, String p_attr){
		char result = ' ';
		if (p_attr != null && p_attr.length() > p_idx){
			result = p_attr.charAt(p_idx);
		}
		//UniLog.logm(null,"debug:[%d,%s] -> %s", p_idx, p_attr, result);
		return(result);
	} 
	
	/***
	 * parse url string to map
	 * @param url - e.g. abc.jsp?var1=val1&var2=val2
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, List<String>> getURLParamMap(String url) throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		if (url == null){
			return query_pairs;
		}
		final String[] pairs = url.substring(url.indexOf('?') < 0 ? 0 : (url.indexOf('?') + 1)).split("&");
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!query_pairs.containsKey(key)) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
	}
	/***
	 * trim out sql non safe char
	 * @param p_str
	 * @return
	 */
	public static String trimSqlSafe(String p_str){
		if (p_str == null) return "";
		return(p_str.replaceAll("[;'\"]", ""));
	}
	
	static public int getChineseCharCount(String ss) {
		char ca[] = ss.toCharArray();
		int n=0;
		for(int i=0;i<ca.length;i++) {
			if(ca[i] > 255) n++;
		}
		return(n);
	}

	static public Pair countChineseCharInBytest(String ss,int p_maxBytes) {
		char ca[] = ss.toCharArray();
		int nb =0;
		int nc =0;
		for(;nc<ca.length;nc++) {
			int n;
			if(ca[nc] > 255) n = 2; else n = 1;
			if( nb + n > p_maxBytes) break;
			nb += n;
		}
		return(Pair.of(nb,nc));
	}
	
	/***
	 * for compare schn tchn str
	 * normalize instr, then compare equal
	 * @param p_str1
	 * @param p_str2
	 * @return
	 */
	public static boolean equalIgnoreEncode(String p_str1, String p_str2) {
		//normalize instr by convert it to big5
		String str1b = ChineseConvert.convertAuto2Bnew(p_str1);
		String str2b = ChineseConvert.convertAuto2Bnew(p_str2);
		//UniLog.log1("org:[%s,%s,%s] auto2b:[%s,%s,%s]", p_str1, p_str2, StringUtils.equals(p_str1, p_str2), str1b, str2b, StringUtils.equals(str1b, str2b));
		
		//compare big5 value
		return StringUtils.equals(str1b, str2b);
	}
	
	
	public static int countSubstring(String str, String subStr) {
	    int count = 0;
	    int index = 0;
	    while ((index = str.indexOf(subStr, index)) != -1) {
	        count++;
	        index += subStr.length();
	    }
	    return count;
	}
	
	public static String defaultStringIfBlank(String str, String... defaultStrs) {
		List<String> list = Lists.asList(str, defaultStrs);
		for (String s : list) {
			if (StringUtils.isNotBlank(s))
				return s;
		}
		return list.get(list.size() - 1);
	}

}
