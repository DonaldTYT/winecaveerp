package com.uniinformation.zkbi;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.UniLog;

public class ZkBiComposerCompany extends ZkBiComposerBase {

	/*
		@Override
		protected void setupExtraButton(final BiResult result)
		{
			Button btSwitchCompany;
			if(masterWin.hasFellow("btSwitchCompany")) {
				btSwitchCompany = (Button) masterWin.getFellow("btSwitchCompany");
			} else {	
				btSwitchCompany = new ZkBiButton();
				btSwitchCompany.setLabel("Switch Company");
				btSwitchCompany.setId("btSwitchCompany");
				btSwitchCompany.addEventListener("onClick",
						new EventListener() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								// TODO Auto-generated method stub
								final java.util.Set selection = listModelList.getSelection();
								if(selection.size() != 1) {
									ZkBiMsgbox.show(
											"Please Select Company"
									);
									return;
								}
        			        	Object currentEditObject = selection.toArray()[0];
        			        	int idx = getTrIdxByObj(listModelList, currentEditObject);
        			        	result.loadOneRecV(idx);
        			        	String cocode = result.getCellString("co_cocode");
        			        	Erpv4Config.setDefaultCocode(result.getSessionHelper(),cocode);
        			        	Executions.getCurrent().sendRedirect("");
							}
					}
				);
				abHelper.addButton(btSwitchCompany, "fa-user");
			} 
		}
		*/
	
		@Override
		protected void doZkbiItemSelected(int p_idx,BiResult p_br) {
				UniLog.log("Switch to company");
				if(p_idx >= 0) {
					p_br.loadOneRecV(p_idx);
					try {
        			String cocode = p_br.getCellString("co_cocode");
        			BiConfig.setDefaultCocode(p_br.getSessionHelper(),cocode);
        			Executions.getCurrent().sendRedirect("");
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} 
				
		}
}
