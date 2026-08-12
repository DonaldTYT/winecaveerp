package com.uniinformation.webcore;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import com.uniinformation.utils.*;
import com.uniinformation.cell.*;

public class PageHelper {

	public static final int LANGUAGE_ENGLISH = 0;
	public static final int LANGUAGE_TRADITIONAL_CHINESE = 1;
	/*

	public static void printSideMenu(SessionHelper sessionHelper,DoorUtil p_doorUtil, JspWriter p_out) {
	}
	public static void printAccountSideMenu(SessionHelper sessionHelper,DoorUtil p_doorUtil, JspWriter p_out) {
	}
	public static void  printAccountTopMenu(SessionHelper sessionHelper,DoorUtil p_doorUtil, JspWriter p_out) throws Exception
	{		CellCollection actms = sessionHelper.getAcTopMenus();
		if(actms != null) {
			Vector v = actms.getCollectionList("menuList");
			for(int i = 0;i<v.size();i++) {
				CellCollection actm = (CellCollection) v.get(i);
				String menuName = actm.getCell("menuName").getString();
				String menuHook = actm.getCell("menuHook").getString();
				UniLog.log("Show Account Menu " + menuName + " : " + menuHook);
				p_out.println(
			"<li id=\""+actm.getCell("menuID").getString()+"\"><a href=\""+
			menuHook+
			"\">"+
			menuName+
			"</a></li>");
			}
		}
	}
	public static void  printAccountDashBoard(SessionHelper sessionHelper,DoorUtil p_doorUtil, JspWriter p_out) {
		
	}
	*/
}
