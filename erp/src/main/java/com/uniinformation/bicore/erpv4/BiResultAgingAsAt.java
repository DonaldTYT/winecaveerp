package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiAsAtReportInterface;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellPair;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;
import org.apache.commons.lang3.tuple.Pair;
public class BiResultAgingAsAt extends BiResultErpv4 implements BiAsAtReportInterface {

	public BiResultAgingAsAt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	private enum FuncName { FUNC_getAsAtDate, FUNC_agingPeriod,FUNC_duePeriod,FUNC_duePeriod2,
					NOT_DEFINED }
	Date asAtDate;
	String asAtColumn;
	HashSet<String>skipPivotColumns;
	HashSet<String>skipSummaryColumns;
	class LocationAsAtCellCollection extends Erpv4BaseCellCollection {

		LocationAsAtCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
			super(p_parent, p_br);
			// TODO Auto-generated constructor stub
		}
		/*
		private HashSet<String>notExistFunc;
		private FuncName checkAndGetFuncName(String p_fname) {
			if(notExistFunc != null && notExistFunc.contains(p_fname)) return(FuncName.NOT_DEFINED);
			try {
				return(FuncName.valueOf("FUNC_"+p_fname));
			}
			catch(Exception ex) {
				//remark: if enum not exist, will got exception here.
				if(notExistFunc == null) notExistFunc = new HashSet<String>();
				notExistFunc.add(p_fname);
				return(FuncName.NOT_DEFINED);
			}
		}	
		*/	
		
		@Override
		public Object evalFunction(String p_fname,Vector p_args) throws Exception {
			/*
			FuncName funcName = FuncName.NOT_DEFINED;
			try {
				funcName = FuncName.valueOf("FUNC_"+p_fname);
			}
			catch(Exception ex) {
				//remark: if enum not exist, will got exception here.
			}
			*/
			/*
			FuncName funcName = checkAndGetFuncName(p_fname);
			*/
			FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);
			switch (funcName){
				case FUNC_getAsAtDate: {
					return(asAtDate);
				}
				case FUNC_duePeriod2: {
					return(CellPair.of(0, "HAHA"));
				}

				case FUNC_duePeriod: {
					java.util.Date d = (java.util.Date) p_args.get(0);
					if(!d.after(DateUtil.minDate)) {
						return(CellPair.of(0, "Overdue Unknown"));
					}
					int jd0 = DateUtil.getJulianDate(asAtDate);
					int jd1 = DateUtil.getJulianDate(d);
					int dd = jd1-jd0;
					/*
					if(dd < -60) return(BiPair.of(1, "Overdue 60+"));
					if(dd < -30) return(BiPair.of(2, "Overdue 30-60"));
					if(dd < 0) return(BiPair.of(3, "Overdue 1-30"));
					if(dd < 30) return(BiPair.of(4, "Current"));
					if(dd < 60) return(BiPair.of(5, "30-60"));
					return(BiPair.of(6, "60+"));
					*/
					if(dd < -90) return(CellPair.of(4, "90+"));
					if(dd < -60) return(CellPair.of(3, "61-90"));
					if(dd < -30) return(CellPair.of(2, "31-60"));
					return(CellPair.of(1, "0-30"));
					
					/*
					if(dd <= 0) return("A:Overdue");
					if(dd <= 30) return("B:0-30");
					if(dd <= 60) return("C:31-60");
					if(dd <= 90) return("D:61-90");
					if(dd <= 180) return("E:91-180");
					if(dd <= 365) return("F:181-365");
					return("E:>365");
					*/
				}
				case FUNC_agingPeriod: {
					java.util.Date d = (java.util.Date) p_args.get(0);
					if(!d.after(DateUtil.minDate)) return("");
					int jd0 = DateUtil.getJulianDate(asAtDate);
					int jd1 = DateUtil.getJulianDate(d);
					int dd = jd0-jd1;
					if(dd <= 30) return("A:0-30");
					if(dd <= 60) return("B:31-60");
					if(dd <= 90) return("C:61-90");
					if(dd <= 180) return("D:91-180");
					if(dd <= 365) return("E:181-365");
					return("E:>365");
				}
			}
			
			return(super.evalFunction(p_fname,p_args) );
		}
		
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new LocationAsAtCellCollection(p_parent, this));
	}

//	public BiResultStockAsAt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
//			SessionHelper p_sh) throws CellException {
//		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
//	}
	
	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag)
	{
		asAtDate = DateUtil.prevday(DateUtil.maxDate); // * date range should be less than maxDate
		Condition cond = getCustomCondition();
		if(cond != null) {
			try {
				List<Condition> l1 = Condition.serializeCondition(false, cond);
				for(Condition cd : l1) {
					if(cd.get_isPredicate()) {
						String s= cd.get_leftExpression().toString();
						if(s.equals(asAtColumn)) {
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
	
	public void setAsAtColumn(String p_AsAtColumn) {
		asAtColumn = p_AsAtColumn;
	}

//	@Override
//	public BiColumn getCumulatorComparable() {
//		return getView().getColumnByLabel("stmd_date");
//	}

	@Override
	public String getFilterStr() {
		String s = "";
		if(asAtDate != null && asAtDate.after(DateUtil.zeroDate)) {
			s += "As At" + DateUtil.toDateString(asAtDate, "yyyy-mmm-dd");
		}
		return(s);
	}

	@Override
	public String getAsAtColumn() {
		// TODO Auto-generated method stub
		return asAtColumn;
	}

	@Override
	public void setSkipPivotColumns(HashSet<String> p_cols) {
		// TODO Auto-generated method stub
		skipPivotColumns = p_cols;
	}

	@Override
	public boolean skipForPivot(String p_aggLabel,AggregateOrPivotHeader p_aop) {
		// TODO Auto-generated method stub
		return(skipPivotColumns != null && skipPivotColumns.contains(p_aggLabel));
	}

	@Override
	public void setSkipSummaryColumns(HashSet<String> p_cols) {
		// TODO Auto-generated method stub
		skipSummaryColumns = p_cols;
	}

	@Override
	public boolean skipForSummary(String p_aggLabel, AggregateOrPivotHeader p_app) {
		// TODO Auto-generated method stub
		return(skipSummaryColumns != null && skipSummaryColumns.contains(p_aggLabel));
	}

	@Override
	public boolean setFifoAging(boolean sw) {
		// TODO Auto-generated method stub
		return(false);
	}

//	@Override
//	public BiColumn getCumulatorKey() {
//		return(getView().getColumnByLabel("st_irg"));
//	}
}
