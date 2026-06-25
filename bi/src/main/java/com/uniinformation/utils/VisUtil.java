package com.uniinformation.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;

public class VisUtil {
	//final static String TIMEZONE = "UTC";
	final static String TIMEZONE = "GMT+8";
	
	//test case 1, json with tz, javascript no override, pc work fine, mobile unable to parse time zone
	//test case 2, json without tz, javascript override monent return utc, pc work fine, mobile work fine
	public static String getDateTimeStr(int year, int month, int day, int hour, int min, int sec){ 
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"); //iphone fail to parse timezone
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //without tz
		Calendar calendar = new GregorianCalendar(year,month-1,day,hour,min,sec);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));  //offset to localtime
		//calendar.setTimeZone(TimeZone.getTimeZone("UTC"));  //testing offset to UTC
		return(sdf.format(calendar.getTime()));
	}
	
	public static String getDayBegin(Date p_date)
	{
		int yy,mm,dd;
		yy = DateUtil.getYear(p_date);
		mm = DateUtil.getMonth(p_date);
		dd = DateUtil.getDay(p_date);
		return(getDateTimeStr(yy, mm, dd, 0, 0, 0));
	}
	public static String getDayEnd(Date p_date)
	{
		int yy,mm,dd;
		yy = DateUtil.getYear(p_date);
		mm = DateUtil.getMonth(p_date);
		dd = DateUtil.getDay(p_date);
		return(getDateTimeStr(yy, mm, dd, 23, 59, 59));
	}
	
	public static int getYearFromTimeString(String p_string)
	{
		try {
			String s = StringUtil.strpart(p_string, 0, 4);
			return(Integer.parseInt(s));
		} catch (Exception ex){
			UniLog.log(ex);
			return(0);
		}
	}
	public static int getMonthFromTimeString(String p_string)
	{
		try {
			String s = StringUtil.strpart(p_string, 5, 2);
			return(Integer.parseInt(s));
		} catch (Exception ex){
			UniLog.log(ex);
			return(0);
		}
	}
	public static int getDayFromTimeString(String p_string)
	{
		try {
			String s = StringUtil.strpart(p_string, 8, 2);
			return(Integer.parseInt(s));
		} catch (Exception ex){
			UniLog.log(ex);
			return(0);
		}
	}
	public static int getHourFromTimeString(String p_string)
	{
		try {
			String s = StringUtil.strpart(p_string, 11, 2);
			return(Integer.parseInt(s));
		} catch (Exception ex){
			UniLog.log(ex);
			return(0);
		}
	}
	public static int getMinuteFromTimeString(String p_string)
	{
		try {
			String s = StringUtil.strpart(p_string, 14, 2);
			return(Integer.parseInt(s));
		} catch (Exception ex){
			UniLog.log(ex);
			return(0);
		}
	}
}
