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
import com.uniinformation.utils.ListGetItemProperty;
import com.uniinformation.utils.MapUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.utils.BiUtil.throwConsumer;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.getMonthRange;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.getMonthCount;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.nextMonth;

public class BiResultParkingTmpLocPayment extends BiResultPropertyMgmt {

	public BiResultParkingTmpLocPayment(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
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
			BiUtil.getTableRecStream(su, "select item.col_c c, item.col_d d, item.col_e e, item.col_f f, contract.col_k k from payparkingitem item "
										+ "left join parkingtlcontract contract on contract.col_a = item.col_b and contract.col_b = item.col_c and contract.col_d = item.col_d "
										+ "where item.col_a = ? and item.col_b = ?",
										new Wherecl().appendArgument(voucherNo).appendArgument(locDesc)).forEach(throwConsumer(c -> {
				String parkingLoc = c.getString("c");
				String conStartMonth = c.getString("d");
				String startMonth = c.getString("e");
				int monthCount = c.getInt("f");
				double monthlyAmt = c.getDouble("k");
				Set<String> monthRange1 = getMonthRange(startMonth, monthCount - 1);
				Set<String> monthRange2 = BiUtil.getTableRecStream(su, "select col_e from payparkingitem2 where col_a = ? and col_b = ? and col_c = ?",
										new Wherecl().appendArgument(voucherNo).appendArgument(locDesc).appendArgument(parkingLoc))
									.map(c1 -> c1.getString("col_e")).collect(Collectors.toCollection(TreeSet::new));
				UniLog.log1("monthRange1:%s, monthRange2:%s", monthRange1, monthRange2);
				monthRange2.stream().filter(item -> !monthRange1.contains(item)).forEach(throwConsumer(month -> {
					UniLog.log1("delete from payparkingitem2 %s", month);
					su.executeUpdate("delete from payparkingitem2 where col_a = ? and col_b = ? and col_c = ? and col_e = ?", 
							new Wherecl().appendArgument(voucherNo).appendArgument(locDesc).appendArgument(parkingLoc).appendArgument(month));
				}));
				monthRange1.stream().filter(item -> !monthRange2.contains(item)).forEach(throwConsumer(month -> {
					BiUtil.executeInsertIntoSql(su, "payparkingitem2", Arrays.asList("col_a", "col_b", "col_c", "col_d", "col_e", "col_f"), 
							new Wherecl().appendArgument(voucherNo).appendArgument(locDesc).appendArgument(parkingLoc).appendArgument(conStartMonth).appendArgument(month).appendArgument(monthlyAmt));
				}));
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
			su.executeUpdate("delete from payparkingitem2 where col_a = ?", new Wherecl().appendArgument(col.getString("col_b")));
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.toString(), true);
		}
		return ReturnMsg.defaultOk;
	}

	public void syncPaymentFromPayItem() throws Exception {
		UniLog.log("syncPaymentFromPayItem");
		Vector<BiCellCollection> list = getSubLinkResult("propertymgmt.PayParkingItem");
		String owner = list.stream().map(c -> c.getString("pcol_h")).distinct().limit(2).count() == 1 ? list.get(0).getString("pcol_h") : null;
		if (owner != null) {
			getCell("col_d").set(owner);
			Map<String, String> m = MapUtil.of("col_e", "pcol_l", "col_i", "pcol_c", "col_j", "pcol_d", "col_k", "pcol_e");
			m.entrySet().forEach(throwConsumer(e -> {
				getCell(e.getKey()).set(list.stream().map(c -> c.getString(e.getValue())).distinct().limit(2).count() == 1 ? list.get(0).getString(e.getValue()) : "");
			}));
		} else
			Stream.of("col_d", "col_e", "col_i", "col_j", "col_k").forEach(throwConsumer(k -> getCell(k).set("")));
		getCell("col_p").set((int)list.stream().map(c -> c.getString("ccol_c")).distinct().count());
		getCell("col_f").set(list.stream().mapToDouble(c -> c.getDouble("col_g")).sum());
	}

	public void calcPaidPeriodAndAmount(CellCollection cl, boolean isInit) throws Exception {
		String voucherNo = getCellString("col_b");
		String parkingLoc = cl.getString("col_c");
		String conStartMonth = cl.getString("col_d");
		String conStartEnd = cl.getString("ccol_e");
		CellCollection[] arr = BiUtil.getTableRecStream(getSelectUtil(), "select col_a, col_e, col_f, col_g from payparkingitem where col_b = ? and col_c = ? and col_d = ? order by col_e, col_f", 
									new Wherecl().appendArgument(getCellString("col_c")).appendArgument(parkingLoc).appendArgument(conStartMonth)).toArray(CellCollection[]::new);
		CellCollection cellCur = Arrays.stream(arr).filter(c -> Objects.equals(c.getString("col_a"), voucherNo)).findFirst().orElse(null);
		String periodCur = cellCur != null ? cellCur.getString("col_e") : null;
		int periodCurCnt = cellCur != null ? cellCur.getInt("col_f") : 0; 
		String periodMaxStart = "", periodMaxStart2 = "";
		String periodMaxEnd = "", periodMaxEnd2 = "";
		double totAmt = 0, totAmt2 = 0;
		for (CellCollection cc : arr) {
			String periodStart = cc.getString("col_e");
			int periodCnt = cc.getInt("col_f");
			double amt = cc.getDouble("col_g");
			if (periodCur == null || periodStart.compareTo(periodCur) < 0) {
				periodMaxStart = periodStart;
				periodMaxEnd = nextMonth(periodStart, periodCnt - 1, "");
				totAmt += amt;
			}
			periodMaxStart2 = periodStart;
			periodMaxEnd2 = nextMonth(periodStart, periodCnt - 1, "");
			totAmt2 += amt;
		}
		cl.getCell("vcol_paidendmonth").set(periodMaxEnd);
		cl.getCell("vcol_paidamt").set(totAmt);
		if (!isInit)
			cl.getCell("col_e").set(periodCur != null ? periodCur : nextMonth(periodMaxEnd2, 1, conStartMonth));
		if (periodCur != null && periodCur.compareTo(periodMaxStart2) < 0) {
			setupPeriodCntItemInterface(cl, periodCurCnt, periodCurCnt, periodCurCnt);
			Stream.of("vcol_block", "vcol_floor", "vcol_flat", "col_c", "col_d", "col_f", "vcol_f", "col_g").map(cl::getCell).forEach(throwConsumer(c -> c.setMode(Cell.VMODE_DISPONLY)));
		} else {
			int cntMax = (int)getMonthCount(cl.getString("col_e"), conStartEnd); 
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
	
	public static Stream<Integer> getPeriodRange(CellCollection cl) throws Exception {
		ListGetItemProperty.SelectorPick gipi = (ListGetItemProperty.SelectorPick)cl.getCell("vcol_f").getItemPropertyInterface();
		return gipi.getValueList().stream().map(r -> Integer.parseInt(r[0]));
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

	public static void calcAmount(CellCollection cl) throws Exception {
		cl.getCell("col_g").set(cl.getDouble("ccol_k") * cl.getInt("col_f"));
	}

}
