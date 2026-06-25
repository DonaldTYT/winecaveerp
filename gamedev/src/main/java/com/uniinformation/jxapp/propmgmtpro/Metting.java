package com.uniinformation.jxapp.propmgmtpro;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Listbox;

import com.google.common.collect.Lists;
import com.kyoko.common.NumberUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.ZkUtil.PickByBiResultForm;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

import static com.uniinformation.bicore.propmgmtpro.PropmgmtProCellCollection.decimalToLetter;

public class Metting extends JxZkBiBase {
	private Timer initTimer;

	@Override
	public void afterBind() {
		super.afterBind();
		Selectors.find("[id^='btExtraJxFormAction_']").forEach(bt -> ((Button)bt).addSclass("orange"));
		try {
			ZkJxPickInput comp = (ZkJxPickInput) jxAdd("lc_desc").getNativeObject();
			/*
			PickByTableTrForm pickForm = new PickByTableTrForm(sessionHelper, new String[] { "lc_desc", "lc_code" }, new String[] {"hflex=1;halign=left", "hflex=min"}, () -> {
				return Triple.of(getBr().getSelectUtil(), "select lc_rg, LPAD(lc_rg, GREATEST(LENGTH(lc_rg),3), '0') lc_code, lc_desc from location order by lc_rg", null);
			}, (rec, tr, userData) -> {
				comp.setText((String)rec[2]);
				Events.echoEvent(Events.ON_CHANGE, comp, null);
			});
			*/
			PickByBiResultForm pickForm = new PickByBiResultForm(sessionHelper, new String[] { "lc_desc", "lc_rg" }, new String[] {"hflex=1;halign=left", "hflex=min"}, () -> {
				return Triple.of("propertymgmt.Location", null, Lists.newArrayList(Pair.of("lc_rg", false)));
			}, (rec, tr, userData) -> {
				comp.setText((String)rec[1]);
				Events.echoEvent(Events.ON_CHANGE, comp, null);
			});
			pickForm.bindComponent(comp, null, true);
			comp.setPopupWidth("420px");
		} catch (Exception e) {
			UniLog.log(e);
		}
	}

	@Override
	public void bindCellCollection(final BiResult p_br, int mode) {
		super.bindCellCollection(p_br, mode);
		Listbox lb = (Listbox)jxAdd("list_propmgmtpro_MettingTopic").getNativeObject();
		lb.addSclass("list_propmgmtpro_MettingTopic");
		lb.renderAll();
		initTimer = ZkUtil.timerEvent(initTimer, curComp, "Loading", 100, () -> {
			Vector<BiCellCollection> clList = getBr().getSubLinkResult("propmgmtpro.MettingTopic");
			Collection<BiCellCollection> firstTopicList = clList.stream().collect(Collectors.toMap(c -> c.getCellInt("col_c"), c -> c, (o, n) -> o, LinkedHashMap::new)).values();
			clList.forEach(bcc -> {
				if (StringUtils.isNotBlank(bcc.getCellString("col_i"))) {
					Button btn = (Button)((ZkBiCellValueMapper)bcc.getCell("vcol_topicbutton").getMapper()).getComponent();
					btn.addSclass("hasrecord");
				}
				try {
					bcc.getCell("col_e").setMode(firstTopicList.contains(bcc) ? Cell.VMODE_NORMAL : Cell.VMODE_DISPONLY);
				} catch (CellException e) {
					UniLog.log(e);
				}
			});
		});
	}
	
	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(false);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(true);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}
	
	private ReturnMsg validationRecord(boolean isUpdate) throws Exception {
		Vector<BiCellCollection> topicList = getBr().getSubLinkResult("propmgmtpro.MettingTopic");
		if (topicList.size() > 99)
			return new ReturnMsg(false, "議程數不能超過99個", true);
		if (topicList.stream().map(bcc -> bcc.getCellString("col_f")).distinct().count() != topicList.size())
			return new ReturnMsg(false, "議程編號不能重复", true);
		if (topicList.stream().anyMatch(bcc -> bcc.getCellInt("col_c") <= 0))
			return new ReturnMsg(false, "頁碼必須大於0", true);
		Vector<BiCellCollection> tmpTopicList = getBr().getSubLinkResult("propmgmtpro.MettingTmpTopic");
		if (tmpTopicList.stream().anyMatch(bcc -> StringUtils.isBlank(bcc.getCellString("col_c"))))
			return new ReturnMsg(false, "臨時議程編號不能为空", true);
		if (!tmpTopicList.isEmpty() && !tmpTopicList.stream().allMatch(bcc -> bcc.getCellString("col_c").matches("^[A-Z]{2}$")))
			return new ReturnMsg(false, "臨時議程編號無效", true);
		if (tmpTopicList.stream().map(bcc -> bcc.getCellString("col_c")).distinct().count() != tmpTopicList.size())
			return new ReturnMsg(false, "臨時議程編號不能重复", true);
		return ReturnMsg.defaultOk;
	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd, BiResult sr, CellCollection cl, int p_insIdx) {
		ReturnMsg rtn = super.beforeAddLink(fd, sr, cl, p_insIdx);
		UniLog.log1("beforeAddLink p_insIdx:%d, fdName:%s", p_insIdx, fd != null ? fd.getName() : "");
		if (rtn != null && !rtn.getStatus()) return rtn;
		try {
			if (fd != null) {
				if (StringUtils.equalsAny(fd.getName(), "list_propmgmtpro_MettingTmpTopic", "btadd_list_propmgmtpro_MettingTmpTopic")) {
					Vector<BiCellCollection> clList = getBr().getSubLinkResult("propmgmtpro.MettingTmpTopic");
					for (int i = 0; ; i++) {
						String s = decimalToLetter(i + 27);
						if (!clList.stream().anyMatch(c -> StringUtils.equals(c.getString("col_c"), s))) {
							cl.getCell("col_c").set(s);
							break;
						}
					}
				}
			}
		} catch (Exception ex) {
			rtn = new ReturnMsg(false, -1, ex.getMessage());
		}
		return rtn;
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return Lists.newArrayList(
			new MettingTopicGetItemProperty(p_br.getSubLink("propmgmtpro.MettingTopic"))
		);	
	}

	private class MettingTopicGetItemProperty extends BiGetItemProperty {
		private static final String DEFAULT_PAGE_TITLE = "分層建築物所有人大會《選票》";
		private static final String DEFAULT_PAGE_REMARK = "上述“表决”只能在選項中選一項，並在該選項中用“√”表示，多選或少選該選項的視作廢票。";
		Textbox tbContent, tbRemark;

		public MettingTopicGetItemProperty(BiResult p_br) {
			super(p_br);
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_VALUE_CHANGED) {
				try {
					if (StringUtils.equals(bcc.getCellLabel(), "col_c")) {
						Vector<BiCellCollection> clList = bigibr.getRowCollectionList();
						int rowNum = clList.indexOf(cl);
						if (rowNum > 1) {
							BiCellCollection bfcl = clList.get(rowNum - 1);
							if (bfcl.getCellInt("col_c") != bcc.getInt()) {
								cl.getCell("col_e").setMode(Cell.VMODE_NORMAL);
								if (StringUtils.isBlank(cl.getCellString("col_e")))
									cl.getCell("col_e").set(DEFAULT_PAGE_TITLE);
							} else {
								cl.getCell("col_e").setMode(Cell.VMODE_DISPONLY);
								cl.getCell("col_e").set("");
							}
						} else {
							cl.getCell("col_e").setMode(Cell.VMODE_NORMAL);
							if (StringUtils.isBlank(cl.getCellString("col_e")))
								cl.getCell("col_e").set(DEFAULT_PAGE_TITLE);
						}
						if (StringUtils.isBlank(cl.getCellString("col_f")))
							cl.getCell("col_f").set(NumberUtil.toTChinese(rowNum + 1));
						if (StringUtils.isBlank(cl.getCellString("col_g")))
							cl.getCell("col_g").set("議題" + cl.getCellString("col_f"));
					} else if (StringUtils.equals(bcc.getCellLabel(), "vcol_topicbutton")) {
						Vector<BiCellCollection> clList = bigibr.getRowCollectionList();
						if (clList.indexOf(cl) < 0)
							return;
						Map<Integer, BiCellCollection> firstTopicMap = clList.stream().collect(Collectors.toMap(c -> c.getCellInt("col_c"), c -> c, (o, n) -> o, LinkedHashMap::new));
						Button btnTopic = (Button)((ZkBiCellValueMapper)bcc.getMapper()).getComponent();
						Vlayout vl = (Vlayout)curComp.getTemplate("inputTemplate").create(null, null, null, null)[0];
						((Label)ZkUtil.getFellowWithNullId(vl, "lbTopicTitle")).setValue(cl.getCellString("col_g"));
						ZkUtil.getFellowWithNullId(vl, "btVoteYNA").addEventListener(Events.ON_CLICK, event -> {
							ZkUtil.js("textArea.insertText('□贊成    □反對    □棄權')");
						});
						ZkUtil.getFellowWithNullId(vl, "btVoteMY").addEventListener(Events.ON_CLICK, event -> {
							ZkUtil.js("textArea.insertText('□多選贊成')");
						});
						ZkUtil.getFellowWithNullId(vl, "btVoteSY").addEventListener(Events.ON_CLICK, event -> {
							ZkUtil.js("textArea.insertText('□單選贊成')");
						});
						ZkUtil.getFellowWithNullId(vl, "btVoteNA").addEventListener(Events.ON_CLICK, event -> {
							ZkUtil.js("textArea.insertText('□統一反對    □統一棄權')");
						});
						ZkUtil.getFellowWithNullId(vl, "btVoteLine").addEventListener(Events.ON_CLICK, event -> {
							ZkUtil.js("textArea.insertText('----------')");
						});
						tbContent = ZkUtil.getFellowWithNullId(vl, "tbContent");
						tbContent.setText(cl.getCellString("col_i"));
						tbRemark = ZkUtil.getFellowWithNullId(vl, "tbRemark");
						if (firstTopicMap.containsValue(cl)) {
							tbRemark.setDisabled(false);
							tbRemark.setValue(StringUtils.defaultIfBlank(cl.getCellString("col_j"), DEFAULT_PAGE_REMARK));
						} else {
							tbRemark.setDisabled(true);
							tbRemark.setValue("");
						}
						ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")), new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
						btns[0].setAttribute("forbitClose", true);
						new ZkBiMsgbox(sessionHelper).setContent(vl).setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
								if (btn.getIdx() != 0)
									return;
								String s = tbContent.getText();
								if (s.length() > 2000) {
									ZkBiMsgbox.show("內容不能超過2000個字符");
									return;
								} 
								int voteYCount = StringUtil.countSubstring(s, "□贊成");
								int voteMYCount = StringUtil.countSubstring(s, "□多選贊成");
								int voteSYCount = StringUtil.countSubstring(s, "□單選贊成");
								int voteMNCount = StringUtil.countSubstring(s, "□統一反對");
								if (Arrays.stream(new Boolean[] {voteYCount > 0, voteMYCount > 0, voteSYCount > 0}).filter(x -> x).count() > 1) {
									ZkBiMsgbox.show("'□贊成/□多選贊成/□單選贊成'不能同時存在");
									return;
								}
								if (voteMNCount > 1) {
									ZkBiMsgbox.show("'□統一反對'必須是1個");
									return;
								}
								if (voteYCount > 0 && voteMNCount > 0) {
									ZkBiMsgbox.show("'□贊成'模式不能包含'□統一反對'");
									return;
								}
								if (voteMYCount > 0 && voteMNCount == 0) {
									ZkBiMsgbox.show("'□多選贊成'必須包含'□統一反對'");
									return;
								}
								if (voteSYCount > 0 && voteMNCount == 0) {
									ZkBiMsgbox.show("'□單選贊成'必須包含'□統一反對'");
									return;
								}
								if (voteYCount > 99 || voteMYCount > 99 || voteSYCount > 99) {
									ZkBiMsgbox.show("只能输入最多99個次選項");
									return;
								}
								
								if (!StringUtils.equals(cl.getCellString("col_i"), s)) {
									cl.getCell("col_i").set(s);
									setDirtyFlag(true);
								}
								if (!StringUtils.equals(cl.getCellString("col_j"), tbRemark.getText())) {
									cl.getCell("col_j").set(tbRemark.getText());
									setDirtyFlag(true);
								}
								if (StringUtils.isNotBlank(cl.getCellString("col_i")))
									btnTopic.addSclass("hasrecord");
								else
									btnTopic.removeSclass("hasrecord");
								for (BiCellCollection cl : firstTopicMap.values()) {
									if (StringUtils.isBlank(cl.getCellString("col_j")))
										cl.getCell("col_j").set(DEFAULT_PAGE_REMARK);
								}
								btn.closeMsgbox();
							}
						}).build().setTitle("編輯選票").appendStyle("width:100%;max-width:830px").setVboxStyle("margin:10px 0 10px 0").doModal();
						ZkUtil.delayJs(curComp, null, 100, "textArea.init('%s')", tbContent.getUuid());
						return;
					}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			if (p_ctype == GIPI_CELL_MAPPED) {
				if (StringUtils.equals(bcc.getCellLabel(), "vcol_topicbutton")) {
					Button btnTopic = (Button)((ZkBiCellValueMapper)bcc.getMapper()).getComponent();
					if (StringUtils.isNotBlank(cl.getCellString("col_i")))
						btnTopic.addSclass("hasrecord");
					else
						btnTopic.removeSclass("hasrecord");
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED && p_ctype != GIPI_PULLDOWN_OPENED && p_ctype != GIPI_PULLDOWN_CLOSED)
				setDirtyFlag(true);
		}
	}
	
}
