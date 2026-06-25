package com.uniinformation.bicore.erpv4;

import com.uniinformation.bicore.BiField;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiTableStock extends BiTable {

	public BiTableStock(CellCollection parent, String p_tableName, String p_dbtName, String p_eName, String p_cName,
			String p_selectWhere, String p_primaryKey, String p_serialid) {
		super(parent, p_tableName, p_dbtName, p_eName, p_cName, p_selectWhere, p_primaryKey, p_serialid);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected Wherecl getFieldUniqueListAppendWhere(BiField p_fd,SessionHelper p_sh) {
		Wherecl wcl = super.getFieldUniqueListAppendWhere(p_fd, p_sh);
		if(Erpv4Config.isMultiCompany(p_sh)) {
			if(!p_sh.hasAccessRight("#multicomp")) {
				if(wcl == null) wcl = new Wherecl();
				wcl.appendString(" and st_irg in (select st_stirg from costock where st_cocode = '"+Erpv4Config.getDefaultCoCode(p_sh)+"')").stripAnd();
			}
		}
		return(wcl);
	}
}
