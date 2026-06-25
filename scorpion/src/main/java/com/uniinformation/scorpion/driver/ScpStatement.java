package com.uniinformation.scorpion.driver;
import com.kyoko.common.CoreLog;
import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.rpccall.*;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.net.*;
public class ScpStatement implements PreparedStatement {
//   public int RPCCALL_TIMEOUT = 600000;
    public int RPCCALL_TIMEOUT = 1800000;
           String sqlstring;
	        int statementid;
	private int lastsqlcode;
	private int lastsyntaxerrpos;
	private int lastsqlerrd1;
	private int lastsqlerrd2;
	private Vector selectlist;
	private Hashtable selectlistindexhash;
	private LinkedList recordlist;
	private String lasterrmsg;
	        ScpConnection currconnection;
 	private ScpResultSet scpresultset;
	private Vector parameter;
	private int sqlMaxRow ;
	private boolean poolable;
	ScpResultSetMetaData metadata;
	/*
   public class ParameterMetaData { // workaround for jdk 1.4 down to 1.3
	}
	*/
	public boolean isClosed() {
	   return(statementid == 0);
	}
	protected void finalize() {
	 	try {
	      if (!isClosed()) {
            CoreLog.logClass(this, "WARNING: Closing by finalize(): "+sqlstring);
	         close0();
	         close1();
         }
		} catch (SQLException e) {
			CoreLog.logClass(this, "sqlstate:"+e.getSQLState() + " errcode:"+e.getErrorCode());
			//CoreLog.log(e);
		}
	}
	void clearSqlVar() {
	 	lastsqlcode = 0;
		lastsyntaxerrpos = 0;
		lastsqlerrd1 = 0;
		lastsqlerrd2 = 0;
		lasterrmsg = null;
	}
	void presetParameter(int parameterIndex) {
	 	int i;
	 	if(parameter == null) {
			parameter = new Vector();
		}
		for(i=parameter.size();i<parameterIndex;i++) {
			parameter.add(null);
		}
	}
	public int getColumnIndex(String p_label) throws SQLException {
	   Integer ii = (Integer) selectlistindexhash.get(p_label);
		if (ii == null) {
		   throw(new SQLException("getColumnIndex("+p_label+") not found"));
		}
	   return(ii.intValue());
	}
	public Vector getSelectlist() {
	 	return(selectlist);
	}
	public void setSelectlist(int p_start,int p_cnt,Vector v) throws Exception {
	 	ScpField fd;
		int k;
		//CoreLog.logClass(this, "selectfield start");
		selectlist = new Vector();
	   selectlistindexhash = new Hashtable();
		for(k=0;k<p_cnt;k++) {
			//CoreLog.logClass(this, "field " + k + " " + v.get(k*3+p_start) + " " + v.get(k*3+1+p_start));
			String label = (String) v.get(p_start+0+k*3);
			fd = new ScpField(label,
							((Integer) v.get(p_start+1+k*3)).intValue(),
							((Integer) v.get(p_start+2+k*3)).intValue()
							);
	      Integer ii = new Integer(selectlist.size()+1);
	      selectlistindexhash.put(label, ii);
			int dot = label.indexOf('.');
			if (dot >= 0)
	         selectlistindexhash.put(StringUtil.strpart(label, dot+1, -1), ii);
			selectlist.add(fd);
		}
		//CoreLog.logClass(this, "selectfield end");
	}
	public void setSelectrecord(int p_start,int p_cnt,Vector v) throws Exception {
	 	Vector selectrecord;
		ScpField f;
		int k;
		selectrecord = new Vector();
		//CoreLog.logClass(this, "selectrecord start");
		for(k=0;k<p_cnt;k++) {
			f = (ScpField) (selectlist.get(k));
			switch(f.getFdtype()) {
			case ScpField.FDTYPE_CHAR :	
						if(v.get(p_start+k) == null) {
							selectrecord.add(v.get(p_start+k));
						} else {
							/*
							byte xx[] = (byte[]) v.get(p_start+k);
							if(xx.length > 10) {
								String ss = "HAHA in setSelectRecord : ";
								for(int i = 0;i<6;i++) {
									int cc = xx[i];
									if(cc < 0) cc += 256;
									ss += new Sprintf(" %02x").add(cc).toString();
								}
								CoreLog.log(ss);
								ss = "HAHA unicode " ;
								for(int i = 0;i<3;i++) {
									String sss =  new String(xx,"MS950");
									int cc = sss.charAt(i);
									ss += " " + cc;
								}
								CoreLog.log(ss);
							}
							*/
							byte[] b = (byte[]) v.get(p_start+k);
							if (b.length >= 2 && b[0] == -1 ) {
								switch (b[1] ) {
								case 'j':
									selectrecord.add(
											new String(b,2,b.length-2,"SJIS"));
									break;
								case 'u':
									selectrecord.add(
											new String(b,2,b.length-2,"UTF8"));
									break;
								default:
									selectrecord.add (new String( b ,"MS950_HKSCS"));
								}
							} else {
								selectrecord.add (new String( b ,"MS950_HKSCS"));
							}
//							selectrecord.add(
//									//	new String((byte[]) v.get(p_start+k),"BIG5"));
//									new String((byte[]) v.get(p_start+k),"MS950_HKSCS"));
						}
						break;
			default : selectrecord.add(v.get(p_start+k));
			}
			//CoreLog.logClass(this, "arg " + k + " " + selectrecord.get(k));
		}
		//CoreLog.logClass(this, "selectrecord end");
		recordlist.add(selectrecord);
	}
	public void setLasterrmsg(String p_val) {
	 	lasterrmsg = p_val;
	}
	public void setLastsqlcode(int p_val) {
	 	lastsqlcode = p_val;
	}
	public void setLastsyntaxerrpos(int p_val) {
	 	lastsyntaxerrpos = p_val;
	}
	public void setLastsqlerrd1(int p_val) {
	 	lastsqlerrd1 = p_val;
	}
	public void setLastsqlerrd2(int p_val) {
	 	lastsqlerrd2 = p_val;
	}
	public int getLastsqlcode() {
	 	return(lastsqlcode);	
	}
	public int getLastsyntaxerrpos() {
	 	return(lastsyntaxerrpos);
	}
	public int getLastsqlerrd1() {
	 	return(lastsqlerrd1);
	}
	public int getLastsqlerrd2() {
	 	return(lastsqlerrd2);
	}
	public String getLasterrmsg() {
	 	return(lasterrmsg);
	}
	public ScpStatement(ScpConnection p_connection) {
	 	currconnection = p_connection;
	}
	public ScpStatement(ScpConnection p_connection,String p_sqlstring) throws SQLException {
	 	currconnection = p_connection;
		Scorpion_preparestmt(p_sqlstring);
	}
	void Scorpion_preparestmt(String p_sqlstring) throws SQLException {
	 	Vector arglist;
		Value v;
		arglist = new Vector();
		arglist.addElement("jdbc_prepare_statement");
		arglist.addElement(p_sqlstring);
	 	clearSqlVar();
		synchronized (currconnection) {
			currconnection.setCurrstmt(this);
			v = currconnection.getPeerrpc().callSegment("callfunction",arglist);
		}
	 	if(v == null) {
			throw new ScpSQLException("Connection Error",sqlstring,-1);
		}
		statementid = v.toInt();
		if(statementid < 0) {
			throw new ScpSQLException(getLasterrmsg(),sqlstring,getLastsqlcode());
		}
	 	sqlstring = p_sqlstring;
		if(selectlist != null) metadata = new ScpResultSetMetaData(this);
	 	
//		CoreLog.logClass(this, "Scorpion_preparestmt():statementid="+statementid);
   }
	public void Scorpion_openstmt() throws SQLException {
	 	Vector arglist;
		Value v;
		if(statementid <= 0) {
			throw new ScpSQLException("Statement Not Prepared",null,-1);
		}
		if(selectlist == null) {
			throw new ScpSQLException("Cannot open non-select statement",null,-1);
		}
		arglist = new Vector();
		arglist.addElement("jdbc_open_statement");
		arglist.addElement(new Integer(statementid));
		if(parameter != null) {
			int i,cnt;
			cnt = parameter.size();
			for(i=0;i<cnt;i++) {
				arglist.addElement(parameter.get(i));
			}
		}
	 	clearSqlVar();
		synchronized (currconnection) {
			currconnection.setCurrstmt(this);
			currconnection.getPeerrpc().getConnection().setTimeOut(RPCCALL_TIMEOUT);
			v = currconnection.getPeerrpc().callSegment("callfunction",arglist);
		}
	 	if(v == null) {
		   CoreLog.logClass(currconnection, "throwing exception");
			throw new ScpSQLException("Connection Error",sqlstring,-1);
		}
		if(v.toInt() < 0) {
		   CoreLog.logClass(currconnection, "throwing exception");
			throw new ScpSQLException(getLasterrmsg(),sqlstring,getLastsqlcode());
		}
	}
	public Vector Scorpion_fetchstmt() throws SQLException {
	 	Vector arglist;
		Value v;
		if(statementid <= 0) {
			throw new ScpSQLException("Statement Not Prepared",null,-1);
		}
		if(selectlist == null) {
			throw new ScpSQLException("Cannot open non-select statement",null,-1);
		}
		if(recordlist == null) {
			recordlist = new LinkedList();
			Scorpion_openstmt();
		} 
		if(recordlist.size() > 0) {
			return( (Vector) recordlist.removeFirst() );
		} else {
			if(getLastsqlcode() != 0) {
				return(null);
			}
		} 
		arglist = new Vector();
		arglist.addElement("jdbc_fetch_statement");
		arglist.addElement(new Integer(statementid));
		arglist.addElement(new Integer(1000));
	 	clearSqlVar();
		synchronized (currconnection) {
			currconnection.setCurrstmt(this);
			currconnection.getPeerrpc().getConnection().setTimeOut(RPCCALL_TIMEOUT);
			v = currconnection.getPeerrpc().callSegment("callfunction",arglist);
		}
	 	if(v == null) {
			throw new ScpSQLException("Connection Error",sqlstring,-1);
		}
		if(v.toInt() < 0) {
			throw new ScpSQLException(getLasterrmsg(),sqlstring,getLastsqlcode());
		}
		if(recordlist.size() > 0) {
			return((Vector) recordlist.removeFirst());
	 	} else {
			return(null);
		}
	}
	void Scorpion_closestmt(ScpResultSet p_resultset) throws SQLException {
      Vector arglist;
      Value v;
      LinkedList tlinklist;
      if (p_resultset != scpresultset || statementid <= 0) {
         throw(new ScpSQLException("Scorpion_closestmt error xxx", sqlstring, -1));
      }
      tlinklist = recordlist;
      recordlist = null;
      scpresultset = null;
      if (!currconnection.useDelayClose()) {
         if (tlinklist != null) {
            arglist = new Vector();
            arglist.addElement("jdbc_close_statement");
            arglist.addElement(new Integer(statementid));
            clearSqlVar();
            synchronized (currconnection) {
               currconnection.setCurrstmt(this);
               v = currconnection.getPeerrpc().callSegment("callfunction",arglist);
            }
            if (v == null)
               throw new ScpSQLException("Connection Error",sqlstring,-1);
            if(v.toInt() < 0)
               throw new ScpSQLException(getLasterrmsg(),sqlstring,getLastsqlcode());
         }
      } 
	}
   public ResultSet executeQuery(String sql) throws SQLException {
	   close0();
	   close1();
		Scorpion_preparestmt(sql);
		if (selectlist == null) {
	      close0();
	      close1();
			throw(new ScpSQLException("Not a select statement", null, -1));
		}
		scpresultset = new ScpResultSet(this);
	 	return(scpresultset);
	}
   public int executeUpdate(String sql) throws SQLException {
	   close0();
	   close1();
		Scorpion_preparestmt(sql);
      return(executeUpdate());
	}
   public void close() throws SQLException {
	   close0();
      if (poolable && currconnection.useDelayClose())
	      currconnection.hashStatement(this);
		else
	      close1();
	}
   public int getMaxFieldSize() throws SQLException {
	 	return(0);
	}
   public void setMaxFieldSize(int max) throws SQLException {
	}
   public int getMaxRows() throws SQLException {
	 	return(sqlMaxRow);
	}
   public void setMaxRows(int max) throws SQLException {
//	   throw(new SQLException("Not yet implement 801"));
	   sqlMaxRow = max;
	}
   public void setEscapeProcessing(boolean enable) throws SQLException {
	   throw(new SQLException("Not yet implement 802"));
	}
   public int getQueryTimeout() throws SQLException {
	   throw(new SQLException("Not yet implement 803"));
	}
   public void setQueryTimeout(int seconds) throws SQLException {
	   throw(new SQLException("Not yet implement 804"));
	}
   public void cancel() throws SQLException {
	   throw(new SQLException("Not yet implement 805"));
	}
   public SQLWarning getWarnings() throws SQLException {
	   throw(new SQLException("Not yet implement 806"));
	}
   public void clearWarnings() throws SQLException {
	   throw(new SQLException("Not yet implement 807"));
	}
   public void setCursorName(String name) throws SQLException {
	   throw(new SQLException("Not yet implement 808"));
	}
   public boolean execute(String sql) throws SQLException {
//	   throw(new SQLException("Not yet implement 809"));
	   close0();
	   close1();
		Scorpion_preparestmt(sql);
		if (selectlist != null) {
			scpresultset = new ScpResultSet(this);
			return(true);
		} else  {
			CoreLog.log("Execute Non Select statement");
			executeUpdate();
			return(false);
		}
	}
   public ResultSet getResultSet() throws SQLException {
//	   throw(new SQLException("Not yet implement 810"));
		return(scpresultset);
	}
   public int getUpdateCount() throws SQLException {
//	   throw(new SQLException("Not yet implement 811"));
		CoreLog.log("getUpdateCount Called");
		if(scpresultset != null) return(-1); else return(lastsqlerrd2);
	}
   public boolean getMoreResults() throws SQLException {
	   throw(new SQLException("Not yet implement 812"));
	}
   public void setFetchDirection(int direction) throws SQLException {
	   throw(new SQLException("Not yet implement 813"));
	}
   public int getFetchDirection() throws SQLException {
	   throw(new SQLException("Not yet implement 814"));
	}
   public void setFetchSize(int rows) throws SQLException {
	   throw(new SQLException("Not yet implement 815"));
	}
   public int getFetchSize() throws SQLException {
	   throw(new SQLException("Not yet implement 816"));
	}
   public int getResultSetConcurrency() throws SQLException {
	   throw(new SQLException("Not yet implement 817"));
	}
   public int getResultSetType()  throws SQLException {
	   throw(new SQLException("Not yet implement 818"));
	}
   public void addBatch( String sql ) throws SQLException {
	   throw(new SQLException("Not yet implement 819"));
	}
   public void clearBatch() throws SQLException {
	   throw(new SQLException("Not yet implement 820"));
	}
   public int[] executeBatch() throws SQLException {
	   throw(new SQLException("Not yet implement 821"));
	}
   public Connection getConnection()  throws SQLException {
	   throw(new SQLException("Not yet implement 822"));
	}
	/* PreparedStatement Methods */
   public ResultSet executeQuery() throws SQLException {
		if(scpresultset != null) {
			scpresultset.close();
		}
	 	if(statementid <= 0) {
			throw new ScpSQLException("Statement not prepared",null,-1);
		}
		if(selectlist == null) {
			throw new ScpSQLException("Not a select statement",null,-1);
		}
		scpresultset=new ScpResultSet(this);
	 	return(scpresultset);
	}
   public int executeUpdate() throws SQLException {
		if(scpresultset != null) {
			scpresultset.close();
		}
	 	if(statementid <= 0) {
			throw new ScpSQLException("Statement not prepared",null,-1);
		}
		if(selectlist != null) {
			throw new ScpSQLException("Is a select statement",null,-1);
		}
	 	Vector arglist;
		Value v;
		arglist = new Vector();
		arglist.addElement("jdbc_update_statement");
		arglist.addElement(new Integer(statementid));
		if(parameter != null) {
			int i,cnt;
			cnt = parameter.size();
			for(i=0;i<cnt;i++) {
				arglist.addElement(parameter.get(i));
			}
		}
	 	clearSqlVar();
		synchronized (currconnection) {
			currconnection.setCurrstmt(this);
			currconnection.getPeerrpc().getConnection().setTimeOut(RPCCALL_TIMEOUT);
			v = currconnection.getPeerrpc().callSegment("callfunction",arglist);
		}
	 	if(v == null) {
			throw new ScpSQLException("Connection Error",sqlstring,-1);
		}
		if(v.toInt() < 0) {
			throw new ScpSQLException(getLasterrmsg(),sqlstring,getLastsqlcode());
		}
	 	return(getLastsqlerrd2());
	}
   public void setNull(int parameterIndex, int sqlType) throws SQLException {
	 	presetParameter(parameterIndex);
	}
   public void setBoolean(int parameterIndex, boolean x) throws SQLException {
	   throw(new SQLException("Not yet implement 823"));
	}
   public void setByte(int parameterIndex, byte x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,new Integer(x));
	}
   public void setShort(int parameterIndex, short x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,new Integer(x));
	}
   public void setInt(int parameterIndex, int x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,new Integer(x));
	}
   public void setLong(int parameterIndex, long x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,new Integer((int) x));
	}
   public void setFloat(int parameterIndex, float x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,new Float(x));
	}
   public void setDouble(int parameterIndex, double x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,new Double(x));
	}
   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
	   throw(new SQLException("Not yet implement 824"));
	}
   public void setString(int parameterIndex, String x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,x);
	}
   public void setBytes(int parameterIndex, byte x[]) throws SQLException {
	   throw(new SQLException("Not yet implement 825"));
	}
   public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
	 	presetParameter(parameterIndex);
	 	// Strangh ? at top application layer, the argument should specified or converted to sql.Date to indicate that only DD MM YY values in the Date Object is usable
	 	// at this stage, the sql.Date is convert back to java.Util.Date as the implementation of rpccall will convert java.util.Date to informix Date
	 	// rpccall dose recognize sql.Date sql.Time and sql.TimeStamp in order to make rpccall clean from jdbc 
	 	parameter.set(parameterIndex-1,DateUtil.toDate(x));
	}
   public void setTime(int parameterIndex, java.sql.Time x) throws SQLException {
	   presetParameter(parameterIndex);
	 	// Strangh ? at top application layer, the argument should specified or converted to sql.Time to indicate that only hh mm ss values in the Date Object is usable
	 	// at this stage, the sql.Date is convert to unixtime before parsing through rpccall
	   parameter.set(parameterIndex-1,DateUtil.dateToUnixtime(x));
	}
   public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws SQLException {
	   presetParameter(parameterIndex);
	 	// Strangh ? at top application layer, the argument should specified or converted to sql.Time to indicate that only hh mm ss values in the Date Object is usable
	 	// at this stage, the sql.Date is convert to unixtime before parsing through rpccall
	   parameter.set(parameterIndex-1,DateUtil.dateToUnixtime(x));
	}
   public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws SQLException {
	   throw(new SQLException("Not yet implement 828"));
	}
   public void setUnicodeStream( 
	               int parameterIndex, 
						java.io.InputStream x, 
			         int length) throws SQLException {
	   throw(new SQLException("Not yet implement 829"));
	}
   public void setBinaryStream(
	               int parameterIndex, 
						java.io.InputStream x, 
			         int length) throws SQLException {
	   throw(new SQLException("Not yet implement 830"));
	}
   public void clearParameters() throws SQLException {
	 	parameter = null;
	}
   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
	   throw(new SQLException("Not yet implement 831"));
	}
   public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,x);
	}
   public void setObject(int parameterIndex, Object x) throws SQLException {
	 	presetParameter(parameterIndex);
		parameter.set(parameterIndex-1,x);
	}
   public boolean execute() throws SQLException {
	 	return(false);
	}
   public void addBatch() throws SQLException {
	   throw(new SQLException("Not yet implement 832"));
	}
   public void setCharacterStream( int parameterIndex, java.io.Reader reader, int length) throws SQLException {
	   throw(new SQLException("Not yet implement 833"));
	}
   public void setRef (int i, Ref x) throws SQLException {
	   throw(new SQLException("Not yet implement 834"));
	}
   public void setBlob (int i, Blob x) throws SQLException {
	   throw(new SQLException("Not yet implement 835"));
	}
   public void setClob (int i, Clob x) throws SQLException {
	   throw(new SQLException("Not yet implement 836"));
	}
   public void setArray (int i, Array x) throws SQLException {
	   throw(new SQLException("Not yet implement 837"));
	}
   public ResultSetMetaData getMetaData() throws SQLException {
	   return(metadata);
//	   throw(new SQLException("Not yet implement 838"));
	}
   public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
	   throw(new SQLException("Not yet implement 839"));
	}
   public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws SQLException {
	   throw(new SQLException("Not yet implement 840"));
	}
   public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws SQLException {
	   throw(new SQLException("Not yet implement 841"));
	}
	public void setNull (int paramIndex, int sqlType, String typeName) throws SQLException {
	   throw(new SQLException("Not yet implement 842"));
	}
	public void closeForReuse() throws SQLException {
		if (scpresultset != null) {
			scpresultset.close();
		}
 	 	if(statementid <= 0) {
			throw new ScpSQLException("Statement not prepared",null,-1);
		}
	 	clearParameters();
	}
   public ParameterMetaData getParameterMetaData() throws SQLException {
	   throw(new SQLException("Not yet implement 843"));
	}
   public void setURL(int parameterIndex, URL x) throws SQLException {
	   throw(new SQLException("Not yet implement 844"));
	}
   public boolean execute(String sql, String[] xx) throws SQLException {
	   throw(new SQLException("Not yet implement 845"));
	}
   public int executeUpdate(String sql, String[] xx) throws SQLException {
	   throw(new SQLException("Not yet implement 846"));
	}
   public ResultSet getGeneratedKeys() throws SQLException {
	   if(lastsqlerrd1 > 0) {
		   return (new ScpInsertResultSet(lastsqlerrd1));
	   } else {
		   return(null);
	   }
	}
   public boolean execute(String sql, int[] xx) throws SQLException {
	   throw(new SQLException("Not yet implement 848"));
	}
   public int executeUpdate(String sql, int[] xx) throws SQLException {
	   throw(new SQLException("Not yet implement 849"));
	}
   public boolean execute(String sql, int xx) throws SQLException {
	   throw(new SQLException("Not yet implement 850"));
	}
   public int executeUpdate(String sql, int xx) throws SQLException {
	   throw(new SQLException("Not yet implement 851"));
	}
   public boolean getMoreResults(int xx) throws SQLException {
	   throw(new SQLException("Not yet implement 852"));
	}
   public int getResultSetHoldability() throws SQLException {
	   throw(new SQLException("Not yet implement 853"));
	}
	public void close0() throws SQLException {
		if (scpresultset != null) {
			try {
				scpresultset.close();
		   } catch (SQLException e) {
			   CoreLog.logClass(this, e.getSQLState() + " Errcode:"+e.getErrorCode());
			   CoreLog.log(e);
		   }
			scpresultset = null;
	   }
	   clearSqlVar();
	}
	public void close1() throws SQLException {
		if (statementid > 0 && !currconnection.isClosed()) {
	 	   Vector arglist = new Vector();
			arglist.addElement("jdbc_free_statement");
			arglist.addElement(new Integer(statementid));
		   Value v = null;
			synchronized (currconnection) {
				currconnection.setCurrstmt(this);
				v = currconnection.getPeerrpc().callSegment("callfunction", arglist);
			}
		 	if (v == null)
				throw(new ScpSQLException("Connection Error", sqlstring, -1));
			if (v.toInt() < 0)
				throw(new ScpSQLException(getLasterrmsg(), sqlstring, getLastsqlcode()));
		}
		statementid = 0;
		selectlist = null;
		sqlstring = null;
		metadata = null;
	}
	public void resetResultSet() {
	   scpresultset = null;
	}
	public String getSqlString() {
	   return(sqlstring);
	}
	public int getStatementId() {
	   return(statementid);
	}
	public void resetStatementId() {
	   statementid = 0;
	}
	/* for java 1.6 compatibility */
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
	   throw(new SQLException("Not yet implement 854"));
   }
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
	   throw(new SQLException("Not yet implement 855"));
   }
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
	   throw(new SQLException("Not yet implement 856"));
   }
	/*
   public void setBlob(int parameterIndex, Blob x) throws SQLException {
      throw(new SQLException("Not yet implemented 857"));
   }
	*/
   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
      throw(new SQLException("Not yet implemented 858"));
   }
   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      throw(new SQLException("Not yet implemented 859"));
   }
   public void setClob(int parameterIndex, Reader reader) throws SQLException {
      throw(new SQLException("Not yet implemented 860"));
	}
   public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      throw(new SQLException("Not yet implemented 861"));
	}
   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      throw(new SQLException("Not yet implemented 862"));
	}
   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      throw(new SQLException("Not yet implemented 863"));
	}
   public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
      throw(new SQLException("Not yet implemented 864"));
	}
   public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      throw(new SQLException("Not yet implemented 865"));
	}
   public void setAsciiStream(int parameterIndex, InputStream inputStream) throws SQLException {
      throw(new SQLException("Not yet implemented 866"));
	}
   public void setAsciiStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      throw(new SQLException("Not yet implemented 867"));
	}
   public void setSQLXML(int parameterIndex, java.sql.SQLXML sqlxml) throws SQLException {
      throw(new SQLException("Not yet implemented 868"));
	}
   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      throw(new SQLException("Not yet implemented 869"));
	}
   public void setNCharacterStream(int parameterIndex, java.io.Reader reader, long length) throws SQLException {
      throw(new SQLException("Not yet implemented 870"));
	}
   public void setNString(int parameterIndex, String nString) throws SQLException {
      throw(new SQLException("Not yet implemented 871"));
	}
   public void setRowId(int parameterIndex, java.sql.RowId rowId) throws SQLException {
      throw(new SQLException("Not yet implemented 872"));
	}
   public boolean isPoolable() throws SQLException {
//      throw(new SQLException("Not yet implemented 873"));
	   return(poolable);
	}
   public void setPoolable(boolean flag) throws SQLException {
//      throw(new SQLException("Not yet implemented 874"));
	   poolable= flag;
	}
   public boolean isWrapperFor(java.lang.Class<?> iface) throws SQLException {
      throw(new SQLException("Not yet implemented 875"));
	}
   public <T>T unwrap(java.lang.Class<T> iface) throws SQLException {
      throw(new SQLException("Not yet implemented 876"));
	}
   public void setClientInfo(java.util.Properties properties) throws SQLException {
      throw(new SQLException("Not yet implemented 877"));
	}
   
   public boolean isCloseOnCompletion() {
	  return(true);
   }
   
   public void closeOnCompletion () throws SQLException {
      throw(new SQLException("Not yet implemented 878"));
   }
}	
