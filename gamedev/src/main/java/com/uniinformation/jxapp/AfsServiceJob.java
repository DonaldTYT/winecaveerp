package com.uniinformation.jxapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zhtml.Fileupload;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listbox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.Sprintf;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.afs.BiResultAfsServiceJob;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;
import com.uniinformation.zkbi.wc.PrintBarcode;

public class AfsServiceJob extends JxZkBiBase {
	TrGetItemProperty tgipiServiceLoc;
	JxSelOpt jxfServiceLoc = null;
	TrGetItemProperty tgipiServiceItem;
	TrGetItemProperty tgipiPreset;
//	JxSelOpt jxfServiceItem = null;
	
	Hashtable<String,Integer> mcListHash=null;
	
	void setServiceMachineList(SelectUtil su,int locrg,String p_default) throws Exception {
						try {
							TableRec tr = getBr().getSelectUtil().getQueryResult(
								"select svmcd.svmc_rg,svmcd.svmc_serno,st_iname,st_modelno,stmcm_name,st_modelno "
								+ "from sv_machine svmcd,outer stock,outer (sv_machine svmcm,stmcmodel)"
								+ " where svmcd.svmc_locrg = " + locrg + " and st_irg = svmcd.svmc_irg "
								+ " and svmcm.svmc_rg = svmcd.svmc_mrg and stmcm_rg = svmcm.svmc_phrg"
							, null);
							Vector itemList = new Vector();
							Hashtable<String,Integer> tHash = new Hashtable<String,Integer>();
							for(int i=0;i < tr.getRecordCount();i++) {
								tr.setRecPointer(i);
//								BiResultAfsServiceJob xxx;
								String s = (((BiResultAfsServiceJob) getBr()).makeServiceMachineName(
											tr.getFieldInt("svmc_rg"),
											tr.getFieldString("st_modelno"),
											tr.getFieldString("svmc_serno"),
											tr.getFieldString("stmcm_name"),
											tr.getFieldString("st_iname")
											));
								itemList.add(s);
								tHash.put(s,tr.getFieldInt("svmc_rg"));
							}
							if(!StringUtils.isBlank(p_default)) {
								if(tHash.get(p_default) == null) {
									UniLog.log("Found on 20240729 Fix this issue later by DT");
									throw new Exception("Setup Machine List Error 01");
								}
							}
							mcListHash = tHash;
							JxField fd = jxAdd("st_iname");
							if(fd != null) {
								fd.setItemList(itemList);
								fd.setText(p_default);
							}
						} catch (Exception ex) {
							UniLog.log(ex);
						}
	}
	
	public void afterBind() {
		super.afterBind();
		JxField jxSvlocDesp = jxAdd("svloc_desp");
		if(jxSvlocDesp != null)  {
			Object o = jxSvlocDesp.getNativeObject();
			if(o instanceof Listbox) { 
				new JxFieldChange("st_iname") {
					@Override
					public boolean valueChanged(JxField jxfield, String orgvalue) {
						Integer mcrg;
						if(StringUtils.isBlank(jxfield.getText())) {
							mcrg = 0;
						} else {
							if(mcListHash == null || (mcrg = mcListHash.get(jxfield.getText())) == null) {
								return(false);
							}
						}
						try {
							getBr().getCell("svjob_mcrg").set(mcrg);
						} catch(Exception ex) {
							UniLog.log(ex);
							return(false);
						}
						return(true);
					}
					
				};
				new JxFieldChange("svloc_desp") {

					@Override
					public boolean valueChanged(JxField jxfield, String orgvalue) {
						SelectUtil su = getBr().getSelectUtil();
						int locrg = getBr().getCell("svmc_locrg").getInt();
						try {
							getBr().getCell("st_iname").update("");
							getBr().getCell("svjob_mcrg").update(0);
							setServiceMachineList(su,locrg,"");
						} catch (Exception ex) {
							UniLog.log(ex);
			    			ZkUtil.errMsg(ex.toString());
						}
						/*
						try {
							SelectUtil su = getBr().getSelectUtil();
							TableRec tr = getBr().getSelectUtil().getQueryResult(
								"select svmcd.svmc_rg,svmcd.svmc_serno,st_iname,st_modelno,stmcm_name,st_modelno "
								+ "from sv_machine svmcd,outer stock,outer (sv_machine svmcm,stmcmodel)"
								+ " where svmcd.svmc_locrg = " + getBr().getCell("svmc_locrg").getInt() + " and st_irg = svmcd.svmc_irg "
								+ " and svmcm.svmc_rg = svmcd.svmc_mrg and stmcm_rg = svmcm.svmc_phrg"
							, null);
							Vector itemList = new Vector();
							for(int i=0;i < tr.getRecordCount();i++) {
								tr.setRecPointer(i);
//								BiResultAfsServiceJob xxx;
								itemList.add(((BiResultAfsServiceJob) getBr()).makeServiceMachineName(tr.getFieldInt("svmc_rg")));
							}
							JxField fd = jxAdd("st_iname");
							if(fd != null) {
								fd.setItemList(itemList);
							}
							getBr().getCell("st_iname").update("");
						} catch (Exception ex) {
							UniLog.log(ex);
						}
						*/
						return true;
					}
					
				};
				
			} else {
		new JxFieldAction("svloc_desp") {
			public void actionPerformed(JxField fd){
				switch(fd.getActionType()) {
				case JxField.ACTIONTYPE_PICKINPUTOPENED:
					ZkJxPickInput zkpi  = (ZkJxPickInput) fd.getNativeObject();
					if(zkpi.isOpen()) {
						if(jxfServiceLoc == null) {
							createSelectopt();
						}
						if(tgipiServiceLoc == null) {
							tgipiServiceLoc = new TrGetItemProperty(
										new VectorUtil()
											.addElement("svloc_desp")
											.toVector(),
										new VectorUtil()
											.addElement("Location")
											.toVector(),
										new VectorUtil()
											.addElement("100%")
											.toVector()
									);
						}
						zkpi.setJxZkForm(jxfServiceLoc);
						if(isMobile){
//							zkpi.setPopupWidth("100%");
//							zkpi.setPopupHeight("300px");
							int cc = sessionHelper.getScreenWidth();
							if( cc <= 100) {
								zkpi.setPopupWidth("100%");
							} else {
								zkpi.setPopupWidth(""+(cc - 20)+"px");
							}
						} else {
							zkpi.setPopupWidth("500px");
							zkpi.setPopupHeight("500px");
						}
						try {
							TableRec tr = getBr().getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_rg in (select svmc_locrg from sv_machine) order by svloc_desp",null);
							tgipiServiceLoc.setTableRec(tr);
						} catch (Exception ex) {
							UniLog.log (ex);
						}
//						jxfServiceLoc.setUserData(bcc.getCollection());
						jxfServiceLoc.setUserData("svloc_desp");
						jxfServiceLoc.jxAdd("pickListBox").setItemListInterface(tgipiServiceLoc);
					}
					break;
				}
			}
		};		
		new JxFieldChange("st_iname") {

			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		new JxFieldAction("st_iname") {
			public void actionPerformed(JxField fd){
				switch(fd.getActionType()) {
				case JxField.ACTIONTYPE_PICKINPUTOPENED:
					ZkJxPickInput zkpi  = (ZkJxPickInput) fd.getNativeObject();
					if(zkpi.isOpen()) {
						if(jxfServiceLoc == null) {
							createSelectopt();
						}
						if( tgipiServiceItem == null) {
							tgipiServiceItem = new TrGetItemProperty(
										new VectorUtil()
											.addElement("sermod_name")
											.addElement("svmc_serno")
											.toVector(),
										new VectorUtil()
											.addElement("Series/Model")
											.addElement("S/N")
											.toVector(),
										new VectorUtil()
											.addElement("100%")
											.addElement("100px")
											.toVector()
									);
						}
						if(isMobile){
//							zkpi.setPopupWidth("100%");
//							zkpi.setPopupHeight("300px");
							int cc = sessionHelper.getScreenWidth();
							if( cc <= 100) {
								zkpi.setPopupWidth("100%");
							} else {
								zkpi.setPopupWidth(""+(cc - 20)+"px");
							}
						} else {
							zkpi.setPopupWidth("500px");
							zkpi.setPopupHeight("500px");
						}
						zkpi.setJxZkForm(jxfServiceLoc);
						try {
							TableRec tr = getBr().getSelectUtil().getQueryResult(
//									"select svmc_rg,svmc_serno,st_iname,st_modelno,stmcm_name from sv_machine,outer stock,outer stmcmodel"

//									"select svmc_rg,svmc_serno,st_iname,st_modelno,strcat(stmcm_name,' ',st_modelno) sermod_name from sv_machine,outer stock,outer stmcmodel"
//									+ " where svmc_locrg = " + getBr().getCell("svmc_locrg").getInt() + " and st_irg = svmc_irg and stmcm_rg = svmc_phrg"
									"select svmcd.svmc_rg,svmcd.svmc_serno,st_iname,st_modelno,strcat(stmcm_name,' ',st_modelno) sermod_name "
									+ "from sv_machine svmcd,outer stock,outer (sv_machine svmcm,stmcmodel)"
									+ " where svmcd.svmc_locrg = " + getBr().getCell("svmc_locrg").getInt() + " and st_irg = svmcd.svmc_irg "
									+ " and svmcm.svmc_rg = svmcd.svmc_mrg and stmcm_rg = svmcm.svmc_phrg"
									, null);
							tgipiServiceItem.setTableRec(tr);
						} catch (Exception ex) {
							UniLog.log (ex);
						}
						jxfServiceLoc.setUserData("st_iname");
						jxfServiceLoc.jxAdd("pickListBox").setItemListInterface(tgipiServiceItem);
					}
					break;
				}
			}
		};	
		new JxFieldAction("svjob_errorcode") {
			public void actionPerformed(JxField fd){
				switch(fd.getActionType()) {
				case JxField.ACTIONTYPE_PICKINPUTOPENED:
					ZkJxPickInput zkpi  = (ZkJxPickInput) fd.getNativeObject();
					if(zkpi.isOpen()) {
						if(jxfServiceLoc == null) {
							createSelectopt();
						}
						if(tgipiPreset == null) {
							tgipiPreset = new TrGetItemProperty(
										new VectorUtil()
											.addElement("pstd_str")
											.toVector(),
										new VectorUtil()
											.addElement("Preset Value")
											.toVector(),
										new VectorUtil()
											.addElement("100%")
											.toVector()
									);
						}
						if(isMobile){
//							zkpi.setPopupWidth("100%");
//							zkpi.setPopupHeight("300px");
							int cc = sessionHelper.getScreenWidth();
							if( cc <= 100) {
								zkpi.setPopupWidth("100%");
							} else {
								zkpi.setPopupWidth(""+(cc - 20)+"px");
							}
							
						} else {
							zkpi.setPopupWidth("500px");
							zkpi.setPopupHeight("500px");
						}
						zkpi.setJxZkForm(jxfServiceLoc);
						try {
							TableRec tr = getBr().getSelectUtil().getQueryResult("select * from presetmaster,presetdetail where "
									+ "pstm_key = '" + "SVST" + "' and pstd_mrg = pstm_rg order by pstd_seq",null);
							tgipiPreset.setTableRec(tr);
						} catch (Exception ex) {
							UniLog.log (ex);
						}
//						jxfServiceLoc.setUserData(bcc.getCollection());
						jxfServiceLoc.setUserData("svjob_errorcode");
						jxfServiceLoc.jxAdd("pickListBox").setItemListInterface(tgipiPreset);
//						jxfServiceLoc.beginPick();
					}
					break;
				}
			}
		};		
				

			}
		}
			
		new JxFieldAction("btClosePdf") {
			public void actionPerformed(JxField fd){
				UniLog.log("cloase view Pressed");
				jxAdd("pdfview").setVisible(false);
			}
		};
		new JxFieldAction("btView") {
			public void actionPerformed(JxField fd){
				UniLog.log("view Pressed");
				JxZkBiBase jxf = (JxZkBiBase) fd.getJxForm();
				BiResult br = jxf.getBr();
				SessionHelper sh = br.getSessionHelper();
				RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
				);
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(sh.getWebContentRealPath("images", true)) .toVector());
				val = rpc.callSegment("erpv4_print_serviceorder",
						new VectorUtil()
						.addElement(getBr().getCell("svjob_rg").getInt())
						.addElement("CHNPRINT")
						.addElement("VARIABLE")
						.addElement("A4P")
						.addElement("NORMAL")
						.addElement("LPTRAW")
						.toVector()
					);
				rpc.close();
				if(val != null && val.toString().startsWith("OK")) {
					String fname = val.toString().substring(4);
					UniLog.log("Print service order" + fname);
					try {
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"'");
						ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
						@Override
						public byte[] getImage(String p_key) {
							try{
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								FilingUtil.getFile(sessionHelper.getAgent(), null, p_key, bos);
								byte[] bytes = bos.toByteArray();
								bos.close();
								return(bytes);
							}
							catch(Exception ex){
								ex.printStackTrace();
								return(null);
							}
						}});
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
//						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						/*
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						*/
						
						/*
						jxAdd("pdfview").setVisible(true);
						String downloadLink = Sessions.getCurrent().getWebApp().getServletContext().getContextPath() + "/" + 
								ZkUtil.getDownloadLinkFromStream(bis,
										"application/pdf", 
										sessionHelper, 
										"JxZkTestEmbedPdf_stream",  //stream key
										"JxZkTestEmbedPdf_mimetype",  //mime key
										false);
						String jsString = String.format("zkDisplayPdf('%s','%s','%s');", downloadLink,"pdfcontent", "btDownloadPdf");
						UniLog.logm(this,"DEBUG:" + jsString);
						Clients.evalJavaScript(jsString);
						*/
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Service Order");
							
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
					
			}
		};
		new JxFieldAction("btUpload") {
			public void actionPerformed(JxField fd){
					UniLog.log("upload Pressed");
					try {
					    Fileupload.get(new EventListener <UploadEvent>(){
				    		public void onEvent(UploadEvent event) {
				        		UniLog.log("upload event catched");
				        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
				                org.zkoss.util.media.Media media = event.getMedia();
				                if(media != null) {
				                	if(!media.getContentType().equals("image/jpeg") &&
				                	   !media.getContentType().equals("image/png")) {
				                		messageBox("Only Jpeg/Png Image File Are Accepted");
//				                		return;
				                		int cc;
				                		cc = 0;
				                	}
				                	RpcClient rpc = getRpcClient();
				                	Value v = rpc.callSegment("getFilingMessageId",new Vector());
				                	rpc.close();
				                	if(v != null && v.toString().startsWith("OK")) {
				                		int cc = Integer.parseInt(v.toString().substring(4));
				                		try  {
				                			InputStream is = media.getStreamData();
				                			FilingUtil.storeFile(
				                					sessionHelper.getAgent(),
				                					null,
				                					new Sprintf("jxAfsServiceOrderFiling_%06d").add(cc).toString(),
				                					"",//mConditionPresetMapMap.customStoreName, 
				                					"",//mConditionPresetMapMap.customStoreDesc, 
				                					is);
				                		
				                			is.close();
				                			TableRec tr = getBr().getSelectUtil().getQueryResult(
				                					"select * from multidoc where mdoc_type = 'SODR' and mdoc_mrg = '" + getBr().getCell("svjob_rg").getInt() + "' order by mdoc_seq desc", null);
				                			int seq = 0;
				                			if(tr.getRecordCount() > 0) {
				                				tr.setRecPointer(0);
				                				seq = (Integer) tr.getField("mdoc_seq");
				                				seq++;
				                			}
				                			getBr().getSelectUtil().executeUpdate("insert into multidoc (mdoc_type,mdoc_mrg,mdoc_seq,mdoc_drg,mdoc_ctime,mdoc_cuser,mdoc_doctype) values (?,?,?,?,?,?,?)", 
				                					new Wherecl()
				                						.appendArgument("SODR")
				                						.appendArgument(getBr().getCell("svjob_rg").getInt())
				                						.appendArgument(seq)
				                						.appendArgument(cc)
				                						.appendArgument(DateUtil.dateToUnixtime(new java.util.Date()))
				                						.appendArgument(getLoginId())
				                						.appendArgument(media.getContentType())
				                					);
//				                			col.getCell("ind_messrg").set(cc);
//				                			col.getCell("ind_messagetype").set(media.getContentType());
				                			getBr().refetchCurrent();
				                			bindCellCollection(getBr(),curMode);
				                		} catch (Exception ex) {
				                			UniLog.log(ex);
				                		}
				                	}
				                }
				    		}
					    });
					} catch (Exception ex) {
							UniLog.log(ex);
					}
			}
		};
	}
	void createSelectopt() {
//		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
			jxfServiceLoc = JxSelOpt.createJxSelOpt(pvdr);
			
//		if(isMobile){
//			jxfServiceLoc.setWidth("100%");
//		} else {
//			jxfServiceLoc.setWidth("480px");
//		}
							
							jxfServiceLoc.setOnSelectAction (
									new JxActionListener() {
										public void actionPerformed(JxField fd) {
											Object[] rec = (Object[]) jxfServiceLoc.getPickListBoxValue();
											if (rec != null) {
												try {
													if(jxfServiceLoc.getUserData().equals("svloc_desp")) {
														TableRec tr = tgipiServiceLoc.getTableRec();
														getBr().getCell("svjob_mcrg").update(0);
														getBr().getCell("svloc_desp").update(rec[tr.getFieldIndex("svloc_desp")]);
													}
													if(jxfServiceLoc.getUserData().equals("st_iname")) {
														TableRec tr = tgipiServiceItem.getTableRec();
														getBr().getCell("svjob_mcrg").sync(rec[tr.getFieldIndex("svmc_rg")]);
													}
													if(jxfServiceLoc.getUserData().equals("svjob_errorcode")) {
														TableRec tr = tgipiPreset.getTableRec();
														getBr().getCell("svjob_errorcode").sync(rec[tr.getFieldIndex("pstd_str")]);
													}
												} catch (CellException cex ) {  
													UniLog.log(cex);
												} 
												jxfServiceLoc.closeForm();
											}
										}
									}
							);
			
	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
		if(sr.getView().getName().equals("AfsJobEngineer")) {
			try {
				SelectUtil su = sr.getSelectUtil();
				TableRec tr = su.getQueryResult(
						"select * from sv_engineer where svegr_code = '" + getLoginId() + "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					cl.getCell("svjobegr_egrrg").set(tr.getFieldInt("svegr_rg"));
//					cl.getCell("svjobegr_date").set(DateUtil.today());
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"Error Adding Engneer"));
			}
		}
		return(null);
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
//		ZkBiGetItemProperty.useGetItemPropertyForSubLink(p_br,this);
		super.bindCellCollection(p_br,mode);
		if(mode == JxZkBiBase.MODE_ADD) {
			try {
				p_br.getCell("svjob_stdate").set(DateUtil.today());
				listboxAddRow(this, p_br.getSubLink("AfsJobEngineer") , jxAdd("list_AfsJobEngineer"), null, -1);
//				SelectUtil su = p_br.getSelectUtil();
//				TableRec tr = su.getQueryResult(
//						"select * from sv_engineer where svegr_code = '" + getLoginId() + "'",null);
//				if(tr.getRecordCount() > 0) {
//					tr.setRecPointer(0);
//					CellCollection cl = p_br.getSubLink("AfsJobEngineer").getRowCollectionV(0);
//					cl.getCell("svjobegr_egrrg").set(tr.getFieldInt("svegr_rg"));
//					cl.getCell("svjobegr_date").set(DateUtil.today());
//				}
			} catch (Exception cex) {
				UniLog.log(cex);
			}
		} 
		if(mode == JxZkBiBase.MODE_UPDATE) { 
			SelectUtil su = p_br.getSelectUtil();
			int locrg = p_br.getCell("svmc_locrg").getInt();
			String st_iname = p_br.getCellString("st_iname");
			try {
				setServiceMachineList(su,locrg,st_iname);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			
		}
		JxField fd = jxAdd("svjob_errorcode");
		try {
		if(fd != null) {
			Object o = fd.getNativeObject();
			if(o instanceof Listbox) { 
				TableRec tr = p_br.getSelectUtil().getQueryResult("select * from presetmaster,presetdetail where "
									+ "pstm_key = '" + "SVST" + "' and pstd_mrg = pstm_rg order by pstd_seq",null);
				Vector v = new Vector();
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					v.add(tr.getFieldString("pstd_str"));
				}
				fd.setItemList(v);
			}
		}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		jxSetVisible("pdfview",false);
		setupActionButton(false);
		
	}	
	void setupActionButton(boolean is_dirty)
	{
		if(curMode == JxZkBiBase.MODE_ADD) {
			jxSetEnable("btUpload",false);
//			jxAdd("btUpload").setEnable(false);
			
			jxSetVisible("btUpload",false);
//			jxAdd("btUpload").setVisible(false);
			
			jxSetEnable("btView",false);
//			jxAdd("btView").setEnable(false);
			
			jxSetVisible("btView",false);
//			jxAdd("btView").setVisible(false);
		} 
		if(curMode == JxZkBiBase.MODE_UPDATE) { 
			jxSetVisible("btUpload",true);
//			jxAdd("btUpload").setVisible(true);
			jxSetVisible("btView",true);
//			jxAdd("btView").setVisible(true);
			if(is_dirty) {
			jxSetEnable("btUpload",false);
//			jxAdd("btUpload").setEnable(false);
			jxSetEnable("btView",false);
//			jxAdd("btView").setEnable(false);
			} else {
			jxSetEnable("btUpload",true);
//			jxAdd("btUpload").setEnable(true);
			jxSetEnable("btView",true);
//			jxAdd("btView").setEnable(true);
			}
		}
	}	
	
	@Override 
	protected void formDirtyChanged() {
		setupActionButton(isDirty());
	}	
	
	
	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		
		//handle Download button
		return ListUtil.of(
			new BiGetItemProperty(p_br.getSubLink("AfsSvDoc")) {
				@Override
				public void onValueChanged(Object p_value,int p_ctype) {
					ColumnCell bcc = (ColumnCell) p_value;
					/*
					if(p_ctype != BiGetItemProperty.GIPI_VALUE_CHANGED ) {
						if(bcc.getCellLabel().equals("mdoc_download")) {
							setDirtyFlag(true);
						}
					}
					*/
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("mdoc_download")){
						String doctype = bcc.getCollection().getCellString("mdoc_doctype");
						UniLog.log1("%s clicked %s", bcc.getCellLabel(),doctype);
						if(doctype != null &&
				                	!doctype.equals("image/jpeg") &&
				                	   !doctype.equals("image/png")) {
							ZkUtil.downloadFileFromFiling(sessionHelper,doctype,bcc.getCollection().getCell("mdoc_filekey").getString(), null);
						} else {
							ZkUtil.downloadFileFromFiling(sessionHelper,bcc.getCollection().getCell("mdoc_filekey").getString(), null);
						}
						/*
				                	if(!media.getContentType().equals("image/jpeg") &&
				                	   !media.getContentType().equals("image/png")) {
				                	   */
					}
				}
			}
		);	
		
	}	
	
}
