package com.uniinformation.zkf.propertymgmt;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.mail.Provider.Type;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
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
import org.zkoss.zul.Vlayout;

import com.kyoko.common.ReturnMsg;
import com.kyoko.crypto.SHA256withRSA;
import com.kyoko.utils.UrlUtils;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultPayment;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.QRCodeUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkFormEpayment3 extends ZkCellActionForm {
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
    
    Template template_payUnit;
	
	TableRec devTr;
	TableRec typeTr;
	TableRec blockTr;
	TableRec floorTr;
	TableRec unitTr;
	Timer uiTimer;
	static enum RUNSTATE {STATE_CHECKTYPE,STATE_WAITTYPE,STATE_CHECKBLOCK,STATE_WAITBLOCK,STATE_CHECKFLOOR,STATE_WAITFLOOR,STATE_CHECKUNIT,STATE_WAITUNIT,STATE_CHECKPAYER,STATE_WAITPASSWORD,
			STATE_CHECKPAYMENT,STATE_WAITPAYMENT,STATE_CHECKSCANSLIP,STATE_WAITSCANSLIP,STATE_WAITMORESLIP}
	static int maxA = 4; 
	static int maxB = 12; 
	static String[] EPAYMENT_NAME = {"大豐銀行", "中國銀行"};
	BiResultPayment paymentBr;
	String password;
	String keybuf="";
//	double mgtFee;
//	double resFee;
//	String payToMonth;
	String barcodeScanner;
	
	RUNSTATE currentState;
	
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
	void displayUnitList() throws Exception {
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
		BiResult sr = paymentBr.getSubLink("propertymgmt.payunit");
		Vector<BiCellCollection> sl = sr.getRowCollectionList();
		for(int i=0;i<sl.size();i++) {
			BiCellCollection bc = sl.get(i);
			Listitem li = new Listitem();
			Listcell lc;
			lc = new Listcell();
			Component carr[];
			carr = template_payUnit.create(lc, null, null, null);
			rootComp = carr[0];
			Label lb = (Label) rootComp.getFellowIfAny("unit_name");
			if(lb != null) {
				lb.setId(lb.getId()+"_"+i);
				lb.setValue(
					CellCollection.stringCombine(
						bc.getCellString("pu_block"),
						bc.getCellString("pu_floor"),
						bc.getCellString("pu_unit")
							)
						);
			}
			
			double mfee = bc.getCellDouble("pu_mgtfee");
			lb = (Label) rootComp.getFellowIfAny("unit_mgtfee");
			if(lb != null) {
				lb.setId(lb.getId()+"_"+i);
				if(mfee > 0.0) {
					lb.setVisible(true);
					lb.setValue( "管理費:" + df.format(mfee));
				} else {
					lb.setVisible(false);
				}
			}
			lb = (Label) rootComp.getFellowIfAny("unit_mgtfrom");
			if(lb != null) {
				lb.setId(lb.getId()+"_"+i);
				if(mfee > 0.0) {
					lb.setVisible(true);
					lb.setValue( bc.getCellString("pu_mgtstart"));
				} else {
					lb.setVisible(false);
				}
			}
			double rfee = bc.getCellDouble("pu_resfee");
			lb = (Label) rootComp.getFellowIfAny("unit_resfee");
			if(lb != null) {
				lb.setId(lb.getId()+"_"+i);
				if(rfee > 0.0) {
					lb.setVisible(true);
					lb.setValue( "儲備金:" + df.format(rfee));
				} else {
					lb.setVisible(false);
				}
			} 
			lb = (Label) rootComp.getFellowIfAny("unit_resfrom");
			if(lb != null) {
				lb.setId(lb.getId()+"_"+i);
				if(rfee > 0.0) {
					lb.setVisible(true);
					lb.setValue( bc.getCellString("pu_resstart"));
				} else {
					lb.setVisible(false);
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
			lc.setParent(li);
			lb_unitList.appendChild(li);
		}
		lb_unitList.appendChild(li_ask_for_more);
		if(lb_paidto != null) {
			String ss = paymentBr.getCellString("col_m");
			lb_paidto.setValue("繳費至:"+ss);
		}
		if(lb_paidtotal != null) {
			double tamt = paymentBr.getCellDouble("vcol_actualfee");
			lb_paidtotal.setValue( "總金額:"+df.format(tamt));
		}
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
					ZkUtil.setEventListenerForCallOne(barcodeS1, "onPostQrCodeData", event -> {
						ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", event.getData(), onClickListener);
					});
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
					UniLog.log("qrCodeData received");
					Map<String,String> pmap = UrlUtils.getQueryParams( (String)ev.getData());
					String punit = null;
					if(pmap != null) punit = pmap.get("punit");
					if(punit != null) {
						boolean ok = syncPropertyByPropertyUnit(punit);
						if(ok) {
							/*
							if("WEBCAM".equals(barcodeScanner)) {
								ZkUtil.js("stopWebcamScanner()");
							}
							*/
							currentState = RUNSTATE.STATE_CHECKPAYER;
							break;
						}
					}
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
							paymentBr.syncPayItemFromPayUnit(null);
							displayUnitList();
							if("WEBCAM".equals(barcodeScanner)) {
								ZkUtil.js("startWebcamScanner()");
							} else {
								ZkUtil.js("android.connectBarcodeScanner('postQrCodeData')");
							}
							currentState = RUNSTATE.STATE_WAITMORESLIP;
							return;
						} else {
							currentState = RUNSTATE.STATE_CHECKTYPE;
						}
					}
				break;
		case STATE_WAITMORESLIP:
			{
				String butId = ev.getTarget().getId();
//				if(butId.equals("uiTimer")) {
//					UniLog.log("Wait for barcode timeout");
//					currentState = RUNSTATE.STATE_CHECKTYPE;
//				}
				if (butId.equals("barcodeS1") && ev.getName().equals("onPostQrCodeData1")) {
					UniLog.log("qrCodeData received");
					Map<String,String> pmap = UrlUtils.getQueryParams( (String)ev.getData());
					String punit = null;
					if(pmap != null) punit = pmap.get("punit");
					if(punit != null) {
						boolean ok = syncPropertyByPropertyUnit(punit);
						if(ok) {
							BiResult sr = paymentBr.getSubLink("propertymgmt.payunit");
							CellCollection col = sr.newRowCollection();
							sr.addSubRecord(col, -1 ,"");
							col.getCell("pu_block").set(blockTr.getFieldString("pblock"));
							col.getCell("pu_floor").set(floorTr.getFieldString("pfloor"));
							col.getCell("pu_flat").set(unitTr.getFieldString("punit"));
						}
					}
//							(String)ev.getData());
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
					ldv_comment.setValue("請掃描手機支付條碼\nPlease Scan Mobile App's Payment Barcode ("+EPAYMENT_NAME[devTr.getFieldInt("lc_epayment")]+")");
//					ldv_comment.setValue("pay up to " + payToMonth + " amount " + mgtFee);
//					uiTimer.start();
					displayAndWaitBarcode1();
					ZkUtil.setEventListenerForCallOne(barcodeS1, "onPostQrCodeData", event -> {
						lbPaymentMsg.setVisible(true);
						lbPaymentMsg.setValue("正在處理，請稍候");
						ZkUtil.echoEvent(barcodeS1, "onPostQrCodeData1", event.getData(), onClickListener);
					});
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
					UniLog.log("qrCodeData received");
					double actualFee = paymentBr.getCellDouble("vcol_actualfee");
					Map<String, Object> m = ePayment(devTr.getFieldInt("lc_epayment"), actualFee, (String)ev.getData());
					Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sessionHelper, "001");
					String coName = StringUtils.defaultString((String)coMap.get("co_coname"));
					String coChnName = StringUtils.defaultString((String)coMap.get("co_chnname"));
					String outTradeNo = (String)m.get("outTradeNo");
					String transNo = (String)m.get("transNo");
					String errMsg = (String)m.get("errMsg");
					double totalFee = (double)m.get("totalFee");
					int transTime = (int)m.get("transTime");
					if (errMsg == null) {
						paymentBr.addCurrent();
						String voucherNo = paymentBr.getCellString("col_b");
						ZkUtil.importAction.accept(sessionHelper, su -> {
							su.executeUpdate("update epayment set epm_voucherno = ?, epm_vtime = ? where epm_outtradeno = ?", 
									new Wherecl().appendArgument(voucherNo)
												.appendArgument(System.currentTimeMillis() / 1000)
												.appendArgument(outTradeNo));
						});
						ZkUtil.js("android.printPropertyMgmtReceipt('%s', '%s', '%s', '%s', %f, %d)", StringEscapeUtils.escapeJava(coName), StringEscapeUtils.escapeJava(coChnName), voucherNo, transNo, totalFee, transTime);
						currentState = RUNSTATE.STATE_CHECKTYPE;
					} else {
						lbPaymentMsg.setVisible(true);
						lbPaymentMsg.setValue(errMsg);
						currentState = RUNSTATE.STATE_CHECKPAYMENT;
						Events.echoEvent("onPostQrCodeData1", barcodeS1, null);
						return;
					}
					/*paymentBr.addCurrent();
					Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sessionHelper, "001");
					String coName = StringUtils.defaultString((String)coMap.get("co_coname"));
					String coChnName = StringUtils.defaultString((String)coMap.get("co_chnname"));
					String voucherNo = paymentBr.getCellString("col_b");
					double actualFee = paymentBr.getCellDouble("vcol_actualfee");
					ZkUtil.js("android.printPropertyMgmtReceipt('%s', '%s', '%s', '%s', %f, %d)", StringEscapeUtils.escapeJava(coName), StringEscapeUtils.escapeJava(coChnName), voucherNo, "TRANSXXX", actualFee, (int)(System.currentTimeMillis() / 1000));*/
					currentState = RUNSTATE.STATE_CHECKTYPE;
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
				uiTimer.stop();
				Component c = (Component)arg0.getTarget();
				UniLog.log("Event " + arg0.getName() + " Id " + c.getId());
				if(arg0 != null && 
						(arg0.getTarget().getId().equals("uiTimer") ||
						arg0.getTarget().getId().equals("ldv_exit"))
						) {
					UniLog.log("Exit key pressed or timer fired");
					paymentBr.rollbackWork();
					paymentBr.clearCurrentRec();
					currentState = RUNSTATE.STATE_CHECKTYPE;
					if("WEBCAM".equals(barcodeScanner)) {
						ZkUtil.js("stopWebcamScanner()");
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
			if(!StringUtils.isBlank(ss)) USER_INPUT_TIMEOUT = Integer.parseInt(ss) * 1000;
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
		formCollection.getCell("ldv_cmpname").set(Erpv4Config.getCoName(sessionHelper, Erpv4Config.getDefaultCoCode(sessionHelper)));
		formCollection.getCell("ldv_locname").set(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
		formCollection.getCell("ldv_title").set("自助繳費機");
		typeTr = su.getQueryResult("select distinct col_a ptype from property where col_b = ? ",
					new Wherecl().appendArgument(devTr.getFieldString("lc_desc"))
				 );

		template_payUnit = arg0.getTemplate("template_payUnit");

		currentState = RUNSTATE.STATE_CHECKTYPE;
		processState(null);
	}
	
	private Map<String, Object> ePayment(int type, double fee, String authCode) throws Exception {
		return type == 1 ? bocpayPayment(fee, authCode) : taifungPayment(fee, authCode);
	}
	
	private Map<String, Object> taifungPayment(double fee, String authCode) throws Exception {
		String type = "taifung";
		String outTradeNo = addEPaymentRecord(type);
        Map<String, String> m = new HashMap<>();
        m.put("service", "pay.qrcode.micropay");
        m.put("out_trade_no", outTradeNo);
        m.put("total_fee", String.valueOf((int)(fee * 100)));
        m.put("body", "propmgmt_epayment");
        m.put("auth_code", authCode);

        boolean b = SHA256withRSA.taifungPayment(m, sessionHelper);
        String rtnCode = (String)m.get("rtnCode");
        String rtnMsg = (String)m.get("rtnMsg");
        String resultCode = (String)m.get("resultCode");
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
		return updateEPaymentRecord(type, m, b);
	}

	private Map<String, Object> bocpayPayment(double fee, String authCode) throws Exception {
		String type = "bocpay";
		String outTradeNo = addEPaymentRecord(type);
        Map<String, String> m = new HashMap<>();
        m.put("service", "B2CPay");
        m.put("requestId", outTradeNo);
        m.put("amount", String.valueOf((int)(fee * 100)));
        m.put("subject", "propmgmt_epayment");
        m.put("authCode", authCode);

        boolean b = SHA256withRSA.bocpayPayment(m, sessionHelper);
        String resultCode = (String)m.get("resultCode");
        String resultMsg = (String)m.get("resultMessage");
        int valTime = Math.max(NumberUtils.toInt((String)m.get("valTime")), 60);
        int tryCount = 10;
        while (!b && ((StringUtils.isBlank(resultCode) && StringUtils.isNotBlank(resultMsg)) || StringUtils.equalsAny(resultCode, "Z", "A")) && --tryCount > 0 && valTime > 0) {
        	Thread.sleep(5000);
        	UniLog.log1("payment failed (rtnCode:%s, resultCode:%s, tryCount:%d), query payment", m.get("rtnCode"), m.get("resultCode"), tryCount);
        	m = new HashMap<>();
        	m.put("requestId", outTradeNo);
        	m.put("service", "OrderQuery");
        	m.put("qryNo", outTradeNo);
        	b = SHA256withRSA.bocpayPayment(m, sessionHelper);
        	resultCode = (String)m.get("resultCode");
        	resultMsg = (String)m.get("resultMessage");
        	if (!b && StringUtils.equalsAny(resultCode, "Z", "A")) {
        		valTime -= 5;
        		tryCount++;
        	}
        }
        m.put("out_trade_no", outTradeNo);
		return updateEPaymentRecord(type, m, b);
	}
	
	private String addEPaymentRecord(String type) throws Exception {
		String outTradeNo = paymentBr.getView().getSchema().getUniqueRg(null, "", 3001, "epayment", "epm_outtradeno", "EP&&&&&&&&&&").toString();
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
	
	private Map<String, Object> updateEPaymentRecord(String type, Map<String, String> m, boolean b) throws Exception {
		double totalFee = NumberUtils.toDouble(m.get("totalFee"));
		int transTime = (int)(System.currentTimeMillis() / 1000);
		if (m.containsKey("transTime"))
			transTime = Integer.parseInt(m.get("transTime"));
        Map<String, Object> m1 = new HashMap<>();
        m1.put("outTradeNo", m.get("out_trade_no"));
        m1.put("transNo", StringUtils.defaultString(m.get("transNo")));
        m1.put("totalFee", totalFee);
        m1.put("transTime", transTime);
        m1.put("rtnCode", StringUtils.defaultString(m.get("rtnCode")));
        m1.put("rtnMsg", StringUtils.defaultString(m.get("rtnMsg")));
        m1.put("resultCode", StringUtils.defaultString(m.get("resultCode")));
        m1.put("resultMessage", StringUtils.defaultString(m.get("resultMessage")));
        if (!b)
        	m1.put("errMsg", StringUtils.defaultIfBlank(m.get(type.equals("bocpay") ? "resultMessage" : "rtnMsg"), "付款失敗") + ", 請重新掃碼");
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
	
}
