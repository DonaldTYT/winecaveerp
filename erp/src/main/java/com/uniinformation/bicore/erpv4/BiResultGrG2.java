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
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultGrG2 extends BiResultGR {

	private class PdsControl {
		int org;
		int irg;
		Comparable item;
		double value;
		double consumed;
		public PdsControl(int p_org,int p_irg,Comparable p_item) {
			org = p_org;
			irg = p_irg;
			item = p_item;
			consumed = 0.0;
		}
	}
	
	Hashtable<Integer,List <PdsControl>> poHash;

	Integer getNewOrgFromPO(int p_mrg,int p_irg) {
		List<PdsControl> pdlist =  getPdListFromHash(p_mrg);
		for(PdsControl pdc : pdlist) {
			if(pdc.irg == p_irg && pdc.value - pdc.consumed > 0.0) {
				return(pdc.org);
			}
		}
		return(null);
	}

	@Override
	public void clearCurrentRec()
	{
		super.clearCurrentRec();
		poHash = new Hashtable<Integer,List<PdsControl>>();
	}
	
	PdsControl getPdsControlFromHash(int p_mrg,int p_org) throws Exception {
		List<PdsControl> pdlist = getPdListFromHash(p_mrg);
		for(PdsControl pd : pdlist) {
			if(pd.org == p_org) {
				return(pd);
			}
		}
		return(null);
	}

	protected void updateConsumedQtyToHash() {
			for( List<PdsControl> pdlist : poHash.values()) {
				for(PdsControl pdc : pdlist) {
					pdc.consumed = 0.0;
				}
			}
			
			for(BiCellCollection bc : getSubLink(getStmdLinkName()).getRowCollectionList()) {
				int mrg = bc.getCellInt("orddet_mrg");
				int org = bc.getCellInt("stmd_org");
				if(mrg > 0 && org > 0) {
					double qty = bc.getCellDouble("stmd_qty") + bc.getCellDouble("stmd_xqty");
					if(qty != 0) {
					try {
						PdsControl pd = getPdsControlFromHash(mrg,org);
						pd.consumed += qty;
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					}
				}
			}
	}
	
	List<PdsControl> getPdListFromHash(int p_mrg) {
		List<PdsControl> pdlist = poHash.get(p_mrg);
		if(pdlist == null) {
			pdlist = new ArrayList<PdsControl>();
			try {
			TableRec tr;
			tr = getSelectUtil().getQueryResult("select stmd_org,stmd_irg,stmd_tdindex from stmov,stmovd where stm_mrg = stmd_mrg and stm_status = 'Confirmed' and stmd_tdtype = 'PD' and stmd_qty > 0 and stm_mrg = " + p_mrg);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				int org = tr.getFieldInt("stmd_org");
				int itemno = tr.getFieldInt("stmd_tdindex"); 
				int irg = tr.getFieldInt("stmd_irg"); 
				PdsControl pdc = new PdsControl(org,irg,itemno);
				TableRec tr2 = getSelectUtil().getQueryResult("select * from podetstatus where pds_ostqty > 0 and pds_org = " + org + " and pds_irg = " + irg);
				for(int j=0;j<tr2.getRecordCount();j++) {
					tr2.setRecPointer(j);
					pdc.value += tr2.getFieldDouble("pds_ostqty");
				}
				pdlist.add(pdc);
			}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			Collections.sort(pdlist, new Comparator<PdsControl>() {
				@Override
				public int compare(PdsControl o1, PdsControl o2) {
					return(o1.item.compareTo(o2.item));
				}
			}
			);
			poHash.put(p_mrg, pdlist);
		}
		return(pdlist);
	}	

	protected void afterFetch() {
		super.afterFetch();
		poHash = new Hashtable<Integer,List<PdsControl>>();
		if(getCellString("stm_stauts").equals("Confirmed")) { 
			/* try { */
			for(BiCellCollection bc : getSubLink(getStmdLinkName()).getRowCollectionList()) {
				int mrg = bc.getCellInt("orders_mrg");
				int org = bc.getCellInt("stmd_org");
				if(org > 0) {
					double qty = bc.getCellDouble("stmd_qty") + bc.getCellDouble("stmd_xqty");
					try {
						PdsControl pd = getPdsControlFromHash(mrg,org);
						pd.value += qty;
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
			/*
			} catch (Exception ex) {
				
			}
			*/
		}
	}	
	public BiResultGrG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	public CellValueAction syncStmdPdBi = new CellValueAction() {
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
		}

		@Override
		public void cellAction_onfree() throws CellException {
		}
		
	};
	
	
}
