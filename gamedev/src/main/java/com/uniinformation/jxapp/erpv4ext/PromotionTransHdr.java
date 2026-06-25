package com.uniinformation.jxapp.erpv4ext;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiTranslateHelper;

public class PromotionTransHdr extends JxZkBiBase {
	private PositionInfoInput positionInfoInput;

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		new JxFieldAction("btAddPosition") {
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty()) {
					ZkUtil.showErrMsg("Cannot add position in editing mode");
					return;
				}
				try {
					/*JSONObject jo = new JSONObject();
					jo.put("eid", getBr().getCellString("em_eid"));
					String key = sessionHelper.putOneTimeData(jo, true);
					ZkUtil.js(String.format("openNewTab('zkbiloader.html?action=add&viewid=erpv4ext.PromotionTrans&page_id=PromotionTrans_01&zul=zkbiloader.zul&closetab=Y&sidemenu=N&addparams=%s')", key));*/
					if (positionInfoInput == null)
						positionInfoInput = new PositionInfoInput();
					positionInfoInput.add(((Button)field.getNativeObject()).getLabel());
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
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
		new PromotionTrans.PickWgtTypeListener(sessionHelper, this, jxAdd("emg_wgtype"));
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		try {
			p_br.getCell("emg_xlaststdate").set(DateUtil.zeroDate);
			p_br.getCell("emg_xnextstdate").set(DateUtil.zeroDate);
			p_br.getCell("emg_xoldstdate").set(p_br.getCellDate("emg_stdate"));
			if (StringUtils.isNotBlank(p_br.getCellString("emg_eid"))) {
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
			}
			else {
				jxSetEnable("emg_stdate", false);
				jxSetEnable("emg_poststatus", false);
				jxSetEnable("emg_tranreason", false);
				jxSetEnable("emg_wage", false);
				jxSetEnable("emg_wgtype", false);
				jxSetEnable("etmt_name", false);
				jxSetEnable("dpmt_name", false);
				jxSetEnable("gdmt_name", false);
				jxSetEnable("ptmt_name", false);
				jxSetVisible("list_erpv4ext_EmgIncome", false);
				jxSetVisible("list_erpv4ext_EmgDeduction", false);
				jxSetVisible("list_erpv4ext_EmgProvidentFund", false);
			}
		} catch (Exception e) {
			UniLog.log(e);
		}
	}

	@Override
	protected void formDirtyChanged() {
		jxSetEnable("btAddPosition", !isDirty());
		Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.PromotionTransDet");
		for (BiCellCollection cc : recs) {
			try {
				((Button)LeaveApplication.getCellComponent(cc.getCell("emg_xupdate"))).setDisabled(isDirty());;
			}
			catch (Exception e) {
			}
		}
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			Date joinDate = br.getCellDate("em_stdate");
			Date startDate = br.getCellDate("emg_stdate");
			Date endDate = br.getCellDate("emg_enddate");
			Date lastStartDate = br.getCellDate("emg_xlaststdate");
			Date nextStartDate = br.getCellDate("emg_xnextstdate");
			Date oldStartDate = br.getCellDate("emg_xoldstdate");
			int emtypeRg = br.getCellInt("etmt_rg");
			int deptRg = br.getCellInt("dpmt_rg");
			int gradeRg = br.getCellInt("gdmt_rg");
			int postRg = br.getCellInt("ptmt_rg");
			String reason = br.getCellString("emg_poststatus");
			String remark = br.getCellString("emg_tranreason");
			double wage = br.getCellDouble("emg_wage");
			String wgType = br.getCellString("emg_wgtype");
			if (startDate.compareTo(joinDate) != 0 && StringUtils.isBlank(reason))
				return new ReturnMsg(false, "Please input Reason", true);
			if (DateUtil.isDateNull(startDate))
				return new ReturnMsg(false, "Please input Take Office Date", true);
			if (startDate.compareTo(LeaveApplication.MAX_DATE) >= 0)
				return new ReturnMsg(false, "Invalid Take Office Date", true);
			if (!DateUtil.isDateNull(lastStartDate) && startDate.compareTo(lastStartDate) <= 0)
				return new ReturnMsg(false, "Take Office Date must be more than Last Take Office Date", true);
			if (!DateUtil.isDateNull(nextStartDate) && startDate.compareTo(nextStartDate) >= 0)
				return new ReturnMsg(false, "Take Office Date must be less than Next Take Office Date", true);

			Vector<BiCellCollection> recs = br.getSubLinkResult("erpv4ext.PromotionTransDet");
			for (BiCellCollection cc : recs) {
				if (cc.getDate("emg0_stdate").compareTo(oldStartDate) == 0) {
					cc.getCell("emg0_stdate").set(startDate);
					cc.getCell("emg0_poststatus").set(reason);
					cc.getCell("emg0_tranreason").set(remark);
					cc.getCell("emg0_wage").set(wage);
					cc.getCell("emg0_wgtype").set(wgType);
					cc.getCell("emg_emtyperg").set(emtypeRg);
					cc.getCell("emg_deptrg").set(deptRg);
					cc.getCell("emg_graderg").set(gradeRg);
					cc.getCell("emg_postrg").set(postRg);
				}
			}
			for (int i = recs.size() - 1; i >= 0; i--) {
				BiCellCollection cc = recs.get(i);
				if (i > 0) {
					BiCellCollection bc = recs.get(i - 1);
					cc.getCell("emg0_enddate").set(DateUtil.prevday(bc.getDate("emg0_stdate")));
				} else
					cc.getCell("emg0_enddate").set(LeaveApplication.MAX_DATE);
				if (cc.getCell("emg0_stdate").compareTo(startDate) == 0)
					endDate = cc.getDate("emg0_enddate");
			}
			recs = br.getSubLinkResult("erpv4ext.EmgIncome");
			for (BiCellCollection cc : recs) {
				cc.getCell("emic_date").set(startDate);
				cc.getCell("emic_enddate").set(endDate);
			}
			recs = br.getSubLinkResult("erpv4ext.EmgDeduction");
			for (BiCellCollection cc : recs) {
				cc.getCell("emde_date").set(startDate);
				cc.getCell("emde_enddate").set(endDate);
			}
			recs = br.getSubLinkResult("erpv4ext.EmgProvidentFund");
			for (BiCellCollection cc : recs) {
				cc.getCell("empe_date").set(startDate);
				cc.getCell("empe_enddate").set(endDate);
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
			new PromotionTrans.EmgIncomeGetItemProperty(sessionHelper, this, p_br.getSubLink("erpv4ext.EmgIncome")),
			new PromotionTrans.EmgDeductionGetItemProperty(sessionHelper, this, p_br.getSubLink("erpv4ext.EmgDeduction")),
			new PromotionTrans.EmgProvidentFundGetItemProperty(sessionHelper, this, p_br.getSubLink("erpv4ext.EmgProvidentFund")),
			new PromotionTransDetGetItemProperty(p_br.getSubLink("erpv4ext.PromotionTransDet"))
		);	
	}
	
	/* Customized GIPI for PromotionTransDet */
	private class PromotionTransDetGetItemProperty extends BiGetItemProperty {

		public PromotionTransDetGetItemProperty(BiResult p_br) {
			super(p_br);
			UniLog.log1("PromotionTransDetGetItemProperty");
		}

		@Override
		public boolean getAllowDelete(Object item) {
			BiCellCollection bcc = bigibr.getRowCollectionO(item) ;
			UniLog.log1("getAllowDelete:%s, bcc:%s", item, bcc);
			Date joinDate = getBr().getCellDate("em_stdate");
			Date startDate = bcc.getDate("emg0_stdate");
			Date endDate = bcc.getDate("emg0_enddate");
			return joinDate.compareTo(startDate) != 0 && endDate.compareTo(LeaveApplication.MAX_DATE) == 0;
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("emg_xupdate")) {
				if (!isDirty()) {
					try {
						if (positionInfoInput == null)
							positionInfoInput = new PositionInfoInput();
						String title = ZkBiTranslateHelper.getText(sessionHelper, "ERPV4EXT.PROMOTIONTRANSHDR.BT_UPDATE_POSITION", "BUTTON", "Update Position");
						positionInfoInput.update(title, cl.getDate("emg0_stdate"));
					}
					catch(Exception ex) {
						UniLog.log(ex);
					}
				}
				else
					ZkUtil.showErrMsg("Cannot update position in editing mode");
			}
			if (p_ctype == GIPI_CELL_MAPPED) {
				if (StringUtils.equals(bcc.getCellLabel(), "emg_xupdate")) {
					try {
						Date curStartDate = getBr().getCellDate("emg_xoldstdate");
						Date startDate = cl.getDate("emg0_stdate");
						UniLog.log1("GIPI_CELL_MAPPED startDate:%s, curStartDate:%s", startDate, curStartDate);
						Button btn = (Button)LeaveApplication.getCellComponent(cl, "emg_xupdate");
						btn.setLabel(sessionHelper.getBtLabel(bcc.getBiColumn().getEngName()));
						btn.setDisabled(!DateUtil.isDateNull(curStartDate) && !DateUtil.isDateNull(startDate) && startDate.compareTo(curStartDate) == 0);
					} catch (Exception e) {
						UniLog.log(e);
					}
				}
			}
		}
	}
	
	private class PositionInfoInput implements JxZkBiBaseCallback {
		private Window popupWin;
		private BiResult br;
		private JxZkBiBase detailForm;
		private boolean needRefreshParentPage;

		public PositionInfoInput() {
			popupWin = newPopupWindow("");
			popupWin.setWidth("100%");
			popupWin.setHeight("100%");
			popupWin.setClosable(false);
			br = sessionHelper.newBiResult("erpv4ext.PromotionTrans");
            detailForm = buildDetailWindow(br, popupWin, sessionHelper.isMobile(), true, this);
            popupWin.addEventListener("onRefreshListitems", new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					zkcb.biBaseRefreshListitems(getBr().getCurrentRecord());
				}
            });
		}
		
		public void add(String title) {
     		br.clearCurrentRec();
     		detailForm.setUserData(getBr().getCellString("em_eid"));
			detailForm.bindCellCollection(br, JxZkBiBase.MODE_ADD);
			detailForm.translateAllComp(br);
			detailForm.doAdd();
			Button btAdd = (Button)popupWin.getFellowIfAny("btAdd", true);
			if (btAdd != null)
				btAdd.setAttribute("ADD_NEXT", "N");
			popupWin.setTitle(title);
		}

		public void update(String title, Date date) {
			try {
				br.clearCondition();
				br.addCustomCondition(String.format("emg_eid = '%s' and emg_stdate = '%s'", getBr().getCellString("em_eid"), DateUtil.dateToDateTimeStr(date, "yyyy/MM/dd")));
				br.query(true);
				br.loadOneRecV(0);
				br.fetchOneRecV(0);
				detailForm.bindCellCollection(br, JxZkBiBase.MODE_UPDATE);
				detailForm.translateAllComp(br);
				detailForm.doUpdate();
				detailForm.jxSetVisible("btNext", false);
				detailForm.jxSetVisible("btPrevious", false);
				Button btUpdate = (Button)popupWin.getFellowIfAny("btUpdate", true);
				if (btUpdate != null)
					btUpdate.setAttribute("UPDATE_NEXT", "N");
				popupWin.setTitle(title);
			}
			catch (Exception e) {
				UniLog.log(e);
				ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Error:" + e.getMessage());
			}
		}
		
		@Override
		public void biBaseRefreshListitems(Object p_dataObj) {
			UniLog.log1("biBaseRefreshListitems");
			needRefreshParentPage = true;
		}
		@Override
		public void biBaseRefresh(BiResult p_result) {
			UniLog.log1("biBaseRefresh");
			needRefreshParentPage = true;
		}
		@Override
		public void biBaseOpen() {
			curComp.setVisible(false);
			needRefreshParentPage = false;
			Div div = (Div)popupWin.getFellowIfAny("zkbiListTop", true);
			if (div != null)
				div.setHeight("calc(80vh - 48px)");
		}
		@Override
		public void biBaseClose(BiResult p_br) {
			curComp.setVisible(true);
			if (needRefreshParentPage) {
				Button btReloadDetail = (Button)curComp.getFellowIfAny("btReloadDetail", true);
				if (btReloadDetail != null) {
					Events.echoEvent(Events.ON_CLICK, btReloadDetail, null);
					Events.echoEvent("onRefreshListitems", popupWin, null);
				}
			}
		}
		@Override
		public ReturnMsg fetchNext(BiResult p_br) {
			return null;
		}
		@Override
		public ReturnMsg fetchPrevious(BiResult p_br) {
			return null;
		}
		@Override
		public Boolean hasNextRec() {
			return false;
		}
		@Override
		public Boolean hasPrevRec() {
			return false;
		}
		@Override
		public String getExtraInfo() {
			return null;
		}

		@Override
		public HashSet<BiColumn> getVisibleColumns(BiResult p_br) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
