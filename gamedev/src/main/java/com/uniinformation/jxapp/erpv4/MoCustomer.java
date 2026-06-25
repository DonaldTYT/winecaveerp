package com.uniinformation.jxapp.erpv4;

public class MoCustomer extends MO {

	public void afterBind() {
		super.afterBind();
		if(sessionHelper.isMobile()) {
			jxSetEnable("stm_ref2",false);
			jxSetEnable("vd_vname",false);
		}
	}
}
