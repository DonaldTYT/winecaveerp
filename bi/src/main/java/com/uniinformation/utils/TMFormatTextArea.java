package com.uniinformation.utils;
import java.io.*;
import java.util.*;

public class TMFormatTextArea extends TMFormat {
	int row, col;
	String wrap;
   public TMFormatTextArea(int p_row, int p_col, String p_wrap) {
		row = p_row;
		col = p_col;
		wrap = p_wrap;
	}
	public int getType() { 
	   return(TMFormat.TMF_TEXTAREA); 
	}
	public int getRow() { 
	   return(row); 
	}
	public int getCol() { 
	   return(col); 
	}
	public String getWrap() { 
	   return(wrap); 
	}
	public StringBuffer getXML(StringBuffer p_sb) {
		p_sb.append("   <Format type=\"textarea\" ");
		p_sb.append("row=\"").append(row).append("\" ");
		p_sb.append("col=\"").append(col).append("\" ");
		if (wrap != null)
			p_sb.append("wrap=\"").append(wrap).append("\" ");
		p_sb.append("/>");
		return(p_sb);
	}
}
