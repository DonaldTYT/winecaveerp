package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
//import com.uniinformation.jxapp.*;
public abstract class JxFormCloseBase implements JxFormCloseListener {
	public JxFormCloseBase(JxForm form) {
		UniLog.log("JxFormClose " + form.getName() != null ? form.getName() : "");
		form.addFormCloseListener(this);
	}
	abstract public int formClose(JxForm p_form);
}
