package com.uniinformation.dynamic.propertymgmt;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.nextMonth;

public class PrintParkingTmpLocPaymentInvoice extends PrintProjectPaymentInvoice {
	
	public PrintParkingTmpLocPaymentInvoice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
	}

	public PrintParkingTmpLocPaymentInvoice() {
		this(null);
	}
	
	@Override
	protected void print() throws Exception {
		ptp.startPrint();
		
		updatePrintCount();
		payItemList = br.getSubLinkResult("propertymgmt.PayParkingItem");
		
		for (BiCellCollection c : payItemList) {
			double fee = c.getDouble("col_g");
			ptp.buildPhrase(1, "暫放費", String.format("位置 (Location) : %s , 合同 (Contract) : %s 至 %s , %s", c.getString("col_c"), c.getString("col_d"), c.getString("ccol_e"), c.getString("ccol_f")));
			ptp.buildAdjustPhrase(1, "", c.getString("ccol_c"), c.getString("col_e"), nextMonth(c.getString("col_e"), c.getInt("col_f") - 1), c.getString("col_f"), df.format(c.getDouble("ccol_k")), df.format(fee));
			pageFee += fee;
		}
		ptp.endPrint();
	}

	protected class PrintTemplate extends PrintProjectPaymentInvoice.PrintTemplate {

		public PrintTemplate() {
			headerStrings = new String[] { "#", "繳費單位\nProperty Unit", "繳費由月份\nMonth from", "繳費至月份\nMonth to", "月數\nNo of mon.", "每月金額\nMonthly fee", "合計\nTotal Amount" };
		}

		@Override
		protected Phrase buildPhrase(Cell cell, int y, int type) {
			Phrase ph = cell.addPhrase("lastPhrase", 0, y).setParentFontAndSize();
			if (type < 2) {
				List<Integer> widthList = Stream.of(60, 90, 90, 80, 100, 120).collect(Collectors.toList());
				widthList.add(1, docWidth - widthList.stream().mapToInt(w -> w).sum());
				widthList.stream().forEach(cw -> {
					ph.addTextCell(cw, type == 0 ? 50 : rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_CENTER);
				});
				if (type == 0) {
					ph.getTextCell(0).getTextItem().setY(15);
					ph.setLeftLines(1, -1, true);
					ph.setTopLines(0, -1, true);
					ph.setBottomLines(0, -1, true);
				} else {
					for (int i = 0; i < ph.getGroupList().size(); i++)
						ph.getTextCell(i).setAlign(i < 4 ? PdfContentByte.ALIGN_LEFT : i == 4 ? PdfContentByte.ALIGN_CENTER : PdfContentByte.ALIGN_RIGHT);
				}
			} else {
				ph.addTextCell(docWidth - 120, rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_RIGHT);
				ph.addTextCell(120, rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_RIGHT);
			}
			return ph;
		}
	}
}
