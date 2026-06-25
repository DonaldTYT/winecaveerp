package com.kikyosoft.utils;

import java.util.*;

public class ValueUtil {
   public static int intValue(Object p_obj) {
	    if (p_obj instanceof Float)
		    return(((Float) p_obj).intValue());
	    else if (p_obj instanceof Double)
		    return(((Double) p_obj).intValue());
	    else if (p_obj instanceof Integer)
		    return(((Integer) p_obj).intValue());
		 else if (p_obj instanceof String)
		    return(Integer.parseInt((String) p_obj));
		 LogUtil.log(new Exception("invalid object type intValue("+p_obj.getClass().getName()+")"));
	    return(0);
	}
   public static long longValue(Object p_obj) {
	    if (p_obj instanceof Float)
		    return(((Float) p_obj).longValue());
	    else if (p_obj instanceof Double)
		    return(((Double) p_obj).longValue());
	    else if (p_obj instanceof Integer)
		    return(((Integer) p_obj).longValue());
		 else if (p_obj instanceof String)
		    return(Long.parseLong((String) p_obj));
		 LogUtil.log(new Exception("invalid object type longValue("+p_obj.getClass().getName()+")"));
	    return(0);
	}
   public static float floatValue(Object p_obj) {
	    if (p_obj instanceof Float)
		    return(((Float) p_obj).floatValue());
	    else if (p_obj instanceof Double)
		    return(((Double) p_obj).floatValue());
	    else if (p_obj instanceof Integer)
		    return(((Integer) p_obj).floatValue());
		 else if (p_obj instanceof String)
		    return(Float.parseFloat((String) p_obj));
		 LogUtil.log(new Exception("invalid object type floatValue("+p_obj.getClass().getName()+")"));
	    return(0);
	}
   public static double doubleValue(Object p_obj) {
	    if (p_obj instanceof Float)
		    return(((Float) p_obj).doubleValue());
	    else if (p_obj instanceof Double)
		    return(((Double) p_obj).doubleValue());
	    else if (p_obj instanceof Integer)
		    return(((Integer) p_obj).doubleValue());
		 else if (p_obj instanceof String)
		    return(Double.parseDouble((String) p_obj));
		 LogUtil.log(new Exception("invalid object type doubleValue("+p_obj.getClass().getName()+")"));
	    return(0);
	}
   public static String stringValue(Object p_obj) {
	    if (p_obj instanceof Float)
		    return(((Float) p_obj).toString());
	    else if (p_obj instanceof Double)
		    return(((Double) p_obj).toString());
	    else if (p_obj instanceof Integer)
		    return(((Integer) p_obj).toString());
	    else if (p_obj instanceof String)
		    return((String) p_obj);
		 LogUtil.log(new Exception("invalid object type toString("+p_obj.getClass().getName()+")"));
	    return(null);
	}
}
