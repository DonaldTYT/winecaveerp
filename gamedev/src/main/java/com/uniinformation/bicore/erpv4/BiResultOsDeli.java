package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultOsDeli extends BiResultErpv4 {

	public BiResultOsDeli(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
		BiColumn locCol = getColumnByLabel("inv_cocode");
		if(locCol != null && columnInSelectList(locCol)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and " + "quotation.inv_cocode = '"+cocode+"' ").stripAnd();
			p_where.andWherecl(wcl1);
		}
		}
		return(ht);
	}	
}
