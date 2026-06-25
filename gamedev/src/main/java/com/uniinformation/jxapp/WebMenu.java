package com.uniinformation.jxapp;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class WebMenu extends JxZkBiBase {

	@Override
	protected ReturnMsg afterUpdate(BiResult br)
	{
		UniLog.log("WebMenu afterUpdate");
		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper();
		sessionHelper.clearSideMenuCache();
		return super.afterUpdate(br);
	}
}
