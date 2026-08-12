package com.uniinformation.bicore.aw;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultWorkOrderRpt extends BiResultWorkOrder {
	
	public BiResultWorkOrderRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new ArtwayCellCollection(p_parent, this));
	}
}
