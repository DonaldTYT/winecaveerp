package com.uniinformation.utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.uniinformation.cell.AbstractGetItemProperty;

public class TrGetItemProperty extends AbstractGetItemProperty {
	
	List<String> fieldList;
	List<String> headerList;
	List<String> widthList;
	TableRec tr;
	public TrGetItemProperty (List<String> p_fieldList,List <String> p_headerList,List <String>  p_widthList) {
		super();
		fieldList = p_fieldList;
		headerList = p_headerList;
		widthList = p_widthList;
	}
	public TrGetItemProperty (List<String> p_fieldList,List <String> p_headerList) {
		super();
		fieldList = p_fieldList;
		headerList = p_headerList;
		widthList = null;
	}
	public TrGetItemProperty (List<String> p_fieldList) {
		super();
		fieldList = p_fieldList;
		headerList = null;
		widthList = null;
	}
	public void setTableRec(TableRec p_tr) {
		tr = p_tr;
	}
	public TableRec getTableRec() {
		return(tr);
	}

	@Override
	public int getColumnCount(Object item) {
		// TODO Auto-generated method stub
		return (fieldList.size());
	}

	@Override
	public String getString(Object item) {
		// TODO Auto-generated method stub
		/*String retStr = "";
		Object rec[] = (Object []) item;
		for(String fd : fieldList) {
			int idx = tr.getFieldIndex(fd);
			if(idx >= 0) retStr += rec[idx].toString() + " ";
		}
		return (retStr);*/
		Object rec[] = (Object []) item;
		return fieldList.stream().map(fd -> {
			int idx = tr.getFieldIndex(fd);
			return idx >= 0 ? rec[idx].toString() : null;
		}).filter(Objects::nonNull).collect(Collectors.joining(" "));
	}

	@Override
	public Object getRow(int p_row) {
		// TODO Auto-generated method stub
		return(tr.getRecord(p_row));
	}

	@Override
	public int getRowCount() {
		return tr != null ? tr.getRecordCount() : 0;
	}

	@Override
	public int getIndexOf(Object item) {
		return tr != null ? tr.getAllData().indexOf(item) : -1;
	}
	public Object getColumnValue(Object p_v,int p_col) {
		int idx = tr.getFieldIndex(fieldList.get(p_col));
		return(((Object[]) p_v)[idx]);
	}
	
	public Object getHeader(Object p_v,int p_col) {
		if(headerList != null)
			return(headerList.get(p_col));
		else
			return(fieldList.get(p_col));
	}
	
	@Override
	public String getColumnWidth(Object p_v,int p_col) {
		int minLen = 0;
		if(tr != null) {
			if(widthList != null) return(widthList.get(p_col));
			if(headerList != null) minLen = headerList.get(p_col).length(); else minLen = 10;
			try {
				int fType = tr.getFieldType(fieldList.get(p_col));
				switch(fType) {
		         case java.sql.Types.SMALLINT:
		         case java.sql.Types.INTEGER :
		         case java.sql.Types.TINYINT :
		         case java.sql.Types.BIT :
		        	 return( ""+(10 * BiUtil.pxPerChar) + "px");
		         case java.sql.Types.REAL :
		         case java.sql.Types.DOUBLE :
		         case java.sql.Types.DECIMAL :
		         case java.sql.Types.NUMERIC :
		         case java.sql.Types.FLOAT :	 
		        	 return( ""+ (minLen * BiUtil.pxPerChar) + "px");
		         case java.sql.Types.DATE :
		        	 return( ""+(minLen * BiUtil.pxPerChar) + "px");
		         case java.sql.Types.CHAR :
		        	 int len = tr.getColumnDisplaySize(fieldList.get(p_col));
		        	 if( len < minLen ) len = minLen ; 
		        	 len *= BiUtil.pxPerChar;
		        	 if(len > BiUtil.pxMax) len = BiUtil.pxMax;
		        	 return( ""+len+"px");
		        default :
		        	 return( ""+(minLen * BiUtil.pxPerChar) + "px");
				}
			} catch (Exception ex ) {
				UniLog.log(ex);
				return(null);
			}
		}
		return(null);
	}
	@Override
	public int getRowWidth() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static class SelectorPick extends TrGetItemProperty {

		public SelectorPick(List<String> p_fieldList) {
			super(p_fieldList);
		}

		@Override
		public Object getRow(int p_row) {
			return fieldList.stream().map(BiUtil.throwFunction(l -> tr.getFieldString(l, p_row))).collect(Collectors.joining(" "));
		}

		@Override
		public String getString(Object item) {
			return item.toString();
		}

		@Override
		public int getIndexOf(Object item) {
			return tr != null ? IntStream.range(0, tr.getRecordCount()).filter(BiUtil.throwIntPredicate(i -> Objects.equals(item, getRow(i)))).findFirst().orElse(-1) : -1;
		}
	}
}
