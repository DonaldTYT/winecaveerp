package com.uniinformation.bicore.erpv4ext;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPromotionTransDet extends BiResult {

	public BiResultPromotionTransDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	/*@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		if (getParent() != null && StringUtils.equals(getParent().getView().getName(), "erpv4ext.PromotionTransHdr")) {
			UniLog.log1("emg_stdate:%s", getParent().getCellDate("emg_stdate"));
			p_where.appendString(" and (emg_stdate > today or emg_enddate < today) ");
		}
		return super.addExtraWhereStr(p_where, p_hash);
	}*/
}
