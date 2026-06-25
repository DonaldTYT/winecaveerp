package com.uniinformation.jxapp.erpv4;

import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultGR;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class GR extends JxZkBiBase {
	GipiNamedItemList prdList;
	Vector <String> cgList = null;
	class CustomDispRec {
		String cName;
		double qty = 0;
		double gwt = 0;
		double volume =0;
		double cost =0;
	}
	Window customScr = null;
	
	class GrGetItemProperty extends ZkBiGetItemProperty {
		JxSelOpt jxf = null;
		TrGetItemProperty tgipi;
		GrGetItemProperty(BiResult br,JxZkBiBase p_base) {
			super(br,p_base);
		}
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) {
				setDirtyFlag(true);
			} else {
				if(bcc.getBiColumn().getLabel().equals("orddet_ref")) {
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
				if(bcc.getCellLabel().equals("orders_ref1")) {
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
				if(bcc.getBiColumn().getLabel().equals("orders_ref1"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxf == null) {
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
											.addElement("stm_ref1")
											.addElement("st_icode")
											.addElement("inv_invno")
											.addElement("pds_ostqty")
											.toVector()
									);
							jxf = JxSelOpt.createJxSelOpt(pvdr);
//							jxf.setWidth("780px");
							jxf.setOnSelectAction (
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
												CellCollection ccx = (CellCollection) jxf.getUserData();
												ccx.getCell("stmd_qorg").update(rec[tr.getFieldIndex("stmd_qorg")]);
												ccx.getCell("stmd_qirg").update(rec[tr.getFieldIndex("stmd_qirg")]);
												ccx.getCell("stmd_org").update(rec[tr.getFieldIndex("stmd_org")]);
												ccx.getCell("stmd_irg").update(rec[tr.getFieldIndex("stmd_irg")]);
												ccx.getCell("stmd_qty").update(rec[tr.getFieldIndex("pds_ostqty")]);
											} catch (CellException cex ) {  
												UniLog.log(cex);
											}
											jxf.closeForm();
										}
									}
							);
						}
//						zjpi.setPopupWidth("800px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);

						HashSet <Integer> hsicode=new HashSet<Integer>();
						int thisorg = 0;
						for(int j = 0;j<getRowCount();j++) {
							CellCollection col = bigibr.getRowCollectionV(j);
							if( col == bcc.getCollection()) {
								thisorg = col.getCell("stmd_org").getInt();
							} else {
								hsicode.add(col.getCell("stmd_org").getInt());
							}
						}
						Wherecl wcl = new Wherecl();
						if(thisorg > 0)  {
							wcl.andWherecl(new Wherecl().orUniop("pds_ostqty", ">", 0).orUniop("pds_org", "=", thisorg));
						} else {
							wcl.andUniop("pds_stockqty", ">", 0);
						}	
						String whereStr = 
								"where "
								+ "pds_ostqty > 0 "
								+ "and st_irg = pds_irg "
								+ "and stmd_org = pds_org "
								+ "and stmd_irg = pds_irg "
//								+ "and stmd_tdtype in ("+Erpv4Config.PURCHASE_TDtypes+") "
								+ "and stmd_flag1 = 'Y' "
								+ "and stm_mrg = stmd_mrg "
								+ "and ind_odrg = stmd_qorg "
								+ "and inv_rg = ind_rg ";

						if(Erpv4Config.isMultiCompany(getSessionHelper())) {
							whereStr += " and " +"stm_cocode ='" +  getBr().getCellString("stm_cocode") + "'";
						}
						String ss = Erpv4Config.getString(getSessionHelper(), "RequireLoc") ;
						if(ss != null && ss.equals("Y")) {
							whereStr += " and " +"pds_loc='" +  getBr().getCellString("stm_fromloc") + "'";
						}
						TableRec tr = getBr().getSelectUtil().getQueryResult("select * from podetstatus , stock, stmovd, stmov,outer(quodet , quotation) " + whereStr
								, null);
						tgipi.setTableRec(tr);
						jxf.setUserData(bcc.getCollection());
						jxf.jxAdd("pickListBox").setItemListInterface(tgipi);
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
	
		LOCK_RECORD_FOR_UPDATE = true;
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		boolean isNew;
		isNew = getGipi(getStmdViewName(p_br)) == null;
		if(isNew) {
			setGipi(getStmdViewName(p_br),new GrGetItemProperty(p_br.getSubLink(getStmdViewName(p_br)),this));	
		}
		super.bindCellCollection(p_br, mode);
		cgList = new Vector();
		try {
			TableRec tr = p_br.getSelectUtil().getQueryResult("select distinct stmd_ref from stmovd where stmd_tdtype in("+Erpv4Config.PURCHASE_TDtypes+") order by 1", null);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				cgList.add((String) tr.getField("stmd_ref"));
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		
		
		if(mode == JxZkBiBase.MODE_ADD) {
		} 
		if(mode == JxZkBiBase.MODE_UPDATE) { 
			boolean poOutOfSync = false;
			BiResult sr = p_br.getSubLink(getStmdViewName(p_br));
			final Vector <BiCellCollection> v = sr.getRowCollectionList();
							try {
			for(BiCellCollection col : v) {
//				if(col.getCell("stmd_cur").getMode() == Cell.VMODE_OVERRIDED) {
				if(col.getCell("stmd_cur").isOverrided()) {
					if(col.getCell("stmd_cur").getString().equals("")) {
//						col.getCell("stmd_cur").syncMode(Cell.VMODE_PROTECTED);
						col.getCell("stmd_cur").clearOverride();
					} else {
						poOutOfSync = true;
							break;
					}
				}
//				if(col.getCell("stmd_uprice").getMode() == Cell.VMODE_OVERRIDED) {
				if(col.getCell("stmd_uprice").isOverrided()) {
					if(col.getCell("stmd_uprice").getDouble() == 0) {
//						col.getCell("stmd_uprice").syncMode(Cell.VMODE_PROTECTED);
						col.getCell("stmd_uprice").clearOverride();
					} else {
						poOutOfSync = true;
						break;
					}
				}
//				if(col.getCell("stmd_xrate").getMode() == Cell.VMODE_OVERRIDED) {
				if(col.getCell("stmd_xrate").isOverrided()) {
					if(col.getCell("stmd_xrate").getDouble() == 0) {
//						col.getCell("stmd_xrate").syncMode(Cell.VMODE_PROTECTED);
						col.getCell("stmd_xrate").clearOverride();
					} else {
						poOutOfSync = true;
						break;
					}
				}
			}
							} catch (Exception cex) {
								UniLog.log(cex);
							}
			if(poOutOfSync) {
				
				confirm("Item Cost Not Sync with PO Entry, Do you want to update ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {

			for(CellCollection col : v) {
//				if(col.getCell("stmd_cur").getMode() == Cell.VMODE_OVERRIDED) {
				if(col.getCell("stmd_cur").isOverrided()) {
//					col.getCell("stmd_cur").syncMode(Cell.VMODE_PROTECTED);
					col.getCell("stmd_cur").clearOverride();
				}
//				if(col.getCell("stmd_uprice").getMode() == Cell.VMODE_OVERRIDED) {
				if(col.getCell("stmd_uprice").isOverrided()) {
//					col.getCell("stmd_uprice").syncMode(Cell.VMODE_PROTECTED);
					col.getCell("stmd_uprice").clearOverride();
				}
//				if(col.getCell("stmd_xrate").getMode() == Cell.VMODE_OVERRIDED) {
				if(col.getCell("stmd_xrate").isOverrided()) {
//					col.getCell("stmd_xrate").syncMode(Cell.VMODE_PROTECTED);
					col.getCell("stmd_xrate").clearOverride();
				}
			}
									setDirtyFlag(true);
									
								}
							} catch (CellException cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
			}
		}
		
			try {
			SelectUtil su = p_br.getSelectUtil();
				TableRec tr = su.getQueryResult("select * from prdsrvmaster");
				prdList = new GipiNamedItemList();
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					prdList.appendItem( tr.getFieldString("pds_code"), tr.getFieldString("pds_desc"));
				}
				if(p_br.getCell("stm_salescode1") != null) p_br.getCell("stm_salescode1").setItemPropertyInterface(prdList);
				if(p_br.getCell("stm_salescode2") != null) p_br.getCell("stm_salescode2").setItemPropertyInterface(prdList);
				if(p_br.getCell("stm_salescode3") != null) p_br.getCell("stm_salescode3").setItemPropertyInterface(prdList);
				if(p_br.getCell("stm_salescode4") != null) p_br.getCell("stm_salescode4").setItemPropertyInterface(prdList);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
	}

	protected String getStmdViewName(BiResult p_br) {
		return(((BiResultGR) p_br).getStmdLinkName());
	}
	

}
