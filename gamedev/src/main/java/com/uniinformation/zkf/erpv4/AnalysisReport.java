package com.uniinformation.zkf.erpv4;

import org.zkoss.zhtml.Br;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Composer;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkf.ZkCellComposer;


public class AnalysisReport extends ZkCellComposer{
   	BiResult br;
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		super.doAfterCompose(arg0);
   		br = getSessionHelper().getBiSchema().getViewByName("erpv4.Quotation").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
	}
	
	void reloadReport() {
		br.query();
	}
}
