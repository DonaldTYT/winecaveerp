package com.uniinformation.zkbi.propertymgmt;

import java.util.Vector;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerPayment extends ZkBiComposerBase {

	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
   		super.doAfterCompose(comp);
   		Selectors.find("[id='btExtraBatchAction_0']").stream().findFirst().ifPresent(bt -> ((Button)bt).addSclass("orange1"));
   	}

	@Override
    protected JxZkBiBase buildDetailWindow(final BiResult result) {
		try {
			return super.buildDetailWindow(result);
		} finally {
			StreamSupport.stream(Selectors.find("[id^='btExtraJxFormAction_']").spliterator(), false).map(x -> (Button)x).forEach(bt -> {
				if (StringUtils.endsWith(bt.getId(), "_1"))
					bt.addSclass("lightgreen1");
				else if (StringUtils.endsWithAny(bt.getId(), "_2", "_3")) {
					bt.addSclass("orange1");
				}
			});
		}
    }

	@Override
    protected void renderOneRecord_real(Listitem item, Object trStat, Vector listColumns, final BiResult result, int idx, Object ts) throws Exception {
		super.renderOneRecord_real(item, trStat, listColumns, result, idx, ts);
		if (StringUtils.equalsAny(result.getCellString("ppm_name"), "待追繳費", "壞帳")) {
			StreamSupport.stream(item.queryAll("listcell").spliterator(), false).filter(comp -> comp.hasAttribute("bclabel")).map(comp -> (Listcell)comp).forEach(lc -> {
				ZkUtil.appendStyle(lc, "color:red");
			});
		}
    }

	@Override
    protected EventListener<Event> getImportButtonEventListener(BiResult result) {
		return new ImportWithReloadButtonEventListener(result, "zkf/propertymgmt/Fileuploaddlg.zul", "upload2");
	}

	@Override
	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		if (StringUtils.equals(bc.getLabel(), "col_a")) {
			ZkJxQueryInput ie = new ZkJxQueryInput();
			ie.useReadonlyTextMode();
			ie.setType(ZkJxQueryInput.TYPE_DATE, sessionHelper);
			return ie;
		}
		return super.buildAdvSearchInputComp(advSearch, result, bc, textFlag, multiPickSelectFlag);
	}
}
