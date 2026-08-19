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
import com.uniinformation.utils.TableRecException;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;


public class SessionUtil {
	static String getContextPath() {
		return("http://localhost:8080/");
	}
	static public final String SUBMENU_PREFIX="menu.html?menuid=";
	static String getMenuIcon(TableRec p_tr, SessionHelper p_sp) throws TableRecException {
		String icon = StringUtils.trimToEmpty(p_tr.getFieldString("webm_img"));
		if (!p_sp.getAllowMenuColor()) {
			icon = StringUtils.substringBefore(icon, " ");
		}
		if (icon.startsWith("fa-")) {
			icon = "fa " + icon;
		}
		return StringUtils.defaultIfBlank(icon, "ti ti-circle");
	}
	static String addIframeParams(String p_url, SessionHelper p_sp) {
		String url = p_url + (p_url.contains("?") ? "&" : "?") + "theme=geneh&sidemenu=N";
		if (p_sp.getAllowMenuColorSelector()
				&& p_url.startsWith("zkbiloader")
				&& StringUtils.contains(p_url, "viewid=WebMenu")) {
			url += "&load=bsip";
		}
		return url;
	}
	static String resolveMenuAgent(TableRec p_tr, String p_defaultAgent) throws TableRecException {
		return StringUtils.defaultString(StringUtils.defaultIfBlank(
				StringUtils.trimToNull(p_tr.getFieldString("webm_agent")),
				StringUtils.trimToNull(p_defaultAgent)));
	}
	static void generateOneSubmenu(String p_contextPath,MenuNode p_parent,TableRec tr, String p_rootMenu,SessionHelper p_sp,String p_defaultAgent) throws Exception{
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
//			CoreLog.log(tr.getFieldString("webm_desc"));
			if(tr.getFieldString("webm_menuid").equals(p_rootMenu)) {
			  String url = tr.getFieldString("webm_url");
			  if(!p_sp.checkWebMenuAccess(url)) continue;
			  if(url.startsWith(SUBMENU_PREFIX)) {
				  MenuNode subMenu = new MenuNode(tr.getFieldString("webm_desc"), "#!", getMenuIcon(tr,p_sp));
				  String nextRoot = url.substring(SUBMENU_PREFIX.length());
				  generateOneSubmenu(p_contextPath,subMenu ,tr, nextRoot,p_sp,p_defaultAgent);
				  p_parent.add(subMenu);
			  } else {
				  String agent = resolveMenuAgent(tr,p_defaultAgent);
				  url = addIframeParams(url,p_sp);
				  if(!StringUtils.isBlank(agent)) {
					  String agentBaseUrl = getContextPathForAgent(p_sp,agent);
					  if(StringUtils.isBlank(agentBaseUrl)) {
						  UniLog.log1("AgentPath_%s is blank. skip menu item %s", agent, tr.getFieldString("webm_desc"));
						  continue;
					  }
					  url = agentBaseUrl + url;
					  agent="&agent="+URLEncoder.encode(agent,StandardCharsets.UTF_8);
				  }
				  p_parent.add(new MenuNode(tr.getFieldString("webm_desc"),
						  p_contextPath+(p_contextPath.contains("?")  ? "&" : "?") +"iframeUrl="+URLEncoder.encode(url,StandardCharsets.UTF_8)+agent
						  , getMenuIcon(tr,p_sp)));
			  }
			}
		}
	}
	static void generateOneMenu(String p_contextPath,List<MenuNode> p_menu,String p_captionName,String p_captionIcon,TableRec tr, String p_rootMenu,SessionHelper p_sp,String p_defaultAgent) throws Exception{
		if(! (StringUtils.isBlank(p_captionName) || StringUtils.isBlank(p_captionIcon))) {
			  p_menu.add(MenuNode.caption(p_captionName, p_captionIcon));
		}
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
//			CoreLog.log(tr.getFieldString("webm_desc"));
			if(tr.getFieldString("webm_menuid").equals(p_rootMenu)) {
			  String url = tr.getFieldString("webm_url");
			  if(!p_sp.checkWebMenuAccess(url)) continue;
			  if(url.startsWith(SUBMENU_PREFIX)) {
				  MenuNode subMenu = new MenuNode(tr.getFieldString("webm_desc"), "#!", getMenuIcon(tr,p_sp));
				  String nextRoot = url.substring(SUBMENU_PREFIX.length());
				  generateOneSubmenu(p_contextPath,subMenu ,tr, nextRoot,p_sp,p_defaultAgent);
				  p_menu.add(subMenu);
			  } else {
				  String agent = resolveMenuAgent(tr,p_defaultAgent);
				  url = addIframeParams(url,p_sp);
				  if(!StringUtils.isBlank(agent)) {
					  String agentBaseUrl = getContextPathForAgent(p_sp,agent);
					  if(StringUtils.isBlank(agentBaseUrl)) {
						  UniLog.log1("AgentPath_%s is blank. skip menu item %s", agent, tr.getFieldString("webm_desc"));
						  continue;
					  }
					  url = agentBaseUrl + url;
					  agent = "&agent="+URLEncoder.encode(agent,StandardCharsets.UTF_8);
				  }
				  p_menu.add(new MenuNode(tr.getFieldString("webm_desc"),
						  p_contextPath+(p_contextPath.contains("?")  ? "&" : "?") +"iframeUrl="+URLEncoder.encode(url,StandardCharsets.UTF_8)+agent
						  , getMenuIcon(tr,p_sp)));
			  }
			}
		}
	}
	static public List<MenuNode> generateSideMenu(List<MenuNode> p_menu,String p_title,String p_icon,String p_contextPath,SessionHelper sp, String p_rootmenu) {
		return generateSideMenu(p_menu,p_title,p_icon,p_contextPath,sp,p_rootmenu,null);
	}
	static public List<MenuNode> generateSideMenu(List<MenuNode> p_menu,String p_title,String p_icon,String p_contextPath,SessionHelper sp, String p_rootmenu,String p_defaultAgent) {
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
					"select distinct webm_seq,webm_url,webm_img,webm_desc,webm_rg,webm_menuid,webm_agent"
							+ " from webmenu,webmenuuser"
							+ " where webmu_mrg = webm_rg and webmu_active = 'Y' order by 1,4,2,3,5"
							,null);
			generateOneMenu(p_contextPath,menu,p_title,p_icon,tr, p_rootmenu,sp,p_defaultAgent);
			
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

	static boolean isYes(String p_value) {
		return StringUtils.equalsAnyIgnoreCase(StringUtils.trim(p_value), "Y", "YES", "TRUE", "1");
	}

	static boolean allowConfiguredMenu(SessionHelper p_sp,String p_access,boolean p_allowAdmin) {
		String access = StringUtils.trimToNull(p_access);
		return access == null || p_sp.hasAccessRight(access) || (p_allowAdmin && p_sp.isAdminUser());
	}

	/**
	 * Builds the shell sidebar in the order declared by ShellMenuIds.
	 * Configured entries are followed by the shell administration tools.
	 */
	static public List<MenuNode> generateConfiguredSideMenu(String p_contextPath,SessionHelper p_sp) {
		List<MenuNode> menu = new ArrayList<MenuNode>();
		String menuIds = BiConfig.getString(p_sp,"ShellMenuIds");
		if(StringUtils.isBlank(menuIds)) {
			UniLog.log1("ShellMenuIds is blank for agent %s",p_sp.getAgent());
			addShellSystemTool(p_contextPath,menu,p_sp);
			return menu;
		}
		for(String rawId : StringUtils.split(menuIds,',')) {
			String id = StringUtils.trimToNull(rawId);
			if(id == null) continue;
			String prefix = "ShellMenu_" + id + "_";
			String type = StringUtils.trimToEmpty(BiConfig.getString(p_sp,prefix + "Type"));
			String access = BiConfig.getString(p_sp,prefix + "Access");
			boolean allowAdmin = isYes(BiConfig.getString(p_sp,prefix + "AllowAdmin"));
			boolean allowed = allowConfiguredMenu(p_sp,access,allowAdmin);
			if(StringUtils.equalsIgnoreCase(type,"tree")) {
				if(!allowed) continue;
				String rootMenu = StringUtils.trimToNull(BiConfig.getString(p_sp,prefix + "RootMenu"));
				if(rootMenu == null) {
					UniLog.log1("%sRootMenu is blank. skip shell menu %s",prefix,id);
					continue;
				}
				menu = generateSideMenu(menu,
						BiConfig.getString(p_sp,prefix + "Caption"),
						BiConfig.getString(p_sp,prefix + "Icon"),
						p_contextPath,p_sp,rootMenu,
						BiConfig.getString(p_sp,prefix + "Agent"));
			} else if(StringUtils.equalsIgnoreCase(type,"link")) {
				String url = StringUtils.trimToNull(BiConfig.getString(p_sp,prefix + "Url"));
				String text = StringUtils.trimToNull(BiConfig.getString(p_sp,prefix + "Text"));
				if(url == null || text == null) {
					UniLog.log1("%sText or %sUrl is blank. skip shell menu %s",prefix,prefix,id);
					continue;
				}
				if(allowed) {
					menu.add(new MenuNode(text,url,BiConfig.getString(p_sp,prefix + "Icon")));
				}
			} else {
				UniLog.log1("%sType is not tree or link. skip shell menu %s",prefix,id);
			}
		}
		addShellSystemTool(p_contextPath,menu,p_sp);
		return menu;
	}

	static void addShellSystemTool(String p_contextPath,List<MenuNode> p_menu,SessionHelper p_sp) {
		if(p_sp.isAdminUser()) {
			String url = "jxzkloader.html?zul=JxZkSystem.zul";
			p_menu.add(new MenuNode("System Tool",
					p_contextPath+(p_contextPath.contains("?") ? "&" : "?")
							+"iframeUrl="+URLEncoder.encode(url,StandardCharsets.UTF_8),
					"ti ti-tools"));
		}
	}
	
	static public String getContextPathForAgent(SessionHelper p_sp,String p_agent) {
		return(BiConfig.getString(p_sp, "AgentPath_"+p_agent));
	}
}
