package com.uniinformation.jxapp.erpv4;

public class MoPosTransfer extends MoPos {

	@Override
	public void afterBind() {
		super.afterBind();
		detViewId = "erpv4.MoDetPosTfr";
//		toLoc = "LNTST";
	}

}
