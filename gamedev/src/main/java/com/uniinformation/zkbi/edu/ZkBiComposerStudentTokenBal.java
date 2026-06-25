package com.uniinformation.zkbi.edu;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerStudentTokenBal extends ZkBiComposerBase {

	@Override
    protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc) {
		BiResult br = super.getQueryResult(sessionHelper, p_viewid, p_sortIdx, p_sortDesc);
		BiColumn bc = br.getColumnByLabel("essbsd_preview");
		if (bc != null)
			br.hideViewColumn(bc);
		return br;
    }
}
