package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellPair;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockLedgerEx extends BiResultErpv4 implements BiLedgerReportInterface {
	
	/*
	class AlwaysFirst implements Comparable {
		String str;

		public AlwaysFirst(String p_str) {
			str = p_str;
		}
		@Override
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			if(arg0 instanceof AlwaysFirst) return(str.compareTo(arg0.toString()));
			return 1;
		}
		
		@Override
		public String toString() {
			return(str);
		}
		
	}

	class AlwaysLast implements Comparable {
		String str;

		public AlwaysLast(String p_str) {
			str = p_str;
		}

		@Override
		public int compareTo(Object arg0) {
			if(arg0 instanceof AlwaysLast) return(str.compareTo(arg0.toString()));
			// TODO Auto-generated method stub
			return -1;
		}
		@Override
		public String toString() {
			return(str);
		}
		
	}
	*/

	Cell sdate;
	Cell edate;
	public BiResultStockLedgerEx(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ColumnCell getValue(ledgerColumns lgf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilterStr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCumulatorColumn(String p_columnLabel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCumulatorColumn() {
		// TODO Auto-generated method stub
		return "stmd_date";
	}

	@Override
	public String getReportTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRptCol(CellCollection p_rptCol) {
		sdate = p_rptCol.getCell("sdate");
		edate = p_rptCol.getCell("edate");
	}
	
	enum FuncName { 
		FUNC_getBeginBalance, 
		FUNC_getLocBeginBalance, 
		FUNC_getEndBalance, 
		FUNC_getLocEndBalance, 
		NOT_DEFINED }	
	class ledgerCellCollectionEx extends Erpv4BaseCellCollection {
		
		public ledgerCellCollectionEx(BiCellCollection p_parent) {
			super(p_parent,BiResultStockLedgerEx.this);
		}
		@Override
		public Object evalFunction(String p_fname,Vector p_args) throws Exception {
			FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);
			
			switch (funcName){
			case FUNC_getLocBeginBalance: {
				if(sdate == null || edate == null) return(0.0);
				double firg;
				int irg;
				double forg;
				int org;
				if(p_args.get(0) instanceof Double) {
					firg = (Double) p_args.get(0);
					irg = (int) firg;
				} else {
					irg = (Integer) p_args.get(0);
				}
				if(irg <= 0) return(0.0);
				if(p_args.get(1) instanceof Double) {
					forg = (Double) p_args.get(1);
					org = (int) forg;
				} else {
					org = (Integer) p_args.get(1);
				}
				if(org <= 0) return(0.0);
				String loc = (String) p_args.get(2);
				if(loc == null || loc.equals("")) return(0.0);
				Date d = sdate.getDate();
				if(irg <= 0 || org <= 0 || !d.after(DateUtil.minDate)) return(0.0);
				d = DateUtil.prevday(d);
				return(CostCalculation.getLocBalance(br.getSessionHelper(),irg, org, loc,d));
			}
			case FUNC_getBeginBalance: {
				if(sdate == null || edate == null) return(0.0);
				double firg;
				int irg;
				double forg;
				int org;
				if(p_args.get(0) instanceof Double) {
					firg = (Double) p_args.get(0);
					irg = (int) firg;
				} else {
					irg = (Integer) p_args.get(0);
				}
				if(irg <= 0) return(0.0);
				if(p_args.get(1) instanceof Double) {
					forg = (Double) p_args.get(1);
					org = (int) forg;
				} else {
					org = (Integer) p_args.get(1);
				}
				if(org <= 0) return(0.0);
				Date d = sdate.getDate();
				if(irg <= 0 || org <= 0 || !d.after(DateUtil.minDate)) return(0.0);
				return(CostCalculation.getBalance(br.getSessionHelper(),irg, org,d));
			}
			case FUNC_getLocEndBalance: {
				if(sdate == null || edate == null) return(0.0);
				double firg;
				int irg;
				double forg;
				int org;
				if(p_args.get(0) instanceof Double) {
					firg = (Double) p_args.get(0);
					irg = (int) firg;
				} else {
					irg = (Integer) p_args.get(0);
				}
				if(irg <= 0) return(0.0);
				if(p_args.get(1) instanceof Double) {
					forg = (Double) p_args.get(1);
					org = (int) forg;
				} else {
					org = (Integer) p_args.get(1);
				}
				if(org <= 0) return(0.0);
				String loc = (String) p_args.get(2);
				if(loc == null || loc.equals("")) return(0.0);
				Date d = edate.getDate();
				if(irg <= 0 || org <= 0 || !d.after(DateUtil.minDate)) return(0.0);
				return(CostCalculation.getLocBalance(br.getSessionHelper(),irg, org, loc,d));
			}
			case FUNC_getEndBalance: {
				if(sdate == null || edate == null) return(0.0);
				double firg;
				int irg;
				double forg;
				int org;
				if(p_args.get(0) instanceof Double) {
					firg = (Double) p_args.get(0);
					irg = (int) firg;
				} else {
					irg = (Integer) p_args.get(0);
				}
				if(irg <= 0) return(0.0);
				if(p_args.get(1) instanceof Double) {
					forg = (Double) p_args.get(1);
					org = (int) forg;
				} else {
					org = (Integer) p_args.get(1);
				}
				if(org <= 0) return(0.0);
				Date d = edate.getDate();
				if(irg <= 0 || org <= 0 || !d.after(DateUtil.minDate)) return(0.0);
				return(CostCalculation.getBalance(br.getSessionHelper(),irg, org,d));
			}
			}
			return(super.evalFunction(p_fname,p_args) );
		}
	}

	Hashtable <Integer,Integer> colBeginBalHash;
	Hashtable <Integer,Integer> colEndBalHash;
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new ledgerCellCollectionEx(p_parent));
	}
	@Override
	protected int addOrGetPivotList(List<String>pivotColumns,Object colVals[],AggregateOrPivot aop) throws Exception {
		int idx;
		UniLog.log("addCustomPivotHeader " + pivotColumns.toString());
		if(pivotColumns.contains("stmd_tdtype")) {
			UniLog.log("addCustomPivotHeader detected stmd_tdtype");
			Object obColVals[] = new Object[colVals.length];
			for(int i=0;i < colVals.length;i++) {
				if(pivotColumns.get(i).equals("stmd_tdtype")) {
					obColVals[i] = new CellPair(-1,"");
//					obColVals[i] = new AlwaysFirst("B/F");
				} else {
					obColVals[i] = colVals[i];
				}
			}
			int idx2 = aop.addOrGetPivotList(obColVals);
			Object obColVals3[] = new Object[colVals.length];
			for(int i=0;i < colVals.length;i++) {
				if(pivotColumns.get(i).equals("stmd_tdtype")) {
					obColVals3[i] = new CellPair(1,"");
//					obColVals3[i] = "ZZZZZZZZZZZZ";
				} else {
					obColVals3[i] = colVals[i];
				}
			}
			int idx3 = aop.addOrGetPivotList(obColVals3);
			idx = aop.addOrGetPivotList(colVals);
			colBeginBalHash.put(idx,idx2);
			colEndBalHash.put(idx,idx3);
		} else {
			idx = aop.addOrGetPivotList(colVals);
		}
		return(idx);
	}
	@Override
	protected int getRealPivotColumn( AggregateOrPivot.AggregateRec agg,int idx) {
		if(agg.getKey().equals("pds_beginlocbal")) {
			Integer idxx = colBeginBalHash.get(idx);
			if(idxx != null) {
				return(idxx);
			}
		}
		if(agg.getKey().equals("pds_endlocbal")) {
			Integer idxx = colEndBalHash.get(idx);
			if(idxx != null) {
				return(idxx);
			}
		}
		return(idx);
	}
	@Override
	public void computeAggregateDataSet(AggregateOrPivot p_aop) throws Exception {
		colBeginBalHash = new Hashtable<Integer,Integer>();
		colEndBalHash = new Hashtable<Integer,Integer>();
		super.computeAggregateDataSet(p_aop);
	}
	
	public boolean isStmdTypeCol(int p_idx) {
		if(colBeginBalHash.get(p_idx) != null) return(true);
		if(colEndBalHash.get(p_idx) != null) return(true);
		return(false);
	}
	public boolean isBalBeginCol(int p_idx) {
		return(colBeginBalHash.contains(p_idx));
	}
	public boolean isBalEndCol(int p_idx) {
		return(colEndBalHash.contains(p_idx));
	}

	@Override
	public void setLedgerDate(Date p_openBalDate, Date p_closeBalDate) {
		// TODO Auto-generated method stub
		
	}
}
