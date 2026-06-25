package com.uniinformation.utils;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.json.*;
import com.uniinformation.utils.*;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.rpccall.*;

public class JdbcRpcServer implements RpcServlet
{
	Connection jdbcConnection;
	private RpcServerConnection conn = null;
	public JdbcRpcServer() {
	   super();
	}
	/* for RpcServlet Interface started */
	public void setConnection(RpcServerConnection p_conn) {
		conn = p_conn ;
		conn.setDebug(false);
	}
	public void init_servlet() {
	}
	public void close_servlet() {
	}
	/* for RpcServlet Interface ended */

	/* rpcservice started */
	public String ping() {
		return("OK  ");
	}
	public void setConnection(Connection p_jdbcConn)
	{
		jdbcConnection = p_jdbcConn;
	}
	public String getConnection(String p_url,String p_user,String p_password)
	{
		try { 
			DriverManager.setLoginTimeout(10);
			UniLog.log("JdbcRpcServer.getConnection " + p_url + "," + p_user + "," + p_password);
			Properties prop = new Properties();
			if(p_user != null) prop.put("user",p_user);
			if(p_password != null) prop.put("password",p_password);
			prop.put("connectTimeout","10000");
//		jdbcConnection = DriverManager.getConnection(p_url, p_user, p_password);
			jdbcConnection = DriverManager.getConnection(p_url, prop);
			UniLog.log("JdbcRpcServer.getConnection got " + jdbcConnection);
			if(conn != null) conn.setDefaultServletClassName(getClass().getName());
			return("OK  "); 
		} catch (SQLException sex) {
			UniLog.log(sex);
			return("FAIL" + sex.toString());
		}
	}

	public String bi_gettablelist(String p_database_not_use,String p_catalog,String p_schema) {
		PreparedStatement st = null;
     	ResultSet rs = null;
		boolean finished;
		try { 
			String catalog = null;
			JSONArray ja = new JSONArray();
			DatabaseMetaData dbmd = jdbcConnection.getMetaData();
			String types[] = {"TABLE","VIEW"};
			if(p_catalog != null && !p_catalog.trim().equals("")) catalog = p_catalog;
//			rs = dbmd.getTables(null,p_database.trim().toUpperCase(),"%",types);
//			rs = dbmd.getTables(p_database.trim(),"dbo","%",types);
			rs = dbmd.getTables(catalog,p_schema == null ? null : p_schema.trim(),"%",types);
//			UniLog.log("HAHA table list for database " + p_database + " is " + rs);
		   for (finished=false;;) {
            finished = !(rs.next());
			   if (finished) break;
//				UniLog.log("HAHA get one table from catalog " + rs.getString("TABLE_CAT") + " " + rs.getString("TABLE_SCHEM") + " " + rs.getString("TABLE_NAME"));
				ja.put(rs.getString("table_name"));
			}

			/*
			String sqlstr = "select table_name from information_schema.tables where table_schema = '" + p_database.trim()+ "'";
			st = jdbcConnection.prepareStatement(sqlstr);
			rs = st.executeQuery();
		   for (finished=false;;) {
            finished = !(rs.next());
			   if (finished) break;
				ja.put(rs.getObject(1));
			}
			st.close();
			*/
			return("OK  "+ja.toString());
		} catch (SQLException sex) {
			UniLog.log(sex);
			return("FAIL" + sex.toString());
		}
	}
	String getBiFdType(Object p_type) {
		if(p_type.toString().toLowerCase().equals("varchar")) return("char");
		if(p_type.toString().toLowerCase().equals("bigint")) return("integer");
		return("unknown");
	}
	String getBiFdTypeX(int p_datatype,String p_autoInc) {
		switch(p_datatype) {
		case java.sql.Types.TINYINT: 
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER: 
		case java.sql.Types.BIGINT: 
					if(p_autoInc != null && p_autoInc.toLowerCase().equals("yes")) 	
							return("serial");
					else
							return("integer");
		case java.sql.Types.CHAR: 
		case java.sql.Types.VARCHAR: 
		case java.sql.Types.LONGVARCHAR: 
							return("char");
		case java.sql.Types.DATE: 
							return("date");
		case java.sql.Types.REAL: 
		case java.sql.Types.DOUBLE: 
		case java.sql.Types.FLOAT: 
							return("float");
		case java.sql.Types.TIME: 
		case java.sql.Types.TIME_WITH_TIMEZONE:
							return("time");
		case java.sql.Types.TIMESTAMP: 
		case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
							return("datetime");
		case java.sql.Types.BINARY: 
		case java.sql.Types.VARBINARY: 
		case java.sql.Types.LONGVARBINARY: 
							return("binary");
		case java.sql.Types.BOOLEAN: 
							return("boolean");
		case java.sql.Types.DECIMAL: 
							return("decimal");
		default : return("Unknown_"+p_datatype);
		}
	}
	public String bi_gettableschema(String p_database_not_use,String p_table,String p_catalog,String p_schema) {
		PreparedStatement st = null;
     	ResultSet rs = null;
		boolean finished;
		try { 
			JSONArray ja = new JSONArray();
			if(BiSchema.getSqlEnginetype(jdbcConnection) == BiSchema.SQLENGINE_SCORPION) {
				/*
				TableRec tr2 = su2.getQueryResult("select colno ddc_seq,colname ddc_colname,callsegs('bi_getfdtype',coltype) ddc_coltype,collength ddc_collen"+ 
						" from systables,syscolumns where tabname = '"+ tr.getField("ddt_dbtname").toString()+"' and syscolumns.tabid = systables.tabid " ,null);
				for(int j = 0;j<tr2.size();j++) {
					tr2.setRecPointer(j);
					//UniLog.log("BISchema add field " + tr2.getField("ddc_colname").toString() +  " type " + tr2.getField("ddc_coltype").toString());
					table.addField (
						tr2.getField("ddc_colname").toString(),
						tr2.getField("ddc_colname").toString(),
						tr2.getField("ddc_colname").toString(),
						tr2.getField("ddc_coltype").toString(),
						((Integer) tr2.getField("ddc_collen")).intValue(),
						0
					);
				}
				*/
				PreparedStatement pstmt = jdbcConnection.prepareStatement(
						"select colno ddc_seq,colname ddc_colname,callsegs('bi_getfdtype',coltype) ddc_coltype,collength ddc_collen"+ 
						" from systables,syscolumns where tabname = ? and syscolumns.tabid = systables.tabid ");
				pstmt.setString(1, p_table);
				rs = pstmt.executeQuery();	
				while (rs.next()) {
					JSONObject jo = new JSONObject();
					jo.put("colidx",rs.getString("ddc_seq"));
					jo.put("colname",rs.getString("ddc_colname"));
					jo.put("coltype",rs.getString("ddc_coltype"));
					jo.put("collen",rs.getInt("ddc_collen"));
					jo.put("coldec",0);
					ja.put(jo);
				}
			} else {
			String catalog = null;
			String table = null;
			DatabaseMetaData dbmd = jdbcConnection.getMetaData();
			int dotIdx = p_table.indexOf('.');
			if(dotIdx >= 0) {
				catalog = p_table.substring(0,dotIdx);
				table = p_table.substring(dotIdx+1);
			} else {
				table = p_table;
				if(p_catalog != null && !p_catalog.trim().equals("")) catalog = p_catalog;
			}
			String schema = StringUtils.isBlank(p_schema) ? null : p_schema.trim();
			if (BiSchema.getSqlEnginetype(jdbcConnection) == BiSchema.SQLENGINE_POSTGRESQL && dotIdx >= 0) {
				schema = p_table.substring(0,dotIdx);
				table = p_table.substring(dotIdx+1);
				catalog = StringUtils.isBlank(p_catalog) ? null : p_catalog.trim();
			}
			rs = dbmd.getColumns(catalog,schema,table.trim(),"%");
		    for (finished=false;;) {
            finished = !(rs.next());
			   if (finished) break;
				/*
				UniLog.log("HAHA get one columns from catalog " + 
					rs.getString("ORDINAL_POSITION") + " , "  +
					rs.getString("COLUMN_NAME")+ " , "  +
					rs.getString("DATA_TYPE")+ " , "  +
					rs.getString("COLUMN_SIZE")+ " , " +
					rs.getInt("DECIMAL_DIGITS")+ " , " +
					rs.getString("IS_AUTOINCREMENT")+ " , " 
					);
				*/
//				UniLog.log("HAHA datatype " + getBiFdTypeX(rs.getInt("DATA_TYPE"),rs.getString("IS_AUTOINCREMENT")));
				JSONObject jo = new JSONObject();
				jo.put("colidx",rs.getString("ORDINAL_POSITION"));
				jo.put("colname",rs.getString("COLUMN_NAME"));
				jo.put("coltype",getBiFdTypeX(rs.getInt("DATA_TYPE"),rs.getString("IS_AUTOINCREMENT")));
				jo.put("collen",rs.getString("COLUMN_SIZE"));
				jo.put("coldec",rs.getString("DECIMAL_DIGITS"));
				ja.put(jo);
//				ja.put(rs.getString("TABLE_NAME"));
			}
			}
			/*
			String sqlstr = 
			"select ordinal_position, column_name, data_type, character_maximum_length "
			+ "from information_schema.columns "
			+ "where table_schema = '" + p_database.trim() + "' and table_name = '" + p_table.trim() + "' and column_name <> 'serial_id'";
			st = jdbcConnection.prepareStatement(sqlstr);
			rs = st.executeQuery();
		   for (;;) {
            finished = !(rs.next());
			   if (finished) break;
				JSONObject jo = new JSONObject();
				jo.put("colidx",rs.getObject(1));
				jo.put("colname",rs.getObject(2));
				jo.put("coltype",getBiFdType(rs.getObject(3)));
				jo.put("collen",rs.getObject(4));
				ja.put(jo);
			}
			st.close();
			*/
			return("OK  "+ja.toString());
		} catch (Exception sex) {
			UniLog.log(sex);
			return("FAIL" + sex.toString());
		}
	}
	public static void main(String args[]){	
		JdbcRpcServer jdbcSvr = new JdbcRpcServer();
		String s = jdbcSvr.getConnection("jdbc:mysql://192.168.17.61:3306","donald","cxvkljr");
		UniLog.log("jdbcConnection got " + s);
		s = jdbcSvr.bi_gettablelist(/* "hkdplcom_wineac2" */ null,"hkdplcom_wineac2" , /* "hkdplcom_wineac2" */ null);
		UniLog.log("bi_gattablelist got " + s);
	}
}
