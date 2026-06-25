package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultQuoProjectCost extends BiResultErpv4 {
	
//	Vector<String> orddetList;
	GipiNamedItemList orddetList;

	public BiResultQuoProjectCost(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
//	@Override
//	protected void afterFetch() {
//		try {
//			TableRec tr = getSelectUtil().getQueryResult("select * from quodet where ind_rg = ? " , new Wherecl().appendArgument(getCellInt("inv_rg")));
//			orddetList = new GipiNamedItemList();
//			orddetList.appendItem( "00000000","For Whole Quotation");
//			for(int i=0;i<tr.getRecordCount();i++) {
//				tr.setRecPointer(i);
//				orddetList.appendItem( String.format("%08d",tr.getFieldInt("ind_odrg")), tr.getFieldString("ind_desc"));
//			}
//			Vector<BiCellCollection > sl = getSubLink("erpv4.ProjectCostDet").getRowCollectionList();
//			for(BiCellCollection bc : sl) {
//				bc.getCell("ind_detitem").setItemPropertyInterface(orddetList);
//			}
//		} catch(Exception p_ex) {
//			UniLog.log(p_ex);
//		}
//	}	
	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_col) {
		if(!p_isFetch) return;
		try {
			TableRec tr = getSelectUtil().getQueryResult("select * from quodet where ind_rg = ? " , new Wherecl().appendArgument(p_col.getCellInt("inv_rg")));
			orddetList = new GipiNamedItemList();
			orddetList.appendItem( "00000000","For Whole Quotation");
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				orddetList.appendItem( String.format("%08d",tr.getFieldInt("ind_odrg")), tr.getFieldString("ind_desc"));
			}
		} catch(Exception p_ex) {
			UniLog.log(p_ex);
		}
	}	

	AbstractGetItemProperty getDetList() {
		return(orddetList);
	}
	
	
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg msg = super.biAfterAddUpdateCurrent(col,isUpdate);
		if(msg == null || !msg.getStatus()) return(msg);
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
					new VectorUtil()
//						.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//						.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
						.addElement(col.getCellString("inv_cocode"))
						.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),col.getCellString("inv_cocode")))
						.toVector()
					);
		Value v = rpc.callSegment("erpv4ProjectCostUpdate",
					new VectorUtil()
						.addElement(col.getCell("inv_invno").getString())
						.addElement("")
						.toVector()
					);
		if(v == null || !v.toString().startsWith("OK")) {
			return(
				new ReturnMsg(false,"Update Project Cost Failed " + (v == null ? "Unknown" : v.toString()),true)
			);
		}
		return(msg);
	}
}
