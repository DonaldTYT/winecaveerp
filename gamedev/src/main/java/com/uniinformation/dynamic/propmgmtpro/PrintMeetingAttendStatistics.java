package com.uniinformation.dynamic.propmgmtpro;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.utils.ChnftrBuilder.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class PrintMeetingAttendStatistics extends PrintMeetingStub {
	private BiResult brMettingSignin;

	public PrintMeetingAttendStatistics(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		Rectangle ps = PageSize.A4.rotate();
		pageSize = new Rectangle(0, 0, ps.getWidth(), ps.getHeight());
		docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(60);
		docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(60);
		docWidth = ChnftrParser.pxToDpi100(docWidthPx);
		docHeight = ChnftrParser.pxToDpi100(docHeightPx);
		rowHeight = 62;
	}

	public PrintMeetingAttendStatistics() {
		this(null);
	}

	@Override
	public void print() throws Exception {
		if (recordCount > 0)
			builder.P();
		Cell cell = new Cell(builder, 0, 0, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
		cell.addItem(new TextItem()).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setFontAndSize(18, engFont, "mshei").setText(br.getCellString("lc_desc"));
		cell.addItem(new TextItem(0, 30)).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText(String.format("%s分層建築物所有人大會出席及授權統計表", sdfm.format(br.getCellDate("col_b"))));

		Phrase ph = cell.addItem(new Phrase(builder, 0, 65)).setParentFontAndSize();
		int cw = docWidth / 7;
		for (int i = 0; i < 7; i++)
			ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(6, -1, true);
		ph.setGroupsTexts(null, "物業類型", "單位總數", "出席戶數", "授權戶數", "出席份額 (%)", "授權份額 (%)	", " 總份額 (%)");

		ph = cell.addItem(new Phrase(builder, 0, ph.getY() + rowHeight)).setParentFontAndSize();
		ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("住宅單位總數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("住宅出席戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("住宅授權戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("住宅出席份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("住宅授權份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("住宅總份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(6, -1, true);
		ph.setGroupsTexts(null, "住宅");

		ph = cell.addItem(new Phrase(builder, 0, ph.getY() + rowHeight)).setParentFontAndSize();
		ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("商鋪單位總數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("商鋪出席戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("商鋪授權戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("商鋪出席份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("商鋪授權份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("商鋪總份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(6, -1, true);
		ph.setGroupsTexts(null, "商鋪");

		ph = cell.addItem(new Phrase(builder, 0, ph.getY() + rowHeight)).setParentFontAndSize();
		ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("車位單位總數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("車位出席戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("車位授權戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("車位出席份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("車位授權份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("車位總份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(6, -1, true);
		ph.setGroupsTexts(null, "車位");

		ph = cell.addItem(new Phrase(builder, 0, ph.getY() + rowHeight)).setParentFontAndSize();
		ph.addGroup(new TextCell(cw, rowHeight * 2, 2, 22)).setAlign(PdfContentByte.ALIGN_RIGHT, cw - 4);
		ph.addGroup("合計單位總數", new TextCell(cw, rowHeight * 2, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("合計出席戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("合計授權戶數", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("合計出席份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("合計授權份額", new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("blank", new TextCell(cw, rowHeight * 4, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(6, -1, true);
		ph.setGroupsTexts(null, "合計：");
		ph.setBottomLines(6, -1, true);
		ph.getTextCell("blank").addItem(new LineItem().setRect(cw, 0, 0, rowHeight * 4));

		ph = cell.addItem(new Phrase(builder, 0, ph.getY() + rowHeight)).setParentFontAndSize();
		ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_RIGHT, cw - 4);
		ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - 4);
		ph.addGroup("戶數", new TextCell(cw * 2, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw * 2 - 4);
		ph.addGroup("份額", new TextCell(cw * 2, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw * 2 - 4);
		ph.setLeftLines(2, -1, true);
		ph.setTopLines(2, -1, true);
		ph.setRightLines(0, -1, false);

		ph = cell.addItem(new Phrase(builder, 0, ph.getY() + rowHeight)).setParentFontAndSize();
		ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_RIGHT, cw - 4);
		ph.addGroup("缺席戶數", new TextCell(cw * 5, rowHeight, 10, 22)).setAlign(PdfContentByte.ALIGN_LEFT, cw - 4);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(1, -1, false);
		ph.setGroupsTexts(null, "缺席戶數：");

		ph = cell.addItem(new Phrase(builder, 0, ph.getY() + rowHeight)).setParentFontAndSize();
		ph.addGroup(new TextCell(cw, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_RIGHT, cw - 4);
		ph.addGroup("缺席份額", new TextCell(cw * 5, rowHeight, 10, 22)).setAlign(PdfContentByte.ALIGN_LEFT, cw - 4);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(1, -1, false);
		ph.setBottomLines(0, -1, true);
		ph.setGroupsTexts(null, "缺席份額 (%)：");

		cell.getItemList().stream().filter(x -> x instanceof Phrase).forEach(item -> {
			((Phrase)item).getGroupMap().entrySet().forEach(entry -> {
				builder.addItem(entry.getKey(), entry.getValue());
			});
		});
		
		brMettingSignin = BiResultHelper.create(sh, "propmgmtpro.MettingSignin", brMettingSignin, String.format("col_a = %d and col_b = '%s'", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
		List<Map<String, Object>> list = ZkUtil.getBiResultRecordMap(brMettingSignin, false);
		long[] unitCount = new long[3];
		long[] attCount = new long[3];
		long[] authCount = new long[3];
		double[] unitQuotient = new double[3];
		double[] attQuotient = new double[3];
		double[] authQuotient = new double[3];
		double[] aaQuotient = new double[3];
		int i = 0;
		for (String propType : new String[] {"住宅", "商鋪", "車位"}) {
			List<Map<String, Object>> l = list.stream().filter(c -> StringUtils.equals((String)c.get("pcol_a"), propType)).collect(Collectors.toList());
			unitCount[i] = l.size();
			attCount[i] = l.stream().filter(c -> StringUtils.equals((String)c.get("col_d"), "Y")).count();
			authCount[i] = l.stream().filter(c -> StringUtils.equals((String)c.get("col_d"), "A")).count();
			unitQuotient[i] = l.stream().mapToDouble(c -> (double)c.get("pcol_m")).sum();
			attQuotient[i] = l.stream().filter(c -> StringUtils.equals((String)c.get("col_d"), "Y")).mapToDouble(c -> (double)c.get("pcol_m")).sum();
			authQuotient[i] = l.stream().filter(c -> StringUtils.equals((String)c.get("col_d"), "A")).mapToDouble(c -> (double)c.get("pcol_m")).sum();
			aaQuotient[i] = l.stream().filter(c -> StringUtils.equalsAny((String)c.get("col_d"), "Y", "A")).mapToDouble(c -> (double)c.get("pcol_m")).sum();
			builder.setText(propType+"單位總數", String.valueOf(unitCount[i]));
			builder.setText(propType+"出席戶數", String.valueOf(attCount[i]));
			builder.setText(propType+"授權戶數", String.valueOf(authCount[i]));
			builder.setText(propType+"出席份額", dfq.format(attQuotient[i]));
			builder.setText(propType+"授權份額", dfq.format(authQuotient[i]));
			builder.setText(propType+"總份額", dfq.format(aaQuotient[i]));
			i++;
		}
		long totalUnitCount, totalAttCount, totalAuthCount;
		double totalunitQuotient, totalAttQuotient, totalAuthQuotient;
		builder.setText("合計單位總數", String.valueOf(totalUnitCount = Arrays.stream(unitCount).sum()));
		builder.setText("合計出席戶數", String.valueOf(totalAttCount = Arrays.stream(attCount).sum()));
		builder.setText("合計授權戶數", String.valueOf(totalAuthCount = Arrays.stream(authCount).sum()));
		builder.setText("合計出席份額", dfq.format(totalAttQuotient = Arrays.stream(attQuotient).sum()));
		builder.setText("合計授權份額", dfq.format(totalAuthQuotient = Arrays.stream(authQuotient).sum()));
		totalunitQuotient = Arrays.stream(unitQuotient).sum();

		builder.setText("戶數", String.valueOf(totalAttCount + totalAuthCount));
		builder.setText("份額", dfq.format(totalAttQuotient + totalAuthQuotient));

		builder.setText("缺席戶數", String.valueOf(totalUnitCount - totalAttCount - totalAuthCount));
		builder.setText("缺席份額", dfq.format(totalunitQuotient - totalAttQuotient - totalAuthQuotient));
		
		cell.getItemList().stream().filter(x -> x instanceof Phrase).forEach(item -> {
			adjustPhrase((Phrase)item);
		});
		cell.build();
		recordCount++;
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return "出席統計表";
	}
}
