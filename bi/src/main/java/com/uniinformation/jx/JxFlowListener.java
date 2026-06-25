package com.uniinformation.jx;
import java.util.*;
import com.uniinformation.utils.*;

public class JxFlowListener {
   public JxFlowListener(JxField fd) {
	   if (fd != null)
		   fd.addFlowListener(this);
	}
	public JxFlowListener(JxForm form, String fieldlist) {
		String s;
		JxField fd;
		for (StringTokenizer token = new StringTokenizer(fieldlist);
			  token.hasMoreTokens();) {
			s = token.nextToken();
			UniLog.log("JxFieldFlowAddLink [" + s + "]");
			if ((fd = form.jxAdd(s)) != null) {
		      fd.addFlowListener(this);
			}
		}
	}
   // return key of the new node
   public String afterAddNode(JxField field) { 
      return(null);
   }
   public boolean beforeAddLink(JxField field, String orgKey, String destKey) {
      return(true);
   }
   // return key of the new link
   public String afterAddLink(JxField field, String orgKey, String destKey) {
      return(null);
   }
   // return false to cancel edit
   public boolean beforeEdit(JxField field, String nodeKey) {
      return(true);
   }
   public void afterEdit(JxField field, String nodeKey) {
      return;
   }
}
