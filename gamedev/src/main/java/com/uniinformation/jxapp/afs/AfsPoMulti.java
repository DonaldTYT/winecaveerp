package com.uniinformation.jxapp.afs;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.erpv4.PoMulti;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.VectorUtil;

public class AfsPoMulti extends PoMulti {
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		p_br.getCell("stm_ref5").setItemPropertyInterface(
				new TranslateListGetItemProperty(
						new VectorUtil()
							.addElement("HK")
							.addElement("SH")
							.addElement("SZ")
							.addElement("TX")
							.toVector()
						) {
					public String translate(Object p_o) {
						if(p_o.toString().equals("HK"))return("Hong Kong");
						if(p_o.toString().equals("SH"))return("Shanghai ,China");
						if(p_o.toString().equals("SZ"))return("Yantian Shenzhen, China");
						if(p_o.toString().equals("TX"))return("Tianjin Xingang PRC");
						return("");
					}

					@Override
					public int getRowWidth() {
						// TODO Auto-generated method stub
						return 0;
					}
				}
		);
	}
}
