package com.uniinformation.bicore.erpv4;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCrhAr extends BiResultErpv4 {
	
	CellValueAction cal_AmtFromDetails;
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		if(cal_Amt == null) cal_Amt = new CellValueAction() {
			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				real_calPayment_AmountX();
				// TODO Auto-generated method stub
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		};
//		p_col.getCell("crh_ljamount").addAction(cal_Amt);
//		if(cal_AmtFromDetails == null) cal_AmtFromDetails = new CellValueAction() {
//
//			@Override
//			public void cellAction_onchange(Cell p_value) throws CellException {
//				BiCellCollection bcol = ((ColumnCell) p_value).getCollection();
//				if(
//						(!bcol.getCellString("crh_module").equals("AR") ) &&
//						(!bcol.getCellString("crh_module").equals("AP") )
//				) return;
//				if(bcol.getCellString("crh_cid").equals("")) return;
//				double amt = (bcol.getCellDouble("crh_liamount") + bcol.getCellDouble("crh_ljamount") ) / bcol.getDouble("crh_setxrate");
//				if(bcol.getCellString("crh_module").equals("AR")) {
//					bcol.getCell("crh_gamount").set(-amt);
//				} else {
//					bcol.getCell("crh_gamount").set(amt);
//				}
//			}
//
//			@Override
//			public void cellAction_onfree() throws CellException {
//				// TODO Auto-generated method stub
//				
//			}
//			
//		};
//		
//		
//		p_col.getCell("crh_gamount").addAction(cal_AmtFromDetails);
//		p_col.getCell("crh_module").addAction(cal_AmtFromDetails);
//		p_col.getCell("crh_cid").addAction(cal_AmtFromDetails);
//		p_col.getCell("crh_setxrate").addAction(cal_AmtFromDetails);
//		p_col.getCell("crh_liamount").addAction(cal_AmtFromDetails);
//		p_col.getCell("crh_ljamount").addAction(cal_AmtFromDetails);
	}
	

	CellValueAction cal_Amt ;
//	CellValueAction cal_lAmt = new CellValueAction() {
//
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			real_calPayment_lAmount();
//			// TODO Auto-generated method stub
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	};
	public BiResultCrhAr(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {

		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	void real_calPayment_AmountX() throws CellException {
		UniLog.log("CrhAr calPaymentAmount");
		Hashtable<String,Double> sumAmtHash = new Hashtable<String,Double>();
		Hashtable<String,Double> sumlAmtHash = new Hashtable<String,Double>();
		if(!isActionEnabled()) return;
		BiResult sr = getSubLink("erpv4.CrdAr");
		if(sr != null) {
			Vector<BiCellCollection> v = sr.getRowCollectionList();
			for(BiCellCollection col : v) {
				String ccy = col.getCellString("crd_cid");
				if(!ccy.equals("")) {
					Double dd = sumAmtHash.get(ccy);
					if(dd == null) dd = 0.0;
					dd -= col.getDouble("crd_amount");
					sumAmtHash.put(ccy, dd);
					dd = sumlAmtHash.get(ccy);
					if(dd == null) dd = 0.0;
					dd -= col.getDouble("crd_lamount");
					sumlAmtHash.put(ccy, dd);
				}
			}
			String dbano = getCellString("crh_dbinputano");
			Object ccys[] = sumAmtHash.keySet().toArray();
			if(dbano.equals("")) {
				if(ccys.length > 1) {
				// multi currency settlement
					getCell("crh_cid").set(Erpv4Config.getBaseCcy(sh, Erpv4Config.getDefaultCoCode(sh)));
					getCell("crh_amount").set(sumlAmtHash.get(ccys[0])-getCellDouble("crh_ljamount"));
					getCell("crh_lamount").resetValue();
					getCell("crh_lamount").sync(sumlAmtHash.get(ccys[0])-getCellDouble("crh_ljamount"));
				} else if(ccys.length == 1){
					getCell("crh_cid").set(ccys[0]);
					getCell("crh_amount").set(sumAmtHash.get(ccys[0]));
//					getCell("crh_lamount").resetValue();
//					getCell("crh_lamount").sync(sumlAmtHash.get(ccys[0])-getCellDouble("crh_ljamount"));
				} else {
					getCell("crh_cid").set(Erpv4Config.getBaseCcy(sh, Erpv4Config.getDefaultCoCode(sh)));
					getCell("crh_amount").set(-getCellDouble("crh_ljamount"));
					getCell("crh_lamount").resetValue();
					getCell("crh_lamount").sync(-getCellDouble("crh_ljamount"));
				}
			}
		}
	}
//	void real_calPayment_lAmount() throws CellException {
//		UniLog.log("CrhAr calPaymentAmount");
//		if(!actionEnabled) return;
//		BiResult sr = getSubLink("erpv4.CrdAr");
//		if(sr != null) {
//			Vector<BiCellCollection> v = sr.getRowCollectionList();
//			double lamt = 0;
//			for(BiCellCollection col : v) {
//				lamt -= col.getDouble("crd_lamount");
//			}
//			getCell("crh_lamount").set(lamt);
//		}
//	}
	public void calPaymentAmountX() throws CellException {
		real_calPayment_AmountX();
//		real_calPayment_lAmount();
	}
	public void setCellActionCal_Amount(Cell p_cell) {
		p_cell.addAction(cal_Amt);
	}
//	public void setCellActionCal_lAmount(Cell p_cell) {
//		p_cell.addAction(cal_lAmt);
//	k}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			if(getCell("crh_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and crh_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
			}
			return(ht);
		} else return(ht);
	}
	
	
	@Override
	protected void setLookupItemList(TableRec lookupTableTr,ColumnCell colCell) throws Exception {
		if(!colCell.getCellLabel().equals("ca_ano")) {
			super.setLookupItemList(lookupTableTr, colCell);
			return;
		}
		Vector <Object> lookupValues = new Vector<Object>();
		Hashtable<Object,String> ht = new Hashtable<Object,String>();
		for(int j = 0;j<lookupTableTr.getRecordCount();j++) {
			lookupTableTr.setRecPointer(j);
			Object oo = lookupTableTr.getField(colCell.getBiColumn().getField().getName());
			lookupValues.add(oo);
			String listString = 
					lookupTableTr.getFieldString("ca_ano") + " " +
					lookupTableTr.getFieldString("ca_aname");
			ht.put(oo,listString);
		}
		Vector<Comparable> vv = new Vector<Comparable>();
		for(Object o : lookupValues) {
			vv.add((Comparable) o);
		}
		Collections.sort(vv);
		GipiNamedItemList prdList;
		prdList = new GipiNamedItemList();
		for(int i=0;i<vv.size();i++) {
			prdList.appendItem( vv.get(i), ht.get(vv.get(i)));
		}
		colCell.setItemPropertyInterface(prdList);
//		colCell.setItemList(vv);
		colCell.setCCObj("lookup_uparent_tr", lookupTableTr);
		colCell.setCCObj("lookup_uparent_values", lookupValues);
	}	
}
