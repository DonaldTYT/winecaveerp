package com.uniinformation.bicore.axa;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultEmailMessage extends BiResultErpv4 {

	public BiResultEmailMessage(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

//	public BiResultEmailMessage(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
//			SessionHelper p_sh) throws CellException {
//		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
//		// TODO Auto-generated constructor stub
//	}
	
	
	
//	@Override
//	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
//	{
//		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
//		if(getParent() == null) return(ht);
//		String phno = getParent().getCellString("axaclm_phno");
//		p_where.andUniop("emm_hashtag", "like", phno);
//		
//		return(ht);
//	}	
}
