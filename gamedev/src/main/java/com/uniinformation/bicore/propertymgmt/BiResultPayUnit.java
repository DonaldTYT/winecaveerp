package com.uniinformation.bicore.propertymgmt;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPayUnit  extends BiResultPropertyMgmt {
	
	CellValueAction blockChanged = null;
	CellValueAction floorChanged = null;
	public BiResultPayUnit(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	void copyItemList(CellCollection p_col,String p_from,String p_to) {
		Cell fromCell = p_col.getCell(p_from);
		Cell toCell = p_col.getCell(p_to);
		if(fromCell != null && toCell != null) {
			toCell.setItemList(fromCell.getItemList());
		}
		
	}
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		/*
		Cell parentBlock = p_col.getCell("super.col_i");
		Cell thisBlock = p_col.getCell("pu_block");
		if(parentBlock != null && thisBlock != null) {
			thisBlock.setItemList(parentBlock.getItemList());
		}
		*/
		if(blockChanged == null) {
			blockChanged = new CellValueAction() {

				@Override
				public void cellAction_onchange(Cell p_value) throws CellException {
					// TODO Auto-generated method stub
					BiCellCollection col = ((ColumnCell) p_value).getCollection();
					Cell pu_floor = col.getCell("pu_floor");
					try {
						TableRec tr = getSelectUtil().getQueryResult("select distinct col_d from property where col_b = ? and col_c = ?",
								new Wherecl().appendArgument(col.getCellString("super.col_c")).appendArgument(col.getCellString("pu_block"))
							);
						Vector<String> itemList = new Vector<String>();
						for(int i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							itemList.add(tr.getFieldString("col_d"));
						}
						pu_floor.setItemList(itemList);
						pu_floor.set("");
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}

				@Override
				public void cellAction_onfree() throws CellException {
					// TODO Auto-generated method stub
					
				}
				
			};
		}
		if(floorChanged == null) {
			floorChanged = new CellValueAction() {

				@Override
				public void cellAction_onchange(Cell p_value) throws CellException {
					// TODO Auto-generated method stub
					BiCellCollection col = ((ColumnCell) p_value).getCollection();
					Cell pu_flat = col.getCell("pu_flat");
					try {
						TableRec tr = getSelectUtil().getQueryResult("select distinct col_e from property where col_b = ? and col_c = ? and col_d = ?",
								new Wherecl().appendArgument(col.getCellString("super.col_c")).appendArgument(col.getCellString("pu_block")).appendArgument(col.getCellString("pu_floor"))
							);
						Vector<String> itemList = new Vector<String>();
						for(int i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							itemList.add(tr.getFieldString("col_e"));
						}
						pu_flat.setItemList(itemList);
						pu_flat.set("");
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}

				@Override
				public void cellAction_onfree() throws CellException {
					// TODO Auto-generated method stub
					
				}
				
			};
		}
		copyItemList(p_col,"super.col_i","pu_block");
		Cell pu_block = p_col.getCell("pu_block");
		pu_block.addAction(blockChanged);
		Cell pu_floor = p_col.getCell("pu_floor");
		pu_floor.addAction(floorChanged);
		try {
			blockChanged.cellAction_onchange(pu_block);
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		/*
		copyItemList(p_col,"super.col_j","pu_floor");
		copyItemList(p_col,"super.col_k","pu_flat");
		*/
	}
}
