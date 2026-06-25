package com.uniinformation.bicore.erpv4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockList extends BiResultErpv4 {
	public BiResultStockList(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			if(!sh.hasAccessRight("#allloc")) {
			if(getCell("loc_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and loc_cocode = '"+cocode+"' ").stripAnd();
				if(Erpv4Config.isMultiStockPrice(sh)) {
					int lcrg = Erpv4Config.getDefaultLcrg(sh);
					wcl1.appendString(" and loc_mrg = " + lcrg).stripAnd();
				} 
				p_where.andWherecl(wcl1);
				
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
				ht.add(getView().getSchema().getTable("locationcode"));
			}
			}
			return(ht);
		} else return(ht);
	}
}
