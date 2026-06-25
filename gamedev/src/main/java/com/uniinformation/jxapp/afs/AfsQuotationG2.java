package com.uniinformation.jxapp.afs;

import java.util.Date;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tabbox;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.bicore.erpv4.Erpv4StockAttribute;
import com.uniinformation.bicore.erpv4.BiResultQuotation.QUOMODE;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4.QuotationG2;
import com.uniinformation.utils.UniLog;

public class AfsQuotationG2 extends QuotationG2{
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		QUOMODE quoMode = ((BiResultQuotationG2) getBr()).getQuomode();
		jxSetVisible("btPrintQuo", quoMode == QUOMODE.QUOTATION);
		jxSetVisible("btPrintCon", quoMode == QUOMODE.ORDER);
		if(mode == JxZkBiBase.MODE_ADD) {
			jxSetVisible("tp_invoices",false);
			jxSetVisible("tp_compstatus",false);
			jxSetVisible("tp_attach",true);
			((Tabbox)jxAdd("zkbiListTop").getNativeObject()).setSelectedIndex(0);;
			//vd_priceclass default 零售價
			Listbox lb = (Listbox)jxAdd("vd_priceclass").getNativeObject();
			for (Listitem li : lb.getItems()) {
				if (StringUtils.equals((String)li.getValue(), Erpv4StockAttribute.dft_Price0_label)) {
					lb.setSelectedItem(li);
					break;
				}
			}
		}
		if(mode == JxZkBiBase.MODE_UPDATE) {
			jxSetVisible("tp_attach",true);
			if(br.getCellString("inv_quostatus").equals("Confirmed")) {
				jxSetVisible("tp_invoices",true);
				jxSetVisible("tp_compstatus",true);
			} else {
				jxSetVisible("tp_invoices",false);
				jxSetVisible("tp_compstatus",false);
				((Tabbox)jxAdd("zkbiListTop").getNativeObject()).setSelectedIndex(0);;
			}
		}
	}
	

	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldChange("inv_delidays") {
			public boolean valueChanged(JxField fd, String orgValue){  
				UniLog.log1("inv_delidays changed newValue:%s, orgValue:%s", fd.getValue(), orgValue);
				try {
					Date quoDate = getBr().getCellDate(((BiResultQuotationG2) getBr()).getQuomode() == QUOMODE.ORDER ? "inv_date" : "inv_quodate");
					getBr().getCell("inv_delidate").set(DateUtil.nextday(quoDate, NumberUtils.toInt((String)fd.getValue())));
				} catch (Exception e) {
					UniLog.log(e);
					return false;
				}
				return true;
			}
		};
		new JxFieldChange("inv_delidate") {
			public boolean valueChanged(JxField fd, String orgValue){  
				UniLog.log1("inv_delidate changed newValue:%s, orgValue:%s", fd.getValue(), orgValue);
				try {
					Date deliDate = DateUtil.dateTimeStrToDate((String)fd.getValue());
					Date quoDate = getBr().getCellDate(((BiResultQuotationG2) getBr()).getQuomode() == QUOMODE.ORDER ? "inv_date" : "inv_quodate");
					getBr().getCell("inv_delidays").set((int)((deliDate.getTime() - quoDate.getTime()) / 86400000));
				} catch (Exception e) {
					UniLog.log(e);
					return false;
				}
				return true;
			}
		};
	}
}
