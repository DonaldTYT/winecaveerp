package com.uniinformation.bicore.wc;

import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.BcTagUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultTransferScp extends BiResultTransfer {

	public BiResultTransferScp(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	
	/* use this for pre process validataion before add/update */
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col,isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(col.getCellInt("stm_nref3") == 0) {
			return(new ReturnMsg(false,"Please Select Warehouse or Location Only"));
		}
		if(col.getCellInt("stm_nref3") == 1) {
			if(
					StringUtils.isBlank(col.getCellString("stm_ref3")) ||
					StringUtils.isBlank(col.getCellString("stm_ref3"))
					) {
				return(new ReturnMsg(false,"Please Select Warehouse"));
			}
			
		}
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	public ReturnMsg lockRecordForUpdate() {
		Date d = getCellDate("stm_cfmdate");
		if(d.after(DateUtil.minDate)) {
			return(new ReturnMsg(false,"Already Posted, Cannot Update"));
		}
		return(super.lockRecordForUpdate());
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		Date d = getCellDate("stm_cfmdate");
		if(d.after(DateUtil.minDate)) {
			return(new ReturnMsg(false,"Already Posted, Cannot Delate"));
		}
		return(super.biBeforeDeleteCurrent(col));
	}
}
