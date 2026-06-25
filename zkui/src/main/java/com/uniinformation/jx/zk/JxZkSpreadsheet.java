package com.uniinformation.jx.zk;

import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;
import com.uniinformation.cell.*;
import com.uniinformation.cell.Cell;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.EditableCellStyle;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.CellAreaEvent;
import org.zkoss.zss.ui.event.CellSelectionEvent;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;

public class JxZkSpreadsheet extends JxZkElement implements CellValueMapper {
	static final int MAX_ROW = 1000000;
	HashMap<Integer,String> colTitles;
	HashMap<Integer,String> rowTitles;
	JxGridChangeListener gridChangeListener = null;
	int maxMismatchCount = 0;
	class GridCell {
		int row,col;
		Cell c;
		GridCell(int r,int c)  {
			row = r;
			col = c;
		}
	}
	int vCols;
	int vRows;
	Hashtable <com.uniinformation.cell.Cell,GridCell> cellMap;
	Vector <Cell[]> gridCellRows;
	public JxZkSpreadsheet(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		UniLog.log("JxZkSpreadsheet initialized");

		Spreadsheet ss = (Spreadsheet) c;
		ss.setLeftheadwidth(100);
		/*
		ss.setMaxVisibleColumns(5);
		ss.setMaxVisibleRows(3);
		ss.setColumntitles("Title,ISBN,Page,Quantity,Unit Price");
//		Book book = ss.getBook();
//		Sheet sheet = book.getSheetAt(0);
		Range r = Ranges.range(ss.getSelectedSheet(),0,0);
		r.setCellValue("HAHA");
		*/
//		Range r = Ranges.range(ss.getSelectedSheet(),0,3);
		
		/*
		Range r = Ranges.range(ss.getSelectedSheet(),
						Ranges.getColumnRefString(3)
					);
		CellOperationUtil.applyDataFormat(r, "d-mmm-yy");
		r = Ranges.range(ss.getSelectedSheet(),0,2);
		CellOperationUtil.applyDataFormat(r, "0.00");
		*/
		
		comp.addEventListener(org.zkoss.zss.ui.event.Events.ON_AFTER_CELL_CHANGE, zkEventListener);
		colTitles = new HashMap<Integer,String> ();
		rowTitles = new HashMap<Integer,String> ();
		
		/*
        Ranges.range(ss.getSelectedSheet()).protectSheet("123456",
                true, true, false, false, false, false, false,
                false, false, false, false, false, false, false, false);
                	*/
		/*
        Ranges.range(ss.getSelectedSheet()).protectSheet("123456",
                true, true, true, true, true, true, true,
                true, true, true, true, true, true, true, true);
                	*/
	}

	protected String processAction(Event ev) {
		JxField fd;
		super.processAction(ev);
		switch(JxZkGadgetProvider.getEventID(ev.getName())) {
		case JxZkGadgetProvider.EV_ONCTRLKEY:
				KeyEvent kev = (KeyEvent) ev;
				UniLog.log("Hotkey "+kev.getKeyCode());
				switch(kev.getKeyCode()) {
				case 86:// Ctrl V
					UniLog.log("do paste");
//					ev.stopPropagation();
					break;
				}
				break;
		case JxZkGadgetProvider.EV_ONCELLCHANGE:
				Spreadsheet ss = (Spreadsheet) comp;
//				CellSelectionEvent cev = (CellSelectionEvent) ev;
//				UniLog.log("Zk Spreadsheed cell change "+cev.getTop()+","+cev.getBottom()+","+cev.getLeft()+","+cev.getRight()+":"+cev.getType());
				CellAreaEvent cev = (CellAreaEvent) ev;
				UniLog.log("Zk Spreadsheed cell change "+cev.getLastRow()+","+cev.getRow()+","+cev.getLastColumn()+","+cev.getColumn());
				if(cev.getLastRow() > MAX_ROW) break;
				for ( int i = cev.getRow(); i <= cev.getLastRow();i++){
					if(i >= vRows) break;
					for(int j = cev.getColumn();j <= cev.getLastColumn();j++) {
					if(j >= vCols) break;

				Range r = Ranges.range(ss.getSelectedSheet(),i,j);
				if(gridCellRows != null && gridCellRows.size() > i) {
					Cell[] cl = gridCellRows.get(i);
					if(cl.length > j) {
						UniLog.log("sync gridCellValue mode " + cl[j].getMode() + ":" + r.getCellValue());
						if(cl[j].getMode() == Cell.VMODE_DISPONLY) {
							UniLog.log("Change in protected cell, should skip");
							if(!value_is_equal(cl[j].getObject(),r.getCellValue())) {
								if(maxMismatchCount < 10) {
									update_spreadsheet_range(r,cl[j].getObject());
									maxMismatchCount ++;
								} else {
									UniLog.log("Max mismatch count > 100, skip resync");
								}
							} else {
								maxMismatchCount = 0;
							}
							continue;
						} 
						try {
							if(cl[j].getType() == com.uniinformation.cell.Cell.VTYPE_DATE) {
								Object o = r.getCellValue();
								java.util.Date jd = null;
								if(o == null ) {
									cl[j].sync(com.kyoko.common.DateUtil.zeroDate);
								} else if(o instanceof Double) {
									double d = ((Double) o).doubleValue();
									cl[j].sync(excelToJDate(d));
									/*
									if(d < 0.1) {
										cl[j].sync(com.uniinformation.utils.DateUtil.zeroDate);
									} else {
										long l = (long) d;
										if(l > 60) l -= 25569; else l -= 25568;
										l *= 86400000;
										jd = new java.util.Date(l);
										cl[j].sync(jd);	
									}
									*/
								} else {
									if(o instanceof Double) {
										UniLog.log("sync gridCellValue Date double other ");
										cl[j].sync(r.getCellValue());
									} else {
										update_spreadsheet_range(r,cl[j].getObject());
									}
								}
							} else {
								cl[j].sync(r.getCellValue());
							}
						} catch (Exception ex ) {
							UniLog.log(ex);
						}
					}
				}
				if(gridChangeListener != null) {
					gridChangeListener.valueChanged(getJxField(), j, i,null);
					
				}
						
					}
				}
				break;
		}
		return(null);
	}	
	
	public void grid_setCol(int n)
	{
		Spreadsheet ss = (Spreadsheet) comp;
		ss.setMaxVisibleColumns(n);
		vCols = n;
	}	
	public void grid_setRow(int n)
	{
		Spreadsheet ss = (Spreadsheet) comp;
		ss.setMaxVisibleRows(n > 0 ? n : 1);
		vRows = n;
	}
	@Override
	public boolean grid_setcolheader(int idx,Object p_obj)
	{
		String s = "";
		if (p_obj instanceof String){
			s = (String)p_obj;
		}
		else if (p_obj instanceof Map){
			s = MapUtil.getString(p_obj, "label", "");
		}
		
		colTitles.put(new Integer(idx), (String)s);
		UniLog.log("HAHA 2017 set grid col " + idx + " title " + s);
		{
				String s2 = colTitles.get(new Integer(idx));
				UniLog.log("HAHA 2017 set grid col " + idx + " title " + s + " from " + s2);
		}
		Spreadsheet ss = (Spreadsheet) comp;
		ss.setColumntitles( (HashMap)  null);
		ss.setColumntitles(colTitles);
		return(true);
	}
	public boolean grid_setrowheader(int idx,String s)
	{
		rowTitles.put(new Integer(idx), s);
		Spreadsheet ss = (Spreadsheet) comp;
		ss.setRowtitles(rowTitles);
		return(true);
	}
	
	void update_spreadsheet_range(Range r,Object o) {
		if(o instanceof java.util.Date) {
			java.util.Date jd = (java.util.Date) o;
			if(jd.before(com.kyoko.common.DateUtil.minDate)) {
				r.clearContents();
			} else {
				r.setCellValue(o);
			}
		} else {
			r.setCellValue(o);
		}	
	}
	
	public boolean grid_setValue(int col, int row, Object value)
	{
		Object o;
		Spreadsheet ss = (Spreadsheet) comp;
		Range r = Ranges.range(ss.getSelectedSheet(),row,col);
		if(value instanceof com.uniinformation.cell.Cell) {
			Cell c = (com.uniinformation.cell.Cell) value;
			if(cellMap == null) {
					cellMap = new Hashtable <com.uniinformation.cell.Cell,GridCell>();
					gridCellRows = new Vector<Cell[]>();
			}
			GridCell gc = cellMap.get(c);
			if(gc != null) {
				if(	gc.row != row || gc.col != col) {
					cellMap.remove(c);
					gridCellRows.get(row)[col] = null;
					for(int i = gridCellRows.size(); i < row;i++) {
						gridCellRows.add(new Cell[vCols]);
					}
					gridCellRows.get(row)[col] = c;
					cellMap.put(c,new GridCell(row,col));
				}
			} else {
				for(int i = gridCellRows.size(); i <= row;i++) {
					gridCellRows.add(new Cell[vCols]);
				}
				gridCellRows.get(row)[col] = c;
				cellMap.put(c,new GridCell(row,col));
				c.map(this);
			}
			o = c.getObject();
		} else {
			if(gridCellRows != null && gridCellRows.size() > row) {
				Cell c = gridCellRows.get(row)[col];
				if(c != null )  {
					cellMap.remove(c);
					gridCellRows.get(row)[col] = null;
					c.map(null);
				}
			}
			o = value;
		}
		/*
		if(o instanceof java.util.Date) {
			java.util.Date jd = (java.util.Date) o;
			if(jd.before(com.uniinformation.utils.DateUtil.minDate)) {
				r.clearContents();
			} else {
				r.setCellValue(o);
			}
		} else {
			r.setCellValue(o);
		}
		*/
		update_spreadsheet_range(r,o);
		return(true);
	}

	public Object grid_getValue(int col, int row)
	{
		Spreadsheet ss = (Spreadsheet) comp;
		Range r = Ranges.range(ss.getSelectedSheet(),row,col);
		return( r.getCellValue() );
	}
	
	private Range getRange(int p_col,int p_row)
	{
		Spreadsheet ss = (Spreadsheet) comp;
		Range r;
		if(p_col >= 0 && p_row >= 0) {
			r = Ranges.range(ss.getSelectedSheet(),p_row,p_col);
		} else {
			if(p_col >= 0 ) {
				r = Ranges.range(ss.getSelectedSheet(),Ranges.getColumnRefString(p_col));
			} else {
				if(p_row >= 0 ) {
					r = Ranges.range(ss.getSelectedSheet(),Ranges.getRowRefString(p_row));
				} else {
					return(null);
				}
			}
			
		}
		return(r);
	}
	public void grid_setDataFormat(int p_col,int p_row,String p_format) {
		Spreadsheet ss = (Spreadsheet) comp;
		Range r = getRange(p_col,p_row);
		if(r != null) {
				if(p_format.equals("noUpdateOn")) {
				    CellStyle oldStyle = r.getCellStyle();
				    EditableCellStyle newStyle = r.getCellStyleHelper().createCellStyle(oldStyle);
				    newStyle.setLocked(true);
				    r.setCellStyle(newStyle);
					return;
				}
				if(p_format.equals("noUpdateOff")) {
				    CellStyle oldStyle = r.getCellStyle();
				    EditableCellStyle newStyle = r.getCellStyleHelper().createCellStyle(oldStyle);
				    newStyle.setLocked(false);
				    r.setCellStyle(newStyle);
					return;
				}
				if(p_format.equals("unProtect")) {
					Ranges.range(ss.getSelectedSheet()).unprotectSheet("123456");
				}
				if(p_format.equals("protect")) {
					Ranges.range(ss.getSelectedSheet()).protectSheet("123456",
							true, true, true, true, true, true, true,
							true, true, true, true, true, true, true, true);
					return;
				}
				CellOperationUtil.applyDataFormat(r, p_format);
		}
	}
	public void addGridChangeListener(JxGridChangeListener x)
	{
		gridChangeListener = x;
	}
	
	public boolean grid_setcolwidth(int idx,int width)
	{
		Range r = getRange(idx,-1);
		r.setColumnWidth(width);
		
		return(false);
	}
	public void cellMap_bind(Cell p_cell)
	{
	}
	public void cellMap_valchange(Cell c)
	{
			UniLog.log("HAHA JxZkSpreadsheet cell value changed");
	}
	public void cellMap_hintchange(Cell c)
	{
	}
	public void cellMap_listchange(Cell c)
	{
	}
	public void cellMap_modechange(Cell c)
	{
	}
	public void cellMap_formatchange(Cell c)
	{
	}	
	public void cellMap_formulachange(Cell c)
	{
	}	
	boolean value_is_equal(Object cell_o, Object sp_o) {
		if(sp_o == null) {
			if(cell_o instanceof java.util.Date) {
				if(((java.util.Date) cell_o).before(com.kyoko.common.DateUtil.minDate)) return(true);
				return(false);
			}
			if(cell_o instanceof Integer) {
				if(((Integer) cell_o).intValue() == 0) return(true);
				return(false);
			}
			if(cell_o instanceof Double) {
				if(((Integer) cell_o).doubleValue() == 0.0) return(true);
				return(false);
			}
			if(cell_o instanceof String) {
				if(!((String) cell_o).trim().equals("")) return(false);
			}
			return(true);
		}
		UniLog.log("Check sp cell class = " + sp_o.getClass().getName());
		if(sp_o instanceof Double) {
			if(cell_o instanceof java.util.Date) {
				java.util.Date d1 = (java.util.Date) cell_o;
				java.util.Date d2 = excelToJDate(((Double) sp_o).doubleValue());
				java.util.Calendar c1 = java.util.Calendar.getInstance();
				java.util.Calendar c2 = java.util.Calendar.getInstance();
				c1.setTime(d1);
				c2.setTime(d2);
				if(c1.get(java.util.Calendar.YEAR) != c2.get(java.util.Calendar.YEAR)) return(false);
				if(c1.get(java.util.Calendar.MONTH) != c2.get(java.util.Calendar.MONTH)) return(false);
				if(c1.get(java.util.Calendar.DAY_OF_MONTH) != c2.get(java.util.Calendar.DAY_OF_MONTH)) return(false);
				return(true);
			}
			if(cell_o instanceof Integer) {
				int n = (int) ((Double) sp_o).doubleValue();
				if(((Integer) cell_o).intValue() == n) return(true);
				return(false);
			}
			if(cell_o instanceof Double) {
				
			}
		}
		if(sp_o instanceof String) {
			UniLog.log("Comparing string");
			if(cell_o instanceof java.util.Date) return(false);
			if(cell_o instanceof String) {
				String s1 = (String) cell_o;
				String s2 = (String) sp_o;
				UniLog.log("Comparing two string ["+s1+"]["+s2+"]");
				if(!s1.trim().equals(s2.trim())) return(false);
			}
		}
		return(true);
	}
	
	java.util.Date excelToJDate(double d) {
		if(d < 0.1) {
			return(com.kyoko.common.DateUtil.zeroDate);
		} else {
			long l = (long) d;
			if(l > 60) l -= 25569; else l -= 25568;
			l *= 86400000;
			return(new java.util.Date(l));
		}
	}
	
	@Override
	public Object getValue() {
		return(null);
	}
}
