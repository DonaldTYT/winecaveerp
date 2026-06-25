package com.kyoko.parser;

import java.util.ArrayList;
import java.util.List;

public class VariableSet {
	public final int VARSET_RANGE  = 0;
	public final int VARSET_LIST   = 1;
	int type;
	Variable vStart;
	Variable vEnd;
	List<Variable> varList;
//	public VariableSet(Expression p_vStart,Expression p_vEnd) throws Exception {
//		type = VARSET_RANGE;
//		vStart = p_vStart.getVariable();
//		vEnd = p_vEnd.getVariable();
//	}
//	public VariableSet(List<Expression> p_varList) {
//		type = VARSET_LIST;
//		varList = new ArrayList<Variable>();
//		for(Expression exp : p_varList) {
//			varList.add(exp.getVariable());
//		}
//	}
	public Variable getStart() {
		if(type != VARSET_RANGE) return(null);
		return(vStart);
	}
	public Variable getEnd() {
		if(type != VARSET_RANGE) return(null);
		return(vEnd);
	}
	public VariableSet(Variable p_vStart,Variable p_vEnd) throws Exception {
		type = VARSET_RANGE;
		vStart = p_vStart;
		vEnd = p_vEnd;
	}
	public VariableSet(List<Variable> p_varList) {
		type = VARSET_LIST;
		varList = p_varList;
	}	
	public String toString() {
		if(type == VARSET_RANGE) {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			sb.append(vStart.toString());
			sb.append(":");
			sb.append(vEnd.toString());
			sb.append("]");
			return(sb.toString());
		}
		if(type == VARSET_LIST) {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			for(int i = 0;i<varList.size();i++) {
				if(i > 0) sb.append(",");
				sb.append(varList.get(i));
			}
			sb.append("]");
			return(sb.toString());
		}
		return(null);
	}
}
