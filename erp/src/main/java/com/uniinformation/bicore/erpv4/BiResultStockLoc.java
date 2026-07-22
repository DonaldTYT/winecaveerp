package com.uniinformation.bicore.erpv4;

import java.util.List;
import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockLoc extends BiResultErpv4 {

	public BiResultStockLoc(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	@Override
    protected String brEvalFunction(String p_functName,List p_args) {
    	if(p_functName.equals("erpv4GetCocode")) {
    		return(Erpv4Config.getDefaultCoCode(sh));
    	}
    	if(p_functName.equals("erpv4GetLcrg")) {
    		return(""+Erpv4Config.getDefaultLcrg(sh));
    	}
		return(super.brEvalFunction(p_functName, p_args));
    }	
}
