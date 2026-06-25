package com.uniinformation.bicore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellFormula;
import com.kyoko.common.StringUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.UniLog;
/*
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
*/

public class ColumnCell extends Cell {

	BiColumn col;
	BiCellCollection cl;
	BiResult br;
	HashMap<Object, Object> ccObjHM = null;
	Object valueBeforeDirty = null;
	
	public ColumnCell() {
		super();
	}
	public ColumnCell(String strval) {
		super(strval);
		// TODO Auto-generated constructor stub
	}

	public ColumnCell(int i, int mode) {
		// TODO Auto-generated constructor stub
		super(i,mode);
	}

	public ColumnCell(Date p_date, int mode) {
		// TODO Auto-generated constructor stub
		super(p_date,mode);
	}

	public ColumnCell(double d, int mode) {
		// TODO Auto-generated constructor stub
		super(d,mode);
	}

	public ColumnCell(boolean d, int mode) {
		// TODO Auto-generated constructor stub
		super(d,mode);
	}

	public ColumnCell(String s, int mode) {
		// TODO Auto-generated constructor stub
		super(s,mode);
	}
	public void setBiColumn(BiColumn p_col,BiCellCollection p_cl,BiResult p_br) {
		col = p_col;
		cl = p_cl;
		br = p_br;
	}
	public BiColumn getBiColumn() {
		return(col);
	}
	public BiResult getBiResult() {
		return(br);
	}
//	public int getColumnWidth() {
//		if(getType() == Cell.VTYPE_STRING && col != null) {
//			if(col.isFlexWidth()) return(0);
//			return(col.fdlen * JxZkBiBase.pxPerChar);
//		} else {
//			return(JxZkBiBase.pxForDate);
//		}
//	}
	
	public BiCellCollection getCollection() {
		return(cl);
	}
	
//	public boolean isLookup() {
//		return(col.isLookup());
//	}
//	
//	public String getColumnType(){
//		return(col.getColumnType());
//	}
	
	public void setCCObj(Object p_objKey,  Object p_objValue){
		if (ccObjHM == null){
			ccObjHM = new HashMap<Object, Object>();
		}
		ccObjHM.put(p_objKey, p_objValue);
	}
	public Object getCCObj(Object p_objKey){
		if (ccObjHM == null){
			return(null);
		}
		return(ccObjHM.get(p_objKey));
	}
	public Object removeCCObj(Object p_objKey){
		if (ccObjHM == null){
			return(null);
		}
		return(ccObjHM.remove(p_objKey));
	}
	/***
	 * type=radio + int field => return index instead of string value
	 * otherwise return string value
	 * @return
	 */
	public int getAlignment() {
		return(col.getAlignment());
	}
	public String getColumnDisplayClass() {
		return(br.getColumnDisplayClass(this));
	}
	
	@Override
	public String getColumnDisplayString() {
		if(br == null)
		return(mygetColumnDisplayString());
		else
		return(br.getColumnDisplayString(this));
	}

	/* remarked and change back to use biresult.getColumnDisplayString 
	 * its change to use ColumnCell's own getDisplayString on 2021/01/18, don't know why
	 */
	public String mygetColumnDisplayString() {
//		getRadioIndexByString("Service");
//		getRadioIndexByString("Check");
//		getRadioIndexByString("Install");
		String str = "";
		if (requireRadioIndex(this)){
			List il = getItemList();
			if(il != null) {
				int iidx = getInt();
				if(iidx >= 0 && iidx < il.size()) {
					str = il.get(iidx).toString();
				}
			}
		}
		else
			str = getString();
		/*
		//andrew220804 mode the code to ZkBiTranslateHelper
		if (getBiResult().getSessionHelper().getAllowOptionTranslate() && getBiResult().getSessionHelper().getAllowTranslate() && StringUtils.equals(getBiColumn().getColumnType(), "radio")) {
			String key = getBiResult().getView().getName() + "." + getCellLabel() + "." + str;
			str = ZkBiTranslateHelper.getText(getBiResult().getSessionHelper(), key, "OPTION", str);
		}
		*/
		
		//andrew220804 br probably null, this block of code should be not effective at all
		if (br != null && col != null && StringUtils.equals(col.getColumnType(), "radio")) {
			str =  TranslateUtil.getTextByCell(br.getSessionHelper(), this, this.getString());
		}
		
		return(str);
	}	
	/***
	 * type=radio + int field => return radio button index
	 * otherwise return -1
	 * @return
	 */
//	public int getRadioIndexByString(String p_displayString){
//		if (requireRadioIndex(this)){
//			List itemList = getItemList();
//			if (itemList == null){
//				UniLog.logm(this, "itemList is null");
//				return(-1);
//			}
//			for (int i=0; i<itemList.size();i++){
//				if (StringUtils.equalsIgnoreCase(itemList.get(i).toString(), p_displayString)){
//					UniLog.logm(this, "found %s(%s) idx:%d", p_displayString, getString(), i);
//					return(i);
//				}
//			}
//			UniLog.logm(this, "not found %s(%s)", p_displayString, getString());
//		}
//		//UniLog.logm(this, "ignore non radio button %s(%s)", p_displayString, getString());
//		return(-1);
//	}
	public static boolean requireRadioIndex(ColumnCell p_cell){
		//check if radio and db is integer field
		if (StringUtils.equals(p_cell.getBiColumn().getColumnType(), "radio") && p_cell.getType() == Cell.VTYPE_INT){ 
			return(true);
		}
		else{
			return(false);
		}
	}
	
	@Override
	public String getCellLabel() {
		return(col.getLabel());
	}

	@Override
	public void setFormula(CellFormula p_formula,boolean p_eval) throws CellException
	{
		if(p_formula != null && ! (p_formula instanceof ColumnCellFormula)) throw new CellException("Should Use setFormula(ColumnCellFormula ...)");
		super.setFormula(p_formula,false);
		if(p_formula != null) {
			Vector v = p_formula.getFunctions();
			if(v.size() > 0) {
				cl.initFormula(this,p_formula);
			}
			if(p_eval) eval();
		}
	}
	
	@Override
	protected void cellUpdated() {
		super.cellUpdated();
		if(cl != null) cl.setDirty(true);
		if(col == null  || !col.allowUndo()) return;
		if(!isDirty()) {
			if(setDirty()) {
				try {
					notifyModeChange();
				} catch(CellException cex) {
					UniLog.log(cex);
				}
			}
		}
	}
	
	@Override
	public void clearDirty() {
		super.clearDirty();
		valueBeforeDirty = getObject();
	}

	public void restoreValueBeforeDirty() {
		if(valueBeforeDirty == null || !isDirty()) return;
		try {
			set(valueBeforeDirty);
			super.clearDirty();
			notifyModeChange();
		} catch(CellException cex) {
			UniLog.log(cex);
		}
	}
}
