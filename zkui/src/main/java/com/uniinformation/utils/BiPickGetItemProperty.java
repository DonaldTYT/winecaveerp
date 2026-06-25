package com.uniinformation.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.AbstractGetItemProperty;

public class BiPickGetItemProperty extends AbstractGetItemProperty {
	private List<String> labelList;
	private List<String> headerList;
	private List<String> widthList;
	private BiResult br;
	private int listItemColumn;
	private Map<Integer, String[]> rowMap = new HashMap<>();
	
	
	public BiPickGetItemProperty(List<String> p_labelList, List<String> p_headerList, List<String> p_widthList) {
		super();
		labelList = p_labelList;
		headerList = p_headerList;
		widthList = p_widthList;
	}

	public BiPickGetItemProperty(List<String> p_labelList, List<String> p_widthList) {
		this(p_labelList, null, p_widthList);
	}

	public BiPickGetItemProperty(List<String> p_labelList) {
		this(p_labelList, null, null);
	}

	public void setBiResult(BiResult p_br) {
		rowMap.clear();
		br = p_br;
	}

	public BiResult getBiResult() {
		return br;
	}
	
	public void setListItemColumn(int col) {
		listItemColumn = col;
	}

	@Override
	public int getColumnCount(Object item) {
		return labelList.size();
	}

	@Override
	public String getString(Object item) {
		return ((String[])item)[labelList.size()];
	}

	@Override
	public Object getRow(int p_row) {
		if (rowMap.containsKey(p_row))
			return rowMap.get(p_row);
		br.loadOneRecV(p_row);
		List<String> list = labelList.stream().map(l -> br.getCellString(l)).collect(Collectors.toList());
		list.add(String.join(" ", list));
		String[] ss = list.toArray(new String[0]);
		rowMap.put(p_row, ss);
		return ss;
	}

	@Override
	public int getRowCount() {
		return br.getRowCount();
	}

	@Override
	public int getIndexOf(Object item) {
		return rowMap.entrySet().stream().filter(e -> {
			if (item instanceof String)
				return getString(e.getValue()).equals(item);
			else
				return e.getValue() == item;
		}).map(e -> e.getKey()).findFirst().orElse(-1);
	}

	@Override
	public Object getColumnValue(Object p_v, int p_col) {
		return ((String[])p_v)[p_col];
	}
	
	@Override
	public Object getHeader(Object p_v,int p_col) {
		if (headerList != null)
			return headerList.get(p_col);
		else
			return labelList.get(p_col);
	}
	
	@Override
	public String getColumnWidth(Object p_v,int p_col) {
		return widthList.get(p_col);
	}

	@Override
	public int getRowWidth() {
		return 0;
	}
	
	public String getListItemValue(Object p_v) {
		return (String)getColumnValue(p_v, listItemColumn);
	}

	public String getRowString(int p_row) {
		return getString(getRow(p_row));
	}

	public String getRowListItemValue(int p_row) {
		return getListItemValue(getRow(p_row));
	}
}
