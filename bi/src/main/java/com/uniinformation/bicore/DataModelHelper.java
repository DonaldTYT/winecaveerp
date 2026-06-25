package com.uniinformation.bicore;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
import org.apache.commons.beanutils.BeanUtils;
*/
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.parser.ExcelTranslate;
import com.kyoko.parser.excelformula.CellPositionInterface;
import com.kyoko.parser.excelformula.ColumnTranslateInterface;
import com.kyoko.parser.excelformula.ExcelCellRef;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.JdbcPool;
import com.uniinformation.utils.MysqlUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class DataModelHelper {
	public final static int MINVARCHARLEN=21;
	public final static int DEFVARCHARLEN=80;   //remark240122 row size limit is 65535, need to set a reasonable default len , change from 256 to 80 by DT on 2024/05/21
	public final static int MAXVARCHARLEN=16384;
	public final static String BACKUP_CMD = "/usr/unc/bin/dmh_backupdb.sh";
	final SessionHelper sh;
	
	
	
	
	public DataModelHelper(SessionHelper p_sh) {
		sh = p_sh;
	}
	

	/*
	private boolean isSupportedDB(Connection conn) throws Exception {
		if (conn == null) {
			UniLog.log1("conn is null");
			return false;
		}
		checkConn(conn);
		String dn = conn.getMetaData().getDriverName();
		UniLog.log("jdbc driver = " + dn);
		if (StringUtils.containsIgnoreCase(dn, "MySQL")) return true;
		return false;
	}
	
	private void closeConn(Connection conn) {
		UniLog.log1("called " + conn);
		checkConn(conn);
		if (conn == null) {
			UniLog.log1("conn is null");
			return;
		}
		try {
			conn.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	private void checkConn(Connection conn) {
		if (conn == null) {
			UniLog.log1("conn is null");
			return;
		}
		try {
			UniLog.log1("conn:%s autoCommit:%s isClose:%s", conn, conn.getAutoCommit(), conn.isClosed());
		}
		catch(Exception ex) {
			ex.printStackTrace();
			
		}
	}
	*/
	
	/***
	 * use it for normalize field/table name and validation
	 * @param p_tag
	 * @return
	 * @throws Exception when unable to normalize
	 */
	public static String normalizeTag(String p_tag) throws Exception {
		return(ExcelTranslate.normalizeTag(p_tag));
	}
	/*
	public static String normalizeTag(String p_tag) throws Exception {
		if (StringUtils.isBlank(p_tag)) return "";
		UniLog.log1("called tag:%s", p_tag);
		
		//pre validation, required for handle excel formula sheetname
		if (!p_tag.matches("^[a-zA-Z0-9_ -]*$")) {
			String errMsg = String.format("invalid name(%s). It contain unsupported character", p_tag);
			UniLog.log1(errMsg);
			throw new Exception(errMsg);
		}
		
		
		//normalize
		String newTag = p_tag;
		newTag = p_tag.replaceAll("[ ]", "_").toLowerCase();
		newTag = newTag.replaceAll("\\$", "dols");  //special handle dollar sign
		newTag = newTag.replaceAll("[^a-zA-Z0-9_]*", "");
		newTag = newTag.replaceAll("^[_]*", "");  //remove _ from left
		newTag = newTag.replaceAll("[_]*$", "");  //remove _ from right
		UniLog.log1("newtag:%s",  newTag);
		
		
		//validation
		if (newTag.length() < 2) {
			String errMsg = String.format("invalid name(%s). length at least 2", p_tag);
			UniLog.log1(errMsg);
			throw new Exception(errMsg);
		}
		if (!newTag.matches("^[a-z].*")){
			String errMsg = String.format("invalid name(%s). first letter must be alphabet", p_tag);
			UniLog.log1(errMsg);
			throw new Exception(errMsg);
		}
		return newTag;
	}
	*/
	
	/***
	 * remind that the view globally shared. don't modify the content of the view.
	 * if need to change the view, should be based on follow flow:
	 * - update use BiResult of bischema.BiView 
	 * - refresh the bischema from db
	 * @return
	 */
	public List<BiView> getAllView() {
		//TODO need to protect system view
		return sh.getBiSchema().getAllView();
	}
	
//	public void createTable() {
//		//remark240118: 
//		//DDL must be autocommit, not possible to rollback
//		//table should contain a invisible serial_id as primary key
//		//table should can contain multiple unique index, but the index field should be notnull
//	}
	
	public static String dropTableSql(String p_tabName) {
		return String.format("DROP TABLE %s", p_tabName);
	}
	
	public static String dropColSql(String p_tabName, String p_colName) {
		return String.format("ALTER TABLE %s DROP %s", p_tabName, p_colName);
	}
	public static String updateColSql(String p_tabName, String p_colName, String p_typeSql) {
		return String.format("ALTER TABLE %s MODIFY %s %s", p_tabName, p_colName, p_typeSql);
	}
	public static String addColSql(String p_tabName, String p_colName, String p_typeSql) {
		return String.format("ALTER TABLE %s ADD %s %s", p_tabName, p_colName, p_typeSql);
	}
	public static String fdTypeSql(String p_fdType, int p_fdLen) throws Exception {
		return fdTypeSql(p_fdType, p_fdLen, true);
	}
	public static String fdTypeSql(String p_fdType, int p_fdLen, boolean p_withDef) throws Exception {
		switch(p_fdType) {
			/* 
			 * in mysql's version, list should alway be mapped to char database field,  for the case that list is map to integer and the integer value represents the
			 *  list index, change the field type to "index"
			 */
		case "list": 
		case "combobox": 
		case "char":
		case "memo":
			if(p_fdLen <= 0 || p_fdLen >= MINVARCHARLEN) {
				if(p_fdLen == 0) p_fdLen = DEFVARCHARLEN;
				if(p_fdLen > MAXVARCHARLEN) p_fdLen = MAXVARCHARLEN;
				return "varchar("+p_fdLen+ ")" + (p_withDef ? " DEFAULT NULL" : "");
			} 
			else {
				return "char("+p_fdLen+ ")" + (p_withDef ? " DEFAULT NULL" : "");
			}
		case "datetime":  //240123 use int as datetime. data will compatible with scorpion db. later can consider update to datetime
		case "integer":
			return " int(10)" + (p_withDef ? "  DEFAULT 0" : "");
		case "float":
			return "double" + (p_withDef ? " DEFAULT 0" : "");
		case "date":
			return "date" + (p_withDef ? " DEFAULT NULL" : "");
		case "checkbox":
			return "char(1)" + (p_withDef ? " DEFAULT NULL" : "");
		default : throw new Exception("field type not supported "+ p_fdType);
		}
		
	}
	
	public static String createIdxSql(String p_tabName, String p_idxName, boolean p_unique, String[] p_cols) {
		return String.format("CREATE %s INDEX %s ON %s(%s)", p_idxName, p_unique ? "UNIQUE" : "", p_tabName, StringUtils.join(p_cols,","));
	}
	public static String dropIdxSql(String p_tabName, String p_idxName) {
		return String.format("DROP INDEX %s ON %s", p_idxName, p_tabName);
	}
	
	public static void testTranslateFormula() throws Exception{
		//sample code for translate a formula content
		//first data cell position (next row of first header cell)
		HashMap<String,String> dataPosHM = new HashMap();
		dataPosHM.put("sheet1", "A2");
		dataPosHM.put("sheet2", "A2");
		
		//formula position
		String formulaSheet = "sheet1";
		String formulaCellPos = "A2";
		
		//excel raw formula. without = prefix
		String formula = "C2 + D2";
		
		
		//translate the excel pos. e.g. A1 to 0:0
		Pair<Boolean,Integer>[] formulaCellPosIdx = ExcelCellRef.decodeExcelRC(formulaCellPos) ;
		int formulaCellColIdx = formulaCellPosIdx[0].getRight();
		int formulaCellRowIdx = formulaCellPosIdx[1].getRight();
		UniLog.log1("formula cell position:%s colIdx:%d rowIdx:%d", formulaCellPos, formulaCellColIdx, formulaCellRowIdx);
		com.kyoko.parser.excelformula.Parser yyparser = new com.kyoko.parser.excelformula.Parser(
			new CellPositionInterface() {
				@Override
				public int getColIdx() {
					return formulaCellColIdx;
				}
				@Override
				public int getRowIdx() {
					return formulaCellRowIdx;
				}
				
		});
		
		//parse formula
		Object parseResult = yyparser.parse(formula);
		UniLog.log1("parseResult:" + parseResult);
		if(!(parseResult instanceof com.kyoko.parser.Expression)) {
			throw new Exception("Invalid Formula:" + formula);
		}
		
		//translate the formula
		com.kyoko.parser.Expression translatedExpression = 
				com.kyoko.parser.ExcelTranslate.translateFromXlsToBi((com.kyoko.parser.Expression) parseResult,null,null,
						new ColumnTranslateInterface() {

					@Override
					public String cellColumnToBiColumn(String p_workSheet, int col) throws CellException {
						//get the col label based on col idx
						UniLog.log1("called: worksheet:%s, col:%d", p_workSheet == null ? "current" : p_workSheet, col);
						
						//TODO need to return the actual bicolumn label based on col idx
						//this sameple only return a dummy column name
						return ("testcol" + col);  
					}

					@Override
					public int biColumnToCellColumn( String p_workSheet, String p_label) throws CellException {
						throw new CellException("Method not implemented");
					}

					@Override
					public String getWorkSheetFirstValuePosition(String p_worksheet) {
						//get first data cell position
						UniLog.log1("called: worksheet:%s", p_worksheet);
						
						return dataPosHM.get(p_worksheet == null ? formulaSheet : p_worksheet);
					}

					@Override
					public String getCurrentSheetName() {
						return formulaSheet;
					}

				});	
		String translatedFormula = translatedExpression.toString();
		UniLog.log1("result: " + translatedFormula);

	}
	
	
	public void selfTest() {
		//Connection conn = null;
		try {
			UniLog.log1("called");
			//conn = sh.getJdbcPool().getConnection();
			
			
			//conn.setAutoCommit(false);  //enable transaction
			
			//test create/update table 
			//remark ddl cannot rollback
			//MysqlUtil.executeUpdate(conn, "CREATE TABLE testcreate (tc_key varchar(100) NOT NULL, tc_name varchar(100) DEFAULT NULL, PRIMARY KEY (tc_key)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			//MysqlUtil.executeUpdate(conn, "CREATE UNIQUE INDEX idx_testcreate_tc_name2 ON testcreate(tc_name)");
			//MysqlUtil.executeUpdate(conn, "CREATE UNIQUE INDEX idx_testcreate_tc_name2 ON testcreate(tc_name)");
			//MysqlUtil.executeUpdate(conn, "DROP INDEX idx_testcreate_tc_name ON testcreate");
			
			//MysqlUtil.executeUpdateNoEx(conn, "DROP TABLE testcreate");
			//MysqlUtil.executeUpdate(conn, "create table testcreate (serial_id bigint unsigned not null auto_increment, tc_key varchar(100) NOT NULL, tc_name varchar(100) DEFAULT NULL, PRIMARY KEY (tc_key)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			
			//MysqlUtil.executeUpdateNoEx(conn, "DROP TABLE testcreate");
			//MysqlUtil.executeUpdate(conn, "CREATE TABLE testcreate (serial_id bigint unsigned not null auto_increment, tc_key varchar(100) DEFAULT '', tc_name varchar(100) DEFAULT '', PRIMARY KEY (serial_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			
			
			
//			CREATE TABLE testcreate (serial_id bigint unsigned not null auto_increment, 
//					tc_key varchar(100) DEFAULT '', 
//					tc_name varchar(20000) DEFAULT '', 
//					PRIMARY KEY (serial_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
//
//					ALTER TABLE testcreate DROP addcol1;
//					ALTER TABLE testcreate ADD addcol1 VARCHAR(20) default '';  
//					ALTER TABLE testcreate MODIFY addcol1 VARCHAR(30) default '';
//
//					ALTER TABLE testcreate DROP addcol2;
//					ALTER TABLE testcreate ADD addcol2 date;  
//
//					ALTER TABLE testcreate DROP coldouble;
//					ALTER TABLE testcreate ADD coldouble double;  
//					ALTER TABLE testcreate ADD coldouble double default 0;  


			
			
			
			//test create/update unique index
			
			
			//commitConn(conn);
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
		}
		
	}
	
	/***
	 * add or update webwemu.
	 * @param p_viewId
	 * @param p_desc
	 * @param p_seq
	 * @param p_deleteMode
	 * @return
	 */
	public ReturnMsg updateWebMenu(String p_viewId, String p_desc, int p_seq, boolean p_removeMode, String p_newAccessRight){
		try{
			//validate viewid
			if (StringUtils.isBlank(p_viewId)) {
				return new ReturnMsg(false,"view id is blank");
			}
			
			//set pageid
			String pageId = p_viewId.replaceAll("^.*[.]", ""); //remove before dot prefix
			if (StringUtils.isBlank(pageId)) {
				return new ReturnMsg(false, String.format("invalid page id %s", pageId));
			}
			pageId = pageId + "_cust01";
			
			String url = String.format("zkbiloader.html?action=browse&viewid=%s&page_id=%s&zul=zkbiloader.zul", p_viewId, pageId);
			
			BiResult br = sh.newBiResult("WebMenu", true);
			br.clearCondition();
			br.appendWherecl(new Wherecl()
//					.andUniop("webm_menuid", "=", "menu_main.html")
					.andUniop("webm_menuid", "=", br.getSessionHelper().getRootMenu())
					.andUniop("webm_url", "=", url).stripAnd()
					);
    		br.query(true);
    		ReturnMsg rtn = null;
    		
    		//update webmenu old record if any
			if(br.getRowCount() > 0) {
				br.loadOneRecV(0);
				br.fetchOneRecV(0);
				if (p_removeMode) {
					Object o = br.getTrStatObj(0);
					br.markDelete(o, true);
					rtn = br.batchAddUpdateDelete();
					UniLog.log1("remove webmenu rtn:" + rtn);
				} else {
					br.getCell("webm_seq").set(p_seq);
					br.getCell("webm_desc").set(p_desc);
					br.getCell("webm_img").set("fa-book");
					rtn = br.updateCurrent();
					UniLog.log1("update webmenu rtn:" + rtn);
				}
				
			} 
			else if (!p_removeMode) {
				//add new webmenu record
//				br.getCell("webm_menuid").set("menu_main.html");
				br.getCell("webm_menuid").set(br.getSessionHelper().getRootMenu());
				br.getCell("webm_seq").set(p_seq);
				br.getCell("webm_desc").set(p_desc);
				br.getCell("webm_url").set(url);
				br.getCell("webm_img").set("fa-book");
				rtn = br.addCurrent();
				if (rtn == null) rtn = ReturnMsg.defaultOk;
				UniLog.log1("add webmenu rtn:" + rtn);
				if (rtn.getStatus() && StringUtils.isNotBlank(p_newAccessRight)) {
					int mrg = br.getCellInt("webm_rg");
					if (mrg > 0) {
						//add webmenuuser
						BiResult brUser = sh.newBiResult("WebMenuUser", true);
						brUser.getCell("webmu_mrg").set(mrg);
						//brUser.getCell("webmu_user").set("anyuser");
						brUser.getCell("webmu_user").set(p_newAccessRight);
						brUser.getCell("webmu_active").set("Y");
						rtn = brUser.addCurrent();
						UniLog.log1("add webmenuuser rtn:" + rtn);
					}
					else {
						UniLog.log1("mrg is 0, skip insert webmenuuser");
					}
				}
			}
    		
			//clear menu cache
			sh.clearSideMenuCache();
			
			if (rtn == null) rtn = ReturnMsg.defaultOk;
			return rtn;
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
			return new ReturnMsg(ex);
		}
	}

	
	public static void main(String args[]) throws Exception{

		DataModelHelper.normalizeTag("a1");
		DataModelHelper.normalizeTag("a'b");
		DataModelHelper.normalizeTag("Summary");
		DataModelHelper.normalizeTag("UFP Stats");
		DataModelHelper.normalizeTag("Webinar Stats");
		DataModelHelper.normalizeTag("Ad Stat");
		DataModelHelper.normalizeTag("Payment Record");
		//DataModelHelper.normalizeTag("Export (Don't edit)");
		DataModelHelper.normalizeTag("Import");
		DataModelHelper.normalizeTag("Dispute");
		DataModelHelper.normalizeTag("WFL Dispute");
		//DataModelHelper.normalizeTag("$0.5");
		DataModelHelper.normalizeTag("Crypto");
		DataModelHelper.normalizeTag("Subscriptions");
		DataModelHelper.normalizeTag("MFP");
		DataModelHelper.normalizeTag("WFC App");
		DataModelHelper.normalizeTag("MFP book");
		DataModelHelper.normalizeTag("Referral");
		DataModelHelper.normalizeTag("Refund-UFP");
		
		
		System.exit(0);
		
		testTranslateFormula();
		System.exit(0);
		
		
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("vincero","hlv",null);
		DataModelHelper dmh = new DataModelHelper(sh);
		dmh.selfTest();
		
		/*
		dmh.updateCol(sh.getBiView("BiTranslate").getColumnByLabel("serial_id"));
		dmh.updateCol(sh.getBiView("BiTranslate").getColumnByLabel("bitl_key"));
		*/
		DataModelHelper.normalizeTag("col_1");
		DataModelHelper.normalizeTag("col_1 a");
		DataModelHelper.normalizeTag("col_1 a");
		
		//BiView view = sh.getBiView("BiTranslate");
		
		UniLog.log1(""+dmh.updateWebMenu("vincero.a1", "a1 desc v2",0, false, "anyuser"));
		System.exit(0);
		
	}
	
	/***
	 * 	call external script to backup the db if possible
	 */
	private void backupDB() {
		try {
			UniLog.log1("called");
			File f = new File(BACKUP_CMD);
			if (!f.exists()) {
				UniLog.log1("backup cmd not exist, skip backup %s", BACKUP_CMD);
				return;
			}
			
			Process p = Runtime.getRuntime().exec("/usr/unc/bin/dmh_backupdb.sh");		
			p.waitFor();
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
			//ex.printStackTrace();
		}
	}
	
	/*
	static public String workSheetNameToViewName(SessionHelper p_sh,String p_sheetName) throws Exception {
		String tabName = DataModelHelper.normalizeTag(p_sheetName);
		return(p_sh.getDbName() + "." + tabName);
	}
	*/
	
}
