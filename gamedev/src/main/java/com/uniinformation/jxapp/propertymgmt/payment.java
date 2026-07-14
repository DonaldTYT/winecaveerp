package com.uniinformation.jxapp.propertymgmt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultPayment;
import com.uniinformation.erpv4.BatchBuildPrintHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkf.propertymgmt.ZkFormEpayment.BANK;
import com.uniinformation.zkf.propertymgmt.ZkFormEpayment.Epayment;

public class payment extends JxZkBiBase {
	private static final DecimalFormat df = new DecimalFormat("$#,##0.00");

	@Override
	public void afterBind() {
		super.afterBind();
		JxField jxfield = jxAdd("list_propertymgmt_PayProjectItem");
		if (jxfield != null) {
			Listbox lb = (Listbox)jxfield.getNativeObject();
			ZkUtil.setEventListener(lb, "onItemRendererCallback", event -> {
				Map<String, Object> m = (Map<String, Object>)event.getData();
				Listitem listItem = (Listitem)m.get("listItem");
				int idx = (int)m.get("idx");
				Object data = m.get("data");
				//UniLog.log1("onItemRendererCallback event:%s, idx:%d", event, idx);
	          	if (data instanceof TrStatFilter)
	           		data = ((TrStatFilter)data).getTrStatIdx();
	          	BiResult brItem = getBr().getSubLink("propertymgmt.PayProjectItem");
	          	BiCellCollection bcc = brItem.getRowCollectionO(data);
	          	Listcell lc = (Listcell)listItem.getFirstChild();
	          	Toolbarbutton btn = (Toolbarbutton)lc.query("toolbarbutton[JxZkListbox.deleteItemButton='Y']");
	          	if (btn != null)
	          		btn.setVisible(!bcc.getCellBoolean("vcol_hidedelbtn"));
			});
			lb.setAttribute("hasOnItemRendererCallback", true);
		}
		ZkUtil.addJxActionListener(this, field -> {
			try {
				BatchBuildPrintHandler ph = (BatchBuildPrintHandler)ZkUtil.createConstructorHandle("com.uniinformation.dynamic.propertymgmt.PrintPaymentInvoice2A5").invoke();
				ph.print(getBr());
				try (ByteArrayOutputStream os = new ByteArrayOutputStream(); GZIPOutputStream gzos = new GZIPOutputStream(os)) {
					gzos.write(ph.getBuilderData());
					gzos.finish();
					FileUtils.writeByteArrayToFile(new File("/tmp/PrintPaymentInvoice2A5.gz"), os.toByteArray());
					ZkUtil.showMsg("download finish");
				}
			} catch (Throwable e) {
				UniLog.log(e);
			}
		}, "btGetPrintData");
		setupScanBarcodeToPayment(this, () -> {
			try {
				return ((BiResultPayment)getBr()).validationRecord(false);
			} catch (Exception e) {
				UniLog.log(e);
				return new ReturnMsg(false, e.getMessage());
			}
		});
	}

	@Override
	protected void formDirtyChanged() {
		super.formDirtyChanged();
		jxSetEnable("btAddForScanBarcode", jxAdd("btAdd").getEnable());
	}

	@Override
   	public void initForm(int p_mode) {
		super.initForm(p_mode);
		jxSetVisible("btAddForScanBarcode", jxAdd("btAdd").getVisible());
		jxSetVisible("btAdd", false);
   	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		super.bindCellCollection(p_br, mode);
		jxSetVisible("btGetPrintData", sessionHelper.isAdminUser());
		ZkUtil.removeAllEventListener((Button)jxAdd("btAdd").getNativeObject(), "onAfterEpayment");
		setupInitSelectItem(this, sessionHelper, p_br, "ppm_name");
		sortPayitemListbox();
	}
	
	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = ((BiResultPayment)getBr()).validationRecord(false);
			if (rtn.getStatus() && Objects.equals(br.getCellDate("col_a"), DateUtil.today()))
				br.getCell("col_a").set(DateUtil.now());
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg afterAdd(BiResult br) {
		Events.echoEvent("onAfterEpayment", (Button)jxAdd("btAdd").getNativeObject(), br.getCellString("col_b"));
		return super.afterAdd(br);
	}

	public static void setupInitSelectItem(JxZkBiBase bibase, SessionHelper sessionHelper, BiResult p_br, String ppmNameLabel) {
		Map<String, Object> m = Erpv4Config.getLcFieldMap(sessionHelper, p_br.getCellString("col_c"));
		Listbox lb = (Listbox)bibase.jxAdd(ppmNameLabel).getNativeObject();
		lb.getItems().stream().filter(it -> it.getLabel().equals("電子支付")).findFirst().ifPresent(it -> {
			boolean isSelect = Objects.equals(p_br.getCellString(ppmNameLabel), "電子支付");
			boolean isVisible = isSelect || ((int)m.get("lc_epayment")) > 1;
			it.setVisible(isVisible);
			if (isSelect && !isVisible)
				lb.setSelectedIndex(-1);
		});
		try {
			Set<String> list = ZkUtil.getTableRecStream(p_br.getSelectUtil(), "select col_a from cashier where col_e = 'Y'").map(c -> c.getString("col_a")).collect(Collectors.toSet());
			String colaa = p_br.getCellString("col_aa");
			Listbox lb1 = (Listbox)bibase.jxAdd("cashier_col_b").getNativeObject();
			lb1.setSelectedIndex(-1);
			lb1.getItems().stream().forEach(it -> {
				boolean isSelect = (StringUtils.isNotBlank(colaa) && it.getLabel().startsWith(colaa));
				boolean isVisible = isSelect || list.stream().noneMatch(k -> it.getLabel().startsWith(k));
				it.setVisible(isVisible);
				if (isVisible && isSelect)
					lb1.setSelectedItem(it);
			});
		} catch (Exception e) {
			UniLog.log(e);
		}
	}

	
	public static void setupScanBarcodeToPayment(JxZkBiBase biBase, Supplier<ReturnMsg> validateCallback) {
		SessionHelper sessionHelper = biBase.getSessionHelper();
		Component toolbar = Selectors.find(".zkbi-detail-toolbar-container toolbar").get(0);
		Button btAdd = (Button)Selectors.find(toolbar, "#btAdd").get(0);
		Button bt = (Button)Selectors.find(toolbar, "button#btAddForScanBarcode").get(0);
		bt.setLabel(btAdd.getLabel());
		bt.setImage(btAdd.getImage());
		bt.setDisabled(btAdd.isDisabled());
		bt.setVisible(true);
		bt.detach();
		toolbar.insertBefore(bt, btAdd);
		ZkUtil.setZkBiEventListener(bt, Events.ON_CLICK, ev -> {
			BiResult br = biBase.getBr();
			if (Objects.equals(br.getCellString("col_g"), "電子支付")) {
				ReturnMsg rtn = validateCallback.get();
				if (!rtn.getStatus()) {
					ZkUtil.errMsg(rtn.getMsg());
					return;
				}
				double actualFee = br.getCellDouble("vcol_actualfee");
				if (actualFee <= 0) {
					ZkUtil.errMsg("付款金額必須大於0");
					return;
				}
				Component curComp = Selectors.find("idSpace").get(0);
				Map<String, Object> m = Erpv4Config.getLcFieldMap(sessionHelper, br.getCellString("col_c"));
				BANK bank = BANK.fromIndex((int)m.get("lc_epayment"));
				Hlayout hl = (Hlayout)curComp.getTemplate("barcodeScanTemplate").create(null, null, null, null)[0];
				Label lbFee = ZkUtil.getFellowWithNullId(hl, "lbFee");
				Label lbBank = ZkUtil.getFellowWithNullId(hl, "lbBank");
				Label lbMessage = ZkUtil.getFellowWithNullId(hl, "lbMessage");
				Textbox tbScan = ZkUtil.getFellowWithNullId(hl, "tbScan");
				lbFee.setValue(df.format(actualFee));
				lbBank.setValue(bank.getName());

				ZkBiMsgbox mb = ZkBiMsgbox.build2(hl, 10, 550, null, "Cancel").setClosable(false).setCloseWinCallback(ev1 -> ZkUtil.js("keepFocusText.exit()")).doModal();
				ZkUtil.js("keepFocusText.init('%s');scannerInputStart()", tbScan.getUuid());
				AtomicBoolean scanFlag = new AtomicBoolean();
				ZkUtil.setEventListener(tbScan, Events.ON_OK, ev2 -> {
					if (scanFlag.get())
						return;
					scanFlag.set(true);
					String scanText = tbScan.getText().trim();
					UniLog.log1("STATE_WAITPAYMENT qrCodeData received %s", scanText);
					lbMessage.setValue("正在處理，請稍候");
					ZkUtil.timerEvent(null, tbScan, 0, () -> {
						try {
							Epayment epayment = new Epayment(sessionHelper, br, curComp, null, sessionHelper.getLoginId(), 1, null, null) {
								@Override
								protected void printReceipt(Map<String, Object> m) {
								}
							};
							Map<String, Object> m1 = epayment.b2cPayment(bank, actualFee, scanText);
							String errMsg = epayment.paymentFinish(m1, false);
							if (errMsg == null) {
								ZkUtil.showMsg("付款成功");
								mb.close();
								ZkUtil.setEventListenerForCallOne(btAdd, "onAfterEpayment", ev3 -> {
									try {
										ZkUtil.importAction.accept(sessionHelper, su -> {
											su.executeUpdate("update epayment set epm_voucherno = ?, epm_vtime = ? where epm_outtradeno = ?", 
													ZkUtil.buildWhereclByArgs((String)ev3.getData(), System.currentTimeMillis() / 1000, m1.get("outTradeNo")));
										});
									} catch (Exception e) {
										UniLog.log(e);
										ZkUtil.errMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
									}
								});
								Events.echoEvent(Events.ON_CLICK, btAdd, null);
							} else {
								lbMessage.setValue(errMsg);
								scanFlag.set(false);
							}
						} catch (Exception e) {
							UniLog.log(e);
							ZkUtil.errMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
							scanFlag.set(false);
						}
					});
				});
				ZkUtil.setEventListener(tbScan, "onScanError", ev3 -> {
					UniLog.log1("ev3:%s,%s", ev3, ev3.getData());
					ZkUtil.errMsg((String)ev3.getData());
				});
			} else
				Events.echoEvent(Events.ON_CLICK, btAdd, null);
		});
	}

	public void sortPayitemListbox() {
		Listbox lb = (Listbox)jxAdd("list_propertymgmt_payitem").getNativeObject();
		BiResult bru = getBr().getSubLink("propertymgmt.payunit");
		BiResult bri = getBr().getSubLink("propertymgmt.payitem");
		ListModelList<Object> model = (ListModelList<Object>)lb.getModel();
		model.sort((a, b) -> {
          	if (a instanceof TrStatFilter)
           		a = ((TrStatFilter)a).getTrStatIdx();
          	if (b instanceof TrStatFilter)
           		b = ((TrStatFilter)b).getTrStatIdx();
          	BiCellCollection bcia = bri.getRowCollectionO(a);
          	BiCellCollection bcib = bri.getRowCollectionO(b);
          	String unita = bcia.getString("col_c");
          	String unitb = bcib.getString("col_c");
          	String montha = bcia.getString("col_d");
          	String monthb = bcib.getString("col_d");
          	int ia = IntStream.range(0, bru.getRowCount()).filter(i -> {
          		BiCellCollection bc = bru.getRowCollectionV(i);
          		return Objects.equals(bc.getString("pu_unit"), unita);
          	}).findFirst().orElse(-1);
          	int ib = IntStream.range(0, bru.getRowCount()).filter(i -> {
          		BiCellCollection bc = bru.getRowCollectionV(i);
          		return Objects.equals(bc.getString("pu_unit"), unitb);
          	}).findFirst().orElse(-1);
          	int ii;
          	if ((ii = ia - ib) != 0)
          		return ii;
			if ((ii = Objects.compare(unita, unitb, Comparator.naturalOrder())) != 0)
				return ii;
			return Objects.compare(montha, monthb, Comparator.naturalOrder());
		});
	}
}