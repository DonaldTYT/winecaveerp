package com.uniinformation.bicore.erp;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCustomerXXXYYY extends BiResult {
	public BiResultCustomerXXXYYY(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultCustomerUsed");
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			if(col.getCell("vd_vcode").getString().equals("")) {
				int rgno = getView().getSchema().getRg(this,"", 1026);
				col.getCell("vd_vcode").set( String.format("C%04d", rgno));
			}
			col.getCell("vd_cuser").set(su.getLoginId());
			col.getCell("vd_uuser").set(su.getLoginId());
			col.getCell("vd_cdate").set(new java.util.Date());
			col.getCell("vd_udate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtnMsg);
	}
}
