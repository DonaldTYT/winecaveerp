package com.uniinformation.bicore.wc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockOutChg extends BiResultErpv4 {
	private static final String STOCK_OUT_DETAIL_LINK = "graphql.StorageDet";
	private static final String STOCK_OUT_CHARGE_LINK = "graphql.StmpostExtOM";
	private static final Date STOCK_OUT_2208_DATE = DateUtil.getDate("2022/08/31");
	private static final int MINIMUM_VOLUME_UNIT = 9000;

	private static class StockOutDetailRow {
		String cocode;
		int org;
		int irg;
		int pkg;
		int storageQty;
		int consignmentQty;
		int stockMovementMrg;

		StockOutDetailRow(String p_cocode,int p_org,int p_irg,int p_pkg,int p_stockMovementMrg) {
			cocode = p_cocode;
			org = p_org;
			irg = p_irg;
			pkg = p_pkg;
			stockMovementMrg = p_stockMovementMrg;
		}
	}

	private static class StockOutQtyRow {
		String cocode;
		int irg;
		int storageQty;
		int consignmentQty;

		StockOutQtyRow(String p_cocode,int p_irg) {
			cocode = p_cocode;
			irg = p_irg;
		}
	}

	public BiResultStockOutChg(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList,
			String p_whereStr,SessionHelper p_sh) throws CellException {
		super(p_parent,p_view,p_su,p_tabList,p_whereStr,p_sh);
	}

	private Date getValidatedStockOutDate() throws Exception {
		Date stockOutDate = getCellDate("storh_date");
		if(stockOutDate == null) throw new Exception("Stock-out charge date is required");

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(stockOutDate);
		if(calendar.get(Calendar.DAY_OF_MONTH) != 1) {
			throw new Exception("Stock-out charge date must be the first day of a month");
		}
		return(stockOutDate);
	}

	private boolean useStockMovementGrouping(Date stockOutDate) {
		return(!stockOutDate.before(STOCK_OUT_2208_DATE));
	}

	private String oldDetailKey(String cocode,int org,int irg) {
		return(cocode.trim()+"\u0000"+org+"\u0000"+irg);
	}

	private ArrayList<StockOutDetailRow> calculateStockOutDetailRows(Date stockOutDate,JxForm jxf) throws Exception {
		Date fromDate = DateUtil.monthStart(DateUtil.prevmonth(stockOutDate,1));
		Date toDate = DateUtil.monthEnd(fromDate);
		boolean movementGrouping = useStockMovementGrouping(stockOutDate);
		String sql;
		if(movementGrouping) {
			sql = "select or_cocode,stm_date,st_msize3,stbd_name,or_org,st_irg,st_msize2,"+
					"stmd_loc,stmd_mrg,sum(-stmd_qty) stockout_qty "+
					"from stmov,stmovd,stock,orders,st_brand "+
					"where stm_type = 'DN' "+
					"and stm_date between ? and ? "+
					"and stm_mrg = stmd_mrg "+
					"and stmd_loc in ('WH01','STOR') "+
					"and stmd_bin <> '' "+
					"and stmd_tdtype = '" + Erpv4Config.getStmd_SO(getSessionHelper()) + "' "+
					"and stmd_qty <> 0 "+
					"and or_org = stmd_org "+
					"and or_cocode <> 'WINECAVE' "+
					"and st_irg = stmd_irg "+
					"and stbd_code = st_mbrand "+
					"group by or_cocode,stm_date,st_msize3,stbd_name,or_org,st_irg,st_msize2,"+
					"stmd_loc,stmd_mrg "+
					"order by or_cocode,stm_date desc,st_msize3,stbd_name,or_org,st_irg,st_msize2,"+
					"stmd_loc,stmd_mrg";
		} else {
			sql = "select or_cocode,or_date,st_msize3,stbd_name,or_org,st_irg,st_msize2,"+
					"stmd_loc,sum(-stmd_qty) stockout_qty "+
					"from stmov,stmovd,stock,orders,st_brand "+
					"where stm_type = 'DN' "+
					"and stm_date between ? and ? "+
					"and stm_mrg = stmd_mrg "+
					"and stmd_loc in ('WH01','STOR') "+
					"and stmd_bin <> '' "+
					"and stmd_tdtype = '" + Erpv4Config.getStmd_SO(getSessionHelper()) + "' "+
					"and stmd_qty <> 0 "+
					"and or_org = stmd_org "+
					"and or_cocode <> 'WINECAVE' "+
					"and st_irg = stmd_irg "+
					"and stbd_code = st_mbrand "+
					"group by or_cocode,or_date,st_msize3,stbd_name,or_org,st_irg,st_msize2,stmd_loc "+
					"order by or_cocode,or_date desc,st_msize3,stbd_name,or_org,st_irg,st_msize2,stmd_loc";
		}

		TableRec sourceRows = getSelectUtil().getQueryResult(
				sql,new Wherecl().appendArgument(fromDate).appendArgument(toDate));
		ArrayList<StockOutDetailRow> generatedRows = new ArrayList<StockOutDetailRow>();
		Map<String,StockOutDetailRow> oldRowsByKey =
				new LinkedHashMap<String,StockOutDetailRow>();

		for(int i=0;i<sourceRows.getRecordCount();i++) {
			sourceRows.setRecPointer(i);
			int qty = (int) sourceRows.getFieldDouble("stockout_qty");
			if(qty <= 0) continue;

			String cocode = sourceRows.getFieldString("or_cocode").trim();
			int org = sourceRows.getFieldInt("or_org");
			int irg = sourceRows.getFieldInt("st_irg");
			int pkg = (int) sourceRows.getFieldDouble("st_msize2");
			StockOutDetailRow generated;
			if(movementGrouping) {
				generated = new StockOutDetailRow(
						cocode,0,irg,pkg,sourceRows.getFieldInt("stmd_mrg"));
				generatedRows.add(generated);
			} else {
				String key = oldDetailKey(cocode,org,irg);
				generated = oldRowsByKey.get(key);
				if(generated == null) {
					generated = new StockOutDetailRow(cocode,org,irg,pkg,0);
					oldRowsByKey.put(key,generated);
					generatedRows.add(generated);
				}
			}

			String location = sourceRows.getFieldString("stmd_loc").trim();
			if(location.equals("STOR")) generated.storageQty += qty;
			else if(location.equals("WH01")) generated.consignmentQty += qty;
		}
		return(generatedRows);
	}

	private void markDetailRowsDeleted(BiResult result,JxField sv) throws CellException {
		for(int i=0;i<result.getRowCount();i++) {
			result.markDelete(result.getTrStatObj(i),true);
			if(sv != null) {
				sv.gridSetDataFormat(-1,i,"add_deleted");
			}
		}
	}

	private Map<Integer,Integer> indexDetailRowsBySequence(BiResult result) {
		Map<Integer,Integer> rowIndexBySequence = new LinkedHashMap<Integer,Integer>();
		for(int i=0;i<result.getRowCount();i++) {
			int sequence = result.getRowCollectionV(i).getCellInt("stord_idx");
			if(!rowIndexBySequence.containsKey(sequence)) rowIndexBySequence.put(sequence,i);
		}
		return(rowIndexBySequence);
	}

	private BiCellCollection getDetailRow(BiResult result,Map<Integer,Integer> rowsBySequence,
			int sequence, JxField sv) throws Exception {
		Integer rowIndex = rowsBySequence.get(sequence);
		if(rowIndex != null) {
			Object trStat = result.getTrStatObj(rowIndex);
			if(result.isMarkedDelete(trStat)) result.markDelete(trStat,false);
			return(result.getRowCollectionV(rowIndex));
		}

		BiCellCollection row = result.newRowCollection();
		ReturnMsg rtn = result.addSubRecord(row,result.getRowCount(),"");
		if(rtn == null || !rtn.getStatus()) {
			throw new Exception(rtn == null
					? "Unable to add row to "+STOCK_OUT_DETAIL_LINK : rtn.getMsg());
		}
		if(sv != null) {
			sv.addItemToList(rtn.getData(), result.getRowCount()-1);
		}
		rowsBySequence.put(sequence,result.getRowCount()-1);
		return(row);
	}

	private double storageutil_cal_volume(int irg,String unit,double qty) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value value = rpc.callSegment(
				"storageutil_cal_volume",
				new VectorUtil().addElement(irg).addElement(unit).addElement(qty).toVector());
		return(value.toDouble());
	}

	private double storageutil_cal_charge(
			String cocode,double uprice,double volume,Date stockOutDate) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value value = rpc.callSegment(
				"storageutil_cal_charge",
				new VectorUtil()
				.addElement(cocode)
				.addElement(uprice)
				.addElement(volume)
				.addElement(0.0)
				.addElement(0.0)
				.addElement(stockOutDate)
				.addElement(DateUtil.monthEnd(stockOutDate))
				.toVector());
		return(value.toDouble());
	}

	private double getVendorStockOutCharge(String cocode) throws Exception {
		TableRec result = getSelectUtil().getQueryResult(
				"select vd_stouchg from vendor where vd_vcode = ?",
				new Wherecl().appendArgument(cocode));
		if(result.getRecordCount() <= 0) return(0.0);
		result.setRecPointer(0);
		return(result.getFieldDouble("vd_stouchg"));
	}

	private String stockOutQtyKey(String cocode,int irg) {
		return(cocode.trim()+"\u0000"+irg);
	}

	private ArrayList<StockOutQtyRow> aggregateStockOutQty(BiResult detailResult) {
		Map<String,StockOutQtyRow> qtyByKey = new LinkedHashMap<String,StockOutQtyRow>();
		for(int i=0;i<detailResult.getRowCount();i++) {
			if(detailResult.isMarkedDelete(detailResult.getTrStatObj(i))) continue;
			BiCellCollection detail = detailResult.getRowCollectionV(i);
			String cocode = detail.getCellString("stord_cocode").trim();
			int irg = detail.getCellInt("stord_irg");
			String key = stockOutQtyKey(cocode,irg);
			StockOutQtyRow qty = qtyByKey.get(key);
			if(qty == null) {
				qty = new StockOutQtyRow(cocode,irg);
				qtyByKey.put(key,qty);
			}
			qty.storageQty += detail.getCellInt("stord_sqty");
			qty.consignmentQty += detail.getCellInt("stord_cqty");
		}
		return(new ArrayList<StockOutQtyRow>(qtyByKey.values()));
	}

	private Map<String,Integer> prepareChargeRows(BiResult chargeResult,JxField sv) throws CellException {
		Map<String,Integer> rowIndexByCustomer = new LinkedHashMap<String,Integer>();
		for(int i=0;i<chargeResult.getRowCount();i++) {
			BiCellCollection row = chargeResult.getRowCollectionV(i);
			row.getCell("stmp_svol").set(0);
			row.getCell("stmp_cvol").set(0);
			row.getCell("stmp_sno").set("");
			chargeResult.markDelete(chargeResult.getTrStatObj(i),true);
			if(sv != null) {
				sv.gridSetDataFormat(-1,i,"add_deleted");
			}
			String cocode = row.getCellString("stmp_cocode").trim();
			if(!rowIndexByCustomer.containsKey(cocode)) rowIndexByCustomer.put(cocode,i);
		}
		return(rowIndexByCustomer);
	}

	private BiCellCollection getChargeRow(BiResult result,Map<String,Integer> rowsByCustomer,
			String cocode,JxField sv) throws Exception {
		Integer rowIndex = rowsByCustomer.get(cocode);
		if(rowIndex != null) {
			Object trStat = result.getTrStatObj(rowIndex);
			if(result.isMarkedDelete(trStat)) result.markDelete(trStat,false);
			return(result.getRowCollectionV(rowIndex));
		}

		BiCellCollection row = result.newRowCollection();
		row.getCell("stmp_cocode").set(cocode);
		ReturnMsg rtn = result.addSubRecord(row,result.getRowCount(),"");
		if(sv != null) {
			sv.addItemToList(rtn.getData(), result.getRowCount()-1);
		}
		if(rtn == null || !rtn.getStatus()) {
			throw new Exception(rtn == null
					? "Unable to add row to "+STOCK_OUT_CHARGE_LINK : rtn.getMsg());
		}
		rowsByCustomer.put(cocode,result.getRowCount()-1);
		return(row);
	}

	private double getChargeVolume(BiCellCollection row) {
		String flag = row.getCellString("stmp_flg");
		if(flag.equals("Storg.")) return(row.getCellDouble("stmp_svol"));
		if(flag.equals("Consg.")) return(row.getCellDouble("stmp_cvol"));
		return(row.getCellDouble("stmp_svol")+row.getCellDouble("stmp_cvol"));
	}

	private void compareStockOutDetailInt(ArrayList<String> differences,String rowKey,
			String field,int expected,int actual) {
		if(expected != actual) {
			differences.add(rowKey+" "+field+": expected="+expected+", actual="+actual);
		}
	}

	private void compareStockOutDetailString(ArrayList<String> differences,String rowKey,
			String field,String expected,String actual) {
		if(!expected.equals(actual)) {
			differences.add(rowKey+" "+field+": expected="+expected+", actual="+actual);
		}
	}

	/**
	 * Recalculates StockOut detail rows in memory and compares them with the
	 * current graphql.StorageDet rows. No BiResult is modified.
	 */
	public ArrayList<String> verify_only() throws Exception {
		Date stockOutDate = getValidatedStockOutDate();
		ArrayList<StockOutDetailRow> expectedRows = calculateStockOutDetailRows(stockOutDate,null);
		Vector<BiCellCollection> actualRows = getSubLink(STOCK_OUT_DETAIL_LINK).getRowCollectionList();
		Map<Integer,Integer> actualIndexBySequence = new LinkedHashMap<Integer,Integer>();
		boolean[] matchedActualRows = new boolean[actualRows.size()];
		ArrayList<String> differences = new ArrayList<String>();

		for(int i=0;i<actualRows.size();i++) {
			int sequence = actualRows.get(i).getCellInt("stord_idx");
			if(!actualIndexBySequence.containsKey(sequence)) {
				actualIndexBySequence.put(sequence,i);
			}
		}

		int expectedSequence = 1;
		for(StockOutDetailRow expected : expectedRows) {
			String rowKey = "idx="+expectedSequence;
			Integer actualIndex = actualIndexBySequence.get(expectedSequence);
			if(actualIndex == null) {
				differences.add(rowKey+": missing from "+STOCK_OUT_DETAIL_LINK);
				expectedSequence++;
				continue;
			}

			BiCellCollection actual = actualRows.get(actualIndex);
			matchedActualRows[actualIndex] = true;
			compareStockOutDetailInt(differences,rowKey,"stord_mrg",
					getCellInt("storh_mrg"),actual.getCellInt("stord_mrg"));
			compareStockOutDetailString(differences,rowKey,"stord_cocode",
					expected.cocode,actual.getCellString("stord_cocode").trim());
			compareStockOutDetailInt(differences,rowKey,"stord_org",
					expected.org,actual.getCellInt("stord_org"));
			compareStockOutDetailInt(differences,rowKey,"stord_irg",
					expected.irg,actual.getCellInt("stord_irg"));
			compareStockOutDetailInt(differences,rowKey,"stord_pkg",
					expected.pkg,actual.getCellInt("stord_pkg"));
			compareStockOutDetailInt(differences,rowKey,"stord_sqty",
					expected.storageQty,actual.getCellInt("stord_sqty"));
			compareStockOutDetailInt(differences,rowKey,"stord_cqty",
					expected.consignmentQty,actual.getCellInt("stord_cqty"));
			compareStockOutDetailInt(differences,rowKey,"stord_stmrg",
					expected.stockMovementMrg,actual.getCellInt("stord_stmrg"));
			expectedSequence++;
			if(differences.size() > 30) break;
		}

		for(int i=0;i<actualRows.size();i++) {
			if(differences.size() > 30) break;
			if(!matchedActualRows[i]) {
				BiCellCollection actual = actualRows.get(i);
				differences.add("idx="+actual.getCellInt("stord_idx")
						+": unexpected row in "+STOCK_OUT_DETAIL_LINK);
			}
		}
		return(differences);
	}

	public void regen_stockoutdet(JxForm jxf) throws Exception {
		Date stockOutDate = getValidatedStockOutDate();
		ArrayList<StockOutDetailRow> generatedRows = calculateStockOutDetailRows(stockOutDate,jxf);
		BiResult detailResult = getSubLink(STOCK_OUT_DETAIL_LINK);
		JxField sv = null;
		if(jxf != null) sv = jxf.jxAdd("list_"+ getSubLink(STOCK_OUT_DETAIL_LINK).getView().getName().replace(".", "_"));
		markDetailRowsDeleted(detailResult,sv);
		Map<Integer,Integer> existingRowsBySequence = indexDetailRowsBySequence(detailResult);

		int sequence = 1;
		for(StockOutDetailRow generated : generatedRows) {
			BiCellCollection row = getDetailRow(detailResult,existingRowsBySequence,sequence,sv);
			row.getCell("stord_mrg").set(getCellInt("storh_mrg"));
			row.getCell("stord_idx").set(sequence++);
			row.getCell("stord_cocode").set(generated.cocode);
			row.getCell("stord_org").set(generated.org);
			row.getCell("stord_irg").set(generated.irg);
			row.getCell("stord_pkg").set(generated.pkg);
			row.getCell("stord_sqty").set(generated.storageQty);
			row.getCell("stord_cqty").set(generated.consignmentQty);
			row.getCell("stord_stmrg").set(generated.stockMovementMrg);
			
			if(sequence % 100 == 0) {
				UniLog.log("doKeepAlive regen_stockoutdet" + inBeginWork());
			}
		}
	}

	public void cal_stockout_charge(JxForm jxf) throws Exception {
		Date stockOutDate = getValidatedStockOutDate();
		BiResult detailResult = getSubLink(STOCK_OUT_DETAIL_LINK);
		BiResult chargeResult = getSubLink(STOCK_OUT_CHARGE_LINK);
		JxField sv = null;
		if(jxf != null) sv = jxf.jxAdd("list_"+ getSubLink(STOCK_OUT_CHARGE_LINK).getView().getName().replace(".", "_"));
		Map<String,Integer> chargeRowsByCustomer = prepareChargeRows(chargeResult,sv);
		boolean applyMinimumVolume = useStockMovementGrouping(stockOutDate);
		for(StockOutQtyRow qty : aggregateStockOutQty(detailResult)) {
			int storageVolume = (int) storageutil_cal_volume(qty.irg,"Btl",qty.storageQty);
			int consignmentVolume = (int) storageutil_cal_volume(qty.irg,"Btl",qty.consignmentQty);
			if(applyMinimumVolume && storageVolume+consignmentVolume > 0) {
				storageVolume = (int) (Math.ceil(
						(storageVolume+consignmentVolume)/(double) MINIMUM_VOLUME_UNIT)
						*MINIMUM_VOLUME_UNIT-consignmentVolume);
			}

			BiCellCollection charge = getChargeRow(
					chargeResult,chargeRowsByCustomer,qty.cocode,sv);
			charge.getCell("stmp_svol").set(
					charge.getCellInt("stmp_svol")+storageVolume);
			charge.getCell("stmp_cvol").set(
					charge.getCellInt("stmp_cvol")+consignmentVolume);
		}

		for(int i=0;i<chargeResult.getRowCount();i++) {
			Object trStat = chargeResult.getTrStatObj(i);
			if(chargeResult.isMarkedDelete(trStat)) continue;
			BiCellCollection charge = chargeResult.getRowCollectionV(i);
			if(charge.getCellInt("stmp_svol") == 0 && charge.getCellInt("stmp_cvol") == 0) {
				chargeResult.markDelete(trStat,true);
				continue;
			}

			charge.getCell("stmp_mrg").set(getCellInt("storh_mrg"));
			charge.getCell("stmp_ptype").set("OM");
			if(charge.getCellString("stmp_flg").equals("")) {
				charge.getCell("stmp_flg").set("Both");
				charge.getCell("stmp_uprice").set(
						getVendorStockOutCharge(charge.getCellString("stmp_cocode")));
			}

			double amount = storageutil_cal_charge(
					charge.getCellString("stmp_cocode"),
					charge.getCellDouble("stmp_uprice"),
					getChargeVolume(charge),stockOutDate);
			charge.getCell("stmp_amount").set(amount);
			charge.getCell("stmp_net").set(amount);
		}
	}
}
