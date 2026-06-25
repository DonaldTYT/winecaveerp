package com.kyoko.common;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.threeten.bp.MonthDay;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.uniinformation.rpccall.Dateval;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.exprpar.FunctionInterface;
import com.uniinformation.utils.exprpar.Parser;
import com.uniinformation.utils.exprpar.VariableInterface;

public class DateUtil implements FunctionInterface, VariableInterface{
//    public static java.util.Date minDate = new java.util.Date(-1325462400000L); // 1st Jan 1928
//    public static java.util.Date minDate = new java.util.Date(-2208988800000L);  //  1st Jan  1900
    public static java.util.Date minDate = new java.util.Date(-2208000000000L);  //  ???
    public static java.util.Date zeroDate = new java.util.Date(-2209161600000L);  // 31th Dec 1899
    public static java.util.Date maxDate = DateUtil.getDate("2037/12/31");
    public static java.util.Date hkDaySavingDate = DateUtil.getDate("1979/10/22");
    public static java.util.Date minTime = new java.util.Date(86400000);  // 2nd Jan 1970(may depends on time zone
    public static java.util.Date zeroTime = new java.util.Date(0);  // 2nd Jan 1970(may depends on time zone
	static long gmtOffsetMinisec = new GregorianCalendar().get(Calendar.ZONE_OFFSET)+ new GregorianCalendar().get(Calendar.DST_OFFSET); // this shouble be wrong  : DT 2024-072
	/*
	//not thread safe, move it to local
	static SimpleDateFormat sdtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	static SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	*/
   public static long getGmtOffset() {
	   return(gmtOffsetMinisec);
	}
   public static java.util.Date today() {
	   return(dayBeginning(new java.util.Date()));
	}
   public static java.util.Date yearStart(java.util.Date p_time) {
	   return(getDate(toDateString(p_time, "yyyy")+"/01/01"));
	}
   public static java.util.Date yearEnd(java.util.Date p_time) {
		int yyyy = Integer.parseInt(toDateString(p_time, "yyyy")) + 1;
	   return(prevday(getDate(""+Integer.toString(yyyy)+"/01/01")));
	}
   public static java.util.Date quarterStart(java.util.Date p_time) {
	   String yystr = toDateString(p_time, "yyyy");
	   int mm = Integer.parseInt(toDateString(p_time, "mm"));
		String datestr = "";
		if (mm>=1 && mm<=3)
	      datestr = yystr + "/01/01";
		if (mm>=4 && mm<=6)
	      datestr = yystr + "/04/01";
		if (mm>=7 && mm<=9)
	      datestr = yystr + "/07/01";
		if (mm>=10 && mm<=12)
	      datestr = yystr + "/10/01";
      return(getDate(datestr));
	}
   public static java.util.Date quarterEnd(java.util.Date p_time) {
	   String yystr = toDateString(p_time, "yyyy");
		int mm = Integer.parseInt(toDateString(p_time, "mm"));
		String datestr = "";
		if (mm>=1 && mm<=3)
	      datestr = yystr + "/04/01";
		if (mm>=4 && mm<=6)
	      datestr = yystr + "/07/01";
		if (mm>=7 && mm<=9)
	      datestr = yystr + "/10/01";
		if (mm>=10 && mm<=12){
			int yyyy = Integer.parseInt(yystr) + 1;
	      datestr = Integer.toString(yyyy) + "/01/01";
      }
	   return(prevday(getDate(datestr)));
	}
   public static java.util.Date monthStart(java.util.Date p_time) {
	   return(getDate(toDateString(p_time, "yyyy/mm")+"/01"));
	}
   public static java.util.Date monthEnd(java.util.Date p_time) {
		int yyyy = Integer.parseInt(toDateString(p_time, "yyyy"));
		int mm = Integer.parseInt(toDateString(p_time, "mm"));
	   if (mm >= 12) {
		   mm = 1;
		   yyyy++;
	   }
		else
		   mm++;
	   return(prevday(getDate(new Sprintf("%04d/%02d")
		               .add(yyyy)
		               .add(mm)
		   			   .toString()
		   				+"/01"
						   )));
	}
   public static java.util.Date weekStart(java.util.Date p_time, boolean p_fStartOnSunday) {
      if (p_fStartOnSunday)
         return(weekStartOnSunday(p_time));
      else
         return(weekStartOnMonday(p_time));
	}
   public static java.util.Date weekStart(java.util.Date p_time) {
		java.util.Date refDate = dayBeginning(getDate("1900/01/01"));
	   java.util.Date today = dayBeginning(p_time);
		int weekday = (int) ((((today.getTime()-refDate.getTime())/86400000)/*-3*/) % 7);
      return(prevday(p_time, weekday));
	}
   public static java.util.Date weekStartOnMonday(java.util.Date p_time) {
      return(nextday(p_time, -(dayInWeek(p_time)-1 < 0 ? 6 : dayInWeek(p_time)-1)));
	}
   public static java.util.Date weekEnd(java.util.Date p_time) {
	   return(nextday(weekStart(p_time), 6));
	}
   public static java.util.Date weekStartOnSunday(java.util.Date p_time) {
      return(nextday(p_time, -dayInWeek(p_time)));
	}
   public static java.util.Date weekEndWithSaturday(java.util.Date p_time) {
	   return(nextday(weekStartOnSunday(p_time), 6));
	}
   public static java.util.Date prevday(java.util.Date p_time, int p_numberofday) {
	   if (p_time == null) return null;
	   java.util.Date today = dayBeginning(p_time);
	   return(new java.util.Date(today.getTime()-((long) 86400000)*p_numberofday));
	}
   public static java.util.Date prevday(java.util.Date p_time) {
      return(prevday(p_time, 1));
	}
   public static java.util.Date prevweek(java.util.Date p_time, int p_numberofweek) {
	   java.util.Date today = dayBeginning(p_time);
	   return(new java.util.Date(today.getTime()-(((long) 86400000)*7*p_numberofweek)));
	}
   public static java.util.Date prevweek(java.util.Date p_time) {
      return(prevweek(p_time, 1));
	}
   public static java.util.Date nextday(java.util.Date p_time, int p_numberofday) {
	   if (p_time == null) return null;
	   java.util.Date today = dayBeginning(p_time);
	   return(new java.util.Date(today.getTime()+((long) 86400000)*p_numberofday));
	}
   public static java.util.Date nextday(java.util.Date p_time) {
      return(nextday(p_time, 1));
	}
   public static java.util.Date nextweek(java.util.Date p_time, int p_numberofweek) {
	   java.util.Date today = dayBeginning(p_time);
	   return(new java.util.Date(today.getTime()+(((long) 86400000)*7*p_numberofweek)));
	}
   public static java.util.Date nextweek(java.util.Date p_time) {
      return(nextweek(p_time, 1));
	}
   public static java.util.Date prevyear(java.util.Date p_time) {
	   return(getDate(""+(NumberUtil.parseInt(toDateString(p_time, "yyyy"))-1)+"/01/01"));
	}
   public static java.util.Date nextyear(java.util.Date p_time) {
	   //return(getDate(""+(NumberUtil.parseInt(toDateString(p_time, "yyyy"))+1)+"/01/01"));
	   return(nextyear(p_time, 1));
	}
   public static java.util.Date now() {
	   return(new java.util.Date());
	}
	public static java.util.Date dayEnding(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		cal.set(cal.get(Calendar.YEAR), 
		        cal.get(Calendar.MONTH), 
		        cal.get(Calendar.DAY_OF_MONTH), 
				  23,59,59);
		//System.out.println(cal.toString());
		return(cal.getTime());
	}
	public static java.util.Date dayBeginning(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		cal.set(cal.get(Calendar.YEAR), 
		        cal.get(Calendar.MONTH), 
		        cal.get(Calendar.DAY_OF_MONTH), 
				  0,0,0);
		//System.out.println(cal.toString());
		return(cal.getTime());
	}
	public static java.util.Date getDate(int p_year, int p_month, int p_dayofmonth) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		if (p_year >= 100)
		   cal.set(p_year,
		        p_month-1,
		        p_dayofmonth,
				  0,0,0);
		else if (p_year >= 50)
		   cal.set(p_year+1900,
		        p_month-1,
		        p_dayofmonth,
				  0,0,0);
		else
		   cal.set(p_year+2000,
		        p_month-1,
		        p_dayofmonth,
				  0,0,0);
		//System.out.println(cal.toString());
		return(roundSecond(cal.getTime()));
	}
	public static java.util.Date getTime(
	                                int p_year, 
											  int p_month, 
											  int p_dayofmonth,
											  int p_hour,
											  int p_min,
											  int p_sec
											  ) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.clear();
		cal.set(p_year,
		        p_month-1,
		        p_dayofmonth,
				  p_hour,
				  p_min,
				  p_sec
				  );
		return(cal.getTime());
	}
	public static java.util.Date getDate(String p_datestr, String p_format) {
		try {
		   if (p_datestr == null)
			   return(null);
		   if (p_datestr.trim().equals(""))
			   return(null);
		   if (p_datestr.trim().startsWith("today()"))
			   return(getTodayWithOffset(p_datestr));
			if (p_format == null)
	         return(getDate(p_datestr));
	      StringTokenizer tok = new StringTokenizer(p_format, "-/");
	      char fc0 = tok.nextToken().charAt(0);
	      char fc1 = tok.nextToken().charAt(0);
	      char fc2 = tok.nextToken().charAt(0);
	      StringTokenizer datetok = new StringTokenizer(p_datestr, "-/");
	      String str0 = datetok.nextToken();
	      String str1 = datetok.nextToken();
	      String str2 = datetok.nextToken();
		   int y = 0;
		   int m = 0;
		   int d = 0;
		   switch (fc0) {
		      case 'y':
			      y = Integer.parseInt(str0);
				   break;
		      case 'm':
			      m = Integer.parseInt(str0);
				   break;
		      case 'd':
			      d = Integer.parseInt(str0);
				   break;
		   }
		   switch (fc1) {
		      case 'y':
			      y = Integer.parseInt(str1);
				   break;
		      case 'm':
			      m = Integer.parseInt(str1);
				   break;
		      case 'd':
			      d = Integer.parseInt(str1);
				   break;
		   }
		   switch (fc2) {
		      case 'y':
			      y = Integer.parseInt(str2);
				   break;
		      case 'm':
			      m = Integer.parseInt(str2);
				   break;
		      case 'd':
			      d = Integer.parseInt(str2);
				   break;
		   }
	      return(getDate(new Sprintf("%04d/%02d/%02d")
		                  .add(y)
		                  .add(m)
		                  .add(d)
							   .toString()
							   ));
      } catch (NoSuchElementException ex) {
//		   UniLog.log(ex);
			return(null);
		}
	}
	public static java.util.Date getDate(String p_ymd) {
		if (p_ymd.trim().startsWith("today()"))
			return(getTodayWithOffset(p_ymd));
	   if (p_ymd.length() == 6)
	      return(getDateY2MD(p_ymd));
	   else if (p_ymd.length() == 8) {
			if (p_ymd.indexOf('-') > 0 || p_ymd.indexOf('/') > 0)
	         return(getDateY2MD(p_ymd));
			else
	         return(getDateY4MD(p_ymd));
		}
	   else if (p_ymd.length() == 10) {
		   if (p_ymd.equals("4501/01/01")) { // VB null date
			   return(new java.util.Date(0));
			}
	      return(getDateY4MD(p_ymd));
		}
	   else
		   return(null);
	}
	public static java.util.Date getDateY4MD(String p_ymd) {
		try {
			if (p_ymd.trim().startsWith("today()")) {
				return(getTodayWithOffset(p_ymd));
			}
			if (p_ymd.length() == 8) {
				return(getDate(
						Integer.parseInt(p_ymd.substring(0, 4)),
						Integer.parseInt(p_ymd.substring(4, 6)),
						Integer.parseInt(p_ymd.substring(6, 8))
						));
			}
			else if (p_ymd.length() == 10) {
				return(getDate(
						Integer.parseInt(p_ymd.substring(0, 4)),
						Integer.parseInt(p_ymd.substring(5, 7)),
						Integer.parseInt(p_ymd.substring(8, 10))
						));
			}
			else {
				return(null);
			}
		}
		catch(Exception ex) {
			UniLog.log("error:" + ex.getMessage());
			return null;
		}
	}
	public static java.util.Date getDateDMY4(String p_dmy) {
		if (p_dmy.trim().startsWith("today()"))
			return(getTodayWithOffset(p_dmy));
	   if (p_dmy.length() == 8)
	      return(getDate(Integer.parseInt(p_dmy.substring(4, 8)),
	                     Integer.parseInt(p_dmy.substring(0, 2)),
	                     Integer.parseInt(p_dmy.substring(2, 4))
							   ));
		else if (p_dmy.length() == 10)
	      return(getDate(Integer.parseInt(p_dmy.substring(6, 10)),
	                     Integer.parseInt(p_dmy.substring(0, 2)),
	                     Integer.parseInt(p_dmy.substring(3, 5))
							   ));
	   else
		   return(null);
	}
	public static java.util.Date getDateMDY4(String p_mdy) {
		if (p_mdy.trim().startsWith("today()"))
			return(getTodayWithOffset(p_mdy));
	   if (p_mdy.length() == 8)
	      return(getDate(Integer.parseInt(p_mdy.substring(4, 8)),
	                     Integer.parseInt(p_mdy.substring(2, 4)),
	                     Integer.parseInt(p_mdy.substring(0, 2))
							   ));
		else if (p_mdy.length() == 10)
	      return(getDate(Integer.parseInt(p_mdy.substring(6, 10)),
	                     Integer.parseInt(p_mdy.substring(3, 5)),
	                     Integer.parseInt(p_mdy.substring(0, 2))
							   ));
	   else
		   return(null);
	}
	public static java.util.Date getDateY2MD(String p_ymd) {
		if (p_ymd.trim().startsWith("today()"))
			return(getTodayWithOffset(p_ymd));
	   if (p_ymd.length() == 6)
	      return(getDate(Integer.parseInt(p_ymd.substring(0, 2)),
	                  Integer.parseInt(p_ymd.substring(2, 4)),
	                  Integer.parseInt(p_ymd.substring(4, 6))
							));
		else if (p_ymd.length() == 8)
	      return(getDate(Integer.parseInt(p_ymd.substring(0, 2)),
	                  Integer.parseInt(p_ymd.substring(3, 5)),
	                  Integer.parseInt(p_ymd.substring(6, 8))
							));
	   else
		   return(null);
	}
	public static java.util.Date getDateDMY2(String p_dmy) {
		if (p_dmy.trim().startsWith("today()"))
			return(getTodayWithOffset(p_dmy));
	   if (p_dmy.length() == 6)
	      return(getDate(Integer.parseInt(p_dmy.substring(4, 6)),
	                  Integer.parseInt(p_dmy.substring(2, 4)),
	                  Integer.parseInt(p_dmy.substring(0, 2))
							));
		else if (p_dmy.length() == 8)
	      return(getDate(Integer.parseInt(p_dmy.substring(6, 8)),
	                  Integer.parseInt(p_dmy.substring(3, 5)),
	                  Integer.parseInt(p_dmy.substring(0, 2))
							));
	   else
		   return(null);
	}
	public static java.util.Date getDateMDY2(String p_mdy) {
		if (p_mdy.trim().startsWith("today()"))
			return(getTodayWithOffset(p_mdy));
	   if (p_mdy.length() == 6)
	      return(getDate(Integer.parseInt(p_mdy.substring(4, 6)),
	                  Integer.parseInt(p_mdy.substring(0, 2)),
	                  Integer.parseInt(p_mdy.substring(2, 4))
							));
		else if (p_mdy.length() == 8)
	      return(getDate(Integer.parseInt(p_mdy.substring(6, 8)),
	                  Integer.parseInt(p_mdy.substring(0, 2)),
	                  Integer.parseInt(p_mdy.substring(3, 5))
							));
	   else
		   return(null);
	}
	/*
	//andrew210618 remove duplicate method
	public static String toDateString(java.sql.Date p_time, String p_formatstr) {
	   return(toTimeStringGen(p_time, p_formatstr, false));
	}
	*/
	public static String toDateString(java.util.Date p_time, String p_formatstr) {
	   return(toTimeStringGen(p_time, p_formatstr, false));
	}
	
	public static String toDateStringY2MD(Date p_date) {
		return toDateStringY2MD(p_date, true);
	}
	public static String toDateStringY2MD(Date p_date, boolean p_withSeparator) {
		return toDateString(p_date, p_withSeparator ? "yy/mm/dd" : "yymmdd");
	}
	public static String toDateStringY4MD(Date p_date) {
		return toDateStringY4MD(p_date, true);
	}
	public static String toDateStringY4MD(Date p_date, boolean p_withSeparator) {
		return toDateString(p_date, p_withSeparator ? "yyyy/mm/dd" : "yyyymmdd");
	}
	
	private static String d2string(int p_number) {
	   if (p_number < 10) 
		   return("0"+p_number);
	   else
		   return(""+p_number);
	}
	private static void appendCalendar(StringBuffer p_sb, GregorianCalendar p_cal, char p_state, int p_length) {
	   switch (p_state) {
			case 'y':
			   if (p_length == 2)
		         p_sb.append(d2string(p_cal.get(Calendar.YEAR) % 1000));
				else
		         p_sb.append(p_cal.get(Calendar.YEAR));
			   break;
			case 'm':
			   if (p_length == 1)
		         p_sb.append(""+(p_cal.get(Calendar.MONTH)+1));
			   else if (p_length == 2)
		         p_sb.append(d2string(p_cal.get(Calendar.MONTH)+1));
			   else if (p_length == 3) {
				   switch (p_cal.get(Calendar.MONTH)+1) {
					   case 1: p_sb.append("Jan"); break;
					   case 2: p_sb.append("Feb"); break;
					   case 3: p_sb.append("Mar"); break;
					   case 4: p_sb.append("Apr"); break;
					   case 5: p_sb.append("May"); break;
					   case 6: p_sb.append("Jun"); break;
					   case 7: p_sb.append("Jul"); break;
					   case 8: p_sb.append("Aug"); break;
					   case 9: p_sb.append("Sep"); break;
					   case 10: p_sb.append("Oct"); break;
					   case 11: p_sb.append("Nov"); break;
					   case 12: p_sb.append("Dec"); break;
					}
				}
			   else {
				   switch (p_cal.get(Calendar.MONTH)+1) {
					   case 1: p_sb.append("January"); break;
					   case 2: p_sb.append("February"); break;
					   case 3: p_sb.append("March"); break;
					   case 4: p_sb.append("April"); break;
					   case 5: p_sb.append("May"); break;
					   case 6: p_sb.append("June"); break;
					   case 7: p_sb.append("July"); break;
					   case 8: p_sb.append("August"); break;
					   case 9: p_sb.append("September"); break;
					   case 10: p_sb.append("October"); break;
					   case 11: p_sb.append("November"); break;
					   case 12: p_sb.append("December"); break;
					}
				}
			   break;
			case 'd':
			   if (p_length == 1)
		         p_sb.append(""+(p_cal.get(Calendar.DAY_OF_MONTH)));
			   else if (p_length == 2)
		         p_sb.append(d2string(p_cal.get(Calendar.DAY_OF_MONTH)));
			   break;
			case 'H':
			   if (p_length == 2)
		         p_sb.append(d2string(p_cal.get(Calendar.HOUR_OF_DAY)));
			   break;
			case 'M':
			   if (p_length == 2)
		         p_sb.append(d2string(p_cal.get(Calendar.MINUTE)));
			   break;
			case 'S': 
			   if (p_length == 2)
		         p_sb.append(d2string(p_cal.get(Calendar.SECOND)));
			   break;
			case 'w': 
			   if (p_length == 3) {
					switch (p_cal.get(Calendar.DAY_OF_WEEK)) {
					   case Calendar.SUNDAY: p_sb.append("Sun"); break;
					   case Calendar.MONDAY: p_sb.append("Mon"); break;
					   case Calendar.TUESDAY: p_sb.append("Tue"); break;
					   case Calendar.WEDNESDAY: p_sb.append("Wed"); break;
					   case Calendar.THURSDAY: p_sb.append("Thu"); break;
					   case Calendar.FRIDAY: p_sb.append("Fri"); break;
					   case Calendar.SATURDAY: p_sb.append("Sat"); break;
					}
				}
				else {
					switch (p_cal.get(Calendar.DAY_OF_WEEK)) {
					   case Calendar.SUNDAY: p_sb.append("Sunday"); break;
					   case Calendar.MONDAY: p_sb.append("Monday"); break;
					   case Calendar.TUESDAY: p_sb.append("Tuesday"); break;
					   case Calendar.WEDNESDAY: p_sb.append("Wednesday"); break;
					   case Calendar.THURSDAY: p_sb.append("Thursday"); break;
					   case Calendar.FRIDAY: p_sb.append("Friday"); break;
					   case Calendar.SATURDAY: p_sb.append("Saturday"); break;
					}
				}
			   break;
		}
	}
	public static int toDayOfWeek(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(p_time);
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY: return(0);
			case Calendar.MONDAY: return(1);
			case Calendar.TUESDAY: return(2);
			case Calendar.WEDNESDAY: return(3);
			case Calendar.THURSDAY: return(4);
			case Calendar.FRIDAY: return(5);
			case Calendar.SATURDAY: return(6);
		}
		return(-1);
	}
	public static String toTimeString(java.util.Date p_time, String p_dateformatstr) {
	   return(toTimeStringGen(p_time, p_dateformatstr, true));
	}
	public static String toTimeStringGen(java.util.Date p_time, String p_dateformatstr, boolean p_fTime) {
		if (p_time == null)
		   return("");
		if (p_fTime && p_time.getTime() <= ((long) 86400000))
		   return("");
		//else if (p_time.getTime() <= -2209017599000L) 
		else if (p_time.getTime() <= (-2209017599000L + 24 * 60 * 60 * 1000)) 
			return("");
		StringBuffer sb = new StringBuffer();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(p_time);

      char state = ' ';
		int cnt = p_dateformatstr.length();
		int i=0;
		char c;
		int l = 0;
      for (;;) {
			if (i >= cnt) {
		      switch (state) {
			      case ' ': 
					   break;
					default:
	               appendCalendar(sb, cal, state, l);
				      break;
				}
			   break;
			}
			c = p_dateformatstr.charAt(i);
		   switch (state) {
			   case ' ': 
				   switch (c) {
				      case 'y':
				      case 'm':
				      case 'd':
				      case 'H':
				      case 'M':
				      case 'S': 
				      case 'w':
							state = c; 
							l = 1; 
							break;
						default: 
							sb.append(c);
							break;
				   }
			      break;
				default:
				   switch (c) {
				      case 'y':
				      case 'm':
				      case 'd':
				      case 'H':
				      case 'M':
				      case 'S': 
				      case 'w':
					      if (state == c)
					         l++;
						   else {
	                     appendCalendar(sb, cal, state, l);
							   state = c; 
							   l = 1; 
							}
							break;
						default: 
	                  appendCalendar(sb, cal, state, l);
							sb.append(c);
					      state = ' ';
							break;
				   }
			      break;
			}
			i++;
		}

	   return(sb.toString());
	}
	public static String secToHourMin(long p_sec) {
	   return(d2string((int) p_sec/60/60)+":"+d2string((int) (p_sec/60)%60));
	}
	public static String secToMinSec(long p_sec) {
	   return(d2string((int) p_sec/60)+":"+d2string((int) p_sec%60));
	}
	public static String secToText(long p_sec) {
		int day = (int) p_sec / 86400;
		int hour = (int) (p_sec % 86400) / 3600;
		int min = (int) (p_sec % 3600)  / 60;
		int sec = (int) p_sec % 60;
		if (day > 0) {
			return String.format("%dd%02dh",day,hour);
		}
		if (hour > 0) {
			return String.format("%dh%02dm",hour,min);
		}
		if (min > 0) {
			return String.format("%dm%02ds",min,sec);
		}
		return String.format("%ds",sec);
	}
	public static java.sql.Date toSqlDate(java.util.Date p_date) {
	   if(p_date == null)
	   return(new java.sql.Date(zeroDate.getTime()));
	   else
	   return(new java.sql.Date(p_date.getTime()));
	}
	public static java.sql.Timestamp toSqlTimestamp(java.util.Date p_date) {
	   return(new java.sql.Timestamp(p_date.getTime()));
	}
	public static java.util.Date toDate(java.sql.Date p_date) {
	   return(new java.util.Date(p_date.getTime()));
	}
	public static boolean isDateNull(java.util.Date p_time) {
		if (p_time == null)
		   return(true);
		//if (p_time.getTime() <= ((long) 86400000))
		if (p_time.getTime() <= -2209017599000L)
		   return(true);
		if (p_time.getTime() > 2524492800620L)
		   return(true);
	   return(false);
	}
	public static boolean isEqualSecond(java.util.Date p_time0, java.util.Date p_time1) {
	   if (p_time0 == null && p_time1 == null)
		   return(true);
	   if (p_time0 == null && p_time1 != null)
		   return(false);
	   if (p_time0 != null && p_time1 == null)
		   return(false);
	   long cc = p_time0.getTime() - p_time1.getTime();
		return((cc <= 1000) && (cc >= -1000));
	}
	public static java.util.Date roundSecond(java.util.Date p_time) {
	   return(new java.util.Date(p_time.getTime() - (p_time.getTime() % 1000)));
	}
	public static int getJulianDate(java.util.Date p_time) {
	   return((int) (p_time.getTime()/86400000));
	}
	/***
	 * 0 - sunday, 1 - monday...
	 * @param p_date
	 * @return
	 */
   public static int dayInWeek(java.util.Date p_date) {
		java.util.Date refDate = dayBeginning(getDate("1900/01/01"));
	   java.util.Date today = dayBeginning(p_date);
	   return((int) ((((today.getTime()-refDate.getTime())/86400000)/*-3*/+1) % 7));
	}
   public static String dayInWeekStr(java.util.Date p_date) {
	   switch(dayInWeek(p_date)) {
	   case 0: return "Sun";
	   case 1: return "Mon";
	   case 2: return "Tue";
	   case 3: return "Wed";
	   case 4: return "Thu";
	   case 5: return "Fri";
	   case 6: return "Sat";
	   default: return "";
	   }
	}
   public static int getWeekNumberYear(java.util.Date p_date, boolean p_fStartOnSunday) {
      int wn = weekNumber(p_date, p_fStartOnSunday);
		int year = NumberUtil.parseInt(toDateString(p_date, "yyyy"));
      java.util.Date ws = getWeekStartFromWeekNumber(year, wn, p_fStartOnSunday);
	   if (p_date.getTime() >= ws.getTime() && p_date.getTime() <= (ws.getTime()+86400000*7))
		   return(year);
      ws = getWeekStartFromWeekNumber(year-1, wn, p_fStartOnSunday);
	   if (p_date.getTime() >= ws.getTime() && p_date.getTime() <= (ws.getTime()+86400000*7))
		   return(year-1);
		return(year+1);
	}
   public static int weekNumber(java.util.Date p_date) {
	   return(weekNumber(p_date, true));
	}
   public static int weekNumber(java.util.Date p_date, boolean p_fStartOnSunday) {
		java.util.Date curdate = p_date;
      java.util.Date ws = null;
		if (p_fStartOnSunday) {
			/*
			// hardcode testing
			if (true) {
				java.util.Date cws = weekStartOnSunday(curdate);
				java.util.Date yws = weekStartOnSunday(yearStart(curdate));
				return((int) ((weekStartOnSunday(curdate).getTime() - weekStartOnSunday(yearStart(curdate)).getTime()) / 86400000 / 7 + 1));
			}
			*/
         if (!toDateString(curdate, "yyyy").equals(toDateString(nextday(weekStartOnSunday(curdate), 6), "yyyy")))
		      curdate = nextday(yearEnd(curdate), 1);
         java.util.Date ys = yearStart(curdate);
			switch (dayInWeek(ys)) {
			   case 0:
			   case 1:
			   case 2:
			   case 3:
				   ws = weekStartOnSunday(ys);
				   break;
			   case 4:
			   case 5:
			   case 6:
               java.util.Date pys = yearStart(nextday(ys, -1));
			      switch (dayInWeek(pys)) {
			         case 0:
			         case 1:
			         case 2:
			         case 3:
				         ws = weekStartOnSunday(pys);
						   break;
			         case 4:
			         case 5:
			         case 6:
				         ws = nextday(weekStartOnSunday(pys), 7);
						   break;
					}
				   break;
			}
			return((int) ((weekStartOnSunday(curdate).getTime()-ws.getTime())/86400000/7+1));
		}
		else {
         if (!toDateString(curdate, "yyyy").equals(toDateString(nextday(weekStartOnMonday(curdate), 6), "yyyy")))
		      curdate = nextday(yearEnd(curdate), 1);
         java.util.Date ys = yearStart(curdate);
			switch (dayInWeek(ys)) {
			   case 1:
			   case 2:
			   case 3:
			   case 4:
				   ws = weekStartOnMonday(ys);
				   break;
			   case 5:
			   case 6:
			   case 0:
               java.util.Date pys = yearStart(nextday(ys, -1));
			      switch (dayInWeek(pys)) {
			         case 1:
			         case 2:
			         case 3:
			         case 4:
				         ws = weekStartOnMonday(pys);
						   break;
			         case 5:
			         case 6:
			         case 0:
				         ws = nextday(weekStartOnMonday(pys), 7);
						   break;
					}
				   break;
			}
			return((int) ((weekStartOnMonday(curdate).getTime()-ws.getTime())/86400000/7+1));
		}
	}
	public static java.util.Date getTodayWithOffset(String p_todayString) {
	   if (p_todayString.trim().equals("today()"))
		   return(today());
	   else if (p_todayString.trim().startsWith("today()")) {
		   int offset = NumberUtil.parseInt(StringUtil.strpart(p_todayString.trim(), 7, -1));
         return(nextday(today(), offset));
		}
		else
		   return(null);
	}
	public static int getDay(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		return(cal.get(Calendar.DAY_OF_MONTH));
	}
	public static int getMonth(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		return(cal.get(Calendar.MONTH)+1);
	}
	public static int getYear(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		return(cal.get(Calendar.YEAR));
	}
	public static int getHour(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		return(cal.get(Calendar.HOUR_OF_DAY));
	}
	public static int getMinute(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		return(cal.get(Calendar.MINUTE));
	}
	public static int getSecond(java.util.Date p_time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new java.util.Date(p_time.getTime() - p_time.getTime() % 1000));
		return(cal.get(Calendar.SECOND));
	}
   public static java.util.Date nextDaySkipHolidayStupid(java.util.Date p_time, int p_numberofday) {
	   java.util.Date nextday = p_time;
		int num = 0;
		for (;;) {
			if (num >= p_numberofday)
				break;
			nextday = nextday(nextday);
			if (toDayOfWeek(nextday) == 0
				 || toDayOfWeek(nextday) == 6) {
				continue;
			}
			num++;
		}
		return(nextday);
	}
   public static java.util.Date nextDaySkipHoliday(java.util.Date p_time, int p_numberofday) {
		return(nextDaySkipHolidayStupid(p_time, p_numberofday));
		/*
	   java.util.Date today = dayBeginning(p_time);
		int curDay = toDayOfWeek(today);
	   java.util.Date startDate;
		if (curDay == 6)
			startDate = nextday(today, 1);
		else
			startDate = today;
		int weekNum	=	(int) p_numberofday/5;
		int newNumOfDay = p_numberofday + weekNum * 2;
		return(new java.util.Date(startDate.getTime()+((long) 86400000)*newNumOfDay));
		*/
	}
   public static java.util.Date getWeekStartFromWeekNumber(int p_yyyy, int p_weekNumber, boolean p_fStartOnSunday) {
		//use yyyy and weekno to getstartdate
      java.util.Date yearStart = DateUtil.getDate(p_yyyy+"/01/01");
		int i;
      if (weekNumber(yearStart, p_fStartOnSunday) == 1)
		   i = 1;
		else
		   i = 0;
      java.util.Date wkst0 = weekStart(yearStart, p_fStartOnSunday);
      //return(nextday(wkst0, (p_weekNumber-i)*7 - (p_fStartOnSunday?1:0)));
      return(nextday(wkst0, (p_weekNumber-i)*7));
	}
   public static java.util.Date prevMonthStart(java.util.Date p_time) {
		java.util.Date curMonthStart = monthStart(p_time);
		return(monthStart(prevday(curMonthStart)));
	}
   public static java.util.Date prevMonthEnd(java.util.Date p_time) {
		java.util.Date curMonthStart = monthStart(p_time);
		return(prevday(curMonthStart));
	}
	public static java.util.Date getTime(
											  java.util.Date p_date,
											  int p_hour,
											  int p_min,
											  int p_sec
											  ) {
	   return(new java.util.Date(
		          dayBeginning(p_date).getTime()+((long) (p_hour*3600+p_min*60+p_sec)) * 1000
		));
	}
   public static java.util.Date nextMonthStart(java.util.Date p_time) {
		return(nextday(monthEnd(p_time)));
	}
   public static java.util.Date nextMonthEnd(java.util.Date p_time) {
		return(monthEnd(nextMonthStart(p_time)));
	}
   public static java.util.Date nextmonth(java.util.Date p_time, int p_numberofmonth) {
	   if (p_time == null) return null;
	   int dd = Integer.parseInt(toDateString(p_time, "dd"));
		java.util.Date nextMonthStart	= nextMonthStart(p_time);
		for (int i=0; i<p_numberofmonth-1; i++) {
			nextMonthStart	= nextMonthStart(nextMonthStart);
		}
		java.util.Date monthend = monthEnd(nextMonthStart);
		java.util.Date nextday = nextday(nextMonthStart, (dd-1));
		if (nextday.compareTo(monthend) > 0)
			return(monthend);
		return(nextday);
	}
   public static java.util.Date prevmonth(java.util.Date p_time, int p_numberofmonth) {
	   if (p_time == null) return null;
	   int dd = Integer.parseInt(toDateString(p_time, "dd"));
		java.util.Date prevMonthStart	= prevMonthStart(p_time);
		for (int i=0; i<p_numberofmonth-1; i++) {
			prevMonthStart	= prevMonthStart(prevMonthStart);
		}
		java.util.Date monthend = monthEnd(prevMonthStart);
		java.util.Date nextday = nextday(prevMonthStart, (dd-1));
		if (nextday.compareTo(monthend) > 0)
			return(monthend);
		return(nextday);
   }
   public static java.util.Date nextmonth(java.util.Date p_time) {
		return(nextmonth(p_time, 1));
	}
   public static java.util.Date nextyear(java.util.Date p_time, int p_numberofyear) {
//	   return(getDate(""+(NumberUtil.parseInt(toDateString(p_time, "yyyy"))+p_numberofyear)+"/01/01"));
	   int d = getDay(p_time);
	   int m = getMonth(p_time);
	   int y = getYear(p_time);
	   y++;
	   return(getDate(String.format("%04d/%02d/%02d", y,m,d)));
	}
   public static Vector getDateRange(String p_range) {
		java.util.Date stDate = null;
		java.util.Date endDate = null;
		if (p_range.equals("currentYear")) {
			stDate = yearStart(today());
			endDate = yearEnd(today());
		}
      else if (p_range.equals("currentQuarter")) {
			stDate = quarterStart(today());
         endDate = quarterEnd(today());
		}
      else if (p_range.equals("prevMonth")) {
			stDate = prevMonthStart(today());
         endDate = prevMonthEnd(today());
		}
      else if (p_range.equals("currentMonth")) {
			stDate = monthStart(today());
         endDate = monthEnd(today());
		}
      else if (p_range.equals("currentWeek")) {
			stDate = weekStartOnSunday(today());
			//weekStart(today());
			endDate = weekEndWithSaturday(today());
			//weekEnd(today());
		}
      else if (p_range.equals("today")) {
			stDate = today();
			endDate = today();
		}
		else 	
			return(null);
		Vector v = new Vector();
		v.addElement(stDate);
		v.addElement(endDate);
		return(v);
	}
	public static java.util.Date getTime(String p_timeStr) {
	   String dateStr = StringUtil.strpart(p_timeStr, 0, 10);
		String hh = StringUtil.strpart(p_timeStr, 11, 2);
		String mm = StringUtil.strpart(p_timeStr, 14, 2);
		String ss = StringUtil.strpart(p_timeStr, 17, 2);
		return(getTime(getDate(dateStr) , NumberUtil.parseInt(hh) , NumberUtil.parseInt(mm) , NumberUtil.parseInt(ss)));
	}
	public static int dateToSqlDateInt(java.util.Date d) {
		long d0 = -2209017600000L ; // = new java.util.Date(00,0,1).getTime();
		long d1 = d.getTime();
		long d2 = (d1-d0) / 86400000 + 1; // for sql date, +2 for c++ TTimeDate;
		return((int) d2);
	}
	public static void main(String[] args) {
		/*
	   System.out.println(secToHourMin(Integer.parseInt(args[0])));
	   System.out.println(today().toString());
	   System.out.println(nextday(now()).toString());
	   System.out.println(prevday(now()).toString());
	   System.out.println(nextweek(now()).toString());
	   System.out.println(prevweek(now()).toString());
	   System.out.println(getDate(2001, 10, 30).toString());
	   System.out.println(getDate("2001/10/31").toString());
	   System.out.println(toTimeString(now(), "yy/mm/dd HH:MM:SS mmmmmm www wwwwww"));
	   System.out.println(toTimeString(now(), args[0]));
	   System.out.println(""+new GregorianCalendar().get(Calendar.ZONE_OFFSET));
	   System.out.println(""+new GregorianCalendar().get(Calendar.DST_OFFSET));
//UniLog.log("getDate() return "+getDate(args[0], args[1]));
	   UniLog.log(""+getDate(toDateString(today(), "yyyy")+"/01/01"));
	   UniLog.log("trace:today="+today());
	   UniLog.log("trace:yearStart="+yearStart(today())); UniLog.log("trace:yearEnd="+yearEnd(today()));
	   UniLog.log("trace:quarterStart="+quarterStart(today()));
	   UniLog.log("trace:quarterEnd="+quarterEnd(today()));
	   UniLog.log("trace:monthStart="+monthStart(today()));
	   UniLog.log("trace:monthEnd="+monthEnd(today()));
	   UniLog.log("trace:weekStart="+weekStart(today()));
	   UniLog.log("trace:weekEnd="+weekEnd(today()));
		UniLog.log("time="+today().getTime());
		UniLog.log("time="+toDateString(new java.util.Date(now().getTime()+((long) 360)*86400*1000), "www, dd mmm yyyy HH:MM:SS ")+"GMT");
		UniLog.log("time="+now());
		//
		java.util.Date date = getDate("1970/01/01");
	   UniLog.log("trace:999000 date="+toDateString(date, "yyy/mm/dd"));
		java.util.Date date1 = getDate("1900/01/01");
	   UniLog.log("trace:999000 date1="+toDateString(date1, "yyy/mm/dd"));
		java.util.Date date2 = getDate("1900/01/02");
	   UniLog.log("trace:999000 date2="+toDateString(date2, "yyy/mm/dd"));
		java.util.Date date3 = getDate("1900/01/02");
	   UniLog.log("trace:999000 date3="+toDateString(date3, "yyy/mm/dd"));
		java.util.Date date1 = getDate("1970/01/01");
	   UniLog.log("trace:999000 getTime()="+date1.getTime());
	   UniLog.log("trace:999000 date1="+toDateString(date1, "yyy/mm/dd"));
		UniLog.log("trace: date="+(new java.util.Date(2524492800620L)));
	   UniLog.log(getDate(args[0]).toString());
	   UniLog.log("dayInWeek()="+dayInWeek(getDate(args[0])));
	   UniLog.log("weekStartOnMondy()="+weekStartOnMonday(getDate(args[0])));
	   UniLog.log("weekNumber()="+weekNumber(getDate(args[0])));
	   UniLog.log("nextDaySkipHolidayStupid()="+nextDaySkipHolidayStupid(getDate(args[0]), NumberUtil.parseInt(args[1])));
	   UniLog.log("nextDaySkipHoliday()="+nextDaySkipHoliday(getDate(args[0]), NumberUtil.parseInt(args[1])));
	   UniLog.log("getMonth()="+getMonth(today()));
	   UniLog.log("prevMonthStart()="+prevMonthStart(today()));
	   UniLog.log("prevMonthEnd()="+prevMonthEnd(today()));
	   UniLog.log("nextMonthStart()="+toDateString(nextMonthStart(today()), "yyyy-mm-dd"));
	   UniLog.log("nextMonthEnd()="+toDateString(nextMonthEnd(today()), "yyyy-mm-dd"));
	   UniLog.log("nextweek()="+toDateString(nextweek(today()), "yyyy-mm-dd"));
		UniLog.log("day="+toDateString(today(), "dd"));
		UniLog.log("nextmonth()="+toDateString(nextmonth(getDate(args[0]), Integer.parseInt(args[1])), "yyyy-mm-dd"));
		UniLog.log("nextyear()="+toDateString(nextyear(getDate(args[0]), Integer.parseInt(args[1])), "mm-dd"));
		//UniLog.log("compareTo()="+getDate(args[1]).compareTo(getDate(args[0])));
		*/

		//UniLog.log("time="+toDateString(new java.util.Date(now().getTime()+((long) 360)*86400*1000), "HH:MM:SS"));
		//UniLog.log("st="+toDateString((java.util.Date) getDateRange("currentWeek").elementAt(0), "yyyy-mm-dd"));
		//UniLog.log("st="+toDateString((java.util.Date) getDateRange("currentWeek").elementAt(1), "yyyy-mm-dd"));

		//UniLog.log("time0="+toDateString(weekStartOnSunday(today()), "yyyy-mm-dd"));
		//UniLog.log("time1="+toDateString(weekEndWithSaturday(today()), "yyyy-mm-dd"));
		//UniLog.log("today = " + new java.util.Date());
		//UniLog.log("dayBeginning = " + DateUtil.dayBeginning(new java.util.Date()));
	   //System.out.println("getDate()="+ getDate(args[0], "yyyy-mm-dd"));
	   // UniLog.log("trace:weekStart="+weekStart(getDate(args[0], "yyyy-mm-dd")));
	   // UniLog.log("trace:weekEnd="+weekEnd(getDate(args[0], "yyyy-mm-dd")));
		/*
	   UniLog.log("trace:time="+getTime(
		                             NumberUtil.parseInt(args[0]),
		                             NumberUtil.parseInt(args[1]),
		                             NumberUtil.parseInt(args[2]),
		                             NumberUtil.parseInt(args[3]),
		                             NumberUtil.parseInt(args[4]),
		                             NumberUtil.parseInt(args[5])
											  ).getTime()/1000);
		UniLog.log("" + DateUtil.weekNumber(new Date((new java.util.Date().getTime()) + 86400000)));
		UniLog.log("" + DateUtil.weekNumber(new Date((new java.util.Date().getTime()))));
   	UniLog.log("" + weekStartOnSunday(getDate(args[0])));
   	UniLog.log("" + weekNumber(getDate(args[0]), true));
		*/
      //UniLog.log("" + getWeekStartFromWeekNumber(NumberUtil.parseInt(args[0]), NumberUtil.parseInt(args[1]), true));
    	//UniLog.log("week number=" + weekNumber(getDate(args[0]), true));
//		UniLog.log("week year=" + getWeekNumberYear(getDate(args[0]), true));
		UniLog.log(zeroDate.toString());
		UniLog.log(minDate.toString());
		UniLog.log1("" + secToText(5));
		UniLog.log1("" + secToText(59));
		UniLog.log1("" + secToText(60));
		UniLog.log1("" + secToText(65));
		UniLog.log1("" + secToText(86400));
		UniLog.log1("" + secToText(86401));
		UniLog.log1("" + secToText(3605000));
		
		UniLog.log1(""+timeToMin("1:00"));
		UniLog.log1(""+timeToMin("01:00"));
		UniLog.log1(""+timeToMin("00:01"));
		UniLog.log1(""+timeToMin("00:02"));
		UniLog.log1(""+timeToMin("10:999"));
		UniLog.log1("" + getDateY4MD("2023/08/29"));
		UniLog.log1("" + getDateY4MD("2023/08/28"));
		UniLog.log1("" + getDateY4MD("2023/08/a3"));
		UniLog.log1("" + getDateY4MD("20200101"));
		UniLog.log1("" + getDateY4MD("20201231"));
		UniLog.log1("" + isValid(getDateY4MD("2050/12/31")));
	}
	public static boolean isValid(Date p_date){
		//return(p_date.compareTo(minDate) > 0);
		if (p_date == null) {
			return false;
		}
		return(p_date.after(minDate));
	}
	public static boolean isValidTime(Date p_date){
		//return(p_date.compareTo(minDate) > 0);
		if (p_date == null) {
			return false;
		}
		return(p_date.after(minTime));
	}
	public static String getMinDateY4MD() {
        return(toDateStringY4MD(minDate));
	}
	
	public static java.util.Date informixToDate(int p_idate) {
		long l = p_idate - 25568;
		l *= 86400000;
		l -= DateUtil.getGmtOffset();
		return(new Date(l));
	}
	
	public static int dateToInformix(java.util.Date p_date) {
		long ll = p_date.getTime() + DateUtil.getGmtOffset() + 2209075200000L;
		if(hkDaySavingDate.after(p_date)) {
			ll += 3600000L;
		}
		int l = (int) (ll / 86400000);
		return(l);
	}

	public static int dateToUnixtime(java.util.Date p_date) {
		long l = p_date.getTime();
		if (l < -86400000) return((int) 0);
		l /= 1000;
		return((int) l);
	}

	public static java.util.Date unixtimeToDate(int p_time) {
		if(p_time < -100000000) return(DateUtil.zeroDate);
		long l =  p_time;
		l *= 1000;
		return( new Date(l));
	}
	
	public static String dateToDateTimeStr(java.util.Date p_date) {
		return(dateToDateTimeStr(p_date,"yyyy/MM/dd HH:mm:ss"));
		
	}
	public static String dateToDateTimeStr(java.util.Date p_date,String p_format) {
//		if (p_date == null) return ("");
		if (p_date == null || !p_date.after(minTime)) return ("");
		SimpleDateFormat sdtf = new SimpleDateFormat(p_format);
		return(sdtf.format(p_date));
	}
	public static java.util.Date dateTimeStrToDate(String p_timestr) {
		return dateTimeStrToDate(p_timestr, true);
	}
	public static java.util.Date dateTimeStrToDate(String p_timestr, boolean p_logException) {
		SimpleDateFormat sdtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat stf1 = new SimpleDateFormat("HH:mm");
		try {
			if(p_timestr.startsWith("@")) {
				DateUtil du = new DateUtil();
				Parser p = new Parser(0,p_timestr.substring(1),du,du);
				double d = (Double) p.evaluate();
				long t = DateUtil.informixToDate((int) d).getTime();
				d -= Math.floor(d);
				if(d > 0.0) t += (int) Math.floor(d * 86400000.0);
				return(new java.util.Date(t));
//				return(DateUtil.informixToDate((int) d)) ;
			}
			if(p_timestr.length() == 5) {
				return(stf1.parse(p_timestr));
			} else if(p_timestr.length() == 8) {
				return(stf.parse(p_timestr));
			} else if(p_timestr.length() == 10) {
				return(sdf.parse(p_timestr));
			} else if(p_timestr.trim().isEmpty()) {
				return(zeroDate);
			} else 
				return(sdtf.parse(p_timestr));
		} catch (Exception pex) {
			if (p_logException){
				//UniLog.log(pex);
				UniLog.log1("error:" + pex.getMessage());
			}
			return(null);
		}
	}
	public static String dateToTimeStr(java.util.Date p_date, boolean p_longFmt) {
		if (p_date == null || p_date.before(minDate)) return ("");
		if (p_longFmt) {
			SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
			return(stf.format(p_date));
		}
		else {
			SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
			return(stf.format(p_date));
		}
	}
	public static String dateToTimeStr(java.util.Date p_date) {
		return dateToTimeStr(p_date, true);
	}
	public static String dateDigtalToTimeStr(java.util.Date p_date, boolean p_longFmt) {
		if (p_date == null || p_date.before(minDate)) return ("");
		long l = (p_date.getTime() + getGmtOffset()) / 1000;
		//UniLog.log1("dateDigtalToTimeStr %s,%d,%d", p_date, getGmtOffset(), l);
		if (l < 0)
			return dateToTimeStr(p_date, p_longFmt);
		return p_longFmt ? String.format("%02d:%02d:%02d", l / 3600, l % 3600 / 60, l % 60) : String.format("%02d:%02d", l / 3600, l % 3600 / 60);
	}
	public static String dateDigtalToTimeStr(java.util.Date p_date) {
		return dateDigtalToTimeStr(p_date, true);
	}
	/***
	 * get day end in unixtime format
	 * @param p_time
	 * @return -1 error
	 */
	public static long dateEnd(long p_time){
		try{
			SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd 23:59:59.999");
			String inDateStr = sdfIn.format(p_time);
			SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			return(sdfOut.parse(inDateStr).getTime());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return (-1);
	}
	/***
	 * get dat begin in unixtime format
	 * @param p_time
	 * @return -1 error
	 */
	public static long dateStart(long p_time){
		try{
			SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd 00:00:00.000");
			SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			String inDateStr = sdfIn.format(p_time);
			return(sdfOut.parse(inDateStr).getTime());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return (-1);
	}
	
	public DateUtil() {
		
	}
	
	@Override
	public Object evalFunction(String p_functName, Vector p_args) throws Exception {
		double d = 0;
		if(p_functName.equals("today")) {
			d = dateToInformix(DateUtil.today());
		} else
		if(p_functName.equals("weekStart")) {
			d = dateToInformix(DateUtil.weekStart(DateUtil.today()));
		} else
		if(p_functName.equals("weekEnd")) {
			d = dateToInformix(DateUtil.weekEnd(DateUtil.today()));
		} else
		if(p_functName.equals("monthStart")) {
			d = dateToInformix(DateUtil.monthStart(DateUtil.today()));
		} else
		if(p_functName.equals("monthEnd")) {
			d = dateToInformix(DateUtil.monthEnd(DateUtil.today()));
		} else
		if(p_functName.equals("yearStart")) {
			d = dateToInformix(DateUtil.yearStart(DateUtil.today()));
		} else
		if(p_functName.equals("yearEnd")) {
			d = dateToInformix(DateUtil.yearEnd(DateUtil.today()));
		} else
		if(p_functName.equals("now")) {
			java.util.Date jdd = new java.util.Date();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(new java.util.Date(jdd.getTime() - jdd.getTime() % 1000));
			int sec = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
			//System.out.println(cal.toString());
			cal.set(cal.get(Calendar.YEAR), 
		        cal.get(Calendar.MONTH), 
		        cal.get(Calendar.DAY_OF_MONTH), 
				  0,0,0);
			d = (double) dateToInformix(cal.getTime());
			d += (double) (sec/86400.0);
		} 
		return(d);
	}
	@Override
	public Object evalVariable(String p_varname) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object evalVariable(String p_varname, int p_idx) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/***
	 * convert hh:mm/hh:mm:ss to min
	 * @param p_timeStr
	 * @return
	 */
	public static int timeToMin(String p_timeStr) {
		return (timeToSec(p_timeStr) / 60);
	}
	
	/***
	 * convert hh:mm/hh:mm:ss to sec
	 * @param p_timeStr
	 * @return
	 */
	public static int timeToSec(String p_timeStr) {
		try {
			//String[] timeArr = StringUtils.split(p_timeStr, ":");
			if (p_timeStr == null) {
				return 0;
			}
			String[] timeArr = p_timeStr.split(":");
			int secCnt = 0;
			if (timeArr == null || timeArr.length == 0) {
				secCnt = 0;
			}
			else if (timeArr.length == 2) {
				secCnt = Integer.parseInt(timeArr[0])*3600 + Integer.parseInt(timeArr[1])*60;
			}
			else if (timeArr.length >= 2) {
				secCnt = Integer.parseInt(timeArr[0])*3600 + Integer.parseInt(timeArr[1])*60 + Integer.parseInt(timeArr[2]);
			}
			return secCnt;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	static public boolean equals(Date p_date1, Date p_date2) {
		if(p_date1 == null) {
			return(p_date2 == null);
		}  else {
			return((!(p_date2 == null)) && (p_date1.equals(p_date2)));
		}
	}
	@Override
	public Object evalVariableRelative(String p_varname, int p_idx) throws Exception {
		throw new Exception("evalVariableRelative not supported");
	}
	
	
	/***
	 * get the date of next task, used for vincero cron scheduler
	 * @param p_lastTask
	 * @param p_interval
	 * @param p_nextHour
	 * @param p_nextMin
	 * @return
	 */
	public static Date nextTask(Date p_lastTask, int p_interval, int p_nextHour, int p_nextMin) {
		//UniLog.log1("lastTask:%s interval:%d", p_lastTask, p_interval);
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date curDate = new Date();
		
		
		Date nextTask = null;
		if (p_lastTask == null) {
			nextTask = curDate;
			p_lastTask = curDate;
			//UniLog.log1("hr:%d min:%d interval:%d last:%s next:%s immediate:%s (no last)", p_nextHour, p_nextMin, p_interval, df.format(p_lastTask),df.format(nextTask), nextTask.getTime() <= curDate.getTime());
			return (nextTask);
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(p_lastTask);
		
		
		cal.set(Calendar.HOUR_OF_DAY, p_nextHour);
		cal.set(Calendar.MINUTE, p_nextMin);
		cal.set(Calendar.SECOND, 0);
		nextTask = cal.getTime();
		if (nextTask.getTime() >= p_lastTask.getTime()) {
			//UniLog.log1("hr:%d min:%d interval:%d last:%s next:%s immediate:%s (no interval)", p_nextHour, p_nextMin, p_interval, df.format(p_lastTask),df.format(nextTask), nextTask.getTime() <= curDate.getTime());
			return (nextTask);
		}
		
		cal.add(Calendar.MILLISECOND, p_interval);
		nextTask = cal.getTime();
		//UniLog.log1("hr:%d min:%d interval:%d last:%s next:%s immediate:%s (add interval)", p_nextHour, p_nextMin, p_interval, df.format(p_lastTask),df.format(nextTask), nextTask.getTime() <= curDate.getTime());
		return (nextTask);

	}	
	
	public static boolean isValidYearMonth(String input, String yearMonthFormat) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(yearMonthFormat);
            YearMonth.parse(input, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

	public static boolean isValidMonthDay(String input, String monthDayFormat) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(monthDayFormat);
            MonthDay.parse(input, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
