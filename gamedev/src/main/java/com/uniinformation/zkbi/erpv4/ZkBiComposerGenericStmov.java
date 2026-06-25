package com.uniinformation.zkbi.erpv4;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.jxapp.wc.StBrand;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;


public class ZkBiComposerGenericStmov extends ZkBiComposerBase {
	
    protected void setupExtraButton(final BiResult result)
    {
    	super.setupExtraButton(result);
    	
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbPrintVoucher","Print Voucher","fa-user",
				new BiActionHandler(this) {
					String paperType;
					String docCode;
					boolean isFirstDocument;
					PrtdocJson ppj;

					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
						docCode = Erpv4Config.getString(getSessionHelper(),"InvDocCode");
						if(paperType == null || paperType.trim().equals("")) paperType = "A4P";
						if(docCode == null || docCode .trim().equals("")) docCode = "GENINV01";
						try {
							ppj = PrtdocJson.newPrtdocJson(	
								Erpv4Config.getDefaultCoCode(getSessionHelper()),
			    				paperType,
			    			    docCode,
			    			    "erpv4_printDocument"
			    				) ;
							isFirstDocument = true;
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false,ex.toString()));
						}
						
						return(ReturnMsg.defaultOk);
//						try {
//							result.beginWork();
//							return(ReturnMsg.defaultOk);
//						} catch (Exception ex) {
//							UniLog.log(ex);
//							return(new ReturnMsg(false,"Begin work failed"));
//						}
					}

					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						boolean ok = result.fetchOneRecV(p_recIdx);
						if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
						if(isFirstDocument) {
							isFirstDocument = false;
						} else {
							UniLog.log("HAHA skip next document because multiple document perf pdf is not supported");
							return(ReturnMsg.defaultOk);
						}

						try {
//							ppj.setTrailerAtLastPageOnly(true);
							ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
							String module = result.getCellString("stm_module");
							if(module.equals("cstmo")) {
								ppj.addHeaderField("doctitle","Stock Out");	
								ppj.addHeaderField("cvname",result.getCellString("floc_desc"));
								ppj.addHeaderField("doctitle","Stock Out");	
							} else if(module.equals("vstmo")) 
								ppj.addHeaderField("doctitle","Stock In");	
							else if(module.equals("stadj")) 
								ppj.addHeaderField("doctitle","Adjustment");	
							else if(module.equals("sttfr")) 
								ppj.addHeaderField("doctitle","Transfer");	
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false,ex.toString()));
						}
						return(ReturnMsg.defaultOk);
//						try {
//							p_result.fetchOneRecV(p_recIdx);
//							return(StBrand.copyImageFromStockImages(p_result));
//						} catch (Exception ex) {
//							UniLog.log(ex);
//							result.rollbackWork();
//							return(new ReturnMsg(false,ex.toString()));
//						}
					}

					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						try {
							ReturnMsg rtn = ppj.toPdfStream(bos, getSessionHelper());
							if(rtn.getStatus()) {
								ZkUtil.showPdfDialog(masterWin, getSessionHelper(), bos.toByteArray(), "StockMovement");
							} else {
								Messagebox.show(rtn.getMsg());
							}
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false,ex.toString()));
						}
						return(ReturnMsg.defaultOk);
//						try {
//							result.commitWork();
//							return(ReturnMsg.defaultOk);
//						} catch (Exception ex) {
//							UniLog.log(ex);
//							return(new ReturnMsg(false,"Begin work failed"));
//						}
					}
				}
			);
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbReync","Re-Sync","fa-user",
				new BiActionHandler(this) {

					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						boolean ok = result.fetchOneRecV(p_recIdx);
						if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
						result.updateCurrent();
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						return(ReturnMsg.defaultOk);
					}
				}
			);
		
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbVerify","Verify Remote","fa-user",
				new BiActionHandler(this) {

					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						return(null);
					}

					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						result.fetchOneRecV(p_recIdx);
						/*
						RecSync.updateOneRecord(getSessionHelper().getAgent(), result.getView().getName(), result.getCurrentCollection());
						*/
						if(result.getCellInt("stm_stmgrg") <= 0) return(null);
						String rHost = Erpv4Config.getString(getSessionHelper(), "ClerpHosts");
						RpcClient rpc = null;
						try {
							rpc = RecSync.openRpc(getSessionHelper().getAgent(), rHost, 5000);
							if(rpc != null)	{
								Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncErpv4RpcServlet.getBiCollection",
											new VectorUtil()
												.addElement(rHost)
												.addElement("erpv4.MoGeneric")
												.addElement("stm_mrg = " + result.getCellInt("stm_stmgrg"))
												.toVector()
											);
								if(v != null && v.toString().startsWith("OK")) {
									JSONObject jo = new JSONObject(v.toString().substring(4));
									CellCollection col = new CellCollection();
									CellCollectionToJsonInterface.JSONObjectToCellCollection(col,jo);
									boolean ok = compateWithRemove(result,col);
									if(!ok) {
										UniLog.log("Comparing record " + result.getCellInt("stm_mrg") + " got "+ ok);
										v = rpc.callSegment("com.uniinformation.erpv4.RecSyncErpv4RpcServlet.resyncStmov",
											new VectorUtil()
												.addElement(rHost)
												.addElement(result.getCellInt("stm_stmgrg"))
												.toVector()
											);
										UniLog.log("resync got " + (v == null ? (null) : v.toString()));
									}
								} else {
									UniLog.log("Comparing record " + result.getCellInt("stm_mrg") + " master record not exist");
								
								}
							}
						} catch (Exception ex) {
							UniLog.log(ex);
							ZkBiMsgbox.show("Cannot Connect to Remote Site, please check netowrk");
						} finally {
							if(rpc != null) {
								rpc.close();
							}
						}
						return(null);
					}
					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						ZkUtil.normMsg("Verify Completed");
						return(ReturnMsg.defaultOk);
					}
				}
			);
    }
    
	boolean compateWithRemove(BiResult result,CellCollection col) {
		if(!result.getCellString("stm_status").equals(col.getCellString("stm_status"))) return(false);
		Vector<BiCellCollection> bv = result.getSubLink("erpv4.MoGenericDet").getRowCollectionList();
		Vector<CellCollection> cv = col.getCollectionList("erpv4.MoGenericDet");
		String stmmodule = col.getCellString("stm_module");
		if(stmmodule.equals("cstmo")) {
		if(!(bv.size() == cv.size())) return(false);
		for(int i = 0;i<bv.size();i++) {
			BiCellCollection bc = bv.get(i);
			CellCollection cc = cv.get(i);
			if(bc.getCellInt("stmd_irg") != cc.getCellInt("stmd_irg")) return(false);
			if(!bc.getCellString("stmd_tdtype").equals(cc.getCellString("stmd_tdtype"))) return(false);
//			if(!bc.getCellString("stmd_loc").equals(cc.getCellString("stmd_loc"))) return(false);
			if(bc.getCellDouble("stmd_qty") != cc.getCellDouble("stmd_qty")) return(false);
		}
		}
		return(true);
	}
	
}
