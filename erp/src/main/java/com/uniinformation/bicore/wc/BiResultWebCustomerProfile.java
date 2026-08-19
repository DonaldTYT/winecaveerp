package com.uniinformation.bicore.wc;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.winecave.webcore.WinecaveSessionHelper;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class BiResultWebCustomerProfile extends BiResultErpv4 {

	public BiResultWebCustomerProfile(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void afterFetch() {
		fetchOneSubLink(getCurrentCollection(),getSubLink("graphql.TfrDetail"),
				new Wherecl() .andUniop("tfr_from", "=", getCellString("vd_customerCode")).orUniop("tfr_to", "=", getCellString("vd_customerCode")
						)
				
				) ;
	}
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht  = super.addExtraWhereStr(p_where, p_hash);
		String uid = getSelectUtil().getLoginId();
		UniLog.log("user = " + getSelectUtil().getLoginId());
		if(!BiSchema.hasAccessRight(sh, "#allcustomer")) {
			Wherecl wcl1 = new Wherecl();
			String customerLoginId = sh instanceof WinecaveSessionHelper
					? ((WinecaveSessionHelper) sh).getWebLoginId()
					: sh.getVcode();
			wcl1.genInList("and", "vd_loginid", "=", customerLoginId);
			if(BiSchema.hasAccessRight(sh, "!!sales")) {
				try {
				SelectUtil su = getSelectUtil();
				TableRec tr = su.getQueryResult("select * from salesman where sm_logname = ? ",
						new Wherecl().appendArgument(sh.getVcode())
						);
				if(tr.getRecordCount() == 1) {
					tr.setRecPointer(0);
					Wherecl wcl2 = new Wherecl();
					wcl2.genInList("and", "vd_salesman", "=", tr.getFieldString("sm_code"));
					wcl1.orWherecl(wcl2);
				}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			p_where.andWherecl(wcl1);
		} 
		return(ht);
	}
}
