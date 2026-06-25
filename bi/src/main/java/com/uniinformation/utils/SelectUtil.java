package com.uniinformation.utils;


import java.util.*;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.io.*;
import java.lang.reflect.*;

import com.uniinformation.cell.*;
import com.uniinformation.rpccall.RpcClient;
import com.kyoko.common.*;

public class SelectUtil {
   final public static String JDBC_DRIVER_CLASS_LIST_FILE = "/yic/v/cp/init/JdbcDriverClass.list";
   final public static boolean DEFAULT_DEBUG = false;
   final public static int SLOW_SQL_TIME = 500;
   String loginId;
   boolean poolable = true;
   boolean allowNull = false;
   private Connection connection = null;
	int sqlerrd1 = 0;
	private static boolean fDebug = DEFAULT_DEBUG;
	public static String debugPrefix = null;
	public static Hashtable connsHash = new Hashtable(); //for debug
	GetConnectionInterface getConnectionInterface = null;
   Connection tmpConnection = null;

   public class Cursor {
	   PreparedStatement st = null;
      ResultSet rs = null;
      TableRec tr0 = null;
		boolean finished = false;
		boolean withnull = false;
	   private Cursor(String p_sqlstr, Wherecl p_wherecl, boolean p_withnull) throws Exception {
			try {
            StringBuffer sqlstr = new StringBuffer();
            ResultSetMetaData rsmd = null;
            int cc = -1;
            sqlstr.append(p_sqlstr);
            if (p_wherecl != null) {
	            String whereString = Wherecl.stripAnd(p_wherecl.getWhereString());
			      if (whereString != null && !whereString.trim().equals("")) {
						if (!whereString.trim().startsWith("limit") &&
							!whereString.trim().startsWith("order by") &&
							!whereString.trim().startsWith("group by"))
						sqlstr.append(" where");
					  	sqlstr.append(" ").append(whereString);
					}
		         if (p_wherecl.getOrderby() != null && !p_wherecl.getOrderby().equals(""))
	               sqlstr.append(" order by ")
				            .append(p_wherecl.getOrderby());
            }
               if (connection == null && getConnectionInterface != null){
            	  connection = getTempConnection();
               }
			   if (connection == null)
			      throw(new Exception("Connection is null"));
			   if( !connection.isValid(900000)) {
				   if (connection instanceof JdbcPool.ConnectionRecord) {
				  		if(!((JdbcPool.ConnectionRecord) connection).reconnect()) {
				  			throw(new Exception("Connection expired"));
				  		}
				   }
			   }
            //UniLog.log(new Exception("Cursor() debug!!!"));
//            UniLog.logClass(this, "Cursor(): "+sqlstr.toString()+" ("+(p_wherecl == null ? "" : p_wherecl.argumentToString())+")");
	         if (debugPrefix != null) {
               if (sqlstr.toString().startsWith(debugPrefix))
                  UniLog.log(new Exception("debug"));
				}
	         st = connection.prepareStatement(sqlstr.toString());
	         st.setPoolable(poolable);
	         if (p_wherecl != null)
	            p_wherecl.setStatementValue(st, 0);
				java.util.Date ts = new java.util.Date();
	         rs = st.executeQuery();
				java.util.Date ts1 = new java.util.Date();
//UniLog.logClass(this, "Cursor():after executeQuery:"+(ts1.getTime()-ts.getTime())+"ms");
            if (ts1.getTime()-ts.getTime() > SLOW_SQL_TIME) {
//UniLog.log(new Exception("Slow SQL "+(ts1.getTime()-ts.getTime())+"ms"+": "+sqlstr.toString()+" ("+(p_wherecl == null ? "" : p_wherecl.argumentToString())+")"));
//UniLog.logClass(SelectUtilSlowLog.getInstance(), "Slow SQL "+(ts1.getTime()-ts.getTime())+"ms"+": "+sqlstr.toString()+" ("+(p_wherecl == null ? "" : p_wherecl.argumentToString())+")");
				}
// if (sqlstr.toString().startsWith("select vendor_u")) UniLog.log(new Exception("debug"));
	         rsmd = rs.getMetaData();
	         int colcnt = rsmd.getColumnCount();
	         String[] labels = new String[colcnt];
	         int[] coltypes = new int[colcnt];
	         int[] collen = new int[colcnt];
	         for (int i=0; i<colcnt; i++) {
	            labels[i] = rsmd.getColumnLabel(i+1).toLowerCase();
	            coltypes[i] = rsmd.getColumnType(i+1);
	            collen[i] = rsmd.getColumnDisplaySize(i+1);
					// patch for mysql-5.0
					if (collen[i] == 196605)
					   collen[i] = 65535;
				   else if (collen[i] == -3)
					   collen[i] = 16777215;
	         }
	         tr0 = new TableRec("none", labels, coltypes, collen);
			} catch (Exception ex) {
	         close();
			   throw(ex);
			}
			/*
			finally{
				closeTempConnection();
			}
			*/
		}
		private Cursor(ResultSet p_rs) throws Exception {
			try {
            ResultSetMetaData rsmd = null;
	         rs = p_rs;
	         rsmd = rs.getMetaData();
	         int colcnt = rsmd.getColumnCount();
	         String[] labels = new String[colcnt];
	         int[] coltypes = new int[colcnt];
	         int[] collen = new int[colcnt];
	         for (int i=0; i<colcnt; i++) {
	            labels[i] = rsmd.getColumnLabel(i+1).toLowerCase();
	            coltypes[i] = rsmd.getColumnType(i+1);
	            collen[i] = rsmd.getColumnDisplaySize(i+1);
					// patch for mysql-5.0
					if (collen[i] == 196605)
					   collen[i] = 65535;
				   else if (collen[i] == -3)
					   collen[i] = 16777215;
	         }
	         tr0 = new TableRec("none", labels, coltypes, collen);
			} catch (Exception ex) {
	         close();
			   throw(ex);
			}
		}
	   public boolean isFinished() {
		   return(finished);
		}
	   public void close() {
		   try {
			 closeTempConnection();
	         if (rs != null) {
					rs.close();
					rs = null;
				}
	         if (st != null) {
					st.close();
					st = null;
				}
			} catch (Exception ex) {
			   UniLog.log(ex);
			}
		}
	   public TableRec fetch(int p_maxcnt, TableRec p_tr) throws Exception {
			try {
		      TableRec tr = p_tr;
				if (tr != null) {
				   if (tr.size() > 0) {
				      tr.setRecPointer(tr.size()-1);
				   }
				}
				else {
				   tr = tr0;
					tr.clear();
				}
			   if (finished)
			      return(tr);
			   int colcnt = tr.getFieldCount();
            int[] coltypes = tr.getColtypes();
            String[] labels = tr.getFieldNames();
			   int cnt = 0;
			   for (;;) {
	            finished = !(rs.next());
				   if (finished)
				      break;
               tr.addRecord();
               for (int i=0; i<colcnt; i++) {
				      Object obj = null;
                  switch (coltypes[i]) {
	               case java.sql.Types.LONGVARBINARY:
	               case java.sql.Types.VARBINARY:
	               case java.sql.Types.BINARY:
	               case java.sql.Types.BLOB:
					         obj = rs.getBytes(i+1);
							   break;
					 default:
				         obj = rs.getObject(i+1);
							   break;
				}
				if (obj instanceof java.time.OffsetDateTime)
					obj = java.util.Date.from(((java.time.OffsetDateTime) obj).toInstant());
				else if (obj instanceof java.time.LocalDateTime)
					obj = java.sql.Timestamp.valueOf((java.time.LocalDateTime) obj);
				else if (obj instanceof java.time.LocalDate)
					obj = java.sql.Date.valueOf((java.time.LocalDate) obj);
				if (obj != null && obj instanceof java.util.Date) {
					 if (DateUtil.isDateNull((java.util.Date) obj)) {
						 obj = null;
					 } else {
						 if(coltypes[i] != java.sql.Types.TIME && coltypes[i] != java.sql.Types.TIMESTAMP) {
						 if (st.getClass().getName().startsWith("com.mysql.jdbc") || 
							 st.getClass().getName().startsWith("com.mysql.cj.jdbc")) {
							 obj = new java.util.Date( ((java.util.Date) obj).getTime() + DateUtil.getGmtOffset());
						 }
						 } else {
							 if(coltypes[i] == java.sql.Types.TIMESTAMP) {
								 obj = new java.util.Date( ((java.util.Date) obj).getTime());
							 }
						 }
					 }
				 }
				 if (obj == null && !withnull) {
					 if (allowNull && coltypes[i] == Types.DOUBLE) {
						 obj = Double.NaN;
					 }
					 else {
						 obj = TableBrowser.getDefaultValue(labels[i], coltypes[i]);
						 
					 }
				 }
				 if (obj instanceof String)
					 obj = StringUtil.sr((String) obj);
				 if (obj instanceof Float)
					      obj = new Double(((Float) obj).doubleValue());
				 
				 // add by DT , currently always convert to int32 until upper level codes can properly handle other types such as bigint
				 if (obj instanceof java.math.BigInteger) {
					 obj = new Integer(((java.math.BigInteger) obj).intValue());
				 } 
				 /*
				 //andrew230109 handle long value. should not convert long to int (for handle gbp bigint deckid)
				 else if (obj instanceof java.lang.Long) {
					 obj = new Integer(((java.lang.Long) obj).intValue());
				 }
				 */
					 
                 tr.setField(i, obj);
               }
			      cnt++;
               if (p_maxcnt > 0 && cnt >= p_maxcnt)
				      break;
            }
//            UniLog.logClass(this, "fetch(): "+cnt);
		      return(tr);
			} catch (Exception ex) {
			   finished = true;
			   throw(ex);
		   }
		}
	   /*
	    * 
	    //This block of code does not work properly, so the closeTempConnection moved to close()
	    @Override
	    protected void finalize() throws Throwable {
	        try{
	            closeTempConnection();
	        }
	        finally{
	            super.finalize();
	        }
	    }
	    */
	}
	public Connection getConnection() {
	   return(connection);
	}
	protected void setConnection(Connection p_connection) {
	   connection = p_connection;
		if (p_connection != null) {
			connsHash.put(p_connection.hashCode(), new Exception("Debug setConnection()")); 
			if(loginId != null) {
				try {
					p_connection.setClientInfo("perfuser", loginId);
				} catch (SQLClientInfoException sex) {
					UniLog.log(sex);
				}
			}
		}
	}
	public void setGetConnectionInterface(GetConnectionInterface p_getConnectionInterface){
		getConnectionInterface = p_getConnectionInterface;
	}
	public SelectUtil init() throws Exception {
	   setConnection(JdbcPool.getJdbcPool().getConnection());
	   return this;
	}
	public SelectUtil init(JdbcPool jdbcPool) throws Exception {
	   setConnection(jdbcPool.getConnection());
	   return this;
	}
	public SelectUtil init(Connection p_connection) throws Exception {
	   setConnection(p_connection);
	   return this;
	}
   public TableRec getQueryResult(String p_sqlstr, Wherecl p_wherecl, int p_maxcnt) throws Exception {
      return(getQueryResultWithMaxCount(p_sqlstr, p_wherecl, false, p_maxcnt));
	}
   public TableRec getQueryResult(String p_sqlstr) throws Exception {
      return(getQueryResult(p_sqlstr, null, false));
	}
   public TableRec getQueryResult(String p_sqlstr, Wherecl p_wherecl) throws Exception {
      return(getQueryResult(p_sqlstr, p_wherecl, false));
	}
   public TableRec getQueryResult(String p_sqlstr, Wherecl p_wherecl, boolean p_withnull) throws Exception {
      return(getQueryResultWithMaxCount(p_sqlstr, p_wherecl, p_withnull, -1));
	}
   public TableRec getQueryResultWithMaxCount(String p_sqlstr, Wherecl p_wherecl, boolean p_withnull, int p_maxcnt) throws Exception {
      return(getQueryResultWithMaxCount(p_sqlstr, p_wherecl, p_withnull, p_maxcnt, null));
	}
   public TableRec getQueryResultWithMaxCount(String p_sqlstr, Wherecl p_wherecl, boolean p_withnull, int p_maxcnt, TableRec p_tablerec) throws Exception {
	   Cursor cursor = null;
      try {
	      cursor = new Cursor(p_sqlstr, p_wherecl, p_withnull);
	      TableRec tr = cursor.fetch(p_maxcnt, p_tablerec);
         return(tr);
	      } finally {
		   if (cursor != null)
			   cursor.close();
	   }
   }
   public TableRec getQueryResultByResultSet(ResultSet p_rs) throws Exception {
	   Cursor cursor = null;
      try {
	      cursor = new Cursor(p_rs);
	      TableRec tr = cursor.fetch(-1, null);
         return(tr);
	   } finally {
		   if (cursor != null)
			   cursor.close();
	   }
   }
   public Cursor openCursor(String p_sqlstr, Wherecl p_wherecl, boolean p_withnull) throws Exception {
	   return(new Cursor(p_sqlstr, p_wherecl, p_withnull));
	}
   public TableRec fetchCursor(Cursor p_cursor, int p_maxcnt, TableRec p_tablerec) throws Exception {
	   TableRec tr = p_cursor.fetch(p_maxcnt, p_tablerec);
      return(tr);
	}
   public void closeCursor(Cursor p_cursor) {
	   p_cursor.close();
	}
	public static Vector makeOptions(TableRec p_tr, String p_colnamelist[]) {
	   return(makeOptions(p_tr, p_colnamelist, " "));
	}
	public static Vector makeOptions(TableRec p_tr, String p_colnamelist[], String p_separator) {
		TableRec tr = p_tr;
		Vector v;
		try {
			v = new Vector();
		   for (int i = 0; i<tr.getRecordCount(); i++) {
			   StringBuffer sb = new StringBuffer();
	         tr.setRecPointer(i);
			   for (int j=0; j<p_colnamelist.length; j++) {
					if (j > 0) 
					   sb.append(p_separator);
					if (tr.getField(p_colnamelist[j]) == null) 
						sb.append("");
					else
						sb.append(tr.getField(p_colnamelist[j]).toString());
				}
			   v.addElement(sb.toString());
		   }
		} catch (Exception ex) {
		   UniLog.log(ex);
		   return(null);
		}
		return(v);
	}

   private void closeTempConnection(){
	   if (tmpConnection == null) {
		   return;
	   }
	   if (fDebug) UniLog.logm(this,"closeTempConnection() connection:%s tmpConnection:%s", connection, tmpConnection);
	   try{
		   if(connection == tmpConnection){
			   connection = null;
		   }
		   tmpConnection.close();
	   }
	   catch(Exception ex){
		   ex.printStackTrace();
	   }
	   tmpConnection = null;
   }
   private Connection getTempConnection(){
	   //UniLog.log("getTempConnection()");
	   if (tmpConnection != null){
		   return(tmpConnection);
	   }
	   tmpConnection = getConnectionInterface.getConnection();
		if(loginId != null) {
			try {
				tmpConnection.setClientInfo("perfuser", loginId);
			} catch (SQLClientInfoException sex) {
				UniLog.log(sex);
			}
		}
	   
	   return(tmpConnection);
   }
   public int executeUpdate(String p_sqlstr, Wherecl p_wherecl) throws Exception {
      StringBuffer sqlstr = new StringBuffer();
      PreparedStatement st = null;
      int cc = -1;

      sqlstr.append(p_sqlstr);
      if (p_wherecl != null && !p_wherecl.getWhereString().trim().equals("")) {
	      String whereString = Wherecl.stripAnd(p_wherecl.getWhereString());
			if (whereString != null && !whereString.trim().equals(""))
	         sqlstr.append(" where ").append(whereString);
      }
      try {
    	    if (connection == null && getConnectionInterface != null){
    		    connection = getTempConnection();
    	    }
			if (connection == null){
			   throw(new Exception("Connection is null"));
			}
			   if( !connection.isValid(900000)) {
				   if (connection instanceof JdbcPool.ConnectionRecord) {
				  		if(!((JdbcPool.ConnectionRecord) connection).reconnect()) {
				  			throw(new Exception("Connection expired"));
	         }
	      }
	}
//         UniLog.logClass(this, "executeUpdate(): "+sqlstr.toString()+", [wherecl="+p_wherecl+"]");
			// Wed Jan 12 14:41:51 HKG 2011
	      //st = connection.prepareStatement(sqlstr.toString());
	      st = connection.prepareStatement(sqlstr.toString(), Statement.RETURN_GENERATED_KEYS);
	      if (p_wherecl != null)
	         p_wherecl.setStatementValue(st, 0);
			long ts0 = System.currentTimeMillis();
	      cc = st.executeUpdate();
			long ts1 = System.currentTimeMillis();
         if ((ts1-ts0) > SLOW_SQL_TIME) {
UniLog.logClass(SelectUtilSlowLog.getInstance(), "Slow SQL "+(ts1-ts0)+"ms"+": "+sqlstr.toString()+" ("+(p_wherecl == null ? "" : p_wherecl.argumentToString())+")");
			}
			if (st.getClass().getName().equals("com.uniinformation.scorpion.driver.ScpStatement")) {
			   Method method = st.getClass().getMethod("getLastsqlerrd1", new Class[0]);
            sqlerrd1 = ((Integer) method.invoke(st, new Object[0])).intValue();
			}
		  	else {
			   try {
					if (sqlstr.toString().toLowerCase().indexOf("insert into") >= 0) {
						try (ResultSet insertedKeyResult = st.getGeneratedKeys()) {
							if (insertedKeyResult != null && insertedKeyResult.next())
								sqlerrd1 = insertedKeyResult.getInt(1);
						}
					}
			   } catch(Exception ex4) {
				   throw(ex4);
			  	}
		  	}
	   } catch (Exception e) {
			//UniLog.log(e);
			UniLog.log1("error:" + e.getMessage());
			throw(e);
	   } finally {
		   closeTempConnection();
	      try {
	         if (st != null) st.close();
	      } catch (SQLException e) {
			   UniLog.log(e);
				throw(e);
         }
	   }
	   return(cc);
   }
	public void close() {
		try {
         if (connection != null) {
		      connection.close();
				connsHash.remove(connection.hashCode());
			}
		} catch (Exception ex) {
		   UniLog.log(ex);
		}
        connection = null;
	}
	/***
	 * set connection record reuse flag
	 * if fReuse = false, the connection will be discarded after close
	 * 
	 * @param p_fReuse
	 */
	public void setReuse(boolean p_fReuse) {
		try {
			if (connection != null && connection instanceof JdbcPool.ConnectionRecord) {
				((JdbcPool.ConnectionRecord) connection).setReuse(p_fReuse);
			}
		} catch (Exception ex) {
		   UniLog.log(ex);
		}
        connection = null;
	}
	/***
	 * This method does not support multiple p_tr, update field based on current record
	 * @param p_tabname
	 * @param p_tr
	 * @param p_colnames
	 * @param p_trColnames
	 * @param p_wc
	 * @return < 0 - error, >= 0 - normal (row count)
	 * @throws Exception
	 */
   public int updateByTableRec(String p_tabname, TableRec p_tr, Vector p_colnames, Vector p_trColnames, Wherecl p_wc) throws Exception {
	   StringBuffer sb = new StringBuffer();
		sb.append("update ")
		  .append(p_tabname)
		  .append(" set ");
	   for (int i=0; i<p_colnames.size(); i++) {
			if (i > 0)
			   sb.append(",");
		   sb.append(p_colnames.elementAt(i).toString())
		     .append(" = ? ");
		}
		if (p_wc != null)
		   sb.append(" where ")
			  .append(p_wc.getWhereString());
      PreparedStatement st = null;
      int cc = -1;
      try {
            if (connection == null && getConnectionInterface != null){
            	connection = getTempConnection();
            }
			if (connection == null)
			   throw(new Exception("Connection is null"));
			   if( !connection.isValid(900000)) {
				   if (connection instanceof JdbcPool.ConnectionRecord) {
				  		if(!((JdbcPool.ConnectionRecord) connection).reconnect()) {
				  			throw(new Exception("Connection expired"));
				  		}
				   }
			   }
	      st = connection.prepareStatement(sb.toString());
	      p_wc.setStatementValue(st, p_colnames.size());
	      for (int i=0; i<p_trColnames.size(); i++) {
	    	  Object dataObj = p_tr.getField(p_trColnames.elementAt(i).toString());
	    	  if (!allowNull && dataObj == null) {
	    		  switch (p_tr.getFieldType(p_trColnames.elementAt(i).toString())) {
	    		  case java.sql.Types.SMALLINT:
	    	  case java.sql.Types.INTEGER :
	    		  st.setInt(i+1, 0);
	    		  break;
	    	  case java.sql.Types.BOOLEAN:
	    		  st.setBoolean(i+1, false);
	    		  break;
	    		  case java.sql.Types.REAL :
	    		  case java.sql.Types.DOUBLE :
	    		  case java.sql.Types.DECIMAL :
	    		  case java.sql.Types.NUMERIC :
	    		  case java.sql.Types.FLOAT :
	    			  st.setDouble(i+1, 0.0);
	    			  break;
	    		  case java.sql.Types.LONGVARCHAR :
	    		  case java.sql.Types.VARCHAR:
	    		  case java.sql.Types.CHAR :
	    			  st.setString(i+1, "");
	    			  break;
	    		  case java.sql.Types.DATE :
	    			  st.setDate(i+1, new java.sql.Date(DateUtil.getDate("1899/12/31").getTime()));
	    			  break;
	    		  case java.sql.Types.BIGINT :
	    			  st.setLong(i+1, 0);
	    			  break;
	    		  default: 
	    			  UniLog.log(new Exception("type not supported: "+p_tr.getFieldType(p_trColnames.elementAt(i).toString())));
	    			  st.setObject(i+1, p_tr.getField(p_trColnames.elementAt(i).toString()));
	    			  break;
	    		  }
	    	  }
	    	  else if (allowNull && dataObj instanceof Double && (((Double)dataObj).isNaN() || ((Double)dataObj).isInfinite())){
	    		  //andrew240215 hotfix NaN/Infinity error
	    		  UniLog.log1("HAHA240215 dataObj:%s set to null", dataObj);
	    		  st.setObject(i+1, null);
	    	  }
	    	  else {
	    		  st.setObject(i+1, dataObj);
	    	  }
	      }
	      cc = st.executeUpdate();
	   } catch (Exception e) {
			UniLog.log(e);
			throw(e);
	   } finally {
		   closeTempConnection();
	      try {
	         if (st != null) st.close();
	      } catch (SQLException e) {
			   UniLog.log(e);
				throw(e);
         }
	   }
	   return(cc);
	}
   /***
    * @param p_tabname
    * @param p_tr
    * @return < 0 error, >= 0 normal (row count)
    * @throws Exception
    */
   public int insertByTableRec(String p_tabname, TableRec p_tr,boolean p_fClearSerial,String p_serialIdColName) throws Exception {
      return(insertByTableRec(p_tabname, p_tr, p_tr.getFieldNamesVector(), p_tr.getFieldNamesVector(),p_fClearSerial,p_serialIdColName));
	}
   /*
   public int insertByTableRec(String p_tabname, TableRec p_tr, Vector p_colnames, Vector p_trColnames) throws Exception {
      return(insertByTableRec(p_tabname, p_tr, p_colnames, p_trColnames, true));
	}
   public int insertByTableRec(String p_tabname, TableRec p_tr, Vector p_colnames, Vector p_trColnames, boolean p_fClearSerial) throws Exception {
		return(insertByTableRec(p_tabname, p_tr, p_colnames, p_trColnames, p_fClearSerial, "serial_id"));
	}
	*/
   public int insertByTableRec(String p_tabname, TableRec p_tr, Vector p_colnames, Vector p_trColnames, boolean p_fClearSerial, String p_serialIdColName) throws Exception {
	   if (connection == null && getConnectionInterface != null)
		   connection = getTempConnection();
	   if (connection == null)
		   throw(new Exception("Connection is null"));
	   if (!connection.isValid(900000) && connection instanceof JdbcPool.ConnectionRecord
			   && !((JdbcPool.ConnectionRecord) connection).reconnect())
		   throw(new Exception("Connection expired"));
	   boolean useDefaultSerial = p_fClearSerial
			   && JdbcPool.findConnectionType(connection) == JdbcPool.JDBC_POSTGRESQL;
	   StringBuffer sb = new StringBuffer();
		sb.append("insert into ")
		  .append(p_tabname)
		  .append(" (");
	   for (int i=0; i<p_colnames.size(); i++) {
			if (i > 0)
			   sb.append(",");
		   sb.append(p_colnames.elementAt(i).toString());
		}
		sb.append(") values (");
	   for (int i=0; i<p_colnames.size(); i++) {
			if (i > 0)
			   sb.append(",");
		   if (useDefaultSerial && p_colnames.elementAt(i).toString().equalsIgnoreCase(p_serialIdColName))
			   sb.append("DEFAULT");
		   else
			   sb.append("?");
		}
		sb.append(")");

      PreparedStatement st = null;
      int cc = -1;
      try {
	      st = connection.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS);
			for (int j=0; j<p_tr.getRecordCount(); j++) {
			   p_tr.setRecPointer(j);
				int serialIdx = -1;
				int statementParamIdx = 1;
		      StringBuffer logSb = new StringBuffer();
			   for (int i=0; i<p_trColnames.size(); i++) {
					if (p_fClearSerial && p_colnames.elementAt(i).toString().equalsIgnoreCase(p_serialIdColName)) {
				      serialIdx=i;
						if (useDefaultSerial) {
							logSb.append(",DEFAULT");
							continue;
						}
				      st.setInt(statementParamIdx++, 0);
						logSb.append(",0");
					}
					else {
					  int paramIdx = statementParamIdx++;
                  if (p_tr.getField(p_trColnames.elementAt(i).toString()) == null) {
//                      UniLog.log("insertFromTableRec A "  + i + " : " + p_trColnames.elementAt(i) + " : " +p_tr.getFieldType(p_trColnames.elementAt(i).toString()));
	                  switch (p_tr.getFieldType(p_trColnames.elementAt(i).toString())) {
							   case java.sql.Types.SMALLINT:
							   case java.sql.Types.INTEGER :
				               st.setInt(paramIdx, 0);
						         logSb.append(",0");
									break;
							   case java.sql.Types.BOOLEAN:
				               st.setBoolean(paramIdx, false);
						         logSb.append(",false");
									break;
							   case java.sql.Types.REAL :
							   case java.sql.Types.DOUBLE :
							   case java.sql.Types.DECIMAL :
							   case java.sql.Types.NUMERIC :
							   case java.sql.Types.FLOAT :
				               st.setDouble(paramIdx, 0.0);
						         logSb.append(",0.0");
									break;
							   case java.sql.Types.LONGVARCHAR :
							   case java.sql.Types.VARCHAR:
							   case java.sql.Types.CHAR :
				               st.setString(paramIdx, "");
						         logSb.append(",\"\"");
									break;
							   case java.sql.Types.DATE :
//							   case java.sql.Types.TIME:
				               java.sql.Date tmpDate = new java.sql.Date(DateUtil.getDate("1899/12/31").getTime());
				               st.setDate(paramIdx, tmpDate);
						         logSb.append(","+tmpDate.toString());
									break;
							   case java.sql.Types.BIGINT :
				               st.setLong(paramIdx, 0);
						         logSb.append(",0");
									break;
							   case java.sql.Types.LONGVARBINARY:
							   case java.sql.Types.VARBINARY:
							   case java.sql.Types.BINARY:
									st.setNull(paramIdx, p_tr.getFieldType(p_trColnames.elementAt(i).toString()));
						         logSb.append(",null");
									break;
							   default: 
                           UniLog.log(new Exception("type not supported: " + i + " : " + p_trColnames.elementAt(i) + " : " +p_tr.getFieldType(p_trColnames.elementAt(i).toString())));
				               st.setObject(paramIdx, p_tr.getField(p_trColnames.elementAt(i).toString()));
						         logSb.append(","+p_tr.getField(p_trColnames.elementAt(i).toString()));
									break;
							}
						}
						else {
//                           UniLog.log("insertFromTableRec B "  + i + " : " + p_trColnames.elementAt(i) + " : " +p_tr.getFieldType(p_trColnames.elementAt(i).toString()));
				         if (p_tr.getField(p_trColnames.elementAt(i).toString()) instanceof java.sql.Timestamp) {
				        	 	java.sql.Timestamp jt = (java.sql.Timestamp) p_tr.getField(p_trColnames.elementAt(i).toString());
								st.setTimestamp(paramIdx, jt);
//								st.setTimestamp(i+1, (java.sql.Timestamp) p_tr.getField(p_trColnames.elementAt(i).toString()));
				         } else if (p_tr.getField(p_trColnames.elementAt(i).toString()) instanceof java.util.Date) {
				        	 if(p_tr.getFieldType(p_trColnames.elementAt(i).toString()) == java.sql.Types.TIME) {
				        		java.util.Date jd = (java.util.Date) p_tr.getField(p_trColnames.elementAt(i).toString());
//				        		java.sql.Time jt = new java.sql.Time(jd.getTime());
//				        		st.setTime(i+1, jt);
				        		java.sql.Timestamp jt = new java.sql.Timestamp(jd.getTime());
				        		st.setTimestamp(paramIdx, jt);
//				        		st.setTimestamp(i+1, new java.sql.Timestamp(((java.util.Date) p_tr.getField(p_trColnames.elementAt(i).toString())).getTime()));
				        	 } else {
				        		st.setDate(paramIdx, new java.sql.Date(((java.util.Date) p_tr.getField(p_trColnames.elementAt(i).toString())).getTime()));
				        	 }
				         } else {
				            st.setObject(paramIdx, p_tr.getField(p_trColnames.elementAt(i).toString()));
				         }
						   logSb.append(","+p_tr.getField(p_trColnames.elementAt(i).toString()));
					   }
				   }
				}
			   if (fDebug) UniLog.logClass(this, "insertByTableRec(): "+sb.toString()+"["+logSb.toString()+"]");
	         cc = st.executeUpdate();
				if (serialIdx >= 0) {
		         if (st.getClass().getName().equals("com.uniinformation.scorpion.driver.ScpStatement")) {
                  Method method = st.getClass().getMethod("getLastsqlerrd1", new Class[0]);
	               p_tr.setField(serialIdx, 
                     ((Integer) method.invoke(st, new Object[0]))
						);
		         }
		         else {
			         try {
			            try (ResultSet insertedKeyResult = st.getGeneratedKeys()) {
			               if (insertedKeyResult != null && insertedKeyResult.next())
			                  p_tr.setField(serialIdx, insertedKeyResult.getObject(1));
			            }
						/*
			         } catch(NoSuchMethodException ex1) {
				         throw(new SQLException("getGeneratedKeys() NoSuchMethodException."));
			         } catch(IllegalAccessException ex2) {
				         throw(new SQLException("getGeneratedKeys() IllegalAccessException."));
			         } catch(InvocationTargetException ex3) {
				         throw(new SQLException("getGeneratedKeys() InvocationTargetException."));
			         }
						*/
			         } catch(Exception ex4) {
				         throw(ex4);
					   }
		         }
				}
				if (cc < 0)
				   break;
			}
	   } catch (Exception e) {
			//UniLog.log(e);
			UniLog.log1("error:" + e.getMessage());
			throw(e);
	   } finally {
		   closeTempConnection();
	      try {
	         if (st != null) st.close();
	      } catch (SQLException e) {
			   UniLog.log(e);
				throw(e);
         }
	   }
	   return(cc);
	}
   public int getSqlerrd1() {
	   return(sqlerrd1);
	}
   public CellVector getQueryResultToCellVector(String p_sqlstr, Wherecl p_wherecl, int p_maxcnt) throws Exception {
      return(getQueryResultWithMaxCountToCellVector(p_sqlstr, p_wherecl, false, p_maxcnt));
	}
   public CellVector getQueryResultToCellVector(String p_sqlstr, Wherecl p_wherecl) throws Exception {
      return(getQueryResultToCellVector(p_sqlstr, p_wherecl, false));
	}
   public CellVector getQueryResultToCellVector(String p_sqlstr, Wherecl p_wherecl, boolean p_withnull) throws Exception {
      return(getQueryResultWithMaxCountToCellVector(p_sqlstr, p_wherecl, p_withnull, -1));
	}
   public CellVector getQueryResultWithMaxCountToCellVector(String p_sqlstr, Wherecl p_wherecl, boolean p_withnull, int p_maxcnt) throws Exception {
      TableRec tr = getQueryResultWithMaxCount(p_sqlstr, p_wherecl, p_withnull, p_maxcnt);
		return(tr.toCellVector());
   }
   public CellCollection getPickResult(String p_sqlstr, Wherecl p_wherecl) throws Exception {
      TableRec tr = getQueryResult(p_sqlstr, p_wherecl);
		CellVector cv = new CellVector();
	   for (int i=0; i<tr.size(); i++) {
		   tr.setRecPointer(i);
			cv.addElement(new CellCollection()
			                  .putValue("value", tr.getField(0).toString()));
		}
	   CellCollection cc = new CellCollection();
		cc.addCollectionList("items", cv);
		return(cc);
	}
   public SelectUtilCursor getCursor(String p_sqlstr, Wherecl p_wherecl) throws Exception {
	   return(getCursor(p_sqlstr, p_wherecl, false));
	}
	public SelectUtilCursor getCursor(String p_sqlstr, Wherecl p_wherecl, boolean p_fWithNull) throws Exception {
	   SelectUtilCursor cursor = new SelectUtilCursor(this, p_sqlstr, p_wherecl, p_fWithNull);
		return(cursor);
	}
	public static void setDebugPrefix(String p_str) {
	   debugPrefix = p_str;
	}
	public boolean jdbcReconnect(){
	   if (connection != null && connection instanceof JdbcPool.ConnectionRecord) {
	  		return ((JdbcPool.ConnectionRecord) connection).reconnect();
	   }
	   UniLog.log("non ConnectionRecord instance. skip reconnect");
	   return(false);
	}
	
	
	
	public static void main(String argv[]) throws Exception {
	   SelectUtil su = new SelectUtil();
//	   Connection conn= DriverManager.getConnection("jdbc:sqlserver://neutrino353.cf3wzfqw5ffu.us-east-2.rds.amazonaws.com:1433","hellovoice","iied31cc");
//	   Connection conn= DriverManager.getConnection("jdbc:oracle:thin:@neutrino838.cf3wzfqw5ffu.us-east-2.rds.amazonaws.com:1521:ORCLTEST","hellovoice","iied31cc");
//	   Connection conn= DriverManager.getConnection("jdbc:mysql://neutrino579.cf3wzfqw5ffu.us-east-2.rds.amazonaws.com:3306/bi","hellovoice","iied31cc");
	   Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
	   Connection conn= DriverManager.getConnection("jdbc:scorpion:perfrpc:dtqemu2.uniconn.com:3102:gl:dbpath:/yic/v/afs/data:chaindir:-p /yic/v/afs/chn");
//	   DatabaseMetaData dbma = conn.getMetaData();
//	   String types[] = {"TABLE"};
//	   ResultSet rs = dbma.getTables(null, "HELLOVOICE", "%", types);
//	   for (boolean finished=false;;) {
//           finished = !(rs.next());
//			   if (finished) break;
//				UniLog.log("HAHA get one table from catalog " + rs.getString("TABLE_CAT") + " " + rs.getString("TABLE_SCHEM") + " " + rs.getString("TABLE_NAME"));
//				UniLog.log(rs.getString("TABLE_CAT") + " " + rs.getString("TABLE_SCHEM")+ " " + rs.getString("TABLE_NAME"));
//			}
//
	   su.setConnection(conn);
	   Wherecl wcl = new Wherecl();
	   wcl.appendString(" stm_date > ? ");
//	   wcl.appendArgument(new java.util.Date());
//	   wcl.appendArgument(DateUtil.getDate("2001/01/01"));
	   wcl.appendArgument("2001/01/01");
	   TableRec tr = su.getQueryResult("select * from stmov", wcl);
	   UniLog.log("HAHA " + tr.getRecordCount());
	}
//	public static void main_abc(String argv[]) throws Exception {
//	   SelectUtil su = new SelectUtil();
//		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
//      Connection conn= 
//                 DriverManager.getConnection("jdbc:scorpion:perfrpc:203.161.226.105:6002:crm");
//      //Connection conn = JdbcPool.getJdbcPool().getConnection();
//		su.setConnection(conn);
//		TableRec tr = su.getQueryResult("select * from vendor", null);
//		//UniLog.log("tr = "+tr.toXML());
//		String[] colnames = tr.getFieldNames();
//		StringBuffer sb = new StringBuffer();
//		for (int i=0; i<colnames.length; i++) {
//			sb.append(colnames[i]).append(" ");
//			sb.append(tr.getFieldType(colnames[i])).append(" ");
//			sb.append(tr.getColumnDisplaySize(colnames[i])).append(" ");
//		}
//		UniLog.log("sb="+sb.toString());
//		su.close();
//	}
	public static void mainXXX(String argv[]) throws Exception {
	   SelectUtil su = new SelectUtil();
		Class.forName(JdbcPool.MYSQL_DRIVER);
      Connection conn= 
                 DriverManager.getConnection("jdbc:mysql://192.168.233.57/crm?user=root&password=donlai");
                 //DriverManager.getConnection("jdbc:mysql:///crm?socketFactory=com.mysql.jdbc.NamedPipeSocketFactory&user=root&password=donlai");
      //Connection conn = JdbcPool.getJdbcPool().getConnection();
		su.setConnection(conn);
		/*
      su.executeUpdate("insert into staff(stf_userid) values (?)", 
		                  new Wherecl().appendArgument(argv[0]));
		*/
		TableRec tr = su.getQueryResult("select * from vendor", null);
		UniLog.log("tr="+tr.toXML());
		su.close();
	}
	public static void main_yyy(String argv[]) throws Exception {
	   SelectUtil su = new SelectUtil();
		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
      Connection conn= DriverManager.getConnection("jdbc:scorpion:perfrpc:203.161.226.105:6002:crm");
		su.setConnection(conn);
		TableRec tr = su.getQueryResult("select * from newsitem", null);
		UniLog.log("tr = "+tr.toXML());
		su.close();
	}
	public static void main_0e0e0(String argv[]) throws Exception {
	   SelectUtil su = new SelectUtil();
		// Class.forName(JdbcPool.MYSQL_DRIVER);
      // Connection conn= DriverManager.getConnection("jdbc:mysql://192.168.233.57/crm?user=root&password=donlai&useUnicode=TRUE&characterEncoding=UTF-8");
		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
      Connection conn= DriverManager.getConnection("jdbc:scorpion:perfrpc:203.161.226.105:6002:cp");
		su.setConnection(conn);
		TableRec tr = su.getQueryResult("select * from dmsfolder where dmsfd_dmsfdrg = 510", null);
		for (int i=0; i<tr.size(); i++) {
		   tr.setRecPointer(i);
			UniLog.log("dmsfd_name:0="+tr.getField("dmsfd_name").toString());
			UniLog.log("dmsfd_name:1="+StringUtil.convertWebString(tr.getField("dmsfd_name").toString()));
		}
		su.close();
	}
	public static void main_040912(String argv[]) throws Exception {
	   SelectUtil su = new SelectUtil();
		/*
		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
      Connection conn= DriverManager.getConnection("jdbc:scorpion:perfrpc:203.161.226.105:6002:crm");
		*/
		Class.forName(JdbcPool.MYSQL_DRIVER);
      Connection conn= DriverManager.getConnection("jdbc:mysql://192.168.233.57/crm?user=root&password=donlai&useUnicode=TRUE&characterEncoding=UTF-8");
		su.setConnection(conn);
		UniLog.log("getCursor(): started");
		SelectUtilCursor cursor = su.getCursor("select serial_id from tmpcontact", null);
		UniLog.log("getCursor(): returned");
		int cnt = 0;
		for (;;) {
		   TableRec tr = cursor.fetchNext();
		   if (tr == null)
			   break;
			cnt++;
			if (cnt % 100 == 0)
			   UniLog.log("cnt = "+cnt);
		}
		UniLog.log("cnt = "+cnt);
		su.close();
	}
	public static void main_040412A(String argv[]) throws Exception {
	   SelectUtil su = new SelectUtil();
		/*
		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
      Connection conn= DriverManager.getConnection("jdbc:scorpion:perfrpc:203.161.226.105:6002:crm");
		*/
		Class.forName(JdbcPool.MYSQL_DRIVER);
      Connection conn= DriverManager.getConnection("jdbc:mysql://192.168.233.57/crm?user=root&password=donlai&useUnicode=TRUE&characterEncoding=UTF-8");
		su.setConnection(conn);
		UniLog.log("getQueryResult(): started");
		TableRec tr = su.getQueryResult("select serial_id from tmpcontact", null);
		UniLog.log("getQueryResult(): returned");
		UniLog.log("cnt = "+tr.size());
		su.close();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/tmp/object.out"));
		UniLog.log("writeObject started");
		oos.writeObject(tr);
		UniLog.log("writeObject ended");
		oos.close();
		Runtime.getRuntime().gc();
	}
	public static void main_040412B(String argv[]) throws Exception {
		ObjectIndexedFile oif = new ObjectIndexedFile();
	   SelectUtil su = new SelectUtil();
		/*
		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
      Connection conn= DriverManager.getConnection("jdbc:scorpion:perfrpc:203.161.226.105:6002:crm");
		*/
		Class.forName(JdbcPool.MYSQL_DRIVER);
      Connection conn= DriverManager.getConnection("jdbc:mysql://192.168.233.57/crm?user=root&password=donlai&useUnicode=TRUE&characterEncoding=UTF-8");
		su.setConnection(conn);
		UniLog.log("getCursor(): started");
		SelectUtilCursor cursor = su.getCursor("select serial_id from tmpcontact", null);
		//SelectUtilCursor cursor = su.getCursor("select * from tmpcontact", null);
		UniLog.log("getCursor(): returned");
		int cnt = 0;
		TableRec lastTr = null;
		long t0 = DateUtil.now().getTime();
		for (;;) {
		   TableRec tr = cursor.fetchNext();
		   if (tr == null)
			   break;
		   oif.addObject(tr);
			cnt++;
			/*
			if (cnt % 100 == 0)
			   UniLog.log("fetchNext cnt = "+cnt);
		   */
		}
		UniLog.log("elapsed (ms) = "+(DateUtil.now().getTime()-t0));
		UniLog.log("fetchNext cnt = "+cnt);

		t0 = DateUtil.now().getTime();
		for (int i=0; i<cnt; i++) {
		   TableRec tr = (TableRec) oif.getObject(i);
		   TableRec tr1 = su.getQueryResult("select * from tmpcontact where serial_id = "+tr.getField("serial_id"), null);
			//UniLog.log("["+i+"] tr.getField(serial_id) = "+tr.getField("serial_id"));
		}
		UniLog.log("getObject elapsed (ms) = "+(DateUtil.now().getTime()-t0));

		oif.close();
		su.close();
	}
	public static void main_040825(String argv[]) throws Exception {
		ObjectIndexedFile oif = new ObjectIndexedFile();
	   SelectUtil su = new SelectUtil();
		Class.forName(JdbcPool.MYSQL_DRIVER);
      Connection conn= DriverManager.getConnection("jdbc:mysql://192.168.233.57/crm?user=root&password=donlai&useUnicode=TRUE&characterEncoding=UTF-8");
		su.setConnection(conn);
		UniLog.log("getCursor(): started");
		long t0 = DateUtil.now().getTime();
		TableRec tr = su.getQueryResult("select serial_id from tmpcontact", null);
		UniLog.log("getQueryResult(): returned");
		UniLog.log("getQueryResult() elapsed (ms) = "+(DateUtil.now().getTime()-t0));
		int cnt = tr.size();
		UniLog.log("tr.size() = "+cnt);
		t0 = DateUtil.now().getTime();
		long[] serArr = new long[cnt];
		for (int i=0; i<cnt; i++) {
		   tr.setRecPointer(i);
		   serArr[i] = ((Long) tr.getField("serial_id")).longValue();
		}
		UniLog.log("setup serArr elapsed (ms) = "+(DateUtil.now().getTime()-t0));

		t0 = DateUtil.now().getTime();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/tmp/serArr.obj"));
      oos.writeObject(serArr);
		oos.close();
		UniLog.log("write serArr elapsed (ms) = "+(DateUtil.now().getTime()-t0));

		t0 = DateUtil.now().getTime();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/tmp/serArr.obj"));
      serArr = (long[]) ois.readObject();
		ois.close();
		UniLog.log("read serArr elapsed (ms) = "+(DateUtil.now().getTime()-t0));
		UniLog.log("serArr.length="+serArr.length);

		t0 = DateUtil.now().getTime();
		for (int i=0; i<cnt; i += 100) {
			StringBuffer sb = new StringBuffer();
			int thiscnt = (i+100 < cnt) ? 100 : (cnt-i);
		   for (int j=0; j<thiscnt; j++) {
			   if (j > 0)
				   sb.append(",");
			   sb.append(serArr[i+j]);
			}
		   TableRec tr1 = su.getQueryResult("select * from tmpcontact where serial_id in ("+sb.toString()+")", null);
			tr1.setRecPointer(0);
			//UniLog.log("["+i+"("+tr1.size()+")"+"] tr1.getField(serial_id) = "+tr1.getField("serial_id"));
		}
		UniLog.log("select 100 by 100 elapsed (ms) = "+(DateUtil.now().getTime()-t0));

		oif.close();
		su.close();
	}
	public static boolean setDebug(boolean p_flag) {
		boolean flag = fDebug;
	   fDebug = p_flag;
		return(flag);
	}
	public static void loadJdbcDrivers() {
	   String driverListFile = JDBC_DRIVER_CLASS_LIST_FILE;
		Map env = System.getenv();
	   if (env.get("JDBC_DRIVER_LIST") != null && !env.get("JDBC_DRIVER_LIST").toString().trim().equals("")) {
	      driverListFile = env.get("JDBC_DRIVER_LIST").toString().trim();
		}
      LineNumberReader lnr = null;
      try {
         UniLog.log("SelectUtil: Loading "+driverListFile+" ...");
         lnr = new LineNumberReader(new FileReader(driverListFile));
         for (;;) {
            String line = lnr.readLine();
            if (line == null)
               break;
            if (line.trim().startsWith("#"))
               continue;
            try {
					Class.forName(line.trim()).newInstance();
            } catch (Exception ex) {
               UniLog.log("Continue ...");
               UniLog.log(ex);
            }
         }
//      } catch (Exception ex) {
      } catch (IOException ex) {
         UniLog.log("JDBC Driver List File open failed. Continue with scoprion jdbc driver");
//         UniLog.log(ex);
         try {
//					Class.forName("com.uniinformation.scorpion.driver.ScpDriver").newInstance();
					loadJdbcDriver("com.uniinformation.scorpion.driver.ScpDriver");
//					Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  250821 to be added back later
         } catch (Exception ex2) {
               UniLog.log("Continue ...");
               UniLog.log(ex2);
         }
	      } finally {
	         if (lnr != null) {
	            try { lnr.close(); } catch (Exception ex1) {};
	         }
	      }
		// JDBC 4 normally auto-registers these drivers, but explicit loading keeps
		// compatibility with the application's legacy driver bootstrap mechanism.
		loadJdbcDriver(JdbcPool.MYSQL_DRIVER);
		loadJdbcDriver(JdbcPool.POSTGRESQL_DRIVER);
	}
	private static void loadJdbcDriver(String p_driverClass) {
		try {
			Class.forName(p_driverClass);
		} catch (Exception ex) {
			UniLog.log("JDBC driver not available: " + p_driverClass);
		}
	}
	
	public void setPoolable(boolean flag) {
		poolable = flag;
	}
	public void setLoginId(String p_loginId) {
		loginId = p_loginId;
	}
	public String getLoginId() {
		return(loginId);
	}
	public static void main_db2(String argv[]) throws Exception {
	   SelectUtil su = new SelectUtil();
		//Class.forName("COM.ibm.db2.jdbc.net.DB2Driver").newInstance();
		Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
      Connection conn = DriverManager.getConnection("jdbc:db2://210.176.61.113:5015/"+argv[0], argv[1], argv[2]);
		su.setConnection(conn);
		UniLog.log(conn.getClass().getName());
		//TableRec tr = su.getQueryResult("select * from outwork", null);
		TableRec tr = su.getQueryResult("select count(*) from syscat.tables", null);
		UniLog.log("tr="+tr.toXML());
		su.close();
	}
	public static void main_filemaker(String argv[]) throws Exception {
      Driver d = (Driver) Class.forName("com.ddtek.jdbc.sequelink.SequeLinkDriver")
		                    .newInstance();
      Connection conn = DriverManager.getConnection(
		   "jdbc:sequelink://127.0.0.1:2399"
			// +";serverDataSource=intouch%95olb"
			// +";HUser=wklai;HPassword=hellovoice"
			+";user=wklai;password=hellovoice"
			);
		UniLog.log(conn.getClass().getName());
		UniLog.log("conn.getCatalog()="+conn.getCatalog());
		DatabaseMetaData metaData = conn.getMetaData();
		UniLog.log("conn.getMetaData()="+metaData);
		UniLog.log("metaData.getTableTypes()="+metaData.getTableTypes());
		ResultSet tablesRs = metaData.getTables(null, null, "%", null);
	   SelectUtil su = new SelectUtil();
		su.setConnection(conn);
		/*
      TableRec ttTr = su.getQueryResultByResultSet(metaData.getTableTypes());
		UniLog.log("ttTr="+ttTr.toXML());
      TableRec tsTr = su.getQueryResultByResultSet(tablesRs);
		UniLog.log("tsTr="+tsTr.toXML());
		//su.executeUpdate("create table t3 (c1 varchar(10))", null);
		//su.executeUpdate("insert into t3 (c1) values ('11111')", null);
		*/
		TableRec tr = su.getQueryResult("select * from t3", null);
		UniLog.log("tr="+tr.toXML());
		/*
		TableRec tr = su.getQueryResult("select * from intouch_olb", null);
		UniLog.log("tr="+tr.toXML());
		//su.executeUpdate("create table t1 (c1 varchar(10))", null);
		//su.executeUpdate("insert into t1 (c1) values ('12345')", null);
		TableRec tr = su.getQueryResult("select * from 'Asset Management'", null);
		UniLog.log("tr="+tr.toXML());
		*/
		/*
		su.executeUpdate(
         "create table amdmaster ("
         +"  amdm_amdmid double default NULL,"
         +"  amdm_cuser varchar(20) default NULL,"
         +"  amdm_cdate date default NULL,"
         +"  amdm_jobnumber varchar(64) default NULL,"
         +"  amdm_brief varchar,"
         +"  amdm_schedtype varchar(64) default NULL,"
         +"  amdm_awsched date default NULL,"
         +"  amdm_reprosched date default NULL,"
         +"  amdm_apprvlsched date default NULL,"
         +"  amdm_schedproddesc varchar,"
         +"  amdm_epapnumber varchar(64) default NULL,"
         +"  amdm_supplyunit varchar(64) default NULL,"
         +"  amdm_maincontact varchar(64) default NULL,"
         +"  amdm_maincontacttel varchar(64) default NULL,"
         +"  u937_uuser varchar(20) default '',"
         +"  u937_udate date default NULL,"
         +"  u937_utime double default '0',"
         +"  u937_ctime double default '0',"
         +"  amdm_companyname varchar(65) default NULL,"
         +"  amdm_location varchar(65) default NULL,"
         +"  amdm_emailaddr varchar(65) default NULL,"
         +"  amdm_contactname varchar(65) default NULL,"
         +"  amdm_phonenumber varchar(65) default NULL,"
         +"  amdm_fcutterchange varchar(1) default NULL,"
         +"  amdm_cutterchgcat varchar(1) default NULL,"
         +"  amdm_fvarcharcontentmpc varchar(1) default NULL,"
         +"  amdm_fvarcharcontentlegal varchar(1) default NULL,"
         +"  amdm_fprinterchange varchar(1) default NULL,"
         +"  amdm_existingprintercode varchar(20) default NULL,"
         +"  amdm_txtctmpcfileid varchar(80) default NULL,"
         +"  amdm_txtctmpcfilename varchar(120) default NULL,"
         +"  amdm_txtctlegalfileid varchar(80) default NULL,"
         +"  amdm_txtctlegalfilename varchar(120) default NULL"
         +")"
		   , null
      );
		su.executeUpdate(
         "create table amddet ("
         +"  amdd_amdmid double default NULL,"
         +"  amdd_command varchar(10) default NULL,"
         +"  amdd_joblineref varchar(64) default NULL,"
         +"  amdd_flinedesc varchar(1) default NULL,"
         +"  amdd_linedesc varchar,"
         +"  amdd_fsapref varchar(1) default NULL,"
         +"  amdd_sapref varchar(64) default NULL,"
         +"  amdd_fcutterref varchar(1) default NULL,"
         +"  amdd_cutterref varchar(64) default NULL,"
         +"  amdd_fbarcoderef varchar(1) default NULL,"
         +"  amdd_barcoderef varchar(64) default NULL,"
         +"  amdd_fpackspec varchar(1) default NULL,"
         +"  amdd_packspec varchar(64) default NULL,"
         +"  amdd_amdfid0 varchar(80) default NULL,"
         +"  amdd_amdfilename0 varchar(120) default NULL,"
         +"  amdd_amdfid1 varchar(80) default NULL,"
         +"  amdd_amdfilename1 varchar(120) default NULL,"
         +"  amdd_amdfid2 varchar(80) default NULL,"
         +"  amdd_amdfilename2 varchar(120) default NULL"
         +")"
		   , null
      );
		*/
	}
	public static void main_20080327(String argv[]) throws Exception {
	   main_filemaker(argv);
	}
//	public static void main(String argv[]) throws Exception {
//		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
//      Connection conn = DriverManager.getConnection("jdbc:scorpion:perfrpc:192.168.1.23:5033:cp");
//	   SelectUtil su = new SelectUtil();
//	   su.setConnection(conn);
//	   Wherecl wc = new Wherecl();
//		Vector v = new Vector();
//      v.addElement(new Integer(-1316));
//      v.addElement(new Integer(-937));
//      v.addElement(new Integer(-938));
//      v.addElement(new Integer(-940));
//      v.addElement(new Integer(-941));
//      v.addElement(new Integer(-751));
//      v.addElement(new Integer(-943));
//      v.addElement(new Integer(-945));
//      v.addElement(new Integer(-946));
//      v.addElement(new Integer(-948));
//      v.addElement(new Integer(-949));
//      v.addElement(new Integer(-952));
//      v.addElement(new Integer(-1337));
//      v.addElement(new Integer(-1));
//      v.addElement(new Integer(-956));
//      v.addElement(new Integer(-2));
//      v.addElement(new Integer(-4));
//      v.addElement(new Integer(-960));
//      v.addElement(new Integer(-961));
//      v.addElement(new Integer(-6));
//      v.addElement(new Integer(-962));
//      v.addElement(new Integer(-7));
//      v.addElement(new Integer(-8));
//      v.addElement(new Integer(-9));
//      v.addElement(new Integer(-964));
//      v.addElement(new Integer(-965));
//      v.addElement(new Integer(-967));
//      v.addElement(new Integer(-776));
//      v.addElement(new Integer(-777));
//      v.addElement(new Integer(-1351));
//      v.addElement(new Integer(-1352));
//      v.addElement(new Integer(-971));
//      v.addElement(new Integer(-781));
//      v.addElement(new Integer(-973));
//      v.addElement(new Integer(-1360));
//      v.addElement(new Integer(-787));
//      v.addElement(new Integer(-1361));
//      v.addElement(new Integer(-979));
//      v.addElement(new Integer(-788));
//      v.addElement(new Integer(-1362));
//      v.addElement(new Integer(-980));
//      v.addElement(new Integer(-1363));
//      v.addElement(new Integer(-790));
//      v.addElement(new Integer(-1364));
//      v.addElement(new Integer(-1366));
//      v.addElement(new Integer(-793));
//      v.addElement(new Integer(-986));
//      v.addElement(new Integer(-987));
//      v.addElement(new Integer(-1372));
//      v.addElement(new Integer(-992));
//      v.addElement(new Integer(-801));
//      v.addElement(new Integer(-804));
//      v.addElement(new Integer(-806));
//      v.addElement(new Integer(-807));
//      v.addElement(new Integer(-808));
//      v.addElement(new Integer(-1385));
//      v.addElement(new Integer(-812));
//      v.addElement(new Integer(-813));
//      v.addElement(new Integer(-814));
//      v.addElement(new Integer(-815));
//      v.addElement(new Integer(-816));
//      v.addElement(new Integer(-817));
//      v.addElement(new Integer(-819));
//      v.addElement(new Integer(-820));
//      v.addElement(new Integer(-821));
//      v.addElement(new Integer(-822));
//      v.addElement(new Integer(-1398));
//      v.addElement(new Integer(-445));
//      v.addElement(new Integer(-447));
//      v.addElement(new Integer(-450));
//      v.addElement(new Integer(-453));
//      v.addElement(new Integer(-840));
//      v.addElement(new Integer(-844));
//      v.addElement(new Integer(-845));
//      v.addElement(new Integer(-855));
//      v.addElement(new Integer(-856));
//      v.addElement(new Integer(-857));
//      v.addElement(new Integer(-858));
//      v.addElement(new Integer(-859));
//      v.addElement(new Integer(-860));
//      v.addElement(new Integer(-861));
//      v.addElement(new Integer(-862));
//      v.addElement(new Integer(-866));
//      v.addElement(new Integer(-867));
//      v.addElement(new Integer(-868));
//      v.addElement(new Integer(-869));
//      v.addElement(new Integer(-870));
//      v.addElement(new Integer(-875));
//      v.addElement(new Integer(-880));
//      v.addElement(new Integer(-881));
//      v.addElement(new Integer(-882));
//      v.addElement(new Integer(-884));
//      v.addElement(new Integer(-885));
//      v.addElement(new Integer(-910));
//      v.addElement(new Integer(-914));
//      v.addElement(new Integer(-916));
//      v.addElement(new Integer(-920));
//      v.addElement(new Integer(-925));
//      v.addElement(new Integer(-927));
//      v.addElement(new Integer(-738));
//		wc.andList(su, "dmsfolder.dmsfd_dmsfdrg", v.elements());
//	   TableRec tr = su.getQueryResult("select dmsfolder.dmsfd_updmsfdrg,dmsfolder.dmsfd_name,dmsfolder.dmsfd_dpuid,dmsfolder.dmsfd_dmsfdrg,dmsfolder.dmsfd_nameexp,dmsfolder.dmsfd_attrvmid,dmsfolder.dmsfd_wfoid,dmsfolder.dmsfd_type,dmsfolder.serial_id,dmsfolder.dmsfd_name64 from dmsfolder", wc);
//	   UniLog.log("tr.size()="+tr);
//	}
	
	public RpcClient getRpcClient()
	{
		if(connection == null) return(null);
		Connection realConnection = null;
		if(connection instanceof com.uniinformation.utils.JdbcPool.ConnectionRecord) {
			realConnection = ((com.uniinformation.utils.JdbcPool.ConnectionRecord ) connection).getConnection();
		} else {
			realConnection = connection;
		}
		if(realConnection != null )  {
			if(realConnection instanceof com.uniinformation.scorpion.driver.ScpConnection) {
				return(((com.uniinformation.scorpion.driver.ScpConnection) realConnection).getPeerrpc());
			} else {
				
			}
		}
		return(null);
	}
	
	public boolean getAutoCommit() throws Exception {
		return(connection.getAutoCommit());
	}
	public void setAutoCommit(boolean p_sw) throws Exception {
		if (connection != null){
			connection.setAutoCommit(p_sw);
		}
	}
	public void commit() throws Exception {
		if (connection != null){
			connection.commit();
		}
	}
	public void rollback() throws Exception {
		if (connection != null){
			connection.rollback();
		}
	}
	
	public void setAliveUntil(int p_msec) {
		if(connection == null) return;
		if(connection instanceof JdbcPool.ConnectionRecord) {
			((JdbcPool.ConnectionRecord) connection).setAliveUntilTime(p_msec);
		}
	}
	/***
	 * obtain a blank tablerec for record insert
	 * @param p_tabName
	 * @throws Exception
	 */
	public TableRec getBlankTr(String p_tabName) throws Exception{
		TableRec tr = getQueryResult("select * from " + p_tabName, new Wherecl().andUniop("1", "=", "2").stripAnd());
		if (tr.size() > 0){
			throw new Exception("Wherecl error");
		}
		tr.addRecord();
		return(tr);
	}
	
	public void setAllowNull(boolean p_sw) {
		allowNull = p_sw;
	}
}
