package com.uniinformation.jxapp.propmgmtpro;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
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
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;

public class TmpVoteInput extends JxZkBiBase {
	private Set<String> voteList;
	private Map<BiCellCollection, Set<String>> unitVoteMap;
	private Vector<BiCellCollection> signinList;

	@Override
	public void afterBind() {
		super.afterBind();
		Listbox lb = (Listbox)jxAdd("list_propmgmtpro_TmpVoteInputDet").getNativeObject();
		ZkUtil.setEventListener(lb, "onItemRendererCallback", event -> {
			Map<String, Object> m = (Map<String, Object>)event.getData();
			Listitem listItem = (Listitem)m.get("listItem");
			int idx = (int)m.get("idx");
			Object data = m.get("data");
			UniLog.log1("onItemRendererCallback event:%s, idx:%d", event, idx);
          	if (data instanceof TrStatFilter)
           		data = ((TrStatFilter)data).getTrStatIdx();
          	BiResult brDet = getBr().getSubLink("propmgmtpro.TmpVoteInputDet");
          	BiCellCollection bcc = brDet.getRowCollectionO(data);
          	Listcell lc = (Listcell)listItem.getFirstChild();
          	lc.setStyle("text-align:center");
			Toolbarbutton b = new Toolbarbutton() {{
				setIconSclass("z-icon-pencil z-icon-2x");
    			setSclass("narrowtoolbarbutton");
				setStyle("opacity:0.7");
				setTooltiptext("編輯臨時選項");
				setVisible(StringUtils.equalsAny(bcc.getCellString("col_d"), "Y", "A"));
				setParent(lc);
			}};
			b.addEventListener(Events.ON_CLICK, ev -> {
				Div div = (Div)curComp.getTemplate("editVoteTemplate").create(null, null, null, null)[0];
				Label lbUnit = ZkUtil.getFellowWithNullId(div, "lbUnit");
				lbUnit.setValue(bcc.getCellString("col_c"));
				Set<String> vlist = unitVoteMap.get(bcc);
				for (String s : voteList) {
					Button bt = new Button(s);
					bt.setAttribute("code", s);
					if (!vlist.contains(s)) {
						bt.setAttribute("unselect", true);
						bt.addSclass("unselect");
					}
					bt.addEventListener(Events.ON_CLICK, evbt -> {
						if (bt.hasAttribute("unselect")) {
							bt.removeAttribute("unselect");
							bt.removeSclass("unselect");
						} else {
							bt.setAttribute("unselect", true);
							bt.addSclass("unselect");
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
						Set<String> selectedList = StreamSupport.stream(div.queryAll("button").spliterator(), false)
													.filter(bt -> !bt.hasAttribute("unselect"))
													.map(bt -> (String)bt.getAttribute("code"))
													.collect(Collectors.toCollection(TreeSet::new));
						if (!selectedList.equals(vlist)) {
							vlist.clear();
							vlist.addAll(selectedList);
							bcc.getCell("col_g").set(String.join(",", vlist));
							sortDetListbox();
							setDirtyFlag(true);
						}
						btn.closeMsgbox();
					}
				}).build().setTitle("編輯臨時選項").appendStyle("width:100%;max-width:570px").setVboxStyle("margin:10px 0 10px 0").doModal();
			});
		});
		lb.setAttribute("hasOnItemRendererCallback", true);
	}

	@Override
	public void bindCellCollection(BiResult c, int mode) {
		super.bindCellCollection(c, mode);
		try {
			BiResult brMettingTmpTopic = BiResultHelper.create(sessionHelper, "propmgmtpro.MettingTmpTopic", String.format("col_a = %d and col_b = '%s'", getBr().getCellInt("col_a"), getBr().getCellString("col_b")), -1, null);
			List<Map<String, Object>> list = ZkUtil.getBiResultRecordMap(brMettingTmpTopic, false);
			voteList = list.stream().map(m -> (String)m.get("col_c")).collect(Collectors.toCollection(TreeSet::new));

			signinList = getBr().getSubLinkResult("propmgmtpro.TmpVoteInputDet");
			unitVoteMap = new HashMap<>();
			for (BiCellCollection bcc : signinList) {
				Set<String> ss = Arrays.stream(StringUtils.split(bcc.getCellString("col_g"), ",")).collect(Collectors.toCollection(TreeSet::new));
				unitVoteMap.put(bcc, ss);
			}
			brMettingTmpTopic.close();
			UniLog.log1("voteList:%s", voteList);
			sortDetListbox();
			Listbox lb = (Listbox)jxAdd("list_propmgmtpro_TmpVoteInputDet").getNativeObject();
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
		Listbox lb = (Listbox)jxAdd("list_propmgmtpro_TmpVoteInputDet").getNativeObject();
		BiResult br = getBr().getSubLink("propmgmtpro.TmpVoteInputDet");
		ListModelList<Object> model = (ListModelList<Object>)lb.getModel();
		model.sort((a, b) -> {
          	if (a instanceof TrStatFilter)
           		a = ((TrStatFilter)a).getTrStatIdx();
          	if (b instanceof TrStatFilter)
           		b = ((TrStatFilter)b).getTrStatIdx();
			int i1 = StringUtils.isNotBlank(br.getRowCollectionO(a).getCellString("col_g")) ? 1 : 0;
			int i2 = StringUtils.isNotBlank(br.getRowCollectionO(b).getCellString("col_g")) ? 1 : 0;
			return i2 - i1;
		});
	}

	private EventListener<InputEvent> footFilterListener = (event) -> {
		sortDetListbox();
	};
}
