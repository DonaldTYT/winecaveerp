package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultStockMovement.StockMovementCellCollection;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLocationMovement extends BiResultErpv4 {
//	Date fromDate;
//	Date toDate;
//	
	private enum FuncName { FUNC_getPivotStr, NOT_DEFINED }


	class LocationMovementCellCollection extends Erpv4BaseCellCollection {

		public LocationMovementCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
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
				case FUNC_getPivotStr: {
					Date d = (Date) p_args.get(0);
					String loc = (String) p_args.get(4);
					if(loc.equals("")) return("");
					String t = (String) p_args.get(1);
					String fl = (String) p_args.get(2);
					String tl = (String) p_args.get(3);
					boolean byDate = getCell("pivotDate").getBoolean();
					boolean byType = getCell("pivotType").getBoolean();
					boolean byLoc = getCell("pivotLoc").getBoolean();
					String ss = "";
					if(byDate && d.after(DateUtil.minDate)) ss += DateUtil.toDateString(d, "yyyy/mm/dd") + " ";
					if(byType) ss += t + " ";
					if(byLoc) {
						if(fl.equals(tl)) {
							ss += fl + " ";
						} else {
							if(!fl.equals("")) {
								ss += "from " + fl + " ";
							}
							if(!tl.equals("")) {
								ss += "to " + tl + " ";
							}
						}
						/*
						if(!fl.equals("") && !fl.equals(getCellString("pds_loc"))) {
							ss += "from " + fl + " ";
						}
						if(!tl.equals("") && !tl.equals(getCellString("pds_loc"))) {
							ss += "to " + tl + " ";
						}
						*/
					}

					ss = ss.trim();
					if(ss.equals("")) {
						return("-"); 
					} else {
						return(ss);
					}
				}
			}
			
			return(super.evalFunction(p_fname,p_args) );
		}
		
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new LocationMovementCellCollection(p_parent, this));
	}
	
	
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		
		Cell c;
		p_col.addCell("rptFrDate", new ColumnCell(DateUtil.zeroDate,Cell.VMODE_DISPONLY));
		p_col.addCell("rptToDate", new ColumnCell(DateUtil.maxDate,Cell.VMODE_DISPONLY));
		p_col.addCell("aggregateExpression", new ColumnCell("",Cell.VMODE_NORMAL));
		p_col.addCell("aggregateFunction", new ColumnCell(AggregateOrPivot.AGGREGATES.SUM.toString() ,Cell.VMODE_NORMAL));
		p_col.addCell("pivotColumn", new ColumnCell("",Cell.VMODE_NORMAL));
		c = p_col.addCell("pivotDate", new ColumnCell(false,Cell.VMODE_NORMAL));
		c = p_col.addCell("pivotType", new ColumnCell(false,Cell.VMODE_NORMAL));
		c = p_col.addCell("pivotLoc", new ColumnCell(false,Cell.VMODE_NORMAL));
		super.createColumnCells(p_col);
	}
	
	public BiResultLocationMovement(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
//		fromDate = DateUtil.zeroDate;
//		toDate = DateUtil.maxDate;
		setQueryIncludeNoDetail(true);
	}
	
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl1 = null;
		if(Erpv4Config.isMultiCompany(sh)) {
			BiColumn locCol = getColumnByLabel("loc_cocode");
			if(locCol != null && columnInSelectList(locCol)) {
				String cocode = Erpv4Config.getDefaultCoCode(sh);
				if(wcl1 == null) wcl1 = new Wherecl();
				wcl1.appendString(" and loc_cocode = '"+cocode+"' ").stripAnd();
			}
		}
		if(wcl1 != null) p_where.andWherecl(wcl1);
		return(ht);
	}


	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag)
	{
		Date fromDate = DateUtil.zeroDate;
		Date toDate = DateUtil.prevday(DateUtil.maxDate);
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
								fromDate = DateUtil.nextday(cd.get_rightExpression().eval(null).getDate());
								break;
							case Condition.COMPARE_OP_GE:
								fromDate = cd.get_rightExpression().eval(null).getDate();
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
		try {

			if(fromDate.after(DateUtil.zeroDate)) {
				fromDate = DateUtil.prevday(fromDate);
			}
			getCell("rptFrDate").set(fromDate);
			getCell("rptToDate").set(toDate);
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		return(super.query(p_rollback, p_sortFlag));
	}	
	@Override
    protected String brEvalFunction(String p_functName,List p_args) {
    	if(p_functName.equals("brGetStmdModule")) {
    		return("stmov_any.stm_module");
    	}
		return(super.brEvalFunction(p_functName, p_args));
    }	
}
