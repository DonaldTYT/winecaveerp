package com.uniinformation.zkbi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiAuService.ZkBiAuEvent;

public class ZkBiComposerSelect2 extends ZkComposerBase{
	@Wire
	Listbox listbox1;
	@Wire
	Listbox listbox2;
	@Wire
	Listbox listbox3;
	@Wire
	Listbox listbox4;
	@Wire
	Listbox listbox5;
	
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		UniLog.log1("called");
		/*
		listbox1.setSelectedIndex(0);
		listbox2.setSelectedIndex(0);
		*/
		listbox3.setMultiple(true);
		listbox4.setMultiple(true);
		
		ZkUtil.js("console.log('listbox1:%s')", listbox1.getUuid());
		ZkUtil.js("console.log('listbox2:%s')", listbox2.getUuid());
		ZkUtil.js("console.log('listbox3:%s')", listbox2.getUuid());
		
		
		/*
		//code move to setupSelect2Listbox
		//call js to build select2 
		ZkUtil.delayJs(p_comp,null,50,"zkbis2.setup('%s')",listbox2.getUuid()); //andrew200811: need some delay to avoid duplicate list problem. //TODO: initialize select2 js without delay
		ZkUtil.delayJs(p_comp,null,50,"zkbis2.setup('%s',true)",listbox3.getUuid());
		ZkUtil.delayJs(p_comp,null,50,"zkbis2.setup('%s',true,true)",listbox4.getUuid());
		*/
		ZkUtil.setupSelect2(listbox2);
		ZkUtil.setupSelect2(listbox3);
		ZkUtil.setupSelect2(listbox4);
		ZkUtil.setupSelect2(listbox5);
		
		
		listbox1.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent>() {
			@Override
			public void onEvent(SelectEvent event) throws Exception {
				UniLog.log1("%s got event:%s,selectedItems:%s,selectedObjects:%s,previousSelectedItems:%s,previousSelectedObjects:%s", event.getTarget(), event, 
						event.getSelectedItems(), event.getSelectedObjects(), event.getPreviousSelectedItems(), event.getPreviousSelectedObjects());
				for (Listitem li : (Set<Listitem>)event.getSelectedItems()) {
					UniLog.log1("li label:%s value:%s", li.getLabel(), li.getValue());
				}
			}}
		);
		
		//TODO: when s2 updated, s2 event need to propagate the data back to zk
		listbox2.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent>() {
			@Override
			public void onEvent(SelectEvent event) throws Exception {
				UniLog.log1("%s got event:%s,selectedItems:%s,selectedObjects:%s,previousSelectedItems:%s,previousSelectedObjects:%s", event.getTarget(), event, 
						event.getSelectedItems(), event.getSelectedObjects(), event.getPreviousSelectedItems(), event.getPreviousSelectedObjects());
				for (Listitem li : (Set<Listitem>)event.getSelectedItems()) {
					UniLog.log1("li label:%s value:%s", li.getLabel(), li.getValue());
				}
			}}
		);
		
		//TODO: when s2 updated, s2 event need to propagate the data back to zk
		listbox3.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent>() {
			@Override
			public void onEvent(SelectEvent event) throws Exception {
				UniLog.log1("%s got event:%s,selectedItems:%s,selectedObjects:%s,previousSelectedItems:%s,previousSelectedObjects:%s", event.getTarget(), event, 
						event.getSelectedItems(), event.getSelectedObjects(), event.getPreviousSelectedItems(), event.getPreviousSelectedObjects());
				for (Listitem li : (Set<Listitem>)event.getSelectedItems()) {
					UniLog.log1("li label:%s value:%s", li.getLabel(), li.getValue());
				}
			}}
		);
		
		//TODO: when s2 updated, s2 event need to propagate the data back to zk
		listbox4.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent>() {
			@Override
			public void onEvent(SelectEvent event) throws Exception {
				UniLog.log1("%s got event:%s,selectedItems:%s,selectedObjects:%s,previousSelectedItems:%s,previousSelectedObjects:%s", event.getTarget(), event, 
						event.getSelectedItems(), event.getSelectedObjects(), event.getPreviousSelectedItems(), event.getPreviousSelectedObjects());
				for (Listitem li : (Set<Listitem>)event.getSelectedItems()) {
					UniLog.log1("li label:%s value:%s", li.getLabel(), li.getValue());
				}
			}}
		);
		
		listbox5.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent>() {
			@Override
			public void onEvent(SelectEvent event) throws Exception {
				UniLog.log1("%s got event:%s,selectedItems:%s,selectedObjects:%s,previousSelectedItems:%s,previousSelectedObjects:%s", event.getTarget(), event, 
						event.getSelectedItems(), event.getSelectedObjects(), event.getPreviousSelectedItems(), event.getPreviousSelectedObjects());
				for (Listitem li : (Set<Listitem>)event.getSelectedItems()) {
					UniLog.log1("li label:%s value:%s", li.getLabel(), li.getValue());
				}
			}}
		);
	}
}
