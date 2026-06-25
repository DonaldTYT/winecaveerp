package com.uniinformation.webcore;

import java.util.ArrayList;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

public class HlayoutHelper extends Hlayout {
	public HlayoutHelper add(Component...p_comps) {
		if (p_comps == null) return this;
		for (int i=0; i<p_comps.length; i++) {
			this.appendChild(p_comps[i]);
		}
		return this;
	}
}