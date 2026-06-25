package com.uniinformation.utils.whereclpar;

import com.kyoko.parser.VariableInterface;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;

public class Variable {
	String varName ;
	VariableInterface vInterface;
	int idx ;
	public Variable (String p_fieldname,VariableInterface p_interface) {
		varName = p_fieldname;
		vInterface = p_interface;
		idx = 0;
	}
	public Variable (String p_fieldname,VariableInterface p_interface,Expression p_index) {
		varName = p_fieldname;
		vInterface = p_interface;
		idx = 0;
	}
	public String toString() {
		if(vInterface != null) return(vInterface.toString(varName, idx,false)); else return(toString(varName,idx));
	}
	public Object getObject() throws CellException {
		if(vInterface != null) return(vInterface.collectObject(varName, idx,false)); else return(toString(varName,idx));
	}
	public Cell eval(Object p_recData) throws CellException {
		if(vInterface == null) return(null);
		return(vInterface.evalVariable(varName,idx,false,p_recData));
	}
	
	public int getDataType() {
		if(vInterface == null) return(0);
		return(vInterface.getDataType(varName));
	}
	
	static public String toString(String p_varName,int idx) {
		if(idx == 0) return(p_varName); else return(p_varName+'['+idx+"]");
	}
}
