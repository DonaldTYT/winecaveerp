package com.uniinformation.scorpion.driver;

import java.util.*;

public class ScpTable {
	Hashtable ht_column;
	String tabname;
	public ScpTable(String p_tabname) {
		ht_column = new Hashtable();
		tabname = p_tabname;
	}
	public void addField(ScpField p_field)
	{
		ht_column.put(p_field.getFdname(),p_field);
	}
	public String getTabname()
	{
		return(tabname);
	}
}
