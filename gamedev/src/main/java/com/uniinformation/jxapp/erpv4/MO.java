package com.uniinformation.jxapp.erpv4;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultMO;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocClass;
//import com.uniinformation.utils.AbstractGetItemProperty;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;
import com.uniinformation.zkf.ZkForm;

public class MO extends JxZkBiBase {
	protected String icodePickerViewName = null;
	protected String detViewId = null;
	protected boolean canCreateStock = false;
	class MoGetItemProperty extends ZkBiGetItemProperty {
		IcodePicker jxf = null;
		JxSelOpt jxfInvNo = null;
		JxForm serialNoInput = null;
		TrGetItemProperty tgipiInvNo;
		TrGetItemProperty sgipi;
		class SerialNoInputListener implements JxActionListener {

			ColumnCell bcc;
			@Override
			public void actionPerformed(JxField field) {
				// TODO Auto-generated method stub
				if(bcc != null) {
					String expdate = StringUtil.strpart(serialNoInput.jxGetText("inp_expdate"),0,10);
					String lotno   = serialNoInput.jxGetText("inp_lotno");
					try {
						bcc.set(""+expdate.trim()+":"+lotno.trim());
					} catch (CellException cex) {
						UniLog.log(cex);
					}
					serialNoInput.closeForm();
				}
				
			}
			
		}
		SerialNoInputListener serialNoInputListener;
//		Vector listColumns=null;
//		Template detailTemplate=null;
		MoGetItemProperty(BiResult br,JxZkBiBase p_base) {
			super(br,p_base);
			/*
			detailTemplate = ((Component) getNativeComponent()).getTemplate("template_"+replaceViewName(br.getView().getName()));
			if(detailTemplate != null) {
				listColumns = new Vector();
				listColumns.add(detailTemplate);
			}
			*/
		}
		/*
		@Override
		public Object getHeader(Object p_v,int p_col) {
			if(detailTemplate == null)return(super.getHeader(p_v, p_col));
			return("Details");
		}	
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			if(detailTemplate == null) return(super.getColumnWidth(p_v, p_col));
			return("100%");
		}
		
		@Override
		public Object getColumnValue(Object p_v,int p_col) {
			if(detailTemplate == null) return(super.getColumnValue(p_v, p_col));
			return(detailTemplate);
		}	
		@Override
		public int getColumnSpan(Object p_v,int p_col) {
			if(detailTemplate == null) return(super.getColumnSpan(p_v, p_col));
			return(1);
		}
		@Override
		protected Vector getListColumns(Object p_v) {
			if(detailTemplate == null) return(super.getListColumns(p_v));
			return(listColumns);
		}	
		*/
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) setDirtyFlag(true); else {
				detailCellMapped(bcc);
				if(bcc.getCellLabel().equals("inv_invno")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("500px");
					}
				}
				if(bcc.getCellLabel().equals("st_icode")) {
					if(!sessionHelper.isMobileDevice() || getListColumns(null).size() > 1){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(sessionHelper.isMobileDevice())
							zjpi.setPopupWidth("300px");
						else
							zjpi.setPopupWidth("700px");
					}
				}
				if(bcc.getCellLabel().equals("or_ocode")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("700px");
					}
				}
				if(bcc.getCellLabel().equals("stmd_ref4")) {
					if(!sessionHelper.isMobileDevice() || getListColumns(null).size() > 1){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						if(zcvm.getComponent() instanceof ZkJxPickInput) {
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(sessionHelper.isMobileDevice())
							zjpi.setPopupWidth("250px");
						else
							zjpi.setPopupWidth("500px");
						}
					}
				}

				if(bcc.getCellLabel().equals("stmd_uprice")) {
						final ColumnCell uprice = (ColumnCell) bcc.getCollection().getCell("stmd_uprice");
						final ColumnCell amount = (ColumnCell) bcc.getCollection().getCell("stmd_exprice");
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						Component comp =  zcvm.getComponent();
						comp.addEventListener(Events.ON_CLICK, 
							new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
									UniLog.log("amount field clicked");
									ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) amount.getMapper();
									if(zcvm != null) {
									InputElement c_amount = (InputElement) zcvm.getComponent();
									zcvm = (ZkBiCellValueMapper) uprice.getMapper();
									InputElement c_uprice = (InputElement) zcvm.getComponent();
									if(c_uprice.isReadonly()) {
										c_amount.setReadonly(true);
										c_uprice.setReadonly(false);
//										uprice.resetValue();
										amount.resetValue();
									}
									}
								}
							}
						);
				}
				if(bcc.getCellLabel().equals("stmd_exprice")) {
						final ColumnCell uprice = (ColumnCell) bcc.getCollection().getCell("stmd_uprice");
						final ColumnCell amount = (ColumnCell) bcc.getCollection().getCell("stmd_exprice");
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						Component comp =  zcvm.getComponent();
						comp.addEventListener(Events.ON_CLICK, 
							new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
									UniLog.log("amount field clicked");
									ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) amount.getMapper();
									InputElement c_amount = (InputElement) zcvm.getComponent();
									zcvm = (ZkBiCellValueMapper) uprice.getMapper();
									InputElement c_uprice = (InputElement) zcvm.getComponent();
									if(c_amount.isReadonly()) {
										c_amount.setReadonly(false);
										c_uprice.setReadonly(true);
									}
								}
							
							}
						);
				}
			}
			if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED) {
				detailCellChange(bcc);
			}
			if(p_ctype == GIPI_PULLDOWN_CLOSED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
			}
			if(p_ctype == GIPI_PULLDOWN_OPENED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("inv_invno"))  {
						UniLog.log("set popup pick quotation item for tradein here, refer to pmsdemo:com.uniinformation.jxapp.AfsDo.java, except that the pick condition is ind_tiqty > 0");				
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxfInvNo == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							tgipiInvNo = new TrGetItemProperty(
										new VectorUtil()
											.addElement("inv_vcode")
											.addElement("inv_invno")
											.addElement("st_icode")
//											.addElement("ind_rg")
//											.addElement("ind_odrg")
											.toVector(),
										new VectorUtil()
											.addElement("Customer Code")
											.addElement("S.O. Number")
											.addElement("Item Code")
											.toVector()
									);
							jxfInvNo = JxSelOpt.createJxSelOpt(pvdr);
//							jxfInvNo.setWidth("480px");
							jxfInvNo.setOnSelectAction (
									new JxActionListener() {
										public void actionPerformed(JxField fd) {
											//Object[] rec = (Object[]) fd.getValue();
											Object[] rec = (Object[]) jxfInvNo.getPickListBoxValue();
											TableRec tr = tgipiInvNo.getTableRec();
											if (rec != null) {
												try {
													CellCollection ccx = (CellCollection) jxfInvNo.getUserData();
//													ccx.getCell("inv_invno").update(rec[tr.getFieldIndex("inv_invno")]);
//													ccx.getCell("ind_rg").update(rec[tr.getFieldIndex("ind_rg")]);
													ccx.getCell("stmd_qorg").update(rec[tr.getFieldIndex("ind_odrg")]);
													ccx.getCell("stmd_qirg").update(rec[tr.getFieldIndex("ind_irg")]);
													ccx.getCell("stmd_irg").update(rec[tr.getFieldIndex("ind_irg")]);
												} catch (CellException cex ) {  
													UniLog.log(cex);
												} 
												jxfInvNo.closeForm();
											}
										}
									}
							);
						}
//						zjpi.setPopupWidth("500px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxfInvNo);
						TableRec tr = getBr().getSelectUtil().getQueryResult("select distinct inv_vcode, inv_invno, ind_rg, ind_odrg , ind_irg, st_icode from tradein, quodet, quotation, stock "
								+ "where trin_nqty > 0 "
								+ " and ind_odrg = trin_qorg and ind_irg = trin_irg "
								+ " and inv_rg = ind_rg "
								+ " and st_irg = ind_irg ", null);
						tgipiInvNo.setTableRec(tr);
						jxfInvNo.setUserData(bcc.getCollection());
						jxfInvNo.jxAdd("pickListBox").setItemListInterface(tgipiInvNo);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
				
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							jxf = (IcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,icodePickerViewName);
						}
//						AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
//						zjpi.setPopupWidth("700px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
						CellCollection col = bcc.getCollection();
						int qorg = 0;
						if(col.testCell("stmd_qorg") != null) qorg = col.getCell("stmd_qorg").getInt();
						Wherecl wcl = null;
						if (qorg > 0) {
							wcl = new Wherecl().appendString(String.format(" and st_irg in (select ind_irg from tradein, quodet where trin_nqty > 0 and trin_qorg = %d and ind_odrg = trin_qorg) ", qorg));
						} else {
							String tdtype = col.getCellString("stmd_tdtype");
							if(		
									tdtype.equals("KO")
								    || tdtype.equals("MO")
								    || tdtype.equals("RO")
								    || tdtype.equals("JO")) {
								Cell loc = col.testCell("stmd_loc");
								Cell bin = col.testCell("stmd_bin");
								Cell nref4 = col.testCell("stmd_nref4");
								if(loc != null && !loc.getString().equals("")) {
									if(nref4 != null && nref4.getInt() == 1 && tdtype.equals("KO")) {
										if(wcl == null) wcl = new Wherecl();
										String tloc = col.getCellString("stmdki_loc");
										String tbin = col.getCellString("stmdki_bin");
										wcl.appendString(String.format(" and st_irg in (select pdlbs_irg from podetlocbinstatus where pdlbs_loc = '%s' and pdlbs_bin = '%s' and pdlbs_stockqty > 0) ", tloc,tbin));
									} else {
									if(!Erpv4Config.getLocationAllowNegative(getSessionHelper(), loc.getString())) {
										if(wcl == null) wcl = new Wherecl();
										wcl.appendString(String.format(" and st_irg in (select pdls_irg from podetlocstatus where pdls_loc = '%s' and pdls_stockqty > 0) ", loc.getString()));
									}
									if(Erpv4Config.isMultiCompany(getSessionHelper())) {
										String dftCocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
										if(wcl == null) wcl = new Wherecl();
										if("Y".equals(Erpv4Config.getString(getSessionHelper(), "UseStockValidFlag"))) {
											if(Erpv4Config.isMultiStockPrice(getSessionHelper())) {
												int lcrg = Erpv4Config.getDefaultLcrg(getSessionHelper());
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s' and st_lcrg = %d and st_issalable = 'Y') ", dftCocode,lcrg));
											} else {
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s' and st_issalable = 'Y') ", dftCocode));
											}
											wcl.andUniop("st_obsolete", "<>", "Y");
										} else {
											if(Erpv4Config.isMultiStockPrice(getSessionHelper())) {
												int lcrg = Erpv4Config.getDefaultLcrg(getSessionHelper());
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s' and st_lcrg = '%s') ", dftCocode,lcrg));
											} else {
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s') ", dftCocode));
											}
										}
									} else {
										if("Y".equals(Erpv4Config.getString(getSessionHelper(), "UseStockValidFlag"))) {
											if(wcl == null) wcl = new Wherecl();
											wcl.andUniop("st_issalable", "=", "Y");
											wcl.andUniop("st_obsolete", "<>", "Y");
										}
									}
									}
									/*
									if(!Erpv4Config.getLocationAllowNegative(getSessionHelper(), loc.getString())) {
										wcl = new Wherecl().appendString(String.format(" and st_irg in (select pdls_irg from podetlocstatus where pdls_loc = '%s' and pdls_stockqty > 0) ", loc.getString()));
									} else {
//										wcl = new Wherecl().appendString(String.format(" and st_irg in (select pdls_irg from podetlocstatus where pdls_loc = '%s') ", loc.getString()));
										if(Erpv4Config.isMultiCompany(getSessionHelper())) {
											wcl = new Wherecl().appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s') ", col.getCellString("stm_cocode")));
										}
									}
									*/
								}
								/*
								if(bin != null) {
									if(nref4 != null && nref4.getInt() == 1 && tdtype.equals("KO")) {
										loc = col.getCell("stmdki_loc");
										bin = col.getCell("stmdki_bin");
									} 
									String allowNegativeStock = Erpv4Config.getString(getSessionHelper(), "AllowNegativeStock");
									if(allowNegativeStock == null || !allowNegativeStock.equals("Y")) {
										wcl = new Wherecl().appendString(String.format(" and st_irg in (select pdlbs_irg from podetlocbinstatus where pdlbs_loc = '%s' and pdlbs_bin='%s' and pdlbs_stockqty > 0) ", loc.getString(),bin.getString()));
									}
								}
								*/
							} else {
								/*
								if(Erpv4Config.isMultiCompany(getSessionHelper())) {
									if(Erpv4Config.isMultiStockPrice(getSessionHelper())) {
										wcl = new Wherecl().appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s') ", col.getCellString("stm_cocode")));
									} else {
										wcl = new Wherecl().appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s') ", col.getCellString("stm_cocode")));
									}
								}
								*/
									if(Erpv4Config.isMultiCompany(getSessionHelper())
									   && Erpv4Config.isMultiStockPrice(getSessionHelper())
											) {
										String dftCocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
										if(wcl == null) wcl = new Wherecl();
										if("Y".equals(Erpv4Config.getString(getSessionHelper(), "UseStockValidFlag"))) {
											if(Erpv4Config.isMultiStockLoc(getSessionHelper())) {
												int lcrg = Erpv4Config.getDefaultLcrg(getSessionHelper());
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s' and st_lcrg = %d and st_issalable = 'Y') ", dftCocode,lcrg));
											} else {
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s' and st_issalable = 'Y') ", dftCocode));
											}
											wcl.andUniop("st_obsolete", "<>", "Y");
										} else {
											if(Erpv4Config.isMultiStockLoc(getSessionHelper())) {
												int lcrg = Erpv4Config.getDefaultLcrg(getSessionHelper());
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s' and st_lcrg = '%s') ", dftCocode,lcrg));
											} else {
												wcl.appendString(String.format(" and st_irg in (select st_stirg from costock where st_cocode = '%s') ", dftCocode));
											}
										}
									} else {
										if("Y".equals(Erpv4Config.getString(getSessionHelper(), "UseStockValidFlag"))) {
											if(wcl == null) wcl = new Wherecl();
											wcl.andUniop("st_issalable", "=", "Y");
											wcl.andUniop("st_obsolete", "<>", "Y");
										}
									}
							}
						}
									if(!StringUtils.isBlank(col.getCellString("stmd_ref4"))) {
										if(wcl == null) {
											wcl = new Wherecl();
										}
										wcl.appendString(" and st_irg in (select stsn_irg from stockserial where stsn_nqty > 0 and stsn_ref4 = '"+
												col.getCellString("stmd_ref4").trim()+"')"
										);
									}
						jxf.setIsMobile(sessionHelper.isMobileDevice());
						jxf.setCanCreateStock(canCreateStock);
						jxf.setShowOicode(true);
						
						jxf.setPickerForAnyStock(MO.this,getBr().getSelectUtil(), wcl,bcc,null,null);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
				if(bcc.getBiColumn().getLabel().equals("or_ocode"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							jxf = (IcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,icodePickerViewName);
						}
//						AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
//						zjpi.setPopupWidth("700px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
						CellCollection col = bcc.getCollection();
						int irg = col.getCell("stmd_irg").getInt();
						Wherecl wcl = null;
						if (irg > 0) {
							wcl = new Wherecl().appendString(" and st_irg = "+irg);
//							jxf.setPickerForAvailableStock(AfsMO.this,br.getSelectUtil(), wcl,null,bcc,bcc.getCollection().getCell("stmd_qty"),0,0,0);
									if(!StringUtils.isBlank(col.getCellString("stmd_ref4"))) {
										if(wcl == null) {
											wcl = new Wherecl();
										}
										wcl.appendString(" and pds_org in (select stsn_org from stockserial where stsn_nqty > 0 and stsn_ref4 = '"+
												col.getCellString("stmd_ref4").trim()+"')"
										);
									}
							jxf.setIsMobile(sessionHelper.isMobileDevice());
							jxf.setCanCreateStock(false);
							jxf.setPickerForAvailableStock(MO.this,getBr().getSelectUtil(), wcl,null,bcc.getCollection().getCell("stmd_org"),null,0,0,0);
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				if(bcc.getBiColumn().getLabel().equals("stmd_ref4"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(sgipi == null) {
							sgipi = new TrGetItemProperty(
										new VectorUtil()
											.addElement("stsn_ref4")
											.toVector()
									);
						}
						if(jxfInvNo == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							jxfInvNo = JxSelOpt.createJxSelOpt(pvdr);
							jxfInvNo.setOnSelectAction (
									new JxActionListener() {
										public void actionPerformed(JxField fd) {
											Object[] rec = (Object[]) fd.getValue();
											TableRec tr = sgipi.getTableRec();
											try {
												CellCollection ccx = (CellCollection) jxfInvNo.getUserData();
												ccx.getCell("stmd_ref4").update(rec[tr.getFieldIndex("stsn_ref4")]);
											} catch (CellException cex ) {  
												UniLog.log(cex);
											}
											jxfInvNo.closeForm();
										}
									}
							);
							String serialNoInputFormName = Erpv4Config.getString(getSessionHelper(), "SerialNoInputForm");
							if(serialNoInputFormName != null) {
								Idspace ids = new Idspace();
								serialNoInput = JxZkBiBase.getOrCreateJxZkForm(ids,pvdr ,serialNoInputFormName);
								if(serialNoInput != null) {
									serialNoInput.jxAdd("inp_expdate");
									serialNoInput.jxAdd("inp_lotno");
									serialNoInputListener = new SerialNoInputListener();
									JxField inpOk = serialNoInput.jxAdd("inp_ok");
									if(inpOk != null) {
										inpOk.addActionListener(serialNoInputListener);
									}
								}
							}
						}
						CellCollection col = bcc.getCollection();
						String tdtype = col.getCellString("stmd_tdtype");
						boolean useSerialNoInputForm = false;
						if(serialNoInput != null) {
						Cell nref4 = col.getCell("stmd_nref4");
						if(tdtype.equals("KO") && nref4.getInt() == 1) {
							useSerialNoInputForm = true;
						}
						if(tdtype.equals("MI") /* && nref4.getInt() == 1 */) {
							useSerialNoInputForm = true;
						}
						}
						if(useSerialNoInputForm) {
							serialNoInputListener.bcc = bcc;
							zjpi.setJxZkForm(serialNoInput);
						} else {
							zjpi.setJxZkForm(jxfInvNo);
							int irg = bcc.getCollection().getCell("stmd_irg").getInt() ;
							int org = bcc.getCollection().getCell("stmd_org").getInt() ;
							TableRec tr;
							if(irg == 0 && org == 0) {
								tr = getBr().getSelectUtil().getQueryResult("select * from stockserial"
									+ " where stsn_ref4 > '!'" 
									+ " and stsn_nqty > 0",null);
								sgipi.setTableRec(tr);
							} else {
								tr = getBr().getSelectUtil().getQueryResult("select * from stockserial"
									+ " where stsn_irg = " 
									+ irg
									+ " and stsn_org = " 
									+ org
									+ " and stsn_nqty > 0",null);
								sgipi.setTableRec(tr);
							}
							jxfInvNo.setUserData(bcc.getCollection());
							jxfInvNo.jxAdd("pickListBox").setItemListInterface(sgipi);
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
					
				}
				
			} 
		}
	}
	
	protected void detailCellMapped(ColumnCell p_cc) {
	}
	protected void detailCellChange(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("stmd_exprice")) {
			UniLog.log("stmd_exprice changed");
			BiCellCollection bcc = p_cc.getCollection();
			Cell cUprice = bcc.testCell("stmd_uprice");
			Cell cQty = bcc.testCell("stmd_qty");
			if(cQty.getDouble() > 0) {
				try {
					cUprice.sync(p_cc.getDouble()/cQty.getDouble());
				} catch (CellException cex) {
					UniLog.log("Catch cell recurrsion exception");
				}
			}
		}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		LOCK_RECORD_FOR_UPDATE = true;
		icodePickerViewName = "erpv4.IcodePicker";
		new JxFieldAction("bt_scan") {
			public void actionPerformed(JxField fd) {
//				jxSetVisible("scr1",false);
//				jxSetVisible("scr2",true);
				UniLog.log("HAHA activate scan barcode");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
				if("WEBCAM".equals( getSessionHelper().getURLParam("BarcodeScanner"))) {
					ZkUtil.js("startWebcamScanner()");
				} else Clients.evalJavaScript("launchScanner()");
			}
		};
		new JxFieldAction("btScanE") {
			public void actionPerformed(JxField fd) {
//				jxSetVisible("scr1",false);
//				jxSetVisible("scr2",true);
				jxSetText("txNewBarcode","");
				UniLog.log("HAHA scan barcode catched");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
			   	Clients.evalJavaScript("closeScanner()");
			}
		};
		new JxFieldChange("txNewBarcode") {

			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				// TODO Auto-generated method stub
				String s = jxfield.getText();
				s = s.replaceAll("\\s+", "");
				UniLog.log("HAHA barcode value changed["+s+"]");
				try {
					SelectUtil su = getBr().getSelectUtil();
					/*
					TableRec tr = su.getQueryResult("select * from podetlocstatus where pdls_irg = ? and pdls_org = ? and pdls_loc = 'STOR' and pdls_stockqty > 0",
									new Wherecl().appendArgument(irg).appendArgument(org)
									);
					if(tr.getRecordCount() <= 0) throw new CellException("Record Not In Stock");
					
					UniLog.log("Do add detail");
					*/
					TableRec tr = su.getQueryResult("select * from stock where st_icode = ?", new Wherecl().appendArgument(s));
					if(tr.getRecordCount() <= 0) {
						tr = su.getQueryResult("select * from stock where st_barcode = ?", new Wherecl().appendArgument(s));
					}
					if(tr.getRecordCount() <= 0) throw new CellException("Record Not In Stock");
					/* addOneTransferDetail(irg,org);*/
					addOneMoDetail(tr.getFieldInt("st_irg"),Erpv4Config.useWeightedAverageOrg(sessionHelper) ? GenbucketUtil.WEIGHTED_AVERAGE_ORG : 0,1.0, tr.getFieldString("st_unit"));
					Clients.evalJavaScript("beep(1)");
					Clients.evalJavaScript("closeScanner()");
					JxField jl = jxAdd("bt_gotolast");
					if(jl != null) {
						Button bt = (Button) jl.getNativeObject();
						Events.echoEvent("onClick", bt, null);
					}
					return true;
				} catch (Exception ex) {
					UniLog.log("barcode invalid " + ex.toString() + "["+s+"]");
					if(ex instanceof CellException) ZkUtil.showMsg(ex.toString()); else ZkUtil.showMsg("Barcode Invalid (0)");
					Clients.evalJavaScript("beep(0)");
					return false;
					
				}
			}
			
		};
		{
			JxField jx = jxAdd("bt_scan");
			if(jx != null) {
				abHelper.addButton((Button) jx.getNativeObject(),false,true,"fa-print",-1);
			}
		}
		new JxFieldAction("bt_gotolast") {
			public void actionPerformed(JxField fd) {
				UniLog.log("HAHA gotolast");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
//			   	Clients.evalJavaScript("launchScanner()");
				BiResult sr = getBr().getSubLink(detViewId);
				JxField sv = jxAdd("list_"+replaceViewName(sr.getView().getName()));
				Listbox lb = (Listbox) sv.getNativeObject();
				ListModelList lm = (ListModelList) lb.getListModel();
				int n = lm.getSize();
				Listitem li = lb.getItemAtIndex(n-1);
				Clients.scrollIntoView(li);
						
//				CellCollection col = sr.newRowCollection();
//	Object tr = sr.addSubRecord(col, idx);
//		int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
			}
		};
		
		new JxFieldAction("btPrintVoucher") {
			public void actionPerformed(JxField fd) {
				String ss = Erpv4Config.getString(getSessionHelper(), "MoVoucher_"+getBr().getCellString("stm_module"));
				if(ss != null) {
					try {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						PrtdocClass jpi = null;
						Class[]	paramTypes = new Class[]{BiResultMO.class};
						jpi = (PrtdocClass) DynamicClassLoader.newInstance(ss, paramTypes,(BiResultMO) getBr());
						jpi.print();
						ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
						if(rtn != null && !rtn.getStatus()) {
							Messagebox.show(rtn.getMsg());
						} else {
							ss = getBr().getCellString("stm_ref1");
							ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), os.toByteArray(), "Voucehr-"+ss);
						}
					} catch (Exception ex){
						UniLog.log(ex);
					}
					return;
				}
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ReturnMsg rtn = ((BiResultStmov) getBr()).printVoucher(bos);
				if(rtn.getStatus()) {
					ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Voucher-"+getBr().getCell("stm_ref1").getString());
				} else {
					Messagebox.show(rtn.getMsg());
				}
			}
			
		};
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		boolean isNew;
		detViewId = ((BiResultMO) br).getStmdLinkName();
		isNew = getGipi(detViewId) == null;
//		if(getGipi("AfsMoDet") == null) {
		if(isNew) {
			BiGetItemProperty gipi = new MoGetItemProperty(br.getSubLink(detViewId),this);	
			setGipi(detViewId,gipi );	
			if(displayOnlyWhenUpdate && mode != MODE_ADD) {
				gipi.setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
			}
		}
		super.bindCellCollection(br, mode);

		/*
		if(jxAdd("pageTitle") != null) {
			jxSetText("pageTitle",br.getView().getHeader());
			ZkUtil.translateOneComp(br.getSessionHelper(),(Component) jxAdd("pageTitle").getNativeObject(),br.getView().getName(),br);
		}
		*/
//		if(isNew) {
////			jxAdd("list_AfsMoDet").setAttribute("paging", "withfilter");
//			jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).setAttribute("paging", "withfilter");
//			/*
//			BiResult sr = br.getSubLink("AfsMoDet");
//			if(sr.getRowCount() > 20) {
//				jxAdd("list_AfsMoDet").setAttribute("paging", "withfilter");
//			} else {
//				
//			}
//			*/
//		}
		/*
		if(mode == JxZkBiBase.MODE_ADD) {
			try {
				br.getCell("stm_date").set(DateUtil.today());
				br.getCell("stm_status").set("Confirmed");
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
		if(br.getCell("stm_cur") != null) {
		if(br.getCell("stm_cur").equals("")) {
			try {
				br.getCell("stm_cur").set("HKD");
			} catch (CellException cex ) {
				UniLog.log(cex);
			}
		}
		}
		*/
		
		
		BiResult sr = br.getSubLink(detViewId);
		/*
		if(sr.getRowCount() > 40) {
			jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).setAttribute("paging", "withfilter");
		} else {
			jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).setAttribute("paging", "disabled");
		}		
		*/
		if(!sessionHelper.isMobileDevice()) {
//			if(sessionHelper.useJxFormG2()) {
//				Listbox lb = (Listbox) jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).getNativeObject();
//				lb.setHeight("500px");
//			}
			if(!sessionHelper.useJxFormG2() || (sr != null && sr.getRowCount() > 20)) {
				jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).setAttribute("paging", "withfilter");
			} else {
				jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).setAttribute("nopaging",null);
			}
		} else {
    		int lh =sessionHelper.getScreenHeight()-600;
    		if(lh < 300) lh = 300;
			Listbox lb = (Listbox) jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).getNativeObject();
    		lb.setHeight(""+lh+"px");
		}
		Component comp = (Component) jxAdd("list_"+JxZkBiBase.replaceViewName(detViewId)).getNativeObject();
		comp.invalidate();
	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
//		return(br.doBeforeAdd(cl));
//		if(sr.getView().getTable().getName().equals("stmovd_any")) {
		if(sr.getView().getName().equals(detViewId)) {
			try {
				if(getBr().getCell("stm_module").getString().equals("cstmo")) {
					cl.getCell("stmd_tdtype").set("MO");
				} else if(getBr().getCell("stm_module").getString().equals("sttfr")) {
					cl.getCell("stmd_tdtype").set("KO");
				} else if(getBr().getCell("stm_module").getString().equals("vstmo")) {
					cl.getCell("stmd_tdtype").set("MI");
				} else if(getBr().getCell("stm_module").getString().equals("cotfr")) {
				} else {
					cl.getCell("stmd_tdtype").set("");
				}
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
		return(null);
	}
	protected CellCollection addOneMoDetail(int p_irg,int p_org,double p_qty,String p_unit) throws Exception {
		BiResult sr = getBr().getSubLink(detViewId);
		int idx = sr.getRowCount();
		JxField sv = jxAdd("list_"+replaceViewName(sr.getView().getName()));
		CellCollection col = sr.newRowCollection();
		ReturnMsg rtn = sr.addSubRecord(col, idx,"");
		Object tr = rtn.getData();
		int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
		sv.addItemToList(tr, rowIdx);
		if(col.getCellString("stm_module").equals("vstmo")) {
			col.getCell("stmd_tdtype").set("MI");
		} else if(col.getCellString("stm_module").equals("sttfr")) {
			col.getCell("stmd_tdtype").set("KO");
		} else if(col.getCellString("stm_module").equals("cotfr")) {
			col.getCell("stmd_tdtype").set("JO");
		} else {
			col.getCell("stmd_tdtype").set("MO");
		}
		col.getCell("stmd_irg").set(p_irg);
		col.getCell("stmd_org").set(p_org);
		col.getCell("stmd_entryqty").set(p_qty);
		col.getCell("stmd_entryunit").set(p_unit);
		/*
		ColumnCell bcc = (ColumnCell) col.getCell("st_icode");
		ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
		ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
		zjpi.setPopupWidth("100%");
		zjpi.setPopupHeight("500px");
		pickAvailableStock(bcc,zjpi,p_irg,p_org);
		*/
		return(col);
	}	
	
//	@Override
//	protected void linkDetailPickInputOpened(BiResult sr, JxField fd)
//	{
//		
//		if(!fd.getName().equals("list_AfsMoDet")) return;
//		Object o = fd.gridGetValue(fd.getCurrentCol() , -2 );
//		if( !(o instanceof ColumnCell) ) return;
//		BiColumn bc = ((ColumnCell) o).getBiColumn();
//		if(!bc.getLabel().equals("st_icode")) return;
//		
//	}
}
