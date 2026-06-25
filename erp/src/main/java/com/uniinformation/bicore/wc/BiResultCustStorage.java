package com.uniinformation.bicore.wc;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCustStorage extends BiResultErpv4 {

	public BiResultCustStorage(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	public BiResultCustStorage(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		int n;
		int irgPos = getSelectFieldPosition( getView().getColumnByLabel("cst_irg"));
		int orgPos = getSelectFieldPosition( getView().getColumnByLabel("cst_org"));
		int qtyPos = getSelectFieldPosition( getView().getColumnByLabel("cst_qty"));
		int storQtyPos = getSelectFieldPosition( getView().getColumnByLabel("cst_storqty"));
		int conQtyPos = getSelectFieldPosition( getView().getColumnByLabel("cst_conqty"));
		int soldQtyPos = getSelectFieldPosition( getView().getColumnByLabel("cst_soldqty"));
		int locPos = getSelectFieldPosition( getView().getColumnByLabel("cst_loc"));
		if(irgPos < 0 || orgPos < 0 || qtyPos < 0 || locPos < 0 || storQtyPos < 0 || conQtyPos < 0 || soldQtyPos < 0) return(ReturnMsg.defaultOk);
		n = getTableRecCount();
		invalidateLoadCache();

		int lastirg = 0;
		int lastorg = 0;
		for(int i=n-1;i>=0;i--) {
			try {
			    {
			    String loc = (String) getResultTrObject(false,locPos,i);
			    if(StringUtils.isBlank(loc)) {
			    	delTrRecord(i);
			    	continue;
			    }
			    if(loc.equals("STOR")) {
			    	double qty = (Double) getResultTrObject(false,qtyPos,i);
					saveOneObjectToResultTr(i,storQtyPos,qty);
			    }
			    if(loc.equals("WH01")) {
			    	double qty = (Double) getResultTrObject(false,qtyPos,i);
					saveOneObjectToResultTr(i,conQtyPos,qty);
			    }
			    if(loc.equals("SOLD")) {
			    	double qty = (Double) getResultTrObject(false,qtyPos,i);
					saveOneObjectToResultTr(i,soldQtyPos,qty);
			    }
			    int irg = (Integer) getResultTrObject(false,irgPos,i);
			    int org = (Integer) getResultTrObject(false,orgPos,i);
			    if(irg == lastirg && org == lastorg) {
			    	double qty0,qty1;
					saveOneObjectToResultTr(i+1,locPos,"");

			    	qty0 = (Double) getResultTrObject(false,qtyPos,i);
			    	qty1 = (Double) getResultTrObject(false,qtyPos,i+1);
			    	qty1 += qty0;
					saveOneObjectToResultTr(i+1,qtyPos,qty1);
					
			    	qty0 = (Double) getResultTrObject(false,storQtyPos,i);
			    	qty1 = (Double) getResultTrObject(false,storQtyPos,i+1);
			    	qty1 += qty0;
					saveOneObjectToResultTr(i+1,storQtyPos,qty1);
					
			    	qty0 = (Double) getResultTrObject(false,conQtyPos,i);
			    	qty1 = (Double) getResultTrObject(false,conQtyPos,i+1);
			    	qty1 += qty0;
					saveOneObjectToResultTr(i+1,conQtyPos,qty1);
					
			    	qty0 = (Double) getResultTrObject(false,soldQtyPos,i);
			    	qty1 = (Double) getResultTrObject(false,soldQtyPos,i+1);
			    	qty1 += qty0;
					saveOneObjectToResultTr(i+1,soldQtyPos,qty1);
					
					delTrRecord(i);
			    } else {
			    	lastirg = irg;
			    	lastorg = org;
			    }
			}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"normalized custstorage error"));
			}
//			List<Pair<java.util.Date,Double>> getLocBalanceFifo(SessionHelper p_sh, int p_irg, int p_org,String p_loc,java.util.Date p_date ) throws Exception {
			
		}
		invalidateLoadCache();
		return(ReturnMsg.defaultOk);
	}
}
