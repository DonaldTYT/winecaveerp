package com.uniinformation.zkbi.erpv4ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.exprpar.FunctionInterface;
import com.uniinformation.utils.exprpar.Parser;
import com.uniinformation.utils.exprpar.VariableInterface;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiHelpDialog;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiTranslateHelper;

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
	
	private IncomeItem[] incomeItemArr;
	private DeductionItem[] deductionItemArr;
	private PensionItem[] pensionItemArr;
	private List<PmdCtrl> pmdCtrlList = new ArrayList<PmdCtrl>();
	private List<PaymentDet> paymentDetList = new ArrayList<PaymentDet>();
	private List<PaymentItem> paymentItemList = new ArrayList<PaymentItem>();
	private List<PaymentItemDet> paymentItemDetList = new ArrayList<PaymentItemDet>();

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");

		final BiResult br = sessionHelper.newBiResult("erpv4ext.Employee");
		try {
			//fill Employee Code
			TableRec tr = br.getSelectUtil().getQueryResult("select em_eid, em_nickname, em_midname, em_surname, em_csurname, em_cmidname from employee order by em_eid");
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
			tr = br.getSelectUtil().getQueryResult("select dpmt_rg, dpmt_code, dpmt_name from deptmt order by dpmt_code");
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				final int rg = tr.getFieldInt("dpmt_rg");
				final String code = tr.getFieldString("dpmt_code");
				final String name = tr.getFieldString("dpmt_name");
				s2Dept.appendChild(new Listitem(code + " - " + name){{setValue(rg);}});
			}
			tr = br.getSelectUtil().getQueryResult("select pp_start, pp_end from payperiod order by pp_start desc");
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
			rd.setLabel(ZkBiTranslateHelper.getText(sessionHelper, "ERPV4EXT.PAYMENTGEN.RD_WHENEX_" + v.toUpperCase(), "OPTION", v));
		}
		winPaymentGen.setTitle(ZkBiTranslateHelper.getText(sessionHelper, "ZkBiPaymentGen_01", "MENU", "Payment Generation"));

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
					TableRec tr = br.getSelectUtil().getQueryResult("select emg_eid from emgrade where emg_deptrg = ? and emg_stdate <= ? and emg_enddate >= ?", 
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
								TableRec tr = br.getSelectUtil().getQueryResult("select em_eid from employee where em_stdate <= ? and (em_enddate = '' or em_enddate >= ?) order by em_eid", 
										new Wherecl().appendArgument(payEndDate).appendArgument(payStartDate));
								for (int i = 0; i < tr.getRecordCount(); i++) {
									tr.setRecPointer(i);
									emIdList.add(tr.getFieldString("em_eid"));
								}
								startGenerate(br, emIdList, payStartDate, payEndDate, payDate, whenExistStatus);
							}
						}
					});
				} else
					startGenerate(br, emIdList, payStartDate, payEndDate, payDate, whenExistStatus);
			}
		});
	}
	
	private void startGenerate(BiResult brQuery, Set<String> emIdList, Date payStartDate, Date payEndDate, Date payDate, String whenExistStatus) {
		UniLog.log1("emIdList size:%s, payStartDate:%s, payEndDate:%s, payDate:%s, whenExistStatus:%s", emIdList.size(), payStartDate, payEndDate, payDate, whenExistStatus);
		int ok = 0, skip = 0, overwrite = 0, fail = 0;
		String err = null;
		List<String> genEmIdList = new ArrayList<String>();
		Set<String> overwriteEmIdList = new HashSet<String>();
		for (String emid : emIdList) {
			UniLog.log1("validateOne emid:%s", emid);
			try {
				switch (validateOne(brQuery, emid, payStartDate, whenExistStatus)) {
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
		
		for (String emid : genEmIdList) {
			UniLog.log1("generateOne emid:%s", emid);
			BiResult br = null;
			try {
				br = sessionHelper.newBiResult("erpv4ext.Employee");
				generateOne(brQuery, br, emid, payStartDate, payEndDate, payDate);
				if (overwriteEmIdList.contains(emid))
					overwrite++;
				else
					ok++;
			}
			catch (Exception ex) {
				UniLog.log(ex);
				err = StringUtils.defaultIfBlank(ex.getMessage(), ex.toString()) + String.format(" (Employee Code: %s)", emid);
				if (br != null)
					br.rollbackWork();
				fail++;
			}
		}
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
	private int validateOne(BiResult brQuery, String emid, Date payStartDate, String whenExistStatus) throws Exception {
		TableRec tr = brQuery.getSelectUtil().getQueryResult("select pm_confirmstatus from paymentmaster where pm_eid = ? and pm_date = ?", 
				new Wherecl().appendArgument(emid).appendArgument(payStartDate));
		if (tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			int pmConfirmStatus = tr.getFieldInt("pm_confirmstatus");
			if (pmConfirmStatus == 1) //已过账
				return 0;
			if (StringUtils.equals(whenExistStatus, "Abort"))
				return 3;
			else if (StringUtils.equals(whenExistStatus, "Skip"))
				return 0;
			return 2;
		}
		return 1;
	}
	
	private void generateOne(BiResult brQuery, BiResult br, String emid, Date payStartDate, Date payEndDate, Date payDate) throws Exception {
		SelectUtil suQuery = brQuery.getSelectUtil();
		SelectUtil su = br.getSelectUtil();
		br.beginWork();
		lockTables(su);
		calPayment(suQuery, su, emid, payStartDate, payEndDate, payDate);
		br.commitWork();
	}
	
	private void lockTables(SelectUtil su) throws Exception {
		UniLog.log1("lockTables");
		su.executeUpdate("lock table paymentmaster in share mode", null);
		su.executeUpdate("lock table leave in share mode", null);
		su.executeUpdate("lock table attendance in share mode", null);
		su.executeUpdate("lock table employee in share mode", null);
	}
	
	private void calPayment(SelectUtil suQuery, SelectUtil su, String p_eid, Date p_stdate, Date p_enddate, Date p_paydate) throws Exception {
		UniLog.log1("Calculating Payment for %s...", p_eid);
		TableRec tr = suQuery.getQueryResult("select * from incomeitem");
		incomeItemArr = new IncomeItem[tr.getRecordCount()];
		for (int i = 0; i < incomeItemArr.length; i++) {
			tr.setRecPointer(i);
			IncomeItem item = incomeItemArr[i] = new IncomeItem();
			item.code = tr.getFieldString("inci_code");
			item.formula = tr.getFieldString("inci_formula");
			item.iswage = StringUtils.equals(tr.getFieldString("inci_iswage"), "Y");
			item.istaxible = StringUtils.equals(tr.getFieldString("inci_istaxible"), "Y");
			item.isrelavent = StringUtils.equals(tr.getFieldString("inci_isrelavent"), "Y");
			item.overridable = StringUtils.equals(tr.getFieldString("inci_overridable"), "Y");
		}
		tr = suQuery.getQueryResult("select * from deductionitem");
		deductionItemArr = new DeductionItem[tr.getRecordCount()];
		for (int i = 0; i < deductionItemArr.length; i++) {
			tr.setRecPointer(i);
			DeductionItem item = deductionItemArr[i] = new DeductionItem();
			item.code = tr.getFieldString("deci_code");
			item.formula = tr.getFieldString("deci_formula");
			item.iswage = StringUtils.equals(tr.getFieldString("deci_iswage"), "Y");
			item.istaxible = StringUtils.equals(tr.getFieldString("deci_istaxible"), "Y");
			item.isrelavent = StringUtils.equals(tr.getFieldString("deci_isrelavent"), "Y");
			item.overridable = StringUtils.equals(tr.getFieldString("deci_overridable"), "Y");
		}
		tr = suQuery.getQueryResult("select * from pensionitem");
		pensionItemArr = new PensionItem[tr.getRecordCount()];
		for (int i = 0; i < pensionItemArr.length; i++) {
			tr.setRecPointer(i);
			PensionItem item = pensionItemArr[i] = new PensionItem();
			item.code = tr.getFieldString("peni_code");
			item.formula = tr.getFieldString("peni_formula");
			item.iswage = StringUtils.equals(tr.getFieldString("peni_iswage"), "Y");
			item.overridable = StringUtils.equals(tr.getFieldString("peni_overridable"), "Y");
		}
		pmdCtrlList.clear();
		for (IncomeItem item : incomeItemArr) {
			if (StringUtils.equals(item.code, "OT")) {
				item.stdate = DateUtil.prevMonthStart(p_stdate);
				item.enddate = DateUtil.prevMonthEnd(p_stdate);
				PmdCtrl pdmc = new PmdCtrl();
				pdmc.stdate = item.stdate;
				pdmc.enddate = item.enddate;
				pmdCtrlList.add(0, pdmc);
			} else {
				item.stdate = p_stdate;
				item.enddate = p_enddate;
			}
			item.flag = encodePmflagFromIncome(item);
		}
		for (DeductionItem item : deductionItemArr) {
			item.stdate = p_stdate;
			item.enddate = p_enddate;
			item.flag = encodePmflagFromDeduction(item);
		}
		for (PensionItem item : pensionItemArr)
			item.flag = encodePmflagFromPension(item);
		PmdCtrl pdmc = new PmdCtrl();
		pdmc.stdate = p_stdate;
		pdmc.enddate = p_enddate;
		pmdCtrlList.add(0, pdmc);
		
		
		tr = suQuery.getQueryResult("select * from employee where em_eid = ?", new Wherecl().appendArgument(p_eid));
		if (tr.getRecordCount() == 0)
			throw new Exception("Employee record not found");
		tr.setRecPointer(0);
		Employee lem = new Employee();
		lem.eid = p_eid;
		lem.stdate = tr.getFieldDate("em_stdate");
		lem.birth = tr.getFieldDate("em_birth");
		
		//paymentmaster, paymentdet, paymentitem, paymentitemdet
		deletePayment(su, p_eid, p_stdate);
		PaymentMaster lpm = new PaymentMaster();
		lpm.eid = p_eid;
		lpm.date = p_stdate;
		lpm.edate = p_enddate;
		lpm.paydate = p_paydate;
		paymentDetList.clear();
		paymentItemList.clear();
		paymentItemDetList.clear();
		for (PmdCtrl pmdc : pmdCtrlList) {
			tr = suQuery.getQueryResult("select * from emgrade where emg_eid = ? and emg_stdate <= ? and emg_enddate >= ?", 
					new Wherecl().appendArgument(p_eid).appendArgument(pmdc.enddate).appendArgument(pmdc.stdate));
			Date tmpdate1 = DateUtil.monthStart(pmdc.stdate);
			Date tmpdate2 = DateUtil.monthEnd(pmdc.enddate);
			int tmpmonthdays = (int)((tmpdate2.getTime() - tmpdate1.getTime()) / 86400000) + 1;
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				Date emg_stdate = tr.getFieldDate("emg_stdate");
				Date emg_enddate = tr.getFieldDate("emg_enddate");
				String emg_wgtype = tr.getFieldString("emg_wgtype");
				double emg_wage = tr.getFieldDouble("emg_wage");
				PaymentDet pyparam = new PaymentDet();
				paymentDetList.add(pyparam);
				pyparam.eid = lpm.eid;
				pyparam.date = lpm.date;
				pyparam.stdate = pmdc.stdate;
				pyparam.enddate = pmdc.enddate;
				pyparam.monthdays = tmpmonthdays;
				pyparam.emgstdate = emg_stdate;
				if (emg_stdate.compareTo(pmdc.stdate) > 0)
					tmpdate1 = emg_stdate;
				else 
					tmpdate1 = pmdc.stdate;
				if (emg_enddate.compareTo(pmdc.enddate) < 0)
					tmpdate2 = emg_enddate;
				else 
					tmpdate2 = pmdc.enddate;
				pyparam.ndays = (int)((tmpdate2.getTime() - tmpdate1.getTime()) / 86400000) + 1;
				if (StringUtils.equals(emg_wgtype, "M"))
					pyparam.basemsal = emg_wage;
				else if (StringUtils.equals(emg_wgtype, "D"))
					pyparam.basedsal = emg_wage;
				else if (StringUtils.equals(emg_wgtype, "W"))
					pyparam.basewsal = emg_wage;
				else if (StringUtils.equals(emg_wgtype, "B"))
					pyparam.basewsal = emg_wage / 2;
				else if (StringUtils.equals(emg_wgtype, "H"))
					pyparam.basehsal = emg_wage;
				getAttendance(suQuery, tmpdate1, tmpdate2, pyparam);
				calIncomeOnePeriod(suQuery, lem, lpm, pyparam);
			}
		}
		for (PmdCtrl pmdc : pmdCtrlList) {
			int j = -1;
			for (int i = 0; i < paymentDetList.size(); i++) {
				PaymentDet item = paymentDetList.get(i);
				if (item.stdate.compareTo(pmdc.stdate) == 0 && item.enddate.compareTo(pmdc.enddate) == 0) {
					j = i;
					break;
				}
			}
			if (j >= 0) {
				for (; j < paymentDetList.size(); j++) {
					PaymentDet pyparam = paymentDetList.get(j);
					if (pyparam.stdate.compareTo(pmdc.stdate) != 0 || pyparam.enddate.compareTo(pmdc.enddate) != 0) 
						break;
					calDeductionOnePeriod(suQuery, lem, lpm, pyparam);
				}
			}
		}
		int j = -1;
		for (int i = 0; i < paymentDetList.size(); i++) {
			PaymentDet item = paymentDetList.get(i);
			if (item.stdate.compareTo(p_stdate) == 0 && item.enddate.compareTo(p_enddate) == 0) {
				j = i;
				break;
			}
		}
		if (j >= 0) {
			for (; j < paymentDetList.size(); j++) {
				PaymentDet pyparam = paymentDetList.get(j);
				if (pyparam.stdate.compareTo(p_stdate) != 0 || pyparam.enddate.compareTo(p_enddate) != 0)
					break;
				countPensionOnePeriod(suQuery, pyparam);
			}
			for (PaymentItem pmi : paymentItemList) {
				if (StringUtils.equals(pmi.type, PAYMENT_TYPE_PENSION))
					calPensionOneType(suQuery, lem, lpm, pmi);
			}
		}
		insertData(suQuery, su, lpm);
	}
	
	private static void deletePayment(SelectUtil su, String p_eid, Date p_date) throws Exception {
		su.executeUpdate("delete from paymentitemdet where pmdi_eid = ? and pmdi_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
		su.executeUpdate("delete from paymentitem where pmi_eid = ? and pmi_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
		su.executeUpdate("delete from paymentdet where pmd_eid = ? and pmd_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
		su.executeUpdate("delete from paymentmaster where pm_eid = ? and pm_date = ?", new Wherecl().appendArgument(p_eid).appendArgument(p_date));
	}
	
	private static void getAttendance(SelectUtil suQuery, Date p_stdate, Date p_enddate, PaymentDet pyparam) throws Exception {
		TableRec tr = suQuery.getQueryResult("select * from leave where lv_eid = ? and lv_sdate <= ? and lv_edate >= ? and lv_ltype = 'No Paid' order by lv_sdate, lv_edate", 
				new Wherecl().appendArgument(pyparam.eid).appendArgument(p_enddate).appendArgument(p_stdate));
		int cc = 0;
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			Date lv_sdate = tr.getFieldDate("lv_sdate");
			Date lv_edate = tr.getFieldDate("lv_edate");
			String lv_stfd = tr.getFieldString("lv_stfd");
			String lv_enfd = tr.getFieldString("lv_enfd");
			for (Date tmpdate1 = lv_sdate; tmpdate1.compareTo(lv_edate) <= 0; tmpdate1 = DateUtil.nextday(tmpdate1)) {
				if (tmpdate1.compareTo(p_stdate) >= 0 && tmpdate1.compareTo(p_enddate) <= 0) {
					if (tmpdate1.compareTo(lv_sdate) == 0) {
						if (StringUtils.equals(lv_stfd, "F"))
							cc += LeaveApplication.LEAVEUNIT_PER_DAY;
						else if (StringUtils.equals(lv_stfd, "H"))
							cc += LeaveApplication.LEAVEUNIT_PER_HALFDAY;
					} else if (tmpdate1.compareTo(lv_edate) == 0) {
						if (StringUtils.equals(lv_enfd, "F"))
							cc += LeaveApplication.LEAVEUNIT_PER_DAY;
						else if (StringUtils.equals(lv_enfd, "H"))
							cc += LeaveApplication.LEAVEUNIT_PER_HALFDAY;
					} else
						cc += LeaveApplication.LEAVEUNIT_PER_DAY;
				}
			}
		}
		pyparam.nopaydays = cc;

		tr = suQuery.getQueryResult("select * from attendance where at_eid = ? and at_date between ? and ?", 
				new Wherecl().appendArgument(pyparam.eid).appendArgument(p_stdate).appendArgument(p_enddate));
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			int at_late = tr.getFieldInt("at_late");
			int at_reallate = tr.getFieldInt("at_reallate");
			int at_ot = tr.getFieldInt("at_ot");
			int at_sot = tr.getFieldInt("at_sot");
			int at_othr = tr.getFieldInt("at_othr");
			int at_nowork = tr.getFieldInt("at_nowork");
			int at_wktime = tr.getFieldInt("at_wktime");
			boolean at_manualot = StringUtils.equals(tr.getFieldString("at_manualot"), "Y");
			boolean at_flag1 = StringUtils.equals(tr.getFieldString("at_flag1"), "Y");
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
			if (at_manualot)
				cc = at_othr;
			else
				cc = at_sot;
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
		}
	}
	
	//create paymentitem, paymentitemdet for Income Item
	private void calIncomeOnePeriod(SelectUtil suQuery, Employee lem, PaymentMaster lpm, PaymentDet pyparam) throws Exception {
		TableRec tr = suQuery.getQueryResult("select * from emincome where emic_eid = ? and emic_date = ?", 
				new Wherecl().appendArgument(pyparam.eid).appendArgument(pyparam.emgstdate));
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			String emic_code = tr.getFieldString("emic_code");
			String emic_formula = tr.getFieldString("emic_formula");
			IncomeItem inci = null;
			for (IncomeItem item : incomeItemArr) {
				if (item.stdate.compareTo(pyparam.stdate) == 0 && item.enddate.compareTo(pyparam.enddate) == 0 && StringUtils.equals(item.code, emic_code)) {
					inci = item;
					break;
				}
			}
			if (inci != null) {
				PaymentItem pmi = null;
				for (PaymentItem item : paymentItemList) {
					if (item.date.compareTo(pyparam.date) == 0 && StringUtils.equals(item.type, PAYMENT_TYPE_INCOME) && StringUtils.equals(item.code, emic_code)) {
						pmi = item;
						break;
					}
				}
				if (pmi == null) {
					pmi = new PaymentItem();
					paymentItemList.add(pmi);
					pmi.eid = pyparam.eid;
					pmi.date = pyparam.date;
					pmi.stdate = pyparam.stdate;
					pmi.enddate = pyparam.enddate;
					pmi.type = PAYMENT_TYPE_INCOME;
					pmi.code = inci.code;
					pmi.flag = inci.flag;
					pmi.optional = inci.overridable;
				}
				PaymentItemDet pmdi = new PaymentItemDet();
				paymentItemDetList.add(pmdi);
				pmdi.eid = pmi.eid;
				pmdi.date = pmi.date;
				pmdi.stdate = pmi.stdate;
				pmdi.enddate = pmi.enddate;
				pmdi.type = pmi.type;
				pmdi.code = pmi.code;
				pmdi.emgstdate = pyparam.emgstdate;
				//calculate paymentitemdetail from formula
				pmdi.amount = evalFormula(suQuery, lem, lpm, pyparam, inci.code, StringUtils.defaultIfBlank(emic_formula, inci.formula));
				//sum to paymentmaster
				//sum to paymentdet
				//sum to paymentitem
				addToPaymentMasterPaymentDetPaymentItem(lpm, pyparam, pmi, pmdi);
			}
		}
	}
	
	//create paymentitem, paymentitemdet for Deduction Item
	private void calDeductionOnePeriod(SelectUtil su, Employee lem, PaymentMaster lpm, PaymentDet pyparam) throws Exception {
		TableRec tr = su.getQueryResult("select * from emdeduction where emde_eid = ? and emde_date = ?", 
				new Wherecl().appendArgument(pyparam.eid).appendArgument(pyparam.emgstdate));
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			String emde_code = tr.getFieldString("emde_code");
			String emde_formula = tr.getFieldString("emde_formula");
			DeductionItem deci = null;
			for (DeductionItem item : deductionItemArr) {
				if (item.stdate.compareTo(pyparam.stdate) == 0 && item.enddate.compareTo(pyparam.enddate) == 0 && StringUtils.equals(item.code, emde_code)) {
					deci = item;
					break;
				}
			}
			if (deci != null) {
				PaymentItem pmi = null;
				for (PaymentItem item : paymentItemList) {
					if (item.date.compareTo(pyparam.date) == 0 && StringUtils.equals(item.type, PAYMENT_TYPE_DEDUCTION) && StringUtils.equals(item.code, emde_code)) {
						pmi = item;
						break;
					}
				}
				if (pmi == null) {
					pmi = new PaymentItem();
					paymentItemList.add(pmi);
					pmi.eid = pyparam.eid;
					pmi.date = pyparam.date;
					pmi.stdate = pyparam.stdate;
					pmi.enddate = pyparam.enddate;
					pmi.type = PAYMENT_TYPE_DEDUCTION;
					pmi.code = deci.code;
					pmi.flag = deci.flag;
					pmi.optional = deci.overridable;
				}
				PaymentItemDet pmdi = new PaymentItemDet();
				paymentItemDetList.add(pmdi);
				pmdi.eid = pmi.eid;
				pmdi.date = pmi.date;
				pmdi.stdate = pmi.stdate;
				pmdi.enddate = pmi.enddate;
				pmdi.type = pmi.type;
				pmdi.code = pmi.code;
				pmdi.emgstdate = pyparam.emgstdate;
				//calculate paymentitemdetail from formula
				pmdi.amount = evalFormula(su, lem, lpm, pyparam, deci.code, StringUtils.defaultIfBlank(emde_formula, deci.formula));
				//sum to paymentmaster
				//sum to paymentdet
				//sum to paymentitem
				addToPaymentMasterPaymentDetPaymentItem(lpm, pyparam, pmi, pmdi);
			}
		}
	}
	
	//create paymentitem for Pension Item
	private void countPensionOnePeriod(SelectUtil su, PaymentDet pyparam) throws Exception {
		TableRec tr = su.getQueryResult("select * from empension where empe_eid = ? and empe_date = ?", 
				new Wherecl().appendArgument(pyparam.eid).appendArgument(pyparam.emgstdate));
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			String empe_code = tr.getFieldString("empe_code");
			PensionItem peni = null;
			for (PensionItem item : pensionItemArr) {
				if (StringUtils.equals(item.code, empe_code)) {
					peni = item;
					break;
				}
			}
			if (peni != null) {
				PaymentItem pmi = null;
				for (PaymentItem item : paymentItemList) {
					if (item.date.compareTo(pyparam.date) == 0 && StringUtils.equals(item.type, PAYMENT_TYPE_PENSION) && StringUtils.equals(item.code, empe_code)) {
						pmi = item;
						break;
					}
				}
				if (pmi == null) {
					pmi = new PaymentItem();
					paymentItemList.add(pmi);
					pmi.eid = pyparam.eid;
					pmi.date = pyparam.date;
					pmi.stdate = pyparam.stdate;
					pmi.enddate = pyparam.enddate;
					pmi.type = PAYMENT_TYPE_PENSION;
					pmi.code = peni.code;
					pmi.flag = peni.flag;
					pmi.optional = peni.overridable;
				}
				pmi.ndays += pyparam.ndays;
			}
		}
	}
	
	private void calPensionOneType(SelectUtil su, Employee lem, PaymentMaster lpm, PaymentItem pmi) throws Exception {
		PensionItem peni = null;
		for (PensionItem item : pensionItemArr) {
			if (StringUtils.equals(item.code, pmi.code)) {
				peni = item;
				break;
			}
		}
		if (peni != null) {
			double tmpf = evalFormula(su, lem, lpm, null, peni.code, peni.formula);
			char[] flagc = flagStrToCharArray(pmi.flag);
			switch (flagc[PAYAMOUNT_IDX_EPENSION]) {
				case '+':
					pmi.pension = tmpf;
					lpm.epension += tmpf;
					break;
				case '-':
					pmi.pension = -tmpf;
					lpm.epension -= tmpf;
					break;
			}
			switch (flagc[PAYAMOUNT_IDX_RPENSION]) {
				case '+':
					pmi.pension = tmpf;
					lpm.rpension += tmpf;
					break;
				case '-':
					pmi.pension = -tmpf;
					lpm.rpension -= tmpf;
					break;
			}
		}
	}
	
	private static void addToPaymentMasterPaymentDetPaymentItem(PaymentMaster lpm, PaymentDet pyparam, PaymentItem pmi, PaymentItemDet pmdi) {
		char[] flagc = flagStrToCharArray(pmi.flag);
		switch (flagc[PAYAMOUNT_IDX_RINCOME]) {
			case '+':
				pyparam.rincome += pmdi.amount;
				pmi.rincome += pmdi.amount;
				lpm.rincome += pmdi.amount;
				break;
			case '-':
				pyparam.rdeduction -= pmdi.amount;
				pmi.rincome -= pmdi.amount;
				lpm.rincome -= pmdi.amount;
				break;
		}
		switch (flagc[PAYAMOUNT_IDX_OINCOME]) {
			case '+':
				pyparam.oincome += pmdi.amount;
				pmi.oincome += pmdi.amount;
				lpm.oincome += pmdi.amount;
				break;
			case '-':
				pyparam.odeduction -= pmdi.amount;
				pmi.oincome -= pmdi.amount;
				lpm.oincome -= pmdi.amount;
				break;
		}
		switch (flagc[PAYAMOUNT_IDX_EPENSION]) {
			case '+':
				pmi.pension += pmdi.amount;
				lpm.epension += pmdi.amount;
				break;
			case '-':
				pmi.pension -= pmdi.amount;
				lpm.epension -= pmdi.amount;
				break;
		}
		switch (flagc[PAYAMOUNT_IDX_RPENSION]) {
			case '+':
				pmi.pension += pmdi.amount;
				lpm.rpension += pmdi.amount;
				break;
			case '-':
				pmi.pension -= pmdi.amount;
				lpm.rpension -= pmdi.amount;
				break;
		}
		switch (flagc[PAYAMOUNT_IDX_TAXIBLE]) {
			case '+':
				lpm.taxible += pmdi.amount;
				break;
			case '-':
				lpm.taxible -= pmdi.amount;
				break;
		}
		switch (flagc[PAYAMOUNT_IDX_RELAVENT]) {
			case '+':
				lpm.eoawages += pmdi.amount;
				break;
			case '-':
				lpm.eoawages -= pmdi.amount;
				break;
		}
	}
	
	private void insertData(SelectUtil suQuery, SelectUtil su, PaymentMaster pm) throws Exception {
		UniLog.log1("insertData eid:%s, date:%s", pm.eid, pm.date);
		for (PaymentItem pmi : paymentItemList) {
			//UniLog.log1("pmi:%s", GsonUtil.objToStr(pmi, PaymentItem.class));
			su.executeUpdate("insert into paymentitem (pmi_eid, pmi_date, pmi_type, pmi_code, pmi_stdate, pmi_enddate, pmi_ndays, pmi_flag, pmi_override, pmi_rincome, pmi_oincome, pmi_pension) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?)", new Wherecl()
						.appendArgument(pmi.eid).appendArgument(pmi.date).appendArgument(pmi.type).appendArgument(pmi.code).appendArgument(pmi.stdate)
						.appendArgument(pmi.enddate).appendArgument(pmi.ndays).appendArgument(pmi.flag).appendArgument("")
						.appendArgument(pmi.rincome).appendArgument(pmi.oincome).appendArgument(pmi.pension));
		}
		for (PaymentItemDet pmdi : paymentItemDetList) {
			//UniLog.log1("pmdi:%s", GsonUtil.objToStr(pmdi, PaymentItemDet.class));
			if (pmdi.amount != 0) {
				su.executeUpdate("insert into paymentitemdet (pmdi_eid, pmdi_date, pmdi_type, pmdi_code, pmdi_stdate, pmdi_enddate, pmdi_emgstdate, pmdi_amount) " 
					+ "values (?,?,?,?,?,?,?,?)", new Wherecl()
						.appendArgument(pmdi.eid).appendArgument(pmdi.date).appendArgument(pmdi.type).appendArgument(pmdi.code).appendArgument(pmdi.stdate)
						.appendArgument(pmdi.enddate).appendArgument(pmdi.emgstdate).appendArgument(pmdi.amount));
			}
		}
		for (PaymentDet pmd : paymentDetList) {
			//UniLog.log1("pmd:%s", GsonUtil.objToStr(pmd, PaymentDet.class));
			su.executeUpdate("insert into paymentdet (pmd_eid, pmd_date, pmd_stdate, pmd_enddate, pmd_monthdays, pmd_emgstdate, pmd_ndays, pmd_basemsal, pmd_basewsal, pmd_basedsal, pmd_basehsal, pmd_otdays, pmd_otmins, pmd_hotdays, pmd_hotmins, pmd_xotdays, pmd_xotmins, pmd_latedays, pmd_latemins, pmd_eldays, pmd_elmins, pmd_nowdays, pmd_nowmins, pmd_wkdays, pmd_wkmins, pmd_nopaydays, pmd_rincome, pmd_oincome, pmd_rdeduction, pmd_odeduction, pmd_epension, pmd_rpension) " 
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Wherecl()
					.appendArgument(pmd.eid).appendArgument(pmd.date).appendArgument(pmd.stdate).appendArgument(pmd.enddate).appendArgument(pmd.monthdays)
					.appendArgument(pmd.emgstdate).appendArgument(pmd.ndays).appendArgument(pmd.basemsal).appendArgument(pmd.basewsal).appendArgument(pmd.basedsal)
					.appendArgument(pmd.basehsal).appendArgument(pmd.otdays).appendArgument(pmd.otmins).appendArgument(pmd.hotdays).appendArgument(pmd.hotmins)
					.appendArgument(pmd.xotdays).appendArgument(pmd.xotmins).appendArgument(pmd.latedays).appendArgument(pmd.latemins).appendArgument(pmd.eldays)
					.appendArgument(pmd.elmins).appendArgument(pmd.nowdays).appendArgument(pmd.nowmins).appendArgument(pmd.wkdays).appendArgument(pmd.wkmins)
					.appendArgument(pmd.nopaydays).appendArgument(pmd.rincome).appendArgument(pmd.oincome).appendArgument(pmd.rdeduction) 
					.appendArgument(pmd.odeduction).appendArgument(0.0).appendArgument(0.0));
		}
		TableRec tr = su.getQueryResult("select pm_date, pm_method from paymentmaster where pm_eid = ? and pm_date < ? order by 1 desc", 
				new Wherecl().appendArgument(pm.eid).appendArgument(pm.date));
		pm.method = "";
		if (tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			String tmpmethod = tr.getFieldString("pm_method");
			pm.method = tmpmethod;
		}
		//UniLog.log1("pm:%s", GsonUtil.objToStr(pm, PaymentMaster.class));
		su.executeUpdate("insert into paymentmaster (pm_eid, pm_date, pm_edate, pm_paydate, pm_confirmstatus, pm_otdays, pm_otmins, pm_hotdays, pm_hotmins, pm_xotdays, pm_xotmins, pm_latedays, pm_latemins, pm_eldays, pm_elmins, pm_nowdays, pm_nowmins, pm_wkdays, pm_wkmins, pm_nopaydays, pm_rincome, pm_oincome, pm_epension, pm_rpension, pm_taxible, pm_eoawages) " 
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Wherecl()
				.appendArgument(pm.eid).appendArgument(pm.date).appendArgument(pm.edate).appendArgument(pm.paydate).appendArgument(0)
				.appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0)
				.appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0)
				.appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0).appendArgument(0)
				.appendArgument(pm.rincome).appendArgument(pm.oincome).appendArgument(pm.epension).appendArgument(pm.rpension).appendArgument(pm.taxible)
				.appendArgument(pm.eoawages));
	}
	
	private static String encodePmflagFromIncome(IncomeItem item) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < MAX_PMI_FLAG; i++) {
			switch (i) {
				case PAYAMOUNT_IDX_RINCOME:
					sb.append(item.iswage ? "+" : ".");
					break;
				case PAYAMOUNT_IDX_OINCOME:
					sb.append(!item.iswage ? "+" : ".");
					break;
				case PAYAMOUNT_IDX_TAXIBLE:
					sb.append(item.istaxible ? "+" : ".");
					break;
				case PAYAMOUNT_IDX_RELAVENT:
					sb.append(item.isrelavent ? "+" : ".");
					break;
				default:
					sb.append(".");
					break;
			}
		}
		return sb.toString();
	}
	
	private static String encodePmflagFromDeduction(DeductionItem item) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < MAX_PMI_FLAG; i++) {
			switch (i) {
				case PAYAMOUNT_IDX_RINCOME:
					sb.append(item.iswage ? "-" : ".");
					break;
				case PAYAMOUNT_IDX_OINCOME:
					sb.append(!item.iswage ? "-" : ".");
					break;
				case PAYAMOUNT_IDX_TAXIBLE:
					sb.append(item.istaxible ? "-" : ".");
					break;
				case PAYAMOUNT_IDX_RELAVENT:
					sb.append(item.isrelavent ? "-" : ".");
					break;
				default:
					sb.append(".");
					break;
			}
		}
		return sb.toString();
	}
	
	private static String encodePmflagFromPension(PensionItem item) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < MAX_PMI_FLAG; i++) {
			switch (i) {
				case PAYAMOUNT_IDX_EPENSION:
					sb.append(item.iswage ? "+" : ".");
					break;
				case PAYAMOUNT_IDX_RPENSION:
					sb.append(!item.iswage ? "+" : ".");
					break;
				default:
					sb.append(".");
					break;
			}
		}
		return sb.toString();
	}
	
	private static double evalFormula(SelectUtil su, Employee lem, PaymentMaster lpm, PaymentDet pyparam, String code, String formula) throws Exception {
		Parser parser = new Parser(0,formula);
		MyParserCallback cb = new MyParserCallback(su, lem, lpm, pyparam);
		parser.setFunctInterface(cb);
		parser.setVarInterface(cb);
		double r = (Double)parser.evaluate();
		UniLog.log1("code:%s, formula:%s, r:%f", code, formula, r);
		return r;
	}
	
	public static char[] flagStrToCharArray(String s) {
		return Arrays.copyOf(StringUtils.defaultString(s).toCharArray(), ZkBiComposerPaymentGen.MAX_PMI_FLAG);
	}
	
	private static class IncomeItem {
		Date stdate;
		Date enddate;
		String flag;
		String code;
		String formula;
		boolean iswage;
		boolean istaxible;
		boolean isrelavent;
		boolean overridable;
	}
	
	private static class DeductionItem {
		Date stdate;
		Date enddate;
		String flag;
		String code;
		String formula;
		boolean iswage;
		boolean istaxible;
		boolean isrelavent;
		boolean overridable;
	}

	private static class PensionItem {
		String flag;
		String code;
		String formula;
		boolean iswage;
		boolean overridable;
	}
	
	private static class PmdCtrl {
		Date stdate;
		Date enddate;
	}
	
	private static class Employee {
		String eid;
		Date stdate;
		Date birth;
	}
	
	private static class PaymentMaster {
		String eid;
		Date date;
		Date edate;
		Date paydate;
		String method;
		double epension, rpension;
		double rincome, oincome;
		double taxible, eoawages;
	}
	
	private static class PaymentDet {
		String eid;
		Date date; //selected startdate
		Date stdate, enddate; //PmdCtrl startdate/enddate
		Date emgstdate;
		int monthdays;
		int ndays, latedays, latemins, eldays, elmins, hotdays, hotmins, xotdays, xotmins, otdays, otmins, nowdays, nowmins, wkdays, wkmins, nopaydays;
		double basemsal, basedsal, basewsal, basehsal, rincome, oincome, rdeduction, odeduction;
	}
	
	private static class PaymentItem {
		boolean optional;
		String eid;
		Date date, stdate, enddate;
		String type;
		String code;
		String flag;
		double rincome, oincome, pension;
		int ndays;
	}

	private static class PaymentItemDet {
		String eid;
		Date date, stdate, enddate;
		Date emgstdate;
		String type;
		String code;
		double amount;
	}
	
	private static class MyParserCallback implements FunctionInterface, VariableInterface {
		SelectUtil suQuery;
		Employee lem;
		PaymentMaster lpm;
		PaymentDet pyparam;
		
		public MyParserCallback(SelectUtil suQuery, Employee lem, PaymentMaster lpm, PaymentDet pyparam) {
			this.suQuery = suQuery;
			this.lem = lem;
			this.lpm = lpm;
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
				return pyparam.nopaydays / LeaveApplication.LEAVEUNIT_PER_DAY;
			else if (StringUtils.equals(p_varname, "joindate"))
				return lem.stdate.getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "penddate"))
				return lpm.edate.getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "pstdate"))
				return lpm.date.getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "over18date")) {
				if (!DateUtil.isValid(lem.birth)) {
					UniLog.log1("Error getting Employee Brithday %s %s", lem.eid, lem.birth);
					return 0;
				}
				int mm = DateUtil.getMonth(lem.birth);
				int yy = DateUtil.getYear(lem.birth);
				int dd = DateUtil.getDay(lem.birth);
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
				double tmpf = 0.0;
				TableRec tr = suQuery.getQueryResult("select pm_rincome from paymentmaster where pm_eid = ? and pm_date = ?", 
						new Wherecl().appendArgument(lem.eid).appendArgument(tmpdate1));
				if (tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					tmpf = tr.getFieldDouble("pm_rincome");
				}
				return tmpf;
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
