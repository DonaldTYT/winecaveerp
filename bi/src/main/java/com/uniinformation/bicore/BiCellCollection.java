package com.uniinformation.bicore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.bicore.BiView.BiViewWhereclParser;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionBrowserInterface;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellFormula;
import com.uniinformation.cell.IgnoreValue;
import com.kyoko.common.*;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.exprpar.FunctionInterface;
import com.uniinformation.utils.exprpar.VariableInterface;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.utils.whereclpar.Parser;

public class BiCellCollection extends CellCollection {
	protected BiResult br;
	int sid;
	int idx;
	boolean dirty = false;
	Hashtable<String,Integer> lockedCells;
	protected ColumnCell formulaInit = null;
	enum FuncName { FUNC_translateCond,FUNC_isMobile,FUNC_aGsum,FUNC_aGcount,FUNC_aGuniqueStr,FUNC_aGvalue,FUNC_encryptAndClear,FUNC_decrypt,FUNC_loginid(),
			FUNC_leftpedding,FUNC_sid,FUNC_idx,FUNC_aGuniqueString,FUNC_dayOfWeek,FUNC_hasIntersectTime,FUNC_subCell,FUNC_rowidx,FUNC_summaryDetail,
			FUNC_userid(),FUNC_hasAccess,FUNC_roundRate,FUNC_informixDate,FUNC_getAgent,FUNC_getDbName,FUNC_recordNotExist,FUNC_isBiSchemaView,
			FUNC_colVisibleAll,FUNC_colVisibleAny,FUNC_colExistAll,FUNC_colExistAny,FUNC_colInListAll,FUNC_colInListAny,FUNC_isFirstMaster,FUNC_isLastMaster,FUNC_lookup
//			,FUNC_defaultLdesc 
			,FUNC_datePeriod
			,FUNC_function
			,FUNC_isFirstRec
			,FUNC_isLastRec
			,FUNC_eval
			,FUNC_getRecordCount
			,FUNC_makeSlug
			,FUNC_jsonValue
			,NOT_DEFINED,
			};
	BiDynamicFunction dnf = null;
	public BiCellCollection (BiCellCollection p_col,BiResult p_br) {
		super(p_col);
		br = p_br;
	}
	
//	public static String translateCond3(BiResult br,String p_viewid,String p_condstr) {
//				String vn = p_viewid;
//				String s0 = p_condstr;
//				String s1 = "";
//				BiView bv = br.getView().getSchema().getViewByName(vn);
//				if(bv == null) return(s1);
//				try {
//					BiViewWhereclParser p = bv.getWhereclParser();
//					p.setParseMode(BiView.BiViewWhereclParser.GETOBJECT_MODE_NAME);
//					Condition cond = (Condition) p.evaluate(s0);
//					s1 = cond.toString();
//					return(s1);
//				} catch (Exception ex) {
//					UniLog.log(ex);
//					return(null);
//				}
//	}
	public static String translateCond(BiView p_view,String p_condstr,BiResult p_br) {
		String s0 = p_condstr;
		String s1 = "";
		BiView bv = p_view;
		if(bv == null) return(s1);
		try {
			BiViewWhereclParser p = bv.getWhereclParser(p_br);
			p.setParseMode(BiView.BiViewWhereclParser.GETOBJECT_MODE_NAME);
			Condition cond = (Condition) p.parse(s0);
			s1 = cond.toString();
			return(s1);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}

	/*
	private HashSet<String>notExistFunc;
	private FuncName checkAndGetFuncName(String p_fname) {
		if(notExistFunc != null && notExistFunc.contains(p_fname)) return(FuncName.NOT_DEFINED);
		try {
			return(FuncName.valueOf("FUNC_"+p_fname));
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
			if(notExistFunc == null) notExistFunc = new HashSet<String>();
			notExistFunc.add(p_fname);
			return(FuncName.NOT_DEFINED);
		}
	}
	*/
	

	@Override
	public Object evalFunction(String p_fname,Vector args) throws Exception
	{
		/*
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		*/
		/*
		FuncName funcName = checkAndGetFuncName(p_fname);		
		*/
		FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);		
		if(formulaInit != null) {
			switch (funcName){
				case FUNC_aGsum:
				case FUNC_aGcount:
				case FUNC_aGvalue:
				case FUNC_aGuniqueString:{
					String cellStr = (String) args.get(0); // cellLabel
					int dotPos = cellStr.indexOf(':');
					String viewId = cellStr.substring(0, dotPos);
					String cellName = cellStr.substring(dotPos+1);
					br.addAggregateCellValueAction(viewId, formulaInit,cellName);
					break;
				}	
				case FUNC_function : {
						if(dnf == null) {
							String dnfClass = "com.uniinformation.dynamic.function."+br.getView().getName();
							Class[]	paramTypes = new Class[]{BiCellCollection.class};
							try {
								dnf = (BiDynamicFunction) DynamicClassLoader.newInstance(dnfClass, paramTypes,BiCellCollection.this);
							} catch (Exception ex) {
								UniLog.log(ex);
							}
							
						}
					break;
					}
				default:
					/* temporary fix to elimiate the Class not found exception during formula init 
					 * since isblank is returning boolean value, return a default of 0.0 will cause class not found exception
					 * in long rung, should update parser .y file to handle it properly
					 * 2022-12-25 by DT
					 */
					if(p_fname.equals("isblank")) {
						return(false);
					}
			}
			return(0.0);
		}
		switch (funcName){
			case FUNC_isFirstRec:{
				if(idx == 0) return(true);
				return(false);
			}
			case FUNC_isLastRec:{
				if(idx < 0) return(false);
				if((idx+1) >= br.resultTr.getRecordCount()) return(true);
				return(false);
			}
			case FUNC_isFirstMaster:{
				if(idx < 0) return(false);
				if(idx == 0) return(true);
				Integer sid2 = (Integer) br.resultTr.getField(0,idx-1);
				if(sid2 == sid) return(false);
				return(true);
			}
			case FUNC_isLastMaster:{
				if(idx < 0) return(false);
				if((idx+1) >= br.resultTr.getRecordCount()) return(true);
				Integer sid2 = (Integer) br.resultTr.getField(0,idx+1);
				if(sid2 == sid) return(false);
				return(true);
			}
			case FUNC_hasIntersectTime:{
				Date fromA;
				Date toA;
				Date fromB;
				Date toB;
			}
			case FUNC_translateCond:{
				String vn = (String) args.get(0);
				String s0 = (String) args.get(1);
				String s1 = "";
				if(vn == null || vn.trim().equals("")) return(s1);
				if(s0 == null || s0.trim().equals("")) return(s1);
				BiView bv = br.getView().getSchema().getViewByName(vn);
				if(bv == null) return(s1);
				return(translateCond(bv,s0,br));
				/*
				try {
					BiViewWhereclParser p = bv.getWhereclParser();
					p.setParseMode(BiView.BiViewWhereclParser.GETOBJECT_MODE_NAME);
					Condition cond = (Condition) p.evaluate(s0);
					s1 = cond.toString();
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				return(s1);
				*/
			}
			case FUNC_aGcount: {
				if(!br.isActionEnabled()) return(0.0);
				String cellStr = (String) args.get(0); // cellLabel
				int dotPos = cellStr.indexOf(':');
				String viewId = cellStr.substring(0, dotPos);
				String cellName = cellStr.substring(dotPos+1);
				BiResult sr = br.getSubLink(viewId);
				if(sr != null) {
					Vector<BiCellCollection> v = sr.getRowCollectionList();
					return((double) v.size());
				} else return(0.0);
			}
			case FUNC_aGsum: {
				if(!br.isActionEnabled()) {
//					return(new com.uniinformation.cell.IgnoreValue());
//					don't know why is it set to return IgnoreValue() , change back to return(0.0) 
//					may cause problem for some special case that required the IgnoreValue()
//					2024/07/07, by DT
					return(0.0);
				}
				String cellStr = (String) args.get(0); // cellLabel
				int dotPos = cellStr.indexOf(':');
				String viewId = cellStr.substring(0, dotPos);
				String cellName = cellStr.substring(dotPos+1);
				BiResult sr = br.getSubLink(viewId);
				if(sr != null) {
					Vector<BiCellCollection> v = sr.getRowCollectionList();
					double result = 0;
					for(BiCellCollection col : v) {
						result += col.getCell(cellName).getDouble();
					}
					return(result);
				} else {
//					return(new com.uniinformation.cell.IgnoreValue());
//					don't know why is it set to return IgnoreValue() , change back to return(0.0) 
//					may cause problem for some special case that required the IgnoreValue()
//					2024/07/07, by DT
					return(0.0);
				}
			}
			case FUNC_aGvalue: {
				if(!br.isActionEnabled()) return(0.0);
				String cellStr = (String) args.get(0); // cellLabel
				int dotPos = cellStr.indexOf(':');
				String viewId = cellStr.substring(0, dotPos);
				String cellName = cellStr.substring(dotPos+1);
				BiResult sr = br.getSubLink(viewId);
				if(sr != null) {
				Vector<BiCellCollection> v = sr.getRowCollectionList();
				double result = 0;
				double didx = (Double) args.get(1);
				int idx = (int) didx;
				if(v.size() > idx) {
					return(v.get(idx).getCell(cellName).getObject());
				} else {
					return(args.get(2));
				}
				} else return(0.0);
			}
		case FUNC_subCell: {
			/* Argument 0 is dummy non database column */
			String collectionName = (String) args.get(1);
			String cellName = (String) args.get(2);
			if(collectionName != null && cellName != null) {
				CellCollection scl = getCollection(collectionName);
				if(scl != null) return(scl.getCell(cellName).getObject());
			}
			return(null);
		}
		case FUNC_aGuniqueString: {
//				if(!br.actionEnabled) return("");
				String cellStr = (String) args.get(0); // cellLabel
				String s = (String) args.get(1); // delimiter
				int dotPos = cellStr.indexOf(':');
				String viewId = cellStr.substring(0, dotPos);
				String cellName = cellStr.substring(dotPos+1);
				BiResult sr = br.getSubLink(viewId);
				if(sr != null) {
					Vector<BiCellCollection> v = sr.getRowCollectionList();
					UniqueStrings us = new UniqueStrings(s);
					for(BiCellCollection col : v) {
						String sval = col.getCell(cellName).getString();
						if(sval != null && !sval.trim().equals("")) {
							us.add(sval.trim());
						}
					}
					String ss = us.toString();
					return(ss == null ? "" : ss);
				} else return("");
		}	
		/* aGuniqueStr is obsolete, only use in kanghong old system, should replace by aGuniqueString */
		case FUNC_aGuniqueStr: {
			String s = (String) args.get(0); // delimiter
			String cellStr = (String) args.get(1); // cellLabel
			int dotPos = cellStr.indexOf(':');
			String viewId = cellStr.substring(0, dotPos);
			String cellName = cellStr.substring(dotPos+1);
			BiResult sr = br.getSubLink(viewId);
			if(sr != null) {
				Vector<BiCellCollection> v = sr.getRowCollectionList();
				UniqueStrings us = new UniqueStrings(s);
				for(BiCellCollection col : v) {
					String sval = col.getCell(cellName).getString();
					if(sval != null && !sval.trim().equals("")) {
						us.add(sval.trim());
					}
				}
				
				String ss = us.toString();
				return(ss == null ? "" : ss);
			} else return("");
			}
			case FUNC_isMobile :{
				return(br.getSessionHelper().isMobile() ? 1 : 0);
			}
			case FUNC_decrypt:{
				String encryptedString = (String ) args.get(0);
				if(br.getSessionHelper().getAESKey() == null || StringUtils.isBlank(encryptedString)) return(args.get(2));
				JSONObject rootJo = new JSONObject(Base64Util.decryptStrFromBase64(br.getSessionHelper(), encryptedString));
				String colName = (String) args.get(1);
				if(rootJo.has(colName)) return(rootJo.get(colName));
				return(args.get(2));
			}
			case FUNC_encryptAndClear:{
				if(br.getSessionHelper().getAESKey() == null) return("");
				JSONObject rootJo = new JSONObject();
				for(int i=0;i<args.size();i++) {
					String s = (String) args.get(i);
					rootJo.put(s, getCell(s).getObject());
					getCell(s).resetValue();
				}
				return(Base64Util.encryptStrToBase64(br.getSessionHelper(), rootJo.toString()));
			}
			case FUNC_loginid:{
				return(br.getSessionHelper().getLoginId());
			}
			case FUNC_userid:{
				return(br.getSessionHelper().getVcode());
			}
			case FUNC_hasAccess:
				String accessKey = (String) args.get(0);
				return(br.getSessionHelper().hasAccessRight(accessKey));
			case FUNC_leftpedding: {
				double didx = (Double) args.get(0);
				int idx = (int) didx;
				String str = (String) args.get(1);
				String os = "";
				for(int i=0;i<idx;i++) {
					os += ".";
				}
				os += str;
				return(os);
			}	
			case FUNC_sid: {
				return(getSid());
			}
			case FUNC_idx: {
				return(getIdx());
			}
			case FUNC_dayOfWeek: {
				if (args.get(0) instanceof Date) {
					Date date = (Date) args.get(0);
					return (DateUtil.dayInWeekStr((Date) args.get(0)));
				}
				return "";
			}
			case FUNC_rowidx: {
//				double dofs = (Double) args.get(0);
//				int ofs = (int) dofs;
				int ofs = Cell.objectToInt(args.get(0));
				Vector<BiCellCollection> v = br.getRowCollectionList();
				int c = v.indexOf(this);
				return((c+ofs));
			}
			case FUNC_summaryDetail: {
				String detView = (String) args.get(0);
				String aggName = (String) args.get(1);
				String detField= (String) args.get(2);
//				Object o = SummaryCache.getSummary(br.getSessionHelper(),br.getView(),br.getSubLink(detView),AggregateOrPivot.AGGREGATES.valueOf(aggName),p_field,p_value) {
			}
			case FUNC_roundRate: {
				double d0 = (Double) args.get(0);
				if(d0 == 0) return(0.0);
				double d1 = (Double) args.get(1);
				if(d1 == 0) return(0.0);
				double pd = (Double) args.get(2);

//				String targetRate = (String) args.get(3);
//				if(targetRate == null || targetRate.isEmpty()) return(0.0);
//				double dr = br.getCellDouble(targetRate);
				double dr = (Double) args.get(3);
				double pr =  (Double) args.get(4);
				double dd = CellCollection.round(dr * d0 , pd);
				if(dd == d1) return(dr); else return(CellCollection.round(d1/d0, pr));
			}
			case FUNC_getAgent: {
				return(br.getSessionHelper().getAgent());
			}
			case FUNC_getDbName: {
				return(br.getSessionHelper().getDbName());
			}
			case FUNC_recordNotExist: {
				return(!br.recordExist(args));
			}
			case FUNC_isBiSchemaView: {
				return(br.getSessionHelper().isBiSchemaView());
			}
			case FUNC_informixDate : {
				Date d = (Date) args.get(0);
				if(d == null || !d.after(DateUtil.minDate)) return(0);
				return(DateUtil.dateToInformix(d));
			}
			case FUNC_colExistAll : {
				for(Object o : args) {
					if(br.getColumnByLabel(o.toString()) == null) return(false);
				}
				return(true);
			}
			case FUNC_colExistAny: {
				for(Object o : args) {
					if(br.getColumnByLabel(o.toString()) != null) return(true);
				}
				return(false);
			}
			case FUNC_colInListAll: {
				for(Object o : args) {
					if(!br.isInViewList(o.toString())) return(false);
				}
				return(true);
			}
			case FUNC_colInListAny: {
				for(Object o : args) {
					if(br.isInViewList(o.toString())) return(true);
				}
				return(false);
			}
			case FUNC_colVisibleAll: {
				for(Object o : args) {
					if(br.getColumnByLabel(o.toString()) == null || br.getColumnByLabel(o.toString()).isInvisible(br.getSessionHelper())) return(false);
				}
				return(true);
			}
			case FUNC_colVisibleAny: {
				for(Object o : args) {
					if(br.getColumnByLabel(o.toString()) != null && !br.getColumnByLabel(o.toString()).isInvisible(br.getSessionHelper())) return(true);
				}
				return(false);
			}
			case FUNC_lookup: {
				String selStr = (String) args.get(0);
				Object val = (Object) args.get(1);
				int mode = (Integer) args.get(2);
				TableRec tr;
				if(args.size()> 3) {
					Wherecl wcl = new Wherecl();
					for(int i=3;i<args.size();i++) {
						Object o = args.get(i);
						if((mode & 1) == 1) {
							if(Cell.objectIsBlank(o)) {
								return(val);
							}
						}
						wcl.appendArgument(o);
					}
					tr = br.getSelectUtil().getQueryResult(selStr,wcl);
				} else {
					tr = br.getSelectUtil().getQueryResult(selStr);
				}
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					return(tr.getField(0));
				}
				return(val);
			}
			case FUNC_eval: {
				String f = (String) args.get(0);
				if(f == null || f.trim().equals("")) return(new IgnoreValue());
				com.uniinformation.utils.exprpar.Parser p = new com.uniinformation.utils.exprpar.Parser(compareMode,f, this, new FunctionInterfaceEx(this,args));
				try {
					return(p.evaluate());
				} catch (Error ex) {
					UniLog.log("eval formula error");
					return(0.0);
				}			
			}
			/*
			case FUNC_defaultLdesc: {
				int lcrg = Erpv4Config.getDefaultLcrg(br.getSessionHelper());
				if(lcrg > 0) {
					return(Erpv4Config.getLcDesc(br.getSessionHelper(),lcrg));
				} else {
					return("");
				}
			}
			*/
		case FUNC_datePeriod: {
			SimpleDateFormat dfmt=null;
				try {
					if(args.size() < 2)
					dfmt = new SimpleDateFormat("yyyy-MM");
					else
					dfmt = new SimpleDateFormat((String) args.get(1));
				} catch (IllegalArgumentException iex) {
					UniLog.log("Period Format Invalid use default yyyy-MM");
					dfmt = new SimpleDateFormat("yyyy-MM");
				} catch (ArrayIndexOutOfBoundsException iex) {
					UniLog.log("Period Format Invalid use default yyyy-MM");
					dfmt = new SimpleDateFormat("yyyy-MM");
				}
				return(dfmt.format((Date) args.get(0)));
			}
		case FUNC_getRecordCount: {
			return(br.getRecordCount());
		}
		case FUNC_makeSlug : {
			return(makeSlug(args.toArray()));
		}
		case FUNC_jsonValue: {
			String js = (String) args.get(0);
			String jk = (String) args.get(1);
			Object o = getJSONObject(js,jk);
			if(o == null) {
				return("");
			}
			return(o);
		}
		case FUNC_function: {
				if(dnf == null) return(0.0);
				return(dnf.eval(args));
			}
		}
		return(super.evalFunction(p_fname, args));
	}
	protected void afterCreateColumnCells() {
		
	}
	
	public boolean isLocked() {
			return(lockedCells != null);
	}
	public void unlock() throws CellException {
		if(lockedCells != null) {
			for(String cn : lockedCells.keySet()) {
				testCell(cn).setMode(lockedCells.get(cn));
			}
			lockedCells = null;
		}
	}
	public void lock() throws CellException {
		lock(null);
	}
	public void lock(HashSet<String> ignoreList) throws CellException {
		if(lockedCells == null) lockedCells=new Hashtable<String,Integer>();
		for(BiColumn bc : br.getColumns()) {
			String cn = bc.getLabel();
			if(ignoreList != null && ignoreList.contains(cn)) continue;
			if(lockedCells.get(cn) == null) {
				if(testCell(cn) != null && testCell(cn).getMode() != Cell.VMODE_DISPONLY) {
					lockedCells.put(cn, testCell(cn).getMode());
					testCell(cn).setMode(Cell.VMODE_DISPONLY);
				}
			}
				
		}
	}
	public void setSid(int p_sid,int p_idx) {
		sid = p_sid;
		idx = p_idx;
	}
	public int getSid() {
		return(sid);
	}
	public int getIdx() {
		return(idx);
	}
	public BiResult getBr() {
		return(br);
	}
	public void initFormula(ColumnCell p_cell,CellFormula p_formula) throws CellException {
		formulaInit = p_cell;
		p_formula.eval();
		formulaInit = null;
	}
//	@Override
//	public void initFunction(String p_functName, Vector p_args) throws Exception {
//		// TODO Auto-generated method stub
//		FuncName funcName = FuncName.NOT_DEFINED;
//		try {
//			funcName = FuncName.valueOf("FUNC_"+p_functName);
//		}
//		catch(Exception ex) {
//			//remark: if enum not exist, will got exception here.
//			switch (funcName){
//				case FUNC_aGsum: {
//					int cc;
//					cc = 0;
//				}
//			}
//		}
//		super.initFunction(p_functName, p_args);
//	}
	protected void setDirty(boolean p_sw) {
		dirty = p_sw;
		if(p_sw) {
			CellCollection pcl = getParent();
			if(pcl != null && pcl instanceof BiCellCollection) {
				((BiCellCollection) pcl).setDirty(p_sw);
			}
		} else {
			try {
			browse(new CellCollectionBrowserInterface () {

				@Override
				public void gotCell(String p_cellName, Cell p_cell) throws Exception {
					// TODO Auto-generated method stub
					if(p_cell instanceof ColumnCell) {
						((ColumnCell) p_cell).clearDirty();
					}
				}

				@Override
				public void gotCellArray(String p_cellName, Cell[] p_cellArray) throws Exception {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void gotCollection(String p_cellName, CellCollection p_collection) throws Exception {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void gotCollectionList(String p_cellName, Vector p_collectionList) throws Exception {
					// TODO Auto-generated method stub
					
				}} ,new Hashtable());
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
	}
	
	static public String makeSlug(Object ... p_objects) {
		String slug = null;
		for(Object o : p_objects) {
			if(o != null) {
				String ss = o.toString();
				if(ss != null) {
					ss = ss.trim().toLowerCase().replace(".", "_").replace(" ", "-").replace ("'","").replace(",", "");
					if(slug == null) slug = ss; else slug = slug + "-" + ss;
				}
			}
		}
		return(slug);
	}
	
	static Object getJSONObject(String p_jsonStr,String p_key) {
		if(StringUtils.isBlank(p_jsonStr)) return(null);
		if(StringUtils.isBlank(p_key)) return(null);
		try {
			JSONObject  jo = new JSONObject(p_jsonStr);
			Object o = jo.opt(p_key);
			if(o != null) return(o);
			int idx = p_key.indexOf(".");
			if(idx < 0) return(null);
			JSONObject jo2 = jo.optJSONObject(p_key.substring(0,idx));
			return(getJSONObject(jo2.toString(),p_key.substring(idx+1)));
		} catch (JSONException jex ) {
			UniLog.log(jex);
			return(null);
		}
	}
}

