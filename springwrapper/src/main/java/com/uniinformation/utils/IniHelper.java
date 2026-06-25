package com.uniinformation.utils;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import com.kikyosoft.config.IniLoader;

import org.apache.commons.configuration2.INIConfiguration;

public class IniHelper extends INIConfiguration{
	static IniLoader loader = null;
	boolean debugFlag = false;
	String agent = null;
	IniHelper parentIni = null;
	public IniHelper(String p_iniFile, String p_resIniFile, String p_agent) throws Exception{
		this(p_iniFile, p_resIniFile, p_agent, false, false);
	}
	public IniHelper(String p_iniFile, String p_resIniFile, String p_agent, boolean p_allowChildIni) throws Exception{
		this(p_iniFile, p_resIniFile, p_agent, p_allowChildIni, false);
	}
	public IniHelper(String p_iniFile, String p_resIniFile, String p_agent, boolean p_allowChildIni, boolean p_debugFlag) throws Exception{
		debugFlag = p_debugFlag;
		UniLog.log1("ini IniHelper iniFile:%s resIniFile:%s agent:%s allowAgentIniFile:%s debug:%s", p_iniFile, p_resIniFile, p_agent, p_allowChildIni, p_debugFlag);
		if (StringUtils.isBlank(p_iniFile) && StringUtils.isBlank(p_resIniFile)) {
			throw new Exception("Both inifile and classloader are null");
		}
		
		if (StringUtils.isBlank(p_agent)) {
			agent = null;
		}
		else {
			agent = p_agent.trim();
		}		
		
		if(p_iniFile != null) {
			loader.load("file:"+p_iniFile, this);
		} else {
			loader.load("classpath:config/"+p_resIniFile, this);
		}
	}
	
	/***
	 * 
	 * @param p_key   - ini key
	 * @param p_defaultValue - if key not found in both agent and global section, return this value
	 * @return
	 */
	public String getString(String p_key, String p_defaultValue){
		String res = null;
		
		//agent section
		if (res == null && agent != null){
			res = getSection(agent).getString(p_key);
		}
		
		//parent agen section
		if (res == null && agent != null) {
			res = getParentString(agent, p_key);
		}
		
		//obtain from this global section
		if (res == null){
			res = getGlobalString(p_key, p_defaultValue);
		}
		
		//substitute special tag
		if (res != null && agent != null) {
			res = res.replaceAll("\\{AGENT\\}", agent);
		}
		
		if (debugFlag){
			UniLog.log(String.format("IniHelper.getString(%s,%s) agent:%s return:%s",p_key, p_defaultValue, agent, res));
		}
		return(res);
	}
	private String getGlobalString(String p_key, String p_defaultValue) {
		String res = null;
		//obtain from this global section
		res = getSection("global").getString(p_key, null);
		
		//obtain from parentIni global section
		if (res == null && parentIni != null) {
			res = parentIni.getSection("global").getString(p_key, null);
		}
		
		//return result
		if (debugFlag) UniLog.log1("key:%s default:%s res:%s", p_key, p_defaultValue, res);
		if (res != null) {
			return res;
		}
		
		//if no match return default
		return p_defaultValue;
	}
	private String getParentString(String p_agent, String p_key) {
		return getParentString(p_agent, p_key, new HashSet<String>());
	}
	/***
	 * 
	 * obtain value from parent section
	 * @param p_agent
	 * @param p_key
	 * @param p_agentHistory - for detect recursive lookup
	 * @return
	 */
	private String getParentString(String p_agent, String p_key, HashSet<String> p_agentHistory){
		String res = null;
		if (debugFlag) UniLog.log1("agent:%s key:%s agentHistory:%s", p_agent, p_key, p_agentHistory.toString());
		
		//validation
		if (p_agent == null || p_key == null) {
			if (debugFlag) UniLog.log1("agent or key is null");
			return null;
		}
		if (p_agentHistory != null && p_agentHistory.contains(p_agent)) {
			UniLog.log1("recursive lookup detected, force abort. agent:%s key:%s", p_agent, p_key);
			return null;
		}
		
		//obtain parent agent from this agent section
		String parentAgent = getSection(p_agent).getString("parentAgent"); 
		
		//obtain parent agent from parentIni agent section
		if (StringUtils.isBlank(parentAgent) && parentIni != null) {
			parentAgent = parentIni.getSection(p_agent).getString("parentAgent");
		}
		
		if (debugFlag) UniLog.log1("parentAgent:%s", parentAgent);
		if (StringUtils.isBlank(parentAgent)) {
			return null;
		}
		
		//get prop from this parent section
		res = getSection(parentAgent).getString(p_key);
		if (res != null) {
			if (debugFlag) UniLog.log1("found prop in curIni. res:%s", res);
			return res;
		}
		
		//get prop from parentIni parent section
		if (parentIni != null) {
			res = parentIni.getSection(parentAgent).getString(p_key);
		}
		if (res != null) {
			if (debugFlag) UniLog.log1("found prop in parentIni. res:%s", res);
			return res;
		}
		
		//if not available, try resursive 
		if (p_agentHistory != null) {
			p_agentHistory.add(p_agent);
		}
		return (getParentString(parentAgent, p_key, p_agentHistory));
	}
	public String getString(String p_key){
		return(getString(p_key, null));
	}
	public Integer getInteger(String p_key, Integer p_defaultValue){
		String resultString = getString(p_key, null);
		if (resultString == null) {
			return p_defaultValue;
		}
		try{
			return (Integer.valueOf(resultString));
		}
		catch(Exception ex){ 
			UniLog.logm(this, "error: key:%s msg:%s",p_key,ex.getMessage());
		}
		return(p_defaultValue);
	}
	public Integer getInteger(String p_key){
		return(getInteger(p_key, null));
	}
	public String getAgent() {
		return agent;
	}
	/***
	 * get all agents in the ini file
	 * @return
	 */
	public List<String> getAgents(){
		Set<String> set = getSections();
		ArrayList<String> agentList;
		if (set == null) {
			agentList = new ArrayList<String>();
		}
		else {
			agentList = new ArrayList<String>(set);
		}
		Collections.sort(agentList);
		return agentList;
	}
	
	public static void setIniLoader(IniLoader p_loader) {
		loader = p_loader;
	}

	public static Properties loadProperty(String p_custPropFile,String p_resourceFile) throws Exception {
		if(p_custPropFile != null) {
			return(loader.loadProperty("file:"+p_custPropFile));
		} else {
			return(loader.loadProperty("classpath:config/"+p_resourceFile));
		}
	}

	public static InputStream getResourceAsStream(ServletContext svc,String p_resource) throws Exception {
		return(loader.getResourceAsStream(p_resource));
	}
}
