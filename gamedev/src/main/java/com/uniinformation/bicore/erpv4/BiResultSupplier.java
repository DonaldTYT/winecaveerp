package com.uniinformation.bicore.erpv4;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultSupplier extends BiResultErpv4 {

	public BiResultSupplier(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg validateOneRow(CellCollection col,boolean p_update)
	{
		ReturnMsg rtnMsg = super.validateOneRow(col,p_update);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			if(col.getCell("vd_vcode").getString().equals("")) {
//				int rgno = getView().getSchema().getRg("", 1026);
//				col.getCell("vd_vcode").set( String.format("S%04d", rgno));
				Value v = getView().getSchema().getUniqueRg(this,"",2015,"vendor","vd_vcode","S&&&");
				col.getCell("vd_vcode").set( v.toString());
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
