package com.kyoko.parser;

import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;

public class Variable {
	String varName ;
	VariableInterface vInterface;
	boolean idxAbsolute = false;
	int idx ;
	public String getName() {
		
		return(varName);
	}
	public Variable (String p_fieldname,VariableInterface p_interface) {
		varName = p_fieldname;
		vInterface = p_interface;
		idx = 0;
	}
	public Variable (String p_fieldname,VariableInterface p_interface,int p_index,boolean p_idxAbsolute) {
		varName = p_fieldname;
		vInterface = p_interface;
		idx = p_index;
		idxAbsolute = p_idxAbsolute;
	}
	public String toString() {
		if(vInterface != null) {
			String ss = vInterface.toString(varName, idx,idxAbsolute); 
			if(ss != null) return(ss);
		}
		return(toFullName(varName,idx,idxAbsolute));
	}
	public Object collectObject() throws CellException {
		if(vInterface != null) {
			Object ss = vInterface.collectObject(varName, idx,idxAbsolute); 
			if(ss != null) return(ss);
		}
		return(varName);
	}
	public Cell eval(Object p_recData) throws CellException {
		if(vInterface == null) return(null);
		return(vInterface.evalVariable(varName,idx,idxAbsolute,p_recData));
	}
	
	public int getDataType() {
		if(vInterface == null) return(0);
		return(vInterface.getDataType(varName));
	}
	
	public Pair<Boolean,Integer> getRowIndex() {
		return(Pair.of(idxAbsolute, idx));
	}
	
	static public String toFullName(String p_varName,int idx,boolean p_idxAbsolute) {
		if(p_idxAbsolute) {
			if(idx == Integer.MAX_VALUE) {
				return(p_varName+"[]");
			} else {
				return(p_varName+"["+idx+"]");
			}
		} else {
			if(idx == 0) return(p_varName); 
			else if(idx > 0) return(p_varName+"[+"+idx+"]");
			else return(p_varName+"[-"+(-idx)+"]");
		}
	}
}
