package com.uniinformation.bicore.erpv4ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication.LeaveCal;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLeaveSummaryRpt extends BiResult {
	private Map<String, LeaveCal> leaveCalCacheMap = new HashMap<String, LeaveCal>(); //key: emid
	private boolean afterLoadSerialMapFlag = false;
	private int queryYear = DateUtil.getYear(DateUtil.today());
	
	public BiResultLeaveSummaryRpt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
		try {
			Vector<CellCollection> vc = su.getQueryResultToCellVector("select lvrs_name from leavereason where lvrs_name <> 'AL' order by lvrs_name", null);
			for (CellCollection cc : vc) {
				String name = cc.getString("lvrs_name");
				addTempColumn("emx_days_" + name, name, "", "0.0", "float", null,0);
			}
		} catch (Exception e) {
			UniLog.log(e);
		}
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		UniLog.log("createColumnCollection");
		return new Erpv4LeaveSummaryRptCellColletion(p_parent, this);
	}

	public Map<String, LeaveCal> getLeaveCalCacheMap() {
		return afterLoadSerialMapFlag ? leaveCalCacheMap : null;
	}
	
	public void setQueryYear(int year) {
		queryYear = year;
	}

	public int getQueryYear() {
		return queryYear;
	}
	
	private void clearCacheMap() {
		UniLog.log("clearCacheMap");
		leaveCalCacheMap.clear();
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
