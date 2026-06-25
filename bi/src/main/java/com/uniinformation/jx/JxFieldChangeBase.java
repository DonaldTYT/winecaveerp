package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxFieldChangeBase implements JxChangeListener {
   public JxFieldChangeBase(JxField fd) {
	   if (fd != null)
		   fd.addChangeListener(this);
		//fd.addChangeListener(this);
	}
	public JxFieldChangeBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxFieldChange [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
				fd.addChangeListener(this);
			}
		}
	}
	abstract public boolean valueChanged(JxField jxfield,String orgvalue);
}
