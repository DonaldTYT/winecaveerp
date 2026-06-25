package com.uniinformation.utils;
import java.io.*;
import java.util.*;

public class TMMapWithTip extends TMMap {
	Vector tipValues;
	public TMMapWithTip(Vector p_leftvalues, Vector p_rightvalues, Vector p_tipvalues) {
	   leftvalues = p_leftvalues;
	   rightvalues = p_rightvalues;
		lefthash = new Hashtable();
		righthash = new Hashtable();
		for (int i=0; i<p_leftvalues.size(); i++) {
		   lefthash.put(keyConvert(p_leftvalues.elementAt(i)), p_rightvalues.elementAt(i));
		   righthash.put(keyConvert(p_rightvalues.elementAt(i)), p_leftvalues.elementAt(i));
		}
		tipValues = p_tipvalues;
	}
	public TMMapWithTip() {
		super();
		tipValues = new Vector();
	}
   public TMMap addPair(Object p_left, Object p_right, Object p_tip) {
	   leftvalues.addElement(p_left);
	   rightvalues.addElement(p_right);
		lefthash.put(keyConvert(p_left), p_right);
		righthash.put(keyConvert(p_right), p_left);
		tipValues.addElement(p_tip);
		return(this);
	}
}
