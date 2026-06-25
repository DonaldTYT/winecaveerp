package com.uniinformation.zkbi.erpv4ext;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultAttendanceRecord;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jxapp.erpv4ext.AttendanceRecord;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerAttendanceRecord extends ZkBiComposerBase {

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("doAfterCompose called");
		masterWin.addEventListener("onHideButtonAttRec", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				Button btn = (Button)masterWin.getFellowIfAny("btAdd");
				if (btn != null)
					btn.setVisible(false);
				btn = (Button)masterWin.getFellowIfAny("btDelete");
				if (btn != null)
					btn.setVisible(false);
			}
		});
		Events.echoEvent("onHideButtonAttRec", masterWin, null);
	}

	@Override
	protected void setupExtraButton(final BiResult result) {
        final Button btReloadAttData = new ZkBiButton();
        btReloadAttData.setLabel("Reload attendance data");
        btReloadAttData.setId("btReloadAttData");
        btReloadAttData.setAttribute("tlkey", "bt_reload_attdata");
        btReloadAttData.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("btReloadAttData event:%s", event);
				final Datebox dbStart = new Datebox();
				final Datebox dbEnd = new Datebox();
				showPeriodDialog(result, dbStart, dbEnd, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
						UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
						if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Ok"))) {
							final Date startDate = dbStart.getValue();
							final Date endDate = dbEnd.getValue();
							if (!validationPeriod(startDate, endDate))
								return;
							int ok = 0, fail = 0, skip = 0;
							Map<String, CellCollection> m = getBatchEmMap(result);
							if (m.isEmpty()) {
								ZkUtil.errMsg("Please choose item");
								return;
							}
							for (CellCollection cc : m.values()) {
								BiResult br = null;
								try {
									AttendanceRecord.Shift shift = new AttendanceRecord.Shift(cc, result.getSelectUtil(), startDate, endDate);
									shift.chkAndAddShiftmask();

									br = sessionHelper.newBiResult("erpv4ext.Attendance");
									br.beginWork();
									AttendanceRecord.AttendanceRecalc attl = new AttendanceRecord.AttendanceRecalc(br.getSelectUtil(), cc.getCellString("em_eid"), cc.getCellString("em_yflag"), startDate, endDate);
									if (attl.start())
										attl.finish();
									else
										skip++;
									br.commitWork();
									ok++;
								}
								catch (Exception e) {
									UniLog.log(e);
									if (br != null)
										br.rollbackWork();
									fail++;
								}
							}
							String msg = (ok > 0 ? "Ok: " + ok + ", " : "") + (fail > 0 ? "Fail: " + fail + ", " : "") + (skip > 0 ? "Skip: " + skip + ", " : "");
							ZkUtil.msg(!msg.isEmpty() ? msg.substring(0, msg.length() - 2) : "Ok: 0");
							Button btCancel = (Button) btReloadAttData.getParent().query("#" + btReloadAttData.getId() + "BatchCancel");
							Events.echoEvent(Events.ON_CLICK, btCancel, null);
						}
					}
				});
			}
        });
        final Button btRearrangeShift = new ZkBiButton();
        btRearrangeShift.setLabel("Rearrange shift");
        btRearrangeShift.setId("btRearrangeShift");
        btRearrangeShift.setAttribute("tlkey", "bt_rearrange_shift");
        btRearrangeShift.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("btRearrangeShift event:%s", event);
				final Datebox dbStart = new Datebox();
				final Datebox dbEnd = new Datebox();
				showPeriodDialog(result, dbStart, dbEnd, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
						UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
						if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Ok"))) {
							final Date startDate = dbStart.getValue();
							final Date endDate = dbEnd.getValue();
							if (!validationPeriod(startDate, endDate))
								return;
							final Map<String, CellCollection> m = getBatchEmMap(result);
							if (m.isEmpty()) {
								ZkUtil.errMsg("Please choose item");
								return;
							}
							ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
							new ZkBiMsgbox(sessionHelper).setContent("Reset all attendance record?").setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
								@Override
								public void onZkBiEvent(Event event) throws Exception {
									ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
									UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
									if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Ok"))) {
										int ok = 0, fail = 0;
										for (CellCollection cc : m.values()) {
											try {
												String eid = cc.getString("em_eid");
												result.getSelectUtil().executeUpdate("delete from attendance where at_eid = ? and at_date between ? and ?", 
														new Wherecl().appendArgument(eid).appendArgument(startDate).appendArgument(endDate));
												ok++;
											}
											catch (Exception e) {
												UniLog.log(e);
												fail++;
											}
										}
										String msg = (ok > 0 ? "Ok: " + ok + ", " : "") + (fail > 0 ? "Fail: " + fail + ", " : "");
										ZkUtil.msg(!msg.isEmpty() ? msg.substring(0, msg.length() - 2) : "Ok: 0");
										Button btCancel = (Button) btRearrangeShift.getParent().query("#" + btRearrangeShift.getId() + "BatchCancel");
										Events.echoEvent(Events.ON_CLICK, btCancel, null);
									}
								}
							}).build().doModal();
						}
					}
				});
			}
        });
        abHelper.addButton(btReloadAttData, "fa-user");
        abHelper.addButton(btRearrangeShift, "fa-user");
		ZkUtil.setupBatchModeButton(btReloadAttData, batchModeToggleButton);
		ZkUtil.setupBatchModeButton(btRearrangeShift, batchModeToggleButton);
	}

	@Override
	public void biBaseClose(BiResult p_br) {
		super.biBaseClose(p_br);
		((BiResultAttendanceRecord)p_br).setQueryPeriod(null, null);
	}
	
	private void showPeriodDialog(BiResult br, final Datebox dbStart, final Datebox dbEnd, ZkBiEventListener<Event> listener) throws Exception {
		dbStart.setValue(DateUtil.monthStart(DateUtil.today()));
		dbEnd.setValue(DateUtil.monthEnd(DateUtil.today()));
		dbStart.setFormat("yyyy/MM/dd");
		dbEnd.setFormat("yyyy/MM/dd");
		ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
		Hlayout hl = new Hlayout() {{
			appendChild(new Label("Period") {{
				setAttribute("tlkey", "lb_period");
			}});
			appendChild(dbStart);
			appendChild(new Label("to") {{
				setAttribute("tlkey", "lb_to");
			}});
			appendChild(dbEnd);
		}};
		new ZkBiMsgbox(sessionHelper).setContent(hl).setButtons(btns).setEventListener(listener).build().doModal();
		ZkUtil.translateAllComp(sessionHelper, hl, br.getView().getName(), br);
	}
	
	private boolean validationPeriod(Date startDate, Date endDate) {
		if (!DateUtil.isValid(startDate)) {
			ZkUtil.errMsg("Invalid start date");
			return false;
		}
		if (!DateUtil.isValid(endDate)) {
			ZkUtil.errMsg("Invalid start date");
			return false;
		}
		if (startDate.compareTo(endDate) > 0) {
			ZkUtil.errMsg("Start date cannot be more than end date");
			return false;
		}
		if ((endDate.getTime() - startDate.getTime()) / 86400000 > 99) {
			ZkUtil.showErrMsg("Period should be within 100 days");
			return false;
		}
		return true;
	}
	
	private Map<String, CellCollection> getBatchEmMap(BiResult br) {
		final Map<String, CellCollection> emMap = new LinkedHashMap<String, CellCollection>();
		Set selection = listModelList.getSelection();
       	for (Iterator it = selection.iterator(); it.hasNext();) {
       		Object o = it.next();
         	Object ts = o;
          	if (ts instanceof TrStatFilter)
            	ts = ((TrStatFilter)ts).getTrStatIdx();
       		CellCollection cc = br.getRowCollectionO(ts);
       		String eid = cc.getString("em_eid");
       		emMap.put(eid, cc);
       	}
       	return emMap;
	}
}
