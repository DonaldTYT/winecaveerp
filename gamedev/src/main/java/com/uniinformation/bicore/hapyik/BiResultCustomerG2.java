package com.uniinformation.bicore.hapyik;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.erpv4.JxZkBiErpv4;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCustomerG2 extends com.uniinformation.bicore.erpv4.BiResultCustomerG2 {

	Vector districtList;
	Vector areaList;
	Vector bsnatureList;
	Vector bstypeList;
	public BiResultCustomerG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	void getPresetList() {
		if(districtList == null) {
			districtList = JxZkBiErpv4.jxZkBiGetPresetItemList(this,getCell("vd_addr1"),null);
			areaList = JxZkBiErpv4.jxZkBiGetPresetItemList(this,getCell("vd_addr2"),null);
			bstypeList = JxZkBiErpv4.jxZkBiGetPresetItemList(this,getCell("vd_bstype"),null);
			bsnatureList = JxZkBiErpv4.jxZkBiGetPresetItemList(this,getCell("vd_bsnature"),null);
		}
	}
	@Override
	protected void createColumnCells(BiCellCollection p_col) {
		super.createColumnCells(p_col);
		getPresetList();
		p_col.getCell("vd_addr1").setItemList(districtList);
		p_col.getCell("vd_addr2").setItemList(areaList);
		p_col.getCell("vd_bstype").setItemList(bstypeList);
		p_col.getCell("vd_bsnature").setItemList(bsnatureList);
	}
	@Override
	public void unmapColumns() {
		super.unmapColumns();
		getPresetList();
		getCell("vd_addr2").setItemList(areaList);
		getCell("vd_bsnature").setItemList(bsnatureList);
	}

}
