package com.uniinformation.jxapp.afs;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jxapp.AfsPO;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;

public class AfsPoMc extends AfsPO {
	@Override
	protected Wherecl createPullDownWherecl(CellCollection col) {
		Wherecl wcl;
		wcl = super.createPullDownWherecl(col);
		if(wcl == null) wcl = new Wherecl();
		wcl.genInList("and", "st_mtype","in","M","O");
		return(wcl);
	}
	@Override
	public void bindCellCollection(BiResult c,int mode) {
		super.bindCellCollection(c, mode);
		/*
		c.getCell("stm_ctrspec").setItemList(
				new VectorUtil()
					.addElement(p_double)
					"Hong Kong " 
					"Shanghai ,China" 
					"Yantian Shenzhen, China"
					"Tianjin Xingang PRC"
					.toVector()
				);
		*/
		/*
		c.getCell("stm_nref4").setItemPropertyInterface(
				new TranslateListGetItemProperty(
						new VectorUtil()
							.addElement(0)
							.addElement(1)
							.addElement(2)
							.addElement(3)
							.toVector()
						) {
					public String translate(Object p_o) {
						switch((Integer) p_o) {
						case 1: return("Hong Kong");
						case 2: return("Shanghai ,China");
						case 3: return("Yantian Shenzhen, China");
						case 4: return("Tianjin Xingang PRC");
						default : return("");
						}
					}
				}
		);
		*/

	}
	
}
