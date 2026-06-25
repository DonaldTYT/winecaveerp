package com.uniinformation.utils;

import java.util.List;

import com.uniinformation.cell.AbstractGetItemProperty;

public abstract class TranslateListGetItemProperty extends AbstractGetItemProperty {
	
	protected List itemList;
	public TranslateListGetItemProperty(List p_itemList) {
		super();
		itemList = p_itemList;
	}
	public abstract String translate(Object p_item);
	@Override 
	public int getRowCount() {
		return(itemList.size());
	}
	@Override 
	public int getColumnCount(Object p_item) {
		return(1);
	}
	@Override 
	public Object getRow(int p_row) {
		return(itemList.get(p_row));
	}
	@Override 
	public int getIndexOf(Object p_item) {
		return(itemList.indexOf(p_item));
	}
	@Override
	public String getString(Object p_item) {
		return(translate(p_item));
	}
}
