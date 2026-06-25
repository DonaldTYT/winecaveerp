package com.kikyosoft.parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.kikyosoft.cell.*;
public class Function {
	String fnName ;
	FunctionInterface fInterface;
	Vector argList;
	public Function (String p_fnName,FunctionInterface p_interface,Vector p_argList) {
		fnName = p_fnName;
		fInterface = p_interface;
		argList = p_argList;
	}
	public String toString() {
		if(fInterface != null) {
			String ss = fInterface.toString(fnName, argList);
			if(ss != null) return(ss);
		}
		return(toFullName(fnName,argList));
	}

	public Object collectObject() throws CellException{
		if(fInterface != null) {
			Object ss = fInterface.collectObject(fnName, argList);
			if(ss != null) return(ss);
		}
		return(fnName);
	}

	public Cell eval(Object p_recdata) throws Exception {
		if(fnName.equalsIgnoreCase("if")) {
			boolean ok = true;
			if(argList.get(0) instanceof Condition) {
				ok = ((Condition) argList.get(0)).eval(p_recdata);
			} else if(argList.get(0) instanceof Expression ) {
				Cell rtn = ((Expression) argList.get(0)).eval(p_recdata);
				ok = rtn.getBoolean();
			} 
			if(ok) {
				if(argList.get(1) instanceof Condition) {
					return (
								new Cell(
										((Condition) argList.get(1)).eval(p_recdata)
										)
							);
				} else {
					Expression expr1 = (Expression) argList.get(1);
					return(expr1.eval(p_recdata));
				}
			} else {
				if(argList.get(2) instanceof Condition) {
					return (
								new Cell(
										((Condition) argList.get(2)).eval(p_recdata)
										)
							);
				} else {
					Expression expr2 = (Expression) argList.get(2);
					return(expr2.eval(p_recdata));
				}
			}
		}
		if(fnName.startsWith("excel")) {
			return(fInterface.evalFunction(fnName,argList,p_recdata));
		}
		if(fInterface == null) return(null);
//		ArrayList arg = new ArrayList(argList.size());
		Vector arg = new Vector();
		if(argList != null) {
			/*
		for(int i=0;i<argList.size();i++) {
			Cell c = ((Expression) argList.get(i)).eval(p_recdata);
			arg.add(c);
		}
			*/
			for(Object o : argList)	 {
				if(o instanceof Expression) arg.add(((Expression) o).eval(p_recdata).getObject()); 
					else if(o instanceof Condition) {
						arg.add(((Condition)o).eval(p_recdata));
					} else arg.add(o);
			}
		}
//		return(fInterface.evalFunction(fnName,argList,p_recdata));
		return(fInterface.evalFunction(fnName,arg,p_recdata));
	}
	
	public int getDataType() {
		if(fInterface == null) return(0);
		return(fInterface.getDataType(fnName));
	}
	
	static public String toFullName(String p_fnName,List p_argList) {
		String s = null;
		if(p_argList == null) return(p_fnName+"()");
		for(Object o : p_argList) {
			if(s == null) {
				s = o.toString();
			} else {
				s += "," + o.toString();
			}
		}
		return(p_fnName+"("+s+")");
	}
}
