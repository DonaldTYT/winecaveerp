package com.uniinformation.zkbi.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Timer;
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
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerPayitemReport extends ZkBiComposerAggregateReport {
	private Listheader contactComp;
	private boolean isContactVisible;
	private Timer displayContactColumnTimer;

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
        		Matcher m = Pattern.compile("col_d between '([\\d-]+)' and '([\\d-]+)'").matcher(condition);
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
        if (presetName.contains("繳費明細報表(管理費)"))
        	generateReportDesignRes = "Propertymgmt_Rptpayitem_Mgt.rptdesign";
        else if (presetName.contains("繳費明細報表(儲備金)"))
        	generateReportDesignRes = "Propertymgmt_Rptpayitem_Res.rptdesign";
		generateReportUserPropMap = MapUtil.of("CompanyZhName", coMap.get("co_coname"), "CompanyEnName", coMap.get("co_chnname"), 
							"CompanyAddress", Erpv4Config.getCoAddr(sessionHelper, cocode), 
							"CompanyTelInfo", String.format("電話 TEL: %s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;傳真 FAX: %s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")),
							"MonthRange", monthRange, "PrintTime", sdf2.format(new Date()));

		super.printGenerateReport(result);
	}

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		defaultColumnOrders = Lists.newArrayList("payment_col_a");
		super.doAfterCompose(p_comp);
	}
	
	@Override
	public void visibleCols(String preset, Component comp,BiResult result) {
		super.visibleCols(preset, comp, result);
		setupContactVisibleParam(result);
    }
	
	@Override
	protected void changedDisplayListColumn(BiResult result) {
		super.changedDisplayListColumn(result);
		setupContactVisibleParam(result);
    	displayContactColumn(result);
	}
	
	@Override
    public void refresh(final BiResult result, final Component p_comp, MultiSortMap sortMap, boolean p_doSearch) {
    	super.refresh(result, p_comp, sortMap, p_doSearch);
    	displayContactColumn(result);
    }

	private void setupContactVisibleParam(BiResult result) {
		Vector<BiColumn> cls = result.getListColumns();
   		Listhead lh = (Listhead) masterWin.query("#browser_listhead");
    	IntStream.range(0, cls.size()).mapToObj(i -> Pair.of(i, cls.get(i)))
    			.filter(p -> StringUtils.equals(p.getValue().getLabel(), "pm_col_k"))
    			.findFirst()
    			.ifPresent(p -> {
					Listheader lhdr = (Listheader) lh.getFellowIfAny("browser_listheader_"+(p.getKey()+1));
    				contactComp = lhdr;
    				isContactVisible = lhdr.isVisible();
    				lhdr.setAttribute("customVisible", isContactVisible);
    				UniLog.log1("isContactVisible:%b", isContactVisible);
    			});
	}

	private void displayContactColumn(BiResult result) {
		displayContactColumnTimer = ZkUtil.timerEvent(displayContactColumnTimer, masterWin, "Loading", 100, () -> {
    		UniLog.log1("refresh isContactVisible: %b", isContactVisible);
    		if (contactComp != null && isContactVisible) {
    			boolean flag = IntStream.range(0, result.getRowCount()).anyMatch(i -> {
    			            result.loadOneRecV(i);
    			            return StringUtils.isNotBlank(result.getCellString("pm_col_k"));
 			    		});
    			boolean b = result.getRowCount() == 0 || flag;
    			if (b != contactComp.isVisible()) {
    				contactComp.setVisible(b);
    				Clients.resize(listbox);
    			}
    		}
    	});
	}

	@Override
	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		if (StringUtils.equals(bc.getLabel(), "col_d")) {
			try {
				ZkJxQueryInput ie = new ZkJxQueryInput();
				ie.setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				TrGetItemProperty gipi = new TrGetItemProperty(Lists.newArrayList("col_d"));
				if (!getSessionHelper().hasAccessRight("#allproperty")) {
					String ss = StringUtils.defaultString(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct col_d from payitem where col_b = ? order by col_d", new Wherecl().appendArgument(ss)));
				} else
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct col_d from payitem order by col_d"));
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
