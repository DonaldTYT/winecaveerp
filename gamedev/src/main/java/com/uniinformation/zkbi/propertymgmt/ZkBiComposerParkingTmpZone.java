package com.uniinformation.zkbi.propertymgmt;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.ListModelList;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import static com.uniinformation.utils.ZkUtil.throwPredicate;

public class ZkBiComposerParkingTmpZone extends ZkBiComposerBase {

	@Override
	protected void setupDeleteButton(final BiResult result) {
		super.setupDeleteButton(result, false);
        if (btnDelete == null || !result.allowDelete()) 
        	return;

        ZkUtil.removeAllEventListener(btnDelete, "onMyClick");
		btnDelete.getEventListeners(Events.ON_CLICK).forEach(event -> btnDelete.addEventListener("onMyClick", event));
		ZkUtil.setZkBiEventListener(btnDelete, Events.ON_CLICK, event -> {
           	ListModelList<Object> lml = listModelList;
           	String[] ss = lml.getSelection().stream().map(ts -> {
           		if (ts instanceof TrStatFilter)
           			ts = ((TrStatFilter)ts).getTrStatIdx();
           		CellCollection cc = result.getRowCollectionO(ts);
           		String lcDesc = cc.getCellString("col_a");
           		String zoneCode = cc.getCellString("col_b");
           		return new String[] {lcDesc, zoneCode};
           	}).filter(throwPredicate(ss1 -> ZkUtil.hasTableRec(result.getSelectUtil(), "select serial_id from parkingtlcontract where col_a = ? and col_h = ?", 
          								new Wherecl().appendArgument(ss1[0]).appendArgument(ss1[1]))))
           	.findFirst().orElse(null);
           	if (ss != null)
           		ZkUtil.errMsg("產生過合同的位置'%s'不能被刪除", ss[1]);
           	else
           		Events.echoEvent("onMyClick", btnDelete, null);
		});
       	setupBatchModeButton(btnDelete);
	}
}
