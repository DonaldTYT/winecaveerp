package com.uniinformation.jxapp;

//import org.zkoss.zsoup.helper.StringUtil;
//import org.zkoss.zul.Textbox;

import com.uniinformation.bicore.BiResult;

public class BiTranslate extends JxZkBiBase{
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br,mode);
		/*
		if (StringUtil.isBlank(p_br.getCell("bitl_key").getString())){
			jxAdd("bitl_key").setEnable(true);
		}
		else{
			jxAdd("bitl_key").setEnable(false);
		}
		if (StringUtil.isBlank(p_br.getCell("bitl_type").getString())){
			jxAdd("bitl_type").setEnable(true);
		}
		else{
			jxAdd("bitl_type").setEnable(false);
		}
		if (StringUtil.isBlank(p_br.getCell("bitl_lang").getString())){
			jxAdd("bitl_lang").setEnable(true);
		}
		else{
			jxAdd("bitl_lang").setEnable(false);
		}
		*/
	}

}
