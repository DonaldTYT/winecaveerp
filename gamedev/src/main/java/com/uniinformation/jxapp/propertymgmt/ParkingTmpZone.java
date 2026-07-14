package com.uniinformation.jxapp.propertymgmt;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.CellReference;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import static com.uniinformation.utils.ZkUtil.throwFunction;

public class ParkingTmpZone extends JxZkBiBase {
	private final TrGetItemProperty.SelectorPick gipiPropertyBlock = new TrGetItemProperty.SelectorPick(Arrays.asList("col_c"));
	private final TrGetItemProperty.SelectorPick gipiPropertyFloor = new TrGetItemProperty.SelectorPick(Arrays.asList("col_d"));
	private String locDesc;

	@Override
	public void afterBind() {
		super.afterBind();
		UniLog.log("ParkingTmpZone afterBind");
		locDesc = Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper));
		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			getBr().getCell("col_f").set("");
			setupFloorItemInterface(getBr());
		}, "col_e");
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		try {
			SelectUtil su = p_br.getSelectUtil();
			if (gipiPropertyBlock.getTableRec() == null) {
				gipiPropertyBlock.setTableRec(su.getQueryResult("select distinct col_c from property where col_b = ? order by col_c", new Wherecl().appendArgument(locDesc)));
				jxAdd("col_e").setItemListInterface(gipiPropertyBlock);
			}
			setupFloorItemInterface(p_br);
			super.bindCellCollection(p_br, mode);
			if (mode == JxZkBiBase.MODE_ADD) {
				String code = ZkUtil.getFirstTableRec(su, "select MAX(LPAD(col_b, 5, ' ')) b from parkingtmpzone where col_a = ?", new Wherecl().appendArgument(locDesc))
								.map(throwFunction(tr -> tr.getFieldString("b"))).orElse("").trim();
				if (isValidZoneCode(code)) {
					int num = CellReference.convertColStringToIndex(code);
					code = CellReference.convertNumToColString(num + 1);
				} else
					code = "AA";
				p_br.getCell("col_b").set(code);
			}
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
		BiResult br = getBr();
		if (!isValidZoneCode(br.getCellString("col_b")))
			return new ReturnMsg(false, "區域編號錯誤", true);
		if (br.getCellInt("col_c") <= 0)
			return new ReturnMsg(false, "區域位置數量必須大於0", true);
		return ReturnMsg.defaultOk;
	}

	private void setupFloorItemInterface(BiResult br) throws Exception {
		gipiPropertyFloor.setTableRec(br.getSelectUtil().getQueryResult("select distinct col_d from property where col_b = ? and col_c = ? order by col_d", 
				new Wherecl().appendArgument(locDesc).appendArgument(br.getCellString("col_e"))));
		jxAdd("col_f").setItemListInterface(gipiPropertyFloor);
	}
	
	private static boolean isValidZoneCode(String code) {
		return StringUtils.isNotBlank(code) && code.matches("[A-Z]+");
	}
}
