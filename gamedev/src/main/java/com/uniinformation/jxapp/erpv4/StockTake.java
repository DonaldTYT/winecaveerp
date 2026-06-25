package com.uniinformation.jxapp.erpv4;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;

public class StockTake extends JxZkBiBase {
//	Hashtable <String,Double> balanceHash;
//	Vector<String> iList;
	StockTakeUtil stu ;
	Set<String>locList = null;
	@Override
	public void onBarcode(String p_barcode) {
				try {

					if(curMode != JxZkBiBase.MODE_ADD) return;
					String s = p_barcode.replaceAll("\\s+", "");
					SelectUtil su = getBr().getSelectUtil();
					TableRec tr = su.getQueryResult("select * from stock where st_icode = ?", new Wherecl().appendArgument(s));
					if(tr.getRecordCount() <= 0) {
						tr = su.getQueryResult("select * from stock where st_barcode = ?", new Wherecl().appendArgument(s));
					}
					if(tr.getRecordCount() <= 0) throw new CellException("Record Not In Stock");
					getBr().getCell("stm_nref4").set(tr.getFieldInt("st_irg"));
					try {
						int sid = getSidByCode();
						if(sid > 0) {
							reloadCurrentBySid(sid, JxZkBiBase.MODE_UPDATE);
						} else {
							stu.getBalance(getBr().getSelectUtil(), getBr().getCellInt("stm_nref4"), getBr().getCell("stm_date").getDate(), getBr().getCellInt("stm_mrg"),locList);
							stu.syncBalance(getBr(),this);
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} catch (Exception ex) {
					UniLog.log("barcode invalid " + ex.toString() + "["+p_barcode+"]");
					if(ex instanceof CellException) ZkUtil.showMsg(ex.toString()); else {
						ZkUtil.showMsg("Barcode Invalid (0)");
						UniLog.log(ex);
					}
				}
					
	}
	
	int getSidByCode() throws Exception {
		SelectUtil su = getBr().getSelectUtil();
		TableRec tr = su.getQueryResult("select serial_id from stmov where stm_ref1 = '" + getBr().getCellString("stm_ref1")+"'");
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			return(tr.getFieldInt("serial_id"));
		}
		return(0);
	}
	
	
	@Override 
	public void afterBind() {
		super.afterBind();
		LOCK_RECORD_FOR_UPDATE = true;
		new JxFieldChange("st_icode st_barcode stm_date") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				// TODO Auto-generated method stub
				try {
						int sid = getSidByCode();
						if(sid > 0) {
							reloadCurrentBySid(sid, JxZkBiBase.MODE_UPDATE);
						} else {
							stu.getBalance(getBr().getSelectUtil(), getBr().getCellInt("stm_nref4"), getBr().getCell("stm_date").getDate(), getBr().getCellInt("stm_mrg"),locList);
							stu.syncBalance(getBr(),StockTake.this);
						}
				} catch (Exception ex) {
					UniLog.log(ex);
				}

				return true;
			}
			
		} ;
		new JxFieldAction("btReload") {
			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				try {
					stu.getBalance(getBr().getSelectUtil(), getBr().getCellInt("stm_nref4"), getBr().getCell("stm_date").getDate(), getBr().getCellInt("stm_mrg"),locList);
					stu.syncBalance(getBr(),StockTake.this);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		new JxFieldAction("btSyncDelta") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				try {
					stu.syncDelta(getBr(),StockTake.this);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		new JxFieldAction("btSyncBalance") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				try {
					StockTakeUtil stkutil = new StockTakeUtil(Erpv4Config.getString(getSessionHelper(), StockTakeUtil.STOCKTAKEFILTER));
					stkutil.init();
					stkutil.getBalance(getBr().getSelectUtil(),getBr().getCellInt("stm_nref4"),getBr().getCell("stm_date").getDate(),0,locList);
					stkutil.syncBalance(getBr(),StockTake.this);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		new JxFieldAction("btFixFifo") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				try {
					stu.fixFiFo(getBr(),StockTake.this,null);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};

		/*
		if(!getSessionHelper().isAdminUser()) {
			jxSetVisible("btSyncDelta",false);
			
		}
		*/
	}
	
	@Override 
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		if(Erpv4Config.isMultiCompany(p_br.getSessionHelper())) {
			if(Erpv4Config.isMultiStockLoc(p_br.getSessionHelper())) {
				locList = Erpv4Config.getLocationListByCompany(p_br.getSessionHelper(), Erpv4Config.getDefaultCoCode(p_br.getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_BYLCRG_EXCLUDE_TRANSIT);
			} else {
				locList = Erpv4Config.getLocationListByCompany(p_br.getSessionHelper(), Erpv4Config.getDefaultCoCode(p_br.getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_ANY);
			}
					
		}
		stu = new StockTakeUtil(Erpv4Config.getString(getSessionHelper(),StockTakeUtil.STOCKTAKEFILTER));
		if(mode == JxZkBiBase.MODE_UPDATE) {
			try {
				stu.getBalance(p_br.getSelectUtil(), p_br.getCellInt("stm_nref4"), p_br.getCell("stm_date").getDate(), p_br.getCellInt("stm_mrg"),locList);
				syncPickList();
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		} else if(mode == JxZkBiBase.MODE_ADD) {
//			balanceHash = new Hashtable<String,Double>();
		}
//		BiResult sr = getBr().getSubLinkByTable(stmovd_any);
		/*
		BiResult sr = getBr().getSubLink(((BiResultStmov) getBr()).getStmdLinkName());
		JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
		if(!getSessionHelper().isAdminUser()) {
			if(sv != null) {
				sv.setVisible(false);
			}
		}
		*/
	}
	
	void syncPickList() throws Exception {
		BiResult sr = getBr().getSubLinkByTable("stocktake");
		for(int i=0;i<sr.getRowCount();i++) {
			BiCellCollection col = sr.getRowCollectionV(i);
			Vector<String>iList = stu.getiList();
			col.getCell("sttk_ref4").setItemList(iList);
			String ref4 = col.getCellString("sttk_ref4");
			Vector snList;
			if(iList.indexOf(ref4) < 0) {
				snList = (Vector) iList.clone();
				snList.add(ref4);
			} else {
				snList = iList;
			}
			col.getCell("sttk_ref4").setItemList(snList);
		}
	}
	
//	void syncBalance() throws Exception {
////		for(Enumeration e : balanceHash.keys()) {
//		UniLog.log("Sync Balance");
//		
//		
//		BiResult sr = getBr().getSubLinkByTable("stocktake");
//		JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
//		int i = 0;
//		for(String ref4 : balanceHash.keySet()) {
//			double qty = balanceHash.get(ref4);
//			UniLog.log("Balance ["+ref4+"]:"+qty);
//			BiCellCollection col;
//			if(i >= sr.getRowCount()) {
//				col = sr.newRowCollection();
//				ReturnMsg rtn = sr.addSubRecord(col, i,"");
//				Object tr = rtn.getData();
//				int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
//				sv.addItemToList(tr, rowIdx);
//			} else {
//				Object o = sr.getTrStatObj(new Integer(i));
//				sr.markDelete( o, false);
//				col = sr.getRowCollectionV(i);
//				sv.gridSetDataFormat(-1,i,"remove_deleted");
//			}
//			col.getCell("sttk_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
//			col.getCell("sttk_ref4").set(ref4);
//			col.getCell("sttk_cqty").set(qty);
//			Vector snList;
//			if(iList.indexOf(ref4) < 0) {
//				snList = (Vector) iList.clone();
//				snList.add(ref4);
//			} else {
//				snList = iList;
//			}
//			col.getCell("sttk_ref4").setItemList(snList);
//			i++;
//		}
//		for(;i<sr.getRowCount();i++) {
//			Object o = sr.getTrStatObj(new Integer(i));
//			sr.markDelete( o, true);
//			sv.gridSetDataFormat(-1,i,"add_deleted");
//		}
//	}
//
//	void syncDelta() throws Exception {
////		for(Enumeration e : balanceHash.keys()) {
//		UniLog.log("Sync Delta");
//		BiResult sr = getBr().getSubLinkByTable("stmovd_any");
//		JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
//
//		Vector<BiCellCollection> bList = getBr().getSubLinkByTable("stocktake").getRowCollectionList();
//		Hashtable <String,Double> tmpBalanceHash = (Hashtable) balanceHash.clone();
//		int i = 0;
//		for(BiCellCollection bcol : bList) {
////			String ref4 = bcol.getCellString("sttk_ref4");
//			
////			String ref4 = DateUtil.dateToDateTimeStr(bcol.getCell("sttk_exprdate").getDate(), "yyyy/MM/dd")+":"+bcol.getCellString("sttk_lotno");
//			String ref4;
//			java.util.Date d = bcol.getCell("sttk_exprdate").getDate();
//
//			if(d.after(DateUtil.minDate)) {
//				ref4 = DateUtil.dateToDateTimeStr(d, "yyyy/MM/dd")+":"+bcol.getCellString("sttk_lotno");
//			} else {
//				ref4 = "1899/12/30:"+bcol.getCellString("sttk_lotno");
//			}
//			double qty = bcol.getCellDouble("sttk_cqty");
//			UniLog.log("Target Balance A["+ref4+"]:"+qty);
//			if(tmpBalanceHash.get(ref4) != null) {
//				qty -= tmpBalanceHash.get(ref4);
//				tmpBalanceHash.remove(ref4);
//			}
//			if(qty == 0.0) continue;
//			BiCellCollection col;
//			if(i >= sr.getRowCount()) {
//				col = sr.newRowCollection();
//				ReturnMsg rtn = sr.addSubRecord(col, i,"");
//				Object tr = rtn.getData();
//				int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
//				sv.addItemToList(tr, rowIdx);
//			} else {
//				Object o = sr.getTrStatObj(new Integer(i));
//				sr.markDelete( o, false);
//				col = sr.getRowCollectionV(i);
//				sv.gridSetDataFormat(-1,i,"remove_deleted");
//			}
//			col.getCell("stmd_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
//			col.getCell("stmd_ref4").set(ref4);
//			col.getCell("stmd_qty").set(-qty);
//			i++;
//		}
//		for(String ref4 : tmpBalanceHash.keySet()) {
//			double qty = tmpBalanceHash.get(ref4);
//			UniLog.log("Target Balance B["+ref4+"]:"+qty);
//			BiCellCollection col;
//			if(i >= sr.getRowCount()) {
//				col = sr.newRowCollection();
//				ReturnMsg rtn = sr.addSubRecord(col, i,"");
//				Object tr = rtn.getData();
//				int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
//				sv.addItemToList(tr, rowIdx);
//			} else {
//				Object o = sr.getTrStatObj(new Integer(i));
//				sr.markDelete( o, false);
//				col = sr.getRowCollectionV(i);
//				sv.gridSetDataFormat(-1,i,"remove_deleted");
//			}
//			col.getCell("stmd_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
//			col.getCell("stmd_ref4").set(ref4);
//			col.getCell("stmd_qty").set(qty);
//			i++;
//		}
//		for(;i<sr.getRowCount();i++) {
//			Object o = sr.getTrStatObj(new Integer(i));
//			sr.markDelete( o, true);
//			sv.gridSetDataFormat(-1,i,"add_deleted");
//		}
//	}
	
	@Override
	protected void afterPickField(String p_FieldName) {
		if(p_FieldName != null && p_FieldName.equals("st_icode")) {
					try {
						int sid = getSidByCode();
						if(sid > 0) {
							reloadCurrentBySid(sid, JxZkBiBase.MODE_UPDATE);
						} else {
							stu.getBalance(getBr().getSelectUtil(), getBr().getCellInt("stm_nref4"), getBr().getCell("stm_date").getDate(), getBr().getCellInt("stm_mrg"),locList);
							stu.syncBalance(getBr(),this);
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
		}
	}
	
	@Override
	protected ReturnMsg beforeAdd(BiResult br)
	{
		try {

			stu.syncDelta(br,this);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(ReturnMsg.defaultOk);
	}
	@Override
	protected ReturnMsg beforeUpdate(BiResult br)
	{
		try {

			stu.syncDelta(br,this);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
		if(sr.getView().getTable().getName().equals("stocktake")) {
			try {

			cl.getCell("sttk_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
			cl.getCell("sttk_ref4").setItemList(stu.getiList());
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			
		}
		return(null);
	}
}
