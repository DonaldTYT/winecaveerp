package com.uniinformation.zkbi.erpv4;

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

public class ZkBiComposerErpv4SalesAnalysis extends ZkBiComposerSalesAnalysis{
	@Override
    protected void setupDataAnalysisButton(final BiResult result) {
		super.setupDataAnalysisButton(result);
		{
			addReportButton(
					new AnalysisReport("By Category By Month")
					.addRow("st_iname")
					.addCol("ind_period")
					.addAggregate(AGGREGATES.SUM,"palc_totqty")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"palc_totqty")
					.addAggregate(AGGREGATES.SUM,"ind_netltotal")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_netltotal")
					.addAggregate(AGGREGATES.SUM,"ind_margin")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_margin")
//					.hideRow("st_mbrand", true)
					.hideAggregate("SUM(palc_totqty)", true)
					.hideAggregate("PERCENT_TOTAL(palc_totqty)", true)
					.hideAggregate("PERCENT_TOTAL(ind_netltotal)", true)
					.hideAggregate("SUM(ind_margin)", true)
					.hideAggregate("PERCENT_TOTAL(ind_margin)", true)
				,result);
			addReportButton(
					new AnalysisReport("By Customer By Month")
					.addRow("sm_name")
					.addRow("vd_addr3")
					.addRow("vd_vname")
					.addCol("ind_period")
					.hideCol("ind_period", true)
					.hideRow("vd_addr3", true)
					.hideRow("sm_name", true)
					.addAggregate(AGGREGATES.SUM,"palc_totqty")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"palc_totqty")
					.addAggregate(AGGREGATES.SUM,"ind_netltotal")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_netltotal")
					.addAggregate(AGGREGATES.SUM,"ind_margin")
					.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_margin")
					.hideAggregate("SUM(palc_totqty)", true)
					.hideAggregate("PERCENT_TOTAL(palc_totqty)", true)
					.hideAggregate("PERCENT_TOTAL(ind_netltotal)", true)
					.hideAggregate("SUM(ind_margin)", true)
					.hideAggregate("PERCENT_TOTAL(ind_margin)", true)
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
