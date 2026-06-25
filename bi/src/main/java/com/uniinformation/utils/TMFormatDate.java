package com.uniinformation.utils;
import java.io.*;
import java.util.*;

import com.kyoko.common.DateUtil;

public class TMFormatDate extends TMFormat {
	String picture;
	public int getType() { 
	   return(TMFormat.TMF_DATE);
	}
	public String getPicture() { 
	   return(picture); 
	}
	public TMFormatDate(String p_picture) {
		picture = p_picture;
	}
	public StringBuffer getXML(StringBuffer p_sb) {
		p_sb.append("   <Format type=\"date\" ");
		p_sb.append("picture=\"").append(picture).append("\" ");
		p_sb.append("/>");
		return(p_sb);
	}
	public String formatDisplay(String p_value) {
	   if (p_value == null)
		   return("");
		if (p_value.trim().equals(""))
		   return("");
		return(DateUtil.toDateString(DateUtil.getDate(p_value), picture));
	}
}
