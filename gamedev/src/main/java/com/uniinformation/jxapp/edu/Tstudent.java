package com.uniinformation.jxapp.edu;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.ZkForm;

public class Tstudent extends JxZkBiBase{
	protected String zkfName = "zkf/edu/TstudentDetail.zul";
	ZkForm zkf = null;
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		JxField fd = jxAdd("estd_detail");
		CellCollection col = p_br.getCurrentCollection().getCollection("estd_jsoncc");
		if(fd != null && col != null) {
			Component comp = (Component) fd.getNativeObject();
			if(zkf != null) {
				comp.removeChild(zkf.getRootComponent());
				zkf = null;
			}
			Div d = new Div();
			comp.appendChild(d);
			zkf = new ZkForm(d,zkfName); 
			try {
				zkf.mapCellCollection(col, new EventListener() {

					@Override
					public void onEvent(Event event) throws Exception {
						setDirtyFlag(true);
					}
					
				});
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}

}
