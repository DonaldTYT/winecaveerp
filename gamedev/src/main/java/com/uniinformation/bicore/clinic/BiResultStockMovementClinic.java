package com.uniinformation.bicore.clinic;

import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultStockMovement;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockMovementClinic extends BiResultStockMovement {

	public BiResultStockMovementClinic(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// not use any more, should not be called
		// TODO Auto-generated constructor stub
	}

//	@Override
//    public Wherecl getFieldUniqueListAppendWhere(BiColumn p_bc, Wherecl p_where) {
//		Wherecl wcl = p_where;
//		if(p_bc.getLabel().equals("cldoc_name")) {
//			if(wcl == null) wcl = new Wherecl();
//			wcl.andUniop("cldoctor.cldoc_cocode", "=", Erpv4Config.getDefaultCoCode(sh));
//		}
//    	return(super.getFieldUniqueListAppendWhere(p_bc, wcl));
//    }
}
