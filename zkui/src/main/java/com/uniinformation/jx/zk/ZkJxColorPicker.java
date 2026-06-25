package com.uniinformation.jx.zk;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

public class ZkJxColorPicker extends Combobox {
	void addColorItem(String p_name,int p_color) {
		Comboitem ci = new Comboitem(p_name);
		ci.setValue(p_color);
		appendChild(ci);
	}
	public ZkJxColorPicker() {
		addColorItem("Black",0x000000);
		addColorItem("Red",0xff0000);
		addColorItem("Blue",0x00ff00);
		addColorItem("Green",0x0000ff);
		setSelectedIndex(0);
	}
}
