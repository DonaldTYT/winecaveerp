package com.uniinformation.jxapp;

import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Idspace;

import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.rpccall.RpcClient;
//import com.uniinformation.utils.ConstUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class AfsStockSet extends JxZkBiBase {
	class AfsStockSetDetGetItemProperty extends BiGetItemProperty {
		AfsIcodePicker jxf = null;
		AfsStockSetDetGetItemProperty(BiResult br) {
			super(br);
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
							jxf = (AfsIcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"AfsIcodePicker");
						}
//						AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
						zjpi.setPopupWidth("700px");
						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
						jxf.setPickerForAnyStock(AfsStockSet.this,getBr().getSelectUtil(), new Wherecl().appendString(" and st_mtype in ('M','O','P') "),bcc,null,null);
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
		
	}
	@Override
	public void bindCellCollection(BiResult p_br,int p_mode) {
//		super.bindCellCollection(p_br, p_mode);
		boolean isNew;
		isNew = getGipi("AfsStockSetDet") == null;
		if(isNew) {
			setGipi("AfsStockSetDet",new AfsStockSetDetGetItemProperty(p_br.getSubLink("AfsStockSetDet")));	
		}
		super.bindCellCollection(p_br, p_mode);
		
	}	
}
