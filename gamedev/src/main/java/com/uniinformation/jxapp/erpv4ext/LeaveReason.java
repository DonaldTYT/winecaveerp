package com.uniinformation.jxapp.erpv4ext;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class LeaveReason extends JxZkBiBase {

	@Override
	public void bindCellCollection(final BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		if (mode == JxZkBiBase.MODE_UPDATE) {
			if (StringUtils.equalsAny(p_br.getCellString("lvrs_name"), "AL", "CL")) {
				jxSetEnable("lvrs_name", false);
				jxSetEnable("lvrs_nopay", false);
			}
		}
	}
}
