package com.uniinformation.jxapp.erpv4;


public class SihAr extends Sih {
	@Override
	public void afterBind() {
		sihViewId = "erpv4.SihAr";
		sidViewId = "erpv4.Sid";
		snoRgControl = "arinv";
		super.afterBind();
	}

}
