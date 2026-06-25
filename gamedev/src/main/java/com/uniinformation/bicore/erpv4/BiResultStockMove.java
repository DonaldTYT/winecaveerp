package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
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
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.StockOpening;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockMove extends BiResultErpv4 {

	boolean excludeTransit = true;
	int locgroup = -1;
	public BiResultStockMove(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl1 = null;
		if(Erpv4Config.isMultiCompany(sh)) {
			BiColumn locCol = getColumnByLabel("loc_cocode");
			if(locCol != null && columnInSelectList(locCol)) {
				String cocode = Erpv4Config.getDefaultCoCode(sh);
				if(wcl1 == null) wcl1 = new Wherecl();
				wcl1.appendString(" and loc_cocode = '"+cocode+"' ").stripAnd();
				if(ht == null) ht = new HashSet<BiTable>();
				ht.add(getView().getSchema().getTable("locationcode"));
				if(Erpv4Config.isMultiStockLoc(sh)) {
					wcl1.appendString(" and loc_mrg = " + Erpv4Config.getDefaultLcrg(sh));
					if(excludeTransit) {
						wcl1.appendString(" and loc_transit <> 'Y'");
					}
					ht.add(getView().getSchema().getTable("locationcode"));
				}
			}
		}
		Date csd = Erpv4Config.getCostOpeningErpDate(sh);
		if(csd.after(DateUtil.minDate)) {
			if(wcl1 == null) wcl1 = new Wherecl();
			wcl1.andUniop("stmovd_any.stmd_date", ">", csd);
		}
		if(locgroup >= 0) {
			if(wcl1 == null) wcl1 = new Wherecl();
			wcl1.appendString("and loc_group = "+locgroup+ " ").stripAnd();
			if(ht == null) ht = new HashSet<BiTable>();
			ht.add(getView().getSchema().getTable("locationcode"));
		}
		String s = Erpv4Config.getStockMoveTypes(sh); {
			if(wcl1 == null) wcl1 = new Wherecl();
			wcl1.appendString("and stmovd_any.stmd_tdtype in ("+s+ ")").stripAnd();
		}
		if(wcl1 != null) p_where.andWherecl(wcl1);
		return(ht);
	}
	public void setLocGroup(int p_group) {
		locgroup = p_group;
	}
	class BalanceAccumulator extends BaseAccumulator {

		public BalanceAccumulator() {
			super(/*DateUtil.maxDate */Integer.MAX_VALUE);
//			super(/*DateUtil.maxDate */DateUtil.maxDate);
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
	
	Hashtable <String,BalanceAccumulator> accuhash = new Hashtable<String,BalanceAccumulator>();
	public void reloadStockMove() {
		try {
				BiResult sr = this;	
						StockOpening sto = null;
						Date csd = Erpv4Config.getCostOpeningErpDate(sh);
						if(csd.after(DateUtil.zeroDate) ) {
							if(locgroup < 0) 
								sto = Erpv4Config.getStockOpening(getSelectUtil(), getParent().getCellInt("st_irg"), csd,false);
							else
								sto = Erpv4Config.getLocgroupOpening(getSelectUtil(), getParent().getCellInt("st_irg"), csd,locgroup);
						}
						if(sr.getColumnByLabel("stmd_balance") != null) {
							BalanceAccumulator accu = accuhash.get(sr.getView().getName());
							if(accu == null) {
								accu = new BalanceAccumulator();
								accuhash.put(sr.getView().getName(), accu);
							}
							accu.reset();
							Vector<BiCellCollection> sl = sr.getRowCollectionList();
//							for(CellCollection col:sl) {
							int n = sl.size();
							try {
							for(int i=0;i<n;i++) {
								BiCellCollection col = sl.get(i);
								double iqty = 0;
								double oqty = 0;
								if(sto != null && i==0) iqty += sto.balance;
								String stmtype = col.getCell("stmd_tdtype").getString();
								if(
										stmtype.equals("MI")
										|| stmtype.equals("RI")
										|| stmtype.equals("BI")
										|| stmtype.equals("JI")
										|| stmtype.equals("KI")
										) {
//									accu.updateBalance(col.getCell("stm_date").getDate(), col.getCell("stmd_onhandqty").getDouble(), 0, 1);
									iqty += col.getCell("stmd_movqty").getDouble();
								}
								if(
										stmtype.equals("MO")
										|| stmtype.equals("RO")
										|| stmtype.equals("SO")
										|| stmtype.equals("JO")
										|| stmtype.equals("KO")
										) {
									oqty += col.getCell("stmd_movqty").getDouble();
//									accu.updateBalance(col.getCell("stm_date").getDate(), 0,col.getCell("stmd_onhandqty").getDouble(), 1);
								}
								accu.updateBalance(i, iqty,oqty, 1,true);
							}
							} catch (CalculationErrorException cex ){
								UniLog.log(cex);
							}
//							double balance = accu.getBalanceEnd(BaseAccumulator.accMaxDate);
//							double freestock = getCell("stg_freestock").getDouble();
//							double reserved = getCell("stg_reserved").getDouble();
							String cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
							int org = Erpv4Config.getCoWtAvOrg(getSessionHelper(), cocode);
							boolean iog = Erpv4Config.ignoreOrgInCost(getSessionHelper());
							try {
							for(int i=0;i<n;i++) {
								CellCollection col = sl.get(i);
								int rorg;
								if(iog) rorg = org; else rorg = getCellInt("stmd_org");
//								col.getCell("stmd_balance").set(accu.getBalanceEnd(col.getCell("stm_date").getDate()));
//								if(col.getCellInt("stmd_org") == org  || Erpv4Config.ignoreOrgInCost(getSessionHelper())) {
//								if(col.getCellInt("stmd_org") == org  ) {
									col.getCell("stmd_avcost").set(CostCalculation.getWaCost(getSessionHelper(),col.getCellInt("st_irg"), rorg, col.getCell("stmd_date").getDate()));
//								}
								col.getCell("stmd_balance").set(accu.getBalanceEnd(i));
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
	public String getLinkedView(String p_colName,CellCollection col) {
		if(p_colName.equals("stm_ref1")) {
			String module = col.getCellString("stm_module");
			String stmtype = col.getCellString("stm_type");
			String ref1 = col.getCellString("stm_ref1");
			UniLog.log("HAHA230303 getLinkedView " + ref1 + " : " + module + " : " + stmtype);
			if(stmtype.equals("GM")) return("erpv4.GR");
			if(module.equals("cstmo")) {
				return("erpv4.MoCustomer");
			}
			if(module.equals("vstmo")) {
				return("erpv4.MoSupplier");
			}
			if(module.equals("sttfr")) {
				return("erpv4.MoTransfer");
			}
			if(module.equals("stadj")) {
				return("erpv4.MoAdjustment");
			}
			if(module.equals("stake")) {
				return("erpv4.StockTake");
			}
			if(module.equals("stkg2")) {
				return("erpv4.StockTakeG2");
			}
			if(module.equals("cotfr")) {
				return("erpv4.CoTransferG2");
			}
		}
		return(super.getLinkedView(p_colName,col));
	}
}
