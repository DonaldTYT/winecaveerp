package com.uniinformation.utils.poi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
/*
import com.uniinformation.utils.ZkUtil.CheckedConsumer;
import com.uniinformation.utils.ZkUtil.CheckedConsumer2;
*/
import com.uniinformation.utils.poi.ExcelPoi.CellRC;

//import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class ExcelPoiXlsx extends ExcelPoi {
	public static boolean fDebug = false;
	XSSFWorkbook workBook = null;
	XSSFSheet workSheet = null;
	XSSFCellStyle xssf_cellStyle = null;
	Hashtable<String, Short> excel_cellStyle;
	Hashtable<String ,Short>  excel_indexedColorMap;
	Hashtable<String, Short> excel_font;	
	public ExcelPoiXlsx () {
		workBook = new XSSFWorkbook();
		workSheet = workBook.createSheet();
		excelSheetIdx = 0;
		excelSheetName = workBook.getSheetName(0);
		excelRowCount = 0;
		excelDefaultRowHeight = workSheet.getDefaultRowHeight();
		excelCurrentRowHeight = excelDefaultRowHeight;
		excel_initFormatStyle();	
		excelRowCount = workSheet.getLastRowNum()+1;
		if(excelRowCount <= 1) {
			if(workSheet.getRow(0) == null) workSheet.createRow(0);
			excelRowCount = 1;
		}
	}
	public ExcelPoiXlsx (InputStream is) throws IOException {
		workBook = new XSSFWorkbook(is);
		workSheet = workBook.getSheetAt(0);
		excelSheetIdx = 0;
		excelSheetName = workBook.getSheetName(0);
		excelDefaultRowHeight = workSheet.getDefaultRowHeight();
		excelCurrentRowHeight = excelDefaultRowHeight;
		excel_initFormatStyle();
		excelRowCount = workSheet.getLastRowNum()+1;
		if(excelRowCount <= 1) {
			if(workSheet.getRow(0) == null) workSheet.createRow(0);
			excelRowCount = 1;
		}
	}
	public int getRowCount() {
		return(workSheet.getLastRowNum()+1);
	}
	
	public boolean writeWorkBook(String p_filename)
	{
		try {
			FileOutputStream os = new FileOutputStream(p_filename);
			workBook.write(os);
			os.close();
			return(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			return(false);
		}	
	}
	public boolean writeWorkBook(OutputStream os)
	{
		try {
			workBook.write(os);
			os.close();
			return(true);
		} catch (Exception ex) {
			return(false);
		}
	}
	public String getStringValue(int p_row,int p_col)
	{
		XSSFRow row = null;
		if(workSheet == null) return(null);
		if(p_row > workSheet.getLastRowNum()) return(null);
		row = workSheet.getRow(p_row);
		if(row == null) return(null);
		XSSFCell cell = row.getCell( p_col);
		if(cell == null) return(null);
		switch(cell.getCellType()) {
		case XSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
											return("Y");
										} else {
											return("N");
										}
		case XSSFCell.CELL_TYPE_STRING: return(cell.getStringCellValue());
		case XSSFCell.CELL_TYPE_NUMERIC: //return("OK  "+DateUtil.toDateString(cell.getDateCellValue(),"yyyy/mm/dd")); return(cell.getNumericCellValue());
										return(new DataFormatter().formatCellValue(cell));
		case XSSFCell.CELL_TYPE_FORMULA: 
						switch(cell.getCachedFormulaResultType()) {
							case XSSFCell.CELL_TYPE_STRING: return(cell.getStringCellValue());
							case XSSFCell.CELL_TYPE_NUMERIC: return(new DataFormatter().formatCellValue(cell));
							case XSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
																return("Y");
															} else {
																return("N");
															}
							default : return(null);
						}
		default : return(null);
		}
		
	}
	
	public Date getDateValue(int p_row,int p_col,String p_datefmt)
	{
		XSSFRow row = null;
		if(workSheet == null) return(null);
		if(p_row > workSheet.getLastRowNum()) return(null);
		row = workSheet.getRow(p_row);
		if(row == null) return(null);
		XSSFCell cell = row.getCell( p_col);
		if(cell == null) return(null);
		switch(cell.getCellType()) {
		case XSSFCell.CELL_TYPE_BOOLEAN:return(null);
		case XSSFCell.CELL_TYPE_STRING: 
							if(p_datefmt == null) return(null);
							String ds = cell.getStringCellValue();
							return(DateUtil.getDate(ds, p_datefmt));
		case XSSFCell.CELL_TYPE_NUMERIC: return(cell.getDateCellValue());
		case XSSFCell.CELL_TYPE_FORMULA: 
						switch(cell.getCachedFormulaResultType()) {
							case XSSFCell.CELL_TYPE_STRING: return(null);
							case XSSFCell.CELL_TYPE_NUMERIC: return(cell.getDateCellValue());
							case XSSFCell.CELL_TYPE_BOOLEAN: return(null);
							default : return(null);
						}
		default : return(null);
		}
	}
	
	public Double getDoubleValue(int p_row,int p_col)
	{
		XSSFRow row = null;
		if(workSheet == null) return(null);
		if(p_row > workSheet.getLastRowNum()) return(null);
		row = workSheet.getRow(p_row);
		if(row == null) return(null);
		XSSFCell cell = row.getCell( p_col);
		if(cell == null) return(null);
		switch(cell.getCellType()) {
		case XSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
											return(new Double (1.0));
										} else {
											return(new Double (0.0));
										}
		case XSSFCell.CELL_TYPE_STRING: try {
										double d = Double.parseDouble(cell.getStringCellValue());
										return(new Double(d));
										} catch (NumberFormatException nex) {
										return(null);
										}
					
		case XSSFCell.CELL_TYPE_NUMERIC: return(cell.getNumericCellValue());
		case XSSFCell.CELL_TYPE_FORMULA: 
						switch(cell.getCachedFormulaResultType()) {
							case XSSFCell.CELL_TYPE_STRING: try {
											double d = Double.parseDouble(cell.getStringCellValue());
											return(new Double(d));
										} catch (NumberFormatException nex) {
											return(null);
										}
							case XSSFCell.CELL_TYPE_NUMERIC: return(cell.getNumericCellValue());
							case XSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
																return(new Double (1.0));
															} else {
																return(new Double (0.0));
															}
							default : return(null);
						}
		default : return(null);
		}
	}

	public Cell getCell(int p_row,int p_col) {
		XSSFRow row = null;
		if(workSheet == null) return(null);
		if(p_row > workSheet.getLastRowNum()) return(null);
		row = workSheet.getRow(p_row);
		if(row == null) return(null);
		return row.getCell( p_col);
	}

	public Sheet getSheet() {
		return workSheet;
	}

	public Workbook getWorkbook() {
		return workBook;
	}

/* Cell Name Methods */
	@Override
	public CellRC getNameRegion(String p_name){
		XSSFName nm = workBook.getName(p_name);
		if(nm != null) {
			String ss = nm.getRefersToFormula();
			if(ss != null) {
				AreaReference[] arefs = AreaReference.generateContiguous(ss);
				if(arefs != null && arefs.length == 1) {
					CellReference cr0 = arefs[0].getFirstCell();
					if(cr0 != null) {
						int r,c,m,n;
						r = cr0.getRow();
						c = cr0.getCol();
						CellReference cr1 = arefs[0].getLastCell();
						if(cr1 != null) {
							m = cr1.getRow()-r+1;
							n = cr1.getCol()-c+1;
						} else {
							m = n = 0;
						}
						return(new CellRC(r,c,m,n));
					} 
				}
				UniLog.log("HAHAXXX");
			}
		}	
		
		return(null);
	}
	
	
	public void excel_setStringValue(int p_row, int p_col, String p_value) throws Exception
	{
			XSSFRow row = null;
			String s;
//			UniLog.log("HAHA 2015 excel_setStringValue "+p_row+","+p_col+":["+p_value+"]");
			if(!excel_translateGTET) s = p_value; else s = excel_translateGTET(p_value);
			if(excel_translateChinese == 1) {
//				s = ChineseConvert.convertAuto2GSquare(s);
				s = ChineseConvert.convertB2G(ChineseConvert.convertG2B(s));
			} else {
				if(excel_translateChinese == 2) {
					s = ChineseConvert.convertAuto2BSquare(s);
				}
			}
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			XSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell(p_col);
				if(xssf_cellStyle != null) cell.setCellStyle(xssf_cellStyle);
			}
			cell.setCellValue(s);
	}
	public void excel_setDateValue(int p_row, int p_col, java.util.Date p_javadate) throws Exception
	{
			XSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			XSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell(p_col);
				if(xssf_cellStyle != null) cell.setCellStyle(xssf_cellStyle);
			}
			if(p_javadate != null) {
				cell.setCellValue(p_javadate);
			} else {
//				UniLog.log("HAHA 2017 setDateValue " + p_row + " : " + p_col + " null");
				cell.setCellValue((String) null);
			}	
	}
	public void excel_setNumericValue(int p_row, int p_col, double p_value) throws Exception
	{
			XSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			XSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell(p_col);
				if(xssf_cellStyle != null) cell.setCellStyle(xssf_cellStyle);
			}
			cell.setCellValue(p_value);
	}
	public void excel_setNumericValue(int p_row, int p_col, int p_value) throws Exception
	{
			XSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			XSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell( p_col);
				if(xssf_cellStyle != null) cell.setCellStyle(xssf_cellStyle);
			}
			cell.setCellValue(p_value);
	}
	public void excel_setNumericValue(int p_row, int p_col, long p_value) throws Exception
	{
			XSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			XSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell( p_col);
				if(xssf_cellStyle != null) cell.setCellStyle(xssf_cellStyle);
			}
			cell.setCellValue(p_value);
	}

	/* Merge Cells */
	public void excel_MergeCells(int p_r1,int p_c1,int p_r2,int p_c2)
	{
		workSheet.addMergedRegion(new CellRangeAddress(p_r1, p_r2, p_c1, p_c2));
	}
	
	public void excel_setCellStyle(int p_row,int p_col,int p_styleIdx) throws Exception
	{
		XSSFRow row = null;
		row = workSheet.getRow(p_row);
		XSSFCell cell = row.getCell(p_col);
		XSSFCellStyle styl = workBook.getCellStyleAt((short) p_styleIdx);
		cell.setCellStyle(styl);
	}
	
	public int excel_getCellStyleIdx(int p_row,int p_col) throws Exception
	{
		XSSFRow row = null;
		row = workSheet.getRow(p_row);
		XSSFCell cell = row.getCell(p_col);
		XSSFCellStyle styl = cell.getCellStyle();
		return(styl.getIndex());
	}

	public int excel_getStyleGen(String p_format,String p_fillColor,String p_fontStyle,String p_fontColor,String p_locked)
	{
		String ss = "";
		String sFormat;
		String sFillColor;
		String sFontStyle;
		String sFontColor;
		String sLocked;
		if(p_format == null) sFormat = "General"; else sFormat = p_format;
		if(p_fillColor == null) sFillColor = "AUTOMATIC"; else sFillColor = p_fillColor;
		if(p_fontStyle == null) sFontStyle = "Normal"; else sFontStyle = p_fontStyle;
		if(p_fontColor == null) sFontColor = "Black"; else sFontColor = p_fontColor;
		if(p_locked == null) sLocked = "unlocked"; else sLocked = p_locked;
		Short sidx = excel_cellStyle.get(sFormat+","+sFillColor+","+sFontStyle+","+sFontColor+","+sLocked);
		if(sidx != null) return(sidx.intValue());
		XSSFDataFormat df = workBook.createDataFormat();
		int dfIdx = df.getFormat(sFormat);
		if(dfIdx < 0) return(-1);
		XSSFCellStyle stl = workBook.createCellStyle();
		stl.setDataFormat((short) dfIdx);
		Short colIdx = excel_indexedColorMap.get(sFillColor);
		if(colIdx != null) {
			stl.setFillForegroundColor((short) colIdx.intValue());
			if(!sFillColor.equals("AUTOMATIC")) stl.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		}
		Short fidx = excel_font.get(sFontStyle+","+sFontColor);
		if(fidx == null) {
			XSSFFont ft = workBook.createFont();
			if(sFontColor.equals("red")) ft.setColor(XSSFFont.COLOR_RED); else ft.setColor(XSSFFont.COLOR_NORMAL);
			if(sFontStyle.contains("Bold")) ft.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD); else ft.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);
			if(sFontStyle.contains("Italic")) ft.setItalic(true); else ft.setItalic(false);
			stl.setFont(ft);
			excel_font.put(sFontStyle+","+sFontColor,ft.getIndex());
		} else {
			stl.setFont( workBook.getFontAt((short) fidx.intValue()));
		}
		stl.setLocked(sLocked.contains("locked"));
		if( sLocked.contains("center")) {
			stl.setAlignment(CellStyle.ALIGN_CENTER);
		}
		if( sLocked.contains("wrapped")) {
			stl.setWrapText(true);
		}
		if (sLocked.contains("alignTop")) {
			stl.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		} else if (sLocked.contains("alignMiddle")) {
			stl.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		} else {
			stl.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		}
//		stl.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
//		stl.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		
//		excel_cellStyle.put(p_format,new Short(stl.getIndex()));
		cacheOneCellStyleAndFont(stl); 
		return(stl.getIndex());
		
	}
	public String excel_setCellStyle(int p_idx)
	{
		if(workBook == null) return("FAIL WorkBook Not Opened");
		if(p_idx < 0) {
			xssf_cellStyle = null;
			return("OK");
		}
		if(p_idx >= workBook.getNumCellStyles()) return("FAIL CellStyle Not Created");
		xssf_cellStyle = workBook.getCellStyleAt((short) p_idx);
		return("OK");
	}	
	private void setCell(XSSFRow p_row, short p_col, Object p_val)
	{
		XSSFCell cell = p_row.createCell(p_col);
		if(xssf_cellStyle != null) cell.setCellStyle(xssf_cellStyle);
		if(p_val == null) return;
		if(p_val instanceof String) cell.setCellValue((String) p_val);
		else
			if(p_val instanceof Integer) 
				cell.setCellValue(((Integer) p_val).intValue());
			else
				if(p_val instanceof Double) 
					cell.setCellValue(((Double) p_val).doubleValue());
				else
					if(p_val instanceof java.util.Date) 
						cell.setCellValue((java.util.Date)  p_val);
					else
						if(p_val instanceof ExcelPoiFormula) 
							cell.setCellFormula(((ExcelPoiFormula) p_val).getFormula());
	}
	public String excel_setValues(int p_row,int p_col,Vector v)
	{
			XSSFRow row = null;
			if(workSheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			for(int i = 0;i<v.size();i++) {
				setCell(row, (short) (i + p_col), v.get(i));
			}
			return("OK");
	}
	public int excel_getColumnStyleIdx(int p_col) {
		if(workSheet == null) return(-1);
		CellStyle styl = workSheet.getColumnStyle(p_col);
		if(styl == null) return(-1);
		return(styl.getIndex());
	}
	public String excel_setDefaultColumnStyle(int p_col,int p_idx) {
		if(workBook == null) return("FAIL");
		if(workSheet == null) return("FAIL");
		if(p_idx < 0) {
			xssf_cellStyle = null;
			return("OK");
		}
		XSSFCellStyle thisCellStyle = null;
		if(p_idx >= workBook.getNumCellStyles()) return("FAIL CellStyle Not Created");
		if(p_idx >= 0) thisCellStyle = workBook.getCellStyleAt((short) p_idx);
		int cWidth = workSheet.getColumnWidth(p_col);
		workSheet.setDefaultColumnStyle(p_col, thisCellStyle); // seem poi 3.9 has a bug that for worksheet idx > 0, the columwidth is incorrect after setDefaultColumnStyle, get and set again to bypass this problem
		workSheet.setColumnWidth(p_col,cWidth);
//		workSheet.autoSizeColumn(p_col);
		return("OK");
	}

	// this code may not work , ported from XLS version but not verified
//	 private XSSFDataValidation setupSheetValidation(XSSFSheet p_sheet, int i,
//	        DataValidationConstraint validationConstraint) {
//	        CellRangeAddressList addressList = new CellRangeAddressList();
//	        XSSFDataValidation dataValidation = new XSSFDataValidation(
//	                (XSSFDataValidationConstraint) validationConstraint,
//	                addressList, null
//	                );
//	        addressList.addCellRangeAddress(1, i, 10000, i);
//	        dataValidation.setEmptyCellAllowed(true);
//	        dataValidation.setShowPromptBox(true);
//	        p_sheet.addValidationData(dataValidation);
//	        return dataValidation;
//	    }
//	
//	public String excel_setColumnValidation(int p_col,Vector p_validList) {
//		if(workBook == null) return("FAIL");
//		if(workSheet == null) return("FAIL");
//		XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(workSheet);
//		String [] vList = new String[p_validList.size()];
//		for(int i=0;i<vList.length;i++) {
//			vList[i] = (String) p_validList.get(i);
//		}
//		DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(vList);
//		setupSheetValidation(workSheet, p_col, validationConstraint );
//		return("OK");
//	}
	public int excel_getFormatStyle(String p_format)
	{
		return(excel_getStyleGen(p_format,null,null,null,null));
	}
	public String excel_setValuesWithStyle(int p_row,int p_col,Vector v)
	{
			XSSFRow row = null;
			if(workSheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			for(int i = 0;i<v.size();i++) {
				int styleidx;
				if(v.get(i) instanceof Double)
						styleidx = (int) ((Double) v.get(i)).doubleValue();
					else
						styleidx = ((Integer) v.get(i)).intValue();
				if(xssf_cellStyle == null || 
						xssf_cellStyle.getIndex() != styleidx) {
						xssf_cellStyle = workBook.getCellStyleAt((short) styleidx);
				}
				i++;
				setCell(row, (short) (i / 2 + p_col), v.get(i));
			}
			return("OK");
	}
	public String excel_autoResizeColumn(int p_col)
	{
		if(workBook == null) return("FAIL");
		if(workSheet == null) return("FAIL");
		workSheet.autoSizeColumn(p_col);
		return("OK");
	}
	void excel_initFormatStyle()
	{
			excel_cellStyle = new Hashtable<String,Short>();
			excel_font = new Hashtable<String,Short>();
			
			if(excel_indexedColorMap == null) {
				excel_indexedColorMap= new Hashtable<String,Short>();
				for(int i = 0;i<IndexedColors.values().length;i++) {
					IndexedColors ic = IndexedColors.values()[i];
					if (fDebug) UniLog.log1("IndexedColors " + i + " : " + ic.name() + " : " + ic.getIndex());
					excel_indexedColorMap.put(ic.name(), ic.getIndex());
				}
			}
			cacheOneCellStyleAndFont(workBook.getCellStyleAt((short) 0)); // assume CellStyle(0) as default;
	}	
	void cacheOneCellStyleAndFont(XSSFCellStyle sty) {
		int fontIdx = sty.getFontIndex();
		XSSFFont ft = workBook.getFontAt((short) fontIdx);
		if (fDebug) UniLog.log1("cache CellStyle " + sty.getIndex() + " fontidx " + fontIdx + " key " +  makeXSSFCellStyleStringHash(sty));
		String sh = makeXSSFCellStyleStringHash(sty);
		if(sh != null ) {
			if(excel_cellStyle.get(sh) == null) {
				String ss = getStyleFromFont(ft)+","+getColorFromFont(ft);
				Short fidx = excel_font.get( ss );
				if(fidx == null) {
					if (fDebug) UniLog.log1("add XSSFFont " +  ss + " idx " + fontIdx );
					excel_font.put(ss, new Short(ft.getIndex()));
				}
				if (fDebug) UniLog.log1("add CellStyleHash " +  sh + " idx " + sty.getIndex());
				excel_cellStyle.put( sh, sty.getIndex());
			}
		}
		
	}
	String makeXSSFCellStyleStringHash(XSSFCellStyle p_style) {
		String s = "";
		String ss = getFormatFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += ss;
		ss = getFillColorFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += "," + ss;
		int fontIdx = p_style.getFontIndex();
		XSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		ss = getStyleFromFont(ft) ;
		if( ss == null ) return(null);
		s += "," + ss;
		ss = getColorFromFont(ft) ;
		if( ss == null ) return(null);
		s += "," + ss;
		ss = getLockedFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += "," + ss;
		return(s);
	}
	String getStyleFromFont(XSSFFont ft) {
		String ss = "";
		if(ft.getBoldweight() == XSSFFont.BOLDWEIGHT_BOLD) ss += "Bold";
		if(ft.getItalic()) ss+= "Italic";
		if(ss.equals("")) ss = "Normal";
		return(ss);
	}
	String getFormatFromStyle(XSSFCellStyle sty) {
		return(sty.getDataFormatString());
	}
	String getColorFromFont(XSSFFont ft) {
		if(ft.getColor() == XSSFFont.COLOR_RED) return("red");else return("Black");
	}
	String getFillColorFromStyle(XSSFCellStyle sty) {
		int bgcol = sty.getFillForegroundColor();
		for(String key : excel_indexedColorMap.keySet()) {
			Short ic = excel_indexedColorMap.get(key);
			if(ic.intValue() == bgcol) return(key);
		}
		UniLog.log(" getFillColorFromStyle index " + bgcol + " unmapped");
		return(null);
	}
	String getLockedFromStyle(XSSFCellStyle sty) {
		if(sty.getLocked()) return("locked") ; else return("unlocked");
	}

	public int excel_getColumnCount(int p_row) {
		XSSFRow row = null;
		if(workSheet == null) return(0);
		if(p_row > workSheet.getLastRowNum()) return(0);
		row = workSheet.getRow(p_row);
		if(row == null) return(0);
		return(row.getLastCellNum());
	}
	
	public boolean excel_getZeroHeight(int p_row) {
		XSSFRow row = null;
		if(workSheet == null) return(false);
		if(p_row > workSheet.getLastRowNum()) return(false);
		row = workSheet.getRow(p_row);
		if(row == null) return(false);
		return(row.getZeroHeight());
	}

	public boolean excel_isColumnHidden(int p_col) {
		if(workSheet == null) return(false);
		return workSheet.isColumnHidden(p_col);
	}
	
	public String excel_setColumnValidation(int p_col,Vector p_validList) {
		if(workBook == null) return("FAIL");
		if(workSheet == null) return("FAIL");
		XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(workSheet);
		String [] vList = new String[p_validList.size()];
		for(int i=0;i<vList.length;i++) {
//			vList[i] = (String) p_validList.get(i);
			vList[i] = p_validList.get(i).toString();
		}
		DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(vList);
		CellRangeAddressList addressList = new CellRangeAddressList(1,10000, p_col, p_col);
		XSSFDataValidation validation = (XSSFDataValidation) validationHelper.createValidation( validationConstraint, addressList);
		validation.setShowErrorBox(true);
		workSheet.addValidationData(validation);		
		return("OK");
	}
	public String excel_setColumnValidation(int p_col,String validateStr) {
		if(workBook == null) return("FAIL");
		if(workSheet == null) return("FAIL");
		XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(workSheet);
		DataValidationConstraint validationConstraint = validationHelper.createFormulaListConstraint(validateStr);
		CellRangeAddressList addressList = new CellRangeAddressList(1,10000, p_col, p_col);
		XSSFDataValidation validation = (XSSFDataValidation) validationHelper.createValidation( validationConstraint, addressList);
		validation.setShowErrorBox(true);
		workSheet.addValidationData(validation);		
		return("OK");
	}

	public int excel_newSheet(String p_sheetname) {
		if(workBook == null) return(-1);
   		XSSFSheet newsheet = workBook.createSheet(p_sheetname);
   		if(newsheet == null) return(-1);
   		return(workBook.getSheetIndex(p_sheetname));
	}

	public int excel_getSheetIndex(String p_sheetname) {
		if(workBook == null)
			return(-1);
		XSSFSheet newsheet = workBook.createSheet(p_sheetname);
		int idx = workBook.getSheetIndex(p_sheetname);
		return(idx);
	}
	public String excel_getSheetName(int idx) {
		if(workBook == null) return(null);
		String sheetName = workBook.getSheetName(idx);
		return(sheetName);
	}
	public void excel_setSheetName(int idx, String p_sheetname) {
		if(workBook == null) return;
		workBook.setSheetName(idx, p_sheetname);
	}
	public int excel_getNumberOfSheets()
	{
		if(workBook == null) return 0;
		return workBook.getNumberOfSheets();
	}
	public ReturnMsg excel_useSheet(int idx) {
		if(workBook == null) return(new ReturnMsg(false,"Work Book is null"));
		XSSFSheet sheet = workBook.getSheetAt(idx);
		if(sheet == null) return(new ReturnMsg(false,"Work sheet index out of range"));
		excelSheetIdx = idx;
		excelSheetName = workBook.getSheetName(idx);
		workSheet = sheet;
		excelDefaultRowHeight = workSheet.getDefaultRowHeight();
		excelRowCount = workSheet.getLastRowNum()+1;
		if(excelRowCount <= 1) {
			if(workSheet.getRow(0) == null) workSheet.createRow(0);
			excelRowCount = 1;
		}
		return(ReturnMsg.defaultOk);
	}

	public int excel_cloneSheet(int p_sheetIdx,String p_name) 
	{
		if(workBook != null) {
			XSSFSheet newSheet = workBook.cloneSheet(p_sheetIdx);
			if(newSheet != null) {
				String s = newSheet.getSheetName();
				int idx = workBook.getSheetIndex(s);
				workBook.setSheetName(idx, p_name);
				return(idx);
			}
		}
		return(-1);
	}
	
	public String excel_shiftRow(int p_start, int p_end, int p_cnt) 
	{
		int endRow;
		if(workSheet != null && p_start >= 0 && p_start < excelRowCount) {
			if(p_end < 0) endRow = excelRowCount; else endRow = p_end;
			workSheet.shiftRows(p_start,endRow,p_cnt);
			if(p_cnt > 0) {
				for(int i=0;i<p_cnt;i++) {
					workSheet.createRow(p_start+i);
				}
			}
			excelRowCount = workSheet.getLastRowNum()+1;
			return("OK");
		}
		return("FAIL");
	}
	public String excel_createFreezePane(int p_row,int p_col) {
		if(workSheet != null ) {
			workSheet.createFreezePane(p_col, p_row);
		}
		return("OK");
	}
	
	public void getStyleFormatByIdx(int p_idx) {
		if(workBook == null) return;
		CellStyle styl = workBook.getCellStyleAt((short) p_idx);
		String ss = styl.getDataFormatString();
		UniLog.log(ss);
	}
	
	/*
	public static void quickReadExcel(String filePath, String sheetName, CheckedConsumer2<Integer, Row> cb) throws Exception {
		try (FileInputStream fis = new FileInputStream(filePath)) {
			Workbook workbook = new XSSFWorkbook(fis);
			Sheet sheet = workbook.getSheet(sheetName);
			for (int i = 0; i <= sheet.getLastRowNum(); i++)
				cb.accept(i, sheet.getRow(i));
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static void quickWriteExcel(String filePath, String sheetName, CheckedConsumer<Sheet> cb) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			Workbook workbook = new XSSFWorkbook();
			cb.accept(workbook.createSheet(sheetName));
			workbook.write(fos);
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	public static void main(String args[]) throws Exception{
		quickWriteExcel("/tmp/tmp.xlsx", "sheet1", writeSheet -> {
			Map<String, Integer> map = new HashMap<String, Integer>();
			int[] writeRowNums = new int[] {0};
			quickReadExcel("/tmp/ownepoint.xlsx", "ownepoint", (rowNum, row) -> {
				if (row == null || StringUtils.isBlank(row.getCell(0).getStringCellValue()))
					return;
				String code = row.getCell(0).getStringCellValue();
				Integer count = map.get(code);
				if (count == null)
					count = 0;
				if (count >= 50)
					return;
				Row writeRow = writeSheet.createRow(writeRowNums[0]);
				for (int i = 0; i < 9; i++) {
					Cell cell = row.getCell(i);
					UniLog.log1("type:%d", cell.getCellType());
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_NUMERIC:
						writeRow.createCell(i).setCellValue(cell.getNumericCellValue());
						break;
					case XSSFCell.CELL_TYPE_FORMULA: 
						UniLog.log1("type1:%d, i:%d, row:%d", cell.getCachedFormulaResultType(), i, writeRowNums[0]);
						switch(cell.getCachedFormulaResultType()) {
							case XSSFCell.CELL_TYPE_NUMERIC: 
								writeRow.createCell(i).setCellValue(new DataFormatter().formatCellValue(cell));
								break;
							default:
								writeRow.createCell(i).setCellValue(cell.getStringCellValue());
								break;
						}
						break;
					default:
						writeRow.createCell(i).setCellValue(cell.getStringCellValue());
						break;
					}
				}
				map.put(code, ++count);
				writeRowNums[0]++;
			});
			map.entrySet().stream().filter(entry -> entry.getValue() < 50).forEach(entry -> UniLog.log1("less 50 code:%s,%d", entry.getKey(), entry.getValue()));
		});
	}
	*/
}