package com.uniinformation.zkbi;

import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.clinic.BiResultHealthQnr;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiComposerClinicHealthQnr extends ZkBiComposerBase {
	/*
	@Override
	protected void setupAddButton(final BiResult result)
	{
        Button btnAutoFiling = new ZkBiButton();
        btnAutoFiling.setLabel("Auto Filing");
        btnAutoFiling.setId("btAutoFiling");
        actionBar.appendChild(btnAutoFiling);
	}
    */
	@Override
	protected void setupExportButton(final BiResult result)
	{
	}
	
	@Override
    protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc){
		BiResult br = super.getQueryResult(sessionHelper,p_viewid, p_sortIdx, p_sortDesc);
		if (br instanceof BiResultHealthQnr){
			((BiResultHealthQnr) br).setAgentId(sessionHelper.getAgent());
			((BiResultHealthQnr) br).setAESKey(sessionHelper.getAESKey());
		}
		return(br);
	}
}
