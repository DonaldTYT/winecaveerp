package com.uniinformation.zkbi.erpv4ext;

import java.util.Date;

import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultLeaveSummaryRpt;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerLeaveSummaryRpt extends ZkBiComposerReport {
	private static final int MAX_YEAR_ITEM_COUNT = 50;
	private static final int listboxHeightAdjust = 70;
   	private Combobox cbYear;

	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	zkbiListTop.getParent().insertBefore(rpth, zkbiListTop);
		adjListboxHeight(listboxHeightAdjust);
    	
	    try {
	    	final ZkForm zkf1 = new ZkForm(rpth, "zkf/erpv4ext/LeaveSummaryRpt.zul");
	    	cbYear = (Combobox)zkf1.getComponent("cbYear");
	    	Date date = DateUtil.today();
	    	for (int i = 0; i < MAX_YEAR_ITEM_COUNT; i++) {
	    		final int y = DateUtil.getYear(date); 
	    		cbYear.appendChild(new Comboitem(String.valueOf(y)) {{setValue(y);}});
	    		date = DateUtil.prevyear(date);
	    	}
	    	cbYear.setSelectedIndex(0);

	    	final CellCollection rptCol = new CellCollection();
			final String joKey = "REPORT_" + result.getView() + "_" + getSessionHelper().getVcode();
			JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null, joKey);
			if (jo != null) CellCollectionToJsonInterface.JSONObjectToCellCollection(rptCol, jo);
			zkf1.mapCellCollection(rptCol, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("event:%s, event target:%s, id:%s, eventname:%s", event, event.getTarget(), event.getTarget().getId(), event.getName());
					if (event.getTarget().getId().equals("cbYear") && event.getName().equals(Events.ON_CHANGE) && event instanceof InputEvent) {
						JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(rptCol);
						FilingUtil.storeJson(getSessionHelper().getAgent(), null, joKey, null, null, jo);
						int year = NumberUtils.toInt(((InputEvent)event).getValue());
						((BiResultLeaveSummaryRpt)result).setQueryYear(year);
						refresh(result, masterWin, -1, true, true); 
					}
				}
			});
			int year = cbYear.getSelectedItem().getValue();
			UniLog.log1("buildBrowserWindow year:%d", year);
			((BiResultLeaveSummaryRpt)result).setQueryYear(year);
	    } catch (Exception cex) {
	    	UniLog.log(cex);
	    }
	}

	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		Integer year = cbYear.getSelectedItem().getValue();
		if (year == null)
			return ReturnMsg.defaultFail;
		Date startDate = DateUtil.dateTimeStrToDate(year + "/01/01");
		Date endDate = DateUtil.yearEnd(startDate);
		result.addCustomCondition(String.format("em_stdate <= '%s' and (em_enddate = '' or em_enddate >= '%s')", 
				DateUtil.dateToDateTimeStr(endDate, "yyyy/MM/dd"), DateUtil.dateToDateTimeStr(startDate, "yyyy/MM/dd")));
		return ReturnMsg.defaultOk;
	}
}
