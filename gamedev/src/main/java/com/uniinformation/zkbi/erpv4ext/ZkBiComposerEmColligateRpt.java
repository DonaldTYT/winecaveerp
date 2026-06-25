package com.uniinformation.zkbi.erpv4ext;

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultEmColligateRpt;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerEmColligateRpt extends ZkBiComposerReport {
	private static final int listboxHeightAdjust = 70;
   	private Datebox dbStart, dbEnd;

	public ZkBiComposerEmColligateRpt() {
		generateReportItemScriptMap = new HashMap<String, String>();
		for (String key : new String[] {"em_xworkmins", "em_xsotmins", "em_xlatemins", "em_xlvearlymins", "em_xotmins", "em_xnoworkmins"}) {
			generateReportItemScriptMap.put(key, "setHHmmDisplayValue(this, false)");
			generateReportItemScriptMap.put("Sum(" + key + ")", "setHHmmDisplayValue(this, false)");
			generateReportItemScriptMap.put("Min(" + key + ")", "setHHmmDisplayValue(this, false)");
			generateReportItemScriptMap.put("Max(" + key + ")", "setHHmmDisplayValue(this, false)");
			generateReportItemScriptMap.put("Avg(" + key + ")", "setHHmmDisplayValue(this, false)");
		}
	}

	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	zkbiListTop.getParent().insertBefore(rpth, zkbiListTop);
		adjListboxHeight(listboxHeightAdjust);
    	
	    try {
	    	final ZkForm zkf1 = new ZkForm(rpth, "zkf/erpv4ext/ScheduleRpt.zul");
	    	dbStart = (Datebox)zkf1.getComponent("dbStart");
	    	dbEnd = (Datebox)zkf1.getComponent("dbEnd");
	    	dbStart.setValue(DateUtil.monthStart(DateUtil.today()));
	    	dbEnd.setValue(DateUtil.monthEnd(DateUtil.today()));

	    	final CellCollection rptCol = new CellCollection();
			final String joKey = "REPORT_" + result.getView() + "_" + getSessionHelper().getVcode();
			JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null, joKey);
			if (jo != null) CellCollectionToJsonInterface.JSONObjectToCellCollection(rptCol, jo);
			zkf1.mapCellCollection(rptCol, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("event:%s, event target:%s, id:%s, eventname:%s", event, event.getTarget(), event.getTarget().getId(), event.getName());
					if (StringUtils.equalsAny(event.getTarget().getId(), "dbStart", "dbEnd") && event.getName().equals(Events.ON_CHANGE) && event instanceof InputEvent) {
						String errMsg = validationPeriod(dbStart.getValue(), dbEnd.getValue());
						if (errMsg != null) {
							LeaveApplication.showErrorNotification(errMsg, event.getTarget());
							return;
						}
						JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(rptCol);
						FilingUtil.storeJson(getSessionHelper().getAgent(), null, joKey, null, null, jo);
						((BiResultEmColligateRpt)result).setQueryPeriod(dbStart.getValue(), dbEnd.getValue());
						refresh(result, masterWin, -1, true, true);
					}
				}
			});
			if (validationPeriod(dbStart.getValue(), dbEnd.getValue()) != null) {
				dbStart.setValue(DateUtil.monthStart(DateUtil.today()));
				dbEnd.setValue(DateUtil.monthEnd(DateUtil.today()));
			}
			((BiResultEmColligateRpt)result).setQueryPeriod(dbStart.getValue(), dbEnd.getValue());
	    } catch (Exception cex) {
	    	UniLog.log(cex);
	    	ZkUtil.showErrMsg(cex.getMessage());
	    }
	}

	private static String validationPeriod(Date startDate, Date endDate) {
		if (!DateUtil.isValid(startDate) || !DateUtil.isValid(endDate) || startDate.compareTo(endDate) > 0)
			return "Invalid Period";
		return null;
	}
}
