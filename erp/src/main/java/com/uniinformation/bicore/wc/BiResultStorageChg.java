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
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
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
	private static final int VENDOR_QUERY_BATCH_SIZE = 500;
	private static final double VERIFY_TOLERANCE = 0.000001;
	private static final int MAX_VERIFY_DIFFERENCES = 30;

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

	private static class StorageChargeVolumeRow {
		String cocode;
		double storageVolume;
		double consignmentVolume;
		double unitPrice;
		double amount;

		StorageChargeVolumeRow(String p_cocode) {
			cocode = p_cocode;
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

	private Map<String,Integer> prepareStorageChargeRows(
			BiResult result,JxField chargeListField) throws CellException {
		Map<String,Integer> rowsByCustomer = new LinkedHashMap<String,Integer>();
		for(int i=0;i<result.getRowCount();i++) {
			BiCellCollection row = result.getRowCollectionV(i);
			row.getCell("stmp_svol").set(0.0);
			row.getCell("stmp_cvol").set(0.0);
			row.getCell("stmp_sno").set("");
			result.markDelete(result.getTrStatObj(i),true);
			if(chargeListField != null) {
				chargeListField.gridSetDataFormat(-1,i,"add_deleted");
			}
			String cocode = row.getCellString("stmp_cocode").trim();
			if(!rowsByCustomer.containsKey(cocode)) rowsByCustomer.put(cocode,i);
		}
		return(rowsByCustomer);
	}

	private BiCellCollection getStorageChargeRow(
			BiResult result,Map<String,Integer> rowsByCustomer,String cocode,
			JxField chargeListField) throws Exception {
		String customerCode = cocode.trim();
		Integer rowIndex = rowsByCustomer.get(customerCode);
		if(rowIndex != null) {
			Object trStat = result.getTrStatObj(rowIndex);
			if(result.isMarkedDelete(trStat)) result.markDelete(trStat,false);
			return(result.getRowCollectionV(rowIndex));
		}

		BiCellCollection row = result.newRowCollection();
		row.getCell("stmp_cocode").set(customerCode);
		ReturnMsg rtn = result.addSubRecord(row,result.getRowCount(),"");
		if(rtn == null || !rtn.getStatus()) {
			throw new Exception(rtn == null
					? "Unable to add row to " + STORAGE_CHARGE_LINK : rtn.getMsg());
		}
		if(chargeListField != null) {
			chargeListField.addItemToList(rtn.getData(),result.getRowCount()-1);
		}
		rowsByCustomer.put(customerCode,result.getRowCount()-1);
		return(row);
	}

	private double storageutil_cal_volume(BiCellCollection detail,String unit,double qty) {
		double volume = detail.getCellDouble("st_msize2") * qty;
		if(unit != null && unit.trim().equalsIgnoreCase("CASE")) {
			volume *= detail.getCellDouble("st_msize1");
		}
		return(volume);
	}

	private double storageutil_cal_charge(
			RpcClient rpc,String cocode,double uprice,double volume,
			Date storageDate,Date storageEndDate) {
		Value value = rpc.callSegment(
				"storageutil_cal_charge",
				new VectorUtil()
				.addElement(cocode)
				.addElement(uprice)
				.addElement(volume)
				.addElement(0.0)
				.addElement(0.0)
				.addElement(storageDate)
				.addElement(storageEndDate)
				.addElement(MINIMUM_STORAGE_CHARGE)
				.toVector()
				);
		return(value.toDouble());
	}

	private Map<String,Double> getVendorStorageCharges(ArrayList<String> customerCodes) throws Exception {
		Map<String,Double> chargesByCustomer = new LinkedHashMap<String,Double>();
		for(int batchStart=0;batchStart<customerCodes.size();batchStart+=VENDOR_QUERY_BATCH_SIZE) {
			int batchEnd = Math.min(customerCodes.size(),batchStart+VENDOR_QUERY_BATCH_SIZE);
			StringBuilder sql = new StringBuilder(
					"select vd_vcode,vd_storchg from vendor where vd_vcode in (");
			Wherecl arguments = new Wherecl();
			for(int i=batchStart;i<batchEnd;i++) {
				if(i > batchStart) sql.append(',');
				sql.append('?');
				arguments.appendArgument(customerCodes.get(i));
			}
			sql.append(')');
			TableRec result = getSelectUtil().getQueryResult(sql.toString(),arguments);
			for(int i=0;i<result.getRecordCount();i++) {
				result.setRecPointer(i);
				chargesByCustomer.put(
						result.getFieldString("vd_vcode").trim(),
						result.getFieldDouble("vd_storchg"));
			}
		}
		return(chargesByCustomer);
	}

	private boolean compareStorageChargeDouble(ArrayList<String> differences,String rowKey,
			String field,double expected,double actual) {
		if(Double.isNaN(expected) != Double.isNaN(actual)
				|| (!Double.isNaN(expected) && Math.abs(expected-actual) > VERIFY_TOLERANCE)) {
			differences.add(rowKey+" "+field+": expected="+expected+", actual="+actual);
		}
		return(differences.size() >= MAX_VERIFY_DIFFERENCES);
	}

	private boolean compareStorageChargeString(ArrayList<String> differences,String rowKey,
			String field,String expected,String actual) {
		if(!expected.equals(actual)) {
			differences.add(rowKey+" "+field+": expected="+expected+", actual="+actual);
		}
		return(differences.size() >= MAX_VERIFY_DIFFERENCES);
	}

	private ArrayList<String> verifyStorageChargeRows(
			BiResult chargeResult,Map<String,StorageChargeVolumeRow> expectedRows,
			int storageMrg) {
		ArrayList<String> differences = new ArrayList<String>();
		boolean[] matchedActualRows = new boolean[chargeResult.getRowCount()];
		Map<String,Integer> actualIndexByCustomer = new LinkedHashMap<String,Integer>();
		for(int i=0;i<chargeResult.getRowCount();i++) {
			if(chargeResult.isMarkedDelete(chargeResult.getTrStatObj(i))) {
				matchedActualRows[i] = true;
				continue;
			}
			String cocode = chargeResult.getRowCollectionV(i)
					.getCellString("stmp_cocode").trim();
			if(!actualIndexByCustomer.containsKey(cocode)) actualIndexByCustomer.put(cocode,i);
		}

		for(StorageChargeVolumeRow expected : expectedRows.values()) {
			if(expected.storageVolume == 0.0 && expected.consignmentVolume == 0.0) continue;
			String rowKey = "cocode="+expected.cocode;
			Integer actualIndex = actualIndexByCustomer.get(expected.cocode);
			if(actualIndex == null) {
				differences.add(rowKey+": missing from "+STORAGE_CHARGE_LINK);
				if(differences.size() >= MAX_VERIFY_DIFFERENCES) return(differences);
				continue;
			}
			matchedActualRows[actualIndex] = true;
			BiCellCollection actual = chargeResult.getRowCollectionV(actualIndex);
			if(actual.getCellInt("stmp_mrg") != storageMrg) {
				differences.add(rowKey+" stmp_mrg: expected="+storageMrg
						+", actual="+actual.getCellInt("stmp_mrg"));
				if(differences.size() >= MAX_VERIFY_DIFFERENCES) return(differences);
			}
			if(compareStorageChargeString(differences,rowKey,"stmp_ptype",
					"SM",actual.getCellString("stmp_ptype"))) return(differences);
			/*
			if(compareStorageChargeString(differences,rowKey,"stmp_sno",
					"",actual.getCellString("stmp_sno"))) return(differences);
					*/
			if(compareStorageChargeString(differences,rowKey,"stmp_flg",
					"Both",actual.getCellString("stmp_flg"))) return(differences);
			if(compareStorageChargeDouble(differences,rowKey,"stmp_svol",
					expected.storageVolume,actual.getCellDouble("stmp_svol"))) return(differences);
			if(compareStorageChargeDouble(differences,rowKey,"stmp_cvol",
					expected.consignmentVolume,actual.getCellDouble("stmp_cvol"))) return(differences);
			if(compareStorageChargeDouble(differences,rowKey,"stmp_uprice",
					expected.unitPrice,actual.getCellDouble("stmp_uprice"))) return(differences);
			if(compareStorageChargeDouble(differences,rowKey,"stmp_amount",
					expected.amount,actual.getCellDouble("stmp_amount"))) return(differences);
			if(compareStorageChargeDouble(differences,rowKey,"stmp_net",
					expected.amount,actual.getCellDouble("stmp_net"))) return(differences);
		}

		for(int i=0;i<chargeResult.getRowCount();i++) {
			if(matchedActualRows[i]) continue;
			BiCellCollection actual = chargeResult.getRowCollectionV(i);
			differences.add("cocode="+actual.getCellString("stmp_cocode").trim()
					+": unexpected row in "+STORAGE_CHARGE_LINK);
			if(differences.size() >= MAX_VERIFY_DIFFERENCES) return(differences);
		}
		return(differences);
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
			if(differences.size() > 30) break;
		}

		for(int i=0;i<actualRows.size();i++) {
			if(differences.size() > 30) break;
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

	/**
	 * Regenerates the storage details directly in the database. This bypasses
	 * BiResult row validation and change tracking, and is intended for an
	 * already-saved storage header.
	 */
	public void regen_storagedet_direct() throws Exception {
		Date storageDate = getValidatedStorageDate();
		ArrayList<StorageDetailRow> generatedRows = calculateStorageDetailRows(storageDate);
		SelectUtil selectUtil = getSelectUtil();
		boolean workStarted = false;
		try {
			if(!beginWork() || !inBeginWork()) {
				throw new Exception("Unable to begin storage-detail regeneration transaction");
			}
			workStarted = true;
			int storageMrg = getCellInt("storh_mrg");
			selectUtil.executeUpdate(
					"delete from storagedet where stord_mrg = ?",
					new Wherecl().appendArgument(storageMrg));

			int sequence = 1;
			for(StorageDetailRow generated : generatedRows) {
				selectUtil.executeUpdate(
						"insert into storagedet "
						+ "(stord_mrg,stord_idx,stord_cocode,stord_org,stord_irg,"
						+ "stord_pkg,stord_sqty,stord_cqty,stord_stmrg) "
						+ "values (?,?,?,?,?,?,?,?,?)",
						new Wherecl()
						.appendArgument(storageMrg)
						.appendArgument(sequence++)
						.appendArgument(generated.cocode)
						.appendArgument(generated.org)
						.appendArgument(generated.irg)
						.appendArgument(generated.pkg)
						.appendArgument(generated.storageQty)
						.appendArgument(generated.consignmentQty)
						.appendArgument(0));
			}
			if(!commitWork()) {
				throw new Exception("Unable to commit storage-detail regeneration transaction");
			}
			workStarted = false;
		} catch(Exception ex) {
			if(workStarted) rollbackWork();
			throw(ex);
		}
		Wherecl detailWhere = new Wherecl();
		detailWhere.andUniop("storagedet.stord_mrg","=",getCellInt("storh_mrg"));
		if(!fetchOneSubLink(getCurrentCollection(),getSubLink(STORAGE_DETAIL_LINK),detailWhere)) {
			throw new Exception("Unable to reload regenerated storage details");
		}
	}

	public void cal_storage_charge() throws Exception {
		cal_storage_charge(false,null);
	}

	public void cal_storage_charge(JxForm jxf) throws Exception {
		cal_storage_charge(false,jxf);
	}

	/**
	 * Calculates storage charges. In verification mode the expected values are
	 * compared with the current charge rows without modifying the charge BiResult.
	 *
	 * @return field-level differences; empty when updating or when verification matches
	 */
	public ArrayList<String> cal_storage_charge(boolean verifyOnly) throws Exception {
		return(cal_storage_charge(verifyOnly,null));
	}

	public ArrayList<String> cal_storage_charge(boolean verifyOnly,JxForm jxf) throws Exception {
		Date storageDate = getValidatedStorageDate();
		Date storageEndDate = DateUtil.monthEnd(storageDate);
		int storageMrg = getCellInt("storh_mrg");
		BiResult detailResult = getSubLink(STORAGE_DETAIL_LINK);
		BiResult chargeResult = getSubLink(STORAGE_CHARGE_LINK);
		RpcClient rpc = getSelectUtil().getRpcClient();
		Map<String,StorageChargeVolumeRow> volumesByCustomer =
				new LinkedHashMap<String,StorageChargeVolumeRow>();

		for(int i=0;i<detailResult.getRowCount();i++) {
			if(detailResult.isMarkedDelete(detailResult.getTrStatObj(i))) continue;
			if(!detailResult.loadOneRecV(i)) {
				throw new Exception("Unable to load storage detail row " + i);
			}
			BiCellCollection detail = detailResult.getCurrentCollection();
			String cocode = detail.getCellString("stord_cocode").trim();
			StorageChargeVolumeRow volumes = volumesByCustomer.get(cocode);
			if(volumes == null) {
				volumes = new StorageChargeVolumeRow(cocode);
				volumesByCustomer.put(cocode,volumes);
			}
			volumes.storageVolume += storageutil_cal_volume(
					detail,"Btl",detail.getCellInt("stord_sqty"));
			volumes.consignmentVolume += storageutil_cal_volume(
					detail,"Btl",detail.getCellInt("stord_cqty"));
		}

		ArrayList<String> chargedCustomerCodes = new ArrayList<String>();
		for(StorageChargeVolumeRow volumes : volumesByCustomer.values()) {
			if(volumes.storageVolume != 0.0 || volumes.consignmentVolume != 0.0) {
				chargedCustomerCodes.add(volumes.cocode);
			}
		}
		Map<String,Double> vendorCharges = getVendorStorageCharges(chargedCustomerCodes);

		for(StorageChargeVolumeRow volumes : volumesByCustomer.values()) {
			if(volumes.storageVolume == 0.0 && volumes.consignmentVolume == 0.0) continue;
			Double vendorCharge = vendorCharges.get(volumes.cocode);
			volumes.unitPrice = vendorCharge == null ? 0.0 : vendorCharge;
			volumes.amount = storageutil_cal_charge(
					rpc,volumes.cocode,volumes.unitPrice,
					volumes.storageVolume+volumes.consignmentVolume,
					storageDate,storageEndDate);
		}

		if(verifyOnly) {
			return(verifyStorageChargeRows(chargeResult,volumesByCustomer,storageMrg));
		}

		JxField chargeListField = null;
		if(jxf != null) {
			chargeListField = jxf.jxAdd(
					"list_"+chargeResult.getView().getName().replace(".","_"));
		}
		Map<String,Integer> chargeRowsByCustomer =
				prepareStorageChargeRows(chargeResult,chargeListField);
		for(StorageChargeVolumeRow volumes : volumesByCustomer.values()) {
			if(volumes.storageVolume == 0.0 && volumes.consignmentVolume == 0.0) continue;
			BiCellCollection charge = getStorageChargeRow(
					chargeResult,chargeRowsByCustomer,volumes.cocode,chargeListField);
			charge.getCell("stmp_mrg").set(storageMrg);
			charge.getCell("stmp_ptype").set("SM");
			charge.getCell("stmp_sno").set("");
			charge.getCell("stmp_flg").set("Both");
			charge.getCell("stmp_svol").set(volumes.storageVolume);
			charge.getCell("stmp_cvol").set(volumes.consignmentVolume);
			charge.getCell("stmp_uprice").set(volumes.unitPrice);
			charge.getCell("stmp_amount").set(volumes.amount);
			charge.getCell("stmp_net").set(volumes.amount);
		}
		return(new ArrayList<String>());
	}

}
