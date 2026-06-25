package com.uniinformation.bicore;

public class StringWithClass {
	String strValue;
	String strClass;
	public StringWithClass(String p_str,String p_class) {
		strValue = p_str;
		strClass = p_class;
	}
	public String toString() {
		return(strValue);
	}
	public String strClass() {
		return(strClass);
	}
}
