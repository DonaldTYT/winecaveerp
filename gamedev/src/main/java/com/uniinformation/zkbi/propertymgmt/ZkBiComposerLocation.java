package com.uniinformation.zkbi.propertymgmt;

import java.util.Set;

import org.zkoss.zk.ui.event.Events;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;

public class ZkBiComposerLocation extends com.uniinformation.zkbi.erpv4.ZkBiComposerCoLocation {

	@Override
	protected void setupDeleteButton(final BiResult result) {
		super.setupDeleteButton(result, false);
        if (btnDelete == null || !result.allowDelete()) 
        	return;

        ZkUtil.removeAllEventListener(btnDelete, "onMyClick");
		btnDelete.getEventListeners(Events.ON_CLICK).forEach(event -> btnDelete.addEventListener("onMyClick", event));
		ZkUtil.setZkBiEventListener(btnDelete, Events.ON_CLICK, event -> {
         	boolean flag = false;
           	Set<?> selection = listModelList.getSelection();
           	for (Object ts : selection) {
           		if (ts instanceof TrStatFilter)
           			ts = ((TrStatFilter)ts).getTrStatIdx();
           		CellCollection cc = result.getRowCollectionO(ts);
           		String lcdesc = cc.getCellString("lc_desc");
	   			TableRec tr = result.getSelectUtil().getQueryResult("select col_a from payment where col_c = ?", new Wherecl().appendArgument(lcdesc));
  		   		if (tr.getRecordCount() > 0) {
   		   			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, String.format("產生過繳費單的物業'%s'不能被刪除", lcdesc));
   		   			flag = true;
   		   			break;
   		   		}
           	}
           	if (!flag)
           		Events.echoEvent("onMyClick", btnDelete, null);
		});
       	setupBatchModeButton(btnDelete);
	}
}
