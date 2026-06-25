package com.kikyosoft.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.uniinformation.webcore.SessionHelper;

public abstract class OptionFinder {
	public class Condition {
		String option;
		String value1;
		String operator;
		String value2;
	}
	Map<String,String> options;
	Map<String,Condition> conditions;
	public OptionFinder() {
		options = new HashMap<String,String>();
		conditions = new HashMap<String,Condition>();
	}
	public void addOption(String p_option,String p_id) {
		options.put(p_option,p_id);
	}
	public Set<String> getOptions() {
		return(options.keySet());
	}
	public void addCondition(String option,String value) {
		addCondition(option,value , null, null);
	}
	public void addCondition(String option,String value, String operator) {
		addCondition(option, value , operator, null);
	}
	public void addCondition(String option,String value1 , String operator, String value2) {
		Condition cond = new Condition();
		cond.option = option;
		cond.operator = operator;
		cond.value1 = value1;
		cond.value2 = value2;
		conditions.put(option, cond);
	}
	public abstract boolean compareOption(Object p_record,Condition p_cond) throws Exception;
	public abstract JSONObject queryOptions(SessionHelper p_sp,String p_option);
}
