package com.uniinformation.scorpion.driver;
import java.util.*;

public class ScpSchema {
	Hashtable ht_table;
	String dbname;
	public ScpSchema(String p_dbname) {
		ht_table = new Hashtable();
		dbname = p_dbname;
	}
	public void addTable(ScpTable p_table)
	{
		ht_table.put(p_table.getTabname(),p_table);
	}
}
