package com.uniinformation.cell;
import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.reflect.*;
import java.lang.Exception.*;
import com.uniinformation.utils.*;
import com.uniinformation.utils.exprpar.*;
public class CellCollectionUtil
{  
	private static CellCollection getOnePageLInk(String p_type, int p_offset, int p_cnt) {
	   CellCollection cc = new CellCollection();
	   cc.putValue("pageType", p_type);
	   cc.putValue("pageIdx", p_offset / p_cnt);
	   cc.putValue("offset", p_offset);
	   cc.putValue("cnt", p_cnt);
	   return(cc);
	}
   public static CellVector getPageLinks(
	                            int p_offset,
										 int p_cntInPage, 
										 int p_ttlcnt,
										 int p_linkShown) {
	   CellVector cv = new CellVector();
		if (p_offset > 0) {
		   int offset0 = p_offset - p_cntInPage;
		   if (offset0 < 0)
			   offset0 = 0;
	      cv.addElement(getOnePageLInk("prevPage", 
			                             offset0,
												  p_offset-offset0));
		}
	   int masterOffset = p_offset - p_offset % (p_linkShown * p_cntInPage);
		if (masterOffset > 0)
	      cv.addElement(getOnePageLInk("prevPages", 
			                             masterOffset-p_cntInPage, 
												  p_cntInPage));
	   for (int i=0; i<p_linkShown; i++) {
			int curOffset = masterOffset + (i * p_cntInPage);
		   if (curOffset >= p_ttlcnt)
			   break;
			boolean isCurrent = (curOffset <= p_offset) && (p_offset < (curOffset + p_cntInPage));
	      cv.addElement(getOnePageLInk(isCurrent ? "current" : "some", 
			                             curOffset, 
												  p_cntInPage));
		}
		if (p_ttlcnt > (masterOffset + p_cntInPage * p_linkShown))
	      cv.addElement(getOnePageLInk("nextPages", 
			                             masterOffset+p_cntInPage*p_linkShown, 
												  p_cntInPage));
		if (p_offset+p_cntInPage < p_ttlcnt)
	      cv.addElement(getOnePageLInk("nextPage", 
			                             p_offset+p_cntInPage,
												  p_cntInPage));
	   return(cv);
	}
}
