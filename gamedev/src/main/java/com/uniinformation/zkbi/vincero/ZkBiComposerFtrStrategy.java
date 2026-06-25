package com.uniinformation.zkbi.vincero;

import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.vincero.BiResultFtrStrategy;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerFtrStrategy extends ZkBiComposerBase{
	@Override
    public boolean doAddOneRow(XulElement p_win,BiResult p_result) {
		try {
			((BiResultFtrStrategy) p_result).addCcyRecords();
		} catch (Exception ex) {
			UniLog.log(ex);
			ZkUtil.msg(ex.toString());
		}
		return(super.doAddOneRow(p_win, p_result));
	}
}
