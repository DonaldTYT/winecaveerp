package com.uniinformation.bicore.wc;

import java.util.ArrayList;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockOutEx extends BiResultStockOut {
	private static final double VERIFY_TOLERANCE = 0.000001;

	private static class ConsignmentPoVerificationRow {
		String cocode;
		int mrg;
		String ptype;
		double cbtl;
		double amount;
		double net;

		ConsignmentPoVerificationRow(String p_cocode) {
			cocode = p_cocode;
		}
	}

	public BiResultStockOutEx(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	private void clearConsignmentPoRows(BiResult result) throws CellException {
		for(int i=0;i<result.getRowCount();i++) {
			BiCellCollection row = result.getRowCollectionV(i);
			row.getCell("stmp_cbtl").set(0.0);
			row.getCell("stmp_amount").set(0.0);
			result.markDelete(result.getTrStatObj(i),true);
		}
	}

	private BiCellCollection getConsignmentPoRow(BiResult result,String cocode) throws Exception {
		for(int i=0;i<result.getRowCount();i++) {
			BiCellCollection row = result.getRowCollectionV(i);
			if(row.getCellString("stmp_cocode").equals(cocode)) {
				Object trStat = result.getTrStatObj(i);
				if(result.isMarkedDelete(trStat)) result.markDelete(trStat,false);
				return(row);
			}
		}

		BiCellCollection row = result.newRowCollection();
		row.getCell("stmp_cocode").set(cocode);
		ReturnMsg rtn = result.addSubRecord(row,result.getRowCount(),"");
		if(rtn == null || !rtn.getStatus()) {
			throw new Exception(rtn == null
					? "Unable to add row to wc.StmpostExt" : rtn.getMsg());
		}
		return(row);
	}

	private ConsignmentPoVerificationRow getConsignmentPoVerificationRow(
			ArrayList<ConsignmentPoVerificationRow> rows,String cocode) {
		for(ConsignmentPoVerificationRow row : rows) {
			if(row.cocode.equals(cocode)) return(row);
		}
		ConsignmentPoVerificationRow row = new ConsignmentPoVerificationRow(cocode);
		rows.add(row);
		return(row);
	}

	private BiCellCollection findConsignmentPoActualRow(
			Vector<BiCellCollection> rows,String cocode,boolean[] matchedRows) {
		for(int i=0;i<rows.size();i++) {
			if(matchedRows != null && matchedRows[i]) continue;
			BiCellCollection row = rows.get(i);
			if(row.getCellString("stmp_cocode").equals(cocode)) return(row);
		}
		return(null);
	}

	private void compareConsignmentPoDouble(ArrayList<String> differences,String rowKey,
			String field,double expected,double actual) {
		if(Double.isNaN(expected) != Double.isNaN(actual)
				|| (!Double.isNaN(expected) && Math.abs(expected-actual) > VERIFY_TOLERANCE)) {
			differences.add(rowKey+" "+field+": expected="+expected+", actual="+actual);
		}
	}

	/**
	 * Recalculates consignment PO values in a temporary list and compares them
	 * with the current wc.StmpostExt rows. Neither BiResult is modified.
	 */
	public ArrayList<String> verify_only() throws Exception {
		ArrayList<ConsignmentPoVerificationRow> expectedRows =
				new ArrayList<ConsignmentPoVerificationRow>();
		Vector<BiCellCollection> omRows = getSubLink("wc.StmdMoSi").getRowCollectionList();
		Vector<BiCellCollection> actualRows = getSubLink("wc.StmpostExt").getRowCollectionList();

		for(BiCellCollection omRow : omRows) {
			String cocode = omRow.getCellString("or_cocode");
			if(cocode.equals("WINECAVE")) continue;
			double bqty = -omRow.getCellDouble("stmd_qty");
			ConsignmentPoVerificationRow expected =
					getConsignmentPoVerificationRow(expectedRows,cocode);
			expected.cbtl += bqty;
			expected.amount += omRow.getCellDouble("stmd_fref1") * bqty;
		}

		for(ConsignmentPoVerificationRow expected : expectedRows) {
			expected.mrg = getCellInt("stm_mrg");
			expected.ptype = "CP";
			expected.net = expected.amount;
		}

		ArrayList<String> differences = new ArrayList<String>();
		boolean[] matchedActualRows = new boolean[actualRows.size()];
		for(ConsignmentPoVerificationRow expected : expectedRows) {
			if(expected.cbtl == 0.0) continue;
			String rowKey = "cocode="+expected.cocode;
			BiCellCollection actual = findConsignmentPoActualRow(
					actualRows,expected.cocode,matchedActualRows);
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
			if(!actual.getCellString("stmp_ptype").equals(expected.ptype)) {
				differences.add(rowKey+" stmp_ptype: expected="+expected.ptype
						+", actual="+actual.getCellString("stmp_ptype"));
			}
			compareConsignmentPoDouble(differences,rowKey,"stmp_cbtl",
					expected.cbtl,actual.getCellDouble("stmp_cbtl"));
			compareConsignmentPoDouble(differences,rowKey,"stmp_amount",
					expected.amount,actual.getCellDouble("stmp_amount"));
			compareConsignmentPoDouble(differences,rowKey,"stmp_net",
					expected.net,actual.getCellDouble("stmp_net"));
		}

		for(int i=0;i<actualRows.size();i++) {
			if(!matchedActualRows[i]) {
				BiCellCollection actual = actualRows.get(i);
				differences.add("cocode="+actual.getCellString("stmp_cocode")
						+": unexpected row in wc.StmpostExt");
			}
		}
		return(differences);
	}

	/**
	 * Recalculates consignment PO rows in wc.StmpostExt. Database persistence is
	 * handled by the BiResult lifecycle; this method performs no direct SQL writes.
	 */
	public void cal_consignment_po() throws Exception {
		BiResult consignmentPoResult = getSubLink("wc.StmpostExt");
		Vector<BiCellCollection> omRows = getSubLink("wc.StmdMoSi").getRowCollectionList();
		clearConsignmentPoRows(consignmentPoResult);

		for(BiCellCollection omRow : omRows) {
			String cocode = omRow.getCellString("or_cocode");
			if(cocode.equals("WINECAVE")) continue;

			double bqty = -omRow.getCellDouble("stmd_qty");
			BiCellCollection poRow = getConsignmentPoRow(consignmentPoResult,cocode);
			poRow.getCell("stmp_cbtl").set(poRow.getCellDouble("stmp_cbtl") + bqty);
			poRow.getCell("stmp_amount").set(
					poRow.getCellDouble("stmp_amount")
					+ omRow.getCellDouble("stmd_fref1") * bqty
					);
		}

		for(int i=0;i<consignmentPoResult.getRowCount();i++) {
			Object trStat = consignmentPoResult.getTrStatObj(i);
			if(consignmentPoResult.isMarkedDelete(trStat)) continue;
			BiCellCollection row = consignmentPoResult.getRowCollectionV(i);
			if(row.getCellDouble("stmp_cbtl") == 0.0) {
				consignmentPoResult.markDelete(trStat,true);
				continue;
			}

			row.getCell("stmp_mrg").set(getCellInt("stm_mrg"));
			row.getCell("stmp_ptype").set("CP");
			row.getCell("stmp_net").set(row.getCellDouble("stmp_amount"));
		}
	}

}
