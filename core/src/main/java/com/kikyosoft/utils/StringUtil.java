package com.kikyosoft.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.*;

public class StringUtil {

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
    public static final char LF = 10;
    public static final char CR = 13;
    public static final char BS = 32;

    public static String jsStringTrim(String s) {
        return jsString(StringUtils.trimToEmpty(s));
    }

    public static String jsString(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else if (c == 0x09 || c == 0x0a || c == 0x0d ||
                    (c >= 0x20 && c <= 0x7e) ||
                    (c >= 0x80 && c <= 0xd7ff) ||
                    (c >= 0xe000 && c <= 0xf8ff) ||
                    (c >= 0xf900 && c <= 0xfffd) ||
                    (c >= 0x10000 && c <= 0x10ffff)) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append("\\u003f"); // '?'
            }
        }
        return sb.toString();
    }

    public static String convertWebStringTrim(String s) {
        return convertWebString(StringUtils.trimToEmpty(s));
    }

    public static String convertWebString(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else if (c == 0x09 || c == 0x0a || c == 0x0d ||
                    (c >= 0x20 && c <= 0x7e) ||
                    (c >= 0x80 && c <= 0xd7ff) ||
                    (c >= 0xe000 && c <= 0xf8ff) ||
                    (c >= 0xf900 && c <= 0xfffd) ||
                    (c >= 0x10000 && c <= 0x10ffff)) {
                sb.append("&#").append((int) c).append(";");
            } else {
                sb.append("&#63;"); // '?'
            }
        }
        return sb.toString();
    }

    public static String basename(String filename) {
        if (filename == null) return null;
        return filename.substring(filename.lastIndexOf('/') + 1).replace("\\", "/");
    }

    public static String fileExtension(String filename) {
        if (filename == null) return null;
        int index = filename.lastIndexOf('.');
        return index >= 0 ? filename.substring(index + 1) : null;
    }

    public static String dirname(String filename) {
        return dirname(filename, '/');
    }

    public static String dirname(String filename, char sep) {
        if (filename == null) return null;
        int index = filename.lastIndexOf(sep);
        return index >= 0 ? filename.substring(0, index) : ".";
    }

    public static String urlencode(String s) {
        try {
            return URLEncoder.encode(StringUtils.defaultString(s), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public static String urldecode(String s) {
        try {
            return URLDecoder.decode(StringUtils.defaultString(s), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public static String trimSqlSafe(String s) {
    	return StringUtils.defaultString(s).replaceAll("[;'\\\"]", "");
    }

    public static String toString(Object o) {
        return StringUtils.defaultString(Objects.toString(o, ""));
    }

    public static String encodeInQuote(String s) {
        if (s == null) return "";
        return s.contains("\"") ? "'" + s + "'" : "\"" + s + "\"";
    }

    public static boolean allDigit(String s) {
        return StringUtils.isNumeric(StringUtils.defaultString(s));
    }

    public static String removeLastChar(String s) {
        if (StringUtils.isEmpty(s)) return s;
        return s.substring(0, s.length() - 1);
    }

    public static String toFirstLetterUpperCase(String s) {
        if (StringUtils.isBlank(s)) return s;
        String[] parts = s.split("\\s");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = StringUtils.capitalize(parts[i].toLowerCase());
        }
        return String.join(" ", parts);
    }

    public static String trimAll(String s) {
        return StringUtils.deleteWhitespace(StringUtils.defaultString(s));
    }

    public static String strpart(String s, int offset, int length) {
        if (s == null || offset < 0 || offset >= s.length()) return "";
        int end = length < 0 ? s.length() : Math.min(s.length(), offset + length);
        return s.substring(offset, end);
    }

    public static String sr(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        return s.substring(0, s.indexOf(trimmed) + trimmed.length());
    }

    public static String filterBaseFileName(String s) {
        return StringUtils.defaultString(s).replaceAll("[?/\\\\:*|<>]", "_");
    }

    public static String matchStringTranslateRE(String inStr) {
        return "^.*" + inStr.replace("*", ".*") + ".*$";
    }

    public static boolean matchString(String field, String searchTag, boolean ignoreCase) {
        try {
            String f = ignoreCase ? StringUtils.lowerCase(field.trim()) : field;
            String tag = ignoreCase ? StringUtils.lowerCase(searchTag.trim()) : searchTag;
            if (searchTag.startsWith("=") && searchTag.length() >= 2) {
                return f.equals(tag.substring(1));
            } else if (searchTag.contains("*")) {
                return f.matches(matchStringTranslateRE(tag));
            } else {
                return f.contains(tag);
            }
        } catch (Exception ex) {
            LogUtil.log("match error: " + ex.getMessage());
            return false;
        }
    }

    public static String getTodayString() {
        return new java.sql.Date(System.currentTimeMillis()).toString();
    }

    public static String exceptionToDisplay(Exception e) {
        if (e == null) return "";
        String msg = e.toString().toLowerCase();
        int idx = msg.indexOf("exception:");
        return idx >= 0 ? msg.substring(idx + 10).trim() : msg;
    }

    public static double atof(String s) {
        try {
            return Double.parseDouble(StringUtils.defaultString(s).replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException ex) {
            LogUtil.log("atof: " + ex.getMessage());
            return 0.0;
        }
    }

    public static String ftostr(double value, String format) {
        DecimalFormat fmt = new DecimalFormat(format);
        return fmt.format(value);
    }

    public static String concatString(String sep, List<?> items) {
        return items == null ? null : StringUtils.join(items, sep);
    }
    public static int toIntNumberOnly(String s) {
        if (StringUtils.isBlank(s)) return 0;
        try {
            return Integer.parseInt(s.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            LogUtil.log("Invalid numeric string: " + s);
            return 0;
        }
    }
    public static double toDoubleNumberOnly(String s) {
        if (StringUtils.isBlank(s) || "null".equalsIgnoreCase(s.trim())) return 0.0;
        try {
            return Double.parseDouble(s.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            LogUtil.log("Invalid double string: " + s);
            return 0.0;
        }
    }
    
	public static boolean equalIgnoreEncode(String p_str1, String p_str2) {
		//normalize instr by convert it to big5
		String str1b = ChineseConvert.convertAuto2Bnew(p_str1);
		String str2b = ChineseConvert.convertAuto2Bnew(p_str2);
		//CoreLog.log1("org:[%s,%s,%s] auto2b:[%s,%s,%s]", p_str1, p_str2, StringUtils.equals(p_str1, p_str2), str1b, str2b, StringUtils.equals(str1b, str2b));
		
		//compare big5 value
		return StringUtils.equals(str1b, str2b);
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
	
   public static String cws(String p_string) {
	   return(convertWebString(p_string));
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
    // Add wrappers and redirects here for other methods as needed...

    // TODO: You can continue to refactor the rest similarly if needed
   
   
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
	
}
