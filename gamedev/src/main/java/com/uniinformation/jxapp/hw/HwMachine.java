package com.uniinformation.jxapp.hw;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class HwMachine extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int p_mode) {
//		ZkBiGetItemProperty.useGetItemPropertyForSubLink(p_br,this);
		super.bindCellCollection(p_br, p_mode);
	}	
}
