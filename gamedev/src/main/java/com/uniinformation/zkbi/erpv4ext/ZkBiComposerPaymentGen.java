package com.uniinformation.zkbi.erpv4ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4ext.BiResultLeaveApplication;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.kyoko.common.DateUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.exprpar.FunctionInterface;
import com.uniinformation.utils.exprpar.Parser;
import com.uniinformation.utils.exprpar.VariableInterface;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiHelpDialog;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

import static com.uniinformation.utils.ZkUtil.throwConsumer;
import static com.uniinformation.utils.ZkUtil.throwFunction;

public class ZkBiComposerPaymentGen extends ZkComposerBase {
	public static final String PAYMENT_TYPE_INCOME = "01";
	public static final String PAYMENT_TYPE_DEDUCTION = "02";
	public static final String PAYMENT_TYPE_PENSION = "03";
	public static final int MAX_PMI_FLAG = 10;
	public static final int PAYAMOUNT_IDX_RINCOME = 0;
	public static final int PAYAMOUNT_IDX_OINCOME = 1;
	public static final int PAYAMOUNT_IDX_EPENSION = 2;
	public static final int PAYAMOUNT_IDX_TAXIBLE = 3;
	public static final int PAYAMOUNT_IDX_RELAVENT = 4;
	public static final int PAYAMOUNT_IDX_RPENSION = 5;
		
	@Wire
	private Window winPaymentGen;
	@Wire
	private Listbox s2Emid, s2Dept, s2PayPeriod, s2PayDate;
	@Wire
	private Radiogroup rgWhenExist;
	@Wire
	private Button btStart;
	
	private MainItem[] incomeItemArr;
	private MainItem[] deductionItemArr;
	private MainItem[] pensionItemArr;
	private PaymentMaster lpm;
	private List<PaymentDet> paymentDetList = new ArrayList<>();
	private List<PaymentItem> paymentItemList = new ArrayList<>();
	private List<PaymentItemDet> paymentItemDetList = new ArrayList<>();
	private SelectUtil suQuery, suUpdate;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");

		suQuery = sessionHelper.newBiResult("erpv4ext.Employee").getSelectUtil();
		try {
			//fill Employee Code
			TableRec tr = suQuery.getQueryResult("select em_eid, em_nickname, em_midname, em_surname, em_csurname, em_cmidname from employee order by em_eid");
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				final String emid = tr.getFieldString("em_eid");
				final String em_nickname = tr.getFieldString("em_nickname");
				final String em_midname = tr.getFieldString("em_midname");
				final String em_surname = tr.getFieldString("em_surname");
				final String em_cmidname = tr.getFieldString("em_cmidname");
				final String em_csurname = tr.getFieldString("em_csurname");
				final String ename = StringUtils.isBlank(em_nickname) ? (em_midname + " " + em_surname) : (em_midname + " " + em_surname + " (" + em_nickname + ")");
				final String cname = StringUtils.isBlank(em_nickname) ? (em_csurname + em_cmidname) : (em_csurname + em_cmidname + " (" + em_nickname + ")");
				s2Emid.appendChild(new Listitem(emid + " - " + (StringUtils.equalsAny(sessionHelper.getLHLang(), "TCHN", "SCHN") ? cname :  ename)){{setValue(emid);}});
			}
			tr = suQuery.getQueryResult("select dpmt_rg, dpmt_code, dpmt_name from deptmt order by dpmt_code");
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				final int rg = tr.getFieldInt("dpmt_rg");
				final String code = tr.getFieldString("dpmt_code");
				final String name = tr.getFieldString("dpmt_name");
				s2Dept.appendChild(new Listitem(code + " - " + name){{setValue(rg);}});
			}
			tr = suQuery.getQueryResult("select pp_start, pp_end from payperiod order by pp_start desc");
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				final Date startDate = tr.getFieldDate("pp_start");
				final Date endDate = tr.getFieldDate("pp_end");
				final String str = DateUtil.dateToDateTimeStr(startDate, "yyyy/MM/dd") + " - " + DateUtil.dateToDateTimeStr(endDate, "yyyy/MM/dd");
				s2PayPeriod.appendChild(new Listitem(str){{setValue(str);}});
			}
		}
		catch (Exception e) {
			UniLog.log(e);
		}

		s2Emid.setMultiple(true);
		s2Emid.setAttribute("placeholder", "Please choose Employee Code");
		s2Emid.setAttribute("select2-multiple", "Y");
		ZkUtil.setupSelect2(s2Emid, true, true);

		s2Dept.setMultiple(false);
		s2Dept.setAttribute("placeholder", "Please choose Department");
		s2Dept.setAttribute("select2-multiple", "N");
		ZkUtil.setupSelect2(s2Dept, true, true);

		s2PayPeriod.setMultiple(false);
		s2PayPeriod.setAttribute("select2-multiple", "N");
		ZkUtil.setupSelect2(s2PayPeriod, true, false);

		s2PayDate.setMultiple(false);
		s2PayDate.setAttribute("select2-multiple", "N");
		ZkUtil.setupSelect2(s2PayDate, true, false);
		
		for (Radio rd : rgWhenExist.getItems()) {
			String v = rd.getLabel();
			rd.setValue(v);
			rd.setLabel(TranslateUtil.getText(sessionHelper, "ERPV4EXT.PAYMENTGEN.RD_WHENEX_" + v.toUpperCase(), "OPTION", v));
		}
		winPaymentGen.setTitle(TranslateUtil.getText(sessionHelper, "ZkBiPaymentGen_01", "MENU", "Payment Generation"));

		Toolbarbutton btnHelp = (Toolbarbutton) winPaymentGen.query("#btnHelp");
		String helpId = StringUtils.defaultIfBlank(Executions.getCurrent().getParameter("helpid"), "edu.TodayAttendance");
		new ZkBiHelpDialog(sessionHelper, btnHelp, winPaymentGen, winPaymentGen.getTitle(), helpId, winPaymentGen.getTitle());
		ZkUtil.translateAllComp(sessionHelper, winPaymentGen, "ERPV4EXT.PAYMENTGEN", null);
		
		s2PayPeriod.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Listitem, String>>(){
			@Override
			public void onEvent(SelectEvent<Listitem, String> event) throws Exception {
				UniLog.log1("s2PayPeriod event:%s, selectedItems:%s", event, event.getSelectedItems());
				while (s2PayDate.getItems().size() > 0)
					s2PayDate.removeItemAt(0);
				Set<Listitem> list = event.getSelectedItems();
				if (list != null && !list.isEmpty()) {
					String s = list.iterator().next().getValue();
					String[] ss = s.split(" - ");
					final Date payStartDate = DateUtil.dateTimeStrToDate(ss[0]);
					final Date payEndDate = DateUtil.dateTimeStrToDate(ss[1]);
					s2PayDate.appendChild(new Listitem(DateUtil.dateToDateTimeStr(payEndDate, "yyyy/MM/dd")){{setValue(payEndDate);}});
					s2PayDate.appendChild(new Listitem(DateUtil.dateToDateTimeStr(DateUtil.nextMonthStart(payEndDate), "yyyy/MM/dd")){{setValue(DateUtil.nextMonthStart(payEndDate));}});
				}
				ZkUtil.delayJs(s2PayDate,null,50,"zkbis2.setup('%s',%b,%b,'%s',%b,%b);$('#%s').focus();"
						+ "$('#%s').val(null).trigger('change').trigger('select2:unselect')",s2PayDate.getUuid(), s2PayDate.isMultiple(), false, StringUtils.defaultString((String)s2PayDate.getAttribute("placeholder")), false, false, s2PayDate.getUuid(), s2PayDate.getUuid());
			}
		});

		btStart.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("btStart event:%s", event);
				if (s2PayPeriod.getSelectedItem() == null) {
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Please choose Pay Period");
					return;
				}
				if (s2PayDate.getSelectedItem() == null) {
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Please choose Pay Date");
					return;
				}
				String s = s2PayPeriod.getSelectedItem().getValue();
				String[] ss = s.split(" - ");
				final Date payStartDate = DateUtil.dateTimeStrToDate(ss[0]);
				final Date payEndDate = DateUtil.dateTimeStrToDate(ss[1]);
				final Date payDate = s2PayDate.getSelectedItem().getValue();
				if (!DateUtil.isValid(payStartDate)) {
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Invalid Start Date");
					return;
				}
				if (!DateUtil.isValid(payEndDate)) {
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Invalid End Date");
					return;
				}
				if (!DateUtil.isValid(payDate)) {
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Invalid Pay Date");
					return;
				}
				if (rgWhenExist.getSelectedItem() == null) {
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Please choose option");
					return;
				}
				final String whenExistStatus = rgWhenExist.getSelectedItem().getValue();
				

				final Set<String> emIdList = new LinkedHashSet<String>();
				for (Listitem li : s2Emid.getSelectedItems())
					emIdList.add((String)li.getValue());
				if (s2Dept.getSelectedItem() != null) {
					int dprg = s2Dept.getSelectedItem().getValue();
					TableRec tr = suQuery.getQueryResult("select emg_eid from emgrade where emg_deptrg = ? and emg_stdate <= ? and emg_enddate >= ?", 
							new Wherecl().appendArgument(dprg).appendArgument(payEndDate).appendArgument(payStartDate));
					Set<String> emIdList1 = new LinkedHashSet<String>();
					for (int i = 0; i < tr.getRecordCount(); i++) {
						tr.setRecPointer(i);
						emIdList1.add(tr.getFieldString("emg_eid"));
					}
					if (emIdList1.isEmpty()) {
						ZkBiMsgbox.show(ZkBiMsgbox.Type.error, String.format("Employee Code not found in department '%s'", s2Dept.getSelectedItem().getLabel()));
						return;
					}
					emIdList.addAll(emIdList1);
				}
				if (emIdList.isEmpty()) {
					ZkBiMsgbox.show(ZkBiMsgbox.Type.question, "Confirm Generate Payment for All Employee?", new String[] {"Ok", "Cancel"}, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("confirm %s", event);
							ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
							if (btn.getName().equals("Ok")) {
								TableRec tr = suQuery.getQueryResult("select em_eid from employee where em_stdate <= ? and (em_enddate = '' or em_enddate >= ?) order by em_eid", 
										new Wherecl().appendArgument(payEndDate).appendArgument(payStartDate));
								for (int i = 0; i < tr.getRecordCount(); i++) {
									tr.setRecPointer(i);
									emIdList.add(tr.getFieldString("em_eid"));
								}
								startGenerate(emIdList, payStartDate, payEndDate, payDate, whenExistStatus);
							}
						}
					});
				} else
					startGenerate(emIdList, payStartDate, payEndDate, payDate, whenExistStatus);
			}
		});
	}
	
	private void startGenerate(Set<String> emIdList, Date payStartDate, Date payEndDate, Date payDate, String whenExistStatus) {
		UniLog.log1("emIdList size:%s, payStartDate:%s, payEndDate:%s, payDate:%s, whenExistStatus:%s", emIdList.size(), payStartDate, payEndDate, payDate, whenExistStatus);
		int ok = 0, skip = 0, overwrite = 0, fail = 0;
		String err = null;
		List<String> genEmIdList = new ArrayList<String>();
		Set<String> overwriteEmIdList = new HashSet<String>();
		for (String emid : emIdList) {
			UniLog.log1("validateOne emid:%s", emid);
			try {
				switch (validateOne(emid, payStartDate, whenExistStatus)) {
					case 1:
						genEmIdList.add(emid);
						break;
					case 2:
						genEmIdList.add(emid);
						overwriteEmIdList.add(emid);
						break;
					case 3:
						err = String.format("Payment Record exist, operation aborted. (Employee Code: %s)", emid);
						break;
					default:
						skip++;
						break;
				}
				if (err != null) {
					genEmIdList.clear();
					overwriteEmIdList.clear();
					skip = emIdList.size();
					break;
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				err = StringUtils.defaultIfBlank(ex.getMessage(), ex.toString()) + String.format(" (Employee Code: %s)", emid);
				genEmIdList.clear();
				overwriteEmIdList.clear();
				fail = 1;
				skip = emIdList.size() - fail;
				break;
			}
		}
		
		BiResult brUpdate = sessionHelper.newBiResult("erpv4ext.Employee");
		suUpdate = brUpdate.getSelectUtil();
		for (String emid : genEmIdList) {
			UniLog.log1("generateOne emid:%s", emid);
			try {
				brUpdate.beginWork();
				lockTables();
				calPayment(emid, payStartDate, payEndDate, payDate);
				brUpdate.commitWork();
				if (overwriteEmIdList.contains(emid))
					overwrite++;
				else
					ok++;
			} catch (Exception ex) {
				UniLog.log(ex);
				err = StringUtils.defaultIfBlank(ex.getMessage(), ex.toString()) + String.format(" (Employee Code: %s)", emid);
				if (brUpdate != null)
					brUpdate.rollbackWork();
				fail++;
			}
		}
		brUpdate.close();

		StringBuilder sb = new StringBuilder();
		if (ok > 0)
			sb.append(String.format("- %d record added\n", ok));
		if (overwrite > 0)
			sb.append(String.format("- %d record updated\n", overwrite));
		if (fail > 0)
			sb.append(String.format("- %d record failed\n", fail));
		if (skip > 0)
			sb.append(String.format("- %d record skipped\n", skip));
		if (err != null)
			sb.insert(0, err + "\n");
		else if (sb.length() > 0)
			sb.insert(0, "Payroll record generated\n");
		else
			sb.append("No records generated");
		ZkUtil.msg(sb.toString());
	}

	//return: {1: add, 0: skip, 2: overwrite, 3: record exist for abort}
	private int validateOne(String emid, Date payStartDate, String whenExistStatus) throws Exception {
		return ZkUtil.getFirstTableRec(suQuery, "select pm_confirmstatus from paymentmaster where pm_eid = ? and pm_date = ?", 
														new Wherecl().appendArgument(emid).appendArgument(payStartDate)).map(throwFunction(tr -> {
			if (tr.getFieldInt("pm_confirmstatus") == 1) //已过账
				return 0;
			else if (Objects.equals(whenExistStatus, "Abort"))
				return 3;
			else if (Objects.equals(whenExistStatus, "Skip"))
				return 0;
			else
				return 2;
		})).orElse(1);
	}
	
	private void lockTables() throws Exception {
		UniLog.log("lockTables");
		suUpdate.executeUpdate("lock table paymentmaster in share mode", null);
		suUpdate.executeUpdate("lock table leave in share mode", null);
		suUpdate.executeUpdate("lock table attendance in share mode", null);
		suUpdate.executeUpdate("lock table employee in share mode", null);
	}
	
	private void calPayment(String p_eid, Date p_stdate, Date p_enddate, Date p_paydate) throws Exception {
		UniLog.log1("Calculating Payment for %s...", p_eid);
		if (incomeItemArr == null) {
			incomeItemArr = ZkUtil.getTableRecStream(suQuery, "select * from incomeitem").map(c -> {
				MainItem item = new MainItem(PAYMENT_TYPE_INCOME);
				item.code = c.getString("inci_code");
				item.formula = c.getString("inci_formula");
				item.iswage = Objects.equals(c.getString("inci_iswage"), "Y");
				item.istaxible = Objects.equals(c.getString("inci_istaxible"), "Y");
				item.isrelavent = Objects.equals(c.getString("inci_isrelavent"), "Y");
				item.overridable = Objects.equals(c.getString("inci_overridable"), "Y");
				return item;
			}).toArray(MainItem[]::new);
		}

		if (deductionItemArr == null) {
			deductionItemArr = ZkUtil.getTableRecStream(suQuery, "select * from deductionitem").map(c -> {
				MainItem item = new MainItem(PAYMENT_TYPE_DEDUCTION);
				item.code = c.getString("deci_code");
				item.formula = c.getString("deci_formula");
				item.iswage = Objects.equals(c.getString("deci_iswage"), "Y");
				item.istaxible = Objects.equals(c.getString("deci_istaxible"), "Y");
				item.isrelavent = Objects.equals(c.getString("deci_isrelavent"), "Y");
				item.overridable = Objects.equals(c.getString("deci_overridable"), "Y");
				return item;
			}).toArray(MainItem[]::new);
		}

		if (pensionItemArr == null) {
			pensionItemArr = ZkUtil.getTableRecStream(suQuery, "select * from pensionitem").map(c -> {
				MainItem item = new MainItem(PAYMENT_TYPE_PENSION);
				item.code = c.getString("peni_code");
				item.formula = c.getString("peni_formula");
				item.iswage = Objects.equals(c.getString("peni_iswage"), "Y");
				item.overridable = Objects.equals(c.getString("peni_overridable"), "Y");
				return item;
			}).toArray(MainItem[]::new);
		}

		PmdCtrl pmdc1 = new PmdCtrl();
		pmdc1.stdate = p_stdate;
		pmdc1.enddate = p_enddate;
		AtomicReference<PmdCtrl> pmdc2 = new AtomicReference<>();
		Stream.of(incomeItemArr, deductionItemArr, pensionItemArr).flatMap(Arrays::stream).forEach(item -> {
			if (Objects.equals(item.type, PAYMENT_TYPE_INCOME) && Objects.equals(item.code, "OT")) {
				//item.stdate = DateUtil.prevMonthStart(p_stdate);
				//item.enddate = DateUtil.prevMonthEnd(p_stdate);
				if (pmdc2.get() == null) {
					PmdCtrl pmdc = new PmdCtrl();
					pmdc.stdate = DateUtil.nextday(DateUtil.prevmonth(p_enddate, 2));
					pmdc.enddate = DateUtil.prevday(p_stdate);
					pmdc2.set(pmdc);
				}
				item.pmdc = pmdc2.get();
			} else
				item.pmdc = pmdc1;
			item.encodeFlag();
		});
		
		
		lpm = ZkUtil.getFirstTableRec(suQuery, "select * from employee where em_eid = ?", new Wherecl().appendArgument(p_eid)).map(throwFunction(tr -> {
			deletePayment(p_eid, p_stdate);
			PaymentMaster pm = new PaymentMaster();
			pm.eid = p_eid;
			pm.emstdate = tr.getFieldDate("em_stdate");
			pm.embirth = tr.getFieldDate("em_birth");
			pm.date = p_stdate;
			pm.edate = p_enddate;
			pm.paydate = p_paydate;
			return pm;
		})).orElseThrow(() -> new Exception("Employee record not found"));
		
		paymentDetList.clear();
		paymentItemList.clear();
		paymentItemDetList.clear();
		for (PmdCtrl pmdc : Arrays.asList(pmdc2.get(), pmdc1)) {
			if (pmdc == null)
				continue;
			int tmpmonthdays = (int)((pmdc.enddate.getTime() - pmdc.stdate.getTime()) / 86400000) + 1;
			ZkUtil.getTableRecStream(suQuery, "select * from emgrade where emg_eid = ? and emg_stdate <= ? and emg_enddate >= ?", 
					new Wherecl().appendArgument(p_eid).appendArgument(pmdc.enddate).appendArgument(pmdc.stdate)).forEach(throwConsumer(c -> {
				Date emg_stdate = c.getDate("emg_stdate");
				Date emg_enddate = c.getDate("emg_enddate");
				String emg_wgtype = c.getString("emg_wgtype");
				double emg_wage = c.getDouble("emg_wage");
				PaymentDet pyparam = new PaymentDet();
				paymentDetList.add(pyparam);
				pyparam.pmdc = pmdc;
				pyparam.monthdays = tmpmonthdays;
				pyparam.emgstdate = emg_stdate;
				Date tmpdate1 = emg_stdate.compareTo(pmdc.stdate) > 0 ? emg_stdate : pmdc.stdate;
				Date tmpdate2 = emg_enddate.compareTo(pmdc.enddate) < 0 ? emg_enddate : pmdc.enddate;
				pyparam.ndays = (int)((tmpdate2.getTime() - tmpdate1.getTime()) / 86400000) + 1;
				switch (emg_wgtype) {
				case "M":
					pyparam.basemsal = emg_wage;
					break;
				case "D":
					pyparam.basedsal = emg_wage;
					break;
				case "W":
					pyparam.basewsal = emg_wage;
					break;
				case "B":
					pyparam.basewsal = emg_wage / 2;
					break;
				case "H":
					pyparam.basehsal = emg_wage;
					break;
				}
				getAttendance(tmpdate1, tmpdate2, pyparam);
			}));
		}
		paymentDetList.stream().forEach(throwConsumer(pyparam -> {
			pyparam.addPaymentItem();
		}));
		paymentItemDetList.stream().forEach(throwConsumer(pmdi -> {
			pmdi.addToPaymentMasterPaymentDetPaymentItem();
		}));
		paymentItemList.stream().forEach(throwConsumer(pmi -> {
			pmi.calPensionOneType();
		}));
		insertData();
	}
	
	private void deletePayment(String p_eid, Date p_date) throws Exception {
		suUpdate.executeUpdate("delete from paymentitemdet where pmdi_eid = ? and pmdi_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
		suUpdate.executeUpdate("delete from paymentitem where pmi_eid = ? and pmi_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
		suUpdate.executeUpdate("delete from paymentdet where pmd_eid = ? and pmd_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
		suUpdate.executeUpdate("delete from paymentmaster where pm_eid = ? and pm_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
	}
	
	private void getAttendance(Date p_stdate, Date p_enddate, PaymentDet pyparam) throws Exception {
		pyparam.nopaydays = ZkUtil.getTableRecStream(suQuery, "select * from leave where lv_eid = ? and lv_sdate <= ? and lv_edate >= ? and lv_ltype = 'No Paid' order by lv_sdate, lv_edate", 
				new Wherecl().appendArgument(lpm.eid).appendArgument(p_enddate).appendArgument(p_stdate)).mapToInt(cell -> {
			int cc = 0;
			Date lv_sdate = cell.getDate("lv_sdate");
			Date lv_edate = cell.getDate("lv_edate");
			String lv_stfd = cell.getString("lv_stfd");
			String lv_enfd = cell.getString("lv_enfd");
			for (Date tmpdate1 = lv_sdate; tmpdate1.compareTo(lv_edate) <= 0; tmpdate1 = DateUtil.nextday(tmpdate1)) {
				if (tmpdate1.compareTo(p_stdate) >= 0 && tmpdate1.compareTo(p_enddate) <= 0) {
					if (tmpdate1.compareTo(lv_sdate) == 0) {
						if (StringUtils.equals(lv_stfd, "F"))
							cc += BiResultLeaveApplication.LEAVEUNIT_PER_DAY;
						else if (StringUtils.equals(lv_stfd, "H"))
							cc += BiResultLeaveApplication.LEAVEUNIT_PER_HALFDAY;
					} else if (tmpdate1.compareTo(lv_edate) == 0) {
						if (StringUtils.equals(lv_enfd, "F"))
							cc += BiResultLeaveApplication.LEAVEUNIT_PER_DAY;
						else if (StringUtils.equals(lv_enfd, "H"))
							cc += BiResultLeaveApplication.LEAVEUNIT_PER_HALFDAY;
					} else
						cc += BiResultLeaveApplication.LEAVEUNIT_PER_DAY;
				}
			}
			return cc;
		}).sum();

		ZkUtil.getTableRecStream(suQuery, "select * from attendance where at_eid = ? and at_date between ? and ?", 
				new Wherecl().appendArgument(lpm.eid).appendArgument(p_stdate).appendArgument(p_enddate)).forEach(cell -> {
			int at_late = cell.getInt("at_late");
			int at_reallate = cell.getInt("at_reallate");
			int at_ot = cell.getInt("at_ot");
			int at_sot = cell.getInt("at_sot");
			int at_othr = cell.getInt("at_othr");
			int at_nowork = cell.getInt("at_nowork");
			int at_wktime = cell.getInt("at_wktime");
			boolean at_manualot = Objects.equals(cell.getString("at_manualot"), "Y");
			boolean at_flag1 = Objects.equals(cell.getString("at_flag1"), "Y");
			if (at_late > 0) {
				if (at_reallate > 0) {
					pyparam.latedays++;
					pyparam.latemins += at_reallate;
				}
				if (at_late - at_reallate > 0) {
					pyparam.eldays++;
					pyparam.elmins += at_late - at_reallate;
				}
			}
			if (at_ot > 0) {
				pyparam.hotdays++;
				pyparam.hotmins += at_ot;
			}
			int cc = at_manualot ? at_othr : at_sot;
			if (cc > 0) {
				if (at_flag1) {
					pyparam.xotdays++;
					pyparam.xotmins += cc;
				} else {
					pyparam.otdays++;
					pyparam.otmins += cc;
				}
			}
			if (at_nowork > 0) {
				pyparam.nowdays++;
				pyparam.nowmins += at_nowork;
			}
			if (at_wktime > 0) {
				pyparam.wkdays++;
				pyparam.wkmins += at_wktime;
			}
		});
	}
	
	
	private void insertData() throws Exception {
		UniLog.log1("insertData eid:%s, date:%s", lpm.eid, lpm.date);
		for (PaymentItem pmi : paymentItemList) {
			//UniLog.log1("pmi:%s", GsonUtil.objToStr(pmi, PaymentItem.class));
			suUpdate.executeUpdate("insert into paymentitem (pmi_eid, pmi_date, pmi_type, pmi_code, pmi_stdate, pmi_enddate, pmi_ndays, pmi_flag, pmi_override, pmi_rincome, pmi_oincome, pmi_pension) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?)", new Wherecl()
						.appendArgument(lpm.eid).appendArgument(lpm.date).appendArgument(pmi.mainitem.type).appendArgument(pmi.mainitem.code).appendArgument(pmi.pmdc.stdate)
						.appendArgument(pmi.pmdc.enddate).appendArgument(pmi.ndays).appendArgument(pmi.mainitem.flag).appendArgument("")
						.appendArgument(pmi.rincome).appendArgument(pmi.oincome).appendArgument(pmi.pension));
		}
		for (PaymentItemDet pmdi : paymentItemDetList) {
			//UniLog.log1("pmdi:%s", GsonUtil.objToStr(pmdi, PaymentItemDet.class));
			if (pmdi.amount != 0) {
				PaymentItem pmi = pmdi.pmi;
				PaymentDet pyparam = pmdi.pyparam;
				PmdCtrl pmdc = pyparam.pmdc;
				suUpdate.executeUpdate("insert into paymentitemdet (pmdi_eid, pmdi_date, pmdi_type, pmdi_code, pmdi_stdate, pmdi_enddate, pmdi_emgstdate, pmdi_amount) " 
					+ "values (?,?,?,?,?,?,?,?)", new Wherecl()
						.appendArgument(lpm.eid).appendArgument(lpm.date).appendArgument(pmi.mainitem.type).appendArgument(pmi.mainitem.code).appendArgument(pmdc.stdate)
						.appendArgument(pmdc.enddate).appendArgument(pyparam.emgstdate).appendArgument(pmdi.amount));
			}
		}
		for (PaymentDet pmd : paymentDetList) {
			PmdCtrl pmdc = pmd.pmdc;
			//UniLog.log1("pmd:%s", GsonUtil.objToStr(pmd, PaymentDet.class));
			suUpdate.executeUpdate("insert into paymentdet (pmd_eid, pmd_date, pmd_stdate, pmd_enddate, pmd_monthdays, pmd_emgstdate, pmd_ndays, pmd_basemsal, pmd_basewsal, pmd_basedsal, pmd_basehsal, pmd_otdays, pmd_otmins, pmd_hotdays, pmd_hotmins, pmd_xotdays, pmd_xotmins, pmd_latedays, pmd_latemins, pmd_eldays, pmd_elmins, pmd_nowdays, pmd_nowmins, pmd_wkdays, pmd_wkmins, pmd_nopaydays, pmd_rincome, pmd_oincome, pmd_rdeduction, pmd_odeduction, pmd_epension, pmd_rpension) " 
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Wherecl()
					.appendArgument(lpm.eid).appendArgument(lpm.date).appendArgument(pmdc.stdate).appendArgument(pmdc.enddate).appendArgument(pmd.monthdays)
					.appendArgument(pmd.emgstdate).appendArgument(pmd.ndays).appendArgument(pmd.basemsal).appendArgument(pmd.basewsal).appendArgument(pmd.basedsal)
					.appendArgument(pmd.basehsal).appendArgument(pmd.otdays).appendArgument(pmd.otmins).appendArgument(pmd.hotdays).appendArgument(pmd.hotmins)
					.appendArgument(pmd.xotdays).appendArgument(pmd.xotmins).appendArgument(pmd.latedays).appendArgument(pmd.latemins).appendArgument(pmd.eldays)
					.appendArgument(pmd.elmins).appendArgument(pmd.nowdays).appendArgument(pmd.nowmins).appendArgument(pmd.wkdays).appendArgument(pmd.wkmins)
					.appendArgument(pmd.nopaydays).appendArgument(pmd.rincome).appendArgument(pmd.oincome).appendArgument(pmd.rdeduction) 
					.appendArgument(pmd.odeduction).appendArgument(0.0).appendArgument(0.0));
		}
		lpm.method = ZkUtil.getFirstTableRec(suQuery, "select pm_date, pm_method from paymentmaster where pm_eid = ? and pm_date < ? order by 1 desc", 
																	new Wherecl().appendArgument(lpm.eid).appendArgument(lpm.date))
							.map(throwFunction(tr -> tr.getFieldString("pm_method"))).orElse("");
		//UniLog.log1("pm:%s", GsonUtil.objToStr(pm, PaymentMaster.class));
		suUpdate.executeUpdate("insert into paymentmaster (pm_eid, pm_date, pm_edate, pm_paydate, pm_confirmstatus, pm_otdays, pm_otmins, pm_hotdays, pm_hotmins, pm_xotdays, pm_xotmins, pm_latedays, pm_latemins, pm_eldays, pm_elmins, pm_nowdays, pm_nowmins, pm_wkdays, pm_wkmins, pm_nopaydays, pm_rincome, pm_oincome, pm_epension, pm_rpension, pm_taxible, pm_eoawages, pm_method) " 
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Wherecl()
				.appendArgument(lpm.eid).appendArgument(lpm.date).appendArgument(lpm.edate).appendArgument(lpm.paydate).appendArgument(0)
				.appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0)
				.appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0)
				.appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0)
				.appendArgument(lpm.rincome).appendArgument(lpm.oincome).appendArgument(lpm.epension).appendArgument(lpm.rpension).appendArgument(lpm.taxible)
				.appendArgument(lpm.eoawages).appendArgument(lpm.method));
	}
	
	private double evalFormula(PaymentDet pyparam, String code, String formula) throws Exception {
		Parser parser = new Parser(0, formula);
		MyParserCallback cb = new MyParserCallback(pyparam);
		parser.setFunctInterface(cb);
		parser.setVarInterface(cb);
		double r = (Double)parser.evaluate();
		UniLog.log1("code:%s, formula:%s, r:%f", code, formula, r);
		return r;
	}

	public static char[] flagStrToCharArray(String s) {
		return Arrays.copyOf(StringUtils.defaultString(s).toCharArray(), MAX_PMI_FLAG);
	}
	
	private static class MainItem {
		String type;
		String flag;
		String code;
		String formula;
		boolean iswage;
		boolean istaxible;
		boolean isrelavent;
		boolean overridable;
		PmdCtrl pmdc;
		public MainItem(String type) {
			this.type = type;
		}
		public void encodeFlag() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < MAX_PMI_FLAG; i++) {
				switch (type) {
				case PAYMENT_TYPE_INCOME:
					switch (i) {
						case PAYAMOUNT_IDX_RINCOME:
							sb.append(iswage ? "+" : ".");
							break;
						case PAYAMOUNT_IDX_OINCOME:
							sb.append(!iswage ? "+" : ".");
							break;
						case PAYAMOUNT_IDX_TAXIBLE:
							sb.append(istaxible ? "+" : ".");
							break;
						case PAYAMOUNT_IDX_RELAVENT:
							sb.append(isrelavent ? "+" : ".");
							break;
						default:
							sb.append(".");
							break;
					}
					break;
				case PAYMENT_TYPE_DEDUCTION:
					switch (i) {
						case PAYAMOUNT_IDX_RINCOME:
							sb.append(iswage ? "-" : ".");
							break;
						case PAYAMOUNT_IDX_OINCOME:
							sb.append(!iswage ? "-" : ".");
							break;
						case PAYAMOUNT_IDX_TAXIBLE:
							sb.append(istaxible ? "-" : ".");
							break;
						case PAYAMOUNT_IDX_RELAVENT:
							sb.append(isrelavent ? "-" : ".");
							break;
						default:
							sb.append(".");
							break;
					}
					break;
				case PAYMENT_TYPE_PENSION:
					switch (i) {
						case PAYAMOUNT_IDX_EPENSION:
							sb.append(iswage ? "+" : ".");
							break;
						case PAYAMOUNT_IDX_RPENSION:
							sb.append(!iswage ? "+" : ".");
							break;
						default:
							sb.append(".");
							break;
					}
					break;
				}
			}
			flag = sb.toString();
		}
	}
	
	private static class PmdCtrl {
		Date stdate;
		Date enddate;
	}
	
	private static class PaymentMaster {
		String eid;
		Date emstdate;
		Date embirth;
		Date date;
		Date edate;
		Date paydate;
		String method;
		double epension, rpension;
		double rincome, oincome;
		double taxible, eoawages;
	}
	
	private class PaymentDet {
		PmdCtrl pmdc;
		Date emgstdate;
		int monthdays;
		int ndays, latedays, latemins, eldays, elmins, hotdays, hotmins, xotdays, xotmins, otdays, otmins, nowdays, nowmins, wkdays, wkmins, nopaydays;
		double basemsal, basedsal, basewsal, basehsal, rincome, oincome, rdeduction, odeduction;

		public void addPaymentItem() throws Exception {
			ZkUtil.getTableRecStream(suQuery, "select * from emincome where emic_eid = ? and emic_date = ?", 
					new Wherecl().appendArgument(lpm.eid).appendArgument(emgstdate)).forEach(c -> {
				addPaymentItem(incomeItemArr, c.getString("emic_code"), c.getString("emic_formula"));
			});
			ZkUtil.getTableRecStream(suQuery, "select * from emdeduction where emde_eid = ? and emde_date = ?", 
					new Wherecl().appendArgument(lpm.eid).appendArgument(emgstdate)).forEach(c -> {
				addPaymentItem(deductionItemArr, c.getString("emde_code"), c.getString("emde_formula"));
			});
			ZkUtil.getTableRecStream(suQuery, "select * from empension where empe_eid = ? and empe_date = ?", 
					new Wherecl().appendArgument(lpm.eid).appendArgument(emgstdate)).forEach(c -> {
				addPaymentItem(pensionItemArr, c.getString("empe_code"), null);
			});
		}

		private void addPaymentItem(MainItem[] mainitems, String code, String formula) {
			Arrays.stream(mainitems)
					.filter(item -> item.pmdc == pmdc && Objects.equals(item.code, code))
					.findFirst().ifPresent(mainitem -> {
				PaymentItem pmi = paymentItemList.stream()
						.filter(item -> item.pmdc == pmdc && item.mainitem == mainitem)
						.findFirst().orElse(null);
				if (pmi == null) {
					pmi = new PaymentItem();
					paymentItemList.add(pmi);
					pmi.pmdc = pmdc;
					pmi.mainitem = mainitem;
				}
				switch (mainitem.type) {
				case PAYMENT_TYPE_INCOME:
				case PAYMENT_TYPE_DEDUCTION:
					PaymentItemDet pmdi = new PaymentItemDet();
					paymentItemDetList.add(pmdi);
					pmdi.pmi = pmi;
					pmdi.pyparam = this;
					pmdi.formula = StringUtils.defaultIfBlank(formula, mainitem.formula);
					break;
				case PAYMENT_TYPE_PENSION:
					pmi.ndays += ndays;
					break;
				}
			});
		}
	}
	
	private class PaymentItem {
		PmdCtrl pmdc;
		MainItem mainitem;
		double rincome, oincome, pension;
		int ndays;

		public void calPensionOneType() throws Exception {
			if (!Objects.equals(mainitem.type, PAYMENT_TYPE_PENSION))
				return;
			double tmpf = evalFormula(null, mainitem.code, mainitem.formula);
			char[] flagc = flagStrToCharArray(mainitem.flag);
			switch (flagc[PAYAMOUNT_IDX_EPENSION]) {
				case '+':
					pension = tmpf;
					lpm.epension += tmpf;
					break;
				case '-':
					pension = -tmpf;
					lpm.epension -= tmpf;
					break;
			}
			switch (flagc[PAYAMOUNT_IDX_RPENSION]) {
				case '+':
					pension = tmpf;
					lpm.rpension += tmpf;
					break;
				case '-':
					pension = -tmpf;
					lpm.rpension -= tmpf;
					break;
			}
		}
	}

	private class PaymentItemDet {
		PaymentDet pyparam;
		PaymentItem pmi;
		String formula;
		double amount;

		public void addToPaymentMasterPaymentDetPaymentItem() throws Exception {
			MainItem mainitem = pmi.mainitem;
			if (!StringUtils.equalsAny(mainitem.type, PAYMENT_TYPE_INCOME, PAYMENT_TYPE_DEDUCTION))
				return;
			amount = evalFormula(pyparam, mainitem.code, formula);
			char[] flagc = flagStrToCharArray(mainitem.flag);
			switch (flagc[PAYAMOUNT_IDX_RINCOME]) {
				case '+':
					pyparam.rincome += amount;
					pmi.rincome += amount;
					lpm.rincome += amount;
					break;
				case '-':
					pyparam.rdeduction -= amount;
					pmi.rincome -= amount;
					lpm.rincome -= amount;
					break;
			}
			switch (flagc[PAYAMOUNT_IDX_OINCOME]) {
				case '+':
					pyparam.oincome += amount;
					pmi.oincome += amount;
					lpm.oincome += amount;
					break;
				case '-':
					pyparam.odeduction -= amount;
					pmi.oincome -= amount;
					lpm.oincome -= amount;
					break;
			}
			switch (flagc[PAYAMOUNT_IDX_EPENSION]) {
				case '+':
					pmi.pension += amount;
					lpm.epension += amount;
					break;
				case '-':
					pmi.pension -= amount;
					lpm.epension -= amount;
					break;
			}
			switch (flagc[PAYAMOUNT_IDX_RPENSION]) {
				case '+':
					pmi.pension += amount;
					lpm.rpension += amount;
					break;
				case '-':
					pmi.pension -= amount;
					lpm.rpension -= amount;
					break;
			}
			switch (flagc[PAYAMOUNT_IDX_TAXIBLE]) {
				case '+':
					lpm.taxible += amount;
					break;
				case '-':
					lpm.taxible -= amount;
					break;
			}
			switch (flagc[PAYAMOUNT_IDX_RELAVENT]) {
				case '+':
					lpm.eoawages += amount;
					break;
				case '-':
					lpm.eoawages -= amount;
					break;
			}
		}
	}
	
	private class MyParserCallback implements FunctionInterface, VariableInterface {
		PaymentDet pyparam;
		
		public MyParserCallback(PaymentDet pyparam) {
			this.pyparam = pyparam;
		}
		
		private Object evalVariable1(String p_varname) throws Exception {
			if (StringUtils.equals(p_varname, "wmins") && pyparam != null)
				return pyparam.wkmins;
			else if (StringUtils.equals(p_varname, "wdays") && pyparam != null)
				return pyparam.wkdays;
			else if (StringUtils.equals(p_varname, "msalary") && pyparam != null)
				return pyparam.basemsal;
			else if (StringUtils.equals(p_varname, "dsalary") && pyparam != null)
				return pyparam.basedsal;
			else if (StringUtils.equals(p_varname, "hsalary") && pyparam != null)
				return pyparam.basehsal;
			else if (StringUtils.equals(p_varname, "ndays") && pyparam != null)
				return pyparam.ndays;
			else if (StringUtils.equals(p_varname, "monthdays") && pyparam != null)
				return pyparam.monthdays;
			else if (StringUtils.equals(p_varname, "otmins") && pyparam != null)
				return pyparam.otmins;
			else if (StringUtils.equals(p_varname, "rincome"))
				return lpm.rincome;
			else if (StringUtils.equals(p_varname, "oincome"))
				return lpm.oincome;
			else if (StringUtils.equals(p_varname, "nopaydays") && pyparam != null)
				return pyparam.nopaydays / BiResultLeaveApplication.LEAVEUNIT_PER_DAY;
			else if (StringUtils.equals(p_varname, "joindate"))
				return lpm.emstdate.getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "penddate"))
				return lpm.edate.getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "pstdate"))
				return lpm.date.getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "over18date")) {
				if (!DateUtil.isValid(lpm.embirth)) {
					UniLog.log1("Error getting Employee Brithday %s %s", lpm.eid, lpm.embirth);
					return 0;
				}
				int mm = DateUtil.getMonth(lpm.embirth);
				int yy = DateUtil.getYear(lpm.embirth);
				int dd = DateUtil.getDay(lpm.embirth);
				if (mm == 2 && dd == 29)
					dd = 28;
				yy += 18;
				Date tmpdate1 = DateUtil.dateTimeStrToDate(String.format("%04d/%02d/%02d", yy, mm, dd));
				return tmpdate1.getTime() / 86400000;
			} 
			UniLog.log1("Variable %s not found", p_varname);
			return null;
		}

		@Override
		public Object evalVariable(String p_varname) throws Exception {
			Object r = evalVariable1(p_varname);
			UniLog.log1("p_varname:%s, r:%s", p_varname, r);
			return r;
		}

		@Override
		public Object evalVariable(String p_varname, int p_idx) throws Exception {
			UniLog.log1("Variable %s(%d) not found", p_varname, p_idx);
			return null;
		}

		@Override
		public Object evalFunction(String p_functName, Vector p_args) throws Exception {
			if (StringUtils.equals(p_functName, "xyz"))
				return 333.0;
			else if (StringUtils.equals(p_functName, "if")) {
				if (p_args.size() != 3) 
					return null;
				if (((Boolean) p_args.get(0)).booleanValue())
					return p_args.get(1);
				else
					return p_args.get(2);
			} else if (StringUtils.equals(p_functName, "f_penddate")) {
				int cc = ((Double)p_args.get(0)).intValue();
				Date tmpdate1;
				if (cc < 0)
					tmpdate1 = DateUtil.monthEnd(DateUtil.prevmonth(lpm.edate, -cc));
				else if (cc > 0)
					tmpdate1 = DateUtil.monthEnd(DateUtil.nextmonth(lpm.edate, cc));
				else
					tmpdate1 = lpm.edate;
				return tmpdate1.getTime() / 86400000;
			} else if (StringUtils.equals(p_functName, "f_rincome")) {
				int cc = ((Double)p_args.get(0)).intValue();
				Date tmpdate1;
				if (cc < 0) 
					tmpdate1 = DateUtil.monthStart(DateUtil.prevmonth(lpm.date, -cc));
				else if(cc > 0) 
					tmpdate1 = DateUtil.monthStart(DateUtil.nextmonth(lpm.date, cc));
				else 
					tmpdate1 = lpm.date;
				return ZkUtil.getFirstTableRec(suQuery, "select pm_rincome from paymentmaster where pm_eid = ? and pm_date = ?", 
																		new Wherecl().appendArgument(lpm.eid).appendArgument(tmpdate1))
							.map(throwFunction(tr -> tr.getFieldDouble("pm_rincome"))).orElse(0.0);
			}
			UniLog.log1("Function %s not found", p_functName);
			return null;
		}

		@Override
		public Object evalVariableRelative(String p_varname, int p_idx) throws Exception {
			throw new Exception("evalVariableRelative not supported");
		}
	}
}
