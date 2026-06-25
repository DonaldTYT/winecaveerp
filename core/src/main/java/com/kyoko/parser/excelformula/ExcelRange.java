package com.kyoko.parser.excelformula;
import org.apache.commons.lang3.tuple.Pair;
import com.kyoko.parser.*;
import com.kyoko.common.CoreLog;

public class ExcelRange {
	String sheetName = null;
	
	String cellRange;
	boolean rowAbsolute0 = false;
	boolean rowAbsolute1 = false;
	int rowIdx0;
	int colIdx0;
	int rowIdx1;
	int colIdx1;
	CellPositionInterface cellPositionInterface;
	public ExcelRange (String p_cellRange, VariableInterface p_interface,CellPositionInterface p_cellPositionInterface) throws Exception {
		cellRange = p_cellRange;
		cellPositionInterface = p_cellPositionInterface;
		Pair<String,String> p0 = ExcelCellRef.splitStringTo2(p_cellRange,'!');
		sheetName = p0.getLeft();
		if(sheetName != null && sheetName.startsWith("'")) {
			sheetName = sheetName.substring(1,sheetName.length()-1);
		}
		Pair<String,String> r0 = ExcelCellRef.splitStringTo2(p0.getRight(),':');
	
		Pair<Boolean,Integer>[] p1 = ExcelCellRef.decodeExcelRC(r0.getLeft());
		colIdx0 = p1[0].getRight(); 
		rowAbsolute0 = p1[1].getLeft(); 
		rowIdx0 = p1[1].getRight(); 
		if(!rowAbsolute0) rowIdx0 -= cellPositionInterface.getRowIdx();

		p1 = ExcelCellRef.decodeExcelRC(r0.getRight());
		colIdx1 = p1[0].getRight(); 
		rowAbsolute1 = p1[1].getLeft(); 
		rowIdx1 = p1[1].getRight(); 
		if(!rowAbsolute1) rowIdx1 -= cellPositionInterface.getRowIdx();
	}

	public boolean getRowAbsolute0() {
		return(rowAbsolute0);
	}
	public boolean getRowAbsolute1() {
		return(rowAbsolute1);
	}
	public String getWorkSheet() {
		return(sheetName);
	}
	public int getColIdx0() {
		return(colIdx0);
	}
	public int getColIdx1() {
		return(colIdx1);
	}
	public int getRowIdx0() {
		return(rowIdx0);
	}
	public int getRowIdx1() {
		return(rowIdx1);
	}
	
	public String toString() {
		int r0,r1;
		String sheetRef = "";
		if(sheetName != null) {
			if(sheetName.indexOf(" ") >= 0) {
				sheetRef = "'"+sheetName+"'!";
			} else {
				sheetRef = sheetName+"!";
			}
		}
		r0 = rowIdx0;
		if(!rowAbsolute0) r0 += cellPositionInterface.getRowIdx();
		r1 = rowIdx1;
		if(!rowAbsolute1) r1 += cellPositionInterface.getRowIdx();
		return(sheetRef+ExcelCellRef.encodeExcelRC(colIdx0,r0,true,rowAbsolute0)+":"+ExcelCellRef.encodeExcelRC(colIdx1,r1,true,rowAbsolute1));
	}
	
	public Variable getStartVariable(VariableInterface p_variableInterface,ColumnTranslateInterface xlsToBiColumnMapper) throws Exception {
		String biViewName = null;
		String realSheetName;
		if(sheetName != null && sheetName.equals(xlsToBiColumnMapper.getCurrentSheetName())) realSheetName = null; else realSheetName = sheetName;
		Pair<Boolean,Integer> zeroOfs[] = ExcelCellRef.decodeExcelRC(xlsToBiColumnMapper.getWorkSheetFirstValuePosition(sheetName));
		String colName = xlsToBiColumnMapper.cellColumnToBiColumn(sheetName, colIdx0-zeroOfs[0].getRight());
		CoreLog.log("Convert column start number to name [" + sheetName +"] " + (colIdx0-zeroOfs[0].getRight()) + " -> " + colName);
		if(realSheetName != null) {
			colName = ExcelTranslate.normalizeTag(sheetName)+ "!" + colName;
		}
		int r = rowIdx0;
		if(rowIdx0 == Integer.MAX_VALUE) {
			return new Variable (colName,p_variableInterface,0,true);
		} else {
			if(rowAbsolute0) {
				r -= zeroOfs[1].getRight();
			} else {
				Pair<Boolean,Integer> zeroOfs0[] = ExcelCellRef.decodeExcelRC(xlsToBiColumnMapper.getWorkSheetFirstValuePosition(null));
				r = r + zeroOfs0[1].getRight() - zeroOfs[1].getRight();
			}
			return new Variable (colName,p_variableInterface,r,rowAbsolute0);
		}
	}
	public Variable getEndVariable(VariableInterface p_variableInterface,ColumnTranslateInterface xlsToBiColumnMapper) throws Exception {
		Pair<Boolean,Integer> zeroOfs[] = ExcelCellRef.decodeExcelRC(xlsToBiColumnMapper.getWorkSheetFirstValuePosition(sheetName));
		String colName = xlsToBiColumnMapper.cellColumnToBiColumn(sheetName, colIdx1-zeroOfs[0].getRight());
		CoreLog.log("Convert column end number to name [" + sheetName +"] " + (colIdx0-zeroOfs[0].getRight()) + " -> " + colName);
		int r = rowIdx1;
		if(rowIdx1 == Integer.MAX_VALUE) {
			return new Variable (colName,p_variableInterface,Integer.MAX_VALUE,true);
		} else {
			if(rowAbsolute1) {
				r -= zeroOfs[1].getRight();
			} else {
				Pair<Boolean,Integer> zeroOfs0[] = ExcelCellRef.decodeExcelRC(xlsToBiColumnMapper.getWorkSheetFirstValuePosition(null));
				r = r + zeroOfs0[1].getRight() - zeroOfs[1].getRight();
			}
			return new Variable (colName,p_variableInterface,r,rowAbsolute1);
		}
	}
	
	/*
	public Dataset toDataset(String[] p_colList,int p_colOfs,int p_rowOfs) {
		Dataset ds = new Dataset();
		ds.cols = new String[colIdx1-colIdx0+1];
		for(int i=colIdx0;i<=colIdx1;i++) {
			ds.cols[i-colIdx0] = p_colList[i-p_colOfs];
		}
		ds.startAbsolute = rowAbsolute0;
		if(rowAbsolute0) {
			ds.startRow = rowIdx0 - p_rowOfs;
		} else {
			ds.startRow = rowIdx0;
		}
		ds.endAbsolute = rowAbsolute1;
		if(rowAbsolute1) {
			ds.endRow = rowIdx1 - p_rowOfs;
		} else {
			ds.endRow = rowIdx1;
		}
		return(ds);
	}
	*/
}
