package com.uniinformation.zkbi.wc;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerWcOsDeli extends ZkBiComposerReport {
	static final String toLocStr = "WH01";
	@Override
	protected void setupExtraButton(final BiResult result) {
		super.setupExtraButton(result);
		Button btnCreateTfr;
    	UniLog.log("ZkBiComposerWcOsDeli setupDeleteButton");
	    btnCreateTfr = new ZkBiButton();
	    btnCreateTfr.setLabel("Transfer To Consignment");
	    btnCreateTfr.setId("btCreateTfr");
        abHelper.addButton(btnCreateTfr, "fa-print");
	    btnCreateTfr.addEventListener("onClick",
	                new EventListener() {
	    				class LocBin {
	    					String bin;
	    					double qty;
	    				}
                		Hashtable<String,Vector<LocBin>> locBinHash;
                		LocBin getNextLocBin(int p_irg,int p_org,SelectUtil su)  {
                			Vector<LocBin> locBinList = locBinHash.get(""+p_irg+"_"+p_org);
                			if(locBinList == null) {
                				locBinList = new Vector<LocBin>();
                				try {
                					TableRec tr = result.getSelectUtil().getQueryResult(
	                						"select * from podetlocbinstatus where pdlbs_stockqty > 0 and pdlbs_irg = ? and "
	                						+ "pdlbs_org = ? and "
	                						+ "pdlbs_loc = 'SOLD' ", 
	                						new Wherecl()
	                							.appendArgument(result.getCell("palc_irg").getInt())
	                							.appendArgument(result.getCell("palc_org").getInt())
	                						);
                					for(int i=0;i<tr.getRecordCount();i++) {
                						LocBin lb = new LocBin();
                						lb.bin = tr.getFieldString("pdlbs_bin");
                						lb.qty = tr.getFieldDouble("pdlbs_stockqty");
                						locBinList.add(lb);
                					}

                					locBinHash.put(""+p_irg+"_"+p_org, locBinList);
                				} catch(Exception ex) {
                					UniLog.log(ex);
                					return(null);
                				}
                				
                			}
                			if(locBinList.size() > 0) {
                				return(locBinList.get(0));
                			}
                			return(null);
                		}
                		void useLocBin(int p_irg,int p_org,int p_qty)  throws Exception {
                			Vector<LocBin> locBinList = locBinHash.get(""+p_irg+"_"+p_org);
                			if(locBinList == null) throw new Exception("Error locBinLIst mismatch 1");
                			if(locBinList.size() <= 0) throw new Exception("Error locBinLIst mismatch 2");
                			LocBin lb = locBinList.get(0);
                			if(lb.qty < (double) p_qty) throw new Exception("Error locBinLIst mismatch 3");
                			lb.qty -= (double) p_qty;
                			if(lb.qty <= 0) {
                				locBinList.remove(0);
                			}
                		}

	                	public void onEvent(Event event) throws Exception {
	                		java.util.Set selection = listModelList.getSelection();
	                		if(selection.size() <= 0) {
	                			Messagebox.show(
	                					"Please Select Items To Transfer",
	                					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	                			result.rollbackWork();
	                			return;
	                		}
	                		locBinHash = new Hashtable<String,Vector<LocBin>>();
	                		result.beginWork();
	                		RpcClient rpc = result.getSelectUtil().getRpcClient();
                			rpc.callSegment(
	                					"erpv3SetCocode", new VectorUtil().addElement("001").toVector()
                					);
	                		Vector args = new Vector();
	                		args.add(result.getSelectUtil().getLoginId());
	                		args.add(DateUtil.today());
	                		args.add("INTRA");
	                		args.add("SOLD");
	                		args.add(toLocStr);
	                		try {
	                			boolean ok = false;
	                			for(Iterator it=selection.iterator();it.hasNext();) {
	                				Object o = it.next();
	                				int tqty = 0;
	                				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
	                				UniLog.log("Create Transfer for " + idx);
	                				result.loadOneRecV(idx);
	                				if(!result.getCell("pdls_loc").getString().equals("SOLD")) continue;
//	                				tqty = result.getCell("pdls_stockqty").getInt();
	                				tqty = result.getCell("palc_delqty").getInt() + result.getCell("palc_actdelqty").getInt();
	                				if(tqty <= 0) continue;
	                				LocBin lb;
	                				while((lb = getNextLocBin(result.getCellInt("palc_irg"),result.getCellInt("palc_org"),result.getSelectUtil())) != null) {
//	                					tr.setRecPointer(i);
//	                					int n = (int) tr.getFieldDouble("pdlbs_stockqty");
	                					int n = (int) lb.qty;
	                					int m;
	                					if( n > tqty ) m = tqty; else m = n;
	                					tqty -= m;
	                					args.add(result.getCell("palc_qorg").getInt());
	                					args.add(result.getCell("palc_org").getInt());
	                					args.add(result.getCell("palc_irg").getInt());
	                					args.add(m);
	                					args.add("Bot");
	                					args.add(m);
	                					args.add("SOLD");
//	                					args.add(tr.getFieldString("pdlbs_bin"));
	                					args.add(lb.bin);
	                					args.add(toLocStr);
//	                					args.add(tr.getFieldString("pdlbs_bin"));
	                					args.add(lb.bin);
	                					ok = true;
	                					useLocBin(result.getCellInt("palc_irg"),result.getCellInt("palc_org"), m);
	                					if(tqty <= 0) break;
	                				}
	                				if(tqty > 0) {
	                					Messagebox.show(
	                							"Error !!! quantity mismatch",
	                							sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	                					result.rollbackWork();
	                					return;
	                				}
	                			}
	                			if(!ok) {
	                					Messagebox.show(
	                							"Error !!! quantity mismatch",
	                							sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	                					result.rollbackWork();
	                					return;
	                			}
	                		Value val =	rpc.callSegment(
	                					"erpv3CreateTF", args
	                					);
	                			ReturnMsg rtnMsg = null;
	                			if(val != null && val.toString().startsWith("OK")) {
	                			} else {
	                				if(val != null) 
	                					rtnMsg = new ReturnMsg(false,val.toString());
	                				else
	                					rtnMsg = new ReturnMsg(false,"Reason Unknown");
	                			}
	                			if(rtnMsg != null && !rtnMsg.getStatus()) {
	                				result.rollbackWork();
	                				Messagebox.show(
	                						"Trasfer Failed : " + rtnMsg.getMsg(),
	                						sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	                			} else {
	                				result.commitWork();
	                				Messagebox.show(
	                						"Transfer Completed ",
	                						"Success", 
	                						Messagebox.OK , 
	                						Messagebox.INFORMATION, 
	                						new org.zkoss.zk.ui.event.EventListener() {
	                							public void onEvent(Event evt) throws InterruptedException {
	                								refresh(result, null,-1,true);
	                							}
	                						}
	                						);	
	                			}

	                		} catch (Exception ex){
	                			result.rollbackWork();
	                			UniLog.log(ex);
	                				Messagebox.show(
	                						"Trasfer Failed : " + ex.toString(),
	                						sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	                		}

	                	}
	            	}
	     );    
        setupBatchModeButton(btnCreateTfr);
	}
//	@Override
//    protected void setupBatchActionBarXX(Vector <BiColumn> p_listColumns, final BiResult result){
//		super.setupBatchActionBar(p_listColumns, result);
//		Button btnCreateTfr;
//    	UniLog.log("ZkBiComposerWcOsDeli setupDeleteButton");
//	    btnCreateTfr = new ZkBiButton();
//	    btnCreateTfr.setLabel("Transfer To Consignment");
//	    btnCreateTfr.setId("btCreateTfr");
//	    batchActionBar.appendChild(btnCreateTfr);
//	    
//	        
//	    btnCreateTfr.addEventListener("onClick",
//	                new EventListener() {
//	    				class LocBin {
//	    					String bin;
//	    					double qty;
//	    				}
//                		Hashtable<String,Vector<LocBin>> locBinHash;
//                		LocBin getNextLocBin(int p_irg,int p_org,SelectUtil su)  {
//                			Vector<LocBin> locBinList = locBinHash.get(""+p_irg+"_"+p_org);
//                			if(locBinList == null) {
//                				locBinList = new Vector<LocBin>();
//                				try {
//                					TableRec tr = result.getSelectUtil().getQueryResult(
//	                						"select * from podetlocbinstatus where pdlbs_stockqty > 0 and pdlbs_irg = ? and "
//	                						+ "pdlbs_org = ? and "
//	                						+ "pdlbs_loc = 'SOLD' ", 
//	                						new Wherecl()
//	                							.appendArgument(result.getCell("palc_irg").getInt())
//	                							.appendArgument(result.getCell("palc_org").getInt())
//	                						);
//                					for(int i=0;i<tr.getRecordCount();i++) {
//                						LocBin lb = new LocBin();
//                						lb.bin = tr.getFieldString("pdlbs_bin");
//                						lb.qty = tr.getFieldDouble("pdlbs_stockqty");
//                						locBinList.add(lb);
//                					}
//
//                					locBinHash.put(""+p_irg+"_"+p_org, locBinList);
//                				} catch(Exception ex) {
//                					UniLog.log(ex);
//                					return(null);
//                				}
//                				
//                			}
//                			if(locBinList.size() > 0) {
//                				return(locBinList.get(0));
//                			}
//                			return(null);
//                		}
//                		void useLocBin(int p_irg,int p_org,int p_qty)  throws Exception {
//                			Vector<LocBin> locBinList = locBinHash.get(""+p_irg+"_"+p_org);
//                			if(locBinList == null) throw new Exception("Error locBinLIst mismatch 1");
//                			if(locBinList.size() <= 0) throw new Exception("Error locBinLIst mismatch 2");
//                			LocBin lb = locBinList.get(0);
//                			if(lb.qty < (double) p_qty) throw new Exception("Error locBinLIst mismatch 3");
//                			lb.qty -= (double) p_qty;
//                			if(lb.qty <= 0) {
//                				locBinList.remove(0);
//                			}
//                		}
//
//	                	public void onEvent(Event event) throws Exception {
//	                		java.util.Set selection = listModelList.getSelection();
//	                		if(selection.size() <= 0) {
//	                			Messagebox.show(
//	                					"Please Select Items To Transfer",
//	                					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//	                			result.rollbackWork();
//	                			return;
//	                		}
//	                		locBinHash = new Hashtable<String,Vector<LocBin>>();
//	                		result.beginWork();
//	                		RpcClient rpc = result.getSelectUtil().getRpcClient();
//                			rpc.callSegment(
//	                					"erpv3SetCocode", new VectorUtil().addElement("001").toVector()
//                					);
//	                		Vector args = new Vector();
//	                		args.add(result.getSelectUtil().getLoginId());
//	                		args.add(DateUtil.today());
//	                		args.add("INTRA");
//	                		args.add("SOLD");
//	                		args.add("WH01");
//	                		try {
//	                			boolean ok = false;
//	                			for(Iterator it=selection.iterator();it.hasNext();) {
//	                				Object o = it.next();
//	                				int tqty = 0;
//	                				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
//	                				UniLog.log("Create Transfer for " + idx);
//	                				result.loadOneRecV(idx);
//	                				if(!result.getCell("pdls_loc").getString().equals("SOLD")) continue;
////	                				tqty = result.getCell("pdls_stockqty").getInt();
//	                				tqty = result.getCell("palc_delqty").getInt() + result.getCell("palc_actdelqty").getInt();
//	                				if(tqty <= 0) continue;
//	                				/*
//
//	                				TableRec tr = result.getSelectUtil().getQueryResult(
//	                						"select * from podetlocbinstatus where pdlbs_stockqty > 0 and pdlbs_irg = ? and "
//	                						+ "pdlbs_org = ? and "
//	                						+ "pdlbs_loc = 'SOLD' ", 
//	                						new Wherecl()
//	                							.appendArgument(result.getCell("palc_irg").getInt())
//	                							.appendArgument(result.getCell("palc_org").getInt())
//	                						);
//	                						*/
//	                				LocBin lb;
////	                				for(int i=0;i<tr.getRecordCount();i++) {
//	                				while((lb = getNextLocBin(result.getCellInt("palc_irg"),result.getCellInt("palc_org"),result.getSelectUtil())) != null) {
////	                					tr.setRecPointer(i);
////	                					int n = (int) tr.getFieldDouble("pdlbs_stockqty");
//	                					int n = (int) lb.qty;
//	                					int m;
//	                					if( n > tqty ) m = tqty; else m = n;
//	                					tqty -= m;
//	                					args.add(result.getCell("palc_qorg").getInt());
//	                					args.add(result.getCell("palc_org").getInt());
//	                					args.add(result.getCell("palc_irg").getInt());
//	                					args.add(m);
//	                					args.add("Bot");
//	                					args.add(m);
//	                					args.add("SOLD");
////	                					args.add(tr.getFieldString("pdlbs_bin"));
//	                					args.add(lb.bin);
//	                					args.add("WH01");
////	                					args.add(tr.getFieldString("pdlbs_bin"));
//	                					args.add(lb.bin);
//	                					ok = true;
//	                					useLocBin(result.getCellInt("palc_irg"),result.getCellInt("palc_org"), m);
//	                					if(tqty <= 0) break;
//	                				}
//	                				if(tqty > 0) {
//	                					Messagebox.show(
//	                							"Error !!! quantity mismatch",
//	                							sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//	                					result.rollbackWork();
//	                					return;
//	                				}
//	                			}
//	                			if(!ok) {
//	                					Messagebox.show(
//	                							"Error !!! quantity mismatch",
//	                							sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//	                					result.rollbackWork();
//	                					return;
//	                			}
//	                		Value val =	rpc.callSegment(
//	                					"erpv3CreateTF", args
//	                					);
//	                			ReturnMsg rtnMsg = null;
//	                			if(val != null && val.toString().startsWith("OK")) {
//	                			} else {
//	                				if(val != null) 
//	                					rtnMsg = new ReturnMsg(false,val.toString());
//	                				else
//	                					rtnMsg = new ReturnMsg(false,"Reason Unknown");
//	                			}
//	                			if(rtnMsg != null && !rtnMsg.getStatus()) {
//	                				result.rollbackWork();
//	                				Messagebox.show(
//	                						"Trasfer Failed : " + rtnMsg.getMsg(),
//	                						sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//	                			} else {
//	                				result.commitWork();
//	                				Messagebox.show(
//	                						"Transfer Completed ",
//	                						"Success", 
//	                						Messagebox.OK , 
//	                						Messagebox.INFORMATION, 
//	                						new org.zkoss.zk.ui.event.EventListener() {
//	                							public void onEvent(Event evt) throws InterruptedException {
//	                								refresh(result, null,-1,true);
//	                							}
//	                						}
//	                						);	
//	                			}
//
//	                		} catch (Exception ex){
//	                			result.rollbackWork();
//	                			UniLog.log(ex);
//	                				Messagebox.show(
//	                						"Trasfer Failed : " + ex.toString(),
//	                						sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//	                		}
//
//	                	}
//	            	}
//	            );    
//	}
}
