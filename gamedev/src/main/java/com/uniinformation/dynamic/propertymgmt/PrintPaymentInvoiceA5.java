package com.uniinformation.dynamic.propertymgmt;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintPaymentInvoiceA5 extends PrintPaymentInvoice {

	public PrintPaymentInvoiceA5() {
		super();
	}

	public PrintPaymentInvoiceA5(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			String docCode = "GENINV01A5";
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			ppj = PrtdocJson.newPrtdocJson(	
    				cocode,
    				"A4P",
    			    docCode,
    			    "erpv4_printDocument"
			);
			ppj.setTopLeftMargin(0);
			docCnt = 0;
			//ppj.addHeaderField("doctitle","Quotation");
			//ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);    	
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return ReturnMsg.defaultOk;
	}
}
