package com.uniinformation.bicore.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.uniinformation.utils.UniLog;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
//import com.uniinformation.jxapp.JxZkBiBase;
import com.kyoko.common.DateUtil;
import com.uniinformation.utils.BiUtil.CheckedConsumer3;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.MonthUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.utils.BiUtil.throwFunction;
import static com.uniinformation.utils.BiUtil.throwToIntFunction;


public class BiResultPayment  extends BiResultPropertyMgmt{
	LinkedHashMap<String,PayUnitRec> payUnits = new LinkedHashMap<String,PayUnitRec>();
	public static Object updateLocker = new Object();

	public BiResultPayment(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		payUnits = new LinkedHashMap<String,PayUnitRec>();
		// TODO Auto-generated constructor stub
	}
	
	class PayItemRec {
		double mgtFee;
		double resFee;
		public PayItemRec() {
		}
		public PayItemRec(double mgtFee, double resFee) {
			this.mgtFee = mgtFee;
			this.resFee = resFee;
		}
	}
	class PayUnitRec {
		String firstMonth;
		String lastMonth;
		int montCnt;
		double mgtFee;
		double resFee;
		LinkedHashMap<String,PayItemRec> items;
		PayUnitRec() {
			items = new LinkedHashMap<String,PayItemRec>();
		}
	}
	
	class PayUnitMonthFeeRec {
	    String unit;
	    String month;
	    double mgtFee;
	    double resFee;
		public PayUnitMonthFeeRec(String unit, String month, double mgtFee, double resFee) {
			this.unit = unit;
			this.month = month;
			this.mgtFee = mgtFee;
			this.resFee = resFee;
		}
	}
	
	public void payUnitClear() {
		payUnits = new LinkedHashMap<String,PayUnitRec>();
	}
	public boolean checkOrUnDeleteIfExist(String p_key,JxZkBiBase jxf) {
		BiResult sru = getSubLink("propertymgmt.payunit");
		int n = sru.getRowCount();
		for(int i=0;i<n;i++) {
			BiCellCollection bc = sru.getRowCollectionV(i);
			if(bc.getCellString("pu_unit").equals(p_key)) {
				Object o = sru.getTrStatObj(i);
				if(sru.isMarkedDelete(o)) {
					sru.markDelete(o, false);
					if(jxf != null) {
						JxField sv = jxf.jxAdd("list_"+jxf.replaceViewName(sru.getView().getName()));
						sv.gridSetDataFormat(-1,i,"remove_deleted");
					}
				}
				return(true);
			}
		}
		return(false);
	}
	public void payUnitAdd(String p_unit,String p_month,double p_mgtFee,double p_resFee) {
		PayUnitRec purec = payUnits.get(p_unit);
		if(purec == null) {
			purec = new PayUnitRec();
			payUnits.put(p_unit, purec);
		}
		if(p_month == null) return;
		PayItemRec pirec = purec.items.get(p_month);
		if(pirec == null) {
			pirec = new PayItemRec();
			purec.items.put(p_month, pirec);
		}
		pirec.mgtFee += p_mgtFee;
		pirec.resFee += p_resFee;
		purec.mgtFee += p_mgtFee;
		purec.resFee += p_resFee;
	}
	public void addPayUnitToList(JxZkBiBase jxf) {
		BiResult sru = getSubLink("propertymgmt.payunit");
		SelectUtil su = getSelectUtil();
		try {
		for(String payUnit : payUnits.keySet()) {
			PayUnitRec purec = payUnits.get(payUnit);
			BiCellCollection col = sru.newRowCollection();
			ReturnMsg rtn = sru.addSubRecord(col, -1,"");
			TableRec tr = su.getQueryResult("select * from property where key_a = ? ",
						new Wherecl().appendArgument(payUnit)
					);
			tr.setRecPointer(0);
			col.getCell("pu_block").set(tr.getFieldString("col_c"));
			col.getCell("pu_floor").set(tr.getFieldString("col_d"));
			col.getCell("pu_flat").set(tr.getFieldString("col_e"));
			col.getCell("pu_mgtfee").resetValue();
			col.getCell("pu_resfee").resetValue();
		}
		if(jxf != null) {
			JxField sv = jxf.jxAdd("list_"+jxf.replaceViewName(sru.getView().getName()));
			jxf.bindSublinkList(sv , sru);
		}
		} catch (Exception cex) {
			UniLog.log(cex);
		}
	}

	public void syncPayUnitFromPayItem() {
		payUnitClear();
		for(BiCellCollection bc : getSubLink("propertymgmt.payitem").getRowCollectionList()) {
			PayUnitRec purec = payUnits.get(bc.getCellString("col_c"));
			if(purec == null) {
				purec = new PayUnitRec();
				payUnits.put(bc.getCellString("col_c"), purec);
			}
			String monthStr = bc.getCellString("col_d");
			PayItemRec pirec = purec.items.get(monthStr);
			if(pirec == null) {
				pirec = new PayItemRec();
				purec.items.put(monthStr, pirec);
			}
			pirec.mgtFee += bc.getCellDouble("col_e");
			pirec.resFee += bc.getCellDouble("col_f");
			purec.mgtFee += bc.getCellDouble("col_e");
			purec.resFee += bc.getCellDouble("col_f");
			if(purec.lastMonth == null || monthStr.compareTo(purec.lastMonth) > 0) {
				purec.lastMonth = monthStr;
			}
			if(purec.firstMonth == null || monthStr.compareTo(purec.firstMonth) < 0) {
				purec.firstMonth = monthStr;
			}
		}
		
		BiResult sru = getSubLink("propertymgmt.payunit");
		SelectUtil su = getSelectUtil();
		try {
			LinkedHashMap<String, PayItemRec> map = payUnits.entrySet().stream()
							.collect(Collectors.toMap(it -> it.getKey(), it -> new PayItemRec(it.getValue().mgtFee, it.getValue().resFee), (o, n) -> n, LinkedHashMap::new));
			CheckedConsumer3<String, BiCellCollection, PayItemRec> colAction = (payUnit, col, purec) -> {
				TableRec tr = BiUtil.getFirstTableRec(su, "select * from property where key_a = ?", payUnit).orElseThrow(() -> new Exception("property record not found"));
				col.getCell("pu_block").set(tr.getFieldString("col_c"));
				col.getCell("pu_floor").set(tr.getFieldString("col_d"));
				col.getCell("pu_flat").set(tr.getFieldString("col_e"));
				col.getCell("pu_rmgtfee").set(purec.mgtFee);
				col.getCell("pu_rresfee").set(purec.resFee);
			};
			for (int ii = 0; ii < sru.getRowCount(); ii++) {
				Object o = sru.getTrStatObj(ii);
				BiCellCollection col = sru.getRowCollectionV(ii);
				String payUnit = col.getString("pu_unit");
				PayItemRec purec = map.get(payUnit);
				if (purec != null) {
					sru.markDelete(o, false);
					colAction.accept(payUnit, col, purec);
					map.remove(payUnit);
				} else
					sru.markDelete(o, true);
			}
			for (Map.Entry<String, PayItemRec> entry : map.entrySet()) {
				BiCellCollection col = sru.newRowCollection();
				sru.addSubRecord(col, "");
				colAction.accept(entry.getKey(), col, entry.getValue());
			}
			/*int n = 0;
			for(String payUnit : payUnits.keySet()) {
				PayUnitRec purec = payUnits.get(payUnit);
				BiCellCollection col;
				if(sru.getRowCount() <= n) {
					col = sru.newRowCollection();
					ReturnMsg rtn = sru.addSubRecord(col, -1,"");
				} else {
					Object o = sru.getTrStatObj(n);
					sru.markDelete( o, false);
					col = sru.getRowCollectionV(n);
				}
				TableRec tr = su.getQueryResult("select * from property where key_a = ? ",
							new Wherecl().appendArgument(payUnit)
						);
				tr.setRecPointer(0);
				col.getCell("pu_block").set(tr.getFieldString("col_c"));
				col.getCell("pu_floor").set(tr.getFieldString("col_d"));
				col.getCell("pu_flat").set(tr.getFieldString("col_e"));
				col.getCell("pu_rmgtfee").set(purec.mgtFee);
				col.getCell("pu_rresfee").set(purec.resFee);
				n++;
			}
			for(int i=n;i<sru.getRowCount();i++) {
				Object o = sru.getTrStatObj(new Integer(i));
				sru.markDelete( o, true);
			}*/
		} catch (Exception cex) {
			UniLog.log(cex);
		}
		syncPaymentFromPayItem(true);
	}

	public ReturnMsg syncPayItemFromPayUnit(JxZkBiBase jxf) {
		return syncPayItemFromPayUnit(jxf, null);
	}
	
	public ReturnMsg syncPayItemFromPayUnit(JxZkBiBase jxf, String paymentEndMonth) {
		BiResult sru = getSubLink("propertymgmt.payunit");
		BiResult sri = getSubLink("propertymgmt.payitem");
		//BiResult srp = getSubLink("propertymgmt.PayProjectItem");
		//Map<String, Map<Integer, Double>> projectfeeItemMap = new HashMap<>();
		try {
			if(StringUtils.isEmpty(getCellString("col_m"))) {
				int nM = 0;
				int cc;
				String monthStr;
				for(BiCellCollection bc : sru.getRowCollectionList()) {
					monthStr = bc.getCellString("pu_mgtstart");
					cc = MonthUtil.getMonth(monthStr);
					if(cc > nM ) nM = cc;
					monthStr = bc.getCellString("pu_resstart");
					cc = MonthUtil.getMonth(monthStr);
					if(cc > nM ) nM = cc;
					if(nM <= 0) {
						String ss = bc.getCellString("pu_jsondet");
						if(StringUtils.isBlank(ss)) continue;
						JSONArray ja = new JSONArray(ss);
						if(ja.length() > 0) {
							JSONObject jo = ja.getJSONObject(0);
							if (jo.getBoolean("enabled")) {
								monthStr = jo.getString("constart");
								cc = MonthUtil.getMonth(monthStr);
								cc--;
								if(cc > nM ) nM = cc;
							}
						}
					}
				}
				if(nM <= 0) {
					if(jxf != null) jxf.messageBox("Paid To Date Not Set");
					return(ReturnMsg.defaultOk);
				} else {
					if (StringUtils.isNotBlank(paymentEndMonth))
						getCell("col_m").set(paymentEndMonth);
					else {
						monthStr = MonthUtil.getMonth(nM+1);
						getCell("col_m").set(monthStr);
					}
				}
			}
			payUnitClear();

			for(BiCellCollection bc : sru.getRowCollectionList()) {
				String ss = bc.getCellString("pu_jsondet");
				if(StringUtils.isBlank(ss)) continue;
				JSONArray ja = new JSONArray(ss);
				double pmgtfee = bc.getCellDouble("pu_mgtfee");
				double restfee = bc.getCellDouble("pu_resfee");
				int nMmgt = MonthUtil.getMonth(bc.getCellString("pu_mgtstart"));
				int nMres = MonthUtil.getMonth(bc.getCellString("pu_resstart"));
//				boolean nMmgtIsLastMonth = true;
				boolean nMresIsLastMonth = true;
				for(int i=0;i<ja.length();i++) {
					JSONObject jo = ja.getJSONObject(i);
					if (!jo.getBoolean("enabled"))
						continue;
					double mgtpermonth = jo.getDouble("mgtfeepermon");
					double respermonth = jo.getDouble("resfeepermon");
					String constart = jo.getString("constart");
					int noofmonth = jo.getInt("noofmonth");
					int nMmax = MonthUtil.getMonth(constart) + noofmonth - 1;
					if(getCellBoolean("col_n") && mgtpermonth > 0) {
						if(nMmgt <= 0) {
							nMmgt = MonthUtil.getMonth(constart);
						} else {
							int nM0 = MonthUtil.getMonth(jo.getString("mgtlastmonth"));
							if(nM0 == nMmgt) {
								double fee = jo.getDouble("mgtfee") % mgtpermonth;
								if(fee > 0) {
									payUnitAdd(bc.getCellString("pu_unit"),MonthUtil.getMonth(nMmgt),fee,0);
									pmgtfee -= fee;
								}
								nMmgt++;
							}
							/*
							double fee = pmgtfee % mgtpermonth;
							if(nMmgtIsLastMonth) {
							if(fee > 0) {
								payUnitAdd(bc.getCellString("pu_unit"),MonthUtil.getMonth(nMmgt),fee,0);
								pmgtfee -= fee;
							}
							nMmgt++;
							nMmgtIsLastMonth = false;
							}
							*/
						}
						while(pmgtfee > 0 && nMmgt <= nMmax) {
							double fee;
							if(pmgtfee > mgtpermonth) fee = mgtpermonth; else fee = pmgtfee;
							payUnitAdd(bc.getCellString("pu_unit"),MonthUtil.getMonth(nMmgt),fee,0);
							pmgtfee -= fee;
							nMmgt++;
						}
					}
					if(getCellBoolean("col_o") && respermonth > 0) {
						if(nMres <= 0) {
							nMres = MonthUtil.getMonth(constart);
						} else {
							int nM0 = MonthUtil.getMonth(jo.getString("reslastmonth"));
							if(nM0 == nMres) {
								double fee = jo.getDouble("resfee") % respermonth;
								if(fee > 0) {
									payUnitAdd(bc.getCellString("pu_unit"),MonthUtil.getMonth(nMres),0,fee);
									restfee -= fee;
								}
								nMres++;
							}
							/*
							double fee = restfee % respermonth;
							if(nMresIsLastMonth) {
							if(fee > 0) {
								payUnitAdd(bc.getCellString("pu_unit"),MonthUtil.getMonth(nMres),0,fee);
								restfee -= fee;
							}
							nMres++;
							nMresIsLastMonth = false;
							}
							*/
						}
						while(restfee > 0 && nMres <= nMmax) {
							double fee;
							if(restfee > respermonth) fee = respermonth; else fee = restfee;
							payUnitAdd(bc.getCellString("pu_unit"),MonthUtil.getMonth(nMres),0,fee);
							restfee -= fee;
							nMres++;
						}
					}
					
					/*
					if(sri.getRowCount() <= n) {
						col = sri.newRowCollection();
						ReturnMsg rtn = sri.addSubRecord(col, n,"");
						if(rtn != null && !rtn.getStatus()) return(rtn);
					} else {
						Object o = sri.getTrStatObj(n);
						sri.markDelete( o, false);
						col = sri.getRowCollectionV(i);
					}
					col.getCell("col_c").set(bc.getCellString("pu_unit"));
					col.getCell("col_d").set(jo.getString("month"));
					col.getCell("col_e").set(jo.getDouble("mgtfee"));
					col.getCell("col_f").set(jo.getDouble("resfee"));
					n++;
					*/
				}
				
				/*//add project fee items
				if (!payItemOnly && srp != null) {
					SelectUtil su = getSelectUtil();
					String voucherNo = getCellString("col_b");
					String unit = bc.getCellString("pu_unit");
					TableRec tr;
					if (StringUtils.isNotBlank(voucherNo))
						tr = su.getQueryResult("select unitprojectfee.*, COALESCE(upf_totpayamt - col_d, 0) totpayamt from unitprojectfee "
											+ "left join payprojectitem on col_b = upf_projectrg and col_c = upf_unit "
											+ "where upf_unit = ? and (upf_totpayamt < upf_allocfee or col_a = ?)", 
								new Wherecl().appendArgument(unit).appendArgument(voucherNo));
					else
						tr = su.getQueryResult("select *, upf_totpayamt totpayamt from unitprojectfee where upf_unit = ? and upf_totpayamt < upf_allocfee", new Wherecl().appendArgument(unit));
					Map<Integer, Double> map = new LinkedHashMap<>();
					UniLog.log("unit:%s, recordcount:%d", unit, tr.getRecordCount());
					for (int i = 0; i < tr.getRecordCount(); i++) {
						tr.setRecPointer(i);
						int projectRg = tr.getFieldInt("upf_projectrg");
						double allocFee = tr.getFieldDouble("upf_allocfee");
						double singlepayFee = tr.getFieldDouble("upf_singlepayfee");
						double totalpayAmt = tr.getFieldDouble("totpayamt");
						UniLog.log("projectRg:%d, allocFee:%f, totalpayAmt:%f, singlepayFee:%f, voucherNo:%s", projectRg, allocFee, totalpayAmt, singlepayFee, voucherNo);
						if (totalpayAmt < allocFee)
							map.put(projectRg, Math.min(singlepayFee, allocFee - totalpayAmt));
					}
					projectfeeItemMap.put(unit, map);
				}*/
			}

			try {
				List<PayUnitMonthFeeRec> list = payUnits.entrySet().stream()
					    .flatMap(ue -> ue.getValue().items.entrySet().stream().map(ie -> new PayUnitMonthFeeRec(ue.getKey(), ie.getKey(), ie.getValue().mgtFee, ie.getValue().resFee)))
					    .collect(Collectors.toList());
				for (int ii = 0; ii < sri.getRowCount(); ii++) {
					Object o = sri.getTrStatObj(ii);
					BiCellCollection col = sri.getRowCollectionV(ii);
					PayUnitMonthFeeRec rec = list.stream().filter(it -> Objects.equals(col.getString("col_c"), it.unit) && Objects.equals(col.getString("col_d"), it.month)).findFirst().orElse(null);
					if (rec != null) {
						sri.markDelete(o, false);
						col.getCell("col_e").set(rec.mgtFee);
						col.getCell("col_f").set(rec.resFee);
						list.remove(rec);
					} else
						sri.markDelete(o, true);
				}
				for (PayUnitMonthFeeRec it : list) {
					BiCellCollection col = sri.newRowCollection();
					ReturnMsg rtn = sri.addSubRecord(col, "");
					if (rtn != null && !rtn.getStatus()) return(rtn);
					col.getCell("col_c").set(it.unit);
					col.getCell("col_d").set(it.month);
					col.getCell("col_e").set(it.mgtFee);
					col.getCell("col_f").set(it.resFee);
				}
			} catch (Exception cex) {
				UniLog.log(cex);
			}
			/*int n = 0;
			try {
				for(String payUnit : payUnits.keySet()) {
					BiCellCollection col;
					PayUnitRec purec = payUnits.get(payUnit);
					for(String payItem : purec.items.keySet()) {
						PayItemRec pirec = purec.items.get(payItem);
						if(sri.getRowCount() <= n) {
							col = sri.newRowCollection();
							ReturnMsg rtn = sri.addSubRecord(col, n,"");
							if(rtn != null && !rtn.getStatus()) return(rtn);
						} else {
							Object o = sri.getTrStatObj(n);
							sri.markDelete( o, false);
							col = sri.getRowCollectionV(n);
						}
						col.getCell("col_c").set(payUnit);
						col.getCell("col_d").set(payItem);
						col.getCell("col_e").set(pirec.mgtFee);
						col.getCell("col_f").set(pirec.resFee);
						n++;
					}
				}
			} catch (Exception cex) {
				UniLog.log(cex);
			}
			for(int i=n;i<sri.getRowCount();i++) {
				Object o = sri.getTrStatObj(new Integer(i));
				sri.markDelete( o, true);
			}*/
			
			
			if(jxf != null) {
				JxField sv = jxf.jxAdd("list_"+jxf.replaceViewName(sri.getView().getName()));
				jxf.bindSublinkList(sv , sri);
				syncPayUnitFromPayItem();
				sv = jxf.jxAdd("list_"+jxf.replaceViewName(sru.getView().getName()));
				jxf.bindSublinkList(sv , sru);
				/*if (!payItemOnly && srp != null) {
					sv = jxf.jxAdd("list_"+jxf.replaceViewName(srp.getView().getName()));
					jxf.bindSublinkList(sv , srp);
				}*/
				syncPaymentFromPayItem(false);
			}
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}

	String matchQstring(String p_qStr,String p_iStr) {
		if(p_qStr == null) return(null);
		if(p_qStr.isEmpty()) return(p_iStr);
		if(! p_qStr.equals(p_iStr)) return(null);
		return(p_qStr);
	}
	
	class PrepaidRec {
		int cnt;
		int lastPaidMonth;
	}
	
	void calPrepaidMonths(HashMap<String,PrepaidRec> pRecMap,String p_unit,double p_paidAmt,double p_dueAmt,int p_curMon, int p_paidMon) {
		if(p_paidAmt <= 0) return;
		if(p_dueAmt <= 0) return;
		PrepaidRec pRec = pRecMap.get(p_unit);
		if(pRec == null) {
			pRec = new PrepaidRec();
			pRecMap.put(p_unit,pRec);
		}
		if(pRec.lastPaidMonth < 0) return;
		if(p_paidAmt < p_dueAmt) {
			pRec.lastPaidMonth = -1;
		} else {
			if(pRec.lastPaidMonth == 0) {
				//if(p_paidMon >= p_curMon) { 預繳月數計算是不應該計繳交本月的管理費，要由次月開始算 1 個月
				if(p_paidMon > p_curMon) {
					pRec.lastPaidMonth = p_paidMon;
					pRec.cnt = 1;
				} else {
					pRec.lastPaidMonth = -1;
				}
			} else {
				if(p_paidMon == pRec.lastPaidMonth + 1) {
					pRec.lastPaidMonth = p_paidMon;
					pRec.cnt++;
				}
			}
		}
	}
	public void syncPaymentFromPayItem(boolean p_vcolOnly) {
		String qOwner = "";
		String qTel = "";
		String qBlock = "";
		String qFloor = "";
		String qUnit = "";
		String qPaidFrom = null;
		String qPaidTo = "";
		boolean pMgtFee = false;
		boolean pResFee = false;
		HashMap<String,PrepaidRec>mgtPrepaid = new HashMap<String,PrepaidRec>();
		HashMap<String,PrepaidRec>resPrepaid = new HashMap<String,PrepaidRec>();
		LinkedHashMap<String,String> punitHash = new LinkedHashMap<String,String>();
		SimpleDateFormat dfmt = new SimpleDateFormat("yyyy-MM");
		String curMonthStr = dfmt.format(DateUtil.nextday(getCellDate("col_a"),0));
		for(BiCellCollection bc : getSubLink("propertymgmt.payitem").getRowCollectionList()) {
			calPrepaidMonths(mgtPrepaid
						,bc.getCellString("col_c")
						,bc.getCellDouble("col_e")
						,bc.getCellDouble("mpy_mgtfee")
						,MonthUtil.getMonth(curMonthStr)
						,MonthUtil.getMonth(bc.getCellString("col_d"))
					);
			calPrepaidMonths(resPrepaid
						,bc.getCellString("col_c")
						,bc.getCellDouble("col_f")
						,bc.getCellDouble("mpy_resfee")
						,MonthUtil.getMonth(curMonthStr)
						,MonthUtil.getMonth(bc.getCellString("col_d"))
					);
			qOwner = matchQstring(qOwner,bc.getCellString("pm_col_h"));
			qTel = matchQstring(qTel,bc.getCellString("pm_col_l"));
			qBlock = matchQstring(qBlock,bc.getCellString("pm_col_c"));
			qFloor = matchQstring(qFloor,bc.getCellString("pm_col_d"));
			qUnit = matchQstring(qUnit,bc.getCellString("pm_col_e"));
			String ss = bc.getCellString("col_d");
			if(ss.compareTo(qPaidTo) > 0) qPaidTo = ss;
			if(qPaidFrom == null) qPaidFrom = ss; else {
				if(ss.compareTo(qPaidFrom) < 0) qPaidFrom = ss;
			}
			if(bc.getCellDouble("col_e") > 0) pMgtFee = true;
			if(bc.getCellDouble("col_f") > 0) pResFee = true;
			punitHash.put(bc.getCellString("col_c"), "");
		}
		try {
			if(! p_vcolOnly) {

			getCell("col_d").set(StringUtils.defaultString(qOwner == null ? "" : qOwner));
			getCell("col_e").set(StringUtils.defaultString(qOwner == null ? "" : qTel));
			getCell("col_i").set(StringUtils.defaultString(qOwner == null ? "" : qBlock));
			getCell("col_j").set(StringUtils.defaultString(qOwner == null ? "" : qFloor));
			getCell("col_k").set(StringUtils.defaultString(qOwner == null ? "" : qUnit));
			getCell("col_l").set(qPaidFrom == null ? "" : qPaidFrom);
			getCell("col_m").set(qPaidTo == null ? "" : qPaidTo);
			getCell("col_n").set(pMgtFee);
			getCell("col_o").set(pResFee);
			getCell("col_p").set(punitHash.size());
			

			getCell("col_q").set(0.0);
			getCell("col_s").set(0.0);
			}
			if (getCell("vcol_mgtprepaid") != null) {
				getCell("vcol_mgtprepaid").set(0);
				List<Integer> cntList = mgtPrepaid.values().stream().map(v -> v.cnt).collect(Collectors.toList());
				int mPrepaid;
				if (!cntList.isEmpty() && (mPrepaid = cntList.get(0)) > 0 && cntList.stream().distinct().count() == 1) {
					Map<Integer, Double> m = MapUtil.of(
						getCellInt("lc_mgtm1"), getCellDouble("lc_mgtd1"),
						getCellInt("lc_mgtm2"), getCellDouble("lc_mgtd2"));
					Double mgtd = m.get(mPrepaid);
					if (mgtd != null) {
						getCell("vcol_mgtprepaid").set(mPrepaid);
						if (!p_vcolOnly)
							getCell("col_q").set(Math.floor(getCellDouble("col_f") * mgtd / 100.0));
					}
					/*int mPrepaid = Integer.MAX_VALUE;
					for(PrepaidRec prec : mgtPrepaid.values()) {
						if(prec.cnt < mPrepaid) mPrepaid = prec.cnt;
					}
					getCell("vcol_mgtprepaid").set(mPrepaid);
					if(! p_vcolOnly) {
					if(getCellInt("lc_mgtm2") > 0 && mPrepaid >= getCellInt("lc_mgtm2")) {
						double disc = getCellDouble("col_f") * getCellDouble("lc_mgtd2") / 100.0;
						getCell("col_q").set(Math.floor(disc));
					} else {
					if(getCellInt("lc_mgtm1") > 0 && mPrepaid >= getCellInt("lc_mgtm1")) {
						double disc = getCellDouble("col_f") * getCellDouble("lc_mgtd1") / 100.0;
						getCell("col_q").set(Math.floor(disc));
					}
					}
					}*/
				}
			}
			if(getCell("vcol_resprepaid") != null) {
				getCell("vcol_resprepaid").set(0);
				List<Integer> cntList = resPrepaid.values().stream().map(v -> v.cnt).collect(Collectors.toList());
				int mPrepaid;
				if (!cntList.isEmpty() && (mPrepaid = cntList.get(0)) > 0 && cntList.stream().distinct().count() == 1) {
					Map<Integer, Double> m = MapUtil.of(
						getCellInt("lc_resm1"), getCellDouble("lc_resd1"),
						getCellInt("lc_resm2"), getCellDouble("lc_resd2"));
					Double resd = m.get(mPrepaid);
					if (resd != null) {
						getCell("vcol_resprepaid").set(mPrepaid);
						if (!p_vcolOnly)
							getCell("col_s").set(Math.floor(getCellDouble("col_t") * resd / 100.0));
					}
					/*int mPrepaid = Integer.MAX_VALUE;
					for(PrepaidRec prec : resPrepaid.values()) {
						if(prec.cnt < mPrepaid) mPrepaid = prec.cnt;
					}
					getCell("vcol_resprepaid").set(mPrepaid);
					if(! p_vcolOnly) {
					if(getCellInt("lc_resm2") > 0 && mPrepaid >= getCellInt("lc_resm2")) {
						double disc = getCellDouble("col_t") * getCellDouble("lc_resd2") / 100.0;
						getCell("col_s").set(Math.floor(disc));
					} else {
					if(getCellInt("lc_resm1") > 0 && mPrepaid >= getCellInt("lc_resm1")) {
						double disc = getCellDouble("col_t") * getCellDouble("lc_resd1") / 100.0;
						getCell("col_s").set(Math.floor(disc));
					}
					}
					}*/
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	
//	@Override
//	public void afterFetch() {
//		super.afterFetch();
//		syncPayUnitFromPayItem();
//	}

	@Override
	protected void afterFetch() {
		try {
			if (StringUtils.isBlank(getCellString("vcol_mgtprepaiddis")))
				getCell("vcol_mgtprepaiddis").set("0 | 0%");
			if (StringUtils.isBlank(getCellString("vcol_resprepaiddis")))
				getCell("vcol_resprepaiddis").set("0 | 0%");
		} catch (CellException e) {
			UniLog.log(e);
		}
	}

	@Override
	public void beforeBind() {
		super.beforeBind();
		syncPayUnitFromPayItem();
	}

	@Override
	protected void setLookupItemList(TableRec lookupTableTr,ColumnCell colCell) throws Exception {
		setLookupItemList(lookupTableTr, colCell, " | ");
	}

	@Override
	protected ReturnMsg validateOneRow(CellCollection col, boolean p_update) {
		ReturnMsg rtnMsg = super.validateOneRow(col, p_update);
		if (rtnMsg != null && !rtnMsg.getStatus()) return rtnMsg;

		try {
			rtnMsg = validationRecord(p_update);
		} catch (Exception cex) {
			UniLog.log(cex);
			return new ReturnMsg(false, -1, cex.getMessage());
		}

		return rtnMsg;
	}

	public ReturnMsg validationRecord(boolean isUpdate) throws Exception {
		String s = getCellString("col_r");
		if (StringUtils.isNotEmpty(s) && !s.matches("[A-Za-z0-9]{1,10}"))
			return new ReturnMsg(false, "[參考編號]必须10個數字或英文以内");
		return ReturnMsg.defaultOk;
	}
	
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		/*
		BiResult sri = getSubLink("propertymgmt.payitem");
		if(sri.getRowCount() > 0) return(ReturnMsg.defaultOk);
		if(getCurrentCollection().getSid() == 0) {
			// is new record
			rtn = syncPayItemFromPayUnit();
		}
		*/
		syncPaymentFromPayItem(false);
		return(rtn);
	}

	@Override
	protected ReturnMsg addCurrent(BiCellCollection cl) {
		synchronized (updateLocker) {
			ReturnMsg rtn = setupNextPaymentNo(sh, this);
			if (!rtn.getStatus())
				return rtn;
			return super.addCurrent(cl);
		}
	}

	@Override
	public ReturnMsg updateCurrent() {
		synchronized (updateLocker) {
			ReturnMsg rtn = setupNextPaymentNo(sh, this);
			if (!rtn.getStatus())
				return rtn;
			return super.updateCurrent();
		}
	}
	
	public static ReturnMsg setupNextPaymentNo(SessionHelper sh, BiResult br) {
		try {
			if (StringUtils.isBlank(br.getCellString("col_b"))) {
				int lcrg = Erpv4Config.getDefaultLcrg(sh);
	    		String ss = Erpv4Config.getLcDesc(sh, lcrg);
	    		int paymentNum = Stream.of("select max(col_b) pmno from payment where col_c = ?").mapToInt(throwToIntFunction(sql -> {
	    			return BiUtil.getFirstTableRec(br.getSelectUtil(), sql, new Wherecl().appendArgument(ss)).map(throwFunction(tr -> {
	    				String s = tr.getFieldString("pmno");
	    				return StringUtils.isNotBlank(s) ? Integer.parseInt(s.substring(3)) + 1 : 1;
	    			})).orElse(1);
				})).max().orElse(1);
				br.getCell("col_b").set(String.format("%02d-%06d", lcrg, paymentNum));
			}
			return ReturnMsg.defaultOk;
		} catch (Exception e) {
			return new ReturnMsg(e);
		}
	}
}