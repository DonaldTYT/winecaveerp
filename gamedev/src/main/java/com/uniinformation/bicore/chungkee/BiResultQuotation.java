package com.uniinformation.bicore.chungkee;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultQuotation extends BiResultQuotationG2 {

	public BiResultQuotation(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getNewOrderNumber(java.util.Date p_date) throws Exception {
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
//				.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//				.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
				.addElement(getCellString("inv_cocode"))
				.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),getCellString("inv_cocode")))
				.toVector()
				);
		Value val = null;
		if(getCellInt("inv_termtype") == 2) {
		
		val = rpc.callSegment("getrg_byrgcontrol_bycategory",
				new VectorUtil()
				.addElement("quotype2")
				.addElement(p_date)
				.toVector()
				);
			
		} else {
		
		val = rpc.callSegment("getrg_byrgcontrol_bycategory",
				new VectorUtil()
				.addElement("quotype0")
				.addElement(p_date)
				.toVector()
				);
			
		}
		if(val == null || !(val instanceof Strval)) {
			throw new Exception("Get Quotation Number Failed");
		}
		//return(val.toString());
		return(val.toString().trim()); //andrew200528: fix value too long bug
	}

}
