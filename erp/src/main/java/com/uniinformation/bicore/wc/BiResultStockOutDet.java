package com.uniinformation.bicore.wc;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockOutDet extends BiResultErpv4 {

    public BiResultStockOutDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	public BiResultStockOutDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	public String getPickColumnCondition(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("or_org")) {
			BiCellCollection bc = p_cc.getCollection();
			int irg = bc.getCellInt("stmd_irg");
			return("pdls_loc = 'WH01' and pdls_irg = " + irg);
		}
		if(p_cc.getCellLabel().equals("stmd_bin")) {
			BiCellCollection bc = p_cc.getCollection();
			int irg = bc.getCellInt("stmd_irg");
			int org = bc.getCellInt("stmd_org");
			return("pdlbs_loc = 'WH01' and pdlbs_irg = " + irg + " and pdlbs_org = "+org);
		}
		if(p_cc.getCellLabel().equals("st_icode")) {
			BiCellCollection bc = p_cc.getCollection();
			return("stl_loc = 'WH01'");
		}
		return(null);
	}
}
