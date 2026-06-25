package com.uniinformation.zkcomp;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.NoDOM;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;

import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiButton extends Button {
	public ZkBiButton() {
		this(null, null, null, null, null);
	}
	public ZkBiButton(java.lang.String label) {
		this(label, null, null, null, null);
	}
	public ZkBiButton(java.lang.String label, java.lang.String image) {
		this(label, image, null, null, null);
	}
	public ZkBiButton(java.lang.String label, java.lang.String image, String id) {
		this(label, image, id, null, null);
	}
	public ZkBiButton(java.lang.String label, java.lang.String image, String id, String tooltip) {
		this(label, image, id, tooltip, null);
	}
	/***
	 * 
	 * @param label
	 * @param image
	 * @param id
	 * @param tooltip
	 * @param sh - optional. for translate only
	 */
	public ZkBiButton(java.lang.String label, java.lang.String image, String id, String tooltip, SessionHelper sh) {
		super();
		if (StringUtils.isNotBlank(label)){
			this.setLabel(sh == null ? label : sh.getBtLabel(label));
		}
		if (StringUtils.isNotEmpty(image)) {
			this.setImage(image);
		}
		if (StringUtils.isNotBlank(id)) {
			this.setId(id);
		}
		if (StringUtils.isNotBlank(tooltip)) {
			this.setTooltip(sh == null ? tooltip : sh.getLabel(tooltip));
		}
	}
	
	@Override
	public void setDisabled(boolean disabled) {
		super.setDisabled(disabled);
		UniLog.log1("called %s", disabled);
		ZkUtil.sendDesktopEvent(MapUtil.of("action", disabled ? "BUTTON_DISABLE" : "BUTTON_ENABLE" ,"comp", this));
	}
}
