package com.uniinformation.zkbi.aw;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.erpv4.ZkBiComposerSalesAnalysis;

public class ZkBiComposerAwOrderAnalysis extends ZkBiComposerSalesAnalysis {
	@Override
    protected void setupDataAnalysisButton(final BiResult result) {
		super.setupDataAnalysisButton(result);
		{
			addReportButton(
					new AnalysisReport("Report A")
					.addRow("jm_cuser")
					.addCol("jm_period")
				,result);
//			currentRpt = rptList.get("By Category By Month");
			currentRpt = rptList.get("Report A");
		}
	}

}
