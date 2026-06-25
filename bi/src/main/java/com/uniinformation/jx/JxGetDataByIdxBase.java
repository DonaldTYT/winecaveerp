package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;
public abstract class JxGetDataByIdxBase implements JxGetDataIdxListener {
   public JxGetDataByIdxBase(JxField fd) {
	   if (fd != null)
		    fd.addGetDataIdxListener(this);
	}
	public JxGetDataByIdxBase(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxGetDataByIdx [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
		    	fd.addGetDataIdxListener(this);
			}
		}
	}
	abstract public void getData(JxField jxfield, int idx);
}
