package com.uniinformation.dynamic.propertymgmt;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultProjectFee;
import com.uniinformation.erpv4.BatchBuildPrintHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.CompInfo;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.kyoko.common.ReturnMsg;
import com.kyoko.utils.MoneyToChinese;

public class PrintProjectPaymentInvoice extends BatchBuildPrintHandler {
	protected static final int TEXT_MARGIN = 2;
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
   	protected static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
   	protected static DecimalFormat df = new DecimalFormat("$#,##0.00");

	protected PrintTemplate ptp;
	
	protected String cocode;
    protected Map<String, Object> coMap;
	protected boolean needUpdatePrintCount = true;

	protected Vector<BiCellCollection> payItemList;
	protected double pageFee;
    private int printCnt;

	
	public PrintProjectPaymentInvoice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
		docWidth = 760;
		docHeight = 1100;
		docWidthPx = ChnftrParser.dpi100ToPx(docWidth);
		docHeightPx = ChnftrParser.dpi100ToPx(docHeight);
		rowHeight = 30;
		lineHeight = 20;
		maxBottom = 100;
		chnFont = "mshei";
		fontPt = 10;
		printMode = PRINT_MODE.DOWNLOAD;
		needDownloadRename = true;
	}

	public PrintProjectPaymentInvoice() {
		this(null);
	}
	
	@Override
	protected ReturnMsg initBuilder() {
        cocode = Erpv4Config.getDefaultCoCode(sh);
        coMap = Erpv4Config.getCoFieldMap(sh, cocode);
		return super.initBuilder();
	}

	@Override
	public void print(BiResult p_br) throws Exception {
		needUpdatePrintCount = false;
		super.print(p_br);
	}

	@Override
	protected void print() throws Exception {
		ptp.startPrint();
		
		updatePrintCount();
		payItemList = br.getSubLinkResult("propertymgmt.PayProjectItem");
		
		String[] pls = BiResultProjectFee.periodColumnLabelList.stream().map(l -> l.replace("col_", "pjfu_")).toArray(String[]::new);
		for (BiCellCollection c : payItemList) {
			ptp.buildPhrase(1, "維修分攤費", c.getString("col_d"));
			for (int i = c.getInt("col_e"); i < c.getInt("col_e") + c.getInt("col_f"); i++) {
				double d = c.getDouble(pls[i - 1]);
				ptp.buildAdjustPhrase(1, "", c.getString("pjf_name"), String.format("第%d期，共%d期", i, c.getInt("pjf_period")), df.format(d), df.format(d));
				pageFee += d;
			}
		}
		ptp.endPrint();
	}

	@Override
	public void afterActionAsync(AfterActionCallback cb) {
		super.afterActionAsync(cb);
		if (biBase != null)
			biBase.refreshNeedSet();
	}

	@Override
	public void actionPerformed(JxField field) {
		jxf = (JxZkBiBase) field.getJxForm();
		br = jxf.getBr();
		sh = br.getSessionHelper();
		if (jxf.isDirty()) {
			ZkUtil.showWarnMsg(sh.getLabel("Record is updating. Print action abort"));
			return;
		}
		if (jxf.getCurMode() == JxZkBiBase.MODE_ADD) {
			ZkUtil.showWarnMsg(sh.getLabel("Record is adding. Print action abort"));
			return;
		}
		super.actionPerformed(field);
	}
	
	protected void updatePrintCount() throws Exception {
		printCnt = br.getCellInt("col_v");
		if (needUpdatePrintCount) {
			printCnt++;
			br.getCell("col_v").set(printCnt);
			br.updateCurrent();
			if (biBase != null)
				biBase.biBaseRefreshListitems(br.getCurrentRecord());
			else if (jxf != null)
				jxf.refreshCurrentBiBaseListitem();
		}
	}
	
	protected class PrintTemplate extends PrintTemplate1 {
		protected String[] headerStrings = new String[] { "#", "繳費單位\nProperty Unit", "繳交期數\nPeriod Number", "期數金額\nPeriod Amount", "合計\nTotal Amount" };

		@Override
		protected Cell newCell() throws Exception {
			if (!cellList.isEmpty())
				buildPhrase(cellList.getLast(), docHeight - 100, 2, "小計 Subtotal for this page (MOP)：", df.format(pageFee)).getTextCell(1).addMultiHorLineItem(5, 28, 30);
			Cell cell = new Cell(builder, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
			cellList.add(cell);
			cell.addTextItem().setFontSize(16).setText((String)coMap.get("co_coname"));
			cell.addTextItem(0, 25).setFontAndSize(9, "helv_br", chnFont).setText((String)coMap.get("co_chnname"));
			cell.addTextItem(0, 40).setFontSize(9).setText(Erpv4Config.getCoAddr(sh, cocode));
			cell.addTextItem(0, 55).setFontSize(9).setText(String.format("電話 TEL: %s       傳真 FAX: %s       准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")));
	
			String s = "聯絡人Contact: ";
			String colk = PrintProjectPaymentInvoice.this instanceof PrintPaymentInvoice2 ? "pm_col_k" : "pcol_k";
			if (payItemList.stream().map(c -> c.getString(colk)).distinct().limit(2).count() == 1)
				s += payItemList.get(0).getString(colk);
			cell.addTextItem(0, 82).setFontSize(10).setText(s);
			cell.addTextItem(0, 20).setFontSize(11).setB().setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText("正式收據 OFFICAL RECEIPT");
	
			addNoLabelItem(cell, 0, "No.:");
			addNoValueItem(cell, 0, true, br.getCellString("col_b") + (printCnt > 1 ? "R" : ""));
			addNoLabelItem(cell, 42, "參考編號 Ref:");
			addNoValueItem(cell, 42, false, br.getCellString("col_r"));
			addNoLabelItem(cell, 62, "日期 Date:");
			addNoValueItem(cell, 62, false, sdf.format(br.getCell("col_a").getDate()));
			addNoLabelItem(cell, 82, "付款方式 Payment type:");
			addNoValueItem(cell, 82, false, br.getCellString("col_g"));
	
			buildPhrase(cell, 110, 0, headerStrings);
			
			if (cellList.size() > 1)
				buildPhrase(cell, 160, 2, "接上頁 Continued from previous page (MOP)：", df.format(pageFee)).getTextCell(1).addMultiHorLineItem(5, 28, 30);
			pageFee = 0;
	
			cell.addLineItem(0, docHeight - 65).setRect(docWidth);
			cell.addTextItem(0, docHeight - 60).setFontAndSize(9, "helv_nr", "msheib").setText("系統產生的收據，無需簽署。System generated receipt, no signature is required.");
			cell.addTextItem(0, docHeight - 60).setFontAndSize(9, "helv_nr", "msheib").setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText("打印時間 Print time: " + sdf2.format(new Date()));
			cell.addPictureItem(440, docHeight - 140).setAll(0, 100, 0, false, sh.getWebContentRealPath(CompInfo.getImageUrl(sh, "RECEIPT_STAMP_"), false));
			return cell;
		}

		@Override
		protected Phrase buildPhrase(Cell cell, int y, int type) {
			Phrase ph = cell.addPhrase("lastPhrase", 0, y).setParentFontAndSize();
			if (type < 2) {
				List<Integer> widthList = Stream.of(100, 130, 130, 130).collect(Collectors.toList());
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
						ph.getTextCell(i).setAlign(i < 3 ? PdfContentByte.ALIGN_LEFT : PdfContentByte.ALIGN_RIGHT);
				}
			} else {
				ph.addTextCell(docWidth - 130, rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_RIGHT);
				ph.addTextCell(130, rowHeight, TEXT_MARGIN, 5).setAlign(PdfContentByte.ALIGN_RIGHT);
			}
			return ph;
		}

		public void endPrint() throws Exception {
			Cell lastCell = cellList.getLast();

			TextCell tc;
			if (cellList.size() > 1) {
				Phrase lastPhrase = lastCell.getAnyItem("lastPhrase");
				tc = buildPhrase(2, "小計 Subtotal for this page (MOP)：", df.format(pageFee)).getTextCell(1).addMultiHorLineItem(30, 32);
				if (lastPhrase.getY() > 160 + rowHeight)
					tc.addHorLineItem(5);
			} 
			double actualFee = br.getCellDouble("vcol_actualfee");
			tc = buildPhrase(2, "收款總額 Total amount (MOP)：", df.format(actualFee)).getTextCell(1).addMultiHorLineItem(30, 32);
			if (cellList.size() == 1)
				tc.addHorLineItem(5);

			lastCell.addLineItem(0, docHeight - 90).setRect(docWidth);
			lastCell.addTextItem(0, docHeight - 85).setFontAndSize(9, "helv_nr", "msheib").setText("交來澳門元" + MoneyToChinese.convertMoneyToWord(actualFee));
			RpcClient rpc = sh.getRpcClient();
			Value v2 = rpc.callSegment("saynumber",
					new VectorUtil()
						.addElement("ENGLISH")
						.addElement(actualFee)
						.toVector());
			rpc.close();
			if (v2 != null)
				lastCell.addTextItem(0, docHeight - 85).setFontSize(9).setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText("The sum of MOP " + v2.toString() + " Dollars.");

			endPrint((cell, pageNum, pageCount) -> {
				cell.addTextItem(0, docHeight - 35).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText(String.format("第%d頁，共%d頁", pageNum + 1, pageCount));
			});
		}

		private void addNoLabelItem(Cell cell, int y, String text) {
			cell.addTextItem(470, y).setFontSize(10).setAlign(PdfContentByte.ALIGN_RIGHT, 200).setText(text);
		}
	
		private void addNoValueItem(Cell cell, int y, boolean bold, String text) {
			cell.addTextItem(docWidth - 85, y).setFontSize(10).setB(bold).setAlign(PdfContentByte.ALIGN_RIGHT, 85).setText(text);
		}

	}
}
