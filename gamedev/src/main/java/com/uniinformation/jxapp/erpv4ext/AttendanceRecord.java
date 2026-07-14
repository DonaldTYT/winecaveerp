package com.uniinformation.jxapp.erpv4ext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Space;
import org.zkoss.zul.Vlayout;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4ext.BiResultAttendanceRecord;
import com.uniinformation.bicore.erpv4ext.BiResultLeaveApplication;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellVector;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jx.zk.ZkJxTimePicker;
import com.uniinformation.jxapp.JxZkBiBase;
import com.kyoko.common.DateUtil;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.MapUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.ZkUtil.PickByListForm;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class AttendanceRecord extends JxZkBiBase {
	private static final int SHIFT_INTERVAL_GAP = 15;
	private static final int MAX_SHIFT_INTERVAL = 2879;
	private static final int LEAVETIME_FOR_WHOLE_DAY = 240;
	//private static final int MIN_PUNCH_INTERVAL = 120;
	private static final String[] xattKeys = new String[] {"at_xattin0", "at_xattout0", "at_xattin1", "at_xattout1", "at_xattin2", "at_xattout2"};
	private static final String[] shiftCodekeys = new String[] {
		"shtar_sfcode7a", "shtar_sfcode7b", "shtar_sfcode7c",
		"shtar_sfcode1a", "shtar_sfcode1b", "shtar_sfcode1c",
		"shtar_sfcode2a", "shtar_sfcode2b", "shtar_sfcode2c",
		"shtar_sfcode3a", "shtar_sfcode3b", "shtar_sfcode3c",
		"shtar_sfcode4a", "shtar_sfcode4b", "shtar_sfcode4c",
		"shtar_sfcode5a", "shtar_sfcode5b", "shtar_sfcode5c",
		"shtar_sfcode6a", "shtar_sfcode6b", "shtar_sfcode6c"
	};
	private static int compensationRg;

	private List<CellCollection> leaveList = new ArrayList<CellCollection>();
	private Map<Date, List<CellCollection>> attenddetMap = new HashMap<Date, List<CellCollection>>();

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
								if ((endDate.getTime() - startDate.getTime()) / 86400000 > 99) {
									ZkUtil.showErrMsg("Period should be within 100 days");
									return;
								}
								((BiResultAttendanceRecord)getBr()).setQueryPeriod(startDate, endDate);
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
		((Listbox)jxAdd("list_erpv4ext_Attendance").getNativeObject()).renderAll();;
		new ZkBiAbstractLongOp(curComp, "Loading", 100) {
			@Override
			public ReturnMsg longOp() {
				try {
					BiResultAttendanceRecord br = (BiResultAttendanceRecord)p_br;
					Date periodStartDate = br.getQueryPeriodStartDate();
					Date periodEndDate = br.getQueryPeriodEndDate();
					br.getCell("em_xperiodstdate").set(periodStartDate);
					br.getCell("em_xperiodenddate").set(periodEndDate);
					br.clearManualAttDetMap();
					fillLeaveList();
					fillAttenddetMap();
					Shift shift = new Shift(ZkUtil.getBiCellCollectionMap(br.getCurrentCollection(), br.getColumns()), br.getSelectUtil(), periodStartDate, periodEndDate, new HashMap<>());
					if (shift.chkAndAddShiftmask()) {
						fireRefreshPage();
						return null;
					}
					Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.Attendance");
					for (BiCellCollection cc : recs) {
						Date date = cc.getDate("at_date");
						int wktime = cc.getInt("at_wktime");
						int sot = cc.getInt("at_sot");
						int ot = cc.getInt("at_ot");
						int late = cc.getInt("at_late");
						int nowork = cc.getInt("at_nowork");
						int othr = cc.getInt("at_othr");
						boolean manualot = cc.getBoolean("at_manualot");
						//fill minute component
						cc.getCell("at_xdayofweek").set(getShortDayOfWeek(sessionHelper, date));
						cc.getCell("at_xholidaystr").set(makeAttendHolidayStr(date));
						cc.getCell("at_xwktime").set(minuteToHHmm(wktime, true));
						cc.getCell("at_xsot").set(minuteToHHmm(sot, true));
						cc.getCell("at_xot").set(minuteToHHmm(ot, true));
						cc.getCell("at_xlate").set(minuteToHHmm(late, true));
						cc.getCell("at_xnowork").set(minuteToHHmm(nowork, true));
						cc.getCell("at_xothr").set(minuteToDate(othr));
						//visible manual OT component
						LeaveApplication.getCellComponent(cc, "at_xothr").setVisible(manualot);
						Button btManualAtt = (Button)LeaveApplication.getCellComponent(cc, "at_xmanualatt");
						List<CellCollection> atdList = attenddetMap.get(date);
						if (atdList != null) {
							//fill attendance detail component
							Map<String, String> xattAtypeMap = Arrays.stream(xattKeys).collect(Collectors.toMap(s -> s, s -> ""));
							int maxCount = fillAttDetComp(cc, "atd_flag", atdList, xattAtypeMap, false);
							//manual attendance detail
							Map<String, Date>[] attDets = new Map[3 * maxCount];
							int maxIndex = -1;
							for (String key : xattKeys) {
								String a = cc.getCellString(key);
								String[] ss = a.split(",", -1);
								String[] atypes = xattAtypeMap.get(key).split(",", -1);
								for (int i = 0; i < ss.length; i++) {
									if (StringUtils.isNotBlank(ss[i]) && atypes[i].equals("00")) {
										int ki = Integer.parseInt(key.substring(key.length() - 1));
										int index = i * 3 + ki;
										Map<String, Date> m = attDets[index];
										if (m == null) {
											m = new HashMap<String, Date>();
											attDets[index] = m;
										}
										m.put(key.contains("out") ? "OU" : "IN", minuteStrToDate(ss[i]));
										maxIndex = Math.max(maxIndex, index);
									}
								}
							}
							List<Map<String, Date>> attDetList = new ArrayList<Map<String, Date>>();
							for (int i = 0; i <= maxIndex; i++) {
								if (attDets[i] != null)
									attDetList.add(attDets[i]);
								else
									attDetList.add(new HashMap<String, Date>());
							}
							if (!attDetList.isEmpty()) {
								br.putManualAttDet(date, attDetList);
								btManualAtt.addSclass("hasrecord");
							}
						}
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
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			BiResultAttendanceRecord parentBr = (BiResultAttendanceRecord)br;
			Vector<BiCellCollection> recs = br.getSubLinkResult("erpv4ext.Attendance");
			for (BiCellCollection cc : recs) {
				Date date = cc.getDate("at_date");
				Date xothr = cc.getDate("at_xothr");
				boolean manualot = cc.getBoolean("at_manualot");
				if (manualot) {
					if (DateUtil.isDateNull(xothr))
						return new ReturnMsg(false, "Time cannot be empty", true);
					int othr = cc.getInt("at_othr");
					if (othr <= 0 || othr >= BiResultLeaveApplication.MAX_MINUTE_IN_DAY)
						return new ReturnMsg(false, "Invalid time", true);
				}
				List<Map<String, Date>> attDetList = parentBr.getManualAttDet(date);
				cc.getCell("at_flag3").set(attDetList != null && !attDetList.isEmpty());
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
			new AttendanceGetItemProperty(p_br.getSubLink("erpv4ext.Attendance"))
		);	
	}

	/* Customized GIPI for Attendance */
	private class AttendanceGetItemProperty extends BiGetItemProperty {
		//PickByTableTrForm pickShiftCodeForm;
		PickByListForm pickShiftCodeForm;
		Template attendanceIn0Template, attendanceOut0Template, attendanceIn1Template, attendanceOut1Template, attendanceIn2Template, attendanceOut2Template;
		Vector<Object> columnListFiling;
		public AttendanceGetItemProperty(BiResult p_br) {
			super(p_br);
			attendanceIn0Template = ((Component) getNativeComponent()).getTemplate("template_Attendance_In0");
			attendanceOut0Template = ((Component) getNativeComponent()).getTemplate("template_Attendance_Out0");
			attendanceIn1Template = ((Component) getNativeComponent()).getTemplate("template_Attendance_In1");
			attendanceOut1Template = ((Component) getNativeComponent()).getTemplate("template_Attendance_Out1");
			attendanceIn2Template = ((Component) getNativeComponent()).getTemplate("template_Attendance_In2");
			attendanceOut2Template = ((Component) getNativeComponent()).getTemplate("template_Attendance_Out2");
			columnListFiling = new Vector<Object>();

			Vector<BiColumn> v = p_br.getListColumns();
			for (BiColumn bc : v) {
				if (StringUtils.equals(bc.getLabel(), "at_xattin0"))
					columnListFiling.add(attendanceIn0Template);
				else if (StringUtils.equals(bc.getLabel(), "at_xattout0"))
					columnListFiling.add(attendanceOut0Template);
				else if (StringUtils.equals(bc.getLabel(), "at_xattin1"))
					columnListFiling.add(attendanceIn1Template);
				else if (StringUtils.equals(bc.getLabel(), "at_xattout1"))
					columnListFiling.add(attendanceOut1Template);
				else if (StringUtils.equals(bc.getLabel(), "at_xattin2"))
					columnListFiling.add(attendanceIn2Template);
				else if (StringUtils.equals(bc.getLabel(), "at_xattout2"))
					columnListFiling.add(attendanceOut2Template);
				else
					columnListFiling.add(bc);
			}
		}

		@Override
		protected Vector getListColumns(Object p_v) {
			return columnListFiling;
		}

		@Override
		public Object getHeader(Object p_v, int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			UniLog.log("getHeader: " + o);
			if (o instanceof BiColumn) 
				return super.getHeader(p_v, p_col);
			BiColumn col = null;
			if (o == attendanceIn0Template)
				col = bigibr.getColumnByLabel("at_xattin0");
			else if (o == attendanceOut0Template)
				col = bigibr.getColumnByLabel("at_xattout0");
			else if (o == attendanceIn1Template)
				col = bigibr.getColumnByLabel("at_xattin1");
			else if (o == attendanceOut1Template)
				col = bigibr.getColumnByLabel("at_xattout1");
			else if (o == attendanceIn2Template)
				col = bigibr.getColumnByLabel("at_xattin2");
			else if (o == attendanceOut2Template)
				col = bigibr.getColumnByLabel("at_xattout2");
			if (col != null)
				return MapUtil.of(
					"label", col.getEngName(),
					"biColumn", col,
					"biResult", bigibr
				);
			return "";
		}

		@Override
		public String getColumnWidth(Object p_v, int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			UniLog.log1("getColumnWidth o:%s", o);
			if (o == attendanceIn0Template || o == attendanceOut0Template
					|| o == attendanceIn1Template || o == attendanceOut1Template
					|| o == attendanceIn2Template || o == attendanceOut2Template)
				return "45px";
			return super.getColumnWidth(p_v, p_col);
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_PULLDOWN_OPENED) {
				if (StringUtils.equals(bcc.getCellLabel(), "at_shiftcode")) {
					try {
						ZkJxPickInput pickComp = (ZkJxPickInput)LeaveApplication.getCellComponent(bcc);
						if (pickShiftCodeForm == null) {
							/*pickShiftCodeForm = new PickByTableTrForm(sessionHelper, new String[] { "emsft_code", "emsft_name" }, new String[] {"width=50px", "hflex=1;halign=left"}, () -> {
								return Triple.of(getBr().getSelectUtil(), "select emsft_code, emsft_name from emshiftmaster order by emsft_code", null);
							}, (rec, tr, userData) -> {
								((BiCellCollection)userData).getCell("at_shiftcode").set((String)rec[tr.getFieldIndex("emsft_code")]);
								setDirtyFlag(true);
							});*/
							pickShiftCodeForm = com.uniinformation.jxapp.erpv4ext.Shift.getPickShiftCodeForm(sessionHelper, getBr().getSelectUtil(), (rec, userData) -> {
								((BiCellCollection)userData).getCell("at_shiftcode").set((String)rec[0]);
								setDirtyFlag(true);
							});
						}
						pickShiftCodeForm.bindComponent(pickComp, cl, false);
					}
					catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
			else if (p_ctype == GIPI_VALUE_CHANGED) {
				try {
					BiResultAttendanceRecord parentBr = (BiResultAttendanceRecord)getBr();
					Date atDate = cl.getDate("at_date");
					Button btManualAtt = (Button)LeaveApplication.getCellComponent(cl, "at_xmanualatt");
					if (StringUtils.equals(bcc.getCellLabel(), "at_xmanualatt")) {
						//click manual attenddet button
						List<Map<String, Date>> refList = parentBr.getManualAttDet(atDate);
						if (refList == null) {
							refList = new ArrayList<Map<String, Date>>();
							parentBr.putManualAttDet(atDate, refList);
						}
						showManualAttdetDialog(atDate, cl.getString("at_shiftcode"), btManualAtt, refList, cl);
					}
					if (StringUtils.equals(bcc.getCellLabel(), "at_manualot")) {
						//change manual OT checkbox
						Component compOthr = LeaveApplication.getCellComponent(cl, "at_xothr");
						if (bcc.getBoolean()) {
							compOthr.setVisible(true);
							cl.getCell("at_xothr").set(minuteToDate(cl.getInt("at_othr")));
						} else {
							compOthr.setVisible(false);
							cl.getCell("at_othr").set(0);
							cl.getCell("at_xothr").set(minuteToDate(0));
						}
					}
					if (StringUtils.equals(bcc.getCellLabel(), "at_xothr")) {
						//change manual OT time
						LeaveApplication.zeroColumnCellDateSecond(bcc);
						cl.getCell("at_othr").set(dateToMinute(cl.getDate("at_xothr")));
						int min = cl.getInt("at_othr");
						UniLog.log1("at_xothr:%s, min:%d", bcc.getDate(), min);
						if (min < 0 || min >= BiResultLeaveApplication.MAX_MINUTE_IN_DAY)
							LeaveApplication.showErrorNotification("Invalid time", bcc);
					}
				}
				catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			if (p_ctype == GIPI_CELL_MAPPED) {
				if (StringUtils.equals(bcc.getCellLabel(), "at_shiftcode")) {
					ZkJxPickInput zjpi = (ZkJxPickInput) LeaveApplication.getCellComponent(bcc);
					zjpi.setPopupWidth("350px");
				}
				try {
					if (StringUtils.equals(bcc.getCellLabel(), "at_xmanualatt")) {
						Button btn = (Button)LeaveApplication.getCellComponent(cl, "at_xmanualatt");
						btn.setLabel(TranslateUtil.getText(sessionHelper, "ERPV4EXT.ATTENDANCE.AT_XMANUALATT", "BUTTON", "Manual"));
					}
				} catch (Exception e) {
					UniLog.log(e);
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED && p_ctype != GIPI_PULLDOWN_OPENED && p_ctype != GIPI_PULLDOWN_CLOSED) {
				if (!StringUtils.equals(bcc.getCellLabel(), "at_xmanualatt"))
					setDirtyFlag(true);
			}
		}
	}

	private void fillLeaveList() throws Exception {
		leaveList.clear();
		CellVector cv = getBr().getSelectUtil().getQueryResultToCellVector("select * from leave " + 
				" where lv_eid = ? and lv_sdate <= ? and lv_edate >= ? order by lv_sdate, lv_edate, lv_reason", 
				new Wherecl().appendArgument(getBr().getCellString("em_eid"))
							.appendArgument(getBr().getCellDate("em_xperiodenddate"))
							.appendArgument(getBr().getCellDate("em_xperiodstdate")));
		for (Object occ : cv) {
			CellCollection cc = (CellCollection)occ;
			//UniLog.log1("lv_sdate:%s, lv_edate:%s, lv_reason:%s", cc.getDate("lv_sdate"), cc.getDate("lv_edate"), cc.getString("lv_reason"));
			leaveList.add(cc);
		}
	}
	
	private void fillAttenddetMap() throws Exception {
		attenddetMap.clear();
		CellVector cv = getBr().getSelectUtil().getQueryResultToCellVector("select * from attenddet " + 
				" where atd_eid = ? and atd_date between ? and ? order by atd_time, atd_atime", 
				new Wherecl().appendArgument(getBr().getCellString("em_eid"))
							.appendArgument(getBr().getCellDate("em_xperiodstdate"))
							.appendArgument(getBr().getCellDate("em_xperiodenddate")));
		for (Object occ : cv) {
			CellCollection cc = (CellCollection)occ;
			Date date = cc.getDate("atd_date");
			List<CellCollection> list = attenddetMap.get(date);
			if (list == null) {
				list = new ArrayList<CellCollection>();
				attenddetMap.put(date, list);
			}
			list.add(cc);
		}
	}
	
	public static class Shift {
		private String[] shiftCodes = new String[shiftCodekeys.length];
		private boolean shiftArrangePubhol;
		
		private Date periodStartDate, periodEndDate;
		
		Map<String, Object> cellCc;
		SelectUtil su;
		public Shift(Map<String, Object> cellCc, SelectUtil su, Date periodStartDate, Date periodEndDate, Map<String, Object> loadMap) throws Exception {
			this.cellCc = cellCc;
			this.su = su;
			this.periodStartDate = periodStartDate;
			this.periodEndDate = periodEndDate;

			Arrays.fill(shiftCodes, "");
			shiftArrangePubhol = false;
	
			int emShiftRg = (int)cellCc.get("em_shtar");
			int gdShiftRg = (int)cellCc.get("gdmt_shtrg");
			Integer coShiftRg = (Integer)loadMap.get("coShiftRg");
			if (coShiftRg == null) {
				TableRec tr = su.getQueryResult("select co_shtarrange from cocode", null);
				tr.setRecPointer(0);
				loadMap.put("coShiftRg", coShiftRg = tr.getFieldInt("co_shtarrange"));
			}
			UniLog.log1("emShiftRg:%d, gdShiftRg:%d, coShiftRg:%d", emShiftRg, gdShiftRg, coShiftRg);
			for (int shiftRg : new int[] { emShiftRg, gdShiftRg, coShiftRg }) {
				if (shiftRg == 0)
					continue;
				UniLog.log1("shiftRg:%d", shiftRg);
				TableRec tr = su.getQueryResult("select * from shiftarrange where shtar_rg = ?", new Wherecl().appendArgument(shiftRg));
				if (tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					boolean foundShift = false;
					for (String key : shiftCodekeys) {
						if (StringUtils.isNotBlank(tr.getFieldString(key))) {
							foundShift = true;
							break;
						}
					}
					if (foundShift) {
						for (int i = 0; i < shiftCodekeys.length; i++)
							shiftCodes[i] = tr.getFieldString(shiftCodekeys[i]);
						shiftArrangePubhol = StringUtils.equals(tr.getFieldString("shtar_pubhol"), "Y");
						break;
					}
				}
			}
			UniLog.log1("shiftCodes:%s, shtar_pubhol:%b", Arrays.toString(shiftCodes), shiftArrangePubhol);
		}
		
		public boolean getShiftArrangePubhol() {
			return shiftArrangePubhol;
		}

		public boolean chkAndAddShiftmask() throws Exception {
			UniLog.log1("chkAndAddShiftmask");
			boolean updatedDb = false;
			String em_eid = (String)cellCc.get("em_eid");
			Date em_stdate = (Date)cellCc.get("em_stdate");
			Date em_enddate = (Date)cellCc.get("em_enddate");
			String shiftchgStr = (String)cellCc.get("em_shiftchg");
			boolean em_shiftchg = (boolean)Objects.equals(shiftchgStr, "Y");
			UniLog.log1("em_eid:%s, em_stdate:%s, em_enddate:%s, em_shiftchg:%b, shiftchgStr:%s", em_eid, em_stdate, em_enddate, em_shiftchg, shiftchgStr);
			Date tdate0 = periodStartDate;
			Date tdate1 = periodEndDate;
			Date tmpdate = DateUtil.prevday(tdate0);
			String at_eid = em_eid;
			CellVector cv = su.getQueryResultToCellVector("select at_date, at_shiftcode from attendance " + 
					" where at_eid = ? and at_date between ? and ? order by at_date", 
					new Wherecl().appendArgument(em_eid).appendArgument(tdate0).appendArgument(tdate1));
			for (Object occ : cv) {
				CellCollection cc = (CellCollection)occ;
				Date at_date = cc.getDate("at_date");
				String at_shiftcode = cc.getString("at_shiftcode");
				Date tmpatdate = at_date;
				String tmpatshiftcode = at_shiftcode;
				while (tmpatdate.getTime() - tmpdate.getTime() > 1 * 86400000) {
					tmpdate = DateUtil.nextday(tmpdate);
					at_date = tmpdate;
					if (at_date.compareTo(em_stdate) < 0 || (!DateUtil.isDateNull(em_enddate) && at_date.compareTo(em_enddate) > 0))
						at_shiftcode = "-";
					else {
						if (em_shiftchg)
							at_shiftcode = getLastShiftCode(em_eid, at_date, "-");
						else 
							at_shiftcode = getDefaultShiftCode(at_date, "-");
					}
					su.executeUpdate("insert into attendance (at_eid, at_date, at_shiftcode) values(?,?,?)", 
							new Wherecl().appendArgument(at_eid).appendArgument(at_date).appendArgument(at_shiftcode));
					//UniLog.log1("chkAndAddShiftmask insert eid:%s, date:%s, shiftcode:%s", at_eid, at_date, at_shiftcode);
					updatedDb = true;
				}
				if (StringUtils.isBlank(tmpatshiftcode)) {
					//should not happen, but still handle it for safety
					if (at_date.compareTo(em_stdate) < 0 || (!DateUtil.isDateNull(em_enddate) && at_date.compareTo(em_enddate) > 0)) {
						tmpatshiftcode = "-";
					} else {
						if (em_shiftchg)
							at_shiftcode = getLastShiftCode(em_eid, at_date, "-");
						else 
							tmpatshiftcode = getDefaultShiftCode(tmpatdate, "-");
					}
					su.executeUpdate("update attendance set at_shiftcode = ? where at_eid = ? and at_date = ?", 
							new Wherecl().appendArgument(tmpatshiftcode).appendArgument(em_eid).appendArgument(tmpatdate));
					//check_and_set_auto_holiday(p_eid,tmpatdate,tmpatshiftcode)
					//UniLog.log1("chkAndAddShiftmask update eid:%s, date:%s, shiftcode:%s", at_eid, at_date, tmpatshiftcode);
					updatedDb = true;
				}
				tmpdate = tmpatdate;
			}
			while (tdate1.compareTo(tmpdate) > 0) {
				tmpdate = DateUtil.nextday(tmpdate);
				Date at_date = tmpdate;
				String at_shiftcode;
				if (at_date.compareTo(em_stdate) < 0 || (!DateUtil.isDateNull(em_enddate) && at_date.compareTo(em_enddate) > 0))
					at_shiftcode = "-";
				else {
					if (em_shiftchg)
						at_shiftcode = getLastShiftCode(em_eid, at_date, "-");
					else 
						at_shiftcode = getDefaultShiftCode(at_date, "-");
				}
				su.executeUpdate("insert into attendance (at_eid, at_date, at_shiftcode) values(?,?,?)", 
							new Wherecl().appendArgument(at_eid).appendArgument(at_date).appendArgument(at_shiftcode));
				//UniLog.log1("chkAndAddShiftmask insert eid:%s, date:%s, shiftcode:%s", at_eid, at_date, at_shiftcode);
				updatedDb = true;
				//check_and_set_auto_holiday(at_eid,at_date,at_shiftcode)
			}
			return updatedDb;
		}
		
		private String getDefaultShiftCode(Date p_date, String p_shiftcode) throws Exception {
			/*if (shiftArrangePubhol) {
				TableRec tr = su.getQueryResult("select serial_id from calendar where cd_date = ?", new Wherecl().appendArgument(p_date));
				if (tr.getRecordCount() > 0)
					return "-";
			}*/
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(p_date);
			int cc = cal.get(Calendar.DAY_OF_WEEK) - 1;
			//UniLog.log1("p_date:%s, cc:%d", p_date, cc);
			return StringUtils.defaultIfBlank(getShiftCode(cc, 3, p_date), p_shiftcode);
		}
		
		private String getShiftCode(int p_idx, int p_mul, Date p_date) {
			if (p_idx < 0 || p_idx * p_mul >= shiftCodes.length)
				return "";
			int i;
			for (i = (p_idx + 1) * p_mul - 1; i >= p_idx * p_mul; i--) {
				if (StringUtils.isNotBlank(shiftCodes[i]))
					break;
			}
			if (i < p_idx * p_mul)
				return "";
			int cc = i - p_idx * p_mul + 1;
			Date zeroDate = DateUtil.dateTimeStrToDate("1899/12/31");
			long dayNum = (p_date.getTime() - zeroDate.getTime()) / 86400000;
			long weekSeq = dayNum / 7;
			int ccc = (int)(weekSeq % cc);
			String r = shiftCodes[p_idx * p_mul + ccc];
			//UniLog.log1("p_idx:%d, p_date:%s, cc:%d, dayNum:%d, weekSeq:%d, ccc:%d, r:%s", p_idx, p_date, cc, dayNum, weekSeq, ccc, r);
			return r;
		}
	
		private String getLastShiftCode(String p_eid, Date p_date, String p_shiftcode) throws Exception {
			String tmpatshiftcode = p_shiftcode;
			Date tmpdate = p_date;
			for (int i = 0; i < 4; i++) {
				tmpdate = DateUtil.prevday(tmpdate, 7);
				TableRec tr = su.getQueryResult("select at_shiftcode from attendance where at_eid = ? and at_date = ?", 
						new Wherecl().appendArgument(p_eid).appendArgument(tmpdate));
				if (tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					tmpatshiftcode = tr.getFieldString("at_shiftcode");
				} else
					return p_shiftcode;
				if (!StringUtils.startsWith(tmpatshiftcode, "-"))
					return tmpatshiftcode;
			}
			return tmpatshiftcode;
		}
	}
	
	
	
	private String makeAttendHolidayStr(Date atdate) {
		StringBuilder sb = new StringBuilder();
		for (CellCollection cc : leaveList) {
			if (cc.getDate("lv_sdate").compareTo(atdate) > 0) 
				break;
			if (cc.getDate("lv_edate").compareTo(atdate) >= 0) {
				if (sb.length() > 0) 
					sb.append("/");
				sb.append(cc.getString("lv_reason"));
				if (cc.getDate("lv_edate").compareTo(atdate) > 0) 
					break;
			}
		}
		return sb.toString();
	}
	
	public static String getShortDayOfWeek(SessionHelper sessionHelper, Date date) {
		int d = DateUtil.toDayOfWeek(date);
		String[] estr = new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		String[] cstr = new String[] { "\u65e5", "\u4e00", "\u4e8c", "\u4e09", "\u56db", "\u4e94", "\u516d" };
		return StringUtils.equalsAny(sessionHelper.getLHLang(), "TCHN", "SCHN") ? cstr[d] : estr[d];
	}
	
	private static String integerToHHmm(int i) {
		return DateUtil.dateDigtalToTimeStr(new Date(i * 1000L), false);
	}

	private static String minuteToHHmm(int i, boolean calcNegative) {
		int j = Math.abs(i);
		return ((calcNegative && i < 0) ? "-" : "") + (j > 0 ? String.format("%02d:%02d", j / 60, j % 60) : "");
	}

	private static int getMinute(AttldOldItem itemd, AttlOldItem item) {
		return (int)((itemd.oldd_atime.getTime() - item.old_date.getTime()) / 60000);
	}

	public static Date minuteToDate(int i) {
		if (i < 0)
			i = 0;
		return new Date(i * 60 * 1000L - DateUtil.getGmtOffset());
	}

	public static int dateToMinute(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		int dd = cal.get(Calendar.DAY_OF_YEAR) - 1;
		int hh = cal.get(Calendar.HOUR_OF_DAY);
		int mm = cal.get(Calendar.MINUTE);
		return dd * 1440 + hh * 60 + mm;
	}

	public static Date minuteStrToDate(String s) {
		return minuteToDate(DateUtil.timeToMin(s));
	}
	
	public static String getAttendShiftCode(String code) {
		return Objects.equals(code, "-") ? "-" : StringUtils.startsWith(code, "-") ? code.substring(1) : code;
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
	
	private void showManualAttdetDialog(final Date atdate, final String atsftcode, final Button button, final List<Map<String, Date>> refList, final BiCellCollection refCl) {
		try {
			Vlayout vl = new Vlayout();
			final GridHelper ghh = new GridHelper(2);
			ghh.getColumn(0).setHflex("min");
			ghh.getColumn(0).setAlign("right");
			ghh.getColumn(1).setHflex("1");
			final GridHelper gh = new GridHelper(5);
			gh.setSclass("zkbi-da");
			gh.getColumn(0).setHflex("1");
			gh.getColumn(1).setWidth("50px");
			gh.getColumn(2).setWidth("50px");
			gh.getColumn(3).setWidth("80px");
			gh.getColumn(4).setWidth("80px");
	
			ghh.addRow(new Label("Name:") {{ setAttribute("tlkey", "lb_name_d"); }}, new Label(getBr().getCellString("em_name")));
			ghh.addRow(new Label("Emp.Id:") {{ setAttribute("tlkey", "lb_emid_d"); }}, new Hlayout() {{
				appendChild(new Label(getBr().getCellString("em_eid")));
				appendChild(new Space());
				appendChild(new Label("Date:") {{ setAttribute("tlkey", "lb_date_d"); }});
				appendChild(new Label(DateUtil.dateToDateTimeStr(atdate, "yyyy/MM/dd")));
				appendChild(new Space());
				appendChild(new Label("Shift:") {{ setAttribute("tlkey", "lb_sftcode_d"); }});
				appendChild(new Label(atsftcode));
			}});

			gh.appendChild(new Auxhead() {{
				appendChild(new Auxheader() {{ appendChild(new Label()); }});
				appendChild(new Auxheader() {{ setColspan(2); appendChild(new Label("Normal") {{ setAttribute("tlkey", "lb_normal"); }}); }});
				appendChild(new Auxheader() {{ setColspan(2); appendChild(new Label("Actual") {{ setAttribute("tlkey", "lb_actual"); }}); }});
			}});
			gh.appendChild(new Auxhead() {{
				appendChild(new Auxheader() {{ appendChild(new Label("Shift Name") {{ setAttribute("tlkey", "lb_sftname"); }}); }});
				appendChild(new Auxheader() {{ appendChild(new Label("In") {{ setAttribute("tlkey", "lb_in"); }}); }});
				appendChild(new Auxheader() {{ appendChild(new Label("Out") {{ setAttribute("tlkey", "lb_out"); }}); }});
				appendChild(new Auxheader() {{ appendChild(new Label("In") {{ setAttribute("tlkey", "lb_in"); }}); }});
				appendChild(new Auxheader() {{ appendChild(new Label("Out") {{ setAttribute("tlkey", "lb_out"); }}); }});
			}});

			vl.appendChild(ghh);
			vl.appendChild(gh);
			
			//fill shift detail
			CellVector cv = getBr().getSelectUtil().getQueryResultToCellVector(
					"select emsftd_name, emsftd_sttime, emsftd_endtime from emshiftdetail " + 
					" where emsftd_code = ? and emsftd_type = 'N' order by emsftd_sttime", new Wherecl().appendArgument(getAttendShiftCode(atsftcode)));
			final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			int i;
			for (i = 0; i < cv.size(); i++) {
				CellCollection cc = (CellCollection)cv.get(i);
				String name = cc.getString("emsftd_name");
				int iStartTime = cc.getInt("emsftd_sttime");
				int iEndTime = cc.getInt("emsftd_endtime");
				Date startTime = new Date(iStartTime * 1000L);
				Date endTime = new Date(iEndTime * 1000L);
				final String startTimeStr = DateUtil.dateDigtalToTimeStr(startTime, false);//hhmmtf.format(startTime);
				final String endTimeStr = DateUtil.dateDigtalToTimeStr(endTime, false);//hhmmtf.format(endTime);
				//UniLog.log1("name:%s, iStartTime:%d, iEndTime:%d, startTime:%s, endTime:%s, startTimeStr:%s, endTimeStr:%s", name, iStartTime, iEndTime, startTime, endTime, startTimeStr, endTimeStr);
				ZkJxTimePicker startTimeComp = newTimeComp();
				ZkJxTimePicker endTimeComp = newTimeComp();
				if (i < refList.size()) {
					Map<String, Date> rm = refList.get(i);
					Date inDate = rm.get("IN");
					Date outDate = rm.get("OU");
					if (inDate != null)
						startTimeComp.setValue(DateUtil.dateDigtalToTimeStr(inDate, false));
					if (outDate != null)
						endTimeComp.setValue(DateUtil.dateDigtalToTimeStr(outDate, false));
				}
				gh.addRow(new Label(name), new Label(startTimeStr), new Label(endTimeStr), startTimeComp, endTimeComp);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("startTimeComp", startTimeComp);
				m.put("endTimeComp", endTimeComp);
				list.add(m);
			}
			for (; i < refList.size(); i++) {
				ZkJxTimePicker startTimeComp = newTimeComp();
				ZkJxTimePicker endTimeComp = newTimeComp();
				Map<String, Date> rm = refList.get(i);
				startTimeComp.setValue(DateUtil.dateDigtalToTimeStr(rm.get("IN"), false));
				endTimeComp.setValue(DateUtil.dateDigtalToTimeStr(rm.get("OU"), false));
				gh.addRow(new Label(), new Label(), new Label(), startTimeComp, endTimeComp);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("startTimeComp", startTimeComp);
				m.put("endTimeComp", endTimeComp);
				list.add(m);
			}
			if (list.isEmpty()) {
				ZkJxTimePicker startTimeComp = newTimeComp();
				ZkJxTimePicker endTimeComp = newTimeComp();
				gh.addRow(new Label(), new Label(), new Label(), startTimeComp, endTimeComp);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("startTimeComp", startTimeComp);
				m.put("endTimeComp", endTimeComp);
				list.add(m);
			}

			ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Clear")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
			new ZkBiMsgbox(sessionHelper).setContent(vl).setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
					if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Clear"))) {
						refList.clear();
						boolean foundRecord = false;
						fillAttDetComp(refCl, "atd_oflag", attenddetMap.get(atdate), null, true);
						for (String key : xattKeys) {
							if (StringUtils.isNotBlank(refCl.getString(key))) {
								foundRecord = true;
								break;
							}
						}
						if (!foundRecord)
							fillAttDetComp(refCl, "atd_flag", attenddetMap.get(atdate), null, true);
						button.removeSclass("hasrecord");
						refCl.getCell("at_xflag3").set(true);
						setDirtyFlag(true);
					}
					else if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Ok"))) {
						List<Map<String, Date>> rList = new ArrayList<Map<String, Date>>();
						for (Map<String, Object> m : list) {
							ZkJxTimePicker startTimeComp = (ZkJxTimePicker)m.get("startTimeComp");
							ZkJxTimePicker endTimeComp = (ZkJxTimePicker)m.get("endTimeComp");
							String startTimeStr = startTimeComp.getValue().trim();
							String endTimeStr = endTimeComp.getValue().trim();
							if (StringUtils.isNotBlank(startTimeStr) || StringUtils.isNoneBlank(endTimeStr)) {
								if (StringUtils.isBlank(startTimeStr)) {
									ZkUtil.errMsg("Please input Start Time");
									return;
								}
								if (StringUtils.isBlank(endTimeStr)) {
									ZkUtil.errMsg("Please input End Time");
									return;
								}
							}
							else
								continue;
							Date startTime = null;
							Date endTime = null;
							try {
								startTime = minuteStrToDate(startTimeStr);
							}
							catch (Exception e) {
								UniLog.log(e);
								ZkUtil.errMsg("Invalid Start Time");
								return;
							}
							try {
								endTime = minuteStrToDate(endTimeStr);
							}
							catch (Exception e) {
								UniLog.log(e);
								ZkUtil.errMsg("Invalid End Time");
								return;
							}
							if (DateUtil.isDateNull(startTime) || startTime.compareTo(BiResultLeaveApplication.MAX_TIME_IN_DAY) >= 0) {
								ZkUtil.errMsg("Invalid Start Time");
								return;
							}
							if (DateUtil.isDateNull(endTime) || endTime.compareTo(BiResultLeaveApplication.MAX_TIME_IN_DAY) >= 0) {
								ZkUtil.errMsg("Invalid End Time");
								return;
							}
							if (startTime.compareTo(endTime) >= 0) {
								ZkUtil.errMsg("Start Time must be less End Time");
								return;
							}
							Map<String, Date> rm = new HashMap<String, Date>();
							rm.put("IN", startTime);
							rm.put("OU", endTime);
							rList.add(rm);
						}
						refList.clear();
						refList.addAll(rList);

						if (refList.isEmpty())
							button.removeSclass("hasrecord");
						else {
							//fill comp
							for (String key : xattKeys) {
								int ki = Integer.parseInt(key.substring(key.length() - 1));
								String flag = key.contains("out") ? "OU" : "IN";
								StringBuilder sb = new StringBuilder();
								for (int i = ki; i < refList.size(); i += 3) {
									Map<String, Date> rm = refList.get(i);
									if (sb.length() > 0)
										sb.append(",");
									sb.append(DateUtil.dateDigtalToTimeStr(rm.get(flag), false));
								}
								refCl.getCell(key).set(sb.toString());
								UniLog.log1("fill comp key:%s, value:%s", key, sb.toString());
							}
							fillAttDetComp(refCl, null);
							button.addSclass("hasrecord");
						}
						refCl.getCell("at_xflag3").set(true);
						setDirtyFlag(true);
					}
				}
			}).build().appendStyle("width:450px;max-width:100%").doModal();
			ZkUtil.translateAllComp(sessionHelper, vl, getBr().getView().getName(), getBr());
		} catch (Exception e) {
			UniLog.log(e);
			ZkUtil.errMsg(e.toString());
		}
	}
	
	private static ZkJxTimePicker newTimeComp() {
		return new ZkJxTimePicker() {{
			setIsShortFormat(true);
			setStepMin(30);
			setEndTime("48:00");
			init();
		}};
	}
	
	private static String getAttDetKey(String flag, int[] js) {
		String key = null;
		if (StringUtils.equals(flag, "IN")) {
			switch (js[0]) {
				case 0: 
				case 5: 
				case 6: 
					key = "at_xattin0";
					js[0] = 1;
					break;
				case 1: 
					case 2: 
					key = "at_xattin1";
					js[0] = 3;
					break;
				case 3:
				case 4: 
					key = "at_xattin2";
					js[0] = 5;
					break;
			}
		}
		else if (StringUtils.equals(flag, "OU")) {
			switch (js[0]) {
				case 0: 
				case 1: 
				case 6: 
					key = "at_xattout0";
					js[0] = 2;
					break;
				case 2: 
				case 3: 
					key = "at_xattout1";
					js[0] = 4;
					break;
				case 4: 
				case 5: 
					key = "at_xattout2";
					js[0] = 6;
					break;
			}
		}
		return key;
	}
	
	private int fillAttDetComp(BiCellCollection cc, String flagKey, List<CellCollection> atdList, Map<String, String> xattAtypeMap, boolean isNot00AType) throws Exception {
		for (String key : xattKeys)
			cc.getCell(key).set("");
		if (atdList != null) {
			int[] js = new int[] {0};
			for (CellCollection atdCc : atdList) {
				String flag = atdCc.getString(flagKey);
				String atype = atdCc.getString("atd_atype");
				int itime = atdCc.getInt("atd_time");
				String timeStr = integerToHHmm(itime);
				String key = getAttDetKey(flag, js);
				if (isNot00AType && StringUtils.equals(atype, "00"))
					continue;
				if (key != null) {
					String a = cc.getCellString(key);
					String str = (StringUtils.isNotBlank(a) ? a + "," : "") + timeStr;
					UniLog.log1("atype:%s, key:%s, str:%s", atype, key, str);
					cc.getCell(key).set(str);
					if (xattAtypeMap != null) {
						String atypes = xattAtypeMap.get(key) + atype + ",";
						xattAtypeMap.put(key, atypes);
					}
				}
			}
		}
		return fillAttDetComp(cc, xattAtypeMap);
	}

	private int fillAttDetComp(BiCellCollection cc, Map<String, String> xattAtypeMap) throws Exception {
		int maxCount = 0;
		for (String key : xattKeys) {
			String a = cc.getCellString(key);
			String[] ss = a.split(",", -1);
			maxCount = Math.max(maxCount, ss.length);
		}
		for (String key : xattKeys) {
			Vlayout vl = (Vlayout)LeaveApplication.getCellComponent(cc, key);
			while (vl.getChildren().size() > 0)
				vl.removeChild(vl.getFirstChild());
			String a = cc.getCellString(key);
			String[] ss = a.split(",", -1);
			String[] atypes = xattAtypeMap != null ? xattAtypeMap.get(key).split(",", -1) : null;
			for (int i = 0; i < maxCount; i++) {
				if (i < ss.length) {
					Label label = new Label(ss[i]);
					if (atypes != null && Objects.equals(atypes[i], "99"))
						label.setStyle("color:#0064ed");
					vl.appendChild(label);
				} else
					vl.appendChild(new Separator() {{setSpacing("24px");}});
			}
		}
		return maxCount;
	}
	
	public static Date unionDateTime(Date pDate, Date pTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(pDate);
		if (!DateUtil.isDateNull(pTime)) {
			Calendar calTime = Calendar.getInstance();
			calTime.setTime(pTime);
			int dd = calTime.get(Calendar.DAY_OF_YEAR) - 1;
			cal.add(Calendar.DAY_OF_YEAR, dd);
			cal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
			cal.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
		} else {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
		return cal.getTime();
	}

	public static void setEmlvrCompensation(SelectUtil su, String p_eid, Date p_date, boolean p_mode) throws Exception {
		if (compensationRg == 0) {
			TableRec tr = su.getQueryResult("select lvrs_rg from leavereason where lvrs_name = 'CL'", null);
			tr.setRecPointer(0);
			compensationRg = tr.getFieldInt("lvrs_rg");
		}
		//UniLog.log1("p_eid: %s, p_date: %s, p_mode: %b", p_eid, p_date, p_mode);
		if (p_mode) {
			TableRec tr = su.getQueryResult("select serial_id from emleaverange "
					+ " where emlvr_emid = ? "
					+ " and emlvr_stdate = ? "
					+ " and emlvr_enddate = ? "
					+ " and emlvr_lvreasonrg = ? "
					+ " and emlvr_mode = 'Y'", 
					new Wherecl().appendArgument(p_eid).appendArgument(p_date).appendArgument(BiResultLeaveApplication.MAX_DATE).appendArgument(compensationRg));
			if (tr.getRecordCount() == 0) {
				su.executeUpdate("insert into emleaverange" + 
						" (emlvr_emid,emlvr_seq,emlvr_stdate,emlvr_enddate," + 
						" emlvr_lvreasonrg,emlvr_lvdaterange,emlvr_lvdleftrange," + 
						" emlvr_remark,emlvr_mode,emlvr_cancelled)" + 
						" values (?,0,?,?,?,1.0,0.0,'','Y','')", 
						new Wherecl().appendArgument(p_eid).appendArgument(p_date).appendArgument(BiResultLeaveApplication.MAX_DATE).appendArgument(compensationRg));
			}
		} else {
			su.executeUpdate("delete from emleaverange where " + 
					" emlvr_emid = ? and " + 
					" emlvr_stdate = ? and " + 
					" emlvr_enddate = ? and " + 
					" emlvr_lvreasonrg = ? and " + 
					" emlvr_mode = 'Y' ", 
					new Wherecl().appendArgument(p_eid).appendArgument(p_date).appendArgument(BiResultLeaveApplication.MAX_DATE).appendArgument(compensationRg));
		}
	}

	private static class AttlOldItem implements Serializable {
		String old_shiftcode;
		Date old_date;
		int old_sttime;
		int old_endtime;
		int old_ot;
		int old_sot;
		int old_dbot;
		int old_lunchot;
		int old_late;
		int old_speclate;
		int old_nowork;
		int old_reallate;
		int old_specatt;
		int old_wktime;
		boolean old_flag2;
		boolean old_flag3;
		boolean old_flag4;
		AttlOldItem saved_item;

		public void saveItem() {
	        saved_item = null;
	        saved_item = SerializationUtils.clone(this);
	    }
		public boolean isDirty() {
			AttlOldItem src = saved_item;
			return !(old_ot == src.old_ot && old_sot == src.old_sot && old_dbot == src.old_dbot && old_lunchot == src.old_lunchot 
					&& old_late == src.old_late && old_speclate == src.old_speclate && old_specatt == src.old_specatt && old_nowork == src.old_nowork 
					&& old_flag2 == src.old_flag2 && old_flag3 == src.old_flag3 && old_reallate == src.old_reallate && old_flag4 == src.old_flag4 && old_wktime == src.old_wktime);
		}
		public boolean isDirty(AttlCurItem src) {
			return !(old_ot == src.cur_ot && old_sot == src.cur_sot && old_dbot == src.cur_dbot && old_lunchot == src.cur_lunchot 
					&& old_late == src.cur_late && old_speclate == src.cur_speclate && old_specatt == src.cur_specatt && old_nowork == src.cur_nowork 
					&& old_flag2 == saved_item.old_flag2 && old_flag3 == saved_item.old_flag3 && old_reallate == src.cur_reallate && old_flag4 == src.cur_flag4 && old_wktime == src.cur_wktime);
		}
	}

	private static class AttlCurItem {
		Date cur_date;
		int cur_sttime;
		int cur_endtime;
		int cur_ot;
		int cur_sot;
		int cur_dbot;
		int cur_lunchot;
		int cur_late;
		int cur_speclate;
		int cur_nowork;
		int cur_reallate;
		int cur_specatt;
		int cur_wktime;
		boolean cur_flag4;
		boolean cur_status;
	}
	
	private static class AttldOldItem {
		Date oldd_date;
		int oldd_time;
		String oldd_flag; // IN/OU
		Date oldd_atime;
		String oldd_atype;
		Date oldd_adate;
		int oldd_serial;
		public boolean isValid(AttlOldItem item) {
			return oldd_atime.getTime() >= item.old_date.getTime() + item.old_sttime * 60000 && oldd_atime.getTime() <= item.old_date.getTime() + item.old_endtime * 60000;
		}
	}

	private static class AttldCurItem {
		Date curd_date;
		int curd_time;
		String curd_flag;
		Date curd_atime;
		String curd_atype;
		Date curd_adate;
		boolean curd_status;
		public boolean equals(AttldOldItem item) {
			return curd_atime.compareTo(item.oldd_atime) == 0 && Objects.equals(curd_atype, item.oldd_atype)
					&& curd_date.compareTo(item.oldd_date) == 0 && curd_time == item.oldd_time && Objects.equals(curd_flag, item.oldd_flag);
		}
	}

	public static class AttendanceRecalc {
		private String MAIN_ATYPE, SEC_ATYPE;
		private Map<String, Pair<Integer, Integer>> shiftMasterMap = new HashMap<String, Pair<Integer, Integer>>(); //shiftmaster, key: shiftcode, value: <starttime, endtime>
		private Map<String, String> readerMap = new HashMap<String, String>(); //key: crdr_id, value: crdr_mode
		private AttlOldItem[] attlOldArr; //old attendance
		private List<AttlCurItem> attlCurList = new ArrayList<AttlCurItem>(); //current attendance
		private AttldOldItem[] attldOldArr; //old attenddet
		private List<AttldCurItem> attldCurList = new ArrayList<AttldCurItem>(); //current attenddet
		
		private CalotShiftTimeItem[] curShiftDetailArr; //shiftdetail
		
		private SelectUtil su;
		private String eid;
		private String attmode; //mode: M/A
		private Date stdate; //period start date
		private Date enddate; //period end date
		
		private Calot calot; //calcute late/night ot/holiday ot/absence etc...
		private ShiftDetCellMap shiftDetCellMap = new ShiftDetCellMap();
		private Map<String, Object> loadMap;
		
		public AttendanceRecalc(SelectUtil su, String eid, String attmode, Date stdate, Date enddate, boolean shiftArrangePubhol, Map<String, Object> loadMap,
							int minlate_theshold, int nightot_lowerlim, String main_atype, String sec_atype, boolean nightot_cross_late) throws Exception {
			this.su = su;
			this.eid = eid;
			this.attmode = attmode;
			this.stdate = stdate;
			this.enddate = enddate;
			this.loadMap = loadMap;
			MAIN_ATYPE = main_atype;
			SEC_ATYPE = sec_atype;
			calot = new Calot(su, stdate, enddate, shiftArrangePubhol, minlate_theshold, nightot_lowerlim, nightot_cross_late, shiftDetCellMap);
		}
		
		private void loadShiftData() throws Exception {
			attlCurList.clear();
			attldCurList.clear();
			shiftMasterMap = (Map<String, Pair<Integer, Integer>>)loadMap.get("shiftMasterMap");
			if (shiftMasterMap == null) {
				shiftMasterMap = ZkUtil.getTableRecStream(su.getQueryResult("select emsft_code,emsft_sttime,emsft_endtime from emshiftmaster order by emsft_code"))
					.collect(Collectors.toMap(c -> c.getString("emsft_code"), 
							c -> Pair.of(dateToMinute(new Date(c.getInt("emsft_sttime") * 1000L)), dateToMinute(new Date(c.getInt("emsft_endtime") * 1000L))), 
							(o, n) -> n));
				loadMap.put("shiftMasterMap", shiftMasterMap);
			}
			readerMap = (Map<String, String>)loadMap.get("readerMap");
			if (readerMap == null) {
				readerMap = ZkUtil.getTableRecStream(su.getQueryResult("select crdr_id,crdr_mode from cardreader"))
					.collect(Collectors.toMap(c -> c.getString("crdr_id"), c -> c.getString("crdr_mode"), (o, n) -> n));
				loadMap.put("readerMap", readerMap);
			}
		}
		
		public boolean start() throws Exception {
			loadShiftData();
			if (shiftMasterMap.isEmpty())
				throw new Exception("Shift Record Not Found !!!");
			if (!checkSftMask(stdate, enddate))
				throw new Exception("Shift Mask Not Set !!!");
			if (StringUtils.isBlank(attmode))
				attmode = "M";
			attlOldArr = ZkUtil.getTableRecStream(su.getQueryResult("select * from attendance "
									+ " where at_eid = ? and at_date between ? and ? "
									+ " order by at_date", new Wherecl().appendArgument(eid).appendArgument(stdate).appendArgument(enddate))).map(cellColl -> {
				AttlOldItem item = new AttlOldItem();
				Date date = cellColl.getDate("at_date");
				String sftcode = cellColl.getString("at_shiftcode");
				item.old_shiftcode = sftcode;
				item.old_date = date;
				if (StringUtils.isNotBlank(sftcode) && !sftcode.equals("-")) {
					Pair<Integer, Integer> p = shiftMasterMap.get(getAttendShiftCode(sftcode));
					item.old_sttime = p.getLeft();
					item.old_endtime = p.getRight();
				}
				item.old_ot = cellColl.getInt("at_ot");
				item.old_sot = cellColl.getInt("at_sot");
				item.old_dbot = cellColl.getInt("at_dbot");
				item.old_lunchot = cellColl.getInt("at_lunchot");
				item.old_late = cellColl.getInt("at_late");
				item.old_speclate = cellColl.getInt("at_speclate");
				item.old_nowork = cellColl.getInt("at_nowork");
				item.old_reallate = cellColl.getInt("at_reallate");
				item.old_specatt = cellColl.getInt("at_specatt");
				item.old_wktime = cellColl.getInt("at_wktime");
				item.old_flag2 = StringUtils.equals(cellColl.getString("at_flag2"), "Y");
				item.old_flag3 = StringUtils.equals(cellColl.getString("at_flag3"), "Y");
				item.old_flag4 = StringUtils.equals(cellColl.getString("at_flag4"), "Y");
				item.saveItem();
				return item;
			}).toArray(AttlOldItem[]::new);
			if (attlOldArr.length == 0)
				return false;
			Date startDate = attlOldArr[0].old_date;
			Date endDate = attlOldArr[attlOldArr.length - 1].old_date;
			for (int i = 0; i < attlOldArr.length; i++) {
				AttlOldItem item = attlOldArr[i];
				if (item.old_endtime <= 0) {
					if (i > 0)
						item.old_sttime = attlOldArr[i - 1].old_endtime + SHIFT_INTERVAL_GAP - 1440;
					else {
						Date tmpdate = DateUtil.prevday(item.old_date);
						String tmpsftcode = null;
						TableRec tr = su.getQueryResult("select at_shiftcode from attendance where at_eid = ? and at_date = ?", new Wherecl().appendArgument(eid).appendArgument(tmpdate));
						if (tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							tmpsftcode = tr.getFieldString("at_shiftcode");
						}
						Pair<Integer, Integer> p = shiftMasterMap.get(tmpsftcode);
						if (p != null)
							item.old_sttime = p.getRight() + SHIFT_INTERVAL_GAP - 1440;
						else
							item.old_sttime = 0;
					}
					if (item.old_sttime < 0) 
						item.old_sttime = 0;
					if (i < attlOldArr.length - 1) 
						item.old_endtime = attlOldArr[i + 1].old_sttime - SHIFT_INTERVAL_GAP + 1440;
					else {
						Date tmpdate = DateUtil.nextday(attlOldArr[attlOldArr.length - 1].old_date);
						String tmpsftcode = null;
						TableRec tr = su.getQueryResult("select at_shiftcode from attendance where at_eid = ? and at_date = ?", new Wherecl().appendArgument(eid).appendArgument(tmpdate));
						if (tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							tmpsftcode = tr.getFieldString("at_shiftcode");
						}
						Pair<Integer, Integer> p = shiftMasterMap.get(tmpsftcode);
						if (p != null) {
							item.old_endtime = p.getLeft() - SHIFT_INTERVAL_GAP + 1440;
							if (item.old_endtime > MAX_SHIFT_INTERVAL) 
								item.old_endtime = MAX_SHIFT_INTERVAL;
						} else
							item.old_endtime = 1439;
					}
				}
				UniLog.log1("old_date:%s, old_shiftcode:%s, old_sttime:%d, old_endtime:%d, old_flag2:%b, old_flag3:%b, old_flag4:%b", item.old_date, item.old_shiftcode, item.old_sttime, item.old_endtime, item.old_flag2, item.old_flag3, item.old_flag4);
			}

			Date tmpsttime = unionDateTime(attlOldArr[0].old_date, minuteToDate(attlOldArr[0].old_sttime));
			int cc = attlOldArr.length - 1;
			Date tmpendtime = unionDateTime(attlOldArr[cc].old_date, minuteToDate(attlOldArr[cc].old_endtime));
			UniLog.log1("eid:%s, tmpsttime:%s,%d, tmpendtime:%s,%d", eid, tmpsttime, tmpsttime.getTime(), tmpendtime, tmpendtime.getTime());
			su.executeUpdate("update attenddet set atd_date = 0,atd_time = 0 "
					+ " where atd_eid = ? and atd_date between ? and ? and atd_atime not between ? and ?", 
						new Wherecl().appendArgument(eid).appendArgument(startDate)
									.appendArgument(endDate).appendArgument(tmpsttime.getTime() / 1000).appendArgument(tmpendtime.getTime() / 1000));

			AtomicBoolean mainAtypeFound = new AtomicBoolean(), secAtypeFound = new AtomicBoolean();
			attldOldArr = ZkUtil.getTableRecStream(su.getQueryResult("select atd_date, atd_time, atd_flag, atd_atime, atd_atype, atd_adate, attenddet.serial_id sid from attenddet,cardreader" + 
					" where atd_eid = ? and atd_atime between ? and ? and crdr_id = atd_atype" + 
					" order by atd_atime", 
					new Wherecl().appendArgument(eid).appendArgument(tmpsttime.getTime() / 1000).appendArgument(tmpendtime.getTime() / 1000))).map(cellColl -> {
				AttldOldItem item = new AttldOldItem();
				item.oldd_atime = new Date(cellColl.getInt("atd_atime") * 1000L);
				item.oldd_atype = cellColl.getString("atd_atype");
				item.oldd_adate = cellColl.getDate("atd_adate");
				item.oldd_serial = cellColl.getInt("sid");
				item.oldd_date = cellColl.getDate("atd_date");
				item.oldd_time = dateToMinute(new Date(cellColl.getInt("atd_time") * 1000L));
				item.oldd_flag = StringUtils.defaultString(cellColl.getString("atd_flag"));
				if (item.oldd_date.compareTo(DateUtil.minDate) < 0)
					item.oldd_date = DateUtil.zeroDate;
				if (StringUtils.isNotBlank(MAIN_ATYPE) && Objects.equals(item.oldd_atype, MAIN_ATYPE))
					mainAtypeFound.set(true);
				if (StringUtils.isNotBlank(SEC_ATYPE) && Objects.equals(item.oldd_atype, SEC_ATYPE))
					secAtypeFound.set(true);
				//UniLog.log1("oldd_date:%s, oldd_time:%d, oldd_flag:%s, oldd_atime:%s, oldd_atype:%s, oldd_adate:%s, oldd_serial:%d", item.oldd_date, item.oldd_time, item.oldd_flag, item.oldd_atime, item.oldd_atype, item.oldd_adate, item.oldd_serial);
				return item;
			}).toArray(AttldOldItem[]::new);
			if (!secAtypeFound.get())
				SEC_ATYPE = null;
			if (!mainAtypeFound.get()) {
				MAIN_ATYPE = null;
				SEC_ATYPE = null;
			}
			return true;
		}
		
		public void finish() throws Exception {
			attldCurList.sort((o1, o2) -> o1.curd_atime.compareTo(o2.curd_atime));
			
			//{add punch by attlOldArr/attldOldArr}
			attlNormalSetFlag();

			//{check and add attendance}
			attldCurList.stream().filter(item -> !attlCurList.stream().anyMatch(it -> it.cur_date.compareTo(item.curd_date) == 0)).forEach(item -> {
				attlCurList.add(new AttlCurItem() {{
					cur_date = item.curd_date;
				}});
			});
			
			//{set holiday ot, night ot, late etc...
			for (AttlCurItem item : attlCurList) {
				item.cur_ot = 0;
				item.cur_sot = 0;
				item.cur_dbot = 0;
				item.cur_lunchot = 0;
				item.cur_late = 0;
				item.cur_speclate = 0;
				item.cur_nowork = 0;
				item.cur_reallate = 0;
				calot.clearAttendDet();
				attldCurList.stream().filter(it -> it.curd_date.compareTo(item.cur_date) == 0).forEach(it -> {
					calot.addAttendDetWithFlag(it.curd_time, it.curd_flag, "");
				});
				Map<String, Integer> m = calot.getLateTimeOtTime(eid, item.cur_date, 
											Arrays.stream(attlOldArr).filter(t -> t.old_date.equals(item.cur_date)).findFirst().map(t -> t.old_shiftcode).orElse(null));
				//item.cur_flag4 = false;
				if (m != null) {
					item.cur_ot = m.get("holidayotmin");
					item.cur_sot = m.get("nightotmin");
					//item.cur_dbot = outworkottime;
					//item.cur_lunchot = lunchottime;
					item.cur_late = m.get("latemin");
					//item.cur_speclate = speclate;
					//item.cur_specatt = specatt;
					item.cur_nowork = m.get("absencemin");
					item.cur_reallate = m.get("reallate");
					item.cur_wktime = m.get("workmin");
				}
			}

			//{update table of attenddet}
			for (AttldOldItem item : attldOldArr) {
				AttldCurItem item1 = attldCurList.stream()
						.filter(it -> it.curd_atime.compareTo(item.oldd_atime) == 0 && Objects.equals(it.curd_atype, item.oldd_atype) && !it.curd_status)
						.findFirst().orElse(null);
				if (item1 != null) {
					updateTableAttenddet(item, item1);
					item1.curd_status = true;
				} else if (Objects.equals(item.oldd_atype, "99"))
					deleteTableAttenddet(item.oldd_atime, item.oldd_atype);
				else {
					item1 = new AttldCurItem();
					item1.curd_atime = item.oldd_atime;
					item1.curd_atype = item.oldd_atype;
					item1.curd_date = DateUtil.zeroDate;
					item1.curd_time = 0;
					item1.curd_flag = "";
					updateTableAttenddet(item, item1);
					//updateTableAttenddet(item.oldd_serial, DateUtil.zeroDate, 0, "");
				}
			}
			
			//{insert table of attenddet}
			for (AttldCurItem item : attldCurList) {
				if (!item.curd_status) {
					insertTableAttenddet(item.curd_atime, item.curd_adate, item.curd_atype, item.curd_date, item.curd_time, item.curd_flag);
					item.curd_status = true;
				}
			}
			
			//{update table of attendance}
			for (AttlOldItem item : attlOldArr) {
				AttlCurItem it = attlCurList.stream().filter(t -> t.cur_date.compareTo(item.old_date) == 0 && !t.cur_status).findFirst().orElse(null);
				if (it != null) {
					it.cur_flag4 = item.old_flag4;
					//if (StringUtils.isBlank(it.cur_flag4)) 
					//	it.cur_flag4 = item.old_flag4;
					if (it.cur_flag4 != item.old_flag4)
						setEmlvrCompensation(su, eid, it.cur_date, it.cur_flag4);
					//updateTableAttendance(item.old_date, it.cur_ot, it.cur_sot, it.cur_dbot, it.cur_lunchot, it.cur_late, it.cur_speclate, it.cur_specatt, it.cur_nowork, item.old_flag2, item.old_flag3, it.cur_reallate, it.cur_flag4, it.cur_wktime);
					updateTableAttendance(item, it);
					it.cur_status = true;
				} else {
					item.old_ot = 0;
					item.old_sot = 0;
					item.old_dbot = 0;
					item.old_lunchot = 0;
					item.old_late = 0;
					item.old_speclate = 0;
					item.old_nowork = 0;
					item.old_reallate = 0;
					item.old_wktime = 0;
					calot.clearAttendDet();
					Map<String, Integer> m = calot.getLateTimeOtTime(eid, item.old_date, item.old_shiftcode);
					/*boolean tmpflag4 = item.old_flag4;
					if (tmpflag4 != item.old_flag4) {
						//segment setEmlvrCompensation(cur_eid,cur_date,old_flag4)
					}*/
					if (m != null) {
						item.old_ot = m.get("holidayotmin");
						item.old_sot = m.get("nightotmin");
						//item.old_dbot = outworkottime;
						//item.old_lunchot = lunchottime;
						item.old_late = m.get("latemin");
						//item.old_speclate = speclate;
						//item.old_specatt = specatt;
						item.old_nowork = m.get("absencemin");
						item.old_reallate = m.get("reallate");
						item.old_wktime = m.get("workmin");
					}
					//updateTableAttendance(item.old_date, item.old_ot, item.old_sot, item.old_dbot, item.old_lunchot, item.old_late, item.old_speclate, item.old_specatt, item.old_nowork, item.old_flag2, item.old_flag3, item.old_reallate, item.old_flag4, item.old_wktime);
					updateTableAttendance(item);
				}
				//segment set_emlvr_regular(old_eid,old_date,old_wktime,old_shiftcode)
			}
			
			//{insert table of attendance}
			attlCurList.stream().filter(item -> !item.cur_status).forEach(ZkUtil.throwConsumer(item -> {
				if (item.cur_flag4)
					setEmlvrCompensation(su, eid, item.cur_date, item.cur_flag4);
				insertTableAttendance(item.cur_date, item.cur_ot, item.cur_sot, item.cur_dbot, item.cur_lunchot, item.cur_late, item.cur_speclate, item.cur_specatt, item.cur_nowork, item.cur_reallate, item.cur_flag4, item.cur_wktime);
				//segment set_emlvr_regular(cur_eid,cur_date,cur_wktime,cur_shiftcode)
			}));
		}
		
		private void attlAddPunchEx(Date p_atime, String p_type, Date p_adate, String p_flag, Date p_date, int p_time) {
			//{ (p_time - 8*3600) mod 86400 between 0 and 59 }
			AttldCurItem item = attldCurList.stream().filter(t -> t.curd_atime.compareTo(p_atime) == 0 && Objects.equals(t.curd_atype, p_type)).findFirst().orElse(null);
			if (item == null) {
				item = new AttldCurItem();
				attldCurList.add(item);
			}
			item.curd_atime = p_atime;
			item.curd_atype = p_type;
			item.curd_adate = p_adate;
			item.curd_flag = p_flag;
			item.curd_date = p_date;
			item.curd_time = p_time;
			//UniLog.log1("attlAddPunchEx i:%d, isNew:%b, curd_atime:%s, curd_atype:%s, curd_adate:%s, curd_flag:%s, curd_date:%s, curd_time:%d", i, isNew, item.curd_atime, item.curd_atype, item.curd_adate, item.curd_flag, item.curd_date, item.curd_time);
		}

		private void attlAddPunchEx99(AttlOldItem item, int lasttime) {
			if (lasttime <= item.old_endtime) {
				Date cc1 = unionDateTime(item.old_date, minuteToDate(lasttime));
				attlAddPunchEx(cc1, "99", DateUtil.dayBeginning(cc1), "OU", item.old_date, lasttime);
			}
		}
		
		private String getModeFromReader(String p_id) {
			return StringUtils.defaultIfBlank(readerMap.get(p_id), "");
		}
		
		private int findValidShiftStart(AttldOldItem[] attldOldArr, String p_shiftcode, int p_earliest, int p_starttime, int p_endtime, int p_arridx) {
			int j = -1;
			for (int i = p_arridx; i < attldOldArr.length; i++) {
				AttldOldItem item = attldOldArr[i];
				if (item.oldd_time >= p_earliest) {
					if (StringUtils.equalsAny(getModeFromReader(item.oldd_atype), "DX", "DI")) {
						if (item.oldd_time > p_endtime)
							break;
						return i;
					}
				}
			}
			return j;
		}
		
		private int findValidShiftEnd(AttldOldItem[] attldOldArr, String p_shiftcode, int p_sttime, int p_endtime, int p_nextstart, int p_arridx, int p_maxidx) {
			if (StringUtils.equals(p_shiftcode, "-"))
				return p_arridx;
			int cc = 99999;
			int j;
			for (j = p_maxidx - 1; j >= p_arridx; j--) {
				AttldOldItem item = attldOldArr[j];
				if (!StringUtils.equals(item.oldd_atype, "99")) {
					if (StringUtils.equalsAny(getModeFromReader(item.oldd_atype), "DX", "DI"))
						break;
				}
			}
			if (j < p_arridx) 
				j = -1;
			if (p_nextstart < 0) 
				return j;
			j = -1;
			for (int i = p_arridx; i < p_maxidx; i++) {
				AttldOldItem item = attldOldArr[i];
				if (!StringUtils.equals(item.oldd_atype, "99")) {
					if (item.oldd_time > p_sttime) {
						if (StringUtils.equalsAny(getModeFromReader(item.oldd_atype), "DX", "DO")) {
							if (item.oldd_time >= p_nextstart) 
								break;
							int k = Math.abs(p_endtime - item.oldd_time);
							if (k < cc) {
								j = i;
								cc = k;
							}
						}
					}
				}
			}
			return j;
		}
		
		private void attlNormalSetFlag() throws Exception {
			AttldOldItem[] attldOldArr1 = Arrays.stream(attldOldArr)
					.filter(itemd -> (StringUtils.isBlank(MAIN_ATYPE) || StringUtils.equalsAny(itemd.oldd_atype, MAIN_ATYPE, "00")) && !Objects.equals(itemd.oldd_atype, "99"))
					.toArray(AttldOldItem[]::new);
			AttldOldItem[] attldOldArr2;
			if (StringUtils.isNotBlank(SEC_ATYPE))
				attldOldArr2 = Arrays.stream(attldOldArr).filter(itemd -> Objects.equals(itemd.oldd_atype, SEC_ATYPE)).toArray(AttldOldItem[]::new);
			else
				attldOldArr2 = new AttldOldItem[0];
			for (AttlOldItem item : attlOldArr) {
				if (item.old_sttime < 0 || item.old_endtime < 0 || item.old_sttime >= item.old_endtime)
					continue;
				AtomicBoolean found00 = new AtomicBoolean();
				Arrays.stream(attldOldArr1).filter(itemd -> itemd.isValid(item) && Objects.equals(itemd.oldd_atype, "00")).forEach(itemd -> {
					attlAddPunchEx(itemd.oldd_atime, itemd.oldd_atype, itemd.oldd_adate, itemd.oldd_flag, item.old_date, getMinute(itemd, item));
					found00.set(true);
				});
				item.old_flag2 = false;
				if (item.old_flag3 = found00.get())
					continue;
				int firsttime, lasttime = -1;
				int lastIntime = -1, lastOuttime = -1;
				while (lasttime < item.old_endtime) {
					int prevSectionEnd = lasttime;
					if ((firsttime = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, lasttime >= 0 ? -1 : item.old_sttime)) < 0)
						break;
					if ((lasttime = mygetShiftCurSectionEnd(item.old_shiftcode, firsttime, item.old_endtime)) < 0)
						break;
					if (firsttime >= lasttime)
						break;
					AttldOldItem itd = mygetFindStartItem(attldOldArr1, item, prevSectionEnd, firsttime, lastOuttime, lasttime);
					if (itd != null) {
						int tmptime = getMinute(itd, item);
						AttldOldItem itd1;
						if (tmptime > firsttime + (lasttime - firsttime) / 2
								&& (itd1 = mygetFindStartItem(attldOldArr2, item, prevSectionEnd, firsttime, lastOuttime, tmptime)) != null)
							itd = itd1;
						attlAddPunchEx(itd.oldd_atime, itd.oldd_atype, itd.oldd_adate, "IN", item.old_date, lastIntime = getMinute(itd, item));
						if (Objects.equals(attmode, "B")) {
							attlAddPunchEx99(item, lastOuttime = lasttime);
							continue;
						}
					} else if ((itd = mygetFindStartItem(attldOldArr2, item, prevSectionEnd, firsttime, lastOuttime, lasttime)) != null)
						attlAddPunchEx(itd.oldd_atime, itd.oldd_atype, itd.oldd_adate, "IN", item.old_date, lastIntime = getMinute(itd, item));
					if (itd == null)
						continue;
					int nextSectionStart = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, -1);
					if ((itd = mygetFindEndItem(attldOldArr1, item, nextSectionStart, lasttime, lastIntime)) != null
							|| (itd = mygetFindEndItem(attldOldArr2, item, nextSectionStart, lasttime, lastIntime)) != null)
						attlAddPunchEx(itd.oldd_atime, itd.oldd_atype, itd.oldd_adate, "OU", item.old_date, lastOuttime = getMinute(itd, item));
					else if (Objects.equals(attmode, "A"))
						attlAddPunchEx99(item, lastOuttime = lasttime);
					else {
						item.old_flag2 = true;
						break;
					}
				}
				/*int j = 0;
				int cc = item.old_sttime;
				Date tmpsttime = unionDateTime(item.old_date, minuteToDate(cc));
				cc = item.old_endtime;
				Date tmpendtime = unionDateTime(item.old_date, minuteToDate(cc));
				int firsttime = mygetShiftCurSectionStart(item.old_shiftcode, 0, item.old_sttime);
				int lasttime = -1;
				for (; j < attldOldArr1.length; j++) {
					if (attldOldArr1[j].oldd_atime.compareTo(tmpsttime) >= 0) 
						break;
				}
				int l;
				boolean flag3 = false;
				for (l = j; l < attldOldArr1.length; l++) {
					AttldOldItem itemd = attldOldArr1[l];
					if (itemd.oldd_atime.compareTo(tmpendtime) > 0)
						break;
					//itemd.oldd_time = floor((oldd_atime - datetotime(old_date, 0, 0, 0)) / 60)
					itemd.oldd_time = getMinute(itemd, item);
					if (!flag3 && itemd.oldd_atype.equals("00"))
						flag3 = true;
				}
				UniLog.log1("tmpsttime:%s, tmpendtime:%s, j:%d, l:%d", tmpsttime, tmpendtime, j, l);
				item.old_flag2 = false;
				item.old_flag3 = flag3;
				int lastIntime = -1, lastOuttime = -1;
				while (j < l) {
					AttldOldItem item1 = attldOldArr1[j];
					if (item1.oldd_atime.compareTo(tmpendtime) > 0) 
						break;
					if (firsttime < 0) 
						break;
					if (item.old_flag3) {
						if (Objects.equals(item1.oldd_atype, "00")) {
							int tmptime = getMinute(item1, item);
							attlAddPunchEx(item1.oldd_atime, item1.oldd_atype, item1.oldd_adate, item1.oldd_flag, item.old_date, tmptime);
						}
						j = j + 1;
					} else {
						if (j == 0 || item1.oldd_atime.getTime() - attldOldArr1[j - 1].oldd_atime.getTime() >= MIN_PUNCH_INTERVAL) {
							if (lasttime < 0) {
								lasttime = mygetShiftCurSectionEnd(item.old_shiftcode, firsttime, item.old_endtime);
								int k = findValidShiftStart(attldOldArr1, item.old_shiftcode, item.old_sttime, firsttime, lasttime, j);
								if (k >= 0) {
									AttldOldItem item2 = attldOldArr1[k];
									int tmptime = getMinute(item2, item);
									AttldOldItem itd;
									if (lasttime > firsttime && tmptime > firsttime + (lasttime - firsttime) / 2
											&& (itd = mygetFindStartItem(attldOldArr2, item, mygetShiftPrevSectionEnd(item.old_shiftcode, firsttime), firsttime, lastOuttime, tmptime)) != null) {
										attlAddPunchEx(itd.oldd_atime, itd.oldd_atype, itd.oldd_adate, "IN", item.old_date, lastIntime = getMinute(itd, item));
										if (StringUtils.equals(attmode, "B")) {
											attlAddPunchEx99(item, lastOuttime = lasttime);
											firsttime = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, -1);
											lasttime = -1;
										}
									} else {
										attlAddPunchEx(item2.oldd_atime, item2.oldd_atype, item2.oldd_adate, "IN", item.old_date, lastIntime = tmptime);
										if (StringUtils.equals(attmode, "B")) {
											attlAddPunchEx99(item, lastOuttime = lasttime);
											firsttime = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, -1);
											lasttime = -1;
										}
										j = k + 1;
									}
								} else {
									AttldOldItem itd = mygetFindStartItem(attldOldArr2, item, mygetShiftPrevSectionEnd(item.old_shiftcode, firsttime), firsttime, lastOuttime, lasttime);
									if (itd != null) {
										attlAddPunchEx(itd.oldd_atime, itd.oldd_atype, itd.oldd_adate, "IN", item.old_date, lastIntime = getMinute(itd, item));
										if (StringUtils.equals(attmode, "B")) {
											attlAddPunchEx99(item, lastOuttime = lasttime);
											firsttime = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, -1);
											lasttime = -1;
										}
									} else {
										firsttime = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, item.old_sttime);
										lasttime = -1;
									}
								}
							} else {
								int nextSectionStart = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, -1);
								int k = findValidShiftEnd(attldOldArr1, item.old_shiftcode, firsttime, lasttime, nextSectionStart, j, l);
								if (k >= 0) {
									AttldOldItem item2 = attldOldArr1[k];
									attlAddPunchEx(item2.oldd_atime, item2.oldd_atype, item2.oldd_adate, "OU", item.old_date, lastOuttime = getMinute(item2, item));
									j = k + 1;
									firsttime = nextSectionStart;
									lasttime = -1;
								} else {
									AttldOldItem itd = mygetFindEndItem(attldOldArr2, item, nextSectionStart, lasttime, lastIntime);
									if (itd != null) {
										if (getMinute(itd, item) < item.old_endtime)
											attlAddPunchEx(itd.oldd_atime, itd.oldd_atype, itd.oldd_adate, "OU", item.old_date, lastOuttime = getMinute(itd, item));
										firsttime = nextSectionStart;
										lasttime = -1;
									} else {
										if (StringUtils.equals(attmode, "A") && lasttime > lastIntime) {
											attlAddPunchEx99(item, lastOuttime = lasttime);
											firsttime = nextSectionStart;
											lasttime = -1;
										} else {
											item.old_flag2 = true;
											firsttime = nextSectionStart;
											break;
										}
									}
								}
							}
						} else
							j++;
					}
				}
				if (lasttime >= 0) {
					AttldOldItem itd = mygetFindEndItem(attldOldArr2, item, -1, lasttime, lastIntime);
					if (itd != null) {
						if (getMinute(itd, item) < item.old_endtime)
							attlAddPunchEx(itd.oldd_atime, itd.oldd_atype, itd.oldd_adate, "OU", item.old_date, getMinute(itd, item));
						firsttime = mygetShiftCurSectionStart(item.old_shiftcode, lasttime + 1, -1);
						lasttime = -1;
					} else {
						if (StringUtils.equalsAny(attmode, "A", "B") && lasttime > lastIntime)
							attlAddPunchEx99(item, lasttime);
						else
							item.old_flag2 = true;
					}
				}*/
			}
		}
		
		private void loadCurShiftDetail(String p_shiftcode) throws Exception {
			p_shiftcode = getAttendShiftCode(p_shiftcode);
			if (curShiftDetailArr != null && StringUtils.equals(curShiftDetailArr[0].cemsftd_code, p_shiftcode))
				return;
			curShiftDetailArr = Arrays.stream(shiftDetCellMap.getCells(su, p_shiftcode)).filter(c -> Objects.equals(c.getCellString("emsftd_type"), "N")).map(cellColl -> {
				CalotShiftTimeItem item = new CalotShiftTimeItem();
				item.cemsftd_code = cellColl.getString("emsftd_code");
				item.cemsftd_sttime = dateToMinute(new Date(cellColl.getInt("emsftd_sttime") * 1000L));
				item.cemsftd_endtime = dateToMinute(new Date(cellColl.getInt("emsftd_endtime") * 1000L));
				return item;
			}).toArray(CalotShiftTimeItem[]::new);
		}
		
		private int getShiftCurSectionEnd(String p_shiftcode, int p_time) throws Exception {
			loadCurShiftDetail(p_shiftcode);
			for (CalotShiftTimeItem item : curShiftDetailArr) {
				if (p_time <= item.cemsftd_endtime)
					return item.cemsftd_endtime;
			}
			return -1;
		}

		private int getShiftCurSectionStart(String p_shiftcode, int p_time) throws Exception {
			loadCurShiftDetail(p_shiftcode);
			int ss = 0;
			for (CalotShiftTimeItem item : curShiftDetailArr) {
				if (p_time >= ss && p_time <= item.cemsftd_endtime) 
					return item.cemsftd_sttime;
				ss = item.cemsftd_endtime + 1;
			}
			return -1;
		}
		
		private int mygetShiftCurSectionEnd(String p_shiftcode, int p_time, int p_endtime) throws Exception {
			if (StringUtils.equals(p_shiftcode, "-"))
				return p_endtime;
			return getShiftCurSectionEnd(p_shiftcode, p_time);
		}

		private int mygetShiftCurSectionStart(String p_shiftcode, int p_time, int p_sttime) throws Exception {
			if (StringUtils.equals(p_shiftcode, "-"))
				return p_sttime;
			return getShiftCurSectionStart(p_shiftcode, p_time);
		}

		private int mygetShiftPrevSectionEnd(String p_shiftcode, int p_time) throws Exception {
			if (Objects.equals(p_shiftcode, "-"))
				return -1;
			loadCurShiftDetail(p_shiftcode);
			for (CalotShiftTimeItem item : curShiftDetailArr) {
				if (p_time >= item.cemsftd_endtime) 
					return item.cemsftd_endtime;
			}
			return -1;
		}
		
		private AttldOldItem mygetFindEndAfterItem(AttldOldItem[] attldOldArr2, AttlOldItem item, int nextSectionStart, int lasttime, int lastIntime) {
			return Arrays.stream(attldOldArr2).filter(itemd -> {
				if (!itemd.isValid(item))
					return false;
				int min = getMinute(itemd, item);
				int maxMin = nextSectionStart >= 0 ? nextSectionStart : item.old_endtime;
				return min >= lasttime && min <= maxMin && min > lastIntime;
			}).findFirst().orElse(null);
		}

		private AttldOldItem mygetFindEndBeforeItem(AttldOldItem[] attldOldArr2, AttlOldItem item, int lasttime, int lastIntime) {
			return Arrays.stream(attldOldArr2).filter(itemd -> {
				if (!itemd.isValid(item))
					return false;
				int min = getMinute(itemd, item);
				return min < lasttime && min > lastIntime;
			}).max(Comparator.comparing(itemd -> itemd.oldd_atime)).orElse(null);
		}

		private AttldOldItem mygetFindEndItem(AttldOldItem[] attldOldArr2, AttlOldItem item, int nextSectionStart, int lasttime, int lastIntime) {
			AttldOldItem itd = mygetFindEndAfterItem(attldOldArr2, item, nextSectionStart, lasttime, lastIntime);
			if (itd == null)
				itd = mygetFindEndBeforeItem(attldOldArr2, item, lasttime, lastIntime);
			return itd;
		}

		private AttldOldItem mygetFindStartBeforeItem(AttldOldItem[] attldOldArr2, AttlOldItem item, int prevSessionEnd, int starttime, int lastOuttime, int tmptime) {
			return Arrays.stream(attldOldArr2).filter(itemd -> {
				if (!itemd.isValid(item))
					return false;
				int min = getMinute(itemd, item);
				int minMin = prevSessionEnd >= 0 ? prevSessionEnd : item.old_sttime;
				return min <= starttime && min >= minMin && min > lastOuttime && min < tmptime;
			}).max(Comparator.comparing(itemd -> itemd.oldd_atime)).orElse(null);
		}

		private AttldOldItem mygetFindStartAfterItem(AttldOldItem[] attldOldArr2, AttlOldItem item, int starttime, int lastOuttime, int tmptime) {
			return Arrays.stream(attldOldArr2).filter(itemd -> {
				if (!itemd.isValid(item))
					return false;
				int min = getMinute(itemd, item);
				return min > starttime && min > lastOuttime && min < tmptime;
			}).findFirst().orElse(null);
		}

		private AttldOldItem mygetFindStartItem(AttldOldItem[] attldOldArr2, AttlOldItem item, int prevSessionEnd, int starttime, int lastOuttime, int tmptime) {
			AttldOldItem itd = mygetFindStartBeforeItem(attldOldArr2, item, prevSessionEnd, starttime, lastOuttime, tmptime);
			if (itd == null)
				itd = mygetFindStartAfterItem(attldOldArr2, item, starttime, lastOuttime, tmptime);
			return itd;
		}

		
		private void insertTableAttenddet(Date p_atime, Date p_adate, String p_atype, Date p_date, int p_time, String p_flag) throws Exception {
			UniLog.log1("insertTableAttenddet p_atime:%s, p_date:%s, p_atype:%s, p_date:%s, p_time:%d, p_flag:%s", p_atime, p_adate, p_atype, p_date, p_time, p_flag);
			su.executeUpdate("insert into attenddet (atd_eid, atd_atime, atd_adate, atd_atype, atd_date, atd_time, atd_flag) values(?,?,?,?,?,?,?)", 
					new Wherecl().appendArgument(eid)
								.appendArgument(p_atime.getTime() / 1000)
								.appendArgument(p_adate)
								.appendArgument(p_atype)
								.appendArgument(p_date)
								.appendArgument((p_time * 60000 - DateUtil.getGmtOffset()) / 1000)
								.appendArgument(p_flag));
		}
		
		private void updateTableAttenddet(int p_serialid, Date p_date, int p_time, String p_flag) throws Exception {
			UniLog.log1("updateTableAttenddet p_serialid:%d, p_date:%s, p_time:%d, p_flag:%s", p_serialid, p_date, p_time, p_flag);
			su.executeUpdate("update attenddet set atd_date = ?, atd_time = ?, atd_flag = ? where serial_id = ?", 
					new Wherecl().appendArgument(p_date)
								.appendArgument((p_time * 60000 - DateUtil.getGmtOffset()) / 1000)
								.appendArgument(p_flag)
								.appendArgument(p_serialid));
		}

		private void updateTableAttenddet(AttldOldItem item, AttldCurItem item1) throws Exception {
			if (!item1.equals(item))
				updateTableAttenddet(item.oldd_serial, item1.curd_date, item1.curd_time, StringUtils.defaultString(item1.curd_flag));
			//else
			//	UniLog.log1("skip updateTableAttenddet p_serialid:%d, p_date:%s, p_time:%d, p_flag:%s", item.oldd_serial, item1.curd_date, item1.curd_time, StringUtils.defaultString(item1.curd_flag));
		}
		
		private void deleteTableAttenddet(Date p_atime, String p_atype) throws Exception {
			UniLog.log1("deleteTableAttenddet p_atime:%s, p_atype:%s", p_atime, p_atype);
			su.executeUpdate("delete from attenddet where atd_eid = ? and atd_atime = ? and atd_atype = ?", 
					new Wherecl().appendArgument(eid)
								.appendArgument(p_atime.getTime() / 1000)
								.appendArgument(p_atype));
		}
		
		private void insertTableAttendance(Date p_date, int p_ot, int p_sot, int p_dbot, int p_lhot, int p_late, int p_speclate, int p_specatt, int p_nowork, int p_reallate, boolean p_flag4, int p_wktime) throws Exception {
			UniLog.log1("insertTableAttendance p_date:%s", p_date);
			su.executeUpdate("insert into attendance (at_eid, at_date, at_ot, at_dbot, at_sot, at_lunchot, at_late, at_speclate, at_specatt, at_nowork, at_reallate, at_flag4, at_wktime) "
						+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
						new Wherecl().appendArgument(eid).appendArgument(p_date).appendArgument(p_ot).appendArgument(p_dbot).appendArgument(p_sot).appendArgument(p_lhot)
									.appendArgument(p_late).appendArgument(p_speclate).appendArgument(p_specatt).appendArgument(p_nowork).appendArgument(p_reallate)
									.appendArgument(p_flag4 ? "Y" : "N").appendArgument(p_wktime));
		}
		
		private void updateTableAttendance(Date p_date, int p_ot, int p_sot, int p_dbot, int p_lhot, int p_late, int p_speclate, int p_specatt, int p_nowork, boolean p_flag2, boolean p_flag3, int p_reallate, boolean p_flag4, int p_wktime) throws Exception {
			UniLog.log1("updateTableAttendance p_date:%s", p_date);
			su.executeUpdate("update attendance set at_ot = ?, at_dbot = ?, at_sot = ?, at_lunchot = ?, at_late = ?, at_speclate = ?," + 
						" at_specatt = ?, at_nowork = ?, at_flag2 = ?, at_flag3 = ?, at_reallate = ?, at_flag4 = ?, at_wktime = ? " + 
						" where at_eid = ? and at_date = ?", 
						new Wherecl().appendArgument(p_ot).appendArgument(p_dbot).appendArgument(p_sot).appendArgument(p_lhot).appendArgument(p_late).appendArgument(p_speclate)
									.appendArgument(p_specatt).appendArgument(p_nowork).appendArgument(p_flag2 ? "Y" : "N").appendArgument(p_flag3 ? "Y" : "N").appendArgument(p_reallate).appendArgument(p_flag4 ? "Y" : "N").appendArgument(p_wktime)
									.appendArgument(eid).appendArgument(p_date));
		}
		
		private void updateTableAttendance(AttlOldItem item) throws Exception {
			if (item.isDirty())
				updateTableAttendance(item.old_date, item.old_ot, item.old_sot, item.old_dbot, item.old_lunchot, item.old_late, item.old_speclate, item.old_specatt, item.old_nowork, item.old_flag2, item.old_flag3, item.old_reallate, item.old_flag4, item.old_wktime);
			//else
			//	UniLog.log1("skip updateTableAttendance p_date:%s, sot:%d", item.old_date, item.old_sot);
		}

		private void updateTableAttendance(AttlOldItem item, AttlCurItem it) throws Exception {
			if (item.isDirty(it))
				updateTableAttendance(item.old_date, it.cur_ot, it.cur_sot, it.cur_dbot, it.cur_lunchot, it.cur_late, it.cur_speclate, it.cur_specatt, it.cur_nowork, item.old_flag2, item.old_flag3, it.cur_reallate, it.cur_flag4, it.cur_wktime);
			//else
			//	UniLog.log1("skip updateTableAttendance p_date:%s, sot:%d", item.old_date, it.cur_sot);
		}

		private boolean checkSftMask(Date p_sdate, Date p_edate) throws Exception {
			TableRec tr = su.getQueryResult("select count(*) from attendance where at_eid = ? and at_date between ? and ? and at_shiftcode <> '' and at_shiftcode is not null", 
					new Wherecl().appendArgument(eid).appendArgument(p_sdate).appendArgument(p_edate));
			tr.setRecPointer(0);
			int i = (Integer)tr.getField(0);
			return DateUtil.nextday(p_sdate, i - 1).compareTo(p_edate) == 0;
		}
	}

	public static class CalotAttendDetItem {
		public int cada_intime;
		public int cada_outtime;
		public String cada_inatype;
		public String cada_outatype;
	}
	
	private static class CalotShiftTimeItem {
		String cemsftd_code;
		String cemsftd_type;
		int cemsftd_sttime;
		int cemsftd_endtime;
		String cemsftd_name;
		boolean cemsftd_nolate;
		boolean cemsftd_noearly;
		int cemsftd_late1;
		int cemsftd_late2; 
		int cemsftd_absence;
		int cemsftd_workmin;
		int cemsftd_nightot;
		int cemsftd_holidayot;
	}
	
	private static class CalotLeaveOneDayItem {
		String clvo_reason;
		Date clvo_sdate;
		int clvo_leavemin;
	}
			
	private static class CalotLeaveItem {
		Date clv_sdate;
		Date clv_edate;
		int clv_sttime;
		int clv_endtime;
		String clv_ltype;
		String clv_reason;
		int clv_leavemin;
	}
	
	public static class Calot {
		private int MINLATE_THESHOLD = 6;
		private int NIGHTOT_LOWERLIM = 30;
		private boolean NIGHTOT_CROSS_LATE;
		private boolean shiftArrangePubhol;

		private SelectUtil su;
		private String calot_cureid;
		private Date calot_curdate;
		private String calot_cursftcode;
		private List<CalotAttendDetItem> attendDetList = new ArrayList<CalotAttendDetItem>();
		private List<CalotShiftTimeItem> shiftTimeList = new ArrayList<CalotShiftTimeItem>();
		private List<CalotLeaveItem> leaveList = new ArrayList<CalotLeaveItem>();
		private List<CalotLeaveOneDayItem> leaveOneDayList = new ArrayList<CalotLeaveOneDayItem>();
		
		private Region cot_reg_day = new Region();
		private Region cot_ot_day = new Region();
		private Region cot_exot_day = new Region();
		private Region cot_reg_ot_day = new Region();
		private Region cot_reg_ot_exot_day = new Region();
		private Region cot_ot_exot_day = new Region();
		private Region cot_leave = new Region();
		private Region cot_leave_minusattend = new Region();

		private Region cot_attend = new Region();

		private Region cot_holiday = new Region();
		private Region cot_holiday_legal = new Region();
		private Region cot_holiday_nopay = new Region();
		private Region cot_holiday_weekday = new Region();
		private Region cot_holiday_special1 = new Region();
		private Region cot_holiday_special2 = new Region();
		private Region cot_holiday_shiftmask1 = new Region(); //"-"
		private Region cot_holiday_shiftmask2 = new Region();
		private Region cot_lv_ho = new Region();
		private Region cot_payholiday = new Region();
		
		private Date stdate, enddate;
		private Map<Date, CellCollection> holidayCellMap;
		private CellCollection[] leaveCells;
		private Set<Date> leaveDetDateList;
		private ShiftDetCellMap shiftDetCellMap;
		
		public Calot(SelectUtil su, Date stdate, Date enddate, boolean shiftArrangePubhol, int minlate_theshold, int nightot_lowerlim, boolean nightot_cross_late, ShiftDetCellMap shiftDetCellMap) throws Exception {
			this.su = su;
			if (minlate_theshold > 0)
				MINLATE_THESHOLD = minlate_theshold;
			if (nightot_lowerlim > 0)
				NIGHTOT_LOWERLIM = nightot_lowerlim;
			NIGHTOT_CROSS_LATE = nightot_cross_late;
			this.shiftArrangePubhol = shiftArrangePubhol;
			UniLog.log1("MINLATE_THESHOLD:%d, NIGHTOT_LOWERLIM:%d, shiftArrangePubhol:%b", MINLATE_THESHOLD, NIGHTOT_LOWERLIM, shiftArrangePubhol);
			
			this.shiftDetCellMap = shiftDetCellMap;
			this.stdate = stdate;
			this.enddate = enddate;
			TableRec tr = su.getQueryResult("select * from calendar where cd_date between ? and ?", new Wherecl().appendArgument(stdate).appendArgument(enddate));
			holidayCellMap = ZkUtil.getTableRecStream(tr).collect(Collectors.toMap(o -> o.getDate("cd_date"), o -> o));
		}
		
		public void init(String p_eid, Date p_date, String p_sftcode) throws Exception {
			if (!Objects.equals(p_eid, calot_cureid)) {
				TableRec tr = su.getQueryResult("select * from leave where lv_eid = ? and lv_sdate <= ? and lv_edate >= ? order by lv_sdate, lv_edate, lv_reason, lv_sttime", 
						new Wherecl().appendArgument(p_eid).appendArgument(enddate).appendArgument(stdate));
				leaveCells = ZkUtil.getTableRecStream(tr).toArray(CellCollection[]::new);

				TableRec tr1 = su.getQueryResult("select lvd_attdate from leavedet where lvd_eid = ? and lvd_attdate between ? and ?", new Wherecl().appendArgument(p_eid).appendArgument(stdate).appendArgument(enddate));
				leaveDetDateList = ZkUtil.getTableRecStream(tr1, i -> tr1.getFieldDate("lvd_attdate", i)).collect(Collectors.toSet());
			}
			calot_cureid = p_eid;
			calot_curdate = p_date;
			calot_cursftcode = p_sftcode;
		}
		
		public void clearAttendDet() {
			attendDetList.clear();
		}
		
		public void addAttendDet(final int intime, final int outtime) {
			attendDetList.add(new CalotAttendDetItem() {{
				cada_intime = intime;
				cada_outtime = outtime;
			}});
		}
		
		public static void addAttendDetWithFlagIntoList(final int p_time, String p_flag, final String p_atype, List<CalotAttendDetItem> list) {
			if (StringUtils.equals(p_flag, "IN")) {
				list.add(new CalotAttendDetItem() {{
					cada_intime = p_time;
					cada_outtime = -1;
					cada_inatype = p_atype;
				}});
			} else if (StringUtils.equals(p_flag, "OU")) {
				if (list.isEmpty()) {
					list.add(new CalotAttendDetItem() {{
						cada_intime = -1;
						cada_outtime = p_time;
						cada_outatype = p_atype;
					}});
				} else {
					CalotAttendDetItem item = list.get(list.size() - 1);
					if (item.cada_outtime < 0) {
						item.cada_outtime = p_time;
						item.cada_outatype = p_atype;
					} else {
						item.cada_intime = -1;
						item.cada_outtime = p_time;
						item.cada_outatype = p_atype;
					}
				}
			}
		}
		
		public void addAttendDetWithFlag(int p_time, String p_flag, String p_atype) {
			//UniLog.log1("p_time:%d, p_flag:%s, p_atype:%s", p_time, p_flag, p_atype);
			addAttendDetWithFlagIntoList(p_time, p_flag, p_atype, attendDetList);
		}
		
		public void sortAttendDet() {
			attendDetList.sort((o1, o2) -> {
				if (o2.cada_outtime >= 0) {
					if (o2.cada_outtime > o1.cada_intime && o2.cada_outtime > o1.cada_outtime)
						return 0;
					else
						return 1;
				} else {
					if (o2.cada_intime > o1.cada_intime && o2.cada_intime > o1.cada_outtime)
						return 0;
					else
						return 1;
				}
			});
		}
		
		public boolean checkAttendDet(int p_intime, int p_outtime) {
			return p_intime >= 0 && p_outtime >= 0;
		}
		
		public void readShift() throws Exception {
			cot_reg_day.clear();
			cot_ot_day.clear();
			cot_exot_day.clear();
			cot_reg_ot_day.clear();
			cot_reg_ot_exot_day.clear();
			boolean flag = false;
			if (!shiftTimeList.isEmpty()) {
				if (StringUtils.equals(shiftTimeList.get(0).cemsftd_code, getAttendShiftCode(calot_cursftcode))) {
					for (CalotShiftTimeItem item : shiftTimeList) {
						item.cemsftd_late1 = 0;
						item.cemsftd_late2 = 0;
						item.cemsftd_absence = 0;
						item.cemsftd_workmin = 0;
						item.cemsftd_nightot = 0;
						item.cemsftd_holidayot = 0;
					}
					flag = true;
				}	
			}
			if (!flag) {
				shiftTimeList.clear();
				String shiftcode = getAttendShiftCode(calot_cursftcode);
				for (CellCollection cellColl : shiftDetCellMap.getCells(su, shiftcode)) {
					shiftTimeList.add(new CalotShiftTimeItem() {{
						cemsftd_code = cellColl.getString("emsftd_code");
						cemsftd_type = cellColl.getString("emsftd_type");
						cemsftd_sttime = dateToMinute(new Date(cellColl.getCellInt("emsftd_sttime") * 1000L));
						cemsftd_endtime = dateToMinute(new Date(cellColl.getCellInt("emsftd_endtime") * 1000L));
						cemsftd_name = cellColl.getString("emsftd_name");
						cemsftd_nolate = StringUtils.equals(cellColl.getString("emsftd_nolate"), "Y");
						cemsftd_noearly = StringUtils.equals(cellColl.getString("emsftd_noearly"), "Y");
					}});
				}
			}
			for (CalotShiftTimeItem item : shiftTimeList) {
				//UniLog.log1("cemsftd_code:%s, cemsftd_type:%s, cemsftd_sttime:%d, cemsftd_endtime:%d", item.cemsftd_code, item.cemsftd_type, item.cemsftd_sttime, item.cemsftd_endtime);
				Region cot_tmptime = new Region(item.cemsftd_sttime, item.cemsftd_endtime);
				if (StringUtils.equals(item.cemsftd_type, "N"))
					cot_reg_day = cot_reg_day.union(cot_tmptime);
				else if (StringUtils.equals(item.cemsftd_type, "O"))
					cot_ot_day = cot_ot_day.union(cot_tmptime);
				else if (StringUtils.equals(item.cemsftd_type, "X"))
					cot_exot_day = cot_exot_day.union(cot_tmptime);
			}
			cot_reg_ot_day = cot_reg_day.union(cot_ot_day);
			cot_reg_ot_exot_day = cot_reg_ot_day.union(cot_exot_day);
			cot_ot_exot_day = cot_ot_day.union(cot_exot_day);
			//UniLog.log1("cot_reg_day:%s, cot_ot_day:%s, cot_exot_day:%s, cot_reg_ot_day:%s, cot_reg_ot_exot_day:%s, cot_ot_exot_day:%s", cot_reg_day, cot_ot_day, cot_exot_day, cot_reg_ot_day, cot_reg_ot_exot_day, cot_ot_exot_day);
		}
		
		public void readStatus2() throws Exception {
			readShift();
			readAttend();
			setShiftDetail();
		}
		
		public void readStatus() throws Exception {
			readShift();
			readHoliday();
			readAttend();
			readLeave();
			setShiftDetail();
		}
		
		public Map<String, Integer> getLateTimeOtTime(String p_eid, Date p_date, String shiftcode) throws Exception {
			int latemin = 0;
			int workmin = 0;
			int absencemin = 0;
			int nightotmin = 0;
			int holidayotmin = 0;
			int reallate = 0;
			if (StringUtils.isBlank(shiftcode))
				shiftcode = getSftCode(p_eid, p_date);
			if (StringUtils.isBlank(shiftcode))
				return null;
			init(p_eid, p_date, shiftcode);
			//UniLog.log1("p_date:%s, tmpsftcode:%s, NIGHTOT_CROSS_LATE:%b", p_date, tmpsftcode, NIGHTOT_CROSS_LATE);
			readStatus();
			Map<String, Integer> m = getLateTime();
			latemin = m.get("late");
			workmin = getWorkTime();
			absencemin = getAbsenceTime();
			nightotmin = getNightOtTime();
			holidayotmin = getHolidayOtTime();
			reallate = m.get("late1");
			if (NIGHTOT_CROSS_LATE) {
				/*int min = nightotmin - latemin;
				if (min >= 0) {
					nightotmin = min;
					latemin = 0;
				} else {
					nightotmin = 0;
					latemin = -min;
				}
				reallate = latemin;*/
				nightotmin -= latemin;
				latemin = reallate = 0;
				if (nightotmin > 0 && nightotmin < NIGHTOT_LOWERLIM)
					nightotmin = 0;
				else if (nightotmin < 0 && -nightotmin < MINLATE_THESHOLD)
					nightotmin = 0;
			} else {
				if (latemin < MINLATE_THESHOLD) 
					latemin = 0;
				if (nightotmin < NIGHTOT_LOWERLIM)
					nightotmin = 0;
			}
			updateLeaveDet();
			return MapUtil.of("latemin", latemin, "workmin", workmin, "absencemin", absencemin, 
					"nightotmin", nightotmin, "holidayotmin", holidayotmin, "reallate", reallate);
		}
		
		private Map<String, Integer> getLateTime() {
			int late1 = 0;
			int late2 = 0;
			for (CalotShiftTimeItem item : shiftTimeList) {
				if (!item.cemsftd_nolate)
					late1 += item.cemsftd_late1;
				if (!item.cemsftd_noearly)
					late2 += item.cemsftd_late2;
			}
			return MapUtil.of("late1", late1, "late2", late2, "late", late1 + late2);
		}
		
		public int getWorkTime() {
			int tmpworkmin = 0;
			for (CalotShiftTimeItem item : shiftTimeList)
				tmpworkmin += item.cemsftd_workmin;
			return tmpworkmin;
		}
		
		private int getAbsenceTime() {
			int tmpabsencemin = 0;
			for (CalotShiftTimeItem item : shiftTimeList)
				tmpabsencemin += item.cemsftd_absence;
			return tmpabsencemin;
		}
		
		private int getNightOtTime() {
			int tmpnightotmin = 0;
			for (CalotShiftTimeItem item : shiftTimeList)
				tmpnightotmin += item.cemsftd_nightot;
			return tmpnightotmin;
		}
		
		private int getHolidayOtTime() {
			int tmpholidayotmin = 0;
			for (CalotShiftTimeItem item : shiftTimeList)
				tmpholidayotmin += item.cemsftd_holidayot;
			return tmpholidayotmin;
		}
		
		private String getSftCode(String p_eid, Date p_date) throws Exception {
			String sftcode = "";
			TableRec tr = su.getQueryResult("select at_shiftcode from attendance where at_eid = ? and at_date = ?", 
								new Wherecl().appendArgument(p_eid).appendArgument(p_date));
			if (tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				sftcode = tr.getFieldString("at_shiftcode");
			}
			return sftcode;
		}
		
		private void setShiftDetail() {
			cot_lv_ho = cot_leave.union(cot_holiday_legal);
			cot_lv_ho = cot_lv_ho.union(cot_holiday_shiftmask1);
			cot_payholiday = cot_holiday_legal.union(cot_holiday_shiftmask1);
			Region cot_tmptime;
			for (CalotShiftTimeItem item : shiftTimeList) {
				Region cot_tmpregtime = new Region(item.cemsftd_sttime, item.cemsftd_endtime);
				//UniLog.log1("cot_tmpregtime:%s", cot_tmpregtime);
				if (StringUtils.equals(item.cemsftd_type, "N")) {
					cot_tmptime = cot_tmpregtime.minus(cot_attend);
					//UniLog.log1("%s, %s, cot_tmptime:%s, cot_tmpregtime:%s, cot_attend:%s, cot_lv_ho:%s, cot_payholiday:%s, cot_tmptime_area:%d, cot_tmpregtime_area:%d", calot_curdate, item.cemsftd_code, cot_tmptime, cot_tmpregtime, cot_attend, cot_lv_ho, cot_payholiday, cot_tmptime.area(), cot_tmpregtime.area());
					if (cot_tmptime.area() < cot_tmpregtime.area()) {
						//{late}
						Region cot_late = cot_tmptime;
						cot_late = cot_late.minus(cot_lv_ho);
						if (cot_late.area() > 0) {
							for (CalotAttendDetItem atdItem : attendDetList) {
								if (checkAttendDet(atdItem.cada_intime, atdItem.cada_outtime)) {
									Region cot_tmpatttime = new Region(atdItem.cada_intime, atdItem.cada_outtime);
									Region cot_tmptime2 = cot_tmpregtime.intersect(cot_tmpatttime);
									if (cot_tmptime2.area() > 0) {
										if (item.cemsftd_sttime < atdItem.cada_intime) {
											Region cot_tmptime3 = new Region(item.cemsftd_sttime, atdItem.cada_intime);
											cot_tmptime3 = cot_tmptime3.minus(cot_lv_ho); //late
											Region cot_tmptime4 = cot_late.minus(cot_tmptime3); //leave early
											item.cemsftd_late1 = cot_tmptime3.area();
											item.cemsftd_late2 = cot_tmptime4.area();
										} else
											item.cemsftd_late2 = cot_late.area();
									}
								}
							}
						}
						if (cot_late.area() > 0 && item.cemsftd_late1 == 0 && item.cemsftd_late2 == 0)
							item.cemsftd_late2 = cot_late.area();
						/*if (item.cemsftd_late1 < MINLATE_THESHOLD) 
							item.cemsftd_late1 = 0;
						if (item.cemsftd_late2 < MINLATE_THESHOLD) 
							item.cemsftd_late2 = 0;*/
					} else {
						//{absence}	
						cot_tmptime = cot_tmptime.minus(cot_lv_ho);
						item.cemsftd_absence = cot_tmptime.area();
					}
					//{workmin}
					cot_tmptime = cot_tmpregtime.intersect(cot_attend);
					if (cot_tmptime.area() > 0) {
						item.cemsftd_workmin = cot_tmptime.area();
						cot_tmptime = cot_tmptime.intersect(cot_payholiday);
						item.cemsftd_holidayot = cot_tmptime.area();
					}
				} else if (StringUtils.equals(item.cemsftd_type, "O")) {
					//{nightot}
					cot_tmptime = cot_tmpregtime.intersect(cot_attend);
					//if (cot_tmptime.area() >= NIGHTOT_LOWERLIM)
						item.cemsftd_nightot = cot_tmptime.area();
				}
			}
		}
		
		private void readAttend() {
			cot_attend.clear();
			for (CalotAttendDetItem item : attendDetList) {
				if (checkAttendDet(item.cada_intime, item.cada_outtime)) {
					cot_attend = cot_attend.union(new Region(item.cada_intime, item.cada_outtime));
				}
			}
			//UniLog.log1("cot_attend:%s", cot_attend);
		}
		
		private void readHoliday() throws Exception {
			cot_holiday.clear();
			cot_holiday_legal.clear();
			cot_holiday_nopay.clear();
			cot_holiday_weekday.clear();
			cot_holiday_special1.clear();
			cot_holiday_special2.clear();
			cot_holiday_shiftmask1.clear();
			cot_holiday_shiftmask2.clear();
			CellCollection cc = holidayCellMap.get(calot_curdate);
			if (cc != null) {
				String cd_holstatus = cc.getString("cd_holstatus");
				//UniLog.log1("calot_curdate:%s, cd_holstatus:%s", calot_curdate, cd_holstatus);
				if (StringUtils.equals(cd_holstatus, "P")) {
					if (shiftArrangePubhol)
						cot_holiday_legal.add(0, 9999);
					else
						cot_holiday_nopay.add(0, 9999);
				} else if (StringUtils.equals(cd_holstatus, "U"))
					cot_holiday_nopay.add(0, 9999);
			}
			if (StringUtils.startsWith(calot_cursftcode, "-"))
				cot_holiday_shiftmask1.add(0, 9999);
			cot_holiday = cot_holiday_legal.union(cot_holiday_nopay);
			cot_holiday = cot_holiday.union(cot_holiday_weekday);
			cot_holiday = cot_holiday.union(cot_holiday_special1);
			cot_holiday = cot_holiday.union(cot_holiday_special2);
			cot_holiday = cot_holiday.union(cot_holiday_shiftmask1);
			cot_holiday = cot_holiday.union(cot_holiday_shiftmask2);
			//UniLog.log1("calot_cursftcode:%s, cot_holiday:%s", calot_cursftcode, cot_holiday);
		}

		private void readLeave() throws Exception {
			cot_leave.clear();
			cot_leave_minusattend.clear();
			leaveList.clear();
			Arrays.stream(leaveCells).filter(o -> o.getDate("lv_sdate").compareTo(calot_curdate) <= 0 && o.getDate("lv_edate").compareTo(calot_curdate) >= 0).forEach(o -> {
				CalotLeaveItem item;
				leaveList.add(item = new CalotLeaveItem() {{
					clv_reason = o.getString("lv_reason");
					clv_ltype = o.getString("lv_ltype");
					clv_sdate = o.getDate("lv_sdate");
					clv_edate = o.getDate("lv_edate");
					clv_sttime = dateToMinute(new Date(o.getInt("lv_sttime") * 1000L));
					clv_endtime = dateToMinute(new Date(o.getInt("lv_endtime") * 1000L));
				}});
				UniLog.log1("leave reason:%s, type:%s, sdate:%s, edate:%s, stime:%d, etime:%d", item.clv_reason, item.clv_ltype, item.clv_sdate, item.clv_edate, item.clv_sttime, item.clv_endtime);
			});
			for (CalotLeaveItem item : leaveList) {
				Region cot_tmptime = new Region();
				if (item.clv_sttime < 0)
					cot_tmptime.add(0, 9999);
				else {
					if (item.clv_sdate.compareTo(item.clv_edate) == 0)
						cot_tmptime.add(item.clv_sttime, item.clv_endtime);
					else {
						if (calot_curdate.compareTo(item.clv_sdate) == 0)
							cot_tmptime.add(item.clv_sttime, item.clv_sttime + 9999);
						else if (calot_curdate.compareTo(item.clv_edate) == 0)
							cot_tmptime.add(0, item.clv_endtime);
						else
							cot_tmptime.add(0, 9999);
					}
				}
				Region cot_tmptime1 = cot_tmptime.intersect(cot_reg_day);
				cot_tmptime1 = cot_tmptime1.minus(cot_holiday);
				cot_tmptime1 = cot_tmptime1.minus(cot_attend);
				item.clv_leavemin = cot_tmptime1.area();

				cot_leave = cot_leave.union(cot_tmptime);
			}
			cot_leave = cot_leave.intersect(cot_reg_ot_exot_day);
			cot_leave_minusattend = cot_leave.minus(cot_attend);
			//UniLog.log1("cot_leave:%s, cot_leave_minusattend:%s", cot_leave, cot_leave_minusattend);
		}
		
		private void updateLeaveDet() throws Exception {
			leaveOneDayList.clear();
			for (final CalotLeaveItem item : leaveList) {
				if (item.clv_leavemin > 0) {
					CalotLeaveOneDayItem item1 = leaveOneDayList.stream().filter(t -> Objects.equals(item.clv_reason, t.clvo_reason) && item.clv_sdate.compareTo(t.clvo_sdate) == 0).findFirst().orElse(null);
					if (item1 != null)
						item1.clvo_leavemin += item.clv_leavemin;
					else {
						leaveOneDayList.add(new CalotLeaveOneDayItem() {{
							clvo_reason = item.clv_reason;
							clvo_sdate = item.clv_sdate;
							clvo_leavemin = item.clv_leavemin;
						}});
					}
				}
			}
			if (leaveDetDateList.contains(calot_curdate)) {
				UniLog.log1("delete from leavedet %s", calot_curdate);
				su.executeUpdate("delete from leavedet where lvd_eid = ? and lvd_attdate = ?", new Wherecl().appendArgument(calot_cureid).appendArgument(calot_curdate));
			}
			for (CalotLeaveOneDayItem item : leaveOneDayList) {
				double clvo_nday;
				if (item.clvo_leavemin > LEAVETIME_FOR_WHOLE_DAY) 
					clvo_nday = 1;
				else 
					clvo_nday = 0.5;
				UniLog.log1("insert into leavedet date:%s,reason:%s,leavemin:%d,clvo_nday:%f", calot_curdate, item.clvo_reason, item.clvo_leavemin, clvo_nday);
				su.executeUpdate("insert into leavedet (lvd_eid, lvd_sdate, lvd_reason, lvd_attdate, lvd_nummin, lvd_nday) values(?, ?, ?, ?, ?, ?)", 
							new Wherecl().appendArgument(calot_cureid).appendArgument(item.clvo_sdate).appendArgument(item.clvo_reason)
										.appendArgument(calot_curdate).appendArgument(item.clvo_leavemin).appendArgument(clvo_nday));
			}
		}
	}
	
	private static class ShiftDetCellMap extends HashMap<String, CellCollection[]> {
		private static final long serialVersionUID = 1L;

		public CellCollection[] getCells(SelectUtil su, String shiftcode) throws Exception {
			if (Objects.equals(shiftcode, "-"))
				return new CellCollection[0];
			CellCollection[] ccs = get(shiftcode);
			if (ccs == null) {
				TableRec tr = su.getQueryResult("select * from emshiftdetail where emsftd_code = ? order by emsftd_sttime", new Wherecl().appendArgument(shiftcode));
				ccs = ZkUtil.getTableRecStream(tr).toArray(CellCollection[]::new);
				put(shiftcode, ccs);
			}
			return ccs;
		}
	}
	
	private static class Region {
		private RangeSet<Integer> rs = TreeRangeSet.create();
		
		public Region() {
		}
		
		public Region(int start, int end) { //range [start,end)
			add(start, end);
		}

		public void clear() {
			rs.clear();
		}
		
		public void add(int start, int end) { //range [start,end)
			rs.add(Range.openClosed(start, end >= start ? end : start));
		}
		
		public Region clone() {
			Region region1 = new Region();
			region1.rs.addAll(rs);
			return region1;
		}
		
		public Region union(Region region) {
			Region region1 = clone();
			region1.rs.addAll(region.rs);
			return region1;
		}
		
		public Region intersect(Region region) {
			Region region1 = new Region();
			if (!region.rs.isEmpty()) {
				for (Range<Integer> r : region.rs.asRanges())
					region1.rs.addAll(rs.subRangeSet(r));
			}
			return region1;
		}
		
		public Region minus(Region region) {
			Region region1 = clone();
			region1.rs.removeAll(region.rs);
			return region1;
		}
		
		public int area() {
			int total = 0;
			for (Range<Integer> r : rs.asRanges())
				total += r.upperEndpoint() - r.lowerEndpoint();
			return total;
		}
		
		public String toString() {
			return rs.toString();
		}
	}

}
