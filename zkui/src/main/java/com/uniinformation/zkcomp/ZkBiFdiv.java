package com.uniinformation.zkcomp;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Div;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Label;
import org.zkoss.zul.NoDOM;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;

import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

/***
 *  This class based on ZkBiDiv with different zul file
 *  If need to add extra feature, should added to parent class
 *
 */
public class ZkBiFdiv extends ZkBiDiv {
	public ZkBiFdiv() {
		super("/WEB-INF/zkcomp/zkbifdiv.zul");
	}
}
