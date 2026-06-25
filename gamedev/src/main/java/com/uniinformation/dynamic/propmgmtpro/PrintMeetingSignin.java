package com.uniinformation.dynamic.propmgmtpro;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.utils.ChnftrBuilder;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.utils.ChnftrBuilder.*;

public class PrintMeetingSignin extends PrintMeetingStub {
	private BiResult brMettingSignin;

	public PrintMeetingSignin(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		Rectangle ps = PageSize.A4.rotate();
		pageSize = new Rectangle(0, 0, ps.getWidth(), ps.getHeight());
		docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(60);
		docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(60);
		docWidth = ChnftrParser.pxToDpi100(docWidthPx);
		docHeight = ChnftrParser.pxToDpi100(docHeightPx);
		UniLog.log1("docWidth:%d, docHeight:%d", docWidth, docHeight);
		rowHeight = 62;
	}

	public PrintMeetingSignin() {
		this(null);
	}

	@Override
	public void print() throws Exception {
		brMettingSignin = BiResultHelper.create(sh, "propmgmtpro.MettingSignin", brMettingSignin, String.format("col_a = %d and col_b = '%s'", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
		List<Map<String, Object>> unitList = ZkUtil.getBiResultRecordMap(brMettingSignin, false);
		unitList.sort((a, b) -> {
			int cc;
			if ((cc = StringUtils.compare((String)a.get("pcol_c"), (String)b.get("pcol_c"))) != 0)
				return cc;
			else if ((cc = StringUtils.compare((String)a.get("pcol_d"), (String)b.get("pcol_d"))) != 0)
				return cc;
			else 
				return StringUtils.compare((String)a.get("pcol_e"), (String)b.get("pcol_e"));
		});
		Cell cell0 = new Cell(builder, 0, 0, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
		cell0.addItem(new TextItem()).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setFontAndSize(18, engFont, "mshei").setText(br.getCellString("lc_desc"));
		cell0.addItem(new TextItem(0, 30)).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText(String.format("%s分層建築物所有人大會簽到表", sdfm.format(br.getCellDate("col_b"))));

		Phrase phHeader = buildPhrase(cell0, false);
		phHeader.setXY(0, 55);
		phHeader.setTopLines(0, -1, true);
		phHeader.setLeftLines(0, -1, true);
		phHeader.setRightLines(6, -1, true);
		phHeader.setGroupsTexts(null, "座號", "樓層", "單位", "業權人", "財產制度", "份額(%)", "依證件簽署");
		adjustPhrase(phHeader);

		Phrase phDetail = buildPhrase(phHeader, true);
		phDetail.setTopLines(0, -1, true);
		phDetail.setLeftLines(0, -1, true);
		phDetail.setRightLines(6, -1, true);

		int i = 0;
		for (Map<String, Object> m : unitList) {
			String building = (String)m.get("pcol_c");
			String floor = (String)m.get("pcol_d");
			String room = (String)m.get("pcol_e");
			String propSystem = (String)m.get("pcol_o");
			String quotient = dfq.format((double)m.get("pcol_m"));
			String owners = (String)m.get("pcol_h");
			int j = i % 10;
			if (j == 0) {
				if (i > 0)
					builder.P();
				cell0.build();
				phHeader.build();
			}
			phDetail.setXY(0, rowHeight + rowHeight * j);
			phDetail.setBottomLines(0, -1, j == 9 || i == unitList.size() - 1);
			phDetail.setGroupsTexts(null, String.format("第%s座", building), String.format("%s樓", floor), room, owners, propSystem, quotient, "");
			adjustPhrase(phDetail);
			phDetail.build();
			i++;
			recordCount++;
		}
	}
	
	@Override
	protected byte[] getPrintData() throws Exception {
		ChnftrParser p = createChnftrParser(null);
		p.loadTemplateStream(new ByteArrayInputStream(createChnftrParser(builder.toString()).printToData()));
		ChnftrBuilder b = new ChnftrBuilder();
		new TextItem(b, 0, docHeight - 20).setFontAndSize(fontPt, engFont, chnFont).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText("第${pagenum}頁，共${pagecount}頁").build();
		p.setTemplatePageChnftrText(b.toString());
		return p.printToData();
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return "簽到表";
	}
	
	private Phrase buildPhrase(Group parent, boolean isDetail) {
		Phrase ph = new Phrase(builder, parent).setParentFontAndSize();
		ph.addGroup(new TextCell(80, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, 76);
		ph.addGroup(new TextCell(60, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, 56);
		ph.addGroup(new TextCell(50, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, 46);
		ph.addGroup(new TextCell(659, rowHeight, 2, 22)).setAlign(isDetail ? PdfContentByte.ALIGN_LEFT : PdfContentByte.ALIGN_CENTER, 655);
		ph.addGroup(new TextCell(80, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, 76);
		ph.addGroup(new TextCell(80, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, 76);
		ph.addGroup(new TextCell(100, rowHeight, 2, 22)).setAlign(PdfContentByte.ALIGN_CENTER, 96);
		return ph;
	}
}
