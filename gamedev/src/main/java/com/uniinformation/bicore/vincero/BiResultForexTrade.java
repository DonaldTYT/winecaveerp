package com.uniinformation.bicore.vincero;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultForexTrade extends BiResult {

	public BiResultForexTrade(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		try {
			addTrRecord(null,0);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(ReturnMsg.defaultOk);
	}
}
