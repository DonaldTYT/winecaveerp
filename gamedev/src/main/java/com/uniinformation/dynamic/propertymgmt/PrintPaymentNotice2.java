package com.uniinformation.dynamic.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.utils.ZkUtil.throwConsumer;

public class PrintPaymentNotice2 extends PrintParkingTmpLocPaymentNotice {

	public PrintPaymentNotice2(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
	}

	public PrintPaymentNotice2() {
		this(null);
	}

	@Override
	protected void print() throws Exception {
		Object[] eggValues = br.getAggregateValues(fetchIndex);
		String fromMonth = getAggregateValue(eggValues, "mpy_month");
		String toMonth = getAggregateValue(eggValues, "mpy_month2");
		String unit = br.getCellString("mpy_propertyunit");

		if (totalRecord == 0)
			nr = null;

		if (nr == null || !Objects.equals(nr.unit, unit)) {
			if (nr != null)
				printByNoticeRec();
			nr = new NoticeRec();
			nr.location = br.getCellString("mpy_propertyname");
			nr.unit = unit;
			nr.contact = br.getCellString("mpy_propertycontact");
		}
		nr.recList.add(MapUtil.of(
			"fromMonth", fromMonth,
			"toMonth", toMonth,
			"mgtUnPaid", (double)getAggregateValue(eggValues, "vcol_mgtunpaid"),
			"resUnPaid", (double)getAggregateValue(eggValues, "vcol_resunpaid"),
			"numOfMonth", (int)(double)getAggregateValue(eggValues, "COUNT()"),
			"mgtFeePerMonth", br.getCellDouble("mpy_mgtfee"),
			"resFeePerMonth", br.getCellDouble("mpy_resfee")
		));
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
		
   		List<Map<String, Object>> list = new ArrayList<>();
		ZkUtil.getTableRecStream(br.getSelectUtil(), 
				"select mpy_mgtfee mgtfee, mpy_resfee resfee, mpy_month month from monthpayment join contract on contract.col_a = mpy_propertyname and contract.col_h = 'Y' and STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d') >= contract.col_c and LAST_DAY(STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d')) <= contract.col_d where mpy_propertyunit = ? and mpy_month < ? order by month",
				new Wherecl().appendArgument(nr.unit).appendArgument(nr.fromMonth)).forEach(c -> {
   			String month = c.getString("month");
    		double d = c.getDouble("mgtfee");
   			if (!Double.isNaN(d)) 
   				list.add(MapUtil.of("startMonth", month, "endMonth", month, "monthCount", 1, "type", "管理費", "fee", d));
   			d = c.getDouble("resfee");
   			if (!Double.isNaN(d)) 
   				list.add(MapUtil.of("startMonth", month, "endMonth", month, "monthCount", 1, "type", "儲備金", "fee", d));
		});
		ZkUtil.getTableRecStream(br.getSelectUtil(),
				"select payitem.col_e mgtfee, payitem.col_f resfee, mpy_month month from monthpayment join payitem on payitem.col_c = mpy_propertyunit and payitem.col_d = mpy_month join contract on contract.col_a = mpy_propertyname and contract.col_h = 'Y' and STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d') >= contract.col_c and LAST_DAY(STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d')) <= contract.col_d where mpy_propertyunit = ? and mpy_month < ?",
				new Wherecl().appendArgument(nr.unit).appendArgument(nr.fromMonth)).forEach(c -> {
   			String month = c.getString("month");
   			final double d1 = c.getDouble("mgtfee");
   			if (!Double.isNaN(d1))
   				list.stream().filter(m -> Objects.equals((String)m.get("startMonth"), month) && Objects.equals((String)m.get("type"), "管理費")).findFirst()
  						.ifPresent(m -> m.put("fee", (Double)m.get("fee") - d1));
   			final double d2 = c.getDouble("resfee");
   			if (!Double.isNaN(d2))
   				list.stream().filter(m -> Objects.equals((String)m.get("startMonth"), month) && Objects.equals((String)m.get("type"), "儲備金")).findFirst()
 						.ifPresent(m -> m.put("fee", (Double)m.get("fee") - d1));
		});
   		list.removeIf(m -> (Double)m.get("fee") <= 0);
   		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
   		for (int i = 0; i < list.size(); ) {
   			Map<String, Object> m1 = list.get(i);
   			Date date1 = sdf1.parse((String)m1.get("endMonth"));
   			int j = i + 1;
   			for (; j < list.size(); j++) {
   				Map<String, Object> m2 = list.get(j);
   				Date date2 = sdf1.parse((String)m2.get("endMonth"));
   				if (m1.get("type").equals(m2.get("type")) && date1.compareTo(DateUtil.prevMonthStart(date2)) == 0) {
   					m1.put("endMonth", m2.get("endMonth"));
   					m1.put("monthCount", (Integer)m1.get("monthCount") + 1);
   					m1.put("fee", (Double)m1.get("fee") + (Double)m2.get("fee"));
   					list.remove(j);
   					break;
   				}
   			}
   			if (j == list.size())
   				i++;
   		}
   		double forwardMgtUnPaid = list.stream().filter(m -> m.get("type").equals("管理費")).mapToDouble(m -> (Double)m.get("fee")).sum();
   		double forwardResUnPaid = list.stream().filter(m -> m.get("type").equals("儲備金")).mapToDouble(m -> (Double)m.get("fee")).sum();

		ptp.buildPhrase(2, "未繳交之費用 Unpaid fees：").getTextCell(0).setB();
		for (Map<String, Object> m : list)
			ptp.buildAdjustPhrase(2, (String)m.get("type"), nr.unit, (String)m.get("startMonth"), (String)m.get("endMonth"), String.valueOf(m.get("monthCount")), "", df.format(m.get("fee")));
		ptp.buildPhrase(2, "", "", "", "", "", "合共 (MOP)：", df.format(forwardMgtUnPaid + forwardResUnPaid)).getTextCell(6).addMultiHorLineItem(5, 30, 32);

		ptp.buildPhrase(2, "本期之費用 Fees for this period:").getTextCell(0).setB();
		double totalUnPaid = 0;
		for (Map<String, Object> m : nr.recList) {
			String fromMonth = (String)m.get("fromMonth");
			String toMonth = (String)m.get("toMonth");
			double mgtUnPaid = (double)m.get("mgtUnPaid");
			double resUnPaid = (double)m.get("resUnPaid");
			int numOfMonth = (int)m.get("numOfMonth");
			double mgtFeePerMonth = (double)m.get("mgtFeePerMonth");
			double resFeePerMonth = (double)m.get("resFeePerMonth");
			if (mgtFeePerMonth > 0) {
				double totalMgt = mgtFeePerMonth * numOfMonth;
				double paidMgt = totalMgt - mgtUnPaid;
				ptp.buildAdjustPhrase(2, "管理費", nr.unit, 
						fromMonth, toMonth, 
						String.valueOf((int)numOfMonth),
						df.format(mgtFeePerMonth),
						df.format(totalMgt));
				if (paidMgt != 0)
					ptp.buildPhrase(2, "", "", "", "", "", "已繳", df.format(paidMgt));
				totalUnPaid += mgtUnPaid;
			}
			if (resFeePerMonth > 0) {
				double totalRes = resFeePerMonth * numOfMonth;
				double paidRes = totalRes - resUnPaid;
				ptp.buildAdjustPhrase(2, "儲備金", nr.unit, 
						fromMonth, toMonth, 
						String.valueOf((int)numOfMonth),
						df.format(resFeePerMonth),
						df.format(totalRes));
				if (paidRes != 0)
					ptp.buildPhrase(2, "", "", "", "", "", "已繳", df.format(paidRes));
				totalUnPaid += resUnPaid;
			}
		}
		ptp.buildPhrase(2, "", "", "", "", "", "合共 (MOP)：", df.format(totalUnPaid)).getTextCell(6).addMultiHorLineItem(5, 30, 32);
		ptp.buildPhrase(2, "", "", "", "", "", "總額 (MOP)：", df.format(totalUnPaid + forwardMgtUnPaid + forwardResUnPaid)).getTextCell(6).addMultiHorLineItem(30, 32);

		ptp.buildPhrase(2, "備註 Remark:").getTextCell(0).setB();
		Stream.of((String)coMap.get("co_payment"), (String)lcMap.get("lc_payment")).filter(StringUtils::isNotBlank).forEach(throwConsumer(s -> {
			ptp.buildRemarkPhrase(0, s);
			ptp.buildHeightPhrase(5, false);
		}));

		ptp.endPrint(null);
	}
	
	protected class PrintTemplate extends PrintParkingTmpLocPaymentNotice.PrintTemplate {
		
		@Override
		protected Map<String, Object> getParams() {
			return MapUtil.of("title", "繳費通知單 Payment Notice",
					"subtitle", String.format("繳費月份 Payment Month %s 至 %s", nr.fromMonth, nr.toMonth),
					"contact", nr.contact,
					"unit", nr.unit,
					"headerstrings", new String[] { "#", "繳費單位\nProperty Unit", "繳費由月份\nMonth from", "繳費至月份\nMonth to", "月數\nNo of mon", "每月金額\nMonthly fee", "合計\nTotal Amount" });
		}
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return "Payment Notice";
	}
}
