package com.uniinformation.zkbi.erpv4ext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultScheduleRpt;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerScheduleRpt extends ZkBiComposerReport {
	private static final int MAX_PERIOD_RANGE_DAY = 365;
	private static final SimpleDateFormat ddf1 = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat ddf2 = new SimpleDateFormat("yyMMdd");
	private static final int listboxHeightAdjust = 70;
   	private Datebox dbStart, dbEnd;

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
						((BiResultScheduleRpt)result).setQueryPeriod(dbStart.getValue(), dbEnd.getValue());
						try {
							addTempBiColumn(result, dbStart.getValue(), dbEnd.getValue());
							refresh(result, masterWin, -1, true, true);
						}
						catch (Exception e) {
							UniLog.log(e);
							ZkUtil.showErrMsg(e.getMessage());
						}
					}
				}
			});
			if (validationPeriod(dbStart.getValue(), dbEnd.getValue()) != null) {
				dbStart.setValue(DateUtil.monthStart(DateUtil.today()));
				dbEnd.setValue(DateUtil.monthEnd(DateUtil.today()));
			}
			((BiResultScheduleRpt)result).setQueryPeriod(dbStart.getValue(), dbEnd.getValue());
			addTempBiColumn(result, dbStart.getValue(), dbEnd.getValue());
	    } catch (Exception cex) {
	    	UniLog.log(cex);
	    	ZkUtil.showErrMsg(cex.getMessage());
	    }
	}

	@Override
	protected void setupListHeader(final BiResult result, final Component comp, int p_sortIdx, boolean p_sortDesc, Listhead listhead) {
		super.setupListHeader(result, comp, p_sortIdx, p_sortDesc, listhead);
		for (Component tmpComp : listbox.queryAll("Listheader"))
			((Listheader) tmpComp).setHflex("min");
	}
	
	private static String validationPeriod(Date startDate, Date endDate) {
		if (!DateUtil.isValid(startDate) || !DateUtil.isValid(endDate) || startDate.compareTo(endDate) > 0)
			return "Invalid Period";
		if ((endDate.getTime() - startDate.getTime()) / 86400000 > MAX_PERIOD_RANGE_DAY)
			return String.format("Period should be within %d days", MAX_PERIOD_RANGE_DAY + 1);
		return null;
	}
	
	private void addTempBiColumn(BiResult result, Date startDate, Date endDate) throws Exception {
    	Vector<BiColumn> listColumns = new Vector<BiColumn>(result.getListColumns());
    	for (BiColumn bc : listColumns) {
    		if (StringUtils.startsWith(bc.getLabel(), "em_xschedate")) {
    			result.hideViewColumn(bc);
    			result.getCurrentCollection().delCell(bc.getLabel());
    		}
    	}
		for (Date date = startDate; date.compareTo(endDate) <= 0; date = DateUtil.nextday(date))
			result.addTempColumn("em_xschedate" + ddf1.format(date), ddf2.format(date) + (DateUtil.toDayOfWeek(date) == 0 ? "(S)" : ""), "", "", "char", null,10);
		Listhead listhead = (Listhead) listbox.query("Listhead");
		setupListHeader(result, masterWin, defaultSortIdx, defaultSortDesc, listhead);
	}
}
