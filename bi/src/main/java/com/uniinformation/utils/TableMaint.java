package com.uniinformation.utils;

import java.sql.*;
import java.util.*;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;

import java.io.*;

public class TableMaint
{
   final static int MAX_HTML_TEXT_INPUT_LENGTH = 100;
	boolean fSelectMulti = false;
	private String curtabname;
	private Connection dbconn;
	private TableBrowser tb;
	private Wherecl curwherecl;
	private String curorderbycollist;
	private String curorderby;
	private boolean fcurrent;
	private Hashtable colAttributes;
   Hashtable addColtypes;
   Hashtable addCollens;
   Hashtable addColsublens;
   Hashtable addColvalues;
   Hashtable addColcomments;
	String errmsg;
	int curRecListOffset;
	Vector curRecMultiList;
	Vector curRecList;
	Vector curRecRowidList;
	Vector queryColnames;
	Vector queryColvalues;
	Hashtable myColIndexes = null;
	String userColNames[] = null;
	String myColNames[] = null;
	int myColTypes[] = null;
	int myColLengths[] = null;
	int myColSublengths[] = null;
	int fdebug = 0;
   // DataSource poolDataSource;
   JdbcPool poolDataSource = null;
	int fbegin;
	boolean initCompleted;
	/*
   TMStorageContainer storageContainer = null;
	*/
	Vector v_tblookup = new Vector();
	Vector v_qtblookup = null;
	String language = "en";
	Hashtable xslhash = new Hashtable();
	TMFormatDate defDateFormat = new TMFormatDate("yyyy/mm/dd");
   String addXmlString = null;
	//WebLogin webLogin = null;
	String appName = null;
   String uniIdColName = null;

	public TableMaint(String p_tabname, JdbcPool p_jdbcPool) throws Exception {
		this(p_tabname, p_jdbcPool, "serial_id");
	}
	public TableMaint(String p_tabname, JdbcPool p_jdbcPool, String p_uniIdColName) throws Exception {
      poolDataSource = p_jdbcPool;
		uniIdColName = p_uniIdColName; 
	   doCreate(p_tabname);
	}
	protected void finalize() throws Throwable {
	   super.finalize();
		remove();
	}
	private void initVariable() {
	   curtabname = null;
	   dbconn = null;
	   tb = null;
	   curwherecl = null;
	   curorderby = null;
	   curorderbycollist = null;
	   fcurrent = false;
	   colAttributes = null;
      addColtypes = new Hashtable();
      addCollens = new Hashtable();
      addColsublens = new Hashtable();
      addColvalues = new Hashtable();
      addColcomments = new Hashtable();
	   errmsg = null;
		curRecListOffset = 0;
	   curRecMultiList = null;
	   curRecList = null;
	   curRecRowidList = null;
		fbegin = 0;
	   myColIndexes = null;
	   myColNames = null;
	   myColTypes = null;
	   myColLengths = null;
	   myColSublengths = null;
	   initCompleted = false;
      xslhash = new Hashtable();
		/*
		if (storageContainer != null) {
		   storageContainer.close();
	      storageContainer = null;
	   }
		storageContainer = new TMStorageContainer();
		*/
	}
   public void log(String str) {
      UniLog.log(str);
   }
	public void checkDbConnection() throws SQLException {
      log("checkDbConnection(): started");
		if (dbconn == null) {
         log("checkDbConnection(): getConnection()");
         dbconn = poolDataSource.getConnection();
	   }
      log("checkDbConnection(): ended dbconn="+dbconn);
	}
	public Connection getConnection() throws SQLException {
      return(poolDataSource.getConnection());
	}
	public int beginwork() throws SQLException {
		int cc;
		cc = fbegin;
		fbegin = 1;
		return(cc);
	}
	public void commitwork() throws SQLException {
      log("commitwork(): started");
		if (dbconn != null) {
         log("commitwork(): dbconn.close()");
	      dbconn.close();
	      dbconn = null;
	   }
		fbegin = 0;
      log("commitwork(): ended");
	}
	public void rollbackwork() throws SQLException {
      log("rollbackwork(): started");
		if (dbconn != null) {
         log("rollbackwork(): dbconn.close()");
	      dbconn.close();
	      dbconn = null;
	   }
      log("rollbackwork(): ended");
		fbegin = 0;
	}
	public void doCreate(String p_tabname) throws Exception {
	   int tmpfbegin = 0;
		initVariable();
		if (p_tabname == null)
		   return;
		if (poolDataSource == null)
         poolDataSource = JdbcPool.getJdbcPool();
      try {
	      tmpfbegin = beginwork();
	      checkDbConnection();
			/*
         tb = new TableBrowser(dbconn, p_tabname);
			tb.setUniqueIdColumnName(getUniqueIdColumnName());
			*/
         tb = new TableBrowser(dbconn, p_tabname, getUniqueIdColumnName());
         tb.setDebug(1);
         log("TableBrowser created");
		   curtabname = p_tabname;
			if (tmpfbegin == 0)
				commitwork();
      } catch (Exception e) {
			// hahaha should free the dbconn
         log("exception at tablebrowser creation"+e.toString());
		   UniLog.log(e);
			if (tmpfbegin == 0) {
				try { rollbackwork(); } catch (Exception ex) {};
			}
		   throw(new Exception(e.toString()));
      }
	}
   public void remove() {
      log("TableMaintBean remove");
		/*
		if (storageContainer != null) {
		   storageContainer.close();
	      storageContainer = null;
	   }
		*/
		initVariable();
   }
	/*
   public void ejbCreate(String p_tabname) throws CreateException {
      log("TableMaintBean ejbCreate");
	   int tmpfbegin = 0;
		initVariable();
		if (p_tabname != null) {
         try {
            Context ctx=new InitialContext();
            poolDataSource = (javax.sql.DataSource) ctx.lookup("OracleDB");
         } catch(Exception e) {
		      log("Exception in ejbCreate"+e.toString()); 
		      UniLog.log(e);
		      throw new CreateException(e.toString());
         }
         try {
	         tmpfbegin = beginwork();
	         checkDbConnection();
            tb = new TableBrowser(dbconn, p_tabname);
				tb.setUniqueIdColumnName(getUniqueIdColumnName());
            tb.setDebug(1);
            log("TableBrowser created");
		      curtabname = p_tabname;
				if (tmpfbegin == 0)
				   commitwork();
         } catch (Exception e) {
			   // hahaha should free the dbconn
            log("exception at tablebrowser creation"+e.toString());
		      UniLog.log(e);
				if (tmpfbegin == 0) {
				   try { rollbackwork(); } catch (Exception ex) {};
				}
		      throw new CreateException(e.toString());
         }
		}

		try {
		   xslprocessor = XSLTProcessorFactory.getProcessor();
		} catch (Exception e) {
	      log( "XSLTProcessorFactory.getProcessor() failed: "+e.toString());
		   UniLog.log(e);
		   throw new CreateException(e.toString());
		}
   }
   public void setSessionContext(SessionContext sct) {
	   sessionContext = sct;
   }
   public void ejbActivate() {
      log("TableMaintBean ejbActivate");
	}
   public void ejbPassivate() {
      log("TableMaintBean ejbPassivate");
   } 
   public void ejbRemove() {
      log("TableMaintBean ejbRemove");
		if (storageContainer != null) {
		   storageContainer.close();
	      storageContainer = null;
	   }
		initVariable();
   }
	*/
   public void setwhereclause(Wherecl p_wherecl) {
	   curwherecl = p_wherecl;
	}
   public void setwhereclauseByRowid(Object p_rowid) {
		curwherecl = tb.getWhereClauseByRowid(p_rowid);
	}
   public Wherecl getwhereclause() {
	   return(curwherecl);
	}
   public void setorderby(String p_orderby) {
	   curorderby = p_orderby;
		curorderbycollist = null;
	}
   public void setorderby(String p_orderby, String p_collist) {
	   curorderby = p_orderby;
		curorderbycollist = p_collist;
	}
   public int query() {
UniLog.logClass(this, "trace:1000 query() curwherecl="+curwherecl);
if (curwherecl != null)
UniLog.logClass(this, "trace:1001 query() curwherecl="+curwherecl.toString());
	   int cc;
		int tmpfbegin = 0;
UniLog.logClass(this, "trace:1001.0 query() tb="+tb);
		if (tb == null) return(-1);
UniLog.logClass(this, "trace:1000.1 query()");
		fcurrent = false;
	   Hashtable h = new VectorUtil(v_qtblookup).toHashtable();;
		if (h == null)
		   h = new Hashtable();
	   for (int k=0; k<v_tblookup.size(); k++) {
		   TBLookup tmplookup = (TBLookup) v_tblookup.elementAt(k);
			tmplookup.setFBlindIncludeUsed(false);
			if (tmplookup.getFBlindInclude()) {
	         if (h.get(tmplookup) == null) {
				   if (v_qtblookup == null)
				      v_qtblookup = new Vector();
				   v_qtblookup.addElement(tmplookup);
			      tmplookup.setFBlindIncludeUsed(true);
			   }
			}
		}
		h.clear();
if (curwherecl != null)
UniLog.logClass(this, "trace:1002 query() curwherecl="+curwherecl.toString());
	   tb.setWhere(curwherecl);
		if (curorderbycollist == null)
		   tb.setOrderby(curorderby);
		else
		   tb.setOrderby(curorderby, curorderbycollist);
		curwherecl = null;
		try {
         tmpfbegin = beginwork();
if (dbconn != null) {
   log("force a dbconn.close(): dbconn.close()");
	dbconn.close();
	dbconn = null;
}
         checkDbConnection();
		   cc = tb.openCursor(dbconn, v_qtblookup);
			if (tmpfbegin == 0)
            commitwork();
		}
		catch (SQLException e) {
         log("OpenCursor Exception"+e.toString());
		   UniLog.log(e);
			if (tmpfbegin == 0) {
            try { rollbackwork(); } catch (Exception ex) {};
			}
			cc = 1;
		}
UniLog.logClass(this, "trace:1003 query() cc="+cc);
		if (cc == 0) {
			fcurrent = true;
		   tb.saveValues();
			return(0);
		}
		else {
			return(-1);
	   }
	}
   public int next() {
	   int cc;
		int tmpfbegin = 0;
		if (tb == null) return(-1);
		if (!fcurrent)
		   return(-1);
		try {
         tmpfbegin = beginwork();
         checkDbConnection();
		   cc = tb.nextCursor(dbconn);
			if (tmpfbegin == 0)
            commitwork();
		}
		catch (SQLException e) {
         log("nextCursor Exception"+e.toString());
		   UniLog.log(e);
			if (tmpfbegin == 0) { 
            try { rollbackwork(); } catch (Exception ex) {};
			}
			cc = -1;
		}
		if (cc >= 0)
		   tb.saveValues();
		return(cc >= 0 ? 0 : -1);
	}
   public int previous() {
	   int cc;
		int tmpfbegin = 0;
		if (tb == null) return(-1);
		if (!fcurrent) 
		   return(-1);
		try {
         tmpfbegin = beginwork();
         checkDbConnection();
		   cc = tb.prevCursor(dbconn);
			if (tmpfbegin == 0) 
            commitwork();
		}
		catch (SQLException e) {
         log("prevCursor Exception"+e.toString());
		   UniLog.log(e);
			if (tmpfbegin == 0) {
            try { rollbackwork(); } catch (Exception ex) {};
			}
			cc = -1;
		}
		if (cc >= 0)
		   tb.saveValues();
		return(cc >= 0 ? 0 : -1);
	}
   public int current() {
	   int cc;
		int tmpfbegin = 0;
		if (tb == null) return(-1);
	   if (!fcurrent) return(0);
		try {
         tmpfbegin = beginwork();
         checkDbConnection();
		   cc = tb.currentCursor(dbconn);
			if (tmpfbegin == 0)
            commitwork();
		}
		catch (SQLException e) {
         log("currentCursor Exception"+e.toString());
		   UniLog.log(e);
			if (tmpfbegin == 0) {
            try { rollbackwork(); } catch (Exception ex) {};
			}
			cc = -1;
		}
		if (cc >= 0)
		   tb.saveValues();
		return(cc >= 0 ? 0 : -1);
   }
	public String getCurrentXML() {
		if (tb == null)
         return(toXml(null, null, addColvalues, addColcomments, null, null)); 
      return(toXml(tb.getFields(), tb.getFieldsComments(), addColvalues, addColcomments, tb.getRowid(), tb.getFieldsMulti())); 
	}
   public String getEmptyXML() {
      return toXml(null, null, null, null, null, null); 
	}
   private void toXml_header(StringBuffer p_mysf) {
	   String desc;
		Enumeration tmpcolnames;
		int i = 0;
		TMMap options;
		TMFormat tmformat = null;
		Vector leftvalues = null;
		Vector rightvalues = null;
      String[] p_sn = null;
      int[] p_st = null;
      int[] p_cl = null;
      int[] p_cs = null;

		if (tb != null) {
         p_sn = tb.getColNames();
         p_st = tb.getColTypes();
         p_cl = tb.getColLengths();
         p_cs = tb.getColSublengths();
		}

      p_mysf.append("<Root>\n");
		if (addXmlString != null)
         p_mysf.append(addXmlString).append("\n");
      p_mysf.append("<Table Tablename=\""+(curtabname == null ? "virtual" : curtabname)+"\">\n");

      p_mysf.append("<Coldefset>\n");
		if (p_cl != null) {
         for (i=0;i<p_cl.length;i++) {
			   p_mysf.append("<Coldef");
	         p_mysf.append(" ID=\""+i+"\"");
	         p_mysf.append(" Name=\""+p_sn[i]+"\"");
			   desc = null;
	         if (colAttributes != null) 
			      desc = (String) colAttributes.get((Object) (p_sn[i]+"_"+"desc"));
			   if (desc == null) 
	            p_mysf.append(" Description=\""+"\"");
			   else {
	            p_mysf.append(" Description=\""+StringUtil.convertWebString(desc)+"\"");
	            //p_mysf.append(" Description=\""+desc+"\"");
			   }
			   p_mysf.append(" Type=\"");
	         switch (p_st[i]) {
               case TableBrowser.SQLTYPE_INTEGER: 
			         p_mysf.append("Integer"); break;
               case TableBrowser.SQLTYPE_CHAR:
			         p_mysf.append("String"); break;
               case TableBrowser.SQLTYPE_FLOAT: 
			         p_mysf.append("Float"); break;
               case TableBrowser.SQLTYPE_DATE: 
			         p_mysf.append("Date"); break;
			   }
			   p_mysf.append("\" Length=\"");
			   p_mysf.append(p_cl[i]);
				{
            Integer iWidth = (Integer) getColumnAttribute(p_sn[i], "fieldwidth");
				if (iWidth != null)
			      p_mysf.append("\" Width=\"").append(iWidth.intValue());
				else
			      p_mysf.append("\" Width=\"").append(p_cl[i]);
				}
			   p_mysf.append("\" Sublength=\"");
			   p_mysf.append(p_cs[i]).append("\"");
            if (getColumnAttribute(p_sn[i], "readonly") != null &&
                ((String) getColumnAttribute(p_sn[i], "readonly")).equals("Y"))
			      p_mysf.append(" readonly=\"Y\"");
			   p_mysf.append(">");
			   options = null;
	         if (colAttributes != null) {
			      options = (TMMap) colAttributes.get((Object) (p_sn[i]+"_"+"option"));
				   if (options != null) {
				      leftvalues = options.getLeftValues();
				      rightvalues = options.getRightValues();
				   }
			      tmformat = (TMFormat) colAttributes.get((Object) (p_sn[i]+"_"+"format"));
			   }
			   if (options != null) {
				   int cnt = leftvalues.size();
				   for (int j=0; j<cnt; j++) {
			         p_mysf.append("   <Option postvalue=\"");
					   p_mysf.append(StringUtil.convertWebString((String) leftvalues.elementAt(j)));
					   //p_mysf.append((String) leftvalues.elementAt(j));
					   p_mysf.append("\">");
			         p_mysf.append(StringUtil.convertWebString((String) rightvalues.elementAt(j)));
			         //p_mysf.append((String) rightvalues.elementAt(j));
			         p_mysf.append("</Option>\n");
				   }
			   }
				if (tmformat != null) {
				   tmformat.getXML(p_mysf);
				}
			   p_mysf.append("</Coldef>\n");
         }
		}
		tmpcolnames = addColtypes.keys();
      while (tmpcolnames.hasMoreElements()) {
		   String tmpcolname;
			tmpcolname = (String) tmpcolnames.nextElement();
			p_mysf.append("<Coldef");
	      p_mysf.append(" ID=\""+i+"\"");
	      p_mysf.append(" Name=\""+tmpcolname+"\"");
			desc = null;
	      if (colAttributes != null) 
			   desc = (String) colAttributes.get((Object) (tmpcolname+"_"+"desc"));
			if (desc == null) 
	         p_mysf.append(" Description=\""+"\"");
			else {
	         p_mysf.append(" Description=\""+StringUtil.convertWebString(desc)+"\"");
	         //p_mysf.append(" Description=\""+desc+"\"");
			}
			p_mysf.append(" Type=\"");
			p_mysf.append(TableBrowser.typeIntToString(((Integer) addColtypes.get(tmpcolname)).intValue()));
			p_mysf.append("\" Length=\"");
			p_mysf.append(addCollens.get(tmpcolname));
			{
         Integer iWidth = (Integer) getColumnAttribute(tmpcolname, "fieldwidth");
			if (iWidth != null)
			   p_mysf.append("\" Width=\"").append(iWidth.intValue());
			else
			   p_mysf.append("\" Width=\"").append(addCollens.get(tmpcolname));
			}
			p_mysf.append("\" Sublength=\"");
			p_mysf.append(addColsublens.get(tmpcolname));
			p_mysf.append("\">\n");
			options = null;
	      if (colAttributes != null) {
			   options = (TMMap) colAttributes.get((Object) (tmpcolname+"_"+"option"));
				if (options != null) {
				   leftvalues = options.getLeftValues();
				   rightvalues = options.getRightValues();
				}
			   tmformat = (TMFormat) colAttributes.get((Object) (tmpcolname+"_"+"format"));
			}
			if (options != null) {
				int cnt = leftvalues.size();
				for (int j=0; j<cnt; j++) {
			      p_mysf.append("   <Option postvalue=\"");
					p_mysf.append(StringUtil.convertWebString((String) leftvalues.elementAt(j)));
					//p_mysf.append((String) leftvalues.elementAt(j));
					p_mysf.append("\">");
			      p_mysf.append(StringUtil.convertWebString((String) rightvalues.elementAt(j)));
			      //p_mysf.append((String) rightvalues.elementAt(j));
			      p_mysf.append("</Option>\n");
				}
			}
			if (tmformat != null) {
				tmformat.getXML(p_mysf);
			}
			p_mysf.append("</Coldef>\n");
		   i++;
		}
      p_mysf.append("</Coldefset>\n");
      p_mysf.append("<Rowset>\n");
	}
   private void toXml_footer(StringBuffer p_mysf) {
      p_mysf.append("</Rowset>\n");
      p_mysf.append("</Table>\n");
      p_mysf.append("</Root>\n");
	}
   private void toXml_onerow(
								StringBuffer p_mysf,
								int irowid,
								String[] p_curValues,
								String[] p_curValuesComments,
                        Hashtable p_addColvalues,
                        Hashtable p_addColcomments,
								Object p_rowid,
								Vector p_curValuesMulti) {
		int i = 0;
		Enumeration tmpcolnames;
      String[] p_sn = null;
      int[] p_st = null;
      int[] p_cl = null;
      int[] p_cs = null;

		if (tb != null) {
         p_sn = tb.getColNames();
         p_st = tb.getColTypes();
         p_cl = tb.getColLengths();
         p_cs = tb.getColSublengths();
		}

      p_mysf.append("<Row");
		p_mysf.append(" ID=\""+irowid+"\"");
		p_mysf.append(" Rowid=\""+StringUtil.urlencode(p_rowid == null ? "" : p_rowid.toString())+"\"");
      p_mysf.append(">\n");
		if (p_cl != null) {
         for (i=0;i<p_cl.length;i++) {
	         p_mysf.append("<Col");
			   p_mysf.append(" ID=\"").append(i).append("\"");
	         p_mysf.append(" Name=\""+p_sn[i]+"\"");
			   if (p_curValuesComments != null && p_curValuesComments[i] != null) {
	            p_mysf.append(" Comments=\""+StringUtil.convertWebString(p_curValuesComments[i])+"\"");
			   }
	         p_mysf.append(">");
			   if (p_curValues != null && p_curValues[i] != null) {
		         String tmpstr = null;
		         TMMap map = (TMMap) getColumnAttribute(p_sn[i], "map");
		         TMFormat tmformat = (TMFormat) getColumnAttribute(p_sn[i], "format");
				   if (map != null) {
				      tmpstr = (String) map.leftToRight((Object) p_curValues[i]);
					}
				   if (tmpstr != null) {
			         p_mysf.append(tmpstr);
					}
				   else {
	               switch (p_st[i]) {
                     case TableBrowser.SQLTYPE_INTEGER: 
								if (tmformat != null) {
			                  p_mysf.append(tmformat.formatDisplay(p_curValues[i]));
								}
								else {
			                  p_mysf.append(p_curValues[i]);
								}
						      break;
                     case TableBrowser.SQLTYPE_CHAR:
								if (tmformat != null) {
				               if (language.equals("utf-8b"))
			                     p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(tmformat.formatDisplay(StringUtil.sr(p_curValues[i])))));
				               else if (language.equals("utf-8g"))
			                     p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(tmformat.formatDisplay(StringUtil.sr(p_curValues[i])))));
				               else if (language.equals("gb"))
			                     p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(tmformat.formatDisplay(StringUtil.sr(p_curValues[i])))));
								   else
			                     p_mysf.append(StringUtil.convertWebString(tmformat.formatDisplay(StringUtil.sr(p_curValues[i]))));
								}
								else {
				               if (language.equals("utf-8b"))
			                     p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(StringUtil.sr(p_curValues[i])))); 
				               else if (language.equals("utf-8g"))
			                     p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(StringUtil.sr(p_curValues[i])))); 
				               else if (language.equals("gb"))
			                     p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(StringUtil.sr(p_curValues[i])))); 
									else
			                     p_mysf.append(StringUtil.convertWebString(StringUtil.sr(p_curValues[i]))); 
								}
						      break;
                     case TableBrowser.SQLTYPE_FLOAT: 
								if (tmformat != null)
			                  p_mysf.append(tmformat.formatDisplay(p_curValues[i]));
								else
			                  p_mysf.append(p_curValues[i]);
						      break;
                     case TableBrowser.SQLTYPE_DATE: 
								if (tmformat != null)
			                  p_mysf.append(tmformat.formatDisplay(p_curValues[i]));
								else
	                        p_mysf.append(defDateFormat.formatDisplay(p_curValues[i]));
						      break;
			         }
				   }
			   }
	         p_mysf.append("</Col>\n");
         }
		}
		tmpcolnames = addColtypes.keys();
      while (tmpcolnames.hasMoreElements()) {
		   String tmpcolname;
			tmpcolname = (String) tmpcolnames.nextElement();
		   TMFormat tmformat = (TMFormat) getColumnAttribute(tmpcolname, "format");
	      p_mysf.append("<Col");
			p_mysf.append(" ID=\"").append(i).append("\"");
	      p_mysf.append(" Name=\""+tmpcolname+"\"");
			if (p_addColcomments != null && p_addColcomments.get(tmpcolname) != null) {
	         p_mysf.append(" Comments=\""+StringUtil.convertWebString((String) p_addColcomments.get(tmpcolname))+"\"");
	         //p_mysf.append(" Comments=\""+p_addColcomments.get(tmpcolname)+"\"");
			}
	      p_mysf.append(">");
			if (p_addColvalues != null && 
			    p_addColvalues.get(tmpcolname) != null &&
			    p_addColvalues.get(tmpcolname) instanceof String) {
				switch (((Integer) addColtypes.get(tmpcolname)).intValue()) {
				   case TableBrowser.SQLTYPE_DATE:
		            if (tmformat != null)
			            p_mysf.append(tmformat.formatDisplay((String) p_addColvalues.get(tmpcolname)));
						else
			            p_mysf.append(defDateFormat.formatDisplay((String) p_addColvalues.get(tmpcolname)));
						break;
				   case TableBrowser.SQLTYPE_CHAR:
		            if (tmformat != null) {
				         if (language.equals("utf-8b"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname))))));
				         else if (language.equals("utf-8g"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname))))));
				         else if (language.equals("gb"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname))))));
					      else
			               p_mysf.append(StringUtil.convertWebString(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
						}
						else {
				         if (language.equals("utf-8b"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
				         else if (language.equals("utf-8g"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
				         else if (language.equals("gb"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
							else
			               p_mysf.append(StringUtil.convertWebString(StringUtil.sr((String) p_addColvalues.get(tmpcolname))));
						}
						break;
				   default:
		            if (tmformat != null) {
				         if (language.equals("utf-8b"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname))))));
				         else if (language.equals("utf-8g"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname))))));
				         else if (language.equals("gb"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname))))));
							else
			               p_mysf.append(StringUtil.convertWebString(tmformat.formatDisplay(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
						}
						else {
				         if (language.equals("utf-8b"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
				         else if (language.equals("utf-8g"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
				         else if (language.equals("gb"))
			               p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(StringUtil.sr((String) p_addColvalues.get(tmpcolname)))));
							else
			               p_mysf.append(StringUtil.convertWebString(StringUtil.sr((String) p_addColvalues.get(tmpcolname))));
						}
					   break;
				}
			}
	      p_mysf.append("</Col>\n");
			i++;
		}
		if (p_curValuesMulti != null) {
	      p_mysf.append("<RowMultiSet>\n");
		   for (int subidx=0; subidx<p_curValuesMulti.size(); subidx++) {
	         p_mysf.append("<RowMulti rowidx=\"").append(subidx).append("\">\n");
			   String[] values = (String[]) p_curValuesMulti.elementAt(subidx);
            for (i=0;i<p_cl.length;i++) {
	            p_mysf.append("<ColMulti");
			      p_mysf.append(" ID=\"").append(i).append("\"");
	            p_mysf.append(" Name=\""+p_sn[i]+"\"");
	            p_mysf.append(">");
			      if (values != null && values[i] != null) {
		            String tmpstr = null;
		            TMMap map = (TMMap) getColumnAttribute(p_sn[i], "map");
		            TMFormat tmformat = (TMFormat) getColumnAttribute(p_sn[i], "format");
				      if (map != null)
				         tmpstr = (String) map.leftToRight((Object) values[i]);
				      if (tmpstr != null)
			            p_mysf.append(tmpstr);
				      else {
	                  switch (p_st[i]) {
                        case TableBrowser.SQLTYPE_INTEGER: 
								   if (tmformat != null)
			                     p_mysf.append(tmformat.formatDisplay(values[i]));
									else
			                     p_mysf.append(values[i]);
						         break;
                        case TableBrowser.SQLTYPE_CHAR:
								   if (tmformat != null) {
				                  if (language.equals("utf-8b"))
			                        p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(tmformat.formatDisplay(StringUtil.sr(values[i])))));
				                  else if (language.equals("utf-8g"))
			                        p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(tmformat.formatDisplay(StringUtil.sr(values[i])))));
				                  else if (language.equals("gb"))
			                        p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(tmformat.formatDisplay(StringUtil.sr(values[i])))));
										else
			                        p_mysf.append(StringUtil.convertWebString(tmformat.formatDisplay(StringUtil.sr(values[i]))));
									}
									else {
				                  if (language.equals("utf-8b"))
			                        p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(StringUtil.sr(values[i])))); 
				                  else if (language.equals("utf-8g"))
			                        p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(StringUtil.sr(values[i])))); 
				                  else if (language.equals("gb"))
			                        p_mysf.append(StringUtil.convertWebString(ChineseConvert.convertB2G(StringUtil.sr(values[i])))); 
										else
			                        p_mysf.append(StringUtil.convertWebString(StringUtil.sr(values[i]))); 
									}
						         break;
                        case TableBrowser.SQLTYPE_FLOAT: 
								   if (tmformat != null)
			                     p_mysf.append(tmformat.formatDisplay(values[i]));
									else
			                     p_mysf.append(values[i]);
						         break;
                        case TableBrowser.SQLTYPE_DATE: 
								   if (tmformat != null)
			                     p_mysf.append(tmformat.formatDisplay(values[i]));
									else
	                           p_mysf.append(defDateFormat.formatDisplay(values[i]));
						         break;
			            }
				      }
			      }
	            p_mysf.append("</ColMulti>\n");
            }
	         p_mysf.append("</RowMulti>\n");
			}
	      p_mysf.append("</RowMultiSet>\n");
		}
      p_mysf.append("</Row>\n");
   }
   private String toXml(String[] p_curValues,
                        String[] p_curValuesComments,
                        Hashtable p_addColvalues,
                        Hashtable p_addColcomments,
								Object p_rowid,
								Vector p_curValuesMulti) {
	   StringBuffer strbuf;

		strbuf = new StringBuffer();
      toXml_header(strbuf);
      toXml_onerow(strbuf, 0,
						 p_curValues,
						 p_curValuesComments,
						 p_addColvalues,
						 p_addColcomments,
						 p_rowid,
						 p_curValuesMulti);
      toXml_footer(strbuf);
      if (fdebug > 0)
         UniLog.log("toXml:"+strbuf.toString());
		return(strbuf.toString());
	}
	public String curRecListToXml() {
	   StringBuffer strbuf;
		if (tb == null) return(null);
		if (curRecList == null) return(null);
		strbuf = new StringBuffer();
      toXml_header(strbuf);
		if (fcurrent) {
	      for (int i=0; i<curRecList.size(); i++) {
            toXml_onerow(strbuf, i+curRecListOffset,
						       (String[]) curRecList.elementAt(i),
							    null,
                         null,
							    null,
							    curRecRowidList.elementAt(i),
							    curRecMultiList == null ? null : (Vector) curRecMultiList.elementAt(i)
							    );
		   }
		}
      toXml_footer(strbuf);
      if (fdebug > 0)
         UniLog.log("curRecListToXml:"+strbuf.toString());
		return(strbuf.toString());
	}
	public void setColumnAttribute(String p_colname, String p_attrname, Object p_attrvalue) {
		if (p_attrvalue == null) 
	      colAttributes.remove(p_colname.toLowerCase()+"_"+p_attrname.toLowerCase());
		else
	      colAttributes.put(p_colname.toLowerCase()+"_"+p_attrname.toLowerCase(), p_attrvalue);
	}
	public void setColumnAttributes(Hashtable p_columnAttributes) {
	   colAttributes = p_columnAttributes;
	}
	public Object getColumnAttribute(String p_colname, String p_attrname) {
	   if (colAttributes == null)
		   return(null);
      else
	      return(colAttributes.get(p_colname.toLowerCase()+"_"+p_attrname.toLowerCase()));
	}
   public TableRec getTableRec() {
	   if (tb == null)
		   return(null);
	   return(tb.getTableRec());
	}
	public void setStylesheetByUrl(String p_stylesheettype, String p_url) {
	   try {
			xslhash.put(p_stylesheettype.toLowerCase()+"_url", p_url);
		} catch (Exception ex) {
		   UniLog.log(ex);
		}
	}
	public void setStylesheet(String p_stylesheettype, String p_stylesheetstring, String p_orgFilename) {
	   try {
		   XslUtil tmpxsl = XslUtil.getByString(p_stylesheetstring, p_orgFilename);
			tmpxsl.prepare();
			xslhash.put(p_stylesheettype.toLowerCase(), tmpxsl);
		} catch (Exception ex) {
		   UniLog.log(ex);
		}
	}
	private XslUtil getStyleSheet(String p_stylesheettype) {
UniLog.logClass(this, "p_stylesheettype="+p_stylesheettype);
	   XslUtil xslUtil = (XslUtil) xslhash.get(p_stylesheettype.toLowerCase());
		if (xslUtil != null)
	      return(xslUtil);
	   xslUtil = (XslUtil) xslhash.get(p_stylesheettype.toLowerCase()+"_"+language);
		if (xslUtil != null)
	      return(xslUtil);
	   String url = (String) xslhash.get(p_stylesheettype.toLowerCase()+"_url");
	   if (url == null)
		   return(null);
      if (url.indexOf("?") >= 0)
         url = url+"&language="+language;
		else
         url = url+"?language="+language;
      XslUtil tmpxsl = (XslUtil) xslhash.get(p_stylesheettype.toLowerCase()+"_url_xslutil_"+url);
		if (tmpxsl != null)
		   return(tmpxsl);
		try {
         tmpxsl = XslUtil.getByUrl(url);
		   tmpxsl.prepare();
			xslhash.put(p_stylesheettype.toLowerCase()+"_url_xslutil_"+url, tmpxsl);
			return(tmpxsl);
		} catch (Exception ex) {
		   UniLog.log(ex);
		   return(null);
		}
	}
	public String xslprocess(String p_stylesheettype) {
		StringWriter sw = new StringWriter();

		if (p_stylesheettype.equals("everyrec")) {
			try {
				getStyleSheet("everyrec").transform(new StringReader(getCurrentXML()), sw);
			} catch (Exception ex) {
			   UniLog.log(ex);
				return("");
			}
		   return(sw.toString());
		} else if (p_stylesheettype.equals("list")) {
			try {
            String xmlString = curRecListToXml();
				StringReader sr = new StringReader(xmlString);
	         XslUtil xslUtil = getStyleSheet("list");
UniLog.logClass(this, "xmlString="+xmlString);
UniLog.logClass(this, "sr="+sr);
UniLog.logClass(this, "xslUtil="+xslUtil);
				xslUtil.transform(sr, sw);
			} catch (Exception ex) {
			   UniLog.log(ex);
				return("");
			}
		   String retstr = sw.toString();
		   return(retstr);
		}
		else
		   return("");
	}
   public void clearDataFields() {
		try {
			if (tb != null) {
		      tb.closeCursor();
		      tb.clearFields();
			}
		   fcurrent = false;
		} catch (Exception e) {
	      log("Exception on clearDataFields: "+e.toString());
		   UniLog.log(e);
		}
	}
   public void clearUserFields() {
		Enumeration tmpcolnames;
		tmpcolnames = addColtypes.keys();
      while (tmpcolnames.hasMoreElements()) {
		   addColvalues.put((String) tmpcolnames.nextElement(), "");
	   }
      addColcomments = new Hashtable();
	}
	public void clearFieldComments() {
      addColcomments = new Hashtable();
		if (tb != null)
		   tb.clearFieldComments();
	}
   public void defaultDataFields() {
		if (tb != null)
		   tb.defaultFields();
	}
	public String[] getUserColNames() {
	   setupMyColumns();
	   return(userColNames);
	}
	private void setupMyColumns() {
		int cnt0=0; 
		int cnt1=0;

	   if (myColNames != null) 
		   return;
	   if (tb != null)
		  cnt0 = tb.getColNames().length;
		cnt1 = addColtypes.size();
		myColIndexes = new Hashtable();
	   myColNames = new String[cnt0+cnt1];
	   myColTypes = new int[cnt0+cnt1];
	   myColLengths = new int[cnt0+cnt1];
	   myColSublengths = new int[cnt0+cnt1];
	   userColNames = new String[cnt1];
		if (tb != null) {
         String p_sn[] = tb.getColNames();
         int p_st[] = tb.getColTypes();
         int p_cl[] = tb.getColLengths();
         int p_cs[] = tb.getColSublengths();
			for (int i=0; i<cnt0; i++) {
            myColNames[i] = p_sn[i];
            myColTypes[i] = p_st[i];
            myColLengths[i] = p_cl[i];
            myColSublengths[i] = p_cs[i];
				myColIndexes.put(myColNames[i], new Integer(i));
			}
		}
		if (!addColtypes.isEmpty()) {
			int i = cnt0;
		   for (Enumeration e=addColtypes.keys(); e.hasMoreElements(); i++) {
            myColNames[i] = (String) e.nextElement();
	         userColNames[i-cnt0] = myColNames[i];
            myColTypes[i] = ((Integer) addColtypes.get(myColNames[i])).intValue();
            myColLengths[i] = ((Integer) addCollens.get(myColNames[i])).intValue();
            myColSublengths[i] = ((Integer) addColsublens.get(myColNames[i])).intValue();
				myColIndexes.put(myColNames[i], new Integer(i));
		   }
		}
	}
   public String[] getColNames() {
	   setupMyColumns();
		return(myColNames);
   }
   public int[] getColTypes() {
	   setupMyColumns();
      return(myColTypes);
   }
   public int[] getColLengths() {
	   setupMyColumns();
      return(myColLengths);
   }
   public int[] getColSublengths() {
	   setupMyColumns();
      return(myColSublengths);
   }
   public void setUserColumn(String p_colname, String p_type, int p_length, int p_sublength) {
      addColtypes.put(p_colname, new Integer(TableBrowser.typeStringToInt(p_type)));
      addCollens.put(p_colname, new Integer(p_length));
      addColsublens.put(p_colname, new Integer(p_sublength));
		addColvalues.put(p_colname, "");
	}
	public int setFieldComments(String p_colname, String p_comments) {
      if (addColvalues.get(p_colname) != null) {
      //if (addColtypes.get(p_colname) != null) {
			if (p_comments == null)
			   addColcomments.remove(p_colname);
			else {
			   addColcomments.put(p_colname, p_comments);
			}
		   return(0);
		}
		if (tb == null) return(-1);
		return(tb.setFieldComments(p_colname, p_comments));
	}
	public void setQueryFieldStart() {
	   queryColnames = new Vector();
	   queryColvalues= new Vector();
	}
	public void setQueryField(String p_colname, String p_strvalue) {
	   if (p_strvalue == null) 
		   return;
	   if (p_strvalue.trim().equals(""))
		   return;
		queryColnames.addElement(p_colname);
		Boolean upshift = (Boolean) getColumnAttribute(p_colname, "upshift");
		if (upshift != null && upshift.booleanValue()) {
		   queryColvalues.addElement(p_strvalue.toUpperCase());
		   return;
		}
		Boolean downshift = (Boolean) getColumnAttribute(p_colname, "downshift");
		if (downshift != null && downshift.booleanValue()) {
		   queryColvalues.addElement(p_strvalue.toLowerCase());
		   return;
		}
		queryColvalues.addElement(p_strvalue);
	}
	public int setFields(String p_colname, String[] p_strvalues) {
	   int cc;
		TMMap options;
		String tmpstr;
      if (fdebug > 1)
	      UniLog.log("setFields("+p_colname+","+arrayToString(p_strvalues)+")");
		if (p_strvalues == null) 
		   return(0);
		options = (TMMap) getColumnAttribute(p_colname, "option");
		if (options == null) {
	      UniLog.log("setFields("+p_colname+","+arrayToString(p_strvalues)+"):is not an option");
		   return(-1);
		}
		if (!options.isMultiple()) {
	      UniLog.log("setFields("+p_colname+","+arrayToString(p_strvalues)+"):is not a multiple option");
		   return(-11);  
		}
      if (addColvalues.get(p_colname) == null) {
	      UniLog.log("setFields("+p_colname+","+arrayToString(p_strvalues)+"):not found in user defined column");
		   return(-1);
		}
      addColvalues.put(p_colname, p_strvalues);
	   return(0);
	}
	public Vector triggerLookup(String p_tagname) {
		VectorUtil vu = null;
		SelectUtil su = null;
		try {
		   for (int i=0; i<v_tblookup.size(); i++) {
		      TBLookup lk = (TBLookup) v_tblookup.elementAt(i);
	         if (lk.isJoinTag(p_tagname) || lk.isMappedTagname(p_tagname)) {
			      if (su == null) {
					   su = new SelectUtil();
					   su.init(getConnection());
				   }
				}
	         if (lk.isJoinTag(p_tagname)) {
	            if (lk.forwardLookup(this, su) > 0) {
					   if (vu == null)
						   vu = new VectorUtil();
						vu.addElements(lk.getMapQualifiedColNames());
					}
			   } 
				else if (lk.isMappedTagname(p_tagname)) {
	            if (lk.backwardLookup(this, su, p_tagname) > 0) {
					   if (vu == null)
						   vu = new VectorUtil();
						vu.addElements(lk.getMapQualifiedColNames());
						vu.addElements(lk.getCondFieldNames());
					}
				}
		   }
			if (vu == null)
			   return(null);
			else
			   return(vu.toVector());
	   } catch (Exception ex) {
		   UniLog.log(ex);
			return(null);
		} finally {
		   if (su != null) {
			   try { su.close(); } catch (Exception ex2) {};
			}
		}
	}
	public Vector setFieldWithLookup(String p_colname, String p_strvalue) {
	   int cc = setField(p_colname, p_strvalue);
		if (cc != 0)
		   return(null);
	   return(triggerLookup(p_colname));
	}
	public Vector setFieldWithLookup(String p_colname, int p_intvalue) {
	   int cc = setField(p_colname, p_intvalue);
		if (cc != 0)
		   return(null);
	   return(triggerLookup(p_colname));
	}
	public int setField(String p_colname, String p_strvalue) {
	   int cc;
		TMMap map;
		TMMap options;
      if (fdebug > 1)
	      UniLog.log("setField("+p_colname+","+p_strvalue+")");
		String tmpstr = null;
		options = (TMMap) getColumnAttribute(p_colname, "option");
		if (options != null && options.isMultiple()) {
		   return(1);  // signal formtofields() that setFields() should be used instead
		}
		map = (TMMap) getColumnAttribute(p_colname, "map");
		if (map != null && p_strvalue != null) {
		   tmpstr = (String) map.rightToLeft((Object) p_strvalue);
		}	
		if (tmpstr == null) {
         TMFormat tmformat = (TMFormat) getColumnAttribute(p_colname, "format");
		   if (tmformat != null) {
			   switch (tmformat.getType()) {
	            case TMFormat.TMF_NUMBER:
						if (p_strvalue == null)
					      tmpstr = null;
					   else
						   tmpstr = StringUtil.toNumberOnly(p_strvalue);
					   break;
				   case TMFormat.TMF_CHECKBOX:
                  if (!((TMFormatCheckBox) tmformat).isMirrored()) {
					      if (p_strvalue == null)
					         tmpstr = "N";
					      else if (p_strvalue.equalsIgnoreCase("on"))
					         tmpstr = "Y";
					      else if (p_strvalue.equalsIgnoreCase("y"))
					         tmpstr = "Y";
					      else if (p_strvalue.equalsIgnoreCase("n"))
					         tmpstr = "N";
					      else
					         tmpstr = "N";
						}
					   break;
			   }
		   }
		}
		if (tmpstr == null)
		   tmpstr = p_strvalue;
		/* 
		if p_strvalue is null -> not defined in form -> ignore the set field from
		formtofields() except checkbox(exclude fmirrored)
		*/
		if (tmpstr == null)
		   return(0);
      if (getColType(p_colname) == TableBrowser.SQLTYPE_DATE) {
		   TMFormat tmformat = (TMFormat) getColumnAttribute(p_colname, "format");
			if (tmformat == null)
	         tmformat = defDateFormat;
		   java.util.Date d = DateUtil.getDate(tmpstr, ((TMFormatDate) tmformat).getPicture());
			if (d == null)
			   tmpstr = "";
         else
			   tmpstr = DateUtil.toDateString(d, "yyyy/mm/dd");
		}
      if (addColvalues.get(p_colname) != null) {
			switch (((Integer) addColtypes.get(p_colname)).intValue()) {
            case TableBrowser.SQLTYPE_DATE: 
					if (tmpstr == null || tmpstr.trim().equals("") || tmpstr.trim().equals(TableBrowser.SQLTYPE_DEFAULT_DATE)) 
		            addColvalues.put(p_colname, "");
					else {
					   java.util.Date d = DateUtil.getDate(tmpstr);
					   if (d == null)
		               addColvalues.put(p_colname, "");
					   else
		               addColvalues.put(p_colname, DateUtil.toDateString(d, "yyyy/mm/dd"));
					}
				   break;
			   default:
		         addColvalues.put(p_colname, tmpstr);
			      break;
			}
			return(0);
		}
		else if (tb != null) {
		   cc = tb.setField(p_colname, tmpstr);
			if (cc == 0)
			   return(0);
		}
	   return(-1);
	}
	public int setField(String p_colname, int p_intvalue) {
	   return(setField(p_colname, (new Integer(p_intvalue)).toString()));
	}
	public String[] getFields(String p_colname) {
		TMMap options;
		Object obj;
		String[] values;
      obj = addColvalues.get(p_colname);
		if (!(obj instanceof String[])) {
		   UniLog.log("getFields("+p_colname+"): not String[]");
		   return(null);
		}
      values = (String[]) addColvalues.get(p_colname);
		if (values == null) {
		   UniLog.log("getFields("+p_colname+"): not found in user columns");
		   return(null);
      }
		return(values);
	}
	public Object getTypedField(String p_colname) {
	   switch (getColType(p_colname)) {
			case TableBrowser.SQLTYPE_INTEGER:
				if (getField(p_colname).trim().equals(""))
				   return(new Integer(0));
				return(new Integer(getField(p_colname).trim()));
			case TableBrowser.SQLTYPE_FLOAT:
				if (getField(p_colname).trim().equals(""))
				   return(new Double(0.0));
				return(new Double(getField(p_colname).trim()));
			case TableBrowser.SQLTYPE_DATE:
				return(DateUtil.getDate(getField(p_colname)));
			case TableBrowser.SQLTYPE_CHAR:
				return(getField(p_colname));
			case TableBrowser.SQLTYPE_TIMESTAMP:
				return(getField(p_colname));
			case TableBrowser.SQLTYPE_BLOB:
				return(getField(p_colname));
			default: 
			   UniLog.log(new Exception("getTypedField: unknown coltype "+getColType(p_colname)+" for column "+p_colname));
		      return(getField(p_colname));
		}
	}
	public String getField(String p_colname) {
	   String strvalue, tmpstr;
		TMMap map;
		Object obj;
      obj = addColvalues.get(p_colname);
		if (obj instanceof String[]) {
		   UniLog.log("getField("+p_colname+"): is String[] instead of String");
		   return(null);
		}
      strvalue = (String) addColvalues.get(p_colname);
		if (strvalue == null && tb != null)
	      strvalue = tb.getField(p_colname);
      if (strvalue == null)
		   return(null);

		tmpstr = null;
		map = (TMMap) getColumnAttribute(p_colname, "map");
		if (map != null)
		   tmpstr = (String) map.leftToRight((Object) strvalue);
		if (tmpstr == null)
		   return(strvalue);
		else
		   return(tmpstr);
	}
	public String getFieldComments(String p_colname) {
	   String comments = null;
      if (addColcomments != null) {
		   comments = (String) addColcomments.get(p_colname);
		}
		if (comments == null && tb != null) {
		   comments = tb.getFieldComments(p_colname);
		}
      if (comments == null)
		   return("");
      if (comments.trim().equals(""))
		   return("");
      /*
		if (webLogin != null)
		   return("<font color=\"red\">"+webLogin.getMsgWebString(comments)+"</font>");
		else
		*/
		   return("<font color=\"red\">"+StringUtil.convertWebString(comments)+"</font>");

	}
	public String getInputField(String p_colname, boolean p_fForQuery, String p_appendString) {
	   return(getInputField(p_colname, p_fForQuery, p_appendString, null));
	}
	public boolean isReadonly(String p_colname) {
      if (getColumnAttribute(p_colname, "readonly") != null &&
         ((String) getColumnAttribute(p_colname, "readonly")).equals("Y"))
			return(true);
	   return(false);
	}
	public String getInputField(String p_colname, boolean p_fForQuery, String p_appendString, String p_language) {
	   return(getInputFieldAlias(null, null, p_colname, p_fForQuery, p_appendString, p_language));
	}
	public String getInputFieldAlias(String p_fieldId, String p_alias, String p_colname, boolean p_fForQuery, String p_appendString, String p_language) {
	   setupMyColumns();
		Integer colInteger = (Integer) myColIndexes.get(p_colname);
		if (colInteger == null) {
		   return("");
		}
		StringBuffer sb = new StringBuffer();
		TMMap options = (TMMap) colAttributes.get((Object) (p_colname+"_"+"option"));
		TMFormat tmformat = (TMFormat) colAttributes.get((Object) (p_colname+"_"+"format"));
		if (tmformat == null 
		    && getColType(p_colname) == TableBrowser.SQLTYPE_DATE)
         tmformat = defDateFormat;
		boolean freadonly = false;
      if (getColumnAttribute(p_colname, "readonly") != null &&
         ((String) getColumnAttribute(p_colname, "readonly")).equals("Y"))
		   freadonly = true;
		boolean fhidden = false;
      if (getColumnAttribute(p_colname, "hidden") != null &&
         ((String) getColumnAttribute(p_colname, "hidden")).equals("Y"))
		   fhidden = true;
		if (fhidden) {
		   sb.append("<input type=\"hidden\" ");
		   sb.append("id=\"").append(p_fieldId).append("\" name=\"").append(p_alias == null ? p_colname : p_alias).append("\" ");
		   sb.append("value=\"");
			if (tmformat == null) {
            if (p_language != null && p_language.trim().equals("utf-8b"))
				   sb.append(StringUtil.convertWebString(StringUtil.sr(ChineseConvert.convertAuto2B(getField(p_colname)))));
            else if (p_language != null && p_language.trim().equals("utf-8g"))
				   sb.append(StringUtil.convertWebString(StringUtil.sr(ChineseConvert.convertAuto2G(getField(p_colname)))));
            else if (p_language != null && p_language.trim().equals("gb"))
				   sb.append(StringUtil.convertWebString(StringUtil.sr(ChineseConvert.convertB2G(getField(p_colname)))));
				else
				   sb.append(StringUtil.convertWebString(StringUtil.sr(getField(p_colname))));
			}
			else {
				sb.append(StringUtil.convertWebString(tmformat.formatDisplay(StringUtil.sr(getField(p_colname)))));
			}
			sb.append("\" ");
			if (p_appendString != null)
			   sb.append(p_appendString).append(" ");
		   sb.append(">");
		}
		else if (options != null) {
			Vector leftvalues = options.getLeftValues();
			Vector rightvalues = options.getRightValues();
		   sb.append("<select id=\"").append(p_fieldId).append("\" name=\"").append(p_alias == null ? p_colname : p_alias).append("\" ");
			if (options.isMultiple()) {
			   sb.append("multiple size=\"").append(options.getRowsize()).append("\" ");
			}
			if (freadonly) 
			   sb.append(" onfocus=\"this.blur()\" ");
			if (p_appendString != null)
			   sb.append(p_appendString).append(" ");
			sb.append(">");
			if (!options.isMultiple())
			   sb.append("<option value=\"\"></option>");
			for (int i=0; i<leftvalues.size(); i++) {
			   sb.append("<option value=\"").append(StringUtil.convertWebString(leftvalues.elementAt(i).toString())).append("\" ");
				if (options.isMultiple()) {
			      if (findStringInArray(getFields(p_colname), leftvalues.elementAt(i).toString()) >= 0)
				      sb.append("selected ");
				}
				else {
			      if (((leftvalues.elementAt(i).toString()).trim()).equals(getField(p_colname).trim()))
				      sb.append("selected ");
				}
				if (options instanceof TMMapWithTip) {
					sb.append(" title=\""+getStringByLanguage(((TMMapWithTip) options).tipValues.elementAt(i).toString(), p_language)+"\"");	
				}
				sb.append(">");
            if (p_language != null && p_language.trim().equals("utf-8g"))
				   sb.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G((String) rightvalues.elementAt(i))));
            else if (p_language != null && p_language.trim().equals("utf-8b"))
				   sb.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B((String) rightvalues.elementAt(i))));
            else if (p_language != null && p_language.trim().equals("gb"))
				   sb.append(StringUtil.convertWebString(ChineseConvert.convertB2G((String) rightvalues.elementAt(i))));
				else
				   sb.append(StringUtil.convertWebString((String) rightvalues.elementAt(i)));
				sb.append("</option>");
			}
			sb.append("</select>");
		}
		else {
			int i = colInteger.intValue();
			if (tmformat != null && tmformat.getType() == TMFormat.TMF_TEXTAREA) {
			   TMFormatTextArea tmf = (TMFormatTextArea) tmformat;
		      sb.append("<textarea ");
		      sb.append("id=\"").append(p_fieldId).append("\" name=\"").append(p_alias == null ? p_colname : p_alias).append("\" ");
		      sb.append("cols=\"").append(tmf.getCol()).append("\" ");
		      sb.append("rows=\"").append(tmf.getRow()).append("\" ");
				if (tmf.getWrap() != null)
		         sb.append("wrap=\"").append(tmf.getWrap()).append("\" ");
			   if (freadonly) 
			      sb.append(" onfocus=\"this.blur()\" ");
			   if (p_appendString != null)
			      sb.append(p_appendString).append(" ");
		      sb.append(">");
            if (p_language != null && p_language.trim().equals("utf-8b"))
		         sb.append(StringUtil.convertWebString(ChineseConvert.convertAuto2B(getField(p_colname))));
            else if (p_language != null && p_language.trim().equals("utf-8g"))
		         sb.append(StringUtil.convertWebString(ChineseConvert.convertAuto2G(getField(p_colname))));
            else if (p_language != null && p_language.trim().equals("gb"))
		         sb.append(StringUtil.convertWebString(ChineseConvert.convertB2G(getField(p_colname))));
				else 
		         sb.append(StringUtil.convertWebString(getField(p_colname)));
		      sb.append("</textarea>");
			}
			else if (tmformat != null && tmformat.getType() == TMFormat.TMF_CHECKBOX) {
		      sb.append("<input ");
		      sb.append("id=\"").append(p_fieldId).append("\" name=\"").append(p_alias == null ? p_colname : p_alias).append("\" ");
		      sb.append("type=\"").append("checkbox").append("\" ");
				if (getField(p_colname).equals("Y"))
		         sb.append("checked ");
			   if (freadonly) 
			      sb.append(" onfocus=\"this.blur()\" ");
			   if (p_appendString != null)
			      sb.append(p_appendString).append(" ");
		      sb.append(">");
			}
			else {
			   if (tmformat != null && tmformat.getType() == TMFormat.TMF_PASSWORD)
		         sb.append("<input type=\"password\" ");
				else
		         sb.append("<input type=\"text\" ");
		      sb.append("id=\"").append(p_fieldId).append("\" name=\"").append(p_alias == null ? p_colname : p_alias).append("\" ");
            if (getColumnAttribute(p_colname, "nofieldwidth") == null
                || !getColumnAttribute(p_colname, "nofieldwidth").toString().trim().equals("Y")) {
               Integer iWidth = (Integer) getColumnAttribute(p_colname, "fieldwidth");
				   if (iWidth != null)
		            sb.append("size=\"").append(iWidth.intValue()).append("\" ");
				   else
		            sb.append("size=\"").append(myColLengths[i] > MAX_HTML_TEXT_INPUT_LENGTH ? MAX_HTML_TEXT_INPUT_LENGTH : myColLengths[i]).append("\" ");
				}
				if (!p_fForQuery)
		         sb.append("maxlength=\"").append(myColLengths[i]).append("\" ");

		      sb.append("value=\"");
				if (tmformat == null) {
               if (p_language != null && p_language.trim().equals("utf-8b"))
				      sb.append(StringUtil.convertWebString(StringUtil.sr(ChineseConvert.convertAuto2B(getField(p_colname)))));
               else if (p_language != null && p_language.trim().equals("utf-8g"))
				      sb.append(StringUtil.convertWebString(StringUtil.sr(ChineseConvert.convertAuto2G(getField(p_colname)))));
               else if (p_language != null && p_language.trim().equals("gb"))
				      sb.append(StringUtil.convertWebString(StringUtil.sr(ChineseConvert.convertB2G(getField(p_colname)))));
					else
				      sb.append(StringUtil.convertWebString(StringUtil.sr(getField(p_colname))));
				}
				else {
					if (tmformat instanceof TMFormatNumber) {
				      sb.append(StringUtil.toNumberOnly(tmformat.formatDisplay(getField(p_colname).trim())));
					}
					else {
				      sb.append(StringUtil.convertWebString(tmformat.formatDisplay(StringUtil.sr(getField(p_colname)))));
					}
				}
				sb.append("\" ");
			   if (freadonly) 
			      sb.append(" onfocus=\"this.blur()\" ");
			   if (p_appendString != null)
			      sb.append(p_appendString).append(" ");
		      sb.append(">");
			}
		}
		return(sb.toString());
	}
	public String getInputFieldWithComments(String p_colname, boolean p_fForQuery, String p_appendString) {
	   return(getInputFieldWithComments(p_colname, p_fForQuery, p_appendString, null));
	}
	public String getInputFieldWithComments(String p_colname, boolean p_fForQuery, String p_appendString, String p_language) {
		try {
		   StringBuffer sb = new StringBuffer();
	      String comments;
		   String inputField = getInputField(p_colname, p_fForQuery, p_appendString, p_language);
			if (inputField != null)
		      sb.append(inputField);
		   comments = getFieldComments(p_colname);
		   if (comments != null) 
		      sb.append(comments);
	      return(sb.toString());
      } catch(Exception e) {
		   UniLog.log(e);
	      return("");
		}
	}
	public Object getRowid() {
		if (tb == null) return(null);
	   return(tb.getRowid());
	}
	public Object getInsertedRowid() {
	   return(tb.getInsertedRowid());
	}
	public int insertRow() {
		if (tb == null) return(-1);
      try {
         checkDbConnection();
	      if (tb.insertRow(dbconn) == 0)
	         return(0);
	      else {
		      errmsg = tb.getErrmsg();
	         return(-1);
	      }
      } catch(Exception e) {
		   errmsg = "exception at insert row"+e.toString();
         log("exception at insert row"+e.toString());
		   UniLog.log(e);
	      return(-1);
      }
	}
	public int updateRow() {
	   return(updateRow(false));
	}
	public int updateRow(boolean p_fUpdateChangeFieldOnly) {
		if (tb == null) return(-1);
		try {
         checkDbConnection();
	      if (tb.updateRow(dbconn, p_fUpdateChangeFieldOnly) == 0) {
	         return(0);
			}
	      else {
		      errmsg = tb.getErrmsg();
	         return(-1);
	      }
      } catch(Exception e) {
		   errmsg = "exception at update row:"+e.toString();
         log("exception at update row:"+e.toString());
		   UniLog.log(e);
	      return(-1);
      }
	}
	public int deleteRow() {
		if (tb == null) return(-1);
		try {
         checkDbConnection();
	      if (tb.deleteRow(dbconn) == 0) {
	         return(0);
			}
	      else {
		      errmsg = tb.getErrmsg();
	         return(-1);
	      }
      } catch(Exception e) {
		   errmsg = "exception at delete row:"+e.toString();
         log("exception at delete row:"+e.toString());
		   UniLog.log(e);
	      return(-1);
      }
	}
	public String getErrmsg() {
	   return(errmsg);
	}
	public Wherecl buildWherecl() throws Exception {
	   Wherecl tmpwherecl;
		int j, cnt;
		if (queryColnames == null)
		   return(null);
		if (queryColvalues == null)
		   return(null);
		if (queryColnames.size() != queryColvalues.size())
		   return(null);
	   v_qtblookup = new Vector();
		tmpwherecl = new Wherecl();
		j = 0;
		cnt = queryColnames.size();

		SelectUtil su = null;
      boolean fScorpion = false;
		try {
			su = new SelectUtil();
			su.init(getConnection());
         fScorpion = (JdbcPool.findConnectionType(su.getConnection()) == JdbcPool.JDBC_SCORPION);
		} catch(Exception ex) {
			UniLog.log(ex.toString());
		} finally {
			if (su != null)
				su.close();
		}
	   for (int i=0; i<cnt; i++) {
			// filter off the user defined column
		   if (addColtypes.get(queryColnames.elementAt(i)) == null) {
				/*
			   if (j > 0)
			      tmpwherecl.appendString(" and ");
			   tmpwherecl.appendString(""+queryColnames.elementAt(i))
				          .appendString(" = '")
							 .appendString(""+queryColvalues.elementAt(i))
							 .appendString("'");
				*/
				Wherecl thiswhere = null;
	         if (v_tblookup.size() > 0) {
               switch (tb.getColType(queryColnames.elementAt(i).toString())) {
				      case TableBrowser.SQLTYPE_INTEGER:
				      case TableBrowser.SQLTYPE_FLOAT:
				      case TableBrowser.SQLTYPE_DATE:
				      case TableBrowser.SQLTYPE_CHAR:
				         if (queryColnames.elementAt(i).toString().indexOf('.') > 0) {
                        if (!new String(queryColvalues.elementAt(i).toString()).trim().equals("")) {
							      String tmpcolname = queryColnames.elementAt(i).toString();
							      String maptabname = tmpcolname.substring(0, tmpcolname.indexOf('.'));
	                        for (int k=0; k<v_tblookup.size(); k++) {
								      TBLookup tmplookup = (TBLookup) v_tblookup.elementAt(k);
									   if (tmplookup.getTabaliase().equals(maptabname)) {
	                              v_qtblookup.addElement(tmplookup);
									    }
								   }
				            }
						   }
						   break;
				   }
				}
				try {
               switch (tb.getColType(queryColnames.elementAt(i).toString())) {
				      case TableBrowser.SQLTYPE_INTEGER:
				      case TableBrowser.SQLTYPE_FLOAT:
				      case TableBrowser.SQLTYPE_DATE:
				         thiswhere = com.uniinformation.utils.constpar.Parser.cwhere(queryColvalues.elementAt(i).toString(),
				                                      queryColnames.elementAt(i).toString(),
															     tb.getColType(queryColnames.elementAt(i).toString())
				                                     );
					      break;
				      case TableBrowser.SQLTYPE_CHAR:
							if (fScorpion) {
				         	thiswhere = com.uniinformation.utils.constpar.Parser.cwherewild(queryColvalues.elementAt(i).toString(),
				                                      queryColnames.elementAt(i).toString(),
															     tb.getColType(queryColnames.elementAt(i).toString())
				                                     );
							}
							else {
				         	thiswhere = com.uniinformation.utils.constpar.Parser.cwherewildlike(queryColvalues.elementAt(i).toString(),
				                                      queryColnames.elementAt(i).toString(),
															     tb.getColType(queryColnames.elementAt(i).toString())
				                                     );
							}
					      break;
				      case TableBrowser.SQLTYPE_BINARY:
					      break;
				   }
				   if (thiswhere != null && !thiswhere.isNull())
				      tmpwherecl.andWherecl(thiswhere);
	         } catch (Error err) {
		         UniLog.log(err);
			      throw(new Exception(err.toString()));
		      }
			   j++;
			}
		}
		if (j > 0)
		   return(tmpwherecl);
	   else
		   return(null);
	}
   public int getRowidIdx() {
		if (tb == null) return(-1);
      return(tb.getRowidIdx());
	}
	public int firstCursor() {
		try {
		   if (tb == null) return(-1);
         return(tb.firstCursor());
		} catch (Exception ex) {
		   return(-1);
		}
	}
   public int list(int p_offset, int p_length) {
		String curarr[], tmparr[];
      int tmpRowidIdx;
		int tmpfbegin = 0;
UniLog.logClass(this, "trace:1 list() fcurrent="+fcurrent);
		if (!fcurrent) return(-1);
UniLog.logClass(this, "trace:2 list()");
		try {
         tmpRowidIdx = tb.getRowidIdx();
         tmpfbegin = beginwork();
         checkDbConnection();
			curRecListOffset = p_offset;
		   curRecList = new Vector();
UniLog.logClass(this, "trace:3 list()");
			if (fSelectMulti)
			   curRecMultiList = new Vector();
			else
			   curRecMultiList = null;
UniLog.logClass(this, "trace:4 list()");
		   curRecRowidList = new Vector();
	      //tb.firstCursor();
			int cc = tb.setCursor(dbconn, p_offset);
	      for (int tmpcnt=0; ; tmpcnt++) {
			   if (cc < 0)
				   break;
				if (p_length > 0 && tmpcnt >= p_length)
				   break;
            curarr = tb.getFields();
				tmparr = new String[curarr.length];
				for (int i=0; i<curarr.length; i++)
				   tmparr[i] = curarr[i];
			   curRecList.addElement(tmparr);
				if (fSelectMulti)
			      curRecMultiList.addElement(tb.getFieldsMulti());
				curRecRowidList.addElement(tb.getRowid());
		      cc = tb.nextCursor(dbconn);
		   }
UniLog.logClass(this, "trace:5 list()");
	      tb.setCursor(dbconn, tmpRowidIdx);
			if (tmpfbegin == 0)
            commitwork();
UniLog.logClass(this, "trace:6 list()");
	      return(curRecList.size());
		}
		catch (SQLException e) {
         log("nextCursor Exception"+e.toString());
		   UniLog.log(e);
			if (tmpfbegin == 0) {
            try { rollbackwork(); } catch (Exception ex) {};
			}
		   return(0);
		}
	}
	public String curRecListXslProcess() {
	   return(xslprocess("list"));
   }
   public int setCursorbyIdxRowid(int p_rowidx, String p_rowid) {
	   int tmpfbegin = 0, cc;
		if (tb == null) return(-1);
		try {
         tmpfbegin = beginwork();
         checkDbConnection();
         cc = tb.setCursorbyIdxRowid(dbconn, p_rowidx, p_rowid);
			if (tmpfbegin == 0) 
            commitwork();
			return(cc);
		}
		catch (SQLException e) {
         log("setCursorbyIdxRowid Exception"+e.toString());
		   UniLog.log(e);
			if (tmpfbegin == 0) {
            try { rollbackwork(); } catch (Exception ex) {};
			}
		   return(-1);
		}
	}
   public int queryCount() {
		if (tb == null) return(0);
	   return(tb.getCursorCount());
	}
   public int getCurRowIdx() {
		if (tb == null) return(-1);
	   return(tb.getCurrentRowIndex());
	}
	public int setDebug(int p_fdebug) {
		int i;
		i = fdebug;
		fdebug = p_fdebug;
	   return(i);
	}
	public void initComplete() {
		if (initCompleted)
		   return;
	   int tmpfbegin = 0;
		if (tb != null) {
         try {
	         tmpfbegin = beginwork();
	         checkDbConnection();
		      tb.setColumns(dbconn);
				if (tmpfbegin == 0)
				   commitwork();
         } catch (Exception e) {
			   // hahaha should free the dbconn
            log("exception at tablebrowser creation"+e.toString());
		      UniLog.log(e);
				if (tmpfbegin == 0) {
				   try { rollbackwork(); } catch (Exception ex) {};
				}
         }
		}
	   initCompleted = true;
	}
	public void addTBLookup(TBLookup p_tblookup) {
	   if (tb != null) {
	      v_tblookup.addElement(p_tblookup);
	      tb.addTBLookup(p_tblookup);
      }
	}
	/*
	public boolean storage_containsKey(Object p_key) {
		return(storageContainer.storage_containsKey(p_key));
	}
   public Object storage_get(Object p_key) {
		return(storageContainer.storage_get(p_key));
	}
	public void storage_set(Object p_key, Object p_value) {
		storageContainer.storage_set(p_key, p_value);
	}
	public boolean storage_create(String p_className, Object p_key) {
		return(storageContainer.storage_create(p_className, p_key,  this));
	}
	public void storage_invokeSet(Object p_key, Object p_name, Object p_value) {
		storageContainer.storage_invokeSet(p_key, p_name, p_value);
	}
	public Object storage_invokeGet(Object p_key, Object p_name) {
		return(storageContainer.storage_invokeGet(p_key, p_name));
	}
   public Object storage_invokeMethod(Object p_key, String p_methodName, Object[] p_args) {
		return(storageContainer.storage_invokeMethod(p_key, p_methodName, p_args));
	}
	*/
	private int findStringInArray(String[] arr, String p_value) {
		if (arr == null)
	      return(-1);
	   if (p_value == null)
		   return(-1);
	   for (int i=0; i<arr.length; i++) {
		   if (arr[i] != null && arr[i].equals(p_value)) {
			   return(i);
			}
		}
		return(-1);
	}
	private String arrayToString(String[] arr) {
		if (arr == null)
		   return("");
		StringBuffer sb = new StringBuffer();
		sb.append("[");
	   for (int i=0; i<arr.length; i++) {
			if (i > 0) {
		      sb.append(",");
			}
		   sb.append(arr[i]);
		}
		sb.append("]");
		return(sb.toString());
	}
	public void ping() {
	}
	public void resetRowid() {
		if (tb != null)
	      tb.resetRowid();
	}
	public boolean setFSelectMulti(boolean p_flag) {
	   boolean oflag = fSelectMulti;
	   fSelectMulti = p_flag;
		tb.setFSelectMulti(p_flag);
	   return(oflag);
	}
	public String getFieldOptionDesc(String p_colname) {
	   String strvalue, tmpstr;
		TMMap map;
		Object obj;
      obj = addColvalues.get(p_colname);
		if (obj instanceof String[]) {
		   UniLog.log("getField("+p_colname+"): is String[] instead of String");
		   return(null);
		}
      strvalue = (String) addColvalues.get(p_colname);
		if (strvalue == null && tb != null)
	      strvalue = tb.getField(p_colname);
      if (strvalue == null)
		   return(null);

		tmpstr = null;
		map = (TMMap) getColumnAttribute(p_colname, "option");
		if (map != null)
		   tmpstr = map.leftToRight((Object) strvalue).toString();
		if (tmpstr == null)
		   return(strvalue);
		else
		   return(tmpstr);
	}
	public void setLanguage(String p_language) {
	   language = p_language;
	}
	public void setAddXmlString(String p_string) {
	   addXmlString = p_string;
	}
	public String getAddXmlString() {
	   return(addXmlString);
	}
	// p_mode = 0 for add
	// p_mode = 1 for add
	public int checkUpdateFields(int p_mode) throws Exception {
	   String errMessage = null;
		int cc = 0;
		if (tb == null)
		   return(0);
		int cnt = tb.getColNames().length;
		for (int i=0; i<cnt; i++) {
			String required = (String) getColumnAttribute(myColNames[i], "required");
			if (required != null && required.equals("Y")) {
	         if (tb.getField(i).trim().equals("")) {
	            setFieldComments(myColNames[i], "Entry is required");
					if (errMessage == null)
	               errMessage = "Entry is required ["+myColNames[i]+"]";
				   cc = -1;
				}
			}
/*
         // does not check because phantom may cause exception
			if (p_mode == 1) {
			   String flag = (String) getColumnAttribute(myColNames[i], "noupdate");
			   if (flag != null && flag.equals("Y")) {
	            if (!tb.getField(i).trim().equals(tb.getSavedField(i).trim())) {
	               setFieldComments(myColNames[i], "Change is not allowed.  Original value is "+tb.getSavedField(i));
					   if (errMessage == null)
	                  errMessage = "Change is not allowed.  Original value is "+tb.getSavedField(i);
				      cc = -1;
			      }
		      }
			}
*/
		}
		if (cc < 0) 
		   throw(new Exception(errMessage));
		return(cc);
	}
	/*
	public void setWebLogin(WebLogin p_webLogin) {
	   webLogin = p_webLogin;
	}
	*/
	public int getColType(String p_colname) {
	   setupMyColumns();
		Integer colInteger = (Integer) myColIndexes.get(p_colname);
		if (colInteger == null)
		   return(-1);
      int[] coltypes = getColTypes();
	   return(coltypes[colInteger.intValue()]);
	}
	public int getColLength(String p_colname) {
	   setupMyColumns();
		Integer colInteger = (Integer) myColIndexes.get(p_colname);
		if (colInteger == null)
		   return(-1);
      int[] colLengths = getColLengths();
	   return(colLengths[colInteger.intValue()]);
	}
	public int getColSublength(String p_colname) {
	   setupMyColumns();
		Integer colInteger = (Integer) myColIndexes.get(p_colname);
		if (colInteger == null)
		   return(-1);
      int[] colSublengths = getColSublengths();
	   return(colSublengths[colInteger.intValue()]);
	}
	public boolean hasCurrentRecord() {
	   return(fcurrent);
	}
	public String getDisplayField(String p_colname, String p_language) {
	   setupMyColumns();
		Integer colInteger = (Integer) myColIndexes.get(p_colname);
		if (colInteger == null) {
		   return("");
		}
		StringBuffer sb = new StringBuffer();
		TMMap options = (TMMap) colAttributes.get((Object) (p_colname+"_"+"option"));
		TMFormat tmformat = (TMFormat) colAttributes.get((Object) (p_colname+"_"+"format"));
		if (options != null) {
			Vector leftvalues = options.getLeftValues();
			Vector rightvalues = options.getRightValues();
			int cnt = 0;
			for (int i=0; i<leftvalues.size(); i++) {
				if (options.isMultiple()) {
			      if (findStringInArray(getFields(p_colname), (String) leftvalues.elementAt(i)) >= 0) {
					   if (cnt > 0)
				         sb.append(", ");
				      sb.append(rightvalues.elementAt(i));
						cnt++;
				   }
				}
				else {
			      if (((String) leftvalues.elementAt(i)).equals(getField(p_colname))) {
				      sb.append(rightvalues.elementAt(i));
						cnt++;
				   }
				}
			}
		}
		else {
			int i = colInteger.intValue();
			if (tmformat != null && tmformat.getType() == TMFormat.TMF_CHECKBOX) {
				if (getField(p_colname).equals("Y"))
		         sb.append("Yes");
			   else
		         sb.append("No");
			}
			else {
				if (tmformat == null) {
               if (getColType(p_colname) == TableBrowser.SQLTYPE_INTEGER
					    && p_colname.endsWith("time")) {
				      sb.append(
							DateUtil.toTimeString(
						      new java.util.Date(Long.parseLong(getField(p_colname)) * 1000),
	                     defDateFormat.getPicture()+" HH:MM:SS"
							)
						);
					}
					else {
				      sb.append(StringUtil.sr(getField(p_colname)));
				   }
				}
				else {
				   sb.append(tmformat.formatDisplay(StringUtil.sr(getField(p_colname))));
			   }
			}
		}
      if (p_language != null && p_language.trim().equals("utf-8g"))
			return(ChineseConvert.convertAuto2G(sb.toString()));
      else if (p_language != null && p_language.trim().equals("utf-8b"))
			return(ChineseConvert.convertAuto2B(sb.toString()));
      else if (p_language != null && p_language.trim().equals("gb"))
			return(ChineseConvert.convertB2G(sb.toString()));
		else
			return(sb.toString());
	}
	public void setDefDateFormat(TMFormatDate p_defDateFormat) {
	   defDateFormat = p_defDateFormat;
	}
	public String getDefaultValue(String p_colname) {
		return((String) getColumnAttribute(p_colname, "default"));
	}
	public Wherecl getCurrentRecordWherecl(Wherecl p_wherecl) {
		return(tb.getCurrentRecordWherecl(p_wherecl));
	}
   public String getSavedField(String p_colname) {
	   return(tb.getSavedField(p_colname));
	}
	public Object getTypedSavedField(String p_colname) throws Exception{
	   switch (getColType(p_colname)) {
			case TableBrowser.SQLTYPE_INTEGER:
				if (getSavedField(p_colname).trim().equals(""))
				   return(new Integer(0));
				return(new Integer(getSavedField(p_colname).trim()));
			case TableBrowser.SQLTYPE_FLOAT:
				if (getSavedField(p_colname).trim().equals(""))
				   return(new Double(0.0));
				return(new Double(getSavedField(p_colname).trim()));
			case TableBrowser.SQLTYPE_DATE:
				return(DateUtil.getDate(getSavedField(p_colname)));
			case TableBrowser.SQLTYPE_CHAR:
				return(getSavedField(p_colname));
			default: 
			   throw(new Exception("getTypedField: unknown coltype "+getColType(p_colname)+" for column "+p_colname));
		}
	}
	public void setUniqueIdColumnName(String p_colName) {
		uniIdColName = p_colName;
	}
	public String getUniqueIdColumnName() {
		return(uniIdColName);
	}
	public void addPreventUpdateColumn(String p_colName) {
	   tb.addPreventUpdateColumn(p_colName);
	}
	public Hashtable getPreventUpdateColumns() {
	   return(tb.getPreventUpdateColumns());
	}
	public void resetUniqueIdColumnName(String p_colName) {
		setUniqueIdColumnName(p_colName);
		if (tb != null)
			tb.setUniqueIdColumnName(p_colName);
	}
	public String getStringByLanguage(String p_str, String p_language) {
      if (p_language != null && p_language.trim().equals("utf-8g"))
			return(StringUtil.convertWebString(ChineseConvert.convertAuto2G(p_str)));
      else if (p_language != null && p_language.trim().equals("utf-8b"))
			return(StringUtil.convertWebString(ChineseConvert.convertAuto2B(p_str)));
      else if (p_language != null && p_language.trim().equals("gb"))
		  	return(StringUtil.convertWebString(ChineseConvert.convertB2G(p_str)));
		else
		  	return(StringUtil.convertWebString(p_str));
	}
}
