package com.kyoko.tables;

import java.util.ArrayList;

public abstract class CellTables {
	/* assumed row->column hierarchy */
	ArrayList<Integer> lastRowNumAtCol=null;
	ArrayList<Integer> lastColNumAtRow=null;
	public CellTables() {
		lastRowNumAtCol = new ArrayList<Integer>();
		lastColNumAtRow = new ArrayList<Integer>();
	}
	public void fillCell(int rowIdx, int colIdx) {
		
	}
	public void addCell(int rowIdx,int colIdx,int rowSpan,int colSpan) throws Exception {
		if(rowIdx < 0 && colIdx < 0) {
			throw new Exception("Cell row and column position cannot be both zero");
		}
		if(rowIdx >= 0) {
			throw new Exception("Add cell at absolute row position current not supported");
		}
		for(int i=lastRowNumAtCol.size();i<colIdx+colSpan;i++) {
			lastRowNumAtCol.add(0);
			createOneColumn();
		}
		rowIdx = lastRowNumAtCol.get(colIdx)+1;
		for(int i=lastColNumAtRow.size();i<rowIdx+rowSpan;i++) {
			lastColNumAtRow.add(0);
			createOneRow();
		}
	}
	abstract Object createOneCell(int rowIdx,int colIdx,int rowSpan,int colSpan);
	abstract Object createOneRow();
	abstract Object createOneColumn();
}
