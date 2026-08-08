package com.uniinformation.utils;
import com.uniinformation.rpccall.*;
import java.util.*;

public class WxAccess
{
	public String description;
	public String formName;
	public String initString;
	public String iconFile;
	public String label;
	public String parent;
	public String butName;
	public String butName2;
	public WxAccess(String p_description,String p_formname,String p_initstring,String p_iconfile,String p_label,String p_parent)
	{
		description = p_description;
		formName = p_formname;
		initString = p_initstring;
		iconFile = p_iconfile;
		label = p_label;
		parent = p_parent;
	}
	public boolean hasChild()
	{
		return(false);
	}
}
