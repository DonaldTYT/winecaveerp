package com.uniinformation.zkf.propertymgmt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Timer;

import com.google.gson.reflect.TypeToken;
import com.kyoko.crypto.SHA256withRSA;
import com.kyoko.utils.UrlUtils;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultPayment;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.MonthUtil;
import com.uniinformation.utils.QRCodeUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.BiUtil.CheckedConsumer4;
import com.uniinformation.utils.BiUtil.CheckedConsumer5;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiUiExecutor;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkFormEpayment extends ZkCellActionForm {
	int GROUPB4_THESHOLD = 80;
	int MAXSELECT_B4 = 20;
	int MAXSELECT_B6 = 36;
	int USER_INPUT_TIMEOUT = 20000;
	@Wire
	Div btGroupA;
	@Wire
	Div btGroupB4;
	@Wire
	Div btGroupB6;
	@Wire
	Div btGroupDpad;
	@Wire
	Div barcodeS0;
	@Wire
	Div barcodeS1;
	@Wire
	Div divTempbuf;
	@Wire
	Label ldv_comment;
	@Wire
	Image barcodeImg;
	@Wire
	Label lbPaymentMsg;
	@Wire
	Div unitList;
    @Wire
    Listitem li_ask_for_more;
    @Wire
    Listbox lb_unitList;
    @Wire
    Label lb_paidto;
    @Wire
    Label lb_paidtotal;
    @Wire
    Button ldv_exit;
    @Wire
    Label lb_epayamount;
    @Wire
    Div div_paymonth;
    
    Template template_payUnit;
	
	TableRec devTr;
	TableRec typeTr;
	TableRec blockTr;
	TableRec floorTr;
	TableRec unitTr;
	Timer uiTimer;
	static boolean DISABLE_WAITMORESLIP = true;
	static enum RUNSTATE {STATE_CHECKTYPE,STATE_WAITTYPE,STATE_CHECKBLOCK,STATE_WAITBLOCK,STATE_CHECKFLOOR,STATE_WAITFLOOR,STATE_CHECKUNIT,STATE_WAITUNIT,STATE_CHECKPAYER,STATE_WAITPASSWORD,
			STATE_CHECKPAYMENT,STATE_WAITPAYMENT,STATE_CHECKSCANSLIP,STATE_WAITSCANSLIP,STATE_WAITMORESLIP}
	static int maxA = 4; 
	static int maxB = 12; 
	static enum BANK {
		OFF("關閉"), TAIFUNG("大豐銀行"), BOC("中國銀行");
		final String name;
		BANK(String name) {
			this.name = name;
		}
		static BANK fromIndex(int index) throws Exception {
			BANK[] bs = values();
			if (index >= 0 && index < bs.length)
				return bs[index];
			else
				throw new Exception("Invalid BANK index");
		}
	}
	BiResultPayment paymentBr;
	String password;
	String keybuf="";
//	double mgtFee;
//	double resFee;
//	String payToMonth;
	String barcodeScanner;
	
	RUNSTATE currentState;

	private Timer payMntTimer;
	
	private static class C2BItem {
		BANK bank;
		CompletableFuture<Void> urlFuture;
		String outTradeNo, url, urlErrMsg;
		CompletableFuture<Map<String, Object>> statusFuture;
		Map<String, Object> paymentMap;
		boolean ok;
	}
	private C2BItem curC2BItem;
	private ExecutorService c2bThreadPool = Executors.newSingleThreadExecutor();
	private ZkBiMsgbox c2bCancelDialog;
	
	void displayAndWaitB_org(TableRec tr,String labelField,int pageIdx) throws Exception {
		btGroupA.setVisible(false);
		btGroupB4.setVisible(true);
		btGroupB6.setVisible(false);
		btGroupDpad.setVisible(false);
		barcodeS0.setVisible(false);
		barcodeS1.setVisible(false);
		unitList.setVisible(false);
		divTempbuf.setVisible(true);
		int i;
		int idx = tr.getRecPointer();
		for(i=0;idx<tr.getRecordCount();i++,idx++) {
			if(i >= maxB) break;
			if(idx >= tr.getRecordCount()) break;
			tr.setRecPointer(idx);
			Button bt = (Button) btGroupB4.getFellow("btB"+i, true);
			Div dv = (Div) btGroupB4.getFellow("spB"+i, true);
			bt.setLabel(tr.getFieldString(labelField));
			bt.setVisible(true);
			dv.setVisible(true);
		}
		for(;i<maxB;i++) {
			Button bt = (Button) btGroupB4.getFellow("btB"+i, true);
			Div dv = (Div) btGroupB4.getFellow("spB"+i, true);
			bt.setVisible(false);
			dv.setVisible(false);
		}
		tr.setRecPointer(0);
	}

	void displayAndWaitB(TableRec tr,String labelField,int pageIdx) throws Exception {
		int maxSelect;
		Div btGroupB;
		btGroupA.setVisible(false);
		String btPrefix;
		String spPrefix;
		if(tr.getRecordCount() <= GROUPB4_THESHOLD ) {
			btGroupB4.setVisible(true);
			btGroupB6.setVisible(false);
			btGroupB = btGroupB4;
			btPrefix="btB4_";
			spPrefix="sbB4_";
			maxSelect = MAXSELECT_B4;
		} else {
			btGroupB4.setVisible(false);
			btGroupB6.setVisible(true);
			maxSelect = MAXSELECT_B6;
			btGroupB = btGroupB6;
			btPrefix="btB6_";
			spPrefix="sbB6_";
		}
		btGroupDpad.setVisible(false);
		barcodeS0.setVisible(false);
		barcodeS1.setVisible(false);
		unitList.setVisible(false);
		divTempbuf.setVisible(true);
		ldv_exit.setVisible(true);
		if(pageIdx * maxSelect >= tr.getRecordCount())  {
			throw new Exception("displayAndWaitB Page Index > max unit count");
		}
		int i;
		int idx = pageIdx * maxSelect;
		for(i=0;idx<tr.getRecordCount();i++,idx++) {
			if(i >= maxSelect) break;
			if(idx >= tr.getRecordCount()) break;
			tr.setRecPointer(idx);
			Button bt = (Button) btGroupB.getFellow(btPrefix+i, true);
			Div dv = (Div) btGroupB.getFellow(spPrefix+i, true);
			bt.setLabel(tr.getFieldString(labelField));
			bt.setVisible(true);
			dv.setVisible(true);
		}
		for(;i<maxSelect;i++) {
			Button bt = (Button) btGroupB.getFellow(btPrefix+i, true);
			Div dv = (Div) btGroupB.getFellow(spPrefix+i, true);
			bt.setVisible(false);
			dv.setVisible(false);
		}
		int n = tr.getRecordCount() / maxSelect + ((tr.getRecordCount() % maxSelect) > 0 ? 1 : 0);
		for(int j=0;j<100;j++) {
			Button bt = (Button) btGroupB.getFellowIfAny("pg_"+btPrefix+j, true);
			if(bt == null) break;
			Div dv = (Div) btGroupB.getFellow("pg_"+spPrefix+j, true);
			if(j < pageIdx || (j > pageIdx && j < n)) {
				String ss;
				tr.setRecPointer(j * maxSelect);
				ss = tr.getFieldString(labelField);
				ss += "-";
				int trMax = j * maxSelect + maxSelect-1;
				if(trMax >= tr.getRecordCount()) trMax = tr.getRecordCount() - 1;
//				tr.setRecPointer(j * maxSelect + maxSelect-1);
				tr.setRecPointer(trMax);
				ss += tr.getFieldString(labelField);
				bt.setLabel(ss);
				bt.setVisible(true);
				dv.setVisible(true);
			} else {
				bt.setVisible(false);
				dv.setVisible(false);
			}
		}
		tr.setRecPointer(pageIdx * maxSelect);
	}
	void displayAndWaitNumpad() throws Exception {
		keybuf="";
		ldv_comment.setValue("Please Enter Password");
		btGroupA.setVisible(false);
		btGroupB4.setVisible(false);
		btGroupB6.setVisible(false);
		btGroupDpad.setVisible(true);
		barcodeS0.setVisible(false);
		barcodeS1.setVisible(false);
		unitList.setVisible(false);
		divTempbuf.setVisible(true);
		ldv_exit.setVisible(true);
	}

	void displayAndWaitType() throws Exception {
		ldv_comment.setValue("Please Select");
		btGroupA.setVisible(true);
		btGroupB4.setVisible(false);
		btGroupB6.setVisible(false);
		btGroupDpad.setVisible(false);
		barcodeS0.setVisible(false);
		barcodeS1.setVisible(false);
		lbPaymentMsg.setVisible(false);
		unitList.setVisible(false);
		divTempbuf.setVisible(false);
		ldv_exit.setVisible(false);
		int i;
		for(i=0;i<typeTr.getRecordCount();i++) {
			typeTr.setRecPointer(i);
			Button bt = (Button) btGroupA.getFellow("btA"+i, true);
			if(typeTr.getFieldString("ptype").equals("住宅")) {
				bt.setClass("btn_four btn_Residental");
			} else if(typeTr.getFieldString("ptype").equals("商鋪")) {
				bt.setClass("btn_four btn_shop");
			} else if(typeTr.getFieldString("ptype").equals("車位")) {
				bt.setClass("btn_four btn_carpark");
			} else bt.setLabel(typeTr.getFieldString("ptype"));
			bt.setVisible(true);
		}
		for(;i<maxA;i++) {
			Button bt = (Button) btGroupA.getFellow("btA"+i, true);
			bt.setVisible(false);
		}
	}

	void displayAndWaitBarcode0() throws Exception {
		ldv_comment.setValue("");
		btGroupA.setVisible(false);
		btGroupB4.setVisible(false);
		btGroupB6.setVisible(false);
		btGroupDpad.setVisible(false);
		barcodeS0.setVisible(true);
		barcodeS1.setVisible(false);
		unitList.setVisible(false);
		divTempbuf.setVisible(false);
		ldv_exit.setVisible(true);
	}
	void displayAndWaitBarcode1() throws Exception {
		btGroupA.setVisible(false);
		btGroupB4.setVisible(false);
		btGroupB6.setVisible(false);
		btGroupDpad.setVisible(false);
		barcodeS0.setVisible(false);
		barcodeS1.setVisible(true);
		unitList.setVisible(false);
		divTempbuf.setVisible(false);
		ldv_exit.setVisible(true);
	}
	boolean displayUnitList() throws Exception {
		ldv_comment.setValue("");
		btGroupA.setVisible(false);
		btGroupB4.setVisible(false);
		btGroupB6.setVisible(false);
		btGroupDpad.setVisible(false);
		barcodeS0.setVisible(false);
		barcodeS1.setVisible(false);
		unitList.setVisible(true);
		divTempbuf.setVisible(false);
		ldv_exit.setVisible(true);
		li_ask_for_more.setParent(null);
		lb_unitList.getItems().clear();
    	DecimalFormat df = new DecimalFormat("$#,##0.00");
		BiResult sru = paymentBr.getSubLink("propertymgmt.payunit");
		BiResult sri = paymentBr.getSubLink("propertymgmt.payitem");
		Vector<BiCellCollection> slu = sru.getRowCollectionList();
		Vector<BiCellCollection> sli = sri.getRowCollectionList();
		//UniLog.log1("paymentBr:%s", ZkUtil.getBiResultRecordJson(paymentBr));
		//UniLog.log1("payunit:%s", ZkUtil.getBiResultRecordJson(sru));
		//UniLog.log1("payitem:%s", ZkUtil.getBiResultRecordJson(sri));
		for(int i=0;i<slu.size();i++) {
			BiCellCollection bc = slu.get(i);
			Listitem li = new Listitem();
			Listcell lc;
			lc = new Listcell();
			Component carr[];
			carr = template_payUnit.create(lc, null, null, null);
			rootComp = carr[0];
			Label lb = (Label) rootComp.getFellowIfAny("unit_name");
			Label lbl;
			if(lb != null) {
				lb.setId(lb.getId()+"_"+i);
				lb.setValue(
					CellCollection.stringCombine(
						//bc.getCellString("pu_block"),
						//bc.getCellString("pu_floor"),
						bc.getCellString("pu_unit")
							)
						);
			}
			
			if (DISABLE_WAITMORESLIP) {
				//double mfee = bc.getCellDouble("pu_mgtfee");
				//double rfee = bc.getCellDouble("pu_resfee");
				String mfrom = bc.getCellString("pu_mgtstart");
				String rfrom = bc.getCellString("pu_resstart");
				//String monthEnd = paymentBr.getCellString("col_m");
				//Button btPayMnt1 = (Button) div_paymonth.getFellowIfAny("btPayMnt1");
				//String mstart = StringUtils.isNotBlank(mfrom) ? MonthUtil.nextNmonth(mfrom, 1) : btPayMnt1 != null ? (String)btPayMnt1.getAttribute("month") : "";
				//String rstart = StringUtils.isNotBlank(rfrom) ? MonthUtil.nextNmonth(rfrom, 1) : btPayMnt1 != null ? (String)btPayMnt1.getAttribute("month") : "";
				//double mfeePerMonth = bc.getCellDouble("pu_mgtfeepermon");
				//double rfeePerMonth = bc.getCellDouble("pu_resfeepermon");
				double mfeePerMonth = 0.0, rfeePerMonth = 0.0;
				List<Map<String, Object>> jlist = GsonUtil.convertToObject(bc.getCellString("pu_jsondet"), new TypeToken<List<Map<String, Object>>>(){}.getType());
				for (Map<String, Object> m : jlist) {
					String constart = (String)m.get("constart");
					String conend = MonthUtil.nextNmonth(constart, (int)(double)(m.get("noofmonth")));
					if (StringUtils.isNotBlank(mfrom) && mfrom.compareTo(constart) >= 0 && mfrom.compareTo(conend) < 0)
						mfeePerMonth = (double)m.get("mgtfeepermon");
					if (StringUtils.isNotBlank(rfrom) && rfrom.compareTo(constart) >= 0 && rfrom.compareTo(conend) < 0)
						rfeePerMonth = (double)m.get("resfeepermon");
				}
				//UniLog.log1("mstart:%s, rstart:%s, mfrom:%s, rfrom:%s, mfeePerMonth:%f, rfeePerMonth:%f", mstart, rstart, mfrom, rfrom, mfeePerMonth, rfeePerMonth);

				final int ii = i;
				CheckedConsumer4<String, String, Double, String> action = (unitlabel, feefrom, fee, value) -> {
					Label lb1 = (Label) rootComp.getFellowIfAny(unitlabel);
					Label lb1l = (Label) rootComp.getFellowIfAny(unitlabel + "label");
					if (lb1 != null) {
						lb1.setId(lb1.getId()+"_"+ii);
						if (StringUtils.isNotBlank(feefrom) && fee > 0.0) {
							lb1.setValue(value);
							lb1.setVisible(true);
						} else
							lb1.setVisible(false);
					} 
					if (lb1l != null) {
						lb1l.setId(lb1l.getId()+"_"+ii);
						lb1l.setVisible(lb1.isVisible());
					}
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
				Div unit_mgtcurdiv = (Div)rootComp.getFellow("unit_mgtcurdiv", true);
				Div unit_rescurdiv = (Div)rootComp.getFellow("unit_rescurdiv", true);
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
					int j = list.indexOf(m);
					lbcurl.setId(lcurl + j + "_" + ii);
					lbcur.setId(lcur + j + "_" + ii);
					lbcurfee.setId(lcurfee + j + "_" + ii);
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
				unit_mgtcurdiv.getParent().removeChild(unit_mgtcurdiv);
				unit_rescurdiv.getParent().removeChild(unit_rescurdiv);
			} else {
				double mfee = bc.getCellDouble("pu_mgtfee");
				lb = (Label) rootComp.getFellowIfAny("unit_mgtfee");
				lbl = (Label) rootComp.getFellowIfAny("unit_mgtfeelabel");
				if(lb != null && lbl != null) {
					lb.setId(lb.getId()+"_"+i);
					lbl.setId(lbl.getId()+"_"+i);
					if(mfee > 0.0) {
						lb.setVisible(true);
						lbl.setVisible(true);
						lb.setValue(df.format(mfee));
					} else {
						lb.setVisible(false);
						lbl.setVisible(false);
					}
				}
				lb = (Label) rootComp.getFellowIfAny("unit_mgtfrom");
				lbl = (Label) rootComp.getFellowIfAny("unit_mgtfromlabel");
				if(lb != null && lbl != null) {
					lb.setId(lb.getId()+"_"+i);
					lbl.setId(lbl.getId()+"_"+i);
					if(mfee > 0.0) {
						lb.setVisible(true);
						lbl.setVisible(true);
						lb.setValue( bc.getCellString("pu_mgtstart"));
					} else {
						lb.setVisible(false);
						lbl.setVisible(false);
					}
				}
				double rfee = bc.getCellDouble("pu_resfee");
				lb = (Label) rootComp.getFellowIfAny("unit_resfee");
				lbl = (Label) rootComp.getFellowIfAny("unit_resfeelabel");
				if(lb != null && lbl != null) {
					lb.setId(lb.getId()+"_"+i);
					lbl.setId(lbl.getId()+"_"+i);
					if(rfee > 0.0) {
						lb.setVisible(true);
						lbl.setVisible(true);
						lb.setValue(df.format(rfee));
					} else {
						lb.setVisible(false);
						lbl.setVisible(false);
					}
				} 
				lb = (Label) rootComp.getFellowIfAny("unit_resfrom");
				lbl = (Label) rootComp.getFellowIfAny("unit_resfromlabel");
				if(lb != null && lbl != null) {
					lb.setId(lb.getId()+"_"+i);
					lbl.setId(lbl.getId()+"_"+i);
					if(rfee > 0.0) {
						lb.setVisible(true);
						lbl.setVisible(true);
						lb.setValue( bc.getCellString("pu_resstart"));
					} else {
						lb.setVisible(false);
						lbl.setVisible(false);
					}
				}
				double tfee = mfee + rfee;
				lb = (Label) rootComp.getFellowIfAny("unit_amount");
				if(lb != null) {
					lb.setId(lb.getId()+"_"+i);
					if(tfee > 0.0) {
						lb.setVisible(true);
						lb.setValue( "金額:" + df.format(tfee));
					} else {
						lb.setVisible(false);
					}
				}
			}

			lc.setParent(li);
			lb_unitList.appendChild(li);
		}
		lb_unitList.appendChild(li_ask_for_more);
		if(lb_paidto != null) {
			String ss = paymentBr.getCellString("col_m");
			lb_paidto.setValue(ss);
		}
		if(lb_paidtotal != null) {
			double tamt = paymentBr.getCellDouble("vcol_actualfee");
			lb_paidtotal.setValue(df.format(tamt));
		}
		if(slu.size() >= 3) {
			li_ask_for_more.setVisible(false);
			return(false);
		} else {
			//li_ask_for_more.setVisible(true);
			li_ask_for_more.setVisible(false);
			return(true);
		}
	}

	void setupPayMonthButtonAttribute() throws Exception {
		String monthStart = paymentBr.getCellString("col_m");
		Arrays.stream(div_paymonth.getChildren().toArray(new Button[0]))
				.filter(btn -> !StringUtils.equalsAny(btn.getId(), "btPrevPayMnt", "btNextPayMnt"))
				.forEach(btn -> div_paymonth.removeChild(btn));
		Button btPrevPayMnt = (Button) div_paymonth.getFellow("btPrevPayMnt", true);
		Button btNextPayMnt = (Button) div_paymonth.getFellow("btNextPayMnt", true);
		div_paymonth.setClientDataAttribute("first_button_index", "0");
		if (StringUtils.isNotBlank(monthStart)) {
			for (int i : new int[] { 1, 2, 3, 4, 5, 6, 12 }) {
				Button bt = new Button();
				bt.setId("btPayMnt" + i);
				bt.setLabel(i + "個月");
				bt.setSclass("btn_green4");
				bt.setAttribute("month", MonthUtil.nextNmonth(monthStart, i - 1));
				div_paymonth.insertBefore(bt, btPrevPayMnt);
				ZkUtil.setEventListener(bt, Events.ON_CLICK, onClickListener);
			}
		}
		payMntTimer = ZkUtil.delayJs(payMntTimer, rootComp, null, 100, "payMonthButtons.show()");
		ZkUtil.setEventListener(btPrevPayMnt, Events.ON_CLICK, event -> {
			ZkUtil.js("payMonthButtons.showPrev()");
		});
		ZkUtil.setEventListener(btNextPayMnt, Events.ON_CLICK, event -> {
			ZkUtil.js("payMonthButtons.showNext()");
		});
		div_paymonth.invalidate();
	}
	
	void selectBlockTr() throws Exception {
				blockTr = paymentBr.getSelectUtil().getQueryResult("select distinct col_c pblock from property where col_a = ? and col_b = ?  ",
								new Wherecl().appendArgument(typeTr.getFieldString("ptype"))
												.appendArgument(devTr.getFieldString("lc_desc")
										)
								);
		
	}
	void selectFloorTr() throws Exception {
				floorTr = paymentBr.getSelectUtil().getQueryResult("select distinct col_d pfloor from property where col_a = ? and col_b = ? and col_c = ?  ",
								new Wherecl().appendArgument(typeTr.getFieldString("ptype"))
												.appendArgument(devTr.getFieldString("lc_desc"))
												.appendArgument(blockTr.getFieldString("pblock"))
								);
	}
	void selectUnitTr() throws Exception {
				unitTr = paymentBr.getSelectUtil().getQueryResult("select distinct col_e punit from property where col_a = ? and col_b = ? and col_c = ?  and col_d = ? ",
								new Wherecl().appendArgument(typeTr.getFieldString("ptype"))
												.appendArgument(devTr.getFieldString("lc_desc"))
												.appendArgument(blockTr.getFieldString("pblock"))
												.appendArgument(floorTr.getFieldString("pfloor"))
								);
	}
	boolean syncPropertyByPropertyUnit(String p_propunit) throws Exception {
		TableRec tr = paymentBr.getSelectUtil().getQueryResult("select * from property where key_a = ? ",new Wherecl().appendArgument(p_propunit));
		if(tr.getRecordCount() != 1) return(false);
		tr.setRecPointer(0) ;
		if(!devTr.getFieldString("lc_desc").equals(tr.getFieldString("col_b"))) {
			return(false);
		}
		boolean ok = false;
		for(int i=0;i<typeTr.getRecordCount();i++) {
			if(tr.getFieldString("col_a").equals( typeTr.getField("ptype", i)) ) {
				typeTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		if(! ok ) return(false);
		selectBlockTr();
		ok = false;
		for(int i=0;i<blockTr.getRecordCount();i++) {
			if(tr.getFieldString("col_c").equals( blockTr.getField("pblock", i)) ) {
				blockTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		if(! ok ) return(false);
		selectFloorTr();
		ok = false;
		for(int i=0;i<floorTr.getRecordCount();i++) {
			if(tr.getFieldString("col_d").equals( floorTr.getField("pfloor", i)) ) {
				floorTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		if(! ok ) return(false);
		selectUnitTr();
		ok = false;
		for(int i=0;i<unitTr.getRecordCount();i++) {
			if(tr.getFieldString("col_e").equals( unitTr.getField("punit", i)) ) {
				unitTr.setRecPointer(i);
				ok = true;
				break;
			}
		}
		return(true);
	}
	
	void processState(Event ev) throws Exception {
		for(;;) {
		switch(currentState) {
		case STATE_CHECKTYPE:
				if(typeTr.getRecordCount() > 0) {
					displayAndWaitType();
					formCollection.getCell("ldv_tmpbuf").set("");
					currentState = RUNSTATE.STATE_WAITTYPE;
					return;
				} else {
					typeTr.setRecPointer(0);
					currentState = RUNSTATE.STATE_CHECKBLOCK;
				}
				break;
		case STATE_WAITTYPE:
				if(ev.getTarget().getId().startsWith("btAS")) {
					currentState = RUNSTATE.STATE_CHECKSCANSLIP;
				} else if(ev.getTarget().getId().startsWith("btA")) {
					int idx = Integer.parseInt( ev.getTarget().getId().substring(3));
					typeTr.setRecPointer(idx);
					currentState = RUNSTATE.STATE_CHECKBLOCK;
				}
				break;
		case STATE_CHECKSCANSLIP:
//					uiTimer.start();
//					ZkUtil.setEventListenerForCallOne(barcodeS1, "onPostQrCodeData", data -> {
//						ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", data, onClickListener);
//					});
					if("WEBCAM".equals(barcodeScanner)) {
//						ZkUtil.js("startWebcamScanner('%s','%s')", "barcodeS1","onPostQrCodeData1");
						ZkUtil.js("startWebcamScanner()");
					} else {
						ZkUtil.js("android.connectBarcodeScanner('postQrCodeData')");
					}
					displayAndWaitBarcode0();
					currentState = RUNSTATE.STATE_WAITSCANSLIP;
					return;
		case STATE_WAITSCANSLIP:
		{
				String butId = ev.getTarget().getId();
				/*
				if(butId.equals("uiTimer")) {
					UniLog.log("Wait for barcode timeout");
					currentState = RUNSTATE.STATE_CHECKTYPE;
				}
				*/
				if (butId.equals("barcodeS1") && ev.getName().equals("onPostQrCodeData1")) {
					UniLog.log1("STATE_WAITSCANSLIP qrCodeData received %s", ev.getData());
					Map<String,String> pmap = UrlUtils.getQueryParams( (String)ev.getData());
					String punit = null;
					if(pmap != null) punit = pmap.get("punit");
					UniLog.log1("punit:%s", punit);
					if(punit != null) {
						boolean ok = syncPropertyByPropertyUnit(punit);
						if(ok) {
							/*
							if("WEBCAM".equals(barcodeScanner)) {
								ZkUtil.js("stopWebcamScanner()");
							}
							*/
//							ZkUtil.setEventListenerForCallOne(barcodeS1, "onPostQrCodeData", data -> {
//								ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", data, onClickListener);
//							});
							currentState = RUNSTATE.STATE_CHECKPAYER;
							break;
						}
					}
//					ZkUtil.setEventListenerForCallOne(barcodeS1, "onPostQrCodeData", data -> {
//							ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", data, onClickListener);
//					});
//							(String)ev.getData());
				}
				return;
				/*
				if(butId.equals("btExitBarcodeS0")) {
					UniLog.log("Barcode Key S0 Pressed");
					currentState = RUNSTATE.STATE_CHECKTYPE;
				}
				*/
		}
		case STATE_CHECKBLOCK:
//				blockTr = paymentBr.getSelectUtil().getQueryResult("select distinct col_c pblock from property where col_a = ? and col_b = ?  order by 1",
				selectBlockTr();
				/*
				blockTr = paymentBr.getSelectUtil().getQueryResult("select distinct col_c pblock from property where col_a = ? and col_b = ?  ",
								new Wherecl().appendArgument(typeTr.getFieldString("ptype"))
												.appendArgument(devTr.getFieldString("lc_desc")
										)
								);
								*/
				if(blockTr.getRecordCount() > 1) {
					blockTr.setRecPointer(0);
					ldv_comment.setValue("Please Select Block");
					displayAndWaitB(blockTr,"pblock",0);
					formCollection.getCell("ldv_tmpbuf").set(
//							typeTr.getFieldString("ptype")
							""
							);
					currentState = RUNSTATE.STATE_WAITBLOCK;
					return;
				} else {
					blockTr.setRecPointer(0);
					currentState = RUNSTATE.STATE_CHECKFLOOR;
				}
				break;
		case STATE_WAITBLOCK:
				if(ev.getTarget().getId().startsWith("pg_")) {
					int idx = Integer.parseInt( ev.getTarget().getId().substring(8));
					displayAndWaitB(blockTr,"pblock",idx);
					return;
				} else
				if(ev.getTarget().getId().startsWith("btB")) {
					int idx = Integer.parseInt( ev.getTarget().getId().substring(5));
					blockTr.setRecPointer(blockTr.getRecPointer()+idx);
					currentState = RUNSTATE.STATE_CHECKFLOOR;
					break;
				}
		case STATE_CHECKFLOOR:
				selectFloorTr();
				/*
				floorTr = paymentBr.getSelectUtil().getQueryResult("select distinct col_d pfloor from property where col_a = ? and col_b = ? and col_c = ?  ",
								new Wherecl().appendArgument(typeTr.getFieldString("ptype"))
												.appendArgument(devTr.getFieldString("lc_desc"))
												.appendArgument(blockTr.getFieldString("pblock"))
								);
				*/
				if(floorTr.getRecordCount() > 1) {
					floorTr.setRecPointer(0);
					ldv_comment.setValue("Please Select Floor");
					displayAndWaitB(floorTr,"pfloor",0);
					formCollection.getCell("ldv_tmpbuf").set(
							CellCollection.stringCombine(
//							typeTr.getFieldString("ptype"),
									"",
							blockTr.getFieldString("pblock")
									)
							);
					currentState = RUNSTATE.STATE_WAITFLOOR;
					return;
				} else {
					floorTr.setRecPointer(0);
					currentState = RUNSTATE.STATE_CHECKUNIT;
				}
				break;
		case STATE_WAITFLOOR:
				if(ev.getTarget().getId().startsWith("pg_")) {
					int idx = Integer.parseInt( ev.getTarget().getId().substring(8));
					displayAndWaitB(floorTr,"pfloor",idx);
					return;
				} else
				if(ev.getTarget().getId().startsWith("btB")) {
					int idx = Integer.parseInt( ev.getTarget().getId().substring(5));
					floorTr.setRecPointer(floorTr.getRecPointer()+idx);
					currentState = RUNSTATE.STATE_CHECKUNIT;
					break;
				}
				break;
		case STATE_CHECKUNIT:
				selectUnitTr();
				unitTr = paymentBr.getSelectUtil().getQueryResult("select distinct col_e punit from property where col_a = ? and col_b = ? and col_c = ?  and col_d = ? ",
								new Wherecl().appendArgument(typeTr.getFieldString("ptype"))
												.appendArgument(devTr.getFieldString("lc_desc"))
												.appendArgument(blockTr.getFieldString("pblock"))
												.appendArgument(floorTr.getFieldString("pfloor"))
								);
				if(unitTr.getRecordCount() > 1) {
					unitTr.setRecPointer(0);
					ldv_comment.setValue("Please Select Unit");
					displayAndWaitB(unitTr,"punit",0);
					formCollection.getCell("ldv_tmpbuf").set(
							CellCollection.stringCombine(
//							typeTr.getFieldString("ptype"),
									"",
							blockTr.getFieldString("pblock"),
							floorTr.getFieldString("pfloor")
									)
							);
					currentState = RUNSTATE.STATE_WAITUNIT;
					return;
				} else {
					unitTr.setRecPointer(0);
					currentState = RUNSTATE.STATE_CHECKPAYER;
				}
				break;
		case STATE_WAITUNIT:
				if(ev.getTarget().getId().startsWith("pg_")) {
					int idx = Integer.parseInt( ev.getTarget().getId().substring(8));
					displayAndWaitB(unitTr,"punit",idx);
					return;
				} else
				if(ev.getTarget().getId().startsWith("btB")) {
					int idx = Integer.parseInt( ev.getTarget().getId().substring(5));
					unitTr.setRecPointer(unitTr.getRecPointer()+idx);
					currentState = RUNSTATE.STATE_CHECKPAYER;
					break;
				}
				break;
		case STATE_CHECKPAYER :
					{
						/*
						formCollection.getCell("ldv_tmpbuf").set(
							CellCollection.stringCombine(
//							typeTr.getFieldString("ptype"),
									"",
							blockTr.getFieldString("pblock"),
							floorTr.getFieldString("pfloor"),
							unitTr.getFieldString("punit")
									)
							);
							*/
						String butId = ev.getTarget().getId();
						if (butId.equals("btConfirmPay")) {
							UniLog.log("Pay Confirm");
							currentState = RUNSTATE.STATE_CHECKPAYMENT;
							break;
						}
						String colm = paymentBr.getCellString("col_m");
						String month = butId.startsWith("btPayMnt") ? (String)((Button)div_paymonth.getFellow(butId, true)).getAttribute("month") : null;
						UniLog.log1("colm:%s, month:%s", colm, month);
						if (StringUtils.isNotBlank(colm) && StringUtils.equals(colm, month))
							return;
						formCollection.getCell("ldv_tmpbuf").set("");
						paymentBr.clearCurrentRec();
						BiResult sr = paymentBr.getSubLink("propertymgmt.payunit");
						CellCollection col = sr.newRowCollection();
						sr.addSubRecord(col, -1 ,"");
						col.getCell("pu_block").set(blockTr.getFieldString("pblock"));
						col.getCell("pu_floor").set(floorTr.getFieldString("pfloor"));
						col.getCell("pu_flat").set(unitTr.getFieldString("punit"));
						if(col.getCellString("pu_type").equals(typeTr.getFieldString("ptype"))) {
							/*
							displayAndWaitNumpad();
							password = "888";
							currentState = RUNSTATE.STATE_WAITPASSWORD;
							*/
							paymentBr.getCell("col_n").set(true);
							paymentBr.getCell("col_o").set(true);
							if (butId.startsWith("btPayMnt"))
								paymentBr.syncPayItemFromPayUnit(null, month);
							else {
								paymentBr.syncPayItemFromPayUnit(null);
								setupPayMonthButtonAttribute();
							}
							displayUnitList();
							if (!DISABLE_WAITMORESLIP) {
								if("WEBCAM".equals(barcodeScanner)) {
									ZkUtil.js("startWebcamScanner()");
								} else {
									ZkUtil.js("android.connectBarcodeScanner('postQrCodeData')");
								}
								currentState = RUNSTATE.STATE_WAITMORESLIP;
							}
							return;
						} else {
							currentState = RUNSTATE.STATE_CHECKTYPE;
						}
					}
				break;
		case STATE_WAITMORESLIP:
			{
				if (DISABLE_WAITMORESLIP)
					return;
				String butId = ev.getTarget().getId();
//				if(butId.equals("uiTimer")) {
//					UniLog.log("Wait for barcode timeout");
//					currentState = RUNSTATE.STATE_CHECKTYPE;
//				}
				if (butId.equals("barcodeS1") && ev.getName().equals("onPostQrCodeData1")) {
					UniLog.log1("STATE_WAITMORESLIP qrCodeData received %s", ev.getData());
					Map<String,String> pmap = UrlUtils.getQueryParams( (String)ev.getData());
					String punit = null;
					if(pmap != null) punit = pmap.get("punit");
					UniLog.log1("punit:%s", punit);
					if(punit != null) {
						BiResult sr = paymentBr.getSubLink("propertymgmt.payunit");
						boolean ok = true;
						for(BiCellCollection bc : sr.getRowCollectionList()) {
							if(bc.getCellString("pu_unit").equals(punit)) {
								ok = false;
								break;
							}
						}
						if(ok) ok = syncPropertyByPropertyUnit(punit);
						if(ok) {
							CellCollection col = sr.newRowCollection();
							sr.addSubRecord(col, -1 ,"");
							col.getCell("pu_block").set(blockTr.getFieldString("pblock"));
							col.getCell("pu_floor").set(floorTr.getFieldString("pfloor"));
							col.getCell("pu_flat").set(unitTr.getFieldString("punit"));
							paymentBr.syncPayItemFromPayUnit(null);
							boolean allowMore = displayUnitList();
							if(!allowMore) {
								if("WEBCAM".equals(barcodeScanner)) {
									ZkUtil.js("stopWebcamScanner()");
								} else {
									ZkUtil.js("android.connectBarcodeScanner('')");
								}
								return;
							}
						}
					}
//							(String)ev.getData());
//					ZkUtil.setEventListenerForCallOne(barcodeS1, "onPostQrCodeData", data -> {
//						ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", data, onClickListener);
//					});
				}
				if(butId.equals("btConfirmPay")) {
					UniLog.log("Pay Confirm");
					currentState = RUNSTATE.STATE_CHECKPAYMENT;
					break;
				}
				/*
				if(butId.equals("btCancelPay")) {
					UniLog.log("Pay Cancel");
					currentState = RUNSTATE.STATE_CHECKTYPE;
					break;
				}
				*/
				return;
			}
		case STATE_WAITPASSWORD: {
				String butId = ev.getTarget().getId();
				if(butId.equals("btaEnter")) {
					if(password.equals(keybuf)) {
							UniLog.log("Login OK");
							currentState = RUNSTATE.STATE_CHECKPAYMENT;
							break;
					}
					keybuf = "";
				} else if(butId.equals("btaBack")) {
					if(keybuf.length() > 0) {
						keybuf = keybuf.substring(0,keybuf.length()-1);
					}
				} else {
					if(butId.startsWith("btN")) {
						String digit = butId.substring(3,4);
						if(keybuf.length() < 8) {
							keybuf += digit;
						}
					}
				}
				formCollection.getCell("ldv_keybuf").set("**********".substring(0, keybuf.length()));
					
				return;
				}
		case STATE_CHECKPAYMENT: {
				paymentBr.getCell("col_n").set(true);
				paymentBr.syncPayItemFromPayUnit(null);
				double payAmount =paymentBr.getCellDouble("vcol_actualfee"); 
				if(payAmount > 0) {
//					String payToMonth = paymentBr.getCellString("col_m");
					if(lb_epayamount != null) {
						DecimalFormat df = new DecimalFormat("$#,##0.00");
						lb_epayamount.setValue(df.format(payAmount));
					}
					ldv_comment.setValue("請掃描手機支付條碼\nPlease Scan Mobile App's Payment Barcode ("+BANK.fromIndex(devTr.getFieldInt("lc_epayment")).name+")");
//					ldv_comment.setValue("pay up to " + payToMonth + " amount " + mgtFee);
//					uiTimer.start();
					displayAndWaitBarcode1();
//					ZkUtil.setEventListenerForCallOne(barcodeS1, "onPostQrCodeData", data -> {
//						lbPaymentMsg.setVisible(true);
//						lbPaymentMsg.setValue("正在處理，請稍候");
//						ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", data, onClickListener);
//					});
					ZkUtil.js("android.connectBarcodeScanner('postQrCodeData')");
					currentState = RUNSTATE.STATE_WAITPAYMENT;
					return;
				} else {
					currentState = RUNSTATE.STATE_CHECKTYPE;
				}
				break;
				}
			
		case STATE_WAITPAYMENT:
		{
				String butId = ev.getTarget().getId();
				if(butId.equals("uiTimer")) {
					UniLog.log("Wait for barcode timeout");
					currentState = RUNSTATE.STATE_CHECKTYPE;
				}
				/*if(butId.equals("btExitBarcodeS1")) {
					UniLog.log("Barcode Key S1 Pressed");
					paymentBr.addCurrent();
					currentState = RUNSTATE.STATE_CHECKTYPE;;
				}*/
				if (butId.equals("barcodeS1") && ev.getName().equals("onPostQrCodeData1")) {
					UniLog.log1("STATE_WAITPAYMENT qrCodeData received %s", ev.getData());
					double actualFee = paymentBr.getCellDouble("vcol_actualfee");
					Map<String, Object> m = b2cPayment(BANK.fromIndex(devTr.getFieldInt("lc_epayment")), actualFee, (String)ev.getData());
					String errMsg = paymentFinish(m);
					if (errMsg == null)
						currentState = RUNSTATE.STATE_CHECKTYPE;
					else {
						lbPaymentMsg.setVisible(true);
						lbPaymentMsg.setValue(errMsg);
						currentState = RUNSTATE.STATE_CHECKPAYMENT;
						Events.echoEvent("onPostQrCodeData1", barcodeS1, null);
						return;
					}
				}
				/*
				if(butId.equals("btExitBarcodeS2")) {
					UniLog.log("Barcode Key S2 Pressed");
					paymentBr.clearCurrentRec();
					currentState = RUNSTATE.STATE_CHECKTYPE;
				}
				*/
		}
				break;
		}
		}
	}

	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				Component c = (Component)arg0.getTarget();
				UniLog.log1("Event name:%s, Id:%s, target:%s, currentState:%s", arg0.getName(), c.getId(), c, currentState);
				if (!(c instanceof Button) && !(c instanceof Timer) && !StringUtils.equals(arg0.getName(), "onPostQrCodeData1")) {
					UniLog.log1("Skip event");
					return;
				}
				uiTimer.stop();
				if(arg0 != null && 
						(c.getId().equals("uiTimer") ||
						c.getId().equals("ldv_exit"))
						) {
					UniLog.log("Exit key pressed or timer fired");
					paymentBr.rollbackWork();
					paymentBr.clearCurrentRec();
					currentState = RUNSTATE.STATE_CHECKTYPE;
					if("WEBCAM".equals(barcodeScanner)) {
						ZkUtil.js("stopWebcamScanner()");
					} else {
						ZkUtil.js("android.connectBarcodeScanner('')");
					}
				}
				processState(arg0);
				uiTimer.start();
			}
		};
//		customLoginUrl = "deviceLogin.html";
//		customLoginUrl = "http://www.hellovoice.com";
		super.doAfterCompose(arg0);
		
		barcodeScanner = Executions.getCurrent().getParameter("BarcodeScanner");
		
		String targetURL = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getRequestURL().toString();
		String  queryString = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getQueryString();
      	String deviceId = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getParameter("deviceid");
		if(queryString != null) targetURL += "?" + queryString;
		sessionHelper.setLogoutURL(targetURL);
//		SelectUtil su = sessionHelper.getBiSchema().getSelectUtil();
		paymentBr = (BiResultPayment) sessionHelper.newBiResult("propertymgmt.payment");
		SelectUtil su = paymentBr.getSelectUtil();
		devTr = su.getQueryResult("select * from devicelogin,location where ldv_login = ? and lc_rg = ldv_lcrg" , 
					new Wherecl().appendArgument(deviceId)
					);
		devTr.setRecPointer(0);
		{
			String ss = Erpv4Config.getString(sessionHelper,"PROPMGMT_GROUPB4_THESHOLD");
			if(!StringUtils.isBlank(ss)) GROUPB4_THESHOLD = Integer.parseInt(ss);
			ss = Erpv4Config.getString(sessionHelper,"PROPMGMT_MAXSELECT_B4");
			if(!StringUtils.isBlank(ss)) MAXSELECT_B4 = Integer.parseInt(ss);
			ss = Erpv4Config.getString(sessionHelper,"PROPMGMT_MAXSELECT_B6");
			if(!StringUtils.isBlank(ss)) MAXSELECT_B6 = Integer.parseInt(ss);
			ss = Erpv4Config.getString(sessionHelper,"PROPMGMT_USER_TIMEOUT");
			if(!StringUtils.isBlank(ss)) {
				USER_INPUT_TIMEOUT = Integer.parseInt(ss) * 1000;
			}
		}
		Erpv4Config.setDefaultLcrg(sessionHelper, devTr.getFieldInt("ldv_lcrg"));
		
		uiTimer = new Timer();
		uiTimer.stop();
		uiTimer.setId("uiTimer");
		uiTimer.setPage(arg0.getPage());
		uiTimer.addEventListener(Events.ON_TIMER,onClickListener );
		uiTimer.setDelay(USER_INPUT_TIMEOUT);
		uiTimer.setRunning(true);
		uiTimer.setRepeats(false);
		
		if(formCollection == null) {
			int cc = 1;
			cc = 0;
		}
		Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sessionHelper, Erpv4Config.getDefaultCoCode(sessionHelper));
		//formCollection.getCell("ldv_cmpname").set(Erpv4Config.getCoName(sessionHelper, Erpv4Config.getDefaultCoCode(sessionHelper)));
		formCollection.getCell("ldv_cmpname").set(coMap.get("co_coname"));
		formCollection.getCell("ldv_cmpname1").set(coMap.get("co_chnname"));
		formCollection.getCell("ldv_locname").set(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
		formCollection.getCell("ldv_title").set("自助繳費機");
		typeTr = su.getQueryResult("select distinct col_a ptype from property where col_b = ? ",
					new Wherecl().appendArgument(devTr.getFieldString("lc_desc"))
				 );

		template_payUnit = arg0.getTemplate("template_payUnit");

		currentState = RUNSTATE.STATE_CHECKTYPE;
		
		barcodeS1.addEventListener("onPostQrCodeData",
					new EventListener() {

						@Override
						public void onEvent(Event data) throws Exception {
							if(currentState == RUNSTATE.STATE_WAITPAYMENT) {
								lbPaymentMsg.setVisible(true);
								lbPaymentMsg.setValue("正在處理，請稍候");
								ZkUtil.js("android.connectBarcodeScanner('')");
							}
							ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", data.getData(), onClickListener);
						}
					}
		);
		
		div_paymonth.addEventListener(Events.ON_AFTER_SIZE, (AfterSizeEvent event) -> {
			UniLog.log1("ON_AFTER_SIZE width:%s, height:%s", event.getWidth(), event.getHeight());
			payMntTimer = ZkUtil.delayJs(payMntTimer, rootComp, null, 100, "payMonthButtons.show()");
		});
		
		EventQueues.lookup("EpaymentNotify", EventQueues.APPLICATION, true).subscribe(event -> {
			UniLog.log1("EpaymentNotify name:%s, data:%s, curC2BItem:%s", event.getName(), event.getData(), curC2BItem);
			if (StringUtils.equalsAny(event.getName(), "onTaifungNotify", "onBocpayNotify") && event.getData() instanceof Map)
				c2bPaymentGetNotify(StringUtils.equals(event.getName(), "onBocpayNotify") ? BANK.BOC : BANK.TAIFUNG, (Map<String, String>)event.getData());
		});
		
		processState(null);
	}
	
	private Map<String, Object> b2cPayment(BANK bank, double fee, String authCode) throws Exception {
		return bank == BANK.BOC ? bocpayB2cPayment(fee, authCode) : bank == BANK.TAIFUNG ? taifungB2cPayment(fee, authCode) : MapUtil.of("errMsg", "支付失敗");
	}
	
	private Map<String, Object> taifungB2cPayment(double fee, String authCode) throws Exception {
		String outTradeNo = addEPaymentRecord("taifung");
        Map<String, String> m = new HashMap<>();
        m.put("service", "pay.qrcode.micropay");
        m.put("out_trade_no", outTradeNo);
        m.put("total_fee", String.valueOf((int)(fee * 100)));
        m.put("auth_code", authCode);

        boolean b = SHA256withRSA.taifungPayment(m, sessionHelper);
        String rtnCode = m.get("rtnCode");
        String rtnMsg = m.get("rtnMsg");
        String resultCode = m.get("resultCode");
        int tryCount = 10;
        while (!b && ((StringUtils.isBlank(rtnCode) && StringUtils.isNotBlank(rtnMsg)) || StringUtils.equalsAny(rtnCode, "N1", "S1") || (StringUtils.equals(rtnCode, "00") && StringUtils.equalsAny(resultCode, "0", "1"))) && --tryCount > 0) {
        	Thread.sleep(5000);
        	UniLog.log1("payment failed (rtnCode:%s, resultCode:%s, tryCount:%d), query payment", m.get("rtnCode"), m.get("resultCode"), tryCount);
        	m = new HashMap<>();
        	m.put("service", "pay.qrcode.chnquery");
        	m.put("out_trade_no", outTradeNo);
        	b = SHA256withRSA.taifungPayment(m, sessionHelper);
        	rtnCode = (String)m.get("rtnCode");
        	rtnMsg = (String)m.get("rtnMsg");
        	resultCode = (String)m.get("resultCode");
        	if (!b && StringUtils.equals(rtnCode, "00") && StringUtils.equalsAny(resultCode, "0", "1"))
        		tryCount++;
        }
		return updateEPaymentRecord(BANK.TAIFUNG, m, b);
	}

	private Map<String, Object> bocpayB2cPayment(double fee, String authCode) throws Exception {
		String outTradeNo = addEPaymentRecord("bocpay");
        Map<String, String> m = new HashMap<>();
        m.put("service", "B2CPay");
        m.put("requestId", outTradeNo);
        m.put("amount", String.valueOf((int)(fee * 100)));
        m.put("authCode", authCode);

        boolean b = SHA256withRSA.bocpayPayment(m, sessionHelper);
        String resultCode = m.get("resultCode");
        String resultMsg = m.get("resultMessage");
        int valTime = Math.max(NumberUtils.toInt((String)m.get("valTime")), 60);
        int tryCount = 10;
        while (!b && ((StringUtils.isBlank(resultCode) && StringUtils.isNotBlank(resultMsg)) || StringUtils.equalsAny(resultCode, "Z", "A")) && --tryCount > 0 && valTime > 0) {
        	Thread.sleep(5000);
        	UniLog.log1("payment failed (rtnCode:%s, resultCode:%s, tryCount:%d), query payment", m.get("rtnCode"), m.get("resultCode"), tryCount);
        	m = new HashMap<>();
        	m.put("requestId", outTradeNo);
        	m.put("service", "OrderQuery");
        	b = SHA256withRSA.bocpayPayment(m, sessionHelper);
        	resultCode = (String)m.get("resultCode");
        	resultMsg = (String)m.get("resultMessage");
        	if (!b && StringUtils.equalsAny(resultCode, "Z", "A")) {
        		valTime -= 5;
        		tryCount++;
        	}
        }
        m.put("out_trade_no", outTradeNo);
		return updateEPaymentRecord(BANK.BOC, m, b);
	}
	
	private void c2bPayment(BANK bank, double fee) {
		C2BItem item = new C2BItem();
		item.bank = bank;
		(item.urlFuture = CompletableFuture.runAsync(() -> {
			try {
				CompletableFuture<Void> self = item.urlFuture;
				if (item != curC2BItem) {
					self.cancel(true);
					return;
				}
		        switch (item.bank) {
		        case TAIFUNG:
		        case BOC:
					String type = item.bank == BANK.TAIFUNG ? "taifungH5" : "bocpayC2b";
					String outTradeNo = null;
					String rtnCode = null;
					String rtnMsg = null;
					int tryCount = 0;
					Map<String, String> m = null;
					boolean b = false;
			        do {
			        	if (tryCount > 0) {
			        		if (sleepSec(self, 5))
			        			break;
			        	}
			        	outTradeNo = genOutTradeNo();
			        	if (StringUtils.isBlank(outTradeNo))
			        		throw new Exception("outTradeNo is blank");
			        	if (item.bank == BANK.TAIFUNG) {
			        		m = MapUtil.of(
		        				"method", "TFPAY008",
		        				"outTradeNo", outTradeNo,
		        				"orderAmt", String.valueOf((int)(fee * 100)));
			        		b = SHA256withRSA.taifungH5Payment(m, sessionHelper);
			        		rtnCode = m.get("rtnCode");
			        		rtnMsg = m.get("rtnMsg");
			        	} else {
			        		m = MapUtil.of(
			        			"service", "C2BPay",
			        			"requestId", outTradeNo,
			        			"amount", String.valueOf((int)(fee * 100)));
			        		b = SHA256withRSA.bocpayPayment(m, sessionHelper);
			        		rtnCode = m.get("resultCode");
			        		rtnMsg = m.get("resultMessage");
			        	}
		        		UniLog.log1("getUrl (rtnCode:%s, rtnMsg:%s, tryCount:%d)", rtnCode, rtnMsg, tryCount);
			        } while (!self.isCancelled() && !b && (StringUtils.isBlank(rtnCode) && StringUtils.isNotBlank(rtnMsg)) && ++tryCount <= 10);
			        if (self.isCancelled())
			        	return; 
		        	addEPaymentRecord(type, outTradeNo);
		        	m.put("out_trade_no", outTradeNo);
		        	updateEPaymentRecord(item.bank, m, b);
		        	item.outTradeNo = outTradeNo;
	        		item.url = m.get("prepayURL");
		        	item.urlErrMsg = rtnMsg;
	        	default:
					throw new Exception(String.format("下單失敗(bank:%s)", item.bank));
		        }
			} catch (Exception e) {
		        item.urlErrMsg = StringUtils.defaultIfBlank(e.getMessage(), e.toString());
			}
		}, c2bThreadPool)).thenRunAsync(() -> {
			if (item == curC2BItem)
				c2bPaymentShowQrcode();
		}, new ZkBiUiExecutor(rootComp));
		curC2BItem = item;
	}

	private void c2bPaymentGetStatus() {
		C2BItem item = curC2BItem;
        AtomicReference<String> statusMessageRef = new AtomicReference<>();
		(item.statusFuture = CompletableFuture.supplyAsync(() -> {
			try {
				CompletableFuture<Map<String, Object>> self = item.statusFuture;
				if (item != curC2BItem) {
					self.cancel(true);
					return null;
				}
	            switch (item.bank) {
	            case TAIFUNG:
	            case BOC:
	            	Map<String, String> m = null;
	            	int tryCount = 0;
		        	String rtnCode = null;
	            	String rtnMsg = null;
	            	String resultCode = null;
	            	String resultMessage = null;
		        	boolean b = false, canBreak = false;
	            	do {
			        	if (sleepSec(self, 5))
			        		break;
		         	  	if (StringUtils.isBlank(item.outTradeNo))
		           			throw new Exception("outTradeNo is blank");
	              	   	if (item.bank == BANK.TAIFUNG) {
	              	   		m = MapUtil.of(
	              	   			"method", "TFPAY002",
	              	   			"outTradeNo", item.outTradeNo);
	              	   		b = SHA256withRSA.taifungH5Payment(m, sessionHelper);
	              	   	} else {
	              	   		m = MapUtil.of(
              	   				"service", "OrderQuery",
              	   				"requestId", item.outTradeNo);
	              	   		b = SHA256withRSA.bocpayPayment(m, sessionHelper);
	              	   	}
              	   		rtnCode = m.get("rtnCode");
              	   		rtnMsg = m.get("rtnMsg");
              	   		resultCode = m.get("resultCode");
              	   		resultMessage = m.get("resultMessage");
              	   		statusMessageRef.set(m.get("statusMessage"));
	   		        	UniLog.log1("getstatus (rtnCode:%s, rtnMsg:%s, resultCode:%s, resultMessage:%s, tryCount:%d)", rtnCode, rtnMsg, resultCode, resultMessage, tryCount);
	   		        	canBreak = item.bank == BANK.TAIFUNG ? StringUtils.equals(rtnCode, "0") && StringUtils.equalsAny(resultCode, "0", "1") : StringUtils.equalsAny(resultCode, "S", "F");
	              	   	//Executions.schedule(rootComp.getDesktop(), c2bPaymentStatusListener, new Event(null, null, statusMessage));
	              	   	tryCount++;
   			        } while (!self.isCancelled() && !canBreak);
	            	if (self.isCancelled())
		        	   	return null; 
	               	m.put("out_trade_no", item.outTradeNo);
   			        return updateEPaymentRecord2(item.bank, m, b);
            	default:
					throw new Exception(String.format("付款失敗(bank:%s)", item.bank));
	            }
			} catch (Exception e) {
		        return MapUtil.of("errMsg", StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
			}
		}, c2bThreadPool)).thenAcceptAsync(m -> {
			item.paymentMap = m;
			if (item == curC2BItem)
				c2bPaymentFinish();
		}, new ZkBiUiExecutor(rootComp, () -> {
			if (curC2BItem == null)
				return;
			String msg = statusMessageRef.get();
			//todo show status message
		}));
	}
	
	private void c2bPaymentGetNotify(BANK bank, Map<String, String> m) {
		if (curC2BItem != null && StringUtils.equals(curC2BItem.outTradeNo, m.get("out_trade_no"))) {
			UniLog.log1("outTradeNo:%s", curC2BItem.outTradeNo);
			if (curC2BItem.urlFuture != null)
				curC2BItem.urlFuture.cancel(true);
			if (curC2BItem.statusFuture != null)
				curC2BItem.statusFuture.cancel(true);
			CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
				try {
					return updateEPaymentRecord2(bank, m, StringUtils.equals(m.get("ok"), "true"));
				} catch (Exception e) {
					return MapUtil.of("errMsg", StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
				}
			}, c2bThreadPool);
			curC2BItem.paymentMap = future.join();
			c2bPaymentFinish();
		}
	}
	
	private void c2bPaymentShowQrcode() {
		if (curC2BItem == null)
			return;
		try {
			if (StringUtils.isNotBlank(curC2BItem.url)) {
				byte[] data = QRCodeUtil.createQRCode(curC2BItem.url, 500, 500, "png");
				String imgStr = "data:image/png;base64," + Base64.getEncoder().encodeToString(data);
				//todo show qrcode
				c2bPaymentGetStatus();
			} else
				throw new Exception(StringUtils.defaultIfBlank(curC2BItem.urlErrMsg, "獲取Url失敗"));
		} catch (Exception e) {
			UniLog.log(e);
			//todo show error message
		}
	}

	private void c2bPaymentFinish() {
		if (curC2BItem == null)
			return;
		try {
			String errMsg = paymentFinish(curC2BItem.paymentMap);
			if (errMsg == null) {
				curC2BItem.ok = true;
				currentState = RUNSTATE.STATE_CHECKTYPE;
				processState(null);
			} else
				throw new Exception(errMsg);
		} catch (Exception e) {
			UniLog.log(e);
			//todo show error message
		}
		curC2BItem = null;
		if (c2bCancelDialog != null) {
			c2bCancelDialog.close();
			c2bCancelDialog = null;
		}
	}
	
	private void c2bPaymentShowCancelDialog() throws Exception {
		if (curC2BItem == null || curC2BItem.paymentMap != null) {
			c2bPaymentCancel();
			return;
		}
		ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
		c2bCancelDialog = new ZkBiMsgbox().setContent("確定取消付款？").setType(ZkBiMsgbox.Type.question).setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
				UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
				if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Ok"))) {
					c2bPaymentCancel();
					Events.echoEvent(Events.ON_CLICK, ldv_exit, null);
				} else {
					uiTimer.stop();
					uiTimer.start();
				}
			}
		}).build();
		c2bCancelDialog.doModal();
	}
	
	private void c2bPaymentCancel() {
		if (curC2BItem == null)
			return;
		C2BItem item = curC2BItem;
		curC2BItem = null;
		if (c2bCancelDialog != null) {
			c2bCancelDialog.close();
			c2bCancelDialog = null;
		}
		if (item.urlFuture != null)
			item.urlFuture.cancel(true);
		if (item.statusFuture != null)
			item.statusFuture.cancel(true);
		if (item.ok)
			return;
		CompletableFuture.runAsync(() -> {
			try {
	            switch (item.bank) {
	            case TAIFUNG:
	            case BOC:
	            	if (item.outTradeNo != null && item.url != null) {
	            		Map<String, String> m;
                 	    String rtnCode;
              	        String rtnMsg;
        	           	boolean b;
	            		if (item.bank == BANK.TAIFUNG) {
	            			m = MapUtil.of(
	            				"method", "TFPAY005",
          	           			"outTradeNo", item.outTradeNo);
         	           		b = SHA256withRSA.taifungH5Payment(m, sessionHelper);
                  	    	rtnCode = (String)m.get("rtnCode");
              	        	rtnMsg = (String)m.get("rtnMsg");
	            		} else {
	            			m = MapUtil.of(
	            				"service", "OrderCancel",
         	           			"requestId", item.outTradeNo);
         	           		b = SHA256withRSA.bocpayPayment(m, sessionHelper);
                  	    	rtnCode = (String)m.get("resultCode");
              	        	rtnMsg = (String)m.get("resultMsg");
	            		}
         	  		    UniLog.log1("cancel payment (b:%b, rtnCode:%s, rtnMsg:%s)", b, rtnCode, rtnMsg);
         	  		    m.put("out_trade_no", item.outTradeNo);
         	  		    updateEPaymentRecord3(m);
	            	}
	            	break;
            	default:
            		throw new Exception(String.format("Cancel payment failed (bank:%s)", item.bank));
	            }
			} catch (Exception e) {
				UniLog.log(e);
			}
		}, c2bThreadPool);
	}
	
	/*private EventListener<Event> c2bPaymentStatusListener = event -> {
		if (curC2BItem == null)
			return;
		String msg = (String)event.getData();
		//todo show status message
	};*/
	
	private String paymentFinish(Map<String, Object> m) {
		String errMsg = (String)m.get("errMsg");
		UniLog.log1("errMsg:%s", errMsg);
		if (errMsg == null) {
			try {
				Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sessionHelper, Erpv4Config.getDefaultCoCode(sessionHelper));
				String coName = StringUtils.defaultString((String)coMap.get("co_coname"));
				String coChnName = StringUtils.defaultString((String)coMap.get("co_chnname"));
				String outTradeNo = (String)m.get("outTradeNo");
				String transNo = (String)m.get("transNo");
				Double totalFee = (Double)m.get("totalFee");
				Long transTime = (Long)m.get("transTime");
				paymentBr.getCell("col_g").set("電子支付");
				paymentBr.getCell("col_w").set(transNo);
				paymentBr.addCurrent();
				String voucherNo = paymentBr.getCellString("col_b");
				ZkUtil.importAction.accept(sessionHelper, su -> {
					su.executeUpdate("update epayment set epm_voucherno = ?, epm_vtime = ? where epm_outtradeno = ?", 
							new Wherecl().appendArgument(voucherNo)
										.appendArgument(System.currentTimeMillis() / 1000)
										.appendArgument(outTradeNo));
				});
				ZkUtil.js("android.printPropertyMgmtReceipt('%s', '%s', '%s', '%s', %f, %d)", StringEscapeUtils.escapeJava(coName), StringEscapeUtils.escapeJava(coChnName), voucherNo, transNo, totalFee, transTime);
			} catch (Exception e) {
				UniLog.log(e);
		        return StringUtils.defaultIfBlank(e.getMessage(), e.toString());
			}
		}
		return errMsg;
	}

	
	private String genOutTradeNo() throws Exception {
		return paymentBr.getView().getSchema().getUniqueRg(null, "", 3001, "epayment", "epm_outtradeno", "EP&&&&&&&&&&").toString();
	}

	private String addEPaymentRecord(String type, String outTradeNo) throws Exception {
		UniLog.log1("outTradeNo:%s", outTradeNo);
		if (StringUtils.isBlank(outTradeNo))
			throw new Exception("outTradeNo is blank");
		ZkUtil.importAction.accept(sessionHelper, su -> {
			su.executeUpdate("insert into epayment(epm_outtradeno, epm_type, epm_ctime) values(?, ?, ?)", new Wherecl()
					.appendArgument(outTradeNo)
					.appendArgument(type)
					.appendArgument(System.currentTimeMillis() / 1000));
		});
		return outTradeNo;
	}

	private String addEPaymentRecord(String type) throws Exception {
		return addEPaymentRecord(type, genOutTradeNo());
	}
	
	//update b2c, c2b getUrl record
	private Map<String, Object> updateEPaymentRecord(BANK bank, Map<String, String> m, boolean b) throws Exception {
		UniLog.log1("outTradeNo:%s", m.get("out_trade_no"));
		Map<String, Object> m1 = getUpdateEPaymentMap(m);
        if (!b)
        	m1.put("errMsg", StringUtils.defaultIfBlank(m.get(bank == BANK.BOC ? "resultMessage" : "rtnMsg"), "付款失敗") + ", 請重新掃碼");
		ZkUtil.importAction.accept(sessionHelper, su -> {
			su.executeUpdate("update epayment set epm_transno = ?, epm_totalfee = ?, epm_rtncode = ?, epm_rtnmsg = ?, epm_resultcode = ?, epm_resultmsg = ?, epm_ttime = ? where epm_outtradeno = ?", 
					new Wherecl().appendArgument(m1.get("transNo"))
								.appendArgument(m1.get("totalFee"))
								.appendArgument(m1.get("rtnCode"))
								.appendArgument(m1.get("rtnMsg"))
								.appendArgument(m1.get("resultCode"))
								.appendArgument(m1.get("resultMessage"))
								.appendArgument(m1.get("transTime"))
								.appendArgument(m1.get("outTradeNo")));
		});
		return m1;
	}

	//update c2b payment record
	private Map<String, Object> updateEPaymentRecord2(BANK bank, Map<String, String> m, boolean b) throws Exception {
		UniLog.log1("outTradeNo:%s", m.get("out_trade_no"));
		Map<String, Object> m1 = getUpdateEPaymentMap(m);
        if (!b)
        	m1.put("errMsg", StringUtils.defaultIfBlank(m.get(bank == BANK.BOC ? "resultMessage" : "rtnMsg"), "付款失敗"));
		ZkUtil.importAction.accept(sessionHelper, su -> {
			su.executeUpdate("update epayment set epm_transno = ?, epm_rtncode2 = ?, epm_rtnmsg2 = ?, epm_resultcode2 = ?, epm_resultmsg2 = ?, epm_ttime2 = ? where epm_outtradeno = ?", 
					new Wherecl().appendArgument(m1.get("transNo"))
								.appendArgument(m1.get("rtnCode"))
								.appendArgument(m1.get("rtnMsg"))
								.appendArgument(m1.get("resultCode"))
								.appendArgument(m1.get("resultMessage"))
								.appendArgument(m1.get("transTime"))
								.appendArgument(m1.get("outTradeNo")));
		});
		return m1;
	}

	//update c2b cancel record
	private void updateEPaymentRecord3(Map<String, String> m) throws Exception {
		UniLog.log1("outTradeNo:%s", m.get("out_trade_no"));
		Map<String, Object> m1 = getUpdateEPaymentMap(m);
		ZkUtil.importAction.accept(sessionHelper, su -> {
			su.executeUpdate("update epayment set epm_rtncode3 = ?, epm_rtnmsg3 = ?, epm_resultcode3 = ?, epm_resultmsg3 = ?, epm_ttime3 = ? where epm_outtradeno = ?", 
					new Wherecl().appendArgument(m1.get("rtnCode"))
								.appendArgument(m1.get("rtnMsg"))
								.appendArgument(m1.get("resultCode"))
								.appendArgument(m1.get("resultMessage"))
								.appendArgument(m1.get("transTime"))
								.appendArgument(m1.get("outTradeNo")));
		});
	}

	private static Map<String, Object> getUpdateEPaymentMap(Map<String, String> m) {
        return MapUtil.of(
       		"outTradeNo", m.get("out_trade_no"),
       		"transNo", StringUtils.defaultString(m.get("transNo")),
        	"totalFee", NumberUtils.toDouble(m.get("totalFee")),
        	"transTime", m.containsKey("transTime") ? NumberUtils.toLong(m.get("transTime")) : (System.currentTimeMillis() / 1000),
        	"rtnCode", StringUtils.defaultString(m.get("rtnCode")),
        	"rtnMsg", StringUtils.defaultString(m.get("rtnMsg")),
        	"resultCode", StringUtils.defaultString(m.get("resultCode")),
        	"resultMessage", StringUtils.defaultString(m.get("resultMessage")));
	}

	private static boolean sleepSec(CompletableFuture<?> future, int sec) throws InterruptedException {
		while (!future.isCancelled() && sec > 0) {
			Thread.sleep(1000);
			sec--;
		}
		return future.isCancelled();
	}
}
