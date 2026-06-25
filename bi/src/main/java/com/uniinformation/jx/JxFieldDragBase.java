package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxFieldDragBase implements JxDragListener {
   public JxFieldDragBase(JxField fd) {
	   if (fd != null)
		   fd.addDragListener(this);
		//fd.addDragListener(this);
	}
   public JxFieldDragBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
		   s = token.nextToken();
			UniLog.log("JxDrag [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
				fd.addDragListener(this);
			}
		}
	}
	abstract public void actionPerformed(JxField jxfield);
}
