package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

public class StringOptUtil {
	public static void main(String args[]){
		UniLog.log(addOpt("111 222 333"," ","111"));
		UniLog.log(addOpt("111 222 333"," ","222"));
		UniLog.log(addOpt("111 222 333"," ","444"));
		UniLog.log(addOpt("111 222 333"," ",""));
		UniLog.log(addOpt(""," ","111"));
		UniLog.log(addOpt(null," ","111"));
		UniLog.log(addOpt("11    2222 33311,1,3",",","1"));
		
		UniLog.log(""+removeOpt("111 222 333"," ","111"));
		UniLog.log(""+removeOpt("111 222 333"," ","222"));
		UniLog.log(""+removeOpt("111 222 333"," ","444"));
		UniLog.log(""+removeOpt("111 222 333"," ",""));
		UniLog.log(""+removeOpt(""," ","111"));
		UniLog.log(""+removeOpt(null," ","111"));
		UniLog.log(""+removeOpt("11    2222 33311,1,3",",","1"));
	}
	/***
	 * remove opt from opt list
	 * @param p_inStr
	 * @param p_separator
	 * @param p_optStr
	 * @return
	 */
	public static String removeOpt(String p_inStr, String p_separator, String p_optStr){
		UniLog.log1("in:[%s] sep:[%s] opt:[%s]", p_inStr, p_separator, p_optStr);
		if (StringUtils.isBlank(p_inStr) || p_separator == null || StringUtils.isBlank(p_optStr)){
			return p_inStr;
		}
		ArrayList<String> splitedList =  new ArrayList<String>(Arrays.asList(p_inStr.split("["+p_separator+"]+")));
		Iterator<String> it = splitedList.iterator();
		while (it.hasNext()) {
			String tmpStr = it.next();
			if (StringUtils.equals(tmpStr, p_optStr) || StringUtils.isBlank(tmpStr)){
				it.remove();
			}
		}
		String outStr = StringUtils.join(splitedList,p_separator);
		return(outStr);
	}
	/***
	 * add opt to opt list
	 * @param p_inStr
	 * @param p_separator
	 * @param p_optStr
	 * @return
	 */
	public static String addOpt(String p_inStr, String p_separator, String p_optStr){
		UniLog.log1("in:[%s] sep:[%s] opt:[%s]", p_inStr, p_separator, p_optStr);
		if (StringUtils.isBlank(p_inStr)){
			return p_optStr;
		}
		if (p_separator == null || StringUtils.isBlank(p_optStr)){
			return p_inStr;
		}
		ArrayList<String> splitedList =  new ArrayList<String>(Arrays.asList(p_inStr.split("["+p_separator+"]+")));
		Iterator<String> it = splitedList.iterator();
		while (it.hasNext()) {
			String tmpStr = it.next();
			if (StringUtils.equals(tmpStr, p_optStr)){
				return p_inStr;
			}
		}
		splitedList.add(p_optStr);
		return StringUtils.join(splitedList,p_separator);
	}
}
