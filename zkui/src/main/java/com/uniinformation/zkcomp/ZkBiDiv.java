package com.uniinformation.zkcomp;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Label;
import org.zkoss.zul.NoDOM;
import org.zkoss.zul.Span;
import org.zkoss.zul.Textbox;

import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class ZkBiDiv extends ZkBiComponent /* implements IdSpace, AfterCompose */ {
	public ZkBiDiv() {
		super("/WEB-INF/zkcomp/zkbidiv.zul");
	}
	public ZkBiDiv(String p_zul) {
		super(p_zul);
	}
	@Override
	public void setJxId(String p_id) {
		super.setJxId(p_id);
		if(compDiv != null) {
			compDiv.setId("compdiv_"+p_id);
		}
	}
	
	public void setLabelPosition(String p_position) {
		if(p_position.equals("invisible")) {
			lbDiv.setVisible(false);
		}
	}
	
	public void setContainerStyle(String style) {
		if (lbDiv.getParent() instanceof Hlayout)
			((Hlayout)lbDiv.getParent()).setStyle(style);
	}

	public void setContainerSclass(String sclass) {
		if (lbDiv.getParent() instanceof Hlayout)
			((Hlayout)lbDiv.getParent()).setSclass(sclass);
	}

	public void setContainerVisible(boolean b) {
		if (lbDiv.getParent() instanceof Hlayout)
			((Hlayout)lbDiv.getParent()).setVisible(false);
	}
}
