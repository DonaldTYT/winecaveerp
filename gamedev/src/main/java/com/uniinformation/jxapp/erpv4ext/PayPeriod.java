package com.uniinformation.jxapp.erpv4ext;

import java.util.Date;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class PayPeriod extends JxZkBiBase {

	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			validRecord();
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			validRecord();
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}
	
	private void validRecord() throws Exception {
		Date startDate = getBr().getCellDate("pp_start");
		Date endDate = getBr().getCellDate("pp_end");
		if (DateUtil.isDateNull(startDate))
			throw new Exception("Start Date cannot be empty");
		if (DateUtil.isDateNull(endDate))
			throw new Exception("Start Date cannot be empty");
		if (startDate.compareTo(endDate) >= 0)
			throw new Exception("End Date must be more than Start Date");
	}
}
