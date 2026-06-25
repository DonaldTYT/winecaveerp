package com.uniinformation.dynamic.aw;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultInvoiceBase;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintInvoiceWithLogo extends PrintInvoiceNoLogo {

public PrintInvoiceWithLogo() {
	super(null);
}
public PrintInvoiceWithLogo(ZkBiComposerBase p_bibase) {
	super(p_bibase);
}
@Override
protected void printOneDoc() throws Exception {
	boolean isPAW = false;
	for(BiCellCollection bc : br.getSubLink(((BiResultInvoiceBase) br).getIndLinkName()).getRowCollectionList()) {
		String str = bc.getCellString("inv_wcocode");
		if(str.equals("BAW1")) isPAW = true;
		break;
	}
//	ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);    	
	if(isPAW) {
		ppj.addHeaderImage("logo", "logo/power_logo3.jpg",600,0,0,150);    	
	} else {
		ppj.addHeaderImage("logo", "logo/pmspacklogo001.jpg",600,0,0,200);    	
	}
	ppj.addHeaderImage("logo", "logo/artway_addr_03.jpg",720,300,0,20);    	
	super.printOneDoc();
	
}
	
}
