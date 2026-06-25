package com.uniinformation.utils;

import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.ChineseConvert;
import com.uniinformation.cell.AbstractGetItemProperty;

public class GipiNamedItemList extends AbstractGetItemProperty {
	private boolean fIgnoreEncoding = false;  //230321 handle schn ui. config via ini
	
	Hashtable<Object,String> nameHash; //key by obj value; value for display only, for multiple column item, it maybe concat stringa e.g. col1:col2
	Vector<Object>itemList;
	public GipiNamedItemList(boolean p_fIgnoreEncoding) {
		itemList = new Vector<Object>();
		nameHash = new Hashtable<Object,String>();
		fIgnoreEncoding = p_fIgnoreEncoding;
	}
	public GipiNamedItemList() {
		this(false);
	}

	@Override
	public int getColumnCount(Object item) {
		// TODO Auto-generated method stub
		return (1);
	}

	/***
	 * obtain name by value
	 */
	@Override
	public String getString(Object item) {
		// TODO Auto-generated method stub
		
		//230321 dirty way to handle param is schn but data strcture is tchn
		if (fIgnoreEncoding && item instanceof String) {
			return nameHash.get(ChineseConvert.convertAuto2Bnew((String)item));
		}
		return nameHash.get(item);
	}

	@Override
	public int getRowWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getRow(int p_row) {
		// TODO Auto-generated method stub
		return itemList.get(p_row);
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return itemList.size();
	}

	@Override
	public int getIndexOf(Object item) {
		// TODO Auto-generated method stub
		
		//230321 dirty way to handle param is schn but data strcture is tchn
		if (fIgnoreEncoding && item instanceof String) {
			return itemList.indexOf(ChineseConvert.convertAuto2Bnew((String)item));
		}
		return itemList.indexOf(item);
	}
	
	public GipiNamedItemList appendItem(Object p_value,String p_name) {
		itemList.add(p_value);
		nameHash.put(p_value, p_name);
		return(this);
	}
	
	/***
	 * obtain name by row
	 * can handle single column item only
	 * @param p_row
	 * @return
	 */
	public String getName(int p_row) {
		Object rowObj = getRow(p_row);
		if (rowObj == null) {
			return null;
		}
		if (getColumnCount(rowObj) != 1) {
			UniLog.log1("warning. not support multiple column item");
		}
		return getString(rowObj);
	}
}