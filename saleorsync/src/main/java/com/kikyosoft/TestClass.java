package com.kikyosoft;

import com.uniinformation.webcore.ZkSessionHelper;

public class TestClass {
	class Spt extends com.uniinformation.webcore.SessionHelper {

		@Override
		public String getURLParam(String p_key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getWebContentRealPath(String p_path, boolean p_withSeparator) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	public TestClass() {
    	System.out.println(
    		    com.uniinformation.webcore.SessionHelper.class
    		        .getProtectionDomain()
    		        .getCodeSource()
    		);
		Spt sspt = new Spt();
		boolean sw = sspt.isLogin();
		sw = false;
		ZkSessionHelper ssp = ZkSessionHelper.getSessionHelperDummy(null, null, null);
		ssp = null;
	}
}
