package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxFieldSelectBase implements JxSelectListener {
	public JxFieldSelectBase(JxField fd) {
	   if (fd != null)
		   fd.addSelectListener(this);
	   fd.addSelectListener(this);
	}
	public JxFieldSelectBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxFieldSelect [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
				fd.addSelectListener(this);
			}
		}
	}
	public abstract void fieldSelected(JxField jxfield);
}
