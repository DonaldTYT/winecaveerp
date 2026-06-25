package com.uniinformation.jxapp.edu;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.ZkForm;

public class EduQnr extends JxZkBiBase {
	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
	}

	private ZkForm zkf;
	private String zkfName;
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		
		jxSetVisible("row_eqnr_jsonstr", sessionHelper.isAdminUser());
		if (mode == JxZkBiBase.MODE_ADD) {
			try {
				p_br.getCell("eqnr_qnrtype").set(EduQnrReplySlip.QNR_TYPE);
			} catch (CellException e) {
				e.printStackTrace();
			}
		}

		JxField fd = jxAdd("eqnr_asdiv");  
		CellCollection col = p_br.getCurrentCollection().getCollection("eqnr_jsoncc");
		if(fd != null && col != null) {
			final Component comp = (Component) fd.getNativeObject();
			if (zkf != null) {
				comp.removeChild(zkf.getRootComponent());
				zkf = null;
				zkfName = null;
			}
			Component parentcomp = comp.getParent();
			if (parentcomp instanceof Row) {
				Row row = (Row) parentcomp;
				row.setSpans("2");
				final Label label = (Label) row.query("#lb_eqnr_asdiv");
				row.removeChild(label);
				row.removeChild(comp);
				row.appendChild(new Div() {{
					setStyle("text-align:left");
					appendChild(comp);
				}});
			}

			if (StringUtils.equals(p_br.getCellString("eqnr_qnrtype"), EduQnrReplySlip.QNR_TYPE))
				zkfName = "zkf/edu/eduQnrReplySlipAs.zul";
			UniLog.log1("eqnr_qnrtype:%s, zkfName:%s", p_br.getCellString("eqnr_qnrtype"), zkfName);
			if (StringUtils.isNotBlank(zkfName)) {
				Div d = new Div();
				comp.appendChild(d);
				zkf = new ZkForm(d, zkfName); 
				try {
					zkf.mapCellCollection(col, new EventListener() {
						@Override
						public void onEvent(Event event) throws Exception {
							//set the form to dirty when got a zkform event
							UniLog.log1("event class:%s, name:%s, target:%s", event.getClass(), event.getName(), event.getTarget());
							if (!(event.getTarget() instanceof Textbox || event.getTarget() instanceof Radiogroup || event.getTarget() instanceof Radio || event.getTarget() instanceof Checkbox
									|| event.getTarget() instanceof Datebox))
								return;
							setDirtyFlag(true);
						}
						
					});
				} catch (CellException cex) {
					UniLog.log(cex);
				}
			}
		}
	}
}
