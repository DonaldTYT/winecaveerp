package com.uniinformation.scorpion.driver;
import com.uniinformation.utils.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.util.Calendar;
public class DbmResultSetMetaData implements ResultSetMetaData {
	DbmResultSet resultset;
	public DbmResultSetMetaData(DbmResultSet p_resultset) {
		resultset = p_resultset;
	}
	public int getColumnCount() throws SQLException {
		return(resultset.ScpResultGetStmt().getSelectlist().size());
	}
	public boolean isAutoIncrement(int column) throws SQLException {
		return(false);
	}
	public boolean isCaseSensitive(int column) throws SQLException {
		return(false);
	}
	public boolean isSearchable(int column) throws SQLException {
		return(false);
	}
	public boolean isCurrency(int column) throws SQLException {
		return(false);
	}
	public int isNullable(int column) throws SQLException {
		return(0);
	}
   int columnNoNulls = 0;
   int columnNullable = 1;
   int columnNullableUnknown = 2;
	public boolean isSigned(int column) throws SQLException {
		return(false);
	}
	public int getColumnDisplaySize(int column) throws SQLException {
		ScpField field;
		field = (ScpField ) (resultset.ScpResultGetStmt().getSelectlist().get(column-1));
		return(field.getFdlen());
	}
	public String getColumnLabel(int column) throws SQLException {
		ScpField field;
		field = (ScpField) (resultset.ScpResultGetStmt().getSelectlist().get(column-1));
		return(field.getColname());
	}
	public String getColumnName(int column) throws SQLException {
		ScpField field;
		field = (ScpField ) (resultset.ScpResultGetStmt().getSelectlist().get(column-1));
		return(field.getColname());
	}
	public String getSchemaName(int column) throws SQLException {
		return(null);
	}
	public int getPrecision(int column) throws SQLException {
		ScpField field;
		field = (ScpField ) (resultset.ScpResultGetStmt().getSelectlist().get(column-1));
		switch (field.getFdtype()) {
		   case ScpField.FDTYPE_CHAR:
			   return(0);
	      case ScpField.FDTYPE_INTEGER:
			   return(10);
	      case ScpField.FDTYPE_FLOAT:
			   return(20);
	      case ScpField.FDTYPE_SERIAL:
			   return(10);
	      case ScpField.FDTYPE_DATE:
			   return(0);
		}
		throw(new ScpSQLException("unknown columntype"));
	}
	public int getScale(int column) throws SQLException {
		ScpField field;
		field = (ScpField ) (resultset.ScpResultGetStmt().getSelectlist().get(column-1));
		switch (field.getFdtype()) {
		   case ScpField.FDTYPE_CHAR:
			   return(0);
	      case ScpField.FDTYPE_INTEGER:
			   return(10);
	      case ScpField.FDTYPE_FLOAT:
			   return(6);
	      case ScpField.FDTYPE_SERIAL:
			   return(0);
	      case ScpField.FDTYPE_DATE:
			   return(0);
		}
		throw(new ScpSQLException("unknown columntype"));
	}
	public String getTableName(int column) throws SQLException {
		ScpField field;
		field = (ScpField ) (resultset.ScpResultGetStmt().getSelectlist().get(column-1));
		if (field.getTabname() == null)
		   return("");
		else
		   return(field.getTabname());
	}
	public String getCatalogName(int column) throws SQLException {
		return(null);
	}
	public int getColumnType(int column) throws SQLException {
		ScpField field;
		field = (ScpField ) (resultset.ScpResultGetStmt().getSelectlist().get(column-1));
		switch (field.getFdtype()) {
		   case ScpField.FDTYPE_CHAR:
			   return(java.sql.Types.CHAR);
	      case ScpField.FDTYPE_INTEGER:
			   return(java.sql.Types.INTEGER);
	      case ScpField.FDTYPE_FLOAT:
			   return(java.sql.Types.DOUBLE);
	      case ScpField.FDTYPE_SERIAL:
			   return(java.sql.Types.INTEGER);
	      case ScpField.FDTYPE_DATE:
			   return(java.sql.Types.DATE);
		}
		throw(new ScpSQLException("unknown columntype"));
	}
	public String getColumnTypeName(int column) throws SQLException {
		return(null);
	}
	public boolean isReadOnly(int column) throws SQLException {
		return(false);
	}
	public boolean isWritable(int column) throws SQLException {
		return(false);
	}
	public boolean isDefinitelyWritable(int column) throws SQLException {
		return(false);
	}
   public String getColumnClassName(int column) throws SQLException {
		return(null);
	}
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
	   return(false);
   }
	public <T> T unwrap(Class<T> iface) throws SQLException {
	   throw(new SQLException("Not yet implemented"));
	}
}
