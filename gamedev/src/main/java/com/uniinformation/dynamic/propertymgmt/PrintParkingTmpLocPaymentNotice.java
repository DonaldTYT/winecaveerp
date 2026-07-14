package com.uniinformation.dynamic.propertymgmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonObject;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.bicore.propertymgmt.BiResultPropertyMgmt;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.nextMonth;
import static com.uniinformation.utils.ZkUtil.throwConsumer;
import static com.uniinformation.utils.ZkUtil.throwFunction;

public class PrintParkingTmpLocPaymentNotice extends PrintProjectPaymentNotice {
	protected NoticeRec nr;
	protected int totalRecord;

	public PrintParkingTmpLocPaymentNotice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
	}

	public PrintParkingTmpLocPaymentNotice() {
		this(null);
	}

	@Override
	protected void print() throws Exception {
		Object[] eggValues = br.getAggregateValues(fetchIndex);
		String fromMonth = getAggregateValue(eggValues, "mpt_month");
		String toMonth = getAggregateValue(eggValues, "mpt_month2");
		//int numOfMonth = (int)(double)getAggregateValue(eggValues, "COUNT()");
		//double unpaid = getAggregateValue(eggValues, "vcol_unpaid");
		String unit = br.getCellString("mpt_unit");
		if (totalRecord == 0)
			nr = null;

		if (nr == null || !Objects.equals(nr.unit, unit)) {
			if (nr != null)
				printByNoticeRec();
			nr = new NoticeRec();
			nr.location = br.getCellString("mpt_building");
			nr.unit = unit;
			nr.contact = br.getCellString("mpt_propertycontact");
		}
		JsonObject json = GsonUtil.objToJson(br.getTrStatObj(fetchIndex));
		JsonObject jsApp = json.getAsJsonObject("app");
		int firstRec = jsApp.get("firstRec").getAsInt();
		int count = jsApp.get("count").getAsJsonArray().get(0).getAsInt();
		List<Map<String, Object>> recList = nr.recList;
		IntStream.range(firstRec, firstRec + count).forEach(i -> {
			recList.add(Stream.of("mpt_tlcode", "mpt_month").collect(Collectors.toMap(s -> s, throwFunction(s -> br.getResultTrObject(false, br.getColumnCachePositionFromHash(s), i)))));
		});

		nr.fromMonth = ObjectUtils.min(nr.fromMonth, fromMonth);
		nr.toMonth = ObjectUtils.max(nr.toMonth, toMonth);
		

		if (++totalRecord == recordCount2) {
			printByNoticeRec();
			totalRecord = 0;
		}
	}
	
	private void printByNoticeRec() throws Exception {
		ptp.startPrint();

		lcMap = Erpv4Config.getLcFieldMap(sh, nr.location);
		
		ptp.buildPhrase(2, "未繳交之費用 Unpaid fees：").getTextCell(0).setB();
		List<CellCollection> list = ZkUtil.getTableRecStream(br.getSelectUtil(), "select *, mpt_month endmonth, mpt_paidamount paidamt, 1 monthcnt from monthparkingtl where mpt_unit = ? and mpt_month < ? and mpt_paidamount < mpt_monthlyfee order by mpt_tlcode, mpt_constartmonth, mpt_month", 
																				new Wherecl().appendArgument(nr.unit).appendArgument(nr.fromMonth)).collect(Collectors.toList());
		List<Pair<String, String>> list1 = list.stream().map(c -> Pair.of(c.getString("mpt_tlcode"), c.getString("mpt_constartmonth"))).distinct().collect(Collectors.toList());
		double totalUnpaid1 = 0.0;
		for (Pair<String, String> p : list1) {
			String tlcode = p.getLeft();
			String constartmonth = p.getRight();
			List<CellCollection> list2 = list.stream().filter(c -> c.getString("mpt_tlcode").equals(tlcode) && c.getString("mpt_constartmonth").equals(constartmonth) ).collect(Collectors.toList());
			CellCollection c = list2.get(0);
			ptp.buildPhrase(2, "暫放費", String.format("位置 (Location) : %s , 合同 (Contract) : %s 至 %s , %s", tlcode, constartmonth, c.getString("mpt_conendmonth"), c.getString("mpt_licenseplate")));
			for (int i = 0; i < list2.size() - 1; ) {
				CellCollection c1 = list2.get(i);
				CellCollection c2 = list2.get(i + 1);
				if (nextMonth(c1.getString("endmonth"), 1).equals(c2.getString("mpt_month"))) {
					c1.getCell("endmonth").set(c2.getString("mpt_month"));
					c1.getCell("paidamt").set(c1.getDouble("paidamt") + c2.getDouble("paidamt"));
					c1.getCell("monthcnt").set(c1.getInt("monthcnt") + 1);
					list2.remove(i + 1);
				} else
					i++;
			}
			for (CellCollection cc : list2) {
				ptp.buildAdjustPhrase(2, "", nr.unit, cc.getString("mpt_month"), cc.getString("endmonth"), 
													cc.getString("monthcnt"), df.format(cc.getDouble("mpt_monthlyfee")), df.format(cc.getDouble("mpt_monthlyfee") * cc.getInt("monthcnt")));
				if (cc.getDouble("paidamt") > 0)
					ptp.buildPhrase(2, "", "", "", "", "", "已繳", df.format(cc.getDouble("paidamt")));
			}
			totalUnpaid1 += list2.stream().mapToDouble(x -> x.getDouble("mpt_monthlyfee") * x.getInt("monthcnt") - x.getDouble("paidamt")).sum();
		}
		ptp.buildPhrase(2, "", "", "", "", "", "合共 (MOP)：", df.format(totalUnpaid1)).getTextCell(6).addMultiHorLineItem(5, 30, 32);


		ptp.buildPhrase(2, "本期之費用 Fees for this period：").getTextCell(0).setB();
		list = ZkUtil.getTableRecStream(br.getSelectUtil(), "select *, IF(mpt_monthlyfee > mpt_paidamount, '未繳', '已繳') AS mpt_status, mpt_month endmonth, mpt_paidamount paidamt, 1 monthcnt from monthparkingtl where mpt_unit = ? and mpt_month between ? and ? order by mpt_tlcode, mpt_constartmonth, mpt_month", 
																				new Wherecl().appendArgument(nr.unit).appendArgument(nr.fromMonth).appendArgument(nr.toMonth)).collect(Collectors.toList());
		list1 = list.stream().map(c -> Pair.of(c.getString("mpt_tlcode"), c.getString("mpt_constartmonth"))).distinct().collect(Collectors.toList());
		double totalUnpaid2 = 0.0;
		for (Pair<String, String> p : list1) {
			String tlcode = p.getLeft();
			String constartmonth = p.getRight();
			List<CellCollection> list2 = list.stream().filter(c -> 
				c.getString("mpt_tlcode").equals(tlcode) && c.getString("mpt_constartmonth").equals(constartmonth) 
				&& nr.recList.stream().anyMatch(x -> x.get("mpt_tlcode").equals(tlcode) && c.getString("mpt_month").equals(x.get("mpt_month")))
			).collect(Collectors.toList());
			if (!list2.isEmpty()) {
				CellCollection c = list2.get(0);
				ptp.buildPhrase(2, "暫放費", String.format("位置 (Location) : %s , 合同 (Contract) : %s 至 %s , %s", tlcode, constartmonth, c.getString("mpt_conendmonth"), c.getString("mpt_licenseplate")));
				for (int i = 0; i < list2.size() - 1; ) {
					CellCollection c1 = list2.get(i);
					CellCollection c2 = list2.get(i + 1);
					if (nextMonth(c1.getString("endmonth"), 1).equals(c2.getString("mpt_month")) && c1.getString("mpt_status").equals(c2.getString("mpt_status"))) {
						c1.getCell("endmonth").set(c2.getString("mpt_month"));
						c1.getCell("paidamt").set(c1.getDouble("paidamt") + c2.getDouble("paidamt"));
						c1.getCell("monthcnt").set(c1.getInt("monthcnt") + 1);
						list2.remove(i + 1);
					} else
						i++;
				}
				for (CellCollection cc : list2) {
					ptp.buildAdjustPhrase(2, "", nr.unit, cc.getString("mpt_month"), cc.getString("endmonth"), 
														cc.getString("monthcnt"), df.format(cc.getDouble("mpt_monthlyfee")), df.format(cc.getDouble("mpt_monthlyfee") * cc.getInt("monthcnt")));
					if (cc.getDouble("paidamt") > 0)
						ptp.buildPhrase(2, "", "", "", "", "", "已繳", df.format(cc.getDouble("paidamt")));
				}
				totalUnpaid2 += list2.stream().mapToDouble(x -> x.getDouble("mpt_monthlyfee") * x.getInt("monthcnt") - x.getDouble("paidamt")).sum();
			}
		}
		ptp.buildPhrase(2, "", "", "", "", "", "合共 (MOP)：", df.format(totalUnpaid2)).getTextCell(6).addMultiHorLineItem(5, 30, 32);
		ptp.buildPhrase(2, "", "", "", "", "", "總額 (MOP)：", df.format(totalUnpaid1 + totalUnpaid2)).getTextCell(6).addMultiHorLineItem(30, 32);
		
		ptp.buildPhrase(2, "備註 Remark:").getTextCell(0).setB();
		Stream.of((String)coMap.get("co_payment"), (String)lcMap.get("lc_payment")).filter(StringUtils::isNotBlank).forEach(throwConsumer(s -> {
			ptp.buildRemarkPhrase(0, s);
			ptp.buildHeightPhrase(5, false);
		}));

		ptp.endPrint(null);
	}

	protected <T> T getAggregateValue(Object[] eggValues, String p_col) throws Exception {
		return BiResultPropertyMgmt.getAggregateValue(br, eggValues, p_col);
	}
	
	protected static class NoticeRec {
		String location, unit, contact, owner;
		String fromMonth = "9999-99", toMonth;
		List<Map<String, Object>> recList = new ArrayList<>();
	}

	protected class PrintTemplate extends PrintProjectPaymentNotice.PrintTemplate {
		
		@Override
		protected Map<String, Object> getParams() {
			String fromMonth = nr.fromMonth;
			String toMonth = nr.toMonth;
			if (br.getCustomCondition() != null) {
        		String condition = StringUtils.defaultString(br.getCustomCondition().toString());
        		Matcher m = Pattern.compile("mpt_month between '([\\d-]+)' and '([\\d-]+)'").matcher(condition);
        		if (m.find()) {
        			fromMonth = m.group(1);
        			toMonth = m.group(2);
        		}
			}
			return MapUtil.of("title", "暫放費繳費通知單 Temporary Fee Payment Notice",
					"subtitle", String.format("繳費月份 Payment Month %s 至 %s", fromMonth, toMonth),
					"contact", nr.contact,
					"unit", nr.unit,
					"headerstrings", new String[] { "#", "繳費單位\nProperty Unit", "繳費由月份\nMonth from", "繳費至月份\nMonth to", "月數\nNo of mon.", "每月金額\nMonthly fee", "合計\nTotal Amount" });
		}

		@Override
		protected Phrase buildPhrase(Cell cell, int y, int type) {
			Phrase ph = cell.addPhrase("lastPhrase", 0, y).setParentFontAndSize();
			if (type > 0) {
				List<Integer> widthList = Stream.of(60, 90, 90, 80, 110, 120).collect(Collectors.toList());
				widthList.add(1, docWidth - widthList.stream().mapToInt(w -> w).sum());
				widthList.stream().forEach(cw -> {
					ph.addTextCell(cw, type == 1 ? 50 : rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_CENTER);
				});
				if (type == 1) {
					ph.getTextCell(0).getTextItem().setY(15);
					ph.setLeftLines(1, -1, true);
					ph.setTopLines(0, -1, true);
					ph.setBottomLines(0, -1, true);
				} else {
					for (int i = 0; i < ph.getGroupList().size(); i++)
						ph.getTextCell(i).setAlign(i < 4 ? PdfContentByte.ALIGN_LEFT : i == 4 ? PdfContentByte.ALIGN_CENTER : PdfContentByte.ALIGN_RIGHT);
				}
			} else
				ph.addTextCell(docWidth, lineHeight, 0, 0);
			return ph;
		}
	}
}
