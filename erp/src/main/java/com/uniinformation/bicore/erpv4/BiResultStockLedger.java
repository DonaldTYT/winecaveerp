package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.uniinformation.accumulator.BaseAccumulator;
import com.uniinformation.accumulator.CalculationErrorException;
import com.uniinformation.accumulator.CostCalculator;
import com.uniinformation.accumulator.BaseAccumulator.DatedValue;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.StockOpening;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockLedger extends BiResultErpv4 implements BiReportInterface {
	int defaultOrg;
//	protected int rptGroupBy;
	boolean alwaysUseCostCalculation=false;
	public class StockCostCalculator extends CostCalculator {

		int irg;
		public StockCostCalculator(Comparable p_maxValue,int p_irg) {
			super(p_maxValue,CostCalculation.useNewCosting(sh),CostCalculation.useFifoCosting(sh));
			irg = p_irg;
			// TODO Auto-generated constructor stub
		}
		@Override
		public double getAverageCost(Comparable p_date) throws CalculationErrorException {

			try {
				double  avc = super.getAverageCost(p_date);
				return(avc);
			} catch (CalculationErrorException ex) {
				UniLog.log("cost error for irg " + irg + " on " + p_date);
				throw(ex);
			}
		}
		
	}
	
	BiResultStockLedger bbr = null;
	public BiResultStockLedger(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		balBeginDate = Erpv4Config.getCostOpeningErpDate(sh);
		try {
			defaultOrg = Erpv4Config.getCoWtAvOrg(sh, BiConfig.getDefaultCoCode(sh));
			String ss = BiConfig.getString(sh, "alwaysUseCostCalculation");
			if(ss != null && ss.equals("Y")) alwaysUseCostCalculation = true;
					
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		resetViewList();		
				
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
	HashSet<String > location=null;
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
//	Hashtable <Integer,StockCostCalculator> costHash = new Hashtable<Integer,StockCostCalculator>();
	Hashtable <Integer,StockCostCalculator> costHash;
	
	enum FuncName { FUNC_getBeginBalance, FUNC_getEndBalance,FUNC_getAverageCost,FUNC_getBeginAmount,FUNC_getEndAmount,FUNC_getStmdInQty,FUNC_getStmdOutQty,
					NOT_DEFINED }
	class StockLedgerCellCollection extends Erpv4BaseCellCollection {

		public StockLedgerCellCollection(BiCellCollection p_parent) {
			super(p_parent,BiResultStockLedger.this);
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
			case FUNC_getBeginBalance: {
					if(costHash == null) return(0.0);
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					Date d = (Date) p_args.get(1);
					StockCostCalculator ba = costHash.get(irg);
					if(!d.after(DateUtil.minDate)) d = openBalDate;
					if(ba != null) {
						return(ba.getBalanceBegin(d));
					} else return(0.0);
				}
			case FUNC_getEndBalance: {
					if(costHash == null) return(0.0);
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					Date d = (Date) p_args.get(1);
					if(!d.after(DateUtil.minDate)) d = openBalDate;
					StockCostCalculator ba = costHash.get(irg);
					if(ba != null) {
						return(ba.getBalanceEnd(d));
					} else return(0.0);
				}
			case FUNC_getAverageCost: {
					if(costHash == null) return(0.0);
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					if(irg <= 0) return(0.0);
					Date d = (Date) p_args.get(1);
					if(!d.after(DateUtil.minDate)) d = openBalDate;
					if(location != null || alwaysUseCostCalculation) {
						dd = ((Double) p_args.get(2));
						int org = (int) dd;
						dd = CostCalculation.getWaCost(getSessionHelper(),irg, org, d);
//						if(Double.isNaN(dd)) {
//							dd = CostCalculation.getLastWaCost(br.getSessionHelper(),irg, org);
//						}
						return(dd);
					} else {
						StockCostCalculator ca = costHash.get(irg);
						if(ca != null) {
							double avg = 0;
							avg =ca.getAverageCost(d);
							return(avg);
						} else return(0.0);
					}
				}
			case FUNC_getBeginAmount : {
					if(costHash == null) return(0.0);
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					if(irg <= 0) return(0.0);
					Date d = (Date) p_args.get(1);
					StockCostCalculator ca = costHash.get(irg);
					if(!d.after(DateUtil.minDate)) d = openBalDate;
					if(ca != null) {
						if(location != null || alwaysUseCostCalculation) {
							Date pd = DateUtil.prevday(d);
							Double cd = CostCalculation.getWaCost(getSessionHelper(),irg, defaultOrg, pd);
//							if(Double.isNaN(cd)) {
//								cd = CostCalculation.getLastWaCost(br.getSessionHelper(),irg, defaultOrg);
//							}
							return(ca.getBalanceBegin(d) * cd);
						} else {
							return(ca.getCostBegin(d));
						}
								
					} else return(0.0);
				}
			case FUNC_getEndAmount : {
					if(costHash == null) return(0.0);
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					if(irg <= 0) return(0.0);
					Date d = (Date) p_args.get(1);
					if(!d.after(DateUtil.minDate)) d = openBalDate;
					StockCostCalculator ca = costHash.get(irg);
					if(ca != null) {
						return(ca.getCostEnd(d));
								
					} else return(0.0);
				}
			case FUNC_getStmdInQty : {
				String tdtype = (String) p_args.get(0);
				double qty = ((Double) p_args.get(1));
				if("KI".equals(tdtype)) {
					int cc;
					cc = 0;
				}
				if(location != null) {
					if(stmdInQty.contains(tdtype)) return(qty); else return(0.0);
				} else {
					if(stkInQty.contains(tdtype)) return(qty); else return(0.0);
				}
			}
			case FUNC_getStmdOutQty : {
				String tdtype = (String) p_args.get(0);
				double qty = ((Double) p_args.get(1));
				if(location != null) {
					if(stmdOutQty.contains(tdtype)) return(qty); else return(0.0);
				} else {
					if(stkOutQty.contains(tdtype)) return(qty); else return(0.0);
				}
			}
			}
			
			return(super.evalFunction(p_fname,p_args) );
		}
	}
		
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new StockLedgerCellCollection(p_parent));
	}
	
//	double getOpenBalance(int p_irg,Date p_date) {
//			try {
//				TableRec tr  = getSelectUtil().getQueryResult("select stmd_tdtype,sum(stmd_qty) sumqty from stmovd,stmov where stmd_irg = ? and stm_mrg = stmd_mrg and stm_status = 'Confirmed' and stm_date < ? group by 1",
//						new Wherecl().appendArgument(p_irg).appendArgument(p_date)
//					);
//				double openBal = 0.0;
//				for(int i=0;i<tr.getRecordCount();i++) {
//					tr.setRecPointer(i);
//					UniLog.log(tr.getFieldString("stmd_tdtype") + " = " + tr.getFieldDouble("sumqty"));
//					String tdtype = tr.getFieldString("stmd_tdtype");
//					if(Erpv4BaseCellCollection.stkInQty.contains(tdtype)) {
//						openBal += tr.getFieldDouble("sumqty");
//					} else {
//						openBal -= tr.getFieldDouble("sumqty");
//					}
//				}
//				return(openBal);
//			} catch (Exception ex) {
//				UniLog.log(ex);
//				return(0);
//			}
//	}
	void createAccumulators() throws Exception {
		/* use irg as key */
		/* use date as the comparable */
		/* assume dataset in key , date order */
		int n;
		n = getTableRecCount();
		invalidateLoadCache();
		for(int i=0;i<n;i++) {
			loadOneRec(i,getDefaultRowCollection(),false);
			int irg;
			if(getCell("st_irg") != null) {
				irg = getCellInt("st_irg");
			} else {
				irg = getCellInt("stmd_irg");
			}
			if(irg > 0) {
//				BalanceAccumulator ba = balHash.get(irg);
//				if(ba == null) {
//					ba = new BalanceAccumulator();
//					ba.reset();
//					balHash.put(irg, ba);
//				}
				StockCostCalculator ca = costHash.get(irg);
				if(ca == null) {
					ca = new StockCostCalculator(DateUtil.maxDate,irg);
					costHash.put(irg, ca);
					ca.reset();
					if(balBeginDate.after(DateUtil.minDate)) {
						StockOpening sto = Erpv4Config.getStockOpening(getSelectUtil(), irg, balBeginDate,location != null);
						if(sto != null) { 
							try {
								if(location == null) {
									ca.updateBalanceWithCost(DateUtil.prevday(balBeginDate), sto.balance, 0, 1,sto.unitcost * sto.balance);
								} else {
									double lbal = 0.0;
									double lcost = 0.0;
									for(String ss : location) {
										Double dd = sto.locBalance.get(ss);
										if(dd != null) {
											lbal += dd;
											lcost += sto.unitcost * dd;
										}
									}
									ca.updateBalanceWithCost(DateUtil.prevday(balBeginDate), lbal, 0, 1,lcost);
								}
							} catch (Exception ex ) {
								UniLog.log(ex);
							}
						}
					}
				}
				String stmtype = getCell("stmd_tdtype").getString();
				try {
					Date d = getCell("stmd_date").getDate();
					if(!stmtype.equals("")) {
						double iQty = getCell("stmd_inqty").getDouble();
						double oQty = getCell("stmd_outqty").getDouble();
						double iCost = 0.0;
						if(Erpv4BaseCellCollection.stkInQty.contains(getCellString("stmd_tdtype"))) {
							iCost = getCell("stmd_exprice1").getDouble();
						}
						if(d.after(DateUtil.minDate))  {
//							ba.updateBalance(d,iQty, -oQty, 1);
							ca.updateBalanceWithCost(d, iQty, 0, 1,iCost);
							ca.updateBalanceWithCost(d, 0, -oQty, 1,0);
						}
					}
					if(!d.after(DateUtil.minDate)) d = openBalDate;
					if(d.after(DateUtil.minDate))  {
//						getCell("stmd_openbal").set( ca.getBalanceBegin(d));
						double dd = ca.getBalanceEnd(d);
						getCell("stmd_avcost").eval();
						if(Double.isNaN(dd)) {
							UniLog.log("HAHA dd is NaN");
						}
						getCell("stmd_closebal").set( ca.getBalanceEnd(d));
//						saveOneColumn("stmd_openbal",i);
						saveOneColumn("stmd_closebal",i);
					}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
			for(Integer I : costHash.keySet()) {
				UniLog.log("recalCost for " + I);
				StockCostCalculator cu = costHash.get(I);
				cu.recalAverageBuyBeforeSell(new Date());
			}
			/*
			for(StockCostCalculator cu : costHash.values()) {
			cu.recalAverageBuyBeforeSell(new Date());
			}
			*/
		if(n > 0) {
			try {
				getCell("stmd_openbal").eval();
//				getCell("stmd_closebal").eval();
				getCell("stmd_avcost").eval();
				getCell("stmd_openamt").eval();
				getCell("stmd_closeamt").eval();
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		invalidateLoadCache();
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
						if(s.equals("stmd_date")) {
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
			addCustomCondition(" stmd_date > '" +
							DateUtil.toDateString(balBeginDate, "yyyy/mm/dd")+ "' '"
					);
			openBalDate = balBeginDate;
		}

		if(location != null) {
			String cd = null;
			if(location.size() > 1) {
//				cd = " stmd_loc in (";
//				cd += " )";
				for(String ss : location) {
					if(cd == null) cd = " stmd_loc in ('"; else cd += ",'";
					cd += ss;
					cd += "'";
				}
				cd += " )";
			} else {
				cd = " stmd_loc = '";
				for(String ss : location) {
					cd += ss;
				}
				cd += "' ";
			}
			addCustomCondition(cd);
		} else {
			addCustomCondition(" stmd_tdtype in ('BI','MI','JI','RI','SO','MO','JO','RO') ");
		}
		if(getQueryIncludeNoDetail()) {
			addCustomCondition(" (stmd_openbal <> 0 or stmd_inqty <> 0 or stmd_outqty <> 0) ");
		}
		return(super.query(p_rollback, p_sortFlag));
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		if(openBalDate.before(balBeginDate)){
				UniLog.log("Warning !!!! should not reach here");
				costHash = new Hashtable<Integer,StockCostCalculator>();
		} else {
			Date d0 = DateUtil.nextday(balBeginDate);
			UniLog.log("HAHA 211109 line x d0 = " + d0.toString() + " openD " + openBalDate.toString());
			if(openBalDate.after(d0) ) {
				if(bbr == null) {
					bbr = (BiResultStockLedger) getView().newBiResult(getSessionHelper().getLoginId(),null, null, getSessionHelper());
					bbr.setRecLimit(100000);
				}
				bbr.clear();
				bbr.clearCondition();
				bbr.setLocation(location);
				if(obl != null) bbr.addCustomCondition(obl.toString());
				if(balBeginDate.after(DateUtil.minDate)) {
					bbr.addCustomCondition("stmd_date between '" + 
							DateUtil.toDateString(d0, "yyyy/mm/dd")+ "' and '" +
							DateUtil.toDateString(DateUtil.prevday(openBalDate), "yyyy/mm/dd")+ "'");
				} else {
					bbr.addCustomCondition("stmd_date < '" + DateUtil.toDateString(openBalDate, "yyyy/mm/dd")+ "'");
				}
				bbr.query();
//				balHash = bbr.balHash;
				costHash = bbr.costHash;
			} else {
//				balHash = new Hashtable<Integer,BalanceAccumulator>();
				costHash = new Hashtable<Integer,StockCostCalculator>();
			}
		}
		try {
			createAccumulators();
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}
	
	public Date getRptStartDate() {
		return(openBalDate);
	}
	public Date getRptEndDate() {
		return(closeBalDate);
	}
	public HashSet<String> getLocation() {
		return(location);
	}
	public void setLocation(HashSet<String> p_location) {
		location = p_location;
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		/*
		if(BiConfig.isMultiCompany(sh)) {
		String cocode = BiConfig.getDefaultCoCode(sh);
		if(getCell("stm_cocode") != null) {
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and stm_cocode = '"+cocode+"' ").stripAnd();
			p_where.andWherecl(wcl1);
		}
		}
		*/
		return(ht);
	}
	@Override
	public String getColumnDisplayString(ColumnCell p_cell) {
		if(p_cell.getCellLabel().equals("stmd_avcost")) {
			return(String.format("%.4f", p_cell.getDouble()));
		}
		return(super.getColumnDisplayString(p_cell));
	}
	
	@Override
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
	

	@Override
	public BiColumn getCumulatorKey() {
		// TODO Auto-generated method stub
		return(getView().getColumnByLabel("st_irg"));
	}
	@Override
	public BiColumn getCumulatorComparable() {
		// TODO Auto-generated method stub
		return(getView().getColumnByLabel("stmd_date"));
	}
	@Override
	public ColumnCell getValue(ledgerColumns lgf) {
		// TODO Auto-generated method stub
		switch(lgf) {
		case st_icode: return(getCell("st_icode"));
		case st_iname: return(getCell("st_iname"));
		case lg_date: return(getCell("stmd_date"));
		case stm_ref1: return(getCell("stm_ref1"));
		case stm_ref2: return(getCell("stm_ref2"));
		case stmd_tdtype: return(getCell("stmd_tdtype"));
		case stmd_openbal: return(getCell("stmd_openbal"));
		case stmd_inqty: return(getCell("stmd_inqty"));
		case stmd_outqty: return(getCell("stmd_outqty"));
		case stmd_closebal: return(getCell("stmd_closebal"));
		case stmd_openamt: return(getCell("stmd_openamt"));
		case stmd_inamount: return(getCell("stmd_inamount"));
		case stmd_outamount: return(getCell("stmd_outamount"));
		case stmd_closeamt: return(getCell("stmd_closeamt"));
		case stmd_avcost: return(getCell("stmd_avcost"));
		}
		return null;
	}

	/*
	@Override
	public List<String> getAggregateExpressionList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List< String> getPivotColumnList() {
		// TODO Auto-generated method stub
		return null;
	}
	*/

	/*
	@Override
	public void setGroupBy(int p_idx) {
		// TODO Auto-generated method stub
		rptGroupBy = p_idx;
	}
	*/
	
	public void resetViewList() {
		super.resetViewList();
		BiColumn clLocation = getView().getColumnByLabel("stm_ref4");
		BiColumn clDoctor = getView().getColumnByLabel("cldoc_name");
		if(clLocation != null && clLocation != null) moveViewColumn(clDoctor,clLocation);
	}
	@Override
    protected String brEvalFunction(String p_functName,List p_args) {
    	if(p_functName.equals("brGetStmdModule")) {
    		return("stmov_any.stm_module");
    	}
		return(super.brEvalFunction(p_functName, p_args));
    }
}
