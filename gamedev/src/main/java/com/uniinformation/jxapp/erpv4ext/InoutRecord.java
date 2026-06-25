package com.uniinformation.jxapp.erpv4ext;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4ext.BiResultInoutRecord;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication.PickByListForm;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class InoutRecord extends JxZkBiBase {
	private static Map<String, String> inoutFlagMap = new LinkedHashMap<String, String>() {{
		put("IN", "上班");
		put("OU", "下班");
		/*put("IR", "上班 <作廢>");
		put("OR", "下班 <作廢>");
		put("ML", "用餐");
		put("MR", "用餐 <作廢>");
		put("XR", "不明 <作廢>");*/
	}};

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		new JxFieldAction("btChangePeriod") {
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty()) {
					ZkUtil.showErrMsg("Cannot change period in editing mode");
					return;
				}
				try {
					final Datebox dbStart = new Datebox();
					final Datebox dbEnd = new Datebox();
					final Label lbStart = new Label("Period");
					final Label lbEnd = new Label("to");
					dbStart.setFormat("yyyy/MM/dd");
					dbEnd.setFormat("yyyy/MM/dd");
					dbStart.setValue(getBr().getCellDate("em_xperiodstdate"));
					dbEnd.setValue(getBr().getCellDate("em_xperiodenddate"));
					lbStart.setAttribute("tlkey", "lb_period");
					lbEnd.setAttribute("tlkey", "lb_to");
					Hbox hb = new Hbox() {{
						appendChild(lbStart);
						appendChild(dbStart);
						appendChild(lbEnd);
						appendChild(dbEnd);
					}};
					ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
					new ZkBiMsgbox(sessionHelper).setContent(hb).setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
							UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
							if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Ok"))) {
								Date startDate = dbStart.getValue();
								Date endDate = dbEnd.getValue();
								if (!DateUtil.isValid(startDate)) {
									ZkUtil.showErrMsg("Invalid Start Date");
									return;
								}
								if (!DateUtil.isValid(endDate)) {
									ZkUtil.showErrMsg("Invalid End Date");
									return;
								}
								if (startDate.compareTo(endDate) > 0) {
									ZkUtil.showErrMsg("Start Date must be less than or equals with End Date");
									return;
								}
								/*if ((endDate.getTime() - startDate.getTime()) / 86400000 > 99) {
									ZkUtil.showErrMsg("Period should be within 100 days");
									return;
								}*/
								((BiResultInoutRecord)getBr()).setQueryPeriod(startDate, endDate);
								fireRefreshPage();
							}
						}
					}).build().doModal();
					ZkUtil.translateAllComp(sessionHelper, hb, getBr().getView().getName(), getBr());
				} catch (Exception e) {
					UniLog.log(e);
				}
			}
		};
	}

	@Override
	protected void formDirtyChanged() {
		jxSetEnable("btChangePeriod", !isDirty());
	}

	@Override
	public void bindCellCollection(final BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		((Listbox)jxAdd("list_erpv4ext_AttendDet").getNativeObject()).renderAll();;
		new ZkBiAbstractLongOp(curComp, "Loading", 100) {
			@Override
			public ReturnMsg longOp() {
				try {
					BiResultInoutRecord br = (BiResultInoutRecord)p_br;
					Date periodStartDate = br.getQueryPeriodStartDate();
					Date periodEndDate = br.getQueryPeriodEndDate();
					br.getCell("em_xperiodstdate").set(periodStartDate);
					br.getCell("em_xperiodenddate").set(periodEndDate);

					Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.AttendDet");
					for (BiCellCollection cc : recs) {
						cc.getCell("atd_xflagdesc").set(inoutFlagMap.get(cc.getString("atd_flag")));
						setupOnOffDetailCell(cc);
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					showErrorMessageAndExit("Error: " + ex.toString());
				}
				return null;
			}
		};
	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd, BiResult sr, CellCollection cl, int p_insIdx) {
		ReturnMsg rtn = super.beforeAddLink(fd, sr, cl, p_insIdx);
		UniLog.log1("beforeAddLink p_insIdx:%d, fdName:%s", p_insIdx, fd != null ? fd.getName() : "");
		if (rtn != null && !rtn.getStatus()) return rtn;
		try {
			if (fd != null) {
				if (StringUtils.equalsAny(fd.getName(), "list_erpv4ext_AttendDet", "btadd_list_erpv4ext_AttendDet")) {
					cl.getCell("atd_atype").set("00");
					cl.getCell("atd_adate").set(DateUtil.today());
					setupOnOffDetailCell(cl);
				}
			}
		} catch (Exception ex) {
			rtn = new ReturnMsg(false, -1, ex.getMessage());
		}
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			Vector<BiCellCollection> recs = br.getSubLinkResult("erpv4ext.AttendDet");
			for (BiCellCollection cc : recs) {
				String atype = cc.getString("atd_atype");
				String flag = cc.getString("atd_flag");
				Date date = cc.getDate("atd_date");
				Date time = cc.getDate("atd_time");
				if (StringUtils.equals(atype, "00")) {
					if (StringUtils.isBlank(flag))
						return new ReturnMsg(false, "Type cannot be empty", true);
					if (DateUtil.isDateNull(date))
						return new ReturnMsg(false, "Shift Date cannot be empty", true);
					if (DateUtil.isDateNull(time))
						return new ReturnMsg(false, "Shift Time cannot be empty", true);
				}
				if (StringUtils.isNotBlank(flag) && !inoutFlagMap.containsKey(flag))
					return new ReturnMsg(false, String.format("Type '%s' not found", flag), true);
				if (!DateUtil.isDateNull(time) && time.compareTo(LeaveApplication.END_TIME_IN_DAY) >= 0)
					return new ReturnMsg(false, "Invalid Time", true);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}


	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new AttendDetGetItemProperty(p_br.getSubLink("erpv4ext.AttendDet"))
		);	
	}

	/* Customized GIPI for AttendDet */
	private class AttendDetGetItemProperty extends BiGetItemProperty {
		private PickByListForm pickFlagForm;
		public AttendDetGetItemProperty(BiResult p_br) {
			super(p_br);
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_PULLDOWN_OPENED) {
				if (StringUtils.equals(bcc.getCellLabel(), "atd_flag")) {
					try {
						ZkJxPickInput pickComp = (ZkJxPickInput)LeaveApplication.getCellComponent(bcc);
						if (pickFlagForm == null) {
							List<String[]> list = new ArrayList<>();
							for (Map.Entry<String, String> entry : inoutFlagMap.entrySet())
								list.add(new String[] { entry.getKey(), entry.getValue() });
							pickFlagForm = new PickByListForm(sessionHelper, 
								new String[] {"50px", "120px"}, 
								list,
								(String[] rec, Object userData) -> {
									try {
										((BiCellCollection)userData).getCell("atd_flag").set((String)rec[0]);
										((BiCellCollection)userData).getCell("atd_xflagdesc").set((String)rec[1]);
										cl.getCell("atd_adate").set(DateUtil.today());
										setDirtyFlag(true);
									} catch (CellException e) {
										UniLog.log(e);
									}
								}
							);
						}
						pickFlagForm.bindComponent(pickComp, cl);
					}
					catch (Exception e) {
						UniLog.log(e);
					}
				}
			}
			if (p_ctype == GIPI_VALUE_CHANGED) {
				try {
					if (StringUtils.equals(bcc.getCellLabel(), "atd_flag")) {
						String flag = bcc.getString();
						if (inoutFlagMap.containsKey(flag))
							cl.getCell("atd_xflagdesc").set(inoutFlagMap.get(flag));
						else {
							cl.getCell("atd_xflagdesc").set("");
							LeaveApplication.showErrorNotification(String.format("Type '%s' not found", flag), bcc);
						}
						cl.getCell("atd_adate").set(DateUtil.today());
					} else if (StringUtils.equalsAny(bcc.getCellLabel(), "atd_date", "atd_time")) {
						String atype = cl.getString("atd_atype");
						if (StringUtils.equals(atype, "00")) {
							Date date = cl.getDate("atd_date");
							Date time = cl.getDate("atd_time");
							if (bcc.getCellLabel().equals("atd_time")) {
								if (bcc.getDate().compareTo(LeaveApplication.END_TIME_IN_DAY) >= 0)
									LeaveApplication.showErrorNotification("Invalid time", bcc);
							}
							cl.getCell("atd_atime").set(AttendanceRecord.unionDateTime(date, time));
						}
						cl.getCell("atd_adate").set(DateUtil.today());
					}
				}
				catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			if (p_ctype == GIPI_CELL_MAPPED) {
				if (StringUtils.equals(bcc.getCellLabel(), "atd_flag")) {
					ZkJxPickInput pickComp = (ZkJxPickInput) LeaveApplication.getCellComponent(bcc);
					pickComp.setPopupWidth("200px");
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED && p_ctype != GIPI_PULLDOWN_OPENED && p_ctype != GIPI_PULLDOWN_CLOSED)
				setDirtyFlag(true);
		}
	}

	private void showErrorMessageAndExit(String errMsg) {
		ZkBiMsgbox.show(ZkBiMsgbox.Type.error, errMsg, new String[] {sessionHelper.getBtLabel("Ok")}, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				Button btClose = (Button) curComp.getFellowIfAny("btClose" ,true);
				Events.echoEvent(Events.ON_CLICK, btClose, null);
			}
		});
	}
	
	private void fireRefreshPage() {
		UniLog.log("fireRefreshPage");
		Button btReloadDetail = (Button)curComp.getFellowIfAny("btReloadDetail", true);
		if (btReloadDetail != null)
			Events.echoEvent(Events.ON_CLICK, btReloadDetail, null);
	}

	private void setupOnOffDetailCell(final CellCollection cl) throws Exception {
		if (StringUtils.equals(cl.getString("atd_atype"), "00")) {
			cl.getCell("atd_date").setMode(Cell.VMODE_NORMAL);
			cl.getCell("atd_time").setMode(Cell.VMODE_NORMAL);
		} else {
			cl.getCell("atd_date").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("atd_time").setMode(Cell.VMODE_DISPONLY);
		}
	}
}
