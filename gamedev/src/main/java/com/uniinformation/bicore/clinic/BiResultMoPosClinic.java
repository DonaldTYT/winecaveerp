package com.uniinformation.bicore.clinic;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultMoPos;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMoPosClinic extends BiResultMoPos{

	public BiResultMoPosClinic(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(Erpv4Config.isMultiCompany(sh)) {
				if(p_lookupTable.getName().equals("cldoctor")) {
					String cocode = Erpv4Config.getDefaultCoCode(sh);
					if(wcl == null ) wcl = new Wherecl();
					wcl.appendString(" and cldoctor.cldoc_cocode = '"+cocode + "' ").stripAnd();
				}
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
}
