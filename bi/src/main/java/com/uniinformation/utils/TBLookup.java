package com.uniinformation.utils;
import java.io.*;
import java.util.*;

public class TBLookup implements Serializable {
	public String tabname, tabaliase;
	public Vector mapQualifiedColNames = null;
	public Vector mapColNames = null;
	public Vector condFieldNames = null;
	public Vector condColNames = null;
	public Wherecl wherecl = null;
	private boolean fBlindInclude = false;
	private boolean fBlindIncludeUsed = false;
	private Hashtable hCond = null;
	private Hashtable hMapColumns = null;
	private String selectString = null;
	public TBLookup(String p_tabname, String p_tabaliase) {
	   tabname = p_tabname;
		tabaliase = p_tabaliase;
	   mapColNames = new Vector();
	   mapQualifiedColNames = new Vector();
	   condFieldNames = new Vector();
	   condColNames = new Vector();
	   hCond = new Hashtable();
	   hMapColumns = new Hashtable();
	}
	public TBLookup addMap(String p_ColName) {
		mapColNames.addElement(p_ColName);
		String fn = tabaliase+"."+p_ColName;
		mapQualifiedColNames.addElement(fn);
	   hMapColumns.put(fn, p_ColName);
	   return(this);
	}
	public TBLookup addCond(String p_fieldName, String p_ColName) {
		condFieldNames.addElement(p_fieldName);
		condColNames.addElement(p_ColName);
	   hCond.put(p_fieldName, p_ColName);
	   return(this);
	}
	public TBLookup setWherecl(Wherecl p_wherecl) {
	   wherecl = p_wherecl;
		return(this);
	}
	public String toString() {
	   StringBuffer sb = new StringBuffer();
	   sb.append(this.getClass().getName()).append("=[map(");
		for (int i=0; i<mapColNames.size(); i++) {
			if (i > 0)
			  sb.append(",");
			sb.append(tabname).append(".");
		   sb.append((String) mapColNames.elementAt(i));
		}
	   sb.append(") joining (");
		for (int i=0; i<condFieldNames.size(); i++) {
			if (i > 0)
			  sb.append(",");
		   sb.append((String) condFieldNames.elementAt(i));
		   sb.append("=");
			sb.append(tabname).append(".");
		   sb.append((String) condColNames.elementAt(i));
		}
	   sb.append(")");
	   if (wherecl != null) {
	      sb.append(" where ");
			sb.append(wherecl.toString());
		}
	   sb.append("]");
	   return(sb.toString());
	}
	public String getTabname() {
	   return(tabname);
	}
	public String getTabaliase() {
	   return(tabaliase);
	}
	public Vector getMapColNames() {
	   return(mapColNames);
	}
	public Vector getMapQualifiedColNames() {
	   return(mapQualifiedColNames);
	}
	public Vector getCondFieldNames() {
	   return(condFieldNames);
	}
	public void setFBlindInclude(boolean p_fBlindInclude) {
	   fBlindInclude = p_fBlindInclude;
	}
	public boolean getFBlindInclude() {
	   return(fBlindInclude);
	}
	public void setFBlindIncludeUsed(boolean p_fBlindIncludeUsed) {
	   fBlindIncludeUsed = p_fBlindIncludeUsed;
	}
	public boolean getFBlindIncludeUsed() {
	   return(fBlindIncludeUsed);
	}
	public boolean isJoinTag(String p_tagname) {
	   return(hCond.get(p_tagname) != null);
	}
	public boolean isMappedTagname(String p_fullQualifiedColName) {
	   if (hMapColumns.get(p_fullQualifiedColName) != null) 
		   return(true);
	   else
		   return(false);
	}
	private void buildSelectString() {
	   if (selectString != null)
		   return;
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		for (int i=0; i<mapColNames.size()+condColNames.size(); i++) {
			if (i > 0)
				sb.append(",");
			if (i < mapColNames.size())
			   sb.append((String) mapColNames.elementAt(i));
			else 
			   sb.append((String) condColNames.elementAt(i-mapColNames.size()));
		}
		sb.append(" from ")
		  .append(tabname);
		selectString = sb.toString();
	}
	public int forwardLookup(TableMaint p_tm, SelectUtil p_su) {
	   if (mapColNames.size() <= 0)
		   return(0);
	   buildSelectString();
		Wherecl wc = new Wherecl();
		for (int i=0; i<condColNames.size(); i++) {
			wc.appendString(" and ")
			  .appendString((String) condColNames.elementAt(i))
		     .appendString(" = ? ")
			  .appendArgument(p_tm.getField((String) condFieldNames.elementAt(i)));
		}
		wc.stripAnd();
		try {
		   TableRec tr = p_su.getQueryResult(selectString, wc, 1);
			if (tr == null || tr.getRecordCount() == 0) {
			   for (int i=0; i<mapColNames.size(); i++)
			      p_tm.setField((String) mapQualifiedColNames.elementAt(i), "");
			}
			else {
			   for (int i=0; i<mapColNames.size(); i++)
			      p_tm.setField((String) mapQualifiedColNames.elementAt(i), 
				                      tr.getField((String) mapColNames.elementAt(i)).toString());
			}
			return(1);
		} catch (Exception ex) {
		   UniLog.log(ex);
			return(0);
		}
	}
	public int backwardLookup(TableMaint p_tm, SelectUtil p_su, String p_fullColName) {
	   String colname = (String) hMapColumns.get(p_fullColName);
		if (colname == null)
		   return(0);
	   buildSelectString();
		Wherecl wc = new Wherecl()
		                 .appendString(colname)
		                 .appendString(" = ? ")
		                 .appendArgument(p_tm.getField(p_fullColName))
		                 .stripAnd();
		try {
		   TableRec tr = p_su.getQueryResult(selectString, wc, 1);
			if (tr == null || tr.getRecordCount() == 0) {
			   for (int i=0; i<mapColNames.size(); i++) {
               if (!((String) mapQualifiedColNames.elementAt(i)).equals(p_fullColName))
			         p_tm.setField((String) mapQualifiedColNames.elementAt(i), "");
				}
			   for (int i=0; i<condFieldNames.size(); i++)
			      p_tm.setField((String) condFieldNames.elementAt(i), "");
			}
			else {
			   for (int i=0; i<mapColNames.size(); i++)
			      p_tm.setField((String) mapQualifiedColNames.elementAt(i), 
				                         tr.getField((String) mapColNames.elementAt(i)).toString());
			   for (int i=0; i<condFieldNames.size(); i++)
			      p_tm.setField((String) condFieldNames.elementAt(i), 
				                      tr.getField((String) condColNames.elementAt(i)).toString());
			}
			return(1);
		} catch (Exception ex) {
		   UniLog.log(ex);
			return(0);
		}
	}
	public static void main(String args[]) {
		System.out.println(
	      (new TBLookup("maptab", "maptabaliase"))
		      .addMap("colname0")
		      .addMap("colname1")
		      .addCond("tagname2", "colname2")
		      .addCond("tagname3", "colname3")
			   .toString()
		);
	}
}
