package com.uniinformation.jxapp;


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
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.erpv4.GR;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class AfsGR extends GR {
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
	AfsCustomScr customjxf ;
	
	
	@Override
	public void afterBind() {
		super.afterBind();
	
	new JxFieldAction("btCustom") {
		public void actionPerformed(JxField fd){
				try {
					if(customScr == null) {
						customScr = newPopupWindow("Custom Data");
						customScr.setWidth("800px");
						customScr.setHeight("600px");
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
						customjxf = (AfsCustomScr) getOrCreateJxZkForm(customScr,pvdr ,"AfsCustomScr");
					}
					JxField jxlb = customjxf.getListBox();
				
					Vector <BiCellCollection> recs = getBr().getSubLinkResult(getStmdViewName(getBr()));
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
						cr.cost += col.getCell("stmd_qty").getDouble() *  col.getCell("orddet_uprice").getDouble();
						tCost += col.getCell("stmd_qty").getDouble() *  col.getCell("orddet_uprice").getDouble();
					}	
					jxlb.gridSetRow(kv.size());
					for(int i = 0;i < kv.size();i++) {
						cr = kv.get(i);
						jxlb.gridSetValue(0,i,cr.cName);
						jxlb.gridSetValue(1,i,cr.qty);
//						jxlb.gridSetValue(2,i,String.format("%.2f",cr.gwt));
						jxlb.gridSetValue(2,i,StringUtil.ftostr(cr.gwt,"#,###,##0.000"));
						jxlb.gridSetValue(3,i,cr.volume);
//						jxlb.gridSetValue(4,i,cr.cost);
						jxlb.gridSetValue(4,i,StringUtil.ftostr(cr.cost,"##,###,##0.00"));
					}
					customjxf.jxSetText("custScr_amt",""+tCost);
					customjxf.jxSetText("custScr_gwt",""+tGwt);
					customScr.doModal();
				} catch (Exception ex) {
					UniLog.log(ex);
				}
		}
	};
		LOCK_RECORD_FOR_UPDATE = true;
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		try {
		SelectUtil su = p_br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from prdsrvmaster");
			prdList = new GipiNamedItemList();
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				prdList.appendItem( tr.getFieldString("pds_ano"), tr.getFieldString("pds_desc"));
			}
			if(p_br.getCell("stm_salescode1") != null) p_br.getCell("stm_salescode1").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode2") != null) p_br.getCell("stm_salescode2").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode3") != null) p_br.getCell("stm_salescode3").setItemPropertyInterface(prdList);
			if(p_br.getCell("stm_salescode4") != null) p_br.getCell("stm_salescode4").setItemPropertyInterface(prdList);
			if(mode == JxZkBiBase.MODE_UPDATE) { 
				BiResult sr = p_br.getSubLink(
						((BiResultStmov)p_br).getStmdLinkName()
						);
				if(sr != null) {
					Vector v = sr.getRowCollectionList();
					for(int i=0;i<v.size();i++) {
						BiCellCollection bcol = (BiCellCollection) v.get(i);
						if(bcol.testCell("orddet_ref") != null) {
							if(bcol.getCellString("orddet_ref").equals("")) {
								UniLog.log("Set Default Custom Group");
								tr = su.getQueryResult("select stmd_mrg,stmd_ref from stmovd where stmd_tdtype = 'PD' and stmd_ref <> '' and stmd_irg = " + bcol.getCellInt("stmd_irg") + " order by stmd_mrg desc",null);
								if(tr.getRecordCount() > 0) {
									tr.setRecPointer(0);
									bcol.getCell("orddet_ref").set(tr.getFieldString("stmd_ref"));
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
}
