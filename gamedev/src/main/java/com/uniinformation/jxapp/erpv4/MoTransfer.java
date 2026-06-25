package com.uniinformation.jxapp.erpv4;

import org.zkoss.zk.ui.Component;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.smartac.ShopifyGetProduct;

public class MoTransfer extends MoPos {
	@Override
	public void afterBind() {
		super.afterBind();
		detViewId = "erpv4.MoDetPosTfr";
//		toLoc = "LNTST";
		new JxFieldAction("btSyncToWeb") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				try {
					ReturnMsg rtn = ShopifyGetProduct.syncTransferToWeb(getBr());
					if(rtn != null) {
						getBr().refetchCurrent();
//						afterDisplayAction(curComp, curMode);
						bindCellCollection(getBr(),curMode);
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					messageBox(ex.toString());
				}
				
			}
			
		};
		new JxFieldAction("btUnSyncToWeb") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				try {
					ReturnMsg rtn = ShopifyGetProduct.unsyncTransferToWeb(getBr());
					if(rtn != null) {
						getBr().refetchCurrent();
//						afterDisplayAction(curComp, curMode);
						bindCellCollection(getBr(),curMode);
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					messageBox(ex.toString());
				}
				
			}
			
		};
	}

	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		String isRequest = jxGetParameter("isRequest");
		if(isRequest != null && isRequest.equals("Y")) {
			try {
				p_br.getCell("floc_desc").setMode(Cell.VMODE_DISPONLY);
				p_br.getCell("stm_status").setMode(Cell.VMODE_DISPONLY);
				if(mode == JxZkBiBase.MODE_ADD) {
					p_br.getCell("stm_status").set("Pending");
				}
			} catch(CellException cex) {
				UniLog.log(cex);
			}
		}

		if("Y".equals(Erpv4Config.getString(getSessionHelper(), "SyncToWeb"))) {
			if(getSessionHelper().hasAccessRight("#websync")) {
				jxSetVisible("btSyncToWeb",true);
				jxSetVisible("btUnSyncToWeb",true);
				if((!p_br.getCellString("stm_syncstat").equals("Synced")) &&  p_br.getCellString("stm_status").equals("Confirmed")) {
					jxSetEnable("btSyncToWeb",true);
				} else {
					jxSetEnable("btSyncToWeb",false);
				}
				if(!p_br.getCellString("stm_syncstat").equals("")) {
					jxSetEnable("btUnSyncToWeb",true);
				} else {
					jxSetEnable("btUnSyncToWeb",false);
				}
			} else {
				jxSetVisible("btSyncToWeb",false);
				jxSetVisible("btUnSyncToWeb",false);
			}
			if((p_br.getCellString("stm_syncstat").equals(""))) {
						jxSetVisible("btUpdate", true);
			} else {
						jxSetVisible("btUpdate", false);
			}
		}
	}
	
	@Override
   	public void afterDisplayAction(Component p_comp, int p_mode){
		super.afterDisplayAction(p_comp, p_mode);
		String isRequest = jxGetParameter("isRequest");
		if(isRequest != null && isRequest.equals("Y")) {
		if(p_mode == JxZkBiBase.MODE_UPDATE) {
					if(getBr().getCellString("stm_status").equals("Confirmed")) {
						jxSetVisible("btUpdate", false);
					}
		
		}
		}
		
   	}
}
