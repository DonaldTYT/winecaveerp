package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockMovement extends BiResultErpv4 {
	Date fromDate;
	Date toDate;
	private enum FuncName { FUNC_getFromDate, FUNC_getToDate,NOT_DEFINED }

	class StockMovementCellCollection extends Erpv4BaseCellCollection {

		public StockMovementCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
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
				case FUNC_getFromDate: {
					return(fromDate);
				}
				case FUNC_getToDate: {
					return(fromDate);
				}
			}
			
			return(super.evalFunction(p_fname,p_args) );
		}
		
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new StockMovementCellCollection(p_parent, this));
	}
	public BiResultStockMovement(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
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
			if(getCell("loc_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and loc_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
				ht.add(getView().getSchema().getTable("locationcode"));
			} else if(getCell("stm_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stm_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
//				ht.add(getView().getSchema().getTable("locationcode"));
			}
			return(ht);
		} else return(ht);
	}
	
	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag)
	{
		fromDate = DateUtil.zeroDate;
		toDate = DateUtil.maxDate;
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
							case Condition.COMPARE_OP_LT:
								toDate = DateUtil.prevday(cd.get_rightExpression().eval(null).getDate());
								break;
							case Condition.COMPARE_OP_LE:
								toDate = cd.get_rightExpression().eval(null).getDate();
								break;
							case Condition.COMPARE_OP_EQ:
								fromDate = cd.get_rightExpression().eval(null).getDate();
								toDate = cd.get_rightExpression().eval(null).getDate();
								break;
							case Condition.COMPARE_OP_GT:
								fromDate = DateUtil.nextday(DateUtil.maxDate);
								break;
							case Condition.COMPARE_OP_GE:
								fromDate = DateUtil.maxDate;
								break;
							case Condition.COMPARE_OP_BETWEEN:
								fromDate = cd.get_rightExpression1().eval(null).getDate();
								toDate = cd.get_rightExpression2().eval(null).getDate();
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
	@Override
	public String getLinkedView(String p_colName,CellCollection p_col) {
		if(p_colName.equals("stm_ref1")) {
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
		return(super.getLinkedView(p_colName,p_col));
	}
	@Override
    public String getLinkedColumn(String p_colName) {
    	if(p_colName.equals("stm_ref1")) {
			return("stm_ref1");
    	}
    	return(super.getLinkedColumn(p_colName));
    }	
	@Override
    protected String brEvalFunction(String p_functName,List p_args) {
    	if(p_functName.equals("brGetStmdModule")) {
    		return("stmov_any.stm_module");
    	}
		return(super.brEvalFunction(p_functName, p_args));
    }
}
