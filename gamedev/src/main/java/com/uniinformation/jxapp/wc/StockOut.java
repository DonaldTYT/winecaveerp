package com.uniinformation.jxapp.wc;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;

public class StockOut extends JxZkBiBase {

	@Override
	public void bindCellCollection(BiResult br, int mode) {
		super.bindCellCollection(br, mode);
		updateSaveButtonState();
	}

	@Override
	protected void formDirtyChanged() {
		super.formDirtyChanged();
		updateSaveButtonState();
	}

	private void updateSaveButtonState() {
		BiResult br = getBr();
		if (br != null && isPosted(br)) {
			jxSetEnable("btUpdate", false);
		}
	}

	private boolean isPosted(BiResult br) {
		return (br.getCellDate("stm_cfmdate") != null
				&& br.getCellDate("stm_cfmdate").after(DateUtil.minDate))
				|| StringUtils.isNotBlank(br.getCellString("stm_cfmuser"));
	}
}
