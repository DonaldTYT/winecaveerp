package com.uniinformation.jxapp;

import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;

public class AfsCustomScr extends JxForm {
	@Override 
	public void afterBind() {
		
	}
	
	public JxField getListBox()
	{
		return(jxAdd("ipk_list"));
		
	}
}
