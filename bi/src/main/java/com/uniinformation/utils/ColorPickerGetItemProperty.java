package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.Hashtable;

import com.uniinformation.cell.AbstractGetItemProperty;

public class ColorPickerGetItemProperty extends AbstractGetItemProperty {
	ArrayList<Integer> colorList;
	Hashtable<Integer,String> colorHash;
	int customColor = 0;
	public ColorPickerGetItemProperty() {
		super();
		addColor("Black",0x000000);
		addColor("Red",0xff0000);
		addColor("Blue",0x0000ff);
		addColor("Green",0x00ff00);
	}
	
	@Override
	public int getColumnCount(Object item) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getString(Object item) {
		// TODO Auto-generated method stub
		return colorHash.get(item);
	}

	@Override
	public Object getRow(int p_row) {
		// TODO Auto-generated method stub
		return (colorList.get(p_row));
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return (colorList.size());
	}

	@Override
	public int getIndexOf(Object item) {
		// TODO Auto-generated method stub
		return (colorList.indexOf(item));
	}
	
	public void addColor(String p_name,int p_color) {
		if(colorList == null) {
			colorList = new ArrayList();
		}
		if(colorHash == null) {
			colorHash = new Hashtable();
		}
		colorHash.put(p_color,p_name);
		if(colorList.indexOf(p_color) < 0) {
			colorList.add(p_color);
		}
	}

	@Override
	public int getRowWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

}
