package com.uniinformation.zkbi.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.impl.XulElement;

import com.google.common.collect.Lists;
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

public class ZkBiComposerMonthPaymentReport extends ZkBiComposerAggregateReport {

	@Override
    protected void printGenerateReport(BiResult result) {
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String presetName = getCurrentPresetName();
		UniLog.log1("presetName:%s", presetName);

        String cocode = Erpv4Config.getDefaultCoCode(sessionHelper);
        Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sessionHelper, cocode);

        String monthRange = "";
        if (result.getCustomCondition() != null) {
        	try {
        		String condition = StringUtils.defaultString(result.getCustomCondition().toString());
        		UniLog.log1("condition:%s", condition);
        		Matcher m = Pattern.compile("mpy_month between '([\\d-]+)' and '([\\d-]+)'").matcher(condition);
        		if (m.find()) {
        			UniLog.log1("matcher found:%s,%s", m.group(1), m.group(2));
        			Date startDate = sdf.parse(m.group(1) + "-01");
        			Date endDate = sdf.parse(m.group(2) + "-01");
        			monthRange = String.format("%s 至 %s", sdf1.format(startDate), sdf1.format(endDate));
        		}
        	}
        	catch (Exception ex) {
        		UniLog.log(ex);
        	}
        }

        generateReportSettingMap = MapUtil.of("outFileName", ReportGenerate.generateOutputFileName(presetName.replace("(public)", "")));
        if (presetName.contains("單位欠繳明細報表(管理費)"))
        	generateReportDesignRes = "Propertymgmt_RptUnitArrears_Mgt.rptdesign";
        else if (presetName.contains("單位欠繳明細報表(儲備金)"))
        	generateReportDesignRes = "Propertymgmt_RptUnitArrears_Res.rptdesign";
		generateReportUserPropMap = MapUtil.of("CompanyZhName", coMap.get("co_coname"), "CompanyEnName", coMap.get("co_chnname"), 
							"CompanyAddress", Erpv4Config.getCoAddr(sessionHelper, cocode), 
							"CompanyTelInfo", String.format("電話 TEL: %s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;傳真 FAX: %s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")),
							"MonthRange", monthRange, "PrintTime", sdf2.format(new Date()));

		super.printGenerateReport(result);
	}

	@Override
	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		if (StringUtils.equals(bc.getLabel(), "mpy_month")) {
			try {
				ZkJxQueryInput ie = new ZkJxQueryInput();
				ie.setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				TrGetItemProperty gipi = new TrGetItemProperty(Lists.newArrayList("mpy_month"));
				if (!getSessionHelper().hasAccessRight("#allproperty")) {
					String ss = StringUtils.defaultString(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct mpy_month from monthpayment where mpy_propertyname = ? order by mpy_month", new Wherecl().appendArgument(ss)));
				} else
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct mpy_month from monthpayment order by mpy_month"));
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
		if (StringUtils.equalsAny(bc.getLabel(), "vcol_mgtstat", "vcol_resstat"))
			re.setAttribute("stringListboxWidth", "348px");
		return re;
	}
}
