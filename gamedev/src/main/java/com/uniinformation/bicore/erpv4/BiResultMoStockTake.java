package com.uniinformation.bicore.erpv4;

import java.util.Set;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultMO;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.erpv4.StockTakeUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMoStockTake extends BiResultMO {

	public BiResultMoStockTake(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getPickColumnCondition(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("st_icode")) {
			/*
			if(Erpv4Config.isMultiCompany(sh)) {
			}
			*/
			if("Y".equals(Erpv4Config.getString(getSessionHelper(), "UseStockValidFlag"))) {
				return("st_obsolete<>'Y' and st_issalable = 'Y'");
			}
			
		}
		return(super.getPickColumnCondition(p_cc));
	}
	
	public ReturnMsg generateBalance(java.util.Date p_date,boolean p_skipUnchange,boolean p_fixFifo,String p_hashTag) {
		int numGenerated = 0;
		try {
			beginWork();
			java.util.Date balBeginDate;
			balBeginDate = Erpv4Config.getCostOpeningErpDate(getSessionHelper());
			SelectUtil su = getSelectUtil();
			TableRec tr = null;
			if(Erpv4Config.isMultiCompany(getSessionHelper())) {
				int lcrg = Erpv4Config.getDefaultLcrg(getSessionHelper());
				tr = su.getQueryResult("select * from locationcode where loc_mrg = ? and loc_tfronly <> 'Y' and loc_transit <> 'Y'",
					new Wherecl().appendArgument(lcrg)
				);
				if(tr.getRecordCount() == 1)		 {
					tr.setRecPointer(0);
					tr = su.getQueryResult("select st_irg from stock,stock_gen where st_irg not in ("
						+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? and stm_fromloc = ?)"
						+ " and stg_irg = st_irg and stg_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper())+ "' ",
						new Wherecl()
							.appendArgument(p_date)
							.appendArgument(tr.getFieldString("loc_code"))
					);
				} else {
					tr = su.getQueryResult("select st_irg from stock,stock_gen where st_irg not in ("
						+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? and stm_cocode = ?)"
						+ " and stg_irg = st_irg and stg_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper())+ "' ",
					new Wherecl()
						.appendArgument(p_date)
						.appendArgument(Erpv4Config.getDefaultCoCode(getSessionHelper()))
					);
				}
			} else {
				tr = su.getQueryResult("select st_irg from stock where st_irg not in ("
					+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? )",
					new Wherecl().appendArgument(p_date)
				);
			}
			Set<String> locList = null;
			if(Erpv4Config.isMultiCompany(getSessionHelper())) {
				if(Erpv4Config.isMultiStockLoc(getSessionHelper())) {
					locList = Erpv4Config.getLocationListByCompany(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_BYLCRG_EXCLUDE_TRANSIT);
				} else {
					locList = Erpv4Config.getLocationListByCompany(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_ANY);
				}
			}
			StockTakeUtil stkutil = new StockTakeUtil(Erpv4Config.getString(getSessionHelper(),StockTakeUtil.STOCKTAKEFILTER));
			java.util.Date bDate = p_date;
			boolean fixFifo = p_fixFifo;
			boolean skipUnchange = p_skipUnchange;
			clearCurrentRec();
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				int irg = tr.getFieldInt("st_irg");
				stkutil.init();
				stkutil.getBalance(su,irg,bDate,0,locList);
				if(!stkutil.isEmpty()) {
					stkutil.syncBalance(this, null);
					if(fixFifo) stkutil.fixFiFo(this, null,null);
					getCell("stm_ctrspec").set(p_hashTag);
					getCell("stm_nref4").set(irg);
					getCell("stm_date").set(bDate);
					if(bDate.after(balBeginDate)) {
							double avCost = CostCalculation.getWaCost(getSessionHelper(), irg, 
										Erpv4Config.getCoWtAvOrg(getSessionHelper(), getCellString("stm_cocode"))
												//	GenbucketUtil.WEIGHTED_AVERAGE_ORG
										, bDate);
							getCell("stm_fref4").set(avCost);
					} else {
							TableRec tr2 = su.getQueryResult("select * from stmov where stm_nref4 = ? and stm_status='Confirmed' and stm_module = 'stake' and stm_date <= ? order by stm_date desc",
								new Wherecl()
								.appendArgument(irg)
								.appendArgument(bDate)
							);
							if(tr2.getRecordCount() > 0) {
								tr2.setRecPointer(0);
								getCell("stm_fref4").set(tr2.getFieldDouble("stm_fref4"));
							}
					}
					stkutil.syncDelta(this,null);
					if(skipUnchange && !stkutil.hasDelta(this)) {
						clearCurrentRec();
						continue;
					}
					ReturnMsg rtn = addCurrent();
					clearCurrentRec();
					if(rtn != null && !rtn.getStatus()) {
						rollbackWork();
						return(rtn);
					}
					numGenerated++;
				}
			}
			commitWork();
		} catch (Exception ex) {
			UniLog.log(ex);
			rollbackWork();
			return(new ReturnMsg(false,ex.toString()));
		}
		ReturnMsg rtn = new ReturnMsg(true);
		rtn.setData(new Integer(numGenerated));
		return(rtn);
	}
}
