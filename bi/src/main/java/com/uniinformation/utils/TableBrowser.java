package com.uniinformation.utils;

import java.sql.*;
import java.util.*;

import com.kyoko.common.DateUtil;
import com.kyoko.common.NumberUtil;
import com.kyoko.common.StringUtil;

import java.io.*;
import java.math.*;
import java.lang.reflect.*;

public class TableBrowser {
   public static final int SQLTYPE_INTEGER = 0;
   public static final int SQLTYPE_CHAR = 1;
   public static final int SQLTYPE_FLOAT = 2;
   public static final int SQLTYPE_DATE = 3;
   public static final int SQLTYPE_BINARY = 4;
   public static final int SQLTYPE_TIMESTAMP = 5;
   public static final int SQLTYPE_BLOB = 6;
//   public static final String SQLTYPE_DEFAULT_DATE = "1900-01-01";
   public static final String SQLTYPE_DEFAULT_DATE = "1899-12-31";
   private String str_update = null;  //statement for update
   private String str_delete = null;  //statement for delete
   private String str_insert = null;  //statement for insert
   private String str_selectbyrowid = null;

   private int debugLevel = 1;
	private String tabname = null;
	private Wherecl curwherecl = null;
	private String curorderbycollist = null;
	private String curorderby = null;
   private String errmsg = "";
	private Vector all_colnames = null;
	private Hashtable all_hash_colindex = null;
	private Vector all_coltypes = null;
	private Vector all_collengths = null;
	private Vector all_colsublengths = null;
	private Vector tmp_colnames = null;
	private Hashtable set_colindex = null;
	private String set_colnames[] = null;
	private int set_coltypes[] = null;
	private int set_collengths[] = null;
	private int set_colsublengths[] = null;
	private String lkfromtable = null;
	private String lkwherecl = null;
	private String fullcolnameList = null;
	private String colnameList = null;
	private String curValues[] = null;
	private String curValuesComments[] = null;
	private Object curRowid = null;
	private Object insertedRowid = null;
	private Vector rowidList = null;
	private Vector rowidStatusList = null;
	private int rowidIdx;
	private Vector lookupVector = null;
	private int realcolcnt = 0;
	private int databaseType = 0;
	private boolean fSelectMulti = false;
	private Vector curMultiValues = null;
	private String savedValues[] = null;
	private String uniIdColName = "serial_id";
	private Hashtable preventUpdateColumns = new Hashtable();

   public TableBrowser(Connection p_dbConnection, String p_tabname) throws SQLException {
		this(p_dbConnection, p_tabname, null);
	}
   public TableBrowser(Connection p_dbConnection, String p_tabname, String p_uniqueColName) throws SQLException {
		setUniqueIdColumnName(p_uniqueColName);
		PreparedStatement tmpstatement;
      ResultSet tmprset;

      UniLog.logClass(this, "connection is "+p_dbConnection.getClass().getName());
		databaseType = JdbcPool.findConnectionType(p_dbConnection);
      tabname = p_tabname;
      all_colnames = new Vector();
      all_coltypes = new Vector();
      all_collengths = new Vector();
      all_colsublengths = new Vector();
      all_hash_colindex = new Hashtable();
	   lookupVector = new Vector();
		int colidx = 0;
		switch (databaseType) {
		   case JdbcPool.JDBC_ORACLE:
		      tmpstatement = p_dbConnection.prepareStatement(
		                           "select column_name, data_type, data_length, data_precision, data_scale from COLS where table_name = ?");
            tmpstatement.setString(1,tabname.toUpperCase());
            tmprset = tmpstatement.executeQuery();
		      for (int i=0; tmprset.next(); i++) {
		         String tmpcolname, tmptype;
			      int tmplength, tmpprecision, tmpscale;
      
               tmpcolname   = tmprset.getString(1).toLowerCase();
               tmptype      = tmprset.getString(2).toLowerCase();
               tmplength    = tmprset.getInt(3);
               tmpprecision = tmprset.getInt(4);
               tmpscale     = tmprset.getInt(5);
      
			      if (tmptype.equalsIgnoreCase("long raw"))
			         continue;
      
               all_colnames.addElement(tmpcolname);
               all_hash_colindex.put(tmpcolname, (Object) new Integer(colidx++));
               all_colsublengths.addElement(new Integer(tmpscale));
			      if (tmptype.equalsIgnoreCase("char")) {
                  all_coltypes.addElement(new Integer(SQLTYPE_CHAR)); 
                  all_collengths.addElement(new Integer(tmplength));
			      }
			      else if (tmptype.equalsIgnoreCase("long raw")) {
                  all_coltypes.addElement(new Integer(SQLTYPE_BINARY)); 
                  all_collengths.addElement(new Integer(tmplength));
			      }
			      else if (tmptype.equalsIgnoreCase("varchar")) {
                  all_coltypes.addElement(new Integer(SQLTYPE_CHAR)); 
                  all_collengths.addElement(new Integer(tmplength));
			      }
			      else if (tmptype.equalsIgnoreCase("varchar2")) {
                  all_coltypes.addElement(new Integer(SQLTYPE_CHAR)); 
                  all_collengths.addElement(new Integer(tmplength));
			      }
			      else if (tmptype.equalsIgnoreCase("number")) {
				      if (tmpscale > 0)
                     all_coltypes.addElement(new Integer(SQLTYPE_FLOAT)); 
                  else
                     all_coltypes.addElement(new Integer(SQLTYPE_INTEGER)); 
                  all_collengths.addElement(new Integer(tmpprecision));
			      }
			      else if (tmptype.equalsIgnoreCase("float")) {
                  all_coltypes.addElement(new Integer(SQLTYPE_FLOAT)); 
                  //all_collengths.addElement(new Integer(tmpprecision));
                  all_collengths.addElement(new Integer(10));
			      }
			      else if (tmptype.equalsIgnoreCase("date")) {
                  all_coltypes.addElement(new Integer(SQLTYPE_DATE)); 
                  //all_collengths.addElement(new Integer(tmpprecision));
                  all_collengths.addElement(new Integer(10));
			      }
			      else {
                  all_coltypes.addElement(new Integer(SQLTYPE_CHAR)); 
                  all_collengths.addElement(new Integer(tmplength));
	               throw new SQLException("invalid type:"+tmptype);
			      }
            }
			   break;
		   case JdbcPool.JDBC_SCORPION:
			default:
            tmprset = null;
				try {
		         tmpstatement = p_dbConnection.prepareStatement(
		                              "select * from "+tabname.toLowerCase()+" where "+getUniqueIdColumnName()+" = 0");
               tmprset = tmpstatement.executeQuery();
				} catch (SQLException ex1) {
				   UniLog.log(ex1);
				}
				if (tmprset == null) {
		         tmpstatement = p_dbConnection.prepareStatement("select * from "+tabname);
               tmprset = tmpstatement.executeQuery();
				}
	         ResultSetMetaData rsmd = tmprset.getMetaData();
	         int colcnt = rsmd.getColumnCount();
	         String[] labels = new String[colcnt];
	         for (int i=0; i<colcnt; i++) {
		         String tmpcolname;
			      int tmplength, tmpprecision, tmpscale;
					int tmptype;
      
               tmpcolname   = rsmd.getColumnLabel(i+1).toLowerCase();
               tmptype      = rsmd.getColumnType(i+1);
               tmplength    = rsmd.getColumnDisplaySize(i+1);
					// patch for mysql-5.0
					if (tmptype == Types.LONGVARBINARY) { 
					   tmplength = 16777215;
					} else if (tmplength == 196605)
					   tmplength = 65535;
				   else if (tmplength == -3)
					   tmplength = 16777215;
				   else if (tmplength == -1)
					   tmplength = 16777215;
               tmpprecision = rsmd.getPrecision(i+1);
               tmpscale     = rsmd.getScale(i+1);

					switch (tmptype) {
						case Types.INTEGER:
						case Types.REAL:
					   case Types.DOUBLE:
					   case Types.DECIMAL:
					   case Types.NUMERIC:
					   case Types.FLOAT:
						case Types.LONGVARCHAR:
						case Types.VARCHAR:
					   case Types.CHAR:
					   case Types.DATE:
						case Types.SMALLINT:
						case Types.BIGINT:
						case Types.TIMESTAMP:
						case Types.TIMESTAMP_WITH_TIMEZONE:
						case Types.TIME_WITH_TIMEZONE:
						case Types.TINYINT:
						case Types.LONGVARBINARY:
						case Types.VARBINARY:
						case Types.BINARY:
						case Types.BIT:
						case Types.BOOLEAN:
						   break;
					   default:
						   UniLog.logClass(this, "ignoring field "+tmpcolname+" for unsupport type "+tmptype);
						   continue;
					}
      
               all_colnames.addElement(tmpcolname);
               all_hash_colindex.put(tmpcolname, (Object) new Integer(colidx++));
               all_colsublengths.addElement(new Integer(tmpscale));
UniLog.logClass(this, "www997:trace:0:"+tmpcolname);
UniLog.logClass(this, "www997:trace:1:"+tmplength);
					switch (tmptype) {
					   case Types.LONGVARCHAR:
                     all_coltypes.addElement(new Integer(SQLTYPE_CHAR)); 
                     all_collengths.addElement(new Integer(tmplength));
						   break;
					   case Types.VARCHAR:
					   case Types.CHAR:
                     all_coltypes.addElement(new Integer(SQLTYPE_CHAR)); 
                     if (databaseType == JdbcPool.JDBC_MYSQL)
                        // all_collengths.addElement(new Integer(tmplength/3));
                        all_collengths.addElement(new Integer(tmplength));
							else
                        all_collengths.addElement(new Integer(tmplength));
						   break;
					   case Types.DATE:
                     all_coltypes.addElement(new Integer(SQLTYPE_DATE)); 
                     all_collengths.addElement(new Integer(10));
						   break;
					   case Types.NUMERIC:
					   case Types.REAL:
					   case Types.FLOAT:
                     all_coltypes.addElement(new Integer(SQLTYPE_FLOAT)); 
                     all_collengths.addElement(new Integer(tmpprecision));
						   break;
					   case Types.DOUBLE:
                     all_coltypes.addElement(new Integer(SQLTYPE_FLOAT)); 
                     all_collengths.addElement(new Integer(tmpprecision));
						   break;
					   case Types.DECIMAL:
				         if (tmpscale > 0)
                        all_coltypes.addElement(new Integer(SQLTYPE_FLOAT)); 
                     else
                        all_coltypes.addElement(new Integer(SQLTYPE_INTEGER)); 
                     all_collengths.addElement(new Integer(tmpprecision));
						   break;
						case Types.BIGINT:
						case Types.INTEGER:
                     all_coltypes.addElement(new Integer(SQLTYPE_INTEGER)); 
                     all_collengths.addElement(new Integer(tmpprecision));
						   break;
						case Types.SMALLINT:
						case Types.TINYINT:
						case Types.BIT:
						case Types.BOOLEAN:
                     all_coltypes.addElement(new Integer(SQLTYPE_INTEGER)); 
                     all_collengths.addElement(new Integer(tmpprecision));
						   break;
						case Types.TIMESTAMP:
						case Types.TIMESTAMP_WITH_TIMEZONE:
						case Types.TIME_WITH_TIMEZONE:
                     all_coltypes.addElement(new Integer(SQLTYPE_TIMESTAMP)); 
                     all_collengths.addElement(new Integer(tmpprecision));
						   break;
						case Types.LONGVARBINARY:
						case Types.VARBINARY:
						case Types.BINARY:
                     all_coltypes.addElement(new Integer(SQLTYPE_BLOB)); 
                     all_collengths.addElement(new Integer(tmpprecision));
						   break;
					   default:
						   UniLog.logClass(this, "should not be here...");
							throw(new SQLException("unsupported type:"+tmptype));
					}
	         }
			   break;
		}
	   tmp_colnames = new Vector(0);
		set_colindex = new Hashtable();
      closeCursor();
		/*
      UniLog.log("all_colnames="+all_colnames.toString());
      UniLog.log("all_coltypes="+all_coltypes.toString());
      UniLog.log("all_collengths="+all_collengths.toString());
      UniLog.log("all_colsublengths="+all_colsublengths.toString());
		*/
   }
	public void close() {
	}
   public int setColumn(String p_colname) {
		Integer tmpInteger;
		tmpInteger = (Integer) all_hash_colindex.get(p_colname);
		if (tmpInteger == null) {
			setErrmsg("Column "+p_colname+" not found!");
		   return(-1);
	   }
	   set_colindex.put(p_colname, new Integer(tmp_colnames.size()));
	   tmp_colnames.addElement(p_colname);
	   return(0);
   }
   public void setColumnComplete(Connection p_dbConnection) {
	   realcolcnt = tmp_colnames.size();
	   setupTBLookup(p_dbConnection);
	   set_colnames = new String[tmp_colnames.size()];
	   set_coltypes = new int[tmp_colnames.size()];
	   set_collengths = new int[tmp_colnames.size()];
	   set_colsublengths = new int[tmp_colnames.size()];
	   curValues = new String[tmp_colnames.size()];
	   curValuesComments = new String[tmp_colnames.size()];
		for (int i=0; i<tmp_colnames.size(); i++) {
		   int tmpidx;
		   set_colnames[i] = (String) tmp_colnames.elementAt(i);
		   tmpidx = ((Integer) all_hash_colindex.get(set_colnames[i])).intValue();
	      set_coltypes[i] = ((Integer) all_coltypes.elementAt(tmpidx)).intValue();
	      set_collengths[i] = ((Integer) all_collengths.elementAt(tmpidx)).intValue();
	      set_colsublengths[i] = ((Integer) all_colsublengths.elementAt(tmpidx)).intValue();
		}
      defaultFields();
	   colnameList = getColnameList(0);
	   fullcolnameList = getColnameList(1);
      if (debugLevel > 1) {
			for (int i=0; i<set_colnames.length; i++) {
            UniLog.log("column("
				                  +((Integer) set_colindex.get(set_colnames[i])).intValue()+")="
				                  +set_colnames[i]+"("
										+set_coltypes[i]+","
										+set_collengths[i]+","
										+set_colsublengths[i]
										+")");
			}
		}
	}
	public String getColnameList(int p_fullqual) {
		StringBuffer tmpcolnamelist;
		tmpcolnamelist = new StringBuffer();
		if (p_fullqual == 0) {
		   for (int i=0; i<realcolcnt; i++) {
			   if (i > 0) 
	            tmpcolnamelist.append(",");
	         tmpcolnamelist.append(set_colnames[i]);
	      }
		}
		else {
		   for (int i=0; i<set_colnames.length; i++) {
			   if (i > 0) 
	            tmpcolnamelist.append(",");
			   if (i < realcolcnt)
	            tmpcolnamelist.append(tabname).append("0.");
	         tmpcolnamelist.append(set_colnames[i]);
	      }
		}
		return(tmpcolnamelist.toString());
	}
   public void setColumns(Connection p_dbConnection) {
		int i;
	   for (i=0; i<all_colnames.size(); i++) {
         setColumn((String) all_colnames.elementAt(i));
		}
      setColumnComplete(p_dbConnection);
   }
   public void setWhere(Wherecl p_wherecl) {
	   curwherecl = p_wherecl;
	}
   public void setOrderby(String p_orderby) {
	   curorderby = p_orderby;
		curorderbycollist = null;
	}
   public void setOrderby(String p_orderby, String p_collist) {
	   curorderby = p_orderby;
		curorderbycollist = p_collist;
	}
   public int openCursor(Connection p_dbConnection) throws SQLException {
      return(openCursor(p_dbConnection, null));
	}
   public int openCursor(Connection p_dbConnection, Vector p_v_qtblookup) throws SQLException {
UniLog.logClass(this, "trace:001 openCursor()");
		try {
         PreparedStatement rowid_select;
         ResultSet rset_rowid;

	      closeCursor();
		   StringBuffer tmpsqlstring = new StringBuffer("select ");

UniLog.logClass(this, "trace:002 openCursor() databaseType="+databaseType);
	      switch (databaseType) {
		      case JdbcPool.JDBC_ORACLE:
				   tmpsqlstring.append(" ROWIDTOCHAR("+tabname+"0.rowid) AS crowid from ");
					break;
		      case JdbcPool.JDBC_SCORPION: 
				default:
				  	if (curorderbycollist != null)
				     	tmpsqlstring.append(" "+tabname+"0."+getUniqueIdColumnName()+", "+curorderbycollist+" from ");
					else
				     	tmpsqlstring.append(" "+tabname+"0."+getUniqueIdColumnName()+" from ");
					break;
         }
         tmpsqlstring.append(tabname).append(" ").append(tabname).append("0");
UniLog.logClass(this, "trace:003 openCursor() p_v_qtblookup="+p_v_qtblookup);
			if (p_v_qtblookup != null) {
		      for (int i=0; i<p_v_qtblookup.size(); i++) {
		         TBLookup tblookup = (TBLookup) p_v_qtblookup.elementAt(i);
				   tmpsqlstring.append(", ");
					if (tblookup.getFBlindIncludeUsed())
				      tmpsqlstring.append("outer ");
				   tmpsqlstring.append(tblookup.tabname)
								   .append(" ")
								   .append(tblookup.tabaliase);
		      }
			}
			if (curwherecl != null) {
UniLog.logClass(this, "trace:004 openCursor() curwherecl="+curwherecl.toString());
			   curwherecl.stripAnd();
			}
		   if (curwherecl != null && !curwherecl.getWhereString().trim().equals("")) {
            tmpsqlstring.append(" where ").append(curwherecl.getWhereString());
			   if (p_v_qtblookup != null && p_v_qtblookup.size() > 0)
               tmpsqlstring.append(" and ");
			}
			else {
			   if (p_v_qtblookup != null && p_v_qtblookup.size() > 0)
               tmpsqlstring.append(" where ");
			}
UniLog.logClass(this, "trace:005 openCursor() p_v_qtblookup="+p_v_qtblookup);
			if (p_v_qtblookup != null) {
		      for (int i=0; i<p_v_qtblookup.size(); i++) {
		         TBLookup tblookup = (TBLookup) p_v_qtblookup.elementAt(i);
				   for (int j=0; j<tblookup.condFieldNames.size(); j++) {
				      if (j > 0) 
				         tmpsqlstring.append(" and ");
				      tmpsqlstring.append(tabname).append("0.")
					             .append((String) tblookup.condFieldNames.elementAt(j))
					             .append(" = ")
								    .append(tblookup.tabaliase).append(".")
					             .append((String) tblookup.condColNames.elementAt(j));
				   }
		      }
			}
		   if (curorderby != null && !curorderby.trim().equals(""))
            tmpsqlstring.append(" order by ").append(curorderby);
		   if (debugLevel > 0)
		      UniLog.logClass(this, "openCursor(): tmpsqlstring = ["+tmpsqlstring.toString()+"]");
         rowid_select = p_dbConnection.prepareStatement(tmpsqlstring.toString());        
UniLog.logClass(this, "trace:9900 rowid_select="+rowid_select);
			if (curwherecl != null) 
	         curwherecl.setStatementValue(rowid_select, 0);
UniLog.logClass(this, "trace:9901 rowid_select="+rowid_select);
         rset_rowid = rowid_select.executeQuery(); 
			Hashtable hash = new Hashtable();
			while (rset_rowid.next()) {
			   String tmprowid = rset_rowid.getString(1);
				if (hash.get(tmprowid) == null) {
				   rowidList.addElement(tmprowid);
				   rowidStatusList.addElement(new Integer(1));
				   hash.put(tmprowid, tmprowid);
				}
			}
			hash.clear();
			rset_rowid.close();
			rowid_select.close();
         int cc = nextCursor(p_dbConnection);
			return(cc);
		} catch (Exception ex) {
			UniLog.log(3, ex);
		   return(-1);
		}
   }
	public void clearFieldComments() {
      for (int i=0;i<set_colnames.length;i++) {
			curValuesComments[i] = null;
      }
	}
	private void rsetToValues(ResultSet resultSet, String[] p_values) throws SQLException {
      for (int i=0;i<set_colnames.length;i++) {
	      switch (set_coltypes[i]) {
            case SQLTYPE_INTEGER:
					if (resultSet.getString(i+1) == null)
				      p_values[i] = "0";
					else
				      p_values[i] = resultSet.getString(i+1);
				   break;
            case SQLTYPE_CHAR:
					if (resultSet.getString(i+1) == null)
				      p_values[i] = "";
					else
				      p_values[i] = resultSet.getString(i+1);
				   break;
            case SQLTYPE_FLOAT: 
					if (resultSet.getString(i+1) == null)
				      p_values[i] = "0.0";
					else
				      p_values[i] = resultSet.getString(i+1);
				   break;
            case SQLTYPE_DATE: 
					if (resultSet.getDate(i+1) == null) {
				      p_values[i] = SQLTYPE_DEFAULT_DATE;
					}
					else {
				      p_values[i] = resultSet.getDate(i+1).toString();
				      if (p_values[i].equals(SQLTYPE_DEFAULT_DATE))
						   p_values[i] = "";
					}
				   break;
            case SQLTYPE_TIMESTAMP: 
					if (resultSet.getTimestamp(i+1) == null) {
				      p_values[i] = "";
					}
					else {
				      p_values[i] = ""+(resultSet.getTimestamp(i+1).getTime()/1000);
					}
					break;
            case SQLTYPE_BLOB: 
					if (resultSet.getBytes(i+1) == null || resultSet.getBytes(i+1).length == 0) {
				      p_values[i] = "";
					}
					else {
						File objFile = FileUtil.bytesToFile(resultSet.getBytes(i+1));
				      p_values[i] = (objFile != null ? objFile.getAbsolutePath() : "");
					}
					break;
            case SQLTYPE_BINARY: 
				   break;
				default: 
				   UniLog.log("Unknown data type!");
					break;
			}
      }
	}
	private void rsetToCurValues(ResultSet resultSet) throws SQLException {
	   clearFieldComments();
	   rsetToValues(resultSet, curValues);
		curRowid = resultSet.getObject(set_colnames.length+1);
		if (debugLevel > 1) {
			for (int i=0; i<set_colnames.length; i++) {
            UniLog.log("restToCurValues():"
				                  +set_colnames[i]+"=["
										+curValues[i]+"]"
										+"<"+curValuesComments[i]+">");
			}
			UniLog.log("curRowid = ["+curRowid+"]");
	   }
   }
   public void closeCursor() throws SQLException {
		if (rowidList == null)
	      rowidList = new Vector();
      else
		   rowidList.removeAllElements();
		if (rowidStatusList == null)
	      rowidStatusList = new Vector();
      else
		   rowidStatusList.removeAllElements();
      rowidIdx = -1;
	   curRowid = null;
   }
   public int firstCursor() throws SQLException {
	   rowidIdx = -1;
	   curRowid = null;
		return(0);
	}
   public int setCursor(Connection p_dbConnection, int p_idx) throws SQLException {
		if (p_idx < 0 || p_idx >= rowidList.size()) 
		   return(-1);
	   rowidIdx = p_idx;
	   curRowid = rowidList.elementAt(rowidIdx);
		return(currentCursor(p_dbConnection));
	}
   public int nextCursor(Connection p_dbConnection) throws SQLException {
		int tmpidx;
		tmpidx = rowidIdx;

		for (;;) {
		   if (tmpidx >= rowidList.size()-1) {
			   return(-1);
		   }
		   else {
		      tmpidx++;
			   if (tmpidx >=0 
			       && tmpidx < rowidList.size() 
			       && ((Integer) rowidStatusList.elementAt(tmpidx)).intValue() == 0) {
			   }
			   else {
	            curRowid = rowidList.elementAt(tmpidx);
				   rowidIdx = tmpidx;
		         return(currentCursor(p_dbConnection));
			   }
		   }
		}
   }
	public int currentCursor(Connection p_dbConnection) throws SQLException {
		int retcc;
		if (rowidIdx < 0 
		    || rowidIdx >= rowidList.size() 
			 || ((Integer) rowidStatusList.elementAt(rowidIdx)).intValue() == 0) {
         return(-1);
		}
      if (str_selectbyrowid == null) {
		   StringBuffer tmpsqlstring = new StringBuffer("select ");
		   tmpsqlstring.append(fullcolnameList).append(", ");
		   switch (databaseType) {
			   case JdbcPool.JDBC_ORACLE:
               tmpsqlstring.append(" ROWIDTOCHAR("+tabname+"0.rowid) AS crowid from ");
				   break;
		      case JdbcPool.JDBC_SCORPION:
				default:
              	tmpsqlstring.append(" "+tabname+"0."+getUniqueIdColumnName()+" from ");
				   break;
			}
         tmpsqlstring.append(tabname).append(" ").append(tabname).append("0");
			if (lkfromtable != null) 
			   tmpsqlstring.append(lkfromtable);
		   switch (databaseType) {
		      case JdbcPool.JDBC_ORACLE:
               tmpsqlstring.append(" where "+tabname+"0.rowid = CHARTOROWID(?) ");
			      break;
		      case JdbcPool.JDBC_SCORPION:
				default:
              	tmpsqlstring.append(" where "+tabname+"0."+getUniqueIdColumnName()+" = ? ");
			      break;
			}
			if (lkwherecl != null)
            tmpsqlstring.append(" and ").append(lkwherecl);
         if (debugLevel > 0) {
			   UniLog.logClass(this, "currentCursor:"+tmpsqlstring.toString());
			}
         str_selectbyrowid = tmpsqlstring.toString();
		}
      PreparedStatement st_selectbyrowid = p_dbConnection.prepareStatement(str_selectbyrowid);
      st_selectbyrowid.setObject(1, curRowid);
      ResultSet tmpresult = st_selectbyrowid.executeQuery();
		if (tmpresult.next()) {
		   retcc = 0;
	      rsetToCurValues(tmpresult);
			if (fSelectMulti) {
			   curMultiValues = new Vector();
				for (;;) {
			      String[] values = new String[set_colnames.length];
			      curMultiValues.addElement(values);
	            rsetToValues(tmpresult, values);
				   if (!tmpresult.next())
				      break;
				}
			}
		}
		else
		   retcc = -1;
		tmpresult.close();
      st_selectbyrowid.close();
	   return(retcc);
	}
   public int prevCursor(Connection p_dbConnection) throws SQLException {
		int tmpidx;
		tmpidx = rowidIdx;
		for (;;) {
		   if (tmpidx <= 0)
		      return(-1);
		   tmpidx--;
			if (tmpidx >=0 
			    && tmpidx < rowidList.size() 
			    && ((Integer) rowidStatusList.elementAt(tmpidx)).intValue() == 0) {
			}
			else {
	         curRowid = rowidList.elementAt(tmpidx);
			   rowidIdx = tmpidx;
		      return(currentCursor(p_dbConnection));
			}
		}
   }
   public int setFieldComments(String p_colname, String p_value) {
		Integer idxInteger;
		idxInteger = (Integer) set_colindex.get(p_colname);
		if (idxInteger == null) return(-1);
      return(setFieldComments(idxInteger.intValue(), p_value));
   }
   public int setFieldComments(int p_colidx, String p_comments) {
		if (curValuesComments == null) return(-1);
		if (p_colidx < 0 || p_colidx >= curValuesComments.length) return(-1);
		curValuesComments[p_colidx] = p_comments;
	   return(0);
	}
   public int setField(String p_colname, String p_value) {
		Integer idxInteger;
		idxInteger = (Integer) set_colindex.get(p_colname);
		if (idxInteger == null) return(-1);
      return(setField(idxInteger.intValue(), p_value));
   }
   public int setField(int p_colidx, String p_value) {
		if (p_value == null) return(-1);
		if (curValues == null) return(-1);
		if (p_colidx < 0 || p_colidx >= curValues.length) return(-1);
	   switch (set_coltypes[p_colidx]) {
         case SQLTYPE_CHAR:
				if (p_value.length() > set_collengths[p_colidx]) {
UniLog.logClass(this, "setField():trace:0:p_colidx="+p_colidx);
UniLog.logClass(this, "setField():trace:1:p_value="+p_value);
UniLog.logClass(this, "setField():trace:2:all_colnames(p_colidx)="+all_colnames.elementAt(p_colidx));
UniLog.logClass(this, "setField():trace:3:set_collengths[p_colidx]="+set_collengths[p_colidx]);
		         curValues[p_colidx] = p_value.substring(0, set_collengths[p_colidx]);
				}
				else {
		         curValues[p_colidx] = new String(p_value);
				}
			   break;
         case SQLTYPE_FLOAT: 
         case SQLTYPE_DATE: 
         case SQLTYPE_INTEGER:
         case SQLTYPE_TIMESTAMP:
		      curValues[p_colidx] = new String(p_value);
			   break;
         case SQLTYPE_BINARY:
			   break;
         case SQLTYPE_BLOB:
		      curValues[p_colidx] = new String(p_value);
			   break;
		}
		curValuesComments[p_colidx] = null;
	   return(0);
   }
   public int clearField(String p_colname) {
		Integer idxInteger;
		idxInteger = (Integer) set_colindex.get(p_colname);
		if (idxInteger == null) return(-1);
      return(clearField(idxInteger.intValue()));
   }
   public int clearField(int p_colidx) {
		if (p_colidx < 0 || p_colidx >= curValues.length) return(-1);
	   switch (set_coltypes[p_colidx]) {
         case SQLTYPE_INTEGER:
            return(setField(p_colidx, "")); // **** haha
         case SQLTYPE_CHAR:
            return(setField(p_colidx, ""));
         case SQLTYPE_FLOAT: 
            return(setField(p_colidx, "")); // **** haha
         case SQLTYPE_DATE: 
            return(setField(p_colidx, ""));
         case SQLTYPE_TIMESTAMP: 
            return(setField(p_colidx, ""));
         case SQLTYPE_BINARY: 
			   return(0);
         case SQLTYPE_BLOB: 
            return(setField(p_colidx, ""));
			default: 
				return(-1);
		}
   }
   public void clearFields() {
		int i;
	   for (i=0; i<set_colnames.length; i++)
         clearField(i);
   }
   public void defaultFields() {
      clearFields();
   }
	public Object getRowid() {
	   return(curRowid);
	}
	public Object getInsertedRowid() {
	   return(insertedRowid);
	}
	public String getRowidString() {
		if (curRowid == null)
		   return(null);
	   return(curRowid.toString());
	}
	public String getRowid(int p_idx) {
	   return((String) rowidList.elementAt(p_idx));
	}
	public int getRowidIdx() {
	   return(rowidIdx);
	}
   public String[] getFields() {
		return(curValues);
   }
   public Vector getFieldsMulti() {
	   return(curMultiValues);
   }
   public String[] getFieldsComments() {
		return(curValuesComments);
   }
   public TableRec getTableRec() {
	   return(null);
   }
   public String getFieldComments(String p_colname) {
		Integer idxInteger;
		idxInteger = (Integer) set_colindex.get(p_colname);
		if (idxInteger == null) return(null);
      return(getFieldComments(idxInteger.intValue()));
   }
   public String getFieldComments(int p_colidx) {
		if (curValuesComments == null) return(null);
		if (p_colidx < 0 || p_colidx >= curValuesComments.length) return(null);
		return(curValuesComments[p_colidx]);
   }
   public String getField(String p_colname) {
		Integer idxInteger;
		idxInteger = (Integer) set_colindex.get(p_colname);
		if (idxInteger == null) return(null);
      return(getField(idxInteger.intValue()));
   }
   public String getField(int p_colidx) {
		if (curValues == null) {
		   return(null);
		}
		if (p_colidx < 0 || p_colidx >= curValues.length) {
		   return(null);
      }
		return(curValues[p_colidx]);
   }
	private void curValuesToSqlStatement(PreparedStatement p_statement) throws SQLException {
	   curValuesToSqlStatement(p_statement, null);
	}
	private void curValuesToSqlStatement(PreparedStatement p_statement, Hashtable p_updateColumnIndexes) throws SQLException {
	   int stmtArgumentIdx = 0;
		for (int i=0; i<realcolcnt/*set_colnames.length*/; i++) {
			if (preventUpdateColumns.get(set_colnames[i]) != null)
				continue;
			if (p_updateColumnIndexes != null && p_updateColumnIndexes.get(new Integer(i)) == null)
			   continue;
	      switch (set_coltypes[i]) {
            case SQLTYPE_CHAR:
               p_statement.setString(stmtArgumentIdx+1,curValues[i]);
				   break;
            case SQLTYPE_INTEGER:
	            switch (databaseType) {
		            case JdbcPool.JDBC_ORACLE:
					      if (curValues[i].trim().equals(""))
                        p_statement.setObject(stmtArgumentIdx+1,new BigDecimal("0"));
					      else
                        p_statement.setObject(stmtArgumentIdx+1,new BigDecimal(curValues[i]));
					      break;
		            case JdbcPool.JDBC_SCORPION: 
						default:
					      if (curValues[i].trim().equals(""))
                        p_statement.setObject(stmtArgumentIdx+1,new Integer(0));
					      else
                        p_statement.setObject(stmtArgumentIdx+1,new Integer(curValues[i]));
					      break;
               }
				   break;
            case SQLTYPE_FLOAT: 
	            switch (databaseType) {
		            case JdbcPool.JDBC_ORACLE:
					      if (curValues[i].trim().equals(""))
                        p_statement.setObject(stmtArgumentIdx+1,new BigDecimal("0"));
					      else
                        p_statement.setObject(stmtArgumentIdx+1,new BigDecimal(curValues[i]));
					      break;
		            case JdbcPool.JDBC_SCORPION: 
						default:
					      if (curValues[i].trim().equals(""))
                        p_statement.setObject(stmtArgumentIdx+1,new Double(0.0));
					      else
                        p_statement.setObject(stmtArgumentIdx+1,new Double(curValues[i]));
					      break;
               }
				   break;
            case SQLTYPE_DATE: 
	            switch (databaseType) {
		            case JdbcPool.JDBC_ORACLE:
                     if (curValues[i] != null && curValues[i].trim().equals(""))
                        p_statement.setDate(stmtArgumentIdx+1,(java.sql.Date.valueOf(SQLTYPE_DEFAULT_DATE)));
					      else
                        p_statement.setDate(stmtArgumentIdx+1,(java.sql.Date.valueOf(curValues[i])));
					      break;
		            case JdbcPool.JDBC_SCORPION: 
						default:
                     if (curValues[i] != null && curValues[i].trim().equals(""))
                        p_statement.setDate(stmtArgumentIdx+1,DateUtil.toSqlDate(DateUtil.getDate(SQLTYPE_DEFAULT_DATE)));
					      else {
                        p_statement.setDate(stmtArgumentIdx+1,DateUtil.toSqlDate(DateUtil.getDate(curValues[i])));
							}
					      break;
               }
				   break;
            case SQLTYPE_TIMESTAMP: 
	            switch (databaseType) {
		            case JdbcPool.JDBC_ORACLE:
                     if (curValues[i] != null && curValues[i].trim().equals(""))
                        p_statement.setTimestamp(stmtArgumentIdx+1,new java.sql.Timestamp((long) 0));
					      else
                        p_statement.setTimestamp(stmtArgumentIdx+1,new java.sql.Timestamp(NumberUtil.parseLong(curValues[i].trim())*1000));
					      break;
		            case JdbcPool.JDBC_SCORPION: 
						default:
                     if (curValues[i] != null && curValues[i].trim().equals(""))
                        p_statement.setTimestamp(stmtArgumentIdx+1, new java.sql.Timestamp((long) 0));
					      else
                        p_statement.setTimestamp(stmtArgumentIdx+1,new java.sql.Timestamp(NumberUtil.parseLong(curValues[i].trim())*1000));
					      break;
               }
				   break;
            case SQLTYPE_BLOB: 
	            switch (databaseType) {
		            case JdbcPool.JDBC_ORACLE:
                     if (curValues[i] != null && curValues[i].trim().equals("")) {
                        p_statement.setBytes(stmtArgumentIdx+1, new byte[0]);
					      } else {
								File objFile = new File(curValues[i].trim());
								byte[] bytes =  FileUtil.getBytesFromFile(objFile);
                        p_statement.setBytes(stmtArgumentIdx+1, bytes);
								objFile.delete();
							}
					      break;
		            case JdbcPool.JDBC_SCORPION: 
						default:
                     if (curValues[i] != null && curValues[i].trim().equals("")) {
                        p_statement.setBytes(stmtArgumentIdx+1, new byte[0]);
					      } else {
								File objFile = new File(curValues[i].trim());
								byte[] bytes = FileUtil.getBytesFromFile(objFile);
                        p_statement.setBytes(stmtArgumentIdx+1, bytes);
								objFile.delete();
							}
               }
				   break;
            case SQLTYPE_BINARY:
				   break;
				default: 
				   UniLog.log("Unknown data type!");
					break;
			}
	      stmtArgumentIdx++;
		}
	}
   public int updateRow(Connection p_dbConnection) throws SQLException { 
	   return(updateRow(p_dbConnection, false));
	}
   public int updateRow(Connection p_dbConnection, boolean p_fUpdateChangeFieldOnly) throws SQLException { 
		int updatecnt, retcc;
		Hashtable updateColumnIndexes = null;
		int updateColCnt = realcolcnt;
		if (p_fUpdateChangeFieldOnly) {
		   updateColumnIndexes = new Hashtable();
		   StringBuffer tmpsqlstring = new StringBuffer();
			tmpsqlstring.append("update ").append(tabname.toLowerCase());
			tmpsqlstring.append(" set ");
			updateColCnt = 0;
			for (int i=0; i<realcolcnt; i++) {
UniLog.logClass(this, "updateRow():trace:0:set_colnames[i]="+set_colnames[i]);
				if (preventUpdateColumns.get(set_colnames[i]) != null)
				   continue;
UniLog.logClass(this, "updateRow():trace:1.1:savedValues[i]="+savedValues[i]);
UniLog.logClass(this, "updateRow():trace:1.2:curValues[i]="+curValues[i]);
	         if (savedValues[i] == null && curValues[i] == null) {
UniLog.logClass(this, "updateRow():trace:2");
				   continue;
				}
	         else if (savedValues[i] == null && curValues[i] != null) {
UniLog.logClass(this, "updateRow():trace:3");
				   continue;
				}
	         else if (savedValues[i] != null && curValues[i] == null) {
UniLog.logClass(this, "updateRow():trace:4");
				   continue;
				}
				else if (StringUtil.sr(savedValues[i]).equals(StringUtil.sr(curValues[i]))) {
UniLog.logClass(this, "updateRow():trace:5");
				   continue;
				}
UniLog.logClass(this, "updateRow():trace:6");
			   if (updateColCnt > 0) 
				   tmpsqlstring.append(", ");
		      updateColumnIndexes.put(new Integer(i), "");
			   tmpsqlstring.append(set_colnames[i]).append(" = ?");
				updateColCnt++;
			}
		   switch (databaseType) {
		      case JdbcPool.JDBC_ORACLE:
               tmpsqlstring.append(" where rowid = ").append("CHARTOROWID(?)");
			      break;
		      case JdbcPool.JDBC_SCORPION:
				default:
               tmpsqlstring.append(" where "+getUniqueIdColumnName()+" = ").append("?");
				   break;
		   }
			str_update = tmpsqlstring.toString();
		}
		else if (str_update == null) {
		   StringBuffer tmpsqlstring = new StringBuffer();
			tmpsqlstring.append("update ").append(tabname.toLowerCase());
			tmpsqlstring.append(" set ");
			updateColCnt = 0;
			for (int i=0; i<realcolcnt; i++) {
				if (preventUpdateColumns.get(set_colnames[i]) != null)
				   continue;
			   if (updateColCnt > 0) 
				   tmpsqlstring.append(", ");
			   tmpsqlstring.append(set_colnames[i]).append(" = ?");
				updateColCnt++;
			}
		   switch (databaseType) {
		      case JdbcPool.JDBC_ORACLE:
               tmpsqlstring.append(" where rowid = ").append("CHARTOROWID(?)");
			      break;
		      case JdbcPool.JDBC_SCORPION:
				default:
               tmpsqlstring.append(" where "+getUniqueIdColumnName()+" = ").append("?");
				   break;
		   }
			str_update = tmpsqlstring.toString();
		}
		UniLog.logClass(this, "updateRow(): str_update = "+str_update);
		if (updateColCnt > 0) {
         PreparedStatement st_update = p_dbConnection.prepareStatement(str_update);
		   curValuesToSqlStatement(st_update, updateColumnIndexes);
         st_update.setObject(updateColCnt+1, curRowid);
         updatecnt = st_update.executeUpdate();
		   st_update.close();
	      return(updatecnt > 0 ? 0 : -1);
		}
		else
		   return(0);
   }
   public int deleteRow(Connection p_dbConnection) throws SQLException {
		int updatecnt;
	   if (rowidIdx < 0
			 || rowidIdx >= rowidList.size() 
			 || ((Integer) rowidStatusList.elementAt(rowidIdx)).intValue() == 0)
		   return(-1);
		if (str_delete == null) {
		   StringBuffer tmpsqlstring = new StringBuffer();
			tmpsqlstring.append("delete from ").append(tabname.toLowerCase());
	      switch (databaseType) {
		      case JdbcPool.JDBC_ORACLE:
               tmpsqlstring.append(" where rowid = CHARTOROWID(?)");
					break;
		      case JdbcPool.JDBC_SCORPION: 
				default:
               tmpsqlstring.append(" where "+getUniqueIdColumnName()+" = ?");
					break;
         }
         str_delete = tmpsqlstring.toString();
		}
      PreparedStatement st_delete = p_dbConnection.prepareStatement(str_delete);
      st_delete.setObject(1, curRowid);
      updatecnt = st_delete.executeUpdate();
		if (updatecnt > 0)
			rowidStatusList.set(rowidIdx, new Integer(0));
	   return(updatecnt > 0 ? 0 : -1);
   }
   public int insertRow(Connection p_dbConnection) throws SQLException {
		int updatecnt;
		if (str_insert == null) {
		   StringBuffer tmpsqlstring = new StringBuffer();
			tmpsqlstring.append("insert into ").append(tabname.toLowerCase());
			//tmpsqlstring.append("insert into ").append(tabname);
			tmpsqlstring.append("(");
	      tmpsqlstring.append(colnameList);
			tmpsqlstring.append(") values (");
			for (int i=0; i<realcolcnt; i++) {
			   if (i > 0) 
				   tmpsqlstring.append(",");
			   tmpsqlstring.append("?");
			}
			tmpsqlstring.append(")");
			str_insert = tmpsqlstring.toString();
		}
		UniLog.logClass(this, "insertRow(): str_insert = "+str_insert);
      PreparedStatement st_insert = p_dbConnection.prepareStatement(str_insert, Statement.RETURN_GENERATED_KEYS);
UniLog.logClass(this, "trace:haha0 st_insert="+st_insert);
	   curValuesToSqlStatement(st_insert);
UniLog.logClass(this, "trace:haha1 getUniqueIdColumnName()="+getUniqueIdColumnName());
UniLog.logClass(this, "trace:haha2 st_insert="+st_insert);
      updatecnt = st_insert.executeUpdate();
		if (st_insert.getClass().getName().equals("com.uniinformation.scorpion.driver.ScpStatement")) {
		   try {
            Method method = st_insert.getClass().getMethod("getLastsqlerrd1", new Class[0]);
	         insertedRowid = ((Integer) method.invoke(st_insert, new Object[0]));
			} catch (Exception ex) {
			   UniLog.log(ex);
			   throw(new SQLException("getLastsqlerrd1 failure"));
			}
		}
		/*
		else if (st_insert instanceof com.mysql.jdbc.PreparedStatement) {
         ResultSet insertedKeyResult = ((com.mysql.jdbc.PreparedStatement) st_insert).getGeneratedKeys();
         insertedKeyResult.next();
	      insertedRowid = new Integer(insertedKeyResult.getInt(1));
		}
		*/
		else if (st_insert.getClass().getName().startsWith("com.mysql.jdbc")) {
			try {
			Method method = st_insert.getClass().getMethod("getGeneratedKeys", new Class[0]);
         ResultSet insertedKeyResult = (ResultSet) method.invoke(st_insert, new Object[0]);
         insertedKeyResult.next();
	      insertedRowid = new Integer(insertedKeyResult.getInt(1));
			} catch(NoSuchMethodException ex1) {
			   UniLog.log(ex1);
				throw(new SQLException("mysql getGeneratedKeys() NoSuchMethodException."));
			} catch(IllegalAccessException ex2) {
			   UniLog.log(ex2);
				throw(new SQLException("mysql getGeneratedKeys() IllegalAccessException."));
			} catch(InvocationTargetException ex3) {
			   UniLog.log(ex3);
				throw(new SQLException("mysql getGeneratedKeys() InvocationTargetException."));
			}
		}
      st_insert.close();
      closeCursor();
	   return(updatecnt > 0 ? 0 : -1);
   }
   public String[] getColNames() {
		return(set_colnames);
   }
	public int getColType(String p_colname) {
		Integer ii = (Integer) all_hash_colindex.get(p_colname);
		if (ii == null)
		   return(-1);
		return(((Integer) all_coltypes.elementAt(ii.intValue())).intValue());
	}
   public int[] getColTypes() {
		return(set_coltypes);
   }
   public int[] getColLengths() {
		return(set_collengths);
   }
   public int[] getColSublengths() {
		return(set_colsublengths);
   }
   public void setErrmsg(String p_errmsg) {
      errmsg = p_errmsg;
   }
   public String getErrmsg() {
      return(errmsg);
   }
   public int setDebug(int p_level) {
      int olevel;
      olevel = debugLevel;
      debugLevel = p_level;
      return(olevel);
   }
   public Object getXXX(String p_what) {
      Object retvalue = null;
      if (p_what.compareTo("curwherecl") == 0) 
         retvalue = (Object) curwherecl;
      else if (p_what.compareTo("curorderby") == 0)
         retvalue = (Object) curorderby;
      return(retvalue);
   }
	public int setCursorbyIdxRowid(Connection p_dbConnection, int p_rowidx, Object p_rowid) throws SQLException {
	   if (rowidList == null)
		   return(-1); 
		if (p_rowidx < 0 || p_rowidx >= rowidList.size())
		   return(-1); 
	   if (!rowidList.elementAt(p_rowidx).equals(p_rowid))
		   return(-1);
	   curRowid = p_rowid;
		rowidIdx = p_rowidx;
		return(currentCursor(p_dbConnection));
	}
	public int getCursorCount() {
	   if (rowidList == null)
		   return(-1); 
	   return(rowidList.size());
	}
	public int getCurrentRowIndex() {
	   return(rowidIdx);
	}
	public static int typeStringToInt(String p_typestring) {
		if (p_typestring.equalsIgnoreCase("string"))
         return(SQLTYPE_CHAR);
		else if (p_typestring.equalsIgnoreCase("integer"))
         return(SQLTYPE_INTEGER);
		else if (p_typestring.equalsIgnoreCase("date"))
         return(SQLTYPE_DATE);
		else if (p_typestring.equalsIgnoreCase("float"))
         return(SQLTYPE_FLOAT);
		else if (p_typestring.equalsIgnoreCase("datetime"))
         return(SQLTYPE_TIMESTAMP);
		else if (p_typestring.equalsIgnoreCase("blob"))
         return(SQLTYPE_BLOB);
	   return(-1);
	}
	public static String typeIntToString(int p_type) {
		switch (p_type) {
         case SQLTYPE_CHAR: return("String");
         case SQLTYPE_INTEGER: return("Integer");
         case SQLTYPE_DATE: return("Date");
         case SQLTYPE_FLOAT: return("Float");
         case SQLTYPE_TIMESTAMP: return("Timestamp");
         case SQLTYPE_BLOB: return("Blob");
			default: UniLog.log("TableBrower.java:typeIntToString():p_type="+p_type+" not supported!");
		}
	   return(null);
	}
	public static int typeIntToSqltype(int p_type) {
		switch (p_type) {
         case SQLTYPE_CHAR: return(java.sql.Types.CHAR);
         case SQLTYPE_INTEGER: return(java.sql.Types.INTEGER);
         case SQLTYPE_DATE: return(java.sql.Types.DATE);
         case SQLTYPE_FLOAT: return(java.sql.Types.FLOAT);
         case SQLTYPE_TIMESTAMP: return(java.sql.Types.TIMESTAMP);
         case SQLTYPE_BLOB: return(java.sql.Types.LONGVARBINARY);
			default: UniLog.log("TableBrower.java:typeIntToSqltype():p_type="+p_type+" not supported!");
		}
	   return(-9999999);
	}
	public void addTBLookup(TBLookup p_tblookup) {
	   lookupVector.addElement(p_tblookup);
	}
	private void setupTBLookup(Connection p_dbConnection) {
	   TBLookup tblookup;
		int cnt, cnt2;
      String[] lkColNames;
      int[] lkColTypes;
      int[] lkColLengths;
      int[] lkColSublengths;
		StringBuffer sb_fromtable = null;
		StringBuffer sb_wherecl = null;

		try {
         cnt = lookupVector.size();
		   for (int i=0; i<cnt; i++) {
		      tblookup = (TBLookup) lookupVector.elementAt(i);
            cnt2 = tblookup.mapColNames.size();
				/*
            TableBrowser tmptable = new TableBrowser(p_dbConnection, tblookup.tabname);
				tmptable.setUniqueIdColumnName(getUniqueIdColumnName());
				*/
            TableBrowser tmptable = new TableBrowser(p_dbConnection, tblookup.tabname, getUniqueIdColumnName());
			   for (int j=0; j<cnt2; j++)
               tmptable.setColumn((String) tblookup.mapColNames.elementAt(j));
            tmptable.setColumnComplete(p_dbConnection);
            lkColNames = tmptable.getColNames();
            lkColTypes = tmptable.getColTypes();
            lkColLengths = tmptable.getColLengths();
            lkColSublengths = tmptable.getColSublengths();
				for (int j=0; j<lkColNames.length; j++) {
					String tmpcolname;
					tmpcolname = tblookup.tabaliase+"."+lkColNames[j];
               all_hash_colindex.put(tmpcolname, (Object) new Integer(all_colnames.size()));
               all_colnames.addElement(tmpcolname);
               all_colsublengths.addElement(new Integer(lkColLengths[j]));
               all_coltypes.addElement(new Integer(lkColTypes[j])); 
               all_collengths.addElement(new Integer(lkColLengths[j]));
               setColumn(tmpcolname);
				}
				if (sb_fromtable == null)
				   sb_fromtable = new StringBuffer();
				sb_fromtable.append(", ");
		      switch (databaseType) {
					case JdbcPool.JDBC_ORACLE:
						break;
	            case JdbcPool.JDBC_SCORPION:
					default:
						sb_fromtable.append(" outer ");
						break;
				}
				sb_fromtable.append(tblookup.tabname)
								.append(" ")
								.append(tblookup.tabaliase);
				if (sb_wherecl == null)
				   sb_wherecl = new StringBuffer();
				else 
				   sb_wherecl.append(" and ");
				for (int j=0; j<tblookup.condFieldNames.size(); j++) {
				   if (j > 0) 
				      sb_wherecl.append(" and ");
				   sb_wherecl.append(tabname).append("0.")
					          .append((String) tblookup.condFieldNames.elementAt(j))
					          .append(" = ")
								 .append(tblookup.tabaliase).append(".")
					          .append((String) tblookup.condColNames.elementAt(j));
		         switch (databaseType) {
					   case JdbcPool.JDBC_ORACLE:
							sb_wherecl.append("(+)");
						   break;
	               case JdbcPool.JDBC_SCORPION:
						default:
						   break;
				   }
				}
			   tmptable.close();
		   }
		} catch (Exception e) {
			UniLog.log(e);
		}
		if (sb_fromtable != null) 
	      lkfromtable = sb_fromtable.toString();
		if (sb_wherecl != null) 
	      lkwherecl = sb_wherecl.toString();
	}
	public Wherecl getWhereClauseByRowid(Object p_rowid) {
		switch (databaseType) {
		   case JdbcPool.JDBC_ORACLE:
            return(new Wherecl().appendString("rowid = CHARTOROWID('"+p_rowid+"')"));
		   case JdbcPool.JDBC_SCORPION:
		   case JdbcPool.JDBC_MYSQL:
            return(new Wherecl().appendString(getUniqueIdColumnName()+" = "+p_rowid));
		   default: 
				UniLog.log(new Exception("Invalid databaseType "+databaseType));
				return(null);
	   }
	}
   public static void main0(String[] args) {
      int cc;
      try {
			/*
         DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
         Connection conn= 
                 DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:mar","media", "media");
			*/
		   Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
         Connection conn= 
                 DriverManager.getConnection("jdbc:scorpion:perfrpc:localhost:5033:cp");
         TableBrowser mytable=new TableBrowser(conn, "areadef");
         mytable.setDebug(1);
			// mytable.setColumn("mvd_contact");
			// mytable.setColumn("mvd_name");
			// mytable.setColumnComplete(conn);
			/*
         mytable.setOrderby("mediavendor0.mvd_mvdcode");
	      mytable.addTBLookup(
            (new TBLookup("medium", "medium1"))
		                   .addMap("mdm_desc")
		                   .addCond("mvd_mdmcode", "mdm_mdmcode")
			);
			*/
			mytable.setColumns(conn);
	      UniLog.log("column list = ["+mytable.getColnameList(0)+"]");
	      UniLog.log("full column list = ["+mytable.getColnameList(1)+"]");
	      UniLog.log("start of next cursor()");
         // mytable.setField("mvd_mvdcode", "AAA1");
         // mytable.setField("mvd_name", "AAA1 Description");
			// mytable.insertRow();
         mytable.openCursor(conn);
	      for (;;) {
			   if (mytable.nextCursor(conn) < 0) 
				   break;
			   else {
			      UniLog.log(mytable.getRowid()+":"+mytable.getField("mvd_mvdcode").trim()+":"+mytable.getField(1).trim()+":"+mytable.getField("medium1.mdm_desc"));
               //mytable.setField("mvd_name", mytable.getField("mvd_name"));
               //mytable.updateRow();
			   }
			}
			/*
	      UniLog.log("start of prev cursor()");
	      for (;;) {
			   if (mytable.prevCursor(conn) < 0) 
				   break;
		      else {
			      UniLog.log(mytable.getRowid()+":"+mytable.getField("mvd_mvdcode").trim()+":"+mytable.getField(1).trim()+":"+mytable.getField("medium1.mdm_desc"));
               //mytable.clearField("mvd_name");
               //mytable.updateRow(conn);
			   }
			}
	      UniLog.log("start of next cursor()");
	      for (;;) {
			   if (mytable.nextCursor(conn) < 0) 
				   break;
		      else {
			      UniLog.log(mytable.getRowid()+":"+mytable.getField("mvd_mvdcode").trim()+":"+mytable.getField(1).trim()+":"+mytable.getField("medium1.mdm_desc"));
			   }
			}
			*/
      }
      catch (Exception e) {
			UniLog.log(e);
      }
   }
	public static Object getDefaultValue(int p_coltype) {
	   return(getDefaultValue(null, p_coltype));
	}
	public static Object getDefaultValue(String p_colname, int p_coltype) {
	   Object obj = null;
      switch (p_coltype) {
			case Types.LONGVARBINARY:
				obj = "";
				break;
			case Types.VARBINARY:
			case Types.BINARY:
				obj = new byte[0];
			   break;
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
         case Types.CHAR:
				obj = "";
				break;
         case Types.DATE:
				obj = DateUtil.getDate(SQLTYPE_DEFAULT_DATE);
				break;
         case Types.REAL:
         case Types.FLOAT:
         case Types.DOUBLE:
         case Types.DECIMAL:
			case Types.NUMERIC :
				obj = new Double(0.0);
				break;
         case Types.INTEGER:
         case Types.SMALLINT:
			case Types.BIGINT:
			case Types.TINYINT:
			case Types.BIT:
				obj = new Integer(0);
			break;
			case Types.BOOLEAN:
				obj = Boolean.FALSE;
				break;
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			case Types.TIME_WITH_TIMEZONE:
				obj = new java.sql.Timestamp((long) 0);
				break;
         default:
				if (p_colname != null)
               UniLog.log("TableBrowser: getDefaultValue(): unsupport type "+p_coltype+" for "+p_colname);
				else
               UniLog.log("TableBrowser: getDefaultValue(): unsupport type "+p_coltype);
		      break;
      }
		return(obj);
	}
	public void resetRowid() {
	   curRowid = null;
	   insertedRowid = null;
	}
	public boolean setFSelectMulti(boolean p_flag) {
	   boolean oflag = fSelectMulti;
	   fSelectMulti = p_flag;
	   return(oflag);
	}
	public void saveValues() {
		if (curValues == null) {
		   savedValues = null;
		   return;
	   }
	   savedValues = new String[curValues.length];
		for (int i=0; i<curValues.length; i++) {
		   savedValues[i] = new String(curValues[i]);
		}
	}
   public String[] getSavedFields() {
		return(savedValues);
   }
   public String getSavedField(String p_colname) {
		Integer idxInteger;
		idxInteger = (Integer) set_colindex.get(p_colname);
		if (idxInteger == null) return(null);
      return(getSavedField(idxInteger.intValue()));
   }
   public String getSavedField(int p_colidx) {
		if (savedValues == null) 
		   return(null);
		if (p_colidx < 0 || p_colidx >= savedValues.length) 
		   return(null);
		return(savedValues[p_colidx]);
   }
   public static void main_xx(String[] args) throws Exception {
		Class.forName("com.uniinformation.scorpion.driver.ScpDriver");
      Connection conn= 
                 DriverManager.getConnection("jdbc:scorpion:perfrpc:localhost:5033:cp");
      TableBrowser mytable=new TableBrowser(conn, "areadef");
      mytable.setDebug(1);
		mytable.setColumns(conn);
	   UniLog.log("column list = ["+mytable.getColnameList(0)+"]");
	   UniLog.log("full column list = ["+mytable.getColnameList(1)+"]");
      int cc = mytable.openCursor(conn);
	   for (;;) {
		   if (cc < 0)
			   break;
			UniLog.log("rowid:"+mytable.getRowid()+":"+mytable.getField("serial_id")+":"+mytable.getField("ad_name").trim()+":"+mytable.getField("ad_description").trim());
         //mytable.setField("mvd_name", mytable.getField("mvd_name"));
         //mytable.updateRow();
			cc = mytable.nextCursor(conn);
		}
	}
   public static void main(String[] args) throws Exception {
		Class.forName(JdbcPool.MYSQL_DRIVER);
      Connection conn= 
                 DriverManager.getConnection("jdbc:mysql://192.168.233.57/crm?user=root&password=donlai&useUnicode=TRUE&characterEncoding=UTF-8");
      TableBrowser mytable=new TableBrowser(conn, "newsitem");
      mytable.setDebug(1);
		mytable.setColumns(conn);
	   UniLog.log("column list = ["+mytable.getColnameList(0)+"]");
	   UniLog.log("full column list = ["+mytable.getColnameList(1)+"]");
      int cc = mytable.openCursor(conn);
	   for (;;) {
		   if (cc < 0)
			   break;
			UniLog.log("rowid:"+mytable.getRowid()+":"+mytable.getField("serial_id")+":"+mytable.getField("nwi_nwirg")+":"+mytable.getField("nwi_subject").trim());
         //mytable.setField("mvd_name", mytable.getField("mvd_name"));
         //mytable.updateRow();
			cc = mytable.nextCursor(conn);
		}
	}
	public Wherecl getCurrentRecordWherecl(Wherecl p_wherecl) {
		switch (databaseType) {
		   case JdbcPool.JDBC_ORACLE:
			   p_wherecl.andUniop("rowid", "=", curRowid);
			   break;
		   case JdbcPool.JDBC_SCORPION:
			default:
			   p_wherecl.andUniop(getUniqueIdColumnName(), "=", curRowid);
				break;
		}
		return(p_wherecl);
	}
	public void setUniqueIdColumnName(String p_colName) {
		uniIdColName = p_colName;
	}
	public String getUniqueIdColumnName() {
		return(uniIdColName);
	}
	public void addPreventUpdateColumn(String p_colName) {
	   preventUpdateColumns.put(p_colName, p_colName);
		str_update = null;
	}
	public Hashtable getPreventUpdateColumns() {
	   return(preventUpdateColumns);
	}
}
