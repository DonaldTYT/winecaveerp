package com.uniinformation.jxapp.propertymgmt;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Div;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Timer;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.BiPickGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiMsgbox;
import static com.uniinformation.utils.ZkUtil.CheckedConsumer;

public class RenovationDeposit extends JxZkBiBase {
	private final BiPickGetItemProperty gipiPropertyUnit = new BiPickGetItemProperty(Arrays.asList("key_a"));
	private final BiPickGetItemProperty gipiPaymentMethod = new BiPickGetItemProperty(Arrays.asList("pmm_name"));

	private Radio rdUnitDefault, rdUnitCustom;

	@Override
	public void afterBind() {
		super.afterBind();
		UniLog.log("RenovationDeposit afterBind");
   		String ss = Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper));
		gipiPropertyUnit.setBiResult(BiResultHelper.create(sessionHelper, "propertymgmt.property", gipiPropertyUnit.getBiResult(), String.format("col_b = '%s'", ss), null, -1, Arrays.asList(Pair.of("key_a", false))));
		gipiPaymentMethod.setBiResult(BiResultHelper.create(sessionHelper, "propertymgmt.paymentmethod", gipiPaymentMethod.getBiResult(), null, null, -1, null));
		jxAdd("vcol_customunit").setItemListInterface(gipiPropertyUnit);
		jxAdd("col_n").setItemListInterface(gipiPaymentMethod);
		
		Component toolbar = Selectors.find(".zkbi-detail-toolbar-container toolbar").get(0);
		Component btExtraJxFormAction_0 = Selectors.find("[id='btExtraJxFormAction_0']").get(0);
		Selectors.find("button[id^='btContent']").forEach(bt -> {
			bt.detach();
			toolbar.insertBefore(bt, btExtraJxFormAction_0);
		});

		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			if (rdUnitCustom.isChecked())
				getBr().getCell("col_c").set(getBr().getCellString("vcol_customunit"));
		}, "vcol_customunit");
		
		AtomicReference<String> oldContent = new AtomicReference<>();
		ZkUtil.addJxActionListener(this, field -> {
			jxSetVisible("divPage1", false);
			jxSetVisible("divPage2", true);
			Stream.of("button", "zkbibuttongroup", "zkbibutton").flatMap(t -> Selectors.find(toolbar, t).stream())
						.filter(bt -> ZkUtil.closestComponent(bt, "popup") == null).forEach(bt -> {
				bt.setAttribute("oldVisibleState", bt.isVisible());
				bt.setVisible(StringUtils.startsWith(bt.getId(), "btContent") && !bt.getId().equals("btContentEdit"));
			});
			oldContent.set(getBr().getCellString("col_r"));
		}, "btContentEdit");
		
		ZkUtil.addJxActionListener(this, new CheckedConsumer<JxField>() {
			private Timer checkTimer;
			private void backPage() {
				jxSetVisible("divPage1", true);
				jxSetVisible("divPage2", false);
				Div divToolbar = (Div)Selectors.find(".zkbi-detail-toolbar-container").get(0);
				Stream.of("button", "zkbibuttongroup", "zkbibutton").flatMap(t -> Selectors.find(divToolbar, t).stream())
							.filter(bt -> ZkUtil.closestComponent(bt, "popup") == null).forEach(bt -> {
					bt.setVisible((boolean)bt.getAttribute("oldVisibleState"));
				});
			}
			@Override
			public void accept(JxField field) throws Exception {
				if (Objects.equals(field.getName(), "btContentClose")) {
					checkTimer = ZkUtil.timerEvent(checkTimer, curComp, 100, () -> {
						if (!Objects.equals(getBr().getCellString("col_r"), oldContent.get())) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.question, "儲存內容?", (event, bt) -> {
								if (bt.getIdx() == 1)
									getBr().getCell("col_r").set(oldContent.get());
								backPage();
							}, "Ok", "Cancel"); 
						} else
							backPage();
					});
				} else
					backPage();
			}
		}, "btContentSave", "btContentClose");
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		super.bindCellCollection(p_br, mode);
		try {
			jxSetVisible("divPage1", true);
			jxSetVisible("divPage2", false);
			//jxSetVisible("btContentEdit", sessionHelper.hasAccessRight("#pmgtadm1"));
			rdUnitDefault = (Radio)Selectors.find("radio[id='rdUnitDefault']").stream().findFirst().orElse(null);
			rdUnitCustom = (Radio)Selectors.find("radio[id='rdUnitCustom']").stream().findFirst().orElse(null);
			rdUnitDefault.setLabel(StringUtils.defaultString(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper))) + "管理機關");
			switch (mode) {
			case JxZkBiBase.MODE_ADD:
				rdUnitDefault.setChecked(true);
				p_br.getCell("col_c").set(rdUnitDefault.getLabel());
				p_br.getCell("vcol_customunit").set("");
				p_br.getCell("col_m").set(((Radiogroup)jxAdd("col_m").getNativeObject()).getItems().get(0).getLabel());
				p_br.getCell("col_p").set("未退");
				break;
			case JxZkBiBase.MODE_UPDATE:
				String unit = p_br.getCellString("col_c");
				if (Objects.equals(rdUnitDefault.getLabel(), unit)) {
					rdUnitDefault.setChecked(true);
					p_br.getCell("vcol_customunit").set("");
				} else {
					rdUnitCustom.setChecked(true);
					p_br.getCell("vcol_customunit").set(unit);
				}
				break;
			}
			jxSetEnable("vcol_customunit", rdUnitCustom.isChecked());
			ZkUtil.setEventListener(rdUnitDefault, Events.ON_CHECK, event -> {
				jxSetEnable("vcol_customunit", false);
				p_br.getCell("col_c").set(rdUnitDefault.getLabel());
				setDirtyFlag(true);
			});
			ZkUtil.setEventListener(rdUnitCustom, Events.ON_CHECK, event -> {
				jxSetEnable("vcol_customunit", true);
				p_br.getCell("col_c").set(p_br.getCellString("vcol_customunit"));
				setDirtyFlag(true);
			});
		} catch (Exception e) {
			UniLog.log(e);
		}
	}
}
