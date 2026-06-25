package com.uniinformation.bicore.axa;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAxaSta2 extends BiResultErpv4 {

	public BiResultAxaSta2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
//		int classno = ((BiResultAxaClaim) getParent()).getClassno();
		int classno = getParent().getCell("axacvp_class").getInt();
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		
		BiResult parent = getParent();
//		Date cdate = parent.getCellDate("axaclm_date");
//		String phno = parent.getCellString("axaclm_phno");
//		Date pdate = BiResultAxaClaim.getPhrEffectDate(phno, cdate, su);
		Date edate = parent.getCellDate("axaclm_poledate");
//		Date adate = parent.getCellDate("axaphr_lastandate");
		Date adate = parent.getCellDate("axaclm_phrstdate");
		Wherecl wcl1 = new Wherecl();
		wcl1.appendString(" and axasta_lastandate = '" +DateUtil.toDateString(adate,"yyyy/mm/dd") + "' and axapol_class = " + classno + " and axapol_effectdate = '"+DateUtil.toDateString(edate,"yyyy/mm/dd")+"' ").stripAnd();
		p_where.andWherecl(wcl1);
		/*
		if(ht == null) {
			ht = new HashSet<BiTable>();
		}
		ht.add(getView().getSchema().getTable("axabfcode"));
		*/
		return(ht);
	}
}
