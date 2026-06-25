package com.uniinformation.utils;
import java.io.*;
import java.util.*;

public abstract class TMFormat implements Serializable {
   public static final int TMF_NUMBER = 0;    // e.g. ###,##&.&&
   public static final int TMF_TEXTAREA = 1;
   public static final int TMF_CHECKBOX = 2;
   public static final int TMF_PASSWORD = 3;
   public static final int TMF_TIME = 4;
   public static final int TMF_DURATION = 5;
   public static final int TMF_WEEKDAY = 6;
   public static final int TMF_DATE = 7;
	public abstract int getType();
	public StringBuffer getXML(StringBuffer p_sb) {
	   return(p_sb);
	}
   public String formatDisplay(String p_value) {
	   return(p_value);
	}
}
