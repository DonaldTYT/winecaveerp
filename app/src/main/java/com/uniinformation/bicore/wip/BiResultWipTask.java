package com.uniinformation.bicore.wip;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultWipTask extends BiResult {

	public BiResultWipTask(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getColumnDisplayClass(ColumnCell p_cell) {
		if((!p_cell.getCellLabel().equals("wfmjt_state")) 
			/* && (!p_cell.getCellLabel().equals("wfmj_updtime")) */) return(null);
		Date d = getCell("wfmjt_updtime").getDate();
		long dd = new Date().getTime();
		dd -= d.getTime();
		if(dd < 60000) {
			return("myLabelNews");
		} else {
			return(null);
		}
	}
	/*
	@Override
	public String getColumnDisplayString(ColumnCell p_cell) {
		String s = super.getColumnDisplayString(p_cell);
		if(p_cell.getCellLabel().equals("wfmj_state")) {
			if(!getCellString("wfmj_updstr").equals("")) {
				return(getCellString("wfmj_updstr"));
			}
		}
		if(p_cell.getCellLabel().equals("wfmj_timeout")) {
			if(s.endsWith("00:00:00")) {
				return("On "+DateUtil.toDateString(p_cell.getDate(), "yyyy/mm/dd"));
			}
			if(s.endsWith("23:59:59")) {
				return("By "+DateUtil.toDateString(p_cell.getDate(), "yyyy/mm/dd"));
			}
			if(s.trim().equals("")) {
				return("ASAP");
			}
		}
		return(s);
	}	
	*/
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht  = super.addExtraWhereStr(p_where, p_hash);
		String uid = getSelectUtil().getLoginId();
		UniLog.log("user = " + getSelectUtil().getLoginId());
		if(!BiSchema.hasAccessRight(sh, "!!allwip")) {
//			p_where.appendString(" and inv_assignby = '"+uid+"' ");
			
			
//			p_where.genInList("and", "inv_assignby", "in", sh.getAccessUsers());
			Wherecl wcl1 = new Wherecl();
			wcl1.genInList("and", "wfmjt_assignto", "in", getSessionHelper().getMatchedAccessRights("!!wip"));
			/*
			wcl1.genInList("and", "wfmjt_assignto", "in", sh.getAccessUsers());
			HashSet<String> deptlist = getSessionHelper().getMatchedAccessRights("!!wip");
			if(deptlist != null && !deptlist.isEmpty()) {
				Wherecl wcl2 = new Wherecl();
				String ss = null;
				for(String as : deptlist) {
					if(ss == null) ss = (
							"wfmjt_assignto in (select quodetxx.ind_rg from quodet quodetxx,stmcmodel stmcmodelxx where stmcmodelxx.stmcm_rg = quodetxx.ind_srg and stmcmodelxx.stmcm_code in('"
							+as+"')"); else ss += ",'"+as+"'";
				}
				ss += ")";
				wcl2.appendString(ss);
				wcl1.orWherecl(wcl2);
			}
			*/
			p_where.andWherecl(wcl1);
		} 
		return(ht);
	}
}
