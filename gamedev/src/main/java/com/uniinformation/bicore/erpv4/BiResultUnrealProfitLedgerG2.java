package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellPair;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultUnrealProfitLedgerG2 extends BiResultLedgerG2  {
	int icodePos;
	int sellerCocodePos;
	int buyerCocodePos;
	int stmdCostPos;
	int stmdRevenuePos;
	public BiResultUnrealProfitLedgerG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
		useFIFO = true;
		setCumulatorColumn("stmd_date");
		icodePos = getSelectFieldPosition( p_view.getColumnByLabel("st_icode" ));
		sellerCocodePos = getSelectFieldPosition( p_view.getColumnByLabel("co_cocode" ));
		buyerCocodePos = getSelectFieldPosition( p_view.getColumnByLabel("bco_cocode" ));
		stmdCostPos = getSelectFieldPosition( p_view.getColumnByLabel("stmd_cost" ));
		stmdRevenuePos = getSelectFieldPosition( p_view.getColumnByLabel("stmd_revenue" ));
		summaryTables = new HashSet<String>();
		summaryTables.add("stock");
		summaryTables.add("mctype");
		summaryTables.add("cocode");
		summaryTables.add("buycocode");
//		rowtypeColumn = "stmdo_desc";
	}
	@Override
	public String getReportTitle() {
		// TODO Auto-generated method stub
		return "Profit and Lost Report";
	}

	@Override
	protected double getUnitCost(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Object makeCumulatorKey(int p_idx) throws Exception {
		// TODO Auto-generated method stub
		return(
				(String) getResultTrObject(false,icodePos,p_idx)+
				getResultTrObject(false,sellerCocodePos,p_idx)+
				getResultTrObject(false,buyerCocodePos,p_idx)
				);
	}

	@Override
	protected double getInQty(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double getOutQty(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setOpeningBalance(int p_idx,LedgerCostCalculator ca) throws Exception {
		// TODO Auto-generated method stub
		/*
		double amt = bbr.getCellDouble("stmd_qty");
		double lamt = bbr.getCellDouble("stmd_exprice") - bbr.getCellDouble("stmd_cost");
		*/
		double amt = bbr.getCellDouble("stmd_cost");
		double lamt = bbr.getCellDouble("stmd_exprice");
		ca.updateBalanceWithCost(-1, amt,0, 1, lamt);
	}

	@Override
	protected void setRunningBalance(int p_idx, LedgerCostCalculator ca) throws Exception {
		int irg = getCellInt("stmd_irg");
		int org = getCellInt("stmd_org");
		java.util.Date stmdDate = getCellDate("stmd_date");
		double qty = getCellDouble("stmd_qty");
		double amt = CostCalculation.getWaCost(sh, irg, org, stmdDate);
		amt *= qty;
		saveOneObjectToResultTr(p_idx,stmdCostPos,amt);
		getCell("stmd_cost").set(amt);
		double lamt = getCellDouble("stmd_revenue");
		saveOneObjectToResultTr(p_idx,stmdRevenuePos,lamt);
		getCell("stmd_revenue").set(lamt);
//		double realizedAmt = getRealizedAmount()
		ca.updateBalanceWithCost(p_idx, lamt,0, 1, amt);
		if(stmdDate.equals(closeBalDate)) {
			getUnRealizedRevenue(p_idx,ca);
		}
	}


	void getUnRealizedRevenue(int p_idx , LedgerCostCalculator ca) throws Exception {
//		int org = getCellInt("stmdji_org");
		String buyerCocode = getCellString("bco_cocode");
		int org = Erpv4Config.getCoWtAvOrg(sh, buyerCocode);
		int cumulatorPosition = getSelectFieldPosition( getColumnByLabel(cumulatorColumn));
		if(org == 0) return;
		double realized = 0;
		for(int i=0;i<=p_idx;i++) {
			java.util.Date d = (java.util.Date) getResultTrObject(false,cumulatorPosition,i);
			double uv = CostCalculation.getBalance(sh, getCellInt("st_irg"), org, d);
			double c = CostCalculation.getWaCost(sh, getCellInt("st_irg"), org, d);
			uv *= c; /* uv = unrealized amount as at i  in buyer's ledger */
			double v = ca.getBalanceEnd(i); /* v = unrealized amount as at i in seller's ledger */
			double rv;
			v += realized;
			if(uv < v) rv = v - uv; else rv = 0;
			if(rv > realized) {
				ca.updateBalanceWithCost(i, 0,realized-rv, 1, 0);
				realized = rv;
			}
		}
		ca.recalAverageBuyBeforeSell(closeBalDate);
	}


	@Override
	public ColumnCell getValue(ledgerColumns lgf) {
		// TODO Auto-generated method stub
		return null;
	}


	enum FuncName { FUNC_getUnRealizedRevenue,FUNC_getUnRealizedQty,FUNC_getUnRealizedAmount,FUNC_getUnRealizedCost,FUNC_getRowType,
					NOT_DEFINED }
	class ProfitledgerCellCollection extends ledgerCellCollection {

		public ProfitledgerCellCollection(BiCellCollection p_parent) {
			super(p_parent);
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
			case FUNC_getRowType: {
				String tranType = (String) p_args.get(0);
				if(tranType.equals("B/F")) return(CellPair.of("B/F",0));
				if(tranType.equals("C/F")) return(CellPair.of("C/F",0));
				return(CellPair.of(tranType,1));
			}
			case FUNC_getUnRealizedRevenue : {
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
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double v = CostCalculation.getBalance(sh, getCellInt("stmd_irg"), org, closeBalDate);
					double c = CostCalculation.getWaCost(sh, getCellInt("stmd_irg"), org, closeBalDate);
					return(v * c);
				} else return(0.0);
				}			
			
			case FUNC_getUnRealizedQty : {
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
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double v = CostCalculation.getBalance(sh, getCellInt("stmd_irg"), org, closeBalDate);
					return(v);
				} else return(0.0);
				}
			case FUNC_getUnRealizedCost: {
				if(costHash == null) return(0.0);
				Object key = p_args.get(0);
				if(!isKeyValid(key)) return(0);
				int idx = getCurrentCollection().getIdx();
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					if(idx < getTableRecCount() - 1) {
						Object nextKey = makeCumulatorKey(idx+1);
						if(nextKey.equals(key)) return(Double.NaN);
					}
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double c = CostCalculation.getWaCost(sh, getCellInt("stmd_irg"), org, closeBalDate);
					double v = CostCalculation.getBalance(sh, getCellInt("stmd_irg"), org, closeBalDate);
					v *= c;
					if(v <= 0.0) return(0.0);
					double a = 0;
					for(int i=idx;i>= 0;i--) {
						double sp = ba.getBalanceEnd(i) - ba.getBalanceEnd(i-1);
						double sq = ba.getCostEnd(i) - ba.getCostEnd(i-1);
						if(sq >= v) {
							a += v * sp / sq;
							v = 0;
							break;
						} else {
							a += sp;
							v -= sq;
						}
					}
					if(v > 0) {
						double sq = ba.getBalanceEnd(-1);
						double sp = ba.getCostEnd(-1);
						if(sq >= v) {
							a += v * sp / sq;
							v = 0;
							break;
						} else {
							a += sp;
							v -= sq;
						}
					}
					return(a);
				} else return(0.0);
				}
			case FUNC_getUnRealizedAmount: {
				if(costHash == null) return(0.0);
				Object key = p_args.get(0);
				if(!isKeyValid(key)) return(0);
				int idx = getCurrentCollection().getIdx();
				LedgerCostCalculator ba = costHash.get(key);
				if(ba != null) {
					if(idx < getTableRecCount() - 1) {
						Object nextKey = makeCumulatorKey(idx+1);
						if(nextKey.equals(key)) return(Double.NaN);
					}
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double v = CostCalculation.getBalance(sh, getCellInt("stmd_irg"), org, closeBalDate);
					if(v <= 0.0) return(0.0);
					double a = 0;
					for(int i=idx;i>= 0;i--) {
						double sq = ba.getBalanceEnd(i) - ba.getBalanceEnd(i-1);
						double sp = ba.getCostEnd(i) - ba.getCostEnd(i-1);
						if(sq >= v) {
							a += v * sp / sq;
							v = 0;
							break;
						} else {
							a += sp;
							v -= sq;
						}
					}
					if(v > 0) {
						double sq = ba.getBalanceEnd(-1);
						double sp = ba.getCostEnd(-1);
						if(sq >= v) {
							a += v * sp / sq;
							v = 0;
							break;
						} else {
							a += sp;
							v -= sq;
						}
					}
					return(a);
				} else return(0.0);
				}
			}
			return(super.evalFunction(p_fname,p_args) );
		}
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new ProfitledgerCellCollection(p_parent));
	}
}
