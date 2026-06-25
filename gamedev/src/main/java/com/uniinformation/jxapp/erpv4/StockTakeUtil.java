package com.uniinformation.jxapp.erpv4;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.cell.Cell;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;

public class StockTakeUtil {
	static public final String STOCKTAKEFILTER="StockTakeFilter";
	String locFilter;
	public StockTakeUtil(String p_locFilter) {
		locFilter = p_locFilter;
	}
	class StHashKey {
		String hashKey;
		String loc;
		String ref4;

		public StHashKey(String p_loc, String p_ref4) {
			ref4 = p_ref4;
			loc = p_loc;
			hashKey = p_loc+"@"+p_ref4;
		}
	    @Override
	    public int hashCode() {
	        return hashKey.hashCode();
	    }

	    @Override
	    public boolean equals(Object p_sh) {
	    	if(!(p_sh instanceof StHashKey)) return(false);
	    	return(hashKey.equals(((StHashKey) p_sh).hashKey));
	    }	
	}
	Hashtable <StHashKey,Double> balanceHash;
	Vector<String> iList;
	public boolean isEmpty() {
		return(balanceHash.size() == 0);
	}
	public void init() {
		balanceHash = new Hashtable<StHashKey,Double>();
		iList = null;
	}
	public Vector<String> getiList() {
		return(iList);
	}
	void getBalance_1(SelectUtil p_su,String selectStr) throws Exception {
		TableRec tr;
		tr = p_su.getQueryResult(selectStr);
		for(int i = 0;i<tr.getRecordCount();i++) {
			tr.setRecPointer(i);
			double qty = tr.getFieldDouble("sumqty");
			StHashKey key = new StHashKey(tr.getFieldString("stmd_loc"),tr.getFieldString("stmd_ref4"));
			if(balanceHash.get(key) != null) qty += balanceHash.get(key);
			if(qty == 0) balanceHash.remove(key); else balanceHash.put(key,qty);
		}
	}


	String constructLocList(Set<String> p_locList) {
		String s = null;
		for(String loc : p_locList) {
			if(s == null) s = " ('" + loc + "'"; else s += ",'" + loc + "'";
		}
		s += ")";
		return(s);
	}
	public void getBalance(SelectUtil su,int p_irg,java.util.Date p_date,int p_mrg,Set<String> p_locList) throws Exception {
//		setDirtyFlag(true);
		balanceHash = new Hashtable<StHashKey,Double>();
		if(p_irg > 0 && p_date.after(DateUtil.minDate)) {
			int irg = p_irg;
			int mrg = p_mrg;
			String whereStr = null;
			
			{
				whereStr = "select distinct stsn_ref4 from stockserial where stsn_irg = " + irg + " and stsn_ref4 <> '' ";
				if(p_locList != null) {
					whereStr += " and stsn_loc in " + constructLocList(p_locList);
				}
				whereStr += " order by stsn_ref4";
				TableRec tr = su.getQueryResult(whereStr);
				iList = new Vector<String>();
				iList.add("");
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					iList.add(tr.getFieldString("stsn_ref4"));
				}
			}
			
			
//			String loc = p_loc;

			String dateStr = DateUtil.dateToDateTimeStr(p_date,"yyyy/MM/dd");

			whereStr = "select stmd_loc,stmd_ref4 ,sum(stmd_qty * stmd_direction) sumqty from stmovd,stmov  where stmd_irg = " + irg +  " and stmd_mrg <> " + mrg + " and stm_mrg = stmd_mrg and stm_status='Confirmed' and stmd_date <= '" + dateStr + "' and stmd_qty <> 0 and stmd_tdtype in('MI','RI','JI','KI','MO','RO','JO','KO','SO') ";
			if(locFilter != null && !locFilter.trim().equals(""))
				whereStr +=  " and " + locFilter;
			if(p_locList != null) {
				whereStr += " and stmd_loc in " + constructLocList(p_locList);
			}
			whereStr +=  " group by 1,2";
			getBalance_1(su,whereStr);
		}
		
	}

	public boolean fixFiFo(BiResult p_br,JxForm p_jxf,Double p_tolQty) throws Exception {
		boolean needFix = false;
		UniLog.log("fix fifo");
		BiResult sr = p_br.getSubLinkByTable("stocktake");
		JxField sv = null;
		if(p_jxf != null) {
			sv = p_jxf.jxAdd("list_"+sr.getView().getName().replace(".", "_"));
		}
		Vector<BiCellCollection> srList = sr.getRowCollectionList();
		if(srList.size() <= 0) return(true);
		BiCellCollection sortList[] = new BiCellCollection[srList.size()];
		for(int i=0;i<sortList.length;i++) {
			sortList[i] = srList.get(i);
		}

		Arrays.sort(sortList,
					new Comparator() {
						@Override
						public int compare(Object o1, Object o2) {
							// TODO Auto-generated method stub
							int cc;
							BiCellCollection b1 = (BiCellCollection)o1;
							BiCellCollection b2 = (BiCellCollection)o2;
							cc = b1.getString("sttk_loc").compareTo(b2.getString("sttk_loc"));
							if(cc != 0) return(cc);
							if(b1.getDouble("sttk_cqty") < 0 && b2.getDouble("sttk_cqty") > 0) return(-1);
							if(b1.getDouble("sttk_cqty") > 0 && b2.getDouble("sttk_cqty") < 0) return(1);
							cc = b1.getString("sttk_ref4").compareTo(b2.getString("sttk_ref4"));
							if(cc != 0) return(cc);
							if(b1.getDouble("sttk_cqty") > b2.getDouble("sttk_cqty")) return(1);
							if(b2.getDouble("sttk_cqty") > b1.getDouble("sttk_cqty")) return(-1);
							return 0;
						}
						
					}
				);
		for(int i=0;i< sortList.length;i++) {
			double qty = sortList[i].getDouble("sttk_cqty");
			if(i < sortList.length-1) {
				if(sortList[i].getCellString("sttk_loc").equals(sortList[i+1].getCellString("sttk_loc"))) {
//					double qty = sortList[i].getDouble("sttk_cqty");
					if(qty < 0) {
						sortList[i].getCell("sttk_cqty").set(0);
						qty += sortList[i+1].getDouble("sttk_cqty");
						sortList[i+1].getCell("sttk_cqty").set(qty);
						needFix = true;
					}
				}
			}
		}
		if(p_tolQty != null) {
			double nTqty = 0;
			double fQty = p_tolQty;
			for(int i=0;i< sortList.length;i++) {
				Cell cc = sortList[i].getCell("sttk_cqty");
				nTqty += cc.getDouble();
			}
			if(p_tolQty != nTqty) {
				needFix=true;
				if(fQty > nTqty) {
					Cell cc = sortList[sortList.length-1].getCell("sttk_cqty");
					cc.set(cc.getDouble() + fQty - nTqty);
				} else {
					Cell cc = sortList[0].getCell("sttk_cqty");
					cc.set(cc.getDouble() + fQty - nTqty);
				}
			}
		}
		return(needFix);
	}
	
	public boolean fixFiFo2(BiResult p_br,JxForm p_jxf,double tolQty) throws Exception {
		boolean needFix = false;
		UniLog.log("fix fifo");
		BiResult sr = p_br.getSubLinkByTable("stocktake");
		JxField sv = null;
		if(p_jxf != null) {
			sv = p_jxf.jxAdd("list_"+sr.getView().getName().replace(".", "_"));
		}
		Vector<BiCellCollection> srList = sr.getRowCollectionList();
		if(srList.size() <= 0) return(true);
		BiCellCollection sortList[] = new BiCellCollection[srList.size()];
		for(int i=0;i<sortList.length;i++) {
			sortList[i] = srList.get(i);
		}

		Arrays.sort(sortList,
					new Comparator() {
						@Override
						public int compare(Object o1, Object o2) {
							// TODO Auto-generated method stub
							int cc;
							BiCellCollection b1 = (BiCellCollection)o1;
							BiCellCollection b2 = (BiCellCollection)o2;
							cc = b1.getString("sttk_loc").compareTo(b2.getString("sttk_loc"));
							if(cc != 0) return(cc);
							if(b1.getDouble("sttk_cqty") < 0 && b2.getDouble("sttk_cqty") > 0) return(-1);
							if(b1.getDouble("sttk_cqty") > 0 && b2.getDouble("sttk_cqty") < 0) return(1);
							cc = b1.getString("sttk_ref4").compareTo(b2.getString("sttk_ref4"));
							if(cc != 0) return(cc);
							if(b1.getDouble("sttk_cqty") > b2.getDouble("sttk_cqty")) return(1);
							if(b2.getDouble("sttk_cqty") > b1.getDouble("sttk_cqty")) return(-1);
							return 0;
						}
						
					}
				);
		for(int i=0;i< sortList.length;i++) {
			if(i < sortList.length-1) {
				if(sortList[i].getCellString("sttk_loc").equals(sortList[i+1].getCellString("sttk_loc"))) {
					double qty = sortList[i].getDouble("sttk_cqty");
					if(qty < 0) {
						sortList[i].getCell("sttk_cqty").set(0);
						qty += sortList[i+1].getDouble("sttk_cqty");
						sortList[i+1].getCell("sttk_cqty").set(qty);
						needFix = true;
					}
				}
			}
		}
		return(needFix);
	}	
	
	
	public void syncBalance(BiResult p_br,JxForm p_jxf) throws Exception {
		UniLog.log("Sync Balance");


		BiResult sr = p_br.getSubLinkByTable("stocktake");
		JxField sv=null;
		if(p_jxf != null) sv = p_jxf.jxAdd("list_"+sr.getView().getName().replace(".", "_"));

		int i = 0;
		Vector<StHashKey> v = new Vector<StHashKey>();
		for(StHashKey stk : balanceHash.keySet()) {
			v.add(stk);
		}
		v.sort(new Comparator() {

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				StHashKey k0 = (StHashKey) arg0;
				StHashKey k1 = (StHashKey) arg1;
				return(k0.ref4.compareTo(k1.ref4));
			}
			
			}
		);
		
		for(StHashKey stk : v) {
			double qty = balanceHash.get(stk);
			UniLog.log("Balance ["+stk.loc+","+stk.ref4+"]:"+qty);
			BiCellCollection col;
			if(i >= sr.getRowCount()) {
				col = sr.newRowCollection();
				ReturnMsg rtn = sr.addSubRecord(col, i,"");
				Object tr = rtn.getData();
				if(p_jxf != null) {
					int rowIdx = p_jxf.getGipi(sr.getView().getName()).getIndexOf(tr);
					sv.addItemToList(tr, rowIdx);
				}
			} else {
				Object o = sr.getTrStatObj(new Integer(i));
				sr.markDelete( o, false);
				col = sr.getRowCollectionV(i);
				if(p_jxf != null) {
					sv.gridSetDataFormat(-1,i,"remove_deleted");
				}
			}
//			col.getCell("sttk_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
			col.getCell("sttk_org").set( Erpv4Config.getCoWtAvOrg(p_br.getSessionHelper(), col.getCellString("stm_cocode")));
			col.getCell("sttk_loc").set(stk.loc);
			col.getCell("sttk_ref4").set(stk.ref4);
			if(stk.ref4.trim().equals("")) {
				col.getCell("sttk_exprdate").set(DateUtil.zeroDate);
				col.getCell("sttk_lotno").set("");
			}
			col.getCell("sttk_cqty").set(qty);
			Vector snList;
			if(iList.indexOf(stk.ref4) < 0) {
				snList = (Vector) iList.clone();
				snList.add(stk.ref4);
			} else {
				snList = iList;
			}
			col.getCell("sttk_ref4").setItemList(snList);
			i++;
		}
		for(;i<sr.getRowCount();i++) {
			Object o = sr.getTrStatObj(new Integer(i));
			sr.markDelete( o, true);
			if(p_jxf != null) {
				sv.gridSetDataFormat(-1,i,"add_deleted");
			}
		}
	}	
	

	public void syncDelta(BiResult p_br,JxForm p_jxf) throws Exception {
//		for(Enumeration e : balanceHash.keys()) {
		
		UniLog.log("Sync Delta");
//		BiResult sr = p_br.getSubLinkByTable("stmovd_any");
		BiResult sr = p_br.getSubLink(((BiResultStmov) p_br).getStmdLinkName());
		
		JxField sv = null;
		AbstractGetItemProperty gipi = null;
		if(p_jxf != null) {
			sv = p_jxf.jxAdd("list_"+sr.getView().getName().replace(".", "_"));
			gipi = p_jxf.getGipi(sr.getView().getName());
		}

		Vector<BiCellCollection> bList = p_br.getSubLinkByTable("stocktake").getRowCollectionList();
		Hashtable <StHashKey,Double> tmpBalanceHash = (Hashtable<StHashKey,Double>) balanceHash.clone();
		int i = 0;
		for(BiCellCollection bcol : bList) {
//			String ref4 = bcol.getCellString("sttk_ref4");
			
//			String ref4 = DateUtil.dateToDateTimeStr(bcol.getCell("sttk_exprdate").getDate(), "yyyy/MM/dd")+":"+bcol.getCellString("sttk_lotno");
			String ref4;
			String loc;
			java.util.Date d = bcol.getCell("sttk_exprdate").getDate();

			if(d.after(DateUtil.minDate)) {
				ref4 = DateUtil.dateToDateTimeStr(d, "yyyy/MM/dd")+":"+bcol.getCellString("sttk_lotno");
			} else {
				if(!bcol.getCellString("sttk_lotno").trim().equals("")) {
					ref4 = "          :"+bcol.getCellString("sttk_lotno");
				} else {
					ref4 = "";
				}
			}
			if(ref4.equals("")) {
				ref4 = bcol.getCellString("sttk_ref4");
			}
			loc = bcol.getCellString("sttk_loc");
			double qty = bcol.getCellDouble("sttk_cqty");
			UniLog.log("Target Balance A["+loc+","+ref4+"]:"+qty);
			StHashKey stk = new StHashKey(loc,ref4);
			if(tmpBalanceHash.get(stk) != null) {
				qty -= tmpBalanceHash.get(stk);
				tmpBalanceHash.remove(stk);
			}
			if(qty == 0.0) continue;
			BiCellCollection col;
			if(i >= sr.getRowCount()) {
				col = sr.newRowCollection();
				ReturnMsg rtn = sr.addSubRecord(col, i,"");
				Object tr = rtn.getData();
				if(gipi != null && sv != null) {
					int rowIdx = gipi.getIndexOf(tr);
					sv.addItemToList(tr, rowIdx);
				}
			} else {
				Object o = sr.getTrStatObj(new Integer(i));
				sr.markDelete( o, false);
				col = sr.getRowCollectionV(i);
				if(sv != null) {
					sv.gridSetDataFormat(-1,i,"remove_deleted");
				}
			}
//			col.getCell("stmd_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
			col.getCell("stmd_org").set( Erpv4Config.getCoWtAvOrg(p_br.getSessionHelper(), col.getCellString("stm_cocode")));
			col.getCell("stmd_ref4").set(ref4);
			col.getCell("stmd_loc").set(loc);
			col.getCell("stmd_qty").set(-qty);
			i++;
		}
		for(StHashKey stk: tmpBalanceHash.keySet()) {
			double qty = tmpBalanceHash.get(stk);
			UniLog.log("Target Balance B["+stk.loc+","+stk.ref4+"]:"+qty);
			BiCellCollection col;
			if(i >= sr.getRowCount()) {
				col = sr.newRowCollection();
				ReturnMsg rtn = sr.addSubRecord(col, i,"");
				Object tr = rtn.getData();
				if(gipi != null && sv != null) {
					int rowIdx = gipi.getIndexOf(tr);
					sv.addItemToList(tr, rowIdx);
				}
			} else {
				Object o = sr.getTrStatObj(new Integer(i));
				sr.markDelete( o, false);
				col = sr.getRowCollectionV(i);
				if(sv != null) {
					sv.gridSetDataFormat(-1,i,"remove_deleted");
				}
			}

//			col.getCell("stmd_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
			col.getCell("stmd_org").set( Erpv4Config.getCoWtAvOrg(p_br.getSessionHelper(), col.getCellString("stm_cocode")));
			col.getCell("stmd_ref4").set(stk.ref4);
			col.getCell("stmd_loc").set(stk.loc);
			col.getCell("stmd_qty").set(qty);
			i++;
		}
		
		for(;i<sr.getRowCount();i++) {
			Object o = sr.getTrStatObj(new Integer(i));
			sr.markDelete( o, true);
			if(sv != null) {
				sv.gridSetDataFormat(-1,i,"add_deleted");
			}
		}
	}
	
	public boolean compBalance(BiResult p_br) throws Exception {
		Hashtable<StHashKey,Double> deltaHash = new Hashtable<StHashKey,Double>();
		UniLog.log("Comp Delta");
		Vector<BiCellCollection> bList = p_br.getSubLinkByTable("stocktake").getRowCollectionList();
		Hashtable <StHashKey,Double> tmpBalanceHash = (Hashtable<StHashKey,Double>) balanceHash.clone();
		int i = 0;
		for(BiCellCollection bcol : bList) {
			String ref4 = bcol.getCellString("sttk_ref4");
			String loc = bcol.getCellString("sttk_loc");
			double qty = bcol.getCellDouble("sttk_cqty");
			StHashKey stk = new StHashKey(loc,ref4);
			UniLog.log("Target Balance A["+loc+","+ref4+"]:"+qty);
			if(tmpBalanceHash.get(stk) != null) {
				qty -= tmpBalanceHash.get(stk);
				tmpBalanceHash.remove(stk);
			}
			if(qty == 0.0) continue;
			deltaHash.put(stk, -qty);
		}
		for(StHashKey stk : tmpBalanceHash.keySet()) {
			double qty = tmpBalanceHash.get(stk);
			deltaHash.put(stk, qty);
		}
		return(deltaHash.isEmpty());
	}
	
	public boolean hasDelta(BiResult p_br) {
//		BiResult sr = p_br.getSubLinkByTable("stmovd_any");
		BiResult sr = p_br.getSubLink(((BiResultStmov) p_br).getStmdLinkName());
		return(sr.getRowCount() > 0) ;
	}

	public void convertStock(BiResult p_br,int p_fromIrg,int p_toIrg,JxForm p_jxf) throws Exception {
		
		UniLog.log("Sync Delta");
//		BiResult sr = p_br.getSubLinkByTable("stmovd_any");
		BiResult sr = p_br.getSubLink(((BiResultStmov) p_br).getStmdLinkName());
		
		JxField sv = null;
		AbstractGetItemProperty gipi = null;
		if(p_jxf != null) {
			sv = p_jxf.jxAdd("list_"+sr.getView().getName().replace(".", "_"));
			gipi = p_jxf.getGipi(sr.getView().getName());
		}
		Hashtable <StHashKey,Double> tmpBalanceHash = (Hashtable<StHashKey,Double>) balanceHash.clone();
		int ofs = sr.getRowCount();
		int k = 0;
		for(StHashKey stk: tmpBalanceHash.keySet()) {
			double qty = tmpBalanceHash.get(stk);
			UniLog.log("Target Balance B["+stk.loc+","+stk.ref4+"]:"+qty);
			BiCellCollection col;
			col = sr.newRowCollection();
			ReturnMsg rtn = sr.addSubRecord(col, ofs + k ,"");
			Object tr = rtn.getData();
			if(gipi != null && sv != null) {
				int rowIdx = gipi.getIndexOf(tr);
				sv.addItemToList(tr, rowIdx);
			}
//			col.getCell("stmd_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
			
			col.getCell("stmd_org").set( Erpv4Config.getCoWtAvOrg(p_br.getSessionHelper(), col.getCellString("stm_cocode")));
			col.getCell("stmd_irg").set(p_fromIrg);
			col.getCell("stmd_tdtype").set("JO");
			col.getCell("stmd_ref4").set(stk.ref4);
			col.getCell("stmd_loc").set(stk.loc);
			col.getCell("stmd_entryqty").set(qty);
			double unitPrice = col.getCellDouble("stmd_uprice");

			col = sr.newRowCollection();
			rtn = sr.addSubRecord(col, ofs + k * 2 + 1,"");
			tr = rtn.getData();
			if(gipi != null && sv != null) {
				int rowIdx = gipi.getIndexOf(tr);
				sv.addItemToList(tr, rowIdx);
			}
//			col.getCell("stmd_org").set(GenbucketUtil.WEIGHTED_AVERAGE_ORG);
			
			col.getCell("stmd_org").set( Erpv4Config.getCoWtAvOrg(p_br.getSessionHelper(), col.getCellString("stm_cocode")));
			col.getCell("stmd_irg").set(p_toIrg);
			col.getCell("stmd_tdtype").set("JI");
			col.getCell("stmd_ref4").set(stk.ref4);
			col.getCell("stmd_loc").set(stk.loc);
			col.getCell("stmd_entryqty").set(qty);
			col.getCell("stmd_uprice").set(unitPrice);
			
			k++;
		}
	}
}
