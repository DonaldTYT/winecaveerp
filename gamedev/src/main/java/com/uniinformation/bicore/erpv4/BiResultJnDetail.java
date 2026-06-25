package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.accumulator.BaseAccumulator;
import com.uniinformation.accumulator.CalculationErrorException;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultJnDetail extends BiResultErpv4 {
	class BalanceAccumulator extends BaseAccumulator {

		public BalanceAccumulator() {
			super(/*DateUtil.maxDate */Integer.MAX_VALUE);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void saveToCache(Comparable p_date, double p_pAmount, double p_nAmount)
				throws CalculationErrorException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteFromCache(Comparable p_date) throws CalculationErrorException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public DatedValue getCurrentBalance() throws CalculationErrorException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<DatedValue> getDatedValues(Comparable p_datefrom, Comparable p_dateto)
				throws CalculationErrorException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	BalanceAccumulator accu = null;
	BalanceAccumulator laccu = null;
	public BiResultJnDetail(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl1 = null;
		if(Erpv4Config.isMultiCompany(sh)) {
			BiColumn locCol = getColumnByLabel("jn_cocode");
			if(locCol != null && columnInSelectList(locCol)) {
				String cocode = Erpv4Config.getDefaultCoCode(sh);
				if(wcl1 == null) wcl1 = new Wherecl();
				wcl1.appendString(" and jn_cocode = '"+cocode+"' ").stripAnd();
			}
		}

		BiResultAccountBalance biAcb = (BiResultAccountBalance) getParent();
		if(biAcb.blStart.after(DateUtil.minDate)
		    && biAcb.blStart.after(DateUtil.minDate)) {
			if(wcl1 == null) wcl1 = new Wherecl();
			wcl1.andRange("jn_xdate", biAcb.blStart,biAcb.blEnd);
		} else if(biAcb.blStart.after(DateUtil.minDate)) {
			if(wcl1 == null) wcl1 = new Wherecl();
			wcl1.andUniop("jn_xdate", ">=", biAcb.blStart);
		} else if(biAcb.blEnd.after(DateUtil.minDate)) {
			if(wcl1 == null) wcl1 = new Wherecl();
			wcl1.andUniop("jn_xdate", "<=", biAcb.blEnd);
		}
		if(wcl1 != null) p_where.andWherecl(wcl1);
		return(ht);
	}
	public void reloadBalance() {
		try {
				BiResult sr = this;	
						if(true) {
							if(accu == null) accu = new BalanceAccumulator();
							if(laccu == null) laccu = new BalanceAccumulator();
							accu.reset();
							laccu.reset();
							Vector<BiCellCollection> sl = sr.getRowCollectionList();
//							for(CellCollection col:sl) {
							int n = sl.size();
							try {
							for(int i=0;i<n;i++) {
								BiCellCollection col = sl.get(i);
								double pamt = col.getDouble("jn_pamount");
								double namt = col.getDouble("jn_namount");
								double lpamt = col.getDouble("jn_lpamount");
								double lnamt = col.getDouble("jn_lnamount");
								double bal = col.getDouble("bls_begbal");
								double lbal = col.getDouble("bls_lbegbal");
								if(i == 0) {
									if(bal > 0) {
										pamt += bal;
									} else {
										namt -= bal;
									}
									if(lbal > 0) {
										lpamt += lbal;
									} else {
										lnamt -= lbal;
									}
								}
								accu.updateBalance(i, pamt,namt, 1,true);
								laccu.updateBalance(i, lpamt,lnamt, 1,true);
							}
							} catch (CalculationErrorException cex ){
								UniLog.log(cex);
							}
//							double balance = accu.getBalanceEnd(BaseAccumulator.accMaxDate);
//							double freestock = getCell("stg_freestock").getDouble();
//							double reserved = getCell("stg_reserved").getDouble();
							String cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
							int org = Erpv4Config.getCoWtAvOrg(getSessionHelper(), cocode);
							try {
							for(int i=0;i<n;i++) {
								CellCollection col = sl.get(i);
								col.getCell("jn_bal").set(accu.getBalanceEnd(i));
								col.getCell("jn_lbal").set(laccu.getBalanceEnd(i));
							}
							} catch (CalculationErrorException cex ){
								UniLog.log(cex);
							}
//							accu.dump();
						}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		
	}
	

	@Override
	public String getLinkedView(String p_colName,CellCollection p_col) {
		if(p_colName.equals("tr_srcno")) {
			String jn = p_col.getCellString("tr_jcode");
			if(jn.equals("AR")) return("erpv4.SihAr");
			if(jn.equals("AP")) return("erpv4.SihAp");
			if(jn.equals("PR")) return("erpv4.CrhAr");
			if(jn.equals("PP")) return("erpv4.CrhAr");
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
		}
		return(super.getLinkedColumn(p_colName));
	}
}
