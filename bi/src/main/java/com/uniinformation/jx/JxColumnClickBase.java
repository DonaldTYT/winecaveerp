package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxColumnClickBase implements JxColumnClickListener {
   public JxColumnClickBase(JxField fd) {
	   if (fd != null)
		   fd.addColumnClickListener(this);
		//fd.addColumnClickListener(this);
	}
   public JxColumnClickBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxColumnClick [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
			   fd.addColumnClickListener(this);
			}
		}
	}
	abstract public void clicked(JxField jxfield,int col);
}
