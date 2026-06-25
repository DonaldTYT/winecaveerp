package com.kikyosoft.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import com.kyoko.common.CoreLog;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;


public class SessionUtil {
	static String getContextPath() {
		return("http://localhost:8080/");
	}
	static public final String SUBMENU_PREFIX="menu.html?menuid=";
	static void generateOneSubmenu(String p_contextPath,MenuNode p_parent,TableRec tr, String p_rootMenu,String p_agent,SessionHelper p_sp) throws Exception{
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
//			CoreLog.log(tr.getFieldString("webm_desc"));
			if(tr.getFieldString("webm_menuid").equals(p_rootMenu)) {
			  String url = tr.getFieldString("webm_url");
			  if(!p_sp.checkWebMenuAccess(url)) continue;
			  if(url.startsWith(SUBMENU_PREFIX)) {
				  MenuNode subMenu = new MenuNode(tr.getFieldString("webm_desc"), "#!", "ti ti-typography");
				  String nextRoot = url.substring(SUBMENU_PREFIX.length());
				  generateOneSubmenu(p_contextPath,subMenu ,tr, nextRoot,p_agent,p_sp);
				  p_parent.add(subMenu);
			  } else {
				  String agent="";
				  url += (url.contains("?")  ? "&" : "?") +"theme=geneh&sidemenu=N";
				  if(!StringUtils.isBlank(p_agent)) {
					  url = getContextPathForAgent(p_sp,p_agent) + url;
					  agent="&agent="+p_agent;
				  }
				  p_parent.add(new MenuNode(tr.getFieldString("webm_desc"), 
						  p_contextPath+(p_contextPath.contains("?")  ? "&" : "?") +"iframeUrl="+URLEncoder.encode(url,StandardCharsets.UTF_8)+agent
						  , "ti ti-typography"));
			  }
			}
		}
	}
	static void generateOneMenu(String p_contextPath,List<MenuNode> p_menu,String p_captionName,String p_captionIcon,TableRec tr, String p_rootMenu,String p_agent,SessionHelper p_sp) throws Exception{
		if(! (StringUtils.isBlank(p_captionName) || StringUtils.isBlank(p_captionIcon))) {
			  p_menu.add(MenuNode.caption(p_captionName, "ti ti-dashboard"));
		}
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
//			CoreLog.log(tr.getFieldString("webm_desc"));
			if(tr.getFieldString("webm_menuid").equals(p_rootMenu)) {
			  String url = tr.getFieldString("webm_url");
			  if(!p_sp.checkWebMenuAccess(url)) continue;
			  if(url.startsWith(SUBMENU_PREFIX)) {
				  MenuNode subMenu = new MenuNode(tr.getFieldString("webm_desc"), "#!", "ti ti-typography");
				  String nextRoot = url.substring(SUBMENU_PREFIX.length());
				  generateOneSubmenu(p_contextPath,subMenu ,tr, nextRoot,p_agent,p_sp);
				  p_menu.add(subMenu);
			  } else {
				  String agent = "";
				  url += (url.contains("?")  ? "&" : "?") +"theme=geneh&sidemenu=N";
				  if(!StringUtils.isBlank(p_agent)) {
					  url = getContextPathForAgent(p_sp,p_agent) + url;
					  agent = "&agent="+p_agent;
				  }
				  p_menu.add(new MenuNode(tr.getFieldString("webm_desc"), 
						  p_contextPath+(p_contextPath.contains("?")  ? "&" : "?") +"iframeUrl="+URLEncoder.encode(url,StandardCharsets.UTF_8)+agent
						  , "ti ti-typography"));
			  }
			}
		}
	}
	static public List<MenuNode> generateSideMenu(List<MenuNode> p_menu,String p_title,String p_icon,String p_agent,String p_contextPath,SessionHelper sp, String p_rootmenu) {
		SelectUtil su = null;
		String rootMenu="main_main.html";		if(!StringUtils.isBlank(sp.getRootMenu())) {
			rootMenu = sp.getRootMenu();
		}
		List<MenuNode> menu = p_menu == null ? new ArrayList<MenuNode>() : p_menu;
		try {
			su = new SelectUtil();
			su.init(sp.getLoginTokenJdbcPool().getConnection());
			HashSet<String> accessRights = sp.getAccessRights();
			CoreLog.log("create sideMenuJson");
//			TableRec tr = su.getQueryResult(
//					"select distinct webm_seq,webm_url,webm_img,webm_desc,webm_rg"
//							+ " from webmenu,webmenuuser"
//							+ " where webm_menuid = '"+ p_rootmenu +"' and  webmu_mrg = webm_rg and webmu_active = 'Y' and  webmu_user in ("
//							+ "'" + "anyuser" + "','" + sp.getLoginId().trim() + "'" + /*sParent*/ ""
//							+ ") order by 1,4,2,3,5"
//							,null);

//			TableRec tr = su.getQueryResult(
//					"select distinct webm_seq,webm_url,webm_img,webm_desc,webm_rg,webm_menuid"
//							+ " from webmenu,webmenuuser"
//							+ " where webmu_mrg = webm_rg and webmu_active = 'Y' and  webmu_user in ("
//							+ "'" + "anyuser" + "','" + sp.getLoginId().trim() + "'" + /*sParent*/ ""
//							+ ") order by 1,4,2,3,5"
//							,null);			
			TableRec tr = su.getQueryResult(
					"select distinct webm_seq,webm_url,webm_img,webm_desc,webm_rg,webm_menuid"
							+ " from webmenu,webmenuuser"
							+ " where webmu_mrg = webm_rg and webmu_active = 'Y' order by 1,4,2,3,5"
							,null);			
			generateOneMenu(p_contextPath,menu,p_title,p_icon,tr, p_rootmenu,p_agent,sp);
			
//			  // Dashboard
//			  menu.add(new MenuNode("DB(Autogen)", getContextPath() + "/larva/dashboard", "ti ti-dashboard"));
//
//			  // Caption: UI Components
//			  menu.add(MenuNode.caption("UI Components", "ti ti-dashboard"));
//			  menu.add(new MenuNode("Typography", getContextPath()+"/larva/elements/bc_typography", "ti ti-typography"));
//			  menu.add(new MenuNode("Color",      getContextPath()+"/larva/elements/bc_color",      "ti ti-color-swatch"));
//			  menu.add(new MenuNode("Icons",      getContextPath()+"/larva/elements/icon-tabler",   "ti ti-plant-2"));
//
//			  // Caption: Pages
//			  menu.add(MenuNode.caption("Pages", "ti ti-news"));
//			  menu.add(new MenuNode("Login",    getContextPath()+"/larva/pages/login",    "ti ti-lock"));
//			  menu.add(new MenuNode("Register", getContextPath()+"/larva/pages/register", "ti ti-user-plus"));
//
//			  // Caption: Other + nested levels
//			  menu.add(MenuNode.caption("Other", "ti ti-brand-chrome"));
//			  MenuNode levels = new MenuNode("Menu levels", "#!", "ti ti-menu");
//			  MenuNode l21 = new MenuNode("Level 2.1", "#!", null);
//			  MenuNode l22 = new MenuNode("Level 2.2", "#!", null);
//			  l22.add(new MenuNode("Level 3.1", "#!", null))
//			     .add(new MenuNode("Level 3.2", "#!", null))
//			     .add(new MenuNode("Level 3.3", "#!", null)
//			         .add(new MenuNode("Level 4.1", "#!", null))
//			         .add(new MenuNode("Level 4.2", "#!", null)));
//			  levels.add(l21).add(l22).add(new MenuNode("Level 2.3", "#!", null));
//			  menu.add(levels);
//
//			  menu.add(new MenuNode("Sample page", getContextPath()+"/larva/other/sample-page", "ti ti-brand-chrome"));
			
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			if (su != null) su.close();
		}
		return(menu);
	}
	
	static public String getContextPathForAgent(SessionHelper p_sp,String p_agent) {
		return(BiConfig.getString(p_sp, "AgentPath_"+p_agent));
		/*
		if("winecavescp".equals(p_agent)) return("https://hub.erpv4.com/winecavescp/");
		if("erpv4winecave".equals(p_agent)) return("http://192.168.1.204:8080/pmsdemo/");
		if("winecaveold".equals(p_agent)) return("https://hub.erpv4.com/winecaveold/");
		if("winecavedevold".equals(p_agent)) return("http://192.168.1.204:8080/pmsdemo/");
		return(null);
		*/
	}
}
