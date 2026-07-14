package com.uniinformation.dynamic.propertymgmt;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.common.util.concurrent.AtomicDouble;
import com.kyoko.common.ReturnMsg;
import com.kyoko.utils.UrlUtils;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.propertymgmt.BiResultProjectFee;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.BatchBuildPrintHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.utils.ZkUtil.throwConsumer;
import static com.uniinformation.utils.ZkUtil.throwIntConsumer;

public class PrintProjectPaymentNotice extends BatchBuildPrintHandler {
	protected static final int TEXT_MARGIN = 2;
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
   	protected static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
   	protected static DecimalFormat df = new DecimalFormat("$#,##0.00");

   	protected PrintTemplate ptp;
	protected String cocode;
    protected Map<String, Object> coMap;
    protected Map<String, Object> lcMap;
	
	
	public PrintProjectPaymentNotice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
		docWidth = 760;
		docHeight = 1100;
		docWidthPx = ChnftrParser.dpi100ToPx(docWidth);
		docHeightPx = ChnftrParser.dpi100ToPx(docHeight);
		rowHeight = 30;
		lineHeight = 20;
		maxBottom = 90;
		chnFont = "mshei";
		fontPt = 10;
		printMode = PRINT_MODE.DOWNLOAD;
		needDownloadRename = true;
	}

	public PrintProjectPaymentNotice() {
		this(null);
	}

	@Override
	protected ReturnMsg initBuilder() {
        cocode = Erpv4Config.getDefaultCoCode(sh);
        coMap = Erpv4Config.getCoFieldMap(sh, cocode);
		return super.initBuilder();
	}

	@Override
	protected void print() throws Exception {
		ptp.startPrint();

		lcMap = Erpv4Config.getLcFieldMap(sh, br.getCellString("upf_location"));
		ptp.buildPhrase(2, "未繳交之費用 Unpaid fees：").getTextCell(0).setB();
		ptp.buildPhrase(2, "維修分攤費", br.getCellString("upf_unit"));
		
		List<CellCollection> list = ZkUtil.getTableRecStream(br.getSelectUtil(), "select upf_projectno pjno, upf_allocamt alcamt, upf_period1amt col_d, upf_period2amt col_e, upf_period3amt col_f, upf_period4amt col_g, upf_period5amt col_h, "
										+ "upf_projectname pjname, upf_projectstartmonth start, upf_projectendmonth end, upf_projectperiodcnt totperiodcnt, upf_totpayamt totpaidamt, "
										+ "COALESCE(item.col_e, 0) periodstart, COALESCE(item.col_f, 0) periodcnt, COALESCE(item.col_g, 0) paidamt "
									+ "from unitprojectfee "
									+ "left join payprojectitem item on item.col_b = upf_location and item.col_c = upf_projectno and item.col_d = upf_unit "
									+ "where upf_unit = ? order by start, pjno, periodstart", 
						new Wherecl().appendArgument(br.getCellString("upf_unit"))).collect(Collectors.toList());
		AtomicDouble totalUnpaidAmt = new AtomicDouble();
		list.stream().map(c -> c.getString("pjno")).distinct().forEach(throwConsumer(pjno -> {
			List<CellCollection> list1 = list.stream().filter(c -> Objects.equals(c.getString("pjno"), pjno)).collect(Collectors.toList());
			CellCollection c1 = list1.get(0);
			ptp.buildPhrase(2, "", "繳交月份：" + c1.getString("start") + " 至 " + c1.getString("end"));
			Double[] pas = BiResultProjectFee.periodColumnLabelList.stream().limit(c1.getInt("totperiodcnt")).map(l -> c1.getDouble(l)).toArray(Double[]::new);
			Set<Integer> periodList = IntStream.range(0, pas.length).mapToObj(i -> i).collect(Collectors.toSet());
			for (CellCollection c2 : list1) {
				for (int i = c2.getInt("periodstart"); i < c2.getInt("periodstart") + c2.getInt("periodcnt"); i++)
					periodList.remove(i - 1);
			}
			IntStream.range(0, pas.length).filter(i -> periodList.contains(i)).forEach(throwIntConsumer(i -> {
				ptp.buildAdjustPhrase(2, "", c1.getString("pjname"), "第" + (i + 1) + "期，共" + c1.getInt("totperiodcnt") + "期", df.format(pas[i]));
			}));
			double unpaidAmt = c1.getDouble("alcamt") - c1.getDouble("totpaidamt");
			totalUnpaidAmt.addAndGet(unpaidAmt > 0 ? unpaidAmt : 0);
		}));
		Phrase ph = ptp.buildPhrase(2, "", "", "合共 (MOP)：", df.format(totalUnpaidAmt));
		ph.getTextCell(2).setAlign(PdfContentByte.ALIGN_RIGHT);
		ph.getTextCell(3).addMultiHorLineItem(5, 30, 32);

		ptp.buildPhrase(2, "備註 Remark:");
		Stream.of((String)coMap.get("co_payment"), (String)lcMap.get("lc_payment")).filter(StringUtils::isNotBlank).forEach(throwConsumer(s -> {
			ptp.buildRemarkPhrase(0, s);
			ptp.buildHeightPhrase(5, false);
		}));

		ptp.endPrint(null);
	}

	protected class PrintTemplate extends PrintTemplate1 {
		
		protected Map<String, Object> getParams() {
			return MapUtil.of("title", "維修分攤繳費通知單",
					"subtitle", "Maintenance Sharing Payment Notice",
					"contact", br.getCellString("upf_propertycontact"),
					"unit", br.getCellString("upf_unit"),
					"headerstrings", new String[] { "#", "繳費單位 / 項目名稱\nProperty Unit / Project Name", "期數\nPeriod Number", "每期金額\nPeriod Amount" });
		}

		@Override
		protected Cell newCell() throws Exception {
			Map<String, Object> m = getParams();
			Cell cell = new Cell(builder, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
			cellList.add(cell);
			cell.addTextItem().setFontSize(16).setText((String)coMap.get("co_coname"));
			cell.addTextItem(0, 25).setFontAndSize(9, "helv_br", chnFont).setText((String)coMap.get("co_chnname"));
			cell.addTextItem(0, 40).setFontSize(9).setText(Erpv4Config.getCoAddr(sh, cocode));
			cell.addTextItem(0, 55).setFontSize(9).setText(String.format("電話 TEL: %s       傳真 FAX: %s       准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")));
	
			cell.addTextItem(0, 85).setFontSize(12).setB().setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText((String)m.get("title"));
			cell.addTextItem(0, 105).setFontSize(10).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText((String)m.get("subtitle"));
	
			cell.addTextItem(0, 135).setFontSize(10).setText("聯絡人Contact: " + m.get("contact"));
			cell.addTextItem(380, 135).setFontSize(10).setText("發單日期 Issue Date: " + sdf.format(new Date()));
	
			if ((int)lcMap.get("lc_epayment") > 0) {
				String s = UrlUtils.buildURLWithParams("https://www.erpv4.com/qrdecode", "agid", "pgmt0001", "punit", (String)m.get("unit"));
				cell.addQRcodeItem(600, 0).setSize(150, 150).setText(s);
			}
	
			buildPhrase(cell, 160, 1, (String[])m.get("headerstrings"));
			return cell;
		}

		@Override
		protected Phrase buildPhrase(Cell cell, int y, int type) {
			Phrase ph = cell.addPhrase("lastPhrase", 0, y).setParentFontAndSize();
			if (type > 0) {
				List<Integer> widthList = Stream.of(100, 130, 130).collect(Collectors.toList());
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
						ph.getTextCell(i).setAlign(i < 3 ? PdfContentByte.ALIGN_LEFT : PdfContentByte.ALIGN_RIGHT);
				}
			} else
				ph.addTextCell(docWidth, lineHeight, 0, 0);
			return ph;
		}
	}
}
