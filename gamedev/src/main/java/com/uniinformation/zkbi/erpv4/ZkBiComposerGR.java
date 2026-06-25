package com.uniinformation.zkbi.erpv4;

import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerGR extends ZkBiComposerBase {
	enum CHECKPRICE_ACTION {CHECKPRICE_ACTION_ABORT,CHECKPRICE_ACTION_UPDATEPRICE};
	
    protected void setupExtraButton(final BiResult result) {
    	super.setupExtraButton(result);
		Button btCheckPrice;
    	if(masterWin.hasFellow("btCheckBalance")) {
    		btCheckPrice = (Button) masterWin.getFellow("btCheckBalance");
    	} 
    	else {	
	        btCheckPrice = new ZkBiButton();
	        btCheckPrice .setLabel("Check Balance");
	        btCheckPrice .setId("btCheckBalance");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btCheckPrice , "fa-user");
    	} 
        btCheckPrice.addEventListener("onClick",
        	new ZkBiEventListener () {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					// TODO Auto-generated method stub
             		final java.util.Set selection = listModelList.getSelection();
	       			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Records To Check",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
					final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/StockTakeBatchAction.zul");
					final CellCollection col = new CellCollection();
	        		zkf1.doModal(col,new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							if(arg0.getTarget() instanceof Button) {
								zkf1.exitModal();
							}
							if(arg0.getTarget().getId().equals("btOK")) {
								int act = col.getCellInt("rgBatchAction");
								switch(act) {
								case 0: batchCheckPriceAction(selection.iterator(),result,CHECKPRICE_ACTION.CHECKPRICE_ACTION_ABORT);
										break;
								case 1: batchCheckPriceAction(selection.iterator(),result,CHECKPRICE_ACTION.CHECKPRICE_ACTION_UPDATEPRICE);
										break;
								}
								
							}
							
						}
	        		});
					
				}
        	}
        );	

        setupBatchModeButton(btCheckPrice);
    	
    }

    boolean checkDnPrice(BiResult result) throws Exception {
    	double currentXrate = ((Erpv4BaseCellCollection) result.getCurrentCollection()).getXrateByDate(
    					result.getCellString("stm_cocode"),
    					result.getCellString("stm_cur"),
    					result.getCellDate("stm_date"));
    	if(result.getCellDouble("stm_rate") != currentXrate) return(false);
    	Vector<BiCellCollection> rows = result.getSubLink( ((BiResultStmov) result).getStmdLinkName()).getRowCollectionList();
    	for(BiCellCollection bc : rows) {
    		double poprice = bc.getCellDouble("orddet_uprice");
    		double grprice = bc.getCellDouble("stmd_uprice");
    		double stmdxrate = bc.getCellDouble("stmd_xrate");
    		if(poprice != grprice ) return(false);
    		if(stmdxrate != currentXrate) return(false);
    	}
    	return(true);
    }
    boolean updateDnPrice(BiResult result) throws Exception {
    	double currentXrate = ((Erpv4BaseCellCollection) result.getCurrentCollection()).getXrateByDate(
    					result.getCellString("stm_cocode"),
    					result.getCellString("stm_cur"),
    					result.getCellDate("stm_date"));
    	if(result.getCellDouble("stm_rate") != currentXrate) {
    		result.getCell("stm_rate").sync(currentXrate);
    	}
    	Vector<BiCellCollection> rows = result.getSubLink( ((BiResultStmov) result).getStmdLinkName()).getRowCollectionList();
    	for(BiCellCollection bc : rows) {
    		double poprice = bc.getCellDouble("orddet_uprice");
    		double grprice = bc.getCellDouble("stmd_uprice");
    		double stmdxrate = bc.getCellDouble("stmd_xrate");
    		if(stmdxrate != currentXrate) {
    			bc.getCell("stmd_xrate").sync(currentXrate);
    		}
    		if(poprice != grprice ) {
    			bc.getCell("stmd_uprice").sync(poprice);
    		}
    	}
    	return(true);
    }
    void batchCheckPriceAction(Iterator it,BiResult result,CHECKPRICE_ACTION p_action) throws Exception {
    	
   		for(;it.hasNext();) {
				Object o = it.next();
				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
				result.loadOneRecV(idx);
				result.fetchOneRecV(idx);
				boolean isPriceOK = checkDnPrice(result);
				ReturnMsg rtn;
				if(!isPriceOK)  {
					switch (p_action) {
					case CHECKPRICE_ACTION_ABORT:
//						ZkBiMsgbox.show("Record " + result.getCellString("stm_ref1") + " Incorrect Balance , please update ");
						ZkBiMsgbox.show("D/N " + result.getCellString("stm_ref1") + " date " + result.getCellDate("stm_date") + " price mismatch");
						return;
					case CHECKPRICE_ACTION_UPDATEPRICE:
						UniLog.log("D/N " + result.getCellString("stm_ref1") + " date " + result.getCellDate("stm_date") + " price mismatch, update price");
						updateDnPrice(result);
						rtn = result.updateCurrent();
						if(rtn != null && !rtn.getStatus()) {
								ZkBiMsgbox.show(rtn.getMsg());
								return;
						}
						break;
					default:
						return;
					}
				}
   		}	 	
    	
    }

}
