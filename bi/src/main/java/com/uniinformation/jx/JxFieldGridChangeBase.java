package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxFieldGridChangeBase implements JxGridChangeListener {
   public JxFieldGridChangeBase(JxField fd) {
		if (fd != null)
		   fd.addGridChangeListener(this);
		fd.addGridChangeListener(this);
	}
	public JxFieldGridChangeBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
		   s = token.nextToken();
			UniLog.log("JxFieldGridChange [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
				fd.addGridChangeListener(this);
			}
		}
	}
	abstract public boolean valueChanged(JxField jxfield,int col,int row,String orgvalue);
}
