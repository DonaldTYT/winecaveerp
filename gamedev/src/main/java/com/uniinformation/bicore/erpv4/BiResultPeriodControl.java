package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPeriodControl extends BiResultErpv4{
	public BiResultPeriodControl(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl1 = null;
		String cocode = Erpv4Config.getDefaultCoCode(sh);
		if(Erpv4Config.isMultiCompany(sh)) {
			BiColumn locCol = getColumnByLabel("pc_cocode");
			if(locCol != null && columnInSelectList(locCol)) {
				if(wcl1 == null) wcl1 = new Wherecl();
				wcl1.appendString(" and pc_cocode = '"+cocode+"' ").stripAnd();
			}
		}
		/*
		try {
			TableRec tr = getSelectUtil().getQueryResult("select * from maxpc where mp_cocode = '" + cocode + "'");
			if(wcl1 == null) wcl1 = new Wherecl();
			tr.setRecPointer(0);
			wcl1.andUniop("pc_pstart", "<=", tr.getFieldDate("mp_pstart"));
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		*/
		if(wcl1 != null) p_where.andWherecl(wcl1);
		return(ht);
	}
}
