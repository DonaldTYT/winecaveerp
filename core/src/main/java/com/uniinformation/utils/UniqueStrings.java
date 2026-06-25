package com.uniinformation.utils;

import java.util.LinkedHashSet;

public class UniqueStrings {
	LinkedHashSet<String> lhs = new LinkedHashSet<String>();
	String delimiter;
	public UniqueStrings(String p_delimiter) {
		delimiter = p_delimiter;
	}
	public UniqueStrings add(String p_str) {
		if(p_str == null || p_str.trim().equals("")) return(this);
		if(lhs.contains(p_str.trim())) return(this);
		lhs.add(p_str.trim());
		return(this);
	}
	public String toString() {
		String s = null;
		for(String ss : lhs) {
			if(s == null) s = ss; else s += delimiter + ss;
		}
		return(s);
	}
}
