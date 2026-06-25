package com.uniinformation.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.kyoko.crypto.SHA256withRSA;
import com.mysql.jdbc.Statement;
import com.uniinformation.webcore.SessionHelper;

//import com.uniinformation.webcore.everbest.SessionHelper;
/***
 * 
 * @author andre
 * 
SUGGESTED DB CONFIG
===================
CREATE TABLE `filing` (
  `fl_key` varchar(100) NOT NULL,
  `fl_version` int(10) NOT NULL,
  `fl_name` varchar(100) DEFAULT NULL,
  `fl_desc` varchar(200) DEFAULT NULL,
  `fl_data` longblob NOT NULL,
  `fl_cts` timestamp NOT NULL DEFAULT 0,
  `fl_uts` timestamp NOT NULL DEFAULT 0,
  `fl_ots` timestamp NOT NULL DEFAULT 0,
  PRIMARY KEY (`fl_key`,`fl_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX idx_filing_cts ON filing(fl_cts);
CREATE INDEX idx_filing_uts ON filing(fl_uts);

my.cnf
innodb_file_per_table


CREATE DATABASE filing;
CREATE USER 'filing'@'%' IDENTIFIED BY 'yourpassword';
GRANT ALL ON filing.* TO 'filing'@'%';

 *
 */

public class FilingUtil {
	public static class FilingUtilPropObj {
		public String filingTabNameDefault = "filing"; //default tabname, tabname provided by method
		public String filingJdbcConnectionClassName = "com.mysql.jdbc.Driver";
		public String filingJdbcConnectionStr = "jdbc:mysql://localhost:3306/filing";
		public String filingJdbcConnectionUser = "user";
		public String filingJdbcConnectionPassword = "password";
		public int filingJdbcConnectionPoolSize = 5;
		public int filingJdbcConnectionMaxPoolSize = JdbcPool.DEFAULT_MAX_CONNECTION_COUNT;
		public JdbcPool jdbcPool = null;
		public boolean enableLock = true;
		@Override
		public String toString(){
			return(String.format("%s,%s,%s", filingJdbcConnectionClassName,filingJdbcConnectionStr,filingJdbcConnectionUser));
		}
	}
	
	public final static int VER_LATEST = -1;
	public final static int VER_ALL = -2;
	public static boolean fDebug = false;
	
	private static HashMap<String, FilingUtilPropObj> propHM = new HashMap<String, FilingUtilPropObj>(); //key: agent + _ + field name e.g. pmsdemo_defaultFilingTabName
	
	//private final static String propertiesFile = "filingutil.properties";
	private final static String defIniFile = "erpsetup.ini";
	private static int bufSize = 65536;
	public static HashMap<String, Object> lockHM = new HashMap<String,Object>();
	private final static long LOCK_TIMEOUT = 60000;
	
	static void initJdbc(FilingUtilPropObj p_propObj, String p_pollName) throws Exception {
		/*
		if (jdbcPool != null){
			return;
		}
		*/
		if(StringUtils.isBlank(p_propObj.filingJdbcConnectionStr)) {
			throw new Exception("FilingUtil ConnectionStr is blank");
		}
		Class.forName(p_propObj.filingJdbcConnectionClassName);
		/*
		try{
			Class.forName(p_propObj.filingJdbcConnectionClassName);
		}
		catch(Exception ex) { 
			ex.printStackTrace(); 
		}
		*/
		JdbcPool jdbcPool = new JdbcPool(p_pollName);
		jdbcPool.checkAllConnections();
		jdbcPool.setConnectionCount(p_propObj.filingJdbcConnectionPoolSize);
		jdbcPool.setMaxConnectionCount(p_propObj.filingJdbcConnectionMaxPoolSize);
		jdbcPool.setConnectionString(p_propObj.filingJdbcConnectionStr, p_propObj.filingJdbcConnectionUser, p_propObj.filingJdbcConnectionPassword);
   		//UniLog.log1("filingjdbc xml:%s", jdbcPool.getRunningStatisticsXML());
		p_propObj.jdbcPool = jdbcPool;
	}
    
	/**
	 * if agent null, load from properties
	 * if agent not null, load from ini
	 * @param p_agent - agent name
	 */
	
    public static FilingUtilPropObj loadProp(String p_agent) throws Exception{
    	synchronized(propHM){
            FilingUtilPropObj propObj = propHM.get(p_agent);
        	if (propObj != null){
        		if (fDebug) UniLog.logm(null, "FilingUtil return cached prop. agent=%s", p_agent);
        		return (propObj);
        	}
    		UniLog.logm(null, "FilingUtil load new prop. agent=%s", p_agent);
            propObj = new FilingUtilPropObj();
            
            //first round load from prop, prefix is option for backward compatibility
            /*
            //andrew190712: filingutil.properties is obsoleted and replaced by erpsetup.ini
    		UniLog.logm(null, "obtain prop from prop " + propertiesFile);
        	Properties prop = new Properties();
        	prop.clear();
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile));
            UniLog.log("FilingUtil load filingutil.properties OK");
        	
	        if (prop.getProperty("tabName") != null && !prop.getProperty("tabName").trim().equals("")){ //without prefix
	        	propObj.filingTabName = prop.getProperty("tabName").trim();
	        }
	        else if (prop.getProperty("filingTabName") != null && !prop.getProperty("filingTabName").trim().equals("")){ //with prefix
	        	propObj.filingTabName = prop.getProperty("filingTabName").trim();
	        }
	        
	        if (prop.getProperty("jdbcConnectionClassName") != null && !prop.getProperty("jdbcConnectionClassName").trim().equals("")){ //without prefix
	        	propObj.filingJdbcConnectionClassName = prop.getProperty("jdbcConnectionClassName").trim();
	        }
	        else if (prop.getProperty("filingJdbcConnectionClassName") != null && !prop.getProperty("filingJdbcConnectionClassName").trim().equals("")){ //with prefix
	        	propObj.filingJdbcConnectionClassName = prop.getProperty("filingJdbcConnectionClassName").trim();
	        }
	        
	        if (prop.getProperty("jdbcConnectionStr") != null && !prop.getProperty("jdbcConnectionStr").trim().equals("")){  //without prefix
	        	propObj.filingJdbcConnectionStr = prop.getProperty("jdbcConnectionStr").trim();
	        }
	        else if (prop.getProperty("filingJdbcConnectionStr") != null && !prop.getProperty("filingJdbcConnectionStr").trim().equals("")){ //with prefix
	        	propObj.filingJdbcConnectionStr = prop.getProperty("filingJdbcConnectionStr").trim();
	        }
	        
	        if (prop.getProperty("jdbcConnectionUser") != null && !prop.getProperty("jdbcConnectionUser").trim().equals("")){ //without prefix
	        	propObj.filingJdbcConnectionUser = prop.getProperty("jdbcConnectionUser").trim();
	        }
	        else if (prop.getProperty("filingJdbcConnectionUser") != null && !prop.getProperty("filingJdbcConnectionUser").trim().equals("")){  //withprefix
	        	propObj.filingJdbcConnectionUser = prop.getProperty("filingJdbcConnectionUser").trim();
	        }
        	
	        if (prop.getProperty("jdbcConnectionPassword") != null && !prop.getProperty("jdbcConnectionPassword").trim().equals("")){ //without prefix
	        	propObj.filingJdbcConnectionPassword = prop.getProperty("jdbcConnectionPassword");
	        }
	        else if (prop.getProperty("filingJdbcConnectionPassword") != null && !prop.getProperty("filingJdbcConnectionPassword").trim().equals("")){ //with prefix
	        	propObj.filingJdbcConnectionPassword = prop.getProperty("filingJdbcConnectionPassword");
	        }
	        
	        if (prop.getProperty("jdbcConnectionPoolSize") != null && !prop.getProperty("jdbcConnectionPoolSize").trim().equals("")){  //without prefix
	        	try{
	        		propObj.filingJdbcConnectionPoolSize = Integer.parseInt(prop.getProperty("jdbcConnectionPoolSize"));  
	        	}
	        	catch(Exception ex){
	        		ex.printStackTrace();
	        	}
	        }
	        else if (prop.getProperty("filingJdbcConnectionPoolSize") != null && !prop.getProperty("filingJdbcConnectionPoolSize").trim().equals("")){  //with prefix
	        	try{
	        		propObj.filingJdbcConnectionPoolSize = Integer.parseInt(prop.getProperty("filingJdbcConnectionPoolSize"));
	        	}
	        	catch(Exception ex){
	        		ex.printStackTrace();
	        	}
	        }
	        */
	        
	        
	        //second round, load from ini, if any
	        if (p_agent != null){
				/*
        		IniHelper ini = null;
				String iniFile = MapUtil.getString(SessionHelper.getDefPropMap(), "iniFile", null);
				if (StringUtils.isNotBlank(iniFile)){
					UniLog.log1("use custom iniFile:%s agent:%s", iniFile, p_agent);
					ini = new IniHelper(new FileReader(new File(iniFile)), p_agent);
				}
				else{
					UniLog.log1("use default iniFile:%s agent:%s", defIniFile, p_agent);
					ini = new IniHelper(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(defIniFile)), p_agent);
				}
				*/
        		IniHelper ini = SessionHelper.getIniHelper(p_agent);
					
	        	
	        	propObj.filingTabNameDefault = ini.getString("filingTabName", propObj.filingTabNameDefault);
	        	propObj.filingJdbcConnectionClassName = ini.getString("filingJdbcConnectionClassName", propObj.filingJdbcConnectionClassName);
	        	propObj.filingJdbcConnectionStr = ini.getString("filingJdbcConnectionStr",propObj.filingJdbcConnectionStr);
	        	propObj.filingJdbcConnectionUser = ini.getString("filingJdbcConnectionUser",propObj.filingJdbcConnectionUser);
	        	propObj.filingJdbcConnectionPassword = ini.getString("filingJdbcConnectionPassword", propObj.filingJdbcConnectionPassword);
	        	propObj.filingJdbcConnectionPoolSize = ini.getInteger("filingJdbcConnectionPoolSize", propObj.filingJdbcConnectionPoolSize);
	        	propObj.filingJdbcConnectionMaxPoolSize = ini.getInteger("filingJdbcConnectionMaxPoolSize", propObj.filingJdbcConnectionMaxPoolSize);
	        	propObj.enableLock = ini.getString("filingEnableLock","Y").endsWith("Y");
        	}
	        initJdbc(propObj, StringUtils.isBlank(p_agent) ? "filing" : "filing@" + p_agent);
            propHM.put(p_agent, propObj); //remark agent allow null
	        return(propObj);
    	}
    }
    
	public static FilingUtilObject storeFile(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, InputStream p_is) throws Exception{
		return storeFile(p_agent, p_tabName, p_key, p_name, p_desc, p_is, false, null);
	}
	public static FilingUtilObject storeFile(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, InputStream p_is,Date p_objTimestamp) throws Exception{
		return storeFile(p_agent, p_tabName, p_key, p_name, p_desc, p_is, false, p_objTimestamp);
	}
	/*
	//andrew190730: obsoleted, remove it later
	public static void storeFile(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, InputStream p_is, boolean p_keepVersion) throws Exception{
		storeFile(p_agent, p_tabName, p_key, p_name, p_desc, p_is, p_keepVersion, null);
	}
	*/
	private static void haha4() throws Exception {
    	FilingUtilPropObj propObj = loadProp("bischemamysql");
		Connection conn = null;
        Timestamp cts;
		java.util.Date jd;
		TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		conn = propObj.jdbcPool.getConnection();
//			String insertSQL = String.format("INSERT INTO multidoc(mdoc_mrg, fl_key, fl_desc, fl_version, fl_data, fl_cts, fl_uts, fl_ots) VALUES(?,?,?,?,?,?,?,?)",tabName);
			String insertSQL = String.format("INSERT INTO multidoc(mdoc_ctime) VALUES(?)");
//			PreparedStatement pstmt = conn.prepareStatement(insertSQL);
			/*
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
			*/
			/*
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
//            cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
			/*
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */

            /*
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
			SelectUtil su = new SelectUtil();
			su.setConnection(conn);
			su.executeUpdate("insert into multidoc(mdoc_ctime) values (?)", 
					new Wherecl().appendArgument(cts)
					);
			/*
            
			pstmt = conn.prepareStatement(insertSQL);
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
	}
	private static void haha2() throws Exception {
    	FilingUtilPropObj propObj = loadProp("bischemamysql");
		Connection conn = null;
        Timestamp cts;
		java.util.Date jd;
//		TimeZone tz = TimeZone.getDefault();
//		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Asia/Hong_Kong"));
//		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
		conn = propObj.jdbcPool.getConnection();
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
			SelectUtil su = new SelectUtil();
			su.setConnection(conn);
			su.executeUpdate("insert into multidoc(mdoc_ctime) values (?)", 
					new Wherecl().appendArgument(cts)
					);
	}
	private static void haha1() throws Exception {
    	FilingUtilPropObj propObj = loadProp("bischemamysql");
		Connection conn = null;
        Timestamp cts;
		java.util.Date jd;
		TimeZone tz = TimeZone.getDefault();
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Asia/Hong_Kong"));
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
		conn = propObj.jdbcPool.getConnection();
//			String insertSQL = String.format("INSERT INTO multidoc(mdoc_mrg, fl_key, fl_desc, fl_version, fl_data, fl_cts, fl_uts, fl_ots) VALUES(?,?,?,?,?,?,?,?)",tabName);
			String insertSQL = String.format("INSERT INTO multidoc(mdoc_ctime) VALUES(?)");
			PreparedStatement pstmt = conn.prepareStatement(insertSQL);
			/*
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
			*/
			/*
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
//            cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();

            /*
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
            /*
			SelectUtil su = new SelectUtil();
			su.setConnection(conn);
			su.executeUpdate("insert into multidoc(mdoc_ctime) values (?)", 
					new Wherecl().appendArgument(cts)
					);
					*/
            
			pstmt = conn.prepareStatement(insertSQL);
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
			
	}
	private static void haha() throws Exception {
    	FilingUtilPropObj propObj = loadProp("bischemamysql");
		Connection conn = null;
        Timestamp cts;
		java.util.Date jd;
		TimeZone tz = TimeZone.getDefault();
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Asia/Hong_Kong"));
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
		conn = propObj.jdbcPool.getConnection();
//			String insertSQL = String.format("INSERT INTO multidoc(mdoc_mrg, fl_key, fl_desc, fl_version, fl_data, fl_cts, fl_uts, fl_ots) VALUES(?,?,?,?,?,?,?,?)",tabName);
			String insertSQL = String.format("INSERT INTO multidoc(mdoc_ctime) VALUES(?)");
			PreparedStatement pstmt = conn.prepareStatement(insertSQL);
			/*
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
			*/
			/*
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
//            cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();

            /*
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
            /*
			SelectUtil su = new SelectUtil();
			su.setConnection(conn);
			su.executeUpdate("insert into multidoc(mdoc_ctime) values (?)", 
					new Wherecl().appendArgument(cts)
					);
					*/
            
			pstmt = conn.prepareStatement(insertSQL);
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
			
	}
	
	private static void haha3() throws Exception {
    	FilingUtilPropObj propObj = loadProp("bischemamysql");
		Connection conn = null;
        Timestamp cts;
		java.util.Date jd;
		TimeZone tz = TimeZone.getDefault();
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("Asia/Hong_Kong"));
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
		conn = propObj.jdbcPool.getConnection();
//			String insertSQL = String.format("INSERT INTO multidoc(mdoc_mrg, fl_key, fl_desc, fl_version, fl_data, fl_cts, fl_uts, fl_ots) VALUES(?,?,?,?,?,?,?,?)",tabName);
			String insertSQL = String.format("INSERT INTO multidoc(mdoc_ctime) VALUES(?)");
			PreparedStatement pstmt = conn.prepareStatement(insertSQL);
			/*
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
			*/
			/*
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
			/*
			jd = new java.util.Date();
			cts = Timestamp.from(Instant.now());
            pstmt.setTimestamp(1, cts); //version starting from 1
            */
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
//            cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
			jd = new java.util.Date();
			cts = Timestamp.from(Instant.now());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();

            /*
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
			cts = new Timestamp(jd.getTime());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
            */
            /*
			SelectUtil su = new SelectUtil();
			su.setConnection(conn);
			su.executeUpdate("insert into multidoc(mdoc_ctime) values (?)", 
					new Wherecl().appendArgument(cts)
					);
					*/
            
			pstmt = conn.prepareStatement(insertSQL);
            TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
			jd = new java.util.Date();
//			cts = new Timestamp(jd.getTime());
			cts = Timestamp.from(Instant.now());
            pstmt.setTimestamp(1, cts); //version starting from 1
            pstmt.executeUpdate();
			
	}
	
	
	private static FilingUtilObject storeFile(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, InputStream p_is, boolean p_keepVersion, Date p_objTimestamp) throws Exception{
		if (fDebug) UniLog.logm(null, "start");
    	FilingUtilPropObj propObj = loadProp(p_agent);
		UniLog.logm(null, "storeFile key:%s agent:%s tab:%s propObj:%s", p_key, p_agent, p_tabName, propObj);
    	String tabName = propObj.filingTabNameDefault;
    	if (p_tabName != null){
    		tabName = p_tabName;
    	}
		Connection conn = null;
		UniLog.log("storeFile:"+p_key);
		try{
			if (p_key == null) {
				if(!(p_is instanceof DigestInputStream)) {
					throw new Exception ("Key is null");
				}
			}
			if (p_key != null && propObj.enableLock) acquireLock(p_agent, p_tabName, p_key);
			conn = propObj.jdbcPool.getConnection();
			conn.setAutoCommit(false);
			
			int version = 1;
			if (p_keepVersion){
				//get max version id
			    String selectRowSQL = String.format("SELECT max(fl_version) FROM %s WHERE fl_key = ?",tabName);
		        PreparedStatement pstmt = conn.prepareStatement(selectRowSQL);
		        pstmt.setString(1, p_key);
				ResultSet rs = pstmt.executeQuery();	
				if (rs.next()){
					int maxVersion = rs.getInt(1);
					if (maxVersion > 0){
						version = maxVersion + 1;
						UniLog.log("set version to:"+ version);
					}
				}
			}
			else{
				//delete old files
				if(p_key != null) {
					PreparedStatement pstmt = conn.prepareStatement(String.format("DELETE FROM %s WHERE fl_key = ?",tabName));
					pstmt.setString(1, p_key);
					int deletedCnt = pstmt.executeUpdate();	
					UniLog.log("delete old file, affected:"+deletedCnt);
				}
			}
			
			
			//insert
			String insertSQL = String.format("INSERT INTO %s(fl_name, fl_key, fl_desc, fl_version, fl_data, fl_cts, fl_uts, fl_ots) VALUES(?,?,?,?,?,?,?,?)",tabName);
			PreparedStatement pstmt ;
			if(p_key != null) {
				pstmt = conn.prepareStatement(insertSQL);
			} else {
				pstmt = conn.prepareStatement(insertSQL,Statement.RETURN_GENERATED_KEYS);
			}
            pstmt.setString(1, p_name);
            pstmt.setString(2, p_key == null ? "" : p_key);
            pstmt.setString(3, p_desc);
            pstmt.setInt(4, version); //version starting from 1
            pstmt.setBinaryStream(5, p_is);
            Date currentDate = new Date();
            Timestamp cts = new Timestamp(currentDate.getTime());
            Timestamp uts = cts;
            Timestamp ots = p_objTimestamp == null ? new Timestamp(8640000L) : new Timestamp(p_objTimestamp.getTime()); //why set to 864000
            pstmt.setTimestamp(6, cts);
            pstmt.setTimestamp(7, uts);
            pstmt.setTimestamp(8, ots);
            int r = pstmt.executeUpdate();

            if(p_key == null) {
            	if(r != 1) {
            		throw new Exception ("No rows insertedf");
            	}
            	 try (ResultSet rs = pstmt.getGeneratedKeys()) {
            	        if (rs.next()) {
            	            long id = rs.getLong(1);   // 🔹 the auto-increment / identity value
            	            System.out.println("New ID: " + id);
            	            String checkSum = SHA256withRSA.bytesToBase64URL(((DigestInputStream) p_is).getMessageDigest().digest());
            	            System.out.println("New ID: " + id + " checksum " + checkSum);
            	            String checkSQL =  String.format("select serial_id from %s where fl_key = ? ",tabName);
            	            PreparedStatement qstmt = conn.prepareStatement(checkSQL);
            	            qstmt.setString(1, checkSum);
            	            ResultSet rs2 = qstmt.executeQuery();	
            	            if (rs2.next()){
            	            	/* record of same checksum already exist */
            	            	conn.rollback();
            	            } else {
            	            	String updateSQL = String.format("UPDATE %s set fl_key = ? where serial_id = ?",tabName);
            	            	PreparedStatement ustmt = conn.prepareStatement(updateSQL);
            	            	ustmt.setString(1, checkSum);
            	            	ustmt.setLong(2, id);
            	            	r = ustmt.executeUpdate();
            	            	conn.commit();
            	            }
            	            FilingUtilObject fuo = new FilingUtilObject(checkSum, version, p_name, p_desc, cts, uts, ots, 0);
            	            return(fuo);
            	        } else {
            	            throw new Exception("Insert succeeded but no ID obtained.");
            	        }
            	}
            } else {
            	conn.commit();
            	UniLog.log("insert ok");
            	FilingUtilObject fuo = new FilingUtilObject(p_key , version, p_name, p_desc, cts, uts, ots, 0);
            	return(fuo);
            }
		}
		catch(Exception ex){
			try{
				if (conn != null){
					conn.rollback();
				}
			} catch (Exception ex2){ ex2.printStackTrace(); }
			throw(ex);
		}
		finally{
			if (p_key != null && propObj.enableLock) releaseLock(p_agent, p_tabName, p_key);
			try{
				if (conn != null) conn.close();
				conn = null;
			} catch (Exception ex){ ex.printStackTrace(); }
		}
	}
	
	/***
	 * get file, latest version
	 * @param p_agent
	 * @param p_tabName
	 * @param p_key
	 * @param p_os
	 * @return
	 * @throws Exception
	 */
	public static FilingUtilObject getFile(String p_agent, String p_tabName, String p_key, OutputStream p_os) throws Exception{
		return(getFile(p_agent, p_tabName, p_key, VER_LATEST, p_os));
	}
	
	/***
	 * get file, specific version.
	 * @param p_agent
	 * @param p_tabName
	 * @param p_key
	 * @param p_version
	 * @param p_os
	 * @return
	 * @throws Exception
	 */
	private static FilingUtilObject getFile(String p_agent, String p_tabName, String p_key, int p_version, OutputStream p_os) throws Exception{
		ArrayList<FilingUtilObject> resultFiles = getFiles(p_agent, p_tabName, p_key, p_version, p_os);
		if (resultFiles.size() == 0){
			return(null);
		}
		else{
			return(resultFiles.get(0));
		}
	}
	
	/***
	 * retrieve all versions meta data only, without binary data, latest version first
	 * @param p_key
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<FilingUtilObject> getFiles(String p_agent, String p_tabName, String p_key) throws Exception{
		return(getFiles(p_agent, p_tabName, p_key, VER_ALL, null));
	}
	public static ArrayList<FilingUtilObject> getFiles(String p_agent, String p_tabName, String p_key,int p_version) throws Exception{
		return(getFiles(p_agent, p_tabName, p_key, p_version, null));
	}
	
	/**
	 * @param p_key - file key
	 * @param p_version - VER_LATEST:return latest version, VER_ALL:return all version
	 * @param p_os - if p_os = null or p_version = VER_ALL, do not download binary data from db
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<FilingUtilObject> getFiles(String p_agent, String p_tabName, String p_key, int p_version, OutputStream p_os) throws Exception{
		if (fDebug) UniLog.logm(null, "start");
    	FilingUtilPropObj propObj = loadProp(p_agent);
		if (fDebug) UniLog.log1("key:%s version:%d agent:%s tab:%s propObj:%s", p_key, p_version, p_agent, p_tabName, propObj);
		ArrayList<FilingUtilObject> resultFiles = new ArrayList<FilingUtilObject>();
    	String tabName = propObj.filingTabNameDefault;
    	if (p_tabName != null){
    		tabName = p_tabName;
    	}
		Connection conn = null;
		if (p_key == null || p_key.trim().equals("")){
			return (null);
		}
		try{
			if (propObj.enableLock) acquireLock(p_agent, p_tabName, p_key);
			conn = propObj.jdbcPool.getConnection();
			conn.setAutoCommit(true);
			String sqlDataLen = "length(fl_data) as fl_data_length";
			if (propObj.filingJdbcConnectionClassName.contains("microsoft")){
				sqlDataLen = "len(fl_data) as fl_data_length";
			}
			String selectSQL = null;
			String sqlData = "";
			if (p_os != null){
				sqlData = "fl_data,";
			}
        	
        	PreparedStatement pstmt;
	        if (p_version == VER_LATEST || p_version == VER_ALL){
	        	selectSQL = String.format("SELECT fl_key, fl_version, fl_name, fl_desc, fl_cts, fl_uts, fl_ots, %s %s FROM %s WHERE fl_key = ? ORDER BY fl_version DESC", sqlData, sqlDataLen, tabName);
	        	pstmt = conn.prepareStatement(selectSQL) ;
	        	pstmt.setString(1, p_key);
	        }
	        else{
	        	selectSQL = String.format("SELECT fl_key, fl_version, fl_name, fl_desc, fl_cts, fl_uts, fl_ots, %s %s FROM %s WHERE fl_key = ? AND fl_version = ?", sqlData, sqlDataLen, tabName);
	        	pstmt = conn.prepareStatement(selectSQL) ;
	        	pstmt.setString(1, p_key);
	        	pstmt.setInt(2, p_version);
	        }
	        if (fDebug) UniLog.log("selectSQL:"+pstmt.toString());
	        
			ResultSet rs = pstmt.executeQuery();	
			while (rs.next()) { //only return the first record
	            if (p_version != VER_ALL && p_os != null){
		            InputStream input = rs.getBinaryStream("fl_data");
		            byte[] buffer = new byte[bufSize];
		            int len;
		            while ((len = input.read(buffer)) >= 0) {
		            	p_os.write(buffer,0,len);
		            }
	            }
		        FilingUtilObject fuo = new FilingUtilObject(
		        		rs.getString("fl_key"), 
		        		rs.getInt("fl_version"),
		        		rs.getString("fl_name"),
		        		rs.getString("fl_desc"),
		        		rs.getTimestamp("fl_cts"),
		        		rs.getTimestamp("fl_uts"),
		        		rs.getTimestamp("fl_ots"),
		        		rs.getLong("fl_data_length"));
		       	
		       	resultFiles.add(fuo);
		       	if (p_version != VER_ALL){
		       		break;
		       	}
	        }
	       	return(resultFiles);
		}
		catch(Exception ex){
			throw (ex);
		}
		finally{
			if (propObj.enableLock) releaseLock(p_agent, p_tabName, p_key);
			try{
				if (conn != null) conn.close();
				conn = null;
			} catch (Exception ex){}
		}
	}
	/***
	 * get json object
	 * @param p_agent
	 * @param p_tabName
	 * @param p_key
	 * @return
	 */
	public static JSONObject getJson(String p_agent, String p_tabName, String p_key) throws Exception{
		try{
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			FilingUtilObject fuo = FilingUtil.getFile(p_agent, p_tabName, p_key, byteOutStream);
			if (byteOutStream != null){
				byteOutStream.close();
			}
			if(fuo != null) {
				UniLog.logm(null, "json object found %s", p_key);
				String jString = new String(byteOutStream.toByteArray(),"UTF-8");
				JSONObject jObj = new JSONObject(jString);
				return(jObj);
			}
			else{
				UniLog.logm(null, "filing object not found %s", p_key);
				return(null);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}

	public static JSONArray getJsonArray(String p_agent, String p_tabName, String p_key) throws Exception{
		try{
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			FilingUtilObject fuo = FilingUtil.getFile(p_agent, p_tabName, p_key, byteOutStream);
			if (byteOutStream != null){
				byteOutStream.close();
			}
			if(fuo != null) {
				UniLog.logm(null, "json array found %s", p_key);
				String jString = new String(byteOutStream.toByteArray(),"UTF-8");
				JSONArray jArr = new JSONArray(jString);
				return(jArr);
			}
			else{
				UniLog.logm(null, "filing array not found %s", p_key);
				return(null);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}
	/***
	 * get gson object
	 * @param p_agent
	 * @param p_tabName
	 * @param p_key
	 * @return return null if record not found
	 * @throws Exception
	 */
	public static JsonObject getGson(String p_agent, String p_tabName, String p_key) throws Exception {
		return GsonUtil.convertToJsonObject(FilingUtil.getJson(p_agent, p_tabName, p_key));
	}
	/***
	 * add / overwrite json content
	 * It will overwrite old json
	 * @param p_agent
	 * @param p_tabName
	 * @param p_key
	 * @param p_name
	 * @param p_desc
	 * @param p_json
	 * @return
	 */
	public static ReturnMsg storeJsonArray(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, JSONArray p_json) throws Exception {
		if (p_json == null) {
			UniLog.log1("json is null, ignore save");
			return new ReturnMsg(false, "json is null");
		}
		try {
			InputStream byteInStream = new ByteArrayInputStream(p_json.toString().getBytes("UTF-8"));
			FilingUtil.storeFile(p_agent, p_tabName, p_key, p_name, p_desc, byteInStream);
			byteInStream.close();
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	public static ReturnMsg storeJson(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, JSONObject p_json) throws Exception {
		if (p_json == null) {
			UniLog.log1("json is null, ignore save");
			return new ReturnMsg(false, "json is null");
		}
		try {
			InputStream byteInStream = new ByteArrayInputStream(p_json.toString().getBytes("UTF-8"));
			FilingUtil.storeFile(p_agent, p_tabName, p_key, p_name, p_desc, byteInStream);
			byteInStream.close();
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	/***
	 * add / overwrite gson 
	 * @param p_agent
	 * @param p_tabName
	 * @param p_key
	 * @param p_name
	 * @param p_desc
	 * @param p_gson
	 * @throws Exception
	 */
	public static ReturnMsg storeGson(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, JsonObject p_gson) throws Exception {
		return storeJson(p_agent, p_tabName, p_key, p_name, p_desc, GsonUtil.convertToJSONObject(p_gson));
	}
	/***
	 * update json content
	 * @param p_agent
	 * @param p_tabName
	 * @param p_key
	 * @param p_name
	 * @param p_desc
	 * @param p_json - new json object
	 * @throws Exception
	 */
	public static ReturnMsg updateJson(String p_agent, String p_tabName, String p_key, String p_name, String p_desc, JSONObject p_inJson) throws Exception {
		if (p_inJson == null || p_inJson.length() <= 0) {
			UniLog.log1("json is blank, ignore update");
			return new ReturnMsg(false,"json is blank");
		}
		try {
			//get a copy of org json
			JSONObject newJson = getJson(p_agent, p_tabName, p_key);
			
			if (newJson == null) {
				//no org json
				return storeJson(p_agent, p_tabName, p_key, p_name, p_desc, p_inJson);
			}
			
			//merge orgJson and inJson
			List<String> inJsonKeys = IteratorUtils.toList(p_inJson.keys());
			for (String key : inJsonKeys) {
				newJson.put(key, p_inJson.get(key));
			}
			return storeJson(p_agent, p_tabName, p_key, p_name, p_desc, newJson);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	
	public static int deleteFile(String p_agent, String p_tabName, String p_key) throws Exception{
		return(deleteFile(p_agent, p_tabName, p_key, VER_ALL));
	}
	/***
	 * 
	 * @param p_key
	 * @param p_version VER_ALL - delete all version
	 * @return
	 * @throws Exception
	 */
	public static int deleteFile(String p_agent, String p_tabName, String p_key, int p_version) throws Exception{
		
		if (fDebug) UniLog.logm(null, "start");
    	FilingUtilPropObj propObj = loadProp(p_agent);
		UniLog.log1("deleteFile key:%s version:%d agent:%s tab:%s propObj:%s", p_key, p_version, p_agent, p_tabName, propObj);
    	String tabName = propObj.filingTabNameDefault;
    	if (p_tabName != null){
    		tabName = p_tabName;
    	}
		Connection conn = null;
		if (p_key == null || p_key.trim().equals("")){
			UniLog.log1("key is blank");
			return 0;
		}
		if (p_version == VER_LATEST){
			UniLog.log1("not yet implement delete latest");
			return 0;
		}
		
		try{
			if (propObj.enableLock) acquireLock(p_agent, p_tabName, p_key);
			conn = propObj.jdbcPool.getConnection();
			conn.setAutoCommit(true);
			
			//delete old files
			PreparedStatement pstmt;
			if (p_version == VER_ALL){
				pstmt = conn.prepareStatement(String.format("DELETE FROM %s WHERE fl_key = ?",tabName));
				pstmt.setString(1, p_key);
			}
			else{
				pstmt = conn.prepareStatement(String.format("DELETE FROM %s WHERE fl_key = ? AND fl_version = ?",tabName));
				pstmt.setString(1, p_key);
				pstmt.setInt(2, p_version);
			}
			int deletedCnt = pstmt.executeUpdate();	
			UniLog.log("delete old file, affected:"+deletedCnt);
			return(deletedCnt);
		}
		catch(Exception ex){
			throw (ex);
		}
		finally{
			if (propObj.enableLock) releaseLock(p_agent, p_tabName, p_key);
			try{
				if (conn != null) conn.close();
				conn = null;
			} catch (Exception ex){}
		}
	}
	public static void main(String args[]) throws Exception{
		haha2();
		/*
		FilingUtil.storeFile(null, "key1", "name1", "����r", new FileInputStream("/tmp/1.jpg"));
		//basic save/load test
		FilingUtil.storeFile(null, "key1", "name1", "����r", new FileInputStream("/tmp/1.jpg"));
		FilingUtil.getFile(null,"key1", new FileOutputStream("/tmp/1_out.jpg")).toString();
		*/
		
		/*
		//large file test
		FilingUtil.storeFile(null, "key2", "name2", "desc2a", new FileInputStream("/tmp/schema.xml"));
		FilingUtil.storeFile(null, "xstream-distribution-1.4.9-bin.zip", "xstream-distribution-1.4.9-bin.zip", "no description", new FileInputStream("/tmp/xstream-distribution-1.4.9-bin.zip"));
		FilingUtil.getFile(null, "xstream-distribution-1.4.9-bin.zip", null).toString();
		FilingUtil.storeFile(null, "zkweb_image_customer.jpg", "customer.jpg", "image file", new FileInputStream("/eclipse_dev/pmsdemo/WebContent/images/customer.jpg"));
		*/
		
		/*
		//multi version test
		FilingUtil.storeFile(null, "key1",  "name1", "desc1", new FileInputStream("/tmp/1.jpg"),false, new java.util.Date());
		FilingUtil.storeFile(null, "key1",  "name1", "desc1 newer 1", new FileInputStream("/tmp/1.jpg"),true, new java.util.Date());
		FilingUtil.storeFile(null, "key1",  "name1", "desc1 newer 2", new FileInputStream("/tmp/1.jpg"),true, new java.util.Date());
		FilingUtil.deleteFile(null, "key1");
		UniLog.log("getFile result:"+ FilingUtil.getFile(null, "key1", new FileOutputStream("/tmp/key1.out")).toString());
		UniLog.log("getFiles result:"+ FilingUtil.getFiles(null, "key1"));
		*/
		
		/*
		//test agent
		try{
			FilingUtil.getFile("abc", null,"key1", new FileOutputStream("/tmp/1c.jpg")).toString();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		UniLog.log("exit");
		System.exit(0);
		*/
		
		/*
		//get filing key by pattern
		for (String key : FilingUtil.getKeys("smartac", "zkbi_querypreset_BiTranslate\\_custom\\_%")) {
			UniLog.log1("key:%s",key);
		}
		System.exit(0);
		*/
		
	}
	private static void acquireLock(String p_agent, String p_tabName, String p_key) throws Exception{
		String lockUID = buildLockUID(p_agent,p_tabName,p_key);
		StopWatch sw = new StopWatch();
		sw.start();
		for(;;){
			synchronized(lockHM){
				if (lockHM.get(lockUID) == null){
					if (fDebug) UniLog.logm(null, "lock ok %s",lockUID);
					lockHM.put(lockUID, new Object());
					return;
				}
			}
			if (sw.getTime() > LOCK_TIMEOUT){
				UniLog.logm(null,"acquireLock timeout %d", sw.getTime());
				throw new Exception("acquireLock timeout");
			}
			UniLog.logm(null, "locked by other thread, waiting %s",lockUID);
			Thread.sleep(RandomUtils.nextLong(50, 300));
		}
	}
	private static void releaseLock(String p_agent, String p_tabName, String p_key) throws Exception{
		String lockUID = buildLockUID(p_agent,p_tabName,p_key);
		synchronized(lockHM){
			if (fDebug) UniLog.logm(null, "release lock ok %s",lockUID);
			lockHM.remove(lockUID);
		}
	}
	private static String buildLockUID(String p_agent, String p_tabName, String p_key){
		return("AGENT:"+p_agent +" TAB:"+ p_tabName +" KEY:" + p_key);
	}
	/***
	 * quick method for guess mimeType and file extension
	 * @param p_bytes
	 * @return result map. if failure, return blank/default value 
	 */
	public static Map<String,String> guessFileType(byte[] p_bytes){
		if (p_bytes == null || p_bytes.length == 0){
			UniLog.log1("byte array is empty");
			return MapUtil.of("mimeType", "application/octet-stream", "ext", "");
		}
		try{
			InputStream is = new BufferedInputStream(new ByteArrayInputStream(p_bytes));
			String mimeType = URLConnection.guessContentTypeFromStream(is);
			UniLog.log1("mimeType:%s", mimeType);
			if (StringUtils.isBlank(mimeType)){
				mimeType = "application/octet-stream";
			}
			String ext = guessFileExt(mimeType);
			return MapUtil.of("mimeType", mimeType,"ext", ext);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return MapUtil.of("mimeType", "application/octet-stream", "ext", "");
		}
	}
	/***
	 * quick method for guess mimeType and file extension
	 * @param p_file
	 * @return result map. if failure, return blank/default value 
	 */
	public static Map<String,String> guessFileType(File p_file){
		try{
			String mimeType = new MimetypesFileTypeMap().getContentType(p_file);;
			if (StringUtils.isBlank(mimeType)){
				mimeType = "application/octet-stream";
			}
			String ext = guessFileExt(mimeType);
			return MapUtil.of("mimeType", mimeType,"ext", ext);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return MapUtil.of("mimeType", "application/octet-stream", "ext", "");
		}
	}
	/***
	 * quick method for guess mimeType and file extension
	 * @param p_fileName
	 * @return
	 */
	public static Map<String,String> guessFileType(String p_fileName){
		String mimeType = URLConnection.guessContentTypeFromName(p_fileName);
		if (StringUtils.isBlank(mimeType)){
			mimeType = "application/octet-stream";
		}
		String ext = FilenameUtils.getExtension(p_fileName);
		return MapUtil.of("mimeType",mimeType, "ext", ext);
	}
	
	/***
	 * get file extension from mime type
	 * @param p_mimeType
	 * @return
	 */
	public static String guessFileExt(String p_mimeType){
		String ext = "";
		if (StringUtils.equalsAnyIgnoreCase(p_mimeType, "image/jpeg")){
			ext = ".jpg";
		}
		else if (StringUtils.equalsAnyIgnoreCase(p_mimeType, "image/png")){
			ext = ".png";
		}
		else if (StringUtils.equalsAnyIgnoreCase(p_mimeType, "image/gif")){
			ext = ".gif";
		}
		else if (StringUtils.equalsAnyIgnoreCase(p_mimeType, "application/pdf")){
			ext = ".pdf";
		} else {
			String ss = MimeTypes.getDefaultExt(p_mimeType);
			if(ss != null) ext = "."+ss;
		}
		return ext;
	}

	/***
	 * get filing key list by pattern
	 * 
	 * pattern remark:
	 * % match any char
	 * _ match single char
	 * If you need to search for underscore, you need to esc it by blackslahes.
	 * 
	 * @param p_agent
	 * @param p_keyPattern
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getKeys(String p_agent, String p_keyPattern) throws Exception{
		return(getKeys(p_agent, p_keyPattern,null));
	}
	public static ArrayList<String> getKeys(String p_agent, String p_keyPattern,String filingTab) throws Exception{
    	FilingUtilPropObj propObj = loadProp(p_agent);
		if (fDebug) UniLog.log1("keyPattern:%s agent:%s", p_keyPattern, p_agent);
    	String tabName = (filingTab == null ? propObj.filingTabNameDefault : filingTab);
    	
		Connection conn = null;
		ArrayList<String> resultList = new ArrayList<String>();
		if (StringUtils.isBlank(p_keyPattern)) {
			if (fDebug) UniLog.log1("keypattern is blank");
	       	return(resultList);
		}
		try{
			conn = propObj.jdbcPool.getConnection();
			conn.setAutoCommit(true);
			String selectSQL = null;
        	
        	PreparedStatement pstmt;
        	selectSQL = String.format("SELECT fl_key, MAX(fl_version) FROM %s WHERE fl_key like ? GROUP BY fl_key ORDER BY fl_key", tabName);
        	pstmt = conn.prepareStatement(selectSQL) ;
        	pstmt.setString(1, p_keyPattern);
	        if (fDebug) UniLog.log("selectSQL:"+pstmt.toString());
	        
			ResultSet rs = pstmt.executeQuery();	
			while (rs.next()) { 
				if (fDebug) UniLog.log1("result:%s", rs.getString("fl_key"));
		        resultList.add(rs.getString("fl_key"));
	        }
	       	return(resultList);
		}
		catch(Exception ex){
			throw (ex);
		}
		finally{
			try{
				if (conn != null) conn.close();
				conn = null;
			} catch (Exception ex){}
		}
	}
}
