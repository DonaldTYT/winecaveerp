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
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.StockOpening;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockLedgerG2 extends BiResultLedgerG2 {
	int irgPos;
	public BiResultStockLedgerG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		setCumulatorColumn("stmd_date");
		activeAccountCondition = " (stmd_openbal <> 0 or stmd_irg > 0) ";
		irgPos = getSelectFieldPosition( p_view.getColumnByLabel("st_irg" ));
		summaryTables = new HashSet<String>();
		summaryTables.add("stock");
		summaryTables.add("mctype");
		rowtypeColumn = "stmdo_desc";
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
	protected Object makeCumulatorKey(int p_idx) throws Exception {
		// TODO Auto-generated method stub
		return(getResultTrObject(false,irgPos,p_idx));
	}
	
	@Override
	protected void setOpeningBalance(int p_idx,LedgerCostCalculator ca) throws Exception {
		int org = resultTr.getFieldInt("stmd_org", p_idx);
		int irg = resultTr.getFieldInt("stmd_irg", p_idx);
		double iQty = resultTr.getFieldDouble("stmd_netqty", p_idx);
		double dd = CostCalculation.getWaCost(getSessionHelper(),irg, org, DateUtil.prevday(openBalDate));
		ca.updateBalanceWithCost(-1, iQty, 0, 1, iQty * dd);
	}
	@Override
	protected void setRunningBalance(int idx,LedgerCostCalculator ca) throws Exception {
		String stmtype = getCell(rowtypeColumn).getString();
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
			int mrg = getCellInt("stmd_mrg");
			if(mrg <= 0) return;
			double netqty = getCellDouble("stmd_netqty");
			double unitCost = getCellDouble("stmd_unitcost");
			double realizedPL = getCellDouble("stmd_realizedpl");
			ca.updateBalanceWithCost(idx, netqty ,0, 1, netqty * unitCost + realizedPL);
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
					/*
					if(d.before(openBalDate)) {
						dd = CostCalculation.getCostOfGoodSold(getSessionHelper(),irg, org, openBalDate);
					}
					*/
					if(Erpv4BaseCellCollection.stkInQty.contains(getCellString("stmd_tdtype"))) {
						dd = getCellDouble("stmd_netqty");
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
					return(dd);
	}	
	@Override
	protected double getInQty(Vector p_args) throws Exception {
		String tdtype = (String) p_args.get(0);
		double qty = ((Double) p_args.get(1));
		if(Erpv4BaseCellCollection.stmdInQty.contains(tdtype)) return(qty); else return(0.0);
	}
	@Override
	protected double getOutQty(Vector p_args) throws Exception {
		String tdtype = (String) p_args.get(0);
		double qty = ((Double) p_args.get(1));
		if(Erpv4BaseCellCollection.stmdOutQty.contains(tdtype)) return(qty); else return(0.0);
	}

	@Override
	void beforeQuery() {
//		addCustomCondition(" stmd_tdtype in ('BI','MI','JI','RI','SO','MO','JO','RO') ");
//		addCustomCondition(" stmd_tdtype in ('BI','MI','JI','RI','SO','MO','JO','RO','KI','KO') ");
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
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		Wherecl wcl1 = new Wherecl();
		wcl1.andUniop("stmov_any.stm_status", "=", "Confirmed");
		wcl1.andUniop("stmovd_any.stmd_date", "<=", closeBalDate);
		if(Erpv4Config.isMultiCompany(sh)) {
			if(!getSessionHelper().hasAccessRight("#multicomp")) {
				String cocode = Erpv4Config.getDefaultCoCode(sh);
				wcl1.appendString(" and " + "stmov_any.stm_cocode = '"+cocode+"' ").stripAnd();
			}
		}
		p_where.andWherecl(wcl1);
		return(ht);
	}	
}
