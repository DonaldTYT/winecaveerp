package com.uniinformation.jxapp.erpv4;

import com.uniinformation.jxapp.JxZkBiBase;

public class CustomerG2 extends JxZkBiErpv4 {
	@Override
	public void afterBind() {
		super.afterBind();
		LOCK_RECORD_FOR_UPDATE = true;
	}
}
