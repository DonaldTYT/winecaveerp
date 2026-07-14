package com.uniinformation.dynamic.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;

import com.google.gson.JsonObject;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.utils.ZkUtil.throwFunction;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.nextMonth;

public class PrintPaymentOverdueNotice extends PrintParkingTmpLocPaymentNotice {
	protected static final int TEXT_MARGIN = 5;
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
	protected PrintTemplate ptp;
	private String overdueContent;
	private boolean printDetailFlag;
	private boolean printOwnerFlag;

	public PrintPaymentOverdueNotice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
		lineHeight = 25;
		maxBottom = 0;
		fontPt = 12;
	}

	public PrintPaymentOverdueNotice() {
		this(null);
	}
	
	@Override
	protected void print() throws Exception {
		Object[] eggValues = br.getAggregateValues(fetchIndex);
		String fromMonth = getAggregateValue(eggValues, "mpy_month");
		String toMonth = getAggregateValue(eggValues, "mpy_month2");
		String unit = br.getCellString("mpy_propertyunit");

		if (overdueContent == null)
			overdueContent = ZkUtil.getFirstTableRec(br.getSelectUtil(), 
								"select co_overpmcontent from cocode where co_cocode = ?", 
								new Wherecl().appendArgument(cocode)).map(throwFunction(tr -> tr.getFieldString("co_overpmcontent"))).orElse("");

		if (totalRecord == 0) {
			nr = null;
			Button btn = (Button)Selectors.find("zkbibutton#btExtraBatchAction_1").get(0);
			printOwnerFlag = (boolean)btn.getAttribute("printOwnerFlag");
		}

		if (nr == null || !Objects.equals(nr.unit, unit)) {
			if (nr != null)
				printByNoticeRec();
			nr = new NoticeRec();
			nr.location = br.getCellString("mpy_propertyname");
			nr.unit = unit;
			nr.contact = br.getCellString("mpy_propertycontact");
			nr.owner = br.getCellString("mpy_propertyowner");
		}
		/*nr.recList.add(MapUtil.of(
			"fromMonth", fromMonth,
			"toMonth", toMonth,
			"mgtUnPaid", (double)getAggregateValue(eggValues, "vcol_mgtunpaid"),
			"resUnPaid", (double)getAggregateValue(eggValues, "vcol_resunpaid"),
			"numOfMonth", (int)(double)getAggregateValue(eggValues, "COUNT()"),
			"mgtFeePerMonth", br.getCellDouble("mpy_mgtfee"),
			"resFeePerMonth", br.getCellDouble("mpy_resfee")
		));*/
		JsonObject json = GsonUtil.objToJson(br.getTrStatObj(fetchIndex));
		JsonObject jsApp = json.getAsJsonObject("app");
		int firstRec = jsApp.get("firstRec").getAsInt();
		int count = jsApp.get("count").getAsJsonArray().get(0).getAsInt();

		List<Map<String, Object>> recList = new ArrayList<>();
		IntStream.range(firstRec, firstRec + count).forEach(i -> {
			recList.add(Stream.of("mpy_mgtfee", "mpy_resfee", "mpy_month", "pi_mgtfee", "pi_resfee").collect(Collectors.toMap(s -> s, throwFunction(s -> br.getResultTrObject(false, br.getColumnCachePositionFromHash(s), i)))));
		});
		for (Map<String, Object> m : recList) {
			if ((double)m.get("mpy_mgtfee") > 0) {
				double paid = (double)m.get("pi_mgtfee");
				if (Double.isNaN(paid))
					paid = 0;
				if (paid == 0)
					nr.recList.add(MapUtil.of("startMonth", m.get("mpy_month"), "endMonth", m.get("mpy_month"), "monthCount", 1, "type", 0, "feePerMonth", m.get("mpy_mgtfee"), "fee", m.get("mpy_mgtfee")));
			}
			if ((double)m.get("mpy_resfee") > 0) {
				double paid = (double)m.get("pi_resfee");
				if (Double.isNaN(paid))
					paid = 0;
				if (paid == 0)
					nr.recList.add(MapUtil.of("startMonth", m.get("mpy_month"), "endMonth", m.get("mpy_month"), "monthCount", 1, "type", 1, "feePerMonth", m.get("mpy_resfee"), "fee", m.get("mpy_resfee")));
			}
		}
		
		nr.fromMonth = ObjectUtils.min(nr.fromMonth, fromMonth);
		nr.toMonth = ObjectUtils.max(nr.toMonth, toMonth);

		if (++totalRecord == recordCount2) {
			printByNoticeRec();
			totalRecord = 0;
		}
	}

	private void printByNoticeRec() throws Exception {
		ptp.startPrint();
		printDetailFlag = false;
		
		nr.recList.sort((a, b) -> {
			if ((int)a.get("type") != (int)b.get("type"))
				return (int)a.get("type") - (int)b.get("type");
			else
				return ((String)a.get("startMonth")).compareTo((String)b.get("startMonth"));
		});
		List<Map<String, Object>> list = nr.recList;
		for (int i = 0; i < list.size(); ) {
			Map<String, Object> m1 = list.get(i);
			String endMonth1 = (String)m1.get("endMonth");
			int j = i + 1;
			for (; j < list.size(); j++) {
				Map<String, Object> m2 = list.get(j);
				String endMonth2 = (String)m2.get("endMonth");
				if ((int)m1.get("type") == (int)m2.get("type") && nextMonth(endMonth1, 1).equals(endMonth2)) {
					m1.put("endMonth", m2.get("endMonth"));
					m1.put("monthCount", (int)m1.get("monthCount") + 1);
					m1.put("fee", (double)m1.get("fee") + (double)m2.get("fee"));
					list.remove(j);
					break;
				}
			}
			if (j == list.size())
				i++;
		}

		lcMap = ZkUtil.getFirstTableRec(br.getSelectUtil(), "select lc_addr1 from location where lc_desc = ?", new Wherecl().appendArgument(nr.location)).map(throwFunction(tr -> 
			(Map<String, Object>)Arrays.stream(tr.getFieldNames()).collect(Collectors.toMap(s -> s, throwFunction(s -> tr.getField(s))))
		)).orElse(new HashMap<>());
		
		ptp.buildRemarkPhrase(0, overdueContent);
		ptp.buildHeightPhrase(lineHeight * 2, false);
		ptp.buildAdjustPhrase(0, (String)coMap.get("co_coname") + "\n發出日期：" + sdf.format(new Date()))[0].getTextCell(0).setAlign(PdfContentByte.ALIGN_RIGHT);
		ptp.buildPhrase(0, "逾期費用詳情如下 Overdue fee detail as below：");

		buildHeaderPhrase();
		printDetailFlag = true;
		
		double totalUnPaid = 0;
		for (Map<String, Object> m : list) {
			String fromMonth = (String)m.get("startMonth");
			String toMonth = (String)m.get("endMonth");
			int numOfMonth = (int)m.get("monthCount");
			double feePerMonth = (double)m.get("feePerMonth");
			double fee = (double)m.get("fee");
			String type = (int)m.get("type") == 1 ? "儲備金" : "管理費";
			Phrase ph = ptp.buildPhrase(2, type, fromMonth, toMonth, String.valueOf(numOfMonth), df.format(feePerMonth), df.format(fee));
			ph.setAllLines(0, -1, true);
			totalUnPaid += fee;
		}
		Phrase ph = ptp.buildPhrase(3, "總額 Total sum (MOP)", df.format(totalUnPaid));
		ph.setAllLines(0, -1, true);

		if (totalUnPaid > 0)
			ptp.endPrint(null);
	}
	
	private void buildHeaderPhrase() throws Exception {
		ptp.changeDocLineHeight(20);
		ptp.buildPhrase(1, "#", "繳費由月份\nMonth from", "繳費至月份\nMonth to", "月數\nNo of mon", "每月金額\nMonthly fee", "合計\nTotal amount");
		ptp.restoreDocLineHeight();
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return "逾期繳費催繳函";
	}
	
	protected class PrintTemplate extends PrintTemplate1 {

		@Override
		protected Cell newCell() throws Exception {
			Cell cell = new Cell(builder, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
			cellList.add(cell);
			cell.addTextItem().setFontSize(16).setText((String)coMap.get("co_coname"));
			cell.addTextItem(0, 25).setFontAndSize(9, "helv_br", chnFont).setText((String)coMap.get("co_chnname"));
			cell.addTextItem(0, 40).setFontSize(9).setText(Erpv4Config.getCoAddr(sh, cocode));
			cell.addTextItem(0, 55).setFontSize(9).setText(String.format("電話 TEL: %s       傳真 FAX: %s       准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")));
			
			//cell.addTextItem(0, 85).setText((printOwnerFlag && StringUtils.isNotBlank(nr.owner) ? nr.owner + " " : "") + "業主 台啟\n" + nr.unit + "\n" + lcMap.get("lc_addr1"));
			buildPhrase(cell, 83, -1);
			buildAdjustPhrase(4, (printOwnerFlag && StringUtils.isNotBlank(nr.owner) ? nr.owner + " " : "") + "業主 台啟\n" + nr.unit + "\n" + lcMap.get("lc_addr1"));
			
			cell.addTextItem(0, 210).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setFontSize(16).setText("逾期繳費催繳函");
			cell.addTextItem(0, 235).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setFontSize(14).setText("Overdue Payment Reminder Letter");
			buildPhrase(cell, 235 + lineHeight, 0);
			
			if (printDetailFlag)
				buildHeaderPhrase();
			return cell;
		}

		@Override
		protected Phrase buildPhrase(Cell cell, int y, int type) {
			Phrase ph = cell.addPhrase("lastPhrase", 0, y).setParentFontAndSize();
			if (type > 0 && type < 3) {
				List<Integer> widthList = Stream.of(120, 120, 110, 150, 150).collect(Collectors.toList());
				widthList.add(0, docWidth - widthList.stream().mapToInt(w -> w).sum());
				widthList.stream().forEach(cw -> ph.addTextCell(cw, type == 1 ? 50 : rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_CENTER));
				if (type == 1) {
					ph.getTextCell(0).getTextItem().setY(15);
					ph.setAllLines(0, -1, true);
				} else {
					for (int i = 0; i < ph.getGroupList().size(); i++)
						ph.getTextCell(i).setAlign(i < 4 ? PdfContentByte.ALIGN_CENTER : PdfContentByte.ALIGN_RIGHT);
				}
			} else if (type == 3) {
				ph.addTextCell(docWidth - 150, rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_RIGHT);
				ph.addTextCell(150, rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_RIGHT);
			} else if (type == 4)
				ph.addTextCell(394, lineHeight, 0, 2);
			else if (type == -1)
				ph.addTextCell(docWidth, 0, 0, 0);
			else
				ph.addTextCell(docWidth, lineHeight, 0, 2);
			return ph;
		}
	}
}
