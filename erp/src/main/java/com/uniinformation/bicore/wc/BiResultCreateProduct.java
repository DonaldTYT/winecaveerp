package com.uniinformation.bicore.wc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.bicore.BiActionListener;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCreateProduct extends BiResultErpv4 {
	private BiActionListener<ColumnCell> actionOnCreate = null;
	public void setActionOnCreate(BiActionListener<ColumnCell> p_action) {
		actionOnCreate = p_action;
	}
	public BiActionListener<ColumnCell> getActionOnCreate() {
		return(actionOnCreate);
	}

	public BiResultCreateProduct(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Pair<String,BiActionListener<ColumnCell>>> getPickColumnExtraButton(ColumnCell p_cc) {
		List<Pair<String,BiActionListener<ColumnCell>>> butList = super.getPickColumnExtraButton(p_cc);
		if(p_cc.getCellLabel().equals("stbd_name") && actionOnCreate != null) {
			if(butList == null) {
				butList = new ArrayList<Pair<String,BiActionListener<ColumnCell>>> ();
			}
			butList.add(Pair.of( "Create",actionOnCreate));
		}
		return(butList);
	}


}
