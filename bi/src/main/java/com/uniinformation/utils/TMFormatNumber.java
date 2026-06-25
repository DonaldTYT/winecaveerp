package com.uniinformation.utils;
import java.io.*;
import java.util.*;

import com.kyoko.common.NumberUtil;
import com.kyoko.common.Sprintf;

import java.text.*;

public class TMFormatNumber extends TMFormat {
	String picture;
	private DecimalFormat decFormat;
	private FieldPosition fieldPosition;
	public int getType() { 
	   return(TMFormat.TMF_NUMBER);
	}
	public String getPicture() { 
	   return(picture); 
	}
	public TMFormatNumber(String p_picture) {
		picture = p_picture;
		decFormat = new DecimalFormat(picture);
	   fieldPosition = new FieldPosition(NumberFormat.INTEGER_FIELD);
	}
	public StringBuffer getXML(StringBuffer p_sb) {
		p_sb.append("   <Format type=\"number\" ");
		p_sb.append("picture=\"").append(picture).append("\" ");
		p_sb.append("/>");
		return(p_sb);
	}
	public String formatDisplay(double t) {
		if (t == 0 && picture.endsWith("#")) {
		   return(new Sprintf("%"+picture.length()+"s").add("").toString());
		}
		return(decFormat.format(t+0.000001, new StringBuffer(), fieldPosition).toString());
	}
	public String formatDisplay(String p_value) {
	   double t;
		try {
			t = NumberUtil.parseDouble(p_value);
		} catch (NumberFormatException ex) {
	      UniLog.logClass(this, "formatDisplay("+p_value+"): failed");
		   UniLog.log(ex);
		   t = 0;
		}
		if (t == 0 && picture.endsWith("#")) {
		   return(new Sprintf("%"+picture.length()+"s").add("").toString());
		}
		return(decFormat.format(t+0.000001, new StringBuffer(), fieldPosition).toString());
	}
	public static void main(String[] args) {
	   TMFormatNumber tmf = new TMFormatNumber(args[0]);
	   UniLog.log(tmf.formatDisplay(args[1]));
	}
}
