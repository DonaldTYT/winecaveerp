package com.uniinformation.utils;
import java.io.*;
import java.util.*;

import com.kyoko.common.StringUtil;

public class TMMap implements Serializable {
   Vector leftvalues;
   Vector rightvalues;
   Hashtable lefthash;
   Hashtable righthash;
	boolean fMultiple = false;
	int rowsize = 1;
	static XslUtil xlsroot;
	public TMMap(Vector p_leftvalues, Vector p_rightvalues) {
	   leftvalues = p_leftvalues;
	   rightvalues = p_rightvalues;
		lefthash = new Hashtable();
		righthash = new Hashtable();
		for (int i=0; i<p_leftvalues.size(); i++) {
		   lefthash.put(keyConvert(p_leftvalues.elementAt(i)), p_rightvalues.elementAt(i));
		   righthash.put(keyConvert(p_rightvalues.elementAt(i)), p_leftvalues.elementAt(i));
		}
	}
	public TMMap() {
	   leftvalues = new Vector();
	   rightvalues = new Vector();
		lefthash = new Hashtable();
		righthash = new Hashtable();
	}
   public TMMap addPair(Object p_left, Object p_right) {
	   leftvalues.addElement(p_left);
	   rightvalues.addElement(p_right);
		lefthash.put(keyConvert(p_left), p_right);
		righthash.put(keyConvert(p_right), p_left);
		return(this);
	}
   public Object leftToRight(Object p_left) {
	   return(lefthash.get(keyConvert(p_left)));
	}
   public Object rightToLeft(Object p_right) {
	   Object leftValue = righthash.get(keyConvert(p_right));
		return(leftValue);
	}
	public Vector getLeftValues() {
	   return(leftvalues);
	}
	public Vector getRightValues() {
	   return(rightvalues);
	}
	public static TMMap genYesNo() {
	   TMMap tm;
		tm = new TMMap();
		tm.addPair("Y", "Yes");
		tm.addPair("N", "No");
	   return(tm);
	}
	public static TMMap genYesNo(String p_lang) {
	   return(genYesNo());
	}
	public static TMMap genWeekday() {
	   TMMap tm;
		tm = new TMMap();
		tm.addPair("0", "Sunday");
		tm.addPair("1", "Monday");
		tm.addPair("2", "Tuesday");
		tm.addPair("3", "Wednesday");
		tm.addPair("4", "Thursday");
		tm.addPair("5", "Friday");
		tm.addPair("6", "Saturday");
	   return(tm);
	}
	public static TMMap genWeekday(String p_lang) {
	   return(genWeekday());
	}
	public String toString() {
	   StringBuffer sb = new StringBuffer();
		sb.append("RightValues=").append(rightvalues.toString());
		sb.append("\nLeftValues=").append(leftvalues.toString());
		return(sb.toString());
	}
	private String toXmlString(String p_curvalue) {
		StringBuffer sb = new StringBuffer();
		int cnt = leftvalues.size();
		sb.append("<tag curvalue=\"").append(StringUtil.convertWebString(p_curvalue)).append("\">\n");
		for (int j=0; j<cnt; j++) {
			sb.append("<Option postvalue=\"");
			sb.append(StringUtil.convertWebString((String) leftvalues.elementAt(j)));
			sb.append("\">");
			sb.append(StringUtil.convertWebString((String) rightvalues.elementAt(j)));
			sb.append("</Option>\n");
		}
		sb.append("</tag>\n");
	   return(sb.toString());
	}
	public String xslprocess(String p_curvalue, boolean p_isIncludeBlank) {
		StringWriter sw = new StringWriter();
		try {
	      TMMap.xlsprepare();
	      xlsroot.transform(new StringReader(toXmlString(p_curvalue)), sw);
		} catch (Exception ex) {
			UniLog.log(ex);
		   return("");
		}
		if (p_isIncludeBlank) 
		   return("<option value=\"\"></option>"+sw.toString());
	   else
		   return(sw.toString());
	}
	static synchronized void xlsprepare() throws Exception {
		if (xlsroot == null) {
		   String xslstring = 
            "<?xml version=\"1.0\"?> \n"
            +"<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">\n"
            +"<xsl:output method=\"html\"/>\n"
            +"\n"
            +"<xsl:template match=\"tag\">\n"
            +"   <xsl:apply-templates select=\"Option\">\n"
            +"      <xsl:with-param name=\"colvalue\">\n"
            +"         <xsl:value-of select=\"@curvalue\"/>\n"
            +"      </xsl:with-param>\n"
            +"   </xsl:apply-templates>\n"
            +"</xsl:template>\n"
            +"\n"
            +"<xsl:template match=\"Option\">\n"
            +"   <xsl:param name=\"colvalue\"></xsl:param>\n"
            +"   <xsl:variable name=\"thiscolvalue\"><xsl:value-of select=\"@postvalue\"/></xsl:variable>\n"
            +"   <xsl:choose>\n"
            +"      <xsl:when test=\"$colvalue=$thiscolvalue\">\n"
            +"         <option>\n"
            +"            <xsl:attribute name=\"value\"><xsl:value-of select=\"@postvalue\"/></xsl:attribute>\n"
            +"            <xsl:attribute name=\"selected\"/>\n"
            +"            <xsl:value-of select=\".\"/>\n"
            +"         </option> \n"
            +"      </xsl:when>\n"
            +"      <xsl:otherwise>\n"
            +"         <option>\n"
            +"            <xsl:attribute name=\"value\"><xsl:value-of select=\"@postvalue\"/></xsl:attribute>\n"
            +"            <xsl:value-of select=\".\"/>\n"
            +"         </option> \n"
            +"      </xsl:otherwise>\n"
            +"   </xsl:choose>\n"
            +"</xsl:template>\n"
            +"\n"
            +"</xsl:stylesheet>\n";
         xlsroot = XslUtil.getByString(xslstring, null);
			xlsroot.prepare();
	   }
	}
	public TMMap setMultiple(boolean p_flag) {
	   fMultiple = p_flag;
		return(this);
	}
	public TMMap setRowsize(int p_rowsize) {
	   rowsize = p_rowsize;
		return(this);
	}
	public boolean isMultiple() {
	   return(fMultiple);
	}
	public int getRowsize() {
	   return(rowsize);
	}
	public Object keyConvert(Object p_object) {
	   if (p_object instanceof String)
		   return((new String((String) p_object)).trim());
	   else
		   return(p_object);
	}
	public static void main(String args[]) {
		TMMap tm = TMMap.genYesNo();
	   System.out.println("Map(Y)="+(String) tm.leftToRight("Y"));
	   System.out.println("Map(N)="+(String) tm.leftToRight("N"));
	   System.out.println("Map(Yes)="+(String) tm.rightToLeft("Yes"));
	   System.out.println("Map(No)="+(String) tm.rightToLeft("No"));
	   System.out.println("Map(Y)="+(String) tm.leftToRight(args[0]));
	   System.out.println("Map="+tm);
	   System.out.println("xmlString="+tm.toXmlString(args[0]));
		for (int i=0; i<10; i++)
	      System.out.println("htmlString="+tm.xslprocess(args[0], true));
	}
}
