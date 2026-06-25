package com.uniinformation.jxapp.erpv4;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class PricePlan extends JxZkBiBase {
	public void bindCellCollection(BiResult c,int mode) {
		super.bindCellCollection(c, mode);
		AbstractGetItemProperty gipi = getGipi("erpv4.PricePlanDet");
		((ZkBiGetItemProperty) gipi).setUseDefaultPickup(true);
	}
}
