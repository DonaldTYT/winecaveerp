package com.uniinformation.jxapp.wc;

import java.util.ArrayList;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.JxZkSkin;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.PopupWindowAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.wc.PrintBarcode;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class Transfer extends JxZkBiBase {
	String transferDetailViewid = null;
	JxSelOpt jxfSelOptForm = null;
	JxSelOpt jxfPopupSelOptForm = null;
	JxSelOpt selopt = null;
	Window popupWin;
	ArrayList<String>popupItems = null;
	int popupRows = 200;
	ListModelList<String> listModelList = null;
	Listbox lb = null;
	TrGetItemProperty tgipiSelAvailableStock;
	TrGetItemProperty tgipiSelAvailableBin;
	JxSelOpt jxfSelBin;
	boolean usePopupSelectOpt = false;
	TrGetItemProperty tgipiSelBin;
	boolean inModalPopup = false;
	class TransferGetItemProperty extends BiGetItemProperty {
		Template transferDetailTemplate=null;
		Vector listColumns=null;
		public TransferGetItemProperty(BiResult p_br) {
			super(p_br);
			// TODO Auto-generated constructor stub
			transferDetailTemplate = ((Component) getNativeComponent()).getTemplate("template_Transfer_Detail");
			if(transferDetailTemplate != null) {
				listColumns = new Vector();
				listColumns.add(transferDetailTemplate);
			}
		}
		@Override
		public Object getHeader(Object p_v,int p_col) {
			if(transferDetailTemplate == null)return(super.getHeader(p_v, p_col));
			return("Service Item");
		}	
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			if(transferDetailTemplate == null) return(super.getColumnWidth(p_v, p_col));
			return("100%");
		}
		
		@Override
		public Object getColumnValue(Object p_v,int p_col) {
			if(transferDetailTemplate == null) return(super.getColumnValue(p_v, p_col));
			return(transferDetailTemplate);
		}	
		@Override
		public int getColumnSpan(Object p_v,int p_col) {
			if(transferDetailTemplate == null) return(super.getColumnSpan(p_v, p_col));
			return(1);
		}
		@Override
		protected Vector getListColumns(Object p_v) {
			if(transferDetailTemplate == null) return(super.getListColumns(p_v));
			return(listColumns);
		}	
		
		
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) {;
				setDirtyFlag(true);
			} else {
				if(bcc.getCellLabel().equals("st_icode")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("500px");
					}
				}
				if(bcc.getCellLabel().equals("stmdki_bin")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("200px");
					}
				}
			}
			switch(p_ctype ) {
			case GIPI_PULLDOWN_CLOSED :
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
					if(jxfSelOptForm != null) jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(null);	
				}
				break;
			case GIPI_PULLDOWN_OPENED:
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
						if(usePopupSelectOpt) {
							if(true) {
							if(popupWin == null) {
								popupItems = new ArrayList<String>();
								for(int i = 0;i<20000;i++) {
									popupItems.add("Item " + i);
								}
								popupWin = ZkUtil.newPopupWindow(sessionHelper.getLabel("Pick By Select"), 
								ZkUtil.getMainComp(),false,
								new PopupWindowAction() {

									@Override
									public void onClose() {
										// TODO Auto-generated method stub
										
									}
									
								}
								);
								lb = new Listbox();
								lb.setId("listbox");
								lb.setHeight("150px");
								lb.setParent(popupWin);
								listModelList = new ListModelList();
								lb.setModel(listModelList);
								ListitemRenderer listitemRenderer = new ListitemRenderer() {
									public void render(Listitem p_listItem, Object p_data, int p_idx) throws Exception {
										Listcell lc = new Listcell((String) p_data);
										lc.setParent(p_listItem);
										if(p_idx >= listModelList.size()-1) {
											int st = listModelList.size();
											for(int i = st;i<st+popupRows;i++) {
												if(i < popupItems.size()) listModelList.add(popupItems.get(i));
											}
											lb.invalidate();
										}
									}
								};
								lb.setItemRenderer(listitemRenderer);
								for(int i = 0;i<popupRows;i++) {
									if(i < popupItems.size()) listModelList.add(popupItems.get(i));
								}
//								ListModelArray<String> listModelArray = new ListModelArray(20000);
//								lb.setModel(listModelArray);
//								ListitemRenderer listitemRenderer = new ListitemRenderer() {
//									public void render(Listitem p_listItem, Object p_data, int p_idx) throws Exception {
//										Listcell lc = new Listcell((String) p_data);
//										lc.setParent(p_listItem);
//									}
//								};
//								lb.setItemRenderer(listitemRenderer);
//								for(int i = 0;i<20000;i++) {
//									listModelArray.getInnerArray()[i] = "Item" + i;
//								}
								lb.invalidate();
								
								
								/*
								for(int i = 0;i<10000;i++) {
									Listitem li = new Listitem();
									Listcell lc = new Listcell();
									lc.setLabel("Item " + i);
									lc.setParent(li);;
									li.setParent(lb);
								}
								*/
								lb.setParent(popupWin);
								popupWin.doHighlighted();
								popupWin.setPosition("middle_center"); 
								popupWin.setVisible(true);
							}
							}
							if(false) {
							if(selopt == null) {
								
								
								BiResult thisBr = getBr().getView().getSchema().getViewByName("wc.Stock").newBiResult( getLoginId(), null, null, sessionHelper);
								thisBr.query();
								final CellCollection thiscol = bcc.getCollection();
//								final JxSelOpt selopt = getPopupSelOpt();
								selopt = JxSelOpt.createPopupJxSelOpt(getSessionHelper());
								selopt.setOnSelectAction (
									new JxActionListener() {
										public void actionPerformed(JxField fd) {
											if (!checkBr()) return;
//											Object o  = fd.getValue();
//											BiGetItemProperty gipi = (BiGetItemProperty) selopt.getUserData();
//											CellCollection col = gipi.getCellCollectionByValue(o);
//											try {
//												thiscol.getCell("ind_postano").set(col.getCell("st_mszrange").getString());
//											} catch (CellException cex) {
//												UniLog.log(cex);
//											}
											selopt.closeForm();
										}
									}
								);
								BiGetItemProperty gipi = new BiGetItemProperty(thisBr);
								gipi.setItemMode(BiGetItemProperty.GETITEM_MODE_PICK);
								selopt.jxAdd("pickListBox").setItemListInterface( gipi);
								selopt.setUserData(gipi);
								selopt.setPopupWidth("500px");
							}
							selopt.modalForm();	
							}
						} else {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						pickAvailableStock(bcc,zjpi,-1,-1);
						}
				}
				if(bcc.getBiColumn().getLabel().equals("stmdki_bin"))  {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
//						if(isMobile) {
//						} else {
//							zjpi.setPopupWidth("200px");
//						}
//						zjpi.setPopupHeight("500px");
						pickAvailableBin(bcc,zjpi);
				}
				break;
			case GIPI_VALUE_CHANGED:
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
//						zjpi.setPopupHeight("500px");
						pickAvailableStock(bcc,zjpi,bcc.getCollection().getCell("st_irg").getInt(),-1);
				}
				break;
			case GIPI_VALUE_ONOK:
				break;
			default :
				break;
			}
		}	
		
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		if(getGipi(transferDetailViewid) == null) {
			setGipi(transferDetailViewid,new TransferGetItemProperty(p_br.getSubLink(transferDetailViewid)));	
		}
		super.bindCellCollection(p_br,mode);
		if(mode == JxZkBiBase.MODE_ADD) {
			try {
				p_br.getCell("stm_date").set(DateUtil.today());
			} catch (CellException ex) {
				UniLog.log(ex);
			}
		}

		if(getSessionHelper().isMobileDevice()) {
		} else {
			JxField sv = jxAdd("list_"+JxZkBiBase.replaceViewName("wc_StmdKl"));
			sv.setAttribute("showfilter","");
			Listbox lb = (Listbox) sv.getNativeObject();
			lb.setHeight("600px");
			ListModelList lm = (ListModelList) lb.getListModel();
			int n = lm.getSize();
			Listitem li = lb.getItemAtIndex(n-1);
			Clients.scrollIntoView(li);
		}
		
	}
	void createSelectopt() {
		if(jxfSelOptForm == null) {
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
		jxfSelOptForm = JxSelOpt.createJxSelOpt(pvdr);
		if(isMobile) {
//			int cc = sessionHelper.getScreenWidth();
//			if( cc <= 100) {
//				jxfSelOptForm.setWidth("100%");
//				p_zkpi.setPopupWidth("100%");
//			} else {
//				jxfSelOptForm.setWidth(""+(cc - 20)+"px");
//				p_zkpi.setPopupWidth(""+(cc - 20)+"px");
//			}
		} else {
//			jxfSelOptForm.setWidth("500px");
		}
		jxfSelOptForm.setOnSelectAction (
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object[] rec = (Object[]) jxfSelOptForm.getPickListBoxValue();
					if (rec != null) {
						try {
							ColumnCell ccol = (ColumnCell) jxfSelOptForm.getUserData();
							if(ccol.getBiColumn().getLabel().equals("st_icode")) {
								TableRec tr = tgipiSelAvailableStock.getTableRec();
								ccol.getCollection().getCell("stmdko_irg").set( rec[tr.getFieldIndex("pdls_irg")] );
								ccol.getCollection().getCell("stmdko_org").set( rec[tr.getFieldIndex("pdls_org")] );
								setDefaultFromBinQty(ccol.getCollection());
							}
							if(ccol.getBiColumn().getLabel().equals("stmdki_bin")) {
								TableRec tr = tgipiSelAvailableBin.getTableRec();
								ccol.set(rec[tr.getFieldIndex("lcb_binno")]);
								UniLog.log("HAHA 190819 stmdki_bin = " + ccol.getString());
							}
						} catch (Exception cex ) {  
							UniLog.log(cex);
						} 
						jxfSelOptForm.closeForm();
					}
				}
			}
		);
		if(usePopupSelectOpt)  {
			jxfPopupSelOptForm = JxSelOpt.createPopupJxSelOpt(getSessionHelper());
		jxfPopupSelOptForm.setOnSelectAction (
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object[] rec = (Object[]) jxfSelOptForm.getPickListBoxValue();
					if (rec != null) {
						try {
							ColumnCell ccol = (ColumnCell) jxfPopupSelOptForm.getUserData();
							if(ccol.getBiColumn().getLabel().equals("st_icode")) {
								TableRec tr = tgipiSelAvailableStock.getTableRec();
								ccol.getCollection().getCell("stmdko_irg").set( rec[tr.getFieldIndex("pdls_irg")] );
								ccol.getCollection().getCell("stmdko_org").set( rec[tr.getFieldIndex("pdls_org")] );
								setDefaultFromBinQty(ccol.getCollection());
							}
							if(ccol.getBiColumn().getLabel().equals("stmdki_bin")) {
								TableRec tr = tgipiSelAvailableBin.getTableRec();
								ccol.set(rec[tr.getFieldIndex("lcb_binno")]);
								UniLog.log("HAHA 190819 stmdki_bin = " + ccol.getString());
							}
						} catch (Exception cex ) {  
							UniLog.log(cex);
						} 
						jxfPopupSelOptForm.closeForm();
					}
				}
			}
		);
		}
		}
		if(tgipiSelAvailableStock == null) {
			tgipiSelAvailableStock = new TrGetItemProperty(
				new VectorUtil()
					.addElement("st_icode")
					.addElement("or_ocode")
					.addElement("or_cocode")
					.toVector(),
				new VectorUtil()
					.addElement("Item Code")
					.addElement("P.O.")
					.addElement("Owner")
					.toVector(),
				new VectorUtil()
					.addElement("25%")
					.addElement("25%")
					.addElement("25%")
					.toVector()
			);
		}
		if(tgipiSelAvailableBin == null) {
			tgipiSelAvailableBin = new TrGetItemProperty(
				new VectorUtil()
					.addElement("lcb_binno")
					.toVector(),
				new VectorUtil()
					.addElement("Location")
					.toVector(),
				new VectorUtil()
					.addElement("100%")
					.toVector()
			);
		}
	}
	boolean pickAvailableBin(ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
		createSelectopt();
		p_zkpi.setJxZkForm(jxfSelOptForm);
//		if(isMobile) {
//			p_zkpi.setPopupWidth("100%");
//		} else {
//			p_zkpi.setPopupWidth("500px");
//		}
		try {
			TableRec tr=null;
			String selStr = "select * from locationbin where lcb_loc = '"+p_bcc.getCollection().getCellString("stmdki_loc")+"' order by lcb_binno";
			tr = getBr().getSelectUtil().getQueryResult(selStr,null);
			if(tr.getRecordCount() > 0) {
				tgipiSelAvailableBin.setTableRec(tr);
				jxfSelOptForm.setUserData(p_bcc);
				jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelAvailableBin);	
				if(tr.getRecordCount() == 1) {
					jxfSelOptForm.setPickListBoxIdx(0);
					jxfSelOptForm.triggerOnSelect();
				} else {
					if(!p_zkpi.isOpen()) p_zkpi.open();
					
					jxfSelOptForm.beginPick(!isMobile);
				}
			} else p_zkpi.close();
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
	boolean pickAvailableStock(ColumnCell p_bcc,ZkJxPickInput p_zkpi,int p_irg,int p_org) {
		createSelectopt();
		if(!usePopupSelectOpt) p_zkpi.setJxZkForm(jxfSelOptForm);
//		if(isMobile) {
//			p_zkpi.setPopupWidth("100%");
//		} else {
//			p_zkpi.setPopupWidth("500px");
//		}
		try {
			TableRec tr=null;
//			String selStr = "select pdls_irg,pdls_org,pdls_stockqty,st_icode,or_ocode,or_cocode from podetlocstatus,stock,orders where st_irg = pdls_irg and or_org = pdls_org and pdls_loc = '"+p_bcc.getCollection().getCellString("stm_ref3")+"' and pdls_stockqty > 0";
			String selStr = null;
			if(getBr().getCellInt("stm_nref3") == 2) {
				selStr = "select pdlbs_irg pdls_irg,pdlbs_org pdls_org,st_icode,or_ocode,or_cocode,sum(pdlbs_stockqty) pdls_stockqty from podetlocbinstatus,stock,orders where st_irg = pdlbs_irg and or_org = pdlbs_org and pdlbs_loc = '"+p_bcc.getCollection().getCellString("stmdko_loc")+"' and pdlbs_stockqty > 0 and pdlbs_bin <> '' ";
			if(p_irg > 0) {
				selStr += " and pdlbs_irg = " + p_irg ;
				if(p_org > 0) {
					selStr += " and pdlbs_org = " + p_org ;
				}
				selStr += " group by 1,2,3,4,5 ";
			} else {
				selStr += " group by 1,2,3,4,5 order by st_icode";
				
			}
			} else {
				selStr = "select pdls_irg,pdls_org,pdls_stockqty,st_icode,or_ocode,or_cocode from podetlocstatus,stock,orders where st_irg = pdls_irg and or_org = pdls_org and pdls_loc = '"+p_bcc.getCollection().getCellString("stmdko_loc")+"' and pdls_stockqty > 0";
			if(p_irg > 0) {
				selStr += " and pdls_irg = " + p_irg;
				if(p_org > 0) {
					selStr += " and pdls_org = " + p_org;
				}
			} else {
				selStr += " order by st_icode";
				
			}
			}
//			selStr += " limit 100";
			tr = getBr().getSelectUtil().getQueryResult(selStr,null);
//					("select * from podetlocstatus,stock,orders where st_irg = pdls_irg and or_org = pdls_org and pdls_stockqty > 0 order by st_icode",null);
			if(tr.getRecordCount() > 0) {
				tgipiSelAvailableStock.setTableRec(tr);
				if(!usePopupSelectOpt) {
					jxfSelOptForm.jxAdd("pickListBox").setAttribute("onDemand", "200");
					jxfSelOptForm.setUserData(p_bcc);
					jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelAvailableStock);	
					if(tr.getRecordCount() == 1) {
						jxfSelOptForm.setPickListBoxIdx(0);
						jxfSelOptForm.triggerOnSelect();
					} else {
						if(!p_zkpi.isOpen()) p_zkpi.open();
						jxfSelOptForm.beginPick(!isMobile);
					}
				} else {
					jxfPopupSelOptForm.setUserData(p_bcc);
					jxfPopupSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelAvailableStock);	
					if(tr.getRecordCount() == 1) {
						jxfPopupSelOptForm.setPickListBoxIdx(0);
						jxfPopupSelOptForm.triggerOnSelect();
					} else {
						jxfPopupSelOptForm.beginPick(!isMobile);
					}
				}
			} else p_zkpi.close();
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}

	void initSelBin() {
					if(jxfSelBin == null) {
						JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
						jxfSelBin = JxSelOpt.createPopupJxSelOpt(getSessionHelper());
						jxfSelBin.setOnSelectAction (
							new JxActionListener() {
								public void actionPerformed(JxField fd) {
									Object[] rec = (Object[]) jxfSelBin.getPickListBoxValue();
									TableRec tr = tgipiSelBin.getTableRec();
									if (rec != null) {
										String bin = rec[tr.getFieldIndex("bin")].toString().trim();
										UniLog.log("bin " + bin + " selected");
										String mode = (String) jxfSelBin.getUserData();
										if(mode.equals("To")) {
											BiResult sr = getBr().getSubLink(transferDetailViewid);
											try {
											for(BiCellCollection bc : sr.getRowCollectionList()) {
												bc.getCell("stmdki_bin").set(bin);
											}
											setDirtyFlag(true);
											} catch (Exception ex) {
												UniLog.log(ex);
											}
										}
										if(mode.equals("From")) {
											try {

						if(getBr().getCellString("stm_ref3").isEmpty()) {
							tr = getBr().getSelectUtil().getQueryResult("select pdlbs_loc,pdlbs_irg,pdlbs_org,ord1.or_cocode,pdlbs_stockqty,stm_type,stm_date,stm_ref1,stpk_packing,st_icode,vd_vname,st_iname,st_msize1,st_msize2,ord1.or_ocode,ord1.or_date,pdlbs_bin "
									+ "from podetlocbinstatus,orders ord1,stock,outer stmov,outer stockpacking,outer vendor where pdlbs_stockqty> 0 and ord1.or_org = pdlbs_org and"
									+ " stm_mrg = ord1.or_stmrg and stpk_irg = pdlbs_irg and stpk_org = pdlbs_org and pdlbs_bin = ? and st_irg = pdlbs_irg and vd_vcode = or_cocode order by pdlbs_irg,pdlbs_bin, ord1.or_cocode, pdlbs_loc ",
										new Wherecl()
											.appendArgument(bin)
									);
						} else {
							tr = getBr().getSelectUtil().getQueryResult("select pdlbs_loc,pdlbs_irg,pdlbs_org,ord1.or_cocode,pdlbs_stockqty,stm_type,stm_date,stm_ref1,stpk_packing,st_icode,vd_vname,st_iname,st_msize1,st_msize2,ord1.or_ocode,ord1.or_date,pdlbs_bin "
									+ "from podetlocbinstatus,orders ord1,stock,outer stmov,outer stockpacking,outer vendor where pdlbs_bin = ? and pdlbs_stockqty> 0 and ord1.or_org = pdlbs_org and "
									+ "stm_mrg = ord1.or_stmrg and stpk_irg = pdlbs_irg and stpk_org = pdlbs_org and pdlbs_loc = ? and st_irg = pdlbs_irg and vd_vcode = or_cocode order by pdlbs_irg,pdlbs_bin, ord1.or_cocode, pdlbs_loc ",
										new Wherecl()
											.appendArgument(bin)
											.appendArgument(getBr().getCellString("stm_ref3"))
										);
						}
												if(tr.getRecordCount() <= 0) throw new Exception("Stock Reocrd Not Found");
												printOrTransferItems(bin,tr,0,true);
												Clients.evalJavaScript("beep(2)");
												
											} catch (Exception ex) {
												UniLog.log(ex);
											}
											/*
											try {
												tr = getBr().getSelectUtil().getQueryResult("select * from podetlocbinstatus where pdlbs_bin = '" + bin + "' and pdlbs_stockqty > 0");
												int j;
												for(j=0;j<tr.getRecordCount();j++) {
													tr.setRecPointer(j);
													addOneTransferDetail(
															tr.getFieldInt("pdlbs_irg"),
															tr.getFieldInt("pdlbs_org"),
															tr.getFieldString("pdlbs_loc"),
															tr.getFieldString("pdlbs_bin"),
															tr.getFieldDouble("pdlbs_stockqty")
															);
															
												}
												setDirtyFlag(true);
											} catch (Exception ex) {
												UniLog.log(ex);
											}
											*/
											
										}
									}
									jxfSelBin.closeForm();
								}
							}
							
						);
						
						tgipiSelBin = new TrGetItemProperty(
										new VectorUtil().addElement("bin").toVector()
									);
					}
			
	}
	
	@Override
	public void afterBind() {
		super.afterBind();
		/*
		IdSpace isp = (IdSpace) getNativeComponent();
		if(!isp.hasFellow("bt_setFrom",true)) {
				Component ca = isp.getFellow("detail_toolbar",true);
				if(ca != null) {
				Button bt = new ZkBiButton("set From");
				bt.setId("bt_setFrom");
				bt.setParent(ca);
				((JxZkSkin) getSkin()).addOneElementToSkin(bt);
				}
		}
		*/
		transferDetailViewid = "wc.StmdKl";
		new JxFieldAction("bt_scan") {
			public void actionPerformed(JxField fd) {
//				jxSetVisible("scr1",false);
//				jxSetVisible("scr2",true);
				UniLog.log("HAHA activate scan barcode");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
			   	Clients.evalJavaScript("launchScanner()");
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
				UniLog.log("HAHA barcode value changed["+s+"]");
				try {

					if(inModalPopup) {
						ZkUtil.showMsg("Please Select or Close Before Scan Next Item");
						Clients.evalJavaScript("beep(0)");
						return(false);
					}
					if(
							(!s.substring(0, 1).equals("1")) &&
							(!s.substring(0, 1).equals("2"))
							) throw new CellException("barcode invalid (1)");
					int irg=Integer.parseInt(s.substring(1,7));
					int org=Integer.parseInt(s.substring(7,13));
					SelectUtil su = getBr().getSelectUtil();
					/*
					TableRec tr = su.getQueryResult("select * from podetlocstatus where pdls_irg = ? and pdls_org = ? and pdls_loc = '"+ getBr().getCellString("stm_ref3")+ "' and pdls_stockqty > 0",
									new Wherecl().appendArgument(irg).appendArgument(org)
									);
									*/
					TableRec tr = null;
					tr = su.getQueryResult("select * from orders where or_org = ?",new Wherecl().appendArgument(org));
					String ocode = "";
					if(tr.getRecordCount() > 0)  {
					tr.setRecPointer(0);
						ocode = tr.getFieldString("or_ocode");
					}
					
					/*
					if(getBr().getCellString("stm_ref3").isEmpty()) {
						tr = su.getQueryResult("select pdlbs_loc,pdlbs_irg,pdlbs_org,ord1.or_cocode,pdlbs_stockqty,stm_type,stm_date,stm_ref1,ord1.or_cocode,stpk_packing,st_icode,vd_vname,st_iname,st_msize1,st_msize2,ord1.or_ocode,ord1.or_date,pdlbs_bin "
								+ "from podetlocbinstatus,orders ord1,stock,orders ord2,outer stmov,outer stockpacking,outer vendor where pdlbs_irg = ? and pdlbs_stockqty> 0 and ord1.or_org = pdlbs_org and"
								+ " ord1.or_ocode = ord2.or_ocode and ord2.or_org = ? and stm_mrg = ord1.or_stmrg and stpk_irg = pdlbs_irg and stpk_org = pdlbs_org and st_irg = pdlbs_irg and vd_vcode = ord1.or_cocode ",
									new Wherecl()
										.appendArgument(irg)
										.appendArgument(org)
									);
					} else {
						tr = su.getQueryResult("select pdlbs_loc,pdlbs_irg,pdlbs_org,ord1.or_cocode,pdlbs_stockqty,stm_type,stm_date,stm_ref1,ord1.or_cocode,stpk_packing,st_icode,vd_vname,st_iname,st_msize1,st_msize2,ord1.or_ocode,ord1.or_date,pdlbs_bin "
								+ "from podetlocbinstatus,orders ord1,stock,orders ord2,outer stmov,outer stockpacking,outer vendor where pdlbs_irg = ? and pdlbs_stockqty> 0 and ord1.or_org = pdlbs_org and "
								+ "ord1.or_ocode = ord2.or_ocode and ord2.or_org = ? and stm_mrg = ord1.or_stmrg and stpk_irg = pdlbs_irg and stpk_org = pdlbs_org and pdlbs_loc = ? and st_irg = pdlbs_irg and vd_vcode = ord1.or_cocode ",
									new Wherecl()
										.appendArgument(irg)
										.appendArgument(org)
										.appendArgument(getBr().getCellString("stm_ref3"))
									);
					}
					*/

//					if(tr.getRecordCount() <= 0) {
						if(getBr().getCellString("stm_ref3").isEmpty()) {
							tr = su.getQueryResult("select pdlbs_loc,pdlbs_irg,pdlbs_org,ord1.or_cocode,pdlbs_stockqty,stm_type,stm_date,stm_ref1,stpk_packing,st_icode,vd_vname,st_iname,st_msize1,st_msize2,ord1.or_ocode,ord1.or_date,pdlbs_bin "
									+ "from podetlocbinstatus,orders ord1,stock,outer stmov,outer stockpacking,outer vendor where pdlbs_irg = ? and pdlbs_stockqty> 0 and ord1.or_org = pdlbs_org and"
									+ " stm_mrg = ord1.or_stmrg and stpk_irg = pdlbs_irg and stpk_org = pdlbs_org and st_irg = pdlbs_irg and vd_vcode = or_cocode order by pdlbs_irg,pdlbs_bin, ord1.or_cocode, pdlbs_loc ",
										new Wherecl()
											.appendArgument(irg)
									);
						} else {
							tr = su.getQueryResult("select pdlbs_loc,pdlbs_irg,pdlbs_org,ord1.or_cocode,pdlbs_stockqty,stm_type,stm_date,stm_ref1,stpk_packing,st_icode,vd_vname,st_iname,st_msize1,st_msize2,ord1.or_ocode,ord1.or_date,pdlbs_bin "
									+ "from podetlocbinstatus,orders ord1,stock,outer stmov,outer stockpacking,outer vendor where pdlbs_irg = ? and pdlbs_stockqty> 0 and ord1.or_org = pdlbs_org and "
									+ "stm_mrg = ord1.or_stmrg and stpk_irg = pdlbs_irg and stpk_org = pdlbs_org and pdlbs_loc = ? and st_irg = pdlbs_irg and vd_vcode = or_cocode order by pdlbs_irg,pdlbs_bin, ord1.or_cocode, pdlbs_loc ",
										new Wherecl()
											.appendArgument(irg)
											.appendArgument(getBr().getCellString("stm_ref3"))
										);
						}
						
//					}
					if(tr.getRecordCount() <= 0) throw new CellException("Record Not In Stock");
					boolean needConfirm = true;
					/*
					if(tr.getRecordCount() == 1) {
						tr.setRecPointer(0);
						if( org == tr.getFieldInt("pdlbs_org") ) {
							needConfirm = false;
						}
					}
					*/
					if(!needConfirm) {
						tr.setRecPointer(0);
						org = tr.getFieldInt("pdlbs_org");
						if(getBr().getCellString("stm_ref3").isEmpty()) {
							addOneTransferDetail(irg,org,tr.getFieldString("pdlbs_loc"));
						} else {
							addOneTransferDetail(irg,org);
						}
						Clients.evalJavaScript("beep(1)");
					} else {
						tr.setRecPointer(0);
						String icode = tr.getFieldString("st_icode");
						printOrTransferItems(icode,tr,org,false);
//						final ZkForm zkf1 = new ZkForm(null,"zkf/winecave/transfer_pickitem.zul");
//						final CellCollection col = new CellCollection();
//						final Listbox resultList = (Listbox) zkf1.getComponent("selectList");	
//						resultList.getItems().clear();
//						tr.setRecPointer(0);
//						String icode = tr.getFieldString("st_icode");
//						int matchedindex = -1;
//						((Listheader) resultList.getFellow("listICode")).setLabel(icode);
//						for(int i=0;i<tr.getRecordCount();i++) {
//							tr.setRecPointer(i);
//							Listitem li = new Listitem();
//							li.setValue(i);
//							Listcell lc = new Listcell(""+(i+1));
//							li.appendChild(lc);
//							
//							lc = new Listcell();
//							Vlayout vl = new Vlayout();
//							Hlayout hl = new Hlayout();
//							/*
//							vl.appendChild(new Label("Owner "+tr.getFieldString("or_cocode")));
//							vl.appendChild(new Label("WH "+tr.getFieldString("pdlbs_loc")));
//							*/
//							hl.appendChild(new Label(tr.getFieldString("stm_type")+" "+tr.getFieldString("stm_ref1")));
//							hl.appendChild(new Label("Date "+DateUtil.toDateString(tr.getFieldDate("stm_date"),"yyyy/mm/dd")));
//							vl.appendChild(hl);
//							hl = new Hlayout();
//							hl.appendChild(new Label("Owner "+tr.getFieldString("or_cocode")));
//							hl.appendChild(new Label(tr.getFieldString("pdlbs_loc")));
//							hl.appendChild(new Label(tr.getFieldString("pdlbs_bin")));
//							vl.appendChild(hl);
//							hl = new Hlayout();
//							hl.appendChild(new Label("Qty "+
//									String.format("%6.0f",tr.getFieldDouble("pdlbs_stockqty"))
//							));
//							hl.appendChild(new Label(tr.getFieldString("stpk_packing")));
//							vl.appendChild(hl);
//							if(tr.getFieldInt("pdlbs_org") != org ) {
//								ZkUtil.setFontColor(vl, "red");
//							}
//							
//							lc.appendChild(vl);
//							li.appendChild(lc);
//							if(tr.getFieldInt("pdlbs_org") == org ) {
//								li.setSelected(true);
//							} else {
//								ZkUtil.setFontColor(lc, "red");
//							}
//							resultList.appendChild(li);
//						}
//						
//						inModalPopup = true;
//						final TableRec ftr = tr;
//						zkf1.doModal(col,new EventListener() {
//							@Override
//							public void onEvent(Event arg0) throws Exception {
//								// TODO Auto-generated method stub
//								if(arg0.getTarget().getId().equals("btPrint")) {
//									CellCollection col = new CellCollection();
//									col.addCell("st_irg", new Cell(0));
//									col.addCell("st_icode", new Cell(""));
//									col.addCell("vd_vname", new Cell(""));
//									col.addCell("st_iname", new Cell(""));
//									col.addCell("st_msize1", new Cell(0.0));
//									col.addCell("st_msize2", new Cell(0.0));
//									col.addCell("stpk_packing", new Cell(""));
//									col.addCell("or_ocode", new Cell(""));
//									col.addCell("or_date", new Cell(DateUtil.zeroDate));
//									if(resultList.getSelectedCount() > 0) {
//									try {
//										PrintBarcode pbc = new PrintBarcode("PTR01");
//
//									for(Listitem li : resultList.getSelectedItems()) {
//										int idx = (Integer) li.getValue();
//										ftr.setRecPointer(idx);
//										col.getCell("st_irg").set(ftr.getFieldInt("pdlbs_irg"));
//										col.getCell("st_icode").set(ftr.getFieldString("st_icode"));
//										col.getCell("vd_vname").set(ftr.getFieldString("vd_vname"));
//										col.getCell("st_iname").set(ftr.getFieldString("st_iname"));
//										col.getCell("st_msize1").set(ftr.getFieldDouble("st_msize1"));
//										col.getCell("st_msize2").set(ftr.getFieldDouble("st_msize2"));
//										col.getCell("or_ocode").set(ftr.getFieldString("or_ocode"));
//										col.getCell("or_date").set(ftr.getFieldDate("or_date"));
//										pbc.printOne(PrintBarcode.LABEL_TYPE.STOCK_ITEM, col, 1, true);
//									}
//										pbc.close();
//									} catch (Exception ex) {
//										UniLog.log(ex);
//										messageBox(ex.toString());
//									}
//									}
//								}
//								if(arg0.getTarget().getId().equals("btOK")) {
//									if(resultList.getSelectedCount() > 0) {
//									for(Listitem li : resultList.getSelectedItems()) {
//										int idx = (Integer) li.getValue();
//										ftr.setRecPointer(idx);
//										int irg = ftr.getFieldInt("pdlbs_irg");
//										int org = ftr.getFieldInt("pdlbs_org");
//										String loc = ftr.getFieldString("pdlbs_loc");
//										String bin = ftr.getFieldString("pdlbs_bin");
//										double qty = ftr.getFieldDouble("pdlbs_stockqty");
//										try {
//											addOneTransferDetail(irg,org,loc,bin,qty);
//											/*
//											if(getBr().getCellString("stm_ref3").isEmpty()) {
//												addOneTransferDetail(irg,org,ftr.getFieldString("pdlbs_loc"));
//											} else {
//												addOneTransferDetail(irg,org);
//											}
//											*/
//										} catch (Exception ex) {
//											UniLog.log("Exception " + ex);
//										}
//									}
//									/*
//									int idx = resultList.getSelectedIndex();
//									if(idx >= 0) {
//										ftr.setRecPointer(idx);
//										int irg = ftr.getFieldInt("pdlbs_irg");
//										int org = ftr.getFieldInt("pdlbs_org");
//										if(getBr().getCellString("stm_ref3").isEmpty()) {
//											addOneTransferDetail(irg,org,ftr.getFieldString("pdbls_loc"));
//										} else {
//											addOneTransferDetail(irg,org);
//										}
//									} else {
//										Clients.evalJavaScript("beep(0)");
//									}
//									*/
//									Clients.evalJavaScript("beep(1)");
//									} 
//								}
//								inModalPopup = false;
//								zkf1.exitModal();
//							}
//						}
//						);

						Clients.evalJavaScript("beep(2)");
						
					}
					return false;
//					Clients.evalJavaScript("closeScanner()");
				} catch (Exception ex) {
					UniLog.log("barcode invalid " + ex.toString() + "["+s+"]");
					if(ex instanceof CellException) ZkUtil.showMsg(ex.toString()); else ZkUtil.showMsg("Barcode Invalid (0)");
					Clients.evalJavaScript("beep(0)");
					return false;
					
				}
			}
			
		};
		

		new JxFieldAction("bt_setFrom") {
			public void actionPerformed(JxField fd) {
				UniLog.log("set from location");
				try {
					initSelBin();
					TableRec tr;
					tr = getBr().getSelectUtil().getQueryResult("select distinct pdlbs_bin bin from podetlocbinstatus where pdlbs_stockqty > 0");
					if(tr.getRecordCount() > 0) {
							jxfSelBin.setUserData("From");
							tgipiSelBin.setTableRec(tr);
							jxfSelBin.jxAdd("pickListBox").setItemListInterface(tgipiSelBin);	
							jxfSelBin.beginPick();
							jxfSelBin.modalForm();
					} 
					
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		
		new JxFieldAction("bt_setTo") {
			public void actionPerformed(JxField fd) {
				UniLog.log("set to location");
				try {
					initSelBin();
					TableRec tr;
					tr = getBr().getSelectUtil().getQueryResult("select lcb_binno bin from locationbin where lcb_loc = 'STOR' and lcb_binno <> ''");
					if(tr.getRecordCount() > 0) {
							tgipiSelBin.setTableRec(tr);
							jxfSelBin.setUserData("To");
							jxfSelBin.jxAdd("pickListBox").setItemListInterface(tgipiSelBin);	
							jxfSelBin.beginPick();
							jxfSelBin.modalForm();
					} 
					
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		new JxFieldAction("bt_setToX") {
			public void actionPerformed(JxField fd) {
				UniLog.log("set to location");
				try {
				JxZkBiBase.pickBySelect(getSessionHelper(),"wc.locationbin","", new EventListener() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						CellCollection col = (CellCollection) arg0.getData();
					}
					}
				);	
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		{
			SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
			if(sessionHelper.isMobileDevice()) {
				jxSetHeight("mobile_bottom", sessionHelper.getScreenHeight()-100);
			}
		}
		new JxFieldAction("btSelOpt") {
			public void actionPerformed(JxField fd) {
				if(selopt != null) {
					selopt.modalForm();
				}
				if(popupWin != null) {
					listModelList.clear();
					for(int i = 0;i<popupRows;i++) {
						if(i < popupItems.size()) listModelList.add(popupItems.get(i));
					}
					lb.invalidate();
					popupWin.doHighlighted();
					popupWin.setPosition("middle_center"); 
					popupWin.setVisible(true);
				}
			}
		};
	}
	void addOneTransferDetail(int p_irg,int p_org) throws Exception {
		addOneTransferDetail(p_irg,p_org,null,null,0);
	}
	void addOneTransferDetail(int p_irg,int p_org,String p_loc) throws Exception {
		addOneTransferDetail(p_irg,p_org,p_loc,null,0);
	}
	
	void addOneTransferDetail(int p_irg,int p_org,String p_loc,String p_bin,double p_qty) throws Exception {
		BiResult sr = getBr().getSubLink(transferDetailViewid);
		int idx = sr.getRowCount();
		for(int i=0;i<idx;i++) {
			CellCollection cl = sr.getRowCollectionV(i);
			if(cl.getCellInt("stmdko_irg") == p_irg
			   && cl.getCellInt("stmdko_org") == p_org) {
				throw new CellException("Stock Item " + cl.getCellString("st_icode") + " Already Scanned");
			}
		}
		JxField sv = jxAdd("list_"+replaceViewName(sr.getView().getName()));
		BiCellCollection col = sr.newRowCollection();
		ReturnMsg rtn = sr.addSubRecord(col, idx,"");
		Object tr = rtn.getData();
		int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
		sv.addItemToList(tr, rowIdx);
		col.getCell("stmdko_irg").set(p_irg);
		col.getCell("stmdko_org").set(p_org);
		if(p_loc != null) {
			col.getCell("stmdko_loc").set(p_loc);
		}
		if(p_bin == null) setDefaultFromBinQty(col); else {
			col.getCell("stmdko_bin").set(p_bin);
			col.getCell("stmdko_entryqty").set(p_qty);
			col.getCell("stmdko_entryunit").set("Bot");
		}
		/*
		ColumnCell bcc = (ColumnCell) col.getCell("st_icode");
		ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
		ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
		zjpi.setPopupWidth("100%");
		zjpi.setPopupHeight("500px");
		pickAvailableStock(bcc,zjpi,p_irg,p_org);
		*/
	}
	
	void setDefaultFromBinQty(BiCellCollection col) throws Exception {
		int irg = col.getCell("stmdko_irg").getInt();
		int org = col.getCell("stmdko_org").getInt();
		int cpb = col.getCell("st_msize1").getInt();
		TableRec tr = getBr().getSelectUtil().getQueryResult("select * from podetlocbinstatus where pdlbs_irg = ? and pdlbs_org = ? and pdlbs_loc = '"+col.getCellString("stmdko_loc")+"' and pdlbs_stockqty > 0",
				new Wherecl().appendArgument(irg).appendArgument(org)
				);
		if(tr.getRecordCount() == 1) {
			tr.setRecPointer(0);
			col.getCell("stmdko_bin").setItemList((Vector) null);
			col.getCell("stmdko_bin").set(tr.getFieldString("pdlbs_bin"));
			col.getCell("stmdko_bin").setMode(Cell.VMODE_DISPONLY);
			double dqty = tr.getFieldDouble("pdlbs_stockqty");
			int qty =  (int) dqty;
			if(cpb > 1 && (qty % cpb == 0)) {
				col.getCell("stmdko_entryqty").set(qty / cpb);
				col.getCell("stmdko_entryunit").set("Case");
			} else {
				col.getCell("stmdko_entryqty").set(qty);
				col.getCell("stmdko_entryunit").set("Bot");
			}
		} else if(tr.getRecordCount() > 1) {
			Vector v = new Vector();
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				v.add(tr.getFieldString("pdlcb_bin"));
			}
			col.getCell("stmdko_bin").setItemList(v);
			col.getCell("stmdko_bin").set("");
			col.getCell("stmdko_bin").setMode(Cell.VMODE_NORMAL);
			col.getCell("stmdko_entryqty").set(0);
			col.getCell("stmdko_entryunit").set("");
		} else throw new CellException("Not Enough Qty");
	}
	protected void afterAddLink(BiResult sr,int idx)
	{
			JxField sv = jxAdd("list_"+JxZkBiBase.replaceViewName("wc_StmdKl"));
			sv.setAttribute("showfilter","");
			Listbox lb = (Listbox) sv.getNativeObject();
			lb.setHeight("600px");
			ListModelList lm = (ListModelList) lb.getListModel();
			Listitem li = lb.getItemAtIndex(idx);
			Clients.scrollIntoView(li);
	}	
	
	void printOneLabel(String devid,Listbox resultList,final TableRec ftr) {
									CellCollection col = new CellCollection();
									col.addCell("st_irg", new Cell(0));
									col.addCell("pdlbs_org", new Cell(0));
									col.addCell("st_icode", new Cell(""));
									col.addCell("vd_vname", new Cell(""));
									col.addCell("st_iname", new Cell(""));
									col.addCell("st_msize1", new Cell(0.0));
									col.addCell("st_msize2", new Cell(0.0));
									col.addCell("stpk_packing", new Cell(""));
									col.addCell("or_ocode", new Cell(""));
									col.addCell("or_date", new Cell(DateUtil.zeroDate));
									if(resultList.getSelectedCount() > 0) {
									try {
										PrintBarcode pbc = new PrintBarcode(devid);

									for(Listitem li : resultList.getSelectedItems()) {
										int idx = (Integer) li.getValue();
										ftr.setRecPointer(idx);
										col.getCell("st_irg").set(ftr.getFieldInt("pdlbs_irg"));
										col.getCell("pdlbs_org").set(ftr.getFieldInt("pdlbs_org"));
										col.getCell("st_icode").set(ftr.getFieldString("st_icode"));
										col.getCell("vd_vname").set(ftr.getFieldString("vd_vname"));
										col.getCell("st_iname").set(ftr.getFieldString("st_iname"));
										col.getCell("st_msize1").set(ftr.getFieldDouble("st_msize1"));
										col.getCell("st_msize2").set(ftr.getFieldDouble("st_msize2"));
										col.getCell("or_ocode").set(ftr.getFieldString("or_ocode"));
										col.getCell("or_date").set(ftr.getFieldDate("or_date"));
										pbc.printOne(PrintBarcode.LABEL_TYPE.STOCK_ITEM, col, 1, true);
									}
										pbc.close();
									} catch (Exception ex) {
										UniLog.log(ex);
										messageBox(ex.toString());
									}
									}
		
	}
	void printOrTransferItems(String header,final TableRec tr,final int org,final boolean showItemCode) throws Exception
	{
		final ZkForm zkf1 = new ZkForm(null,"zkf/winecave/transfer_pickitem.zul");
		final CellCollection col = new CellCollection();
		final Listbox resultList = (Listbox) zkf1.getComponent("selectList");	
		((Listheader) resultList.getFellow("listICode")).setLabel(header + " ("+tr.getRecordCount()+")");
		ListModelArray lma = new ListModelArray(tr.getAllData());
		ListitemRenderer listitemRenderer = new ListitemRenderer() {
		public void render(Listitem p_listItem, Object p_data, int p_idx) throws Exception {
							tr.setRecPointer(p_idx);
							Listitem li = p_listItem;
							li.setValue(p_idx);
							Listcell lc = new Listcell(""+(p_idx+1));
							li.appendChild(lc);
							lc = new Listcell();
							Vlayout vl = new Vlayout();
							Hlayout hl = new Hlayout();
							if(showItemCode) {
								hl.appendChild(new Label(tr.getFieldString("st_icode")));
								hl.appendChild(new Label(String.format("%dx%d", (int) tr.getFieldDouble("st_msize1"), (int) tr.getFieldDouble("st_msize2")
										)));
								vl.appendChild(hl);
								hl = new Hlayout();
							}
							hl.appendChild(new Label(tr.getFieldString("stm_type")+" "+tr.getFieldString("stm_ref1")));
							hl.appendChild(new Label("Date "+DateUtil.toDateString(tr.getFieldDate("stm_date"),"yyyy/mm/dd")));
							vl.appendChild(hl);
							hl = new Hlayout();
							hl.appendChild(new Label("Owner "+tr.getFieldString("or_cocode")));
							hl.appendChild(new Label(tr.getFieldString("pdlbs_loc")));
							vl.appendChild(hl);
							hl = new Hlayout();
							if(!showItemCode) {
								hl.appendChild(new Label(tr.getFieldString("pdlbs_bin") + " "+ "Qty "+
									String.format("%6.0f",tr.getFieldDouble("pdlbs_stockqty"))
								));
							} else {
								hl.appendChild(new Label("Qty "+
									String.format("%6.0f",tr.getFieldDouble("pdlbs_stockqty"))
								));
							}
							hl.appendChild(new Label(tr.getFieldString("stpk_packing")));
							vl.appendChild(hl);
							if(tr.getFieldInt("pdlbs_org") != org ) {
								ZkUtil.setFontColor(vl, "red");
							}
							
							lc.appendChild(vl);
							li.appendChild(lc);
							if(tr.getFieldInt("pdlbs_org") == org ) {
								li.setSelected(true);
							} else {
								ZkUtil.setFontColor(lc, "red");
							}
									}
		};
		
						resultList.setModel(lma);
						lma.setMultiple(true);
						resultList.setMultiple(true);
						resultList.setCheckmark(true);
						resultList.invalidate();
						resultList.setItemRenderer(listitemRenderer);
						inModalPopup = true;
						final TableRec ftr = tr;
						zkf1.doModal(col,new EventListener() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								// TODO Auto-generated method stub
								if(arg0.getTarget().getId().equals("btPrint2")) {
									printOneLabel("PTR02",resultList,ftr);
								}
								if(arg0.getTarget().getId().equals("btPrint")) {
									printOneLabel("PTR01",resultList,ftr);
								}
								if(arg0.getTarget().getId().equals("btOK")) {
									if(resultList.getSelectedCount() > 0) {
									for(Listitem li : resultList.getSelectedItems()) {
										int idx = (Integer) li.getValue();
										ftr.setRecPointer(idx);
										int irg = ftr.getFieldInt("pdlbs_irg");
										int org = ftr.getFieldInt("pdlbs_org");
										String loc = ftr.getFieldString("pdlbs_loc");
										String bin = ftr.getFieldString("pdlbs_bin");
										double qty = ftr.getFieldDouble("pdlbs_stockqty");
										try {
											addOneTransferDetail(irg,org,loc,bin,qty);
											/*
											if(getBr().getCellString("stm_ref3").isEmpty()) {
												addOneTransferDetail(irg,org,ftr.getFieldString("pdlbs_loc"));
											} else {
												addOneTransferDetail(irg,org);
											}
											*/
										} catch (Exception ex) {
											UniLog.log("Exception " + ex);
										}
									}
									Clients.evalJavaScript("beep(1)");
									} 
								}
								inModalPopup = false;
								zkf1.exitModal();
							}
						}
						);
	}
}
