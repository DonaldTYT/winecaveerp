package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAccountLedger extends BiResultLedger {
	double cumulatedPandL = 0.0;
	boolean notCarrayPandLaccount = false;
	Date pAndLStartDate;
	public BiResultAccountLedger(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		setCumulatorColumn("jn_xdate");
		activeAccountCondition = " (jn_openbal <> 0 or jn_lopenbal <> 0 or jn_xno > 0) ";
		getOpeningBalanceViewId = "erpv4.GlJnG2AsAt";
	}

	@Override
	protected double getUnitCost(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return(getCellDouble("jn_xrate"));
	}

	@Override
	protected double getInQty(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return(getCellDouble("jn_pamount"));
	}

	@Override
	protected double getOutQty(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return(getCellDouble("jn_namount"));
	}

	@Override
	protected void setOpeningBalance(LedgerCostCalculator ca) throws Exception {
		// TODO Auto-generated method stub
		double amt = bbr.getCellDouble("jn_amount");
		double lamt = bbr.getCellDouble("jn_lamount");
		if(notCarrayPandLaccount) {
			String cat = bbr.getCellString("ca_category");
			if(
					cat.equals("INCOME")||
					cat.equals("COSTING")||
					cat.equals("EXPENSES")
					) {
				Date d = bbr.getCellDate("jn_xdate");
				if(d.before(pAndLStartDate)) {
					cumulatedPandL += lamt;
					return;
				}
			}
		}
		ca.updateBalanceWithCost(-1, amt,0, 1, lamt);
	}

	@Override
	protected void setRunningBalance(int idx, LedgerCostCalculator ca) throws Exception {
		// TODO Auto-generated method stub
		double amt = getCellDouble("jn_amount");
		double lamt = getCellDouble("jn_lamount");
		ca.updateBalanceWithCost(idx, amt,0, 1, lamt);
		ca.recalAverageBuyBeforeSell(idx);
	}

	@Override
	protected BiColumn getCumulatorKey() {
		// TODO Auto-generated method stub
		return(getColumnByLabel("ca_ano"));
	}

	@Override
	public ColumnCell getValue(ledgerColumns lgf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReportTitle() {
		// TODO Auto-generated method stub
		return "Account Ledger Report";
	}
	@Override
	public String getLinkedView(String p_colName,CellCollection p_col) {
		if(p_colName.equals("tr_srcno")) {
			String jn = p_col.getCellString("tr_jcode");
			if(jn.equals("AR")) return("erpv4.SihAr");
			if(jn.equals("AP")) return("erpv4.SihAp");
			if(jn.equals("PR")) return("erpv4.CrhAr");
			if(jn.equals("PP")) return("erpv4.CrhAp");
			if(jn.equals("GJ")) return("erpv4.GlTran");
		}
		return(super.getLinkedView(p_colName,p_col));
	}

	@Override
	public String getLinkedColumn(String p_colName) {
		if(p_colName.equals("tr_srcno")) {
			String jn = getCellString("tr_jcode");
			if(jn.equals("AR")) return("sih_sno");
			if(jn.equals("AP")) return("sih_sno");
			if(jn.equals("PR")) return("crh_voucher");
			if(jn.equals("PP")) return("crh_voucher");
			if(jn.equals("GJ")) return("tr_srcno");
		}
		return(super.getLinkedColumn(p_colName));
	}
	
	@Override
	void beforeQuery() {
		cumulatedPandL = 0.0;
		notCarrayPandLaccount = false;
		if(rptCol.testCell("notCarryPandLaccount") != null && rptCol.getBoolean("notCarryPandLaccount")) {
			notCarrayPandLaccount = true;
			pAndLStartDate = DateUtil.yearStart(openBalDate);
		}
	}

	@Override
	public void setLedgerDate(Date p_openBalDate, Date p_closeBalDate) {
		// TODO Auto-generated method stub
		
	}
}
