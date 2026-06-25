package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultArAp extends BiResultErpv4{
	Date asAtDate = DateUtil.zeroDate;
	public BiResultArAp(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{

		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		p_where.andUniop("stm_ref1", ">", "");
		if(asAtDate.after(DateUtil.minDate)) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and sih_date <= '"+DateUtil.dateToDateTimeStr(asAtDate, "yyyy/MM/dd")+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
		}
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			if(getCell("sih_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and sih_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
			}
			return(ht);
		} else return(ht);
	}
	
	public void setAsAtDate(Date p_date) {
		asAtDate = p_date;
	}
	
	
	@Override
	public String getLinkedView(String p_colName,CellCollection p_col) {
		if(p_colName.equals("inv_srcref")) {
			String fxref = p_col.getCellString("stm_ref1");
			if(fxref.isEmpty()) {
				fxref = p_col.getCellString("inv_invno");
				if(!fxref.isEmpty()) return("erpv4.Quotation");
			} else {
				String stmtype = p_col.getCellString("stm_type");
				if(stmtype.equals("PD")) return("erpv4.PO");
				if(stmtype.equals("GM")) return("erpv4.GR");
				if(stmtype.equals("MO")) {
					String module = p_col.getCellString("stm_module");
					if(module.equals("cstmo")) return("erpv4.MoCustomer");
					if(module.equals("vstmo")) return("erpv4.MoSupplier");
					if(module.equals("sttfr")) return("erpv4.MoTransfer");
					if(module.equals("stadj")) return("erpv4.MoAdjustment");
					if(module.equals("stake")) return("erpv4.StockTake");
					if(module.equals("stkg2")) return("erpv4.StockTakeG2");
				}
			}
		}
		return(super.getLinkedView(p_colName,p_col));
	}
	
	@Override
    public String getLinkedColumn(String p_colName) {
    	if(p_colName.equals("inv_srcref")) {
			String fxref = getCellString("stm_ref1");
			if(!fxref.isEmpty()) return("stm_ref1");
			fxref = getCellString("inv_invno");
			if(!fxref.isEmpty()) return("inv_invno");
    	}
    	return(super.getLinkedColumn(p_colName));
    }
}
