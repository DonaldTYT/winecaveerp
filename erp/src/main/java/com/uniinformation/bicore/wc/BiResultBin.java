package com.uniinformation.bicore.wc;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultBin extends BiResult {

	public BiResultBin(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	protected void biResultBeforeCommit() throws Exception { 
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("synclocbin", 
					new VectorUtil()
					.addElement("STOR")
					.addElement("WH01")
					.addElement("SOLD")
					.toVector()
				);
		UniLog.log("HAHA wc.Bin Before Commit");
	}
}
