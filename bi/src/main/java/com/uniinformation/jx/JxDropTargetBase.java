package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxDropTargetBase implements JxDropTargetListener {
	public JxDropTargetBase(JxField fd) {
	   if (fd != null)
		   fd.addDropTargetListener(this);
		//fd.addDropTargetListener(this);
	}
	public JxDropTargetBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxDropTarget [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
				fd.addDropTargetListener(this);
			}
		}
	}
	abstract public boolean actionPerformed(JxField jxfield,int x,int y);
}
