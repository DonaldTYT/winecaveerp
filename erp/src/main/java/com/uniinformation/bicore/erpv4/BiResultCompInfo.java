package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCompInfo extends BiResultErpv4 {

	public BiResultCompInfo(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
//	@Override
//	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
//		Wherecl wcl = p_wherecl;
//		if(sh.useJxFormG2()) {
//		if(p_col == null && p_lookupTable.getName().equals("st_origin")) {
//				if(wcl == null ) wcl = new Wherecl();
//				wcl.appendString(" and st_origin.storg_enabled = 'Y'").stripAnd();
//		}
//		}
//		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
//	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(!sh.hasAccessRight("#allcomp")) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and co_cocode = '"+BiConfig.getDefaultCoCode(sh)+"' ").stripAnd();
				p_where.andWherecl(wcl1);
		}
		return(ht);
	}
}
