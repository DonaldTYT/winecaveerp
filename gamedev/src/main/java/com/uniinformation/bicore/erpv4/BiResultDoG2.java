package com.uniinformation.bicore.erpv4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultDoG2 extends BiResultDO {
//	class AllocateControl {
//		int qorg;
//		int qirg;
//		int org;
//		int irg;
//		Comparable item;
//		double value;
//		double consumed;
//		public AllocateControl(int p_qorg,int p_qirg,int p_org,int p_irg,Comparable p_item) {
//			qorg = p_qorg;
//			org = p_org;
//			qirg = p_qirg;
//			irg = p_irg;
//			item = p_item;
//			consumed = 0.0;
//		}
//	}
//	Hashtable<Integer,List <AllocateControl>> allocateHash;
//
//	Pair<Integer,Integer> getNewQorgOrgFromQuotation(int p_invrg,int p_irg) {
//		List<AllocateControl> alclist =  getAllocateListFromHash(p_invrg);
//		for(AllocateControl alc : alclist) {
//			if(alc.irg == p_irg && alc.value - alc.consumed > 0.0) {
//				return(Pair.of(alc.qorg, alc.org));
//			}
//		}
//		return(null);
//	}
//
//	@Override
//	public void clearCurrentRec()
//	{
//		super.clearCurrentRec();
//		allocateHash = new Hashtable<Integer,List<AllocateControl>>();
//	}
//	
//	AllocateControl getAllocateControlFromHash(int p_invrg,int p_qorg,int p_org) throws Exception {
//		List<AllocateControl> alclist = getAllocateListFromHash(p_invrg);
//		for(AllocateControl alc : alclist) {
//			if(alc.qorg == p_qorg && alc.org == p_org) {
//				return(alc);
//			}
//		}
//		return(null);
//	}
//
//	protected void updateConsumedQtyToHash() {
//			for( List<AllocateControl> alclist : allocateHash.values()) {
//				for(AllocateControl alc : alclist) {
//					alc.consumed = 0.0;
//				}
//			}
//			
//			for(BiCellCollection bc : getSubLink(getStmdLinkName()).getRowCollectionList()) {
//				int invrg = bc.getCellInt("ind_rg");
//				int qorg = bc.getCellInt("stmd_qorg");
//				int org = bc.getCellInt("stmd_org");
//				if(invrg > 0 && qorg > 0 && org > 0) {
//					double qty = bc.getCellDouble("stmd_qty") + bc.getCellDouble("stmd_xqty");
//					if(qty != 0) {
//					try {
//						AllocateControl pd = getAllocateControlFromHash(invrg,qorg,org);
//						pd.consumed += qty;
//					} catch (Exception ex) {
//						UniLog.log(ex);
//					}
//					}
//				}
//			}
//	}
//	
//	List<AllocateControl> getAllocateListFromHash(int p_invrg) {
//		List<AllocateControl> alclist = allocateHash.get(p_invrg);
//		if(alclist == null) {
//			alclist = new ArrayList<AllocateControl>();
//			try {
// 			Hashtable<String,AllocateControl> alcHash = new Hashtable<String,AllocateControl>();
//			TableRec tr;
//			tr = getSelectUtil().getQueryResult("select ind_odrg,ind_irg,ind_itemno from quodet where ind_stqty > 0 and ind_rg = " + p_invrg);
//			for(int i=0;i<tr.getRecordCount();i++) {
//				tr.setRecPointer(i);
//				int qorg = tr.getFieldInt("ind_odrg");
//				int itemno = tr.getFieldInt("ind_itemno"); 
//				int qirg = tr.getFieldInt("ind_irg"); 
////				AllocateControl alc = new AllocateControl(qorg,irg,itemno);
//				TableRec tr2 = getSelectUtil().getQueryResult("select * from poallocate where palc_delqty > 0 and palc_qorg = " + qorg + " and palc_qirg = " + qirg);
//				for(int j=0;j<tr2.getRecordCount();j++) {
//					tr2.setRecPointer(j);
//					int org = tr2.getFieldInt("palc_org");
//					int irg = tr2.getFieldInt("palc_irg");
//					AllocateControl alc = alcHash.get(""+qorg+"_"+org);
//					if(alc == null) {
//						alc = new AllocateControl(qorg,qirg,org,irg,itemno);
//						alclist.add(alc);
//					}
//					alc.value += tr2.getFieldDouble("palc_delqty");
//				}
//			}
//			} catch (Exception ex) {
//				UniLog.log(ex);
//			}
//			Collections.sort(alclist, new Comparator<AllocateControl>() {
//				@Override
//				public int compare(AllocateControl o1, AllocateControl o2) {
//					int cc = o1.item.compareTo(o2.item);
//					if(cc != 0) return(cc);
//					return(Integer.compare(o1.org, o2.org));
//				}
//			}
//			);
//			allocateHash.put(p_invrg, alclist);
//		}
//		return(alclist);
//	}	
//
//	protected void afterFetch() {
//		super.afterFetch();
//		allocateHash = new Hashtable<Integer,List<AllocateControl>>();
//		if(getCellString("stm_stauts").equals("Confirmed")) { 
//			/* try { */
//			for(BiCellCollection bc : getSubLink(getStmdLinkName()).getRowCollectionList()) {
//				int invrg = bc.getCellInt("ind_rg");
//				int qorg = bc.getCellInt("stmd_qorg");
//				int org = bc.getCellInt("stmd_org");
//				if(qorg > 0 && org > 0) {
//					double qty = bc.getCellDouble("stmd_qty") + bc.getCellDouble("stmd_xqty");
//					try {
//						AllocateControl alc = getAllocateControlFromHash(invrg,qorg,org);
//						alc.value += qty;
//					} catch (Exception ex) {
//						UniLog.log(ex);
//					}
//				}
//			}
//			/*
//			} catch (Exception ex) {
//				
//			}
//			*/
//		}
//	}		
//	

	public BiResultDoG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

}
