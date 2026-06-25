package com.uniinformation.bicore;

import java.util.Vector;

import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultBiTranslate extends BiResult{
	public BiResultBiTranslate(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col){
		UniLog.logm(this,"called");
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		TranslateUtil.updateText(sh, 
									   col.getCell("bitl_key").getString(), 
									   col.getCell("bitl_type").getString(), 
									   col.getCell("bitl_lang").getString(), 
									   col.getCell("bitl_labelstr").getString());
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col){
		UniLog.logm(this,"called");
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		TranslateUtil.updateText(sh, 
									   col.getCell("bitl_key").getString(), 
									   col.getCell("bitl_type").getString(), 
									   col.getCell("bitl_lang").getString(), 
									   col.getCell("bitl_labelstr").getString());
		return(new ReturnMsg(true));
	}

}
