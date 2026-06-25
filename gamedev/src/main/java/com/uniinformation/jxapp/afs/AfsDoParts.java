package com.uniinformation.jxapp.afs;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.AfsDO;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;


public class AfsDoParts extends AfsDO {
//	GipiNamedItemList prdList;
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		/*
		try {
		SelectUtil su = p_br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from prdsrvmaster");
			prdList = new GipiNamedItemList();
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				prdList.appendItem( tr.getFieldString("pds_ano"), tr.getFieldString("pds_desc"));
			}
			if(p_br.getCell("stm_salescode1") != null) p_br.getCell("stm_salescode1").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode2") != null) p_br.getCell("stm_salescode2").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode3") != null) p_br.getCell("stm_salescode3").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode4") != null) p_br.getCell("stm_salescode4").setItemPropertyInterface(prdList);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		*/
	}
}
