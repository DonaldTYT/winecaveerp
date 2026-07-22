package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPrdsrvmaster extends BiResultErpv4 {

	public BiResultPrdsrvmaster(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			if(getCell("pds_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and pds_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
			}
			return(ht);
		} else return(ht);
	}
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(Erpv4Config.isMultiCompany(sh)) {
		if(p_lookupTable.getName().equals("dca")) {
			if(wcl == null ) wcl = new Wherecl();
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			wcl.appendString(" and dca.ca_cocode = '"+cocode+"' ").stripAnd();
		}
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	
}
