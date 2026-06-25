package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultInvoice;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsInvoice extends BiResultInvoice {

	public BiResultAfsInvoice(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected String makeInvoiceItemDescription(String indPrefix,CellCollection c,PrtdocJson ppj) {
		String s = "";
		/*
		if(c.testCell(indPrefix+"desc") != null)  {
			s += c.getCellString(indPrefix+"desc");
		}
		*/
		if(c.testCell(indPrefix+"subitem") != null) {
			if(!c.testCell(indPrefix+"subitem").getBoolean()) {
				if(c.testCell("stmcm_name") != null)  {
					ppj.setBold(true);
//					ppj.setUnderLine(true);
					s += c.getCellString("stmcm_name");
				}
			}
		}

		if(c.testCell(indPrefix+"irg") != null) {
			if(c.testCell(indPrefix+"irg").getInt() > 0) {
				if(c.testCell("st_mtype") != null) {
					String mt = c.getCellString("st_mtype");
					if(mt.equals("M")) {
						String md = c.getCellString("st_modelno");
						if(!md.isEmpty()) {
							s += md + " ";
						}
					}
					if(mt.equals("P")) {
						String pn = c.getCellString("st_oicode");
						if(!pn.isEmpty()) {
							s += pn + " ";
						}
					}
				}
				if(c.testCell("st_iname") != null)  {
					s += c.getCellString("st_iname");
				}
			}
		}
		if(c.testCell(indPrefix+"desc") != null) {
				s += c.getCellString(indPrefix+"desc");
		}
		return(s);
	}
}
