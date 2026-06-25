package com.uniinformation.zkbi.test;

import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.ZkComposerBase;

public class ZkBiComposerTestBS extends ZkComposerBase{
	
	@Override
	protected boolean validateURL(String p_requestURL) {
		UniLog.log1("skip url validation");
		return true;
	}

}
