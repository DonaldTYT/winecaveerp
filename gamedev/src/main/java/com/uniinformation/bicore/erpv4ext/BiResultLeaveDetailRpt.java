package com.uniinformation.bicore.erpv4ext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLeaveDetailRpt extends BiResult {
	private Map<String, Map<Date, Double>> leaveSumDetNDayCacheMap = new HashMap<String, Map<Date, Double>>(); //key: emid, value: <lv_sdate, sum(lvd_nday)>
	boolean afterLoadSerialMapFlag = false;

	public BiResultLeaveDetailRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		UniLog.log("createColumnCollection");
		return(new Erpv4LeaveDetailRptCellColletion(p_parent, this));
	}

	public Map<String, Map<Date, Double>> getLeaveSumDetNDayCacheMap() {
		return afterLoadSerialMapFlag ? leaveSumDetNDayCacheMap : null;
	}
	
	private void clearCacheMap() {
		UniLog.log("clearCacheMap");
		leaveSumDetNDayCacheMap.clear();
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		//clear cache map when perform query/refresh
		ReturnMsg rtn = super.afterLoadSerialMap();
		if (!rtn.getStatus()) return rtn;
		
		clearCacheMap();
		afterLoadSerialMapFlag = true;
		return(ReturnMsg.defaultOk);
	}
}
