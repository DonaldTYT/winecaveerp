package com.uniinformation.dynamic.afs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.BiResultLedger.LedgerCostCalculator;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.GlBalanceCalculation;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsGlJnG2 extends BiResultErpv4{

	public BiResultAfsGlJnG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl;
		if(p_where != null) wcl = p_where; else wcl = new Wherecl();
		wcl.andRange("jn_xdate", getParent().getCellDate("sdate"), getParent().getCellDate("edate"));
		return(ht);
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap2() {
//		try {
//		for(int i=0;i<getRowCount();i++) {
//			BiCellCollection bc = getRowCollectionV(i);
//			bc.getCell("jn_beginbal").set(10000.0+i);
//			saveOneRecV(i);
//		}
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
		try {
		List<Object> accodes = getParent().getCurrentColumnValues("ca_ctrlano");
		List<Object> ccys = getParent().getCurrentColumnValues("ca_cid");
		double beginlBal = 0.0;
		HashMap<String,Double> beginBals = new HashMap<String,Double>();
		String cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
		for(int i=0;i<accodes.size();i++) {
			GlBalanceCalculation.BalanceAccumulatorPair acuPair = GlBalanceCalculation.getDaBalanceAccumulator(getSessionHelper(), cocode,(String) accodes.get(i),(String) ccys.get(i));
			if(acuPair != null) {
				beginlBal += acuPair.getLacu().getBalanceBegin(getParent().getCellDate("sdate"));
				double bal =0.0;
				if(beginBals.get((String) ccys.get(i)) != null) {
					bal = beginBals.get((String) ccys.get(i));
				}
				bal += acuPair.getCacu().getBalanceBegin(getParent().getCellDate("sdate"));
				beginBals.put((String) ccys.get(i), bal);
			}
//			beginlbal += GlBalanceCalculation.getDaBalanceAccumulator(p_sh, p_cocode, p_ano, p_ccy)
		}
		int balPos = getSelectFieldPosition( getColumnByLabel("jn_balance" ));
		int lbalPos = getSelectFieldPosition( getColumnByLabel("jn_lbalance" ));
		int amtPos = getSelectFieldPosition( getColumnByLabel("jn_amount" ));
		int lamtPos = getSelectFieldPosition( getColumnByLabel("jn_lamount" ));
		int ccyPos = getSelectFieldPosition( getColumnByLabel("jn_cid" ));
//		int ballPos = getSelectFieldPosition( getColumnByLabel("jn_lbalance" ));
		int n;
		n = getTableRecCount();
//		double beginBal = GlbalanceCalculation.
		for(int i=0;i<n;i++) {
//			BiCellCollection bc = getRowCollectionV(i);
//			bc.getCell("jn_beginbal").set(10000.0+i);
			beginlBal += (Double) resultTr.getField(lamtPos, i);
			saveOneObjectToResultTr(i,lbalPos,beginlBal);
			double bal = beginBals.get((String) resultTr.getField(ccyPos, i));
			bal += (Double) resultTr.getField(amtPos, i);
			beginBals.put((String) resultTr.getField(ccyPos, i),bal);
			saveOneObjectToResultTr(i,balPos,bal);
		}		
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		invalidateLoadCache();
		return(ReturnMsg.defaultOk);
	}
	
	
	@Override
	public String getColumnDisplayClass(ColumnCell p_cell) {
		
//		if(p_cell.getCellLabel().equals("jn_lbalance")) {
//			double d0 = getCellDouble("jn_lbalancex");
//			if(d0 == 0.0) return(null);
//			return("textInError");
//		}
		return(super.getColumnDisplayClass(p_cell));
	}
}
