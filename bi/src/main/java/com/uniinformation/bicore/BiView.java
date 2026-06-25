package com.uniinformation.bicore;
import java.io.*;
import java.lang.reflect.Constructor;
import java.sql.SQLClientInfoException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.kyoko.parser.FunctionInterface;
import com.kyoko.parser.VariableInterface;
import com.uniinformation.cell.*;
import com.uniinformation.utils.whereclpar.*;
import com.uniinformation.webcore.SessionHelper;
/***
 * Logical structure:
 * BiView --- BiChain -- BiTable(primitive) -- BiField(primitive)
 *        \-- BiColumn -- BiField(primitive)
 *
 *
 */
public class BiView extends BiChain {
	class LinkProperty {
		boolean noExport;
		boolean noAdd;
		boolean noUpdate;
		boolean noDelete;
		boolean autoDelete;
		int maxRow;
		String accessKey;
		String addUpdateKey;
		boolean expand;
		boolean onDemand;
		boolean isVirtual;
		int seqStart= 0;
	}
	public class BiViewWhereclParser extends com.uniinformation.utils.whereclpar.Parser  
		implements VariableInterface,FunctionInterface{
		static public final int GETOBJECT_MODE_COLUMN= 0;
		static public final int GETOBJECT_MODE_FIELD = 1;
		static public final int GETOBJECT_MODE_TABLE = 2;
		static public final int GETOBJECT_MODE_DATABASE = 3;
		static public final int GETOBJECT_MODE_NAME = 4;
		static public final int GETOBJECT_MODE_OPTIONSELECT = 5;
		int parseMode = 0;
		BiResult br;
		public int getDataType(String p_varName) {
			int dataType=0;
			BiColumn c;
			c = getColumnByLabel(p_varName);
			if(c == null && p_varName.equals("serial_id")) {
				return(Cell.VTYPE_INT);
			}
			String ft;
			if(c.getField() != null ) ft = c.getField().getFieldType(); else ft = c.getColumnType();
			if(ft.equals("integer") || ft.equals("serial")) {
				if(c.getColumnType().equals("datetime") ||
				   c.getColumnType().equals("time")) {
					dataType = Cell.VTYPE_DATETIME;
				} else {
					dataType = Cell.VTYPE_INT;
				}
			}
			if(ft.equals("date")) dataType = Cell.VTYPE_DATE;
			if(ft.equals("float") || ft.equals("double") || ft.equals("money")) dataType = Cell.VTYPE_DOUBLE;
			if(ft.equals("char")) {
				if(c.isUTF8) {
					dataType = Cell.VTYPE_STRING | 0x100;
				} else {
					dataType = Cell.VTYPE_STRING;
				}
			}
			if(ft.equals("list")) dataType = Cell.VTYPE_STRING;
			if(ft.equals("combobox")) dataType = Cell.VTYPE_STRING;
			if(ft.equals("pickinput")) dataType = Cell.VTYPE_STRING;
			if(ft.equals("button")) dataType = Cell.VTYPE_STRING;
			if(ft.equals("checkbox")) dataType = Cell.VTYPE_BOOLEAN;
			return(dataType);
		}
		public String toString(String p_varName,int p_idx,boolean p_idxAbsolute) {
			BiColumn bc;
			if(p_varName.equals("serial_id")) {
				return(getTable().getSidField());
			}
			bc = getColumnByLabel(p_varName);
			switch(parseMode)  {
			case GETOBJECT_MODE_NAME:
				if(bc == null) 
					return(null);
				else
					return(bc.getEngName());
			case GETOBJECT_MODE_FIELD :
				if(bc == null) 
					return(null);
				else
					return(bc.getSelectName());
			case GETOBJECT_MODE_OPTIONSELECT: {
					if(p_varName.startsWith("__") && cellCollection != null) {
						Object eo = cellCollection.evalVariable(p_varName.substring(2));
						if(eo instanceof Double) return(""+eo);
						if(eo instanceof Integer) return(""+eo);
						if(eo instanceof java.util.Date) {
							String ss = DateUtil.dateToDateTimeStr(((java.util.Date) eo), "yy/mm/dd");
							return("'"+ss+"'");
						}
						if(eo instanceof Boolean) {
							if((Boolean) eo) {
								return("Y");
							} else {
								return("N");
							}
						}
						return("'"+eo+"'");
					}
				}
			case GETOBJECT_MODE_TABLE:
			case GETOBJECT_MODE_DATABASE:
			default :
				return(com.uniinformation.utils.whereclpar.Variable.toString(p_varName,p_idx));
			}
		}
		@Override
		public Object collectObject(String p_varName,int p_idx,boolean p_idxAbsolute) throws CellException {
			BiColumn bc;
			BiView bcView = BiView.this;
			if(p_varName.equals("serial_id")) {
					switch(parseMode)  {
					case GETOBJECT_MODE_FIELD :
						return(null);
					case GETOBJECT_MODE_TABLE:
						return(getTable());
					case GETOBJECT_MODE_DATABASE:
						return(bcView.getName()+"@"+bcView.getSchema().dbLabel.toString()); 
					case GETOBJECT_MODE_COLUMN:
						return(null);
					default :
						throw new CellException("Invalid getObject Mode");
					}
			}
			bc = getColumnByLabel(p_varName);
			if(bc == null) {
//				if(bc == null && p_varName.equals("serial_id")) {
//					switch(parseMode)  {
//					case GETOBJECT_MODE_FIELD :
//						return(null);
//					case GETOBJECT_MODE_TABLE:
//						return(getTable());
//					case GETOBJECT_MODE_DATABASE:
//						return(bcView.getName()+"@"+bcView.getSchema().dbLabel.toString()); 
//					case GETOBJECT_MODE_COLUMN:
//						return(null);
//					default :
//						throw new CellException("Invalid getObject Mode");
//					}
//				}
				for(BiView subV : links) {
					bc = subV.getColumnByLabel(p_varName);
					if(bc != null) {
						bcView = subV;
						break;
					}
				}

				if(bc == null) {
					throw new CellException("Variable Name : " + p_varName + " Invalid ");
				}
			}
			switch(parseMode)  {
			case GETOBJECT_MODE_FIELD :
					/* later should further parse the expression if the column is an stored expression */
					return(bc.getField());
			case GETOBJECT_MODE_TABLE:
					return(bc.getTable());
			case GETOBJECT_MODE_DATABASE:
				// eval conditon of store procedure in BI instead of database engine, may put the eval back to
				// database engine if supported, (currently not supported by scorpion
//					if(bc.getSelectName() != null) 
					if(bc.getField() != null) 
						return(bcView.getName()+"@"+bcView.getSchema().dbLabel.toString()); 
					else {
						if(bcView != BiView.this) {
							return(bcView.getName());
						} else return(null);
					}
			case GETOBJECT_MODE_COLUMN:
					return(bc);
			default :
					throw new CellException("Invalid getObject Mode");
			}
		}
		
		@Override
		public Cell evalVariable(String p_varName,int p_idx,boolean p_idxAbsolute,Object p_recData) throws CellException {
			if(p_recData instanceof CellCollection) {
				if(p_idx != 0) {
//					Cell cExpr = p_expr.eval(p_recData);
					return((CellCollection) p_recData).getCellArray(p_varName,p_idx);
				} else return((CellCollection) p_recData).getCell(p_varName);
			}
			return(null);
		}

		public String toString(String p_functName,List p_exprList) {
//			return(com.uniinformation.utils.whereclpar.Function.toString(p_functName,p_exprList));
			return(br.brEvalFunction(p_functName, p_exprList));
		}
//		public Object getObject(String p_varName,List p_exprList) throws CellException {
//			return(com.uniinformation.utils.whereclpar.Function.toString(p_varName,p_exprList));
//		}
		public Cell evalFunction(String p_varName,Vector p_exprList,Object p_recdata) throws CellException {
			if(p_recdata instanceof CellCollection) {
				CellCollection col = (CellCollection) p_recdata;
				Vector args = new Vector();
				for(int i=0;i<p_exprList.size();i++) {
					args.add(((Cell) p_exprList.get(i)).getObject());
				}
				try {
					Object o = col.evalFunction(p_varName, args);
					if(o != null) return(new Cell(o));
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			
			return(null);
		}
		BiViewWhereclParser (BiResult p_br) {
			super ( );
			br = p_br;
			setVarInterface(this);
			setFunctInterface(this);
		}
		
		public void setParseMode(int p_mode) {
			parseMode = p_mode;
		}
		@Override
		public Object collectObject(String p_functName, List p_args) throws CellException {
			// TODO Auto-generated method stub
			return null;
		}
		
		BiCellCollection cellCollection = null;
		public void setCollection(BiCellCollection p_col) {
			cellCollection = p_col;
		}
	}
	static public final String DEFAULT_ATTRIBUTE="NNYNNNNYY";
	Cell label;
	Cell id;
	Cell header;
	private Vector<BiView> links;
//	Vector<BiView> masters;
//	Vector exportLinks;
	/*
	Hashtable linkAttr;
	Hashtable linkMaxRow;
	*/
	Hashtable<BiView,LinkProperty> linkProperties;
	Hashtable orphan;
	String brClass=null;
	String fdUpdcnt=null;
	boolean noSearch = false;
	boolean noImport = true;
	boolean noExport = false;
	String wherecl = null;
	String joincl = null;
//	static int temptab_cnt = 0;
	final static boolean setConnectionOnDemand = true;
	AtomicBoolean hasLookupCol = null;
//	String ncStr = null;
	String seqColumn = null;
	String addAccess = null;
	String updAccess = null;
	String delAccess = null;
	String rptAccess = null;
	String impAccess = null;
	String detAccess = null;
	String batupdAccess = null;
	String classPath = null;
	boolean allowMasterDetail = false;
	private boolean isGroupBy = false;
	String customBiViewFname = null;
	Hashtable<BiView,ArrayList<BiColumn>> summaryViews = null;
	HashSet<BiView> autoAddUpdateViews = null;
	boolean useViewCache = false;
	boolean compareMasterWhenImport = false;
	boolean allowInsertBiTable = false;
	boolean allowUpdateBiTable = false;
	boolean allowMoveAndFreezeColumn = false;
	boolean allowUndo = false;
	boolean allowAdhocColumn = false;
	boolean alwaysInListView = false;
	boolean newBatchUpdate = false;
//	Vector<BiTable> lookupList = null;
	Hashtable<String,BiColumn> columnHash; /* add to increase the performance of getting BiColumn By Name */
	HashMap<String,BiColumn> columnHdrHash; //240206 key by hdr
	List<String> preferListOrders; //240516 by DT
	
	Hashtable<BiTable,HashSet<BiColumn>> autoSaveColumns = null;
	public BiView(CellCollection parent,String p_header,BiTable rootTable,String p_label,String p_BrClass,String p_attribute,String p_fdUpdcnt,String p_ncStr,String p_addaccess,String p_updaccess,String p_delaccess,String p_rptaccess,String p_impaccess, String p_detaccess,String p_condition,String p_batupdaccess,String p_classPath)
	{
		super(parent,rootTable);
		label= new Cell(p_label);
//		id = new Cell(p_id);
		header = new Cell(p_header);
		addCollectionList("orderby",new CellVector());
//		addCollectionList("wherecl",new CellVector());
		addCollectionList("columns",new CellVector());
		columnHash = new Hashtable<String,BiColumn>();
		columnHdrHash = new HashMap<String,BiColumn>();
		links = new Vector<BiView>();
		if(!StringUtils.isBlank(p_classPath)) {
			classPath = p_classPath;
		}
//		masters = new Vector<BiView>();
//		exportLinks = new Vector();
		/*
		linkAttr = new Hashtable();
		linkMaxRow = new Hashtable();
		*/
		linkProperties = new Hashtable<BiView,LinkProperty>();
		orphan = new Hashtable();
		Hashtable columnsHash;
		fdUpdcnt=p_fdUpdcnt;
		if(p_BrClass != null && !p_BrClass.trim().equals("")) {
				brClass = p_BrClass.trim();
		}
		if(StringUtil.strpart(p_attribute, 0, 1).equals("Y")) noSearch = true;
		if(StringUtil.strpart(p_attribute, 1, 1).equals("Y")) noExport = true;
		if(StringUtil.strpart(p_attribute, 2, 1).equals("Y")) noImport = false;
		if(StringUtil.strpart(p_attribute, 3, 1).equals("Y")) allowMasterDetail = true;
		if(StringUtil.strpart(p_attribute, 4, 1).equals("Y")) isGroupBy = true;
		if(StringUtil.strpart(p_attribute, 5, 1).equals("Y")) useViewCache = true;
		if(StringUtil.strpart(p_attribute, 6, 1).equals("Y")) compareMasterWhenImport = true;
		if(StringUtil.strpart(p_attribute, 7, 1).equals("Y")) allowInsertBiTable = true;
		if(StringUtil.strpart(p_attribute, 8, 1).equals("Y")) allowUpdateBiTable = true;
		if(StringUtil.strpart(p_attribute, 9, 1).equals("Y")) allowMoveAndFreezeColumn = true;
		if(StringUtil.strpart(p_attribute, 10, 1).equals("Y")) allowUndo = true;
		if(StringUtil.strpart(p_attribute, 11, 1).equals("Y")) allowAdhocColumn = true;
		if(StringUtil.strpart(p_attribute, 12, 1).equals("Y")) alwaysInListView = true;
		if(StringUtil.strpart(p_attribute, 13, 1).equals("Y")) {
			newBatchUpdate = true;
		}
//		ncStr = p_ncStr;
		if(p_addaccess != null && !p_addaccess.trim().equals("")) addAccess = p_addaccess;
		if(p_updaccess != null && !p_updaccess.trim().equals("")) updAccess = p_updaccess;
		if(p_delaccess != null && !p_delaccess.trim().equals("")) delAccess = p_delaccess;
		if(p_rptaccess != null && !p_rptaccess.trim().equals("")) rptAccess = p_rptaccess;
		if(p_impaccess != null && !p_impaccess.trim().equals("")) impAccess = p_impaccess;
		if(p_detaccess != null && !p_detaccess.trim().equals("")) detAccess = p_detaccess;
		if(p_condition != null && !p_condition.trim().equals("")) wherecl = p_condition;
		if(p_batupdaccess != null && !p_batupdaccess.trim().equals("")) batupdAccess = p_batupdaccess;
	}

	public BiColumn addColumn(String p_label,int p_idx,BiField p_field, String p_header,boolean p_inlist,boolean p_inselect,String p_formula,String p_attribute,String p_format,boolean p_noquery,boolean p_invisible,int p_rgno,String p_fdtype,int p_fdlen,String p_accesskey,String p_optionlist,String p_default,String p_pickView,int p_fgcolor,int p_bgcolor,String p_validation,String p_aggregate)	
	{
		//UniLog.log("BiView.addColumn " + label.getString() + " : " + p_label);
		BiColumn column = new BiColumn(this,p_label,p_field,p_header,p_inlist,p_inselect,p_formula,p_attribute,p_format,p_noquery,p_invisible,p_rgno,p_fdtype,p_fdlen,p_accesskey,p_optionlist,p_default,p_pickView,p_fgcolor,p_bgcolor,p_validation,p_aggregate);
		Vector v = getCollectionList("columns");
		// p_idx start from 1 instead of 0 
		if(p_idx > 0 && p_idx <= v.size()) {
				v.add(p_idx-1,column);
		} else {
				v.add(column);
		}
		columnHash.put(p_label, column);
		columnHdrHash.put(p_header, column);
		return(column);	
	}
	
	public HashMap<String,BiColumn> buildColumnHM(){
		HashMap<String,BiColumn> colHM = new HashMap();
		for (BiColumn col : getCollectionList("columns",BiColumn.class)) {
			if (StringUtils.isNotBlank(col.getLabel())) {
				colHM.put(col.getLabel(), col);
			}
		}
		return (colHM);
	}
	
	
	
	private void setCustomBiViewFname(String p_customBiViewBase) {
		customBiViewFname = null; //set a default value first. fix BiView construct from xml
		if (StringUtils.isBlank(p_customBiViewBase)) {
			return;
		}
		if (label == null || StringUtils.isBlank(label.getString())) {
			return;
		}
		try {
			/*
			if (!new File(customBiViewFname).exists()) {
				//UniLog.log1("file not found %s",customBiViewFname);
				return null;
			}
			*/
			
			
			
			
			customBiViewFname =  (p_customBiViewBase + "/" + label.getString()).replaceAll("\\s+", "").replaceAll("\\.", "/") +".json";
		}
		catch(Exception ex) {
			UniLog.log1("update fail:%s customBiViewBase:%s customBiViewFname:%s",ex.getMessage(), p_customBiViewBase, customBiViewFname);
			ex.printStackTrace();
		}
	}
	

	private void setCustomBiViewFnameByList(List<String >p_customBiViewBaseList) {
		customBiViewFname = null; //set a default value first. fix BiView construct from xml
		if (label == null || StringUtils.isBlank(label.getString())) {
			return;
		}
		try {
			for(String ss : p_customBiViewBaseList) {
				String tname =  (ss + "/" + label.getString()).replaceAll("\\s+", "").replaceAll("\\.", "/") +".json";
				if(new File(tname).exists()) {
					customBiViewFname =  tname;
					break;
				}
			}
		}
		catch(Exception ex) {
			UniLog.log1("update fail:%s customBiViewBase:%s customBiViewFname:%s",ex.getMessage(), p_customBiViewBaseList, customBiViewFname);
			ex.printStackTrace();
		}
	}
	/***
	 * entry point for setup custom biviews
	 * @param p_schema
	 */
	public static void setupCustomBiViews(BiSchema p_schema, String p_customBiViewBase) {
		if (p_schema == null) {
			UniLog.log1("schema is null, ignore");
			return;
		}
		try {
			UniLog.log1("setup custom biview base:%s",p_customBiViewBase);
			if(p_schema.getCollection("views").getCollections(BiView.class) != null) {
				
				if(p_customBiViewBase != null && p_customBiViewBase.contains(",")) {
					Vector<String>cbList =  StringUtil.getElementsByDelimiter(p_customBiViewBase,",");
					for(BiView tmpView : p_schema.getCollection("views").getCollections(BiView.class)) {
						tmpView.setCustomBiViewFnameByList(cbList);
						tmpView.setupCustomBiView();
						//tmpView.saveCustomBiView(); //dump all biview to json (for dev only)
					}
				} else {
					for(BiView tmpView : p_schema.getCollection("views").getCollections(BiView.class)) {
						if(tmpView.getName().equals("erpv4.CustomerG2")) {
							int cc;
							cc = 0;
						}
						tmpView.setCustomBiViewFname(p_customBiViewBase);
						tmpView.setupCustomBiView();
						//tmpView.saveCustomBiView(); //dump all biview to json (for dev only)
					}
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/***
	 * setup one custom biview
	 * call it after addColumn()
	 */
	private void setupCustomBiView() {
		try {
			//UniLog.log("setupCustomBiView:"+getName() + " : " + customBiViewFname);
			if (StringUtils.isBlank(customBiViewFname)) {
				UniLog.log1("%s customBiViewFname is not defined", getName());
				return;
			}
	
			HashMap<String,BiColumn> colHM = buildColumnHM();

			JsonArray ja = readCustomBiViewJA();
			if (ja == null) {
				//UniLog.log1("readCustomBiView:%s:%s not exist",getName(),customBiViewFname);
				return;
			}
			UniLog.log1("readCustomBiView:%s:%s exist",getName(),customBiViewFname);
			for (JsonElement je : ja) {
				if (!je.isJsonObject()) continue;
				JsonObject json = je.getAsJsonObject();
				if (json.get("label") == null) {
					UniLog.log1("ignore. label is null");
					continue;
				}
				String label = json.get("label").getAsString();
				if(label.equals("listOrder")) {
					JsonArray ja2 = json.getAsJsonArray("colLabels");
					for (JsonElement je2 : ja2) {
						String colLabel = je2.getAsString();
						UniLog.log(colLabel);
						if(preferListOrders == null) {
							preferListOrders = new ArrayList<String>();
						}
						preferListOrders.add(colLabel);
					}
					continue;
				}
				if (StringUtils.isBlank(label)) {
					UniLog.log1("ignore. label is blank");
					continue;
				}
				BiColumn col = colHM.get(label);
				if (col == null) {
					UniLog.log1("ignore %s. column not found", label);
					continue;
				}
				col.update(json);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public Vector<BiColumn> getColumns()
	{
		return(getCollectionList("columns"));
	}
	
	public BiColumn getColumnByLabel(String p_label) {
		/*
		Vector <BiColumn> bv = (getCollectionList("columns"));
		for(BiColumn bc : bv) {
			if(bc.getLabel().equals(p_label)) return(bc);
		}
		return(null);"
		*/
		return(columnHash.get(p_label));
	}
	public BiColumn getColumnByHdr(String p_hdr) {
		return(columnHdrHash.get(p_hdr));
	}
/*	
	public Vector getColumns()
	{
		final Vector v = new Vector();
		try {
			traverseCollection(true,
				new TraverseInterface() {
					public void traverseOne(CellCollection p_col) throws CellException
					{
						if(p_col instanceof BiChain) {	
							BiChain chain = (BiChain) p_col;
							Enumeration e = chain.getCollection("columns").getCollectionKeys();
//							for(Enumeration e = chain.columns.getCollectionKeys();e.hasMoreElements();) {
							if(e != null) {
							for(;e.hasMoreElements();) {
								BiColumn column = (BiColumn) chain.getCollection("columns").getCollection((String) e.nextElement());
								v.add(column);
							}
							}
						}
					}
				}
			);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
		Collections.sort(v,
				new Comparator() {
					public int compare (Object p_a,Object p_b) {
						int c1 = ((BiColumn) p_a).getIdx();
						int c2 = ((BiColumn) p_b).getIdx();
						return(c1 > c2 ? 1 : (c2 > c1 ?  -1 : 0));
					}
				}
			);
		return(v);
	}
*/
	/*
	public BiResult newBiResult(String loginId,BiResult p_parent) 
	{
		return(newBiResult(loginId,p_parent,null,null));
	}
	*/
	/*
	public BiResult newBiResult(String loginId,BiResult p_parent,String biClass) {
		return(newBiResult(loginId,p_parent,biClass,null));
	}
	*/
	
	public BiResult newBiResult(String loginId,final BiResult p_parent,String biClass, SessionHelper p_sh)  {
		return(newBiResult(null,loginId,p_parent,biClass, p_sh));
	}
	
	public BiResult newBiResult(SelectUtil p_su,String loginId,final BiResult p_parent,String biClass, SessionHelper p_sh) {
		return newBiResult(p_su,loginId,p_parent,biClass, p_sh, true);
	}
	BiResult newBiResult(SelectUtil p_su,String loginId,final BiResult p_parent,String biClass, SessionHelper p_sh, boolean p_allowLookupItemList) {
		//if no su provided, use parent su; if no parent, create su.
		if(summaryViews == null) {
			summaryViews = new Hashtable<BiView,ArrayList<BiColumn>>();
			for(BiColumn bc : getColumns()) {
				if(bc.isStoredFunction() && bc.pickView != null) {
					BiView sv = getSchema().getViewByName(bc.getPickViewName());
					ArrayList<BiColumn> sc = summaryViews.get(sv);
					if(sc == null) {
						sc = new ArrayList<BiColumn>();
						summaryViews.put(sv,sc);
					}
					sc.add(bc);
				}
			}
			
		}
		if(autoAddUpdateViews == null) {
			autoAddUpdateViews = new HashSet<BiView>();
			for(BiColumn bc : getColumns()) {
				if(bc.isAutoAddUpdate () && bc.pickView != null) {
					BiView sv = getSchema().getViewByName(bc.getPickViewName());
					if(sv != null) autoAddUpdateViews.add(sv);
				}
			}
			
		}
		if(autoSaveColumns == null) {
			autoSaveColumns = new Hashtable<BiTable,HashSet<BiColumn>> ();
			for(BiColumn bc : getColumns()) {
				if(bc.isAutoSave() && bc.getField() != null) {
					BiTable bt = bc.getField().getTable();
					if(bt != getTable()) {
						BiChain ch = findChain(bt);
						BiTable master = ch.getMaster();
						BiJoin bj = bt.getJoin(master);
						if(bj != null && !bj.isOneToOne()) {
							HashSet<BiColumn> hs = autoSaveColumns.get(bt);
							if(hs == null) {
								hs = new HashSet<BiColumn>();
								autoSaveColumns.put(bt, hs);
							}
							hs.add(bc);
						}
						
					}
				}
			}
		}
		SelectUtil su = p_su;
		if(p_su == null) {
			if(p_parent != null) {
				su = p_parent.su;
			} else {
				su = new SelectUtil();
				if(p_sh != null && p_sh.isDbAllowNull()) {
					su.setAllowNull(true);
				}
				su.setLoginId(loginId);
			}
		}
		
		try {
			StopWatchHelper sw = new StopWatchHelper(1000,"newBiResult:" + label.getString());
			final StringBuffer whereStr = new StringBuffer();
			if(joincl != null) {
				whereStr.append(joincl);
			}
			final Vector tabList = new Vector();
			final Vector<BiTable> tmpLookupList = new Vector<BiTable>();
			traverseCollection(true,
				new TraverseInterface() {
					public boolean traverseOne(CellCollection p_col) throws CellException
					{
						if(p_col instanceof BiChain) {	
							BiChain chain = (BiChain) p_col;
							if(p_parent != null && p_parent.getView().getTable() == chain.getTable() ) {
								UniLog.log("Skip View Chain this is in parent view");
								return(false);
							}
							BiTable master = chain.getMaster();
							BiTable lookup = chain.getTable();
//							UniLog.log("query traverse Chain table: " + lookup.getName() + " master: " + (master == null ? "null":master.getName()));
//							selStr.append(","+lookup.getName()+".serial_id " + lookup.getName() + "_sid");
							String s = lookup.getWherecl();
							if(s != null && !s.trim().equals("")) whereStr.append(" and " + s);
							if(master == null) tabList.add(lookup);
//							fromStr.append(","+lookup.getName());
							if(master != null) {
								BiJoin join = null;
								join = lookup.getJoin(master.getName());								
								UniLog.log("master: " + master.getName() + " join " + join);
								if(join == null) {
									if(allowMasterDetail) {
										join = master.getJoin(lookup.getName());		
										if(join != null) {
											UniLog.log("AllowMasterUpdate is true use reverse join" + join);
											chain.reverseJoin = true;
											chain.optional = true; // reverse join should always be optional
										}
									}
								}
								if(join == null) {
									throw(new CellException("get Join got null master "+ master.getName() + " -> " + lookup.getName()));
								} else {
									if(chain.isAllOneOne()) {
										tabList.add(lookup);
										UniLog.log("HAHA tablist size = " + tabList.size());
									} else {
										tmpLookupList.add(lookup);
									}
									/*
									if(join.isOneToOne()) {
										tabList.add(lookup);
										UniLog.log("HAHA tablist size = " + tabList.size());
									}
									*/
									if(getSchema().getSqlEngine() == BiSchema.SQLENGINE_SCORPION) {
										whereStr.append(" and " );
										whereStr.append(join.getPredicate());
									}
									if(join.getBaseCondition() != null) {
										whereStr.append(" and " );
										whereStr.append(join.getBaseCondition());
									}
									if(join.isOptional()) {
											//UniLog.log("HAHA 20170906 set Bichain " + chain.getTable().getName());
											chain.optional = true;
									}
								}
							}
						}
						return(true);
					}
				}
			);
			UniLog.log("where:"+whereStr.toString());
//			if(lookupList == null) lookupList = tmpLookupList;
//
//			Vector vWherecl = getWhereCl();
//			for(int i = 0;i< vWherecl.size();i++) {
//				BiWhereCl w = (BiWhereCl) vWherecl.get(i);
//				UniLog.log("View Condition " + w.toString());
//				if(!w.toString().equals("")) whereStr.append(" and " + w.toString());
//			}
//			
			String wStr;
			if(!whereStr.toString().trim().equals("")) wStr = " where " + Wherecl.stripAnd(whereStr.toString()) ; else wStr = "";
			UniLog.log("setConnectionOnDemand:" + setConnectionOnDemand);
			if(p_su == null) {
				if (setConnectionOnDemand){
					su.setGetConnectionInterface(getConnInterface());
				}
				else{
					su.init(getConn());
				}
			}
			BiResult result;
			String curBrClass = biClass;
			if(curBrClass == null) {
				curBrClass = brClass;
			}
			if(curBrClass != null) {
				UniLog.logm(this,"overridding BrResult to " + curBrClass);
				Class biResultClass = Class.forName(curBrClass);
				Class[] paramTypes = null;
				Constructor constructor = null;
				
				//get constructor 1
				try {
					paramTypes = new Class[]{BiResult.class,BiView.class,SelectUtil.class,Vector.class,String.class,SessionHelper.class,boolean.class};
					constructor = biResultClass.getConstructor(paramTypes);
					result = (BiResult) constructor.newInstance(p_parent,this,su,tabList,Wherecl.stripAnd(whereStr.toString()),p_sh,p_allowLookupItemList);
					result.setLookupList(tmpLookupList);
					sw.stop();
					return(result);
				}
				catch(NoSuchMethodException ex) {}
				
				//get constructor (for backward compatibility)
				try {
					paramTypes = new Class[]{BiResult.class,BiView.class,SelectUtil.class,Vector.class,String.class,SessionHelper.class};
					constructor = biResultClass.getConstructor(paramTypes);
					result = (BiResult) constructor.newInstance(p_parent,this,su,tabList,Wherecl.stripAnd(whereStr.toString()),p_sh);
					result.setLookupList(tmpLookupList);
					sw.stop();
					return(result);
				}
				catch(NoSuchMethodException ex) {}
				throw new Exception("get constructor failed " + curBrClass);
			} else {
				if(p_parent == null) {
					result = new BiResult(null,this,su,tabList,Wherecl.stripAnd(whereStr.toString()),p_sh,p_allowLookupItemList);
					result.setLookupList(tmpLookupList);
				} else {
					result = new BiResult(p_parent,this,su,tabList,Wherecl.stripAnd(whereStr.toString()),p_sh,p_allowLookupItemList);
					result.setLookupList(tmpLookupList);
				}
			}
			sw.stop();
			return(result);
		}
		catch (Exception ex) {
			UniLog.log(ex);
			if(p_parent == null && p_su == null && su != null) su.close();
		}
		return(null);
	}
	public String getName() {
		if (label == null) {
			return "";
		}
		return(label.getString());
	}
	
	void addOrderBy(BiColumn p_column,boolean p_desc)
	{
		Vector v = getCollectionList("orderby");
		v.add(new BiOrderBy(this,p_column,p_desc));
	}
	public void addWhereCl(String p_table,String p_field,String p_operator, String p_value)
	{
		/*
		if(wherecl != null) wherecl += " and "; else wherecl = "";
		if(p_operator != null && !p_operator.trim().equals("")) {
			wherecl += p_field + " " + p_operator + " " + p_wherecl;
		} else {
			wherecl += p_wherecl;
		}
		*/
		if(joincl != null) joincl += " and "; else joincl = "";
		if(p_operator != null && !p_operator.trim().equals("")) {
			if(!StringUtils.isBlank(p_table)) joincl += p_table+".";
			joincl += p_field + " " + p_operator + " "+p_value.trim();
		}
		
//		Vector v = getCollectionList("wherecl");
//		v.add(new BiWhereCl(this,p_table,p_field,p_operator,p_wherecl));
	}
	public String getWhereCl()
	{
//		return(getCollectionList("wherecl"));
		return(wherecl);
	}
	
	public void addLinkView(BiView p_subView,String p_attribute,int p_maxrow,String p_accesskey){
		links.add(p_subView);
		//UniLog.log("View " + p_subView.label.getString() +" linked to " + label.getString());
		/*
		if(!StringUtil.strpart(p_attribute, 0, 1).equals("Y")) {
			exportLinks.add(p_subView);
		}
		*/
		/*
		linkAttr.put(p_subView, p_attribute);
		if(p_maxrow > 0 ) {
			linkMaxRow.put(p_subView,p_maxrow);
		}
		*/
		LinkProperty lp = new LinkProperty();
		lp.maxRow = p_maxrow;
		lp.noExport = StringUtil.strpart(p_attribute, 0, 1).equals("Y");
		lp.noAdd = StringUtil.strpart(p_attribute, 1, 1).equals("Y");
		lp.noUpdate = StringUtil.strpart(p_attribute, 2, 1).equals("Y");
		lp.noDelete = StringUtil.strpart(p_attribute, 3, 1).equals("Y");
		lp.autoDelete = StringUtil.strpart(p_attribute, 4, 1).equals("Y");
		lp.expand = StringUtil.strpart(p_attribute, 6, 1).equals("Y");
		lp.onDemand = StringUtil.strpart(p_attribute, 7, 1).equals("Y");
		lp.isVirtual = StringUtil.strpart(p_attribute, 8, 1).equals("Y");

		if(StringUtil.strpart(p_attribute, 9, 1).equals("Y")) {
			lp.seqStart = 1;
		}
		
		if(p_accesskey != null && !p_accesskey.trim().equals("")) {
			String keys[] = p_accesskey.split(",");
			if(keys.length > 0) {
				if(!keys[0].trim().equals("")) lp.accessKey = keys[0].trim();
			}
			if(keys.length > 1) {
				if(!keys[1].trim().equals("")) lp.addUpdateKey = keys[1].trim();
			}
		}
		linkProperties.put(p_subView, lp);
	}
//	public void addMasterView(BiView p_masterView){
//		masters.add(p_masterView);
//		//UniLog.log("View " + p_subView.label.getString() +" linked to " + label.getString());
//		/*
//		if(!StringUtil.strpart(p_attribute, 0, 1).equals("Y")) {
//			exportLinks.add(p_subView);
//		}
//		*/
//	}
	/***
	 * obtain link view list, the relation of view
	 * andrew190508: expose links for construct translation
	 * @return
	 */
	Vector<BiView> getLinkViews(){
		return(links);
	}
	
	public Vector getDefaultOrderBy()
	{
			return(getCollectionList("orderby"));
	}
	
	public void addOneChain(BiTable p_master,BiTable p_child) {
		BiChain pchain = findChain(p_master);
		BiChain nchain = null;
		Vector v;
		if(pchain != null) {
			nchain = pchain.addChain(p_child);
		} else {
			for(Iterator it = orphan.values().iterator();it.hasNext();) {
				Vector ov = (Vector) it.next();
				for(int i = 0;i<ov.size();i++) {
					pchain = ((BiChain) ov.get(i)).findChain(p_master);
					if(pchain != null) break;
				}
				if(pchain != null) break;
			}
			if(pchain != null) {
				nchain = pchain.addChain(p_child);
			} else {
				nchain = new BiChain(this,p_child);
				v = (Vector) orphan.get(p_master);
				if(v == null) {
					v = new Vector();
					orphan.put(p_master,v);
				}
				v.add(nchain);
				if (BiSchema.debugFlag.get()) UniLog.log("view " + getName() + " add one orphan for " + p_master.getName() + " total " + orphan.size());
			}
		}
		v = (Vector) orphan.get(p_child);
		if(v != null) {
			for(int i = 0;i<v.size();i++) {
				pchain = (BiChain) v.get(i);
				pchain.setParent(nchain);
				nchain.getCollection("subChains").addCollection(pchain.getTable().getName(),pchain);
			}
			orphan.remove(p_child);
			if (BiSchema.debugFlag.get()) UniLog.log("view " + getName() + "reduce one orphan " + p_child.getName() + " remain " + orphan.size());
		}
	}
	public BiTable getMaster()
	{
		return(null);
	}
	
	public String getHeader()
	{
		return(header.getString());
	}
	
	public boolean linkDeleteByMaster(BiView p_view) {
		
		/*
		if(links == null || links.indexOf(p_view) < 0) return(false);
		String attr = (String) linkAttr.get(p_view);
		if(attr == null) return(false);
		if(StringUtil.strpart(attr, 4, 1).equals("Y")) return(true);
		return(false);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return lp.autoDelete;
	}
	public int linkGetSeqStart(BiView p_view) {
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(0);
		return (lp.seqStart);
	}
	public boolean linkNoAddUpDate(BiView p_view) {
		/*
		if(links == null || links.indexOf(p_view) < 0) return(false);
		String attr = (String) linkAttr.get(p_view);
		if(attr == null) return(false);
		if(StringUtil.strpart(attr, 1, 3).equals("YYY")) return(true);
		return(false);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return (lp.noAdd && lp.noUpdate);
	}
	public boolean linkNoAddUpDateDelete(BiView p_view) {
		/*
		if(links == null || links.indexOf(p_view) < 0) return(false);
		String attr = (String) linkAttr.get(p_view);
		if(attr == null) return(false);
		if(StringUtil.strpart(attr, 1, 3).equals("YYY")) return(true);
		return(false);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return (lp.noAdd && lp.noUpdate && lp.noDelete);
	}
	public boolean inExportList(BiView p_view) {
		/*
		if(links == null || links.indexOf(p_view) < 0) return(false);
		String attr = (String) linkAttr.get(p_view);
		if(attr == null) return(true);
		if(StringUtil.strpart(attr, 0, 1).equals("Y")) return(false);
		return(true);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return (!lp.noExport);
	}
	public boolean linkAllowAdd(BiView p_view,SessionHelper sh) {
		/*
		if(links == null || links.indexOf(p_view) < 0) return(false);
		String attr = (String) linkAttr.get(p_view);
		if(attr == null) return(true);
		if(StringUtil.strpart(attr, 1, 1).equals("Y")) return(false);
		return(true);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		String addUpdateKey = linkAddUpdateKey(p_view);
		if(addUpdateKey != null) {
			if(!BiSchema.hasAccessRight(sh, addUpdateKey)) return(false);
		}
		return (!lp.noAdd);
	}
	public boolean linkAllowUpdate(BiView p_view,SessionHelper sh) {
		/*
		if(links == null || links.indexOf(p_view) < 0) return(false);
		String attr = (String) linkAttr.get(p_view);
		if(attr == null) return(true);
		if(StringUtil.strpart(attr, 2, 1).equals("Y")) return(false);
		return(true);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return (!lp.noUpdate);
	}
	public boolean linkAllowRemove(BiView p_view,SessionHelper sh) {
		/*
		if(links == null || links.indexOf(p_view) < 0) return(false);
		String attr = (String) linkAttr.get(p_view);
		if(attr == null) return(true);
		if(StringUtil.strpart(attr, 3, 1).equals("Y")) return(false);
		return(true);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		String addUpdateKey = linkAddUpdateKey(p_view);
		if(addUpdateKey != null) {
			if(!BiSchema.hasAccessRight(sh, addUpdateKey)) return(false);
		}
		return (!lp.noDelete);
	}
//	public boolean linkShowHeader(BiView p_view) {
//		if(links == null || links.indexOf(p_view) < 0) return(false);
//		String attr = (String) linkAttr.get(p_view);
//		if(attr == null) return(false);
//		if(StringUtil.strpart(attr, 5, 1).equals("Y")) return(true);
//		return(false);
//	}
	public int linkMaxRow(BiView p_view) {
		/*
		if(links == null || links.indexOf(p_view) < 0) return(0);
		Integer maxRow = (Integer) linkMaxRow.get(p_view);
		if(maxRow == null) return(0); else return(maxRow);
		*/
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(0);
		return (lp.maxRow);
	}
	
	public boolean isNoSearch()
	{
		return(noSearch);
	}
	public boolean isNoExport(SessionHelper p_sh)
	{
		if(noExport) return(noExport);
		if(impAccess == null || p_sh.isAdminUser() || BiSchema.hasAccessRight(p_sh, impAccess)) return(false);
		return(true);
	}
	public boolean isNoImport(SessionHelper p_sh)
	{
		if(noImport) return(noImport);
		if(impAccess == null || p_sh.isAdminUser() || BiSchema.hasAccessRight(p_sh, impAccess)) return(false);
		return(true);
	}
	public String toString(){
		return(getName());
	}
	synchronized public boolean hasLookupCol(){
		try{
			if (hasLookupCol != null){
//				UniLog.logm(this,"%s return %s(cached)", this.toString(),hasLookupCol.get());
				return(hasLookupCol.get());
			}
			for (BiColumn curCol: getColumns()){
				if (curCol.isLookup()){
					hasLookupCol = new AtomicBoolean(true);
//					UniLog.logm(this,"%s return %s(new)", this.toString(),hasLookupCol.get());
					return(hasLookupCol.get());
				}
			}
			hasLookupCol = new AtomicBoolean(false);
//			UniLog.logm(this,"%s return %s(new)", this.toString(),hasLookupCol.get());
			return(hasLookupCol.get());
		}
		catch(Exception ex){
			ex.printStackTrace();
			return(false);
		}
	}
	
	public String getFdUpdcnt()  {
		return(fdUpdcnt);
	}
	
	public BiViewWhereclParser getWhereclParser(BiResult p_br)  
	{
		return(new BiViewWhereclParser(p_br));
	}
	
//	public int getNotifyCount(String p_loginId){ //REMARK: this call will be obsolete and replaced by preset search query logic
//		int nc = 0;
//		if (StringUtils.isBlank(ncStr)){
//			return(-1);
//		}
//		BiResult biResult = newBiResult(p_loginId,null,null,null);
//		biResult.addCustomCondition(ncStr);
//		nc = biResult.getQueryRecCount();
//		/*
//		SelectUtil su = null;
//		try{
//			su = new SelectUtil();
//			su.setGetConnectionInterface(getConnInterface());
//			TableRec tr = su.getQueryResult(ncStr,null);
//			if (tr.size() >= 0){
//				tr.setRecPointer(0);
//				nc = ((Integer)tr.getField(0)).intValue();
//			}
//		}
//		catch(Exception ex){
//			ex.printStackTrace();
//		}
//		finally{
//			if (su != null){
//				try{ su.close(); } catch(Exception ex){ }
//			}
//		}
//		*/
//		UniLog.logm(this,"view:%s label:%s ncStr:%s nc:%d",getName(),label,ncStr,nc);
//		return(nc);
//	}
	
	public void setSeqColumn(String p_col) {
		seqColumn = p_col;
	}
	public String getSeqColumn() {
		return(seqColumn);
	}
	public boolean allowAdd(SessionHelper p_sh) {
		/* default true */
		if(addAccess == null || p_sh.isAdminUser() || BiSchema.hasAccessRight(p_sh, addAccess)) return(true);
		else return(false);
	}
	public boolean allowUpdate(SessionHelper p_sh) {
		/* default true */
		if(updAccess == null || p_sh.isAdminUser() || BiSchema.hasAccessRight(p_sh, updAccess)) return(true);
		else return(false);
	}
	public boolean allowReport(SessionHelper p_sh) {
		/* default true */
		if(rptAccess == null || p_sh.isAdminUser() || BiSchema.hasAccessRight(p_sh, rptAccess)) return(true);
		else return(false);
	}
	public boolean allowDelete(SessionHelper p_sh) {
		/* default false */
		if(p_sh.isAdminUser() || (delAccess != null && BiSchema.hasAccessRight(p_sh, delAccess))) return(true);
		else return(false);
	}
	public boolean allowDetail(SessionHelper p_sh) {
		if(detAccess == null || p_sh.isAdminUser() || BiSchema.hasAccessRight(p_sh, detAccess)) return(true);
		else return(false);
	}
	public boolean allowBatchUpdate(SessionHelper p_sh) {
		if(p_sh.isAdminUser() || (batupdAccess != null && BiSchema.hasAccessRight(p_sh, batupdAccess))) return(true);
		else return(false);
	}
	/***
	 * get json from json file
	 * @param p_biView
	 * @return
	 */
	private JsonArray readCustomBiViewJA() {
		if (StringUtils.isBlank(customBiViewFname)) {
			return null;
		}
		if (!new File(customBiViewFname).exists()) {
			//UniLog.log1("file not found %s",customBiViewFname);
			return null;
		}
		UniLog.log1("load customerview %s",customBiViewFname);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(customBiViewFname));
			JsonParser parser = new JsonParser();
			JsonArray array = parser.parse(br).getAsJsonArray();
			br.close();
			return array;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/***
	 * save customBiView to json file
	 * @return true/false
	 */
	public boolean saveCustomBiView(boolean p_dumpAllFields) {
		if (StringUtils.isBlank(customBiViewFname)) {
			UniLog.log1("view or columns or fname is null");
			return false;
		}
		if (!p_dumpAllFields) {
			UniLog.log1("only supply dump all fields (dev in progress)");
			return false;
		}
		Vector<BiColumn> biColumns = getCollectionList("columns",BiColumn.class);
		if (biColumns == null) {
			UniLog.log("biColumns is null");
			return false;
		}
		
		try {
			//make folders
			String dirName = "/" + FilenameUtils.getPath(customBiViewFname);
			//UniLog.log1("%s,%s",customBiViewFname,dirName);
			new File(dirName).mkdirs();
			
			//save data to file
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
			FileWriter fw = new FileWriter(customBiViewFname);
			gson.toJson(biColumns, fw);
			fw.close();
			
			return true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	public static void main(String args[]) throws Exception {
		UniLog.log1("HAHA:%s", new Gson().toJson(MapUtil.of("hello1","world1","aaa",111)));
		new Gson().toJson(MapUtil.of("hello1","world1","aaa",111), new FileWriter("/tmp/a1.json"));
		new GsonBuilder().create().toJson(MapUtil.of("hello1","world1","aaa",111), new FileWriter("/tmp/a2.json"));
	}
	
	List<BiColumn> getPickList() {
		Vector<BiColumn> v = new Vector<BiColumn>();
		for(BiColumn bc : getColumns()) {
			if(bc.inPickList()) {
				v.add(bc);
			}
		}
		return(v);
	}

	public boolean isGroupBy() {
		return(isGroupBy);
	}
	
	Hashtable<BiView,ArrayList<BiColumn>> getSummaryViews() {
		return(summaryViews);
	}
//	Vector<BiTable>getLookupList() {
//		return(lookupList);
//	}
	
	public boolean useViewCache() {
		return(useViewCache);
	}
	public boolean compareMasterWhenImport() {
		return(compareMasterWhenImport);
	}
	public boolean allowMoveAndFreezeColumn() {
		return(allowMoveAndFreezeColumn);
	}
	/*
	List<BiColumn> getSummaryList() {
		ArrayList<BiColumn> l = new ArrayList<BiColumn>();
		for(BiColumn bc : getColumns()) {
			if(bc.isSummaryFunction()) {
				l.add(bc);
			}
		}
		return(l);
	}
	*/
	public boolean linkAutoExpand(BiView p_view) {
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return lp.expand;
	}

	public boolean linkOnDemand(BiView p_view) {
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return lp.onDemand;
	}

	public boolean linkIsVirtual(BiView p_view) {
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(false);
		return lp.isVirtual;
	}
	
	public Set<BiTable>getAutoSaveTables() {
		return(autoSaveColumns.keySet());
	}

	public Set<BiColumn>getAutoSaveColumns(BiTable bt) {
		return(autoSaveColumns.get(bt));
	}
	HashSet<BiView> getAutoAddUpdateViews() {
		return(autoAddUpdateViews);
	}
	
	public String getClassPath() {
		return(classPath);
	}

	public String linkAccessKey(BiView p_view) {
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(null);
		return (lp.accessKey);
	}
	public String linkAddUpdateKey(BiView p_view) {
		LinkProperty lp = linkProperties.get(p_view);
		if(lp == null) return(null);
		return (lp.addUpdateKey);
	}
	
	
	static void cloneBiChain(BiChain p_srcChain,BiChain p_newChain) {
		CellCollection subchain = p_srcChain.getCollection("subChains");
		if(subchain == null) return;
		String[] subchainList = subchain.listCollection_SingleLevel();
		if(subchainList == null) return;
		for(String subchainName : subchainList) {
			BiChain bchain = (BiChain) subchain.getCollection(subchainName);
			BiChain newSubChain = p_newChain.addChain(bchain.getTable());
			cloneBiChain(bchain,newSubChain);
		}
	}
//	public BiView(CellCollection parent,String p_header,BiTable rootTable,String p_label,String p_BrClass,String p_attribute,String p_fdUpdcnt,String p_ncStr,String p_addaccess,String p_updaccess,String p_delaccess,String p_rptaccess,String p_impaccess, String p_detaccess,String p_condition,String p_batupdaccess,String p_classPath)
	static BiView cloneView(String newViewName,BiView p_srcView) throws CellException {
		final BiView newView = new BiView(
									p_srcView.getParent(),
									p_srcView.getCellString("header"),
									p_srcView.getTable(),
									newViewName,
									p_srcView.brClass,
									"",
									p_srcView.fdUpdcnt,
									"",
									p_srcView.addAccess,
									p_srcView.updAccess,
									p_srcView.delAccess,
									p_srcView.rptAccess,
									p_srcView.impAccess,
									p_srcView.detAccess,
									"",
									p_srcView.batupdAccess,
									p_srcView.classPath
									);
		newView.wherecl = p_srcView.wherecl;
		newView.noSearch = p_srcView.noSearch;
		newView.noImport = p_srcView.noImport;
		newView.noExport = p_srcView.noExport;
		newView.allowMasterDetail = p_srcView.allowMasterDetail;
		newView.isGroupBy = p_srcView.isGroupBy;
		newView.useViewCache = p_srcView.useViewCache;
		newView.compareMasterWhenImport = p_srcView.compareMasterWhenImport;
		newView.allowInsertBiTable = p_srcView.allowInsertBiTable;
		newView.allowUpdateBiTable = p_srcView.allowUpdateBiTable;


		Vector<BiColumn> sv = p_srcView.getCollectionList("columns");
		Vector<BiColumn> dv = newView.getCollectionList("columns");
		for(BiColumn bc : sv) {
//			dv.add(bc);
//			newView.columnHash.put(bc.getLabel(), bc);
			BiColumn nbc = bc.cloneBiColumn(newView);
			dv.add(nbc);
			newView.columnHash.put(bc.getLabel(), nbc);
			newView.columnHdrHash.put(bc.getEngName(), nbc);
		}
		Vector<BiOrderBy> sov = p_srcView.getCollectionList("orderby");
		Vector<BiOrderBy> dov = newView.getCollectionList("orderby");
		for(BiOrderBy ob : sov) {
			dov.add(new BiOrderBy(newView,ob.getColumn(),ob.getDesc()));
		}
		for(BiView sl : p_srcView.links) {
			newView.links.add(sl);
			newView.linkProperties.put(sl, p_srcView.linkProperties.get(sl));
		}
		
		cloneBiChain(p_srcView,newView);
		return(newView);
	}

	
	public Class getBiResultClass() throws Exception {
		if(brClass == null) return(Class.forName("com.uniinformation.bicore.BiResult"));
		else return(Class.forName(brClass.trim()));
	}
	
	public List<String> getPreferListOrders() {
		return(preferListOrders);
	}
	public List<String> getPreferOrders() {
		return(preferListOrders);
	}
	
	public boolean allowUndo() {
		return(allowUndo);
	}
	public boolean allowAdhocColumn() {
		return(allowAdhocColumn);
	}
	public boolean alwaysInListView() {
		return(alwaysInListView);
	}

	public boolean newBatchUpdate() {
		return(newBatchUpdate);
	}
	
	static public void translateHeaders(BiSchema p_schema,String p_lhLang) {
//		if(!p_schema.getSchemaAutotranslate()) return;
		if(p_lhLang == null || p_lhLang.equals(p_schema.lhLang)) return;
		p_schema.lhLang = p_lhLang;
		if(p_lhLang.equals("TCHN")) {
		for(BiView tmpView : p_schema.getCollection("views").getCollections(BiView.class)) {
			for(BiColumn bc : tmpView.getColumns()) {
				if(bc.header != null) {
					bc.header = ChineseConvert.convertAuto2Bnew(bc.header);
				}
			}
		}
		}
		if(p_lhLang.equals("SCHN")) {
		for(BiView tmpView : p_schema.getCollection("views").getCollections(BiView.class)) {
			for(BiColumn bc : tmpView.getColumns()) {
				if(bc.header != null) {
					bc.header = ChineseConvert.convertAuto2Gnew(bc.header);
				}
			}
		}
		}
	}
}
