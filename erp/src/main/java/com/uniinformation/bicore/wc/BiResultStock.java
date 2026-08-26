package com.uniinformation.bicore.wc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.bicore.BiActionListener;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStock extends com.uniinformation.bicore.erpv4.BiResultStock{
	private BiActionListener<ColumnCell> actionOnCreate = null;
	public void setActionOnCreate(BiActionListener<ColumnCell> p_action) {
		actionOnCreate = p_action;
	}
	public BiActionListener<ColumnCell> getActionOnCreate() {
		return(actionOnCreate);
	}


	public BiResultStock(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getPickColumnCondition(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("stbd_name")) {
			String sttype = getCellString("st_mtype");
			return(" stbd_type = '" + sttype + "' ");
		} else return super.getPickColumnCondition(p_cc);
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
