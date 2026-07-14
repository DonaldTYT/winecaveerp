package com.uniinformation.dynamic.propertymgmt;

import com.uniinformation.erpv4.BatchBuildPrintHandler;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintProjectPaymentInvoiceA5 extends PrintProjectPaymentInvoice {

	public PrintProjectPaymentInvoiceA5() {
		this(null);
	}

	public PrintProjectPaymentInvoiceA5(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		pageSize = BatchBuildPrintHandler.A5L;
		docWidth = 760;
		docHeight = 540;
		docWidthPx = ChnftrParser.dpi100ToPx(docWidth);
		docHeightPx = ChnftrParser.dpi100ToPx(docHeight);
	}
}
