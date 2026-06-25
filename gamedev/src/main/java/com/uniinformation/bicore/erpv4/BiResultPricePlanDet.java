package com.uniinformation.bicore.erpv4;

import java.util.Hashtable;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPricePlanDet extends BiResultErpv4 {
	TranslateListGetItemProperty priceTypeList = null;
	protected String stockViewId = null;
	public BiResultPricePlanDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_col) {
		return(new Erpv4PriceDetCellCollection(p_col,this));
	};
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		if(stockViewId == null) stockViewId = "erpv4.stock";
		if(p_col.testCell("prpd_priceclass") != null) {
			if(priceTypeList == null) {
				final Hashtable<String,String> ht = new Hashtable();
//				ht.put(BiResultCustomer.dft_Price0_label,stockViewId.toUpperCase()+BiResultStock.key_Price0);
				ht.put("Manual","ADDITONAL_LABEL_manual");
				ht.put(BiResultCustomer.dft_Price1_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price1);
				ht.put(BiResultCustomer.dft_Price2_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price2);
				ht.put(BiResultCustomer.dft_Price3_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price3);
				priceTypeList = new TranslateListGetItemProperty(
					new VectorUtil()
					.addElement("Manual")
					.addElement(BiResultCustomer.dft_Price1_label)
					.addElement(BiResultCustomer.dft_Price2_label)
					.addElement(BiResultCustomer.dft_Price3_label)
					.toVector()
						) {

					@Override
					public String translate(Object p_item) {
						return(TranslateUtil.getText(getSessionHelper(), ht.get((String) p_item), "LABEL", (String) p_item));
						// TODO Auto-generated method stub
					}

					@Override
					public int getRowWidth() {
						// TODO Auto-generated method stub
						return 0;
					}};
					
			}
			p_col.getCell("prpd_priceclass").setItemPropertyInterface(priceTypeList);
		}
	}
}
