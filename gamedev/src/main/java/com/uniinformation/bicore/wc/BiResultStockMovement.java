package com.uniinformation.bicore.wc;

import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
public class BiResultStockMovement extends BiResult {
	public BiResultStockMovement(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		queryIncludeNoDetail = true;
		sortAggregates = false;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		String cocode = Erpv4Config.getDefaultCoCode(sh);
		Wherecl wcl0 = new Wherecl();
		wcl0.appendString(" and stmd_date >= '"+DateUtil.toDateString(getNativeCell("fromDate").getDate(), "yyyy/mm/dd")+"' ").stripAnd();
		p_where.andWherecl(wcl0);
		if(Erpv4Config.isMultiCompany(sh)) {
			/*
			if(getCell("ca_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and ca_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
			}
			*/
			return(ht);
		} else return(ht);
	}
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		BiCellCollection col = super.createColumnCollection(p_parent);
		col.addCell("fromDate", new Cell(DateUtil.today(),Cell.VMODE_NORMAL));
		col.addCell("toDate", new Cell(DateUtil.today(),Cell.VMODE_DISPONLY));
		return(col);
	}
	
}
