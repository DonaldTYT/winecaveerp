package com.uniinformation.jx.zk;

import org.zkoss.zk.ui.Component;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Vector;

import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.impl.InputElement;

import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.utils.UniLog;

public class ZkJxCellValueMapper implements CellValueMapper {
	
	Component ic;
	Cell bindedCell;
	public ZkJxCellValueMapper(Component comp) {
		ic = comp;
	}
	public void cellMap_bind(Cell p_cell)
	{
		if(p_cell != null && bindedCell != null) bindedCell.map(null);
		bindedCell = p_cell;
		if(bindedCell != null) {
			cellMap_hintchange(bindedCell);
			cellMap_modechange(bindedCell);
			cellMap_formatchange(bindedCell);
			cellMap_formulachange(bindedCell);
			cellMap_listchange(bindedCell);
			cellMap_valchange(bindedCell);
		}
		
	}
	public void cellMap_valchange(Cell c)
	{
		if(bindedCell != null) {
//			UniLog.log("cellMap_valchange " + jxValue.getString());
			if(!(ic instanceof InputElement))  {
				if(ic instanceof Checkbox) {
					switch(bindedCell.getType()) {
					case Cell.VTYPE_BOOLEAN :
						((Checkbox) ic).setChecked(bindedCell.getBoolean());
						break;
					case Cell.VTYPE_INT    :
						((Checkbox) ic).setChecked(bindedCell.getInt() > 0);
						break;
					case Cell.VTYPE_DOUBLE :
						((Checkbox) ic).setChecked(bindedCell.getDouble() >= 1.0);
						break;
					case Cell.VTYPE_STRING :
						((Checkbox) ic).setChecked(bindedCell.getString().equals("Y"));
						break;
					}
				}
				if(ic instanceof Label) {
					if(bindedCell instanceof ColumnCell) {
						String sclass = ((ColumnCell) bindedCell).getColumnDisplayClass();
						if(sclass != null) {
							if(sclass != null) ((Label) ic).setSclass(sclass);
						}
					}
					((Label) ic).setValue(c.toString());
				}
				if(ic instanceof Radiogroup) {
						int idx =bindedCell.getInt();
						int itemCnt = ((Radiogroup) ic).getItemCount();
						if(itemCnt > idx) {
							((Radiogroup) ic).setSelectedIndex(bindedCell.getInt());
						}
				}
				if(ic instanceof Listbox) {
					if(ic instanceof ZkJxTimePickerList ) {
//						ic.setText(DateUtil.dateToTimeStr(d));
						((ZkJxTimePickerList) ic).setSelectedItemByTime(bindedCell.getDate());
					} else {
						Vector il = bindedCell.getItemList();
						if(il != null) {
							int idx = il.indexOf(bindedCell.getObject());
							if(idx >= 0) {
								((Listbox) ic).setSelectedIndex(idx); 
							} else {
								((Listbox) ic).setSelectedIndex(-1); 
							}
						} else {
							if(bindedCell.isBlank())
								((Listbox) ic).setSelectedIndex(-1); 
							else {
								Listbox cb = (Listbox) ic;
								Listitem ci = cb.appendItem(bindedCell.getString(),bindedCell.getString());
								ci.setValue(bindedCell.getString());
								cb.setSelectedItem(ci);
								/*
								int n = cb.getItemCount();
								for(int i = n-1;i>=0;i--) {
									cb.removeItemAt(i);
								}
								*/
								
							}
						}
					}
				}
				return;
			}
			if(ic instanceof Combobox) {
				if(c.getItemPropertyInterface() != null) {
					int idx = c.getItemPropertyInterface().getIndexOf(c.getObject());
					if(idx >= 0) {
						((Combobox)ic).setSelectedIndex(idx);
					} else {
						((Combobox)ic).setSelectedIndex(-1);
					}
					return;
				}
			}
			InputElement tb = (InputElement) ic;
			switch(bindedCell.getType()) {
			case Cell.VTYPE_INT    :
					tb.setText(""+ bindedCell.getInt());
					break;
			case Cell.VTYPE_DOUBLE :
					tb.setText(""+ bindedCell.getDouble());
					break;
			case Cell.VTYPE_STRING :
					tb.setText(""+ bindedCell.getString());
					if ("".equals(bindedCell.getString()) && tb instanceof Combobox){ //when clear combobox, require to set selectedIndex to avoid caching problem
						((Combobox) tb).setSelectedIndex(-1);
					}
					break;
			case Cell.VTYPE_DATETIME: {
				java.util.Date d = bindedCell.getDate();
				if(d == null || d.before(com.kyoko.common.DateUtil.minDate)) {
					tb.setText("");
				} else {
					if(tb instanceof Datebox) {
						((Datebox) tb).setValue(d);
					} else if(tb instanceof Timebox) {
						((Timebox) tb).setValue(d);
					} else {
						tb.setText(DateUtil.dateToDateTimeStr(d));
					}
				}
				break;		
				}
			case Cell.VTYPE_DATE: {
					java.util.Date d = bindedCell.getDate();
					if(d == null || d.before(com.kyoko.common.DateUtil.minDate)) {
						tb.setText("");
					} else {
						if(tb instanceof Datebox) {
							((Datebox) tb).setValue(d);
						} else {
							tb.setText(DateUtil.toDateString(d,"yyyy/mm/dd"));
						}
					}
					break;
				}
			case Cell.VTYPE_BOOLEAN:	
					if(bindedCell.getBoolean()) {
						tb.setText("Y");
					} else {
						tb.setText("N");
					}
					break;
			}
		}
	}
	public void cellMap_hintchange(Cell c)
	{
	}
	public void cellMap_listchange(Cell c)
	{
		if(c.getItemList() == null && c.getItemPropertyInterface() == null) return;
		if(ic instanceof Combobox && !(ic instanceof ZkJxTimePicker)) {
			Combobox cb = (Combobox) ic;
			int n = cb.getItemCount();
			for(int i = n-1;i>=0;i--) {
				cb.removeItemAt(i);
			}
			if(c.getItemPropertyInterface() != null) {
				n = c.getItemPropertyInterface().getRowCount();
				for(int i=0;i<n;i++) {
					Object o = c.getItemPropertyInterface().getRow(i);
					Comboitem ci = cb.appendItem(c.getItemPropertyInterface().getString(o));
					ci.setValue(0);
				}
			} else if(c.getItemList() != null) {
				Vector itemlist = c.getItemList();
				if(itemlist == null) return;
				for(int i = 0;i<itemlist.size();i++) {
					Comboitem ci = cb.appendItem(itemlist.get(i).toString());
					ci.setValue(itemlist.get(i));
				}
			}
		}
		if(ic instanceof Radiogroup) {
			Radiogroup cb = (Radiogroup) ic;
			int n = cb.getItemCount();
			for(int i = n-1;i>=0;i--) {
				cb.removeItemAt(i);
			}
			if(c.getItemPropertyInterface() != null) {
				n = c.getItemPropertyInterface().getRowCount();
				for(int i=0;i<n;i++) {
					Object o = c.getItemPropertyInterface().getRow(i);
					Radio rd =  cb.appendItem(c.getItemPropertyInterface().getString(o) ,o.toString());
					if(c.getMode() == Cell.VMODE_DISPONLY) rd.setDisabled(true);
				}
			} else if(c.getItemList() != null) {
				Vector itemlist = c.getItemList();
				if(itemlist == null) return;
				for(int i = 0;i<itemlist.size();i++) {
					Radio rd =  cb.appendItem(itemlist.get(i).toString() ,null);
					if(c.getMode() == Cell.VMODE_DISPONLY) rd.setDisabled(true);
					rd.setValue(itemlist.get(i));
				}
			}
		}
		if(ic instanceof Listbox && !(ic instanceof ZkJxTimePickerList)) {
			Listbox cb = (Listbox) ic;
			int n = cb.getItemCount();
			for(int i = n-1;i>=0;i--) {
				cb.removeItemAt(i);
			}
			if(c.getItemPropertyInterface() != null) {
				n = c.getItemPropertyInterface().getRowCount();
				for(int i=0;i<n;i++) {
					Object o = c.getItemPropertyInterface().getRow(i);
					Listitem ci = cb.appendItem(c.getItemPropertyInterface().getString(o),c.getItemPropertyInterface().getString(o));
					ci.setValue(o);
				}
			} else if(c.getItemList() != null) {
				Vector itemlist = c.getItemList();
				if(itemlist == null) return;
				for(int i = 0;i<itemlist.size();i++) {
					Listitem ci = cb.appendItem(itemlist.get(i).toString(),itemlist.get(i).toString());
					ci.setValue(itemlist.get(i));
				}
			}
		}
	}

	public void cellMap_modechange(Cell c)
	{
		switch(c.getMode()) {
		case Cell.VMODE_DISPONLY :
			if(ic instanceof InputElement) {
				((InputElement)ic).setDisabled(true);
			}
			if(ic instanceof Checkbox) {
				((Checkbox)ic).setDisabled(true);
			}
			if(ic instanceof Button) {
				((Button)ic).setDisabled(true);
			}

			if(ic instanceof Radiogroup) {
				for (Component tmpComp : ((Radiogroup) ic).getChildren()){
					if (tmpComp instanceof Radio){
						((Radio) tmpComp).setDisabled(true);
					}
				}	
			}
			break;
		default :	
			if(ic instanceof InputElement) {
				((InputElement)ic).setDisabled(false);
			}
			if(ic instanceof Checkbox) {
				((Checkbox)ic).setDisabled(false);
			}
			if(ic instanceof Button) {
				((Button)ic).setDisabled(false);
			}
			if(ic instanceof Radiogroup) {
				for (Component tmpComp : ((Radiogroup) ic).getChildren()){
					if (tmpComp instanceof Radio){
						((Radio) tmpComp).setDisabled(false);
					}
				}	
			}
			break;
		}
		cellMap_formulachange(c);
		
	}
	
	public void cellMap_formatchange(Cell c)
	{
		if(c.getIgnoreFormula()) {
			
		}
		DecimalFormat df;
		if((df = c.getDecFormat()) != null) {
			if(ic instanceof Doublebox) {
				((Doublebox) ic).setFormat(df.toPattern());
			}
		}
	}	
	public void cellMap_formulachange(Cell c)
	{
		if(c.getType() != Cell.VTYPE_STRING && c.getMode() != Cell.VMODE_DISPONLY) {
			if(c.getFormula() != null && c.getMode() == Cell.VMODE_NORMAL) {
				if(c.getIgnoreFormula()) {
					if(ic instanceof InputElement) {
						((InputElement) ic).setDisabled(false);
					}
				} else {
					if(ic instanceof InputElement) {
						((InputElement) ic).setDisabled(true);
					}
				}
			} else {
				if(ic instanceof InputElement) {
					((InputElement) ic).setDisabled(false);
				}
			}
		}
		
	}	
	
	public ReturnMsg validateChange(Object p_object)
	{
		String p_newvalue = null;
		Object newValueObj = null;
		Object restoreValue = null;
		try{
			if(p_object instanceof String )  {
				p_newvalue = (String) p_object;
				
				if(bindedCell.getItemPropertyInterface() != null || bindedCell.getItemList() != null) { 
					if(ic instanceof Combobox) {
						if(p_newvalue != null && !((String) p_newvalue).trim().equals("")) {
						Combobox cb = (Combobox) ic;
						int idx = cb.getSelectedIndex();
						if( idx >= 0 && bindedCell.getItemPropertyInterface() != null) {
							if( bindedCell.getItemPropertyInterface().getStatus( 
											cb.getValue(),
											AbstractGetItemProperty.GIPI_DELETED)) {
									idx = -1;
								}
						}
						if(((Combobox) ic).getSelectedIndex() < 0) {
							return(new ReturnMsg(false,"Invalud Input Value"));
						} else {
							if( bindedCell.getItemPropertyInterface() != null) {
							restoreValue = bindedCell.getObject();
							newValueObj = bindedCell.getItemPropertyInterface().getRow(idx);
							return(bindedCell.update(newValueObj,restoreValue));
							}
						}
						}
					}
				}
				
				
			} else {
				if(p_object instanceof Integer) {
					if(ic instanceof Listbox) {
						if(bindedCell.getType() == Cell.VTYPE_INT) {
							int ri = bindedCell.getInt();
							return(bindedCell.update(p_object,ri));
						} else {
							
						}
					}
				}
			}
			if(bindedCell == null) {
				UniLog.logm(this, "bindedCell is null");
				return(new ReturnMsg(true));
			}
			switch(bindedCell.getType()) {
			case Cell.VTYPE_BOOLEAN:
				if(p_newvalue.equals("Y")) {
					return(bindedCell.update(true));
				} else {
					return(bindedCell.update(false));
				}
			case Cell.VTYPE_DATETIME: {
				Date d;
				if(p_newvalue == null || p_newvalue.equals("")) {
					d = com.kyoko.common.DateUtil.zeroDate;
				} else {
					d = com.kyoko.common.DateUtil.dateTimeStrToDate(p_newvalue);
				}
				if(d == null) {
					return(new ReturnMsg(false));
				} else {
					return(bindedCell.update(d,restoreValue));
				}
			}
			case Cell.VTYPE_DATE: {
				Date d;
				if(p_newvalue == null || p_newvalue.equals("")) {
					d = com.kyoko.common.DateUtil.zeroDate;
				} else {
					d = com.kyoko.common.DateUtil.getDateY4MD(p_newvalue);
				}
				if(d == null) {
					return(new ReturnMsg(false));
				} else {
					return(bindedCell.update(d,restoreValue));
				}
			}
			case Cell.VTYPE_STRING:

				if(bindedCell instanceof ColumnCell && ((ColumnCell) bindedCell).getBiResult() != null) {
//					if(((ColumnCell) bindedCell).getBiColumn().isRequired()) {
					if(
							((ColumnCell) bindedCell).getBiResult().isRequired(((ColumnCell) bindedCell).getBiColumn())
							) {
						restoreValue = "";
					}
				}
				return(bindedCell.update(StringUtil.sr(p_newvalue),restoreValue));
			case Cell.VTYPE_INT: 
				try{
					newValueObj = Integer.parseInt( StringUtil.stripDecimal(p_newvalue));
				}
				catch(Exception ex){
					cellMap_valchange(bindedCell); //fallback to org value
					return(new ReturnMsg(false,"Invalid format"));
				}
				return(bindedCell.update(newValueObj,restoreValue));
			case Cell.VTYPE_DOUBLE: 
				try{
					newValueObj = Double.parseDouble( StringUtil.stripDecimal(p_newvalue));
				}
				catch(Exception ex){
					cellMap_valchange(bindedCell);  //fallback to org value
					return(new ReturnMsg(false,"Invalid format"));
				}
				return(bindedCell.update(newValueObj,restoreValue));
			}
			return(new ReturnMsg(true));
		}
		catch(Exception ex){
			return(new ReturnMsg(false, ex));
		}
	}
	
	public void restore() {
		cellMap_valchange(bindedCell);
	}
	
	public Cell getBindedCell() {
		return(bindedCell);
	}
}
