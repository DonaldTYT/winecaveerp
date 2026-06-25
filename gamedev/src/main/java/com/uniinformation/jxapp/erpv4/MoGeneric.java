package com.uniinformation.jxapp.erpv4;

import org.zkoss.zul.Listbox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;

public class MoGeneric extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		String detViewId = "erpv4.MoGenericDet";
		BiResult sr = br.getSubLink(detViewId);
		if(!sessionHelper.isMobileDevice()) {
			if(!sessionHelper.useJxFormG2() || (sr != null && sr.getRowCount() > 20)) {
				jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).setAttribute("paging", "withfilter");
			} else {
				jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).setAttribute("nopaging",null);
			}
		} else {
    		int lh =sessionHelper.getScreenHeight()-600;
    		if(lh < 300) lh = 300;
			Listbox lb = (Listbox) jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).getNativeObject();
    		lb.setHeight(""+lh+"px");
		}
		
	}

}
