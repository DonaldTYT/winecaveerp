package com.uniinformation.dynamic.propmgmtpro;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.util.concurrent.AtomicDouble;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.BiUtil.CheckedSupplier;
import com.uniinformation.zkbi.ZkBiComposerBase;
import static com.uniinformation.utils.ChnftrBuilder.*;

public class PrintMeetingVoteStatistics extends PrintMeetingStub {
	private static int TEXT_MARGIN = 5;
	private LinkedList<Cell> cellList = new LinkedList<>();
	private boolean printFlag;
	private BiResult brMettingTmpTopic, brMettingTopic, brMettingSignin;
	
	public PrintMeetingVoteStatistics(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		Rectangle ps = PageSize.A4.rotate();
		pageSize = new Rectangle(0, 0, ps.getWidth(), ps.getHeight());
		docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(60);
		docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(60);
		docWidth = ChnftrParser.pxToDpi100(docWidthPx);
		docHeight = ChnftrParser.pxToDpi100(docHeightPx);
		rowHeight = 60;
	}

	public PrintMeetingVoteStatistics() {
		this(null);
	}

	@Override
	public void print() throws Exception {
		if (recordCount > 0)
			builder.P();
		cellList.clear();
		printFlag = false;

		AtomicDouble totalQuotientRef = new AtomicDouble();
		brMettingTopic = BiResultHelper.create(sh, "propmgmtpro.MettingTopic", brMettingTopic, String.format("col_a = %d and col_b = '%s'", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
		Map<String, Object> voteMap = getVoteMap(ZkUtil.getBiResultRecordMap(brMettingTopic, false), totalQuotientRef);
		UniLog.log1("voteMap.keyset:%s", voteMap.keySet());
		UniLog.log1("voteMap double keys:%s", voteMap.entrySet().stream().filter(e -> e.getValue() instanceof Double).map(e -> e.getKey()).collect(Collectors.toCollection(TreeSet::new)));
		
		Map<String, Set<String>> voteMap1 = voteMap.keySet().stream()
	            .collect(Collectors.groupingBy(
	                    key -> key.substring(0, 4), TreeMap::new,
	                    Collectors.mapping(
	                        key -> key.substring(4),
	                        Collectors.toCollection(TreeSet::new))));
		for (Map.Entry<String, Set<String>> entry : voteMap1.entrySet()) {
			String prefix = entry.getKey();
			int topicNum = NumberUtils.toInt(prefix.substring(0, 2));
			int optionNum = NumberUtils.toInt(prefix.substring(2, 4));
			Phrase ph = buildPhrase();
			TextCell tc = ph.getTextCell(0);
			if (optionNum == 0)
				tc.setAlign(PdfContentByte.ALIGN_LEFT, 0).setText(String.format("議題%d", topicNum));
			else
				tc.setAlign(PdfContentByte.ALIGN_RIGHT, tc.getWidth() - TEXT_MARGIN * 2).setText(String.format("議題%d.%d", topicNum, optionNum));
			for (String voteStr : entry.getValue()) {
				int phNum;
				if (voteStr.equals("贊成"))
					phNum = 1;
				else if (voteStr.equals("反對"))
					phNum = 2;
				else if (voteStr.equals("棄權"))
					phNum = 3;
				else if (voteStr.equals("空白票"))
					phNum = 4;
				else if (voteStr.equals("合共"))
					phNum = 5;
				else if (PrintMeetingTopic.VoteResultMap.values().stream().anyMatch(v -> String.valueOf(v).equals(voteStr)))
					continue;
				else
					throw new Exception(String.format("voteStr %s not found", voteStr));
				builder.addItem(prefix + voteStr, ph.getTextCell(phNum));
			}
		}

		brMettingTmpTopic = BiResultHelper.create(sh, "propmgmtpro.MettingTmpTopic", brMettingTmpTopic, String.format("col_a = %d and col_b = '%s'", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
		List<Map<String, Object>> tmpTopicList = ZkUtil.getBiResultRecordMap(brMettingTmpTopic, false);
		Map<String, String> tdMap = tmpTopicList.stream().collect(Collectors.toMap(m -> (String)m.get("col_c"), m -> (String)m.get("col_d"), (o, n) -> o, TreeMap::new));
		Map<String, Double> ttMap = StreamSupport.stream(tdMap.keySet().spliterator(), false).collect(Collectors.toMap(s -> s, s -> 0.0, (o, n) -> o, TreeMap::new));

		printFlag = false;
		if (!cellList.isEmpty()) {
			Cell cell = cellList.getLast();
			Phrase lastPhrase = (Phrase)cell.getItem("lastPhrase");
			lastPhrase.setBottomLines(0, -1, true);
			//cell.addItem("lastPhrase", new Phrase(0, lastPhrase.getY() + lastPhrase.getHeight() + rowHeight));
			cell.addItem("lastPhrase", new Phrase(0, docHeight));
		}

		int i = 0;
		Phrase[] phs = null;
		for (Map.Entry<String, String> entry : tdMap.entrySet()) {
			String code = entry.getKey();
			String desc = entry.getValue();
			int j = i % 5;
			if (j == 0)
				phs = buildPhrase2();
			phs[0].getTextCell(j + 1).setText(code + " " + desc);
			builder.addItem(code, phs[1].getTextCell(j + 1));
			i++;
		}

		if (!cellList.isEmpty()) {
			brMettingSignin = BiResultHelper.create(sh, "propmgmtpro.MettingSignin", brMettingSignin, String.format("col_a = %d and col_b = '%s' and col_d in ('Y', 'A')", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
			List<Map<String, Object>> signinList = ZkUtil.getBiResultRecordMap(brMettingSignin, false);
			totalQuotientRef.set(signinList.stream().mapToDouble(m -> (double)m.get("pcol_m")).sum());
			cellList.getFirst().setText("有效戶數", String.format("有效戶數：%d", signinList.size()));
			cellList.getFirst().setText("有效份額", String.format("有效份額：%s%%", dfq.format(totalQuotientRef.get())));
			
			for (Map<String, Object> m : signinList) {
				double quotient = (double)m.get("pcol_m");
				String voteListStr = (String)m.get("col_f");
				String ttListStr = (String)m.get("col_g");
				if (StringUtils.isNotBlank(voteListStr)) {
					Arrays.stream(voteListStr.split(",")).filter(s -> StringUtils.isNotBlank(s)).forEach(voteKey -> {
						Object o = voteMap.get(voteKey);
						if (o != null && o instanceof Double)
							voteMap.put(voteKey, (double)o + quotient);
					});
				}
				if (StringUtils.isNotBlank(ttListStr)) {
					Arrays.stream(ttListStr.split(",")).filter(s -> StringUtils.isNotBlank(s)).forEach(ttKey -> {
						Double d = ttMap.get(ttKey);
						if (d != null)
							ttMap.put(ttKey, d + quotient);
					});
				}
			}
			for (Map.Entry<String, Object> entry : voteMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Double)
					builder.setText(key, dfq.format((double)value));
				else if (value instanceof CheckedSupplier)
					builder.setText(key, dfq.format(((CheckedSupplier<Double>)value).get()));
				else if (value == null)
					drawSlash((TextCell)builder.getItem(key));
			}
			for (Map.Entry<String, Double> entry : ttMap.entrySet())
				builder.setText(entry.getKey(), dfq.format(entry.getValue()));
		} else
			getCurrentCell();
		
		int pageNum = 0;
		for (Cell cell : cellList) {
			if (pageNum > 0)
				builder.P();
			((Phrase)cell.getItem("lastPhrase")).setBottomLines(0, -1, true);
			cell.addItem(new TextItem(0, docHeight - 20).setAlign(PdfContentByte.ALIGN_CENTER, docWidth)).setText(String.format("第%d頁，共%d頁", pageNum + 1, cellList.size()));
			cell.getItemList().stream().filter(item -> item instanceof Phrase).forEach(item -> adjustPhrase((Phrase)item));
			cell.build();
			pageNum++;
		}
		recordCount++;
	}
	
	private Cell getCurrentCell() {
		Cell cell = null;
		if (!cellList.isEmpty()) {
			cell = cellList.getLast();
			Phrase lastPhrase = (Phrase)cell.getItem("lastPhrase");
			if (lastPhrase.getY() + lastPhrase.getHeight() + rowHeight > docHeight)
				cell = null;
		}
		if (cell == null) {
			cellList.add(cell = new Cell(builder, 0, 0).setFontAndSize(fontPt, engFont, chnFont));
			cell.addItem(new TextItem()).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setFontAndSize(18, engFont, "mshei").setText(br.getCellString("lc_desc"));
			cell.addItem(new TextItem(0, 30)).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText(String.format("%s分層建築物所有人大會投票結果統計表", sdfm.format(br.getCellDate("col_b"))));
			Phrase ph = buildPhrase(cell, 0).setGroupsTexts(null, "議題編號", "贊成 %", "反對 %", "棄權 %", "空白票 %", "合共 %");
			if (!printFlag) {
				cell.addItem("有效戶數", new TextItem(0, 55).setText("有效戶數："));
				cell.addItem("有效份額", new TextItem(200, 55).setText("有效份額："));
				ph.setY(80);
				printFlag = true;
			} else
				ph.setY(55);
		}
		return cell;
	}

	private Cell getCurrentCell2() {
		Cell cell = null;
		if (!cellList.isEmpty()) {
			cell = cellList.getLast();
			Phrase lastPhrase = (Phrase)cell.getItem("lastPhrase");
			if (lastPhrase.getY() + lastPhrase.getHeight() + rowHeight * 2 > docHeight)
				cell = null;
		}
		if (cell == null) {
			cellList.add(cell = new Cell(builder, 0, 0).setFontAndSize(fontPt, engFont, chnFont));
			cell.addItem(new TextItem()).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setFontAndSize(18, engFont, "mshei").setText(br.getCellString("lc_desc"));
			cell.addItem(new TextItem(0, 30)).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText(String.format("%s分層建築物所有人大會投票結果統計表", sdfm.format(br.getCellDate("col_b"))));
			Phrase ph = cell.addItem("lastPhrase", new Phrase(0, 0));
			if (!printFlag) {
				cell.addItem(new TextItem(0, 55).setText("臨時議程選項結果："));
				ph.setY(80);
				printFlag = true;
			} else
				ph.setY(55);
		}
		return cell;
	}

	private Phrase buildPhrase() {
		Cell cell = getCurrentCell();
		Phrase lastPhrase = (Phrase)cell.getItem("lastPhrase");
		return buildPhrase(cell, lastPhrase.getY() + lastPhrase.getHeight());
	}

	private Phrase[] buildPhrase2() {
		Cell cell = getCurrentCell2();
		Phrase[] phs = new Phrase[2];
		for (int i = 0; i < phs.length; i++) {
			Phrase lastPhrase = (Phrase)cell.getItem("lastPhrase");
			phs[i] = buildPhrase(cell, lastPhrase.getY() + lastPhrase.getHeight());
		}
		phs[0].getTextCell(0).setAlign(PdfContentByte.ALIGN_LEFT, 0).setText("臨時議程編號");
		phs[1].getTextCell(0).setAlign(PdfContentByte.ALIGN_LEFT, 0).setText("贊成 %");
		return phs;
	}
	
	private Phrase buildPhrase(Cell cell, int y) {
		Phrase ph = cell.addItem("lastPhrase", new Phrase(0, y)).setParentFontAndSize();
		int cw = docWidth / 6;
		for (int i = 0; i < 6; i++)
			ph.addGroup(new TextCell(cw, rowHeight, TEXT_MARGIN, 22)).setAlign(PdfContentByte.ALIGN_CENTER, cw - TEXT_MARGIN * 2);
		ph.setLeftLines(0, -1, true);
		ph.setTopLines(0, -1, true);
		ph.setRightLines(5, -1, true);
		ph.setBottomLines(0, -1, false);
		return ph;
	}

	private void drawSlash(TextCell tc) {
		tc.addItem(new LineItem().setRect(0, tc.getHeight(), tc.getWidth(), 0));
	}
	
	private static String makeVotePrefix(int topicNum, int optionNum) {
		return String.format("%02d%02d", topicNum, optionNum);
	}
	
	public static Map<String, Object> getVoteMap(List<Map<String, Object>> mettingTopicList, AtomicDouble totalQuotientRef) {
		Map<String, Object> voteMap = new TreeMap<>();
		int topicNum = 0;
		for (Map<String, Object> m : mettingTopicList.stream()
												.sorted((a, b) -> (int)a.get("col_c") - (int)b.get("col_c"))
												.sorted((a, b) -> (int)a.get("col_h") - (int)b.get("col_h")).collect(Collectors.toList())) {
			topicNum++;
			int optionCount = 0;
			int mode = 0;
			String topicContent = (String)m.get("col_i");
			for (String lineStr : Arrays.stream(topicContent.split("\\r\\n|\\r|\\n")).filter(lineStr -> StringUtils.isNotBlank(lineStr)).toArray(String[]::new)) {
				if (StringUtils.indexOf(lineStr, "□多選贊成") >= 0) {
					optionCount++;
					mode = 2;
				} else if (StringUtils.indexOf(lineStr, "□單選贊成") >= 0) {
					optionCount++;
					mode = 1;
				} else if (StringUtils.indexOf(lineStr, "□贊成") >= 0) {
					optionCount++;
					mode = 0;
				}
			}
			UniLog.log1("topicNum:%d, mode:%d, optionCount:%d", topicNum, mode, optionCount);
			CheckedSupplier<Double> f;
			if (optionCount > 1) {
				String prefix0 = makeVotePrefix(topicNum, 0);
				for (int optionNum = 1; optionNum <= optionCount; optionNum++) {
					String prefix = makeVotePrefix(topicNum, optionNum);
					switch (mode) {
					case 2:
						voteMap.put(prefix + 4, 0.0);
						voteMap.put(prefix + "贊成", f = () -> (double)voteMap.get(prefix + 4));
						voteMap.put(prefix + "反對", f = () -> (double)voteMap.get(prefix0 + 5));
						voteMap.put(prefix + "棄權", f = () -> (double)voteMap.get(prefix0 + 6));
						voteMap.put(prefix + "空白票", f = () -> totalQuotientRef.get() - (double)voteMap.get(prefix + 4) - (double)voteMap.get(prefix0 + 5) - (double)voteMap.get(prefix0 + 6));
						break;
					case 1:
						voteMap.put(prefix + 1, 0.0);
						voteMap.put(prefix + "贊成", f = () -> (double)voteMap.get(prefix + 1));
						voteMap.put(prefix + "反對", f = () -> (double)voteMap.get(prefix0 + 5));
						voteMap.put(prefix + "棄權", f = () -> (double)voteMap.get(prefix0 + 6));
						voteMap.put(prefix + "空白票", f = () -> totalQuotientRef.get() - (double)voteMap.get(prefix + 1) - (double)voteMap.get(prefix0 + 5) - (double)voteMap.get(prefix0 + 6));
						break;
					case 0:
						voteMap.put(prefix + 1, 0.0);
						voteMap.put(prefix + 2, 0.0);
						voteMap.put(prefix + 3, 0.0);
						voteMap.put(prefix + "贊成", f = () -> (double)voteMap.get(prefix + 1));
						voteMap.put(prefix + "反對", f = () -> (double)voteMap.get(prefix + 2));
						voteMap.put(prefix + "棄權", f = () -> (double)voteMap.get(prefix + 3));
						voteMap.put(prefix + "空白票", f = () -> totalQuotientRef.get() - (double)voteMap.get(prefix + 1) - (double)voteMap.get(prefix + 2) - (double)voteMap.get(prefix + 3));
						break;
					}
					voteMap.put(prefix + "合共", f = () -> totalQuotientRef.get());
				}
				if (mode == 1 || mode == 2) {
					voteMap.put(prefix0 + 5, 0.0);
					voteMap.put(prefix0 + 6, 0.0);
				}
				voteMap.put(prefix0 + "贊成", null);
				voteMap.put(prefix0 + "反對", null);
				voteMap.put(prefix0 + "棄權", null);
				voteMap.put(prefix0 + "空白票", null);
				voteMap.put(prefix0 + "合共", null);
			} else if (optionCount == 1) {
				String prefix = makeVotePrefix(topicNum, 0);
				switch (mode) {
				case 2:
					voteMap.put(prefix + 4, 0.0);
					voteMap.put(prefix + 5, 0.0);
					voteMap.put(prefix + 6, 0.0);
					voteMap.put(prefix + "贊成", f = () -> (double)voteMap.get(prefix + 4));
					voteMap.put(prefix + "反對", f = () -> (double)voteMap.get(prefix + 5));
					voteMap.put(prefix + "棄權", f = () -> (double)voteMap.get(prefix + 6));
					voteMap.put(prefix + "空白票", f = () -> totalQuotientRef.get() - (double)voteMap.get(prefix + 4) - (double)voteMap.get(prefix + 5) - (double)voteMap.get(prefix + 6));
					break;
				case 1:
					voteMap.put(prefix + 1, 0.0);
					voteMap.put(prefix + 5, 0.0);
					voteMap.put(prefix + 6, 0.0);
					voteMap.put(prefix + "贊成", f = () -> (double)voteMap.get(prefix + 1));
					voteMap.put(prefix + "反對", f = () -> (double)voteMap.get(prefix + 5));
					voteMap.put(prefix + "棄權", f = () -> (double)voteMap.get(prefix + 6));
					voteMap.put(prefix + "空白票", f = () -> totalQuotientRef.get() - (double)voteMap.get(prefix + 1) - (double)voteMap.get(prefix + 5) - (double)voteMap.get(prefix + 6));
					break;
				case 0:
					voteMap.put(prefix + 1, 0.0);
					voteMap.put(prefix + 2, 0.0);
					voteMap.put(prefix + 3, 0.0);
					voteMap.put(prefix + "贊成", f = () -> (double)voteMap.get(prefix + 1));
					voteMap.put(prefix + "反對", f = () -> (double)voteMap.get(prefix + 2));
					voteMap.put(prefix + "棄權", f = () -> (double)voteMap.get(prefix + 3));
					voteMap.put(prefix + "空白票", f = () -> totalQuotientRef.get() - (double)voteMap.get(prefix + 1) - (double)voteMap.get(prefix + 2) - (double)voteMap.get(prefix + 3));
					break;
				}
				voteMap.put(prefix + "合共", f = () -> totalQuotientRef.get());
			} else {
				String prefix = makeVotePrefix(topicNum, 0);
				voteMap.put(prefix + "贊成", null);
				voteMap.put(prefix + "反對", null);
				voteMap.put(prefix + "棄權", null);
				voteMap.put(prefix + "空白票", null);
				voteMap.put(prefix + "合共", null);
			}
		}
		return voteMap;
	}
	
	@Override
	protected String getDocumentName(BiResult p_br) {
		return "投票結果統計表";
	}
}
