package com.uniinformation.cell;

import java.util.List;

import org.json.JSONObject;

public abstract class AbstractGetItemProperty {
	static public int GIPI_NORMAL  = 1;
	static public int GIPI_DELETED = 2;
	static public int GIPI_INSERTED= 4;
	static public final int GIPI_PULLDOWN_OPENED = 1;
	static public final int GIPI_PULLDOWN_CLOSED = 2;
	static public final int GIPI_VALUE_CHANGED = 3;
	static public final int GIPI_CELL_MAPPED = 4;
	static public final int GIPI_VALUE_ONOK = 5;
	/***
	 * @param item - row data item 
	 * @return
	 */
	public abstract int getColumnCount(Object item);
//	public abstract int getColumnSpan(Object item,int p_col);
	public int getColumnSpan(Object item,int p_col) {return(1);};
//	public abstract Object getColumnValue(Object item,int p_col);
	public Object getColumnValue(Object item,int p_col){return(null);};
//	public abstract Object getColumnValueByName(Object item,String p_name);
	public Object getColumnValueByName(Object item,String p_name) {return(null);};
//	public abstract String getColumnLabel(Object item,int p_col);
	public abstract String getString(Object item);
	public abstract int getRowWidth();
	
	/***
	 * obtain item status (e.g. NORMAL/DELETED/INSERTED)
	 * @param item
	 * @return status flag matrix for bitwise operation
	 */
	public int getStatus(Object item){
		return(GIPI_NORMAL);
	}
	public boolean getStatus(Object item, int p_flag){
		if ((getStatus(item) & p_flag) == p_flag){
			return(true);
		}
		else{
			return(false);
		}
	}
//	public abstract String getHeader(Object item,int p_col);
	/***
	 * @param item - always null
	 * @param p_col - col idx
	 * @return
	 */
	public Object getHeader(Object item,int p_col){return(null);};
	public abstract Object getRow(int p_row);
	public abstract int getRowCount();
	public abstract int getIndexOf(Object item);
//	public abstract void onValueChanged(Object value,int p_ctype);
	public void onValueChanged(Object value,int p_ctype){};
	public String getColumnWidth(Object item,int p_col){
		return(null);
	}
	public Object onBeforeValueChange(Object item,Object value){return(null);};
//	public Object getListTitle() { return (null);};
	
	public void setColumnCellList(int p_idx,List<String> p_ccList)  { }
	public int getColumnAlignment(int p_colidx) { return(0);};
	public String getLinkedUrl(Object p_v,int p_idx) { return (null);};
	public JSONObject getLinkedCondition(Object p_v,int p_idx) { return (null);};
	public Object getColumnNativeObject(Object p_data,Object p_obj) { return (null);};
	/***
	 * row level allow delete attribute
	 * can override by child
	 * @param item
	 * @return
	 */
	public boolean getAllowDelete(Object item) {
		return true; //allow delete by default
	}
}
