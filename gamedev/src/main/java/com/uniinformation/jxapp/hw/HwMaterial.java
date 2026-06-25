package com.uniinformation.jxapp.hw;

import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class HwMaterial extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int p_mode) {
//		boolean isNew;
//		isNew = getGipi("hw.MatOption") == null;
//		if(isNew) {
//			setGipi("hw.MatOption",new BiGetItemProperty(p_br.getSubLink("hw.MatOption")));	
//		}
//		ZkBiGetItemProperty.useGetItemPropertyForSubLink(p_br,this);
		super.bindCellCollection(p_br, p_mode);
	}	
}
