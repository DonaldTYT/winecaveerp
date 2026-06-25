package com.uniinformation.bicore.aw;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class BiResultAwQuotation extends BiResultQuotationG2 {

	public BiResultAwQuotation(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht  = super.addExtraWhereStr(p_where, p_hash);
		String uid = getSelectUtil().getLoginId();
		UniLog.log("user = " + getSelectUtil().getLoginId());
		if(!BiSchema.hasAccessRight(sh, "#allorder")) {
			HashSet<String> accessList = sh.getMatchedAccessRights("!#");
			accessList.addAll(sh.getAccessUsers());
			Wherecl wcl1 = new Wherecl();
			wcl1.genInList("and", "inv_cuser", "in", accessList);

			ColumnCell vdAccess = getCell("vd_accesskey");
			if(vdAccess != null && vdAccess.getBiColumn().getField() != null) {
				Wherecl wcl2 = new Wherecl();
				wcl2.genInList("and", vdAccess.getBiColumn().getField().getFullName(), "in", accessList);
				wcl1.orWherecl(wcl2);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
				ht.add(getView().getTable());
				ht.add(vdAccess.getBiColumn().getField().getTable());
			}
			/*
			HashSet<String> deptlist = getSessionHelper().getMatchedAccessRights("^dept");
			if(deptlist != null && !deptlist.isEmpty()) {
				Wherecl wcl2 = new Wherecl();
				String ss = null;
				for(String as : deptlist) {
					if(ss == null) ss = (
							"inv_rg in (select quodetxx.ind_rg from quodet quodetxx,stmcmodel stmcmodelxx where stmcmodelxx.stmcm_rg = quodetxx.ind_srg and stmcmodelxx.stmcm_code in('"
							+as+"')"); else ss += ",'"+as+"'";
				}
				ss += ")";
				wcl2.appendString(ss);
				wcl1.orWherecl(wcl2);
			}
			*/
			p_where.andWherecl(wcl1);
		} 
		return(ht);
	}
	HashMap<Integer,Integer> getWoProductDeviation() {
		HashMap<Integer,Integer> hm0 = new HashMap<Integer,Integer>();
		for(BiCellCollection bc : getSubLink(subLinkId).getRowCollectionList()) {
			if( BiResultQuoDet.getDeltaType(sh, bc.getCellInt("ind_pdsrg")) == BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM) {
				hm0.put(bc.getCellInt("ind_odrg"), bc.getCellInt("ind_stqty"));
			}
		}
		SelectUtil su = getSelectUtil();
		try {
			TableRec tr = su.getQueryResult("select wq_irg,wq_qty from jobmaster_real,woqty where jm_jobno = ? and wq_mrg = jm_rg ",
					new Wherecl().appendArgument(getCellString("inv_jobno"))
				);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				Integer rq = hm0.get(tr.getFieldInt("wq_irg"));
				if(rq == null) rq = -tr.getFieldInt("wq_qty"); else rq = rq - tr.getFieldInt("wq_qty");
				hm0.put(tr.getFieldInt("wq_irg"),rq);
			}
		}  catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
		Iterator<Map.Entry<Integer, Integer>> it = hm0.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry<Integer, Integer> entry = it.next();
		    if (entry.getValue() == 0) {
		        it.remove(); // Safe removal
		    }
		}
	
		
		
		
		return(hm0);
	}
	@Override 
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		/*
			if(!col.getBoolean("inv_system")) {
				if( col.getCell("inv_quostatus").getString().equals("Confirmed")) {
					if(!StringUtils.isBlank(col.getCellString("inv_jobno"))) {
						int cc;
						cc = 0;
						HashMap<Integer,Integer> hm0 = getWoProductDeviation();
						if(!hm0.isEmpty()) {
							return(new ReturnMsg(false,"Printing Product / Qty not matched with Workorders"));
						}
					}
				}
			}
			*/
			return(super.biAfterAddUpdateCurrent(col, isUpdate));
	}
}
