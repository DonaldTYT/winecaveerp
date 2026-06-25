package com.uniinformation.jxapp.erpv4;


public class SihAp extends Sih {
	@Override
	public void afterBind() {
		sihViewId = "erpv4.SihAp";
		sidViewId = "erpv4.Sid";
		snoRgControl = "apinv";
		super.afterBind();
	}

}
