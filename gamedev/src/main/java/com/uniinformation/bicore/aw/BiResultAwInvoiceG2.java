package com.uniinformation.bicore.aw;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.BiResultInvoiceG2;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.Longval;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAwInvoiceG2 extends BiResultInvoiceG2 {

	public BiResultAwInvoiceG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}


	static public String newInvoiceNo(BiResult p_br,java.util.Date p_date) {
		try {
			String s = p_br.getCellString("inv_wcocode");
			java.util.Date d = p_date;
			if(s.equals("AAW1")) {
				return(BiResultErpv4.getCodeByRgControl(p_br,p_br.getCellString("invh_cocode"),"invoicing",d));
			}
			if(s.equals("AAWB")) {
				return(BiResultErpv4.getCodeByRgControl(p_br,p_br.getCellString("invh_cocode"),"invoicingb",d));
			}
			return(null);
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(null);
		}
	}	
	
	
	@Override
	protected ReturnMsg validateOneRow(CellCollection col,boolean p_update) {
		ReturnMsg rtn = super.validateOneRow(col,p_update);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		if(col.getCell("invh_invno").isBlank()) {
			try {
				String invno = newInvoiceNo(this,col.getCell("invh_date").getDate());
				if(StringUtils.isBlank(invno)) return(new ReturnMsg(false,"Fail to get Invoice Number"));
				col.getCell("invh_invno").set(invno); 
			} catch (CellException cex) {
				UniLog.log(cex);
				return(new ReturnMsg(false,cex.toString()));
			}
		}		
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(col.getCellInt("invh_quorg") > 0) {
			RpcClient rpc = getSelectUtil().getRpcClient();
			rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
//										Erpv4Config.getCoCode(getSessionHelper()) 
										col.getCellString("invh_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),col.getCellString("invh_cocode"))
									)
							.toVector()
							);
			Value val = rpc.callSegment("erpv4CalQuoinv",
				new VectorUtil()
				.addElement(getCell("invh_quorg").getInt())
				.toVector()
				);
			if(val == null || val.toInt() != 0) {
				return(new ReturnMsg(false,"Error : sync invoice to quotation failed"));
			}
		}
		return(ReturnMsg.defaultOk);
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(col.getCellInt("invh_quorg") > 0) {
			RpcClient rpc = getSelectUtil().getRpcClient();
			rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
//										Erpv4Config.getCoCode(getSessionHelper()) 
										col.getCellString("invh_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),col.getCellString("invh_cocode"))
									)
							.toVector()
							);
			Value val = rpc.callSegment("erpv4CalQuoinv",
				new VectorUtil()
				.addElement(getCell("invh_quorg").getInt())
				.toVector()
				);
			if(val == null || val.toInt() != 0) {
				return(new ReturnMsg(false,"Error : sync invoice to quotation failed"));
			}
		}
		return(rtn);
	}
}
