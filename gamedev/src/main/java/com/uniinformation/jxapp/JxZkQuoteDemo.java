package com.uniinformation.jxapp;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;

import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;

public class JxZkQuoteDemo extends ZkComposerBase{
	@Wire
	Textbox textbox1;
	@Wire
	Textbox textbox2;
	
	@Wire
	Radiogroup radiogroup1;
	
	@Wire
	Checkbox checkbox1;
	@Wire
	Checkbox checkbox2;
	@Wire
	Checkbox checkbox3;
	
	@Override
   	public void doAfterCompose(Component comp) throws Exception { 
   		super.doAfterCompose(comp);
   		
		textbox1.setInstant(true);
		textbox1.addEventListener(Events.ON_CHANGE, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				textbox2.setValue(textbox1.getValue());
			}
			
		});
		
		radiogroup1.addEventListener(Events.ON_CHECK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				textbox2.setValue("radio:"+radiogroup1.getSelectedItem().getLabel());
			}
				
		});
		
		checkbox1.addEventListener(Events.ON_CHECK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				textbox2.setValue("checkbox:"+checkbox1.getLabel() + ":" + checkbox1.isChecked());
			}
				
		});
		checkbox2.addEventListener(Events.ON_CHECK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				textbox2.setValue("checkbox:"+checkbox2.getLabel() + ":" + checkbox2.isChecked());
			}
				
		});
		checkbox3.addEventListener(Events.ON_CHECK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				textbox2.setValue("checkbox:"+checkbox3.getLabel() + ":" + checkbox3.isChecked());
			}
		});
   	}
	
	@Override
	protected boolean validateURL(String p_url) {
		return true;
		//return sessionHelper.isAdminUser();
	}

}
