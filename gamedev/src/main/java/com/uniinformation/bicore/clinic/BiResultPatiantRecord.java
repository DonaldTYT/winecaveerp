package com.uniinformation.bicore.clinic;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPatiantRecord extends BiResult {

	public BiResultPatiantRecord(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where)
	{
		String uid = getSelectUtil().getLoginId();
		UniLog.log("user = " + getSelectUtil().getLoginId());
		if(!BiSchema.hasAccessRight(sh, "#alldoctor")) {
			try {
				TableRec tr;
				tr = getSelectUtil().getQueryResult("select * from cldoctor where cldoc_login = '"+uid+"'");
				if(tr.getRecordCount() != 1) {
					p_where.appendString(" and 1 = 0 ");
					return(null);
				}
				tr.setRecPointer(0);
				int doctorRg = tr.getFieldInt("cldoc_rg");
				Wherecl wcl1 = new Wherecl();
				p_where.appendString(" and (clcl_refdoctor = " + doctorRg + " or clcl_rg in (select clcld_rg from clclaimd where clcld_doctor = '"+doctorRg+"'))");
			} catch (Exception ex) {
				UniLog.log(ex);
				p_where.appendString(" and 1 = 0 ");
			}
		} 
		return(null);
	}
}
