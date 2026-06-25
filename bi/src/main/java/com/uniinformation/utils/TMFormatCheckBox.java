package com.uniinformation.utils;
import java.io.*;
import java.util.*;

public class TMFormatCheckBox extends TMFormat {
   boolean fmirrored = false;
	public TMFormatCheckBox() {
	}
	public boolean setMirrored(boolean p_fmirrored) { 
	   boolean oflag = fmirrored;
	   fmirrored = p_fmirrored;
		return(oflag);
	}
	public boolean isMirrored() { 
	   return(fmirrored);
	}
	public int getType() { 
	   return(TMFormat.TMF_CHECKBOX); 
	}
	public StringBuffer getXML(StringBuffer p_sb) {
		p_sb.append("   <Format type=\"checkbox\" ");
		p_sb.append("/>");
	   return(p_sb);
	}
}
