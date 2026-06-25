package com.uniinformation.zkbi.erpv4ext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultLeaveChartRpt;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerLeaveChartRpt extends ZkBiComposerReport {
	private static final int MAX_YEAR_ITEM_COUNT = 50;
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat ddf1 = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat ddf2 = new SimpleDateFormat("dd");
	private static final int listboxHeightAdjust = 70;
   	private Combobox cbYear, cbMonth;

	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	zkbiListTop.getParent().insertBefore(rpth, zkbiListTop);
		adjListboxHeight(listboxHeightAdjust);
    	
	    try {
	    	final ZkForm zkf1 = new ZkForm(rpth, "zkf/erpv4ext/LeaveChartRpt.zul");
	    	cbYear = (Combobox)zkf1.getComponent("cbYear");
	    	cbMonth = (Combobox)zkf1.getComponent("cbMonth");
	    	Date today = DateUtil.today();
	    	Date date = today;
	    	for (int i = 0; i < MAX_YEAR_ITEM_COUNT; i++) {
	    		final int y = DateUtil.getYear(date); 
	    		cbYear.appendChild(new Comboitem(String.valueOf(y)) {{setValue(y);}});
	    		date = DateUtil.prevyear(date);
	    	}
	    	for (int i = 0; i < 12; i++) {
	    		final int m = i + 1;
	    		cbMonth.appendChild(new Comboitem(String.valueOf(m)) {{setValue(m);}});
	    	}
	    	cbYear.setSelectedIndex(0);
	    	cbMonth.setSelectedIndex(DateUtil.getMonth(today) - 1);

	    	final CellCollection rptCol = new CellCollection();
			final String joKey = "REPORT_" + result.getView() + "_" + getSessionHelper().getVcode();
			JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null, joKey);
			if (jo != null) CellCollectionToJsonInterface.JSONObjectToCellCollection(rptCol, jo);
			zkf1.mapCellCollection(rptCol, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("event:%s, event target:%s, id:%s, eventname:%s", event, event.getTarget(), event.getTarget().getId(), event.getName());
					if (StringUtils.equalsAny(event.getTarget().getId(), "cbYear", "cbMonth") && event.getName().equals(Events.ON_CHANGE) && event instanceof InputEvent) {
						JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(rptCol);
						FilingUtil.storeJson(getSessionHelper().getAgent(), null, joKey, null, null, jo);
						Date stdate = getStartDate();
						Date enddate = DateUtil.monthEnd(stdate);
						((BiResultLeaveChartRpt)result).setQueryPeriod(stdate, enddate);
						try {
							addTempBiColumn(result, stdate, enddate);
							refresh(result, masterWin, -1, true, true);
						}
						catch (Exception e) {
							UniLog.log(e);
							ZkUtil.showErrMsg(e.getMessage());
						}
					}
				}
			});
			Date stdate = getStartDate();
			Date enddate = DateUtil.monthEnd(stdate);
			((BiResultLeaveChartRpt)result).setQueryPeriod(stdate, enddate);
			addTempBiColumn(result, stdate, enddate);
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
	
	private Date getStartDate() throws ParseException {
		int year = NumberUtils.toInt(cbYear.getValue());
		int month = NumberUtils.toInt(cbMonth.getValue());
		return ddf.parse(String.format("%04d/%02d/01", year, month));
	}
	
	private void addTempBiColumn(BiResult result, Date startDate, Date endDate) throws Exception {
    	Vector<BiColumn> listColumns = new Vector<BiColumn>(result.getListColumns());
    	for (BiColumn bc : listColumns) {
    		if (StringUtils.startsWith(bc.getLabel(), "em_xdate") || StringUtils.equals(bc.getLabel(), "em_xtotal")) {
    			result.hideViewColumn(bc);
    			result.getCurrentCollection().delCell(bc.getLabel());
    		}
    	}
		for (Date date = startDate; date.compareTo(endDate) <= 0; date = DateUtil.nextday(date))
			result.addTempColumn("em_xdate" + ddf1.format(date), ddf2.format(date) + "(" + AttendanceRecord.getShortDayOfWeek(sessionHelper, date) + ")", "", "", "char", null,10);
		result.addTempColumn("em_xtotal", "Total", "", "#0.0", "float", null,10);
		Listhead listhead = (Listhead) listbox.query("Listhead");
		setupListHeader(result, masterWin, defaultSortIdx, defaultSortDesc, listhead);
	}
}
