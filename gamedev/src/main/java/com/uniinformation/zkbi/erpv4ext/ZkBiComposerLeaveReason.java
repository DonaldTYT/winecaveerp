package com.uniinformation.zkbi.erpv4ext;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;

public class ZkBiComposerLeaveReason extends ZkBiComposerBase {

	@Override
	protected void setupDeleteButton(final BiResult result) {
		super.setupDeleteButton(result, false);
		UniLog.log1("call setupDeleteButton");
        if (!result.allowDelete()) return;

		Iterator<EventListener<?>> it = btnDelete.getEventListeners(Events.ON_CLICK).iterator();
		while (it.hasNext()) {
			EventListener<?> el = it.next();
			btnDelete.addEventListener("onMyClick", el);
			btnDelete.removeEventListener(Events.ON_CLICK, el);
		}
        btnDelete.addEventListener(Events.ON_CLICK,
           	new ZkBiEventListener<Event>() {
           		public void onZkBiEvent(Event event) throws Exception {
           			boolean flag = false;
           			Set selection = listModelList.getSelection();
           			for (Iterator it = selection.iterator();it.hasNext();) {
           				Object o = it.next();
           				Object ts = o;
           				if (ts instanceof TrStatFilter)
           					ts = ((TrStatFilter)ts).getTrStatIdx();
           				CellCollection cc = result.getRowCollectionO(ts);
   		   				if (StringUtils.equalsAny(cc.getCellString("lvrs_name"), "AL", "CL")) {
   		   					flag = true;
   		   					break;
   		   				}
           			}
           			if (flag)
           				ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "'AL' or 'CL' record cannot be deleted");
           			else
           				Events.echoEvent("onMyClick", btnDelete, null);
           		}
           	}	
        );
       	setupBatchModeButton(btnDelete);
	}
}
