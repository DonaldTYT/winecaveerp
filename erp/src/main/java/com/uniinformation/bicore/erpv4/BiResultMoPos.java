package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;


public class BiResultMoPos extends BiResultMO {

	public BiResultMoPos(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
//		stmdLinkName = "erpv4.MoDetPos";
		resetViewList();
	}

	public void resetViewList() {
		super.resetViewList();
		BiColumn clMrg  = getView().getColumnByLabel("stm_mrg");
		BiColumn clRef3 = getView().getColumnByLabel("stm_ref3");
		BiColumn clRef4 = getView().getColumnByLabel("stm_ref4");
		BiColumn clLocation = getView().getColumnByLabel("floc_desc");
		BiColumn clDoctor = getView().getColumnByLabel("cldoc_name");
		if(clMrg != null) {
			if(clRef4 != null) moveViewColumn(clRef4,clMrg);
			if(clRef3 != null) moveViewColumn(clRef3,clMrg);
			if(clDoctor != null) moveViewColumn(clDoctor,clMrg);
		}
	}
}
