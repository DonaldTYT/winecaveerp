package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxFieldActionBase implements JxActionListener {
   public JxFieldActionBase(JxField fd) {
	   if (fd != null)
		    fd.addActionListener(this);
		//fd.addActionListener(this);
	}
	public JxFieldActionBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxFieldAction [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
				fd.addActionListener(this);
			}
		}
	}
	abstract public void actionPerformed(JxField jxfield);
	
	static public void setActionListener(JxForm form,String fieldlist,JxActionListener l)
	{
		String s;
		JxField fd;
		if(fieldlist == null) return;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxField Clear Action [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
				fd.addActionListener(l);
			}
		}
	}
}
