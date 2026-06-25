package com.uniinformation.bicore;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.sql.*;

import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;
import com.kyoko.parser.Expression;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.bicore.bischema.ExcelWorkSheetCache;
import com.uniinformation.cell.*;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.webcore.*;
import com.kyoko.common.*;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
/*
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;
*/
import org.json.JSONObject;

public class BiSchema extends BiBase {
//	static Hashtable<String,Hashtable<String,Integer>> biVersionControl = new Hashtable<String,Hashtable<String,Integer>> ();
	public final static int SQLENGINE_GENERIC = 0;
	public final static int SQLENGINE_SCORPION = 1;
	public final static int SQLENGINE_MYSQL = 2;
	public final static int SQLENGINE_POSTGRESQL = 3;
//	static Hashtable schemaHash;
	Cell dbLabel;
//	Cell userid;
//	CellCollection tables;
//	CellCollection views;
	GetConnectionInterface getConn;
	String rpcServerPrefix;
	String rpcServerHost;
	int rpcServerPort;
	int sqlEngine;
	private boolean autoTranslate = false;
	transient String defaultDateFormat = null;
	//private HashSet<String> accessRights;
	static Hashtable <String,Integer> fieldTypeHash;
	String agentCode = null;
	Hashtable<String,HashSet<String>> aliasViews;
	String lhLang = null;
	boolean defaultAllowPickOldInput=false;
	public static AtomicBoolean debugFlag = new AtomicBoolean(false); //global debug flag, more log
	static HashMap<String,BiSchema> schemaCacheHM = new HashMap<String,BiSchema>();  //global schema cache, key by agent+_+loginId
	transient Hashtable<BiView,Hashtable<BiView,Hashtable<Object,Object[]>>> vhash;
	/*
	public void setDebug(boolean p_sw) //andrew190804: debug move to sessionHelper.setSchemaDebugFlag
	{
		debug = p_sw;
	}
	*/
	synchronized static public int getSqlTypeFromFieldType(String ftype)
	{
		//if(fieldTypeHash == null) return(-1);
		//andrew230601 fix cannot obtain typefield when load schema from xml 
		if(fieldTypeHash == null) {
			fieldTypeHash = new Hashtable();
			fieldTypeHash.put("integer", java.sql.Types.INTEGER);
			fieldTypeHash.put("char", java.sql.Types.CHAR);
			fieldTypeHash.put("float", java.sql.Types.DOUBLE);
			fieldTypeHash.put("decimal", java.sql.Types.DECIMAL);
			fieldTypeHash.put("money", java.sql.Types.DECIMAL);
			fieldTypeHash.put("date", java.sql.Types.DATE);
//			fieldTypeHash.put("datetime", java.sql.Types.TIME);
		}
		Integer I = fieldTypeHash.get(ftype);
		if(I == null) {
			return(-2);
		}
		return(I.intValue());
	}
			
	public BiSchema(String p_label,boolean p_defaultAllowPickOldInput) 
	{
		super(null);
		dbLabel = new Cell(p_label);
		addCollection("tables",new CellCollection(this));
		addCollection("views",new CellCollection(this));
		/*
		//andrew230601 fix cannot obtain typefield when load schema from xml 
		if(fieldTypeHash == null) {
			fieldTypeHash = new Hashtable();
			fieldTypeHash.put("integer", java.sql.Types.INTEGER);
			fieldTypeHash.put("char", java.sql.Types.CHAR);
			fieldTypeHash.put("float", java.sql.Types.DOUBLE);
			fieldTypeHash.put("date", java.sql.Types.DATE);
		}
		*/
		vhash = new Hashtable<BiView,Hashtable<BiView,Hashtable<Object,Object[]>>>();
		defaultAllowPickOldInput = p_defaultAllowPickOldInput;
	}
	/*
	public Connection getConnection()
	{
		return(getConn.getConnection());
	}*/
	public BiTable addTable(String p_tabname,String p_dbtname,String p_ename,String p_cname,String p_wherecl,String p_primaryKey,String p_serialid,String p_class)
	{
		BiTable table = null;
		if(p_class != null && !p_class.trim().equals("")) {
			try {
				UniLog.logm(this,"overridding BiTable to " + p_class);
				Class biTableClass = Class.forName(p_class);
				Class[] paramTypes = null;
				Constructor constructor = null;
				paramTypes = new Class[]{CellCollection.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class};
				constructor = biTableClass.getConstructor(paramTypes);
				table = (BiTable) constructor.newInstance(getCollection("tables"),p_tabname,p_dbtname,p_ename,p_cname,p_wherecl,p_primaryKey,p_serialid);
			} catch (Exception ex) {
				UniLog.log("class " + p_class + " not found , fallback to default BiTable");
				UniLog.log(ex);
				table = null;
			}
		}
		if(table == null) table = new BiTable(getCollection("tables"),p_tabname,p_dbtname,p_ename,p_cname,p_wherecl,p_primaryKey,p_serialid);
		getCollection("tables").addCollection(p_tabname,table);
		return(table);
	}
	public BiTable getTable(String p_tabname)
	{
		return((BiTable) getCollection("tables").getCollection(p_tabname));
	}
	BiView addView(String p_label,String p_header,BiTable p_rootTable,/* int p_rg,*/String p_BrClass,String p_attrubute,String p_fdUpdcnt,String p_ncStr,String p_addaccess,String p_updaccess,String p_delaccess,String p_rptaccess,String p_impaccess,String p_detaccess,String p_condition,String p_batupdaccess,String p_classPath)
	{
		BiView view = new BiView(getCollection("views"),p_header,p_rootTable,p_label,p_BrClass,p_attrubute,p_fdUpdcnt,p_ncStr,p_addaccess,p_updaccess,p_delaccess,p_rptaccess,p_impaccess,p_detaccess,p_condition,p_batupdaccess,p_classPath);
		getCollection("views").addCollection(""+p_label,view);
		return(view);
	}
	/*
	public BiView getView(int p_viewid)
	{
		return((BiView) getCollection("views").getCollection(""+p_viewid));
	}
	*/
	/*
	synchronized public static BiSchema getSchema(String p_label) 
	{
		if(schemaHash == null) return(null);
		return((BiSchema) schemaHash.get(p_label));
	}
	*/
	static private BiSchema loadSchemaFromDb(SelectUtil su,String p_dbLabel,GetConnectionInterface p_getConn,String p_connectStr/*,HashSet <String> p_accessRights*/,String p_dbSchema,String p_dbCatalog,boolean p_defaultAllowPickOldInput) throws Exception
	{
		UniLog.log("BiCore loadSchema entered "+p_connectStr);
		BiSchema schema=null;
		TableRec tr=null;
		String dbName;
		String dbCatalog;
		String dbSchema;
		tr = su.getQueryResult("select * from ddd_database where dddb_database = '"+p_dbLabel+"'",null);
		if(tr.size() <= 0) {
			UniLog.log("dblabel " + p_dbLabel + " not found in ddd_database ");
			return(null);
		}
		dbName = tr.getField("dddb_dbname").toString();
		dbCatalog = tr.getField("dddb_catalog").toString();
		dbSchema  = tr.getField("dddb_schema").toString();
		if(dbCatalog != null && dbCatalog.trim().equals("")) dbCatalog = null;
		if(p_dbSchema != null) dbSchema = p_dbSchema;
		if(p_dbCatalog != null) dbCatalog = p_dbCatalog;
		schema = new BiSchema(p_dbLabel,p_defaultAllowPickOldInput);
		String ss = tr.getField("dddb_autotranslate").toString();
		if(ss != null && ss.equals("Y")) {
			schema.autoTranslate = true;
		}
		ArrayList<String>dbLabels = new ArrayList<String>();
		dbLabels.add(p_dbLabel);
		for(;;) {
			if(tr.getFieldString("dddb_parent").trim().equals("")) break;
			tr = su.getQueryResult("select * from ddd_database where dddb_database = '"+tr.getFieldString("dddb_parent")+"'",null);
			if(tr.size() <= 0) {
				UniLog.log("dblabel " + tr.getFieldString("dddb_parent") + " not found in ddd_database ");
				return(null);
			}
			dbLabels.add(tr.getFieldString("dddb_database"));
		}
		SelectUtil su2 = new SelectUtil();
		java.sql.Connection jdbcConn = p_getConn.getConnection();
		if (jdbcConn == null){
			UniLog.logm(null,"jdbcConn is null");
			throw new Exception("jdbcConn is null");
		}
		schema.sqlEngine = getSqlEnginetype(jdbcConn);
		/*
		schema.sqlEngine=SQLENGINE_GENERIC;
		{
			String dn = jdbcConn.getMetaData().getDriverName();
			UniLog.log("jdbc Driver Name = " + dn);
			if(dn.contains("scorpion")) {
				schema.sqlEngine = SQLENGINE_SCORPION ;
			}
			if(dn.contains("MySQL")) {
				schema.sqlEngine = SQLENGINE_MYSQL;
			}
		}
		*/
		su2.init(jdbcConn);
		
//		{
//			if(schema.sqlEngine == SQLENGINE_SCORPION) {
//				UniLog.log("240517 check sql connection speed start");
//				RpcClient rpc = su2.getRpcClient();
//				for(int i=0;i<5;i++) {
//					UniLog.log("240517 check sql connection speed start " + i);
//					//TableRec trt = su2.getQueryResult("select count(*) numtables from systables");
//					Value v = rpc.callSegment("uname");
//					UniLog.log("240517 check sql connection speed end");
//				}
//			}
//		}
		
//		su2.init(p_getConn.getConnection());
		for(int i=0;i< dbLabels.size();i++) {
			loadSchemaTableFromDb(su2,schema.sqlEngine,schema,dbName,dbCatalog,dbSchema,su,dbLabels.get(i),p_getConn,p_connectStr);
		}
		su2.close();
		for(int i=0;i< dbLabels.size();i++) {
//			if(dbLabels.get(i).equals("clerpmulti"))  {
//				int cc;
//				cc = 0;
//			}
		
			loadSchemaViewFromDb(schema,dbName,dbCatalog,dbSchema,su,dbLabels.get(i),p_getConn,p_connectStr/*,HashSet <String> p_accessRights*/);
		}
		for(int i=0;i< dbLabels.size();i++) {
			String lastDbLabel = (i > 0 ? dbLabels.get(i-1):null);
			loadSchemaColumnFromDb(lastDbLabel,schema,dbName,dbCatalog,dbSchema,su,dbLabels.get(i),p_getConn,p_connectStr/*,HashSet <String> p_accessRights*/);
		}
		
		for(int i=0;i< dbLabels.size();i++) {
			loadSchemaOrdersFromDb(schema,dbName,dbCatalog,dbSchema,su,dbLabels.get(i),p_getConn,p_connectStr/*,HashSet <String> p_accessRights*/);
		}
		tr = su.getQueryResult("select * from dddviewrptclone where grptn_database = '"+p_dbLabel+"'",null);
		for(int i=0;i<tr.getRecordCount();i++) {
			tr.setRecPointer(i);
			String sv = tr.getFieldString("grptn_srcview");
			String cv = tr.getFieldString("grptn_cloneview");
			BiView bv0 = schema.getViewByName(sv);
			if(bv0 != null) {
				BiView bv1 = BiView.cloneView(cv,bv0);
				schema.getCollection("views").addCollection(cv,bv1);
				UniLog.log("cloneView " + sv + " -> " + cv);
			}
		}
		schema.getConn = p_getConn;
		return(schema);
	}
		
	static private void loadSchemaTableFromDb(SelectUtil su2,int sqlEngine,BiSchema schema,String dbName,String dbCatalog,String dbSchema,SelectUtil su,String p_dbLabel,GetConnectionInterface p_getConn,String p_connectStr/*,HashSet <String> p_accessRights*/) throws Exception 
	{
		TableRec tr=null;
		BiTable  table=null;
		tr = su.getQueryResult("select * from ddd_table where ddt_database = '"+p_dbLabel+"'",null);
		//schema.accessRights = p_accessRights;
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
			if(schema.getTable(tr.getFieldString("ddt_tabname")) != null) continue;
			table = schema.addTable(
				tr.getField("ddt_tabname").toString(),
				tr.getField("ddt_dbtname").toString(),
				tr.getField("ddt_ename").toString(),
				tr.getField("ddt_cname").toString(),
				tr.getField("ddt_wherecl").toString(),
				tr.getField("ddt_primarykey").toString(),
				tr.getField("ddt_serialid").toString(),
				tr.getField("ddt_class").toString()
			);
			if (debugFlag.get()) {
				UniLog.log("BiSchema addTable " + tr.getField("ddt_tabname").toString());
			}
			
			if(table != null) {
				if(sqlEngine == SQLENGINE_SCORPION) {
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
				}
				if(sqlEngine == SQLENGINE_MYSQL || sqlEngine == SQLENGINE_POSTGRESQL) {
//					TableRec tr2 = su2.getQueryResult(
//							"select ordinal_position,column_name,data_type,character_maximum_length"+
//								" from information_schema.columns" +
//									" where table_schema = '" + dbName
//									+ "' and table_name = '" + tr.getField("ddt_dbtname").toString()
//									+ "' order by ordinal_position",null);
//					for(int j = 0;j<tr2.size();j++) {
//						tr2.setRecPointer(j);
//						String s = tr2.getField("data_type").toString();
//						String st="unknown";
//						if(s.toUpperCase().equals("VARCHAR")) {
//							st = "char";
//						}
//						if(s.toUpperCase().equals("BIGINT")) {
//							st = "integer";
//						}
//						UniLog.log("BISchema add field " +
//										tr2.getField("column_name").toString() +  " type " + 
//										tr2.getField("data_type").toString());
//						table.addField (
//							tr2.getField("column_name").toString(),
//							tr2.getField("column_name").toString(),
//							tr2.getField("column_name").toString(),
//							st,
//							((Integer) tr2.getField("character_maximum_length")).intValue(),
//							0
//						);
//					}
					
					JdbcRpcServer jrpc = new JdbcRpcServer();
					jrpc.setConnection(su2.getConnection());
					String s = jrpc.bi_gettableschema(dbName, tr.getField("ddt_dbtname").toString(), dbCatalog, dbSchema);
//					UniLog.log("bi_gettableschema got " + s);
					{
						if(s != null && s.startsWith("OK  ")) {
//							JSONParser jsonParser = new JSONParser();
//							JSONArray columns = (JSONArray) jsonParser.parse(s.substring(4));
							JSONArray columns = new JSONArray(s.substring(4));
							for(int j=0;j<columns.length();j++) {
								JSONObject jo = (JSONObject) columns.get(j);
//								UniLog.log("colidx" + jo.get("colidx"));
//								UniLog.log("colname" + jo.get("colname"));
								int len=0;
								int dec=0;
								try {
									len  = Integer.parseInt((String) jo.opt("collen"));
								} catch (NumberFormatException nex) {
									
								}
								try {
									dec  = Integer.parseInt((String) jo.opt("coldec"));
								} catch (NumberFormatException nex) {
									
								}
								table.addField (
								(String) jo.get("colname"),
								(String) jo.get("colname"),
								(String) jo.get("colname"),
								(String) jo.get("coltype"),
								len,dec
								);								
								
								
							}
						} else {
							UniLog.log("bi_gettableschema error, table skipped");
						}
					}
				}
			} else {
				UniLog.log("BiSchema addTable " + tr.getField("ddt_tabname").toString() + " Failed" );
			}
		}
		/*
//		tr = su.getQueryResult("select * from ddd_column",null);
		tr = su2.getQueryResult("select ddt_tabname ddc_tabname,colno ddc_seq,colname ddc_colname,callsegs('bi_getfdtype',coltype) ddc_coltype,collength ddc_collen from ddd_table,systables,syscolumns where tabname = ddt_dbtname and syscolumns.tabid = systables.tabid " ,null);
		
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
			table = schema.getTable(tr.getField("ddc_tabname").toString());
			if(table != null) {
				UniLog.log("BISchema add field table " + tr.getField("ddc_tabname").toString() + " field " +
									tr.getField("ddc_colname").toString() +  " type " + 
									tr.getField("ddc_coltype").toString());
				table.addField (
					tr.getField("ddc_colname").toString(),
					tr.getField("ddc_colname").toString(),
					tr.getField("ddc_colname").toString(),
					tr.getField("ddc_coltype").toString(),
					((Integer) tr.getField("ddc_collen")).intValue(),
					0
				);
			} else {
				UniLog.log("addField getTable " + tr.getField("ddc_tabname").toString() + " not defined,skipped");
			}
		}
		*/
		
		
	}

	static private void loadSchemaViewFromDb(BiSchema schema,String dbName,String dbCatalog,String dbSchema,SelectUtil su,String p_dbLabel,GetConnectionInterface p_getConn,String p_connectStr/*,HashSet <String> p_accessRights*/) throws Exception
	{
		TableRec tr=null;
		BiTable  table=null;
		BiView   view = null;
//		tr = su.getQueryResult("select * from ddd_joins where ddj_idx = 0",null);
		tr = su.getQueryResult("select * from ddd_joins where ddj_database = '"+p_dbLabel+"'",null);
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
			table = schema.getTable(tr.getField("ddj_tabnamea").toString());
			if(table != null) {
//				if(table.getJoin(tr.getFieldString("ddj_tabnameb")) != null) continue;
				boolean isOptional = false;
				boolean isOneToOne = false;
				if(tr.getField("ddj_optional").toString().equals("Y")) {
					isOptional = true;
				}
				if(tr.getField("ddj_onetone").toString().equals("Y")) {
					isOneToOne = true;
				}
				boolean success = table.addJoin(
						tr.getField("ddj_tabnameb").toString(),
						tr.getField("ddj_joinfielda").toString(),
						tr.getField("ddj_joinfieldb").toString(),
						isOptional,
						tr.getField("ddj_condition").toString(),
						isOneToOne
					);
				if(!success) 
					UniLog.log("addJoin getTable " + 
						tr.getField("ddj_tabnamea").toString() + " " +
						tr.getField("ddj_tabnameb").toString() + " " +
						tr.getField("ddj_joinfielda").toString() + " " +
						tr.getField("ddj_joinfieldb").toString() + " failed, skipped");
				UniLog.log("addJoin getTable " + tr.getField("ddj_tabnamea").toString() + " sucessful");
			} else {
				UniLog.log("addJoin getTable " + tr.getField("ddj_tabnamea").toString() + " not defined, skipped");
			}
		}
		
		tr = su.getQueryResult("select * from dddviewrpthdr where grpth_database = '"+p_dbLabel+"'",null);
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
			int grpth_rg = ((Integer) tr.getField("grpth_rg")).intValue();
			int grpth_aliase = ((Integer) tr.getField("grpth_aliase")).intValue();
//			if(tr.getFieldString("grpth_id").equals("erpv4.Supplier")) {
//
//				int cc;
//				cc = 0;
//			}
			view = schema.getViewByName(tr.getFieldString("grpth_id"));
			if(view == null) {
			if(grpth_aliase > 0) {
				TableRec trv = su.getQueryResult("select grpth_id from dddviewrpthdr where grpth_rg = " + grpth_aliase);
				trv.setRecPointer(0);
				if(schema.aliasViews == null) {
					schema.aliasViews = new Hashtable<String,HashSet<String>>();
				}
				HashSet<String>aliasSet = schema.aliasViews.get(trv.getFieldString("grpth_id"));
				if(aliasSet == null) {
					aliasSet = new HashSet<String>();
					schema.aliasViews.put(trv.getFieldString("grpth_id"), aliasSet);
				}
				aliasSet.add( tr.getField("grpth_id").toString());
			}
			table = schema.getTable(tr.getField("grpth_table").toString());
			if(table == null) {
				UniLog.log("addView getTable " + tr.getField("grpth_table").toString() + " not defined, skipped");
				continue;
			}
			String fdUpdcnt = null;
			if(tr.existField("grpth_fupdcnt")) {
				fdUpdcnt = (String) tr.getField("grpth_fupdcnt");
			}
			view = schema.addView(
				tr.getField("grpth_id").toString(),
				tr.getField("grpth_header").toString(),
				table,
//				((Integer) tr.getField("grpth_rg")).intValue(),
				tr.getField("grpth_biclass").toString(),
				tr.getField("grpth_attribute").toString(),
				fdUpdcnt,
				tr.getField("grpth_ncstr").toString(),
				tr.getField("grpth_addaccess").toString(),
				tr.getField("grpth_updaccess").toString(),
				tr.getField("grpth_delaccess").toString(),
				tr.getField("grpth_rptaccess").toString(),
				tr.getField("grpth_impaccess").toString(),
				tr.getField("grpth_detaccess").toString(),
				tr.getField("grpth_condition").toString(),
				tr.getField("grpth_batupdaccess").toString(),
				tr.getField("grpth_classpath").toString()
				);
			} else {
				table = view.getTable();
			}
			//TableRec tr2 = su.getQueryResult("select distinct grptc_mastertab,grptc_subtable from dddviewrptcols where grptc_mrg = " + grpth_rg,null);  
			TableRec tr2 = su.getQueryResult("select distinct grptc_mastertab,grptc_subtable from dddviewrptcols where grptc_mrg = " + grpth_rg + " or grptc_mrg = "+grpth_aliase,null); //240106 fix mysql unique not support error
			for(int j = 0;j< tr2.size();j++) {
				tr2.setRecPointer(j);
				//UniLog.log("HAHA addOneSubChain (" +  view.getName() + ") " + tr2.getField("grptc_mastertab").toString().trim() + "," + tr2.getField("grptc_subtable").toString().trim());
				
				String subtableStr = tr2.getField("grptc_subtable").toString().trim();
				String mastertabStr = tr2.getField("grptc_mastertab").toString().trim();
				if(subtableStr.equals("") || subtableStr.equals(table.getName())) {
				} else {
					BiTable subtable = schema.getTable(subtableStr);
					BiTable mastertable;
					if(subtable == null) {
						UniLog.log("addViewColumn getSubTable " + subtableStr + " not defined, skipped");
						continue;
					}
					if(mastertabStr.equals("")) {
						mastertable = table;
					} else {
						mastertable = schema.getTable(mastertabStr);
						if(mastertable == null) {
							UniLog.log("addViewColumn getMasterTable " + tr.getField("grptc_mastertab").toString() + " not defined, skipped");
							continue;
						}
					}
					view.addOneChain(mastertable,subtable);
					if(debugFlag.get()) UniLog.log("Add subtable " + subtable.getName()+ " master " + mastertable.getName());
				}	
				
			}
		}
		
	}
	static private void loadSchemaColumnFromDb(String lastDbLabel,BiSchema schema,String dbName,String dbCatalog,String dbSchema,SelectUtil su,String p_dbLabel,GetConnectionInterface p_getConn,String p_connectStr/*,HashSet <String> p_accessRights*/) throws Exception
	{
		UniLog.log1("called dbLabel:%s lastDbLabel:%s", p_dbLabel, lastDbLabel);
		TableRec tr=null;
		BiView   view = null;
		BiField  field=null;
		

		//andrew210225: TODO slow query, need to optimize
		//andrew240122 mysql bischema(e.g. agent vincero) with a strange YYNN column. it's due to dddviewrptcols ld contains still has newline and the corrupted data affect this join. need to fix the newline before load to mysql
		tr = su.getQueryResult("select * from dddviewrpthdr,dddviewrptcols where grpth_database = '"+p_dbLabel+"' and (grptc_mrg = grpth_rg or grptc_mrg = grpth_aliase) order by grpth_rg,grptc_mrg,grptc_seq",null);
		int cidx = 0;
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
			/*
			if(tr.getFieldInt("grpth_rg") != tr.getFieldInt("grptc_mrg")) {
				int cc;
				cc = 0;
			}
			*/
			if(view ==  null || !view.getName().equals(tr.getFieldString("grpth_id"))) {
				view = schema.getViewByName(tr.getFieldString("grpth_id"));
				if(lastDbLabel == null) {
					cidx = 0;
				} else {
					TableRec tr2 = su.getQueryResult("select grpth_colidx from dddviewrpthdr where grpth_database = '" + lastDbLabel + "' and grpth_id = '"+ tr.getFieldString("grpth_id") + "'");
					if(tr2.getRecordCount() > 0) {
						cidx = tr2.getFieldInt("grpth_colidx");
					} else cidx = 0;
				}
			}
			if(view != null) {
				cidx++;
				if(view.getColumnByLabel(tr.getFieldString("grptc_label")) != null) {
					continue;
					
				}
				/*
				table = view.getTable();
				String subtableStr = tr.getField("grptc_subtable").toString().trim();
				if(subtableStr.equals("") || subtableStr.equals(table.getName())) {
					chain = (BiChain) view;
				} else {
					BiTable subtable = schema.getTable(subtableStr);
					BiTable mastertable;
					String mastertabStr = tr.getField("grptc_mastertab").toString().trim();
					if(subtable == null) {
						UniLog.log("addViewColumn getSubTable " + tr.getField("grptc_subtable").toString() + " not defined, skipped");
						continue;
					}
					if(mastertabStr.equals("")) {
						mastertable = table;
					} else {
						mastertable = schema.getTable(mastertabStr);
						if(mastertable == null) {
							UniLog.log("addViewColumn getMasterTable " + tr.getField("grptc_mastertab").toString() + " not defined, skipped");
							continue;
						}
					}
					if(schema.debug) UniLog.log("Add subtable " + subtable.getName()+ " master " + mastertable.getName());
					chain = view.findChain(mastertable).addChain(subtable);
				}
				*/

				String subtableStr = tr.getField("grptc_subtable").toString().trim();
				field = null;
				if(subtableStr.trim().equals("")){ 
				} else {
					BiTable subtable = schema.getTable(subtableStr);
					if(subtable != null) {
						BiChain chain = view.findChain(subtable);
						field = chain.getTable().getField(tr.getField("grptc_fd").toString());
						if(field == null) {
							//UniLog.log("addViewColumn getField " + tr.getField("grptc_fd").toString() + " for " + view.findChain(subtable).getTable().getName() + " not defined, set to non-database column");
							UniLog.log("addViewColumn view:["+view+"] field:[" + tr.getField("grptc_fd").toString() + "] table:[" + view.findChain(subtable).getTable().getName() + "] not defined, set to non-database column");
						}
					} else {
						UniLog.log("addViewColumn find table ("+subtableStr+") got null, set to non-database column");
					}
				}
				
//				field = chain.getTable().getField(tr.getField("grptc_fd").toString());
//				UniLog.log("HAHA view col " + ((Integer) tr.getField("grptc_seq")).intValue() + "(" + tr.getField("grptc_fd").toString()+")");
				boolean inlist;
				boolean inselect;
				boolean invisible;
				boolean noquery;
				if(tr.getField("grptc_inlist").toString().equals("Y")) {
					inlist = true;
				} else {
					inlist = false;
				}
				if(tr.getField("grptc_inselect").toString().equals("Y")) {
					inselect = true;
				} else {
					inselect = false;
				}
				if(tr.getField("grptc_noquery").toString().equals("Y")) {
					noquery = true;
				} else {
					noquery = false;
				}
				if(tr.getField("grptc_invisible").toString().equals("Y")) {
					invisible = true;
				} else {
					invisible = false;
				}
				String colLabel = tr.getField("grptc_label").toString();
				if(colLabel.trim().equals("")) {
					colLabel = "r_col"+((Integer) tr.getField("grptc_seq")).intValue();
				}
//				cidx++;
				view.addColumn(colLabel,
//										((Integer) tr.getField("grptc_seq")).intValue(),
									cidx,
									field,tr.getField("grptc_header").toString(),inlist,inselect,
									tr.getField("grptc_formula").toString(),
									tr.getField("grptc_attribute").toString(),
									tr.getField("grptc_format").toString(),
									noquery,
									invisible,
									((Integer) tr.getField("grptc_rgno")).intValue(),
									tr.getField("grptc_fdtype").toString(),
									((Integer) tr.getField("grptc_fdlen")).intValue(),
									tr.getField("grptc_accesskey").toString(),
									tr.getField("grptc_optionlist").toString(),
									tr.getField("grptc_default").toString(),
									tr.getField("grptc_pickview").toString(),
									tr.getFieldInt("grptc_fgcolor"),
									tr.getFieldInt("grptc_bgcolor"),
									tr.getFieldString("grptc_validation"),
									tr.getFieldString("grptc_aggregate")
									);
			} else {
				UniLog.log("addColumn getView " + tr.getField("grptc_mrg").toString() + " not defined,skipped");
			}
		}
		
	}
	static private void loadSchemaOrdersFromDb(BiSchema schema,String dbName,String dbCatalog,String dbSchema,SelectUtil su,String p_dbLabel,GetConnectionInterface p_getConn,String p_connectStr/*,HashSet <String> p_accessRights*/) throws Exception
	{
		
		TableRec tr=null;
		BiView   view = null;
		
		tr = su.getQueryResult("select * from dddviewrptorder,dddviewrpthdr where grpth_database = '"+p_dbLabel+"' and grpto_mrg = grpth_rg order by grpto_seq",null);
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
//			view = schema.getView(((Integer) tr.getField("grpto_mrg")).intValue());
			view = schema.getViewByName(tr.getFieldString("grpth_id"));
			Vector cv = view.getColumns();
			if(view != null) {
				int colidx = ((Integer) tr.getField("grpto_colidx")).intValue();
				String s = tr.getField("grpto_desc").toString().trim();
				boolean desc = false;
				if(s.equals("Y")) desc = true;
				/*
				if(colidx == 0) {
					view.addOrderBy(null,desc);
				} else
				if(colidx > 0) {
					//UniLog.log("HAHAHAHAHA add order by to view");
					view.addOrderBy((BiColumn) cv.get(colidx-1),desc);
				}
				*/
				BiColumn cl = view.getColumnByLabel(tr.getFieldString("grpto_fd"));
				if(cl != null) {
					view.addOrderBy(cl,desc);
				}
			}
		}
		tr = su.getQueryResult("select * from dddviewrptcond,dddviewrpthdr where grpth_database = '"+p_dbLabel+"' and grptd_mrg = grpth_rg",null);
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
//			view = schema.getView(((Integer) tr.getField("grptd_mrg")).intValue());
			view = schema.getViewByName(tr.getFieldString("grpth_id"));
			if(view != null) {
				view.addWhereCl(
					tr.getField("grptd_table").toString().trim(),
					tr.getField("grptd_fd").toString().trim(),
					tr.getField("grptd_cond").toString().trim(),
					tr.getField("grptd_val").toString().trim()
				);
			}
		}
		tr = su.getQueryResult("select grptl_seq,mview.grpth_id mgrpth_id,lview.grpth_id lgrpth_id,grptl_attribute,grptl_maxrow,grptl_access from dddviewrptlinks,dddviewrpthdr mview,dddviewrpthdr lview  where mview.grpth_database = '"+p_dbLabel+"' and grptl_mrg = mview.grpth_rg and grptl_lrg = lview.grpth_rg order by grptl_seq",null);
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
//			BiView view1 = schema.getView(((Integer) tr.getField("grptl_mrg")).intValue());
//			BiView view2 = schema.getView(((Integer) tr.getField("grptl_lrg")).intValue());
			BiView view1 = schema.getViewByName(tr.getFieldString("mgrpth_id"));
			BiView view2 = schema.getViewByName(tr.getFieldString("lgrpth_id"));
			if(view1 != null && view2 != null) {
				view1.addLinkView(view2,tr.getField("grptl_attribute").toString(),tr.getFieldInt("grptl_maxrow"),tr.getFieldString("grptl_access"));
//				view2.addMasterView(view1);
			}
		}
		
//		su.close();
//		schemaHash.put(p_connectStr,schema);
	}
	public List<BiView> getAllView() {
		List<BiView> list = new ArrayList<BiView>();
		Enumeration e = getCollection("views").getCollectionKeys();
		for(;e.hasMoreElements();) {
			BiView view = (BiView) getCollection("views").getCollection((String) e.nextElement());
			list.add(view);
		}
		return list;
	}
	public BiView getViewByName(String p_view)
	{
		
		Enumeration e = getCollection("views").getCollectionKeys();
		if(e == null) return(null);
		for(;e.hasMoreElements();) {
			BiView view = (BiView) getCollection("views").getCollection((String) e.nextElement());
			if(p_view.equals(view.label.getString())) {
				return(view);
			}
		}
		return(null);
	}
	public void exportToXML(File p_file) throws Exception{
		XStreamUtil.objToXML(this, p_file,
			new VectorUtil()
				.addElement("omitField")  //exlcude field
				.addElement("com.uniinformation.bicore.BiSchema")
				.addElement("getConn").toVector());
	}
	/*
	public static BiSchema loadSchema(SessionHelper sessionHelper) throws Exception
	{
		final String connectStr  = sessionHelper.getConnectionStr();
		final String dbLabel = sessionHelper.getDbLabel();
		BiSchema schema;
		schema = (BiSchema) sessionHelper.getSessionData("biSchema");
		String schemaXML = sessionHelper.getProperty("schemaXML");
		if(schemaXML != null && !schemaXML.equals("")) {
			UniLog.log("HAHA load schema from xml " + schemaXML);
			schema = BiSchema.loadSchemaFromXML(
					new File(schemaXML),
				new GetConnectionInterface(){
				public Connection getConnection() {
					return(WebCoreUtil.getJdbcPoolByConnectionString(dbLabel,2,connectStr).getConnection());
				}
				}, connectStr,sessionHelper.getAccessRights());
		} else {
			schema = BiSchema.loadSchemaFromDb(
				new GetConnectionInterface(){
				public Connection getConnection() {
					return(WebCoreUtil.getJdbcPoolByConnectionString(dbLabel,2,connectStr).getConnection());
				}
				}, connectStr,sessionHelper.getAccessRights()
				);
		}
		if(schema != null) sessionHelper.putSessionData("biSchema", schema);
		schema.userid = new Cell(sessionHelper.getLoginId());
		schema.rpcServerPrefix= sessionHelper.getRpcServerPrefix();
		schema.rpcServerHost = sessionHelper.getRpcServerHost();
		schema.rpcServerPort = sessionHelper.getRpcServerPort();
		return(schema);
	}
	*/

	public static synchronized BiSchema loadSchema(SessionHelper sessionHelper) throws Exception{
		return loadSchema(sessionHelper, false);
	}
	/***
	 * 
	 * @param sessionHelper
	 * @param reload true - force reload and load schema from db
	 * @return
	 * @throws Exception
	 */
	public static synchronized BiSchema loadSchema(SessionHelper sessionHelper, boolean reload) throws Exception
	{
		BiSchema schema = null;
		
		if (reload) {
			UniLog.log1("reload schema");
			sessionHelper.putSessionData("biSchema", null);
			putSchemaToCache(sessionHelper,null);
			
			//240130 when bischema reload, it will clear excel brcache too
			ExcelWorkSheetCache.clearBrCache();
		}
		
		//step 1: obtain schema from session
		schema = (BiSchema) sessionHelper.getSessionData("biSchema");
		if (schema != null && StringUtils.equals(schema.agentCode, sessionHelper.getAgent())){
			//UniLog.logm(null, "return session schema. agent:%s", schema.agentCode);
			return(schema);
		}
		//step 2: obtain schema from cache 
		schema = getSchemaFromCache(sessionHelper);
		if (schema != null && StringUtils.equals(schema.agentCode, sessionHelper.getAgent())){
			UniLog.log1("return cache schema. agent:%s", schema.agentCode);
			sessionHelper.putSessionData("biSchema", schema);
			return(schema);
		}
		
		//step 3: create new schema
		schema = loadSchema_gen(
				sessionHelper.getConnectionStr(),
				sessionHelper.getDbPoolCnt(),
				sessionHelper.getDbMaxPoolCnt(),
				sessionHelper.getDbLabel(),
				reload ? null : sessionHelper.getSchemaXML(), //andrew210827: when reload, ignore schema xml file
				//sessionHelper.getAccessRights(),
				sessionHelper.getRpcServerPrefix(),
				sessionHelper.getRpcServerHost(),
				sessionHelper.getRpcServerPort(),
				sessionHelper.getSchemaJdbc(),
				sessionHelper.getDbLogin(),
				sessionHelper.getDbPassword(),
				sessionHelper.getAgent(),
				sessionHelper.getCustomBiViewBase(),
				sessionHelper.getDbSchema(),
				sessionHelper.getDbCatalog(),
				sessionHelper.getLHLang(),
				sessionHelper.getAllowPickOldInput()
				);
		
		//step 4: store schema 
		if(schema != null) {
			UniLog.logm(null, "new schema created. agent:%s", schema.agentCode);
			sessionHelper.putSessionData("biSchema", schema);
			putSchemaToCache(sessionHelper,schema);
		}
		schema.defaultDateFormat = BiConfig.getDefaultDateFormat(sessionHelper);
		return(schema);

	}
	
	/***
	 * get schema from cache. 
	 * @param p_sh
	 * @return
	 */
	private static BiSchema getSchemaFromCache(SessionHelper p_sh){
		synchronized(schemaCacheHM){
			if (p_sh == null || !p_sh.getAllowSchemaCache()) {
				UniLog.log1("ignore cache schama");
				return null;
			}
			if (StringUtils.isBlank(p_sh.getAgent())){
				UniLog.log1("id or agent is null");
				return null;
			}
			UniLog.log1("get schema from cache. agent:" + p_sh.getAgent());
			return schemaCacheHM.get(p_sh.getAgent());
		}
	}
	/***
	 * put schema from cache. 
	 * @param p_sh
	 * @param p_schema
	 * @return
	 */
	private static ReturnMsg putSchemaToCache(SessionHelper p_sh, BiSchema p_schema){
		synchronized(schemaCacheHM){
			if (p_sh == null || !p_sh.getAllowSchemaCache()) {
				UniLog.log1("ignore cache schama");
				return ReturnMsg.defaultOk;
			}
			UniLog.log1("store schema to cache. agent:" + p_sh.getAgent());
			if (p_schema == null) {
				schemaCacheHM.remove(p_sh.getAgent());
			}
			else {
				schemaCacheHM.put(p_sh.getAgent(), p_schema);
			}
			return ReturnMsg.defaultOk;
		}
	}
	public static ReturnMsg clearSchemaCache(){
		UniLog.log1("called");
		synchronized(schemaCacheHM){
			schemaCacheHM.clear();
			return ReturnMsg.defaultOk;
		}
	}
	
	public static BiSchema loadSchema_gen(SessionHelper sessionHelper, String p_schemaXML) throws Exception {
		return loadSchema_gen(
				sessionHelper.getConnectionStr(),
				sessionHelper.getDbPoolCnt(),
				sessionHelper.getDbMaxPoolCnt(),
				sessionHelper.getDbLabel(),
				p_schemaXML,
				sessionHelper.getRpcServerPrefix(),
				sessionHelper.getRpcServerHost(),
				sessionHelper.getRpcServerPort(),
				sessionHelper.getSchemaJdbc(),
				sessionHelper.getDbLogin(),
				sessionHelper.getDbPassword(),
				sessionHelper.getAgent(),
				sessionHelper.getCustomBiViewBase(),
				sessionHelper.getDbSchema(),
				sessionHelper.getDbCatalog(),
				sessionHelper.getLHLang(),
				sessionHelper.getAllowPickOldInput()
				);
	}
	
	private static BiSchema loadSchema_gen(String p_connectionStr, int p_poolCnt, int p_maxPoolCnt,
				String p_dblabel,String p_schemaXML,String p_rpcprefix,
				String p_rpchost,int p_rpcport,String p_schemaJdbc,String p_jdbclogin,String p_jdbcpassword, String p_agent, String p_customBiViewBase,String p_jdbcSchema,String p_jdbcCatalog,String p_lhLang,boolean p_defaultAllowPickOldInput) throws Exception {
		if (StringUtils.isBlank(p_dblabel)) {
			UniLog.log1("dblabel is null");
			return null;
		}
		/*
		//should not load schema if agent is blank. 
		if (StringUtils.isBlank(p_agent)) {
			UniLog.log1("agent is blank");
			return null;
		}
		*/
		final String connectStr  = p_connectionStr;
		final String dbLabelFull = p_dblabel;  //e.g. ctps@live
		final String dbLabelShort = p_dblabel.replaceFirst("@.*$", "");  //e.g. ctps
		final String dbLogin = p_jdbclogin;
		final String dbPassword = p_jdbcpassword;
		final int poolCnt = p_poolCnt;
		final int maxPoolCnt = p_maxPoolCnt;
		BiSchema schema;
		File schemaXMLFile = null;
		if(StringUtils.isNotBlank(p_schemaXML)) {
			UniLog.log1("check schemal xml file " + p_schemaXML);
			schemaXMLFile = new File(p_schemaXML);
			if (!schemaXMLFile.exists()) {
				UniLog.log1("WARNING! schema xml file %s not exist, ignore xml file.", p_schemaXML);
				schemaXMLFile = null;
			}
		}
		if (schemaXMLFile != null) {
			UniLog.log1("load schema from xml");
			schema = BiSchema.loadSchemaFromXML(
					schemaXMLFile,
					new GetConnectionInterface(){
						public Connection getConnection() {
							Connection conn = WebCoreUtil.getJdbcPoolByConnectionString(dbLabelFull,poolCnt,maxPoolCnt,connectStr,dbLogin,dbPassword).getConnection();
							return(conn);
						}
					}, connectStr);
			schema.vhash = new Hashtable<BiView,Hashtable<BiView,Hashtable<Object,Object[]>>>();
		} 
		else {
			UniLog.log1("load schema from db");
			SelectUtil su = new SelectUtil();
			if(p_schemaJdbc == null) {
				su.init(WebCoreUtil.getJdbcPoolByConnectionString(dbLabelFull,poolCnt,maxPoolCnt,connectStr,dbLogin,dbPassword).getConnection());
			} 
			else {
				//su.init(WebCoreUtil.getJdbcPoolByConnectionString("bischema",poolCnt,maxPoolCnt,p_schemaJdbc,dbLogin,dbPassword).getConnection());
				//andrew240122 bischema db should be agent specific
				String biDbLabelFull = "bischema" + (StringUtils.isBlank(p_agent) ? "" : "@" + p_agent);
				su.init(WebCoreUtil.getJdbcPoolByConnectionString(biDbLabelFull,poolCnt,maxPoolCnt,p_schemaJdbc,dbLogin,dbPassword).getConnection());
			}
			
			schema = BiSchema.loadSchemaFromDb(
					su,
					dbLabelShort,
					new GetConnectionInterface(){
						public Connection getConnection() {
							Connection conn = WebCoreUtil.getJdbcPoolByConnectionString(dbLabelFull,poolCnt,maxPoolCnt,connectStr,dbLogin,dbPassword).getConnection();
							return(conn);
						}
					}, 
					connectStr,p_jdbcSchema,p_jdbcCatalog,p_defaultAllowPickOldInput);
			su.close();
		}
		
		//setup custom biview
		BiView.setupCustomBiViews(schema,p_customBiViewBase);
		BiView.translateHeaders(schema,p_lhLang);
		
//		schema.userid = new Cell(p_loginid);
		schema.rpcServerPrefix= p_rpcprefix;
		schema.rpcServerHost = p_rpchost;
		schema.rpcServerPort = p_rpcport;
		schema.agentCode = p_agent;
		return(schema);
	}
	static BiSchema loadSchemaFromXML(File p_file, GetConnectionInterface p_getConn,String p_connectStr/*,HashSet <String> p_accessRights*/) throws Exception{
		BiSchema schema = (BiSchema) XStreamUtil.xmlToObj(p_file);
		schema.setDbLabel(p_connectStr);
		schema.setGetConn(p_getConn);
		//schema.setAccessRights(p_accessRights);
		return(schema);
	}
	public void setGetConn(GetConnectionInterface p_getConn){
		getConn = p_getConn;
	}
	public void setDbLabel(String p_dbLabel){
		dbLabel = new Cell(p_dbLabel);
	}
	public String getDbLabel(){
		if(dbLabel == null) return(null);
		return(dbLabel.getString());
	}
	
	/*

	public static void main(String args[]) 
	{
		final String connectStr  = "databaseString=jdbc:scorpion:perfrpc:localhost:6002:everbest";
		BiSchema schema;
		BiView view;

		try {
			schema = loadSchema(new GetConnectionInterface(){
							public Connection getConnection() {
								return(WebCoreUtil.getJdbcPoolByConnectionString("everbest",2,connectStr).getConnection());
							}
						}
							,connectStr);
			view = schema.getViewByName("Customer Contact");
			UniLog.log("Customer Contact View = " + view);
			Vector v = view.getColumns();
			for(int i=0;i<v.size();i++) {
				BiColumn column = (BiColumn) v.get(i);
				UniLog.log("Column:"+column.getLabel() + " " + column.getEngName() + " " + column.getChnName() );
			}
			BiResult result = view.newBiResult(null);
			UniLog.log("HAHA BiResult : " + result);
			if(result != null) {
				result.query();
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	*/

	/*
	public String getLoginId()
	{
			if(userid != null) return(userid.getString()) ; else return(null);
	}
	*/
	
	public RpcClient getRpcClient() {
		if(rpcServerHost == null) return(null);
		if(rpcServerPort <= 0) return(null);
		RpcClient rpc = new RpcClient(rpcServerHost,rpcServerPort);
		rpc.open();
		if(rpcServerPrefix != null && rpcServerPrefix.startsWith(":")) {
			StringTokenizer stok = new StringTokenizer(rpcServerPrefix,":");
			String s = null;
			if(stok.hasMoreTokens()) {
				s = stok.nextToken();
				rpc.callSegment("jdbc_putenv",
						new VectorUtil()
							.addElement("DBPATH="+s)
							.toVector()
							);
				if(stok.hasMoreTokens()) {
					s = stok.nextToken();
					rpc.callSegment("perfwx_setchaindir",
						new VectorUtil()
							.addElement(s)
							.toVector()
							);
				}
			}
		}
		return(rpc);
		
	}
	
	public int getRg(BiResult p_br,String p_cocode,int p_rgno) {
		Value v = getUniqueRg(p_br,p_cocode,p_rgno,null,null,null);
		if(v!=null) return(v.toInt()); else return(-1);
	}
	public Value getUniqueRg(BiResult p_br,String p_cocode,int p_rgno,String p_tabName,String p_fieldName,String p_format) {
		/*
		if(sqlEngine != SQLENGINE_SCORPION) {
			return(RgUtil.getUniqueRg());
		}
		*/
		if(rpcServerHost == null) return(null);
		if(rpcServerPrefix == null) return(null);
		RpcClient rpc = new RpcClient(rpcServerHost,rpcServerPort);
		rpc.open();
		Value v = null;
		if(rpcServerPrefix.startsWith(":")) {
			StringTokenizer stok = new StringTokenizer(rpcServerPrefix,":");
			String s = null;
			if(stok.hasMoreTokens()) {
				s = stok.nextToken();
				rpc.callSegment("jdbc_putenv",
						new VectorUtil()
							.addElement("DBPATH="+s)
							.toVector()
							);
				if(stok.hasMoreTokens()) {
					s = stok.nextToken();
					rpc.callSegment("perfwx_setchaindir",
						new VectorUtil()
							.addElement(s)
							.toVector()
							);
				}
			}
			if(p_tabName != null && p_fieldName != null) {
				v = rpc.callSegment("getuniquerg",
						new VectorUtil()
							.addElement(p_rgno)
							.addElement(p_tabName)
							.addElement(p_fieldName)
							.addElement(p_format == null ? "":p_format)
							.addElement(p_cocode)
							.toVector()
							);
			} else {
				v = rpc.callSegment("getrg",
						new VectorUtil()
							.addElement(p_rgno)
							.addElement(p_cocode)
							.toVector()
							);
			}
		} else {
			if(p_tabName != null && p_fieldName != null) {
				v = rpc.callSegment(rpcServerPrefix,
						new VectorUtil().addElement("getuniquerg")
							.addElement(p_rgno)
							.addElement(p_tabName)
							.addElement(p_fieldName)
							.addElement(p_format == null ? "":p_format)
							.addElement(p_cocode)
							.toVector()
							);
			} else {
				v = rpc.callSegment(rpcServerPrefix,
						new VectorUtil().addElement("getrg")
							.addElement(p_rgno)
							.addElement(p_cocode)
							.toVector()
							);
			}
		}
		rpc.close();
		if(v == null) return(null);
		return(v);
	}
	static public String getUniqueField(BiResult p_br,String p_tabName,String p_fieldName,String p_format,int p_rgno,boolean p_reuse) {
		RpcClient rpc = p_br.getSessionHelper().getRpcClient();
		Value v = null;
		rpc.callSegment("callfunction",
			new VectorUtil()
				.addElement("beginwork")
				.toVector()
			);
		v = rpc.callSegment("getuniquerg",
			new VectorUtil()
				.addElement(p_rgno)
				.addElement(p_tabName)
				.addElement(p_fieldName)
				.addElement(p_format == null ? "":p_format)
				.addElement("")
				.toVector()
			);
		if(p_reuse) {
		rpc.callSegment("callfunction",
			new VectorUtil()
				.addElement("rollbackwork")
				.toVector()
			);
		} else {
		rpc.callSegment("callfunction",
			new VectorUtil()
				.addElement("commitwork")
				.toVector()
			);
		}
		rpc.close();
		if(v != null) return(v.toString()); else return(null);
	}
	static public String getUniqueFieldEx(BiResult p_br,String p_tabName,String p_fieldName,String p_format,int p_rgno,boolean p_reuse) {
		RpcClient rpc = p_br.getSessionHelper().getRpcClient();
		Value v = null;
		BiSchema sch =p_br.getSessionHelper().getBiSchema();
		if(sch.rpcServerPrefix.startsWith(":")) {
			StringTokenizer stok = new StringTokenizer(sch.rpcServerPrefix,":");
			String s = null;
			if(stok.hasMoreTokens()) {
				s = stok.nextToken();
				rpc.callSegment("jdbc_putenv",
						new VectorUtil()
							.addElement("DBPATH="+s)
							.toVector()
							);
				if(stok.hasMoreTokens()) {
					s = stok.nextToken();
					rpc.callSegment("perfwx_setchaindir",
						new VectorUtil()
							.addElement(s)
							.toVector()
							);
				}
			}
		}
		if(sch.sqlEngine != SQLENGINE_SCORPION) {
			rpc.callSegment("getDbLabel", new Vector());
		}
		rpc.callSegment("callfunction",
			new VectorUtil()
				.addElement("beginwork")
				.toVector()
			);
		v = rpc.callSegment("getuniquergex",
			new VectorUtil()
				.addElement(p_rgno)
				.addElement(p_tabName)
				.addElement(p_fieldName)
				.addElement(p_format == null ? "":p_format)
				.addElement("")
				.toVector()
			);
		if(p_reuse) {
		rpc.callSegment("callfunction",
			new VectorUtil()
				.addElement("rollbackwork")
				.toVector()
			);
		} else {
		rpc.callSegment("callfunction",
			new VectorUtil()
				.addElement("commitwork")
				.toVector()
			);
		}
		rpc.close();
		if(v != null) return(v.toString()); else return(null);
	}
	/*
	public void setAccessRights(HashSet <String> p_accessRights) {
		accessRights = p_accessRights;
	}
	public static boolean hasAccessRight(String p_key) {
		if(accessRights == null) return(false);
		return(accessRights.contains(p_key));
	}
	*/
	/***
	 * check user access right against p_key
	 * @param p_sh
	 * @param p_key
	 * @return
	 */
	public static boolean hasAccessRight(SessionHelper p_sh, String p_key) {  //andrew190719. change to static as it does not depend on BiSchema
		if(p_sh == null) {
			UniLog.log1("sh is null");
			return false;
		}
		return p_sh.hasAccessRight(p_key);  //andrew201116: core logic moved to SessionHelper
	}

	/* will obsolate soon , don't use */
	
	public String getRpcServerPrefix() {
		return(rpcServerPrefix);
	}
	public String getRpcServerHost() {
		return(rpcServerHost);
	}
	public int getRpcServerPort() {
		return(rpcServerPort);
	}
	private static void selfTest(){
		BiSchema schema;
		BiView view;
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("erpv4","dummy",null);
		try {
			schema = loadSchema(sh);
			
			view = schema.getViewByName("DirectSalesDet");
			UniLog.logm(null, "view:%s table:%s", view.getName(), view.getTable().getName());
			Vector v = view.getColumns();
			//Hashtable depHt = new Hashtable();
			for(int i=0;i<v.size();i++) {
				BiColumn col = (BiColumn) v.get(i);
				BiField field = col.getField();
				BiTable table = field.getTable();
				BiChain chain = view.findChain(table);
				if (chain.getParent() != null){
					CellCollection subChainCC =  chain.getParent().getCollection("subChains");
					BiTable subChainTable = subChainCC == null ? null : (BiTable)subChainCC.getCollection("table");
					
					if (subChainTable != null){
						if (!subChainTable.equals(view.getTable())){
							UniLog.logm(null,"HAHA: label:%s(%s) field:%s table:%s subChainTable:%s", col.getLabel(), col.getEngName(), field.getName(), table.getName(), subChainTable == null ? "null" : subChainTable.getName());
						}
					}
					/*
					if (field.getName().equals("mt_tpname")){
						StringBuffer sb = new StringBuffer();
						chain.getParent().toXML(sb);
						FileWriter fw = new FileWriter("/tmp/a.xml");
						fw.write(sb.toString());
						fw.close();
					}
					*/
					
				}
				
				BiTable tableDep = col.getTableDepend();
				
				boolean tableDepFlag = false;
				if (tableDep != null && !tableDep.getName().equals(view.getTable().getName())){
					tableDepFlag = true;
				}
				UniLog.logm(null, "col:%d: label:%s parent:%s tableDep:%s joinFlag:%s size:%d", 
						i, col.getLabel(), ((BiView)col.getParent()).getName(), tableDep == null ? "null" : tableDep.getName(), tableDepFlag, col.getTableDepends().size());
				if (tableDepFlag){
					BiJoin join = tableDep.getJoin(view.getTable());
					if (join != null){
						for (int j=0; j<join.getJoinCount(); j++){
							UniLog.logm(null, "join from:%s to:%s", join.getFromField(j).getName(), join.getToField(j).getName());
							//depHt.put(join.getToField(j).getName(), join.getFromField(j));
						}
					}
				}
			}
			BiResult result = view.newBiResult(sh.getLoginId(),null,null,sh);
			UniLog.log("BiResult : " + result);
			if(result != null) {
				result.query(true);
			}
		} 
		catch (Exception ex) {
			UniLog.log(ex);
		}
		
		UniLog.logm(null,"bye");
		System.exit(0);
	}
	
	public String getAgent() {
		return(agentCode);
	}
	public static void main(String args[]){
		selfTest();
	}
	
//	static public int getBiVersion(String p_agent,String p_viewid) {
//		return(0);
//		/*
//		synchronized (biVersionControl){
//			Hashtable<String,Integer> agentVersionControl = biVersionControl.get(p_agent);
//			if(agentVersionControl == null) {
//				agentVersionControl = new Hashtable<String,Integer>();
//				biVersionControl.put(p_agent, agentVersionControl);
//			}
//			Integer ver = agentVersionControl.get(p_viewid);
//			if(ver == null) {
//				ver = 0;
//				agentVersionControl.put(p_viewid,ver);
//			}
//			return(ver);
//		}
//		*/
//	}
//	static public int updateBiVersion(String p_agent,String p_viewid) {
//		return(0);
//		/*
//		synchronized (biVersionControl){
//			Hashtable<String,Integer> agentVersionControl = biVersionControl.get(p_agent);
//			if(agentVersionControl == null) {
//				agentVersionControl = new Hashtable<String,Integer>();
//				biVersionControl.put(p_agent, agentVersionControl);
//			}
//			Integer ver = agentVersionControl.get(p_viewid);
//			if(ver == null) {
//				ver = 0;
//				agentVersionControl.put(p_viewid,ver);
//			}
//			agentVersionControl.put(p_viewid,ver+1);
//			return(ver+1);
//		}
//		*/
//	}
	/*
	public boolean getDebug( ) {
		return(debug);
	}
	*/
	static public String getJdbcTableList(SessionHelper p_sh,String p_host,int p_port , String p_jdbcstr,String p_user,String p_password,String p_dbpath,String p_dbname,String p_catalog,String p_schema ) {
		RpcClient rpc = p_sh.getRpcClient();
		String rpcHost = p_host;
		int rpcPort = p_port;
		if(rpcPort <= 0) return(null);
		if(rpcHost.equals("")) return(null);
		Value v = rpc.callSegment(
				"rpccall_open", 
				new VectorUtil()
					.addElement(rpcHost)
					.addElement(rpcPort)
					.toVector()
				);
		if(v == null || v.toInt() < 0) return(null);
		int rpcHandle = v.toInt();
		String jdbcStr = p_jdbcstr;
		if(!jdbcStr.equals("")) {
			v = rpc.callSegment(
				"rpccall_one", 
				new VectorUtil()
					.addElement(rpcHandle)
					.addElement("com.uniinformation.utils.JdbcRpcServer.getConnection")
					.addElement(jdbcStr)
					.addElement(p_user)
					.addElement(p_password)
					.toVector()
				);
			if(v == null || !v.toString().startsWith("OK")) return(null);
		}
		String dbPath = p_dbpath;
		if(!dbPath.equals("")) {
			v = rpc.callSegment(
				"rpccall_one", 
				new VectorUtil()
					.addElement(rpcHandle)
					.addElement("jdbc_putenv")
					.addElement("DBPATH="+dbPath)
					.toVector()
				);
		}
		v = rpc.callSegment(
				"rpccall_one", 
				new VectorUtil()
					.addElement(rpcHandle)
					.addElement("bi_gettablelist")
					.addElement(p_dbname)
					.addElement(p_catalog)
					.addElement(p_schema)
					.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) return(null);
		return(v.toString().substring(4));
	}
	static public String getJdbcFieldList(SessionHelper p_sh,String p_host,int p_port , String p_jdbcstr,String p_user,String p_password,String p_dbpath,String p_dbname,String p_catalog,String p_schema ,String p_table) {
		RpcClient rpc = p_sh.getRpcClient();
		String rpcHost = p_host;
		int rpcPort = p_port;
		if(rpcPort <= 0) return(null);
		if(rpcHost.equals("")) return(null);
		Value v = rpc.callSegment(
				"rpccall_open", 
				new VectorUtil()
					.addElement(rpcHost)
					.addElement(rpcPort)
					.toVector()
				);
		if(v == null || v.toInt() < 0) return(null);
		int rpcHandle = v.toInt();
		String jdbcStr = p_jdbcstr;
		if(!jdbcStr.equals("")) {
			v = rpc.callSegment(
				"rpccall_one", 
				new VectorUtil()
					.addElement(rpcHandle)
					.addElement("com.uniinformation.utils.JdbcRpcServer.getConnection")
					.addElement(jdbcStr)
					.addElement(p_user)
					.addElement(p_password)
					.toVector()
				);
			if(v == null || !v.toString().startsWith("OK")) return(null);
		}
		String dbPath = p_dbpath;
		if(!dbPath.equals("")) {
			v = rpc.callSegment(
				"rpccall_one", 
				new VectorUtil()
					.addElement(rpcHandle)
					.addElement("jdbc_putenv")
					.addElement("DBPATH="+dbPath)
					.toVector()
				);
		}
		v = rpc.callSegment(
				"rpccall_one", 
				new VectorUtil()
					.addElement(rpcHandle)
					.addElement("bi_gettableschema")
					.addElement(p_dbname)
					.addElement(p_table)
					.addElement(p_catalog)
					.addElement(p_schema)
					.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) return(null);
		return(v.toString().substring(4));
	}
	
	public SelectUtil getSelectUtil() throws Exception {
		SelectUtil su = new SelectUtil();
		su.init(getConn.getConnection());
		return(su);
	}
	
	public int getSqlEngine() {
		return(sqlEngine);
	}

	public void removeSummaryCache(BiView p_view) {
		synchronized(vhash) {
			vhash.remove(p_view);
		}
	}
	public void removeSummaryCache(BiView p_mview,BiView p_dview) {
		synchronized(vhash) {
			vhash.remove(p_dview);
		}
	}
	public Hashtable<Object,Object[]>createSummaryCache(BiView p_mview,BiView p_dview) {
		Hashtable<Object,Object[]>cache = null;
		synchronized(vhash) {
			Hashtable<BiView,Hashtable<Object,Object[]>> dvh = vhash.get(p_mview);
			if(dvh == null) {
				dvh = new Hashtable<BiView,Hashtable<Object,Object[]>>();
				vhash.put(p_mview, dvh);
			}
			cache = dvh.get(p_dview);
			if(cache == null) {
				cache = new Hashtable<Object,Object[]>();
				dvh.put(p_dview,cache);
			}
		}
		return(cache);
	}
	public void addSummaryCache(BiView p_mview,BiView p_dview,Object key,Object[] value) {
		Hashtable<Object,Object[]>cache = null;
		Hashtable<BiView,Hashtable<Object,Object[]>> dvh = vhash.get(p_mview);
		if(dvh != null) cache = dvh.get(p_dview);
		if(cache == null) {
			cache = createSummaryCache(p_mview,p_dview);
		}
		cache.put(key, value);
	}
	public void removeSummaryCache(BiView p_mview,BiView p_dview,Object key) {
		synchronized(vhash) {
			Hashtable<BiView,Hashtable<Object,Object[]>> dvh = vhash.get(p_mview);
			if(dvh == null) return;
			Hashtable<Object,Object[]>cache = dvh.get(p_dview);
			if(cache == null) return;
			cache.remove(key);
		}
	}
	public Object[] getSummaryCache(BiView p_mview,BiView p_dview,Object key) {
		Hashtable<BiView,Hashtable<Object,Object[]>> dvh = vhash.get(p_mview);
		if(dvh == null) return(null);
		Hashtable<Object,Object[]>cache = dvh.get(p_dview);
		if(cache == null) return(null);
		return(cache.get(key));
	}
	
	public boolean getSchemaAutotranslate() {
		return(autoTranslate);
	}
	
	static public int getSqlEnginetype(java.sql.Connection p_conn) throws SQLException {
		String dn = p_conn.getMetaData().getDriverName();
		String productName = p_conn.getMetaData().getDatabaseProductName();
		String driver = dn == null ? "" : dn.toLowerCase(java.util.Locale.ROOT);
		String product = productName == null ? "" : productName.toLowerCase(java.util.Locale.ROOT);
		if(driver.contains("scorpion") || product.contains("scorpion")) {
			return(SQLENGINE_SCORPION );
		}
		if(driver.contains("mysql") || product.contains("mysql")) {
			return(SQLENGINE_MYSQL);
		}
		if(driver.contains("postgresql") || product.contains("postgresql")) {
			return(SQLENGINE_POSTGRESQL);
		}
		return(SQLENGINE_GENERIC);
	}
	
	transient Hashtable<String,HashSet<String>>dependencyHash = null;
	
	public synchronized HashSet<String> getDependedViews(String p_viewName) {
		if(dependencyHash == null) {
			dependencyHash = new Hashtable<String,HashSet<String>>();
			synchronized(dependencyHash) {
				com.kyoko.parser.whereclpar.Parser parser = new com.kyoko.parser.whereclpar.Parser(0,null);
				for(BiView bv : getAllView()) {
					if(!bv.getName().startsWith(dbLabel.getString())) continue;
					Vector<BiColumn> cols = bv.getColumns();
					for(BiColumn bc : cols) {
						//if(bc.getField() == null) continue; // skip those columns that does not has real database column
						
						if(!StringUtils.isBlank(bc.getFormula(false))) {
							try {
								Expression exp = (Expression) parser.parse( bc.getFormula(false).toString());
								HashSet<String> vs  = exp.getViewHash(new HashSet<String>());
								for(String ss : vs) {
									String vn = dbLabel.getString()+"."+ss;
									if(!bv.getName().equals(vn)) {
										HashSet<String> dHash = dependencyHash.get(vn);
										if(dHash == null) {
											dHash = new HashSet<String>();
											dependencyHash.put(vn,dHash);
										}
										dHash.add(bv.getName());
									}
								}
							} catch (Exception ex) {
								UniLog.log(ex);
							}
						}
					}
				}
			}
		}
		return(dependencyHash.get(p_viewName));
	}
	
	public HashSet<String> getAliasSet(String p_view) {
		if(aliasViews == null)  return(null);
		return(aliasViews.get(p_view));
	}
}
