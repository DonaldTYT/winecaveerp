package com.uniinformation.bicore.wc;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.IgnoreValue;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class WcCellCollection extends Erpv4BaseCellCollection {
	private static final String UNIT_COST_CACHE_SESSION_KEY =
			WcCellCollection.class.getName() + ".unitCostCache";
	private static final long SELECT_UTIL_IDLE_THRESHOLD_MS = 60_000L;
	private static final long UNIT_COST_CACHE_TTL_MS = 30L * 60L * 1000L;

	private static final class UnitCostCacheKey {
		private final int irg;
		private final int org;
		private final int dateKey;

		UnitCostCacheKey(int p_irg,int p_org,java.util.Date p_date) {
			irg = p_irg;
			org = p_org;
			dateKey = toDateKey(p_date);
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + irg;
			result = 31 * result + org;
			result = 31 * result + dateKey;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof UnitCostCacheKey)) return false;
			UnitCostCacheKey other = (UnitCostCacheKey) obj;
			return irg == other.irg && org == other.org && dateKey == other.dateKey;
		}

		private static int toDateKey(java.util.Date date) {
			if(date == null) return 0;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar.get(Calendar.YEAR) * 1000
					+ calendar.get(Calendar.DAY_OF_YEAR);
		}
	}

	private static final class UnitCostCache {
		private final Map<UnitCostCacheKey,Double> costs =
				new HashMap<UnitCostCacheKey,Double>();
		private SelectUtil selectUtil;
		private NewStockCost newStockCost;
		private long lastSelectUtilUse;

		synchronized double getNetUnitPrice(SessionHelper sessionHelper,int irg,int org,
				java.util.Date date) throws Exception {
			long now = System.currentTimeMillis();
			closeSelectUtilIfIdle(now);
			UnitCostCacheKey key = new UnitCostCacheKey(irg,org,date);
			Double cachedCost = costs.get(key);
			if(cachedCost != null) return cachedCost.doubleValue();

			ensureSelectUtil(sessionHelper);
			try {
				double cost = newStockCost.newstcost_getnetuprice(irg,org,date);
				costs.put(key,Double.valueOf(cost));
				lastSelectUtilUse = System.currentTimeMillis();
				return cost;
			} catch(Exception ex) {
				// A failed SQL operation can leave the pooled/scorpion connection in
				// an unknown state. Never reuse it on the next calculation.
				closeSelectUtil();
				throw ex;
			}
		}

		private void ensureSelectUtil(SessionHelper sessionHelper) throws Exception {
			if(selectUtil != null && newStockCost != null) return;
			SelectUtil openedSelectUtil = new SelectUtil();
			try {
				openedSelectUtil.init(sessionHelper.getBiSchema().getConn());
				selectUtil = openedSelectUtil;
				newStockCost = new NewStockCost(selectUtil);
				lastSelectUtilUse = System.currentTimeMillis();
			} catch(Exception ex) {
				openedSelectUtil.close();
				selectUtil = null;
				newStockCost = null;
				lastSelectUtilUse = 0L;
				throw ex;
			}
		}

		private void closeSelectUtilIfIdle(long now) {
			if(selectUtil != null && now-lastSelectUtilUse > SELECT_UTIL_IDLE_THRESHOLD_MS) {
				closeSelectUtil();
			}
		}

		synchronized void close() {
			closeSelectUtil();
			costs.clear();
		}

		private void closeSelectUtil() {
			if(selectUtil != null) {
				selectUtil.close();
			}
			selectUtil = null;
			newStockCost = null;
			lastSelectUtilUse = 0L;
		}
	}

	private static final SessionHelper.SessionDataExCleanUpCallback UNIT_COST_CACHE_CLEANUP =
			new SessionHelper.SessionDataExCleanUpCallback() {
				@Override
				public void cleanUp(Object key,Object data) {
					if(data instanceof UnitCostCache) {
						((UnitCostCache) data).close();
					}
				}
			};

	String getNonConsignOwner() {
		return("WINECAVE");
	}

	public WcCellCollection(BiCellCollection p_col, BiResultErpv4 p_br) {
		super(p_col, p_br);
		// TODO Auto-generated constructor stub
	}
	private enum FuncName { FUNC_newBrandCode, FUNC_getConsignCost, FUNC_getConsignPrice, FUNC_getSmCode, FUNC_getUnitCost,NOT_DEFINED }
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
		case FUNC_getSmCode: {
			SelectUtil su = br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from salesman where sm_logname = ?",
						new Wherecl().appendArgument(br.getSessionHelper().getVcode()));
			if(tr.getRecordCount() <= 0) return("");
			tr.setRecPointer(0);
			String ss = tr.getFieldString("sm_code");
			if(StringUtils.isBlank(ss)) {
				return(new IgnoreValue());
			} else return(ss);
		}
		case FUNC_getConsignCost: {
			int irg = Cell.objectToInt(p_args.get(0));
			int org = Cell.objectToInt(p_args.get(1));
			if(irg == 0 || org  == 0) return(0.0);
			String cocode = (String) p_args.get(2);
			if(StringUtils.isBlank(cocode) || cocode.equals(getNonConsignOwner())) {
				return(0.0);
			}
			SelectUtil su = br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from consgprice where consgp_irg = ? and consgp_org = ?",
						new Wherecl().appendArgument(irg).appendArgument(org));
			if(tr.getRecordCount() <= 0) return(0.0);
			tr.setRecPointer(0);
			return(tr.getFieldDouble("consgp_cost"));
		}
		case FUNC_getConsignPrice : {
			int irg = Cell.objectToInt(p_args.get(0));
			int org = Cell.objectToInt(p_args.get(1));
			if(irg == 0 || org  == 0) return(0.0);
			double wprice = (Double) p_args.get(2);
			String cocode = (String) p_args.get(3);
			if(StringUtils.isBlank(cocode) || cocode.equals(getNonConsignOwner())) {
				return(wprice);
			}
			SelectUtil su = br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from consgprice where consgp_irg = ? and consgp_org = ?",
						new Wherecl().appendArgument(irg).appendArgument(org));
			if(tr.getRecordCount() <= 0) return(0.0);
			tr.setRecPointer(0);
			return(tr.getFieldDouble("consgp_price"));
		}
		case FUNC_newBrandCode: {
				String wc = Erpv4Config.getString(br.getSessionHelper(), "WINEAC");
				if(wc != null && wc.equals("Y")) {
					String ss = br.getCellString("stbd_code");
					if(ss.trim().equals("")) {
						RpcClient rpc = br.getSelectUtil().getRpcClient();
						Value v = rpc.callSegment("new_brand_code");
						if(v != null ) {
							return(v.toString());
						}
					}
				}
				return(br.getCellString("stbd_code"));
			}
		case FUNC_getUnitCost : {
			String owner = (String) p_args.get(3);
			if(getNonConsignOwner().equals(owner)) return(0.0);
			java.util.Date costDate = (java.util.Date) p_args.get(2);
			int irg = Cell.objectToInt(p_args.get(0));
			int org = Cell.objectToInt(p_args.get(1));
			return(cached_newstcost_getnetuprice(irg,org,costDate));
		}
		}

		return(super.evalFunction(p_fname,p_args) );
	}

	double cached_newstcost_getnetuprice(int p_irg,int p_org,java.util.Date p_date)
			throws Exception {
		SessionHelper sessionHelper = br.getSessionHelper();
		UnitCostCache cache = getUnitCostCache(sessionHelper);
		try {
			return cache.getNetUnitPrice(sessionHelper,p_irg,p_org,p_date);
		} catch(Exception ex) {
			UniLog.log(ex);
			throw ex;
		}
	}

	private UnitCostCache getUnitCostCache(SessionHelper sessionHelper) {
		synchronized(sessionHelper) {
			Object sessionValue = sessionHelper.getSessionData(UNIT_COST_CACHE_SESSION_KEY);
			if(sessionValue instanceof SessionHelper.SessionDataEx) {
				sessionValue = ((SessionHelper.SessionDataEx) sessionValue).getData();
			}
			if(sessionValue instanceof UnitCostCache) {
				return (UnitCostCache) sessionValue;
			}
			UnitCostCache cache = new UnitCostCache();
			sessionHelper.putSessionDataEx(UNIT_COST_CACHE_SESSION_KEY,cache,
					UNIT_COST_CACHE_TTL_MS,UNIT_COST_CACHE_CLEANUP);
			return cache;
		}
	}

}
