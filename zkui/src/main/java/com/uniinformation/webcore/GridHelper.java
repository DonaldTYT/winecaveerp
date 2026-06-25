package com.uniinformation.webcore;

import java.util.ArrayList;

import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

public class GridHelper extends Grid {
	ArrayList<Column> columnList = new ArrayList();
	Columns columns = null;
	Rows rows = null;
	public GridHelper() {
		this(0);
	}
	public GridHelper(int colCnt) {
		super();
		columns = new Columns();
		rows = new Rows();
		this.appendChild(columns);
		this.appendChild(rows);
		for (int i=0; i<colCnt; i++) {
			Column column = new Column();
			columnList.add(column);
			columns.appendChild(column);
		}
	}
	public Column getColumn(int p_idx) throws IndexOutOfBoundsException {
		return columnList.get(p_idx);
	}
	public Rows getRows() {
		return rows;
	}
	public Row getRow(int p_idx) {
		return (Row) rows.getChildren().get(p_idx);
	}
	public Row getLastRow() {
		return (Row) rows.getLastChild();
	}
	public GridHelper addRow(HtmlBasedComponent...comps) {
		Row row = new Row();
    	rows.appendChild(row);
	    for (int i=0; i<comps.length; i+=1) {
	    	row.appendChild(comps[i]);
	    }
		return this;
	}
	public void setLabels(String...ss) {
		for (int i = 0; i < ss.length; i++)
			getColumn(i).setLabel(ss[i]);
	}
	public void setHflexs(String hflex) {
		for (Column col : columnList)
			col.setHflex(hflex);
	}
	public void setHflexs(String...hflexs) {
		for (int i = 0; i < hflexs.length; i++)
			getColumn(i).setHflex(hflexs[i]);
	}
	public static void main(String args[]){
	}
}