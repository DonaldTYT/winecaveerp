package com.uniinformation.bicore.erpv4ext;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultEmployeeGrade extends BiResult {
	
	public BiResultEmployeeGrade(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			if (col.getCellInt("gdmt_shtrg") == 0) {
				Value v = getView().getSchema().getUniqueRg(this, "", 12001, "shiftarrange", "shtar_rg", "");
				getCell("gdmt_shtrg").set(v.toInt());
				getCell("shtar_rg").set(v.toInt());
			}
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtn);
	}
}