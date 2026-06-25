package com.uniinformation.jxapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.afs.BiResultAfsDO;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.erpv4.DO;
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

public class AfsDO extends DO {
//	GipiNamedItemList prdList;
	AfsCustomScr jxfCustom ;
	Window customScr = null;
	class CustomDispRec {
		String cName;
		double qty = 0;
		double gwt = 0;
		double volume =0;
		double cost=0;
	}
	@Override
	public void afterBind() {
		super.afterBind();
		detViewName = "AfsDoDet";
		new JxFieldAction("btCustom") {
		public void actionPerformed(JxField fd){
				try {
					if(customScr == null) {
						customScr = newPopupWindow("Custom Data");
						customScr.setWidth("800px");
						customScr.setHeight("600px");
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
						jxfCustom = (AfsCustomScr) getOrCreateJxZkForm(customScr,pvdr ,"AfsCustomScr");
					}
					JxField jxlb = jxfCustom.getListBox();
				
					Vector <BiCellCollection> recs = getBr().getSubLinkResult("AfsDoDet");
					jxlb.gridSetCol(5); 
					Hashtable <String,CustomDispRec> ht = new Hashtable();
					Vector <CustomDispRec> kv = new Vector <CustomDispRec>();
					CustomDispRec cr;
					double tCost = 0.0;
					double tGwt = 0.0;
					for(CellCollection col:recs) {
						if((cr = ht.get( col.getCell("orddet_ref").getString())) == null) {
							cr = new CustomDispRec();
							cr.cName = col.getCell("orddet_ref").getString();
							ht.put(cr.cName,cr);
							kv.add(cr);
						}
//						cr.qty += col.getCell("stmd_qty").getDouble();
//						cr.gwt += col.getCell("stmd_fref1").getDouble();
						cr.qty += 1.0;
						cr.gwt += col.getCell("stmd_fref1").getDouble() * col.getCell("stmd_qty").getDouble();
						tGwt += col.getCell("stmd_fref1").getDouble() * col.getCell("stmd_qty").getDouble();
						cr.volume += col.getCell("stmd_volume").getDouble();
						tCost += col.getCell("stmd_qty").getDouble() *  col.getCell("orddet_uprice").getDouble();
						cr.cost += col.getCell("stmd_qty").getDouble() *  col.getCell("orddet_uprice").getDouble();
					}	
					jxlb.gridSetRow(kv.size());
					for(int i = 0;i < kv.size();i++) {
						cr = kv.get(i);
						jxlb.gridSetValue(0,i,cr.cName);
						jxlb.gridSetValue(1,i,cr.qty);
//						jxlb.gridSetValue(2,i,cr.gwt);
						jxlb.gridSetValue(2,i,StringUtil.ftostr(cr.gwt,"#,###,##0.000"));
						jxlb.gridSetValue(3,i,cr.volume);
//						jxlb.gridSetValue(4,i,cr.cost);
						jxlb.gridSetValue(4,i,StringUtil.ftostr(cr.cost,"##,###,##0.00"));
					}
					jxfCustom.jxSetText("custScr_amt",""+tCost);
					jxfCustom.jxSetText("custScr_gwt",""+tGwt);
					customScr.doModal();
				} catch (Exception ex) {
					UniLog.log(ex);
				}
		}
		};
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		if(mode == JxZkBiBase.MODE_UPDATE) { 
			boolean dnOutOfSync = false;
			BiResult sr = p_br.getSubLink(detViewName);
			final Vector <BiCellCollection> v = sr.getRowCollectionList();
			try {
			for(BiCellCollection col : v) {
//				if(col.getCell("stmd_fref1").getMode() == Cell.VMODE_OVERRIDED) {
				if(col.getCell("stmd_fref1").isOverrided()) {
					if(col.getCell("stmd_fref1").getDouble() == 0) {
//						col.getCell("stmd_fref1").syncMode(Cell.VMODE_PROTECTED);
						col.getCell("stmd_fref1").clearOverride();
					} else {
						dnOutOfSync = true;
						break;
					}
				}
			}
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			if(dnOutOfSync) {
				confirm("Gross Weight Not Sync with GR Entry, Do you want to update ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {

			for(CellCollection col : v) {
				if(col.getCell("stmd_fref1").isOverrided()) {
//					col.getCell("stmd_fref1").syncMode(Cell.VMODE_PROTECTED);
					col.getCell("stmd_fref1").clearOverride();
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
//		try {
//		SelectUtil su = p_br.getSelectUtil();
//			TableRec tr = su.getQueryResult("select * from prdsrvmaster");
//			prdList = new GipiNamedItemList();
//			for(int i=0;i<tr.getRecordCount();i++) {
//				tr.setRecPointer(i);
//				prdList.appendItem( tr.getFieldString("pds_ano"), tr.getFieldString("pds_desc"));
//			}
//			if(p_br.getCell("stm_salescode1") != null) p_br.getCell("stm_salescode1").setItemPropertyInterface(prdList);
//			if(p_br.getCell("stm_salescode2") != null) p_br.getCell("stm_salescode2").setItemPropertyInterface(prdList);
//			if(p_br.getCell("stm_salescode3") != null) p_br.getCell("stm_salescode3").setItemPropertyInterface(prdList);
//			if(p_br.getCell("stm_salescode4") != null) p_br.getCell("stm_salescode4").setItemPropertyInterface(prdList);
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
	}
	
	void setupOneSalesCode(BiResult p_br,String p_prdano,String p_salesano,Vector p_aclist) throws CellException {
			if(p_br.getCell(p_prdano) != null) {
				p_br.getCell(p_prdano).setItemPropertyInterface(prdList);
				int cc = prdList.getIndexOf(p_br.getCellString(p_salesano));
				if( cc >= 0) {
					p_br.getCell(p_prdano).set(p_br.getCellString(p_salesano));
				} else {
					p_br.getCell(p_prdano).set("");
				}
				p_br.getCell(p_salesano).setItemList(p_aclist);
			}
		
	}
	protected void setupSalesCode(BiResult p_br) {
		try {
		SelectUtil su = p_br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from prdsrvmaster");
			Vector<String> acList = new Vector<String>();
			prdList = new GipiNamedItemList();
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				prdList.appendItem( tr.getFieldString("pds_ano"), tr.getFieldString("pds_desc"));
			}
			tr = su.getQueryResult("select ca_ano from ca where ca_utype = 'UD' order by 1");
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				acList.add(tr.getFieldString("ca_ano"));
			}
			setupOneSalesCode(p_br,"stm_prdano1","stm_salescode1",acList);
			
				/*
			if(p_br.getCell("stm_prdano1") != null) {
				p_br.getCell("stm_prdano1").setItemPropertyInterface(prdList);
				int cc = prdList.getIndexOf(p_br.getCellString("stm_salescode1"));
				if( cc >= 0) {
					p_br.getCell("stm_prdano1").set(p_br.getCellString("stm_salescode1"));
				} else {
					p_br.getCell("stm_prdano1").set("");
				}
			}
				*/
			if(p_br.getCell("stm_prdano2") != null) p_br.getCell("stm_prdano2").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_prdano3") != null) p_br.getCell("stm_prdano3").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_prdano4") != null) p_br.getCell("stm_prdano4").setItemPropertyInterface(prdList);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		
	}
}
