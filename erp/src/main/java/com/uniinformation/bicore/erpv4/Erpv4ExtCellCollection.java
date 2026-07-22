package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.parser.Condition;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.bischema.ExcelFunction;
import com.uniinformation.utils.UniLog;

public class Erpv4ExtCellCollection extends Erpv4BaseCellCollection {

	ExcelFunction ef;
	public Erpv4ExtCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
		super(p_parent, p_br);
		ef = new ExcelFunction(this);
		compareMode = Condition.COMPARE_MODE_IGNORECASE | Condition.COMPARE_MODE_STRICTTYPE;
	}

	private enum FuncName { 
		NOT_DEFINED }

	public Object evalFunction(String p_fname,Vector args) throws Exception
	{
		Object rtn = ef.evalFunction(p_fname, args, formulaInit);
		if(rtn != null) return(rtn);
		FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);
		if(p_fname.startsWith("excel")) {
			UniLog.log("excel equivalent Function " + p_fname + " not found ");
			return(0.0);
		} else {
			return(super.evalFunction(p_fname, args));
		}
	}

}
