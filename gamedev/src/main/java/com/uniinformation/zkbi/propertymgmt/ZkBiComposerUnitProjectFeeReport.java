package com.uniinformation.zkbi.propertymgmt;

import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Timer;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;
import static com.uniinformation.bicore.propertymgmt.BiResultPropertyMgmt.getAggregateIndex;

public class ZkBiComposerUnitProjectFeeReport extends ZkBiComposerAggregateReport {
	private Timer selectionTimer;

	@Override
    protected void onSelectionChanged(BiResult p_result, MultiSortMap sortMap, Component comp) throws Exception {
		super.onSelectionChanged(p_result, sortMap, comp);
    	String preset = (String)conditionPresetListbox.getSelectedItem().getValue();
    	selectionTimer = ZkUtil.timerEvent(selectionTimer, conditionPresetListbox, 100, () -> {
    		UniLog.log1("onSelectionChanged preset:%s", preset);
    		Selectors.find("#btExtraBatchAction_0").stream().findFirst().ifPresent(bt -> {
    			((Button)bt).addSclass("orange1");
    			bt.setVisible(Objects.equals(preset, "public_繳費通知單"));
    		});
   			Selectors.find("#btGeneralReport").forEach(bt -> bt.setVisible(!Objects.equals(preset, "public_繳費通知單")));
    	});
    }

	@Override
    protected void renderOneRecord_real(Listitem item, Object trStat, Vector listColumns, final BiResult br, int idx, Object ts) throws Exception {
		super.renderOneRecord_real(item, trStat, listColumns, br, idx, ts);
		List<Listcell> lcs = Selectors.find(item, "listcell").stream().map(c -> (Listcell)c).collect(Collectors.toList());
		Listcell lcl = lcs.stream().filter(c -> c.hasAttribute("bclabel")).reduce((a, b) -> b).orElse(null);
		int bclabelCol = lcs.indexOf(lcl);
		int periodCnt = br.getCellInt("upf_projectperiodcnt");
		Object[] eggValues = br.getAggregateValues(idx);
		for (int i = 1;; i++) {
			int eggIdx = getAggregateIndex(br, "vcol_unpaid" + i);
			if (eggIdx < 0)
				break;
			Listcell lc = lcs.get(bclabelCol + eggIdx + 1);
			if (i <= periodCnt) {
				double d = (double)eggValues[eggIdx];
				if (d == 0)
					lc.setLabel("\u2713");
			} else
				lc.setLabel("-");
		}
    }
}
