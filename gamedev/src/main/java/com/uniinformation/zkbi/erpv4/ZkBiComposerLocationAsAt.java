package com.uniinformation.zkbi.erpv4;

import java.util.Hashtable;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultLocationAsAt;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rest.clerpmulti.ClinicRefillHZentre;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerAnalysis;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiComposerBase.MultiSortMap;

public class ZkBiComposerLocationAsAt extends ZkBiComposerAnalysis {
    	
	@Override
    protected void setupExportButton(final BiResult result)
	{
		super.setupExportButton(result);
		Button btnGlPost,btnGlUnPost;
		if(!result.allowUpdate()) return;

		if(!getSessionHelper().getLoginId().equals("hlv")) return;
    	if(masterWin.hasFellow("btPivot")) {
    		btnGlPost = (Button) masterWin.getFellow("btPivot");
    	} 
    	else {	
	        btnGlPost = new ZkBiButton();
	        btnGlPost.setLabel("Pivot");
	        btnGlPost.setId("btPivot");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnGlPost, "fa-user");
    	} 

//		 Obselated
//        btnGlPost.addEventListener("onClick",
//                new EventListener() {
//                	public void onEvent(Event event) throws Exception {
//                			computeAggregateAndPivot(result,
//                					null
//                						, AggregateOrPivot.AGGREGATES.SUM
//                						,"stmd_sumqty"
//                						, "loc_desc"
//                						);
//                	}
//            	}
//            );	
    	
	}
    protected void setupExtraButton(final BiResult result)
    {
    	super.setupExtraButton(result);
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbRightOff","Right Off","fa-user",
				new BiActionHandler(this) {
					BiResult stmovBr = null;
					String fromLoc;
					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						stmovBr = getSessionHelper().getBiSchema().getViewByName("erpv4.MoAdjustmentG2").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
						fromLoc = null;
						stmovBr.clearCurrentRec();
						fromLoc = null;
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
//							boolean ok = result.fetchOneRecV(p_recIdx);
							boolean ok = result.loadOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
							String loc = result.getCellString("stmd_loc");
							if(fromLoc != null) {
								if(!fromLoc.equals(loc)) {
									return(new ReturnMsg(false,"Cannot right off multiple location"));
								}
							} else {
								fromLoc = loc;
								stmovBr.getCell("stm_date").set(((BiResultLocationAsAt) result).getAsAtDate());
								stmovBr.getCell("stm_fromloc").set(fromLoc);
								stmovBr.getCell("stm_toloc").set(fromLoc);
								stmovBr.getCell("stm_status").set("Confirmed");
								stmovBr.getCell("stm_ctrspec").set("Right Off");
//								stmovBr.getCell("stm_cur").set( Erpv4Config.getBaseCcy(getSessionHelper(),Erpv4Config.getDefaultCoCode(getSessionHelper())));
							}
							BiResult sr = stmovBr.getSubLink("erpv4.MoDetG2");
							BiCellCollection col = sr.newRowCollection();								
							sr.addSubRecord(col, -1 ,"");

							col.getCell("stmd_tdtype").set("JO");
							col.getCell("stmd_irg").set(result.getCellInt("stmd_irg"));
							col.getCell("stmd_entryqty").set(result.getCellDouble("stmd_sumqty"));
							col.getCell("stmd_eratio").set(1.0);
							col.getCell("stmd_org").set(result.getCellInt("stmd_org"));
							col.getCell("stmd_uprice").sync(0.0);
							col.getCell("stmd_ref4").set(result.getCellString("stmd_ref4"));
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
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbRefill","Refill","fa-user",
				new BiActionHandler(this) {
					ClinicRefillHZentre crhz = null;
					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						crhz = new ClinicRefillHZentre(result);
						crhz.beginRefill();
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							//boolean ok = result.fetchOneRecV(p_recIdx);
							boolean ok = result.loadOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
							crhz.addOneItem();
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false,ex.toString()));
						}
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						
		    			Messagebox.show(
	        					"Confirm Refill Selected Items ?",
	        					"Refill", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {

	        			    public void onZkBiEvent(Event evt) throws InterruptedException {
	        			        if (evt.getName().equals("onOK")) {
	        			        	ReturnMsg rtn = crhz.endRefill();
	        			        	if(rtn == null || rtn.getStatus()) {
	        			        		refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
	        			        		Messagebox.show("Refill Completed");
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
//		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbRefill","Refill","fa-user",
//				new BiActionHandler() {
//					BiResult stmovBr = null;
//					String toLoc = null;
//					Hashtable<String,Double> refillQtyHash;
//					@Override
//					public ReturnMsg beforeAction(int cnt) {
//						stmovBr = getSessionHelper().getBiSchema().getViewByName("erpv4.MoCompanyTfr").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
//						stmovBr.clearCurrentRec();
//						toLoc = null;
//						refillQtyHash = new Hashtable<String,Double>();
//						return(ReturnMsg.defaultOk);
//					}
//					@Override
//					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
//						try {
//							//boolean ok = result.fetchOneRecV(p_recIdx);
//							boolean ok = result.loadOneRecV(p_recIdx);
//							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
//							String loc = result.getCellString("stmd_loc");
//							if(toLoc != null) {
//								if(!toLoc.equals(loc)) {
//									return(new ReturnMsg(false,"Cannot refill multiple location"));
//								}
//							} else {
//								toLoc = loc;
//								stmovBr.getCell("stm_date").set(((BiResultLocationAsAt) result).getAsAtDate());
//								stmovBr.getCell("stm_fromloc").set("HQ01");
//								stmovBr.getCell("stm_toloc").set(toLoc);
//								stmovBr.getCell("stm_status").set("Confirmed");
//								stmovBr.getCell("stm_ctrspec").set("Refill");
//								//stmovBr.getCell("stm_cur").set( Erpv4Config.getBaseCcy(getSessionHelper(),Erpv4Config.getDefaultCoCode(getSessionHelper())));
//							}
//							
//							BiResult sr = stmovBr.getSubLink("erpv4.MoCompanyTfrDet");
//							BiCellCollection col = sr.newRowCollection();								
//							sr.addSubRecord(col, -1 ,"");
//							col.getCell("stmd_irg").set(result.getCellInt("stmd_irg"));
//							col.getCell("stmd_org").set(2000000001);
//							double fillQty = -result.getCellDouble("stmd_sumqty");
//							double avalQty = CostCalculation.getBalance(getSessionHelper(), col.getCellInt("stmd_irg"), col.getCellInt("stmd_org"), stmovBr.getCellDate("stm_date"));
//							Double usedQty = refillQtyHash.get(""+col.getCellInt("stmd_irg")+"_"+col.getCellInt("stmd_org"));
//							if(usedQty != null) {
//								avalQty -= usedQty;
//							}
//							if(fillQty > avalQty) {
//								fillQty = avalQty;
//							}
//							if(usedQty != null) usedQty += fillQty; else usedQty = fillQty;
//							refillQtyHash.put(""+col.getCellInt("stmd_irg")+"_"+col.getCellInt("stmd_org"),usedQty);
//							col.getCell("stmd_entryqty").set(fillQty);
//							//col.getCell("stmd_entryqty").set(-result.getCellDouble("stmd_sumqty"));
//							col.getCell("stmd_eratio").set(1.0);
//							//col.getCell("stmd_org").set(result.getCellInt("stmd_org"));
//							
//							col.getCell("stmd_ref4").set(result.getCellString("stmd_ref4"));
//							if(col.getCellString("stmd_ref4").equals("")) {
//								col.getCell("stmd_lotno").sync("");
//								col.getCell("stmd_exprdate").sync(DateUtil.zeroDate);
//							}
//						} catch (Exception ex) {
//							UniLog.log(ex);
//							return(new ReturnMsg(false,ex.toString()));
//						}
//						return(ReturnMsg.defaultOk);
//					}
//					@Override
//					public ReturnMsg afterAction() {
//						
//		    			Messagebox.show(
//	        					"Confirm Refill Selected Items ?",
//	        					"Right Off", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {
//
//	        			    public void onZkBiEvent(Event evt) throws InterruptedException {
//	        			        if (evt.getName().equals("onOK")) {
//	        			        	ReturnMsg rtn = stmovBr.addCurrent();
//	        			        	if(rtn == null || rtn.getStatus()) {
//	        			        		refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
//	        			        		Messagebox.show("Refill Completed");
//	        			        	} else {
//	        			        		Messagebox.show(rtn.getMsg());
//	        			        	}
//	        			        } else {
//	        			        }
//	        			    }
//	        			});	
//       			        return(ReturnMsg.defaultOk);
//					}
//				}
//			);
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbMoveToTrash","Move To Trash","fa-user",
				new BiActionHandler(this) {
					BiResult stmovBr = null;
					String toLoc = null;
					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						stmovBr = getSessionHelper().getBiSchema().getViewByName("erpv4.MoTransferG2").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
						stmovBr.clearCurrentRec();
						toLoc = null;
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
//							boolean ok = result.fetchOneRecV(p_recIdx);
							boolean ok = result.loadOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
							String loc = result.getCellString("stmd_loc");
							if(toLoc != null) {
								if(!toLoc.equals(loc)) {
									return(new ReturnMsg(false,"Cannot move multiple location"));
								}
							} else {
								toLoc = loc;
								stmovBr.getCell("stm_date").set(((BiResultLocationAsAt) result).getAsAtDate());
								stmovBr.getCell("stm_fromloc").set("PD000");
								stmovBr.getCell("stm_toloc").set(toLoc);
								stmovBr.getCell("stm_nref4").set(2);
								stmovBr.getCell("stm_status").set("Void");
//								stmovBr.getCell("stm_cur").set( Erpv4Config.getBaseCcy(getSessionHelper(),Erpv4Config.getDefaultCoCode(getSessionHelper())));
							}
							BiResult sr = stmovBr.getSubLink("erpv4.MoDetPosTfrG2");
							BiCellCollection col = sr.newRowCollection();								
							sr.addSubRecord(col, -1 ,"");
							col.getCell("stmd_irg").set(result.getCellInt("stmd_irg"));
							double qty = result.getCellDouble("stmd_sumqty");
							if(qty > 0) {
								col.getCell("stmd_entryqty").set(qty);
								col.getCell("stmd_nref4").set(1);
							} else {
								col.getCell("stmd_entryqty").set(-qty);
								col.getCell("stmd_nref4").set(0);
							}
							col.getCell("stmd_eratio").set(1.0);
//							col.getCell("stmd_org").set(result.getCellInt("stmd_org"));
							
							col.getCell("stmd_org").set(result.getCellInt("stmd_org"));
							col.getCell("stmd_uprice").sync(0.0);
							col.getCell("stmd_ref4").set(result.getCellString("stmd_ref4"));
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
	        					"Confirm Move Selected Items To Trash ?",
	        					"Right Off", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener() {

	        			    public void onZkBiEvent(Event evt) throws InterruptedException {
	        			        if (evt.getName().equals("onOK")) {
	        			        	ReturnMsg rtn = stmovBr.addCurrent();
	        			        	if(rtn == null || rtn.getStatus()) {
	        			        		refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
	        			        		Messagebox.show("Move Completed");
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
