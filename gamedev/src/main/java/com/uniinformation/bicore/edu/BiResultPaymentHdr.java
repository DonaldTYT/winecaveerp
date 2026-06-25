package com.uniinformation.bicore.edu;

import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPaymentHdr extends BiResultErpv4 {

	public BiResultPaymentHdr(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			RpcClient rpc = getSelectUtil().getRpcClient();
			Vector args = new Vector();
			args.add(getCellInt("esph_sdrg"));
			args.add(getCellInt("esph_rg"));
			Value v = rpc.callSegment("token_addUpdatePaymentMulti", args);
			if(v == null && !v.toString().startsWith("OK")) {
				return(
						new ReturnMsg(false,v == null ? "null" : v.toString())
						);
			}
		} catch (Exception cex) {
			UniLog.log(cex);
		}
		//need to call ProcessScanLog.setCSMapDirty() when payment updated.
		String cardNo = getCellString("essd_cardno");
		UniLog.log1("cardNo:%s", cardNo);
		ProcessScanLog.setCSMapDirty(cardNo);
		return(rtnMsg);
	}

	ReturnMsg doAddUpdateToken() {
		try {
			RpcClient rpc = getSelectUtil().getRpcClient();
			Vector args = new Vector();
			args.add(getCellInt("esph_sdrg"));
			args.add(getCellInt("esph_rg"));
			for(BiCellCollection bc : (Vector<BiCellCollection>) getSubLinkResult("edu.PaymentDet")) {
				//args.add(bc.getCellInt("espd_avrg"));  //course rg
				args.add(0);
				//args.add(bc.getCellString("eaav0_tokenccy"));
				String ccy = bc.getCellString("eaav0_tokenccy"); //andrew210901 fix purchase token		
				if (StringUtils.isBlank(ccy))
					ccy = bc.getCellString("essnas_tokenccy");  
				if (StringUtils.isBlank(ccy))
					ccy = bc.getCellString("espd_tokenccy");  
				args.add(ccy);
				args.add(bc.getCellDouble("espd_amount"));
			}
			Value v = rpc.callSegment("token_addUpdatePaymentMulti", args);
			if(v == null && !v.toString().startsWith("OK")) {
				return(
						new ReturnMsg(false,v == null ? "null" : v.toString())
						);
			}
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		//need to call ProcessScanLog.setCSMapDirty() when payment updated.
		String cardNo = getCellString("essd_cardno");
		UniLog.log1("cardNo:%s", cardNo);
		ProcessScanLog.setCSMapDirty(cardNo);
		return(null);
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		ReturnMsg rtn2 = doAddUpdateToken() ;
		if(rtn2 != null && !rtn2.getStatus()) return(rtn2);
		return(rtnMsg);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		ReturnMsg rtn2 = doAddUpdateToken() ;
		if(rtn2 != null && !rtn2.getStatus()) return(rtn2);
		return(rtnMsg);
	}
}
