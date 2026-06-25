package com.uniinformation.bicore.bischema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.DataModelHelper;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.JdbcRpcServer;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.WebCoreUtil;

public class BiResultBiTable extends BiResult {
	boolean transactionalDDL = false;
	
	
	public class FieldRec {
		public String fieldName;
		public String fieldType;
		public int fieldLen;
		FieldRec(String p_name,String p_type,int p_len) {
			fieldName = p_name;
			fieldType = p_type;
			fieldLen = p_len;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof FieldRec) ) return(false);
			if(!((FieldRec) o).fieldName.equals(fieldName)) return(false);
			if(!((FieldRec) o).fieldType.equals(fieldType)) return(false);
			if(((FieldRec) o).fieldLen != fieldLen) return(false);
			return(true);
		}
	}
	
	String currentCatalogsDb = null;
	HashMap<String,LinkedHashMap<String,FieldRec>> dbCatalogs;
	
	boolean fetchJdbcCatalogOk = true;
	boolean allowAlterSqlTable = false;
	
	
	LinkedHashMap<String,FieldRec>dbCatalogGetFieldList(String p_dbName,String p_tabName,SelectUtil p_su,String p_sidField) throws Exception {
		if(!p_dbName.equals(getCellString("dddb_database"))) return(null);
		if(!dbCatalogExistTable(p_dbName,p_tabName,p_su)) return(null);
		LinkedHashMap<String,FieldRec> fieldList = dbCatalogs.get(p_tabName);
		if(fieldList == null) {
			String dbCatalog;
			String dbSchema;
			String dbDatabase = getCellString("dddb_dbname");
			if(sh.isBiSchemaView()) {
				dbCatalog = getCellString("dddb_catalog"); 
				dbSchema  = getCellString("dddb_schema");
			} else {
				if(!p_dbName.equals(sh.getDbName())) return(null);
				dbCatalog = sh.getDbCatalog();
				dbSchema = sh.getDbSchema();
			}
			java.sql.Connection conn = p_su.getConnection();
			JdbcRpcServer jrpc = new JdbcRpcServer();
			jrpc.setConnection(conn);
			String s = jrpc.bi_gettableschema(
					dbDatabase,
					p_tabName, 
					dbCatalog,
					dbSchema
					);
			if(s == null && ! s.startsWith("OK  ")) throw new Exception("cannot get fieldlist from database");
			s = s.substring(4);
			JSONArray fList = new JSONArray(s);
			fieldList = new LinkedHashMap<String,FieldRec>();
			for(int i=0;i<fList.length();i++) {
				JSONObject jfrec = fList.getJSONObject(i);
				if(p_sidField != null && p_sidField.equals(jfrec.getString("colname"))) continue;
				int len;
				if(jfrec.getString("coltype").equals("char")) {
					len = jfrec.getInt("collen"); 
					//if(len >= DataModelHelper.MAXVARCHARLEN) len = 0;
					if(len == DataModelHelper.DEFVARCHARLEN) len = 0;  //andrew240123 revise to default length
				} else len = 0;
				fieldList.put(
						jfrec.getString("colname"), 
						new FieldRec(
						jfrec.getString("colname"), 
						jfrec.getString("coltype"), 
						len
								)
						);
			}
			
			dbCatalogs.put(p_tabName, fieldList);
		}
		return(fieldList);
	}
	
	Set<String >dbCatalogGetTableList(String p_dbName,SelectUtil p_su) throws Exception {
		if(currentCatalogsDb != null && !currentCatalogsDb.equals(p_dbName) ) {
			currentCatalogsDb = null;
			dbCatalogs = null;
		}
		if(!p_dbName.equals(getCellString("dddb_database"))) return(null);
		if(dbCatalogs == null) {
			String dbCatalog;
			String dbSchema;
			String dbDatabase = getCellString("dddb_dbname");
			if(sh.isBiSchemaView()) {
				dbCatalog = getCellString("dddb_catalog"); 
				dbSchema  = getCellString("dddb_schema");
			} else {
				if(!p_dbName.equals(sh.getDbName())) return(null);
				dbCatalog = sh.getDbCatalog();
				dbSchema = sh.getDbSchema();
			}
			java.sql.Connection conn = p_su.getConnection();
			JdbcRpcServer jrpc = new JdbcRpcServer();
			jrpc.setConnection(conn);
			String s = jrpc.bi_gettablelist(
					dbDatabase,
					dbCatalog,
					dbSchema
			);
			if(s == null && ! s.startsWith("OK  ")) throw new Exception("cannot get tablelist from database");
			s = s.substring(4);
			JSONArray tList = new JSONArray(s);
			dbCatalogs = new HashMap<String,LinkedHashMap<String,FieldRec>> ();
			currentCatalogsDb = p_dbName;
			for(int i=0;i<tList.length();i++) {
				dbCatalogs.put(tList.getString(i), null);
			}
		}
		return(dbCatalogs.keySet());
	}
	boolean dbCatalogExistTable(String p_dbName,String p_tabName,SelectUtil p_su) throws Exception {
		Set<String> ts = dbCatalogGetTableList(p_dbName,p_su);
		return(ts.contains(p_tabName));
	}
	
	public BiResultBiTable(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	/*
	@Override
	protected void createColumnCells(BiCellCollection p_col) {
		super.createColumnCells(p_col);
		ColumnCell cc = (ColumnCell) p_col.getCell("ddt_database");
		cc.addAction(new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
				BiCellCollection bcol = ((ColumnCell) p_value).getCollection();
				String agentConnectStr = readIniString(getSessionHelper(),p_value.getString(),"databaseString");
				if(agentConnectStr == null) return;
				java.sql.Connection conn = 
						WebCoreUtil.getJdbcPoolByConnectionString(
								"bischema__"+p_value.getString(),
								1,
								3,
								readIniString(getSessionHelper(),p_value.getString(),"databaseString"),
								readIniString(getSessionHelper(),p_value.getString(),"databaseLogin"),
								readIniString(getSessionHelper(),p_value.getString(),"databasePassword")
							).getConnection();
				JdbcRpcServer jrpc = new JdbcRpcServer();
				jrpc.setConnection(conn);
				String s = jrpc.bi_gettablelist(
						bcol.getCellString("dddb_dbname"), 
						bcol.getCellString("dddb_cataglog"), 
						bcol.getCellString("dddb_schema")
						);
				UniLog.log("bi_gettableschema got " + s);
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	*/

	/*
	static String readIniString(SessionHelper p_sp,String p_agent,String p_key) {
		String agent = p_sp.getAgent();
				
		if(agent == p_agent) {
			switch(p_key) {
			case "databaseString":
				return(p_sp.getConnectionStr());
			case "databaseLogin" :
				return(p_sp.getDbLogin());
			case "databasePassword":
				return(p_sp.getDbPassword());
			}
		}
		return(null);
	}
	*/
	
	ReturnMsg checkDeleteTableOkForBiSchema(String p_dbtName) {
		return(ReturnMsg.defaultOk);
	}
	ReturnMsg checkDeleteColumnOkForBiSchema(String p_dbtName) {
		return(ReturnMsg.defaultOk);
	}
	ReturnMsg checkModifyColumnOkForBiSchema(String p_dbtName) {
		return(ReturnMsg.defaultOk);
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {

		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(!col.getCellString("ddt_tabname").equals(col.getCellString("ddt_dbtname"))) return(rtn);
		
		SelectUtil su = null;
		try {
			if(!transactionalDDL || sh.isBiSchemaView()) {
				su = getSchemaSelectUtil(col.getCellString("ddt_database"));
			} else su = getSelectUtil(); 
			if(su == null) return(ReturnMsg.defaultOk);
			if(!dbCatalogExistTable(col.getCellString("ddt_database"),getCellString("ddt_dbtname"),su)) {
				return(ReturnMsg.defaultOk);
			}
			StringBuffer sb = new StringBuffer();
			sb.append("DROP TABLE `");
			sb.append(col.getCellString("ddt_dbtname"));
			sb.append("`");
			currentCatalogsDb = null;
			dbCatalogs = null;
			su.executeUpdate(sb.toString(), null);
		} catch (Exception ex) {
			UniLog.log(ex);
			if(!transactionalDDL || sh.isBiSchemaView()) su.close();
			
			return(new ReturnMsg(false,"fail to create SU"));
		} finally {
			if(!transactionalDDL || sh.isBiSchemaView()) su.close();
		}
		if(!transactionalDDL || sh.isBiSchemaView()) su.close();
		return(ReturnMsg.defaultOk);	
	}
	
	SelectUtil getSchemaSelectUtil(String p_dbName) throws Exception{
		SelectUtil su = null;
		String connectStr;
		String loginStr;
		String passwordStr;
		if(!sh.isBiSchemaView()) {
			if(!getSessionHelper().getDbName().equals(p_dbName)) return(null);
			connectStr = getSessionHelper().getConnectionStr();
			loginStr = getSessionHelper().getDbLogin();
			passwordStr = getSessionHelper().getDbPassword();
		} else {
			connectStr = getCellString("dddb_jdbcstr");
			loginStr = getCellString("dddb_jdbcuser");
			passwordStr = getCellString("dddb_jdbcpassword");
			if(StringUtils.isBlank(connectStr)) return(null);
		}
		try {
			su = new SelectUtil();
			su.init(WebCoreUtil.getJdbcPoolByConnectionString("bischema_"+p_dbName,1,1,
						connectStr,
						loginStr,
						passwordStr
						).getConnection(5));
			return(su);
		} catch (Exception ex) {
			UniLog.log(ex);
			if(su != null) su.close();
			return(null);
		}
	}
	
	ReturnMsg doInsertBiTable(String p_dbName,String p_tabName,SelectUtil p_su) throws Exception {
//		HashSet<FieldRec> fList = new HashSet<FieldRec>();
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE `");
		sb.append(p_tabName);
		sb.append("` ( `serial_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,");
		Vector<BiCellCollection> sl = getSubLink("bischema.BiField").getRowCollectionList();
		for(BiCellCollection bc : sl) {
			String fdName = bc.getCellString("ddf_fdname");
			String fdType = bc.getCellString("ddf_type");
			int fdLen = bc.getCellInt("ddf_len");
			UniLog.log("Add Field " + fdName + " " +  fdType + " " + fdLen);
			sb.append(String.format("`%s` %s,", fdName, DataModelHelper.fdTypeSql(fdType, fdLen)));
		}
		//sb.append(" PRIMARY KEY (`serial_id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
		sb.append(" PRIMARY KEY (`serial_id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4");  //andrew240124 change to utf8mb4 to support emoji
//		p_su.executeUpdate("use " + p_dbName,null );
		currentCatalogsDb = null;
		dbCatalogs = null;
		p_su.executeUpdate(sb.toString(), null);
		return(ReturnMsg.defaultOk);	
	}

	ReturnMsg doAlterBiTable(String p_dbName,String p_tabName,SelectUtil p_su,String p_sid) throws Exception {
		LinkedHashMap<String,FieldRec> fdList = dbCatalogGetFieldList(
			p_dbName,
			p_tabName,
			su,
			p_sid
		);
		List<FieldRec> addList = new ArrayList<FieldRec>();
		List<FieldRec> commonList = new ArrayList<FieldRec>();
		HashSet<String> newFdNames = new HashSet<String>();
		
		Vector<BiCellCollection> sl = getSubLink("bischema.BiField").getRowCollectionList();
		for(BiCellCollection bc : sl) {
			newFdNames.add(bc.getCellString("ddf_fdname"));
			if(fdList.containsKey(bc.getCellString("ddf_fdname"))) {
				commonList.add(
						new FieldRec(
								bc.getCellString("ddf_fdname"),
								bc.getCellString("ddf_type"),
								bc.getCellInt("ddf_len")
								)
						);
			} else {
				addList.add(
						new FieldRec(
								bc.getCellString("ddf_fdname"),
								bc.getCellString("ddf_type"),
								bc.getCellInt("ddf_len")
								)
						);
			}
		}
		for(String fn : fdList.keySet()) {
			if(!newFdNames.contains(fn)) {
				UniLog.log("delete column " + fn);
				String sql = DataModelHelper.dropColSql(p_tabName, fn);
				UniLog.log1("alter column sql:%s",sql);
				p_su.executeUpdate(sql, null);
			}
		}
		for(FieldRec fr : commonList) {
			if(!fr.equals(fdList.get(fr.fieldName))) {
				UniLog.log("alter column " + fr.fieldName);
				String sql = DataModelHelper.updateColSql(p_tabName, fr.fieldName, DataModelHelper.fdTypeSql(fr.fieldType, fr.fieldLen));
				UniLog.log1("alter column sql:%s",sql);
				p_su.executeUpdate(sql, null);
			}
		}
		for(FieldRec fr : addList) {
			UniLog.log("add column " + fr.fieldName);
			String sql = DataModelHelper.addColSql(p_tabName, fr.fieldName, DataModelHelper.fdTypeSql(fr.fieldType, fr.fieldLen));
			UniLog.log1("add column sql:%s",sql);
			p_su.executeUpdate(sql, null);
		}
		
		
		currentCatalogsDb = null;
		dbCatalogs = null;
		/*
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE `");
		sb.append(p_tabName);
		sb.append("` ( `serial_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, PRIMARY KEY (`serial_id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
		p_su.executeUpdate(sb.toString(), null);
		*/
		return(ReturnMsg.defaultOk);	
	}
	
	ReturnMsg doInsertOrAlterDbTable(CellCollection col) {
		if(!col.getCellString("ddt_tabname").equals(col.getCellString("ddt_dbtname"))) return(ReturnMsg.defaultOk);
		ReturnMsg rtn;
		SelectUtil su = null;
		try {
			if(!transactionalDDL || sh.isBiSchemaView()) {
				su = getSchemaSelectUtil(col.getCellString("ddt_database"));
			} else su = getSelectUtil(); 	
			if(su == null) return(ReturnMsg.defaultOk);
			String dbName = col.getCellString("ddt_database");
			String tabName = col.getCellString("ddt_dbtname");
			if(dbCatalogExistTable(dbName,tabName,su)) {
				String sid = col.getCellString("ddt_serialid");
				if(StringUtils.isBlank(sid)) sid = "serial_id";
				rtn = doAlterBiTable(col.getCellString("ddt_database"),col.getCellString("ddt_dbtname"),su,sid);
			} else {
				rtn = doInsertBiTable(col.getCellString("ddt_database"),col.getCellString("ddt_dbtname"),su);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			if(!transactionalDDL || sh.isBiSchemaView()) su.close();
			return(new ReturnMsg(false,"fail to create SU : "+ex.toString()));
		} finally {
			if(!transactionalDDL || sh.isBiSchemaView()) su.close();
		}
		if(!transactionalDDL || sh.isBiSchemaView()) su.close();
		return(rtn);	
		
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		return(doInsertOrAlterDbTable(col));
	}
	
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		return(doInsertOrAlterDbTable(col));
	}
	
	@Override
	protected ReturnMsg validateOneRow(CellCollection pcol,boolean isUpdate) {
		if(! isUpdate) {
		try {
			if(recordExist(
				new VectorUtil()
					.addElement("ddt_database")
					.addElement("ddt_tabname")
					.addElement("ddt_dbtname")
					.toVector()
				)) {
				return(new ReturnMsg(false,"Table Record Already Exist"));
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"got Exception " + ex.toString()));
		}
		}
		return(super.validateOneRow(pcol,isUpdate));
	}
	
	@Override
	protected void afterFetch() {
		Collection<FieldRec> fdList = dbCatalogGetFieldList();
		if (fdList != null) {
			try {
				BiResult sr = getSubLink("bischema.BiField");
				for(FieldRec fr : fdList) {
					BiCellCollection col = sr.newRowCollection();								
					sr.addSubRecord(col, -1 ,"");
					col.getCell("ddf_fdname").set(fr.fieldName);
					col.getCell("ddf_type").set(fr.fieldType);
					col.getCell("ddf_len").set(fr.fieldLen);
				}
			} catch (CellException ex) {
				UniLog.log(ex);
			}
		}
	}
	public Collection<FieldRec> dbCatalogGetFieldList() {
		currentCatalogsDb = null;
		dbCatalogs = null;
		fetchJdbcCatalogOk = false;
		if(sh.isBiSchemaView() || getCellString("ddt_database").equals(getSessionHelper().getDbName())) {
			SelectUtil su = null;
			try {
				String sid = getCellString("ddt_serialid");
				if(StringUtils.isBlank(sid)) sid = "serial_id";
				if(sh.isBiSchemaView()) {
					su = getSchemaSelectUtil(getCellString("ddt_database"));
					if(su == null) return null;
				} else {
					su = getSelectUtil();
					if(su == null) return null;
					if(su.getConnection() == null) su.init(getView().getConn());
				}
				switch(BiSchema.getSqlEnginetype(su.getConnection())) {
					case BiSchema.SQLENGINE_MYSQL : allowAlterSqlTable = true;
						break;
					default :allowAlterSqlTable = false;
						break;
				}
				Collection<FieldRec> fdList = dbCatalogGetFieldList(
						getCellString("ddt_database"),
						getCellString("ddt_dbtname"),
						su,
						sid
						).values();
				if(fdList == null) {
					UniLog.log("sql table " + getCellString("ddt_dbtname") + " not exist ");
					return null;
				}
				fetchJdbcCatalogOk = true;
				return fdList;
			} catch (Exception ex) {
				if(sh.isBiSchemaView() && su != null) {
					su.close();
				}
				UniLog.log(ex);
			} finally {
				if(sh.isBiSchemaView() && su != null) {
					su.close();
				}
			}
		}
		return null;
	}

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash) {
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(!sh.isBiSchemaView()) {
			p_where.andUniop("ddt_database", "=", sh.getDbName());
		}
		return(ht);
	}
	public boolean isFetchJdbcCatalogOk() {
		return(fetchJdbcCatalogOk);
	}
	public boolean isAllowAlterSqlTable() {
		return(allowAlterSqlTable);
	}
}
