package com.uniinformation.jxapp.aw;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class PresetMaster extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
//		ZkBiGetItemProperty.useGetItemPropertyForSubLink(p_br,this);
		super.bindCellCollection(p_br,mode);
	}
}
