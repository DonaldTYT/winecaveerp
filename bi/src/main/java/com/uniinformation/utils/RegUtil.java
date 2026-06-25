package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class RegUtil {
	/***
	 * parse string to array list
	 * @param p_inStr - input string. e.g. "BCKRG0000000001BDTRG0000000002"
	 * @param p_regEx - regular expression. e.g. "BCKRG([0-9]{10})BDTRG([0-9]{10}).*", "BCKRG[0-9]*x"
	 * @return array list of matched pattern
	 *         if not matched, return empty array list
	 */
	public static List<String> parse(String p_inStr, String p_regEx){
		ArrayList<String> resultList = new ArrayList<String>();
		if (StringUtils.isBlank(p_inStr)){
			UniLog.log1("inStr is blank");
			return(resultList);
		}
		if (StringUtils.isBlank(p_regEx)){
			UniLog.log1("regEx is blank");
			return(resultList);
		}
		Pattern pat = Pattern.compile(p_regEx);
		Matcher mat = pat.matcher(p_inStr);
		if (mat.find()){
			if (mat.groupCount() == 0){
				resultList.add(mat.group(0));
			}
			else{
				for (int i=1; i<=mat.groupCount(); i++){
					resultList.add(mat.group(i));
				}
			}
		}
		return(resultList);
	}
	public static void main(String args[]){
		UniLog.log1(""+parse("BCKRG0000000001BDTRG0000000002", "BCKRG([0-9]{10})BDTRG([0-9]{10}).*"));
		UniLog.log1("" + parse("BCKRG0000000001BDTRG0000000001", "BCKRG[0-9]{3}.*"));
		UniLog.log1("" + parse("B", ""));
	}
}
