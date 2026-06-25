package com.uniinformation.bicore.erpv4;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultProjectSalesAnalysis extends BiResultErpv4 {

	public BiResultProjectSalesAnalysis(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
		sortAggregates = false;
	}
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		int n;
		int indAmountPos = getSelectFieldPosition( getView().getColumnByLabel("ind_amount"));
		n = getTableRecCount();
		invalidateLoadCache();
		int lastInvrg = 0;
		int lastOdrg  = 0;
		for(int i=0;i<n;i++) {
			loadOneRec(i,getDefaultRowCollection(),false);
			int thisInvrg = getCellInt("inv_rg");
			int thisOdrg  = getCellInt("ind_odrg");

			try {
				if(thisInvrg == lastInvrg && thisOdrg == lastOdrg) {
						saveOneObjectToResultTr(i,indAmountPos,0.0);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"getLocBalaecFifo error"));
			}
			lastInvrg = thisInvrg;
			lastOdrg  = thisOdrg;
		}
		invalidateLoadCache();
		return(ReturnMsg.defaultOk);
	}
}
