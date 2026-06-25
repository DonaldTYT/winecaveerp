package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxDblClickBase implements JxDblClickListener {
   public JxDblClickBase(JxField fd) {
	   if (fd != null)
		   fd.addDblClickListener(this);
		//fd.addDblClickListener(this);
	}
   public JxDblClickBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxDblClick [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
			   fd.addDblClickListener(this);
			}
		}
	}
	abstract public void actionPerformed(JxField jxfield);
}
