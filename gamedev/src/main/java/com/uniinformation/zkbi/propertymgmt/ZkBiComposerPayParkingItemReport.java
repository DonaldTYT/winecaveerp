package com.uniinformation.zkbi.propertymgmt;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;

public class ZkBiComposerPayParkingItemReport extends ZkBiComposerAggregateReport {

	@Override
	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		if (Objects.equals(bc.getLabel(), "col_e")) {
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
		return super.buildAdvSearchInputComp(advSearch, result, bc, textFlag, multiPickSelectFlag);
	}
}
