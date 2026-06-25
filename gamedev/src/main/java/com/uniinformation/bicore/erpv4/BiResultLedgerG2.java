package com.uniinformation.bicore.erpv4;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.uniinformation.accumulator.CalculationErrorException;
import com.uniinformation.accumulator.CostCalculator;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellPair;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.BiConfig;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public abstract class BiResultLedgerG2 extends BiResultErpv4 implements BiLedgerReportInterface {
	
//	int defaultOrg;
//	int keyFieldPosition;
	String cumulatorColumn;
	protected String rowtypeColumn;
	String activeAccountCondition;
	protected HashSet<String>summaryTables;
	protected CellCollection rptCol;
	protected boolean useFIFO=false;
	
	public class LedgerCostCalculator extends CostCalculator {

		Object key;
		public LedgerCostCalculator(Comparable p_maxValue,Object p_key) {
			super(p_maxValue,CostCalculation.useNewCosting(sh),useFIFO);
			key = p_key;
			// TODO Auto-generated constructor stub
		}
		@Override
		public double getAverageCost(Comparable p_date) throws CalculationErrorException {

			try {
				double  avc = super.getAverageCost(p_date);
				return(avc);
			} catch (CalculationErrorException ex) {
				UniLog.log("cost error for key " + key + " on " + p_date);
				throw(ex);
			}
		}
	}
	
//	BiResultLocationAsAt bbr = null;
	BiResult bbr = null;
	public BiResultLedgerG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		balBeginDate = Erpv4Config.getCostOpeningErpDate(sh);
//		try {
//			defaultOrg = BiConfig.getCoWtAvOrg(sh, BiConfig.getDefaultCoCode(sh));
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
		//keyFieldPosition = getSelectFieldPosition( getCumulatorKey() );
		//if(keyFieldPosition < 0) throw new CellException("Error : Ledger Key Field Invalid");
		// TODO Auto-generated constructor stub
	}	

//	class OpenBalance {
//		Date d;
//		double balance;
//		public OpenBalance(Date p_d,double p_bal) {
//			d = p_d;
//			balance = p_bal;
//		}
//	}
	Date balBeginDate = null;
	Date openBalDate = null;
	Date closeBalDate = null;
//	HashSet<String > location=null;
	Condition obl = null;
	String queryCond = null;
	String filterStr = null;
//	Hashtable <Integer,OpenBalance> openHash = null;
//	class BalanceAccumulator extends BaseAccumulator {

//		public BalanceAccumulator() {
////			super(/*DateUtil.maxDate */Integer.MAX_VALUE);
//			super(/*DateUtil.maxDate */DateUtil.maxDate);
//			// TODO Auto-generated constructor stub
//		}
//
//		@Override
//		public void saveToCache(Comparable p_date, double p_pAmount, double p_nAmount)
//				throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void deleteFromCache(Comparable p_date) throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public DatedValue getCurrentBalance() throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public List<DatedValue> getDatedValues(Comparable p_datefrom, Comparable p_dateto)
//				throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	}
	enum FuncName { FUNC_getBeginBalance, FUNC_getEndBalance,FUNC_getAverageCost,FUNC_getBeginAmount,FUNC_getEndAmount,FUNC_getStmdInQty,FUNC_getStmdOutQty,
					FUNC_getUnitCost,FUNC_getRunningBalance,FUNC_getRunningAmount,FUNC_getRealizedPL,FUNC_dtoixx,FUNC_getRowType,FUNC_getBfValue,FUNC_getCfValue,
					FUNC_getRunningValue,FUNC_getCaPosBalanceEnd,FUNC_getCaPosBalanceChange,FUNC_getCaNegBalanceChange,FUNC_getCaBalanceEnd,FUNC_getRunningPAmount,FUNC_getRunningPBalance,
					FUNC_getCaBuyCostChange,FUNC_getCaSellCostChange,FUNC_getCaCostEnd,FUNC_getAvCostEnd,NOT_DEFINED }
//	Hashtable <Integer,BalanceAccumulator> balHash;
//	Hashtable <Integer,LedgerCostCalculator> costHash = new Hashtable<Integer,LedgerCostCalculator>();
	Hashtable <Object,LedgerCostCalculator> costHash;
	
	class ledgerCellCollection extends Erpv4BaseCellCollection {
		public ledgerCellCollection(BiCellCollection p_parent) {
			super(p_parent,BiResultLedgerG2.this);
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
			case FUNC_getCaNegBalanceChange: {
				int idx = getCurrentCollection().getIdx();
				Object key = makeCumulatorKey(idx);
				if(!isKeyValid(key)) return(0.0);
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					return(ba.getNegBalanceChange(idx));
				} else return(0.0);
			}
			case FUNC_getCaPosBalanceChange: {
				int idx = getCurrentCollection().getIdx();
				Object key = makeCumulatorKey(idx);
				if(!isKeyValid(key)) return(0.0);
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					return(ba.getPosBalanceChange(idx));
				} else return(0.0);
			}
			case FUNC_getCaPosBalanceEnd: {
				int idx = getCurrentCollection().getIdx();
				Object key = makeCumulatorKey(idx);
				if(!isKeyValid(key)) return(0.0);
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					return(ba.getPosBalanceEnd(idx));
				} else return(0.0);
			}
			case FUNC_getCaBalanceEnd: {
				int idx = getCurrentCollection().getIdx();
				Object key = makeCumulatorKey(idx);
				if(!isKeyValid(key)) return(0.0);
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					return(ba.getBalanceEnd(idx));
				} else return(0.0);
			}
			case FUNC_getCaBuyCostChange: {
				int idx = getCurrentCollection().getIdx();
				Object key = makeCumulatorKey(idx);
				if(!isKeyValid(key)) return(0.0);
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					return(ba.getBuyCostChange(idx));
				} else return(0.0);
			}
			case FUNC_getCaSellCostChange: {
				int idx = getCurrentCollection().getIdx();
				Object key = makeCumulatorKey(idx);
				if(!isKeyValid(key)) return(0.0);
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					return(ba.getSellCostChange(idx));
				} else return(0.0);
			}
			case FUNC_getCaCostEnd: {
				int idx = getCurrentCollection().getIdx();
				Object key = makeCumulatorKey(idx);
				if(!isKeyValid(key)) return(0.0);
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					return(ba.getCostEnd(idx));
				} else return(0.0);
			}
			/*
			case FUNC_getAvCostEnd: {
				if(costHash == null) return(0.0);
				Object key = p_args.get(0);
				if(!isKeyValid(key)) return(0.0);
				int idx = getCurrentCollection().getIdx();
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					if(idx < getTableRecCount() - 1) {
							Object nextKey = makeCumulatorKey(idx+1);
							if(nextKey.equals(key)) return(Double.NaN);
					}
					double endBal = ba.getBalanceEnd(idx);
					if(Double.isNaN(endBal)) return(Double.NaN);
					double dd = CostCalculation.getWaCost(getSessionHelper(),irg, org, DateUtil.prevday(openBalDate));
				} else return(0.0);
			}
			*/
			case FUNC_getRunningValue: {
				if(closeBalDate == null)return(Double.NaN);
				if(openBalDate == null)return(Double.NaN);
				Date d = (Date) p_args.get(0);
				Double v = (Double) p_args.get(1);
				if(openBalDate.after(d)) return(Double.NaN);

				if(!closeBalDate.after(d)) {
					if(getCurrentCollection().getSid() <= 0) {
						return(Double.NaN);
					}
				}
				return(v);
			}
			case FUNC_getBfValue: {
				if(closeBalDate == null)return(Double.NaN);
				if(openBalDate == null)return(Double.NaN);
				Date d = (Date) p_args.get(0);
				if(DateUtil.minDate.after(d)) return(Double.NaN);
				Double v = (Double) p_args.get(1);
				if(openBalDate.after(d)) return(v);
				return(Double.NaN);
			}
			case FUNC_getCfValue: {
				if(closeBalDate == null)return(Double.NaN);
				if(openBalDate == null)return(Double.NaN);
				Date d = (Date) p_args.get(0);
				Double v = (Double) p_args.get(1);
				if(closeBalDate.after(d)) return(Double.NaN);
				return(v);
			}
			case FUNC_getRowType: {
				String tranType = (String) p_args.get(0);
				if(tranType.equals("B/F")) return(CellPair.of(0,"B/F"));
				if(tranType.equals("C/F")) return(CellPair.of(9999,"C/F"));
				return(CellPair.of(1,tranType));
			}
			case FUNC_dtoixx: {
				if(p_args.size() != 1) return(null);
				if(! (p_args.get(0) instanceof Double) ) return(null);
				double d = (Double)p_args.get(0);
				return((int) d);
			}
			case FUNC_getRealizedPL: {
				if(costHash == null) return(0.0);
				int irg = (Integer)p_args.get(0);
				if(irg <= 0) return(0.0);
				Date d = (Date) p_args.get(1);
				if(!d.after(DateUtil.minDate)) d = openBalDate;
//				dd = ((Double) p_args.get(2));
//				int org = (int) dd;
				int org = (Integer)p_args.get(2);
				if(org <= 0) return(0.0);
				double pAmount = (Double) p_args.get(3);
				double dd = CostCalculation.getRealizedPL(getSessionHelper(), irg, org, d, pAmount);
				return(dd);
			}
			case FUNC_getBeginBalance: {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
//					int irg = (Integer)p_args.get(0);
					Object key = p_args.get(0);
					if(!isKeyValid(key)) return(0.0);
					int idx = getCurrentCollection().getIdx();
					LedgerCostCalculator ba = costHash.get(key);
					if(ba != null) {
						if(idx > 0 ) {
							Object previouskey = makeCumulatorKey(idx-1);
							if(previouskey.equals(key)) {
								return(Double.NaN);
							}
						}
						return(ba.getBalanceBegin(idx));
					} else return(Double.NaN);
				}
			case FUNC_getRunningBalance: {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
//					int irg = (Integer)p_args.get(0);
//					Object key = p_args.get(0);
					int idx = getCurrentCollection().getIdx();
					Object key = makeCumulatorKey(idx);
					if(!isKeyValid(key)) return(0.0);
					LedgerCostCalculator ba = costHash.get(key);
					if(ba != null) {
						return(ba.getBalanceEnd(idx));
					} else return(0.0);
				}
			case FUNC_getRunningPBalance: {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
//					int irg = (Integer)p_args.get(0);
//					Object key = p_args.get(0);
					int idx = getCurrentCollection().getIdx();
					Object key = makeCumulatorKey(idx);
					if(!isKeyValid(key)) return(0.0);
					LedgerCostCalculator ba = costHash.get(key);
					if(ba != null) {
						return(ba.getPosBalanceEnd(idx));
					} else return(0.0);
				}
			case FUNC_getEndBalance: {
				if(costHash == null) return(0.0);
				/*
				double dd = ((Double) p_args.get(0));
				int irg = (int) dd;
				*/
//				int irg = (Integer)p_args.get(0);
				Object key = p_args.get(0);
				if(!isKeyValid(key)) return(0.0);
				int idx = getCurrentCollection().getIdx();
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					if(idx < getTableRecCount() - 1) {
							Object nextKey = makeCumulatorKey(idx+1);
							if(nextKey.equals(key)) return(Double.NaN);
					}
					return(ba.getBalanceEnd(idx));
				} else return(0.0);
			}

			case FUNC_getUnitCost: {
					return(getUnitCost(p_args));
				}
			case FUNC_getBeginAmount : {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
//					int irg = (Integer)p_args.get(0);
//					if(irg <= 0) return(0.0);
					Object key = p_args.get(0);
					if(!isKeyValid(key)) return(0.0);
					int idx = getCurrentCollection().getIdx();
					LedgerCostCalculator ca = costHash.get(key);
					if(ca != null) {
						if(idx > 0 ) {
							Object previousKey = makeCumulatorKey(idx-1);
							if(previousKey.equals(key)) return(Double.NaN);
						}
						return(ca.getCostBegin(idx));
					} else return(0.0);
				}
			case FUNC_getRunningAmount : {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
//					int irg = (Integer)p_args.get(0);
//					if(irg <= 0) return(0.0);
//					Object key = p_args.get(0);
					
					int idx = getCurrentCollection().getIdx();
					Object key = makeCumulatorKey(idx);
					if(!isKeyValid(key)) return(0.0);
					LedgerCostCalculator ca = costHash.get(key);
					if(ca != null) {
						return(ca.getCostEnd(idx));
								
					} else return(0.0);
				}
			case FUNC_getRunningPAmount : {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
//					int irg = (Integer)p_args.get(0);
//					if(irg <= 0) return(0.0);
//					Object key = p_args.get(0);
					
					int idx = getCurrentCollection().getIdx();
					Object key = makeCumulatorKey(idx);
					if(!isKeyValid(key)) return(0.0);
					LedgerCostCalculator ca = costHash.get(key);
					if(ca != null) {
						return(ca.getBuyCostEnd(idx));
								
					} else return(0.0);
				}
			case FUNC_getEndAmount : {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
//					int irg = (Integer)p_args.get(0);
//					if(irg <= 0) return(0.0);
					Object key = p_args.get(0);
					if(!isKeyValid(key)) return(0.0);
					int idx = getCurrentCollection().getIdx();
					LedgerCostCalculator ca = costHash.get(key);
					if(ca != null) {
						if(idx < getTableRecCount() - 1) {
							Object nextKey = makeCumulatorKey(idx+1);
							if(nextKey.equals(key)) return(Double.NaN);
						}
						return(ca.getCostEnd(idx));
								
					} else return(0.0);
				}
			case FUNC_getStmdInQty : {
				return(getInQty(p_args));
			}
			case FUNC_getStmdOutQty : {
				return(getOutQty(p_args));
			}
			}
			return(super.evalFunction(p_fname,p_args) );
		}
	}
		
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new ledgerCellCollection(p_parent));
	}
	
	void createAccumulators() throws Exception {
		/* use irg as key */
		/* use date as the comparable */
		/* assume dataset in key , date order */
		int n;
		n = getTableRecCount();
		/*
		UniLog.log("2023/04/21 createAccumulator profiling a");
		invalidateLoadCache();
		for(int i=0;i<n;i++) {
			fadeloadOneRec(i,getDefaultRowCollection(),false);
		}
		UniLog.log("2023/04/21 createAccumulator profiling b");
		UniLog.log("2023/04/21 createAccumulator profiling c");
		*/
		invalidateLoadCache();
		for(int i=0;i<n;i++) {
			loadOneRec(i,getDefaultRowCollection(),false);
//			Object key = getCell(getCumulatorKey().getLabel()).getObject();
			Object key = makeCumulatorKey(getCurrentCollection().getIdx());
			if(key != null) {
//				BalanceAccumulator ba = balHash.get(irg);
//				if(ba == null) {
//					ba = new BalanceAccumulator();
//					ba.reset();
//					balHash.put(irg, ba);
//				}
				LedgerCostCalculator ca = costHash.get(key);
				if(ca == null) {
//					ca = new LedgerCostCalculator(DateUtil.maxDate,irg);
					ca = new LedgerCostCalculator(Integer.MAX_VALUE,key);
					costHash.put(key, ca);
					ca.reset();
				}

				try {
					int idx = getCurrentCollection().getIdx();
					setRunningBalance(idx,ca);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
	}
	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag)
	{
		Condition cond = getCustomCondition();
		obl = null;
		if(cond != null) {
			try {
				List<Condition> l1 = Condition.serializeCondition(false, cond);
				for(Condition cd : l1) {
					if(cd.get_isPredicate()) {
						String s= cd.get_leftExpression().toString();
						if(s.equals(cumulatorColumn)) {
						} else {
							if(obl != null) {
								obl = new Condition(obl,Condition.LOGIC_OP_AND,cd);
							} else {
								obl = cd;
							}
						}
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		if(obl != null) {
			queryCond = BiCellCollection.translateCond(this.getView(), obl.toString(),this);
		} else queryCond = null;
		beforeQuery();
		return(super.query(p_rollback, p_sortFlag));
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		costHash = new Hashtable<Object,LedgerCostCalculator>();
		if(openBalDate.before(balBeginDate)){
				UniLog.log("Warning !!!! should not reach here");
				return(new ReturnMsg(false,"Invalid Start Date"));
		} else {
			Date d0 = DateUtil.nextday(balBeginDate);
		}
//		if(true) return(ReturnMsg.defaultOk);
//		Hashtable<BiColumn,Integer>detailColumnList = new Hashtable<BiColumn,Integer>();
		Hashtable<Integer,Object>detailColumnList = new Hashtable<Integer,Object>();
		Hashtable<Integer,Object>valueColumnList = new Hashtable<Integer,Object>();
		int cumulatorPosition = getSelectFieldPosition( getColumnByLabel(cumulatorColumn));
		int rowtypePosition = (rowtypeColumn != null ? getSelectFieldPosition( getColumnByLabel(rowtypeColumn)) : -1);
		for(BiColumn bl : selectFieldList) {
			if(bl.getField() != null) {
				if(!summaryTables.contains(bl.getField().getTable().getName()) ) {
					if(bl.getLabel().equals(cumulatorColumn)) {
//						detailColumnList.put(getSelectFieldPosition( bl ), bl.getField().getEmptyObject());
					} else if(bl.getLabel().equals(rowtypeColumn)) {
					} else {
						detailColumnList.put(getSelectFieldPosition( bl ), bl.getField().getEmptyObject());
					}
				}
			} else {
				if(bl.isStoredFunction()) {
				// should also check isGroupBy and isGroupByField for completeness. Currenly, not handled	
				// should also check the cell type , curreny assument doublw
						valueColumnList.put(getSelectFieldPosition( bl ),0.0);
				}
			}
		}
		try {
			Comparable lastCumulator = null;
			Comparable lastKey = null;
			for(int i=resultTr.getRecordCount()-1;i>=0;i--) {
				Comparable key = (Comparable) makeCumulatorKey(i);
				Comparable cumulator = (Comparable) getResultTrObject(false,cumulatorPosition,i);
				if(cumulator.compareTo(openBalDate) < 0) {
					LedgerCostCalculator ca = costHash.get(key);
					if(ca == null) {
						ca = new LedgerCostCalculator(Integer.MAX_VALUE,key);
						costHash.put(key, ca);
						ca.reset();
					}
					try {
						setOpeningBalance(i,ca);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				if(lastKey == null || !lastKey.equals(key)) {
					if(lastCumulator != null && lastCumulator.compareTo(openBalDate) < 0) {
						saveOneObjectToResultTr(i+1,cumulatorPosition,DateUtil.prevday(openBalDate));
						if(rowtypePosition >= 0) saveOneObjectToResultTr(i+1,rowtypePosition,"B/F");
						for(int fp : detailColumnList.keySet()) {
							saveOneObjectToResultTr(i+1,fp,detailColumnList.get(fp));
						}
					}
					lastCumulator = cumulator;
					lastKey = key;
					if(lastCumulator.compareTo(closeBalDate) < 0) {
							Object[] orgrec = getTrRecord(i);
							addTrRecord(orgrec,i+1);
							saveOneObjectToResultTr(i+1,cumulatorPosition,closeBalDate);
							if(rowtypePosition >= 0) saveOneObjectToResultTr(i+1,rowtypePosition,"C/F");
							for(int fp : detailColumnList.keySet()) {
								saveOneObjectToResultTr(i+1,fp,detailColumnList.get(fp));
							}
						for(int fp : valueColumnList.keySet()) {
							saveOneObjectToResultTr(i+1,fp,valueColumnList.get(fp));
						}
					}
				} else {
					if(lastCumulator != null && lastCumulator.compareTo(openBalDate) < 0) {
						for(int fp : valueColumnList.keySet()) {
							Object oo = getResultTrObject(false,fp,i+1);
							if(oo instanceof Double) {
								Double d0 = (Double) getResultTrObject(false,fp,i);
								d0 += (Double) oo;
								saveOneObjectToResultTr(i,fp,d0);
							}
							if(oo instanceof Integer ) {
								Integer d0 = (Integer) getResultTrObject(false,fp,i);
								d0 += (Integer) oo;
								saveOneObjectToResultTr(i,fp,d0);
							}
						}
						resultTr.deleteRecord(i+1);
					}
					lastCumulator = cumulator;
				}
			}
			if(lastCumulator != null && lastCumulator.compareTo(openBalDate) < 0) {
				saveOneObjectToResultTr(0,cumulatorPosition,DateUtil.prevday(openBalDate));
				if(rowtypePosition >= 0) saveOneObjectToResultTr(0,rowtypePosition,"B/F");
				for(int fp : detailColumnList.keySet()) {
					saveOneObjectToResultTr(0,fp,detailColumnList.get(fp));
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}
	
	abstract protected double getUnitCost(Vector args) throws Exception;
	abstract protected double getInQty(Vector args) throws Exception;
	abstract protected double getOutQty(Vector args) throws Exception;
	abstract protected void setOpeningBalance(int idx,LedgerCostCalculator ca) throws Exception;
	abstract protected void setRunningBalance(int idx,LedgerCostCalculator ca) throws Exception;
	@Override
	protected ReturnMsg afterLoadSerialMap2() {
		try {
			UniLog.log("2023/04/21 before createAccumulator");
			createAccumulators();
			UniLog.log("2023/04/21 after createAccumulator");
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		invalidateLoadCache();
		return(ReturnMsg.defaultOk);
	}
	
	public Date getRptStartDate() {
		return(openBalDate);
	}
	public Date getRptEndDate() {
		return(closeBalDate);
	}
	/*
	public HashSet<String> getLocation() {
		return(location);
	}
	public void setLocation(HashSet<String> p_location) {
		location = p_location;
	}
	*/
	public String getFilterStr() {
		String s = "";
		if(openBalDate != null && openBalDate.after(DateUtil.zeroDate)) {
			s += "From " + DateUtil.toDateString(openBalDate, "yyyy-mmm-dd");
		}
		if(closeBalDate != null && closeBalDate.after(DateUtil.zeroDate) && DateUtil.maxDate.after(closeBalDate)) {
			s += " To " + DateUtil.toDateString(closeBalDate, "yyyy-mmm-dd");
		}
		if( queryCond != null) s += " and " + queryCond;
		return(s);
	}
	
	/*
	@Override
	public BiColumn getCumulatorComparable() {
		// TODO Auto-generated method stub
		return(getView().getColumnByLabel("stmd_date"));
	}
	*/
//	abstract protected BiColumn getCumulatorKey();
	abstract protected Object makeCumulatorKey(int p_idx) throws Exception;
	abstract public ColumnCell getValue(ledgerColumns lgf);
	
	void beforeQuery() {
		
	}

	@Override
	public void setCumulatorColumn(String p_columnLabel) {
		// TODO Auto-generated method stub
		cumulatorColumn = p_columnLabel;
	}

	@Override
	public String getCumulatorColumn() {
		// TODO Auto-generated method stub
		return cumulatorColumn;
	}
	
	protected boolean isKeyValid(Object p_key) {
		if(p_key instanceof Integer && ((Integer) p_key).intValue() > 0) return(true);
		if(p_key instanceof String && !((String) p_key).isEmpty() ) return(true);
		if(p_key instanceof Double && ((Double) p_key).doubleValue() > 0.0) return(true);
		return(false);
	}

	public void setRptCol(CellCollection p_rptCol) {
		rptCol = p_rptCol;
	}
	
	public void setLedgerDate(java.util.Date p_openBalDate,java.util.Date p_closeBalDate) {
		openBalDate = p_openBalDate;
		closeBalDate = p_closeBalDate;
	}
}
