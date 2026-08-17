package com.uniinformation.zkbi.wc;

import org.zkoss.zk.ui.Component;

import com.uniinformation.zkbi.ZkBiComposerExtended;

public class ZkBiComposerStorageKpi extends ZkBiComposerExtended {
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		//Selectors.wireComponents(p_comp, this, false);  //important for wire variable
		useAverageForPivotSubtal = true;
		hideRowCount = true;
//    	if(hasAUDColumn == null) hasAUDColumn=false;
		super.doAfterCompose(p_comp); 
	}
}
