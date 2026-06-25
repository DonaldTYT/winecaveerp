package com.uniinformation.dynamic.propmgmtpro;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.erpv4.BatchBuildPrintHandler;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ChnftrParser.TextSpliter;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

import static com.uniinformation.utils.ChnftrBuilder.*;

public class PrintMeetingStub extends BatchBuildPrintHandler {
	protected SimpleDateFormat sdfm = new SimpleDateFormat("yyyy年M月dd日");
	protected DecimalFormat dfq = new DecimalFormat("0.0000");

	private BiResult brMettingSignin;
	private List<Map<String, Object>> unitList;

	public PrintMeetingStub(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	public PrintMeetingStub() {
		this(null);
	}

	@Override
	public void print() throws Exception {
		int i = 0;
		for (Map<String, Object> m : unitList) {
			int j = i % 8;
			if ((i == 0 && recordCount > 0) || (i > 0 && j == 0))
				builder.P();
			String unit = (String)m.get("col_c");
			String quotient = dfq.format((double)m.get("pcol_m"));
			String barcode = (String)m.get("pcol_n");
			Date meetingDate = br.getCellDate("col_b");
			int x = j % 2 == 1 ? docWidth / 2 : 0;
			int y = (docHeight / 4) * (j / 2);
			TextSpliter ts = new TextSpliter(unit, engFont, chnFont, fontPt, docWidth / 2 - 10);
			int maxWidth = Math.max(ts.getWidth(), 200);

			Cell cell = new Cell(builder, x, y, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
			cell.addItem(new TextItem()).setFontAndSize(16, engFont, "mshei").setText("會議存根");
			cell.addItem(new TextItem(0, 2)).setAlign(PdfContentByte.ALIGN_RIGHT, maxWidth).setText(quotient + "%");
			cell.addItem(new TextItem(0, rowHeight)).setText(String.join("\n", ts.getResultList()));
			cell.addItem(new TextItem(0, rowHeight * 2)).setText(String.format("日期：%s", sdfm.format(meetingDate)));
			addBarcodeItem(cell, 100, rowHeight * 3, 85, barcode + "-01");
			cell.addItem(new TextItem(0, rowHeight * 3 + 15)).setText("出席：");
			cell.addItem(new BoxItem(55, rowHeight * 3 + 15).setRectAndNum(0, 0, 15, 15, 0));
			addBarcodeItem(cell, 100, 180, 85, barcode + "-02");
			cell.addItem(new TextItem(0, 180 + 15)).setText("授權：");
			cell.addItem(new BoxItem(55, 180 + 15).setRectAndNum(0, 0, 15, 15, 0));
			cell.build();
			recordCount++;
			i++;
		}
	}

	protected void addBarcodeItem(Cell cell, int x, int y, int width, String barcode) {
		addBarcodeItem(cell, null, x, y, width, barcode);
	}

	protected void addBarcodeItem(Cell cell, String key, int x, int y, int width, String barcode) {
		cell.addItem(key, new BarcodeItem(x, y).setText(barcode).setSize(22, width));
		cell.addItem(key + "_text",new TextItem(x, y + 25)).setAlign(PdfContentByte.ALIGN_CENTER, width).setFontAndSize(8, "helv_nr", chnFont).setText(barcode);
	}
	
	protected void adjustPhrase(Phrase ph) {
		ph.getGroupList().stream().filter(grp -> grp instanceof TextCell).map(grp -> (TextCell)grp).forEach(tc -> {
			TextItem ti = tc.getTextItem();
			TextSpliter ts = new TextSpliter(ti.getText(), ti.getEngFontFace(), ti.getChnFontFace(), ti.getFontSize(), tc.getWidth() - ti.getX() * 2);
			ti.setY(Math.max((tc.getHeight() - lineHeight * (ts.getResultCount() - 1) - ChnftrParser.pxToDpi100(ti.getFontSize())) / 2, 2));
			ti.setText(String.join("\n", ts.getResultList()));
		});
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return "會議存根";
	}

	@Override
	public void actionPerformed(JxField field) {
		if (!StringUtils.equals(getClass().getSimpleName(), "PrintMeetingStub")) {
			super.actionPerformed(field);
			return;
		}
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
			Listbox[] lbs = new Listbox[3];
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
					UniLog.log1("building:%s, floor:%s, room:%s", building, floor, room);
					unitList = ulist.stream().filter(m -> (building != null ? StringUtils.equals((String)m.get("pcol_c"), building) : true)
											&& (floor != null ? StringUtils.equals((String)m.get("pcol_d"), floor) : true)
											&& (room != null ? StringUtils.equals((String)m.get("pcol_e"), room) : true))
									.collect(Collectors.toList());
					if (unitList.isEmpty()) {
						ZkBiMsgbox.show("物業資料沒有找到");
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

	private void resetupS2Comp(Listbox lb) {
		ZkUtil.delayJs(lb,null,50,"zkbis2.setup('%s',%b,%b,'%s',%b,%b);$('#%s').focus()",lb.getUuid(), lb.isMultiple(), false, StringUtils.defaultString((String)lb.getAttribute("placeholder")), true, false, lb.getUuid());
	}
}