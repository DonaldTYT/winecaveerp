package com.uniinformation.utils.poi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.kyoko.common.ReturnMsg;

public abstract class ExcelPoi {
	int excelSheetIdx = -1;
	String excelSheetName = null;
	int excelRowCount = 0;
	int excelDefaultRowHeight = 0;
	int excelCurrentRowHeight = 0;
	boolean excel_translateGTET = false;
	int excel_translateChinese = 0;

	public class CellRC {
		public int row;
		public int col;
		public int m;
		public int n;
		public CellRC(int p_row,int p_col, int p_m,int p_n) {
			row = p_row;
			col = p_col;
			m = p_m;
			n = p_n;
		}
	}
	
	public static ExcelPoi newExcelPoi(boolean isXlsx){
		if(isXlsx) {
			return(new ExcelPoiXlsx());
		} else {
			return(new ExcelPoiXls());
		}
	}
	public static ExcelPoi newExcelPoi(String p_filename,boolean p_isXlsx) throws IOException {
		ExcelPoi xpoi=null;
		FileInputStream is = new FileInputStream(p_filename);
		if(p_isXlsx) {
			xpoi = new ExcelPoiXlsx(is);
		} else {
			xpoi = new ExcelPoiXls(is);
		}
		is.close();
		return(xpoi);
	}
	public static ExcelPoi newExcelPoi(InputStream is, boolean isXlsx) throws IOException {
		if(isXlsx ) return(new ExcelPoiXlsx(is)); 
			else return(new ExcelPoiXls(is));
	}
	
	public abstract int getRowCount();
	public abstract boolean writeWorkBook(String p_filename);
	public abstract boolean writeWorkBook(OutputStream os);
	public abstract String getStringValue(int p_row,int p_col);
	public abstract Date getDateValue(int p_row,int p_col,String p_datefmt);
	public abstract Double getDoubleValue(int p_row,int p_col);
	public abstract Cell getCell(int p_row,int p_col);
	public abstract Sheet getSheet();
	public abstract Workbook getWorkbook();
	public abstract CellRC getNameRegion(String p_name);
	public abstract void excel_setStringValue(int p_row, int p_col, String p_value) throws Exception;
	public abstract void excel_setDateValue(int p_row, int p_col, java.util.Date p_javadate) throws Exception;
	public abstract void excel_setNumericValue(int p_row, int p_col, double p_value) throws Exception;
	public abstract void excel_setNumericValue(int p_row, int p_col, int p_value) throws Exception;
	public abstract void excel_setNumericValue(int p_row, int p_col, long p_value) throws Exception;
	public abstract void excel_setCellStyle(int p_row,int p_col,int p_styleIdx) throws Exception;
	public abstract int excel_getCellStyleIdx(int p_row,int p_col) throws Exception;
	public abstract void excel_MergeCells(int p_r1,int p_c1,int p_r2,int p_c2);
	public abstract int excel_getStyleGen(String p_format,String p_fillColor,String p_fontStyle,String p_fontColor,String p_locked);
	public abstract String excel_setCellStyle(int p_idx);
	public abstract String excel_setValues(int p_row,int p_col,Vector v);
	public abstract int excel_getColumnStyleIdx(int p_col);
	public abstract String excel_setDefaultColumnStyle(int p_col,int p_idx);
	public abstract String excel_setColumnValidation(int p_col,Vector p_validList);
	public abstract int excel_getFormatStyle(String p_format);
	public abstract String excel_setValuesWithStyle(int p_row,int p_col,Vector v);
	public abstract String excel_autoResizeColumn(int p_col);
	public abstract int excel_getColumnCount(int p_row);
	public abstract boolean excel_getZeroHeight(int p_row);
	public abstract boolean excel_isColumnHidden(int p_col);

	public abstract int excel_newSheet(String p_sheetname);
	public abstract int excel_getSheetIndex(String p_sheetname);
	public abstract String excel_getSheetName(int idx);
	public abstract void excel_setSheetName(int idx, String p_sheetname);
	public abstract int excel_getNumberOfSheets();
	public abstract ReturnMsg excel_useSheet(int idx);
	public abstract int excel_cloneSheet(int idx,String name);
	public abstract String excel_setColumnValidation(int p_col,String p_validateStr);
	public abstract String excel_shiftRow(int p_start,int p_end,int p_cnt);
	public abstract String excel_createFreezePane(int p_row,int p_col);
	public abstract void getStyleFormatByIdx(int p_idx) ;
	
	public Integer getIntegerValue(int p_row,int p_col)
	{
		Double dd = getDoubleValue(p_row,p_col);
		if(dd == null) return(null);
		return(new Integer ((int) dd.doubleValue()));
	}
	
	public Date getDateValue(int p_row,int p_col) {
		return(getDateValue(p_row,p_col,"yyyy/mm/dd"));
	}

	/* Chinese Translate */
	void excel_setTranslateGTET(boolean p_boolean)
	{
		excel_translateGTET = p_boolean;
	}
	String excel_translateGTET(String s)
	{
		if(s == null) return(null);
		char[] carr = s.toCharArray();
		for(int i = 0;i<carr.length;i++) {
			switch(carr[i]) {
			case 8807 : carr[i] = 8805; break;
			case 8806 : carr[i] = 8804; break;
			case 33274: carr[i] =21488; break;
			}
		}
		return(new String(carr));
	}
	public void excel_translate_Chinese(int p_sw) 
	{
		excel_translateChinese = p_sw;
	}
	
	public int getCurrentSheetIndex() {
		return(excelSheetIdx);
	}
	
	static public String cellRangeToString(String p_sheetName,int p_r1,int p_r2,int p_c1,int p_c2,boolean p_absolute) {
		CellRangeAddress cr = new CellRangeAddress(p_r1,p_r2,p_c1,p_c2);
		/*
		if(cr != null) {
			if(p_sheetName != null ) return(cr.formatAsString(p_sheetName, p_absolute)); else return(cr.formatAsString());
		} else return(null);
		*/
		return(cr.formatAsString(p_sheetName, p_absolute)); 
	}
	
}
