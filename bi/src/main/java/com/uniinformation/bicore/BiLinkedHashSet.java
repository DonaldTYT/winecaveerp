package com.uniinformation.bicore;

import java.util.LinkedHashSet;

public class BiLinkedHashSet extends LinkedHashSet implements Comparable {
	private static final long serialVersionUID = 1L;

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return(toString().compareTo(arg0.toString()));
	}

}
