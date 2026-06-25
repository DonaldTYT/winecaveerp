package com.uniinformation.zkcomp;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.NoDOM;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;

import com.uniinformation.utils.UniLog;

public class ZkBiIntbox extends ZkBiComponent /* implements IdSpace, AfterCompose */ {
	public ZkBiIntbox() {
		super("/WEB-INF/zkcomp/zkbiintbox.zul");
	}
}
