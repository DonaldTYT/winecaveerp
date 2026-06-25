package com.uniinformation.bicore.wc;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

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
}
