package com.uniinformation.bicore.wc;

import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultApiStockInfo extends BiResult {
	boolean useFilter=true;
	boolean excludeMajor=true;
	
	public BiResultApiStockInfo(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(useFilter) {
			if(excludeMajor) {
			Wherecl wcl0 = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders,stock st2 where or_org = pdls_org and st2.st_irg = pdls_irg and pdls_loc = 'WH01' and or_cocode <> 'MAJOR1' and pdls_stockqty > 0 ) ");
				p_where.andWherecl(wcl0);
			} else {
			Wherecl wcl0 = new Wherecl().appendString( " and st_irg in (select pdls_irg from podetlocstatus,orders,stock st2 where or_org = pdls_org and st2.st_irg = pdls_irg and pdls_loc = 'WH01' and pdls_stockqty > 0 ) ");
				p_where.andWherecl(wcl0);
			}
		}
		return(ht);
	}
	
	public void setUseFilter(boolean p_sw) {
		useFilter = p_sw;
	}
	public void setExcludeMajor(boolean p_sw) {
		excludeMajor = p_sw;
	}
}
