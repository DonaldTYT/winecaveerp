package com.uniinformation.utils;
import java.lang.*;
import java.io.*;
import java.util.*;

import com.kyoko.common.DateUtil;
import com.uniinformation.rpccall.Strval;

import java.sql.*;
import java.lang.reflect.*;
import java.math.*;

public class Wherecl implements Serializable {
	StringBuffer sqlsb = new StringBuffer();
	Vector values = new Vector();
	String orderby = null;
	boolean fImpossible = false; 
	boolean fAlwaysTrue = false; 
	String dbLabel = null;
	public Wherecl() {
	}
	public StringBuffer getSqlsb() {
	   return(sqlsb);
	}
	public Wherecl appendString(String p_wherestring) {
		sqlsb.append(p_wherestring);
		return(this);
	}
	public Wherecl appendArgument(long p_argument) {
		values.addElement(new Long(p_argument));
		return(this);
	}
	public Wherecl appendArgument(int p_argument) {
		values.addElement(new Integer(p_argument));
		return(this);
	}
	public Wherecl appendArgument(String p_argument) {
		values.addElement(p_argument);
		return(this);
	}
	public Wherecl appendArgument(java.sql.Date p_argument) {
		values.addElement(p_argument);
		return(this);
	}
	public Wherecl appendArgument(java.sql.Time p_argument) {
		values.addElement(p_argument);
		return(this);
	}
	public Wherecl appendArgument(java.sql.Timestamp p_argument) {
		values.addElement(p_argument);
		return(this);
	}
//	public Wherecl appendArgument(java.util.Date p_argument) {
//		values.addElement(p_argument);
//		return(this);
//	}
	public Wherecl appendArgument(double p_argument) {
		values.addElement(new Double(p_argument));
		return(this);
	}
	public Wherecl appendArgument(Object p_argument) {
		values.addElement(p_argument);
		return(this);
	}
   public Wherecl genUniop(String p_andOr, String p_colname, String p_op, long p_int0) {
		sqlsb.append(" ").append(p_andOr).append(" ")
		     .append(p_colname)
		     .append(" ")
		     .append(p_op)
		     .append(" ? ");
		values.addElement(new Long(p_int0));
		return(this);
	}
   public Wherecl genUniop(String p_andOr, String p_colname, String p_op, int p_int0) {
		sqlsb.append(" ").append(p_andOr).append(" ")
		     .append(p_colname)
		     .append(" ")
		     .append(p_op)
		     .append(" ? ");
		values.addElement(new Integer(p_int0));
		return(this);
	}
   public Wherecl genUniop(String p_andOr, String p_colname, String p_op, String p_str0) {
		int cc;
		sqlsb.append(" ").append(p_andOr).append(" ")
		     .append(p_colname)
		     .append(" ")
		     .append(p_op)
		     .append(" ?");
		values.addElement(p_str0);
	   return(this);
	}
   public Wherecl genUniop(String p_andOr, String p_colname, String p_op, java.util.Date p_date0) {
		int cc;
		sqlsb.append(" ").append(p_andOr).append(" ")
		     .append(p_colname)
		     .append(" ")
		     .append(p_op)
		     .append(" ?");
		     values.addElement(p_date0);
	   return(this);
	}
   public Wherecl genUniop(String p_andOr, String p_colname, String p_op, double p_float0) {
		int cc;
		sqlsb.append(" ").append(p_andOr).append(" ")
		     .append(p_colname)
		     .append(" ")
		     .append(p_op)
		     .append(" ?");
		values.addElement(new Double(p_float0));
	   return(this);
	}
   public Wherecl genUniop(String p_andOr, String p_colname, String p_op, Object p_object) {
		if (p_object instanceof Integer)
		   return(genUniop(p_andOr, p_colname, p_op, ((Integer) p_object).intValue()));
		else if (p_object instanceof Long)
		   return(genUniop(p_andOr, p_colname, p_op, ((Long) p_object).longValue()));
		else if (p_object instanceof Double)
		   return(genUniop(p_andOr, p_colname, p_op, ((Double) p_object).doubleValue()));
		else if (p_object instanceof String)
		   return(genUniop(p_andOr, p_colname, p_op, (String) p_object));
		else if (p_object instanceof java.util.Date)
		   return(genUniop(p_andOr, p_colname, p_op, (java.util.Date) p_object));
		else if (p_object instanceof byte[])
		   return(genUniop(p_andOr, p_colname, p_op, (byte[]) p_object));
		else if (p_object instanceof BigDecimal)
		   return(genUniop(p_andOr, p_colname, p_op, ((BigDecimal) p_object).doubleValue()));
		else if (p_object instanceof BigInteger)
		   return(genUniop(p_andOr, p_colname, p_op, ((BigInteger) p_object).longValue()));
		else 
		   UniLog.log(new Exception("object type not found!"));
	   return(this);
	}
   public Wherecl genRange(String p_andOr, String p_colname, long p_int0, long p_int1) {
	   if (p_int0 == p_int1) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" = ? ");
		   values.addElement(new Long(p_int0));
		}
		else if (p_int0 < p_int1) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ? ");
		   values.addElement(new Long(p_int0));
		   values.addElement(new Long(p_int1));
		}
	   else if (p_int1 > p_int0) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ?");
		   values.addElement(new Long(p_int1));
		   values.addElement(new Long(p_int0));
      }
	   return(this);
	}
   public Wherecl genRange(String p_andOr, String p_colname, int p_int0, int p_int1) {
	   if (p_int0 == p_int1) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" = ? ");
		   values.addElement(new Integer(p_int0));
		}
		else if (p_int0 < p_int1) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ? ");
		   values.addElement(new Integer(p_int0));
		   values.addElement(new Integer(p_int1));
		}
	   else if (p_int1 > p_int0) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ? ");
		   values.addElement(new Integer(p_int1));
		   values.addElement(new Integer(p_int0));
      }
	   return(this);
	}
   public Wherecl genRange(String p_andOr, String p_colname, String p_str0, String p_str1) {
		int cc;
		cc = p_str0.compareTo(p_str1);
		if (cc == 0) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" = ?");
		   values.addElement(p_str0);
		}
		else if (cc < 0) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ?");
		   values.addElement(p_str0);
		   values.addElement(p_str1);
		}
	   else {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ?");
		   values.addElement(p_str1);
		   values.addElement(p_str0);
      }
	   return(this);
	}
   public Wherecl genRange(String p_andOr, String p_colname, java.util.Date p_date0, java.util.Date p_date1) {
		int cc;
		cc = p_date0.compareTo(p_date1);
		if (cc == 0) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" = ?");
		   values.addElement(p_date0);
		}
		else if (cc < 0) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ?");
		   values.addElement(p_date0);
		   values.addElement(p_date1);
		}
	   else {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ?");
		   values.addElement(p_date1);
		   values.addElement(p_date0);
      }
	   return(this);
	}
   public Wherecl genRange(String p_andOr, String p_colname, double p_float0, double p_float1) {
		int cc;
		if (p_float0 == p_float1) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" = ?");
		   values.addElement(new Double(p_float0));
		}
		else if (p_float0 < p_float1) {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ?");
		   values.addElement(new Double(p_float0));
		   values.addElement(new Double(p_float1));
		}
	   else {
		   sqlsb.append(" ").append(p_andOr).append(" ")
			     .append(p_colname)
			     .append(" between ? and ?");
		   values.addElement(new Double(p_float1));
		   values.addElement(new Double(p_float0));
      }
	   return(this);
	}
   public Wherecl genInList(String p_andOr, String p_colname, String p_operator, Object ... p_objs) {
	   sqlsb.append(" ").append(p_andOr).append(" ")
		     .append(p_colname)
		     .append(" ")
		     .append(p_operator)
		     .append(" (");
	   for(int i=0;i<p_objs.length;i++) {
		     if(i>0) sqlsb.append(",");
		     sqlsb.append("?");
		     values.addElement(p_objs[i]);
	   }
	   sqlsb.append(")");
	   return(this);
   }
   public Wherecl genRange(String p_andOr, String p_colname, Object p_obj0, Object p_obj1) {
		if (p_obj0 instanceof Integer)
		   return(genRange(p_andOr, p_colname, ((Integer) p_obj0).intValue(), ((Integer) p_obj1).intValue()));
		else if (p_obj0 instanceof Long)
		   return(genRange(p_andOr, p_colname, ((Long) p_obj0).longValue(), ((Long) p_obj1).longValue()));
		else if (p_obj0 instanceof Double)
		   return(genRange(p_andOr, p_colname, ((Double) p_obj0).doubleValue(), ((Double) p_obj1).doubleValue()));
		else if (p_obj0 instanceof String)
		   return(genRange(p_andOr, p_colname, (String) p_obj0, (String) p_obj1));
		else if (p_obj0 instanceof java.util.Date)
		   return(genRange(p_andOr, p_colname, (java.util.Date) p_obj0, (java.util.Date) p_obj1));
		else if (p_obj0 instanceof BigDecimal)
		   return(genRange(p_andOr, p_colname, ((BigDecimal) p_obj0).doubleValue(), ((BigDecimal) p_obj1).doubleValue()));
		else if (p_obj0 instanceof BigInteger)
		   return(genRange(p_andOr, p_colname, ((BigInteger) p_obj0).longValue(), ((BigInteger) p_obj1).longValue()));
		else
		   UniLog.log(new Exception("genRange(): p_obj0 unsupported class: "+p_obj0.getClass().getName()));
	   return(this);
	}
   public Wherecl andUniop(String p_colname, String p_op, BigInteger p_int0) {
      return(genUniop("and", p_colname, p_op, p_int0.longValue()));
	}
   public Wherecl andUniop(String p_colname, String p_op, BigDecimal p_int0) {
      return(genUniop("and", p_colname, p_op, p_int0.doubleValue()));
	}
   public Wherecl andUniop(String p_colname, String p_op, long p_int0) {
      return(genUniop("and", p_colname, p_op, p_int0));
	}
   public Wherecl andUniop(String p_colname, String p_op, int p_int0) {
      return(genUniop("and", p_colname, p_op, p_int0));
	}
   public Wherecl andUniop(String p_colname, String p_op, String p_str0) {
      return(genUniop("and", p_colname, p_op, p_str0));
	}
   public Wherecl andUniop(String p_colname, String p_op, java.util.Date p_date0) {
      return(genUniop("and", p_colname, p_op, p_date0));
	}
   public Wherecl andUniop(String p_colname, String p_op, double p_float0) {
      return(genUniop("and", p_colname, p_op, p_float0));
	}
   public Wherecl andUniop(String p_colname, String p_op, Object p_object) {
      return(genUniop("and", p_colname, p_op, p_object));
	}
   public Wherecl andRange(String p_colname, BigInteger p_int0, BigInteger p_int1) {
      return(genRange("and", p_colname, p_int0.longValue(), p_int1.longValue()));
	}
   public Wherecl andRange(String p_colname, BigDecimal p_int0, BigDecimal p_int1) {
      return(genRange("and", p_colname, p_int0.doubleValue(), p_int1.doubleValue()));
	}
   public Wherecl andRange(String p_colname, long p_int0, long p_int1) {
      return(genRange("and", p_colname, p_int0, p_int1));
	}
   public Wherecl andRange(String p_colname, int p_int0, int p_int1) {
      return(genRange("and", p_colname, p_int0, p_int1));
	}
   public Wherecl andRange(String p_colname, String p_str0, String p_str1) {
      return(genRange("and", p_colname, p_str0, p_str1));
	}
   public Wherecl andRange(String p_colname, java.util.Date p_date0, java.util.Date p_date1) {
      return(genRange("and", p_colname, p_date0, p_date1));
	}
   public Wherecl andRange(String p_colname, double p_float0, double p_float1) {
      return(genRange("and", p_colname, p_float0, p_float1));
	}
   public Wherecl andRange(String p_colname, Object p_obj0, Object p_obj1) {
      return(genRange("and", p_colname, p_obj0, p_obj1));
	}
   public Wherecl orUniop(String p_colname, String p_op, BigInteger p_int0) {
      return(genUniop("or", p_colname, p_op, p_int0.longValue()));
	}
   public Wherecl orUniop(String p_colname, String p_op, BigDecimal p_int0) {
      return(genUniop("or", p_colname, p_op, p_int0.doubleValue()));
	}
   public Wherecl orUniop(String p_colname, String p_op, long p_int0) {
      return(genUniop("or", p_colname, p_op, p_int0));
	}
   public Wherecl orUniop(String p_colname, String p_op, int p_int0) {
      return(genUniop("or", p_colname, p_op, p_int0));
	}
   public Wherecl orUniop(String p_colname, String p_op, String p_str0) {
      return(genUniop("or", p_colname, p_op, p_str0));
	}
   public Wherecl orUniop(String p_colname, String p_op, java.util.Date p_date0) {
      return(genUniop("or", p_colname, p_op, p_date0));
	}
   public Wherecl orUniop(String p_colname, String p_op, double p_float0) {
      return(genUniop("or", p_colname, p_op, p_float0));
	}
   public Wherecl orUniop(String p_colname, String p_op, Object p_object) {
      return(genUniop("or", p_colname, p_op, p_object));
	}
   public Wherecl orRange(String p_colname, BigInteger p_int0, BigInteger p_int1) {
      return(genRange("or", p_colname, p_int0.longValue(), p_int1.longValue()));
	}
   public Wherecl orRange(String p_colname, BigDecimal p_int0, BigDecimal p_int1) {
      return(genRange("or", p_colname, p_int0.doubleValue(), p_int1.doubleValue()));
	}
   public Wherecl orRange(String p_colname, long p_int0, long p_int1) {
      return(genRange("or", p_colname, p_int0, p_int1));
	}
   public Wherecl orRange(String p_colname, int p_int0, int p_int1) {
      return(genRange("or", p_colname, p_int0, p_int1));
	}
   public Wherecl orRange(String p_colname, String p_str0, String p_str1) {
      return(genRange("or", p_colname, p_str0, p_str1));
	}
   public Wherecl orRange(String p_colname, java.util.Date p_date0, java.util.Date p_date1) {
      return(genRange("or", p_colname, p_date0, p_date1));
	}
   public Wherecl orRange(String p_colname, double p_float0, double p_float1) {
      return(genRange("or", p_colname, p_float0, p_float1));
	}
   public Wherecl orRange(String p_colname, Object p_obj0, Object p_obj1) {
      return(genRange("or", p_colname, p_obj0, p_obj1));
	}
	public Wherecl stripAnd() {
	   String tmpstr;
		tmpstr = sqlsb.toString().trim();
		if (tmpstr.length() >= 4 && tmpstr.substring(0, 4).equalsIgnoreCase("and "))
		   sqlsb = new StringBuffer(tmpstr.substring(4));
	   return(this);
	}
	static public String stripAnd(String p_string) {
	   String tmpstr;
		tmpstr = p_string.trim();
		if (tmpstr.length() >= 4 && tmpstr.substring(0, 4).equalsIgnoreCase("and "))
		   return(tmpstr.substring(4));
	   else
		   return(tmpstr);
	}
	public Wherecl stripOr() {
	   String tmpstr;
		tmpstr = sqlsb.toString().trim();
		if (isOr())
		   sqlsb = new StringBuffer(tmpstr.substring(3));
	   return(this);
	}
	static public String stripOr(String p_string) {
	   String tmpstr;
		tmpstr = p_string.trim();
		if (tmpstr.length() >= 3 && tmpstr.substring(0, 3).equalsIgnoreCase("or "))
		   return(tmpstr.substring(3));
	   else
		   return(tmpstr);
	}
	public int getArgumentCount() {
	   if (values == null)
		   return(0);
	   return(values.size());
	}
	public String argumentToString() {
		StringBuffer sb;
		sb = new StringBuffer();
		for(int i=0;i<values.size();i++) {
			Object obj = values.elementAt(i);
		  if(i > 0)
		  	sb.append(", ");
		  sb.append("[").append(obj != null ? obj.getClass().getName() : "null").append("]");
		  sb.append(obj);
		}
		return(sb.toString());
	}
	public String toString() {
		StringBuffer sb;
		sb = new StringBuffer();
		sb.append(sqlsb.toString())
		  .append("(");
		for(int i=0;i<values.size();i++) {
			Object obj = values.elementAt(i);
		  if(i > 0)
		  	sb.append(", ");
		  sb.append("[").append(obj != null ? obj.getClass().getName() : "null").append("]");
		  sb.append(obj);
		}
	   sb.append(")");
		if (isImpossible())
		   sb.append("[Impossible]");
		if (isAlwaysTrue())
		   sb.append("[AlwaysTrue]");
	   if (dbLabel != null)
		   sb.append("[").append(dbLabel).append("]");
		if (orderby != null)
		   sb.append(" order by ").append(orderby);
		return(sb.toString());
	}
	public String getWhereString() {
	   if (fImpossible)
		   return(" 1 < 0 ");
		if (fAlwaysTrue)
		   return("");
		if (sqlsb == null) return("");
	   return(sqlsb.toString());
	}
	public void setStatementValue(PreparedStatement p_statement, int p_offset) throws SQLException {
		if (values == null) return;
		int cnt = values.size();
		for (int i=0; i<cnt; i++) {
		   if (values.elementAt(i) == null)
				throw(new SQLException("setStatementValue(): value is null ["+i+"]"));
		   else if (values.elementAt(i) instanceof String) {
            p_statement.setString(p_offset+i+1, (String) values.elementAt(i));
			}
		   else if (values.elementAt(i) instanceof Double) {
            p_statement.setDouble(p_offset+i+1, ((Double) values.elementAt(i)).doubleValue());
			}
		   else if (values.elementAt(i) instanceof Float) {
            p_statement.setDouble(p_offset+i+1, (double) ((Float) values.elementAt(i)).floatValue());
			}
		   else if (values.elementAt(i) instanceof java.sql.Time) {
            p_statement.setTime(p_offset+i+1, (java.sql.Time) values.elementAt(i));
			}
		   else if (values.elementAt(i) instanceof java.sql.Timestamp) {
            p_statement.setTimestamp(p_offset+i+1, (java.sql.Timestamp) values.elementAt(i));
			}
		   else if (values.elementAt(i) instanceof java.sql.Date) {
            p_statement.setDate(p_offset+i+1, new java.sql.Date(((java.util.Date) values.elementAt(i)).getTime()));
			}
		   else if (values.elementAt(i) instanceof Integer) {
            p_statement.setInt(p_offset+i+1, ((Integer) values.elementAt(i)).intValue());
			}
		   else if (values.elementAt(i) instanceof Long) {
            p_statement.setLong(p_offset+i+1, ((Long) values.elementAt(i)).longValue());
			}
		   else if (values.elementAt(i) instanceof byte[]) {
            p_statement.setBytes(p_offset+i+1, (byte[]) values.elementAt(i));
			}
		   else if (values.elementAt(i) instanceof BigDecimal) {
            p_statement.setDouble(p_offset+i+1, ((BigDecimal) values.elementAt(i)).doubleValue());
			}
		   else if (values.elementAt(i) instanceof BigInteger) {
            p_statement.setDouble(p_offset+i+1, ((BigInteger) values.elementAt(i)).longValue());
			}
		   else if (values.elementAt(i) instanceof Boolean) {
            p_statement.setBoolean(p_offset+i+1, ((Boolean) values.elementAt(i)).booleanValue());
			}
		   else if (values.elementAt(i) instanceof java.util.Date) {
			   p_statement.setDate(p_offset+i+1, new java.sql.Date(((java.util.Date) values.elementAt(i)).getTime()));
			}
		   else if (values.elementAt(i) instanceof Strval) {
			   p_statement.setObject(p_offset+i+1, values.elementAt(i));
			}
			else
				throw(new SQLException("setStatementValue(): values type not recognised ["+i+"]"+values.elementAt(i).getClass().getName()));
		}
	}
	public boolean isNull() {
	   return(sqlsb.toString().trim().equals("") && values.size() == 0 && !fImpossible && !fAlwaysTrue);
	}
	public Vector getValues() {
	   return(values);
	}
	public boolean isAnd() {
	   return(sqlsb.length() >= 4 && sqlsb.substring(0, 4).trim().equalsIgnoreCase("and"));
	}
	public boolean isOr() {
	   return(sqlsb.length() >= 3 && sqlsb.substring(0, 3).trim().equalsIgnoreCase("or"));
	}
	public Wherecl andWherecl(Wherecl p_wherecl) {
		if (p_wherecl == null)
		   return(this);
		if (p_wherecl.getOrderby() != null 
		    && !p_wherecl.getOrderby().trim().equals(""))
		   setOrderby(p_wherecl.getOrderby());
	   if (p_wherecl.isNull())
		   return(this);
		if (p_wherecl.isImpossible()) {
		   setImpossible(true);
			return(this);
		}
	   if (p_wherecl.isAlwaysTrue())
		   return(this);
		if (isImpossible())
		   return(this);
	   if (isAlwaysTrue()) {
	      copy(p_wherecl);
			return(this);
		}
	   String tmpstr;
		if (p_wherecl.isOr())
	      tmpstr = "("+Wherecl.stripOr(p_wherecl.getWhereString()).trim()+")";
		else
	      tmpstr = Wherecl.stripAnd(p_wherecl.getWhereString()).trim();
		if (!tmpstr.trim().equals("")) {
			StringBuffer sb = new StringBuffer();
			sb.append(sqlsb.toString())
			  .append(" and ")
			  .append(tmpstr);
		   sqlsb = sb;
		}
		Vector v = p_wherecl.getValues();
		if (v.size() > 0) {
		   VectorUtil vu = new VectorUtil(values);
			vu.addElements(v.toArray());
			values = vu.toVector();
		}
	   return(this);
	}
	public Wherecl orWherecl(Wherecl p_wherecl) {
		if (p_wherecl == null)
		   return(this);
	   if (p_wherecl.isNull())
		   return(this);
	   if (p_wherecl.isImpossible())
		   return(this);
	   if (p_wherecl.isAlwaysTrue()) {
	      setAlwaysTrue(true);
			return(this);
		}
		if (isImpossible()) {
	      copy(p_wherecl);
		   return(this);
		}
		if (isAlwaysTrue())
		   return(this);
	   String tmpstr = Wherecl.stripAnd(p_wherecl.getWhereString()).trim();
		if (!tmpstr.trim().equals("")) {
			StringBuffer sb = new StringBuffer();
			if (isAnd())
			   sb.append("and ");
			else if (isOr())
			   sb.append("or ");
			if (sqlsb.toString().trim().equals(""))
			   sb.append(tmpstr);
			else
			   sb.append("(( ")
			     .append(stripAnd(sqlsb.toString()))
			     .append(" ) or ( ")
			     .append(tmpstr)
			     .append(" ))");
		   sqlsb = sb;
		}
		Vector v = p_wherecl.getValues();
		if (v.size() > 0) {
		   VectorUtil vu = new VectorUtil(values);
			vu.addElements(v.toArray());
			values = vu.toVector();
		}
	   return(this);
	}
	public Wherecl notWherecl() {
	   StringBuffer sb = new StringBuffer();
		if (isAnd())
			sb.append("and ");
		sb.append("not ( ")
			  .append(stripAnd(sqlsb.toString()))
			  .append(" ) ");
		sqlsb = sb;
	   return(this);
	}
	public Wherecl setOrderby(String p_orderby) {
	   orderby = p_orderby;
	   return(this);
	}
	public String getOrderby() {
	   return(orderby);
	}
	public Wherecl copy(Wherecl p_wc) {
		sqlsb = p_wc.getSqlsb();
		values = p_wc.getValues();
		orderby = p_wc.getOrderby();
	   fImpossible = p_wc.isImpossible();
	   fAlwaysTrue = p_wc.isAlwaysTrue();
	   dbLabel = p_wc.dbLabel;
		return(this);
	}
	public int hashCode() {
		return(toString().hashCode());
	}
	public boolean equals(Object p_obj) {
	   if (p_obj == null)
		   return(false);
	   if (!(p_obj instanceof Wherecl))
		   return(false);
		Wherecl wc1 = (Wherecl) p_obj;
	   if (!getSqlsb().toString().equals(wc1.getSqlsb().toString())) 
		   return(false);
	   if (getOrderby() != null && !getOrderby().equals(wc1.getOrderby()))
		   return(false);
		if (getOrderby() == null && wc1.getOrderby() != null)
		   return(false);
	   Vector v0 = getValues();
	   Vector v1 = wc1.getValues();
		if (v0.size() != v1.size())
		   return(false);
	   for (int i=0; i<v0.size(); i++) {
		   if (!v0.elementAt(i).equals(v1.elementAt(i)))
			   return(false);
		}
      if (isImpossible() != wc1.isImpossible())
		   return(false);
      if (isAlwaysTrue() != wc1.isAlwaysTrue())
		   return(false);
		return(true);
	}
//	public Wherecl andList(SelectUtil p_su, String p_fieldName, Enumeration p_en) {
//		boolean started = false;
//		Hashtable h = new Hashtable();
//	   for (; p_en.hasMoreElements(); ) {
//		   Object obj = p_en.nextElement();
//		   h.put(obj, "");
//		}
//      if (JdbcPool.findConnectionType(p_su.getConnection()) == JdbcPool.JDBC_SCORPION) {
//	      for (Enumeration en=h.keys(); en.hasMoreElements(); ) {
//		      Object obj = en.nextElement();
//				if (!started)
//				   appendString(" and (");
//				else
//				   appendString(" or ");
//				appendString(p_fieldName+" = ?");
//				appendArgument(obj);
//			   started = true;
//			}
//			if (started)
//			   appendString(" ) ");
//		}
//		else 
//		{
//	      for (Enumeration en=h.keys(); en.hasMoreElements(); ) {
//		      Object obj = en.nextElement();
//				if (!started) {
//				   appendString(" and ");
//					appendString(p_fieldName);
//				   appendString(" in (");
//				}
//				else
//				   appendString(",");
//				appendString("?");
//				appendArgument(obj);
//			   started = true;
//			}
//			if (started)
//			   appendString(" ) ");
//      }
//		return(this);
//	}
	/*
   public static void main_xxx(String args[]) {
	   Wherecl wc;

		wc = (new Wherecl()).andRange("abc_field1", "ABC", "DEF")
		                    .andRange("abc_field2", 10, 20)
		                    .andRange("abc_field3", 10.1, 20.1)
		                    .andRange("abc_field4", new java.util.Date(new java.util.Date().getTime()), new java.util.Date(new java.util.Date().getTime()));
	   System.out.println(wc.toString());
	   System.out.println("argument count = "+wc.getArgumentCount());
		
	   Wherecl wc2;
		wc2 = (new Wherecl()).andRange("abc_field1", "ABC", "DEF")
		                    .andRange("abc_field2", 10, 20)
		                    .andRange("abc_field3", 10.1, 20.1)
		                    .andRange("abc_field4", new java.util.Date(new java.util.Date().getTime()), new java.util.Date(new java.util.Date().getTime()));
	   wc.andWherecl(wc2);
	   System.out.println(wc.toString());
	   System.out.println("argument count = "+wc.getArgumentCount());
	}
		*/
	public Wherecl setImpossible(boolean p_flag) {
	   fImpossible = p_flag;
		if (p_flag) {
	      sqlsb = new StringBuffer();
	      values = new Vector();
	      orderby = null;
	      fAlwaysTrue = false;
		}
		return(this);
	}
	public boolean isImpossible() {
	   return(fImpossible);
	}
	public Wherecl setAlwaysTrue(boolean p_flag) {
	   fAlwaysTrue = p_flag;
		if (p_flag) {
	      sqlsb = new StringBuffer();
	      values = new Vector();
	      orderby = null;
	      fImpossible = false;
		}
		return(this);
	}
	public boolean isAlwaysTrue() {
	   return(fAlwaysTrue);
	}
   public static void main(String[] args) {
	   /*
	   Wherecl wc = new Wherecl()
		                 .orUniop("abc", "=", "x1")
		                 .orUniop("abc", "=", "x2")
		                 .orUniop("abc", "=", "x3")
		                 .orUniop("abc", "=", "x4");
	   Wherecl wc0 = new Wherecl();
	   */
//	   Wherecl wc = new Wherecl()
//	   						.genInList("and", "abc", "not in", "AAA","BBB","ACC")
//	   						.andUniop("xyz", "=",3)
//	   						.orWherecl(
//	   								new Wherecl()
//	   								.andUniop("abc", "=", "x4")
//	   								.andUniop("ddd", "=",4)
//	   								)
//                 			;
	   Wherecl wc = new Wherecl()
	   						.genRange("and","xxx",1,2)
	   								;
	   try {
	   UniLog.log("toString: "+wc.toString());
	   UniLog.log("toWhereClString: "+wc.toWhereclString());
	   UniLog.log("getWhereString: "+wc.getWhereString());
	   } catch (Exception ex) {
		   UniLog.log(ex);
	   }
	}
	public void setDbLabel(String p_dbLabel) {
	   dbLabel = p_dbLabel;
	}
	public String getDbLabel() {
	   return(dbLabel);
	}
	public Object clone() {
      Wherecl wc = new Wherecl();
		wc.appendString(sqlsb.toString());
		wc.values = (Vector) values.clone();
		wc.orderby = orderby == null ? null : new String(orderby);
      wc.fImpossible = fImpossible;
      wc.fAlwaysTrue = fAlwaysTrue;
		wc.dbLabel = dbLabel;
	   return(wc);
	}
	public String toWhereclString() throws Exception {
	   StringBuffer sb = new StringBuffer();
	   int len = sqlsb.length();
		int argIdx = 0;
		for (int i=0; i<len; i++) {
		   char c = sqlsb.charAt(i);
			if (c != '?') {
			   sb.append(c);
			   continue;
		   }
			if (argIdx >= values.size()) {
			   sb.append(c);
			   continue;
	      }
			Object value = values.elementAt(argIdx++);
		   if (value instanceof String) {
			   String str = (String) value;
				if (str.indexOf('"') >= 0) {
				   if (str.indexOf('\'') >= 0)
					   throw(new Exception("value contains both double and single quote ["+str+"]"));
				   sb.append('\'').append(str).append('\'');
				}
				else
				   sb.append('"').append(str).append('"');
			}
		   else if (value instanceof java.util.Date)
	         sb.append(DateUtil.toDateString((java.util.Date) value, "yyyy/mm/dd"));
			else
			   sb.append(value);
		}
		return(sb.toString());
	}
   public Wherecl genInList(String p_andOr, String p_colname, String p_operator,Set p_args) {
	   sqlsb.append(" ").append(p_andOr).append(" ")
		     .append(p_colname)
		     .append(" ")
		     .append(p_operator)
		     .append(" (");
	   int i=0;
	   for(Object arg : p_args) {
		   if(i>0) sqlsb.append(",");
		   sqlsb.append("?");
		   i++;
		   values.addElement(arg);
	   }
	   sqlsb.append(")");
	   return(this);
   }
}
