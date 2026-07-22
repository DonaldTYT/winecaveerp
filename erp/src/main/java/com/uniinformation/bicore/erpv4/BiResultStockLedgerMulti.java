package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.accumulator.BaseAccumulator;
import com.uniinformation.accumulator.CalculationErrorException;
import com.uniinformation.accumulator.CostCalculator;
import com.uniinformation.accumulator.BaseAccumulator.DatedValue;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.BiLedgerReportInterface.ledgerColumns;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.StockOpening;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockLedgerMulti extends BiResultLedger {
	public BiResultStockLedgerMulti(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		setCumulatorColumn("stmd_date");
		activeAccountCondition = " (stmd_openbal <> 0 or stmd_irg > 0) ";
		getOpeningBalanceViewId = "erpv4.StockLedgerOpening";
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

	@Override
	public String getColumnDisplayString(ColumnCell p_cell) {
		if(p_cell.getCellLabel().equals("stmd_avcost")) {
			return(String.format("%.4f", p_cell.getDouble()));
		}
		if(p_cell.getCellLabel().equals("stmd_unitcost")) {
			return(String.format("%.4f", p_cell.getDouble()));
		}
		return(super.getColumnDisplayString(p_cell));
	}

	@Override
	protected BiColumn getCumulatorKey() {
		// TODO Auto-generated method stub
		return(getView().getColumnByLabel("st_irg"));
	}
	
	@Override
	protected void setOpeningBalance(LedgerCostCalculator ca) throws Exception {
		double iQty = bbr.getCellDouble("stmd_qty");
		double iCost = bbr.getCellDouble("stmd_amount");
		ca.updateBalanceWithCost(-1, iQty, 0, 1,iCost);
		ca.setOrg(bbr.getCellInt("stmd_org"));
	}
	@Override
	protected void setRunningBalance(int idx,LedgerCostCalculator ca) throws Exception {
		String stmtype = getCell("stmd_tdtype").getString();
		if(!stmtype.equals("")) {
			/*
			getCell("stmd_unitcost").eval();
			double iQty = getCell("stmd_inqty").getDouble();
			double oQty = getCell("stmd_outqty").getDouble();
			double iCost = 0.0;
			double oCost = getCell("stmd_unitcost").getDouble() * oQty;
			if(Erpv4BaseCellCollection.stkInQty.contains(getCellString("stmd_tdtype"))) {
				iCost = getCell("stmd_exprice1").getDouble();
			}
			ca.updateBalanceWithCost(idx, iQty, 0, 1,iCost);
			ca.updateBalanceWithCost(idx, -oQty,0, 1,-oCost);
			*/
			/*
			double direction = getCellDouble("stmd_direction");
			double qty = getCellDouble("stmd_qty");
			double unitCost = getCellDouble("stmd_unitcost");
			double realizedPL = getCellDouble("stmd_realizedpl");
			ca.updateBalanceWithCost(idx, qty * direction ,0, 1, qty * direction * unitCost + realizedPL);
			*/
			double qty = getCellDouble("stmd_qty");
			qty *= getCellDouble("stmd_direction");
			double cost = getCellDouble("stmd_inamount");
			cost += getCellDouble("stmd_outamount");
//			cost += getCellDouble("stmd_realizedpl"); realized cost already included in out amount, no need to add it at this point
			ca.updateBalanceWithCost(idx, qty ,0, 1, cost);
		}
	}
	
	@Override
	protected double getUnitCost(Vector p_args) throws Exception {
					if(costHash == null) return(0.0);
					/*
					double dd = ((Double) p_args.get(0));
					int irg = (int) dd;
					*/
					int irg = (Integer)p_args.get(0);
					if(irg <= 0) return(0.0);
					Date d = (Date) p_args.get(1);
					if(!d.after(DateUtil.minDate)) d = openBalDate;
//					dd = ((Double) p_args.get(2));
//					int org = (int) dd;
					int org = (Integer)p_args.get(2);
					if(org <= 0) return(0.0);
					double dd;
//					if(getCellString("stmd_tdtype").equals("KO")) {
//						int cc;
//						cc = 0;
//					}
					if(getCellString("stmd_tdtype").equals("KO")) {
						dd = CostCalculation.getWaCost(getSessionHelper(), irg, org, d);
					} else if(Erpv4BaseCellCollection.stkInQty.contains(getCellString("stmd_tdtype"))) {
						dd = getCellDouble("stmd_qty");
						if(dd !=  0) {
							dd = getCellDouble("stmd_exprice1") / dd;
						}
					} else {
//						dd = CostCalculation.getWaCost(getSessionHelper(),irg, org, d);
						dd = CostCalculation.getCostOfGoodSold(getSessionHelper(),irg, org, d);
//						if(Double.isNaN(dd)) {
//							dd = CostCalculation.getLastWaCost(getSessionHelper(),irg, org);
//						}
					}
					if(getCellString("stmd_tdtype").equals("KI")) {
						if(dd == 0.0 ) {
							dd = CostCalculation.getWaCost(getSessionHelper(), irg, org, d);
						}
					}
					return(dd);
	}	
	@Override
	protected double getInQty(Vector p_args) throws Exception {
		String tdtype = (String) p_args.get(0);
		double qty = ((Double) p_args.get(1));
		if(p_args.size() > 2) {
			String module = (String) p_args.get(2);
			if("sttfr".equals(module)) {
				int tfrMode = Cell.objectToInt(p_args.get(3));
				/*
				if("KI".equals(tdtype)) {
					if(tfrMode == 1) return(0.0);
				}
				*/
				if("KO".equals(tdtype)) {
					if(tfrMode == 1) return(qty);
				}
			}
		}
		if(Erpv4BaseCellCollection.stmdInQty.contains(tdtype)) return(qty); else return(0.0);
	}
	@Override
	protected double getOutQty(Vector p_args) throws Exception {
		String tdtype = (String) p_args.get(0);
		double qty = ((Double) p_args.get(1));
		if(p_args.size() > 2) {
			String module = (String) p_args.get(2);
			if("sttfr".equals(module)) {
				int tfrMode = Cell.objectToInt(p_args.get(3));
				if("KO".equals(tdtype)) {
					if(tfrMode == 1) return(0.0);
				}
				/*
				if("KI".equals(tdtype)) {
					if(tfrMode == 1) return(qty);
				}
				*/
			}
		}
		if(Erpv4BaseCellCollection.stmdOutQty.contains(tdtype)) return(qty); else return(0.0);
	}
	@Override
	void beforeQuery() {
//		addCustomCondition(" stmd_tdtype in ('BI','MI','JI','RI','SO','MO','JO','RO') ");
		addCustomCondition(" stmd_tdtype in ('BI','MI','JI','RI','SO','MO','JO','RO','KI','KO') ");
	}
	@Override
	public String getReportTitle() {
		// TODO Auto-generated method stub
		return "Stock Ledger Report";
	}
	@Override
    protected String brEvalFunction(String p_functName,List p_args) {
    	if(p_functName.equals("brGetStmdModule")) {
    		return("stmov_any.stm_module");
    	}
		return(super.brEvalFunction(p_functName, p_args));
    }
	@Override
	public void setLedgerDate(Date p_openBalDate, Date p_closeBalDate) {
		// TODO Auto-generated method stub
		
	}
}
