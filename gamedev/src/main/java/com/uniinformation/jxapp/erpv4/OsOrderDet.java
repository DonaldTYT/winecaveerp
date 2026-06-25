package com.uniinformation.jxapp.erpv4;

import java.util.HashSet;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Idspace;

import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class OsOrderDet extends JxZkBiBase {
	protected String detailViewId = "erpv4.OsDet";
	class OsOrderDetGetItemProperty extends BiGetItemProperty {
		IcodePicker jxf = null;
		OsOrderDetGetItemProperty(BiResult p_br) {
			super(p_br);
		}
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) setDirtyFlag(true);
			if(p_ctype == GIPI_PULLDOWN_CLOSED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
			}
			if(p_ctype == GIPI_PULLDOWN_OPENED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							jxf = (IcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"erpv4.IcodePicker");
						}
						zjpi.setPopupWidth("700px");
						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
						Wherecl wcl = null;
						if(getBr().getCell("st_mbrand").getString().equals("HOR")) {
							String pn = getBr().getCell("st_oicode").getString();
							int n0 = pn.indexOf('-');
							if(n0 >= 0) {
								String s0 = pn.substring(0,n0);
								int n1 = getBr().getCell("st_icode").getString().indexOf(getBr().getCell("st_oicode").getString());
								if( n1 > 0) {
									String s1 = getBr().getCell("st_icode").getString().substring(0,n1);
									wcl = new Wherecl().andUniop("st_icode", "like", s1+s0+"%");
								}
							}
						} 
						if(wcl == null) {
							wcl = new Wherecl().andUniop("st_icode", "=", getBr().getCell("st_icode").getString());
						}
//						double maxQty = 
//									getBr().getCell("qdst_ostqty").getDouble() +
//									getBr().getCell("qdst_resqty").getDouble() +
//									getBr().getCell("qdst_alcqty").getDouble() ;
//						double reqQty = 0;
						double reqQty = getBr().getCell("qdst_ostqty").getDouble();
						int alcOrg = 0;
						double alcQty = 0;
						HashSet <String> hsicode=new HashSet<String>();
						/*
						for(int j = 0;j<getRowCount();j++) {
							CellCollection col = bigibr.getRowCollectionV(j);
							reqQty += col.getCell("stmd_qty").getSetDb();
							if( col == bcc.getCollection()) {
								alcOrg = col.getCell("stmd_org").getSetInt();
								alcQty = col.getCell("stmd_qty").getSetDb();
							} else {
								if(!bigibr.isMarkedDelete(getRow(j))) {
									reqQty -= col.getCell("stmd_qty").getDouble();
								}
								String ticode = col.getCell("st_icode").getString();
								if(!ticode.equals("")) hsicode.add(ticode);
							}
						}
						*/
						if(hsicode.size() > 0) {
							String s = null;
							for(String hic : hsicode) {
								if(s == null) {
									s = " and st_icode not in (";
								} else {
									s += ",";
								}
								s += "'"+hic+"'";
							}
							s += ")";
							wcl.appendString(s);
						}
						jxf.setPickerForAvailableStock(OsOrderDet.this,getBr().getSelectUtil(),wcl
								,bcc
								,bcc.getCollection().getCell("stmd_org")
								,bcc.getCollection().getCell("stmd_qty")
								,reqQty
								,alcOrg
								,alcQty
								);
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
//		LOCK_RECORD_FOR_UPDATE = true;
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		boolean isNew;
		isNew = getGipi(detailViewId) == null;
//		if(getGipi("AfsMoDet") == null) {
		if(isNew) {
			setGipi(detailViewId,new OsOrderDetGetItemProperty(br.getSubLink(detailViewId)));	
		}
		super.bindCellCollection(br, mode);
	}
}
