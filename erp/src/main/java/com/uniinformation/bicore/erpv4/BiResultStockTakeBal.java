package com.uniinformation.bicore.erpv4;

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

public class BiResultStockTakeBal extends BiResultErpv4 {

	public BiResultStockTakeBal(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(Erpv4Config.isMultiCompany(sh)) {
				if(p_lookupTable.getName().equals("locationcode")) {
					if(wcl == null ) wcl = new Wherecl();
					String cocode = Erpv4Config.getDefaultCoCode(sh);
					wcl.appendString(" and locationcode.loc_cocode = '"+cocode+"' ").stripAnd();
				}
			}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}

}
