package com.uniinformation.utils;

import java.util.*;

public class ThreadData {
	static ThreadLocal thGrpDataHash = new ThreadLocal();
	static public void setThGrpHash(Hashtable ht) {
		thGrpDataHash.set(ht);
	}
	static public Hashtable getThGrpHash() {
		Hashtable ht = (Hashtable) thGrpDataHash.get();
		if(ht == null) {
			ht = new Hashtable();
			thGrpDataHash.set(ht);
		}
		return(ht);
	}
	static public Object getThGrpData(String p_key) {
		Hashtable ht = getThGrpHash();
		return(ht.get(p_key));
	}
	static public void setThGrpData(String p_key,Object p_obj) {
		Hashtable ht = getThGrpHash();
		if(p_obj == null) ht.remove(p_key); else ht.put(p_key,p_obj);
	}
}
