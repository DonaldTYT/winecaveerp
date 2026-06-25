package com.uniinformation.zkbi;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Html;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.impl.InputElement;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jx.zk.ZkJxTimePicker;
import com.uniinformation.jx.zk.ZkJxTimePickerList;
import com.uniinformation.jxapp.JxZkBiBase;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkcomp.S2Listbox;

public class ZkBiCellValueMapper implements CellValueMapper {
	Component ic;
	Cell bindedCell;
	AbstractGetItemProperty gipi;
	public ZkBiCellValueMapper(Component comp,AbstractGetItemProperty p_gipi) {
		ic = comp;
		if(comp instanceof S2Listbox) {
			ic = ((S2Listbox) comp).getComp();
		}
		gipi = p_gipi;
		if(ic instanceof Bandbox) {
			ic.addEventListener("onOpen",new EventListener() {
				public void onEvent(Event event) throws Exception {	
					if(((ZkJxPickInput) ic).isOpen()) {
						gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_PULLDOWN_OPENED);
					} else {
						gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_PULLDOWN_CLOSED);
					}
				}
			});
		}
		if(ic instanceof Radiogroup) {
			ic.addEventListener(Events.ON_CHECK, 
			new EventListener() {
				public void onEvent(Event event) throws Exception {
					UniLog.log("ZkBiCellValueMapper component clicked");
					Component tb = event.getTarget();
					String s;
					ReturnMsg retMsg = null;
					try{
						Radiogroup rg = (Radiogroup) ic;
						int idx = rg.getSelectedIndex();
						retMsg = ((ZkBiCellValueMapper) ic.getAttribute("CellValueMapper")).validateChange(idx); 
						if(retMsg.getStatus()) {
							gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_CHANGED);
						} 
					}
					catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		    );
		}
		if(ic instanceof Listbox) {
			ic.addEventListener(Events.ON_SELECT, 
				new EventListener() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("ZkBiCellValueMapper listbox clicked");
						Listbox tb = (Listbox) event.getTarget();
						String s;
						ReturnMsg retMsg = null;
						try{
							String ov = "";
							Object orgV = null;
							Listitem si = tb.getSelectedItem();
							if(si != null) {
								ov = si.getLabel();
							}
							if(bindedCell != null && bindedCell.getType() == Cell.VTYPE_INT) {
								int idx = ((Listbox) ic).getSelectedIndex();
								orgV = idx;
								
							} else {
								//orgV = si.getValue();
								//andrew231018 hot fix for si null
								if (si != null) {
									orgV = si.getValue();
								}
							}
							
							Object changedValue = gipi.onBeforeValueChange(bindedCell, orgV);
							
//							onBeforeValueChange  will not alter selected value for listbox
//							if(changedValue != null) {
//								orgV = changedValue;
//								ov = orgV.toString();
//								((InputElement) tb).setText(ov);
//							}
							retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(orgV); 
							if(retMsg.getStatus()) {
								gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_CHANGED);
							} else {
								UniLog.log("list box change validate failed");
//								tb.setErrorMessage(retMsg.getMsg());
							}
						}
						catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}
			);
			if(comp instanceof S2Listbox) {

//				ZkUtil.setupSelect2(ic);
			}
		} if(ic instanceof Checkbox) {
			ic.addEventListener(Events.ON_CLICK, 
				new EventListener() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("ZkBiCellValueMapper component clicked");
						Component tb = event.getTarget();
						String s;
						ReturnMsg retMsg = null;
						try{
							String ov = "";
							Object orgV = null;
							orgV = ((Checkbox)tb).isChecked();
							if(((Checkbox)tb).isChecked()) {
								retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("Y"); 
							} 
							else {
								retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("N"); 
							}
							if(retMsg.getStatus()) {
								gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_CHANGED);
							} 
						}
						catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}
			);
		} else 
			ic.addEventListener(Events.ON_CHANGE, 
				new EventListener() {
				public void onEvent(Event event) throws Exception {
					UniLog.log("ZkBiCellValueMapper component changed");
					Component tb = event.getTarget();
					String s;
					ReturnMsg retMsg = null;
					try{
						String ov = "";
						Object orgV = null;
						if(tb instanceof Datebox) {
							orgV = ((Datebox) tb).getValue();
							//s = com.uniinformation.utils.DateUtil.toDateString(((Datebox) tb).getValue(), "yyyy/mm/dd");
							//xjcheng211228: fix cannot update time properly
							s = com.kyoko.common.DateUtil.dateToDateTimeStr(((Datebox) tb).getValue(), StringUtils.defaultIfBlank(((Datebox) tb).getFormat(), "yyyy/MM/dd"));
							retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(s); 
						} 
						else if(tb instanceof Checkbox) {
							orgV = ((Checkbox)tb).isChecked();
							if(((Checkbox)tb).isChecked()) {
								retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("Y"); 
							} 
							else {
								retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("N"); 
							}
						} 
						else if(tb instanceof Doublebox) {
							orgV = ov = ((InputElement) tb).getText();
							retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(((Doublebox) tb).getValue()); 
						}
						else if(tb instanceof Intbox) {
							orgV = ov = ((InputElement) tb).getText();
							retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(((Intbox) tb).getValue()); 
						}
						else {
//							orgV = ov = ((InputElement) tb).getText();
//							orgV = ov = ChineseConvert.convertAuto2Bnew(((InputElement) tb).getText());
							if(tb instanceof Combobox && bindedCell.getType() == Cell.VTYPE_INT) {
								ov = ((Combobox) tb).getText();
								orgV = ((Combobox) tb).getSelectedIndex();
							} else {
								if(needAutoConvert()) orgV = ov = ChineseConvert.convertAuto2Bnew(((InputElement) tb).getText()); else orgV = ov = ((InputElement) tb).getText();
							}
							Object changedValue = gipi.onBeforeValueChange(bindedCell, orgV);
							if(changedValue != null) {
								orgV = changedValue;
								ov = orgV.toString();
								((InputElement) tb).setText(ov);
								
							}
//							retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(((InputElement) tb).getText()); 
							retMsg = ((ZkBiCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(orgV);
						}
						if(retMsg.getStatus()) {
//							currcol = editRow.indexOf(tb);
//							lastActionType = JxField.ACTIONTYPE_EDITCELLCHANGED;
//							actionListener.actionPerformed(getJxField());
//							lastActionType = 0;
							// how to notify top app that value has changed ?
							gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_CHANGED);
						} 
						else {
							/*
							if (tb instanceof InputElement){
								if(!((String) orgV).equals("")) {
									((InputElement) tb).setText((String) orgV);
									((InputElement) tb).setErrorMessage(retMsg.getMsg());
								}
							}
							*/
							cellMap_valchange(bindedCell);
							if (tb instanceof InputElement){
//								((InputElement) tb).setErrorMessage(retMsg.getMsg());
								Clients.showNotification(retMsg.getMsg(), "warning", tb, "end_center", 3000);
							}
						}
					}
					catch(Exception ex){
//						ex.printStackTrace();
						UniLog.log(ex);
						/*
						if (tb instanceof InputElement){
							String dispMsg = ex.getMessage();
							if (dispMsg.contains(":")){
								dispMsg = dispMsg.replaceFirst("^.*: ", "");
							}
							((InputElement) tb).setErrorMessage(dispMsg);
						}
						*/
					}
				}		
				}
			);
		ic.addEventListener(Events.ON_OK, 
				new EventListener() {
				public void onEvent(Event event) throws Exception {
					UniLog.log("ZkBiCellValueMapper component ok");
					Component tb = event.getTarget();
					gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_ONOK);
				}		
			}
		);	
		if(ic instanceof InputElement) {
			((InputElement) ic).setCtrlKeys("^d");
			ic.addEventListener(Events.ON_CTRL_KEY, 
				new EventListener() {
				public void onEvent(Event ev) throws Exception {
					UniLog.log("control key pressed");
				if(((KeyEvent) ev ).isCtrlKey() && ((KeyEvent) ev).getKeyCode() == 68) {
					try {
//					if(bindedCell.getMode() == Cell.VMODE_OVERRIDED) {
//						bindedCell.syncMode(Cell.VMODE_PROTECTED);
					if(bindedCell.isOverrided()) {
						gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_CHANGED);
						bindedCell.clearOverride();
					}
					} catch (CellException cex){
						UniLog.log(cex);
					}
				}
				}		
			}
			);	
			ic.addEventListener(Events.ON_SWIPE, 
				new EventListener() {
				public void onEvent(Event ev) throws Exception {
					UniLog.log("swipe catched");
					try {
					if(bindedCell.isOverrided()) {
						gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_CHANGED);
						bindedCell.clearOverride();
					}
					} catch (CellException cex){
						UniLog.log(cex);
					}
				}		
			}
			);	
		}
		if(ic instanceof Button) {
			ic.addEventListener(Events.ON_CLICK, 
				new EventListener() {
				public void onEvent(Event ev) throws Exception {
					UniLog.log("listbox cell button pressed");
					if(bindedCell.getType() == Cell.VTYPE_INT) {
						int clickCnt = bindedCell.getInt() + 1;
						ReturnMsg retMsg = validateChange(new Integer(clickCnt));
						if(retMsg.getStatus()) {
							gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_VALUE_CHANGED);
						} 
					}
				}		
			}
			);	
		}
		if(ic instanceof Datebox) {
//			if( ((ColumnCell) bindedCell).getBiResult().getSessionHelper().isMobile()) {
			if(ZkSessionHelper.getSessionHelper().isMobile()) {
				JxZkBiBase.setMobileDateInput((Datebox) ic);
			}
		}
		ic.setAttribute("CellValueMapper", this);
	}
	public void cellMap_bind(Cell p_cell)
	{
		if(p_cell != null && bindedCell != null) bindedCell.map(null);
		bindedCell = p_cell;
		if(bindedCell instanceof ColumnCell) {
//			if(((ColumnCell) bindedCell).getBiColumn().isRequired()) {
			if(
				((ColumnCell) bindedCell).getBiResult().isRequired(
				((ColumnCell) bindedCell).getBiColumn())
					) {
				if(ic instanceof InputElement) {
					((InputElement) ic).setPlaceholder(((ColumnCell) bindedCell).getBiResult().getSessionHelper().getLabel("Required"));
				} 
			} else {
				if(ic instanceof InputElement) {
					((InputElement) ic).setPlaceholder(null);
				} 
			}
		}
		if(bindedCell != null) {
			cellMap_hintchange(bindedCell);
			cellMap_modechange(bindedCell);
			cellMap_formatchange(bindedCell);
			cellMap_formulachange(bindedCell);
			cellMap_listchange(bindedCell);
			cellMap_valchange(bindedCell);
			gipi.onValueChanged(bindedCell, AbstractGetItemProperty.GIPI_CELL_MAPPED);
		}
	}
	
	public void cellMap_valchange(Cell c)
	{
		if(bindedCell != null) {
//			UniLog.log("cellMap_valchange " + jxValue.getString());
			if(!(ic instanceof InputElement))  {
				if(ic instanceof Radiogroup) {
					switch(bindedCell.getType()) {
					case Cell.VTYPE_INT    :
						((Radiogroup) ic).setSelectedIndex(bindedCell.getInt());
						break;
					case Cell.VTYPE_STRING :
						boolean matched = false;
						for (Component tmpComp : ((Radiogroup) ic).getChildren()){
							if (tmpComp instanceof Radio){
								Object o = ((Radio) tmpComp).getValue();
								if(bindedCell.getString().equals(o.toString())) {
									((Radiogroup) ic).setSelectedItem((Radio) tmpComp);
									matched = true;
									break;
								}
							}
						}	
						if(!matched) ((Radiogroup) ic).setSelectedIndex(-1);
						break;
					default:
						UniLog.log(new Exception("ERROR !!! ZkBiCellValueMapper Radiogroup binded to non-integer cell"));
					}
				}
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
					((Label) ic).setValue(c.getString());
				}
				if(ic instanceof Html) {
					((Html) ic).setContent(c.getString());
				}
				if(ic instanceof Listbox) {
					if(ic instanceof ZkJxTimePickerList ) {
//						ic.setText(DateUtil.dateToTimeStr(d));
						((ZkJxTimePickerList) ic).setSelectedItemByTime(bindedCell.getDate());
					} else {
						int idx = -1;
						AbstractGetItemProperty gipi = bindedCell.getItemPropertyInterface();
						if(gipi != null) {
							idx = gipi.getIndexOf(bindedCell.getObject());
						} else {
							if(bindedCell != null && bindedCell.getType() == Cell.VTYPE_INT) {
								idx = bindedCell.getInt();
							} else {
								Vector il = bindedCell.getItemList();
								if(il != null) idx = il.indexOf(bindedCell.getObject());
							}
						}
						if(idx < 0) {
							if(bindedCell.getType() == Cell.VTYPE_INT || bindedCell.isBlank())
								((Listbox) ic).setSelectedIndex(-1); 
							else {
								Listbox cb = (Listbox) ic;
								Listitem ci = cb.appendItem(bindedCell.getString(),bindedCell.getString());
								ci.setValue(bindedCell.getString());
								cb.setSelectedItem(ci);
							}
						} else {
							((Listbox) ic).setSelectedIndex(idx); 
							// fixed by Andriew 2023/04/15 to sync between listbox and inputbox value
							if(ZkUtil.isSelect2(((ColumnCell) bindedCell).getBiResult().getSessionHelper(),ic)) {
								ZkUtil.setupSelect2(ic);
							}
							
						}
//						Vector il = bindedCell.getItemList();
//						if(il != null) {
//							int idx = il.indexOf(bindedCell.getObject());
//							if(idx >= 0) {
//								((Listbox) ic).setSelectedIndex(idx); 
//							} else {
//								//((Listbox) ic).setSelectedIndex(-1); 
//								Listbox cb = (Listbox) ic;
//								Listitem ci = cb.appendItem(bindedCell.getString(),bindedCell.getString());
//								ci.setValue(bindedCell.getString());
//								cb.setSelectedItem(ci);
//							}
//						} else {
//							if(bindedCell.isBlank())
//								((Listbox) ic).setSelectedIndex(-1); 
//							else {
//								Listbox cb = (Listbox) ic;
//								Listitem ci = cb.appendItem(bindedCell.getString(),bindedCell.getString());
//								ci.setValue(bindedCell.getString());
//								cb.setSelectedItem(ci);
//								/*
//								int n = cb.getItemCount();
//								for(int i = n-1;i>=0;i--) {
//									cb.removeItemAt(i);
//								}
//								*/
//								
//							}
//						}
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
				} else {
					if(bindedCell.getType() == Cell.VTYPE_INT) {
						int idx = bindedCell.getInt();
						if(idx >= 0) {
							((Combobox) ic).setSelectedIndex(idx);
						} else {
							((Combobox) ic).setText("");
						}
						return;
					}
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

						if(!d.after(DateUtil.minTime)) {
							((Datebox) tb).setText("");
						} else {
							((Datebox) tb).setValue(d);
						}
					} else if(tb instanceof Timebox) {
						if(!d.after(DateUtil.minTime)) {
							((Timebox) tb).setText("");
						} else {
							((Timebox) tb).setValue(d);
						}
					} else {
						if(tb instanceof ZkJxTimePicker ) {
							if (bindedCell instanceof ColumnCell) {
								BiColumn bc = ((ColumnCell)bindedCell).getBiColumn();
								if (StringUtils.isNotBlank(bc.getTimeCompEndTime()))
									tb.setText(DateUtil.dateDigtalToTimeStr(d, !bc.getTimeCompIsShortFmt()));
								else
									tb.setText(DateUtil.dateToTimeStr(d, !bc.getTimeCompIsShortFmt()));
							} else
								tb.setText(DateUtil.dateToTimeStr(d));
						} else {
							tb.setText(DateUtil.dateToDateTimeStr(d));
						}
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
		String s = c.getHint();
		if(s != null && !s.trim().equals("")) {
			if(ic instanceof InputElement ) {
//				((InputElement) ic).
				Clients.showNotification(s, "warning", (InputElement) ic, "end_center", 3000);
			}
		}
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
					ci.setValue(o);
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
					if(bindedCell instanceof ColumnCell){
						BiColumn bc = ((ColumnCell) bindedCell).getBiColumn();
//						if(bc.getOptionList( ((ColumnCell) bindedCell).getBiResult().getSessionHelper() 
						if(bc.getOptionList( ((ColumnCell) bindedCell).getBiResult(),null
								) != null) {
							if(StringUtils.isBlank(itemlist.get(i).toString())) {
								ci.setVisible(false);
							}
						}
					}
				}
			}
			if(bindedCell instanceof ColumnCell){
				if(ZkUtil.isSelect2(((ColumnCell) bindedCell).getBiResult().getSessionHelper(),ic)) {
					ZkUtil.setupSelect2(ic);
				}
			}
			cb.invalidate();
		}
		
			if(ic instanceof Radiogroup) {
				Radiogroup cb = (Radiogroup) ic;
				int n = cb.getItemCount();
				for(int i = n-1;i>=0;i--) {
					cb.removeItemAt(i);
				}
				AbstractGetItemProperty gipi = bindedCell.getItemPropertyInterface();
				if(gipi != null) {
					for(int i = 0;i<gipi.getRowCount();i++) {
						Radio rd;
						Object item = gipi.getRow(i);
						rd =  cb.appendItem(gipi.getString(item),item.toString());
						if((item == null || item.toString().trim().equals(""))) {
							rd.setVisible(false);
						}
						if(c.getMode() == Cell.VMODE_DISPONLY) rd.setDisabled(true);
					}	
				} else {
					List itemlist = bindedCell.getItemList();
					if(itemlist != null) {
						for(int i = 0;i<itemlist.size();i++) {
							Object item = itemlist.get(i);
							Radio rd;
							rd =  cb.appendItem(item.toString(),null);
							rd.setValue(itemlist.get(i));
							if((item == null || item.toString().trim().equals(""))) {
								rd.setVisible(false);
							}
							if(c.getMode() == Cell.VMODE_DISPONLY) rd.setDisabled(true);
						}	
					}
				}
			}
	}

	void disableField() {
			if(ic instanceof InputElement) {
				((InputElement)ic).setDisabled(true);
				return;
			}
			if(ic instanceof Button) {
				((Button)ic).setDisabled(true);
				return;
			}
			if(ic instanceof Checkbox) {
				((Checkbox)ic).setDisabled(true);
				return;
			}
			if(ic instanceof Radiogroup) {
				for (Component tmpComp : ((Radiogroup) ic).getChildren()){
					if (tmpComp instanceof Radio){
						((Radio) tmpComp).setDisabled(true);
					}
				}	
				return;
			}
			if(ic instanceof Listbox) {
				((Listbox) ic).setDisabled(true);
				return;
			}
	}
	void enableField() {
			if(ic instanceof InputElement) {
				((InputElement)ic).setDisabled(false);
				return;
			}
			if(ic instanceof Button) {
				((Button)ic).setDisabled(false);
				return;
			}
			if(ic instanceof Checkbox) {
				((Checkbox)ic).setDisabled(false);
				return;
			}
			if(ic instanceof Radiogroup) {
				for (Component tmpComp : ((Radiogroup) ic).getChildren()){
					if (tmpComp instanceof Radio){
						((Radio) tmpComp).setDisabled(false);
					}
				}	
				return;
			}
			if(ic instanceof Listbox) {
				((Listbox) ic).setDisabled(false);
				return;
			}
	}
	
	public void cellMap_modechange(Cell c)
	{
		if(c.getMode() == Cell.VMODE_HIDDEN) {
			ic.setVisible(false);
		} else {
			ic.setVisible(true);
		}
		switch(c.getMode()) {
		case Cell.VMODE_DISPONLY :
			/*
			if(ic instanceof InputElement) {
				((InputElement)ic).setDisabled(true);
			}
			if(ic instanceof Button) {
				((Button)ic).setDisabled(true);
			}
			if(ic instanceof Checkbox) {
				((Checkbox)ic).setDisabled(true);
			}
			if(ic instanceof Radiogroup) {
				for (Component tmpComp : ((Radiogroup) ic).getChildren()){
					if (tmpComp instanceof Radio){
						((Radio) tmpComp).setDisabled(true);
					}
				}	
			}
			*/
			disableField();
			break;
		default :	
			/*
			if(ic instanceof InputElement) {
				((InputElement)ic).setDisabled(false);
			}
			if(ic instanceof Button) {
				((Button)ic).setDisabled(false);
			}
			if(ic instanceof Checkbox) {
				((Checkbox)ic).setDisabled(false);
			}
			if(ic instanceof Radiogroup) {
				for (Component tmpComp : ((Radiogroup) ic).getChildren()){
					if (tmpComp instanceof Radio){
						((Radio) tmpComp).setDisabled(false);
					}
				}	
			}
			*/
			if(c.isProtected() || c.getFormula() == null  || c.getIgnoreFormula()) {
				enableField();
			} else {
				disableField();
			}
			break;
		}
		if(ic instanceof InputElement) {
//			if(c.getMode() == Cell.VMODE_OVERRIDED) {
			if(c.isOverrided()) {
				ZkUtil.setFontColor((InputElement) ic, "blue");
			} else {
				ZkUtil.setFontColor((InputElement) ic, null);
			}
		}
		cellMap_formulachange(c);
	}
	public void cellMap_formatchange(Cell c)
	{
		if(c.getIgnoreFormula()) {
			
		}
	}	
	public void cellMap_formulachange(Cell c)
	{
//		if(/* c.getType() != Cell.VTYPE_STRING && */ c.getMode() != Cell.VMODE_DISPONLY) {
//			if(c.getFormula() != null && !c.isProtected()) {
//				if(c.getIgnoreFormula()) {
//					if(ic instanceof InputElement) {
//						((InputElement) ic).setDisabled(false);
//					}
//				} else {
//					if(ic instanceof InputElement) {
//						((InputElement) ic).setDisabled(true);
//					}
//				}
//			} else {
//				if(ic instanceof InputElement) {
//					((InputElement) ic).setDisabled(false);
//				}
//			}
//		}
		if(c.getMode() == Cell.VMODE_DISPONLY) {
			disableField();
		} else if(c.getMode() == Cell.VMODE_NORMAL) {
			if(c.isProtected() || c.getFormula() == null  || c.getIgnoreFormula()) {
				enableField();
			} else {
				disableField();
			}
		}
	}	
	public ReturnMsg validateChange(Object p_newvalue)
	{
		try{
			Object restoreValue = null;
			Object newValueObj = null;
			if(bindedCell == null) {
				UniLog.logm(this, "bindedCell is null");
				return(new ReturnMsg(true));
			}
//			if(p_newvalue != null) {
//				if(ic instanceof Combobox) {
//				if(p_newvalue instanceof String && !((String) p_newvalue).trim().equals("")) {
//					if(((Combobox) ic).getSelectedIndex() < 0) {
//						return(new ReturnMsg(false,"Invalud Input Value"));
//					}
//				}
//				}
//			}
			
			if(bindedCell.getItemPropertyInterface() != null || bindedCell.getItemList() != null) { 
				if(bindedCell == null || bindedCell.getType() != Cell.VTYPE_INT || bindedCell.getItemPropertyInterface() != null) {
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
					if(((Combobox) ic).getSelectedIndex() < 0 && 
							(bindedCell.getItemPropertyInterface() != null || !((ColumnCell) bindedCell).getBiResult().getSessionHelper().useS2ListboxForReadOnly())
							) {
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
				if(ic instanceof Listbox) {
					Listbox lb = (Listbox) ic;
					int idx = lb.getSelectedIndex();
					if(idx < 0) bindedCell.resetValue();
					else {
						Listitem li = lb.getSelectedItem();
						//andrew220803 hotfix getItemPropertyInterface null exception
						if( bindedCell.getItemPropertyInterface() != null && bindedCell.getItemPropertyInterface().getStatus( 
										li.getValue(),
										AbstractGetItemProperty.GIPI_DELETED)) {
								idx = -1;
							}
					}
				}
				}
			}
			
			
			switch(bindedCell.getType()) {
			case Cell.VTYPE_BOOLEAN:
				Boolean B;
				if(p_newvalue instanceof Boolean) B = (Boolean) p_newvalue;
				else 
				if(p_newvalue.equals("Y")) {
					B = true;
					return(bindedCell.update(true));
				} else {
					B = false;
				}
				return(bindedCell.update(B));
			case Cell.VTYPE_DATETIME: 
			case Cell.VTYPE_DATE: {
				Date d;
				if(p_newvalue == null || p_newvalue.equals("")) {
					d = com.kyoko.common.DateUtil.zeroDate;
				} else {
					if(p_newvalue instanceof Date) {
						d = (Date) p_newvalue;
					} else {
						if(bindedCell.getType() == Cell.VTYPE_DATE) {
							d = com.kyoko.common.DateUtil.getDateY4MD((String) p_newvalue);
						} else {
							d = com.kyoko.common.DateUtil.dateTimeStrToDate((String) p_newvalue);
						}
					}
				}
				if(d == null) {
					return(new ReturnMsg(false));
				} else {
					return(bindedCell.update(d,restoreValue));
				}
			}	
			case Cell.VTYPE_STRING:
				if(bindedCell instanceof ColumnCell) {
//					if(((ColumnCell) bindedCell).getBiColumn().isRequired()) {
					if(
						((ColumnCell) bindedCell).getBiResult().isRequired(
						((ColumnCell) bindedCell).getBiColumn())
						) {
						restoreValue = "";
					}
				}
				if(ic instanceof Radiogroup) {
					Radiogroup rg = (Radiogroup) ic;
					String rdStr = null;
					int idx = (Integer) p_newvalue;
					if(idx < 0) rdStr = ""; else rdStr = rg.getSelectedItem().getValue().toString();
					return(bindedCell.update(rdStr,restoreValue));
				} else return(bindedCell.update(StringUtil.sr((String) p_newvalue),restoreValue));
			case Cell.VTYPE_INT: 
				try{
					if(p_newvalue instanceof Integer ) {
						newValueObj = (Integer) p_newvalue;
						
					} else newValueObj = Integer.parseInt((String) p_newvalue);
				}
				catch(Exception ex){
					cellMap_valchange(bindedCell); //fallback to org value
					return(new ReturnMsg(false,"Invalid format"));
				}
				return(bindedCell.update(newValueObj,restoreValue));
			case Cell.VTYPE_DOUBLE: 
				try{
					if(p_newvalue instanceof Double ) {
						newValueObj = (Double) p_newvalue;
					} else newValueObj = Double.parseDouble((String) p_newvalue);
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
			UniLog.log(ex);
			return(new ReturnMsg(false, ex));
		}
	}
	
	public void restore() {
		cellMap_valchange(bindedCell);
	}
	
	public Cell getBindedCell() {
		return(bindedCell);
	}
	public Component getComponent() {
		return(ic);
	}
	
	boolean needAutoConvert() { 
		if(bindedCell instanceof ColumnCell) {
//			return(!((ColumnCell) bindedCell).getBiColumn().isUTF8());
			return(
					((ColumnCell) bindedCell).getBiResult().needAutoConvert(
								((ColumnCell) bindedCell).getBiColumn()
							)
					);
		}
		return(false);
	}
}
