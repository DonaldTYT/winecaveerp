package com.uniinformation.utils;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;

public class MonthUtil {
	static public int getMonth(String p_month) {
		try {
			if(StringUtils.isBlank(p_month)) return(0);
			int y = Integer.parseInt(p_month.substring(0,4));
			int m = Integer.parseInt(p_month.substring(5,7));
			return(y * 12 + m-1);
		} catch (Exception ex) {
			return(-1);
		}
	}
	static public String getMonth(int p_n) {
		int year = p_n / 12;
		int month = p_n % 12;
		return(String.format("%04d-%02d", year,month+1));
	}
	static public String nextNmonth(String p_month,int p_n) {
		int n = getMonth(p_month);
		if(n < 0) return(null);
		return(getMonth(n+p_n));
	}
	static public Date dateBegin(String p_month) {
		if(StringUtils.isBlank(p_month)) return(null);
		int y = Integer.parseInt(p_month.substring(0,4));
		int m = Integer.parseInt(p_month.substring(5,7));
		return(DateUtil.getDate(y, m, 1));
	}
	static public Date dateBegin(int p_month) {
		if(p_month <= 0) return(null);
		return(dateBegin(getMonth(p_month)));
	}
}
