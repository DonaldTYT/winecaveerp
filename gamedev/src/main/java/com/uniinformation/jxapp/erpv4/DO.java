package com.uniinformation.jxapp.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultDO;
import com.uniinformation.bicore.erpv4.BiResultInvoice;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class DO extends JxZkBiBase {
	protected GipiNamedItemList prdList;
	protected String detViewName=null;
	protected String prtDoSegName=null;
	Vector <String> cgList = null;
	protected class DoGetItemProperty extends ZkBiGetItemProperty {
public DoGetItemProperty(BiResult p_br, JxZkBiBase p_bibase) {
			super(p_br, p_bibase);
			// TODO Auto-generated constructor stub
		}
//		AfsIcodePicker jxf = null;
		JxSelOpt tjxf = null;
		JxSelOpt sjxf = null;
		TrGetItemProperty tgipi;
		TrGetItemProperty sgipi;
		/*
		DoGetItemProperty(BiResult br) {
			super(br);
		}
		*/
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) {
				setDirtyFlag(true);
			} else {
				if(bcc.getBiColumn().getLabel().equals("stmd_ref")) {
					ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
					Component comp = zcvm.getComponent();
					if(comp instanceof Combobox) {
						Combobox cb = (Combobox) comp;
						List cl = cb.getItems();
						if(cl.size() <= 0) {
							for(String lstr : cgList) {
								cb.appendItem(lstr);
							}
						}
					}
				}
				if(bcc.getCellLabel().equals("stmd_ref4")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						if(zcvm.getComponent() instanceof ZkJxPickInput) {
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("200px");
						}
					}
				}
				if(bcc.getCellLabel().equals("inv_invno")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("800px");
					}
				}
			}
			if(p_ctype == GIPI_PULLDOWN_CLOSED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
			}
			if(p_ctype == GIPI_PULLDOWN_OPENED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("stmd_ref4"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(sjxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							sgipi = new TrGetItemProperty(
										new VectorUtil()
											.addElement("stsn_ref4")
											.addElement("stsn_nqty")
											.toVector()
									);
							sjxf = JxSelOpt.createJxSelOpt(pvdr);
//							sjxf.setWidth("200px");
							sjxf.setOnSelectAction (
									new JxActionListener() {
										public void actionPerformed(JxField fd) {
											Object[] rec = (Object[]) fd.getValue();
											TableRec tr = sgipi.getTableRec();
											try {
												CellCollection ccx = (CellCollection) sjxf.getUserData();
												ccx.getCell("stmd_ref4").update(rec[tr.getFieldIndex("stsn_ref4")]);
											} catch (CellException cex ) {  
												UniLog.log(cex);
											}
											sjxf.closeForm();
										}
									}
							);
						}
//						zjpi.setPopupWidth("200px");
//						zjpi.setPopupHeight("300px");
						zjpi.setJxZkForm(sjxf);
						TableRec tr = getBr().getSelectUtil().getQueryResult("select * from stockserial"
								+ " where stsn_irg = " 
								+ bcc.getCollection().getCell("stmd_irg").getInt() 
								+ " and stsn_org = " 
								+ bcc.getCollection().getCell("stmd_org").getInt() 
								+ " and stsn_nqty > 0",null);
						sgipi.setTableRec(tr);
						sjxf.setUserData(bcc.getCollection());
						sjxf.jxAdd("pickListBox").setItemListInterface(sgipi);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
					
				}
				if(bcc.getBiColumn().getLabel().equals("inv_invno"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(tjxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
//							Idspace ids = new Idspace();
//							Listbox lb = new Listbox();
//							lb.setHeight("500px");
//							lb.setWidth("100%");
//							lb.setId("pickListBox");
//							ids.appendChild(lb);
//							jxf = getOrCreateJxZkForm(ids,pvdr ,"JxSelOpt");
							tgipi = new TrGetItemProperty(
										new VectorUtil()
											.addElement("inv_vcode")
											.addElement("inv_invno")
											.addElement("st_icode")
											.addElement("stm_ref1")
											.addElement("palc_delqty")
											.toVector()
									);
							tjxf = JxSelOpt.createJxSelOpt(pvdr);
//							tjxf.setWidth("780px");
							tjxf.setOnSelectAction (
									new JxActionListener() {
										public void actionPerformed(JxField fd) {
//											Vector selList = fd.getSelectList();
//											for(Object o : selList) {
//												int idx = tgipi.getIndexOf(o);
//												UniLog.log("Item " + idx + " selected");
//												
//											}
											Object[] rec = (Object[]) fd.getValue();
											TableRec tr = tgipi.getTableRec();
											try {
												CellCollection ccx = (CellCollection) tjxf.getUserData();
												ccx.getCell("stmd_qorg").update(rec[tr.getFieldIndex("palc_qorg")]);
												ccx.getCell("stmd_qirg").update(rec[tr.getFieldIndex("palc_qirg")]);
												ccx.getCell("stmd_org").update(rec[tr.getFieldIndex("palc_org")]);
												ccx.getCell("stmd_irg").update(rec[tr.getFieldIndex("palc_irg")]);
												ccx.getCell("stmd_qty").update(rec[tr.getFieldIndex("palc_delqty")]);
												if(ccx.testCell("stmd_entryqty") != null) {
													double r = (Double) rec[tr.getFieldIndex("ind_stqty")];
													r /= (Double) rec[tr.getFieldIndex("ind_qty")];
													ccx.getCell("stmd_entryqty").update( ccx.getCell("stmd_qty").getDouble() / r);
													ccx.getCell("stmd_eratio").update( r);
													ccx.getCell("stmd_entryunit").update( rec[tr.getFieldIndex("ind_unit")]);
												}
											} catch (CellException cex ) {  
												UniLog.log(cex);
											}
											tjxf.closeForm();
										}
									}
							);
						}
//						zjpi.setPopupWidth("800px");
//						zjpi.setPopupHeight("500px");
						
						zjpi.setJxZkForm(tjxf);
						String qstr =
								"select * from poallocate , quodet , quotation, stock , outer (stmovd , stmov )"
								+ "where palc_delqty > 0 and ind_odrg = palc_qorg and inv_rg = ind_rg and st_irg = palc_irg and stmd_org = palc_org and stmd_irg = palc_irg and stmd_tdtype in ("+Erpv4Config.STOCKIN_TDtypes+") and stm_mrg = stmd_mrg  and stmd_org < 2000000000 ";
						if(!Erpv4Config.allowMultipleCustomerDN(sessionHelper, getBr().getSelectUtil()) ||
								!getBr().getCell("stm_ref2").isBlank()) {
							qstr += " and inv_vcode = '"+ getBr().getCell("stm_ref2").getString() + "'";
						}
						if(Erpv4Config.isMultiCompany(getSessionHelper())) {
							qstr += " and " +"inv_cocode ='" +  getBr().getCellString("stm_cocode") + "'";
						}
						String ss = Erpv4Config.getString(getSessionHelper(), "RequireLoc") ;
						if(ss != null && ss.equals("Y")) {
							qstr += " and " +"palc_loc='" +  getBr().getCellString("stm_fromloc") + "'";
						}
						TableRec tr = getBr().getSelectUtil().getQueryResult(qstr,null);
						tgipi.setTableRec(tr);
						tjxf.setUserData(bcc.getCollection());
						tjxf.jxAdd("pickListBox").setItemListInterface(tgipi);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
			} 
		}
	}
	
	@Override
	public void afterBind() {
		super.afterBind();
		detViewName = "erpv4.DoDet";
		prtDoSegName= "erpv4_print_do";
		new JxFieldChange("vd_vname") {
			public boolean valueChanged(JxField fd,String orgValue){  
				try {
					TableRec tr = getBr().getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + getBr().getCell("stm_ref2").getString() + "'", null);
						Vector v = new Vector();
						for(int i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							if(getBr().getSessionHelper().getLHLang().equals("SCHN")) {
								String lstr = tr.getFieldString("svloc_desp");
								lstr = ChineseConvert.convertAuto2Gnew(lstr);
								v.add(lstr);
							} else {
								v.add(tr.getField("svloc_desp"));
							}
						}
						getBr().getCell("svloc_desp").setItemList(v);
					} catch (Exception ex){ 
						UniLog.log(ex);
					}	
				return(true);
			}
		};
		new JxFieldAction("btPrint") {
			public void actionPerformed(JxField fd){
						UniLog.log("print Pressed");
				if(!getBr().getCell("stm_status").equals("Confirmed")) {
						messageBox(sessionHelper.getLabel("Please confirm before print Invoices"));
						return;
				}
				RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement("c:\\images\\").toVector());
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(getSessionHelper().getWebContentRealPath("images", true)).toVector());
				
				String paperType = Erpv4Config.getString(getSessionHelper(),"DnPaperType");
				
				if(paperType == null) {
					paperType = "A4P";
				}
				String cocode = null;
				if(getBr().getCell("stm_cocode") != null)  {
					cocode = getBr().getCellString("stm_cocode");
				} else {
					cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
				}
				val = rpc.callSegment("setCocodeBaseccy",
									new VectorUtil()
									.addElement(cocode)
									.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),cocode))
									.toVector()
									);
				
				val = rpc.callSegment(prtDoSegName,
							new VectorUtil()
							.addElement(getBr().getCell("stm_mrg").getInt())
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement(paperType)
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
						);
				rpc.close();
				if(val != null && val.toString().startsWith("OK")) {
					String fname = val.toString().substring(4);
					UniLog.log("Print Do got " + fname);
					try {
//					ZkUtil.print((Component) (jxAdd("detail_grid").getNativeObject()));	
						InputStream is = erpFileInputStream(fname);
						int cc = ChnftrParser.getPaperTypeIndex(paperType);
						ChnftrParser ps;
						if( cc >= 0) {
							ps = new ChnftrParser(is,"-p"+cc);
						} else {
							ps = new ChnftrParser(is,"'");
						}
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						/*
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						*/
						/*
						{
							Collection<Component> comps = Executions.getCurrent().getDesktop().getComponents();
							for (Component comp : comps) {
								if (comp instanceof Window) {
									ZkUtil.showPdfDialog(comp, getSessionHelper(), bos.toByteArray(), "PackingList");
									break;
								}
							}
						}
						*/
						
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Invoice");
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					if(val != null) 
						messageBox(sessionHelper.getLabel("Print D/N Error") + ": " + val.toString());
					else
						messageBox(sessionHelper.getLabel("Print D/N Error") + ": Unknown");
				}
			}
		};
		new JxFieldAction("btPrintCInv") {
			public void actionPerformed(JxField fd){
						UniLog.log("print Pressed");
				RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(getSessionHelper().getWebContentRealPath("images", true)).toVector());
				/*
				val = rpc.callSegment("erpv4SetImageDir",
							new VectorUtil()
							.addElement("c:\\images\\")
							.toVector()
						);
				*/
				String addr = getBr().getCell("svloc_addr1").getString() + " " + getBr().getCell("svloc_addr2").getString() + " " + getBr().getCell("svloc_city").getString();
				addr = addr.replaceAll("\\r\\n|\\r|\\n", "");
				List<String> addrList = ChnftrParser.splitText(addr, "helv_nr", "chinese", 10, 450);
				String wrapAddr = StringUtils.join(addrList, "\n");
				val = rpc.callSegment("erpv4_print_cinv",
							new VectorUtil()
							.addElement(getBr().getCell("stm_mrg").getInt())
							.addElement(wrapAddr)
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
					UniLog.log("Print Do got " + fname);
					try {
//					ZkUtil.print((Component) (jxAdd("detail_grid").getNativeObject()));	
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"'");
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					if(val != null) 
						messageBox(sessionHelper.getLabel("Print Commerical Invoice Error") + ": " + val.toString());
					else
						messageBox(sessionHelper.getLabel("Print Commerical Invoice Error") + ": Unknown");
				}
			}
		};
		new JxFieldAction("btPrintInv") {
			public void actionPerformed(JxField fd){
						UniLog.log("print Pressed");
				if(getBr().getCell("stm_ref3") != null) {
				if(getBr().getCell("stm_ref3").getString().equals("")) {
						messageBox(sessionHelper.getLabel("Please Update and Save Invoice Number before Print Invoice"));
						return;
				}
				}
				RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(getSessionHelper().getWebContentRealPath("images", true)).toVector());
				/*
				val = rpc.callSegment("erpv4SetImageDir",
							new VectorUtil()
							.addElement("c:\\images\\")
							.toVector()
						);
				*/
				/*
				String addr = getBr().getCell("svloc_addr1").getString() + " " + getBr().getCell("svloc_addr2").getString() + " " + getBr().getCell("svloc_city").getString();
				addr = addr.replaceAll("\\r\\n|\\r|\\n", "");
				List<String> addrList = ChnftrParser.splitText(addr, "helv_nr", "chinese", 10, 450);
				String wrapAddr = StringUtils.join(addrList, "\n");
				*/
				
				/*
				if(getBr().getCell("stm_ref3").getString().equals("")) {
					try {
						SelectUtil su = new SelectUtil();
						su.init(getBr().getView().getConn());
						String s = BiResultAfsDO.newInvCode(su, DateUtil.today(), "API");
						su.executeUpdate("update stmov set stm_ref3 = '"+s+"' where stm_mrg = "+ getBr().getCell("stm_mrg").getInt(),null );
						getBr().getCell("stm_ref3").set(s);
						su.close();
					} catch (Exception ex){
						UniLog.log(ex);
					}
				}
				*/
				
//				val = rpc.callSegment(prtDoSegName,
				val = rpc.callSegment("erpv4_print_inv",
							new VectorUtil()
							.addElement(getBr().getCell("stm_mrg").getInt())
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
					UniLog.log("Print Do got " + fname);
					try {

//					ZkUtil.print((Component) (jxAdd("detail_grid").getNativeObject()));	
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"'");
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Dnote-"+getBr().getCell("stm_ref1").getString());
						/*
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						*/
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					if(val != null) 
						messageBox(sessionHelper.getLabel("Print Invoice Error") + ": " + val.toString());
					else
						messageBox(sessionHelper.getLabel("Print Invoice Error") + ": Unknown");
				}
			}
		};

		new JxFieldAction("btPrintInvNew") {
			public void actionPerformed(JxField fd){
				// TODO Auto-generated method stub
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ReturnMsg rtn = ((BiResultDO) getBr()).printInvoice(bos,null);
				if(rtn.getStatus()) {
					String ss = getBr().getCellString("stm_ref3");
					if(ss == null || ss.isEmpty()) {
						ss = getBr().getCellString("stm_ref1");
					}
					if(ss == null || ss.isEmpty()) {
						ss = "";
					}
					ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Invoice-"+ss);
				} else {
					Messagebox.show(rtn.getMsg());
				}
			}
		};
		new JxFieldAction("btPrintDnNew") {
			public void actionPerformed(JxField fd){
				// TODO Auto-generated method stub
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ReturnMsg rtn = ((BiResultDO) getBr()).printDnote(bos);
				if(rtn.getStatus()) {
					ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Dnote-"+getBr().getCell("stm_ref1").getString());
				} else {
					Messagebox.show(rtn.getMsg());
				}
			}
		};
		LOCK_RECORD_FOR_UPDATE = true;
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		boolean isNew;
		isNew = getGipi(detViewName) == null;
		if(isNew) {
			setGipi(detViewName,new DoGetItemProperty(p_br.getSubLink(detViewName),this));	
		}
		super.bindCellCollection(p_br, mode);
		cgList = new Vector();
		try {
			TableRec tr = p_br.getSelectUtil().getQueryResult("select distinct stmd_ref from stmovd where stmd_tdtype ='SO' order by 1", null);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				cgList.add((String) tr.getField("stmd_ref"));
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		
//		if(isNew) jxAdd("list_AfsDoDet").setAttribute("paging", "withfilter");
		
		if(mode == JxZkBiBase.MODE_ADD) {
				getBr().getCell("svloc_desp").setItemList(new Vector());
		} 
		if(mode == JxZkBiBase.MODE_UPDATE) { 
			try {
			TableRec tr = getBr().getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + getBr().getCell("stm_ref2").getString() + "'", null);
				Vector v = new Vector();
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
							if(getBr().getSessionHelper().getLHLang().equals("SCHN")) {
								String lstr = tr.getFieldString("svloc_desp");
								lstr = ChineseConvert.convertAuto2Gnew(lstr);
								v.add(lstr);
							} else {
								v.add(tr.getField("svloc_desp"));
							}
				}
				getBr().getCell("svloc_desp").setItemList(v);
			} catch (Exception ex){ 
				UniLog.log(ex);
			}
		}
		setupActionButton(false);
		setupSalesCode(p_br);
	}
	
	protected void setupSalesCode(BiResult p_br) {
		try {
		SelectUtil su = p_br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from prdsrvmaster");
			prdList = new GipiNamedItemList();
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				prdList.appendItem( tr.getFieldString("pds_ano"), tr.getFieldString("pds_desc"));
			}
			String ss = Erpv4Config.getString(getSessionHelper(), "freeSalesCode");
			if(!"Y".equals(ss)) {
			if(p_br.getCell("stm_salescode1") != null) p_br.getCell("stm_salescode1").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode2") != null) p_br.getCell("stm_salescode2").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode3") != null) p_br.getCell("stm_salescode3").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode4") != null) p_br.getCell("stm_salescode4").setItemPropertyInterface(prdList);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	void setupActionButton(boolean is_dirty)
	{
	}
	@Override 
	protected void formDirtyChanged() {
		super.formDirtyChanged();
		setupActionButton(isDirty());
	}
}
