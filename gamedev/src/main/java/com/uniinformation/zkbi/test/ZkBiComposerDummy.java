package com.uniinformation.zkbi.test;

import com.uniinformation.webcore.ZkComposerBase;

public class ZkBiComposerDummy extends ZkComposerBase{
	@Override
	protected boolean validateURL(String p_requestURL) {
		return true;
	}

}
