package com.uniinformation.utils;

import java.util.*;
import java.sql.*;
import java.io.*;

import com.kyoko.common.DateUtil;
import com.uniinformation.cell.*;

public class SelectUtilCursor {
   TableRec tr = null;
	boolean fWithNull = false;
   ResultSet rs = null;
   PreparedStatement st = null;
	int colcnt = 0;
   public SelectUtilCursor(SelectUtil p_su, String p_sqlstr, Wherecl p_wherecl, boolean p_fWithNull) throws Exception {
	   fWithNull = p_fWithNull;

      StringBuffer sqlstr = new StringBuffer();
      ResultSetMetaData rsmd = null;
      int cc = -1;

      sqlstr.append(p_sqlstr);
      if (p_wherecl != null) {
	      String whereString = p_wherecl.getWhereString();
			if (whereString != null && !whereString.trim().equals(""))
	         sqlstr.append(" where ").append(p_wherecl.getWhereString());
		   if (p_wherecl.getOrderby() != null && !p_wherecl.getOrderby().equals(""))
	         sqlstr.append(" order by ")
				      .append(p_wherecl.getOrderby());
      }
      try {
	      st = p_su.getConnection().prepareStatement(sqlstr.toString());
	      if (p_wherecl != null)
	         p_wherecl.setStatementValue(st, 0);
//UniLog.logClass(this, "SelectUtilCursor():trace:getFetchSize()="+st.getFetchSize());
//UniLog.logClass(this, "SelectUtilCursor():trace:getMaxRows()="+st.getMaxRows());
	      rs = st.executeQuery();
//UniLog.logClass(this, "SelectUtilCursor():trace:executeQuery() returned");
	      rsmd = rs.getMetaData();
	      colcnt = rsmd.getColumnCount();
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
	      tr = new TableRec("none", labels, coltypes, collen);
	   } catch (Exception e) {
	      UniLog.logClass(this, "sqlstr="+sqlstr);
			UniLog.log(e);
			throw(e);
	   }
	}
   public TableRec getTableRec() {
	   return(tr);
	}
   public TableRec fetchNext() throws Exception {
	   if (!rs.next())
		   return(null);
	   tr.clear();
	   tr.addRecord();
	   for (int i=0; i<colcnt; i++) {
			Object obj = null;
			switch (tr.getFieldType(i)) {
			   case java.sql.Types.LONGVARBINARY:
			   case java.sql.Types.VARBINARY:
					obj = rs.getBytes(i+1);
					break;
				default:
					obj = rs.getObject(i+1);
					break;
			}
			if (obj != null && obj instanceof java.util.Date) {
				if (DateUtil.isDateNull((java.util.Date) obj))
				   obj = null;
			}
			if (obj == null && !fWithNull) {
				obj = TableBrowser.getDefaultValue(tr.getFieldName(i), tr.getFieldType(i));
			}
	      tr.setField(i, obj);
	   }
		/*
	   for (int i=0; i<colcnt; i++) {
			Object obj = rs.getObject(i+1);
			if (obj != null && obj instanceof java.util.Date) {
				if (DateUtil.isDateNull((java.util.Date) obj))
					obj = null;
			}
			if (obj == null && !fWithNull)
				obj = TableBrowser.getDefaultValue(tr.getFieldName(i), tr.getFieldType(i));
	      tr.setField(i, obj);
	   }
		*/
	   return(tr);
	}
   public void close() {
	   if (rs != null) {
		   try {
		      rs.close();
		   } catch (Exception ex) {
		      UniLog.logClass(this, "Exception ignored.");
		      UniLog.log(ex);
		   }
			rs = null;
	   }
	   if (st != null) {
		   try {
		      st.close();
		   } catch (Exception ex) {
		      UniLog.logClass(this, "Exception ignored.");
		      UniLog.log(ex);
		   }
			st = null;
	   }
	}
   protected void finalize() {
	   if (rs != null || st != null) {
		   UniLog.logClass(this, "finalize() before close()");
		}
	}
}
