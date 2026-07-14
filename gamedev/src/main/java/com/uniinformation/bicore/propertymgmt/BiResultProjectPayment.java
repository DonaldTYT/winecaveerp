package com.uniinformation.bicore.propertymgmt;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.ListGetItemProperty;
import com.uniinformation.utils.MapUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.utils.ZkUtil.throwConsumer;
import static com.uniinformation.utils.ZkUtil.throwFunction;
import static com.uniinformation.utils.ZkUtil.throwToIntFunction;

public class BiResultProjectPayment extends BiResultPropertyMgmt {
	public static Object updateLocker = new Object();

	public BiResultProjectPayment(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	protected void setLookupItemList(TableRec lookupTableTr, ColumnCell colCell) throws Exception {
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
		Vector<BiCellCollection> list = getSubLinkResult("propertymgmt.PayProjectItem");
		if (list.stream().anyMatch(c -> c.getInt("col_e") <= 0 || c.getInt("col_f") <= 0))
			return new ReturnMsg(false, "[繳交第期]或[繳交期數]必須大於0");
		if (list.stream().anyMatch(c -> c.getInt("col_e") + c.getInt("col_f") - 1 > c.getInt("pjf_period")))
			return new ReturnMsg(false, "繳交期數不能大於總期數");
		return ReturnMsg.defaultOk;
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return rtn;
		try {
			syncPaymentFromPayItem();
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		return rtn;
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		try {
			String voucherNo = col.getString("col_b");
			String locDesc = col.getString("col_c");
			ZkUtil.getTableRecStream(su, "select col_c, col_d, col_e, col_f, upf_period1amt, upf_period2amt, upf_period3amt, upf_period4amt, upf_period5amt, upf_totpayperiod, upf_totpayamt, upf_projectperiodcnt, upf_allocamt from payprojectitem join unitprojectfee on upf_projectno = col_c and upf_unit = col_d "
										+ "where col_a = ? and col_b = ? ", 
									new Wherecl().appendArgument(voucherNo).appendArgument(locDesc)).forEach(throwConsumer(c -> {
				String projectNo = c.getString("col_c");
				String unit = c.getString("col_d");
				if (c.getCellInt("upf_totpayperiod") > c.getInt("upf_projectperiodcnt") || c.getDouble("upf_totpayamt") > c.getDouble("upf_allocamt"))
					throw new Exception(String.format("缴交期数或金额已超出范围[%s,%s] (%d,%f)", projectNo, unit, c.getInt("upf_totpayperiod"), c.getDouble("upf_totpayamt")));

				Set<Integer> periodRange1 = IntStream.range(c.getInt("col_e"), c.getInt("col_e") + c.getInt("col_f")).mapToObj(period -> period).collect(Collectors.toCollection(TreeSet::new));
				Set<Integer> periodRange2 = ZkUtil.getTableRecStream(su, "select col_e from payprojectitem2 where col_a = ? and col_c = ? and col_d = ?", 
													new Wherecl().appendArgument(voucherNo).appendArgument(projectNo).appendArgument(unit))
													.map(c1 -> c1.getInt("col_e")).collect(Collectors.toCollection(TreeSet::new));
				UniLog.log1("periodRange1:%s, periodRange2:%s", periodRange1, periodRange2);
				periodRange2.stream().filter(item -> !periodRange1.contains(item)).forEach(throwConsumer(period -> {
					UniLog.log1("delete from payprojectitem2 %s", period);
					su.executeUpdate("delete from payprojectitem2 where col_a = ? and col_c = ? and col_d = ? and col_e = ?", 
							new Wherecl().appendArgument(voucherNo).appendArgument(projectNo).appendArgument(unit).appendArgument(period));
				}));
				periodRange1.stream().filter(item -> !periodRange2.contains(item)).forEach(throwConsumer(period -> {
					ZkUtil.executeInsertIntoSql(su, "payprojectitem2", Arrays.asList("col_a", "col_b", "col_c", "col_d", "col_e", "col_f"), 
							new Wherecl().appendArgument(voucherNo).appendArgument(locDesc).appendArgument(projectNo).appendArgument(unit).appendArgument(period)
											.appendArgument(c.getDouble("upf_period"+period+"amt")));
				}));
				/*Set<Integer> periodList = new HashSet<>();
				ZkUtil.getTableRecStream(su, "select col_a, col_e, col_f from payprojectitem where col_c = ? and col_d = ?", new Wherecl().appendArgument(projectNo).appendArgument(unit)).forEach(c1 -> {
					IntStream.range(c1.getInt("col_e"), c1.getInt("col_e") + c1.getInt("col_f")).forEach(throwIntConsumer(period -> {
						if (periodList.contains(period))
							throw new Exception(String.format("繳交期重複[%s][%s][%s][%d]", c1.getString("col_a"), projectNo, unit, period));
						else
							periodList.add(period);
					}));
				});*/
			}));
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.toString(), true);
		}
		return ReturnMsg.defaultOk;
	}

	@Override
	protected ReturnMsg biAfterDeleteCurrent(CellCollection col) {
		try {
			su.executeUpdate("delete from payprojectitem2 where col_a = ?", new Wherecl().appendArgument(col.getString("col_b")));
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.toString(), true);
		}
		return ReturnMsg.defaultOk;
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
	    		int paymentNum = Stream.of("select max(col_b) pmno from projectpayment where col_c = ?").mapToInt(throwToIntFunction(sql -> {
	    			return ZkUtil.getFirstTableRec(br.getSelectUtil(), sql, new Wherecl().appendArgument(ss)).map(throwFunction(tr -> {
	    				String s = tr.getFieldString("pmno");
	    				return StringUtils.isNotBlank(s) ? Integer.parseInt(s.substring(4)) + 1 : 1;
	    			})).orElse(1);
				})).max().orElse(1);
				br.getCell("col_b").set(String.format("%02d-A%05d", lcrg, paymentNum));
			}
			return ReturnMsg.defaultOk;
		} catch (Exception e) {
			return new ReturnMsg(e);
		}
	}

	public void syncPaymentFromPayItem() throws Exception {
		UniLog.log("syncPaymentFromPayItem");
		Vector<BiCellCollection> list = getSubLinkResult("propertymgmt.PayProjectItem");
		String owner = list.stream().map(c -> c.getString("pcol_h")).distinct().limit(2).count() == 1 ? list.get(0).getString("pcol_h") : null;
		if (owner != null) {
			getCell("col_d").set(owner);
			Map<String, String> m = MapUtil.of("col_e", "pcol_l", "col_i", "pcol_c", "col_j", "pcol_d", "col_k", "pcol_e");
			m.entrySet().forEach(throwConsumer(e -> {
				getCell(e.getKey()).set(list.stream().map(c -> c.getString(e.getValue())).distinct().limit(2).count() == 1 ? list.get(0).getString(e.getValue()) : "");
			}));
		} else
			Stream.of("col_d", "col_e", "col_i", "col_j", "col_k").forEach(throwConsumer(k -> getCell(k).set("")));
		getCell("col_p").set((int)list.stream().map(c -> c.getString("col_d")).distinct().count());
		getCell("col_f").set(list.stream().mapToDouble(c -> c.getDouble("col_g")).sum());
	}

	public void calcPaidPeriodAndAmount(CellCollection cl, boolean isInit) throws Exception {
		String voucherNo = getCellString("col_b");
		String projectNo = cl.getString("col_c");
		String unit = cl.getString("col_d");
		CellCollection[] arr = ZkUtil.getTableRecStream(getSelectUtil(), "select col_a, col_e, col_f, col_g from payprojectitem where col_b = ? and col_c = ? and col_d = ?", 
									new Wherecl().appendArgument(getCellString("col_c")).appendArgument(projectNo).appendArgument(unit)).toArray(CellCollection[]::new);
		CellCollection cellCur = Arrays.stream(arr).filter(c -> Objects.equals(c.getString("col_a"), voucherNo)).findFirst().orElse(null);
		int periodCur = cellCur != null ? cellCur.getInt("col_e") : 0;
		int periodCurCnt = cellCur != null ? cellCur.getInt("col_f") : 0; 
		int periodMax = 0;
		int totPeriodCnt = 0, totPeriodCnt2 = 0;
		double totAmt = 0, totAmt2 = 0;
		for (CellCollection cc : arr) {
			int periodStart = cc.getCellInt("col_e");
			int periodCnt = cc.getCellInt("col_f");
			double amt = cc.getCellDouble("col_g");
			if (periodCur <= 0 || periodStart < periodCur) {
				totPeriodCnt += periodCnt;
				totAmt += amt;
			}
			totPeriodCnt2 += periodCnt;
			totAmt2 += amt;
			periodMax = Math.max(periodMax, periodStart);
		}
		cl.getCell("vcol_paidperiod").set(totPeriodCnt);
		cl.getCell("vcol_paidamt").set(totAmt);
		cl.getCell("vcol_paidperiod2").set(totPeriodCnt2);
		cl.getCell("vcol_paidamt2").set(totAmt2);
		if (!isInit)
			cl.getCell("col_e").set(periodCur > 0 ? periodCur : totPeriodCnt2 + 1);
		if (periodCur > 0 && periodCur < periodMax) {
			setupPeriodCntItemInterface(cl, periodCurCnt, periodCurCnt, periodCurCnt);
			Stream.of("vcol_block", "vcol_floor", "vcol_flat", "col_d", "pjf_name", "col_f", "vcol_f", "col_g").map(cl::getCell).forEach(throwConsumer(c -> c.setMode(Cell.VMODE_DISPONLY)));
		} else {
			int cntMax = cl.getCellInt("pjf_period") - cl.getCellInt("col_e") + 1;
			if (cntMax > 0) {
				if (periodCurCnt >= 1 && periodCurCnt <= cntMax)
					setupPeriodCntItemInterface(cl, 1, cntMax, periodCurCnt);
				else {
					if (isInit)
						setupPeriodCntItemInterface(cl, periodCurCnt, periodCurCnt, periodCurCnt);
					else
						setupPeriodCntItemInterface(cl, 1, cntMax, 1);
				}
			} else {
				if (isInit)
					setupPeriodCntItemInterface(cl, periodCurCnt, periodCurCnt, periodCurCnt);
				else
					setupPeriodCntItemInterface(cl, 0, 0, 0);
			}
		}
	}

	public void calcUnit(CellCollection cl) throws Exception {
		cl.getCell("col_d").set(getCellString("col_c") + " " + Stream.of("vcol_block", "vcol_floor", "vcol_flat").map(cl::getString).filter(StringUtils::isNotEmpty).collect(Collectors.joining(" ")));
	}

	public static void setupPeriodCntItemInterface(CellCollection cl, int cntStart, int cntEnd, int cnt) throws Exception {
		Cell cell = cl.getCell("vcol_f");
		String cntStr = "" + cnt;
		if (Objects.equals(cell.getString(), cntStr))
			cell.set("");
		ListGetItemProperty.SelectorPick gipi = new ListGetItemProperty.SelectorPick();
		gipi.setOneColValueList(IntStream.rangeClosed(cntStart, cntEnd).mapToObj(i -> "" + i).collect(Collectors.toList()));
		cell.setItemPropertyInterface(gipi);
		cell.set(cntStr);
	}

	public static Stream<Integer> getPeriodRange(CellCollection cl) throws Exception {
		ListGetItemProperty.SelectorPick gipi = (ListGetItemProperty.SelectorPick)cl.getCell("vcol_f").getItemPropertyInterface();
		return gipi.getValueList().stream().map(r -> Integer.parseInt(r[0]));
	}

	public static void calcAmount(CellCollection cl) throws Exception {
		String[] labels = new String[] {"pjfu_d", "pjfu_e", "pjfu_f", "pjfu_g", "pjfu_h"};
		int periodStart = cl.getInt("col_e");
		int periodCnt = cl.getInt("col_f");
		cl.getCell("col_g").set(IntStream.range(0, labels.length)
				.filter(i -> periodStart > 0 && i >= periodStart - 1 && i < periodStart + periodCnt - 1)
				.mapToDouble(i -> cl.getCellDouble(labels[i])).sum());
	}

	public static void emptyCellValue(CellCollection cl, String... labels) throws Exception {
		for (String label : labels)
			cl.getCell(label).set("");
	}

}
