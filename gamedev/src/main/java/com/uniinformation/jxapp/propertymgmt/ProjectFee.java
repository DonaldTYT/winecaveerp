package com.uniinformation.jxapp.propertymgmt;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultProjectFee;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListGetItemProperty;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.utils.ZkUtil.throwConsumer;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.isValidYearMonth;

public class ProjectFee extends JxZkBiBase {
	private final ListGetItemProperty.SelectorPick gipiPeriod = new ListGetItemProperty.SelectorPick();
	private final ListGetItemProperty.SelectorPick gipiPayMonth = new ListGetItemProperty.SelectorPick();
	
	public ProjectFee() {
		gipiPeriod.setOneColValueList(IntStream.range(1, 6).mapToObj(String::valueOf).collect(Collectors.toList()));
	}

	@Override
	public void afterBind() {
		super.afterBind();
		jxAdd("vcol_period").setItemListInterface(gipiPeriod);
		/*ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			getBr().getCell("col_g").set(getBr().getCellString("vcol_period"));
		}, "vcol_period");*/
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		try {
			gipiPayMonth.setOneColValueList(getPickMonthList(p_br, sessionHelper));
			jxAdd("col_e").setItemListInterface(gipiPayMonth);
			jxAdd("col_f").setItemListInterface(gipiPayMonth);
			super.bindCellCollection(p_br, mode);
			BiResultProjectFee br = (BiResultProjectFee)p_br;
			if (mode == JxZkBiBase.MODE_ADD)
				p_br.getCell("col_g").set(1);
			p_br.getCell("vcol_period").set(p_br.getCellString("col_g"));
			br.setupStatisticsField();

			Listbox lb = (Listbox)jxAdd("list_propertymgmt_ProjectFeeUnit").getNativeObject();
			Selectors.find(lb, "listheader").forEach(lh -> {
				String label = (String)lh.getAttribute("biColumnLabel"); 
				lh.setVisible(!BiResultProjectFee.periodColumnLabelList.contains(label) || br.getPeriodColumnLabelStream().anyMatch(s -> Objects.equals(s, label)));
			});

			Selectors.find("[id^='btExtraJxFormAction_']").stream().map(x -> (Button)x).forEach(bt -> {
				bt.addSclass("orange1");
			});

			disableDeleteLink(p_br, "propertymgmt.ProjectFeeUnit");
		} catch (Exception e) {
			UniLog.log(e);
		}
	}

	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(false);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult p_br) {
		ReturnMsg rtn = super.beforeUpdate(p_br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			BiResultProjectFee br = (BiResultProjectFee)p_br;
			rtn = validationRecord(true);
			if (!rtn.getStatus())
				return rtn;
			Set<String> list = br.getPeriodColumnLabelStream().collect(Collectors.toSet());
			List<String> list1 = BiResultProjectFee.periodColumnLabelList.stream().filter(s -> !list.contains(s)).collect(Collectors.toList());
			getBr().getSubLinkResult("propertymgmt.ProjectFeeUnit").forEach(bcc -> {
				list1.forEach(throwConsumer(k -> bcc.getCell(k).set(0.0)));
			});
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	private ReturnMsg validationRecord(boolean isUpdate) throws Exception {
		BiResultProjectFee br = (BiResultProjectFee)getBr();
		double fee = br.getCellDouble("col_d");
		if (fee <= 0)
			return new ReturnMsg(false, "項目總額必須大於0");
		String startMonth = getBr().getCellString("col_e");
		String endMonth = getBr().getCellString("col_f");
		if (StringUtils.isNotBlank(startMonth) && StringUtils.isNotBlank(endMonth)) {
			if (!isValidYearMonth(startMonth))
				return new ReturnMsg(false, "開始月份錯誤", true);
			if (!isValidYearMonth(endMonth))
				return new ReturnMsg(false, "結束月份錯誤", true);
			if (startMonth.compareTo(endMonth) > 0)
				return new ReturnMsg(false, "合約開始月不能大於合約結束月", true);
		}
		
		Vector<BiCellCollection> list = getBr().getSubLinkResult("propertymgmt.ProjectFeeUnit");
		if (!list.isEmpty()) {
			for (BiCellCollection bcc : list) {
				String unit = bcc.getString("col_b");
				double allocFee = bcc.getDouble("col_c");
				if (allocFee < 0)
					return new ReturnMsg(false, String.format("[%s]分攤金額不能少於0", unit));
				if (br.getPeriodColumnLabelStream().anyMatch(k -> bcc.getDouble(k) < 0))
					return new ReturnMsg(false, String.format("[%s]分期金額不能小於0", unit));
				if (Math.abs(br.getPeriodColumnLabelStream().mapToDouble(k -> bcc.getDouble(k)).sum() - allocFee) >= 1e-6)
					return new ReturnMsg(false, String.format("[%s]分期總金額必須等於分攤金額", unit));
			}
			double totColc = list.stream().mapToDouble(bcc -> bcc.getDouble("col_c")).sum();
			if (list.stream().anyMatch(bcc -> bcc.getDouble("col_c") != 0) && Math.abs(totColc - fee) >= 1e-6)
				return new ReturnMsg(false, String.format("分攤總金額[%f]必須等於項目總額[%f]", totColc, fee));
		}
		return ReturnMsg.defaultOk;
	}

	public static List<String> getPickMonthList(BiResult br, SessionHelper sessionHelper) throws Exception {
		/*LocalDate today = LocalDate.now();
        LocalDate tenYearsBefore = today.minusYears(2);
        LocalDate tenYearsLater = today.plusYears(10);
        List<String> list = new ArrayList<>();
        for (YearMonth month = YearMonth.from(tenYearsBefore); !month.isAfter(YearMonth.from(tenYearsLater)); month = month.plusMonths(1))
            list.add(month.format(DateTimeFormatter.ofPattern("yyyy-MM")));*/
		String ss = Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper));
		return ZkUtil.getTableRecStream(br.getSelectUtil(), "select contractmonth.col_c c from contract join contractmonth on contractmonth.col_a = contract.col_a and contractmonth.col_b = contract.col_b "
				+ "where contract.col_a = ? order by contractmonth.col_c", new Wherecl().appendArgument(ss))
				.map(c -> c.getString("c"))
				.collect(Collectors.toList());
	}
	
}
