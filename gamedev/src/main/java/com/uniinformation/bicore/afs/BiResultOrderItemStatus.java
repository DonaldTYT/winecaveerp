package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultOrderItemStatus extends BiResultErpv4 {

	public BiResultOrderItemStatus(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLinkedView(String p_colName,CellCollection p_col) {
		if(p_colName.equals("inv_invno")) {
			String invno = p_col.getCellString("inv_invno");
			if(invno.startsWith("AQM")) {
				return("afs.AfsQuoMc");
			} else {
				return("afs.AfsQuoParts");
			}
		}
		if(p_colName.equals("stm_ref1")) {
			String pono = p_col.getCellString("stm_ref1");
			if(pono.startsWith("AFSM")) {
				return("afs.AfsPoMc");
			} else {
				return("afs.AfsPoParts");
			}
		}
		return(super.getLinkedView(p_colName,p_col));
	}
}
