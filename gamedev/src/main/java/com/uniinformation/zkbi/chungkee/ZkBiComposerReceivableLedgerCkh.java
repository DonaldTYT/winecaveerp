package com.uniinformation.zkbi.chungkee;

import org.zkoss.zk.ui.Component;

import com.uniinformation.zkbi.erpv4.ZkBiComposerReceivableLedgerG2;

public class ZkBiComposerReceivableLedgerCkh extends ZkBiComposerReceivableLedgerG2 {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		if(zkfName == null) zkfName = "zkf/reports/chungkee/ReceivableLedgerReport.zul";	
		super.doAfterCompose(comp);
		listboxHeightAdjust = 100;
	}
}
