package com.kyoko.parser.excelformula;

import com.uniinformation.cell.CellException;

public interface ColumnTranslateInterface {
	public String cellColumnToBiColumn(String p_workSheet,int col) throws CellException;
	public int biColumnToCellColumn(String p_workSheet,String p_label) throws CellException;
	public String getWorkSheetFirstValuePosition(String p_worksheet);
	public String getCurrentSheetName();
	//public String getWorkSheetFirstHeaderPosition(String p_worksheet);
}
