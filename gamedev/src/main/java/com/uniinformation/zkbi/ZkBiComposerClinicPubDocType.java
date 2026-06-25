package com.uniinformation.zkbi;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.clinic.BiResultPubDocType;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiComposerClinicPubDocType extends ZkBiComposerBase {
	@Override
    protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc){
		BiResult br = super.getQueryResult(sessionHelper,p_viewid, p_sortIdx, p_sortDesc);
		if (br instanceof BiResultPubDocType){
			((BiResultPubDocType) br).setAgentId(sessionHelper.getAgent());
		}
		return(br);
	}
}
