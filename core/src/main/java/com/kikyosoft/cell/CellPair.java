package com.kikyosoft.cell;

import org.apache.commons.lang3.tuple.Pair;

import org.apache.commons.lang3.tuple.Pair;

public class CellPair extends Pair {
	Comparable left;
	Object right;
	
	public CellPair(Comparable p_left,Object p_right) {
		left =p_left;
		right = p_right;
	}
	@Override
	public Object setValue(Object arg0) {
		left = (Comparable) arg0;
		Object oo = left;
		return(oo);
	}
	@Override
	public Object getValue() {
		return(left);
	}
	@Override
	public int compareTo(Pair arg0) {
		int cc = left.compareTo(((CellPair) arg0).left);
		if( cc != 0) return(cc);
		return(right.toString().compareTo(((CellPair) arg0).right.toString()));
	}
	
	@Override
	public Object getLeft() {
		return left;
	}

	@Override
	public Object getRight() {
		return right;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof CellPair) {
			return(left.equals(((CellPair)o).left));
		} else return(right.equals(o));
	}
	
	@Override public String toString() {
		return(right.toString());
	}

	static public CellPair of(Comparable p_left, Object p_right) {
		return(new CellPair(p_left,p_right));
	}
}