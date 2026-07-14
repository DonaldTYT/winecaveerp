package com.uniinformation.zkbi.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.XulElement;

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
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiComposerAggregateReport;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkcomp.ZkBiButton;
import static com.uniinformation.utils.ZkUtil.throwFunction;

public class ZkBiComposerMonthPaymentReport extends ZkBiComposerAggregateReport {
	private Timer selectionTimer;

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
    protected void onSelectionChanged(BiResult p_result, MultiSortMap sortMap, Component comp) throws Exception {
		super.onSelectionChanged(p_result, sortMap, comp);
    	String preset = (String)conditionPresetListbox.getSelectedItem().getValue();
    	selectionTimer = ZkUtil.timerEvent(selectionTimer, conditionPresetListbox, 100, () -> {
    		UniLog.log1("onSelectionChanged preset:%s", preset);
    		Selectors.find("#btExtraBatchAction_0").stream().findFirst().ifPresent(bt -> {
    			((Button)bt).addSclass("orange1");
    			bt.setVisible(Objects.equals(preset, "public_繳費通知單"));
    		});
    		Selectors.find("#btOverduePaymentContent").stream().findFirst().ifPresent(bt -> {
    			((Button)bt).addSclass("lightgreen1");
    			bt.setVisible(Objects.equals(preset, "public_逾期通知函"));
    		});
    		Selectors.find("#btExtraBatchAction_1").stream().findFirst().ifPresent(bt -> {
    			((Button)bt).addSclass("orange1");
    			bt.setVisible(Objects.equals(preset, "public_逾期通知函"));
    		});
   			Selectors.find("#btGeneralReport").forEach(bt -> bt.setVisible(!Objects.equals(preset, "public_逾期通知函")));
    	});
    }

	@Override
	protected XulElement buildAdvSearchInputComp(ZkBiAdvSearch advSearch, BiResult result, BiColumn bc, boolean textFlag, boolean multiPickSelectFlag) {
		if (StringUtils.equals(bc.getLabel(), "mpy_month")) {
			try {
				ZkJxQueryInput ie = new ZkJxQueryInput();
				ie.setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				TrGetItemProperty gipi = new TrGetItemProperty(Arrays.asList("mpy_month"));
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
		} else if (StringUtils.equals(bc.getLabel(), "mpy_propertyunit")) {
			try {
				ZkJxQueryInput ie = new ZkJxQueryInput();
				ie.setType(ZkJxQueryInput.TYPE_STRING, sessionHelper);
				TrGetItemProperty gipi = new TrGetItemProperty(Arrays.asList("mpy_propertyunit"));
				if (!getSessionHelper().hasAccessRight("#allproperty")) {
					String ss = StringUtils.defaultString(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct mpy_propertyunit from monthpayment where mpy_propertyname = ? order by mpy_propertyunit", new Wherecl().appendArgument(ss)));
				} else
					gipi.setTableRec(result.getSelectUtil().getQueryResult("select distinct mpy_propertyunit from monthpayment order by mpy_propertyunit"));
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

	@Override
    protected void setupExtraButton(final BiResult result) {
    	super.setupExtraButton(result);
    	if (sessionHelper.hasAccessRight("#pmgtadm1")) {
    		String cocode = Erpv4Config.getDefaultCoCode(sessionHelper);
	       	Button btn = new ZkBiButton("逾期通知函內容", null, "btOverduePaymentContent");
	       	btn.addEventListener(Events.ON_CLICK, event -> {
	       		Textbox tb = new Textbox();
	       		tb.setHflex("1");
	       		tb.setVflex("1");
	       		tb.setStyle("resize:none;font-size: 12pt !important;line-height:18pt");
	       		tb.setMultiline(true);
	       		tb.setText(ZkUtil.getFirstTableRec(result.getSelectUtil(), 
	       							"select co_overpmcontent from cocode where co_cocode = ?", new Wherecl().appendArgument(cocode))
	       						.map(throwFunction(tr -> tr.getFieldString("co_overpmcontent"))).orElse(""));
	       		Vlayout vl = new Vlayout();
	       		vl.setHeight("calc(80vh - 120px)");
	       		vl.appendChild(tb);
	       		ZkBiMsgbox.build2(vl, 10, 785.2, (ev, mbbt) -> {
	       			if (mbbt.getIdx() == 0) {
	       				ZkUtil.importAction.accept(sessionHelper, su -> {
	       					su.executeUpdate("update cocode set co_overpmcontent = ? where co_cocode = ?", new Wherecl().appendArgument(tb.getText()).appendArgument(cocode));
	       				});
	       				ZkUtil.showMsg("更新完成");
	       			}
	       		}, "Ok", "Cancel").setTitle(btn.getLabel()).doModal();
	       	});
	        abHelper.addButton(btn);
    	}
    }

	@Override
    public Button addBatchBiActionHandler(final BiResult p_result,boolean p_BatchMode ,int p_AccessControl, String p_AccesKey,String p_id,String p_label,String p_icon,final BiActionHandler p_handler) {
		if (p_handler.getClass().getSimpleName().equals("PrintPaymentOverdueNotice")) {
			Button btn = super.addBatchBiActionHandler(p_result, false, p_AccessControl, p_AccesKey, p_id, p_label, p_icon, p_handler);
			ZkUtil.removeAllEventListener(btn, "onMyClick");
			btn.getEventListeners(Events.ON_CLICK).forEach(event -> btn.addEventListener("onMyClick", event));
			ZkUtil.setZkBiEventListener(btn, Events.ON_CLICK, event -> {
				Checkbox cb = new Checkbox("列印業權人姓名");
	       		Div div = new Div();
	       		div.setHeight("25px");
	       		div.appendChild(cb);
	       		div.setStyle("display:flex;justify-content:center;align-items:flex-end");
	       		ZkBiMsgbox.build2(div, 10, 350, (ev, mbbt) -> {
	       			if (mbbt.getIdx() == 0) {
	       				btn.setAttribute("printOwnerFlag", cb.isChecked());
	       				Events.echoEvent("onMyClick", btn, null);
	       			}
	       		}, "Ok", "Cancel").setTitle(btn.getLabel()).doModal();
			});
			if (p_BatchMode) setupBatchModeButton(btn);
	    	return btn;
		} else
			return super.addBatchBiActionHandler(p_result, p_BatchMode, p_AccessControl, p_AccesKey, p_id, p_label, p_icon, p_handler);
    }
}
