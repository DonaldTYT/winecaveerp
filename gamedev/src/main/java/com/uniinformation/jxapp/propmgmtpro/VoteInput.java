package com.uniinformation.jxapp.propmgmtpro;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.dynamic.propmgmtpro.PrintMeetingVoteStatistics;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import static com.uniinformation.bicore.propmgmtpro.PropmgmtProCellCollection.getVoteDesc;

public class VoteInput extends JxZkBiBase {
	private Set<String> voteList;
	private Map<BiCellCollection, Set<String>> unitVoteMap;
	private Vector<BiCellCollection> signinList;
	
	private Textbox tbScan;

	@Override
	public void afterBind() {
		super.afterBind();
		Selectors.find("[id='btVote']").stream().findFirst().ifPresent(bt -> ((Button)bt).addSclass("orange"));
		jxAdd("btVote").addActionListener(btnEvent -> {
			try {
				if (!tbScan.isVisible()) {
					tbScan.setVisible(true);
					ZkUtil.js("$('#%s').closest('.zkbi-detail-grid.z-row').show();keepFocusText.init('%s')", tbScan.getUuid(), tbScan.getUuid());

					ZkUtil.setEventListener(tbScan, Events.ON_OK, ev -> {
						String barcode = tbScan.getText();
						if (StringUtils.length(barcode) != 11 || !barcode.matches("^[\\d]+$")) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "選票代碼輸入錯誤");
							return;
						}
						String unitBarcode = barcode.substring(0, 6);
						String voteCode = barcode.substring(6, 11);
						int topicNum = Integer.parseInt(barcode.substring(6, 8));
						int optionNum = Integer.parseInt(barcode.substring(8, 10));
						int voteNum = Integer.parseInt(barcode.substring(10, 11));
						BiCellCollection bcc = signinList.stream().filter(c -> StringUtils.equals(c.getCellString("pcol_n"), unitBarcode)).findFirst().orElse(null);
						if (bcc == null) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "物業單位編碼不正確");
							return;
						}
						if (!StringUtils.equalsAny(bcc.getCellString("col_d"), "Y", "A")) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "未簽到單位不能投票");
							return;
						}
						if (!voteList.contains(voteCode)) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "選票代碼輸入錯誤。");
							return;
						}
						Set<String> vlist = unitVoteMap.get(bcc);
						if (vlist.contains(voteCode)) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "選票代碼輸入重複");
							return;
						}
						switch (voteNum) {
						case 1:
							if (vlist.contains(String.format("%02d%02d2", topicNum, optionNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'反對'不能再投'贊成'");
								return;
							}
							if (vlist.contains(String.format("%02d%02d3", topicNum, optionNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'棄權'不能再投'贊成'");
								return;
							}
							if (vlist.stream().anyMatch(s -> s.matches(String.format("^%02d\\d{2}1$", topicNum)))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'單選贊成'不能再投'單選贊成'");
								return;
							}
							break;
						case 2:
							if (vlist.contains(String.format("%02d%02d1", topicNum, optionNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'贊成'不能再投'反對'");
								return;
							}
							if (vlist.contains(String.format("%02d%02d3", topicNum, optionNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'棄權'不能再投'反對'");
								return;
							}
							break;
						case 3:
							if (vlist.contains(String.format("%02d%02d1", topicNum, optionNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'贊成'不能再投'棄權'");
								return;
							}
							if (vlist.contains(String.format("%02d%02d2", topicNum, optionNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'反對'不能再投'棄權'");
								return;
							}
							break;
						case 4:
							if (vlist.contains(String.format("%02d005", topicNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'統一反對'不能再投'多選贊成'");
								return;
							}
							if (vlist.contains(String.format("%02d006", topicNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'統一棄權'不能再投'多選贊成'");
								return;
							}
							break;
						case 5:
							if (vlist.stream().anyMatch(s -> s.matches(String.format("^%02d\\d{2}1$", topicNum)))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'單選贊成'不能再投'統一反對'");
								return;
							}
							if (vlist.stream().anyMatch(s -> s.matches(String.format("^%02d\\d{2}4$", topicNum)))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'多選贊成'不能再投'統一反對'");
								return;
							}
							if (vlist.contains(String.format("%02d006", topicNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'統一棄權'不能再投'統一反對'");
								return;
							}
							break;
						case 6:
							if (vlist.stream().anyMatch(s -> s.matches(String.format("^%02d\\d{2}1$", topicNum)))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'單選贊成'不能再投'統一棄權'");
								return;
							}
							if (vlist.stream().anyMatch(s -> s.matches(String.format("^%02d\\d{2}4$", topicNum)))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'多選贊成'不能再投'統一棄權'");
								return;
							}
							if (vlist.contains(String.format("%02d005", topicNum))) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "投了'統一反對'不能再投'統一棄權'");
								return;
							}
							break;
						}
						vlist.add(voteCode);
						bcc.getCell("col_f").set(String.join(",", vlist));
						//bcc.getCell("vcol_votes").set(getVoteListDesc(bcc.getCellString("col_f")));
						tbScan.setText("");
						sortDetListbox();
						setDirtyFlag(true);
					});
				} else {
					tbScan.setVisible(false);
					ZkUtil.js("$('#%s').closest('.zkbi-detail-grid.z-row').hide();keepFocusText.exit()", tbScan.getUuid());
				}
			} catch (Exception e) {
				UniLog.log(e);
				ZkBiMsgbox.show(ZkBiMsgbox.Type.error, e.toString());
			}
		});

		Listbox lb = (Listbox)jxAdd("list_propmgmtpro_VoteInputDet").getNativeObject();
		ZkUtil.setEventListener(lb, "onItemRendererCallback", event -> {
			Map<String, Object> m = (Map<String, Object>)event.getData();
			Listitem listItem = (Listitem)m.get("listItem");
			int idx = (int)m.get("idx");
			Object data = m.get("data");
			UniLog.log1("onItemRendererCallback event:%s, idx:%d", event, idx);
          	if (data instanceof TrStatFilter)
           		data = ((TrStatFilter)data).getTrStatIdx();
          	BiResult brDet = getBr().getSubLink("propmgmtpro.VoteInputDet");
          	BiCellCollection bcc = brDet.getRowCollectionO(data);
          	Listcell lc = (Listcell)listItem.getFirstChild();
          	lc.setStyle("text-align:center");
			Toolbarbutton b = new Toolbarbutton() {{
				setIconSclass("z-icon-pencil z-icon-2x");
    			setSclass("narrowtoolbarbutton");
				setStyle("opacity:0.7");
				setTooltiptext("編輯選票");
				setVisible(StringUtils.equalsAny(bcc.getCellString("col_d"), "Y", "A"));
				setParent(lc);
			}};
			b.addEventListener(Events.ON_CLICK, ev -> {
				Div div = (Div)curComp.getTemplate("editVoteTemplate").create(null, null, null, null)[0];
				Label lbUnit = ZkUtil.getFellowWithNullId(div, "lbUnit");
				lbUnit.setValue(bcc.getCellString("col_c"));
				Set<String> vlist = unitVoteMap.get(bcc);
				for (String s : vlist) {
					Button bt = new Button(getVoteDesc(s));
					bt.setAttribute("code", s);
					bt.addEventListener(Events.ON_CLICK, evbt -> {
						if (bt.hasAttribute("markdelete")) {
							bt.removeAttribute("markdelete");
							bt.removeSclass("markdelete");
						} else {
							bt.setAttribute("markdelete", true);
							bt.addSclass("markdelete");
							StreamSupport.stream(div.queryAll("button").spliterator(), false).filter(bt1 -> bt1 != bt && bt1.hasAttribute("markdelete")).map(bt1 -> (Button)bt1).forEach(bt1 -> {
								bt1.removeAttribute("markdelete");
								bt1.removeSclass("markdelete");
							});
						}
					});
					div.appendChild(bt);
				}

				ZkBiMsgboxButton[] btns = Arrays.stream(new String[] {"OK", "Cancel"}).map(s -> new ZkBiMsgboxButton(sessionHelper.getBtLabel(s))).toArray(ZkBiMsgboxButton[]::new);
				btns[0].setAttribute("forbitClose", true);
				new ZkBiMsgbox(sessionHelper).setContent(div).setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
						UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
						if (btn.getIdx() != 0)
							return;
						AtomicBoolean markDelRef = new AtomicBoolean();
						StreamSupport.stream(div.queryAll("button").spliterator(), false).filter(bt -> bt.hasAttribute("markdelete")).forEach(bt -> {
							vlist.remove(bt.getAttribute("code"));
							markDelRef.set(true);
						});
						if (markDelRef.get()) {
							bcc.getCell("col_f").set(String.join(",", vlist));
							//bcc.getCell("vcol_votes").set(getVoteListDesc(bcc.getCellString("col_f")));
							sortDetListbox();
							setDirtyFlag(true);
						}
						btn.closeMsgbox();
					}
				}).build().setTitle("編輯選票").appendStyle("width:100%;max-width:570px").setVboxStyle("margin:10px 0 10px 0").doModal();
			});
		});
		lb.setAttribute("hasOnItemRendererCallback", true);
	}

	@Override
	public void bindCellCollection(BiResult c, int mode) {
		super.bindCellCollection(c, mode);
		try {
			ColumnCell cc = getBr().getCell("vcol_scan");
			cc.setMode(Cell.VMODE_HIDDEN);
			Textbox tb = (Textbox)((JxField)cc.getMapper()).getNativeObject();
			if (!tb.hasAttribute("tbScan")) {
				tbScan = new Textbox();
				tbScan.setMaxlength(11);
				tbScan.setWidth("120px");
				tb.getParent().insertBefore(tbScan, tb);
				tb.setAttribute("tbScan", tbScan);
			} else
				tbScan = (Textbox)tb.getAttribute("tbScan");
			tbScan.setText("");
			tbScan.setVisible(false);
			ZkUtil.js("$('#%s').closest('.zkbi-detail-grid.z-row').hide()", tbScan.getUuid());

			BiResult brMettingTopic = BiResultHelper.create(sessionHelper, "propmgmtpro.MettingTopic", String.format("col_a = %d and col_b = '%s'", getBr().getCellInt("col_a"), getBr().getCellString("col_b")), -1, null);
			voteList = PrintMeetingVoteStatistics.getVoteMap(ZkUtil.getBiResultRecordMap(brMettingTopic, false), null)
							.entrySet().stream().filter(entry -> entry.getValue() instanceof Double).map(entry -> entry.getKey())
							.collect(Collectors.toCollection(TreeSet::new));
			signinList = getBr().getSubLinkResult("propmgmtpro.VoteInputDet");
			unitVoteMap = new HashMap<>();
			for (BiCellCollection bcc : signinList) {
				Set<String> ss = Arrays.stream(StringUtils.split(bcc.getCellString("col_f"), ",")).collect(Collectors.toCollection(TreeSet::new));
				unitVoteMap.put(bcc, ss);
				//bcc.getCell("vcol_votes").set(getVoteListDesc(bcc.getCellString("col_f")));
			}
			brMettingTopic.close();
			UniLog.log1("voteList:%s", voteList);
			sortDetListbox();
			Listbox lb = (Listbox)jxAdd("list_propmgmtpro_VoteInputDet").getNativeObject();
			Textbox tbFilter = (Textbox)lb.getFellowIfAny("footlayout_filterInput_" + lb.getId(), true);
			if (tbFilter != null) {
				UniLog.log1("tbFilter:%s", tbFilter);
				tbFilter.removeEventListener(Events.ON_CHANGE, footFilterListener);
				tbFilter.addEventListener(Events.ON_CHANGE, footFilterListener);
			}
		} catch (Exception e) {
			UniLog.log(e);
			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, e.toString());
		}
	}

	private void sortDetListbox() {
		Listbox lb = (Listbox)jxAdd("list_propmgmtpro_VoteInputDet").getNativeObject();
		BiResult br = getBr().getSubLink("propmgmtpro.VoteInputDet");
		ListModelList<Object> model = (ListModelList<Object>)lb.getModel();
		model.sort((a, b) -> {
          	if (a instanceof TrStatFilter)
           		a = ((TrStatFilter)a).getTrStatIdx();
          	if (b instanceof TrStatFilter)
           		b = ((TrStatFilter)b).getTrStatIdx();
			int i1 = StringUtils.isNotBlank(br.getRowCollectionO(a).getCellString("col_f")) ? 1 : 0;
			int i2 = StringUtils.isNotBlank(br.getRowCollectionO(b).getCellString("col_f")) ? 1 : 0;
			return i2 - i1;
		});
	}

	private EventListener<InputEvent> footFilterListener = (event) -> {
		sortDetListbox();
	};
}
