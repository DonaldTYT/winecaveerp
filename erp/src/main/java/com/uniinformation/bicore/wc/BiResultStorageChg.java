package com.uniinformation.bicore.wc;

import java.util.Calendar;
import java.util.ArrayList;
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
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStorageChg extends BiResultErpv4 {
	private static final String STORAGE_DETAIL_LINK = "graphql.StorageDet";
	private static final String STORAGE_CHARGE_LINK = "graphql.StmpostExtSM";
	private static final int MINIMUM_STORAGE_CHARGE = 90;

	private static class StorageDetailRow {
		String cocode;
		int org;
		int irg;
		int pkg;
		int storageQty;
		int consignmentQty;

		StorageDetailRow(String p_cocode,int p_org,int p_irg,int p_pkg) {
			cocode = p_cocode;
			org = p_org;
			irg = p_irg;
			pkg = p_pkg;
		}
	}

	private ArrayList<StorageDetailRow> calculateStorageDetailRows(Date storageDate) throws Exception {
		TableRec sourceRows = getSelectUtil().getQueryResult(
				"select or_cocode,or_date,st_msize3,stbd_name,or_org,st_irg,st_msize2,stmd_loc,sum(stmd_qty) storage_qty " +
				"from stmov,stmovd,stock,orders,st_brand " +
				"where stm_date < ? " +
				"and stm_void <> 'Y' " +
				"and stmd_mrg = stm_mrg " +
				"and stmd_loc in ('STOR','WH01') " +
				"and or_org = stmd_org " +
				"and or_cocode <> 'WINECAVE' " +
				"and stmd_bin <> '' " +
				"and st_irg = stmd_irg " +
				"and stbd_code = st_mbrand " +
				"group by or_cocode,or_date,st_msize3,stbd_name,or_org,st_irg,st_msize2,stmd_loc " +
				"order by or_cocode,or_date desc,st_msize3,stbd_name,or_org,st_irg,st_msize2,stmd_loc" 
				,
				new Wherecl().appendArgument(storageDate)
				);

		Map<String,StorageDetailRow> generatedRows = new LinkedHashMap<String,StorageDetailRow>();
		for(int i=0;i<sourceRows.getRecordCount();i++) {
			sourceRows.setRecPointer(i);
			int qty = (int) sourceRows.getFieldDouble("storage_qty");
			if(qty <= 0) continue;

			String cocode = sourceRows.getFieldString("or_cocode").trim();
			int org = sourceRows.getFieldInt("or_org");
			int irg = sourceRows.getFieldInt("st_irg");
			String key = cocode + "\u0000" + org + "\u0000" + irg;
			StorageDetailRow generated = generatedRows.get(key);
			if(generated == null) {
				generated = new StorageDetailRow(
						cocode,org,irg,(int) sourceRows.getFieldDouble("st_msize2"));
				generatedRows.put(key,generated);
			}

			String location = sourceRows.getFieldString("stmd_loc").trim();
			if(location.equals("STOR")) generated.storageQty += qty;
			else if(location.equals("WH01")) generated.consignmentQty += qty;
		}
		return(new ArrayList<StorageDetailRow>(generatedRows.values()));
	}

	public BiResultStorageChg(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	private Date getValidatedStorageDate() throws Exception {
		Date storageDate = getCellDate("storh_date");
		if(storageDate == null) throw new Exception("Storage date is required");

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(storageDate);
		if(calendar.get(Calendar.DAY_OF_MONTH) != 1) {
			throw new Exception("Storage date must be the first day of a month");
		}
		return(storageDate);
	}

	private void markAllRowsDeleted(BiResult result) throws CellException {
		for(int i=0;i<result.getRowCount();i++) {
			result.markDelete(result.getTrStatObj(i),true);
		}
	}

	private String storageDetailKey(String cocode,int org,int irg) {
		return(cocode.trim() + "\u0000" + org + "\u0000" + irg);
	}

	private Map<String,Integer> indexStorageDetailRows(BiResult result) {
		Map<String,Integer> rowsByKey = new LinkedHashMap<String,Integer>();
		for(int i=0;i<result.getRowCount();i++) {
			BiCellCollection row = result.getRowCollectionV(i);
			String key = storageDetailKey(
					row.getCellString("stord_cocode"),
					row.getCellInt("stord_org"),
					row.getCellInt("stord_irg"));
			if(!rowsByKey.containsKey(key)) rowsByKey.put(key,i);
		}
		return(rowsByKey);
	}

	private BiCellCollection getStorageDetailRow(
			BiResult result,Map<String,Integer> rowsByKey,
			String cocode,int org,int irg) throws Exception {
		String key = storageDetailKey(cocode,org,irg);
		Integer rowIndex = rowsByKey.get(key);
		BiCellCollection row;
		if(rowIndex != null) {
			row = result.getRowCollectionV(rowIndex);
			Object trStat = result.getTrStatObj(rowIndex);
			if(result.isMarkedDelete(trStat)) result.markDelete(trStat,false);
			return(row);
		}

		row = result.newRowCollection();
		ReturnMsg rtn = result.addSubRecord(row,result.getRowCount(),"");
		if(rtn == null || !rtn.getStatus()) {
			throw new Exception(rtn == null
					? "Unable to add row to " + STORAGE_DETAIL_LINK : rtn.getMsg());
		}
		rowsByKey.put(key,result.getRowCount()-1);
		return(row);
	}

	private BiCellCollection getStorageChargeRow(BiResult result,String cocode) throws Exception {
		for(int i=0;i<result.getRowCount();i++) {
			BiCellCollection row = result.getRowCollectionV(i);
			if(row.getCellString("stmp_cocode").equals(cocode)) {
				Object trStat = result.getTrStatObj(i);
				if(result.isMarkedDelete(trStat)) {
					row.getCell("stmp_svol").set(0.0);
					row.getCell("stmp_cvol").set(0.0);
					result.markDelete(trStat,false);
				}
				return(row);
			}
		}

		BiCellCollection row = result.newRowCollection();
		row.getCell("stmp_cocode").set(cocode);
		ReturnMsg rtn = result.addSubRecord(row,result.getRowCount(),"");
		if(rtn == null || !rtn.getStatus()) {
			throw new Exception(rtn == null
					? "Unable to add row to " + STORAGE_CHARGE_LINK : rtn.getMsg());
		}
		return(row);
	}

	private double storageutil_cal_volume(int irg,String unit,double qty) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value value = rpc.callSegment(
				"storageutil_cal_volume",
				new VectorUtil().addElement(irg).addElement(unit).addElement(qty).toVector()
				);
		return(value.toDouble());
	}

	private double storageutil_cal_charge(
			String cocode,double uprice,double volume,Date storageDate) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value value = rpc.callSegment(
				"storageutil_cal_charge",
				new VectorUtil()
				.addElement(cocode)
				.addElement(uprice)
				.addElement(volume)
				.addElement(0.0)
				.addElement(0.0)
				.addElement(storageDate)
				.addElement(DateUtil.monthEnd(storageDate))
				.addElement(MINIMUM_STORAGE_CHARGE)
				.toVector()
				);
		return(value.toDouble());
	}

	private double getVendorStorageCharge(String cocode) throws Exception {
		TableRec result = getSelectUtil().getQueryResult(
				"select vd_storchg from vendor where vd_vcode = ?",
				new Wherecl().appendArgument(cocode)
				);
		if(result.getRecordCount() <= 0) return(0.0);
		result.setRecPointer(0);
		return(result.getFieldDouble("vd_storchg"));
	}

	private double getChargeVolume(BiCellCollection row) {
		String flag = row.getCellString("stmp_flg");
		if(flag.equals("Storg.")) return(row.getCellDouble("stmp_svol"));
		if(flag.equals("Consg.")) return(row.getCellDouble("stmp_cvol"));
		return(row.getCellDouble("stmp_svol") + row.getCellDouble("stmp_cvol"));
	}

	private void compareStorageDetailInt(ArrayList<String> differences,String rowKey,
			String field,int expected,int actual) {
		if(expected != actual) {
			differences.add(rowKey+" "+field+": expected="+expected+", actual="+actual);
		}
	}
	/**
	 * Recalculates storage-detail rows in memory and compares them with the
	 * current graphql.StorageDet rows. Neither the detail nor charge BiResult is modified.
	 */
	public ArrayList<String> regen_storagedet_verify_only() throws Exception {
		Date storageDate = getValidatedStorageDate();
		ArrayList<StorageDetailRow> expectedRows = calculateStorageDetailRows(storageDate);
		Vector<BiCellCollection> actualRows = getSubLink(STORAGE_DETAIL_LINK).getRowCollectionList();
		ArrayList<String> differences = new ArrayList<String>();
		boolean[] matchedActualRows = new boolean[actualRows.size()];
		Map<String,Integer> actualIndexByKey = new LinkedHashMap<String,Integer>();
		for(int i=0;i<actualRows.size();i++) {
			BiCellCollection actual = actualRows.get(i);
			String key = storageDetailKey(
					actual.getCellString("stord_cocode"),
					actual.getCellInt("stord_org"),
					actual.getCellInt("stord_irg"));
			if(!actualIndexByKey.containsKey(key)) actualIndexByKey.put(key,i);
		}
		for(StorageDetailRow expected : expectedRows) {
			String rowKey = "cocode="+expected.cocode
					+", org="+expected.org+", irg="+expected.irg;
			Integer actualIndex = actualIndexByKey.get(
					storageDetailKey(expected.cocode,expected.org,expected.irg));
			if(actualIndex == null) {
				differences.add(rowKey+": missing from "+STORAGE_DETAIL_LINK);
				continue;
			}
			BiCellCollection actual = actualRows.get(actualIndex);
			matchedActualRows[actualIndex] = true;
			compareStorageDetailInt(differences,rowKey,"stord_mrg",
					getCellInt("storh_mrg"),actual.getCellInt("stord_mrg"));
			compareStorageDetailInt(differences,rowKey,"stord_pkg",
					expected.pkg,actual.getCellInt("stord_pkg"));
			compareStorageDetailInt(differences,rowKey,"stord_sqty",
					expected.storageQty,actual.getCellInt("stord_sqty"));
			compareStorageDetailInt(differences,rowKey,"stord_cqty",
					expected.consignmentQty,actual.getCellInt("stord_cqty"));
			compareStorageDetailInt(differences,rowKey,"stord_stmrg",
					0,actual.getCellInt("stord_stmrg"));
		}

		for(int i=0;i<actualRows.size();i++) {
			if(!matchedActualRows[i]) {
				BiCellCollection actual = actualRows.get(i);
				differences.add("cocode="+actual.getCellString("stord_cocode")
						+", org="+actual.getCellInt("stord_org")
						+", irg="+actual.getCellInt("stord_irg")
						+": unexpected row in "+STORAGE_DETAIL_LINK);
			}
		}
		return(differences);
	}

	public void regen_storagedet() throws Exception {
		Date storageDate = getValidatedStorageDate();
		ArrayList<StorageDetailRow> generatedRows = calculateStorageDetailRows(storageDate);

		BiResult detailResult = getSubLink(STORAGE_DETAIL_LINK);
		markAllRowsDeleted(detailResult);
		Map<String,Integer> existingRowsByKey = indexStorageDetailRows(detailResult);
		int idx = 1;
		for(StorageDetailRow generated : generatedRows) {
			BiCellCollection row = getStorageDetailRow(
					detailResult,existingRowsByKey,
					generated.cocode,generated.org,generated.irg);
			row.getCell("stord_mrg").set(getCellInt("storh_mrg"));
			row.getCell("stord_idx").set(idx++);
			row.getCell("stord_cocode").set(generated.cocode);
			row.getCell("stord_org").set(generated.org);
			row.getCell("stord_irg").set(generated.irg);
			row.getCell("stord_pkg").set(generated.pkg);
			row.getCell("stord_sqty").set(generated.storageQty);
			row.getCell("stord_cqty").set(generated.consignmentQty);
			row.getCell("stord_stmrg").set(0);
		}
	}

	public void cal_storage_charge() throws Exception {
		Date storageDate = getValidatedStorageDate();
		BiResult detailResult = getSubLink(STORAGE_DETAIL_LINK);
		BiResult chargeResult = getSubLink(STORAGE_CHARGE_LINK);

		markAllRowsDeleted(chargeResult);
		for(int i=0;i<detailResult.getRowCount();i++) {
			if(detailResult.isMarkedDelete(detailResult.getTrStatObj(i))) continue;
			BiCellCollection detail = detailResult.getRowCollectionV(i);
			String cocode = detail.getCellString("stord_cocode");
			BiCellCollection charge = getStorageChargeRow(chargeResult,cocode);

			double storageVolume = storageutil_cal_volume(
					detail.getCellInt("stord_irg"),"Btl",detail.getCellInt("stord_sqty"));
			double consignmentVolume = storageutil_cal_volume(
					detail.getCellInt("stord_irg"),"Btl",detail.getCellInt("stord_cqty"));
			charge.getCell("stmp_svol").set(charge.getCellDouble("stmp_svol") + storageVolume);
			charge.getCell("stmp_cvol").set(charge.getCellDouble("stmp_cvol") + consignmentVolume);
		}

		for(int i=0;i<chargeResult.getRowCount();i++) {
			Object trStat = chargeResult.getTrStatObj(i);
			if(chargeResult.isMarkedDelete(trStat)) continue;
			BiCellCollection charge = chargeResult.getRowCollectionV(i);
			if(charge.getCellDouble("stmp_svol") == 0.0
					&& charge.getCellDouble("stmp_cvol") == 0.0) {
				chargeResult.markDelete(trStat,true);
				continue;
			}

			charge.getCell("stmp_mrg").set(getCellInt("storh_mrg"));
			charge.getCell("stmp_ptype").set("SM");
			charge.getCell("stmp_sno").set("");
			charge.getCell("stmp_flg").set("Both");
			charge.getCell("stmp_uprice").set(
					getVendorStorageCharge(charge.getCellString("stmp_cocode")));
			double amount = storageutil_cal_charge(
					charge.getCellString("stmp_cocode"),
					charge.getCellDouble("stmp_uprice"),
					getChargeVolume(charge),
					storageDate);
			charge.getCell("stmp_amount").set(amount);
			charge.getCell("stmp_net").set(amount);
		}
	}

}
