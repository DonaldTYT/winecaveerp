package com.kyoko.common;

import java.io.*;
import java.util.*;

import com.uniinformation.utils.UniLog;

public class NumberUtil {
   static public double max(double p_n0, double p_n1) {
	   return(p_n0 > p_n1 ? p_n0 : p_n1);
	}
   static public double min(double p_n0, double p_n1) {
	   return(p_n0 < p_n1 ? p_n0 : p_n1);
	}
   static public int max(int p_n0, int p_n1) {
	   return(p_n0 > p_n1 ? p_n0 : p_n1);
	}
   static public int min(int p_n0, int p_n1) {
	   return(p_n0 < p_n1 ? p_n0 : p_n1);
	}
   static public double maxDouble(double p_n0, double p_n1) {
	   return(p_n0 > p_n1 ? p_n0 : p_n1);
	}
   static public double minDouble(double p_n0, double p_n1) {
	   return(p_n0 < p_n1 ? p_n0 : p_n1);
	}
   static public int maxInt(int p_n0, int p_n1) {
	   return(p_n0 > p_n1 ? p_n0 : p_n1);
	}
   static public int minInt(int p_n0, int p_n1) {
	   return(p_n0 < p_n1 ? p_n0 : p_n1);
	}
	static public double parseDouble(String p_string) {
	   if (p_string == null)
		   return(0.0);
	   if (p_string.trim().equals(""))
		   return(0.0);
	   if (p_string.trim().equals("null"))
		   return(0.0);
	   try {
		   return(Double.parseDouble(p_string.trim()));
		} catch (NumberFormatException ex) {
		   UniLog.log(ex);
			return(0.0);
		}
	}
	static public long parseLong(String p_string) {
	   if (p_string == null)
		   return(0);
	   if (p_string.trim().equals(""))
		   return(0);
	   if (p_string.trim().equals("null"))
		   return(0);
	   try {
		   return(Long.parseLong(p_string.trim()));
		} catch (NumberFormatException ex) {
		   UniLog.log("p_string=["+p_string+"]");
		   UniLog.log(ex);
			return(0);
		}
	}
	static public int parseInt(String p_string) {
	   if (p_string == null)
		   return(0);
	   if (p_string.trim().equals(""))
		   return(0);
	   if (p_string.trim().equals("null"))
		   return(0);
	   try {
		   return(Integer.parseInt(p_string.trim()));
		} catch (NumberFormatException ex) {
		   UniLog.log("p_string=["+p_string+"]");
		   UniLog.log(ex);
			return(0);
		}
	}
	public static int atoi(String p_string) {
		return atoi(p_string, 0);
	}
	public static int atoi(String p_string, int p_default) {
		try {
			if (p_string == null) {
				UniLog.log1("error:input is null");
				return p_default;
			}
			return Integer.parseInt(p_string.replaceAll("[^0-9]", ""));
		}
		catch(Exception ex) {
			UniLog.log1("error:" +ex.getMessage());
			//ex.printStackTrace();
			return p_default;
		}
	}
	public static void main(String args[]) {
		UniLog.log1("" + atoi("123abc888"));
		UniLog.log1("" + atoi("abc"));
	}
	// 12,345 -> 12345
	static public String removeComma(String p_str, char p_comma) {
		char[] ca = p_str.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<ca.length; i++) {
			if (ca[i] != p_comma)
				sb.append(ca[i]);
		}
		return(sb.toString());
	}
	// 123.00 -> 123
	static public String removeDecimalZero(String p_value) {
		double doubleValue = (new Double(removeComma(p_value, ','))).doubleValue();
		int intValue = (int) doubleValue;
		if (doubleValue != (new Double(""+intValue)).doubleValue()) {
			return(""+StringUtil.ftostr(doubleValue, "#.#########"));
		}
		else {
			return(""+intValue);
		}
	}

	public static String toTChinese(long number) {
		return NumberToChinese.convertToChinese(number);
	}

	public static String toSChinese(long number) {
		return NumberToChinese.convertToChinese(number).replace("萬", "万").replace("億", "亿");
	}
	
	private static class NumberToChinese {
	    private static final String[] CHINESE_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
	    private static final String[] CHINESE_UNITS = {"", "十", "百", "千"};
	    private static final String[] CHINESE_BIG_UNITS = {"", "萬", "億", "兆"};

	    public static String convertToChinese(long number) {
	        if (number == 0) {
	            return CHINESE_NUMBERS[0];
	        }
	        if (number < 0 || number > 999_9999_9999_9999L) {
	            return "Unsupported number range";
	        }

	        List<String> resultParts = new ArrayList<>();
	        int unitPos = 0;
	        boolean lastSegmentWasZero = false;

	        while (number > 0) {
	            long segment = number % 10000;
	            number /= 10000;

	            String segmentStr = convertSegment((int) segment);
	            
	            if (segment > 0) {
	                // 添加大单位（万、亿、兆）
	                segmentStr += CHINESE_BIG_UNITS[unitPos];
	                // 如果前一段是零且当前段不为空，添加零
	                if (lastSegmentWasZero && !resultParts.isEmpty()) {
	                    resultParts.add(CHINESE_NUMBERS[0]);
	                }
	                resultParts.add(segmentStr);
	                lastSegmentWasZero = false;
	            } else {
	                lastSegmentWasZero = true;
	            }

	            unitPos++;
	        }

	        // 合并结果
	        StringBuilder result = new StringBuilder();
	        for (int i = resultParts.size() - 1; i >= 0; i--) {
	            result.append(resultParts.get(i));
	        }

	        // 处理特殊情况："一十"开头简化为"十"
	        String finalResult = result.toString();
	        if (finalResult.startsWith("一十")) {
	            finalResult = finalResult.substring(1);
	        }

	        // 处理连续的零
	        finalResult = finalResult.replaceAll("零+", "零");
	        if (finalResult.endsWith("零")) {
	            finalResult = finalResult.substring(0, finalResult.length() - 1);
	        }

	        return finalResult;
	    }

	    private static String convertSegment(int segment) {
	        if (segment == 0) {
	            return "";
	        }

	        StringBuilder segmentStr = new StringBuilder();
	        int unitPos = 0;
	        boolean lastWasZero = true;

	        while (segment > 0) {
	            int digit = segment % 10;
	            segment /= 10;

	            if (digit != 0) {
	                // 添加数字和单位
	                if (unitPos > 0) {
	                    segmentStr.insert(0, CHINESE_UNITS[unitPos]);
	                }
	                segmentStr.insert(0, CHINESE_NUMBERS[digit]);
	                lastWasZero = false;
	            } else if (!lastWasZero && unitPos > 0) {
	                // 只在非连续零且不是个位时添加零
	                segmentStr.insert(0, CHINESE_NUMBERS[0]);
	                lastWasZero = true;
	            }

	            unitPos++;
	        }

	        return segmentStr.toString();
	    }
	}
}
