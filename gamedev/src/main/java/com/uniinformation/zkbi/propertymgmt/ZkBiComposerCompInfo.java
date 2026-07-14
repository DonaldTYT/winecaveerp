package com.uniinformation.zkbi.propertymgmt;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbarbutton;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerCompInfo extends ZkBiComposerBase {
	private Timer tmGoDetail;

	@Override
	public void buildBrowserWindow(BiResult result, Component comp, int p_sortIdx, boolean p_sortDesc){
		super.buildBrowserWindow(result, comp, p_sortIdx, p_sortDesc);
		tmGoDetail = ZkUtil.timerEvent(tmGoDetail, comp, 100, () -> {
			if (result.getRowCount() == 1) {
				if (sessionHelper.isMobile()) {
					Listitem li = (Listitem)listbox.query("Listitem");
					if (li != null && li.hasAttribute("renderidx")) {
						int renderidx = (int)li.getAttribute("renderidx");
						listModelList.setSelection(Collections.singleton(listModelList.get(renderidx)));
						Events.postEvent(new SelectEvent<Listitem, Object>(Events.ON_SELECT, listbox, Collections.singleton(li), li));
						Events.echoEvent(Events.ON_CLICK, li, null);
					}
				} else {
					Toolbarbutton bt = (Toolbarbutton)listbox.query("Toolbarbutton");
					if (bt != null && bt.hasAttribute("trStat") && StringUtils.equals(bt.getTooltiptext(), sessionHelper.getTtLabel("Record Detail")))
						Events.echoEvent(Events.ON_CLICK, bt, null);
				}
			}
		});
	}

	@Override
    protected JxZkBiBase buildDetailWindow(BiResult result) {
		try {
			return super.buildDetailWindow(result);
		} finally {
			if (result.getRowCount() == 1) {
				Selectors.find("[id='btClose']").stream().findFirst().ifPresent(btClose -> ((Button)btClose).setDisabled(true));
				Selectors.find("[id='btUpdate']").stream().findFirst().ifPresent(btUpdate -> ((Button)btUpdate).setAttribute("NO_PROMPT_WITH_CONTINUE", "Y"));
			}
		}
    }
}
