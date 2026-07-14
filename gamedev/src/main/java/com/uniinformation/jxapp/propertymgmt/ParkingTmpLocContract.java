package com.uniinformation.jxapp.propertymgmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.Timer;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListGetItemProperty;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import static com.uniinformation.utils.ZkUtil.throwFunction;
import static com.uniinformation.utils.ZkUtil.safeRunnable;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.isValidYearMonth;

public class ParkingTmpLocContract extends JxZkBiBase {
	private final ListGetItemProperty.SelectorPick gipiContractMonth = new ListGetItemProperty.SelectorPick();
	private final ListGetItemProperty.SelectorPick gipiLocCode = new ListGetItemProperty.SelectorPick();
	private final TrGetItemProperty.SelectorPick gipiPropertyBlock = new TrGetItemProperty.SelectorPick(Arrays.asList("col_c"));
	private final TrGetItemProperty.SelectorPick gipiPropertyFloor = new TrGetItemProperty.SelectorPick(Arrays.asList("col_d"));
	private final TrGetItemProperty.SelectorPick gipiPropertyUnit = new TrGetItemProperty.SelectorPick(Arrays.asList("key_a"));
	private String locDesc, savedLocCode;
	private Timer setupLocCodeItemInterfaceTimer;
	
	@Override
	public void afterBind() {
		super.afterBind();
		locDesc = Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper));
		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			if (curMode == JxZkBiBase.MODE_ADD) {
				getBr().getCell("col_b").set("");
				setupLocCodeItemInterfaceWithTimer(getBr(), 1000);
			}
		}, "col_d", "col_e");
		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			getBr().getCell("col_j").set("");
			getBr().getCell("col_b").set("");
			setupFloorItemInterface(getBr());
			setupLocCodeItemInterfaceWithTimer(getBr(), 0);
		}, "col_i");
		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			getBr().getCell("col_b").set("");
			setupLocCodeItemInterfaceWithTimer(getBr(), 0);
		}, "col_j");
		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			BiResult br = getBr();
			String zoneCode = extractZoneCode(br.getCellString("col_b"));
			br.getCell("col_h").set(zoneCode);
			br.getCell("col_k").set(ZkUtil.getFirstTableRec(br.getSelectUtil(), "select col_d from parkingtmpzone where col_a = ? and col_b = ?", 
							new Wherecl().appendArgument(locDesc).appendArgument(zoneCode)).map(throwFunction(tr -> tr.getFieldDouble("col_d"))).orElse(0.0));
		}, "col_b");
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		try {
			gipiContractMonth.setOneColValueList(ProjectFee.getPickMonthList(p_br, sessionHelper));
			jxAdd("col_d").setItemListInterface(gipiContractMonth);
			jxAdd("col_e").setItemListInterface(gipiContractMonth);
			savedLocCode = p_br.getCellString("col_b");
			if (gipiPropertyBlock.getTableRec() == null) {
				gipiPropertyBlock.setTableRec(p_br.getSelectUtil().getQueryResult("select distinct col_c from property where col_b = ? order by col_c", new Wherecl().appendArgument(locDesc)));
				jxAdd("col_i").setItemListInterface(gipiPropertyBlock);
			}
			setupFloorItemInterface(p_br);
			setupLocCodeItemInterface(p_br, true);
			setupUnitItemInterface(p_br);
			super.bindCellCollection(p_br, mode);
			disableDeleteLink(p_br, "propertymgmt.ParkingTmpLocMonth");
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
			rtn = validationRecord(true);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	private ReturnMsg validationRecord(boolean isUpdate) throws Exception {
		String startMonth = getBr().getCellString("col_d");
		String endMonth = getBr().getCellString("col_e");
		if (StringUtils.isNotBlank(startMonth) && StringUtils.isNotBlank(endMonth)) {
			if (!isValidYearMonth(startMonth))
				return new ReturnMsg(false, "合約開始月錯誤", true);
			if (!isValidYearMonth(endMonth))
				return new ReturnMsg(false, "合約結束月錯誤", true);
			if (startMonth.compareTo(endMonth) > 0)
				return new ReturnMsg(false, "合約開始月不能大於合約結束月", true);
		}
		return ReturnMsg.defaultOk;
	}

	private void setupFloorItemInterface(BiResult br) throws Exception {
		gipiPropertyFloor.setTableRec(br.getSelectUtil().getQueryResult("select distinct col_d from property where col_b = ? and col_c = ? order by col_d", 
				new Wherecl().appendArgument(locDesc).appendArgument(br.getCellString("col_i"))));
		jxAdd("col_j").setItemListInterface(gipiPropertyFloor);
	}

	private void setupLocCodeItemInterface(BiResult br, boolean isInit) throws Exception {
		String startMonth = br.getCellString("col_d");
		String endMonth = br.getCellString("col_e");
		if (!isValidYearMonth(startMonth) || !isValidYearMonth(endMonth)
				|| (StringUtils.isBlank(br.getCellString("col_i")) && StringUtils.isBlank(br.getCellString("col_j")))
				|| startMonth.compareTo(endMonth) > 0) {
			List<String[]> list = new ArrayList<>();
			if (isInit && StringUtils.isNotBlank(savedLocCode))
				list.add(new String[] {savedLocCode});
			if (!Objects.deepEquals(gipiLocCode.getValueList(), list)) {
				gipiLocCode.setValueList(list);
				jxAdd("col_b").setItemListInterface(gipiLocCode);
			}
			return;
		}
		SelectUtil su = br.getSelectUtil();
		List<String> list = ZkUtil.getTableRecStream(su, "select col_b, col_c from parkingtmpzone where col_a = ? and col_e = ? and col_f = ? order by col_b", 
											new Wherecl().appendArgument(locDesc).appendArgument(br.getCellString("col_i")).appendArgument(br.getCellString("col_j"))).map(throwFunction(cell -> {
			String zoneCode = cell.getString("col_b");
			int locCount = cell.getInt("col_c");
			UniLog.log1("startMonth:%s, endMonth:%s, zoneCode:%s, locCount:%d", startMonth, endMonth, zoneCode, locCount);
			List<String> locCodeList = IntStream.rangeClosed(1, locCount).mapToObj(i -> String.format("%s%02d", zoneCode, i)).collect(Collectors.toList());
			ZkUtil.getTableRecStream(su, "select distinct col_b from parkingtlconmonth where col_a = ? and col_b like ? and col_d between ? and ? and col_b <> ?", 
								new Wherecl().appendArgument(locDesc).appendArgument(zoneCode + "%").appendArgument(startMonth).appendArgument(endMonth).appendArgument(savedLocCode)).forEach(cell1 -> {
				locCodeList.remove(cell1.getString("col_b"));
			});
			return locCodeList;
		})).flatMap(List::stream).collect(Collectors.toList());
		if (isInit && StringUtils.isNotBlank(savedLocCode))
			list.add(savedLocCode);
		gipiLocCode.setOneColValueList(list);
		jxAdd("col_b").setItemListInterface(gipiLocCode);
	}

	private void setupLocCodeItemInterfaceWithTimer(BiResult br, int delay) {
		setupLocCodeItemInterfaceTimer = ZkUtil.timerEvent(setupLocCodeItemInterfaceTimer, curComp, delay, safeRunnable(() -> {
			setupLocCodeItemInterface(br, false);
		}));
	}

	private void setupUnitItemInterface(BiResult br) throws Exception {
		gipiPropertyUnit.setTableRec(br.getSelectUtil().getQueryResult("select key_a from property where col_b = ? order by key_a", 
				new Wherecl().appendArgument(locDesc)));
		jxAdd("col_c").setItemListInterface(gipiPropertyUnit);
	}
	
	public static String extractZoneCode(String input) {
        Matcher matcher = Pattern.compile("^([A-Z]+)").matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }
}
