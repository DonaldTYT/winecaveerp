package com.kikyosoft.cell;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.kikyosoft.utils.*;

import java.time.*;

import java.text.*;
public class Cell implements Serializable
{  
	static public boolean useCorrectDateCompare = false;
	transient private boolean inClearAllOverride = false;
	transient private boolean ignoreTimeZone = false;
//	static int cdCount;
//	static int cdCount;
	private static final AtomicInteger atomicCdCount = new AtomicInteger();

	private class CellDetail implements Serializable {
	   static final long serialVersionUID = 7779990684696159528L;
		private Hashtable actionList;
		private Hashtable<Integer,Cell> triggerList;
		private String hintContent = null;
		private CellValidation validation;
		private CellFormula formula;
		private Vector itemList=null;
//		private DecimalFormat format =null;
		private Format cellFormat =null;
		private Object hahaabc123Value;
//		private int hahaabc123setInt;
//		transient private CellValueMapper valueMapper;
		transient private Vector<CellValueMapper> valueMapperList;
		transient private boolean evaluatingChild = false;
		private Hashtable attributes = null;
		private CellValueAction modeChangeAction;
//		private java.util.Date hahaabc123setDate;
		private AbstractGetItemProperty gipi;
		private boolean isOverrided = false;
		private boolean isProtected = false;
		private CellPair pairObj;
//	   	private String cellLabel=null;
		transient private boolean ignoreFormula = false;
		transient private boolean ignoreEncode = false;
		transient private boolean isDirty = false;
		transient private boolean ignoreTimezone = false;

		/*
   	private void readObject(ObjectInputStream p_stream) throws
	                IOException, ClassNotFoundException, CellException{
	   	p_stream.defaultReadObject();
			evaluatingChild = false;
		}
		*/
		public CellDetail() {
	        atomicCdCount.incrementAndGet();
//			cdCount++;
		}
        protected void finalize() throws Throwable{
	         super.finalize();
	         atomicCdCount.decrementAndGet();
//           cdCount--;
	    }
	}
	static final long serialVersionUID = 3388772845523613363L;
	public  static final int VTYPE_INT			= 0;
	public  static final int VTYPE_STRING		= 1;
	public  static final int VTYPE_DOUBLE		= 2;
	public  static final int VTYPE_DATE 		= 3;
	public  static final int VTYPE_BOOLEAN		= 4;

	public  static final int VTYPE_DATETIME 		= 8;
	public  static final int VTYPE_IGNORE = 899;
	
	/* following types not actually used and supported, dont use it at this moment */
	public  static final int VTYPE_CHOICE		= 5;
	public  static final int VTYPE_SET			= 6;
	public  static final int VTYPE_BYTEARRAY  = 7;
	public  static final int VTYPE_OBJWRAPPER	= 9;
	public  static final int VTYPE_NULL = 999;
	
	static Map<String, Integer> cellTypeMap = new LinkedHashMap<String, Integer>(){{
		put("char",VTYPE_STRING);
		put("int",VTYPE_INT);
		put("boolean",VTYPE_BOOLEAN);
		put("double",VTYPE_DOUBLE);
		put("date",VTYPE_DATE);
		put("datetime",VTYPE_DATETIME);
	}};
	
	static public int getTypeByName(String p_type) {
		return(cellTypeMap.get(p_type));
	}
	/***
	 *  VMODE Remark:
	 *	VMODE_NORMAL VMODE_DISPONLY,VMODE_HIDDEN - sync/set not different
	 *  VMODE_PROTECTED,VMODE_OVERRIDED - set update original value, sync update overrided value. 
	 *  
	 *  - In general case, only use sync() instead of set().
	 *  - Special use case of set(),
	 *     e.g. Undo feature (Ctrl-D) when undo fire, the field will reverse to original value.
	 */
	public  static final int VMODE_NORMAL	= 1;  //normal field
	public  static final int VMODE_DISPONLY = 2;  //display only field (readonly)
	public  static final int VMODE_PROTECT  = 3;  //field allow to override
//	public  static final int VMODE_PROTECTED= 3;  //field allow to override
//	public  static final int VMODE_OVERRIDED= 4;  //field overrided (set original
	public  static final int VMODE_HIDDEN = 5;    //hidden field
	public  static final DecimalFormat defaultDoubleFormat = new DecimalFormat("###########0.00");

	private short jxValueMode = 0; 
	private short jxValueType = -1;
	private short flag = 0;
	private Object hahaabc123valObj;
	private CellDetail cd;
	private String cellLabel;
	/*
	private Object setObj;
	private int getIntVal();
	private double getDbVal();
	private boolean getBoolVal();
	private String getStrVal();
	private byte[] getBaVal();
	private java.util.Date getDateVal();
	*/

	/*
	*/


	/*
	private boolean evaluatingChild = false;
	private Hashtable actionList;
	private Hashtable triggerList;
	private String hintContent = null;
	private CellValidation validation;
	private CellFormula formula;
	private Vector itemList=null;
	private DecimalFormat format =null;
	private byte[] getBaVal();
	private java.util.Date getDateVal();
	*/

//	static int cellCount;
	private static final AtomicInteger atomicCellCount = new AtomicInteger();
	//transient private Parser parser;
	public Cell() {
		atomicCellCount.incrementAndGet();
		jxValueType = VTYPE_NULL;
		jxValueMode = VMODE_NORMAL;
//		cellCount++;
	}
    protected void finalize() throws Throwable{
        super.finalize();
//        cellCount--;
		atomicCellCount.decrementAndGet();
    }
	
	public Cell(Object o) {
		atomicCellCount.incrementAndGet();
		if(o instanceof String) {
			xJxValue((String) o ,VMODE_NORMAL);
		}
		if(o instanceof Boolean) {
			xJxValue((Boolean) o ,VMODE_NORMAL);
		}
		if(o instanceof Integer ) {
			xJxValue((Integer) o ,VMODE_NORMAL);
		}
		if(o instanceof Double ) {
			xJxValue((Double) o ,VMODE_NORMAL);
		}
		if(o instanceof Date ) {
			xJxValue((Date) o ,VMODE_NORMAL);
		}
		if(o instanceof AbstractSet) {
			xJxValue((AbstractSet) o ,VMODE_NORMAL);
		}
		if(o instanceof IgnoreValue) {
			jxValueType = VTYPE_IGNORE;
		}
		if(o instanceof CellPair ) {
			jxValueType = VTYPE_STRING;
			initcd();
			cd.pairObj = (CellPair) o;
			xJxValue(((CellPair) o).toString() ,VMODE_NORMAL);
		}
		if(o == null) {
			LogUtil.log("cell type null");
		} else if(jxValueType < 0) {
			LogUtil.log("cell type mismatch");
		}
//		cellCount++;
	}
	public Cell(String strval)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(strval,VMODE_NORMAL);
//		cellCount++;
	}
	public Cell(int intval)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(intval,VMODE_NORMAL);
//		cellCount++;
	}
	public Cell(double dbval)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(dbval,VMODE_NORMAL);
//		cellCount++;
	}
	public Cell(boolean boolval)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(boolval,VMODE_NORMAL);
//		cellCount++;
	}
	public Cell(byte[] baval)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(baval,VMODE_NORMAL);
//		cellCount++;
	}
	public Cell(java.util.Date dateval)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(dateval,VMODE_NORMAL);
//		cellCount++;
	}
	public Cell(java.util.Date dateval,int p_mode)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(dateval,p_mode);
//		cellCount++;
	}
	public Cell(CellObjWrapper p_val)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(p_val,VMODE_NORMAL);
//		cellCount++;
	}
	public void setItemList(Vector p_list)
	{
		setItemList(p_list,null);
	}
	public void setItemList(List p_list) {
		Vector v = new Vector();
		for(Object o : p_list) {
			v.add(o);
		}
		setItemList(v,null);
	}
	public void setItemList(Set p_set) {
		Vector v = new Vector();
		for(Object o : p_set) {
			v.add(o);
		}
		setItemList(v,null);
	}
	public void setItemList(Object p_list[])
	{
		Vector v = new Vector();
		for(int i=0;i<p_list.length;i++) {
			v.add(p_list[i]);
		}
		setItemList(v,null);
	}
	public void setItemPropertyInterface(AbstractGetItemProperty p_gipi)
	{
		setItemList(null,p_gipi);
	}
	
	
	// will remove and replayce by setItemList(Vector p_list,GetItemDetailInterface p_interface)
	void setItemList(Vector p_list,AbstractGetItemProperty p_interface)
	{
		initcd();
		cd.itemList = p_list;
		cd.gipi = p_interface;
		
		//ZkUtil.dumpData(p_interface);
		if(cd.valueMapperList != null) {
			for(int i=0;i<cd.valueMapperList.size();i++) {
				cd.valueMapperList.get(i).cellMap_listchange(this);
			}
		}
	}
	public Cell(String strval,int p_mode, Vector p_itemlist)
	{
		atomicCellCount.incrementAndGet();
		initcd();
		xJxValue(strval,p_mode);
		cd.itemList = p_itemlist;
//		cellCount++;
	}
	public Cell(String strval,int p_mode, Vector p_itemlist, CellValidation p_validation)
	{
		atomicCellCount.incrementAndGet();
		initcd();
		xJxValue(strval,p_mode);
		cd.itemList = p_itemlist;
		cd.validation = p_validation;
//		cellCount++;
	}
	public Cell(String strval,int p_mode)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(strval,p_mode);
//		cellCount++;
	}
	public Cell(int intval,int p_mode)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(intval,p_mode);
//		cellCount++;
	}
	public Cell(int intval,int p_mode,CellValidation p_validation)
	{
		atomicCellCount.incrementAndGet();
		initcd();
		xJxValue(intval,p_mode);
		cd.validation = p_validation;
//		cellCount++;
	}
	public Cell(double dbval,int p_mode)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(dbval,p_mode);
//		cellCount++;
	}
	public Cell(double dbval,int p_mode, CellValidation p_validation)
	{
		atomicCellCount.incrementAndGet();
		initcd();
		xJxValue(dbval,p_mode);
		cd.validation = p_validation;
//		cellCount++;
	}
	public CellFormula getFormula()
	{
		if(cd == null) return(null);
		return(cd.formula);
	}

	public void setFormula(CellFormula p_formula) throws CellException
	{
		setFormula(p_formula,true);
	}
	public void setFormula(CellFormula p_formula,boolean p_eval) throws CellException
	{
		initcd();
		if(cd.formula != null) cd.formula.unsetTrigger(this);
		cd.formula = p_formula;
		if(p_formula != null) {
			cd.formula.setTrigger(this);
			if(p_eval) eval();
		}
	}
	public Cell(boolean boolval,int p_mode)
	{
		atomicCellCount.incrementAndGet();
		xJxValue(boolval,p_mode);
//		cellCount++;
	}
	/* private use */
	private void xJxValue(String strval,int p_mode)
	{
		jxValueType = VTYPE_STRING;
		jxValueMode = (short) p_mode;
		setStrVal(strval);
	}
	private void xJxValue(int intval,int p_mode)
	{
		jxValueType = VTYPE_INT;
		jxValueMode = (short) p_mode;
		setIntVal(intval);
	}
	private void xJxValue(double dbval,int p_mode)
	{
		jxValueType = VTYPE_DOUBLE;
		jxValueMode = (short) p_mode;
		setDbVal(dbval);
	}
	private void xJxValue(boolean boolval,int p_mode)
	{
		jxValueType = VTYPE_BOOLEAN;
		jxValueMode = (short) p_mode;
		setBoolVal(boolval);
	}
	private void xJxValue(byte[] baval,int p_mode)
	{
		jxValueType = VTYPE_BYTEARRAY;
		jxValueMode = (short) p_mode;
		setBaVal(baval);
	}
	private void xJxValue(java.util.Date dateval,int p_mode)
	{
		jxValueType = VTYPE_DATE;
		jxValueMode = (short) p_mode;
		setDateVal(dateval);
	}
	private void xJxValue(AbstractSet p_obj,int p_mode)
	{
		jxValueType = VTYPE_SET;
		jxValueMode = (short) p_mode;
		setSetVal(p_obj);
	}
	private void xJxValue(CellObjWrapper p_obj,int p_mode)
	{
		jxValueType = VTYPE_OBJWRAPPER;
		jxValueMode = (short) p_mode;
		setObjWrapperVal(p_obj);
	}
	public Vector getItemList()
	{
		if(cd == null) return(null);
		return(cd.itemList);
	}
	public AbstractGetItemProperty getItemPropertyInterface()
	{
		if(cd == null) return(null);
		return(cd.gipi);
	}
	
	//* add p_mapper to map list
	public void mapAdd(CellValueMapper p_mapper) /* throws CellException */
	{
		initcd();
		if(p_mapper != null) {
			if(cd.valueMapperList == null) cd.valueMapperList = new Vector <CellValueMapper>();
			if(cd.valueMapperList.indexOf(p_mapper) < 0) {
				cd.valueMapperList.add(p_mapper);
			}
			p_mapper.cellMap_bind(this);
		}
	}
	public void mapDelete(CellValueMapper p_mapper) /* throws CellException */
	{
		if(cd == null) return;
		if(p_mapper != null) {
			if(cd.valueMapperList == null) return;
			int idx = cd.valueMapperList.indexOf(p_mapper);
			if(idx >= 0) {
				cd.valueMapperList.remove(idx);
			}
		}
	}
	//* remote all previouse mapper and map cell to p_mapper 
	public void map(CellValueMapper p_mapper) /* throws CellException */
	{
		if(cd == null) {
			if(p_mapper == null) return;
			initcd();
		}
		if(cd.valueMapperList != null) {
			for(int i=0;i<cd.valueMapperList.size();i++) {
				cd.valueMapperList.get(i).cellMap_bind(null);
			}
			cd.valueMapperList.clear();
		}
		if(p_mapper != null) {
			if(cd.valueMapperList == null) cd.valueMapperList = new Vector <CellValueMapper>();

//			p_mapper.cellMap_bind(this);
//			cd.valueMapperList.add(p_mapper);

//			change the sequence that the valueMapperList is set properly before cellMap_bind is called, to make GIPI_CELL_MAPPED trigger workable
//          changed on 2018-07-05, don't whether there will have any issue after changed
			
			cd.valueMapperList.add(p_mapper);
			p_mapper.cellMap_bind(this);
		}
	}
	public boolean eval() throws CellException
	{
		switch(jxValueType) {
		case VTYPE_INT:
		case VTYPE_DOUBLE :
		case VTYPE_STRING :
		case VTYPE_DATE   :
		case VTYPE_BOOLEAN:
		case VTYPE_DATETIME:
					if(cd.formula != null) {
						Object o = cd.formula.eval();
						if(o instanceof CellPair) {
							cd.pairObj = (CellPair) o;
							o = cd.pairObj.toString();
						}
						if(o != null) {
							/*
							&& o instanceof Double) setDb = o.doubleValue();
							*/
							if(cd.ignoreFormula) {
								if( o instanceof IgnoreValue) {
									if(cd.isOverrided) {
										cd.isOverrided = false;
										notifyModeChange();
									}
//									if(jxValueMode == VMODE_OVERRIDED) setMode(VMODE_PROTECTED);
									return(false);
								} else {
									setIgnoreFormula(false);
								}
							} else {
								if( o instanceof IgnoreValue) {
									setIgnoreFormula(true);
									if(cd.isOverrided) {
										cd.isOverrided = false;
										notifyModeChange();
									}
//									if(jxValueMode == VMODE_OVERRIDED) setMode(VMODE_PROTECTED);
									return(false);
								} 
							}
//							switch(jxValueType) {
//							case VTYPE_INT:
//							case VTYPE_DOUBLE :
//								if(o instanceof String && o.equals("Ignored")) {
//									setIgnoreFormula(true);
//									return;
//								} else {
//									setIgnoreFormula(false);
//								}
//							}
							Object oo = hahaabc123valObj;
							try {
								set(o);
								oo = null;
							} catch (CellException ex) {
								set(oo);
								throw ex;
							}

//							if(jxValueMode != VMODE_OVERRIDED) {
							if(!cd.isOverrided) {
								switch(jxValueType) {
								case VTYPE_INT:
								if((Integer) cd.hahaabc123Value != getIntVal()) {
									setIntVal((Integer) cd.hahaabc123Value);
									if(cd.valueMapperList != null) {
										for(int i=0;i<cd.valueMapperList.size();i++) {			
											cd.valueMapperList.get(i).cellMap_valchange(this);
										}
									}
									checkActionAndTrigger();
								}
								break;
								case VTYPE_DOUBLE :
								if(((Double) cd.hahaabc123Value) != getDbVal()) {
									setDbVal((Double) cd.hahaabc123Value);
									if(cd.valueMapperList != null) {
										for(int i=0;i<cd.valueMapperList.size();i++) {			
											cd.valueMapperList.get(i).cellMap_valchange(this);
										}
									}
									checkActionAndTrigger();
								}
								break;
								}
							
							}
						}
					}
					break;
		}
		return(true);
	}
	public void clearEvaluatingChild(){ //use it carefully, it may generate trigger recursively!!
		LogUtil.log("clearEvaulatingChildFlag called");
		if (cd != null)
			cd.evaluatingChild = false;
	}
	private void checkActionAndTrigger() throws CellException
	{
			if(cd == null) return;
			if(cd.actionList != null) {
				CellValueAction action;
				Enumeration e = cd.actionList.elements();
				while(e.hasMoreElements()) {
					/*
					if(cd.evaluatingChild) 
						throw new CellException("Recursive Evaluation Inhibited")+getIntVal() + getDbVal() + getBoolVal() + getStrVal());
					*/
					if(cd.evaluatingChild) {
						CellException ex = new CellException("Recursive Evaluation Inhibited",CellException.CELLEXCEPTION_RECURSIVE);
						
//						LogUtil.log(ex);
						LogUtil.log("Recursive Evaluation Inhibited CellLabel: " + getCellLabel());
						throw ex;
					}
					cd.evaluatingChild = true;
					//LogUtil.log("Evaluating columnCell 1: " + getCellLabel());
					action = (CellValueAction) e.nextElement();
					try {
//						LogUtil.log("Before cellAction_onchange " + action);
						action.cellAction_onchange(this);
//						LogUtil.log("After cellAction_onchange " + action);
					} catch (CellException ex) {
						cd.evaluatingChild = false;
						throw ex;
						/*
						LogUtil.log(ex);
						throw new CellException(ex.toString());
						*/
					}
					cd.evaluatingChild = false;
				}
			}
			if(cd.triggerList != null) {
				Cell c;
				Enumeration e = cd.triggerList.elements();
				while(e.hasMoreElements()) {
					/*
					if(cd.evaluatingChild) throw new CellException("Recursive Evaluation Inhibited"+
										getIntVal() + getDbVal() + getBoolVal() + getStrVal());
					*/
					if(cd.evaluatingChild) {
						CellException ex = new CellException("Recursive Evaluation Inhibited");
						LogUtil.log(ex);
						throw ex;
					}
					//LogUtil.log("Evaluating columnCell 2 : " + getCellLabel());
					c = (Cell) e.nextElement();
					if(c == this) {
						LogUtil.log("Warning !!! ignore trigger to parent cell itself");
						continue;
					}
					cd.evaluatingChild = true;
					try {
//						LogUtil.log("Before eval " + getString());
						c.eval();
//						LogUtil.log("After eval " + getString());
					} catch (CellException ex) {
						cd.evaluatingChild = false;
						throw ex;
						/*
						LogUtil.log(ex);
						throw new CellException(ex.toString());
						*/
					}
					cd.evaluatingChild = false;
				}
			}
	}
	/***
	 * VMODE_NORMAL - set/sync update original value
	 * VMODE_PROTECTED,VMODE_OVERRIDED - set update original value, sync update overrided value. 
	 * 
	 * @param o - input data
	 * @throws CellException
	 */
	public void set(Object o) throws CellException
	{
		if(o instanceof Double) {
			set(((Double) o).doubleValue());
		} else if( o instanceof String ) {
			set((String) o);
		} else if( o instanceof Integer ) {
			set(((Integer) o).intValue());
		}  else if( o instanceof java.util.Date) {
			set((java.util.Date) o);
		}  else if( o instanceof Boolean) {
			set(((Boolean) o).booleanValue());
		}  else if( o instanceof Cell ) {
			set(((Cell ) o).getObject());
		}
	}
	public void set(String strval) throws CellException
	{
		switch(jxValueType) {
		case VTYPE_BOOLEAN:
			if(strval.equals("Y")) set(true); 
			else if(strval.equals("是")) set(true); 
			else set(false);
			break;
		case VTYPE_DATE:
			Date d = DateUtil.dateTimeStrToDate(strval, false) ;
			set(d);
			break;
		case VTYPE_INT:
			try {
				set(Integer.parseInt(strval));
			} catch (Exception ex) {
				if(!StringUtils.isBlank(strval)) {
					LogUtil.log1("Set Non-Integer [%s] to Cell.Integer is not allowed, replaced to 0", strval);
				}
//				set(Double.NaN);
				set(0);
			}
			break;
		case VTYPE_DOUBLE:
			try {
				set(Double.parseDouble(strval));
			} catch (Exception ex) {
				//LogUtil.log("Set Non-Number to Cell.Double is not allowed, replaced to 0");
				if (!StringUtils.equalsAny(strval, "-", "")) {   //too much error msg for vincero
					LogUtil.log1("Set Non-Number %s to Cell.Double is not allowed, replaced to 0", strval);
				}
//				set(Double.NaN);
				set(0.0);
			}
			break;
		case VTYPE_STRING:
			if(cd == null) {setStrVal(strval);return;} else {
				if(!StringUtils.equals((String) cd.hahaabc123Value, strval)) {
					cellUpdated();
				}
				cd.hahaabc123Value = strval;
			}
			if(!cd.isOverrided) {
				if((strval == null && getStrVal() != null) 
			  	  || !(getIgnoreEncode() ? StringUtil.equalIgnoreEncode(strval, getStrVal()) : strval.equals(getStrVal()))) {
					setStrVal(strval);
					if(cd.valueMapperList != null) {
						for(int i=0;i<cd.valueMapperList.size();i++) {	
							cd.valueMapperList.get(i).cellMap_valchange(this);
						}
					}
					checkActionAndTrigger();
				} 
			}
			break;
		case VTYPE_DATETIME: {
				//d = DateUtil.getTime(strval);
				//set(d);
				set(DateUtil.zeroDate);
			}
			break;
		default:
			LogUtil.log(new Exception("set String to unsupported Cell type ignored type = " + jxValueType));
			return;
		}

	}
	
//	public void set(String strval) throws CellException
//	{
//		if(jxValueType == VTYPE_BOOLEAN) {
//			if(strval.equals("Y")) set(true); else set(false);
//			return;
//		}
//		if(jxValueType == VTYPE_DATE) {
//			Date d = DateUtil.dateTimeStrToDate(strval, false) ;
//			set(d);
//			return;
//		}
//		if(jxValueType != VTYPE_STRING) return;
//		if(cd == null) {setStrVal(strval);return;} else {
//			if(!StringUtils.equals(cd.hahaabc123setStr, strval)) {
//				cellUpdated();
//			}
//			cd.hahaabc123setStr = strval;
//		}
////		if(jxValueMode != VMODE_OVERRIDED) {
//		if(!cd.isOverrided) {
//			if((strval == null && getStrVal() != null) 
//		  	  || !(getIgnoreEncode() ? StringUtil.equalIgnoreEncode(strval, getStrVal()) : strval.equals(getStrVal()))) {
//				setStrVal(strval);
//				if(cd.valueMapperList != null) {
//					for(int i=0;i<cd.valueMapperList.size();i++) {	
//						cd.valueMapperList.get(i).cellMap_valchange(this);
//					}
//				}
//				checkActionAndTrigger();
//			} 
//		}
//	}	
	
	
	public void setSilent(String strval) {
		try {
			set(strval);
		}
		catch(Exception ex) {
			LogUtil.log1("error:" + ex.getMessage());
		}
	}
	public void setSilent(Object obj) {
		try {
			set(obj);
		}
		catch(Exception ex) {
			LogUtil.log1("error:" + ex.getMessage());
		}
	}

	
	public void updateReverse(Object p_orgValue){
		try{
			if (p_orgValue == null) {
				//LogUtil.log("ignore null");
				return;
			}
//			if(jxValueMode == VMODE_OVERRIDED) {
			if(cd.isOverrided) {
				if (p_orgValue instanceof Boolean){
					sync(((Boolean)p_orgValue).booleanValue());
				}
				else if (p_orgValue instanceof Integer){
					sync(((Integer)p_orgValue).intValue());
				}
				else if (p_orgValue instanceof Double){
					sync(((Double)p_orgValue).doubleValue());
				}
				else if (p_orgValue instanceof String){
					sync(((String)p_orgValue));
				}
				else if (p_orgValue instanceof Date){
					sync(((Date)p_orgValue));
				}
				else{
					LogUtil.log("format not supported: " + p_orgValue);
				}
			}
			else{
				if (p_orgValue instanceof Boolean){
					set(((Boolean)p_orgValue).booleanValue());
				}
				else if (p_orgValue instanceof Integer){
					set(((Integer)p_orgValue).intValue());
				}
				else if (p_orgValue instanceof Double){
					set(((Double)p_orgValue).doubleValue());
				}
				else if (p_orgValue instanceof String){
					set(((String)p_orgValue));
				}
				else if (p_orgValue instanceof Date){
					set(((Date)p_orgValue));
				}
				else{
					LogUtil.log("format not supported: " + p_orgValue);
				}
			}
		}
		catch(Exception ex){
			LogUtil.log("sth wrong during reverse");
			ex.printStackTrace();
		}
	}
	/***
	 * @param p_value - data value
	 * @return update status
	 * @throws CellException - should never throw Exception, it's only for backward compatible
	 * @throws NumberFormatException - should never throw Exception, it's only for backward compatible
	 */
	public ReturnMsg update(Object p_value) throws CellException, NumberFormatException
	{
		return(update(p_value,null));
	}
	public ReturnMsg update(Object p_value,Object p_restoreValue) throws CellException, NumberFormatException
	{
		initcd();
		if(cd.validation != null && !cd.validation.validate(this,p_value)) {
			throw new NumberFormatException(cd.validation.getErrMsg());
		}
//		temporary move the validation to cellvalmap.validateChange, only able to handle combox correctly 
//		if(jxValueType == VTYPE_STRING) {
//			if(cd.gipi != null) {
//				
//			} else if(cd.itemList != null) {
//				if(p_value != null && ! cd.itemList.contains(p_value))
//					throw new NumberFormatException("Invalid Choice ["+p_value.toString()+"]");
//			}
//		}
		Object oldValue = p_restoreValue;
		switch(jxValueType) {
		case VTYPE_BOOLEAN :
			oldValue = getBoolVal();
			try{
				if(p_value instanceof Boolean) {
					if(oldValue == null) oldValue = (Boolean)p_value;
					sync(((Boolean) p_value).booleanValue());
				}
				else{
					return(new ReturnMsg(false,"Invalid format"));
				}
			}
			catch(CellException ex){
				updateReverse(oldValue);
				return(new ReturnMsg(false,ex));
			}
			break;
		case VTYPE_INT :		
			if(oldValue == null) oldValue = getIntVal();
			try {
				if (p_value instanceof Integer){
					sync(((Integer) p_value).intValue());
				}
				else{
					return(new ReturnMsg(false,"Invalid format"));
				}
			} 
			catch (CellException ex) {
				LogUtil.log("CellException Catched restore 1 "+oldValue);
				updateReverse(oldValue);
				return(new ReturnMsg(false,ex));
			}
			break;
		case VTYPE_DOUBLE:
			if(oldValue == null) oldValue = getDbVal();
			try {
				if (p_value instanceof Double){
					sync(((Double) p_value).doubleValue());
				}
				else{
					return(new ReturnMsg(false,"Invalid format"));
				}
			}
			catch (CellException ex) {
				LogUtil.log("CellException Catched restore 2 " + oldValue);
				updateReverse(oldValue);
				return(new ReturnMsg(false,ex));
			}
			break;
		case VTYPE_STRING :
			if(oldValue == null) oldValue = getStrVal();
			try {
				if (p_value == null || p_value instanceof String){
					sync(
							StringUtils.stripEnd((String) p_value," ")
							);
				}
				else{
					return(new ReturnMsg(false,"Invalid format"));
				}
			} 
			catch (CellException ex) {
				LogUtil.log("CellException Catched restore 3 " + oldValue);
				updateReverse(oldValue);
				return(new ReturnMsg(false,ex));
			}
			break;
		case VTYPE_DATE   :
		case VTYPE_DATETIME   :
			if(oldValue == null) oldValue = getDateVal();
			try{
				if(p_value instanceof java.util.Date) {
					sync((java.util.Date)p_value);
				} 
				else if(p_value instanceof String){
					java.util.Date jd;
					if(p_value == null || ((String) p_value).trim() == "")  {
						jd = null; 
					} else {
						if(jxValueType == VTYPE_DATE) {
						jd = DateUtil.getDate((String) p_value);
						} else {
						jd = DateUtil.dateTimeStrToDate((String) p_value);
						}
						if (jd == null){
							return(new ReturnMsg(false,"Invalid format"));
						}
					}
					sync(jd);
				}
				else{
					return(new ReturnMsg(false,"Invalid format"));
				}
			}
			catch(Exception ex){
				LogUtil.log("CellException Catched restore 4 " + oldValue);
				updateReverse(oldValue);
				return(new ReturnMsg(false,ex));
			}
			break;
		default : 
			return(new ReturnMsg(false, "Unsupported Data Format"));
		}
		return(new ReturnMsg(true));
	}
	public void sync(String strval) throws CellException
	
	{
		switch(jxValueType) {
		case VTYPE_BOOLEAN:
			{
				if("Y".equals(strval)) sync(true);
				else if("是".equals(strval)) sync(true);
				else sync(false);
			}
			break;
		default:
		if((strval == null && getStrVal() != null) 
		    || (strval != null && !(getIgnoreEncode() ? StringUtil.equalIgnoreEncode(strval, getStrVal()) : strval.equals(getStrVal())))) {
//			LogUtil.log("syncMode str override " + getStrVal());
			String orgs = getStrVal();
			setStrVal(strval);
			if(cd.valueMapperList != null) {
				for(int i=0;i<cd.valueMapperList.size();i++) {
					cd.valueMapperList.get(i).cellMap_valchange(this);
				}
			}
			checkActionAndTrigger();
			if(/* jxValueMode == VMODE_PROTECTED */ cd.isProtected) {
				if(!cd.ignoreFormula) {
					cd.isOverrided = true;
					notifyModeChange();
				}
			}
			/*
			if(jxValueMode == VMODE_PROTECTED) {
				if(!cd.ignoreFormula) setMode(VMODE_OVERRIDED);
				try {
					checkActionAndTrigger();
				} catch (CellException ex) {
					LogUtil.log("cell sync string newoverride exception captured");
					setMode(VMODE_PROTECTED);
					throw(ex);
				}
			} else {
				checkActionAndTrigger();
			}
			if(jxValueMode == VMODE_PROTECTED) setMode(VMODE_OVERRIDED);
			*/
		} 
		break;
		}
	}
	public void set(int intval) throws CellException
	{
		// why trigger is only activated for jxValueType == VTYPE_INT ? , may change in future
		// 2018-09-28
		if(jxValueType == VTYPE_INT) {
			if(cd == null) {setIntVal(intval);return;} else {
				if(((Integer) cd.hahaabc123Value) != intval) {
					cellUpdated();
				}
				cd.hahaabc123Value = intval;
			}
//			if(jxValueMode != VMODE_OVERRIDED) {
			if(!cd.isOverrided) {
				if(intval != getIntVal()) {
					setIntVal(intval);
					if(cd.valueMapperList != null) {
						for(int i=0;i<cd.valueMapperList.size();i++) {
							cd.valueMapperList.get(i).cellMap_valchange(this);
						}
					}
					checkActionAndTrigger();
				}
			}
		}  else if(jxValueType == VTYPE_DOUBLE) {
			set ( (double) intval);
		}  else if(jxValueType == VTYPE_DATETIME) {
			if(!ignoreTimeZone) {
				set ( DateUtil.unixtimeToDate(intval));
			} else {
//				set ( DateUtil.unixtimeToDate(intval));
				int cc = intval + 28800;
				int s = cc % 60;
				int m = (cc / 60) % 60;
				int h = (cc / 3600);
				set ( DateUtil.getTime(1970, 1, 1, h, m, s));
			}
		}  else if(jxValueType == VTYPE_DATE) {
			set ( DateUtil.informixToDate(intval));
		}  else if(jxValueType == VTYPE_BOOLEAN) {
			if(intval == 0) set(false); else set(true);
		}  else if(jxValueType == VTYPE_STRING) {
//			if(cd == null) {setStrVal(""+intval);return;} else cd.setStr = ""+intval;
			set(""+intval);
		}
	}
	public void sync(int intval) throws NumberFormatException ,CellException
	{
		if(intval != getIntVal()) {
//			LogUtil.log("syncMode int override " + getIntVal());
			setIntVal(intval);
			if(cd != null && cd.valueMapperList != null) {
				for(int i=0;i<cd.valueMapperList.size();i++) {
					cd.valueMapperList.get(i).cellMap_valchange(this);
				}
			}
			checkActionAndTrigger();
			if(/* jxValueMode == VMODE_PROTECTED */ cd.isProtected) {
				if(!cd.ignoreFormula) {
					cd.isOverrided = true;
					notifyModeChange();
				}
			}
			/*
			if(jxValueMode == VMODE_PROTECTED) {
				if(!cd.ignoreFormula) setMode(VMODE_OVERRIDED);
				try {
					checkActionAndTrigger();
				} catch (CellException ex) {
					LogUtil.log("cell sync int newoverride exception captured");
					setMode(VMODE_PROTECTED);
					throw(ex);
				}
			} else {
				checkActionAndTrigger();
			}
			if(jxValueMode == VMODE_PROTECTED) setMode(VMODE_OVERRIDED);
			*/
		}
	}
	public void set(java.util.Date d) throws CellException
	{
		if(jxValueType == VTYPE_DATE || jxValueType == VTYPE_DATETIME) {
			if(cd == null) {setDateVal(d);return;} else {
				if(!DateUtil.equals(d,(Date) cd.hahaabc123Value)) {
					cellUpdated();
				}
				cd.hahaabc123Value = d;
			}
//			if(jxValueMode != VMODE_OVERRIDED) {
			if(!cd.isOverrided) {
				if(useCorrectDateCompare) {
				if(getDateVal() == null || !getDateVal().equals(d) ) {
					setDateVal(d);
					if(cd.valueMapperList != null) {
						for(int i=0;i<cd.valueMapperList.size();i++) {
							cd.valueMapperList.get(i).cellMap_valchange(this);
						}
					}
					checkActionAndTrigger();
				}
				} else {
				if(d != getDateVal()) {
					setDateVal(d);
					if(cd.valueMapperList != null) {
						for(int i=0;i<cd.valueMapperList.size();i++) {
							cd.valueMapperList.get(i).cellMap_valchange(this);
						}
					}
					checkActionAndTrigger();
				}
				}
			}
		}
		if(jxValueType == VTYPE_INT) {
			int nDate = 0;
			if(d != null && d.after(DateUtil.minDate)) {
				nDate = DateUtil.dateToSqlDateInt(d);
			}
			set(nDate);
		}
	}
	public void sync(java.util.Date d) throws CellException
	{
		if((d != null && d != getDateVal()) || (d == null && getDateVal() != null  && getDateVal().after(com.kikyosoft.utils.DateUtil.minDate))) {
//			LogUtil.log("syncMode date override " + getDateVal() + " <-> " + d);
			setDateVal(d);
			if(cd != null && cd.valueMapperList != null) {
				for(int i=0;i<cd.valueMapperList.size();i++) {
					cd.valueMapperList.get(i).cellMap_valchange(this);
				}
			}
			checkActionAndTrigger();
			if(/* jxValueMode == VMODE_PROTECTED */ cd.isProtected) {
				if(!cd.ignoreFormula) {
					cd.isOverrided = true;
					notifyModeChange();
				}
			}
			/*
			if(jxValueMode == VMODE_PROTECTED) {
				if(!cd.ignoreFormula) setMode(VMODE_OVERRIDED);
				try {
					checkActionAndTrigger();
				} catch (CellException ex) {
					LogUtil.log("cell sync date newoverride exception captured");
					setMode(VMODE_PROTECTED);
					throw(ex);
				}
			} else {
				checkActionAndTrigger();
			}
			if(jxValueMode == VMODE_PROTECTED) setMode(VMODE_OVERRIDED);
			*/
		}
	}
	/***
	 * VMODE_NORMAL - set/sync update original value
	 * VMODE_PROTECTED,VMODE_OVERRIDED - set update original value, sync update overrided value. 
	 * @param o - input data
	 * @throws CellException
	 */
	public void sync(Object o) throws CellException
	{
		boolean forceOverride = false;
		if(cd == null) {
			if(jxValueMode != 0) {
				initcd();
				LogUtil.log("sync object called to cell with no celldetail call initcd");
				forceOverride = true;
			} else {
				LogUtil.log("sync object called to cell with no celldetail, use setvalue");
				set(o);
				return;
			}
		}
		if(cd.validation != null && !cd.validation.validate(this,o)) {
			throw new CellException(cd.validation.getErrMsg());
		}
		switch(jxValueType) {
		case VTYPE_BOOLEAN: {
					boolean b = false;
					if(o instanceof Double) {
						int i = (int) (((Double) o).doubleValue());
						if(i != 0) b = true; else b = false;
					} else {
						if( o instanceof String ) {
							String s = (String) o;
							if(o.equals("Y")) b = true; 
							else if(o.equals("是")) b = true; 
							else b = false;
//							sync((String) o);
							sync(b);
						} else {
							if( o instanceof Integer ) {
								int i = ((Integer) o).intValue();
								if(i != 0) b = true; else b = false;
							} else {
								if( o instanceof Boolean ) {
									b = ((Boolean) o).booleanValue();
								}
							}
						}
					}
					sync(b);
				}
				break;
		case VTYPE_INT: {
					int n = 0;
					if(o instanceof Double) {
						n = (int) (((Double) o).doubleValue());
					} else {
						if( o instanceof String ) {
							n = Integer.parseInt((String) o);
						} else {
							if( o instanceof Integer ) {
								n = ((Integer) o).intValue();
							} else {
								if( o instanceof Boolean ) {
									boolean b;
									b = ((Boolean) o).booleanValue();
									if(b) n = 1; else n = 0;
								}
							}
						}
					}
					sync(n);
				}
				break;
		case VTYPE_STRING: {
					sync(o.toString());
				}
				break;
		case VTYPE_DOUBLE: {
					double d = 0.0;
					if(o instanceof Double) {
						d = ((Double) o).doubleValue();
					} else {
						if( o instanceof String ) {
							d = Double.parseDouble((String) o);
						} else {
							if( o instanceof Integer ) {
								d = (double) (((Integer) o).intValue());
							} else {
								if( o instanceof Boolean ) {
									boolean b;
									b = ((Boolean) o).booleanValue();
									if(b) d = 1.0; else d = 0.0;
								}
							}
						}
					}
					sync(d);
				}
				break;
		case VTYPE_DATETIME:
				if(o instanceof String) {
					java.util.Date d = DateUtil.dateTimeStrToDate((String) o);
					sync(d);
				} else if(o instanceof java.util.Date) sync((java.util.Date) o);
				break;
		case VTYPE_DATE :
				if(o instanceof String) {
//					java.util.Date d = DateUtil.getDateDMY4((String) o);
					java.util.Date d = DateUtil.getDate((String) o);
					sync(d);
				} else if(o instanceof java.util.Date) sync((java.util.Date) o);
				break;
		default:
			LogUtil.log("Unsupported Sync Object Ignored " + o);
		}
		if(forceOverride) {
			if(/* jxValueMode == VMODE_PROTECTED*/ cd.isProtected) {
//				jxValueMode = VMODE_OVERRIDED;
				cd.isOverrided = true;
			}
		}
	}
	public void set(double dbval) throws CellException
	{
		if(jxValueType == VTYPE_DOUBLE) {
			if(cd == null) {
				setDbVal(dbval);
				return;
			} 
			else {
				if(((Double) cd.hahaabc123Value) != dbval) {
					cellUpdated();
				}
				cd.hahaabc123Value = dbval;
			}
//			if(jxValueMode != VMODE_OVERRIDED) {
			if(!cd.isOverrided) {
				if(dbval != getDbVal()) {
					setDbVal(dbval);
					if(cd.valueMapperList != null) {
						for(int i=0;i<cd.valueMapperList.size();i++) {
							cd.valueMapperList.get(i).cellMap_valchange(this);
						}
					}
					checkActionAndTrigger();
				}
			}
		}
		if(jxValueType == VTYPE_INT) {
			if(cd == null) {
				setIntVal((int) dbval);
				return;
			} 
			else {
				if(((Integer) cd.hahaabc123Value) != ((int) dbval)) cellUpdated();
				cd.hahaabc123Value = (int) dbval;
			}
//			if(jxValueMode != VMODE_OVERRIDED) {
			if(!cd.isOverrided) {
				if(((Integer) cd.hahaabc123Value) != getIntVal()) {
					setIntVal((Integer) cd.hahaabc123Value);
					if(cd.valueMapperList != null) {
						for(int i=0;i<cd.valueMapperList.size();i++) {
							cd.valueMapperList.get(i).cellMap_valchange(this);
						}
					}
					checkActionAndTrigger();
				}
			}
		}
	}
	public void sync(double dbval) throws CellException
	{
		if(dbval != getDbVal()) {
//			LogUtil.log("syncMode double override " + getDbVal());
			setDbVal(dbval);
			if(cd.valueMapperList != null) {
				for(int i=0;i<cd.valueMapperList.size();i++) {
					cd.valueMapperList.get(i).cellMap_valchange(this);
				}
			}
			checkActionAndTrigger();
			if(/* jxValueMode == VMODE_PROTECTED */ cd.isProtected) {
				if(!cd.ignoreFormula) {
					cd.isOverrided = true;
					notifyModeChange();
				}
			}
			/*
			if(jxValueMode == VMODE_PROTECTED) {
				if(!cd.ignoreFormula) setMode(VMODE_OVERRIDED);
				try {
					checkActionAndTrigger();
				} catch (CellException ex) {
					LogUtil.log("cell sync double newoverride exception captured");
					setMode(VMODE_PROTECTED);
					throw(ex);
				}
			} else {
				checkActionAndTrigger();
			}
			*/
		}
	}
	public void set(boolean boolval) throws CellException
	{
		if(jxValueType != VTYPE_BOOLEAN) return;
		if(cd == null) {setBoolVal(boolval);return;} else {
			if(((Boolean) cd.hahaabc123Value) != boolval) {
				cellUpdated();
			}
			cd.hahaabc123Value = boolval;
		}
//		if(jxValueMode != VMODE_OVERRIDED) {
		if(!cd.isOverrided) {
			if(boolval != getBoolVal()) {
				setBoolVal(boolval);
				if(cd.valueMapperList != null) {
					for(int i=0;i<cd.valueMapperList.size();i++) {
						cd.valueMapperList.get(i).cellMap_valchange(this);
					}
				}
				checkActionAndTrigger();
			}
		}
	}
	public void sync(boolean boolval) throws CellException
	{
		if(boolval != getBoolVal()) {
//			LogUtil.log("syncMode boolean override " + getBoolVal());
			setBoolVal(boolval);
			if(cd.valueMapperList != null) {
				for(int i=0;i<cd.valueMapperList.size();i++) {
					cd.valueMapperList.get(i).cellMap_valchange(this);
				}
			}
			checkActionAndTrigger();
			if(/* jxValueMode == VMODE_PROTECTED */ cd.isProtected) {
				if(!cd.ignoreFormula) {
					cd.isOverrided = true;
					notifyModeChange();
				}
			}
			/*
			if(jxValueMode == VMODE_PROTECTED) {
				if(!cd.ignoreFormula) setMode(VMODE_OVERRIDED);
				try {
					checkActionAndTrigger();
				} catch (CellException ex) {
					LogUtil.log("cell sync boolean newoverride exception captured");
					setMode(VMODE_PROTECTED);
					throw(ex);
				}
			} else {
				checkActionAndTrigger();
			}
			if(jxValueMode == VMODE_PROTECTED) setMode(VMODE_OVERRIDED);
			*/
		}
	}

	public int getType()
	{
		return(jxValueType);
	}
	public Object getObject() {
		switch(jxValueType) {
		case VTYPE_STRING : 
				if(cd != null && cd.pairObj != null) return(cd.pairObj); else return(getString());
		case VTYPE_DOUBLE : return(new Double(getDouble()));
		case VTYPE_INT		: return(new Integer(getInt()));
		case VTYPE_BOOLEAN: return(new Boolean(getBoolean()));
		case VTYPE_BYTEARRAY: return(getByteArray().clone());
		case VTYPE_DATETIME   : 
		case VTYPE_DATE   : if(getDate() != null) return(getDate().clone()); else return(null);
		case VTYPE_OBJWRAPPER  : return(getObjWrapper());
		case VTYPE_SET : return((AbstractSet) hahaabc123valObj);
		case VTYPE_IGNORE : return(new IgnoreValue());
		default 			   : return(null);
		}
	}
	public String getString()
	{
		switch(jxValueType) {
		case VTYPE_STRING : return(getStrVal());
		/*
		case VTYPE_DOUBLE : return(Double.toString(getDbVal()));
		*/
		case VTYPE_DOUBLE : 
							if(Double.isNaN(getDbVal())) return("NaN");
			                if(cd != null && cd.cellFormat != null && (cd.cellFormat instanceof DecimalFormat)) {
								return(cd.cellFormat.format(getDbVal()));
							} else {
								return(defaultDoubleFormat.format(getDbVal()));
							}
		case VTYPE_INT		: if(cd != null && cd.cellFormat != null && (cd.cellFormat instanceof DecimalFormat))
										return(cd.cellFormat.format(getIntVal()));
								  else
										return(Integer.toString(getIntVal()));
		case VTYPE_BOOLEAN: if(getBoolVal()) return("Y");else return("N");
		case VTYPE_BYTEARRAY: if(getBaVal() != null) return("[ByteArray]");else return("");
		case VTYPE_DATETIME   : {
//				if (getDateVal() != null) return(getDateVal().toLocaleString()); else return("");
				if (getDateVal() != null) {
					if(getDateFormat() != null) {
						return(getDateFormat().format(getDateVal())); 
					} else {
						return(DateUtil.dateToDateTimeStr(getDateVal())); 
					}
				} else return("");
			}
		case VTYPE_DATE   : if (getDateVal() != null) {
								if(getDateFormat() != null) {
									if(getDateVal().after(DateUtil.minDate)) {
										return(getDateFormat().format(getDateVal())); 
									} else {
										return("");
									}
								} else {
									return(DateUtil.toDateString(getDateVal(), "yyyy/mm/dd"));
								}
							} else
		                       return("");
		default 			   : return("Unknown Value");
		}
	}
	public int getInt()
	{
		switch(jxValueType) {
		case VTYPE_STRING : return(StringUtil.toIntNumberOnly(getStrVal()));
		case VTYPE_INT		: return(getIntVal());
		case VTYPE_DOUBLE   : return((int) getDbVal());
		case VTYPE_BOOLEAN: if(getBoolVal()) return(1) ; else return(0);
		case VTYPE_DATETIME : {
			if(getDateVal() != null) {
				int iv = DateUtil.dateToUnixtime(getDateVal()); 
				if(!ignoreTimeZone) {
					return(iv);
				}
				Date d = getDateVal();
				int zonedSec = ZonedDateTime.now().getOffset().getTotalSeconds();
				long time = d.getTime() / 1000;
				if (time < -zonedSec) {
					int h = DateUtil.getHour(d);
					int m = DateUtil.getMinute(d);
					int s = DateUtil.getSecond(d);
					return(h * 3600 + m * 60 + s - zonedSec);
				} else
					return (int)time;
			} else return(0);
		}
		case VTYPE_DATE : if(getDateVal() != null) return(DateUtil.dateToInformix(getDateVal())); else return(0);
		default : return(0);
		}
	}
	
	public Number getNumber(){
		switch(jxValueType) {
		case VTYPE_INT		: return(getIntVal());
		case VTYPE_DOUBLE   : return(getDbVal());
		case VTYPE_BOOLEAN: if(getBoolVal()) return(1) ; else return(0);
		default : return(0);
		}
	}

	public double getDouble()
	{
		switch(jxValueType) {
		case VTYPE_STRING : return(StringUtil.toDoubleNumberOnly(getStrVal()));
		case VTYPE_DOUBLE : return(getDbVal());
		case VTYPE_INT		: return((double) getIntVal());
		case VTYPE_BOOLEAN: if(getBoolVal()) return(1.0) ; else return(0.0);
		case VTYPE_DATE :   
							if(DateUtil.minDate.after(getDateVal())) {
								return(Double.NaN);
							} else {
								return((double) DateUtil.dateToInformix(getDateVal()));
							}
		default : return(0.0);
		}
	}

	public boolean getBoolean()
	{
		switch(jxValueType) {
		case VTYPE_BOOLEAN : return(getBoolVal());
		case VTYPE_INT     : return((getIntVal() != 0));
		case VTYPE_STRING  : return("Y".equals(getStrVal() ));
		case VTYPE_DOUBLE  : return((getDbVal() != 0.0));
		default	: return(false);
		}
	}
	public byte[] getByteArray()
	{
		if(jxValueType != VTYPE_BYTEARRAY) return(null);
		return(getBaVal());
	}
	public java.util.Date getDate()
	{
		if(jxValueType == VTYPE_STRING) return(DateUtil.dateTimeStrToDate(getStrVal()));
		if(jxValueType != VTYPE_DATE && jxValueType != VTYPE_DATETIME) return(null);
		return(getDateVal());
	}
	public CellObjWrapper getObjWrapper()
	{
		if(jxValueType != VTYPE_OBJWRAPPER) return(null);
		return(getObjWrapperVal());
	}
	public void setHint(String p_hint)
	{
		initcd();
		cd.hintContent = p_hint;
		if(cd.valueMapperList != null) {
			for(int i=0;i<cd.valueMapperList.size();i++) {	
				cd.valueMapperList.get(i).cellMap_hintchange(this);
			}
		}
	}
	public String getHint()
	{
		if(cd == null) return(null);
		return(cd.hintContent);
	}
	public void syncHint(String p_hint)
	{
		initcd();
		cd.hintContent = p_hint;
	}
	protected void notifyModeChange() throws CellException {
		if(cd != null && cd.valueMapperList != null) {
			for(int i=0;i<cd.valueMapperList.size();i++) {
				cd.valueMapperList.get(i).cellMap_modechange(this);
			}
			if(cd.modeChangeAction != null) 
				cd.modeChangeAction.cellAction_onchange(this);
		}
		
	}
	public void setMode(int p_mode) throws CellException
	{
		int orgmode = jxValueMode;
		jxValueMode = (short) p_mode;
		notifyModeChange();
		/*
		if(cd != null && cd.valueMapperList != null) {
			for(int i=0;i<cd.valueMapperList.size();i++) {
				cd.valueMapperList.get(i).cellMap_modechange(this);
			}
			if(orgmode != p_mode && cd.modeChangeAction != null) 
						cd.modeChangeAction.cellAction_onchange(this);
		}
		*/
	}
	
	/*
	public void syncMode(int p_mode) throws CellException
	{
		int orgMode;
		orgMode = jxValueMode;
		if(jxValueMode != p_mode){
			jxValueMode = p_mode;
			if(orgMode == VMODE_OVERRIDED) {
				switch(jxValueType) {
				case VTYPE_INT : 
							LogUtil.log("syncMode restore " + cd.setInt);
							set(cd.setInt); 
							break;
				case VTYPE_STRING : set(cd.setStr); break;
				case VTYPE_DOUBLE: set(cd.setDb); break;
				case VTYPE_BOOLEAN: set(cd.setBool); break;
				case VTYPE_DATE:
				case VTYPE_DATETIME:
						set(cd.setDate);break;
				}
			}
			if(cd.valueMapperList != null) {
			for(int i=0;i<cd.valueMapperList.size();i++) {
				cd.valueMapperList.get(i).cellMap_modechange(this);
			}
			}
			if(cd != null && cd.modeChangeAction != null) 
						cd.modeChangeAction.cellAction_onchange(this);
		}
	}
	*/
	public int getMode()
	{
		return(jxValueMode);
	}
	public void addAction(CellValueAction p_action)
	{	
		initcd();
		if(cd.actionList == null) {
			cd.actionList = new Hashtable();
		}
		cd.actionList.put(p_action,p_action);
	}
	public void delAction(CellValueAction p_action)
	{
		if(cd == null) return;
		if(cd.actionList == null) return;
		cd.actionList.remove(p_action);
	}
	public void clearAction()
	{
		if(cd == null) return;
		cd.actionList = null;
	}
	void addTrigger(Cell p_val)
	{	
		initcd();
		if(cd.triggerList == null) {
			cd.triggerList = new Hashtable<Integer,Cell>();
		}
		cd.triggerList.put(p_val.hashCode(),p_val);
	}
	void delTrigger(Cell p_val)
	{
		if(cd == null) return;
		if(cd.triggerList == null) return;
		cd.triggerList.remove(p_val.hashCode());
	}
	public void setValidation(CellValidation p_validation)
	{
		initcd();
		cd.validation = p_validation;
	}
	public CellValidation getValidation()
	{
		if(cd == null) return(null);
		return(cd.validation);
	}
	public void setFormat(DecimalFormat p_format)
	{
		setFormat((Format) p_format);
	}
	public void setFormat(Format p_format)
	{
		initcd();
		cd.cellFormat = p_format;		
		if(cd.valueMapperList != null) {
			
			for(int i=0;i<cd.valueMapperList.size();i++) {	
//				cd.valueMapperList.get(i).cellMap_formatchange(this,cd.format.toPattern());
				cd.valueMapperList.get(i).cellMap_formatchange(this);
			}
		}
	}
	public Object clone() {
	   switch (getType()) {
         case VTYPE_INT:
	         return(new Cell(getInt()));
         case VTYPE_STRING:
	         return(new Cell(getString()));
         case VTYPE_DOUBLE:
	         return(new Cell(getDouble()));
         case VTYPE_DATETIME:
        	 Cell cc = new Cell(getDate());
        	 cc.jxValueType = VTYPE_DATETIME;
	         return(cc);
         case VTYPE_DATE :
	         return(new Cell(getDate()));
         case VTYPE_BOOLEAN:
	         return(new Cell(getBoolean()));
         case VTYPE_CHOICE:
			   return(null);
         case VTYPE_SET:
	         return(null);
         case VTYPE_BYTEARRAY:
	         return(new Cell(getByteArray()));
		}
		return(null);
	}
	public String getTypeInXmlAndAttribute(String p_cname) {
	   String t = getTypeInXml();
		if (t.equals("I") && p_cname.endsWith("time")) {
         if (getAttribute("ts") == null) {
	         long t0 = getInt();
			   t0 = t0 * 1000;
			   return(t+"\" ts=\""+DateUtil.toTimeString(new java.util.Date(t0), "yyyy/mm/dd HH:MM:SS")); 
		   }
		}
		return(t);
	}
	public String getTypeInXml() {
	   switch (getType()) {
         case VTYPE_INT:
		      return("I");
         case VTYPE_STRING:
	         return("S");
         case VTYPE_DOUBLE:
	         return("DB");
         case VTYPE_DATETIME :
         case VTYPE_DATE :
	         return("DT");
         case VTYPE_BOOLEAN:
	         return("B");
         case VTYPE_CHOICE:
	         return("CH");
         case VTYPE_SET:
	         return("ST"); 
			case VTYPE_BYTEARRAY:
	         return("BA"); 
         case VTYPE_OBJWRAPPER:
	         return("OW"); 
		}
		return(null);
	}
	private void initcd()
	{
		if(cd == null) {
			cd = new CellDetail();
			switch(jxValueType) {
			case  VTYPE_INT  : cd.hahaabc123Value = ((Integer) hahaabc123valObj).intValue();
			/* no need to call cellUpdated as the value set to cd during initcd is just copied from cell's valObj */
//										cellUpdated();
										break;
			case  VTYPE_DOUBLE: cd.hahaabc123Value = ((Double) hahaabc123valObj).doubleValue();
//										cellUpdated();
										break;
			case  VTYPE_BOOLEAN: cd.hahaabc123Value = ((Boolean) hahaabc123valObj).booleanValue();
//										cellUpdated();
										break;
			case  VTYPE_STRING: cd.hahaabc123Value = (String) hahaabc123valObj;
//										cellUpdated();
										break;
			case  VTYPE_DATETIME: 
			case  VTYPE_DATE: cd.hahaabc123Value = (java.util.Date) hahaabc123valObj;
//										cellUpdated();
										break;
			}
		}
	}
	private int getIntVal()
	{
		return(((Integer) hahaabc123valObj).intValue());
	}
	private double getDbVal() 
	{
		if(!(hahaabc123valObj instanceof Double)) {
			if(hahaabc123valObj instanceof String) {
				if(StringUtils.isBlank((String) hahaabc123valObj)) return(0.0);
				try {
				double dd = Double.parseDouble((String) hahaabc123valObj);
					return(dd);
				} catch (NumberFormatException nex) {
					LogUtil.log(nex);
					return(0.0);
				}
			}
		}
		return(((Double) hahaabc123valObj).doubleValue());
	}
	private boolean getBoolVal()
	{
		return(((Boolean) hahaabc123valObj).booleanValue());
	}
	private String getStrVal()
	{
		return((String) hahaabc123valObj);
	}
	private byte[] getBaVal()
	{
		return((byte[]) hahaabc123valObj);
	}
	private java.util.Date getDateVal()
	{
		return((java.util.Date) hahaabc123valObj);
	}
	private CellObjWrapper getObjWrapperVal()
	{
		return((CellObjWrapper) hahaabc123valObj);
	}

	private void setSetVal(AbstractSet p_set)
	{
		/* allway call update for AbstractSet, handle later */
		cellUpdated();
		hahaabc123valObj = p_set;
	}
	private void setIntVal(int p_int)
	{
		if(hahaabc123valObj != null && (((Integer) hahaabc123valObj).intValue() != p_int )) cellUpdated();
		hahaabc123valObj = new Integer(p_int);
	}
	private void setDbVal(double p_db) 
	{
		if(hahaabc123valObj != null && (((Double) hahaabc123valObj).doubleValue() != p_db)) cellUpdated();
		hahaabc123valObj = new Double(p_db);
	}
	private void setBoolVal(boolean p_bol)
	{
		if(hahaabc123valObj != null && (((Boolean) hahaabc123valObj).booleanValue() != p_bol)) cellUpdated();
		hahaabc123valObj = new Boolean(p_bol);
	}
	private void setStrVal(String s)
	{
		if(!StringUtils.equals((String) hahaabc123valObj, s)) cellUpdated();
		hahaabc123valObj = (Object) s;
	}

	private void setBaVal(byte[] p_ba)
	{
		/* allway call update for AbstractSet, handle later */
		cellUpdated();
		hahaabc123valObj = (Object) p_ba;
	}
	private void setDateVal(java.util.Date p_date)
	{
		if(!DateUtil.equals(p_date,(Date) hahaabc123valObj)) {
			cellUpdated();
		}
		hahaabc123valObj = (Object) p_date;
	}
	private void setObjWrapperVal(CellObjWrapper p_obj)
	{
		hahaabc123valObj = (Object) p_obj;
		cellUpdated();
	}
	public static Cell newCell(Object p_object) throws Exception {
		if (p_object == null)
		   throw(new Exception("p_object is null"));
	   if (p_object instanceof String)
		   return(new Cell((String) p_object));
	   else if (p_object instanceof Integer)
		   return(new Cell(((Integer) p_object).intValue()));
	   else if (p_object instanceof Double)
		   return(new Cell(((Double) p_object).doubleValue()));
	   else if (p_object instanceof Boolean)
		   return(new Cell(((Boolean) p_object).booleanValue()));
	   else if (p_object instanceof byte[])
		   return(new Cell((byte[]) p_object));
	   else if (p_object instanceof Date)
		   return(new Cell((Date) p_object));
	   else
		   throw(new Exception("Type not supported "+p_object.getClass().getName()));
	}
	public boolean equals(Object o) {
		switch(jxValueType) {
		   case VTYPE_STRING : 
					if(! (o instanceof String) ) return(false);
					return(getString().equals((String) o));
		   case VTYPE_DOUBLE : 
					if(! (o instanceof Double) ) return(false);
					if(Double.isNaN(getDouble()) && Double.isNaN((Double) o)) {
						return(true);
					}
					return(getDouble() == ((Double) o).doubleValue());
		   case VTYPE_INT		: 
					if(! (o instanceof Integer) ) return(false);
					return(getInt() == ((Integer) o).intValue());
		   case VTYPE_BOOLEAN: 
					if(! (o instanceof Boolean) ) {
						if(o instanceof String) {
							if("Y".equals(o)) return(getBoolean());
							if("是".equals(o)) return(getBoolean());
							return(!getBoolean());
						}
						return(false);
					}
					return(getBoolean() == ((Boolean) o).booleanValue());
		   case VTYPE_BYTEARRAY: 
					if(! (o instanceof byte[]) ) return(false);
					return(getByteArray().equals((byte[]) o));
		   case VTYPE_DATETIME   : 
					if(! (o instanceof java.util.Date) ) return(false);
			   		if(DateUtil.minDate.after((Date) o) &&
			   			DateUtil.minDate.after(getDate())
			   				) return(true);
					return(getDate().equals((java.util.Date) o));
		   case VTYPE_DATE   :  {
			   		Date d0;
			   		Date d1;
			   		d0 = getDate();
			   		if(d0 == null || DateUtil.minDate.after(d0)) {
			   			d0 = DateUtil.zeroDate;
			   		}
			   		d1 = (java.util.Date) o;
			   		if(d1 == null || DateUtil.minDate.after(d1)) {
			   			d1 = DateUtil.zeroDate;
			   		}
			   		return( DateUtils.truncate(d0, Calendar.DATE).equals (
			   				DateUtils.truncate(d1, Calendar.DATE)
			   				));
		   		}
		   default 			   : 
					return(false);
		}
	}
	public boolean equalsIgnoreCase(Object o) {
		if(jxValueType == VTYPE_STRING) {
					if(! (o instanceof String) ) return(false);
					return(getString().equalsIgnoreCase((String) o));
		} else return(equals(o));
	}
	public boolean equals(Cell p_cell) {
//		if (jxValueType != p_cell.jxValueType)
//		   return(false);
		switch(jxValueType) {
		   case VTYPE_STRING : return(getString().equals(p_cell.getString()));
		   case VTYPE_DOUBLE : return(getDouble() == p_cell.getDouble());
		   case VTYPE_INT		: return(getInt() == p_cell.getInt());
		   case VTYPE_BOOLEAN: return(getBoolean() == p_cell.getBoolean());
		   case VTYPE_BYTEARRAY: return(getByteArray().equals(p_cell.getByteArray()));
		   case VTYPE_DATETIME   : 
			   		return(getDate().equals(p_cell.getDate()));
		   case VTYPE_DATE   : {
			   		Date d0;
			   		Date d1;
			   		d0 = getDate();
			   		if(d0 == null || DateUtil.minDate.after(d0)) {
			   			d0 = DateUtil.zeroDate;
			   		}
			   		d1 = p_cell.getDate();
			   		if(d1 == null || DateUtil.minDate.after(d1)) {
			   			d1 = DateUtil.zeroDate;
			   		}
			   		return( DateUtils.truncate(d0, Calendar.DATE).equals (
			   				DateUtils.truncate(d1, Calendar.DATE)
			   				));
		   		}
		   default 			   : 
			   Object xx = null;
				xx.hashCode();  // make it null pointer exception
				return(false);
		}
	}
	public int compareTo(Cell p_cell) {
		switch(jxValueType) {
		   case VTYPE_STRING : 
					return(compareTo(p_cell.getString()));
		   case VTYPE_DOUBLE : 
					return(compareTo(p_cell.getDouble()));
		   case VTYPE_INT		: 
					return(compareTo(p_cell.getInt()));
		   case VTYPE_DATETIME   : 
		   case VTYPE_DATE   : 
					return(compareTo(p_cell.getDate()));
		   default 			   : 
					return(0);
		}
	}
	
	public int compareTo(Object o) {
		switch(jxValueType) {
		   case VTYPE_STRING : 
					return(getString().compareTo(o.toString()));
		   case VTYPE_DOUBLE : 
//					if(! (o instanceof Double) ) return(false);
					return(((Double) getObject()).compareTo((Double) o ));
		   case VTYPE_INT		: 
					return(((Integer) getObject()).compareTo((Integer) o ));
		   case VTYPE_DATETIME   : 
			   		if(DateUtil.minDate.after((Date) o) &&
			   			DateUtil.minDate.after(getDate())
			   				) return(0);
					return(getDate().compareTo((java.util.Date) o));
		   case VTYPE_DATE   :  {
			   		Date d0;
			   		Date d1;
			   		d0 = getDate();
			   		if(d0 == null || DateUtil.minDate.after(d0)) {
			   			d0 = DateUtil.zeroDate;
			   		}
			   		d1 = (java.util.Date) o;
			   		if(d1 == null || DateUtil.minDate.after(d1)) {
			   			d1 = DateUtil.zeroDate;
			   		}
			   		return( DateUtils.truncate(d0, Calendar.DATE).compareTo(
			   				DateUtils.truncate(d1, Calendar.DATE)
			   				));
		   		}
		   default 			   : 
					return(0);
		}
	}
	public int compareToIgnoreCase(Object o) {
		if(jxValueType == VTYPE_STRING) {
			return(getString().compareToIgnoreCase(o.toString()));
		} else return (compareTo(o));
	}
	public static Cell[] toCellArray(Vector p_v) throws Exception {
	   Cell[] cells = new Cell[p_v.size()];
		for (int i=0; i<p_v.size(); i++) {
	      cells[i] = newCell(p_v.elementAt(i));
		}
		return(cells);
	}
   public Cell setAttribute(String p_attrName, Object p_attrValue) {
	   initcd();
		if (cd.attributes == null)
		   cd.attributes = new Hashtable();
		cd.attributes.put(p_attrName, p_attrValue);
		return(this);
	}
   public Object getAttribute(String p_attrName) {
	   if (cd == null)
		   return(null);
	   if (cd.attributes == null)
		   return(null);
	   return(cd.attributes.get(p_attrName));
	}
   public Hashtable getAttributes() {
	   if (cd == null)
		   return(null);
	   return(cd.attributes);
	}
	public void setupTsAttribute() {
	   long t0 = getInt();
		t0 = t0 * 1000;
      setAttribute("ts", DateUtil.toTimeString(new java.util.Date(t0), "yyyy/mm/dd HH:MM:SS"));
	}
	public void dump()
	{
		LogUtil.log("Dump Cell addr " + this);
		LogUtil.log("Dump Cell mode " + jxValueMode);
		LogUtil.log("Dump Cell Value" + getString());
	}

	public Object getSetObject() {
		if(cd == null) return(null);
		switch(jxValueType) {
		case VTYPE_INT	 : return((Integer) cd.hahaabc123Value);
		case VTYPE_STRING: return((String) cd.hahaabc123Value);
		case VTYPE_DATETIME:
		case VTYPE_DATE : return((Date) cd.hahaabc123Value);
		case VTYPE_DOUBLE: return((Double) cd.hahaabc123Value);
		case VTYPE_BOOLEAN: return((Boolean) cd.hahaabc123Value);
		}
		return(null);
		/*
	public  static final int VTYPE_INT			= 0;
	public  static final int VTYPE_STRING		= 1;
	public  static final int VTYPE_DOUBLE		= 2;
	public  static final int VTYPE_DATE 		= 3;
	public  static final int VTYPE_BOOLEAN		= 4;
	public  static final int VTYPE_CHOICE		= 5;
	public  static final int VTYPE_SET			= 6;
	public  static final int VTYPE_BYTEARRAY  = 7;
	public  static final int VTYPE_DATETIME 		= 8;
	public  static final int VTYPE_OBJWRAPPER	= 9;
	public  static final int VTYPE_NULL = 999;
		}
		*/
	}
	
	public String getSetString()
	{
		if(cd != null) return((String) cd.hahaabc123Value); else return("");
	}
	public Date getSetDate()
	{
		if(cd != null) return((Date) cd.hahaabc123Value); else return(DateUtil.zeroDate);
	}
	public double getSetDb()
	{
		if(cd != null) return((Double) cd.hahaabc123Value); else return(0.0);
	}
	public int getSetInt()
	{
		if(cd != null) return((Integer) cd.hahaabc123Value); else return(0);
	}

	public CellValueAction setModeChangeAction (CellValueAction p_action)
	{
		initcd();
		CellValueAction oAction = cd.modeChangeAction;
		cd.modeChangeAction = p_action;
		return(oAction);
	}
	public DecimalFormat getDecFormat()
	{
		if(cd == null || cd.cellFormat == null || !(cd.cellFormat instanceof DecimalFormat)) return(null);
		return((DecimalFormat) cd.cellFormat);
	}
	public DateFormat getDateFormat()
	{
		if(cd == null || cd.cellFormat == null || !(cd.cellFormat instanceof DateFormat)) return(null);
		return((DateFormat) cd.cellFormat);
	}
	
	public void resetValue() throws CellException
	{
		/*
		if(jxValueMode == VMODE_OVERRIDED) {
			setMode(VMODE_PROTECTED);
		}
		*/
		if(cd != null && cd.isOverrided) {
			cd.isOverrided = false;
			notifyModeChange();
		}
		if(getFormula() != null && !getFormula().equals("")) {
			if(eval()) return;
		}
		switch(jxValueType) {
		case VTYPE_INT :		
								setIntVal(0);
								if(cd != null) {
									if(((Integer) cd.hahaabc123Value) != getIntVal()) cellUpdated();
									cd.hahaabc123Value = getIntVal();
								}
								break;
		case VTYPE_DOUBLE:		setDbVal(0.0);
								if(cd != null) {
									if(((Double)cd.hahaabc123Value)  != getDbVal()) cellUpdated();
									cd.hahaabc123Value = getDbVal();
								}
								break;
		case VTYPE_STRING :		
								setStrVal("");
								if(cd != null) {
									if(!StringUtils.equals(((String) cd.hahaabc123Value), getStrVal())) cellUpdated();
									cd.hahaabc123Value = getStrVal();
								}
								break;
		case VTYPE_DATETIME   :		
		case VTYPE_DATE   :		
								setDateVal(com.kikyosoft.utils.DateUtil.zeroDate);
								if(cd != null) {
									if(!DateUtil.equals((Date) cd.hahaabc123Value, getDateVal())) {
										cellUpdated();
									}
									cd.hahaabc123Value = getDateVal();
								}
								break;
		case VTYPE_BOOLEAN:		setBoolVal(false);
								if(cd != null) {
									if((Boolean) cd.hahaabc123Value != getBoolVal()) {
										cellUpdated();
									}
									cd.hahaabc123Value = getBoolVal();
								}
								break;
		default : 
		}
		if(cd != null) {
		if(cd.valueMapperList != null) {
			for(int i=0;i<cd.valueMapperList.size();i++) {
				cd.valueMapperList.get(i).cellMap_valchange(this);
			}
		}
		checkActionAndTrigger();
		}
	}
	
	
//	public void resetValue(boolean p_forceTrigger) throws CellException
//	{
//		if(jxValueMode == VMODE_OVERRIDED) {
//			setMode(VMODE_PROTECTED);
//		}
//		switch(jxValueType) {
//		case VTYPE_INT :		set(0);
//								break;
//		case VTYPE_DOUBLE:		set(0.0);
//								break;
//		case VTYPE_STRING :		set("");
//								break;
//		case VTYPE_DATETIME   :		
//		case VTYPE_DATE   :		set(com.uniinformation.utils.DateUtil.zeroDate);
//								break;
//		case VTYPE_BOOLEAN:		set(false);
//								break;
//		default : 
//		}
//	}
	
	public boolean isBlank()
	{
		String s = getString();
		switch(jxValueType ) {
		case VTYPE_INT : return(getInt() == 0);
		case VTYPE_DOUBLE : return(getDouble() == 0.0);
		case VTYPE_DATETIME : 
		case VTYPE_DATE : {
				if(getDate() == null || !getDate().after(DateUtil.minDate)) return(true); else return(false);
			}
		default : 
				s = getString();
				return(s == null || s.trim().equals(""));
		}
	}
	void setIgnoreFormula(boolean p_sw) throws CellException
	{
		if(cd == null  || p_sw == cd.ignoreFormula) return;
		cd.ignoreFormula = p_sw;
		if(cd.valueMapperList != null) {
		for(int i=0;i<cd.valueMapperList.size();i++) {
			cd.valueMapperList.get(i).cellMap_formulachange(this);
		}
		}
	}
	public boolean getIgnoreFormula()
	{
		if(cd == null) return(false);
		return(cd.ignoreFormula);
	}
	
	public CellValueMapper getMapper() {
		if(cd == null) return(null);
		if(cd.valueMapperList == null || cd.valueMapperList.size() <= 0) return(null);
		return(cd.valueMapperList.get(0));
	}
	
	public String toString() {
		return(getString());
	}
	
	public void setDateTime(boolean  p_flag,boolean p_ignoreTimeZone) {
		if(jxValueType != VTYPE_DATE && jxValueType != VTYPE_DATETIME) return;
		if(p_flag) {
			jxValueType = VTYPE_DATETIME;
			ignoreTimeZone = p_ignoreTimeZone;
		} else {
			jxValueType = VTYPE_DATE;
			Date d = (Date) hahaabc123valObj;
			if(d != null) {
				hahaabc123valObj = DateUtil.informixToDate(DateUtil.dateToInformix(d));
				cellUpdated();
			}
			if(cd != null) {
				d = (Date) cd.hahaabc123Value ;
				if(d != null) {
					cd.hahaabc123Value = DateUtil.informixToDate(DateUtil.dateToInformix(d));
					cellUpdated();
				}
			}
		}
	}
	
	public String getCellLabel() {
//		return(cd == null ? null : cd.cellLabel);
		return(cellLabel);
	}
	public void setCellLabel(String p_label) {
//		initcd();
//		cd.cellLabel = p_label;
		cellLabel=p_label;
	}
	public boolean isOverrided() {
		if(cd != null) return(cd.isOverrided) ; else return(false);
	}
	public void clearOverride()  throws CellException
	{
		if(cd.isOverrided) {
			cd.isOverrided=false;
			switch(jxValueType) {
				case VTYPE_INT : set((Integer) cd.hahaabc123Value); break;
				case VTYPE_STRING : set((String) cd.hahaabc123Value); break;
				case VTYPE_DOUBLE: set((Double) cd.hahaabc123Value); break;
				case VTYPE_BOOLEAN: set((Boolean) cd.hahaabc123Value); break;
				case VTYPE_DATE:
				case VTYPE_DATETIME: set((Date) cd.hahaabc123Value);break;
			}
		}
		notifyModeChange();
	}
	
	protected void cellUpdated() {
	}
	
	public String getColumnDisplayString() {
		return(getString());
	}
	
	public void clearAllOverrides() throws CellException {
		if(cd == null) return;
		if(cd.evaluatingChild && cd.isOverrided) {
			LogUtil.log("clearAllOverrider Recursive Detected, return immediately");
			return;
		}
		if(inClearAllOverride == true) {
			throw new CellException("ClearAllOverride Recurrance Inhibited");
		}
		inClearAllOverride = true;
		clearOverride();
		if(cd.triggerList != null) {
			cd.evaluatingChild = true;
			for(Cell cc : cd.triggerList.values()) {
				if(cc == this) {
					LogUtil.log("Warning !!! ignore clearAllOverride to parent cell itself");
					continue;
				}
				cc.clearAllOverrides();
			}
			cd.evaluatingChild = false;
		}
		inClearAllOverride = false;
	}
	
	public void protect(boolean sw) throws CellException{
		if(cd == null) {
			if(!sw) return;
			initcd();
		}
		if(sw == cd.isProtected) return;
		cd.isProtected = sw;
		notifyModeChange();
	}
	public boolean isProtected() {
		if(cd == null) return(false);
		return(cd.isProtected);
	}
	
	public void setIgnoreEncode(boolean p_sw) {
		if(cd == null  || p_sw == cd.ignoreEncode) return;
		cd.ignoreEncode = p_sw;
	}
	public boolean getIgnoreEncode() {
		return (cd == null ? false : cd.ignoreEncode);
	}
	
	public int getFlag() {
		return(flag);
	}
	public void setFlag(int p_flag) {
		flag = (short) p_flag;
	}
	static public boolean objectIsBlank(Object p_obj) {
		if(p_obj instanceof Integer) {
			return(((Integer) p_obj) == 0);
		} else if(p_obj instanceof Long ) {
			return(((Long) p_obj) == 0L);
		} else if(p_obj instanceof Double ) {
			return(((Double) p_obj) == 0.0);
		} else if(p_obj instanceof java.util.Date) {
			return(DateUtil.minDate.after((java.util.Date) p_obj));
		} else if(p_obj instanceof Short ) {
			return(((Short) p_obj) == 0);
		} else {
			return(StringUtils.isBlank(p_obj.toString()));
		}
	}
	static public int objectToInt(Object p_obj) {
		if(p_obj instanceof Integer) {
			return((Integer) p_obj);
		} else if(p_obj instanceof Long ) {
			long l = (Long) p_obj;
			return( (int) l);
		} else if(p_obj instanceof Double ) {
			double d = (Double) p_obj;
			return( (int) d);
		} else if(p_obj instanceof Short ) {
			short s = (Short) p_obj;
			return( (int) s);
		} else {
			return(Integer.parseInt(p_obj.toString()));
		}
	}
	static public double objectToDouble(Object p_obj) {
		if(p_obj instanceof Double) {
			return((Double) p_obj);
		} else if(p_obj instanceof Integer ) {
			int i = (Integer) p_obj;
			return((double) i);
		} else if(p_obj instanceof Long ) {
			long l = (Long) p_obj;
			return((double) l);
		} else if(p_obj instanceof Short ) {
			short s = (Short) p_obj;
			return( (double) s);
		} else {
			return(Double.parseDouble(p_obj.toString()));
		}
	}	
	
	static public int objectCompare(Object p_obj,Object p_value, boolean p_ignoreCaseAndTime) {
		if(p_obj == null) {
			if(p_value != null ) return(-objectCompare(p_value,p_obj,p_ignoreCaseAndTime)); else return(0);
		} 
		if(p_value == null) return(1);
		if(p_obj instanceof Double) {
			double v;
			if(p_value instanceof Double) {
				v = (Double) p_value;
			} else if(p_value instanceof Integer ){
				int i = (Integer) p_value;
				v = (double) i;
			} else {
				v = Double.parseDouble(p_value.toString());
			}
			return((Double) p_obj).compareTo(v);
		} else if(p_obj instanceof Integer) {
			int i;
			if(p_value instanceof Integer) {
				i = (Integer) p_value;
			} else if(p_value instanceof Double){
				double d = (Double) p_value;
				i = (int) d;
			} else {
				i = Integer.parseInt(p_value.toString());
			}
			return((Integer) p_obj).compareTo(i);
		} else if(p_obj instanceof Date) {
			Date d0 = (Date) p_obj;
			if(DateUtil.minDate.after(d0)) d0 = DateUtil.zeroDate;
			Date d1;
			if(p_value instanceof Date) {
				d1 = (Date) p_value;
			} else {
				d1 = DateUtil.dateTimeStrToDate(p_value.toString());
			}
			if(DateUtil.minDate.after(d1)) d1 = DateUtil.zeroDate;
			if(p_ignoreCaseAndTime ) {
			   	return( DateUtils.truncate(d0, Calendar.DATE).compareTo(
			   				DateUtils.truncate(d1, Calendar.DATE)
			   	));
			} else {
				return(d0.compareTo(d1));
			}
		} else {
			if(p_ignoreCaseAndTime ) {
				return(p_obj.toString().compareToIgnoreCase(p_value.toString()));
			} else {
				return(p_obj.toString().compareTo(p_value.toString()));
			}
		}
	}

	public boolean setDirty() {
		if(cd == null) return(false);
		cd.isDirty = true;
		return(true);
	}
	
	public boolean isDirty() {
		return(cd == null ? false : cd.isDirty);
	}
	public void clearDirty() {
		if(cd != null) cd.isDirty =  false;
	}
	
	
	public Collection<Cell> getTriggerCells() {
		if(cd == null || cd.triggerList == null) return(null); else return(cd.triggerList.values());
	}
	
	static public int getCellCount() {
//		return(cellCount);
		return(atomicCellCount.get());
	}
	static public int getCdCount() {
//		return(cdCount);
		return(atomicCdCount.get());
	}
	public boolean isTimeOnly() {
		return(ignoreTimeZone);
	}
}
