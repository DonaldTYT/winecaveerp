package com.uniinformation.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.FilingUtil.FilingUtilPropObj;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;


public class MysqlUtil {
	/***
	 * create table / index
	 * remark: mysql create table is implicit commit. not support rollback
	 * 
	 * @param conn
	 * @param p_tabName
	 * @param p_sqls
	 * @throws Exception
	 */
	public static void createTable(Connection conn, String p_tabName, String...p_sqls) throws Exception{
		if (checkTableExist(conn, p_tabName)){
			UniLog.logm(null,"table %s exist, skip create", p_tabName);
			return;
		}
		Statement stmt = conn.createStatement();
		for (int i=0; i<p_sqls.length; i++){
			UniLog.log1("execute %s", p_sqls[i]);
			int cc = stmt.executeUpdate(p_sqls[i]);
			UniLog.log1("return %d", cc);
		}
	}
	
	public static int executeUpdate(Connection conn, String p_sql) throws Exception{
		Statement stmt = conn.createStatement();
		UniLog.log1("execute %s", p_sql);
		int cc = stmt.executeUpdate(p_sql);
		UniLog.log1("return %d", cc);
		return cc;
	}
	public static ReturnMsg executeUpdateNoEx(Connection conn, String p_sql) {
		try {
			Statement stmt = conn.createStatement();
			UniLog.log1("execute %s", p_sql);
			int cc = stmt.executeUpdate(p_sql);
			UniLog.log1("return %d", cc);
			return new ReturnMsg(true).setData(cc);
		}
		catch(Exception ex) {
			UniLog.log1("error:" +ex.getMessage());
			return new ReturnMsg(ex);
		}
	}
	
	public static boolean checkTableExist(SelectUtil su, String p_tabName) throws Exception{
		return checkTableExist(su.getConnection(), p_tabName);
		
	}
	public static boolean checkTableExist(Connection conn, String p_tabName) throws Exception{
		String curSchema = conn.getMetaData().getURL().replaceAll("^.*/", "").replaceAll("\\?.*", "");
	
		
		PreparedStatement pst = conn.prepareStatement( "SELECT COUNT(*) FROM information_schema.tables WHERE TABLE_SCHEMA=? and TABLE_NAME=?");
		pst.setString(1, curSchema);
		pst.setString(2, p_tabName);
		ResultSet rs = pst.executeQuery();
		boolean tableExist = false;
		if (rs.next()){
			if (rs.getInt(1) >= 1){
				tableExist = true;
			}
		}
		
		
		UniLog.log1("table:%s exist:%s", p_tabName, tableExist);
		return(tableExist);
	}
	public static List<String> getTableList(Connection conn) throws Exception{
		String curSchema = conn.getMetaData().getURL().replaceAll("^.*/", "").replaceAll("\\?.*", "");
		ArrayList<String> tableList = new ArrayList<String>();
		
		//list all table
		PreparedStatement pst = conn.prepareStatement( "SELECT tables.TABLE_NAME FROM information_schema.tables WHERE TABLE_SCHEMA=?");
		pst.setString(1, curSchema);
		ResultSet rs = pst.executeQuery();
		while (rs.next()){
			tableList.add(rs.getString(1));
		}
		return tableList;
		
	}
	
	public static boolean checkColumnExist(Connection conn, String p_tabName, String p_colName) throws Exception{
		if (StringUtils.isAnyBlank(p_tabName, p_colName)) {
			UniLog.log1("tab or col is blank");
			return false;
		}
		String curSchema = conn.getMetaData().getURL().replaceAll("^.*/", "").replaceAll("\\?.*", "");
	
		
		PreparedStatement pst = conn.prepareStatement( "SELECT DATA_TYPE FROM information_schema.columns WHERE TABLE_SCHEMA=? and TABLE_NAME=? AND COLUMN_NAME=?");
		pst.setString(1, curSchema);
		pst.setString(2, p_tabName);
		pst.setString(3, p_colName);
		ResultSet rs = pst.executeQuery();
		boolean fExist = false;
		if (rs.next()){
			if (StringUtils.isNotBlank(rs.getString(1))){
				fExist = true;
			}
		}
		
		
		UniLog.log1("table:%s col:%s exist:%s", p_tabName, p_colName, fExist);
		return(fExist);
		
	}
	
	private static void selfTestCheckTable(){
		SelectUtil su = null;
		for (int i=0; i<50;i++) {
			UniLog.log1("test:%d", i);
			try{
				FilingUtilPropObj fup = FilingUtil.loadProp("pmsdemo");
				Connection conn = fup.jdbcPool.getConnection();
				su = new SelectUtil();
				su.init(conn);
				checkTableExist(su, "filing");
				Thread.sleep(200);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			finally{
				CloseUtil.close(su);
			}
		}
		System.exit(0);
	}
	private static void selfTestCreateTable() {
		SelectUtil su = null;
		try {
			FilingUtilPropObj fup = FilingUtil.loadProp("pmsdemo");
			Connection conn = fup.jdbcPool.getConnection();
			su = new SelectUtil();
			su.init(conn);
			MysqlUtil.createTable(conn, "testcreate", 
					"CREATE TABLE testcreate (tc_key varchar(100) NOT NULL, tc_name varchar(100) DEFAULT NULL, PRIMARY KEY (tc_key)) ENGINE=InnoDB DEFAULT CHARSET=utf8;",
					"CREATE INDEX idx_testcreate_tc_name ON testcreate(tc_name)");
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			CloseUtil.close(su);
		}
		System.exit(0);
	}
	private static void selfTestListTable() {
		try {
			FilingUtilPropObj fup = FilingUtil.loadProp("pmsdemo");
			Connection conn = fup.jdbcPool.getConnection();
			List<String> tableList = getTableList(conn);
			for (String s : tableList) {
				UniLog.log1("table:%s", s);
			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}
	public static void main(String args[]) throws Exception{
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("vincero-mysqlschema", "hlv",null);
		checkColumnExist(sh.getJdbcPool().getConnection(), "rg","serial_id");
		System.exit(0);
		//selfTestCheckTable();
		//selfTestCreateTable();
		//selfTestListTable();
	}
}