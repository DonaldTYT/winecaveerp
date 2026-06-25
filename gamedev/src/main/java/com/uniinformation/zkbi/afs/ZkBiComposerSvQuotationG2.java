package com.uniinformation.zkbi.afs;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.afs.BiResultAfsQuotationG2;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.erpv4.ZkBiComposerQuotationG2;

public class ZkBiComposerSvQuotationG2 extends ZkBiComposerQuotationG2 {
	@Override
	protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc) {
		BiResult result = super.getQueryResult(sessionHelper, p_viewid, p_sortIdx, p_sortDesc);
		((BiResultAfsQuotationG2) result).setInvType("AQS");
		return(result);
	}
}
