package com.uniinformation.bicore.afs;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

//import org.eclipse.birt.report.model.api.util.StringUtil;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsStockG2 extends BiResultAfsStock {

	public BiResultAfsStockG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	public void resetViewList() {
		super.resetViewList();
		moveViewColumn(getView().getColumnByLabel("mt_tpname"),getView().getColumnByLabel("st_icode"));
		moveViewColumn(getView().getColumnByLabel("stbd_name"),getView().getColumnByLabel("mt_tpname"));
		moveViewColumn(getView().getColumnByLabel("st_modelno"),getView().getColumnByLabel("stbd_name"));
	}
	@Override
	protected void updateIcode(CellCollection col)  throws CellException {
		if(StringUtils.isBlank(col.getCellString("st_icode"))) {
			super.updateIcode(col);
		}
	}

}
