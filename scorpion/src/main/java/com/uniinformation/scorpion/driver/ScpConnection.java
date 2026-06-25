package com.uniinformation.scorpion.driver;
import com.kyoko.common.CoreLog;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

public class ScpConnection extends RpcServletClass implements Connection {
    public static int DELAY_CLOSE_MAXCNT = 10;
     static final int CONNECTION_IDLE_TIMEOUT = 900000;
	 RpcClient peerrpc;
	 ScpStatement currstmt;
	 Hashtable statementHash;
	 private int version;
	 int delayCloseCnt = 0;
	 Properties clientInfo = null;
	 ScpDbMeta dbmeta = null;
	 String dbname;
	 boolean autocommit = true;
	 /*
	 public class Savepoint {  // workaround for jdk 1.4 down to 1.3
	 }
	 */
	 public String rpcCallback(Vector v) throws Exception
    {
	 	int i,n,j,m;
		n = v.size();
		for(i=0;i<n;i+=2) {
			j = ((Integer) v.get(i)).intValue();
			//  CoreLog.log("rpcCallback arg " + i + " cmd " + j);
			switch(j) {
			case 1 : currstmt.setLastsqlcode(((Integer) v.get(i+1)).intValue()); break;
			case 2 : currstmt.setLastsyntaxerrpos(((Integer) v.get(i+1)).intValue()); break;
			case 3 : currstmt.setLasterrmsg(((String) v.get(i+1))); break;
			case 4 : m = ((Integer) v.get(i+1)).intValue();
						currstmt.setSelectlist(i+2,m,v);
						i+=m*3;
						break;
			case 5 : m = ((Integer) v.get(i+1)).intValue();
						currstmt.setSelectrecord(i+2,m,v);
						i+=m;
						break;
			case 6 : currstmt.setLastsqlerrd1(((Integer) v.get(i+1)).intValue());
			         break;
			case 7 : currstmt.setLastsqlerrd2(((Integer) v.get(i+1)).intValue());
			         break;
			}
		}
		return("OK");
	 }
	 public void setCurrstmt(ScpStatement p_stmt)
	 {
	 	currstmt = p_stmt;
	 }
	 public RpcClient getPeerrpc()
	 {
	 	return(peerrpc);
	 }
	 public void scpGetdbschema() throws SQLException
	 {
		ResultSet resultset;
		/*
	 	PreparedStatement preparedstatement;
		preparedstatement = prepareStatement(
			"select tabname,colno,colname,coltype,collength " +
			"from systables,syscolumns " +
			"where syscolumns.tabid = systables.tabid " +
			"order by tabname,colno"
		);
		*/
		/*
		Statement statement;
		statement = createStatement();
		resultset = statement.executeQuery(
				"select * from mnotice where mn_type = 'omn'"
			);
		*/

		/*
		PreparedStatement statement;

		//statement = prepareStatement( "select * from scp_bill ");
		statement = prepareStatement(
"select areadef0.serial_id,areadef0.create_host,areadef0.unique_id,areadef0.update_host,areadef0.version_id,areadef0.update_id,areadef0.nextupd_host,areadef0.timestamp,areadef0.ad_name,areadef0.ad_description,areadef0.ad_timezone,areadef0.ad_gmtoffset,areadef0.ad_teldigit,  areadef0.serial_id from areadef areadef0 where areadef0.serial_id = ? ");
		statement.setObject(1,new Integer(1));
		resultset = statement.executeQuery();
		{
		int i;
	 	for(i=0;;i++) {
			if(resultset.next() == true) {
				CoreLog.log("Record "+i+" "+
								resultset.getString(0)+
								resultset.getInt(1)+
								resultset.getString(2)+
								resultset.getInt(3)+
								resultset.getInt(4));
			} else break;
	 	}	
		CoreLog.log("(1)Total " + i + "Record Fetched");
		}
		statement.setObject(1,new Integer(2));
		resultset = statement.executeQuery();
		{
		int i;
	 	for(i=0;;i++) {
			if(resultset.next() == true) {
				CoreLog.log("Record "+i+" "+
								resultset.getString(0)+
								resultset.getInt(1)+
								resultset.getString(2)+
								resultset.getInt(3)+
								resultset.getInt(4));
			} else break;
	 	}	
		CoreLog.log("(2)Total " + i + "Record Fetched");
		}
		*/
		/*
		resultset = statement.executeQuery(
				"select tabname,colno,colname,coltype,collength " +
				"from systables,syscolumns " +
				"where syscolumns.tabid = systables.tabid " +
				"order by tabname,colno"
			);
		{
		int i;
	 	for(i=0;;i++) {
			if(resultset.next() == true) {
				CoreLog.log("Record "+i+" "+
								resultset.getString(0)+
								resultset.getInt(1)+
								resultset.getString(2)+
								resultset.getInt(3)+
								resultset.getInt(4));
			} else break;
	 	}	
		CoreLog.log("Total " + i + "Record Fetched");
		}
		resultset.close();
		resultset = null;
		statement.close();
		statement = null;
		*/
	 }

	 public ScpConnection (RpcClient p_rpcclient) throws SQLException
	 {
	 	peerrpc = p_rpcclient;
	 	statementHash = new Hashtable();
		dbmeta = new ScpDbMeta(this);
	 }
    public Statement createStatement() throws SQLException {
	    return(new ScpStatement(this));
	 }
    public PreparedStatement prepareStatement(String sql) throws SQLException {
	    ScpStatement stmt = null;
		 /*
       Sun Mar 30 11:34:09 HKG 2008 lai: disable reuse of statement
		 synchronized (this) {
		    Vector v = (Vector) statementHash.get(sql);
			 if (v != null && v.size() > 0) {
			    stmt = (ScpStatement) v.remove(0);
			    if (v.size() == 0) {
		          statementHash.remove(sql);
			    }
	          delayCloseCnt--;
			 }
		 }
		 */
		 if (stmt == null)
			 return(new ScpStatement(this, sql));
		 CoreLog.logClass(stmt,"Reuse statement from hash["+delayCloseCnt+"]:"+stmt.getStatementId()+":"+stmt.getSqlString());

		 return(stmt);
	 }
    public CallableStatement prepareCall(String sql) throws SQLException {
		 throw(new SQLException("Not yet implemented 1"));
	 }
    public String nativeSQL(String sql) throws SQLException {
		 throw(new SQLException("Not yet implemented 2"));
	 }
    public void setAutoCommit(boolean p_autoCommit) throws SQLException {
//		 throw(new SQLException("Not yet implemented 3"));
   		if(getAutoCommit() != p_autoCommit) {
//   			CoreLog.log("set autocommit to " + p_autoCommit);
   			/*
   			ScpStatement stmt = new ScpStatement(this);
   			if(p_autoCommit) {
   				stmt.executeUpdate("commit work");
   			} else {
   				stmt.executeUpdate("begin work");
   			}
   			stmt.close();
   			*/
   			if(p_autoCommit) {
   				peerrpc.callSegment("callfunction",new VectorUtil().addElement("commitwork").toVector());
   			} else {
   				peerrpc.callSegment("callfunction",new VectorUtil().addElement("beginwork").toVector());
   			}
   			if(version <= 3) autocommit = p_autoCommit;
   		}
	 }
    public boolean getAutoCommit() throws SQLException {
    	if(version > 3) {
    		Vector arglist = new Vector();
    		arglist.addElement("jdbc_isbegin");
    		Value v = peerrpc.callSegment("callfunction",arglist);
    		if(v == null) {
    			throw new SQLException("Peer Disconnect");
    		} else {
    			return(v.toInt() == 0); // not begin work => autocommit on
    		}
    	} else {
    		return(autocommit);
    	}
	 }
    public void commit() throws SQLException {
   		if(!getAutoCommit()) {
   			/*
   			ScpStatement stmt = new ScpStatement(this);
  			stmt.executeUpdate("commit work");
  			stmt.executeUpdate("begin work");
   			stmt.close();
   			*/
  			peerrpc.callSegment("callfunction",new VectorUtil().addElement("commitwork").toVector());
  			peerrpc.callSegment("callfunction",new VectorUtil().addElement("beginwork").toVector());
   		}
	 }
    public void rollback() throws SQLException {
   		if(!getAutoCommit()) {
   			/*
   			ScpStatement stmt = new ScpStatement(this);
  			stmt.executeUpdate("rollback work");
  			stmt.executeUpdate("begin work");
   			stmt.close();
   			*/
   			/* free and close all hashed Statement before rollback */
   			/* added by DT on 2020-03-16 */
   			keepAlive();
  			peerrpc.callSegment("callfunction",new VectorUtil().addElement("rollbackwork").toVector());
  			peerrpc.callSegment("callfunction",new VectorUtil().addElement("beginwork").toVector());
   		}
	 }
    public void close() throws SQLException {
		synchronized (this) {
	      if (peerrpc != null) {
			   peerrpc.close();
			   peerrpc = null;
		   }
		}
	 }
    public boolean isClosed() throws SQLException {
	    return(peerrpc == null);
	 }
    public DatabaseMetaData getMetaData() throws SQLException {
	 	 
//		 throw(new SQLException("Not yet implemented"));
    	return(dbmeta);
	 }
    public void setReadOnly(boolean readOnly) throws SQLException {
		 throw(new SQLException("Not yet implemented 4"));
	 }
    public boolean isReadOnly() throws SQLException {
	    return(false);
	 }
    public void setCatalog(String catalog) throws SQLException {
		 throw(new SQLException("Not yet implemented 5"));
	 }
    public String getCatalog() throws SQLException {
		 throw(new SQLException("Not yet implemented 6"));
	 }

    /**
     * Dirty reads, non-repeatable reads and phantom reads can occur.
     * This level allows a row changed by one transaction to be read
     * by another transaction before any changes in that row have been
     * committed (a "dirty read").  If any of the changes are rolled back, 
     * the second transaction will have retrieved an invalid row.
    int TRANSACTION_READ_UNCOMMITTED = 1 {
	 }
     */

    /**
     * Dirty reads are prevented {
	  }non-repeatable reads and phantom
     * reads can occur.  This level only prohibits a transaction
     * from reading a row with uncommitted changes in it.
    int TRANSACTION_READ_COMMITTED   = 2;
     */

    /**
     * Dirty reads and non-repeatable reads are prevented; phantom
     * reads can occur.  This level prohibits a transaction from
     * reading a row with uncommitted changes in it, and it also
     * prohibits the situation where one transaction reads a row,
     * a second transaction alters the row, and the first transaction
     * rereads the row, getting different values the second time
     * (a "non-repeatable read").
    int TRANSACTION_REPEATABLE_READ  = 4;
     */

    /**
     * Dirty reads, non-repeatable reads and phantom reads are prevented.
     * This level includes the prohibitions in
     * TRANSACTION_REPEATABLE_READ and further prohibits the 
     * situation where one transaction reads all rows that satisfy
     * a WHERE condition, a second transaction inserts a row that
     * satisfies that WHERE condition, and the first transaction
     * rereads for the same condition, retrieving the additional
     * "phantom" row in the second read.
    int TRANSACTION_SERIALIZABLE     = 8;
     */

    public void setTransactionIsolation(int level) throws SQLException {
		 throw(new SQLException("Not yet implemented 7"));
	 }
    public int getTransactionIsolation() throws SQLException {
	    return(0);
	 }
    public SQLWarning getWarnings() throws SQLException {
		 throw(new SQLException("Not yet implemented 8"));
	 }
    public void clearWarnings() throws SQLException {
	 }


    //--------------------------JDBC 2.0-----------------------------

    public Statement createStatement(int resultSetType, int resultSetConcurrency) 
      throws SQLException {
		   throw(new SQLException("Not yet implemented 9"));
		}

     public PreparedStatement prepareStatement(String sql, int resultSetType, 
					int resultSetConcurrency)
       throws SQLException {
		   throw(new SQLException("Not yet implemented 10"));
		 }

    public CallableStatement prepareCall(String sql, int resultSetType, 
				 int resultSetConcurrency) throws SQLException {
		   throw(new SQLException("Not yet implemented 11"));
	 }
    public java.util.Map getTypeMap() throws SQLException {
		   throw(new SQLException("Not yet implemented 12"));
	 }
    public void setTypeMap(java.util.Map map) throws SQLException {
		 throw(new SQLException("Not yet implemented 13"));
	 }
	 public void hashStatement(ScpStatement p_stmt) {
		 synchronized (this) {
			if(p_stmt.getSqlString() == null) return;
	 	    Vector v = (Vector) statementHash.get(p_stmt.getSqlString());
			 if (v == null) {
			    v = new Vector();
				 statementHash.put(p_stmt.getSqlString(), v);
			 }
		    v.addElement(p_stmt);
	       delayCloseCnt++;
		 }
//		 CoreLog.logClass(this, "delayCloseCnt="+delayCloseCnt);
		 if (delayCloseCnt > DELAY_CLOSE_MAXCNT) {
		    try {
		 		 keepAlive();
		    } catch (Exception ex) {
				 CoreLog.log(ex);
		    }
		 }
	 }
	 public boolean keepAlive() throws SQLException {
	 	//CoreLog.log("Scp KeepAliver Version " +  version);
	 	if (version >= 2) {
			synchronized (this) {
			   Vector arglist = new Vector();
			   Value v;
			   StringBuffer sb = new StringBuffer();
			   arglist.addElement("jdbc_close_multistatement");
			   for (Enumeration e = statementHash.elements(); e.hasMoreElements();) {
				   Vector vec = (Vector) e.nextElement();
				   for (int i=0; i<vec.size(); i++) {
				      ScpStatement st = (ScpStatement) vec.elementAt(i);
				      if (st.getStatementId() > 0)
				         arglist.addElement(new Integer(st.getStatementId()));
					   sb.append(", ").append(st.getStatementId());
				      st.resetStatementId();
				   }
			   }
			   statementHash.clear();
	         delayCloseCnt = 0;
//			   CoreLog.logClass(this, "keepAlive():multi close statement:"+sb.toString());
				v = getPeerrpc().callSegment("callfunction",arglist);
				if (v == null) {
				   throw(new SQLException("keep alive failed"));
				}
			}
			return(true);
		}
		else {
			return(false);
		}
	 }
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) 
	    throws SQLException {
		 throw(new SQLException("not yet implemented"));
	 }
    public PreparedStatement prepareStatement(String sql, String[] columnNames) 
	    throws SQLException {
		 throw(new SQLException("not yet implemented"));
	 }
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) 
	    throws SQLException {
		 throw(new SQLException("not yet implemented"));
	 }
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) 
	    throws SQLException {
		/*
		 throw(new SQLException("not yet implemented"));
	 	*/
		return(prepareStatement(sql));
	 }
    public Savepoint setSavepoint() throws SQLException {
	    throw(new SQLException("Not yet implemented 14"));
	 }
    public Savepoint setSavepoint(String name) throws SQLException {
	    throw(new SQLException("Not yet implemented 15"));
	 }
    public CallableStatement prepareCall(String sql, int resultSetType, 
				 int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		 throw(new SQLException("Not yet implemented 16"));
	 }
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) 
      throws SQLException {
		   throw(new SQLException("Not yet implemented 17"));
		}
    public void rollback(Savepoint savepoint) throws SQLException {
		   throw(new SQLException("Not yet implemented 18"));
	 }
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		   throw(new SQLException("Not yet implemented 19"));
	 }
    public void setHoldability(int holdability) throws SQLException {
		   throw(new SQLException("Not yet implemented 20"));
	 }
    public int getHoldability() throws SQLException {
		   throw(new SQLException("Not yet implemented 21"));
	 }
	 public void setVersion(int p_version) {
	    version = p_version;
	 }
	 public int getVersion() {
	    return(version);
	 }
	 public boolean useDelayClose() {
	 	 return(version >= 2);
	 }
	 public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		 throw(new SQLException("Not yet implemented 22"));
	 }
	 public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		 throw(new SQLException("Not yet implemented 23"));
	 }
    public Properties getClientInfo() throws SQLException {
		 throw(new SQLException("Not yet implemented 34"));
	 }
	 public String getClientInfo(String name) throws SQLException {
		 throw(new SQLException("Not yet implemented 25"));
	 }
	 public void setClientInfo(Properties properties) {
	    clientInfo = properties;
	 }
	 public void setClientInfo(String name, String value) {
		if(name != null && name.equals("perfuser")) {
			Vector arglist = new Vector();
			arglist.addElement("PERFUSER="+value);
			peerrpc.callSegment("jdbc_putenv",arglist);
		}
	 }
	 public boolean isValid(int timeout) throws SQLException {
		 java.util.Date d1 = new java.util.Date();
		 java.util.Date d0 = peerrpc.getConnection().getLastCallTime();
		 if(d0 == null) return(true);
		 if(d1.getTime() - d0.getTime() < CONNECTION_IDLE_TIMEOUT) {
			 return(true);
		 } else {
			 return(false);
		 }
	 }
	 public SQLXML createSQLXML() throws SQLException {
		 throw(new SQLException("Not yet implemented 27"));
	 }
	 public NClob createNClob() throws SQLException {
		 throw(new SQLException("Not yet implemented 28"));
	 }
	 public Blob createBlob() throws SQLException {
		 throw(new SQLException("Not yet implemented 29"));
	 }
	 public Clob createClob() throws SQLException {
		 throw(new SQLException("Not yet implemented 30"));
	 }
	 public boolean isWrapperFor(Class<?> iface) throws SQLException {
		 throw(new SQLException("Not yet implemented 31"));
	 }
	 public <T> T unwrap(Class<T> iface) throws SQLException {
		 throw(new SQLException("Not yet implemented 32"));
	 }
	 
	 public void setNetworkTimeout(Executor executor, int milliseconds)
	 {
		 
	 }
	 public int getNetworkTimeout() {
		 return(0);
	 }
	 public void abort(Executor executor)
	 {
		 CoreLog.log("ScpConnection.abort called , do nothing");
	 }
	 
	 public String getSchema()
	 {
		 return(dbname);
	 }
	 public void setSchema(String p_dbname)
	 {
		 dbname = p_dbname;
	 }
} 
