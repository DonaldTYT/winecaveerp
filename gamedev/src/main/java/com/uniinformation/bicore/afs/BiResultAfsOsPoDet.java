package com.uniinformation.bicore.afs;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsOsPoDet extends BiResult {
	public BiResultAfsOsPoDet (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("BiResultAfsOsPoDet Used");
	}
	
	@Override 
	public HashSet<BiTable>addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash) {
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(BiSchema.hasAccessRight(getSessionHelper(),"hlv")) return(null);
		if(!BiSchema.hasAccessRight(getSessionHelper(),"machine") || !BiSchema.hasAccessRight(getSessionHelper(),"parts")) {
			if( BiSchema.hasAccessRight(getSessionHelper(),"parts") ) {
				p_where.andUniop("stm_ref1", "like" , "AFSP%");
			}
			if( BiSchema.hasAccessRight(getSessionHelper(),"machine") ) {
				p_where.andUniop("stm_ref1", "like" , "AFSM%");
			}
		}
		return(ht);
	}
}
