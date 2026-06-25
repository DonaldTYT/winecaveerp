package com.uniinformation.zkbi.propmgmtpro;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerProperty extends ZkBiComposerBase {

	@Override
    protected EventListener<Event> getImportButtonEventListener(BiResult result) {
		return new ImportWithReloadButtonEventListener(result, "zkf/propertymgmt/Fileuploaddlg.zul", "upload2");
	}
}
