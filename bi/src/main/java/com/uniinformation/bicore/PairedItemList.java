package com.uniinformation.bicore;

import java.util.List;

import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.CellPair;

public class PairedItemList extends AbstractGetItemProperty{
	
	List<CellPair>itemList;
	public PairedItemList(List<CellPair> p_itemList) {
		itemList = p_itemList;
	}

	@Override
	public int getColumnCount(Object item) {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public String getString(Object item) {
		// TODO Auto-generated method stub
		int idx = itemList.indexOf(item);
		if(idx >= 0) return(itemList.get(idx).getRight().toString());
		return null;
	}

	@Override
	public int getRowWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getRow(int p_row) {
		// TODO Auto-generated method stub
		return (itemList.get(p_row));
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return itemList.size();
	}

	@Override
	public int getIndexOf(Object item) {
		// TODO Auto-generated method stub
		return(itemList.indexOf(item));
	}

}
