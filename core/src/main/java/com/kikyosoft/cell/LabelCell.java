package com.kikyosoft.cell;

public class LabelCell {
	public static final int CTYPE_RADIOGROUP = 1;
	public static final int CTYPE_COMBOBOX = 2;
	String label;
	Cell cell;
	int type;
	public LabelCell(String p_label,Cell p_cell,int p_type)
	{
		label=p_label;
		cell = p_cell;
		type = p_type;
	}
	public LabelCell(String p_label,Cell p_cell)
	{
		label=p_label;
		cell = p_cell;
		type = 0;
	}
	public String getLabel()
	{
		return(label);
	}
	public Cell getCell()
	{
		return(cell);
	}
	public int getType()
	{
		return(type);
	}
}
