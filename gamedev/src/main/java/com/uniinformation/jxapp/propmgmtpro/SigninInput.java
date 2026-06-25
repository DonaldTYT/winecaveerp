package com.uniinformation.jxapp.propmgmtpro;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Label;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;

public class SigninInput extends JxZkBiBase {
	private Textbox tbScan;
	private Map<String, String> statusMap;

	@Override
	public void afterBind() {
		super.afterBind();
		Selectors.find("[id='btSignin']").stream().findFirst().ifPresent(bt -> ((Button)bt).addSclass("orange"));
		jxAdd("btSignin").addActionListener(btnEvent -> {
			try {
				if (!tbScan.isVisible()) {
					tbScan.setVisible(true);
					ZkUtil.js("$('#%s').closest('.zkbi-detail-grid.z-row').show();keepFocusText.init('%s')", tbScan.getUuid(), tbScan.getUuid());
					ZkUtil.setEventListener(tbScan, Events.ON_OK, ev -> {
						String s = tbScan.getText();
						if (StringUtils.length(s) != 9 || !StringUtils.endsWithAny(s, "-01", "-02")) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "編碼輸入不正確");
							return;
						}
						int signinMode = Integer.parseInt(StringUtils.right(s, 2));
						AtomicReference<String> strRef = new AtomicReference<>(s.substring(0, 6));
						BiCellCollection bcc = getBr().getSubLinkResult("propmgmtpro.SigninInputDet").stream()
								.filter(c -> StringUtils.equals(c.getCellString("pcol_n"), strRef.get()))
								.findFirst().orElse(null);
						if (bcc == null) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "編碼輸入不正確");
							return;
						}
						if (StringUtils.equalsAny(bcc.getCellString("col_d"), "Y", "A")) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "此單位已簽到，操作已取消");
							return;
						}
						bcc.getCell("col_d").set(signinMode == 2 ? "A" : "Y");
						bcc.getCell("col_e").set(new Date());
						tbScan.setText("");
						sortDetListbox();
						sumQuotient();
						setDirtyFlag(true);
					});
				} else {
					tbScan.setVisible(false);
					ZkUtil.js("$('#%s').closest('.zkbi-detail-grid.z-row').hide();keepFocusText.exit()", tbScan.getUuid());
				}
			} catch (Exception e) {
				UniLog.log(e);
				ZkBiMsgbox.show(ZkBiMsgbox.Type.error, e.toString());
			}
		});

		Listbox lb = (Listbox)jxAdd("list_propmgmtpro_SigninInputDet").getNativeObject();
		ZkUtil.setEventListener(lb, "onItemRendererCallback", event -> {
			Map<String, Object> m = (Map<String, Object>)event.getData();
			Listitem listItem = (Listitem)m.get("listItem");
			int idx = (int)m.get("idx");
			Object data = m.get("data");
			UniLog.log1("onItemRendererCallback event:%s, idx:%d", event, idx);
          	if (data instanceof TrStatFilter)
           		data = ((TrStatFilter)data).getTrStatIdx();
          	BiResult brDet = getBr().getSubLink("propmgmtpro.SigninInputDet");
          	BiCellCollection bcc = brDet.getRowCollectionO(data);
          	Listcell lc = (Listcell)listItem.getFirstChild();
          	lc.setStyle("text-align:center");
			Toolbarbutton b = new Toolbarbutton() {{
				setIconSclass("z-icon-pencil z-icon-2x");
    			setSclass("narrowtoolbarbutton");
				setStyle("opacity:0.7");
				setTooltiptext("編輯簽到狀態");
				setVisible(StringUtils.equalsAny(bcc.getCellString("col_d"), "Y", "A"));
				setParent(lc);
			}};
			b.addEventListener(Events.ON_CLICK, ev -> {
				if (statusMap == null) {
					BiResult brStatus = BiResultHelper.create(sessionHelper, "propmgmtpro.SigninStatus", null, -1, null);
					List<Map<String, Object>> list = ZkUtil.getBiResultRecordMap(brStatus, false);
					statusMap = list.stream().collect(Collectors.toMap(lm -> (String)lm.get("ss_code"), lm -> (String)lm.get("ss_desc"), (o, n) -> n, LinkedHashMap::new));
					brStatus.close();
				}
				Div div = (Div)curComp.getTemplate("editSigninTemplate").create(null, null, null, null)[0];
				Label lbUnit = ZkUtil.getFellowWithNullId(div, "lbUnit");
				lbUnit.setValue(bcc.getCellString("col_c"));
				Combobox cbStatus = ZkUtil.getFellowWithNullId(div, "cbStatus");
				for (Map.Entry<String, String> entry : statusMap.entrySet()) {
					Comboitem item = cbStatus.appendItem(entry.getValue());
					item.setValue(entry.getKey());
					if (StringUtils.equals(bcc.getCellString("col_d"), entry.getKey()))
						cbStatus.setSelectedItem(item);
				}

				ZkBiMsgboxButton[] btns = Arrays.stream(new String[] {"OK", "Cancel"}).map(s -> new ZkBiMsgboxButton(sessionHelper.getBtLabel(s))).toArray(ZkBiMsgboxButton[]::new);
				btns[0].setAttribute("forbitClose", true);
				new ZkBiMsgbox(sessionHelper).setContent(div).setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
						UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
						if (btn.getIdx() != 0)
							return;
						String v = cbStatus.getSelectedItem().getValue();
						if (!StringUtils.equals(bcc.getCellString("col_d"), v)) {
							bcc.getCell("col_d").set(v);
							if (StringUtils.isBlank(v)) {
								bcc.getCell("col_e").set(DateUtil.zeroTime);
								b.setVisible(false);
							}
							sortDetListbox();
							setDirtyFlag(true);
						}
						btn.closeMsgbox();
					}
				}).build().setTitle("編輯簽到狀態").appendStyle("width:100%;max-width:570px").setVboxStyle("margin:10px 0 10px 0").doModal();
			});
		});
		lb.setAttribute("hasOnItemRendererCallback", true);
	}

	@Override
	public void bindCellCollection(BiResult c, int mode) {
		super.bindCellCollection(c, mode);
		try {
			ColumnCell cc = getBr().getCell("vcol_scan");
			cc.setMode(Cell.VMODE_HIDDEN);
			Textbox tb = (Textbox)((JxField)cc.getMapper()).getNativeObject();
			if (!tb.hasAttribute("tbScan")) {
				tbScan = new Textbox();
				tbScan.setMaxlength(9);
				tbScan.setWidth("100px");
				tb.getParent().insertBefore(tbScan, tb);
				tb.setAttribute("tbScan", tbScan);
			} else
				tbScan = (Textbox)tb.getAttribute("tbScan");
			tbScan.setText("");
			tbScan.setVisible(false);
			Textbox tbScan = (Textbox)((JxField)cc.getMapper()).getNativeObject();
			ZkUtil.js("$('#%s').closest('.zkbi-detail-grid.z-row').hide()", tbScan.getUuid());
			sortDetListbox();
			sumQuotient();
			Listbox lb = (Listbox)jxAdd("list_propmgmtpro_SigninInputDet").getNativeObject();
			Textbox tbFilter = (Textbox)lb.getFellowIfAny("footlayout_filterInput_" + lb.getId(), true);
			if (tbFilter != null) {
				UniLog.log1("tbFilter:%s", tbFilter);
				tbFilter.removeEventListener(Events.ON_CHANGE, footFilterListener);
				tbFilter.addEventListener(Events.ON_CHANGE, footFilterListener);
			}
		} catch (Exception e) {
			UniLog.log(e);
			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, e.toString());
		}
	}
	
	private void sortDetListbox() {
		Listbox lb = (Listbox)jxAdd("list_propmgmtpro_SigninInputDet").getNativeObject();
		BiResult br = getBr().getSubLink("propmgmtpro.SigninInputDet");
		ListModelList<Object> model = (ListModelList<Object>)lb.getModel();
		model.sort((a, b) -> {
          	if (a instanceof TrStatFilter)
           		a = ((TrStatFilter)a).getTrStatIdx();
          	if (b instanceof TrStatFilter)
           		b = ((TrStatFilter)b).getTrStatIdx();
			Date date1 = br.getRowCollectionO(a).getDate("col_e");
			Date date2 = br.getRowCollectionO(b).getDate("col_e");
			if (date1 == null && date2 == null)
				return 0;
			else if (date1 == null)
				return 1;
			else if (date2 == null)
				return -1;
			return date2.compareTo(date1);
		});
	}
	
	private void sumQuotient() throws CellException {
		Vector<BiCellCollection> list = getBr().getSubLinkResult("propmgmtpro.SigninInputDet");
		for (String label : new String[] {"vcol_attqt", "vcol_authqt", "vcol_totqt"}) {
			ColumnCell cc = getBr().getCell(label);
			Doublebox db = (Doublebox)((JxField)cc.getMapper()).getNativeObject();
			if (!db.hasAttribute("addedPerComp")) {
				db.setAttribute("addedPerComp", true);
				db.getParent().appendChild(new Label("%"));
			}
			switch (label) {
			case "vcol_attqt":
				cc.set(list.stream().filter(c -> StringUtils.equals(c.getCellString("col_d"), "Y")).mapToDouble(c -> c.getCellDouble("pcol_m")).sum());
				break;
			case "vcol_authqt":
				cc.set(list.stream().filter(c -> StringUtils.equals(c.getCellString("col_d"), "A")).mapToDouble(c -> c.getCellDouble("pcol_m")).sum());
				break;
			case "vcol_totqt":
				cc.set(getBr().getCellDouble("vcol_attqt") + getBr().getCellDouble("vcol_authqt"));
				break;
			}
		}
	}
	
	private EventListener<InputEvent> footFilterListener = (event) -> {
		sortDetListbox();
	};
}
