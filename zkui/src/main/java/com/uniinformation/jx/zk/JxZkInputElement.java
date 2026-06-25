package com.uniinformation.jx.zk;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.kyoko.common.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.rpccall.*;

import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.*;
import org.apache.commons.lang3.tuple.Pair;
public class JxZkInputElement extends JxZkElement {
	private final static boolean longOpFlag = true;
	String textValue = null; 
	String orgValue  = null; 
	JxChangeListener changeListener = null;
	public JxZkInputElement(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		comp.addEventListener("onChange", zkEventListener);
		if (longOpFlag){
			comp.addEventListener("onLongOp", zkEventListener);
		}
//		if(comp instanceof InputElement) ((InputElement) comp).setCtrlKeys("^d^e^f");
//		if(comp instanceof XulElement) ((XulElement) comp).setCtrlKeys("^a^b^d^e^f");
		if(comp instanceof XulElement) ((XulElement) comp).setCtrlKeys("^b^d^e^f");
		if (c instanceof InputElement){
			((InputElement) c).setInstant(true);
			comp.addEventListener(Events.ON_OK, zkEventListener);
		}
		//it caused input validation problem for estimation lib
		/*
		if (comp instanceof Textbox){
			((Textbox)comp).setInstant(true);
		}
		*/
	}
	private Component getRootComponent(Component c){
		if (c.getParent() == null){
			return(c);
		}
		else{
			return(getRootComponent(c.getParent()));
		}
	}
	
	@Override
	protected String processAction(Event ev) {
		if (longOpFlag){
			Component rootComp = getRootComponent(ev.getTarget());
			switch(JxZkGadgetProvider.getEventID(ev.getName())) {
				case JxZkGadgetProvider.EV_ONLONGOP:
					processLongAction((Event)ev.getData());
					UniLog.log("JxZkInputElement clear processing: event:" + ev.getName() +" target:" + ev.getTarget());
					Clients.clearBusy(rootComp);
					break;
				default:
					if (ev.getTarget() instanceof Radio){ 
						//remark: click radio button will generate onCheck event to both radio button and radiogroup. why radiogroup onCheck event become onClick event???
						//dirty fix: for radio button, change event target to radio group
						Clients.showBusy(rootComp, ZkUtil.getSessionHelperLabel(getJxField(), "Processing..."));
						UniLog.log("JxZkInputElement show processing: event:" + ev.getName() + " target:" + ev.getTarget());
						Events.echoEvent("onLongOp", ((Radio)ev.getTarget()).getRadiogroup(), ev);
						return(null);
					}
					else{
						Clients.showBusy(rootComp, ZkUtil.getSessionHelperLabel(getJxField(), "Processing..."));
						UniLog.log("JxZkInputElement show processing: event:" + ev.getName() + " target:" + ev.getTarget());
						Events.echoEvent("onLongOp", ev.getTarget(), ev);
					}
					break;
			}
			return(null);
		}
		else{
			return(processLongAction(ev));
		}
	}
	protected String processLongAction(Event ev) {
		JxField fd;
//		int jxValueMode = -1;
		Boolean jxValueProtected = null;
		super.processAction(ev);
		UniLog.log(String.format("JxZkInputElement processLongAction: event:%s target:%s", ev.getName(), ev.getTarget()));
		switch(JxZkGadgetProvider.getEventID(ev.getName())) {
		case JxZkGadgetProvider.EV_ONCTRLKEY:
				UniLog.log("JxZkInputElement ctrl key catched ctro:"+
						((KeyEvent) ev ).isCtrlKey() + " " + 
						((KeyEvent) ev).getKeyCode()
						);
				if(((KeyEvent) ev ).isCtrlKey() && ((KeyEvent) ev).getKeyCode() == 68) {
					fd = getJxField();
					try {
						if(fd.isOverrided()) {
							fd.clearOverride();
						}
					} catch (CellException cex){
						UniLog.log(cex);
					}
				}
				if(((KeyEvent) ev ).isCtrlKey() && ((KeyEvent) ev).getKeyCode() == 69) {
					fd = getJxField();
					com.uniinformation.cell.Cell cc = fd.getJxValue();
					if(cc instanceof ColumnCell) {
						((ColumnCell) cc).restoreValueBeforeDirty();
					}
				}
			break;
		case JxZkGadgetProvider.EV_ONSELECT:
			UniLog.log("Event OnSelect catched");
			if(!(this instanceof JxZkSelector)) return(null);
		case JxZkGadgetProvider.EV_ONCHANGE:
		case JxZkGadgetProvider.EV_ONCHECK:
			
			String s = orgValue;
			if(comp instanceof Listbox) {
				fd = getJxField();
				if(fd != null && fd.getJxFieldType() == JxField.FTYPE_INT ) {
					textValue = ""+ ((Listbox) comp).getSelectedIndex();
				} else {
				Listitem li = ((Listbox) comp).getSelectedItem();
				//ZkUtil.dumpData(li);
				if(li != null) {
//					if(li.getValue() != null && li.getValue() instanceof Pair) textValue = ((Pair) li.getValue()).getLeft().toString();
//						else textValue = li.getLabel(); //just try to follow the combobox logic
						textValue = li.getLabel(); 
				} 
				else {
					textValue = "";
				}
				}
			} else if(comp instanceof Radiogroup) {
				Radiogroup rg = (Radiogroup) comp;
				fd = getJxField();
				if(fd.getJxFieldType() == JxField.FTYPE_INT) {
					textValue = ""+ rg.getSelectedIndex();
				} else {
					textValue = rg.getSelectedItem().getValue();
				}
			} else {
				if(comp instanceof Datebox) {
					Datebox db = (Datebox) comp;
					fd = getJxField();
					if(fd.getJxFieldType() == JxField.FTYPE_DATE) {

						textValue = DateUtil.toDateString(db.getValue(), "yyyy/mm/dd");
					} else {
						textValue = DateUtil.dateToDateTimeStr(db.getValue());
					}
				} else if(comp instanceof Timebox ){
					Timebox tb = (Timebox) comp;
					textValue = DateUtil.dateToDateTimeStr(tb.getValue());
				} else if(comp instanceof Checkbox){
						Checkbox cb = (Checkbox) comp;
						if(cb.isChecked()) {
							textValue = "Y";
						} else {
							textValue = "N";
						}
				} else {

						InputElement ie = (InputElement) comp;
//						textValue = ie.getText();
						String tx;
						if(ie instanceof Combobox) {
							if(getJxField().getJxFieldType() == JxField.FTYPE_INT) {
								int idx = ((Combobox) ie).getSelectedIndex();
								if(idx >= 0) {
									tx = ""+idx;
								} else {
									tx = "";
								}
							} else {
							Comboitem ci = ((Combobox) ie).getSelectedItem();
							if(ci != null && ci.getValue() != null ) {
//								if(ci.getValue() instanceof Pair) {
//									tx = ((Pair) ci.getValue()).getLeft().toString();
//								} else {
//									tx = ie.getText();
//								}
								tx = ie.getText();
							} else {
								tx = ie.getText();
							}
							}
						} else tx = ie.getText();
						
						if(getJxField() != null) {
							com.uniinformation.cell.Cell cc = getJxField().getJxValue();
							if(cc instanceof ColumnCell) {
								BiColumn ccc = ((ColumnCell) cc).getBiColumn();
								if(ccc.isChinese()) {
									int cLen = ccc.getColumnLength();
									Pair cCnt = StringUtil.countChineseCharInBytest(tx,cLen);
									int cC = (Integer) cCnt.getRight();
									int cB = (Integer) cCnt.getLeft();
									if(cC < tx.length()) {
										tx = tx.substring(0,cC);
										ie.setText(tx);
									}
								}
							}
						}
//						if(needAutoConvert(getJxField())) textValue = ChineseConvert.convertAuto2Bnew(ie.getText()); else textValue = ie.getText();
						if(needAutoConvert(getJxField())) textValue = ChineseConvert.convertAuto2Bnew(tx); else textValue = tx;
				}
				
			}
			UniLog.log("JxZkInputElement "+comp.getId()+" -> " + textValue);
			UniLog.log("Object "+this);
			if((fd = getJxField()) != null) {
//				if(fd.getJxValue() != null) jxValueMode = fd.getJxValue().getMode();
				if(fd.getJxValue() != null) jxValueProtected = fd.getJxValue().isProtected();
				UniLog.log("calling JxField Change");
				JxGadgetProvider provider = JxZkGadgetProvider.getCurrentProvider();
				if (provider != null){
					String compPath = (new Path(comp)).getPath();
					UniLog.log("set AFTER_MSGBOX_FOCUS="+compPath);
					provider.setUserData("AFTER_MSGBOX_FOCUS", compPath); //focus need to perform in msgbox
				}
				boolean isValidFlag;
				/*
				if(fd.getJxFieldType() == JxField.FTYPE_INT) {
					if(comp instanceof Radiogroup) {
						String ss = ""+((Radiogroup) comp).getSelectedIndex();
						isValidFlag = fd.validateChange(s,ss);
					} else {
						isValidFlag = fd.validateChange(s,textValue);
					}
				} else {
					isValidFlag = fd.validateChange(s,textValue);
				}
				*/
				isValidFlag = fd.validateChange(s,textValue);
				UniLog.log1("validateChange org:%s new:%s result:%s", s, textValue, isValidFlag);
				if (provider != null){
					provider.setUserData("AFTER_MSGBOX_FOCUS", null);
				}
				if(!isValidFlag){
					UniLog.log("fd.validateChange failed");
					break;
				}
			}	
			if(changeListener != null) {
				UniLog.log("calling valueChange Option");
				if(!changeListener.valueChanged(getJxField(), s)) {
					if(fd.validateChange(textValue,s)) {
						setText(s);
						if(jxValueProtected != null && jxValueProtected) {
							/*
							try {
//								fd.getJxValue().syncMode(com.uniinformation.cell.Cell.VMODE_PROTECTED);
								// cell.isOverride
								fd.getJxValue().setMode(com.uniinformation.cell.Cell.VMODE_PROTECTED);
							} catch (CellException cex){
								UniLog.log(cex);
							}
							*/
							try {
								fd.getJxValue().protect(true);
							} catch(CellException cex) {
								UniLog.log(cex);
							}
						}
					}
				}
			}
			
			orgValue = textValue;
			if((fd = getJxField()) != null) fd.afterChange(textValue);
			getSkin().setDirtyFlag(true);
			break;
		case JxZkGadgetProvider.EV_ONOPEN :
//			if(((JxZkGadgetProvider) JxZkGadgetProvider.getCurrentProvider()).isTouchPanel()) {
//				if(comp instanceof HtmlBasedComponent) {
//					UniLog.log("unfocus component in touch panel mode");
//					((HtmlBasedComponent) comp).setFocus(false);
//				}
//			}
			break;
		}
		if (comp instanceof Listbox && ZkUtil.isSelect2(comp)) {
			ZkUtil.setupSelect2(comp);
		}
		return(null);
	}
	public void setText(String p_text)
	{
		orgValue = p_text;
		if(p_text != null && getJxField() != null){
			com.uniinformation.cell.Cell cc = getJxField().getJxValue();
			if(cc != null && cc instanceof ColumnCell) {
				BiColumn ccc = ((ColumnCell) cc).getBiColumn();
				if(ccc.isChinese()) {
					if(comp instanceof InputElement) {
						InputElement ie = (InputElement) comp;
						int cLen = ccc.getColumnLength();
						Pair cCnt = StringUtil.countChineseCharInBytest(p_text,cLen);
						int cC = (Integer) cCnt.getRight();
						int cB = (Integer) cCnt.getLeft();
						UniLog.log("setTextMaxLength C " + cC + " B " + cB + " max " + (cC + cLen-cB) + " left " + (cLen - cB));
						ie.setMaxlength(cC+cLen-cB);
					}
				}
			}
		}
		if(textValue == null || !textValue.equals(p_text)) {
			textValue = p_text;
			if(comp instanceof Radiogroup) {
				Radiogroup rg = (Radiogroup) comp;
				JxField fd = getJxField();
				if(fd.getJxFieldType() == JxField.FTYPE_INT) {
					int idx;
					if(p_text == null || p_text.trim().equals("")) idx = -1;
						else idx = Integer.parseInt(p_text.trim());
					if(idx >= 0 && idx < rg.getItemCount()) rg.setSelectedIndex(idx);
				} else {
					if(p_text == null) rg.setSelectedIndex(-1) ; else {
						for(Iterator itr = rg.getItems().iterator();itr.hasNext();) {
							Radio r = (Radio) itr.next();
							if(p_text.equals(r.getValue())) {
								rg.setSelectedItem(r);
							}
						}
					}
				}
			} else {
				if(comp instanceof Datebox) {
					Datebox db = (Datebox) comp;
					java.util.Date jd = null;
					if(textValue != null && !textValue.equals("")) {
						jd = DateUtil.getDate(textValue);
						if(jd == null) jd = DateUtil.dateTimeStrToDate(textValue);
					}
					db.setValue(jd);
					textValue = db.getText();
				} else if(comp instanceof ZkJxTimePicker) {
					ZkJxTimePicker tp = (ZkJxTimePicker) comp;
					java.util.Date jd = null;
					if(textValue != null && !textValue.equals("")) {
						jd = DateUtil.dateTimeStrToDate(textValue);
						if(jd.before(DateUtil.minDate)) {
							tp.setText("");
						} else {
							if (getJxField() != null && getJxField().getJxValue() != null && getJxField().getJxValue() instanceof ColumnCell) {
								BiColumn bc = ((ColumnCell)getJxField().getJxValue()).getBiColumn();
								if (StringUtils.isNotBlank(bc.getTimeCompEndTime()))
									tp.setText(DateUtil.dateDigtalToTimeStr(jd, !bc.getTimeCompIsShortFmt()));
								else
									tp.setText(DateUtil.dateToTimeStr(jd, !bc.getTimeCompIsShortFmt()));
							}
							else
								tp.setText(DateUtil.dateToTimeStr(jd));
						}
					} else {
						tp.setText("");
					}
				} else if(comp instanceof Timebox) {
					Timebox tb = (Timebox) comp;
					java.util.Date jd = null;
					if(textValue != null && !textValue.equals("")) {
						jd = DateUtil.dateTimeStrToDate(textValue);
					}
					tb.setValue(jd);
				} else if(comp instanceof Checkbox) {
					Checkbox cb = (Checkbox) comp;
					if(textValue != null && textValue.trim().equals("Y")) {
						cb.setChecked(true);
					} else {
						cb.setChecked(false);
					}
				} 
				else if(comp instanceof Intbox) {
					Intbox intbox = (Intbox) comp;
						try {
							intbox.setValue(Integer.parseInt(StringUtil.toNumberOnly(textValue)));
						} catch (NumberFormatException nex) {
							UniLog.log(nex);
							intbox.setValue(0);
						}
						/*
					try {
						intbox.setValue(Integer.parseInt(textValue));
					} catch (NumberFormatException nex) {
						intbox.setValue((int) Double.parseDouble(textValue));
					}
						*/
				} 
				else if(comp instanceof Doublebox) {
					Doublebox doublebox = (Doublebox) comp;
					try {
						if(textValue.equals("NaN")) {
							doublebox.setValue(Double.NaN);
						} else {
							doublebox.setValue(Double.parseDouble(StringUtil.toNumberOnly(textValue)));
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} 
				else if(comp instanceof InputElement) {
					if(comp instanceof Combobox && getJxField() != null && getJxField().getJxFieldType() == JxField.FTYPE_INT) {
						Combobox cb = (Combobox) comp;
						int idx = -1;
						if(p_text != null && !p_text.trim().equals("")) idx = Integer.parseInt(p_text.trim());
						if(idx >= 0) {
							cb.setSelectedIndex(idx);
						}	else {
							cb.setText("");
						}
					} else {
						InputElement ie = (InputElement) comp;

						try {
							ie.setText(textValue);
//							ie.setText(ChineseConvert.convertAuto2Gnew(textValue));
						} 
						catch(Exception ex) {
							//UniLog.log(ex);
							UniLog.log1("setText fail. comp:%s value:[%s] errMsg:%s", comp.getId(), textValue, ex.getMessage());
							if (ie instanceof Textbox) {
								ie.setText("Error");
							}
							else {
								ie.setText(null);
							}
						}
					}
				}
				else {
					UniLog.log1("ignore unsupported comp. comp:%s value:%s", comp.getId(), textValue);
				}
			}	
		}
	}
	public String getText()
	{
		return textValue;
	}	
	public void addChangeListener(JxChangeListener x)
	{
		changeListener = x;
	}
	
	public void setEnable(boolean b)
	{
		if(getJxField() != null && getJxField().getJxValue() != null) {
			if(getJxField().getJxValue().getCellLabel().equals("inv_addr0")) {
				int cc;

				cc = 0;
			}
			
		}
		if(comp instanceof InputElement) {
			//UniLog.log("HAHA setEnable " + getName() + " " + b);
			((InputElement) comp).setDisabled(!b);  //remark: change to readonly is more visible, but it may break the jxzk mapping logic
		}
		if(comp instanceof Checkbox) {
			((Checkbox) comp).setDisabled(!b);  //remark: change to readonly is more visible, but it may break the jxzk mapping logic
		}
	}
	public void setJxField(JxField f)
	{
		super.setJxField(f);
		if(comp instanceof InputElement) {
			jxfield_setEnable(!((InputElement) comp).isDisabled());
		}
	}	
	
	public void setInstant(boolean p_flag)
	{
		if (comp instanceof InputElement){
			((InputElement) comp).setInstant(p_flag);
		}
	}
	@Override
	public void setAttribute(String p_attr,String p_value)
	{	
		if(p_attr.equals("multiLine")) {
			if(comp instanceof Textbox) {
				int n = Integer.parseInt(p_value);
				((Textbox) comp).setRows(n);
			}
		}
	}
	
	@Override
	public Object getValue() {
		return(getText());
	}
	@Override
	public void setFontColor(int p_color)
	{
			if(comp instanceof InputElement) {
				switch(p_color) {
				/*
				case 0x800000 :
				case 0xff0000 :
					ZkUtil.setFontColor((Textbox) comp, "red");
					break;	
					*/
				case 0x008000 :
				case 0x00ff00 :
					ZkUtil.setFontColor((InputElement) comp, "green");
					break;	
				case 0x000080 :
				case 0x0000ff :
					ZkUtil.setFontColor((InputElement) comp, "blue");
					break;	
				default:
					ZkUtil.setFontColor((InputElement) comp, null);
					break;	
				}
			}
	}
	
	public String getElementType() {
		if(comp instanceof ZkJxTimePicker) {
			return("TimePicker");
		}
		return("");
	}
}
