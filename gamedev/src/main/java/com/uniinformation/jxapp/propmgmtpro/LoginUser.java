package com.uniinformation.jxapp.propmgmtpro;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.BiPickGetItemProperty;
import com.uniinformation.utils.UniLog;

public class LoginUser extends JxZkBiBase {
	private static final BiPickGetItemProperty gipiMettingDate = new BiPickGetItemProperty(Lists.newArrayList("col_b"));

	@Override
	public void afterBind() {
		super.afterBind();
		jxAdd("lc_desc").addChangeListener((JxField field, String orgvalue) -> {
			try {
				getBr().getCell("lgu_mettingdate").set(DateUtil.zeroDate);
				setupMettingDateItemListInterface(getBr());
			} catch (CellException e) {
				UniLog.log(e);
			}
			return true;
		});
	}

	@Override
	public void bindCellCollection(BiResult c, int mode) {
		setupMettingDateItemListInterface(c);
		super.bindCellCollection(c, mode);
	}
	
	private void setupMettingDateItemListInterface(BiResult br) {
		gipiMettingDate.setBiResult(BiResultHelper.create(sessionHelper, "propmgmtpro.MettingCopy", gipiMettingDate.getBiResult(), String.format("col_a = %d", br.getCellInt("lgu_lcrg")), null, -1, Lists.newArrayList(Pair.of("col_b", true))));
		jxAdd("lgu_mettingdate").setItemListInterface(gipiMettingDate);
	}
}
