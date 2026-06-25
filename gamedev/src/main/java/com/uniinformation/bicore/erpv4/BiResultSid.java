package com.uniinformation.bicore.erpv4;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.webcore.SessionHelper;

public class BiResultSid extends BiResultErpv4 {

	public BiResultSid(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void setLookupItemList(TableRec lookupTableTr,ColumnCell colCell) throws Exception {
		if(!colCell.getCellLabel().equals("ca_ano")) {
			super.setLookupItemList(lookupTableTr, colCell);
			return;
		}
		Vector <Object> lookupValues = new Vector<Object>();
		Hashtable<Object,String> ht = new Hashtable<Object,String>();
		for(int j = 0;j<lookupTableTr.getRecordCount();j++) {
			lookupTableTr.setRecPointer(j);
			Object oo = lookupTableTr.getField(colCell.getBiColumn().getField().getName());
			lookupValues.add(oo);
			String listString = 
					lookupTableTr.getFieldString("ca_ano") + " " +
					lookupTableTr.getFieldString("ca_aname");
			ht.put(oo,listString);
		}
		Vector<Comparable> vv = new Vector<Comparable>();
		for(Object o : lookupValues) {
			vv.add((Comparable) o);
		}
		Collections.sort(vv);
		GipiNamedItemList prdList;
		prdList = new GipiNamedItemList();
		for(int i=0;i<vv.size();i++) {
			prdList.appendItem( vv.get(i), ht.get(vv.get(i)));
		}
		colCell.setItemPropertyInterface(prdList);
		//colCell.setItemList(vv);
		colCell.setCCObj("lookup_uparent_tr", lookupTableTr);
		colCell.setCCObj("lookup_uparent_values", lookupValues);
	}
}
