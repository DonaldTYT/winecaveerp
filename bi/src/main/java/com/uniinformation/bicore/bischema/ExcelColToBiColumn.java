package com.uniinformation.bicore.bischema;

import java.util.HashMap;
import java.util.Vector;

import com.kyoko.parser.excelformula.ColumnTranslateInterface;
import com.kyoko.parser.excelformula.ExcelCellRef;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.webcore.SessionHelper;

public class ExcelColToBiColumn implements ColumnTranslateInterface {
	
	SessionHelper sh;
	HashMap<String,String[]> xlsToBiMap;
	HashMap<String,String> firstPositionMap;
	
	public ExcelColToBiColumn(SessionHelper p_sh,HashMap<String,String[]> p_xlstobimap,HashMap<String,String> p_firstPositionMap) {
		xlsToBiMap = (HashMap<String,String[]>) p_xlstobimap.clone();
		firstPositionMap = p_firstPositionMap;
		sh = p_sh;
	}

	@Override
	public String cellColumnToBiColumn(String p_workSheet, int p_col) throws CellException {
		// TODO Auto-generated method stub
		String[] colStr = xlsToBiMap.get(p_workSheet);
		if(colStr != null) return(colStr[p_col]);
		if(p_workSheet == null) return(null);
		BiView bv = sh.getBiSchema().getViewByName(p_workSheet);
		if(bv == null) {
			return("col_" + ExcelCellRef.encodeExcelRC(p_col,Integer.MAX_VALUE,false,true).toLowerCase());
		}
		Vector<BiColumn> cl = bv.getColumns();
		colStr = new String[cl.size()];
		for(int i=0;i<colStr.length;i++) {
			colStr[i] = cl.get(i).getLabel();
		}
		xlsToBiMap.put(p_workSheet, colStr);
		return colStr[p_col];
	}

	@Override
	public int biColumnToCellColumn(String p_workSheet, String p_label) throws CellException {
		// TODO Auto-generated method stub
		throw new CellException("Method not implemented");
	}

	@Override
	public String getWorkSheetFirstValuePosition(String p_worksheet) {
		// TODO Auto-generated method stub
		String ss = firstPositionMap.get(p_worksheet);
		if(ss != null) return(ss);
		return("A2");
	}

	@Override
	public String getCurrentSheetName() {
		// TODO Auto-generated method stub
		return null;
	}
}
