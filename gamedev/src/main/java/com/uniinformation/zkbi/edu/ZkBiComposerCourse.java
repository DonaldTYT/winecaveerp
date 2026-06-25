package com.uniinformation.zkbi.edu;

import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.edu.BiResultCourse;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerCourse extends ZkBiComposerBase {
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");
	}

	@Override
	protected void setupExtraButton(BiResult result) {
	}
	

	@Override
    protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc){
		BiResult br = super.getQueryResult(sessionHelper,p_viewid, p_sortIdx, p_sortDesc);
		if (br instanceof BiResultCourse){
			UniLog.log1("setAgentId:%s", sessionHelper.getAgent());
			((BiResultCourse) br).setAgentId(sessionHelper.getAgent());
		}
		return(br);
	}
}
