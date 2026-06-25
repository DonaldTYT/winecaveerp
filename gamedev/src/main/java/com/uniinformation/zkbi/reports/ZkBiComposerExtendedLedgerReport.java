package com.uniinformation.zkbi.reports;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultLedgerG2;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerExtendedReport;

public class ZkBiComposerExtendedLedgerReport extends ZkBiComposerExtendedReport {
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		zkfName = "zkf.reports.LedgerReportG2";
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	@Override
	protected void onZkFEvent(BiResult result,Event ev) throws Exception {
		String p_id = ev.getTarget().getId();
		UniLog.log("got event " + ev.getName() + " on " + ev.getTarget().getId());
		boolean needRegen = false;
		boolean needRefresh = false;
		if(p_id.equals("sdate")) {
			needRefresh = true;
			needRegen = true;
		}
		if(p_id.equals("edate")) {
			needRefresh = true;
			needRegen = true;
		}
		if(p_id.equals("showNoDetail")) {
			result.setQueryIncludeNoDetail(result.getCellBoolean("showNoDetail"));
			needRefresh = true;
			needRegen = true;
		}
		if(p_id.equals("rgSummaryOrDetail")) {
			resetListHeader(result);
			String presetKey = conditionPresetListbox.getSelectedItem().getValue();
			visibleCols(presetKey, listbox,result);
			needRefresh = true;
			needRegen = true;
		}
		if(needRefresh) refresh(result,masterWin,-1,true,true); 
		if(needRegen) regenAggregateAndPivot(result);
	}
	
	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		Date d0 = result.getCellDate("sdate");
		Date d1 = result.getCellDate("edate");
		if(d0 == null) d0 = DateUtil.zeroDate;
		if(d1 == null) d1 = DateUtil.zeroDate;
		if(!d0.after(DateUtil.minDate) || !d1.after(DateUtil.minDate)) {
			return(new ReturnMsg(false,"Please Select Date Range"));
		}
		result.addCustomCondition(
					((BiLedgerReportInterface) result).getCumulatorColumn() + " between '" + DateUtil.toDateString(d0, "yyyy/mm/dd") + "' and '" + DateUtil.toDateString(d1, "yyyy/mm/dd") + "'");
		return(ReturnMsg.defaultOk);
	}
}
