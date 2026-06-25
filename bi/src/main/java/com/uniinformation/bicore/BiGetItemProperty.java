package com.uniinformation.bicore;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiGetItemProperty extends AbstractGetItemProperty {
	
	public static final int GETITEM_MODE_INPUT = 0;
	public static final int GETITEM_MODE_LIST  = 1;
	public static final int GETITEM_MODE_PICK  = 2;
	protected BiResult bigibr;
	protected int getItemMode = 0;
//	boolean pickListMode = false;
	
	/***
	 * @param p_v - row item. should be null as row data is not important
	 * @return
	 */
	protected Vector<BiColumn> getListColumns(Object p_v) {
		
		if(getItemMode != GETITEM_MODE_PICK) return(bigibr.getListColumns()); else return(bigibr.getPickColumns());
	}
	
	public BiGetItemProperty(BiResult p_br) {
		bigibr = p_br;
//		pickListMode = false;
	}
	public void setItemMode(int p_mode) {
		getItemMode = p_mode;
	}
	/*
	public BiGetItemProperty(BiResult p_br,boolean p_pickListMode) {
		bigibr = p_br;
		pickListMode = p_pickListMode;
	}
	*/
	@Override
	public int getColumnCount(Object p_v) {
		return(getListColumns(p_v).size());
	}
	@Override
	public int getColumnSpan(Object p_v,int p_col) {
		return(1);
	}
	@Override
	public Object getColumnValueByName(Object p_v,String p_name) {
		Object o = bigibr.getTrStatObj(p_v);
		CellCollection col = bigibr.getRowCollectionO(o);
		if(getItemMode != GETITEM_MODE_INPUT) {
			
			ColumnCell cc = (ColumnCell) col.testCell(p_name);
			if(cc.getBiColumn().getColumnType().equals("button")) {
				return(col.testCell(p_name));
			}
			String strClass = cc.getColumnDisplayClass();
			String strValue = cc.getColumnDisplayString(); 
			if(strClass == null) 
				return(strValue);
			else 
				return(new StringWithClass(strValue,strClass));
		} else {
			return(col.testCell(p_name));
		}
	}
	@Override
	public Object getColumnValue(Object p_v,int p_col) {
		Object o = getListColumns(p_v).get(p_col);
		if(o instanceof BiColumn) return(getColumnValueByName(p_v,((BiColumn) o).getLabel()));
		return(o);
		/*
		Vector <BiColumn> v = getListColumns(p_v);
		if(v.get(p_col) == null) return(null);
		return(getColumnValueByName(p_v,v.get(p_col).getLabel()));
		*/
	}
//	@Override
//	public String getColumnLabel(Object p_v,int p_col) {
//		ColumnCell c = (ColumnCell) getColumnValue(p_v,p_col);
//		if(c != null) return(c.getString());
//		else return("");
//	}
	@Override
	public String getString(Object p_v) {
		String str = "";
		for(int i=0;i<getColumnCount(p_v);i++) {
			if(getItemMode != GETITEM_MODE_INPUT) {
				str += getColumnValue(p_v,i);
			} else {
				ColumnCell c = (ColumnCell) getColumnValue(p_v,i);
				if(c != null) str += c.getString() + " ";
			}
//			str += getColumnLabel(p_v,i);
		}
		return(str);
	}		
	@Override
	public int getStatus(Object p_v) {
		Object o = bigibr.getTrStatObj(p_v);
		int flag = 0;
		if(bigibr.isMarkedDelete(o)){
			flag += GIPI_DELETED;
		}
//		if(bigibr.isMarkedInsert(o)){
//			flag += GIPI_INSERTED;
//		}
		if (flag == 0){
			flag += GIPI_NORMAL;
		}
		return(flag);
	}
	
	@Override
	public Object getHeader(Object p_v,int p_col) {
		if(p_col < 0) {
			if(bigibr != null) return(bigibr.getView().getHeader());
			return("");
		}
		Object o = getListColumns(p_v).get(p_col);
		/*
		if(o instanceof BiColumn) return(((BiColumn) o).getEngName()); else return("");
		*/
		
		//andrew190508: how to obtain showcellname parameter here?
		if(o instanceof BiColumn){
			/*
			BiColumn col = (BiColumn) o;
			StringBuilder sb = new StringBuilder();
			sb.append(col.getEngName()); 
			if (bigibr.sh != null && bigibr.sh.getShowCellName()){
				sb.append("(");
				sb.append(col.getView().getName());
				sb.append(".");
				sb.append(col.getCellLabel());
				sb.append(")");
			}
			return(sb.toString());
			*/
			//return(bigibr.sh.getLabel((BiColumn) o));
			BiColumn col = (BiColumn) o;
			String label = col.getEngName();
			boolean allowUpdateTranslate = false;
			if (bigibr.sh != null){
				label = bigibr.sh.getLabel(col);
				allowUpdateTranslate = bigibr.sh.getAllowUpdateTranslate();
			}
			return(MapUtil.of(
					"label",label,
					"cellFullName", col.getCellFullName(), //this opt will be obsolated
					"biColumn", col,
					"biResult", bigibr,
					"allowUpdateTranslate", allowUpdateTranslate
					));
			
		} else{
			return("");
		}
	}
	@Override
	public int getRowCount() {
		return(bigibr.getRowCount());
	}
	@Override
	public Object getRow(int p_idx) {
		return(bigibr.getTrStatObj(p_idx));
	}
	
	@Override
	public int getIndexOf(Object p_v) {
		return(bigibr.getResultStat().indexOf(p_v));
	}
	
	@Override
	public void onValueChanged(Object p_value,int p_ctype) {
		
	}
	
	@Override
	public String getColumnWidth(Object p_v ,int p_col){
		//return(ZkUtil.calColumnWidth(bigibr.getListColumns().get(p_col),10));
//		return(ZkUtil.calColumnWidth(getListColumns(p_v).get(p_col),0));
		Object o = getListColumns(p_v).get(p_col);
		if(o instanceof BiColumn)  {
			//special handle for small button column
			if (StringUtils.equalsAny(((BiColumn) o).getColumnType().trim(), "button")) {
				return(BiUtil.calColumnWidth((BiColumn) o,10,40,0,null)); 
			}
			else {
				return(BiUtil.calColumnWidth((BiColumn) o,0,0,0,null));
			}
		}  else {
			return("100%");
		}
	}
	public BiResult getBiResult(){
		return(bigibr);
	}
	
	public CellCollection getCellCollectionByValue(Object p_value) {
		CellCollection col = bigibr.getRowCollectionO(p_value);
		return(col);
	}

	@Override
	public int getRowWidth() {
		// TODO Auto-generated method stub
		Vector<BiColumn> v = getListColumns(null);
		int px = 0;
		for(BiColumn bc : v) {
			px+= BiUtil.calColumnPx(bc, 0, 100, 400);
		}
		return px;
	}

//	@Override
//	public Object getListTitle() {
//		// TODO Auto-generated method stub
//		if(bigibr.getParent() != null) {
//		BiView parentView = bigibr.getParent().getView();
//		if(parentView.linkShowHeader(bigibr.getView())) {
//			return bigibr.getView().getHeader();
//		}
//		}
//		return(null);
//	}
	
	@Override 
	public int getColumnAlignment(int p_colidx) {

		Object o = getListColumns(null).get(p_colidx);
		if(o instanceof BiColumn)  {
    		return(((BiColumn) o).getAlignment());
		} else {
			if(getItemMode == GETITEM_MODE_INPUT) return(1);
			return(0);
		}
	}
	
//	@Override
//	public String getLinkedUrl(Object p_v,int p_col) { 
//		if(!bigibr.getSessionHelper().getAllowVisitView()) return(null);
//		Object o = getListColumns(null).get(p_col);
//		if(o instanceof BiColumn)  {
//			BiColumn bc = (BiColumn) o;
//			o = bigibr.getTrStatObj(p_v);
//			CellCollection col = bigibr.getRowCollectionO(o);
//			ColumnCell cc = (ColumnCell) col.testCell(bc.getLabel());
//   			String visitUrl = null;
//   			if(bigibr.getLinkedView(bc.getLabel()) != null) visitUrl = SessionHelper.getUrlByViewid(bigibr.getSessionHelper(), bigibr.getLinkedView(bc.getLabel()));
//   			if(visitUrl != null) {
//	   			try {
//	   				JSONObject jo = new JSONObject();
//	   				String cond = bigibr.getLinkedColumn(bc.getLabel()) + " = '" + bigibr.getCellString(bc.getLabel()) + "'";
//	   				jo.put("customCondition", cond);
//	   				String key = bigibr.getSessionHelper().putOneTimeData( jo);
//	   				visitUrl += "&querycondition="+key;
//	   				final String url = visitUrl;
//					return(url);
//	   			} catch (Exception ex) {
//	   				UniLog.log(ex);
//	   			}
//   			}
//			
//		}
//		return (null);
//	};

	@Override
	public String getLinkedUrl(Object p_v,int p_col) { 
		if(!bigibr.getSessionHelper().getAllowVisitView()) return(null);
		Object o = getListColumns(null).get(p_col);
		if(o instanceof BiColumn)  {
			BiColumn bc = (BiColumn) o;
//			o = bigibr.getTrStatObj(p_v);
//			CellCollection col = bigibr.getRowCollectionO(o);
//			ColumnCell cc = (ColumnCell) col.testCell(bc.getLabel());
   			String visitUrl = null;
   			Object oo = bigibr.getTrStatObj(p_v);
   			CellCollection col = bigibr.getRowCollectionO(oo);
   			return(bigibr.getLinkedUrl(bc.getLabel(),col));

			
		}
		return (null);
	};
	@Override
	public JSONObject getLinkedCondition(Object p_v,int p_col) { 
		if(!bigibr.getSessionHelper().getAllowVisitView()) return(null);
		Object o = getListColumns(null).get(p_col);
		if(o instanceof BiColumn)  {
			BiColumn bc = (BiColumn) o;
			o = bigibr.getTrStatObj(p_v);
			CellCollection col = bigibr.getRowCollectionO(o);
			ColumnCell cc = (ColumnCell) col.testCell(bc.getLabel());
			return(bigibr.getLinkedCondition(bc.getLabel(),cc));
		}
		return (null);
	};
}
