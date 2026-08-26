package com.uniinformation.bicore.wc;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class BiResultCreateBrand extends BiResult {

	public BiResultCreateBrand(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection pcol, boolean isUpdate) {
		String ss = pcol.getCellString("stbd_code");
		if(StringUtils.isBlank(ss)) {
			String ip = sh.getRpcServerHost();
			int port = sh.getRpcServerPort();
			RpcClient rpc = new RpcClient(ip,port);
			rpc.open();
			try {
				Value v = rpc.callSegment("WineCaveConnection", 
					new VectorUtil()
						.addElement("winecave_get_brand_code")
						.toVector()
					);
				if(v == null || !v.toString().startsWith("OK  ") ) {
					return(new ReturnMsg(false,"Failed to get Brand Code 1"));
				}
				ss = v.toString().substring(4);
				if(StringUtils.isBlank(ss)) {
					return(new ReturnMsg(false,"Failed to get Brand Code 2"));
				}
				pcol.getCell("stbd_code").set(ss);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			} finally {
				rpc.close();
			}
		}
		return(ReturnMsg.defaultOk);
	}
}
