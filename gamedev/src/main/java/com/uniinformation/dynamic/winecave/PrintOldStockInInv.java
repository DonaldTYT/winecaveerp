package com.uniinformation.dynamic.winecave;

import java.util.ArrayList;
import java.util.List;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintOldDocMulti;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class PrintOldStockInInv extends PrintOldDocMulti {

	public PrintOldStockInInv(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
		docName = "StockInInvoice";
	}

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		// TODO Auto-generated method stub
		String invoices = br.getCellString("stm_allinv");
		if(!StringUtils.isBlank(invoices)) {
			String s[] = invoices.split(" ");
			for(String inv : s) {
				int mrg = br.getCellInt("stm_mrg");
				Value val = rpc.callSegment("winecave_print_stockininv",
							new VectorUtil()
							.addElement(mrg)
							.addElement(inv)
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement("A4P")
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
						);
				if(val != null && val.toString().startsWith("OK  ")) {
					ReturnMsg rtn = printOneDocToPdf(val.toString().substring(4));
					if(rtn != null && !rtn.getStatus()) return(rtn);
				} else {
					return(null);
				}
			}
		}
		return ReturnMsg.defaultOk;
	}

	@Override
	protected void doInitRpcClient() {
		// TODO Auto-generated method stub
		rpc.callSegment("setCocodeBaseccy",
		new VectorUtil()
		.addElement( Erpv4Config.getDefaultCoCode(sh))
		.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),
				Erpv4Config.getDefaultCoCode(sh)
				))
		.toVector()
		);	
	}

}
