package com.uniinformation.zkbi.erpv4ext;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultAttendanceRpt;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerReport;

public class ZkBiComposerAttendanceRpt extends ZkBiComposerReport {

	public ZkBiComposerAttendanceRpt() {
		generateReportItemScriptMap = new HashMap<String, String>();
		for (String key : new String[] {"at_xsumot", "at_xdreallate", "at_xleaveearly", "at_xdnowork", "at_xdwktime"}) {
			generateReportItemScriptMap.put(key, "setHHmmDisplayValue(this, true)");
			generateReportItemScriptMap.put("Sum(" + key + ")", "setHHmmDisplayValue(this, false)");
			generateReportItemScriptMap.put("Min(" + key + ")", "setHHmmDisplayValue(this, false)");
			generateReportItemScriptMap.put("Max(" + key + ")", "setHHmmDisplayValue(this, false)");
			generateReportItemScriptMap.put("Avg(" + key + ")", "setHHmmDisplayValue(this, false)");
		}
	}

	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
		final BiResultAttendanceRpt br = (BiResultAttendanceRpt)result;
		masterWin.addEventListener("onShowInOutAttRpt", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				Listhead listhead = (Listhead) listbox.query("#browser_listhead");
				UniLog.log1("showAttendDetInOutCount:%d, listhead:%s", br.getShowAttendDetInOutCount(), listhead);
				Map<String, Boolean> m = new HashMap<String, Boolean>();
				for (int i = BiResultAttendanceRpt.DEFAULT_ATTENDDET_INOUT_COLUMN_COUNT; i < BiResultAttendanceRpt.MAX_ATTENDDET_INOUT_COLUMN_COUNT; i++) {
					boolean b = i >= br.getShowAttendDetInOutCount();
					m.put("at_xattintype" + i, b);
					m.put("at_xattin" + i, b);
					m.put("at_xattouttype" + i, b);
					m.put("at_xattout" + i, b);
				}
				if (listhead != null) {
					for (int i = 1; i <= listhead.getChildren().size(); i++) {
						Listheader listheader = (Listheader) listhead.query("#browser_listheader_" + i);
						if (listheader != null) {
							BiColumn biColumn = (BiColumn) listheader.getAttribute("ma_bicolumn");
							if (biColumn != null && m.containsKey(biColumn.getLabel())) {
								boolean b = m.get(biColumn.getLabel());
								listheader.setAttribute("isHideForTempBiColumn", b);
							}
						}
					}
					String preset = conditionPresetListbox.getSelectedItem().getValue();
   		   			visibleCols(preset, listbox, br);
   		   			Events.echoEvent("onResizeAttRpt", masterWin, null);
				}
			}
		});
		masterWin.addEventListener("onResizeAttRpt", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				Clients.resize(listbox);
			}
		});
		Events.echoEvent("onShowInOutAttRpt", masterWin, null);
	}

	@Override
	protected void setupListHeader(final BiResult result, final Component comp, int p_sortIdx, boolean p_sortDesc, Listhead listhead) {
		super.setupListHeader(result, comp, p_sortIdx, p_sortDesc, listhead);
		for (Component tmpComp : listbox.queryAll("Listheader"))
			((Listheader) tmpComp).setHflex("min");
	}

	@Override
	public void refresh(final BiResult result, final Component p_comp, MultiSortMap sortMap, boolean p_doSearch) {
		super.refresh(result, p_comp, sortMap, p_doSearch);
		Events.echoEvent("onShowInOutAttRpt", masterWin, null);
	}
}
