package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultWebMenuTree extends BiResult {

	public BiResultWebMenuTree(BiResult p_parent, BiView p_view,
			SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(p_lookupTable.getName().equals("userandaccess")) {
			BiResult pr = getParent();
			if(pr != null && pr.getCell("lgu_login") != null) {
				if(wcl == null ) wcl = new Wherecl();
				wcl.andUniop("lgu_login", "<>", pr.getCell("lgu_login").getString());
			}
			if(BiSchema.hasAccessRight(sh, "#super") || sh.isAdminUser()) {
				
			} else {
//				HashSet<String> deptlist = sh.getMatchedAccessRights("^dept");
				HashSet<String> accesslist = sh.getAccessRights();
				String ss = null;
				for(String as : accesslist) {
					if(ss == null)  {
						ss = " and (lgu_access = '' or lgu_access in ('"+ as + "'";
					} else ss += ",'"+as+"'";
				}
				ss += ")) ";
				wcl.appendString(ss);
			}
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
}
