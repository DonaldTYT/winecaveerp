package com.uniinformation.jx.zk;

import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;

import java.util.Vector;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;

public class JxZkButton extends JxZkElement {
	public JxZkButton(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		comp.addEventListener("onClick", zkEventListener);
//		if(comp instanceof Toolbarbutton) comp.addEventListener("onChange", zkEventListener);
	}
	public void setEnable(boolean b)
	{
		UniLog.log("HAHA enable button " + b);
		((Button) comp).setDisabled(!b);
	}	
	public void setJxField(JxField f)
	{
		super.setJxField(f);
		if(comp instanceof Button) {
			jxfield_setEnable(!((Button) comp).isDisabled());
		}
	}	
	public void setAttribute(String p_attr,String p_value)
	{
		UniLog.log("button setattribute "+p_attr + " "  + p_value);
		if(p_attr.equals("mode")) {
			if(comp instanceof Toolbarbutton) {
				((Toolbarbutton) comp).setMode(p_value);
			}
		}
	}
	public String getText() {
		if(comp instanceof Toolbarbutton) {
			if(((Toolbarbutton) comp).isChecked()) {
				return("Y");
			} else {
				return("N");
			}
		} else return(null);
	}
	public void setText(String p_text) {
		if(comp instanceof Toolbarbutton) {
			if(p_text != null && p_text.equals("Y")) {
				((Toolbarbutton) comp).setChecked(true);
			} else {
				((Toolbarbutton) comp).setChecked(false);
			}
		}
		else if(comp instanceof Image) {
			if (p_text != null){
				Image xx = ((Image) comp);
				xx.setSrc(p_text);
			}
		} else if(comp instanceof Button) {
			if (p_text != null){
				((Button) comp).setLabel(p_text);
			}
		}
	}
	
	@Override
	public Object getValue() {
		return(null);
	}
}
