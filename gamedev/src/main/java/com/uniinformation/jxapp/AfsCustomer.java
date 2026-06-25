package com.uniinformation.jxapp;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class AfsCustomer extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int p_mode) {
//		ZkBiGetItemProperty.useGetItemPropertyForSubLink(p_br,this);
		super.bindCellCollection(p_br, p_mode);
	}	
}
