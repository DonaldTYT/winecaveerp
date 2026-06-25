package com.uniinformation.zkbi.erpv4;


import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiComposerBase.MultiSortMap;
import com.uniinformation.zkbi.reports.ZkBiComposerAggregateReport;

public class ZkBiComposerStockList extends ZkBiComposerAggregateReport  {
    protected void setupExtraButton(final BiResult result)
    {
    	super.setupExtraButton(result);
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbRightOff",sessionHelper.getBtLabel("Right Off"),"fa-user",
				new BiActionHandler(this) {
					BiResult stmovBr = null;
					String fromLoc;
					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						stmovBr = getSessionHelper().getBiSchema().getViewByName("clinic.MoAdjustmentClinic").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
						fromLoc = null;
						stmovBr.clearCurrentRec();
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							boolean ok = result.fetchOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
							String loc = result.getCellString("stsn_loc");
							if(fromLoc != null) {
								if(!fromLoc.equals(loc)) {
									return(new ReturnMsg(false,"Cannot right off multiple location"));
								}
							} else {
								fromLoc = loc;
								stmovBr.getCell("stm_fromloc").set(fromLoc);
								stmovBr.getCell("stm_toloc").set(fromLoc);
								stmovBr.getCell("stm_status").set("Confirmed");
								stmovBr.getCell("stm_ctrspec").set("Right Off");
								stmovBr.getCell("stm_cur").set( Erpv4Config.getBaseCcy(getSessionHelper(),Erpv4Config.getDefaultCoCode(getSessionHelper())));
							}
							BiResult sr = stmovBr.getSubLink("clinic.DrugDetail");
							BiCellCollection col = sr.newRowCollection();								
							sr.addSubRecord(col, -1 ,"");

							col.getCell("stmd_tdtype").set("JO");
							col.getCell("stmd_irg").set(result.getCellInt("stsn_irg"));
							col.getCell("stmd_entryqty").set(result.getCellDouble("stsn_nqty"));
							col.getCell("stmd_eratio").set(1.0);
							col.getCell("stmd_org").set(result.getCellInt("stsn_org"));
							col.getCell("stmd_uprice").sync(0.0);
							col.getCell("stmd_ref4").set(result.getCellString("stsn_ref4"));
							if(col.getCellString("stmd_ref4").equals("")) {
								col.getCell("stmd_lotno").sync("");
								col.getCell("stmd_exprdate").sync(DateUtil.zeroDate);
							}
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false,ex.toString()));
						}
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						
		    			Messagebox.show(
	        					"Confirm Rightof Selected Items ?",
	        					"Right Off", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {
	        			    public void onZkBiEvent(Event evt) throws InterruptedException {
	        			        if (evt.getName().equals("onOK")) {
	        			        	ReturnMsg rtn = stmovBr.addCurrent();
	        			        	if(rtn == null || rtn.getStatus()) {
	        			        		refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
	        			        		Messagebox.show("Right Off Completed");
	        			        	} else {
	        			        		Messagebox.show(rtn.getMsg());
	        			        	}
	        			        } else {
	        			        }
	        			    }
	        			});	
       			        return(ReturnMsg.defaultOk);
					}
				}
			);
    }
    

}
