package com.uniinformation.dynamic.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.kyoko.utils.MoneyToChinese;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.utils.ZkUtil.throwFunction;

public class PrintRenovationDeposit extends PrintProjectPaymentInvoice {
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
	protected PrintTemplate ptp;
	private String depositContent;

	public PrintRenovationDeposit(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
		lineHeight = 25;
		maxBottom = 0;
		fontPt = 12;
	}

	public PrintRenovationDeposit() {
		this(null);
	}

	@Override
	protected void print() throws Exception {
		if (depositContent == null)
			depositContent = ZkUtil.getFirstTableRec(br.getSelectUtil(), 
								"select co_rendepcontent from cocode where co_cocode = ?", 
								new Wherecl().appendArgument(cocode)).map(throwFunction(tr -> tr.getFieldString("co_rendepcontent"))).orElse("");
		ptp.startPrint();
		ptp.buildPhrase(1, "申請日期：", sdf.format(br.getCellDate("col_l")), "", "", "按金單編號：", br.getCellString("col_a")).getTextCell(4).moveX(-17);
		ptp.buildAdjustPhrase(1, "申請單位：", br.getCellString("col_c"), "業權人：", br.getCellString("pcol_h"), "聯絡電話：", br.getCellString("pcol_l"));
		ptp.buildPhrase(1, "施工日期：", sdf.format(br.getCellDate("col_d")) + "至" + sdf.format(br.getCellDate("col_e")));
		ptp.buildAdjustPhrase(1, "施工公司：", br.getCellString("col_g"), "聯絡人：", br.getCellString("col_i"), "電話號碼：", br.getCellString("col_j"));
		ptp.buildPhrase(1, "裝修按金：", String.format("%s (%s)", MoneyToChinese.convertMoneyToWord(br.getCellDouble("col_f")), df.format(br.getCellDouble("col_f"))), 
													"", "", "支付方式：", br.getCellString("col_n")).addHeight(rowHeight);

		ptp.buildRemarkPhrase(0, depositContent);
		ptp.buildHeightPhrase(rowHeight, false);
		ptp.buildAdjustPhrase(0, br.getCellString("col_g") + " -- 簽署/蓋印\n\n\n\n"
												+ (String)coMap.get("co_coname") + " -- 簽署/蓋印\n"
												+ sdf.format(br.getCellDate("col_l")))[0]
			.getTextCell(0).addTextItem().setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText(br.getCellString("col_c") + " -- 簽署");
		ptp.endPrint(null);
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
			cell.addLineItem(0, 75).setRect(docWidth);
			cell.addTextItem(0, 80).setFontSize(16).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText("工程裝修按金單");
			cell.addTextItem(0, 105).setFontSize(14).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText("Construction and Renovation Deposit Slip");
			buildPhrase(cell, 105 + rowHeight, 0);
			return cell;
		}

		@Override
		protected Phrase buildPhrase(Cell cell, int y, int type) {
			Phrase ph = cell.addPhrase("lastPhrase", 0, y).setParentFontAndSize();
			if (type == 1) {
				List<Integer> widthList = Stream.of(85, 68, 150, 85, 130).collect(Collectors.toList());
				widthList.add(1, docWidth - widthList.stream().mapToInt(w -> w).sum());
				widthList.stream().forEach(cw -> ph.addTextCell(cw, rowHeight, 0, 5));
			} else
				ph.addTextCell(docWidth, lineHeight, 0, 2);
			return ph;
		}
	}
}
