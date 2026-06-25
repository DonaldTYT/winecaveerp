package com.uniinformation.zkcomp;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Vlayout;

import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;

public class ZkBiButtonGroup {
	GMainButton mainButton = null;  //mainbutton
	ArrayList<Component> comps = new ArrayList();
	String imageSrc = "";
	String iconSclass = "";
	String label = "Button Group";
	String id = null;
	String tooltiptext;
	
	public ZkBiButtonGroup(SessionHelper sh) {
		tooltiptext = sh.getLabel("Click to expand button group");
	}
	
	public class GMainButton extends Button{
		Popup popup = null;
		
		/***
		 * capture the parent of the mainbutton
		 * 
		 */
		@Override
		public void setParent(Component p_parent) {
			UniLog.log1("called");
			super.setParent(p_parent);
			if (popup != null) {
				popup.setParent(p_parent);  //popup parent should be same as mainbutton parent
			}
		}
		
		public void setButtonGroupPopup(Popup p_popup) {
			popup = p_popup;
		}
	}
	public ZkBiButtonGroup addButton(Button p_button) {
		UniLog.log1("called");
		if (p_button == null) {
			UniLog.log1("ignore null button");
			return this;
			
		}
		comps.add(p_button);
		
		return this;
	}
	public ZkBiButtonGroup addSeparator() {
		//ignore if previous element is separator
		if (comps.size() == 0) return this;
		if (comps.get(comps.size() - 1) instanceof Separator) {
			return this;
		}
		
		Separator sep = new Separator();
		sep.setBar(true);
		comps.add(sep);
		return this;
	}
	public ZkBiButtonGroup setLabel(String p_label) {
		label = p_label;
		return this;
	}
	public ZkBiButtonGroup setTooltiptext(String p_tooltiptext) {
		tooltiptext = p_tooltiptext;
		return this;
	}
	
	public ZkBiButtonGroup setImage(String p_src) {
		imageSrc = p_src;
		return this;
	}
	public ZkBiButtonGroup setIconSclass(String p_sclass) {
		iconSclass = p_sclass;
		return this;
	}
	
	public ZkBiButtonGroup setId(String p_id) {
		id = p_id;
		return this;
	}
	public Button build() {
		if (mainButton != null) {
			UniLog.log1("called build twice, return old instance");
			return mainButton;
		}
		final Popup popup = new Popup();
		popup.appendChild(new Vlayout() {{
			for (Component comp : comps) {
				
				//add a extra click event for close the popup	
				if (comp instanceof Button) {
					comp.detach();
					appendChild(comp);
					((Button)comp).addEventListener("onClick", new EventListener() {
						@Override
						public void onEvent(Event event) throws Exception {
							UniLog.log1("close popup");
							popup.close();
						}});
				}
				else if (comp instanceof Separator) {
					comp.detach();
					appendChild(comp);
				}
			}
		}});
		
		mainButton = new GMainButton();
		
		if (StringUtils.isNotBlank(iconSclass)) { //set sclass if any
			mainButton.setIconSclass(iconSclass);
		}
		else if (StringUtils.isNotBlank(imageSrc)) { //set image if any
			mainButton.setImage(imageSrc);
		}
		else {  //default image
			mainButton.setImage("images/icons/zkweb/041-folder-25x25.png");
		}
		
		if (StringUtils.isNotBlank(label)) {
			mainButton.setLabel(label);
		}
		if (StringUtils.isNotBlank(id)) {
			mainButton.setId(id);
		}
		if (StringUtils.isNotBlank(tooltiptext)) {
			mainButton.setTooltiptext(tooltiptext);
		}
		
		mainButton.setButtonGroupPopup(popup);
		mainButton.addEventListener("onClick", new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				//popup.open(mainButton, "top_left");
				popup.open(mainButton, "at_pointer");
			}
			
		});
		return mainButton;
	}

}
