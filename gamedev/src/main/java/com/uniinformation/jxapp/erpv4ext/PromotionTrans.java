package com.uniinformation.jxapp.erpv4ext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellVector;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.PickByTableTrForm;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.PickByTableTrForm.PickByTableTrFormCallback;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.PickByListForm;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.PickByListForm.PickByListFormCallback;

public class PromotionTrans extends JxZkBiBase {

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		new JxFieldChange("emg_stdate") {
			@Override
			public boolean valueChanged(JxField field, String orgvalue) {
				Date startDate = field.getJxValue().getDate();
				Date lastStartDate = getBr().getCellDate("emg_xlaststdate");
				Date nextStartDate = getBr().getCellDate("emg_xnextstdate");
				if (!DateUtil.isDateNull(startDate)) {
					if (startDate.compareTo(LeaveApplication.MAX_DATE) >= 0)
						LeaveApplication.showErrorNotification("Invalid Date", (Component)field.getNativeObject());
					else if (!DateUtil.isDateNull(lastStartDate) && startDate.compareTo(lastStartDate) <= 0)
						LeaveApplication.showErrorNotification("Take Office Date must be more than Last Take Office Date", (Component)field.getNativeObject());
					else if (!DateUtil.isDateNull(nextStartDate) && startDate.compareTo(lastStartDate) >= 0)
						LeaveApplication.showErrorNotification("Take Office Date must be less than Next Take Office Date", (Component)field.getNativeObject());
				}
				return true;
			}
		};
		new PickWgtTypeListener(sessionHelper, this, jxAdd("emg_wgtype"));
	}

	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(false);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(true);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}
	
	private ReturnMsg validationRecord(boolean isUpdate) throws Exception {
		Date joinDate = getBr().getCellDate("em_stdate");
		Date startDate = getBr().getCellDate("emg_stdate");
		Date lastStartDate = getBr().getCellDate("emg_xlaststdate");
		Date nextStartDate = getBr().getCellDate("emg_xnextstdate");
		String postStatus = getBr().getCellString("emg_poststatus");
		UniLog.log1("isUpdate:%b, joinDate:%s, startDate:%s, lastStartDate:%s, nextStartDate:%s", isUpdate, joinDate, startDate, lastStartDate, nextStartDate);
		if (startDate.compareTo(joinDate) != 0 && StringUtils.isBlank(postStatus))
			return new ReturnMsg(false, "Please input Reason", true);
		if (DateUtil.isDateNull(startDate))
			return new ReturnMsg(false, "Please input Take Office Date", true);
		if (startDate.compareTo(LeaveApplication.MAX_DATE) >= 0)
			return new ReturnMsg(false, "Invalid Take Office Date", true);
		if (isUpdate) {
			if (!DateUtil.isDateNull(lastStartDate) && startDate.compareTo(lastStartDate) <= 0)
				return new ReturnMsg(false, "Take Office Date must be more than Last Take Office Date", true);
			if (!DateUtil.isDateNull(nextStartDate) && startDate.compareTo(nextStartDate) >= 0)
				return new ReturnMsg(false, "Take Office Date must be less than Next Take Office Date", true);
		}
		else {
			if (DateUtil.isDateNull(lastStartDate))
				return new ReturnMsg(false, "Invalid Last Take Office Date", true);
			if (startDate.compareTo(lastStartDate) <= 0)
				return new ReturnMsg(false, "Take Office Date must be more than Last Take Office Date", true);
		}
		return ReturnMsg.defaultOk;
	}
	
	public static class PickWgtTypeListener extends ZkBiEventListener<Event> {
		private ZkJxPickInput pickComp; 
		private PickByListForm pickForm;
		public PickWgtTypeListener(SessionHelper sh, final JxZkBiBase biBase, final JxField fdWgtype) {
			pickComp = (ZkJxPickInput)fdWgtype.getNativeObject();
			pickComp.setPopupWidth("180px");
			pickForm = new PickByListForm(sh, 
				new String[] {"50px", "100px"}, 
				new ArrayList<String[]>() {{
					add(new String[] {"H", "hourly"});
					add(new String[] {"D", "daily"});
					add(new String[] {"W", "weekly"});
					add(new String[] {"B", "biweekly"});
					add(new String[] {"M", "monthly"});
				}},
				new PickByListFormCallback() { 
					public void callback(String[] rec, Object userData) {
						try {
							fdWgtype.getJxValue().set(rec[0]);
							biBase.setDirtyFlag(true);
						} catch (CellException e) {
							UniLog.log(e);
						}
					}
				}
			);
			pickComp.addEventListener(Events.ON_OPEN, this);
		}
		@Override
		public void onZkBiEvent(Event event) throws Exception {
			UniLog.log1("emg_wgtype event:%s", event);
			pickForm.bindComponent(pickComp, null);
		}
	};

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		if (mode == JxZkBiBase.MODE_ADD) {
			String eid = (getUserData() != null && getUserData() instanceof String) ? (String)getUserData() : null;
			UniLog.log1("param eid:%s", eid);
			if (StringUtils.isBlank(eid)) {
				showErrorMessageAndExit("Employee Code cannot be empty");
				return;
			}
			try {
				p_br.getCell("emg_eid").set(eid);
				p_br.getCell("emg_enddate").set(LeaveApplication.MAX_DATE);
				TableRec tr = p_br.getSelectUtil().getQueryResult("select emg_stdate, emg_deptrg, emg_postrg, emg_graderg, emg_emtyperg, emg_wgtype, emg_includepay from emgrade"
						+ " where emg_eid = ? and emg_enddate = ?", 
						new Wherecl()
							.appendArgument(eid)
							.appendArgument(LeaveApplication.MAX_DATE));
				if (tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					p_br.getCell("emg_xlaststdate").set(tr.getFieldDate("emg_stdate"));
					p_br.getCell("emg_deptrg").set(tr.getFieldInt("emg_deptrg"));
					p_br.getCell("emg_postrg").set(tr.getFieldInt("emg_postrg"));
					p_br.getCell("emg_graderg").set(tr.getFieldInt("emg_graderg"));
					p_br.getCell("emg_emtyperg").set(tr.getFieldInt("emg_emtyperg"));
					p_br.getCell("emg_wgtype").set(tr.getFieldString("emg_wgtype"));
					p_br.getCell("emg_includepay").set(StringUtils.equals(tr.getFieldString("emg_includepay"), "Y"));
				}
				Date lastStartDate = p_br.getCellDate("emg_xlaststdate");
				if (DateUtil.isDateNull(lastStartDate)) {
					showErrorMessageAndExit("Last record not found");
					return;
				}
				clonePaymentItem();
				setDirtyFlag(true);
			} catch (Exception e) {
				UniLog.log(e);
				showErrorMessageAndExit("Error:" + e.toString());
				return;
			}
		}
		else {
			try {
				p_br.getCell("emg_xlaststdate").set(DateUtil.zeroDate);
				p_br.getCell("emg_xnextstdate").set(DateUtil.zeroDate);
				String eid = p_br.getCellString("emg_eid");
				Date joinDate = p_br.getCellDate("em_stdate");
				Date startDate = p_br.getCellDate("emg_stdate");
				Date endDate = p_br.getCellDate("emg_enddate");
				if (startDate.compareTo(joinDate) == 0) {
					jxSetEnable("emg_stdate", false);
					jxSetEnable("emg_poststatus", false);
					jxSetEnable("emg_tranreason", false);
				}
				if (endDate.compareTo(LeaveApplication.MAX_DATE) < 0)
					p_br.getCell("emg_xnextstdate").set(DateUtil.nextday(endDate));
				if (startDate.compareTo(joinDate) > 0) {
					TableRec tr = p_br.getSelectUtil().getQueryResult("select emg_stdate from emgrade where emg_eid = ? and emg_enddate = ?", 
						new Wherecl().appendArgument(eid).appendArgument(DateUtil.prevday(startDate)));
					if (tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
						p_br.getCell("emg_xlaststdate").set(tr.getFieldDate("emg_stdate"));
					}
				}
			} catch (Exception e) {
				UniLog.log(e);
			}
		}
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new EmgIncomeGetItemProperty(sessionHelper, this, p_br.getSubLink("erpv4ext.EmgIncome")),
			new EmgDeductionGetItemProperty(sessionHelper, this, p_br.getSubLink("erpv4ext.EmgDeduction")),
			new EmgProvidentFundGetItemProperty(sessionHelper, this, p_br.getSubLink("erpv4ext.EmgProvidentFund"))
		);	
	}
	
	/* Customized GIPI for EmgIncome */
	public static class EmgIncomeGetItemProperty extends BiGetItemProperty {
		SessionHelper sessionHelper;
		JxZkBiBase biBase;
		PickByTableTrForm pickCodeForm;

		public EmgIncomeGetItemProperty(SessionHelper sh, JxZkBiBase biBase, BiResult p_br) {
			super(p_br);
			UniLog.log1("EmgIncomeGetItemProperty");
			sessionHelper = sh;
			this.biBase = biBase;
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_PULLDOWN_OPENED) {
				if (StringUtils.equals(bcc.getCellLabel(), "emic_code")) {
					try {
						ZkJxPickInput pickComp = (ZkJxPickInput)LeaveApplication.getCellComponent(bcc);
						if (pickCodeForm == null) {
							pickCodeForm = new PickByTableTrForm(sessionHelper, new String[] {"inci_code", "inci_compdesc"}, new PickByTableTrFormCallback() {
								public void callback(Object[] rec, TableRec tr, Object userData) {
									try {
										((BiCellCollection)userData).getCell("emic_code").set((String)rec[tr.getFieldIndex("inci_code")]);
									} catch (CellException e) {
										UniLog.log(e);
									}
								}
							});
						}
						pickCodeForm.bindComponent(pickComp, cl, bigibr, "select inci_code, inci_compdesc from incomeitem order by inci_code", null);
					}
					catch (Exception e) {
						UniLog.log(e);
					}
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED)
				biBase.setDirtyFlag(true);
			else {
				if (StringUtils.equals(bcc.getCellLabel(), "emic_code")) {
					ZkJxPickInput pickComp = (ZkJxPickInput) LeaveApplication.getCellComponent(bcc);
					pickComp.setPopupWidth("600px");
				}
			}
		}
	}

	/* Customized GIPI for EmgDeduction */
	public static class EmgDeductionGetItemProperty extends BiGetItemProperty {
		SessionHelper sessionHelper;
		JxZkBiBase biBase;
		PickByTableTrForm pickCodeForm;

		public EmgDeductionGetItemProperty(SessionHelper sh, JxZkBiBase biBase, BiResult p_br) {
			super(p_br);
			UniLog.log1("EmgDeductionGetItemProperty");
			sessionHelper = sh;
			this.biBase = biBase;
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_PULLDOWN_OPENED) {
				if (StringUtils.equals(bcc.getCellLabel(), "emde_code")) {
					try {
						ZkJxPickInput pickComp = (ZkJxPickInput)LeaveApplication.getCellComponent(bcc);
						if (pickCodeForm == null) {
							pickCodeForm = new PickByTableTrForm(sessionHelper, new String[] {"deci_code", "deci_compdesc"}, new PickByTableTrFormCallback() {
								public void callback(Object[] rec, TableRec tr, Object userData) {
									try {
										((BiCellCollection)userData).getCell("emde_code").set((String)rec[tr.getFieldIndex("deci_code")]);
									} catch (CellException e) {
										UniLog.log(e);
									}
								}
							});
						}
						pickCodeForm.bindComponent(pickComp, cl, bigibr, "select deci_code, deci_compdesc from deductionitem order by deci_code", null);
					}
					catch (Exception e) {
						UniLog.log(e);
					}
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED)
				biBase.setDirtyFlag(true);
			else {
				if (StringUtils.equals(bcc.getCellLabel(), "emde_code")) {
					ZkJxPickInput pickComp = (ZkJxPickInput) LeaveApplication.getCellComponent(bcc);
					pickComp.setPopupWidth("600px");
				}
			}
		}
	}

	/* Customized GIPI for EmgProvidentFund */
	public static class EmgProvidentFundGetItemProperty extends BiGetItemProperty {
		SessionHelper sessionHelper;
		JxZkBiBase biBase;
		PickByTableTrForm pickCodeForm;

		public EmgProvidentFundGetItemProperty(SessionHelper sh, JxZkBiBase biBase, BiResult p_br) {
			super(p_br);
			UniLog.log1("EmgProvidentFundGetItemProperty");
			sessionHelper = sh;
			this.biBase = biBase;
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_PULLDOWN_OPENED) {
				if (StringUtils.equals(bcc.getCellLabel(), "empe_code")) {
					try {
						ZkJxPickInput pickComp = (ZkJxPickInput)LeaveApplication.getCellComponent(bcc);
						if (pickCodeForm == null) {
							pickCodeForm = new PickByTableTrForm(sessionHelper, new String[] {"peni_code", "peni_compdesc"}, new PickByTableTrFormCallback() {
								public void callback(Object[] rec, TableRec tr, Object userData) {
									try {
										((BiCellCollection)userData).getCell("empe_code").set((String)rec[tr.getFieldIndex("peni_code")]);
									} catch (CellException e) {
										UniLog.log(e);
									}
								}
							});
						}
						pickCodeForm.bindComponent(pickComp, cl, bigibr, "select peni_code, peni_compdesc from pensionitem order by peni_code", null);
					}
					catch (Exception e) {
						UniLog.log(e);
					}
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED)
				biBase.setDirtyFlag(true);
			else {
				if (StringUtils.equals(bcc.getCellLabel(), "empe_code")) {
					ZkJxPickInput pickComp = (ZkJxPickInput) LeaveApplication.getCellComponent(bcc);
					pickComp.setPopupWidth("600px");
				}
			}
		}
	}
	
	private void clonePaymentItem() throws Exception {
		String eid = getBr().getCellString("emg_eid");
		Date lastStartDate = getBr().getCellDate("emg_xlaststdate");
		Date startDate = getBr().getCellDate("emg_stdate");
		Date endDate = getBr().getCellDate("emg_enddate");
		Wherecl wherecl = new Wherecl().appendArgument(eid).appendArgument(lastStartDate);

		CellVector cv = getBr().getSelectUtil().getQueryResultToCellVector("select * from emincome where emic_eid = ? and emic_date = ?", wherecl);
		BiResult sr = getBr().getSubLink("erpv4ext.EmgIncome");
		for (Object o : cv) {
			CellCollection cc = (CellCollection)o;
			ReturnMsg rtn = listboxAddRow(this, sr, jxAdd("list_erpv4ext_EmgIncome"), null, -1);
			if (rtn.getStatus()) {
				CellCollection col = sr.getRowCollectionV(sr.getRowCount() - 1);
				col.getCell("emic_eid").set(eid);
				col.getCell("emic_date").set(startDate);
				col.getCell("emic_enddate").set(endDate);
				col.getCell("emic_code").set(cc.getCellString("emic_code"));
				col.getCell("emic_formula").set(cc.getCellString("emic_formula"));
			}
			else if (rtn != null)
				throw new Exception(rtn.getEx());
		}

		cv = getBr().getSelectUtil().getQueryResultToCellVector("select * from emdeduction where emde_eid = ? and emde_date = ?", wherecl);
		sr = getBr().getSubLink("erpv4ext.EmgDeduction");
		for (Object o : cv) {
			CellCollection cc = (CellCollection)o;
			ReturnMsg rtn = listboxAddRow(this, sr, jxAdd("list_erpv4ext_EmgDeduction"), null, -1);
			if (rtn.getStatus()) {
				CellCollection col = sr.getRowCollectionV(sr.getRowCount() - 1);
				col.getCell("emde_eid").set(eid);
				col.getCell("emde_date").set(startDate);
				col.getCell("emde_enddate").set(endDate);
				col.getCell("emde_code").set(cc.getCellString("emde_code"));
				col.getCell("emde_formula").set(cc.getCellString("emde_formula"));
			}
			else if (rtn != null)
				throw new Exception(rtn.getEx());
		}

		cv = getBr().getSelectUtil().getQueryResultToCellVector("select * from empension where empe_eid = ? and empe_date = ?", wherecl);
		sr = getBr().getSubLink("erpv4ext.EmgProvidentFund");
		for (Object o : cv) {
			CellCollection cc = (CellCollection)o;
			ReturnMsg rtn = listboxAddRow(this, sr, jxAdd("list_erpv4ext_EmgProvidentFund"), null, -1);
			if (rtn.getStatus()) {
				CellCollection col = sr.getRowCollectionV(sr.getRowCount() - 1);
				col.getCell("empe_eid").set(eid);
				col.getCell("empe_date").set(startDate);
				col.getCell("empe_enddate").set(endDate);
				col.getCell("empe_code").set(cc.getCellString("empe_code"));
				col.getCell("empe_formula").set(cc.getCellString("empe_formula"));
			}
			else if (rtn != null)
				throw new Exception(rtn.getEx());
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
}
