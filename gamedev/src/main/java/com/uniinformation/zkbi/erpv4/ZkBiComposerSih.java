package com.uniinformation.zkbi.erpv4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultCrhAr;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiPopupBase;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerSih extends com.uniinformation.zkbi.ZkBiComposerBase{
		protected String module = "";
		protected String paymentViewId = "";
		
		class PopupCrh extends ZkBiPopupBase {
			String custCode = null;
			String cocode = null;
			String cid = null;
			double amount = 0.0;
			double lamount = 0.0;
			BiResult pbr;
			class CrdDetail {
				String vcode;
				String sno;
				String cid;
				double amount;
				double xrate;
				double lamount;
			}
			ArrayList<CrdDetail> crdarr = null;
			public PopupCrh(XulElement masterWin, SessionHelper sessionHelper, String p_viewName,BiResult p_pbr) throws Exception {
				super(masterWin, sessionHelper, p_viewName);
				pbr = p_pbr;
				init(
						new JxZkBiBaseCallback()  {
							@Override
							public void biBaseRefreshListitems(Object p_dataObj) {
								// TODO Auto-generated method stub
								
							}
							@Override
							public void biBaseRefresh(BiResult p_result) {
								// TODO Auto-generated method stub
								
							}
							@Override
							public void biBaseOpen() {
								// TODO Auto-generated method stub
								UniLog.log("before baseOpen");
//								try {
////									RpcClient rpc = popupBr.getSelectUtil().getRpcClient();
////									rpc.callSegment("setCocodeBaseccy",
////										new VectorUtil()
////										.addElement("001")
////										.addElement("MOP")
////										.toVector()
////									);
//									
//									
//									popupBr.getCell("crh_cocode").set("001");
//									popupBr.getCell("crh_dbinputano").set("13301");
//									popupBr.getCell("crh_date").set(DateUtil.today());
//									popupBr.getCell("crh_vcode").set(custCode);
//									popupBr.getCell("crh_cid").set(cid);
//									popupBr.getCell("crh_amount").set(amount);
//									popupBr.getCell("crh_lamount").set(lamount);
//									BiResult sr = popupBr.getSubLink("erpv4.CrdAr");
//									for(CrdDetail cd : crdarr ) {
//										CellCollection col = sr.newRowCollection();
////										col.getCell("crd_cocode").set("001");
////										col.getCell("crd_crno").set("");
//										col.getCell("crd_sno").set(cd.sno);
////										col.getCell("crd_vcode").set(cd.vcode);
//										col.getCell("crd_amount").set(cd.amount);
//										col.getCell("crd_cid").set(cd.cid);
//										col.getCell("crd_xrate").set(cd.xrate);
//										col.getCell("crd_lamount").set(cd.lamount);
//										Object tr = sr.addSubRecord(col);
//									}
//								} catch (CellException cex){
//									UniLog.log(cex);
//								}
							}
							@Override
							public void biBaseClose(BiResult p_br) {
								// TODO Auto-generated method stub
								refresh(pbr,null);
							}
							@Override
							public ReturnMsg fetchNext(BiResult p_br) {
								// TODO Auto-generated method stub
								return null;
							}
							@Override
							public ReturnMsg fetchPrevious(BiResult p_br) {
								// TODO Auto-generated method stub
								return null;
							}
							@Override
							public String getExtraInfo() {
								// TODO Auto-generated method stub
								return null;
							}
							@Override
							public Boolean hasNextRec() {
								// TODO Auto-generated method stub
								return null;
							}
							@Override
							public Boolean hasPrevRec() {
								// TODO Auto-generated method stub
								return null;
							}
							@Override
							public HashSet<BiColumn> getVisibleColumns(BiResult p_br) {
								// TODO Auto-generated method stub
								return null;
							} }
						);
				// TODO Auto-generated constructor stub
				crdarr = new ArrayList<CrdDetail>();
			}
			@Override
			protected void beforePopup(int p_mode,BiResult p_br) {
				UniLog.log("before popup");
								try {
//									RpcClient rpc = popupBr.getSelectUtil().getRpcClient();
//									rpc.callSegment("setCocodeBaseccy",
//										new VectorUtil()
//										.addElement("001")
//										.addElement("MOP")
//										.toVector()
//									);
									popupBr.getCell("crh_cocode").set(cocode);
									String defaultBankAc = Erpv4Config.getString(getSessionHelper(), "DefaultBankAc");
									if(defaultBankAc == null) defaultBankAc = "";
									popupBr.getCell("crh_module").set(module);
//									popupBr.getCell("crh_dbinputano").set("1-2100");
									popupBr.getCell("crh_dbinputano").set(defaultBankAc);
									popupBr.getCell("crh_date").set(DateUtil.today());
									popupBr.getCell("crh_vcode").set(custCode);
									popupBr.getCell("crh_cid").set(cid);
									popupBr.getCell("crh_amount").set(module.equals("AR") ? amount : -amount);
//									popupBr.getCell("crh_lamount").set(module.equals("AR") ? lamount : -lamount);
									BiResult sr = popupBr.getSubLink("erpv4.CrdAr");
									for(CrdDetail cd : crdarr ) {
										CellCollection col = sr.newRowCollection();
//										col.getCell("crd_cocode").set("001");
//										col.getCell("crd_crno").set("");
//										col.getCell("crd_sno").set(cd.sno);
//										col.getCell("sih_vcode").set(cd.vcode);
										col.getCell("sih_sno").set(cd.sno);
										col.getCell("crd_cid").set(cd.cid);
										col.getCell("crd_xrate").set(cd.xrate);
//										col.getCell("crd_amount").set(-cd.amount);
//										col.getCell("crd_lamount").set(-cd.lamount);
										col.getCell("crd_settleamount").set(module.equals("AR") ? cd.amount : -cd.amount);
//										col.getCell("crd_drcr").set(cd.amount >= 0 ? "CR" : "DR");
										ReturnMsg rtn = sr.addSubRecord(col,"");
										Object tr = rtn.getData();
									}
									((BiResultCrhAr) popupBr).calPaymentAmountX();
								} catch (CellException cex){
									UniLog.log(cex);
								}
			}

			void clearCrh() {
				custCode = null;
				cocode = null;
				cid = null;
				crdarr.clear();
				amount = 0.0;
				lamount = 0.0;
			}
//			public String getCustCode() {
//				return custCode;
//			}
//			public void setCustCode(String custCode) {
//				this.custCode = custCode;
//			}
			public boolean isMultiVcode() {
				return ("".equals(custCode));
			}
			public boolean isMultiCid() {
				return ("".equals(cid));
			}
			public int getCrdCount() {
				return(crdarr.size());
			}
			public void addOnePayment(String p_cocode,String p_custcode,String p_sno,String p_cid,double p_amount,double p_xrate, double p_lamount) throws Exception {
				if(cocode == null) {
					cocode = p_cocode;
				} else {
					if(!custCode.equals(p_custcode)) {
//						throw new Exception("Cannot Create Payment for Multiple Company");
						custCode = "";
					}
				}
				if(cid == null) {
					cid = p_cid;
				} else {
					if(!cid.equals(p_cid)) {
						cid = "";
						amount = 0;
					}
				}
				if(custCode == null) {
					custCode = p_custcode;
				} else {
					if(!custCode.equals(p_custcode)) {
						custCode = "";
					}
				}
				CrdDetail cd = new CrdDetail();
				cd.vcode = p_custcode;
				cd.sno = p_sno;
				cd.cid = p_cid;
				cd.amount = p_amount;
				cd.xrate = p_xrate;
				cd.lamount = p_lamount;
				crdarr.add(cd);
				if(!isMultiCid()) amount += p_amount;
				lamount += p_lamount;
			}
		}
		PopupCrh zkcrhpo = null;

		@Override
	    protected void setupExportButton(final BiResult result) {
			Button btnDoPayment;
			super.setupExportButton(result);
			if(!result.allowUpdate() &&
			   !result.allowDetail()) return;
	    	if(masterWin.hasFellow("btPayment")) {
	    		btnDoPayment = (Button) masterWin.getFellow("btPayment");
	    	} 
	    	else {	
		        btnDoPayment = new ZkBiButton();
		        btnDoPayment.setLabel("Payment");
		        btnDoPayment.setId("btPayment");
		        btnDoPayment.setAttribute("tlkey", "btPayment");
		        //batchActionBar.appendChild(btnPrintLabel);
		        abHelper.addButton(btnDoPayment, "fa-user");
	    	} 
	        btnDoPayment.addEventListener("onClick",
	            new EventListener() {
	            	public void onEvent(Event event) throws Exception {
	            		java.util.Set selection = listModelList.getSelection();
	            		RpcClient rpc = sessionHelper.getRpcClient();
	        			Vector args = new Vector();
//	        			args.add(DateUtil.now());
//	        			String custCode = null;
	        			if(selection.size() <= 0) {
	      					Messagebox.show(
	   							"Please Select Invoice to Pay",
	   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	       					return;
	        			}
	            		if(zkcrhpo == null) {
//	            			zkcrhpo= new PopupCrh(masterWin,sessionHelper,"erpv4.CrhAr",result);
	            			zkcrhpo= new PopupCrh(masterWin,sessionHelper,paymentViewId,result);
	            		}
	            		zkcrhpo.clearCrh();
//	        			final CellCollectionArPayment col = new CellCollectionArPayment();
	            		for(Iterator it=selection.iterator();it.hasNext();) {
	        				Object o = it.next();
	        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
	        				UniLog.log("Create Payment for " + idx);
	        				result.loadOneRecV(idx);
//	        				String s = result.getCell("sih_vcode").getString();
//	        				if(!zkcrhpo.isMultiVcode() && zkcrhpo.getCustCode() == null) {
//	        						zkcrhpo.setCustCode(s);
//	        				} else {
//	        					if(!zkcrhpo.getCustCode().equals(s)) {
//	        						if(Erpv4Config.allowMultipleCustomerPayment(sessionHelper,result.getSelectUtil())) {
//	        							zkcrhpo.setCustCode("");
//	        						} else {
//	        						Messagebox.show(
//	    								"Create Payment Failed : Cannot create Payment for multiple customter",
//	    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//	        						return;
//	        						}
//	        					}
//	        				}
	        				zkcrhpo.addOnePayment(
	        							result.getCell("sih_cocode").getString(),
	        							result.getCell("sih_vcode").getString(),
	        							result.getCell("sih_sno").getString(),
	        							result.getCell("sih_cid").getString(),
	        							result.getCell("sih_osbal").getDouble(),
	        							result.getCell("sih_xrate").getDouble(),
	        							result.getCell("sih_losbal").getDouble()
	        							);
	        							
	            		}
	            		if(zkcrhpo.getCrdCount() <= 0) {
	        				Messagebox.show(
	        					"Create D/N Failed : No Outstanding Item in selected list",
	        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	        				return;
	            		}
	            		if(zkcrhpo.isMultiVcode()) {
	        				if(!Erpv4Config.allowMultipleCustomerPayment(sessionHelper,result.getSelectUtil())) {
	        				Messagebox.show(
	        					"Multiple Customer Payment Not Supported",
	        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	        				return;
	        				}
	            		}
	            		if(zkcrhpo.isMultiCid()) {
	        				Messagebox.show(
	        					"Multiple Currency Payment Not Supported",
	        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	        				return;
	            		}
//		        		zkf1.doModalPayment(col);
	            		zkcrhpo.popUp(JxZkBiBase.MODE_ADD, "");
	            	}
	        	}
	        );
	    	setupBatchModeButton(btnDoPayment);
	    	if(!getSessionHelper().hasAccessRight("#postarap")) return;
	        Button btnSihPost,btnSihUnpost;
	    	
			if(!result.allowUpdate()) return;
	    	if(masterWin.hasFellow("btSihPost")) {
	    		btnSihPost = (Button) masterWin.getFellow("btSihPost");
	    	} 
	    	else {	
		        btnSihPost = new ZkBiButton();
		        btnSihPost.setLabel("Post");
		        btnSihPost.setId("btSihPost");
		        //batchActionBar.appendChild(btnPrintLabel);
		        abHelper.addButton(btnSihPost, "fa-user");
	    	} 
	    	if(masterWin.hasFellow("btSihUnPost")) {
	    		btnSihUnpost = (Button) masterWin.getFellow("btSihUnPost");
	    	} 
	    	else {	
		        btnSihUnpost = new ZkBiButton();
		        btnSihUnpost.setLabel("UnPost");
		        btnSihUnpost.setId("btSihUnPost");
		        //batchActionBar.appendChild(btnPrintLabel);
		        abHelper.addButton(btnSihUnpost, "fa-user");
	    	} 
	        btnSihPost.addEventListener("onClick",
	            new EventListener() {
	            	public void onEvent(Event event) throws Exception {
	            		java.util.Set selection = listModelList.getSelection();
	        			Vector args = new Vector();
	        			if(selection.size() <= 0) {
	      					Messagebox.show(
	   							"Please Select Transaction to Post",
	   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	       					return;
	        			}
//	        			final CellCollectionArPayment col = new CellCollectionArPayment();
	        			result.beginWork();
	        			try {
	        			RpcClient rpc = result.getSelectUtil().getRpcClient();
						Value v;
	            		for(Iterator it=selection.iterator();it.hasNext();) {
	        				Object o = it.next();
	        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
	        				result.loadOneRecV(idx);
							v = rpc.callSegment("setCocodeBaseccy",
								new VectorUtil()
								.addElement(
											result.getCellString("sih_cocode")
									)
								.addElement(
										Erpv4Config.getBaseCcy(getSessionHelper(),result.getCellString("sih_cocode"))
										)
								.toVector()
								);
	        				v = rpc.callSegment("erpv4_postartogl",
	        						new VectorUtil()
	        						.addElement(module)
	        						.addElement(result.getCellString("sih_sno"))
	        						.toVector()
	        						);
	        				if(v == null || !v.toString().startsWith("OK")) {
	        					Messagebox.show( "Post Error "+(v == null ? "null" : v.toString()), "Error", Messagebox.OK, Messagebox.ERROR);
	        					result.rollbackWork();
	        					return;
	        				}
	        							
	            		}
	            		result.commitWork();
						refresh(result,null);
	        			} catch (Exception ex) {
	        				UniLog.log(ex);
	        				result.rollbackWork();
	        				throw(ex);
	        			}
	            	}
	        	}
	        );
	        btnSihUnpost.addEventListener("onClick",
	            new EventListener() {
	            	public void onEvent(Event event) throws Exception {
	            		java.util.Set selection = listModelList.getSelection();
	        			Vector args = new Vector();
	        			if(selection.size() <= 0) {
	      					Messagebox.show(
	   							"Please Select Transaction to UnPost",
	   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	       					return;
	        			}
//	        			final CellCollectionArPayment col = new CellCollectionArPayment();
	        			result.beginWork();
	        			try {
	        			RpcClient rpc = result.getSelectUtil().getRpcClient();
						Value v;
	            		for(Iterator it=selection.iterator();it.hasNext();) {
	        				Object o = it.next();
	        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
	        				result.loadOneRecV(idx);
							v = rpc.callSegment("setCocodeBaseccy",
								new VectorUtil()
								.addElement(
//											Erpv4Config.getCoCode(getSessionHelper()) 
											result.getCellString("sih_cocode")
									)
								.addElement(
										Erpv4Config.getBaseCcy(getSessionHelper(),result.getCellString("tr_cocode"))
										)
								.toVector()
								);

	        				v = rpc.callSegment("erpv4_unpostartogl",
	        						new VectorUtil()
	        						.addElement(result.getCellString("sih_cocode"))
	        						.addElement(result.getCellString("sih_sno"))
	        						.toVector()
	        						);
	        				if(v == null || !v.toString().startsWith("OK")) {
	        					Messagebox.show( "UnPost Error "+(v == null ? "null" : v.toString()), "Error", Messagebox.OK, Messagebox.ERROR);
	        					result.rollbackWork();
	        					return;
	        				}
	            		}
	            		result.commitWork();
						refresh(result,null);
	        			} catch (Exception ex) {
	        				UniLog.log(ex);
	        				result.rollbackWork();
	        				throw(ex);
	        			}
	            	}
	        	}
	        );
		        
		        
		    	setupBatchModeButton(btnSihPost);
		    	setupBatchModeButton(btnSihUnpost);
	    	
		}
	    
	    protected void setupExportButtonXX(final BiResult result)
		{
			Button btnDoPayment;
			super.setupExportButton(result);
			if(!result.allowUpdate() &&
			   !result.allowDetail()) return;

			/*
	    	if(masterWin.hasFellow("btPayment")) {
	    		btnDoPayment = (Button) masterWin.getFellow("btPayment");
	    	} 
	    	else {	
		        btnDoPayment = new ZkBiButton();
		        btnDoPayment.setLabel("Payment");
		        btnDoPayment.setId("btPayment");
		        batchActionBar.appendChild(btnDoPayment);
	    	} 
	    	*/
	    	if(masterWin.hasFellow("btPayment")) {
	    		btnDoPayment = (Button) masterWin.getFellow("btPayment");
	    	} 
	    	else {	
		        btnDoPayment = new ZkBiButton();
		        btnDoPayment.setLabel("Payment(XXX)");
		        btnDoPayment.setId("btPayment");
		        btnDoPayment.setAttribute("tlkey", "btPayment");
		        //batchActionBar.appendChild(btnPrintLabel);
		        abHelper.addButton(btnDoPayment, "fa-user");
	    	} 
//	        final ZkfArPayment zkf1 = new ZkfArPayment(null,result.getSelectUtil());
	        btnDoPayment.addEventListener("onClick",
	            new EventListener() {
	            	public void onEvent(Event event) throws Exception {
	            		java.util.Set selection = listModelList.getSelection();
	            		RpcClient rpc = sessionHelper.getRpcClient();
	        			Vector args = new Vector();
//	        			args.add(DateUtil.now());
//	        			String custCode = null;
	        			if(selection.size() <= 0) {
	      					Messagebox.show(
	   							"Please Select Invoice to Pay",
	   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	       					return;
	        			}
	            		if(zkcrhpo == null) {
//	            			zkcrhpo= new PopupCrh(masterWin,sessionHelper,"erpv4.CrhAr",result);
	            			zkcrhpo= new PopupCrh(masterWin,sessionHelper,paymentViewId,result);
	            		}
	            		zkcrhpo.clearCrh();
//	        			final CellCollectionArPayment col = new CellCollectionArPayment();
	            		for(Iterator it=selection.iterator();it.hasNext();) {
	        				Object o = it.next();
	        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
	        				UniLog.log("Create Payment for " + idx);
	        				result.loadOneRecV(idx);
//	        				String s = result.getCell("sih_vcode").getString();
//	        				if(!zkcrhpo.isMultiVcode() && zkcrhpo.getCustCode() == null) {
//	        						zkcrhpo.setCustCode(s);
//	        				} else {
//	        					if(!zkcrhpo.getCustCode().equals(s)) {
//	        						if(Erpv4Config.allowMultipleCustomerPayment(sessionHelper,result.getSelectUtil())) {
//	        							zkcrhpo.setCustCode("");
//	        						} else {
//	        						Messagebox.show(
//	    								"Create Payment Failed : Cannot create Payment for multiple customter",
//	    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
//	        						return;
//	        						}
//	        					}
//	        				}
	        				zkcrhpo.addOnePayment(
	        							result.getCell("sih_cocode").getString(),
	        							result.getCell("sih_vcode").getString(),
	        							result.getCell("sih_sno").getString(),
	        							result.getCell("sih_cid").getString(),
	        							result.getCell("sih_osbal").getDouble(),
	        							result.getCell("sih_xrate").getDouble(),
	        							result.getCell("sih_losbal").getDouble()
	        							);
	        							
	            		}
	            		if(zkcrhpo.getCrdCount() <= 0) {
	        				Messagebox.show(
	        					"Create D/N Failed : No Outstanding Item in selected list",
	        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	        				return;
	            		}
	            		if(zkcrhpo.isMultiVcode()) {
	        				if(!Erpv4Config.allowMultipleCustomerPayment(sessionHelper,result.getSelectUtil())) {
	        				Messagebox.show(
	        					"Multiple Customer Payment Not Supported",
	        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	        				return;
	        				}
	            		}
	            		if(zkcrhpo.isMultiCid()) {
	        				Messagebox.show(
	        					"Multiple Currency Payment Not Supported",
	        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	        				return;
	            		}
//		        		zkf1.doModalPayment(col);
	            		zkcrhpo.popUp(JxZkBiBase.MODE_ADD, "");
	            	}
	        	}
	        );
	        
	        Button btnSihLock,btnSihUnlock;
		if(!result.allowUpdate()) return;
    	if(masterWin.hasFellow("btSihLock")) {
    		btnSihLock = (Button) masterWin.getFellow("btSihLock");
    	} 
    	else {	
	        btnSihLock = new ZkBiButton();
	        btnSihLock.setLabel("Lock");
	        btnSihLock.setId("btSihLock");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnSihLock, "fa-user");
    	} 
    	if(masterWin.hasFellow("btSihUnLock")) {
    		btnSihUnlock = (Button) masterWin.getFellow("btSihUnLock");
    	} 
    	else {	
	        btnSihUnlock = new ZkBiButton();
	        btnSihUnlock.setLabel("UnLock");
	        btnSihUnlock.setId("btSihUnLock");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnSihUnlock, "fa-user");
    	} 
        btnSihLock.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		java.util.Set selection = listModelList.getSelection();
        			Vector args = new Vector();
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Transaction to Lock",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
//        			final CellCollectionArPayment col = new CellCollectionArPayment();
        			result.beginWork();
        			try {
        			RpcClient rpc = result.getSelectUtil().getRpcClient();
					Value v;
            		for(Iterator it=selection.iterator();it.hasNext();) {
        				Object o = it.next();
        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
        				result.loadOneRecV(idx);
						v = rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
										result.getCellString("sih_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),result.getCellString("sih_cocode"))
									)
							.toVector()
							);
        				v = rpc.callSegment("erpv4_postartogl",
        						new VectorUtil()
        						.addElement(module)
        						.addElement(result.getCellString("sih_sno"))
        						.toVector()
        						);
        				if(v == null || !v.toString().startsWith("OK")) {
        					Messagebox.show( "Lock Error "+(v == null ? "null" : v.toString()), "Error", Messagebox.OK, Messagebox.ERROR);
        					result.rollbackWork();
        					return;
        				}
        							
            		}
            		result.commitWork();
					refresh(result,null);
        			} catch (Exception ex) {
        				UniLog.log(ex);
        				result.rollbackWork();
        				throw(ex);
        			}
            	}
        	}
        );
        btnSihUnlock.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		java.util.Set selection = listModelList.getSelection();
        			Vector args = new Vector();
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Transaction to UnLock",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
//        			final CellCollectionArPayment col = new CellCollectionArPayment();
        			result.beginWork();
        			try {
        			RpcClient rpc = result.getSelectUtil().getRpcClient();
					Value v;
            		for(Iterator it=selection.iterator();it.hasNext();) {
        				Object o = it.next();
        				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
        				result.loadOneRecV(idx);
						v = rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
//										Erpv4Config.getCoCode(getSessionHelper()) 
										result.getCellString("sih_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),result.getCellString("tr_cocode"))
									)
							.toVector()
							);

        				v = rpc.callSegment("erpv4_unpostartogl",
        						new VectorUtil()
        						.addElement(result.getCellString("sih_cocode"))
        						.addElement(result.getCellString("sih_sno"))
        						.toVector()
        						);
        				if(v == null || !v.toString().startsWith("OK")) {
        					Messagebox.show( "UnLock Error "+(v == null ? "null" : v.toString()), "Error", Messagebox.OK, Messagebox.ERROR);
        					result.rollbackWork();
        					return;
        				}
        							
            		}
            		result.commitWork();
					refresh(result,null);
        			} catch (Exception ex) {
        				UniLog.log(ex);
        				result.rollbackWork();
        				throw(ex);
        			}
            	}
        	}
        );
	        
	        
	    	setupBatchModeButton(btnDoPayment);
	    	setupBatchModeButton(btnSihLock);
	    	setupBatchModeButton(btnSihUnlock);
		}

	@Override
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
//			zkfName = "zkf/erpv4/ArApInvoice.zul";
			super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
			
	}
//	@Override
//	protected void createZkfCollection(BiResult p_result) {
//		super.createZkfCollection(p_result);
//	    	if(p_result.getView().getTable().getName().equals("sih_ar")) {
//	    		rptCol.addCell("rpttitle",new Cell( "Receivable Invoices",Cell.VMODE_NORMAL));
//	    	}
//	    	if(p_result.getView().getTable().getName().equals("sih_ap")) {
//	    		rptCol.addCell("rpttitle",new Cell( "Payable Invoices",Cell.VMODE_NORMAL));
//	    	}
//	}	

	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
    	hasAUDColumn=true;
		super.doAfterCompose(comp);
   	}


	@Override
    public boolean doBrowseItemSelected(XulElement p_win, BiResult p_result)
    {
    	if(!p_result.allowDetail()) return(true);
   		return(doUpdateOneRow(p_win,p_result)) ;
    }
}
