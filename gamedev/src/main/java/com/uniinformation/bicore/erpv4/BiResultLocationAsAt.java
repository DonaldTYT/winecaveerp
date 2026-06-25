package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLocationAsAt extends BiResultErpv4 {

	private enum FuncName { FUNC_getAsAtDate, 
					NOT_DEFINED }
	Date asAtDate;
	class LocationAsAtCellCollection extends Erpv4BaseCellCollection {

		public LocationAsAtCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
			super(p_parent, p_br);
			// TODO Auto-generated constructor stub
		}
		@Override
		public Object evalFunction(String p_fname,Vector p_args) throws Exception {
			FuncName funcName = FuncName.NOT_DEFINED;
			try {
				funcName = FuncName.valueOf("FUNC_"+p_fname);
			}
			catch(Exception ex) {
				//remark: if enum not exist, will got exception here.
			}
			switch (funcName){
				case FUNC_getAsAtDate: {
					return(asAtDate);
				}
			}
			
			return(super.evalFunction(p_fname,p_args) );
		}
		
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new LocationAsAtCellCollection(p_parent, this));
	}

	public BiResultLocationAsAt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub

		// asAtDate = DateUtil.dateTimeStrToDate("2021/01/31");
				
	}
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl1 = null;
		if(Erpv4Config.isMultiCompany(sh)) {
			if(!sh.hasAccessRight("#allloc")) {
			BiColumn locCol = getColumnByLabel("loc_cocode");
			if(locCol != null && columnInSelectList(locCol)) {
				String cocode = Erpv4Config.getDefaultCoCode(sh);
				if(wcl1 == null) wcl1 = new Wherecl();
				wcl1.appendString(" and loc_cocode = '"+cocode+"' ").stripAnd();
			}
			}
		}
		if(wcl1 != null) p_where.andWherecl(wcl1);
		return(ht);
	}
	

	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag)
	{
		asAtDate = DateUtil.maxDate;
		Condition cond = getCustomCondition();
		if(cond != null) {
			try {
				List<Condition> l1 = Condition.serializeCondition(false, cond);
				for(Condition cd : l1) {
					if(cd.get_isPredicate()) {
						String s= cd.get_leftExpression().toString();
						if(s.equals("stmd_date")) {
							Date md;
							switch(cd.get_operator()) {
							case Condition.COMPARE_OP_LE:
							case Condition.COMPARE_OP_EQ:
								asAtDate = cd.get_rightExpression().eval(null).getDate();
								break;
							case Condition.COMPARE_OP_GE:
								asAtDate = DateUtil.maxDate;
								break;
							case Condition.COMPARE_OP_BETWEEN:
								asAtDate = cd.get_rightExpression2().eval(null).getDate();
								break;
							}
						}
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		return(super.query(p_rollback, p_sortFlag));
	}
	public java.util.Date getAsAtDate() {
		return(asAtDate);
	}
	
	
//	@Override
//    public Wherecl getFieldUniqueListAppendWhere(BiColumn p_bc, Wherecl p_where) {
//		Wherecl wcl = p_where;
//		if(Erpv4Config.isMultiCompany(sh)) {
//		if(
//				p_bc.getLabel().equals("loc_code") ||
//				p_bc.getLabel().equals("loc_desc")
//				) {
//			if(wcl == null) wcl = new Wherecl();
//			wcl.andUniop("locationcode.loc_cocode", "=", Erpv4Config.getDefaultCoCode(sh));
//		}
//		}
//    	return(super.getFieldUniqueListAppendWhere(p_bc, wcl));
//    }
	
}
