package com.uniinformation.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.uniinformation.cell.AbstractGetItemProperty;

public class ListGetItemProperty extends AbstractGetItemProperty {
	protected List<String> widthList;
	protected List<String[]> valueList;

	public ListGetItemProperty() {
		super();
	}

	public ListGetItemProperty(List<String[]> p_valueList) {
		setValueList(p_valueList);
	}
	
	public void setWidthList(List<String> p_widthList) {
		widthList = p_widthList;
	}

	public void setValueList(List<String[]> p_valueList) {
		valueList = p_valueList;
	}
	
	public List<String[]> getValueList() {
		return valueList;
	}
	
	@Override
	public int getColumnCount(Object item) {
		return widthList.size();
	}

	@Override
	public String getString(Object item) {
		return Arrays.stream((String[])item).collect(Collectors.joining(" "));
	}

	@Override
	public Object getColumnValue(Object p_v, int p_col) {
		return ((String[])p_v)[p_col];
	}

	@Override
	public int getRowWidth() {
		return 0;
	}

	@Override
	public Object getRow(int p_row) {
		return valueList.get(p_row);
	}

	@Override
	public int getRowCount() {
		return valueList.size();
	}

	@Override
	public int getIndexOf(Object item) {
		return valueList.indexOf(item);
	}

	@Override
	public String getColumnWidth(Object p_v, int p_col) {
		return widthList.get(p_col);
	}
	
	public static class SelectorPick extends ListGetItemProperty {

		public SelectorPick() {
			super();
		}

		public SelectorPick(List<String[]> p_valueList) {
			super(p_valueList);
		}

		public void setOneColValueList(List<String> p_valueList) {
			setValueList(p_valueList.stream().map(s -> new String[] {s}).collect(Collectors.toList()));
		}

		@Override
		public Object getRow(int p_row) {
			return Arrays.stream(valueList.get(p_row)).collect(Collectors.joining(" "));
		}

		@Override
		public String getString(Object item) {
			return item.toString();
		}

		@Override
		public int getIndexOf(Object item) {
			return IntStream.range(0, valueList.size()).filter(i -> Objects.equals(item, getRow(i))).findFirst().orElse(-1);
		}
	}
}
