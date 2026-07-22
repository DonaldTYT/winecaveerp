package com.uniinformation.bicore.wc;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
public class BiResultStockInEx extends BiResultStockIn {
	private static final Date STMPOEXT_IM_DATE = DateUtil.getDate("2022/08/01");
	private static final double VERIFY_TOLERANCE = 0.000001;

	private static class StorageChargeVerificationRow {
		String cocode;
		String ptype;
		int mrg;
		double svol;
		double cvol;
		String flg;
		double uprice;
		double amount;
		double net;

		StorageChargeVerificationRow(String p_cocode,String p_ptype) {
			cocode = p_cocode;
			ptype = p_ptype;
		}
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			cal_storage_charge();
		} catch (Exception ex) {
			UniLog.log(ex);;
			return(new ReturnMsg(false,ex.toString()));
		}
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			cal_storage_charge();
		} catch (Exception ex) {
			UniLog.log(ex);;
			return(new ReturnMsg(false,ex.toString()));
		}
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}	
	public BiResultStockInEx(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col,isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment("erpwc_afterAddUpdateMI",
					new VectorUtil().addElement(col.getCellInt("stm_mrg")).toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) return(new ReturnMsg(false,v == null ? "" : v.toString()));
		fetchOneSubLink(getCurrentCollection(),getSubLink("wc.StmpostExt"),null) ;
		return(rtn);
	}
	
	double storageutil_cal_volume(int irg,String eu,double qty) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment(
						"storageutil_cal_volume",
						new VectorUtil()
						.addElement(irg).addElement(eu).addElement(qty).toVector()
						);
		return(v.toDouble());
	}

	double storageutil_cal_charge(String cocode,double uprice,double volume,Date stmDate) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment(
						"storageutil_cal_charge",
						new VectorUtil()
						.addElement(cocode)
						.addElement(uprice)
						.addElement(volume)
						.addElement(0.0)
						.addElement(0.0)
						.addElement(stmDate)
						.addElement(DateUtil.monthEnd(stmDate))
						.toVector()
						);
		return(v.toDouble());
	}

	double getVendorStorageCharge(String cocode) throws Exception {
		TableRec tr = getSelectUtil().getQueryResult(
				"select vd_storchg from vendor where vd_vcode = ?",
				new Wherecl().appendArgument(cocode)
				);
		if(tr.getRecordCount() <= 0) return(0.0);
		tr.setRecPointer(0);
		return(tr.getFieldDouble("vd_storchg"));
	}
	
	
	void cleararray(String arrayName) throws CellException {
		BiResult sr = getSubLink(arrayName);
		int n = sr.getRowCount();
		for(int i=0;i<n;i++) {
			BiCellCollection row = sr.getRowCollectionV(i);
			row.getCell("stmp_svol").set(0.0);
			row.getCell("stmp_cvol").set(0.0);
			Object o = sr.getTrStatObj(i);
			sr.markDelete( o, true);	
		}
	}
	int sumintarray(String arrayName,int numkey,Object ... vals) throws Exception {
		BiResult sr = getSubLink(arrayName);
		int n = sr.getRowCount();
		double amount = ((Number) vals[numkey]).doubleValue();
		String valfd = (String) vals[(numkey+1) + numkey];
		int i = 0;
		for(;i<n;i++) {
			BiCellCollection row = sr.getRowCollectionV(i);
			boolean matched = true;
			for(int k=0;k<numkey;k++) {
				Object val = vals[k];
				String keyfd = (String) vals[(numkey+1) + k];
				if(!row.getCell(keyfd).equals(val)) {
					matched = false;
					break;
				}
			}
			if(matched) {
				Object o = sr.getTrStatObj(i);
				if(sr.isMarkedDelete(o)) {
					sr.markDelete( o, false);	
					row.getCell(valfd).set(amount);
				} else row.getCell(valfd).set(row.getCell(valfd).getDouble() + amount);
				break;
			}
		}		
		if(i >= n) {
			BiCellCollection col = sr.newRowCollection();
			for(int k=0;k<numkey;k++) {
				String keyfd = (String) vals[(numkey+1) + k];
				col.getCell(keyfd).set(vals[k]);
			}
			col.getCell(valfd).set(amount);
			ReturnMsg rtn = sr.addSubRecord(col, i,"");
			if(rtn == null || !rtn.getStatus()) {
				throw new Exception(rtn == null ? "Unable to add row to " + arrayName : rtn.getMsg());
			}
		}
		return(i);
	}

	private StorageChargeVerificationRow getVerificationRow(
			ArrayList<StorageChargeVerificationRow> rows,String cocode,String ptype) {
		for(StorageChargeVerificationRow row : rows) {
			if(row.cocode.equals(cocode) && row.ptype.equals(ptype)) return(row);
		}
		StorageChargeVerificationRow row = new StorageChargeVerificationRow(cocode,ptype);
		rows.add(row);
		return(row);
	}

	private BiCellCollection findStorageChargeRow(
			Vector<BiCellCollection> rows,String cocode,String ptype,boolean[] matchedRows) {
		for(int i=0;i<rows.size();i++) {
			if(matchedRows != null && matchedRows[i]) continue;
			BiCellCollection row = rows.get(i);
			if(row.getCellString("stmp_cocode").equals(cocode)
					&& row.getCellString("stmp_ptype").equals(ptype)) {
				return(row);
			}
		}
		return(null);
	}

	private void compareStorageChargeDouble(ArrayList<String> differences,String rowKey,
			String field,double expected,double actual) {
		if(Double.isNaN(expected) != Double.isNaN(actual)
				|| (!Double.isNaN(expected) && Math.abs(expected-actual) > VERIFY_TOLERANCE)) {
			differences.add(rowKey+" "+field+": expected="+expected+", actual="+actual);
		}
	}

	/**
	 * Recalculates storage charges in a temporary list and compares them with the
	 * current wc.StmpostExt rows. This method does not modify either BiResult.
	 */
	public ArrayList<String> verify_storage_charge() throws Exception {
		ArrayList<StorageChargeVerificationRow> expectedRows = new ArrayList<StorageChargeVerificationRow>();
		Vector<BiCellCollection> imRows = getSubLink("wc.StmdMi").getRowCollectionList();
		Vector<BiCellCollection> actualRows = getSubLink("wc.StmpostExt").getRowCollectionList();
		Date stmDate = getCellDate("stm_date");
		String fl = getCellString("stm_ref4");
		boolean includeImCharge = stmDate != null && !stmDate.before(STMPOEXT_IM_DATE);

		for(BiCellCollection imRow : imRows) {
			String cocode = imRow.getCellString("or_cocode");
			if(cocode.equals("WINECAVE")) continue;
			double storvol = storageutil_cal_volume(
					imRow.getCellInt("stmd_irg"),
					imRow.getCellString("stmd_entryunit"),
					imRow.getCellDouble("stmd_entryqty")
					);

			if(fl.equals("STOR")) {
				getVerificationRow(expectedRows,cocode,"ST").svol += storvol;
			} else if(fl.equals("WH01")) {
				getVerificationRow(expectedRows,cocode,"ST").cvol += storvol;
			}

			if(includeImCharge && (fl.equals("STOR") || fl.equals("WH01"))) {
				double imstorvol = storvol > 0 && storvol < 9000 ? 9000 : storvol;
				StorageChargeVerificationRow imRowExpected = getVerificationRow(expectedRows,cocode,"IM");
				if(fl.equals("STOR")) imRowExpected.svol += imstorvol;
				else if(fl.equals("WH01")) imRowExpected.cvol += imstorvol;
			}
		}

		for(StorageChargeVerificationRow expected : expectedRows) {
			expected.mrg = getCellInt("stm_mrg");
			BiCellCollection actual = findStorageChargeRow(actualRows,expected.cocode,expected.ptype,null);
			expected.flg = actual == null ? "" : actual.getCellString("stmp_flg");
			if(expected.flg.equals("")) {
				expected.flg = "Both";
				expected.uprice = expected.ptype.equals("ST")
						? getVendorStorageCharge(expected.cocode) : 10.0;
			} else {
				expected.uprice = actual.getCellDouble("stmp_uprice");
			}
			double volume = fl.equals("WH01") ? expected.cvol : expected.svol;
			expected.amount = storageutil_cal_charge(
					expected.cocode,expected.uprice,volume,stmDate);
			expected.net = expected.amount;
		}

		ArrayList<String> differences = new ArrayList<String>();
		boolean[] matchedActualRows = new boolean[actualRows.size()];
		for(StorageChargeVerificationRow expected : expectedRows) {
			String rowKey = "cocode="+expected.cocode+", ptype="+expected.ptype;
			BiCellCollection actual = findStorageChargeRow(
					actualRows,expected.cocode,expected.ptype,matchedActualRows);
			if(actual == null) {
				differences.add(rowKey+": missing from wc.StmpostExt");
				continue;
			}
			for(int i=0;i<actualRows.size();i++) {
				if(actualRows.get(i) == actual) {
					matchedActualRows[i] = true;
					break;
				}
			}

			if(actual.getCellInt("stmp_mrg") != expected.mrg) {
				differences.add(rowKey+" stmp_mrg: expected="+expected.mrg
						+", actual="+actual.getCellInt("stmp_mrg"));
			}
			if(!actual.getCellString("stmp_flg").equals(expected.flg)) {
				differences.add(rowKey+" stmp_flg: expected="+expected.flg
						+", actual="+actual.getCellString("stmp_flg"));
			}
			compareStorageChargeDouble(differences,rowKey,"stmp_svol",
					expected.svol,actual.getCellDouble("stmp_svol"));
			compareStorageChargeDouble(differences,rowKey,"stmp_cvol",
					expected.cvol,actual.getCellDouble("stmp_cvol"));
			compareStorageChargeDouble(differences,rowKey,"stmp_uprice",
					expected.uprice,actual.getCellDouble("stmp_uprice"));
			compareStorageChargeDouble(differences,rowKey,"stmp_amount",
					expected.amount,actual.getCellDouble("stmp_amount"));
			compareStorageChargeDouble(differences,rowKey,"stmp_net",
					expected.net,actual.getCellDouble("stmp_net"));
		}

		for(int i=0;i<actualRows.size();i++) {
			if(!matchedActualRows[i]) {
				BiCellCollection actual = actualRows.get(i);
				differences.add("cocode="+actual.getCellString("stmp_cocode")
						+", ptype="+actual.getCellString("stmp_ptype")
						+": unexpected row in wc.StmpostExt");
			}
		}
		return(differences);
	}
		
	
	public void cal_storage_charge() throws Exception {
		Vector<BiCellCollection> IM_arr = getSubLink("wc.StmdMi").getRowCollectionList();
		Date stmDate = getCellDate("stm_date");
		boolean includeImCharge = stmDate != null && !stmDate.before(STMPOEXT_IM_DATE);
		cleararray("wc.StmpostExt");
		for(int i=0;i<IM_arr.size();i++) {
			BiCellCollection bc = IM_arr.get(i);
			if(!bc.getCellString("or_cocode").equals("WINECAVE")) {
				double storvol = storageutil_cal_volume(bc.getCellInt("stmd_irg"),bc.getCellString("stmd_entryunit"),bc.getCellDouble("stmd_entryqty"));
				if(bc.testCell("stmd_nref4") != null) {
					bc.testCell("stmd_nref4").set(storvol);
				}
				double imstorvol;
				if(getCellString("stm_ref4").equals("STOR")) {
					sumintarray("wc.StmpostExt",2,bc.getCellString("or_cocode"),"ST",storvol,"stmp_cocode","stmp_ptype","stmp_svol");
					if(includeImCharge) {
						if(storvol > 0 &&  storvol < 9000) imstorvol = 9000 ; else imstorvol = storvol ;
						sumintarray("wc.StmpostExt",2,bc.getCellString("or_cocode"),"IM",imstorvol,"stmp_cocode","stmp_ptype","stmp_svol");
					}
				} else if(getCellString("stm_ref4").equals("WH01")) {
					sumintarray("wc.StmpostExt",2,bc.getCellString("or_cocode"),"ST",storvol,"stmp_cocode","stmp_ptype","stmp_cvol");
					if(includeImCharge) {
						if(storvol > 0 &&  storvol < 9000) imstorvol = 9000 ; else imstorvol = storvol ;
						sumintarray("wc.StmpostExt",2,bc.getCellString("or_cocode"),"IM",imstorvol,"stmp_cocode","stmp_ptype","stmp_cvol");
					}
				}
			}
		}

		BiResult storageChargeResult = getSubLink("wc.StmpostExt");
		Vector<BiCellCollection> storageChargeRows = storageChargeResult.getRowCollectionList();
		for(BiCellCollection row : storageChargeRows) {
			row.getCell("stmp_mrg").set(getCellInt("stm_mrg"));
			if(row.getCellString("stmp_flg").equals("")) {
				row.getCell("stmp_flg").set("Both");
				if(row.getCellString("stmp_ptype").equals("ST")) {
					row.getCell("stmp_uprice").set(getVendorStorageCharge(row.getCellString("stmp_cocode")));
				} else {
					row.getCell("stmp_uprice").set(10.0);
				}
			}

			double volume = getCellString("stm_ref4").equals("WH01")
					? row.getCellDouble("stmp_cvol")
					: row.getCellDouble("stmp_svol");
			double amount = storageutil_cal_charge(
					row.getCellString("stmp_cocode"),
					row.getCellDouble("stmp_uprice"),
					volume,
					stmDate
					);
			row.getCell("stmp_amount").set(amount);
			row.getCell("stmp_net").set(amount);
		}
	}
}
