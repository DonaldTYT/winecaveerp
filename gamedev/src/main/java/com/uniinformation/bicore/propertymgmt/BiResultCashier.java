package com.uniinformation.bicore.propertymgmt;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCashier extends BiResultPropertyMgmt {

	public BiResultCashier(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		try {
			if (BiUtil.hasTableRec(su, "select col_c, count(*) from cashier where col_c != '' group by col_c having count(*) > 1"))
				return new ReturnMsg(false, "IC卡不能夠同時2人以上使用");
			return ReturnMsg.defaultOk;
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.toString());
		}
	}
}
