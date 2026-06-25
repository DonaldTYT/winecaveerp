package com.uniinformation.bicore;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.uniinformation.cell.CellException;
import com.uniinformation.webcore.SessionHelper;

public class BiWorkSheet implements Sheet {

	@Override
	public Iterator<Row> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addMergedRegion(CellRangeAddress arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addValidationData(DataValidation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void autoSizeColumn(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void autoSizeColumn(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Drawing createDrawingPatriarch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createFreezePane(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createFreezePane(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Row createRow(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createSplitPane(int arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getAutobreaks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Comment getCellComment(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getColumnBreaks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellStyle getColumnStyle(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumnWidth(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DataValidationHelper getDataValidationHelper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDefaultColumnWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getDefaultRowHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getDefaultRowHeightInPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getDisplayGuts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getFirstRowNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getFitToPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Footer getFooter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getForceFormulaRecalculation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Header getHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getHorizontallyCenter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getLastRowNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getLeftCol() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMargin(short arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CellRangeAddress getMergedRegion(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumMergedRegions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PaneInformation getPaneInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPhysicalNumberOfRows() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PrintSetup getPrintSetup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getProtect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CellRangeAddress getRepeatingColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellRangeAddress getRepeatingRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Row getRow(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getRowBreaks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getRowSumsBelow() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getRowSumsRight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getScenarioProtect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SheetConditionalFormatting getSheetConditionalFormatting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSheetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short getTopRow() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getVerticallyCenter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Workbook getWorkbook() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void groupColumn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void groupRow(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isColumnBroken(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isColumnHidden(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayFormulas() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayGridlines() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayRowColHeadings() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisplayZeros() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrintGridlines() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRightToLeft() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRowBroken(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void protectSheet(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CellRange<? extends Cell> removeArrayFormula(Cell arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeColumnBreak(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeMergedRegion(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRow(Row arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRowBreak(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Row> rowIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellRange<? extends Cell> setArrayFormula(String arg0, CellRangeAddress arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoFilter setAutoFilter(CellRangeAddress arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAutobreaks(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnBreak(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnGroupCollapsed(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnHidden(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultColumnStyle(int arg0, CellStyle arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultColumnWidth(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultRowHeight(short arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultRowHeightInPoints(float arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayFormulas(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayGridlines(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayGuts(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayRowColHeadings(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayZeros(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFitToPage(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setForceFormulaRecalculation(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHorizontallyCenter(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMargin(short arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPrintGridlines(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRepeatingColumns(CellRangeAddress arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRepeatingRows(CellRangeAddress arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRightToLeft(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRowBreak(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRowGroupCollapsed(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRowSumsBelow(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRowSumsRight(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelected(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVerticallyCenter(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setZoom(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shiftRows(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shiftRows(int arg0, int arg1, int arg2, boolean arg3, boolean arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInPane(short arg0, short arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ungroupColumn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ungroupRow(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}


	BiResult result;
	boolean inRecal=false;
	public BiWorkSheet(SessionHelper p_sh,String p_viewName,String[] colNames) {
		BiView view = p_sh.getBiSchema().getViewByName(p_viewName);
		result = view.newBiResult(p_sh.getLoginId(), null, null , p_sh);
	}
	public void load(String p_condition) throws Exception {
		result.clear();
		result.addCustomCondition(p_condition);
		result.query();
		result.recal();
	}
}
