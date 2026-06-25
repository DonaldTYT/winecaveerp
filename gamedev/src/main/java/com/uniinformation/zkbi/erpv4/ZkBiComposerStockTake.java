package com.uniinformation.zkbi.erpv4;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultMoStockTake;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.jxapp.erpv4.StockTakeUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerStockTake extends ZkBiComposerBase {
	enum STOCKTAKE_ACTION {STOCKTAKE_ACTION_ABORT,STOCKTAKE_ACTION_UPDATE_DETAIL,STOCKTAKE_ACTION_UPDATE_BALANCE,STOCKTAKE_ACTION_UPDATE_TOTAL};
    protected void setupExtraButton(final BiResult result)
    {
    	super.setupExtraButton(result);

    	if(!getSessionHelper().isAdminUser()) return;
    	{
		Button btClearNonZero;
    	if(masterWin.hasFellow("btClearNonZero")) {
    		btClearNonZero = (Button) masterWin.getFellow("btNonZero");
    	} 
    	else {	
	        btClearNonZero = new ZkBiButton();
	        btClearNonZero.setLabel("Clear Non-zero");
	        btClearNonZero.setId("btClearNonZero");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btClearNonZero, "fa-user");
    	} 
    	
        btClearNonZero.addEventListener("onClick",
        	new EventListener() {

				@Override
				public void onEvent(Event arg0) throws Exception {
					// TODO Auto-generated method stub
					final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/StockTakeClearZero.zul");
					final CellCollection col = new CellCollection();
					col.addCell("bfDate",new Cell(DateUtil.today()));
	        		zkf1.doModal(col,new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							// TODO Auto-generated method stub
							if(arg0.getTarget() instanceof Button) {
								zkf1.exitModal();
							}
							if(arg0.getTarget().getId().equals("btOK")) {
								SelectUtil su = result.getSelectUtil();
								TableRec tr = null;
								if(Erpv4Config.isMultiCompany(getSessionHelper())) {
									tr = su.getQueryResult("select st_irg from stock,stock_gen where st_irg not in ("
										+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? )"
										+ " and stg_irg = st_irg and stg_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper())+ "' ",
										new Wherecl().appendArgument(col.getCell("bfDate").getDate())
										);
								} else {
									tr = su.getQueryResult("select st_irg from stock where st_irg not in ("
										+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? )",
										new Wherecl().appendArgument(col.getCell("bfDate").getDate())
										);
								}
								StockTakeUtil stkutil = new StockTakeUtil(Erpv4Config.getString(getSessionHelper(), StockTakeUtil.STOCKTAKEFILTER));
								java.util.Date bDate = col.getDate("bfDate");
								result.clearCurrentRec();
								Set<String> locList = Erpv4Config.getLocationListByCompany(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_ANY);
								for(int i=0;i<tr.getRecordCount();i++) {
									tr.setRecPointer(i);
									int irg = tr.getFieldInt("st_irg");
									stkutil.init();
									stkutil.getBalance(su,irg,bDate,0,locList);
									if(!stkutil.isEmpty()) {

										UniLog.log("generate empty stock take record for " + irg);
										result.getCell("stm_ctrspec").set("Auto Generated");
										result.getCell("stm_nref4").set(irg);
										result.getCell("stm_date").set(bDate);
										stkutil.syncDelta(result,null);
										ReturnMsg rtn = result.addCurrent();
										result.clearCurrentRec();
										if(rtn != null && !rtn.getStatus()) {
											ZkBiMsgbox.show(rtn.getMsg());
											break;
										}
									}
								}
							}
							
						}
	        			}
	        		);
					
					
				}
        	
        	}
        );
    	}
        
        
		Button btGenStockTake;
    	if(masterWin.hasFellow("btGenStockTake")) {
    		btGenStockTake = (Button) masterWin.getFellow("btGenStockTake");
    	} 
    	else {	
	        btGenStockTake = new ZkBiButton();
	        btGenStockTake.setLabel("Generate Closing");
	        btGenStockTake.setId("btGenStockTake");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btGenStockTake, "fa-user");
    	} 
    	
      btGenStockTake.addEventListener("onClick",
    	new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/StockTakeClearZero.zul");
				final CellCollection col = new CellCollection();
				col.addCell("bfDate",new Cell(DateUtil.today()));
				col.addCell("hashtag",
						new Cell(
								"Autogen "+ DateUtil.dateToDateTimeStr(DateUtil.today(), "yyyy/MM/dd")
						)
				);

        		zkf1.doModal(col,new EventListener() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						// TODO Auto-generated method stub
						if(arg0.getTarget() instanceof Button) {
							zkf1.exitModal();
						}
						if(arg0.getTarget().getId().equals("btOK")) {

							java.util.Date bDate = col.getDate("bfDate");
							boolean fixFifo = col.getCell("cbFixFifo").getBoolean();
							boolean skipUnchange = col.getCell("cbSkipUnchange").getBoolean();
							String hashTag = col.getCellString("hashtag");
							
							BiResultMoStockTake rss =  (BiResultMoStockTake) result;
							ReturnMsg rtn = rss.generateBalance(bDate, skipUnchange, fixFifo, hashTag);
							if(rtn != null && !rtn.getStatus()) {
								ZkBiMsgbox.show(rtn.getMsg());
							}	
						}
						
					}
        			}
        		);
			}
    	
    	}
    );   
    	
    	
    	
//        btGenStockTake.addEventListener("onClick",
//        	new EventListener() {
//				@Override
//				public void onEvent(Event arg0) throws Exception {
//					// TODO Auto-generated method stub
//					final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/StockTakeClearZero.zul");
//					final CellCollection col = new CellCollection();
//					col.addCell("bfDate",new Cell(DateUtil.today()));
//					col.addCell("hashtag",
//							new Cell(
//									"Autogen "+ DateUtil.dateToDateTimeStr(DateUtil.today(), "yyyy/MM/dd")
//							)
//					);
//
//	        		zkf1.doModal(col,new EventListener() {
//						@Override
//						public void onEvent(Event arg0) throws Exception {
//							// TODO Auto-generated method stub
//							if(arg0.getTarget() instanceof Button) {
//								zkf1.exitModal();
//							}
//							if(arg0.getTarget().getId().equals("btOK")) {
//								java.util.Date balBeginDate;
//								balBeginDate = Erpv4Config.getCostOpeningErpDate(getSessionHelper());
//								SelectUtil su = result.getSelectUtil();
//								TableRec tr = null;
//								if(Erpv4Config.isMultiCompany(getSessionHelper())) {
//									int lcrg = Erpv4Config.getDefaultLcrg(getSessionHelper());
//									tr = su.getQueryResult("select * from locationcode where loc_mrg = ? and loc_tfronly <> 'Y' and loc_transit <> 'Y'",
//												new Wherecl().appendArgument(lcrg)
//											);
//									if(tr.getRecordCount() == 1)		 {
//										tr.setRecPointer(0);
//										tr = su.getQueryResult("select st_irg from stock,stock_gen where st_irg not in ("
//										+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? and stm_fromloc = ?)"
//										+ " and stg_irg = st_irg and stg_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper())+ "' ",
//										new Wherecl()
//										.appendArgument(col.getCell("bfDate").getDate())
//										.appendArgument(tr.getFieldString("loc_code"))
//										);
//									} else {
//									tr = su.getQueryResult("select st_irg from stock,stock_gen where st_irg not in ("
//										+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? and stm_cocode = ?)"
//										+ " and stg_irg = st_irg and stg_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper())+ "' ",
//										new Wherecl()
//										.appendArgument(col.getCell("bfDate").getDate())
//										.appendArgument(Erpv4Config.getDefaultCoCode(getSessionHelper()))
//										);
//									}
//								} else {
//									tr = su.getQueryResult("select st_irg from stock where st_irg not in ("
//										+ " select stm_nref4 from stmov where stm_type = 'MO' and stm_module = 'stake' and stm_date >= ? )",
//										new Wherecl().appendArgument(col.getCell("bfDate").getDate())
//										);
//								}
//
//								Set<String> locList = null;
//								if(Erpv4Config.isMultiCompany(getSessionHelper())) {
//									if(Erpv4Config.isMultiStockLoc(getSessionHelper())) {
//										locList = Erpv4Config.getLocationListByCompany(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_BYLCRG_EXCLUDE_TRANSIT);
//									} else {
//										locList = Erpv4Config.getLocationListByCompany(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_ANY);
//									}
//					
//								}
//								
//								StockTakeUtil stkutil = new StockTakeUtil(Erpv4Config.getString(getSessionHelper(),StockTakeUtil.STOCKTAKEFILTER));
//								java.util.Date bDate = col.getDate("bfDate");
//								boolean fixFifo = col.getCell("cbFixFifo").getBoolean();
//								boolean skipUnchange = col.getCell("cbSkipUnchange").getBoolean();
//								result.clearCurrentRec();
//								for(int i=0;i<tr.getRecordCount();i++) {
//									tr.setRecPointer(i);
//									int irg = tr.getFieldInt("st_irg");
//									stkutil.init();
//									stkutil.getBalance(su,irg,bDate,0,locList);
//									if(!stkutil.isEmpty()) {
//										stkutil.syncBalance(result, null);
//										if(fixFifo) stkutil.fixFiFo(result, null,null);
//										UniLog.log("generate closing stock take record for " + irg);
//										result.getCell("stm_ctrspec").set(col.getString("hashtag"));
//										result.getCell("stm_nref4").set(irg);
//										result.getCell("stm_date").set(bDate);
//										if(bDate.after(balBeginDate)) {
//											double avCost = CostCalculation.getWaCost(getSessionHelper(), irg, 
//													Erpv4Config.getCoWtAvOrg(getSessionHelper(), result.getCellString("stm_cocode"))
//												//	GenbucketUtil.WEIGHTED_AVERAGE_ORG
//													, bDate);
//											result.getCell("stm_fref4").set(avCost);
//										} else {
//											TableRec tr2 = su.getQueryResult("select * from stmov where stm_nref4 = ? and stm_status='Confirmed' and stm_module = 'stake' and stm_date <= ? order by stm_date desc",
//														new Wherecl()
//														.appendArgument(irg)
//														.appendArgument(bDate)
//														);
//											if(tr2.getRecordCount() > 0) {
//												tr2.setRecPointer(0);
//												result.getCell("stm_fref4").set(tr2.getFieldDouble("stm_fref4"));
//											}
//										}
//										stkutil.syncDelta(result,null);
//										if(skipUnchange && !stkutil.hasDelta(result)) {
//											result.clearCurrentRec();
//											continue;
//										}
//										ReturnMsg rtn = result.addCurrent();
//										result.clearCurrentRec();
//										if(rtn != null && !rtn.getStatus()) {
//											ZkBiMsgbox.show(rtn.getMsg());
//											break;
//										}
//									}
//								}
//							}
//							
//						}
//	        			}
//	        		);
//					
//					
//				}
//        	
//        	}
//        );
        
		Button btCheckBalance;
    	if(masterWin.hasFellow("btCheckBalance")) {
    		btCheckBalance = (Button) masterWin.getFellow("btNonZero");
    	} 
    	else {	
	        btCheckBalance = new ZkBiButton();
	        btCheckBalance.setLabel("Check Balance");
	        btCheckBalance.setId("btCheckBalance");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btCheckBalance, "fa-user");
    	} 
    	
        btCheckBalance.addEventListener("onClick",
        	new EventListener() {

				@Override
				public void onEvent(Event arg0) throws Exception {
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
								case 0: batchStockTakeAction(selection.iterator(),result,STOCKTAKE_ACTION.STOCKTAKE_ACTION_ABORT);
										break;
								case 1: batchStockTakeAction(selection.iterator(),result,STOCKTAKE_ACTION.STOCKTAKE_ACTION_UPDATE_DETAIL);
										break;
								case 2: batchStockTakeAction(selection.iterator(),result,STOCKTAKE_ACTION.STOCKTAKE_ACTION_UPDATE_BALANCE);
										break;
								case 3: batchStockTakeAction(selection.iterator(),result,STOCKTAKE_ACTION.STOCKTAKE_ACTION_UPDATE_TOTAL);
										break;
								}
								
							}
							
						}
	        		});
				}
        	
        	}
        );	

        setupBatchModeButton(btCheckBalance);
    }
    
    void batchStockTakeAction(Iterator it,BiResult result,STOCKTAKE_ACTION p_action) throws Exception {
    	
			StockTakeUtil stkutil = new StockTakeUtil(Erpv4Config.getString(getSessionHelper(), StockTakeUtil.STOCKTAKEFILTER));
			Set<String> locList = null;
			if(Erpv4Config.isMultiCompany(getSessionHelper())) {
				if(Erpv4Config.isMultiStockLoc(getSessionHelper())) {
					locList = Erpv4Config.getLocationListByCompany(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_BYLCRG_EXCLUDE_TRANSIT);
				} else {
					locList = Erpv4Config.getLocationListByCompany(getSessionHelper(), Erpv4Config.getDefaultCoCode(getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_ANY);
				}
					
			}
		int mismatchCnt = 0;
		int checkCnt= 0;
   		for(;it.hasNext();) {
				Object o = it.next();
				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
				result.loadOneRecV(idx);
				result.fetchOneRecV(idx);
				String status = result.getCellString("stm_status");
				if(p_action == STOCKTAKE_ACTION.STOCKTAKE_ACTION_UPDATE_TOTAL) {
					result.getCell("stm_status").set("Confirmed");
				} else {
					if(!status.equals("Confirmed")) continue;
				}
				java.util.Date bDate = result.getCell("stm_date").getDate();
				int irg = result.getCell("stm_nref4").getInt();
				int mrg = result.getCell("stm_mrg").getInt();
				String fromLoc = result.getCell("stm_fromloc").getString();
				SelectUtil su = result.getSelectUtil();
				stkutil.init();
				stkutil.getBalance(su,irg,bDate,0,locList);
//				Hashtable<String,Double> deltaHash = stkutil.compBalance(result);
				boolean isEmpty = stkutil.compBalance(result);
				ReturnMsg rtn = null;
				checkCnt++;
				if(!isEmpty || p_action == STOCKTAKE_ACTION.STOCKTAKE_ACTION_UPDATE_TOTAL)  {
					switch (p_action) {
					case STOCKTAKE_ACTION_ABORT:
//						ZkBiMsgbox.show("Record " + result.getCellString("stm_ref1") + " Incorrect Balance , please update ");
//						return;
						mismatchCnt++;
						UniLog.log("Record " + result.getCellString("stm_ref1") + " Incorrect Balance , please update ");
						break;
					case STOCKTAKE_ACTION_UPDATE_DETAIL:
						UniLog.log("Record " + result.getCellString("stm_ref1") + " Incorrect Balance , auto-update");
//						result.getCell("stm_ctrspec").set("Auto Update");
						stkutil.init();
						stkutil.getBalance(su,irg,bDate,mrg,locList) ;
						stkutil.syncDelta(result,  null);
						rtn = result.updateCurrent();
						if(rtn != null && !rtn.getStatus()) {
								ZkBiMsgbox.show(rtn.getMsg());
								return;
						}
						break;
					case STOCKTAKE_ACTION_UPDATE_BALANCE:
						UniLog.log("Record " + result.getCellString("stm_ref1") + " Incorrect Balance , auto-update");
//						result.getCell("stm_ctrspec").set("Auto Update");
						stkutil.init();
						stkutil.getBalance(su,irg,bDate,0,locList) ;
						stkutil.syncBalance(result,  null);
						rtn = result.updateCurrent();
						if(rtn != null && !rtn.getStatus()) {
								ZkBiMsgbox.show(rtn.getMsg());
								return;
						}
						break;
					case STOCKTAKE_ACTION_UPDATE_TOTAL:
						UniLog.log("Record " + result.getCellString("stm_ref1") + " Update Total, auto-update");
//						result.getCell("stm_ctrspec").set("Auto Update");
						stkutil.init();
						stkutil.getBalance(su,irg,bDate,mrg,locList) ;
						stkutil.syncBalance(result,  null);
						
						BiResult sr = result.getSubLinkByTable("stocktake");
						result.getCell("stm_status").set("Confirmed");

						stkutil.fixFiFo(result,null,result.getCellDouble("stm_fref3"));
						stkutil.syncDelta(result,  null);
						rtn = result.updateCurrent();
						if(rtn != null && !rtn.getStatus()) {
								ZkBiMsgbox.show(rtn.getMsg());
								return;
						}
						break;
					default:
						return;
					}
					/*
					UniLog.log("Record " + result.getCellString("stm_ref1") + " Incorrect Balance , auto-update");
					result.getCell("stm_ctrspec").set("Auto Update");
					stkutil.init();
					stkutil.getBalance(su,irg,bDate,mrg,locList) ;
					stkutil.syncDelta(result,  null);
					ReturnMsg rtn = result.updateCurrent();
					if(rtn != null && !rtn.getStatus()) {
							ZkBiMsgbox.show(rtn.getMsg());
							break;
					}
					*/
				}
   		}	 	
   		
					switch (p_action) {
					case STOCKTAKE_ACTION_ABORT:
						ZkBiMsgbox.show("Total " + checkCnt + " record checked " + mismatchCnt + " record mismatch");
					}
    	
    }
}
