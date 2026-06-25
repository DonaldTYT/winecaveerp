package com.uniinformation.dynamic.propmgmtpro;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.propmgmtpro.PropmgmtProCellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.TextSpliter;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import static com.uniinformation.utils.ChnftrBuilder.*;

public class PrintMeetingTopic extends PrintMeetingStub {
	public static Map<String, Integer> VoteResultMap = MapUtil.of("贊成", 1, "單選贊成", 1, "反對", 2, "棄權", 3, "多選贊成", 4, "統一反對", 5, "統一棄權", 6);
	private BiResult brMettingSignin;
	private List<Map<String, Object>> unitList;
	private Map<Integer, List<BiCellCollection>> topicMap;

	public PrintMeetingTopic(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(60);
		docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(60);
		docWidth = ChnftrParser.pxToDpi100(docWidthPx);
		docHeight = ChnftrParser.pxToDpi100(docHeightPx);
		rowHeight = 25;
		lineHeight = 25;
	}

	public PrintMeetingTopic() {
		this(null);
	}

	@Override
	public void print() throws Exception {
		for (Map<String, Object> unitMap : unitList) {
			int topicNum = 0;
			for (Map.Entry<Integer, List<BiCellCollection>> entry : topicMap.entrySet()) {
				if (recordCount > 0)
					builder.P();
				Cell cell0 = new Cell(builder, 0, 0, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
				String lcdesc = br.getCellString("lc_desc");
				Date meetingDate = br.getCellDate("col_b");
				String building = (String)unitMap.get("pcol_c");
				String floor = (String)unitMap.get("pcol_d");
				String room = (String)unitMap.get("pcol_e");
				String quotient = dfq.format((double)unitMap.get("pcol_m"));
				String ownerBarcode = (String)unitMap.get("pcol_n");
				List<BiCellCollection> topicList = entry.getValue();
				String sign = topicList.get(0).getCellString("col_d");
				String pageTitle = topicList.get(0).getCellString("col_e");
				//print header
				cell0.addItem(new TextItem()).setText(String.format("%s\n單位：%s -- （%s%%）\n日期：%s", 
								lcdesc,
								StringUtils.equals(building, "000") ? String.format("%s樓%s座", floor, room) : String.format("第%s座%s樓%s座", building, floor, room),
								quotient,
								sdfm.format(meetingDate)));
				cell0.addItem(new TextItem(docWidth - 35, 0)).setFontAndSize(24, "helv_br", chnFont).setText(sign);
				addBarcodeItem(cell0, docWidth - 140, 0, 85, ownerBarcode);
				cell0.addItem(new LineItem(0, 75).setRect(0, 0, docWidth, 0));
				cell0.build();

				//print topic page
				Cell cell = new Cell(builder, cell0, 0, 80, 0, 0).setParentFontAndSize();
				int offset = 0, offsetVote = 45;
				cell.addItem(new TextItem()).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText(pageTitle);
				for (BiCellCollection bcc : topicList) {
					String topicNo = bcc.getCellString("col_f");
					String topicTitle = bcc.getCellString("col_g");
					String topicContent = bcc.getCellString("col_i");
					topicNum++;
					offset += rowHeight;
					cell.addItem(new TextItem(0, offset)).setText(String.format("%s：%s", topicNo, topicTitle));
					int optionNum = 0;
					Phrase ph = null;
					for (String lineStr : topicContent.split("\\r\\n|\\r|\\n")) {
						offset += rowHeight;
						cell.addItem(new TextItem(0, offset)).setText(lineStr.replaceAll("□贊成|□反對|□棄權|□多選贊成|□單選贊成|□統一反對|□統一棄權", "").replaceAll("[\\u00A0\\s]", " "));
						if (lineStr.indexOf("□贊成") >= 0) {
							ph = buildVotePhrase(cell, "barcode", offset, ownerBarcode, topicNum, ++optionNum, "贊成", "反對", "棄權");
							offset += offsetVote - rowHeight;
						} else if (lineStr.indexOf("□多選贊成") >= 0) {
							ph = buildVotePhrase(cell, "barcode", offset, ownerBarcode, topicNum, ++optionNum, "", "", "多選贊成");
							offset += offsetVote - rowHeight;
						} else if (lineStr.indexOf("□單選贊成") >= 0) {
							ph = buildVotePhrase(cell, "barcode", offset, ownerBarcode, topicNum, ++optionNum, "", "", "單選贊成");
							offset += offsetVote - rowHeight;
						} else if (lineStr.indexOf("□統一反對") >= 0) {
							buildVotePhrase(cell, null, offset, ownerBarcode, topicNum, 0, "", "統一反對", "統一棄權");
							offset += offsetVote - rowHeight;
						} 
					}
					if (optionNum == 1 && ph != null) {
						for (TextCell tc : ph.getGroupList().stream().map(x -> (TextCell)x).toArray(TextCell[]::new)) {
							if (StringUtils.isBlank(tc.getTextItem().getText()))
								continue;
							String s = makeVoteBarcodeString(tc, ownerBarcode, topicNum, 0);
							tc.setText("barcode", s);
							tc.setText("barcode_text", s);
						}
					}
					offset += rowHeight;
				}
				cell.build();
				
				//print bottom
				cell = new Cell(builder, cell0, 0, docHeight - 30, 0, 0).setParentFontAndSize();
				cell.addItem(new LineItem().setRect(0, 0, docWidth, 0));
				cell.addItem(new TextItem(0, 5)).setText(topicList.get(0).getCellString("col_j"));
				cell.build();
				recordCount++;
			}
		}
	}

	private Phrase buildVotePhrase(Cell cell, String barcodeKey, int y, String ownerBarcode, int topicNum, int optionNum, String... str) {
		if (ownerBarcode.length() != 6)
			ownerBarcode = "000000";
		if (topicNum > 99)
			topicNum = 0;
		if (optionNum > 99)
			optionNum = 0;
		Phrase ph = cell.addItem(new Phrase(builder, 0, y)).setParentFontAndSize();
		int cw = docWidth / 3;
		ph.addGroup(new TextCell(cw, cw, 0, 0));
		if (StringUtils.equalsAny(str[2], "多選贊成", "單選贊成", "統一棄權")) {
			ph.addGroup(new TextCell(cw + 70, cw, 0, 0));
			ph.addGroup(new TextCell(cw - 70, cw, 0, 0));
		} else {
			ph.addGroup(new TextCell(cw, cw, 0, 0));
			ph.addGroup(new TextCell(cw, cw, 0, 0));
		}
		int i = 0;
		for (TextCell tc : ph.getGroupList().stream().map(x -> (TextCell)x).toArray(TextCell[]::new)) {
			TextItem ti = tc.getTextItem();
			ti.setText(str[i]);
			if (StringUtils.isNoneBlank(ti.getText())) {
				TextSpliter ts = new TextSpliter(ti.getText(), engFont, chnFont, fontPt, tc.getWidth());
				ti.setXY(20, 2);
				tc.addItem(new BoxItem().setRect(0, 3, 15, 3 + 15));
				addBarcodeItem(tc, barcodeKey, 20 + ts.getWidth() + 10, 0, 85, makeVoteBarcodeString(tc, ownerBarcode, topicNum, optionNum));
			}
			i++;
		}
		return ph;
	}
	
	private String makeVoteBarcodeString(TextCell tc, String ownerBarcode, int topicNum, int optionNum) {
		return String.format("%s%02d%02d%d", ownerBarcode, topicNum, optionNum,
								VoteResultMap.get(tc.getTextItem().getText()));
	}

	private void resetupS2Comp(Listbox lb) {
		ZkUtil.delayJs(lb,null,50,"zkbis2.setup('%s',%b,%b,'%s',%b,%b);$('#%s').focus()",lb.getUuid(), lb.isMultiple(), false, StringUtils.defaultString((String)lb.getAttribute("placeholder")), true, false, lb.getUuid());
	}

	@Override
	public void actionPerformed(JxField field) {
		try {
			JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
			br = jxf.getBr();
			sh = br.getSessionHelper();
			Button bt = (Button)field.getNativeObject();
			Integer sid = (Integer)bt.getAttribute("brSid");
			if (brMettingSignin == null || sid == null || br.getCurrentCollection().getSid() != sid) {
				brMettingSignin = BiResultHelper.create(sh, "propmgmtpro.MettingSignin", brMettingSignin, String.format("col_a = %d and col_b = '%s'", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
				bt.setAttribute("brSid", br.getCurrentCollection().getSid());
			}
			List<Map<String, Object>> ulist = ZkUtil.getBiResultRecordMap(brMettingSignin, false);
			Vector<BiCellCollection> tlist = br.getSubLinkResult("propmgmtpro.MettingTopic");
			Listbox[] lbs = new Listbox[5];
			for (int i = 0; i < lbs.length; i++) {
				int ii = i;
				Listbox lb = lbs[i] = new Listbox();
				lb.setMold("select");
				lb.setMultiple(false);
				lb.setAttribute("select2-enable", "Y");
				lb.setAttribute("select2-multiple", "N");
				lb.setAttribute("placeholder", sh.getLabel("Please choose an option"));
				lb.setWidth(i < 3 ? "150px" : "50px");
				lb.addEventListener(Events.ON_SELECT, (SelectEvent<Listitem, String> event) -> {
					UniLog.log1("ON_SELECT event:", event);
					switch (ii) {
					case 0:
						lbs[1].setSelectedIndex(-1);
						lbs[1].getItems().clear();
						ulist.stream().filter(m -> lbs[0].getSelectedIndex() >= 0 && StringUtils.equals((String)m.get("pcol_c"), lbs[0].getSelectedItem().getValue()))
									.map(m -> (String)m.get("pcol_d")).distinct().sorted().forEach(s -> {
							lbs[1].appendItem(s, s);
						});
						lbs[2].setSelectedIndex(-1);
						lbs[2].getItems().clear();
						resetupS2Comp(lbs[1]);
						resetupS2Comp(lbs[2]);
						break;
					case 1:
						lbs[2].setSelectedIndex(-1);
						lbs[2].getItems().clear();
						ulist.stream().filter(m -> lbs[0].getSelectedIndex() >= 0 && StringUtils.equals((String)m.get("pcol_c"), lbs[0].getSelectedItem().getValue())
												&& lbs[1].getSelectedIndex() >= 0 && StringUtils.equals((String)m.get("pcol_d"), lbs[1].getSelectedItem().getValue()))
									.map(m -> (String)m.get("pcol_e")).distinct().sorted().forEach(s -> {
							lbs[2].appendItem(s, s);
						});
						resetupS2Comp(lbs[2]);
						break;
					}
				});
				switch (ii) {
				case 0:
					ulist.stream().map(m -> (String)m.get("pcol_c")).distinct().sorted().forEach(s -> {
						lbs[ii].appendItem(s, s);
					});
					break;
				case 3:
				case 4:
					tlist.stream().sorted((a, b) -> a.getCellInt("col_c") - b.getCellInt("col_c")).map(bcc -> bcc.getCellString("col_d")).distinct().forEach(s -> {
						lbs[ii].appendItem(s, s);
					});
					break;
				}
				ZkUtil.timerEvent(null, lb, 50, () -> {
					ZkUtil.setupSelect2(lb, true, true);
				});
			}

			GridHelper gh = new GridHelper(2);
			gh.setWidth("265px");
			gh.getColumn(0).setWidth("100px");
			gh.getColumn(1).setHflex("max");
			gh.addRow(new Label("座號："), new Div() {{ appendChild(lbs[0]); }});
			gh.addRow(new Label("樓層："), new Div() {{ appendChild(lbs[1]); }});
			gh.addRow(new Label("單位："), new Div() {{ appendChild(lbs[2]); }});
			gh.addRow(new Label("頁面標誌："), new Hlayout() {{
				appendChild(lbs[3]);
				appendChild(new Label("至"));
				appendChild(lbs[4]);
			}});
			new ZkBiMsgbox(sh).setContent(gh).setButtons(new String[] {sh.getBtLabel("Ok"), sh.getBtLabel("Cancel")}).setEventListener(new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
					if (btn.getIdx() != 0)
						return;
					ZkUtil.CheckedFunction<Integer, String> ca = i -> {
						return lbs[i].getSelectedIndex() >= 0 ? lbs[i].getSelectedItem().getValue() : null;
					};
					String building = ca.apply(0);
					String floor = ca.apply(1);
					String room = ca.apply(2);
					String signFrom = ca.apply(3);
					String signTo = ca.apply(4);
					int pageFrom1 = 0, pageTo1 = 0;
					try {
						pageFrom1 = PropmgmtProCellCollection.letterToDecimal(signFrom);
						pageTo1 = PropmgmtProCellCollection.letterToDecimal(signTo);
						if ((pageFrom1 != 0 && pageFrom1 < 1) || pageFrom1 > 1000 || (pageTo1 != 0 && pageTo1 < 1) || pageTo1 > 1000 || pageFrom1 > pageTo1)
							throw new Exception("");
					} catch (Exception e) {
						UniLog.log(e);
						ZkBiMsgbox.show("頁面標誌輸入不正確");
						return;
					}
					if (pageTo1 == 0)
						pageTo1 = 1000;
					int pageFrom = pageFrom1, pageTo = pageTo1;
					UniLog.log1("building:%s, floor:%s, room:%s, pageFrom:%d, pageTo:%d", building, floor, room, pageFrom, pageTo);
					unitList = ulist.stream().filter(m -> (building != null ? StringUtils.equals((String)m.get("pcol_c"), building) : true)
											&& (floor != null ? StringUtils.equals((String)m.get("pcol_d"), floor) : true)
											&& (room != null ? StringUtils.equals((String)m.get("pcol_e"), room) : true))
									.collect(Collectors.toList());
					topicMap = new TreeMap<>();
					tlist.stream().filter(bcc -> bcc.getInt("col_c") >= pageFrom && bcc.getInt("col_c") <= pageTo)
									.sorted((a, b) -> a.getCellInt("col_c") - b.getCellInt("col_c"))
									.sorted((a, b) -> a.getCellInt("col_h") - b.getCellInt("col_h")).forEach(bcc -> {
						List<BiCellCollection> list = topicMap.get(bcc.getCellInt("col_c"));
						if (list == null)
							topicMap.put(bcc.getCellInt("col_c"), list = new ArrayList<>());
						list.add(bcc);
					});
					if (unitList.isEmpty()) {
						ZkBiMsgbox.show("物業資料沒有找到");
						return;
					}
					if (topicMap.isEmpty()) {
						ZkBiMsgbox.show("選票資料沒有找到");
						return;
					}
					initBuilder();
					print();
					ZkUtil.showPdfDialog((Component) field.getJxForm().getNativeComponent(), sh, getPrintData(), getDocumentName(br));
				}
			}).build().appendStyle("width:300px").doModal();
		} catch (Exception ex) {
			UniLog.log(ex); 
			ZkBiMsgbox.show(ex.toString());
		}
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return "選票";
	}
}
