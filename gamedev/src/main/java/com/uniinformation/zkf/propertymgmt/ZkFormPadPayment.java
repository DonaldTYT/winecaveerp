package com.uniinformation.zkf.propertymgmt;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;

import com.kyoko.utils.UrlUtils;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.propertymgmt.BiResultParkingTmpLocPayment;
import com.uniinformation.bicore.propertymgmt.BiResultPayment;
import com.uniinformation.bicore.propertymgmt.BiResultProjectFee;
import com.uniinformation.bicore.propertymgmt.BiResultProjectPayment;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.BatchBuildPrintHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rest.propertymgmt.PropmgmtRS;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.CryptoUtil;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.MonthUtil;
import com.uniinformation.utils.NumberUtil;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.BiUtil.CheckedConsumer4;
import com.uniinformation.utils.BiUtil.CheckedConsumer5;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.webcore.propmgmtpro.PropmgmtproSessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkf.ZkCellActionForm;
import com.uniinformation.zkf.propertymgmt.ZkFormEpayment.BANK;
import com.uniinformation.zkf.propertymgmt.ZkFormEpayment.Epayment;
import static com.uniinformation.utils.BiUtil.throwFunction;
import static com.uniinformation.utils.BiUtil.throwConsumer;
import static com.uniinformation.utils.BiUtil.throwIntPredicate;
import static com.uniinformation.utils.BiUtil.throwIntConsumer;
import static com.uniinformation.utils.BiUtil.safeRunnable;
import static com.uniinformation.utils.BiUtil.buildWhereclByArgs;
import static com.uniinformation.utils.BiUtil.getTableRec;
import static com.uniinformation.utils.BiUtil.getFirstTableRec;
import static com.uniinformation.utils.BiUtil.getTableRecStream;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.nextMonth;

public class ZkFormPadPayment extends ZkCellActionForm {
	private static final int DEFAULT_PROPMGMT_LOGIN_TIMEOUT = 300;
	private static final int DEFAULT_PROPMGMT_PAGE_TIMEOUT = 120;
	private static final DecimalFormat df = new DecimalFormat("$#,##0.00");
	private String deviceId, cashierId, cashierName, paymentType, paymentUnit;
	private Map<Integer, Pair<String, Integer>> locationMap;
	private int locationRg;
	private String barcodeScanner;
	private Epayment epayment;
	private Timer uiTimer1, uiTimer2;

	private BiResultPayment paymentBr;
	private BiResultParkingTmpLocPayment packtlPaymentBr;
	private BiResultProjectPayment projectPaymentBr;
	private TableRec devTr, typeTr, blockTr, floorTr, unitTr, parktlContractTr, projectTr;
	private String requestURL;
	
	@Wire
	private Div divPageLogin, divPageMainMenu2, divPageMainMenu, divPageUnit, divPageScan, divPagePayment1, divPagePayment2, divPagePayment3, divPagePayment4, divPagePrtReceipt, divUnitContent;
	@Wire
	private Textbox tbDeviceId, /*tbDevicePassword,*/ tbCashierId;
	@Wire
	private Button btLogin, btLogout, btExitMainMenu2, btReprintReceipt, btScan, btPay2Epay, btPay2Cash, btPay4, btPrintReceipt;
	@Wire
	private Label lbUnitSubtitle, lbLabel2Unit, lbPayMonthEnd, lbPayMessage3, lbLabel1Payment5, lbLabel2Payment5;
	@Wire
	private Vlayout vlPayLast, vlPayCurr, vlPayLast1, vlPayCurr1;
	
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		UniLog.log1("p_comp:%s", p_comp);
//		customLoginUrl = "";
		needCheckLogin = false;
		requestURL = ZkUtil.getURL();
		onClickListener = event -> {
			Component c = event.getTarget();
			String cid = c.getId();
			UniLog.log1("Event name:%s, Id:%s, target:%s", event.getName(), cid, c);
			if (!(c instanceof Button)) {
				UniLog.log1("Skip event");
				return;
			}
			restartUiTimer();
			if (cid.startsWith("btExit"))
				gotoFirstPage(null);
			else if (cid.startsWith("btBuilding"))
				gotoFirstPage((Button)c);
			else if (cid.equals("btLogin"))
				login();
			else if (cid.equals("btLogout"))
				logoutCashierByConfirm();
			else if (cid.equals("btReprintReceipt"))
				reprintReceipt();
			else if (cid.equals("btScan"))
				gotoScanPage();
			else if (StringUtils.startsWith(cid, "btPmType"))
				gotoMainMenuPage((Button)c);
			else if (StringUtils.startsWithAny(cid, "btType", "btBlock", "btFloor", "btParktlCode"))
				gotoUnitPage((Button)c);
			else if (StringUtils.startsWith(cid, "btUnit")) {
				if (StringUtils.equalsAny(paymentType, "管理費", "儲備金"))
					gotoPaymentPage1((Button)c);
				else
					gotoUnitPage((Button)c);
			} else if (StringUtils.startsWithAny(cid, "btParktlContract", "btProject", "btPayMnt", "btPayPd"))
				gotoPaymentPage1((Button)c);
			else if (cid.equals("btPay1"))
				gotoPaymentPage2();
			else if (cid.equals("btPay2Epay"))
				gotoPaymentPage3(false);
			else if (cid.equals("btPay2Cash"))
				gotoPaymentPage4();
			else if (cid.equals("btPay4"))
				payCash();
			else if (cid.equals("btPrintReceipt"))
				printReceipt(getPaymentBr(), null);
		};
		super.doAfterCompose(p_comp);
		sessionHelper.setLHLang("TCHN", 2, false);

		barcodeScanner = sessionHelper.getURLParam("BarcodeScanner");
		deviceId = sessionHelper.getURLParam("deviceid");
		cashierId = (String)sessionHelper.getSessionData("cashierId");
		paymentType = null;
		
		paymentBr = (BiResultPayment) sessionHelper.newBiResult("propertymgmt.payment");
		packtlPaymentBr = (BiResultParkingTmpLocPayment) sessionHelper.newBiResult("propertymgmt.ParkingTmpLocPayment");
		projectPaymentBr = (BiResultProjectPayment) sessionHelper.newBiResult("propertymgmt.ProjectPayment");
		SelectUtil su = paymentBr.getSelectUtil();
		/*if (StringUtils.isNotBlank(deviceId)) {
			devTr = getTableRec(su, "select * from devicelogin, location where ldv_login = ? and lc_rg = ldv_lcrg", deviceId);
			if (devTr.getRecordCount() > 0) {
				devTr.setRecPointer(0);
				password = devTr.getFieldString("ldv_password");
				locationRg = devTr.getFieldInt("lc_rg");
				Erpv4Config.setDefaultLcrg(sessionHelper, locationRg);
				typeTr = getTableRec(su, "select distinct col_a ptype from property where col_b = ? ", devTr.getFieldString("lc_desc"));
			}
		}*/
		if (StringUtils.isNotBlank(cashierId)) {
			TableRec tr = getFirstTableRec(su, "select col_b from cashier where col_a = ?", cashierId).orElse(null);
			if (tr != null)
				cashierName = tr.getFieldString("col_b");
			else
				cashierId = null;
		}

		uiTimer1 = ZkUtil.timerEvent(uiTimer1, p_comp, Erpv4Config.getInteger(sessionHelper, "PAD_PAYMENT_LOGIN_TIMEOUT", DEFAULT_PROPMGMT_LOGIN_TIMEOUT) * 1000, true, true, () -> {
			safeRunnable(() -> {
				if (divPageMainMenu2.isVisible())
					logoutCashier();
			}).run();
			return false;
		});
		uiTimer2 = ZkUtil.timerEvent(uiTimer2, p_comp, Erpv4Config.getInteger(sessionHelper, "PAD_PAYMENT_PAGE_TIMEOUT", DEFAULT_PROPMGMT_PAGE_TIMEOUT) * 1000, true, true, () -> {
			safeRunnable(() -> {
				Sessions.getCurrent().setMaxInactiveInterval(-1);
				if (!divPageMainMenu2.isVisible() && !divPageLogin.isVisible())
					gotoFirstPage(null);
			}).run();
			return false;
		});
		gotoFirstPage(null);
	}
	
	private void hideAllPage() {
		Selectors.find("div[id^='divPage']").forEach(div -> div.setVisible(false));
	}
	
	private void gotoFirstPage(Button btn) throws Exception {
		Stream.of(paymentBr, packtlPaymentBr, projectPaymentBr).forEach(br -> {
			br.rollbackWork();
			br.clearCurrentRec();
		});
		disConnectScanner();
		hideAllPage();
		if (sessionHelper.isLogin() && !super.validateURL(requestURL)) {
			ZkUtil.errMsg("訪問被拒絕");
			return;
		}
		String deviceIdStr = null, cashierStr = null;
		locationRg = 0;
		if (btn != null)
			locationRg = (int)btn.getAttribute("rg");
		else if (StringUtils.isNotBlank(deviceId)) {
			SelectUtil su = paymentBr.getSelectUtil();
			devTr = getFirstTableRec(su, "select * from devicelogin left join location on lc_rg = ldv_lcrg where ldv_login = ?", deviceId).orElse(null);
			if (devTr == null) {
				ZkUtil.errMsg("設備號碼錯誤");
				return;
			}
			if (sessionHelper.isLogin() && !Objects.equals(devTr.getFieldString("ldv_type"), "PMS MiniPOS")) {
				ZkUtil.errMsg("訪問被拒絕");
				return;
			}
			locationMap = new LinkedHashMap<>();
			if (devTr.getFieldInt("lc_rg") > 0 && devTr.getFieldInt("lc_epayment") > 0)
				locationMap.put(devTr.getFieldInt("lc_rg"), Pair.of(devTr.getFieldString("lc_desc"), devTr.getFieldInt("lc_epayment")));
			Map<Integer, Pair<String, Integer>> extraLocationMap = Stream.of("ldv_lcdesc2", "ldv_lcdesc3", "ldv_lcdesc4").<CellCollection>map(throwFunction(s -> {
				String d = devTr.getFieldString(s);
				if (StringUtils.isBlank(d))
					return null;
				return getFirstTableRec(su, "select lc_rg, lc_desc, lc_epayment from location where lc_desc = ? and lc_epayment > 0", d).map(throwFunction(tr -> tr.toCellCollection(0))).orElse(null);
			})).filter(Objects::nonNull).collect(Collectors.toMap(c -> c.getInt("lc_rg"), c -> Pair.of(c.getString("lc_desc"), c.getInt("lc_epayment")), (o, n) -> n,
					() -> new LinkedHashMap<Integer, Pair<String, Integer>>()));
			locationMap.putAll(extraLocationMap);
			UniLog.log1("locationMap:%s", locationMap);
			Selectors.find(divPageMainMenu2, "button.house").forEach(bt -> bt.getParent().removeChild(bt));
			for (int rg : locationMap.keySet()) {
				Button bt = (Button)divPageMainMenu2.getFellow("btPmTypeMgtFee").clone();
				bt.setId("btBuilding" + rg);
				bt.setAttribute("rg", rg);
				bt.setImage("/propertymgmt/gra/House.svg");
				bt.setLabel(locationMap.get(rg).getLeft());
				bt.setSclass("house");
				bt.addEventListener(Events.ON_CLICK, onClickListener);
				Selectors.find(divPageMainMenu2, "div.content1").forEach(div -> div.appendChild(bt));
			}
			if (locationMap.size() == 1)
				locationRg = locationMap.keySet().stream().findFirst().orElse(0);
		}
		devTr.setField("ldv_lcrg", locationRg);
		devTr.setField("lc_rg", locationRg);
		Pair<String, Integer> p = locationMap.get(locationRg);
		if (p != null) {
			devTr.setField("lc_desc", p.getLeft());
			devTr.setField("lc_epayment", p.getRight());
		} else {
			devTr.setField("lc_desc", "");
			devTr.setField("lc_epayment", 0);
		}
		Erpv4Config.setDefaultLcrg(sessionHelper, locationRg);
		if (locationRg != 0)
			typeTr = getTableRec(paymentBr.getSelectUtil(), "select distinct col_a ptype from property where col_b = ? ", devTr.getFieldString("lc_desc"));
		else
			typeTr = null;

		if (sessionHelper.isLogin() && StringUtils.isNotBlank(cashierId)) {
			divPageMainMenu2.setVisible(true);
			Selectors.find(divPageMainMenu2, "button.house").forEach(bt -> bt.setVisible(locationRg == 0));
			Selectors.find(divPageMainMenu2, "button.fee").forEach(bt -> bt.setVisible(locationRg != 0));
			Selectors.find(divPageMainMenu2, "div.header label").forEach(lb -> ((Label)lb).setValue(locationRg != 0 ? "選擇繳費類型 Select payment type" : "選擇大廈 Select Building"));
			if (locationRg == 0 || locationMap.size() == 1) {
				btReprintReceipt.setVisible(true);
				btLogout.setVisible(true);
				btExitMainMenu2.setVisible(false);
			} else {
				btReprintReceipt.setVisible(false);
				btLogout.setVisible(false);
				btExitMainMenu2.setVisible(true);
			}
			btReprintReceipt.setDisabled(StringUtils.isBlank((String)sessionHelper.getSessionData("lastVoucherNo")));

			if (locationRg != 0) {
				String loc = devTr.getFieldString("lc_desc");
				Selectors.find(divPageMainMenu2, "button[id^='btPmType']").forEach(throwConsumer(bt -> {
					switch (bt.getId()) {
					case "btPmTypeResFee":
						bt.setVisible(
							ZkUtil.hasTableRec(paymentBr.getSelectUtil(), "select serial_id from contractfee where col_a = ? and col_e > 0 and col_g = 'Y'", new Wherecl().appendArgument(loc))
						);
						break;
					case "btPmTypeProjectFee":
						bt.setVisible(
							ZkUtil.hasTableRec(paymentBr.getSelectUtil(), "select serial_id from unitprojectfee where upf_location = ? and upf_allocamt > upf_totpayamt", new Wherecl().appendArgument(loc))
						);
						break;
					case "btPmTypeParkingFee":
						bt.setVisible(
							ZkUtil.hasTableRec(paymentBr.getSelectUtil(), "select cpt_unit from contractparkingtl where cpt_building = ? and cpt_totalamount > cpt_paidamount", new Wherecl().appendArgument(loc))
						);
						break;
					}
				}));
			} else
				Selectors.find(divPageMainMenu2, "button[id^='btPmType']").forEach(bt -> bt.setVisible(false));
		} else {
			divPageLogin.setVisible(true);
			tbDeviceId.setText(deviceId);
			//tbDevicePassword.setText(null);
			//tbDevicePassword.getParent().setVisible(!sessionHelper.isLogin());
			tbCashierId.setText(null);
			connectNfcScanner();
			ZkUtil.setEventListener(divPageLogin, "onPostNfcData", ev -> {
				if (!divPageLogin.isVisible())
					return;
				UniLog.log1("Nfc card received %s", ev.getData());
				if (!Selectors.find(".zkbi-messagebox-window").isEmpty())
					return;
				UniLog.log("setText ok");
				tbCashierId.setText((String)ev.getData());
				Events.echoEvent(Events.ON_CLICK, btLogin, null);
			});
			/*ZkUtil.timerEvent(null, divPageLogin, 20000, () -> {
				ZkUtil.js("postNfcData('%s')", "001");
			});*/
		}
		if (StringUtils.isNotBlank(deviceId) && StringUtils.isNotBlank(cashierId)) {
			deviceIdStr = "本機ID號： " + deviceId;
			cashierStr = "收費員：" + cashierName;
		}
		String lcdesc = devTr.getFieldString("lc_desc");
		ZkUtil.js("$('span.location').text('%s');$('span.deviceid').text('%s');$('span.cashier').text('%s')", 
				escapeJava(StringUtils.defaultIfBlank(lcdesc, "\u00A0")),
				escapeJava(StringUtils.defaultIfBlank(deviceIdStr, "\u00A0")),
				escapeJava(StringUtils.defaultIfBlank(cashierStr, "\u00A0")));
		Stream.of("lbLabel2MainMenu", "lbLabel2Scan").forEach(k -> {
			Selectors.find(String.format("label[id^='%s']", k)).forEach(lb -> ((Label)lb).setValue(lcdesc));
		});
	}

	private void login() throws Exception {
		if (!sessionHelper.isLogin()) {
			//if (StringUtils.isNotBlank(password) && password.equals(tbDevicePassword.getText())) {
				ReturnMsg rtn = sessionHelper.login("dev#0000", "irns481");
				if (rtn.getStatus()) {
					sessionHelper.setVcode(deviceId);
					//tbDevicePassword.setText(null);
					//tbDevicePassword.getParent().setVisible(false);
					Erpv4Config.setDefaultLcrg(sessionHelper, locationRg);
					UniLog.log("Login device Ok");
				}
			//}
		}
		if (sessionHelper.isLogin()) {
			if (StringUtils.isNotBlank(tbCashierId.getText())) {
				getFirstTableRec(paymentBr.getSelectUtil(), "select col_a, col_b from cashier where (col_a = ? or col_c = ?) and col_e <> 'Y'", 
													tbCashierId.getText(), tbCashierId.getText()).ifPresent(throwConsumer(tr -> {
					cashierId = tr.getFieldString("col_a");
					cashierName = tr.getFieldString("col_b");
					sessionHelper.putSessionData("cashierId", cashierId);
					UniLog.log("Login cashier Ok");
				}));
			}
		}
		if (sessionHelper.isLogin() && StringUtils.isNotBlank(cashierId))
			gotoFirstPage(null);
		else
			ZkUtil.errMsg("登入失敗");
	}

	private void logoutCashierByConfirm() throws Exception {
		ZkBiMsgboxButton[] btns = Stream.of("Ok", "Cancel").map(ZkBiMsgboxButton::new).toArray(ZkBiMsgboxButton[]::new);
		btns[1].addSclass("red");
		new ZkBiMsgbox(sessionHelper).setType(ZkBiMsgbox.Type.question).setContent("確定登出?").setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
				if (btn.getIdx() == 0)
					logoutCashier();
			}
		}).build().doModal();
	}

	private void logoutCashier() throws Exception {
		sessionHelper.removeSessionData("cashierId");
		cashierId = null;
		cashierName = null;
		paymentType = null;
		gotoFirstPage(null);
	}

	private void gotoMainMenuPage(Button btn) throws Exception {
		hideAllPage();
		divPageMainMenu.setVisible(true);
		paymentType = btn.getLabel();
		Selectors.find("label[id^='lbLabel1']").forEach(lb -> ((Label)lb).setValue(paymentType));
		Selectors.find(divPageMainMenu, "div.content1 button").forEach(bt -> {
			bt.setVisible(false);
			bt.removeAttribute("recIdx");
		});
		IntStream.range(0, typeTr.getRecordCount()).forEach(throwIntConsumer(i -> {
			typeTr.setRecPointer(i);
			Selectors.find(divPageMainMenu, String.format("div.content1 button[ptype='%s']", typeTr.getFieldString("ptype"))).stream().findFirst().map(bt -> (Button)bt).ifPresent(bt -> {
				bt.setVisible(true);
				bt.setAttribute("recIdx", i);
			});
		}));
		if (!Selectors.find(divPageMainMenu, "div.content1 button[visible=true]").isEmpty())
			btScan.setVisible(true);
		epayment = new MyEpayment();
	}
	
	private void gotoUnitPage(Button btn) throws Exception {
		hideAllPage();
		divPageUnit.setVisible(true);
		Component subtitleParent = lbUnitSubtitle.getParent();
		subtitleParent.setVisible(btn == null || StringUtils.startsWithAny(btn.getId(), "btUnit", "btParktlCode"));
		Selectors.find(divPageUnit, ".content").forEach(c -> ((Div)c).setHeight(subtitleParent.isVisible() ? "calc(100vh - 386px)" : "calc(100vh - 316px)"));

		divUnitContent.getChildren().clear();
		if ((btn == null || btn.getId().startsWith("btUnit"))) {
			if (btn != null) {
				unitTr.setRecPointer((int)btn.getAttribute("recIdx"));
				setupPaymentUnit();
			}
			lbLabel2Unit.setValue(paymentUnit);
			if (paymentType.equals("暫放費")) {
				lbUnitSubtitle.setValue("選擇位置編號  Select location number");
				selectParkTlContractTr();
				AtomicInteger i = new AtomicInteger();
				getTableRecStream(parktlContractTr).map(c -> c.getString("cpt_tlcode")).distinct().forEach(code -> {
					Button bt;
					divUnitContent.appendChild(bt = newUnitButton("btParktlCode", i.getAndIncrement(), code));
					bt.setAttribute("key", code);
				});
			} else if (paymentType.equals("維修分攤費")) {
				lbUnitSubtitle.setValue("選擇項目編號  Select project number");
				selectProjectTr();
				IntStream.range(0, projectTr.getRecordCount()).forEach(throwIntConsumer(i -> {
					projectTr.setRecPointer(i);
					divUnitContent.appendChild(new Hlayout() {{
						setStyle("padding:6px 0");
						setValign("middle");
						appendChild(newUnitButton("btProject", i, projectTr.getFieldString("upf_projectno")));
						appendChild(new Label(projectTr.getFieldString("upf_projectname")) {{
							setStyle("font-size:var(--content-font-size)");
						}});
					}});
				}));
			}
		} else if (btn.getId().startsWith("btParktlCode")) {
			lbUnitSubtitle.setValue("選擇合同起始月  Select contract start month");
			lbLabel2Unit.setValue(paymentUnit);
			String tlcode = (String)btn.getAttribute("key");
			IntStream.range(0, parktlContractTr.getRecordCount()).filter(throwIntPredicate(i -> Objects.equals(parktlContractTr.getFieldString("cpt_tlcode", i), tlcode))).forEach(throwIntConsumer(i -> {
				parktlContractTr.setRecPointer(i);
				divUnitContent.appendChild(newUnitButton("btParktlContract", i, 
									parktlContractTr.getFieldString("cpt_constartmonth") + " / " + parktlContractTr.getFieldString("cpt_conendmonth")));
			}));
		} else if (btn.getId().startsWith("btType")) {
			typeTr.setRecPointer((int)btn.getAttribute("recIdx"));
			lbLabel2Unit.setValue(typeTr.getFieldString("ptype"));
			selectBlockTr();
			for (int i = 0; i < blockTr.getRecordCount(); i++) {
				blockTr.setRecPointer(i);
				Button bt;
				divUnitContent.appendChild(bt = newUnitButton("btBlock", i, blockTr.getFieldString("pblock")));
				if (blockTr.getRecordCount() == 1 && StringUtils.isBlank(blockTr.getFieldString("pblock"))) {
					divPageUnit.setVisible(false);
					Events.echoEvent(Events.ON_CLICK, bt, null);
				}
			}
		} else if (btn.getId().startsWith("btBlock")) {
			blockTr.setRecPointer((int)btn.getAttribute("recIdx"));
			lbLabel2Unit.setValue(blockTr.getFieldString("pblock"));
			selectFloorTr();
			for (int i = 0; i < floorTr.getRecordCount(); i++) {
				floorTr.setRecPointer(i);
				divUnitContent.appendChild(newUnitButton("btFloor", i, floorTr.getFieldString("pfloor")));
			}
		} else if (btn.getId().startsWith("btFloor")) {
			floorTr.setRecPointer((int)btn.getAttribute("recIdx"));
			lbLabel2Unit.setValue(floorTr.getFieldString("pfloor"));
			selectUnitTr();
			for (int i = 0; i < unitTr.getRecordCount(); i++) {
				unitTr.setRecPointer(i);
				divUnitContent.appendChild(newUnitButton("btUnit", i, unitTr.getFieldString("punit")));
			}
		}
	}

	private void gotoScanPage() {
		hideAllPage();
		divPageScan.setVisible(true);
		connectBarcodeScanner();
		ZkUtil.setEventListener(divPageScan, "onPostQrCodeData", new EventListener<Event>() {
			private boolean scanOk;
			@Override
			public void onEvent(Event ev) throws Exception {
				if (scanOk || !divPageScan.isVisible())
					return;
				restartUiTimer();
				UniLog.log1("STATE_WAITSCANSLIP qrCodeData received %s", ev.getData());
				Map<String,String> pmap = UrlUtils.getQueryParams((String)ev.getData());
				String punit = null;
				if (pmap != null) 
					punit = pmap.get("punit");
				UniLog.log1("punit:%s", punit);
				if (punit != null && syncPropertyByPropertyUnit(punit)) {
					scanOk = true;
					disConnectScanner();
					if (StringUtils.equalsAny(paymentType, "管理費", "儲備金"))
						gotoPaymentPage1(null);
					else {
						paymentUnit = punit;
						gotoUnitPage(null);
					}
				} else
					ZkUtil.showErrMsg("無效二維碼");
			}
		});
		/*ZkUtil.timerEvent(null, divPageScan, 5000, () -> {
			try {
				String s = UrlUtils.buildURLWithParams("https://www.erpv4.com/qrdecode", "agid", "pgmt0001", "punit", "測試大廈 車位 03樓 293");
				ZkUtil.js("postQrCodeData('%s')", s);
			} catch (Exception e) {
				UniLog.log(e);
			}
		});*/
	}

	private void handleMgrResFee(Button btClick) throws Exception {
		if (btClick != null && btClick.getId().startsWith("btUnit"))
			unitTr.setRecPointer((int)btClick.getAttribute("recIdx"));
		String colm = paymentBr.getCellString("col_m");
		String month = (btClick != null && btClick.getId().startsWith("btPayMnt")) ? (String)btClick.getAttribute("month") : null;
		UniLog.log1("colm:%s, month:%s", colm, month);
		if (StringUtils.isNotBlank(colm) && StringUtils.equals(colm, month))
			return;
		hideAllPage();
		divPagePayment1.setVisible(true);
		paymentBr.clearCurrentRec();
		BiResult sr = paymentBr.getSubLink("propertymgmt.payunit");
		CellCollection col = sr.newRowCollection();
		sr.addSubRecord(col, -1, "");
		col.getCell("pu_block").set(blockTr.getFieldString("pblock"));
		col.getCell("pu_floor").set(floorTr.getFieldString("pfloor"));
		col.getCell("pu_flat").set(unitTr.getFieldString("punit"));
		if (col.getCellString("pu_type").equals(typeTr.getFieldString("ptype"))) {
			paymentBr.getCell("col_n").set(paymentType.equals("管理費"));
			paymentBr.getCell("col_o").set(paymentType.equals("儲備金"));
			if (btClick != null && btClick.getId().startsWith("btPayMnt"))
				paymentBr.syncPayItemFromPayUnit(null, month);
			else {
				paymentBr.syncPayItemFromPayUnit(null);
				Selectors.find(divPagePayment1, "button[id^='btPayMnt']").forEach(bt -> {
					int i = Integer.parseInt(bt.getId().substring("btPayMnt".length()));
					bt.setAttribute("month", MonthUtil.nextNmonth(paymentBr.getCellString("col_m"), i - 1));
				});
			}
			paymentBr.syncPaymentFromPayItem(false);

			//clear ui
			Selectors.find(divPagePayment1, "button[id^='btPayMnt']").forEach(bt -> bt.setVisible(true));
			Selectors.find(divPagePayment1, "button[id^='btPayPd']").forEach(bt -> bt.setVisible(false));
			Selectors.find("label[id^='lbLabel2Payment']").forEach(lb -> ((Label)lb).setValue(null));
			Selectors.find("label[id^='lbPayAmount']").forEach(lb -> ((Label)lb).setValue(null));
			lbPayMonthEnd.setValue(null);

			BiResult sru = paymentBr.getSubLink("propertymgmt.payunit");
			BiResult sri = paymentBr.getSubLink("propertymgmt.payitem");
			Vector<BiCellCollection> slu = sru.getRowCollectionList();
			Vector<BiCellCollection> sli = sri.getRowCollectionList();
			//UniLog.log1("paymentBr:%s", ZkUtil.getBiResultRecordJson(paymentBr));
			//UniLog.log1("payunit:%s", ZkUtil.getBiResultRecordJson(sru));
			//UniLog.log1("payitem:%s", ZkUtil.getBiResultRecordJson(sri));
			if (!slu.isEmpty()) {
				BiCellCollection bc = slu.get(0);
				String unit = bc.getCellString("pu_unit");
				Selectors.find("label[id^='lbLabel2Payment']").forEach(lb -> ((Label)lb).setValue(unit));
				String mfrom = bc.getCellString("pu_mgtstart");
				String rfrom = bc.getCellString("pu_resstart");
				double mfeePerMonth = 0.0, rfeePerMonth = 0.0;
				List<Map<String, Object>> jlist = GsonUtil.convertToCollection(bc.getCellString("pu_jsondet"));
				if (CollectionUtils.isNotEmpty(jlist)) {
					for (Map<String, Object> m : jlist) {
						String constart = (String)m.get("constart");
						String conend = MonthUtil.nextNmonth(constart, (int)(double)(m.get("noofmonth")));
						if (StringUtils.isNotBlank(mfrom) && mfrom.compareTo(constart) >= 0 && mfrom.compareTo(conend) < 0)
							mfeePerMonth = (double)m.get("mgtfeepermon");
						if (StringUtils.isNotBlank(rfrom) && rfrom.compareTo(constart) >= 0 && rfrom.compareTo(conend) < 0)
							rfeePerMonth = (double)m.get("resfeepermon");
					}
				} else
					ZkUtil.errMsg("不用繳費\nNo fee required");
				CheckedConsumer4<String, String, Double, String> action = (unitlabel, feefrom, fee, value) -> {
					Label lb1 = (Label) vlPayLast.getFellowIfAny(unitlabel);
					Label lb1l = (Label) vlPayLast.getFellowIfAny(unitlabel + "label");
					if (lb1 != null) {
						if (StringUtils.isNotBlank(feefrom) && fee > 0.0) {
							lb1.setValue(value);
							lb1.setVisible(true);
						} else
							lb1.setVisible(false);
					} 
					if (lb1l != null)
						lb1l.setVisible(lb1.isVisible());
				};
				action.accept("unit_mgtfrom", mfrom, mfeePerMonth, mfrom);
				action.accept("unit_mgtfee", mfrom, mfeePerMonth, df.format(mfeePerMonth));
				action.accept("unit_resfrom", rfrom, rfeePerMonth, rfrom);
				action.accept("unit_resfee", rfrom, rfeePerMonth, df.format(rfeePerMonth));

				List<Map<String, Object>> list = new ArrayList<>();
				sli.stream().filter(bcc -> bcc.getCellDouble("col_e") > 0.0 || bcc.getCellDouble("col_f") > 0.0)
							.sorted((a, b) -> a.getCellString("col_d").compareTo(b.getCellString("col_d"))).forEach(bcc -> {
					Map<String, Object> lm = list.isEmpty() ? null : list.get(list.size() - 1);
					if (lm != null && (double)lm.get("mgtfee") == bcc.getCellDouble("col_e") && (double)lm.get("resfee") == bcc.getCellDouble("col_f") 
							&& MonthUtil.nextNmonth((String)lm.get("endMonth"), 1).compareTo(bcc.getCellString("col_d")) == 0)
						lm.put("endMonth", bcc.getCellString("col_d"));
					else
						list.add(MapUtil.of("mgtfee", bcc.getCellDouble("col_e"), "resfee", bcc.getCellDouble("col_f"), "startMonth", bcc.getCellString("col_d"), "endMonth", bcc.getCellString("col_d")));
				});
				Div unit_mgtcurdiv = (Div)vlPayCurr.getFellow("unit_mgtcurdiv", true);
				Div unit_rescurdiv = (Div)vlPayCurr.getFellow("unit_rescurdiv", true);
				Selectors.find(vlPayCurr, "div[vcomp=true]").forEach(c -> c.getParent().removeChild(c));
				CheckedConsumer5<Div, Map<String, Object>, String, String, String> action1 = (curdiv, m, lcurl, lcur, lcurfee) -> {
					double fee = (double)m.get(curdiv == unit_rescurdiv ? "resfee" : "mgtfee");
					String startMonth = (String)m.get("startMonth");
					String endMonth = (String)m.get("endMonth");
					if (fee <= 0.0)
						return;
					Div div = (Div)curdiv.clone();
					Label lbcurl = (Label)div.getFellow(lcurl, true);
					Label lbcur = (Label)div.getFellow(lcur, true);
					Label lbcurfee = (Label)div.getFellow(lcurfee, true);
					div.setId(null);
					div.setAttribute("vcomp", true);
					lbcurl.setId(null);
					lbcur.setId(null);
					lbcurfee.setId(null);
					div.setVisible(true);
					curdiv.getParent().appendChild(div);

					int count = MonthUtil.getMonth(endMonth) - MonthUtil.getMonth(startMonth) + 1;
					String labelValue = lbcur.getValue();
					for (Entry<String, String> entry1 : MapUtil.of2("from", startMonth, "to", endMonth, "fee", df.format(fee), "count", String.valueOf(count)).entrySet())
						labelValue = labelValue.replace(entry1.getKey(), entry1.getValue());
					lbcur.setValue(labelValue);
					lbcurfee.setValue(df.format(fee * count));
				};
				for (Map<String, Object> m : list) {
					action1.accept(unit_mgtcurdiv, m, "unit_mgtcurl", "unit_mgtcur", "unit_mgtcurfee");
					action1.accept(unit_rescurdiv, m, "unit_rescurl", "unit_rescur", "unit_rescurfee");
				}
				lbPayMonthEnd.setValue(paymentBr.getCellString("col_m"));
				Selectors.find("label[id^='lbPayAmount']").forEach(lb -> ((Label)lb).setValue(df.format(getPaymentBr().getCellDouble("vcol_actualfee"))));
			}
		} else
			clickExitButton();
	}

	private void handleParktlFee(Button btClick) throws Exception {
		if (btClick != null && btClick.getId().startsWith("btParktlContract"))
			parktlContractTr.setRecPointer((int)btClick.getAttribute("recIdx"));
		int payMonthCount = (btClick != null && btClick.getId().startsWith("btPayMnt")) ? Integer.parseInt(btClick.getId().substring("btPayMnt".length())) : 1;
		UniLog.log1("payMonthCount:%s", payMonthCount);
		if (packtlPaymentBr.getSubLinkResult("propertymgmt.PayParkingItem").stream().anyMatch(c -> c.getInt("col_f") == payMonthCount))
			return;
		hideAllPage();
		divPagePayment1.setVisible(true);
		packtlPaymentBr.clearCurrentRec();
		BiResult sr = packtlPaymentBr.getSubLink("propertymgmt.PayParkingItem");
		CellCollection col = sr.newRowCollection();
		sr.addSubRecord(col, -1, "");
		col.getCell("col_c").set(parktlContractTr.getFieldString("cpt_tlcode"));
		col.getCell("col_d").set(parktlContractTr.getFieldString("cpt_constartmonth"));
		packtlPaymentBr.calcPaidPeriodAndAmount(col, false);
		col.getCell("vcol_f").set("" + payMonthCount);
		BiResultParkingTmpLocPayment.calcAmount(col);
		packtlPaymentBr.syncPaymentFromPayItem();
		//UniLog.log1("PayParkingItem:%s", ZkUtil.getBiResultRecordJson(sr));

		Selectors.find(divPagePayment1, "button[id^='btPayMnt']").forEach(bt -> bt.setVisible(false));
		Selectors.find(divPagePayment1, "button[id^='btPayPd']").forEach(bt -> bt.setVisible(false));
		Selectors.find("label[id^='lbLabel2Payment']").forEach(lb -> ((Label)lb).setValue(String.format("%s (%s) , 合同 (Contract) %s 至 %s", col.getString("col_c"), col.getString("ccol_f"), col.getString("col_d"), col.getString("ccol_e"))));
		Selectors.find("label[id^='lbLabel3Payment']").forEach(lb -> ((Label)lb).setValue(col.getString("ccol_c")));
		Selectors.find("label[id^='lbPayAmount']").forEach(lb -> ((Label)lb).setValue(df.format(packtlPaymentBr.getCellDouble("col_f"))));
		lbPayMonthEnd.setValue(nextMonth(col.getString("col_e"), payMonthCount - 1));
		BiResultParkingTmpLocPayment.getPeriodRange(col).forEach(cnt -> {
			Button bt = (Button)divPagePayment1.getFellowIfAny("btPayMnt" + cnt);
			if (bt != null)
				bt.setVisible(true);
		});
		vlPayLast1.getChildren().clear();
		vlPayCurr1.getChildren().clear();
		vlPayLast1.appendChild(new Label("已繳交："));
		if (StringUtils.isNotBlank(col.getString("vcol_paidendmonth"))) {
			vlPayLast1.appendChild(new Div() {{
				setStyle("display:flex;align-items:center");
				appendChild(new Label("暫放費：" + col.getString("vcol_paidendmonth")));
				appendChild(new Span() {{ setStyle("flex:auto"); }});
				appendChild(new Label("每月" + df.format(col.getDouble("ccol_k"))));
			}});
		}
		vlPayCurr1.appendChild(new Label(String.format("本次繳交：%s 至 %s (%d個月)", col.getString("col_d"), col.getString("ccol_e"), payMonthCount)));
		vlPayCurr1.appendChild(new Div() {{
			setStyle("display:flex;align-items:center");
			appendChild(new Label("暫放費："));
			appendChild(new Span() {{ setStyle("flex:auto"); }});
			appendChild(new Label(String.format("%s*%d = %s", df.format(col.getDouble("ccol_k")), col.getInt("col_f"), df.format(col.getDouble("col_g")))));
		}});
	}

	private void handleProjectFee(Button btClick) throws Exception {
		if (btClick != null && btClick.getId().startsWith("btProject"))
			projectTr.setRecPointer((int)btClick.getAttribute("recIdx"));
		int payPeriodCount = (btClick != null && btClick.getId().startsWith("btPayPd")) ? Integer.parseInt(btClick.getId().substring("btPayPd".length())) : 1;
		UniLog.log1("payPeriodCount:%s", payPeriodCount);
		if (projectPaymentBr.getSubLinkResult("propertymgmt.PayProjectItem").stream().anyMatch(c -> c.getInt("col_f") == payPeriodCount))
			return;
		hideAllPage();
		divPagePayment1.setVisible(true);
		projectPaymentBr.clearCurrentRec();
		BiResult sr = projectPaymentBr.getSubLink("propertymgmt.PayProjectItem");
		CellCollection col = sr.newRowCollection();
		sr.addSubRecord(col, -1, "");
		col.getCell("col_c").set(projectTr.getFieldString("upf_projectno"));
		col.getCell("col_d").set(projectTr.getFieldString("upf_unit"));
		projectPaymentBr.calcPaidPeriodAndAmount(col, false);
		col.getCell("vcol_f").set("" + payPeriodCount);
		BiResultProjectPayment.calcAmount(col);
		projectPaymentBr.syncPaymentFromPayItem();
		//UniLog.log1("PayProjectItem:%s", ZkUtil.getBiResultRecordJson(sr));

		Selectors.find(divPagePayment1, "button[id^='btPayMnt']").forEach(bt -> bt.setVisible(false));
		Selectors.find(divPagePayment1, "button[id^='btPayPd']").forEach(bt -> bt.setVisible(false));
		Selectors.find("label[id^='lbLabel2Payment']").forEach(lb -> ((Label)lb).setValue(col.getString("col_d")));
		Selectors.find("label[id^='lbLabel3Payment']").forEach(lb -> ((Label)lb).setValue(col.getString("col_c") + " - " + col.getString("pjf_name")));
		Selectors.find("label[id^='lbPayAmount']").forEach(lb -> ((Label)lb).setValue(df.format(projectPaymentBr.getCellDouble("col_f"))));
		lbPayMonthEnd.setValue("繳費");
		BiResultProjectPayment.getPeriodRange(col).forEach(cnt -> {
			Button bt = (Button)divPagePayment1.getFellowIfAny("btPayPd" + cnt);
			if (bt != null)
				bt.setVisible(true);
		});
		vlPayLast1.getChildren().clear();
		vlPayCurr1.getChildren().clear();
		vlPayLast1.appendChild(new Label("已繳交："));
		String[] labels = BiResultProjectFee.periodColumnLabelList.stream().map(s -> s.replace("col_", "pjfu_")).toArray(String[]::new);
		int pd = col.getInt("vcol_paidperiod");
		if (pd > 0) {
			double amt = col.getDouble(labels[pd - 1]);
			vlPayLast1.appendChild(new Div() {{
				setStyle("display:flex;align-items:center");
				appendChild(new Label("維修分攤費："));
				appendChild(new Span() {{ setStyle("flex:auto"); }});
				appendChild(new Label(String.format("第%s期 %s", NumberUtil.toTChinese(pd), df.format(amt))));
			}});
		}
		vlPayCurr1.appendChild(new Label("本次繳交："));
		IntStream.range(col.getInt("col_e") - 1, col.getInt("col_e") + col.getInt("col_f") - 1).forEach(i -> {
			double amt = col.getDouble(labels[i]);
			vlPayCurr1.appendChild(new Div() {{
				setStyle("display:flex;align-items:center");
				appendChild(new Label("維修分攤費："));
				appendChild(new Span() {{ setStyle("flex:auto"); }});
				appendChild(new Label(String.format("第%s期 %s", NumberUtil.toTChinese(i + 1), df.format(amt))));
			}});
		});
	}
	
	private void gotoPaymentPage1(Button btClick) throws Exception {
		Selectors.find(divPagePayment1, "div.header1").forEach(bt -> bt.setVisible(StringUtils.equalsAny(paymentType, "暫放費", "維修分攤費")));
		Selectors.find(divPagePayment1, "vlayout[id^='vlPay']").forEach(vl -> {
			((Vlayout)vl).setHeight(String.format("calc(100vh - %dpx)", StringUtils.equalsAny(paymentType, "暫放費", "維修分攤費") ? 470 : 410));
			vl.setVisible(false);
		});
		lbPayMonthEnd.getParent().getFirstChild().setVisible(true);
		switch (paymentType) {
		case "管理費":
		case "儲備金":
			vlPayLast.setVisible(true);
			vlPayCurr.setVisible(true);
			handleMgrResFee(btClick);
			break;
		case "暫放費":
			vlPayLast1.setVisible(true);
			vlPayCurr1.setVisible(true);
			handleParktlFee(btClick);
			break;
		case "維修分攤費":
			vlPayLast1.setVisible(true);
			vlPayCurr1.setVisible(true);
			lbPayMonthEnd.getParent().getFirstChild().setVisible(false);
			handleProjectFee(btClick);
			break;
		}
	}

	private void gotoPaymentPage2() throws Exception {
		if (!checkPaymentFee())
			return;
		hideAllPage();
		divPagePayment2.setVisible(true);
		btPay2Cash.getParent().setVisible(devTr.getFieldInt("lc_epayment") > 0);
		btPay2Epay.getParent().setVisible(devTr.getFieldInt("lc_epayment") > 1);
	}

	private void gotoPaymentPage3(boolean isRetry) {
		if (!checkPaymentFee())
			return;
		hideAllPage();
		divPagePayment3.setVisible(true);
		if (!isRetry)
			lbPayMessage3.setValue(null);
		connectBarcodeScanner();
		ZkUtil.setEventListenerForCallOne(divPagePayment3, "onPostQrCodeData", ev -> {
			if (!divPagePayment3.isVisible())
				return;
			disConnectScanner();
			restartUiTimer();
			UniLog.log1("STATE_WAITPAYMENT qrCodeData received %s", ev.getData());
			lbPayMessage3.setValue("正在處理，請稍候");
			double actualFee = getPaymentBr().getCellDouble("vcol_actualfee");
			try {
				Map<String, Object> m = epayment.b2cPayment(BANK.fromIndex(devTr.getFieldInt("lc_epayment")), actualFee, (String)ev.getData());
				String errMsg = epayment.paymentFinish(m, true);
				if (errMsg == null) {
					ZkUtil.showMsg("付款成功");
					clickExitButton();
				} else {
					lbPayMessage3.setValue(errMsg);
					gotoPaymentPage3(true);
				}
			} catch (Exception e) {
				UniLog.log(e);
				ZkBiMsgbox.show(ZkBiMsgbox.Type.error, StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
				clickExitButton();
			}
		});
		/*ZkUtil.timerEvent(null, divPagePayment3, 5000, () -> {
			try {
				ZkUtil.js("postQrCodeData('xxx')");
			} catch (Exception e) {
				UniLog.log(e);
			}
		});*/
	}
	
	private void gotoPaymentPage4() {
		if (!checkPaymentFee())
			return;
		hideAllPage();
		divPagePayment4.setVisible(true);
		btPay4.setDisabled(false);
		btPrintReceipt.setDisabled(true);
	}

	private void payCash() throws Exception {
		BiResult br = getPaymentBr();
		br.getCell("col_a").set(DateUtil.now());
		br.getCell("col_g").set("現金");
		br.getCell("col_x").set(deviceId);
		br.getCell("col_aa").set(cashierId);
		br.addCurrent();
		sessionHelper.putSessionData("lastVoucherNo", br.getCellString("col_b"));
		sessionHelper.removeSessionData("lastOutTradeNo");
		btPay4.setDisabled(true);
		btPrintReceipt.setDisabled(false);
		ZkUtil.showMsg("付款成功");
	}
	
	public static void addPrintReceiptDataRecord(SessionHelper p_sh, BiResult p_br, String urlSuffix) throws Exception {
		String voucherNo = p_br.getCellString("col_b");
		//PropmgmtRS.PdfCache.getInstance().invalidate(voucherNo);
		Map<String, Pair<String, String>> m = MapUtil.ofp("BiResultPayment", "PrintPaymentInvoice2A5", "paymentreceipt",
					"BiResultParkingTmpLocPayment", "PrintParkingTmpLocPaymentInvoiceA5", "paymentreceipt3",
					"BiResultProjectPayment", "PrintProjectPaymentInvoiceA5", "paymentreceipt2");
		Pair<String, String> p = m.get(p_br.getClass().getSimpleName());
		UniLog.log1("class:%s, p:%s", p_br.getClass().getSimpleName(), p);
		BatchBuildPrintHandler ph = null;
		try {
			ph = (BatchBuildPrintHandler)ZkUtil.createConstructorHandle("com.uniinformation.dynamic.propertymgmt." + p.getLeft()).invoke();
		} catch (Throwable e) {
			UniLog.log(e);
		}
		if (ph == null)
			throw new Exception("Print function not found");
		ph.print(p_br);
		String tb = p.getRight();
		try (ByteArrayOutputStream os = new ByteArrayOutputStream();
				GZIPOutputStream gzos = new GZIPOutputStream(os)) {
			gzos.write(ph.getBuilderData());
			gzos.finish();
			byte[] data = os.toByteArray();
			ZkUtil.importAction.accept(p_sh, su -> {
				String cold = getFirstTableRec(su, "select COALESCE(col_d, '') d from "+tb+" where col_a = ? for update", voucherNo)
											.map(throwFunction(tr -> tr.getFieldString("d"))).orElse(null);
				if (cold != null) {
					if (StringUtils.isNotBlank(cold)) {
						cold = cold.trim() + "," + urlSuffix;
						if (cold.length() > 71)
							cold = cold.substring(cold.length() - 71);
					} else
						cold = urlSuffix;
					su.executeUpdate("update "+tb+" set col_b = ?, col_c = now(), col_d = ? where col_a = ?", buildWhereclByArgs(data, cold, voucherNo));
				} else
					su.executeUpdate("insert into "+tb+"(col_a, col_b, col_c, col_d) values(?,?,now(),?)", buildWhereclByArgs(voucherNo, data, urlSuffix));
			});
		}
	}
	
	private void printReceipt(BiResult br, String lastOutTradeNo) {
		hideAllPage();
		divPagePrtReceipt.setVisible(true);
		try {
			String unit = StringUtils.defaultString(getPayitemUnit(br));
			lbLabel1Payment5.setValue(getPaymentType(br));
			lbLabel2Payment5.setValue(unit);
			String voucherNo = br.getCellString("col_b");
			String transNo = "";
			double totalFee = 0.0;
			long transTime = br.getCellDate("col_a").getTime() / 1000;
			UniLog.log1("voucherNo:%s, br:%s", voucherNo, br);
			if (StringUtils.isNotBlank(lastOutTradeNo)) {
				TableRec tr = getFirstTableRec(paymentBr.getSelectUtil(), "select * from epayment where epm_outtradeno = ?", lastOutTradeNo).orElse(null);
				if (tr != null) {
					transNo = tr.getFieldString("epm_transno");
					totalFee = tr.getFieldDouble("epm_totalfee");
				}
			} else
				totalFee = br.getCellDouble("vcol_actualfee");
			String logoBase64 = "";
			PropmgmtproSessionHelper sh = (PropmgmtproSessionHelper)sessionHelper;
			String path = "/images/" + sh.getCompanyLogo();
			UniLog.log1("companyLogo:%s", path);
			try (InputStream inStream = sessionHelper.getSvc().getResourceAsStream(path)) {
				byte[] data = IOUtils.toByteArray(inStream);
				logoBase64 = Base64Util.convertToString(data);
				UniLog.log1("base64:%s", logoBase64);
			} catch (Exception e) {
				UniLog.log(e);
			}
			String receiptUrl = getPaymentReceiptUrl(voucherNo);
			Pair<String, String> period = getPayitemPeriod(br);
			ZkUtil.js("android.printPropertyMgmtReceipt2('%s', '%s', '%s', '%s', '%s', '%s', %f, %d, %d, '%s', '%s', '%s', '%s')", 
					escapeJava(logoBase64), 
					escapeJava(unit), 
					escapeJava(voucherNo), 
					escapeJava(transNo), 
					escapeJava(br.getCellString("col_g")), 
					escapeJava(receiptUrl),
					totalFee, 
					transTime,
					getPrintPageCount(br, deviceId),
					escapeJava(paymentType),
					escapeJava(period.getLeft()),
					escapeJava(period.getRight()),
					escapeJava(br.getCellString("cashier_col_b")));
			addPrintReceiptDataRecord(sessionHelper, br, receiptUrl.substring(receiptUrl.length() - 8));
		} catch (Exception e) {
			UniLog.log(e);
			ZkUtil.errMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
		}
		ZkUtil.timerEvent(null, divPagePrtReceipt, 5000, () -> clickExitButton());
	}
	
	private void reprintReceipt() throws Exception {
		String lastVoucherNo = (String)sessionHelper.getSessionData("lastVoucherNo");
		String lastOutTradeNo = (String)sessionHelper.getSessionData("lastOutTradeNo");
		if (StringUtils.isBlank(lastVoucherNo)) {
			ZkUtil.showErrMsg("請先做付款操作");
			return;
		}
		BiResult br = Stream.of("propertymgmt.payment", "propertymgmt.ProjectPayment", "propertymgmt.ParkingTmpLocPayment").map(name -> {
			BiResult br1 = BiResultHelper.create(sessionHelper, name, String.format("col_b = '%s'", lastVoucherNo), -1, null);
			if (br1.next(false))
				return br1;
			else {
				br1.close();
				return null;
			}
		}).filter(Objects::nonNull).findFirst().orElse(null);
		if (br != null)
			printReceipt(br, lastOutTradeNo);
		else
			ZkUtil.errMsg(String.format("收據號碼'%s'不存在", lastVoucherNo));
	}
	
	private void selectBlockTr() throws Exception {
		blockTr = getTableRec(paymentBr.getSelectUtil(), "select distinct col_c pblock from property where col_a = ? and col_b = ?  ",
							typeTr.getFieldString("ptype"), devTr.getFieldString("lc_desc"));
	}
	private void selectFloorTr() throws Exception {
		floorTr = getTableRec(paymentBr.getSelectUtil(), "select distinct col_d pfloor from property where col_a = ? and col_b = ? and col_c = ?  ",
												typeTr.getFieldString("ptype"), devTr.getFieldString("lc_desc"), blockTr.getFieldString("pblock"));
	}
	private void selectUnitTr() throws Exception {
		unitTr = getTableRec(paymentBr.getSelectUtil(), "select distinct col_e punit from property where col_a = ? and col_b = ? and col_c = ?  and col_d = ? ",
												typeTr.getFieldString("ptype"), devTr.getFieldString("lc_desc"), blockTr.getFieldString("pblock"), floorTr.getFieldString("pfloor"));
	}
	private void selectParkTlContractTr() throws Exception {
		parktlContractTr = getTableRec(paymentBr.getSelectUtil(), "select * from contractparkingtl where cpt_unit = ? and cpt_totalamount > cpt_paidamount order by cpt_tlcode, cpt_constartmonth",
												paymentUnit);
	}
	private void selectProjectTr() throws Exception {
		projectTr = getTableRec(paymentBr.getSelectUtil(), "select * from unitprojectfee where upf_unit = ? and upf_allocAmt > upf_totpayamt order by upf_projectno",
												paymentUnit);
	}
	private void setupPaymentUnit() throws Exception {
		paymentUnit = getFirstTableRec(paymentBr.getSelectUtil(), "select key_a from property where col_b = ? and col_c = ? and col_d = ? and col_e = ?", 
											devTr.getFieldString("lc_desc"), blockTr.getFieldString("pblock"), floorTr.getFieldString("pfloor"), unitTr.getFieldString("punit"))
								.map(throwFunction(tr -> tr.getFieldString("key_a"))).orElse(null);
	}

	private boolean syncPropertyByPropertyUnit(String p_propunit) throws Exception {
		TableRec tr = getTableRec(paymentBr.getSelectUtil(), "select * from property where key_a = ? ", p_propunit);
		if (tr.getRecordCount() != 1) 
			return false;
		tr.setRecPointer(0);
		if (!devTr.getFieldString("lc_desc").equals(tr.getFieldString("col_b")))
			return false;
		boolean ok = false;
		for (int i=0;i<typeTr.getRecordCount();i++) {
			if (tr.getFieldString("col_a").equals(typeTr.getField("ptype", i))) {
				typeTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		if (!ok) return false;
		selectBlockTr();
		ok = false;
		for (int i=0;i<blockTr.getRecordCount();i++) {
			if(tr.getFieldString("col_c").equals( blockTr.getField("pblock", i)) ) {
				blockTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		if (!ok) return false;
		selectFloorTr();
		ok = false;
		for (int i=0;i<floorTr.getRecordCount();i++) {
			if(tr.getFieldString("col_d").equals( floorTr.getField("pfloor", i)) ) {
				floorTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		if (!ok) return false;
		selectUnitTr();
		ok = false;
		for (int i=0;i<unitTr.getRecordCount();i++) {
			if(tr.getFieldString("col_e").equals( unitTr.getField("punit", i)) ) {
				unitTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		return true;
	}
	
	private void clickExitButton() {
		Component comp = Selectors.find("div[id^='divPage'][visible=true] button[id^='btExit']").stream().findFirst().orElse(null);
		UniLog.log1("comp:%s", comp);
		if (comp != null)
			Events.echoEvent(Events.ON_CLICK, comp, null);
		else {
			try {
				gotoFirstPage(null);
			} catch (Exception e) {
				UniLog.log(e);
			}
		}
	}

	private Button newUnitButton(String idPrefix, int i, String label) {
		Button bt = new Button();
		bt.setId(idPrefix + i);
		bt.setSclass("btn_green4");
		bt.setLabel(label);
		bt.setAttribute("recIdx", i);
		bt.addEventListener(Events.ON_CLICK, onClickListener);
		return bt;
	}

	private boolean checkPaymentFee() {
		if (getPaymentBr().getCellDouble("vcol_actualfee") <= 0) {
			ZkUtil.showErrMsg("付款金額必須大於0");
			clickExitButton();
			return false;
		}
		return true;
	}
	
	private BiResult getPaymentBr() {
		switch (paymentType) {
		case "管理費":
		case "儲備金":
			return paymentBr;
		case "維修分攤費":
			return projectPaymentBr;
		case "暫放費":
			return packtlPaymentBr;
		}
		return null;
	}

	private static String getPaymentType(BiResult br) {
		if (br instanceof BiResultPayment)
			return br.getCellBoolean("col_n") ? "管理費" : br.getCellBoolean("col_o") ? "儲備金" : "";
		else if (br instanceof BiResultProjectPayment)
			return "維修分攤費";
		else if (br instanceof BiResultParkingTmpLocPayment)
			return "暫放費";
		return null;
	}

	private static String getPayitemUnit(BiResult br) {
		if (br instanceof BiResultPayment)
			return br.getSubLinkResult("propertymgmt.payitem").stream().findFirst().map(sli -> sli.getString("col_c")).orElse("");
		else if (br instanceof BiResultProjectPayment)
			return br.getSubLinkResult("propertymgmt.PayProjectItem").stream().findFirst().map(sli -> sli.getString("col_d")).orElse("");
		else if (br instanceof BiResultParkingTmpLocPayment)
			return br.getSubLinkResult("propertymgmt.PayParkingItem").stream().findFirst().map(sli -> sli.getString("ccol_c")).orElse("");
		return null;
	}
	
	private static Pair<String, String> getPayitemPeriod(BiResult br) throws Exception {
		if (br instanceof BiResultPayment)
			return Pair.of(br.getCellString("col_l"), br.getCellString("col_m"));
		else if (br instanceof BiResultProjectPayment) {
			Pair<Integer, Integer> p = br.getSubLinkResult("propertymgmt.PayProjectItem").stream().findFirst().map(sli -> Pair.of(sli.getInt("col_e"), sli.getInt("col_f"))).orElse(null);
			if (p.getLeft() > 0 && p.getRight() > 0)
				return Pair.of("第" + NumberUtil.toTChinese(p.getLeft()) + "期", "第" + NumberUtil.toTChinese(p.getLeft() + p.getRight() - 1) + "期");
		} else if (br instanceof BiResultParkingTmpLocPayment) {
			Pair<String, Integer> p = br.getSubLinkResult("propertymgmt.PayParkingItem").stream().findFirst().map(sli -> Pair.of(sli.getString("col_e"), sli.getInt("col_f"))).orElse(null);
			if (StringUtils.isNotBlank(p.getLeft()) && p.getRight() > 0)
				return Pair.of(p.getLeft(), nextMonth(p.getLeft(), p.getRight() - 1));
		}
		return null;
	}

	private void connectBarcodeScanner() {
		if ("WEBCAM".equals(barcodeScanner))
			ZkUtil.js("startWebcamScanner()");
		else
			ZkUtil.js("android.connectYokoBarcodeScanner('postQrCodeData')");
	}

	private void connectNfcScanner() {
		ZkUtil.js("android.connectNfcScanner('postNfcData')");
	}

	private void disConnectScanner() {
		if ("WEBCAM".equals(barcodeScanner))
			ZkUtil.js("stopWebcamScanner()");
		else
			ZkUtil.js("android.connectYokoBarcodeScanner('')");
		ZkUtil.js("android.connectNfcScanner('')");
	}
	
	private void restartUiTimer() {
		Stream.of(uiTimer1, uiTimer2).forEach(t -> {
			t.stop();
			t.start();
		});
	}

	private Runnable restartUiTimerRunnable = () -> {
		restartUiTimer();
	};
	
	public static String getPaymentReceiptUrl(String voucherNo) {
		HttpServletRequest request = ZkSessionHelper.getCurrentHttpServletRequest();
		String receiptUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() 
							+ "/rest/propmgmt/pr/" 
							+ CryptoUtil.encryptToBase64(PropmgmtRS.PAYMENT_RECEIPT_KEYS, voucherNo.getBytes(StandardCharsets.UTF_8), false, true);
		UniLog.log1("receiptUrl:%s", receiptUrl);
		return receiptUrl;
	}
	
	public static int getPrintPageCount(BiResult br, String deviceId) throws Exception {
		return getFirstTableRec(br.getSelectUtil(), "select ldv_printpagecnt from devicelogin where ldv_login = ?", deviceId)
						.map(throwFunction(tr -> tr.getFieldInt("ldv_printpagecnt"))).orElse(1);
	}

	@Override
	protected boolean validateURL(String p_requestURL) {
		return true;
	}
	
	private class MyEpayment extends Epayment {

		public MyEpayment() throws Exception {
			super(sessionHelper, getPaymentBr(), rootComp, null, deviceId, getPrintPageCount(getPaymentBr(), deviceId), restartUiTimerRunnable, null);
		}

		@Override
		protected void printReceipt(Map<String, Object> m) {
			ZkFormPadPayment.this.printReceipt(getPaymentBr(), (String)m.get("outTradeNo"));
		}
	}
}
