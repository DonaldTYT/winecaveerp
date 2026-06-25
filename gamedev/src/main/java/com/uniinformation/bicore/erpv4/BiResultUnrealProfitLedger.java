package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultLedger.ledgerCellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultUnrealProfitLedger extends BiResultLedger  {

	public BiResultUnrealProfitLedger(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
		setCumulatorColumn("stmdji_date");
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
	protected void setOpeningBalance(LedgerCostCalculator ca) throws Exception {
		// TODO Auto-generated method stub
		/*
		double amt = bbr.getCellDouble("stmd_qty");
		double lamt = bbr.getCellDouble("stmd_exprice") - bbr.getCellDouble("stmd_cost");
		*/
		double amt = bbr.getCellDouble("stmdji_cost");
		double lamt = bbr.getCellDouble("stmdji_exprice");
		ca.updateBalanceWithCost(-1, amt,0, 1, lamt);
	}

	@Override
	protected void setRunningBalance(int idx, LedgerCostCalculator ca) throws Exception {
		// TODO Auto-generated method stub
		/*
		double amt = getCellDouble("stmd_qty");
		double lamt = getCellDouble("stmd_exprice") - getCellDouble("stmd_cost");
		*/
		double amt = getCellDouble("stmdji_cost");
		double lamt = getCellDouble("stmdji_exprice");
		ca.updateBalanceWithCost(idx, amt,0, 1, lamt);
	}

	@Override
	protected BiColumn getCumulatorKey() {
		// TODO Auto-generated method stub
		return(getColumnByLabel("urpl_key"));
	}

	@Override
	public ColumnCell getValue(ledgerColumns lgf) {
		// TODO Auto-generated method stub
		return null;
	}


	enum FuncName { FUNC_getUnRealizedRevenue,FUNC_getUnRealizedQty,FUNC_getUnRealizedAmount,FUNC_getUnRealizedCost,
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
			case FUNC_getUnRealizedRevenue : {
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
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double v = CostCalculation.getBalance(sh, getCellInt("urpl_irg"), org, closeBalDate);
					double c = CostCalculation.getWaCost(sh, getCellInt("urpl_irg"), org, closeBalDate);
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
							Object nextIrg = getResultTrObject(false,keyFieldPosition,idx+1);
							if(nextIrg.equals(key)) return(Double.NaN);
					}
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double v = CostCalculation.getBalance(sh, getCellInt("urpl_irg"), org, closeBalDate);
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
							Object nextIrg = getResultTrObject(false,keyFieldPosition,idx+1);
							if(nextIrg.equals(key)) return(Double.NaN);
					}
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double c = CostCalculation.getWaCost(sh, getCellInt("urpl_irg"), org, closeBalDate);
					double v = CostCalculation.getBalance(sh, getCellInt("urpl_irg"), org, closeBalDate);
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
							Object nextIrg = getResultTrObject(false,keyFieldPosition,idx+1);
							if(nextIrg.equals(key)) return(Double.NaN);
					}
					int org = getCellInt("stmdji_org");
					if(org == 0) return(0.0);
					double v = CostCalculation.getBalance(sh, getCellInt("urpl_irg"), org, closeBalDate);
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

	@Override
	public void setLedgerDate(Date p_openBalDate, Date p_closeBalDate) {
		// TODO Auto-generated method stub
		
	}
}
