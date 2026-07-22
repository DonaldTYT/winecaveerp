package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
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
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public abstract class BiResultLedger extends BiResultErpv4 implements BiLedgerReportInterface {
	
//	int defaultOrg;
	int keyFieldPosition;
	String cumulatorColumn;
	String activeAccountCondition;
	String getOpeningBalanceViewId = null;
	protected CellCollection rptCol;
	
	public class LedgerCostCalculator extends CostCalculator {

		Object key;
		int org;
		public LedgerCostCalculator(Comparable p_maxValue,Object p_key) {
			super(p_maxValue,CostCalculation.useNewCosting(sh),CostCalculation.useFifoCosting(sh));
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
		public void setOrg(int p_org) {
			org = p_org;
		}
		public int getOrg() {
			return(org);
		}
	}
	
//	BiResultLocationAsAt bbr = null;
	BiResult bbr = null;
	public BiResultLedger(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		balBeginDate = Erpv4Config.getCostOpeningErpDate(sh);
//		try {
//			defaultOrg = Erpv4Config.getCoWtAvOrg(sh, Erpv4Config.getDefaultCoCode(sh));
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
		keyFieldPosition = getSelectFieldPosition( getCumulatorKey() );
		if(keyFieldPosition < 0) throw new CellException("Error : Ledger Key Field Invalid");
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
	
//	Hashtable <Integer,BalanceAccumulator> balHash;
//	Hashtable <Integer,LedgerCostCalculator> costHash = new Hashtable<Integer,LedgerCostCalculator>();
	Hashtable <Object,LedgerCostCalculator> costHash;
	
	enum FuncName { FUNC_getBeginBalance, FUNC_getEndBalance,FUNC_getBeginAmount,FUNC_getEndAmount,FUNC_getStmdInQty,FUNC_getStmdOutQty,
					FUNC_getUnitCost,FUNC_getRunningBalance,FUNC_getRunningAmount,FUNC_getRealizedPL,
					FUNC_getAvEndAmount,NOT_DEFINED }
	class ledgerCellCollection extends Erpv4BaseCellCollection {

		public ledgerCellCollection(BiCellCollection p_parent) {
			super(p_parent,BiResultLedger.this);
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
//							int previousIrg = (Integer) getResultTrObject(getCumulatorKey(),keyFieldPosition,idx-1);
							Object previousIrg = getResultTrObject(false,keyFieldPosition,idx-1);
							if(previousIrg.equals(key)) {
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
					Object key = p_args.get(0);
					if(!isKeyValid(key)) return(0.0);
					int idx = getCurrentCollection().getIdx();
					LedgerCostCalculator ba = costHash.get(key);
					if(ba != null) {
						return(ba.getBalanceEnd(idx));
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
							Object nextIrg = getResultTrObject(false,keyFieldPosition,idx+1);
							if(nextIrg.equals(key)) return(Double.NaN);
					}
					return(ba.getBalanceEnd(idx));
				} else return(0.0);
			}
			case FUNC_getAvEndAmount: {
				if(costHash == null) return(0.0);
				Object key = p_args.get(0);
				if(!isKeyValid(key)) return(0.0);
				int idx = getCurrentCollection().getIdx();
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					if(idx < getTableRecCount() - 1) {
							Object nextIrg = getResultTrObject(false,keyFieldPosition,idx+1);
							if(nextIrg.equals(key)) return(Double.NaN);
					}
					double endBal = ba.getBalanceEnd(idx);
					if(Double.isNaN(endBal)) return(Double.NaN);
					int irg = (Integer) (p_args.get(0));
					int org = ba.getOrg();
					if(org == 0) {
						// no opening balance, use stmd_org hot fix, should use other method to get org later
						// 2024-07-19
						org = getCurrentCollection().getCellInt("stmd_org");
					}
					double dd = CostCalculation.getWaCost(getSessionHelper(),irg, org, closeBalDate);
					if(Double.isNaN(dd)) return(Double.NaN);
					return(dd * endBal);
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
							Object previousIrg = getResultTrObject(false,keyFieldPosition,idx-1);
							if(previousIrg.equals(key)) return(Double.NaN);
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
					Object key = p_args.get(0);
					if(!isKeyValid(key)) return(0.0);
					int idx = getCurrentCollection().getIdx();
					LedgerCostCalculator ca = costHash.get(key);
					if(ca != null) {
						return(ca.getCostEnd(idx));
								
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
							Object nextIrg = getResultTrObject(false,keyFieldPosition,idx+1);
							if(nextIrg.equals(key)) return(Double.NaN);
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
		invalidateLoadCache();
		for(int i=0;i<n;i++) {
			loadOneRec(i,getDefaultRowCollection(),false);
			Object key = getCell(getCumulatorKey().getLabel()).getObject();
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
		openBalDate = DateUtil.zeroDate;
		closeBalDate = DateUtil.maxDate;
		Condition cond = getCustomCondition();
		obl = null;
		if(cond != null) {
			try {
				List<Condition> l1 = Condition.serializeCondition(false, cond);
				for(Condition cd : l1) {
					if(cd.get_isPredicate()) {
						String s= cd.get_leftExpression().toString();
						if(s.equals(cumulatorColumn)) {
							Date md;
							switch(cd.get_operator()) {
							case Condition.COMPARE_OP_EQ:
							case Condition.COMPARE_OP_GE:
								md = cd.get_rightExpression().eval(null).getDate();
								if(md.after(openBalDate)) {
									openBalDate = md;
								}
								break;
							case Condition.COMPARE_OP_BETWEEN:
								md = cd.get_rightExpression1().eval(null).getDate();
								if(md.after(openBalDate)) {
									openBalDate = md;
								}
								md = cd.get_rightExpression2().eval(null).getDate();
								if(md.before(closeBalDate)) {
									closeBalDate = md;
								}
								break;
							case Condition.COMPARE_OP_GT:
								md = cd.get_rightExpression().eval(null).getDate();
								md = DateUtil.nextday(md);
								if(md.after(openBalDate)) {
									openBalDate = md;
								}
								break;
							}
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
		if(openBalDate.before(balBeginDate)){
			addCustomCondition(cumulatorColumn+" > '" +
							DateUtil.toDateString(balBeginDate, "yyyy/mm/dd")+ "' '"
					);
			openBalDate = balBeginDate;
		}

		beforeQuery();
//		addCustomCondition(" stmd_tdtype in ('BI','MI','JI','RI','SO','MO','JO','RO') ");
		if(getQueryIncludeNoDetail()) {
//			addCustomCondition(" (stmd_openbal <> 0 or stmd_inqty <> 0 or stmd_outqty <> 0) ");
//			addCustomCondition(" (stmd_openbal <> 0 or stmd_irg > 0) ");
			if(activeAccountCondition != null && !activeAccountCondition.trim().equals("")) {
				if(rptCol.testCell("showAllAccount") == null || !rptCol.getBoolean("showAllAccount")) {
					addCustomCondition(activeAccountCondition);
					
				}
			}
		}
		return(super.query(p_rollback, p_sortFlag));
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		costHash = new Hashtable<Object,LedgerCostCalculator>();
		if(openBalDate.before(balBeginDate)){
				UniLog.log("Warning !!!! should not reach here");
				costHash = new Hashtable<Object,LedgerCostCalculator>();
		} else {
			Date d0 = DateUtil.nextday(balBeginDate);
			UniLog.log("HAHA 211109 line x d0 = " + d0.toString() + " openD " + openBalDate.toString());
			if(openBalDate.after(d0) ) {
				if(bbr == null) {
					if (getOpeningBalanceViewId == null) {
						bbr = getView().getSchema().getViewByName(getView().getName()).newBiResult(getSessionHelper().getLoginId(),null, null, getSessionHelper());
						((BiResultLedger) bbr).setRptCol(rptCol);
					} else {
						bbr = getView().getSchema().getViewByName(getOpeningBalanceViewId).newBiResult(getSessionHelper().getLoginId(),null, null, getSessionHelper());
					}
//					bbr = (BiResultLocationAsAt) getView().getSchema().getViewByName("erpv4.LocationAsAtMulti").newBiResult(getSessionHelper().getLoginId(),null, null, getSessionHelper());
					bbr.setRecLimit(100000);
				}
				bbr.clear();
				bbr.clearCondition();
//				bbr.setLocation(location);
				if(obl != null) bbr.addCustomCondition(obl.toString());
				if(balBeginDate.after(DateUtil.minDate)) {
					bbr.addCustomCondition(cumulatorColumn + " between '" + 
							DateUtil.toDateString(d0, "yyyy/mm/dd")+ "' and '" +
							DateUtil.toDateString(DateUtil.prevday(openBalDate), "yyyy/mm/dd")+ "'");
				} else {
					bbr.addCustomCondition(cumulatorColumn + " <= '" + DateUtil.toDateString(DateUtil.prevday(openBalDate), "yyyy/mm/dd")+ "'");
				}
				bbr.query();
				try {
				for(int i=0;i<bbr.getRowCount();i++) {
					bbr.loadOneRecV(i);
					Object key = bbr.getCell(getCumulatorKey().getLabel()).getObject();
					if(key != null) {
						LedgerCostCalculator ca = costHash.get(key);
						if(ca == null) {
							ca = new LedgerCostCalculator(Integer.MAX_VALUE,key);
							costHash.put(key, ca);
							ca.reset();
						}
						setOpeningBalance(ca);
					}
				}
				} catch (Exception ex) {
					UniLog.log(ex);
					return(new ReturnMsg(false,ex.toString()));
				}
			}
		}
//		try {
//			createAccumulators();
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return(new ReturnMsg(false,ex.toString()));
//		}
		return(ReturnMsg.defaultOk);
	}
	
	abstract protected double getUnitCost(Vector args) throws Exception;
	abstract protected double getInQty(Vector args) throws Exception;
	abstract protected double getOutQty(Vector args) throws Exception;
	abstract protected void setOpeningBalance(LedgerCostCalculator ca) throws Exception;
	abstract protected void setRunningBalance(int idx,LedgerCostCalculator ca) throws Exception;
	@Override
	protected ReturnMsg afterLoadSerialMap2() {
		try {
			createAccumulators();
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
	abstract protected BiColumn getCumulatorKey();
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
}
