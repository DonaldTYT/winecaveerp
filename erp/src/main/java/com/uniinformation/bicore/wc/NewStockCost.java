package com.uniinformation.bicore.wc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kyoko.common.DateUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;

/**
 * Java conversion of the Wine Cave {@code newstcost.chn} stock-cost library.
 *
 * <p>An instance contains the calculation state for one stock item after
 * {@link #newstcost_init(int, Date, String)} has been called. It is therefore
 * deliberately not static and must not be shared by concurrent calculations.</p>
 */
public class NewStockCost {
	public static final String COSTMETHOD_FIFO = "F";
	public static final String COSTMETHOD_WEIGHTED_AVERAGE = "A";
	public static final String COSTMETHOD_LOT_BY_LOT = "L";

	public static final int INCLUDE_NORMAL_AND_ON_ORDER = 0;
	public static final int IGNORE_ON_ORDER = 1;
	public static final int ONLY_ON_ORDER = 2;

	public static final String DEFAULT_OWNER = "WINECAVE";
	public static final String ON_ORDER_WAREHOUSE = "OODR";

	private static final double ZERO_TOLERANCE = 0.0000001d;

	public static final class Balance {
		private final double inputQuantity;
		private final double inputCost;
		private final double outputQuantity;
		private final double outputCost;
		private final double outputPrice;

		Balance(double p_inputQuantity,double p_inputCost,double p_outputQuantity,
				double p_outputCost,double p_outputPrice) {
			inputQuantity = p_inputQuantity;
			inputCost = p_inputCost;
			outputQuantity = p_outputQuantity;
			outputCost = p_outputCost;
			outputPrice = p_outputPrice;
		}

		public double getInputQuantity() { return inputQuantity; }
		public double getInputCost() { return inputCost; }
		public double getOutputQuantity() { return outputQuantity; }
		public double getOutputCost() { return outputCost; }
		public double getOutputPrice() { return outputPrice; }
		public double getQuantityOnHand() { return inputQuantity - outputQuantity; }
		public double getCostOnHand() { return inputCost - outputCost; }
	}

	public static final class CostLot {
		private final int org;
		private double inputQuantity;
		private double inputCost;
		private double outputQuantity;
		private double outputCost;
		private double outputPrice;

		CostLot(int p_org) {
			org = p_org;
		}

		CostLot copy() {
			CostLot result = new CostLot(org);
			result.inputQuantity = inputQuantity;
			result.inputCost = inputCost;
			result.outputQuantity = outputQuantity;
			result.outputCost = outputCost;
			result.outputPrice = outputPrice;
			return result;
		}

		public int getOrg() { return org; }
		public double getInputQuantity() { return inputQuantity; }
		public double getInputCost() { return inputCost; }
		public double getOutputQuantity() { return outputQuantity; }
		public double getOutputCost() { return outputCost; }
		public double getOutputPrice() { return outputPrice; }
	}

	private static final class DatedBalance {
		Date date;
		Balance balance;

		DatedBalance(Date p_date,Balance p_balance) {
			date = p_date;
			balance = p_balance;
		}
	}

	private static final class PurchaseBalance {
		int org;
		Date date;
		double inputQuantity;
		double outputQuantity;

		PurchaseBalance(int p_org,Date p_date) {
			org = p_org;
			date = p_date;
		}
	}

	private final SelectUtil selectUtil;
	private final String defaultOwner;
	private final String onOrderWarehouse;
	private final LinkedHashMap<Integer,CostLot> lotsByOrg =
			new LinkedHashMap<Integer,CostLot>();
	private final LinkedHashMap<Integer,PurchaseBalance> purchasesByOrg =
			new LinkedHashMap<Integer,PurchaseBalance>();
	private final ArrayList<DatedBalance> datedBalances = new ArrayList<DatedBalance>();

	private int ignoreOnOrder = INCLUDE_NORMAL_AND_ON_ORDER;
	private boolean useNetCost;
	private int itemRg;
	private Date costDate = DateUtil.zeroDate;
	private String costMethod = COSTMETHOD_LOT_BY_LOT;
	private double sumInputQuantity;
	private double sumOutputQuantity;
	private double sumInputCost;
	private double sumOutputCost;
	private double sumOutputPrice;

	public NewStockCost(SelectUtil p_selectUtil) {
		this(p_selectUtil,DEFAULT_OWNER,ON_ORDER_WAREHOUSE);
	}

	public NewStockCost(SelectUtil p_selectUtil,String p_defaultOwner,
			String p_onOrderWarehouse) {
		if(p_selectUtil == null) throw new IllegalArgumentException("SelectUtil is required");
		selectUtil = p_selectUtil;
		defaultOwner = normalize(p_defaultOwner);
		onOrderWarehouse = normalize(p_onOrderWarehouse);
	}

	public void newstcost_setmode(String p_cocode,int p_ignoreOnOrder,int p_useNetCost) {
		if(p_ignoreOnOrder < INCLUDE_NORMAL_AND_ON_ORDER || p_ignoreOnOrder > ONLY_ON_ORDER) {
			throw new IllegalArgumentException("Unsupported on-order mode " + p_ignoreOnOrder);
		}
		ignoreOnOrder = p_ignoreOnOrder;
		useNetCost = p_useNetCost != 0;
	}

	public void newstcost_init(int p_irg,Date p_date) throws Exception {
		newstcost_init(p_irg,p_date,COSTMETHOD_LOT_BY_LOT);
	}

	public void newstcost_init(int p_irg,Date p_date,String p_costMethod) throws Exception {
		if(p_date == null) throw new IllegalArgumentException("Cost date is required");
		if(!COSTMETHOD_LOT_BY_LOT.equals(p_costMethod)
				&& !COSTMETHOD_WEIGHTED_AVERAGE.equals(p_costMethod)
				&& !COSTMETHOD_FIFO.equals(p_costMethod)) {
			throw new IllegalArgumentException("Unknown cost method " + p_costMethod);
		}

		reset(p_irg,p_costMethod);
		loadOpeningSnapshot(p_irg,p_date);
		if(!p_date.after(DateUtil.nextday(costDate))) return;

		TableRec movements = selectUtil.getQueryResult(
				"select stmd_org,stmd_date,stmd_loc,stmd_tdtype,stmd_exprice1,"
				+ "stmd_qty,stmd_retqty,stmd_exprice,stmd_xrate,stmd_mrg,"
				+ "stmd_tdindex,stmd_qorg,or_cocode,or_stmrg "
				+ "from stmovd,orders,stmov "
				+ "where stmd_irg = ? and stmd_date > ? and stmd_date < ? "
				+ "and or_org = stmd_org and stm_mrg = stmd_mrg and stm_void <> 'Y' "
				+ "order by stmd_date,stmd_qty desc,stmd_mrg",
				new Wherecl().appendArgument(p_irg).appendArgument(costDate)
						.appendArgument(p_date));
		for(int i=0;i<movements.getRecordCount();i++) {
			movements.setRecPointer(i);
			newstcost_addentry(
					movements.getFieldInt("stmd_org"),
					movements.getFieldDate("stmd_date"),
					movements.getFieldString("stmd_loc"),
					movements.getFieldString("stmd_tdtype"),
					movements.getFieldDouble("stmd_exprice1"),
					movements.getFieldDouble("stmd_qty"),
					movements.getFieldDouble("stmd_retqty"),
					movements.getFieldDouble("stmd_exprice"),
					movements.getFieldDouble("stmd_xrate"),
					movements.getFieldInt("stmd_mrg"),
					movements.getFieldInt("stmd_tdindex"),
					movements.getFieldString("or_cocode"),
					movements.getFieldInt("or_stmrg"),
					movements.getFieldInt("stmd_qorg"));
		}
	}

	private void reset(int p_irg,String p_costMethod) {
		lotsByOrg.clear();
		purchasesByOrg.clear();
		datedBalances.clear();
		itemRg = p_irg;
		costMethod = p_costMethod;
		costDate = DateUtil.zeroDate;
		sumInputQuantity = 0;
		sumOutputQuantity = 0;
		sumInputCost = 0;
		sumOutputCost = 0;
		sumOutputPrice = 0;
		datedBalances.add(new DatedBalance(DateUtil.zeroDate,newstcost_getbalance()));
	}

	private void loadOpeningSnapshot(int p_irg,Date p_beforeDate) throws Exception {
		TableRec headers = selectUtil.getQueryResult(
				"select stc_irg,stc_date,stc_iqty,stc_oqty,stc_nwicost,stc_nwocost,"
				+ "stc_noprice,stc_gwicost,stc_gwocost,stc_goprice "
				+ "from stcost where stc_irg = ? and stc_date < ? order by stc_date",
				new Wherecl().appendArgument(p_irg).appendArgument(p_beforeDate));
		if(headers.getRecordCount() == 0) return;
		headers.setRecPointer(headers.getRecordCount()-1);
		costDate = headers.getFieldDate("stc_date");

		TableRec details = selectUtil.getQueryResult(
				"select stcd_org,stcd_iqty,stcd_oqty,stcd_nicost,stcd_nocost,"
				+ "stcd_noprice,stcd_gicost,stcd_gocost,stcd_goprice "
				+ "from stcostdet where stcd_irg = ? and stcd_date = ? order by stcd_org",
				new Wherecl().appendArgument(p_irg).appendArgument(costDate));

		if(COSTMETHOD_WEIGHTED_AVERAGE.equals(costMethod)) {
			CostLot lot = getOrCreateLot(0);
			lot.inputQuantity = headers.getFieldDouble("stc_iqty");
			lot.outputQuantity = headers.getFieldDouble("stc_oqty");
			if(useNetCost) {
				lot.inputCost = headers.getFieldDouble("stc_nwicost");
				lot.outputCost = headers.getFieldDouble("stc_nwocost");
				lot.outputPrice = headers.getFieldDouble("stc_noprice");
			} else {
				lot.inputCost = headers.getFieldDouble("stc_gwicost");
				lot.outputCost = headers.getFieldDouble("stc_gwocost");
				lot.outputPrice = headers.getFieldDouble("stc_goprice");
			}
		} else {
			for(int i=0;i<details.getRecordCount();i++) {
				details.setRecPointer(i);
				CostLot lot = getOrCreateLot(details.getFieldInt("stcd_org"));
				lot.inputQuantity = details.getFieldDouble("stcd_iqty");
				lot.outputQuantity = details.getFieldDouble("stcd_oqty");
				if(useNetCost) {
					lot.inputCost = details.getFieldDouble("stcd_nicost");
					lot.outputCost = details.getFieldDouble("stcd_nocost");
					lot.outputPrice = details.getFieldDouble("stcd_noprice");
				} else {
					lot.inputCost = details.getFieldDouble("stcd_gicost");
					lot.outputCost = details.getFieldDouble("stcd_gocost");
					lot.outputPrice = details.getFieldDouble("stcd_goprice");
				}
			}
		}

		for(int i=0;i<details.getRecordCount();i++) {
			details.setRecPointer(i);
			PurchaseBalance purchase = getOrCreatePurchase(
					details.getFieldInt("stcd_org"),costDate);
			purchase.inputQuantity += details.getFieldDouble("stcd_iqty");
			purchase.outputQuantity += details.getFieldDouble("stcd_oqty");
		}
		recalculateTotals();
		datedBalances.get(0).balance = newstcost_getbalance();
	}

	/**
	 * Owner/order-aware overload selected by NEWSTCOST_CHECK_OD in the PERF build.
	 * The legacy wrapper always returned zero; callers needing the calculated cost
	 * should call {@link #newstcost_addentry_raw(int, Date, String, String, double, double, double)}.
	 */
	public double newstcost_addentry(int p_org,Date p_date,String p_loc,String p_tdtype,
			double p_exprice1,double p_qty,double p_retqty,double p_exprice,double p_xrate,
			int p_mrg,int p_tdindex,String p_cocode,int p_stmrg,int p_qorg) throws Exception {
		if(ignoreOnOrder == ONLY_ON_ORDER) {
			throw new UnsupportedOperationException("The legacy newstcost mode 2 is not supported");
		}

		String tdtype = normalize(p_tdtype);
		String owner = normalize(p_cocode);
		String location = normalize(p_loc);
		int realOrg = p_org;
		double realExprice = p_exprice;
		double realExprice1 = p_exprice1;
		double realXrate = p_xrate;

		if(ignoreOnOrder == INCLUDE_NORMAL_AND_ON_ORDER) {
			if("PD".equals(tdtype) || "SD".equals(tdtype)) return -1;
			if("JA".equals(tdtype) || "MI".equals(tdtype) || "JB".equals(tdtype)) {
				if(!defaultOwner.equals(owner) || onOrderWarehouse.equals(location)) return -1;
			} else if("KO".equals(tdtype) || "SO".equals(tdtype)) {
				if(p_qorg < 100000) return -1;
				TableRec source = selectUtil.getQueryResult(
						"select s1.stmd_org realorg,or_cocode realcocode "
						+ "from stmovd s0,stmovd s1,orders "
						+ "where s0.stmd_qorg = ? and s0.stmd_irg = ? "
						+ "and s0.stmd_tdtype = 'SI' and s1.stmd_mrg = s0.stmd_mrg "
						+ "and s1.stmd_tdindex = s0.stmd_tdindex and s1.stmd_tdtype = 'MO' "
						+ "and or_org = s1.stmd_org",
						new Wherecl().appendArgument(p_qorg).appendArgument(itemRg));
				if(source.getRecordCount() == 0) return -1;
				source.setRecPointer(0);
				realOrg = source.getFieldInt("realorg");
				if(!defaultOwner.equals(normalize(source.getFieldString("realcocode")))) return -1;
			} else if("MO".equals(tdtype)) {
				if(!defaultOwner.equals(owner)) return -1;
			} else if("BI".equals(tdtype)) {
				if(!defaultOwner.equals(owner)) {
					TableRec source = selectUtil.getQueryResult(
							"select s3.stmd_org realorg,s4.stmd_exprice realexprice,"
							+ "s4.stmd_exprice1 realexprice1,s4.stmd_xrate realxrate,"
							+ "s4.stmd_qty realqty "
							+ "from stmovd s1,stmovd s2,stmovd s3,stmovd s4 "
							+ "where s1.stmd_mrg = ? and s1.stmd_tdindex = ? "
							+ "and s1.stmd_tdtype = 'SD' and s2.stmd_org = s1.stmd_org "
							+ "and s2.stmd_irg = s1.stmd_irg and s2.stmd_tdtype = 'SI' "
							+ "and s3.stmd_mrg = s2.stmd_mrg "
							+ "and s3.stmd_tdindex = s2.stmd_tdindex and s3.stmd_tdtype = 'MO' "
							+ "and s4.stmd_irg = s3.stmd_irg and s4.stmd_org = s3.stmd_org "
							+ "and s4.stmd_tdtype = 'PD'",
							new Wherecl().appendArgument(p_mrg).appendArgument(p_tdindex));
					if(source.getRecordCount() == 0) return -1;
					source.setRecPointer(0);
					realOrg = source.getFieldInt("realorg");
					realExprice = source.getFieldDouble("realexprice");
					realExprice1 = source.getFieldDouble("realexprice1");
					realXrate = source.getFieldDouble("realxrate");
					double realQty = source.getFieldDouble("realqty");
					if(realQty > 0) {
						realExprice = realExprice * p_qty / realQty;
						realExprice1 = realExprice1 * p_qty / realQty;
					}
				}
			} else {
				return -1;
			}
		} else {
			if("PD".equals(tdtype) || "SD".equals(tdtype)) return -1;
			if("JA".equals(tdtype) || "MI".equals(tdtype)
					|| "JB".equals(tdtype) || "MO".equals(tdtype)) {
				if(!defaultOwner.equals(owner) || onOrderWarehouse.equals(location)) return -1;
			} else if("BI".equals(tdtype)) {
				if(!defaultOwner.equals(owner)) return -1;
			} else {
				return -1;
			}
		}

		double effectiveCost = useNetCost ? realExprice * realXrate : realExprice1;
		newstcost_addentry_raw(realOrg,p_date,p_loc,tdtype,effectiveCost,p_qty,p_retqty);
		return 0;
	}

	public double newstcost_addentry_raw(int p_org,Date p_date,String p_loc,
			String p_tdtype,double p_exprice1,double p_qty,double p_retqty) throws Exception {
		if(p_date == null) throw new IllegalArgumentException("Movement date is required");
		String tdtype = normalize(p_tdtype);
		int lotOrg = COSTMETHOD_WEIGHTED_AVERAGE.equals(costMethod) ? 0 : p_org;
		CostLot lot = getOrCreateLot(lotOrg);
		double transactionCost = 0;

		if("PD".equals(tdtype) || "JA".equals(tdtype) || "BI".equals(tdtype)
				|| "MI".equals(tdtype)) {
			if(p_qty < 0 && !isZero(p_qty)) {
				throw new IllegalArgumentException("Invalid inbound quantity " + p_qty
						+ " for item " + itemRg + ", org " + p_org);
			}
			lot.inputQuantity += p_qty;
			lot.inputCost += p_exprice1;
			sumInputQuantity += p_qty;
			sumInputCost += p_exprice1;
			transactionCost = p_exprice1;
			PurchaseBalance purchase = getOrCreatePurchase(p_org,p_date);
			purchase.inputQuantity += p_qty;
		} else if("KO".equals(tdtype) || "SO".equals(tdtype) || "JB".equals(tdtype)
				|| "SD".equals(tdtype) || "MO".equals(tdtype)) {
			if(p_qty > 0 && !isZero(p_qty)) {
				throw new IllegalArgumentException("Invalid outbound quantity " + p_qty
						+ " for item " + itemRg + ", org " + p_org);
			}
			if(p_qty < 0) {
				double availableQuantity = lot.inputQuantity - lot.outputQuantity;
				if(availableQuantity + p_qty < 0) {
					UniLog.log("Not enough stock for cost calculation: item " + itemRg
							+ ", org " + p_org + ", date " + p_date
							+ ", shortage " + (availableQuantity + p_qty));
					TableRec totals;
					if(useNetCost) {
						totals = selectUtil.getQueryResult(
								"select sum(stmd_exprice * stmd_xrate) tmpcost,sum(stmd_qty) tmpqty "
								+ "from stmovd where stmd_irg = ? and stmd_org = ? "
								+ "and stmd_tdtype in ('MI','PD','BI')",
								new Wherecl().appendArgument(itemRg).appendArgument(p_org));
					} else {
						totals = selectUtil.getQueryResult(
								"select sum(stmd_exprice1) tmpcost,sum(stmd_qty) tmpqty "
								+ "from stmovd where stmd_irg = ? and stmd_org = ? "
								+ "and stmd_tdtype in ('MI','PD','BI')",
								new Wherecl().appendArgument(itemRg).appendArgument(p_org));
					}
					if(totals.getRecordCount() > 0) {
						totals.setRecPointer(0);
						// Preserve the PERF implementation: its intended division by
						// total quantity is commented out in newstcost.chn.
						transactionCost = totals.getFieldDouble("tmpcost");
					}
				} else if(!isZero(availableQuantity)) {
					transactionCost = (lot.inputCost-lot.outputCost)
							* (p_qty/availableQuantity);
				}
				lot.outputQuantity -= p_qty;
				lot.outputPrice -= p_exprice1;
				lot.outputCost -= transactionCost;
				sumOutputPrice -= p_exprice1;
				sumOutputCost -= transactionCost;
				sumOutputQuantity -= p_qty;
				PurchaseBalance purchase = purchasesByOrg.get(p_org);
				if(purchase == null) {
					UniLog.log("Purchase lot not found for outbound cost entry: item "
							+ itemRg + ", org " + p_org);
				} else {
					purchase.outputQuantity -= p_qty;
				}
			}
		}

		updateDatedBalance(p_date);
		return transactionCost;
	}

	public Balance newstcost_getbalance() {
		return new Balance(sumInputQuantity,sumInputCost,sumOutputQuantity,
				sumOutputCost,sumOutputPrice);
	}

	public Balance newstcost_getbalance_asat(Date p_date) {
		if(p_date == null) return null;
		for(int i=datedBalances.size()-1;i>=0;i--) {
			DatedBalance row = datedBalances.get(i);
			if(!row.date.after(p_date)) return row.balance;
		}
		return null;
	}

	public double newstcost_getbalance_by_indate(Date p_from,Date p_to) {
		double quantity = 0;
		for(PurchaseBalance row : purchasesByOrg.values()) {
			if(!row.date.before(p_from) && !row.date.after(p_to)) {
				quantity += row.inputQuantity-row.outputQuantity;
			}
		}
		return quantity;
	}

	public double newstcost_getuprice(int p_irg,int p_org,Date p_date) throws Exception {
		return getUnitPrice(p_irg,p_org,false);
	}

	public double newstcost_getnetuprice(int p_irg,int p_org,Date p_date) throws Exception {
		return getUnitPrice(p_irg,p_org,true);
	}

	private double getUnitPrice(int p_irg,int p_org,boolean net) throws Exception {
		String costExpression = net
				? "sum(stmd_exprice * stmd_xrate) total_cost"
				: "sum(stmd_exprice1) total_cost";
		TableRec result = selectUtil.getQueryResult(
				"select sum(stmd_qty+stmd_xqty) total_qty," + costExpression + " "
				+ "from stmovd where stmd_irg = ? and stmd_org = ? "
				+ "and stmd_tdtype in ('MI','BI','PD')",
				new Wherecl().appendArgument(p_irg).appendArgument(p_org));
		if(result.getRecordCount() == 0) return 0;
		result.setRecPointer(0);
		double quantity = result.getFieldDouble("total_qty");
		return isZero(quantity) ? 0 : result.getFieldDouble("total_cost")/quantity;
	}

	/**
	 * Rebuilds the legacy stcost/stcostdet snapshot for one item and date.
	 * All delete/insert/update statements run in one transaction when the supplied
	 * SelectUtil connection was initially in auto-commit mode.
	 */
	public void newstcost_makepend(int p_irg,Date p_date) throws Exception {
		if(p_date == null) throw new IllegalArgumentException("Snapshot date is required");
		boolean ownTransaction = selectUtil.getAutoCommit();
		try {
			if(ownTransaction) selectUtil.setAutoCommit(false);
			selectUtil.executeUpdate("delete from stcostdet where stcd_irg = ? and stcd_date >= ?",
					new Wherecl().appendArgument(p_irg).appendArgument(p_date));
			selectUtil.executeUpdate("delete from stcost where stc_irg = ? and stc_date >= ?",
					new Wherecl().appendArgument(p_irg).appendArgument(p_date));

			newstcost_setmode(DEFAULT_OWNER,INCLUDE_NORMAL_AND_ON_ORDER,1);
			newstcost_init(p_irg,DateUtil.nextday(p_date),COSTMETHOD_LOT_BY_LOT);
			if(lotsByOrg.isEmpty() && !hasMovementBefore(p_irg,p_date)) {
				if(ownTransaction) selectUtil.commit();
				return;
			}
			Balance netLot = newstcost_getbalance();
			LinkedHashMap<Integer,CostLot> netLots = copyLots();

			for(CostLot lot : netLots.values()) {
				selectUtil.executeUpdate(
						"insert into stcostdet (stcd_irg,stcd_date,stcd_org,stcd_iqty,"
						+ "stcd_oqty,stcd_nicost,stcd_nocost,stcd_noprice,"
						+ "stcd_gicost,stcd_gocost,stcd_goprice) values (?,?,?,?,?,?,?,?,?,?,?)",
						new Wherecl().appendArgument(p_irg).appendArgument(p_date)
								.appendArgument(lot.org).appendArgument(lot.inputQuantity)
								.appendArgument(lot.outputQuantity).appendArgument(lot.inputCost)
								.appendArgument(lot.outputCost).appendArgument(lot.outputPrice)
								.appendArgument(0d).appendArgument(0d).appendArgument(0d));
			}

			newstcost_init(p_irg,DateUtil.nextday(p_date),COSTMETHOD_WEIGHTED_AVERAGE);
			Balance netWeighted = newstcost_getbalance();

			newstcost_setmode(DEFAULT_OWNER,INCLUDE_NORMAL_AND_ON_ORDER,0);
			newstcost_init(p_irg,DateUtil.nextday(p_date),COSTMETHOD_LOT_BY_LOT);
			Balance grossLot = newstcost_getbalance();
			for(CostLot lot : lotsByOrg.values()) {
				selectUtil.executeUpdate(
						"update stcostdet set stcd_gicost = ?,stcd_gocost = ?,stcd_goprice = ? "
						+ "where stcd_irg = ? and stcd_date = ? and stcd_org = ?",
						new Wherecl().appendArgument(lot.inputCost).appendArgument(lot.outputCost)
								.appendArgument(lot.outputPrice).appendArgument(p_irg)
								.appendArgument(p_date).appendArgument(lot.org));
			}

			newstcost_init(p_irg,DateUtil.nextday(p_date),COSTMETHOD_WEIGHTED_AVERAGE);
			Balance grossWeighted = newstcost_getbalance();
			selectUtil.executeUpdate(
					"insert into stcost (stc_irg,stc_date,stc_iqty,stc_oqty,"
					+ "stc_nlicost,stc_nlocost,stc_noprice,stc_nwicost,stc_nwocost,"
					+ "stc_glicost,stc_glocost,stc_goprice,stc_gwicost,stc_gwocost) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Wherecl().appendArgument(p_irg).appendArgument(p_date)
							.appendArgument(netLot.inputQuantity).appendArgument(netLot.outputQuantity)
							.appendArgument(netLot.inputCost).appendArgument(netLot.outputCost)
							.appendArgument(netLot.outputPrice).appendArgument(netWeighted.inputCost)
							.appendArgument(netWeighted.outputCost).appendArgument(grossLot.inputCost)
							.appendArgument(grossLot.outputCost).appendArgument(grossLot.outputPrice)
							.appendArgument(grossWeighted.inputCost).appendArgument(grossWeighted.outputCost));
			if(ownTransaction) selectUtil.commit();
		} catch(Exception ex) {
			if(ownTransaction) selectUtil.rollback();
			throw ex;
		} finally {
			if(ownTransaction) selectUtil.setAutoCommit(true);
		}
	}

	public List<CostLot> getCostLots() {
		ArrayList<CostLot> result = new ArrayList<CostLot>();
		for(CostLot lot : lotsByOrg.values()) result.add(lot.copy());
		return Collections.unmodifiableList(result);
	}

	public int getItemRg() { return itemRg; }
	public Date getCostDate() { return costDate; }
	public String getCostMethod() { return costMethod; }
	public boolean isUseNetCost() { return useNetCost; }
	public int getIgnoreOnOrderMode() { return ignoreOnOrder; }

	private boolean hasMovementBefore(int p_irg,Date p_date) throws Exception {
		TableRec result = selectUtil.getQueryResult(
				"select serial_id from stmovd where stmd_irg = ? and stmd_date > ? "
				+ "and stmd_date < ? limit 1",
				new Wherecl().appendArgument(p_irg).appendArgument(DateUtil.zeroDate)
						.appendArgument(p_date));
		return result.getRecordCount() > 0;
	}

	private CostLot getOrCreateLot(int org) {
		CostLot lot = lotsByOrg.get(org);
		if(lot == null) {
			lot = new CostLot(org);
			lotsByOrg.put(org,lot);
		}
		return lot;
	}

	private PurchaseBalance getOrCreatePurchase(int org,Date date) {
		PurchaseBalance result = purchasesByOrg.get(org);
		if(result == null) {
			result = new PurchaseBalance(org,date);
			purchasesByOrg.put(org,result);
		}
		return result;
	}

	private void recalculateTotals() {
		sumInputQuantity = 0;
		sumOutputQuantity = 0;
		sumInputCost = 0;
		sumOutputCost = 0;
		sumOutputPrice = 0;
		for(CostLot lot : lotsByOrg.values()) {
			sumInputQuantity += lot.inputQuantity;
			sumOutputQuantity += lot.outputQuantity;
			sumInputCost += lot.inputCost;
			sumOutputCost += lot.outputCost;
			sumOutputPrice += lot.outputPrice;
		}
	}

	private void updateDatedBalance(Date date) {
		DatedBalance row = datedBalances.get(datedBalances.size()-1);
		if(date.after(row.date)) {
			row = new DatedBalance(date,newstcost_getbalance());
			datedBalances.add(row);
		} else {
			row.balance = newstcost_getbalance();
		}
	}

	private LinkedHashMap<Integer,CostLot> copyLots() {
		LinkedHashMap<Integer,CostLot> result = new LinkedHashMap<Integer,CostLot>();
		for(Map.Entry<Integer,CostLot> entry : lotsByOrg.entrySet()) {
			result.put(entry.getKey(),entry.getValue().copy());
		}
		return result;
	}

	private static boolean isZero(double value) {
		return Math.abs(value) < ZERO_TOLERANCE;
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim();
	}
}
