package com.uniinformation.bicore.hw;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultHwInvDet extends BiResultHwOrdDetBase {

	public BiResultHwInvDet(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection p_col) {
		try {
			getSelectUtil().executeUpdate("update quodet set ind_linked = 0 where ind_odrg = " + p_col.getCell("invqd_odrg").getInt(),null);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Error unlink quodet/invdet",true));
		}
		return(super.biBeforeDeleteCurrent(p_col));
	}
}
