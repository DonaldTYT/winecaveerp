package com.uniinformation.cell;
import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.*;
import java.lang.reflect.*;
import java.lang.Exception.*;
import java.math.*;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.kyoko.common.Sprintf;
import com.kyoko.common.StringUtil;
import com.kyoko.common.CoreLog;
import com.uniinformation.utils.*;
import com.uniinformation.utils.exprpar.*;

public class CellCollection implements Serializable,FunctionInterface,VariableInterface,GetCellInterface,ToXMLInterface
{  
   static final long serialVersionUID = 3183356868541481533L;
   public final static int TYPE_OBJECT_START = 0;
   public final static int TYPE_OBJECT_END   = 1;
   public final static int TYPE_SCALAR       = 2;
   public final static int TYPE_ARRAY        = 3;
   public final static int TYPE_VARARRAY_START = 4;
   public final static int TYPE_VARARRAY_END   = 5;
   public final static int TYPE_VARSCALARARRAY_START = 6;
   public final static int TYPE_VARSCALARARRAY_END = 7;
	public final static int CONV_NONE = 0;
	public final static int CONV_ANY2B = 3;
	public final static int CONV_ANY2G = 4;
	static private Hashtable reflectedFieldHashHash = new Hashtable();  // hash of (hash of Field key by varname) key by Class
	private boolean valid = true;
	protected int compareMode = 0;
	
	private CellCollection parent = null;
	//private boolean reflected = false;
	private Hashtable cellTable = null;
	private Hashtable<String,CellCollection> collectionTable = null;
	private Hashtable collectionListTable = null;
	private Random rd = null;
	
	private enum FuncName { FUNC_btod, FUNC_if, FUNC_sqrt, FUNC_isblank, FUNC_sprintf, FUNC_format, FUNC_ignored, FUNC_equals, FUNC_formatDate, FUNC_dateToUnixtime, FUNC_toValue, 
			FUNC_mod, FUNC_dtob, FUNC_roundup, FUNC_rounddown, FUNC_ftostr, FUNC_eval, FUNC_deltaDate,FUNC_strcat,FUNC_strpart,FUNC_today,FUNC_random,FUNC_now,FUNC_getParentCell,FUNC_isNull,
			FUNC_contains,FUNC_strtodate,FUNC_sa,FUNC_maxf,FUNC_chr2int,FUNC_int2chr,FUNC_yearofdate,FUNC_monthofdate,FUNC_weekdayofdate,FUNC_dayofdate,FUNC_age,FUNC_toSet,FUNC_includeSet,
			FUNC_jdate,FUNC_zeroDate,FUNC_maxDate,FUNC_stripNumber,FUNC_irounddown,FUNC_unixtimeToDate,FUNC_nextMonth,FUNC_nextYear,FUNC_dtoi,FUNC_round,FUNC_firstLine,FUNC_strncat,
			FUNC_valueof,FUNC_dateof,FUNC_min,FUNC_max,FUNC_sum,FUNC_floor,FUNC_ceil,FUNC_strcombine,FUNC_isNaN,FUNC_isfalse,FUNC_mapping,FUNC_stepup,FUNC_tolower,FUNC_toupper,NOT_DEFINED }
	
	public CellCollection()
	{
	}
	public CellCollection(CellCollection p_parent)
	{
		parent = p_parent;
	}
	
	public boolean cellTableContain(String p_cell)
	{
		if(cellTable == null) return(false);
		return(cellTable.containsKey(p_cell));
	}
	public Cell addCell(String p_name,Cell p_cell)
	{
		if(p_cell == null || p_name == null) return(null);
		checkReflected();
      Hashtable h = (Hashtable) reflectedFieldHashHash.get(getClass());
		if (h != null) {
         Field fd = (Field) h.get(p_name);
         if (fd != null) {
            try {
					fd.set(this, p_cell);
            } catch (IllegalAccessException ex) {
               CoreLog.log(ex);
            }
				return(p_cell);
         }
		}
		if(cellTable == null) cellTable = new Hashtable();
		cellTable.put(p_name,p_cell);
		return(p_cell);
	}
	public Cell[] addCell(String p_name,Cell p_cell[])
	{
		if(p_cell == null || p_name == null) return(null);
		checkReflected();
      Hashtable h = (Hashtable) reflectedFieldHashHash.get(getClass());
		if (h != null) {
         Field fd = (Field) h.get(p_name);
         if (fd != null) {
            try {
					fd.set(this, p_cell);
            } catch (IllegalAccessException ex) {
               CoreLog.log(ex);
            }
				return(p_cell);
         }
		}
		if(cellTable == null) cellTable = new Hashtable();
		cellTable.put(p_name,p_cell);
		return(p_cell);
	}
	public void delCell(String p_name)
	{
		if(cellTable == null) return;
		cellTable.remove(p_name);
	}
	public CellCollection addCollection(String p_name,CellCollection p_collection)
	{
		if(p_collection == null || p_name == null) return(null);
		if(collectionTable == null) collectionTable = new Hashtable();
		collectionTable.put(p_name,p_collection);
		return(p_collection);
	}
	public void delCollection(String p_name)
	{
		if(collectionTable == null) return;
		collectionTable.remove(p_name);
	}
	public CellCollection clearCollection(String p_name) {
		//delete old collection
		delCollection(p_name);
		//add new cc
		CellCollection cc = new CellCollection(this);
		addCollection(p_name, cc);
		return cc;
	}
	public Vector addCollectionList(String p_name,CellVector p_collectionlist)
	{
		if(p_collectionlist == null || p_name == null) return(null);
		if(collectionListTable == null) collectionListTable = new Hashtable();
		collectionListTable.put(p_name,p_collectionlist);
		return(p_collectionlist);
	}
	public void delCollectionList(String p_name)
	{
		if(collectionListTable == null) return;
		collectionListTable.remove(p_name);
	}
	public Object evalVariable(String p_vname,int p_idx)
	{
		Cell c = getCellArray(p_vname,p_idx);
		if (c == null) return(null);
		if(c.getType() == Cell.VTYPE_STRING)
			return(c.getString());
		else if(c.getType() == Cell.VTYPE_DATE)	
			return(c.getDate());
		else if(c.getType() == Cell.VTYPE_DATETIME)	
			return(c.getDate());
		else if(c.getType() == Cell.VTYPE_BOOLEAN)
			return(new Boolean(c.getBoolean()));
		else
			return(new Double(c.getDouble()));
	}
	public Object evalVariable(String p_vname)
	{
		Cell c = getCell(p_vname);
		if (c == null) return(null);
		if(c.getType() == Cell.VTYPE_STRING)
			return(c.getString());
		else if(c.getType() == Cell.VTYPE_DATETIME)	
			return(c.getDate());
		else if(c.getType() == Cell.VTYPE_DATE)
			return(c.getDate());
		else if(c.getType() == Cell.VTYPE_BOOLEAN)
			return(new Boolean(c.getBoolean()));
		else
			return(new Double(c.getDouble()));
	}
	/*
	 * 
	//andrew200106: obsoleted. replaced by switch
	public Object evalFunction(String p_fname,Vector args) throws Exception
	{
		if(p_fname.equals("btod")) {
			if(args.size() != 1) return(null);
			if(! (args.get(0) instanceof Boolean) ) return(null);
			if(((Boolean) args.get(0)).booleanValue()) return new Double(1.0);
			else return(new Double(0.0));
		}
		if(p_fname.equals("if")) {
			//CoreLog.log("CellCollectin eval 'if' " + args.size());
			if(args.size() != 3) return(null);
			//CoreLog.log("args " + ((Boolean) args.get(0)).booleanValue() + " " + args.get(1) + " " + args.get(2));
			if(((Boolean) args.get(0)).booleanValue()) {
				return(args.get(1));
			} else {
				return(args.get(2));
			}
		}
		if(p_fname.equals("sqrt")) {
			if(args.size() != 1) return(null);
			if(! (args.get(0) instanceof Double) ) return(null);
			double d = Math.sqrt(((Double) args.get(0)).doubleValue());
			return(new Double(d));
		}
		if(p_fname.equals("isblank")) {
			return(args.get(0).toString().trim().equals(""));
		} 
		if(p_fname.equals("sprintf")) {
			Sprintf spf = new Sprintf((String) args.get(0));
			for(int i=1;i<args.size();i++) {
				if(args.get(i) instanceof Integer) {
					spf.add(((Integer) args.get(i)).intValue());
				} else if(args.get(i) instanceof Double) {
					spf.add(((Double) args.get(i)).doubleValue());
				} else if(args.get(i) instanceof java.util.Date) {
					spf.add( DateUtil.toDateString((java.util.Date) args.get(i) ,"yy/mm/dd"));
				} else {
					spf.add( args.get(i).toString());
				}
			}
			return(spf.toString());
		}
		if(p_fname.equals("format")) {
			switch(args.size()) {
			case 1 : return(String.format((String) args.get(0)));
			case 2 : return(String.format((String) args.get(0), args.get(1)));
			case 3 : return(String.format((String) args.get(0), args.get(1),args.get(2)));
			case 4 : return(String.format((String) args.get(0), args.get(1),args.get(2),args.get(3)));
			case 5 : return(String.format((String) args.get(0), args.get(1),args.get(2),args.get(3),args.get(4)));
			case 6 : return(String.format((String) args.get(0), args.get(1),args.get(2),args.get(3),args.get(4),args.get(5)));
			}
		}
		if(p_fname.equals("ignored")) {
			return(new com.uniinformation.utils.exprpar.IgnoreValue());
		}
		if(p_fname.equals("equals")) {
			return(args.get(0).toString().equals(args.get(1).toString()));
		}
		if(p_fname.equals("formatDate")) {
			SimpleDateFormat dfmt = new SimpleDateFormat((String) args.get(1));
			return(dfmt.format((Date) args.get(0)));
		}
		if(p_fname.equals("dateToUnixtime")) {
			Date d = (Date) args.get(0);
			if(d != null && d.after(DateUtil.minDate))  {
				double dd = DateUtil.dateToUnixtime(d);
				return(new Double(dd));
			} else return(0.0);
		}
		if(p_fname.equals("toValue")) {
			Object o = args.get(0);
			if( o instanceof String) {
				try {
					double d = Double.parseDouble(((String) o).trim());
					return(d);
				} catch (Exception ex) {
					CoreLog.log("toValue " + o.toString() + "failed, return 0.0" );
					return(0.0);
				}
			}
		}
		if(p_fname.equals("mod")) {
			int a;
			int b;
			double d;
			d = (Double) args.get(0);
			a = (int) d;
			d = (Double) args.get(1);
			b = (int) d;
			if(b <= 0) d = 0.0; else d  = (double) (a % b);
			return(d);
		}
		if(p_fname.equals("dtob")) {
			if(args.size() != 1) return(null);
			if(! (args.get(0) instanceof Double) ) return(null);
			if((Double) args.get(0) > 0) {
				return(true);
			} else  {
				return(false);
			}
		}
		
		if(p_fname.equals("roundup")) {
			double d = (Double) args.get(0);
			d = Math.round(d * 1000000.0) / 1000000.0;
			double r = (Double) args.get(1);
			d = Math.ceil(d * (1.0d / r)) * r;
			d = Math.round(d * 1000000.0) / 1000000.0;
			return(d);
		}
		if(p_fname.equals("rounddown")) {
			double d = (Double) args.get(0);
			d = Math.round(d * 1000000.0) / 1000000.0;
			double r = (Double) args.get(1);
			d = Math.floor(d * (1.0d/ r)) * r;
			d = Math.round(d * 1000000.0) / 1000000.0;
			return(d);
		}
		if(p_fname.equals("ftostr")) {
			double d = (Double) args.get(0);
			String s = (String) args.get(1);
			return(StringUtil.ftostr(d, s));
		}
		if(p_fname.equals("eval")) {
			String f = (String) args.get(0);
			if(f == null || f.trim().equals("")) return(0.0);
			Parser p = new Parser(f,this,this);
			try {
				return(p.evaluate());
			} catch (Error ex) {
				CoreLog.log("eval formula error");
				return(0.0);
			}
		}
		if(parent != null) 
			return(parent.evalFunction(p_fname,args));
		else
			return(null);
	}
	*/
	/*
	//230425 obsoelected. replaced by checkAndGetFuncNameCache
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

	
	private HashMap<String,Object>funcNameCacheHM = new HashMap<String,Object>(); //key: classfullname +"."+funcname
	/***
	 * global cache function for obtain FuncName enum by name
	 * @param p_fname - function name
	 * @param p_notDefined - enum not defined value
	 * @return
	 */
	final protected <E extends Enum<E>> E checkAndGetFuncNameCache(String p_fname, E p_notDefined) {
		String fullName = p_notDefined.getClass().getCanonicalName() + "." + p_fname;
		Object funcName = funcNameCacheHM.get(fullName);
		try {
			if (funcName != null) {
				return (E) funcName;
			}
			//funcName = FuncName.valueOf("FUNC_" + p_fname);
			funcName = Enum.valueOf(p_notDefined.getClass(), "FUNC_"+p_fname);
			//funcName = Enum.valueOf(p_notDefined.getClass(), "FUNC_"+p_fname);
			funcNameCacheHM.put(fullName, funcName);
			return((E)funcName);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
			funcNameCacheHM.put(fullName, p_notDefined);
			return p_notDefined;
		}
	}
	
	
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
		//230425 obsoelected. replaced by checkAndGetFuncNameCache
		FuncName funcName = checkAndGetFuncName(p_fname);
		*/
		FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);
		
		//remark: each function should has it own block to avoid duplicate variable
		switch (funcName){
			case FUNC_strncat:{
				StringBuffer sb = new StringBuffer();
				for(int i=0;i<args.size();i+=2) {
					String str = args.get(i).toString();
					int len = Cell.objectToInt(args.get(i+1));
					if(str.length() >= len) {
						sb.append(str.substring(0,len));
					} else {
						sb.append(str);
						for(int n=str.length();n<len;n++) {
							sb.append(' ');
						}
					}
				}
				return(sb.toString());
			}
			case FUNC_strcat:{
				String s = "";
				for(Object o : args) {
					if(o instanceof String) {
						s += (String) o;
					} else if(o instanceof Boolean) {
						if((Boolean) o) {
							s += "Y";
						} else {
							s += "N";
						}
					} else {
						s += o.toString();
					}
				}
				return(s);
			}
			case FUNC_strcombine:{
				String s = null;
				for(Object o : args) {
					String ss = o.toString();
					if(StringUtils.isEmpty(ss)) continue;
					if(s == null) s = ss; else s += " " + ss;
				}
				return(s == null ? "" : s);
			}
			case FUNC_strpart:{
				String s = (String) args.get(0);
				int idx = 0;
				int len = -1;
				if(args.get(1) instanceof Integer) {
					idx = (Integer) args.get(1);
				} else if(args.get(1) instanceof Double ) {
					double d = (Double) args.get(1);
					idx = (int) d;
				}
				if(args.get(2) instanceof Integer) {
					len = (Integer) args.get(2);
				} else if(args.get(1) instanceof Double ) {
					double d = (Double) args.get(2);
					len = (int) d;
				}
				return(StringUtil.strpart(s, idx, len));
			}
			case FUNC_btod:{
				if(args.size() != 1) return(null);
				if(! (args.get(0) instanceof Boolean) ) return(null);
				if(((Boolean) args.get(0)).booleanValue()) return new Double(1.0);
				else return(new Double(0.0));			
			}
			case FUNC_if:{
				//CoreLog.log("CellCollectin eval 'if' " + args.size());
				if(args.size() != 3) return(null);
				//CoreLog.log("args " + ((Boolean) args.get(0)).booleanValue() + " " + args.get(1) + " " + args.get(2));
				if(((Boolean) args.get(0)).booleanValue()) {
					return(args.get(1));
				} else {
					return(args.get(2));
				}				
			}
			case FUNC_sqrt:{
				if(args.size() != 1) return(null);
				if(! (args.get(0) instanceof Double) ) return(null);
				double d = Math.sqrt(((Double) args.get(0)).doubleValue());
				return(new Double(d));
			}
			case FUNC_isblank:{
				if(args.size() < 1 || args.get(0) == null) return true; //andrew230213 hotfix for null exception. assume null is blank
				if(args.get(0) instanceof java.util.Date) {
					if(DateUtil.minDate.after((java.util.Date) args.get(0))) {
						return(true);
					} else return(false);
				}
				return(args.get(0).toString().trim().equals(""));

			}
			case FUNC_isfalse:{
				Object o = args.get(0);
				if(o instanceof Boolean) {
					return(! (Boolean) o);
				}
				if(o instanceof String) {
					if(o.toString().toLowerCase().equals("true") ) return(false);
					if(o.toString().toLowerCase().equals("y") ) return(false);
					return(true);
				}
				if(o instanceof Integer) {
					return(((Integer) o).intValue() == 0); 
				}
				if(o instanceof Double) {
					return(((Integer) o).doubleValue() == 0.0); 
				}
				return(false);
			}
			case FUNC_sprintf:{
				Sprintf spf = new Sprintf((String) args.get(0));
				for(int i=1;i<args.size();i++) {
					if(args.get(i) instanceof Integer) {
						spf.add(((Integer) args.get(i)).intValue());
					} else if(args.get(i) instanceof Double) {
						spf.add(((Double) args.get(i)).doubleValue());
					} else if(args.get(i) instanceof java.util.Date) {
						spf.add( DateUtil.toDateString((java.util.Date) args.get(i) ,"yy/mm/dd"));
					} else {
						spf.add( args.get(i).toString());
					}
				}
				return(spf.toString());
				
			}
			case FUNC_format:{
				switch(args.size()) {
				case 1 : return(String.format((String) args.get(0)));
				case 2 : return(String.format((String) args.get(0), args.get(1)));
				case 3 : return(String.format((String) args.get(0), args.get(1),args.get(2)));
				case 4 : return(String.format((String) args.get(0), args.get(1),args.get(2),args.get(3)));
				case 5 : return(String.format((String) args.get(0), args.get(1),args.get(2),args.get(3),args.get(4)));
				case 6 : return(String.format((String) args.get(0), args.get(1),args.get(2),args.get(3),args.get(4),args.get(5)));
				default: throw new Exception("Too many argument");
				}				
			}
			case FUNC_ignored:{
				return(new IgnoreValue());
			}
			case FUNC_equals:{
				return(args.get(0).toString().equals(args.get(1).toString()));
			}
			case FUNC_contains:{
				for(int i=1;i<args.size();i++) {
					if(args.get(0).equals(args.get(i))) return(true);
				}
				return(false);
			}
			case FUNC_formatDate:{
				SimpleDateFormat dfmt = new SimpleDateFormat((String) args.get(1));
				return(dfmt.format((Date) args.get(0)));
			}
			case FUNC_dateToUnixtime:{
				Date d = (Date) args.get(0);
				if(d != null && d.after(DateUtil.minDate))  {
					double dd = DateUtil.dateToUnixtime(d);
					return(new Double(dd));
				} else return(0.0);				
			}
			case FUNC_unixtimeToDate:{
				int a = 0;
				double d;
				if(args.get(0) instanceof Double) { 
					d = (Double) args.get(0);
					a = (int) d;
				}
				if(args.get(0) instanceof Integer) { 
					a = (Integer) args.get(0);
				}
				if(a <= 0) return(DateUtil.zeroDate);
				return(DateUtil.unixtimeToDate(a));
			}
			case FUNC_toValue:{
				Object o = args.get(0);
				if(o instanceof String) {
					try {
						double d = Double.parseDouble(((String) o).trim());
						return(d);
					} catch (Exception ex) {
						CoreLog.log("toValue " + o.toString() + "failed, return 0.0" );
						return(0.0);
					}
				}				
				throw new Exception("Non string value");
			}
			case FUNC_nextMonth:{
				Date d  = (Date) args.get(0);
				if(! d.after(DateUtil.minDate)) return(DateUtil.zeroDate);
				/*
				double f;
				f = (Double) args.get(1);
				int n = (int) f;
				*
				*/
				int n = Cell.objectToInt(args.get(1));
				if(n > 0)
					d = DateUtil.nextmonth(d,n);
				else if(n < 0)
					d = DateUtil.prevmonth(d,-n);
				return(d);
			}
			case FUNC_nextYear:{
				Date d  = (Date) args.get(0);
				if(! d.after(DateUtil.minDate)) return(DateUtil.zeroDate);
				double f;
				f = (Double) args.get(1);
				int n = (int) f;
				d = DateUtil.nextyear(d,n);
				return(d);
			}
			case FUNC_mod:{
				int a;
				int b;
				/*
				double d;
				d = (Double) args.get(0);
				a = (int) d;
				*/
				a = Cell.objectToInt(args.get(0));
				/*
				d = (Double) args.get(1);
				b = (int) d;
				*/
				double d;
				b = Cell.objectToInt(args.get(1));
				if(b <= 0) d = 0.0; else d  = (double) (a % b);
				return(d);				
			}
			case FUNC_irounddown:{
				int a;
				int b;
				/*
				double d;
				d = (Double) args.get(0);
				a = (int) d;
				d = (Double) args.get(1);
				b = (int) d;
				*/
				a = Cell.objectToInt(args.get(0));
				b = Cell.objectToInt(args.get(1));
				if(a == 0 || b  == 0 ) return((Integer) 0);
				return((Integer) a - (a % b));
			}
			case FUNC_dtob:{
				if(args.size() != 1) return(null);
				if(! (args.get(0) instanceof Double) ) return(null);
				if((Double) args.get(0) > 0) {
					return(true);
				} else  {
					return(false);
				}					
			}
			case FUNC_dtoi:{
				if(args.size() != 1) return(null);
				if(! (args.get(0) instanceof Double) ) return(null);
				double d = (Double)args.get(0);
				return((int) d);
			}
			case FUNC_round:{
				if(args.get(0) instanceof IgnoreValue) {
					return(args.get(0));
				}
				double d = (Double) args.get(0);
				if(d == 0.0) return(d);
				double r = (Double) args.get(1);
				return(round(d,r));
			}
			case FUNC_roundup:{
				/* need to change */
				double d = (Double) args.get(0);
				double r = (Double) args.get(1);
				d = Math.round(d * 10000000.0) / 10000000.0;
				d = Math.ceil(d * (1.0d / r) - 0.00000001) * r;
				d = Math.round(d * 10000000.0) / 10000000.0;
				return(d);			
			}
			case FUNC_rounddown:{
				double d = (Double) args.get(0);
				double r = (Double) args.get(1);
				d = Math.round(d * 10000000.0) / 10000000.0;
				d = Math.floor(d * (1.0d/ r) + 0.00000001 ) * r;
				d = Math.round(d * 10000000.0) / 10000000.0;
				return(d);				
			}
			case FUNC_ftostr:{
				double d = (Double) args.get(0);
				String s = (String) args.get(1);
				return(StringUtil.ftostr(d, s));				
			}
			case FUNC_eval:{
				String f = (String) args.get(0);
				if(f == null || f.trim().equals("")) return(new IgnoreValue());
				Parser p = new Parser(compareMode,f,this,this);
				try {
					return(p.evaluate());
				} catch (Error ex) {
					CoreLog.log("eval formula error");
					return(0.0);
				}			
			}
			
			case FUNC_deltaDate:{
				Date d0 = (Date) args.get(0);
				int delta = Cell.objectToInt(args.get(1));
				Date d1 = DateUtil.nextday(d0, delta) ;
//				double delta = (Double) args.get(1);
//				Date d1 = DateUtil.nextday(d0, (int) delta) ;
				return(d1);
			}
			case FUNC_today:{
				return(DateUtil.today());
			}
			case FUNC_random :{
				if(rd == null) {
					rd = new Random();
				}
				return(rd.nextDouble());
			}
			case FUNC_now:{
				return(new Date());
			}
			case FUNC_getParentCell:{
				if(getParent() == null) return(null);
				Cell c = getParent().testCell((String) args.get(0));
				if(c == null) return(null);
				return(c.getObject());
			}
			case FUNC_isNull :{
				if(args.get(0) == null) return(true); else return(false);
			}
			case FUNC_strtodate : {
				java.util.Date d = DateUtil.dateTimeStrToDate((String) args.get(0));
				return(d);
			}
			case FUNC_sa : {
				String s = (String) args.get(0);
				return(s.trim());
			}
			case FUNC_maxf : {
				double maxd = 0.0;
				for(Object o : args) {
					if(o instanceof Double) {
						double d = (Double) o;
						if(d > maxd) maxd = d;
					}
					if(o instanceof Integer) {
						int d = (Integer) o;
						double dd = d;
						if(dd > maxd) maxd = dd;
					}
				}
				return(maxd);
			}
			case FUNC_chr2int : {
				String s = (String) args.get(0);
				if(s.length() > 0) {
					char c = s.charAt(0);
					int i = ((int) c) - 32;
					if( i< 0) return(0);
					return(i);
				} else return(0);
			}
			case FUNC_int2chr: {
				double d = (Double) args.get(0);
				int a = (int) d;
				a += 32;
				char c[] = new char[1];
				c[0] = (char) a;
				String s = new String(c);
				return(s);
			}
			case FUNC_age: {
				Date d = (Date) args.get(0);
				if(d == null || !d.after(DateUtil.minDate)) {
					return(0);
				}
				int y0 = DateUtil.getYear(d);
				int m0 = DateUtil.getMonth(d);
				int d0 = DateUtil.getDay(d);
				d = DateUtil.today();
				int y1 = DateUtil.getYear(d);
				int m1 = DateUtil.getMonth(d);
				int d1 = DateUtil.getDay(d);
				int a = y1-y0;
				if(m0 > m1) {
					a--;
				} else if(m0 == m1) {
					if(d0 > d1) a--;
				} 
				return(a);
			}
			case FUNC_yearofdate : {
				Date d = (Date) args.get(0);
				if(d == null) return(0);
				int y = DateUtil.getYear(d);
				return(y);
			}
			case FUNC_monthofdate : {
				Date d = (Date) args.get(0);
				if(d == null) return(0);
				int m = DateUtil.getMonth(d);
				return(m);
			}
			case FUNC_dayofdate : {
				Date d = (Date) args.get(0);
				if(d == null) return(0);
				int m = DateUtil.getDay(d);
				return(m);
			}
			case FUNC_weekdayofdate : {
				Date d = (Date) args.get(0);
				if(d == null) return(0);
				int w = DateUtil.toDayOfWeek(d);
				return(w);
			}
			case FUNC_toSet: {
				HashSet set = new HashSet();
				String str = (String) args.get(0);
				StringTokenizer tk = new StringTokenizer(str, ",");
				for (int i=0; tk.hasMoreTokens(); ) {
					String arg = tk.nextToken().trim();
					if (!arg.trim().equals("")) set.add(arg);
				}
				return(set);
			}
			case FUNC_includeSet: {
				AbstractCollection set0 = (AbstractCollection) args.get(0);
				AbstractCollection set1 = (AbstractCollection) args.get(1);
				if(set0 == null) return(new Boolean(false));
				if(set1 == null) return(new Boolean(true));
				return(set0.containsAll(set1));
			}
			case FUNC_jdate: {
				Date d = (Date) args.get(0);
				int n = 0;
				if(d != null) n = DateUtil.getJulianDate(d);
				return(n);
			}
			case FUNC_zeroDate : {
				return(DateUtil.zeroDate);
			}
			case FUNC_stripNumber: {
				String s =  (String) args.get(0);
				return(StringUtil.stripNumber(s));
			}
			case FUNC_firstLine: {
				String s =  (String) args.get(0);
				int len;
				if(args.size() > 1) {
					double dl = (Double) args.get(1);
					len = (int) dl;
				} else len = -1;
				int idx = s.indexOf('\n');
				if(idx >= 0) {
					if(len < 0 || len > idx) len = idx;
				}
				return(StringUtil.strpart(s, 0, len));
				
			}
			case FUNC_valueof : {
				return(args.get(0));
			}
			case FUNC_dateof: {
				java.util.Date dd = (java.util.Date) (args.get(0));
				if(dd == null) return(DateUtil.zeroDate);
				dd =	DateUtils.truncate(dd, Calendar.DATE);
				return(
						new java.util.Date(
							dd.getTime() + DateUtil.getGmtOffset()
						)
						);
			}
			case FUNC_min: {
				double val = Double.POSITIVE_INFINITY;
				for (int i=0; i<args.size(); i++) {
					if (args.elementAt(i) instanceof Double) {
						double thisval = ((Double) args.elementAt(i)).doubleValue();
						if (thisval < val)
							val = thisval;
					} else if (args.elementAt(i) instanceof Integer) {
						Integer ii = ((Integer) args.elementAt(i)).intValue();
						double thisval = (double) ii;
						if (thisval < val)
							val = thisval;
					}
				}
				return(Double.isInfinite(val) ? Double.NaN: new Double(val));
			}
			case FUNC_max: {
				double val = Double.NEGATIVE_INFINITY;
				for (int i=0; i<args.size(); i++) {
					if (args.elementAt(i) instanceof Double) {
						double thisval = ((Double) args.elementAt(i)).doubleValue();
						if (thisval > val)
							val = thisval;
					} else if (args.elementAt(i) instanceof Integer) {
						Integer ii = ((Integer) args.elementAt(i)).intValue();
						double thisval = (double) ii;
						if (thisval > val)
							val = thisval;
					}
				}
				return(Double.isInfinite(val) ? Double.NaN: new Double(val));
			}
			case FUNC_sum: {
				double val = 0;
				for (int i=0; i<args.size(); i++) {
					if (args.elementAt(i) instanceof Double)
						val += ((Double) args.elementAt(i)).doubleValue();
					else if (args.elementAt(i) instanceof Integer) {
						Integer ii = ((Integer) args.elementAt(i)).intValue();
						val += (double) ii;
					}
				}
				return(new Double(val));
			}
			case FUNC_floor: {
				if (args.elementAt(0) instanceof Integer) {
					Integer ii = ((Integer) args.elementAt(0)).intValue();
					return((double) ii );
				}
				if (args.elementAt(0) instanceof Double)
					return(new Double(Math.floor(((Double) args.elementAt(0)).doubleValue())));
				else
					return(new Double(0));
			}
			case FUNC_ceil: {
				if (args.elementAt(0) instanceof Integer) {
					Integer ii = ((Integer) args.elementAt(0)).intValue();
					return((double) ii );
				}
				if (args.elementAt(0) instanceof Double)
					return(new Double(Math.ceil(((Double) args.elementAt(0)).doubleValue())));
				else
					return(new Double(0));
			}
			case FUNC_isNaN: {
				if (args.elementAt(0) instanceof Double) {
					return(Double.isNaN((Double) args.get(0)));
				} else return(false);
			}
			case FUNC_mapping: {
				int n = args.size();
				Object o = args.get(0);
				for(int i=2;i<n;i+=2){
					if(Cell.objectCompare(o, args.get(i), true) == 0) { 
						return(args.get(i+1));
					}
				}
				return(args.get(1));
			}
			case FUNC_tolower: {
				String s = args.get(0).toString();
				return(s.toLowerCase());
			}
			case FUNC_toupper: {
				String s = args.get(0).toString();
				return(s.toUpperCase());
			}
			case FUNC_stepup: {
				int n = args.size();
				Object o = args.get(0);
				for(int i=2;i<n;i+=2){
					if(Cell.objectCompare(o, args.get(i), true) < 0) { 
						return(args.get(i+1));
					}
				}
				return(args.get(1));
			}
			default:
				//CoreLog.log1("function (%s) not defined", p_fname);
		}
		if(parent != null) 
			return(parent.evalFunction(p_fname,args));
		else
			return(null);
	}
	public Cell getCellNoException(String p_cname) {
		Cell c = testCell(p_cname);
		return(c);
	}
	public Cell getCell(String p_cname)
	{
		Cell c = testCell(p_cname);
		if (c == null) 
		   CoreLog.log(new Exception("getCell " + p_cname + " got null"));
		return(c);
	}
	public Cell testCell(String p_cname)
	{
		/* Cell c; */
		/* will be enhanced to handle full path and relative path variable */
		checkReflected();
		if(p_cname.startsWith("super.")) {
			if(parent != null) return(parent.testCell(p_cname.substring(6))); else return(null);
		}
		if (cellTable != null) {
			Object o = cellTable.get(p_cname);
			if(o != null && o instanceof Cell) return((Cell) o);
		}
	   Field fd = (Field) ((Hashtable) reflectedFieldHashHash.get(getClass())).get(p_cname);
		if (fd != null) {
			try {
			   return((Cell) fd.get(this));
         } catch (IllegalAccessException ex) {
			   CoreLog.log(ex);
			   return(null);
			}
		}
		if (parent != null) 
		   return(parent.testCell(p_cname)); 
		/*
		CoreLog.log(new Exception("getCell " + p_cname + " got null"));
		CoreLog.log("getCell " + p_cname + " got null");
		*/
		return(null);
	}
	public Cell[] getCells(String p_cname, boolean p_fComplaint) {
		Cell c;
		/* will be enhanced to handle full path and relative path variable */
		checkReflected();
		if (cellTable != null) {
			Object o = cellTable.get(p_cname);
			if (o != null && o instanceof Cell[])
			   return((Cell[]) o);
		}
	   Field fd = (Field) ((Hashtable) reflectedFieldHashHash.get(getClass())).get(p_cname);
		if (fd != null) {
			try {
			   return((Cell[]) fd.get(this));
         } catch (IllegalAccessException ex) {
			   CoreLog.log(ex);
			   return(null);
			}
		}
		if (parent != null) 
		   return(parent.getCells(p_cname)); 
		if (p_fComplaint)
		   CoreLog.log("getCells " + p_cname + " got null");
		return(null);
	}
	public Cell[] getCells(String p_cname) {
	   return(getCells(p_cname, true));
	}
	public Cell getCellArray(String p_cname, int p_idx) {
	   Cell[] cellArr = getCells(p_cname);
		if (cellArr == null)
		   return(null);
		try {
		   return(cellArr[p_idx]);
		} catch (Exception ex) {
		   CoreLog.log(ex);
			return(null);
		}
		/*
		Cell c;
		checkReflected();
		if (cellTable != null) {
			Object o = cellTable.get(p_cname);
			if(o != null && o instanceof Cell[]) {
				try {
				   return(((Cell[]) o)[p_idx]);
  	       	} catch (Exception ex) {
			  	 	CoreLog.log(ex);
				   return(null);
				}
			}
		}
	   Field fd = (Field) ((Hashtable) reflectedFieldHashHash.get(getClass())).get(p_cname);
		if (fd != null) {
			try {
			   return(((Cell[]) fd.get(this))[p_idx]);
         } catch (IllegalAccessException ex) {
			   CoreLog.log(ex);
			   return(null);
			}
		}
		if (parent != null) 
		   return(parent.getCellArray(p_cname, p_idx)); 
		CoreLog.log("getCellArray " + p_cname + " got null");
		return(null);
		*/
	}
	public CellCollection getCollection(String p_name) {
		return getCollection(p_name, true);
	}
	/***
	 * 
	 * @param p_name
	 * @param p_multiLevel - try to obtain collection from parent 
	 * @return
	 */
	public CellCollection getCollection(String p_name, boolean p_multiLevelFlag)
	{
		CellCollection c;
		if(collectionTable != null && (c = (CellCollection) collectionTable.get(p_name)) != null) {
			return(c);
		} else if (p_multiLevelFlag){
			if(parent != null) return(parent.getCollection(p_name, p_multiLevelFlag)); else return(null);
		}
		return null;
	}
	/***
	 * get collection at current level. 
	 * will not obtain value from parent.
	 * @param p_name
	 * @return
	 */
	public CellCollection getCollection_SingleLevel(String p_name) {
		return getCollection(p_name, false);
		/*
		//andrew200715: code refine
		CellCollection c;
		if (collectionTable != null && (c = (CellCollection) collectionTable.get(p_name)) != null) 
			return(c);
		else
		   return(null);
		*/
	}
	/***
	 * list the key name of collectionTable
	 * andrew190508: newly added method
	 * @return return null if no key available
	 */
	public String[] listCollection_SingleLevel() {
		if (collectionTable != null && collectionTable.keySet().size() > 0){
			return (String[]) (collectionTable.keySet().toArray(new String[collectionTable.keySet().size()]));
		}
		else{
		   return(null);
		}
	}
	public Vector getCollectionList_SingleLevel(String p_name)
	{
		Vector v;
		if(collectionListTable != null && (v = (Vector) collectionListTable.get(p_name)) != null) {
			return(v);
		} else {
			return(null);
		}
	}
	/***
	 * getCollectionList with type casting
	 * @param p_name
	 * @param p_class
	 * @return
	 */
	public <T> Vector<T> getCollectionList(String p_name, Class<T> p_class){
		return (Vector<T>)getCollectionList(p_name);
	}
	public Vector getCollectionList(String p_name)
	{
		Vector v;
		if(collectionListTable != null && (v = (Vector) collectionListTable.get(p_name)) != null) {
			return(v);
		} else {
			if(parent != null) return(parent.getCollectionList(p_name)); else return(null);
		}
	}
	public CellCollection getParent()
	{
		return(parent);
	}
	public CellCollection getRoot()
	{
		CellCollection col=this;
		while(col.parent != null) col = col.parent;
		return(col);
	}
   public StringBuffer toCellXML(StringBuffer p_sb, Hashtable p_history) {
      return(toCellXMLGen(p_sb, p_history, false, CONV_NONE));
	}
   public StringBuffer toCellXMLRecursive(StringBuffer p_sb, Hashtable p_history) {
      return(toCellXMLGen(p_sb, p_history, true, CONV_NONE));
	}
   public StringBuffer toCellXMLGen(StringBuffer p_sb, Hashtable p_history, boolean p_fRecursive, int p_convOption) {
      return(toCellXMLGen(p_sb, p_history, p_fRecursive, p_convOption, "yyyy/mm/dd"));
	}
   public StringBuffer toCellXMLGen(StringBuffer p_sb, Hashtable p_history, boolean p_fRecursive, int p_convOption, String p_dateFormat) {
	   if (p_history.get(this) != null)
		   return(p_sb);

	   checkReflected();

      p_history.put(this, this);
	   p_sb.append("<CellCollection ");
		if (!p_fRecursive)
		   p_sb.append("hashCode=\"").append(hashCode()).append("\" ");
		p_sb.append("class=\"").append(this.getClass().getName()).append("\"");
		p_sb.append(">");
		if (parent != null && parent.isValid()) {
		   p_sb.append("<parent>").append(parent.hashCode()).append("</parent>");
		}

		p_sb.append("<cellTable>");
		if (cellTable != null) {
		   Vector vv = new VectorUtil(cellTable.keys()).toVector();
			Collections.sort(vv);
		   for (int k=0; k<vv.size(); k++) {
		      String key = (String) vv.elementAt(k);
				if(cellTable.get(key) instanceof Cell) {
			      Cell cell = (Cell) cellTable.get(key);
					p_sb.append("<").append(key)
					    .append(" t=\"").append(cell.getTypeInXmlAndAttribute(key));
					Hashtable h = cell.getAttributes();
					if (h != null) {
					   for (Enumeration en=h.keys(); en.hasMoreElements(); ) {
						   String attrName = (String) en.nextElement();
							p_sb.append("\" ")
							    .append(attrName)
								 .append("=\"")
								 .append(StringUtil.cws(h.get(attrName).toString()))
							    ;
						}
					}
					p_sb.append("\">");
	            cell2String(p_sb, cell, p_dateFormat, p_convOption, key);
					p_sb.append("</").append(key).append(">");
				} else {
					if(cellTable.get(key) instanceof Cell[]) {
              	 Cell[] cells = (Cell[]) cellTable.get(key);
				  	 if (cells != null) {
		        	    p_sb.append("<").append(key).append(" array=\"true\">");
						   for (int i=0; i<cells.length; i++) {
              	       if (cells[i] != null) {
		        	          p_sb.append("<entry idx=\"").append(i).append("\" ")
                             .append("t=\"").append((cells[i]).getTypeInXmlAndAttribute(key)).append("\"")
									  .append(">");
	                      cell2String(p_sb, cells[i], p_dateFormat, p_convOption, key);
		        	          p_sb.append("</entry>");
								}
								else{
		        	          p_sb.append("<entry idx=\"").append(i).append("\"/>");
								}
							}
		        	    p_sb.append("</").append(key).append(">");
				  	 }
					}
				}
		   }
		}
      Hashtable h = (Hashtable) reflectedFieldHashHash.get(getClass());
		if (h != null) {
		   Vector vv = new VectorUtil(h.keys()).toVector();
			Collections.sort(vv);
		   for (int k=0; k<vv.size(); k++) {
		      String cname = (String) vv.elementAt(k);
            Field fd = (Field) h.get(cname);
            if (fd != null) {
               try {
		            Object obj = fd.get(this);
						if (obj instanceof Cell[]) {
		               Cell[] cells = (Cell[]) obj;
						   if (cells != null) {
				            p_sb.append("<").append(cname).append(" array=\"true\">");
							   for (int i=0; i<cells.length; i++) {
		                     if (cells[i] != null) {
				                  p_sb.append("<entry idx=\"").append(i).append("\" ")
                                  .append("t=\"").append(cells[i].getTypeInXmlAndAttribute(cname)).append("\"")
										    .append(">");
	                           cell2String(p_sb, cells[i], p_dateFormat, p_convOption, cname);
				                  p_sb.append("</entry>");
									}
									else{
				                  p_sb.append("<entry idx=\"").append(i).append("\"/>");
									}
								}
				            p_sb.append("</").append(cname).append(">");
						   }
						}
						else if (obj instanceof Cell) {
		               Cell cell = (Cell) obj;
						   if (cell != null) {
				            p_sb.append("<").append(cname)
                            .append(" t=\"").append(cell.getTypeInXmlAndAttribute(cname)).append("\"")
								    .append(">");
	                     cell2String(p_sb, cell, p_dateFormat, p_convOption, cname);
				            p_sb.append("</").append(cname).append(">");
						   }
						}
               } catch (IllegalAccessException ex) {
                  CoreLog.log(ex);
               }
            }
		   }
		}
		p_sb.append("</cellTable>");

		p_sb.append("<collectionTable>");
		if (collectionTable != null) {
		   Vector vv = new VectorUtil(collectionTable.keys()).toVector();
			Collections.sort(vv);
		   for (int k=0; k<vv.size(); k++) {
		      String key = (String) vv.elementAt(k);
		      CellCollection cl = (CellCollection) collectionTable.get(key);
            if (p_fRecursive) {
				   if (p_history.get(cl) == null) {
		            p_sb.append("<collectionTableItem name=\"")
						    .append(key)
							 .append("\">");
				      if (cl.isValid())
                     cl.toCellXMLGen(p_sb, p_history, true, p_convOption, p_dateFormat);
					}
               else {
		            p_sb.append("<collectionTableItem name=\"")
						    .append(key)
							 .append("\" appeared=\"true\"")
							 .append(">");
					}
		         p_sb.append("</collectionTableItem>");
				}
				else {
		          p_sb.append("<hashCode name=\""+key+"\">")
				        .append(cl.isValid() ? ""+cl.hashCode() : "")
					     .append("</hashCode>");
			   }
		   }
		}
		p_sb.append("</collectionTable>");

		p_sb.append("<collectionListTable>");
		if (collectionListTable != null) {
		   Vector vv = new VectorUtil(collectionListTable.keys()).toVector();
			Collections.sort(vv);
		   for (int k=0; k<vv.size(); k++) {
		      String key = (String) vv.elementAt(k);
			   CellVector v = (CellVector) collectionListTable.get(key);
				if(v.notvalid) continue;
				p_sb.append("<").append(key).append(">");
			   for (int i=0; i<v.size(); i++) {
				  if(!(v.elementAt(i) instanceof CellCollection)){
					  CoreLog.log("ignore non cellcollection element");
					  continue;
				  }
			      CellCollection cl = (CellCollection) v.elementAt(i);
					if(cl == null || !cl.isValid()) {
                  if (p_fRecursive)
		         	   p_sb.append("<collectionListTableItem idx=\""+"\">").append("</collectionListTableItem>");
						else
		         	   p_sb.append("<hashCode idx=\""+"\">").append("</hashCode>");
					} else {
                  if (p_fRecursive) {
				         if (p_history.get(cl) == null) {
		         	      p_sb.append("<collectionListTableItem idx=\"")
							       .append(i)
								    .append("\">");
                        cl.toCellXMLGen(p_sb, p_history, true, p_convOption, p_dateFormat);
							}
							else {
		         	      p_sb.append("<collectionListTableItem idx=\"")
							       .append(i)
									 .append("\" appeared=\"true\"")
								    .append(">");
							}
						   p_sb.append("</collectionListTableItem>");
						}
						else
		         	   p_sb.append("<hashCode idx=\""+i+"\">").append(cl.hashCode()).append("</hashCode>");
					}
			   }
				p_sb.append("</").append(key).append(">");
		   }
		}
		p_sb.append("</collectionListTable>");
	   p_sb.append("</CellCollection>");

		if (p_fRecursive)
		   return(p_sb);

	   if (parent != null && parent.isValid())
         parent.toCellXML(p_sb, p_history);
		if (collectionTable != null) {
		   for (Enumeration en=collectionTable.elements(); en.hasMoreElements(); ) {
		      CellCollection cl = (CellCollection) en.nextElement();
				if (cl.isValid())
               cl.toCellXMLGen(p_sb, p_history, false, p_convOption, p_dateFormat);
		   }
		}
		if (collectionListTable != null) {
		   for (Enumeration en=collectionListTable.elements(); en.hasMoreElements(); ) {
			   Vector v = (Vector) en.nextElement();
			   for (int i=0; i<v.size(); i++) {
				  if(!(v.elementAt(i) instanceof CellCollection)){
					  CoreLog.log("ignore non cellcollection element");
					  continue;
				  }
			      CellCollection cl = (CellCollection) v.elementAt(i);
					if (cl != null && cl.isValid()) 
                  cl.toCellXMLGen(p_sb, p_history, false, p_convOption, p_dateFormat);
			   }
		   }
      }

		return(p_sb);
	}
	public StringBuffer toXML(StringBuffer p_sb) {
	   return(toXMLGen(p_sb, false, CONV_NONE));
	}
	public StringBuffer toXMLRecursive(StringBuffer p_sb) {
	   return(toXMLGen(p_sb, true, CONV_NONE));
	}
	public StringBuffer toXMLGen(StringBuffer p_sb, boolean p_fRecursive, int p_convOption) {
	   return toXMLGen(p_sb, p_fRecursive, p_convOption, "yyyy/mm/dd");
	}
	public StringBuffer toXMLGen(StringBuffer p_sb, boolean p_fRecursive, int p_convOption, String p_dateFormat) {
	   if (valid) {
	      p_sb.append("<CellCollections ");
	      if (!p_fRecursive)
		      p_sb.append("start=\"").append(hashCode()).append("\"");
			p_sb.append(" timestamp=\"").append(DateUtil.toTimeString(new java.util.Date(), "yyyy/mm/dd HH:MM:SS")).append("\"");
		   p_sb.append(">");
         toCellXMLGen(p_sb, new Hashtable(), p_fRecursive, p_convOption, p_dateFormat);
	      p_sb.append("</CellCollections>");
		}
		return(p_sb);
	}
	private static synchronized void makeReflected(CellCollection p_instance) {
	   if (reflectedFieldHashHash.get(p_instance.getClass()) != null)
		   return;
		Class cl = null;
		try {
		   cl = Class.forName("com.uniinformation.cell.Cell");
		} catch (Exception ex) {
		   CoreLog.log(ex);
			return;
		}
	   Hashtable reflectedFieldHash = new Hashtable();
		Field[] fds = p_instance.getClass().getFields();
      for (int i=0; i<fds.length; i++) {
		   Field fd = fds[i];
			if (fd.getType().isArray() && cl.isAssignableFrom(fd.getType().getComponentType())) {
            //CoreLog.logClass(p_instance, "makeReflected: reflecting got array " + fd.getName());
		      reflectedFieldHash.put(fd.getName(), fd);
			}
		   else if (cl.isAssignableFrom(fd.getType())) {
            //CoreLog.logClass(p_instance, "makeReflected: reflecting got " + fd.getName());
		      reflectedFieldHash.put(fd.getName(), fd);
			}
		}
	   reflectedFieldHashHash.put(p_instance.getClass(), reflectedFieldHash);
	}
	private void checkReflected() {
	   if (reflectedFieldHashHash.get(getClass()) != null)
		   return;
	   makeReflected(this);
	}
	public void setValid(boolean p_sw)
	{
		valid = p_sw;		
	}
	public boolean isValid() {
	   return(valid);
	}
	void traverseCollection_start(boolean p_topdown,Hashtable ht,TraverseInterface p_traverse) throws CellException
	{
		checkReflected();
		if(ht.get(this) != null) return;
		ht.put(this,this);
		if(p_topdown) {
			boolean valid = p_traverse.traverseOne(this);
			if(!valid) return;
		}
		if(collectionTable != null) {
			for(Enumeration e = collectionTable.elements();e.hasMoreElements();){
				CellCollection col = (CellCollection) e.nextElement();
				if(col != null) col.traverseCollection_start(p_topdown,ht,p_traverse);
			}
		}
		if(collectionListTable != null) {
			for(Enumeration e = collectionListTable.elements();e.hasMoreElements();){
				CellVector v = (CellVector) e.nextElement();
				for(int i = 0;i < v.size();i++) {
					CellCollection col = (CellCollection) v.get(i);
					if(col != null) col.traverseCollection_start(p_topdown,ht,p_traverse);
				}
			}
		}
		if(!p_topdown) p_traverse.traverseOne(this);
	}
	public void traverseCollection(boolean p_topdown,TraverseInterface p_traverse) throws CellException
	{
		Hashtable ht = new Hashtable();
		traverseCollection_start(p_topdown,ht,p_traverse);
	}
	void traverseCollection2_start(CellCollection parent,String name,int index,boolean p_topdown,Hashtable ht,TraverseInterface2 p_traverse) throws CellException
	{
		checkReflected();
		if(ht.get(this) != null) return;
		ht.put(this,this);
		if(p_topdown) {
			boolean valid = p_traverse.traverseOne(parent,name,index,this);
			if(!valid) return;
		}
		if(collectionTable != null) {
			for(String key : collectionTable.keySet()) {
				CellCollection col = collectionTable.get(key);
				col.traverseCollection2_start(this,key,-1,p_topdown,ht,p_traverse);
			}
		}
		if(collectionListTable != null) {
			for(Object key : collectionListTable.keySet()) {
				CellVector v = (CellVector) collectionListTable.get(key);
				for(int i = 0;i < v.size();i++) {
					CellCollection col = (CellCollection) v.get(i);
					if(col != null) col.traverseCollection2_start(this,key.toString(),i,p_topdown,ht,p_traverse);
				}
			}
		}
		if(!p_topdown) p_traverse.traverseOne(parent,name,index,this);
	}
	public void traverseCollection2(boolean p_topdown,TraverseInterface2 p_traverse) throws CellException
	{
		Hashtable ht = new Hashtable();
		traverseCollection2_start(null,null,-1,p_topdown,ht,p_traverse);
	}
	/*
	private void checkReflected() {
		if (reflected) 
		   return;
		reflected = true;
		if(cellTable == null) cellTable = new Hashtable();
		Field[] fds = getClass().getFields();
      for (int i=0; i<fds.length; i++) {
		   Field fd = fds[i];
			try {
			   Object obj = fd.get(this);
				if (obj != null) {
			      if (obj instanceof Cell) {
						//CoreLog.log("HAHA__ reflecting got " + fd.getName());
	               cellTable.put(fd.getName(), obj);
			      }
				}
			} catch (Exception ex) {
			   CoreLog.log(ex);
			}
		}
	}
	*/
	public void browse(CellCollectionBrowserInterface p_br, Hashtable p_history) throws Exception {
	   if (p_history.get(this) != null)
		   return;
	   checkReflected();
      p_history.put(this, this);
		if (cellTable != null) {
			for (Enumeration en=cellTable.keys(); en.hasMoreElements(); ) {
		      String key = (String) en.nextElement();
				if (cellTable.get(key) instanceof Cell) {
				   p_br.gotCell(key, (Cell) cellTable.get(key));
				} 
				else if (cellTable.get(key) instanceof Cell[]) {
				   p_br.gotCellArray(key, (Cell[]) cellTable.get(key));
				}
		   }
		}
      Hashtable h = (Hashtable) reflectedFieldHashHash.get(getClass());
		if (h != null) {
		   for (Enumeration en=h.keys(); en.hasMoreElements(); ) {
		      String cname = (String) en.nextElement();
            Field fd = (Field) h.get(cname);
            if (fd != null) {
               try {
		            Object obj = fd.get(this);
						if (obj instanceof Cell[]) {
							if (obj != null)
				            p_br.gotCellArray(cname, (Cell[]) obj);
						}
						else if (obj instanceof Cell) {
							if (obj != null)
				            p_br.gotCell(cname, (Cell) obj);
						}
               } catch (IllegalAccessException ex) {
                  CoreLog.log(ex);
               }
            }
		   }
		}

		if (collectionTable != null) {
			for (Enumeration en=collectionTable.keys(); en.hasMoreElements(); ) {
		      String key = (String) en.nextElement();
		      CellCollection cl = (CellCollection) collectionTable.get(key);
				p_br.gotCollection(key, cl);
		   }
		}

		if (collectionListTable != null) {
		   for (Enumeration en=collectionListTable.keys(); en.hasMoreElements(); ) {
		      String key = (String) en.nextElement();
			   Vector v = (Vector) collectionListTable.get(key);
				p_br.gotCollectionList(key, v);
		   }
		}
	}
	public CellCollection putValuesFromHash(Hashtable p_hash) throws Exception {
	   for (Enumeration en=p_hash.keys(); en.hasMoreElements(); ) {
		   String name = (String) en.nextElement();
	      putValue(name, p_hash.get(name));
		}
		return(this);
	}
	public CellCollection putValue(String p_name, Object p_object) throws Exception {
	   if (p_object instanceof String)
	      return(putValue(p_name, (String) p_object));
	   else if (p_object instanceof Float)
	      return(putValue(p_name, ((Float) p_object).doubleValue()));
	   else if (p_object instanceof Double)
	      return(putValue(p_name, ((Double) p_object).doubleValue()));
	   else if (p_object instanceof Long)
	      return(putValue(p_name, ((Long) p_object).intValue()));
	   else if (p_object instanceof Integer)
	      return(putValue(p_name, ((Integer) p_object).intValue()));
	   else if (p_object instanceof Boolean)
	      return(putValue(p_name, ((Boolean) p_object).booleanValue()));
	   else if (p_object instanceof java.util.Date)
	      return(putValue(p_name, (java.util.Date) p_object));
	   else if (p_object instanceof byte[])
	      return(putValue(p_name, (byte[]) p_object));
	   throw(new Exception("Unknown object type "+p_object.getClass().getName()));
	}
	public CellCollection putValue(String p_name, long p_value) {
	   addCell(p_name, new Cell((int) p_value));
		return(this);
	}
	public CellCollection putValue(String p_name, int p_value) {
	   addCell(p_name, new Cell(p_value));
		return(this);
	}
	public CellCollection putValue(String p_name, double p_value) {
	   addCell(p_name, new Cell(p_value));
		return(this);
	}
	public CellCollection putValue(String p_name, String p_value) {
	   addCell(p_name, new Cell(p_value));
		return(this);
	}
	public CellCollection putValue(String p_name, boolean p_value) {
	   addCell(p_name, new Cell(p_value));
		return(this);
	}
	public CellCollection putValue(String p_name, byte[] p_value) {
	   addCell(p_name, new Cell(p_value));
		return(this);
	}
	public CellCollection putValue(String p_name, java.util.Date p_value) {
	   addCell(p_name, new Cell(p_value));
		return(this);
	}
	public CellCollection putValue(String p_name, CellObjWrapper p_value) {
	   addCell(p_name, new Cell(p_value));
		return(this);
	}
	public boolean hasValue(String p_name) {
	   Cell cell = getCell(p_name);
		return(cell != null);
	}
	public java.util.Date getDate(String p_name) {
	   Cell cell = getCell(p_name);
		if (cell == null)
		   return(null);
	   return(cell.getDate());
	}
	public double getDouble(String p_name) {
	   Cell cell = getCell(p_name);
		if (cell == null)
		   return(0.0);
	   return(cell.getDouble());
	}
	public int getInt(String p_name) {
	   Cell cell = getCell(p_name);
		if (cell == null)
		   return(0);
	   return(cell.getInt());
	}
	public String getString(String p_name) {
	   Cell cell = getCell(p_name);
		if (cell == null)
		   return(null);
	   return(cell.getString());
	}
	public boolean getBoolean(String p_name) {
	   Cell cell = getCell(p_name);
		if (cell == null)
		   return(false);
	   return(cell.getBoolean());
	}
	public byte[] getByteArray(String p_name) {
	   Cell cell = getCell(p_name);
		if (cell == null)
		   return(null);
	   return(cell.getByteArray());
	}
	public CellObjWrapper getObjWrapper(String p_name) {
	   Cell cell = getCell(p_name);
		if (cell == null)
		   return(null);
	   return(cell.getObjWrapper());
	}
   public VectorUtil saveToVectorUtil(String p_name, VectorUtil p_vu, Hashtable p_history) throws Exception {
	   final VectorUtil vu = p_vu;
	   final Hashtable history = p_history;
		vu.addElement(TYPE_OBJECT_START);
		vu.addElement(p_name);
		vu.addElement(getClass().getName());
	   CellCollectionBrowserInterface browser = new CellCollectionBrowserInterface() {
			private void putCell(VectorUtil p_vu, Cell p_cell, String p_cellName) throws Exception {
			   switch (p_cell.getType()) {
	            case Cell.VTYPE_INT:
			         p_vu.addElement(p_cell.getInt());
						break;
	            case Cell.VTYPE_STRING:
			         p_vu.addElement(p_cell.getString());
						break;
	            case Cell.VTYPE_DOUBLE:
			         p_vu.addElement(p_cell.getDouble());
						break;
	            case Cell.VTYPE_BOOLEAN:
			         p_vu.addElement(p_cell.getBoolean() ? "True" : "False");
                  break;
	            case Cell.VTYPE_DATE:
			         p_vu.addElement(p_cell.getDate());
						break;
					default:
						throw(new Exception("Invalid Cell type "+p_cellName+":"+p_cell.getType()));
				}
			}
	      public void gotCell(String p_cellName, Cell p_cell) throws Exception {
			   vu.addElement(TYPE_SCALAR);
			   vu.addElement(p_cellName);
			   putCell(vu, p_cell, p_cellName);
	      }
	      public void gotCellArray(String p_cellName, Cell[] p_cellArray) throws Exception {
			   vu.addElement(TYPE_VARSCALARARRAY_START);
			   vu.addElement(p_cellName);
				for (int i=0; p_cellArray != null && i<p_cellArray.length; i++) {
			      vu.addElement(TYPE_SCALAR);
			      putCell(vu, p_cellArray[i], p_cellName);
				}
			   vu.addElement(TYPE_VARSCALARARRAY_END);
	      }
	      public void gotCollection(String p_cellName, CellCollection p_collection) throws Exception {
			   if (history.get(p_collection) != null) {
				   CoreLog.logClass(this, "saveToVectorUtil(): gotCollection(): browsed already "+p_cellName);
				   return;
				}
            p_collection.saveToVectorUtil(p_cellName, vu, history);
	      }
	      public void gotCollectionList(String p_cellName, Vector p_collectionList) throws Exception {
			   vu.addElement(TYPE_ARRAY);
			   vu.addElement(p_cellName);
			   vu.addElement(p_collectionList.size());
				for (int i=0; i<p_collectionList.size(); i++) {
				   CellCollection oo = (CellCollection) p_collectionList.elementAt(i);
               oo.saveToVectorUtil(p_cellName+"_"+i, vu, history);
				}
	      }
		};
		browse(browser, history);
		vu.addElement(TYPE_OBJECT_END);
	   return(vu);
	}
   public static int parseFromVector(Object p_parent, Vector p_v, int p_at) throws Exception {
		String varname = (String) p_v.elementAt(p_at+1);
	   switch (((Integer) p_v.elementAt(p_at)).intValue()) {
		   case TYPE_OBJECT_START:
            CellCollection olobj;
				String objectType = (String) p_v.elementAt(p_at+2);
				if (objectType.trim().equals(""))
               olobj = new CellCollection();
				else {
               Class myclass = Class.forName(objectType);
               Constructor constructor = myclass.getConstructor((Class []) null);
               if (constructor == null)
				      throw(new Exception("Cannot find constructor for "+objectType));
               olobj = (CellCollection) constructor.newInstance();
				}
				if (p_parent instanceof CellVector) {
				   CellVector cv = (CellVector) p_parent;
				   cv.addElement(olobj);
				}
				else if (p_parent instanceof CellCollection) {
				   CellCollection cc = (CellCollection) p_parent;
					cc.addCollection(varname, olobj);
				}
				int tmpidx = p_at+3;
				for (;;) {
					int what = ((Integer) p_v.elementAt(tmpidx)).intValue();
					if (what == TYPE_OBJECT_END) {
					   tmpidx++;
						break;
					}
					else if (what == TYPE_ARRAY) {
				      CellVector cv = new CellVector();
						String cvName = (String) p_v.elementAt(tmpidx+1);
						olobj.addCollectionList(cvName, cv);
						int cnt = ((Integer) p_v.elementAt(tmpidx+2)).intValue();
						tmpidx += 3;
						for (int i=0; i<cnt; i++) {
						   tmpidx = parseFromVector(cv, p_v, tmpidx);
						}
					}
					else if (what == TYPE_VARARRAY_START) {
				      CellVector cv = new CellVector();
						String cvName = (String) p_v.elementAt(tmpidx+1);
						olobj.addCollectionList(cvName, cv);
						tmpidx += 2;
						for (;;) {
						   if (((Integer) p_v.elementAt(tmpidx)).intValue() == TYPE_VARARRAY_END) {
							   tmpidx++;
							   break;
							}
						   tmpidx = parseFromVector(cv, p_v, tmpidx);
						}
					}
					else if (what == TYPE_SCALAR) {
                  tmpidx = parseFromVector(olobj, p_v, tmpidx);
					}
					else if (what == TYPE_OBJECT_START) {
                  tmpidx = parseFromVector(olobj, p_v, tmpidx);
					}
					else if (what == TYPE_VARSCALARARRAY_START) {
						String cvName = (String) p_v.elementAt(tmpidx+1);
				      Vector v = new CellVector();
						tmpidx += 2;
						for (;;) {
						   if (((Integer) p_v.elementAt(tmpidx)).intValue() == TYPE_VARSCALARARRAY_END) {
							   tmpidx++;
							   break;
							}
							v.addElement(Cell.newCell(p_v.elementAt(tmpidx+1)));
							tmpidx += 2;
						}
						Cell[] cells = new Cell[v.size()];
						for (int i=0; i<v.size(); i++)
						   cells[i] = (Cell) v.elementAt(i);
						olobj.addCell(cvName, cells);
					}
				}
				return(tmpidx);
		   case TYPE_SCALAR:
			   Cell cell = null;
				cell = Cell.newCell(p_v.elementAt(p_at+2));
				if (p_parent instanceof CellVector) {
				   CellVector cv = (CellVector) p_parent;
				   cv.addElement(cell);
				}
				else if (p_parent instanceof CellCollection) {
				   CellCollection cc = (CellCollection) p_parent;
					cc.addCell(varname, cell);
				}
			   return(p_at+3);
		}
	   throw(new Exception("parseFromVector(): unknown stream ["+p_at+"]"+p_v));
	}
	/*
	public void copy(CellCollection p_target) throws Exception {
		final CellCollection target = p_target;
	   CellCollectionBrowserInterface br = new CellCollectionBrowserInterface() {
	      public void gotCell(String p_cellName, Cell p_cell) throws Exception {
	         target.addCell(p_cellName, (Cell) p_cell.clone());
			}
	      public void gotCellArray(String p_cellName, Cell[] p_cellArray) throws Exception {
			   Cell[] ca = new Cell[p_cellArray.length];
				for (int i=0; i<ca.length; i++) {
				   ca[i] = (Cell) p_cellArray[i].clone();
				}
	         target.addCell(p_cellName, ca);
			}
	      public void gotCollection(String p_cellName, CellCollection p_collection) throws Exception {
	         target.addCollection(p_cellName, (CellCollection) p_collection.clone());
			}
	      public void gotCollectionList(String p_cellName, Vector p_collectionList) throws Exception {
			   CellVector v = new CellVector();
			   for (int i=0; i<p_collectionList.size(); i++) {
				   v.addElement(((CellCollection) p_collectionList.elementAt(i)).clone());
				}
			   target.addCollectionList(p_cellName, v);
			}
		};
	   browse(br, new Hashtable());
	}
	public Object clone() {
		try {
         Constructor constructor = getClass().getConstructor((Class []) null);
         if (constructor == null)
			   throw(new Exception("Cannot find constructor for "+getClass().getName()));
         CellCollection obj = (CellCollection) constructor.newInstance(null);
		   copy(obj);
	      return(obj);
		} catch (Exception ex) {
		   CoreLog.log(ex);
		   return(null);
		}
	}
	*/
	public Object clone() throws CloneNotSupportedException {
      File tmpFile = null;
		try {
         tmpFile = File.createTempFile("CCCL", null);
	      FileOutputStream baos = new FileOutputStream(tmpFile);
		   ObjectOutputStream oos = new ObjectOutputStream(baos);
		   oos.writeObject(this);
			baos.close();
	      FileInputStream bais = new FileInputStream(tmpFile);
	      ObjectInputStream ois = new ObjectInputStream(bais);
		   Object obj = ois.readObject();
		   ois.close();
		   bais.close();
		   oos.close();
	      return(obj);
		} catch (Exception ex) {
			CoreLog.log(ex);
		   throw(new CloneNotSupportedException(ex.toString()));
		} finally {
		   if (tmpFile != null) {
			   tmpFile.delete();
		   }
		}
	}
	public String getClassName() {
	   return(getClass().getName());
	}
	public CellCollection copy() throws Exception {
	   return(copy(new CellCollection()));
	}
	public CellCollection copy(CellCollection p_cc) throws Exception {
		final CellCollection cc = p_cc;
      CellCollectionBrowserInterface browser = 
		   new CellCollectionBrowserInterface() {
	         public void gotCell(String p_cellName, Cell p_cell) throws Exception {
	            cc.addCell(p_cellName, (Cell) p_cell.clone());
				}
	         public void gotCellArray(String p_cellName, Cell[] p_cellArray) throws Exception {
				   Cell[] cs = new Cell[p_cellArray.length];
					for (int i=0; i<p_cellArray.length; i++)
					   cs[i] = (Cell) p_cellArray[i].clone();
				   cc.addCell(p_cellName, cs);
				}
	         public void gotCollection(String p_cellName, CellCollection p_collection) throws Exception {
				   cc.addCollection(p_cellName, p_collection.copy());
				}
	         public void gotCollectionList(String p_cellName, Vector p_collectionList) throws Exception {
					CellVector cv = new CellVector();
					for (int i=0; i<p_collectionList.size(); i++)
					   cv.addElement(((CellCollection) p_collectionList.elementAt(i)).copy());
				   cc.addCollectionList(p_cellName, cv);
				}
			};
	   browse(browser, new Hashtable());
	   return(cc);
	}
	public static StringBuffer cell2String(StringBuffer p_sb, Cell p_cell, String p_dateFormat, int p_convOption, String p_cellName) {
	   return(cell2String(p_sb, p_cell, p_dateFormat, p_convOption, p_cellName, true));
	}
	public static StringBuffer cell2String(StringBuffer p_sb, Cell p_cell, String p_dateFormat, int p_convOption, String p_cellName, boolean p_fcws) {
      if (p_cell.getType() == Cell.VTYPE_DATE)
         p_sb.append(DateUtil.toDateString(p_cell.getDate(), p_dateFormat));
		else {
			String str = null;
         switch (p_convOption) {
            case CONV_ANY2B:
		         str = ChineseConvert.convertAuto2B(p_cell.getString());
               break;
            case CONV_ANY2G:
		         str = ChineseConvert.convertAuto2G(p_cell.getString());
               break;
            default:
		         str = p_cell.getString();
               break;
         }
			if (p_cellName.endsWith("_cdata")) {
			   p_sb.append("<![CDATA[\n");
				p_sb.append(StringUtil.cws2(str));
			   p_sb.append("\n]]>");
			}
			else {
				if (p_fcws)
		         p_sb.append(StringUtil.cws(str));
			   else
		         p_sb.append(str);
			}
		}
		return(p_sb);
	}
	public Vector getCollectionListByPath(String p_path) {
	   return(getCollectionListByPathGen(p_path, new Hashtable()));
	}
	public Vector getCollectionListByPathGen(String p_path, Hashtable p_footage) {
	   String path = p_path;
	   if (path == null)
		   return(null);
		for (;;) {
         if (path.startsWith("/"))
		      path = StringUtil.strpart(path, 1, -1);
		   else
			   break;
      }
		if (path.equals(""))
		   return(new VectorUtil().addElement(this).toVector());
		String node = null;
		if (path.indexOf("/") > 0) 
		   node = StringUtil.strpart(path, 0, path.indexOf("/"));
		else
		   node = path;
      Vector v = getCollectionList_SingleLevel(node);
		CellCollection cc = getCollection_SingleLevel(node);
	   if (v == null && cc == null)
		   return(null);
	   if (cc != null) {
		   if (v == null)
			   v = new Vector();
		   v.addElement(cc);
	   }
		for (int i=v.size()-1; i>=0; i--) {
		   CellCollection tmpCc = (CellCollection) v.elementAt(i);
			if (p_footage.get(tmpCc) == null)
			   p_footage.put(tmpCc, "");
		   else
		      v.remove(i);
		}
		String nextPath = StringUtil.strpart(path, node.length(), -1);
		if (nextPath.equals("")) {
		   if (v.size() > 0)
		      return(v);
		   else
			   return(null);
		}
		VectorUtil vu = new VectorUtil();
		for (int i=v.size()-1; i>=0; i--) {
		   CellCollection tmpCc = (CellCollection) v.elementAt(i);
			Vector v0 = tmpCc.getCollectionListByPathGen(nextPath, p_footage);
		   if (v0 != null)
			   vu.addElements(v0);
		}
		return(vu.toVector().size() > 0 ? vu.toVector() : null);
	}
	public static CellCollection getSampleCc() throws Exception {
		CellCollection cc = new CellCollection();
		cc.putValue("intcell", 1);
		cc.putValue("stringcell", "abcde");
		cc.putValue("doublecell", (double) 123.456);
		cc.putValue("datecell", new Date());
		cc.putValue("booleancell", true);

		Cell[] cells = new Cell[2];
		cells[0] = new Cell(1);
		cells[1] = new Cell(2);
		cc.addCell("intcells", cells);

		cells = new Cell[2];
		cells[0] = new Cell("string1");
		cells[1] = new Cell("string2");
		cc.addCell("stringcells", cells);

		cells = new Cell[2];
		cells[0] = new Cell((double) 1.1);
		cells[1] = new Cell((double) 2.2);
		cc.addCell("doublecells", cells);

		cells = new Cell[2];
		cells[0] = new Cell(new Date());
		cells[1] = new Cell(new Date(new Date().getTime()+86400000));
		cc.addCell("datecells", cells);

		cells = new Cell[2];
		cells[0] = new Cell(true);
		cells[1] = new Cell(false);
		cc.addCell("booleancells", cells);
		return(cc);
	}
	public static void main_xxx(String[] args) throws Exception {
		/*
	   CellCollection cc = new CellCollection();
	   CoreLog.log("cc.testCell() return "+cc.testCell("colnames"));
	   Cell[] colnames = new Cell[1];
		colnames[0] = new Cell(10);
		cc.addCell("colnames", colnames);
	   CoreLog.log("cc.testCell() return "+cc.testCell("colnames"));
	   CoreLog.log("cc.getCells() return "+cc.getCells("colnames"));
	   CoreLog.log("cc.getCells() return "+cc.getCells("abcde"));
	   CellCollection cc = getSampleCc();
	   cc.addCollection("cc0", getSampleCc());
		CellVector cv = new CellVector();
		cv.addElement(getSampleCc());
		cv.addElement(getSampleCc());
	   cc.addCollectionList("cclist0", cv);
      VectorUtil vu = cc.saveToVectorUtil("xxcc", new VectorUtil(), new Hashtable());
	   CoreLog.log("vu.toVector() = "+vu.toVector());
	   CoreLog.log("vu.toVector().size() = "+vu.toVector().size());

      CellCollection cc1 = new CellCollection();
      int idx = parseFromVector(cc1, vu.toVector(), 0);
		CoreLog.log("idx="+idx);
		CoreLog.log("cc1="+cc1.toXMLRecursive(new StringBuffer()).toString());
		*/
		CellCollection cc = new CellCollection();
		cc.putValue("v1", 1);
		cc.putValue("v2", 2);
		Cell[] cells = new Cell[2];
		cells[0] = new Cell(10);
		cells[1] = new Cell(20);
	   cc.addCell("arraya", cells);
		CellCollection cc0 = new CellCollection();
		cc0.putValue("vb1", 10);
		cc0.putValue("vb2", 20);
		cc.addCollection("cc0", cc0);
		CellVector cv0 = new CellVector();
		CellCollection ccv0 = new CellCollection();
		ccv0.putValue("cvv0a", "abcde");
		ccv0.putValue("cvv0b", "defgh");
		cv0.addElement(ccv0);
		CellCollection ccv1 = new CellCollection();
		ccv1.putValue("cvv1a", "abcde");
		ccv1.putValue("cvv1b", "defgh");
		ccv1.putValue("cvv1a", "abcde");
		cv0.addElement(ccv1);
		cc.addCollectionList("cv0", cv0);
      VectorUtil vu = cc.saveToVectorUtil("xxcc", 
		                    new VectorUtil(), new Hashtable());
	   CoreLog.log("vu.toVector() = "+vu.toVector());
	   CoreLog.log("vu.toVector().size() = "+vu.toVector().size());
	}
	/*
	public static void main(String[] args) throws Exception {
	   CellCollection cc = new CellCollection();
	   CellCollection cc_c0 = new CellCollection();
		cc_c0.putValue("name", "cc_c0");
		cc.addCollectionList("c0", (CellVector) new VectorUtil(new CellVectocr()).addElement(cc_c0).toVector());
	   CellCollection cc_c0_c0 = new CellCollection();
		cc_c0_c0.putValue("name", "cc_c0_c0");
		cc_c0.addCollectionList("c0", (CellVector) new VectorUtil(new CellVector()).addElement(cc_c0_c0).toVector());
	   CoreLog.log(args[0]+"="+cc.getCollectionListByPath(args[0]));
	}
	*/
	public Hashtable getCellTable()
	{
		checkReflected();
		return(cellTable);
	}
	public String getOriginalString(String p_name) {
	   Cell cell = testCell(p_name+"_original");
		if (cell != null)
	      return(cell.getString());
	   return(getString(p_name));
	}
	public int getOriginalInt(String p_name) {
	   Cell cell = testCell(p_name+"_original");
		if (cell != null)
	      return(cell.getInt());
	   return(getInt(p_name));
	}
	public boolean getOriginalBoolean(String p_name) {
	   Cell cell = testCell(p_name+"_original");
		if (cell != null)
	      return(cell.getBoolean());
	   return(getBoolean(p_name));
	}
	public double getOriginalDouble(String p_name) {
	   Cell cell = testCell(p_name+"_original");
		if (cell != null)
	      return(cell.getDouble());
	   return(getDouble(p_name));
	}
	public Enumeration getCollectionKeys() {
	   if (collectionTable == null)
		   return(null);
	   return(collectionTable.keys());
	}
	public Enumeration getCollectionListKeys() {
	   if (collectionListTable == null)
		   return(null);
	   return(collectionListTable.keys());
	}
	public int getIntNoException(String p_name) {
	   Cell cell = getCellNoException(p_name);
		if (cell == null)
		   return(0);
	   return(cell.getInt());
	}
	public String getStringNoException(String p_name) {
	   Cell cell = getCellNoException(p_name);
		if (cell == null)
		   return(null);
	   return(cell.getString());
	}
	public void clearCellTrigger() throws CellException
	{
		checkReflected();
//		CoreLog.log("clearCellTrigger start " + this);
		if (cellTable != null) {
		   for (Enumeration e=cellTable.keys(); e.hasMoreElements(); ) {
				String s = (String) e.nextElement();
//				CoreLog.log("clearCell a " + s);
				Object o = cellTable.get(s);
				if(o != null && o instanceof Cell) {
					Cell c = (Cell) o;
					if(c.getFormula() != null) {
//						CoreLog.log("clearCellFormula a " + c.getFormula().toString());
						c.setFormula(null);
					}
				}
				if(o != null && o instanceof Cell[]) {
					Cell ca[] = (Cell[]) o;
					for(int i = 0;i<ca.length;i++) {
						Cell c = ca[i];
						if(c != null && c.getFormula() != null) {
//							CoreLog.log("clearCellFormula c " + c.getFormula().toString());
							c.setFormula(null);
						}
					}
				}
			}
		}
	   Hashtable ht = (Hashtable) reflectedFieldHashHash.get(getClass());
	   for (Enumeration e=ht.keys(); e.hasMoreElements(); ) {
			String s = (String) e.nextElement();
//			CoreLog.log("clearCell b " + s);
			Field fd = (Field) ht.get(s);
			if(fd != null) {
				try {
					Object o = (Object) fd.get(this);
					if(o != null && o instanceof Cell) {
						Cell c = (Cell) o;
						if(c.getFormula() != null) {
//							CoreLog.log("clearCellFormula b " + c.getFormula().toString());
							c.setFormula(null);
						}
					}
					if(o != null && o instanceof Cell[]) {
						Cell ca[] = (Cell[]) o;
						for(int i = 0;i<ca.length;i++) {
							Cell c = ca[i];
							if(c != null && c.getFormula() != null) {
//								CoreLog.log("clearCellFormula d " + c.getFormula().toString());
								c.setFormula(null);
							}
						}
					}
				} catch (Exception ex) {
					CoreLog.log(ex);
				}
			}
		}
//		CoreLog.log("clearCellTrigger end " + this);
	}
	public static void clearAllTrigger(CellCollection p_col) throws CellException
	{
			p_col.traverseCollection(false,new TraverseInterface() 
			{
				public boolean traverseOne(CellCollection p_col) throws CellException
				{
					p_col.clearCellTrigger();
					return(true);
				}
			}
			);
	}
	
	public void setParent(CellCollection p_parent) {
		parent = p_parent;
	}
	
	public Object[] getCollections()
	{
			if(collectionTable == null) return(null);
			return(collectionTable.values().toArray());
//			return((CellCollection[]) collectionTable.values().toArray());
	}
	/***
	 * obtain collectionTable values. 
	 * auto cast objects to target class
	 * @param p_class object class
	 * @return
	 */
	public <T> T[] getCollections(Class<T> p_class) {
		if(collectionTable == null) return(null);
		if (p_class == null) return null;
		
		//convert obj array to specific class array. this operation is a bit waste cpu but handy for dev.
		Object[] objs = collectionTable.values().toArray();
		if (objs == null || objs.length == 0) {
			return null;
		}
	    T[] outArr = (T[]) Array.newInstance(p_class,objs.length);   
	    System.arraycopy(objs, 0, outArr, 0, objs.length);
		return(outArr);
	}
	
	public double getCellDouble(String p_cell)
	{
		Cell cell = testCell(p_cell);
		if (cell == null) { return 0.0; }
		return(cell.getDouble());
	}
	public int getCellInt(String p_cell)
	{
		Cell cell = testCell(p_cell);
		if (cell == null) { return 0; }
		return(cell.getInt());
	}
	public String getCellString(String p_cell)
	{
		Cell cell = testCell(p_cell);
		if (cell == null) { return ""; }
		return(cell.getString());
	}
	public boolean getCellBoolean(String p_cell)
	{
		Cell cell = testCell(p_cell);
		if (cell == null) { return false; }
		return(cell.getBoolean());
	}
	public Date getCellDate(String p_cell)
	{
		Cell colCell = testCell(p_cell);
		if (colCell == null) {
			return null;
		}
		Date date = colCell.getDate();
		if (!DateUtil.isValid(date)) {
			return null;
		}
		return(date);
	}	
//	protected int argToInteger(Object arg) {
//		double darg = (Double) arg;
//		int iarg = (int) darg;
//		return(iarg);
//	}
//
//	protected String argToString(Object arg) {
//		return((String) arg);
//	}
	
	public static double round(double d,double r) {
		if(Double.isNaN(d)) return(d);
		d += r/2.0;
		d = Math.round(d * 10000000.0) / 10000000.0;
		d = Math.floor(d * (1.0d/ r) + 0.00000001 ) * r;
		d = Math.round(d * 10000000.0) / 10000000.0;
		return(d);
	}
	@Override
	public Object evalVariableRelative(String p_varname, int p_idx) throws Exception {
		throw new Exception("evalVariableRelative not supported");
	}
	
	public int getCompareMode() {
		return(compareMode);
	}
	
	static public String stringCombine(Object ... args) {
				String s = null;
				for(Object o : args) {
					String ss = o.toString();
					if(StringUtils.isEmpty(ss)) continue;
					if(s == null) s = ss; else s += " " + ss;
				}
				return(s == null ? "" : s);
	}
}
