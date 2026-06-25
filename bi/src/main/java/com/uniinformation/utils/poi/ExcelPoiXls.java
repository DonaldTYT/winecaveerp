package com.uniinformation.utils.poi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;

public class ExcelPoiXls extends ExcelPoi {
	HSSFWorkbook workBook = null;
	HSSFSheet workSheet = null;
	Hashtable<String, Short> excel_cellStyle;
	Hashtable<String, Short> excel_font;	
	Hashtable<String ,Short>  excel_indexedColorMap;
	HSSFCellStyle hssf_cellStyle = null;
//	Hashtable excelNames = null;
//	boolean excel_filterNonPrintable = false;
	public static boolean fDebug = false;
	
	public ExcelPoiXls () {
	    workBook = new HSSFWorkbook();
		workSheet = workBook.createSheet();
		excelSheetIdx = 0;
		excelSheetName = workBook.getSheetName(0);
		excelRowCount = 0;
		excelDefaultRowHeight = workSheet.getDefaultRowHeight();
		excelCurrentRowHeight = excelDefaultRowHeight;
		excel_initFormatStyle();	
		excelRowCount = workSheet.getLastRowNum()+1;
		if(excelRowCount < 1) workSheet.createRow(0);
	}
	public ExcelPoiXls (InputStream is) throws IOException {
	    workBook = new HSSFWorkbook(is);
		workSheet = workBook.getSheetAt(0);
		excelSheetIdx = 0;
		excelSheetName = workBook.getSheetName(0);
		excelDefaultRowHeight = workSheet.getDefaultRowHeight();
		excelCurrentRowHeight = excelDefaultRowHeight;
		excel_initFormatStyle();
		excelRowCount = workSheet.getLastRowNum()+1;
		if(excelRowCount < 1) workSheet.createRow(0);
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
		HSSFRow row = null;
		if(workSheet == null) return(null);
		if(p_row > workSheet.getLastRowNum()) return(null);
		row = workSheet.getRow(p_row);
		if(row == null) return(null);
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return(null);
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
											return("Y");
										} else {
											return("N");
										}
		case HSSFCell.CELL_TYPE_STRING: return(cell.getStringCellValue());
		case HSSFCell.CELL_TYPE_NUMERIC: //return("OK  "+DateUtil.toDateString(cell.getDateCellValue(),"yyyy/mm/dd")); return(cell.getNumericCellValue());
										return(new DataFormatter().formatCellValue(cell));
		case HSSFCell.CELL_TYPE_FORMULA: 
						switch(cell.getCachedFormulaResultType()) {
							case HSSFCell.CELL_TYPE_STRING: return(cell.getStringCellValue());
							case HSSFCell.CELL_TYPE_NUMERIC: return(new DataFormatter().formatCellValue(cell));
							case HSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
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
		HSSFRow row = null;
		if(workSheet == null) return(null);
		if(p_row > workSheet.getLastRowNum()) return(null);
		row = workSheet.getRow(p_row);
		if(row == null) return(null);
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return(null);
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_BOOLEAN:return(null);
		case HSSFCell.CELL_TYPE_STRING: 
							if(p_datefmt == null) return(null);
							String ds = cell.getStringCellValue();
							return(DateUtil.getDate(ds, p_datefmt));
		case HSSFCell.CELL_TYPE_NUMERIC: return(cell.getDateCellValue());
		case HSSFCell.CELL_TYPE_FORMULA: 
						switch(cell.getCachedFormulaResultType()) {
							case HSSFCell.CELL_TYPE_STRING: return(null);
							case HSSFCell.CELL_TYPE_NUMERIC: return(cell.getDateCellValue());
							case HSSFCell.CELL_TYPE_BOOLEAN: return(null);
							default : return(null);
						}
		default : return(null);
		}
	}
	
	public Double getDoubleValue(int p_row,int p_col)
	{
		HSSFRow row = null;
		if(workSheet == null) return(null);
		if(p_row > workSheet.getLastRowNum()) return(null);
		row = workSheet.getRow(p_row);
		if(row == null) return(null);
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return(null);
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
											return(new Double (1.0));
										} else {
											return(new Double (0.0));
										}
		case HSSFCell.CELL_TYPE_STRING: try {
										double d = Double.parseDouble(cell.getStringCellValue());
										return(new Double(d));
										} catch (NumberFormatException nex) {
										return(null);
										}
					
		case HSSFCell.CELL_TYPE_NUMERIC: return(cell.getNumericCellValue());
		case HSSFCell.CELL_TYPE_FORMULA: 
						switch(cell.getCachedFormulaResultType()) {
							case HSSFCell.CELL_TYPE_STRING: try {
											double d = Double.parseDouble(cell.getStringCellValue());
											return(new Double(d));
										} catch (NumberFormatException nex) {
											return(null);
										}
							case HSSFCell.CELL_TYPE_NUMERIC: return(cell.getNumericCellValue());
							case HSSFCell.CELL_TYPE_BOOLEAN:if(cell.getBooleanCellValue()) {
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
		HSSFRow row = null;
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
	public static void testPoiHeaderFooter()
	{
		final List<String> columns = new ArrayList();
		final int colCnt = 5;
		for (int i=0; i<colCnt; i++){
			columns.add(String.format("ListHeader:%d",i));
		}
		
		final HSSFWorkbook workbook = new HSSFWorkbook();
	    final HSSFSheet sheet = workbook.createSheet ("sheet");

	    final HSSFHeader header = sheet.getHeader ();
	    header.setLeft (HSSFHeader.fontSize((short) 16) + "XXX Comapny");
	    header.setCenter(HSSFHeader.fontSize((short) 16) +" XXX Report YYMMDD-YYMMDD");

	    final HSSFFooter footer = sheet.getFooter ();
	    footer.setRight ( HeaderFooter.date () + " " + HeaderFooter.time () );
	    footer.setCenter ( "Page "+ HeaderFooter.page () + " / "+ HeaderFooter.numPages () );

	    //make list header
        final Font font = sheet.getWorkbook ().createFont();
        //font.setFontName ("Arial");
        //font.setFontHeightInPoints((short) 20);
        font.setBoldweight (Font.BOLDWEIGHT_BOLD);
        final CellStyle style = sheet.getWorkbook ().createCellStyle ();
        style.setFont ( font );
        final HSSFRow headerRow = sheet.createRow ( 0 );
        for (int i=0; i<columns.size(); i++ )
        {
            final String field = columns.get(i);
            final HSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(field);
            cell.setCellStyle(style);
        }
	    

	    final HSSFPrintSetup printSetup = sheet.getPrintSetup ();
	    printSetup.setLandscape ( false );
	    printSetup.setFitWidth ( (short)1 );
	    printSetup.setFitHeight ( (short)0 );
	    printSetup.setPaperSize ( PrintSetup.A4_PAPERSIZE );

	    //sheet.setAutoFilter ( new CellRangeAddress ( 0, 0, 0, columns.size () - 1 ) );
	    sheet.createFreezePane ( 0, 1 );
	    sheet.setFitToPage ( true );
	    sheet.setAutobreaks ( true );
	    
	    //generate dummy data for testing
	    List<String> dataList = new ArrayList<String>();
	    for (int i=0; i<200; i++){
	    	StringBuilder sb = new StringBuilder();
	    	for (int j=0; j<colCnt; j++){
	    		if (sb.length() == 0){
	    			sb.append(String.format("unicode:\u4F60\u597D\u55CE?"));
	    		}
	    		else{
	    			sb.append(String.format("|data%d",i));
	    		}
	    	}
	    	dataList.add(sb.toString());
	    }
	    for (int i= 0; i < dataList.size(); i++) {
	        Row row = sheet.createRow(i+1);
	        String[] cellValues = dataList.get(i).split("\\|");
	        for (int colIndex = 0; colIndex < cellValues.length; colIndex++) {
	            Cell cell = row.createCell(colIndex);
	            cell.setCellValue(cellValues[colIndex]);
	        }
	    }
	    

	    //set margin
	    printSetup.setFooterMargin ( 0.25 );
	    sheet.setMargin ( Sheet.LeftMargin, 1 );
	    sheet.setMargin ( Sheet.RightMargin, 0.5 );
	    sheet.setMargin ( Sheet.TopMargin, 1 );
	    sheet.setMargin ( Sheet.BottomMargin, 0.5 );
	    
	    for (int i=0; i<colCnt; i++){
	    	//sheet.autoSizeColumn(i);
	    	sheet.setColumnWidth(i, 1000 * (i+6) );
	    }
	    
	    //write to file
	    try{
	    	workbook.write(new FileOutputStream("/tmp/a.xls"));
	    }
	    catch(Exception ex){
	    	ex.printStackTrace();
	    }
	}	
    public static void main(String args[]){
    	testPoiHeaderFooter();
    }

/* Cell Name Methods */
	@Override
	public CellRC getNameRegion(String p_name){
		HSSFName nm = workBook.getName(p_name);
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

	/* set value */
	
	public void excel_setStringValue(int p_row, int p_col, String p_value) throws Exception
	{
			HSSFRow row = null;
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
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell(p_col);
				if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			}
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			cell.setCellValue(s);
	}
	public void excel_setDateValue(int p_row, int p_col, java.util.Date p_javadate) throws Exception
	{
			HSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell(p_col);
				if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			}
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			if(p_javadate != null) {
				cell.setCellValue(p_javadate);
			} else {
//				UniLog.log("HAHA 2017 setDateValue " + p_row + " : " + p_col + " null");
				cell.setCellValue((String) null);
			}	
	}
	public void excel_setNumericValue(int p_row, int p_col, double p_value) throws Exception
	{
			HSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell(p_col);
				if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			}
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell.setCellValue(p_value);
	}
	public void excel_setNumericValue(int p_row, int p_col, int p_value) throws Exception
	{
			HSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell( p_col);
				if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			}
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell.setCellValue(p_value);
	}
	public void excel_setNumericValue(int p_row, int p_col, long p_value) throws Exception
	{
			HSSFRow row = null;
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = workSheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = workSheet.getRow(p_row);
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell( p_col);
				if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			}
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell.setCellValue(p_value);
	}

/* Merge Cells */
	public void excel_MergeCells(int p_r1,int p_c1,int p_r2,int p_c2)
	{
		workSheet.addMergedRegion(new CellRangeAddress(p_r1, p_r2, p_c1, p_c2));
	}
	

/* Cell Style Mathods */
	

	public void excel_setCellStyle(int p_row,int p_col,int p_styleIdx) throws Exception
	{
		HSSFRow row = null;
		for(;excelRowCount <= p_row;excelRowCount++) {
			row = workSheet.createRow(excelRowCount);
			if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
		}
		row = workSheet.getRow(p_row);
		HSSFCell cell = row.getCell(p_col);
		if(cell == null) {
			cell = row.createCell( p_col);
		}
		HSSFCellStyle styl = workBook.getCellStyleAt((short) p_styleIdx);
		cell.setCellStyle(styl);
	}
	
	public int excel_getCellStyleIdx(int p_row,int p_col) throws Exception
	{
		HSSFRow row = null;
		row = workSheet.getRow(p_row);
		HSSFCell cell = row.getCell(p_col);
		HSSFCellStyle styl = cell.getCellStyle();
		return(styl.getIndex());
	}
    
	String makeHSSFCellStyleStringHash(HSSFCellStyle p_style) {
		String s = "";
		String ss = getFormatFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += ss;
		ss = getFillColorFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += "," + ss;
		int fontIdx = p_style.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
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

	void cacheOneCellStyleAndFont(HSSFCellStyle sty) {
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if (fDebug) UniLog.log1("cache CellStyle " + sty.getIndex() + " fontidx " + fontIdx + " key " +  makeHSSFCellStyleStringHash(sty));
		String sh = makeHSSFCellStyleStringHash(sty);
		if(sh != null ) {
			if(excel_cellStyle.get(sh) == null) {
				String ss = getStyleFromFont(ft)+","+getColorFromFont(ft);
				Short fidx = excel_font.get( ss );
				if(fidx == null) {
					UniLog.log("add HSSFFont " +  ss + " idx " + fontIdx );
					excel_font.put(ss, new Short(ft.getIndex()));
				}
				UniLog.log("add CellStyleHash " +  sh + " idx " + sty.getIndex());
				excel_cellStyle.put( sh, sty.getIndex());
			}
		}
		
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
	String getFormatFromStyle(HSSFCellStyle sty) {
		return(sty.getDataFormatString());
	}
	String getFillColorFromStyle(HSSFCellStyle sty) {
		int bgcol = sty.getFillForegroundColor();
		for(String key : excel_indexedColorMap.keySet()) {
			Short ic = excel_indexedColorMap.get(key);
			if(ic.intValue() == bgcol) return(key);
		}
		UniLog.log(" getFillColorFromStyle index " + bgcol + " unmapped");
		return(null);
	}
	String getStyleFromFont(HSSFFont ft) {
		String ss = "";
		if(ft.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) ss += "Bold";
		if(ft.getItalic()) ss+= "Italic";
		if(ss.equals("")) ss = "Normal";
		return(ss);
	}
	String getColorFromFont(HSSFFont ft) {
		if(ft.getColor() == HSSFFont.COLOR_RED) return("red");else return("Black");
	}
	String getLockedFromStyle(HSSFCellStyle sty) {
		if(sty.getLocked()) return("locked") ; else return("unlocked");
	}
	

	private void setCell(HSSFRow p_row, int p_col, Object p_val)
	{
		HSSFCell cell = p_row.createCell(p_col);
		if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
//		cell.setEncoding(HSSFCell.ENCODING_UTF_16);
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
		HSSFDataFormat df = workBook.createDataFormat();
		int dfIdx = df.getFormat(sFormat);
		if(dfIdx < 0) return(-1);
		HSSFCellStyle stl = workBook.createCellStyle();
		stl.setDataFormat((short) dfIdx);
		Short colIdx = excel_indexedColorMap.get(sFillColor);
		if(colIdx != null) {
			stl.setFillForegroundColor((short) colIdx.intValue());
			if(!sFillColor.equals("AUTOMATIC")) stl.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		}
		Short fidx = excel_font.get(sFontStyle+","+sFontColor);
		if(fidx == null) {
			HSSFFont ft = workBook.createFont();
			if(sFontColor.equals("red")) ft.setColor(HSSFFont.COLOR_RED); else ft.setColor(HSSFFont.COLOR_NORMAL);
			if(sFontStyle.contains("Bold")) ft.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); else ft.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			if(sFontStyle.contains("Italic")) ft.setItalic(true); else ft.setItalic(false);
			stl.setFont(ft);
			excel_font.put(sFontStyle+","+sFontColor,ft.getIndex());
		} else {
			stl.setFont( workBook.getFontAt((short) fidx.intValue()));
		}
		stl.setLocked(sLocked.equals("locked"));
		
//		stl.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
//		stl.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		stl.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		
//		excel_cellStyle.put(p_format,new Short(stl.getIndex()));
		cacheOneCellStyleAndFont(stl); 
		return(stl.getIndex());
		
	}
	public int excel_getFormatStyle(String p_format)
	{
		return(excel_getStyleGen(p_format,null,null,null,null));
	}
/*
	public String excel_getFormatFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		String s = getFormatFromStyle(sty);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getFillColorFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		String s = getFillColorFromStyle(sty);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getFontStyleFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		String s = getStyleFromFont(ft);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getFontColorFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		String s = getColorFromFont(ft);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getLockedFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		String s = getLockedFromStyle(sty);
		if(s != null) return("OK  "+s); else return("FAIL");
	}

	public String excel_CloseWorkBook()
	{
		workBook = null;
		workSheet = null;
		hssf_cellStyle = null;
		excelRowCount = 0;
		excelSheetIdx = -1;
		excelSheetName = null;
		excelNames = null;
		return("OK");
	}

	public String excel_InsertOneRow(Vector v)
	{
			if(workSheet == null) return("FAIL WorkBook Not Opened");
			HSSFRow row = workSheet.createRow(excelRowCount++);
			if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			for(int i = 0;i<v.size();i++) {
				setCell(row, (short) i , v.get(i));
			}
			return("OK");
	}
	*/
	public String excel_setValues(int p_row,int p_col,Vector v)
	{
			HSSFRow row = null;
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
	public String excel_setValuesWithStyle(int p_row,int p_col,Vector v)
	{
			HSSFRow row = null;
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
				if(hssf_cellStyle == null || 
						hssf_cellStyle.getIndex() != styleidx) {
						hssf_cellStyle = workBook.getCellStyleAt((short) styleidx);
				}
				i++;
				setCell(row, (short) (i / 2 + p_col), v.get(i));
			}
			return("OK");
	}
	/*
	public String excel_WriteWorkBook(String p_outfile)
	{
		try {
			FileOutputStream os = new FileOutputStream(p_outfile);
			workBook.write(os);
			os.close();
			return("OK");
		} catch (Exception ex) {
			return("FAIL");
		}
	}
	public void excel_WriteWorkBook(OutputStream os) throws Exception
	{
		workBook.write(os);
	}
	public String excel_newSheet(String p_sheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
   		HSSFSheet newsheet = workBook.createSheet(p_sheetname);
         return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_renameSheet(String p_oldsheetname, String p_newsheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int idx = workBook.getSheetIndex(p_oldsheetname);
			if(idx < 0) {
				return("FAILSheet " + p_oldsheetname + " not found");
			}
//			workBook.setSheetName(idx, p_newsheetname,HSSFCell.ENCODING_UTF_16);
			workBook.setSheetName(idx, p_newsheetname);
			if(idx == excelSheetIdx) excelSheetName = p_newsheetname;
         return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_removeSheet(String p_sheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int idx = workBook.getSheetIndex(p_sheetname);
			if(idx < 0) {
				return("FAILSheet " + p_sheetname + " not found");
			}
			workBook.removeSheetAt(idx);
			if(idx <= excelSheetIdx) {
				workSheet = null;
				excelSheetName = null;
				excelSheetIdx = -1;
			}
         return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getSheet(String p_sheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int idx = workBook.getSheetIndex(p_sheetname);
			if(idx < 0) {
				return("FAILSheet " + p_sheetname + " not found");
			}
			workSheet = workBook.getSheetAt(idx);
//      	excelRowCount = workSheet.getPhysicalNumberOfRows();
      	excelRowCount = workSheet.getLastRowNum()+1;
			if(excelRowCount < 1) workSheet.createRow(0);
			excelSheetIdx = idx;
			excelSheetName = p_sheetname;
			UniLog.log("excel_getSheet " + p_sheetname+ " " + workSheet.getLastRowNum() + " R " + workSheet.getPhysicalNumberOfRows() + " H " + excelDefaultRowHeight);
			return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}

	public String excel_setColumnWidth(int p_col,int p_width)
	{
			if(workSheet == null) return("FAIL WorkSheet Not Opened");
			workSheet.setColumnWidth((short) p_col,(short) p_width);
			return("OK");
	}

	public String excel_isWriteProtected()
	{
			if(workBook == null) return("FAIL WorkBook Not Opened");
			boolean b = workBook.isWriteProtected();
			if(b ) return("OK  1"); else return("OK  0");
	}

	public String excel_protectWorkBook(boolean p_protected,String p_password,String p_username)
	{
			if(workBook == null) return("FAIL WorkBook Not Opened");
			if(p_protected) {
				workBook.writeProtectWorkbook(p_password,p_username);
			} else {
				workBook.unwriteProtectWorkbook();
			}
			return("OK");
	}

	public String excel_setProtected(boolean p_protected,String p_password)
	{
			UniLog.log("excel_setprotection " + p_protected);
			if(workSheet == null) return("FAIL WorkSheet Not Opened");
			if(p_protected)
				workSheet.protectSheet(p_password);
			else
				workSheet.protectSheet(null);
			return("OK");
	}
	public String excel_setLock(boolean p_locked)
	{
		UniLog.log("excel_setLock " + p_locked);
		if(hssf_cellStyle == null) return("FAIL CellStyle Not Selected");
		if(p_locked) {
			UniLog.log("set excel style to locked");
			hssf_cellStyle.setLocked(true);
		} else {
			UniLog.log("set excel style to unlocked");
			hssf_cellStyle.setLocked(false);
		}
		return("OK");
	}
	public String excel_setDefaultRowHeight(int p_height)
	{
		if(p_height <= 0) 
			excelCurrentRowHeight = excelDefaultRowHeight;
		else
			excelCurrentRowHeight = p_height;
		return("OK");
	}
	public String excel_setCellColor(int p_color)
	{
		UniLog.log("excel_setCellColor " + p_color);
		if(hssf_cellStyle == null) return("FAIL CellStyle Not Selected");
//		hssf_cellStyle.setFillBackgroundColor((short) p_color);
		return("OK");
	}

	public String excel_createCellStyle()
	{
		if(workBook == null) return("FAIL WorkBook Not Opened");
		hssf_cellStyle = workBook.createCellStyle();
		if(hssf_cellStyle == null) return("FAIL Fail to Create CellStyle");
		return("OK  " + hssf_cellStyle.getIndex());
	}
	public String excel_setCellStyle(int p_idx)
	{
		if(workBook == null) return("FAIL WorkBook Not Opened");
		if(p_idx < 0) {
			hssf_cellStyle = null;
			return("OK");
		}
		if(p_idx >= workBook.getNumCellStyles()) return("FAIL CellStyle Not Created");
		hssf_cellStyle = workBook.getCellStyleAt((short) p_idx);
		return("OK");
	}

	public String excel_getCurrentSheetName()
	{
		if(workSheet == null) return("FAIL WorkSheet Not Opened");
		return("OK  " + excelSheetName);
	}
	public String excel_getCurrentSheetIdx()
	{
		if(workSheet == null) return("FAIL WorkSheet Not Opened");
		return("OK  " + excelSheetIdx);
	}

	public String excel_getDefaultRowHeight()
	{
		if(workSheet == null) return("FAIL WorkSheet Not Opened");
		return("OK  " + excelDefaultRowHeight);
	}
	public String excel_setRowHeight(int p_row,int p_height)
	{
		HSSFRow row = null;
		if(workSheet == null) return("FAIL WorkSheet Not Opened");
		for(;excelRowCount <= p_row;excelRowCount++) {
			row = workSheet.createRow(excelRowCount);
			if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
		}
		if(p_height != excelCurrentRowHeight) {
			row.setHeight((short) p_height);
		}
		return("OK");
	}
	public String excel_getStringValue(int p_row,int p_col)
	{
		HSSFRow row = null;
		if(workSheet == null) return("FAIL WorkSheet Not Opened");
		if(p_row > workSheet.getLastRowNum()) return("OK  ");
//		if(p_row >= workSheet.getPhysicalNumberOfRows()) return("OK  ");
		row = workSheet.getRow(p_row);
		if(row == null) return("OK  ");
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return("OK  ");
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING: {
				if(excel_filterNonPrintable) {
						return("OK  "+stripNonPrintable(cell.getStringCellValue()));
				} else {
						return("OK  "+cell.getStringCellValue());
				}
			}
		case HSSFCell.CELL_TYPE_FORMULA:
		case HSSFCell.CELL_TYPE_NUMERIC: return("OK  "+cell.getNumericCellValue());
		case HSSFCell.CELL_TYPE_BOOLEAN: if(cell.getBooleanCellValue()) return("OK  Y"); else return("OK  N");
		default : return("OK  ");
		}
	}
	public String excel_getStringValueAuto2B(int p_row,int p_col)
	{
		return(excel_getStringValueAuto2B(p_row,p_col,null));
	}
	public String excel_getStringValueAuto2B(int p_row,int p_col,String format)
	{
		HSSFRow row = null;
		if(workSheet == null) return("FAIL WorkSheet Not Opened");
		if(p_row > workSheet.getLastRowNum()) return("OK  ");
//		if(p_row >= workSheet.getPhysicalNumberOfRows()) return("OK  ");
		row = workSheet.getRow(p_row);
		if(row == null) return("OK  ");
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return("OK  ");
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING: {
					if(excel_filterNonPrintable) {
						return("OK  "+ ChineseConvert.convertAuto2B(stripNonPrintable(cell.getStringCellValue())));
					} else {
						return("OK  "+ ChineseConvert.convertAuto2B(cell.getStringCellValue()));
					}

				}
		case HSSFCell.CELL_TYPE_FORMULA:
		case HSSFCell.CELL_TYPE_NUMERIC: {
						UniLog.log("getStringValueAuto " + format);
						if(format != null) {
							double d = cell.getNumericCellValue();
							FieldPosition fpos = new FieldPosition(NumberFormat.INTEGER_FIELD);
							DecimalFormat df = new DecimalFormat(format);
							StringBuffer sb = new StringBuffer();
							df.format(d,sb,fpos);
							UniLog.log("getStringValueAuto " + d + " with format got " + sb);
							return("OK  "+ sb.toString());
						} else return("OK  "+cell.getNumericCellValue());
					}
		case HSSFCell.CELL_TYPE_BOOLEAN: if(cell.getBooleanCellValue()) return("OK  Y"); else return("OK  N");
		default : return("OK  ");
		}
	}
	public String excel_getDateValue(int p_row,int p_col)
	{
		HSSFRow row = null;
		if(workSheet == null) return("FAIL WorkSheet Not Opened");
		if(p_row > workSheet.getLastRowNum()) return("OK  ");
//		if(p_row >= workSheet.getPhysicalNumberOfRows()) return("OK  ");
		row = workSheet.getRow(p_row);
		if(row == null) return("OK  ");
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return("OK  ");
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING: return("OK  "+cell.getStringCellValue());
		case HSSFCell.CELL_TYPE_FORMULA:
		case HSSFCell.CELL_TYPE_NUMERIC: return("OK  "+DateUtil.toDateString(cell.getDateCellValue(),"yyyy/mm/dd"));
		default : return("OK  ");
		}
	}
	public String excel_getRowCount()
	{
		return("OK  " + excelRowCount);
	}

	public String excel_getSheetCount()
	{
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int nSheet = workBook.getNumberOfSheets();
         return("OK  "+nSheet);
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getSheetName(int p_index)
	{
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			String sheetName = workBook.getSheetName(p_index);
         return("OK  "+sheetName);
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getSheetByIdx(int p_index) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int nSheet = workBook.getNumberOfSheets();
			if(p_index >= nSheet) {
         	return("FAILWorksheet Index Out of Range");
			}
			workSheet = workBook.getSheetAt(p_index);
//      	excelRowCount = workSheet.getPhysicalNumberOfRows();
      	excelRowCount = workSheet.getLastRowNum()+1;
			if(excelRowCount < 1) workSheet.createRow(0);
			excelSheetIdx = p_index;
			excelSheetName = workBook.getSheetName(p_index);
			UniLog.log("excel_getSheetByIdx " + p_index + " " + workSheet.getLastRowNum() + " R " + workSheet.getPhysicalNumberOfRows() + " H " + excelDefaultRowHeight);
			return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getNameFormula(String p_name)
	{
		if(workBook == null) return("FAILWorkBook is null");
		if(excelNames == null) {
			excelNames = new Hashtable();
			int namecnt = workBook.getNumberOfNames();
			UniLog.log("Extract Excel Names "+namecnt);
			for(int i=0;i<namecnt;i++) {
				HSSFName nm = workBook.getNameAt(i);
				String ref = nm.getRefersToFormula();
				excelNames.put(nm.getNameName(),ref);
				UniLog.log("Excel Names "+i+" "+nm.getNameName()+ " " + ref);
			}
		}
		String ref = (String) excelNames.get(p_name);
		if(ref == null) {
			return("FAILName Not Found");
		} else {
			return("OK  "+ref);
		}
	}

	public String excel_clearRow(int p_row) 
	{
		if(workSheet != null && p_row >= 0 && p_row < excelRowCount) {
			HSSFRow row = workSheet.getRow(p_row);
			if(row != null) workSheet.removeRow(row);
			excelRowCount = workSheet.getLastRowNum()+1;
			return("OK");
		}
		return("FAIL");
	}


	public String excel_cloneSheet(int p_sheetIdx,String p_name) 
	{
		if(workBook != null) {
			HSSFSheet newSheet = workBook.cloneSheet(p_sheetIdx);
			if(newSheet != null) {
				String s = newSheet.getSheetName();
				int idx = workBook.getSheetIndex(s);
				workBook.setSheetName(idx, p_name);
				return("OK  "+idx);
			}
		}
		return("FAIL");
	}

	public String excel_removeSheet(int p_sheetIdx) 
	{
		if(workBook != null) {
			workBook.removeSheetAt(p_sheetIdx);
		}
		return("OK");
	}



	public String stripNonPrintable(String p_s)
	{
		StringBuffer sb = new StringBuffer();
		char[] ca = p_s.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if(ca[i] < 32) continue;
			if(Character.isWhitespace(ca[i])) sb.append(' '); else sb.append(ca[i]);
		}
		return(sb.toString());
	}
	public String excel_setFilterNonPrintable(boolean p_boolean)
	{
		excel_filterNonPrintable = p_boolean;
		return("OK");
	}
	
	*/
	public String excel_autoResizeColumn(int p_col)
	{
		if(workBook == null) return("FAIL");
		if(workSheet == null) return("FAIL");
		workSheet.autoSizeColumn(p_col);
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
			hssf_cellStyle = null;
			return("OK");
		}
		HSSFCellStyle thisCellStyle = null;
		if(p_idx >= workBook.getNumCellStyles()) return("FAIL CellStyle Not Created");
		if(p_idx >= 0) thisCellStyle = workBook.getCellStyleAt((short) p_idx);
		int cWidth = workSheet.getColumnWidth(p_col);
		workSheet.setDefaultColumnStyle(p_col, thisCellStyle); // seem poi 3.9 has a bug that for worksheet idx > 0, the columwidth is incorrect after setDefaultColumnStyle, get and set again to bypass this problem
		workSheet.setColumnWidth(p_col,cWidth);
		return("OK");
	}
	
	 private HSSFDataValidation setupSheetValidation(HSSFSheet p_sheet, int i,
	        DataValidationConstraint validationConstraint) {
	        CellRangeAddressList addressList = new CellRangeAddressList();
	        HSSFDataValidation dataValidation = new HSSFDataValidation(
	                addressList, validationConstraint);
	        addressList.addCellRangeAddress(1, i, 10000, i);
	        dataValidation.setEmptyCellAllowed(true);
	        dataValidation.setShowPromptBox(true);
	        p_sheet.addValidationData(dataValidation);
	        return dataValidation;
	    }
	
	public String excel_setColumnValidation(int p_col,Vector p_validList) {
		if(workBook == null) return("FAIL");
		if(workSheet == null) return("FAIL");
		HSSFDataValidationHelper validationHelper = new HSSFDataValidationHelper(workSheet);
		String [] vList = new String[p_validList.size()];
		for(int i=0;i<vList.length;i++) {
//			vList[i] = (String) p_validList.get(i);
			vList[i] = p_validList.get(i).toString();
		}
		DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(vList);
		setupSheetValidation(workSheet, p_col, validationConstraint );
		return("OK");
	}
    
	public String excel_setColumnValidation(int p_col,String validateStr) {
		if(workBook == null) return("FAIL");
		if(workSheet == null) return("FAIL");
		HSSFDataValidationHelper validationHelper = new HSSFDataValidationHelper(workSheet);
		DataValidationConstraint validationConstraint = validationHelper.createFormulaListConstraint(validateStr);
		setupSheetValidation(workSheet, p_col, validationConstraint );
		return("OK");
	}
	
	public String excel_setCellStyle(int p_idx)
	{
		if(workBook == null) return("FAIL WorkBook Not Opened");
		if(p_idx < 0) {
			hssf_cellStyle = null;
			return("OK");
		}
		if(p_idx >= workBook.getNumCellStyles()) return("FAIL CellStyle Not Created");
		hssf_cellStyle = workBook.getCellStyleAt((short) p_idx);
		return("OK");
	}
	
	public int excel_getColumnCount(int p_row) {
		HSSFRow row = null;
		if(workSheet == null) return(0);
		if(p_row > workSheet.getLastRowNum()) return(0);
		row = workSheet.getRow(p_row);
		if(row == null) return(0);
		return(row.getLastCellNum());
	}

	public boolean excel_getZeroHeight(int p_row) {
		HSSFRow row = null;
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

	public int excel_newSheet(String p_sheetname) {
		if(workBook == null) return(-1);
   		HSSFSheet newsheet = workBook.createSheet(p_sheetname);
   		if(newsheet == null) return(-1);
   		return(workBook.getSheetIndex(p_sheetname));
	}

	public int excel_getSheetIndex(String p_sheetname) {
		if(workBook == null)
			return(-1);
		HSSFSheet newsheet = workBook.createSheet(p_sheetname);
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
		HSSFSheet sheet = workBook.getSheetAt(idx);
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
			HSSFSheet newSheet = workBook.cloneSheet(p_sheetIdx);
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
	}
}
