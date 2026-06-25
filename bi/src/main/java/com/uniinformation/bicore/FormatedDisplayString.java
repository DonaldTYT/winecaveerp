package com.uniinformation.bicore;

public class FormatedDisplayString {
	Object value;
	String formattedString;
	public FormatedDisplayString(Object Value,String type,String format,String p_str) {
			formattedString = p_str;
		
	}
	public String toString() {
		return(formattedString);
	}
}
