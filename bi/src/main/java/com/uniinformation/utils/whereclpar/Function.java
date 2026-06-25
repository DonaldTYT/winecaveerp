package com.uniinformation.utils.whereclpar;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.kyoko.parser.FunctionInterface;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
public class Function {
	String fnName ;
	FunctionInterface fInterface;
	List argList;
	public Function (String p_fnName,FunctionInterface p_interface,List p_argList) {
		fnName = p_fnName;
		fInterface = p_interface;
		argList = p_argList;
	}
	public String toString() {
		if(fInterface != null) return(fInterface.toString(fnName, argList));
		return(toString(fnName,argList));
	}
//	public Object getObject() throws CellException{
//		if(fInterface != null) return(fInterface.getObject(fnName, argList));
//		return(toString(fnName,argList));
//	}

	public Cell eval(Object p_recdata) throws Exception {
		if(fInterface == null) return(null);
//		ArrayList arg = new ArrayList(argList.size());
		Vector arg = new Vector();
		if(argList != null) {
		for(int i=0;i<argList.size();i++) {
			Cell c = ((Expression) argList.get(i)).eval(p_recdata);
			arg.add(c);
		}
		}
//		return(fInterface.evalFunction(fnName,argList,p_recdata));
		return(fInterface.evalFunction(fnName,arg,p_recdata));
	}
	
	public int getDataType() {
		if(fInterface == null) return(0);
		return(fInterface.getDataType(fnName));
	}
	
	static public String toString(String p_fnName,List p_argList) {
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
