package com.kikyosoft.utils;

import java.io.*;
import java.util.*;

public class Sprintf {
	String format;
	Vector v = new Vector();
	public Sprintf(String p_format) {
	   format = p_format;
	}
	public void clear() {
	   v.clear();
	}
	public Sprintf add(int p_int) {
	   v.addElement(new Integer(p_int));
		return(this);
	}
	public Sprintf add(double p_double) {
	   v.addElement(new Double(p_double));
		return(this);
	}
	public Sprintf add(float p_float) {
	   v.addElement(new Double(p_float));
		return(this);
	}
	public Sprintf add(String p_string) {
	   v.addElement(p_string);
		return(this);
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
	   char[] ca = format.toCharArray();
		int j=0;
		int state = 0;
		int length = 0;
		int decimal = 0;
		boolean isNegative = false;
		boolean isZero = false;
		for (int i=0; i<ca.length; ) {
			String tmpstr;
		   switch (state) {
			   case 0: // looking for %
					if (ca[i] == '%') {
						length = 0;
						decimal = 0;
						isNegative = false;
						isZero = false;
					   state = 1;
					}
					else
					   sb.append(ca[i]);
					i++;
				   break;
			   case 1: // looking for -ve
					if (ca[i] == '-') {
					   isNegative = true;
					   i++;
					}
					state = 2;
				   break;
			   case 2: // looking for length
				   if (ca[i] >= '0' && ca[i] <= '9') {
					   length = length * 10 + (ca[i]-'0');
						if (length == 0)
						   isZero = true;
					   i++;
					}
					else if (ca[i] == '.') {
					   state = 3;
					   i++;
					}
					else
					   state = 4;
				   break;
			   case 3: // looking for decimal place
				   if (ca[i] >= '0' && ca[i] <= '9') {
					   decimal = decimal * 10 + (ca[i]-'0');
					   i++;
					}
					else 
					   state = 4;
				   break;
			   case 4: // looking for format type
					if (ca[i] == '%') {
					   sb.append("%");
					   i++;
					} 
					else if (j < v.size()) {
					   switch (ca[i]) {
							case '%':
								sb.append("%");
								i++;
							   break;
						  case 'c':
						  {
							    Object o = v.elementAt(j++);
								int chint = 0;
							    if(o instanceof Integer) chint = ((Integer) o).intValue();
							    if(o instanceof Double ) chint = (int) ((Double) o).doubleValue();
							    if(chint > 0) {
							    	char ch = (char) chint;
							    	sb.append(ch);
							    }
							    i++;
							    break;
						  }
					      case 'f':
								sb.append(v.elementAt(j++));
								i++;
						      break;
					      case 'x':
					      case 'd':
							   if (ca[i] == 'x')
								   tmpstr = TextUtil.intToHex(ValueUtil.intValue(v.elementAt(j++)));
								else
							      tmpstr = ""+ValueUtil.intValue(v.elementAt(j++));
								if (tmpstr.length() < length) {
								   for (int k=(length-tmpstr.length()); k>0; k--) {
									   if (isZero)
										   sb.append("0");
									   else
										   sb.append(" ");
									}
								}
								sb.append(tmpstr);
								i++;
						      break;
					      case 's':
								tmpstr = ValueUtil.stringValue(v.elementAt(j++));
								if (length <= 0)
								   sb.append(tmpstr);
								else {
								   if (tmpstr.length() >= length) {
										if (decimal > 0)
										   sb.append(tmpstr.substring(0, decimal));
										else
								         sb.append(tmpstr);
									}
									else {
										if (isNegative)
										   sb.append(tmpstr);
								      for (int k=(length-tmpstr.length()); k>0; k--)
										   sb.append(" ");
										if (!isNegative)
										   sb.append(tmpstr);
									}
								}
								i++;
						      break;
					   }
					}
					state = 0;
			      break;
			}
		}
		return(sb.toString());
	}
	public static void main(String[] args) {
		Sprintf a = new Sprintf(args[0]);
		for (int i=1; i<args.length; i++) {
		   a.add(args[i]);
		}
	   System.out.println("["+a.toString()+"]");
	}
}
