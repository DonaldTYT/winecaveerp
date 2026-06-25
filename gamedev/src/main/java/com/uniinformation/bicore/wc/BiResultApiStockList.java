package com.uniinformation.bicore.wc;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultApiStockList extends BiResult{

	public BiResultApiStockList(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl0 = new Wherecl().appendString( " and pdls_stockqty > 0 and pdls_loc = 'WH01' ");
		p_where.andWherecl(wcl0);
		return(ht);
	}
	
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		int n;
		int qtyPos = getSelectFieldPosition( getView().getColumnByLabel("pdls_stockqtyreal"));
		int orgPos = getSelectFieldPosition( getView().getColumnByLabel("pdls_org"));
		n = getTableRecCount();
		invalidateLoadCache();
		int lastirg = 0;
		double lastqty = 0;
		for(int i=n-1;i>=0;i--) {
			loadOneRec(i,getDefaultRowCollection(),false);
			try {
				if(getCellString("or_cocode").equals("MAJOR1")) {
						delTrRecord(i);
						continue;
				} else if(getCellString("or_cocode").equals("WINECAVE")) {
					if(lastirg > 0 && getCellInt("pdls_irg") == lastirg) {
						getCell("pdls_stockqtyreal").set(lastqty + getCellDouble("pdls_stockqtyreal"));
						saveOneObjectToResultTr(i,qtyPos,getCellDouble("pdls_stockqtyreal"));
						saveOneObjectToResultTr(i,orgPos,0);
						delTrRecord(i+1);
					}
					lastirg = getCellInt("pdls_irg");
					lastqty = getCellDouble("pdls_stockqtyreal");
				} else {
					lastirg = 0;
					lastqty = 0;
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"refine stocklist error"));
			}
//			List<Pair<java.util.Date,Double>> getLocBalanceFifo(SessionHelper p_sh, int p_irg, int p_org,String p_loc,java.util.Date p_date ) throws Exception {
			
		}
		return(ReturnMsg.defaultOk);
	}
}
