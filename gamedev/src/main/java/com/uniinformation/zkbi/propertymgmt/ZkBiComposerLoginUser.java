package com.uniinformation.zkbi.propertymgmt;

import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.event.Events;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.NetworkNodeUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;

public class ZkBiComposerLoginUser extends ZkBiComposerBase {

	@Override
	protected void setupDeleteButton(final BiResult result) {
		super.setupDeleteButton(result, false);
        if (btnDelete == null || !result.allowDelete()) 
        	return;

        ZkUtil.removeAllEventListener(btnDelete, "onMyClick");
		btnDelete.getEventListeners(Events.ON_CLICK).forEach(event -> btnDelete.addEventListener("onMyClick", event));
		ZkUtil.setZkBiEventListener(btnDelete, Events.ON_CLICK, event -> {
         	boolean flag = false;
           	NetworkNodeUtil wmt = sessionHelper.getAllAccessRights();
           	Set<?> selection = listModelList.getSelection();
           	for (Object ts : selection) {
           		if (ts instanceof TrStatFilter)
           			ts = ((TrStatFilter)ts).getTrStatIdx();
           		CellCollection cc = result.getRowCollectionO(ts);
           		String loginid = cc.getCellString("lgu_login");
           		List<String> accessRights = wmt.getParentList(loginid, false);
   		   		if (sessionHelper.isAdminUser(loginid) || accessRights.contains("#pmgtadm1")) {
   		   			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, String.format("管理員帳戶'%s'不能被刪除", loginid));
   		   			flag = true;
   		   			break;
   		   		} else {
   		   			TableRec tr = result.getSelectUtil().getQueryResult("select col_a from payment where col_x = ?", 
   		   							new Wherecl().appendArgument(loginid));
   		   			if (tr.getRecordCount() > 0) {
   		   				ZkBiMsgbox.show(ZkBiMsgbox.Type.error, String.format("產生過繳費單的帳戶'%s'不能被刪除", loginid));
   		   				flag = true;
   		   				break;
   		   			}
   		   		}
           	}
           	if (!flag)
           		Events.echoEvent("onMyClick", btnDelete, null);
		});
       	setupBatchModeButton(btnDelete);
	}
}
