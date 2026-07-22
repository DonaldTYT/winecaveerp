package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;

public class QuotationG2CellCollection extends Erpv4BaseCellCollection {
	private enum FuncName { FUNC_getQuoMode, NOT_DEFINED };

	public QuotationG2CellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
		super(p_parent, p_br);
		// TODO Auto-generated constructor stub
	}
	@Override
	public Object evalFunction(String p_fname,Vector p_args) throws Exception {
		FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);		
		if(formulaInit != null) return(super.evalFunction(p_fname, p_args));
		switch (funcName){
		case FUNC_getQuoMode: 
			if(br == null) return("");
			BiResultQuotation.QUOMODE qm = ((BiResultQuotation) br).getQuomode();
			if(qm == null) return("");
			return(qm.name());
		}
		return(super.evalFunction(p_fname,p_args) );
	}
}
