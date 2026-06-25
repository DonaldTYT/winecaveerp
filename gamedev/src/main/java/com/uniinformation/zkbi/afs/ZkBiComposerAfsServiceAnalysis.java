package com.uniinformation.zkbi.afs;

import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.ZkBiComposerAnalysis.AGGREGATES;
import com.uniinformation.zkbi.erpv4.ZkBiComposerSalesAnalysis;

public class ZkBiComposerAfsServiceAnalysis extends ZkBiComposerSalesAnalysis{
		@Override
	    protected void setupDataAnalysisButton(final BiResult result) {
			super.setupDataAnalysisButton(result);
			{
				addReportButton(
						new AnalysisReport("By Location By Month")
						.addRow("svloc_city")
						.addRow("svloc_desp")
						.addRow("svmc_name")
						.addRow("svjob_status")
						.addCol("svjob_mofdate")
						.addCol("svjob_sertype")
						.hideRow("svloc_city",true)
						.hideRow("svloc_desp",true)
						.hideRow("svmc_name",true)
						.hideRow("svjob_status",true)
						.hideCol("svjob_sertype",true)
						.addAggregate(AGGREGATES.SUM,"svjob_worktime")
						.addAggregate(AGGREGATES.PERCENT_TOTAL,"svjob_worktime")
						.hideAggregate("PERCENT_TOTAL(svjob_worktime)",true)
					,result);
				addReportButton(
						new AnalysisReport("By Machine By Month")
						.addRow("svmc_name")
						.addRow("st_modelno")
						.addRow("svmc_serno")
						.addRow("svjob_status")
						.addCol("svjob_mofdate")
						.addCol("svjob_sertype")
						.hideRow("svmc_name",true)
						.hideRow("st_modelno",true)
						.hideRow("svmc_serno",true)
						.hideCol("svjob_sertype",true)
						.addAggregate(AGGREGATES.SUM,"svjob_worktime")
						.addAggregate(AGGREGATES.PERCENT_TOTAL,"svjob_worktime")
						.hideAggregate("PERCENT_TOTAL(svjob_worktime)",true)
					,result);
//				addReportButton(
//						new AnalysisReport("By Customer By Month")
//						.addRow("sm_name")
//						.addRow("vd_vname")
//						.addCol("ind_period")
//						.hideCol("ind_period", true)
//						.hideRow("vd_vname", true)
//						.hideRow("sm_name", true)
//						.addAggregate(AGGREGATES.SUM,"palc_totqty")
//						.addAggregate(AGGREGATES.PERCENT_TOTAL,"palc_totqty")
//						.addAggregate(AGGREGATES.SUM,"ind_netltotal")
//						.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_netltotal")
//						.addAggregate(AGGREGATES.SUM,"ind_margin")
//						.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_margin")
//						.hideAggregate("SUM(palc_totqty)", true)
//						.hideAggregate("PERCENT_TOTAL(palc_totqty)", true)
//						.hideAggregate("PERCENT_TOTAL(ind_netltotal)", true)
//						.hideAggregate("SUM(ind_margin)", true)
//						.hideAggregate("PERCENT_TOTAL(ind_margin)", true)
//					,result);
//				currentRpt = rptList.get("By Category By Month");
				currentRpt = rptList.get("By Location By Month");
			}
		}
		@Override
		protected void processAnalysizedData(JSONObject p_data,BiResult p_result) throws JSONException{
			super.processAnalysizedData(p_data,p_result);
			rgChartType.setSelectedIndex(1);
			drawOneChart(p_data);
		}
}
