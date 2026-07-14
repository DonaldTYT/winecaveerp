package com.uniinformation.zkbi.propertymgmt;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Timer;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;

public class ZkBiComposerMonthParkingTmpLocReport extends ZkBiComposerAggregateReport {
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
	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		if (Objects.equals(bc.getLabel(), "mpt_month")) {
			try {
				ZkJxQueryInput ie = new ZkJxQueryInput();
				ie.setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				TrGetItemProperty gipi = new TrGetItemProperty(Arrays.asList("mpt_month"));
				if (!getSessionHelper().hasAccessRight("#allproperty")) {
					String ss = StringUtils.defaultString(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct mpt_month from monthparkingtl where mpt_building = ? order by mpt_month", new Wherecl().appendArgument(ss)));
				} else
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct mpt_month from monthparkingtl order by mpt_month"));
				ie.setAttribute("stringListboxWidth", "139px");
				ie.setGiPi(gipi);
				ie.setMaxlength(bc.getColumnLength());
				return ie;
			} catch (Exception e) {
				UniLog.log(e);
				return null;
			}
		}
		XulElement re = super.buildAdvSearchInputComp(advSearch, result, bc, textFlag, multiPickSelectFlag);
		if (Objects.equals(bc.getLabel(), "vcol_status"))
			re.setAttribute("stringListboxWidth", "348px");
		return re;
	}
}
