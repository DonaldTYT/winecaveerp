package com.uniinformation.bicore.wc;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;

public class WcCellCollection extends Erpv4BaseCellCollection {

	public WcCellCollection(BiCellCollection p_col, BiResultErpv4 p_br) {
		super(p_col, p_br);
		// TODO Auto-generated constructor stub
	}
	private enum FuncName { FUNC_newBrandCode,
					NOT_DEFINED }
	@Override
	public Object evalFunction(String p_fname,Vector p_args) throws Exception {
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		switch (funcName){
		case FUNC_newBrandCode: {
				String wc = Erpv4Config.getString(br.getSessionHelper(), "WINEAC");
				if(wc != null && wc.equals("Y")) {
					String ss = br.getCellString("stbd_code");
					if(ss.trim().equals("")) {
						RpcClient rpc = br.getSelectUtil().getRpcClient();
						Value v = rpc.callSegment("new_brand_code");
						if(v != null ) {
							return(v.toString());
						}
					}
				}
				return(br.getCellString("stbd_code"));
			}
		}

		return(super.evalFunction(p_fname,p_args) );
	}

}
