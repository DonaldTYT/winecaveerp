package com.uniinformation.jx.zk;

import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;

import java.util.List;
import java.util.Vector;




import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;

public class JxZkRadiogroup extends JxZkInputElement {
	boolean isDisabled = false;
	public JxZkRadiogroup(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		comp.addEventListener("onCheck", zkEventListener);
		UniLog.log("HAHA 2016 JxZKRadiogroup created " + getName());
	}
	
	@Override
	public void setItemList(List itemlist)
	{
			Radiogroup cb = (Radiogroup) comp;
			int n = cb.getItemCount();
			for(int i = n-1;i>=0;i--) {
				cb.removeItemAt(i);
			}
			if(itemlist == null) return;
			for(int i = 0;i<itemlist.size();i++) {
				String item = itemlist.get(i).toString();
//				Radio rd =  cb.appendItem(item,item);
				Radio rd =  cb.appendItem(item,null);
				if(isDisabled) rd.setDisabled(true);
				rd.setValue(itemlist.get(i));
//				if(i==0 && (item == null || item.trim().equals(""))) {
				if((item == null || item.trim().equals(""))) {
					rd.setVisible(false);
				}
			}
	}	
	
	@Override
	public int getItemIndex()
	{
		return(((Radiogroup) comp).getSelectedIndex());
	}
	@Override
	public void setItemIndex(int p_idx)
	{
		((Radiogroup) comp).setSelectedIndex(p_idx);
	}
	@Override
	public void setEnable(boolean b)
	{
		for (Component tmpComp : ((Radiogroup) comp).getChildren()){
			if (tmpComp instanceof Radio){
				((Radio) tmpComp).setDisabled(!b);
			}
		}
		isDisabled = !b;
	}
	
	@Override
	public Object getValue() {
		Radiogroup rg = (Radiogroup) comp;
		Radio rd = rg.getSelectedItem();
		if(rd != null) return(rd.getValue()); else return(null);
	}
}
