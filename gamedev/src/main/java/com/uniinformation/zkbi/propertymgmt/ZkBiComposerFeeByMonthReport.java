package com.uniinformation.zkbi.propertymgmt;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zul.impl.XulElement;

import com.google.common.collect.Lists;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.birt.ReportGenerate;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerFeeByMonthReport extends ZkBiComposerAggregateReport {

	@Override
    protected void printGenerateReport(BiResult result) {
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		final DecimalFormat df = new DecimalFormat("$#,##0.00");
		final DecimalFormat df1 = new DecimalFormat("#,##0.00");
		String presetName = getCurrentPresetName();
		UniLog.log1("presetName:%s", presetName);

        String cocode = Erpv4Config.getDefaultCoCode(sessionHelper);
        Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sessionHelper, cocode);

        String monthRange = "";
        String monthCount = "";
        if (result.getCustomCondition() != null) {
        	try {
        		String condition = StringUtils.defaultString(result.getCustomCondition().toString());
        		UniLog.log1("condition:%s", condition);
        		Matcher m = Pattern.compile("col_c between '([\\d-]+)' and '([\\d-]+)'").matcher(condition);
        		if (m.find()) {
        			UniLog.log1("matcher found:%s,%s", m.group(1), m.group(2));
        			Date startDate = sdf.parse(m.group(1) + "-01");
        			Date endDate = sdf.parse(m.group(2) + "-01");
        			monthRange = String.format("%s 至 %s", sdf1.format(startDate), sdf1.format(endDate));
        			int cc = 0;
        			for (Date d = startDate; d.compareTo(endDate) <= 0; d = DateUtil.nextmonth(d))
        				cc++;
        			monthCount = String.valueOf(cc);
        		}
        	}
        	catch (Exception ex) {
        		UniLog.log(ex);
        	}
        }

        generateReportSettingMap = MapUtil.of("outFileName", ReportGenerate.generateOutputFileName(presetName.replace("(public)", "")));
        if (presetName.contains("管理費統計報表 (按月) "))
        	generateReportDesignRes = "Propertymgmt_Rptfeebymonth_Mgt.rptdesign";
        else if (presetName.contains("儲備金統計報表 (按月)"))
        	generateReportDesignRes = "Propertymgmt_Rptfeebymonth_Res.rptdesign";
		generateReportUserPropMap = MapUtil.of("CompanyZhName", coMap.get("co_coname"), "CompanyEnName", coMap.get("co_chnname"), 
							"CompanyAddress", Erpv4Config.getCoAddr(sessionHelper, cocode), 
							"CompanyTelInfo", String.format("電話 TEL: %s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;傳真 FAX: %s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")),
							"MonthRange", monthRange, "MonthCount", monthCount, "TotalMgmFee", "", "MonthlyRec", "", "TotalRec", "", "TotalPayRate", "",
							"PrintTime", sdf2.format(new Date()));
		
		generateReportCallback = new ReportGenerate.Callback() {
			@Override
			public void beforeAddUserProp(ReportGenerate rptGen) {
				UniLog.log("beforeAddUserProp");
				Map<String, Double> m = rptGen.getAggregateSubtotalValueMap();
				double totalMgmFee = 0, totalRec = 0;
				if (presetName.contains("管理費統計報表 (按月) ")) {
					for (Map.Entry<String, Double> entry : m.entrySet()) {
						String key = entry.getKey();
						double value = entry.getValue();
						if (key.contains("已收管理費總數"))
							totalRec += value;
						if (key.contains("每月管理費應收"))
							totalMgmFee += value;
					}
					generateReportUserPropMap.put("TotalMgmFee", df.format(totalMgmFee));
					generateReportUserPropMap.put("TotalRec", df.format(totalRec));
					Double totalPayRate = m.get("繳費率(%)");
					if (totalPayRate != null)
						generateReportUserPropMap.put("TotalPayRate", df1.format(totalPayRate) + "%");
				} else if (presetName.contains("儲備金統計報表 (按月)")) {
					for (Map.Entry<String, Double> entry : m.entrySet()) {
						String key = entry.getKey();
						double value = entry.getValue();
						if (key.contains("已收儲備金總數"))
							totalRec += value;
						if (key.contains("每月儲備金應收"))
							totalMgmFee += value;
					}
					generateReportUserPropMap.put("TotalMgmFee", df.format(totalMgmFee));
					generateReportUserPropMap.put("TotalRec", df.format(totalRec));
					Double totalPayRate = m.get("繳費率(%)_1");
					if (totalPayRate != null)
						generateReportUserPropMap.put("TotalPayRate", df1.format(totalPayRate) + "%");
				}
			}
		};
		super.printGenerateReport(result);
    }

	@Override
	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		if (StringUtils.equals(bc.getLabel(), "col_c")) {
			try {
				ZkJxQueryInput ie = new ZkJxQueryInput();
				ie.setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				TrGetItemProperty gipi = new TrGetItemProperty(Lists.newArrayList("col_c"));
				if (!getSessionHelper().hasAccessRight("#allproperty")) {
					String ss = StringUtils.defaultString(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct col_c from contractmonth where col_a = ? order by col_c", new Wherecl().appendArgument(ss)));
				} else
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct col_c from contractmonth order by col_c"));
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
