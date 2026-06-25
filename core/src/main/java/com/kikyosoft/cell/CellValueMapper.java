package com.kikyosoft.cell;

import java.util.*;

public interface CellValueMapper
{
	public void cellMap_bind(Cell p_value);
	public void cellMap_valchange(Cell p_cell);
	public void cellMap_modechange(Cell p_cell);
	public void cellMap_hintchange(Cell p_cell);
	public void cellMap_listchange(Cell p_cell);
//	public void cellMap_formatchange(Cell p_cell,String p_format);
	public void cellMap_formatchange(Cell p_cell);
	public void cellMap_formulachange(Cell p_cell);
}
