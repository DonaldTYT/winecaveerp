package com.uniinformation.zkbi.clinic;


import java.io.InputStream;
import java.util.Iterator;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.clinic.DispenseUpload;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerDrugDispense extends ZkBiComposerBase  {
	
		void showUploadResult(String p_status,int p_skipped,int p_added,int p_updated,int p_failed,DispenseUpload p_dUpload,final BiResult p_result) {
	    	final ZkForm zkf1 = new ZkForm(null,"zkf/clinic/dispenseUpload.zul");
	    	final CellCollection col = new CellCollection();
	    	final DispenseUpload dUpload = p_dUpload;
	    	final Listbox resultList = (Listbox) zkf1.getComponent("resultList");
	    	col.addCell("trcount",new Cell(dUpload.getTrCount(),Cell.VMODE_DISPONLY));
	    	col.addCell("resultSummary",new Cell(p_status + " " + p_skipped + " skipped, " + p_added + " added, " + p_updated + " updated, " + p_failed + " failed", Cell.VMODE_DISPONLY));
	    	resultList.getChildren().clear();
	    	if(p_status != "Verified") {
	    		zkf1.getComponent("btOK").setVisible(false);
	    	}
	    	for(DispenseUpload.DispenseRec rec : dUpload.getTrans() ) {
	    		Listitem li = new Listitem();
	    		li.appendChild(new Listcell(rec.trxNo));
	    		li.appendChild(new Listcell(DateUtil.toDateStringY4MD(rec.trDate)));
	    		li.appendChild(new Listcell(rec.result));
	    		li.appendChild(new Listcell(rec.errNo));
	    		resultList.appendChild(li);
	    	}
	    	try {
	    	zkf1.doModal(col,new EventListener() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					// TODO Auto-generated method stub
	    			if(arg0.getTarget().getId().equals("btOK")) {
	    				exportTimerEvent.zkBiTimerEventInterface = new DispenseImportProcess(dUpload,p_result,false);	
	    			}
	    			zkf1.exitModal();
				}
	    		}
			);
	    	} catch (Exception ex) {
	    		UniLog.log(ex);
	    	}
	    	
		}
    	class DispenseImportProcess implements ZkBiTimerEventInterface {
	    	int skipped = 0;
	    	int added = 0;
	    	int updated = 0;
	    	int failed = 0;
    		int idx;
    		int count;
	    	BiResult result;
    		final DispenseUpload dUpload;
    		Iterator<DispenseUpload.DispenseRec> it;
	    	SelectUtil su;
	    	BiResult sr;
	    	boolean verifyOnly;
    		DispenseImportProcess(DispenseUpload p_dUpload,BiResult p_result,boolean p_verifyOnly) {
    			dUpload = p_dUpload;
    			it = dUpload.getTrans().iterator();
    			verifyOnly = p_verifyOnly;
    			result = p_result;
	    		su = result.getSelectUtil();
	    		sr = result.getSubLink("clinic.DrugDetail");
    			idx = 0;

    			count = 5;
	    		exportTimer.setDelay(100);
//	    		exportTimer.setRepeats(true);
//	    		exportTimer.setRunning(true);
	    		exportTimer.setRepeats(false);
	    		exportTimer.start();
	    		
	    		if(verifyOnly)
	    			progressName.setValue("Verifying ...");
	    		else
	    			progressName.setValue("Importing ...");
	    		progressMeter.setValue(idx);
	    		progressMeter.invalidate();
	    		progressPanel.doModal();	
    		}
	    	void addOneResultRow(DispenseUpload.DispenseRec rec,String p_result) {
	    		rec.result = p_result;
	    	}
			@Override
			public void onTimerFired() {
				// TODO Auto-generated method stub
				for(int next = idx+count;idx < next;idx++) {
					if(!it.hasNext()) break;
		    		progressMeter.setValue((idx * 100)/dUpload.getTrCount());
		    		try {
	    				boolean isUpdate = false;
	    				DispenseUpload.DispenseRec rec = it.next();
	    				rec.errNo = null;
	    				result.clear();
	    				result.clearCondition();
	    				result.addCustomCondition("stm_ref4 = '" + rec.trxNo + "'") ;
	    				result.query();
	    				
	    				if(result.getRowCount() > 0) {
	    					result.loadOneRecV(0);
	    					if(result.getCellString("stm_status").equals("Void")) {
	    						isUpdate = true;
	    						result.fetchOneRecV(0);
	    						for(int i=0;i<sr.getRowCount();i++) {
	    							Object o = sr.getTrStatObj(new Integer(i));
	    							sr.markDelete(o,true);
	    						}
	    			        } else {
	    			        	rec.errNo = "Transaction Already Exist";
	    						addOneResultRow(rec,"skipped");
	    						skipped++;
	    						continue;
	    			        }
	    				} 
	    				{
	    					if(!isUpdate) result.clear();
	    					if(rec.trDate == null) {
	    						rec.errNo = "Date Invalid";
	    						addOneResultRow(rec,"failed");
	    						failed++;
	    						continue;
	    					}
	    					result.getCell("stm_date").set(rec.trDate);
	    					result.getCell("stm_ref3").set(rec.patient);
	    					Cell docName = result.getCell("cldoc_name");
	    					if(docName.getItemList().contains(rec.doctor)) {
	    						result.getCell("cldoc_name").set(rec.doctor);
	    					} else  {
	    						rec.errNo = "Doctor " + rec.doctor + " not exist";
	    						addOneResultRow(rec,"failed");
	    						failed++;
	    						continue;
	    					}
	    					result.getCell("stm_ref4").set(rec.trxNo);
	    					result.getCell("vd_vname").set(rec.account);
	    					result.getCell("stm_status").set("Confirmed");
	    					result.getCell("stm_ctrspec").set("Excel Upload");
	    					boolean detailOK = true;
	    					for(DispenseUpload.DispenseDetail dd : rec.details) {
	    						if(dd.quantity <= 0.000001) {
	    							rec.errNo = "Drug " + dd.drugName + " no quantity";
	    							detailOK = false;
	    							break;
	    						}
//	    						TableRec tr = su.getQueryResult("select st_icode from stock where st_iname = '"+dd.drugName+"'");
	    						TableRec tr;
	    						tr = su.getQueryResult("select st_icode from stock where st_iname = ?",
	    																	new Wherecl().appendArgument(dd.drugName)
	    						);
	    						if(tr.getRecordCount() <= 0) {
	    							tr = su.getQueryResult("select st_icode from stock where st_iname like ?",
	    									new Wherecl().appendArgument(dd.drugName)
	    									);
	    						}
	    						if(tr.getRecordCount() > 0) {
	    							tr.setRecPointer(0);
	    							BiCellCollection dcol = sr.newRowCollection();
	    							dcol.getCell("st_icode").set(tr.getFieldString("st_icode"));
	    							dcol.getCell("stmd_entryqty").sync(dd.quantity);
	    							dcol.getCell("stmd_uprice").sync(dd.price/dd.quantity);
	    							dcol.getCell("stmd_tdtype").set("MO");
	    							sr.addSubRecord(dcol, null);
	    						} else {
	    							rec.errNo = "Drug " + dd.drugName + " not exist";
	    							detailOK=false;
	    							break;
	    						}
	    					}
	    					if(!detailOK) {
	    						addOneResultRow(rec,"failed");
	    						failed++;
	    						continue;
	    					}
	    					if(verifyOnly) {
	    						addOneResultRow(rec,"passed");
	    						added++;
	    					} else {
	    						ReturnMsg rtn =  null;
    							if(!isUpdate)
    								rtn =  result.addCurrent();
    							else
    								rtn =  result.updateCurrent();
	    						if(rtn == null || rtn.getStatus()) {
	    							addOneResultRow(rec,isUpdate ? "updated" : "added");
	    							added++;
	    						} else {
	    							rec.errNo = rtn.getMsg();
	    							addOneResultRow(rec,"failed");
	    							failed++;
	    						}
	    					}
	    				}
		    		} catch (Exception ex) {
		    			UniLog.log(ex);
		    			showUploadResult("Interruped",skipped,added,updated,failed,dUpload,result);
		    		}
				}
		    	if(!it.hasNext()) {
		    		progressPanel.setVisible(false);
		    		exportTimer.stop();
	    			exportTimerEvent.zkBiTimerEventInterface = null;
	    			if(verifyOnly) {
	    				showUploadResult("Verified",skipped,added,updated,failed,dUpload,result);
	    			} else {
	    				showUploadResult("Imported",skipped,added,updated,failed,dUpload,result);
	    			}
		    	} else {
		    		exportTimer.start();
		    	}
			}

			@Override
			public void onCancelClicked() {
				// TODO Auto-generated method stub
	    		progressPanel.setVisible(false);
		    	exportTimer.stop();
	    		exportTimerEvent.zkBiTimerEventInterface = null;
	    		UniLog.log("Loop Cancelled");
	    		showUploadResult("Cancelled",skipped,added,updated,failed,dUpload,result);
			}
    		
    	}
	   
	   protected void setupExtraButton(final BiResult result)
	    {
	    	super.setupExtraButton(result);
	    	Button btnUpload;
	    	if(masterWin.hasFellow("btnUpload")) {
	    		btnUpload = (Button) masterWin.getFellow("btnUpload");
	    	} 
	    	else {	
		        btnUpload = new ZkBiButton();
		        btnUpload.setLabel("Upload Excel");
		        btnUpload.setId("btnUpload");
		        abHelper.addButton(btnUpload, "fa-user");
	    	} 

	        btnUpload.addEventListener("onClick",
	        	new EventListener() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						// TODO Auto-generated method stub
						UniLog.log("Event Upload");
					    Fileupload.get(new ZkBiEventListener <UploadEvent>(){
								@Override
								public void onZkBiEvent(UploadEvent event) throws Exception {
									// TODO Auto-generated method stub
		    		        		UniLog.log("upload event catched");
		    		                org.zkoss.util.media.Media media = event.getMedia();
		    		                if(media != null) {
	    		                		InputStream is = media.getStreamData();
	    		                		ExcelPoi exlpoi = null;
	    		                		try {
	    		                			exlpoi = ExcelPoi.newExcelPoi(is,false);
	    		                		} 
	    		                		catch (Exception ex) {
	    		                			is.close();
	    		                			is = media.getStreamData();
	    		                			exlpoi = ExcelPoi.newExcelPoi(is,true);
	    		                		}
	    		                		exlpoi.excel_translate_Chinese(0);
	    		                		is.close();
	    			        			final ZkForm zkf1 = new ZkForm(null,"zkf/clinic/dispenseUpload.zul");
	    			        			final CellCollection col = new CellCollection();
	    								final DispenseUpload dUpload = new DispenseUpload(exlpoi);
	    								final Listbox resultList = (Listbox) zkf1.getComponent("resultList");
	    								col.addCell("trcount",new Cell(dUpload.getTrCount(),Cell.VMODE_DISPONLY));
//	    								col.addCell("trdate",new Cell(dUpload.getDefaultDate(),Cell.VMODE_DISPONLY));
	    								exportTimerEvent.zkBiTimerEventInterface = new DispenseImportProcess(dUpload,result,true);	

	    								
	    								
	    								/*
	    			        			zkf1.doModal(col,new EventListener() {
	    			        				void addOneResultRow(DispenseUpload.DispenseRec rec,String p_result) {
	    			        					Listitem li = new Listitem();
	    			        					li.appendChild(new Listcell(rec.trxNo));
	    			        					li.appendChild(new Listcell(p_result));
	    			        					li.appendChild(new Listcell(rec.errNo));
	    			        					resultList.appendChild(li);
	    			        				}
	    			        				
	    									@Override
	    									public void onEvent(Event arg0) throws Exception {
	    										// TODO Auto-generated method stub
	    										if(arg0.getTarget().getId().equals("btOK")
	    										   || arg0.getTarget().getId().equals("btRetry")) {
	    											int skipped = 0;
	    											int added = 0;
	    											int failed = 0;
	    											resultList.getChildren().clear();
	    											SelectUtil su = result.getSelectUtil();
	    											BiResult sr = result.getSubLink("clinic.DrugDetail");
	    											for(DispenseUpload.DispenseRec rec : dUpload.getTrans() ) {
	    												boolean isUpdate = false;
	    												rec.errNo = null;
	    												result.clear();
	    												result.clearCondition();
	    												result.addCustomCondition("stm_ref4 = '" + rec.trxNo + "'") ;
	    												result.query();
	    												
	    												if(result.getRowCount() > 0) {
	    													result.loadOneRecV(0);
	    													if(result.getCellString("stm_status").equals("Void")) {
	    														isUpdate = true;
	    														result.fetchOneRecV(0);
	    														for(int i=0;i<sr.getRowCount();i++) {
	    															Object o = sr.getTrStatObj(new Integer(i));
	    															sr.markDelete(o,true);
	    														}
	    			        								} else {
	    			        									rec.errNo = "Transaction Already Exist";
	    														addOneResultRow(rec,"skipped");
	    														skipped++;
	    														continue;
	    			        								}
	    												} 
	    												{
	    													if(!isUpdate) result.clear();
	    													if(rec.trDate == null) {
	    														rec.errNo = "Date Invalid";
	    														addOneResultRow(rec,"failed");
	    														failed++;
	    														continue;
	    													}
	    													result.getCell("stm_date").set(rec.trDate);
	    													result.getCell("stm_ref3").set(rec.patient);
	    													Cell docName = result.getCell("cldoc_name");
	    													if(docName.getItemList().contains(rec.doctor)) {
	    														result.getCell("cldoc_name").set(rec.doctor);
	    													} else  {
	    														rec.errNo = "Doctor " + rec.doctor + " not exist";
	    														addOneResultRow(rec,"failed");
	    														failed++;
	    														continue;
	    													}
	    													result.getCell("stm_ref4").set(rec.trxNo);
	    													result.getCell("vd_vname").set("Walk In");
	    													result.getCell("stm_status").set("Confirmed");
	    													result.getCell("stm_ctrspec").set("Excel Upload");
	    													boolean detailOK = true;
	    													for(DispenseUpload.DispenseDetail dd : rec.details) {
	    														if(dd.quantity <= 0.000001) {
	    															rec.errNo = "Drug " + dd.drugName + " no quantity";
	    															detailOK = false;
	    															break;
	    														}
//	    														TableRec tr = su.getQueryResult("select st_icode from stock where st_iname = '"+dd.drugName+"'");
	    														TableRec tr;
	    														tr = su.getQueryResult("select st_icode from stock where st_iname = ?",
	    																	new Wherecl().appendArgument(dd.drugName)
	    																);
	    														if(tr.getRecordCount() <= 0) {
	    														tr = su.getQueryResult("select st_icode from stock where st_iname like ?",
	    																	new Wherecl().appendArgument(dd.drugName)
	    																);
	    														}
	    														if(tr.getRecordCount() > 0) {
	    															tr.setRecPointer(0);
	    															
	    															BiCellCollection dcol = sr.newRowCollection();
	    															dcol.getCell("st_icode").set(tr.getFieldString("st_icode"));
	    															dcol.getCell("stmd_entryqty").sync(dd.quantity);
	    															dcol.getCell("stmd_uprice").sync(dd.price/dd.quantity);
	    															dcol.getCell("stmd_tdtype").set("MO");
	    															sr.addSubRecord(dcol, null);
	    														} else {
	    															rec.errNo = "Drug " + dd.drugName + " not exist";
	    															detailOK=false;
	    															break;
	    														}
	    													}
	    													if(!detailOK) {
	    														addOneResultRow(rec,"failed");
	    														failed++;
	    														continue;
	    													}
	    													if(col.getBoolean("cbverifyonly")) {
	    														addOneResultRow(rec,"passed");
	    														added++;
	    													} else {
	    														
	    														ReturnMsg rtn =  null;
    															if(!isUpdate)
    																rtn =  result.addCurrent();
    															else
    																rtn =  result.updateCurrent();
	    														if(rtn == null || rtn.getStatus()) {
	    															addOneResultRow(rec,isUpdate ? "updated" : "added");
	    															added++;
	    														} else {
	    															rec.errNo = rtn.getMsg();
	    															addOneResultRow(rec,"failed");
	    															failed++;
	    														}
	    													}
	    												}
	    											}
	    											//zkf1.exitModal();
	    											//ZkBiMsgbox.show("Upload: "+added + " Added, " + skipped + " Skipped, " + failed + " failed");
	    											((Label) zkf1.getComponent("resultSummary")).setValue(
	    													"Result: "+added + (col.getBoolean("cbverifyonly") ? " Passed, " :" Added, ") + skipped + " Skipped, " + failed + " failed"
	    													);
	    											
	    											zkf1.getComponent("divAction").setVisible(false);
	    											zkf1.getComponent("divResult").setVisible(true);
	    											progressPanel.setVisible(false);
	    										}
	    										if(arg0.getTarget().getId().equals("btCancel")) {
	    											zkf1.exitModal();
	    										}
	    										if(arg0.getTarget().getId().equals("btBack")) {
	    											refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
	    											zkf1.exitModal();
	    										}
	    									}
	    		        				}
	    		        			);
	    									*/
	    			        			
		    		                }
								}
					    	}
					    );
					}
      			}
	        );	
	    }
}
