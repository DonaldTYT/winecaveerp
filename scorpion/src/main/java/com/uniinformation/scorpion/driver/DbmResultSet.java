package com.uniinformation.scorpion.driver;
import com.kyoko.common.CoreLog;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.math.BigDecimal;
import java.util.Calendar;
public class DbmResultSet implements ResultSet {
	 ScpStatement scpstatement;
	 DbmResultSetMetaData metadata;
	 Vector currrow;
	 private boolean fClosed = false;
	 protected void finalize() {
	 	 try {
			 close();
		 } catch (SQLException e) {
			 CoreLog.log(e.getSQLState() + " Errcode:"+e.getErrorCode());
			 CoreLog.log(e);
		 }
	 }
	 public ScpStatement ScpResultGetStmt() {
	 	return(scpstatement);
	 }
	 public DbmResultSet(ScpStatement p_stmt) {
//	 	scpstatement = p_stmt;
		metadata = new DbmResultSetMetaData(this);
	 }
    public boolean next() throws SQLException {
		CoreLog.log("ResultSet.next called");
		return (false);
	 }
    public void close() throws SQLException {
	    if (fClosed)
		    return;
	 	 if (scpstatement != null) {
		    ScpStatement tstmt = scpstatement;
	 		 scpstatement = null;
//			 tstmt.Scorpion_closestmt(this);
		 }
		 fClosed = true;
	 }
    public boolean wasNull() throws SQLException {
		return(false);
	 }
    public String getString(int columnIndex) throws SQLException {
		String s;
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
		if (currrow.get(columnIndex-1) == null)
		   return(null);
		else if (currrow.get(columnIndex-1) instanceof java.lang.String)
		   return((String) currrow.get(columnIndex-1));
	   else
		   return(currrow.get(columnIndex-1).toString());
	 }
    public boolean getBoolean(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
		return(false);
	 }
    public byte getByte(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
	 	return(((Integer) currrow.get(columnIndex-1)).byteValue());
	 }
    public short getShort(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
	 	return(((Integer) currrow.get(columnIndex-1)).shortValue());
	 }
    public int getInt(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
	 	return(((Integer) currrow.get(columnIndex-1)).intValue());
	 }
    public long getLong(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
	 	return(((Integer) currrow.get(columnIndex-1)).longValue());
	 }
    public float getFloat(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
	 	return(((Float) currrow.get(columnIndex-1)).floatValue());
	 }
    public double getDouble(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
	 	return(((Double) currrow.get(columnIndex-1)).doubleValue());
	 }
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
	   throw(new SQLException("Not yet implemented 401"));
	 }
    public byte[] getBytes(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 402"));
	 }
    public java.sql.Date getDate(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
		java.util.Date tmpdate = (java.util.Date) currrow.get(columnIndex-1);
		if (tmpdate == null)
		   return(null);
	 	return(new java.sql.Date(tmpdate.getTime()));
	 }
    public java.sql.Time getTime(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 403"));
	 }
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 404"));
	 }
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 405"));
	 }
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 406"));
	 }
    public java.io.InputStream getBinaryStream(int columnIndex)
        throws SQLException {
	   throw(new SQLException("Not yet implemented 407"));
    }
    public String getString(String columnName) throws SQLException {
      String retstr = getString(scpstatement.getColumnIndex(columnName));
	   CoreLog.logClass(this, "getString("+columnName+") returning ["+retstr+"]");
      return(retstr);
	 }
    public boolean getBoolean(String columnName) throws SQLException {
      return(getBoolean(scpstatement.getColumnIndex(columnName)));
	 }
    public byte getByte(String columnName) throws SQLException {
      return(getByte(scpstatement.getColumnIndex(columnName)));
	 }
    public short getShort(String columnName) throws SQLException {
      return(getShort(scpstatement.getColumnIndex(columnName)));
	 }
    public int getInt(String columnName) throws SQLException {
      return(getInt(scpstatement.getColumnIndex(columnName)));
	 }
    public long getLong(String columnName) throws SQLException {
      return(getLong(scpstatement.getColumnIndex(columnName)));
	 }
    public float getFloat(String columnName) throws SQLException {
      return(getFloat(scpstatement.getColumnIndex(columnName)));
	 }
    public double getDouble(String columnName) throws SQLException {
      return(getDouble(scpstatement.getColumnIndex(columnName)));
	 }
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
      return(getBigDecimal(scpstatement.getColumnIndex(columnName)));
	 }
    public byte[] getBytes(String columnName) throws SQLException {
      return(getBytes(scpstatement.getColumnIndex(columnName)));
	 }
    public java.sql.Date getDate(String columnName) throws SQLException {
      return(getDate(scpstatement.getColumnIndex(columnName)));
	 }
    public java.sql.Time getTime(String columnName) throws SQLException {
      return(getTime(scpstatement.getColumnIndex(columnName)));
	 }
    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
      return(getTimestamp(scpstatement.getColumnIndex(columnName)));
	 }
    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
      return(getAsciiStream(scpstatement.getColumnIndex(columnName)));
	 }
    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
      return(getUnicodeStream(scpstatement.getColumnIndex(columnName)));
	 }
    public java.io.InputStream getBinaryStream(String columnName)
        throws SQLException {
      return(getBinaryStream(scpstatement.getColumnIndex(columnName)));
	 }
    public SQLWarning getWarnings() throws SQLException {
//	   throw(new SQLException("Not yet implemented 408"));
	   return(null);
	 }
    public void clearWarnings() throws SQLException {
//	   throw(new SQLException("Not yet implemented 409"));
	   return;
	 }
    public String getCursorName() throws SQLException {
	   throw(new SQLException("Not yet implemented 410"));
	 }
    public ResultSetMetaData getMetaData() throws SQLException {
	 	return(metadata);
	 }
    public Object getObject(int columnIndex) throws SQLException {
		if(columnIndex > currrow.size()) {
			throw new ScpSQLException("ColumnIndex OutofRange",null,-1);
		}
	 	return(currrow.get(columnIndex-1));
	 }
    public Object getObject(String columnName) throws SQLException {
	   throw(new SQLException("Not yet implemented 411"));
	 }
    public int findColumn(String columnName) throws SQLException {
	   throw(new SQLException("Not yet implemented 412"));
	 }
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 413"));
	 }
    public java.io.Reader getCharacterStream(String columnName) throws SQLException {
	   throw(new SQLException("Not yet implemented 414"));
	 }
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 415"));
	 }
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
	   throw(new SQLException("Not yet implemented 416"));
	 }
    public boolean isBeforeFirst() throws SQLException {
	   throw(new SQLException("Not yet implemented 417"));
	 }
    public boolean isAfterLast() throws SQLException {
	   throw(new SQLException("Not yet implemented 418"));
	 }
    public boolean isFirst() throws SQLException {
	   throw(new SQLException("Not yet implemented 419"));
	 }
    public boolean isLast() throws SQLException {
	   throw(new SQLException("Not yet implemented 420"));
	 }
    public void beforeFirst() throws SQLException {
	   throw(new SQLException("Not yet implemented 421"));
	 }
    public void afterLast() throws SQLException {
	   throw(new SQLException("Not yet implemented 422"));
	 }
    public boolean first() throws SQLException {
	   throw(new SQLException("Not yet implemented 423"));
	 }
    public boolean last() throws SQLException {
	   throw(new SQLException("Not yet implemented 424"));
	 }
    public int getRow() throws SQLException {
	   throw(new SQLException("Not yet implemented 425"));
	 }
    public boolean absolute( int row ) throws SQLException {
	   throw(new SQLException("Not yet implemented 426"));
	 }
    public boolean relative( int rows ) throws SQLException {
	   throw(new SQLException("Not yet implemented 427"));
	 }
    public boolean previous() throws SQLException {
	   throw(new SQLException("Not yet implemented 428"));
	 }
	 /*
    int FETCH_FORWARD = 1000;
    int FETCH_REVERSE = 1001;
    int FETCH_UNKNOWN = 1002;
	 */
    public void setFetchDirection(int direction) throws SQLException {
	   throw(new SQLException("Not yet implemented 429"));
	 }
    public int getFetchDirection() throws SQLException {
		return(FETCH_FORWARD);
	 }
    public void setFetchSize(int rows) throws SQLException {
	   throw(new SQLException("Not yet implemented 430"));
	 }
    public int getFetchSize() throws SQLException {
	   throw(new SQLException("Not yet implemented 431"));
	 }
	 /*
    int TYPE_FORWARD_ONLY = 1003;
    int TYPE_SCROLL_INSENSITIVE = 1004;
    int TYPE_SCROLL_SENSITIVE = 1005;
	 */
    public int getType() throws SQLException {
       return(TYPE_FORWARD_ONLY);
	 }
	 /*
    int CONCUR_READ_ONLY = 1007;
    int CONCUR_UPDATABLE = 1008;
	 */
    public int getConcurrency() throws SQLException {
      return(CONCUR_READ_ONLY);
	 }
    public boolean rowUpdated() throws SQLException {
	   throw(new SQLException("Not yet implemented 432"));
	 }
    public boolean rowInserted() throws SQLException {
	   throw(new SQLException("Not yet implemented 433"));
	 }
    public boolean rowDeleted() throws SQLException {
	   throw(new SQLException("Not yet implemented 434"));
	 }
    public void updateNull(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 435"));
	 }
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
	   throw(new SQLException("Not yet implemented 436"));
	 }
    public void updateByte(int columnIndex, byte x) throws SQLException {
	   throw(new SQLException("Not yet implemented 437"));
	 }
    public void updateShort(int columnIndex, short x) throws SQLException {
	   throw(new SQLException("Not yet implemented 438"));
	 }
    public void updateInt(int columnIndex, int x) throws SQLException {
	   throw(new SQLException("Not yet implemented 439"));
	 }
    public void updateLong(int columnIndex, long x) throws SQLException {
	   throw(new SQLException("Not yet implemented 440"));
	 }
    public void updateFloat(int columnIndex, float x) throws SQLException {
	   throw(new SQLException("Not yet implemented 441"));
	 }
    public void updateDouble(int columnIndex, double x) throws SQLException {
	   throw(new SQLException("Not yet implemented 442"));
	 }
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
	   throw(new SQLException("Not yet implemented 443"));
	 }
    public void updateString(int columnIndex, String x) throws SQLException {
	   throw(new SQLException("Not yet implemented 444"));
	 }
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
	   throw(new SQLException("Not yet implemented 445"));
	 }
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
	   throw(new SQLException("Not yet implemented 446"));
	 }
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
	   throw(new SQLException("Not yet implemented 447"));
	 }
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
      throws SQLException {
	   throw(new SQLException("Not yet implemented 448"));
	 }
    public void updateAsciiStream(int columnIndex, 
			   java.io.InputStream x, 
			   int length) throws SQLException {
	   throw(new SQLException("Not yet implemented 449"));
	 }
    public void updateAsciiStream(int columnIndex, 
			   java.io.InputStream x, 
			   long length) throws SQLException {
	   throw(new SQLException("Not yet implemented 450"));
	 }
    public void updateAsciiStream(int columnIndex, 
			   java.io.InputStream x
			   ) throws SQLException {
	   throw(new SQLException("Not yet implemented 451"));
	 }
    public void updateBinaryStream(int columnIndex, 
			    java.io.InputStream x,
			    int length) throws SQLException {
	   throw(new SQLException("Not yet implemented 452"));
	 }
    public void updateBinaryStream(int columnIndex, 
			    java.io.InputStream x,
			    long length) throws SQLException {
	   throw(new SQLException("Not yet implemented 453"));
	 }
    public void updateBinaryStream(int columnIndex, 
			    java.io.InputStream x
			    ) throws SQLException {
	   throw(new SQLException("Not yet implemented 454"));
	 }
    public void updateCharacterStream(int columnIndex,
			     java.io.Reader x,
			     int length) throws SQLException {
	   throw(new SQLException("Not yet implemented 455"));
	 }
    public void updateCharacterStream(int columnIndex,
			     java.io.Reader x
			     ) throws SQLException {
	   throw(new SQLException("Not yet implemented 456"));
	 }
    public void updateObject(int columnIndex, Object x, int scale)
      throws SQLException {
	   throw(new SQLException("Not yet implemented 457"));
	 }
    public void updateObject(int columnIndex, Object x) throws SQLException {
	   throw(new SQLException("Not yet implemented 458"));
	 }
    public void updateNull(String columnName) throws SQLException {
	   throw(new SQLException("Not yet implemented 459"));
	 }
    public void updateBoolean(String columnName, boolean x) throws SQLException {
	   throw(new SQLException("Not yet implemented 460"));
	 }
    public void updateByte(String columnName, byte x) throws SQLException {
	   throw(new SQLException("Not yet implemented 461"));
	 }
    public void updateShort(String columnName, short x) throws SQLException {
	   throw(new SQLException("Not yet implemented 462"));
	 }
    public void updateInt(String columnName, int x) throws SQLException {
	   throw(new SQLException("Not yet implemented 463"));
	 }
    public void updateLong(String columnName, long x) throws SQLException {
	   throw(new SQLException("Not yet implemented 464"));
	 }
    public void updateFloat(String columnName, float x) throws SQLException {
	   throw(new SQLException("Not yet implemented 465"));
	 }
    public void updateDouble(String columnName, double x) throws SQLException {
	   throw(new SQLException("Not yet implemented 466"));
	 }
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
	   throw(new SQLException("Not yet implemented 467"));
	 }
    public void updateString(String columnName, String x) throws SQLException {
	   throw(new SQLException("Not yet implemented 468"));
	 }
    public void updateBytes(String columnName, byte x[]) throws SQLException {
	   throw(new SQLException("Not yet implemented 469"));
	 }
    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
	   throw(new SQLException("Not yet implemented 470"));
	 }
    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
	   throw(new SQLException("Not yet implemented 477"));
	 }
    public void updateTimestamp(String columnName, java.sql.Timestamp x)
      throws SQLException {
	   throw(new SQLException("Not yet implemented 478"));
	 }
    public void updateAsciiStream(String columnName, 
			   java.io.InputStream x, 
			   int length) throws SQLException {
	   throw(new SQLException("Not yet implemented 479"));
	 }
    public void updateAsciiStream(String columnName, 
			   java.io.InputStream x, 
			   long length) throws SQLException {
	   throw(new SQLException("Not yet implemented 480"));
	 }
    public void updateAsciiStream(String columnName, 
			   java.io.InputStream x
			   ) throws SQLException {
	   throw(new SQLException("Not yet implemented 481"));
	 }
    public void updateBinaryStream(String columnName, 
			    java.io.InputStream x,
			    int length) throws SQLException {
	   throw(new SQLException("Not yet implemented 482"));
	 }
    public void updateBinaryStream(String columnName, 
			    java.io.InputStream x,
			    long length) throws SQLException {
	   throw(new SQLException("Not yet implemented 483"));
	 }
    public void updateBinaryStream(String columnName, 
			    java.io.InputStream x
			    ) throws SQLException {
	   throw(new SQLException("Not yet implemented 484"));
	 }
    public void updateCharacterStream(String columnName,
			     java.io.Reader reader,
			     int length) throws SQLException {
	   throw(new SQLException("Not yet implemented 485"));
	 }
    public void updateCharacterStream(String columnName,
			     java.io.Reader reader,
			     long length) throws SQLException {
	   throw(new SQLException("Not yet implemented 486"));
	 }
    public void updateCharacterStream(String columnName,
			     java.io.Reader reader
			     ) throws SQLException {
	   throw(new SQLException("Not yet implemented"));
	 }
    public void updateCharacterStream(int columnIndex,
			     java.io.Reader reader,
			     long length) throws SQLException {
	   throw(new SQLException("Not yet implemented 487"));
	 }
    public void updateObject(String columnName, Object x, int scale)
      throws SQLException {
	   throw(new SQLException("Not yet implemented 488"));
	 }
    public void updateObject(String columnName, Object x) throws SQLException {
	   throw(new SQLException("Not yet implemented 489"));
	 }
    public void insertRow() throws SQLException {
	   throw(new SQLException("Not yet implemented 490"));
	 }
    public void updateRow() throws SQLException {
	   throw(new SQLException("Not yet implemented 491"));
	 }
    public void deleteRow() throws SQLException {
	   throw(new SQLException("Not yet implemented 492"));
	 }
    public void refreshRow() throws SQLException {
	   throw(new SQLException("Not yet implemented 493"));
	 }
    public void cancelRowUpdates() throws SQLException {
	   throw(new SQLException("Not yet implemented 494"));
	 }
    public void moveToInsertRow() throws SQLException {
	   throw(new SQLException("Not yet implemented 495"));
	 }
    public void moveToCurrentRow() throws SQLException {
	   throw(new SQLException("Not yet implemented 496"));
	 }
    public Statement getStatement() throws SQLException {
	   return(scpstatement);
	 }
    public Object getObject(int i, java.util.Map map) throws SQLException {
	   throw(new SQLException("Not yet implemented 497"));
	 }
    public Ref getRef(int i) throws SQLException {
	   throw(new SQLException("Not yet implemented 498"));
	 }
    public Blob getBlob(int i) throws SQLException {
	   throw(new SQLException("Not yet implemented 499"));
	 }
    public Clob getClob(int i) throws SQLException {
	   throw(new SQLException("Not yet implemented 500"));
	 }
    public Array getArray(int i) throws SQLException {
	   throw(new SQLException("Not yet implemented 501"));
	 }
    public Object getObject(String colName, java.util.Map map) throws SQLException {
	   throw(new SQLException("Not yet implemented 502"));
	 }
    public Ref getRef(String colName) throws SQLException {
	   throw(new SQLException("Not yet implemented 503"));
	 }
    public Blob getBlob(String colName) throws SQLException {
	   throw(new SQLException("Not yet implemented 504"));
	 }
    public Clob getClob(String colName) throws SQLException {
	   throw(new SQLException("Not yet implemented 505"));
	 }
    public Array getArray(String colName) throws SQLException {
	   throw(new SQLException("Not yet implemented 506"));
	 }
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
	   throw(new SQLException("Not yet implemented 507"));
	 }
    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
	   throw(new SQLException("Not yet implemented 508"));
	 }
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
	   throw(new SQLException("Not yet implemented 509"));
	 }
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
	   throw(new SQLException("Not yet implemented 510"));
	 }
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) 
      throws SQLException {
	   throw(new SQLException("Not yet implemented 511"));
	 }
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)	
      throws SQLException {
	   throw(new SQLException("Not yet implemented 512"));
	 }
    public void updateRef(int columnIndex, Ref x) throws SQLException {
	   throw(new SQLException("Not yet implemented 513"));
	 }
    public void updateRef(String columnName, Ref x) throws SQLException {
	   throw(new SQLException("Not yet implemented 514"));
	 }
    public void updateClob(int columnIndex, Clob clob) throws SQLException {
	   throw(new SQLException("Not yet implemented 515"));
	 }
    public void updateClob(String columnName, Clob clob) throws SQLException {
	   throw(new SQLException("Not yet implemented 516"));
	 }
    public void updateBlob(int columnIndex, Blob blob) throws SQLException {
	   throw(new SQLException("Not yet implemented 517"));
	 }
    public void updateBlob(String columnName, Blob blob) throws SQLException {
	   throw(new SQLException("Not yet implemented 518"));
	 }
    public void updateArray(int columnIndex, Array arr) throws SQLException {
	   throw(new SQLException("Not yet implemented 519"));
	 }
    public void updateArray(String columnName, Array arr) throws SQLException {
	   throw(new SQLException("Not yet implemented 520"));
	 }
    public URL getURL(int columnIndex) throws SQLException {
	   throw(new SQLException("Not yet implemented 521"));
	 }
    public URL getURL(String columnName) throws SQLException {
	   throw(new SQLException("Not yet implemented 522"));
	 }
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
       throw(new SQLException("Not yet implemented 523"));
    }
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
       throw(new SQLException("Not yet implemented 524"));
    }
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
       throw(new SQLException("Not yet implemented 525"));
    }
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
       throw(new SQLException("Not yet implemented 526"));
    }
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
       throw(new SQLException("Not yet implemented 527"));
    }
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
       throw(new SQLException("Not yet implemented 528"));
    }
	 /*
    public void updateClob(int columnIndex, Clob x) throws SQLException {
       throw(new SQLException("Not yet implement 529"));
    }
	 */
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
       throw(new SQLException("Not yet implement 530"));
    }
	 /*
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
       throw(new SQLException("Not yet implement 351"));
    }
	 */
	 /*
    public void updateClob(String columnLabel, Clob x) throws SQLException {
       throw(new SQLException("Not yet implement 532"));
    }
	 */
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
       throw(new SQLException("Not yet implement 533"));
    }
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
       throw(new SQLException("Not yet implement 534"));
    }
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
       throw(new SQLException("Not yet implement 535"));
    }
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
       throw(new SQLException("Not yet implement 536"));
	 }
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
       throw(new SQLException("Not yet implement 537"));
	 }
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
       throw(new SQLException("Not yet implement 538"));
	 }
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
       throw(new SQLException("Not yet implement 539"));
	 }
    public void updateNCharacterStream(int columnIndex, Reader reader) throws SQLException {
       throw(new SQLException("Not yet implement 540"));
	 }
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
       throw(new SQLException("Not yet implement 541"));
	 }
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
       throw(new SQLException("Not yet implement 542"));
	 }
    public void updateNCharacterStream(int columnIndex, Reader reader, long length) throws SQLException {
       throw(new SQLException("Not yet implement 543"));
	 }
    public Reader getNCharacterStream(String columnName) throws SQLException {
       throw(new SQLException("Not yet implement 544"));
	 }
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
       throw(new SQLException("Not yet implement 545"));
	 }
    public String getNString(String columnName) throws SQLException {
       throw(new SQLException("Not yet implement 546"));
	 }
    public String getNString(int columnIndex) throws SQLException {
       throw(new SQLException("Not yet implement 547"));
	 }
    public void updateSQLXML(String colunmName, java.sql.SQLXML xmlObject) throws SQLException {
       throw(new SQLException("Not yet implement 548"));
	 }
    public void updateSQLXML(int colunmIndex, java.sql.SQLXML xmlObject) throws SQLException {
       throw(new SQLException("Not yet implement 549"));
	 }
    public SQLXML getSQLXML(String columnName) throws SQLException {
       throw(new SQLException("Not yet implement 550"));
    }
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
       throw(new SQLException("Not yet implement 551"));
    }
    public NClob getNClob(String columnName) throws SQLException {
       throw(new SQLException("Not yet implement 552"));
	 }
    public NClob getNClob(int columnIndex) throws SQLException {
       throw(new SQLException("Not yet implement 553"));
	 }
    public void updateNString(String columnName, String nString)  throws SQLException {
       throw(new SQLException("Not yet implement 554"));
	 }
    public void updateNString(int columnIndex, String nString)  throws SQLException {
       throw(new SQLException("Not yet implement 555"));
	 }
    public boolean isClosed() throws SQLException {
       throw(new SQLException("Not yet implement 556"));
	 }
    public int getHoldability() throws SQLException {
       throw(new SQLException("Not yet implement 557"));
	 }
    public void updateRowId(String columnName, java.sql.RowId x) throws SQLException {
       throw(new SQLException("Not yet implement 558"));
	 }
    public void updateRowId(int columnIndex, java.sql.RowId x) throws SQLException {
       throw(new SQLException("Not yet implement 559"));
	 }
    public RowId getRowId(String columnName) throws SQLException {
       throw(new SQLException("Not yet implement 560"));
	 }
    public RowId getRowId(int columnIndex) throws SQLException {
       throw(new SQLException("Not yet implement 561"));
	 }
    public boolean isWrapperFor(java.lang.Class<?> iface) throws SQLException {
       throw(new SQLException("Not yet implement 562"));
	 }
    public <T> T unwrap(Class<T> iface) throws SQLException {
       throw(new SQLException("Not yet implement 563"));
	 }
    
    public <T> T getObject(int columnIndex , Class<T> iface) throws SQLException {
        throw(new SQLException("Not yet implement 564"));
 	 }
     public <T> T getObject(String columnLabel, Class<T> iface) throws SQLException {
        throw(new SQLException("Not yet implement 564"));
 	 }
}
