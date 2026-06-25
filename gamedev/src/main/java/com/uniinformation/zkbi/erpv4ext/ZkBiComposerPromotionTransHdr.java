package com.uniinformation.zkbi.erpv4ext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkcomp.ZkBiButtonGroup;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.PickByTableTrForm;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.PickByTableTrForm.PickByTableTrFormCallback;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ZkBiComposerPromotionTransHdr extends ZkBiComposerBase {
	private Button clickedSalaryItemButton;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("doAfterCompose called");
		masterWin.addEventListener("onHideButtonProTranH", new EventListener<Event>() {
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
		Events.echoEvent("onHideButtonProTranH", masterWin, null);
	}

	@Override
	protected void setupExtraButton(final BiResult result) {
		Button btAddSalaryItem = new ZkBiButton("Add Salary Item", null, "btAddSalaryItem");
        btAddSalaryItem.setAttribute("tlkey", "bt_add_salary_item");

		Button btDelSalaryItem = new ZkBiButton("Delete Salary Item", null, "btDelSalaryItem");
        btDelSalaryItem.setAttribute("tlkey", "bt_del_salary_item");

       	Button btAddDeductionItem = new ZkBiButton("Add Deduction Item", null, "btAddDeductionItem");
        btAddDeductionItem.setAttribute("tlkey", "bt_add_dedu_item");

       	Button btDelDeductionItem = new ZkBiButton("Delete Deduction Item", null, "btDelDeductionItem");
        btDelDeductionItem.setAttribute("tlkey", "bt_del_dedu_item");

       	Button btAddProvidentFundItem = new ZkBiButton("Add Provident Fund Item", null, "btAddProvidentFundItem");
        btAddProvidentFundItem.setAttribute("tlkey", "bt_add_provfund_item");

       	Button btDelProvidentFundItem = new ZkBiButton("Delete Provident Fund Item", null, "btDelProvidentFundItem");
        btDelProvidentFundItem.setAttribute("tlkey", "bt_del_provfund_item");

		Button btResetSalaryItem = new ZkBiButton("Reset Salary Item", null, "btResetSalaryItem");
        btResetSalaryItem.setAttribute("tlkey", "bt_reset_salary_item");

		Button btResetDeductionItem = new ZkBiButton("Reset Deduction Item", null, "btResetDeductionItem");
        btResetDeductionItem.setAttribute("tlkey", "bt_reset_deduction_item");

    	final Button bgAddDelSalaryItem = new ZkBiButtonGroup(sessionHelper)
    			.addButton(btAddSalaryItem)
    			.addButton(btDelSalaryItem)
    			.addButton(btAddDeductionItem)
    			.addButton(btDelDeductionItem)
    			.addButton(btAddProvidentFundItem)
    			.addButton(btDelProvidentFundItem)
    			.addButton(btResetSalaryItem)
    			.addButton(btResetDeductionItem)
    			.setId("bgAddDelSalaryItem").setLabel("Add/Del Salary Item").build();
        bgAddDelSalaryItem.setAttribute("tlkey", "bg_salary_item");
        abHelper.addButton(bgAddDelSalaryItem, "fa-folder-open-o");

        final Button btAddDelSalaryItem = new ZkBiButton("", null, "btAddDelSalaryItem");
        btAddDelSalaryItem.setVisible(false);
        btAddDelSalaryItem.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("btAddDelSalaryItem event:%s", event);
				buildAddDelSalaryItemDialog(result, btAddDelSalaryItem);
			}
        });
        abHelper.addButton(btAddDelSalaryItem);
		ZkUtil.setupBatchModeButton(btAddDelSalaryItem, batchModeToggleButton);
        btAddDelSalaryItem.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("btAddDelSalaryItem event :%s", event);
				Button btCancel = (Button) btAddDelSalaryItem.getParent().query("#" + btAddDelSalaryItem.getId() + "BatchCancel");
				btCancel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log1("btAddDelSalaryItem BatchCancel event:%s", event);
						bgAddDelSalaryItem.setVisible(true);
						btAddDelSalaryItem.setVisible(false);
					}
				});
			}
        });

		for (final Button btn : new Button[] {btAddSalaryItem, btDelSalaryItem, btAddDeductionItem, btDelDeductionItem, btAddProvidentFundItem, btDelProvidentFundItem, btResetSalaryItem, btResetDeductionItem}) {
			btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					clickedSalaryItemButton = btn;
					btAddDelSalaryItem.setLabel(btn.getLabel());
					bgAddDelSalaryItem.setVisible(false);
					btAddDelSalaryItem.setVisible(true);
					Events.echoEvent(Events.ON_CLICK, btAddDelSalaryItem, null);
				}
        	});
		}
	}
	
	private void buildAddDelSalaryItemDialog(final BiResult br, final Button btAddDelSalaryItem) {
		try {
			ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
			Label lb = new Label(clickedSalaryItemButton.getLabel() + ":");
			final ZkJxPickInput pickComp = new ZkJxPickInput();
			Hlayout hl = new Hlayout();
			hl.appendChild(lb);
			hl.appendChild(pickComp);
			pickComp.setWidth("100px");
			pickComp.setPopupWidth("600px");
			pickComp.addEventListener(Events.ON_OPEN, new ZkBiEventListener<Event>() {
				PickByTableTrForm pickCodeForm;
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					try {
						String[] selectFields = null;
						String sqlStr = null;
						if (StringUtils.endsWith(clickedSalaryItemButton.getId(), "SalaryItem")) {
							selectFields = new String[] {"inci_code", "inci_compdesc"};
							sqlStr = "select inci_code, inci_compdesc from incomeitem order by inci_code";
						} else if (StringUtils.endsWith(clickedSalaryItemButton.getId(), "DeductionItem")) {
							selectFields = new String[] {"deci_code", "deci_compdesc"};
							sqlStr = "select deci_code, deci_compdesc from deductionitem order by deci_code";
						} else if (StringUtils.endsWith(clickedSalaryItemButton.getId(), "ProvidentFundItem")) {
							selectFields = new String[] {"peni_code", "peni_compdesc"};
							sqlStr = "select peni_code, peni_compdesc from pensionitem order by peni_code";
						}
						if (pickCodeForm == null) {
							pickCodeForm = new PickByTableTrForm(sessionHelper, selectFields, new PickByTableTrFormCallback() {
								public void callback(Object[] rec, TableRec tr, Object userData) {
									pickComp.setText((String)rec[0]);
								}
							});
						}
						pickCodeForm.bindComponent(pickComp, null, br, sqlStr, null);
					}
					catch (Exception e) {
						UniLog.log(e);
					}
				}
			});
			
			new ZkBiMsgbox(sessionHelper).setContent(hl).setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
					if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Ok"))) {
						String code = pickComp.getText();
						if (StringUtils.isBlank(code)) {
							ZkUtil.showErrMsg("Please choose Code");
							return;
						}
						Map<String, Pair<Date, Date>> emMap = getBatchEmMap(br);
						if (emMap.isEmpty()) {
							ZkUtil.showErrMsg("Please choose Employee");
							return;
						}
						String errMsg = addDelResetSalaryItem(br.getSelectUtil(), emMap, code);
						if (errMsg != null)
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Error:" + errMsg);
						else {
							ZkUtil.showMsg("Update successfully");
							Button btCancel = (Button) btAddDelSalaryItem.getParent().query("#" + btAddDelSalaryItem.getId() + "BatchCancel");
							Events.echoEvent(Events.ON_CLICK, btCancel, null);
						}
					}
				}
			}).build().doModal();
		} catch (Exception e) {
			UniLog.log(e);
		}
	}

	private Map<String, Pair<Date, Date>> getBatchEmMap(BiResult br) {
		final Map<String, Pair<Date, Date>> emMap = new LinkedHashMap<String, Pair<Date, Date>>();
		Set selection = listModelList.getSelection();
       	for (Iterator it = selection.iterator(); it.hasNext();) {
       		Object o = it.next();
         	Object ts = o;
          	if (ts instanceof TrStatFilter)
            	ts = ((TrStatFilter)ts).getTrStatIdx();
       		CellCollection cc = br.getRowCollectionO(ts);
       		String eid = cc.getString("em_eid");
       		Date startDate = cc.getDate("emg_stdate");
       		Date endDate = cc.getDate("emg_enddate");
       		emMap.put(eid, Pair.of(startDate, endDate));
       	}
       	return emMap;
	}
	
	private String addDelResetSalaryItem(SelectUtil su, Map<String, Pair<Date, Date>> emMap, String code) {
		UniLog.log1("addDelResetSalaryItem %s %s", clickedSalaryItemButton.getId(), code);
		String btnId = clickedSalaryItemButton.getId();
		BiResult br = null;
		try {
			Set<String> hasRecordList = new HashSet<String>();
			if (StringUtils.startsWith(btnId, "btAdd")) {
				for (Map.Entry<String, Pair<Date, Date>> entry : emMap.entrySet()) {
					String eid = entry.getKey();
					Date startDate = entry.getValue().getLeft();
					TableRec tr = null;
					Wherecl wherecl = new Wherecl().appendArgument(eid).appendArgument(startDate).appendArgument(code);
					if (StringUtils.endsWith(clickedSalaryItemButton.getId(), "SalaryItem"))
						tr = su.getQueryResult("select serial_id from emincome where emic_eid = ? and emic_date = ? and emic_code = ?", wherecl);
					else if (StringUtils.endsWith(clickedSalaryItemButton.getId(), "DeductionItem"))
						tr = su.getQueryResult("select serial_id from emdeduction where emde_eid = ? and emde_date = ? and emde_code = ?", wherecl);
					else if (StringUtils.endsWith(clickedSalaryItemButton.getId(), "ProvidentFundItem"))
						tr = su.getQueryResult("select serial_id from empension where empe_eid = ? and empe_date = ? and empe_code = ?", wherecl);
					if (tr != null && tr.getRecordCount() > 0)
						hasRecordList.add(eid);
				}
			}
			br = sessionHelper.newBiResult("erpv4ext.PromotionTransHdr");
			br.beginWork();
			su = br.getSelectUtil();
			for (Map.Entry<String, Pair<Date, Date>> entry : emMap.entrySet()) {
				String eid = entry.getKey();
				Date startDate = entry.getValue().getLeft();
				Date endDate = entry.getValue().getRight();
				UniLog.log1("eid:%s, startDate:%s, endDate:%s", eid, startDate, endDate);
				Wherecl whereclAdd = new Wherecl().appendArgument(eid).appendArgument(startDate).appendArgument(endDate).appendArgument(code);
				Wherecl whereclUpdate = new Wherecl().appendArgument(eid).appendArgument(startDate).appendArgument(code);
				if (StringUtils.equals(btnId, "btAddSalaryItem") && !hasRecordList.contains(eid))
					su.executeUpdate("insert into emincome (emic_eid, emic_date, emic_enddate, emic_code) values(?,?,?,?)", whereclAdd);
				else if (StringUtils.equals(btnId, "btDelSalaryItem"))
					su.executeUpdate("delete from emincome where emic_eid = ? and emic_date = ? and emic_code = ?", whereclUpdate);
				else if (StringUtils.equals(btnId, "btResetSalaryItem"))
					su.executeUpdate("update emincome set emic_formula = '' where emic_eid = ? and emic_date = ? and emic_code = ?", whereclUpdate);
				else if (StringUtils.equals(btnId, "btAddDeductionItem") && !hasRecordList.contains(eid))
					su.executeUpdate("insert into emdeduction (emde_eid, emde_date, emde_enddate, emde_code) values(?,?,?,?)", whereclAdd);
				else if (StringUtils.equals(btnId, "btDelDeductionItem"))
					su.executeUpdate("delete from emdeduction where emde_eid = ? and emde_date = ? and emde_code = ?", whereclUpdate);
				else if (StringUtils.equals(btnId, "btResetDeductionItem"))
					su.executeUpdate("update emdeduction set emde_formula = '' where emde_eid = ? and emde_date = ? and emde_code = ?", whereclUpdate);
				else if (StringUtils.equals(btnId, "btAddProvidentFundItem") && !hasRecordList.contains(eid))
					su.executeUpdate("insert into empension (empe_eid, empe_date, empe_enddate, empe_code) values(?,?,?,?)", whereclAdd);
				else if (StringUtils.equals(btnId, "btDelProvidentFundItem"))
					su.executeUpdate("delete from empension where empe_eid = ? and empe_date = ? and empe_code = ?", whereclUpdate);
			}
			br.commitWork();
		}
		catch (Exception e) {
			UniLog.log(e);
			if (br != null)
				br.rollbackWork();
			return e.getMessage();
		}
		finally {
			if (br != null)
				br.close();
		}
		return null;
	}
}
