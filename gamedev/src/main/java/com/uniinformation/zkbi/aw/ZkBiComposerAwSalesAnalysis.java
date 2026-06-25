package com.uniinformation.zkbi.aw;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Vbox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.ZkReportHelper;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.erpv4.ZkBiComposerSalesAnalysis;

public class ZkBiComposerAwSalesAnalysis extends ZkBiComposerSalesAnalysis{
	@Override
    protected void setupDataAnalysisButton(final BiResult result) {
		super.setupDataAnalysisButton(result);
		{
			addReportButton(
					new AnalysisReport("By Customer By Month")
					.addRow("inv_smcode")
					.addRow("vd_vname")
					.addCol("inv_period")
					.hideCol("inv_period", true)
					.hideRow("vd_vname", true)
					.addAggregate(AGGREGATES.SUM,"inv_total")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"inv_total")
					.addAggregate(AGGREGATES.SUM,"inv_cost")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"inv_cost")
//					.addAggregate(AGGREGATES.SUM,"inv_margin")
//					.addAggregate(AGGREGATES.PERCENT_TOTAL,"inv_margin")
					.hideAggregate("SUM(inv_cost)", true)
					.hideAggregate("PERCENT_TOTAL(inv_cost)", true)
//					.hideAggregate("SUM(inv_margin)", true)
//					.hideAggregate("PERCENT_TOTAL(inv_margin)", true)
				,result);
//			currentRpt = rptList.get("By Category By Month");
			currentRpt = rptList.get("By Customer By Month");
		}
	}
	@Override
	protected void processAnalysizedData(JSONObject p_data,BiResult p_result) throws JSONException{
		super.processAnalysizedData(p_data,p_result);
		rgChartType.setSelectedIndex(1);
		drawOneChart(p_data);
	}
}
