package com.uniinformation.jxapp.erpv4ext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listbox;

import com.google.common.collect.Lists;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class LeaveApplication extends JxZkBiBase {
	private static final int MINUTES_PER_LEAVEUNIT = 45;
	private static final int DAYS_PER_YEAR = 365;
	private static final int YEAR_TO_IGNORE_MAX_CARRYFORWARD = 2047;
	public static final int LEAVE_EXPIRE_YEAR = 2;
	public static final int LEAVEUNIT_PER_DAY = 10;
	public static final int LEAVEUNIT_PER_HALFDAY = 5;
	public static final int LEAVEUNIT_MAX_CARRYFORWARD = 40;
	private static final int LEAVEUNIT_MIN_INCREMENT = 1;
	private static final double LEAVEUNIT_INCREMENT_ROUNDING = 0.5;

	public static final Date START_TIME_IN_DAY = new Date(-DateUtil.getGmtOffset());
	public static final Date END_TIME_IN_DAY = new Date(48 * 3600000 - DateUtil.getGmtOffset());
	public static final Date MAX_TIME_IN_DAY = new Date(32 * 3600000 - DateUtil.getGmtOffset());
	public static final Date MAX_DATE = DateUtil.dateTimeStrToDate("2047/12/31");
	public static final int MAX_MINUTE_IN_DAY = 24 * 60;

	LeaveCal leaveCal;

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		/*final Listbox lb = (Listbox)jxAdd("list_erpv4ext_LeaveApplicationDet").getNativeObject();
		lb.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
			@Override
			public void onEvent(AfterSizeEvent event) throws Exception {
				int desktopHeight = sessionHelper.getDesktopHeight();
				UniLog.log1("list_erpv4ext_LeaveApplicationDet event:%s, height:%d, desktopHeight:%d", event, event.getHeight(), desktopHeight);
				int maxHeight = Math.max((int)(desktopHeight * 0.8 - 200 - (sessionHelper.isMobile() ? 56 : 0)), 100);
				if (event.getHeight() >= maxHeight - 32)
					lb.setHeight(maxHeight + "px");
			}
		});*/
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		leaveCal = new LeaveCal(p_br, p_br.getCurrentCollection());
		//lvUtilList.clear();
		try {
			String eid = getBr().getCellString("em_eid");
			Date almaxd1 = DateUtil.yearEnd(DateUtil.today());
			leaveCal.clearLvUtilList("");
			leaveCal.genCalLeave("AL", eid, almaxd1);
			leaveCal.genCalLeave("CL", eid, almaxd1);
			Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.LeaveApplicationDet");
			for (BiCellCollection cc : recs) {
				String ltype = cc.getString("lv_ltype");
				String stfd = cc.getString("lv_stfd");
				String enfd = cc.getString("lv_enfd");
				int nlunit = cc.getInt("lv_leaveunit");
				cc.getCell("lvx_nopaid").set(StringUtils.equals(ltype, "No Paid"));
				cc.getCell("lvx_stfullday").set(StringUtils.equals(stfd, "F"));
				cc.getCell("lvx_sthalfday").set(StringUtils.equals(stfd, "H"));
				cc.getCell("lvx_enfullday").set(StringUtils.equals(enfd, "F"));
				cc.getCell("lvx_enhalfday").set(StringUtils.equals(enfd, "H"));
				cc.getCell("lvx_nldays").set(NumberUtils.toDouble(getLeaveUnit2LvStr(nlunit)));
				setupOnOffDetailCell(cc);
			}
			calLeaveRemain();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd, BiResult sr, CellCollection cl, int p_insIdx) {
		ReturnMsg rtn = super.beforeAddLink(fd, sr, cl, p_insIdx);
		if (rtn != null && !rtn.getStatus()) return rtn;
		UniLog.log1("beforeAddLink fdName:%s, insIdx:%d", fd.getName(), p_insIdx);
		if (fd != null && StringUtils.equalsAny(fd.getName(), "list_erpv4ext_LeaveApplicationDet", "btadd_list_erpv4ext_LeaveApplicationDet")) {
			try {
				setupOnOffDetailCell(cl);
			} catch (Exception ex) {
				rtn = new ReturnMsg(false, -1, ex.getMessage());
			}
		}
		return rtn;
	}

	@Override
	protected void afterDeleteLink(BiResult sr, int idx) {
		super.afterDeleteLink(sr, idx);
		try {
			calLeaveRemain();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
	protected void afterUnDeleteLink(BiResult sr,int idx) {
		super.afterUnDeleteLink(sr, idx);
		try {
			calLeaveRemain();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}


	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			Vector<BiCellCollection> recs = br.getSubLinkResult("erpv4ext.LeaveApplicationDet");
			for (BiCellCollection cc : recs) {
				String reason = cc.getString("lv_reason");
				Date startDate = cc.getDate("lv_sdate");
				Date endDate = cc.getDate("lv_edate");
				Date startTime = cc.getDate("lv_sttime");
				Date endTime = cc.getDate("lv_endtime");
				String stfd = cc.getString("lv_stfd");
				UniLog.log1("startDate:%s, endDate:%s, startTime:%s, endTime:%s, stfd:%s", startDate, endDate, startTime, endTime, stfd);
				if (StringUtils.isBlank(reason))
					return new ReturnMsg(false, "Type cannot be empty", true);
				if (DateUtil.isDateNull(startDate))
					return new ReturnMsg(false, "Start Date cannot be empty", true);
				if (DateUtil.isDateNull(endDate))
					return new ReturnMsg(false, "End Date cannot be empty", true);
				if (DateUtil.isDateNull(startTime))
					return new ReturnMsg(false, "Start Time cannot be empty", true);
				if (DateUtil.isDateNull(endTime))
					return new ReturnMsg(false, "End Time cannot be empty", true);
				if (startDate.compareTo(endDate) > 0)
					return new ReturnMsg(false, "Start Date cannot be more than End Date", true);
				if (startTime.compareTo(endTime) > 0)
					return new ReturnMsg(false, "Start Time must be less than End Time", true);
				if (startTime.compareTo(endTime) == 0 && (startTime.compareTo(START_TIME_IN_DAY) != 0 || endTime.compareTo(START_TIME_IN_DAY) != 0))
					return new ReturnMsg(false, "Start Time must be less than End Time", true);
				if ((endTime.getTime() - startTime.getTime()) % 3600000 != 0)
					return new ReturnMsg(false, "Should be multiple of hours", true);
				if (!StringUtils.equalsAny(stfd, "F", "H") && endTime.compareTo(MAX_TIME_IN_DAY) >= 0)
					return new ReturnMsg(false, "Invalid time", true);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}


	private void setupOnOffDetailCell(final CellCollection cl) throws Exception {
		if (!cl.getBoolean("lv_autogen")) {
			cl.getCell("lv_reason").setMode(Cell.VMODE_NORMAL);
			cl.getCell("lvx_nopaid").setMode(Cell.VMODE_NORMAL);
			cl.getCell("lv_sdate").setMode(Cell.VMODE_NORMAL);
			cl.getCell("lvx_stfullday").setMode(Cell.VMODE_NORMAL);
			cl.getCell("lvx_sthalfday").setMode(Cell.VMODE_NORMAL);
			if (!cl.getBoolean("lvx_stfullday") && !cl.getBoolean("lvx_sthalfday")) {
				cl.getCell("lv_sttime").setMode(Cell.VMODE_NORMAL);
				cl.getCell("lv_endtime").setMode(Cell.VMODE_NORMAL);
				//cl.getCell("lv_edate").setMode(Cell.VMODE_DISPONLY);
				//cl.getCell("lvx_enfullday").setMode(Cell.VMODE_DISPONLY);
				//cl.getCell("lvx_enhalfday").setMode(Cell.VMODE_DISPONLY);
				cl.getCell("lv_edate").setMode(Cell.VMODE_HIDDEN);
				cl.getCell("lvx_enfullday").setMode(Cell.VMODE_HIDDEN);
				cl.getCell("lvx_enhalfday").setMode(Cell.VMODE_HIDDEN);

				Date startTime = cl.getDate("lv_sttime");
				Date endTime = cl.getDate("lv_endtime");
				if (!DateUtil.isDateNull(startTime) && !DateUtil.isDateNull(endTime) && startTime.compareTo(START_TIME_IN_DAY) == 0 && endTime.compareTo(END_TIME_IN_DAY) == 0) {
					cl.getCell("lv_sttime").set((Date)null);
					cl.getCell("lv_endtime").set((Date)null);
				}
				cl.getCell("lv_edate").set(cl.getCell("lv_sdate").getDate());
				cl.getCell("lvx_enfullday").set(false);
				cl.getCell("lvx_enhalfday").set(false);

				if (!DateUtil.isDateNull(startTime) && !DateUtil.isDateNull(endTime) && startTime.compareTo(START_TIME_IN_DAY) == 0 && endTime.compareTo(START_TIME_IN_DAY) == 0)
					cl.getCell("lvx_nldays").setMode(Cell.VMODE_NORMAL);
				else
					cl.getCell("lvx_nldays").setMode(Cell.VMODE_DISPONLY);
			} else {
				//cl.getCell("lv_sttime").setMode(Cell.VMODE_DISPONLY);
				//cl.getCell("lv_endtime").setMode(Cell.VMODE_DISPONLY);
				//cl.getCell("lvx_nldays").setMode(Cell.VMODE_DISPONLY);
				cl.getCell("lv_sttime").setMode(Cell.VMODE_HIDDEN);
				cl.getCell("lv_endtime").setMode(Cell.VMODE_HIDDEN);
				cl.getCell("lvx_nldays").setMode(Cell.VMODE_HIDDEN);
				cl.getCell("lv_edate").setMode(Cell.VMODE_NORMAL);
				cl.getCell("lvx_nldays").setMode(Cell.VMODE_DISPONLY);

				if (cl.getDate("lv_sdate").compareTo(cl.getDate("lv_edate")) == 0) {
					//cl.getCell("lvx_enfullday").setMode(Cell.VMODE_DISPONLY);
					//cl.getCell("lvx_enhalfday").setMode(Cell.VMODE_DISPONLY);
					cl.getCell("lvx_enfullday").setMode(Cell.VMODE_HIDDEN);
					cl.getCell("lvx_enhalfday").setMode(Cell.VMODE_HIDDEN);
					new ZkBiAbstractLongOp(curComp, "", 0){
						@Override
						public ReturnMsg longOp() {
							try {
								cl.getCell("lvx_enfullday").set(cl.getBoolean("lvx_stfullday"));
								cl.getCell("lvx_enhalfday").set(cl.getBoolean("lvx_sthalfday"));
							} catch (Exception ex) {
								UniLog.log(ex);
							}
							return null;
						}
					};
				} else {
					cl.getCell("lvx_enfullday").setMode(Cell.VMODE_NORMAL);
					cl.getCell("lvx_enhalfday").setMode(Cell.VMODE_NORMAL);
				}

				cl.getCell("lv_sttime").set(START_TIME_IN_DAY);
				cl.getCell("lv_endtime").set(END_TIME_IN_DAY);
			}
		}
		else {
			cl.getCell("lv_reason").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lvx_nopaid").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lv_sdate").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lvx_stfullday").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lvx_sthalfday").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lv_sttime").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lv_endtime").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lv_edate").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lvx_enfullday").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lvx_enhalfday").setMode(Cell.VMODE_DISPONLY);
			cl.getCell("lvx_nldays").setMode(Cell.VMODE_DISPONLY);
		}
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new LeaveApplicationDetGetItemProperty(p_br.getSubLink("erpv4ext.LeaveApplicationDet"))
		);	
	}

	/* Customized GIPI for LeaveApplicationDet */
	private class LeaveApplicationDetGetItemProperty extends BiGetItemProperty {
		PickByTableTrForm pickReasonForm;

		public LeaveApplicationDetGetItemProperty(BiResult p_br) {
			super(p_br);
			UniLog.log1("LeaveApplicationDetGetItemProperty");
		}

		@Override
		public boolean getAllowDelete(Object item) {
			BiCellCollection bcc = getBiResult().getRowCollectionO(item) ;
			UniLog.log1("getAllowDelete:%s, bcc:%s, reason:%s, autogen:%b", item, bcc, bcc.getString("lv_reason"), bcc.getBoolean("lv_autogen"));
			return !bcc.getBoolean("lv_autogen");
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_PULLDOWN_OPENED) {
				if (StringUtils.equals(bcc.getCellLabel(), "lv_reason")) {
					try {
						ZkJxPickInput pickComp = (ZkJxPickInput)getCellComponent(bcc);
						if (pickReasonForm == null) {
							pickReasonForm = new PickByTableTrForm(sessionHelper, new String[] {"lvrs_name", "lvrs_desc"}, new PickByTableTrForm.PickByTableTrFormCallback() {
								public void callback(Object[] rec, TableRec tr, Object userData) {
									try {
										BiCellCollection cl = (BiCellCollection) userData;
										cl.getCell("lv_reason").set((String)rec[tr.getFieldIndex("lvrs_name")]);
										cl.getCell("lvx_nopaid").set(StringUtils.equals((String)rec[tr.getFieldIndex("lvrs_nopay")], "Y"));
										calNlDaysNlUnit(cl);
									} catch (Exception e) {
										UniLog.log(e);
									}
								}
							});
						}
						pickReasonForm.bindComponent(pickComp, cl, bigibr, "select lvrs_name, lvrs_desc, lvrs_nopay from leavereason order by lvrs_name", null);
					}
					catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
			else if (p_ctype == GIPI_VALUE_CHANGED) {
				try {
					//setup entry/update on/off
					if (StringUtils.equalsAny(bcc.getCellLabel(), "lvx_stfullday", "lvx_sthalfday", "lvx_enfullday", "lvx_enhalfday", "lv_sdate", "lv_sttime", "lv_endtime", "lv_edate"))
						setupOnOffDetailCell(cl);

					//valication
					if (StringUtils.equalsAny(bcc.getCellLabel(), "lv_sdate", "lv_edate")) {
						Date startDate = cl.getDate("lv_sdate");
						Date endDate = cl.getDate("lv_edate");
						if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && startDate.compareTo(endDate) > 0)
							showErrorNotification("Start Date cannot be more than End Date", bcc);
					}
					if (StringUtils.equalsAny(bcc.getCellLabel(), "lv_sttime", "lv_endtime")) {
						zeroColumnCellDateSecond(bcc);
						Date startTime = cl.getDate("lv_sttime");
						Date endTime = cl.getDate("lv_endtime");
						if (!DateUtil.isDateNull(startTime) && !DateUtil.isDateNull(endTime) && (startTime.compareTo(START_TIME_IN_DAY) != 0 || endTime.compareTo(START_TIME_IN_DAY) != 0)) {
							if (startTime.compareTo(endTime) >= 0)
								showErrorNotification("Start Time must be less than End Time", bcc);
							else if ((endTime.getTime() - startTime.getTime()) % 3600000 != 0)
								showErrorNotification("Should be multiple of hours", bcc);
							else if (bcc.getDate().compareTo(MAX_TIME_IN_DAY) >= 0)
								showErrorNotification("Invalid time", bcc);
						}
					}

					//full day/half day
					if (StringUtils.equals(bcc.getCellLabel(), "lvx_stfullday") && bcc.getBoolean())
						cl.getCell("lvx_sthalfday").set(false);
					if (StringUtils.equals(bcc.getCellLabel(), "lvx_sthalfday") && bcc.getBoolean())
						cl.getCell("lvx_stfullday").set(false);
					if (StringUtils.equals(bcc.getCellLabel(), "lvx_enfullday")) {
						if (bcc.getBoolean())
							cl.getCell("lvx_enhalfday").set(false);
						else if (cl.getBoolean("lvx_stfullday") || cl.getBoolean("lvx_sthalfday"))
							bcc.set(true);
					}
					if (StringUtils.equals(bcc.getCellLabel(), "lvx_enhalfday")) {
						if (bcc.getBoolean())
							cl.getCell("lvx_enfullday").set(false);
						else if (cl.getBoolean("lvx_stfullday") || cl.getBoolean("lvx_sthalfday"))
							bcc.set(true);
					}

					if (StringUtils.equalsAny(bcc.getCellLabel(), "lv_reason", "lv_sdate", "lv_edate", "lvx_stfullday", "lvx_sthalfday", "lvx_enfullday", "lvx_enhalfday", "lv_sttime", "lv_endtime")) {
						//cal nldays/nlunit
						calNlDaysNlUnit(cl);
					}
					if (StringUtils.equals(bcc.getCellLabel(), "lvx_nldays")) {
						double cc = bcc.getDouble();
						cl.getCell("lv_leaveunit").set(getLvStr2LeaveUnit(String.valueOf(cc)));
						//cal AL/CL
						calLeaveRemain();
					}
				}
				catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED)
				setDirtyFlag(true);
			else {
				if (StringUtils.equals(bcc.getCellLabel(), "lv_reason")) {
					ZkJxPickInput zjpi = (ZkJxPickInput) getCellComponent(bcc);
					zjpi.setPopupWidth("500px");
				}
			}
		}
		
		private void calNlDaysNlUnit(BiCellCollection cl) throws Exception {
			if (cl.getBoolean("lvx_stfullday") || cl.getBoolean("lvx_sthalfday")) {
				cl.getCell("lvx_nldays").set(0.0);
				cl.getCell("lv_leaveunit").set(0);
				Date startDate = cl.getDate("lv_sdate");
				Date endDate = cl.getDate("lv_edate");
				if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate)) {
					long daycnt = (cl.getDate("lv_edate").getTime() - cl.getDate("lv_sdate").getTime()) / 86400000;
					double cc;
					if (daycnt != 0)
						cc = (cl.getBoolean("lvx_sthalfday") ? 0.5 : 1) * daycnt + (cl.getBoolean("lvx_enhalfday") ? 0.5 : 1);
					else
						cc = cl.getBoolean("lvx_sthalfday") ? 0.5 : 1;
					cl.getCell("lvx_nldays").set(cc);
					cl.getCell("lv_leaveunit").set(getLvStr2LeaveUnit(String.valueOf(cc)));
				}
			}
			else {
				Date startTime = cl.getDate("lv_sttime");
				Date endTime = cl.getDate("lv_endtime");
				if (!DateUtil.isDateNull(startTime) && !DateUtil.isDateNull(endTime)) {
					if (startTime.compareTo(START_TIME_IN_DAY) == 0 && endTime.compareTo(START_TIME_IN_DAY) == 0) { //manual mode
						double cc = cl.getCellDouble("lvx_nldays");
						cl.getCell("lv_leaveunit").set(getLvStr2LeaveUnit(String.valueOf(cc)));
					} else {
						long min = (endTime.getTime() - startTime.getTime()) / 60000;
						UniLog.log1("min:%d", min);
						double cc = NumberUtils.toDouble(getLeaveUnit2LvStr(getMinute2LeaveUnit(min)));
						cl.getCell("lvx_nldays").set(cc);
						cl.getCell("lv_leaveunit").set(getLvStr2LeaveUnit(String.valueOf(cc)));
					}
				} else {
					cl.getCell("lvx_nldays").set(0.0);
					cl.getCell("lv_leaveunit").set(0);
				}
			}

			//cal AL/CL
			calLeaveRemain();
		}
	}
	
	private void calLeaveRemain() throws Exception {
		getBr().getCell("emx_ralstr").set(calLeaveRemain("AL"));
		getBr().getCell("emx_rclstr").set(calLeaveRemain("CL"));
		clearLeaveRemain("AL", "CL");
	}
	
	private String calLeaveRemain(String code) throws Exception {
		leaveCal.resetLvUtilList(code);
		Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.LeaveApplicationDet");
		for (BiCellCollection cc : recs) {
			if (StringUtils.equals(cc.getString("lv_reason"), code)) {
				int nrUnit = leaveCal.genCalLeaveRemained(code, cc.getDate("lv_sdate"), cc.getInt("lv_leaveunit"));
				cc.getCell("lvx_nrunit").set(nrUnit);
				cc.getCell("lvx_nrdays").set(NumberUtils.toDouble(getLeaveUnit2LvStr(nrUnit)));
			}
		}

		List<RlvItem> rlvList = new ArrayList<RlvItem>();
		leaveCal.genGetLeaveRemain(code, DateUtil.today(), DateUtil.yearEnd(DateUtil.today()), rlvList);
		StringBuilder sbRlvStr = new StringBuilder();
		int cc = 0;
		for (RlvItem item : rlvList) { 
			if (sbRlvStr.length() > 0)
				sbRlvStr.append(", ");
			if (item.eftd == null || item.eftd.compareTo(DateUtil.today()) <= 0) 
				cc += item.unit;
			else {
				if (cc != 0) {
					sbRlvStr.append(getLeaveUnit2LvStr(cc));
					sbRlvStr.append(" + ");
					cc = 0;
				}
				sbRlvStr.append(getLeaveUnit2LvStr(item.unit));
				sbRlvStr.append(" ( ");
				sbRlvStr.append(DateUtil.dateToDateTimeStr(item.eftd, "yyyy/MM/dd"));
				sbRlvStr.append(" )");
			}
		}
		if (cc != 0)
			sbRlvStr.append(getLeaveUnit2LvStr(cc));
		UniLog.log1("genGetLeaveRemain %s,%s,%s,%s,%s", code, DateUtil.today(), DateUtil.yearEnd(DateUtil.today()), GsonUtil.objToStr(rlvList), sbRlvStr);
		return sbRlvStr.toString();
	}
	
	private void clearLeaveRemain(String... excludeCode) throws Exception {
		Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.LeaveApplicationDet");
		for (BiCellCollection cc : recs) {
			if (!StringUtils.equalsAny(cc.getString("lv_reason"), excludeCode)) {
				cc.getCell("lvx_nrunit").set(0);
				cc.getCell("lvx_nrdays").set(0.0);
			}
		}
	}

	
	
	private static class RlvItem {
		int unit;
		Date eftd;
		Date expd;
		int total;
	}
	
	private static class LvUtilItem {
		String code;
		Date date;
		Date expire;
		int unit;
		int used;
	}
	
	public static class LeaveCal {

		private List<LvUtilItem> lvUtilList = new ArrayList<LvUtilItem>();
		private BiResult br;
		private Date em_stdate;
		private Date em_enddate;
		private int em_stalcnt;
		private int em_maxalcnt;
		private int em_ofsalcnt;
		private String em_alstday;
		
		private Map<String, Object> userMap = new HashMap<String, Object>();
	
		public LeaveCal(BiResult br, CellCollection emCc) {
			this.br = br;
			em_stdate = emCc.getDate("em_stdate");
			em_enddate = emCc.getDate("em_enddate");
			em_stalcnt = emCc.getInt("em_stalcnt");
			em_maxalcnt = emCc.getInt("em_maxalcnt");
			em_ofsalcnt = emCc.getInt("em_ofsalcnt");
			em_alstday = emCc.getString("em_alstday");
		}

		public LeaveCal(BiResult br, Date em_stdate, Date em_enddate, int em_stalcnt, int em_maxalcnt, int em_ofsalcnt, String em_alstday) {
			this.br = br;
			this.em_stdate = em_stdate;
			this.em_enddate = em_enddate;
			this.em_stalcnt = em_stalcnt;
			this.em_maxalcnt = em_maxalcnt;
			this.em_ofsalcnt = em_ofsalcnt;
			this.em_alstday = em_alstday;
		}
		
		public Map<String, Object> getUserMap() {
			return userMap;
		}

		public void genGetLeaveRemain(String p_code, Date p_stdate, Date p_enddate, List<RlvItem> rlvList) {
			int idx0 = 0;
			int r0 = 0;
			int r1 = 0;
			Date date0 = null;
			Date date1 = null;
			Date tdate = null;
			for (LvUtilItem item : lvUtilList) {
				if (StringUtils.equals(item.code, p_code)) {
					if (item.date.compareTo(p_enddate) > 0) 
						break;
					tdate = item.date;
					if (item.expire.compareTo(p_stdate) > 0 && item.unit - item.used != 0) {
						if (date0 != tdate) {
							if (r0 != 0 || r1 != 0) {
								RlvItem newItem = new RlvItem();
								rlvList.add(idx0, newItem);
								newItem.unit = r0;
								newItem.total = r1;
								newItem.eftd = date0;
								newItem.expd = date1;
								idx0++;
							}
							r0 = item.unit - item.used;
							r1 = item.unit;
							date0 = tdate;
							date1 = item.expire;
						} else {
							r0 += item.unit - item.used;
							r1 += item.unit;
						}
					}
				}
			}
			if (r0 != 0 || r1 != 0) {
				RlvItem newItem = new RlvItem();
				rlvList.add(idx0, newItem);
				newItem.unit = r0;
				newItem.total= r1;
				newItem.eftd = date0;
				newItem.expd = date1;
				idx0++;
			}
		}
		
		public void clearLvUtilList(String p_code) {
			for (int i = lvUtilList.size() - 1; i >= 0; i--) {
				if (StringUtils.isBlank(p_code) || StringUtils.equals(p_code, lvUtilList.get(i).code))
					lvUtilList.remove(i);
			}
		}
	
		public void resetLvUtilList(String p_code) {
			for (int i = lvUtilList.size() - 1; i >= 0; i--) {
				if (StringUtils.equals(p_code, lvUtilList.get(i).code))
					lvUtilList.get(i).used = 0;
			}
		}
		
		public void genCalLeave(String p_code, String p_eid, Date p_todate) throws Exception {
			UniLog.log1("genCalLeave %s,%s,%s", p_code, p_eid, p_todate);
			Date todate = p_todate;
			if (StringUtils.equals(p_code, "AL")) {
				//sal,mal,ofs,tstday
				Date stdate = em_stdate;
				Date enddate = em_enddate;
				if (DateUtil.isDateNull(enddate))
					enddate = MAX_DATE;
				int sal = em_stalcnt;
				int mal = em_maxalcnt;
				int ofs = em_ofsalcnt;
				String tstday = em_alstday;
				//ifelse(CUSTOMIZATION_PREFIX
				//\)
				int ty = NumberUtils.toInt(DateUtil.dateToDateTimeStr(todate, "yyyy"));
				int fy = NumberUtils.toInt(DateUtil.dateToDateTimeStr(stdate, "yyyy"));
				//ifelse(CAL_ANNUALLEAVE_FROM_STDATE,1,\	
				tstday = "/" + tstday.trim();
				Date tstdate = getValidYymmdd(fy, tstday);
				Date tenddate = getValidYymmdd(ty, tstday);
				if (tstdate.compareTo(stdate) > 0)
					fy--;
				genCalAnnualLeave(stdate, fy, ty, enddate, sal, mal, ofs, tstday);
				//\,\
				//\)
				UniLog.log1("genCalAnnualLeave startDate:%s, fy:%d, ty:%d, endDate:%s, sal:%d, mal:%d, ofs:%d, tstday:%s, lvUtilList:%s", stdate, fy, ty, enddate, sal, mal, ofs, tstday, GsonUtil.objToStr(lvUtilList));
			}
			if (StringUtils.equals(p_code, "CL"))
				genCalPresetLeave(p_eid, p_code);
			//ifelse(REGULAR_LEAVE_CODE,,\
			//\,\
			//\)
		}
		
	
		public int genCalLeaveRemained(String p_code, Date p_date, int p_leaveunit) {
			Date date0 = p_date;
			int idx0 = -1;
			for (int i = 0; i < lvUtilList.size(); i++) {
				if (StringUtils.equals(lvUtilList.get(i).code, p_code)) {
					idx0 = i;
					break;
				}
			}
			if (idx0 < 0)
				return -p_leaveunit;
			else {
				int r0 = p_leaveunit;
				int r1 = 0;
				int r2 = 0;
				int idx1;
				for (idx1 = idx0; idx1 < lvUtilList.size();idx1++) {
					LvUtilItem item = lvUtilList.get(idx1);
					if (!StringUtils.equals(item.code, p_code)) 
						break;
					if (item.date.compareTo(date0) > 0)
						break;
					if (item.expire.compareTo(p_date) > 0) {
						if (item.unit - item.used > r0) {
							item.used += r0;
							r0 = 0;
							r1 += item.unit - item.used;
						} else {
							if (item.unit > item.used) {
								r0 -= item.unit - item.used;
								item.used = item.unit;
							} else
								r2 += item.used - item.unit;
						}
					}
					date0 = DateUtil.nextday(date0, (int)Math.floor((r1 + p_leaveunit - r0) / LEAVEUNIT_PER_DAY));
				}
				if (r0 > 0) {
					for (idx1 = idx1 - 1; idx1 >= idx0; idx1--) {
						LvUtilItem item = lvUtilList.get(idx1);
						if (item.date.compareTo(date0) <= 0 && item.expire.compareTo(p_date) > 0) {
							item.used += r0;
							return -r0 - r2;
						}
					}
					UniLog.log("Leave calculateion error (1)");
					return -r0 - r2;
				} else
					return r1 - r2;
			}
		}
		
		public void genCalAnnualLeave(Date p_stdate, int p_fy, int p_ty, Date p_enddate, int sal, int mal, int ofs, String p_stday) throws Exception {
			String tstday = p_stday;
			Date stdate = p_stdate;
			int fy = p_fy;
			int ty = p_ty;
	
			int idx0 = -1;
			for (int i = 0; i < lvUtilList.size(); i++) {
				if (StringUtils.equals(lvUtilList.get(i).code, "AL")) {
					idx0 = i;
					break;
				}
			}
			int idx1;
			if (idx0 < 0) {
				idx0 = lvUtilList.size();
				idx1 = idx0;
			} else {
				for (idx1 = idx0; idx1 < lvUtilList.size(); idx1++) {
					if (!StringUtils.equals(lvUtilList.get(idx1).code, "AL"))
						break;
				}
			}
	
			for (int yy = fy; yy <= ty; yy++) {
				int tday = customGetAnnualLeave(p_stdate, yy, p_enddate, sal, mal, ofs, tstday);
				Date date0 = getValidYymmdd(yy, tstday);
				if (date0.compareTo(stdate) < 0) 
					date0 = stdate;
				for (;idx0 < idx1; idx0++) {
					LvUtilItem item = lvUtilList.get(idx0);
					if (item.date.compareTo(date0) == 0)
						break;
					
					if (item.date.compareTo(date0) > 0) {
						//ifelse(IS_MACAU_3,1,\
						//\,\
						if (yy < YEAR_TO_IGNORE_MAX_CARRYFORWARD) {
							if (LEAVE_EXPIRE_YEAR != 1 && LEAVEUNIT_MAX_CARRYFORWARD != 0) {
								if (tday > LEAVEUNIT_MAX_CARRYFORWARD) {
									LvUtilItem newItem = new LvUtilItem();
									lvUtilList.add(idx0, newItem);
									newItem.code = "AL";
									newItem.date = date0;
									newItem.expire = getValidYymmdd(yy + 1, tstday);
									newItem.unit = tday - LEAVEUNIT_MAX_CARRYFORWARD;
									idx0++;
									idx1++;
									tday = LEAVEUNIT_MAX_CARRYFORWARD;
								}
							}
						}
						//\)
						LvUtilItem newItem = new LvUtilItem();
						lvUtilList.add(idx0, newItem);
						newItem.code = "AL";
						newItem.date = date0;
						if (LEAVE_EXPIRE_YEAR > 0)
							newItem.expire = getValidYymmdd(yy + LEAVE_EXPIRE_YEAR, tstday);
						else 
							newItem.expire = MAX_DATE;
						if (yy + LEAVE_EXPIRE_YEAR > YEAR_TO_IGNORE_MAX_CARRYFORWARD)
							newItem.expire = MAX_DATE;
						newItem.unit = tday;
						idx0++;
						idx1++;
						//ifelse(IS_MACAU_3,1,\
						//\)
						break;
					}
				}
				if (idx0 >= idx1) {
					//ifelse(IS_MACAU_3,1,\
					//\,\
					if (yy < YEAR_TO_IGNORE_MAX_CARRYFORWARD) {
						if (LEAVE_EXPIRE_YEAR != 1 && LEAVEUNIT_MAX_CARRYFORWARD != 0) {
							if (tday > LEAVEUNIT_MAX_CARRYFORWARD) {
								LvUtilItem newItem = new LvUtilItem();
								lvUtilList.add(idx0, newItem);
								newItem.code = "AL";
								newItem.date = date0;
								newItem.expire = getValidYymmdd(yy + 1, tstday);
								newItem.unit = tday - LEAVEUNIT_MAX_CARRYFORWARD;
								idx0++;
								idx1++;
								tday = LEAVEUNIT_MAX_CARRYFORWARD;
							}
						}
					}
					//\)
					LvUtilItem newItem = new LvUtilItem();
					lvUtilList.add(idx0, newItem);
					newItem.code = "AL";
					newItem.date = date0;
					if (LEAVE_EXPIRE_YEAR > 0)
						newItem.expire = getValidYymmdd(yy + LEAVE_EXPIRE_YEAR, tstday);
					else 
						newItem.expire = MAX_DATE;
					if (yy + LEAVE_EXPIRE_YEAR > YEAR_TO_IGNORE_MAX_CARRYFORWARD)
						newItem.expire = MAX_DATE;
					newItem.unit = tday;
					idx0++;
					//ifelse(IS_MACAU_3,1,\
					//\)
				}
			}
		}
		
		public void genCalPresetLeave(String p_eid, String p_code) throws Exception {
			int idx0 = lvUtilList.size();
			TableRec tr = br.getSelectUtil().getQueryResult("select emlvr_stdate, emlvr_enddate, emlvr_lvdaterange, emlvr_cancelled from emleaverange, leavereason"
					+ " where emlvr_emid = ? and lvrs_name = ? and lvrs_rg = emlvr_lvreasonrg"
					+ " order by emlvr_stdate", 
					new Wherecl().appendArgument(p_eid)
								.appendArgument(p_code));
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				if (!StringUtils.equals(tr.getFieldString("emlvr_cancelled"), "Y")) {
					LvUtilItem item = new LvUtilItem();
					lvUtilList.add(idx0, item);
					item.code = p_code;
					item.date = tr.getFieldDate("emlvr_stdate");
					item.expire = DateUtil.nextday(tr.getFieldDate("emlvr_enddate"));
					item.unit = (int)Math.ceil(tr.getFieldDouble("emlvr_lvdaterange") * LEAVEUNIT_PER_DAY);
					idx0++;
				}
			}
		}
	}
	
	//Customer Specific routine to calculate the annual leave days of whole year base on date joined
	private static int customGetAnnualLeave(Date p_stdate, int yy, Date p_enddate, int p_sal, int p_mal, int p_ofs, String p_stday) throws Exception {
		//this calculation is only valid if p_stday > mm/dd(p_stdate)
		//ifelse(PRORA_LEAVE_EVERYYEAR,1,\
		String tstday = p_stday;
		Date tstdate = getValidYymmdd(yy, tstday);
		if (p_enddate.compareTo(tstdate) <= 0) 
			return 0;
		if (tstdate.compareTo(p_stdate) <= 0)
			return customGetAnnualLeaveReal(p_stdate, yy, p_enddate, p_sal, p_mal, p_ofs, p_stday);
		else {
			String tstday2 = DateUtil.dateToDateTimeStr(p_stdate, "/MM/dd");
			int yy2 = yy;
			Date tmpdate = getValidYymmdd(yy2, tstday2);
			if (tstdate == tmpdate)
				return customGetAnnualLeaveReal(p_stdate, yy, p_enddate, p_sal, p_mal, p_ofs, p_stday);
			if (tmpdate.compareTo(tstdate) > 0) {
				yy2--;
				tmpdate = getValidYymmdd(yy2, tstday2);
			}
			Date tenddate = getValidYymmdd(yy2 + 1, tstday2);
			int tday = (int)((tstdate.getTime() - tmpdate.getTime()) / 86400000);
			int a0 = customGetAnnualLeaveReal(p_stdate, yy2, tenddate, p_sal, p_mal, p_ofs, tstday2);
			yy2++;
			tmpdate = getValidYymmdd(yy2,tstday2);
			tenddate = getValidYymmdd(yy2 + 1,tstday2);
			int a1 = customGetAnnualLeaveReal(p_stdate, yy2, tenddate, p_sal, p_mal, p_ofs, tstday2);
			if (a0 == a1)
				return customGetAnnualLeaveReal(p_stdate, yy, p_enddate, p_sal, p_mal, p_ofs, p_stday);

			a0 /= LEAVEUNIT_PER_DAY;
			a1 /= LEAVEUNIT_PER_DAY;
			//ifelse(LEAVEUNIT_CALCULATION_ROUNDUP,1,\
			//\,\
			int a2 = (int)Math.floor( (a0 * tday / DAYS_PER_YEAR * LEAVEUNIT_PER_DAY + LEAVEUNIT_INCREMENT_ROUNDING) / LEAVEUNIT_MIN_INCREMENT) * LEAVEUNIT_MIN_INCREMENT;
			//\)

			a0 = a0 * LEAVEUNIT_PER_DAY - a2;
			if (p_enddate.compareTo(tmpdate) <= 0) {
				tday = (int)((p_enddate.getTime() - tstdate.getTime()) / 86400000);
				int tday2 = (int)((tmpdate.getTime() - tstdate.getTime()) / 86400000);
				a2 = a0 * tday / tday2;
				return a2;
			} 
			else {
				tenddate = getValidYymmdd(yy + 1, tstday);
				tday = (int)((tenddate.getTime() - tmpdate.getTime()) / 86400000);
				//ifelse(LEAVEUNIT_CALCULATION_ROUNDUP,1,\
				//\,\
				a2 = (int)Math.floor((a1 * tday / DAYS_PER_YEAR * LEAVEUNIT_PER_DAY + LEAVEUNIT_INCREMENT_ROUNDING) / LEAVEUNIT_MIN_INCREMENT) * LEAVEUNIT_MIN_INCREMENT;
				//\)

				a1 = a2;
				if (p_enddate.compareTo(tenddate) < 0) {
					int tday2 = (int)((p_enddate.getTime() - tmpdate.getTime()) / 86400000);
					a2 = a1 * tday / tday2;
					return a2 + a0;
				} else 
					return a1 + a0;
			}
		}
		//\,\
		///)
	}
	
	private static int customGetAnnualLeaveReal(Date p_stdate, int yy, Date p_enddate, int p_sal, int p_mal, int p_ofs, String p_stday) throws Exception {
		Date stdate = p_stdate;
		Date enddate = p_enddate;
		int sy = NumberUtils.toInt(DateUtil.dateToDateTimeStr(stdate, "yyyy"));
		int cc = -1;
		String tstday = p_stday;
		Date tstdate = getValidYymmdd(yy, tstday);
		Date tenddate = getValidYymmdd(yy + 1, tstday);

		//ifelse(IS_STD_ANNUALLEAVE,1,\
		Date tmpstdate = getValidYymmdd(sy - 1, tstday);
		Date tmpenddate = getValidYymmdd(sy, tstday);
		if (stdate.compareTo(tmpstdate) >= 0 && stdate.compareTo(tmpenddate) < 0)
			sy--;
		//\)
		
		//ifelse(IS_MASTV,1,\
		//\)

		//ifelse(IS_STD_ANNUALLEAVE,1,\
		int sal = p_sal;
		int mal = p_mal;
		if (yy - sy - p_ofs >= 0)
			cc = sal + yy - sy - p_ofs;
		else
			cc = sal;
		if (cc > mal) 
			cc = mal;
		//\)
		//ifelse(IS_MACAU_3,1,\
		//\)

		if (cc < 0)
			throw new Exception("Annual Leave Calculation Mathod not Set");
		//cc = num of annual leave days for whole year
		if (stdate.compareTo(tstdate) > 0 || enddate.compareTo(tenddate) < 0) {
			Date date0, date1;
			if (enddate.compareTo(tenddate) < 0) 
				date0 = DateUtil.nextday(enddate);
			else 
				date0 = tenddate;
			if (stdate.compareTo(tstdate) > 0) 
				date1 = stdate;
			else 
				date1 = tstdate;
			int tday = (int)((date0.getTime() - date1.getTime()) / 86400000);
			if (tday >= DAYS_PER_YEAR) 
				return cc * LEAVEUNIT_PER_DAY;
			if (tday <= 0) 
				return 0;
			//ifelse(LEAVEUNIT_CALCULATION_ROUNDUP,1,\
			//\,\
			return (int)Math.floor((cc * tday / DAYS_PER_YEAR * LEAVEUNIT_PER_DAY + LEAVEUNIT_INCREMENT_ROUNDING) / LEAVEUNIT_MIN_INCREMENT) * LEAVEUNIT_MIN_INCREMENT;
			//\)
		} 
		else
			return cc * LEAVEUNIT_PER_DAY;
	}
	
	private static Date getValidYymmdd(int p_yy, String p_mdstr) {
		if (StringUtils.equals(p_mdstr, "/02/29")) {
			Date tmpDate = DateUtil.dateTimeStrToDate(String.format("%04d/03/01", p_yy));
			tmpDate = DateUtil.prevday(tmpDate);
			if (!DateUtil.dateToDateTimeStr(tmpDate, "/MM/dd").equals(p_mdstr))
				return DateUtil.nextday(tmpDate);
		}
		return DateUtil.dateTimeStrToDate(String.format("%04d%s", p_yy, p_mdstr));
	}

	public static int getLvStr2LeaveUnit(String p_lvstr) {
		int tday = 0;
		int cc = p_lvstr.indexOf('/');
		if (cc >= 0) {
			double tmpf = NumberUtils.toDouble(p_lvstr.substring(0, cc));
			tday = (int)Math.floor(tmpf * LEAVEUNIT_PER_DAY);
			String lvStr = p_lvstr.substring(cc + 1);
			cc = lvStr.indexOf(':');
			if (cc >= 0) {
				tmpf = NumberUtils.toDouble(lvStr.substring(0, cc));
				tday += (int)Math.floor(tmpf * 60 / MINUTES_PER_LEAVEUNIT);
				tmpf = NumberUtils.toDouble(lvStr.substring(cc + 1));
				tday += (int)Math.floor(tmpf / MINUTES_PER_LEAVEUNIT);
			} else {
				tmpf = NumberUtils.toDouble(lvStr);
				tday += (int)Math.floor(tmpf * 60 / MINUTES_PER_LEAVEUNIT);
			}
		} else
			tday = (int)Math.floor(NumberUtils.toDouble(p_lvstr) * LEAVEUNIT_PER_DAY);
		return tday;
	}

	public static String getLeaveUnit2LvStr(int p_leaveunit) {
		if (p_leaveunit < 0)  
			return "-" + getLeaveUnit2LvStr(-p_leaveunit);
		StringBuilder sbLvStr = new StringBuilder();
		int cc = p_leaveunit / LEAVEUNIT_PER_DAY;
		int r0 = p_leaveunit - cc * LEAVEUNIT_PER_DAY;
		sbLvStr.append(cc);
		if (r0 > 0) {
			if (r0 == LEAVEUNIT_PER_HALFDAY) {
				sbLvStr.append(".5");
				return sbLvStr.toString();
			} else {
				//ifelse(SHOW_LEAVE_IN_DECIMAL,1,\
				cc = (int)Math.round((double)r0 / LEAVEUNIT_PER_DAY * 10);
				sbLvStr.append(".");
				sbLvStr.append(cc);
				//\,\
				///)
			}
		}
		return sbLvStr.toString();
	}
	
	public static int getMinute2LeaveUnit(long p_minute) {
		return (int)Math.floor(p_minute / MINUTES_PER_LEAVEUNIT);
	}
	
	public static void zeroColumnCellDateSecond(ColumnCell bcc) throws Exception {
		Date date = bcc.getDate();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		if (cal.get(Calendar.SECOND) != 0 || cal.get(Calendar.MILLISECOND) != 0) {
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			bcc.set(cal.getTime());
		}
	}
	
	public static Component getCellComponent(Cell c) {
		return ((ZkBiCellValueMapper)c.getMapper()).getComponent();
	}

	public static Component getCellComponent(CellCollection cl, String key) throws Exception {
		return getCellComponent(cl.getCell(key));
	}

	public static void showErrorNotification(String msg, Component comp) {
		Clients.showNotification(msg, "error", comp, "end_center", 5000, true); 
	}

	public static void showErrorNotification(String msg, Cell c) {
		showErrorNotification(msg, getCellComponent(c));
	}

	public static void showErrorNotification(String msg, CellCollection cl, String key) throws Exception {
		showErrorNotification(msg, cl.getCell(key));
	}

	public static class ListGetItemProperty extends AbstractGetItemProperty {
		private List<String> widthList;
		private List<String[]> valueList;

		public ListGetItemProperty(List<String> p_widthList, List<String[]> p_valueList) {
			super();
			widthList = p_widthList;
			valueList = p_valueList;
		}
		
		@Override
		public int getColumnCount(Object item) {
			return widthList.size();
		}

		@Override
		public String getString(Object item) {
			StringBuilder sb = new StringBuilder();
			String[] rec = (String[])item;
			for (String s : rec) {
				sb.append(s);
				sb.append(" ");
			}
			return sb.toString();
		}

		@Override
		public Object getColumnValue(Object p_v, int p_col) {
			return ((String[])p_v)[p_col];
		}

		@Override
		public int getRowWidth() {
			return 0;
		}

		@Override
		public Object getRow(int p_row) {
			return valueList.get(p_row);
		}

		@Override
		public int getRowCount() {
			return valueList.size();
		}

		@Override
		public int getIndexOf(Object item) {
			return valueList.indexOf(item);
		}

		@Override
		public String getColumnWidth(Object p_v, int p_col) {
			return widthList.get(p_col);
		}
	}

	public static class PickByTableTrForm {
		private JxSelOpt gipiForm;
		private TrGetItemProperty gipi;
		
		public interface PickByTableTrFormCallback {
			void callback(Object[] rec, TableRec tr, Object userData);
		}

		public PickByTableTrForm(SessionHelper sh, String[] fieldList, final PickByTableTrFormCallback cb) {
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
			gipi = new TrGetItemProperty(Lists.newArrayList(fieldList));
			gipiForm = JxSelOpt.createJxSelOpt(pvdr);
			gipiForm.setOnSelectAction(new JxActionListener() {
				public void actionPerformed(JxField fd) {
					try {
						Object[] rec = (Object[]) fd.getValue();
						TableRec tr = gipi.getTableRec();
						cb.callback(rec, tr, gipiForm.getUserData());
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					gipiForm.closeForm();
				}
			});
		}
		
		public void bindComponent(ZkJxPickInput pickComp, Object userData, BiResult br, String selectStr, Wherecl wherecl) throws Exception {
			pickComp.setJxZkForm(gipiForm);
			TableRec tr = br.getSelectUtil().getQueryResult(selectStr, wherecl);
			gipi.setTableRec(tr);
			gipiForm.setUserData(userData);
			gipiForm.jxAdd("pickListBox").setItemListInterface(gipi);
		}
	}

	public static class PickByListForm {
		private JxSelOpt gipiForm;
		private ListGetItemProperty gipi;

		public interface PickByListFormCallback {
			void callback(String[] rec, Object userData);
		}

		public PickByListForm(SessionHelper sh, String[] widthList, List<String[]> valueList, final PickByListFormCallback pickByListFormCallback) {
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
			gipi = new ListGetItemProperty(Lists.newArrayList(widthList), valueList);
			gipiForm = JxSelOpt.createJxSelOpt(pvdr);
			gipiForm.setOnSelectAction(new JxActionListener() {
				public void actionPerformed(JxField fd) {
					try {
						String[] rec = (String[]) fd.getValue();
						pickByListFormCallback.callback(rec, gipiForm.getUserData());
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					gipiForm.closeForm();
				}
			});
		}

		public void bindComponent(ZkJxPickInput pickComp, Object userData) throws Exception {
			pickComp.setJxZkForm(gipiForm);
			gipiForm.setUserData(userData);
			gipiForm.jxAdd("pickListBox").setItemListInterface(gipi);
		}
	}
}
