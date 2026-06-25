package com.uniinformation.bicore.clinic;

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

public class BiResultClDoctor extends BiResultErpv4 {

	public BiResultClDoctor(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl1 = null;
		if(Erpv4Config.isMultiCompany(sh)) {
			if(!sh.hasAccessRight("#allloc")) {
			BiColumn locCol = getColumnByLabel("cldoc_cocode");
			if(locCol != null && columnInSelectList(locCol)) {
				String cocode = Erpv4Config.getDefaultCoCode(sh);
				if(wcl1 == null) wcl1 = new Wherecl();
				wcl1.appendString(" and cldoc_cocode = '"+cocode+"' ").stripAnd();
			}
			}
		}
		if(wcl1 != null) p_where.andWherecl(wcl1);
		return(ht);
	}

}
