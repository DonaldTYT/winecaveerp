package com.uniinformation.erpv4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.StringWithClass;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;

abstract public class Erpv4GetItemProperty extends BiGetItemProperty {
	private static final double QTY_EPSILON = 0.0000001;

	class EffectivePickRec {
		int pickBrIdx;
		Object[] vals;
		double qty;
		BiCellCollection exposedCol;
	}
	BiColumn[] pickValColumns; /* set of columns in pickBr that uniquely identify the record */
	BiColumn pickQtyColumn;	   /* qty in pickBr */
	BiColumn[] listValColumns; /* set of columns in listBr that uniquely identify the record */
	BiColumn listQtyColumn;    /* qty in listBr */
	BiResult listBr;
	ColumnCell pickCell;
	List<EffectivePickRec>effectivePickArr;
	public Erpv4GetItemProperty(BiResult p_listBr,BiResult p_pickBr,ColumnCell p_pickCell,
			BiColumn[] p_pickValColumns,BiColumn p_pickQtyColumn,
			BiColumn[] p_listValColumns,BiColumn p_listQtyColumn
			) {
		super(p_pickBr);
		validateConfiguration(p_listBr, p_pickBr,
				p_pickValColumns, p_pickQtyColumn,
				p_listValColumns, p_listQtyColumn);
		listBr = p_listBr;
		pickCell = p_pickCell;
		pickValColumns = p_pickValColumns;
		pickQtyColumn  = p_pickQtyColumn;
		listValColumns = p_listValColumns;
		listQtyColumn  = p_listQtyColumn;
	}
	private static int getColumnCellType(BiResult p_br, BiColumn p_column) {
		if(p_column == null) throw new IllegalArgumentException("GetItemProperty column cannot be null");
		if(p_br.getCurrentCollection() == null) {
			throw new IllegalArgumentException("GetItemProperty BiResult has no current CellCollection");
		}
		Cell cell = p_br.getCurrentCollection().testCell(p_column.getLabel());
		if(cell == null) {
			throw new IllegalArgumentException("GetItemProperty column not found in BiResult: "
					+ p_column.getLabel());
		}
		return(cell.getType());
	}
	private static void validateConfiguration(BiResult p_listBr, BiResult p_pickBr,
			BiColumn[] p_pickValColumns, BiColumn p_pickQtyColumn,
			BiColumn[] p_listValColumns, BiColumn p_listQtyColumn) {
		if(p_listBr == null || p_pickBr == null) {
			throw new IllegalArgumentException("GetItemProperty listBr and pickBr are required");
		}
		if(p_pickValColumns == null || p_listValColumns == null
				|| p_pickValColumns.length == 0
				|| p_pickValColumns.length != p_listValColumns.length) {
			throw new IllegalArgumentException(
					"GetItemProperty picker and list keys must have the same non-zero column count");
		}
		for(int i = 0; i < p_pickValColumns.length; i++) {
			int pickType = getColumnCellType(p_pickBr, p_pickValColumns[i]);
			int listType = getColumnCellType(p_listBr, p_listValColumns[i]);
			if(pickType != listType) {
				throw new IllegalArgumentException("GetItemProperty key type mismatch: "
						+ p_pickValColumns[i].getLabel() + " and "
						+ p_listValColumns[i].getLabel());
			}
		}
		int pickQtyType = getColumnCellType(p_pickBr, p_pickQtyColumn);
		int listQtyType = getColumnCellType(p_listBr, p_listQtyColumn);
		if(pickQtyType != listQtyType
				|| (pickQtyType != Cell.VTYPE_INT && pickQtyType != Cell.VTYPE_DOUBLE)) {
			throw new IllegalArgumentException("GetItemProperty quantity columns must have the same numeric type: "
					+ p_pickQtyColumn.getLabel() + " and " + p_listQtyColumn.getLabel());
		}
	}
	/*
	 * Return committed quantities keyed by the serial_id of the picker record.
	 * The returned map is read-only to this class.
	 */
	protected abstract HashMap<Integer,Double> getCommitedValues();

	Object valsToHash(Object[] vals) {
		List<Object> key = new ArrayList<Object>(vals.length);
		for(Object val : vals) key.add(val);
		return(key);
	}
	Object[] pickRecToVals(BiCellCollection col) {
		Object[] vals = new Object[pickValColumns.length];
		for(int i = 0; i < pickValColumns.length; i++) {
			vals[i] = col.getCell(pickValColumns[i].getLabel()).getObject();
		}
		return(vals);
	}
	double pickRecGetQty(BiCellCollection col) {
		return(col.getCellDouble(pickQtyColumn.getLabel()));
	}
	Object[] listRecToVals(BiCellCollection col) {
		Object[] vals = new Object[listValColumns.length];
		for(int i = 0; i < listValColumns.length; i++) {
			vals[i] = col.getCell(listValColumns[i].getLabel()).getObject();
		}
		return(vals);
	}
	double listRecGetQty(BiCellCollection col) {
		return(col.getCellDouble(listQtyColumn.getLabel()));
	}
	private BiCellCollection getPickRowCollection(int p_idx) {
		if(bigibr.getParent() != null) {
			return(bigibr.getRowCollectionV(p_idx));
		}
		bigibr.loadOneRecV(p_idx);
		return(bigibr.getCurrentCollection());
	}
	boolean listRecIsDeleted(BiCellCollection p_col) {
		for(int i = 0; i < listBr.getRowCount(); i++) {
			if(listBr.getRowCollectionV(i) == p_col) {
				return(listBr.isMarkedDelete(listBr.getTrStatObj(i)));
			}
		}
		return(false);
	}
	@Override 
	public void refresh() {
		effectivePickArr = new ArrayList<EffectivePickRec>();
		LinkedHashMap<Object,EffectivePickRec> recHash = new LinkedHashMap<Object,EffectivePickRec> ();
		/* first loop, load record pointer from bigibr to recHash */
		for(int i=0;i<bigibr.getRecordCount();i++) {
			BiCellCollection col = getPickRowCollection(i);
			Object[] vals = pickRecToVals(col);
			double qty = pickRecGetQty(col);
			Object key = valsToHash(vals);
			EffectivePickRec rec = recHash.get(key);
			if(rec != null) {
				/* this should not happen , just for flexibility */
				rec.qty += qty;
			} else {
				rec = new EffectivePickRec();
				rec.pickBrIdx = i;
				rec.vals = vals;
				rec.qty = qty;
				recHash.put(key, rec);
			}
		}
		HashMap<Integer,Double> commitedValues = getCommitedValues();
		if(commitedValues != null && !commitedValues.isEmpty()) {
			for(int pickBrIdx = 0; pickBrIdx < bigibr.getRecordCount(); pickBrIdx++) {
				BiCellCollection col = getPickRowCollection(pickBrIdx);
				Double commitedQty = commitedValues.get(Integer.valueOf(col.getSid()));
				if(commitedQty == null) continue;
				Object key = valsToHash(pickRecToVals(col));
				EffectivePickRec rec = recHash.get(key);
				if(rec != null) rec.qty += commitedQty.doubleValue();
			}
		}
		/* Deduct all non-deleted rows in the list from the available quantity. */
		for(int i = 0; i < listBr.getRowCount(); i++) {
			Object rowState = listBr.getTrStatObj(i);
			if(listBr.isMarkedDelete(rowState)) continue;
			BiCellCollection col = listBr.getRowCollectionV(i);
			Object key = valsToHash(listRecToVals(col));
			EffectivePickRec rec = recHash.get(key);
			if(rec != null) rec.qty -= listRecGetQty(col);
		}
		if(pickCell != null) {
			/* we are pick from the row of this pick cell, the qty of this row must add back to the available list
			 * 
			 */
			BiCellCollection col = pickCell.getCollection();
			if(!listRecIsDeleted(col)) {
				Object key = valsToHash(listRecToVals(col));
				EffectivePickRec rec = recHash.get(key);
				if(rec != null) rec.qty += listRecGetQty(col);
			}
		}
		
		/* Ignore insignificant positive residues introduced by double arithmetic. */
		for(EffectivePickRec rec : recHash.values()) {
			if(rec.qty > QTY_EPSILON) effectivePickArr.add(rec);
		}
	}

	private void ensureEffectivePickArr() {
		if(effectivePickArr == null) refresh();
	}

	private EffectivePickRec getEffectivePickRec(Object p_value) {
		ensureEffectivePickArr();
		if(p_value instanceof Erpv4GetItemProperty.EffectivePickRec) {
			return((EffectivePickRec) p_value);
		}
		for(EffectivePickRec rec : effectivePickArr) {
			if(bigibr.getTrStatObj(rec.pickBrIdx) == p_value) return(rec);
		}
		return(null);
	}

	private Object getSourceRow(EffectivePickRec p_rec) {
		return(p_rec == null ? null : bigibr.getTrStatObj(p_rec.pickBrIdx));
	}

	private BiCellCollection getEffectiveCellCollection(EffectivePickRec p_rec) {
		if(p_rec == null) return(null);
		BiCellCollection sourceCol = getPickRowCollection(p_rec.pickBrIdx);
		if(p_rec.exposedCol == null) {
			p_rec.exposedCol = new BiCellCollection(sourceCol, bigibr);
			ColumnCell qtyCell = new ColumnCell(p_rec.qty, Cell.VMODE_NORMAL);
			qtyCell.setBiColumn(pickQtyColumn, p_rec.exposedCol, bigibr);
			p_rec.exposedCol.addCell(pickQtyColumn.getLabel(), qtyCell);
		}
		return(p_rec.exposedCol);
	}

	@Override
	public int getRowCount() {
		ensureEffectivePickArr();
		return(effectivePickArr.size());
	}

	@Override
	public Object getRow(int p_idx) {
		ensureEffectivePickArr();
		return(effectivePickArr.get(p_idx));
	}

	@Override
	public int getIndexOf(Object p_value) {
		ensureEffectivePickArr();
		EffectivePickRec rec = getEffectivePickRec(p_value);
		return(rec == null ? -1 : effectivePickArr.indexOf(rec));
	}

	@Override
	public Object getColumnValueByName(Object p_value, String p_name) {
		BiCellCollection col = getEffectiveCellCollection(getEffectivePickRec(p_value));
		if(col == null) return(null);
		if(getItemMode == GETITEM_MODE_INPUT) return(col.testCell(p_name));

		ColumnCell cc = (ColumnCell) col.testCell(p_name);
		if(cc.getBiColumn().getColumnType().equals("button")) return(cc);
		String strClass = cc.getColumnDisplayClass();
		String strValue = cc.getColumnDisplayString();
		return(strClass == null ? strValue : new StringWithClass(strValue, strClass));
	}

	@Override
	public int getStatus(Object p_value) {
		EffectivePickRec rec = getEffectivePickRec(p_value);
		return(rec == null ? GIPI_NORMAL : super.getStatus(getSourceRow(rec)));
	}

	@Override
	public CellCollection getCellCollectionByValue(Object p_value) {
		return(getEffectiveCellCollection(getEffectivePickRec(p_value)));
	}

	@Override
	public String getLinkedUrl(Object p_value, int p_col) {
		if(!bigibr.getSessionHelper().getAllowVisitView()) return(null);
		Object o = getListColumns(null).get(p_col);
		if(!(o instanceof BiColumn)) return(null);
		BiCellCollection col = getEffectiveCellCollection(getEffectivePickRec(p_value));
		return(col == null ? null : bigibr.getLinkedUrl(((BiColumn) o).getLabel(), col));
	}

	@Override
	public JSONObject getLinkedCondition(Object p_value, int p_col) {
		if(!bigibr.getSessionHelper().getAllowVisitView()) return(null);
		Object o = getListColumns(null).get(p_col);
		if(!(o instanceof BiColumn)) return(null);
		BiCellCollection col = getEffectiveCellCollection(getEffectivePickRec(p_value));
		if(col == null) return(null);
		ColumnCell cc = (ColumnCell) col.testCell(((BiColumn) o).getLabel());
		return(bigibr.getLinkedCondition(((BiColumn) o).getLabel(), cc));
	}
}
